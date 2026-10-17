import { describe, it, expect, vi } from "vitest";
const sdk = vi.hoisted(() => ({
  rpc: vi.fn(),
  auth: { getSession: vi.fn(), signInAnonymously: vi.fn() },
}));
vi.mock("@supabase/supabase-js", () => ({ createClient: () => sdk }));
import { SupabaseAdapter } from "./supabase";
describe("Supabase adapter trust boundary", () => {
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
