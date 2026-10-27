-- Additive admin authoring and future view analytics. Existing grading/reward RPCs stay intact.
create table ar_reels_private.admin_emails(email text primary key check(email=lower(email)),active boolean not null default true,created_at timestamptz not null default now());
alter table ar_reels_private.admin_emails enable row level security;
revoke all on ar_reels_private.admin_emails from public,anon,authenticated;
create function ar_reels_private.require_admin() returns uuid language plpgsql security definer set search_path='' as $$
declare u uuid:=auth.uid();begin
 if u is null or coalesce((nullif(current_setting('request.jwt.claims',true),'')::jsonb->>'is_anonymous')::boolean,true) then raise exception 'Verified administrator sign-in required' using errcode='42501';end if;
 if not exists(select 1 from auth.users a join ar_reels_private.admin_emails e on e.email=lower(a.email) and e.active where a.id=u and a.email_confirmed_at is not null) then raise exception 'Administrator access required' using errcode='42501';end if;
 return u;end $$;
revoke all on function ar_reels_private.require_admin() from public,anon,authenticated;
create function public.ar_reels_admin_access() returns jsonb language plpgsql security definer set search_path='' as $$
begin return jsonb_build_object('userId',ar_reels_private.require_admin(),'authorized',true);end $$;

create table ar_reels_private.admin_uploads(
 id uuid primary key,owner_id uuid not null references auth.users(id),reel_id text unique not null,sha256 text unique not null,
 video_size bigint not null,poster_sha text not null,poster_size bigint not null,duration numeric not null,
 video_object text unique not null,poster_object text unique not null,metadata jsonb not null,questions jsonb not null default '[]',
 status text not null default 'draft' check(status in ('draft','verified','published')),created_at timestamptz not null default now(),verified_at timestamptz,published_at timestamptz);
alter table ar_reels_private.admin_uploads enable row level security;
revoke all on ar_reels_private.admin_uploads from public,anon,authenticated;
create table ar_reels_private.admin_audit(id uuid primary key default gen_random_uuid(),actor_id uuid not null references auth.users(id),action text not null,reel_id text,created_at timestamptz not null default now());
alter table ar_reels_private.admin_audit enable row level security;
revoke all on ar_reels_private.admin_audit from public,anon,authenticated;
create function ar_reels_private.validate_reel_metadata(m jsonb) returns void language plpgsql set search_path='' as $$
begin
 if m is null or jsonb_typeof(m)<>'object' or pg_column_size(m)>65536 or length(trim(coalesce(m->>'title',''))) not between 1 and 160 or length(trim(coalesce(m->>'topic',''))) not between 1 and 120 or length(trim(coalesce(m->>'subject',''))) not between 1 and 60 or length(coalesce(m->>'concept',''))>2000 then raise exception 'Title, subject and topic are required; metadata is too long or invalid';end if;
 if jsonb_typeof(m->'grades') is distinct from 'array' or jsonb_array_length(m->'grades') not between 1 and 7 or exists(select 1 from jsonb_array_elements_text(m->'grades') g where g !~ '^(6|7|8|9|10|11|12)$') then raise exception 'Select valid classes';end if;
 if jsonb_typeof(m->'languages') is distinct from 'array' or jsonb_array_length(m->'languages') not between 1 and 2 or exists(select 1 from jsonb_array_elements_text(m->'languages') l where l not in ('English','Hindi')) then raise exception 'Select a supported language';end if;
end $$;
revoke all on function ar_reels_private.validate_reel_metadata(jsonb) from public,anon,authenticated;
create function ar_reels_private.validate_reel_questions(qs jsonb) returns void language plpgsql set search_path='' as $$
declare q jsonb;begin
 if jsonb_typeof(qs) is distinct from 'array' or jsonb_array_length(qs) not between 2 and 10 then raise exception 'Publishing requires 2 to 10 related questions per reel';end if;
 for q in select value from jsonb_array_elements(qs) loop
  if length(trim(coalesce(q->>'prompt',''))) not between 8 and 2000 or jsonb_typeof(q->'options') is distinct from 'array' or jsonb_array_length(q->'options')<>4 or exists(select 1 from jsonb_array_elements_text(q->'options') o where length(trim(o)) not between 1 and 500) or (select count(distinct lower(trim(o))) from jsonb_array_elements_text(q->'options') o)<>4 or coalesce(q->>'correct','') !~ '^[0-3]$' or length(trim(coalesce(q->>'explanation',''))) not between 5 and 4000 then raise exception 'Each question needs a prompt, four different options, one correct answer and an explanation';end if;
 end loop;
 if (select count(distinct lower(trim(value->>'prompt'))) from jsonb_array_elements(qs))<>jsonb_array_length(qs) then raise exception 'Questions must be distinct';end if;
end $$;
revoke all on function ar_reels_private.validate_reel_questions(jsonb) from public,anon,authenticated;

create function public.ar_reels_admin_create_upload(p_id uuid,p_sha text,p_size bigint,p_poster_sha text,p_poster_size bigint,p_duration numeric,p_metadata jsonb,p_questions jsonb) returns jsonb language plpgsql security definer set search_path='' as $$
declare u uuid:=ar_reels_private.require_admin();r ar_reels_private.admin_uploads%rowtype;rid text;prefix text;begin
 perform ar_reels_private.validate_reel_metadata(p_metadata);
 if p_sha is null or p_sha !~ '^[a-f0-9]{64}$' or p_poster_sha is null or p_poster_sha !~ '^[a-f0-9]{64}$' or p_size is null or p_size not between 1 and 52428800 or p_poster_size is null or p_poster_size not between 1 and 5242880 or p_duration is null or p_duration not between 1 and 1800 or p_id is null then raise exception 'Invalid MP4/poster metadata; limits are 50 MB video, 5 MB poster and 30 minutes';end if;
 if jsonb_typeof(p_questions) is distinct from 'array' or jsonb_array_length(p_questions)>10 or pg_column_size(p_questions)>200000 then raise exception 'Invalid draft questions';end if;
 perform pg_advisory_xact_lock(hashtext('ar-reels-upload-'||p_sha));
 select * into r from ar_reels_private.admin_uploads where id=p_id or sha256=p_sha;
 if found then
  if r.owner_id<>u or r.sha256<>p_sha then raise exception 'This file or upload belongs to another draft' using errcode='42501';end if;
  return to_jsonb(r);
 end if;
 if exists(select 1 from public.ar_reels_discovery_media where content->>'sha256'=p_sha) then raise exception 'This video already exists in the catalogue';end if;
 rid:='lesson-'||p_sha;prefix:='admin/'||p_id::text||'/';
 insert into ar_reels_private.admin_uploads(id,owner_id,reel_id,sha256,video_size,poster_sha,poster_size,duration,video_object,poster_object,metadata,questions)
 values(p_id,u,rid,p_sha,p_size,p_poster_sha,p_poster_size,p_duration,prefix||p_sha||'.mp4',prefix||p_poster_sha||'.jpg',p_metadata,p_questions) returning * into r;
 insert into ar_reels_private.admin_audit(actor_id,action,reel_id) values(u,'draft_created',rid);
 return to_jsonb(r);end $$;
create function public.ar_reels_admin_save(p_id uuid,p_metadata jsonb,p_questions jsonb) returns jsonb language plpgsql security definer set search_path='' as $$
declare u uuid:=ar_reels_private.require_admin();r ar_reels_private.admin_uploads%rowtype;begin
 perform ar_reels_private.validate_reel_metadata(p_metadata);
 if jsonb_typeof(p_questions) is distinct from 'array' or jsonb_array_length(p_questions)>10 or pg_column_size(p_questions)>200000 then raise exception 'Invalid draft questions';end if;
 update ar_reels_private.admin_uploads set metadata=p_metadata,questions=p_questions where id=p_id and owner_id=u and status<>'published' returning * into r;
 if not found then raise exception 'Editable draft not found' using errcode='42501';end if;return to_jsonb(r);end $$;
create function public.ar_reels_admin_upload_ticket(p_id uuid) returns jsonb language plpgsql security definer set search_path='' as $$
declare u uuid:=ar_reels_private.require_admin();r ar_reels_private.admin_uploads%rowtype;begin
 select * into r from ar_reels_private.admin_uploads where id=p_id and owner_id=u and status<>'published';
 if not found then raise exception 'Upload not found' using errcode='42501';end if;return to_jsonb(r);end $$;
create function public.ar_reels_admin_mark_verified(p_id uuid,p_video_sha text,p_poster_sha text,p_size bigint) returns void language plpgsql security definer set search_path='' as $$
begin
 update ar_reels_private.admin_uploads set status='verified',verified_at=now() where id=p_id and status='draft' and sha256=p_video_sha and poster_sha=p_poster_sha and video_size=p_size;
 if not found and not exists(select 1 from ar_reels_private.admin_uploads where id=p_id and status='verified' and sha256=p_video_sha and poster_sha=p_poster_sha and video_size=p_size) then raise exception 'Verified upload does not match this draft';end if;
end $$;
revoke all on function public.ar_reels_admin_mark_verified(uuid,text,text,bigint) from public,anon,authenticated;
do $$ begin if exists(select 1 from pg_roles where rolname='service_role') then grant execute on function public.ar_reels_admin_mark_verified(uuid,text,text,bigint) to service_role;end if;end $$;
create function public.ar_reels_admin_publish(p_id uuid) returns jsonb language plpgsql security definer set search_path='' as $$
declare u uuid:=ar_reels_private.require_admin();r ar_reels_private.admin_uploads%rowtype;c jsonb;q jsonb;i integer:=0;qid text;begin
 select * into r from ar_reels_private.admin_uploads where id=p_id and owner_id=u for update;
 if not found then raise exception 'Draft not found' using errcode='42501';end if;
 if r.status='published' then select content into c from public.ar_reels_discovery_media where id=r.reel_id;return c;end if;
 if r.status<>'verified' then raise exception 'Upload and verify the video and poster before publishing';end if;
 perform ar_reels_private.validate_reel_metadata(r.metadata);perform ar_reels_private.validate_reel_questions(r.questions);
 c:=jsonb_build_object('id',r.reel_id,'title',trim(r.metadata->>'title'),'subject',trim(r.metadata->>'subject'),'topic',trim(r.metadata->>'topic'),'concept',coalesce(r.metadata->>'concept',''),'chapterId','chapter-'||md5(lower(trim(r.metadata->>'subject')||':'||trim(r.metadata->>'topic'))),'grades',r.metadata->'grades','languages',r.metadata->'languages','duration',r.duration,'contentVersion',1,'sha256',r.sha256,'discovery',true,'source','https://pbaegeqjargtsfortsyh.supabase.co/storage/v1/object/public/arivihan-reels/'||r.video_object,'poster','https://pbaegeqjargtsfortsyh.supabase.co/storage/v1/object/public/arivihan-reels/'||r.poster_object,'isTest',coalesce((r.metadata->>'isTest')::boolean,false),'publishedAt',now(),'storageBucket','arivihan-reels','storageObject',r.video_object);
 insert into public.ar_reels_discovery_media(id,chapter_id,duration,content_type,content,active) values(r.reel_id,c->>'chapterId',r.duration,'educational',c,not coalesce((r.metadata->>'isTest')::boolean,false));
 for q in select value from jsonb_array_elements(r.questions) loop
  i:=i+1;qid:=r.reel_id||'-q'||lpad(i::text,2,'0');
  insert into public.ar_reels_discovery_questions(id,reel_id,prompt,options) values(qid,r.reel_id,trim(q->>'prompt'),q->'options');
  insert into ar_reels_private.discovery_answer_keys(question_id,correct_index,explanation) values(qid,(q->>'correct')::integer,trim(q->>'explanation'));
 end loop;
 update ar_reels_private.admin_uploads set status='published',published_at=now() where id=r.id;
 insert into ar_reels_private.admin_audit(actor_id,action,reel_id) values(u,'reel_published',r.reel_id);return c;
end $$;

-- Views require >=3 seconds of active playback. They are analytics, never reward eligibility.
create table ar_reels_private.play_sessions(id uuid primary key,user_id uuid not null references public.ar_reels_profiles(id) on delete cascade,reel_id text not null references public.ar_reels_discovery_media(id),last_sequence integer not null default 0,active_seconds numeric not null default 0,started_at timestamptz not null default now(),last_seen_at timestamptz not null default now());
alter table ar_reels_private.play_sessions enable row level security;
revoke all on ar_reels_private.play_sessions from public,anon,authenticated;
create index ar_reels_play_date on ar_reels_private.play_sessions(started_at,reel_id);
create function public.ar_reels_discovery_playback(p_session uuid,p_sequence integer,p_reel text,p_from numeric,p_to numeric,p_wall numeric,p_speed numeric,p_visible boolean,p_playing boolean) returns void language plpgsql security definer set search_path='' as $$
declare u uuid:=ar_reels_private.require_user();r ar_reels_private.play_sessions%rowtype;d numeric;begin
 perform 1 from public.ar_reels_profiles where id=u and persona='Pro' for update;if not found then raise exception 'Pro access required' using errcode='42501';end if;
 select duration into d from public.ar_reels_discovery_media where id=p_reel and active;
 if d is null or p_session is null or p_sequence is null or p_sequence<1 or p_visible is not true or p_playing is not true or p_from is null or p_to is null or p_wall is null or p_speed is null or p_wall<=0 or p_wall>2 or p_speed not between .25 and 2 or p_from<0 or p_to>d+.05 or p_to<=p_from or p_to-p_from>p_wall*p_speed+.15 then return;end if;
 insert into ar_reels_private.play_sessions(id,user_id,reel_id) values(p_session,u,p_reel) on conflict do nothing;
 select * into r from ar_reels_private.play_sessions where id=p_session for update;
 if r.user_id<>u or r.reel_id<>p_reel then raise exception 'Playback session ownership mismatch' using errcode='42501';end if;
 if p_sequence<=r.last_sequence then return;end if;
 perform public.ar_reels_discovery_progress(p_reel,p_from,p_to,p_wall,p_speed,p_visible,p_playing);
 update ar_reels_private.play_sessions set last_sequence=p_sequence,active_seconds=active_seconds+p_wall,last_seen_at=now() where id=p_session;
end $$;

create function public.ar_reels_admin_dashboard(p_days integer default 30,p_include_test boolean default false) returns jsonb language plpgsql security definer set search_path='' as $$
declare u uuid:=ar_reels_private.require_admin();since timestamptz;result jsonb;begin
 if p_days not between 1 and 90 or p_days is null then raise exception 'Choose a range from 1 to 90 days';end if;
 since:=(date_trunc('day',now() at time zone 'Asia/Kolkata')-make_interval(days=>p_days-1)) at time zone 'Asia/Kolkata';
 with plays as (select s.* from ar_reels_private.play_sessions s join public.ar_reels_profiles p on p.id=s.user_id where s.started_at>=since and (p_include_test or not p.is_test)),
 views as (select reel_id,count(*) filter(where active_seconds>=3) views,count(distinct user_id) filter(where active_seconds>=3) viewers,coalesce(sum(active_seconds),0) seconds from plays group by reel_id),
 quals as (select c.reel_id,count(*) n from public.ar_reels_discovery_coverage c join public.ar_reels_discovery_cycles cy on cy.id=c.cycle_id join public.ar_reels_profiles p on p.id=cy.user_id where c.qualified_at>=since and (p_include_test or not p.is_test) group by c.reel_id),
 days as (select generate_series((since at time zone 'Asia/Kolkata')::date,(now() at time zone 'Asia/Kolkata')::date,'1 day')::date as bucket_date),
 daily as (select (started_at at time zone 'Asia/Kolkata')::date as bucket_date,count(*) filter(where active_seconds>=3) views,coalesce(sum(active_seconds),0) seconds from plays group by 1)
 select jsonb_build_object('days',p_days,'includeTest',p_include_test,'views',(select count(*) from plays where active_seconds>=3),'uniqueViewers',(select count(distinct user_id) from plays where active_seconds>=3),'watchSeconds',(select coalesce(sum(active_seconds),0) from plays),'qualifiedViews',(select coalesce(sum(n),0) from quals),'quizCompletions',(select count(*) from public.ar_reels_events where name='reels_quiz_completed' and created_at>=since and (p_include_test or not is_test)),'coinsEarned',(select 5*count(*) from public.ar_reels_events where name='reels_reward_earned' and created_at>=since and (p_include_test or not is_test)),
 'daily',(select coalesce(jsonb_agg(jsonb_build_object('date',d.bucket_date,'views',coalesce(a.views,0),'watchSeconds',coalesce(a.seconds,0)) order by d.bucket_date),'[]') from days d left join daily a on a.bucket_date=d.bucket_date),
 'reels',(select coalesce(jsonb_agg(m.content || jsonb_build_object('active',m.active,'views',coalesce(v.views,0),'uniqueViewers',coalesce(v.viewers,0),'watchSeconds',coalesce(v.seconds,0),'qualifiedViews',coalesce(q.n,0),'questionCount',(select count(*) from public.ar_reels_discovery_questions b where b.reel_id=m.id)) order by coalesce(v.views,0) desc,m.content->>'title'),'[]') from public.ar_reels_discovery_media m left join views v on v.reel_id=m.id left join quals q on q.reel_id=m.id),
 'drafts',(select coalesce(jsonb_agg(to_jsonb(a) order by a.created_at desc),'[]') from ar_reels_private.admin_uploads a where a.owner_id=u and a.status<>'published')) into result;
 return result;end $$;

do $$ declare sig text;begin
 foreach sig in array array['public.ar_reels_admin_access()','public.ar_reels_admin_create_upload(uuid,text,bigint,text,bigint,numeric,jsonb,jsonb)','public.ar_reels_admin_save(uuid,jsonb,jsonb)','public.ar_reels_admin_upload_ticket(uuid)','public.ar_reels_admin_publish(uuid)','public.ar_reels_admin_dashboard(integer,boolean)','public.ar_reels_discovery_playback(uuid,integer,text,numeric,numeric,numeric,numeric,boolean,boolean)'] loop
  execute 'revoke all on function '||sig||' from public,anon,authenticated';execute 'grant execute on function '||sig||' to authenticated';
 end loop;
end $$;
-- Admin-only answer-key review; student catalogue APIs remain unchanged.
create function public.ar_reels_admin_questions(p_reel text) returns jsonb language plpgsql security definer set search_path='' as $$
begin
 perform ar_reels_private.require_admin();
 return coalesce((select jsonb_agg(jsonb_build_object('prompt',q.prompt,'options',q.options,'correct',k.correct_index,'explanation',k.explanation) order by q.id) from public.ar_reels_discovery_questions q join ar_reels_private.discovery_answer_keys k on k.question_id=q.id where q.reel_id=p_reel),'[]');
end $$;
revoke all on function public.ar_reels_admin_questions(text) from public,anon,authenticated;
grant execute on function public.ar_reels_admin_questions(text) to authenticated;
