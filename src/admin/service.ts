import { createClient, type SupabaseClient } from "@supabase/supabase-js";
import type { Lesson } from "../services/discovery";
export type QuizQuestion = {
  prompt: string;
  options: string[];
  correct: number;
  explanation: string;
};
export type Metadata = {
  title: string;
  subject: string;
  topic: string;
  concept: string;
  grades: number[];
  languages: string[];
  notes?: string;
  isTest: boolean;
};
export type UploadRecord = {
  id: string;
  reel_id: string;
  status: "draft" | "verified" | "published";
  metadata: Metadata;
  questions: QuizQuestion[];
  video_object: string;
  poster_object: string;
  sha256: string;
  video_size: number;
  duration: number;
  created_at: string;
};
export type ReelStats = Lesson & {
  active: boolean;
  isTest?: boolean;
  views: number;
  uniqueViewers: number;
  watchSeconds: number;
  qualifiedViews: number;
  questionCount: number;
};
export type Dashboard = {
  views: number;
  uniqueViewers: number;
  watchSeconds: number;
  qualifiedViews: number;
  quizCompletions: number;
  coinsEarned: number;
  daily: { date: string; views: number; watchSeconds: number }[];
  reels: ReelStats[];
  drafts: UploadRecord[];
  includeTest: boolean;
  days: number;
};
let cached: SupabaseClient | undefined;
export function adminClient() {
  if (cached) return cached;
  if (import.meta.env.VITE_SERVICE_MODE !== "supabase")
    throw Error(
      "Admin tools require live Supabase mode. No local admin success is simulated.",
    );
  const url = import.meta.env.VITE_SUPABASE_URL,
    key = import.meta.env.VITE_SUPABASE_PUBLISHABLE_KEY;
  if (!url || !key || key.startsWith("sb_secret_"))
    throw Error("Configure the browser-safe Supabase URL and publishable key.");
  if (!key.startsWith("sb_publishable_")) {
    try {
      if (JSON.parse(atob(key.split(".")[1])).role !== "anon") throw Error();
    } catch {
      throw Error(
        "Privileged keys are forbidden in the administrator frontend.",
      );
    }
  }
  cached = createClient(url, key, {
    auth: {
      storageKey: "arivihan-admin-auth",
      persistSession: true,
      autoRefreshToken: true,
      detectSessionInUrl: true,
      flowType: "pkce",
    },
  });
  return cached;
}
export async function adminRpc<T>(
  name: string,
  args: Record<string, unknown> = {},
): Promise<T> {
  const { data, error } = await adminClient().rpc(
    "ar_reels_admin_" + name,
    args,
  );
  if (error) throw Error(error.message);
  return data as T;
}
export const emptyQuestion = (): QuizQuestion => ({
  prompt: "",
  options: ["", "", "", ""],
  correct: 0,
  explanation: "",
});
export function questionError(qs: QuizQuestion[]): string | null {
  if (qs.length < 2 || qs.length > 10)
    return "Add between 2 and 10 questions related to this reel.";
  for (let i = 0; i < qs.length; i++) {
    const q = qs[i];
    if (
      q.prompt.trim().length < 8 ||
      q.prompt.length > 2000 ||
      q.options.length !== 4 ||
      q.options.some((o) => !o.trim() || o.length > 500) ||
      new Set(q.options.map((o) => o.trim().toLowerCase())).size !== 4 ||
      !Number.isInteger(q.correct) ||
      q.correct < 0 ||
      q.correct > 3 ||
      q.explanation.trim().length < 5 ||
      q.explanation.length > 4000
    )
      return `Question ${i + 1}: add a prompt, four different options, a correct answer and an explanation.`;
  }
  if (new Set(qs.map((q) => q.prompt.trim().toLowerCase())).size !== qs.length)
    return "Each question must be different.";
  return null;
}
export const metadataError = (m: Metadata) =>
  !m.title.trim() ||
  !m.subject.trim() ||
  !m.topic.trim() ||
  !m.grades.length ||
  !m.languages.length
    ? "Add a title, subject, topic, class and language."
    : null;
export async function mediaAction(
  action: "sign" | "verify",
  uploadId: string,
): Promise<any> {
  const c = adminClient(),
    { data, error } = await c.auth.getSession();
  if (error || !data.session) throw Error("Sign in as an administrator again.");
  const response = await fetch(
    `${import.meta.env.VITE_SUPABASE_URL}/functions/v1/ar-reels-admin-media`,
    {
      method: "POST",
      headers: {
        apikey: import.meta.env.VITE_SUPABASE_PUBLISHABLE_KEY,
        Authorization: `Bearer ${data.session.access_token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ action, uploadId }),
    },
  );
  const result = await response.json();
  if (!response.ok) throw Error(result.error || "Upload service failed.");
  return result;
}
export async function uploadAssets(
  record: UploadRecord,
  file: File,
  poster: Blob,
  step: (label: string) => void,
) {
  step("Authorizing upload…");
  const signed = await mediaAction("sign", record.id);
  for (const [kind, blob, mime] of [
    ["video", file, "video/mp4"],
    ["poster", poster, "image/jpeg"],
  ] as const) {
    if (signed[kind].exists) continue;
    step(kind === "video" ? "Uploading video…" : "Uploading poster…");
    const { error } = await adminClient()
      .storage.from("arivihan-reels")
      .uploadToSignedUrl(signed[kind].path, signed[kind].token, blob, {
        contentType: mime,
        cacheControl: "31536000",
        upsert: false,
      });
    if (error) throw Error(error.message);
  }
  step("Checking uploaded files…");
  await mediaAction("verify", record.id);
}
export type ImportedLesson = {
  file: string;
  metadata: Partial<Metadata>;
  questions?: QuizQuestion[];
};
export function parseMetadataImport(text: string): ImportedLesson[] {
  const rows = JSON.parse(text);
  if (!Array.isArray(rows) || rows.length < 1 || rows.length > 200)
    throw Error("Import a JSON array of 1–200 lessons.");
  const names = new Set<string>();
  return rows.map((row: any, index: number) => {
    const fail = () => {
      throw Error(
        `Metadata row ${index + 1} has invalid fields. Use the downloaded template.`,
      );
    };
    if (
      !row ||
      typeof row !== "object" ||
      typeof row.file !== "string" ||
      !row.file ||
      names.has(row.file)
    )
      fail();
    names.add(row.file);
    const metadata: Partial<Metadata> = {};
    for (const key of [
      "title",
      "subject",
      "topic",
      "concept",
      "notes",
    ] as const) {
      if (key in row) {
        if (typeof row[key] !== "string") fail();
        metadata[key] = row[key];
      }
    }
    if ("grades" in row) {
      if (
        !Array.isArray(row.grades) ||
        !row.grades.length ||
        row.grades.some(
          (g: unknown) =>
            !Number.isInteger(g) || Number(g) < 6 || Number(g) > 12,
        )
      )
        fail();
      metadata.grades = [...new Set<number>(row.grades)];
    }
    if ("languages" in row) {
      if (
        !Array.isArray(row.languages) ||
        !row.languages.length ||
        row.languages.some((l: unknown) => l !== "Hindi" && l !== "English")
      )
        fail();
      metadata.languages = [...new Set<string>(row.languages)];
    }
    if ("isTest" in row) {
      if (typeof row.isTest !== "boolean") fail();
      metadata.isTest = row.isTest;
    }
    let questions: QuizQuestion[] | undefined;
    if ("questions" in row) {
      if (!Array.isArray(row.questions) || row.questions.length > 10) fail();
      questions = row.questions.map((q: any) => {
        if (
          !q ||
          typeof q.prompt !== "string" ||
          !Array.isArray(q.options) ||
          q.options.length !== 4 ||
          q.options.some((o: unknown) => typeof o !== "string") ||
          !Number.isInteger(q.correct) ||
          q.correct < 0 ||
          q.correct > 3 ||
          typeof q.explanation !== "string"
        )
          fail();
        return {
          prompt: q.prompt,
          options: [...q.options],
          correct: q.correct,
          explanation: q.explanation,
        };
      });
    }
    return { file: row.file, metadata, questions };
  });
}
