import fs from "node:fs";
import { parseEnv } from "node:util";
const env = { ...process.env, ...parseEnv(fs.readFileSync(".env", "utf8")) };
const url = env.VITE_SUPABASE_URL || env.NEXT_PUBLIC_SUPABASE_URL;
const key =
  env.VITE_SUPABASE_PUBLISHABLE_KEY || env.NEXT_PUBLIC_SUPABASE_PUBLISHABLE_KEY;
if (!url || !key) throw Error("Missing browser-safe project configuration");
console.log("Project:", new URL(url).hostname.split(".")[0]);
console.log(
  "Credential kind:",
  key.startsWith("sb_publishable_") ? "publishable" : "legacy browser key",
);
const headers = { apikey: key };
const report = {
  project: new URL(url).hostname.split(".")[0],
  checkedAt: new Date().toISOString(),
  endpoints: {},
};
for (const [label, path] of [
  ["auth-settings", "/auth/v1/settings"],
  ["catalogue-schema", "/rest/v1/"],
  ["storage-buckets", "/storage/v1/bucket"],
]) {
  const r = await fetch(url + path, { headers });
  let body;
  try {
    body = await r.json();
  } catch {
    body = { message: "Non-JSON response" };
  }
  if (label === "catalogue-schema" && r.ok) {
    fs.mkdirSync("supabase/inspection", { recursive: true });
    fs.writeFileSync(
      "supabase/inspection/openapi.json",
      JSON.stringify(body, null, 2),
    );
    body = {
      tables: Object.keys(body.definitions || {}),
      paths: Object.keys(body.paths || {}),
    };
  }
  if (label === "auth-settings" && r.ok)
    body = {
      anonymous_users_enabled: body.external?.anonymous_users,
      disable_signup: body.disable_signup,
      external: body.external,
    };
  report.endpoints[label] = { status: r.status, body };
  console.log(label, JSON.stringify(report.endpoints[label]));
}
for (const table of [
  "profiles",
  "reels",
  "questions",
  "reel_events",
  "quiz_attempts",
  "coin_ledger",
  "leaderboard",
  "ar_reels_profiles",
  "ar_reels_chapters",
]) {
  const r = await fetch(`${url}/rest/v1/${table}?select=*&limit=0`, {
    headers: { ...headers, Prefer: "count=exact" },
  });
  const body = await r.json();
  const result = {
    status: r.status,
    count: r.headers.get("content-range"),
    ...(r.ok ? {} : { code: body.code, message: body.message }),
  };
  report.endpoints[table] = result;
  console.log("table", table, JSON.stringify(result));
}
// Auth settings above are read-only. User creation belongs only to the dedicated verification runner.
fs.mkdirSync("supabase/inspection", { recursive: true });
fs.writeFileSync(
  "supabase/inspection/report.json",
  JSON.stringify(report, null, 2),
);
