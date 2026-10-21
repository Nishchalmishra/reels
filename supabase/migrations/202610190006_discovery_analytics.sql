-- Only new discovery triggers/functions. Existing analytics tables/RPCs remain unchanged.
create function ar_reels_private.discovery_activity() returns trigger language plpgsql security definer set search_path='' as $$
declare u uuid;rid text;rk text;begin
 if tg_table_name='ar_reels_discovery_batches' then
  u:=new.user_id;rk:=new.reward_key;
  if tg_op='INSERT' then perform ar_reels_private.event(u,'reels_quiz_offered',null,rk);
  elsif old.status='offered' and new.status='deferred' then perform ar_reels_private.event(u,'reels_quiz_deferred',null,rk);end if;
 elsif tg_table_name='ar_reels_discovery_attempts' then
  u:=new.user_id;select reward_key into rk from public.ar_reels_discovery_batches where id=new.discovery_batch_id;
  if tg_op='INSERT' then perform ar_reels_private.event(u,'reels_quiz_started',null,rk,new.id);
  elsif not old.finished and new.finished then
   perform ar_reels_private.event(u,'reels_quiz_completed',null,rk,new.id);
   if new.rewarded then perform ar_reels_private.event(u,'reels_reward_earned',null,rk,new.id);end if;
  end if;
 elsif tg_table_name='ar_reels_discovery_answers' then
  select a.user_id,b.reward_key into u,rk from public.ar_reels_discovery_attempts a join public.ar_reels_discovery_batches b on b.id=a.discovery_batch_id where a.id=new.attempt_id;
  select reel_id into rid from public.ar_reels_discovery_questions where id=new.question_id;
  perform ar_reels_private.event(u,'reels_quiz_answer_submitted',null,rk,new.attempt_id,rid);
 elsif tg_table_name='ar_reels_discovery_coverage' then
  select user_id into u from public.ar_reels_discovery_cycles where id=new.cycle_id;
  if old.qualified_at is null and new.qualified_at is not null then perform ar_reels_private.event(u,'reels_video_completed',null,null,null,new.reel_id);
  elsif floor(old.covered/5)<>floor(new.covered/5) then perform ar_reels_private.event(u,'reels_watch_progress',null,null,null,new.reel_id);end if;
 end if;return new;
end $$;
revoke all on function ar_reels_private.discovery_activity() from public,anon,authenticated;
create trigger discovery_batch_activity after insert or update on public.ar_reels_discovery_batches for each row execute function ar_reels_private.discovery_activity();
create trigger discovery_attempt_activity after insert or update on public.ar_reels_discovery_attempts for each row execute function ar_reels_private.discovery_activity();
create trigger discovery_answer_activity after insert on public.ar_reels_discovery_answers for each row execute function ar_reels_private.discovery_activity();
create trigger discovery_coverage_activity after update on public.ar_reels_discovery_coverage for each row execute function ar_reels_private.discovery_activity();
