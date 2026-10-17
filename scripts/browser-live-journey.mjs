import fs from "node:fs";
import crypto from "node:crypto";
import assert from "node:assert/strict";
import { chromium } from "playwright";
import { createClient } from "@supabase/supabase-js";
import { publicConfig } from "./supabase-env.mjs";
import {retainTestSessions} from './test-sessions.mjs';
const { url, key } = publicConfig(),
  project = new URL(url).hostname.split(".")[0],
  run = crypto.randomUUID();
const report = { run, project, checks: [], status: "running" };
let client;
const rpc = async (name, args = {}) => {
  const { data, error } = await client.rpc("ar_reels_" + name, args);
  if (error) throw error;
  return data;
};
const config = await (
  await fetch(url + "/auth/v1/settings", { headers: { apikey: key } })
).json();
fs.mkdirSync("verification-live", { recursive: true });
if (!config.external?.anonymous_users) {
  report.status = "blocked";
  report.reason = "anonymous_provider_disabled";
  fs.writeFileSync(
    "verification-live/full-journey.json",
    JSON.stringify(report, null, 2),
  );
  console.log(
    "BLOCKED full hosted mobile journey: anonymous sign-in is disabled. No user or test records created.",
  );
  process.exitCode = 2;
} else {
  client = createClient(url, key, {
    auth: { persistSession: false, autoRefreshToken: false },
  });
  let browser;
  try {
    const { data, error } = await client.auth.signInAnonymously({
      options: { data: { integration_test: true, integration_run: run } },
    });
    if (error) throw error;
    const session = data.session;
    report.userId = data.user.id;
    retainTestSessions(run,[data.user.id],[session]);
    await rpc("profile", {
      p_name: "TEST mobile " + run.slice(0, 8),
      p_batch: "Integration verification",
      p_language: "English",
      p_test: true,
    });
    const ownedProfile = await client
      .from("ar_reels_profiles")
      .select("id,is_test")
      .single();
    if (ownedProfile.error) throw ownedProfile.error;
    assert.equal(ownedProfile.data.is_test, true);
    browser = await chromium.launch({ channel: "chrome", headless: true });
    const page = await browser.newPage({
      viewport: { width: 390, height: 844 },
      reducedMotion: "reduce",
    });
    page.setDefaultTimeout(20000);
    const errors = [];
    page.on("pageerror", (e) => errors.push(e.message));
    await page.addInitScript(
      ({ storageKey, session }) => {
        if (!localStorage.getItem(storageKey))
          localStorage.setItem(storageKey, JSON.stringify(session));
      },
      { storageKey: `sb-${project}-auth-token`, session },
    );
    await page.goto(process.env.REELS_BASE_URL || "http://127.0.0.1:4174/");
    await page
      .getByRole("heading", { name: "Physics chapters", exact: true })
      .waitFor();
    report.checks.push(
      "Actual guest session/profile restored into live mobile app",
    );
    await page.getByRole("button", { name: /Units & Measurements/ }).click();
    await page.getByRole("button", { name: "Play All", exact: true }).click();
    await page.getByRole("button", { name: "Playback settings" }).click();
    await page.getByRole("button", { name: "2×", exact: true }).click();
    for (let i = 0; i < 5; i++) {
      await page
        .getByRole("button", { name: "Tap to Play", exact: true })
        .click();
      await page
        .locator(".progress-pill")
        .filter({ hasText: `${i + 1}/5 watched` })
        .waitFor({ timeout: 30000 });
      if (i < 4) {
        if (i === 1) {
          await page.reload();
          await page.getByRole("button", { name: "Tap to Play" }).waitFor();
        }
        await page
          .getByRole("button", { name: "Next lesson", exact: true })
          .click();
      }
      console.log("PASS live unique watch", i + 1, "/5");
    }
    await page
      .getByRole("button", { name: "Start Challenge", exact: true })
      .click();
    const answer = async (indices) => {
      for (let i = 0; i < 5; i++) {
        await page.locator(".option").nth(indices[i]).click();
        await page
          .getByRole("button", { name: "Submit Answer", exact: true })
          .click();
        await page.locator(".feedback").waitFor();
        if(i===1){
          await page.reload();
          await page.locator(".option").first().waitFor();
          const saved=await rpc("snapshot");
          assert.ok(saved.attempts.some(a=>!a.finished&&a.answers.filter(x=>x!==null).length===2));
          continue; // Restored attempts resume at the next unanswered question.
        }
        await page
          .getByRole("button", {
            name: i === 4 ? "See my result" : "Next question",
            exact: true,
          })
          .click();
      }
    };
    await answer([0, 2, 0, 3, 2]);
    await page.getByText(/0 coins earned/).waitFor();
    assert.equal((await rpc("snapshot")).ledger.length, 0);
    await page.getByRole("button", { name: "Retry full challenge" }).click();
    await answer([1, 2, 0, 3, 2]);
    await page
      .getByRole("heading", { name: "Perfect score! +5 coins", exact: true })
      .waitFor();
    let state = await rpc("snapshot");
    assert.equal(state.ledger.length, 1);
    assert.equal(state.ledger[0].amount, 5);
    const rank = await rpc("recognition");
    report.checks.push(
      "Five actual lessons and in-progress quiz answers survive reload; 4/5 zero; successful full retry five coins",
    );
    await page.getByRole("button", { name: "Go to Wallet" }).click();
    await page.reload();
    await page.locator(".wallet-number").filter({ hasText: "5" }).waitFor();
    await page.getByRole("link", { name: "Explore the demo store" }).click();
    await page.getByRole("link", { name: /Arivihan Pro/ }).click();
    await page.getByRole("checkbox", { name: "Use available coins" }).check();
    await page.getByText(/499\.00/).waitFor();
    await page.getByRole("button", { name: "Cancel", exact: false }).click();
    assert.equal(
      (await rpc("snapshot")).ledger.reduce((n, l) => n + l.amount, 0),
      5,
    );
    await page.getByRole("link", { name: /Arivihan Pro/ }).click();
    await page.getByRole("checkbox", { name: "Use available coins" }).check();
    await page.getByText(/499\.00/).waitFor();
    await page.getByRole("button", { name: "Confirm simulated order" }).click();
    await page
      .getByRole("heading", { name: "Your demo order is recorded." })
      .waitFor();
    await page.reload();
    await page
      .getByRole("heading", { name: "Your demo order is recorded." })
      .waitFor();
    await page.screenshot({
      path: "verification-live/completed-simulated-order.png",
      fullPage: true,
    });
    state = await rpc("snapshot");
    assert.equal(state.orders.length, 1);
    assert.equal(
      state.ledger.reduce((n, l) => n + l.amount, 0),
      0,
    );
    const order = state.orders[0];
    await rpc("confirm", { p_product: "pro", p_coins: 5, p_request: order.id });
    assert.equal((await rpc("snapshot")).orders.length, 1);
    assert.deepEqual(await rpc("recognition"), rank);
    assert.deepEqual(errors, []);
    const actualLedger = await client
      .from("ar_reels_ledger")
      .select("amount,reason,ref");
    if (actualLedger.error) throw actualLedger.error;
    assert.equal(actualLedger.data.length, 2);
    report.checks.push(
      "Actual ledger/order records reconcile; cancel is free; refresh/idempotent confirm once; lifetime rank unchanged",
    );
    report.status = "passed";
    report.records = {
      attempts: state.attempts.length,
      ledger: actualLedger.data,
      orderId: order.id,
    };
    report.retainedTestIdentity = true;
    console.log(
      "PASS full live mobile journey against actual Supabase records. Dedicated TEST identity retained.",
    );
  } catch (e) {
    report.status = "failed";
    report.code = e.code || e.name;
    report.reason = String(e.message).slice(0, 200);
    console.error(
      "Live mobile journey failed:",
      report.code,
      "— credentials withheld.",
    );
    process.exitCode = 1;
  } finally {
    await browser?.close();
    fs.writeFileSync(
      "verification-live/full-journey.json",
      JSON.stringify(report, null, 2),
    );
    fs.mkdirSync("verification-live/runs",{recursive:true});
    fs.writeFileSync(`verification-live/runs/${run}.json`,JSON.stringify(report,null,2));
  }
}
