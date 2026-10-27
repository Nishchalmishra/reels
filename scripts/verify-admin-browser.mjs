import fs from 'node:fs';import assert from 'node:assert/strict';import {chromium} from 'playwright';import {createClient} from '@supabase/supabase-js';import {publicConfig} from './supabase-env.mjs';
const {url,key}=publicConfig(),job=JSON.parse(fs.readFileSync('supabase/.test-sessions/admin-qa-private.json','utf8'));
const report={run:job.run,checks:[],status:'running'};const c=createClient(url,key,{auth:{persistSession:false,autoRefreshToken:false}});
const {data,error}=await c.auth.signInWithPassword({email:job.email,password:job.password});if(error)throw Error('QA admin sign-in failed');
const rpc=async(name,args={})=>{const {data,error}=await c.rpc('ar_reels_admin_'+name,args);if(error)throw Error(name+': '+error.message);return data};
const browser=await chromium.launch({channel:'chrome',headless:true});const page=await browser.newPage({viewport:{width:1440,height:1000}}),pageErrors=[];page.on('pageerror',e=>pageErrors.push(e.message));
try{
 await page.goto('http://127.0.0.1:4176/admin',{waitUntil:'domcontentloaded',timeout:60000});
 await page.getByLabel('Email',{exact:true}).fill(job.email);await page.getByLabel('Password',{exact:true}).fill(job.password);await page.getByRole('button',{name:'Sign in',exact:true}).click();
 await page.getByRole('heading',{name:'A clearer view of learning.'}).waitFor();await page.screenshot({path:'verification-live/admin/dashboard.png'});
 report.checks.push('Actual admin password login, verified email approval and dashboard loading');
 await page.getByRole('button',{name:'Add reels',exact:true}).click();
 await page.locator('input[type=file][multiple]').setInputFiles('verification-live/admin/TEST-admin-upload.mp4');
 await page.getByLabel('Reel title').fill('TEST admin upload verification');await page.getByLabel('Chapter / topic').fill('TEST Newton laws');await page.getByLabel('Concept / short description').fill('QA test of lesson upload and mapped questions. Hidden from students.');
 await page.getByLabel(/Test content/).check();
 await page.getByRole('button',{name:'Publish reel & questions',exact:true}).click();await page.getByRole('alert').first().waitFor();assert.equal((await rpc('dashboard')).reels.filter(r=>r.title==='TEST admin upload verification').length,0);
 const prompts=['When the bus brakes, why does a passenger tend to move forward?','Which equation relates force, mass and acceleration in this lesson?'];
 const options=[['Their mass disappears','Gravity reverses','Inertia of motion','The brakes pull them forward'],['F = m/a','F = a/m','F = m + a','F = ma']];
 for(let i=0;i<2;i++){
  const q=page.locator('.admin-question').nth(i);await q.getByLabel('Question prompt').fill(prompts[i]);for(let n=0;n<4;n++)await q.getByLabel('Option '+String.fromCharCode(65+n),{exact:true}).fill(options[i][n]);await q.getByLabel('Correct answer').selectOption(String(i===0?2:3));await q.getByLabel('Explanation shown after submission').fill(i===0?'Inertia keeps the passenger moving forward when the bus slows.':'Newton’s second law is F = ma.');
 }
 await page.getByRole('button',{name:'Upload / save draft',exact:true}).click();
 await page.getByText('Draft saved. It is not visible to students yet.',{exact:true}).waitFor({timeout:120000});
 let dashboard=await rpc('dashboard');const draft=dashboard.drafts.find(d=>d.metadata.title==='TEST admin upload verification');assert.ok(draft);assert.equal(draft.status,'verified');report.uploadId=draft.id;report.reelId=draft.reel_id;
 await assert.rejects(()=>rpc('mark_verified',{p_id:draft.id,p_video_sha:draft.sha256,p_poster_sha:draft.poster_sha,p_size:draft.video_size}),/permission denied/);
 report.checks.push('Question validation prevents incomplete publishing; actual MP4/poster upload and server checksum verification persist a draft; admin cannot forge verification');
 await page.screenshot({path:'verification-live/admin/upload.png'});
 await page.reload({waitUntil:'domcontentloaded'});await page.getByRole('button',{name:/Upload reels/}).click();await page.getByRole('button',{name:/TEST admin upload verification/}).click();
 await page.getByLabel('Reel title').waitFor();assert.equal(await page.getByLabel('Reel title').inputValue(),'TEST admin upload verification');
 await page.getByRole('button',{name:'Publish reel & questions',exact:true}).click();await page.getByText('Reel published with its mapped quiz questions.',{exact:true}).waitFor({timeout:30000});
 await Promise.all([rpc('publish',{p_id:draft.id}),rpc('publish',{p_id:draft.id})]);
 dashboard=await rpc('dashboard');const reel=dashboard.reels.find(r=>r.id===draft.reel_id);assert.ok(reel);assert.equal(reel.active,false);assert.equal(reel.questionCount,2);assert.equal((await rpc('questions',{p_reel:draft.reel_id})).length,2);
 report.checks.push('Draft/authoring restore after reload; repeated publication creates one hidden test reel with two private mapped keys');
 await page.getByRole('button',{name:'Reel library',exact:true}).click();await page.getByRole('button',{name:/TEST admin upload verification/}).click();await page.getByRole('dialog',{name:'Published reel preview'}).waitFor();await page.screenshot({path:'verification-live/admin/published.png'});await page.getByRole('button',{name:'Close reel preview'}).click();
 await page.getByRole('button',{name:'Analytics',exact:true}).click();await page.getByLabel('Analytics date range').selectOption('7');await page.waitForTimeout(500);await page.setViewportSize({width:390,height:844});await page.screenshot({path:'verification-live/admin/mobile.png'});
 assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),true);
 const student=createClient(url,key,{auth:{persistSession:false,autoRefreshToken:false}}),retained=JSON.parse(fs.readFileSync('supabase/.test-sessions.json','utf8'));const restored=await student.auth.setSession(retained.sessions[0]);if(restored.error)throw Error('Retained TEST student restoration failed');
 const blocked=await student.rpc('ar_reels_admin_dashboard');assert.ok(blocked.error);const keys=await student.rpc('ar_reels_admin_questions',{p_reel:draft.reel_id});assert.ok(keys.error);
 const denied=await fetch(url+'/functions/v1/ar-reels-admin-media',{method:'POST',headers:{apikey:key,Authorization:'Bearer '+restored.data.session.access_token,'Content-Type':'application/json'},body:JSON.stringify({action:'sign',uploadId:draft.id})});assert.ok([401,403].includes(denied.status));
 const catalogue=await student.rpc('ar_reels_discovery_catalogue');if(catalogue.error)throw Error('Student catalogue failed');assert.ok(!catalogue.data.some(r=>r.id===draft.reel_id));
 report.checks.push('Student JWT cannot read analytics/answer keys or get upload tokens; hidden QA reel absent from student catalogue; mobile admin has no document overflow');assert.deepEqual(pageErrors,[]);report.status='passed';
}catch(e){report.status='failed';report.error=e.message;process.exitCode=1;await page.screenshot({path:'verification-live/admin/failure.png'}).catch(()=>{})}
finally{await browser.close();fs.writeFileSync('verification-live/admin/browser.json',JSON.stringify(report,null,2));console.log(JSON.stringify(report,null,2))}
