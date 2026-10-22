// One-time, scoped media uploader. Credentials stay in an ignored server/tooling file.
import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';
import {createClient} from '@supabase/supabase-js';
import {publicConfig} from './supabase-env.mjs';
const {url,key}=publicConfig();
if(new URL(url).hostname!=='pbaegeqjargtsfortsyh.supabase.co')throw Error('Unexpected project');
const bucket='arivihan-reels',broker='ar-reels-media-upload-20261022';
const privatePath='supabase/.test-sessions/media-upload-private.json';
const reportPath='supabase/inspection/media-upload.json';
const digest=b=>crypto.createHash('sha256').update(b).digest('hex');
const manifest=JSON.parse(fs.readFileSync('reference-frames/discovery/manifest.json','utf8'));
const phase=process.argv[2];
const assets=manifest.flatMap(l=>{
 const file=path.join('videos',l.file),bytes=fs.readFileSync(file),sha=digest(bytes);
 if(sha!==l.sha256)throw Error('Source checksum mismatch: '+l.id);
 const poster=`public/posters/${l.id}.jpg`,posterBytes=fs.readFileSync(poster),posterSha=digest(posterBytes);
 return [{id:l.id,kind:'video',file,sha256:sha,size:bytes.length,type:'video/mp4',object:`lessons/${l.id}/v1/${sha}.mp4`},{id:l.id,kind:'poster',file:poster,sha256:posterSha,size:posterBytes.length,type:'image/jpeg',object:`lessons/${l.id}/v1/poster-${posterSha}.jpg`}];
});
const publicUrl=object=>`${url}/storage/v1/object/public/${bucket}/${object}`;
if(phase==='prepare'){
 if(fs.existsSync(privatePath))throw Error('Uploader already prepared; reuse or explicitly complete existing job');
 const client=createClient(url,key,{auth:{persistSession:false,autoRefreshToken:false}});
 const {data,error}=await client.auth.signInAnonymously({options:{data:{integration_test:true,media_uploader:true}}});if(error)throw Error('Cannot create isolated uploader identity: '+error.message);
 const job={bucket,broker,userId:data.user.id,session:data.session,expires:new Date(Date.now()+90*60000).toISOString(),assets};
 fs.mkdirSync(path.dirname(privatePath),{recursive:true});fs.writeFileSync(privatePath,JSON.stringify(job),{mode:0o600});
 const template=fs.readFileSync('scripts/media-upload-broker.template.txt','utf8');
 const code=template.replace('__ALLOWED_USER__',JSON.stringify(job.userId)).replace('__EXPIRES__',JSON.stringify(job.expires)).replace('__BUCKET__',JSON.stringify(bucket)).replace('__ASSETS__',JSON.stringify(assets.map(({object,size,type})=>({object,size,type}))));
 fs.mkdirSync('supabase/inspection',{recursive:true});fs.writeFileSync('supabase/inspection/media-upload-broker-private.ts',code);
 fs.writeFileSync(reportPath,JSON.stringify({project:'pbaegeqjargtsfortsyh',bucket,broker,uploaderId:job.userId,expires:job.expires,phase:'prepared',videos:10,posters:10,bytes:assets.reduce((s,a)=>s+a.size,0)},null,2));
 console.log('Prepared 10 distinct videos and 10 posters; isolated uploader session stored privately.');
}else{
 const job=JSON.parse(fs.readFileSync(privatePath,'utf8'));
 const invoke=async body=>{const r=await fetch(`${url}/functions/v1/${broker}`,{method:'POST',headers:{apikey:key,Authorization:`Bearer ${job.session.access_token}`,'Content-Type':'application/json'},body:JSON.stringify(body)});const data=await r.json();if(!r.ok)throw Error('Scoped upload broker: '+r.status+' '+(data.error||'request failed'));return data;};
 if(phase==='upload'){
  const client=createClient(url,key,{auth:{persistSession:false,autoRefreshToken:false}});
  const results=[];
  for(const asset of assets){
   const destination=publicUrl(asset.object);const existing=await fetch(destination,{method:'HEAD'});
   if(!existing.ok){
    const signed=await invoke({action:'sign',object:asset.object});
    const {error}=await client.storage.from(bucket).uploadToSignedUrl(asset.object,signed.token,fs.readFileSync(asset.file),{contentType:asset.type,cacheControl:'31536000',upsert:false});
    if(error)throw Error(`Upload failed for ${asset.id} ${asset.kind}: ${error.message}`);
   }
   console.log(`Uploaded/available ${asset.kind}: ${asset.id}`);results.push({...asset,url:destination});
  }
  const report=JSON.parse(fs.readFileSync(reportPath,'utf8'));fs.writeFileSync(reportPath,JSON.stringify({...report,phase:'uploaded',assets:results},null,2));
 }else if(phase==='verify'){
  const results=[];
  for(const asset of assets){
   const r=await fetch(publicUrl(asset.object));if(!r.ok)throw Error('Public media unavailable: '+asset.id);
   const bytes=Buffer.from(await r.arrayBuffer());if(bytes.length!==asset.size||digest(bytes)!==asset.sha256)throw Error('Hosted checksum mismatch: '+asset.id);
   const ranged=await fetch(publicUrl(asset.object),{headers:{Range:'bytes=0-1023'}});const part=Buffer.from(await ranged.arrayBuffer());
   if(ranged.status!==206||part.length!==Math.min(1024,asset.size))throw Error('Byte-range playback verification failed: '+asset.id);
   results.push({id:asset.id,kind:asset.kind,checksum:true,range:true,url:publicUrl(asset.object)});console.log('Verified hosted checksum and byte range:',asset.kind,asset.id);
  }
  const report=JSON.parse(fs.readFileSync(reportPath,'utf8'));fs.writeFileSync(reportPath,JSON.stringify({...report,phase:'verified',verified:results,verifiedAt:new Date().toISOString()},null,2));
  const sql=[`-- Hosted delivery only; content IDs/versions, quiz mappings, progress and rewards remain unchanged.`];
  for(const l of manifest){const video=assets.find(a=>a.id===l.id&&a.kind==='video'),poster=assets.find(a=>a.id===l.id&&a.kind==='poster');const q=s=>"'"+s.replaceAll("'","''")+"'";
   sql.push(`update public.ar_reels_discovery_media set content=content || jsonb_build_object('source',${q(publicUrl(video.object))},'poster',${q(publicUrl(poster.object))},'storageBucket',${q(bucket)},'storageObject',${q(video.object)}) where id=${q(l.id)} and content->>'sha256'=${q(l.sha256)} and coalesce((content->>'contentVersion')::integer,1)=1;`);
  }
  sql.push(`do $$ begin if (select count(*) from public.ar_reels_discovery_media where content->>'storageBucket'='${bucket}' and content->>'source' like '${url}/storage/v1/object/public/${bucket}/%')<>10 then raise exception 'Hosted lesson catalogue verification failed';end if;end $$;`);
  fs.writeFileSync('supabase/migrations/202610220008_hosted_media.sql',sql.join('\n')+'\n');
 }else if(phase==='cleanup'){
  await invoke({action:'finish'});fs.unlinkSync(privatePath);
  const report=JSON.parse(fs.readFileSync(reportPath,'utf8'));fs.writeFileSync(reportPath,JSON.stringify({...report,uploaderDeleted:true},null,2));console.log('Removed the isolated uploader identity and local upload session.');
 }else throw Error('Use prepare, upload, verify or cleanup');
}
