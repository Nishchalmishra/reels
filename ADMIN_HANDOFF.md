# Admin studio

Open `/admin` on the same web deployment. The admin area uses the existing black/cyan visual system, a separate Supabase email/password session and server-enforced authorization. Student onboarding and guest sessions are kept separate.

## Activate the owner account

The email provided in the development session is approved in the private `ar_reels_private.admin_emails` allowlist. It is not embedded in the frontend. It did not have an Auth account when provisioning ran. The owner subsequently registered and confirmed it; the actual database audit confirms both verification and active approval.

1. Open `/admin` and choose **Create admin account**.
2. Use the approved email, choose a password of at least 12 characters and verify the confirmation email.
3. Return to `/admin` and sign in. An unapproved or unverified account cannot enter.

No password was generated for the owner or stored by this implementation. Registration sends a confirmation email only when the user submits the form. If the confirmation lands on a different URL, return to `/admin` after confirming. Configure the actual deployed domain in Supabase Authentication URL Configuration for a direct return to the admin page. Email sign-in and account registration were confirmed enabled; the owner completed account activation during implementation, and the confirmed/approved account was verified in the actual database. Its password was never accessed.

For local use: `npm run build:live`, then `npm run preview:live`. Vercel needs the updated frontend redeployed; the existing `vercel.json` handles `/admin` deep links. Only browser-safe URL/publishable-key configuration is needed by the frontend. `ARIVIHAN_ADMIN_EMAIL` is an optional, tooling-only ignored `.env` setting used for owner provisioning; never prefix it with `VITE_`.

## Add 25-30 reels

- Open **Upload reels**, choose common class/subject/language defaults, then select multiple MP4s. Limits are 50 MB per video and 30 minutes per reel.
- Edit each reel's title, chapter/topic, description, class and languages. Duration and a JPEG poster are extracted from the actual video; full SHA-256 identities are calculated locally and verified again on the server.
- Add at least two distinct, lesson-related questions per reel. Each has four different options, one correct choice and an explanation. Up to ten questions can be authored per reel.
- **Upload / save draft** uploads files without making them visible to students. **Publish reel & questions** verifies and atomically publishes the catalogue record, mapped questions and private answer keys. Bulk save and publish process the queue sequentially; individual failures remain visible and do not pretend to succeed.
- A downloaded JSON template supports batch metadata/question authoring. Match each `file` field to the exact MP4 filename, import it and review before publishing. The format has `title`, `subject`, `topic`, `concept`, `grades`, `languages`, `isTest`, optional `notes`, and `questions: [{prompt, options: [four strings], correct: 0..3, explanation}]`.
- Saved drafts can be reopened after refresh. Verified uploads can be published without selecting the MP4 again. For an interrupted upload, select the original file and retry. Duplicate file hashes reuse the owner's draft or reject an already catalogued clip.
- Published content is read-only to preserve saved quizzes. The library can preview the actual video and its mapped questions. Hidden test content remains out of the student catalogue.

Questions are authored/reviewed by the admin; this release does not claim to understand a video or generate accurate quizzes from a filename. The existing quiz selector automatically uses the published questions from the exact qualifying videos a student watched. Three/four-source cycles, five-question distribution and the five-coin one-time reward remain unchanged.

## Analytics definitions

- **View:** a playback session with at least three seconds of valid active playback. Repeat requests with the same sequence do not add time or views.
- **Unique viewers:** distinct authenticated students among counted views in the selected range.
- **Active watch time:** accepted foreground playing time; repeat watching within a session can contribute time. This differs from unique interval coverage used for quiz eligibility.
- **80% qualifications:** stored qualifying reel/cycle coverage milestones, including historical data where available.
- **Completed quizzes / coins earned:** trusted backend completion and reward events; spending does not reduce earned recognition.
- Daily charts use India Standard Time. Filters cover 7/30/90 days. Per-reel tables include views, viewers, watch time, qualifications and mapped-question counts, with search and CSV export.

New view/session tracking starts with this release. It does not invent historical views from older watch events. TEST accounts are excluded by default and can be included explicitly. No student names or private answers are returned in analytics. Playback reports cannot prove attention or eliminate deliberate client abuse; they never supply an authoritative score or coin balance.

## Backend and storage

Versioned migration `202610270009_admin.sql` adds private email approvals, private draft/upload records and audit records, private playback sessions, guarded admin RPCs, and a wrapper around the existing progress RPC. The existing grading, eligibility, reward and redemption functions are preserved.

`ar-reels-admin-media` verifies the actual Supabase user, confirmed email, database role approval and draft ownership. Its service key stays only in the Edge runtime. It issues temporary tokens restricted to stored draft paths, verifies actual MP4/JPEG signatures, lengths and SHA-256 after upload, and marks verification through a service-role-only function. Student/anon users cannot mark uploads verified or write the bucket. The intended media bucket remains publicly readable for demo playback; it is not paid-video access protection. The upload limit is now 50 MB, with MP4/JPEG MIME restrictions.

Publishing inserts the media, questions and private keys atomically and idempotently. Draft keys are not in student APIs. Student catalogue reads return only active media and question prompts/options. The admin read-only question API is separately authorized. No frontend role flag or editable Auth metadata can grant access: authorization requires a confirmed Auth email in the private allowlist.

## Verification

- Embedded PostgreSQL checks cover student/anonymous denial, unconfirmed-email denial, private data denial, service-only verification, draft ownership, incomplete publish rejection, publication idempotency, private keys, view/session deduplication, invalid/background report rejection, test filtering, and actual new question mappings in a qualifying quiz with exactly one five-coin reward.
- Live browser verification used a temporary confirmed QA administrator and a hidden TEST lesson. MP4/poster upload, server verification, refresh restoration, mapped-question review, repeated publication, mobile layout and student-token denial passed. No fake admin success was used.
- Live student playback created one real view and about five seconds of active watch time. Concurrent repeated telemetry, background reports and seeks did not inflate totals; test traffic remained excluded from normal totals.
- Machine evidence and screenshots are in ignored `verification-live/admin/`. A scoped QA setup function is used only by verification tooling and is disabled after cleanup. Only QA records and objects are removed; the approved owner and normal student data remain intact.

`npm test`, `npm run typecheck`, `npm run build:live` and the relevant live checks are run during implementation. Final results are appended after cleanup below.

Final results: **40 tests passed across seven suites**, typecheck passed, and the live production build/bundle credential check passed. Actual browser checks passed for uploads, mapped questions, private grading keys, draft refresh, idempotent publication, authenticated student denial, real playback analytics and a 30-file queue with metadata import. The 30 files were queued/imported for UI verification; only one hidden QA lesson was actually uploaded/published, not 30 public test lessons. Mobile controls and independent student/admin sessions passed.

Cleanup passed: the QA approval, Auth account, hidden reel, draft, answer keys and its two storage objects were removed; the scoped QA bootstrap now returns 410. Normal content, the real approved owner and student records were preserved. Live analytics verification used an existing marked TEST student and is excluded from normal totals. Owner approval/confirmation, private schema restrictions, service-only verification, migration checksum and zero remaining QA objects were independently audited after cleanup. Evidence: `verification-live/admin/final-audit.json`, `browser.json`, `analytics.json`, and `bulk.json`.

The active local production preview is http://127.0.0.1:4176/admin. The Supabase backend is already deployed. The existing Vercel site still needs the new frontend redeployed; no Vercel deployment credentials or project connection were provided, so that deployment was not performed.
