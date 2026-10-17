import { beforeEach, describe, it, expect, vi } from "vitest";
import type { LocalDemoAdapter as Adapter } from "./local";
const memory = () => {
  const map = new Map<string, string>();
  return {
    getItem: (k: string) => map.get(k) || null,
    setItem: (k: string, v: string) => {
      map.set(k, v);
    },
    removeItem: (k: string) => {
      map.delete(k);
    },
  };
};
vi.stubGlobal("localStorage", memory());
const { LocalDemoAdapter, calculateQuote, qualified, balance } =
  await import("./local");
const { chapters } = await import("../content");
let a: Adapter;
beforeEach(async () => {
  a = new LocalDemoAdapter(memory());
  a.latency = 0;
  await a.session({
    name: "Student",
    batch: "11",
    language: "English",
    persona: "Pro",
  });
});
async function watch(adapter: Adapter, chapter = "units") {
  for (const r of chapters.find((c) => c.id === chapter)!.reels)
    for (let t = 0; t < 15; t++)
      await adapter.progress(r.id, t, t + 1, 1, 1, true, true);
}
async function complete(adapter: Adapter, correct = true) {
  const q = await adapter.startQuiz("units");
  const answers = [1, 2, 0, 3, 2];
  for (let i = 0; i < 5; i++)
    await adapter.submitAnswer(q.id, i, !correct && i === 0 ? 0 : answers[i]);
  return adapter.finishAttempt(q.id);
}
describe("qualification", () => {
  it("requires five distinct reels in a single chapter and persists merged unique coverage", async () => {
    for (let i = 0; i < 5; i++)
      await a.progress("units-1", 0, 1, 1, 1, true, true);
    expect(qualified(await a.snapshot(), "units-1")).toBe(false);
    await watch(a, "motion");
    await expect(a.startQuiz("units")).rejects.toThrow();
    await watch(a);
    const s = await a.snapshot();
    expect(chapters[0].reels.every((r) => qualified(s, r.id))).toBe(true);
    const q = await a.startQuiz("units");
    expect(q.questions.map((q) => q.reelId)).toEqual(
      chapters[0].reels.map((r) => r.id),
    );
    expect(q.questions[0]).not.toHaveProperty("correct");
  });
  it("rejects seeking, hidden, paused and duplicate intervals; respects speed", async () => {
    await a.progress("units-1", 0, 17, 0.2, 1, true, true);
    await a.progress("units-1", 0, 15, 1, 1, false, true);
    await a.progress("units-1", 0, 15, 1, 1, true, false);
    expect((await a.snapshot()).coverage).toEqual({});
    await a.progress("units-1", 0, 2, 1, 2, true, true);
    await a.progress("units-1", 0, 2, 1, 2, true, true);
    expect((await a.snapshot()).coverage["units-1"]).toEqual([[0, 2]]);
  });
  it("locked and unavailable reels do not qualify", async () => {
    await a.preset("plus");
    await watch(a);
    expect((await a.snapshot()).coverage).toEqual({});
    await expect(a.startQuiz("units")).rejects.toThrow();
  });
});
describe("grading and persistence", () => {
  it("4/5 awards zero, a full successful retry awards exactly five once", async () => {
    await watch(a);
    const failed = await complete(a, false);
    expect(failed.score).toBe(4);
    expect(balance(await a.snapshot())).toBe(0);
    const passed = await complete(a);
    expect(passed.score).toBe(5);
    expect(balance(await a.snapshot())).toBe(5);
    await Promise.all([a.finishAttempt(passed.id), a.finishAttempt(passed.id)]);
    const practice = await complete(a);
    expect(practice.alreadyRewarded).toBe(true);
    expect(balance(await a.snapshot())).toBe(5);
    expect((await a.snapshot()).ledger).toHaveLength(1);
  });
  it("restores session and quiz and locks duplicate submission across refresh", async () => {
    const storage = memory();
    const first = new LocalDemoAdapter(storage);
    first.latency = 0;
    await first.preset("ready");
    await watch(first);
    const q = await first.startQuiz("units");
    await Promise.all([
      first.submitAnswer(q.id, 0, 1),
      first.submitAnswer(q.id, 0, 0),
    ]);
    const restored = new LocalDemoAdapter(storage);
    restored.latency = 0;
    expect((await restored.session())?.name).toBe("Ananya");
    expect((await restored.startQuiz("units")).id).toBe(q.id);
    expect((await restored.reviewAttempt(q.id)).answers[0]?.selected).toBe(1);
  });
  it("safely recovers malformed saved data", async () => {
    const storage = memory();
    storage.setItem("arivihan.demo.v1", "{broken");
    const restored = new LocalDemoAdapter(storage);
    restored.latency = 0;
    expect(balance(await restored.snapshot())).toBe(0);
    storage.setItem(
      "arivihan.demo.v1",
      JSON.stringify({
        version: 1,
        state: { coverage: { "units-1": null }, deferred: null },
      }),
    );
    const partial = new LocalDemoAdapter(storage);
    partial.latency = 0;
    expect((await partial.snapshot()).coverage).toEqual({});
  });
});
describe("integer coin redemption", () => {
  it.each([
    [5, 50000, 5, 100, 0],
    [500, 50000, 250, 5000, 250],
    [500, 200000, 500, 10000, 0],
    [500, 9900, 49, 980, 451],
  ])(
    "%i coins against %i paise",
    (available, subtotal, used, discount, remaining) => {
      const q = calculateQuote("p", subtotal, available, available);
      expect(q.coins).toBe(used);
      expect(q.discount).toBe(discount);
      expect(q.remaining).toBe(remaining);
      expect(q.payable).toBe(subtotal - discount);
    },
  );
  it("clamps insufficient funds, negative and noninteger requests", () => {
    expect(calculateQuote("p", 50000, 0, 500).coins).toBe(0);
    expect(calculateQuote("p", 50000, 5, 999).coins).toBe(5);
    expect(calculateQuote("p", 50000, 5, -1).coins).toBe(0);
    expect(calculateQuote("p", 50000, 5, 2.9).coins).toBe(2);
  });
  it("495 → 500 → redeem 250 → 250; quote/cancel have no debit and rank stays", async () => {
    await a.preset("ready");
    await watch(a);
    await complete(a);
    expect(balance(await a.snapshot())).toBe(500);
    const before = await a.recognition();
    const q = await a.quote("pro", 500);
    expect(q.coins).toBe(250);
    expect(balance(await a.snapshot())).toBe(500);
    const [o1, o2] = await Promise.all([
      a.confirmOrder("pro", 250, "order-1"),
      a.confirmOrder("pro", 250, "order-1"),
    ]);
    expect(o1.id).toBe(o2.id);
    expect(balance(await a.snapshot())).toBe(250);
    expect((await a.snapshot()).orders).toHaveLength(1);
    expect(await a.recognition()).toEqual(before);
    const state = await a.snapshot();
    expect(balance(state)).toBe(state.ledger.reduce((n, l) => n + l.amount, 0));
  });
  it("restores an already completed checkout rather than creating another debit", async () => {
    const storage = memory();
    const first = new LocalDemoAdapter(storage);
    first.latency = 0;
    await first.preset("ready");
    const q = await first.quote("pro", 250);
    await first.confirmOrder("pro", q.coins, q.requestId!);
    const restored = new LocalDemoAdapter(storage);
    restored.latency = 0;
    const resumed = await restored.quote("pro", 0);
    expect(resumed.requestId).toBe(q.requestId);
    await restored.confirmOrder("pro", 250, resumed.requestId!);
    expect((await restored.snapshot()).orders).toHaveLength(1);
    expect(balance(await restored.snapshot())).toBe(245);
  });
  it("presets never reapply seed on reading and no personal data or keys enter events", async () => {
    await a.preset("ready");
    await a.snapshot();
    await a.snapshot();
    expect(balance(await a.snapshot())).toBe(495);
    expect(JSON.stringify((await a.snapshot()).events)).not.toMatch(
      /Ananya|correct_index|explanation/,
    );
  });
});
