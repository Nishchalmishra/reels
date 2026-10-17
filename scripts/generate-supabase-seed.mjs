import fs from "node:fs";
import ts from "typescript";
const load = async (source) =>
  import(
    "data:text/javascript;base64," +
      Buffer.from(
        ts.transpileModule(source, {
          compilerOptions: {
            module: ts.ModuleKind.ESNext,
            target: ts.ScriptTarget.ES2022,
          },
        }).outputText,
      ).toString("base64")
  );
const { chapters, challenges, products } = await load(
  fs.readFileSync("src/content.ts", "utf8"),
);
const raw = fs.readFileSync("src/services/local.ts", "utf8");
const begin = raw.indexOf("const keys ="),
  end = raw.indexOf("] as [string", begin);
if (begin < 0 || end < 0)
  throw Error("Mock answer source shape changed: review seed generator.");
const { keys } = await load("export " + raw.slice(begin, end + 1) + ";");
const q = (value) => "'" + String(value).replaceAll("'", "''") + "'";
const j = (value) => q(JSON.stringify(value)) + "::jsonb";
const lines = [
  "-- Explicit, idempotent bundled-demo content seed. No users, progress, balances or fictional activity.",
];
for (const [position, c] of chapters.entries()) {
  const { reels, ...meta } = c;
  lines.push(
    `insert into public.ar_reels_chapters(id,position,content) values(${q(c.id)},${position},${j(meta)}) on conflict(id) do nothing;`,
  );
  for (const r of reels)
    lines.push(
      `insert into public.ar_reels_media(id,chapter_id,duration,content_type,content) values(${q(r.id)},${q(c.id)},${r.duration},'educational',${j(r)}) on conflict(id) do nothing;`,
    );
}
let index = 0;
for (const c of challenges) {
  for (const pair of c.pairs) {
    const key = keys[index++];
    lines.push(
      `insert into public.ar_reels_questions(id,reel_id,prompt,options) values(${q(pair.questionId)},${q(pair.reelId)},${q(key[0])},${j(key[1])}) on conflict(id) do nothing;`,
    );
    lines.push(
      `insert into ar_reels_private.answer_keys(question_id,correct_index,explanation) values(${q(pair.questionId)},${key[2]},${q(key[3])}) on conflict(question_id) do nothing;`,
    );
  }
  lines.push(
    `insert into public.ar_reels_challenges(id,chapter_id,content_version) values(${q(c.id)},${q(c.chapterId)},${c.version}) on conflict(id) do nothing;`,
  );
  c.pairs.forEach((pair, i) =>
    lines.push(
      `insert into public.ar_reels_challenge_items(challenge_id,position,reel_id,question_id) values(${q(c.id)},${i},${q(pair.reelId)},${q(pair.questionId)}) on conflict do nothing;`,
    ),
  );
}
for (const [position, p] of products.entries())
  lines.push(
    `insert into public.ar_reels_products(id,position,price_paise,content) values(${q(p.id)},${position},${p.price},${j(p)}) on conflict(id) do nothing;`,
  );
fs.mkdirSync("supabase/migrations", { recursive: true });
fs.writeFileSync(
  "supabase/migrations/202610170002_demo_content.sql",
  lines.join("\n") + "\n",
);
console.log(
  "Generated idempotent content seed:",
  chapters.length,
  "chapters,",
  index,
  "question mappings,",
  products.length,
  "sample products.",
);
