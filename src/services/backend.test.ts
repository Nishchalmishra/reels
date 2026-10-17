import {
  buildMigrationBatch,
  auditMigrationSql,
} from "../../scripts/migration-batch.mjs";
import { beforeAll, afterAll, describe, it, expect } from "vitest";
import { PGlite } from "@electric-sql/pglite";
import { readFileSync } from "node:fs";
let db: PGlite;
const A = crypto.randomUUID(),
  B = crypto.randomUUID();
async function owner() {
  await db.exec("reset role");
}
async function user(id: string) {
  await db.exec(`set role authenticated;set request.jwt.claim.sub='${id}'`);
}
async function call<T = any>(name: string, args: unknown[] = []): Promise<T> {
  const placeholders = args.map((_, i) => `$${i + 1}`).join(",");
  return (
    await db.query<{ result: T }>(
      `select public.ar_reels_${name}(${placeholders}) result`,
      args,
    )
  ).rows[0].result;
}
async function profile(id: string) {
  await owner();
  await db.query(
    "insert into auth.users(id) values($1) on conflict do nothing",
    [id],
  );
  await user(id);
  return call("profile", ["Integration test", "Test batch", "English", true]);
}
async function watch(chapter = "units") {
  for (let i = 1; i <= 5; i++)
    for (let t = 0; t < 15; t++)
      await call("progress", [`${chapter}-${i}`, t, t + 1, 1, 1, true, true]);
}
async function attempt(correct = true) {
  const a = await call("start", ["units"]);
  const answers = [1, 2, 0, 3, 2];
  for (let i = 0; i < 5; i++)
    await call("submit", [a.id, i, correct || i !== 0 ? answers[i] : 0]);
  return call("finish", [a.id]);
}
beforeAll(async () => {
  db = new PGlite();
  await db.exec(
    `create role anon nologin;create role authenticated nologin;create schema auth;create table auth.users(id uuid primary key);create function auth.uid() returns uuid language sql stable as $$select nullif(current_setting('request.jwt.claim.sub',true),'')::uuid$$;grant usage on schema auth to anon,authenticated;grant execute on function auth.uid() to anon,authenticated;`,
  );
  await db.exec(buildMigrationBatch().sql);
  await db.exec(buildMigrationBatch().sql);
  const audit = (
    await db.query<Record<string, boolean | number>>(auditMigrationSql)
  ).rows[0];
  expect(audit).toEqual({
    unprotected_tables: 0,
    private_schema_access: false,
    answer_key_access: false,
    direct_reward_insert: false,
    direct_progress_update: false,
    anonymous_finish: false,
    authenticated_finish: true,
  });
  await profile(A);
  await profile(B);
}, 60000);
afterAll(async () => {
  await db?.close();
});
describe("real PostgreSQL migration and RPC contract (embedded; not hosted Supabase)", () => {
  it("deploys RLS, private keys and only authenticated mutations", async () => {
    await owner();
    const policies = await db.query<{
      relname: string;
      relrowsecurity: boolean;
    }>(
      "select relname,relrowsecurity from pg_class where relname like 'ar_reels_%' and relkind='r'",
    );
    expect(policies.rows.every((r) => r.relrowsecurity)).toBe(true);
    await db.exec("set role anon");
    expect((await call<any[]>("catalogue")).length).toBe(4);
    await expect(call("start", ["units"])).rejects.toThrow(/permission denied/);
    await user(A);
    await expect(
      db.query("select * from ar_reels_private.answer_keys"),
    ).rejects.toThrow(/permission denied/);
    await expect(
      db.query(
        "insert into public.ar_reels_ledger(user_id,amount,reason,ref) values($1,500,'forged','forged')",
        [A],
      ),
    ).rejects.toThrow(/permission denied/);
    const rows = await db.query("select * from public.ar_reels_questions");
    expect(rows.rows).toHaveLength(10);
    expect(rows.rows[0]).not.toHaveProperty("correct_index");
  });
  it("profile restores, seek/background/repeats do not unlock, and mixed chapters cannot qualify", async () => {
    await user(A);
    await call("progress", ["units-1", 0, 17, 0.2, 1, true, true]);
    await call("progress", ["units-1", 0, 15, 1, 1, false, true]);
    await call("progress", ["units-1", 0, 15, 1, 1, true, false]);
    for (let i = 0; i < 5; i++)
      await call("progress", ["units-1", 0, 1, 1, 1, true, true]);
    await watch("motion");
    await expect(call("start", ["units"])).rejects.toThrow(/Watch at least/);
    await call("checkpoint", ["units-1", 16]);
    const state = await call("snapshot");
    expect(state.profile.id).toBe(A);
    expect(state.coverage["units-1"]).toEqual([[0, 1]]);
    expect(state.positions["units-1"]).toBe(16);
  });
  it("server maps exact questions, enforces ownership/order and persists locked feedback", async () => {
    await user(A);
    await watch();
    const a = await call("start", ["units"]);
    expect(a.questions.map((q: any) => q.reelId)).toEqual(
      [1, 2, 3, 4, 5].map((i) => `units-${i}`),
    );
    expect(a.answers).toEqual([null, null, null, null, null]);
    expect(JSON.stringify(a)).not.toContain("correct_index");
    await expect(call("submit", [a.id, 1, 2])).rejects.toThrow(
      /Invalid answer transition/,
    );
    await call("submit", [a.id, 0, 0]);
    const repeated = await call("submit", [a.id, 0, 1]);
    expect(repeated.answers[0].selected).toBe(0);
    await user(B);
    await expect(call("review", [a.id])).rejects.toThrow(/Attempt not found/);
    await expect(call("submit", [a.id, 0, 1])).rejects.toThrow(
      /Attempt not found/,
    );
    await expect(call("finish", [a.id])).rejects.toThrow(/Attempt not found/);
    expect(
      (await db.query("select * from public.ar_reels_progress")).rows,
    ).toHaveLength(0);
    expect(
      (await db.query("select * from public.ar_reels_attempts")).rows,
    ).toHaveLength(0);
    await user(A);
    expect((await call("review", [a.id])).answers[0].selected).toBe(0);
  });
  it("4/5 earns zero, retry earns exactly five, duplicate finish/practice never mint again", async () => {
    await user(A);
    let a = await call("start", ["units"]);
    for (let i = 1; i < 5; i++)
      await call("submit", [a.id, i, [1, 2, 0, 3, 2][i]]);
    const failed = await call("finish", [a.id]);
    expect(failed.score).toBe(4);
    expect((await call("snapshot")).ledger).toHaveLength(0);
    a = await attempt();
    expect(a.score).toBe(5);
    expect(a.rewarded).toBe(true);
    await Promise.all([call("finish", [a.id]), call("finish", [a.id])]);
    const practice = await attempt();
    expect(practice.alreadyRewarded).toBe(true);
    expect(practice.rewarded).toBe(false);
    const s = await call("snapshot");
    expect(s.ledger).toHaveLength(1);
    expect(s.ledger[0].amount).toBe(5);
    expect(
      s.events.filter((e: any) => e.name === "reels_reward_earned"),
    ).toHaveLength(1);
  });
  it("uses trusted prices, no quote debit, integer caps, idempotent orders and lifetime rank", async () => {
    await user(A);
    let q = await call("quote", ["pro", 500]);
    expect(q.coins).toBe(5);
    expect(q.discount).toBe(100);
    expect((await call("snapshot")).ledger).toHaveLength(1);
    const id = crypto.randomUUID();
    await profile(id);
    await owner();
    await db.query(
      "insert into public.ar_reels_ledger(user_id,amount,reason,ref) values($1,500,'Isolated SQL test fixture','test-500')",
      [id],
    );
    await user(id);
    for (const [product, used, discount] of [
      ["pro", 250, 5000],
      ["kit", 500, 10000],
      ["notes", 49, 980],
    ]) {
      q = await call("quote", [product, 500]);
      expect(q.coins).toBe(used);
      expect(q.discount).toBe(discount);
    }
    const before = await call("recognition"),
      request = crypto.randomUUID();
    await call("confirm", ["pro", 250, request]);
    await call("confirm", ["pro", 250, request]);
    expect(await call("recognition")).toEqual(before);
    let s = await call("snapshot");
    expect(s.orders).toHaveLength(1);
    expect(s.ledger.reduce((n: number, l: any) => n + l.amount, 0)).toBe(250);
    expect((await call("quote", ["pro", 0])).requestId).toBe(request);
    await expect(
      call("confirm", ["kit", 500, crypto.randomUUID()]),
    ).rejects.toThrow(/insufficient coins/);
    s = await call("snapshot");
    expect(s.orders).toHaveLength(1);
    await user(B);
    expect(
      (await db.query("select * from public.ar_reels_orders")).rows,
    ).toHaveLength(0);
    expect(
      (await db.query("select * from public.ar_reels_ledger")).rows,
    ).toHaveLength(0);
  });
  it("deduplicates analytics and strips arbitrary personal payloads", async () => {
    await user(B);
    const id = crypto.randomUUID();
    const e = JSON.stringify({
      id,
      name: "reels_entry_clicked",
      sessionId: crypto.randomUUID(),
      name_to_leak: "PRIVATE NAME",
      correct_index: 1,
    });
    await call("analytics", [e]);
    await call("analytics", [e]);
    const s = await call("snapshot");
    expect(s.events.filter((e: any) => e.id === id)).toHaveLength(1);
    expect(JSON.stringify(s.events)).not.toContain("PRIVATE NAME");
    await expect(
      call("analytics", [
        JSON.stringify({
          id: crypto.randomUUID(),
          name: "reels_reward_earned",
        }),
      ]),
    ).rejects.toThrow(/Unsupported/);
  });
});
