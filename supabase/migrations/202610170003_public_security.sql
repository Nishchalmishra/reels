-- Public catalogue reads need no owner privileges; existing RLS/grants suffice.
alter function public.ar_reels_catalogue() security invoker;
alter function public.ar_reels_product_catalogue() security invoker;
-- Preserve the historical view/columns/data while making reads honor underlying RLS.
-- Reels recognition uses ar_reels_recognition(), never this net-balance view.
do $$ begin
 if to_regclass('public.leaderboard') is not null then
  execute 'alter view public.leaderboard set (security_invoker=true)';
 end if;
end $$;
