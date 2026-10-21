package arivihan.technologies.doubtbuzzter2.activity;
import arivihan.technologies.doubtbuzzter2.reelsdemo.*;
import arivihan.technologies.doubtbuzzter2.BuildConfig;

import static arivihan.technologies.doubtbuzzter2.activity.HomeScreen.BroadcastStringForAction;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_TRUECALLER_USER_INFO;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_GET_USER_BY_PHONE;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.truecaller.android.sdk.oAuth.CodeVerifierUtil;
import com.truecaller.android.sdk.oAuth.OAuthResponse;
import com.truecaller.android.sdk.oAuth.TcOAuthCallback;
import com.truecaller.android.sdk.oAuth.TcOAuthData;
import com.truecaller.android.sdk.oAuth.TcOAuthError;
import com.truecaller.android.sdk.oAuth.TcSdk;
import com.truecaller.android.sdk.oAuth.TcSdkOptions;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.onboarding.NeetClassSelectionActivity;
import arivihan.technologies.doubtbuzzter2.common.BundleParamConstants;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.event.ActionEventName;
import arivihan.technologies.doubtbuzzter2.enums.event.ActionEventScreen;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceMethods;
import arivihan.technologies.doubtbuzzter2.fragments.authentication.LogInWIthMobileNumberFragment;
import arivihan.technologies.doubtbuzzter2.fragments.authentication.LoginCourseSelectionFragment;
import arivihan.technologies.doubtbuzzter2.fragments.authentication.OnBoardingUserLocationFragment;
import arivihan.technologies.doubtbuzzter2.fragments.authentication.VerifyNumberFragment;
import arivihan.technologies.doubtbuzzter2.fragments.authentication.WelcomeToArivihanFragment;
import arivihan.technologies.doubtbuzzter2.helper.firebase.FirebaseMessagingManager;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.helper.slack.SlackOnboardingAlert;
import arivihan.technologies.doubtbuzzter2.listener.FragmentChangeListener;
import arivihan.technologies.doubtbuzzter2.listener.onboarding.OnBoardingUIListener;
import arivihan.technologies.doubtbuzzter2.model.ArivihanLanguageEnum;
import arivihan.technologies.doubtbuzzter2.model.TruecallerResponseDto;
import arivihan.technologies.doubtbuzzter2.model.batch.user.UserDetailsResponse;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.volley.EventApiHelper;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper;

public class UserAuthenticationActivity extends LocaleActivity
        implements FragmentChangeListener, OnBoardingUIListener, EdgeToEdgeHelper.SelfManagedInsets {
    private static final String TAG = UserAuthenticationActivity.class.getSimpleName();
    FrameLayout ParentLayout;
    private IntentFilter filter;
    SlackOnboardingAlert slackOnboardingAlert;
    private ActivityResultLauncher<IntentSenderRequest> showPhoneNumberHintPopUP;

    private ActivityResultLauncher<Intent> trueCallerLauncher;

    private final ArrayList<Object> imageUrls = new ArrayList<>(Arrays.asList(
            R.drawable.on_boarding_image_1,
            R.drawable.on_boarding_image_2,
            R.drawable.on_boarding_image_3
    ));
    private LottieAnimationView celebration;
    private ImageView imageView1;
    private ImageView imageView2;
    private View gradientView;
    private int currentImageIndex = 0;
    //    private Handler imageHandler = new Handler();
    private Runnable imageRunnable;
    private Handler handler = new Handler();
    private final int SLIDESHOW_INTERVAL = 7000;
    NetworkStatusLiveData networkStatusLiveData;
    private ExoPlayer player;
    PlayerView playerView;
    FrameLayout imageSliderContainer;
    private MaterialCardView cv_back;
    private long lastBackPressedTime = 0;
    private String currentFragmentClassName, previousFragmentClassName;
    private boolean isSlideshowRunning = false;
    private Bundle welcomeFragmentBundle;

    private boolean loginSuggestionBottomOpened = false;

    String codeVerifier = CodeVerifierUtil.Companion.generateRandomCodeVerifier();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_authentication_layout);
        // Edge-to-edge with adjustPan: only nav bar inset for starting_frame
        // (not keyboard). adjustPan handles keyboard by sliding the window up.
        EdgeToEdgeHelper.applyArtworkEdgeToEdgeAdjustPan(
                findViewById(android.R.id.content),
                new View[] {findViewById(R.id.starting_frame)},
                new View[] {findViewById(R.id.cv_back)});
        playerView = findViewById(R.id.video_player);
        imageSliderContainer = findViewById(R.id.image_slider_container);
        celebration = findViewById(R.id.celebration);
        cv_back = findViewById(R.id.cv_back);

        imageView1 = findViewById(R.id.imageView1);
        imageView2 = findViewById(R.id.imageView2);
        gradientView = findViewById(R.id.view);

        // The transparent status bar with dark icons comes from EdgeToEdgeHelper, applied to
        // every activity. Setting the legacy system UI flags here would undo it.

        if (!BuildConfig.REELS_DEMO) {
        TcSdkOptions tcSdkOptions = new TcSdkOptions.Builder(this, new TcOAuthCallback() {
            @Override
            public void onVerificationRequired(@Nullable TcOAuthError tcOAuthError) {
                Log.i("TRUECALLER_USER", "USER_DETAIL_ERROR ");

            }

            @Override
            public void onFailure(@NonNull TcOAuthError tcOAuthError) {
                Log.i("TRUECALLER_USER", "USER_DETAIL_FAILURE ");

            }

            @Override
            public void onSuccess(@NonNull TcOAuthData tcOAuthData) {
                Log.i("TRUECALLER_USER", "USER_DETAIL kjh:: " + tcOAuthData);

            }
        })
                .buttonColor(0xFF26C6DA)
                .buttonTextColor(0x00000000)
                .sdkOptions(TcSdkOptions.OPTION_VERIFY_ONLY_TC_USERS)
                .loginTextPrefix(TcSdkOptions.LOGIN_TEXT_PREFIX_TO_GET_STARTED)
                .ctaText(TcSdkOptions.CTA_TEXT_CONTINUE)
                .buttonShapeOptions(TcSdkOptions.BUTTON_SHAPE_ROUNDED)
                .footerType(TcSdkOptions.FOOTER_TYPE_SKIP)
                .consentTitleOption(TcSdkOptions.SDK_CONSENT_HEADING_SIGN_IN_TO)
                .build();

        new Thread(() -> TcSdk.init(tcSdkOptions)).start();
        }

        ParentLayout = findViewById(R.id.starting_frame);
        filter = new IntentFilter();
        slackOnboardingAlert = new SlackOnboardingAlert(this);
        filter.addAction(BroadcastStringForAction);
        Log.d("launcherBug", "onCreateCalled.");
        MyApplication.saveData(PreferenceKeys.EXPLORE_ACTIVITY.getKey(), null);
        setLocale();

        Map<String, String> params = new HashMap<>();
        params.put("Steps", "log_in_screen");
        try {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.User_onBoarding_process.getActionName(), params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean isBackFromOnboarding =
                getIntent().getBooleanExtra("backFromOnboardingActivityNew", false);

        if (StringUtils.isNotEmpty(PreferenceMethods.getSelectedOnBoardingId())) {
            if ((MyApplication.getBoolData(PreferenceKeys.AUTHENTICATION_DONE.getKey())
                    && StringUtils.isEmpty(MyApplication.
                    getStringData(PreferenceKeys.USER_NAME_ENTERED_DURING_ONBOARDING.getKey()))) || isBackFromOnboarding) {
                setFragment(new WelcomeToArivihanFragment());
                startImageSlideshow();
            } else {
                setFragment(new LogInWIthMobileNumberFragment());
            }
        } else {
            setFragment(new LoginCourseSelectionFragment());
        }
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.login_screen_viewed.getActionName());

        cv_back.setOnClickListener(v -> {
            onBackPressedClick(false, "UI_BACK");
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBackPressedClick(true, "NATIVE");
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            if (isOnline) {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    CommonMethods.showPositiveSnackbar(ParentLayout, MyApplication.getInstance().getString(R.string.InternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
                }
            } else {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                    CommonMethods.showNegativeSnackbar(ParentLayout, MyApplication.getInstance().getString(R.string.NoInternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), false);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), true);
                }
            }
        });

//        trueCallerLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    loginSuggestionBottomOpened = true;
//
//                    if (result.getResultCode() == RESULT_OK) {
//                        Bundle extras = result.getData().getExtras();
//                        if (extras != null) {
//                            Object response = extras.get("OAUTH_SDK_RESPONSE_EXTRA");
//                            if (response instanceof OAuthResponse.SuccessResponse) {
//                                checkTrueCallerUserDetails(codeVerifier, ((OAuthResponse.SuccessResponse) response).getTcOAuthData().getAuthorizationCode());
//                            }
//                        }
//                    } else {
//
//                    }
//                });

        trueCallerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    loginSuggestionBottomOpened = true;

                    if (result.getResultCode() == RESULT_OK) {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null) {
                            Object response = extras.get("OAUTH_SDK_RESPONSE_EXTRA");
                            if (response instanceof OAuthResponse.SuccessResponse) {

                                // user clicked Continue
                                Map<String, String> eventParams = new HashMap<>();
                                eventParams.put(PreferenceKeys.COURSE_NAME.getKey(), PreferenceMethods.getSelectedOnBoardingId());
                                MyApplication.logFirebaseBundleCreation(FirebaseEvents.truecaller_continue_btn_click.getActionName(), eventParams);

                                checkTrueCallerUserDetails(codeVerifier, ((OAuthResponse.SuccessResponse) response).getTcOAuthData().getAuthorizationCode());
                            }
                        }
                    } else {

                        // user clicked Skip/Dismissed
                        Map<String, String> eventParams = new HashMap<>();
                        eventParams.put(PreferenceKeys.COURSE_NAME.getKey(), PreferenceMethods.getSelectedOnBoardingId());
                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.truecaller_skip_btn_click.getActionName(), eventParams);

                    }
                });

        showPhoneNumberHintPopUP = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
            loginSuggestionBottomOpened = true;
            try {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String number = Identity.getSignInClient(this)
                            .getPhoneNumberFromIntent(result.getData());

                    Map<String, String> eventParams = new HashMap<>();
                    eventParams.put(PreferenceKeys.COURSE_NAME.getKey(), PreferenceMethods.getSelectedOnBoardingId());
                    eventParams.put("phone", number != null ? number : "");
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.phone_sheet_number_selected.getActionName(), eventParams);

                    if (number != null && number.length() >= 10) {
                        String last10 = number.substring(number.length() - 10);
                        EventApiHelper.sendActionEvent(ActionEventName.AUTH_INITIATED.getValue(),
                                PreferenceMethods.getSelectedOnBoardingId(), last10, "ANDROID_HINT", ActionEventScreen.PHONE_SCREEN.getValue());

                        LogInWIthMobileNumberFragment fragment =
                                (LogInWIthMobileNumberFragment) getSupportFragmentManager()
                                        .findFragmentById(R.id.starting_frame);

                        if (fragment != null) {
                            fragment.onPhoneNumberReceived(last10);
                        }
                    }
                } else {
//                    result.getResultCode() == RESULT_CANCELED
                    // user closed/dismissed the popup
                    Map<String, String> eventParams = new HashMap<>();
                    eventParams.put(PreferenceKeys.COURSE_NAME.getKey(), PreferenceMethods.getSelectedOnBoardingId());
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.phone_sheet_number_cross_click.getActionName(), eventParams);

                }

            } catch (ApiException e) {

                Map<String, String> eventParams = new HashMap<>();
                eventParams.put(PreferenceKeys.COURSE_NAME.getKey(), PreferenceMethods.getSelectedOnBoardingId());
                eventParams.put("error", e.getMessage() != null ? e.getMessage() : "");
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.phone_sheet_number_cross_click.getActionName(), eventParams);

                if (e.getStatusCode() == CommonStatusCodes.CANCELED) {
                    Log.d("PhoneHint", "User cancelled phone number selection");
                } else {
                    Log.e("PhoneHint", "API Exception: " + e.getStatusCode(), e);
                }
            } catch (Exception e) {
                Log.e("PhoneHint", "Unexpected error", e);
                e.printStackTrace();
            }
        });

        getSupportFragmentManager().registerFragmentLifecycleCallbacks(
                new FragmentManager.FragmentLifecycleCallbacks() {
//                    @Override
//                    public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
//                        if (!(f instanceof LogInWIthMobileNumberFragment)) {
//                            Log.d(TAG, "--> Assigning the fragment"+loginSuggestionBottomOpened);
//                            loginSuggestionBottomOpened = false;
//                        }
//                    }

                    @Override
                    public void onFragmentCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @androidx.annotation.Nullable Bundle savedInstanceState) {
                        super.onFragmentCreated(fm, f, savedInstanceState);
                        if (!(f instanceof LogInWIthMobileNumberFragment)) {
                            loginSuggestionBottomOpened = false;
                        }
                    }
                }, true);

        try {
            FirebaseMessagingManager.getFcmToken(this, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void onBackPressedClick(boolean isBackPressed, String source) {
        try {
            if (StringUtils.isNotEmpty(currentFragmentClassName)) {
                // OTP screen → Phone Number screen (BACK)
                if (currentFragmentClassName.equals(VerifyNumberFragment.TAG)) {
                    setFragmentBack(new LogInWIthMobileNumberFragment());
                    Map<String, String> params = new HashMap<>();
                    params.put("back_type", source);
                    params.put("screen", "otp");
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.onboarding_otp_back_btn_click.getActionName(), params);
                    return;
                }

                // Phone Number screen → Goal Selection (BACK, no animation to avoid layout glitch)
                if (currentFragmentClassName.equals(LogInWIthMobileNumberFragment.TAG)) {
                    setFragmentNoAnim(new LoginCourseSelectionFragment());
                    Map<String, String> params = new HashMap<>();
                    params.put("back_type", source);
                    String phoneForMobileBack = MyApplication.getDoNotDeleteSharedPrefKey(PreferenceKeys.USER_ONBOARDING_LOGIN_PHONE_NUMBER.getKey());
                    params.put(PreferenceKeys.USER_ONBOARDING_LOGIN_PHONE_NUMBER.getKey(),
                            phoneForMobileBack != null ? phoneForMobileBack : "");
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.onboarding_mobile_back_btn_click.getActionName(), params);
                    return;
                }

                // Location → Welcome (BACK)
                if (currentFragmentClassName.equals(OnBoardingUserLocationFragment.TAG)) {
                    WelcomeToArivihanFragment welcomeToArivihanFragment = new WelcomeToArivihanFragment();
                    welcomeToArivihanFragment.setArguments(welcomeFragmentBundle);
                    setFragmentBack(welcomeToArivihanFragment);

                    Map<String, String> params = new HashMap<>();
                    params.put("type", source);
                    String phoneForLocationBack = MyApplication.getDoNotDeleteSharedPrefKey(PreferenceKeys.USER_ONBOARDING_LOGIN_PHONE_NUMBER.getKey());
                    params.put(PreferenceKeys.USER_ONBOARDING_LOGIN_PHONE_NUMBER.getKey(),
                            phoneForLocationBack != null ? phoneForLocationBack : "");
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.user_onboarding_location_back_button_click.getActionName(), params);
                    return;
                }
            }

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastBackPressedTime < MyApplication.BACK_PRESS_INTERVAL) {
                finishAffinity();
            } else {
                lastBackPressedTime = currentTime;
                Toast.makeText(this, getString(R.string.click_to_exit), Toast.LENGTH_SHORT).show();

                // haptic feedback
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.CONFIRM);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void checkForLoginFlow() {
        if (StringUtils.isNotEmpty(currentFragmentClassName)) {
            if (currentFragmentClassName.equals(LogInWIthMobileNumberFragment.TAG) && (StringUtils.isEmpty(previousFragmentClassName) || !previousFragmentClassName.equals(VerifyNumberFragment.TAG))) {
                releasePlayer();
                if (MyApplication.isCompetitiveProfile() && StringUtils.isEmpty(getCampaignBasedUrl())) {
                    player = new ExoPlayer.Builder(this).build();
                    // Make PlayerView VISIBLE before prepare() so the surface
                    // exists when ExoPlayer starts rendering video frames
                    imageSliderContainer.setVisibility(View.GONE);
                    playerView.setVisibility(View.VISIBLE);
                    playerView.setPlayer(player);
                    int videoResId = R.raw.neet_onboarding_video;
                    Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + videoResId);
                    MediaItem mediaItem = MediaItem.fromUri(videoUri);
                    player.setMediaItem(mediaItem);
                    player.prepare();
                    player.setPlayWhenReady(true);
                    player.addListener(new Player.Listener() {
                        @Override
                        public void onPlaybackStateChanged(int state) {
                            switch (state) {

                                case Player.STATE_BUFFERING:
                                    Log.d("VideoDebug", "Video buffering...");
                                    break;
                                case Player.STATE_READY:
                                    player.play();
                                    break;
                                case Player.STATE_ENDED:
                                    Log.d("VideoDebug", "Video ended — show image 1 again");
                                    playerView.setVisibility(View.GONE);
                                    imageSliderContainer.setVisibility(View.VISIBLE);
                                    stopSlideshowAndShowFirstImage();
//                                    ImageView imageViewEnded = findViewById(R.id.imageView1);
//                                    Glide.with(UserAuthenticationActivity.this)
//                                            .load(R.drawable.on_boarding_image_1)
//                                            .into(imageViewEnded);
                                    break;

                                default:
                                    break;
                            }
                        }

                        @Override
                        public void onPlayerError(PlaybackException error) {
                            Log.e("VideoDebug", "Player error: " + error.getMessage());
                            playerView.setVisibility(View.GONE);
                            imageSliderContainer.setVisibility(View.VISIBLE);
                            stopSlideshowAndShowFirstImage();
//                            ImageView imageViewError = findViewById(R.id.imageView1);
//                            Glide.with(UserAuthenticationActivity.this)
//                                    .load(R.drawable.on_boarding_image_1)
//                                    .into(imageViewError);
                        }
                    });

                } else {
                    playerView.setVisibility(View.GONE);
                    imageSliderContainer.setVisibility(View.VISIBLE);
                    startImageSlideshow();
//                    ImageView imageViewEnded = findViewById(R.id.imageView1);
//                    Glide.with(UserAuthenticationActivity.this)
//                            .load(R.drawable.on_boarding_image_1)
//                            .into(imageViewEnded);
                }
//                phoneSelection();
            } else if (currentFragmentClassName.equals(LoginCourseSelectionFragment.TAG)) {
                releasePlayer();
                playerView.setVisibility(View.GONE);
                imageSliderContainer.setVisibility(View.VISIBLE);
                stopSlideshowAndShowFirstImage();
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View fragmentContainer = findViewById(R.id.starting_frame);

            try {
                if (fragmentContainer != null) {
                    int[] location = new int[2];
                    fragmentContainer.getLocationOnScreen(location);
                    float x = event.getRawX();
                    float y = event.getRawY();

                    // Top boundary of fragment container
                    float topBoundary = location[1];

                    // If touch is above the fragment area → hide suggestions
                    if (y < topBoundary) {
                        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.starting_frame);
                        if (fragment instanceof OnBoardingUserLocationFragment) {
                            ((OnBoardingUserLocationFragment) fragment).hideSuggestionsAndKeyboard();
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return super.dispatchTouchEvent(event);
    }

    private void releasePlayer() {
        if (player != null) {
            player.stop();                 // Stop playback immediately
            player.clearMediaItems();      // Clear any loaded media
            player.release();              // Release internal resources
            player = null;                 // Set reference to null for GC
            Log.d("VideoDebug", "Player released completely.");
        }
    }

    private void startImageSlideshow() {
        String imageUrl = getCampaignBasedUrl();

        if (imageUrl != null && !imageUrl.isEmpty()) {
            imageView1.setVisibility(View.VISIBLE);
            imageView2.setVisibility(View.GONE);

            Glide.with(this)
                    .load(imageUrl)
                    .into(imageView1);

            isSlideshowRunning = false;
            return;
        }

        final ImageView[] currentView = {imageView1};
        final ImageView[] nextView = {imageView2};

        imageRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if (isFinishing() || isDestroyed() || !isSlideshowRunning) return;

                    // compute next index
                    currentImageIndex = (currentImageIndex + 1) % imageUrls.size();

                    final ImageView cv = imageView1.getVisibility() == View.VISIBLE ? imageView1 : imageView2;
                    final ImageView nv = cv == imageView1 ? imageView2 : imageView1;

                    // ensure measured width
                    int width = cv.getWidth();
                    if (width == 0) {
                        // view not measured yet -> try again shortly
                        handler.postDelayed(this, 100);
                        return;
                    }

                    // cancel any running animations to avoid conflicts
                    cv.animate().cancel();
                    nv.animate().cancel();

                    // prepare next view
                    Glide.with(UserAuthenticationActivity.this)
                            .load(imageUrls.get(currentImageIndex))
                            .into(nv);

                    nv.setVisibility(View.VISIBLE);
                    nv.bringToFront();
                    nv.setTranslationX(width);

                    // animate both views
                    cv.animate().translationX(-width).setDuration(500).withEndAction(() -> {
                        cv.setVisibility(View.GONE);
                        // reset translation so future expansions start correctly
                        cv.setTranslationX(0);
                    }).start();

                    nv.animate().translationX(0).setDuration(500).start();

                    Log.d(TAG, " ---> Image sliding: " + currentImageIndex);
                    // schedule next run
                    handler.postDelayed(this, SLIDESHOW_INTERVAL);
//                if (isFinishing() || isDestroyed()) {
//                    return;
//                }
//                currentImageIndex = (currentImageIndex + 1) % imageUrls.size();
//
//                nextView[0].setVisibility(View.VISIBLE);
//                Glide.with(UserAuthenticationActivity.this)
//                        .load(imageUrls.get(currentImageIndex))
//                        .into(nextView[0]);
//
//                nextView[0].setTranslationX(currentView[0].getWidth());
//
//                currentView[0].animate().translationX(-currentView[0].getWidth()).setDuration(500).start();
//                nextView[0].animate().translationX(0).setDuration(500).start();
//
//                ImageView temp = currentView[0];
//                currentView[0] = nextView[0];
//                nextView[0] = temp;
//
//                handler.postDelayed(this, SLIDESHOW_INTERVAL);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        Glide.with(this).load(imageUrls.get(currentImageIndex)).into(currentView[0]);
        resumeSlideshow();
        //handler.postDelayed(imageRunnable, SLIDESHOW_INTERVAL);

    }

    private String getCampaignBasedUrl() {
        String referralSource = MyApplication.getUtmCampaign();
        Log.d(TAG, "getCampaignBasedUrl: " + referralSource);
        if (StringUtils.isNotEmpty(referralSource)) {
            String json = MyApplication.getFirebaseRemoteConfig().getString("campaign_map");
            try {
                JSONObject object = new JSONObject(json);
                String key = referralSource.trim().toLowerCase();
                if (object.has(key)) {
                    String url = object.optString(key);
                    Log.d(TAG, "getCampaignBasedUrl--: " + url);
                    return StringUtils.isNotEmpty(url) ? url : "";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return "";
        }

        return "";
    }

    private void resumeSlideshow() {
        if (imageUrls == null || imageUrls.isEmpty()) return;
        if (isSlideshowRunning) return;
        isSlideshowRunning = true;

        // ensure runnable exists
        if (imageRunnable == null) {
            startImageSlideshow(); // will create runnable and return early if needed
            return;
        }

        // post first run immediately or after interval depending desired behavior
        handler.postDelayed(imageRunnable, SLIDESHOW_INTERVAL);
    }

    private void stopSlideshowAndShowFirstImage() {
        try {
            String imageUrl = getCampaignBasedUrl();
            isSlideshowRunning = false;
            handler.removeCallbacks(imageRunnable);
            currentImageIndex = 0;

            // cancel anims and reset state
            imageView1.animate().cancel();
            imageView2.animate().cancel();
            if (StringUtils.isNotEmpty(imageUrl)) {
                Glide.with(this).load(imageUrl).error(imageUrls.get(0)).into(imageView1);
            } else {
                Glide.with(this).load(imageUrls.get(0)).into(imageView1);
            }
            imageView1.setVisibility(View.VISIBLE);
            imageView1.setTranslationX(0);

            imageView2.setVisibility(View.GONE);
            imageView2.setTranslationX(0);
            Log.d(TAG, "---> Image slider stopped : First image showed");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop the slideshow when the activity is paused
        if (handler != null && imageRunnable != null) {
            handler.removeCallbacks(imageRunnable);
        }
    }


    private void phoneSelection() {
        try {

            GetPhoneNumberHintIntentRequest request = GetPhoneNumberHintIntentRequest.builder().build();
            Identity.getSignInClient(this)
                    .getPhoneNumberHintIntent(request)
                    .addOnFailureListener(e -> {
                        // Handle the failure
                    })
                    .addOnSuccessListener(pendingIntent -> {
                        if (isFinishing() || isDestroyed()) return;
                        try {
                            IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(pendingIntent.getIntentSender()).build();
                            if (showPhoneNumberHintPopUP != null) {
                                showPhoneNumberHintPopUP.launch(intentSenderRequest);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setCurrentDefaultLocaleStr(String my_lang) {
        MyApplication.saveData(PreferenceKeys.MY_LANG.getKey(), ArivihanLanguageEnum.fromStringToCode(my_lang));
        updateLocale(this, my_lang);
    }

    @Override
    public void playAnimation() {
        new Handler().postDelayed(() -> celebration.setVisibility(View.VISIBLE), 500);
    }

    @Override
    public void finishActivity() {
        finish();
    }

    private void setLocale() {
        String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        Log.d("langcode", "" + lang);
        if (lang != null) {
            Locale locale = new Locale(lang);
            Locale.setDefault(locale);
            Configuration configuration = new Configuration();
            configuration.locale = locale;
            getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
        }

    }


    private void updateLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);

        context.getApplicationContext().createConfigurationContext(configuration);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }

    @Override
    public void setFragment(Fragment fragment) {
        setFragmentWithDirection(fragment, true);
    }

    public void setFragmentBack(Fragment fragment) {
        setFragmentWithDirection(fragment, false);
    }

    public void setFragmentNoAnim(Fragment fragment) {
        try {
            runOnUiThread(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    if (!fragmentManager.isDestroyed()) {
                        setFragmentSelection(fragment);
                        previousFragmentClassName = currentFragmentClassName;
                        fragmentManager.beginTransaction()
                                .replace(R.id.starting_frame, fragment)
                                .commitAllowingStateLoss();
                        currentFragmentClassName = fragment.getClass().getSimpleName();
                        setBackButtonVisibility();
                        checkForLoginFlow();
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setFragmentWithDirection(Fragment fragment, boolean forward) {
        try {
            runOnUiThread(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    if (!fragmentManager.isDestroyed()) {
                        setFragmentSelection(fragment);
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        if (forward) {
                            fragmentTransaction.setCustomAnimations(
                                    R.anim.fade_in_slide_in, R.anim.fade_out_slide_out);
                        } else {
                            fragmentTransaction.setCustomAnimations(
                                    R.anim.slide_in_left, R.anim.slide_out_right);
                        }
                        fragmentTransaction.replace(R.id.starting_frame, fragment);
                        fragmentTransaction.commitAllowingStateLoss();
                        currentFragmentClassName = fragment.getClass().getSimpleName();
                        setBackButtonVisibility();
                        checkForLoginFlow();
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setFragmentSelection(Fragment fragment) {
        previousFragmentClassName = currentFragmentClassName;
        currentFragmentClassName = fragment.getClass().getSimpleName();
    }

    /**
     * Controls top-level back arrow visibility.
     * The phone number screen now has its own bottom back/forward buttons,
     * so the top arrow is hidden for it. Only the location fragment still uses it.
     */
    private void setBackButtonVisibility() {
        // Top arrow only for location fragment now — phone number screen has bottom nav buttons
        boolean showTopArrow = StringUtils.isNotEmpty(currentFragmentClassName)
                && currentFragmentClassName.equals(OnBoardingUserLocationFragment.TAG)
                && StringUtils.isEmpty(MyApplication.getUtmCampaign());
        cv_back.setVisibility(showTopArrow ? View.VISIBLE : View.GONE);

        // Gradient hidden on goal selection screen (it sits below the video/image area)
        gradientView.setVisibility(
                StringUtils.isNotEmpty(currentFragmentClassName)
                        && currentFragmentClassName.equals(LoginCourseSelectionFragment.TAG)
                        ? View.GONE : View.VISIBLE);
    }

    @Override
    public void setPhoneNumber(String number) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume the slideshow when the activity resumes
        if (handler != null && imageRunnable != null) {
            handler.postDelayed(imageRunnable, 4000);
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        TcSdk.clear();

        if (handler != null && imageRunnable != null) {
            handler.removeCallbacks(imageRunnable);
            imageRunnable = null;
            handler = null;
        }

//        PlayerView playerView = findViewById(R.id.video_player);
        if (playerView != null && playerView.getPlayer() != null) {
            playerView.getPlayer().release();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FrameLayout imageSliderContainer = findViewById(R.id.image_slider_container);
//        PlayerView playerView = findViewById(R.id.video_player);
        if (player != null) {
            player.pause();
            player.release();   // Release ExoPlayer
            player = null;
        }
        if (playerView != null) {
            playerView.setVisibility(View.GONE);
        }
        if (imageSliderContainer != null) {
            imageSliderContainer.setVisibility(View.VISIBLE);
            stopSlideshowAndShowFirstImage();
        }
    }


    @Override
    public void moveToOnBoardingActivity(String phoneNumberToLoginWith, String email, String id) {
        MyApplication.saveData(PreferenceKeys.AUTHENTICATION_DONE.getKey(), true);
        MyApplication.saveData(PreferenceKeys.USER_PHONE_NUMBER_ENTERED_DURING_ONBOARDING.getKey(),
                phoneNumberToLoginWith);

        // NEET users go through class selection first
        if (MyApplication.isCompetitiveProfile()) {
            // Set pending flag BEFORE launching — eliminates race condition where
            // AUTHENTICATION_DONE=true but NEET flag isn't set if process is killed
            MyApplication.saveData(PreferenceKeys.NEET_CLASS_SELECTION_PENDING.getKey(), true);

            Intent intent = new Intent(this, NeetClassSelectionActivity.class);
            intent.putExtra(PreferenceKeys.PHONE_NUMBER.getKey(), phoneNumberToLoginWith);
            intent.putExtra(PreferenceKeys.USER_ID.getKey(), id);
            startActivity(intent);
            finish();
            return;
        }

        // Board users go directly to onboarding
        Intent intent = new Intent(this, OnboardingActivityNew.class);
        intent.putExtra(PreferenceKeys.PHONE_NUMBER.getKey(), phoneNumberToLoginWith);
        intent.putExtra(PreferenceKeys.USER_ID.getKey(), id);
        intent.putExtra(OnboardingActivityNew.EXTRA_FRESH_LOGIN, true);
        startActivity(intent);
        finish();
    }


    public void checkUserExistence(String phoneNumberToLoginWith, String email, String id) {
        if (StringUtils.isNotEmpty(phoneNumberToLoginWith)) {
            Map<String, Object> map = new HashMap();
            map.put("phoneNumber", phoneNumberToLoginWith);
            map.put("key", BuildConfig.API_KEY);

            MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                    CommonMethods.getUrlParam(AV_GET_USER_BY_PHONE, map), null,
                    response -> {
                        Gson gson = new Gson();

                        Log.d(TAG, " userdto before typetoken " + response.toString());

                        TypeToken<ApiWrappedResponse<UserDetailsResponse>> token = new TypeToken<ApiWrappedResponse<UserDetailsResponse>>() {
                        };

                        ApiWrappedResponse<UserDetailsResponse> apiResponse = gson.fromJson(response.toString(), token.getType());

                        Map<String, String> params = new HashMap<>();
                        params.put("user_phone_number", phoneNumberToLoginWith);

                        if (!apiResponse.isSuccess() || apiResponse.getCode() == 404 || apiResponse.getData() == null) {
                            moveToOnBoardingActivity(phoneNumberToLoginWith, email, id);
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.user_onboarding_process.getActionName(), params);
                            return;
                        }

                        UserDetailsResponse user = (apiResponse != null && apiResponse.isSuccess()) ? apiResponse.getData() : null;

                        Log.d(TAG, " userdto typetoken converted");
                        if (user != null) {
                            MyApplication.saveUserProfileLocally(user);
                        }

                        MyApplication.saveData(PreferenceKeys.NUMBER_UPPER.getKey(), phoneNumberToLoginWith);
                        MyApplication.saveData(PreferenceKeys.PHONE_NUMBER.getKey(), String.valueOf(phoneNumberToLoginWith));
                        MyApplication.saveData(PreferenceKeys.USER_LOGGED_IN.getKey(), true);

                        if (user != null && user.getUserDto() != null) {
                            MyApplication.saveData(PreferenceKeys.USER_ID.getKey(), user.getUserDto().getUserId());
                        }
                        MyApplication.saveData(PreferenceKeys.COUNT_POP_UP.getKey(), 0);
//                        MyApplication.saveData(PreferenceKeys.BOARD_SWITCH.getKey(), true);

                        moveToHomeScreen();

                        params.put("steps", "otp_verified");
                        params.put("user_on_boarding_status", "Registered");
                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.user_login_process.getActionName(), params);
                    }, new VolleyGlobalErrorListener(getApplicationContext()) {
                @Override
                public void postError(VolleyError error) {
                    Log.e(TAG, "postError: " + error.getMessage());
                    try {
                    } catch (Exception e) {
                        Log.e(TAG, "postError: " + e.getMessage());
                    }
                    moveToOnBoardingActivity(phoneNumberToLoginWith, email, id);
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "postError: " + error.getMessage());
                    try {
                    } catch (Exception e) {
                        Log.e(TAG, "postError: " + e.getMessage());
                    }
                    moveToOnBoardingActivity(phoneNumberToLoginWith, email, id);
                }
            });
            myJsonObjectRequest2.setTag("ONBOARDING_LANGUAGE_SELECTION_FRAGMENT");
            MyVolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(myJsonObjectRequest2);
        }

    }

    public void checkTrueCallerUserDetails(String codeVerifier, String accessCode) {
        JSONObject object = new JSONObject();
        try {
            object.put("code", accessCode);
            object.put("code_verifier", codeVerifier);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        Map<String, String> eventParams = new HashMap<>();
        eventParams.put("code", accessCode);
        eventParams.put("code_verifier", codeVerifier);
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.true_caller_login_click.getActionName(), eventParams);
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.POST,
                GET_TRUECALLER_USER_INFO, object,
                response -> {
                    Gson gson = new Gson();
                    TypeToken<TruecallerResponseDto> token = new TypeToken<TruecallerResponseDto>() {
                    };
                    TruecallerResponseDto truecallerResponseDto = gson.fromJson(response.toString(), token.getType());

                    if (truecallerResponseDto.getPhone_number() != null && !truecallerResponseDto.getPhone_number().isEmpty()) {
                        if (truecallerResponseDto.getToken() != null) {
                            MyApplication.saveData(PreferenceKeys.AUTH_TOKEN.getKey(), truecallerResponseDto.getToken());
                        }

                        eventParams.put("phone", truecallerResponseDto.getPhone_number());
                        eventParams.put("email", truecallerResponseDto.getEmail());
                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.true_caller_login_click_success.getActionName(), eventParams);

                        String phoneForEvent = StringUtils.substring(truecallerResponseDto.getPhone_number(), 2);
                        EventApiHelper.sendActionEvent(ActionEventName.AUTH_INITIATED.getValue(),
                                PreferenceMethods.getSelectedOnBoardingId(), phoneForEvent, "TRUECALLER", ActionEventScreen.PHONE_SCREEN.getValue());
                        fireExamSelectedIfPending(phoneForEvent);

                        Log.d(TAG, "checkTrueCallerUserDetails: " + truecallerResponseDto.toString());
                        checkUserExistence(phoneForEvent, truecallerResponseDto.getEmail(), UUID.randomUUID().toString());
                    } else {
                        showPhoneNumberHintBottomSheet();
                    }

                    Log.i("TRUECALLER", "checkTrueCallerUserDetails: " + truecallerResponseDto.toString());
                }, new VolleyGlobalErrorListener(getApplicationContext()) {
            @Override
            public void postError(VolleyError error) {
                try {
                    Map<String, String> eventParams = new HashMap<>();
                    eventParams.put("code", accessCode);
                    eventParams.put("code_verifier", codeVerifier);
                    eventParams.put("error", error.getMessage());
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.true_caller_login_click_failure.getActionName(), eventParams);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    Map<String, String> eventParams = new HashMap<>();
                    eventParams.put("code", accessCode);
                    eventParams.put("code_verifier", codeVerifier);
                    eventParams.put("error", error.getMessage());
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.true_caller_login_click_failure.getActionName(), eventParams);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        myJsonObjectRequest2.setTag("ONBOARDING_LANGUAGE_SELECTION_FRAGMENT");
        MyVolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(myJsonObjectRequest2);
    }

    private void fireExamSelectedIfPending(String phoneNumber) {
        String pendingExamCode = MyApplication.getStringData(PreferenceKeys.EXAM_SELECTED_PENDING.getKey());
        if (pendingExamCode != null && !pendingExamCode.isEmpty()) {
            MyApplication.saveData(PreferenceKeys.EXAM_SELECTED_PENDING.getKey(), "");
            EventApiHelper.sendActionEvent(
                    ActionEventName.EXAM_SELECTED.getValue(),
                    null,
                    phoneNumber,
                    pendingExamCode,
                    ActionEventScreen.EXAM_SCREEN.getValue());
        }
    }

    public void showPhoneNumberPopUp() {
        if (!loginSuggestionBottomOpened) {
            try {
                if (MyApplication.getFirebaseRemoteConfig().getBoolean("true_caller_enabled") && TcSdk.getInstance() != null && TcSdk.getInstance().isOAuthFlowUsable()) {
                    String codeChallenge = CodeVerifierUtil.Companion.getCodeChallenge(codeVerifier);

                    Log.i("TRUECALLER", "CODE_VERIFIER :: " + codeVerifier);
                    if (codeChallenge != null) {
                        TcSdk.getInstance().setCodeChallenge(codeChallenge);
                    }
                    TcSdk.getInstance().setOAuthState(UUID.randomUUID().toString());
                    TcSdk.getInstance().setOAuthScopes(new String[]{"phone", "email", "openid", "address", "profile"});

                    Map<String, String> eventParams = new HashMap<>();
                    eventParams.put(PreferenceKeys.COURSE_NAME.getKey(), PreferenceMethods.getSelectedOnBoardingId());
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.truecaller_sheet_view.getActionName(), eventParams);

                    TcSdk.getInstance().getAuthorizationCode(UserAuthenticationActivity.this, trueCallerLauncher);
                } else {
                    showPhoneNumberHintBottomSheet();
                }
            } catch (Exception e) {
                Log.e("TRUECALLER", "TcSdk not initialized, falling back: " + e.getMessage());
                showPhoneNumberHintBottomSheet();
            }
        }
    }

    public void showPhoneNumberHintBottomSheet() {
        // No extra delay here — the caller already delays to let
        // the fragment transition finish before triggering the hint.
        GetPhoneNumberHintIntentRequest request =
                GetPhoneNumberHintIntentRequest.builder().build();

        Identity.getSignInClient(this)
                .getPhoneNumberHintIntent(request)
                    .addOnSuccessListener(result -> {
                        if (isFinishing() || isDestroyed()) return;
                        try {

                            IntentSenderRequest intentSenderRequest =
                                    new IntentSenderRequest.Builder(result.getIntentSender())
                                            .build();

                            Map<String, String> eventParams = new HashMap<>();
                            eventParams.put(PreferenceKeys.COURSE_NAME.getKey(), PreferenceMethods.getSelectedOnBoardingId());
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.android_phone_bottom_sheet_view.getActionName(), eventParams);

                            showPhoneNumberHintPopUP.launch(intentSenderRequest);

                        } catch (Exception e) {
                            Map<String, String> eventParams = new HashMap<>();
                            eventParams.put(PreferenceKeys.COURSE_NAME.getKey(), PreferenceMethods.getSelectedOnBoardingId());
                            eventParams.put("error", e.getMessage() != null ? e.getMessage() : "");
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.android_phone_bottom_sheet_view_failed.getActionName(), eventParams);

                            Log.e("PhoneHint", "Error launching phone hint", e);
                        }

                    })
                    .addOnFailureListener(e -> {
                        Map<String, String> eventParams = new HashMap<>();
                        eventParams.put(PreferenceKeys.COURSE_NAME.getKey(), PreferenceMethods.getSelectedOnBoardingId());
                        eventParams.put("error", e.getMessage() != null ? e.getMessage() : "");
                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.android_phone_bottom_sheet_view_failed.getActionName(), eventParams);

                        Log.e("PhoneHint", "Failed to get phone hint intent", e);
                    });
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

        Intent intent = new Intent(UserAuthenticationActivity.this, HomeScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(BundleParamConstants.LOCATION_SHOW_LOCATION_PERMISSION, true);

        // Accommodating the home pop sequence after on boarding
        MyApplication.saveData(PreferenceKeys.LAUNCH_COUNT.getKey(), 1);
        String classId = MyApplication.getStringData(PreferenceKeys.CLASS_ID.getKey());
        Log.d(TAG, "Class Id: " + classId);
        intent.putExtra("open_new_session_popup", true);

        startActivity(intent);
        finish();

    }

    @Override
    public void setGradientVisibility(boolean isVisible) {
        gradientView.setVisibility(isVisible ? View.VISIBLE : View.GONE);

        if (Build.VERSION.SDK_INT <= 25) {
            // Android 7 or below
            gradientView.setVisibility(View.GONE);
        }
    }

    @Override
    public void saveWelcomeFragmentBundle(Bundle bundle) {
        welcomeFragmentBundle = bundle;
    }


}
