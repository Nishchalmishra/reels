# Web Feed and Explore handoff

The active demo is the responsive React/TypeScript application. The native Android source is preserved in `native-app/`, but APK and emulator work is paused at the user's request. Existing web onboarding, authenticated profile, wallet and simulated checkout are reused. This is not a claim that the Android onboarding was ported screen for screen.

## Run

```sh
npm run live
# http://127.0.0.1:5173
npm run build:live
npm run preview:live
# http://127.0.0.1:4173
```

The workspace `.env` selects project `pbaegeqjargtsfortsyh`. Only its URL and browser-safe publishable key enter the frontend. `.env.example` has placeholders. Live errors are displayed; no mock data or fake success replaces a failed Supabase request. `npm run demo` retains the separate, older local chapter demo. Revised discovery requires live mode.

Vite development and production-preview middleware stream only manifest-whitelisted MP4s from `videos/`, with byte ranges. Ten small posters are in `public/posters/`. No full media library copy, upload, AWS or S3 integration was added. Static hosting of `dist-live/` alone does not include the media: a public deployment needs matching `/media/<lesson-id>` endpoints. The current demo is local, not an internet deployment.

## Experience

Home links to Reels, which opens a playing vertical Feed. Feed and Explore are the two primary Reels destinations; a small coin action opens the existing wallet. Main navigation remains Home, Reels and Store.

One active visible HTML video plays. Inactive players pause; bounded neighboring players load metadata. Quizzes, notes, Explore and background visibility pause playback. The player has mute, pause, speed and seek controls, actual lesson posters, errors/retry and an autoplay tap fallback. Likes and saved lessons persist through the backend. Sharing produces a source lesson link. Notes show the supplied concept and allow a text revision-note download; full transcripts and offline video downloads are explicitly unavailable.

The session queue mixes topics and avoids its recent six where the library allows. Render/state updates do not shuffle it. Queue, position, Explore query/filter/scroll and current attempt are stored per authenticated identity. Returning from Explore preserves the Feed queue. Hosted progress, quiz answers and wallet remain associated with the same Supabase user. Clearing browser authentication loses access to an anonymous identity; retaining the session is necessary to restore it.

The supplied folder has 11 files and 10 unique videos; duplicate Rock painting footage was excluded by SHA-256. `reference-frames/discovery/manifest.json` records measured duration, stable ID, source file and teaching metadata. Class 11 has six Physics clips; Class 12 has four. Other classes/languages show an honest empty state. Representative teaching frames/equations were reviewed, but full audio/transcript editorial review remains pending. These are supplied animated lessons, not claimed as real teacher recordings.

## Trusted backend

Existing project-scoped Supabase MCP was authenticated and real tool calls succeeded earlier. It was reused without reconfiguration. It is not available as a callable namespace in the latest resumed session; browser/API verification still uses the actual hosted project. The following migrations were already applied and verified before the web switch:

- `202610190004_discovery.sql`: isolated discovery media/questions, server cycles, unique interval coverage, frozen batch/item mappings, attempts/answers, private answer keys and authenticated RPCs.
- `202610190005_discovery_content.sql`: explicit idempotent seed of ten actual lesson metadata records and twenty mapped questions; no student balances or fictional activity.
- `202610190006_discovery_analytics.sql`: deduplicated server events from discovery state changes.
- `202610190007_discovery_lifecycle.sql`: acceptance retains the cycle; completion or explicit deferral advances it once and prevents overlapping invitations.

Original chapter tables, constraints, RPCs, unrelated policies and existing records are preserved. The initially proposed broader migration was rejected by automatic review and was not applied; the deployed replacement uses isolated discovery structures.

The latest revised brief supersedes the original five-videos/one-chapter unlock. Each server cycle stores a private threshold of three or four distinct qualifying videos. Eighty percent unique active/visible playback is required. Seeks, repeat intervals and background time cannot add coverage or distinct video counts. The backend freezes five mapped questions representing every qualifying source in the batch: 2+2+1 for three sources or 2+1+1+1 for four. Missing mappings produce a diagnostic instead of generic questions. Threshold/countdown is not exposed in the student UI.

Question grading uses private answer keys. Ownership, state transitions and saved eligibility are checked on the backend. Completion calculates the saved score and inserts exactly five coins for 5/5, once per canonical sorted content/version/question set. Below five earns zero; later perfect retries may earn the previously unearned reward. Atomic ledger constraints and profile locks protect duplicate completion. Client playback reports cannot prove attention or eliminate determined abuse.

The existing ledger supplies the wallet; recognition sums earned rewards rather than net balance. Five coins = INR 1. Redemption uses backend product prices, integer paise and a maximum ten-percent discount. Quotes do not spend coins. Confirm creates a SIMULATED order and debit atomically under the profile lock, with request-key idempotency. No SMS, real payment or real subscription activation occurs.

## Verification evidence

- Current typecheck and live build passed. The live-bundle check excludes local mock grading/storage and configured privileged credential values.
- 31 tests pass across five suites: original backend/local/adapter contracts, isolated discovery backend contracts, and session queue behavior including a 1,000-choice recent-window test.
- Prior actual hosted API runs verified anonymous session restoration, persisted 3/4 cycles, invalid watch rejection, frozen mixed-topic mappings, deferral/lifecycle, 4/5 zero, perfect retry five, concurrent reward completion, private keys/cross-user denial, quote cancellation, concurrent simulated spending and unchanged recognition. Those reports are retained under ignored `verification-native/` because they predate the web switch.
- Mobile web at 390 x 844 passed against the production preview. Actual clips were watched at the permitted 2x speed without seeking until the server offered a quiz. Explore search/no-results/back restoration, a single playing video, notes pause, saved preference persistence and authenticated refresh passed.
- The first attempt exposed a quick-tap race after deferral. The saved-quiz button now disables while saving; the same TEST identity then resumed from actual stored eligibility, without a synthetic qualification fixture.
- The resumed UI run passed answer persistence across refresh; scores 4, 5 and 5; exactly one five-coin reward; concurrent/repeated completion; no repeat coin animation; wallet reconciliation; cancelled quote with no debit; simulated INR 500 checkout with INR 1 discount; duplicate confirmation; order restoration; and unchanged recognition. A direct authenticated table read confirmed exactly two hosted ledger rows, +5 and -5. No page errors occurred.
- Verification run `234dd582-a221-4721-8a4e-c05fc3d0d4ad`, TEST user `df89c73c-feb6-45eb-8c87-8dc73b090a8b`. Attempts: `f772f33f-259a-48bd-9fee-6d3112765962` (4/5), `93eb91f6-a8df-4208-9828-cd750adeceea` (5/5 rewarded), `868f321c-f906-4800-9e13-b383db9f6e1a` (perfect practice). Simulated order: `7a29054f-638d-4077-90ce-4885f2facf4f`.
- Evidence: `verification-live/web/journey.json` preserves the initial playback run and discovered issue; `journey-complete.json` records the successful continuation using that same student. Screenshots include Feed, Explore, quiz, result, wallet and checkout. `scripts/web-discovery-journey.mjs` reruns the full fresh-identity journey; `scripts/web-discovery-complete.mjs` continues a retained failed run and is not a standalone fresh-user test.
- Required INR 500 / INR 2,000 / INR 99 rounding examples and insufficient-balance cases pass in embedded PostgreSQL contract tests. The current hosted UI run specifically used five coins on INR 500; it did not seed a 500-coin hosted balance.

Only clearly marked TEST identities are used. Test sessions stay in ignored `supabase/.test-sessions/`, never in the frontend bundle or logs. Existing normal student records are preserved. Test accounts are retained for inspection; no presenter balance is seeded into normal accounts.

## Remaining limits

No blocker remains for the local connected web demo. Internet hosting and hosted media delivery are not configured. Full audio/transcript editorial review, physical mobile-browser coverage and a screen-for-screen port of native onboarding were not performed. Native device acceptance is paused, not represented as passed by these web tests.
