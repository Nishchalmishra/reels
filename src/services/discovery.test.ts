import { describe, it, expect } from "vitest";
import {
  extendQueue,
  extendFocusQueue,
  chapterLessons,
  type Lesson,
} from "./discovery";
const library = Array.from(
  { length: 10 },
  (_, i) => ({ id: `r${i}`, chapterId: `c${i % 3}` }) as Lesson,
);
describe("discovery session queue", () => {
  it("preserves existing history and avoids the recent six while mixing chapters", () => {
    const queue = extendQueue(library, ["r0"], 1000, () => 0.37);
    expect(queue[0]).toBe("r0");
    for (let i = 1; i < queue.length; i++) {
      expect(queue.slice(Math.max(0, i - 6), i)).not.toContain(queue[i]);
      expect(library.find((l) => l.id === queue[i])!.chapterId).not.toBe(
        library.find((l) => l.id === queue[i - 1])!.chapterId,
      );
    }
  });
  it("replenishes a two-video library without immediate repeats and handles one or no clips", () => {
    expect(extendQueue(library.slice(0, 2), [], 20, () => 0)).toEqual(
      Array.from({ length: 20 }, (_, i) => `r${i % 2}`),
    );
    expect(extendQueue(library.slice(0, 1), [], 3)).toEqual(["r0", "r0", "r0"]);
    expect(extendQueue([], [])).toEqual([]);
  });
});

describe("chapter focus queue", () => {
  const lib = ["b3", "a1", "b1", "a2", "b2"].map(
    (id) => ({ id, chapterId: id[0] }) as Lesson,
  );
  it("orders a chapter by lesson id", () => {
    expect(chapterLessons(lib, "b").map((l) => l.id)).toEqual([
      "b1",
      "b2",
      "b3",
    ]);
  });
  it("continues in sequence after the current reel and wraps", () => {
    expect(extendFocusQueue(lib, "b", ["a1", "b2"], 4)).toEqual([
      "a1",
      "b2",
      "b3",
      "b1",
      "b2",
      "b3",
    ]);
  });
  it("starts at the first lesson when history is outside the chapter", () => {
    expect(extendFocusQueue(lib, "a", ["b1"], 3)).toEqual([
      "b1",
      "a1",
      "a2",
      "a1",
    ]);
  });
  it("falls back to the random mix for an unknown chapter", () => {
    expect(extendFocusQueue(lib, "zz", [], 3)).toHaveLength(3);
  });
});
