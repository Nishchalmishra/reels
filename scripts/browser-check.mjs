import { chromium } from "playwright";
import fs from "node:fs";
fs.mkdirSync("verification", { recursive: true });
const browser = await chromium.launch({ channel: "chrome", headless: true });
const page = await browser.newPage({
  viewport: { width: 390, height: 844 },
  reducedMotion: "reduce",
});
page.setDefaultTimeout(12000);
const errors = [];
page.on("pageerror", (e) => errors.push(e.message));
await page.goto("http://127.0.0.1:4173/");
await page.getByRole("button", { name: "Continue with demo student" }).click();
await page
  .getByRole("heading", { name: "A clearer idea.", exact: false })
  .waitFor();
await page.screenshot({ path: "verification/home-390.png", fullPage: true });
for (const width of [360, 390, 430]) {
  await page.setViewportSize({ width, height: 844 });
  if (
    await page.evaluate(() => document.documentElement.scrollWidth > innerWidth)
  )
    throw Error(`Overflow ${width}`);
}
await page.setViewportSize({ width: 390, height: 844 });
await page.getByRole("button", { name: "Chemistry", exact: true }).click();
await page.getByRole("button", { name: /Structure of Atom/ }).click();
await page.getByRole("dialog").waitFor();
await page.getByRole("button", { name: "Close dialog" }).click();
await page.getByRole("button", { name: "Physics", exact: true }).click();
await page.getByRole("button", { name: /Units & Measurements/ }).click();
await page.getByRole("button", { name: "Play All", exact: true }).click();
await page.getByRole("button", { name: "Tap to Play", exact: true }).click();
await page.waitForTimeout(1600);
await page.getByRole("button", { name: "Pause", exact: true }).click();
await page.screenshot({ path: "verification/player-390.png", fullPage: true });
await page.getByRole("button", { name: "Notes", exact: true }).click();
const downloadPromise = page.waitForEvent("download");
await page
  .getByRole("button", { name: "Download teaching note", exact: true })
  .click();
const download = await downloadPromise;
await download.saveAs("verification/" + download.suggestedFilename());
await page.getByRole("button", { name: "Close dialog" }).click();
console.log("PASS playback and note download");
await page.goto("http://127.0.0.1:4173/presenter?demo=1");
await page
  .getByRole("button", { name: "Presentation-ready · 495 seeded coins" })
  .click();
await page.getByRole("button", { name: "Tap to Play", exact: true }).click();
try {
  await page
    .getByRole("dialog", { name: "Ready to test what you learned?" })
    .waitFor({ timeout: 24000 });
} catch (e) {
  await page.screenshot({ path: "verification/failure.png", fullPage: true });
  console.log(await page.locator("body").innerText());
  console.log(
    await page.evaluate(() => localStorage.getItem("arivihan.demo.v1")),
  );
  throw e;
}
console.log("PASS watch qualification");
await page.screenshot({
  path: "verification/challenge-invitation.png",
  fullPage: true,
});
await page.getByRole("button", { name: "Later", exact: true }).click();
await page
  .getByRole("button", { name: "Resume Challenge", exact: true })
  .click();
const correct = [2, 3, 1, 4, 3];
for (let i = 0; i < 5; i++) {
  await page.locator(".quiz h1").waitFor();
  const option = page.locator(".option").nth(correct[i] - 1);
  await option.click();
  await page
    .getByRole("button", { name: "Submit Answer", exact: true })
    .click();
  await page
    .getByRole("heading", { name: "That’s right!", exact: true })
    .waitFor();
  if (i === 1) {
    await page.reload();
    await page.locator(".quiz").waitFor();
    i++;
    await page
      .locator(".option")
      .nth(correct[i] - 1)
      .click();
    await page
      .getByRole("button", { name: "Submit Answer", exact: true })
      .click();
  }
  await page
    .getByRole("button", {
      name: i === 4 ? "See my result" : "Next question",
      exact: true,
    })
    .click();
}
await page
  .getByRole("heading", { name: "Perfect score! +5 coins", exact: true })
  .waitFor();
await page.screenshot({
  path: "verification/perfect-result.png",
  fullPage: true,
});
await page.getByRole("button", { name: "Go to Wallet", exact: true }).click();
await page.locator(".wallet-number").filter({ hasText: "500" }).waitFor();
await page.getByRole("link", { name: "Explore the demo store" }).click();
await page.getByRole("link", { name: /Arivihan Pro/ }).click();
await page.getByRole("checkbox", { name: "Use available coins" }).check();
await page.getByText(/450\.00/).waitFor();
console.log("PASS 250-coin redemption quote");
await page.screenshot({
  path: "verification/redemption-preview.png",
  fullPage: true,
});
await page.getByRole("button", { name: "Confirm simulated order" }).click();
await page
  .getByRole("heading", { name: "Your demo order is recorded." })
  .waitFor();
await page.getByRole("link", { name: "View wallet", exact: true }).click();
try {
  await page.locator(".wallet-number").filter({ hasText: "250" }).waitFor();
} catch (e) {
  await page.screenshot({
    path: "verification/wallet-failure.png",
    fullPage: true,
  });
  console.log(
    await page.evaluate(() => localStorage.getItem("arivihan.demo.v1")),
  );
  throw e;
}
await page.reload();
await page.locator(".wallet-number").filter({ hasText: "250" }).waitFor();
await page.screenshot({
  path: "verification/wallet-after-redemption.png",
  fullPage: true,
});
await page.goto("http://127.0.0.1:4173/downloads");
await page.goto("http://127.0.0.1:4173/chapter/motion");
await page
  .getByRole("heading", { name: "Laws of Motion", exact: true })
  .waitFor();
await page.screenshot({ path: "verification/chapter-390.png", fullPage: true });
for (const width of [360, 430]) {
  await page.setViewportSize({ width, height: 844 });
  await page.goto("http://127.0.0.1:4173/chapter/units");
  await page.getByRole("button", { name: "Play All", exact: true }).waitFor();
  if (
    await page.evaluate(() => document.documentElement.scrollWidth > innerWidth)
  )
    throw Error("Chapter overflow " + width);
  await page.screenshot({
    path: `verification/chapter-${width}.png`,
    fullPage: true,
  });
}
await page.setViewportSize({ width: 390, height: 844 });
await page.goto("http://127.0.0.1:4173/recognition");
await page.screenshot({ path: "verification/cohort.png", fullPage: true });
await page.setViewportSize({ width: 1280, height: 900 });
await page.goto("http://127.0.0.1:4173/");
await page.screenshot({ path: "verification/desktop.png", fullPage: true });
if (errors.length) throw Error(errors.join("\n"));
console.log(
  "PASS: onboarding, discovery, coming soon, playback, note download, qualification, Later/resume, quiz refresh, 5/5, wallet, redemption, persistence, direct routes and mobile overflow.",
);
await browser.close();
