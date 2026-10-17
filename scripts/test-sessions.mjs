import fs from 'node:fs';
export function retainTestSessions(run,users,sessions){
 const payload=JSON.stringify({run,users,sessions},null,2);
 fs.mkdirSync('supabase/.test-sessions',{recursive:true});
 fs.writeFileSync(`supabase/.test-sessions/${run}.json`,payload,{mode:0o600});
 fs.writeFileSync('supabase/.test-sessions.json',payload,{mode:0o600});
}
