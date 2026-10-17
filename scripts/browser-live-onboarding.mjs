import fs from 'node:fs';
import crypto from 'node:crypto';
import assert from 'node:assert/strict';
import {chromium} from 'playwright';
import {createClient} from '@supabase/supabase-js';
import {publicConfig} from './supabase-env.mjs';
import {retainTestSessions} from './test-sessions.mjs';

const {url,key}=publicConfig(),run=crypto.randomUUID();
const name=`TEST onboarding ${run.slice(0,8)}`;
const browser=await chromium.launch({channel:'chrome',headless:true});
const report={run,project:new URL(url).hostname.split('.')[0],status:'running',checks:[]};
try{
 const page=await browser.newPage({viewport:{width:390,height:844}});
 const errors=[];page.on('pageerror',e=>errors.push(e.message));
 // Forward the actual request unchanged except for the dedicated TEST marker.
 // Authentication, name/batch values, responses and persistence remain real.
 await page.route(url+'/rest/v1/rpc/ar_reels_profile',route=>route.continue({
   postData:JSON.stringify({...route.request().postDataJSON(),p_test:true}),
 }));
 await page.goto(process.env.REELS_BASE_URL||'http://127.0.0.1:4174/');
 await page.getByLabel('Your name').fill(name);
 await page.getByLabel('Class / batch').selectOption({index:1});
 const batch=await page.getByLabel('Class / batch').inputValue();
 await page.getByRole('button',{name:/start learning/}).click();
 await page.getByRole('heading',{name:'Physics chapters',exact:true}).waitFor();
 const storageKey=`sb-${report.project}-auth-token`;
 const session=await page.evaluate(key=>JSON.parse(localStorage.getItem(key)),storageKey);
 assert.ok(session?.access_token);
 const client=createClient(url,key,{auth:{persistSession:false,autoRefreshToken:false}});
 const auth=await client.auth.setSession(session);if(auth.error)throw auth.error;
 report.userId=auth.data.user.id;
 retainTestSessions(run,[report.userId],[auth.data.session]);
 const profile=await client.from('ar_reels_profiles').select('id,name,batch,is_test').single();
 if(profile.error)throw profile.error;
 assert.equal(profile.data.name,name);assert.equal(profile.data.batch,batch);assert.equal(profile.data.is_test,true);
 await page.reload();
 await page.getByRole('heading',{name:'Physics chapters',exact:true}).waitFor();
 const {data,error}=await client.rpc('ar_reels_snapshot');if(error)throw error;
 assert.equal(data.profile.id,report.userId);assert.equal(data.profile.name,name);assert.equal(data.profile.batch,batch);
 assert.equal(data.ledger.length,0);assert.deepEqual(errors,[]);
 report.status='passed';report.checks=['Actual production onboarding form creates anonymous Auth session','Entered name and batch match hosted profile rows','Profile and session survive reload without mock state','Dedicated profile marked TEST; no coins seeded'];
 console.log('PASS actual live onboarding form, anonymous Auth, hosted profile and reload persistence.');
}catch(e){report.status='failed';report.code=e.code||e.name;process.exitCode=1;console.error('Live onboarding failed:',report.code,'— secret values withheld.');}
finally{
 await browser.close();fs.mkdirSync('verification-live/runs',{recursive:true});
 fs.writeFileSync('verification-live/onboarding.json',JSON.stringify(report,null,2));
 fs.writeFileSync(`verification-live/runs/${run}.json`,JSON.stringify(report,null,2));
}
