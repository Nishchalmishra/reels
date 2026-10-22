# Arivihan Reels - web demo

The active demo is now the responsive **React/TypeScript web application**. No APK or Android emulator is required. Existing web onboarding creates one persistent Supabase guest identity, then Home to Reels opens the playing **Feed**. **Explore** searches the supplied lesson videos; mapped quizzes connect to the existing wallet and simulated store checkout. The native source is preserved in `native-app/` as a separate, paused implementation.

Start with `npm run live`, then open **http://127.0.0.1:5173**. Your workspace `.env` already selects the live project. See [DISCOVERY_HANDOFF.md](DISCOVERY_HANDOFF.md) for architecture and verification details. The separate `npm run demo` mode retains the older local chapter demo; it does not pretend to provide live discovery success.

The revised discovery backend is deployed on project `pbaegeqjargtsfortsyh`. The earlier [SUPABASE_VERIFICATION.md](SUPABASE_VERIFICATION.md) describes the original chapter-based web journey. Current web discovery evidence is documented separately in the handoff and `verification-live/web/`.

The mobile web journey is verified: real video qualification, deferred quiz restoration, persisted answers, 4/5 zero, perfect retry five coins, duplicate reward protection, wallet, cancelled quote and simulated redemption. Build and 31 tests pass. Open the running production preview at **http://127.0.0.1:4173**. Verification details and remaining limits are in the handoff.

## Run

Use Node 22 or newer, install dependencies with `npm install`.

```sh
npm run demo
npm run live
```

`demo` explicitly selects the persistent LocalDemoAdapter. `live` explicitly selects Supabase and never falls back to mocks. `npm run dev` uses `VITE_SERVICE_MODE` from `.env`, defaulting to local if absent. The workspace `.env` now selects `supabase`; existing `NEXT_PUBLIC_SUPABASE_URL` and `NEXT_PUBLIC_SUPABASE_PUBLISHABLE_KEY` names are accepted as browser-safe aliases. `.env.example` contains placeholders only. Restart Vite after changing configuration.

Browser-safe live settings:

```dotenv
VITE_SERVICE_MODE=supabase
VITE_SUPABASE_URL=https://YOUR_PROJECT.supabase.co
VITE_SUPABASE_PUBLISHABLE_KEY=sb_publishable_YOUR_KEY
VITE_DEMO_TOOLS=false
VITE_INTEGRATION_TEST=false
```

Only the URL/publishable key belong in public configuration. Privileged keys, database URLs and management tokens must never have a `VITE_` prefix. Vite rejects unsafe key configuration and blocks backend seeds/tooling/mock-key source in live dev mode. The build outputs frontend assets to `dist-live/`. Videos and posters are now hosted in the existing Supabase project, in bucket `arivihan-reels`. The adapter uses the catalogue HTTPS URLs directly, so the frontend works on Vercel without local files or a media server. `VITE_MEDIA_MODE=hosted` is the default; `local` is an explicit development-only override and is rejected during deployment builds. No AWS or Cloudinary account is used.

## Database deployment and verification

The official project-scoped Supabase MCP server is configured, OAuth authenticated, and verified by actual `get_project_url`, database inspection, migration and advisor tool calls. See [SUPABASE_MCP_SETUP.md](SUPABASE_MCP_SETUP.md). Anonymous Auth was enabled by the project owner and verified through real sessions.

```sh
npm run mcp:verify
npm run mcp:inspect
npm run db:migrate:mcp
npm run test:live
npm run test:live:browser
npm run test:live:onboarding
npm run test:live:restore
```

Three versioned migrations are deployed: secured application schema/RPCs, explicit bundled lesson seed, and public catalogue/legacy leaderboard security hardening. The checksummed transaction skips already-applied migrations and refuses changed applied files. Historical tables, policies and data were preserved. No database password or management token is needed for the authenticated MCP route. Optional direct database/management runners remain available as `db:inspect` and `db:migrate`; privileged configuration belongs only in the ignored server-side environment.

The hosted API verifier creates marked TEST A/B identities, reconciles actual records, checks owner isolation and concurrent completion/spending, and adds a labelled 495-coin fixture only to its dedicated test identity. The browser verifier creates a separate marked test identity and exercises actual timed playback, failed/perfect quizzes, wallet, cancellation and simulated checkout. Marked test identities from the verified runs are retained and documented; no normal student balances/activity were seeded.

## Builds and tests

```sh
npm run typecheck
npm run test
npm run build:demo
npm run build:live
npm run preview
```

`build:demo` writes `dist/`; `build:live` writes `dist-live/` and checks that mock grading material and privileged configuration values are absent. `npm run build` builds the configured mode into `dist/`. To preview the live build:

```sh
node node_modules/vite/bin/vite.js preview --host 127.0.0.1 --port 4174 --outDir dist-live
```

`npm run verify:browser` tests the local demo preview on port 4173. `node scripts/browser-revision.mjs` verifies the unseeded local watch/failure/retry/practice journey. `node scripts/browser-live.mjs` is the historical missing-backend/Auth-error diagnostic; do not run it against the now healthy project. `node scripts/browser-live-journey.mjs` uses a dedicated real guest identity and real database checks against the deployed backend. Set `REELS_BASE_URL` to change that script's preview origin. Scripts use installed Chrome through Playwright. No lint script is configured.

## Demo walkthrough

Choose the demo student, Physics, Units & Measurements, then Play All. Play at least 80% of each distinct lesson. Next/swipe advances lessons; real speed controls change timeline advancement. At 5/5 watched, start the challenge or choose Later and resume. Submit five answers with teaching feedback. A perfect result earns five coins total; 4/5 earns zero; a later full successful retry can earn the one available reward. Review answers, open the wallet, browse the sample store, preview redemption, cancel or confirm a simulated order.

Quick local presentation: `/presenter?demo=1` -> Presentation-ready -> fifth lesson -> perfect score -> 495 seeded coins become 500 -> redeem 250 on the INR 500 sample -> 250 remain. Seeds/presets are explicitly local, reset consistently and never refresh-mint coins. Live mode refuses all presenter presets, answer guides and persona upgrades. Simulated checkout never creates real payments, shipments or subscriptions in either mode.

Reels support ten meaningful animated demo lessons and real text-note downloads. No real educational MP4s were supplied. The Figma walkthrough was inspected only as a visual reference. Lessons are labelled animated demos, have readable teaching text and no audio track; optional MP4 sources keep the same progress interface. Full offline playback is not claimed. English teaching content is supplied; Hindi is upcoming.

Routes remain `/`, `/chapter/:id`, `/player/:chapter/:reel`, `/quiz/:attempt`, `/wallet`, `/store`, `/store/:product`, `/downloads`, `/recognition`, `/presenter?demo=1`. Static hosts must rewrite application routes to `index.html`.

## Security and limits

Guest sessions are genuine Supabase anonymous Auth sessions; ownership comes from `auth.uid()`. They persist in this browser through the SDK. Clearing that session loses access to an anonymous identity unless it is later linked to a recoverable account. Local demo IDs confer no live access.

Live answers/grades, stored eligibility, ledger rewards, prices and orders use narrow authenticated RPCs. Keys live in a private database schema; frontend questions contain no unsubmitted answers. Rewards and debits are transactional and idempotent; spending does not lower learning recognition. Browser watch reports cannot prove attention or defeat determined abuse: the server validates/report-merges coverage but production anti-abuse needs more than client timing.

See [SUPABASE_HANDOFF.md](SUPABASE_HANDOFF.md), [SUPABASE_VERIFICATION.md](SUPABASE_VERIFICATION.md), [CONTENT_GUIDE.md](CONTENT_GUIDE.md), and the historical [local frontend verification](VERIFICATION.md).

## Hosted media and Vercel

All ten unique source videos and ten posters are uploaded and verified. The duplicate Rock painting file shares its existing lesson asset. Bucket `arivihan-reels` allows public playback; anyone with a file URL can view it. It does not permit student uploads, updates or deletes. These demo assets are not a protected paid-video delivery system.

`vercel.json` builds with `npm run build:live`, serves `dist-live/`, and supports React Router deep links. Set the existing browser-safe Supabase URL/key in Vercel environment settings and keep `VITE_SERVICE_MODE=supabase`. `VITE_MEDIA_MODE=hosted` is optional because it is the default. Push/redeploy this updated frontend to your existing Vercel project: uploading the videos does not replace an already deployed JavaScript bundle. No Vercel credentials or deployment connection were available in this workspace, so an actual Vercel redeploy was not performed.

The media migration `202610220008_hosted_media.sql` changes delivery metadata only. Lesson IDs, content versions, mappings, progress and reward uniqueness remain intact. Full-file SHA-256 and HTTP 206 byte-range checks passed for every object. See `supabase/inspection/media-upload.json` and `verification-live/hosted-media/browser.json` for local evidence. The one-time uploader identity was deleted and its temporary Edge Function was replaced with a disabled 410 response.
