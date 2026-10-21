import {beforeAll,afterAll,it,expect} from 'vitest';
import {PGlite} from '@electric-sql/pglite';
import {buildMigrationBatch} from '../../scripts/migration-batch.mjs';
let db:PGlite;const A=crypto.randomUUID(), B=crypto.randomUUID();
const rpc=async(raw:string,args:unknown[]=[]) => {const name=["progress","submit","finish","review"].includes(raw)?"discovery_"+raw:raw;return (await db.query<{v:any}>(`select public.ar_reels_${name}(${args.map((_,i)=>'$'+(i+1)).join(',')}) v`,args)).rows[0].v;};
const user=async(id=A)=>db.exec(`reset role;set role authenticated;set request.jwt.claim.sub='${id}'`);
const owner=async()=>db.exec('reset role');
let media:any[]=[];
beforeAll(async()=>{
 db=new PGlite();
 await db.exec(`create role anon nologin;create role authenticated nologin;create schema auth;create table auth.users(id uuid primary key);create function auth.uid() returns uuid language sql stable as $$ select nullif(current_setting('request.jwt.claim.sub',true),'')::uuid $$;grant usage on schema auth to anon,authenticated;grant execute on function auth.uid() to anon,authenticated;`);
 await db.exec(buildMigrationBatch().sql);await db.exec(buildMigrationBatch().sql);
 for(const id of [A,B]){await owner();await db.query('insert into auth.users(id) values($1)',[id]);await user(id);await rpc('profile',['TEST discovery','11 science','Hindi',true]);}
 await user();media=await rpc('discovery_catalogue');
},60000);
afterAll(async()=>db?.close());
async function watch(id:string,duration:number){for(let t=0;t<Math.ceil(duration*.8);t++)await rpc('progress',[id,t,Math.min(t+1,duration),1,1,true,true]);}
it('persists one private 3/4 threshold, rejects swipe/seek/background/repeat qualification',async()=>{
 const state=await rpc('discovery_state');expect(state).not.toHaveProperty('threshold');
 expect((await rpc('discovery_state')).cycleId).toBe(state.cycleId);
 await rpc('progress',[media[0].id,0,media[0].duration,1,1,true,true]);
 await rpc('progress',[media[0].id,0,1,1,1,false,true]);
 for(let i=0;i<5;i++)await rpc('progress',[media[0].id,0,1,1,1,true,true]);
 expect((await rpc('discovery_offer')).ready).toBe(false);
 await expect(db.query('select * from public.ar_reels_discovery_cycles')).rejects.toThrow(/permission denied/);
});
let batch:any,first:any;
it('freezes five mapped questions across three/four actual watched reels and defers once',async()=>{
 for(const m of media.slice(0,4))await watch(m.id,m.duration);
 batch=await rpc('discovery_offer');expect(batch.ready).toBe(true);
 expect((await rpc('discovery_offer')).id).toBe(batch.id);
 await user(B);await expect(rpc('discovery_start',[batch.id])).rejects.toThrow(/Quiz not found/);
 await user();await rpc('discovery_defer',[batch.id]);const state=await rpc('discovery_state');
 expect(state.pending[0].status).toBe('deferred');expect((await rpc('discovery_offer')).ready).toBe(false);
 await rpc('discovery_defer',[batch.id]);expect((await rpc('discovery_state')).cycleId).toBe(state.cycleId);
 first=await rpc('discovery_start',[batch.id]);expect(first.questions).toHaveLength(5);
 expect(new Set(first.questions.map((q:any)=>q.reelId)).size).toBe(4);
 expect(first.questions.every((q:any)=>media.slice(0,4).some(m=>m.id===q.reelId))).toBe(true);
 expect(first.questions.every((q:any)=>!('correct' in q)&&!('explanation' in q))).toBe(true);
 expect((await rpc('discovery_start',[batch.id])).id).toBe(first.id);
});
async function answer(a:any,perfect:boolean){
 for(let i=0;i<5;i++){
  await owner();const key=(await db.query<{correct_index:number}>('select correct_index from ar_reels_private.discovery_answer_keys where question_id=$1',[a.questions[i].id])).rows[0].correct_index;
  await user();const selected=!perfect&&i===0?(key+1)%4:key;
  const saved=await rpc('submit',[a.id,i,selected]);const repeated=await rpc('submit',[a.id,i,(selected+1)%4]);expect(repeated.answers).toEqual(saved.answers);
 }
 return rpc('finish',[a.id]);
}
it('awards zero for 4/5 then five total on a perfect retry, never twice',async()=>{
 const failed=await answer(first,false);expect(failed.score).toBe(4);expect(failed.rewarded).toBe(false);
 const retry=await rpc('discovery_start',[batch.id]);expect(retry.id).not.toBe(first.id);
 const completed=await answer(retry,true);expect(completed.score).toBe(5);expect(completed.rewarded).toBe(true);
 expect(await rpc('finish',[retry.id])).toEqual(completed);
 const practice=await rpc('discovery_start',[batch.id]);expect((await answer(practice,true)).rewarded).toBe(false);
 const snapshot=await rpc('snapshot');expect(snapshot.ledger.reduce((n:number,r:any)=>n+r.amount,0)).toBe(5);
});
it('private keys and other users attempts remain inaccessible, redemption remains transactional',async()=>{
 await expect(db.query('select * from ar_reels_private.discovery_answer_keys')).rejects.toThrow(/permission denied/);
 await user(B);await expect(rpc('review',[first.id])).rejects.toThrow(/Attempt not found/);
 await user();const quote=await rpc('quote',['pro',5]);expect(quote.discount).toBe(100);
 expect((await rpc('snapshot')).ledger.reduce((n:number,r:any)=>n+r.amount,0)).toBe(5);
 const order=await rpc('confirm',['pro',5,quote.requestId]);expect(await rpc('confirm',['pro',5,quote.requestId])).toEqual(order);
 expect((await rpc('snapshot')).ledger.reduce((n:number,r:any)=>n+r.amount,0)).toBe(0);
 const rank=await rpc('recognition');expect(rank.find((r:any)=>r.current).earned).toBe(5);
});
it('acceptance keeps the cycle stable and blocks overlapping offers until completion or explicit deferral',async()=>{
 const C=crypto.randomUUID();await owner();await db.query('insert into auth.users(id) values($1)',[C]);await user(C);await rpc('profile',['TEST lifecycle','11 science','Hindi',true]);
 const initial=await rpc('discovery_state');
 // Deterministic embedded-only fixture for the three-video distribution edge.
 await owner();await db.query('update public.ar_reels_discovery_cycles set threshold=3 where id=$1',[initial.cycleId]);await user(C);
 for(const m of media.slice(0,3))await watch(m.id,m.duration);
 const offer=await rpc('discovery_offer');expect(offer.ready).toBe(true);
 const attempt=await rpc('discovery_start',[offer.id]);expect((await rpc('discovery_state')).cycleId).toBe(initial.cycleId);expect((await rpc('discovery_offer')).ready).toBe(false);
 expect(new Set(attempt.questions.map((q:any)=>q.reelId)).size).toBe(3);
 const distribution=new Map<string,number>();for(const q of attempt.questions)distribution.set(q.reelId,(distribution.get(q.reelId)||0)+1);expect([...distribution.values()].sort()).toEqual([1,2,2]);
 await rpc('discovery_defer',[offer.id]);const advanced=await rpc('discovery_state');expect(advanced.cycleId).not.toBe(initial.cycleId);
 expect((await rpc('discovery_start',[offer.id])).id).toBe(attempt.id);expect((await rpc('discovery_state')).cycleId).toBe(advanced.cycleId);
});
it('rewatching the same content in a different order cannot reward a new cycle again',async()=>{
 await user(A);for(const m of [...media.slice(0,4)].reverse())await watch(m.id,m.duration);
 const offered=await rpc('discovery_offer');expect(offered.ready).toBe(true);expect(offered.id).not.toBe(batch.id);
 const repeatedContent=await rpc('discovery_start',[offered.id]);expect(repeatedContent.alreadyRewarded).toBe(true);
 expect(repeatedContent.questions.map((q:any)=>q.id)).toEqual(first.questions.map((q:any)=>q.id));
 const finished=await answer(repeatedContent,true);expect(finished.score).toBe(5);expect(finished.rewarded).toBe(false);expect(finished.alreadyRewarded).toBe(true);
 const ledger=(await rpc('snapshot')).ledger;expect(ledger.filter((row:any)=>row.amount===5)).toHaveLength(1);
});
