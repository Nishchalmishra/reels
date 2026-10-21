package arivihan.technologies.doubtbuzzter2.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.ReferAndEarn;

import arivihan.technologies.doubtbuzzter2.activity.SettingsActivity;
import arivihan.technologies.doubtbuzzter2.activity.SubscriptionCongatulationsActivity;

import arivihan.technologies.doubtbuzzter2.activity.TestResultActivity;
import arivihan.technologies.doubtbuzzter2.activity.asat.ASATActivity;
import arivihan.technologies.doubtbuzzter2.activity.AdminEditRoutineActivity;
import arivihan.technologies.doubtbuzzter2.activity.AdminToDoListMain;
import arivihan.technologies.doubtbuzzter2.activity.AnalyticsActivity;
import arivihan.technologies.doubtbuzzter2.activity.ChatSupportHistoryActivity;
import arivihan.technologies.doubtbuzzter2.activity.CurrentDoubtActivity;
import arivihan.technologies.doubtbuzzter2.activity.DoubtHistoryActivity;
import arivihan.technologies.doubtbuzzter2.activity.EditProfileActivity;
import arivihan.technologies.doubtbuzzter2.activity.GameActivity;
import arivihan.technologies.doubtbuzzter2.activity.MyDownloadActivity;
import arivihan.technologies.doubtbuzzter2.activity.MySubscriptions;
import arivihan.technologies.doubtbuzzter2.activity.NotificationActivity;
import arivihan.technologies.doubtbuzzter2.activity.UserAuthenticationActivity;
import arivihan.technologies.doubtbuzzter2.activity.QuickNotesActivity;
import arivihan.technologies.doubtbuzzter2.activity.ToDoListMain;
import arivihan.technologies.doubtbuzzter2.activity.VernacularActivity;
import arivihan.technologies.doubtbuzzter2.activity.competitive.FeatureInfoVideoActivity;
import arivihan.technologies.doubtbuzzter2.activity.refer.OrderHistoryActivity;
import arivihan.technologies.doubtbuzzter2.activity.refer.ReferAndEarnActivity;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.customView.ContactUsBottomSheetFragment;
import arivihan.technologies.doubtbuzzter2.customView.QuestionDialogPopup;
import arivihan.technologies.doubtbuzzter2.enums.AnalyticsEvent;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.helper.BranchAnalyticsHelper;
import arivihan.technologies.doubtbuzzter2.helper.RemoteConfigUtils;
import arivihan.technologies.doubtbuzzter2.helper.ScreenAnalyticsHelper;
import arivihan.technologies.doubtbuzzter2.helper.firebase.FirebaseMessagingManager;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionType;
import arivihan.technologies.doubtbuzzter2.listener.TapTargetListener;
import arivihan.technologies.doubtbuzzter2.model.TestGenerationStatus;
import arivihan.technologies.doubtbuzzter2.model.TestGenerationStatusDto;
import arivihan.technologies.doubtbuzzter2.model.UserDto;
import arivihan.technologies.doubtbuzzter2.model.batch.user.UserDetailsResponse;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.viewModel.HomeScreenViewModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_GET_USER_BY_PHONE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.LOGOUT_YOUR_ACCOUNT;

import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class ProfileFragment extends Fragment implements TapTargetListener {

    private static final String TAG = ProfileFragment.class.getSimpleName();
    public static String bundleTodoVisibility = "todo_visibility";
    LinearLayout ll;
    ImageView edit;
    RelativeLayout quick_notes, language, analytics, game, notification, rl_competition, current_doubt, solved_doubt, mock_test, refer_earn, buy_points, contact, logout,
            subscriptionPlan, leaderboard, routine, adminRoutine, chatSupportHistory, myDownload, rl_app_review, rl_settings, refer_and_earn_order_history;
    ConstraintLayout parentLayout;
    String Reference_code = "";
    TextView yourPoints, FreeDoubt, name, Class, course, tv_asat_text;
    private boolean syllabusGenerated;
    private boolean generationInProgress;
    private ImageView profileIcon, badge;
    CardView insta, whatsapp, gmail, youtube;
    private int dataLoaded = 0;
    private QuestionDialogPopup behaviourDialogPopup;
    private Dialog dialog, logOutDialog;
    private ProgressDialog loading, routineLoading;
    private LottieAnimationView lottieAnimationView;
    boolean openContactUs = false;
    private Timer todoListNotificationCheckTimer;
    private ValueEventListener valueEventListener;
    private boolean isTodoVisible;
    private boolean hideSubscriptionPlan;

    //badge
    private MaterialCardView profileSubscriptionBadge;
    private ImageView badgeIcon;
    private TextView batchName;
    private MaterialCardView profileOuterRing;

    private enum ProfileItems {
        SUBSCRIPTION_PLAN,
        LANGUAGE,
        MY_SUBSCRIPTION,
        MY_DOWNLOADS,
        SCHOLARSHIP_TEST,
        RATE_THE_APP,
        ANALYTICS,
        NOTIFICATION,
        YOUR_ROUTINE,
        SHORT_NOTES,
        RANKING,
        MY_DOUBTS,
        CONTACT_US,
    }

    @Override
    public void onResume() {
        super.onResume();
        if (MyApplication.getStringData(PreferenceKeys.GRADE_NAME.getKey()) != null)
            Class.setText(MyApplication.getStringData(PreferenceKeys.GRADE_NAME.getKey()));
        if (MyApplication.getStringData(PreferenceKeys.YEAR_NAME.getKey()) != null)
            course.setText(MyApplication.getStringData(PreferenceKeys.YEAR_NAME.getKey()));
        if (MyApplication.getStringData(PreferenceKeys.NAME.getKey()) != null) {
            dataLoaded = 1;
            name.setText(MyApplication.getStringData(PreferenceKeys.NAME.getKey()));
        }
        if (MyApplication.getStringData(PreferenceKeys.IMAGE_URI.getKey()) != null) {
            Log.i("Image Url ", MyApplication.getStringData(PreferenceKeys.IMAGE_URI.getKey()));
            Glide.with(getActivity()).load(MyApplication.getStringData(PreferenceKeys.IMAGE_URI.getKey())).into(profileIcon);
        }
        if (MyApplication.getIntData(PreferenceKeys.YOUR_POINTS.getKey()) < 0)
            yourPoints.setText("0");
        else {
            yourPoints.setText(String.valueOf(MyApplication.getIntData(PreferenceKeys.YOUR_POINTS.getKey())));
        }

        if (MyApplication.getBoolData(PreferenceKeys.UNWATCHED.getKey())) {
            badge.setVisibility(View.VISIBLE);
        } else {
            badge.setVisibility(View.GONE);
        }
        FreeDoubt.setText(String.valueOf(MyApplication.getIntData(PreferenceKeys.FREE_DOUBT.getKey())));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (logOutDialog != null && logOutDialog.isShowing())
            logOutDialog.dismiss();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isTodoVisible = getArguments().getBoolean(bundleTodoVisibility, true);
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        EdgeToEdgeHelper.padForSystemBars(view);

        getAPIDataByPhoneData(false, null);

        routine = view.findViewById(R.id.routine);
        if (RemoteConfigUtils.isFeatureVisible(RemoteConfigUtils.FeatureVisibilityName.EDIT_ROUTINE.getFeature())) {
            routine.setVisibility(isTodoVisible ? View.VISIBLE : View.GONE);
        } else {
            routine.setVisibility(View.GONE);
        }
//        adminRoutine = view.findViewById(R.id.admin_routine);
//        String adminPhoneNumbers = CommonMethods.getObjectFromRemoteConfig("admin_phone_number");
//
//        if (StringUtils.isNotEmpty(adminPhoneNumbers)) {
//            String[] numberList = adminPhoneNumbers.split(",");
//            String phoneNumber = MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey());
//            for (String s : numberList) {
//                if (StringUtils.isNotEmpty(phoneNumber) && StringUtils.isNotEmpty(s) && s.equals(phoneNumber)) {
//                    adminRoutine.setVisibility(View.VISIBLE);
//                    break;
//                }
//            }
//
//        }

        ll = view.findViewById(R.id.bottom_card);
        name = view.findViewById(R.id.name);
        Class = view.findViewById(R.id.Class);
        course = view.findViewById(R.id.course);
        badge = view.findViewById(R.id.badge);
        parentLayout = view.findViewById(R.id.ParentRelativeLayout);
        edit = view.findViewById(R.id.edit_profile);
        rl_competition = view.findViewById(R.id.rl_competition);
        tv_asat_text = view.findViewById(R.id.tv_asat_text);
        quick_notes = view.findViewById(R.id.quick_notes);
        language = view.findViewById(R.id.language);
        myDownload = view.findViewById(R.id.my_download);
        analytics = view.findViewById(R.id.analytics);
        game = view.findViewById(R.id.game);
        notification = view.findViewById(R.id.notification);
        current_doubt = view.findViewById(R.id.Current_Doubt);
        solved_doubt = view.findViewById(R.id.Solved_Doubt);
        mock_test = view.findViewById(R.id.Subscription);
        yourPoints = view.findViewById(R.id.your_points);
        FreeDoubt = view.findViewById(R.id.free_doubt);
        refer_earn = view.findViewById(R.id.Refer_And_Earn);
        refer_and_earn_order_history = view.findViewById(R.id.refer_and_earn_order_history);
        subscriptionPlan = view.findViewById(R.id.SubscriptionPlan);
        buy_points = view.findViewById(R.id.Buy_Points);
        contact = view.findViewById(R.id.Contact_Us);
        leaderboard = view.findViewById(R.id.leaderboard);
        logout = view.findViewById(R.id.Logout);
        profileIcon = view.findViewById(R.id.profile_image);
        chatSupportHistory = view.findViewById(R.id.profile_chat_support);
        rl_app_review = view.findViewById(R.id.rl_app_review);
        rl_settings = view.findViewById(R.id.rl_settings);

        //badge
        profileSubscriptionBadge = view.findViewById(R.id.subscriptionBadge);
        badgeIcon = view.findViewById(R.id.badge_icon);
        batchName = view.findViewById(R.id.badge_name);
        profileOuterRing = view.findViewById(R.id.profileOuterRing);

        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.arivihan_dialog);
        dialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(getContext(), R.drawable.bachground_custom_dialogu_typeactivity));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        course.setVisibility(MyApplication.getFirebaseRemoteConfig().getBoolean("profile_course_visibility") ? View.VISIBLE : View.GONE);
        lottieAnimationView = dialog.findViewById(R.id.lottie);
        lottieAnimationView.setAnimation(R.raw.edit_routine_lottie_star);
        if (MyApplication.getBoolData(PreferenceKeys.UNWATCHED.getKey())) {
            badge.setVisibility(View.VISIBLE);
        } else {
            badge.setVisibility(View.GONE);
        }

        loading = new ProgressDialog(getContext());
        loading.setMessage(getString(R.string.preparing_your_test));
        loading.setCanceledOnTouchOutside(false);

        routineLoading = new ProgressDialog(getContext());
        routineLoading.setMessage(getString(R.string.preparing_your_test));
        routineLoading.setCanceledOnTouchOutside(true);

        if (MyApplication.getStringData(PreferenceKeys.GRADE_NAME.getKey()) != null)
            Class.setText(MyApplication.getStringData(PreferenceKeys.GRADE_NAME.getKey()));
        if (MyApplication.getStringData(PreferenceKeys.YEAR_NAME.getKey()) != null)
            course.setText(MyApplication.getStringData(PreferenceKeys.YEAR_NAME.getKey()));
        if (MyApplication.getStringData(PreferenceKeys.DISPLAY_NAME.getKey()) != null) {
            dataLoaded = 1;
            name.setText(MyApplication.getStringData(PreferenceKeys.DISPLAY_NAME.getKey()));
        }
        if (MyApplication.getStringData(PreferenceKeys.IMAGE_URI.getKey()) != null) {
            Log.i("Image Url ", MyApplication.getStringData(PreferenceKeys.IMAGE_URI.getKey()));
            Glide.with(getActivity()).load(MyApplication.getStringData(PreferenceKeys.IMAGE_URI.getKey())).into(profileIcon);
        }
        if (MyApplication.getIntData(PreferenceKeys.YOUR_POINTS.getKey()) == null)
            yourPoints.setText("0");
        else
            yourPoints.setText(String.valueOf(MyApplication.getIntData(PreferenceKeys.YOUR_POINTS.getKey())));

        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_up);
        ll.startAnimation(animation);

        Animation animationx = AnimationUtils.loadAnimation(getActivity(), R.anim.scale);
        profileIcon.startAnimation(animationx);
        profileSubscriptionBadge.startAnimation(animationx);
        profileOuterRing.startAnimation(animationx);

        quick_notes.setOnClickListener(view16 -> {
            Intent intent = new Intent(getActivity(), QuickNotesActivity.class);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.profile_quick_notes_clicked.getActionName());
            startActivity(intent);
        });

        openContactUs = getActivity().getIntent().getBooleanExtra("openContactUs", false);
        if (openContactUs) {
            view.post(this::showContactUsBottomSheet);
        }

        rl_settings.setOnClickListener(view14 -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.app_settings_clicked.getActionName());
            ScreenAnalyticsHelper.sendScreenVisitedEvent(AnalyticsEvent.PROFILE_SCREEN_SETTINGS.name(), AnalyticsEvent.PROFILE_SCREEN_SETTINGS.name());
            startActivity(intent);
        });

        language.setOnClickListener(view16 -> {
            Intent intent = new Intent(getActivity(), VernacularActivity.class);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.profile_language_clicked.getActionName());
            startActivity(intent);
        });

        analytics.setOnClickListener(view16 -> {
            Intent intent = new Intent(getActivity(), AnalyticsActivity.class);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.profile_analytics_clicked.getActionName());
            startActivity(intent);
        });

        edit.setOnClickListener(view14 -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
//            Intent intent = new Intent(getActivity(), UnlockActivity.class);
            intent.putExtra("condition_type", "node");

            MyApplication.logFirebaseBundleCreation(FirebaseEvents.clicked_edit_profile.getActionName());
            startActivity(intent);
//            showDialog();
        });

        leaderboard.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TestResultActivity.class);
            intent.putExtra("TestId", "");
            startActivity(intent);
        });
//        leaderboard.setOnClickListener(v -> {
//            Intent intent = new Intent(requireContext(),
//                    leaderboard.class);
//            startActivity(intent);
//        });

        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), NotificationActivity.class);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.clicked_notifications.getActionName());
                startActivity(intent);
            }
        });
        game.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), GameActivity.class);
            startActivity(intent);
        });

        myDownload.setOnClickListener(V -> {
            //TODO-remove this dummy code
            // DEBUG: create IN_PROGRESS test generation entry for status card testing
            if (BuildConfig.DEBUG) {
                HashMap<String, Object> testData = new HashMap<>();
                testData.put("testName", "NEET Practice Test");
                testData.put("status", TestGenerationStatus.IN_PROGRESS.name());
                testData.put("startTime", System.currentTimeMillis());
                testData.put("closeTime", System.currentTimeMillis() + 5 * 60 * 1000L);
                MyApplication.getTestGenerationDatabaseReference("debug_test_gen_2").setValue(testData);
                return;
            }
            Intent intent = new Intent(getActivity(), MyDownloadActivity.class);
            ScreenAnalyticsHelper.sendScreenVisitedEvent(AnalyticsEvent.PROFILE_SCREEN_DOWNLOAD.name(), AnalyticsEvent.PROFILE_SCREEN_DOWNLOAD.name());
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.profile_my_download_clicked.getActionName());
            startActivity(intent);
        });
        
        current_doubt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CurrentDoubtActivity.class);
                startActivity(intent);
            }
        });
        solved_doubt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), DoubtHistoryActivity.class);
                intent.putExtra("redirectToProfileScreen", true);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.profile_my_doubt_clicked.getActionName());
                ScreenAnalyticsHelper.sendScreenVisitedEvent(AnalyticsEvent.PROFILE_SCREEN_MY_DOUBT.name(), AnalyticsEvent.PROFILE_SCREEN_MY_DOUBT.name());
                startActivity(intent);
            }
        });

        chatSupportHistory.setOnClickListener(V -> {
            startActivity(new Intent(getActivity(), ChatSupportHistoryActivity.class));
        });

        mock_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MySubscriptions.class);
                intent.putExtra("hideSubscriptionPlan",hideSubscriptionPlan);
                startActivity(intent);
            }
        });
        subscriptionPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SubscriptionNavigationHelper.navigateToSubscriptionPage(getActivity(), RedirectionSource.PROFILE_JOIN);
//                Intent intent = new Intent(getActivity(), CampaignSubscriptionActivity.class);
////                intent.putExtra("plan_level", "COURSE");
//                intent.putExtra(PreferenceKeys.GO_TO_PROFILE_FRAGMENT.getKey(), true);
//                MyApplication.logFirebaseBundleCreation(FirebaseEvents.clicked_subscription_plan.getActionName());
//                startActivity(intent);
            }
        });
        refer_earn.setOnClickListener(view12 -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.clicked_refer_earn.getActionName());
            Intent intent = new Intent(getActivity(), ReferAndEarnActivity.class);
            intent.putExtra("Refer", Reference_code);
            startActivity(intent);
        });

        refer_and_earn_order_history.setOnClickListener(view13 -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.clicked_order_history.getActionName());
            OrderHistoryActivity.Companion.start(getActivity(), true);
        });

        routine.setOnClickListener(view1 -> {
            getAPIDataByPhoneData(true, null);
        });

//        adminRoutine.setOnClickListener(view1 -> {
//            showPhoneNumberDialog();
//        });

        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.clicked_contact_us.getActionName());
                showContactUsBottomSheet();
            }
        });
        logout.setOnClickListener(view15 -> {

//            if (BuildConfig.DEBUG) {
//                MyApplication.getTestGenerationDatabaseReference("debug_test_gen_2")
//                        .child("status")
//                        .setValue(TestGenerationStatus.COMPLETED_UNSEEN.name());
//                return;
//            }
            logOutPopUp();
        });

        rl_app_review.setOnClickListener(v -> {
            CommonMethods.initAppReviewDialog(requireContext());
        });

        rl_competition.setOnClickListener(view1 -> {

            //trigger event of asat clicked
            Map<String, String> params = new HashMap<>();
            params.put("screen", ProfileFragment.class.getSimpleName());
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.asat_screen_click.getActionName(), params);

            Intent intent = new Intent(requireActivity(), ASATActivity.class);
            intent.putExtra(SubscriptionNavigationHelper.EXTRA_SOURCE, RedirectionSource.ASAT);
//            intent.putExtra(ASATActivity.ASAT_OPEN_RESULT, MyApplication.getBoolData("asatSubscribed"));
            startActivity(intent);
        });

        rl_competition.setVisibility(MyApplication.getBoolData(PreferenceKeys.SHOW_ASAT.getKey())
                ? View.VISIBLE : View.GONE);
        tv_asat_text.setText(MyApplication.getStringData(PreferenceKeys.ASAT_TITLE.getKey())
                != null ? MyApplication.getStringData(PreferenceKeys.ASAT_TITLE.getKey()) : getString(R.string.Scholarship_Test));


        if (CommonMethods.isOnline(getActivity())) {
            if (dataLoaded != 1)
                getAPIDataByPhoneData(false, null);
            if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
            }
        } else {
            if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                CommonMethods.showNegativeSnackbar(parentLayout, getActivity().getString(R.string.NoInternetConnection));
                MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
            }
        }

        hideProfileItems();

        if (RemoteConfigUtils.isFeatureVisible(RemoteConfigUtils.FeatureVisibilityName.LANGUAGE_CHANGE.getFeature())) {
            language.setVisibility(View.VISIBLE);
        } else {
            language.setVisibility(View.GONE);
        }

        if (!MyApplication.isCompetitiveProfile()) {
            solved_doubt.setVisibility(SubscriptionType.hasActiveSubscription() ? View.VISIBLE : View.GONE);
        }

        //order history
        refer_and_earn_order_history.setVisibility(isOrderHistoryEligible() ? View.VISIBLE : View.GONE);

        //badge
        updateSubscriptionBadge();


        profileSubscriptionBadge.setOnClickListener(v -> {
            if (!MyApplication.isNetworkAvailable()) {
                Toast.makeText(getContext(), getString(R.string.internet_failure_error), Toast.LENGTH_SHORT).show();
                return;
            }

            //NEET
            if (MyApplication.isCompetitiveProfile()) {
                if (!SubscriptionNavigationHelper.hasActiveSubscriptionByBatch()) return;

                String planId = MyApplication.getStringData(PreferenceKeys.USER_SUBSCRIPTION_PLAN_ID.getKey());
                if (planId == null || planId.isEmpty()) return;

                Intent intent = new Intent(requireContext(), SubscriptionCongatulationsActivity.class);
                intent.putExtra("planId", planId);
                startActivity(intent);

            }
            //Board
            else {
                SubscriptionType subscriptionType = SubscriptionType.getSubscriptionType();

                if (subscriptionType == SubscriptionType.PLUS) {
                    String url = MyApplication.getStringData(PreferenceKeys.USER_SUBSCRIPTION_PROFILE_VIDEO_URL.getKey());
                    if (url == null || url.isEmpty()) return;

                    navigateToCongratulations(url);

                } else if (subscriptionType == SubscriptionType.PRO) {
                    String planId = MyApplication.getStringData(PreferenceKeys.USER_SUBSCRIPTION_PLAN_ID.getKey());
                    if (planId == null || planId.isEmpty()) return;

                    Intent intent = new Intent(requireContext(), SubscriptionCongatulationsActivity.class);
                    intent.putExtra("planId", planId);
                    startActivity(intent);
                }
            }
        });
        return view;
    }

    private void navigateToCongratulations(String completedVideoUrl) {
        Log.d(TAG, "navigateToCongratulations()");
        try {
            if (StringUtils.isNotEmpty(completedVideoUrl)) {
                Intent intent = new Intent(requireContext(), FeatureInfoVideoActivity.class);
                intent.putExtra("hide_bottom_layout", true);
                intent.putExtra("videoUrl", completedVideoUrl);
                intent.putExtra(FeatureInfoVideoActivity.bundleNavigationFrom,
                        FeatureInfoVideoActivity.NavigationFromEnum
                                .SUBSCRIPTION_PAYMENT_SCREEN.name());
                startActivity(intent);
            }
        } catch (Exception e) {
            Log.e(TAG, "navigateToCongratulations error", e);
            Toast.makeText(requireContext(), getString(R.string.server_error), Toast.LENGTH_SHORT).show();
        }
    }

    private void showDialog() {

        Dialog dialog1 = new Dialog(requireContext());
        dialog1.setContentView(R.layout.dialog_competitive_lecture_start_warning);
        Objects.requireNonNull(dialog1.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog1.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        EdgeToEdgeHelper.applyToDialog(dialog1);
        dialog1.setCancelable(false);
        dialog1.setCanceledOnTouchOutside(false);
        dialog1.show();
    }

    private void logOutPopUp() {
        if (logOutDialog != null && logOutDialog.isShowing()) {
            return;
        }
        logOutDialog = new Dialog(requireContext());
        logOutDialog.setContentView(R.layout.dialog_logout);
        Objects.requireNonNull(logOutDialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        logOutDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        EdgeToEdgeHelper.applyToDialog(logOutDialog);
        logOutDialog.setCancelable(false);
        logOutDialog.setCanceledOnTouchOutside(false);

        ImageView iv_cancel = logOutDialog.findViewById(R.id.iv_cancel);

        MaterialCardView cv_yes = logOutDialog.findViewById(R.id.cv_yes);
        MaterialCardView cv_no = logOutDialog.findViewById(R.id.cv_no);

        TextView tv_logout_title = logOutDialog.findViewById(R.id.tv_logout_title);
        TextView tv_logout_body = logOutDialog.findViewById(R.id.tv_logout_body);

        tv_logout_title.setText(getString(R.string.logout_confirm_karein) + "!");
        tv_logout_body.setText(getString(R.string.kya_aap_logout_karna_chahte_hein) + "?");

        LottieAnimationView animationView = logOutDialog.findViewById(R.id.lottie);
        animationView.setRotation(0);
        animationView.setAnimation(R.raw.logout_popup);
        animationView.playAnimation();

        logOutDialog.setOnDismissListener(v -> {
            animationView.cancelAnimation();
            animationView.clearAnimation();
        });

        iv_cancel.setOnClickListener(v -> {
            logOutDialog.dismiss();
        });
        cv_no.setOnClickListener(v -> {
            logOutDialog.dismiss();
        });
        cv_yes.setOnClickListener(v -> {
            logOutDialog.dismiss();
            logout();
        });

        logOutDialog.show();

    }

    private void logout() {
        try {
            if (getActivity() == null)
                return;

            FirebaseMessagingManager.detachUser(requireContext());
            FirebaseMessagingManager.unSubscribeToUserDefaultTopics();
            BranchAnalyticsHelper.logoutUser();

            //logout api
            try {
                MyJsonObjectRequest logoutRequest = new MyJsonObjectRequest(
                        Request.Method.DELETE,
                        CommonMethods.getUrlParam(LOGOUT_YOUR_ACCOUNT, Collections.emptyMap()),
                        null,
                        response -> {},
                        error -> {}
                );
                MyVolleySingleton.getInstance(requireContext()).addToRequestQueue(logoutRequest);
            } catch (Exception e) {
                e.printStackTrace();
            }

            MyApplication application = (MyApplication) getActivity().getApplication();
            HomeScreenViewModel homeScreenViewModel = application.getHomeScreenViewModel();
            homeScreenViewModel.removeDataModel(); // cancel in-flight requests BEFORE clearing prefs

            deleteAllLocalData();

            Intent homeIntent = new Intent(getActivity(), UserAuthenticationActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(homeIntent);

            CommonMethods.showPositiveSnackbar(parentLayout, getString(R.string.SuccessfullLogout));
            getActivity().finish();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void deleteAllLocalData() {
        MyApplication.saveData(getString(R.string.user_selected_video_quality), "");
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.clicked_logout.getActionName());
        MyApplication.clearSharedPref();
        getViewModelStore().clear();

        MyApplication.removeFromDoNotDeleteSharedPreference(PreferenceKeys.REFERRAL_CODE.getKey());
        MyApplication.removeFromDoNotDeleteSharedPreference(PreferenceKeys.REFERRAL_SOURCE.getKey());
        MyApplication.removeFromDoNotDeleteSharedPreference(PreferenceKeys.REFERRAL_DETECTED.getKey());

//        Executors.newSingleThreadExecutor().execute(() -> {
//            AppDatabase db = AppDatabase.getDatabase(requireContext().getApplicationContext());
//            db.clearAllTables();
//        });
    }

    private void showContactUsBottomSheet() {
        if (!isAdded() || getActivity() == null) return;
        ContactUsBottomSheetFragment contactSheet = new ContactUsBottomSheetFragment();
        contactSheet.show(
                requireActivity().getSupportFragmentManager(),
                "profile_contact_bottom_sheet"
        );
    }


    private void handleTodolistReady() {
        if (MyApplication.getBoolData(PreferenceKeys.IS_TODOLIST_READY.getKey())) {
            dialog.dismiss();
            MyApplication.saveData(PreferenceKeys.IS_TODOLIST_READY.getKey(), false);
            todoListNotificationCheckTimer.cancel();
        }
    }

    private void getAPIDataByPhoneData(boolean checkInRoutine, String phoneNumber) {
        Log.d("courseidissue", "getAPIDataByPhoneData: " +
                MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey()));
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isEmpty(phoneNumber)) {
            String PhoneNumber = MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey());
            map.put("phoneNumber", PhoneNumber);
        } else {
            map.put("phoneNumber", phoneNumber);
        }
        map.put("key", BuildConfig.API_KEY);
//        map.put(PreferenceKeys.BOARD.getKey(), MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey()));
//        map.put(PreferenceKeys.SELECTED_COURSE_ID_LOCAL.getKey(), MyApplication.getStringData(PreferenceKeys.SELECTED_COURSE_ID_LOCAL.getKey()));
        Log.d(TAG, "getAPIDataByPhoneData: " + map.toString());
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(AV_GET_USER_BY_PHONE, map), null,
                response -> {
                    Gson gson = new Gson();
                    Log.i("Api called", "Profile Fragment");
                    TypeToken<ApiWrappedResponse<UserDetailsResponse>> token = new TypeToken<ApiWrappedResponse<UserDetailsResponse>>() {
                    };
                    ApiWrappedResponse<UserDetailsResponse> apiResponse = gson.fromJson(response.toString(), token.getType());
                    UserDetailsResponse user = (apiResponse != null && apiResponse.isSuccess()) ? apiResponse.getData() : null;
                    Log.d(TAG, "getAPIDataByPhoneData: " + user);
                    if (user != null && user.getUserDto() != null) {
                        yourPoints.setText(String.valueOf(user.getUserDto().getPoints()));
                        FreeDoubt.setText(String.valueOf(user.getUserDto().getFreeDoubt()));
                        name.setText(user.getUserDto().getUsername());
                        Reference_code = user.getUserDto().getMyReferCode();
                        syllabusGenerated = Boolean.TRUE.equals(user.getUserDto().getSyllabusGenerated());
                        generationInProgress = Boolean.TRUE.equals(user.getUserDto().getGenerationInProgress());
                        if (StringUtils.isNotEmpty(user.getUserDto().getImageUrl())) {
                            MyApplication.saveData(PreferenceKeys.PROFILE_ICON.getKey(), user.getUserDto().getImageUrl());
                            if (getActivity() != null && !getActivity().isDestroyed())
                                Glide.with(this).load(user.getUserDto().getImageUrl()).into(profileIcon);
                        }
                        MyApplication.saveUserProfileLocally(user);
                    }

                    //badge update
                    updateSubscriptionBadge();

                    Class.setText(MyApplication.getStringData(PreferenceKeys.GRADE_NAME.getKey()));
                    course.setText(MyApplication.getStringData(PreferenceKeys.YEAR_NAME.getKey()));

                    if (checkInRoutine && StringUtils.isEmpty(phoneNumber)) {
                        routineLoading.dismiss();
                        if (syllabusGenerated) {
                            observeForRoutine();
                        } else {
                            MyApplication.navigateToTodoSteps(requireContext()); //remove this once new development done
//                            Intent intent = new Intent(getContext(), ToDoListSteps.class);
//                            intent.putExtra(PreferenceKeys.GO_TO_PROFILE_FRAGMENT.getKey(), true);
//                            startActivity(intent);
                        }
                    } else if (StringUtils.isNotEmpty(phoneNumber)) {
                        String userId = (user != null && user.getUserDto() != null) ? user.getUserDto().getUserId() : "";
                        if (syllabusGenerated) {
                            Intent intent = new Intent(getContext(), AdminToDoListMain.class);
                            intent.putExtra("UserPhoneNumber", userId);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(getContext(), AdminEditRoutineActivity.class);
                            intent.putExtra("UserPhoneNumber", userId);
                            startActivity(intent);
                        }
                    }
                }, new VolleyGlobalErrorListener(getContext()) {
            @Override
            public void postError(VolleyError error) {
                CommonMethods.showNegativeSnackbar(parentLayout, error.getMessage());
                if (checkInRoutine) {
                    routineLoading.dismiss();
                }
            }
        });
        myJsonObjectRequest2.setTag("ProfileFragment");
        MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(myJsonObjectRequest2);

    }


    private void updateSubscriptionBadge() {
        profileSubscriptionBadge.setVisibility(View.GONE);

        //batch name
        String userBatchName = MyApplication.getStringData(PreferenceKeys.BATCH_DISPLAY_NAME.getKey());
        if (userBatchName == null || userBatchName.isBlank()) return;

        boolean isBadgeToShow = MyApplication.getFirebaseRemoteConfig().getBoolean("show_profile_badge_for_neet");

        //NEET
        if(MyApplication.isCompetitiveProfile()) {
            if (!isBadgeToShow) return;
            if(!SubscriptionNavigationHelper.hasActiveSubscriptionByBatch())  return;

            profileSubscriptionBadge.setBackgroundResource(R.drawable.bg_gradient_plus_plan);
            badgeIcon.setImageResource(R.drawable.ic_profile_star_outline);
            profileOuterRing.setVisibility(View.VISIBLE);
            profileOuterRing.setCardBackgroundColor(Color.parseColor("#4CCEDD"));
            batchName.setText(userBatchName);
            batchName.setTextColor(ContextCompat.getColor(requireContext(), R.color.profile_batch_name));
        }
        //Board
        else {
            //sub
            SubscriptionType subscriptionType = SubscriptionType.getSubscriptionType();

            //basic
            if (subscriptionType == SubscriptionType.BASIC) {
                return;
            }

            if (subscriptionType == SubscriptionType.PLUS) {
                profileSubscriptionBadge.setBackgroundResource(R.drawable.bg_gradient_plus_plan);
                profileOuterRing.setCardBackgroundColor(Color.parseColor("#4CCEDD"));
                badgeIcon.setImageResource(R.drawable.ic_profile_star_outline);
                profileSubscriptionBadge.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.profile_badge_stroke));
//                badgeIcon.setImageResource(R.drawable.ic_edit_profile_star);
                batchName.setText(userBatchName);
                batchName.setTextColor(ContextCompat.getColor(requireContext(), R.color.profile_batch_name));
            }

            if (subscriptionType == SubscriptionType.PRO) {
                profileSubscriptionBadge.setBackgroundResource(R.drawable.bg_gradient_pro_plan);
                profileOuterRing.setCardBackgroundColor(Color.parseColor("#F4C542"));
                badgeIcon.setImageResource(R.drawable.ic_profile_crown_outline);
                batchName.setText(getString(R.string.pro_plan));
                batchName.setTextColor(Color.parseColor("#F4BD2D"));
            }
        }

        profileSubscriptionBadge.setVisibility(View.VISIBLE);
    }



    private void observeForRoutine() {
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    if (snapshot.exists()) {
                        if (valueEventListener != null)
                            MyApplication.getReferenceForUserRoutine().removeEventListener(valueEventListener);

                        Intent intent = new Intent(requireActivity(), ToDoListMain.class);
//                        Intent intent = new Intent(requireActivity(), ToDoListSteps.class);
//                        intent.putExtra(PreferenceKeys.GO_TO_PROFILE_FRAGMENT.getKey(), true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    } else {
                        if (valueEventListener != null)
                            MyApplication.getReferenceForUserRoutine().removeEventListener(valueEventListener);
                        Intent intent = new Intent(requireActivity(), ToDoListMain.class);
//                        intent.putExtra(PreferenceKeys.GO_TO_PROFILE_FRAGMENT.getKey(), true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                } catch (Exception ignored) {
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        MyApplication.getReferenceForUserRoutine().addValueEventListener(valueEventListener);

    }


    private void updateImage(UserDto user) {
        if (StringUtils.isNotEmpty(user.getImageUrl())) {
            MyApplication.saveData(PreferenceKeys.PROFILE_ICON.getKey(), user.getImageUrl());
            if (getActivity() != null && !getActivity().isDestroyed())
                Glide.with(this).load(user.getImageUrl()).into(profileIcon);
        }
    }

    @Override
    public void onDestroy() {
        if (getActivity() != null) {
            MyVolleySingleton.getInstance(getActivity()).getRequestQueue().cancelAll("ProfileFragment");
        }
        super.onDestroy();

    }

    @Override
    public void backPressed() {

    }


    public void hideProfileItems() {
        try {
            String batchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
            String subType   = SubscriptionType.getSubscriptionType().name();

            Log.d(TAG, "hideProfileItemsForBoard: starting | subType=" + subType + " | batchCode=" + batchCode);

            String configJson = MyApplication.getFirebaseRemoteConfig().getString("hide_profile_items_v3");

            if (configJson == null || configJson.trim().isEmpty()) {
                Log.w(TAG, "hideProfileItemsForBoard: config is null or empty for key=hide_profile_items_v3");
                return;
            }

            Log.d(TAG, "hideProfileItemsForBoard: config fetched successfully | json=" + configJson);

            JSONObject root = new JSONObject(configJson);

            hideItemsForBlock(root, subType, batchCode);
            hideItemsForBlock(root, "ALL", batchCode);

        } catch (Exception ex) {
            Log.e(TAG, "hideProfileItemsForBoard: unexpected error | " + ex.getMessage(), ex);
        }
    }

    private void hideItemsForBlock(JSONObject root, String blockKey, String currentBatch) {
        try {
            if (!root.has(blockKey)) {
                Log.d(TAG, "hideItemsForBlock: no block found for key=" + blockKey + " | skipping");
                return;
            }

            JSONObject block = root.getJSONObject(blockKey);
            Log.d(TAG, "hideItemsForBlock: processing block=" + blockKey + " | currentBatch=" + currentBatch);

            Iterator<String> profileItems = block.keys();
            while (profileItems.hasNext()) {
                String profileItem = profileItems.next();
                String batchCodes  = block.getString(profileItem);

                if (batchCodes.equals("ALL")) {
                    Log.d(TAG, "hideItemsForBlock: hiding item=" + profileItem + " | reason=batch is ALL");
                    hideProfileItem(profileItem);
                } else if (Arrays.asList(batchCodes.split("::")).contains(currentBatch)) {
                    Log.d(TAG, "hideItemsForBlock: hiding item=" + profileItem + " | reason=batchCode matched | batchCodes=" + batchCodes);
                    hideProfileItem(profileItem);
                } else {
                    Log.d(TAG, "hideItemsForBlock: skipping item=" + profileItem + " | currentBatch=" + currentBatch + " not in batchCodes=" + batchCodes);
                }
            }

        } catch (JSONException e) {
            Log.e(TAG, "hideItemsForBlock: JSON error in block=" + blockKey + " | " + e.getMessage(), e);
        }
    }

    private void hideProfileItem(String itemName) {
        try {
            ProfileItems item = ProfileItems.valueOf(itemName.trim());
            switch (item) {
                case SUBSCRIPTION_PLAN:
                    subscriptionPlan.setVisibility(View.GONE);
                    hideSubscriptionPlan=true;
                break;
                case MY_SUBSCRIPTION:   mock_test.setVisibility(View.GONE);        break;
                case LANGUAGE:          language.setVisibility(View.GONE);         break;
                case MY_DOWNLOADS:      myDownload.setVisibility(View.GONE);       break;
                case SCHOLARSHIP_TEST:  rl_competition.setVisibility(View.GONE);   break;
                case RATE_THE_APP:      rl_app_review.setVisibility(View.GONE);    break;
                case ANALYTICS:         analytics.setVisibility(View.GONE);        break;
                case NOTIFICATION:      notification.setVisibility(View.GONE);     break;
                case YOUR_ROUTINE:      routine.setVisibility(View.GONE);          break;
                case SHORT_NOTES:       quick_notes.setVisibility(View.GONE);      break;
                case RANKING:           leaderboard.setVisibility(View.GONE);      break;
                case MY_DOUBTS:         current_doubt.setVisibility(View.GONE);    break;
                case CONTACT_US:        contact.setVisibility(View.GONE);          break;
            }
            Log.d(TAG, "hideProfileItem: successfully hid item=" + itemName);

        } catch (IllegalArgumentException e) {
            Log.w(TAG, "hideProfileItem: unknown item name in config | itemName=" + itemName + " | skipping");
        }
    }

    public void showPhoneNumberDialog() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View dialogView = inflater.inflate(R.layout.phone_number_entry, null);
        final EditText editTextPhone = dialogView.findViewById(R.id.editTextPhone);

        new AlertDialog.Builder(getActivity())
                .setView(dialogView) // Set the custom layout
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String phoneNumber = editTextPhone.getText().toString();
                        if (phoneNumber.isEmpty()) {
                            Toast.makeText(getContext(), "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
                        } else {
                            getAPIDataByPhoneData(true, phoneNumber);
                        }
                    }
                })
                .setNegativeButton("Cancel", null) // Do nothing on Cancel
                .show();
    }


    private boolean isOrderHistoryEligible() {
        try {
            String batchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
            if (batchCode == null || batchCode.isEmpty()) {
                Log.d(TAG, "isOrderHistoryEligible: batchCode is null or empty");
                return false;
            }

            // Get eligible batch codes from Remote Config
            String eligibleBatchCodes = MyApplication.getFirebaseRemoteConfig().getString("order_history_eligible_batch_codes");

            if (eligibleBatchCodes == null || eligibleBatchCodes.trim().isEmpty()) {
                Log.w(TAG, "isOrderHistoryEligible: Remote Config key 'order_history_eligible_batch_codes' is empty");
                return false;
            }

            // Check if current batch code is in the eligible list (separated by ::)
            boolean isEligible = Arrays.asList(eligibleBatchCodes.split("::")).contains(batchCode);
            Log.d(TAG, "isOrderHistoryEligible: batchCode=" + batchCode + " | isEligible=" + isEligible);

            return isEligible && SubscriptionNavigationHelper.hasActiveSubscriptionByBatch();
        } catch (Exception e) {
            Log.e(TAG, "isOrderHistoryEligible: error | " + e.getMessage(), e);
            return false;
        }
    }
}
