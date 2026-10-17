You are implementing the complete frontend for an Arivihan educational Reels MVP. Build the working application, not just a plan, landing page, screenshots, or disconnected components. Continue through implementation and verification.

1. SCOPE AND SOURCES

Inspect the repository, package manager, AGENTS.md, and attached references first. Preserve useful existing code. If this is a new project, use React, TypeScript, Vite, Tailwind, React Router, and a lightweight state solution. Use the existing stack if already established.

The attached "Reels_Figma_Flow_Analytics (3).mp4" is the PRIMARY VISUAL REFERENCE. It is a narrated-by-captions Figma walkthrough, NOT educational reel footage. Extract representative frames if tools permit. Use the app screens inside the walkthrough as the design reference; do not copy the surrounding analytics presentation layout.

Observed design: near-black mobile screens, cyan primary controls, white text, muted secondary text, subject chips, illustrated chapter covers, chapter/topic lists, Play All, a full-height reel player, chapter bottom sheet, notes/download actions, Pro lock popups, and coming-soon states. Match the supplied screens' composition and spacing. Extend this visual system to the new quiz/rewards UI. Avoid a generic purple SaaS dashboard, oversized hero, or an unrelated Instagram clone.

The earlier Claude conversation discussed a standalone web MVP with Supabase, a leaderboard, spotlight, and promo slots. Its earlier quiz/reward rules are superseded by the requirements below.

BUILD FRONTEND ONLY. No live Supabase calls, no database migrations, no backend deployment, no payment integration, no account creation, and no requests for credentials. Everything must run with a deterministic local mock adapter. Keep Supabase connection work clearly isolated for later.

2. FIXED PRODUCT RULES

- Challenge = FIVE distinct qualifying educational videos from ONE chapter, followed by FIVE MCQs directly related to those exact videos.
- Each challenge explicitly maps five reel IDs to five question IDs; seed one question per reel.
- All five answers correct earns exactly FIVE coins TOTAL. A 4/5 result earns ZERO coins. Do not silently implement one coin per correct answer.
- FIVE coins = INR 1. ONE coin = 20 paise.
- Coins can discount eligible subscriptions and store products by AT MOST 10% of eligible price, and never exceed available balance.
- No coins for watching alone, likes, sharing, featured content, or promo reels.
- One reward per user/challenge/content-version. Replaying videos, retries, refresh, duplicate callbacks, and double clicks must not duplicate reward.
- Allow practice/revision and a new full attempt after failure. A later 5/5 can earn the reward if that challenge has never been rewarded. Already rewarded challenges remain practice-only.
- Do not add an unrequested daily cap, expiry, paid coins, streak penalty, or random reward.
- Recognition is based on learning participation/lifetime earned activity, not remaining spendable balance. Redeeming coins cannot lower rank.

3. STUDENT EXPERIENCE

Implement this complete journey:
Reels Home -> subject/chapter discovery -> chapter detail -> Play All or selected topic -> reel playback -> progress 1/5 through 5/5 -> quiz invitation -> five questions -> result/review -> wallet -> eligible product/subscription -> redemption preview -> simulated order confirmation.

Add a lightweight demo onboarding screen collecting name, class/batch, and language, or a one-tap "Continue with demo student" option. This is a local demo identity, not real authenticated access. Restore its session on refresh. Do not put login in the middle of the presentation.

Use usable routes and back navigation. Preserve chapter, playback, quiz, and wallet state across navigation/reload. Support direct-route loading.

4. SCREENS TO FINISH

Reels Home:
- Match the reference's chapter discovery layout and subject sections.
- Physics, Chemistry, Biology subject chips; search; See All.
- Resume Learning card, unobtrusive wallet entry, and challenge explanation.
- Search/filter really work against local catalogue. No dead cards.
- Secondary catalogue items without content explicitly show Coming Soon and an intentional popup; never pretend videos are available.

Chapter Detail:
- Cover, chapter name, actual available video count/duration, Play All, ordered topic rows, watched markers.
- A compact "Watch 5 videos. Answer 5 questions. Earn 5 coins = INR 1" challenge card.
- Topic rows open the selected reel.
- Each chapter retains independent progress.
- If a chapter has multiple groups, group videos into explicitly defined non-overlapping five-video challenges.

Player:
- Mobile full-height player with vertical navigation, title/topic, play/pause, mute/unmute, timeline, replay, playback speed, like/save/share, notes, and chapter sheet.
- Only active video plays. Pause when the quiz/sheet opens, another reel becomes active, page becomes hidden, or player unmounts. Buffering is not watch time.
- Preserve user mute preference; handle autoplay rejection with a clear Tap to Play control.
- Use scroll-snap/gesture handling without conflicts with quizzes or bottom sheets.
- Small challenge progress pill; do not obstruct the lesson with a large reward banner.
- "In This Chapter" bottom sheet supports selecting topics and navigating to another chapter.
- Implement speed options meaningfully. Show quality selection only when real alternate sources exist; otherwise show Auto without inventing quality changes.
- Share uses Web Share when supported, clipboard fallback otherwise. Handle failure gracefully.
- Notes open a real local teaching note or accessible document preview.
- Download works for bundled downloadable media/notes. Do not show a fake successful download; if a sample cannot be downloaded, explain or disable that action. Do not claim full offline playback unless implemented.
- Include a Downloads screen for genuinely saved demo items/notes, with real open/remove actions.

Watch qualification:
- Require at least 80% UNIQUE media-timeline coverage per distinct reel while visible and actively playing. Seeking past content, repeat loops, paused time, buffering, background tabs, and duplicate events do not create coverage.
- Store/merge watched time intervals or equivalent coverage buckets. Respect actual timeline advancement at different playback speeds.
- Videos from other chapters, locked/unavailable content, promotional items, and repeated videos do not contribute to the current challenge.
- Persist progress across sessions.
- This is frontend demo eligibility only; document that real anti-abuse verification requires backend work.

Challenge Invitation:
- At five qualifying reels, pause and show a polished bottom sheet:
  "Ready to test what you learned?"
  "5 questions from the videos you watched."
  "Get all 5 right to earn 5 coins = INR 1."
- Start Challenge and Later.
- Later must work. Keep a Resume Challenge CTA; do not re-open on every progress callback.
- Never show more than one quiz overlay or mix question sets.

Quiz:
- One question per screen; Question X of 5; four options; explicit Submit Answer.
- Disable submission before selection and while pending.
- After submission, lock the answer and show correct/incorrect feedback with a concise teaching explanation; then Next.
- Do not award coins after individual answers.
- Preserve in-progress answers on refresh; handle leaving/resuming cleanly.
- Prevent duplicate submissions and double navigation.
- No speed timer or negative marking.

Result/Review:
- 5/5: "Perfect score! +5 coins", clearly show "= INR 1", update wallet immediately, brief restrained coin animation once.
- Below 5/5: supportive accurate feedback, zero coins, incorrect topics, review/replay actions. Do not falsely imply coins were earned.
- Review shows selected answer, correct answer, explanation, and replay link to the mapped reel.
- Retry starts a fresh full attempt; previously saved answers do not become new submissions.
- Already rewarded challenges say "Practice completed — reward already earned", with no duplicate animation/ledger entry.
- Keep Continue Learning, Review Answers, and Wallet navigation working.

Wallet:
- Integer coin balance, rupee equivalent, earned/spent transaction history, transaction references, reward explanation, maximum-10%-discount rule.
- Derive local balance from ledger; do not let separate counters drift.
- Empty wallet state and redemption entry.

Store / Subscription:
- A small polished demo store with at least one subscription and two physical/sample products, using clearly labelled sample prices.
- Product detail, eligible subtotal, coin toggle/selector, price breakdown, discount, amount payable, coins used, and remaining balance.
- Handle insufficient coins, zero balance, cancellation, repeated confirmation, and already completed orders.
- Do not deduct coins at quotation time. Deduct once on confirmed SIMULATED order; cancellation leaves balance unchanged.
- Label checkout/order completion "Demo checkout — no real payment".
- No payment card form, payment provider, or misleading purchase-success claim.

Recognition / Future Slots:
- Finish a small cohort leaderboard and Student Spotlight preview using clearly labelled fictional demo students, not real student photos or invented real results.
- Current user ranking must not decrease when spending coins.
- Support feed item types educational, featured, and promo. Include one separately labelled preview of a relevant Arivihan feature promotion and a spotlight card, controlled by configuration. Neither counts toward learning challenges. Keep these secondary to the core demo.
- CTA on a promo opens a usable feature preview, not a dead button.
- Pro-lock and Coming Soon popups follow the reference. Default demo persona is Pro so the full challenge can be demonstrated. A presenter-controlled Plus persona can demonstrate locking and a simulated upgrade.
- Non-subscribed demo persona does not see Reels in bottom navigation; show an intentional unavailable state on direct access.
- Locked content cannot count toward the challenge.

5. MONEY CALCULATION

All calculations use integer coins and integer paise:
coinValuePaise = 20
capPaise = floor(eligibleSubtotalPaise * 10 / 100)
maxRedeemableCoins = min(balanceCoins, floor(capPaise / coinValuePaise))
coinsToUse = integer clamped between 0 and maxRedeemableCoins
discountPaise = coinsToUse * coinValuePaise
payablePaise = eligibleSubtotalPaise - discountPaise

Eligible subtotal excludes shipping/tax, if represented. Do not stack extra sample coupons. Format using INR currency display.

Required examples:
- 5 coins, INR 500 item -> INR 1 off, 5 coins used.
- 500 coins, INR 500 item -> INR 50 off, 250 coins used, 250 remain.
- 500 coins, INR 2000 item -> INR 100 off, 500 coins used.
- INR 99 item -> coin redemption cannot exceed INR 9.90; because coins are indivisible at INR 0.20, maximum is 49 coins = INR 9.80.

6. CONTENT AND MEDIA

Build two fully playable chapters: Units & Measurements and Laws of Motion, with five distinct short lessons and five correct, reviewed-for-consistency MCQs per chapter. Keep questions about what those lessons actually teach. Use simple meaningful numerical questions/diagrams with unambiguous answers.

Prefer supplied real educational clips and thumbnails when present. Do not treat the Figma walkthrough as lesson content, split the same unrelated stock video into five supposed lessons, or use unrelated personal uploads.

If no educational clips exist, create short local educational demo clips using available tools. If this is not possible, implement an explicitly labelled animated demo-lesson renderer with actual concept diagrams, captions, timeline, play/pause/seek and the same playback/coverage interface. It must teach the corresponding questions, not be a poster with a fake Play button. Keep a standard MP4 media-source adapter so replacing demo lessons later does not change the quiz flow.

Core presentation must work from bundled local assets without reliance on random remote videos/images/CDNs. No claim of full offline app support is required.

Keep all seed data in typed content files with stable IDs. Include a small content-replacement guide with video paths, chapter mapping, question IDs, options, explanations and reel links.

7. ARCHITECTURE: SUPABASE LATER, WITHOUT REBUILDING SCREENS

Components must call typed async service interfaces, not directly read/write localStorage or import answer keys.
Provide a LocalDemoAdapter as the complete default implementation. Add a clearly unimplemented Supabase adapter boundary that is NOT selected or imported at startup; no placeholder throw should break the demo.

Interfaces should cover:
- Session/profile
- Catalogue/chapter content
- Playback progress
- Start/resume quiz, submit answer, finish attempt, review attempt
- Wallet/ledger
- Quote redemption and confirm demo order
- Recognition
- Analytics

Use serializable DTOs, explicit loading/error states, stable request/idempotency IDs, and configurable mock latency/errors.
Keep answer keys inside the mock service, not question DTOs given to components. Clearly document that bundled mock answers and local storage are inspectable, and that production grading/rewards must be server-authoritative.

Use versioned persistent local storage or IndexedDB with migrations/reset handling. Persist only serializable state, not media elements/timers. Recover safely from malformed saved data. Avoid scaffolding a huge abstraction framework.

EXISTING SUPABASE CONTEXT (REFERENCE ONLY; DO NOT MUTATE):
The shared conversation describes these six tables and one view:
profiles(id, name, batch, created_at)
reels(id, title, video_url, subject, chapter, type, is_active, created_at)
questions(id, subject, chapter, question, options, correct_index, explanation)
reel_events(id, user_id, reel_id, event, watch_ms, created_at)
quiz_attempts(id, user_id, chapter, score, coins_awarded, created_at)
coin_ledger(id, user_id, amount, reason, ref_id, created_at)
leaderboard: historical view summing coin_ledger amounts per user.

That schema is historical context, not a verified current database snapshot or a complete integration contract. It lacks explicit challenge mappings, watch persistence, answer persistence, and redemption/order support. Do not force the frontend into its gaps or claim integration is done.
Document existing-to-needed mappings and proposed future additions in SUPABASE_HANDOFF.md, without executing SQL. The old net-ledger leaderboard must not be used as recognition rank, since spending should not lower rank.
Document RLS, private answer keys, authenticated RPC ownership checks, atomic grading/reward writes, unique reward constraints and server-priced redemption as later backend requirements. Do not recommend disabling RLS or putting tokens in URLs.

8. ANALYTICS PREPARATION

Create one analytics adapter with a locally persisted/exportable event log, stable event IDs, user/session IDs, UTC timestamps, chapter/reel/challenge/attempt IDs where relevant, content_type, source, and demo/presenter flags.

Cover actual actions:
reels_entry_clicked, reels_chapter_viewed, reels_video_started,
reels_watch_progress, reels_video_completed,
reels_quiz_offered, reels_quiz_started, reels_quiz_deferred,
reels_quiz_answer_submitted, reels_quiz_completed,
reels_reward_earned, reels_wallet_viewed,
reels_redemption_quoted, reels_redemption_confirmed.

Avoid frame-by-frame event floods and render-triggered duplicate completion/reward events. Do not log names, phone numbers, correct answer keys, or raw personal details.
Seeded presentation progress is explicitly marked, not represented as genuine viewing. Any internal numbers are demo data, not real D7 retention or evidence of traction.

9. PRESENTER TOOLS

Add a separate local presenter route, available only when demo tooling is explicitly enabled. No controls overlaid on the student's player.

Provide deterministic presets:
- Fresh student, zero coins/progress.
- Presentation-ready: 495 coins as a labelled demo seed ledger entry; first four reels of one chapter qualified; fifth ready to play.
- Failed attempt/revision scenario.
- Already rewarded challenge.
- Plus persona/pro-lock scenario.
- Empty content, coming soon, loading and retryable error examples.

Never silently change normal qualification rules for presenter speed.
Presets reset related attempts, ledger, orders and events consistently. Refresh must not reapply the 495 seed or mint coins.
Include Reset Demo, a revealable answer guide for presenters, and JSON event export.
Make zero real network mutations. Explain presenter mode is convenience, not a security boundary.

10. VISUAL AND ACCESSIBILITY QUALITY

Mobile-first at 360, 390 and 430px; respect safe-area insets and dynamic viewport height. No horizontal overflow. On desktop, use a centered mobile-width app stage on a subdued background rather than stretching the player into a dashboard.
Desktop may offer a discreet full-width/mobile preview toggle if useful.

Use consistent spacing, reusable cards/chips/buttons, proper icon assets, crisp typography and restrained animation. Use cyan as primary, gold for rewards, clear success/error feedback. No emoji-only navigation.
No nested scroll traps, clipped options, controls under browser chrome, or modals behind the player. Maintain accessible contrast, keyboard operation, visible focus, touch targets, modal focus trapping/return, and reduced-motion support.
Provide captions or readable demo-lesson text. Preserve aspect ratio and avoid covering diagrams with controls.

Every visible control must work, provide a meaningful state, or explicitly explain unavailability. Keep implementation details and Supabase status out of normal student screens.

11. ACCEPTANCE AND DELIVERY

Do not stop at scaffolding. Run the app and fix errors. Use available browser automation for the full interaction flow and screenshots. Run typecheck, build, lint if configured, and meaningful focused tests for:
- Five distinct videos in one chapter; mixed chapters/repeats/seeking/background time do not qualify incorrectly.
- Five questions map to exactly the watched challenge.
- 5/5 awards five coins, below 5/5 zero, successful retry earns only once.
- Refresh/resume/double-submit do not duplicate attempts/rewards/orders.
- Wallet persists and equals ledger sum.
- All four money examples, insufficient funds, cancellation and no early debit.
- Spending does not reduce recognition rank.
- Presenter 495 -> reward 500 -> redeem 250 -> remaining 250 flow.
- Normal mobile navigation, overlays, actual media behaviour and direct routes.

Do not add repetitive tests for trivial visual styling.
If a verification tool or media capability is unavailable, report the exact limitation; do not claim checks were run.

Deliver:
- Complete runnable frontend and bundled demo content.
- README with install/run/build commands and demo walkthrough.
- SUPABASE_HANDOFF.md with typed service contracts, historical table mappings, backend gaps and security responsibilities.
- CONTENT_GUIDE.md and .env.example with non-secret configuration only; demo runs without env values.
- A concise list of actual remaining Supabase integration tasks.
- Final report: what works, verification performed, limitations and changed files.

Use sensible defaults and proceed without routine questions. The finished result must let me demonstrate the entire journey immediately, with Supabase connection work remaining as a separate, documented step.
