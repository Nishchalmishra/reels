import { describe, it, expect } from "vitest";
import { extendQueue, type Lesson } from "./discovery";
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
