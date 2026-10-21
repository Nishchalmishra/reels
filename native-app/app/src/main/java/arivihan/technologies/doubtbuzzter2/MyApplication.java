package arivihan.technologies.doubtbuzzter2;
import arivihan.technologies.doubtbuzzter2.reelsdemo.*;
import arivihan.technologies.doubtbuzzter2.BuildConfig;

import static android.content.ContentValues.TAG;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.CHECK_SUBSCRIPTION;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.REGISTER_FOR_LIVE_SESSION;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.UPDATE_TODO_ORDER_STATE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.UPDATE_VIDEO_SELECTION;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.LocaleList;
import android.os.Looper;
import android.os.Parcelable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.compose.ui.platform.ComposeView;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.HtmlCompat;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.facebook.appevents.AppEventsLogger;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.crashlytics.CustomKeysAndValues;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.razorpay.AppSignatureHelper;

import arivihan.technologies.doubtbuzzter2.helper.WebViewPrewarm;
import arivihan.technologies.doubtbuzzter2.model.RescheduledLectureStatusDto;
import arivihan.technologies.doubtbuzzter2.model.chapter.live.ChapterPopUpDto;
import io.branch.referral.Branch;

import arivihan.technologies.doubtbuzzter2.compose.ComposePrewarm;
import arivihan.technologies.doubtbuzzter2.helper.NewNotificationHelper;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import arivihan.technologies.doubtbuzzter2.activity.ExceptionHandlingActivity;
import arivihan.technologies.doubtbuzzter2.activity.ToDoListMain;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.firebase.RemoteConfigKey;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceMethods;
import arivihan.technologies.doubtbuzzter2.fragments.MediumFragment;
import arivihan.technologies.doubtbuzzter2.helper.ActivityLauncher;
import arivihan.technologies.doubtbuzzter2.helper.BranchAnalyticsHelper;
import arivihan.technologies.doubtbuzzter2.helper.EnvironmentManager;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.helper.offline.OfflineTestManager;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionType;
import arivihan.technologies.doubtbuzzter2.listener.ActivityLifecycleListener;
import arivihan.technologies.doubtbuzzter2.listener.SubscriptionCallback;
import arivihan.technologies.doubtbuzzter2.listener.TodayClassApiCallback;
import arivihan.technologies.doubtbuzzter2.model.ArivihanLanguageEnum;
import arivihan.technologies.doubtbuzzter2.model.carousel.CarouselResponse;
import arivihan.technologies.doubtbuzzter2.model.DoubtSolutionStatus;
import arivihan.technologies.doubtbuzzter2.model.DoubtStatusDto;
import arivihan.technologies.doubtbuzzter2.model.HighlistModel;
import arivihan.technologies.doubtbuzzter2.model.HomeScreenCacheData;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureDto;
import arivihan.technologies.doubtbuzzter2.model.NotificationModel;
import arivihan.technologies.doubtbuzzter2.model.PendingSubscriptionEntry;
import arivihan.technologies.doubtbuzzter2.model.crash_course.TodayClassResponse;
import arivihan.technologies.doubtbuzzter2.model.subject.SubjectResponse;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionCheckDto;
import arivihan.technologies.doubtbuzzter2.model.TestEvaluationStatusEnum;
import arivihan.technologies.doubtbuzzter2.model.TestGenerationStatus;
import arivihan.technologies.doubtbuzzter2.model.TestGenerationStatusDto;
import arivihan.technologies.doubtbuzzter2.model.TestSeriesAnalytics;
import arivihan.technologies.doubtbuzzter2.model.batch.test_series.TestSeriesCardResponse;
import arivihan.technologies.doubtbuzzter2.model.TestSeriesQuestionDto;
import arivihan.technologies.doubtbuzzter2.model.ToDoStateUser;
import arivihan.technologies.doubtbuzzter2.model.TopperNoteDownloadedStatusDto;
import arivihan.technologies.doubtbuzzter2.model.ToppersHomeScreenModel;
import arivihan.technologies.doubtbuzzter2.model.UserDto;
import arivihan.technologies.doubtbuzzter2.model.UserOngoingTestStateDto;
import arivihan.technologies.doubtbuzzter2.model.UserTestEvaluationStautsObject;
import arivihan.technologies.doubtbuzzter2.model.asat.UserAsatFirebaseData;
import arivihan.technologies.doubtbuzzter2.model.batch.user.UserDetailsResponse;
import arivihan.technologies.doubtbuzzter2.model.competitive.HomeScreenCardResponse;
import arivihan.technologies.doubtbuzzter2.model.competitive.ToppersResponseDto;
import arivihan.technologies.doubtbuzzter2.model.firebase.rdb.NeetWeakTopicRdbModel;
import arivihan.technologies.doubtbuzzter2.model.offline.type.OfflineNoteConfigModel;
import arivihan.technologies.doubtbuzzter2.model.pdf_circle.PDFCircleDailyCounter;
import arivihan.technologies.doubtbuzzter2.model.bottom_nav.BottomNavTabNavigation;
import arivihan.technologies.doubtbuzzter2.model.bottom_nav.CompetitiveBatchNavConfig;
import arivihan.technologies.doubtbuzzter2.model.bottom_nav.CompetitiveBottomNavTab;
import arivihan.technologies.doubtbuzzter2.model.remote_config.ActivityNavigationConfig;
import arivihan.technologies.doubtbuzzter2.utils.PdfUtils;
import arivihan.technologies.doubtbuzzter2.viewModel.HomeScreenViewModel;
import arivihan.technologies.doubtbuzzter2.viewModel.MediaViewModel;
import arivihan.technologies.doubtbuzzter2.viewModel.MicroLectureViewModel;
import arivihan.technologies.doubtbuzzter2.viewModel.NotesViewModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.viewModel.TooltipViewModel;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import lombok.Setter;

public class MyApplication extends Application {

    private static MyApplication me;

    private static SharedPreferences sharedPreferences;
    private static SharedPreferences sharedPreferencesDonotDelete;
    private static FirebaseCrashlytics instance;
    private static FirebaseAnalytics firebaseAnalytics;
    private static AppEventsLogger facebookEventLogger;
    private static AppSignatureHelper appSignatureHelper;
    // Endpoint is managed exclusively by EnvironmentManager — do not add a local field here.
    private HomeScreenViewModel homeScreenViewModel;
    private TooltipViewModel tooltipViewModel;
    private MediaViewModel mediaViewModel;
    private NotesViewModel notesViewModel;
    private MicroLectureViewModel microLectureViewModel;
    private static DatabaseReference userSelectedVideoQualityDatabaseReference;
    private static DatabaseReference userSelectedDownloadQualityDatabaseReference;
    @Setter
    private static TodayClassApiCallback todayClassApiCallback;
    public static final int BACK_PRESS_INTERVAL = 1500; // 1.5 seconds

    public static DatabaseReference getTestListEvaluationDatabaseReference() {
        String key;
        if (MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey()))
            key = "/" + MyApplication.getStringData(PreferenceKeys.CLASS_ID.getKey()) + "_" + "board" + "_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        else
            key = "/" + MyApplication.getStringData(PreferenceKeys.CLASS_ID.getKey()) + "_" + MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey()) + "_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        return FirebaseDatabase.getInstance().
                getReference("users/testListEvaluationStatus/" + MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()) + key);
    }

    public static DatabaseReference getReferenceForUserRoutine() {
        String key;
        if (MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey()))
            key = "/" + MyApplication.getStringData(PreferenceKeys.CLASS_ID.getKey()) + "_" + "board" + "_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        else
            key = "/" + MyApplication.getStringData(PreferenceKeys.CLASS_ID.getKey()) + "_" + MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey()) + "_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        return FirebaseDatabase.getInstance().getReference("users/userRoutineStatus/" + MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()) + key);
    }

    public static DatabaseReference getTestStatusDatabaseReference() {
        String key;
        if (MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey()))
            key = "/" + MyApplication.getStringData(PreferenceKeys.CLASS_ID.getKey()) + "_" + "board" + "_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        else
            key = "/" + MyApplication.getStringData(PreferenceKeys.CLASS_ID.getKey()) + "_" + MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey()) + "_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        return FirebaseDatabase.getInstance().
                getReference("users/testEvaluationStatus/" + MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()) + key);
    }


    public static DatabaseReference getToppersNotesStatusDatabaseReference() {
        String batchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
        String path = "users/toppers_notes/" + MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()) + "/" + batchCode;
        Log.d("toppers_notes_rtm", path);
        return FirebaseDatabase.getInstance().getReference(path);
    }

    public static DatabaseReference getASATStatusDatabaseReference() {
        boolean isBoard = MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey());
        String courseId = isBoard ? MyApplication.getStringData(PreferenceKeys.SELECTED_COURSE_ID_LOCAL.getKey()) : MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey());
        String path = "users/asat_result/" + MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()) + "/" + MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()) + "::" + MyApplication.getStringData(PreferenceKeys.CLASS_ID.getKey()) + "::" + courseId;
        Log.d("AsatKey", path);
        return FirebaseDatabase.getInstance().getReference(path);
    }

    public static DatabaseReference getNeetWeakTopicDatabaseReference() {
        String path = "users/weak-topic-extraction-status/" + MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
        return FirebaseDatabase.getInstance().getReference(path);
    }

    public static DatabaseReference getDoubtStatusDatabaseReference() {
        return FirebaseDatabase.getInstance().
                getReference("users/doubtEvaluationStatus/" +
                        MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
    }

    public static DatabaseReference getTestGenerationDatabaseReference(String requestId) {
        return FirebaseDatabase.getInstance()
                .getReference("users/testgeneration/"
                        + MyApplication.getStringData(PreferenceKeys.USER_ID.getKey())
                        + "/" + requestId);
    }

    public static DatabaseReference getTestGenerationParentDatabaseReference() {
        return FirebaseDatabase.getInstance()
                .getReference("users/testgeneration/"
                        + MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
    }

    public static TestGenerationStatusDto parseTestGenerationResult(String requestId, Map<String, Object> data) {
        TestGenerationStatusDto dto = new TestGenerationStatusDto();
        try {
            dto.setRequestId(requestId);
            dto.setTestName((String) data.get("testName"));
            dto.setStatus(TestGenerationStatus.getVal((String) data.get("status")));
            Object startTime = data.get("startTime");
            if (startTime instanceof Long) dto.setStartTime((Long) startTime);
            else if (startTime instanceof Number) dto.setStartTime(((Number) startTime).longValue());
            Object closeTime = data.get("closeTime");
            if (closeTime instanceof Long) dto.setCloseTime((Long) closeTime);
            else if (closeTime instanceof Number) dto.setCloseTime(((Number) closeTime).longValue());
        } catch (Exception e) {
            Log.d("parseTestGenResult", "Error: " + e.getMessage());
        }
        return dto;
    }

    public static DatabaseReference getVirtualChatUnseenCountReference() {
        return FirebaseDatabase.getInstance()
                .getReference("users/virtual_chat_unseen_message_count/"
                        + MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
    }

    public static DatabaseReference getPendingSubscriptionDatabaseReference() {
        return FirebaseDatabase.getInstance().
                getReference("users/pendingSubscriptionStatus/" + MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
    }

    private static String getLiveSessionKey() {
        String key;
        if (MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey())) {
            key = "liveSession" + "_"
                    + MyApplication.getStringData(PreferenceKeys.CLASS_ID.getKey())
                    + MyApplication.getStringData(PreferenceKeys.SELECTED_COURSE_ID_LOCAL.getKey())
                    + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
            Log.d("LiveSessionHelper", "Board switch is ON. Key: " + key);
        } else {
            key = "liveSession" + "_"
                    + MyApplication.getStringData(PreferenceKeys.CLASS_ID.getKey()) + "_"
                    + MyApplication.getStringData(PreferenceKeys.SELECTED_COURSE_ID_LOCAL.getKey()) + "_"
                    + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
            Log.d("LiveSessionHelper", "Board switch is OFF. Key: " + key);
        }
        return key;
    }

    public static boolean getLiveSessionStatus() {
        String key = "";
        try {
            key = getLiveSessionKey();

            long savedTime = MyApplication.getLongData(key + "_timestamp");
            if (savedTime != -1) {
                //Delete after -24 * 60 * 60 * 1000(24hrs)
                long currentTime = System.currentTimeMillis();
                if (currentTime - savedTime >= getFirebaseRemoteConfig().getLong("delete_live_session_time_interval")) {
                    deleteLiveSessionStatus();
                    removeString(key + "_timestamp");
                    Log.d("LiveSessionHelper", "Session expired after 24 hrs. Key: " + key);
                    return false;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return getBoolData(key);
    }

    public static void insertLiveSessionStatus(boolean value) {
        String key = getLiveSessionKey();
        saveData(key, value);

        // Save insert timestamp
        long currentTime = System.currentTimeMillis();
        saveData(key + "_timestamp", currentTime);

        Log.d("LiveSessionHelper", "Inserted/Updated session status: " + value
                + " for key: " + key + " at time: " + currentTime);
    }

    public static void deleteLiveSessionStatus() {
        String key = getLiveSessionKey();
        removeString(key);
        removeString(key + "_timestamp"); // also delete timestamp
        Log.d("LiveSessionHelper", "Deleted session status for key: " + key);
    }

    public static void registerForLiveSession() {
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                REGISTER_FOR_LIVE_SESSION, null,
                response -> {
                    Log.d("Registered for live session", "Successfully updated.");
                    logFirebaseBundleCreation(FirebaseEvents.registered_for_live_session.getActionName());
                    if (todayClassApiCallback != null) {
                        todayClassApiCallback.onApiCompleted();
                    }
                },
                new VolleyGlobalErrorListener(getInstance()) {
                    @Override
                    public void postError(VolleyError error) {
                        Log.e("UPDATE_TODO_ORDER_STATE", error.getMessage());
                    }
                });

        myJsonObjectRequest.setTag("registerForLiveSession");
        MyVolleySingleton.getInstance(getInstance()).addToRequestQueue(myJsonObjectRequest);
    }

    public static DatabaseReference getPendingSubscriptionDatabaseReference(String planId) {
        return FirebaseDatabase.getInstance().
                getReference("users/pendingSubscriptionStatus/" + MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()) + "/" + planId);
    }

    public static DatabaseReference getDoubtStatusDatabaseReferenceFotChat(String sessionId) {
        return FirebaseDatabase.getInstance().
                getReference("users/doubtEvaluationStatus/" + MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()) + "/" + sessionId);
    }

    public static DatabaseReference getUserSelectedVideoQualityDatabaseReference() {
        userSelectedVideoQualityDatabaseReference =
                FirebaseDatabase.getInstance().getReference("users/UserSelectedVideoQuality/" + MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
        return userSelectedVideoQualityDatabaseReference;
    }

    public static void getUserSelectedVideoQuality(String key) {
        getUserSelectedVideoQualityDatabaseReference();
        userSelectedVideoQualityDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        saveData(key, dataSnapshot.getValue(String.class));
                    } catch (Exception e) {
                        Log.d("onDataChange", e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error fetching data: " + databaseError.getMessage());
            }
        });
    }

    public static DatabaseReference getUserSelectedDownloadQualityDatabaseReference() {
        userSelectedDownloadQualityDatabaseReference =
                FirebaseDatabase.getInstance().getReference("users/UserSelectedVideoQuality/" + MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
        return userSelectedDownloadQualityDatabaseReference;
    }

    public static void getUserSelectedDownloadQuality(String key) {
        getUserSelectedDownloadQualityDatabaseReference();
        userSelectedDownloadQualityDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    MyApplication.saveData(key, dataSnapshot.getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error fetching data: " + databaseError.getMessage());
            }
        });
    }

    public static String getHashCode() {
        for (String appSignature : appSignatureHelper.getAppSignatures()) {
            Log.d("appSignature", appSignature);
            return appSignature;
        }
        return "HJx+ZiscyuC";
    }

    public static void purchaseFirebaseEvent(String planId, String transactionID, double amount, String paymentType) {
        purchaseFirebaseEvent(planId, transactionID, amount, paymentType, "unknown");
    }

    public static void purchaseFirebaseEvent(String planId, String transactionID, double amount, String paymentType, String navigationFrom) {
        Bundle purchaseBundle = new Bundle();
        purchaseBundle.putString(FirebaseAnalytics.Param.TRANSACTION_ID, transactionID);
        purchaseBundle.putString(FirebaseAnalytics.Param.CURRENCY, "INR");
        purchaseBundle.putDouble(FirebaseAnalytics.Param.VALUE, amount);
        purchaseBundle.putString(FirebaseAnalytics.Param.PAYMENT_TYPE, paymentType);
        purchaseBundle.putString(FirebaseAnalytics.Param.ITEM_ID, planId);
        purchaseBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, planId);
        purchaseBundle.putString("navigation_from", navigationFrom);
        logFirebaseEventOffMainThread(FirebaseAnalytics.Event.PURCHASE, purchaseBundle);
        Log.d("firebasePurchase", "purchaseFirebaseEvent: " + purchaseBundle);
    }

    public static void logFirebaseEventOffMainThread(String eventName, Bundle bundle) {
        try {
            ANALYTICS_EXECUTOR.execute(() -> {
                try {
                    if (firebaseAnalytics != null) {
                        firebaseAnalytics.logEvent(eventName, bundle);
                    }
                } catch (Exception ignored) {
                }
            });
        } catch (Exception ignored) {
        }
    }

    public static void purchaseFirebaseEvent(String planId, int total) {
        Bundle addToCartBundle = new Bundle();
        addToCartBundle.putString(FirebaseAnalytics.Param.ITEM_ID, planId);
        addToCartBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, planId);
        addToCartBundle.putDouble(FirebaseAnalytics.Param.VALUE, total);
        addToCartBundle.putString(FirebaseAnalytics.Param.CURRENCY, "INR");
        logFirebaseEventOffMainThread(FirebaseAnalytics.Event.ADD_TO_CART, addToCartBundle);
        Log.d("firebasePurchase", "purchaseFirebaseEvent: " + addToCartBundle);

    }

    public static void syncOfflineTestData() {
        OfflineTestManager offlineTestManager = OfflineTestManager.getInstance();
        offlineTestManager.syncAllOfflineTests(MyApplication.getInstance());
    }

    public static void removeOnboardingSharedPreferencesKey() {
        removeString(PreferenceKeys.AUTHENTICATION_DONE.getKey());
        removeString(PreferenceKeys.USER_NAME_ENTERED_DURING_ONBOARDING.getKey());
        removeString(PreferenceKeys.NEET_CLASS_SELECTION_PENDING.getKey());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appSignatureHelper = new AppSignatureHelper(this);

//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        me = this;
        sharedPreferencesDonotDelete = getSharedPreferences("shared_preference_do_not_delete", Context.MODE_PRIVATE);
        sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE);

        if (BuildConfig.REELS_DEMO) DemoEnvironment.initialize(this);
        registerActivityLifecycleCallbacks(new ActivityLifecycleListener(this));
        if (!BuildConfig.REELS_DEMO) {
        // ── Branch.io deep-linking SDK ──────────────────────────────────────
        // Must be initialised before any Activity is created. The Branch keys and
        // TestMode flag are read from AndroidManifest meta-data. Session init /
        // deep-link handling happens in the launcher Activity (StartActivity).
        if (BuildConfig.DEBUG) {
            Branch.enableLogging();
        }
        Branch.getAutoInstance(this);

        initFirebaseAnalyticsOffMainThread();
        Firebase.setAndroidContext(this);
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(1)
                .build();
        remoteConfig.setConfigSettingsAsync(configSettings);
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        try {
            instance = FirebaseCrashlytics.getInstance();
            remoteConfig.fetchAndActivate().addOnCompleteListener(
                    task -> {
                    });
        } catch (Exception e) {

        }

        prewarmWebViewWhenIdle();

        facebookEventLogger = AppEventsLogger.newLogger(this);
        } // isolated demo does not initialise production SDKs

        tooltipViewModel = new ViewModelProvider.AndroidViewModelFactory(this).create(TooltipViewModel.class);
        homeScreenViewModel = new ViewModelProvider.AndroidViewModelFactory(this).create(HomeScreenViewModel.class);
        mediaViewModel = new ViewModelProvider.AndroidViewModelFactory(this).create(MediaViewModel.class);
        microLectureViewModel = new ViewModelProvider.AndroidViewModelFactory(this).create(MicroLectureViewModel.class);
        notesViewModel = new ViewModelProvider.AndroidViewModelFactory(this).create(NotesViewModel.class);
        NewNotificationHelper.ensureChannel(this);
        prewarmFonts();
        prewarmCompose();
        setUpExceptionHandler();
    }
    private void initFirebaseAnalyticsOffMainThread() {
        final Context appContext = getApplicationContext();
        try {
            ANALYTICS_EXECUTOR.execute(() -> {
                try {
                    firebaseAnalytics = FirebaseAnalytics.getInstance(appContext);
                } catch (Exception e) {
                    Log.e(TAG, "FirebaseAnalytics init failed", e);
                }
            });
        } catch (Exception ignored) {
            // RejectedExecutionException during shutdown - analytics just stays uninitialised.
        }
    }

    private void prewarmWebViewWhenIdle() {
        Looper.myQueue().addIdleHandler(() -> {
            new Handler(Looper.getMainLooper())
                    .postDelayed(() -> WebViewPrewarm.prewarm(this), 3000);
            return false; // one-shot
        });
    }

    /**
     * Loads all custom fonts on a background thread so Typeface.nativeCreateFromArray()
     * doesn't block the main thread during the first layout inflation of any activity.
     */
    private void prewarmFonts() {
        final int[] fontResIds = {
            R.font.urbanist_regular, R.font.urbanist_medium, R.font.urbanist_bold,
            R.font.urbanist_semi_bold, R.font.urbanist_extra_bold, R.font.urbanist_light_italic,
            R.font.nunito_regular, R.font.nunito_medium, R.font.nunito_semibold,
            R.font.nunito_bold, R.font.nunito_extra_bold,
            R.font.inter, R.font.inter_medium,
            R.font.opensans_regular, R.font.opensans_bold, R.font.opensans_light,
            R.font.opensans_semibolditalic,
            R.font.comfortaa, R.font.poppins_light, R.font.rubik_semi_bold,
            R.font.bangers_regular
        };
        new Thread(() -> {
            for (int resId : fontResIds) {
                try {
                    ResourcesCompat.getFont(this, resId);
                } catch (Exception ignored) {}
            }
        }).start();
    }



    //compose preview - load time reduce
    private void prewarmCompose() {
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                long startTime = System.currentTimeMillis();
                Log.d("ComposePrewarm", "Starting prewarm...");
                ComposePrewarm.prewarm(this);
                long duration = System.currentTimeMillis() - startTime;
                Log.d("ComposePrewarm", "Completed in " + duration + "ms");
            } catch (Exception e) {
                Log.e("ComposePrewarm", "Failed to prewarm Compose", e);
            }
        });
    }




    public TooltipViewModel getTooltipViewModel() {
        return tooltipViewModel;
    }

    public HomeScreenViewModel getHomeScreenViewModel() {
        return homeScreenViewModel;
    }

    public MediaViewModel getMediaViewModel() {
        return mediaViewModel;
    }

    public MicroLectureViewModel getMicroLectureViewModel() {
        return microLectureViewModel;
    }

    public NotesViewModel getNotesViewModel() {
        return notesViewModel;
    }

    public static void clearSharedPref() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public static String getPhoneNumber() {
        return getFirebaseRemoteConfig().getString("whatsapp_number_chat");
    }


    private static JSONObject getSocialUrlsData() throws Exception {
        String socialUrlsJson = getFirebaseRemoteConfig().getString("social_urls_v3");
        if (socialUrlsJson == null || socialUrlsJson.trim().isEmpty()) {
            throw new Exception("social_urls_v3 remote config is empty");
        }
        JSONObject socialUrls = new JSONObject(socialUrlsJson);
        String batchKey = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
        JSONObject data = (batchKey != null) ? socialUrls.optJSONObject(batchKey) : null;
        if (data == null) {
            data = socialUrls.optJSONObject("default");
        }
        if (data == null) {
            throw new Exception("No matching batch key or default found in social_urls_v3");
        }
        return data;
    }

    public static String getPhoneNumberWith91() {
        try {
            return getSocialUrlsData().getString("whatsapp_number");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getInstagramUrl() {
        try {
            return getSocialUrlsData().getString("instagram_url");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getYoutubeUrl() {
        try {
            return getSocialUrlsData().getString("youtube_url");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    public static boolean getBooleanValueFromRemoteConfig(String key) {
        String value = getFirebaseRemoteConfig().getString(key);
        return "true".equalsIgnoreCase(value);
    }

    public static void removeString(String key) {
        sharedPreferences.edit().remove(key).commit();
    }

    public static void removeFromDoNotDeleteSharedPreference(String key) {
        sharedPreferencesDonotDelete.edit().remove(key).commit();
    }

    public static void saveUserProfileLocally(UserDto user) {
        MyApplication.saveData(PreferenceKeys.NAME.getKey(), user.getUsername());
        MyApplication.saveData(PreferenceKeys.DISPLAY_NAME.getKey(), user.getDisplayName());
        MyApplication.saveData(PreferenceKeys.COURSE_ID.getKey(), user.getCourseId());
        MyApplication.saveSharedPreferencesDoNotDelete(PreferenceKeys.SELECTED_COURSE_ID.getKey(), user.getSecondaryCourseId());
        MyApplication.saveData(PreferenceKeys.SECONDARY_COURSE_ID.getKey(), user.getSecondaryCourseId());
        MyApplication.saveData(PreferenceKeys.SELECTED_COURSE_ID_LOCAL.getKey(), user.getSecondaryCourseId());
        Log.d("courseidissue", "onViewCreated  save  profile locally : " +
                MyApplication.getStringData(PreferenceKeys.SELECTED_COURSE_ID_LOCAL.getKey()));
        MyApplication.saveData(PreferenceKeys.USER_ID.getKey(), user.getUserId());
        BranchAnalyticsHelper.setUserIdentity(user.getUserId());
        MyApplication.saveData(PreferenceKeys.USER_EMAIL.getKey(), user.getEmail());
        MyApplication.saveData(PreferenceKeys.COURSE_NAME.getKey(), user.getCourseName());
        MyApplication.saveData(PreferenceKeys.CLASS_NAME.getKey(), user.getClassName());
        MyApplication.saveData(PreferenceKeys.BOARD_CODE.getKey(), user.getBoardCode());
        MyApplication.saveData(PreferenceKeys.CLASS_ID.getKey(), user.getClassId());
        MyApplication.saveData(PreferenceKeys.YOUR_POINTS.getKey(), user.getPoints());
        MyApplication.saveData(PreferenceKeys.FREE_DOUBT.getKey(), user.getFreeDoubt());
        MyApplication.saveData(PreferenceKeys.IMAGE_URI.getKey(), user.getImageUrl());
        MyApplication.saveData(PreferenceKeys.REFER_CODE.getKey(), user.getMyReferCode());
        MyApplication.saveData(PreferenceKeys.USER_BOARD.getKey(), user.getBoardId());
        MyApplication.saveData(PreferenceKeys.USER_BOARD_NAME.getKey(), user.getBoardName());
        MyApplication.saveData(PreferenceKeys.ASKED_DOUBT.getKey(), user.getAskedDoubt());
        MyApplication.saveData(PreferenceKeys.SHOW_MOCK_VIDEO.getKey(), user.isShowMockVideo());
        MyApplication.saveData(PreferenceKeys.SYLLABUS_GENERATED.getKey(), user.getSyllabusGenerated());
        MyApplication.saveData(PreferenceKeys.SUBSCRIBED.getKey(), user.getSubscribed() != null && user.getSubscribed());
//        saveUserCampaignSubscription(user);
        MyApplication.saveData(PreferenceKeys.CAMPAIGN_SUBSCRIBED.getKey(), user.getCampaignSubscribed());
        MyApplication.saveData(PreferenceKeys.RAJASTHAN_BOARD_SELECTED.getKey(), user.isRajasthanBoard());
        MyApplication.saveData(PreferenceKeys.MY_LANG.getKey(), user.getSelectedLanguage() != null ? user.getSelectedLanguage().getCode() : ArivihanLanguageEnum.ENGLISH.getCode());
        MyApplication.saveData(PreferenceKeys.GO_TO_COACHING.getKey(), user.isGoToCoaching());
        if (StringUtils.isNotEmpty(user.getGender())) {
            MyApplication.saveData(PreferenceKeys.GENDER.getKey(), user.getGender());
        }
        String subType = (user.getSubscriptionInfo() != null && StringUtils.isNotEmpty(user.getSubscriptionInfo().getSubscriptionType()))
                ? user.getSubscriptionInfo().getSubscriptionType()
                : SubscriptionType.BASIC.name();
        MyApplication.saveData(PreferenceKeys.USER_SUBSCRIPTION_TYPE.getKey(), subType);
        if (user.getSubscriptionInfo() != null) {
            MyApplication.saveData(PreferenceKeys.USER_SUBSCRIPTION_PLAN_ID.getKey(), user.getSubscriptionInfo().getSubscriptionPlanId());
            MyApplication.saveData(PreferenceKeys.USER_SUBSCRIPTION_PROFILE_VIDEO_URL.getKey(), user.getSubscriptionInfo().getSubscriptionCompletedVideoUrl());
        }

        setLocale();
        Log.d(TAG, "saveUserProfileLocally: " + user.getSelectedCourseId());
    }

    public static void saveUserProfileLocally(UserDetailsResponse user) {
        UserDto userDto = user.getUserDto();
        if (userDto != null) {
            MyApplication.saveData(PreferenceKeys.USER_ID.getKey(), userDto.getUserId());
            BranchAnalyticsHelper.setUserIdentity(userDto.getUserId());
            MyApplication.saveData(PreferenceKeys.DISPLAY_NAME.getKey(), userDto.getUsername());
            MyApplication.saveData(PreferenceKeys.NAME.getKey(), userDto.getUsername());
            MyApplication.saveData(PreferenceKeys.COURSE_ID.getKey(), userDto.getCourseId());
            MyApplication.saveSharedPreferencesDoNotDelete(PreferenceKeys.SELECTED_COURSE_ID.getKey(), userDto.getSecondaryCourseId());
            MyApplication.saveData(PreferenceKeys.SECONDARY_COURSE_ID.getKey(), userDto.getSecondaryCourseId());
            MyApplication.saveData(PreferenceKeys.SELECTED_COURSE_ID_LOCAL.getKey(), userDto.getSecondaryCourseId());
            MyApplication.saveData(PreferenceKeys.COURSE_NAME.getKey(), userDto.getCourseName());
            MyApplication.saveData(PreferenceKeys.CLASS_NAME.getKey(), userDto.getClassName());
            MyApplication.saveData(PreferenceKeys.BOARD_CODE.getKey(), userDto.getBoardCode());
            MyApplication.saveData(PreferenceKeys.CLASS_ID.getKey(), userDto.getClassId());

            MyApplication.saveData(PreferenceKeys.USER_BOARD.getKey(), userDto.getBoardId());
            MyApplication.saveData(PreferenceKeys.USER_BOARD_NAME.getKey(), userDto.getBoardName());
            MyApplication.saveData(PreferenceKeys.ASKED_DOUBT.getKey(), userDto.getAskedDoubt());
            MyApplication.saveData(PreferenceKeys.SUBSCRIBED.getKey(), userDto.getSubscribed() != null && userDto.getSubscribed());
            MyApplication.saveData(PreferenceKeys.GO_TO_COACHING.getKey(), userDto.isGoToCoaching());

            MyApplication.saveData(PreferenceKeys.IMAGE_URI.getKey(), userDto.getImageUrl());
            MyApplication.saveData(PreferenceKeys.USER_ADDRESS.getKey(), userDto.getAddress());
            MyApplication.saveData(PreferenceKeys.PHONE_NUMBER.getKey(), String.valueOf(userDto.getPhoneNumber()));
            MyApplication.saveData(PreferenceKeys.COACHING_ID.getKey(), userDto.getCoachingId());
            MyApplication.saveData(PreferenceKeys.USER_EMAIL.getKey(), userDto.getEmail());
            MyApplication.saveData(PreferenceKeys.YOUR_POINTS.getKey(), userDto.getPoints());
            MyApplication.saveData(PreferenceKeys.FREE_DOUBT.getKey(), userDto.getFreeDoubt());
            MyApplication.saveData(PreferenceKeys.REFER_CODE.getKey(), userDto.getMyReferCode());
            MyApplication.saveData(PreferenceKeys.SHOW_MOCK_VIDEO.getKey(), userDto.isShowMockVideo());
            MyApplication.saveData(PreferenceKeys.CAMPAIGN_SUBSCRIBED.getKey(), userDto.getCampaignSubscribed());
            MyApplication.saveData(PreferenceKeys.MY_LANG.getKey(), userDto.getSelectedLanguage() != null
                    ? userDto.getSelectedLanguage().getCode()
                    : ArivihanLanguageEnum.ENGLISH.getCode());
            MyApplication.saveData(PreferenceKeys.SYLLABUS_GENERATED.getKey(), userDto.getSyllabusGenerated());
            if (StringUtils.isNotEmpty(userDto.getGender())) {
                MyApplication.saveData(PreferenceKeys.GENDER.getKey(), userDto.getGender());
            }
            String subType = (userDto.getSubscriptionInfo() != null && StringUtils.isNotEmpty(userDto.getSubscriptionInfo().getSubscriptionType()))
                    ? userDto.getSubscriptionInfo().getSubscriptionType()
                    : SubscriptionType.BASIC.name();
            MyApplication.saveData(PreferenceKeys.USER_SUBSCRIPTION_TYPE.getKey(), subType);
            if (userDto.getSubscriptionInfo() != null) {
                MyApplication.saveData(PreferenceKeys.USER_SUBSCRIPTION_PLAN_ID.getKey(), userDto.getSubscriptionInfo().getSubscriptionPlanId());
                MyApplication.saveData(PreferenceKeys.USER_SUBSCRIPTION_PROFILE_VIDEO_URL.getKey(), userDto.getSubscriptionInfo().getSubscriptionCompletedVideoUrl());
            }

        }
        MyApplication.saveData(PreferenceKeys.IS_NEW_USER.getKey(), user.getIsNewUser() != null && user.getIsNewUser());
        MyApplication.saveData(PreferenceKeys.BOARD_SWITCH.getKey(), user.isBoardSwitch());

        Log.d(TAG, "saveUserProfileLocally: " + user.toString());
        if (user.getBatches() != null && !user.getBatches().isEmpty()) {
            // Save full batch list as JSON
            PreferenceMethods.saveUserBatchList(user.getBatches());

            // Find active batch; fall back to first if none is marked active
            UserDetailsResponse.BatchDetail activeBatch = null;
            for (UserDetailsResponse.BatchDetail b : user.getBatches()) {
                if (Boolean.TRUE.equals(b.getActive())) {
                    activeBatch = b;
                    break;
                }
            }

            // Default 1st batch in case no one is active
            if (activeBatch == null) {
                activeBatch = user.getBatches().get(0);
            }
            saveUserActiveBatch(activeBatch);
        }
    }

    public static void saveUserActiveBatch(UserDetailsResponse.BatchDetail activeBatch) {
        MyApplication.saveData(PreferenceKeys.BATCH_ID.getKey(), String.valueOf(activeBatch.getBatchId()));
        MyApplication.saveData(PreferenceKeys.BATCH_NAME.getKey(), activeBatch.getBatchName());
        MyApplication.saveData(PreferenceKeys.BATCH_DISPLAY_NAME.getKey(), activeBatch.getBatchDisplayName());
        MyApplication.saveData(PreferenceKeys.BATCH_CODE.getKey(), activeBatch.getBatchCode());
        MyApplication.saveData(PreferenceKeys.MY_LANG.getKey(), ArivihanLanguageEnum.fromStringToCode(activeBatch.getLanguage()));
        MyApplication.saveData(PreferenceKeys.EXAM_ID.getKey(), String.valueOf(activeBatch.getExamId()));
        MyApplication.saveData(PreferenceKeys.EXAM_CODE.getKey(), activeBatch.getExamCode());
        MyApplication.saveData(PreferenceKeys.EXAM_TYPE.getKey(), activeBatch.getExamType());
        MyApplication.saveData(PreferenceKeys.EXAM_NAME.getKey(), activeBatch.getExamName());
        MyApplication.saveData(PreferenceKeys.GRADE_ID.getKey(), String.valueOf(activeBatch.getGradeId()));
        MyApplication.saveData(PreferenceKeys.GRADE_NAME.getKey(), activeBatch.getGradeName());
        MyApplication.saveData(PreferenceKeys.GRADE_CODE.getKey(), activeBatch.getGradeCode());
        MyApplication.saveData(PreferenceKeys.YEAR_ID.getKey(), String.valueOf(activeBatch.getYearId()));
        MyApplication.saveData(PreferenceKeys.YEAR_NAME.getKey(), activeBatch.getYearName());
        MyApplication.saveData(PreferenceKeys.YEAR_CODE.getKey(), activeBatch.getYearCode());
        MyApplication.saveData(PreferenceKeys.STREAM_ID.getKey(), String.valueOf(activeBatch.getStreamId()));
        MyApplication.saveData(PreferenceKeys.STREAM_NAME.getKey(), activeBatch.getStreamName());
        MyApplication.saveData(PreferenceKeys.STREAM_CODE.getKey(), activeBatch.getStreamCode());
        MyApplication.saveData(PreferenceKeys.SUBSCRIBED.getKey(), activeBatch.getSubscribed() != null && activeBatch.getSubscribed());

        PreferenceMethods.setSelectedOnBoardingModelById(activeBatch.getExamCode());
    }

    // ---- Batch-code change detection (fire-once per API-call pair) ----
    private static final java.util.concurrent.atomic.AtomicBoolean batchCodeRefreshTriggered =
            new java.util.concurrent.atomic.AtomicBoolean(false);

    /** Call at the START of each apiGetUserByPhone / getAPIDataByPhoneData to arm the flag. */
    public static void resetBatchCodeRefreshFlag() {
        batchCodeRefreshTriggered.set(false);
    }

    /**
     * Returns true (and arms the flag) for the first caller that detects a batchCode change.
     * All subsequent callers in the same API-call cycle return false.
     */
    public static boolean tryAcquireBatchCodeRefresh() {
        return batchCodeRefreshTriggered.compareAndSet(false, true);
    }

    /** Extracts the active batch's batchCode from an API response (mirrors saveUserProfileLocally logic). */
    public static String extractActiveBatchCode(UserDetailsResponse response) {
        if (response == null || response.getBatches() == null || response.getBatches().isEmpty()) return null;
        for (UserDetailsResponse.BatchDetail b : response.getBatches()) {
            if (Boolean.TRUE.equals(b.getActive())) return b.getBatchCode();
        }
        return response.getBatches().get(0).getBatchCode();
    }

    /** Extracts the active batch's language code from an API response. */
    public static String extractActiveBatchLanguage(UserDetailsResponse response) {
        if (response == null || response.getBatches() == null || response.getBatches().isEmpty()) return null;
        for (UserDetailsResponse.BatchDetail b : response.getBatches()) {
            if (Boolean.TRUE.equals(b.getActive())) return ArivihanLanguageEnum.fromStringToCode(b.getLanguage());
        }
        return ArivihanLanguageEnum.fromStringToCode(response.getBatches().get(0).getLanguage());
    }

    /**
     * Returns whether batch-change auto-refresh is enabled via Remote Config.
     * Defaults to true; set "batch_change_auto_refresh_enabled" to false in Firebase Remote Config
     * to disable the feature in case of issues.
     */
    public static boolean isBatchChangeAutoRefreshEnabled() {
        try {
            return getFirebaseRemoteConfig().getBoolean("batch_change_auto_refresh_enabled");
        } catch (Exception e) {
            return true;
        }
    }
    // --------------------------------------------------------------------

    public static void saveUserCampaignSubscription(UserDto user) {
        MyApplication.saveData(PreferenceKeys.CAMPAIGN_SUBSCRIBED.getKey(), user.getCampaignSubscribed());
    }

    public static void saveUserCampaignSubscription(UserDetailsResponse user) {
        if (user != null && user.getUserDto() != null) {
            MyApplication.saveData(PreferenceKeys.CAMPAIGN_SUBSCRIBED.getKey(), user.getUserDto().getCampaignSubscribed());
        }
    }

    public static void saveData(String key, Object object) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof String) {
            editor.putString(key, String.valueOf(object));
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        }
        editor.apply();
    }

    public static void saveDataForDoNotDeleteSharedPreference(String key, Object object) {
        SharedPreferences.Editor editor = sharedPreferencesDonotDelete.edit();
        if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof String) {
            editor.putString(key, String.valueOf(object));
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        }
        editor.apply();
    }

    public static String getUtmCampaign() {
        try {
            String json = sharedPreferencesDonotDelete.getString(
                    PreferenceKeys.ALL_UTM_PARAMS.getKey(), null);
            Log.d(TAG, "getUtmCampaign-json: " + json);
            if (json == null) return null;
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, String>>() {
            }.getType();
            Map<String, String> utmParams = gson.fromJson(json, type);
            Log.d(TAG, "getUtmCampaign: " + utmParams.get("utm_campaign"));

            return utmParams.get("utm_campaign");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object getDataFromDoNotDeleteSharedPreference(String key, Class<?> type) {
        if (sharedPreferencesDonotDelete == null) return null;

        if (type == Boolean.class) {
            return sharedPreferencesDonotDelete.getBoolean(key, false);
        } else if (type == String.class) {
            return sharedPreferencesDonotDelete.getString(key, "");
        } else if (type == Integer.class) {
            return sharedPreferencesDonotDelete.getInt(key, 0);
        } else if (type == Long.class) {
            return sharedPreferencesDonotDelete.getLong(key, 0L);
        }

        return null; // Unknown type
    }

    public static void saveStringSet(String key, Set<String> data) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(key, data);
        editor.apply();
    }

    public static List<NotificationModel> getNotificationList() {
        Gson gson = new Gson();
        String s = sharedPreferences.getString("notificationKey", null);

        if (StringUtils.isNotEmpty(s)) {
            try {
                TypeToken<List<NotificationModel>> token = new TypeToken<>() {
                };
                return gson.fromJson(s, token.getType());
            } catch (Exception e) {
                Log.e(TAG, "getNotificationList: failed to parse JSON", e);
            }
        }
        return new ArrayList<>();
    }


    public static void saveTestSeriesAnalytics(String key, TestSeriesAnalytics testSeriesAnalytics) {
        Gson gson = new Gson();
        try {
            String test = gson.toJson(testSeriesAnalytics);
            saveData(key, test);
        } catch (Exception e) {
            saveData(key, null);
        }
    }

    public static List<CarouselResponse> getCarouselDataFromLocal(String key) {
        Gson gson = new Gson();
        String savedData = sharedPreferences.getString(key, null);

        if (StringUtils.isNotEmpty(savedData)) {
            try {
                TypeToken<List<CarouselResponse>> token = new TypeToken<>() {
                };
                return gson.fromJson(savedData, token.getType());
            } catch (Exception e) {
                Log.e(TAG, "getCarouselDataFromLocal: failed to parse JSON", e);
            }
        }
        return null;
    }

    public static void saveCarouselData(String key, List<CarouselResponse> carouselData) {
        Gson gson = new Gson();
        try {
            String json = gson.toJson(carouselData);
            saveData(key, json);
        } catch (Exception e) {
            saveData(key, null);
        }
    }

    public static void saveUserLocation(String key, double latitude, double longitude) {
        Gson gson = new Gson();
        try {
            Map<String, Double> locationMap = new HashMap<>();
            locationMap.put("latitude", latitude);
            locationMap.put("longitude", longitude);

            String json = gson.toJson(locationMap);
            saveData(key, json);
        } catch (Exception e) {
            saveData(key, null);
        }
    }

    public static Map<String, Double> getUserLocation() {
        try {
            String json = getStringData(PreferenceKeys.USER_LOCATION.getKey());
            if (json == null || json.isEmpty()) return null;
            Gson gson = new Gson();
            return gson.fromJson(
                    json,
                    new TypeToken<Map<String, Double>>() {
                    }.getType()
            );
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isLocationAlreadySaved() {
        Map<String, Double> location = MyApplication.getUserLocation();
        return location != null
                && location.get("latitude") != null
                && location.get("longitude") != null;
    }


    public static TestSeriesCardResponse getTestSeriesDataFromLocal(String key) {
        Gson gson = new Gson();
        String savedData = sharedPreferences.getString(key, null);

        if (savedData != null && !savedData.isEmpty()) {
            try {
                TypeToken<TestSeriesCardResponse> token = new TypeToken<>() {
                };
                return gson.fromJson(savedData, token.getType());
            } catch (Exception e) {
                Log.e(TAG, "getTestSeriesDataFromLocal: failed to parse JSON", e);
            }
        }
        return null;
    }

    public static void saveTestSeriesData(String key, TestSeriesCardResponse testSeriesData) {
        Gson gson = new Gson();
        try {
            String test = gson.toJson(testSeriesData);
            saveData(key, test);
        } catch (Exception e) {
            saveData(key, null);
        }
    }


    public static TestSeriesAnalytics getTestSeriesAnalytics(String key) {
        Gson gson = new Gson();
        String s = sharedPreferences.getString(key, null);

        if (StringUtils.isNotEmpty(s)) {
            try {
                TypeToken<TestSeriesAnalytics> token = new TypeToken<>() {
                };
                return gson.fromJson(s, token.getType());
            } catch (Exception e) {
                Log.e(TAG, "getTestSeriesAnalytics: failed to parse JSON", e);
            }
        }
        return null;
    }

    public static void saveTestSeriesQuestions(String key, List<TestSeriesQuestionDto> testSeriesQuestionDtos) {
        Gson gson = new Gson();
        try {
            String test = gson.toJson(testSeriesQuestionDtos);
            saveData(key, test);
        } catch (Exception e) {
            saveData(key, null);
        }
    }

    public static List<TestSeriesQuestionDto> getTestSeriesQuestionList(String key) {
        Gson gson = new Gson();
        String s = sharedPreferences.getString(key, null);

        if (StringUtils.isNotEmpty(s)) {
            try {
                TypeToken<List<TestSeriesQuestionDto>> token = new TypeToken<>() {
                };
                return gson.fromJson(s, token.getType());
            } catch (Exception e) {
                Log.e(TAG, "getTestSeriesQuestionList: failed to parse JSON", e);
            }
        }
        return new ArrayList<>();
    }

    public static void saveOfflineNoteConfig(String key, OfflineNoteConfigModel model) {
        Gson gson = new Gson();
        try {
            String json = gson.toJson(model);
            saveData(key, json);
        } catch (Exception e) {
            saveData(key, null);
        }
    }

    public static OfflineNoteConfigModel getOfflineNoteConfig(String key) {
        Gson gson = new Gson();
        String s = sharedPreferences.getString(key, null);

        if (s != null && !s.isEmpty()) {
            try {
                return gson.fromJson(s, OfflineNoteConfigModel.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String getStringData(String key) {
        try {
            return sharedPreferences.getString(key, null);
        } catch (ClassCastException e) {
            // Key was stored as a non-String type (e.g. Integer); coerce to String
            try {
                Object val = sharedPreferences.getAll().get(key);
                return val != null ? String.valueOf(val) : null;
            } catch (Exception ex) {
                return null;
            }
        }
    }

    public static String getDoNotDeleteSharedPrefKey(String key) {
        try {
            return sharedPreferencesDonotDelete.getString(key, null);
        } catch (ClassCastException e) {
            try {
                Object val = sharedPreferencesDonotDelete.getAll().get(key);
                return val != null ? String.valueOf(val) : null;
            } catch (Exception ex) {
                return null;
            }
        }
    }

    public static Integer getIntData(String key) {
        try {
            return sharedPreferences.getInt(key, -1);
        } catch (ClassCastException e) {
            try {
                Object val = sharedPreferences.getAll().get(key);
                if (val instanceof Number) return ((Number) val).intValue();
                if (val instanceof String) return Integer.parseInt((String) val);
            } catch (Exception ex) {
                // ignore
            }
            return -1;
        }
    }

    public static boolean getBoolData(String key) {
        try {
            return sharedPreferences.getBoolean(key, false);
        } catch (Exception e) {
        }
        return false;
    }

    public static long getLongData(String key) {
        return sharedPreferences.getLong(key, -1);
    }

    public static Set<String> getStringSet(String key) {
        return sharedPreferences.getStringSet(key, new HashSet<>());
    }

    public static String getStringDataFromDoNotDeleteSharedPreference(String key) {
        try {
            return sharedPreferencesDonotDelete.getString(key, null);
        } catch (ClassCastException e) {
            try {
                Object val = sharedPreferencesDonotDelete.getAll().get(key);
                return val != null ? String.valueOf(val) : null;
            } catch (Exception ex) {
                return null;
            }
        }
    }

    public static Integer getIntDataFromDoNotDeleteSharedPreference(String key) {
        return sharedPreferencesDonotDelete.getInt(key, -1);
    }

    public static boolean getBooleanDataFromDoNotDeleteSharedPreference(String key) {
        try {
            return sharedPreferencesDonotDelete.getBoolean(key, false);
        } catch (Exception e) {
        }
        return false;
    }

    public static long getLongDataFromDoNotDeleteSharedPreference(String key) {
        return sharedPreferencesDonotDelete.getLong(key, -1);
    }


    public static void saveCompletedMicroLectureList(List<String> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        saveData(PreferenceKeys.COMPLETED_LECTURES.getKey(), json); // Assuming saveData is your existing method to store in SharedPreferences
    }

    public static List<String> getCompletedMicroLectureList() {
        String savedData = sharedPreferences.getString(PreferenceKeys.COMPLETED_LECTURES.getKey(), null);
        if (!TextUtils.isEmpty(savedData)) {
            try {
                Type type = new TypeToken<List<String>>() {
                }.getType();
                return new Gson().fromJson(savedData, type);
            } catch (Exception e) {
                Log.e(TAG, "getCompletedMicroLectureList: failed to parse JSON", e);
            }
        }
        return new ArrayList<>();
    }

    public static void appendCompletedMicroLectureList(String value) {
        List<String> currentList = getCompletedMicroLectureList();
        if (!currentList.contains(value) && StringUtils.isNotEmpty(value)) {
            currentList.add(value);
            saveCompletedMicroLectureList(currentList);
        }
    }

    public static void savePDFCircleDailyCounter(String key, PDFCircleDailyCounter counter) {
        Gson gson = new Gson();
        try {
            String s = gson.toJson(counter);
            saveData(key, s);
        } catch (Exception e) {
            saveData(key, null);
        }
    }

    public static PDFCircleDailyCounter getPDFCircleDailyCounter(String key) {
        Gson gson = new Gson();
        String s = sharedPreferences.getString(key, null);
        if (StringUtils.isNotEmpty(s)) {
            try {
                TypeToken<PDFCircleDailyCounter> token = new TypeToken<>() {
                };
                return gson.fromJson(s, token.getType());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        return PDFCircleDailyCounter.builder()
                .lastDateSaved(today)
                .viewCount(0)
                .build();
    }

    public static int getLocationDenialCount() {
        return sharedPreferences.getInt(PreferenceKeys.USER_LOCATION_DENIAL_COUNT.getKey(), 0);
    }

    public static void incrementLocationDenialCount() {
        saveData(PreferenceKeys.USER_LOCATION_DENIAL_COUNT.getKey(), getLocationDenialCount() + 1);
    }

    public static void resetLocationDenialCount() {
        removeString(PreferenceKeys.USER_LOCATION_DENIAL_COUNT.getKey());
    }

    public static boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) me.getSystemService(Context.
                CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network nw = connectivityManager.getActiveNetwork();
            if (nw == null) return false;
            NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);
            return actNw != null && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH));
        } else {
            NetworkInfo nwInfo = connectivityManager.getActiveNetworkInfo();
            return nwInfo != null && nwInfo.isConnected();
        }
    }


    public static void saveSharedPreferencesDoNotDelete(String key, Object object) {
        SharedPreferences.Editor editor = sharedPreferencesDonotDelete.edit();
        if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof String) {
            editor.putString(key, String.valueOf(object));
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        }
        editor.apply();
    }

    public static MyApplication getInstance() {
        return me;
    }

    public static String getEndPoint() {
        Log.d(TAG, "getEndPoint: " + EnvironmentManager.getEndPoint());
        return EnvironmentManager.getEndPoint();
    }

    public static void setEndPoint(String url) {
        Log.d(TAG, "setEndPoint: " + url);
        EnvironmentManager.setEndPoint(url);
    }


    public static String getFCMToken() {
        String authToken = getStringData(PreferenceKeys.AUTH_TOKEN.getKey());

        Log.d("AuthTokenOTP", "token = " + authToken);
        if (StringUtils.isNotEmpty(authToken)) {
            return authToken;
        }

        long FCMTime = getLongData(PreferenceKeys.FCM_TOKEN_STORE_TIME.getKey());

        Log.d("AuthTokenOTP", "token = fcmTime");

//        if (System.currentTimeMillis() > FCMTime) {
//            saveData(PreferenceKeys.FCM_TOKEN.getKey(), "");
//        }

        Log.d("AuthTokenOTP", "token = getStringData");

        String fcMtoken = getDoNotDeleteSharedPrefKey(PreferenceKeys.FCM_TOKEN.getKey());
        if (StringUtils.isNotEmpty(fcMtoken)) {
            Log.d("AuthTokenOTP", "token = fcMtoken");

            return fcMtoken;
        }
        try {
            FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
            if (mUser == null) {
                return fcMtoken;
            }
            Task<GetTokenResult> task = mUser.getIdToken(true);
            Tasks.await(task);
            fcMtoken = task.getResult().getToken();

        } catch (Exception e) {

            return fcMtoken;
        }
        return fcMtoken;
    }

    public static void getFCMToken(TokenCallback callback) {
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser == null) {
            Log.d("AuthTokenOTP", "User not signed in");
            callback.onTokenError(new Exception("User not signed in"));
            return;
        }

//        mUser.getIdToken(true)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        String token = task.getResult().getToken();
//                        saveData(PreferenceKeys.FCM_TOKEN.getKey(), token);
//                        saveData(PreferenceKeys.FCM_TOKEN_STORE_TIME.getKey(), System.currentTimeMillis() + 2700000);
//                        callback.onTokenReceived(token);
//                    } else {
//                        callback.onTokenError(task.getException());
//                    }
//                });
    }

    public static FirebaseRemoteConfig getFirebaseRemoteConfig() {
        return FirebaseRemoteConfig.getInstance();
    }

    public static FirebaseCrashlytics getFirebaseInstance() {

        if (instance == null) {
            instance = FirebaseCrashlytics.getInstance();
        }

        return instance;
    }


    public static void updateUserIdForCrashlytics() {
        try {
            getFirebaseInstance().setUserId(getStringData(PreferenceKeys.USER_ID.getKey()));
        } catch (Exception e) {
            Log.e("Crashlytics", "Error" + e.getMessage());

        }
    }

    public static void updateCrashLyticsKeys(CustomKeysAndValues keysAndValues) {
        try {
            getFirebaseInstance().setCustomKeys(keysAndValues);
        } catch (Exception e) {
            Log.e("Crashlytics", "Error" + e.getMessage());
        }
    }

    public static void setLocale() {
        String lang = getStringData(PreferenceKeys.MY_LANG.getKey());
        if (lang != null) {
            Locale locale = new Locale(lang);
            Locale.setDefault(locale);
            Configuration configuration = new Configuration();
            configuration.locale = locale;
            getInstance().getResources().updateConfiguration(configuration, getInstance().getResources().getDisplayMetrics());
        }
    }

    public static Locale getLocale() {
        String lang = getStringData(PreferenceKeys.MY_LANG.getKey());
        if (StringUtils.isEmpty(lang)) {
            lang = ArivihanLanguageEnum.ENGLISH.getCode();
        }
        Locale locale = null;
        locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration configuration = getInstance().getBaseContext().getResources().getConfiguration();
        configuration.locale = locale;
        getInstance().getResources().updateConfiguration(configuration, getInstance().getResources().getDisplayMetrics());

        return locale;
    }


    public static void logFirebaseBundleCreation(String eventName) {
        if (BuildConfig.REELS_DEMO) return;
        Bundle bundle = new Bundle();
        logFirebaseEvent(eventName, bundle);
    }

    public static void logFirebaseBundleCreation(String eventName, Map<String, String> params) {
        if (BuildConfig.REELS_DEMO) return;
        Bundle bundle = new Bundle();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            bundle.putString(entry.getKey(), entry.getValue());
        }
        logFirebaseEvent(eventName, bundle);
    }

    /**
     * Single background thread that all analytics dispatch runs on.
     *
     * Single-threaded on purpose: events keep the order they were logged in. Min priority so it
     * can never compete with the UI thread for CPU.
     */
    private static final ExecutorService ANALYTICS_EXECUTOR =
            Executors.newSingleThreadExecutor(runnable -> {
                Thread thread = new Thread(runnable, "analytics-dispatch");
                thread.setPriority(Thread.MIN_PRIORITY);
                return thread;
            });

    /**
     * Logs an event to Firebase and Facebook off the main thread.
     *
     * Everything this does - reading remote config, pulling ~15 values out of SharedPreferences to
     * decorate the bundle, and the three SDK calls themselves - used to run on whichever thread
     * logged the event, which is nearly always the main thread. The GMS Measurement SDK in
     * particular can make a blocking Binder call into Play Services from there, which is what
     * froze the UI in ANR 293b2117.
     *
     * The bundle is copied here, on the calling thread, because callers are free to reuse or
     * mutate theirs the moment this returns.
     */
    public static void logFirebaseEvent(String eventName, Bundle eventBundle) {
        if (TextUtils.isEmpty(eventName)) return;
        final Bundle snapshot = eventBundle != null ? new Bundle(eventBundle) : new Bundle();
        try {
            ANALYTICS_EXECUTOR.execute(() -> dispatchAnalyticsEvent(eventName, snapshot));
        } catch (Exception ignored) {
            // RejectedExecutionException during shutdown - dropping an analytics event is fine.
        }
    }

    private static void dispatchAnalyticsEvent(String eventName, Bundle eventBundle) {
        try {

            // -------- Remote config bypass check --------
            FirebaseRemoteConfig remoteConfig = getFirebaseRemoteConfig();
            String allowToByPassForUsers =
                    remoteConfig != null
                            ? remoteConfig.getString(RemoteConfigKey.INTERNAL_DEV_USERS_TO_BY_PASS_FEATURE.getKey())
                            : "";

            String phoneNumber = MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey());

            String env = EnvironmentManager.getEnvironmentNameByUrl(
                    EnvironmentManager.getEndPoint()
            );

            if (!TextUtils.isEmpty(phoneNumber)
                    && !TextUtils.isEmpty(allowToByPassForUsers)
                    && allowToByPassForUsers.contains(phoneNumber)) {
                return;
            }

            if (!TextUtils.isEmpty(env) && !env.equalsIgnoreCase("prod")) {
                return;
            }

            // -------- Bundle safety --------
            if (eventBundle == null) {
                eventBundle = new Bundle();
            }

            Log.d("logFirebaseEvent",
                    "logFirebaseBundleCreation: " + eventName + ", eventBundle: " + eventBundle);

            // -------- User details --------
            String userId = sharedPreferences != null
                    ? sharedPreferences.getString(PreferenceKeys.USER_ID.getKey(), null)
                    : null;

            if (!TextUtils.isEmpty(userId)) {
                eventBundle.putString("userId", userId);
            }

            String userName = MyApplication.getStringData(PreferenceKeys.NAME.getKey());
            if (!TextUtils.isEmpty(userName)) {
                eventBundle.putString("userName", userName);
            }

//            boolean campaignSubscribed = MyApplication.getBoolData(PreferenceKeys.CAMPAIGN_SUBSCRIBED.getKey());
//            eventBundle.putString("campaignSubscribed", campaignSubscribed ? "Yes" : "NO");

            // -------- Phone number fallback --------
            if (!TextUtils.isEmpty(phoneNumber)) {
                eventBundle.putString("phoneNumber", phoneNumber);
            } else {
                String onboardingNumber = MyApplication.getStringData(PreferenceKeys.ON_BOARDING_NUMBER.getKey());
                eventBundle.putString("phoneNumber",
                        onboardingNumber != null ? onboardingNumber : "");
            }

            // -------- Language --------
//            String language = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
//            eventBundle.putString("language", language != null ? language : "");
//            eventBundle.putString("environment", env != null ? env.toLowerCase() : "");

            // -------- Batch --------

            String batchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
//            String examCode = MyApplication.getStringData(PreferenceKeys.EXAM_CODE.getKey());
//            String gradeCode = MyApplication.getStringData(PreferenceKeys.GRADE_CODE.getKey());
//            String streamCode = MyApplication.getStringData(PreferenceKeys.STREAM_CODE.getKey());
            String subscriptionType = MyApplication.getStringData(PreferenceKeys.USER_SUBSCRIPTION_TYPE.getKey());

            String courseId = MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey());
            String classId = MyApplication.getStringData(PreferenceKeys.CLASS_ID.getKey());
            String selectedExam = PreferenceMethods.getSelectedOnBoardingId();

            eventBundle.putString("batchCode", batchCode != null ? batchCode : "");
//            eventBundle.putString("examCode", examCode != null ? examCode : "");
//            eventBundle.putString("gradeCode", gradeCode != null ? gradeCode : "");
//            eventBundle.putString("streamCode", streamCode != null ? streamCode : "");
            eventBundle.putString("subscriptionType", subscriptionType != null ? subscriptionType : "");

//            eventBundle.putString("courseId", courseId != null ? courseId : "");
//            eventBundle.putString("classId", classId != null ? classId : "");
            eventBundle.putString("selectedExam", selectedExam != null ? selectedExam : "");

            // -------- Active day & streak --------
            long userActiveDay = getLongData(PreferenceKeys.USER_ACTIVE_DAYS.getKey());
            if (userActiveDay >= 0) {
                eventBundle.putString("user_active_day", String.valueOf(userActiveDay));
            }
            Integer userStreakDay = getIntData(PreferenceKeys.USER_STREAK_COUNT.getKey());
            if (userStreakDay != null && userStreakDay > 0) {
                eventBundle.putString("user_streak_day", String.valueOf(userStreakDay));
            }
            String streakStartDay = getStringData(PreferenceKeys.USER_STREAK_START_DATE.getKey());
            if (!TextUtils.isEmpty(streakStartDay)) {
                eventBundle.putString("user_streak_start_day", streakStartDay);
            }

            // -------- Firebase event --------
            if (firebaseAnalytics != null) {
                firebaseAnalytics.logEvent(eventName, eventBundle);
                Log.d(TAG, "logFirebaseEvent: " + eventName + "--eventBundle--" + eventBundle);
            }

            // -------- Ads specific events --------
            String adsSpecificEvents =
                    remoteConfig != null
                            ? remoteConfig.getString("google_ads_specific_events")
                            : "";

            if (!TextUtils.isEmpty(adsSpecificEvents)) {
                List<String> specificEvents =
                        Arrays.asList(adsSpecificEvents.split("\\s*,\\s*"));

                if (SubscriptionType.hasActiveSubscription() && specificEvents.contains(eventName)) {
                    eventBundle.putString("eventsName", eventName);
                    if (firebaseAnalytics != null) {
                        firebaseAnalytics.logEvent("adsSpecificEvents", eventBundle);
                    }
                    Log.d("adsSpecificEvents", "logFirebaseEvent: " + eventBundle);
                }
            }

            Log.d(TAG, "eventName=" + eventName + ", eventBundle=" + eventBundle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        try {
            facebookEventLogger.logEvent(eventName, withHashedPhoneNumbers(eventBundle));
        } catch (Exception ignored) {
        }

    }

    /**
     * Meta's platform terms prohibit sending raw PII in event parameters, so the
     * Facebook copy of the bundle carries only a SHA-256 digest of any phone-like
     * field. Firebase keeps receiving the original bundle.
     */
    private static Bundle withHashedPhoneNumbers(Bundle source) {
        Bundle sanitized = new Bundle(source);
        for (String key : source.keySet()) {
            if (key != null && key.toLowerCase(Locale.US).contains("phone")) {
                Object value = source.get(key);
                String raw = value != null ? String.valueOf(value) : "";
                sanitized.putString(key, sha256Phone(raw));
            }
        }
        return sanitized;
    }

    /**
     * Digits-only normalisation before hashing (Meta's matching prep) so the same
     * number hashes identically regardless of "+91 "/dash formatting at call sites.
     * Returns "" on any failure — the raw number must never leave the app.
     */
    private static String sha256Phone(String phoneNumber) {
        try {
            String normalized = phoneNumber != null ? phoneNumber.replaceAll("\\D", "") : "";
            if (normalized.isEmpty()) return "";
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(normalized.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            return "";
        }
    }

//    public static void logFirebaseEvent(String eventName, Bundle eventBundle) {
//
//        String allowToByPassForUsers = FirebaseRemoteConfig.getInstance().
//                getString(RemoteConfigKey.INTERNAL_DEV_USERS_TO_BY_PASS_FEATURE.getKey());
//        String phoneNumber = getStringData(PreferenceKeys.PHONE_NUMBER.getKey());
//
//        if (allowToByPassForUsers.contains(phoneNumber)) {
//            return;
//        }
//
//        Log.d("logFirebaseEvent", "logFirebaseBundleCreation: " + eventName + ", eventBundle: " + (eventBundle != null ? eventBundle.toString() : "null"));
//
//        if (eventBundle == null) {
//            eventBundle = new Bundle();
//        }
//
//        String userId = sharedPreferences.getString(PreferenceKeys.USER_ID.getKey(), null);
//        if (userId != null) {
//            eventBundle.putString(PreferenceKeys.USER_ID.getKey(), userId);
//        }
//
//        String userName = MyApplication.getStringData(PreferenceKeys.NAME.getKey());
//        if (userName != null) {
//            Log.d("logFirebaseEvent", "User name: " + userName);
//            eventBundle.putString("UserName", userName);
//        }
//        boolean campaignSubscribed = MyApplication.getBoolData(PreferenceKeys.CAMPAIGN_SUBSCRIBED.getKey());
//        Log.d("logFirebaseEvent", "campaignSubscribed:" + campaignSubscribed);
//        eventBundle.putString("campaign_subscribed", campaignSubscribed ? "Yes" : "NO");
//        if (phoneNumber != null) {
//            eventBundle.putString("phoneNumber", phoneNumber);
//        } else {
//            phoneNumber = getStringData(PreferenceKeys.ON_BOARDING_NUMBER.getKey());
//            eventBundle.putString("phoneNumber", phoneNumber != null ? phoneNumber : "");
//        }
//
//        String language = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
//        eventBundle.putString("Language", language != null ? language : "");
//        boolean board = MyApplication.getBoolData(PreferenceKeys.BOARD.getKey());
//        String env = EnvironmentManager.getEnvironmentNameByUrl(EnvironmentManager.getEndPoint());
//        String courseId = board ? "2" : MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey());
//
//        String className = MyApplication.getStringData(PreferenceKeys.CLASS_NAME.getKey());
//
//        if (StringUtils.isNotEmpty(env)) {
//            eventBundle.putString("Environment", env.toLowerCase());
//        }
//
//        String selectedCourseId = MyApplication.getDoNotDeleteSharedPrefKey(PreferenceKeys.SELECTED_COURSE_ID.getKey());
//
//        eventBundle.putString("Course_Name", board ? "board" : MyApplication.getStringData(PreferenceKeys.COURSE_NAME.getKey()));
//        eventBundle.putString("Course_ID", courseId);
//        eventBundle.putString("Class", className);
//        eventBundle.putString(PreferenceKeys.SECONDARY_COURSE_ID.getKey(), MyApplication.getStringData(PreferenceKeys.SECONDARY_COURSE_ID.getKey()));
//        eventBundle.putString(PreferenceKeys.BOARD_CODE.getKey(), MyApplication.getStringData(PreferenceKeys.BOARD_CODE.getKey()));
//
//        if (StringUtils.isNotEmpty(selectedCourseId)) {
//            eventBundle.putString(PreferenceKeys.SELECTED_COURSE_ID_LOCAL.getKey(), selectedCourseId);
//        }
//
//        firebaseAnalytics.logEvent(eventName, eventBundle);
//
//        String adsSpecificEvents = FirebaseRemoteConfig.getInstance().getString("google_ads_specific_events");
//        List<String> specificEvents = Arrays.asList(adsSpecificEvents.split("\\s*,\\s*"));
//        if (campaignSubscribed && specificEvents.contains(eventName)) {
//            eventBundle.putString("eventsName", eventName);
//            firebaseAnalytics.logEvent("adsSpecificEvents", eventBundle);
//            Log.d("adsSpecificEvents", "logFirebaseEvent: " + eventBundle);
//        }
//
//        Log.d(TAG, "eventBundle: " + eventBundle + ", eventName: " + eventName);
//    }

    public void setUpExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(this::uncaughtException);
    }

    public void uncaughtException(Thread thread, Throwable throwable) {
        MyApplication.instance.log(Log.getStackTraceString(throwable));
        MyApplication.instance.recordException(throwable);
        Log.e(TAG, Log.getStackTraceString(throwable));
        Intent intent = new Intent(this, ExceptionHandlingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("body", Log.getStackTraceString(throwable));
        startActivity(intent);
        System.exit(0);
    }

    public static void saveSubjectList(String key, ArrayList<String> subjects) {
        Gson gson = new Gson();
        try {
            String test = gson.toJson(subjects);
            saveData(key, test);
        } catch (Exception e) {
            saveData(key, null);
        }
    }

    public static void saveTodayClassesData(String key, TodayClassResponse model) {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(model);
            MyApplication.saveData(key, json);

            Log.d("TodayClassCache", "Data saved successfully for key: " + key);
        } catch (Exception e) {
            Log.e("TodayClassCache", "Error saving today class data", e);
            MyApplication.saveData(key, null);
        }
    }

    public static TodayClassResponse getTodayClassesData(String key) {
        try {
            String json = MyApplication.getStringData(key);

            if (StringUtils.isEmpty(json)) {
                Log.d("TodayClassCache", "No cached data found for key: " + key);
                return null;
            }

            Gson gson = new Gson();
            TypeToken<TodayClassResponse> token = new TypeToken<TodayClassResponse>() {
            };
            return gson.fromJson(json, token.getType());

        } catch (Exception e) {
            Log.e("TodayClassCache", "Error reading today class cache", e);
            return null;
        }
    }


    public static ArrayList<String> getSubjectList(String key) {
        Gson gson = new Gson();
        String s = sharedPreferences.getString(key, null);

        if (StringUtils.isNotEmpty(s)) {
            try {
                TypeToken<List<String>> token = new TypeToken<List<String>>() {
                };
                return gson.fromJson(s, token.getType());
            } catch (Exception e) {
                Log.e(TAG, "getSubjectList: failed to parse JSON", e);
            }
        }
        return new ArrayList<>();
    }


    public static List<SubjectResponse> getSubjectModelList(String key) {
        String s = sharedPreferences.getString(key, null);
        Gson gson = new Gson();

        if (StringUtils.isNotEmpty(s)) {
            try {
                TypeToken<List<SubjectResponse>> token = new TypeToken<List<SubjectResponse>>() {
                };
                return gson.fromJson(s, token.getType());
            } catch (Exception e) {
                Log.e(TAG, "getSubjectModelList: failed to parse JSON", e);
            }
        }
        return new ArrayList<>();
    }

    public static void saveSubjectModelList(String key, List<SubjectResponse> subjectModelList) {
        Gson gson = new Gson();
        try {
            String test = gson.toJson(subjectModelList);
            saveData(key, test);
        } catch (Exception e) {
            saveData(key, null);
        }

    }

    public static void saveMicroLectureList(String key, ArrayList<MicroLectureDto> subjects) {
        Gson gson = new Gson();
        try {
            String test = gson.toJson(subjects);
            saveData(key, test);
        } catch (Exception e) {
            saveData(key, null);
        }
    }

    public static ArrayList<MicroLectureDto> getMicroLectureList(String key) {
        Gson gson = new Gson();
        String s = sharedPreferences.getString(key, null);
        if (StringUtils.isNotEmpty(s)) {
            try {
                TypeToken<ArrayList<MicroLectureDto>> token = new TypeToken<>() {
                };
                return gson.fromJson(s, token.getType());
            } catch (Exception e) {
                Log.e(TAG, "getMicroLectureList: failed to parse JSON", e);
            }
        }
        return new ArrayList<>();
    }

    public static List<HighlistModel> getHighlistData(String key) {
        Gson gson = new Gson();
        String s = sharedPreferences.getString(key, null);

        if (StringUtils.isNotEmpty(s)) {
            try {
                TypeToken<List<HighlistModel>> token = new TypeToken<List<HighlistModel>>() {
                };
                return gson.fromJson(s, token.getType());
            } catch (Exception e) {
                Log.e(TAG, "getHighlistData: failed to parse JSON", e);
            }
        }
        return new ArrayList<>();
    }

    public static void saveHighlightsData(String key, List<HighlistModel> highlistModelList) {
        Gson gson = new Gson();
        try {
            checkSavedData(HomeScreenCacheData.AV_HIGHLIGHT_CACHE_KEY.name());
            String test = gson.toJson(highlistModelList);
            saveData(key, test);
        } catch (Exception e) {
            saveData(key, null);
        }
    }


    public static void checkSavedData(String key) {
        String savedData = sharedPreferences.getString(key, null);
        if (savedData != null) {
            Log.i("SharedPreferences", "Data found for key: " + key);
            Log.i("SharedPreferences", "Saved Data: " + savedData);
        } else {
            Log.i("SharedPreferences", "No data found for key: " + key);
        }
    }

    public static void setCaseStudyGroupList(String key, List<String> group) {
        Gson gson = new Gson();
        try {
            String list = gson.toJson(group);
            saveData(key, list);
        } catch (Exception e) {
            saveData(key, null);
        }
    }

    public static HashMap<String, Object> getMap(String key) {
        String s = sharedPreferences.getString(key, null);
        Gson gson = new Gson();
        if (StringUtils.isNotEmpty(s)) {
            try {
                TypeToken<HashMap<String, Object>> token = new TypeToken<>() {
                };
                return gson.fromJson(s, token.getType());
            } catch (Exception e) {
                Log.e(TAG, "getMap: failed to parse JSON", e);
            }
        }
        return null;
    }

    public static void setMap(String key, HashMap<String, Object> map) {
        Gson gson = new Gson();
        try {
            String list = gson.toJson(map);
            saveData(key, list);
        } catch (Exception e) {
            saveData(key, null);
        }
    }

    public static List<String> getCaseStudyGroupList(String key) {
        Gson gson = new Gson();
        String s = sharedPreferences.getString(key, null);
        if (StringUtils.isNotEmpty(s)) {
            try {
                TypeToken<List<String>> token = new TypeToken<>() {
                };
                return gson.fromJson(s, token.getType());
            } catch (Exception e) {
                Log.e(TAG, "getCaseStudyGroupList: failed to parse JSON", e);
            }
        }
        return new ArrayList<>();
    }

    public static void openGooglePlayStore(Context context) {
        String appPackageName = "arivihan.technologies.doubtbuzzter2";
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
            context.startActivity(intent);
        } catch (Exception e) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
            context.startActivity(intent);
        }
    }

    public static void updateVideoSelection(String chapterId, String microlectureId) {
        final Map<String, Object> map = new HashMap<>();
        map.put("chapterId", chapterId);
        map.put(PreferenceKeys.MICROLECTURE_ID.getKey(), microlectureId);

        Log.d("videoProgressRequest", "ChapterId : " + chapterId + ", MicrolectureId : " + microlectureId);

        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(UPDATE_VIDEO_SELECTION, map), null,
                response -> {
                    MyApplication.saveData(PreferenceKeys.MICROLECTURE_DATA_RELOAD.getKey(), true);
                    Log.d("updateVideoSelection", "Video selection updated successfully.");
                },
                new VolleyGlobalErrorListener(getInstance()) {
                    @Override
                    public void postError(VolleyError error) {
                        Log.e("updateVideoSelection", "Error updating video selection: " + error.getMessage());
                    }
                });
        myJsonObjectRequest.setTag("updateVideoSelection");
        MyVolleySingleton.getInstance(getInstance()).addToRequestQueue(myJsonObjectRequest);
    }

    public static void saveObject(String key, UserOngoingTestStateDto object) {
        Gson gson = new Gson();
        try {
            String s = gson.toJson(object);
            saveData(key, s);
        } catch (Exception e) {
            saveData(key, null);
        }
    }

    public static UserOngoingTestStateDto getObject(String key) {
        Gson gson = new Gson();
        String s = sharedPreferences.getString(key, null);
        if (StringUtils.isNotEmpty(s)) {
            TypeToken<UserOngoingTestStateDto> token = new TypeToken<>() {
            };
            return gson.fromJson(s, token.getType());
        }
        return UserOngoingTestStateDto.builder().build();
    }

    public static List<DoubtStatusDto> parseFirebaseResponseDoubt(Map<String, Object> map) throws JSONException {
        List<DoubtStatusDto> list = new ArrayList<>();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            try {
                String sessionId = entry.getKey();
                if (entry.getValue() instanceof Map) {
                    Map<String, Object> sessionData = (Map<String, Object>) entry.getValue();
                    String type = (String) sessionData.get("type");
                    if ("DOUBT".equals(type)) {
                        DoubtStatusDto dto = new DoubtStatusDto();
                        dto.setSessionId(sessionId);
                        dto.setDoubtText((String) sessionData.get("doubtText"));
                        dto.setLanguage((String) sessionData.get("language"));
                        Object statusRaw = sessionData.get("status");
                        dto.setStatus(statusRaw != null ? DoubtSolutionStatus.valueOf((String) statusRaw) : null);
                        Object startRaw = sessionData.get("startTime");
                        dto.setStartTime(startRaw instanceof Number ? ((Number) startRaw).longValue() : 0L);
                        Object closeRaw = sessionData.get("closeTime");
                        dto.setCloseTime(closeRaw instanceof Number ? ((Number) closeRaw).longValue() : 0L);
                        dto.setResultIn(formatTime(dto.getCloseTime() - System.currentTimeMillis() + 60000));
                        list.add(dto);
                    }
                }
            } catch (Exception e) {
                Log.d("Skipping invalid entry", e.getMessage());
            }
        }
        return list;
    }

    public static List<PendingSubscriptionEntry> parseFirebaseResponsePendingSubscriptionEntries(Map<String, Object> map) {
        List<PendingSubscriptionEntry> list = new ArrayList<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            try {
                String planId = entry.getKey();
                if (entry.getValue() instanceof Map) {
                    Map<String, Object> sessionData = (Map<String, Object>) entry.getValue();
                    PendingSubscriptionEntry dto = PendingSubscriptionEntry.builder()
                            .planId(planId)
                            .name((String) sessionData.get(PreferenceKeys.NAME.getKey()))
                            .language((String) sessionData.get("language"))
                            .status((String) sessionData.get("status")).build();
                    list.add(dto);
                }
            } catch (Exception e) {
                Log.d("Skipping invalid entry", e.getMessage());
            }
        }
        return list;
    }

    public static ArrayList<ToppersHomeScreenModel> parseToToppersHomeScreenModel(String key) {
        Gson gson = new Gson();
        String s = sharedPreferences.getString(key, null);

        if (StringUtils.isNotEmpty(s)) {
            try {
                TypeToken<ArrayList<ToppersHomeScreenModel>> token = new TypeToken<>() {
                };
                return gson.fromJson(s, token.getType());
            } catch (Exception e) {
                Log.e(TAG, "parseToToppersHomeScreenModel: failed to parse JSON", e);
            }
        }
        return new ArrayList<>();
    }

    public static UserAsatFirebaseData parseAsatResult(Map<String, Object> sessionData) throws JSONException {
        UserAsatFirebaseData UserAsatFirebaseData = new UserAsatFirebaseData();
        try {
            UserAsatFirebaseData.setTitle((String) sessionData.get("title"));
            UserAsatFirebaseData.setSubTitle((String) sessionData.get("subTitle"));
            UserAsatFirebaseData.setLanguage((String) sessionData.get("language"));
        } catch (Exception e) {
            Log.d("Skipping invalid entry", e.getMessage());
        }
        return UserAsatFirebaseData;
    }

    public static TopperNoteDownloadedStatusDto parseToppersNotesResult(Map<String, Object> sessionData) {
        TopperNoteDownloadedStatusDto dto = new TopperNoteDownloadedStatusDto();
        try {
            dto.setTitle((String) sessionData.get("title"));
            dto.setSubTitle((String) sessionData.get("subTitle"));
            dto.setClassName((String) sessionData.get("className"));
            dto.setActivityParams((String) sessionData.get("activityParams"));
            dto.setTitleImg((String) sessionData.get("titleImg"));
        } catch (Exception e) {
            Log.d("parseToppersNotesResult", "Skipping invalid entry: " + e.getMessage());
        }
        return dto;
    }

    public static NeetWeakTopicRdbModel parseNeetWeakTopicResult(Map<String, Object> sessionData) throws JSONException {
        NeetWeakTopicRdbModel neetWeakTopicRdbModel = new NeetWeakTopicRdbModel();
        try {
            neetWeakTopicRdbModel.setUserId((String) sessionData.get(PreferenceKeys.USER_ID.getKey()));
            neetWeakTopicRdbModel.setStatus((String) sessionData.get("status"));
            neetWeakTopicRdbModel.setTitle((String) sessionData.get("title"));
            neetWeakTopicRdbModel.setSubtitle((String) sessionData.get("subtitle"));
            neetWeakTopicRdbModel.setPopUpTitle((String) sessionData.get("popUpTitle"));
            neetWeakTopicRdbModel.setPopUpSubTitle((String) sessionData.get("popUpSubTitle"));
            neetWeakTopicRdbModel.setHomeScreenTitle((String) sessionData.get("homeScreenTitle"));
            neetWeakTopicRdbModel.setHomeScreenSubTitle((String) sessionData.get("homeScreenSubTitle"));
            neetWeakTopicRdbModel.setHomeScreenBtnText((String) sessionData.get("homeScreenBtnText"));

        } catch (Exception e) {
            Log.d("Skipping invalid entry", e.getMessage());
        }
        return neetWeakTopicRdbModel;
    }

    public static UserTestEvaluationStautsObject parseFirebaseResponseTest(Map<String, Object> sessionData) throws JSONException {
        UserTestEvaluationStautsObject userTestEvaluationStautsObject = new UserTestEvaluationStautsObject();
        try {
            userTestEvaluationStautsObject.setTestId((String) sessionData.get("testId"));
            userTestEvaluationStautsObject.setTestName((String) sessionData.get("testName"));
            userTestEvaluationStautsObject.setListId((String) sessionData.get("listId"));
            userTestEvaluationStautsObject.setListName((String) sessionData.get("listName"));
            Object startRaw = sessionData.get("startTime");
            userTestEvaluationStautsObject.setStartTime(startRaw instanceof Number ? ((Number) startRaw).longValue() : 0L);
            Object closeRaw = sessionData.get("closeTime");
            userTestEvaluationStautsObject.setCloseTime(closeRaw instanceof Number ? ((Number) closeRaw).longValue() : 0L);
            String statusStr = (String) sessionData.get("status");
            if (statusStr != null) {
                try {
                    userTestEvaluationStautsObject.setTestEvaluationStatusEnum(TestEvaluationStatusEnum.valueOf(statusStr));
                } catch (IllegalArgumentException ex) {
                    Log.w("parseFirebaseResponseTest", "Unknown status value: " + statusStr);
                }
            }
            userTestEvaluationStautsObject.setResultIn(formatTime(userTestEvaluationStautsObject.getCloseTime() - System.currentTimeMillis() + 60000));
        } catch (Exception e) {
            Log.d("Skipping invalid entry", e.getMessage());
        }
        return userTestEvaluationStautsObject;
    }

    public static int formatTime(long millis) {
        return (int) Math.round(((double) millis / (1000 * 60)) % 60);
    }

    public interface TokenCallback {
        void onTokenReceived(String token);

        void onTokenError(Exception e);
    }


    public static void updateToDoState(ToDoStateUser toDoStateUser) {
        try {
            JSONObject jsonBody = new JSONObject(new Gson().toJson(toDoStateUser));

            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                    UPDATE_TODO_ORDER_STATE,
                    jsonBody,
                    response -> {
                        Log.d("UPDATE_TODO_ORDER_STATE", "Successfully updated.");
                    },
                    new VolleyGlobalErrorListener(getInstance()) {
                        @Override
                        public void postError(VolleyError error) {
                            Log.e("UPDATE_TODO_ORDER_STATE", error.getMessage());
                        }
                    });

            myJsonObjectRequest.setTag("updateVideoSelection");
            MyVolleySingleton.getInstance(getInstance()).addToRequestQueue(myJsonObjectRequest);
        } catch (JSONException e) {
            Log.e("UPDATE_TODO_ORDER_STATE", "JSON Exception: " + e.getMessage());
        }
    }

    public static SpannableStringBuilder getFragmentFormattedText(Context context, String fragmentName) {
        String language = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        if (language == null) language = "en";

        // These fragments always use English
        if (MediumFragment.TAG.equals(fragmentName) ||
                "medium_fragment_bubble_text".equals(fragmentName)) {
            language = "en";
        }

        String remoteJson = getFirebaseRemoteConfig()
                .getString("onBoarding_fragment_text_batch_v3");

        if (remoteJson == null || remoteJson.isEmpty()) {
            return new SpannableStringBuilder();
        }

        try {
            JSONObject mainObject = new JSONObject(remoteJson);

            if (!mainObject.has(fragmentName)) {
                Log.w("FragmentText", "Fragment key not found: " + fragmentName);
                return new SpannableStringBuilder();
            }

            JSONObject fragmentObject = mainObject.getJSONObject(fragmentName);

            if (!fragmentObject.has(language)) {
                Log.w("FragmentText", "Language key not found for: " + fragmentName);
                language = "en"; // fallback
            }

            if (!fragmentObject.has(language)) {
                return new SpannableStringBuilder();
            }

            Object langValue = fragmentObject.get(language);

            // Get the JSON array string
            String jsonArrayString;
            if (langValue instanceof JSONArray) {
                jsonArrayString = ((JSONArray) langValue).toString();
            } else {
                Log.w("FragmentText", "Unexpected value type for: " + fragmentName);
                return new SpannableStringBuilder();
            }

            // Replace user_Name placeholder before passing to builder
            String userName = MyApplication.getStringData(PreferenceKeys.NAME.getKey());
            if (userName != null && !userName.isEmpty()) {
                jsonArrayString = jsonArrayString.replace("user_Name", userName);
            } else {
                jsonArrayString = jsonArrayString.replace("user_Name", "");
            }

            return StyledTextBuilder.fromJson(context, jsonArrayString);

        } catch (JSONException e) {
            Log.e("FragmentText", "JSON parsing error: " + e.getMessage());
            return new SpannableStringBuilder();
        }
    }

    public static Spanned fromHtmlCompat(String html) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(html);
        }
    }

    public static String toCamelCaseFirstNWords(String input, int wordLimit) {
        if (input == null) {
            return "";
        }

        input = input.trim();
        if (input.isEmpty() || wordLimit <= 0) {
            return input;
        }

        String[] words = input.split("\\s+");  // Split by one or more spaces
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (i < wordLimit && !word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1));
                }
            } else {
                result.append(word);
            }

            if (i < words.length - 1) {
                result.append(" ");
            }
        }

        return result.toString();
    }

    public static ArivihanLanguageEnum getCurrentAppLanguageEnum() {
        String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        ArivihanLanguageEnum languageEnum = ArivihanLanguageEnum.ENGLISH;
        if (lang == null) {
        } else if ("hi".equals(lang) || "hi-rIN".equals(lang)) {
            languageEnum = ArivihanLanguageEnum.HINDI;
        }

        return languageEnum;
    }

    public static String getAppVersion() {

        StringBuilder versionBuilder = new StringBuilder();

        try {
            String baseUrl = getEndPoint();
//            String baseUrlDev = BuildConfig.BASE_URL_DEV;
            String baseUrlStage = BuildConfig.BASE_URL_STAGE;
            String baseUrlProd = BuildConfig.BASE_URL_PROD;

            versionBuilder.append("v").append(BuildConfig.VERSION_NAME);

            if (!baseUrl.equals(baseUrlProd)) {
//                versionBuilder.append(" DEV");
                versionBuilder.append(baseUrl.equals(baseUrlStage) ? " STAGE" : " DEV");
            }

            if (BuildConfig.DEBUG) {
                versionBuilder.append(" | DEBUG");
            }

        } catch (Exception e) {
            versionBuilder.setLength(0);
        }

        return versionBuilder.toString();
    }

    public static void openActivity(Context context, String className, Map<String, Object> params) {
        try {
            if (StringUtils.isEmpty(className)) {
                return;
            }

            Class<?> targetClass = ActivityLauncher.resolveClass(className);
            Intent intent = new Intent(context, targetClass);

            if (params != null) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();

                    // ✅ FIX: skip null values
                    if (value == null) {
                        Log.w(TAG, "Skipping null value for key: " + key);
                        continue;
                    }

                    if (value instanceof String) {
                        intent.putExtra(key, (String) value);
                    } else if (value instanceof Integer) {
                        intent.putExtra(key, (Integer) value);
                    } else if (value instanceof Boolean) {
                        intent.putExtra(key, (Boolean) value);
                    } else if (value instanceof HashMap) {
                        intent.putExtra(entry.getKey(), (Serializable) value);
                    } else if (value instanceof Short) {
                        intent.putExtra(key, (Short) value);

                    } else if (value instanceof Byte) {
                        intent.putExtra(key, (Byte) value);

                    } else if (value instanceof CharSequence) {
                        intent.putExtra(key, (CharSequence) value);

                    } else if (value instanceof Bundle) {
                        intent.putExtra(key, (Bundle) value);

                    } else if (value instanceof Parcelable) {
                        intent.putExtra(key, (Parcelable) value);

                    } else if (value instanceof Serializable) {
                        intent.putExtra(key, (Serializable) value);

                    } else if (value instanceof Map) {
                        Gson gson = new Gson();
                        String json = gson.toJson(value);
                        intent.putExtra(key, json);
                    } else {
                        intent.putExtra(key, String.valueOf(value));
                    }
                }
            }

            context.startActivity(intent);
//            Log.d(TAG, "openActivity--live: "+CommonMethods.getIntentParams(intent));

        } catch (ClassNotFoundException e) {
            Log.e("ClassNotFound", "Activity class not found", e);
            MyApplication.openGooglePlayStore(context);
        }
    }

    public static void openActivity(Context context, String className, String activityParamsJson) {
        try {
            Class<?> clazz = ActivityLauncher.resolveClass(className);
            Intent intent = new Intent(context, clazz);

            intent.putExtra("activityParams", activityParamsJson);

            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void openDeepLink(Context context, String deepLink) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(deepLink));
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ignored) {
        }
    }


    public static List<ToppersHomeScreenModel> getToppersData(String key) {
        Gson gson = new Gson();
        String s = sharedPreferences.getString(key, null);

        if (StringUtils.isNotEmpty(s)) {
            try {
                TypeToken<List<ToppersHomeScreenModel>> token =
                        new TypeToken<List<ToppersHomeScreenModel>>() {
                        };
                return gson.fromJson(s, token.getType());
            } catch (Exception e) {
                Log.e(TAG, "getToppersData: failed to parse JSON", e);
            }
        }
        return new ArrayList<>();
    }

    public static UserDetailsResponse getUserDetailsResponse(String key) {
        Gson gson = new Gson();
        String s = sharedPreferences.getString(key, null);

        if (StringUtils.isNotEmpty(s)) {
            try {
                if (s.trim().startsWith("{")) {
                    return gson.fromJson(s, UserDetailsResponse.class);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return new UserDetailsResponse(); // fallback
    }

    public static UserDto getUserDto(String key) {
        UserDetailsResponse userDetailsResponse = getUserDetailsResponse(key);

        if (userDetailsResponse == null) {
            userDetailsResponse = new UserDetailsResponse();
        }

        return userDetailsResponse.getUserDto();
    }

    public static void saveUserDto(String key, UserDetailsResponse userDto) {
        Gson gson = new Gson();
        if (userDto != null) {
            String json = gson.toJson(userDto);
            sharedPreferences.edit().putString(key, json).apply();
        }
    }


    public static void saveToppersData(String key, List<ToppersHomeScreenModel> highlistModelList) {
        Gson gson = new Gson();
        try {
            checkSavedData(HomeScreenCacheData.AV_TOPPERS_DATA.name());
            String test = gson.toJson(highlistModelList);
            saveData(key, test);
        } catch (Exception e) {
            saveData(key, null);
        }
    }

    public static void openPdfFromUrl(Context context, String pdfUrl) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(pdfUrl), "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            Intent chooser = Intent.createChooser(intent, "Open PDF");

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(chooser);
            } else {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl));
                context.startActivity(browserIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, context.getString(R.string.no_app_available_to_open_pdf), Toast.LENGTH_SHORT).show();
        }
    }

    public static void openInAppPdfFromUrl(Context context, String pdfUrl, String title, String noteType, String subjectName, String tag) {
        try {
            PdfUtils.openPdfViewer(context, pdfUrl, title, "lecture.notes", "", false, "", tag);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, context.getString(R.string.please_try_again), Toast.LENGTH_SHORT).show();
        }
    }

    public static void navigateToTodoSteps(Context context) {
        context.startActivity(new Intent(context, ToDoListMain.class));
//        context.startActivity(new Intent(context, ToDoListSteps.class));
    }

    public static String getAppVersionName(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (Exception ex) {
            return "";
        }
    }

    public static int getAppVersionCode(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionCode;
        } catch (Exception ex) {
            return 0;
        }
    }

    public static void getSubscriptionStatus(Context context, SubscriptionCallback callback) {
        Gson gson = new Gson();
        MyJsonObjectRequest request = new MyJsonObjectRequest(
                Request.Method.GET,
                CommonMethods.getUrlParam(CHECK_SUBSCRIPTION, null),
                null,
                response -> {
                    try {
                        TypeToken<SubscriptionCheckDto> token = new TypeToken<>() {
                        };
                        SubscriptionCheckDto dto = gson.fromJson(response.toString(), token.getType());
                        if (dto != null) {
                            Log.d("getSubscriptionStatus", "API Success: " + dto);
                            callback.onSuccess(dto);
                        } else {
                            Log.e("getSubscriptionStatus", "Parsed DTO is null");
                            callback.onError("Invalid response from server");
                        }
                    } catch (Exception e) {
                        Log.e("getSubscriptionStatus", "Error parsing API response", e);
                        callback.onError("Parsing error: " + e.getMessage());
                    }
                },
                new VolleyGlobalErrorListener(context) {
                    @Override
                    public void postError(VolleyError error) {
                        Log.e("getSubscriptionStatus", "VolleyError: " + error.getMessage(), error);
                        callback.onError("API error: " + error.getMessage());
                    }
                }
        );

        request.setTag("SubscriptionStatusRequest");
        MyVolleySingleton.getInstance(context).addToRequestQueue(request);
    }


    public static List<HomeScreenCardResponse> getFeatureSectionData(String key) {
        Gson gson = new Gson();
        String s = sharedPreferences.getString(key, null);

        if (StringUtils.isNotEmpty(s)) {
            try {
                TypeToken<List<HomeScreenCardResponse>> token =
                        new TypeToken<List<HomeScreenCardResponse>>() {
                        };
                return gson.fromJson(s, token.getType());
            } catch (Exception e) {
                Log.e(TAG, "getFeatureSectionData: failed to parse JSON", e);
            }
        }
        return new ArrayList<>();
    }

    public static void saveFeatureSectionData(String key, List<HomeScreenCardResponse> list) {
        Gson gson = new Gson();
        try {
            checkSavedData(HomeScreenCacheData.AV_FEATURE_SECTION_DATA.name());
            String test = gson.toJson(list);
            saveData(key, test);
        } catch (Exception e) {
            saveData(key, null);
        }
    }


    public static boolean isCompetitiveProfile() {

        String courseId = PreferenceMethods.getSelectedOnBoardingId();

        String matchingIds = FirebaseRemoteConfig
                .getInstance()
                .getString("competitive_selected_batch_id_v1");

        Log.d("CompetitiveCheck", "selected_course_id = " + courseId);
        Log.d("CompetitiveCheck", "competitive_ids = " + matchingIds);

        boolean result = Arrays.asList(matchingIds.split(",")).contains(courseId);

        Log.d("CompetitiveCheck", "isCompetitiveProfile = " + result);
        MyApplication.saveData(PreferenceKeys.IS_COMPETITIVE_PROFILE.getKey(), result);

        return result;
    }


    public static void saveCompetitiveToppersData(String key, ToppersResponseDto toppersResponseDtos) {
        Gson gson = new Gson();
        try {
            checkSavedData(HomeScreenCacheData.AV_COMPETITIVE_TOPPERS_DATA.name());
            String json = gson.toJson(toppersResponseDtos); // convert the full object
            saveData(key, json);
        } catch (Exception e) {
            saveData(key, null);
        }
    }

    public static ToppersResponseDto getCompetitiveToppersData(String key) {
        Gson gson = new Gson();
        String s = sharedPreferences.getString(key, null);

        if (StringUtils.isNotEmpty(s)) {
            try {
                return gson.fromJson(s, ToppersResponseDto.class);
            } catch (Exception e) {
                Log.e(TAG, "getCompetitiveToppersData: failed to parse JSON", e);
            }
        }
        return new ToppersResponseDto();
    }

    public static String getCurrentAppLanguage(Context context) {
        if (context == null) return "";

        Locale locale;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            LocaleList locales = context.getResources()
                    .getConfiguration()
                    .getLocales();
            locale = locales.isEmpty() ? Locale.getDefault() : locales.get(0);
        } else {
            // Android 12 and below
            locale = context.getResources()
                    .getConfiguration()
                    .locale;
        }

        return locale != null ? locale.getLanguage() : "";
    }

    public static void updateAppLocale(Context context, String languageCode) {
        try {
            String currentLang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
            if (languageCode == null || (StringUtils.isNotEmpty(currentLang) && currentLang.equals(languageCode))) {
                return;
            }

            Locale locale = new Locale(languageCode);
            Locale.setDefault(locale);

            Resources resources = context.getResources();
            Configuration configuration = resources.getConfiguration();
            configuration.setLocale(locale);

            MyApplication.saveData(PreferenceKeys.MY_LANG.getKey(), ArivihanLanguageEnum.fromStringToCode(languageCode));
            context.getApplicationContext().createConfigurationContext(configuration);
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        } catch (Exception ignored) {
        }
    }

    public static ActivityNavigationConfig getBottomNavActivityConfig() {
        try {
            String batchCode = getStringData(PreferenceKeys.BATCH_CODE.getKey());
            String json = getFirebaseRemoteConfig().getString("competitive_bottom_nav_config");
            if (StringUtils.isEmpty(json)) {
                Log.e("Navigation", "competitive_bottom_nav_config is empty");
                return null;
            }

            Type type = new TypeToken<Map<String, CompetitiveBatchNavConfig>>() {}.getType();
            Map<String, CompetitiveBatchNavConfig> configMap = new Gson().fromJson(json, type);
            if (configMap == null) return null;

            CompetitiveBatchNavConfig batchConfig = configMap.get(batchCode);
            if (batchConfig == null || batchConfig.getTabs() == null) return null;

            for (CompetitiveBottomNavTab tab : batchConfig.getTabs()) {
                if (!"CENTER".equals(tab.getTabId())) continue;
                BottomNavTabNavigation nav = tab.getSubscribedNavigation();
                if (nav == null) return null;

                String lang = getStringData(PreferenceKeys.MY_LANG.getKey());
                String featureName = null;
                if (tab.getLabel() != null) {
                    featureName = tab.getLabel().get(StringUtils.isNotEmpty(lang) ? lang : "en");
                    if (StringUtils.isEmpty(featureName)) featureName = tab.getLabel().get("en");
                }
                Log.d("Navigation", "competitive_bottom_nav_config CENTER tab resolved for batchCode=" + batchCode);
                return new ActivityNavigationConfig(
                        nav.getClassName(),
                        nav.getParams(),
                        featureName,
                        null, false, null,
                        nav.getNavigationType());
            }
            Log.e("Navigation", "No CENTER tab found in competitive_bottom_nav_config for batchCode=" + batchCode);
        } catch (Exception e) {
            Log.e("Navigation", "getBottomNavActivityConfig: failed to parse config", e);
        }
        return null;
    }

    public static boolean isOldTwelve2026Profile() {
        try {
            String gradeCode = MyApplication.getStringData(PreferenceKeys.GRADE_CODE.getKey());
            String yearCode = MyApplication.getStringData(PreferenceKeys.YEAR_CODE.getKey());

            String selectedGrade = MyApplication.getFirebaseRemoteConfig().getString("hidden_selected_profile");

            return gradeCode.equals(selectedGrade) && yearCode.equals("2026");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static RescheduledLectureStatusDto parseRescheduledLectureResult(Map<String, Object> sessionData) {
        Log.d("rescheduled_debug", "parseRescheduledLectureResult keys=" + sessionData.keySet());
        RescheduledLectureStatusDto dto = new RescheduledLectureStatusDto();
            try {
                dto.setChapterId((String) sessionData.get("chapterId"));
                dto.setSubjectId((String) sessionData.get("subjectId"));
                dto.setSubjectCode((String) sessionData.get("subjectCode"));
                dto.setTitle((String) sessionData.get("title"));
                dto.setSubTitle((String) sessionData.get("subTitle"));
                dto.setThumbnailUrl((String) sessionData.get("thumbnailUrl"));

            Object newStartTimeObj = sessionData.get("newStartTime");
            if (newStartTimeObj instanceof Long) {
                dto.setNewStartTime((Long) newStartTimeObj);
            } else if (newStartTimeObj instanceof Number) {
                dto.setNewStartTime(((Number) newStartTimeObj).longValue());
            }

            Object popUpObj = sessionData.get("upcomingStatusPopUp");
            if (popUpObj instanceof Map) {
                Map<String, Object> popUpMap = (Map<String, Object>) popUpObj;
                ChapterPopUpDto popUp = ChapterPopUpDto.builder()
                        .title((String) popUpMap.get("title"))
                        .subTitle((String) popUpMap.get("subTitle"))
                        .btnName((String) popUpMap.get("btnName"))
                        .build();
                dto.setUpcomingStatusPopUp(popUp);
            }
        } catch (Exception e) {
            Log.d("parseRescheduledLecture", "Skipping invalid entry: " + e.getMessage());
        }
        return dto;
    }

    public static DatabaseReference getRescheduledLectureStatusDatabaseReference() {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());
        String batchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
        String env = EnvironmentManager.getEnvironmentNameByUrl(EnvironmentManager.getEndPoint());
        String nodeBase = "rescheduled_live_classes";
        if (!"PROD".equalsIgnoreCase(env)) {
            nodeBase = nodeBase + "_" + env.toLowerCase();
        }
        String path = "users/" + nodeBase + "/" + batchCode + "/" + currentDate;
        Log.d("rescheduled_debug", "getRescheduledLectureStatusDatabaseReference: batchCode=" + batchCode
                + " date=" + currentDate + " fullPath=" + path);
        Log.d("rescheduled_live_class_rtm", path);
        return FirebaseDatabase.getInstance().getReference(path);
    }

    public static void markRescheduledLectureViewed(String lectureId) {
        if (lectureId == null) return;
        saveData("viewed_rescheduled_" + lectureId, true);
        Log.d("rescheduled_debug", "markRescheduledLectureViewed: lectureId=" + lectureId);
    }

    public static boolean isRescheduledLectureViewed(String lectureId) {
        if (lectureId == null) return false;
        boolean viewed = sharedPreferences.getBoolean("viewed_rescheduled_" + lectureId, false);
        Log.d("rescheduled_debug", "isRescheduledLectureViewed: lectureId=" + lectureId + " viewed=" + viewed);
        return viewed;
    }
}
