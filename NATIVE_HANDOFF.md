> Native development is paused at the user?s request in favor of the web demo. Demo APK compilation passed and the startup registrar issue was fixed; full native device acceptance did not pass. The automatic six-digit OTP path still bypasses the demo handler, and requires correction before native reuse. No native end-to-end success is claimed. Use `npm run live` for the active web implementation.

# Native Arivihan Reels demo

**Revised experience:** [DISCOVERY_HANDOFF.md](DISCOVERY_HANDOFF.md) is the current Reels specification and verification handoff. Feed/Explore and three/four-video watched batches supersede the earlier chapter-first discovery described by the original video.

Current status: implementation is in the supplied application's Android framework. **APK compilation and the fresh native device journey are still being verified.** The earlier standalone web verification is backend/reference evidence, not proof that the native journey has passed.

## Foundation and isolation

The source at `C:\Users\lenovo\OneDrive\Desktop\app\Arivihan` was copied into `native-app/`, excluding git metadata, production secret properties, Firebase production configuration, signing keys and build outputs. The original checkout is untouched. No AGENTS.md was found in the supplied repository or inspected ancestor locations.

The app uses Java/Kotlin, Android XML views, Material components, some Compose, Volley/OkHttp, Room and Hilt. Its original authentication/onboarding fragments, validation, selection configuration, board/competitive home layouts, fonts and bottom-navigation resources are retained. Demo bindings connect those views to isolated services. Unavailable app sections explain that their backend is unavailable instead of sending production requests.

The supplied 208-second video informed the original Reels visual reference. The revised 19 October brief overrides its chapter-first discovery where they conflict. Representative frames are in `reference-frames/latest/`; the analytics presentation is not copied.

Only the `demoDebug` variant is enabled by default, with application ID `arivihan.technologies.doubtbuzzter2.reelsdemo`. It has separate private storage and does not replace the real app. Original production variants are disabled in this workspace copy. Production SDK initialization/telemetry is disabled, Volley/OkHttp reject other backend hosts, and the demo network-security policy trusts only the explicitly supplied Supabase host. Bundled private Slack defaults were removed from the copy; no production secrets were copied into demo configuration.

## Identity and student selections

The original phone/OTP screens remain. OTP **123456** is explicitly simulated; no SMS is sent and the phone number is not verified. Successful entry establishes one persistent, real Supabase anonymous Auth session. Its UUID is reused by the application profile, Reels, attempts, progress and wallet. No client-selected user ID acts as authentication. An expired or failed session is not silently replaced with a different identity.

Original exam/board, language, class and stream choices remain in the app's preferences; a compact academic summary and language are stored in the owned Supabase profile. Discovery filters the supplied Class 11/12 Physics clips using those selections. Hindi/Hinglish narration and English scientific labels are identified. Unsupported selections show an empty state rather than mislabelled content.

The demo uses the backend's MVP Pro access, clearly separate from a real paid subscription. Server entitlement checks remain authoritative; locked profiles show the lock flow. Simulated checkout never activates subscriptions.

## Reels and backend operations

`reelsdemo/NativeReelsService.java` adapts the existing Volley queue to the deployed Supabase Auth and `ar_reels_*` RPCs. It uses only the public project URL and publishable key. Auth sessions are in application-private preferences and excluded from Android backup. Analytics queues retain UUIDs for deduplicated retries.

Discovery uses three or four distinct lessons per persisted server cycle, across topics, requiring 80% unique visible active timeline coverage. Seeks, background time and replay overlap do not add unique coverage. The server freezes five mapped questions covering every qualifying video. Historical five-video chapter records remain available to the legacy integration. Client playback reports cannot prove attention or eliminate determined abuse.

Questions and submitted-answer feedback come from the backend. The APK contains no Supabase answer-key seed or grading logic. The backend calculates the score, pays exactly five coins only for 5/5, and enforces one reward per user/challenge/content version. Practice retries remain available. Wallet balance is the ledger sum; recognition uses earned learning activity rather than remaining balance.

Quotes use trusted backend prices, integer paise and coins: one coin = 20 paise, five coins = INR 1, maximum redemption 10%. Quoting/cancellation do not spend. Confirmation uses persisted request IDs and the existing atomic order/debit function. All checkout is marked simulated.

The newly supplied `videos/` folder contains ten unique educational MP4s and one duplicate. Discovery streams those actual local files through the loopback development server and adb reverse; small source-frame posters are bundled. No media was uploaded to Supabase/AWS. Generated concept-summary notes are real files opened through a scoped FileProvider. Offline video download is explicitly unavailable. Full audio/transcript review remains unverified.

## Configuration and launch

`native-app/demo.properties` contains only the supplied project URL and browser-safe publishable key; it is ignored by git. `demo.properties.example` contains placeholders. `secrete.properties.example` has disabled demo placeholders for the legacy app's build fields. No database password, service-role key or management token belongs in these APK settings.

JDK 17, Android SDK 36 and Build-Tools 35 are required. The missing official Gradle 8.13 wrapper JAR was restored and checksum verified. The supplied missing baselineprofile module is optional in this copy. Legacy AARs use the supplied valid local files. Oversized configuration defaults are preserved in a JSON asset and loaded into the same Remote Config interface, avoiding Android's resource-string limit.

From the workspace:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/native-demo.ps1 -Action Build
powershell -ExecutionPolicy Bypass -File scripts/native-demo.ps1 -Action Run -Serial emulator-5556
```

Alternatively, open `native-app/` in Android Studio, choose JDK 17 and the `demoDebug` variant. This build is still under verification; do not treat these commands as a passing build claim yet.

Presenter reset is a separate demo-only activity, absent from student navigation:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/native-demo.ps1 -Action Presenter -Serial emulator-5556
```

Reset clears only this demo installation's local selections/session/notes. It does not delete server data or alter the real app. No presenter balance or progress is inserted into normal accounts. Native demo profiles are marked TEST.

## Verification status

- Existing authenticated MCP was reused, reauthenticated and verified against the intended project. Actual schema/functions/policies/storage were inspected. Revised discovery migrations 004–007 are deployed; existing chapter schema/RPCs remain intact.
- Java syntax parsing passed for 1,091 native files. The full Android build passed Java/Kotlin compilation for its current snapshot and is generating DEX; final compilation of subsequent UI refinements and APK packaging remain pending.
- The initial legacy cropper transform was repaired using the supplied local binary. No native compilation error is currently reported.
- The installed emulator image lacked its kernel. A separate official API 34 emulator/image was provisioned under TEMP and booted successfully, without changing Hyper-V or existing AVDs. It is temporarily stopped to free build memory.
- Native onboarding, playback, quiz, wallet, restoration and simulated-order device checks remain pending until the APK/device are ready. No native end-to-end success is claimed yet.

Earlier backend/web results remain in `SUPABASE_VERIFICATION.md`; they must not be conflated with native acceptance.
