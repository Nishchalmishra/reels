package arivihan.technologies.doubtbuzzter2.activity;
import arivihan.technologies.doubtbuzzter2.reelsdemo.*;
import arivihan.technologies.doubtbuzzter2.BuildConfig;

import static arivihan.technologies.doubtbuzzter2.enums.HOW_TO_TYPE.HOME_SCREEN;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.Locale;

import arivihan.technologies.doubtbuzzter2.activity.onboarding.NeetClassSelectionActivity;
import arivihan.technologies.doubtbuzzter2.enums.onboarding.OnboardingScreenName;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceMethods;
import arivihan.technologies.doubtbuzzter2.helper.onboarding.OnboardingStateApiHelper;
import arivihan.technologies.doubtbuzzter2.helper.refer.ReferralLinkHandler;
import arivihan.technologies.doubtbuzzter2.model.OnBoardingSelectionModel;
import arivihan.technologies.doubtbuzzter2.model.onboarding.UserOnboardingStateDto;
import io.branch.referral.Branch;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.BundleParamConstants;
import arivihan.technologies.doubtbuzzter2.enums.event.ActionEventName;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.fragments.IntroSplashScreenFragment;
import arivihan.technologies.doubtbuzzter2.fragments.authentication.LogInWIthMobileNumberFragment;
import arivihan.technologies.doubtbuzzter2.helper.BranchAnalyticsHelper;
import arivihan.technologies.doubtbuzzter2.helper.NewNotificationHelper;
import arivihan.technologies.doubtbuzzter2.listener.FragmentChangeListener;
import arivihan.technologies.doubtbuzzter2.volley.EventApiHelper;

public class StartActivity extends LocaleActivity implements FragmentChangeListener {

    private static final String TAG = StartActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.REELS_DEMO) { DemoEnvironment.launch(this); return; }
//        new Handler(Looper.getMainLooper()).post(() -> {
//            FacebookSdk.sdkInitialize(getApplicationContext());
//        });
        if (!FacebookSdk.isInitialized()) {
            FacebookSdk.setAutoInitEnabled(true);
            FacebookSdk.fullyInitialize();
        }
//        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication());
        if (!isTaskRoot()) {
            finish();
            return;
        }
        setContentView(R.layout.activity_start);


        ReferralLinkHandler.checkInstallReferrer(getApplicationContext());

        Log.d("launcherBug", "onCreateCalled.");
        setLocale();

//        if (getIntent().getBooleanExtra("isOpenWelcomeFragment", false)) {
//            Bundle bundle = getIntent().getExtras();
//            WelcomeToArivihanFragment fragment = new WelcomeToArivihanFragment();
//            fragment.setArguments(bundle);
//
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.starting_frame, fragment)
//                    .commit();
//        } else {
//            try {
//                checkPendingIntent();
//            } catch (Exception e) {
//
//            }
//        }
        checkPendingIntent();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (BuildConfig.REELS_DEMO) return;
        // Branch SDK: initialise a session on every foreground so deep-link data
        // (referring params) is delivered. Recommended to run in onStart().
        Branch.sessionBuilder(this)
                .withCallback((Branch.BranchReferralInitListener) (referringParams, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Branch init failed: " + error.getMessage());
                    } else {
                        Log.i(TAG, "Branch init complete: "
                                + (referringParams != null ? referringParams.toString() : "no params"));
                        // Users who logged in before setIdentity existed (or reinstalled)
                        // get their Branch identity attached on next launch.
                        BranchAnalyticsHelper.setUserIdentityFromPrefs();
                        handleBranchDeepLink(referringParams);
                    }
                })
                .withData(getIntent() != null ? getIntent().getData() : null)
                .init();
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        // If this launcher (singleTask) is re-launched from a Branch link, force a
        // fresh session so the new referring params are delivered to the callback.
        setIntent(intent);
        if (intent.getBooleanExtra("branch_force_new_session", false)) {
            Branch.sessionBuilder(this)
                    .withCallback((Branch.BranchReferralInitListener) (referringParams, error) -> {
                        if (error != null) {
                            Log.e(TAG, "Branch reInit failed: " + error.getMessage());
                        } else {
                            handleBranchDeepLink(referringParams);
                        }
                    })
                    .reInit();
        }
    }

    /**
     * Reads deep-link data returned by Branch. When a "$deeplink_path" (or custom
     * "deepLink") key is present, it is forwarded to the existing app-wide deep-link
     * handling by stashing it on the intent so the normal launch flow can pick it up.
     */
    private void handleBranchDeepLink(JSONObject referringParams) {
        if (referringParams == null) return;
        try {
            String deepLink = referringParams.optString("deepLink",
                    referringParams.optString("$deeplink_path", ""));
            if (StringUtils.isNotEmpty(deepLink)) {
                getIntent().putExtra("deepLink", deepLink);
            }
        } catch (Exception e) {
            Log.e(TAG, "handleBranchDeepLink: " + e.getMessage());
        }
    }

    private void setLocale() {
        String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        if (lang == null || lang.isEmpty()) lang = "en";
        Log.d("langcode", lang);

        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
    }


    private void checkPendingIntent() {
        NewNotificationHelper.ensureChannel(this);

        // If a notification message is tapped, any data accompanying the notification
        // message is available in the intent extras. In this sample the launcher
        // intent is fired when the notification is tapped, so any accompanying data would
        // be handled here. If you want a different intent fired, set the click_action
        // field of the notification message to the desired intent. The launcher intent
        // is used when no click_action is specified.
        //
        // Handle possible data accompanying notification message.
        // [START handle_data_extras]
        String deepLink = getDeepLink();
        setFragment(new IntroSplashScreenFragment(deepLink));


    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("launcherBug", "onPauseCalled.");

    }

    @Override
    protected void onResume() {
        super.onResume();
        counterBegin();
    }

    @NonNull
    private String getDeepLink() {
        String deepLink = "";
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                if (key.equalsIgnoreCase("deepLink")) {

                    deepLink = value.toString();
                }
            }
        }
        return deepLink;
    }

    /**
     * Persists a pending deep link so it survives the login/onboarding flow (which spans
     * several activities before finally reaching HomeScreen). HomeScreen.checkPendingIntent()
     * reads and clears it once the user actually lands there.
     */
    private void stashPendingDeepLinkIfPresent() {
        String pendingDeepLink = getDeepLink();
        if (StringUtils.isNotEmpty(pendingDeepLink)) {
            MyApplication.saveDataForDoNotDeleteSharedPreference(
                    PreferenceKeys.PENDING_DEEP_LINK.getKey(), pendingDeepLink);
        }
    }

    /**
     * Checks local v2 onboarding state and routes to the correct activity.
     *
     * Validation rules:
     * 1. Resume flow must be enabled via remote config
     * 2. Local state must exist with a valid stepName
     * 3. selectionCode in state must match current OnBoardingSelectionModel.id
     *    (prevents resuming stale state from a different exam selection)
     *
     * Routing:
     * - NEET screens (NEET_GRADE/BOARD/COMING_SOON with NEET selectionCode)
     *   → NeetClassSelectionActivity
     * - All other screens → OnboardingActivityNew (handles step internally)
     *
     * @return true if routed successfully, false to fall through to legacy logic
     */
    private boolean routeByResumeStateV2() {
        if (!OnboardingStateApiHelper.isResumeFlowEnabled()) return false;

        UserOnboardingStateDto localState = PreferenceMethods.getOnboardingStateDto();
        if (localState == null || localState.getStepName() == null) return false;

        // ── Validate selectionCode matches current exam selection ────
        String currentSelCode = PreferenceMethods.getSelectedOnBoardingId();
        String stateSelCode = localState.getSelectionCode();

        if (currentSelCode == null || stateSelCode == null
                || !currentSelCode.equals(stateSelCode)) {
            Log.d(TAG, "routeByResumeStateV2: selectionCode mismatch"
                    + " current=" + currentSelCode + " state=" + stateSelCode
                    + " → clearing stale state");
            PreferenceMethods.clearOnboardingStateDto();
            return false;
        }

        Log.d(TAG, "routeByResumeStateV2: resuming at " + localState.getStepName()
                + " selectionCode=" + stateSelCode);

        // ── Determine target activity ────────────────────────────────
        OnboardingScreenName step = localState.getStepName();
        boolean isNeetActivityScreen =
                "NEET".equalsIgnoreCase(stateSelCode)
                && (step == OnboardingScreenName.NEET_GRADE_SCREEN
                    || step == OnboardingScreenName.NEET_COMING_SOON_SCREEN
                    || step == OnboardingScreenName.NEET_BOARD_SCREEN);

        if (isNeetActivityScreen) {
            Intent intent = new Intent(this, NeetClassSelectionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            // NAME_SCREEN, LANGUAGE_SCREEN, GRADE_LEVEL_SCREEN, STREAM_SCREEN,
            // or NEET_BOARD_SCREEN for non-NEET (Board step)
            Intent intent = new Intent(this, OnboardingActivityNew.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        return true;
    }

    private void counterBegin() {
        Log.d("launcherBug", "counterBegin called");


        Handler handler = new Handler(Looper.getMainLooper());
        long delayMillis = 1000; // 1-second delay
        boolean[] handlerExecuted = {false}; // Use an array to make it mutable in inner classes

        // Delayed execution
        handler.postDelayed(() -> {
            Log.d("launcherBug", "postDelayed called");
            handlerExecuted[0] = true; // Mark as executed

            String number = MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey());
            boolean isLoggedInUser =
                    MyApplication.getBoolData(PreferenceKeys.USER_LOGGED_IN.getKey());
            boolean isAuthDone =
                    MyApplication.getBoolData(PreferenceKeys.AUTHENTICATION_DONE.getKey());
            Log.d("LauncherDebug", "name entered " + MyApplication.getStringData(PreferenceKeys.USER_NAME_ENTERED_DURING_ONBOARDING.getKey()));

            if (isAuthDone) {
                // Mid-onboarding resume: stash deep link for replay after registration
                stashPendingDeepLinkIfPresent();

                // ── v2 resume flow: route by saved state ─────────────
                if (routeByResumeStateV2()) return;

                // ── Fallback: existing flag-based routing ────────────
                if (MyApplication.getBoolData(PreferenceKeys.NEET_CLASS_SELECTION_PENDING.getKey())) {
                    Intent neetIntent = new Intent(this, NeetClassSelectionActivity.class);
                    neetIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(neetIntent);
                    finish();
                } else {
                    Intent homeIntent = new Intent(this, OnboardingActivityNew.class);
                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(homeIntent);
                    finish();
                }

            } else {

                if (number == null) {

                    Log.d("LauncherDebug",
                            "User not authenticated. Navigating to UserAuthenticationActivity");

                    // Not logged in yet: stash any pending deep link so it can be replayed
                    // once the user finishes login/onboarding and reaches HomeScreen.
                    stashPendingDeepLinkIfPresent();

                    Intent intent = new Intent(this, UserAuthenticationActivity.class);
                    intent.putExtra("NewUser", false);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                } else {

                    Log.d("LauncherDebug",
                            "User data available. Calling processFurther()");
                    processFurther(number);
                }
            }

        }, delayMillis);

        // Fallback mechanism
        handler.postDelayed(() -> {

            if (!handlerExecuted[0]) {

                Log.e("LauncherDebug",
                        "Fallback triggered: Primary handler did not execute in time");

                fallbackAction();
            } else {
                Log.d("LauncherDebug",
                        "Fallback check: Primary handler already executed successfully");
            }

        }, delayMillis + 500);
    }

    private void processFurther(String number) {

        Log.d("LauncherDebug", "processFurther() called with number: " + number);

        if (number == null) {

            Log.d("LauncherDebug",
                    "Number is null. Loading LogInWIthMobileNumberFragment");

            setFragment(new LogInWIthMobileNumberFragment());

        } else {

            Log.d("LauncherDebug",
                    "Number is available. Proceeding to goTo()");
            goTo();
        }
    }


    private void goTo() {
        int launchCount = MyApplication.getIntData(PreferenceKeys.LAUNCH_COUNT.getKey());
        if (launchCount < 0)
            launchCount = 0;
        launchCount++;
        MyApplication.saveData(PreferenceKeys.LAUNCH_COUNT.getKey(), launchCount);



        if (StringUtils.isNotEmpty(getDeepLink())) {
            // A Branch/App-Link deep link is pending: skip the onboarding/promo funnel
            // (how-to videos, refer posters, notification popups) and go straight to it.
            goToHomeScreen();
            return;
        }

        // ---: below is temporary solution - remove this after revamp :---
        boolean shouldReset = MyApplication.getFirebaseRemoteConfig().getBoolean("home_screen_reset_launch_counter");
        boolean resetDone = MyApplication.getBoolData(PreferenceKeys.RESET_LAUNCH_COUNT.getKey());

        Log.d(TAG, "LaunchCount: " + launchCount + ", shouldReset: " + shouldReset + ", ResetDone: " + resetDone);
        if (shouldReset && !resetDone && (launchCount > 8 || launchCount == 1)) {
            MyApplication.removeString(HOME_SCREEN.name() + "_COUNT" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
            MyApplication.saveData(PreferenceKeys.LAUNCH_COUNT.getKey(), 1);
            MyApplication.saveData(PreferenceKeys.RESET_LAUNCH_COUNT.getKey(), true);
        }
        switch (launchCount) {
            case 1 -> {
                String classId = MyApplication.getStringData(PreferenceKeys.CLASS_ID.getKey());
                if ("5".equals(classId) || "6".equals(classId)) openNewSessionPopup();
                else openHowTo();
            }
            case 2, 3 -> openHowTo();
            case 4 -> goToReferPoster(launchCount);
            case 5, 6 -> goToPosterPopUP(launchCount);
            case 7, 8 -> goToNotificationPopup();
            default -> goToHomeScreen();
        }
    }

    private void goToReferPoster(int launchCount) {
        try {
            int count = MyApplication.getIntData(PreferenceKeys.REFER_POSTER_COUNT.getKey());
            if (count < 0) count = 0;
            count++;

            if (MyApplication.getFirebaseRemoteConfig().getBoolean("refer_win_poster_pop_visibility") && count <= 2) {
                Intent intent = new Intent(this, HomeScreen.class);
                intent.putExtra("redirect_to_home", false);
                intent.putExtra("open_refer_win_poster_popup", true);
                intent.putExtra(BundleParamConstants.LOCATION_SHOW_LOCATION_PERMISSION, true);
                intent.putExtra("from_app_open", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                goTo();
            }
            MyApplication.saveData(PreferenceKeys.REFER_POSTER_COUNT.getKey(), count);
        } catch (Exception ignored) {
        }
    }

    private void goToPosterPopUP(int launchCount) {
        try {
            int count = MyApplication.getIntData(getString(R.string.poster_count));
            if (count < 0) count = 0;
            count++;

            Log.d(TAG, "Welcome Sequence: Count Popup: " + count);
            if (count <= 2) {
                Intent intent = new Intent(this, HomeScreen.class);
                intent.putExtra("redirect_to_home", false);
                intent.putExtra("open_poster_popup", true);
                intent.putExtra(BundleParamConstants.LOCATION_SHOW_LOCATION_PERMISSION, true);
                intent.putExtra("from_app_open", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                Log.d(TAG, "Welcome Sequence: " + intent.getExtras().toString());
                finish();
            } else {
                goTo();
            }
            MyApplication.saveData(getString(R.string.poster_count), count);
        } catch (Exception ignored) {
        }
    }


    public void openNewSessionPopup() {
        Log.d(TAG, "openNewSessionPopup: ");
        try {
            Intent intent = new Intent(this, HomeScreen.class);
            intent.putExtra("NewUser", false);
            intent.putExtra(BundleParamConstants.LOCATION_SHOW_LOCATION_PERMISSION, true);
            intent.putExtra("from_app_open", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("open_new_session_popup", true);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e("openHowTo", e.getMessage());
        }

    }

    public void openHowTo() {
        try {
            int count = MyApplication.getIntData(HOME_SCREEN.name() + "_COUNT" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
            if (count < 0)
                count = 0;
            count = count + 1;
            int maxShowCount = (int) MyApplication.getFirebaseRemoteConfig().getLong("how_to_video_max_show_count");
            if (maxShowCount <= 0) {
                maxShowCount = 2;
            }
            if (count <= maxShowCount) {
                Intent intent = new Intent(this, HomeScreen.class);
                intent.putExtra("NewUser", false);
                intent.putExtra(BundleParamConstants.LOCATION_SHOW_LOCATION_PERMISSION, true);
                intent.putExtra("from_app_open", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("open_how_to_video", true);
                MyApplication.saveData(HOME_SCREEN.name() + "_COUNT" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()), count);
                startActivity(intent);
                finish();
            } else {
                goTo();
            }
        } catch (Exception e) {
            Log.e("openHowTo", e.getMessage());
        }

    }


    public void goToNotificationPopup() {
        try {
            int count = MyApplication.getIntData(getString(R.string.Notification_Permission_Popup_Count));
            if (count < 0)
                count = 0;
            count = count + 1;
            if (count <= 2) {
                Intent intent = new Intent(this, HomeScreen.class);
                intent.putExtra("redirect_to_home", true);
                intent.putExtra("open_notification_popup", true);
                intent.putExtra(BundleParamConstants.LOCATION_SHOW_LOCATION_PERMISSION, true);
                intent.putExtra("from_app_open", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                MyApplication.saveData(getString(R.string.Notification_Permission_Popup_Count), count);
                finish();
            } else {
                goTo();
            }
        } catch (Exception ignored) {
        }
    }

    private void fallbackAction() {
        // Fallback logic here, such as showing an error dialog, retrying, etc.
        Log.d("launcherBug", "Executing fallback action");
        // You might want to retry, navigate to a safe screen, or log an error
        String number = MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey());

        if (number == null) {
            Log.d("launcherBug", "number is null, setting LogInWIthMobileNumberFragment");
            setFragment(new LogInWIthMobileNumberFragment());
        } else {
            Log.d("launcherBug", "number is not null, processing further");
            // Rest of your logic
            goToHomeScreen();
        }
    }

    public void goToHomeScreen() {
        String deepLink = getDeepLink();
        Intent intent = new Intent(this, HomeScreen.class);
        MyApplication.saveData(PreferenceKeys.REFRESH.getKey(), true);
        intent.putExtra("NewUser", false);
        intent.putExtra(BundleParamConstants.LOCATION_SHOW_LOCATION_PERMISSION, true);
        intent.putExtra("from_app_open", true);

        if (StringUtils.isNotEmpty(deepLink))
            intent.putExtra("deeplink", deepLink);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        try {
            if (getApplicationContext() != null) {
                startActivity(intent);
                finish();
            }
        } catch (ActivityNotFoundException e) {
            Log.e("HomeScreen", "Activity not found: " + e.getMessage());
        }
    }

    public void setFragment(Fragment fragment) {
        runOnUiThread(() -> {
            if (!isFinishing() && !isDestroyed()) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                if (!fragmentManager.isDestroyed()) {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.starting_frame, fragment);
                    fragmentTransaction.commitAllowingStateLoss();
                }
            }
        });
    }

    @Override
    public void setPhoneNumber(String number) {

    }

    @Override
    public void showPhoneNumberPopUp() {

    }

    @Override
    public void setName(String name) {

    }

    @Override
    public void setClass(String className) {

    }

    @Override
    public void setCourse(String course) {

    }

    @Override
    public void setExam(String exam) {

    }

    @Override
    public void registered(boolean registered) {

    }

    @Override
    public void moveToHomeScreen() {
        Intent intent = new Intent(StartActivity.this, HomeScreen.class);
        intent.putExtra("from_app_open", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

    }

    @Override
    public void setCurrentDefaultLocaleStr(String lang) {

    }

    @Override
    public void playAnimation() {

    }

    @Override
    public void finishActivity() {

    }

    @Override
    public void moveToOnBoardingActivity(String phoneNumberToLoginWith, String email, String id) {

    }
}