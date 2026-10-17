// Official Codex app-server transport only. No model turn, agent delegation or credential extraction.
import {spawn} from 'node:child_process';
import readline from 'node:readline';
export function mcpRows(result){
 let text=result.content?.filter(x=>x.type==='text').map(x=>x.text).join('\n')||'';
 try{const value=JSON.parse(text);if(Array.isArray(value))return value;text=typeof value.result==='string'?value.result:text;}catch{}
 const begin=text.indexOf('\n['),end=text.indexOf('\n</',begin);
 if(begin<0||end<0)throw Error('Expected a structured SQL-row result from Supabase MCP.');
 return JSON.parse(text.slice(begin+1,end).trim());
}
export async function connectSupabaseMcp(){
 const proc=spawn('codex',['app-server','--stdio'],{stdio:['pipe','pipe','pipe'],windowsHide:true});
 const pending=new Map();let next=0;proc.stderr.resume();
 const fail=()=>{for(const p of pending.values()){clearTimeout(p.timer);p.reject(Error('Official Codex MCP process stopped.'));}pending.clear();};proc.on('error',fail);proc.on('exit',fail);
 readline.createInterface({input:proc.stdout}).on('line',line=>{let msg;try{msg=JSON.parse(line);}catch{return;}if(msg.id!=null&&pending.has(msg.id)){const p=pending.get(msg.id);pending.delete(msg.id);clearTimeout(p.timer);msg.error?p.reject(Error(msg.error.message)):p.resolve(msg.result);}else if(msg.id!=null)proc.stdin.write(JSON.stringify({id:msg.id,error:{code:-32601,message:'Interactive approval must be completed in Codex.'}})+'\n');});
 const request=(method,params)=>new Promise((resolve,reject)=>{const id=++next,timer=setTimeout(()=>{pending.delete(id);reject(Error('MCP request timed out: '+method));},60000);pending.set(id,{resolve,reject,timer});proc.stdin.write(JSON.stringify({id,method,params})+'\n');});
 const close=async()=>{for(const p of pending.values())clearTimeout(p.timer);pending.clear();proc.stdin.end();proc.kill();};
 try{
  await request('initialize',{clientInfo:{name:'arivihan_supabase_integration',title:'Arivihan verified Supabase integration',version:'1.0.0'},capabilities:{experimentalApi:true,requestAttestation:false}});proc.stdin.write(JSON.stringify({method:'initialized'})+'\n');
  const {thread}=await request('thread/start',{cwd:process.cwd(),ephemeral:true});
  const status=await request('mcpServerStatus/list',{threadId:thread.id,serverName:'supabase',detail:'full'});const server=status.data.find(s=>s.name==='supabase');if(!server?.tools?.get_project_url)throw Error('Official Supabase MCP tools are unavailable.');
  const call=async(tool,args={})=>{if(!server.tools[tool])throw Error('Supabase tool unavailable: '+tool);const result=await request('mcpServer/tool/call',{threadId:thread.id,server:'supabase',tool,arguments:args});if(result.isError)throw Error('Supabase MCP tool failed: '+tool);return result;};
  const project=await call('get_project_url');if(!JSON.stringify(project).includes('pbaegeqjargtsfortsyh.supabase.co'))throw Error('MCP is connected to a different project.');
  const query=async(sql,values=[])=>{const literal=value=>value==null?'null':typeof value==='number'?(Number.isFinite(value)?String(value):(()=>{throw Error('Invalid SQL number');})()):"'"+String(value).replaceAll("'","''")+"'";const query=sql.replace(/\$(\d+)/g,(_,i)=>literal(values[Number(i)-1]));return {rows:mcpRows(await call('execute_sql',{query}))};};
  return {call,query,close};
 }catch(e){await close();throw e;}
}
