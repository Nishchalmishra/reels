import {beforeAll,afterAll,it,expect} from 'vitest';
import {PGlite} from '@electric-sql/pglite';
import {buildMigrationBatch} from '../../scripts/migration-batch.mjs';
let db:PGlite;const admin=crypto.randomUUID(),other=crypto.randomUUID(),student=crypto.randomUUID(),testStudent=crypto.randomUUID();
const setUser=async(id:string,anonymous=false)=>db.exec(`reset role;set role authenticated;set request.jwt.claim.sub='${id}';set request.jwt.claims='{"is_anonymous":${anonymous}}'`);
const owner=()=>db.exec('reset role');
const rpc=async(name:string,args:unknown[]=[])=>{const r=await db.query<{v:any}>(`select public.ar_reels_${name}(${args.map((_,i)=>'$'+(i+1)).join(',')}) v`,args);return r.rows[0].v};
const metadata={title:'Verified new lesson',subject:'Physics',topic:'Momentum',concept:'Conservation of momentum',grades:[11],languages:['English'],isTest:false};
const questions=[{prompt:'Which quantity is conserved in an isolated system?',options:['Speed','Volume','Momentum','Temperature'],correct:2,explanation:'The total momentum is conserved without external force.'},{prompt:'Which change can create a net change in momentum?',options:['Changing colour','Watching again','Changing the title','An external force'],correct:3,explanation:'A net external force changes the total momentum.'}];
let upload:any;
beforeAll(async()=>{db=new PGlite();await db.exec(`create role anon nologin;create role authenticated nologin;create role service_role nologin;create schema auth;create table auth.users(id uuid primary key,email text,email_confirmed_at timestamptz);create function auth.uid() returns uuid language sql stable as $$ select nullif(current_setting('request.jwt.claim.sub',true),'')::uuid $$;grant usage on schema auth to anon,authenticated;grant execute on function auth.uid() to anon,authenticated;`);await db.exec(buildMigrationBatch().sql);await db.exec(buildMigrationBatch().sql);
 for(const [id,email] of [[admin,'admin@example.invalid'],[other,'other@example.invalid'],[student,'student@example.invalid'],[testStudent,'test@example.invalid']])await db.query('insert into auth.users(id,email,email_confirmed_at) values($1,$2,now())',[id,email]);
 await db.exec(`insert into ar_reels_private.admin_emails(email) values('admin@example.invalid'),('other@example.invalid')`);
 await setUser(student);await rpc('profile',['Embedded student','11','English',false]);await setUser(testStudent);await rpc('profile',['TEST analytics','11','English',true]);
},60000);
afterAll(async()=>db?.close());
it('denies student/anonymous admin access and direct private reads or verification',async()=>{
 await setUser(student);await expect(rpc('admin_dashboard')).rejects.toThrow(/Administrator/);await expect(rpc('admin_questions',['lesson'])).rejects.toThrow(/Administrator/);
 await expect(db.query('select * from ar_reels_private.admin_emails')).rejects.toThrow(/permission denied/);
 await setUser(admin,true);await expect(rpc('admin_access')).rejects.toThrow(/Verified/);
 await setUser(admin);expect((await rpc('admin_access')).authorized).toBe(true);
 await expect(rpc('admin_mark_verified',[crypto.randomUUID(),'a'.repeat(64),'b'.repeat(64),10])).rejects.toThrow(/permission denied/);
 await owner();await db.query('update auth.users set email_confirmed_at=null where id=$1',[admin]);await setUser(admin);await expect(rpc('admin_access')).rejects.toThrow(/Administrator/);await owner();await db.query('update auth.users set email_confirmed_at=now() where id=$1',[admin]);
});
it('persists owned drafts, validates publishing, and publishes immutable questions atomically',async()=>{
 await setUser(admin);const args=[crypto.randomUUID(),'a'.repeat(64),100,'b'.repeat(64),10,60,metadata,questions];upload=await rpc('admin_create_upload',args);expect((await rpc('admin_create_upload',args)).id).toBe(upload.id);
 await expect(rpc('admin_publish',[upload.id])).rejects.toThrow(/verify/);
 await setUser(other);await expect(rpc('admin_upload_ticket',[upload.id])).rejects.toThrow(/not found/);await expect(rpc('admin_save',[upload.id,metadata,questions])).rejects.toThrow(/not found/);
 await owner();await rpc('admin_mark_verified',[upload.id,'a'.repeat(64),'b'.repeat(64),100]);await setUser(admin);
 await rpc('admin_save',[upload.id,metadata,[questions[0]]]);await expect(rpc('admin_publish',[upload.id])).rejects.toThrow(/2 to 10/);
 await rpc('admin_save',[upload.id,metadata,questions]);const content=await rpc('admin_publish',[upload.id]);expect(content.source).toMatch(/^https:.*admin\//);expect((await rpc('admin_publish',[upload.id])).id).toBe(content.id);
 expect((await rpc('admin_questions',[upload.reel_id])).map((q:any)=>q.correct)).toEqual([2,3]);await expect(rpc('admin_save',[upload.id,metadata,questions])).rejects.toThrow(/not found/);
 await setUser(student);const cat=await rpc('discovery_catalogue');expect(cat.some((r:any)=>r.id===upload.reel_id)).toBe(true);const qs=await db.query('select * from public.ar_reels_discovery_questions where reel_id=$1',[upload.reel_id]);expect(qs.rows).toHaveLength(2);expect(qs.rows[0]).not.toHaveProperty('correct');await expect(rpc('admin_questions',[upload.reel_id])).rejects.toThrow(/Administrator/);
});
it('counts >=3-second views once per session, rejects forged playback, and excludes test traffic',async()=>{
 const sid=crypto.randomUUID();await setUser(student);
 const playback=(session:string,sequence:number,from:number,to:number,visible=true)=>rpc('discovery_playback',[session,sequence,upload.reel_id,from,to,1,1,visible,true]);
 await playback(sid,1,0,1);await playback(sid,1,0,1);await playback(sid,2,1,2);await playback(sid,3,2,3);await playback(sid,4,3,60);await playback(sid,5,3,4,false);
 await setUser(testStudent);await expect(playback(sid,6,3,4)).rejects.toThrow(/ownership/);const ts=crypto.randomUUID();await playback(ts,1,0,1);await playback(ts,2,1,2);await playback(ts,3,2,3);
 await setUser(admin);let d=await rpc('admin_dashboard',[7,false]);expect(d.views).toBe(1);expect(d.uniqueViewers).toBe(1);expect(d.watchSeconds).toBe(3);expect(d.daily).toHaveLength(7);expect(d.daily.reduce((n:number,v:any)=>n+v.views,0)).toBe(1);
 d=await rpc('admin_dashboard',[7,true]);expect(d.views).toBe(2);expect(d.watchSeconds).toBe(6);await expect(rpc('admin_dashboard',[999,false])).rejects.toThrow(/1 to 90/);
});
it('new mapped questions join qualifying quizzes and keep exact one-time five-coin rewards',async()=>{
 await setUser(student);const media=(await rpc('discovery_catalogue'));const chosen=[media.find((m:any)=>m.id===upload.reel_id),...media.filter((m:any)=>m.id!==upload.reel_id).slice(0,3)];
 for(const m of chosen)for(let t=0;t<Math.ceil(m.duration*.8);t++)await rpc('discovery_progress',[m.id,t,Math.min(t+1,m.duration),1,1,true,true]);
 const offer=await rpc('discovery_offer');expect(offer.ready).toBe(true);let a=await rpc('discovery_start',[offer.id]);expect(a.questions.some((q:any)=>q.reelId===upload.reel_id)).toBe(true);
 for(let i=0;i<5;i++){const q=a.questions[i],selected=q.reelId===upload.reel_id?(q.id.endsWith('01')?2:3):(q.id.endsWith('-q1')?0:1);a=await rpc('discovery_submit',[a.id,i,selected]);}
 a=await rpc('discovery_finish',[a.id]);expect(a.score).toBe(5);expect(a.rewarded).toBe(true);await rpc('discovery_finish',[a.id]);const snapshot=await rpc('snapshot');expect(snapshot.ledger).toHaveLength(1);expect(snapshot.ledger[0].amount).toBe(5);
},30000);
