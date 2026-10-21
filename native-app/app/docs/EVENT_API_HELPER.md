# `EventApiHelper.sendActionEvent` — Client Event Contract

Documents [`EventApiHelper.java`](../src/main/java/arivihan/technologies/doubtbuzzter2/volley/EventApiHelper.java)
— every place it's called from, the exact payload each call sends, and what backend row it
produces. Cross-referenced against the backend storage spec `onboarding-analytics-spec-v7`
(ClickHouse table `onboarding_events`).

Enums involved:
- [`ActionEventName`](../src/main/java/arivihan/technologies/doubtbuzzter2/enums/event/ActionEventName.java)
- [`ActionEventScreen`](../src/main/java/arivihan/technologies/doubtbuzzter2/enums/event/ActionEventScreen.java)
- [`ActionEventDirection`](../src/main/java/arivihan/technologies/doubtbuzzter2/enums/event/ActionEventDirection.java) — every `*_SCREEN_VIEWED` call uses it for the `data` argument

Reference-style links (`[ref][id]`) don't reliably resolve to a clickable file jump in every
Markdown viewer, so every link in this doc is a plain inline link — click it directly. The
**quick-reference table** below keeps its Source column as plain `File.java:line` text (no link,
so the table stays narrow); the actual clickable link for each of those sites is in section 2
("Call sites, by event") and in the section 7 index at the bottom.

---

## 0. Quick reference — every event, screen and value in one table

| Event (`ActionEventName`) | Screen (`ActionEventScreen`) | `value` sent | Source (see §2/§7 for links) |
|---|---|---|---|
| `APP_OPENED` | *(none)* | `null` | `LoginCourseSelectionFragment.java:108` |
| `SCREEN_VIEWED` | — | — | **dead — never called** |
| `EXAM_SELECTED` | `EXAM_SCREEN` | pending exam/board code | `LogInWIthMobileNumberFragment.java:412` |
| `AUTH_INITIATED` | `PHONE_SCREEN` | `"MANUAL"` | `LogInWIthMobileNumberFragment.java:441` |
| `AUTH_INITIATED` | `PHONE_SCREEN` | `"MANUAL"` | `LogInWIthMobileNumberFragment.java:516` (Firebase path) |
| `AUTH_INITIATED` | `PHONE_SCREEN` | `"ANDROID_HINT"` | `UserAuthenticationActivity.java:324` |
| `AUTH_INITIATED` | `PHONE_SCREEN` | `"TRUECALLER"` | `UserAuthenticationActivity.java:1010` |
| `MEDIUM_SCREEN_VIEWED` | `MEDIUM_SCREEN` | `FORWARD`/`BACKWARD` | `MediumFragment.java:197` |
| `MEDIUM_SCREEN_VIEWED` | `MEDIUM_SCREEN` | `FORWARD`/`BACKWARD` | `CompetitiveMediumFragment.java:205` |
| `MEDIUM_SELECTED` | `MEDIUM_SCREEN` | `"hi"`/`"en"` | `OnboardingActivityNew.java:1200` |
| `CLASS_SCREEN_VIEWED` | `CLASS_SCREEN` | `FORWARD`/`BACKWARD` | `GradesFragment.java:183` |
| `CLASS_SELECTED` | `CLASS_SCREEN` | `"CLASS_10"`/`"CLASS_12"` | `OnboardingActivityNew.java:655` |
| `STREAM_SCREEN_VIEWED` | `STREAM_SCREEN` | `FORWARD`/`BACKWARD` | `StreamsFragment.java:302` |
| `STREAM_SELECTED` | `STREAM_SCREEN` | `"PCM"`/`"PCB"` | `OnboardingActivityNew.java:667` |
| `NAME_SCREEN_VIEWED` | `NAME_SCREEN` | `FORWARD`/`BACKWARD` | `NameFragment.java:165` |
| `NAME_SUBMITTED` | `NAME_SCREEN` | ⚠️ user's entered name, verbatim | `OnboardingActivityNew.java:643` |
| `"board_screen_view"` *(no enum)* | `"Board Screen"` *(no enum)* | `null` | `BoardFragment.java:134` |
| `OTP_GENERATED` | — | `SUCCESS`/failure reason | backend-only |
| `AUTH_VERIFIED` | — | `OTP`/`TRUECALLER` | backend-only |
| `AUTH_FAILED` | — | reason | backend-only |
| `ONBOARDING_COMPLETED` | — | new `userId` | backend-only |
| `ONBOARDING_FAILED` | — | `VALIDATION_FAILED`/`INVALID_LANGUAGE`/`USER_ALREADY_EXISTS`/`NO_ACTIVE_BATCH` | backend-only |
| `LOGIN_SUCCESS` | — | — | backend-only |
| `DELETE_ACCOUNT` | — | — | backend-only |

`ActionEventScreen.OTP_SCREEN` is declared but never used, client or backend-mapped.

---

## 1. What the method actually does

```java
EventApiHelper.sendActionEvent(event, examSelected, phoneNumber, data, screenName [, callback]);
```

| Java param | Sent as (JSON key) | Backend column (`onboarding_events`) |
|---|---|---|
| `event` | `event` | `event_name` |
| `examSelected` | **ignored** — see below | `exam_selected` |
| `phoneNumber` | `phoneNumber` | `phone_number` |
| `data` | `value` (comment `// was "data"` marks the rename) | `value` |
| `screenName` | `screenName` | `screen_name` |
| — | `eventTime` = `System.currentTimeMillis()` | `event_time` |
| — | `source` = `"CLIENT"` (hardcoded) | `source` |
| — | `secretKey` = `BuildConfig.EVENT_SECRET_KEY` | (auth only, not stored) |
| — | `appVersion` = `BuildConfig.VERSION_NAME` | `app_version` |
| — | `osVersion` = `Build.VERSION.RELEASE` | `os_version` |
| — | `platform` = `"ANDROID"` (hardcoded) | `platform` |

POSTs to `UrlRoutes.ACTION_EVENT`. Fire-and-forget: the 5-arg overload calls the 6-arg one with
`callback = null`. No call site in the app passes a callback — nothing in the client ever reacts
to event-send success/failure.

**Bug/gotcha**: the `examSelected` argument every caller passes in (always `sel.getId()`, an
onboarding-selection *id*) is never actually sent. Inside the method it's overwritten:

```java
OnBoardingSelectionModel onboardingModel = PreferenceMethods.getSelectedOnboardingModel();
String examCode = (onboardingModel != null) ? onboardingModel.getExamCode() : null;
body.put("examSelected", examCode == null ? JSONObject.NULL : examCode);
```

So `exam_selected` on every row is whatever `PreferenceMethods.getSelectedOnboardingModel()`
returns *at send time*, regardless of what the caller passed. Harmless while that preference is
correctly populated by the time each event fires, but the parameter itself is dead — worth
removing or wiring up for real in a follow-up.

`event` and `screenName` are plain `String` — nothing enforces that callers pass an enum
`.getValue()` rather than a raw literal (see section 5, "Inconsistencies / dead code").

---

## 2. Call sites, by event

"Guard" is the condition that must hold for the call to execute at all. Each source link below is
a plain inline link — click it to jump to the file.

### `EXAM_SELECTED` → screen `EXAM_SCREEN`

- **Source**: [`LogInWIthMobileNumberFragment.java:412`](../src/main/java/arivihan/technologies/doubtbuzzter2/fragments/authentication/LogInWIthMobileNumberFragment.java#L412), method `fireExamSelectedIfPending`
- **`value` sent**: the pending exam/board code
- **Guard**: not fired when the user taps an exam card. `LoginCourseSelectionFragment.onExamSelected()`
  just persists the tap to `PreferenceKeys.EXAM_SELECTED_PENDING` and navigates to the phone-number
  screen. The event only actually fires once OTP dispatch succeeds —
  `fireExamSelectedIfPending()` is called from both the custom-backend OTP success handler and the
  Firebase `onCodeSent` callback, and only sends if a pending code is present (then clears it).

### `AUTH_INITIATED` → screen `PHONE_SCREEN`

Fired once per phone-verification *channel*, right after that channel successfully starts
OTP/verification. `value` = channel tag.

- `"MANUAL"` — [`LogInWIthMobileNumberFragment.java:441`](../src/main/java/arivihan/technologies/doubtbuzzter2/fragments/authentication/LogInWIthMobileNumberFragment.java#L441) — success response of the custom "Get OTP" backend call, `if (!isHintInitiated)`
- `"MANUAL"` (again) — [`LogInWIthMobileNumberFragment.java:516`](../src/main/java/arivihan/technologies/doubtbuzzter2/fragments/authentication/LogInWIthMobileNumberFragment.java#L516) — Firebase `PhoneAuthProvider.onCodeSent(...)`, `if (!isHintInitiated)` — same `"MANUAL"` literal reused here, looks copy-pasted even though this is the Firebase path, not the manual-backend path
- `"ANDROID_HINT"` — [`UserAuthenticationActivity.java:324`](../src/main/java/arivihan/technologies/doubtbuzzter2/activity/UserAuthenticationActivity.java#L324) — Android Sign-In phone-number-hint bottom sheet returns `RESULT_OK` with a number ≥10 digits
- `"TRUECALLER"` — [`UserAuthenticationActivity.java:1010`](../src/main/java/arivihan/technologies/doubtbuzzter2/activity/UserAuthenticationActivity.java#L1010) — Truecaller SDK returns a non-empty phone number

Both `isHintInitiated`-guarded sites exist so a manually-typed number that gets auto-filled by the
OS hint doesn't double-fire `AUTH_INITIATED`.

### `MEDIUM_SCREEN_VIEWED` → screen `MEDIUM_SCREEN`

- **Sources**: [`MediumFragment.java:197`](../src/main/java/arivihan/technologies/doubtbuzzter2/fragments/MediumFragment.java#L197) (standard flow), [`CompetitiveMediumFragment.java:205`](../src/main/java/arivihan/technologies/doubtbuzzter2/fragments/competitive/CompetitiveMediumFragment.java#L205) (NEET flow — near-duplicate fragment)
- **`value` sent**: `navigationDirection` — `FORWARD`/`BACKWARD`
- **Guard**: unconditional, fires from `startAnimation()` every time `onCreateView()` runs for that fragment

### `MEDIUM_SELECTED` → screen `MEDIUM_SCREEN`

- **Source**: [`OnboardingActivityNew.java:1200`](../src/main/java/arivihan/technologies/doubtbuzzter2/activity/OnboardingActivityNew.java#L1200), `onMediumClick(...)` callback shared by both medium fragments
- **`value` sent**: `my_lan` — `"hi"` or `"en"`
- **Guard**: fires every time the user taps a medium/language option, unconditionally, right before `navigateNext("MEDIUM")`

### `CLASS_SCREEN_VIEWED` → screen `CLASS_SCREEN`

- **Source**: [`GradesFragment.java:183`](../src/main/java/arivihan/technologies/doubtbuzzter2/fragments/GradesFragment.java#L183), `startAnimation()` from `onCreateView()`
- **`value` sent**: `navigationDirection`
- **Guard**: unconditional on view creation

### `CLASS_SELECTED` → screen `CLASS_SCREEN`

- **Source**: [`OnboardingActivityNew.java:655`](../src/main/java/arivihan/technologies/doubtbuzzter2/activity/OnboardingActivityNew.java#L655), inside `navigateNext(...)`'s 500ms-delayed step-advance
- **`value` sent**: `gradeCodeForEvent` — `GradeModel.code` (falls back to `gradeCode`), e.g. `"CLASS_10"`/`"CLASS_12"`
- **Guard**: only when the step being left is `GRADE`, and the one-shot flag `classSelectedEventFired`
  is still false (flag resets whenever the GRADE step is (re-)entered, so re-visiting fires it
  again, but multiple "Next" taps within one visit don't double-fire)
- ⚠️ **Spec mismatch**: the backend spec's example value for `CLASS_SELECTED` is plain `10`/`12`.
  The client actually sends the prefixed grade code `CLASS_10`/`CLASS_12`. Anyone writing
  ClickHouse queries filtering/grouping on this `value` needs the prefixed form, not the spec's
  bare-number example.

### `STREAM_SCREEN_VIEWED` → screen `STREAM_SCREEN`

- **Source**: [`StreamsFragment.java:302`](../src/main/java/arivihan/technologies/doubtbuzzter2/fragments/StreamsFragment.java#L302), `startAnimation()` from `onCreateView()`/setup path
- **`value` sent**: `navigationDirection`
- **Guard**: unconditional on view creation

### `STREAM_SELECTED` → screen `STREAM_SCREEN`

- **Source**: [`OnboardingActivityNew.java:667`](../src/main/java/arivihan/technologies/doubtbuzzter2/activity/OnboardingActivityNew.java#L667), same `navigateNext(...)` block as `CLASS_SELECTED`
- **`value` sent**: `sel.getStreamCode()` (e.g. `"PCM"`/`"PCB"`)
- **Guard**: step being left is `STREAM`, one-shot flag `streamSelectedEventFired`

### `NAME_SCREEN_VIEWED` → screen `NAME_SCREEN`

- **Source**: [`NameFragment.java:165`](../src/main/java/arivihan/technologies/doubtbuzzter2/fragments/NameFragment.java#L165), directly in `onCreateView()`
- **`value` sent**: `navigationDirection`
- **Guard**: unconditional on view creation
- **Dead code**: a commented-out, per-keystroke call (`"onboarding_name_input"`, string literals
  not enums) inside the `TextWatcher` — disabled, not currently sending anything

### `NAME_SUBMITTED` → screen `NAME_SCREEN`

- **Source**: [`OnboardingActivityNew.java:643`](../src/main/java/arivihan/technologies/doubtbuzzter2/activity/OnboardingActivityNew.java#L643), same `navigateNext(...)` block
- **`value` sent**: ⚠️ the user's entered name, verbatim (`name != null ? name : ""`)
- **Guard**: step being left is `NAME` — no one-shot flag, unlike `CLASS_SELECTED`/`STREAM_SELECTED`;
  navigating back to the Name step and forward again re-fires it
- ⚠️ **Spec mismatch**: the backend spec documents `NAME_SUBMITTED`'s `value` column as `—`
  (unused). The client instead writes the raw name string into `value`, which lands in the
  `onboarding_events` ClickHouse table. Flag this to whoever owns the backend spec — it's a PII
  field going into analytics storage undocumented.

### `APP_OPENED` → no screen (`screenName` passed as `null`)

- **Source**: [`LoginCourseSelectionFragment.java:108`](../src/main/java/arivihan/technologies/doubtbuzzter2/fragments/authentication/LoginCourseSelectionFragment.java#L108), `markAppOpenedIfFirstInstall()` from `onCreateView()`
- **`value` sent**: `null`
- **Guard**: fires exactly once per install, gated by a persistent (do-not-delete) SharedPreferences
  flag `PreferenceKeys.APP_OPENED_EVENT_SENT` — not "app process started," but "user reached the
  exam-selection screen for the first time ever"
- **Note**: not part of the backend spec's `onboarding_events` catalog at all — this event name
  isn't in the spec's event table, so its downstream handling/storage is undocumented from the
  client side.

### Non-enum call: `BoardFragment`

- **Source**: [`BoardFragment.java:134`](../src/main/java/arivihan/technologies/doubtbuzzter2/fragments/BoardFragment.java#L134), `startAnimation()` from `onCreateView()`
- **Sent**: `event = "board_screen_view"` (raw string), `examSelected = null`, `phoneNumber = null`,
  `value = null`, `screenName = "Board Screen"` (raw string, not `BOARD_SCREEN`)
- **Guard**: unconditional on view creation
- ⚠️ Bypasses both enums entirely. Neither `ActionEventName` nor `ActionEventScreen` has a Board
  constant, and this event name doesn't appear anywhere in the backend spec's event catalog — an
  orphaned/legacy event that predates the enum-based contract. `examSelected`/`phoneNumber` being
  `null` also means this row can never be attributed to a user in the funnel.

---

## 3. Events with no client call site (backend-sourced)

The spec's event catalog also lists `OTP_GENERATED`, `AUTH_VERIFIED`, `AUTH_FAILED`,
`ONBOARDING_COMPLETED`, `ONBOARDING_FAILED`, `LOGIN_SUCCESS`, `DELETE_ACCOUNT` — all with
`source = backend`. None of these appear in any `EventApiHelper.sendActionEvent` call anywhere in
this codebase, which is expected: the backend writes those rows itself. `EventApiHelper` only ever
sends `source = "CLIENT"` rows.

---

## 4. Dead enum values

| Enum value | Status |
|---|---|
| `ActionEventName.SCREEN_VIEWED` | Never referenced by any call site. Every screen-view event uses its own specific name (`MEDIUM_SCREEN_VIEWED`, `CLASS_SCREEN_VIEWED`, etc.) instead of this generic one. |
| `ActionEventScreen.OTP_SCREEN` | Never referenced. There is no client-fired event tied to the OTP screen — `AUTH_VERIFIED`/`AUTH_FAILED` (which would logically use it) are backend-sourced. |

---

## 5. Inconsistencies / dead code — summary

1. **`examSelected` parameter is dead on arrival** — every caller's value is discarded and replaced inside `sendActionEvent` by a fresh read of `PreferenceMethods.getSelectedOnboardingModel()`.
2. **`NAME_SUBMITTED` sends the user's real name as `value`**, contradicting the backend spec (which documents that column as unused for this event) — a PII item worth flagging to backend/data owners.
3. **`BoardFragment` bypasses the enum contract entirely**, using raw string literals for both event and screen name, with no corresponding enum constants and no entry in the backend spec's event catalog.
4. **Commented-out per-keystroke event** in `NameFragment` (`"onboarding_name_input"`) — dead code using string literals.
5. **Two dead enum values**: `ActionEventName.SCREEN_VIEWED`, `ActionEventScreen.OTP_SCREEN`.
6. **`AUTH_INITIATED`'s `"MANUAL"` literal is reused on the Firebase `onCodeSent` path** even though that's a different channel from the custom-backend "MANUAL" path above it — likely copy-paste; worth confirming with whoever owns that fragment whether it should be a distinct value.
7. **`NAME_SUBMITTED` has no one-shot guard** (unlike `CLASS_SELECTED`/`STREAM_SELECTED`), so navigating back to the Name step and forward again re-fires it — inconsistent with the sibling selection events in the same method.
8. **`CLASS_SELECTED`'s value format doesn't match the spec's example** — client sends `CLASS_10`/`CLASS_12`, spec example shows bare `10`/`12`.
9. Every call site uses the 5-arg fire-and-forget overload; the 6-arg `EventCallback` overload is never used anywhere, so send failures are silently swallowed (only logged via `Log.e`).

---

## 6. Funnel reference (from backend spec `onboarding-analytics-spec-v7`)

For context, the flows the events above are expected to reconstruct server-side:

- **New user, board exam**: `EXAM_SELECTED → AUTH_INITIATED → OTP_GENERATED → AUTH_VERIFIED → MEDIUM_SCREEN_VIEWED → MEDIUM_SELECTED → CLASS_SCREEN_VIEWED → CLASS_SELECTED → STREAM_SCREEN_VIEWED → STREAM_SELECTED → ONBOARDING_COMPLETED`
- **New user, NEET**: same through `MEDIUM_SELECTED`, then `NAME_SCREEN_VIEWED → NAME_SUBMITTED → ONBOARDING_COMPLETED` (no class/stream step)
- **Returning user**: `AUTH_INITIATED → OTP_GENERATED → AUTH_VERIFIED → LOGIN_SUCCESS`
- **Truecaller**: `AUTH_INITIATED → AUTH_VERIFIED` (no `OTP_GENERATED` — Truecaller never sends an OTP); `AUTH_FAILED` still applies if Truecaller itself fails

Grouping key server-side is `phone_number`. Events before the phone screen (`EXAM_SELECTED`) carry
an empty `phone_number` and can't be attributed to an individual until `AUTH_INITIATED` — this
matches the client behavior above (`EXAM_SELECTED` is deferred until the phone number is known,
right after OTP dispatch, rather than fired at tap time).

---

## 7. Source file index

Flat, clickable list of every file cited above (same links as §2, gathered here for convenience).

- [`EventApiHelper.java`](../src/main/java/arivihan/technologies/doubtbuzzter2/volley/EventApiHelper.java)
- [`ActionEventName.java`](../src/main/java/arivihan/technologies/doubtbuzzter2/enums/event/ActionEventName.java)
- [`ActionEventScreen.java`](../src/main/java/arivihan/technologies/doubtbuzzter2/enums/event/ActionEventScreen.java)
- [`ActionEventDirection.java`](../src/main/java/arivihan/technologies/doubtbuzzter2/enums/event/ActionEventDirection.java)
- [`LoginCourseSelectionFragment.java:108`](../src/main/java/arivihan/technologies/doubtbuzzter2/fragments/authentication/LoginCourseSelectionFragment.java#L108) — `APP_OPENED`
- [`LogInWIthMobileNumberFragment.java:412`](../src/main/java/arivihan/technologies/doubtbuzzter2/fragments/authentication/LogInWIthMobileNumberFragment.java#L412) — `EXAM_SELECTED`
- [`LogInWIthMobileNumberFragment.java:441`](../src/main/java/arivihan/technologies/doubtbuzzter2/fragments/authentication/LogInWIthMobileNumberFragment.java#L441) — `AUTH_INITIATED` (MANUAL)
- [`LogInWIthMobileNumberFragment.java:516`](../src/main/java/arivihan/technologies/doubtbuzzter2/fragments/authentication/LogInWIthMobileNumberFragment.java#L516) — `AUTH_INITIATED` (Firebase)
- [`UserAuthenticationActivity.java:324`](../src/main/java/arivihan/technologies/doubtbuzzter2/activity/UserAuthenticationActivity.java#L324) — `AUTH_INITIATED` (ANDROID_HINT)
- [`UserAuthenticationActivity.java:1010`](../src/main/java/arivihan/technologies/doubtbuzzter2/activity/UserAuthenticationActivity.java#L1010) — `AUTH_INITIATED` (TRUECALLER)
- [`MediumFragment.java:197`](../src/main/java/arivihan/technologies/doubtbuzzter2/fragments/MediumFragment.java#L197) — `MEDIUM_SCREEN_VIEWED`
- [`CompetitiveMediumFragment.java:205`](../src/main/java/arivihan/technologies/doubtbuzzter2/fragments/competitive/CompetitiveMediumFragment.java#L205) — `MEDIUM_SCREEN_VIEWED` (NEET)
- [`OnboardingActivityNew.java:1200`](../src/main/java/arivihan/technologies/doubtbuzzter2/activity/OnboardingActivityNew.java#L1200) — `MEDIUM_SELECTED`
- [`GradesFragment.java:183`](../src/main/java/arivihan/technologies/doubtbuzzter2/fragments/GradesFragment.java#L183) — `CLASS_SCREEN_VIEWED`
- [`OnboardingActivityNew.java:655`](../src/main/java/arivihan/technologies/doubtbuzzter2/activity/OnboardingActivityNew.java#L655) — `CLASS_SELECTED`
- [`StreamsFragment.java:302`](../src/main/java/arivihan/technologies/doubtbuzzter2/fragments/StreamsFragment.java#L302) — `STREAM_SCREEN_VIEWED`
- [`OnboardingActivityNew.java:667`](../src/main/java/arivihan/technologies/doubtbuzzter2/activity/OnboardingActivityNew.java#L667) — `STREAM_SELECTED`
- [`NameFragment.java:165`](../src/main/java/arivihan/technologies/doubtbuzzter2/fragments/NameFragment.java#L165) — `NAME_SCREEN_VIEWED`
- [`OnboardingActivityNew.java:643`](../src/main/java/arivihan/technologies/doubtbuzzter2/activity/OnboardingActivityNew.java#L643) — `NAME_SUBMITTED`
- [`BoardFragment.java:134`](../src/main/java/arivihan/technologies/doubtbuzzter2/fragments/BoardFragment.java#L134) — `"board_screen_view"` (non-enum)
