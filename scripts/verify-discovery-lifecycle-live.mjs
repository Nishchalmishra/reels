import {createClient} from '@supabase/supabase-js';
import {publicConfig} from './supabase-env.mjs';
import fs from 'node:fs';import assert from 'node:assert/strict';
const {url,key}=publicConfig(),run=crypto.randomUUID();const c=createClient(url,key,{auth:{persistSession:false,autoRefreshToken:false}});
const report={run,at:new Date().toISOString(),checks:[]};
const rpc=async(n,p={})=>{const {data,error}=await c.rpc('ar_reels_'+n,p);if(error)throw Error(n+': '+error.message);return data;};
try{
 const {data,error}=await c.auth.signInAnonymously({options:{data:{integration_test:true,discovery_lifecycle_test:true,run}}});if(error)throw error;report.userId=data.user.id;
 await rpc('profile',{p_name:'TEST discovery lifecycle',p_batch:'TEST source-mapped content',p_language:'Hindi',p_test:true});const initial=await rpc('discovery_state');const media=await rpc('discovery_catalogue');
 async function watch(m){const parts=[];for(let t=0;t<Math.ceil(m.duration*.8);t++)parts.push({p_reel:m.id,p_from:t,p_to:Math.min(t+1,m.duration),p_wall:1,p_speed:1,p_visible:true,p_playing:true});for(let i=0;i<parts.length;i+=6)await Promise.all(parts.slice(i,i+6).map(p=>rpc('discovery_progress',p)));}
 for(const m of media.slice(0,3))await watch(m);let offer=await rpc('discovery_offer');if(!offer.ready){await watch(media[3]);offer=await rpc('discovery_offer');}assert.ok(offer.ready);
 const attempt=await rpc('discovery_start',{p_batch:offer.id});report.attemptId=attempt.id;report.mappedReels=new Set(attempt.questions.map(q=>q.reelId)).size;assert.ok([3,4].includes(report.mappedReels));assert.equal((await rpc('discovery_state')).cycleId,initial.cycleId);assert.equal((await rpc('discovery_offer')).ready,false);report.checks.push('acceptance preserves cycle and blocks overlapping invitations');
 for(let i=0;i<5;i++)await rpc('discovery_submit',{p_attempt:attempt.id,p_index:i,p_selected:attempt.questions[i].id.endsWith('-q1')?0:1});
 const finished=await Promise.all(Array.from({length:3},()=>rpc('discovery_finish',{p_attempt:attempt.id})));assert.ok(finished.every(a=>a.score===5));const advanced=await rpc('discovery_state');assert.notEqual(advanced.cycleId,initial.cycleId);await rpc('discovery_finish',{p_attempt:attempt.id});assert.equal((await rpc('discovery_state')).cycleId,advanced.cycleId);report.checks.push('completion advances once; concurrent repeated finish retains one reward and the same next cycle');
 const practice=await rpc('discovery_start',{p_batch:offer.id});assert.equal((await rpc('discovery_state')).cycleId,advanced.cycleId);await rpc('discovery_defer',{p_batch:offer.id});assert.equal((await rpc('discovery_state')).cycleId,advanced.cycleId);assert.equal((await rpc('discovery_start',{p_batch:offer.id})).id,practice.id);report.checks.push('deferring/resuming older practice preserves the current viewing cycle and exact attempt');
 const snapshot=await rpc('snapshot');assert.equal(snapshot.ledger.length,1);assert.equal(snapshot.ledger[0].amount,5);report.status='passed';
}catch(e){report.status='failed';report.error=e.message;process.exitCode=1;}
fs.mkdirSync('verification-native',{recursive:true});fs.writeFileSync('verification-native/discovery-lifecycle-live.json',JSON.stringify(report,null,2));console.log(JSON.stringify(report,null,2));
