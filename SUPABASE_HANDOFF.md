# Supabase integration handoff

Status: **connected, deployed and hosted-verified** on project `pbaegeqjargtsfortsyh`. Official MCP, anonymous Auth, live services, secured grading/rewards and simulated checkout work. No remaining integration blocker. See [SUPABASE_VERIFICATION.md](SUPABASE_VERIFICATION.md).

## Adapter and contracts

`src/services/contracts.ts` keeps the async application contract (historically named `DemoService`) for profile/session, catalogue/products, unique-timeline progress/checkpoints, quiz start/submit/finish/review, wallet/ledger, trusted quotes/simulated orders, recognition, analytics and UI preferences. `src/services/index.ts` explicitly selects LocalDemoAdapter or SupabaseAdapter using `VITE_SERVICE_MODE`; no failed live call falls back to mocks. Live presenter operations are rejected. `src/services/state.ts` contains shared DTO helpers without grading keys.

`src/services/supabase.ts` uses Supabase Auth, persistent SDK sessions and RPCs. Profile/row ownership is always `auth.uid()` on the server; no local demo ID, requested persona, client score, eligible flag, price or balance is authoritative. A new MVP guest has server-default Pro content access; that is not a real paid subscription. User profile updates cannot change entitlement or balances.

## Actual project inspection

Official OAuth-authenticated MCP inspected actual tables, columns, functions, RLS policies, storage and Auth triggers before changes. Historical profiles/reels/questions/attempts/ledger were empty, with RLS enabled and no policies. No existing clips or buckets were found. Existing data and policies were preserved. The owner enabled anonymous Auth in the dashboard; real guest sign-in and restoration passed.

The ignored `.env` selects live mode and supplies only browser-safe project configuration. Privileged deployment uses Codex-managed OAuth through actual official MCP calls, without copying tokens into the workspace. Run `npm run mcp:verify`, `npm run mcp:inspect`, then `npm run db:migrate:mcp`. Optional direct DB/management runners remain available but are not required for the working MCP route.

## Deployed additive migrations

- `supabase/migrations/202610170001_reels.sql`: new `ar_reels_*` application tables, private answer schema, RLS/read policies, least-privilege grants and trusted RPCs.
- `supabase/migrations/202610170002_demo_content.sql`: explicit, idempotent seed of the existing two playable chapters, ten unique lessons/questions with exact mappings, two Coming Soon chapters and three sample-priced products. No profiles, watch progress, presenter coins or fictional activity are seeded.
- `supabase/migrations/202610170003_public_security.sql`: invoker-security catalogue functions and historical leaderboard view; no columns, data or policies removed.

`ar_reels_migrations` records SHA-256 migration versions. One transaction and an advisory migration lock guard application; repeat application is safe and changed checksums fail. Existing historical tables and policies are not reset, dropped or overwritten. The historical leaderboard now honors underlying RLS through invoker security. New names deliberately avoid guessing the historical schema before privileged inspection. This preserves existing data; it is not a claim that the old schema has been reconciled or migrated.

After committing, the runner rechecks actual RLS, private-schema/key access, direct reward/progress write denial and anonymous/authenticated finish privileges. A passing direct-runner audit produces `supabase/inspection/deployment.json`; the official MCP route produces `mcp-deployment-audit.json`. The audit passes both hosted and embedded PostgreSQL. Actual hosted evidence is in `supabase/inspection/mcp-deployment-audit.json`; deployed versions/counts are in `mcp-deployed-records.json`.

| Historical structure                | New integration / future reconciliation                                                                                                                                                                       |
| ----------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| profiles                            | `ar_reels_profiles`, authenticated UUID ownership, name/batch/language, server-owned persona and test flag. Reconcile existing Auth/profile triggers only after inspection.                                   |
| reels                               | `ar_reels_chapters` / `ar_reels_media`, typed content, actual duration, availability, content type and display order. Preserve authorized real sources if found; current content remains labelled animations. |
| questions                           | Browser-safe `ar_reels_questions`; correct indexes/explanations in non-exposed `ar_reels_private.answer_keys`. Audit historical question grants before reusing them.                                          |
| quiz_attempts                       | `ar_reels_challenges`, `ar_reels_challenge_items`, `ar_reels_attempts`, `ar_reels_answers`: versioned mapping, ownership, locked persisted submissions and finish state.                                      |
| reel_events                         | `ar_reels_progress` stores merged timeline intervals/checkpoints; `ar_reels_events` stores deduplicated analytics. These are distinct concerns.                                                               |
| coin_ledger                         | Immutable-through-client `ar_reels_ledger`; unique `(user_id,reward_key)` with reward_key equal to the stable versioned challenge ID. No client insert/update grants.                                         |
| leaderboard                         | `ar_reels_recognition()` uses positive learning reward activity, not net balance. Public labels are pseudonyms; test accounts are excluded except the caller's own row.                                       |
| missing preferences/orders/products | `ar_reels_preferences`, `ar_reels_products`, `ar_reels_checkouts`, `ar_reels_orders` retain local UI behavior using server state and trusted prices.                                                          |

## Trusted operations

Every mutating public function requires Auth and uses fully qualified objects with empty `search_path`. Only explicitly listed function signatures receive execute permission. Private helper functions/schema/answer keys have no anon/authenticated access. Private tables have own-row SELECT policies; direct client mutation is revoked. Browser-safe catalogue access exposes teaching material/options, never answer keys. Backend files and local mock grading source are denied by the live Vite server and absent from live production assets.

`ar_reels_progress` validates visible/playing advancement, timing bounds, speed, actual duration, active educational content and entitlement, locks the progress row and merges unique intervals. Repeats/seek gaps/background/paused reports add no coverage. `ar_reels_start` checks five distinct mapped reels from one chapter with at least 80% stored coverage and matching question-to-reel IDs; it serializes start against the user's profile and resumes the one unfinished attempt.

`ar_reels_submit` locks the owned attempt, checks the exact mapped question/order/state, grades against private keys and saves immutable feedback. Duplicate submissions return their existing answer. `ar_reels_finish` locks the profile then attempt, rechecks eligibility, calculates the score from stored submissions/private keys, completes atomically and inserts exactly five coins only for a first 5/5 on that versioned challenge. The unique ledger constraint prevents repeat rewards. A failed full retry remains eligible until one perfect result is rewarded; later practice does not mint coins.

`ar_reels_quote` reads server-owned integer prices/eligibility and ledger balance. It never debits. `ar_reels_confirm` locks the same user profile used for rewards, recalculates the quote, rejects insufficient funds, checks request idempotency and commits the SIMULATED order/debit atomically. Competing orders cannot spend the same balance through these RPCs. No coupon, shipping/tax eligibility, real payment or entitlement grant is added.

Integer rule: coin = 20 paise; cap = floor(eligibleSubtotalPaise * 10 / 100); maximum coins = min(balance, floor(cap / 20)); discount = coins * 20. INR 99 therefore permits 49 coins / INR 9.80. Ledger sum is the wallet authority. Recognition uses historical positive reward activity and remains stable when coins are spent.

## Analytics and tests

Events use UUID IDs, Auth owner, session headers/IDs, UTC timestamps and relevant content/attempt identifiers. Authoritative quiz/reward/redemption events are emitted inside server transactions. Client analytics is whitelisted and ignores arbitrary personal/answer-key payloads. Event IDs deduplicate. Test flags come from the dedicated profile; presenter events/balances are local only.

`npm run test` runs the original local tests, actual migrations/RPCs in embedded PostgreSQL and adapter trust-boundary tests. The exact migration batch is applied twice in tests. This is not hosted or multi-connection verification.

`npm run test:live` creates dedicated marked A/B guest identities, checks persisted rows and RLS, and exercises concurrent completion. It adds a 495-coin fixture only to its dedicated TEST identity when database access is available, then verifies caps and parallel spend protection. `scripts/browser-live-journey.mjs` exercises real playback, failure/retry, reward, cancellation and simulated confirmation with actual-record reconciliation. Both passed against the actual project, including concurrent completion/spending and full mobile playback. Marked identities are retained; exact IDs and evidence are in SUPABASE_VERIFICATION.md. Session files are ignored and are never printed. If test identities are retained for reproduction, their exact IDs/run are recorded; no existing students are cleaned up.

## Running and operational notes

Use `npm run live` with the existing `.env`, or set `VITE_SERVICE_MODE=supabase`, `VITE_SUPABASE_URL` and `VITE_SUPABASE_PUBLISHABLE_KEY` from `.env.example`. `npm run demo` explicitly selects isolated local mode. Live failures surface meaningful errors with no mock fallback. Build with `npm run build:live`; serve only `dist-live` with SPA route rewrites. The verified preview runs at http://127.0.0.1:4174.

Typecheck, both builds, 23 tests, ten hosted API checks and the full live mobile journey passed. A focused post-hardening check additionally verified public catalogue, Auth refresh/re-login and persisted order UI. The latest rerun also checks concurrent duplicate answer submissions, immutable stored answers and mid-quiz browser reload. The actual production onboarding form also passed real anonymous signup and persisted name/batch checks after reload. Run it with `npm run test:live:onboarding`; explicit session refresh/restoration is `npm run test:live:restore`. Dedicated tests remain marked/excluded from normal recognition; no presenter fixtures enter normal accounts.

The final security advisor has no ERROR or anonymous definer-function warning. Expected warnings cover authenticated trusted RPCs, anonymous Auth, intentional deny-all tables and existing leaked-password protection disabled for a project where this MVP uses no passwords. No unrelated Auth/policy settings were changed. No manual deployment step remains.

Watch reports alone cannot prove attention or eliminate determined abuse. The RPC validates timing/coverage consistency and ownership, not human attention. Production anti-abuse, recoverable account linking, more robust catalogue version management and any real payments remain separate work.
