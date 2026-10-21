-- Additive discovery model. Historical chapter challenges and records remain valid.
create table public.ar_reels_discovery_media(id text primary key,chapter_id text not null,duration numeric not null check(duration>0),content_type text not null check(content_type='educational'),content jsonb not null,active boolean not null default true);
create table public.ar_reels_discovery_questions(id text primary key,reel_id text not null references public.ar_reels_discovery_media(id),prompt text not null,options jsonb not null check(jsonb_array_length(options)=4));
create table ar_reels_private.discovery_answer_keys(question_id text primary key references public.ar_reels_discovery_questions(id),correct_index integer not null check(correct_index between 0 and 3),explanation text not null);
alter table ar_reels_private.discovery_answer_keys enable row level security;
revoke all on ar_reels_private.discovery_answer_keys from public,anon,authenticated;
create table public.ar_reels_discovery_cycles(
 id uuid primary key default gen_random_uuid(), user_id uuid not null references public.ar_reels_profiles(id) on delete cascade,
 threshold integer not null check(threshold in (3,4)),closed boolean not null default false,created_at timestamptz not null default now());
create unique index ar_reels_active_cycle on public.ar_reels_discovery_cycles(user_id) where not closed;
create table public.ar_reels_discovery_coverage(
 cycle_id uuid not null references public.ar_reels_discovery_cycles(id) on delete cascade,
 reel_id text not null references public.ar_reels_discovery_media(id),content_version integer not null,
 intervals jsonb not null default '[]',covered numeric not null default 0,qualified_at timestamptz,
 primary key(cycle_id,reel_id));
create table public.ar_reels_discovery_batches(
 id uuid primary key default gen_random_uuid(),user_id uuid not null references public.ar_reels_profiles(id) on delete cascade,
 cycle_id uuid unique not null references public.ar_reels_discovery_cycles(id),reward_key text not null,
 status text not null default 'offered' check(status in ('offered','deferred','started','completed')),
 created_at timestamptz not null default now());
create table public.ar_reels_discovery_items(
 batch_id uuid not null references public.ar_reels_discovery_batches(id) on delete cascade,
 position integer not null check(position between 0 and 4),question_id text not null references public.ar_reels_discovery_questions(id),
 reel_id text not null references public.ar_reels_discovery_media(id),content_version integer not null,
 primary key(batch_id,position),unique(batch_id,question_id));
create table public.ar_reels_discovery_attempts(id uuid primary key default gen_random_uuid(),user_id uuid not null references public.ar_reels_profiles(id) on delete cascade,discovery_batch_id uuid not null references public.ar_reels_discovery_batches(id),finished boolean not null default false,score integer not null default 0 check(score between 0 and 5),rewarded boolean not null default false,already_rewarded boolean not null default false,created_at timestamptz not null default now());
create unique index ar_reels_discovery_active_attempt on public.ar_reels_discovery_attempts(user_id,discovery_batch_id) where not finished;
create table public.ar_reels_discovery_answers(attempt_id uuid not null references public.ar_reels_discovery_attempts(id) on delete cascade,position integer not null check(position between 0 and 4),question_id text not null references public.ar_reels_discovery_questions(id),selected integer not null check(selected between 0 and 3),correct integer not null check(correct between 0 and 3),explanation text not null,primary key(attempt_id,position));
do $$ declare t text; begin
 foreach t in array array['discovery_cycles','discovery_coverage','discovery_batches','discovery_items','discovery_media','discovery_questions','discovery_attempts','discovery_answers'] loop
 execute format('alter table public.ar_reels_%I enable row level security',t);
 execute format('revoke all on public.ar_reels_%I from public,anon,authenticated',t);
 end loop;
end $$;
grant select on public.ar_reels_discovery_media,public.ar_reels_discovery_questions to authenticated;
create policy discovery_catalogue_read on public.ar_reels_discovery_media for select to authenticated using(active);
create policy discovery_question_read on public.ar_reels_discovery_questions for select to authenticated using(exists(select 1 from public.ar_reels_discovery_media m where m.id=reel_id and m.active));
-- Only RPC DTOs expose cycle state. The internal random threshold is not returned.
create function ar_reels_private.discovery_cycle(u uuid) returns uuid language plpgsql security definer set search_path='' as $$
declare cid uuid;begin
 perform 1 from public.ar_reels_profiles where id=u for update;
 select id into cid from public.ar_reels_discovery_cycles where user_id=u and not closed;
 if cid is null then insert into public.ar_reels_discovery_cycles(user_id,threshold) values(u,3+floor(random()*2)::integer) returning id into cid;end if;
 return cid;end $$;

create function public.ar_reels_discovery_progress(p_reel text,p_from numeric,p_to numeric,p_wall numeric,p_speed numeric,p_visible boolean,p_playing boolean) returns void language plpgsql security definer set search_path='' as $$
declare u uuid:=ar_reels_private.require_user();cid uuid;d numeric;ver integer;v jsonb;merged jsonb:='[]';seg jsonb;lo numeric;hi numeric;coverage numeric;begin
 -- Consistent profile-first lock order with completion and checkout.
 perform 1 from public.ar_reels_profiles where id=u for update;
 if not exists(select 1 from public.ar_reels_profiles where id=u and persona='Pro') then raise exception 'Pro access required' using errcode='42501';end if;
 select duration,coalesce((content->>'contentVersion')::integer,1) into d,ver from public.ar_reels_discovery_media where id=p_reel and active and content->>'discovery'='true';
 if d is null or p_visible is not true or p_playing is not true or p_wall is null or p_speed is null or p_from is null or p_to is null or p_wall<=0 or p_wall>2 or p_speed not between .25 and 2 or p_from<0 or p_to>d+.05 or p_to<=p_from or p_to-p_from>p_wall*p_speed+.15 then return;end if;
 cid:=ar_reels_private.discovery_cycle(u);
 insert into public.ar_reels_discovery_coverage(cycle_id,reel_id,content_version) values(cid,p_reel,ver) on conflict do nothing;
 select intervals into v from public.ar_reels_discovery_coverage where cycle_id=cid and reel_id=p_reel and content_version=ver for update;
 if v is null then return;end if;
 for seg in select value from jsonb_array_elements(v||jsonb_build_array(jsonb_build_array(p_from,least(p_to,d)))) order by (value->>0)::numeric loop
 if lo is null then lo:=(seg->>0)::numeric;hi:=(seg->>1)::numeric;
 elsif (seg->>0)::numeric<=hi+.025 then hi:=greatest(hi,(seg->>1)::numeric);
 else merged:=merged||jsonb_build_array(jsonb_build_array(lo,hi));lo:=(seg->>0)::numeric;hi:=(seg->>1)::numeric;end if;end loop;
 merged:=merged||jsonb_build_array(jsonb_build_array(lo,hi));select coalesce(sum((value->>1)::numeric-(value->>0)::numeric),0) into coverage from jsonb_array_elements(merged);
 update public.ar_reels_discovery_coverage set intervals=merged,covered=coverage,qualified_at=case when coverage>=d*.8 then coalesce(qualified_at,now()) else null end where cycle_id=cid and reel_id=p_reel;
end $$;

create function public.ar_reels_discovery_catalogue() returns jsonb language sql stable security invoker set search_path='' as $$
 select coalesce(jsonb_agg(m.content order by m.id),'[]') from public.ar_reels_discovery_media m
 where m.active and m.content->>'discovery'='true'
$$;

create function public.ar_reels_discovery_state() returns jsonb language plpgsql security definer set search_path='' as $$
declare u uuid:=ar_reels_private.require_user();cid uuid;begin
 cid:=ar_reels_private.discovery_cycle(u);
 return jsonb_build_object('cycleId',cid,'pending',coalesce((select jsonb_agg(jsonb_build_object('id',b.id,'status',b.status,'attemptId',(select a.id from public.ar_reels_discovery_attempts a where a.discovery_batch_id=b.id and a.user_id=u order by a.created_at desc limit 1)) order by b.created_at) from public.ar_reels_discovery_batches b where b.user_id=u and b.status<>'completed'),'[]'));
end $$;

-- Called at a natural transition only. It freezes the exact mapped content once.
create function public.ar_reels_discovery_offer() returns jsonb language plpgsql security definer set search_path='' as $$
declare u uuid:=ar_reels_private.require_user();cid uuid;bid uuid;t integer;rids text[];qids text[];key text;n integer;begin
 cid:=ar_reels_private.discovery_cycle(u);
 select id into bid from public.ar_reels_discovery_batches where user_id=u and status='offered' order by created_at limit 1;
 if bid is not null then return jsonb_build_object('id',bid,'ready',true);end if;
 select threshold into t from public.ar_reels_discovery_cycles where id=cid;
 select array_agg(reel_id order by qualified_at,reel_id) into rids from (select p.reel_id,p.qualified_at from public.ar_reels_discovery_coverage p join public.ar_reels_discovery_media m on m.id=p.reel_id where p.cycle_id=cid and p.qualified_at is not null and m.active and p.content_version=coalesce((m.content->>'contentVersion')::integer,1) order by p.qualified_at,p.reel_id limit 4) x;
 n:=coalesce(cardinality(rids),0);if n<t then return jsonb_build_object('ready',false);end if;
 -- Every watched reel must have a valid private key and at least one question.
 if exists(select 1 from unnest(rids) rid where not exists(select 1 from public.ar_reels_discovery_questions q join ar_reels_private.discovery_answer_keys k on k.question_id=q.id where q.reel_id=rid)) then return jsonb_build_object('ready',false,'diagnostic','missing_reel_question_mapping');end if;
 select array_agg(id order by rn,reel_id,id) into qids from (select * from (select q.id,q.reel_id,row_number() over(partition by q.reel_id order by q.id) rn from public.ar_reels_discovery_questions q join ar_reels_private.discovery_answer_keys k on k.question_id=q.id where q.reel_id=any(rids)) ranked order by rn,reel_id,id limit 5) chosen;
 if cardinality(qids)<>5 then return jsonb_build_object('ready',false,'diagnostic','insufficient_mapped_questions');end if;
 select 'discovery-v1-'||md5(string_agg(reel_id||':'||content_version,',' order by reel_id)||'|'||(select string_agg(v,',' order by v) from unnest(qids) v)) into key from public.ar_reels_discovery_coverage where cycle_id=cid and reel_id=any(rids);
 insert into public.ar_reels_discovery_batches(user_id,cycle_id,reward_key) values(u,cid,key) returning id into bid;
 insert into public.ar_reels_discovery_items(batch_id,position,question_id,reel_id,content_version)
 select bid,(ord-1)::integer,q.id,q.reel_id,p.content_version from unnest(qids) with ordinality chosen(id,ord) join public.ar_reels_discovery_questions q on q.id=chosen.id join public.ar_reels_discovery_coverage p on p.cycle_id=cid and p.reel_id=q.reel_id;
 return jsonb_build_object('id',bid,'ready',true);
end $$;

create function public.ar_reels_discovery_defer(p_batch uuid) returns void language plpgsql security definer set search_path='' as $$
declare u uuid:=ar_reels_private.require_user();cid uuid;begin
 perform 1 from public.ar_reels_profiles where id=u for update;
 update public.ar_reels_discovery_batches set status='deferred' where id=p_batch and user_id=u and status='offered' returning cycle_id into cid;
 if cid is not null then update public.ar_reels_discovery_cycles set closed=true where id=cid and user_id=u;perform ar_reels_private.discovery_cycle(u);end if;
end $$;

create function ar_reels_private.discovery_dto(aid uuid) returns jsonb language sql stable security definer set search_path='' as $$
 select jsonb_build_object('id',a.id,'batchId',a.discovery_batch_id,'challengeId',b.reward_key,'finished',a.finished,'score',a.score,'rewarded',a.rewarded,'alreadyRewarded',a.already_rewarded,
 'questions',(select jsonb_agg(jsonb_build_object('id',q.id,'reelId',i.reel_id,'contentVersion',i.content_version,'prompt',q.prompt,'options',q.options) order by i.position) from public.ar_reels_discovery_items i join public.ar_reels_discovery_questions q on q.id=i.question_id where i.batch_id=b.id),
 'answers',(select jsonb_agg(case when ans.position is null then 'null'::jsonb else jsonb_build_object('selected',ans.selected,'correct',ans.correct,'explanation',ans.explanation) end order by i.position) from public.ar_reels_discovery_items i left join public.ar_reels_discovery_answers ans on ans.attempt_id=a.id and ans.position=i.position where i.batch_id=b.id))
 from public.ar_reels_discovery_attempts a join public.ar_reels_discovery_batches b on b.id=a.discovery_batch_id where a.id=aid and a.user_id=auth.uid()
$$;
create function public.ar_reels_discovery_start(p_batch uuid) returns jsonb language plpgsql security definer set search_path='' as $$
declare u uuid:=ar_reels_private.require_user();b public.ar_reels_discovery_batches%rowtype;aid uuid;begin
 perform 1 from public.ar_reels_profiles where id=u and persona='Pro' for update;if not found then raise exception 'Pro access required' using errcode='42501';end if;
 select * into b from public.ar_reels_discovery_batches where id=p_batch and user_id=u for update;if not found then raise exception 'Quiz not found' using errcode='42501';end if;
 if (select count(*) from public.ar_reels_discovery_items where batch_id=b.id)<>5 then raise exception 'Invalid mapped quiz';end if;
 if exists(select 1 from public.ar_reels_discovery_items i join public.ar_reels_discovery_media m on m.id=i.reel_id left join public.ar_reels_discovery_coverage p on p.cycle_id=b.cycle_id and p.reel_id=i.reel_id and p.content_version=i.content_version where i.batch_id=b.id and (not m.active or p.covered is null or p.covered<m.duration*.8 or i.content_version<>coalesce((m.content->>'contentVersion')::integer,1))) then raise exception 'Quiz eligibility changed' using errcode='42501';end if;
 select id into aid from public.ar_reels_discovery_attempts where user_id=u and discovery_batch_id=b.id and not finished;
 if aid is null then insert into public.ar_reels_discovery_attempts(user_id,discovery_batch_id,already_rewarded) values(u,b.id,exists(select 1 from public.ar_reels_ledger where user_id=u and reward_key=b.reward_key)) returning id into aid;end if;
 update public.ar_reels_discovery_batches set status='started' where id=b.id;
 -- Advance once at offer acceptance, so completed/deferred cycles cannot reopen.
 update public.ar_reels_discovery_cycles set closed=true where id=b.cycle_id and user_id=u;perform ar_reels_private.discovery_cycle(u);
 return ar_reels_private.discovery_dto(aid);
end $$;

create function public.ar_reels_discovery_submit(p_attempt uuid,p_index integer,p_selected integer) returns jsonb language plpgsql security definer set search_path='' as $$
declare u uuid:=ar_reels_private.require_user();a public.ar_reels_discovery_attempts%rowtype;k ar_reels_private.discovery_answer_keys%rowtype;qid text;begin
 select * into a from public.ar_reels_discovery_attempts where id=p_attempt and user_id=u for update;if not found then raise exception 'Attempt not found' using errcode='42501';end if;
 if exists(select 1 from public.ar_reels_discovery_answers where attempt_id=p_attempt and position=p_index) then return ar_reels_private.discovery_dto(p_attempt);end if;
 if a.finished or p_index is null or p_selected is null or p_index not between 0 and 4 or p_selected not between 0 and 3 or p_index<>(select count(*) from public.ar_reels_discovery_answers where attempt_id=p_attempt) then raise exception 'Invalid answer transition';end if;
 select i.question_id into qid from public.ar_reels_discovery_items i join public.ar_reels_discovery_questions q on q.id=i.question_id and q.reel_id=i.reel_id where i.batch_id=a.discovery_batch_id and i.position=p_index;
 if qid is null then raise exception 'Question is not mapped to this quiz';end if;
 select * into strict k from ar_reels_private.discovery_answer_keys where question_id=qid;
 insert into public.ar_reels_discovery_answers(attempt_id,position,question_id,selected,correct,explanation) values(p_attempt,p_index,qid,p_selected,k.correct_index,k.explanation);
 return ar_reels_private.discovery_dto(p_attempt);
end $$;
create function public.ar_reels_discovery_finish(p_attempt uuid) returns jsonb language plpgsql security definer set search_path='' as $$
declare u uuid:=ar_reels_private.require_user();a public.ar_reels_discovery_attempts%rowtype;b public.ar_reels_discovery_batches%rowtype;s integer;awarded boolean:=false;was boolean;begin
 perform 1 from public.ar_reels_profiles where id=u for update;
 select * into a from public.ar_reels_discovery_attempts where id=p_attempt and user_id=u for update;if not found then raise exception 'Attempt not found' using errcode='42501';end if;
 if a.finished then return ar_reels_private.discovery_dto(p_attempt);end if;
 select * into strict b from public.ar_reels_discovery_batches where id=a.discovery_batch_id and user_id=u;
 if not exists(select 1 from public.ar_reels_profiles where id=u and persona='Pro') or exists(select 1 from public.ar_reels_discovery_items i join public.ar_reels_discovery_media m on m.id=i.reel_id left join public.ar_reels_discovery_coverage p on p.cycle_id=b.cycle_id and p.reel_id=i.reel_id and p.content_version=i.content_version where i.batch_id=b.id and (not m.active or p.covered is null or p.covered<m.duration*.8 or i.content_version<>coalesce((m.content->>'contentVersion')::integer,1))) then raise exception 'Quiz eligibility changed' using errcode='42501';end if;
 if (select count(*) from public.ar_reels_discovery_answers where attempt_id=p_attempt)<>5 then raise exception 'Complete all five answers first';end if;
 select count(*) into s from public.ar_reels_discovery_answers ans join public.ar_reels_discovery_items i on i.batch_id=b.id and i.position=ans.position and i.question_id=ans.question_id join ar_reels_private.discovery_answer_keys k on k.question_id=ans.question_id where ans.attempt_id=p_attempt and ans.selected=k.correct_index;
 select exists(select 1 from public.ar_reels_ledger where user_id=u and reward_key=b.reward_key) into was;
 if s=5 then insert into public.ar_reels_ledger(user_id,amount,reason,ref,reward_key) values(u,5,'Perfect discovery quiz',b.reward_key,b.reward_key) on conflict(user_id,reward_key) do nothing;awarded:=found;end if;
 update public.ar_reels_discovery_attempts set finished=true,score=s,rewarded=awarded,already_rewarded=was where id=p_attempt;
 update public.ar_reels_discovery_batches set status='completed' where id=b.id;
 return ar_reels_private.discovery_dto(p_attempt);
end $$;

create function public.ar_reels_discovery_review(p_attempt uuid) returns jsonb language plpgsql security definer set search_path='' as $$
declare v jsonb;begin perform ar_reels_private.require_user();v:=ar_reels_private.discovery_dto(p_attempt);if v is null then raise exception 'Attempt not found' using errcode='42501';end if;return v;end $$;

-- New functions get explicit least-privilege grants; no public default execution.
revoke all on function ar_reels_private.discovery_cycle(uuid),ar_reels_private.discovery_dto(uuid) from public,anon,authenticated;
do $$ declare f record;begin
 for f in select p.oid::regprocedure sig from pg_proc p join pg_namespace n on n.oid=p.pronamespace where n.nspname='public' and p.proname like 'ar_reels_discovery_%' loop
 execute format('revoke all on function %s from public,anon,authenticated',f.sig);
 execute format('grant execute on function %s to authenticated',f.sig);
 end loop;
end $$;
