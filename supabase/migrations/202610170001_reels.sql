-- Additive integration; historical tables, views and policies remain untouched.
create schema ar_reels_private;
revoke all on schema ar_reels_private from public,anon,authenticated;

create table public.ar_reels_profiles(
 id uuid primary key references auth.users(id) on delete cascade,
 name text not null check(length(name) between 1 and 60), batch text not null,
 language text not null default 'English', persona text not null default 'Pro' check(persona in ('Pro','Plus','None')),
 is_test boolean not null default false, created_at timestamptz not null default now()
);
create table public.ar_reels_chapters(id text primary key,position integer not null default 0, content jsonb not null, active boolean not null default true);
create table public.ar_reels_media(
 id text primary key,chapter_id text not null references public.ar_reels_chapters(id),
 duration numeric not null check(duration>0),content_type text not null check(content_type in ('educational','featured','promo')),
 content jsonb not null,active boolean not null default true
);
create table public.ar_reels_questions(id text primary key,reel_id text unique not null references public.ar_reels_media(id),prompt text not null,options jsonb not null check(jsonb_array_length(options)=4));
create table ar_reels_private.answer_keys(question_id text primary key references public.ar_reels_questions(id),correct_index integer not null check(correct_index between 0 and 3),explanation text not null);
create table public.ar_reels_challenges(id text primary key,chapter_id text not null references public.ar_reels_chapters(id),content_version integer not null check(content_version>0),active boolean not null default true,unique(chapter_id,content_version));
create table public.ar_reels_challenge_items(
 challenge_id text not null references public.ar_reels_challenges(id),position integer not null check(position between 0 and 4),
 reel_id text not null references public.ar_reels_media(id),question_id text not null references public.ar_reels_questions(id),
 primary key(challenge_id,position),unique(challenge_id,reel_id),unique(challenge_id,question_id)
);
create table public.ar_reels_progress(user_id uuid not null references public.ar_reels_profiles(id) on delete cascade,reel_id text not null references public.ar_reels_media(id),intervals jsonb not null default '[]',position numeric not null default 0,covered numeric not null default 0,updated_at timestamptz not null default now(),primary key(user_id,reel_id));
create table public.ar_reels_preferences(user_id uuid primary key references public.ar_reels_profiles(id) on delete cascade,state jsonb not null default '{"likes":[],"saved":[],"downloads":[],"deferred":[],"offered":[],"mute":true,"lastReel":null}');
create table public.ar_reels_attempts(id uuid primary key default gen_random_uuid(),user_id uuid not null references public.ar_reels_profiles(id) on delete cascade,challenge_id text not null references public.ar_reels_challenges(id),finished boolean not null default false,score integer not null default 0 check(score between 0 and 5),rewarded boolean not null default false,already_rewarded boolean not null default false,created_at timestamptz not null default now());
create unique index ar_reels_one_active_attempt on public.ar_reels_attempts(user_id,challenge_id) where not finished;
create table public.ar_reels_answers(attempt_id uuid not null references public.ar_reels_attempts(id) on delete cascade,position integer not null check(position between 0 and 4),question_id text not null references public.ar_reels_questions(id),selected integer not null check(selected between 0 and 3),correct integer not null check(correct between 0 and 3),explanation text not null,primary key(attempt_id,position));
create table public.ar_reels_products(id text primary key,position integer not null default 0,price_paise integer not null check(price_paise>0),eligible boolean not null default true,active boolean not null default true,content jsonb not null);
create table public.ar_reels_checkouts(user_id uuid not null references public.ar_reels_profiles(id) on delete cascade,product_id text not null references public.ar_reels_products(id),request_id uuid not null default gen_random_uuid(),primary key(user_id,product_id),unique(user_id,request_id));
create table public.ar_reels_orders(id uuid not null,user_id uuid not null references public.ar_reels_profiles(id) on delete cascade,product_id text not null references public.ar_reels_products(id),quote jsonb not null,simulated boolean not null default true check(simulated),created_at timestamptz not null default now(),primary key(user_id,id));
create table public.ar_reels_ledger(id uuid primary key default gen_random_uuid(),user_id uuid not null references public.ar_reels_profiles(id) on delete cascade,amount integer not null check(amount<>0),reason text not null,ref text not null,reward_key text,created_at timestamptz not null default now(),unique(user_id,reward_key),unique(user_id,ref));
create table public.ar_reels_events(id uuid not null,user_id uuid not null references public.ar_reels_profiles(id) on delete cascade,name text not null,session_id uuid,chapter_id text,reel_id text,challenge_id text,attempt_id uuid,content_type text not null,source text not null,is_test boolean not null default false,presenter boolean not null default false,created_at timestamptz not null default now(),primary key(user_id,id));

-- Own rows only. All writes pass through narrow authenticated RPCs.
do $rls$
declare t text;
begin
 foreach t in array array['profiles','progress','preferences','attempts','checkouts','orders','ledger','events'] loop
  execute format('alter table public.ar_reels_%I enable row level security',t);
  execute format('revoke all on public.ar_reels_%I from public,anon,authenticated',t);
  execute format('grant select on public.ar_reels_%I to authenticated',t);
  execute format('create policy own_read on public.ar_reels_%I for select to authenticated using (%I = (select auth.uid()))',t,case when t='profiles' then 'id' else 'user_id' end);
 end loop;
 foreach t in array array['chapters','media','questions','challenges','challenge_items','products'] loop
  execute format('alter table public.ar_reels_%I enable row level security',t);
  execute format('revoke all on public.ar_reels_%I from public,anon,authenticated',t);
  execute format('grant select on public.ar_reels_%I to anon,authenticated',t);
  execute format('create policy catalogue_read on public.ar_reels_%I for select to anon,authenticated using (true)',t);
 end loop;
end $rls$;
drop policy catalogue_read on public.ar_reels_chapters;
create policy catalogue_read on public.ar_reels_chapters for select to anon,authenticated using(active);
drop policy catalogue_read on public.ar_reels_media;
create policy catalogue_read on public.ar_reels_media for select to anon,authenticated using(active and exists(select 1 from public.ar_reels_chapters c where c.id=chapter_id and c.active));
drop policy catalogue_read on public.ar_reels_products;
create policy catalogue_read on public.ar_reels_products for select to anon,authenticated using(active);
drop policy catalogue_read on public.ar_reels_challenges;
create policy catalogue_read on public.ar_reels_challenges for select to anon,authenticated using(active);
alter table public.ar_reels_answers enable row level security;
revoke all on public.ar_reels_answers from public,anon,authenticated;
grant select on public.ar_reels_answers to authenticated;
create policy own_answer_read on public.ar_reels_answers for select to authenticated using(exists(select 1 from public.ar_reels_attempts a where a.id=attempt_id and a.user_id=(select auth.uid())));
alter table ar_reels_private.answer_keys enable row level security;
revoke all on ar_reels_private.answer_keys from public,anon,authenticated;

create function ar_reels_private.require_user() returns uuid language plpgsql security definer set search_path='' as $$
declare u uuid:=auth.uid(); begin if u is null then raise exception 'Authentication required' using errcode='28000';end if;return u;end $$;
create function ar_reels_private.balance(u uuid) returns bigint language sql stable security definer set search_path='' as $$ select coalesce(sum(amount),0)::bigint from public.ar_reels_ledger where user_id=u $$;
create function ar_reels_private.event(u uuid,n text,c text default null,r text default null,a uuid default null,vid text default null) returns void language sql security definer set search_path='' as $$
 insert into public.ar_reels_events(id,user_id,name,session_id,chapter_id,challenge_id,attempt_id,reel_id,content_type,source,is_test)
 select gen_random_uuid(),u,n,coalesce((nullif(current_setting('request.headers',true),'')::jsonb->>'x-reels-session-id')::uuid,gen_random_uuid()),c,r,a,vid,case when n like 'reels_redemption_%' then 'store' else 'educational' end,'server',p.is_test from public.ar_reels_profiles p where p.id=u
$$;
create function ar_reels_private.attempt_dto(aid uuid) returns jsonb language sql stable security definer set search_path='' as $$
 select jsonb_build_object('id',a.id,'chapterId',c.chapter_id,'challengeId',a.challenge_id,'finished',a.finished,'score',a.score,'rewarded',a.rewarded,'alreadyRewarded',a.already_rewarded,
 'questions',(select jsonb_agg(jsonb_build_object('id',q.id,'reelId',q.reel_id,'prompt',q.prompt,'options',q.options) order by i.position) from public.ar_reels_challenge_items i join public.ar_reels_questions q on q.id=i.question_id where i.challenge_id=a.challenge_id),
 'answers',(select jsonb_agg(case when ans.position is null then 'null'::jsonb else jsonb_build_object('selected',ans.selected,'correct',ans.correct,'explanation',ans.explanation) end order by i.position) from public.ar_reels_challenge_items i left join public.ar_reels_answers ans on ans.attempt_id=a.id and ans.position=i.position where i.challenge_id=a.challenge_id))
 from public.ar_reels_attempts a join public.ar_reels_challenges c on c.id=a.challenge_id where a.id=aid and a.user_id=auth.uid()
$$;
create function public.ar_reels_profile(p_name text,p_batch text,p_language text,p_test boolean default false) returns jsonb language plpgsql security definer set search_path='' as $$
declare u uuid:=ar_reels_private.require_user();v jsonb;begin
 if length(trim(p_name)) not between 1 and 60 or length(p_batch)>100 or length(p_language)>30 then raise exception 'Invalid profile';end if;
 insert into public.ar_reels_profiles(id,name,batch,language,is_test) values(u,trim(p_name),p_batch,p_language,p_test) on conflict(id) do update set name=excluded.name,batch=excluded.batch,language=excluded.language;
 insert into public.ar_reels_preferences(user_id) values(u) on conflict do nothing;
 select jsonb_build_object('id',id,'name',name,'batch',batch,'language',language,'persona',persona) into v from public.ar_reels_profiles where id=u;return v;
end $$;
create function public.ar_reels_catalogue() returns jsonb language sql stable security definer set search_path='' as $$
 select coalesce(jsonb_agg(c.content||jsonb_build_object('reels',coalesce((select jsonb_agg(m.content order by m.id) from public.ar_reels_media m where m.chapter_id=c.id and m.active),'[]'::jsonb)) order by c.position,c.id),'[]'::jsonb) from public.ar_reels_chapters c where c.active
$$;
create function public.ar_reels_product_catalogue() returns jsonb language sql stable security definer set search_path='' as $$ select coalesce(jsonb_agg(content||jsonb_build_object('price',price_paise) order by position,id),'[]'::jsonb) from public.ar_reels_products where active $$;

create function public.ar_reels_progress(p_reel text,p_from numeric,p_to numeric,p_wall numeric,p_speed numeric,p_visible boolean,p_playing boolean) returns void language plpgsql security definer set search_path='' as $$
declare u uuid:=ar_reels_private.require_user();d numeric;old_coverage numeric;new_coverage numeric;v jsonb;merged jsonb:='[]';seg jsonb;lo numeric;hi numeric;cid text;before_group integer;after_group integer;begin
 select m.duration,m.chapter_id into d,cid from public.ar_reels_media m join public.ar_reels_chapters c on c.id=m.chapter_id join public.ar_reels_profiles p on p.id=u where m.id=p_reel and m.active and m.content_type='educational' and c.active and (c.content->>'available')::boolean and p.persona='Pro';
 if d is null then raise exception 'Lesson is unavailable or locked' using errcode='42501';end if;
 if p_visible is not true or p_playing is not true or p_wall is null or p_speed is null or p_from is null or p_to is null or p_wall<=0 or p_wall>2 or p_speed not between 0.25 and 2 or p_from<0 or p_to>d+0.05 or p_to<=p_from or p_to-p_from>p_wall*p_speed+0.15 then return;end if;
 insert into public.ar_reels_progress(user_id,reel_id) values(u,p_reel) on conflict do nothing;
 select intervals,covered into v,old_coverage from public.ar_reels_progress where user_id=u and reel_id=p_reel for update;
 for seg in select value from jsonb_array_elements(v||jsonb_build_array(jsonb_build_array(p_from,least(p_to,d)))) order by (value->>0)::numeric loop
  if lo is null then lo:=(seg->>0)::numeric;hi:=(seg->>1)::numeric;
  elsif (seg->>0)::numeric<=hi+0.025 then hi:=greatest(hi,(seg->>1)::numeric);
  else merged:=merged||jsonb_build_array(jsonb_build_array(lo,hi));lo:=(seg->>0)::numeric;hi:=(seg->>1)::numeric;end if;
 end loop;
 merged:=merged||jsonb_build_array(jsonb_build_array(lo,hi));select coalesce(sum((value->>1)::numeric-(value->>0)::numeric),0) into new_coverage from jsonb_array_elements(merged);
 update public.ar_reels_progress set intervals=merged,covered=new_coverage,position=least(p_to,d),updated_at=now() where user_id=u and reel_id=p_reel;
 if old_coverage<d*.8 and new_coverage>=d*.8 then perform ar_reels_private.event(u,'reels_video_completed',cid,null,null,p_reel);elsif floor(old_coverage/5)<>floor(new_coverage/5) then perform ar_reels_private.event(u,'reels_watch_progress',cid,null,null,p_reel);end if;
end $$;
create function public.ar_reels_checkpoint(p_reel text,p_position numeric) returns void language plpgsql security definer set search_path='' as $$
declare u uuid:=ar_reels_private.require_user();d numeric;begin
 select duration into d from public.ar_reels_media where id=p_reel and active;if d is null or p_position is null then raise exception 'Invalid lesson checkpoint';end if;
 insert into public.ar_reels_progress(user_id,reel_id,position) values(u,p_reel,greatest(0,least(d,p_position))) on conflict(user_id,reel_id) do update set position=excluded.position;
end $$;
create function public.ar_reels_start(p_chapter text) returns jsonb language plpgsql security definer set search_path='' as $$
declare u uuid:=ar_reels_private.require_user();cid text;aid uuid;n integer;begin
 perform 1 from public.ar_reels_profiles where id=u and persona='Pro' for update;if not found then raise exception 'Pro access required' using errcode='42501';end if;
 select id into cid from public.ar_reels_challenges where chapter_id=p_chapter and active order by content_version desc limit 1;
 select count(*) into n from public.ar_reels_challenge_items i join public.ar_reels_media m on m.id=i.reel_id join public.ar_reels_questions q on q.id=i.question_id and q.reel_id=m.id join public.ar_reels_progress p on p.reel_id=m.id and p.user_id=u where i.challenge_id=cid and m.chapter_id=p_chapter and m.active and m.content_type='educational' and p.covered>=m.duration*.8 and exists(select 1 from public.ar_reels_chapters ch where ch.id=p_chapter and ch.active and (ch.content->>'available')::boolean);
 if cid is null or n<>5 or (select count(*) from public.ar_reels_challenge_items where challenge_id=cid)<>5 then raise exception 'Watch at least 80%% of all five mapped lessons first' using errcode='42501';end if;
 select id into aid from public.ar_reels_attempts where user_id=u and challenge_id=cid and not finished;
 if aid is null then insert into public.ar_reels_attempts(user_id,challenge_id,already_rewarded) values(u,cid,exists(select 1 from public.ar_reels_ledger where user_id=u and reward_key=cid)) returning id into aid;perform ar_reels_private.event(u,'reels_quiz_started',p_chapter,cid,aid);end if;
 return ar_reels_private.attempt_dto(aid);
end $$;
create function public.ar_reels_submit(p_attempt uuid,p_index integer,p_selected integer) returns jsonb language plpgsql security definer set search_path='' as $$
declare u uuid:=ar_reels_private.require_user();a public.ar_reels_attempts%rowtype;k ar_reels_private.answer_keys%rowtype;qid text;begin
 select * into a from public.ar_reels_attempts where id=p_attempt and user_id=u for update;if not found then raise exception 'Attempt not found' using errcode='42501';end if;
 if exists(select 1 from public.ar_reels_answers where attempt_id=p_attempt and position=p_index) then return ar_reels_private.attempt_dto(p_attempt);end if;
 if a.finished or p_index not between 0 and 4 or p_selected not between 0 and 3 or p_index is null or p_selected is null or p_index<>(select count(*) from public.ar_reels_answers where attempt_id=p_attempt) then raise exception 'Invalid answer transition';end if;
 select question_id into qid from public.ar_reels_challenge_items where challenge_id=a.challenge_id and position=p_index;if qid is null then raise exception 'Question does not belong to this challenge';end if;
 select * into strict k from ar_reels_private.answer_keys where question_id=qid;
 insert into public.ar_reels_answers(attempt_id,position,question_id,selected,correct,explanation) values(p_attempt,p_index,qid,p_selected,k.correct_index,k.explanation);
 perform ar_reels_private.event(u,'reels_quiz_answer_submitted',(select chapter_id from public.ar_reels_challenges where id=a.challenge_id),a.challenge_id,p_attempt,(select reel_id from public.ar_reels_questions where id=qid));return ar_reels_private.attempt_dto(p_attempt);
end $$;
create function public.ar_reels_finish(p_attempt uuid) returns jsonb language plpgsql security definer set search_path='' as $$
declare u uuid:=ar_reels_private.require_user();a public.ar_reels_attempts%rowtype;s integer;awarded boolean:=false;was_rewarded boolean;begin
 perform 1 from public.ar_reels_profiles where id=u for update;
 select * into a from public.ar_reels_attempts where id=p_attempt and user_id=u for update;if not found then raise exception 'Attempt not found' using errcode='42501';end if;
 if a.finished then return ar_reels_private.attempt_dto(p_attempt);end if;
 if not exists(select 1 from public.ar_reels_profiles where id=u and persona='Pro') or (select count(*) from public.ar_reels_challenge_items i join public.ar_reels_media m on m.id=i.reel_id join public.ar_reels_challenges c on c.id=i.challenge_id join public.ar_reels_progress p on p.user_id=u and p.reel_id=m.id where i.challenge_id=a.challenge_id and m.chapter_id=c.chapter_id and m.active and c.active and m.content_type='educational' and p.covered>=m.duration*.8 and exists(select 1 from public.ar_reels_chapters ch where ch.id=c.chapter_id and ch.active and (ch.content->>'available')::boolean))<>5 then raise exception 'Challenge eligibility changed' using errcode='42501';end if;
 if (select count(*) from public.ar_reels_answers where attempt_id=p_attempt)<>5 then raise exception 'Complete all five answers first';end if;
 select count(*) into s from public.ar_reels_answers ans join public.ar_reels_challenge_items i on i.challenge_id=a.challenge_id and i.position=ans.position and i.question_id=ans.question_id join ar_reels_private.answer_keys k on k.question_id=ans.question_id where ans.attempt_id=p_attempt and ans.selected=k.correct_index;
 select exists(select 1 from public.ar_reels_ledger where user_id=u and reward_key=a.challenge_id) into was_rewarded;
 if s=5 then insert into public.ar_reels_ledger(user_id,amount,reason,ref,reward_key) values(u,5,'Perfect challenge',a.challenge_id,a.challenge_id) on conflict(user_id,reward_key) do nothing;awarded:=found;end if;
 update public.ar_reels_attempts set finished=true,score=s,rewarded=awarded,already_rewarded=was_rewarded where id=p_attempt;
 if awarded then perform ar_reels_private.event(u,'reels_reward_earned',(select chapter_id from public.ar_reels_challenges where id=a.challenge_id),a.challenge_id,p_attempt);end if;
 perform ar_reels_private.event(u,'reels_quiz_completed',(select chapter_id from public.ar_reels_challenges where id=a.challenge_id),a.challenge_id,p_attempt);return ar_reels_private.attempt_dto(p_attempt);
end $$;
create function public.ar_reels_review(p_attempt uuid) returns jsonb language plpgsql security definer set search_path='' as $$
declare result jsonb;begin perform ar_reels_private.require_user();result:=ar_reels_private.attempt_dto(p_attempt);if result is null then raise exception 'Attempt not found' using errcode='42501';end if;return result;end $$;

create function ar_reels_private.quote(u uuid,p_product text,p_coins integer) returns jsonb language plpgsql security definer set search_path='' as $$
declare price bigint;b bigint;maximum bigint;used bigint;eligible boolean;begin
 select price_paise,p.eligible into price,eligible from public.ar_reels_products p where id=p_product and active;if price is null then raise exception 'Product unavailable';end if;
 b:=ar_reels_private.balance(u);maximum:=case when eligible then greatest(0,least(b,floor(floor(price*10/100)/20))) else 0 end;used:=greatest(0,least(maximum,coalesce(p_coins,0)));
 return jsonb_build_object('productId',p_product,'subtotal',price,'max',maximum,'coins',used,'discount',used*20,'payable',price-used*20,'remaining',b-used);
end $$;
create function public.ar_reels_quote(p_product text,p_coins integer) returns jsonb language plpgsql security definer set search_path='' as $$
declare u uuid:=ar_reels_private.require_user();rid uuid;q jsonb;begin
 perform 1 from public.ar_reels_profiles where id=u;if not found then raise exception 'Create your profile first';end if;
 insert into public.ar_reels_checkouts(user_id,product_id) values(u,p_product) on conflict do nothing;
 select request_id into rid from public.ar_reels_checkouts where user_id=u and product_id=p_product;
 select quote into q from public.ar_reels_orders where user_id=u and id=rid;if q is null then q:=ar_reels_private.quote(u,p_product,p_coins);end if;
 return q||jsonb_build_object('requestId',rid);
end $$;
create function public.ar_reels_confirm(p_product text,p_coins integer,p_request uuid) returns jsonb language plpgsql security definer set search_path='' as $$
declare u uuid:=ar_reels_private.require_user();q jsonb;o public.ar_reels_orders%rowtype;begin
 perform 1 from public.ar_reels_profiles where id=u for update;if not found then raise exception 'Create your profile first';end if;
 select * into o from public.ar_reels_orders where user_id=u and id=p_request;
 if found then if o.product_id<>p_product or (o.quote->>'coins')::integer<>p_coins then raise exception 'Idempotency key conflicts with an existing order';end if;return jsonb_build_object('id',o.id,'quote',o.quote,'at',o.created_at);end if;
 q:=ar_reels_private.quote(u,p_product,p_coins)||jsonb_build_object('requestId',p_request);
 if p_coins is null or p_coins<>(q->>'coins')::integer then raise exception 'Balance changed or insufficient coins. Request a new quote.' using errcode='P0001';end if;
 insert into public.ar_reels_orders(id,user_id,product_id,quote) values(p_request,u,p_product,q) returning * into o;
 insert into public.ar_reels_checkouts(user_id,product_id,request_id) values(u,p_product,p_request) on conflict(user_id,product_id) do update set request_id=excluded.request_id;
 if p_coins>0 then insert into public.ar_reels_ledger(user_id,amount,reason,ref) values(u,-p_coins,'Demo redemption',p_request::text);end if;
 perform ar_reels_private.event(u,'reels_redemption_confirmed');return jsonb_build_object('id',o.id,'quote',o.quote,'at',o.created_at);
end $$;

create function public.ar_reels_preference(p_key text,p_id text default null,p_value jsonb default null) returns void language plpgsql security definer set search_path='' as $$
declare u uuid:=ar_reels_private.require_user();v jsonb;items jsonb;begin
 select state into v from public.ar_reels_preferences where user_id=u for update;if not found then raise exception 'Create your profile first';end if;
 if p_key in ('likes','saved','downloads','deferred','offered') then
  if p_id is null or length(p_id)>100 then raise exception 'Invalid item';end if;items:=coalesce(v->p_key,'[]');
  if p_key in ('deferred','offered') then if not items ? p_id then items:=items||to_jsonb(p_id);perform ar_reels_private.event(u,case when p_key='offered' then 'reels_quiz_offered' else 'reels_quiz_deferred' end,p_id);end if;
  elsif items ? p_id then select coalesce(jsonb_agg(value),'[]') into items from jsonb_array_elements(items) where value<>to_jsonb(p_id);
  else items:=items||to_jsonb(p_id);end if;v:=jsonb_set(v,array[p_key],items);
 elsif p_key='mute' and jsonb_typeof(p_value)='boolean' then v:=jsonb_set(v,'{mute}',p_value);
 elsif p_key='lastReel' and exists(select 1 from public.ar_reels_media where id=p_id and active) then v:=jsonb_set(v,'{lastReel}',to_jsonb(p_id));
 else raise exception 'Invalid preference';end if;
 update public.ar_reels_preferences set state=v where user_id=u;
end $$;
create function public.ar_reels_analytics(p_event jsonb) returns void language plpgsql security definer set search_path='' as $$
declare u uuid:=ar_reels_private.require_user();n text:=p_event->>'name';begin
 if n not in ('reels_entry_clicked','reels_chapter_viewed','reels_video_started','reels_wallet_viewed','reels_redemption_quoted','reward_animation_shown') then raise exception 'Unsupported client event';end if;
 insert into public.ar_reels_events(id,user_id,name,session_id,chapter_id,reel_id,challenge_id,attempt_id,content_type,source,is_test,presenter)
 select (p_event->>'id')::uuid,u,n,(p_event->>'sessionId')::uuid,left(p_event->>'chapterId',100),left(p_event->>'reelId',100),left(p_event->>'challengeId',100),(p_event->>'attemptId')::uuid,case when p_event->>'content_type'='store' then 'store' else 'educational' end,'student',p.is_test,false from public.ar_reels_profiles p where id=u on conflict(user_id,id) do nothing;
end $$;
create function public.ar_reels_recognition() returns jsonb language plpgsql security definer set search_path='' as $$
declare u uuid:=ar_reels_private.require_user();result jsonb;begin
 select coalesce(jsonb_agg(jsonb_build_object('name',case when id=u then 'You' else 'Learner '||left(md5(id::text),6) end,'earned',earned,'current',id=u) order by earned desc,id),'[]') into result from (select p.id,coalesce(sum(l.amount) filter(where l.amount>0 and l.reward_key is not null),0) earned from public.ar_reels_profiles p left join public.ar_reels_ledger l on l.user_id=p.id where not p.is_test or p.id=u group by p.id order by earned desc,p.id limit 50) r;return result;
end $$;
create function public.ar_reels_snapshot() returns jsonb language plpgsql security definer set search_path='' as $$
declare u uuid:=ar_reels_private.require_user();prefs jsonb;p jsonb;begin
 select jsonb_build_object('id',id,'name',name,'batch',batch,'language',language,'persona',persona) into p from public.ar_reels_profiles where id=u;
 select state into prefs from public.ar_reels_preferences where user_id=u;
 return coalesce(prefs,'{}')||jsonb_build_object('profile',p,'presenter',false,'scenario','live',
 'coverage',coalesce((select jsonb_object_agg(reel_id,intervals) from public.ar_reels_progress where user_id=u),'{}'),
 'positions',coalesce((select jsonb_object_agg(reel_id,position) from public.ar_reels_progress where user_id=u),'{}'),
 'checkouts',coalesce((select jsonb_object_agg(product_id,request_id) from public.ar_reels_checkouts where user_id=u),'{}'),
 'attempts',coalesce((select jsonb_agg(ar_reels_private.attempt_dto(id) order by created_at) from public.ar_reels_attempts where user_id=u),'[]'),
 'ledger',coalesce((select jsonb_agg(jsonb_build_object('id',id,'amount',amount,'reason',reason,'ref',ref,'at',created_at) order by created_at,id) from public.ar_reels_ledger where user_id=u),'[]'),
 'orders',coalesce((select jsonb_agg(jsonb_build_object('id',id,'quote',quote,'at',created_at) order by created_at) from public.ar_reels_orders where user_id=u),'[]'),
 'events',coalesce((select jsonb_agg(jsonb_build_object('id',id,'name',name,'userId',user_id,'sessionId',session_id,'at',created_at,'demo',is_test,'presenter',presenter,'source',source,'content_type',content_type,'chapterId',chapter_id,'reelId',reel_id,'challengeId',challenge_id,'attemptId',attempt_id) order by created_at) from public.ar_reels_events where user_id=u),'[]'));
end $$;

-- Deny implicit PUBLIC execution. Only audited public entry points are granted.
revoke all on all functions in schema ar_reels_private from public,anon,authenticated;
do $grants$
declare signature text;begin
 foreach signature in array array['public.ar_reels_profile(text,text,text,boolean)','public.ar_reels_catalogue()','public.ar_reels_product_catalogue()','public.ar_reels_progress(text,numeric,numeric,numeric,numeric,boolean,boolean)','public.ar_reels_checkpoint(text,numeric)','public.ar_reels_start(text)','public.ar_reels_submit(uuid,integer,integer)','public.ar_reels_finish(uuid)','public.ar_reels_review(uuid)','public.ar_reels_quote(text,integer)','public.ar_reels_confirm(text,integer,uuid)','public.ar_reels_preference(text,text,jsonb)','public.ar_reels_analytics(jsonb)','public.ar_reels_recognition()','public.ar_reels_snapshot()'] loop
  execute format('revoke all on function %s from public,anon,authenticated',signature::regprocedure);
  execute format('grant execute on function %s to authenticated',signature::regprocedure);
 end loop;
end $grants$;
grant execute on function public.ar_reels_catalogue(),public.ar_reels_product_catalogue() to anon;
