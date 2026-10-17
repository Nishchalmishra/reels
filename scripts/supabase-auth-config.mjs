import { environment, publicConfig } from "./supabase-env.mjs";
const env = environment(),
  { url } = publicConfig(env),
  project = new URL(url).hostname.split(".")[0];
if (!env.SUPABASE_ACCESS_TOKEN) {
  console.error(
    "BLOCKED: SUPABASE_ACCESS_TOKEN with auth_config_read/auth_config_write is unavailable.",
  );
  process.exit(2);
}
const endpoint = `https://api.supabase.com/v1/projects/${project}/config/auth`;
const headers = {
  Authorization: `Bearer ${env.SUPABASE_ACCESS_TOKEN}`,
  "Content-Type": "application/json",
};
const r = await fetch(endpoint, { headers });
if (!r.ok) {
  console.error("Auth configuration inspection failed, HTTP", r.status);
  process.exit(1);
}
const config = await r.json();
console.log(
  "Anonymous sign-in:",
  !!config.external_anonymous_users_enabled,
  "Email provider:",
  !!config.external_email_enabled,
);
if (
  process.argv.includes("--enable-anonymous") &&
  !config.external_anonymous_users_enabled
) {
  const update = await fetch(endpoint, {
    method: "PATCH",
    headers,
    body: JSON.stringify({ external_anonymous_users_enabled: true }),
  });
  if (!update.ok) {
    console.error("Enable anonymous sign-in failed, HTTP", update.status);
    process.exit(1);
  }
  const verify = await fetch(endpoint, { headers });
  if (!verify.ok || !(await verify.json()).external_anonymous_users_enabled)
    throw Error("Auth setting did not persist.");
  console.log(
    "Verified anonymous sign-in enabled. Other Auth settings preserved.",
  );
}
