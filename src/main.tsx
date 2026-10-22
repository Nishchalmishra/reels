import React, { useCallback, useEffect, useRef, useState } from "react";
import { createRoot } from "react-dom/client";
import {
  BrowserRouter,
  Link,
  useNavigate,
  useLocation,
  useParams,
} from "react-router-dom";
import {
  ArrowLeft,
  ArrowRight,
  BookOpen,
  Bookmark,
  Check,
  CheckCircle2,
  ChevronDown,
  ChevronRight,
  Coins,
  Download,
  Heart,
  Home,
  Layers,
  Lock,
  MoreHorizontal,
  Pause,
  Play,
  Search,
  Share2,
  ShoppingBag,
  Sparkles,
  Trophy,
  Volume2,
  VolumeX,
  X,
  RotateCcw,
  Settings,
  GraduationCap,
} from "lucide-react";
import {
  featureConfig,
  type Chapter,
  type Reel,
  type Product,
} from "./content";
import {
  service,
  qualified as isQualified,
  balance,
  liveMode,
} from "./services";
import type { Snapshot, Attempt, Quote } from "./services/contracts";
import "./styles.css";
import { Discovery } from "./Discovery";
const money = (p: number) =>
  new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 2,
  }).format(p / 100);
const initial: Snapshot = {
  profile: null,
  coverage: {},
  positions: {},
  checkouts: {},
  attempts: [],
  ledger: [],
  orders: [],
  events: [],
  deferred: [],
  offered: [],
  likes: [],
  saved: [],
  downloads: [],
  mute: true,
  lastReel: null,
  presenter: false,
  scenario: "fresh",
};
const icons = { Physics: "↗", Chemistry: "⚗", Biology: "❋" };
function Button({
  children,
  onClick,
  disabled = false,
  secondary = false,
  className = "",
  type = "button",
}: {
  children: React.ReactNode;
  onClick?: () => void;
  disabled?: boolean;
  secondary?: boolean;
  className?: string;
  type?: "button" | "submit";
}) {
  return (
    <button
      type={type}
      disabled={disabled}
      onClick={onClick}
      className={`${secondary ? "button secondary" : "button"} ${className}`}
    >
      {children}
    </button>
  );
}
function Modal({
  title,
  children,
  close,
}: {
  title: string;
  children: React.ReactNode;
  close: () => void;
}) {
  const ref = useRef<HTMLDivElement>(null);
  useEffect(() => {
    const prev = document.activeElement as HTMLElement;
    const el = ref.current;
    el?.querySelector<HTMLElement>("button,input,a")?.focus();
    const key = (e: KeyboardEvent) => {
      if (e.key === "Escape") close();
      if (e.key === "Tab") {
        const items = el?.querySelectorAll<HTMLElement>(
          "button:not([disabled]),a,input",
        );
        if (!items?.length) return;
        const first = items[0],
          last = items[items.length - 1];
        if (e.shiftKey && document.activeElement === first) {
          e.preventDefault();
          last.focus();
        } else if (!e.shiftKey && document.activeElement === last) {
          e.preventDefault();
          first.focus();
        }
      }
    };
    document.addEventListener("keydown", key);
    return () => {
      document.removeEventListener("keydown", key);
      prev?.focus();
    };
  }, [close]);
  return (
    <div className="scrim" onClick={close}>
      <div
        className="sheet"
        role="dialog"
        aria-modal="true"
        aria-label={title}
        ref={ref}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="sheet-handle" />
        <div className="row">
          <h2>{title}</h2>
          <button className="icon" aria-label="Close dialog" onClick={close}>
            <X />
          </button>
        </div>
        {children}
      </div>
    </div>
  );
}
function Cover({
  chapter,
  small = false,
}: {
  chapter: Chapter;
  small?: boolean;
}) {
  return (
    <div
      className={`cover ${chapter.color} ${small ? "small" : ""}`}
      aria-hidden="true"
    >
      <div className="cover-title">
        <span>{chapter.subject.toUpperCase()}</span>
        <strong>{chapter.title}</strong>
      </div>
      <div className="orbit o1" />
      <div className="orbit o2" />
      {chapter.id === "units" ? (
        <div className="ruler-art">
          <span>0</span>
          <span>1</span>
          <span>2</span>
          <span>3</span>
          <i />
        </div>
      ) : chapter.id === "motion" ? (
        <>
          <div className="planet" />
          <div className="arrow-art">↗</div>
          <span className="formula-art">F = ma</span>
        </>
      ) : (
        <div className="atom-art">{chapter.symbol}</div>
      )}
      <span className="cover-label">
        {chapter.subject.toUpperCase()}
        <br />
        <b>
          {chapter.id === "units"
            ? "MEASURE YOUR WORLD"
            : chapter.id === "motion"
              ? "MAKE YOUR MOVE"
              : "A CLOSER LOOK"}
        </b>
      </span>
      {!chapter.available && <span className="soon-tag">COMING SOON</span>}
    </div>
  );
}
type Ctx = {
  s: Snapshot;
  nav: ReturnType<typeof useNavigate>;
  loc: ReturnType<typeof useLocation>;
  run: (fn: () => Promise<void>) => Promise<void>;
  refresh: () => Promise<void>;
  show: (title: string, body: React.ReactNode) => void;
  close: () => void;
  pro: () => void;
  coming: (c: Chapter) => void;
  start: (c: Chapter) => Promise<void>;
  challengeCard: (c: Chapter) => React.ReactNode;
  busy: boolean;
  catalogue: Chapter[];
  chapters: Chapter[];
  products: Product[];
  qualified: (s: Snapshot, id: string) => boolean;
  setCatalogue: React.Dispatch<React.SetStateAction<Chapter[]>>;
  setProducts: React.Dispatch<React.SetStateAction<Product[]>>;
  setError: React.Dispatch<React.SetStateAction<string>>;
  modal: React.ReactNode;
};
const AppContext = React.createContext<Ctx>(null as unknown as Ctx);
function Onboarding() {
  const {
    s,
    nav,
    loc,
    run,
    refresh,
    show,
    close,
    pro,
    coming,
    start,
    challengeCard,
    busy,
    catalogue,
    chapters,
    products,
    qualified,
    setCatalogue,
    setProducts,
    setError,
    modal,
  } = React.useContext(AppContext);
  const [name, setName] = useState(""),
    [batch, setBatch] = useState("Class 11 · NEET"),
    [language, setLanguage] = useState("English");
  return (
    <main className="onboarding">
      <div className="brand">
        <span className="brandmark">a</span> arivihan
        <span className="brand-dot">•</span>
      </div>
      <div className="welcome-art">
        <div className="welcome-orbit" />
        <GraduationCap size={80} />
        <span className="float f1">F = ma</span>
        <span className="float f2">✦</span>
        <span className="float f3">E = hν</span>
      </div>
      <span className="eyebrow">A LITTLE CURIOSITY. EVERY DAY.</span>
      <h1>
        Big ideas.
        <br />
        Small reels.
      </h1>
      <p>
        Make a little time for your next
        <br />
        “oh, now I get it” moment.
      </p>
      <form
        onSubmit={(e) => {
          e.preventDefault();
          run(async () => {
            await service.session({
              name: name.trim() || "Ananya",
              batch,
              language,
              persona: "Pro",
            });
            await service.analytics("reels_entry_clicked");
          });
        }}
      >
        <label>
          Your name
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="What should we call you?"
            maxLength={40}
          />
        </label>
        <div className="form-grid">
          <label>
            Class / batch
            <select value={batch} onChange={(e) => setBatch(e.target.value)}>
              <option>Class 11 · NEET</option>
              <option>Class 12 · NEET</option>
              <option>Class 11 · JEE</option>
            </select>
          </label>
          <label>
            Language
            <select
              value={language}
              onChange={(e) => setLanguage(e.target.value)}
            >
              <option>English</option>
              <option>Hindi</option>
            </select>
          </label>
        </div>
        <Button type="submit" disabled={busy}>
          Let’s start learning <ArrowRight size={18} />
        </Button>
      </form>
      <button
        className="text-button"
        disabled={busy}
        onClick={() =>
          run(async () => {
            await service.session({
              name: "Ananya",
              batch: "Class 11 · NEET",
              language: "English",
              persona: "Pro",
            });
            await service.analytics("reels_entry_clicked");
          })
        }
      >
        {liveMode ? "Continue as guest student" : "Continue with demo student"}
      </button>
      <p className="fine">
        {liveMode ? "Persistent guest profile" : "Local demo profile"} · No
        account needed
        <br />
        Lessons in English · Hindi content coming soon
      </p>
    </main>
  );
}
function HomePage() {
  const {
    s,
    nav,
    loc,
    run,
    refresh,
    show,
    close,
    pro,
    coming,
    start,
    challengeCard,
    busy,
    catalogue,
    chapters,
    products,
    qualified,
    setCatalogue,
    setProducts,
    setError,
    modal,
  } = React.useContext(AppContext);
  const [subject, setSubject] = useState("Physics"),
    [search, setSearch] = useState(""),
    [all, setAll] = useState(false);
  const list = catalogue.filter(
    (c) =>
      (all || c.subject === subject) &&
      `${c.title} ${c.subject}`.toLowerCase().includes(search.toLowerCase()),
  );
  const resume = chapters
    .flatMap((c) => c.reels)
    .find((r) => r.id === s.lastReel);
  return (
    <>
      <header className="home-header">
        <div className="row">
          <div className="brand">
            <span className="brandmark">a</span> arivihan
            <span className="brand-dot">•</span>
          </div>
          <Link to="/wallet" className="wallet-chip">
            <Coins size={17} />
            {balance(s)}
            <ChevronRight size={13} />
          </Link>
        </div>
      </header>
      <main>
        <div className="feature-heading">
          <span className="eyebrow cyan">
            <Sparkles size={14} /> ARIVIHAN REELS
          </span>
          <span className="tiny-label">LEARN. RECALL. GROW.</span>
        </div>
        <h2 className="discovery-title">
          A clearer idea.
          <br />
          One reel at a time.
        </h2>
        <p className="intro">
          Your favourite chapters, one clear idea at a time.
        </p>
        <div className="search">
          <Search size={19} />
          <input
            aria-label="Search chapters"
            placeholder="Find a chapter or concept"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          {search && (
            <button aria-label="Clear search" onClick={() => setSearch("")}>
              <X size={16} />
            </button>
          )}
        </div>
        <div className="subject-tabs">
          {["Physics", "Chemistry", "Biology"].map((x) => (
            <button
              key={x}
              aria-label={x}
              onClick={() => {
                setSubject(x);
                setAll(false);
              }}
              className={subject === x && !all ? "active" : ""}
            >
              <span>{icons[x as keyof typeof icons]}</span>
              {x}
            </button>
          ))}
        </div>
        {resume && (
          <Link
            className="resume-card"
            to={`/player/${resume.chapterId}/${resume.id}`}
          >
            <span className="play-badge">
              <Play size={17} />
            </span>
            <div>
              <span className="eyebrow cyan">PICK UP WHERE YOU LEFT OFF</span>
              <h3>{resume.title}</h3>
              <small className="muted">
                {chapters.find((c) => c.id === resume.chapterId)?.title}
              </small>
            </div>
            <ChevronRight size={18} />
          </Link>
        )}
        <div className="section-title">
          <h2>{all ? "All chapters" : `${subject} chapters`}</h2>
          <button className="text-button" onClick={() => setAll(!all)}>
            {all ? "Show subject" : "See All"}
            <ArrowRight size={14} />
          </button>
        </div>
        <div className="chapter-grid">
          {list.map((c, i) => (
            <button
              className="chapter-tile"
              key={c.id}
              onClick={() =>
                c.available ? nav(`/chapter/${c.id}`) : coming(c)
              }
            >
              <Cover chapter={c} />
              <div className="tile-text">
                <span className="overline">
                  {c.subject.toUpperCase()} · {String(i + 1).padStart(2, "0")}
                </span>
                <h3>{c.title}</h3>
                <p>
                  {c.available
                    ? "5 videos · 1.5 min"
                    : "New lessons on the way"}
                </p>
              </div>
            </button>
          ))}
        </div>
        {list.length === 0 && (
          <div className="empty">
            <Search />
            <h3>No chapters found</h3>
            <p>Try another subject or a shorter search.</p>
          </div>
        )}
        {!search &&
          !all &&
          subject === "Physics" &&
          ["Chemistry", "Biology"].map((sub) => (
            <section key={sub}>
              <div className="section-title">
                <h2>{sub}</h2>
                <button
                  className="text-button"
                  onClick={() => {
                    setSubject(sub);
                    window.scrollTo({ top: 0, behavior: "smooth" });
                  }}
                >
                  See All <ArrowRight size={14} />
                </button>
              </div>
              <div className="chapter-grid">
                {catalogue
                  .filter((c) => c.subject === sub)
                  .map((c) => (
                    <button
                      className="chapter-tile"
                      key={c.id}
                      onClick={() => coming(c)}
                    >
                      <Cover chapter={c} />
                      <div className="tile-text">
                        <h3>{c.title}</h3>
                        <p>Coming Soon</p>
                      </div>
                    </button>
                  ))}
              </div>
            </section>
          ))}
        <div className="learning-note">
          <div className="coin-orb">
            <Coins />
          </div>
          <div>
            <h3>A little learning. A little reward.</h3>
            <p>
              Watch 5 lessons in a chapter, ace 5 questions.
              <br />
              <b>5 coins = ₹1</b> towards your learning essentials.
            </p>
          </div>
        </div>
        <div className="section-title">
          <h2>Beyond the lesson</h2>
          <Link className="text-button" to="/recognition">
            Your cohort <ArrowRight size={14} />
          </Link>
        </div>
        {featureConfig.promo && (
          <button
            className="promo"
            onClick={() =>
              show(
                "Meet your study companion",
                <>
                  <div className="modal-emblem">
                    <Sparkles />
                  </div>
                  <h3>A clearer path through difficult concepts</h3>
                  <p>
                    This Arivihan feature preview demonstrates a study plan:
                    watch a concept, read the note, test your understanding,
                    then revise.
                  </p>
                  <Button
                    onClick={() => {
                      close();
                      nav("/chapter/units");
                    }}
                  >
                    Try the learning flow
                  </Button>
                  <p className="fine">
                    Feature promotion preview · No challenge credit
                  </p>
                </>,
              )
            }
          >
            <span className="eyebrow">FEATURE PREVIEW · PROMOTION</span>
            <h3>
              A doubt today.
              <br />A breakthrough tomorrow.
            </h3>
            <span className="cyan">
              Explore guided learning <ArrowRight size={15} />
            </span>
            <Sparkles className="promo-icon" size={52} />
          </button>
        )}
        {featureConfig.spotlight && (
          <Link to="/recognition" className="spotlight">
            <div className="spot-avatar">M</div>
            <div>
              <span className="eyebrow gold">
                STUDENT SPOTLIGHT · FICTIONAL DEMO
              </span>
              <h3>Meet Meera, the curious one.</h3>
              <p>Small steps make confident learners.</p>
            </div>
            <ChevronRight size={18} />
          </Link>
        )}
        <p className="fine footnote">
          Made for curious minds. Built for your pace.
        </p>
      </main>
    </>
  );
}
function ChapterPage({ id }: { id: string }) {
  const {
    s,
    nav,
    loc,
    run,
    refresh,
    show,
    close,
    pro,
    coming,
    start,
    challengeCard,
    busy,
    catalogue,
    chapters,
    products,
    qualified,
    setCatalogue,
    setProducts,
    setError,
    modal,
  } = React.useContext(AppContext);
  const c = chapters.find((c) => c.id === id);
  useEffect(() => {
    if (c) void service.analytics("reels_chapter_viewed", { chapterId: id });
  }, [id]);
  if (!c) return <NotFound />;
  if (!c.available)
    return (
      <main>
        <h1>Coming soon</h1>
        <p>{c.title} is being prepared.</p>
        <Link to="/">Explore available chapters</Link>
      </main>
    );
  return (
    <>
      <PageHeader title="Chapter" />
      <main>
        <div className="chapter-overview">
          <Cover chapter={c} />
          <div>
            <span className="eyebrow cyan chapter-eyebrow">
              {c.subject} · CLASS 11
            </span>
            <h1>{c.title}</h1>
            <p>{c.subtitle}</p>
            <div className="row chapter-meta">
              <span>
                <Layers size={16} /> {c.reels.length} videos
              </span>
              <span>1 min 30 sec</span>
              <span>Animated lessons</span>
            </div>
            <Button
              disabled={busy}
              onClick={() =>
                s.profile?.persona === "Pro"
                  ? nav(`/player/${c.id}/${c.reels[0].id}`)
                  : pro()
              }
            >
              <Play size={17} fill="currentColor" /> Play All
            </Button>
          </div>
        </div>
        <div className="space" />
        {challengeCard(c)}
        <div className="section-title">
          <h2>In this chapter</h2>
          <span className="muted">
            {c.reels.filter((r) => qualified(s, r.id)).length} watched
          </span>
        </div>
        <div className="topics">
          {c.reels.map((r, i) => (
            <button
              key={r.id}
              className="topic"
              onClick={() =>
                s.profile?.persona === "Pro"
                  ? nav(`/player/${c.id}/${r.id}`)
                  : pro()
              }
            >
              <span className="topic-number">
                {qualified(s, r.id) ? (
                  <Check size={18} />
                ) : (
                  String(i + 1).padStart(2, "0")
                )}
              </span>
              <div>
                <h3>{r.title}</h3>
                <span className="muted">
                  00:18 · {qualified(s, r.id) ? "Watched" : "Concept lesson"}
                </span>
              </div>
              {s.profile?.persona === "Pro" ? (
                <Play size={17} />
              ) : (
                <Lock size={17} />
              )}
            </button>
          ))}
        </div>
      </main>
    </>
  );
}
function PageHeader({ title }: { title: string }) {
  const {
    s,
    nav,
    loc,
    run,
    refresh,
    show,
    close,
    pro,
    coming,
    start,
    challengeCard,
    busy,
    catalogue,
    chapters,
    products,
    qualified,
    setCatalogue,
    setProducts,
    setError,
    modal,
  } = React.useContext(AppContext);
  return (
    <header className="page-header">
      <button
        className="icon"
        aria-label="Go back"
        onClick={() => (loc.key === "default" ? nav("/") : nav(-1))}
      >
        <ArrowLeft />
      </button>
      <h2>{title}</h2>
      <Link className="icon" aria-label="Open wallet" to="/wallet">
        <Coins size={20} />
      </Link>
    </header>
  );
}
function Player({ chapterId, reelId }: { chapterId: string; reelId: string }) {
  const {
    s,
    nav,
    loc,
    run,
    refresh,
    show,
    close,
    pro,
    coming,
    start,
    challengeCard,
    busy,
    catalogue,
    chapters,
    products,
    qualified,
    setCatalogue,
    setProducts,
    setError,
    modal,
  } = React.useContext(AppContext);
  const c = chapters.find((c) => c.id === chapterId),
    r = c?.reels.find((r) => r.id === reelId);
  const [time, setTime] = useState(0),
    [playing, setPlaying] = useState(false),
    [speed, setSpeed] = useState(1),
    [sheet, setSheet] = useState(""),
    [notice, setNotice] = useState("");
  const videoRef = useRef<HTMLVideoElement>(null),
    timeRef = useRef(0),
    lastTick = useRef(0),
    seekRef = useRef(false),
    touchY = useRef(0),
    offering = useRef(false),
    started = useRef(false);
  useEffect(() => {
    const position = s.positions?.[reelId] || 0;
    timeRef.current = position;
    setTime(position);
    setPlaying(false);
    setSheet("");
    started.current = false;
    offering.current = false;
    if (r) void service.lastReel(r.id).then(refresh);
  }, [reelId]);
  useEffect(() => {
    const stop = () => setPlaying(false);
    window.addEventListener("reels-service-error", stop);
    return () => window.removeEventListener("reels-service-error", stop);
  }, []);
  useEffect(() => {
    const pause = () => {
      if (document.hidden) setPlaying(false);
    };
    document.addEventListener("visibilitychange", pause);
    return () => document.removeEventListener("visibilitychange", pause);
  }, []);
  useEffect(() => {
    if (sheet || modal) setPlaying(false);
  }, [sheet, modal]);
  useEffect(() => {
    if (!r || !playing || sheet || modal) return;
    lastTick.current = performance.now();
    const interval = setInterval(() => {
      const now = performance.now(),
        wall = (now - lastTick.current) / 1000;
      lastTick.current = now;
      if (document.hidden) return;
      const from = timeRef.current;
      const next = r.source
        ? videoRef.current?.currentTime || from
        : Math.min(r.duration, from + wall * speed);
      if (next > from && !started.current) {
        started.current = true;
        void service.analytics("reels_video_started", {
          reelId: r.id,
          chapterId: c!.id,
        });
      }
      if (
        !seekRef.current &&
        (!r.source ||
          (!!videoRef.current &&
            !videoRef.current.paused &&
            videoRef.current.readyState >= 3))
      )
        void service
          .progress(r.id, from, next, wall, speed, true, true)
          .then(refresh);
      seekRef.current = false;
      timeRef.current = next;
      setTime(next);
      if (next >= r.duration) setPlaying(false);
    }, 200);
    return () => clearInterval(interval);
  }, [playing, r, speed, sheet, modal]);
  useEffect(() => {
    const v = videoRef.current;
    if (!v) return;
    v.muted = s.mute;
    v.playbackRate = speed;
    if (playing && !sheet && !modal)
      void v.play().catch(() => {
        setPlaying(false);
        setNotice("Tap to Play to start this lesson.");
      });
    else v.pause();
    return () => v.pause();
  }, [playing, speed, s.mute, sheet, modal]);
  useEffect(() => {
    if (
      !c ||
      !r ||
      offering.current ||
      s.offered.includes(c.id) ||
      !c.reels.every((x) => qualified(s, x.id))
    )
      return;
    offering.current = true;
    setPlaying(false);
    setSheet("invite");
    void service.invitation(c.id, false).then(refresh);
  }, [s.coverage]);
  if (!c || !r) return <NotFound />;
  if (s.profile?.persona !== "Pro")
    return (
      <main className="empty">
        <Lock />
        <h1>Pro lessons await</h1>
        <p>Unlock the complete Reels learning experience.</p>
        <Button onClick={pro}>Explore Pro</Button>
        <Link to="/">Back to discovery</Link>
      </main>
    );
  const index = c.reels.indexOf(r),
    n = c.reels.filter((x) => qualified(s, x.id)).length;
  const change = (delta: number) => {
    const next = c.reels[index + delta];
    if (next) {
      setPlaying(false);
      nav(`/player/${c.id}/${next.id}`);
    }
  };
  const toggle = () => {
    if (!playing) {
      if (timeRef.current >= r.duration) {
        timeRef.current = 0;
        setTime(0);
        if (videoRef.current) videoRef.current.currentTime = 0;
      }
    }
    setPlaying(!playing);
  };
  return (
    <div
      className="player"
      onTouchStart={(e) => {
        touchY.current = e.touches[0].clientY;
      }}
      onTouchEnd={(e) => {
        if (
          sheet ||
          modal ||
          (e.target as HTMLElement).closest("button,input,a")
        )
          return;
        const dy = touchY.current - e.changedTouches[0].clientY;
        if (Math.abs(dy) > 80) change(dy > 0 ? 1 : -1);
      }}
    >
      <header className="player-header">
        <Link
          className="icon"
          aria-label="Back to chapter"
          to={`/chapter/${c.id}`}
        >
          <ArrowLeft />
        </Link>
        <div>
          <span className="eyebrow">{c.title}</span>
          <small>Lesson {index + 1} of 5</small>
        </div>
        <button
          className="icon"
          aria-label="Playback settings"
          onClick={() => setSheet("settings")}
        >
          <MoreHorizontal />
        </button>
      </header>
      <div className="player-topline">
        <span className="tiny-label">ANIMATED DEMO LESSON · ENGLISH</span>
        <button className="progress-pill" onClick={() => setSheet("progress")}>
          <Coins size={14} />
          {n}/5 watched
        </button>
      </div>
      <div className="lesson-stage">
        {r.source ? (
          <video
            ref={videoRef}
            src={r.source}
            playsInline
            preload="metadata"
            aria-label={r.title}
            onLoadedMetadata={(e) => {
              e.currentTarget.currentTime = timeRef.current;
            }}
            onSeeking={() => {
              seekRef.current = true;
            }}
            onError={() => {
              setPlaying(false);
              setNotice(
                "This lesson could not load. Try replaying or choose another topic.",
              );
            }}
          />
        ) : (
          <>
            <div className="lesson-grid" />
            <span className="lesson-step">
              CONCEPT {String(index + 1).padStart(2, "0")}
            </span>
            <h1>{r.title}</h1>
            <p>{r.concept}</p>
            <div className={`diagram ${r.diagram}`}>
              <div className="axis" />
              {r.diagram === "measure" ? (
                <div
                  className="animated-ruler"
                  style={{
                    transform: `translateX(${Math.sin(time / 3) * 12}px) rotate(-8deg)`,
                  }}
                >
                  <span>0</span>
                  <span>1</span>
                  <span>2</span>
                  <span>3</span>
                  <span>4</span>
                </div>
              ) : (
                <>
                  <div
                    className="motion-block"
                    style={{
                      transform: `translateX(${Math.sin(time / 3) * 65}px)`,
                    }}
                  >
                    m
                  </div>
                  <div className="force-arrow">F ⟶</div>
                </>
              )}
              <div className="diagram-label">
                {time < 6
                  ? "OBSERVE"
                  : time < 12
                    ? "CONNECT THE IDEA"
                    : "REMEMBER"}
              </div>
            </div>
            <div className={`equation ${time > 5 ? "reveal" : ""}`}>
              {time > 5 ? r.equation : "A closer look at the concept…"}
            </div>
            <p className="lesson-caption">{time < 6 ? r.concept : r.caption}</p>
          </>
        )}
      </div>
      <div className="player-actions">
        <button
          className={s.likes.includes(r.id) ? "selected" : ""}
          onClick={() => run(() => service.preference("likes", r.id))}
        >
          <Heart size={21} />
          <span>{s.likes.includes(r.id) ? "Liked" : "Like"}</span>
        </button>
        <button
          className={s.saved.includes(r.id) ? "selected" : ""}
          onClick={() => run(() => service.preference("saved", r.id))}
        >
          <Bookmark size={21} />
          <span>{s.saved.includes(r.id) ? "Saved" : "Save"}</span>
        </button>
        <button
          onClick={async () => {
            try {
              const url = `${window.location.origin}/player/${c.id}/${r.id}`;
              if (navigator.share)
                await navigator.share({ title: r.title, url });
              else {
                await navigator.clipboard.writeText(url);
                setNotice("Lesson link copied.");
              }
            } catch {
              setNotice(
                "Sharing was cancelled or unavailable. You can copy the page URL.",
              );
            }
          }}
        >
          <Share2 size={21} />
          <span>Share</span>
        </button>
        <button onClick={() => setSheet("notes")}>
          <BookOpen size={21} />
          <span>Notes</span>
        </button>
      </div>
      <div className="playback-panel">
        <div className="row">
          <button
            className="icon play-control"
            aria-label={playing ? "Pause" : "Tap to Play"}
            onClick={toggle}
          >
            {playing ? (
              <Pause fill="currentColor" />
            ) : (
              <Play fill="currentColor" />
            )}
          </button>
          <button
            className="icon"
            aria-label="Replay lesson"
            onClick={() => {
              timeRef.current = 0;
              setTime(0);
              if (videoRef.current) videoRef.current.currentTime = 0;
              setPlaying(true);
            }}
          >
            <RotateCcw size={18} />
          </button>
          <span className="time">
            00:{String(Math.floor(time)).padStart(2, "0")} /{" "}
            {Math.floor(r.duration / 60)
              .toString()
              .padStart(2, "0")}
            :{(r.duration % 60).toString().padStart(2, "0")}
          </span>
          <button className="speed" onClick={() => setSheet("settings")}>
            {speed}×
          </button>
          <button
            className="icon"
            aria-label={s.mute ? "Unmute" : "Mute"}
            onClick={() => run(() => service.mute(!s.mute))}
          >
            {s.mute ? <VolumeX size={20} /> : <Volume2 size={20} />}
          </button>
        </div>
        <input
          className="timeline"
          type="range"
          aria-label="Lesson timeline"
          min="0"
          max={r.duration}
          step="0.1"
          value={time}
          onChange={(e) => {
            seekRef.current = true;
            const value = Number(e.target.value);
            timeRef.current = value;
            setTime(value);
            void service.checkpoint(r.id, value);
            if (videoRef.current) videoRef.current.currentTime = value;
          }}
        />
        <div className="row player-title">
          <div>
            <span className="eyebrow cyan">
              {c.subject} · {index + 1}/5
            </span>
            <h3>{r.title}</h3>
          </div>
          <button
            className="chapter-button"
            onClick={() => setSheet("chapter")}
          >
            <Layers size={17} />
            <ChevronDown size={15} />
          </button>
        </div>
        <div className="row player-nav">
          <button disabled={index === 0} onClick={() => change(-1)}>
            <ArrowLeft size={15} /> Previous
          </button>
          <button
            onClick={() =>
              index === 4
                ? setSheet(n === 5 ? "invite" : "progress")
                : change(1)
            }
          >
            {index === 4 ? "Challenge" : "Next lesson"}
            <ArrowRight size={15} />
          </button>
        </div>
        {n === 5 && (
          <button className="resume-quiz" onClick={() => start(c)}>
            Resume Challenge <ArrowRight size={15} />
          </button>
        )}
        {notice && (
          <p role="status" className="notice">
            {notice}
          </p>
        )}
        <p className="fine">
          Animated lesson has readable captions; no audio track.
        </p>
      </div>
      {sheet && (
        <Modal
          title={
            sheet === "invite"
              ? "Ready to test what you learned?"
              : sheet === "chapter"
                ? "In This Chapter"
                : sheet === "notes"
                  ? "Your teaching note"
                  : sheet === "settings"
                    ? "Playback settings"
                    : "Your chapter challenge"
          }
          close={() => {
            if (sheet === "invite")
              void service.invitation(c.id, true).then(refresh);
            setSheet("");
          }}
        >
          {sheet === "invite" ? (
            <>
              <div className="modal-emblem gold">
                <Trophy />
              </div>
              <p>5 questions from the videos you watched.</p>
              <p>
                Get all 5 right to earn{" "}
                <strong className="gold">5 coins = ₹1.</strong>
              </p>
              <Button disabled={busy} onClick={() => start(c)}>
                Start Challenge <ArrowRight size={18} />
              </Button>
              <Button
                secondary
                onClick={() => {
                  void service.invitation(c.id, true).then(refresh);
                  setSheet("");
                }}
              >
                Later
              </Button>
            </>
          ) : sheet === "chapter" ? (
            <>
              <h3>{c.title}</h3>
              {c.reels.map((x, i) => (
                <button
                  className="topic"
                  key={x.id}
                  onClick={() => {
                    setSheet("");
                    nav(`/player/${c.id}/${x.id}`);
                  }}
                >
                  <span>{i + 1}</span>
                  <div>{x.title}</div>
                  {qualified(s, x.id) && <Check size={16} />}
                </button>
              ))}
              <h3 className="space">Another chapter</h3>
              {chapters
                .filter((x) => x.available && x.id !== c.id)
                .map((x) => (
                  <Link key={x.id} className="topic" to={`/chapter/${x.id}`}>
                    {x.title}
                    <ChevronRight />
                  </Link>
                ))}
            </>
          ) : sheet === "notes" ? (
            <>
              <span className="eyebrow cyan">{c.title}</span>
              <h3>{r.title}</h3>
              <div className="note-equation">{r.equation}</div>
              <p>{r.caption}</p>
              <Button
                onClick={() =>
                  run(async () => {
                    downloadNote(r);
                    if (!s.downloads.includes(r.id))
                      await service.preference("downloads", r.id);
                    setNotice("Teaching note downloaded as a text file.");
                  })
                }
              >
                <Download size={18} /> Download teaching note
              </Button>
              <p className="fine">
                Real downloadable text note. Animated video download is
                unavailable.
              </p>
            </>
          ) : sheet === "settings" ? (
            <>
              <h3>Playback speed</h3>
              <div className="speed-options">
                {[0.75, 1, 1.25, 1.5, 2].map((v) => (
                  <button
                    className={v === speed ? "active" : ""}
                    key={v}
                    onClick={() => {
                      setSpeed(v);
                      setSheet("");
                    }}
                  >
                    {v}×
                  </button>
                ))}
              </div>
              <div className="setting-row">
                <span>Quality</span>
                <b>Auto · local animation</b>
              </div>
              <p className="fine">No alternate video sources are bundled.</p>
            </>
          ) : (
            <>
              {challengeCard(c)}
              <p>
                Watch at least 80% of each distinct lesson. Seeking or replaying
                the same section won’t add new progress.
              </p>
            </>
          )}
        </Modal>
      )}
    </div>
  );
}
function Quiz({ id }: { id: string }) {
  const {
    s,
    nav,
    loc,
    run,
    refresh,
    show,
    close,
    pro,
    coming,
    start,
    challengeCard,
    busy,
    catalogue,
    chapters,
    products,
    qualified,
    setCatalogue,
    setProducts,
    setError,
    modal,
  } = React.useContext(AppContext);
  const [a, setA] = useState<Attempt | null>(null),
    [index, setIndex] = useState(0),
    [selected, setSelected] = useState<number | null>(null),
    [pending, setPending] = useState(false);
  useEffect(() => {
    service
      .reviewAttempt(id)
      .then((x) => {
        setA(x);
        setIndex(
          Math.min(
            x.answers.findIndex((v) => !v) < 0
              ? 4
              : x.answers.findIndex((v) => !v),
            4,
          ),
        );
      })
      .catch((e) => setError(e.message));
  }, [id]);
  if (!a) return <Loading />;
  if (a.finished) return <Result attempt={a} />;
  const answer = a.answers[index],
    q = a.questions[index];
  return (
    <>
      <PageHeader title="Chapter challenge" />
      <main className="quiz">
        <div className="row">
          <span className="eyebrow cyan">
            {chapters.find((c) => c.id === a.chapterId)?.title}
          </span>
          <span className="quiz-reward">
            <Coins size={14} /> 5 coins = ₹1
          </span>
        </div>
        <div className="segments quiz-segments">
          {a.questions.map((q, i) => (
            <i key={q.id} className={i <= index ? "done" : ""} />
          ))}
        </div>
        <div className="row">
          <span className="muted">Question {index + 1} of 5</span>
          <span className="tiny-label">NO TIMER · TAKE YOUR TIME</span>
        </div>
        <h1>{q.prompt}</h1>
        <div className="options">
          {q.options.map((o, i) => (
            <button
              key={o}
              disabled={!!answer || pending}
              className={`option ${selected === i ? "chosen" : ""} ${answer && i === answer.correct ? "correct" : ""} ${answer && i === answer.selected && i !== answer.correct ? "wrong" : ""}`}
              onClick={() => setSelected(i)}
            >
              <span className="option-letter">{"ABCD"[i]}</span>
              <span>{o}</span>
              {answer && i === answer.correct && <CheckCircle2 size={19} />}
            </button>
          ))}
        </div>
        {answer && (
          <div
            className={`feedback ${answer.selected === answer.correct ? "success" : "failure"}`}
            role="status"
          >
            <h3>
              {answer.selected === answer.correct
                ? "That’s right!"
                : "A good moment to learn"}
            </h3>
            <p>{answer.explanation}</p>
          </div>
        )}
        <div className="quiz-footer">
          <p className="fine">
            All 5 correct earns 5 coins total. No negative marking.
          </p>
          {!answer ? (
            <Button
              disabled={selected === null || pending}
              onClick={async () => {
                if (pending || selected === null) return;
                setPending(true);
                try {
                  const next = await service.submitAnswer(
                    a.id,
                    index,
                    selected,
                  );
                  setA(next);
                  await refresh();
                } catch (e) {
                  setError((e as Error).message);
                } finally {
                  setPending(false);
                }
              }}
            >
              {pending ? "Submitting…" : "Submit Answer"}
            </Button>
          ) : (
            <Button
              disabled={pending}
              onClick={async () => {
                if (pending) return;
                if (index < 4) {
                  setIndex(index + 1);
                  setSelected(null);
                } else {
                  setPending(true);
                  try {
                    const result = await service.finishAttempt(a.id);
                    setA(result);
                    await refresh();
                  } catch (e) {
                    setError((e as Error).message);
                  } finally {
                    setPending(false);
                  }
                }
              }}
            >
              {index === 4 ? "See my result" : "Next question"}
              <ArrowRight size={18} />
            </Button>
          )}
          <button
            className="text-button center"
            onClick={() => nav(`/chapter/${a.chapterId}`)}
          >
            Save & continue later
          </button>
        </div>
      </main>
    </>
  );
}
function Result({ attempt: a }: { attempt: Attempt }) {
  const {
    s,
    nav,
    loc,
    run,
    refresh,
    show,
    close,
    pro,
    coming,
    start,
    challengeCard,
    busy,
    catalogue,
    chapters,
    products,
    qualified,
    setCatalogue,
    setProducts,
    setError,
    modal,
  } = React.useContext(AppContext);
  const [review, setReview] = useState(false);
  const animated = useRef(false);
  useEffect(() => {
    if (
      a.rewarded &&
      !s.events.some(
        (e) => e.name === "reward_animation_shown" && e.attemptId === a.id,
      )
    ) {
      animated.current = true;
      void service
        .analytics("reward_animation_shown", { attemptId: a.id })
        .then(refresh);
    }
  }, []);
  return (
    <>
      <PageHeader title={review ? "Review answers" : "Challenge completed"} />
      <main className="result">
        {!review ? (
          <>
            <div
              className={`result-orb ${a.rewarded && animated.current ? "celebrate" : ""}`}
            >
              <Trophy size={48} />
            </div>
            <span className="eyebrow gold">ONE CHAPTER. FIVE NEW IDEAS.</span>
            <h1>
              {a.alreadyRewarded
                ? "Practice completed — reward already earned"
                : a.score === 5
                  ? "Perfect score! +5 coins"
                  : "You’re getting there."}
            </h1>
            <div className="score">
              {a.score}
              <span>/ 5</span>
            </div>
            <p>
              {a.rewarded ? (
                <strong className="gold">
                  5 coins = ₹1 · Added to your wallet
                </strong>
              ) : a.alreadyRewarded ? (
                "Keep revising. This challenge’s reward was already earned."
              ) : (
                "0 coins earned. Get all five right on a new attempt to earn 5 coins."
              )}
            </p>
            {a.score < 5 && (
              <div className="review-topics">
                <h3>Let’s revisit</h3>
                {a.answers.map(
                  (x, i) =>
                    x?.selected !== x?.correct && (
                      <Link
                        key={i}
                        to={`/player/${a.chapterId}/${a.questions[i].reelId}`}
                      >
                        {
                          chapters
                            .flatMap((c) => c.reels)
                            .find((r) => r.id === a.questions[i].reelId)?.title
                        }
                        <Play size={14} />
                      </Link>
                    ),
                )}
              </div>
            )}
            <Button onClick={() => setReview(true)}>
              Review Answers <BookOpen size={17} />
            </Button>
            <Button secondary onClick={() => nav("/wallet")}>
              Go to Wallet <Coins size={17} />
            </Button>
            <Button
              secondary
              disabled={busy}
              onClick={() => start(chapters.find((c) => c.id === a.chapterId)!)}
            >
              {a.score === 5 ? "Practice again" : "Retry full challenge"}
            </Button>
            <Link className="text-button center" to="/">
              Continue Learning <ArrowRight size={15} />
            </Link>
          </>
        ) : (
          <>
            {a.questions.map((q, i) => {
              const x = a.answers[i]!;
              return (
                <section className="review-card" key={q.id}>
                  <span className="eyebrow">QUESTION {i + 1}</span>
                  <h3>{q.prompt}</h3>
                  <p>
                    Your answer:{" "}
                    <b className={x.selected === x.correct ? "cyan" : "red"}>
                      {q.options[x.selected]}
                    </b>
                  </p>
                  <p>
                    Correct answer: <b>{q.options[x.correct]}</b>
                  </p>
                  <p>{x.explanation}</p>
                  <Link
                    className="text-button"
                    to={`/player/${a.chapterId}/${q.reelId}`}
                  >
                    <Play size={15} /> Replay this lesson
                  </Link>
                </section>
              );
            })}
            <Button onClick={() => setReview(false)}>Back to result</Button>
          </>
        )}
      </main>
    </>
  );
}
function Wallet() {
  const {
    s,
    nav,
    loc,
    run,
    refresh,
    show,
    close,
    pro,
    coming,
    start,
    challengeCard,
    busy,
    catalogue,
    chapters,
    products,
    qualified,
    setCatalogue,
    setProducts,
    setError,
    modal,
  } = React.useContext(AppContext);
  useEffect(() => {
    void service.analytics("reels_wallet_viewed");
  }, []);
  return (
    <>
      <PageHeader title="Your wallet" />
      <main>
        <div className="wallet-hero">
          <span className="eyebrow gold">LITTLE WINS, REAL VALUE</span>
          <div className="wallet-number">
            <Coins size={34} />
            {balance(s)}
            <span>coins</span>
          </div>
          <p>
            {money(balance(s) * 20)} available towards your next learning
            essential
          </p>
          <Link className="button" to="/store">
            Explore the demo store <ArrowRight size={18} />
          </Link>
        </div>
        <div className="info-card">
          <h3>Knowledge pays forward.</h3>
          <p>
            5 coins = ₹1. Earn 5 coins when you answer all 5 questions correctly
            in a quick quiz drawn from your watched lessons.
          </p>
          <p>
            Use coins for up to <strong>10% of an eligible price</strong>. Your
            lifetime learning rank stays with you when you spend.
          </p>
        </div>
        <div className="section-title">
          <h2>Your activity</h2>
          <span className="muted">{s.ledger.length} entries</span>
        </div>
        {s.ledger.length === 0 ? (
          <div className="empty">
            <Coins />
            <h3>Your first little win awaits</h3>
            <p>Get all five questions right to earn your first five coins.</p>
            <Link className="text-button" to="/reels">
              Start learning <ArrowRight size={15} />
            </Link>
          </div>
        ) : (
          s.ledger
            .slice()
            .reverse()
            .map((l) => (
              <div className="transaction" key={l.id}>
                <span
                  className={`transaction-icon ${l.amount < 0 ? "spent" : ""}`}
                >
                  <Coins size={18} />
                </span>
                <div>
                  <h3>{l.reason}</h3>
                  <small>
                    {new Date(l.at).toLocaleDateString("en-IN")} · ref {l.ref}
                  </small>
                </div>
                <b className={l.amount > 0 ? "gold" : "muted"}>
                  {l.amount > 0 ? "+" : ""}
                  {l.amount}
                </b>
              </div>
            ))
        )}
      </main>
    </>
  );
}
function Store() {
  const {
    s,
    nav,
    loc,
    run,
    refresh,
    show,
    close,
    pro,
    coming,
    start,
    challengeCard,
    busy,
    catalogue,
    chapters,
    products,
    qualified,
    setCatalogue,
    setProducts,
    setError,
    modal,
  } = React.useContext(AppContext);
  return (
    <>
      <PageHeader title="Learning essentials" />
      <main>
        <span className="eyebrow cyan">THE DEMO STORE</span>
        <h1>
          A little boost for
          <br />
          your learning journey.
        </h1>
        <p>Use your coins on something that helps you grow.</p>
        <div className="store-balance">
          <Coins size={18} />
          <b>{balance(s)} coins</b>
          <span>{money(balance(s) * 20)} available</span>
        </div>
        {products.map((p) => (
          <Link className="product-card" to={`/store/${p.id}`} key={p.id}>
            <div className={`product-art ${p.id}`}>
              <span>{p.symbol}</span>
              <small>arivihan</small>
            </div>
            <div className="product-info">
              <span className="eyebrow">{p.kind} · SAMPLE PRICE</span>
              <h2>{p.name}</h2>
              <p>{p.description}</p>
              <div className="row">
                <b>{money(p.price)}</b>
                <span className="text-button">
                  View details <ArrowRight size={16} />
                </span>
              </div>
            </div>
          </Link>
        ))}
        <p className="fine">
          All products and subscriptions are demonstrations. No real payment,
          delivery or entitlement is created.
        </p>
      </main>
    </>
  );
}
function Checkout({ id }: { id: string }) {
  const {
    s,
    nav,
    loc,
    run,
    refresh,
    show,
    close,
    pro,
    coming,
    start,
    challengeCard,
    busy,
    catalogue,
    chapters,
    products,
    qualified,
    setCatalogue,
    setProducts,
    setError,
    modal,
  } = React.useContext(AppContext);
  const p = products.find((p) => p.id === id),
    [coins, setCoins] = useState(0),
    [q, setQ] = useState<Quote | null>(null),
    [order, setOrder] = useState(""),
    [pending, setPending] = useState(false);
  const request = useRef<string>(crypto.randomUUID());
  useEffect(() => {
    if (order) return;
    let active = true;
    service
      .quote(id, coins)
      .then((x) => {
        if (active) {
          setQ(x);
          if (x.requestId) request.current = x.requestId;
          const completed = s.orders.find((o) => o.id === x.requestId);
          if (completed) setOrder(completed.id);
        }
      })
      .catch((e) => setError(e.message));
    return () => {
      active = false;
    };
  }, [id, coins, s.ledger.length, order]);
  if (!p) return <NotFound />;
  return (
    <>
      <PageHeader title={order ? "Order recorded" : "Demo checkout"} />
      <main>
        {order ? (
          <div className="order-confirmation">
            <div className="result-orb">
              <CheckCircle2 size={50} />
            </div>
            <span className="eyebrow cyan">
              DEMO CHECKOUT — NO REAL PAYMENT
            </span>
            <h1>Your demo order is recorded.</h1>
            <p>{p.name}</p>
            <div className="info-card">
              <p>Reference: {order}</p>
              <p>
                {q?.coins} coins used · {money(q?.discount || 0)} discount
              </p>
              <p>Simulated payable: {money(q?.payable || 0)}</p>
            </div>
            <p>
              No purchase, shipment or subscription was created. Coins were
              deducted once from your learning wallet.
            </p>
            <Link className="button" to="/wallet">
              View wallet
            </Link>
            <Link className="text-button center" to="/">
              Continue learning
            </Link>
          </div>
        ) : (
          <>
            <div className={`product-art detail ${p.id}`}>
              <span>{p.symbol}</span>
              <small>arivihan · learning essentials</small>
            </div>
            <span className="eyebrow cyan">{p.kind} · SAMPLE PRICE</span>
            <h1>{p.name}</h1>
            <p>{p.description}</p>
            <h2>{money(p.price)}</h2>
            <div className="redemption">
              <div className="row">
                <div>
                  <h3>Put your coins to work</h3>
                  <p>{balance(s)} coins available</p>
                </div>
                <input
                  type="checkbox"
                  aria-label="Use available coins"
                  checked={coins > 0}
                  disabled={!q?.max || pending}
                  onChange={(e) => setCoins(e.target.checked ? q?.max || 0 : 0)}
                />
              </div>
              <label>
                Coins to use (maximum {q?.max || 0})
                <input
                  type="number"
                  min="0"
                  max={q?.max || 0}
                  value={coins}
                  disabled={pending}
                  onChange={(e) =>
                    setCoins(
                      Math.max(
                        0,
                        Math.min(
                          q?.max || 0,
                          Math.floor(Number(e.target.value) || 0),
                        ),
                      ),
                    )
                  }
                />
              </label>
              <p className="fine">
                Up to 10% of the eligible subtotal. 1 coin = ₹0.20.
                {!balance(s) &&
                  " Earn coins in a quick recall quiz to use this benefit."}
              </p>
            </div>
            {q && (
              <div className="price-breakdown">
                <div>
                  <span>Eligible subtotal</span>
                  <b>{money(q.subtotal)}</b>
                </div>
                <div>
                  <span>Coin discount ({q.coins} coins)</span>
                  <b className="cyan">−{money(q.discount)}</b>
                </div>
                <div className="total">
                  <span>Simulated amount payable</span>
                  <b>{money(q.payable)}</b>
                </div>
                <div>
                  <span>Coins remaining</span>
                  <b>{q.remaining}</b>
                </div>
              </div>
            )}
            <div className="demo-notice">
              <Lock size={15} />
              Demo checkout — no real payment
            </div>
            <Button
              disabled={pending || !q || q.coins !== coins}
              onClick={async () => {
                if (pending || !q) return;
                setPending(true);
                try {
                  const o = await service.confirmOrder(
                    p.id,
                    q.coins,
                    request.current,
                  );
                  setQ(o.quote);
                  setOrder(o.id);
                  await refresh();
                } catch (e) {
                  setError((e as Error).message);
                } finally {
                  setPending(false);
                }
              }}
            >
              {pending ? "Confirming…" : "Confirm simulated order"}
              <ArrowRight size={17} />
            </Button>
            <Button secondary disabled={pending} onClick={() => nav("/store")}>
              Cancel — keep my coins
            </Button>
          </>
        )}
      </main>
    </>
  );
}
function Recognition() {
  const {
    s,
    nav,
    loc,
    run,
    refresh,
    show,
    close,
    pro,
    coming,
    start,
    challengeCard,
    busy,
    catalogue,
    chapters,
    products,
    qualified,
    setCatalogue,
    setProducts,
    setError,
    modal,
  } = React.useContext(AppContext);
  const [rows, setRows] = useState<
    { name: string; earned: number; current: boolean }[]
  >([]);
  useEffect(() => {
    service
      .recognition()
      .then(setRows)
      .catch((e) => setError(e.message));
  }, []);
  return (
    <>
      <PageHeader title="Learning together" />
      <main>
        <div className="recognition-intro">
          <Trophy size={38} />
          <span className="eyebrow gold">
            {liveMode ? "YOUR LEARNING COHORT" : "YOUR DEMO COHORT"}
          </span>
          <h1>
            Celebrate the
            <br />
            curiosity.
          </h1>
          <p>
            Lifetime earned activity recognises participation. Spending coins
            never lowers your rank.
          </p>
        </div>
        <div className="info-card">
          <span className="eyebrow">
            {liveMode
              ? "LEARNING ACTIVITY - PRIVATE STUDENT LABELS"
              : "FICTIONAL STUDENTS - DEMO DATA"}
          </span>
          {rows.map((r, i) => (
            <div key={r.name} className={`leader ${r.current ? "you" : ""}`}>
              <span>{i + 1}</span>
              <div className="avatar">{r.name[0]}</div>
              <b>
                {r.name}
                {r.current ? " (you)" : ""}
              </b>
              <span className="gold">
                {r.earned} <Coins size={14} />
              </span>
            </div>
          ))}
        </div>
        <div className="spotlight big">
          <div className="spot-avatar">M</div>
          <span className="eyebrow gold">
            STUDENT SPOTLIGHT · FICTIONAL PREVIEW
          </span>
          <h2>Meera’s small-step mindset</h2>
          <p>“I come back to the idea, then try again.”</p>
          <p>
            A fictional learner story celebrating revision and participation.
            These are demo figures, not real student outcomes.
          </p>
        </div>
      </main>
    </>
  );
}
function Downloads() {
  const {
    s,
    nav,
    loc,
    run,
    refresh,
    show,
    close,
    pro,
    coming,
    start,
    challengeCard,
    busy,
    catalogue,
    chapters,
    products,
    qualified,
    setCatalogue,
    setProducts,
    setError,
    modal,
  } = React.useContext(AppContext);
  return (
    <>
      <PageHeader title="Your downloads" />
      <main>
        <h1>Keep the idea close.</h1>
        <p>
          Real teaching notes saved to your device. Open or download them again
          here.
        </p>
        {!s.downloads.length && (
          <div className="empty">
            <Download />
            <h3>No notes saved yet</h3>
            <p>Open Notes in a lesson to download its teaching note.</p>
            <Link to="/">Explore lessons</Link>
          </div>
        )}
        {s.downloads.map((id) => {
          const r = chapters.flatMap((c) => c.reels).find((r) => r.id === id);
          if (!r) return null;
          return (
            <div className="download-card" key={id}>
              <BookOpen className="cyan" />
              <h3>{r.title}</h3>
              <p>Teaching note · TXT</p>
              <div className="row">
                <button
                  className="text-button"
                  onClick={() =>
                    show(
                      r.title,
                      <>
                        <div className="note-equation">{r.equation}</div>
                        <p>{r.caption}</p>
                        <Button onClick={() => downloadNote(r)}>
                          <Download size={16} /> Download again
                        </Button>
                      </>,
                    )
                  }
                >
                  Open note
                </button>
                <button
                  className="text-button red"
                  onClick={() => run(() => service.preference("downloads", id))}
                >
                  Remove
                </button>
              </div>
            </div>
          );
        })}
        <p className="fine">
          Removing an item removes this library entry. Manage the downloaded
          file in your device’s Downloads folder. Full offline playback is not
          implemented.
        </p>
      </main>
    </>
  );
}
function Presenter() {
  const {
    s,
    nav,
    loc,
    run,
    refresh,
    show,
    close,
    pro,
    coming,
    start,
    challengeCard,
    busy,
    catalogue,
    chapters,
    products,
    qualified,
    setCatalogue,
    setProducts,
    setError,
    modal,
  } = React.useContext(AppContext);
  const [guide, setGuide] = useState<string[]>([]);
  return (
    <>
      <PageHeader title="Local presenter tools" />
      <main>
        <span className="eyebrow gold">EXPLICIT DEMO TOOLING</span>
        <h1>A story, ready to show.</h1>
        <p>
          Convenience tools, not a security boundary. Presets replace all local
          progress, attempts, orders, ledger and analytics consistently.
        </p>
        <div className="info-card">
          <b>Recommended walkthrough</b>
          <p>
            Presentation-ready → play lesson 5 → score 5/5 → 500 coins → ₹500
            subscription → use 250 coins → 250 remaining.
          </p>
        </div>
        {[
          ["fresh", "Fresh student · zero coins"],
          ["ready", "Presentation-ready · 495 seeded coins"],
          ["failed", "Failed attempt · revision"],
          ["rewarded", "Already rewarded · practice only"],
          ["plus", "Plus persona · Pro lock"],
          ["none", "Non-subscribed · unavailable Reels"],
          ["empty", "Empty catalogue"],
          ["loading", "Loading example"],
          ["error", "Retryable error example"],
        ].map(([key, label]) => (
          <Button
            secondary
            disabled={busy}
            key={key}
            onClick={() =>
              run(async () => {
                await service.preset(key);
                if (key === "loading") {
                  show("Loading example", <Loading />);
                  setTimeout(close, 1800);
                }
                if (key === "error") {
                  try {
                    await service.catalogue();
                  } catch (e) {
                    setError((e as Error).message);
                  }
                }
                setCatalogue(await service.catalogue());
                setProducts(await service.products());
                if (!["loading", "error"].includes(key))
                  nav(
                    key === "ready"
                      ? "/player/units/units-5"
                      : key === "failed"
                        ? "/chapter/units"
                        : "/",
                  );
              })
            }
          >
            {label}
            <ArrowRight size={15} />
          </Button>
        ))}
        <Button
          secondary
          onClick={() =>
            run(async () => {
              setGuide(guide.length ? [] : await service.answerGuide());
            })
          }
        >
          {guide.length ? "Hide" : "Reveal"} presenter answer guide
        </Button>
        {guide.map((g) => (
          <p className="guide" key={g}>
            {g}
          </p>
        ))}
        <Button
          secondary
          onClick={() =>
            downloadFile(
              "arivihan-events.json",
              JSON.stringify(s.events, null, 2),
              "application/json",
            )
          }
        >
          Export local analytics JSON
        </Button>
        <Button
          onClick={() =>
            run(async () => {
              await service.preset("fresh");
              setCatalogue(await service.catalogue());
              setProducts(await service.products());
              nav("/");
            })
          }
        >
          Reset Demo
        </Button>
        <p className="fine">
          Seeded coverage and the 495-coin ledger entry are labelled
          presentation data. Normal watch qualification is unchanged.
        </p>
      </main>
    </>
  );
}
function NotFound() {
  const {
    s,
    nav,
    loc,
    run,
    refresh,
    show,
    close,
    pro,
    coming,
    start,
    challengeCard,
    busy,
    catalogue,
    chapters,
    products,
    qualified,
    setCatalogue,
    setProducts,
    setError,
    modal,
  } = React.useContext(AppContext);
  return (
    <main className="empty">
      <Search />
      <h1>That page isn’t here.</h1>
      <Link className="button" to="/">
        Back to Reels
      </Link>
    </main>
  );
}
function App() {
  const nav = useNavigate(),
    loc = useLocation();
  const [s, setS] = useState(initial),
    [ready, setReady] = useState(false),
    [error, setError] = useState(""),
    [catalogue, setCatalogue] = useState<Chapter[]>([]),
    [productCatalogue, setProducts] = useState<Product[]>([]),
    [modal, setModal] = useState<React.ReactNode>(null),
    [modalTitle, setModalTitle] = useState(""),
    [busy, setBusy] = useState(false);
  const chapters = catalogue;
  const products = productCatalogue;
  const qualified = (state: Snapshot, id: string) =>
    isQualified(state, id, catalogue);
  const pendingRef = useRef(false);
  useEffect(() => {
    const onError = (event: Event) =>
      setError((event as CustomEvent<string>).detail);
    window.addEventListener("reels-service-error", onError);
    const rejection = (event: PromiseRejectionEvent) => {
      if (
        event.reason instanceof Error &&
        event.reason.message.startsWith("Supabase:")
      ) {
        event.preventDefault();
        setError(event.reason.message);
      }
    };
    window.addEventListener("unhandledrejection", rejection);
    return () => {
      window.removeEventListener("reels-service-error", onError);
      window.removeEventListener("unhandledrejection", rejection);
    };
  }, []);
  const close = useCallback(() => setModal(null), []);
  const refresh = useCallback(async () => {
    setS(await service.snapshot());
  }, []);
  const run = async (fn: () => Promise<void>) => {
    if (pendingRef.current) return;
    pendingRef.current = true;
    setBusy(true);
    try {
      await fn();
      await refresh();
    } catch (e) {
      setError((e as Error).message);
    } finally {
      pendingRef.current = false;
      setBusy(false);
    }
  };
  const show = (title: string, body: React.ReactNode) => {
    setModalTitle(title);
    setModal(body);
  };
  useEffect(() => {
    (async () => {
      try {
        const [state, cat, productList] = await Promise.all([
          service.snapshot(),
          service.catalogue(),
          service.products(),
        ]);
        setS(state);
        setCatalogue(cat);
        setProducts(productList);
      } catch (e) {
        setError((e as Error).message);
      } finally {
        setReady(true);
      }
    })();
  }, []);
  useEffect(() => {
    setError("");
    window.scrollTo(0, 0);
  }, [loc.pathname]);
  const presenterEnabled =
    !liveMode &&
    (import.meta.env.VITE_DEMO_TOOLS === "true" ||
      new URLSearchParams(loc.search).get("demo") === "1");
  const pro = () =>
    liveMode
      ? show(
          "Pro access required",
          <p>
            Your access is managed by the service. This simulated checkout does
            not grant real subscriptions.
          </p>,
        )
      : show(
          "Unlock Reels with Pro",
          <>
            <div className="modal-emblem">
              <Lock />
            </div>
            <p>
              Your Plus demo includes a preview. Upgrade this local persona to
              explore all lessons and challenges.
            </p>
            <Button
              onClick={() =>
                run(async () => {
                  await service.session({ ...s.profile!, persona: "Pro" });
                  await refresh();
                  close();
                })
              }
            >
              Try simulated Pro upgrade
            </Button>
            <p className="fine">
              Demo only · No payment or subscription created
            </p>
          </>,
        );
  const coming = (c: Chapter) =>
    show(
      "Something good is on its way",
      <>
        <Cover chapter={c} />
        <h3>{c.title}</h3>
        <p>
          These lessons are coming soon. Explore our two complete Physics
          chapters in the meantime.
        </p>
        <Button
          onClick={() => {
            close();
            nav("/");
          }}
        >
          Explore Physics
        </Button>
      </>,
    );
  const start = (c: Chapter) =>
    run(async () => {
      const a = await service.startQuiz(c.id);
      close();
      nav(`/quiz/${a.id}`);
    });
  const challengeCard = (c: Chapter) => {
    const n = c.reels.filter((r) => qualified(s, r.id)).length;
    return (
      <div className="challenge-card">
        <div className="row">
          <span className="gold">
            <Coins size={18} /> CHAPTER CHALLENGE
          </span>
          <b>{n}/5</b>
        </div>
        <p>
          Watch 5 videos. Answer 5 questions.
          <br />
          <strong>Earn 5 coins = ₹1.</strong>
        </p>
        <div className="segments">
          {c.reels.map((r) => (
            <i key={r.id} className={qualified(s, r.id) ? "done" : ""} />
          ))}
        </div>
        {n === 5 && (
          <Button secondary disabled={busy} onClick={() => start(c)}>
            {s.attempts.some((a) => a.chapterId === c.id && !a.finished)
              ? "Resume Challenge"
              : s.ledger.some((l) => l.ref === c.challengeId)
                ? "Practice again"
                : "Start Challenge"}
            <ArrowRight size={16} />
          </Button>
        )}
      </div>
    );
  };
  let page: React.ReactNode;
  const parts = loc.pathname.split("/").filter(Boolean);
  if (!ready) page = <Loading />;
  else if (parts[0] === "presenter")
    page = presenterEnabled ? (
      <Presenter />
    ) : (
      <main className="empty">
        <Lock />
        <h2>Presenter tools are disabled</h2>
        <p>Enable local demo tooling explicitly with ?demo=1 on this route.</p>
        <Link to="/">Go home</Link>
      </main>
    );
  else if (!s.profile) page = <Onboarding />;
  else if (
    s.profile.persona === "None" &&
    !["wallet", "store", "recognition", "downloads"].includes(parts[0])
  )
    page = (
      <main className="empty">
        <Lock />
        <h1>Reels is a subscriber experience.</h1>
        <p>This demo persona has no active subscription.</p>
        <Link className="button" to="/store">
          Explore subscriptions
        </Link>
      </main>
    );
  else if (!parts.length && !liveMode) page = <HomePage />;
  else if (!parts.length)
    page = (
      <main className="web-home">
        <div className="brand">
          <span className="brandmark">a</span> arivihan
        </div>
        <p>
          {s.profile.batch} · {s.profile.language}
        </p>
        <span className="eyebrow cyan">YOUR DAILY DOSE OF CURIOSITY</span>
        <h1>
          <span className="home-greeting" title={s.profile.name}>Hello, {s.profile.name}.</span>
          Let's make an idea click.
        </h1>
        <Link to="/reels" className="home-reels-card">
          <span className="eyebrow cyan">ARIVIHAN REELS</span>
          <h2>
            Small lessons.
            <br />
            Big lightbulb moments.
          </h2>
          <p>Watch, explore and put your learning to the test.</p>
          <span className="button">
            Start watching <Play size={16} />
          </span>
        </Link>
        <div className="home-summary">
          <Link to="/wallet">
            <Coins size={20} color="#efc86b" /> {balance(s)} coins
            <small>5 coins = ₹1</small>
          </Link>
          <Link to="/store">
            <ShoppingBag size={20} /> Demo store
            <small>Simulated checkout only</small>
          </Link>
        </div>
        <p className="fine">
          {liveMode
            ? "Live Supabase · Persistent guest profile"
            : "Local demo · Device-only storage"}
        </p>
      </main>
    );
  else if (parts[0] === "reels") page = <Discovery s={s} refresh={refresh} />;
  else if (parts[0] === "chapters") page = <HomePage />;
  else if (parts[0] === "chapter") page = <ChapterPage id={parts[1]} />;
  else if (parts[0] === "player")
    page = <Player chapterId={parts[1]} reelId={parts[2]} />;
  else if (parts[0] === "quiz") page = <Quiz id={parts[1]} />;
  else if (parts[0] === "wallet") page = <Wallet />;
  else if (parts[0] === "store")
    page = parts[1] ? <Checkout id={parts[1]} /> : <Store />;
  else if (parts[0] === "recognition") page = <Recognition />;
  else if (parts[0] === "downloads") page = <Downloads />;
  else page = <NotFound />;
  return (
    <AppContext.Provider
      value={{
        s,
        nav,
        loc,
        run,
        refresh,
        show,
        close,
        pro,
        coming,
        start,
        challengeCard,
        busy,
        catalogue,
        chapters,
        products,
        qualified,
        setCatalogue,
        setProducts,
        setError,
        modal,
      }}
    >
      <div className={`app-stage${ready && s.profile && liveMode && !parts.length ? " home-stage" : ""}`}>
        {page}
        {error && (
          <div className="error-toast" role="alert">
            <p>{error}</p>
            <button
              className="text-button"
              onClick={() =>
                run(async () => {
                  setError("");
                  await refresh();
                  setCatalogue(await service.catalogue());
                  setProducts(await service.products());
                })
              }
            >
              Retry
            </button>
            <button
              aria-label="Dismiss error"
              className="icon"
              onClick={() => setError("")}
            >
              <X size={18} />
            </button>
          </div>
        )}
        {modal && (
          <Modal title={modalTitle} close={close}>
            {modal}
          </Modal>
        )}
        {ready &&
          s.profile &&
          !["player", "quiz", "presenter"].includes(parts[0]) && (
            <nav className="bottom-nav" aria-label="Main navigation">
              {s.profile.persona !== "None" && (
                <Link
                  to="/"
                  className={
                    !parts.length || parts[0] === "chapter" ? "active" : ""
                  }
                >
                  <Home size={21} />
                  <span>Home</span>
                </Link>
              )}
              <Link
                to={liveMode ? "/reels" : "/chapters"}
                className={
                  parts[0] === "reels" || parts[0] === "chapters"
                    ? "active"
                    : ""
                }
              >
                <Play size={21} />
                <span>Reels</span>
              </Link>
              <Link
                to="/store"
                className={parts[0] === "store" ? "active" : ""}
              >
                <ShoppingBag size={21} />
                <span>Store</span>
              </Link>
            </nav>
          )}
        <div className="desktop-caption">
          <span className="brandmark">a</span>
          <span>A little learning goes a long way.</span>
          <span>
            ARIVIHAN REELS · {liveMode ? "LIVE SUPABASE" : "LOCAL DEMO"}
          </span>
        </div>
      </div>
    </AppContext.Provider>
  );
}
function Loading() {
  return (
    <div className="loading" role="status">
      <div className="spinner" />
      <p>Getting your next idea ready…</p>
    </div>
  );
}
function downloadFile(name: string, text: string, type: string) {
  const u = URL.createObjectURL(new Blob([text], { type }));
  const a = document.createElement("a");
  a.href = u;
  a.download = name;
  a.click();
  setTimeout(() => URL.revokeObjectURL(u), 1000);
}
function downloadNote(r: Reel) {
  downloadFile(
    `${r.id}-teaching-note.txt`,
    `${r.title}\nArivihan animated demo lesson\n\n${r.equation}\n\n${r.caption}\n\nRevisit: /player/${r.chapterId}/${r.id}`,
    "text/plain",
  );
}
createRoot(document.getElementById("root")!).render(
  <BrowserRouter>
    <App />
  </BrowserRouter>,
);
