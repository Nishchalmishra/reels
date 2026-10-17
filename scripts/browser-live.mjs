import fs from "node:fs";
import assert from "node:assert/strict";
import { chromium } from "playwright";
fs.mkdirSync("verification-live", { recursive: true });
const browser = await chromium.launch({ channel: "chrome", headless: true });
try {
  const page = await browser.newPage({ viewport: { width: 390, height: 844 } });
  const errors = [],
    requests = [];
  page.on("pageerror", (e) => errors.push(e.message));
  page.on("request", (r) => {
    const u = new URL(r.url());
    if (u.hostname.endsWith(".supabase.co"))
      requests.push({ host: u.hostname, path: u.pathname, method: r.method() });
  });
  await page.goto("http://127.0.0.1:4174/");
  await page
    .getByRole("button", { name: "Continue as guest student", exact: true })
    .waitFor();
  await page
    .getByRole("alert")
    .filter({ hasText: /migrations|schema cache|function/i })
    .waitFor();
  await page.screenshot({
    path: "verification-live/backend-not-deployed.png",
    fullPage: true,
  });
  await page
    .getByRole("button", { name: "Continue as guest student", exact: true })
    .click();
  await page
    .getByRole("alert")
    .filter({ hasText: /Anonymous sign-in is disabled/i })
    .waitFor();
  await page.screenshot({
    path: "verification-live/anonymous-auth-disabled.png",
    fullPage: true,
  });
  assert.equal(
    await page.getByRole("heading", { name: "Physics chapters" }).count(),
    0,
  );
  const storage = await page.evaluate(() => Object.keys(localStorage));
  assert.ok(!storage.includes("arivihan.demo.v1"));
  await page.reload();
  await page
    .getByRole("button", { name: "Continue as guest student" })
    .waitFor();
  assert.deepEqual(errors, []);
  assert.ok(requests.some((r) => r.path === "/rest/v1/rpc/ar_reels_catalogue"));
  assert.ok(requests.some((r) => r.path === "/auth/v1/signup"));
  fs.writeFileSync(
    "verification-live/browser-report.json",
    JSON.stringify(
      {
        status: "passed-blocker-behavior-only",
        fullLiveJourney: "blocked",
        requests,
        checks: [
          "actual Supabase RPC request",
          "missing migration surfaced",
          "actual anonymous auth rejection surfaced",
          "no fake profile/catalogue",
          "no mock local storage",
          "reload remains signed out",
        ],
        errors,
      },
      null,
      2,
    ),
  );
  console.log(
    "PASS live-mode blocker behavior against actual Supabase: missing migrations/Auth disabled surfaced, no mock fallback. Full live journey remains blocked.",
  );
} finally {
  await browser.close();
}
