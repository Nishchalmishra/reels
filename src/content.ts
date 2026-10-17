export type Reel = {
  id: string;
  chapterId: string;
  title: string;
  concept: string;
  equation: string;
  caption: string;
  duration: number;
  diagram: "measure" | "motion";
  contentType: "educational";
  source?: string;
};
export type Chapter = {
  id: string;
  title: string;
  subject: string;
  subtitle: string;
  symbol: string;
  color: string;
  available: boolean;
  reels: Reel[];
  challengeId: string;
  version: number;
};
export type Product = {
  id: string;
  name: string;
  kind: string;
  price: number;
  description: string;
  symbol: string;
};
const units = [
  [
    "The language of measurement",
    "Every measurement needs a number and a unit.",
    "Length → metre (m)",
    "The SI base unit of length is the metre. A distance of 3 metres means three standard metre lengths.",
  ],
  [
    "Small scale, big precision",
    "Convert centimetres into metres.",
    "250 cm ÷ 100 = 2.5 m",
    "One metre contains 100 centimetres. Divide a value in centimetres by 100 to express it in metres.",
  ],
  [
    "Speed has dimensions",
    "Speed tells us distance travelled per unit time.",
    "[speed] = L T⁻¹",
    "Speed = distance / time. Its SI unit is metres per second and its dimensional formula is L T⁻¹.",
  ],
  [
    "Read between the marks",
    "The least count is the smallest readable division.",
    "1 cm ÷ 10 = 1 mm",
    "A ruler with ten equal divisions per centimetre has a least count of 1 millimetre.",
  ],
  [
    "How certain is your answer?",
    "Percentage error compares uncertainty with the measured value.",
    "(0.1 ÷ 10) × 100 = 1%",
    "For a length of 10 cm with uncertainty 0.1 cm, percentage error is 1%.",
  ],
];
const motion = [
  [
    "When nothing pushes",
    "An object keeps its velocity when net force is zero.",
    "ΣF = 0 → constant velocity",
    "Newton’s first law: zero net force means no acceleration. A moving object continues at constant velocity.",
  ],
  [
    "Force changes motion",
    "Acceleration grows with force and decreases with mass.",
    "F = ma = 2 × 3 = 6 N",
    "A 2 kg object accelerating at 3 m/s² needs a net force of 6 newtons.",
  ],
  [
    "Every action has a partner",
    "Interaction forces act on different objects.",
    "F₍A on B₎ = −F₍B on A₎",
    "Newton’s third law pairs have equal magnitude and opposite directions, acting on different objects.",
  ],
  [
    "The measure of motion",
    "Momentum depends on mass and velocity.",
    "p = mv = 2 × 4 = 8 kg·m/s",
    "A 2 kg object moving at 4 m/s has momentum 8 kg·m/s in the direction of its velocity.",
  ],
  [
    "A push over time",
    "Impulse equals the change in momentum.",
    "J = FΔt = 3 × 2 = 6 N·s",
    "A constant net force of 3 N acting for 2 seconds gives an impulse of 6 N·s.",
  ],
];
export const chapters: Chapter[] = [
  {
    id: "units",
    title: "Units & Measurements",
    subject: "Physics",
    subtitle: "The foundation of every discovery",
    symbol: "↗",
    color: "mint",
    available: true,
    reels: [],
    challengeId: "units-five-v1",
    version: 1,
  },
  {
    id: "motion",
    title: "Laws of Motion",
    subject: "Physics",
    subtitle: "Understand what moves the world",
    symbol: "→",
    color: "blue",
    available: true,
    reels: [],
    challengeId: "motion-five-v1",
    version: 1,
  },
  {
    id: "atoms",
    title: "Structure of Atom",
    subject: "Chemistry",
    subtitle: "A whole world within",
    symbol: "◎",
    color: "peach",
    available: false,
    reels: [],
    challengeId: "",
    version: 1,
  },
  {
    id: "cells",
    title: "Cell: The Unit of Life",
    subject: "Biology",
    subtitle: "Life starts small",
    symbol: "◉",
    color: "green",
    available: false,
    reels: [],
    challengeId: "",
    version: 1,
  },
];
[units, motion].forEach((rows, i) => {
  chapters[i].reels = rows.map((r, j) => ({
    id: `${chapters[i].id}-${j + 1}`,
    chapterId: chapters[i].id,
    title: r[0],
    concept: r[1],
    equation: r[2],
    caption: r[3],
    duration: 18,
    diagram: i === 0 ? "measure" : "motion",
    contentType: "educational",
  }));
});
export const products = [
  {
    id: "pro",
    name: "Arivihan Pro · 1 month",
    kind: "Subscription",
    price: 50000,
    description:
      "A sample month of guided learning, chapter reels and revision.",
    symbol: "✦",
  },
  {
    id: "kit",
    name: "The Revision Kit",
    kind: "Study essentials",
    price: 200000,
    description: "Sample set of revision cards, notebooks and practice tools.",
    symbol: "▤",
  },
  {
    id: "notes",
    name: "Pocket Formula Book",
    kind: "Study essentials",
    price: 9900,
    description: "A sample compact companion for your physics formulas.",
    symbol: "ƒ",
  },
];
export const featureConfig = { promo: true, spotlight: true };
export type Challenge = {
  id: string;
  chapterId: string;
  version: number;
  pairs: { reelId: string; questionId: string }[];
};
export const challenges: Challenge[] = [
  {
    id: "units-five-v1",
    chapterId: "units",
    version: 1,
    pairs: [
      { reelId: "units-1", questionId: "q-units-1" },
      { reelId: "units-2", questionId: "q-units-2" },
      { reelId: "units-3", questionId: "q-units-3" },
      { reelId: "units-4", questionId: "q-units-4" },
      { reelId: "units-5", questionId: "q-units-5" },
    ],
  },
  {
    id: "motion-five-v1",
    chapterId: "motion",
    version: 1,
    pairs: [
      { reelId: "motion-1", questionId: "q-motion-1" },
      { reelId: "motion-2", questionId: "q-motion-2" },
      { reelId: "motion-3", questionId: "q-motion-3" },
      { reelId: "motion-4", questionId: "q-motion-4" },
      { reelId: "motion-5", questionId: "q-motion-5" },
    ],
  },
];
export type FeedItem =
  | { id: string; type: "educational"; reelId: string; chapterId: string }
  | {
      id: string;
      type: "featured";
      feature: "student-spotlight";
      qualifies: false;
    }
  | { id: string; type: "promo"; feature: "guided-learning"; qualifies: false };
export const feedItems: FeedItem[] = [
  ...chapters.flatMap((c) =>
    c.reels.map((r) => ({
      id: `feed-${r.id}`,
      type: "educational" as const,
      reelId: r.id,
      chapterId: c.id,
    })),
  ),
  {
    id: "spotlight-meera",
    type: "featured",
    feature: "student-spotlight",
    qualifies: false,
  },
  {
    id: "promo-guided",
    type: "promo",
    feature: "guided-learning",
    qualifies: false,
  },
];
