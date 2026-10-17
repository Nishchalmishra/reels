import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  if (
    Object.keys(env).some((name) =>
      /^VITE_.*(SECRET|SERVICE_ROLE|ACCESS_TOKEN|DB_URL|DATABASE_URL|PASSWORD)/i.test(
        name,
      ),
    )
  )
    throw Error("Privileged credentials must not use a VITE_ prefix.");
  const url = env.VITE_SUPABASE_URL || env.NEXT_PUBLIC_SUPABASE_URL || "";
  const key =
    env.VITE_SUPABASE_PUBLISHABLE_KEY ||
    env.NEXT_PUBLIC_SUPABASE_PUBLISHABLE_KEY ||
    "";
  if (key) {
    let safe = key.startsWith("sb_publishable_");
    if (!safe && key.split(".").length === 3) {
      try {
        safe =
          JSON.parse(Buffer.from(key.split(".")[1], "base64url").toString())
            .role === "anon";
      } catch {}
    }
    if (!safe)
      throw Error(
        "Refusing to expose a non-publishable Supabase credential to Vite.",
      );
  }
  return {
    server: {
      fs: {
        deny: [
          ".env",
          ".env.*",
          "*.{crt,pem}",
          "**/.git/**",
          "**/supabase/**",
          "**/scripts/**",
          "**/*.test.*",
          ...(env.VITE_SERVICE_MODE === "supabase"
            ? ["**/src/services/local.ts"]
            : []),
        ],
      },
    },
    plugins: [react(), tailwindcss()],
    build: { target: "es2022" },
    define: {
      "import.meta.env.VITE_SUPABASE_URL": JSON.stringify(url),
      "import.meta.env.VITE_SUPABASE_PUBLISHABLE_KEY": JSON.stringify(key),
    },
  };
});
