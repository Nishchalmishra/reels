// Direct official Codex app-server MCP calls. No model turn is started; OAuth stays in Codex.
import fs from 'node:fs';
import {spawn} from 'node:child_process';
import readline from 'node:readline';
import {buildMigrationBatch,auditMigrationSql} from './migration-batch.mjs';
import {mcpRows} from './codex-supabase-client.mjs';
const proc=spawn('codex',['app-server','--stdio'],{stdio:['pipe','pipe','pipe'],windowsHide:true});
const pending=new Map();let next=0;let stderr='';proc.stderr.on('data',d=>{stderr+=d.toString();});
proc.on('error',()=>{for(const p of pending.values())p.reject(Error('Could not start official Codex app server.'));});
proc.on('exit',()=>{for(const p of pending.values())p.reject(Error('Codex app server exited before completing the request.'));});
readline.createInterface({input:proc.stdout}).on('line',line=>{let msg;try{msg=JSON.parse(line);}catch{return;}
 if(msg.id!=null&&pending.has(msg.id)){const p=pending.get(msg.id);pending.delete(msg.id);clearTimeout(p.timer);if(msg.error)p.reject(Error(msg.error.message));else p.resolve(msg.result);}
 else if(msg.id!=null){proc.stdin.write(JSON.stringify({id:msg.id,error:{code:-32601,message:'Interactive approval is required in the Codex UI; this read-only diagnostic does not approve requests.'}})+'\n');}
});
const request=(method,params)=>new Promise((resolve,reject)=>{const id=++next,timer=setTimeout(()=>{pending.delete(id);reject(Error('Timed out waiting for '+method));},60000);pending.set(id,{resolve,reject,timer});proc.stdin.write(JSON.stringify({id,method,params})+'\n');});
try{
 await request('initialize',{clientInfo:{name:'arivihan_mcp_verification',title:'Arivihan Supabase MCP verification',version:'1.0.0'},capabilities:{experimentalApi:true,requestAttestation:false}});
 proc.stdin.write(JSON.stringify({method:'initialized'})+'\n');
 const {thread}=await request('thread/start',{cwd:process.cwd(),ephemeral:true});
 const status=await request('mcpServerStatus/list',{threadId:thread.id,serverName:'supabase',detail:'full'});
 const server=status.data.find(s=>s.name==='supabase');if(!server)throw Error('Supabase is not loaded by the official Codex runtime.');
 fs.mkdirSync('supabase/inspection',{recursive:true});
 const safe={at:new Date().toISOString(),server:server.name,origin:server.httpOrigin,runtimeStatus:server.runtimeStatus,authStatus:server.authStatus,toolsError:server.toolsError,tools:server.tools};
 fs.writeFileSync('supabase/inspection/mcp-tools.json',JSON.stringify(safe,null,2));
 console.log('Official runtime:',server.name,'tools:',Object.keys(server.tools||{}).join(', '));
 if(!Object.keys(server.tools||{}).length)throw Error(server.toolsError||'No Supabase tools were discovered.');
 const tool=Object.keys(server.tools).find(t=>/get_project_url$/.test(t))||Object.keys(server.tools).find(t=>/list_tables$/.test(t));
 if(!tool)throw Error('No supported read-only project verification tool is available.');
 const args=tool.endsWith('list_tables')?{schemas:['public']}:{};
 const result=await request('mcpServer/tool/call',{threadId:thread.id,server:'supabase',tool,arguments:{...(server.tools[tool].inputSchema?.properties?.project_id?{project_id:'pbaegeqjargtsfortsyh'}:{}),...args}});
 if(result.isError)throw Error('The actual Supabase MCP tool returned an error.');
 const contents=JSON.stringify(result);if(tool.endsWith('get_project_url')&&!contents.includes('pbaegeqjargtsfortsyh.supabase.co'))throw Error('MCP project URL does not match the intended project.');
 const report={at:new Date().toISOString(),project:'pbaegeqjargtsfortsyh',server:'supabase',tool,toolCallVerified:true,result};
 fs.writeFileSync('supabase/inspection/mcp-verified.json',JSON.stringify(report,null,2));
 console.log('PASS actual authenticated Supabase MCP tool call:',tool,'for project pbaegeqjargtsfortsyh.');
 const call=async(tool,args)=>{const result=await request('mcpServer/tool/call',{threadId:thread.id,server:'supabase',tool,arguments:{...(server.tools[tool].inputSchema?.properties?.project_id?{project_id:'pbaegeqjargtsfortsyh'}:{}),...args}});if(result.isError)throw Error('Supabase MCP '+tool+' returned an error.');return result;};
 if(process.argv.includes('--inspect')){
  const query=`select jsonb_build_object('columns',(select jsonb_agg(to_jsonb(c)) from information_schema.columns c where table_schema='public'), 'policies',(select jsonb_agg(to_jsonb(p)) from pg_policies p where schemaname='public'), 'functions',(select jsonb_agg(jsonb_build_object('name',p.proname,'arguments',pg_get_function_identity_arguments(p.oid),'security_definer',p.prosecdef,'definition',pg_get_functiondef(p.oid))) from pg_proc p join pg_namespace n on n.oid=p.pronamespace where n.nspname='public' and p.prokind='f'), 'tables',(select jsonb_agg(jsonb_build_object('name',c.relname,'rls',c.relrowsecurity)) from pg_class c join pg_namespace n on n.oid=c.relnamespace where n.nspname='public' and c.relkind='r'), 'buckets',(select jsonb_agg(jsonb_build_object('id',id,'public',public,'limit',file_size_limit)) from storage.buckets)) inspection`;
  const inspection=await call('execute_sql',{query});fs.writeFileSync('supabase/inspection/mcp-database-private.json',JSON.stringify(inspection,null,2));console.log('Actual database columns, policies, function definitions, RLS and bucket metadata saved privately; no row data or credentials printed.');
 }
 if(process.argv.includes('--inventory')){
  const query=`select jsonb_build_object('counts',jsonb_build_object('profiles',(select count(*) from public.profiles),'reels',(select count(*) from public.reels),'questions',(select count(*) from public.questions),'attempts',(select count(*) from public.quiz_attempts),'ledger',(select count(*) from public.coin_ledger)), 'media',(select jsonb_agg(jsonb_build_object('id',id,'title',title,'chapter',chapter,'subject',subject,'type',type,'urlHost',split_part(split_part(video_url,'://',2),'/',1))) from public.reels), 'auth_triggers',(select jsonb_agg(jsonb_build_object('name',t.tgname,'function',p.proname)) from pg_trigger t join pg_class c on c.oid=t.tgrelid join pg_namespace n on n.oid=c.relnamespace join pg_proc p on p.oid=t.tgfoid where n.nspname='auth' and c.relname='users' and not t.tgisinternal), 'new_private_schema_exists',to_regnamespace('ar_reels_private') is not null) inventory`;
  const inventory=await call('execute_sql',{query});fs.writeFileSync('supabase/inspection/mcp-inventory-private.json',JSON.stringify(inventory,null,2));console.log('Saved actual historical row counts and authorized media metadata privately.');
 }
 if(process.argv.includes('--audit-project')){
  const advisors=await call('get_advisors',{type:'security'});fs.writeFileSync('supabase/inspection/mcp-security-advisors.json',JSON.stringify(advisors,null,2));
  const query=`select jsonb_build_object('migrations',(select jsonb_agg(version order by version) from public.ar_reels_migrations),'chapters',(select count(*) from public.ar_reels_chapters),'lessons',(select count(*) from public.ar_reels_media),'questions',(select count(*) from public.ar_reels_questions),'challenge_mappings',(select count(*) from public.ar_reels_challenge_items),'products',(select count(*) from public.ar_reels_products),'test_profiles',(select count(*) from public.ar_reels_profiles where is_test),'normal_profiles',(select count(*) from public.ar_reels_profiles where not is_test),'historical',jsonb_build_object('profiles',(select count(*) from public.profiles),'reels',(select count(*) from public.reels),'questions',(select count(*) from public.questions),'attempts',(select count(*) from public.quiz_attempts),'ledger',(select count(*) from public.coin_ledger))) deployment`;
  const verification=await call('execute_sql',{query});fs.writeFileSync('supabase/inspection/mcp-deployed-records.json',JSON.stringify(mcpRows(verification),null,2));console.log('Saved actual deployed records/versions and security advisor results.');
 }
 if(process.argv.includes('--apply-migrations')){
  if(!fs.existsSync('supabase/inspection/mcp-database-private.json'))throw Error('Inspect and review the actual schema before applying migrations.');
  const {sql,files}=buildMigrationBatch();await call('apply_migration',{name:'arivihan_reels_'+files.at(-1).replace(/\.sql$/,''),query:sql});
  const audit=await call('execute_sql',{query:auditMigrationSql});const row=mcpRows(audit)[0];if(row.unprotected_tables!==0||row.private_schema_access||row.answer_key_access||row.direct_reward_insert||row.direct_progress_update||row.anonymous_finish||!row.authenticated_finish)throw Error('Actual deployed access audit failed.');fs.writeFileSync('supabase/inspection/mcp-deployment-audit.json',JSON.stringify(audit,null,2));console.log('Supabase applied the versioned migration batch; actual post-deployment access audit passed.');
 }
}catch(e){console.error('MCP verification failed:',e.message);process.exitCode=1;}
finally{for(const p of pending.values())clearTimeout(p.timer);pending.clear();proc.stdin.end();proc.kill();}
