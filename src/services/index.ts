import type { DemoService } from "./contracts";
export const liveMode = import.meta.env.VITE_SERVICE_MODE === "supabase";
const live = async () => {
  try {
    const { SupabaseAdapter } = await import("./supabase");
    return new SupabaseAdapter(
      import.meta.env.VITE_SUPABASE_URL || "",
      import.meta.env.VITE_SUPABASE_PUBLISHABLE_KEY || "",
      import.meta.env.VITE_INTEGRATION_TEST === "true",
    );
  } catch (e) {
    const error =
      e instanceof Error ? e : new Error("Supabase configuration failed.");
    return new Proxy({} as DemoService, {
      get: () => async () => {
        throw error;
      },
    });
  }
};
// Literal build-time branch keeps bundled mock grading keys out of live-mode builds.
export const service: DemoService =
  import.meta.env.VITE_SERVICE_MODE === "supabase"
    ? await live()
    : (await import("./local")).service;
export { balance, qualified } from "./state";
