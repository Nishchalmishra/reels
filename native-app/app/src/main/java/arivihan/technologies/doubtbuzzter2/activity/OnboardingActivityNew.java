package arivihan.technologies.doubtbuzzter2.activity;
import arivihan.technologies.doubtbuzzter2.reelsdemo.*;
import arivihan.technologies.doubtbuzzter2.BuildConfig;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_CHANGE_USER_LANGUAGE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_REGISTER;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import arivihan.technologies.doubtbuzzter2.enums.onboarding.OnboardingScreenName;
import arivihan.technologies.doubtbuzzter2.helper.onboarding.OnboardingStateApiHelper;
import arivihan.technologies.doubtbuzzter2.model.onboarding.UserOnboardingStateDto;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.onboarding.NeetClassSelectionActivity;
import arivihan.technologies.doubtbuzzter2.common.BundleParamConstants;
import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.constants.UrlRoutes;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.event.ActionEventDirection;
import arivihan.technologies.doubtbuzzter2.enums.event.ActionEventName;
import arivihan.technologies.doubtbuzzter2.enums.event.ActionEventScreen;
import arivihan.technologies.doubtbuzzter2.enums.onboarding.OnboardingStep;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceMethods;
import arivihan.technologies.doubtbuzzter2.fragments.BoardFragment;
import arivihan.technologies.doubtbuzzter2.fragments.CoachingFragment;
import arivihan.technologies.doubtbuzzter2.fragments.GradesFragment;
import arivihan.technologies.doubtbuzzter2.fragments.MediumFragment;
import arivihan.technologies.doubtbuzzter2.fragments.NameFragment;
import arivihan.technologies.doubtbuzzter2.fragments.StreamsFragment;
import arivihan.technologies.doubtbuzzter2.fragments.competitive.CompetitiveMediumFragment;
import arivihan.technologies.doubtbuzzter2.helper.BranchAnalyticsHelper;
import arivihan.technologies.doubtbuzzter2.helper.DeviceInfoUtil;
import arivihan.technologies.doubtbuzzter2.helper.refer.ReferralLinkHandler;
import arivihan.technologies.doubtbuzzter2.model.AddSubjectAboutFragmentModel;
import arivihan.technologies.doubtbuzzter2.model.ArivihanLanguageEnum;
import arivihan.technologies.doubtbuzzter2.model.NameIdModel;
import arivihan.technologies.doubtbuzzter2.model.OnBoardingSelectionModel;
import arivihan.technologies.doubtbuzzter2.model.batch.grade.GradeModel;
import arivihan.technologies.doubtbuzzter2.model.batch.stream.StreamModel;
import arivihan.technologies.doubtbuzzter2.model.batch.user.UserDetailsResponse;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.model.user.UserRegisterRequest;
import arivihan.technologies.doubtbuzzter2.utils.AudioManager;
import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper;
import arivihan.technologies.doubtbuzzter2.volley.EventApiHelper;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;

/**
 * OnboardingActivityNew — fully refactored.
 * <p>
 * Key fixes vs. the previous version
 * ────────────────────────────────────
 * 1. SINGLE SOURCE OF TRUTH for step count.
 * `totalSteps` field is REMOVED. Every place that used it now calls
 * `activeSteps.size()` directly, so the list and the count can never
 * drift apart when STREAM is added/removed at runtime.
 * <p>
 * 2. NO PREMATURE saveData() on GRADE screen.
 * Fixed by deriving the bound from activeSteps.size() at the moment
 * navigateNext fires (captured into a local so the 500 ms lambda sees
 * the right value even if the list mutates later).
 * <p>
 * 3. GUARD against double-save.
 * isRegisterRequestInProgress flag is kept; also added an activity-
 * lifecycle guard so a pending postDelayed cannot fire after finish().
 * <p>
 * 4. STREAM restoration on back-navigation is now symmetric with removal.
 * navigateBack() restores STREAM before decrementing stepNumber.
 * <p>
 * 5. switchFragment(GRADE) no longer mutates activeSteps when gradeCode
 * comes from a restored session unless the grade was explicitly set.
 * <p>
 * 6. navigateNext() captures stepNumber and activeSteps.size() into final
 * locals before the postDelayed lambda, avoiding a data-race.
 * <p>
 * 7. NEET flow uses CompetitiveMediumFragment; all other flows use
 * MediumFragment. Routing is centralised in isNeetFlow().
 */
public class OnboardingActivityNew extends BaseActivity implements
        EdgeToEdgeHelper.SelfManagedInsets,
        NameFragment.NameClickListener,
        MediumFragment.MediumClickListener,
        CompetitiveMediumFragment.MediumClickListener,
        BoardFragment.BoardClickListener,
        CoachingFragment.CoachingClickListener,
        GradesFragment.GradeClickListener,
        StreamsFragment.SubjectGroupClickListener {

    // ─── constants ────────────────────────────────────────────────────
    private static final String TAG = OnboardingActivityNew.class.getSimpleName();
    private static final int SWIPE_THRESHOLD = 100;
    public static final String EXTRA_FRESH_LOGIN = "fresh_login";

    // ─── views ────────────────────────────────────────────────────────
    private LinearLayout back_button_layout;
    private LinearLayout bottom_layout;
    private LinearLayout toolbar;
    private LinearProgressIndicator progress;
    private CardView next_card;
    private CardView previous_card;
    private ShimmerFrameLayout shimmerFrameLayout;
    private FrameLayout fragment_container_view;
    private CardView cv_volume;
    private ImageView iv_volume;
    private ProgressDialog loading;

    // ─── navigation state ─────────────────────────────────────────────
    /**
     * SINGLE SOURCE OF TRUTH: always derive step count from activeSteps.size().
     * The old `totalSteps` field has been removed to prevent drift.
     */
    private final ArrayList<OnboardingStep> activeSteps = new ArrayList<>();
    private final ArrayList<String> audioUrlsEn = new ArrayList<>();
    private final ArrayList<String> audioUrlsHi = new ArrayList<>();
    private final ArrayList<Boolean> audioRepeat = new ArrayList<>();

    private int stepNumber = 1;
    private boolean nextIsEnabled = false;
    private boolean isRegisterRequestInProgress = false;
    /**
     * True when the selected grade is terminal (no STREAM step follows).
     */
    private boolean isClassTerminalStep = false;

    // ─── user data ────────────────────────────────────────────────────
    private String selectedBoard = "";
    private String selectedBoardCode = "";
    private String selectedBoardName = "";
    private String SecondaryCourseId = "";
    private String selectedCoachingId = "";
    private String gradeName = "";
    private String gradeId = "";
    private String gradeCode = "";
    private String gradeCodeForEvent = "";
    private boolean mediumSelectedEventFired = false;
    private boolean classSelectedEventFired = false;
    private boolean streamSelectedEventFired = false;
    private String courseId = "";
    private String userId;
    private String name;
    private Long number = 0L;
    private String my_lan = "";
    private double lat, lng;
    private String address;

    private boolean hindiSelected;
    private boolean englishSelected;

    private final ArrayList<NameIdModel> boardList = new ArrayList<>();
    private final ArrayList<NameIdModel> examList = new ArrayList<>();
    private final ArrayList<NameIdModel> coachingList = new ArrayList<>();
    private final ArrayList<GradeModel> classList = new ArrayList<>();
    private final ArrayList<StreamModel> subjectGroupModels = new ArrayList<>();
    private final ArrayList<AddSubjectAboutFragmentModel> subModel = new ArrayList<>();
    private final ArrayList<String> subjectIds = new ArrayList<>();

    // ─── misc ─────────────────────────────────────────────────────────
    private float downX;
    private final Gson gson = new Gson();
    private boolean toShowLanguagePopUp;
    private String switchLanguage;
    private String currentFragment;

    // ══════════════════════════════════════════════════════════════════
    //  Lifecycle
    // ══════════════════════════════════════════════════════════════════

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding_new);
        EdgeToEdgeHelper.applyArtworkEdgeToEdge(
                findViewById(R.id.main),
                new View[] {findViewById(R.id.bottom_layout)},
                new View[] {findViewById(R.id.toolbar)});

        // Name
        String intentName = getIntent().getStringExtra("Name");
        name = StringUtils.isNotEmpty(intentName)
                ? intentName
                : MyApplication.getStringData(
                PreferenceKeys.USER_NAME_ENTERED_DURING_ONBOARDING.getKey());
        Log.d(TAG, "onCreate name=" + name);

        // User ID
        userId = getIntent().getStringExtra(PreferenceKeys.USER_ID.getKey());

        // Phone number
        String intentNumber = getIntent().getStringExtra(
                PreferenceKeys.PHONE_NUMBER.getKey());
        try {
            if (StringUtils.isNotEmpty(intentNumber)) {
                number = Long.parseLong(intentNumber);
            } else {
                number = Long.parseLong(MyApplication.getStringData(
                        PreferenceKeys.USER_PHONE_NUMBER_ENTERED_DURING_ONBOARDING.getKey()));
            }
        } catch (Exception ex) {
            Log.e(TAG, "Failed to parse phone number", ex);
        }

        lat = getIntent().getDoubleExtra("lat", 0.0);
        lng = getIntent().getDoubleExtra("long", 0.0);
        address = getIntent().getStringExtra("address");

        initializeViews();

        AudioManager.getInstance().setMuted(
                MyApplication.getFirebaseRemoteConfig()
                        .getBoolean("onboarding_mute_default"));
        updateMuteIcon();

        buildActiveStepsFromRemote();
        detectSwipe();
        getOnboardingState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        AudioManager.getInstance().pause();
        // Persist v2 state locally + server on pause
        saveOnboardingStateV2();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AudioManager.getInstance().resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AudioManager.getInstance().release();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    // ══════════════════════════════════════════════════════════════════
    //  Remote config → active steps
    // ══════════════════════════════════════════════════════════════════

    private void buildActiveStepsFromRemote() {
        Log.d(TAG, "buildActiveStepsFromRemote() started");

        activeSteps.clear();
        audioUrlsEn.clear();
        audioUrlsHi.clear();
        audioRepeat.clear();

        try {
            String jsonStr = MyApplication.getFirebaseRemoteConfig()
                    .getString("onboarding_data_batch_v10");

            Log.d(TAG, "Remote config raw json: " + jsonStr);

            if (StringUtils.isBlank(jsonStr)) {
                Log.w(TAG, "Remote config is empty. Falling back to default steps.");
                setDefaultSteps();
                return;
            }

            JSONObject root = new JSONObject(jsonStr);
            Log.d(TAG, "Parsed root JSON successfully");

            JSONObject configObj = null;

            OnBoardingSelectionModel selModel =
                    PreferenceMethods.getSelectedOnboardingModel();

            if (selModel != null) {
                Log.d(TAG, "Selected onboarding model -> id=" + selModel.getId());
            } else {
                Log.d(TAG, "Selected onboarding model is null");
            }

            if (selModel != null && StringUtils.isNotEmpty(selModel.getId())) {
                configObj = root.optJSONObject(selModel.getId());
                Log.d(TAG, "Looking for config block with id=" + selModel.getId()
                        + " found=" + (configObj != null));
            }

            if (configObj == null) {
                configObj = root.optJSONObject("default");
                Log.d(TAG, "Using default config block. found=" + (configObj != null));
            }

            if (configObj == null && root.has("steps")) {
                configObj = root;
                Log.d(TAG, "Using legacy root-level steps config");
            }

            if (configObj == null) {
                Log.w(TAG, "No valid config block found. Falling back to defaults.");
                setDefaultSteps();
                return;
            }

            Log.d(TAG, "Resolved onboarding config block: " + configObj);

            if (!configObj.has("steps")) {
                Log.w(TAG, "Config block does not contain steps array.");
                setDefaultSteps();
                return;
            }

            JSONArray arr = configObj.getJSONArray("steps");
            Log.d(TAG, "Total steps received from remote config: " + arr.length());

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String stepName = obj.optString("name", null);
                boolean enabled = obj.optBoolean("enabled", true);
                String skipFlag = obj.optString("skipIfFlag", null);
                JSONObject audio = obj.optJSONObject("audio");

                Log.d(TAG, "Processing step[" + i + "] name=" + stepName
                        + ", enabled=" + enabled + ", skipFlag=" + skipFlag);

                if (!enabled) {
                    Log.d(TAG, "Skipping step because enabled=false : " + stepName);
                    continue;
                }

                if (StringUtils.isNotEmpty(skipFlag)) {
                    String flagVal = MyApplication.getFirebaseRemoteConfig()
                            .getString(skipFlag);
                    Log.d(TAG, "skipIfFlag check -> " + skipFlag + "=" + flagVal);
                    if (StringUtils.isNotEmpty(flagVal)
                            && Boolean.parseBoolean(flagVal)) {
                        Log.d(TAG, "Skipping step " + stepName
                                + " because flag " + skipFlag + " is true");
                        continue;
                    }
                }

                if (StringUtils.isEmpty(stepName)) {
                    Log.w(TAG, "Skipping step because name is empty");
                    continue;
                }

                OnboardingStep step = OnboardingStep.fromKey(stepName);
                if (step == null) {
                    Log.w(TAG, "Unknown onboarding step: " + stepName);
                    continue;
                }

                String enAudio = audio != null ? audio.optString("en", "") : "";
                String hiAudio = audio != null ? audio.optString("hi", "") : "";
                boolean repeat = audio != null && audio.optBoolean("repeat", false);

                activeSteps.add(step);
                audioUrlsEn.add(enAudio);
                audioUrlsHi.add(hiAudio);
                audioRepeat.add(repeat);

                Log.d(TAG, "Added step=" + step
                        + ", enAudio=" + enAudio
                        + ", hiAudio=" + hiAudio
                        + ", repeat=" + repeat);
            }

            if (activeSteps.isEmpty()) {
                Log.w(TAG, "No active steps generated. Falling back to defaults.");
                setDefaultSteps();
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing onboarding remote config", e);
            setDefaultSteps();
        } catch (Exception ex) {
            Log.e(TAG, "Unexpected error building active steps", ex);
            setDefaultSteps();
        }

        Log.d(TAG, "Final activeSteps count=" + activeSteps.size());
        Log.d(TAG, "Final activeSteps=" + activeSteps);
        Log.d(TAG, "Final audioUrlsEn=" + audioUrlsEn);
        Log.d(TAG, "Final audioUrlsHi=" + audioUrlsHi);
        Log.d(TAG, "Final audioRepeat=" + audioRepeat);
        Log.d(TAG, "buildActiveStepsFromRemote() completed");
    }

    private void setDefaultSteps() {
        activeSteps.clear();
        audioUrlsEn.clear();
        audioUrlsHi.clear();
        audioRepeat.clear();
        Collections.addAll(activeSteps,
                OnboardingStep.NAME, OnboardingStep.MEDIUM, OnboardingStep.BOARD,
                OnboardingStep.COACHING, OnboardingStep.GRADE, OnboardingStep.STREAM);
        for (int i = 0; i < activeSteps.size(); i++) {
            audioUrlsEn.add("");
            audioUrlsHi.add("");
            audioRepeat.add(false);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  Resume flow v2 — local-first with server sync
    //
    //  Local (DoNotDelete prefs) is the source of truth for fast startup.
    //  Server is synced for cross-device / reinstall recovery.
    //
    //  Flow:
    //    1. onCreate → try local cache first, then fetch from server
    //    2. navigateNext → save locally + push to server (fire-and-forget)
    //    3. onRegisterSuccess → clear local + delete from server
    //    4. All paths guarded by remote config kill-switch
    // ══════════════════════════════════════════════════════════════════

    private boolean isFreshLogin() {
        return getIntent() != null
                && getIntent().getBooleanExtra(EXTRA_FRESH_LOGIN, false);
    }

    private void getOnboardingState() {
        if (!OnboardingStateApiHelper.isResumeFlowEnabled()) {
            Log.d(TAG, "Resume flow disabled via remote config");
            switchFragment(1);
            return;
        }

        // ── Fresh login (just completed OTP) → skip local cache ──────
        // Auth events (auth_verified, login_success) are fired by the backend
        // during OTP verification. Local cache resume is instant and would
        // cause screen-viewed events to race with auth events. Skip local
        // cache and go to server fetch — the network delay ensures auth
        // events are processed first, while still supporting reinstall resume.
        if (!isFreshLogin()) {
            // ── Try local cache first (instant, no network needed) ────────
            UserOnboardingStateDto localState = PreferenceMethods.getOnboardingStateDto();
            if (localState != null && localState.getStepName() != null) {
                Log.d(TAG, "Resuming from local cache: " + localState.getStepName());
                applyResumedState(localState);

                // Also check server in background — another device may have
                // completed onboarding since we last saved locally.
                syncOnboardingCompletedFromServer();
                return;
            }
        } else {
            Log.d(TAG, "Fresh login — skipping local cache, will fetch from server");
        }

        // ── No local cache → fetch from server (reinstall scenario) ───
        shimmerFrameLayout.startShimmer();
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        fragment_container_view.setVisibility(View.GONE);

        OnboardingStateApiHelper.fetchState(String.valueOf(number),
                new OnboardingStateApiHelper.OnStateFetched() {
                    @Override
                    public void onSuccess(UserOnboardingStateDto state) {
                        if (isFinishing() || isDestroyed()) return;
                        stopShimmer();
                        if (state != null && state.getStepName() != null) {
                            PreferenceMethods.setOnboardingStateDto(state);
                        }
                        checkIfAlreadyOnboarded(state, () -> applyResumedState(state));
                    }

                    @Override
                    public void onError(String message) {
                        if (isFinishing() || isDestroyed()) return;
                        Log.e(TAG, "getOnboardingState error: " + message);
                        stopShimmer();
                        switchFragment(1);
                    }
                });
    }

    private void applyResumedState(UserOnboardingStateDto state) {
        if (state == null || state.getStepName() == null) {
            switchFragment(1);
            return;
        }

        Log.d(TAG, "applyResumedState stepName=" + state.getStepName());

        // ── Validate selectionCode FIRST ─────────────────────────────
        // The selectionCode in the DTO must match the current
        // OnBoardingSelectionModel.id (exam the user selected on the
        // course-selection screen). A mismatch means the user chose a
        // different exam this time → discard stale state, start fresh.
        // This MUST happen before firing any resume event.
        OnBoardingSelectionModel currentSel = PreferenceMethods.getSelectedOnboardingModel();
        String currentSelCode = currentSel != null ? currentSel.getId() : null;
        String stateSelCode = state.getSelectionCode();

        if (currentSelCode != null && stateSelCode != null
                && !currentSelCode.equals(stateSelCode)) {
            Log.w(TAG, "selectionCode mismatch: current=" + currentSelCode
                    + " state=" + stateSelCode + " → starting fresh");
            PreferenceMethods.clearOnboardingStateDto();
            switchFragment(1);
            return;
        }

        // ── Validate step belongs to this activity ───────────────────
        // NEET-only screens (NEET_GRADE_SCREEN, NEET_BOARD_SCREEN,
        // NEET_COMING_SOON_SCREEN) are handled by NeetClassSelectionActivity.
        // If a stale DTO has one of these, discard and start fresh.
        OnboardingStep targetStep = state.getStepName().toStep();
        if (targetStep == null) {
            Log.w(TAG, "Screen " + state.getStepName()
                    + " not valid for OnboardingActivityNew, clearing stale state");
            PreferenceMethods.clearOnboardingStateDto();
            switchFragment(1);
            return;
        }

        // ── Fire resume event (only after all validations pass) ──────
        // If coming from NeetClassSelectionActivity, this is a forward
        // navigation in an active flow, not a cold-start resume.
        if (!isFromNeetClassSelection()) {
            String resumeScreenName;
            switch (state.getStepName()) {
                case NAME_SCREEN:
                    resumeScreenName = ActionEventScreen.NAME_SCREEN.getValue();
                    break;
                case LANGUAGE_SCREEN:
                    resumeScreenName = ActionEventScreen.MEDIUM_SCREEN.getValue();
                    break;
                case GRADE_LEVEL_SCREEN:
                    resumeScreenName = ActionEventScreen.CLASS_SCREEN.getValue();
                    break;
                case STREAM_SCREEN:
                    resumeScreenName = ActionEventScreen.STREAM_SCREEN.getValue();
                    break;
                default:
                    resumeScreenName = ActionEventScreen.NAME_SCREEN.getValue();
                    break;
            }
            EventApiHelper.logResumeFlowInitiated(
                    String.valueOf(number),
                    state.getStepName().name(),
                    resumeScreenName
            );
        }

        // ── restore name ──────────────────────────────────────────────
        if (StringUtils.isNotEmpty(state.getUserName())) {
            name = state.getUserName();
        }

        // ── restore language ──────────────────────────────────────────
        if (StringUtils.isNotEmpty(state.getLanguageCode())) {
            my_lan = state.getLanguageCode();
            hindiSelected = "hi".equals(my_lan);
            englishSelected = "en".equals(my_lan);
            updateLocale(this, my_lan);
        }

        // ── restore selection model codes ─────────────────────────────
        OnBoardingSelectionModel sel = PreferenceMethods.getSelectedOnboardingModel();
        if (sel != null) {
            if (StringUtils.isNotEmpty(state.getExamCode()))
                sel.setExamCode(state.getExamCode());
            if (StringUtils.isNotEmpty(state.getGradeCode()))
                sel.setGradeCode(state.getGradeCode());
            if (StringUtils.isNotEmpty(state.getStreamCode()))
                sel.setStreamCode(state.getStreamCode());
            if (StringUtils.isNotEmpty(state.getSelectionCode()))
                sel.setId(state.getSelectionCode());
            PreferenceMethods.setSelectedOnboardingModel(sel);
        }

        if (StringUtils.isNotEmpty(state.getGradeCode())) {
            gradeCode = state.getGradeCode();
        }

        // ── resolve step position in activeSteps ──────────────────────
        // targetStep was already validated above (non-null guaranteed here)
        int targetPosition = -1;
        for (int i = 0; i < activeSteps.size(); i++) {
            if (activeSteps.get(i) == targetStep) {
                targetPosition = i + 1; // 1-based
                break;
            }
        }

        if (targetPosition < 1) {
            Log.w(TAG, "Step " + targetStep
                    + " not found in activeSteps, starting from 1");
            switchFragment(1);
            return;
        }

        stepNumber = targetPosition;
        switchFragment(stepNumber);
    }

    // ── Already-onboarded check (another device completed onboarding) ──

    /**
     * Background server fetch to detect if onboarding was completed on another device.
     * Called after applying local cache — if server returns onboardingCompleted=true,
     * verifies user existence and navigates to HomeScreen.
     */
    private void syncOnboardingCompletedFromServer() {
        String phone = String.valueOf(number);
        if (StringUtils.isEmpty(phone)) return;

        OnboardingStateApiHelper.fetchState(phone,
                new OnboardingStateApiHelper.OnStateFetched() {
                    @Override
                    public void onSuccess(@Nullable UserOnboardingStateDto serverState) {
                        if (isFinishing() || isDestroyed()) return;
                        if (serverState != null
                                && Boolean.TRUE.equals(serverState.getOnboardingCompleted())) {
                            Log.d(TAG, "Server says onboardingCompleted=true");
                            PreferenceMethods.setOnboardingStateDto(serverState);
                            checkIfAlreadyOnboarded(serverState, () -> {
                                // User not found on server — do nothing, stay on current screen
                            });
                        }
                    }

                    @Override
                    public void onError(String message) {
                        // Silent — background check, don't disrupt UI
                    }
                });
    }

    /**
     * If the state says onboarding is completed (e.g. finished on another device),
     * verifies the user exists via API and navigates to HomeScreen.
     * Otherwise runs the normal flow.
     */
    private void checkIfAlreadyOnboarded(@Nullable UserOnboardingStateDto state,
                                         @NonNull Runnable normalFlow) {
        if (state == null || !Boolean.TRUE.equals(state.getOnboardingCompleted())) {
            normalFlow.run();
            return;
        }

        String phone = state.getPhoneNumber();
        if (StringUtils.isEmpty(phone)) {
            phone = String.valueOf(number);
        }
        if (StringUtils.isEmpty(phone)) {
            normalFlow.run();
            return;
        }

        Log.d(TAG, "onboardingCompleted=true, verifying user existence");

        Map<String, Object> params = new HashMap<>();
        params.put("phoneNumber", phone);
        params.put("key", BuildConfig.API_KEY);

        MyJsonObjectRequest request = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(UrlRoutes.AV_GET_USER_BY_PHONE, params),
                null,
                response -> {
                    if (isFinishing() || isDestroyed()) return;
                    try {
                        TypeToken<ApiWrappedResponse<UserDetailsResponse>> token =
                                new TypeToken<ApiWrappedResponse<UserDetailsResponse>>() {};
                        ApiWrappedResponse<UserDetailsResponse> apiResponse =
                                gson.fromJson(response.toString(), token.getType());

                        if (apiResponse.isSuccess() && apiResponse.getCode() != 404
                                && apiResponse.getData() != null) {
                            navigateToHomeAsExistingUser(apiResponse.getData());
                        } else {
                            normalFlow.run();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "checkIfAlreadyOnboarded parse error", e);
                        normalFlow.run();
                    }
                },
                error -> {
                    if (isFinishing() || isDestroyed()) return;
                    Log.e(TAG, "checkIfAlreadyOnboarded API error", error);
                    normalFlow.run();
                });

        MyVolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void navigateToHomeAsExistingUser(UserDetailsResponse user) {
        if (isFinishing() || isDestroyed()) return;

        Log.d(TAG, "User already onboarded on another device → navigating to HomeScreen");

        MyApplication.saveUserProfileLocally(user);
        MyApplication.saveData(PreferenceKeys.NUMBER_UPPER.getKey(), String.valueOf(number));
        MyApplication.saveData(PreferenceKeys.PHONE_NUMBER.getKey(), String.valueOf(number));
        MyApplication.saveData(PreferenceKeys.USER_LOGGED_IN.getKey(), true);
        if (user.getUserDto() != null) {
            MyApplication.saveData(PreferenceKeys.USER_ID.getKey(), user.getUserDto().getUserId());
        }
        MyApplication.saveData(PreferenceKeys.BOARD_SWITCH.getKey(), true);

        // Clean up onboarding state
        deleteOnboardingStateV2();

        Intent intent = new Intent(this, HomeScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(BundleParamConstants.LOCATION_SHOW_LOCATION_PERMISSION, true);
        startActivity(intent);
        finish();
    }

    // ── Build current state DTO from activity fields ─────────────────

    private UserOnboardingStateDto buildCurrentStateDto() {
        OnboardingStep currentStep = getStepForPosition(stepNumber);
        OnboardingScreenName screenName = OnboardingScreenName.fromStep(currentStep);
        if (screenName == null) return null;

        OnBoardingSelectionModel sel = PreferenceMethods.getSelectedOnboardingModel();

        return UserOnboardingStateDto.builder()
                .phoneNumber(String.valueOf(number))
                .userName(name)
                .languageCode(my_lan)
                .stepName(screenName)
                .selectionCode(sel != null ? sel.getId() : null)
                .examCode(sel != null ? sel.getExamCode() : null)
                .gradeCode(sel != null ? sel.getGradeCode() : null)
                .gradeId(parseIntSafe(gradeId))
                .streamCode(sel != null ? sel.getStreamCode() : null)
                .academicYearCode(sel != null ? sel.getYearCode() : null)
                .build();
    }

    private Integer parseIntSafe(String value) {
        if (StringUtils.isEmpty(value)) return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ── Save state: local first, then server (fire-and-forget) ───────

    private void saveOnboardingStateV2() {
        if (!OnboardingStateApiHelper.isResumeFlowEnabled()) return;

        UserOnboardingStateDto state = buildCurrentStateDto();
        if (state == null) return;

        // Local — always succeeds, instant
        PreferenceMethods.setOnboardingStateDto(state);

        // Server — fire-and-forget, don't block UI
        OnboardingStateApiHelper.saveState(state, null);
    }

    // ── Clear state: local + server ──────────────────────────────────

    private void deleteOnboardingStateV2() {
        PreferenceMethods.clearOnboardingStateDto();
        if (!OnboardingStateApiHelper.isResumeFlowEnabled()) return;
        OnboardingStateApiHelper.deleteState(String.valueOf(number), null);
    }

    // ══════════════════════════════════════════════════════════════════
    //  View initialisation
    // ══════════════════════════════════════════════════════════════════

    private void initializeViews() {
        shimmerFrameLayout = findViewById(R.id.shimmer_layout);
        toolbar = findViewById(R.id.toolbar);
        bottom_layout = findViewById(R.id.bottom_layout);
        fragment_container_view = findViewById(R.id.fragment_container_view);
        back_button_layout = findViewById(R.id.back_button_layout);
        progress = findViewById(R.id.progress);
        next_card = findViewById(R.id.next_card);
        previous_card = findViewById(R.id.previous_card);
        cv_volume = findViewById(R.id.cv_volume);
        iv_volume = findViewById(R.id.iv_volume);

        previous_card.setOnClickListener(v -> onBackPressedActivity("UI_BACK_BOTTOM"));
        next_card.setOnClickListener(v -> navigateNext("BUTTON_CLICK"));

        cv_volume.setVisibility(View.GONE);
        cv_volume.setOnClickListener(v -> {
            AudioManager.getInstance().toggleMute();
            updateMuteIcon();
            Map<String, String> ep = new HashMap<>();
            ep.put("mute", String.valueOf(AudioManager.getInstance().isMuted()));
            MyApplication.logFirebaseBundleCreation(
                    FirebaseEvents.onboarding_audio_mute_click.getActionName(), ep);
        });

        loading = new ProgressDialog(this);
        loading.setCancelable(false);
        loading.setMessage(getString(R.string.please_wait));
        loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBackPressedActivity("NATIVE");
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════
    //  Navigation helpers
    // ══════════════════════════════════════════════════════════════════

    private void onBackPressedActivity(String source) {
        OnBoardingSelectionModel sel = PreferenceMethods.getSelectedOnboardingModel();

        Map<String, String> params = new HashMap<>();
        params.put("source", source != null ? source : "");
        params.put("screen", currentFragment != null ? currentFragment : "");
        MyApplication.logFirebaseBundleCreation(
                FirebaseEvents.user_onboarding_navigation_left_arrow.getActionName(), params);

        if (stepNumber > 1) {
            navigateBack();
        } else if (isFromNeetClassSelection()) {
            // Came from NeetClassSelectionActivity — go back to it with smooth transition
            navigateBackToNeetClassSelection();
        } else {
            finish();
        }
    }

    /**
     * Move backward one step.
     * Restores STREAM into activeSteps before decrementing when the grade
     * was previously marked as terminal, keeping the list symmetric.
     */
    private void navigateBack() {
        Log.d(TAG, "navigateBack stepNumber=" + stepNumber);

        if (isClassTerminalStep) {
            isClassTerminalStep = false;
            ensureStreamInList();
        }

        stepNumber -= 1;
        switchFragmentBack(stepNumber);
        startButtonPressAnimation(previous_card);

        // Persist updated step for resume flow (fire-and-forget)
        saveOnboardingStateV2();
    }

    /**
     * Move forward one step.
     * Captures stepNumber and list size into final locals BEFORE the 500 ms
     * postDelayed to avoid a data-race with concurrent fragment callbacks.
     */
    private void navigateNext(String type) {
        Log.d(TAG, "navigateNext type=" + type + " nextIsEnabled=" + nextIsEnabled);

        if (!nextIsEnabled) {
            showToast();
            return;
        }

        if (!CommonMethods.isOnline(this)) {
            CommonMethods.showNegativeSnackbar(fragment_container_view, getString(R.string.NoInternetConnection));
            return;
        }

        startButtonPressAnimation(next_card);
        next_card.setEnabled(false);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        final int capturedStep = stepNumber;
        final int capturedListSize = activeSteps.size();
        final String capturedScreen = currentFragment;

        next_card.postDelayed(() -> {

            if (isFinishing() || isDestroyed()) return;

            OnBoardingSelectionModel sel = PreferenceMethods.getSelectedOnboardingModel();

            if (capturedScreen != null
                    && capturedScreen.equalsIgnoreCase(OnboardingStep.NAME.name())) {
                Map<String, String> p = new HashMap<>();
                p.put("name", name != null ? name : "");
                p.put("phone", number.toString());
                MyApplication.logFirebaseBundleCreation(
                        FirebaseEvents.onboarding_name_entered.getActionName(), p);
                EventApiHelper.sendActionEvent(
                        ActionEventName.NAME_SUBMITTED.getValue(),
                        (sel != null && sel.getId() != null) ? sel.getId() : "",
                        String.valueOf(number),
                        name != null ? name : "",
                        ActionEventScreen.NAME_SCREEN.getValue());
            }

            if (capturedScreen != null
                    && capturedScreen.equalsIgnoreCase(OnboardingStep.MEDIUM.name())
                    && !mediumSelectedEventFired) {
                mediumSelectedEventFired = true;
                EventApiHelper.sendActionEvent(
                        ActionEventName.MEDIUM_SELECTED.getValue(),
                        (sel != null && sel.getId() != null) ? sel.getId() : "",
                        String.valueOf(number),
                        my_lan,
                        ActionEventScreen.MEDIUM_SCREEN.getValue());
            }

            if (capturedScreen != null
                    && capturedScreen.equalsIgnoreCase(OnboardingStep.GRADE.name())
                    && !classSelectedEventFired) {
                classSelectedEventFired = true;
                EventApiHelper.sendActionEvent(
                        ActionEventName.CLASS_SELECTED.getValue(),
                        (sel != null && sel.getId() != null) ? sel.getId() : "",
                        String.valueOf(number),
                        gradeCodeForEvent,
                        ActionEventScreen.CLASS_SCREEN.getValue());
            }

            if (capturedScreen != null
                    && capturedScreen.equalsIgnoreCase(OnboardingStep.STREAM.name())
                    && !streamSelectedEventFired) {
                streamSelectedEventFired = true;
                EventApiHelper.sendActionEvent(
                        ActionEventName.STREAM_SELECTED.getValue(),
                        (sel != null && sel.getId() != null) ? sel.getId() : "",
                        String.valueOf(number),
                        (sel != null && sel.getStreamCode() != null) ? sel.getStreamCode() : "",
                        ActionEventScreen.STREAM_SCREEN.getValue());
            }

            if (capturedStep < capturedListSize) {
                stepNumber = capturedStep + 1;
                switchFragment(stepNumber);
            } else {
                stepNumber = capturedListSize + 1;
                switchFragment(stepNumber);
            }

            // Persist current state for resume flow (fire-and-forget)
            saveOnboardingStateV2();

            Map<String, String> p = new HashMap<>();
            p.put("screen", capturedScreen != null ? capturedScreen : "");
            p.put("type", type != null ? type : "");
            MyApplication.logFirebaseBundleCreation(
                    FirebaseEvents.user_onboarding_navigation_right_arrow.getActionName(), p);

        }, 500);
    }

    // ══════════════════════════════════════════════════════════════════
    //  Swipe detection
    // ══════════════════════════════════════════════════════════════════

    @SuppressLint("ClickableViewAccessibility")
    private void detectSwipe() {
        fragment_container_view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getX();
                    return true;
                case MotionEvent.ACTION_UP:
                    float delta = event.getX() - downX;
                    if (Math.abs(delta) > SWIPE_THRESHOLD) {
                        if (delta < 0) navigateNext("SWIPE");
                        else onBackPressedActivity("SWIPE");
                    }
                    return true;
            }
            return false;
        });
    }

    // ══════════════════════════════════════════════════════════════════
    //  Fragment switching — FORWARD
    // ══════════════════════════════════════════════════════════════════

    private void switchFragment(int stepNumber) {
        next_card.setEnabled(true);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        toolbar.setBackgroundResource(R.drawable.on_boarding_background_new);
        stopShimmer();

        int totalSteps = activeSteps.size();
        Log.d(TAG, "switchFragment step=" + stepNumber + "/" + totalSteps
                + " isTerminal=" + isClassTerminalStep);

        if (stepNumber > totalSteps) {
            boolean subjectCheckRequired =
                    !MyApplication.isCompetitiveProfile() && !isClassTerminalStep;

            OnBoardingSelectionModel sel = PreferenceMethods.getSelectedOnboardingModel();
            if (subjectCheckRequired
                    && (sel == null || StringUtils.isEmpty(sel.getStreamCode()))) {
                stepNumber = totalSteps;
                this.stepNumber = stepNumber;
                showToast();
            } else if (!MyApplication.isNetworkAvailable()) {
                // No internet — reset stepNumber to last valid step so Back works correctly
                this.stepNumber = totalSteps;
                Toast.makeText(this, getString(R.string.internet_failure_error), Toast.LENGTH_SHORT).show();
            } else {
                saveData();
            }
            return;
        }

        Fragment fragment = null;
        OnboardingStep step = getStepForPosition(stepNumber);

        previous_card.setVisibility(
                (stepNumber <= 1 && !isFromNeetClassSelection()) ? View.INVISIBLE : View.VISIBLE);

        if (step != null) {
            switch (step) {

                case NAME:
                    nextIsEnabled = StringUtils.isNotEmpty(name);
                    fragment = NameFragment.newInstance(
                            name != null ? name : "", String.valueOf(number),
                            ActionEventDirection.FORWARD.getValue());
                    currentFragment = OnboardingStep.NAME.name();
                    break;

                case MEDIUM:
                    mediumSelectedEventFired = false;
                    if (!hindiSelected && !englishSelected) {
                        String savedLang = MyApplication.getStringData(
                                PreferenceKeys.MY_LANG.getKey());
                        if ("hi".equals(savedLang)) {
                            hindiSelected = true;
                            my_lan = "hi";
                        } else if ("en".equals(savedLang)) {
                            englishSelected = true;
                            my_lan = "en";
                        }
                    }
                    nextIsEnabled = hindiSelected || englishSelected;
                    if (isNeetFlow()) {
                        fragment = CompetitiveMediumFragment.newInstance(
                                name != null ? name : "", String.valueOf(number), my_lan,
                                ActionEventDirection.FORWARD.getValue());
                        // Competitive flow keeps default toolbar — no gradient
                        toolbar.setBackgroundResource(R.drawable.on_boarding_background_new);
                    } else {
                        fragment = MediumFragment.newInstance(
                                name != null ? name : "", String.valueOf(number), my_lan,
                                ActionEventDirection.FORWARD.getValue());
                        toolbar.setBackground(
                                ContextCompat.getDrawable(this, R.drawable.gradient_toolbar));
                    }
                    currentFragment = OnboardingStep.MEDIUM.name();
                    break;

                case BOARD:
                    nextIsEnabled = !CollectionUtils.isEmpty(boardList);
                    fragment = BoardFragment.newInstance(boardList);
                    currentFragment = OnboardingStep.BOARD.name();
                    break;

                case COACHING:
                    nextIsEnabled = StringUtils.isNotEmpty(selectedCoachingId);
                    fragment = CoachingFragment.newInstance(coachingList, selectedCoachingId);
                    currentFragment = OnboardingStep.COACHING.name();
                    break;

                case GRADE:
                    classSelectedEventFired = false;
                    nextIsEnabled = !CollectionUtils.isEmpty(classList);
                    fragment = GradesFragment.newInstance(
                            name != null ? name : "", String.valueOf(number), classList,
                            ActionEventDirection.FORWARD.getValue());
                    currentFragment = OnboardingStep.GRADE.name();
                    if (StringUtils.isNotEmpty(gradeCode)) {
                        applyGradeTerminalLogic(gradeCode, /*fromClick=*/false);
                    }
                    break;

                case STREAM:
                    streamSelectedEventFired = false;
                    nextIsEnabled = !CollectionUtils.isEmpty(subjectGroupModels);
                    fragment = StreamsFragment.newInstance(
                            name != null ? name : "", String.valueOf(number),
                            subjectGroupModels, ActionEventDirection.FORWARD.getValue());
                    currentFragment = OnboardingStep.STREAM.name();
                    break;

                default:
                    nextIsEnabled = StringUtils.isNotEmpty(name);
                    fragment = NameFragment.newInstance(
                            name != null ? name : "", String.valueOf(number),
                            ActionEventDirection.FORWARD.getValue());
                    currentFragment = OnboardingStep.NAME.name();
            }
        }

        Log.d(TAG, "switchFragment nextIsEnabled=" + nextIsEnabled);
        applyNextCardColor();

        if (fragment != null) {
            startTransition(fragment);
            handleLinearProgress(stepNumber);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  Fragment switching — BACKWARD
    // ══════════════════════════════════════════════════════════════════

    private void switchFragmentBack(int stepNumber) {
        Fragment fragment = null;
        OnboardingStep step = getStepForPosition(stepNumber);

        previous_card.setVisibility(
                (stepNumber <= 1 && !isFromNeetClassSelection()) ? View.INVISIBLE : View.VISIBLE);

        if (step != null) {
            switch (step) {

                case NAME:
                    nextIsEnabled = StringUtils.isNotEmpty(name);
                    fragment = NameFragment.newInstance(
                            name != null ? name : "", String.valueOf(number),
                            ActionEventDirection.BACKWARD.getValue());
                    currentFragment = OnboardingStep.NAME.name();
                    break;

                case MEDIUM:
                    mediumSelectedEventFired = false;
                    if (!hindiSelected && !englishSelected) {
                        String savedLang = MyApplication.getStringData(
                                PreferenceKeys.MY_LANG.getKey());
                        if ("hi".equals(savedLang)) {
                            hindiSelected = true;
                            my_lan = "hi";
                        } else if ("en".equals(savedLang)) {
                            englishSelected = true;
                            my_lan = "en";
                        }
                    }
                    nextIsEnabled = hindiSelected || englishSelected;
                    // ── FIX: guard toolbar background by flow type, same as switchFragment
                    if (isNeetFlow()) {
                        fragment = CompetitiveMediumFragment.newInstance(
                                name != null ? name : "", String.valueOf(number), my_lan,
                                ActionEventDirection.BACKWARD.getValue());
                        // Competitive flow keeps default toolbar — no gradient
                        toolbar.setBackgroundResource(R.drawable.on_boarding_background_new);
                    } else {
                        fragment = MediumFragment.newInstance(
                                name != null ? name : "", String.valueOf(number), my_lan,
                                ActionEventDirection.BACKWARD.getValue());
                        toolbar.setBackground(
                                ContextCompat.getDrawable(this, R.drawable.gradient_toolbar));
                    }
                    currentFragment = OnboardingStep.MEDIUM.name();
                    break;

                case BOARD:
                    nextIsEnabled = !CollectionUtils.isEmpty(boardList);
                    fragment = BoardFragment.newInstance(boardList);
                    currentFragment = OnboardingStep.BOARD.name();
                    break;

                case COACHING:
                    nextIsEnabled = StringUtils.isNotEmpty(selectedCoachingId);
                    fragment = CoachingFragment.newInstance(coachingList, selectedCoachingId);
                    currentFragment = OnboardingStep.COACHING.name();
                    break;

                case GRADE:
                    classSelectedEventFired = false;
                    nextIsEnabled = StringUtils.isNotEmpty(gradeId);
                    fragment = GradesFragment.newInstance(
                            name != null ? name : "", String.valueOf(number), classList,
                            ActionEventDirection.BACKWARD.getValue());
                    currentFragment = OnboardingStep.GRADE.name();
                    break;

                case STREAM:
                    streamSelectedEventFired = false;
                    nextIsEnabled = !CollectionUtils.isEmpty(subjectGroupModels);
                    fragment = StreamsFragment.newInstance(
                            name != null ? name : "", String.valueOf(number),
                            subjectGroupModels, ActionEventDirection.BACKWARD.getValue());
                    currentFragment = OnboardingStep.STREAM.name();
                    break;

                default:
                    nextIsEnabled = StringUtils.isNotEmpty(name);
                    fragment = NameFragment.newInstance(
                            name != null ? name : "", String.valueOf(number),
                            ActionEventDirection.BACKWARD.getValue());
                    currentFragment = OnboardingStep.NAME.name();
            }
        } else if (stepNumber <= 1) {
            nextIsEnabled = StringUtils.isNotEmpty(name);
            fragment = NameFragment.newInstance(
                    name != null ? name : "", String.valueOf(number),
                    ActionEventDirection.BACKWARD.getValue());
            currentFragment = OnboardingStep.NAME.name();
        }

        if (fragment != null) {
            applyNextCardColor();
            handleLinearProgress(stepNumber);
            startTransitionBack(fragment);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  Step / list helpers
    // ══════════════════════════════════════════════════════════════════

    private OnboardingStep getStepForPosition(int pos) {
        if (pos < 1 || pos > activeSteps.size()) return null;
        return activeSteps.get(pos - 1);
    }

    private void applyGradeTerminalLogic(String gradeCode, boolean fromClick) {
        if (shouldTreatGradeAsLastStep(gradeCode)) {
            isClassTerminalStep = true;
            removeStreamFromList();
        } else {
            isClassTerminalStep = false;
            ensureStreamInList();
        }
        Log.d(TAG, "applyGradeTerminalLogic gradeCode=" + gradeCode
                + " terminal=" + isClassTerminalStep
                + " steps=" + activeSteps);
    }

    private void removeStreamFromList() {
        activeSteps.remove(OnboardingStep.STREAM);
    }

    private void ensureStreamInList() {
        if (!activeSteps.contains(OnboardingStep.STREAM)) {
            int gradePos = activeSteps.indexOf(OnboardingStep.GRADE);
            int insertAt = gradePos >= 0 ? gradePos + 1 : activeSteps.size();
            activeSteps.add(insertAt, OnboardingStep.STREAM);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  Progress bar
    // ══════════════════════════════════════════════════════════════════

    private void handleLinearProgress(int step) {
        int total = activeSteps.size();
        if (total <= 0) {
            progress.setProgress(0);
            return;
        }
        int pct = (step * 100) / total;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progress.setProgress(pct, true);
        } else {
            progress.setProgress(pct);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  UI helpers
    // ══════════════════════════════════════════════════════════════════

    private void applyNextCardColor() {
        next_card.setCardBackgroundColor(nextIsEnabled
                ? getColor(R.color.subscription_blue)
                : getColor(R.color.disabled_next_color));
    }

    private void updateMuteIcon() {
        iv_volume.setImageResource(AudioManager.getInstance().isMuted()
                ? R.drawable.img_volume_mute
                : R.drawable.img_volume_high);
    }

    private void stopShimmer() {
        shimmerFrameLayout.stopShimmer();
        shimmerFrameLayout.setVisibility(View.GONE);
        fragment_container_view.setVisibility(View.VISIBLE);
    }

    private void showToast() {
        if (!MyApplication.isNetworkAvailable()) {
            Toast.makeText(this, getString(R.string.slow_internet_error),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        OnboardingStep step = getStepForPosition(stepNumber);
        String message;
        if (step == OnboardingStep.NAME)
            message = getString(R.string.please_enter_your_name);
        else if (step == OnboardingStep.MEDIUM)
            message = getString(R.string.Please_apna_medium_select_kare);
        else if (step == OnboardingStep.BOARD)
            message = getString(R.string.Please_apna_board_select_kare);
        else if (step == OnboardingStep.GRADE)
            message = getString(R.string.Please_apni_class_select_kare);
        else if (step == OnboardingStep.COACHING || step == OnboardingStep.STREAM)
            message = getString(R.string.Please_ek_option_select_kare);
        else
            message = getString(R.string.please_wait);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void playAudioForPage(int zeroBasedPosition) {
        if (zeroBasedPosition < 0 || zeroBasedPosition >= audioUrlsEn.size()) return;

        String lang = MyApplication.getStringData("My_Lang");
        boolean isHindi = ArivihanLanguageEnum.HINDI.getCode().equals(lang);
        String url = isHindi
                ? audioUrlsHi.get(zeroBasedPosition)
                : audioUrlsEn.get(zeroBasedPosition);

        if (StringUtils.isEmpty(url)) {
            url = isHindi
                    ? audioUrlsEn.get(zeroBasedPosition)
                    : audioUrlsHi.get(zeroBasedPosition);
        }

        AudioManager.getInstance().setRepeat(audioRepeat.get(zeroBasedPosition));
        AudioManager.getInstance().playAudio(url, this);
        cv_volume.setVisibility(StringUtils.isNotEmpty(url) ? View.VISIBLE : View.GONE);
    }

    // ══════════════════════════════════════════════════════════════════
    //  Fragment transitions
    // ══════════════════════════════════════════════════════════════════

    private void startTransition(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(
                R.anim.fade_in_slide_in, R.anim.fade_out_slide_out,
                R.anim.slide_in_left, R.anim.slide_out_right);
        ft.replace(R.id.fragment_container_view, fragment);
        if (stepNumber > 0) ft.addToBackStack(null);
        ft.commitAllowingStateLoss();
        playAudioForPage(stepNumber - 1);
    }

    private void startTransitionBack(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(
                R.anim.slide_in_left, R.anim.slide_out_right,
                R.anim.slide_in_left, R.anim.slide_out_right);
        ft.replace(R.id.fragment_container_view, fragment);
        if (stepNumber > 0) ft.addToBackStack(null);
        ft.commitAllowingStateLoss();
        playAudioForPage(stepNumber - 1);
    }

    private void startButtonPressAnimation(View view) {
        ObjectAnimator sdX = ObjectAnimator.ofFloat(view, View.SCALE_X, 0.9f);
        ObjectAnimator sdY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 0.9f);
        ObjectAnimator suX = ObjectAnimator.ofFloat(view, View.SCALE_X, 1f);
        ObjectAnimator suY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f);

        AnimatorSet down = new AnimatorSet();
        down.playTogether(sdX, sdY);
        down.setDuration(50);

        AnimatorSet up = new AnimatorSet();
        up.playTogether(suX, suY);
        up.setDuration(50);

        AnimatorSet seq = new AnimatorSet();
        seq.playSequentially(down, up);
        seq.start();
    }

    // ══════════════════════════════════════════════════════════════════
    //  Locale
    // ══════════════════════════════════════════════════════════════════

    private void updateLocale(Context context, String langCode) {
        MyApplication.saveData(
                PreferenceKeys.MY_LANG.getKey(),
                ArivihanLanguageEnum.fromStringToCode(langCode));

        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration cfg = res.getConfiguration();
        cfg.setLocale(locale);
        context.getApplicationContext().createConfigurationContext(cfg);
        res.updateConfiguration(cfg, res.getDisplayMetrics());

        saveLanguageOfUser();
    }

    private void saveLanguageOfUser() {
        try {
            MyJsonObjectRequest req = new MyJsonObjectRequest(
                    Request.Method.PATCH,
                    CommonMethods.getUrlParam(AV_CHANGE_USER_LANGUAGE, null),
                    null, r -> {
            }, e -> {
            });
            MyVolleySingleton.getInstance(this).addToRequestQueue(req);
        } catch (Exception e) {
            Log.e(TAG, "saveLanguageOfUser error", e);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  Fragment listener callbacks
    // ══════════════════════════════════════════════════════════════════

    @Override
    public void onNameEntered(String enteredName) {
        name = enteredName;
        MyApplication.saveData(
                PreferenceKeys.USER_NAME_ENTERED_DURING_ONBOARDING.getKey(), name);
        MyApplication.saveData(PreferenceKeys.NAME.getKey(), name);

        boolean hasName = StringUtils.isNotEmpty(name);
        if (nextIsEnabled != hasName) {
            nextIsEnabled = hasName;
            applyNextCardColor();
        }
    }

    /**
     * Shared callback for both MediumFragment and CompetitiveMediumFragment.
     * Java resolves the identical signature from both interfaces to this single method.
     */
    @Override
    public void onMediumClick(boolean hindiSelected, boolean englishSelected) {
        nextIsEnabled = true;
        this.hindiSelected = hindiSelected;
        this.englishSelected = englishSelected;

        my_lan = hindiSelected ? "hi" : "en";
        updateLocale(this, my_lan);

        Map<String, String> p = new HashMap<>();
        p.put("name", name != null ? name : "");
        p.put("phone", number.toString());
        p.put("medium_selected", my_lan);
        MyApplication.logFirebaseBundleCreation(
                FirebaseEvents.onboarding_medium_selected.getActionName(), p);

        // For NEET, BOARD / COACHING / EXAM steps are disabled in remote config,
        // so clearing those lists is misleading — guard explicitly.
        if (!isNeetFlow()) {
            boardList.clear();
            coachingList.clear();
            examList.clear();
        }
        // Always clear grade/stream data — language change invalidates them in all flows
        subjectGroupModels.clear();
        classList.clear();
        subModel.clear();
        subjectIds.clear();

        mediumSelectedEventFired = true;
        OnBoardingSelectionModel sel = PreferenceMethods.getSelectedOnboardingModel();
        EventApiHelper.sendActionEvent(
                ActionEventName.MEDIUM_SELECTED.getValue(),
                (sel != null && sel.getId() != null) ? sel.getId() : "",
                String.valueOf(number),
                my_lan,
                ActionEventScreen.MEDIUM_SCREEN.getValue());

        navigateNext("MEDIUM");
        loading.setMessage(getString(R.string.please_wait));
    }

    @Override
    public void onBoardClick(NameIdModel nameIdModel,
                             ArrayList<NameIdModel> boardList, boolean clicked) {
        nextIsEnabled = true;
        this.boardList.clear();
        this.boardList.addAll(boardList);
        selectedBoard = nameIdModel.getId();
        selectedBoardName = nameIdModel.getName();
        selectedBoardCode = nameIdModel.getBoardCode();
        courseId = nameIdModel.getId();

        Map<String, String> p = new HashMap<>();
        p.put("name", name != null ? name : "");
        p.put("phone", number.toString());
        p.put("board_selected", nameIdModel.getBoardCode());
        MyApplication.logFirebaseBundleCreation(
                FirebaseEvents.onboarding_board_selected.getActionName(), p);

        coachingList.clear();
        subjectGroupModels.clear();
        examList.clear();
        classList.clear();
        subModel.clear();
        subjectIds.clear();

        OnBoardingSelectionModel sel = PreferenceMethods.getSelectedOnboardingModel();

        if (clicked) navigateNext("BOARD");
    }

    @Override
    public void nextCardEnabled(String simpleName) {
        String remoteBoard = MyApplication.getFirebaseRemoteConfig()
                .getString("selected_board_code");
        String remoteGrade = MyApplication.getFirebaseRemoteConfig()
                .getString("selected_grade_v1");

        if (BoardFragment.TAG.equals(simpleName)
                && StringUtils.isNotEmpty(remoteBoard)) {
            nextIsEnabled = true;
            applyNextCardColor();
        } else if (GradesFragment.TAG.equals(simpleName)
                && StringUtils.isNotEmpty(remoteGrade)) {
            nextIsEnabled = true;
            applyNextCardColor();
        }
    }

    @Override
    public void onCoachingClicked(NameIdModel nameIdModel, int position,
                                  ArrayList<NameIdModel> coachingList) {
        this.coachingList.clear();
        this.coachingList.addAll(coachingList);
        selectedCoachingId = nameIdModel.getId();
        nextIsEnabled = true;

        subjectGroupModels.clear();
        examList.clear();
        classList.clear();
        subModel.clear();
        subjectIds.clear();

        OnBoardingSelectionModel sel = PreferenceMethods.getSelectedOnboardingModel();

        navigateNext("COACHING");
    }

    @Override
    public void onGradeClick(GradeModel gradeModel, boolean clicked) {
        nextIsEnabled = true;
        gradeName = gradeModel.getName();
        gradeId = String.valueOf(gradeModel.getId());
        gradeCode = gradeModel.getGradeCode();
        gradeCodeForEvent = gradeModel.getCode() != null ? gradeModel.getCode() : gradeModel.getGradeCode();

        MyApplication.saveData(PreferenceKeys.SELECTED_GRADE_ID.getKey(), gradeId);
        MyApplication.saveData(PreferenceKeys.SELECTED_GRADE_CODE.getKey(), gradeCode);

        Map<String, String> p = new HashMap<>();
        p.put("name", name != null ? name : "");
        p.put("phone", number.toString());
        p.put("class_id", gradeId);
        p.put("class_name", gradeModel.getName());
        MyApplication.logFirebaseBundleCreation(
                FirebaseEvents.onboarding_class_selected.getActionName(), p);

        OnBoardingSelectionModel sel = PreferenceMethods.getSelectedOnboardingModel();
        if (sel != null) {
            sel.setGradeCode(gradeModel.getGradeCode());
            sel.setYearCode(gradeModel.getAcademicYearCode());
            sel.setStreamCode(null);
            PreferenceMethods.setSelectedOnboardingModel(sel);
        }

        subModel.clear();
        subjectGroupModels.clear();
        subjectIds.clear();

        // Mutate list BEFORE any navigation call so the 500 ms lambda
        // in navigateNext captures the already-corrected list size.
        applyGradeTerminalLogic(gradeCode, clicked);

        if (!clicked) return;

        if (isClassTerminalStep) {
            applyNextCardColor();
            handleLinearProgress(stepNumber);
        } else {
            navigateNext("CLASS");
        }
    }

    @Override
    public void saveGradeData(ArrayList<GradeModel> gradeModels) {
        this.classList.clear();
        this.classList.addAll(gradeModels);
    }

    @Override
    public void onSubjectGroupClicked(StreamModel subjectGroup,
                                      ArrayList<StreamModel> subjectGroupModelArrayList,
                                      boolean clicked,
                                      String subjectGroupLanguage) {
        toShowLanguagePopUp = StringUtils.isNotEmpty(subjectGroupLanguage);
        if (toShowLanguagePopUp) switchLanguage = subjectGroupLanguage;

        subjectGroupModels.clear();
        subjectGroupModels.addAll(subjectGroupModelArrayList);

        nextIsEnabled = true;
        applyNextCardColor();

        OnBoardingSelectionModel sel = PreferenceMethods.getSelectedOnboardingModel();
        if (sel != null) {
            sel.setStreamCode(subjectGroup.getCode());
            PreferenceMethods.setSelectedOnboardingModel(sel);
        }

        Map<String, String> p = new HashMap<>();
        p.put("name", name != null ? name : "");
        p.put("phone", number.toString());
        p.put("stream_id", String.valueOf(subjectGroup.getId()));
        p.put("stream_name", subjectGroup.getDisplayName());
        p.put("language_pop_up", toShowLanguagePopUp ? "true" : "false");
        MyApplication.logFirebaseBundleCreation(
                FirebaseEvents.onboarding_stream_selected.getActionName(), p);
    }

    // ══════════════════════════════════════════════════════════════════
    //  Save data to server
    // ══════════════════════════════════════════════════════════════════

    private void saveData() {
        if (BuildConfig.REELS_DEMO) {
            if(isRegisterRequestInProgress)return;isRegisterRequestInProgress=true;
            DemoEnvironment.finishOnboarding(this,name,new NativeReelsService.Callback(){
                public void success(Object profile){isRegisterRequestInProgress=false;}
                public void failure(String error){isRegisterRequestInProgress=false;new androidx.appcompat.app.AlertDialog.Builder(OnboardingActivityNew.this).setTitle("Could not save profile").setMessage(error).setPositiveButton("Retry",(d,w)->saveData()).setNegativeButton("Stay here",null).show();}
            });return;
        }
        if (isRegisterRequestInProgress) {
            Log.w(TAG, "saveData: request already in progress, ignoring");
            return;
        }
        isRegisterRequestInProgress = true;

        if (!isFinishing() && !isDestroyed()) {
            loading.show();
        }

        MyApplication.saveData(PreferenceKeys.PHONE_NUMBER.getKey(), String.valueOf(number));

        OnBoardingSelectionModel sel = PreferenceMethods.getSelectedOnboardingModel();

        UserRegisterRequest req = new UserRegisterRequest();
        req.setUserName(name != null ? name
                : MyApplication.getStringData(PreferenceKeys.NAME.getKey()));
        req.setPhoneNumber(number);
        req.setExamCode(sel.getExamCode());
        req.setGradeCode(sel.getGradeCode());
        req.setStreamCode(sel.getStreamCode());
        req.setBatchCode(StringUtils.isNotEmpty(sel.getBatchCode())
                ? getCompetitiveBatchCode(sel.getId()) : "");
        req.setAcademicYearCode(sel.getYearCode());
        req.setBoardCode(sel.getBoardCode());
        req.setDeviceKey(DeviceInfoUtil.getDeviceId(this));
        req.setReferralCode(ReferralLinkHandler.getStoredReferrer());

        String utmStr = MyApplication.getStringDataFromDoNotDeleteSharedPreference(
                PreferenceKeys.ALL_UTM_PARAMS.getKey());
        if (StringUtils.isNotEmpty(utmStr)) {
            Type mapType = new TypeToken<Map<String, String>>() {
            }.getType();
            req.setAllUtmParams(new Gson().fromJson(utmStr, mapType));
        }

        req.setFcmToken(MyApplication.getDoNotDeleteSharedPrefKey(
                PreferenceKeys.FCM_TOKEN.getKey()));

        Log.d(TAG, "saveData request=" + req);
        String body = gson.toJson(req);


        try {
            MyJsonObjectRequest request = new MyJsonObjectRequest(
                    Request.Method.POST,
                    AV_REGISTER,
                    new JSONObject(body),
                    response -> onRegisterSuccess(response.toString()),
                    this::onRegisterError);
            MyVolleySingleton.getInstance(this).addToRequestQueue(request);
        } catch (JSONException e) {
            dismissLoader();
            isRegisterRequestInProgress = false;
            Log.e(TAG, "JSON exception in saveData", e);
        }
    }

    private String getCompetitiveBatchCode(String examCode) {
        Log.d(TAG, "getCompetitiveBatchCode: examCode=" + examCode);

        String json = FirebaseRemoteConfig.getInstance()
                .getString("batch_code_for_competitive_exam");
        Log.d(TAG, "getCompetitiveBatchCode: remote json=" + json);

        try {
            JSONObject jsonObject = new JSONObject(json);

            if (jsonObject.has(examCode)) {
                JSONObject batchObject = jsonObject.getJSONObject(examCode);
                String language = MyApplication.getStringData(
                        PreferenceKeys.MY_LANG.getKey());
                Log.d(TAG, "getCompetitiveBatchCode: language=" + language);

                if (batchObject.has(language)) {
                    String batchCode = batchObject.getString(language);
                    Log.d(TAG, "getCompetitiveBatchCode: batchCode=" + batchCode);
                    return batchCode;
                } else {
                    String batchCode = batchObject.getString("en");
                    Log.d(TAG, "getCompetitiveBatchCode: fallback en batchCode=" + batchCode);
                    return batchCode;
                }
            } else {
                Log.w(TAG, "getCompetitiveBatchCode: examCode not found: " + examCode);
            }

        } catch (JSONException e) {
            Log.e(TAG, "getCompetitiveBatchCode: JSONException=" + e.getMessage());
        }

        Log.w(TAG, "getCompetitiveBatchCode: returning null for examCode=" + examCode);
        return null;
    }

    private void onRegisterSuccess(String json) {
        try {
            TypeToken<ApiWrappedResponse<UserDetailsResponse>> token =
                    new TypeToken<ApiWrappedResponse<UserDetailsResponse>>() {
                    };
            ApiWrappedResponse<UserDetailsResponse> apiResponse =
                    gson.fromJson(json, token.getType());

            UserDetailsResponse userDetails = apiResponse.getData();
            Log.d(TAG, "onRegisterSuccess: " + userDetails);

            if (!apiResponse.isSuccess() || userDetails == null) {
                Toast.makeText(this, getString(R.string.something_went_wrong),
                        Toast.LENGTH_SHORT).show();
                isRegisterRequestInProgress = false;
                dismissLoader();

                return;
            }

            MyApplication.saveData(
                    PreferenceKeys.ON_BOARDING_NUMBER.getKey(), String.valueOf(number));
            MyApplication.saveData(PreferenceKeys.USER_LOGGED_IN.getKey(), true);
            MyApplication.saveUserProfileLocally(userDetails);

            // Onboarding complete — clean up resume state from server
            deleteOnboardingStateV2();

            dismissLoader();

            Map<String, String> p = new HashMap<>();
            p.put("name", name != null ? name : "");
            p.put("phone", number.toString());
            p.put("secondary_exam", SecondaryCourseId != null ? SecondaryCourseId : "");
            MyApplication.logFirebaseBundleCreation(
                    FirebaseEvents.onboarding_completed.getActionName(), p);

            String registeredUserId = userDetails.getUserDto() != null ? userDetails.getUserDto().getUserId() : null;
            List<UserDetailsResponse.BatchDetail> registeredBatches = userDetails.getBatches();
            String registeredBatchCode = (registeredBatches != null && !registeredBatches.isEmpty() && registeredBatches.get(0).getBatchCode() != null)
                    ? registeredBatches.get(0).getBatchCode() : "";
            BranchAnalyticsHelper.logCompleteRegistration(this, registeredUserId, number != null ? number.toString() : "", registeredBatchCode);

            try {
                List<UserDetailsResponse.BatchDetail> batches = userDetails.getBatches();
                if (batches != null && !batches.isEmpty()) {
                    UserDetailsResponse.BatchDetail firstBatch = batches.get(0);

                    String batchCode = firstBatch.getBatchCode() != null
                            ? firstBatch.getBatchCode() : "";
                    String examCode = firstBatch.getExamCode() != null
                            ? firstBatch.getExamCode() : "";
                    String gc = firstBatch.getGradeCode() != null
                            ? firstBatch.getGradeCode() : "";

                    Map<String, String> dp = new HashMap<>();
                    dp.put("name", name != null ? name : "");
                    dp.put("phone", number != null ? number.toString() : "");
                    dp.put("batch_code", batchCode);
                    dp.put("exam_code", examCode);
                    dp.put("grade_code", gc);

                    MyApplication.logFirebaseBundleCreation(batchCode + "_registered", dp);
                    MyApplication.logFirebaseBundleCreation(examCode + "_registered", dp);
                    MyApplication.logFirebaseBundleCreation(gc + "_" + examCode + "_registered", dp);
                    MyApplication.logFirebaseBundleCreation(batchCode + "_completed", dp);

                    Log.d(TAG, "Dynamic events logged for batchCode=" + batchCode);
                }
            } catch (Exception ex) {
                Log.e(TAG, "onRegisterSuccess: failed to log dynamic event", ex);
            }

            MyApplication.saveSharedPreferencesDoNotDelete(
                    PreferenceKeys.SELECTED_COURSE_ID.getKey(), courseId);
            MyApplication.removeOnboardingSharedPreferencesKey();

            MyApplication.saveData(PreferenceKeys.ON_BOARDING_COMPLETED.getKey(), true);
            MyApplication.saveData(PreferenceKeys.LAUNCH_COUNT.getKey(), 1);
            MyApplication.saveData("show_topper_notes_status", true);
            MyApplication.saveData("OnboardingCompleted", true);
            MyApplication.saveData("show_welcome_bottom_sheet", true);
            isRegisterRequestInProgress = false;

            Intent intent = new Intent(this, HomeScreen.class);
            intent.putExtra("NewUser", true);
            intent.putExtra(BundleParamConstants.LOCATION_SHOW_LOCATION_PERMISSION, true);
            intent.putExtra("open_new_session_popup", true);
            intent.putExtra("show_language_update_popUp", toShowLanguagePopUp);
            intent.putExtra("is_user_onboarded", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        } catch (Exception ex) {
            isRegisterRequestInProgress = false;
            dismissLoader();
            Log.e(TAG, "onRegisterSuccess parse error", ex);

            Map<String, String> p = new HashMap<>();
            p.put("name", name != null ? name : "");
            p.put("phone", number.toString());
            p.put("error_message", ex.getMessage() != null
                    ? ex.getMessage() : "ParseException");
            MyApplication.logFirebaseBundleCreation(
                    FirebaseEvents.onboarding_failed.getActionName(), p);

        }
    }

    private void onRegisterError(com.android.volley.VolleyError error) {
        try {
            isRegisterRequestInProgress = false;
            dismissLoader();

            String errorMsg = error != null && error.getMessage() != null
                    ? error.getMessage() : "";
            Map<String, String> p = new HashMap<>();
            p.put("name", name != null ? name : "");
            p.put("phone", number.toString());
            p.put("error_message", errorMsg);
            MyApplication.logFirebaseBundleCreation(
                    FirebaseEvents.onboarding_failed.getActionName(), p);

            if (!MyApplication.isNetworkAvailable() || error instanceof NoConnectionError) {
                Toast.makeText(this, getString(R.string.internet_failure_error),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.server_error),
                        Toast.LENGTH_SHORT).show();
            }

            OnBoardingSelectionModel sel = PreferenceMethods.getSelectedOnboardingModel();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void dismissLoader() {
        if (!isFinishing() && !isDestroyed()
                && loading != null && loading.isShowing()) {
            loading.dismiss();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  Flow type helpers
    // ══════════════════════════════════════════════════════════════════

    /**
     * Returns true when the currently selected onboarding model is NEET.
     * Used to decide which medium fragment to show:
     * CompetitiveMediumFragment for NEET, MediumFragment for all others.
     */
    private boolean isNeetFlow() {
        OnBoardingSelectionModel sel = PreferenceMethods.getSelectedOnboardingModel();
        return sel != null && "NEET".equalsIgnoreCase(sel.getId());
    }

    /**
     * Returns true if this activity was launched from NeetClassSelectionActivity
     * (either Dropper or Board redirect flow). Used for back navigation at step 1.
     */
    private boolean isFromNeetClassSelection() {
        return getIntent() != null && getIntent().getBooleanExtra(
                NeetClassSelectionActivity.EXTRA_FROM_NEET_CLASS_SELECTION, false);
    }

    /**
     * Smooth back navigation to NeetClassSelectionActivity.
     * Passes back the origin context so the correct screen is restored
     * (class selection for Dropper, board redirect for Class 10/11/12).
     */
    private void navigateBackToNeetClassSelection() {
        if (isFinishing() || isDestroyed()) return;

        // Clear stale onboarding state — user is going backward, not resuming
        deleteOnboardingStateV2();

        MyApplication.saveData(PreferenceKeys.NEET_CLASS_SELECTION_PENDING.getKey(), true);

        Intent intent = new Intent(this, NeetClassSelectionActivity.class);
        // Forward the origin context back so NeetClassSelectionActivity
        // can restore the correct screen (class selection or board redirect)
        Intent incoming = getIntent();
        if (incoming != null) {
            String origin = incoming.getStringExtra(NeetClassSelectionActivity.EXTRA_BACK_ORIGIN);
            if (origin != null) intent.putExtra(NeetClassSelectionActivity.EXTRA_BACK_ORIGIN, origin);
            String classCode = incoming.getStringExtra(NeetClassSelectionActivity.EXTRA_BACK_CLASS_CODE);
            if (classCode != null) intent.putExtra(NeetClassSelectionActivity.EXTRA_BACK_CLASS_CODE, classCode);
            String classLabel = incoming.getStringExtra(NeetClassSelectionActivity.EXTRA_BACK_CLASS_LABEL);
            if (classLabel != null) intent.putExtra(NeetClassSelectionActivity.EXTRA_BACK_CLASS_LABEL, classLabel);
        }

        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    // ══════════════════════════════════════════════════════════════════
    //  Grade terminal check — driven by Firebase Remote Config
    // ══════════════════════════════════════════════════════════════════

    /**
     * Returns true if gradeCode appears in the remote-config comma-separated list
     * "subjects_screen_skip_for_grade_code" (case-insensitive).
     * When true, the STREAM step is skipped for this user.
     */
    private boolean shouldTreatGradeAsLastStep(String gradeCode) {
        String remote = MyApplication.getFirebaseRemoteConfig()
                .getString("subjects_screen_skip_for_grade_code");

        Log.d(TAG, "shouldTreatGradeAsLastStep: input=" + gradeCode + " remote=" + remote);

        if (StringUtils.isBlank(gradeCode)) {
            Log.w(TAG, "gradeCode blank → false");
            return false;
        }
        if (StringUtils.isBlank(remote)) {
            Log.w(TAG, "remote config blank → false");
            return false;
        }

        for (String code : remote.split(",")) {
            if (gradeCode.trim().equalsIgnoreCase(code.trim())) {
                Log.d(TAG, "Matched " + code.trim() + " → true");
                return true;
            }
        }
        Log.d(TAG, "No match → false");
        return false;
    }

    // ══════════════════════════════════════════════════════════════════
    //  Generic utility
    // ══════════════════════════════════════════════════════════════════

    private <T> void replaceAll(ArrayList<T> target, java.util.List<T> source) {
        target.clear();
        if (!CollectionUtils.isEmpty(source)) {
            target.addAll(source);
        }
    }
}
