-- Coins: one coin for every correct answer in a Reels quiz (previously 5 coins for a perfect quiz).
-- Each question pays at most once per student, tracked in discovery_question_rewards, so a retry
-- earns coins only for questions that have not paid before. Past ledger rows are untouched.
alter table public.ar_reels_discovery_attempts add column if not exists coins integer not null default 0;
create table if not exists public.ar_reels_discovery_question_rewards(user_id uuid not null references public.ar_reels_profiles(id) on delete cascade,question_id text not null references public.ar_reels_discovery_questions(id),created_at timestamptz not null default now(),primary key(user_id,question_id));
alter table public.ar_reels_discovery_question_rewards enable row level security;
revoke all on public.ar_reels_discovery_question_rewards from public,anon,authenticated;

create or replace function ar_reels_private.discovery_dto(aid uuid) returns jsonb language sql stable security definer set search_path='' as $$
 select jsonb_build_object('id',a.id,'batchId',a.discovery_batch_id,'challengeId',b.reward_key,'finished',a.finished,'score',a.score,'rewarded',a.rewarded,'alreadyRewarded',a.already_rewarded,'coins',a.coins,
 'questions',(select jsonb_agg(jsonb_build_object('id',q.id,'reelId',i.reel_id,'contentVersion',i.content_version,'prompt',q.prompt,'options',q.options) order by i.position) from public.ar_reels_discovery_items i join public.ar_reels_discovery_questions q on q.id=i.question_id where i.batch_id=b.id),
 'answers',(select jsonb_agg(case when ans.position is null then 'null'::jsonb else jsonb_build_object('selected',ans.selected,'correct',ans.correct,'explanation',ans.explanation) end order by i.position) from public.ar_reels_discovery_items i left join public.ar_reels_discovery_answers ans on ans.attempt_id=a.id and ans.position=i.position where i.batch_id=b.id))
 from public.ar_reels_discovery_attempts a join public.ar_reels_discovery_batches b on b.id=a.discovery_batch_id where a.id=aid and a.user_id=auth.uid()
$$;

create or replace function public.ar_reels_discovery_finish(p_attempt uuid) returns jsonb language plpgsql security definer set search_path='' as $$
declare u uuid:=ar_reels_private.require_user();a public.ar_reels_discovery_attempts%rowtype;b public.ar_reels_discovery_batches%rowtype;s integer;c integer:=0;awarded boolean:=false;was boolean;begin
 perform 1 from public.ar_reels_profiles where id=u for update;
 select * into a from public.ar_reels_discovery_attempts where id=p_attempt and user_id=u for update;if not found then raise exception 'Attempt not found' using errcode='42501';end if;
 if a.finished then return ar_reels_private.discovery_dto(p_attempt);end if;
 select * into strict b from public.ar_reels_discovery_batches where id=a.discovery_batch_id and user_id=u;
 if not exists(select 1 from public.ar_reels_profiles where id=u and persona='Pro') or exists(select 1 from public.ar_reels_discovery_items i join public.ar_reels_discovery_media m on m.id=i.reel_id left join public.ar_reels_discovery_coverage p on p.cycle_id=b.cycle_id and p.reel_id=i.reel_id and p.content_version=i.content_version where i.batch_id=b.id and (not m.active or p.covered is null or p.covered<m.duration*.8 or i.content_version<>coalesce((m.content->>'contentVersion')::integer,1))) then raise exception 'Quiz eligibility changed' using errcode='42501';end if;
 if (select count(*) from public.ar_reels_discovery_answers where attempt_id=p_attempt)<>(select count(*) from public.ar_reels_discovery_items where batch_id=b.id) then raise exception 'Complete all answers first';end if;
 select count(*) into s from public.ar_reels_discovery_answers ans join public.ar_reels_discovery_items i on i.batch_id=b.id and i.position=ans.position and i.question_id=ans.question_id join ar_reels_private.discovery_answer_keys k on k.question_id=ans.question_id where ans.attempt_id=p_attempt and ans.selected=k.correct_index;
 -- One coin per correct answer. Each question pays once per student, so replaying a quiz cannot farm coins.
 with correct_q as (select ans.question_id from public.ar_reels_discovery_answers ans join public.ar_reels_discovery_items i on i.batch_id=b.id and i.position=ans.position and i.question_id=ans.question_id join ar_reels_private.discovery_answer_keys k on k.question_id=ans.question_id where ans.attempt_id=p_attempt and ans.selected=k.correct_index),
 ins as (insert into public.ar_reels_discovery_question_rewards(user_id,question_id) select u,question_id from correct_q on conflict do nothing returning 1)
 select count(*) into c from ins;
 if c>0 then insert into public.ar_reels_ledger(user_id,amount,reason,ref,reward_key) values(u,c,'Correct quiz answers','discovery-attempt:'||p_attempt,'discovery-attempt:'||p_attempt) on conflict(user_id,reward_key) do nothing;end if;
 awarded:=c>0;was:=(s>0 and c=0);
 update public.ar_reels_discovery_attempts set finished=true,score=s,rewarded=awarded,already_rewarded=was,coins=c where id=p_attempt;
 update public.ar_reels_discovery_batches set status='completed' where id=b.id;
 update public.ar_reels_discovery_cycles set closed=true where id=b.cycle_id and user_id=u;perform ar_reels_private.discovery_cycle(u);
 return ar_reels_private.discovery_dto(p_attempt);
end $$;
