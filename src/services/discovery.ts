import type { Attempt } from "./contracts";
export type Lesson = {
  id: string;
  title: string;
  topic: string;
  chapterId: string;
  subject: string;
  concept: string;
  duration: number;
  grades: number[];
  languages: string[];
  source: string;
  poster: string;
  contentVersion: number;
};
export type DiscoveryAttempt = Attempt & { batchId: string };
export type DiscoveryState = {
  cycleId: string;
  pending: { id: string; status: string; attemptId: string | null }[];
};
export interface DiscoveryService {
  discoveryCatalogue(): Promise<Lesson[]>;
  discoveryState(): Promise<DiscoveryState>;
  discoveryProgress(
    id: string,
    from: number,
    to: number,
    wall: number,
    speed: number,
    visible: boolean,
    playing: boolean,
    playback?: { id: string; sequence: number },
  ): Promise<void>;
  discoveryOffer(): Promise<{
    ready: boolean;
    id?: string;
    diagnostic?: string;
  }>;
  // Tie the active quiz cycle to a chapter (null clears it).
  discoveryFocus(chapterId: string | null): Promise<void>;
  discoveryDefer(id: string): Promise<void>;
  discoveryStart(id: string): Promise<DiscoveryAttempt>;
  discoveryReview(id: string): Promise<DiscoveryAttempt>;
  discoverySubmit(
    id: string,
    index: number,
    selected: number,
  ): Promise<DiscoveryAttempt>;
  discoveryFinish(id: string): Promise<DiscoveryAttempt>;
}
// Called only when a session queue is extended, never during render.
export function extendQueue(
  lessons: Lesson[],
  history: string[],
  count = 12,
  random = Math.random,
): string[] {
  const result = [...history];
  for (let i = 0; i < count; i++) {
    const window = Math.min(6, Math.max(0, lessons.length - 1));
    const recent = result.slice(-window);
    let pool = lessons.filter((l) => !recent.includes(l.id));
    const last = lessons.find((l) => l.id === result.at(-1));
    const mixed = pool.filter((l) => l.chapterId !== last?.chapterId);
    if (mixed.length) pool = mixed;
    if (!pool.length) pool = lessons;
    if (!pool.length) break;
    result.push(pool[Math.floor(random() * pool.length)].id);
  }
  return result;
}

// Chapter focus: lessons of one chapter in lesson (id) order.
export function chapterLessons(lessons: Lesson[], chapterId: string): Lesson[] {
  return lessons
    .filter((l) => l.chapterId === chapterId)
    .sort((a, b) => a.id.localeCompare(b.id, undefined, { numeric: true }));
}
// Extends a focused queue in lesson order, continuing after the last entry and
// wrapping to the chapter's first lesson. Falls back to the random mix when the
// chapter has no lessons.
export function extendFocusQueue(
  lessons: Lesson[],
  chapterId: string,
  history: string[],
  count = 8,
): string[] {
  const chapter = chapterLessons(lessons, chapterId);
  if (!chapter.length) return extendQueue(lessons, history, count);
  const result = [...history];
  let pos = chapter.findIndex((l) => l.id === result.at(-1));
  for (let i = 0; i < count; i++) {
    pos = (pos + 1) % chapter.length;
    result.push(chapter[pos].id);
  }
  return result;
}
