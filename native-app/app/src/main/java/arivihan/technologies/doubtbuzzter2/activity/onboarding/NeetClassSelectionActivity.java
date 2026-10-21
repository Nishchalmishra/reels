package arivihan.technologies.doubtbuzzter2.activity.onboarding;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.HomeScreen;
import arivihan.technologies.doubtbuzzter2.activity.OnboardingActivityNew;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.constants.UrlRoutes;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.event.ActionEventDirection;
import arivihan.technologies.doubtbuzzter2.enums.event.ActionEventName;
import arivihan.technologies.doubtbuzzter2.enums.event.ActionEventScreen;
import arivihan.technologies.doubtbuzzter2.enums.onboarding.OnboardingScreenName;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceMethods;
import arivihan.technologies.doubtbuzzter2.fragments.authentication.NeetBoardRedirectFragment;
import arivihan.technologies.doubtbuzzter2.fragments.authentication.NeetClassSelectionFragment;
import arivihan.technologies.doubtbuzzter2.fragments.authentication.NeetComingSoonFragment;
import arivihan.technologies.doubtbuzzter2.helper.onboarding.OnboardingStateApiHelper;
import arivihan.technologies.doubtbuzzter2.model.NeetClassOption;
import arivihan.technologies.doubtbuzzter2.model.OnBoardingSelectionModel;
import arivihan.technologies.doubtbuzzter2.model.batch.user.UserDetailsResponse;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.model.onboarding.UserOnboardingStateDto;
import arivihan.technologies.doubtbuzzter2.utils.AudioManager;
import arivihan.technologies.doubtbuzzter2.volley.EventApiHelper;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;

/**
 * NEET Class Selection Activity.
 * Always English. Hosts class selection + Board redirect / Coming Soon fragments.
 * Speaker/volume button for audio playback (same as OnboardingActivityNew).
 * Back hidden on first screen, forward disabled until selection.
 */
public class NeetClassSelectionActivity extends AppCompatActivity
        implements NeetClassSelectionFragment.OnNeetClassActionListener,
                   NeetBoardRedirectFragment.OnBoardRedirectListener {

    private static final String TAG = "NeetClassSelection";
    public static final String EXTRA_FROM_NEET_CLASS_SELECTION = "from_neet_class_selection";
    public static final String EXTRA_BACK_ORIGIN = "neet_back_origin";
    public static final String EXTRA_BACK_CLASS_CODE = "neet_back_class_code";
    public static final String EXTRA_BACK_CLASS_LABEL = "neet_back_class_label";
    public static final String ORIGIN_DROPPER = "dropper";
    public static final String ORIGIN_BOARD_REDIRECT = "board_redirect";

    private View toolbar;
    private CardView btnBack;
    private CardView btnForward;
    private CardView cvVolume;
    private ImageView ivVolume;

    private NeetClassOption pendingSelection;
    private String currentFragmentTag;
    private boolean isNavigating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        forceEnglishLocale();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neet_class_selection);

        toolbar = findViewById(R.id.toolbar);
        btnBack = findViewById(R.id.btn_back);
        btnForward = findViewById(R.id.btn_forward);
        cvVolume = findViewById(R.id.cv_volume);
        ivVolume = findViewById(R.id.iv_volume);

        // NEET_CLASS_SELECTION_PENDING is set by the caller (UserAuthenticationActivity
        // or StartActivity resume) BEFORE launching this activity, so no race condition.

        setupBottomNavigation();
        setupVolumeButton();
        setupBackPress();

        if (savedInstanceState == null) {
            restoreOrShowInitial();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(createEnglishContext(newBase));
    }

    @Override
    protected void onPause() {
        super.onPause();
        AudioManager.getInstance().pause();
        // Persist v2 state locally + server for resume flow
        saveNeetStateV2(getCurrentScreenName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-force English in case app locale was changed by OnboardingActivityNew
        forceEnglishLocale();
        AudioManager.getInstance().resume();
    }

    @Override
    protected void onDestroy() {
        // Cancel any pending delayed button updates
        if (btnBack != null && btnBack.getHandler() != null) {
            btnBack.getHandler().removeCallbacksAndMessages(null);
        }
        AudioManager.getInstance().release();
        super.onDestroy();
    }

    // ── Fragment management ─────────────────────────────────────────────────

    /**
     * On launch, determines which screen to show:
     * 1. Intent extras (back from OnboardingActivityNew) → restore board redirect
     * 2. Local DTO state (app kill resume) → route to saved screen
     * 3. Server fetch (reinstall resume) → route to saved screen
     * 4. Fresh start → class selection
     *
     * Steps 2–3 are guarded by remote config kill-switch.
     */
    private void restoreOrShowInitial() {
        // ── 1. Back from OnboardingActivityNew ──────────────────────
        String origin = getIntent().getStringExtra(EXTRA_BACK_ORIGIN);
        if (origin != null) {
            // Clear stale DTO from OnboardingActivityNew's session so that
            // subsequent navigations within this activity don't trigger resume events
            PreferenceMethods.clearOnboardingStateDto();
        }
        if (ORIGIN_DROPPER.equals(origin)) {
            // Dropper user pressed back on step 1 of OnboardingActivityNew
            // → show class selection with BACKWARD direction event
            PreferenceMethods.setSelectedOnBoardingModelById("NEET");
            showClassSelection();
            return;
        }
        if (ORIGIN_BOARD_REDIRECT.equals(origin)) {
            String classCode = getIntent().getStringExtra(EXTRA_BACK_CLASS_CODE);
            String classLabel = getIntent().getStringExtra(EXTRA_BACK_CLASS_LABEL);

            PreferenceMethods.setSelectedOnBoardingModelById("NEET");

            replaceFragment(
                    NeetBoardRedirectFragment.newInstance(
                            classCode != null ? classCode : "",
                            classLabel != null ? classLabel : ""),
                    NeetBoardRedirectFragment.TAG,
                    DIR_NONE);
            btnBack.setVisibility(View.VISIBLE);
            updateForwardState(false);

            // Fire screen viewed event — user navigated back from OnboardingActivityNew
            EventApiHelper.sendActionEvent(
                    ActionEventName.NEET_CLASS_UNAVAILABLE_SCREEN_VIEWED.getValue(),
                    null,
                    resolvePhoneForFetch(),
                    ActionEventDirection.BACKWARD.getValue(),
                    ActionEventScreen.NEET_CLASS_UNAVAILABLE_SCREEN.getValue()
            );
            return;
        }

        // ── Resume flow (guarded by remote config) ──────────────────
        if (!OnboardingStateApiHelper.isResumeFlowEnabled()) {
            showClassSelectionInitial();
            return;
        }

        // ── 2. Local DTO (instant, handles app-kill resume) ─────────
        UserOnboardingStateDto localState = PreferenceMethods.getOnboardingStateDto();
        if (localState != null && localState.getStepName() != null) {
            Log.d(TAG, "restoreOrShowInitial: resuming from local cache → "
                    + localState.getStepName());
            applyNeetResumeState(localState);

            // Also check server in background — another device may have
            // completed onboarding since we last saved locally.
            syncOnboardingCompletedFromServer();
            return;
        }

        // ── 3. Server fetch (reinstall resume) ─────────────────────
        String phone = resolvePhoneForFetch();
        if (phone == null || phone.isEmpty()) {
            showClassSelectionInitial();
            return;
        }

        // Hide UI during fetch
        findViewById(R.id.fragment_container).setVisibility(View.GONE);
        findViewById(R.id.bottom_layout).setVisibility(View.GONE);

        OnboardingStateApiHelper.fetchState(phone,
                new OnboardingStateApiHelper.OnStateFetched() {
                    @Override
                    public void onSuccess(@Nullable UserOnboardingStateDto state) {
                        if (isFinishing() || isDestroyed()) return;
                        showUiAfterFetch();
                        if (state != null && state.getStepName() != null) {
                            PreferenceMethods.setOnboardingStateDto(state);
                            Log.d(TAG, "restoreOrShowInitial: resuming from server → "
                                    + state.getStepName());
                            checkIfAlreadyOnboarded(state,
                                    () -> applyNeetResumeState(state));
                        } else {
                            showClassSelectionInitial();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        if (isFinishing() || isDestroyed()) return;
                        Log.e(TAG, "restoreOrShowInitial: server fetch failed → "
                                + message);
                        showUiAfterFetch();
                        showClassSelectionInitial();
                    }
                });
    }

    /**
     * Routes to the correct NEET sub-screen based on the saved state DTO.
     * Falls back to class selection for unknown/unmappable screen names.
     */
    private void applyNeetResumeState(UserOnboardingStateDto state) {
        if (state == null || state.getStepName() == null) {
            showClassSelectionInitial();
            return;
        }

        OnboardingScreenName screen = state.getStepName();

        // Only log resume for valid NEET screens — non-NEET screens (e.g. MEDIUM_SCREEN
        // from OnboardingActivityNew) are stale state that should be discarded silently.
        boolean isValidNeetScreen = screen == OnboardingScreenName.NEET_GRADE_SCREEN
                || screen == OnboardingScreenName.NEET_BOARD_SCREEN
                || screen == OnboardingScreenName.NEET_COMING_SOON_SCREEN;

        if (isValidNeetScreen) {
            String resumeScreenName;
            switch (screen) {
                case NEET_BOARD_SCREEN:
                    resumeScreenName = ActionEventScreen.NEET_CLASS_UNAVAILABLE_SCREEN.getValue();
                    break;
                case NEET_COMING_SOON_SCREEN:
                    resumeScreenName = ActionEventScreen.NEET_COMING_SOON_SCREEN.getValue();
                    break;
                default:
                    resumeScreenName = ActionEventScreen.NEET_CLASS_SCREEN.getValue();
                    break;
            }
            EventApiHelper.logResumeFlowInitiated(
                    resolvePhoneForFetch(),
                    screen.name(),
                    resumeScreenName
            );
        } else {
            // Stale DTO from a different activity — clear it
            PreferenceMethods.clearOnboardingStateDto();
            showClassSelectionInitial();
            return;
        }

        switch (screen) {
            case NEET_BOARD_SCREEN:
                String classCode = state.getNeetGradeCode();
                String classLabel = state.getNeetGradeLabel();
                if (classCode != null && !classCode.isEmpty()) {
                    replaceFragment(
                            NeetBoardRedirectFragment.newInstance(
                                    classCode,
                                    classLabel != null ? classLabel : ""),
                            NeetBoardRedirectFragment.TAG,
                            DIR_NONE);
                    btnBack.setVisibility(View.VISIBLE);
                    updateForwardState(false);

                    EventApiHelper.sendActionEvent(
                            ActionEventName.NEET_CLASS_UNAVAILABLE_SCREEN_VIEWED.getValue(),
                            null,
                            resolvePhoneForFetch(),
                            classCode.replaceAll("[^0-9]", ""),
                            ActionEventScreen.NEET_CLASS_UNAVAILABLE_SCREEN.getValue()
                    );
                } else {
                    // No class context — can't show board redirect meaningfully
                    showClassSelectionInitial();
                }
                break;

            case NEET_COMING_SOON_SCREEN:
                toolbar.setVisibility(View.GONE);
                replaceFragment(NeetComingSoonFragment.newInstance(),
                        NeetComingSoonFragment.TAG, DIR_NONE);
                btnBack.setVisibility(View.VISIBLE);
                btnForward.setVisibility(View.INVISIBLE);

                EventApiHelper.sendActionEvent(
                        ActionEventName.NEET_COMING_SOON_SCREEN_VIEWED.getValue(),
                        null,
                        resolvePhoneForFetch(),
                        "OTHER",
                        ActionEventScreen.NEET_COMING_SOON_SCREEN.getValue()
                );
                break;

            case NEET_GRADE_SCREEN:
            default:
                showClassSelectionInitial();
                break;
        }
    }

    /**
     * Resolves the phone number for server fetch.
     * Tries DoNotDelete prefs first, then intent extras.
     */
    private String resolvePhoneForFetch() {
        String phone = MyApplication.getDoNotDeleteSharedPrefKey(
                PreferenceKeys.USER_ONBOARDING_LOGIN_PHONE_NUMBER.getKey());
        if (phone != null && !phone.isEmpty()) return phone;
        return getIntent().getStringExtra(PreferenceKeys.PHONE_NUMBER.getKey());
    }

    private void showUiAfterFetch() {
        findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
        findViewById(R.id.bottom_layout).setVisibility(View.VISIBLE);
    }

    // ── Already-onboarded check (another device completed onboarding) ──

    private void syncOnboardingCompletedFromServer() {
        String phone = resolvePhoneForFetch();
        if (phone == null || phone.isEmpty()) return;

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

    private void checkIfAlreadyOnboarded(@Nullable UserOnboardingStateDto state,
                                         @NonNull Runnable normalFlow) {
        if (state == null || !Boolean.TRUE.equals(state.getOnboardingCompleted())) {
            normalFlow.run();
            return;
        }

        String resolvedPhone = state.getPhoneNumber();
        if (resolvedPhone == null || resolvedPhone.isEmpty()) {
            resolvedPhone = resolvePhoneForFetch();
        }
        if (resolvedPhone == null || resolvedPhone.isEmpty()) {
            normalFlow.run();
            return;
        }

        final String phone = resolvedPhone;
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
                                new Gson().fromJson(response.toString(), token.getType());

                        if (apiResponse.isSuccess() && apiResponse.getCode() != 404
                                && apiResponse.getData() != null) {
                            navigateToHomeAsExistingUser(apiResponse.getData(), phone);
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

    private void navigateToHomeAsExistingUser(UserDetailsResponse user, String phone) {
        if (isFinishing() || isDestroyed()) return;

        Log.d(TAG, "User already onboarded on another device → navigating to HomeScreen");

        MyApplication.saveUserProfileLocally(user);
        MyApplication.saveData(PreferenceKeys.NUMBER_UPPER.getKey(), phone);
        MyApplication.saveData(PreferenceKeys.PHONE_NUMBER.getKey(), phone);
        MyApplication.saveData(PreferenceKeys.USER_LOGGED_IN.getKey(), true);
        if (user.getUserDto() != null) {
            MyApplication.saveData(PreferenceKeys.USER_ID.getKey(), user.getUserDto().getUserId());
        }
        MyApplication.saveData(PreferenceKeys.BOARD_SWITCH.getKey(), true);

        // Clean up onboarding state
        PreferenceMethods.clearOnboardingStateDto();
        OnboardingStateApiHelper.deleteState(phone, null);

        Intent intent = new Intent(this, HomeScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showClassSelectionInitial() {
        isNavigating = false;
        toolbar.setVisibility(View.VISIBLE);
        replaceFragment(NeetClassSelectionFragment.newInstance(), NeetClassSelectionFragment.TAG, DIR_NONE);
        btnBack.setVisibility(View.INVISIBLE);
        btnForward.setVisibility(View.VISIBLE);
        updateForwardState(false);
        pendingSelection = null;
        playAudioForClassSelection();
        saveNeetStateV2(OnboardingScreenName.NEET_GRADE_SCREEN);

        EventApiHelper.sendActionEvent(
                ActionEventName.NEET_CLASS_SCREEN_VIEWED.getValue(),
                null,
                resolvePhoneForFetch(),
                ActionEventDirection.FORWARD.getValue(),
                ActionEventScreen.NEET_CLASS_SCREEN.getValue()
        );

        MyApplication.logFirebaseBundleCreation(
                FirebaseEvents.onboarding_neet_class_screen_view.getActionName(),
                new HashMap<>()
        );
    }

    private void showClassSelection() {
        isNavigating = false;
        toolbar.setVisibility(View.VISIBLE);
        replaceFragment(NeetClassSelectionFragment.newInstance(), NeetClassSelectionFragment.TAG, DIR_BACK);
        // Sync button changes with fragment transition
        updateButtonsDelayed(() -> {
            btnBack.setVisibility(View.INVISIBLE);
            btnForward.setVisibility(View.VISIBLE);
            updateForwardState(false);
        });
        pendingSelection = null;
        playAudioForClassSelection();
        saveNeetStateV2(OnboardingScreenName.NEET_GRADE_SCREEN);

        EventApiHelper.sendActionEvent(
                ActionEventName.NEET_CLASS_SCREEN_VIEWED.getValue(),
                null,
                resolvePhoneForFetch(),
                ActionEventDirection.BACKWARD.getValue(),
                ActionEventScreen.NEET_CLASS_SCREEN.getValue()
        );

        MyApplication.logFirebaseBundleCreation(
                FirebaseEvents.onboarding_neet_class_screen_view.getActionName(),
                new HashMap<>()
        );
    }

    private void showComingSoon() {
        toolbar.setVisibility(View.GONE);
        replaceFragment(NeetComingSoonFragment.newInstance(), NeetComingSoonFragment.TAG, DIR_FORWARD);
        updateButtonsDelayed(() -> {
            btnBack.setVisibility(View.VISIBLE);
            btnForward.setVisibility(View.INVISIBLE);
        });
        saveNeetStateV2(OnboardingScreenName.NEET_COMING_SOON_SCREEN);

        EventApiHelper.sendActionEvent(
                ActionEventName.NEET_COMING_SOON_SCREEN_VIEWED.getValue(),
                null,
                resolvePhoneForFetch(),
                "OTHER",
                ActionEventScreen.NEET_COMING_SOON_SCREEN.getValue()
        );

        MyApplication.logFirebaseBundleCreation(
                FirebaseEvents.onboarding_coming_soon_screen_view.getActionName(),
                new HashMap<>()
        );
    }

    private void showBoardRedirect(String classCode, String classLabel) {
        replaceFragment(
                NeetBoardRedirectFragment.newInstance(classCode, classLabel),
                NeetBoardRedirectFragment.TAG,
                DIR_FORWARD);
        updateButtonsDelayed(() -> {
            btnBack.setVisibility(View.VISIBLE);
            updateForwardState(false);
        });
        saveNeetStateV2(OnboardingScreenName.NEET_BOARD_SCREEN);

        EventApiHelper.sendActionEvent(
                ActionEventName.NEET_CLASS_UNAVAILABLE_SCREEN_VIEWED.getValue(),
                null,
                resolvePhoneForFetch(),
                classCode != null ? classCode.replaceAll("[^0-9]", "") : "",
                ActionEventScreen.NEET_CLASS_UNAVAILABLE_SCREEN.getValue()
        );

        Map<String, String> params = new HashMap<>();
        params.put("class_selected", classCode != null ? classCode.replaceAll("[^0-9]", "") : "");
        MyApplication.logFirebaseBundleCreation(
                FirebaseEvents.onboarding_neet_unavail_screen_view.getActionName(),
                params
        );
    }

    /**
     * Delays button visibility changes so they sync with the fragment
     * transition animation instead of snapping before it starts.
     */
    private void updateButtonsDelayed(Runnable action) {
        btnBack.postDelayed(() -> {
            if (!isFinishing() && !isDestroyed()) action.run();
        }, 150);
    }

    private static final int DIR_NONE = 0;
    private static final int DIR_FORWARD = 1;
    private static final int DIR_BACK = 2;

    private void replaceFragment(Fragment fragment, String tag, int direction) {
        if (isFinishing() || isDestroyed()) return;
        currentFragmentTag = tag;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setReorderingAllowed(true);
        if (direction == DIR_FORWARD) {
            ft.setCustomAnimations(R.anim.fade_in_slide_in, R.anim.fade_out_slide_out);
        } else if (direction == DIR_BACK) {
            ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        }
        ft.replace(R.id.fragment_container, fragment, tag);
        ft.commitAllowingStateLoss();
    }

    // ── Bottom navigation ───────────────────────────────────────────────────

    private void setupBottomNavigation() {
        btnBack.setOnClickListener(v -> handleBack("in_app"));
        btnForward.setOnClickListener(v -> {
            if (pendingSelection != null) handleForward();
        });
    }

    private void updateForwardState(boolean enabled) {
        btnForward.setCardBackgroundColor(ContextCompat.getColor(this,
                enabled ? R.color.subscription_blue : R.color.disabled_next_color));
        btnForward.setClickable(enabled);
    }

    // ── Volume / Speaker ────────────────────────────────────────────────────

    private void setupVolumeButton() {
        updateMuteIcon();
        cvVolume.setOnClickListener(v -> {
            AudioManager.getInstance().toggleMute();
            updateMuteIcon();

            boolean isMuted = AudioManager.getInstance().isMuted();
            FirebaseEvents event = isMuted ? FirebaseEvents.onboarding_neet_class_mute_click : FirebaseEvents.onboarding_neet_class_unmute_click;
            MyApplication.logFirebaseBundleCreation(event.getActionName(), new HashMap<>());
        });
    }

    private void updateMuteIcon() {
        ivVolume.setImageResource(AudioManager.getInstance().isMuted()
                ? R.drawable.img_volume_mute : R.drawable.img_volume_high);
    }

    private void playAudioForClassSelection() {
        try {
            String json = FirebaseRemoteConfig.getInstance()
                    .getString("neet_class_selection_config_v1");
            if (StringUtils.isNotBlank(json)) {
                org.json.JSONObject obj = new org.json.JSONObject(json);
                String audioUrl = obj.optString("audioUrl", "");
                if (StringUtils.isNotBlank(audioUrl)) {
                    AudioManager.getInstance().playAudio(audioUrl, this);
                    cvVolume.setVisibility(View.VISIBLE);
                    return;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Audio playback failed", e);
        }
        cvVolume.setVisibility(View.GONE);
    }

    private void stopAudio() {
        AudioManager.getInstance().release();
        cvVolume.setVisibility(View.GONE);
    }

    // ── Navigation logic ────────────────────────────────────────────────────

    private void handleForward() {
        if (pendingSelection == null || isNavigating) return;

        if (NeetBoardRedirectFragment.TAG.equals(currentFragmentTag)) {
            MyApplication.logFirebaseBundleCreation(
                    FirebaseEvents.onboarding_neet_unavail_fwd_btn_click.getActionName(),
                    new HashMap<>()
            );
        }

        isNavigating = true;

        String action = pendingSelection.getAction();
        if (action == null) action = "";

        switch (action) {
            case "NEET_FLOW":
                stopAudio();
                launchOnboardingActivity();
                break;
            case "NEET_NOT_AVAILABLE":
                stopAudio();
                showBoardRedirect(pendingSelection.getClassCode(), pendingSelection.getLabel());
                break;
            case "COMING_SOON":
                stopAudio();
                showComingSoon();
                break;
            default:
                Log.w(TAG, "Unknown action: " + action);
                break;
        }
    }

    private void handleBack(String source) {
        if (isFinishing() || isDestroyed()) return;

        FirebaseEvents fbEvent = null;
        if (NeetBoardRedirectFragment.TAG.equals(currentFragmentTag)) {
            fbEvent = FirebaseEvents.onboarding_neet_unavail_back_btn_click;
        } else if (NeetComingSoonFragment.TAG.equals(currentFragmentTag)) {
            fbEvent = FirebaseEvents.onboarding_coming_soon_back_btn_click;
        }

        if (fbEvent != null) {
            Map<String, String> params = new HashMap<>();
            params.put("back_type", source);
            MyApplication.logFirebaseBundleCreation(fbEvent.getActionName(), params);
        }

        if (NeetClassSelectionFragment.TAG.equals(currentFragmentTag)) {
            finish();
        } else {
            showClassSelection();
        }
    }

    private void launchOnboardingActivity() {
        if (isFinishing() || isDestroyed()) return;
        MyApplication.saveData(PreferenceKeys.NEET_CLASS_SELECTION_PENDING.getKey(), false);

        String phone = MyApplication.getDoNotDeleteSharedPrefKey(
                PreferenceKeys.USER_ONBOARDING_LOGIN_PHONE_NUMBER.getKey());
        String userId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());

        MyApplication.saveData(PreferenceKeys.AUTHENTICATION_DONE.getKey(), true);

        Intent intent = new Intent(this, OnboardingActivityNew.class);
        intent.putExtra(PreferenceKeys.PHONE_NUMBER.getKey(), phone != null ? phone : "");
        intent.putExtra(PreferenceKeys.USER_ID.getKey(), userId != null ? userId : "");
        intent.putExtra(EXTRA_FROM_NEET_CLASS_SELECTION, true);
        intent.putExtra(EXTRA_BACK_ORIGIN, ORIGIN_DROPPER);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in_slide_in, R.anim.fade_out_slide_out);
        finish();
    }

    private void launchOnboardingForBoard(OnBoardingSelectionModel board) {
        if (isFinishing() || isDestroyed()) return;

        // Store the class context so back navigation can restore board redirect screen
        String classCode = pendingSelection != null ? pendingSelection.getClassCode() : "";
        String classLabel = pendingSelection != null ? pendingSelection.getLabel() : "";

        PreferenceMethods.setSelectedOnboardingModel(board);

        MyApplication.saveData(PreferenceKeys.NEET_CLASS_SELECTION_PENDING.getKey(), false);
        MyApplication.saveData(PreferenceKeys.AUTHENTICATION_DONE.getKey(), true);

        String phone = MyApplication.getDoNotDeleteSharedPrefKey(
                PreferenceKeys.USER_ONBOARDING_LOGIN_PHONE_NUMBER.getKey());
        String userId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());

        Intent intent = new Intent(this, OnboardingActivityNew.class);
        intent.putExtra(PreferenceKeys.PHONE_NUMBER.getKey(), phone != null ? phone : "");
        intent.putExtra(PreferenceKeys.USER_ID.getKey(), userId != null ? userId : "");
        intent.putExtra(EXTRA_FROM_NEET_CLASS_SELECTION, true);
        intent.putExtra(EXTRA_BACK_ORIGIN, ORIGIN_BOARD_REDIRECT);
        intent.putExtra(EXTRA_BACK_CLASS_CODE, classCode);
        intent.putExtra(EXTRA_BACK_CLASS_LABEL, classLabel);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in_slide_in, R.anim.fade_out_slide_out);
        finish();
    }

    // ── Back press ──────────────────────────────────────────────────────────

    private void setupBackPress() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBack("android_system");
            }
        });
    }

    // ── Fragment callbacks: NeetClassSelectionFragment ───────────────────────

    @Override
    public void onNeetClassSelected(NeetClassOption option) {
        pendingSelection = option;

        if (option != null) {
            String value;
            if ("DROPPER".equalsIgnoreCase(option.getId())) {
                value = "DROPPER";
            } else if ("OTHER".equalsIgnoreCase(option.getId())) {
                value = "OTHER";
            } else {
                // "12th", "12", "CLASS_12" -> "12"
                String code = option.getClassCode();
                value = (code != null) ? code.replaceAll("[^0-9]", "") : "";
            }

            EventApiHelper.sendActionEvent(
                    ActionEventName.NEET_CLASS_SELECTED.getValue(),
                    null,
                    resolvePhoneForFetch(),
                    value,
                    ActionEventScreen.NEET_CLASS_SCREEN.getValue()
            );

            Map<String, String> params = new HashMap<>();
            params.put("class_selected", value);
            MyApplication.logFirebaseBundleCreation(
                    FirebaseEvents.onboarding_neet_class_selected.getActionName(),
                    params
            );
        }
    }

    @Override
    public void onForwardStateChanged(boolean enabled) {
        updateForwardState(enabled);
    }

    @Override
    public void onAutoNavigate() {
        if (pendingSelection != null && !isFinishing() && !isDestroyed()) {
            handleForward();
        }
    }

    // ── Fragment callbacks: NeetBoardRedirectFragment ────────────────────────

    @Override
    public void onBoardSelected(OnBoardingSelectionModel board) {
        launchOnboardingForBoard(board);

        EventApiHelper.sendActionEvent(
                ActionEventName.NEET_BOARD_SELECTED.getValue(),
                null,
                resolvePhoneForFetch(),
                board.getBoardCode(),
                ActionEventScreen.NEET_CLASS_UNAVAILABLE_SCREEN.getValue()
        );

        Map<String, String> params = new HashMap<>();
        params.put("board_selected", board.getBoardCode());
        MyApplication.logFirebaseBundleCreation(
                FirebaseEvents.onboarding_neet_board_selected.getActionName(),
                params
        );
    }

    // ── Resume flow v2 — state persistence ────────────────────────────────

    /**
     * Returns the {@link OnboardingScreenName} for the currently displayed fragment.
     */
    private OnboardingScreenName getCurrentScreenName() {
        if (NeetComingSoonFragment.TAG.equals(currentFragmentTag))
            return OnboardingScreenName.NEET_COMING_SOON_SCREEN;
        if (NeetBoardRedirectFragment.TAG.equals(currentFragmentTag))
            return OnboardingScreenName.NEET_BOARD_SCREEN;
        return OnboardingScreenName.NEET_GRADE_SCREEN; // default / class selection
    }

    /**
     * Builds a DTO for the current NEET screen with all available data.
     */
    private UserOnboardingStateDto buildNeetStateDto(OnboardingScreenName screenName) {
        String phone = MyApplication.getDoNotDeleteSharedPrefKey(
                PreferenceKeys.USER_ONBOARDING_LOGIN_PHONE_NUMBER.getKey());
        OnBoardingSelectionModel sel = PreferenceMethods.getSelectedOnboardingModel();

        UserOnboardingStateDto.UserOnboardingStateDtoBuilder builder =
                UserOnboardingStateDto.builder()
                        .phoneNumber(phone != null ? phone : "")
                        .stepName(screenName)
                        .selectionCode(sel != null ? sel.getId() : null)
                        .examCode(sel != null ? sel.getExamCode() : null);

        // For board redirect, store the class code/label so routing can work on resume
        if (screenName == OnboardingScreenName.NEET_BOARD_SCREEN
                && pendingSelection != null) {
            builder.neetGradeCode(pendingSelection.getClassCode());
            builder.neetGradeLabel(pendingSelection.getLabel());
        }

        return builder.build();
    }

    /**
     * Saves state locally + server (fire-and-forget).
     * Called on every screen transition within this activity.
     */
    private void saveNeetStateV2(OnboardingScreenName screenName) {
        if (!OnboardingStateApiHelper.isResumeFlowEnabled()) return;

        UserOnboardingStateDto state = buildNeetStateDto(screenName);
        PreferenceMethods.setOnboardingStateDto(state);
        OnboardingStateApiHelper.saveState(state, null);
    }

    // ── Locale forcing (always English) ─────────────────────────────────────

    private void forceEnglishLocale() {
        Locale english = Locale.ENGLISH;
        Locale.setDefault(english);
        Resources res = getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(english);
        res.updateConfiguration(config, res.getDisplayMetrics());
    }

    private Context createEnglishContext(Context context) {
        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(Locale.ENGLISH);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.createConfigurationContext(config);
        }
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
        return context;
    }
}
