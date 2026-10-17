import fs from 'node:fs';
import {execFileSync} from 'node:child_process';
const expectedProject='pbaegeqjargtsfortsyh';
try{
 const servers=JSON.parse(execFileSync('codex',['mcp','list','--json'],{encoding:'utf8',stdio:['ignore','pipe','pipe']}));
 const server=servers.find(s=>s.name==='supabase');
 if(!server){console.error('Supabase MCP is not visible to this Codex configuration. Check the active Codex profile and restart after adding it.');process.exitCode=2;}
 else{
  const endpoint=new URL(server.transport.url);const project=endpoint.searchParams.get('project_ref');
  if(endpoint.origin!=='https://mcp.supabase.com'||project!==expectedProject)throw Error('Configured Supabase endpoint does not match the intended project.');
  const report={checkedAt:new Date().toISOString(),server:'supabase',project,enabled:server.enabled,transport:server.transport.type,authMethod:server.auth_status,toolCallVerified:false,note:'Configuration/OAuth method only. A successful project MCP tool call is still required.'};
  fs.mkdirSync('supabase/inspection',{recursive:true});fs.writeFileSync('supabase/inspection/mcp-config.json',JSON.stringify(report,null,2));
  console.log(JSON.stringify(report,null,2));
 }
}catch{console.error('Could not verify the Supabase MCP configuration. No credentials or raw configuration were printed.');process.exitCode=1;}
