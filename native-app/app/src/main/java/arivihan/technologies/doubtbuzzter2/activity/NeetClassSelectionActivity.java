package arivihan.technologies.doubtbuzzter2.activity;

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
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceMethods;
import arivihan.technologies.doubtbuzzter2.fragments.authentication.NeetBoardRedirectFragment;
import arivihan.technologies.doubtbuzzter2.fragments.authentication.NeetClassSelectionFragment;
import arivihan.technologies.doubtbuzzter2.fragments.authentication.NeetComingSoonFragment;
import arivihan.technologies.doubtbuzzter2.model.NeetClassOption;
import arivihan.technologies.doubtbuzzter2.model.OnBoardingSelectionModel;
import arivihan.technologies.doubtbuzzter2.utils.AudioManager;

/**
 * NEET Class Selection Activity.
 * Always English. Hosts class selection + Board redirect / Coming Soon fragments.
 * Speaker/volume button for audio playback (same as OnboardingActivityNew).
 * Back hidden on first screen, forward disabled until selection.
 */
public class NeetClassSelectionActivity extends AppCompatActivity
        implements NeetClassSelectionFragment.OnNeetClassActionListener,
                   NeetBoardRedirectFragment.OnBoardRedirectListener {

    private static final String TAG = "NeetClassSelectionActivity";
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
        if (btnBack != null) btnBack.getHandler().removeCallbacksAndMessages(null);
        AudioManager.getInstance().release();
        super.onDestroy();
    }

    // ── Fragment management ─────────────────────────────────────────────────

    /**
     * On launch, checks if we're returning from OnboardingActivityNew via back.
     * If so, restores the correct screen (board redirect with class context).
     * Otherwise, shows class selection as the initial screen.
     */
    private void restoreOrShowInitial() {
        String origin = getIntent().getStringExtra(EXTRA_BACK_ORIGIN);

        if (ORIGIN_BOARD_REDIRECT.equals(origin)) {
            // Returning from OnboardingNew that was launched via board redirect
            String classCode = getIntent().getStringExtra(EXTRA_BACK_CLASS_CODE);
            String classLabel = getIntent().getStringExtra(EXTRA_BACK_CLASS_LABEL);

            // Restore NEET as selected model (was switched to board before launching OnboardingNew)
            PreferenceMethods.setSelectedOnBoardingModelById("NEET");

            // Show board redirect without forward animation (user is coming back)
            replaceFragment(
                    NeetBoardRedirectFragment.newInstance(
                            classCode != null ? classCode : "",
                            classLabel != null ? classLabel : ""),
                    NeetBoardRedirectFragment.TAG,
                    DIR_NONE);
            btnBack.setVisibility(View.VISIBLE);
            updateForwardState(false);
        } else {
            showClassSelectionInitial();
        }
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
    }

    private void showComingSoon() {
        toolbar.setVisibility(View.GONE);
        replaceFragment(NeetComingSoonFragment.newInstance(), NeetComingSoonFragment.TAG, DIR_FORWARD);
        updateButtonsDelayed(() -> {
            btnBack.setVisibility(View.VISIBLE);
            btnForward.setVisibility(View.INVISIBLE);
        });
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
        btnBack.setOnClickListener(v -> handleBack());
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

    private void handleBack() {
        if (isFinishing() || isDestroyed()) return;
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
                handleBack();
            }
        });
    }

    // ── Fragment callbacks: NeetClassSelectionFragment ───────────────────────

    @Override
    public void onNeetClassSelected(NeetClassOption option) {
        pendingSelection = option;
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
