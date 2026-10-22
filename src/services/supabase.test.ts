import { describe, it, expect, vi } from "vitest";
const sdk = vi.hoisted(() => ({
  rpc: vi.fn(),
  auth: { getSession: vi.fn(), signInAnonymously: vi.fn() },
}));
vi.mock("@supabase/supabase-js", () => ({ createClient: () => sdk }));
import { SupabaseAdapter } from "./supabase";
describe("Supabase adapter trust boundary", () => {
  it("keeps hosted catalogue URLs rather than replacing them with local paths", async () => {
    const row = {
      id: "lesson",
      source:
        "https://example.supabase.co/storage/v1/object/public/reels/video.mp4",
      poster:
        "https://example.supabase.co/storage/v1/object/public/reels/poster.jpg",
    };
    sdk.rpc.mockResolvedValue({ data: [row], error: null });
    const hosted = new SupabaseAdapter(
      "https://example.supabase.co",
      "sb_publishable_test",
      false,
      "hosted",
    );
    expect(await hosted.discoveryCatalogue()).toEqual([row]);
    const local = new SupabaseAdapter(
      "https://example.supabase.co",
      "sb_publishable_test",
      false,
      "local",
    );
    expect(await local.discoveryCatalogue()).toEqual([
      { ...row, source: "/media/lesson", poster: "/posters/lesson.jpg" },
    ]);
  });
  it("fails clearly on missing or local URLs in hosted mode without substituting media", async () => {
    const hosted = new SupabaseAdapter(
      "https://example.supabase.co",
      "sb_publishable_test",
      false,
      "hosted",
    );
    for (const source of [
      "/media/lesson",
      "http://127.0.0.1:8766/media/lesson",
      "https://localhost/media/lesson",
    ]) {
      sdk.rpc.mockResolvedValue({
        data: [
          {
            id: "lesson",
            source,
            poster: "https://example.supabase.co/poster.jpg",
          },
        ],
        error: null,
      });
      await expect(hosted.discoveryCatalogue()).rejects.toThrow(
        /no valid hosted media URL/,
      );
    }
  });
  it("rejects privileged browser configuration", () => {
    expect(
      () =>
        new SupabaseAdapter("https://example.supabase.co", "sb_secret_private"),
    ).toThrow(/Privileged/);
    const payload = btoa(JSON.stringify({ role: "service_role" }));
    expect(
      () =>
        new SupabaseAdapter("https://example.supabase.co", `x.${payload}.y`),
    ).toThrow(/validation failed/);
  });
  it("submits choices/idempotency only and preserves server outcomes without mock fallback", async () => {
    const a = new SupabaseAdapter(
      "https://example.supabase.co",
      "sb_publishable_test",
    );
    sdk.rpc.mockResolvedValue({
      data: { id: "attempt", score: 0 },
      error: null,
    });
    await a.submitAnswer("attempt", 2, 1);
    expect(sdk.rpc).toHaveBeenLastCalledWith("ar_reels_submit", {
      p_attempt: "attempt",
      p_index: 2,
      p_selected: 1,
    });
    await a.finishAttempt("attempt");
    expect(sdk.rpc).toHaveBeenLastCalledWith("ar_reels_finish", {
      p_attempt: "attempt",
    });
    await a.confirmOrder("pro", 5, "request");
    expect(sdk.rpc).toHaveBeenLastCalledWith("ar_reels_confirm", {
      p_product: "pro",
      p_coins: 5,
      p_request: "request",
    });
    sdk.rpc.mockResolvedValue({
      data: null,
      error: { code: "42501", message: "Access denied" },
    });
    await expect(a.catalogue()).rejects.toThrow(/Supabase: Access denied/);
  });
  it("reports disabled anonymous Auth and blocks live presenter minting", async () => {
    const a = new SupabaseAdapter(
      "https://example.supabase.co",
      "sb_publishable_test",
    );
    sdk.auth.getSession.mockResolvedValue({
      data: { session: null },
      error: null,
    });
    sdk.auth.signInAnonymously.mockResolvedValue({
      data: {},
      error: { code: "anonymous_provider_disabled", message: "disabled" },
    });
    await expect(
      a.session({
        name: "Test",
        batch: "11",
        language: "English",
        persona: "Pro",
      }),
    ).rejects.toThrow(/Anonymous sign-in is disabled/);
    await expect(a.preset("ready")).rejects.toThrow(/local-demo-only/);
    await expect(a.answerGuide()).rejects.toThrow(/unavailable in live/);
  });
});
