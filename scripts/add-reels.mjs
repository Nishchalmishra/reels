// Adds new MP4s from videos/ to the hosted Reels catalogue.
//
//   node scripts/add-reels.mjs scan     -> finds new videos, writes videos/new-reels.draft.json
//   (edit the draft: title, topic, grade, concept, 2+ questions per reel, optional "posterFile")
//   node scripts/add-reels.mjs build    -> validates draft, updates manifest, writes a NEW migration
//   node scripts/add-reels.mjs upload   -> uploads MP4s/posters to Supabase Storage (needs service key)
//
// Then apply the generated migration (SUPABASE_HANDOFF.md / scripts/migrate-supabase.mjs) and redeploy.
// Existing migrations are never modified (the checksum guard rejects that).
import fs from "node:fs";
import path from "node:path";
import crypto from "node:crypto";

const root = path.resolve(import.meta.dirname, "..");
const videosDir = path.join(root, "videos");
const manifestPath = path.join(root, "reference-frames/discovery/manifest.json");
const draftPath = path.join(videosDir, "new-reels.draft.json");
const stagePath = path.join(videosDir, "new-reels.staged.json");
const BUCKET = "arivihan-reels";

// ---------- helpers ----------
const sha256 = (buf) => crypto.createHash("sha256").update(buf).digest("hex");
const readJson = (p, fallback) => (fs.existsSync(p) ? JSON.parse(fs.readFileSync(p, "utf8")) : fallback);
const q = (x) => "'" + String(x).replace(/'/g, "''") + "'";
const slug = (s) => s.toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-|-$/g, "");

function loadEnv() {
  const env = { ...process.env };
  const f = path.join(root, ".env");
  if (fs.existsSync(f))
    for (const line of fs.readFileSync(f, "utf8").split(/\r?\n/)) {
      const m = /^\s*([A-Z0-9_]+)\s*=\s*(.*)\s*$/.exec(line);
      if (m && !(m[1] in env)) env[m[1]] = m[2].replace(/^["']|["']$/g, "");
    }
  return env;
}

// Minimal MP4 box walker (no ffmpeg): duration and frame size. Handles fragmented MP4s
// (header duration is 0; duration is the sum of sample durations in moof/traf boxes).
function probeMp4(buf) {
  const out = { duration: null, width: null, height: null };
  const timescale = {}, trexDefault = {}, total = {};
  let movieTs = 1, movieDur = 0, mehd = 0, curTrack = null;
  const walk = (start, end, parent) => {
    let p = start;
    while (p + 8 <= end) {
      let size = buf.readUInt32BE(p);
      const type = buf.toString("latin1", p + 4, p + 8);
      let hdr = 8;
      if (size === 1) { size = Number(buf.readBigUInt64BE(p + 8)); hdr = 16; }
      else if (size === 0) size = end - p;
      if (size < hdr || p + size > end) break;
      const b = p + hdr, v = buf[b];
      if (["moov", "trak", "mdia", "mvex", "moof"].includes(type)) { if (type === "trak") curTrack = null; walk(b, p + size, type); }
      else if (type === "traf") { walk(b, p + size, type); }
      else if (type === "mvhd") { movieTs = buf.readUInt32BE(b + (v === 1 ? 20 : 12)); movieDur = v === 1 ? Number(buf.readBigUInt64BE(b + 24)) : buf.readUInt32BE(b + 16); }
      else if (type === "mehd") mehd = v === 1 ? Number(buf.readBigUInt64BE(b + 4)) : buf.readUInt32BE(b + 4);
      else if (type === "tkhd") {
        curTrack = buf.readUInt32BE(b + (v === 1 ? 20 : 12));
        const w = buf.readUInt32BE(p + size - 8) / 65536, h = buf.readUInt32BE(p + size - 4) / 65536;
        if (w > 0 && h > 0 && out.width === null) { out.width = Math.round(w); out.height = Math.round(h); }
      } else if (type === "mdhd" && curTrack !== null) timescale[curTrack] = buf.readUInt32BE(b + (v === 1 ? 20 : 12));
      else if (type === "trex") trexDefault[buf.readUInt32BE(b + 4)] = buf.readUInt32BE(b + 12);
      else if (type === "tfhd") {
        const flags = buf.readUIntBE(b + 1, 3), id = buf.readUInt32BE(b + 4);
        let o = b + 8;
        if (flags & 1) o += 8; if (flags & 2) o += 4;
        walk.cur = { id, dflt: flags & 8 ? buf.readUInt32BE(o) : trexDefault[id] || 0 };
      } else if (type === "trun" && walk.cur) {
        const flags = buf.readUIntBE(b + 1, 3), n = buf.readUInt32BE(b + 4);
        let o = b + 8;
        if (flags & 1) o += 4; if (flags & 4) o += 4;
        const per = 4 * [0x100, 0x200, 0x400, 0x800].filter((f) => flags & f).length;
        let sum = 0;
        for (let i = 0; i < n; i++) { sum += flags & 0x100 ? buf.readUInt32BE(o) : walk.cur.dflt; o += per; }
        total[walk.cur.id] = (total[walk.cur.id] || 0) + sum;
      }
      p += size;
    }
  };
  walk(0, buf.length, "root");
  let seconds = movieDur / movieTs;
  if (!seconds && mehd) seconds = mehd / movieTs;
  if (!seconds) for (const id in total) if (timescale[id]) seconds = Math.max(seconds, total[id] / timescale[id]);
  out.duration = seconds ? Math.round(seconds * 1000) / 1000 : null;
  return out;
}

const titleFromFile = (f) =>
  f.replace(/\.mp4$/i, "").split(/ _ | #/)[0].replace(/_/g, " ").trim();

// ---------- scan ----------
function scan() {
  const manifest = readJson(manifestPath, []);
  const known = new Set(manifest.map((m) => m.sha256));
  const draft = readJson(draftPath, { note: "", reels: [] });
  const drafted = new Set(draft.reels.map((r) => r.sha256));
  const seen = new Set();
  let added = 0;
  for (const file of fs.readdirSync(videosDir).filter((f) => /\.mp4$/i.test(f)).sort()) {
    const buf = fs.readFileSync(path.join(videosDir, file));
    const sha = sha256(buf);
    if (known.has(sha) || drafted.has(sha)) continue;
    if (seen.has(sha)) { console.log(`skip duplicate content: ${file}`); continue; }
    seen.add(sha);
    const meta = probeMp4(buf);
    if (!meta.duration) { console.log(`skip (cannot read MP4 header): ${file}`); continue; }
    draft.reels.push({
      include: true,
      file, sha256: sha, duration: meta.duration, width: meta.width, height: meta.height,
      title: titleFromFile(file), topic: "", subject: "Physics", grade: 11,
      concept: "", posterFile: null,
      questions: [
        { prompt: "", options: ["", "", "", ""], correctIndex: 0, explanation: "" },
        { prompt: "", options: ["", "", "", ""], correctIndex: 0, explanation: "" },
      ],
    });
    added++;
  }
  draft.note = "Fill topic, concept and 2+ questions (4 options each) per reel. Set include:false to skip a reel. posterFile = optional JPG path (relative to videos/).";
  fs.writeFileSync(draftPath, JSON.stringify(draft, null, 2), "utf8");
  console.log(`${added} new video(s) added to ${path.relative(root, draftPath)} (${draft.reels.length} total in draft).`);
  console.log("Edit the draft, then run: node scripts/add-reels.mjs build");
}

// ---------- build ----------
function build() {
  const draft = readJson(draftPath, null);
  if (!draft) throw Error("No draft. Run scan first.");
  const manifest = readJson(manifestPath, []);
  const sql = [], staged = [], problems = [];
  for (const r of draft.reels.filter((x) => x.include !== false)) {
    const bad = [];
    if (!r.title?.trim()) bad.push("title");
    if (!r.topic?.trim()) bad.push("topic");
    if (!r.concept?.trim()) bad.push("concept");
    if (![11, 12].includes(Number(r.grade))) bad.push("grade (11 or 12)");
    const qs = r.questions || [];
    if (qs.length < 2) bad.push("at least 2 questions");
    qs.forEach((x, i) => {
      if (!x.prompt?.trim() || !x.explanation?.trim() || !Array.isArray(x.options) || x.options.length !== 4 || x.options.some((o) => !o.trim()) || !(x.correctIndex >= 0 && x.correctIndex <= 3))
        bad.push(`question ${i + 1} incomplete`);
    });
    if (!fs.existsSync(path.join(videosDir, r.file))) bad.push("file missing");
    if (bad.length) { problems.push(`${r.file}: ${bad.join(", ")}`); continue; }

    const id = "lesson-" + r.sha256.slice(0, 16);
    const object = `lessons/${id}/v1/${r.sha256}.mp4`;
    const posterObject = r.posterFile ? `lessons/${id}/v1/poster-${sha256(fs.readFileSync(path.join(videosDir, r.posterFile)))}.jpg` : null;
    const base = `https://${"__PROJECT__"}.supabase.co/storage/v1/object/public/${BUCKET}/`;
    const chapterId = "discovery-" + slug(r.topic);
    const content = {
      id, file: r.file, duration: r.duration, width: r.width, height: r.height, sha256: r.sha256, contentVersion: 1,
      reviewStatus: "added via add-reels.mjs; editorial review pending",
      title: r.title.trim(), topic: r.topic.trim(), chapterId, subject: r.subject || "Physics",
      grades: [Number(r.grade)], languages: ["Hindi", "English"],
      narration: "Hindi / Hinglish; English scientific labels", concept: r.concept.trim(), discovery: true,
      source: base + object, ...(posterObject ? { poster: base + posterObject } : {}),
      storageBucket: BUCKET, storageObject: object,
    };
    staged.push({ id, file: r.file, object, contentType: "video/mp4", path: r.file });
    if (posterObject) staged.push({ id, file: r.posterFile, object: posterObject, contentType: "image/jpeg", path: r.posterFile });
    manifest.push(content);
    sql.push(`insert into public.ar_reels_discovery_media(id,chapter_id,duration,content_type,content) values(${q(id)},${q(chapterId)},${r.duration},'educational',${q(JSON.stringify(content))}::jsonb) on conflict(id) do nothing;`);
    qs.forEach((x, i) => {
      const qid = `${id}-q${i + 1}`;
      sql.push(`insert into public.ar_reels_discovery_questions(id,reel_id,prompt,options) values(${q(qid)},${q(id)},${q(x.prompt)},${q(JSON.stringify(x.options))}::jsonb) on conflict(id) do nothing;`);
      sql.push(`insert into ar_reels_private.discovery_answer_keys(question_id,correct_index,explanation) values(${q(qid)},${x.correctIndex},${q(x.explanation)}) on conflict(question_id) do nothing;`);
    });
  }
  if (problems.length) {
    console.log("Not ready (fix in the draft, then rerun build):\n  - " + problems.join("\n  - "));
  }
  if (!staged.some((s) => s.contentType === "video/mp4")) { console.log("Nothing to build."); return; }

  const env = loadEnv();
  const url = env.VITE_SUPABASE_URL || env.NEXT_PUBLIC_SUPABASE_URL;
  if (!url) throw Error("VITE_SUPABASE_URL missing in .env (needed to write hosted URLs).");
  const host = new URL(url).hostname.split(".")[0];
  const fixed = sql.map((s) => s.replaceAll("__PROJECT__", host));
  const videoCount = staged.filter((s) => s.contentType === "video/mp4").length;
  const stamp = new Date().toISOString().replace(/\D/g, "").slice(0, 12); // yyyymmddhhmm
  const migPath = path.join(root, `supabase/migrations/${stamp}_new_reels.sql`);
  const guard = `do $$ begin if (select count(*) from public.ar_reels_discovery_media where id in (${staged.filter((s) => s.contentType === "video/mp4").map((s) => q(s.id)).join(",")}))<>${videoCount} then raise exception 'New reels verification failed'; end if; end $$;`;
  fs.writeFileSync(migPath, `-- New reels added via scripts/add-reels.mjs. Upload the files to Storage BEFORE applying.\n${fixed.join("\n")}\n${guard}\n`, "utf8");
  fs.writeFileSync(manifestPath, JSON.stringify(manifest, null, 2), "utf8");
  fs.writeFileSync(stagePath, JSON.stringify(staged, null, 2), "utf8");
  // Mark built reels so they are not re-added next time.
  const builtIds = new Set(staged.map((s) => s.id));
  draft.reels = draft.reels.filter((r) => !builtIds.has("lesson-" + r.sha256.slice(0, 16)));
  fs.writeFileSync(draftPath, JSON.stringify(draft, null, 2), "utf8");
  console.log(`Built ${videoCount} reel(s).\n  migration: ${path.relative(root, migPath)}\n  manifest updated; upload list: ${path.relative(root, stagePath)}`);
  console.log("Next: node scripts/add-reels.mjs upload  ->  apply the migration  ->  redeploy.");
}

// ---------- upload ----------
async function upload() {
  const env = loadEnv();
  const url = env.VITE_SUPABASE_URL || env.NEXT_PUBLIC_SUPABASE_URL;
  const key = env.SUPABASE_SERVICE_ROLE_KEY;
  if (!url || !key) throw Error("Set SUPABASE_SERVICE_ROLE_KEY (server-only, in .env or the shell) and VITE_SUPABASE_URL.");
  const staged = readJson(stagePath, null);
  if (!staged?.length) throw Error("Nothing staged. Run build first.");
  for (const s of staged) {
    const body = fs.readFileSync(path.join(videosDir, s.path));
    const res = await fetch(`${url}/storage/v1/object/${BUCKET}/${s.object}`, {
      method: "POST",
      headers: { apikey: key, Authorization: `Bearer ${key}`, "Content-Type": s.contentType, "Cache-Control": "max-age=31536000", "x-upsert": "false" },
      body,
    });
    const text = await res.text();
    if (res.ok) console.log(`uploaded  ${s.object}`);
    else if (res.status === 409 || /already exists|Duplicate/i.test(text)) console.log(`exists    ${s.object}`);
    else throw Error(`Upload failed for ${s.object}: ${res.status} ${text}`);
    // Verify the public URL is reachable.
    const head = await fetch(`${url}/storage/v1/object/public/${BUCKET}/${s.object}`, { method: "HEAD" });
    if (!head.ok) throw Error(`Public URL check failed (${head.status}) for ${s.object}. Is the bucket public?`);
  }
  console.log("All staged files are uploaded and publicly reachable. Now apply the new migration.");
}

const cmd = process.argv[2];
({ scan, build, upload })[cmd]
  ? Promise.resolve(({ scan, build, upload })[cmd]()).catch((e) => { console.error(e.message); process.exit(1); })
  : console.log("Usage: node scripts/add-reels.mjs scan | build | upload");
