import fs from "node:fs";
import assert from "node:assert/strict";
import crypto from "node:crypto";
import pg from "pg";
import { createClient } from "@supabase/supabase-js";
import { environment, publicConfig } from "./supabase-env.mjs";
import {connectSupabaseMcp} from './codex-supabase-client.mjs';
import {retainTestSessions} from './test-sessions.mjs';
const env = environment(),
  { url, key } = publicConfig(env),
  run = crypto.randomUUID();
const report = {
  run,
  project: new URL(url).hostname.split(".")[0],
  startedAt: new Date().toISOString(),
  checks: [],
  users: [],
  limitations: [],
};
const clients = [],
  sessions = [];
let database;
const newClient = () =>
  createClient(url, key, {
    auth: {
      persistSession: false,
      autoRefreshToken: false,
      detectSessionInUrl: false,
    },
  });
const check = async (name, fn) => {
  try {
    await fn();
    report.checks.push({ name, status: "passed" });
    console.log("PASS", name);
  } catch (e) {
    report.checks.push({
      name,
      status: "failed",
      code: e.code || e.name,
      message: String(e.message).slice(0, 220),
    });
    throw e;
  }
};
const rpc = async (c, name, args = {}) => {
  const { data, error } = await c.rpc("ar_reels_" + name, args);
  if (error) throw error;
  return data;
};
const snap = (c) => rpc(c, "snapshot");
async function watch(c, chapter) {
  for (let i = 1; i <= 5; i++)
    for (let t = 0; t < 15; t++)
      await rpc(c, "progress", {
        p_reel: `${chapter}-${i}`,
        p_from: t,
        p_to: t + 1,
        p_wall: 1,
        p_speed: 1,
        p_visible: true,
        p_playing: true,
      });
}
async function submitAll(c, id, perfect) {
  for (let i = 0; i < 5; i++)
    await rpc(c, "submit", {
      p_attempt: id,
      p_index: i,
      p_selected: !perfect && i === 0 ? 0 : [1, 2, 0, 3, 2][i],
    });
}
try {
  const settings = await fetch(url + "/auth/v1/settings", {
    headers: { apikey: key },
  });
  if (!settings.ok)
    throw Error("Auth settings request failed: " + settings.status);
  const config = await settings.json();
  if (!config.external?.anonymous_users) {
    report.checks.push({
      name: "Hosted anonymous authentication",
      status: "blocked",
      code: "anonymous_provider_disabled",
    });
    report.limitations.push(
      "Anonymous sign-in is disabled; no test users or records were created.",
    );
    console.log("BLOCKED anonymous authentication is disabled.");
    process.exitCode = 2;
  } else {
    await check(
      "Dedicated authenticated A/B identities and profile restoration",
      async () => {
        for (let i = 0; i < 2; i++) {
          const c = newClient();
          const { data, error } = await c.auth.signInAnonymously({
            options: { data: { integration_test: true, integration_run: run } },
          });
          if (error) throw error;
          clients.push(c);
          sessions.push(data.session);
          report.users.push(data.user.id);
          await rpc(c, "profile", {
            p_name: "Integration test " + (i ? "B" : "A"),
            p_batch: "TEST " + run,
            p_language: "English",
            p_test: true,
          });
        }
        const restored = newClient();
        const { error } = await restored.auth.setSession(sessions[0]);
        if (error) throw error;
        assert.equal((await snap(restored)).profile.id, report.users[0]);
      },
    );
    const [a, b] = clients;
    await check(
      "Actual catalogue and private answer-key API isolation",
      async () => {
        const cat = await rpc(a, "catalogue");
        assert.equal(cat.filter((c) => c.available).length, 2);
        assert.equal(cat.find((c) => c.id === "units").reels.length, 5);
        const publicQuestions = await a.from("ar_reels_questions").select("*");
        if (publicQuestions.error) throw publicQuestions.error;
        assert.equal(publicQuestions.data.length, 10);
        assert.ok(
          publicQuestions.data.every(
            (q) => !("correct_index" in q) && !("explanation" in q),
          ),
        );
        assert.ok(
          (await a.schema("ar_reels_private").from("answer_keys").select("*"))
            .error,
        );
        assert.ok(
          (await a.from("ar_reels_questions").select("correct_index")).error,
        );
      },
    );
    await check(
      "Stored coverage rejects seeks/background/repeats and mixed chapters",
      async () => {
        const base = {
          p_reel: "units-1",
          p_from: 0,
          p_to: 17,
          p_wall: 0.2,
          p_speed: 1,
          p_visible: true,
          p_playing: true,
        };
        await rpc(a, "progress", base);
        await rpc(a, "progress", {
          ...base,
          p_to: 1,
          p_wall: 1,
          p_visible: false,
        });
        await rpc(a, "progress", {
          ...base,
          p_to: 1,
          p_wall: 1,
          p_playing: false,
        });
        for (let i = 0; i < 5; i++)
          await rpc(a, "progress", { ...base, p_to: 1, p_wall: 1 });
        await watch(a, "motion");
        await assert.rejects(() => rpc(a, "start", { p_chapter: "units" }));
        assert.deepEqual((await snap(a)).coverage["units-1"], [[0, 1]]);
        await watch(a, "units");
        const restored = newClient();
        await restored.auth.setSession(sessions[0]);
        assert.deepEqual(
          (await snap(restored)).coverage,
          (await snap(a)).coverage,
        );
      },
    );
    let first, passed;
    await check(
      "Answer persistence, ownership, 4/5 zero coins and correct mapping",
      async () => {
        first = await rpc(a, "start", { p_chapter: "units" });
        assert.deepEqual(
          first.questions.map((q) => q.reelId),
          [1, 2, 3, 4, 5].map((i) => "units-" + i),
        );
        assert.ok(first.answers.every((x) => x === null));
        await assert.rejects(() => rpc(a, "finish", { p_attempt: first.id }));
        const repeated = await Promise.all([
          rpc(a, "submit", {p_attempt:first.id,p_index:0,p_selected:0}),
          rpc(a, "submit", {p_attempt:first.id,p_index:0,p_selected:0}),
        ]);
        assert.deepEqual(repeated[0],repeated[1]);
        const original = await a.from("ar_reels_answers").select("*")
          .eq("attempt_id",first.id).eq("position",0);
        if(original.error)throw original.error;
        assert.equal(original.data.length,1);
        assert.equal(original.data[0].selected,0);
        await rpc(a,"submit",{p_attempt:first.id,p_index:0,p_selected:1});
        const unchanged = await a.from("ar_reels_answers").select("*")
          .eq("attempt_id",first.id).eq("position",0);
        if(unchanged.error)throw unchanged.error;
        assert.deepEqual(unchanged.data,original.data);
        await submitAll(a, first.id, false);
        assert.equal(
          (await rpc(a, "review", { p_attempt: first.id })).answers.length,
          5,
        );
        await assert.rejects(() => rpc(b, "review", { p_attempt: first.id }));
        await assert.rejects(() =>
          rpc(b, "submit", { p_attempt: first.id, p_index: 0, p_selected: 1 }),
        );
        const result = await rpc(a, "finish", { p_attempt: first.id });
        assert.equal(result.score, 4);
        assert.deepEqual(await rpc(a,"submit",{
          p_attempt:first.id,p_index:0,p_selected:1,
        }),result);
        assert.equal((await snap(a)).ledger.length, 0);
      },
    );
    await check(
      "Perfect retry and concurrent completion earn exactly five once",
      async () => {
        passed = await rpc(a, "start", { p_chapter: "units" });
        await submitAll(a, passed.id, true);
        const results = await Promise.all([
          rpc(a, "finish", { p_attempt: passed.id }),
          rpc(a, "finish", { p_attempt: passed.id }),
        ]);
        assert.ok(results.every((r) => r.score === 5));
        let s = await snap(a);
        assert.equal(s.ledger.length, 1);
        assert.equal(s.ledger[0].amount, 5);
        const practice = await rpc(a, "start", { p_chapter: "units" });
        await submitAll(a, practice.id, true);
        assert.equal(
          (await rpc(a, "finish", { p_attempt: practice.id })).alreadyRewarded,
          true,
        );
        s = await snap(a);
        assert.equal(s.ledger.length, 1);
        const records = await a.from("ar_reels_ledger").select("*");
        if (records.error) throw records.error;
        assert.equal(
          records.data.reduce((n, l) => n + l.amount, 0),
          5,
        );
      },
    );
    await check(
      "Five-coin quote uses trusted backend price and cancellation has no debit",
      async () => {
        const q = await rpc(a, "quote", { p_product: "pro", p_coins: 500 });
        assert.equal(q.subtotal, 50000);
        assert.equal(q.coins, 5);
        assert.equal(q.discount, 100);
        assert.equal(
          (await snap(a)).ledger.reduce((n, l) => n + l.amount, 0),
          5,
        );
      },
    );
    const dbUrl = env.SUPABASE_DB_URL || env.DATABASE_URL;
    if (dbUrl || process.argv.includes('--mcp')) {
      if(!dbUrl){const mcp=await connectSupabaseMcp();database={query:mcp.query,end:mcp.close};}
      else {
      const parsed = new URL(dbUrl);
      if (
        !parsed.hostname.includes(report.project) &&
        !parsed.username.includes(report.project)
      )
        throw Error("Database project mismatch");
      database = new pg.Client({
        connectionString: dbUrl,
        ssl: { rejectUnauthorized: true },
      });
      await database.connect();
      }
      await check(
        "All four money examples with an isolated TEST-only 495-coin fixture",
        async () => {
          await database.query(
            "insert into public.ar_reels_ledger(user_id,amount,reason,ref) select id,495,'TEST ONLY integration fixture',$2 from public.ar_reels_profiles where id=$1 and is_test",
            [report.users[0], "test-fixture-" + run],
          );
          for (const [product, coins, discount] of [
            ["pro", 250, 5000],
            ["kit", 500, 10000],
            ["notes", 49, 980],
          ]) {
            const q = await rpc(a, "quote", {
              p_product: product,
              p_coins: 500,
            });
            assert.equal(q.coins, coins);
            assert.equal(q.discount, discount);
          }
          assert.equal(
            (await snap(a)).ledger.reduce((n, l) => n + l.amount, 0),
            500,
          );
        },
      );
      await check(
        "Concurrent spend protection, idempotency, actual orders/debits and stable recognition",
        async () => {
          const before = await rpc(a, "recognition"),
            requests = [
              { product: "pro", coins: 250, id: crypto.randomUUID() },
              { product: "kit", coins: 500, id: crypto.randomUUID() },
            ];
          const results = await Promise.allSettled(
            requests.map((r) =>
              rpc(a, "confirm", {
                p_product: r.product,
                p_coins: r.coins,
                p_request: r.id,
              }),
            ),
          );
          assert.equal(
            results.filter((r) => r.status === "fulfilled").length,
            1,
          );
          const winner =
            requests[results.findIndex((r) => r.status === "fulfilled")];
          await rpc(a, "confirm", {
            p_product: winner.product,
            p_coins: winner.coins,
            p_request: winner.id,
          });
          const rows = await database.query(
            "select count(*)::int n from public.ar_reels_orders where user_id=$1",
            [report.users[0]],
          );
          assert.equal(rows.rows[0].n, 1);
          const ledger = await database.query(
            "select amount from public.ar_reels_ledger where user_id=$1",
            [report.users[0]],
          );
          assert.equal(
            ledger.rows.reduce((n, l) => n + l.amount, 0),
            500 - winner.coins,
          );
          assert.deepEqual(await rpc(a, "recognition"), before);
        },
      );
    } else
      report.limitations.push(
        "500-coin hosted money examples and parallel spend verification need SUPABASE_DB_URL to create an explicitly labelled fixture on this dedicated test identity. No normal student balances are seeded.",
      );
    await check(
      "User B cannot read or mutate user A records; no direct reward writes",
      async () => {
        for (const table of [
          "progress",
          "attempts",
          "answers",
          "ledger",
          "orders",
          "events",
        ]) {
          let query = b.from("ar_reels_" + table).select("*");
          if (table !== "answers") query = query.eq("user_id", report.users[0]);
          const { data, error } = await query;
          if (error) throw error;
          assert.equal(data.length, 0);
        }
        assert.ok(
          (
            await b
              .from("ar_reels_progress")
              .update({ covered: 999 })
              .eq("user_id", report.users[0])
          ).error,
        );
        assert.ok(
          (
            await a.from("ar_reels_ledger").insert({
              user_id: report.users[0],
              amount: 500,
              reason: "forged",
              ref: run,
            })
          ).error,
        );
      },
    );
    await check(
      "Analytics event IDs deduplicate and test flags persist",
      async () => {
        const e = {
          id: crypto.randomUUID(),
          sessionId: crypto.randomUUID(),
          name: "reels_entry_clicked",
          raw_personal_detail: "must be stripped",
        };
        await rpc(a, "analytics", { p_event: e });
        await rpc(a, "analytics", { p_event: e });
        const rows = await a.from("ar_reels_events").select("*").eq("id", e.id);
        if (rows.error) throw rows.error;
        assert.equal(rows.data.length, 1);
        assert.equal(rows.data[0].is_test, true);
        assert.ok(!JSON.stringify(rows.data).includes("must be stripped"));
      },
    );
  }
} catch (e) {
  console.error(
    "Live integration verification failed:",
    e.code || e.name,
    "— secret values withheld.",
  );
  report.limitations.push(
    "Hosted checks did not all complete. See failed check and current deployment/Auth status.",
  );
  process.exitCode = 1;
} finally {
  if (database) await database.end();
  if (sessions.length) {
    retainTestSessions(run,report.users,sessions);
    report.limitations.push(
      "Dedicated test identities retained for reproducibility. Their session file is ignored by git. Cleanup must target only these exact user IDs.",
    );
  }
  report.finishedAt = new Date().toISOString();
  fs.mkdirSync("supabase/inspection", { recursive: true });
  fs.writeFileSync(
    "supabase/inspection/live-verification.json",
    JSON.stringify(report, null, 2),
  );
  fs.mkdirSync("supabase/inspection/runs",{recursive:true});
  fs.writeFileSync(`supabase/inspection/runs/${run}.json`,JSON.stringify(report,null,2));
}
