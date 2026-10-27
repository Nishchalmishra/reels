// Authenticated admin-only signing and server-side verification. No privileged key is returned.
import {createClient} from 'npm:@supabase/supabase-js@2';
const cors={'Access-Control-Allow-Origin':'*','Access-Control-Allow-Headers':'authorization, apikey, content-type, x-client-info','Access-Control-Allow-Methods':'POST, OPTIONS'};
const json=(data:unknown,status=200)=>new Response(JSON.stringify(data),{status,headers:{...cors,'Content-Type':'application/json','Cache-Control':'no-store'}});
const bucket='arivihan-reels';
const sha=async(bytes:ArrayBuffer)=>Array.from(new Uint8Array(await crypto.subtle.digest('SHA-256',bytes))).map(v=>v.toString(16).padStart(2,'0')).join('');
Deno.serve(async(req:Request)=>{
 if(req.method==='OPTIONS')return new Response(null,{headers:cors});
 if(req.method!=='POST')return json({error:'Method not allowed'},405);
 const authorization=req.headers.get('Authorization')||'';
 if(!authorization.startsWith('Bearer '))return json({error:'Administrator sign-in required'},401);
 const url=Deno.env.get('SUPABASE_URL')!;
 const client=createClient(url,Deno.env.get('SUPABASE_ANON_KEY')!,{global:{headers:{Authorization:authorization}},auth:{persistSession:false,autoRefreshToken:false}});
 const {data:{user},error:authError}=await client.auth.getUser(authorization.slice(7));
 if(authError||!user||user.is_anonymous||!user.email_confirmed_at)return json({error:'Verified administrator sign-in required'},401);
 try{
  const body=await req.json();
  if(!['sign','verify'].includes(body.action)||typeof body.uploadId!=='string')return json({error:'Invalid upload request'},400);
  const {data:ticket,error:denied}=await client.rpc('ar_reels_admin_upload_ticket',{p_id:body.uploadId});
  if(denied||!ticket)return json({error:'Administrator access or draft ownership denied'},403);
  const admin=createClient(url,Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')!,{auth:{persistSession:false,autoRefreshToken:false}});
  if(body.action==='sign'){
   const {data:config,error}=await admin.storage.getBucket(bucket);if(error||!config.public)return json({error:'Configured media bucket unavailable'},503);
   if(Number(config.file_size_limit||0)<52428800){const {error}=await admin.storage.updateBucket(bucket,{public:true,fileSizeLimit:52428800,allowedMimeTypes:['video/mp4','image/jpeg']});if(error)return json({error:'Could not configure upload size limit'},503);}
   const output:Record<string,unknown>={};
   for(const [kind,path,size] of [['video',ticket.video_object,ticket.video_size],['poster',ticket.poster_object,ticket.poster_size]]){
    if(typeof path!=='string'||!path.startsWith('admin/'+ticket.id+'/'))return json({error:'Invalid stored object path'},500);
    const existing=await fetch(`${url}/storage/v1/object/public/${bucket}/${path}`,{method:'HEAD'});
    if(existing.ok){if(Number(existing.headers.get('content-length'))!==Number(size))return json({error:'An existing object does not match this draft'},409);output[kind]={exists:true};}
    else{const {data,error}=await admin.storage.from(bucket).createSignedUploadUrl(path,{upsert:false});if(error)return json({error:'Could not authorize upload'},500);output[kind]={exists:false,path,token:data.token};}
   }
   return json(output);
  }
  if(ticket.status==='verified')return json({verified:true});
  const {data:video,error:videoError}=await admin.storage.from(bucket).download(ticket.video_object);
  if(videoError||!video||video.size!==Number(ticket.video_size)||video.size>52428800)return json({error:'Video not uploaded completely'},409);
  const vb=await video.arrayBuffer(),header=new Uint8Array(vb,0,Math.min(16,vb.byteLength));
  if(String.fromCharCode(...header.slice(4,8))!=='ftyp'||await sha(vb)!==ticket.sha256)return json({error:'MP4 signature or checksum mismatch'},409);
  const {data:poster,error:posterError}=await admin.storage.from(bucket).download(ticket.poster_object);
  if(posterError||!poster||poster.size!==Number(ticket.poster_size)||poster.size>5242880)return json({error:'Poster not uploaded completely'},409);
  const pb=await poster.arrayBuffer(),ph=new Uint8Array(pb,0,Math.min(2,pb.byteLength));
  if(ph[0]!==255||ph[1]!==216||await sha(pb)!==ticket.poster_sha)return json({error:'JPEG signature or checksum mismatch'},409);
  const {error}=await admin.rpc('ar_reels_admin_mark_verified',{p_id:ticket.id,p_video_sha:ticket.sha256,p_poster_sha:ticket.poster_sha,p_size:Number(ticket.video_size)});
  return error?json({error:'Could not verify draft'},500):json({verified:true});
 }catch{return json({error:'Admin media request failed; retry or check the uploaded files'},500);}
});
