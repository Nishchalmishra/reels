import { spawnSync } from "node:child_process";
const [mode, action = "dev", ...args] = process.argv.slice(2);
if (
  !["local", "supabase"].includes(mode) ||
  !["dev", "build", "preview"].includes(action)
)
  throw Error(
    "Use: run-mode.mjs local|supabase dev|build|preview [Vite arguments]",
  );
const env = { ...process.env, VITE_SERVICE_MODE: mode };
if (action === "build") {
  const t = spawnSync(
    process.execPath,
    ["node_modules/typescript/bin/tsc", "-b"],
    { env, stdio: "inherit" },
  );
  if (t.status) process.exit(t.status || 1);
}
const r = spawnSync(
  process.execPath,
  [
    "node_modules/vite/bin/vite.js",
    ...(action === "dev" ? [] : [action]),
    ...(action === "build" ? [] : ["--host", "127.0.0.1"]),
    ...args,
  ],
  { env, stdio: "inherit" },
);
if (r.status !== 0) process.exit(r.status ?? 1);
if (mode === "supabase" && action === "build") {
  const i = args.indexOf("--outDir");
  const check = spawnSync(
    process.execPath,
    ["scripts/check-live-bundle.mjs", i >= 0 ? args[i + 1] : "dist"],
    { env, stdio: "inherit" },
  );
  process.exit(check.status ?? 1);
}
process.exit(0);
