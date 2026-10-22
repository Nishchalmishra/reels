import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";
import { createReadStream, statSync, readFileSync } from "node:fs";
import { resolve } from "node:path";
import type { Connect } from "vite";
// Only explicitly reviewed media IDs are served. No database credentials or arbitrary paths.
function suppliedMedia() {
  const manifest = JSON.parse(
    readFileSync(resolve("reference-frames/discovery/manifest.json"), "utf8"),
  ) as { id: string; file: string }[];
  const files = new Map(manifest.map((r) => [r.id, resolve("videos", r.file)]));
  const handler: Connect.NextHandleFunction = (req, res, next) => {
    if (!req.url?.startsWith("/media/")) return next();
    const file = files.get(req.url.split("?")[0].slice(7));
    if (!file) {
      res.statusCode = 404;
      res.end("Unknown lesson");
      return;
    }
    try {
      const size = statSync(file).size;
      let start = 0,
        end = size - 1;
      if (req.headers.range) {
        const m = /^bytes=(\d+)-(\d*)$/.exec(req.headers.range);
        if (!m) {
          res.statusCode = 416;
          res.end();
          return;
        }
        start = Number(m[1]);
        end = m[2] ? Math.min(Number(m[2]), end) : end;
        if (start > end) {
          res.statusCode = 416;
          res.setHeader("Content-Range", `bytes */${size}`);
          res.end();
          return;
        }
        res.statusCode = 206;
        res.setHeader("Content-Range", `bytes ${start}-${end}/${size}`);
      }
      res.setHeader("Content-Type", "video/mp4");
      res.setHeader("Accept-Ranges", "bytes");
      res.setHeader("Content-Length", end - start + 1);
      if (req.method === "HEAD") {
        res.end();
        return;
      }
      const stream = createReadStream(file, { start, end });
      stream.on("error", () => res.destroy());
      res.on("close", () => stream.destroy());
      stream.pipe(res);
    } catch {
      res.statusCode = 404;
      res.end("Supplied lesson file unavailable");
    }
  };
  return {
    name: "supplied-lesson-media",
    configureServer(s: { middlewares: Connect.Server }) {
      s.middlewares.use(handler);
    },
    configurePreviewServer(s: { middlewares: Connect.Server }) {
      s.middlewares.use(handler);
    },
  };
}
export default defineConfig(({ mode, command }) => {
  const env = loadEnv(mode, process.cwd(), "");
  if (env.VITE_MEDIA_MODE && !["hosted", "local"].includes(env.VITE_MEDIA_MODE))
    throw Error("VITE_MEDIA_MODE must be hosted or local.");
  if (command === "build" && env.VITE_MEDIA_MODE === "local")
    throw Error(
      "Local video delivery is development-only. Set VITE_MEDIA_MODE=hosted for deployment.",
    );
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
          "**/.tools/**",
          "**/native-app/**",
          "**/*.hprof",
          "**/hs_err_pid*.log",
          "**/supabase/**",
          "**/scripts/**",
          "**/*.test.*",
          ...(env.VITE_SERVICE_MODE === "supabase"
            ? ["**/src/services/local.ts"]
            : []),
        ],
      },
    },
    plugins: [
      react(),
      tailwindcss(),
      ...(env.VITE_MEDIA_MODE === "local" ? [suppliedMedia()] : []),
    ],
    build: { target: "es2022" },
    define: {
      "import.meta.env.VITE_SUPABASE_URL": JSON.stringify(url),
      "import.meta.env.VITE_SUPABASE_PUBLISHABLE_KEY": JSON.stringify(key),
    },
  };
});
