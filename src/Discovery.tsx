import React, { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import {
  ArrowLeft,
  Bookmark,
  Check,
  Coins,
  Heart,
  Pause,
  Play,
  Search,
  Share2,
  Volume2,
  VolumeX,
  X,
  BookOpen,
} from "lucide-react";
import { service, liveMode, balance } from "./services";
import {
  extendQueue,
  type DiscoveryService,
  type DiscoveryAttempt,
  type Lesson,
  type DiscoveryState,
} from "./services/discovery";
import type { Snapshot } from "./services/contracts";
import "./discovery.css";

type Saved = {
  queue: string[];
  index: number;
  tab: "feed" | "explore";
  search: string;
  topic: string;
  scroll: number;
  positions: Record<string, number>;
  returnFeed?: { queue: string[]; index: number };
  attempt?: string;
};
function readSaved(key: string): Saved {
  try {
    return {
      queue: [],
      index: 0,
      tab: "feed",
      search: "",
      topic: "",
      scroll: 0,
      positions: {},
      ...JSON.parse(sessionStorage.getItem(key) || "{}"),
    };
  } catch {
    return {
      queue: [],
      index: 0,
      tab: "feed",
      search: "",
      topic: "",
      scroll: 0,
      positions: {},
    };
  }
}
const api = service as typeof service & DiscoveryService;
export function Discovery({
  s,
  refresh,
}: {
  s: Snapshot;
  refresh: () => Promise<void>;
}) {
  const key = `ar-discovery-v1:${s.profile!.id}`;
  const [saved, setSaved] = useState(() => readSaved(key));
  const state = useRef(saved);
  state.current = saved;
  const [lessons, setLessons] = useState<Lesson[]>([]),
    [pending, setPending] = useState<DiscoveryState["pending"]>([]);
  const [error, setError] = useState(""),
    [loading, setLoading] = useState(true),
    [busy, setBusy] = useState(false);
  const [invite, setInvite] = useState<string | null>(null),
    [attempt, setAttempt] = useState<DiscoveryAttempt | null>(null);
  const [question, setQuestion] = useState(0),
    [selected, setSelected] = useState<number | null>(null),
    [note, setNote] = useState<Lesson | null>(null);
  const [muted, setMuted] = useState(s.mute),
    [paused, setPaused] = useState(false),
    [hidden, setHidden] = useState(document.hidden),
    [transitioning, setTransitioning] = useState(false);
  const [message, setMessage] = useState(""),
    [explorePlayer, setExplorePlayer] = useState(false);
  const [celebrate, setCelebrate] = useState(false);
  const feed = useRef<HTMLDivElement>(null),
    grid = useRef<HTMLDivElement>(null),
    operations = useRef(Promise.resolve()),
    offerBusy = useRef(false);
  const mounted = useRef(true);
  const report = (fn: () => Promise<void>) => {
    operations.current = operations.current.then(fn).catch((e) => {
      if (mounted.current) setError(e.message);
    });
  };
  const update = (patch: Partial<Saved>) =>
    setSaved((v) => ({ ...v, ...patch }));
  const task = async (fn: () => Promise<void>) => {
    if (busy) return;
    setBusy(true);
    setError("");
    try {
      await fn();
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setBusy(false);
    }
  };
  useEffect(() => {
    sessionStorage.setItem(key, JSON.stringify(saved));
  }, [key, saved]);
  useEffect(() => {
    mounted.current = true;
    const visibility = () => setHidden(document.hidden);
    document.addEventListener("visibilitychange", visibility);
    return () => {
      mounted.current = false;
      document.removeEventListener("visibilitychange", visibility);
    };
  }, []);
  const load = async () => {
    setLoading(true);
    setError("");
    try {
      if (!liveMode)
        throw Error(
          "The revised Feed uses the live Supabase adapter. The separate local chapter demo is available from Home. Start with npm run live for connected Feed, quizzes and wallet.",
        );
      const [rows, p] = await Promise.all([
        api.discoveryCatalogue(),
        api.discoveryState(),
      ]);
      const grade = /12/.test(s.profile!.batch) ? 12 : 11;
      const eligible = rows.filter(
        (l) =>
          l.grades.includes(grade) && l.languages.includes(s.profile!.language),
      );
      setLessons(eligible);
      setPending(p.pending);
      const ids = new Set(eligible.map((l) => l.id));
      setSaved((v) => {
        const queue = v.queue.filter((id) => ids.has(id));
        const shared = new URLSearchParams(location.search).get("lesson");
        if (shared && ids.has(shared) && queue[0] !== shared)
          return {
            ...v,
            tab: "feed",
            queue: extendQueue(eligible, [shared]),
            index: 0,
          };
        return {
          ...v,
          queue: queue.length ? queue : extendQueue(eligible, []),
          index: Math.min(v.index, Math.max(0, queue.length - 1)),
        };
      });
      if (state.current.attempt) {
        const a = await api.discoveryReview(state.current.attempt);
        setAttempt(a);
        setQuestion(
          Math.max(
            0,
            a.answers.findIndex((x) => !x),
          ),
        );
      }
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setLoading(false);
    }
  };
  useEffect(() => {
    void load();
  }, []);
  useEffect(() => {
    if (saved.tab === "feed" && feed.current && !loading)
      feed.current.scrollTop = saved.index * feed.current.clientHeight;
    if (saved.tab === "explore" && !explorePlayer && grid.current)
      grid.current.scrollTop = saved.scroll;
  }, [saved.tab, loading, explorePlayer]);
  useEffect(() => {
    if (!message) return;
    const timer = setTimeout(() => setMessage(""), 3500);
    return () => clearTimeout(timer);
  }, [message]);
  const checkOffer = async () => {
    if (offerBusy.current || invite || attempt) return;
    offerBusy.current = true;
    setTransitioning(true);
    try {
      await operations.current;
      const offer = await api.discoveryOffer();
      if (offer.ready && offer.id) setInvite(offer.id);
      else if (offer.diagnostic)
        setMessage(
          "This set needs more mapped lesson questions. Keep exploring.",
        );
      setPending((await api.discoveryState()).pending);
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setTransitioning(false);
      offerBusy.current = false;
    }
  };
  const move = (index: number) => {
    if (index === state.current.index) return;
    setPaused(false);
    setSaved((v) => ({
      ...v,
      index,
      queue:
        index >= v.queue.length - 3
          ? extendQueue(lessons, v.queue, 8)
          : v.queue,
    }));
    void checkOffer();
  };
  const begin = async (id: string) => {
    setCelebrate(false);
    await operations.current;
    const a = await api.discoveryStart(id);
    setAttempt(a);
    update({ attempt: a.id });
    setQuestion(
      Math.max(
        0,
        a.answers.findIndex((x) => !x),
      ),
    );
    setSelected(null);
    setInvite(null);
    setPending((await api.discoveryState()).pending);
  };
  const defer = async () => {
    const id = invite || attempt?.batchId;
    if (id) await api.discoveryDefer(id);
    setInvite(null);
    setAttempt(null);
    update({ attempt: undefined });
    setPending((await api.discoveryState()).pending);
  };
  const finish = async () => {
    if (!attempt) return;
    const a = await api.discoveryFinish(attempt.id);
    setAttempt(a);
    await refresh();
    setPending((await api.discoveryState()).pending);
    if (a.rewarded && !localStorage.getItem(`ar-celebrated:${a.id}`)) {
      setCelebrate(true);
      localStorage.setItem(`ar-celebrated:${a.id}`, "1");
    }
  };
  const playing =
    (saved.tab === "feed" || explorePlayer) &&
    !attempt &&
    !invite &&
    !note &&
    !hidden &&
    !paused &&
    !transitioning;
  const filtered = lessons.filter(
    (l) =>
      (!saved.topic || l.topic === saved.topic) &&
      `${l.title} ${l.concept} ${l.subject} ${l.topic}`
        .toLowerCase()
        .includes(saved.search.toLowerCase()),
  );
  const selectTab = (tab: "feed" | "explore") => {
    setSaved((v) => ({
      ...v,
      ...(v.returnFeed || {}),
      tab,
      returnFeed: undefined,
    }));
    setExplorePlayer(false);
  };
  const openLesson = (id: string) => {
    const rest = extendQueue(lessons, [id], 12);
    setSaved((v) => ({
      ...v,
      returnFeed: v.returnFeed || { queue: v.queue, index: v.index },
      queue: rest,
      index: 0,
    }));
    setExplorePlayer(true);
    setPaused(false);
    if (feed.current) feed.current.scrollTop = 0;
  };
  const activeAnswer = attempt?.answers[question];
  if (s.profile!.persona !== "Pro")
    return (
      <div className="discovery-empty">
        <BookOpen />
        <h2>Reels requires Pro access</h2>
        <p>
          The demo checkout is simulated and does not activate a subscription.
        </p>
        <Link className="button" to="/store">
          Explore demo products
        </Link>
        <Link to="/">Back home</Link>
      </div>
    );
  return (
    <div className="discovery">
      <header className="discovery-header">
        {explorePlayer ? (
          <button
            aria-label="Back to Explore"
            className="icon"
            onClick={() => selectTab("explore")}
          >
            <ArrowLeft size={21} />
          </button>
        ) : (
          <Link to="/" aria-label="Back to home" className="icon">
            <ArrowLeft size={21} />
          </Link>
        )}
        <div className="discovery-tabs">
          <button
            className={saved.tab === "feed" && !explorePlayer ? "selected" : ""}
            onClick={() => selectTab("feed")}
          >
            Feed
          </button>
          <button
            className={saved.tab === "explore" ? "selected" : ""}
            onClick={() => selectTab("explore")}
          >
            Explore
          </button>
        </div>
        <Link
          to="/wallet"
          className="coin-action"
          aria-label={`Wallet ${balance(s)} coins`}
        >
          <Coins size={18} />
          {balance(s)}
        </Link>
      </header>
      {loading ? (
        <div className="discovery-empty" role="status">
          Loading your lessons…
        </div>
      ) : !lessons.length ? (
        <div className="discovery-empty">
          <BookOpen />
          <h2>
            {error ? "Lessons unavailable" : "More lessons are on their way"}
          </h2>
          <p>
            {error || "No supplied videos match your class and language yet."}
          </p>
          <button className="button" onClick={() => void load()}>
            Retry
          </button>
          <Link to="/">Go home</Link>
        </div>
      ) : (
        <>
          {(saved.tab === "feed" || explorePlayer) && (
            <div
              className="feed-scroll"
              ref={feed}
              onScroll={(e) => {
                const el = e.currentTarget;
                const index = Math.round(el.scrollTop / el.clientHeight);
                if (index >= 0 && index < state.current.queue.length)
                  move(index);
              }}
            >
              {saved.queue.map((id, i) => {
                const l = lessons.find((x) => x.id === id);
                if (!l) return null;
                return (
                  <article
                    className="feed-slide"
                    key={`${id}:${i}`}
                    aria-label={l.title}
                  >
                    {Math.abs(i - saved.index) <= 1 ? (
                      <LessonVideo
                        lesson={l}
                        active={i === saved.index}
                        playing={playing && i === saved.index}
                        muted={muted}
                        position={saved.positions[id] || 0}
                        onPosition={(position) =>
                          setSaved((v) => ({
                            ...v,
                            positions: { ...v.positions, [id]: position },
                          }))
                        }
                        onProgress={(from, to, wall, speed) =>
                          report(() =>
                            api.discoveryProgress(
                              id,
                              from,
                              to,
                              wall,
                              speed,
                              true,
                              true,
                            ),
                          )
                        }
                        onEnded={() => {
                          feed.current?.scrollTo({top:(i+1)*feed.current.clientHeight});
                        }}
                        onError={setError}
                      />
                    ) : (
                      <img src={l.poster} alt="" />
                    )}
                    {i === saved.index && (
                      <>
                        <div className="video-topline">
                          <span>
                            {l.subject} · Class {l.grades[0]}
                          </span>
                          <button
                            onClick={() => {
                              const v = !muted;
                              setMuted(v);
                              void task(async () => {
                                await service.mute(v);
                                await refresh();
                              });
                            }}
                            aria-label={muted ? "Unmute" : "Mute"}
                          >
                            {muted ? <VolumeX /> : <Volume2 />}
                          </button>
                        </div>
                        <button
                          className="play-toggle"
                          onClick={() => setPaused((v) => !v)}
                          aria-label={paused ? "Play lesson" : "Pause lesson"}
                        >
                          {paused ? <Play /> : <Pause />}
                        </button>
                        <aside className="video-actions">
                          <button
                            aria-label="Like lesson"
                            className={s.likes.includes(id) ? "liked" : ""}
                            onClick={() =>
                              void task(async () => {
                                await service.preference("likes", id);
                                await refresh();
                              })
                            }
                          >
                            <Heart
                              fill={
                                s.likes.includes(id) ? "currentColor" : "none"
                              }
                            />
                            <span>Like</span>
                          </button>
                          <button
                            aria-label="Save lesson"
                            className={s.saved.includes(id) ? "saved" : ""}
                            onClick={() =>
                              void task(async () => {
                                await service.preference("saved", id);
                                await refresh();
                              })
                            }
                          >
                            <Bookmark
                              fill={
                                s.saved.includes(id) ? "currentColor" : "none"
                              }
                            />
                            <span>Save</span>
                          </button>
                          <button
                            aria-label="Share lesson"
                            onClick={() =>
                              void task(async () => {
                                const url = new URL("/reels", location.origin);
                                url.searchParams.set("lesson", id);
                                if (navigator.share)
                                  await navigator.share({
                                    title: l.title,
                                    url: url.href,
                                  });
                                else {
                                  await navigator.clipboard.writeText(url.href);
                                  setMessage("Lesson link copied");
                                }
                              })
                            }
                          >
                            <Share2 />
                            <span>Share</span>
                          </button>
                          <button
                            aria-label="Lesson notes"
                            onClick={() => setNote(l)}
                          >
                            <BookOpen />
                            <span>Notes</span>
                          </button>
                        </aside>
                        <div className="video-caption">
                          <span className="lesson-topic">{l.topic}</span>
                          <h1>{l.title}</h1>
                          <p>{l.concept}</p>
                          <div className="video-footer">
                            <span>Arivihan · Supplied lesson</span>
                            <button
                              onClick={() => {
                                const next = Math.min(
                                  saved.index + 1,
                                  saved.queue.length - 1,
                                );
                                feed.current?.scrollTo({
                                  top: next * feed.current.clientHeight,
                                  behavior: "smooth",
                                });
                              }}
                            >
                              Next ↑
                            </button>
                          </div>
                        </div>
                      </>
                    )}
                  </article>
                );
              })}
            </div>
          )}
          {saved.tab === "explore" && !explorePlayer && (
            <section
              className="explore-scroll"
              ref={grid}
              onScroll={(e) => update({ scroll: e.currentTarget.scrollTop })}
            >
              <div className="explore-intro">
                <span className="eyebrow cyan">FOLLOW YOUR CURIOSITY</span>
                <h1>
                  Find your next
                  <br />
                  “now I get it.”
                </h1>
                <p>
                  {s.profile!.batch} · {s.profile!.language}
                </p>
              </div>
              <label className="discovery-search">
                <Search size={18} />
                <input
                  placeholder="Search a concept, topic or chapter"
                  aria-label="Search lessons"
                  value={saved.search}
                  onChange={(e) => update({ search: e.target.value })}
                />
                {saved.search && (
                  <button
                    aria-label="Clear search"
                    onClick={() => update({ search: "" })}
                  >
                    <X size={16} />
                  </button>
                )}
              </label>
              <div className="topic-filters">
                <button
                  className={!saved.topic ? "chosen" : ""}
                  onClick={() => update({ topic: "" })}
                >
                  All topics
                </button>
                {[...new Set(lessons.map((l) => l.topic))].map((t) => (
                  <button
                    key={t}
                    className={saved.topic === t ? "chosen" : ""}
                    onClick={() => update({ topic: t })}
                  >
                    {t}
                  </button>
                ))}
              </div>
              <div className="discovery-grid">
                {filtered.map((l) => (
                  <button
                    className="lesson-tile"
                    key={l.id}
                    onClick={() => openLesson(l.id)}
                  >
                    <div>
                      <img src={l.poster} alt="" loading="lazy" />
                      <span className="tile-play">
                        <Play size={16} fill="currentColor" />
                      </span>
                      <span className="tile-duration">
                        {Math.floor(l.duration / 60)}:
                        {String(Math.floor(l.duration % 60)).padStart(2, "0")}
                      </span>
                    </div>
                    <span>{l.topic}</span>
                    <h2>{l.title}</h2>
                  </button>
                ))}
              </div>
              {!filtered.length && (
                <div className="discovery-empty">
                  <Search />
                  <h2>No matching lessons</h2>
                  <p>Try another concept or clear your filters.</p>
                  <button
                    className="button secondary"
                    onClick={() => update({ search: "", topic: "" })}
                  >
                    Clear filters
                  </button>
                </div>
              )}
            </section>
          )}
          {pending.length > 0 && !attempt && !invite && (
            <button
              className="pending-quiz"
              disabled={busy}
              onClick={() => void task(() => begin(pending[0].id))}
            >
              ✦ {pending[0].status === "started" ? "Resume" : "Your"} quick quiz
            </button>
          )}
        </>
      )}
      {message && (
        <div className="discovery-message" role="status">
          {message}
        </div>
      )}
      {error && lessons.length > 0 && (
        <div className="discovery-error" role="alert">
          <span>{error}</span>
          <button aria-label="Dismiss error" onClick={() => setError("")}>
            <X size={18} />
          </button>
        </div>
      )}
      {invite && !attempt && (
        <div className="discovery-scrim">
          <section
            className="discovery-sheet"
            role="dialog"
            aria-modal="true"
            aria-label="Quick recall"
          >
            <span className="quiz-symbol">✦</span>
            <span className="eyebrow cyan">A MOMENT TO RECALL</span>
            <h2>Make those ideas stick.</h2>
            <p>
              Five questions from the lessons you watched.
              <br />
              Get all five right to earn five coins.
            </p>
            <button
              className="button"
              disabled={busy}
              onClick={() => void task(() => begin(invite))}
            >
              Start quick quiz
            </button>
            <button
              className="text-button"
              disabled={busy}
              onClick={() => void task(defer)}
            >
              Later, keep watching
            </button>
          </section>
        </div>
      )}
      {note && (
        <div className="discovery-scrim">
          <section
            className="discovery-sheet"
            role="dialog"
            aria-modal="true"
            aria-label="Lesson notes"
          >
            <button
              className="sheet-close icon"
              aria-label="Close notes"
              onClick={() => setNote(null)}
            >
              <X />
            </button>
            <span className="eyebrow cyan">REVISION NOTE</span>
            <h2>{note.title}</h2>
            <p>{note.concept}</p>
            <p>
              Watch the teaching diagrams again to revise this concept. Full
              transcripts and offline video downloads are not available in this
              web demo.
            </p>
            <button
              className="button"
              onClick={() => {
                const blob = new Blob(
                  [
                    `${note.title}\n${note.topic}\n${note.concept}\nSource: supplied Arivihan demo lesson\n`,
                  ],
                  { type: "text/plain" },
                );
                const url = URL.createObjectURL(blob);
                const a = document.createElement("a");
                a.href = url;
                a.download = `${note.id}-note.txt`;
                a.click();
                setTimeout(() => URL.revokeObjectURL(url), 1000);
              }}
            >
              Download revision note
            </button>
          </section>
        </div>
      )}
      {attempt && (
        <section className="discovery-quiz" aria-label="Recall quiz">
          <header>
            <button
              className="icon"
              aria-label="Save quiz for later"
              disabled={busy}
              onClick={() => void task(defer)}
            >
              <ArrowLeft />
            </button>
            <span>{attempt.finished ? "YOUR RECALL" : "QUICK RECALL"}</span>
            <Coins color="#efc86b" />
          </header>
          {attempt.finished ? (
            <div className="quiz-result">
              <div className={`result-medal ${celebrate && attempt.rewarded ? "coin-pop" : ""}`}>
                {attempt.rewarded ? "+5" : `${attempt.score}/5`}
              </div>
              <span className="eyebrow cyan">
                {attempt.score === 5
                  ? "ALL FIVE. WELL REMEMBERED."
                  : "A LITTLE REVISION GOES A LONG WAY."}
              </span>
              <h1>
                {attempt.rewarded
                  ? "Five ideas. Five coins."
                  : attempt.score === 5
                    ? "Perfect practice."
                    : "Keep your curiosity going."}
              </h1>
              <p>
                {attempt.rewarded
                  ? "Five coins have been added to your wallet. ₹1, earned by learning."
                  : attempt.score === 5
                    ? "This lesson set has already earned its five-coin reward."
                    : "No coins this time. Revisit the lessons and try again — a later perfect attempt can earn the reward."}
              </p>
              <div className="quiz-review">
                {attempt.questions.map((q, i) => (
                  <div key={q.id}>
                    <strong>
                      {i + 1}. {q.prompt}
                    </strong>
                    <p>{attempt.answers[i]?.explanation}</p>
                    <button
                      className="text-button"
                      onClick={() => {
                        setAttempt(null);
                        update({ attempt: undefined });
                        openLesson(q.reelId);
                      }}
                    >
                      Replay source lesson →
                    </button>
                  </div>
                ))}
              </div>
              <button
                className="button"
                onClick={() => {
                  setAttempt(null);
                  setCelebrate(false);
                  update({ attempt: undefined });
                }}
              >
                Back to Reels
              </button>
              <button
                className="text-button"
                disabled={busy}
                onClick={() => void task(() => begin(attempt.batchId))}
              >
                Practice again
              </button>
              <Link to="/wallet" className="text-button">
                View wallet
              </Link>
            </div>
          ) : (
            <div className="quiz-body">
              <div className="quiz-progress">
                {attempt.questions.map((q, i) => (
                  <span key={q.id} className={i <= question ? "filled" : ""} />
                ))}
              </div>
              <span className="eyebrow">QUESTION {question + 1} OF 5</span>
              <span className="source-chip">
                From{" "}
                {lessons.find(
                  (l) => l.id === attempt.questions[question].reelId,
                )?.title || "your watched lesson"}
              </span>
              <h1>{attempt.questions[question].prompt}</h1>
              <div className="quiz-options">
                {attempt.questions[question].options.map((option, i) => (
                  <button
                    key={i}
                    disabled={!!activeAnswer || busy}
                    className={`${selected === i ? "picked" : ""} ${activeAnswer?.correct === i ? "correct" : ""} ${activeAnswer && activeAnswer.selected === i && activeAnswer.correct !== i ? "incorrect" : ""}`}
                    onClick={() => setSelected(i)}
                  >
                    <span>{String.fromCharCode(65 + i)}</span>
                    {option}
                    {activeAnswer?.correct === i && <Check size={18} />}
                  </button>
                ))}
              </div>
              {activeAnswer ? (
                <div className="answer-feedback">
                  <strong>
                    {activeAnswer.selected === activeAnswer.correct
                      ? "You got it."
                      : "One to remember."}
                  </strong>
                  <p>{activeAnswer.explanation}</p>
                </div>
              ) : (
                <p className="quiz-rule">
                  All five correct = five coins total. You can always retry.
                </p>
              )}
              <button
                className="button"
                disabled={busy || (!activeAnswer && selected === null)}
                onClick={() =>
                  void task(async () => {
                    if (!activeAnswer) {
                      setAttempt(
                        await api.discoverySubmit(
                          attempt.id,
                          question,
                          selected!,
                        ),
                      );
                    } else if (question === 4) {
                      await finish();
                    } else {
                      setQuestion(question + 1);
                      setSelected(null);
                    }
                  })
                }
              >
                {busy
                  ? "Saving…"
                  : activeAnswer
                    ? question === 4
                      ? "See my result"
                      : "Next question"
                    : "Submit answer"}
              </button>
            </div>
          )}
        </section>
      )}
    </div>
  );
}

function LessonVideo({
  lesson,
  active,
  playing,
  muted,
  position,
  onPosition,
  onProgress,
  onEnded,
  onError,
}: {
  lesson: Lesson;
  active: boolean;
  playing: boolean;
  muted: boolean;
  position: number;
  onPosition: (p: number) => void;
  onProgress: (from: number, to: number, wall: number, speed: number) => void;
  onEnded: () => void;
  onError: (e: string) => void;
}) {
  const video = useRef<HTMLVideoElement>(null),
    last = useRef<{ position: number; at: number } | null>(null);
  const callbacks = useRef({ onPosition, onProgress, onEnded, onError });
  callbacks.current = { onPosition, onProgress, onEnded, onError };
  const [blocked, setBlocked] = useState(false),
    [failed, setFailed] = useState(false),
    [waiting, setWaiting] = useState(true),
    [elapsed, setElapsed] = useState(position),
    [speed, setSpeed] = useState(1);
  const sample = () => {
    const v = video.current;
    if (!v || !active) return;
    setElapsed(v.currentTime);
    const now = performance.now();
    const previous = last.current;
    last.current = { position: v.currentTime, at: now };
    callbacks.current.onPosition(v.currentTime);
    if (
      !previous ||
      v.paused ||
      v.seeking ||
      document.hidden ||
      !active ||
      !playing
    )
      return;
    const wall = (now - previous.at) / 1000,
      delta = v.currentTime - previous.position;
    if (
      wall > 0 &&
      wall <= 2 &&
      delta > 0 &&
      delta <= wall * v.playbackRate + 0.15
    )
      callbacks.current.onProgress(
        previous.position,
        v.currentTime,
        wall,
        v.playbackRate,
      );
  };
  useEffect(() => {
    const v = video.current;
    if (!v) return;
    last.current = null;
    if (active && playing) {
      v.play()
        .then(() => setBlocked(false))
        .catch(() => setBlocked(true));
    } else v.pause();
    return () => {
      v.pause();
      last.current = null;
    };
  }, [active, playing]);
  useEffect(() => {
    if (video.current) {
      video.current.playbackRate = speed;
      last.current = null;
    }
  }, [speed]);
  const sampleRef = useRef(sample);
  sampleRef.current = sample;
  useEffect(() => {
    const timer = setInterval(() => sampleRef.current(), 500);
    return () => clearInterval(timer);
  }, []);
  return (
    <>
      <video
        ref={video}
        src={lesson.source}
        poster={lesson.poster}
        muted={muted}
        playsInline
        preload={active ? "auto" : "metadata"}
        onLoadedMetadata={() => {
          if (video.current && position > 0 && position < lesson.duration - 1)
            video.current.currentTime = position;
        }}
        onSeeking={() => {
          last.current = null;
        }}
        onSeeked={() => {
          last.current = null;
        }}
        onWaiting={() => {
          last.current = null;
          setWaiting(true);
        }}
        onPlaying={() => {
          last.current = null;
          setWaiting(false);
        }}
        onPause={() => {
          last.current = null;
        }}
        onEnded={() => {
          sampleRef.current();
          callbacks.current.onEnded();
        }}
        onError={() => {
          setFailed(true);
          setWaiting(false);
          callbacks.current.onError(
            "This supplied lesson could not load. Retry the video or explore another lesson.",
          );
        }}
        controls={false}
      />
      {active && (
        <>
          <select
            className="video-speed"
            aria-label="Playback speed"
            value={speed}
            onChange={(e) => setSpeed(Number(e.target.value))}
          >
            <option value={1}>1x</option>
            <option value={1.5}>1.5x</option>
            <option value={2}>2x</option>
          </select>
          <input
            className="watch-timeline"
            aria-label="Lesson position"
            type="range"
            min="0"
            max={lesson.duration}
            step="0.1"
            value={elapsed}
            onChange={(e) => {
              if (video.current) {
                last.current = null;
                video.current.currentTime = Number(e.target.value);
                setElapsed(Number(e.target.value));
              }
            }}
          />
        </>
      )}
      {active && waiting && !failed && (
        <span className="video-loading" role="status">
          Loading lesson…
        </span>
      )}
      {active && (blocked || failed) && (
        <button
          className="video-retry button"
          onClick={() => {
            const v = video.current;
            if (!v) return;
            if (failed) {
              setFailed(false);
              v.load();
            }
            void v
              .play()
              .then(() => setBlocked(false))
              .catch(() => setBlocked(true));
          }}
        >
          {failed ? "Retry lesson" : "Tap to play"}
        </button>
      )}
    </>
  );
}
