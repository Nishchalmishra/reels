package arivihan.technologies.doubtbuzzter2.activity;
import arivihan.technologies.doubtbuzzter2.reelsdemo.*;
import arivihan.technologies.doubtbuzzter2.BuildConfig;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper;
import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.enums.AnalyticsEvent;
import arivihan.technologies.doubtbuzzter2.enums.ChapterEnum.ChapterStatus;
import arivihan.technologies.doubtbuzzter2.helper.ActivityLauncher;
import arivihan.technologies.doubtbuzzter2.helper.NewNotificationHelper;
import arivihan.technologies.doubtbuzzter2.helper.ReferEarnPopupHelper;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.BundleParamConstants;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.common.PosterPopUp;
import arivihan.technologies.doubtbuzzter2.customView.CustomBottomSheetDialog;
import arivihan.technologies.doubtbuzzter2.customView.HowToVideoView;
import arivihan.technologies.doubtbuzzter2.customView.PermissionRequestView;
import arivihan.technologies.doubtbuzzter2.customView.SubscriptionReminderView;
import arivihan.technologies.doubtbuzzter2.customView.ToDoBottomSheetDialog;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.NotificationEventType;
import arivihan.technologies.doubtbuzzter2.enums.PermissionType;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceMethods;
import arivihan.technologies.doubtbuzzter2.model.NotificationEventMetadata;
import arivihan.technologies.doubtbuzzter2.volley.EventApiHelper;
import arivihan.technologies.doubtbuzzter2.fragments.competitive.home.CompetitiveHomeScreenFragment;
import arivihan.technologies.doubtbuzzter2.fragments.MainDashboard;
import arivihan.technologies.doubtbuzzter2.helper.ContextWrapper;
import arivihan.technologies.doubtbuzzter2.helper.DeviceInfoUtil;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.helper.SubscriptionQrLoadHelper;
import arivihan.technologies.doubtbuzzter2.helper.firebase.FirebaseMessagingManager;
import arivihan.technologies.doubtbuzzter2.helper.offline.OfflineDownloadManager;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionType;
import arivihan.technologies.doubtbuzzter2.listener.OnCarouselClickListener;
import arivihan.technologies.doubtbuzzter2.manager.PDFCircleDailyCounterManager;
import arivihan.technologies.doubtbuzzter2.model.ChapterListInfoDto;
import arivihan.technologies.doubtbuzzter2.model.batch.home.BottomSheetActionButtonResponse;
import arivihan.technologies.doubtbuzzter2.model.batch.home.BottomSheetContentResponse;
import arivihan.technologies.doubtbuzzter2.model.carousel.CarouselResponse;
import arivihan.technologies.doubtbuzzter2.model.SwitchUserClassCourseRequestDto;
import arivihan.technologies.doubtbuzzter2.model.batch.user.UserDetailsResponse;
import arivihan.technologies.doubtbuzzter2.model.chapter.live.ChapterLiveDto;
import arivihan.technologies.doubtbuzzter2.model.chapter.live.ChapterPopUpDto;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.model.remote_config.poster.PosterPopUpConfig;
import arivihan.technologies.doubtbuzzter2.viewModel.HomeScreenViewModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import lombok.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import arivihan.technologies.doubtbuzzter2.manager.BottomNavigationManager;


import static arivihan.technologies.doubtbuzzter2.MyApplication.getLocationDenialCount;
import static arivihan.technologies.doubtbuzzter2.MyApplication.getLongData;
import static arivihan.technologies.doubtbuzzter2.activity.MicrolectureListActivity.NOTIFICATION_PERMISSION_REQUEST_CODE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_BOTTOM_SHEET_CONTENT;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_GET_USER_BY_PHONE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.CHAPTER_FETCH_BY_ID;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.SAVE_FCM_TOKEN;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.SAVE_USER_LOCATION;
import static arivihan.technologies.doubtbuzzter2.enums.HOW_TO_TYPE.HOME_SCREEN;
import static arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper.getSubscriptionPageClass;
import static com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE;

import arivihan.technologies.doubtbuzzter2.helper.ScreenAnalyticsHelper;

public class HomeScreen extends BaseActivity implements MainDashboard.DataBack, MainDashboard.MainDashboardListener,
        ToDoBottomSheetDialog.ToDoBottomSheetListener,
        CustomBottomSheetDialog.CustomBottomSheetListener,
        MainDashboard.OnFragmentInteractionListener,
        CompetitiveHomeScreenFragment.OnFragmentInteractionListener,
        HowToVideoView.RemoveListener, PosterPopUp.OnDismissListener, OnCarouselClickListener,
        BottomNavigationManager.Delegate, EdgeToEdgeHelper.SelfManagedInsets {
    private static final String TAG = HomeScreen.class.getSimpleName();
    public static final String EXTRA_OPEN_HOME_TAB = "open_home_tab";
    private static final int MY_REQUEST_CODE = 101;

    // Static so it survives recreate() — prevents Firebase subscription listener from triggering a recreate loop
    private static volatile long lastRecreateMs = 0L;
    // Survives recreate() — prevents batch-code change detection from triggering a recreate loop
    private long recreateTimeInMillis = 10000L;
    private static final int MY_REQUEST_CODE_2 = 100;
    RelativeLayout parentLayout;
    public static final String BroadcastStringForAction = "checkinternet";
    private boolean backPressed;
    AppUpdateManager appUpdateManager;
    private InstallStateUpdatedListener listener;
    private boolean homeScreenLaunched;
    private boolean NewUser = false;
    private FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(this);
    private SubscriptionQrLoadHelper subscriptionQrLoadHelper = null;
    private boolean isAskClicked;
    private boolean openAnalytics;
    String exam, examId;
//    private boolean isCampaignSubscribed;
    private BottomSheetContentResponse popupData;
    private ReferEarnPopupHelper referEarnPopupHelper;
    TextView text4, newFeatureText;
    ToDoBottomSheetDialog toDoBottomSheetDialog;
    CustomBottomSheetDialog bottomSheetDialog;
    Handler mHandler = new Handler();
    Runnable mRunnable;
    Dialog appUpdate;
    LottieAnimationView appUpdateButton;
    ImageView cancel, gifImage;
    private HomeScreenViewModel homeScreenViewModel;
    PosterPopUp posterPopUp;
    CharSequence formattedText = null;
    long noOfTimesUpdateShow = MyApplication.getFirebaseRemoteConfig().getLong("no_of_times_update_show");
    boolean toShowUpdatePopup = MyApplication.getFirebaseRemoteConfig().getBoolean("show_app_update_pop_up");
    boolean welcomeSequenceVisible = true;
    String featureUrl = MyApplication.getFirebaseRemoteConfig().getString("new_feature_url");
    ShimmerFrameLayout imageShimmer;
    FirebaseRemoteConfig remoteConfig = MyApplication.getFirebaseRemoteConfig();
    private PermissionRequestView notificationPermissionView;
    private HowToVideoView howToVideoView;
    private View how_to_video_view;
    private SubscriptionReminderView subscriptionReminderView;
    private boolean isTodoVisible = true;

    private Dialog languageUpdatePopUp;

    private boolean isNavigating = false;

    // Data Variable
    private boolean isCompetitiveUser;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int LOCATION_SETTINGS_REQUEST_CODE = 5100;
    private static final long MAX_DENIAL_BEFORE_SETTINGS = MyApplication.getFirebaseRemoteConfig().getLong("home_screen_location_permission_denial_count_before_settings");
    private FusedLocationProviderClient fusedLocationClient;
    private SettingsClient settingsClient;
    private final AtomicBoolean locationConsumed = new AtomicBoolean(false);
    private LocationCallback locationCallback;
    static BottomNavigationView bottomNavigationView; // kept for legacy access; managed by bottomNavigationManager
    private BottomNavigationManager bottomNavigationManager;
    private SwitchUserClassCourseRequestDto switchUserClassCourseRequestDto;

    private boolean isResumedFromBackground = false;
    private boolean onCreateCalled = false;
    private final ActivityResultLauncher<IntentSenderRequest> locationSettingsLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartIntentSenderForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Log.d(TAG, "User enabled location services");
                            startHighAccuracyUpdates();
                        } else {
                            Log.d(TAG, "User cancelled location enable: " + getLocationDenialCount());
                            MyApplication.incrementLocationDenialCount();
                        }
                    }
            );

    // onBackPressed logic moved to OnBackPressedCallback in onCreate

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        if (BuildConfig.REELS_DEMO) { DemoHome.bind(this); applyEdgeToEdgeInsets(); return; }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d(TAG, "--> Back Pressed on Home Screen: " + backPressed);

                if (howToVideoView != null && howToVideoView.videoView != null
                        && howToVideoView.videoView.getVisibility() == View.VISIBLE && howToVideoView.expanded) {
                    bottomNavigationManager.setVisibility(View.VISIBLE);
                    howToVideoView.resize();
                    return;
                }

                if (getIntent().getStringExtra("redirected_from") != null
                        && getIntent().getStringExtra("redirected_from").equals("WebViewDoubtChatScreen")) {
                    destroyHowTo();
                    CommonNavigator.navigateToDoubtChatClearTop(HomeScreen.this);
                    finish();
                    return;
                }

                if (openAnalytics) {
                    openAnalytics = false;
                    finish();
                    return;
                }

                // Case 4: Handle double back press to exit
                if (!backPressed && !isAskClicked) {
                    backPressed = true;

                    if (bottomNavigationManager != null && bottomNavigationManager.getSelectedItemId() != R.id.item_home) {
                        bottomNavigationManager.selectHomeTab();
                        bottomNavigationManager.navigateToDashboard(null);
                    }

                    try {
                        Toast.makeText(HomeScreen.this, R.string.click_to_exit, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e("TOAST", "Unable to show toast");
                    }

                    new Handler().postDelayed(() -> backPressed = false, 2000);
                    return;
                }

                Log.d(TAG, "--> Back Pressed on Home Screen: " + backPressed);
                if (backPressed) {
                    finishAffinity();
                    return;
                }

                isAskClicked = false;
            }
        });

        how_to_video_view = findViewById(R.id.how_to_video_view);
        parentLayout = findViewById(R.id.ParentLayout);
        MyApplication.saveData(PreferenceKeys.HOME_SCREEN_ON_CREATE_CALLED.getKey(), true);
        handleRedirectMiddleware(getIntent()); //handle redirection
        handleDeepLinkIntent(getIntent());

        //sub qr - cache
        subscriptionQrLoadHelper = new SubscriptionQrLoadHelper(this);
        isCompetitiveUser = MyApplication.isCompetitiveProfile();
        Log.d(TAG, "onCreate: ");
//        isCampaignSubscribed = MyApplication.getBoolData(PreferenceKeys.CAMPAIGN_SUBSCRIBED.getKey());
        Log.d("switchIssue", "onCreate---isCompetitiveUser " + isCompetitiveUser);
        try {
            PackageInfo info = getPackageManager().getPackageInfo("arivihan.technologies.doubtbuzzter2", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("FACEBOOKHASH", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        onCreateCalled = true;
        boolean fromAppOpen = getIntent().getBooleanExtra("from_app_open", false);

        Log.d("AppOpen", "from_app_open: " + fromAppOpen);

        if (fromAppOpen) {
            Log.d("AppOpen", "Sending app open analytics event");
            ScreenAnalyticsHelper.sendAppOpenEvent();
            Log.d("AppOpen", "App open analytics event sent");
        } else {
            Log.d("AppOpen", "Not launched from app open");
        }
        handleBottomNavigation();
        applyEdgeToEdgeInsets();
        handleOpenHomeTabIntent(getIntent());

        getAPIDataByPhoneData();
        if (getIntent().getBooleanExtra(BundleParamConstants.LOCATION_SHOW_LOCATION_PERMISSION, false)) {
            Log.d(TAG, "---> Location permission popup");
            if (!MyApplication.getFirebaseRemoteConfig().getBoolean("disable_location_service_v5")) {
                initLocationClient();
                requestLocationPermission();
            } else {
                handleWelcomeSequence();
            }
        }

        recreateTimeInMillis = MyApplication.getFirebaseRemoteConfig().getLong("home_screen_refresh_time_out");

        text4 = findViewById(R.id.text4);
        homeScreenViewModel = new ViewModelProvider(this).get(HomeScreenViewModel.class);

        openAnalytics = getIntent().getBooleanExtra("openAnalytics", false);
        homeScreenLaunched = true;
        MyApplication.updateUserIdForCrashlytics();
        checkPendingIntent();
        updateFcmNotificationToken();
        subscribeToTopic(); //topic subscribe
        FirebaseMessagingManager.getFirebaseTopicsToSubscribeUnsubscribe(getApplicationContext());
        OfflineDownloadManager.getFirebaseRemoteConfigOfflineDownloadTag();
        PDFCircleDailyCounterManager.setPdfCircleLimit();
        PDFCircleDailyCounterManager.getPdfCircleUsageApiUpdateToday(this);
        String my_lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        setCurrentDefaultLocaleStr(this, my_lang);

        if (savedInstanceState == null) {
            ScreenAnalyticsHelper.sendScreenVisitedEvent(AnalyticsEvent.HOME_SCREEN.name(), AnalyticsEvent.HOME_SCREEN.name());
        }

        MyApplication.syncOfflineTestData();

    }

    private void appUpdatePopUp() {
        try {
            if (isForceUpdateShow()) {
                //dialog show
                showForceUpdateDialog(getForceUpdateData());
                return;
            }

            appUpdateManager = AppUpdateManagerFactory.create(HomeScreen.this);
            Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

            // Checks that the platform will allow the specified type of update.
            appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
                if (isFinishing() || isDestroyed()) return;
                int availability = appUpdateInfo.updateAvailability();
                // UPDATE_AVAILABLE  → start a new immediate update
                // DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS → resume an update already
                //   in progress (e.g. user backgrounded the app mid-update)
                if (availability == UpdateAvailability.UPDATE_AVAILABLE
                        || availability == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    try {
                        if (BuildConfig.APP_UPDATE_POPUP) {
                            appUpdateManager.startUpdateFlowForResult(
                                    appUpdateInfo,
                                    IMMEDIATE,
                                    this,
                                    MY_REQUEST_CODE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            listener = state -> {
                // (Optional) Provide a download progress bar.
                if (state.installStatus() == InstallStatus.DOWNLOADING) {
                    // Implement progress bar.
                }

                if (state.installStatus() == InstallStatus.DOWNLOADED) {
                    // After the update is downloaded, show a notification
                    // and request user confirmation to restart the app.
                    popupSnackbarForCompleteUpdate();
                }
                // Log state or install the update.
            };

            appUpdateManager.registerListener(listener);
        } catch (Exception e) {
            Log.e("error", e.getMessage());
        }
    }

    private void checkSubscriptionDeepLink() {
        try {
            Uri data = getIntent().getData();
            Log.d(TAG, "DeepLink URI: " + data);

            // ✅ Check URI param first, then fall back to Intent extra
            String openSubscription = null;

            if (data != null) {
                openSubscription = getCaseInsensitiveQueryParam(data, "openSubscription");
            }

            if (openSubscription == null) {
                openSubscription = getIntent().getStringExtra("openSubscription"); // ✅ fallback
            }

            Log.d(TAG, "openSubscription value: " + openSubscription);

            if (!"true".equalsIgnoreCase(openSubscription)) {
                Log.d(TAG, "openSubscription is not true → skipping navigation");
                return;
            }

            Log.d(TAG, "✅ openSubscription=true detected");

            Intent intent = SubscriptionNavigationHelper.getSubscriptionPageIntent(this, RedirectionSource.DEEPLINK);

            // Pass all other extras from the incoming intent
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                for (String key : extras.keySet()) {
                    if (!key.equalsIgnoreCase("openSubscription")) {
                        intent.putExtra(key, extras.getString(key));
                        Log.d(TAG, "Passing param → " + key + "=" + extras.getString(key));
                    }
                }
            }

            startActivity(intent);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error handling deep link", e);
        }
    }


    private String getCaseInsensitiveQueryParam(Uri uri, String targetKey) {
        for (String key : uri.getQueryParameterNames()) {
            if (key.equalsIgnoreCase(targetKey)) {
                return uri.getQueryParameter(key);
            }
        }
        return null;
    }


    private void handleWelcomeSequence() {

        try {
            Log.d(TAG, "-----> handleWelcomeSequence : 1: " + welcomeSequenceVisible);
            Intent intent = getIntent();
            if (intent == null) return;

            if (!welcomeSequenceVisible) {
                return;
            }

            if (toShowUpdatePopup) {
                checkForUpdate();
                return;
            }

            welcomeSequenceVisible = false;

//            Log.d(TAG, "-----> handleWelcomeSequence : 2: " + welcomeSequenceVisible);

            if (intent.getBooleanExtra("show_language_update_popUp", false)) {
                toShowLanguageUpdatePopUp();
                return;
            }

            if (intent.getBooleanExtra("open_how_to_video", false)) {
                initializeHowTo();
                return;
            }

            if (intent.getBooleanExtra("open_notification_popup", false)) {
                showNotificationPermissionPopup();
                return;
            }

            if (intent.getBooleanExtra("open_refer_win_poster_popup", false)) {
                showReferWinPosterPopup();
                return;
            }

            if (intent.getBooleanExtra("open_poster_popup", false)) {
                showDynamicPosterPopup();
                return;
            }

            if (intent.getBooleanExtra("open_to_do_list_popup", false)) {
                openToDoListPopup();
                return;
            }

            showSubscriptionReminder();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toShowToppersNotesHomeScreenStatusCard() {
        String remoteJson = MyApplication.getFirebaseRemoteConfig().getString("toppers_notes_configuration_v2");
        Log.d(TAG, "toShowToppersNotesHomeScreenStatusCard: " + remoteJson);

        String configKey = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());

        try {
            JSONObject configJson = new JSONObject(remoteJson);
            if (!configJson.has(configKey)) {
                Log.w(TAG, "No config found for key: " + configKey);
                return;
            }
            JSONObject targetConfig = configJson.getJSONObject(configKey);

            String className = targetConfig.getString("className");
            String activityParams = targetConfig.getString("activityParams");

            //edit dialog text according to batch code
            String cardJson = MyApplication.getFirebaseRemoteConfig().getString("toppers_notes_dialog_text_v1");

            String title = getString(R.string.toppers_notes_title);
            String subTitle = getString(R.string.notes_ready_for_you);
            String titleImg = "";

            try {
                JSONObject cardConfig = new JSONObject(cardJson);

                if (cardConfig.has(configKey)) {
                    JSONObject cardData = cardConfig.getJSONObject(configKey);
                    title = cardData.optString("title", title);
                    subTitle = cardData.optString("sub_title", subTitle);
                    titleImg = cardData.optString("title_img", "");
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse card config: " + e.getMessage());
            }


            Map<String, Object> data = new HashMap<>();
            data.put("title", title);
            data.put("subTitle", subTitle);
            data.put("titleImg", titleImg);
            data.put("className", className);
            data.put("activityParams", activityParams);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.hs_toppers_notes_info_card_view.getActionName());
            MyApplication.getToppersNotesStatusDatabaseReference()
                    .setValue(data)
                    .addOnSuccessListener(aVoid ->
                            Log.d("FIREBASE_TOPPERS_NOTES", "Data pushed successfully for key: " + configKey))
                    .addOnFailureListener(e ->
                            Log.e("FIREBASE_TOPPERS_NOTES", "Failed to push data: " + e.getMessage()));

            Log.d("FIREBASE_TOPPERS_NOTES", "toShowToppersNotesHomeScreenStatusCard: " + data);

            MyApplication.saveData("show_topper_notes_status", false);
        } catch (JSONException e) {
            Log.e("FIREBASE_TOPPERS_NOTES", "Failed to parse Remote Config JSON: " + e.getMessage());
        }
    }


    private void showNotificationPermissionPopup() {
        if (notificationPermissionView == null) {
            notificationPermissionView = new PermissionRequestView(this);
            notificationPermissionView.initPermission(PermissionType.NOTIFICATION, true);
        }
    }


    private void showReferWinPosterPopup() {

        try {
            String key = "refer_win_poster_pop_up_home_screen_batch_" +
                    MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()) + "_" +
                    MyApplication.getStringData(PreferenceKeys.SELECTED_COURSE_ID_LOCAL.getKey());

            String configString = remoteConfig.getString(key);

            PosterPopUpConfig config =
                    new Gson().fromJson(configString, PosterPopUpConfig.class);

            PosterPopUp popUp = new PosterPopUp(this);

            if (config != null && StringUtils.isNotEmpty(config.getClassName())) {
                popUp.setTargetActivity(config.getClassName());
                if (config.getParams() != null) {
                    for (Map.Entry<String, Object> entry : config.getParams().entrySet()) {
                        popUp.addIntentExtra(entry.getKey(), String.valueOf(entry.getValue()));
                    }
                }
            }

            popUp.setPosterClickListener(() -> {
                MainDashboard.callMeBackReferAndWin(HomeScreen.this, parentLayout, null);
                Map<String, String> params = new HashMap<>();
                params.put("clickFrom", "poster");
                MyApplication.logFirebaseBundleCreation(
                        FirebaseEvents.refer_win_click.getActionName(), params);
            });

            if (config != null && StringUtils.isNotEmpty(config.getUrl())) {
                popUp.showPosterPopUp("", config.getUrl(),
                        config.getTimer() != 0 ? config.getTimer() : 4000);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showDynamicPosterPopup() {

        try {
            String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
            String classId = MyApplication.getStringData(PreferenceKeys.CLASS_ID.getKey());
            String courseId = MyApplication.getStringData(PreferenceKeys.SECONDARY_COURSE_ID.getKey());

            String configString =
                    remoteConfig.getString("dynamic_poster_pop_up_home_screen_batch_v2_" + lang);

            JSONObject allConfig = new JSONObject(configString);

            // Check classId first
            if (StringUtils.isEmpty(classId) || !allConfig.has(classId)) return;

            JSONObject classConfig = allConfig.getJSONObject(classId);

            // Then check courseId
            if (StringUtils.isEmpty(courseId) || !classConfig.has(courseId)) return;

            JSONObject courseConfig = classConfig.getJSONObject(courseId);

            JSONObject screenConfig = courseConfig.optJSONObject("HomeScreen");

            if (screenConfig == null) return;

            String posterUrl = screenConfig.optString("poster_url");
            long timer = screenConfig.optLong("pop_up_timer", 4000);
            String targetActivity = screenConfig.optString("target_activity");

            if (StringUtils.isEmpty(posterUrl)) return;
            posterPopUp = new PosterPopUp(this);

            if (!StringUtils.isEmpty(targetActivity)) {
                posterPopUp.setTargetActivity(targetActivity);

                JSONObject extras = screenConfig.optJSONObject("intent_extras");
                if (extras != null) {
                    Iterator<String> keys = extras.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        posterPopUp.addIntentExtra(key, extras.optString(key));
                    }
                }
            }

            posterPopUp.showPosterPopUp("", posterUrl, timer);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openToDoListPopup() {
        mRunnable = this::handleToDoBottomSheet;
        mHandler.postDelayed(mRunnable, 500);
    }


    private void showSubscriptionReminder() {
        if (subscriptionReminderView == null) {
            subscriptionReminderView =
                    new SubscriptionReminderView(this, getSupportFragmentManager());
        }
    }


    private void toShowLanguageUpdatePopUp() {
        try {
            if (isFinishing() || isDestroyed()) return;

            if (languageUpdatePopUp != null && languageUpdatePopUp.isShowing()) {
                return; // already visible
            }
            languageUpdatePopUp = new Dialog(this);
            languageUpdatePopUp.setContentView(R.layout.home_screen_language_switch_info);
            if (languageUpdatePopUp.getWindow() != null) {
                languageUpdatePopUp.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
                languageUpdatePopUp.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
            }
            languageUpdatePopUp.setCancelable(false);

            TextView title = languageUpdatePopUp.findViewById(R.id.title);
            TextView subtitle = languageUpdatePopUp.findViewById(R.id.subtitle);
            TextView btnTextView = languageUpdatePopUp.findViewById(R.id.btnText);
            CardView cv_confirm = languageUpdatePopUp.findViewById(R.id.cv_okay);

            // Fetch remote config
            Log.d("LanguagePopup", "Fetching text from Firebase Remote Config");
            String remoteJson = MyApplication.getFirebaseRemoteConfig()
                    .getString("home_screen_language_switch_info_text");
            Log.d("LanguagePopup", "Remote JSON received: " + remoteJson);

            JSONObject root = new JSONObject(remoteJson);
            String titleText = root.optString("title");
            String subTitle = root.optString("subtitle");
            String buttonText = root.optString("btnText");

            Log.d("LanguagePopup", "Parsed values - Title: " + titleText + ", Subtitle: " + subTitle + ", ButtonText: " + buttonText);

            // Set text safely with JSON validation
            Log.d("LanguagePopup", "Setting popup text values");
            title.setText(CommonMethods.isValidJson(titleText) ?
                    StyledTextBuilder.fromJson(this, titleText) : titleText);
            subtitle.setText(CommonMethods.isValidJson(subTitle) ?
                    StyledTextBuilder.fromJson(this, subTitle) : subTitle);
            btnTextView.setText(CommonMethods.isValidJson(buttonText) ?
                    StyledTextBuilder.fromJson(this, buttonText) : buttonText);

            // Set click listener
            cv_confirm.setOnClickListener(view -> {
                Log.d("LanguagePopup", "Okay button clicked - dismissing popup");
                dismissLanguageUpdatePopUp();
            });

            // Auto dismiss after configured duration
            long popupDuration = MyApplication.getFirebaseRemoteConfig().getLong("language_pop_up_duration");
            Log.d("LanguagePopup", "Popup auto-dismiss duration (ms): " + popupDuration);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (languageUpdatePopUp != null && languageUpdatePopUp.isShowing()) {
                    dismissLanguageUpdatePopUp();
                }
            }, popupDuration);

            // Show popup
            Log.d("LanguagePopup", "Displaying language update popup");
            languageUpdatePopUp.show();
            Log.d("LanguagePopup", "Popup displayed successfully");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void dismissLanguageUpdatePopUp() {
        if (languageUpdatePopUp != null &&
                languageUpdatePopUp.isShowing() &&
                !isFinishing() &&
                !isDestroyed()) {
            languageUpdatePopUp.dismiss();
        }
    }

    public void updateSubscribedUserUI() {
        try {
            if (SubscriptionNavigationHelper.hasActiveSubscriptionByBatch()) {
                Fragment fragment = getCurrentFragment();
                if (fragment instanceof MainDashboard) {
                    Log.d(TAG, "Active Fragment: MainDashboard");
                    ((MainDashboard) fragment).setToolBarUserData();
                    ((MainDashboard) fragment).updateCarouselData();
                } else if (fragment instanceof CompetitiveHomeScreenFragment) {
                    Log.d(TAG, "Active Fragment: CompHomeFragment");
                    ((CompetitiveHomeScreenFragment) fragment).setToolBarUserSubscribedOrNot();
                    ((CompetitiveHomeScreenFragment) fragment).updateCarouselData();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void updateBottomNavForSubscription(boolean checkSubscription) {
        Log.d("checkSubscription", "updateBottomNavForSubscription" + checkSubscription);
        try {
            if (!SubscriptionNavigationHelper.hasActiveSubscriptionByBatch()) {
                if (checkSubscription) {
                    long now = System.currentTimeMillis();
                    if (now - lastRecreateMs > recreateTimeInMillis) {
                        lastRecreateMs = now;
                        recreate();
                        checkUserCampaignSubscription(checkSubscription);
                    }
                } else {
                    updateBottomNavigationUI();
                }
            }
        } catch (Exception ignored) {
        }
    }

    public void updateBottomNavigationUI() {
        if (bottomNavigationManager != null) {
            bottomNavigationManager.updateUI();
        }
    }



    /**
     * Splits the system bar insets between the two things that have to react to them, instead of
     * letting {@link EdgeToEdgeHelper} pad the whole layout and letterbox the screen.
     *
     * <p>The bottom navigation takes the navigation bar inset as its own padding, so its background
     * runs to the bottom of the screen while the tabs stay above the bars. The top is handed down
     * untouched to the fragment in {@code main_frame}; each home fragment applies that inset to
     * its own root, which keeps content clear while its background - the dashboard banner or
     * profile gradient - still draws behind the bar.
     */
    private void applyEdgeToEdgeInsets() {
        View root = findViewById(R.id.home_root);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets safeArea = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            bottomNav.setPadding(0, 0, 0, safeArea.bottom);

            // The bottom is zeroed on the way down: main_frame already stops above the bottom
            // navigation, so a fragment padding for the navigation bar would only add a gap.
            return new WindowInsetsCompat.Builder(insets)
                    .setInsets(
                            WindowInsetsCompat.Type.systemBars()
                                    | WindowInsetsCompat.Type.displayCutout(),
                            Insets.of(safeArea.left, safeArea.top, safeArea.right, 0))
                    .build();
        });

        // BottomNavigationView installs its own inset listener that would reset the padding set
        // above back to what the layout declares; this replaces it with one that leaves it be.
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (view, insets) -> insets);

        ViewCompat.requestApplyInsets(root);
    }

    private void handleBottomNavigation() {
        NewUser = getIntent().getBooleanExtra("NewUser", false);
        MyApplication.updateAppLocale(this, MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.item_home);

        bottomNavigationManager = new BottomNavigationManager(this, bottomNavigationView, this);
        bottomNavigationManager.setIsUserOnboarded(getIntent().getBooleanExtra("is_user_onboarded", false));
        bottomNavigationManager.setup(
                NewUser,
                getIntent().getStringExtra(PreferenceKeys.SELECTED_TAB.getKey()),
                examId,
                exam);

    }


    private void initializeHowTo() {
        Log.d(TAG, "initializeHowTo--triggredfrom startActivity: ");
        Log.d(TAG, "initializeHowTo: howToVideoView=" + howToVideoView + " | isNetworkAvailable=" + MyApplication.isNetworkAvailable());

        if (howToVideoView != null || !MyApplication.isNetworkAvailable()) {
            Log.d(TAG, "initializeHowTo: skipped — howToVideoView null=" + (howToVideoView == null) + " | network=" + MyApplication.isNetworkAvailable());
            return;
        }

        FirebaseRemoteConfig.getInstance().fetchAndActivate().addOnCompleteListener(this, task -> {
            if (isFinishing() || isDestroyed()) return;
            if (howToVideoView != null) return; // already initialized (e.g. concurrent call)
            Log.d(TAG, "initializeHowTo: RC fetchAndActivate done (success=" + task.isSuccessful() + "), initializing HowToVideoView");

            MyApplication.saveData(PreferenceKeys.HOW_TO_VIDEO_VISIBLE.getKey(), true);
            Log.d(TAG, "initializeHowTo: saved HOW_TO_VIDEO_VISIBLE = true");

            howToVideoView = HowToVideoView.getInstance(this);
            Log.d(TAG, "initializeHowTo: HowToVideoView instance created=" + howToVideoView);

            Log.d(TAG, "initializeHowTo: how_to_video_view=" + how_to_video_view);

            howToVideoView.setView(this, how_to_video_view);
            Log.d(TAG, "initializeHowTo: setView called");

            howToVideoView.setType(HOME_SCREEN, false);
            Log.d(TAG, "initializeHowTo: setType called with HOME_SCREEN");

            howToVideoView.setRemoveListener(this);
            Log.d(TAG, "initializeHowTo: setRemoveListener set");

            howToVideoView.setButtonText("");
            Log.d(TAG, "initializeHowTo: setButtonText set to empty");
        });
    }

    private void setupAppUpdatePopup() {
        appUpdate = new Dialog(this);
        appUpdate.setContentView(R.layout.app_update_dialog);
        if (appUpdate.getWindow() != null) {
            appUpdate.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            appUpdate.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        appUpdate.setCancelable(false);
        appUpdateButton = appUpdate.findViewById(R.id.UpdateApp); // Corrected line
        cancel = appUpdate.findViewById(R.id.cancel);
        newFeatureText = appUpdate.findViewById(R.id.newFeatureText);
        gifImage = appUpdate.findViewById(R.id.gif_image);

        imageShimmer = appUpdate.findViewById(R.id.image_shimmer);
        imageShimmer.startShimmer();
        loadFeatureImage();

        appUpdateButton.setOnClickListener(view -> {
            String appPackageName = getApplicationContext().getPackageName();
            String playStoreUrl = "https://play.google.com/store/apps/details?id=" + appPackageName;
            destroyHowTo();
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl)));
            appUpdate.dismiss();
        });

        cancel.setOnClickListener(v -> appUpdate.dismiss());
        String language = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        String newFeatureKey = "new_feature_" + language;
        String newFeature = MyApplication.getFirebaseRemoteConfig().getString(newFeatureKey);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            formattedText = Html.fromHtml(newFeature, Html.FROM_HTML_MODE_LEGACY);
        }
        newFeatureText.setText(formattedText);
        appUpdate.show();
    }

    private void checkPendingIntent() {

        // If a notification message is tapped, any data accompanying the notification
        // message is available in the intent extras. In this sample the launcher
        // intent is fired when the notification is tapped, so any accompanying data would
        // be handled here. If you want a different intent fired, set the click_action
        // field of the notification message to the desired intent. The launcher intent
        // is used when no click_action is specified.
        //
        // Handle possible data accompanying notification message.
        // [START handle_data_extras]
        String url = null;
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                if ("deeplink".equalsIgnoreCase(key) && value != null && StringUtils.isNotEmpty(value.toString())) {
                    url = value.toString();
                }
            }
        }

        if (StringUtils.isEmpty(url)) {
            // Fall back to a deep link captured while the user was signed out (see
            // StartActivity.stashPendingDeepLinkIfPresent) and replay it now that
            // login/onboarding is done and HomeScreen has actually been reached.
            url = MyApplication.getStringDataFromDoNotDeleteSharedPreference(PreferenceKeys.PENDING_DEEP_LINK.getKey());
            if (StringUtils.isNotEmpty(url)) {
                MyApplication.removeFromDoNotDeleteSharedPreference(PreferenceKeys.PENDING_DEEP_LINK.getKey());
            }
        }

        if (StringUtils.isNotEmpty(url)) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (intent.resolveActivity(getPackageManager()) != null) {
                destroyHowTo();
                startActivity(intent);
            } else {
                Log.e("DeepLink", "No Activity found to handle: " + url);
            }
        }

    }

    private void subscribeToTopic() {
        FirebaseMessagingManager.updateUserTopics();
    }


    private String getCurrentAppVersion() {
        String version = "";
        try {
            PackageManager packageManager = getApplicationContext().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(getApplicationContext().getPackageName(), 0);
            version = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    private void checkForUpdate() {
        String latestVersion = MyApplication.getFirebaseRemoteConfig().getString("app_latest_version");
        Log.d("App Version", "App Latest Version: " + latestVersion);
        String currentVersion = getCurrentAppVersion();
        Log.d("App Version", "App current Version: " + currentVersion);
        int numberOfTimes = MyApplication.getIntData(currentVersion);
        if (numberOfTimes == -1) {
            numberOfTimes = 0;
        }

        if (numberOfTimes <= noOfTimesUpdateShow && isVersionOutdated(currentVersion, latestVersion)) {
            setupAppUpdatePopup();
            numberOfTimes++;
            MyApplication.saveData(currentVersion, numberOfTimes);
        }
    }

    private void loadFeatureImage() {
        int width = (int) MyApplication.getFirebaseRemoteConfig().getLong("image_width_app_update");
        int height = (int) MyApplication.getFirebaseRemoteConfig().getLong("image_height_app_update");
        ViewGroup.LayoutParams params = gifImage.getLayoutParams();
        params.width = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                width,
                getResources().getDisplayMetrics()
        );

        params.height = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                height,
                getResources().getDisplayMetrics()
        );
        gifImage.setLayoutParams(params);
        if (StringUtils.isNotEmpty(featureUrl)) {
            Glide.with(this)
                    .load(featureUrl)
                    .into(gifImage);
            imageShimmer.stopShimmer();
            imageShimmer.setVisibility(View.GONE);
            gifImage.setVisibility(View.VISIBLE);
        }
    }

    private boolean isVersionOutdated(String currentVersion, String latestVersion) {
        String[] currentParts = currentVersion.split("[-\\.]");
        String[] latestParts = latestVersion.split("[-\\.]");
        int length = Math.max(currentParts.length, latestParts.length);

        for (int i = 0; i < length; i++) {
            int currentPart = (i < currentParts.length) && currentParts[i].matches("\\d+") ? Integer.parseInt(currentParts[i]) : 0;
            int latestPart = (i < latestParts.length) && latestParts[i].matches("\\d+") ? Integer.parseInt(latestParts[i]) : 0;

            if (currentPart < latestPart) {
                return true;
            } else if (currentPart > latestPart) {
                return false;
            }

            if (i >= currentParts.length || i >= latestParts.length) {
                String currentSuffix = (i < currentParts.length) ? currentParts[i] : "";
                String latestSuffix = (i < latestParts.length) ? latestParts[i] : "";

                if (!currentSuffix.equals(latestSuffix)) {
                    return latestSuffix.compareTo(currentSuffix) > 0;
                }
            }
        }
        return false;
    }

    private void showUpdateDialog() {
        Log.d("Show Dialog", "showUpdateDialog: ");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Required");
        builder.setMessage("A new version of the app is available. Please update to continue.");
        builder.setCancelable(false); // Prevent dismissing the dialog

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Redirect to the Play Store
                String appPackageName = getApplicationContext().getPackageName();
                String playStoreUrl = "https://play.google.com/store/apps/details?id=" + appPackageName;
                destroyHowTo();
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl)));
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
            // Optionally, close the app if the update is mandatory
        });

        builder.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (BuildConfig.REELS_DEMO) return;
        isResumedFromBackground = true;
        if (howToVideoView != null && howToVideoView.videoView != null)
            howToVideoView.pausePlayer();
        homeScreenLaunched = false;
        try {
            appUpdateManager.unregisterListener(listener);
        } catch (Exception e) {

        }

        MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll("HomeScreen");

        if (mHandler != null)
            mHandler.removeCallbacksAndMessages(null);

        if (bottomNavigationManager != null) {
            bottomNavigationManager.stopListeningForChatBadge();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (BuildConfig.REELS_DEMO) return;

        // Always remove listeners regardless of isFinishing — prevents leaking the Activity
        if (referEarnPopupHelper != null) {
            referEarnPopupHelper.stopListening();
            referEarnPopupHelper = null;
        }

        if (!isFinishing()) {
            return;
        }

        try {
            if (toDoBottomSheetDialog != null && toDoBottomSheetDialog.isAdded()) {
                toDoBottomSheetDialog.dismiss();
                toDoBottomSheetDialog = null;
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

        try {
            if (bottomSheetDialog != null && bottomSheetDialog.isAdded()) {
                bottomSheetDialog.dismiss();
                bottomSheetDialog = null;
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

        if (appUpdate != null && appUpdate.isShowing()) {
            appUpdate.dismiss();
            appUpdate = null;
        }

        removeLocationCallback();

        if (bottomNavigationManager != null)
            bottomNavigationManager.stopListeningForChatBadge();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (BuildConfig.REELS_DEMO) return;
        boolean isFirstCreate = onCreateCalled;
        onCreateCalled = false;
        Log.d(TAG, "onResume: isFirstCreate=" + isFirstCreate
                + " | isResumedFromBackground=" + isResumedFromBackground);

        if (!MyApplication.isCompetitiveProfile() && bottomNavigationManager != null) {
            bottomNavigationManager.startListeningForChatBadge();
        }


        // For CompetitiveUser Welcome bottom sheet is
        // displayed on first time home screen launch after onboarding
        if (isResumedFromBackground || isCompetitiveUser &&
                MyApplication.getBoolData("show_welcome_bottom_sheet")) {
            if (popupData == null) getData();
            else handleBottomSheet();
        }
        boolean show_course_information_pop_up_on_home_screen = MyApplication.getBoolData(PreferenceKeys.SHOW_COURSE_INFORMATION_POP_UP_ON_HOME_SCREEN.getKey());

        Log.d(TAG, "onResume: " + isResumedFromBackground);
        Log.d(TAG, "onResume=----home: " + homeScreenLaunched);
        if (MyApplication.isNetworkAvailable() && show_course_information_pop_up_on_home_screen) {
            CommonMethods.showCourseChangeInformationDialog(this);
            MyApplication.saveData(PreferenceKeys.SHOW_COURSE_INFORMATION_POP_UP_ON_HOME_SCREEN.getKey(), false);
        } else if (howToVideoView != null && howToVideoView.videoView != null) {
            howToVideoView.resumePlayer();
        }

        appUpdatePopUp();

        if (subscriptionQrLoadHelper != null && !SubscriptionType.hasActiveSubscription()) {
            subscriptionQrLoadHelper.initCache();
        }

        checkForReferral();

        // Start listening for refer-earn popup from Firebase RDB
        if (referEarnPopupHelper == null) {
            referEarnPopupHelper = new ReferEarnPopupHelper(this);
        }
        referEarnPopupHelper.startListening();

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Context context = ContextWrapper.wrap(newBase, MyApplication.getLocale());
        super.attachBaseContext(context);
    }

    // Displays the snackbar notification and call to action.
    private void popupSnackbarForCompleteUpdate() {

        Snackbar snackbar =
                Snackbar.make(parentLayout,
                        "An update has just been downloaded.",
                        Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("RESTART", view -> appUpdateManager.completeUpdate());
        snackbar.setActionTextColor(
                getResources().getColor(R.color.snackbar_action_text_color));

        try {
            snackbar.show();
        } catch (Exception e) {
            Log.e("DIALOG", "Unable to show snackbar");
        }
    }

    private void handleToDoBottomSheet() {
        if (isFinishing() || isDestroyed() || getSupportFragmentManager().isStateSaved()) return;
        String thumbnailUrlKey = getString(R.string.create_to_do_popup_thumbnail) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        String videoUrlKey = getString(R.string.create_to_do_popup_video) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        String thumbnail = MyApplication.getFirebaseRemoteConfig().getString(thumbnailUrlKey);
        String video = MyApplication.getFirebaseRemoteConfig().getString(videoUrlKey);
        if (StringUtils.isNotEmpty(thumbnail) && StringUtils.isNotEmpty(video)) {
            try {
                toDoBottomSheetDialog = new ToDoBottomSheetDialog(this, this, thumbnail, video);
                toDoBottomSheetDialog.show(getSupportFragmentManager(), "");
                toDoBottomSheetDialog.setCancelable(false);
            } catch (Exception e) {
                Log.i("BOTTOM_SHEET", "Fragment not associated with a fragment manager");
            }
        }
    }

    private void handleBottomSheet() {
        if (bottomNavigationManager.getSelectedItemId() != R.id.item_home) return;
        if (!MyApplication.getBoolData("show_welcome_bottom_sheet")) return;
        if (isFinishing() || isDestroyed() || getSupportFragmentManager().isStateSaved()) return;

        if (bottomSheetDialog != null && bottomSheetDialog.isAdded()) {
            bottomSheetDialog.dismiss();
            bottomSheetDialog = null;
        }
        try {
            bottomSheetDialog = new CustomBottomSheetDialog(this, this, null, null, 0, true, popupData);
            bottomSheetDialog.show(getSupportFragmentManager(), "");
            bottomSheetDialog.setCancelable(false);
        } catch (Exception e) {
            Log.e(TAG, "Failed to show bottom sheet", e);
        }

        MyApplication.saveData("show_welcome_bottom_sheet", false);
    }

    private void getData() {
        Gson gson = new Gson();
        Log.d("BottomSheet", "AV_BOTTOM_SHEET_CONTENT Request batchId=" + MyApplication.getIntData(PreferenceKeys.BATCH_ID.getKey()));

        Integer batchId = MyApplication.getIntData(PreferenceKeys.BATCH_ID.getKey());
        String url = AV_BOTTOM_SHEET_CONTENT + "?batchId=" + batchId + "&forLiveClass=true";
        MyJsonObjectRequest getDataRequest = new MyJsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    if (isFinishing() || isDestroyed()) return;
                    Log.d("BottomSheet", "AV_BOTTOM_SHEET_CONTENT Response: " + response.toString());
                    if (response == null) return;

                    TypeToken<ApiWrappedResponse<BottomSheetContentResponse>> token = new TypeToken<>() {};
                    ApiWrappedResponse<BottomSheetContentResponse> apiResponse = gson.fromJson(response.toString(), token.getType());
                    if (apiResponse == null || apiResponse.getData() == null) return;

                    popupData = apiResponse.getData();
                    mRunnable = this::handleBottomSheet;
                    mHandler.postDelayed(mRunnable, 500);
                },
                new VolleyGlobalErrorListener(this) {
                    @Override
                    public void postError(VolleyError error) {
                        Log.d("BottomSheet", "ERROR = " + error.toString());

                        Log.e("Error", error.toString());
                    }
                }
        );
        getDataRequest.setTag("HomeScreen");
        MyVolleySingleton.getInstance(this).addToRequestQueue(getDataRequest);
    }

    @Override
    public void isClicked(boolean clicked) {
        isAskClicked = clicked;
    }

    @Override
    public void onCloseClick() {
        if (toDoBottomSheetDialog != null) {
            toDoBottomSheetDialog.dismiss();
        }
    }

    @Override
    public void onCloseButtonClick() {
        try {
            if (bottomSheetDialog != null) {
                bottomSheetDialog.dismiss();
                bottomSheetDialog = null;
                if (popupData != null && popupData.isShowPopup()) {
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.wclm_sheet_join_karein_cross_click.getActionName());
                    CommonMethods.showCourseChangeInformationDialog(this);
                }
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void onStartNowClick(String className, String activityParams) {
        try {
            if (StringUtils.isNotEmpty(className)) {

                if (className.equalsIgnoreCase("external_link")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);

                    JSONObject jsonParams = new JSONObject(activityParams);

                    intent.setData(Uri.parse(Objects.requireNonNull(jsonParams.get("link")).toString()));

                    if (getApplicationContext() != null && intent.resolveActivity(getApplicationContext().getPackageManager()) != null) {
                        destroyHowTo();
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), "Unable to redirect", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Intent intent = new Intent(this, ActivityLauncher.resolveClass(className));

                    if (activityParams != null) {
                        // Convert activityParams string into a JSON object
                        JSONObject jsonParams = new JSONObject(activityParams);
                        Iterator<String> keys = jsonParams.keys();

                        while (keys.hasNext()) {
                            String key = keys.next();
                            Object value = jsonParams.get(key);

                            if (value instanceof String) {
                                intent.putExtra(key, (String) value);
                            } else if (value instanceof Integer) {
                                intent.putExtra(key, (Integer) value);
                            } else if (value instanceof Boolean) {
                                intent.putExtra(key, (Boolean) value);
                            } else if (value instanceof Double) {
                                intent.putExtra(key, ((Double) value).floatValue()); // Handle doubles as floats
                            } else if (value instanceof Long) {
                                intent.putExtra(key, (Long) value);
                            }
                        }
                    }

                    bottomSheetDialog.dismiss();
                    destroyHowTo();
                    startActivity(intent);
                }

            }
        } catch (ClassNotFoundException | JSONException e) {
            bottomSheetDialog.dismiss();
            destroyHowTo();
//            startActivity(new Intent(this, HomeScreen.class));
        }
    }

    @Override
    public void onDismiss(BottomSheetActionButtonResponse btnData) {
        PosterPopUp posterPopUp = new PosterPopUp(this);
        posterPopUp.setOnDismissListener(this);
        posterPopUp.showPosterPopUp(btnData.getDialogBannerText(), btnData.getDialogBannerImage(), btnData.getDialogTimeout());
        MyApplication.registerForLiveSession();

    }

    @Override
    public void onCreateNowClick() {
        toDoBottomSheetDialog.dismissAll();
        Intent todoListIntent = new Intent(this, ToDoListMain.class);
        todoListIntent.putExtra("redirect_to_home", true);
        destroyHowTo();
        startActivityForResult(todoListIntent, MY_REQUEST_CODE_2);
        overridePendingTransition(R.anim.right_in_activity, R.anim.fade_out);
        MyApplication.saveData(PreferenceKeys.SHOWN_AFTER_INTRO.getKey(), "ToDoListMain");
        logToDoFirebaseEvent();
    }

    private void logToDoFirebaseEvent() {
        Map<String, String> params = new HashMap<>();
        params.put("action", "to_do_popup_create_now__click");
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.to_do_popup_create_now__click.getActionName(), params);
    }

    @Override
    public void onValuePass(String examId, String examName) {
        this.examId = examId;
        this.exam = examName;
    }

    @Override
    public void onHowToPosterClick() {
        showExpandedVideo();
        logHowToFirebaseEvent();
    }

    private void logHowToFirebaseEvent() {
        Map<String, String> params = new HashMap<>();
        params.put("action", "how_to_poster_click");
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.how_to_poster_click.getActionName(), params);
    }

    private void showExpandedVideo() {
        if (MyApplication.isNetworkAvailable() && !MyApplication.getFirebaseRemoteConfig().getBoolean("hideHowToVideos")) {
            bottomNavigationManager.setVisibility(View.GONE);
            howToVideoView = HowToVideoView.getInstance(this);
            howToVideoView.setView(this, how_to_video_view);
            howToVideoView.setType(HOME_SCREEN, true);
            howToVideoView.setRemoveListener(this);
            howToVideoView.setButtonText("");
        }
    }

    @Override
    public void onButtonClick() {
        bottomNavigationManager.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClose() {
        Log.d("issueonVideoPLayer", "homescreenonClose:");
        bottomNavigationManager.setVisibility(View.VISIBLE);
    }

    @Override
    public void onExpand() {
        bottomNavigationManager.setVisibility(View.GONE);
    }

    @Override
    public void onCollapse() {
        bottomNavigationManager.setVisibility(View.VISIBLE);
    }

    @Override
    public void onChangeTodoVisibility(boolean isVisible) {
        isTodoVisible = isVisible;
    }

    @Override
    public void userDataFetchedSuccessfully() {
        try {
            Log.d(TAG, "userDataFetchedSuccessfully: ");
            triggerHomeScreenStatusCard();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void userClassCourseUpdated() {
        try {
            if (!MyApplication.getBoolData("is_class_course_updated")) {
                MyApplication.saveData("is_class_course_updated", true);
                updateBottomNavigationUI();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onDismissListener() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Log.d(TAG, "onDismissListener notification listener called2 : ");
                if (ActivityCompat.checkSelfPermission(
                        HomeScreen.this,
                        Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(
                            HomeScreen.this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS},
                            NOTIFICATION_PERMISSION_REQUEST_CODE
                    );
                } else {
                    Log.d(TAG, "Notification permission already granted.");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error requesting notification permission", e);
        }

    }

    @Override
    public void onCarouselClicked(CarouselResponse item) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }


    public static class Analytic_fragment_clicked {
        public void analytic_frag() {
            bottomNavigationView.getMenu().findItem(R.id.analytics).setChecked(true);
        }
    }

    public Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.main_frame);
    }

    // ─────────────────────────────────────────────
    // BottomNavigationManager.Delegate implementation
    // ─────────────────────────────────────────────

    @Override
    public void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    @Override
    public void destroyHowTo() {
        if (how_to_video_view != null) {
            how_to_video_view.setVisibility(View.GONE);
        }
        if (howToVideoView != null) {
            howToVideoView.remove();
            howToVideoView = null;
        }
    }

    @Override
    public void removeHowTo() {
        if (howToVideoView != null) {
            howToVideoView.remove();
            howToVideoView = null;
        }
    }

    @Override
    public void reloadData() {
//        getData();
    }

    @Override
    public boolean isTodoVisible() {
        return isTodoVisible;
    }

    /**
     * Activity result method will be called after closing the camera
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                try {
                    Toast.makeText(getApplicationContext(),
                                    getString(R.string.update_flow_failed), Toast.LENGTH_SHORT)
                            .show();

                } catch (Exception e) {
                    Log.e("TOAST", "Unable to show toast");
                }
            }
        } else if (requestCode == MY_REQUEST_CODE_2) {
            if (toDoBottomSheetDialog != null)
                toDoBottomSheetDialog.dismiss();
        }

    }

    public void updateFcmTokenAndVersion(String deviceToken) {
        String versionName = "";
        try {
            Log.d("updateFcmToken", deviceToken);

            versionName = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;

            Map<String, Object> map = new HashMap<>();
            map.put("fcmToken", deviceToken);
            map.put("version", versionName);
            MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                    CommonMethods.getUrlParam(SAVE_FCM_TOKEN, map), null,
                    response -> {
                        Log.d("updateFcmToken", "success");
                        MyApplication.saveData(PreferenceKeys.FCM_NOTIFICATION_TOKEN.getKey(), deviceToken);
                    }, null);
            MyVolleySingleton.getInstance(HomeScreen.this).addToRequestQueue(myJsonObjectRequest2);
        } catch (Throwable e) {

        }


    }

    private void updateFcmNotificationToken() {
        try {

            long FCMTime = getLongData(PreferenceKeys.FCM_NOTIFICATION_TOKEN_STORE_TIME.getKey());

            if (System.currentTimeMillis() > FCMTime) {
                MyApplication.saveData(PreferenceKeys.FCM_NOTIFICATION_TOKEN.getKey(), "");
            }

            String fcmNotificationToken = MyApplication.getStringData(PreferenceKeys.FCM_NOTIFICATION_TOKEN.getKey());

            if (StringUtils.isEmpty(fcmNotificationToken)) {
                Log.d("updateFcmToken", "fcmNotificationToken empty");

                if (MyApplication.isNetworkAvailable()) {
                    MyApplication.saveData(PreferenceKeys.FCM_NOTIFICATION_TOKEN_STORE_TIME.getKey(), System.currentTimeMillis() + 604800000);
                    String token = MyApplication.getStringData(PreferenceKeys.DEVICE_TOKEN.getKey());

                    updateFcmTokenAndVersion(token == null ? "" : token);

                }
            }
        } catch (Throwable e) {
            Log.e("FCM", "Error");
        }
    }


    private void setCurrentDefaultLocaleStr(Context context, String my_lang) {
        if (StringUtils.isNotEmpty(my_lang)) {
            Locale newLang = new Locale(my_lang);
            Locale.setDefault(newLang);
            Configuration configuration = getBaseContext().getResources().getConfiguration();
            configuration.setLocale(newLang);
            context.createConfigurationContext(configuration);
        }

    }


    @Override
    public void onNewIntent(Intent intent) {

        super.onNewIntent(intent);
        setIntent(intent);

        Log.d("APPLICATIONINBACKGROUND", "onNewIntent - starting");
        Bundle extras = intent.getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                Log.d("APPLICATIONINBACKGROUND", "Extras received at onNewIntent:  Key: " + key + " Value: " + value);
            }
            String title = extras.getString("title");
            String message = extras.getString("body");

        }


        handleRedirectMiddleware(intent);
        handleDeepLinkIntent(intent);
        handleOpenHomeTabIntent(intent);
    }



    private void handleRedirectMiddleware(Intent intent) {
        if (intent == null) return;

        String redirectionFrom = intent.getStringExtra("redirectionFrom");

        String redirectActivity = intent.getStringExtra("redirect_activity");
        if (redirectActivity == null || redirectActivity.isEmpty()) return;

        String redirectParams = intent.getStringExtra("redirect_params");
        String redirectKey = intent.getStringExtra("redirect_key");
        if (redirectKey == null) return;

        Log.d(TAG, "redirectKey " + redirectKey);

        String landedScreen = redirectActivity;
        String fallbackReason = "";

        try {
            Intent targetIntent = null;


            //for subscription
            if (redirectKey.equals("SUBSCRIPTION")) {
                Class<?> targetClass = getSubscriptionPageClass();
                targetIntent = new Intent(this, targetClass);
                landedScreen = targetClass.getName();
            }
            //if live class and basic plan
            else if ((redirectKey.equals("LIVE_CLASS")) && SubscriptionType.isBasicSubscription()) {
                Class<?> targetClass = getSubscriptionPageClass();
                targetIntent = new Intent(this, targetClass);
                targetIntent.putExtra("navigatedFromScreen", "LECTURES");
                landedScreen = targetClass.getName();
                fallbackReason = "basic_subscription";
            }
            //if live class: - show dialog, else redirect to class
            else if (redirectKey.equals("LIVE_CLASS")) {
                //send event before handling
                if (redirectionFrom != null && redirectionFrom.equals("NOTIFICATION")) {
                    sendNotificationClickEvent(intent, redirectActivity, "LiveClassCondition", "");
                }
                handleLiveClassRedirectCondition(redirectActivity, redirectParams);
                return;
            }
            //else redirect activity
            else {
                JSONObject extras = null;

                if (redirectParams != null && !redirectParams.isEmpty()) {
                    try {
                        extras = new JSONObject(redirectParams);
                        Log.d(TAG, "Built extras from params");
                    } catch (Exception e) {
                        Log.w(TAG, "Failed to parse params", e);
                    }
                }
                Log.d(TAG, "Calling buildTapIntent with activity=" + redirectActivity);
                targetIntent = NewNotificationHelper.buildTargetIntent(this, redirectActivity.trim(), extras);
            }


            //run
            if (targetIntent != null) {
                destroyHowTo();
                startActivity(targetIntent);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in redirectToActivity: " + redirectActivity, e);
        }


        //send event
        if (redirectionFrom == null) redirectionFrom = "";
        if (redirectionFrom.equals("NOTIFICATION")){
            sendNotificationClickEvent(intent, redirectActivity, landedScreen, fallbackReason);
        }


    }


    private void sendNotificationClickEvent(Intent intent, String targetScreen, String landedScreen, String fallbackReason) {
        try {
            NotificationEventMetadata metadata = NotificationEventMetadata.fromBundle(intent.getExtras());

            String ctaType = intent.getStringExtra("cta_type");
            String ctaValue = intent.getStringExtra("cta_value");

            // Determine click source
            String eventType = NotificationEventType.NOTIFICATION_CLICK.getValue();
            if (ctaType != null && !ctaType.isEmpty()) {
                eventType = NotificationEventType.NOTIFICATION_CLICK.getValue();
                metadata.ctaType = ctaType;
            }
            if (ctaValue != null && !ctaValue.isEmpty()) {
                metadata.ctaValue = ctaValue;
            }

            EventApiHelper.sendActionEventNotificationClick(
                    this, metadata, eventType, targetScreen,
                    landedScreen, fallbackReason
            );

        } catch (Exception e) {
            Log.e(TAG, "Error sending notification click event", e);
        }
    }



    private void handleLiveClassRedirectCondition(String redirectActivity, String redirectParams){
        try {
            String batchId = MyApplication.getStringData(PreferenceKeys.BATCH_ID.getKey());

            String chapterId = "";
            String chapterName;
            String title;
            String subjectId;
            String boardCode;

            if (redirectParams != null && !redirectParams.isEmpty()) {
                JSONObject paramsObj = new JSONObject(redirectParams);
                chapterId = paramsObj.optString("chapterId", "");
                chapterName = paramsObj.optString("chapterName", "");
                title = paramsObj.optString("title", "");
                subjectId = paramsObj.optString("subjectId", "");
                boardCode = paramsObj.optString("boardCode", "");
            } else {
                chapterName = "";
                boardCode = "";
                subjectId = "";
                title = "";
            }

            if (StringUtils.isEmpty(chapterId)) return;

            //parms
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("batchId", batchId);


            //api call
            MyJsonObjectRequest apiRequest = new MyJsonObjectRequest(Request.Method.GET,
                    CommonMethods.getUrlParam(CHAPTER_FETCH_BY_ID + "/" + chapterId, requestParams),
                    null,
                    response -> {
                        try {
                            Gson gson = new Gson();
                            TypeToken<ApiWrappedResponse<ChapterListInfoDto>> token = new TypeToken<>() {};
                            ApiWrappedResponse<ChapterListInfoDto> apiResponse = gson.fromJson(response.toString(), token.getType());

                            if (apiResponse != null && apiResponse.getData() != null) {
                                ChapterListInfoDto chapterData = apiResponse.getData();
                                ChapterLiveDto chapterLiveDto = chapterData.getChapterLiveDto();

                                if (chapterLiveDto != null) {

                                    //is live class
                                    ChapterStatus status = ChapterStatus.fromString(chapterData.getChapterStatus());
                                    if (ChapterStatus.LIVE.equals(status)) {

                                        Bundle bundle = new Bundle();
                                        bundle.putString("title", title);
                                        bundle.putString("id", subjectId);
                                        bundle.putString(PreferenceKeys.NAME.getKey(), chapterData.getSubjectName());
                                        bundle.putString("code", chapterData.getSubjectCode());
                                        bundle.putString(PreferenceKeys.BOARD_CODE.getKey(), boardCode);


                                        CommonNavigator.navigateToLiveClassWebView(
                                                HomeScreen.this,
                                                chapterName, //chapter name
                                                chapterData.getSubjectName(),
                                                chapterData.getSubjectCode(),
                                                chapterData.getChapterId(),
                                                chapterData.getChapterCode(),
                                                chapterData.getImageUrl(),
                                                chapterData.getTeacherName(),
                                                TAG,
                                                bundle
                                        );

                                    }

                                    //show popup
                                    else {
                                        ChapterPopUpDto upcomingPopup = chapterLiveDto.getUpcomingStatusPopUp();
                                        if (upcomingPopup != null &&
                                                (StringUtils.isNotEmpty(upcomingPopup.getTitle()) ||
                                                        StringUtils.isNotEmpty(upcomingPopup.getSubTitle())))
                                        {
                                            CommonMethods.showGenericPopupInformation(
                                                    HomeScreen.this,
                                                    upcomingPopup.getTitle(),
                                                    upcomingPopup.getSubTitle(),
                                                    upcomingPopup.getBtnName(),
                                                    chapterData.getChapterStatus()
                                            );
                                        }
                                    }
                                } else {
                                    Log.w(TAG, "No chapterLiveDto data");
                                }
                            }
                        } catch (Exception ex) {
                            Log.e(TAG, "❌ Error parsing chapter response", ex);
                        }
                    },
                    new VolleyGlobalErrorListener(this) {
                        @Override
                        public void postError(VolleyError error) {
                            Log.e(TAG, "❌ Error fetching chapter data: " + (error != null ? error.getMessage() : "unknown error"));
                        }
                    }
        );
            apiRequest.setTag(TAG);
            MyVolleySingleton.getInstance(this).addToRequestQueue(apiRequest);
        } catch (Exception ex) {
            Log.e(TAG, "❌ Error handling LIVE_CLASS redirect", ex);
        }
    }






    private void handleOpenHomeTabIntent(Intent intent) {
        if (intent == null || !intent.getBooleanExtra(EXTRA_OPEN_HOME_TAB, false)) {
            return;
        }

        if (bottomNavigationManager != null) {
            bottomNavigationManager.selectHomeTab();
            bottomNavigationManager.navigateToDashboard(null);
        }
    }

    private void handleDeepLinkIntent(Intent intent) {
        Uri data = intent.getData();
        Log.d(TAG, "handleDeepLinkIntent: " + data);
        if (data != null) {
            String openSubscription = data.getQueryParameter("openSubscription");

            if ("true".equals(openSubscription)) {
                openSubscriptionScreen();
            }
        }
    }

    private void openSubscriptionScreen() {
        Log.d(TAG, "openSubscriptionScreen: Navigating to subscription page from HOME");
        SubscriptionNavigationHelper.navigateToSubscriptionPage(this, RedirectionSource.DEEPLINK);
    }

    private void getAPIDataByPhoneData() {
        String selectedCourseId = MyApplication.getStringData(PreferenceKeys.SECONDARY_COURSE_ID.getKey());
        Map<String, Object> map = new HashMap<>();
        String phoneNumber = MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey());
        map.put("phoneNumber", phoneNumber);
        map.put("key", BuildConfig.API_KEY);
        map.put(PreferenceKeys.SELECTED_COURSE_ID_LOCAL.getKey(), selectedCourseId);
        long lastDeviceInfoTime = getLongData(PreferenceKeys.LAST_DEVICE_INFO_TIME.getKey());
        map.put("selectedCourseId", selectedCourseId);
        long currentTime = System.currentTimeMillis();


        String remoteKey = PreferenceKeys.UNLOCK_SUBSCRIPTION_SCREEN_DATA_V2.getKey()
                + "_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        String remoteJson = MyApplication.getFirebaseRemoteConfig()
                .getString(remoteKey);
        MyApplication.saveData(PreferenceKeys.UNLOCK_SUBSCRIPTION_SCREEN_DATA_V2.getKey(), remoteJson);

        if (lastDeviceInfoTime == -1 || !isSameDay(currentTime, lastDeviceInfoTime)) {
            DeviceInfoUtil.getDeviceInfoAsync(this, deviceInfo -> {
                map.putAll(deviceInfo);

                MyApplication.saveData(PreferenceKeys.LAST_DEVICE_INFO_TIME.getKey(), currentTime);

                apiGetUserByPhone(map);
                Log.d(TAG, "getAPIDataByPhoneData: " + map);
            });
        } else {
            apiGetUserByPhone(map);
        }
    }

    private void apiGetUserByPhone(Map<String, Object> map) {
        Log.d(TAG, "---> req: " + map.toString());
        final String priorBatchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
        final String priorLang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        MyApplication.resetBatchCodeRefreshFlag();
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(AV_GET_USER_BY_PHONE, map), null,
                response -> {
                    if (isFinishing() || isDestroyed()) return;
                    try {
                        Log.d(TAG, "---> Response: " + response);
                        Gson gson = new Gson();
//                        Log.d("switchIssue", "Raw API Response: " + response.toString());

                        TypeToken<ApiWrappedResponse<UserDetailsResponse>> token = new TypeToken<ApiWrappedResponse<UserDetailsResponse>>() {
                        };
                        ApiWrappedResponse<UserDetailsResponse> apiResponse = gson.fromJson(response.toString(), token.getType());
                        UserDetailsResponse userDto = (apiResponse != null && apiResponse.isSuccess()) ? apiResponse.getData() : null;

                        MyApplication.saveUserDto("userDto", userDto);
//                        Log.d("switchIssue", "Parsed UserDto: " + userDto);
//                        triggerHomeScreenStatusCard();

                        String selectedCourseId = MyApplication.getStringData(PreferenceKeys.SECONDARY_COURSE_ID.getKey());
                        Log.d("switchIssue", "Selected CourseId from SharedPref: " + selectedCourseId);

                        String savedCourseId = PreferenceMethods.getSelectedOnBoardingId();
                        Log.d("switchIssue", "Saved CourseId in SharedPref: " + savedCourseId);
                        MyApplication.saveSharedPreferencesDoNotDelete(PreferenceKeys.SELECTED_COURSE_ID.getKey(), selectedCourseId);
                        if (userDto != null && userDto.getUserDto() != null
                                && userDto.getUserDto().getSwitchableCourseClassInfo() != null
                                && userDto.getUserDto().getSwitchableCourseClassInfo().getBottomSheetContentEntity() != null) {
                            Log.d("switchIssue", "Bottom Sheet Content" + userDto.getUserDto().getSwitchableCourseClassInfo().getBottomSheetContentEntity());
                            TriggerBottomSheetSwitchToNeet(userDto.getUserDto().getSwitchableCourseClassInfo().getBottomSheetContentEntity());
                            switchUserClassCourseRequestDto = userDto.getUserDto().getSwitchableCourseClassInfo();
                        } else {

//                            if (StringUtils.isNotEmpty(selectedCourseId)
//                                    && !savedCourseId.equals(selectedCourseId)) {
//                                Log.d("switchIssue", "CourseId changed. Clearing HomeScreen cache and refreshing activity.");
//                                homeScreenViewModel.removeDataModel();
//                                Log.d("switchIssue", "New CourseId saved: " + selectedCourseId);
//                                refreshActivity();
//                            } else {
//                                Log.d("switchIssue", "CourseId not changed. No refresh needed.");
//                            }
                        }
                        if (userDto != null) {
                            MyApplication.saveUserProfileLocally(userDto);

                            String newBatchCode = MyApplication.extractActiveBatchCode(userDto);
                            if (newBatchCode != null && StringUtils.isNotEmpty(priorBatchCode)
                                    && !newBatchCode.equals(priorBatchCode)
                                    && MyApplication.isBatchChangeAutoRefreshEnabled()
                                    && MyApplication.tryAcquireBatchCodeRefresh()) {
                                String newLang = MyApplication.extractActiveBatchLanguage(userDto);
                                boolean langChanged = newLang != null && StringUtils.isNotEmpty(priorLang) && !newLang.equals(priorLang);
                                Log.d(TAG, "batchCode changed: " + priorBatchCode + " → " + newBatchCode
                                        + (langChanged ? ", lang: " + priorLang + " → " + newLang : "") + " (HomeScreen)");

                                // Unsubscribe from old batch topics before changing
                                FirebaseMessagingManager.unSubscribeToUserDefaultTopics();
                                Log.d(TAG, "✅ Unsubscribed from old batch topics");

//                                if (langChanged) {
                                // Language changed with the batch — full activity recreation needed
                                // so all string resources and UI are re-inflated in the new locale.
                                // saveUserProfileLocally already persisted the new lang; just apply it and recreate.
                                homeScreenViewModel.removeDataModel();
//                                        homeScreenViewModel.refreshHomeScreenData(true);
                                homeScreenViewModel.triggerBatchCodeRefresh();

                                MyApplication.updateAppLocale(this, newLang);
                                refreshActivity();

                                // Subscribe to new batch topics after changes are saved
                                FirebaseMessagingManager.updateUserTopics();
                                Log.d(TAG, "✅ Subscribed to new batch topics");
                                return;
//                                } else {
//                                    homeScreenViewModel.removeDataModel();
//                                    homeScreenViewModel.refreshHomeScreenData(true);
//                                    homeScreenViewModel.triggerBatchCodeRefresh();
//                                }
                            }
                            logUserActiveDayEvent(userDto);
                        }

                        try {
                            FirebaseMessagingManager.attachUser(this);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        Log.d("switchIssue", "User profile saved locally.");
                        if (bottomNavigationManager != null) {
                            bottomNavigationManager.updateUI();
                            bottomNavigationManager.setExam(examId, exam);
                        }
                        Log.d("switchIssue", "Bottom navigation UI updated.");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                Log.d(TAG, "postError-HOMESCREENAPI: ");
                CommonMethods.showNegativeSnackbar(parentLayout, error.getMessage());
            }
        });
        myJsonObjectRequest2.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
    }

    private void checkForReferral() {
        if (MyApplication.getBooleanDataFromDoNotDeleteSharedPreference(PreferenceKeys.REFERRAL_REDIRECTED_PENDING.getKey()) && !SubscriptionNavigationHelper.hasActiveSubscriptionByBatch()) {
            MyApplication.removeFromDoNotDeleteSharedPreference(PreferenceKeys.REFERRAL_REDIRECTED_PENDING.getKey());
            Intent startActivityIntent = SubscriptionNavigationHelper.getSubscriptionPageIntent(this, RedirectionSource.REFERRAL_REDIRECT);
            startActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startActivityIntent);
        }
    }

    /**
     * Fires "user_active_day_{N}" once per calendar day, where N is the number of days
     * since the user's registration date. The last-fired date is persisted in SharedPreferences
     * so the event is skipped if it has already been logged today.
     */
    private void logUserActiveDayEvent(UserDetailsResponse userDto) {

        try {
            if (userDto == null || userDto.getUserDto() == null) return;
            Log.d(TAG, "---> User Active: " + userDto.getUserDto().getRegistrationDate());
            String registrationDate = userDto.getUserDto().getRegistrationDate();
            if (registrationDate == null || registrationDate.isEmpty()) return;

            // Fire at most once per calendar day
            String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
            String lastLogged = MyApplication.getStringData(PreferenceKeys.USER_ACTIVE_DAY_LAST_LOGGED.getKey());
            if (todayStr.equals(lastLogged)) return;

            Date regDate = parseRegistrationDate(registrationDate);
            if (regDate == null) return;

            long dayDiff = (System.currentTimeMillis() - regDate.getTime()) / (1000L * 60 * 60 * 24);

            // ---- Streak tracking ----
            // Compute yesterday's date string for consecutive-day detection
            Calendar yesterdayCal = Calendar.getInstance();
            yesterdayCal.add(Calendar.DAY_OF_YEAR, -1);
            String yesterdayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(yesterdayCal.getTime());

            int savedStreak = MyApplication.getIntData(PreferenceKeys.USER_STREAK_COUNT.getKey());
            if (savedStreak <= 0) savedStreak = 0; // treat -1 (never set) as 0

            String existingStreakStart = MyApplication.getStringData(PreferenceKeys.USER_STREAK_START_DATE.getKey());

            SimpleDateFormat ddMMyyyyFmt = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
            String todayFormatted = ddMMyyyyFmt.format(new Date());

            int newStreak;
            String newStreakStartDate;

            if (yesterdayStr.equals(lastLogged)) {
                // User was active yesterday → consecutive streak continues
                newStreak = savedStreak + 1;
                newStreakStartDate = (existingStreakStart != null && !existingStreakStart.isEmpty())
                        ? existingStreakStart
                        : todayFormatted;
            } else {
                // First time ever, or gap in activity → reset streak
                newStreak = 1;
                newStreakStartDate = todayFormatted;
            }

            MyApplication.saveData(PreferenceKeys.USER_STREAK_COUNT.getKey(), newStreak);
            MyApplication.saveData(PreferenceKeys.USER_STREAK_START_DATE.getKey(), newStreakStartDate);

            Map<String, String> params = new HashMap<>();
            params.put("registration_date", registrationDate);
            params.put("day_difference", String.valueOf(dayDiff));
            params.put("user_streak_day", String.valueOf(newStreak));
            params.put("user_streak_start_day", newStreakStartDate);
            MyApplication.logFirebaseBundleCreation("user_active_day_" + dayDiff, params);
            MyApplication.logFirebaseBundleCreation("user_active", params);

            Log.d(TAG, "--> Event: " + params.toString());
            MyApplication.saveData(PreferenceKeys.USER_ACTIVE_DAY_LAST_LOGGED.getKey(), todayStr);
            MyApplication.saveData(PreferenceKeys.USER_ACTIVE_DAYS.getKey(), dayDiff);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Parses an ISO 8601 datetime string (e.g. "2023-01-15T10:30:00.000Z") to a {@link Date}.
     * Only the date portion (first 10 chars) is used, in UTC, since day-level granularity is enough.
     */
    private Date parseRegistrationDate(String dateStr) {
        if (dateStr == null || dateStr.length() < 10) return null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            return sdf.parse(dateStr.substring(0, 10));
        } catch (ParseException e) {
            Log.e(TAG, "parseRegistrationDate failed for: " + dateStr, e);
            return null;
        }
    }

    private void triggerHomeScreenStatusCard() {
//        Intent intent = getIntent();
//        Log.d(TAG, "triggerHomeScreenStatusCard: " + intent.getBooleanExtra("show_topper_notes_status", false));
        if (MyApplication.getBoolData("show_topper_notes_status")) {
            toShowToppersNotesHomeScreenStatusCard();
        }
    }

    private void TriggerBottomSheetSwitchToNeet(BottomSheetContentResponse bottomSheetContentEntity) {
        if (isFinishing() || isDestroyed() || getSupportFragmentManager().isStateSaved()) return;
        popupData = bottomSheetContentEntity;
        Log.d(TAG, "TriggerBottomSheet: " + bottomSheetContentEntity);
        if (bottomSheetDialog != null && bottomSheetDialog.isAdded()) {
            bottomSheetDialog.dismiss();
            bottomSheetDialog = null;
        }
        try {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.home_neet_wclm_sheet_view.getActionName());
            bottomSheetDialog = new CustomBottomSheetDialog(this, this,
                    null, null, 0, true, bottomSheetContentEntity);
            bottomSheetDialog.show(getSupportFragmentManager(), "");
            bottomSheetDialog.setCancelable(false);
        } catch (Exception e) {
            Log.e(TAG, "Failed to show NEET switch bottom sheet", e);
        }
    }

    private void checkUserCampaignSubscription(boolean checkSubscription) {
        Map<String, Object> map = new HashMap<>();
        String PhoneNumber = MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey());
//        String selectedCourseId = MyApplication.getStringData(PreferenceKeys.SECONDARY_COURSE_ID.getKey());

        map.put("phoneNumber", PhoneNumber);
        map.put("key", BuildConfig.API_KEY);
//        map.put(PreferenceKeys.SELECTED_COURSE_ID_LOCAL.getKey(), selectedCourseId);
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(AV_GET_USER_BY_PHONE, map), null,
                response -> {
                    if (isFinishing() || isDestroyed()) return;
                    try {
                        Gson gson = new Gson();
                        TypeToken<ApiWrappedResponse<UserDetailsResponse>> token = new TypeToken<ApiWrappedResponse<UserDetailsResponse>>() {
                        };
                        ApiWrappedResponse<UserDetailsResponse> apiResponse = gson.fromJson(response.toString(), token.getType());
                        UserDetailsResponse userDto = (apiResponse != null && apiResponse.isSuccess()) ? apiResponse.getData() : null;
                        MyApplication.saveUserCampaignSubscription(userDto);
                        updateBottomNavigationUI();
                        updateSubscribedUserUI();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                CommonMethods.showNegativeSnackbar(parentLayout, error.getMessage());
            }
        });
        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
    }

    public static boolean isSameDay(long time1, long time2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(time1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(time2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public void refreshActivity() {
        long nowBatch = System.currentTimeMillis();
        if (nowBatch - lastRecreateMs <= recreateTimeInMillis) {
            Log.d(TAG, "batchCode recreate suppressed — too soon after last recreate");
            return;
        }
        lastRecreateMs = nowBatch;
        recreate();
    }

    private void initLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        settingsClient = LocationServices.getSettingsClient(this);
    }

    private void requestLocationPermission() {

        // Location already saved
        if (MyApplication.isLocationAlreadySaved()) {
            Log.d(TAG, "Location is already saved");
            handleWelcomeSequence();
            return;
        }

        // Already granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED) {
            proceedWithLocationIfNeeded();
            return;
        }

        int denialCount = getLocationDenialCount();

        if (denialCount > MAX_DENIAL_BEFORE_SETTINGS) {
            Log.d(TAG, "---> Denial count: " + denialCount);
            handleWelcomeSequence();
        } else {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );

            Map<String, String> eventParam = new HashMap<>();
            eventParam.put("denial_count", String.valueOf(denialCount));
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.user_location_permission_popup_triggered.getActionName(), eventParam);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        try {
            if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {

                Map<String, String> eventParam = new HashMap<>();
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    MyApplication.resetLocationDenialCount();
                    proceedWithLocationIfNeeded();
                    eventParam.put("action", "PERMISSION_GRANTED");
                } else {
                    MyApplication.incrementLocationDenialCount();
                    handleWelcomeSequence();
                    eventParam.put("action", "PERMISSION_DENIED");
                }
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.user_location_permission_popup_action.getActionName(), eventParam);

            }
        } catch (Exception ignored) {
        }
    }

    private boolean hasCoarsePermission() {
        return ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isLocationServiceEnabled() {
        LocationManager lm =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (lm == null) return false;

        return lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                || lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void proceedWithLocationIfNeeded() {
        Log.d(TAG, "---> proceedWithLocationIfNeeded ");
        if (!MyApplication.isLocationAlreadySaved()) {
            getLocationSafely();
            return;
        }
        handleWelcomeSequence();
    }

    private void openLocationSettingsManually() {
        handleWelcomeSequence();
    }

    public void getLocationSafely() {

        // Permission check
        if (!hasLocationPermission()) {
            Log.d(TAG, "Location permission not granted");
            handleWelcomeSequence();
            return;
        }

        // Location service check
        if (!isLocationServiceEnabled()) {
            promptEnableLocationService();
            return;
        }

        fetchLastKnownLocation();
    }

    private void promptEnableLocationService() {

        LocationRequest request =
                new LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        2000L
                ).build();

        LocationSettingsRequest settingsRequest =
                new LocationSettingsRequest.Builder()
                        .addLocationRequest(request)
                        .setAlwaysShow(true)
                        .build();

        settingsClient.checkLocationSettings(settingsRequest)
                .addOnSuccessListener(r -> startHighAccuracyUpdates())
                .addOnFailureListener(e -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (e instanceof ResolvableApiException && getLocationDenialCount() <= MAX_DENIAL_BEFORE_SETTINGS) {
                        try {
                            IntentSenderRequest intent =
                                    new IntentSenderRequest.Builder(
                                            ((ResolvableApiException) e).getResolution()
                                    ).build();
                            locationSettingsLauncher.launch(intent);
                        } catch (Exception ex) {
                            Log.e(TAG, "locationSettingsLauncher.launch failed: " + ex.getMessage());
                            openLocationSettingsManually();
                        }
                    } else {
                        openLocationSettingsManually();
                    }
                });

    }

    @SuppressLint("MissingPermission")
    private void startHighAccuracyUpdates() {

        if (locationConsumed.get()) return;

        Log.d(TAG, "Starting HIGH accuracy updates");

        LocationRequest request =
                new LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        2000L
                )
                        .setMinUpdateIntervalMillis(1000)
                        .setWaitForAccurateLocation(false)
                        .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {

                Location location = result.getLastLocation();
                if (location != null) {
//                    Log.d(TAG, "Location update received: " + location.toString());
                    removeLocationCallback();
                    handleLocationSuccess(location);
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
        );
    }

    private void removeLocationCallback() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @SuppressLint("MissingPermission")
    private void fetchLastKnownLocation() {
        if (!hasLocationPermission()) {
            return;
        }

        Log.d(TAG, "--> fetchLastKnownLocation");
        if (fusedLocationClient != null) {
            Log.d(TAG, "--> fetchLastKnownLocation: Not Null");
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        Log.d(TAG, "--> fetchLastKnownLocation: Success");
                        if (location != null) {
                            handleLocationSuccess(location);
                        } else {
                            startHighAccuracyUpdates();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.d(TAG, "--> fetchLastKnownLocation: Failure");
                        startHighAccuracyUpdates();
                    });
        }

        handleWelcomeSequence();
    }

    private void handleLocationSuccess(Location location) {

        if (!locationConsumed.compareAndSet(false, true)) return;

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        Log.d(TAG, "Latitude=" + latitude + ", Longitude=" + longitude);

        saveUserLocationInDb(longitude, latitude);
    }

    private void handleLocationUnavailable() {

        Log.d(TAG, "Location unavailable, continuing without location");
    }

    private void saveUserLocationInDb(double longitude, double latitude) {
        Map<String, Object> map = new HashMap<>();
        map.put("longitude", longitude);
        map.put("latitude", latitude);
        Log.d("apiParams", "saveUserLocationInDb: " + map);

        Map<String, String> eventParams = new HashMap<>();
        eventParams.put("longitude", String.valueOf(longitude));
        eventParams.put("latitude", String.valueOf(latitude));
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.user_location_lat_long.getActionName(), eventParams);

        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                CommonMethods.getUrlParam(SAVE_USER_LOCATION, map), null,
                response -> {
//                    Log.d(TAG, "saveUserLocationInDb: " + response.toString());
                    MyApplication.saveUserLocation(
                            PreferenceKeys.USER_LOCATION.getKey(),
                            latitude,
                            longitude
                    );
                    MyApplication.saveData(PreferenceKeys.USER_LOCATION_SAVED.getKey(), true);

                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
    }


}
