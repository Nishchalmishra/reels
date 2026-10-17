# Supabase verification — 17 October 2026

**Deployed and verified against actual project `pbaegeqjargtsfortsyh`. No remaining integration blocker.** Earlier access/Auth blockers are resolved. Secrets were withheld from reports, frontend assets and logs.

## Actual deployment

Official authenticated MCP calls inspected current columns, function definitions, policies, RLS, storage buckets, Auth triggers and historical counts before migration. Historical profiles, reels, questions, attempts and ledger were empty; existing tables had RLS and no policies. No clips or storage buckets were present. Bundled lessons remain labelled animations.

All three migrations are applied: `202610170001_reels.sql`, `202610170002_demo_content.sql`, `202610170003_public_security.sql`. Hosted counts: four chapters, ten lessons, ten safe questions, ten explicit mappings, three products, eight marked test profiles, zero normal profiles. Historical counts remained zero. The final migration makes public catalogue functions invoker-security and the historical leaderboard honor underlying RLS.

The hosted privilege audit passed: application tables have RLS; browser roles cannot access private keys/schema, insert rewards or update progress; anonymous completion is denied and authenticated completion permitted. Final advisors have no ERROR or anonymous definer-function warning. Remaining advisories concern intentional authenticated trusted functions, deny-all tables without policies, enabled anonymous Auth, and existing disabled leaked-password protection (no password login is used). RLS was not weakened.

## Hosted acceptance

| Check | Actual result |
| --- | --- |
| Session/profile creation and restoration | Passed; real anonymous Auth plus explicit refresh/re-login |
| Catalogue and media playback | Passed; hosted catalogue and five timed animations in mobile browser |
| Coverage survives reload | Passed; reload after two lessons restored stored progress |
| Incomplete/mixed/repeat/seek/background qualification | Passed; backend rejects false unlocks |
| Attempts, answers, review and exact mappings | Passed against stored rows |
| 4/5 zero then 5/5 retry | Passed; exactly five coins total |
| Refresh/retry/concurrent completion | Passed; one reward and stable existing outcome |
| Wallet matches ledger | Passed; actual rows reconciled |
| Prices/cancellation/four money examples | Passed, including INR 99 maximum 49 coins / INR 9.80 |
| Concurrent spending and order idempotency | Passed; one competing spend, no duplicate debit/order |
| A/B ownership and write restrictions | Passed; cross-user access and direct rewards denied |
| Private keys inaccessible to browser | Passed; safe questions omit keys |
| Analytics UUID dedup and privacy | Passed; test flags persisted, personal payload stripped |
| Full live mobile journey | Passed; watches, failed quiz, perfect retry, wallet, cancel, simulated order, reload |

API evidence: `supabase/inspection/live-verification.json`, run `18e7cb56-9ddb-4313-b08d-884bfff2b252`, ten passing checks. A: `093af569-3b0d-4b5e-8865-08016d436447`; B: `7cfe6f66-6521-4b10-b2b0-4fe514044f5b`. The current API run additionally verified concurrent duplicate answers produce one row, conflicting resubmissions cannot change it, premature completion fails, and post-completion resubmission returns the existing result. A received a labelled TEST-only 495-coin fixture for cap/concurrency tests. Normal accounts received none.

Mobile evidence: `verification-live/full-journey.json`, run `10a75af6-d2ff-40f0-b406-49f3b53715ba`, identity `af372f49-4c83-4174-a21f-ff2ca9ac2583`. Actual two attempts, ledger +5/-5, one order `ea621d1a-9282-42c2-83c7-6640db13fcdd`, final balance zero, recognition unchanged. Screenshot: `verification-live/completed-simulated-order.png`. The browser starts with a genuine SDK-created session and reloads midway through both quizzes, restoring the next unanswered question. Separately, `verification-live/onboarding.json` records a passing test of the actual production onboarding form, real anonymous signup, entered name/batch database persistence and reload. Only the outgoing profile TEST flag is marked by automation; authentication, entered values, responses and persistence remain real.

After final hardening, `scripts/verify-live-restoration.mjs` passed public reads, Auth refresh/re-login, persisted wallet/order, repeated idempotent confirmation and mobile order reload, creating no users. Evidence: `verification-live/post-hardening.json`.

Eight marked identities are retained for reproducibility and excluded from other students' recognition results. The rerun added two API identities, one successful mobile identity, one onboarding identity and one interrupted browser-test identity to the original three. That test expected previous answer feedback after reload; the application correctly resumed at the next unanswered question. The expectation was corrected and the full rerun passed, preserving application behavior. Session files are ignored, never printed and denied by the live server. Cleanup must target only these exact test users. No existing student data was deleted.

## Local checks

- Typecheck, demo build and live build passed. Live bundle excludes mock grading/storage and checks configured privileged values without printing them.
- 23 tests passed: 14 local, six embedded PostgreSQL, three adapter tests. Actual migration batch applies twice with checksums. Hosted concurrency was independently checked above.
- Existing local demo and unseeded mobile regression passed: playback, failed/perfect/practice quizzes, wallet, redemption, locks and navigation.
- Live dev requests for `.env`, backend SQL/tooling and local grading source returned 403. Production must serve `dist-live`, never workspace root.
- No lint script is configured. Historical `browser-live.mjs` error diagnostics document resolved blockers and are not current acceptance results.

Run `npm run test:live`, `npm run test:live:browser`, `npm run test:live:onboarding` and `npm run test:live:restore` (preview port 4174). Full reruns create marked identities and archive reports in `supabase/inspection/runs/` or `verification-live/runs/`. Restoration now selects the mobile session by run ID, independent of test execution order.

Actual-record invariant audit (`supabase/inspection/current-invariants.json`): zero duplicate rewards, invalid reward amounts, negative wallets or duplicate orders. RLS and private-key/direct reward restrictions reconfirmed. No new migration or MCP reconfiguration was needed.

Other retained identities: original API A `6ac77c54-a315-4840-b55f-00a3db1310dc`, B `41f47154-247e-4ed3-92a8-671e74941860`; original mobile `43d35185-4421-44f2-be10-6ab1dc6deb4f`; corrected-test diagnostic `dfacff88-c0a9-4ef5-b75b-000414e51c0d`; onboarding `f22e5c63-3a72-45f8-b098-d81758d50c23` (run `c6ff6540-4d42-4bbd-896f-7cae9f63952c`). Current API/mobile IDs appear above. Cleanup must target exact IDs with verified TEST ownership.

## Limits

Client playback reports cannot prove attention or eliminate determined abuse. Clearing an unlinked anonymous session loses access. Cross-device account recovery, real teacher MP4s, subscriptions and payments were not implemented or verified. Checkout is a labelled simulation. These are scope limits, not deployment blockers.
