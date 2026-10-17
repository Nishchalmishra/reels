import { chapters, products, challenges } from "../content";
import type {
  Answer,
  Attempt,
  DemoService,
  Event,
  Profile,
  Quote,
  Snapshot,
} from "./contracts";
const KEY = "arivihan.demo.v1";
const keys = [
  [
    "What is the SI base unit of length?",
    ["Centimetre", "Metre", "Kilometre", "Millimetre"],
    1,
    "The metre is the SI base unit of length.",
  ],
  [
    "250 cm is equal to how many metres?",
    ["25 m", "0.25 m", "2.5 m", "250 m"],
    2,
    "Divide centimetres by 100: 250 / 100 = 2.5 m.",
  ],
  [
    "What is the dimensional formula of speed?",
    ["L T⁻¹", "L T⁻²", "M L T⁻¹", "L² T⁻¹"],
    0,
    "Speed = distance / time, so its dimensions are L T⁻¹.",
  ],
  [
    "A ruler has 10 divisions in 1 cm. What is its least count?",
    ["10 mm", "1 cm", "0.1 mm", "1 mm"],
    3,
    "1 cm / 10 = 0.1 cm = 1 mm.",
  ],
  [
    "A 10 cm length has uncertainty 0.1 cm. What is its percentage error?",
    ["10%", "0.1%", "1%", "100%"],
    2,
    "(0.1 / 10) × 100 = 1%.",
  ],
  [
    "With zero net force, a moving object has…",
    [
      "Constant velocity",
      "Increasing speed",
      "Zero velocity always",
      "Increasing acceleration",
    ],
    0,
    "Zero net force means zero acceleration, so velocity stays constant.",
  ],
  [
    "What net force accelerates 2 kg at 3 m/s²?",
    ["1.5 N", "5 N", "6 N", "9 N"],
    2,
    "F = ma = 2 × 3 = 6 N.",
  ],
  [
    "Action and reaction forces act on…",
    [
      "The same object",
      "Different objects",
      "Only stationary objects",
      "Only falling objects",
    ],
    1,
    "Third-law pairs act on different interacting objects.",
  ],
  [
    "What is the momentum of 2 kg moving at 4 m/s?",
    ["2 kg·m/s", "4 kg·m/s", "6 kg·m/s", "8 kg·m/s"],
    3,
    "p = mv = 2 × 4 = 8 kg·m/s.",
  ],
  [
    "A 3 N force acts for 2 s. What impulse does it give?",
    ["6 N·s", "1.5 N·s", "5 N·s", "9 N·s"],
    0,
    "Impulse = FΔt = 3 × 2 = 6 N·s.",
  ],
] as [string, string[], number, string][];
export function mergeIntervals(
  intervals: [number, number][],
): [number, number][] {
  const out: [number, number][] = [];
  for (const [a, b] of intervals.sort((x, y) => x[0] - y[0])) {
    const last = out.at(-1);
    if (last && a <= last[1] + 0.025) last[1] = Math.max(last[1], b);
    else out.push([a, b]);
  }
  return out;
}
export function covered(s: Snapshot, id: string) {
  return (s.coverage[id] || []).reduce((n, [a, b]) => n + b - a, 0);
}
export function qualified(s: Snapshot, id: string) {
  const reel = chapters.flatMap((c) => c.reels).find((r) => r.id === id);
  return !!reel && covered(s, id) >= reel.duration * 0.8;
}
export function balance(s: Snapshot) {
  return s.ledger.reduce((n, l) => n + l.amount, 0);
}
export function calculateQuote(
  productId: string,
  subtotal: number,
  available: number,
  requested: number,
): Quote {
  const max = Math.max(
    0,
    Math.min(available, Math.floor(Math.floor((subtotal * 10) / 100) / 20)),
  );
  const coins = Math.max(
    0,
    Math.min(max, Math.floor(Number.isFinite(requested) ? requested : 0)),
  );
  return {
    productId,
    subtotal,
    max,
    coins,
    discount: coins * 20,
    payable: subtotal - coins * 20,
    remaining: available - coins,
  };
}
function fresh(): Snapshot {
  return {
    profile: null,
    coverage: {},
    positions: {},
    checkouts: {},
    attempts: [],
    ledger: [],
    orders: [],
    events: [],
    deferred: [],
    offered: [],
    likes: [],
    saved: [],
    downloads: [],
    mute: true,
    lastReel: null,
    presenter: false,
    scenario: "fresh",
  };
}
const uid = () => crypto.randomUUID();
function validState(s: Snapshot): boolean {
  const stringLists = [s.deferred, s.offered, s.likes, s.saved, s.downloads];
  if (
    !stringLists.every(
      (list) =>
        Array.isArray(list) && list.every((id) => typeof id === "string"),
    )
  )
    return false;
  if (
    s.profile &&
    (!["Pro", "Plus", "None"].includes(s.profile.persona) ||
      typeof s.profile.name !== "string" ||
      typeof s.profile.batch !== "string" ||
      typeof s.profile.language !== "string")
  )
    return false;
  if (
    !s.coverage ||
    typeof s.coverage !== "object" ||
    !Object.values(s.coverage).every(
      (list) =>
        Array.isArray(list) &&
        list.every(
          (interval) =>
            Array.isArray(interval) &&
            interval.length === 2 &&
            interval.every(Number.isFinite) &&
            interval[0] >= 0 &&
            interval[1] >= interval[0],
        ),
    )
  )
    return false;
  if (
    !s.positions ||
    !Object.values(s.positions).every(Number.isFinite) ||
    !s.checkouts ||
    !Object.values(s.checkouts).every((id) => typeof id === "string")
  )
    return false;
  if (
    !Array.isArray(s.ledger) ||
    !s.ledger.every(
      (l) =>
        Number.isInteger(l.amount) &&
        typeof l.ref === "string" &&
        typeof l.reason === "string",
    )
  )
    return false;
  if (
    !Array.isArray(s.attempts) ||
    !s.attempts.every(
      (a) =>
        typeof a.id === "string" &&
        Array.isArray(a.questions) &&
        a.questions.length === 5 &&
        a.questions.every(
          (q) =>
            typeof q.prompt === "string" &&
            Array.isArray(q.options) &&
            q.options.length === 4,
        ) &&
        Array.isArray(a.answers) &&
        a.answers.length === 5 &&
        a.answers.every(
          (x) =>
            x === null ||
            (Number.isInteger(x.selected) &&
              Number.isInteger(x.correct) &&
              typeof x.explanation === "string"),
        ),
    )
  )
    return false;
  return (
    Array.isArray(s.orders) &&
    s.orders.every(
      (o) =>
        typeof o.id === "string" &&
        !!o.quote &&
        Number.isInteger(o.quote.coins),
    ) &&
    Array.isArray(s.events)
  );
}
export class LocalDemoAdapter implements DemoService {
  async products() {
    await this.wait();
    return structuredClone(products);
  }
  private s: Snapshot;
  private sessionId = uid();
  latency = 40;
  errorNext = false;
  constructor(
    private storage: Pick<
      Storage,
      "getItem" | "setItem" | "removeItem"
    > = localStorage,
  ) {
    try {
      const raw = JSON.parse(storage.getItem(KEY) || "null");
      if (raw?.version === 1 && raw.state) {
        this.s = { ...fresh(), ...raw.state };
        if (!validState(this.s)) throw Error("Malformed state");
      } else this.s = fresh();
    } catch {
      this.s = fresh();
      this.save();
    }
  }
  private save() {
    this.storage.setItem(KEY, JSON.stringify({ version: 1, state: this.s }));
  }
  private async wait() {
    await new Promise((r) => setTimeout(r, this.latency));
    if (this.errorNext) {
      this.errorNext = false;
      throw Error("Could not load this time. Please try again.");
    }
  }
  async snapshot() {
    await this.wait();
    return structuredClone(this.s);
  }
  async session(p?: Omit<Profile, "id">) {
    await this.wait();
    if (p) {
      this.s.profile = { ...p, id: "demo-student" };
      this.save();
    }
    return this.s.profile;
  }
  async catalogue() {
    await this.wait();
    return this.s.scenario === "empty" ? [] : structuredClone(chapters);
  }
  async analytics(name: string, data: Partial<Event> = {}) {
    this.s.events.push({
      ...data,
      id: uid(),
      name,
      userId: this.s.profile?.id || "anonymous",
      sessionId: this.sessionId,
      at: new Date().toISOString(),
      demo: true,
      presenter: this.s.presenter,
      content_type: data.content_type || "educational",
      source: data.source || "student",
    });
    this.save();
  }
  async progress(
    id: string,
    from: number,
    to: number,
    wall: number,
    speed: number,
    visible: boolean,
    playing: boolean,
  ) {
    const r = chapters.flatMap((c) => c.reels).find((x) => x.id === id);
    if (
      !r ||
      this.s.profile?.persona !== "Pro" ||
      !visible ||
      !playing ||
      from < 0 ||
      to > r.duration + 0.05 ||
      to <= from ||
      wall <= 0 ||
      wall > 2 ||
      to - from > wall * speed + 0.15
    )
      return;
    const was = qualified(this.s, id);
    const before = covered(this.s, id);
    this.s.positions[id] = to;
    this.s.coverage[id] = mergeIntervals([
      ...(this.s.coverage[id] || []),
      [from, Math.min(to, r.duration)],
    ]);
    this.save();
    if (!was && qualified(this.s, id))
      await this.analytics("reels_video_completed", {
        reelId: id,
        chapterId: r.chapterId,
      });
    else if (Math.floor(before / 5) !== Math.floor(covered(this.s, id) / 5))
      await this.analytics("reels_watch_progress", {
        reelId: id,
        chapterId: r.chapterId,
      });
  }
  async invitation(chapterId: string, defer: boolean) {
    const list = defer ? this.s.deferred : this.s.offered;
    if (!list.includes(chapterId)) {
      list.push(chapterId);
      await this.analytics(
        defer ? "reels_quiz_deferred" : "reels_quiz_offered",
        { chapterId },
      );
    }
    this.save();
  }
  async checkpoint(id: string, position: number) {
    const r = chapters.flatMap((c) => c.reels).find((r) => r.id === id);
    if (r && Number.isFinite(position)) {
      this.s.positions[id] = Math.max(0, Math.min(r.duration, position));
      this.save();
    }
  }
  async startQuiz(chapterId: string) {
    await this.wait();
    const c = chapters.find((c) => c.id === chapterId);
    if (
      !c ||
      this.s.profile?.persona !== "Pro" ||
      !c.reels.every((r) => qualified(this.s, r.id))
    )
      throw Error(
        "Watch at least 80% of all five lessons to unlock this challenge.",
      );
    const old = this.s.attempts.find(
      (a) => a.chapterId === chapterId && !a.finished,
    );
    if (old) return structuredClone(old);
    const offset = chapterId === "units" ? 0 : 5;
    const a: Attempt = {
      id: uid(),
      chapterId,
      challengeId: c.challengeId,
      questions: challenges
        .find((x) => x.id === c.challengeId)!
        .pairs.map((pair, i) => ({
          id: pair.questionId,
          reelId: pair.reelId,
          prompt: keys[offset + i][0],
          options: keys[offset + i][1],
        })),
      answers: Array(5).fill(null),
      finished: false,
      score: 0,
      rewarded: false,
      alreadyRewarded: this.s.ledger.some((l) => l.ref === c.challengeId),
    };
    this.s.attempts.push(a);
    await this.analytics("reels_quiz_started", {
      chapterId,
      challengeId: c.challengeId,
      attemptId: a.id,
    });
    this.save();
    return structuredClone(a);
  }
  async submitAnswer(id: string, index: number, selected: number) {
    await this.wait();
    const a = this.s.attempts.find((a) => a.id === id);
    if (!a || a.finished) throw Error("Attempt is unavailable.");
    if (a.answers[index]) return structuredClone(a);
    if (
      index !== a.answers.filter(Boolean).length ||
      !Number.isInteger(selected) ||
      selected < 0 ||
      selected > 3
    )
      throw Error("Invalid answer.");
    const key = keys[(a.chapterId === "units" ? 0 : 5) + index];
    a.answers[index] = {
      selected,
      correct: key[2],
      explanation: key[3],
    } satisfies Answer;
    await this.analytics("reels_quiz_answer_submitted", {
      attemptId: id,
      chapterId: a.chapterId,
      challengeId: a.challengeId,
      reelId: a.questions[index].reelId,
    });
    this.save();
    return structuredClone(a);
  }
  async finishAttempt(id: string) {
    await this.wait();
    const a = this.s.attempts.find((a) => a.id === id);
    if (!a) throw Error("Attempt not found");
    if (a.finished) return structuredClone(a);
    if (a.answers.some((x) => !x))
      throw Error("Complete all five questions first.");
    a.score = a.answers.filter((x) => x?.selected === x?.correct).length;
    a.alreadyRewarded = this.s.ledger.some((l) => l.ref === a.challengeId);
    a.rewarded = a.score === 5 && !a.alreadyRewarded;
    a.finished = true;
    if (a.rewarded) {
      this.s.ledger.push({
        id: uid(),
        amount: 5,
        reason: "Perfect challenge",
        ref: a.challengeId,
        at: new Date().toISOString(),
      });
      await this.analytics("reels_reward_earned", {
        attemptId: id,
        challengeId: a.challengeId,
        chapterId: a.chapterId,
      });
    }
    await this.analytics("reels_quiz_completed", {
      attemptId: id,
      chapterId: a.chapterId,
      challengeId: a.challengeId,
    });
    this.save();
    return structuredClone(a);
  }
  async reviewAttempt(id: string) {
    await this.wait();
    const a = this.s.attempts.find((a) => a.id === id);
    if (!a) throw Error("Attempt not found");
    return structuredClone(a);
  }
  async wallet() {
    await this.wait();
    return { balance: balance(this.s), ledger: structuredClone(this.s.ledger) };
  }
  async quote(productId: string, coins: number) {
    await this.wait();
    const p = products.find((p) => p.id === productId);
    if (!p) throw Error("Product not found");
    const requestId = this.s.checkouts[p.id] || uid();
    this.s.checkouts[p.id] = requestId;
    const completed = this.s.orders.find((o) => o.id === requestId);
    const q = completed
      ? { ...completed.quote, requestId }
      : { ...calculateQuote(p.id, p.price, balance(this.s), coins), requestId };
    await this.analytics("reels_redemption_quoted", { content_type: "store" });
    return q;
  }
  async confirmOrder(productId: string, coins: number, requestId: string) {
    await this.wait();
    const old = this.s.orders.find((o) => o.id === requestId);
    if (old) return structuredClone(old);
    const p = products.find((p) => p.id === productId);
    if (!p) throw Error("Product unavailable");
    const q = calculateQuote(productId, p.price, balance(this.s), coins);
    if (q.coins !== coins)
      throw Error("Balance changed. Please refresh your quote.");
    this.s.checkouts[productId] = requestId;
    q.requestId = requestId;
    const order = { id: requestId, quote: q, at: new Date().toISOString() };
    this.s.orders.push(order);
    if (coins)
      this.s.ledger.push({
        id: uid(),
        amount: -coins,
        reason: "Demo redemption",
        ref: requestId,
        at: order.at,
      });
    await this.analytics("reels_redemption_confirmed", {
      content_type: "store",
    });
    this.save();
    return structuredClone(order);
  }
  async recognition() {
    await this.wait();
    return [
      { name: "Aarav", earned: 560, current: false },
      { name: "Meera", earned: 520, current: false },
      {
        name: this.s.profile?.name || "You",
        earned: this.s.ledger
          .filter((l) => l.amount > 0)
          .reduce((n, l) => n + l.amount, 0),
        current: true,
      },
      { name: "Kabir", earned: 30, current: false },
    ].sort((a, b) => b.earned - a.earned);
  }
  async preference(key: "likes" | "saved" | "downloads", id: string) {
    const list = this.s[key];
    this.s[key] = list.includes(id)
      ? list.filter((x) => x !== id)
      : [...list, id];
    this.save();
  }
  async mute(value: boolean) {
    this.s.mute = value;
    this.save();
  }
  async lastReel(id: string) {
    this.s.lastReel = id;
    this.save();
  }
  async preset(name: string) {
    this.s = fresh();
    this.s.presenter = true;
    this.s.scenario = name;
    this.s.profile = {
      id: "demo-student",
      name: "Ananya",
      batch: "Class 11 · NEET",
      language: "English",
      persona: name === "plus" ? "Plus" : name === "none" ? "None" : "Pro",
    };
    if (["ready", "failed", "rewarded"].includes(name)) {
      this.s.coverage = Object.fromEntries(
        chapters[0].reels
          .slice(0, name === "ready" ? 4 : 5)
          .map((r) => [r.id, [[0, r.duration]]]),
      );
      this.s.lastReel = "units-5";
    }
    if (name === "ready")
      this.s.ledger.push({
        id: uid(),
        amount: 495,
        reason: "Presentation seed · demo data",
        ref: "seed-495",
        at: new Date().toISOString(),
      });
    this.save();
    await this.analytics("presenter_preset", {
      source: "presenter",
      content_type: "demo",
    });
    if (name === "failed" || name === "rewarded") {
      const a = await this.startQuiz("units");
      for (let i = 0; i < 5; i++)
        await this.submitAnswer(
          a.id,
          i,
          name === "failed" ? (keys[i][2] + 1) % 4 : keys[i][2],
        );
      await this.finishAttempt(a.id);
    }
    if (name === "error") this.errorNext = true;
  }
  async answerGuide() {
    return keys.map(
      (k, i) =>
        `${i < 5 ? "Units" : "Motion"} ${(i % 5) + 1}: ${k[1][k[2]]} — ${k[3]}`,
    );
  }
}
export const service: DemoService = new LocalDemoAdapter();
