# Replacing educational content

All catalogue data and diagrams are local. `src/content.ts` contains typed `Chapter` and `Reel` records, stable reel/chapter IDs, duration, title, concept, equation, caption, and optional `source`. No lesson is cut from the Figma reference or unrelated stock footage.

| Chapter              | Reel IDs                  | Challenge/version  | Question IDs                  |
| -------------------- | ------------------------- | ------------------ | ----------------------------- |
| Units & Measurements | units-1 through units-5   | units-five-v1 / 1  | q-units-1 through q-units-5   |
| Laws of Motion       | motion-1 through motion-5 | motion-five-v1 / 1 | q-motion-1 through q-motion-5 |

Each chapter explicitly maps its five ordered reel IDs to the corresponding five question IDs. The local adapter assembles the mapping from these stable IDs. There is exactly one question per distinct educational reel. Future multi-group chapters must define separate, non-overlapping groups of five and unique versioned challenge IDs; do not combine promotional items or partial groups.

For MP4 replacement, place authorised files in `public/lessons/<chapter>/<reel>.mp4`, add `source: '/lessons/<chapter>/<reel>.mp4'`, and set the duration to the real media length. The existing player chooses the native MP4 adapter when a source exists. Verify captions, duration and watch coverage at all playback speeds. Add an accessible caption track when teaching depends on speech. Keep assets bundled for presentation reliability. Do not invent quality levels; only Auto is currently offered.

Question prompts, four options, private correct indexes and concise explanations live in the mock service's `keys` table in `src/services/local.ts`. The public question DTO contains only ID, reel ID, prompt and options. Review the teaching caption/equation against each new prompt and answer. Keys are hidden from component imports but are inspectable in the bundled demo; they must move to private server storage in production.

Units lessons cover metre, 250 cm = 2.5 m, speed dimensions L T⁻¹, 1 mm ruler least count and 1% relative uncertainty. Motion lessons cover zero-force constant velocity, F = 6 N, third-law pairs on different objects, p = 8 kg·m/s and J = 6 N·s. These match all ten seeded MCQs.

Downloadable notes are generated as real UTF-8 text from each reel's teaching caption and equation. To replace with PDFs, bundle authorised accessible documents under `public/notes/`, update note download/open handling and verify real file retrieval. No fake video download is presented.

Change the content version/challenge ID when questions or substantive teaching content changes. Decide reward migration policy with the backend team rather than reusing an old rewarded content reference accidentally. Chemistry and Biology entries are intentionally Coming Soon. Config in `featureConfig` controls the labelled featured spotlight and promotion preview; neither participates in a challenge.

## Live catalogue and private keys

Live mode reads chapters, media, public question text/options and sample product prices from Supabase. The existing typed frontend content remains the local demo source and the explicit seed source. `scripts/generate-supabase-seed.mjs` produces `202610170002_demo_content.sql` from those teaching records and the mock key table; this is a backend SQL seed, not a public asset or browser API. Live grading keys belong only in `ar_reels_private.answer_keys`. The live build excludes LocalDemoAdapter and its key table; live Vite denies the backend seed and mock-key source paths.

After any migration has been applied, add a new versioned content migration instead of regenerating/changing an applied file: the checksum guard deliberately rejects modified migrations. New teaching versions need new stable challenge IDs and reviewed reel/question mappings. Upload only authorized MP4s/notes and update the server catalogue through controlled migrations/admin tools. Keep actual duration, captions, availability and question teaching consistent. Do not put grading keys in public question JSON or upload backend seeds to public storage.

The deployed seed preserves the existing two chapters, ten teaching lessons/questions and sample prices. It inserts no students, balances or activity. It has been applied to the hosted project through official MCP, verified against actual database records, and idempotency-tested in embedded PostgreSQL.
