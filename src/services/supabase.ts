import { createClient, type SupabaseClient } from "@supabase/supabase-js";
import type { Chapter, Product } from "../content";
import type {
  DemoService,
  Event,
  Profile,
  Snapshot,
  Attempt,
  Quote,
  Order,
} from "./contracts";
import { emptySnapshot, balance } from "./state";
import type {
  DiscoveryService,
  DiscoveryAttempt,
  DiscoveryState,
  Lesson,
} from "./discovery";
export class SupabaseAdapter implements DemoService, DiscoveryService {
  discoveryCatalogue() {
    return this.rpc<Lesson[]>("ar_reels_discovery_catalogue").then((rows) =>
      rows.map((l) => ({
        ...l,
        source:
          this.mediaMode === "local"
            ? `/media/${l.id}`
            : this.hostedUrl(l.source, l.id),
        poster:
          this.mediaMode === "local"
            ? `/posters/${l.id}.jpg`
            : this.hostedUrl(l.poster, l.id),
      })),
    );
  }
  discoveryState() {
    return this.rpc<DiscoveryState>("ar_reels_discovery_state");
  }
  async discoveryProgress(
    id: string,
    from: number,
    to: number,
    wall: number,
    speed: number,
    visible: boolean,
    playing: boolean,
    playback?: { id: string; sequence: number },
  ) {
    await this.rpc(
      playback ? "ar_reels_discovery_playback" : "ar_reels_discovery_progress",
      {
        ...(playback
          ? { p_session: playback.id, p_sequence: playback.sequence }
          : {}),
        p_reel: id,
        p_from: from,
        p_to: to,
        p_wall: wall,
        p_speed: speed,
        p_visible: visible,
        p_playing: playing,
      },
    );
  }
  discoveryOffer() {
    return this.rpc<{ ready: boolean; id?: string; diagnostic?: string }>(
      "ar_reels_discovery_offer",
    );
  }
  async discoveryDefer(id: string) {
    await this.rpc("ar_reels_discovery_defer", { p_batch: id });
  }
  discoveryStart(id: string) {
    return this.rpc<DiscoveryAttempt>("ar_reels_discovery_start", {
      p_batch: id,
    });
  }
  discoveryReview(id: string) {
    return this.rpc<DiscoveryAttempt>("ar_reels_discovery_review", {
      p_attempt: id,
    });
  }
  discoverySubmit(id: string, index: number, selected: number) {
    return this.rpc<DiscoveryAttempt>("ar_reels_discovery_submit", {
      p_attempt: id,
      p_index: index,
      p_selected: selected,
    });
  }
  discoveryFinish(id: string) {
    return this.rpc<DiscoveryAttempt>("ar_reels_discovery_finish", {
      p_attempt: id,
    });
  }
  readonly client: SupabaseClient;
  private mediaMode: "hosted" | "local";
  private hostedUrl(value: string, id: string): string {
    try {
      const parsed = new URL(value);
      if (
        parsed.protocol === "https:" &&
        !parsed.username &&
        !parsed.password &&
        !["localhost", "127.0.0.1", "[::1]"].includes(parsed.hostname) &&
        !parsed.hostname.endsWith(".localhost")
      )
        return value;
    } catch {}
    return this.fail(
      `Lesson ${id} has no valid hosted media URL. Publish the lesson assets or explicitly select VITE_MEDIA_MODE=local for local development.`,
    );
  }
  private sessionId: string = crypto.randomUUID();
  private test: boolean;
  constructor(
    url: string,
    key: string,
    test = false,
    mediaMode: "hosted" | "local" = import.meta.env.VITE_MEDIA_MODE === "local"
      ? "local"
      : "hosted",
  ) {
    this.mediaMode = mediaMode;
    if (!url || !key)
      throw Error(
        "Live Supabase mode requires a project URL and publishable key.",
      );
    if (key.startsWith("sb_secret_"))
      throw Error(
        "Privileged Supabase keys are forbidden in browser configuration.",
      );
    if (key.split(".").length === 3) {
      try {
        const role = JSON.parse(atob(key.split(".")[1])).role;
        if (role !== "anon")
          throw Error("Use an anon/publishable key, never a privileged key.");
      } catch (e) {
        throw Error(
          "Browser key validation failed: use a Supabase publishable key.",
        );
      }
    }
    if (typeof sessionStorage !== "undefined") {
      this.sessionId =
        sessionStorage.getItem("ar-reels-session-id") || this.sessionId;
      sessionStorage.setItem("ar-reels-session-id", this.sessionId);
    }
    this.client = createClient(url, key, {
      global: { headers: { "x-reels-session-id": this.sessionId } },
      auth: {
        persistSession: true,
        autoRefreshToken: true,
        detectSessionInUrl: false,
      },
    });
    this.test = test;
  }
  private fail(message: string, code?: string): never {
    const text =
      code === "anonymous_provider_disabled"
        ? "Anonymous sign-in is disabled for this Supabase project. Enable it in Authentication → Providers → Anonymous Sign-Ins."
        : code === "PGRST202" || code === "PGRST205"
          ? "The Reels backend migrations are not deployed. Apply the versioned Supabase migrations."
          : message;
    const e = new Error(`Supabase: ${text}`);
    if (typeof window !== "undefined")
      window.dispatchEvent(
        new CustomEvent("reels-service-error", { detail: e.message }),
      );
    throw e;
  }
  private async rpc<T>(
    name: string,
    args: Record<string, unknown> = {},
  ): Promise<T> {
    const { data, error } = await this.client.rpc(name, args);
    if (error) this.fail(error.message, error.code);
    return data as T;
  }
  private async user() {
    const { data, error } = await this.client.auth.getSession();
    if (error) this.fail(error.message, error.code);
    return data.session?.user || null;
  }
  async session(profile?: Omit<Profile, "id">): Promise<Profile | null> {
    let user = await this.user();
    if (profile && !user) {
      const { data, error } = await this.client.auth.signInAnonymously({
        options: { data: { reels_mvp: true, integration_test: this.test } },
      });
      if (error) this.fail(error.message, error.code);
      user = data.user;
    }
    if (!user) return null;
    if (profile)
      return this.rpc<Profile>("ar_reels_profile", {
        p_name: profile.name,
        p_batch: profile.batch,
        p_language: profile.language,
        p_test: this.test,
      });
    return (await this.snapshot()).profile;
  }
  async snapshot(): Promise<Snapshot> {
    if (!(await this.user())) return { ...emptySnapshot(), scenario: "live" };
    return {
      ...emptySnapshot(),
      ...(await this.rpc<Snapshot>("ar_reels_snapshot")),
      scenario: "live",
    };
  }
  async catalogue() {
    return this.rpc<Chapter[]>("ar_reels_catalogue");
  }
  async products() {
    return this.rpc<Product[]>("ar_reels_product_catalogue");
  }
  async progress(
    reelId: string,
    from: number,
    to: number,
    wallSeconds: number,
    speed: number,
    visible: boolean,
    playing: boolean,
  ) {
    await this.rpc("ar_reels_progress", {
      p_reel: reelId,
      p_from: from,
      p_to: to,
      p_wall: wallSeconds,
      p_speed: speed,
      p_visible: visible,
      p_playing: playing,
    });
  }
  async checkpoint(reelId: string, position: number) {
    await this.rpc("ar_reels_checkpoint", {
      p_reel: reelId,
      p_position: position,
    });
  }
  async startQuiz(chapterId: string) {
    return this.rpc<Attempt>("ar_reels_start", { p_chapter: chapterId });
  }
  async submitAnswer(attemptId: string, index: number, selected: number) {
    return this.rpc<Attempt>("ar_reels_submit", {
      p_attempt: attemptId,
      p_index: index,
      p_selected: selected,
    });
  }
  async finishAttempt(id: string) {
    return this.rpc<Attempt>("ar_reels_finish", { p_attempt: id });
  }
  async reviewAttempt(id: string) {
    return this.rpc<Attempt>("ar_reels_review", { p_attempt: id });
  }
  async wallet() {
    const s = await this.snapshot();
    return { balance: balance(s), ledger: s.ledger };
  }
  async quote(productId: string, coins: number) {
    const q = await this.rpc<Quote>("ar_reels_quote", {
      p_product: productId,
      p_coins: Math.floor(coins),
    });
    await this.analytics("reels_redemption_quoted", { content_type: "store" });
    return q;
  }
  async confirmOrder(productId: string, coins: number, requestId: string) {
    return this.rpc<Order>("ar_reels_confirm", {
      p_product: productId,
      p_coins: coins,
      p_request: requestId,
    });
  }
  async recognition() {
    return this.rpc<{ name: string; earned: number; current: boolean }[]>(
      "ar_reels_recognition",
    );
  }
  async analytics(name: string, data: Partial<Event> = {}) {
    if (!(await this.user())) return;
    await this.rpc("ar_reels_analytics", {
      p_event: {
        id: data.id || crypto.randomUUID(),
        name,
        sessionId: this.sessionId,
        chapterId: data.chapterId,
        reelId: data.reelId,
        challengeId: data.challengeId,
        attemptId: data.attemptId,
        content_type: data.content_type,
      },
    });
  }
  async preference(key: "likes" | "saved" | "downloads", id: string) {
    await this.rpc("ar_reels_preference", { p_key: key, p_id: id });
  }
  async mute(value: boolean) {
    await this.rpc("ar_reels_preference", { p_key: "mute", p_value: value });
  }
  async lastReel(id: string) {
    await this.rpc("ar_reels_preference", { p_key: "lastReel", p_id: id });
  }
  async invitation(chapterId: string, defer: boolean) {
    await this.rpc("ar_reels_preference", {
      p_key: defer ? "deferred" : "offered",
      p_id: chapterId,
    });
  }
  async preset(_name: string): Promise<void> {
    this.fail(
      "Presenter presets are local-demo-only. Live accounts cannot seed progress, change entitlement or mint coins.",
    );
  }
  async answerGuide(): Promise<string[]> {
    this.fail("The presenter answer guide is unavailable in live mode.");
  }
}
