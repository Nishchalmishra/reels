import fs from 'node:fs';import assert from 'node:assert/strict';import {chromium} from 'playwright';import {createClient} from '@supabase/supabase-js';import {publicConfig} from './supabase-env.mjs';import {retainTestSessions} from './test-sessions.mjs';
const {url,key}=publicConfig(),qa=JSON.parse(fs.readFileSync('supabase/.test-sessions/admin-qa-private.json','utf8')),retained=JSON.parse(fs.readFileSync('supabase/.test-sessions.json','utf8'));
const client=()=>createClient(url,key,{auth:{persistSession:false,autoRefreshToken:false}}),admin=client(),student=client();
const a=await admin.auth.signInWithPassword({email:qa.email,password:qa.password});if(a.error)throw Error('QA admin authentication failed');
const auth=await student.auth.setSession(retained.sessions[0]);if(auth.error)throw Error('TEST student restoration failed');retainTestSessions(retained.run,retained.users,[auth.data.session]);
const rpc=async(c,name,args={})=>{const {data,error}=await c.rpc('ar_reels_'+name,args);if(error)throw Error(name+': '+error.message);return data};
const snapshot=await rpc(student,'snapshot'),grade=snapshot.profile.batch.includes('12')?12:11,catalogue=await rpc(student,'discovery_catalogue'),lesson=catalogue.find(l=>l.grades.includes(grade));
const before=await rpc(admin,'admin_dashboard',{p_days:7,p_include_test:true}),normalBefore=await rpc(admin,'admin_dashboard',{p_days:7,p_include_test:false});
const browser=await chromium.launch({channel:'chrome',headless:true}),page=await browser.newPage({viewport:{width:390,height:697}}),reports=[];page.on('request',r=>{if(r.url().endsWith('/rpc/ar_reels_discovery_playback'))reports.push(r.postDataJSON())});
await page.addInitScript(s=>localStorage.setItem('sb-pbaegeqjargtsfortsyh-auth-token',JSON.stringify(s)),auth.data.session);
const result={status:'running',studentId:auth.data.user.id,reelId:lesson.id,checks:[]};
try{
 await page.goto('http://127.0.0.1:4176/reels?lesson='+lesson.id,{waitUntil:'domcontentloaded',timeout:60000});await page.waitForFunction(()=>{const v=document.querySelector('.feed-slide video');return v&&v.currentTime>5.5&&!v.paused},{},{timeout:30000});
 await page.getByRole('button',{name:'Pause lesson',exact:true}).click();await page.waitForTimeout(2200);assert.ok(reports.length>=6);
 const after=await rpc(admin,'admin_dashboard',{p_days:7,p_include_test:true}),normalAfter=await rpc(admin,'admin_dashboard',{p_days:7,p_include_test:false});
 const old=before.reels.find(r=>r.id===lesson.id),now=after.reels.find(r=>r.id===lesson.id);assert.equal(now.views,old.views+1);assert.ok(now.watchSeconds>=old.watchSeconds+3);assert.equal(normalAfter.views,normalBefore.views);
 const replay=reports.at(-1);await Promise.all(Array.from({length:4},()=>rpc(student,'discovery_playback',replay)));
 await rpc(student,'discovery_playback',{...replay,p_sequence:replay.p_sequence+1,p_visible:false});await rpc(student,'discovery_playback',{...replay,p_sequence:replay.p_sequence+1,p_from:0,p_to:lesson.duration});
 const duplicate=await rpc(admin,'admin_dashboard',{p_days:7,p_include_test:true});const same=duplicate.reels.find(r=>r.id===lesson.id);assert.equal(same.views,now.views);assert.equal(same.watchSeconds,now.watchSeconds);
 result.checks=['Actual mobile student playback creates one >=3-second view and real watch time','Test playback excluded from normal admin totals','Repeated concurrent telemetry, background reports and invalid seeks cannot inflate analytics'];result.viewsAdded=now.views-old.views;result.watchSecondsAdded=now.watchSeconds-old.watchSeconds;result.sessionId=replay.p_session;result.status='passed';
}catch(e){result.status='failed';result.error=e.message;process.exitCode=1;await page.screenshot({path:'verification-live/admin/analytics-failure.png'}).catch(()=>{})}
finally{await browser.close();fs.writeFileSync('verification-live/admin/analytics.json',JSON.stringify(result,null,2));console.log(JSON.stringify(result,null,2))}
