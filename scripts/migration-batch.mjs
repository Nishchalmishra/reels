import fs from "node:fs";
import path from "node:path";
import crypto from "node:crypto";
export const auditMigrationSql = `select
 (select count(*)::int from pg_class c join pg_namespace n on n.oid=c.relnamespace where n.nspname='public' and c.relname like 'ar_reels_%' and c.relkind='r' and not c.relrowsecurity) unprotected_tables,
 has_schema_privilege('authenticated','ar_reels_private','usage') private_schema_access,
 has_table_privilege('authenticated','ar_reels_private.answer_keys','select') answer_key_access,
 has_table_privilege('authenticated','public.ar_reels_ledger','insert') direct_reward_insert,
 has_table_privilege('authenticated','public.ar_reels_progress','update') direct_progress_update,
 has_function_privilege('anon','public.ar_reels_finish(uuid)','execute') anonymous_finish,
 has_function_privilege('authenticated','public.ar_reels_finish(uuid)','execute') authenticated_finish`;
export function buildMigrationBatch(directory = "supabase/migrations") {
  const files = fs
    .readdirSync(directory)
    .filter((x) => x.endsWith(".sql"))
    .sort();
  const quote = (value) => "'" + String(value).replaceAll("'", "''") + "'";
  const batch = [
    "begin;select pg_advisory_xact_lock(hashtext('arivihan-reels-migrations'));create table if not exists public.ar_reels_migrations (version text primary key, checksum text not null, applied_at timestamptz not null default now());alter table public.ar_reels_migrations enable row level security;revoke all on public.ar_reels_migrations from public,anon,authenticated;",
  ];
  for (const file of files) {
    const sql = fs.readFileSync(path.join(directory, file), "utf8"),
      sum = crypto.createHash("sha256").update(sql).digest("hex");
    batch.push(
      `do $version_guard$ begin if exists(select 1 from public.ar_reels_migrations where version=${quote(file)} and checksum<>${quote(sum)}) then raise exception 'Migration checksum mismatch';elsif not exists(select 1 from public.ar_reels_migrations where version=${quote(file)}) then execute ${quote(sql)};insert into public.ar_reels_migrations(version,checksum) values(${quote(file)},${quote(sum)});end if;end $version_guard$;`,
    );
  }
  batch.push("notify pgrst,'reload schema';commit;");
  return { files, sql: batch.join("\n") };
}
