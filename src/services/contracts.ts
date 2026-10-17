import type { Chapter, Reel, Product } from "../content";
export type Profile = {
  id: string;
  name: string;
  batch: string;
  language: string;
  persona: "Pro" | "Plus" | "None";
};
export type Answer = { selected: number; correct: number; explanation: string };
export type Question = {
  id: string;
  reelId: string;
  prompt: string;
  options: string[];
};
export type Attempt = {
  id: string;
  challengeId: string;
  chapterId: string;
  questions: Question[];
  answers: (Answer | null)[];
  finished: boolean;
  score: number;
  rewarded: boolean;
  alreadyRewarded: boolean;
};
export type Ledger = {
  id: string;
  amount: number;
  reason: string;
  ref: string;
  at: string;
};
export type Event = {
  id: string;
  name: string;
  userId: string;
  sessionId: string;
  at: string;
  demo: boolean;
  presenter: boolean;
  content_type: string;
  source: string;
  chapterId?: string;
  reelId?: string;
  challengeId?: string;
  attemptId?: string;
};
export type Quote = {
  requestId?: string;
  productId: string;
  subtotal: number;
  max: number;
  coins: number;
  discount: number;
  payable: number;
  remaining: number;
};
export type Order = { id: string; quote: Quote; at: string };
export type Snapshot = {
  profile: Profile | null;
  coverage: Record<string, [number, number][]>;
  positions: Record<string, number>;
  checkouts: Record<string, string>;
  attempts: Attempt[];
  ledger: Ledger[];
  orders: Order[];
  events: Event[];
  deferred: string[];
  offered: string[];
  likes: string[];
  saved: string[];
  downloads: string[];
  mute: boolean;
  lastReel: string | null;
  presenter: boolean;
  scenario: string;
};
export interface DemoService {
  snapshot(): Promise<Snapshot>;
  session(profile?: Omit<Profile, "id">): Promise<Profile | null>;
  catalogue(): Promise<Chapter[]>;
  products(): Promise<Product[]>;
  progress(
    reelId: string,
    from: number,
    to: number,
    wallSeconds: number,
    speed: number,
    visible: boolean,
    playing: boolean,
  ): Promise<void>;
  checkpoint(reelId: string, position: number): Promise<void>;
  startQuiz(chapterId: string): Promise<Attempt>;
  submitAnswer(
    attemptId: string,
    index: number,
    selected: number,
  ): Promise<Attempt>;
  finishAttempt(id: string): Promise<Attempt>;
  reviewAttempt(id: string): Promise<Attempt>;
  wallet(): Promise<{ balance: number; ledger: Ledger[] }>;
  quote(productId: string, coins: number): Promise<Quote>;
  confirmOrder(
    productId: string,
    coins: number,
    requestId: string,
  ): Promise<Order>;
  recognition(): Promise<{ name: string; earned: number; current: boolean }[]>;
  analytics(name: string, data?: Partial<Event>): Promise<void>;
  preference(key: "likes" | "saved" | "downloads", id: string): Promise<void>;
  mute(value: boolean): Promise<void>;
  lastReel(id: string): Promise<void>;
  invitation(chapterId: string, defer: boolean): Promise<void>;
  preset(name: string): Promise<void>;
  answerGuide(): Promise<string[]>;
}
export type MediaSourceAdapter = {
  reel: Reel;
  kind: "animated" | "mp4";
  src?: string;
};
