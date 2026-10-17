import { chromium } from "playwright";
import assert from "node:assert/strict";
const browser = await chromium.launch({ channel: "chrome", headless: true });
try {
  const page = await browser.newPage({
    viewport: { width: 360, height: 780 },
    reducedMotion: "reduce",
  });
  page.setDefaultTimeout(15000);
  const errors = [];
  page.on("pageerror", (e) => errors.push(e.message));
  await page.goto("http://127.0.0.1:4173/");
  await page
    .getByRole("button", { name: "Continue with demo student" })
    .click();
  await page.getByRole("button", { name: /Units & Measurements/ }).click();
  await page.getByRole("button", { name: "Play All", exact: true }).click();
  await page
    .getByRole("button", { name: "Playback settings", exact: true })
    .click();
  await page.getByRole("button", { name: "2×", exact: true }).click();
  for (let i = 0; i < 5; i++) {
    await page
      .getByRole("button", { name: "Tap to Play", exact: true })
      .click();
    await page
      .locator(".progress-pill")
      .filter({ hasText: `${i + 1}/5 watched` })
      .waitFor({ timeout: 15000 });
    console.log(`PASS normal unique watch qualification ${i + 1}/5`);
    if (i < 4)
      await page
        .getByRole("button", { name: "Next lesson", exact: true })
        .click();
  }
  await page
    .getByRole("dialog", { name: "Ready to test what you learned?" })
    .waitFor();
  await page
    .getByRole("button", { name: "Start Challenge", exact: true })
    .click();
  async function answer(indices) {
    for (let i = 0; i < 5; i++) {
      await page.locator(".option").nth(indices[i]).click();
      await page
        .getByRole("button", { name: "Submit Answer", exact: true })
        .click();
      await page.locator(".feedback").waitFor();
      await page
        .getByRole("button", {
          name: i === 4 ? "See my result" : "Next question",
          exact: true,
        })
        .click();
    }
  }
  await answer([0, 2, 0, 3, 2]);
  await page
    .getByRole("heading", { name: "You’re getting there.", exact: true })
    .waitFor();
  await page.getByText(/0 coins earned/).waitFor();
  await page.screenshot({
    path: "verification/failed-attempt-360.png",
    fullPage: true,
  });
  await page
    .getByRole("button", { name: "Review Answers", exact: true })
    .click();
  assert.equal(await page.locator(".review-card").count(), 5);
  await page.screenshot({
    path: "verification/review-360.png",
    fullPage: true,
  });
  await page
    .getByRole("button", { name: "Back to result", exact: true })
    .click();
  await page
    .getByRole("button", { name: "Retry full challenge", exact: true })
    .click();
  await answer([1, 2, 0, 3, 2]);
  await page
    .getByRole("heading", { name: "Perfect score! +5 coins", exact: true })
    .waitFor();
  await page
    .getByRole("button", { name: "Practice again", exact: true })
    .click();
  await answer([1, 2, 0, 3, 2]);
  await page
    .getByRole("heading", {
      name: "Practice completed — reward already earned",
      exact: true,
    })
    .waitFor();
  const state = await page.evaluate(
    () => JSON.parse(localStorage.getItem("arivihan.demo.v1")).state,
  );
  assert.equal(
    state.ledger.reduce((n, l) => n + l.amount, 0),
    5,
  );
  assert.equal(state.ledger.length, 1);
  assert.equal(state.attempts.length, 3);
  assert.equal(
    state.events.filter((e) => e.name === "reels_reward_earned").length,
    1,
  );
  assert.ok(
    state.attempts.every(
      (a) =>
        a.questions.map((q) => q.reelId).join(",") ===
        "units-1,units-2,units-3,units-4,units-5",
    ),
  );
  await page.goto("http://127.0.0.1:4173/presenter?demo=1");
  await page.getByRole("button", { name: "Plus persona · Pro lock" }).click();
  await page.getByRole("button", { name: /Units & Measurements/ }).click();
  await page.getByRole("button", { name: "Play All", exact: true }).click();
  await page.getByRole("dialog", { name: "Unlock Reels with Pro" }).waitFor();
  await page.keyboard.press("Tab");
  await page.screenshot({ path: "verification/pro-lock.png", fullPage: true });
  await page.getByRole("button", { name: "Try simulated Pro upgrade" }).click();
  await page.getByRole("button", { name: "Play All", exact: true }).click();
  await page
    .getByRole("button", { name: "Tap to Play", exact: true })
    .waitFor();
  await page.goto("http://127.0.0.1:4173/presenter?demo=1");
  await page
    .getByRole("button", { name: "Non-subscribed · unavailable Reels" })
    .click();
  await page
    .getByRole("heading", { name: "Reels is a subscriber experience." })
    .waitFor();
  assert.equal(
    await page
      .getByRole("navigation")
      .getByRole("link", { name: "Reels", exact: true })
      .count(),
    0,
  );
  assert.deepEqual(errors, []);
  console.log(
    "PASS: five unseeded watches at 2x, failure 4/5, full retry 5/5, practice-only, exact mapping, one reward, Plus lock/upgrade and non-subscriber navigation.",
  );
} finally {
  await browser.close();
}
