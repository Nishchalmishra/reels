# Official Supabase MCP connection

The official server is configured for **pbaegeqjargtsfortsyh**, OAuth authenticated, and verified on 17 October 2026 by successful real `get_project_url`, `execute_sql`, `apply_migration` and `get_advisors` calls. No authorization token was copied into the workspace.

The running chat's native tool catalogue did not reload the new server. Workspace scripts use the official `codex app-server --stdio` MCP methods with Codex-managed OAuth instead. They start no model turn, extract no credentials, verify the project URL, and invoke actual Supabase tools. This experimental app-server protocol may change with future Codex versions.

```powershell
npm run mcp:verify
npm run mcp:inspect
npm run db:migrate:mcp
node scripts/supabase-mcp.mjs --audit-project
```

`mcp:verify` writes `supabase/inspection/mcp-verified.json` only after a successful tool call. The older `scripts/check-supabase-mcp.mjs` checks configuration only. Private schema inspection files are ignored by git and denied by the live server.

The following setup already completed. If authorization expires, run login again in the same Windows/Codex account:

```powershell
codex mcp add supabase --url 'https://mcp.supabase.com/mcp?project_ref=pbaegeqjargtsfortsyh&features=docs%2Caccount%2Cdatabase%2Cdebugging%2Cdevelopment%2Cfunctions%2Cbranching'
codex mcp login supabase
codex mcp list
```

Quote the URL in PowerShell because it contains `&`. `/mcp` runs inside the Codex terminal UI. To expose native tools in a fresh IDE session, restart the Codex extension/session after configuration. No restart or plugin installation is necessary for the verified script route. See [official Codex MCP documentation](https://developers.openai.com/codex/mcp).

Anonymous Auth was enabled by the owner in the dashboard and verified with real sessions. No manual connection or Auth step remains outstanding. Optional Supabase agent skills were not installed.
