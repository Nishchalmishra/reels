import React, { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import {
  BarChart3,
  BookOpen,
  CheckCircle2,
  ChevronRight,
  Clock,
  Download,
  Eye,
  Film,
  LogOut,
  Plus,
  RefreshCw,
  Search,
  ShieldCheck,
  UploadCloud,
  Users,
  X,
} from "lucide-react";
import type { Session } from "@supabase/supabase-js";
import {
  adminClient,
  adminRpc,
  emptyQuestion,
  metadataError,
  questionError,
  parseMetadataImport,
  uploadAssets,
  type Dashboard,
  type Metadata,
  type QuizQuestion,
  type ReelStats,
  type UploadRecord,
} from "./service";
import { inspectVideo } from "./media";
import "./admin.css";
type Job = {
  key: string;
  name: string;
  file?: File;
  meta: Metadata;
  questions: QuizQuestion[];
  record?: UploadRecord;
  status: string;
  error?: string;
};
const number = (n: number) =>
  new Intl.NumberFormat("en-IN", { maximumFractionDigits: 1 }).format(n || 0);
const duration = (s: number) =>
  s >= 3600
    ? `${number(s / 3600)} h`
    : s >= 60
      ? `${number(s / 60)} min`
      : `${number(s)} sec`;
const fromRecord = (r: UploadRecord): Job => ({
  key: r.id,
  name: r.metadata.title,
  meta: r.metadata,
  questions: r.questions.length
    ? r.questions
    : [emptyQuestion(), emptyQuestion()],
  record: r,
  status:
    r.status === "verified"
      ? "Ready to publish"
      : "Draft: select original file to resume",
});
export default function Admin() {
  const [session, setSession] = useState<Session | null>(null),
    [authorized, setAuthorized] = useState(false),
    [ready, setReady] = useState(false),
    [error, setError] = useState("");
  const [email, setEmail] = useState(""),
    [password, setPassword] = useState(""),
    [register, setRegister] = useState(false),
    [notice, setNotice] = useState(""),
    [busy, setBusy] = useState(false);
  const [tab, setTab] = useState<"analytics" | "reels" | "uploads">(
      "analytics",
    ),
    [days, setDays] = useState(30),
    [includeTest, setIncludeTest] = useState(false),
    [data, setData] = useState<Dashboard | null>(null);
  const [jobs, setJobs] = useState<Job[]>([]),
    [selected, setSelected] = useState(""),
    [search, setSearch] = useState(""),
    [preview, setPreview] = useState<ReelStats | null>(null),
    [previewQuestions, setPreviewQuestions] = useState<QuizQuestion[]>([]);
  const [commonGrade, setCommonGrade] = useState(11),
    [commonSubject, setCommonSubject] = useState("Physics"),
    [commonLanguage, setCommonLanguage] = useState("Hindi");
  useEffect(() => {
    window.scrollTo(0, 0);
  }, [tab]);
  const uploadInput = useRef<HTMLInputElement>(null),
    latestLoad = useRef(0);
  useEffect(() => {
    try {
      const c = adminClient();
      c.auth.getSession().then(({ data, error }) => {
        if (error) setError(error.message);
        setSession(data.session);
        setReady(true);
      });
      const { data } = c.auth.onAuthStateChange((_event, s) => {
        setSession(s);
        if (!s) {
          setAuthorized(false);
          setData(null);
          setJobs([]);
        }
      });
      return () => data.subscription.unsubscribe();
    } catch (e) {
      setError((e as Error).message);
      setReady(true);
    }
  }, []);
  const load = async () => {
    const serial = ++latestLoad.current;
    const result = await adminRpc<Dashboard>("dashboard", {
      p_days: days,
      p_include_test: includeTest,
    });
    if (serial !== latestLoad.current) return;
    setData(result);
    setJobs((old) => [
      ...old,
      ...result.drafts
        .filter((r) => !old.some((j) => j.record?.id === r.id))
        .map(fromRecord),
    ]);
  };
  useEffect(() => {
    if (!session) {
      setAuthorized(false);
      return;
    }
    let current = true;
    setReady(false);
    adminRpc("access")
      .then(() => {
        if (current) setAuthorized(true);
      })
      .catch((e) => {
        if (current) {
          setAuthorized(false);
          setError(e.message);
        }
      })
      .finally(() => {
        if (current) setReady(true);
      });
    return () => {
      current = false;
    };
  }, [session?.user.id]);
  useEffect(() => {
    if (authorized) void load().catch((e) => setError(e.message));
  }, [authorized, days, includeTest]);
  const action = async (fn: () => Promise<void>) => {
    if (busy) return;
    setBusy(true);
    setError("");
    setNotice("");
    try {
      await fn();
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setBusy(false);
    }
  };
  const update = (key: string, patch: Partial<Job>) =>
    setJobs((old) => old.map((j) => (j.key === key ? { ...j, ...patch } : j)));
  const job = jobs.find((j) => j.key === selected);
  const addFiles = (files: FileList | null) => {
    if (!files) return;
    const added = [...files].map(
      (file) =>
        ({
          key: crypto.randomUUID(),
          name: file.name,
          file,
          meta: {
            title: file.name.replace(/\.mp4$/i, "").replace(/[_-]+/g, " "),
            topic: "",
            subject: commonSubject,
            concept: "",
            grades: [commonGrade],
            languages: [commonLanguage],
            isTest: false,
          },
          questions: [emptyQuestion(), emptyQuestion()],
          status: "Not uploaded",
        }) satisfies Job,
    );
    setJobs((old) => [...old, ...added]);
    if (added[0]) setSelected(added[0].key);
    setTab("uploads");
  };
  const process = async (j: Job, publish: boolean) => {
    const badMeta = metadataError(j.meta),
      badQuestions = publish ? questionError(j.questions) : null;
    if (badMeta || badQuestions) throw Error(badMeta || badQuestions!);
    let record = j.record;
    update(j.key, { error: undefined, status: "Preparing…" });
    if (!record) {
      if (!j.file) throw Error("Choose the original MP4 to upload.");
      update(j.key, { status: "Reading video and creating poster…" });
      const inspected = await inspectVideo(j.file);
      record = await adminRpc<UploadRecord>("create_upload", {
        p_id: j.key,
        p_sha: inspected.sha,
        p_size: j.file.size,
        p_poster_sha: inspected.posterSha,
        p_poster_size: inspected.poster.size,
        p_duration: inspected.duration,
        p_metadata: j.meta,
        p_questions: j.questions,
      });
      update(j.key, { record });
      if (record.status === "draft") {
        await uploadAssets(record, j.file, inspected.poster, (status) =>
          update(j.key, { status }),
        );
        record = { ...record, status: "verified" };
        update(j.key, { record, status: "Ready to publish" });
      }
    } else if (record.status === "draft") {
      if (!j.file) throw Error("Choose the original MP4 to resume this draft.");
      const inspected = await inspectVideo(j.file);
      if (inspected.sha !== record.sha256)
        throw Error("This is a different video. Choose the original file.");
      await uploadAssets(record, j.file, inspected.poster, (status) =>
        update(j.key, { status }),
      );
      record = { ...record, status: "verified" };
      update(j.key, { record });
    }
    if (record.status === "published") {
      update(j.key, { status: "Published" });
      return;
    }
    record = await adminRpc<UploadRecord>("save", {
      p_id: record.id,
      p_metadata: j.meta,
      p_questions: j.questions,
    });
    update(j.key, { record });
    if (publish) {
      update(j.key, { status: "Publishing…" });
      await adminRpc("publish", { p_id: record.id });
      update(j.key, {
        record: { ...record, status: "published" },
        status: j.meta.isTest ? "Published as hidden test" : "Published",
      });
    } else
      update(j.key, {
        status: "Draft saved · ready for questions / publishing",
      });
  };
  const runJob = (j: Job, publish: boolean) =>
    action(async () => {
      try {
        await process(j, publish);
        await load();
        setNotice(
          publish
            ? "Reel published with its mapped quiz questions."
            : "Draft saved. It is not visible to students yet.",
        );
      } catch (e) {
        update(j.key, {
          error: (e as Error).message,
          status: "Needs attention",
        });
        throw e;
      }
    });
  const batch = (publish: boolean) =>
    action(async () => {
      let completed = 0,
        failed = 0;
      for (const j of jobs.filter((j) => j.record?.status !== "published")) {
        try {
          await process(j, publish);
          completed++;
        } catch (e) {
          failed++;
          update(j.key, {
            error: (e as Error).message,
            status: "Needs attention",
          });
        }
      }
      await load();
      setNotice(
        `${completed} ${publish ? "published" : "drafts saved"}; ${failed} need attention.`,
      );
    });
  const importMetadata = async (file: File | undefined) => {
    if (!file) return;
    if (file.size > 1024 * 1024)
      throw Error("Metadata JSON must be under 1 MB.");
    const list = parseMetadataImport(await file.text());
    const matched = jobs.filter((j) =>
      list.some((r) => r.file === j.name),
    ).length;
    if (!matched)
      throw Error(
        "No file names match your upload queue. Use the exact names shown in the template.",
      );
    setJobs((old) =>
      old.map((j) => {
        const row = list.find((r) => r.file === j.name);
        return row
          ? {
              ...j,
              meta: { ...j.meta, ...row.metadata },
              questions: row.questions || j.questions,
            }
          : j;
      }),
    );
    setNotice(
      `Metadata imported for ${matched} reel${matched === 1 ? "" : "s"}. Review every question before publishing.`,
    );
  };
  const download = (name: string, text: string, type = "application/json") => {
    const u = URL.createObjectURL(new Blob([text], { type })),
      a = document.createElement("a");
    a.href = u;
    a.download = name;
    a.click();
    setTimeout(() => URL.revokeObjectURL(u), 1000);
  };
  const openPreview = (reel: ReelStats) =>
    action(async () => {
      setPreviewQuestions([]);
      const questions = await adminRpc<QuizQuestion[]>("questions", {
        p_reel: reel.id,
      });
      setPreviewQuestions(questions);
      setPreview(reel);
    });
  const submitAuth = (e: React.FormEvent) => {
    e.preventDefault();
    void action(async () => {
      const c = adminClient();
      if (register) {
        const { data, error } = await c.auth.signUp({
          email: email.trim(),
          password,
          options: { emailRedirectTo: location.origin + "/admin" },
        });
        if (error) throw error;
        setPassword("");
        setNotice(
          data.session
            ? "Account created. Checking administrator access…"
            : "Check your email to confirm your account, then return here and sign in.",
        );
        setRegister(false);
      } else {
        const { error } = await c.auth.signInWithPassword({
          email: email.trim(),
          password,
        });
        if (error) throw error;
        setPassword("");
      }
    });
  };
  if (!ready)
    return (
      <div className="admin-app admin-center" role="status">
        Checking administrator access…
      </div>
    );
  if (!session || !authorized)
    return (
      <div className="admin-app admin-center">
        <div className="admin-login">
          <Link to="/" className="admin-brand">
            <span>a</span> arivihan <b>ADMIN</b>
          </Link>
          <ShieldCheck size={36} className="admin-accent" />
          <h1>
            {session
              ? "Access restricted"
              : register
                ? "Create admin account"
                : "Welcome back."}
          </h1>
          <p>Manage reels, related quiz questions and learning analytics.</p>
          {error && (
            <div className="admin-alert" role="alert">
              {error}
            </div>
          )}
          {notice && (
            <div className="admin-notice" role="status">
              {notice}
            </div>
          )}
          {session ? (
            <>
              <p>
                Only an approved, email-verified account can enter. Student
                guest accounts cannot access this area.
              </p>
              <button
                className="admin-primary"
                onClick={() =>
                  void action(async () => {
                    await adminClient().auth.signOut({ scope: "local" });
                    setError("");
                  })
                }
              >
                Use another account
              </button>
            </>
          ) : (
            <form onSubmit={submitAuth}>
              <label>
                Email
                <input
                  type="email"
                  value={email}
                  required
                  autoComplete="email"
                  onChange={(e) => setEmail(e.target.value)}
                />
              </label>
              <label>
                Password
                <input
                  type="password"
                  value={password}
                  required
                  minLength={register ? 12 : 1}
                  autoComplete={register ? "new-password" : "current-password"}
                  onChange={(e) => setPassword(e.target.value)}
                />
              </label>
              {register && (
                <p className="admin-hint">
                  Use at least 12 characters. Creating an account sends a
                  verification email; it does not grant administrator access by
                  itself.
                </p>
              )}
              <button className="admin-primary" disabled={busy}>
                {busy
                  ? "Please wait…"
                  : register
                    ? "Create account & verify email"
                    : "Sign in"}
              </button>
              <button
                className="admin-link"
                type="button"
                onClick={() => {
                  setRegister((v) => !v);
                  setError("");
                }}
              >
                {register
                  ? "Already have an account? Sign in"
                  : "First time? Create admin account"}
              </button>
            </form>
          )}
          <Link className="admin-link" to="/">
            Back to student app
          </Link>
        </div>
      </div>
    );
  const filtered =
    data?.reels.filter((r) =>
      `${r.title} ${r.subject} ${r.topic}`
        .toLowerCase()
        .includes(search.toLowerCase()),
    ) || [];
  return (
    <div className="admin-app">
      <aside className="admin-sidebar">
        <Link to="/admin" className="admin-brand">
          <span>a</span> arivihan <b>ADMIN</b>
        </Link>
        <span className="admin-overline">CONTENT STUDIO</span>
        <nav aria-label="Admin navigation">
          {[
            ["analytics", "Analytics", BarChart3],
            ["reels", "Reel library", Film],
            ["uploads", "Upload reels", UploadCloud],
          ].map(([id, label, Icon]) => (
            <button
              key={String(id)}
              className={tab === id ? "selected" : ""}
              onClick={() => setTab(id as typeof tab)}
            >
              {React.createElement(Icon, { size: 19 })}
              {String(label)}
              {id === "uploads" && jobs.length > 0 && (
                <small>{jobs.length}</small>
              )}
            </button>
          ))}
        </nav>
        <div className="admin-account">
          <ShieldCheck size={17} />
          <span>{session.user.email}</span>
          <button
            aria-label="Sign out admin"
            onClick={() =>
              void action(async () => {
                await adminClient().auth.signOut({ scope: "local" });
              })
            }
          >
            <LogOut size={17} />
          </button>
        </div>
        <Link to="/" className="admin-link">
          Open student app <ChevronRight size={14} />
        </Link>
      </aside>
      <div className="admin-workspace">
        <header className="admin-topbar">
          <span>
            <ShieldCheck size={16} /> ADMINISTRATOR
          </span>
          <div className="admin-topbar-actions">
            <button
              className="admin-mobile-logout admin-secondary"
              aria-label="Sign out admin"
              disabled={busy}
              onClick={() =>
                void action(async () => {
                  await adminClient().auth.signOut({ scope: "local" });
                })
              }
            >
              <LogOut size={17} />
            </button>
            <button
              className="admin-primary"
              onClick={() => {
                setTab("uploads");
                uploadInput.current?.click();
              }}
              disabled={busy}
            >
              <Plus size={17} /> Add reels
            </button>
          </div>
        </header>
        <main className="admin-main">
          <input
            ref={uploadInput}
            type="file"
            accept="video/mp4,.mp4"
            multiple
            hidden
            onChange={(e) => {
              addFiles(e.target.files);
              e.target.value = "";
            }}
          />
          {error && (
            <div className="admin-alert" role="alert">
              {error}
              <button aria-label="Dismiss error" onClick={() => setError("")}>
                <X size={17} />
              </button>
            </div>
          )}
          {notice && (
            <div className="admin-notice" role="status">
              {notice}
              <button aria-label="Dismiss notice" onClick={() => setNotice("")}>
                <X size={17} />
              </button>
            </div>
          )}
          <div className="admin-heading">
            <div>
              <span className="admin-overline">ARIVIHAN REELS</span>
              <h1>
                {tab === "analytics"
                  ? "A clearer view of learning."
                  : tab === "reels"
                    ? "Your lesson library."
                    : "Turn a reel into a lesson."}
              </h1>
              <p>
                {tab === "uploads"
                  ? "Upload videos, add their questions, then publish to the student Feed."
                  : "Real activity from the connected Supabase project."}
              </p>
            </div>
            <div className="admin-filters">
              <select
                aria-label="Analytics date range"
                value={days}
                onChange={(e) => setDays(Number(e.target.value))}
              >
                <option value={7}>Last 7 days</option>
                <option value={30}>Last 30 days</option>
                <option value={90}>Last 90 days</option>
              </select>
              <button
                className="admin-secondary"
                disabled={busy}
                onClick={() => void action(load)}
                aria-label="Refresh admin data"
              >
                <RefreshCw size={16} />
              </button>
            </div>
          </div>
          {!data ? (
            <div className="admin-panel admin-empty">
              Loading connected data…
            </div>
          ) : (
            <>
              {tab === "analytics" && (
                <>
                  <div className="admin-metrics">
                    {[
                      [Eye, "Views (≥3 sec)", number(data.views)],
                      [Users, "Unique viewers", number(data.uniqueViewers)],
                      [Clock, "Active watch time", duration(data.watchSeconds)],
                      [
                        CheckCircle2,
                        "80% qualifications",
                        number(data.qualifiedViews),
                      ],
                      [
                        BookOpen,
                        "Completed quizzes",
                        number(data.quizCompletions),
                      ],
                      [ShieldCheck, "Coins earned", number(data.coinsEarned)],
                    ].map(([Icon, label, value]) => (
                      <article key={String(label)}>
                        {React.createElement(Icon, { size: 19 })}
                        <span>{String(label)}</span>
                        <strong>{String(value)}</strong>
                      </article>
                    ))}
                  </div>
                  <section className="admin-panel">
                    <div className="admin-panel-heading">
                      <div>
                        <h2>Watching, day by day</h2>
                        <p>Views across your reels · India Standard Time</p>
                      </div>
                      <label className="admin-check">
                        <input
                          type="checkbox"
                          checked={includeTest}
                          onChange={(e) => setIncludeTest(e.target.checked)}
                        />{" "}
                        Include test activity
                      </label>
                    </div>
                    <div className="admin-chart" aria-label="Daily reel views">
                      {data.daily.map((day) => (
                        <div
                          key={day.date}
                          title={`${day.date}: ${day.views} views · ${duration(day.watchSeconds)}`}
                        >
                          <span>{day.views || ""}</span>
                          <i
                            style={{
                              height: day.views
                                ? `${Math.max(4, (day.views / Math.max(1, ...data.daily.map((d) => d.views))) * 140)}px`
                                : "2px",
                            }}
                          />
                          <small>
                            {new Date(
                              day.date + "T12:00:00Z",
                            ).toLocaleDateString("en-IN", {
                              day: "numeric",
                              month: "short",
                            })}
                          </small>
                        </div>
                      ))}
                    </div>
                    <p className="admin-hint">
                      A view requires three seconds of active playback. New view
                      tracking starts with this release; older 80% watch
                      qualifications remain separate. Playback reports measure
                      activity, not attention.
                    </p>
                  </section>
                </>
              )}
              {(tab === "reels" || tab === "analytics") && (
                <section className="admin-panel">
                  <div className="admin-panel-heading">
                    <div>
                      <h2>
                        {tab === "analytics"
                          ? "Reel performance"
                          : "Published reels"}
                      </h2>
                      <p>
                        {data.reels.length} catalogue records · questions mapped
                        to each video
                      </p>
                    </div>
                    <div className="admin-table-actions">
                      <label className="admin-search">
                        <Search size={16} />
                        <input
                          aria-label="Search admin reels"
                          placeholder="Search title or topic"
                          value={search}
                          onChange={(e) => setSearch(e.target.value)}
                        />
                      </label>
                      <button
                        className="admin-secondary"
                        aria-label="Export reel analytics"
                        onClick={() => {
                          const cell = (v: unknown) =>
                            '"' +
                            String(v)
                              .replace(/^[=+\-@]/, "'$&")
                              .replaceAll('"', '""') +
                            '"';
                          download(
                            "reel-analytics.csv",
                            [
                              [
                                "Title",
                                "Subject",
                                "Views",
                                "Unique viewers",
                                "Watch seconds",
                                "80% qualifications",
                                "Questions",
                              ],
                              ...filtered.map((r) => [
                                r.title,
                                r.subject,
                                r.views,
                                r.uniqueViewers,
                                r.watchSeconds,
                                r.qualifiedViews,
                                r.questionCount,
                              ]),
                            ]
                              .map((row) => row.map(cell).join(","))
                              .join("\n"),
                            "text/csv",
                          );
                        }}
                      >
                        <Download size={16} />
                      </button>
                    </div>
                  </div>
                  <div className="admin-table-scroll">
                    <table>
                      <thead>
                        <tr>
                          <th>Reel</th>
                          <th>Views</th>
                          <th>Viewers</th>
                          <th>Watch time</th>
                          <th>Qualified</th>
                          <th>Questions</th>
                        </tr>
                      </thead>
                      <tbody>
                        {filtered.map((r) => (
                          <tr key={r.id}>
                            <td>
                              <button
                                className="admin-reel"
                                disabled={busy}
                                onClick={() => void openPreview(r)}
                              >
                                <img src={r.poster} alt="" />
                                <span>
                                  <strong>{r.title}</strong>
                                  <small>
                                    {r.subject} · {r.topic}
                                    {!r.active ? " · Hidden test" : ""}
                                  </small>
                                </span>
                              </button>
                            </td>
                            <td>{number(r.views)}</td>
                            <td>{number(r.uniqueViewers)}</td>
                            <td>{duration(r.watchSeconds)}</td>
                            <td>{number(r.qualifiedViews)}</td>
                            <td>
                              <span className="admin-badge">
                                {r.questionCount} mapped
                              </span>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                    {!filtered.length && (
                      <div className="admin-empty">
                        No reels match your search.
                      </div>
                    )}
                  </div>
                </section>
              )}
              {tab === "uploads" && (
                <>
                  <section className="admin-panel">
                    <div className="admin-upload-defaults">
                      <label>
                        Default subject
                        <select
                          value={commonSubject}
                          onChange={(e) => setCommonSubject(e.target.value)}
                        >
                          {[
                            "Physics",
                            "Chemistry",
                            "Biology",
                            "Mathematics",
                          ].map((s) => (
                            <option key={s}>{s}</option>
                          ))}
                        </select>
                      </label>
                      <label>
                        Default class
                        <select
                          value={commonGrade}
                          onChange={(e) =>
                            setCommonGrade(Number(e.target.value))
                          }
                        >
                          <option value={11}>Class 11</option>
                          <option value={12}>Class 12</option>
                        </select>
                      </label>
                      <label>
                        Default language
                        <select
                          value={commonLanguage}
                          onChange={(e) => setCommonLanguage(e.target.value)}
                        >
                          <option>Hindi</option>
                          <option>English</option>
                        </select>
                      </label>
                    </div>
                    <button
                      className="admin-dropzone"
                      disabled={busy}
                      onClick={() => uploadInput.current?.click()}
                    >
                      <UploadCloud size={33} />
                      <strong>Choose MP4 videos</strong>
                      <span>
                        Select 25–30 files together · up to 50 MB and 30 minutes
                        per reel
                      </span>
                      <small>
                        Posters and duration are read from the actual video.
                        Files are not published until you approve their
                        questions.
                      </small>
                    </button>
                    <div className="admin-bulk-actions">
                      <button
                        className="admin-secondary"
                        disabled={busy || !jobs.length}
                        onClick={() => void batch(false)}
                      >
                        Upload / save all drafts
                      </button>
                      <button
                        className="admin-primary"
                        disabled={busy || !jobs.length}
                        onClick={() => void batch(true)}
                      >
                        Publish ready reels
                      </button>
                      <label className="admin-import">
                        Import metadata JSON
                        <input
                          type="file"
                          accept="application/json,.json"
                          disabled={busy}
                          onChange={(e) =>
                            void action(() =>
                              importMetadata(e.target.files?.[0]),
                            )
                          }
                        />
                      </label>
                      <button
                        className="admin-link"
                        onClick={() =>
                          download(
                            "reel-metadata-template.json",
                            JSON.stringify(
                              [
                                {
                                  file: jobs[0]?.name || "your-video.mp4",
                                  ...(jobs[0]?.meta || {
                                    title: "",
                                    subject: "Physics",
                                    topic: "",
                                    concept: "",
                                    grades: [11],
                                    languages: ["Hindi"],
                                    isTest: false,
                                  }),
                                  questions: [emptyQuestion(), emptyQuestion()],
                                },
                              ],
                              null,
                              2,
                            ),
                          )
                        }
                      >
                        Download template
                      </button>
                    </div>
                  </section>
                  {jobs.length ? (
                    <div className="admin-editor-layout">
                      <section className="admin-panel admin-queue">
                        <h2>
                          Upload queue <small>{jobs.length}</small>
                        </h2>
                        {jobs.map((j) => (
                          <button
                            key={j.key}
                            disabled={busy}
                            className={selected === j.key ? "selected" : ""}
                            onClick={() => setSelected(j.key)}
                          >
                            <Film size={19} />
                            <span>
                              <strong>{j.meta.title || j.name}</strong>
                              <small className={j.error ? "admin-danger" : ""}>
                                {j.error || j.status}
                              </small>
                            </span>
                            {j.record?.status === "published" && (
                              <CheckCircle2 size={17} />
                            )}
                          </button>
                        ))}
                      </section>
                      {job ? (
                        <section
                          className="admin-panel admin-editor"
                          aria-label="Reel editor"
                        >
                          <div className="admin-panel-heading">
                            <div>
                              <span className="admin-overline">
                                LESSON DETAILS
                              </span>
                              <h2>{job.meta.title || job.name}</h2>
                            </div>
                            <span className="admin-badge">
                              {job.record?.status || "Local file"}
                            </span>
                          </div>
                          <MediaPreview file={job.file} record={job.record} />
                          {job.record?.status === "published" ? (
                            <p>
                              This reel and its questions are published.
                              Published content is read-only so saved quizzes
                              remain consistent.
                            </p>
                          ) : (
                            <>
                              <fieldset disabled={busy}>
                                <label>
                                  Reel title
                                  <input
                                    value={job.meta.title}
                                    maxLength={160}
                                    onChange={(e) =>
                                      update(job.key, {
                                        meta: {
                                          ...job.meta,
                                          title: e.target.value,
                                        },
                                      })
                                    }
                                  />
                                </label>
                                <div className="admin-form-grid">
                                  <label>
                                    Subject
                                    <input
                                      value={job.meta.subject}
                                      maxLength={60}
                                      onChange={(e) =>
                                        update(job.key, {
                                          meta: {
                                            ...job.meta,
                                            subject: e.target.value,
                                          },
                                        })
                                      }
                                    />
                                  </label>
                                  <label>
                                    Chapter / topic
                                    <input
                                      value={job.meta.topic}
                                      maxLength={120}
                                      onChange={(e) =>
                                        update(job.key, {
                                          meta: {
                                            ...job.meta,
                                            topic: e.target.value,
                                          },
                                        })
                                      }
                                    />
                                  </label>
                                </div>
                                <label>
                                  Concept / short description
                                  <textarea
                                    value={job.meta.concept}
                                    maxLength={2000}
                                    rows={2}
                                    onChange={(e) =>
                                      update(job.key, {
                                        meta: {
                                          ...job.meta,
                                          concept: e.target.value,
                                        },
                                      })
                                    }
                                  />
                                </label>
                                <div className="admin-form-grid">
                                  <div>
                                    <span className="admin-field-label">
                                      Classes
                                    </span>
                                    {[11, 12].map((g) => (
                                      <label className="admin-check" key={g}>
                                        <input
                                          type="checkbox"
                                          checked={job.meta.grades.includes(g)}
                                          onChange={(e) =>
                                            update(job.key, {
                                              meta: {
                                                ...job.meta,
                                                grades: e.target.checked
                                                  ? [...job.meta.grades, g]
                                                  : job.meta.grades.filter(
                                                      (n) => n !== g,
                                                    ),
                                              },
                                            })
                                          }
                                        />{" "}
                                        Class {g}
                                      </label>
                                    ))}
                                  </div>
                                  <div>
                                    <span className="admin-field-label">
                                      Languages
                                    </span>
                                    {["English", "Hindi"].map((l) => (
                                      <label className="admin-check" key={l}>
                                        <input
                                          type="checkbox"
                                          checked={job.meta.languages.includes(
                                            l,
                                          )}
                                          onChange={(e) =>
                                            update(job.key, {
                                              meta: {
                                                ...job.meta,
                                                languages: e.target.checked
                                                  ? [...job.meta.languages, l]
                                                  : job.meta.languages.filter(
                                                      (n) => n !== l,
                                                    ),
                                              },
                                            })
                                          }
                                        />{" "}
                                        {l}
                                      </label>
                                    ))}
                                  </div>
                                </div>
                                <label className="admin-check">
                                  <input
                                    type="checkbox"
                                    checked={!!job.meta.isTest}
                                    onChange={(e) =>
                                      update(job.key, {
                                        meta: {
                                          ...job.meta,
                                          isTest: e.target.checked,
                                        },
                                      })
                                    }
                                  />{" "}
                                  Test content — keep hidden from student Feed
                                </label>
                                <label>
                                  Transcript / editor notes (optional)
                                  <textarea
                                    value={job.meta.notes || ""}
                                    rows={3}
                                    maxLength={20000}
                                    onChange={(e) =>
                                      update(job.key, {
                                        meta: {
                                          ...job.meta,
                                          notes: e.target.value,
                                        },
                                      })
                                    }
                                  />
                                </label>
                                {job.record?.status === "draft" &&
                                  !job.file && (
                                    <label>
                                      Original MP4 to resume upload
                                      <input
                                        type="file"
                                        accept="video/mp4,.mp4"
                                        onChange={(e) =>
                                          update(job.key, {
                                            file: e.target.files?.[0],
                                          })
                                        }
                                      />
                                    </label>
                                  )}
                                <div className="admin-question-heading">
                                  <h2>Questions for this reel</h2>
                                  <p>
                                    Write at least two distinct questions from
                                    this video's teaching. A student's
                                    five-question quiz uses only their watched
                                    reels. We do not invent questions from a
                                    filename.
                                  </p>
                                </div>
                                {job.questions.map((q, i) => (
                                  <QuestionEditor
                                    key={i}
                                    question={q}
                                    index={i}
                                    onChange={(value) =>
                                      update(job.key, {
                                        questions: job.questions.map(
                                          (old, n) => (n === i ? value : old),
                                        ),
                                      })
                                    }
                                    remove={
                                      job.questions.length > 2
                                        ? () =>
                                            update(job.key, {
                                              questions: job.questions.filter(
                                                (_, n) => n !== i,
                                              ),
                                            })
                                        : undefined
                                    }
                                  />
                                ))}
                                {job.questions.length < 10 && (
                                  <button
                                    className="admin-secondary"
                                    type="button"
                                    onClick={() =>
                                      update(job.key, {
                                        questions: [
                                          ...job.questions,
                                          emptyQuestion(),
                                        ],
                                      })
                                    }
                                  >
                                    <Plus size={16} /> Add question
                                  </button>
                                )}
                              </fieldset>
                              <p className="admin-hint">
                                Five correct answers earn five coins total, once
                                per content set. Answer keys stay private on the
                                backend.
                              </p>
                              {job.error && (
                                <p className="admin-danger" role="alert">
                                  {job.error}
                                </p>
                              )}
                              <div className="admin-editor-actions">
                                <button
                                  className="admin-secondary"
                                  disabled={busy}
                                  onClick={() => void runJob(job, false)}
                                >
                                  {busy ? job.status : "Upload / save draft"}
                                </button>
                                <button
                                  className="admin-primary"
                                  disabled={busy}
                                  onClick={() => void runJob(job, true)}
                                >
                                  {busy
                                    ? job.status
                                    : "Publish reel & questions"}
                                </button>
                              </div>
                            </>
                          )}
                        </section>
                      ) : (
                        <div className="admin-panel admin-empty">
                          Choose a reel from the queue to edit its details.
                        </div>
                      )}
                    </div>
                  ) : (
                    <div className="admin-panel admin-empty">
                      Your upload queue is empty. Choose videos above to get
                      started.
                    </div>
                  )}
                </>
              )}
            </>
          )}
        </main>
      </div>
      {preview && (
        <div className="admin-modal-scrim" onClick={() => setPreview(null)}>
          <section
            className="admin-modal"
            role="dialog"
            aria-modal="true"
            aria-label="Published reel preview"
            onClick={(e) => e.stopPropagation()}
          >
            <button
              className="admin-close"
              aria-label="Close reel preview"
              onClick={() => setPreview(null)}
            >
              <X />
            </button>
            <h2>{preview.title}</h2>
            <video
              src={preview.source}
              poster={preview.poster}
              controls
              playsInline
            />
            <p>
              {preview.topic} ·{" "}
              {preview.grades.map((g) => `Class ${g}`).join(", ")}
            </p>
            <h3>Mapped questions (read-only)</h3>
            {previewQuestions.map((q, i) => (
              <div className="admin-preview-question" key={i}>
                <strong>
                  {i + 1}. {q.prompt}
                </strong>
                <ol type="A">
                  {q.options.map((o, n) => (
                    <li
                      key={n}
                      className={n === q.correct ? "admin-accent" : ""}
                    >
                      {o}
                      {n === q.correct ? " ✓" : ""}
                    </li>
                  ))}
                </ol>
                <p>{q.explanation}</p>
              </div>
            ))}
          </section>
        </div>
      )}
    </div>
  );
}
function QuestionEditor({
  question: q,
  index,
  onChange,
  remove,
}: {
  question: QuizQuestion;
  index: number;
  onChange: (q: QuizQuestion) => void;
  remove?: () => void;
}) {
  return (
    <div className="admin-question">
      <div className="admin-panel-heading">
        <h3>Question {index + 1}</h3>
        {remove && (
          <button type="button" className="admin-link" onClick={remove}>
            Remove question
          </button>
        )}
      </div>
      <label>
        Question prompt
        <textarea
          value={q.prompt}
          rows={2}
          maxLength={2000}
          onChange={(e) => onChange({ ...q, prompt: e.target.value })}
        />
      </label>
      <div className="admin-form-grid">
        {q.options.map((o, n) => (
          <label key={n}>
            Option {String.fromCharCode(65 + n)}
            <input
              value={o}
              maxLength={500}
              onChange={(e) =>
                onChange({
                  ...q,
                  options: q.options.map((old, i) =>
                    i === n ? e.target.value : old,
                  ),
                })
              }
            />
          </label>
        ))}
      </div>
      <label>
        Correct answer
        <select
          value={q.correct}
          onChange={(e) => onChange({ ...q, correct: Number(e.target.value) })}
        >
          {q.options.map((_, n) => (
            <option key={n} value={n}>
              Option {String.fromCharCode(65 + n)}
            </option>
          ))}
        </select>
      </label>
      <label>
        Explanation shown after submission
        <textarea
          value={q.explanation}
          rows={2}
          maxLength={4000}
          onChange={(e) => onChange({ ...q, explanation: e.target.value })}
        />
      </label>
    </div>
  );
}
function MediaPreview({
  file,
  record,
}: {
  file?: File;
  record?: UploadRecord;
}) {
  const [src, setSrc] = useState("");
  useEffect(() => {
    if (file) {
      const u = URL.createObjectURL(file);
      setSrc(u);
      return () => URL.revokeObjectURL(u);
    }
    setSrc(
      record?.status === "verified"
        ? `${import.meta.env.VITE_SUPABASE_URL}/storage/v1/object/public/arivihan-reels/${record.video_object}`
        : "",
    );
  }, [file, record?.id, record?.status]);
  return src ? (
    <video
      className="admin-editor-video"
      controls
      playsInline
      preload="metadata"
      src={src}
    />
  ) : null;
}
