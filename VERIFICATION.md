# Historical local frontend verification

This records the original local-only build. Current Supabase implementation, actual hosted probes and blockers are documented in [SUPABASE_VERIFICATION.md](SUPABASE_VERIFICATION.md). Statements below about the absence of integration describe the earlier build, not the current source.

Executed locally on 17 October 2026 against Chrome and the Vite production preview at `http://127.0.0.1:4173/`.

## Passing checks

- `npm run typecheck` — TypeScript passes. `npm run build` also includes full TypeScript compilation.
- `npm run build` — production assets generated successfully in `dist/`.
- `npm run test` — **14 tests passed**, covering unique watch intervals, seeking/background/paused rejection, repeated intervals, locked content, exact chapter/question mapping, 4/5 zero reward, successful retry, practice-only rewards, duplicate submissions and callbacks, restored session/answers, malformed saved state, all four required integer-money examples, insufficient balance, quote/cancel without debit, idempotent confirmation and restored completed orders, ledger-derived balance, stable recognition and the 495 → 500 → 250 presentation journey.
- Dependency installation/audit after patching the test runner reported **zero vulnerabilities**.
- `node scripts/browser-check.mjs` — passed against the production preview. Covers demo onboarding, subject selection, chapter discovery, Coming Soon dialog, actual animated playback, genuine teaching-note download, presenter preset followed by normal fifth-lesson watch qualification, invitation, Later/resume, all five submitted answers with feedback, reload during an attempt, perfect result, immediate 500-coin wallet, ₹50/250-coin discount on the ₹500 subscription, simulated confirmation, persistent 250-coin wallet, direct routes and no horizontal overflow at 360/390/430px.
- `node scripts/browser-revision.mjs` — passed against the production preview. Watches **all five lessons without seeded progress** at the genuine 2× playback setting; verifies distinct 1/5 through 5/5 progress; completes a 4/5 attempt with zero reward; reviews all answers; completes a fresh successful retry; finishes a third practice-only attempt; verifies exactly one five-coin reward event/ledger entry and correct mapped reel IDs; tests Plus lock, simulated Pro upgrade, player access after upgrade and non-subscriber navigation.
- Both browser scripts assert that no uncaught page errors were emitted.

No lint script is configured; lint was not claimed as run. Browser scripts require installed Chrome (or change the Playwright channel to an available browser).

## Visual inspection and reference

The supplied video was located at `C:/Users/lenovo/Downloads/Reels_Figma_Flow_Analytics (3).mp4` (208 seconds). Representative frames were extracted into `reference-frames/`. Frames at 8, 30, 38, 45, 60, 85, 110, 124 and 140 seconds were inspected for Home/discovery, chapter list/detail, player, settings, Pro lock, notes and Downloads. Only the embedded mobile app screens informed the UI; the analytics slide layout was not reproduced.

The implementation uses a near-black centered mobile stage, cyan controls, dark illustrated chapter-book covers, compact chapter header and topic rows, a full-height lesson player and bottom sheets. Rewards use gold. Screenshots in `verification/` include Home, player, invitation, perfect and failed results, answer review, Pro lock, checkout preview, wallet after redemption, cohort, desktop and chapter detail at 360/390/430px. These were visually reviewed. Full-page screenshots contain a viewport-fixed bottom bar; scrolling makes all underlying content reachable through the bottom content padding.

## Issues found and fixed

- Stabilised component identity so persisted watch-progress updates cannot remount/reset the player.
- Added accessible names for subject buttons.
- Persisted playback checkpoints and checkout request IDs; completed checkouts restore their confirmation instead of generating another debit.
- Added a synchronous in-flight guard for shared actions.
- Kept the lock dialog open until the Pro persona refreshes, and disabled Play All during the transition, preventing a rapid click from reopening the lock dialog.
- Patched test dependencies identified by the package audit.
- Used a production preview for final browser runs to avoid development reloads during interaction. Corrected a test-script currency-character encoding issue.

## Deliberate limitations

- Lessons are explicitly labelled local animated demos with readable teaching text and diagrams. No genuine educational MP4 clips were supplied. The Figma video is never played as a lesson.
- There is no audio track. Mute preference persists; native MP4 replacement, real buffering/error behavior for supplied media, OS Web Share and external production hosting were not end-to-end exercised because these assets/platforms are not part of this demo.
- Teaching notes genuinely download as text files. Animated-video downloads and full offline app caching are not implemented. Downloads removal manages the local library entry, not the browser's file system.
- English teaching content is supplied; the onboarding language preference persists and Hindi content is explicitly described as upcoming.
- Cross-tab/device atomicity, trusted entitlement, secure grading and anti-abuse cannot be provided by inspectable local storage. The mock is deterministic for local demonstration, with production authority documented separately.
- No Supabase integration, backend deployment, SQL mutation, real login, payment or purchase fulfillment was performed.

## Changed files

The original prompt remains unchanged. New application/configuration files: `index.html`, `package.json`, `package-lock.json`, `tsconfig.json`, `vite.config.ts`, `.gitignore`, `.env.example`; typed content and UI in `src/content.ts`, `src/main.tsx`, `src/styles.css`, `src/vite-env.d.ts`; async service contracts, local implementation, isolated future Supabase boundary and focused tests in `src/services/`; browser verification scripts in `scripts/`; setup/content/backend handoff in `README.md`, `CONTENT_GUIDE.md`, `SUPABASE_HANDOFF.md` and this report. Reference frames and verification screenshots are retained locally for review.
