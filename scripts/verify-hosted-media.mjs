// Actual production bundle + actual Supabase Storage. No intercepted media responses.
import {chromium} from 'playwright';
import {createClient} from '@supabase/supabase-js';
import fs from 'node:fs';
import assert from 'node:assert/strict';
import {publicConfig} from './supabase-env.mjs';
import {retainTestSessions} from './test-sessions.mjs';
const {url,key}=publicConfig(),run=crypto.randomUUID(),base=process.env.REELS_BASE_URL||'http://127.0.0.1:4175';
const report={run,project:'pbaegeqjargtsfortsyh',checks:[],lessons:[],status:'running'};
const c=createClient(url,key,{auth:{persistSession:false,autoRefreshToken:false}});
const rpc=async(name,args={})=>{const {data,error}=await c.rpc('ar_reels_'+name,args);if(error)throw Error(name+': '+error.message);return data};
const {data,error}=await c.auth.signInAnonymously({options:{data:{integration_test:true,hosted_media_test:true}}});if(error)throw Error('Cannot authenticate hosted media TEST identity');
report.userId=data.user.id;retainTestSessions(run,[data.user.id],[data.session]);
const browser=await chromium.launch({channel:'chrome',headless:true});
const page=await browser.newPage({viewport:{width:390,height:697}}),pageErrors=[],localMedia=[];
page.on('pageerror',e=>pageErrors.push(e.message));page.on('request',r=>{if(r.url().startsWith(base+'/media/'))localMedia.push(r.url())});
await page.addInitScript(s=>localStorage.setItem('sb-pbaegeqjargtsfortsyh-auth-token',JSON.stringify(s)),data.session);
fs.mkdirSync('verification-live/hosted-media',{recursive:true});
try{
 for(const grade of [11,12]){
  await rpc('profile',{p_name:'TEST hosted media '+run.slice(0,8),p_batch:`Class ${grade} Physics`,p_language:'English',p_test:true});
  const catalogue=(await rpc('discovery_catalogue')).filter(l=>l.grades.includes(grade));
  assert.equal(catalogue.length,grade===11?6:4);
  for(const lesson of catalogue){
   assert.ok(lesson.source.startsWith(url+'/storage/v1/object/public/arivihan-reels/'));
   await page.goto(`${base}/reels?lesson=${lesson.id}`,{waitUntil:'domcontentloaded',timeout:60000});
   await page.waitForFunction(id=>{const v=document.querySelector('.feed-slide video');return v&&v.src.includes(id)&&v.readyState>=2&&v.currentTime>1.5&&!v.paused},lesson.id,{timeout:60000});
   const actual=await page.locator('.feed-slide video').first().evaluate(v=>({source:v.currentSrc,duration:v.duration,currentTime:v.currentTime,error:v.error?.code||null}));
   assert.ok(actual.source.startsWith(url+'/storage/v1/object/public/arivihan-reels/'));assert.equal(actual.error,null);assert.ok(Math.abs(actual.duration-lesson.duration)<1);
   assert.equal(await page.locator('video').evaluateAll(vs=>vs.filter(v=>!v.paused).length),1);
   report.lessons.push({id:lesson.id,grade,played:true,source:actual.source,duration:actual.duration});console.log('PASS hosted playback:',lesson.id);
   if(report.lessons.length===1)await page.screenshot({path:'verification-live/hosted-media/feed.png'});
  }
  await page.getByRole('button',{name:'Explore',exact:true}).click();
  assert.equal(await page.locator('.lesson-tile').count(),catalogue.length);
  await page.waitForFunction(()=>[...document.querySelectorAll('.lesson-tile img')].every(i=>i.complete&&i.naturalWidth>0));
  if(grade===12)await page.screenshot({path:'verification-live/hosted-media/explore.png'});
 }
 report.checks.push('All ten actual MP4s play from Supabase Storage in the built mobile frontend, with one active player and measured durations');
 assert.deepEqual(localMedia,[]);assert.deepEqual(pageErrors,[]);
 report.checks.push('No local /media requests, intercepted video responses or browser page errors');
 await page.reload({waitUntil:'domcontentloaded'});await page.locator('.lesson-tile').first().waitFor();
 report.checks.push('Hosted posters and class-filtered Explore survive refresh');
 const upload=await c.storage.from('arivihan-reels').upload('security-probe-'+run+'.mp4',new Uint8Array([0,0,0,0]),{contentType:'video/mp4',upsert:false});assert.ok(upload.error);
 report.checks.push('An ordinary authenticated student cannot upload to the media bucket');
 const closed=await fetch(url+'/functions/v1/ar-reels-media-upload-20261022',{method:'POST',headers:{apikey:key,'Content-Type':'application/json'},body:JSON.stringify({action:'sign'})});assert.equal(closed.status,410);
 report.checks.push('Temporary upload endpoint is disabled (410)');
 const snapshot=await rpc('snapshot');assert.equal(snapshot.ledger.length,0);assert.equal(snapshot.orders.length,0);
 report.status='passed';
}catch(e){report.status='failed';report.error=e.message;process.exitCode=1;await page.screenshot({path:'verification-live/hosted-media/failure.png'}).catch(()=>{})}
finally{await browser.close();fs.writeFileSync('verification-live/hosted-media/browser.json',JSON.stringify(report,null,2));console.log(JSON.stringify(report,null,2))}
