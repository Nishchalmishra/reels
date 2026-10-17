import { chapters } from "../content";
import type { Chapter } from "../content";
import type { Snapshot } from "./contracts";
export function emptySnapshot(): Snapshot {
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
export function balance(s: Snapshot) {
  return s.ledger.reduce((n, l) => n + l.amount, 0);
}
export function qualified(
  s: Snapshot,
  id: string,
  catalogue: Chapter[] = chapters,
) {
  const r = catalogue.flatMap((c) => c.reels).find((r) => r.id === id);
  return (
    !!r &&
    (s.coverage[id] || []).reduce((n, [a, b]) => n + b - a, 0) >=
      r.duration * 0.8
  );
}
