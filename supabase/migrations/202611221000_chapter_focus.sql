-- Chapter focus. A quiz cycle may be tied to one chapter (focus_chapter). Cycles without a
-- focus (focus off) behave exactly as before: 3/4 reels, exactly five questions.
-- With a focus, the quiz is built only from that chapter's qualified reels, the reel
-- threshold is min(3|4, reels in the chapter), and the quiz has up to five questions
-- (fewer when the chapter's reels have fewer mapped questions). A perfect score of any
-- length earns the usual reward, still once per unique reel/question set.
alter table public.ar_reels_discovery_cycles add column if not exists focus_chapter text;

create or replace function public.ar_reels_discovery_set_focus(p_chapter text) returns void language plpgsql security definer set search_path='' as $$
declare u uuid:=ar_reels_private.require_user();cid uuid;begin
 perform 1 from public.ar_reels_profiles where id=u for update;
 if not exists(select 1 from public.ar_reels_profiles where id=u and persona='Pro') then raise exception 'Pro access required' using errcode='42501';end if;
 if p_chapter is not null and not exists(select 1 from public.ar_reels_discovery_media where chapter_id=p_chapter and active and content->>'discovery'='true') then raise exception 'Unknown chapter' using errcode='22023';end if;
 cid:=ar_reels_private.discovery_cycle(u);
 -- A cycle that already produced a quiz is frozen; a new focus applies from the next cycle.
 update public.ar_reels_discovery_cycles set focus_chapter=p_chapter where id=cid and not exists(select 1 from public.ar_reels_discovery_batches b where b.cycle_id=cid);
end $$;

create or replace function public.ar_reels_discovery_offer() returns jsonb language plpgsql security definer set search_path='' as $$
declare u uuid:=ar_reels_private.require_user();cid uuid;bid uuid;t integer;rids text[];qids text[];key text;n integer;fc text;nr integer;begin
 cid:=ar_reels_private.discovery_cycle(u);
 select id into bid from public.ar_reels_discovery_batches where user_id=u and status='offered' order by created_at limit 1;
 if bid is not null then return jsonb_build_object('id',bid,'ready',true);end if;
 if exists(select 1 from public.ar_reels_discovery_batches where cycle_id=cid) then return jsonb_build_object('ready',false);end if;
 select threshold,focus_chapter into t,fc from public.ar_reels_discovery_cycles where id=cid;
 -- Chapter focus: a small chapter lowers the threshold to the reels it has (a chapter of 5+ reels keeps the normal 3/4).
 if fc is not null then select count(*) into nr from public.ar_reels_discovery_media where chapter_id=fc and active and content->>'discovery'='true';t:=least(t,greatest(nr,1));end if;
 select array_agg(reel_id order by qualified_at,reel_id) into rids from (select p.reel_id,p.qualified_at from public.ar_reels_discovery_coverage p join public.ar_reels_discovery_media m on m.id=p.reel_id where p.cycle_id=cid and p.qualified_at is not null and m.active and (fc is null or m.chapter_id=fc) and p.content_version=coalesce((m.content->>'contentVersion')::integer,1) order by p.qualified_at,p.reel_id limit 4) x;
 n:=coalesce(cardinality(rids),0);if n<t then return jsonb_build_object('ready',false);end if;
 -- Every watched reel must have a valid private key and at least one question.
 if exists(select 1 from unnest(rids) rid where not exists(select 1 from public.ar_reels_discovery_questions q join ar_reels_private.discovery_answer_keys k on k.question_id=q.id where q.reel_id=rid)) then return jsonb_build_object('ready',false,'diagnostic','missing_reel_question_mapping');end if;
 select array_agg(id order by rn,reel_id,id) into qids from (select * from (select q.id,q.reel_id,row_number() over(partition by q.reel_id order by q.id) rn from public.ar_reels_discovery_questions q join ar_reels_private.discovery_answer_keys k on k.question_id=q.id where q.reel_id=any(rids)) ranked order by rn,reel_id,id limit 5) chosen;
 if coalesce(cardinality(qids),0)<1 or (fc is null and cardinality(qids)<>5) then return jsonb_build_object('ready',false,'diagnostic','insufficient_mapped_questions');end if;
 select 'discovery-v1-'||md5(string_agg(reel_id||':'||content_version,',' order by reel_id)||'|'||(select string_agg(v,',' order by v) from unnest(qids) v)) into key from public.ar_reels_discovery_coverage where cycle_id=cid and reel_id=any(rids);
 insert into public.ar_reels_discovery_batches(user_id,cycle_id,reward_key) values(u,cid,key) returning id into bid;
 insert into public.ar_reels_discovery_items(batch_id,position,question_id,reel_id,content_version)
 select bid,(ord-1)::integer,q.id,q.reel_id,p.content_version from unnest(qids) with ordinality chosen(id,ord) join public.ar_reels_discovery_questions q on q.id=chosen.id join public.ar_reels_discovery_coverage p on p.cycle_id=cid and p.reel_id=q.reel_id;
 return jsonb_build_object('id',bid,'ready',true);
end $$;

create or replace function public.ar_reels_discovery_start(p_batch uuid) returns jsonb language plpgsql security definer set search_path='' as $$
declare u uuid:=ar_reels_private.require_user();b public.ar_reels_discovery_batches%rowtype;aid uuid;begin
 perform 1 from public.ar_reels_profiles where id=u and persona='Pro' for update;if not found then raise exception 'Pro access required' using errcode='42501';end if;
 select * into b from public.ar_reels_discovery_batches where id=p_batch and user_id=u for update;if not found then raise exception 'Quiz not found' using errcode='42501';end if;
 if (select count(*) from public.ar_reels_discovery_items where batch_id=b.id) not between 1 and 5 then raise exception 'Invalid mapped quiz';end if;
 if exists(select 1 from public.ar_reels_discovery_items i join public.ar_reels_discovery_media m on m.id=i.reel_id left join public.ar_reels_discovery_coverage p on p.cycle_id=b.cycle_id and p.reel_id=i.reel_id and p.content_version=i.content_version where i.batch_id=b.id and (not m.active or p.covered is null or p.covered<m.duration*.8 or i.content_version<>coalesce((m.content->>'contentVersion')::integer,1))) then raise exception 'Quiz eligibility changed' using errcode='42501';end if;
 select id into aid from public.ar_reels_discovery_attempts where user_id=u and discovery_batch_id=b.id and not finished;
 if aid is null then insert into public.ar_reels_discovery_attempts(user_id,discovery_batch_id,already_rewarded) values(u,b.id,exists(select 1 from public.ar_reels_ledger where user_id=u and reward_key=b.reward_key)) returning id into aid;end if;
 update public.ar_reels_discovery_batches set status='started' where id=b.id;
 return ar_reels_private.discovery_dto(aid);
end $$;

create or replace function public.ar_reels_discovery_finish(p_attempt uuid) returns jsonb language plpgsql security definer set search_path='' as $$
declare u uuid:=ar_reels_private.require_user();a public.ar_reels_discovery_attempts%rowtype;b public.ar_reels_discovery_batches%rowtype;s integer;awarded boolean:=false;was boolean;begin
 perform 1 from public.ar_reels_profiles where id=u for update;
 select * into a from public.ar_reels_discovery_attempts where id=p_attempt and user_id=u for update;if not found then raise exception 'Attempt not found' using errcode='42501';end if;
 if a.finished then return ar_reels_private.discovery_dto(p_attempt);end if;
 select * into strict b from public.ar_reels_discovery_batches where id=a.discovery_batch_id and user_id=u;
 if not exists(select 1 from public.ar_reels_profiles where id=u and persona='Pro') or exists(select 1 from public.ar_reels_discovery_items i join public.ar_reels_discovery_media m on m.id=i.reel_id left join public.ar_reels_discovery_coverage p on p.cycle_id=b.cycle_id and p.reel_id=i.reel_id and p.content_version=i.content_version where i.batch_id=b.id and (not m.active or p.covered is null or p.covered<m.duration*.8 or i.content_version<>coalesce((m.content->>'contentVersion')::integer,1))) then raise exception 'Quiz eligibility changed' using errcode='42501';end if;
 if (select count(*) from public.ar_reels_discovery_answers where attempt_id=p_attempt)<>(select count(*) from public.ar_reels_discovery_items where batch_id=b.id) then raise exception 'Complete all answers first';end if;
 select count(*) into s from public.ar_reels_discovery_answers ans join public.ar_reels_discovery_items i on i.batch_id=b.id and i.position=ans.position and i.question_id=ans.question_id join ar_reels_private.discovery_answer_keys k on k.question_id=ans.question_id where ans.attempt_id=p_attempt and ans.selected=k.correct_index;
 select exists(select 1 from public.ar_reels_ledger where user_id=u and reward_key=b.reward_key) into was;
 if s=(select count(*) from public.ar_reels_discovery_items where batch_id=b.id) then insert into public.ar_reels_ledger(user_id,amount,reason,ref,reward_key) values(u,5,'Perfect discovery quiz',b.reward_key,b.reward_key) on conflict(user_id,reward_key) do nothing;awarded:=found;end if;
 update public.ar_reels_discovery_attempts set finished=true,score=s,rewarded=awarded,already_rewarded=was where id=p_attempt;
 update public.ar_reels_discovery_batches set status='completed' where id=b.id;
 update public.ar_reels_discovery_cycles set closed=true where id=b.cycle_id and user_id=u;perform ar_reels_private.discovery_cycle(u);
 return ar_reels_private.discovery_dto(p_attempt);
end $$;

revoke all on function public.ar_reels_discovery_set_focus(text) from public,anon;
grant execute on function public.ar_reels_discovery_set_focus(text) to authenticated;
