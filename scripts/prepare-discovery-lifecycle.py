"""Generate a versioned fix scoped to the newly introduced discovery RPCs."""
from pathlib import Path
import re
root=Path(__file__).resolve().parents[1]
source=(root/'supabase/migrations/202610190004_discovery.sql').read_text(encoding='utf8')
parts=[]
for name in ['offer','defer','start','finish']:
 match=re.search(r'create function public\.ar_reels_discovery_'+name+r'\(.*?\$\$;\n',source,re.S)
 if not match: raise ValueError(name)
 sql=match.group().replace('create function','create or replace function',1)
 if name=='offer':
  sql=sql.replace(' select threshold into t'," if exists(select 1 from public.ar_reels_discovery_batches where cycle_id=cid) then return jsonb_build_object('ready',false);end if;\n select threshold into t")
 if name=='defer':sql=sql.replace("and status='offered' returning","and status in ('offered','started') returning")
 if name=='start':
  sql=sql.replace(' -- Advance once at offer acceptance, so completed/deferred cycles cannot reopen.\n update public.ar_reels_discovery_cycles set closed=true where id=b.cycle_id and user_id=u;perform ar_reels_private.discovery_cycle(u);\n','')
 if name=='finish':
  sql=sql.replace(" update public.ar_reels_discovery_batches set status='completed' where id=b.id;", " update public.ar_reels_discovery_batches set status='completed' where id=b.id;\n update public.ar_reels_discovery_cycles set closed=true where id=b.cycle_id and user_id=u;perform ar_reels_private.discovery_cycle(u);")
 parts.append(sql)
analytics=(root/'supabase/migrations/202610190006_discovery_analytics.sql').read_text(encoding='utf8')
trigger=re.search(r'create function ar_reels_private\.discovery_activity\(\).*?\$\$;',analytics,re.S).group()
parts.append(trigger.replace('create function','create or replace function',1).replace("old.status='offered'", "old.status in ('offered','started')"))
(root/'supabase/migrations/202610190007_discovery_lifecycle.sql').write_text('-- Only discovery RPCs introduced in 004 are refined; legacy chapter operations remain untouched.\n-- Acceptance keeps a cycle open; completion or explicit deferral advances it once.\n'+'\n'.join(parts),encoding='utf8')
print('Prepared discovery-only lifecycle migration.')
