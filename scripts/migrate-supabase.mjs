import { buildMigrationBatch, auditMigrationSql } from "./migration-batch.mjs";
import fs from "node:fs";
import path from "node:path";
import crypto from "node:crypto";
import pg from "pg";
import { environment, publicConfig } from "./supabase-env.mjs";
const env = environment(),
  { url } = publicConfig(env),
  project = new URL(url).hostname.split(".")[0];
const dbUrl = env.SUPABASE_DB_URL || env.DATABASE_URL;
if (!dbUrl && !env.SUPABASE_ACCESS_TOKEN) {
  console.error(
    "BLOCKED: SUPABASE_DB_URL or a Supabase management token with database query permission is required.",
  );
  process.exit(2);
}
let client;
if (dbUrl) {
  const database = new URL(dbUrl);
  if (
    !database.hostname.includes(project) &&
    !database.username.includes(project)
  )
    throw Error(
      "Database URL does not identify the supplied project; refusing to apply changes.",
    );
  client = new pg.Client({
    connectionString: dbUrl,
    ssl: { rejectUnauthorized: true },
  });
} else
  client = {
    connect: async () => {},
    end: async () => {},
    query: async (query) => {
      const r = await fetch(
        `https://api.supabase.com/v1/projects/${project}/database/query`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${env.SUPABASE_ACCESS_TOKEN}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ query, read_only: false }),
        },
      );
      if (!r.ok) {
        const e = new Error("Management SQL request failed");
        e.code = "HTTP_" + r.status;
        throw e;
      }
      const body = await r.json();
      return { rows: Array.isArray(body) ? body : body.result || [] };
    },
  };
try {
  await client.connect();
  const inspection = {
    project,
    at: new Date().toISOString(),
    tables: (
      await client.query(
        "select table_schema,table_name,column_name,data_type from information_schema.columns where table_schema in ('public','ar_reels_private') order by table_schema,table_name,ordinal_position",
      )
    ).rows,
    policies: (
      await client.query(
        "select schemaname,tablename,policyname,roles,cmd,qual,with_check from pg_policies where schemaname in ('public','ar_reels_private')",
      )
    ).rows,
    functions: (
      await client.query(
        "select n.nspname schema,p.proname name,p.prosecdef security_definer,p.proconfig settings,pg_get_function_identity_arguments(p.oid) arguments from pg_proc p join pg_namespace n on n.oid=p.pronamespace where n.nspname in ('public','ar_reels_private')",
      )
    ).rows,
    rls: (
      await client.query(
        "select n.nspname schema,c.relname name,c.relrowsecurity rls from pg_class c join pg_namespace n on n.oid=c.relnamespace where n.nspname in ('public','ar_reels_private') and c.relkind='r'",
      )
    ).rows,
    buckets: (
      await client.query(
        "select id,public,file_size_limit,allowed_mime_types from storage.buckets",
      )
    ).rows,
  };
  fs.mkdirSync("supabase/inspection", { recursive: true });
  fs.writeFileSync(
    "supabase/inspection/database.json",
    JSON.stringify(inspection, null, 2),
  );
  console.log(
    "Inspected project",
    project,
    "tables/policies/functions/storage. No row data exported.",
  );
  if (process.argv.includes("--inspect-only")) process.exitCode = 0;
  else {
    const { files, sql } = buildMigrationBatch();
    await client.query(sql);
    const audit = (await client.query(auditMigrationSql)).rows[0];
    if (
      audit.unprotected_tables !== 0 ||
      audit.private_schema_access ||
      audit.answer_key_access ||
      audit.direct_reward_insert ||
      audit.direct_progress_update ||
      audit.anonymous_finish ||
      !audit.authenticated_finish
    )
      throw Object.assign(new Error("Post-deployment access audit failed"), {
        code: "ACCESS_AUDIT_FAILED",
      });
    fs.writeFileSync(
      "supabase/inspection/deployment.json",
      JSON.stringify(
        { project, at: new Date().toISOString(), migrations: files, audit },
        null,
        2,
      ),
    );
    console.log(
      "Committed/verified versioned non-destructive Reels migrations:",
      files.join(", "),
    );
  }
} catch (e) {
  try {
    await client.query("rollback");
  } catch {}
  console.error(
    "Migration/inspection failed:",
    e.code || e.name,
    "— credentials and connection strings withheld.",
  );
  process.exitCode = 1;
} finally {
  await client.end();
}
