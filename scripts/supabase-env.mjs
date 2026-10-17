import fs from "node:fs";
import { parseEnv } from "node:util";
export function environment() {
  return {
    ...process.env,
    ...(fs.existsSync(".env") ? parseEnv(fs.readFileSync(".env", "utf8")) : {}),
  };
}
export function publicConfig(env = environment()) {
  const url = env.VITE_SUPABASE_URL || env.NEXT_PUBLIC_SUPABASE_URL;
  const key =
    env.VITE_SUPABASE_PUBLISHABLE_KEY ||
    env.NEXT_PUBLIC_SUPABASE_PUBLISHABLE_KEY;
  if (!url || !key) throw Error("Supabase URL or publishable key is missing.");
  if (key.startsWith("sb_secret_"))
    throw Error("A privileged key must not be used as browser configuration.");
  return { url, key };
}
