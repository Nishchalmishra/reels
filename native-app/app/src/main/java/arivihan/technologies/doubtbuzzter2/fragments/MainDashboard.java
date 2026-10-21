package arivihan.technologies.doubtbuzzter2.fragments;

import static arivihan.technologies.doubtbuzzter2.activity.MicrolectureListActivity.NOTIFICATION_PERMISSION_REQUEST_CODE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.CONFIGURATION;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_HOME_SCREEN_CARDS;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_DOUBT_MICROLECTURE_CONTENT;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_EXAM_UI_DETAILS;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_DASHBOARD_VIDEO;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_GET_USER_BY_PHONE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.PUT_UPDATE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.SWITCH_TO_TARGET_COURSE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_SYNC_DOWNLOADED_LECTURE_DATA;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.denzcoskun.imageslider.models.SlideModel;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.DoubtChatScreen;
import arivihan.technologies.doubtbuzzter2.activity.MyDownloadActivity;
import arivihan.technologies.doubtbuzzter2.activity.TestResultActivity;
import arivihan.technologies.doubtbuzzter2.ReelsActivity;
import arivihan.technologies.doubtbuzzter2.activity.ChatScreenChooseSubjectForChat;
import arivihan.technologies.doubtbuzzter2.activity.ExoPlayerActivity;
import arivihan.technologies.doubtbuzzter2.activity.GlobalSearchActivity;
import arivihan.technologies.doubtbuzzter2.activity.HomeScreen;
import arivihan.technologies.doubtbuzzter2.activity.QuickNotesActivity;
import arivihan.technologies.doubtbuzzter2.activity.ToppersNotesActivity;
import arivihan.technologies.doubtbuzzter2.common.ParikshaAdhyayanReferralDialog;
import arivihan.technologies.doubtbuzzter2.toppernotes.presentation.TopperNotesActivity;
import arivihan.technologies.doubtbuzzter2.activity.VernacularActivity;
import arivihan.technologies.doubtbuzzter2.activity.VideoPlayerActivity;
import arivihan.technologies.doubtbuzzter2.activity.competitive.subject.chapter.ChapterListByBatchActivity;
import arivihan.technologies.doubtbuzzter2.activity.competitive.subject.chapter.ChapterListSubscribedActivity;
import arivihan.technologies.doubtbuzzter2.adapter.ClassesLiveAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.HighlistAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.ReleaseNoteAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.SortSubjectNameAnalyticAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.SubjectNameAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.SubjectProgressAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.TestSeriesAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.ToppersAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.VernacularGridAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.competitive.FeaturesSectionAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.common.NameInputActivity;
import arivihan.technologies.doubtbuzzter2.common.PosterPopUp;
import arivihan.technologies.doubtbuzzter2.common.datetime.DateTimeUtils;
import arivihan.technologies.doubtbuzzter2.constants.UrlRoutes;
import arivihan.technologies.doubtbuzzter2.customView.DialogListener;
import arivihan.technologies.doubtbuzzter2.customView.RegisterCardView;
import arivihan.technologies.doubtbuzzter2.enums.ChapterEnum.ChapterType;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.customView.CustomBottomStatusView;
import arivihan.technologies.doubtbuzzter2.customView.HowToVideoView;
import arivihan.technologies.doubtbuzzter2.enums.batch.videos.BatchVideoType;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceMethods;
import arivihan.technologies.doubtbuzzter2.model.OnBoardingSelectionModel;
import arivihan.technologies.doubtbuzzter2.enums.batch.videos.VideoOrientationType;
import arivihan.technologies.doubtbuzzter2.helper.DeviceInfoUtil;
import arivihan.technologies.doubtbuzzter2.helper.DottedUnderlineSpan;
import arivihan.technologies.doubtbuzzter2.helper.EnvironmentManager;
import arivihan.technologies.doubtbuzzter2.helper.RemoteConfigUtils;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.helper.UserNameValidationConfig;
import arivihan.technologies.doubtbuzzter2.helper.WrapContentLinearLayoutManager;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionType;
import arivihan.technologies.doubtbuzzter2.listener.ApiCallBackListener;
import arivihan.technologies.doubtbuzzter2.model.UserDto;
import arivihan.technologies.doubtbuzzter2.model.carousel.CarouselResponse;
import arivihan.technologies.doubtbuzzter2.model.ClassesLiveModel;
import arivihan.technologies.doubtbuzzter2.model.Configuration;
import arivihan.technologies.doubtbuzzter2.model.ConfigurationResult;
import arivihan.technologies.doubtbuzzter2.database.helper.MediaMapper;
import arivihan.technologies.doubtbuzzter2.model.DownloadMedia;
import arivihan.technologies.doubtbuzzter2.model.DownloadedLectureResponse;
import arivihan.technologies.doubtbuzzter2.model.SyncDownloadRequest;
import arivihan.technologies.doubtbuzzter2.model.HighlistModel;
import arivihan.technologies.doubtbuzzter2.model.VideoResponse;
import arivihan.technologies.doubtbuzzter2.model.HomeScreenCacheData;
import arivihan.technologies.doubtbuzzter2.model.batch.home.ExamUiDataResponse;
import arivihan.technologies.doubtbuzzter2.model.MediaEntity;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureContentDto;
import arivihan.technologies.doubtbuzzter2.model.NameInputDto;
import arivihan.technologies.doubtbuzzter2.model.StudentProgressDto;
import arivihan.technologies.doubtbuzzter2.customView.OptionalSubjectBottomSheet;
import arivihan.technologies.doubtbuzzter2.model.subject.SubjectResponse;
import arivihan.technologies.doubtbuzzter2.model.SubjectProgressDto;
import arivihan.technologies.doubtbuzzter2.model.SwitchUserClassCourseRequestDto;
import arivihan.technologies.doubtbuzzter2.model.batch.test_series.TestSeriesCard;
import arivihan.technologies.doubtbuzzter2.model.batch.test_series.TestSeriesCardResponse;
import arivihan.technologies.doubtbuzzter2.model.ToppersHomeScreenModel;
import arivihan.technologies.doubtbuzzter2.model.batch.user.UserDetailsResponse;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.model.VernacularItem;
import arivihan.technologies.doubtbuzzter2.model.call_me_back.CallMeBackModel;
import arivihan.technologies.doubtbuzzter2.model.competitive.HomeScreenCardResponse;
import arivihan.technologies.doubtbuzzter2.model.remote_config.ActivityNavigationConfig;
import arivihan.technologies.doubtbuzzter2.utils.LoaderOverlay;
import arivihan.technologies.doubtbuzzter2.utils.RecyclerViewEdgeEffect;
import arivihan.technologies.doubtbuzzter2.viewModel.HomeScreenViewModel;
import arivihan.technologies.doubtbuzzter2.viewModel.MediaViewModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class MainDashboard extends BaseFragment implements ToppersAdapter.OnTopperClickListener, HighlistAdapter.OnHighlightClick,
        SubjectNameAdapter.OnSubjectClick, ClassesLiveAdapter.OnClassesClick, SubjectProgressAdapter.OnProgressClick, VernacularGridAdapter.VernacularAction, SortSubjectNameAnalyticAdapter.OnSubjectSortClick,
        TestSeriesAdapter.TestSeriesSetClickListener, TodayTaskFragment.TodayTaskListener,
        RegisterCardView.MainDashBoardListener, PosterPopUp.OnDismissListener,
        FeaturesSectionAdapter.OnFeatureClickListener {
    private static final String TAG = MainDashboard.class.getSimpleName();
    private static final String PARIKSHA_ADHYAYAN_FEATURE_TAG = "PARIKSHA_ADHYAYAN";
    private boolean lastKnownState = false;
    NetworkStatusLiveData networkStatusLiveData;
    ShimmerFrameLayout headerShimmer;
    ImageView  release_notes, globalSearch;
    CardView queries, refer;
    String selectedBoard, selectedBoardName;
    UserDetailsResponse userDetailsResponse;
    ExamUiDataResponse examUiDataResponse;
    ImageView welcome_icon, footerImage;
    LinearLayout footerImageLayout;
    LinearLayout linearLayoutScrollView, globalSearchButton;
    CardView cv_subject_left_scroll, cv_subject_right_scroll;
    TextView view_details, user_name, your_Progress, searchTextStart, searchTextEnd;
    Dialog dialog, selectSubject;
    ScrollView scrollView;
    ProgressBar progressBar, progressBarX;
    MaterialCardView fab_menu;
    private RelativeLayout ask_relative;
    private MicroLectureContentDto microLectureContentDto;
    Boolean clicked = false;
    Animation toBottom, fromBottom, rotateOpen, rotateClose, leftRight, Rightleft;
    String Reference_Code = "";
    Dialog dialogHome;
    Gson gson = new Gson();
    boolean progressApiLoaded = false;
    TextView testSeries;
    int timer = 4;
    public int appear = 0;
    ArrayList<String> titles = new ArrayList<>();
    CardView classes_in_progress;
    ConstraintLayout constraintLayout;
    ShimmerFrameLayout shimmerSubjects, shimmerFeatureSection, shimmerTestSeries, shimmerClasses,
            shimmerHighlights, carouselShimmer, toppersShimmer;
    RecyclerView recyclerViewToppers;
    RecyclerView recyclerViewSub;
    RecyclerView recyclerViewClasses;
    RecyclerView recyclerViewTextSeries;
    RecyclerView recyclerViewhighlight;
    TextSwitcher searchTextSwitcher;
    ArrayList<SlideModel> imageList = new ArrayList<>();
    FrameLayout img_slider;

    private boolean onCreateCalled;
    private OnFragmentInteractionListener mListener;
    boolean preExamNonBoardData;
    CardView toopersCardLayout;
    private CardView cv_subjects;

    RecyclerView recyclerViewSubjectProgress;
    private List<SubjectResponse> sub = new ArrayList<>();
    private List<SubjectProgressDto> subjectProgressDtos = new ArrayList<>();
    private List<ClassesLiveModel> classeslive = new ArrayList<>();
    private List<TestSeriesCard> testseries = new ArrayList<>();
    private List<HighlistModel> highlistModelList = new ArrayList<>();
    public int dataLoaded = 0;
    private IntentFilter filter;
    public static final String KEY_VIDEO_URI = "video_uri";
    private Context mContext;
    ArrayList<String> searchOptionList;
    Dialog boardDialog;
    RecyclerView vernacularRecyler;
    VernacularGridAdapter vernacularGridAdapter;
    private ArrayList<VernacularItem> languageList = new ArrayList<>();
    private ProgressDialog loading;
    private Dialog progressDialog;
    private LottieAnimationView lottieAnimationView, clickAskBtnLottieAnim;
    RecyclerView recyclerViewReleaseNotes;
    ReleaseNoteAdapter releaseNoteAdapter;
    SubjectNameAdapter subjectnameAdapter;
    ToppersAdapter toppersAdapter;
    TestSeriesAdapter testSeriesAdapter;
    HighlistAdapter highlistAdapter;
    ClassesLiveAdapter classesLiveAdapter;
    DataBack mDataBack;
    int searchTextAnimationPosition = 0;

    HomeScreenViewModel homeScreenViewModel;
    private boolean hideContinue;
    FrameLayout todayTaskContainer;
    private TodayTaskFragment todayTaskFragment;
    TextView subjects;
    boolean preExamDataAvailable = false;

    ArrayList<ToppersHomeScreenModel> toppersList = new ArrayList<>();

    ImageView edit_your_name;

    private Handler toolTipHandler;
    private Runnable showTooltipRunnable;
    private Runnable hideTooltipRunnable;
    private Handler searchTextHandler;
    private Runnable searchTextRunnable;

    DatabaseReference databaseReference3;
    int totalTestsCount;
    private LinearLayout contentLayout;
    private CustomBottomStatusView customBottomStatusView;
    private View home_screen_status_card_layout, custom_home_screen_secondary_layout;
    DatabaseReference databaseReference;
    private ImageView how_to_poster, headerBackgroundImage;
    private String posterUrl;
    private TextView highlightsText;

    private String selectedCourseId;
    private TextView toppersLayoutTitle, toppersLayoutSubTitle;
    private MediaViewModel mediaViewModel;
    private Dialog noInternetDialog;
    private MainDashboardListener mainDashboardListener;
    private RegisterCardView registerCardView;
    private CardView cv_test_card;
    private LinearLayout ll_header_name_layout;

    private TextView tvSwitchLabel;
    private boolean isSwitchInProgress = false;

    private SwitchUserClassCourseRequestDto switchableCourseClassInfo;

    // Track current mode

    boolean isNeetMode = false;


    //All Feature Sections
    private static final int MAX_FEATURE_ANIM_COUNT = 4;
    private FeaturesSectionAdapter featuresSectionAdapter;
    private List<HomeScreenCardResponse> featureSectionModelsList = new ArrayList<>();
    RecyclerView featureSectionRv;
    CardView cv_feature_left_scroll, cv_feature_right_scroll;
    private boolean isFeatureSectionLoading = false;
    private CardView cv_join_button_right;
    private AnimatorSet pulseAnimator;
    private TextView tv_join_button_right;
    private boolean todayTaskFetched, allFeaturesFetched;

    private TextView chipBoard, chipStream, chipClass;
    private MaterialCardView cardChipBoard, cardChipStream, cardChipClass;
    private LinearLayout llChipsRow;

    @Override
    public void onStart() {
        super.onStart();
    }

    public void setObserver() {
        fetchHomeScreenDataFromCache();
        getAPIDataByPhoneData();
        getSubjectData();
        highlightExtraction();
        toppersExtraction();
        getCarouselData();
        textSeriesExtraction();
        getAllFeatureSectionVisibilityCheckFromCache();
        getStudentProgress();
        getRoutineDataCheckStatus();
        getFeaturesSectionData();
        homeScreenViewModel.getBatchCodeRefreshTrigger().observe(getViewLifecycleOwner(), shouldRefresh -> {
            if (!Boolean.TRUE.equals(shouldRefresh) || !isAdded()) return;
            isFeatureSectionLoading = false;
            featureSectionModelsList.clear();
            if (featuresSectionAdapter != null) featuresSectionAdapter.notifyDataSetChanged();
            getFeaturesSectionData();
        });
    }

    private void updateFeatureSectionArrowVisibility() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) featureSectionRv.getLayoutManager();
        if (layoutManager == null || featureSectionRv.getAdapter() == null) return;

        int firstVisible = layoutManager.findFirstCompletelyVisibleItemPosition();
        int lastVisible = layoutManager.findLastCompletelyVisibleItemPosition();
        int total = featureSectionRv.getAdapter().getItemCount();

        cv_feature_left_scroll.setVisibility(firstVisible > 0 ? View.VISIBLE : View.GONE);
        cv_feature_right_scroll.setVisibility(lastVisible < total - 1 ? View.VISIBLE : View.GONE);
    }

    void reload() {
        shimmerSubjects.startShimmer();
        shimmerSubjects.setVisibility(View.VISIBLE);
        shimmerClasses.startShimmer();
        shimmerClasses.setVisibility(View.VISIBLE);
        recyclerViewSub.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void loadTodayTaskFragment() {

        String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        String classId = MyApplication.getStringData(PreferenceKeys.CLASS_ID.getKey());
        String courseId = MyApplication.getStringData(PreferenceKeys.SELECTED_COURSE_ID_LOCAL.getKey());

        boolean isVisible = RemoteConfigUtils.isFeatureVisible(
                RemoteConfigUtils.FeatureVisibilityName.TODAY_CLASS.getFeature()
        );
        MyApplication.saveData(HomeScreenCacheData.AV_TODAYS_CLASSES.name(), isVisible);
        if (!isVisible) {
            todayTaskContainer.setVisibility(View.GONE);
            return;
        }
        showTodayTaskFragment();
    }

    private void showTodayTaskFragment() {
        if (!isAdded() || todayTaskContainer == null) {
            return;
        }
        todayTaskContainer.setVisibility(View.VISIBLE);
        // Reset so playSequentialBorderAnimation() gates until the new fragment
        // delivers onDataFetched() after loading from cache or API.
        todayTaskFetched = false;
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        todayTaskFragment = new TodayTaskFragment(this, TAG);
        fragmentTransaction.replace(R.id.today_task_container, todayTaskFragment);
        fragmentTransaction.commit();
    }


    @Override
    public void onPause() {
        super.onPause();
        try {
            onCreateCalled = false;
            MyVolleySingleton.getInstance(getContext()).getRequestQueue().cancelAll(TAG);
        } catch (Exception e) {

        }
        if (customBottomStatusView != null) {
            customBottomStatusView.dismissListener();
            customBottomStatusView = null;
        }
        if (loading != null) {
            loading.cancel();
        }

        if (dialog != null) {
            dialog.dismiss();
        }

        if (toolTipHandler != null)
            toolTipHandler.removeCallbacksAndMessages(null);

        if (searchTextHandler != null)
            searchTextHandler.removeCallbacksAndMessages(null);

        if (featuresSectionAdapter != null && featureSectionRv != null) {
            featuresSectionAdapter.stopCurrentAnimationIfBound(featureSectionRv);
        }

    }

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_main_dashboard, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EdgeToEdgeHelper.padForSystemBars(view.findViewById(R.id.linear_layout_scrollView));
        Log.d("getSubscribedSubjectData", "onCreate");
        MyApplication.saveData(PreferenceKeys.SHOW_MATCH_THESE_DIALOG.getKey(), true);

//        FirebaseMessaging.getInstance().getToken()
//                .addOnCompleteListener(task -> {
//                    if (!task.isSuccessful()) {
//                        Log.w("FIREBASE_MESSAGING_TOKEN", "Fetching FCM registration token failed", task.getException());
//                        return;
//                    }
//
//                    // Get new FCM registration token
//                    String token = task.getResult();
//
//                    // Log and use the token
//                    Log.i("FIREBASE_MESSAGING_TOKEN", "TOKEN :: " + token);
//                });

        MyApplication.saveData(PreferenceKeys.BOARD_SWITCH.getKey(), true);
        selectedCourseId = MyApplication.getStringData(PreferenceKeys.SECONDARY_COURSE_ID.getKey());
        Log.d("TAG", "CourseId: " + MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey()) + ", Secondary: " + MyApplication.getStringData(PreferenceKeys.SECONDARY_COURSE_ID.getKey()));
        MyApplication.saveData(PreferenceKeys.SELECTED_COURSE_ID_LOCAL.getKey(), selectedCourseId);

        headerShimmer = view.findViewById(R.id.header_shimmer);

        how_to_poster = view.findViewById(R.id.how_to_poster);
        cv_join_button_right = view.findViewById(R.id.cv_join_button_right);
        cardChipBoard = view.findViewById(R.id.card_chip_board);
        cardChipStream = view.findViewById(R.id.card_chip_stream);
        cardChipClass = view.findViewById(R.id.card_chip_class);

        chipBoard = view.findViewById(R.id.chip_board);
        chipStream = view.findViewById(R.id.chip_stream);
        chipClass = view.findViewById(R.id.chip_class);

        llChipsRow = view.findViewById(R.id.ll_chips_row);

        startPulseAnimation();

        tv_join_button_right = view.findViewById(R.id.tv_join_button_right);
        home_screen_status_card_layout = view.findViewById(R.id.home_screen_status_card_layout);
        registerCardView = view.findViewById(R.id.register_card);
        contentLayout = view.findViewById(R.id.content_layout);
        edit_your_name = view.findViewById(R.id.edit_your_name);

        ll_header_name_layout = view.findViewById(R.id.ll_header_name_layout);
        headerShimmer = view.findViewById(R.id.header_shimmer);

        tvSwitchLabel = view.findViewById(R.id.tv_switch_label);

// Initial state from backend
        applyToggleState(isNeetMode);
        edit_your_name.setOnClickListener(view3 -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.hs_name_pencil_icon_click.getActionName());
            showInputDialog(requireActivity());
        });


        cv_join_button_right.setOnClickListener(v -> {
//            Intent intent = new Intent(getActivity(), CampaignSubscriptionActivity.class);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.hs_campaign_Join_now_clicked.getActionName());
            destroyHowTo();
//            startActivity(intent);
            SubscriptionNavigationHelper.navigateToSubscriptionPage(getActivity(), RedirectionSource.HOME_SCREEN_JOIN_NOW);
        });

        cv_feature_left_scroll = view.findViewById(R.id.cv_feature_left_scroll);
        cv_feature_right_scroll = view.findViewById(R.id.cv_feature_right_scroll);

        progressBar = view.findViewById(R.id.progress_dataLoading);
        scrollView = view.findViewById(R.id.scroll_bar);
        your_Progress = view.findViewById(R.id.your_progress);
        constraintLayout = view.findViewById(R.id.pdflayout);
        testSeries = view.findViewById(R.id.test_series);
        img_slider = view.findViewById(R.id.image_sliders);
        welcome_icon = view.findViewById(R.id.welcome_icon);
        footerImageLayout = view.findViewById(R.id.footer_board_image);
        footerImage = view.findViewById(R.id.home_screen_footer_image);
        searchTextSwitcher = view.findViewById(R.id.search_text_switcher);
        searchTextStart = view.findViewById(R.id.txt_search_start);
        searchTextEnd = view.findViewById(R.id.txt_search_end);
        globalSearchButton = view.findViewById(R.id.global_search_button);
        carouselShimmer = view.findViewById(R.id.carousel_shimmer);
        toppersShimmer = view.findViewById(R.id.shimmer_toppers);
        clickAskBtnLottieAnim = view.findViewById(R.id.click_ask_anim);
        toopersCardLayout = view.findViewById(R.id.topper_card);
        headerBackgroundImage = view.findViewById(R.id.header_background_image);
        highlightsText = view.findViewById(R.id.highlights);
        cv_subjects = view.findViewById(R.id.cv_subjects);

        toppersLayoutTitle = view.findViewById(R.id.toppers_title);
        toppersLayoutSubTitle = view.findViewById(R.id.toppers_sub_title);
        cv_test_card = view.findViewById(R.id.test_card);

        noInternetDialog = CommonMethods.getOfflineNoInternetDialog(requireContext(), new DialogListener() {
            @Override
            public void buttonACallback() {
                Intent intent = new Intent(getActivity(), MyDownloadActivity.class);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.dialog_no_internet_my_download_clicked.getActionName());
                startActivity(intent);
            }

            @Override
            public void buttonBCallback() {
            }
        });

        registerCardView.setMainDashboardListener(this);

        fab_menu = view.findViewById(R.id.cv_ask_doubt);
        ask_relative = view.findViewById(R.id.ask_relative);
        linearLayoutScrollView = view.findViewById(R.id.linear_layout_scrollView);
        classes_in_progress = view.findViewById(R.id.classes_in_progress_card);
        user_name = view.findViewById(R.id.user_name);
        queries = view.findViewById(R.id.Queries);
        view_details = view.findViewById(R.id.view_details);
        release_notes = view.findViewById(R.id.release_notes);
        refer = view.findViewById(R.id.Refer_and_earn);
        globalSearch = view.findViewById(R.id.search);
        todayTaskContainer = view.findViewById(R.id.today_task_container);

        View.OnClickListener onAcademicDetailsClick = v -> {
            boolean isSubscribed = SubscriptionNavigationHelper.hasActiveSubscriptionByBatch();
            boolean isEditable = examUiDataResponse != null && Boolean.TRUE.equals(examUiDataResponse.getProfileEditable());
            
            if (!isSubscribed && isEditable) {
                // For greeting name, only navigate if name is already entered
                if (v.getId() == R.id.user_name) {
                    boolean nameIsEmpty = StringUtils.isEmpty(MyApplication.getStringData(PreferenceKeys.NAME.getKey()));
                    if (nameIsEmpty) return;
                }
                Intent intent = new Intent(requireActivity(), arivihan.technologies.doubtbuzzter2.activity.profile.AcademicDetailsActivity.class);
                if (examUiDataResponse != null && examUiDataResponse.getExamId() != null) {
                    intent.putExtra("examId", examUiDataResponse.getExamId());
                }
                startActivity(intent);
            }
        };

        user_name.setOnClickListener(onAcademicDetailsClick);
        welcome_icon.setOnClickListener(onAcademicDetailsClick);
        cardChipBoard.setOnClickListener(onAcademicDetailsClick);
        cardChipStream.setOnClickListener(onAcademicDetailsClick);
        cardChipClass.setOnClickListener(onAcademicDetailsClick);
        llChipsRow.setOnClickListener(onAcademicDetailsClick);

        cv_subject_left_scroll = view.findViewById(R.id.cv_subject_left_scroll);
        cv_subject_right_scroll = view.findViewById(R.id.cv_subject_right_scroll);

        cv_subject_left_scroll.setVisibility(View.GONE);
        cv_subject_right_scroll.setVisibility(View.GONE);

        // Animation---------
        rotateOpen = AnimationUtils.loadAnimation(getContext(), R.anim.rotateopen);
        rotateClose = AnimationUtils.loadAnimation(getContext(), R.anim.rotateclose);
        toBottom = AnimationUtils.loadAnimation(getContext(), R.anim.tobottom);
        fromBottom = AnimationUtils.loadAnimation(getContext(), R.anim.frombottom);
        Rightleft = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_left);
        leftRight = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_right);

        selectSubject = new AppCompatDialog(requireContext()); // new Dialog(getActivity());
        selectSubject.setContentView(R.layout.custom_dialogue_subjectselection);
        if (selectSubject.getWindow() != null) {
            selectSubject.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
            selectSubject.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        loading = new ProgressDialog(getContext());
        loading.setCancelable(true);
        loading.setMessage(getString(R.string.loading));
        loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        progressDialog = new Dialog(getContext());
        progressDialog.setContentView(R.layout.arivihan_dialog);
        if (progressDialog.getWindow() != null) {
            progressDialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(getContext(), R.drawable.bachground_custom_dialogu_typeactivity));
            progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        lottieAnimationView = progressDialog.findViewById(R.id.lottie);
        lottieAnimationView.setAnimation(R.raw.edit_routine_lottie_star);

        MyApplication myapp = (MyApplication) getActivity().getApplication();
        homeScreenViewModel = myapp.getHomeScreenViewModel();
        homeScreenViewModel.getCarouselData(false);
        homeScreenViewModel.getSubscribedSubjectData(false);
        homeScreenViewModel.getTestSeriesData(false);
        homeScreenViewModel.getStudentProgressData(false);

//        joinCampaignButton.setOnClickListener(view1 -> {
//            Intent intent = new Intent(getActivity(), CampaignSubscriptionActivity.class);
//            MyApplication.logFirebaseBundleCreation(FirebaseEvents.Campaign_Join_now_clicked.getActionName());
//            destroyHowTo();
//            startActivity(intent);
//            SubscriptionNavigationHelper.navigateToSubscriptionPage(getActivity());
//        });

        scrollView.setVerticalScrollBarEnabled(false);

        globalSearchButton.setVisibility(RemoteConfigUtils.isFeatureVisible(RemoteConfigUtils.FeatureVisibilityName.GLOBAL_SEARCH.getFeature()) ? View.VISIBLE : View.GONE);

        globalSearchButton.setOnClickListener(ev -> {
            Intent intent = new Intent(getActivity(), GlobalSearchActivity.class);
            destroyHowTo();
            startActivity(intent);
        });

        try {
            handleCourseSwitchSelection();
        } catch (Exception e) {
            Log.e("Error", e.getMessage(), e);
        }
        globalSearch.setOnClickListener(view13 -> {
            Intent intent = new Intent(getActivity(), GlobalSearchActivity.class);
            destroyHowTo();
            startActivity(intent);
        });

        scrollView.setOnScrollChangeListener((view16, i, i1, i2, i3) -> {
            if (i < 120 && i1 < 120 && i2 < 120 && i3 < 120) {
                appear = 0;
                setFabMenuVisibility(true);
                clickAskBtnLottieAnim.setVisibility(View.VISIBLE);
            } else {
                if (appear == 0) {
                    clickAskBtnLottieAnim.setVisibility(View.GONE);
                    setFabMenuVisibility(false);
                    clicked = false;
                }
                appear = 1;
            }
        });

        // Initialize Shimmer frames
        initializeAllShimmerFrames(view);

        //Initialize All Recyclerview
        initializeAllRecyclerView(view);

        // Initialize All Adapters
        initializeAllAdapters();

        setFabMenuVisibility(true);

        linearLayoutScrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            Rect scrollBounds = new Rect();
            scrollView.getHitRect(scrollBounds);
            highlistAdapter.handleRecyclerVisibility(recyclerViewhighlight.getLocalVisibleRect(scrollBounds));
        });

        setBoardDialog();

        getAndUpdateVideoUrl();
        getDoubtVideoContent(null);

        refer.setOnClickListener(view110 -> {
            Intent intent = new Intent(getActivity(), QuickNotesActivity.class);
            destroyHowTo();
            startActivity(intent);
        });

        view_details.setOnClickListener(view19 -> {
            HomeScreen.Analytic_fragment_clicked afc = new HomeScreen.Analytic_fragment_clicked();
            afc.analytic_frag();
            setFragments(new AnalyticFragment());
        });

        fab_menu.setOnClickListener(view17 -> {
            try {
                boolean skipSubjectSelection = MyApplication.getFirebaseRemoteConfig().
                        getBoolean("doubt_chat_web_web_view_skip_subject_selection_" + EnvironmentManager.getEnvironmentNameByUrl(MyApplication.getEndPoint()).toLowerCase());

                if (skipSubjectSelection) {
                    Map<String, String> params = new HashMap<>();
                    params.put("subject", "");
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.ask_doubt_subject_clicked.getActionName(), params);
                    destroyHowTo();
                    CommonNavigator.navigateToDoubtChatWithSubject(getActivity(), "", "", "", true);
                } else {
                    mDataBack.isClicked(true);
                    hideAskDoubtToBottomRight();
                    Intent intent = new Intent(getActivity(), ChatScreenChooseSubjectForChat.class);
                    intent.putExtra("subscriptionActive", true);
                    destroyHowTo();
                    startActivity(intent);
                    requireActivity().overridePendingTransition(R.anim.slide_in_from_right_switch, R.anim.slide_out_to_left_switch);
                    if (!clicked) clicked = true;
                    else clicked = false;
                    HashMap<String, String> eventParams = new HashMap<>();
                    eventParams.put("source", TAG);
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.click_ask_doubt.getActionName(), eventParams);
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.hs_click_ask_doubt.getActionName(), eventParams);
                }


            } catch (Exception e) {
                Log.d("onViewCreated: ", e.getMessage());
            }
        });


        if (BuildConfig.DEBUG) {
            release_notes.setVisibility(View.VISIBLE);
        }

        Dialog release_dialog = new Dialog(mContext);
        release_dialog.setContentView(R.layout.activity_release_notes);
        if (release_dialog.getWindow() != null) {
            release_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        recyclerViewReleaseNotes = release_dialog.findViewById(R.id.rv_release_notes);
        recyclerViewReleaseNotes.setLayoutManager(new WrapContentLinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        releaseNoteAdapter = new ReleaseNoteAdapter(Arrays.asList(getResources().getStringArray(R.array.release_notes)), mContext);
        recyclerViewReleaseNotes.setAdapter(releaseNoteAdapter);

        release_notes.setOnClickListener(view111 -> release_dialog.show());

        dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_doubt_picture_click);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        dialog.setCancelable(false);


        LinearLayout linearLayout2 = dialog.findViewById(R.id.linear_layout);
        CardView cut2 = dialog.findViewById(R.id.cancel);
        TextView subtitle = dialog.findViewById(R.id.doubt_text);
        TextView title = dialog.findViewById(R.id.text);


        title.setText("Subscription Required");
        subtitle.setText("Please subscribe your plan to open this Lecture \n");
        linearLayout2.setVisibility(View.GONE);

        cut2.setOnClickListener(v -> dialog.dismiss());

        networkStatusLiveData = new NetworkStatusLiveData(requireContext());
        networkStatusLiveData.observe(getViewLifecycleOwner(), isOnline -> {

            if (isOnline != lastKnownState) {
                lastKnownState = isOnline;

                if (isOnline) {
                    Log.d("NETWORK_STATUS", "Status :: " + isOnline);

                    if (dataLoaded == 0) {
                        reloadData();
                    }

                    if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                        if (noInternetDialog != null && noInternetDialog.isShowing()) {
                            noInternetDialog.dismiss();
                        }
                        CommonMethods.showPositiveSnackbar(constraintLayout, MyApplication.getInstance().getString(R.string.InternetConnection));
                        MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                        MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
                    }
                } else {
                    Log.d("NETWORK_STATUS", "Status :: " + isOnline);
                    if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                        if (noInternetDialog != null && !noInternetDialog.isShowing()) {
                            noInternetDialog.show();
                        }
                        CommonMethods.showNegativeSnackbar(constraintLayout, MyApplication.getInstance().getString(R.string.NoInternetConnection));
                        MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), false);
                        MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), true);
                    }
                }
            }
        });


        if (CommonMethods.isOnline(getActivity())) {
            if (dataLoaded == 0) {
                MyApplication.saveData(PreferenceKeys.PROGRESS_RELOAD.getKey(), true);
//                checkUserCampaignSubscriptionStatus();
            }
            if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
            }
        } else {
            if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
            }
        }
        startSearchTextSwitch();

        cv_subject_left_scroll.setOnClickListener(v -> recyclerViewSub.smoothScrollBy(-recyclerViewSub.getWidth(), 0));
        cv_subject_right_scroll.setOnClickListener(v -> recyclerViewSub.smoothScrollBy(recyclerViewSub.getWidth(), 0));

        showAskDoubtFromItsBottomRight();

        cv_feature_left_scroll.setOnClickListener(v -> featureSectionRv.smoothScrollBy(-featureSectionRv.getWidth(), 0));
        cv_feature_right_scroll.setOnClickListener(v -> featureSectionRv.smoothScrollBy(featureSectionRv.getWidth(), 0));

        featureSectionRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                updateFeatureSectionArrowVisibility();
            }
        });

        how_to_poster.setOnClickListener(v -> {
            v.setEnabled(false);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.home_name_popup_close_click.getActionName());
            mListener.onHowToPosterClick();
            releaseHighlightVideo();

            v.postDelayed(() -> v.setEnabled(true), 2000);
        });

        try {
            posterUrl = HowToVideoView.getHowToPosterUrl("HOME_SCREEN");
            Log.d("posterUrl", "onViewCreated: " + posterUrl);
            if (StringUtils.isNotEmpty(posterUrl)) {
                how_to_poster.setVisibility(View.VISIBLE);
                Glide.with(this).load(posterUrl).into(how_to_poster);
            } else {
                how_to_poster.setVisibility(View.GONE);
            }
        } catch (Exception ignored) {
        }

    }


    public void setToolBarUserData() {
        headerShimmer.startShimmer();
        headerShimmer.setVisibility(View.VISIBLE);

        //join now btn
        boolean isVisible = true;
        String joinBtnTxt = getString(R.string.join_now_text);

        //grade
        String gradeCode = MyApplication.getStringData(PreferenceKeys.GRADE_CODE.getKey());

        //sub
        SubscriptionType subscriptionType = SubscriptionType.getSubscriptionType();

        try {
            //get data remote config
            String json = MyApplication.getFirebaseRemoteConfig().getString("board_join_now_button_config");

            String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
            if (StringUtils.isEmpty(lang)) lang = "en";

            if (StringUtils.isNotEmpty(json)) {
                JSONObject root = new JSONObject(json);

                //grade
                JSONObject gradeObj = root.optJSONObject(gradeCode);
                if (gradeObj == null) gradeObj = root.optJSONObject("default");

                if (gradeObj != null) {

                    //plan
                    JSONObject plans = gradeObj.optJSONObject("plans");

                    if (plans != null) {
                        JSONObject planObj = plans.optJSONObject(subscriptionType.name());

                        if (planObj != null) {
                            joinBtnTxt = planObj.optString(lang, joinBtnTxt);
                        } else {
                            isVisible = false;
                        }
                    } else {
                        isVisible = false;
                    }
                } else {
                    isVisible = false;
                }
            }
        } catch (Exception e) {
            Log.e("TAG", "error " + e.getMessage());
        }


        if (isVisible) {
            //set text color
            tv_join_button_right.setText(joinBtnTxt);
            cv_join_button_right.setVisibility(View.VISIBLE);
        } else {
            cv_join_button_right.setVisibility(View.GONE);
        }

        headerShimmer.stopShimmer();
        headerShimmer.setVisibility(View.GONE); //shimmer
        ll_header_name_layout.setVisibility(View.VISIBLE); //layout


        String myLang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        String classId = MyApplication.getStringData(PreferenceKeys.CLASS_ID.getKey());
        String selectedCourseId = MyApplication.getStringData(PreferenceKeys.SELECTED_COURSE_ID_LOCAL.getKey());
        Log.d(TAG, "setToolBarUserSubscribedOrNot: checking language feature → My_Lang=" + myLang
                + ", classId=" + classId + ", selectedCourseId=" + selectedCourseId);

        boolean isLanguageFeatureVisible = RemoteConfigUtils.isFeatureVisible(
                RemoteConfigUtils.FeatureVisibilityName.LANGUAGE_CHANGE.getFeature()
        );
        Log.d(TAG, "setToolBarUserSubscribedOrNot: isLanguageFeatureVisible = " + isLanguageFeatureVisible);



        Log.d(TAG, "setToolBarUserData: completed");
    }


    public void updateCarouselData() {
        homeScreenViewModel.clearCarouselData();
        homeScreenViewModel.callCarouselApi();
    }

    private void navigateToATLTestScreen(String categoryId, boolean attempted, String testSeriesName) {
        if (!attempted) {
            Map<String, String> params = new HashMap<>();
            params.put(PreferenceKeys.SELECTED_TEST_SERIES_TITLE.getKey(), categoryId);
            params.put("TestSeriesName", testSeriesName);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.ATL_Test_click.getActionName(), params);
            destroyHowTo();
            CommonNavigator.navigateToTestSeriesListActivity(requireActivity(),
                    categoryId, testSeriesName, null, false, null, true, false, TAG);
        } else {
            Intent intent = new Intent(requireActivity(), TestResultActivity.class);
            requireActivity().startActivity(intent);
        }
    }


    public void getAllFeatureSectionVisibilityCheckFromCache() {
        if (MyApplication.getBoolData(HomeScreenCacheData.AV_TODAYS_CLASSES.name())) {
            todayTaskContainer.setVisibility(View.VISIBLE);
            showTodayTaskFragment();
        } else {
            todayTaskContainer.setVisibility(View.GONE);
        }
    }


    public static void callMeBackReferAndWin(Context context, View view, CallMeBackModel callMeBackModel) {
        try {
            String lastDateTime = MyApplication.getStringData(PreferenceKeys.REFER_WIN_LAST_CALL_BACK_TIME.getKey());
            long callBackDuration = MyApplication.getFirebaseRemoteConfig()
                    .getLong("refer_win_call_back_restrict_duration");

            if (lastDateTime != null && DateTimeUtils.getDifferenceInMinutes(lastDateTime) <= callBackDuration) {
                setReferWinSnackMessage(context, view, null);
            } else {
                if (CommonMethods.isOnline(context)) {
                    CommonMethods.callMeBack(context, TAG, "REFER_WIN", "", new ApiCallBackListener() {
                        @Override
                        public void onSuccess(boolean status, String message, Object object) {
                            try {
                                if (callMeBackModel != null && callMeBackModel.getSnackBarMessage() != null && StringUtils.isNotEmpty(callMeBackModel.getSnackBarMessage().getMessage())) {
                                    if (callMeBackModel.getSnackBarMessage().isNegativeMessage()) {
                                        CommonMethods.showNegativeSnackbar(view, callMeBackModel.getSnackBarMessage().getMessage(), (callMeBackModel.getSnackBarMessage().getDuration() != 0) ? callMeBackModel.getSnackBarMessage().getDuration() : 3000);
                                    } else {
                                        CommonMethods.showPositiveSnackbar(view, callMeBackModel.getSnackBarMessage().getMessage(), (callMeBackModel.getSnackBarMessage().getDuration() != 0) ? callMeBackModel.getSnackBarMessage().getDuration() : 3000);
                                    }
                                } else {
                                    setReferWinSnackMessage(context, view, null);
                                }

                                MyApplication.saveData(PreferenceKeys.REFER_WIN_LAST_CALL_BACK_TIME.getKey(), DateTimeUtils.getCurrentDateTime());
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(boolean status, String message, Object object) {
                        }
                    });
                } else {
                    CommonMethods.showNegativeSnackbar(view, context.getString(R.string.internet_failure_error), 3000);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void setReferWinSnackMessage(Context context, View view, String message) {
        String callBackStartTime = MyApplication.getFirebaseRemoteConfig()
                .getString("refer_win_call_back_start_time");
        String callBackEndTime = MyApplication.getFirebaseRemoteConfig()
                .getString("refer_win_call_back_end_time");

        if (DateTimeUtils.isCurrentTimeInRange(callBackStartTime, callBackEndTime)) {
            CommonMethods.showPositiveSnackbar(view, StringUtils.isNotEmpty(message) ? message : context.getString(R.string.aapko_30_min_me_aapko_call_karke_jankari_di_jayegi), 3000);
        } else {
            CommonMethods.showPositiveSnackbar(view, StringUtils.isNotEmpty(message) ? message : context.getString(R.string.aapko_subah_tak_call_kiya_jayega), 3000);
        }

    }

    private void fetchHomeScreenDataFromCache() {
        try {
            Log.d(TAG, "---- fetchHomeScreenDataFromCache START ----");

            String welcomeText = MyApplication.getStringData(HomeScreenCacheData.AV_HOME_SCREEN_WELCOME_TEXT.name());
            Log.d(TAG, "WELCOME_TEXT = " + welcomeText);

            if (!StringUtils.isEmpty(welcomeText)) {
                user_name.setText(welcomeText);
                user_name.setVisibility(View.VISIBLE);
                Log.d(TAG, "User name set");
            } else {
                user_name.setVisibility(View.GONE);
                Log.d(TAG, "WELCOME_TEXT is empty");
            }

            String footerUrl = MyApplication.getStringData(HomeScreenCacheData.AV_HOME_SCREEN_FOOTER_IMAGE_URL.name());
            Log.d(TAG, "FOOTER_IMAGE_URL = " + footerUrl);

            if (!StringUtils.isEmpty(footerUrl)) {
                Glide.with(this)
                        .load(footerUrl)
                        .into(footerImage);
                footerImageLayout.setVisibility(View.VISIBLE);
                Log.d(TAG, "Footer image loaded");
            } else {
                Log.d(TAG, "FOOTER_IMAGE_URL is empty");
            }

            String iconUrl = MyApplication.getStringData(HomeScreenCacheData.AV_HOME_SCREEN_WELCOME_ICON.name());
            Log.d(TAG, "WELCOME_ICON_URL = " + iconUrl);

            if (!StringUtils.isEmpty(iconUrl)) {
                welcome_icon.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(iconUrl)
                        .into(welcome_icon);
                Log.d(TAG, "Welcome icon visible & loaded");
            } else {
                welcome_icon.setVisibility(View.GONE);
                Log.d(TAG, "Welcome icon hidden");
            }

            String headerUrl = MyApplication.getStringData(PreferenceKeys.HEADER_IMAGE_URL.getKey());
            Log.d(TAG, "HEADER_IMAGE_URL = " + headerUrl);

            if (!StringUtils.isEmpty(headerUrl)) {
                Glide.with(requireActivity())
                        .load(headerUrl)
                        .into(new CustomTarget<Drawable>() {

                            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                Log.d(TAG, "Header image loaded");

                                headerBackgroundImage.setImageDrawable(resource);
                                headerBackgroundImage.setAdjustViewBounds(true);
                                headerBackgroundImage.setScaleType(ImageView.ScaleType.FIT_START);

                                ViewGroup.LayoutParams params = headerBackgroundImage.getLayoutParams();
                                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                                headerBackgroundImage.setLayoutParams(params);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                Log.d(TAG, "Header image cleared");
                            }
                        });
            } else {
                Log.d(TAG, "HEADER_IMAGE_URL is empty");
            }

            // Load chips from cached ExamUiDataResponse before API fires
            String cachedExamUi = MyApplication.getStringData(HomeScreenCacheData.AV_EXAM_UI_DATA.name());
            Log.d(TAG, "CACHED_EXAM_UI_DATA = " + cachedExamUi);
            if (!StringUtils.isEmpty(cachedExamUi)) {
                ExamUiDataResponse cached = new Gson().fromJson(cachedExamUi, ExamUiDataResponse.class);
                if (cached != null) {
                    examUiDataResponse = cached;
                }
                updateHeaderChips(cached, MyApplication.getUserDetailsResponse("userDto"));
                Log.d(TAG, "Chips loaded from cache");
            } else {
                Log.d(TAG, "No cached ExamUiData for chips");
            }

            Log.d(TAG, "Calling fetchHomeScreenUI()");
            fetchHomeScreenUI();

            Log.d(TAG, "---- fetchHomeScreenDataFromCache END ----");

        } catch (Exception ex) {
            Log.e(TAG, "Exception in fetchHomeScreenDataFromCache", ex);
        }
    }

    private void startButtonAnimation(ConstraintLayout buttonLayout, TextView txt, ImageView icon,
                                      String[] texts, Integer[] icons, Integer[] backgrounds, long interval) {

        Handler handler = new Handler(Looper.getMainLooper());
        final int[] index = {1};
        Runnable r = new Runnable() {
            @Override
            public void run() {
                txt.animate()
                        .translationY(-txt.getHeight())
                        .setDuration(200)
                        .start();
                if (icon != null) {
                    icon.animate()
                            .translationY(-icon.getHeight())
                            .setDuration(200)
                            .start();
                }

                // Change background after slide-out
                handler.postDelayed(() -> {
                    // Change text and icon
                    txt.setText(texts[index[0]]);
                    txt.setTranslationY(txt.getHeight()); // reset position

                    if (icon != null) {
                        icon.setImageResource(icons[index[0]]);
                        icon.setTranslationY(icon.getHeight()); // reset position
                    }

                    // Animate background change
                    Drawable newBg = ContextCompat.getDrawable(buttonLayout.getContext(), backgrounds[index[0]]);
                    Drawable oldBg = buttonLayout.getBackground();

                    if (oldBg == null) {
                        buttonLayout.setBackground(newBg);
                    } else if (newBg != null) {
                        TransitionDrawable transitionDrawable = new TransitionDrawable(new Drawable[]{oldBg, newBg});
                        buttonLayout.setBackground(transitionDrawable);
                        transitionDrawable.startTransition(200); // smooth transition
                    }

                    txt.animate()
                            .translationY(0)
                            .setDuration(200)
                            .start();

                    if (icon != null) {
                        icon.animate()
                                .translationY(0)
                                .setDuration(200)
                                .start();
                    }

                    // Move to next index
                    index[0] = (index[0] + 1) % texts.length;

                    // Repeat animation
                    handler.postDelayed(this, interval);

                }, 200); // wait for slide-out animation
            }
        };

        handler.postDelayed(r, interval);
    }


    private void fetchHomeScreenUI() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("examCode", MyApplication.getStringData(PreferenceKeys.EXAM_CODE.getKey()));
        MyJsonObjectRequest request = new MyJsonObjectRequest(Request.Method.GET, CommonMethods.getUrlParam(AV_EXAM_UI_DETAILS, map), null,
                response -> {
                    if (!isAdded()) return;
                    try {
                        Log.d("fetchHomeScreenUI", "fetchHomeScreenUI: " + response);
                        if (response != null) {
                            Gson gson = new Gson();
                            TypeToken<ApiWrappedResponse<ExamUiDataResponse>> token = new TypeToken<ApiWrappedResponse<ExamUiDataResponse>>() {
                            };
                            ApiWrappedResponse<ExamUiDataResponse> apiResponse = gson.fromJson(response.toString(), token.getType());
                            if (apiResponse != null && apiResponse.getData() != null) {
                                ExamUiDataResponse homeData = apiResponse.getData();
                                examUiDataResponse = homeData;
                                MyApplication.saveData(HomeScreenCacheData.AV_EXAM_UI_DATA.name(), new Gson().toJson(homeData));
                                updateUIWithHomeScreenData(homeData);
                                updateHeaderChips(homeData, userDetailsResponse);
                                updateHomeScreenHeaderTitle(homeData);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                new VolleyGlobalErrorListener(getActivity()) {
                    @Override
                    public void postError(VolleyError error) {
                        Log.e(TAG, "API Error - Failed to fetch home screen UI data", error);

                        if (error.networkResponse != null) {
                            Log.e(TAG, "Status Code: " + error.networkResponse.statusCode);
                        }

                        if (error.getMessage() != null) {
                            Log.e(TAG, "Error Message: " + error.getMessage());
                        }
                    }
                }
        );

        request.setTag(TAG);
        MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(request);
    }

    private void updateHomeScreenHeaderTitle(ExamUiDataResponse homeData) {

        if (!StringUtils.isEmpty(homeData.getHomeScreenTitleText())) {

            boolean editYourNameVisibility = RemoteConfigUtils.isFeatureVisible(
                    RemoteConfigUtils.FeatureVisibilityName.EDIT_YOUR_NAME.getFeature()
            );

            // Always work on trimmed title to avoid index mismatches
            String title = homeData.getHomeScreenTitleText().trim();
            SpannableStringBuilder spannable = new SpannableStringBuilder(title);

            String[] words = title.split("\\s+");

            boolean nameIsEmpty = StringUtils.isEmpty(MyApplication.getStringData("name"));

            Log.d("NameHighlight", "words=" + words.length
                    + " nameEmpty=" + nameIsEmpty
                    + " editVisible=" + editYourNameVisibility);

            if (words.length >= 2 && nameIsEmpty && editYourNameVisibility) {

                String lastWord = words[words.length - 1];

// Keep Hindi matras
                String cleanName = lastWord.replaceAll("[^\\p{L}\\p{M}\\p{N}]", "");

                if (!cleanName.isEmpty()) {
                    int start = title.lastIndexOf(lastWord);
                    int end = start + lastWord.length();
                    Log.d("NameHighlight", "lastWord='" + lastWord
                            + "' clean='" + cleanName
                            + "' start=" + start + " end=" + end);
                    if (start >= 0 && end <= title.length()) {
                        int color = ContextCompat.getColor(
                                requireActivity(), R.color.arivihan_secondary_color);
                        spannable.setSpan(
                                new DottedUnderlineSpan(color),
                                start,
                                end,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        );
                    }
                }
            }

            user_name.setText(spannable);
            user_name.setVisibility(View.VISIBLE);

            MyApplication.saveData(
                    HomeScreenCacheData.AV_HOME_SCREEN_WELCOME_TEXT.name(),
                    title
            );
        }

        if (!StringUtils.isEmpty(homeData.getHomeScreenTitleIcon())) {
            Glide.with(this)
                    .load(homeData.getHomeScreenTitleIcon())
                    .placeholder(R.drawable.blank)
                    .into(welcome_icon);

            welcome_icon.setVisibility(View.VISIBLE);

            MyApplication.saveData(
                    HomeScreenCacheData.AV_HOME_SCREEN_WELCOME_ICON.name(),
                    homeData.getHomeScreenTitleIcon()
            );
        } else {
            welcome_icon.setVisibility(View.GONE);
        }
    }


    private void updateUIWithHomeScreenData(ExamUiDataResponse data) {
        if (!StringUtils.isEmpty(data.getHomeScreenFooterImageUrl())) {
            Glide.with(this)
                    .load(data.getHomeScreenFooterImageUrl())
                    .into(footerImage);
            MyApplication.saveData(HomeScreenCacheData.AV_HOME_SCREEN_FOOTER_IMAGE_URL.name(), data.getHomeScreenFooterImageUrl());
            footerImageLayout.setVisibility(View.VISIBLE);
        } else {
            footerImageLayout.setVisibility(View.GONE);
        }
        if (!StringUtils.isEmpty(data.getHomeScreenHeaderBackgroundImageUrl())) {
            Glide.with(requireActivity())
                    .load(data.getHomeScreenHeaderBackgroundImageUrl())
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource,
                                                    @Nullable Transition<? super Drawable> transition) {
                            headerBackgroundImage.setImageDrawable(resource);
                            headerBackgroundImage.setAdjustViewBounds(true);

                            headerBackgroundImage.setScaleType(ImageView.ScaleType.FIT_START);
                            ViewGroup.LayoutParams params = headerBackgroundImage.getLayoutParams();
                            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                            headerBackgroundImage.setLayoutParams(params);
                            MyApplication.saveData(PreferenceKeys.HEADER_IMAGE_URL.getKey(), data.getHomeScreenHeaderBackgroundImageUrl());
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
        } else {
            linearLayoutScrollView.setBackgroundResource(R.color.white);
        }

    }


    private void showAskDoubtFromItsBottomRight() {
        ask_relative.post(() -> {
            setAskDoubtVisibility(true);
            ask_relative.setPivotX(ask_relative.getWidth());
            ask_relative.setPivotY(ask_relative.getHeight());
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(ask_relative, "scaleX", 0f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(ask_relative, "scaleY", 0f, 1f);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleX, scaleY);
            animatorSet.setDuration(300);
            animatorSet.start();
        });

    }

    private void hideAskDoubtToBottomRight() {
        ask_relative.setPivotX(ask_relative.getWidth());
        ask_relative.setPivotY(ask_relative.getHeight());
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(ask_relative, "scaleX", 1f, 0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(ask_relative, "scaleY", 1f, 0f);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(500);

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setAskDoubtVisibility(false);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        animatorSet.start();
    }

    private void updateSubjectArrowsVisibility() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerViewSub.getLayoutManager();
        if (layoutManager == null || recyclerViewSub.getAdapter() == null) return;

        int firstVisible = layoutManager.findFirstCompletelyVisibleItemPosition();
        int lastVisible = layoutManager.findLastCompletelyVisibleItemPosition();
        int total = recyclerViewSub.getAdapter().getItemCount();

        cv_subject_left_scroll.setVisibility(firstVisible > 0 ? View.VISIBLE : View.GONE);
        cv_subject_right_scroll.setVisibility(lastVisible < total - 1 ? View.VISIBLE : View.GONE);
    }

    void startSearchTextSwitch() {
        searchOptionList = new ArrayList<>();

        String searchSwitchText = CommonMethods.getObjectFromRemoteConfig("global_search_switch_text");
        String globalSearchStartText = CommonMethods.getObjectFromRemoteConfig("global_search_start_text");
        String globalSearchEndText = CommonMethods.getObjectFromRemoteConfig("global_search_end_text");

        searchTextStart.setText(globalSearchStartText);
        searchTextEnd.setText(globalSearchEndText);

        if (StringUtils.isNotEmpty(searchSwitchText)) {
            searchOptionList.addAll(Arrays.asList(searchSwitchText.split(",")));
        } else {
            searchOptionList.add("Notes");
            searchOptionList.add("Classes");
            searchOptionList.add("Test Series");
        }

        if (searchTextHandler != null) {
            searchTextHandler.removeCallbacksAndMessages(null);
        }
        searchTextHandler = new Handler(Looper.getMainLooper());
        searchTextRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isAdded() || searchTextSwitcher == null) return;
                if (searchTextAnimationPosition == searchOptionList.size()) {
                    searchTextAnimationPosition = 0;
                }
                if (!searchOptionList.isEmpty()) {
                    searchTextSwitcher.setText(searchOptionList.get(searchTextAnimationPosition));
                }

                searchTextAnimationPosition++;
                searchTextHandler.postDelayed(this, 3200);
            }
        };
        searchTextHandler.post(searchTextRunnable);
    }

    private void setCarouselData(List<CarouselResponse> carouselItems) {
        Log.d(TAG, "setCarouselData: " + carouselItems);
        if (carouselItems == null || carouselItems.isEmpty()) {
            return;
        }
        try {
            ArrayList<CarouselResponse> carouselList = new ArrayList<>(carouselItems);
            FragmentManager fragmentManager = getChildFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            CarouselFragment carouselFragment = CarouselFragment.newInstance(carouselList);
            fragmentTransaction.replace(R.id.image_sliders, carouselFragment);
            fragmentTransaction.commit();
        } catch (Exception e) {
            Log.e("YourActivity", "Error setting carousel data", e);
        }
    }

    public void releaseHighlightVideo() {
        highlistAdapter.stopPlayer();
    }

    @Override
    public void setAskDoubtMargin() {
        setBottomMargin(ask_relative, 0);
    }

    @Override
    public void onDismiss(String dialogBannerText, String dialogBannerImage, long dialogTimeout) {
        PosterPopUp posterPopUp = new PosterPopUp(requireContext());
        posterPopUp.setOnDismissListener(this);
        posterPopUp.showPosterPopUp(MyApplication.getStringData(PreferenceKeys.DIALOG_BANNER_TEXT.getKey()),
                MyApplication.getStringData(PreferenceKeys.DIALOG_BANNER_IMAGE.getKey()),
                MyApplication.getLongData(PreferenceKeys.DIALOG_TIMEOUT.getKey()));
    }

    @Override
    public void onDismissListener() {
        Log.d("onDismissListener", "onDismissListener notification listener called : ");
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(
                            requireActivity(),
                            new String[]{Manifest.permission.POST_NOTIFICATIONS},
                            NOTIFICATION_PERMISSION_REQUEST_CODE
                    );
                } else {
                    Log.d("onDismissListener", "Notification permission already granted.");
                }
            }
        } catch (Exception e) {
            Log.e("onDismissListener", "Error requesting notification permission", e);
        }
    }

    @Override
    public void onFeatureClick(HomeScreenCardResponse item) {
        try {
            String featureTag = item.getFeatureTag();
            if (featuresSectionAdapter != null && featureTag != null) {
                markAnimationTagAsClicked(featureTag);
            }

//            CommonMethods.navigateFromActivityConfig(requireContext(), item.getNavigationType(), item.getClassName(), ActivityLauncher.convertJsonBackToActivityParams(item.getActivityParams()));
            Log.d(TAG, "onFeatureClick: " + item);
//            Map<String, String> params = new HashMap<>();
            String fullClassName = item.getClassName();
            String shortClassName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);

            if (ToppersNotesActivity.TAG.equals(shortClassName)
                    && item.getActivityParams() != null
                    && !item.getActivityParams().containsKey("ncertSolutions")
                    && !item.getActivityParams().containsKey("categoryId")) {
            }

            boolean isTopperNotesScreen = ToppersNotesActivity.TAG.equals(shortClassName)
                    || TopperNotesActivity.class.getSimpleName().equals(shortClassName);
            if (isTopperNotesScreen
                    && StringUtils.isEmpty(MyApplication.getStringData("name"))
                    && item.getActivityParams() != null
                    && !item.getActivityParams().containsKey("ncertSolutions")) {

                HashMap<String, Object> extras = new HashMap<>();
                extras.put("activity_params_json", new Gson().toJson(item.getActivityParams()));
                extras.put("navigation_type", item.getNavigationType());

                NameInputDto dto = NameInputDto.builder()
                        .nextScreenClass(fullClassName)
                        .screen(shortClassName)
                        .extras(extras)
                        .navigationType("Activity")
                        .build();

                Intent intent = new Intent(requireActivity(), NameInputActivity.class);
                intent.putExtra("activity_params", dto);
                startActivity(intent);
                return;
            }

            if (PARIKSHA_ADHYAYAN_FEATURE_TAG.equals(featureTag)) {
                UserDto userDto = MyApplication.getUserDto("userDto");
                String accessStatus = userDto != null && userDto.getParikshaAdhyayanAccess() != null
                        ? userDto.getParikshaAdhyayanAccess() : "UNKNOWN";
                Map<String, String> paCardParams = new HashMap<>();
                paCardParams.put("access_status", accessStatus);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.pa_card_clicked.getActionName(), paCardParams);
                if ("ACCESS_DENIED".equals(accessStatus)) {
                    new ParikshaAdhyayanReferralDialog(requireContext()).show();
                    return;
                }
            }

            CommonMethods.navigateFromActivityConfig(
                    requireContext(),
                    item.getNavigationType(),
                    fullClassName,
                    item.getActivityParams()
            );

            Log.d(TAG, "onFeatureClick: " + item);

            Map<String, String> params = new HashMap<>();
            params.put("class_name", fullClassName);
            params.put("featureClicked", shortClassName);
            params.put("feature_name", item.getTitle());
            params.put("activity_params", item.getActivityParams().toString());
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.hs_feature_section_clicked.getActionName(), params);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void playSequentialBorderAnimation() {
        if (!SubscriptionType.isBasicSubscription()) return;

        // Guard: if an animation is already showing in either section, don't disturb it.
        // On resume (after returning from another screen), refresh the active animation so

        // it restarts if it was stopped by onPause.
        if (isAnyAnimationActive()) {
            if (featuresSectionAdapter != null)
                featuresSectionAdapter.refreshAnimatedTag(featureSectionRv);
            return;
        }

        if (!todayTaskFetched || !allFeaturesFetched)
            return;

        String batchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
        String json = MyApplication.getFirebaseRemoteConfig().getString("card_animation_sequence");

        List<String> sequence = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(json);
            JSONArray arr = root.optJSONArray(batchCode);
            if (arr == null) arr = root.optJSONArray("DEFAULT");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    sequence.add(arr.getString(i));
                }
            }
        } catch (Exception ignored) {
        }

        if (sequence.isEmpty()) return;

        Log.d(TAG, "---------> Animation seq : " + sequence);

        for (String tag : sequence) {
//            boolean isPriority = tag.contains("(P)");
//            if (isPriority) {
//                tag = tag.substring(0, tag.length() - 3);
//            }
            String userId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
            boolean clicked = MyApplication.getBooleanDataFromDoNotDeleteSharedPreference(userId + "_animation_tag_clicked_" + tag);
            if (clicked) continue;

            // Check today's class section
            if (todayTaskFragment != null && todayTaskFragment.hasItemWithTag(tag)) {
//                Log.d(TAG, "---> animating today-class tag: " + tag);
                todayTaskFragment.setAnimatedItemTag(tag);
                return;
            }

            // Check feature section
            if (featuresSectionAdapter != null) {
                for (HomeScreenCardResponse card : featureSectionModelsList) {
                    if (tag.equals(card.getFeatureTag())) {
//                        Log.d(TAG, "---> animating feature tag: " + tag);
                        featuresSectionAdapter.setAnimatedItemTag(tag);
                        return;
                    }
                }
            }

//            if (isPriority) {
//                return;
//            }
            // Tag not found in either section.
            // If today-class data hasn't arrived yet, hold — the tag may belong there.
            // playSequentialBorderAnimation() will be called again once the data loads.
//            if (todayTaskFragment != null && !todayTaskFragment.isTodayClassDataLoaded()) {
//                Log.d(TAG, "---> today class not loaded, holding for tag: " + tag);
//                return;
//            }

            // Data loaded but tag absent from both sections — skip to next
            Log.d(TAG, "---> tag not found, skipping: " + tag);
        }
    }

    private boolean isAnyAnimationActive() {
        if (featuresSectionAdapter != null && featuresSectionAdapter.getAnimatedItemTag() != null) {
            return true;
        }
        if (todayTaskFragment != null && todayTaskFragment.getAnimatedItemTag() != null) {
            return true;
        }
        return false;
    }

    private void markAnimationTagAsClicked(String tag) {
        if (StringUtils.isEmpty(tag)) return;

        String userId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
        MyApplication.saveSharedPreferencesDoNotDelete(userId + "_animation_tag_clicked_" + tag, true);
        // Clear the completed animation, then find the next one in the sequence
        if (todayTaskFragment != null) todayTaskFragment.clearAnimatedItemTag();
        if (featuresSectionAdapter != null) featuresSectionAdapter.clearAnimatedTag();
        playSequentialBorderAnimation();
    }

    public interface OnFragmentInteractionListener {
        void onHowToPosterClick();
    }

    private void getCarouselData() {
        Log.d("ObserverCarousel ", "observer data : ");
        if (getView() == null) {
            return;
        }
        homeScreenViewModel.getCarouselData().removeObservers(getViewLifecycleOwner());
        homeScreenViewModel.getCarouselData().observe(getViewLifecycleOwner(),
                (data) -> {
                    List<CarouselResponse> sliderItems = new ArrayList<>(data);
                    Log.d("ObserverCarousel ", "observer: " + data.size());
                    carouselShimmer.setVisibility(View.GONE);
                    setCarouselData(sliderItems);
                });
    }

    private void handleCourseSwitchSelection() {

        Log.d("SWITCHING_COURSE", "VALUE = " + MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey()));

    }

    private void getRoutineDataCheckStatus() {
        if (getView() == null) {
            return;
        }
    }

    @Override
    public void onsubjectSortClick(String id, String name, int position) {
        if (selectSubject != null) {
            selectSubject.dismiss();
            selectSubject.cancel();
        }
        if (fab_menu != null) {
            fab_menu.performClick();
        }
        if (microLectureContentDto != null) {
            startVideoActivity(id);
        } else {
            if (getContext() != null) {
                loading.show();
            }
            getDoubtVideoContent(id);
        }
    }

    private void startVideoActivity(String id) {
        @OptIn(markerClass = UnstableApi.class) Intent intent = new Intent(getActivity(), VideoPlayerActivity.class);
        intent.putExtra(getResources().getString(R.string.passed_obj), microLectureContentDto);
        intent.putExtra("selectedChapter", microLectureContentDto.getChapterName());
        intent.putExtra(getString(R.string.selected_chpter_id), microLectureContentDto.getChapterId());
        intent.putExtra("position", microLectureContentDto.getDefaultStartTime());
        intent.putExtra("selectedSubject", id);
        intent.putExtra("shouldNotResume", false);
        intent.putExtra("doubtVideo", true);
        intent.putExtra("isAskDoubtActivity", true);
        intent.putExtra("askDoubtResumePosition", Long.valueOf(microLectureContentDto.getResumePosition()));
        intent.putExtra("resumePosition", Long.valueOf(microLectureContentDto.getDefaultStartTime()));
        destroyHowTo();
        startActivity(intent);
    }

    private void showPopupDialog(String id) {

        String oldId = MyApplication.getStringData(PreferenceKeys.POPUP_ID.getKey());

        int countPopUp = MyApplication.getIntData(PreferenceKeys.COUNT_POP_UP.getKey());

        if (oldId == null) {
            oldId = "dummy";
        }

        if (countPopUp > 2 && oldId.equalsIgnoreCase(id)) {
            return;
        }

        if (!oldId.equalsIgnoreCase(id)) {
            countPopUp = 0;
            MyApplication.saveData(PreferenceKeys.COUNT_POP_UP.getKey(), 0);
            MyApplication.saveData(PreferenceKeys.POPUP_ID.getKey(), id);
            MyApplication.saveData(PreferenceKeys.POP_UP_INDEX.getKey(), -1);
        }

        if (countPopUp < 1) {
            MyApplication.saveData(PreferenceKeys.COUNT_POP_UP.getKey(), 1);
        } else {
            MyApplication.saveData(PreferenceKeys.COUNT_POP_UP.getKey(), countPopUp + 1);
        }


        dialogHome = new Dialog(getActivity());
        dialogHome.setContentView(R.layout.custom_home_dialogue);
        dialogHome.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        dialogHome.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogHome.setCancelable(false);

        if (getContext() != null) {
            dialogHome.show();
        }


        CardView cancel = dialogHome.findViewById(R.id.cancel);
        ImageView image = dialogHome.findViewById(R.id.image);

        if (!titles.isEmpty()) {

            int popUpIndex = MyApplication.getIntData(PreferenceKeys.POP_UP_INDEX.getKey());

            try {
                popUpIndex = (popUpIndex + 1) % titles.size();

                MyApplication.saveData(PreferenceKeys.POP_UP_INDEX.getKey(), popUpIndex);
                String url = titles.get(popUpIndex);
                Glide.with(this).load(url).into(image);

                Log.d("PopupStoreTimeIndex", "" + popUpIndex);

            } catch (Exception e) {
                Glide.with(this).asGif().load(R.raw.image_gif).into(image);
            }

        } else {
            Glide.with(this).asGif().load(R.raw.image_gif).into(image);

        }


        MyApplication.saveData(PreferenceKeys.POPUP_STORE_TIME.getKey(), System.currentTimeMillis() + 43200000);


        cancel.setOnClickListener(v -> dialogHome.dismiss());
    }

    private void setBoardDialog() {
        boardDialog = new Dialog(getActivity());
        boardDialog.setContentView(R.layout.custom_board_dialog);
        boardDialog.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        boardDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        boardDialog.setCancelable(false);
        progressBarX = boardDialog.findViewById(R.id.progessBar);

        vernacularRecyler = boardDialog.findViewById(R.id.recyclerView);
        vernacularRecyler.setLayoutManager(new WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        vernacularGridAdapter = new VernacularGridAdapter(languageList, getContext(), this::changeImage, true);
        vernacularRecyler.setAdapter(vernacularGridAdapter);

        vernacularRecyler.setVisibility(View.VISIBLE);
        progressBarX.setVisibility(View.GONE);

    }


    @Override
    public void changeImage(VernacularItem item) {
        selectedBoard = item.id;
        selectedBoardName = item.name;
        saveData();
    }

    private void getDoubtVideoContent(String selectedSubject) {
        Map<String, Object> map = new HashMap<>();
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_DOUBT_MICROLECTURE_CONTENT, map), null,
                response -> {
                    try {
                        TypeToken<MicroLectureContentDto> token = new TypeToken<MicroLectureContentDto>() {
                        };
                        microLectureContentDto = gson.fromJson(response.toString(), token.getType());
                        if (StringUtils.isNotEmpty(selectedSubject)) {
                            startVideoActivity(selectedSubject);
                            loading.cancel();
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }, new VolleyGlobalErrorListener(getActivity()) {
            @Override
            public void postError(VolleyError error) {

                dataLoaded = 0;
//                CommonMethods.showNegativeSnackbar(constraintLayout, error.getMessage());
                if (loading != null) {
                    loading.cancel();
                }
            }
        });
        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(getContext()).addToRequestQueue(myJsonObjectRequest);
    }

    private void getAndUpdateVideoUrl() {
        try {
            final Map<String, Object> map = new HashMap<>();
            map.put("featureKey", "INFO_VIDEO_URL");
            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                    CommonMethods.getUrlParam(CONFIGURATION, map), null,
                    response -> {
                        try {
                            Gson gson = new Gson();
                            TypeToken<ConfigurationResult> token = new TypeToken<ConfigurationResult>() {
                            };
                            ConfigurationResult configurationResult = gson.fromJson(response.toString(), token.getType());

                            if (configurationResult != null && configurationResult.getData() != null) {
                                Configuration data = configurationResult.getData();

                                Map<String, String> keyValues = data.getKeyValues();


                                String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());

                                if (lang == null) {
                                    lang = "";
                                }
                                String key = "video_url" + lang;

                                if (keyValues.containsKey(key)) {
                                    MyApplication.saveData(key, keyValues.get(key));
                                }

                            }

                        } catch (Exception e) {

                        }
                    }, new VolleyGlobalErrorListener(getActivity()) {
                @Override
                public void postError(VolleyError error) {

                }
            });
            myJsonObjectRequest.setTag(TAG);
            MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(myJsonObjectRequest);
        } catch (Exception e) {

        }

    }

    private void setBottomMargin(View view, int bottomMarginDp) {
        float scale = view.getContext().getResources().getDisplayMetrics().density;
        int bottomMarginPx = (int) (bottomMarginDp * scale + 0.5f);

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        params.bottomMargin = bottomMarginPx;
        view.setLayoutParams(params);
    }

    @Override
    public void onResume() {
        if (!onCreateCalled) {
            Log.d("issue", "onResumecalledNot created: ");
        }

        Log.d("getSubscribedSubjectData", "onResume");
        super.onResume();
        showAskDoubtFromItsBottomRight();

        if (MyApplication.getLiveSessionStatus() && !MyApplication.getBoolData(PreferenceKeys.HOME_SCREEN_ON_CREATE_CALLED.getKey())) {
            Log.d("LiveSessionHelper", "✅ Condition TRUE -> Fetching from Remote Config");

            FirebaseRemoteConfig mFirebaseRemoteConfig = MyApplication.getFirebaseRemoteConfig();

            String jsonString = mFirebaseRemoteConfig.getString("home_screen_register_card_json_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) ask_relative.getLayoutParams();

            if (!jsonString.isEmpty()) {
                Log.d("LiveSessionHelper", "JSON Applied: " + jsonString);
                registerCardView.applyJson(jsonString);
                registerCardView.setVisibility(View.VISIBLE);
                setBottomMargin(ask_relative, -30);
            } else {
                Log.d("LiveSessionHelper", "JSON is empty, resetting layout.");
                setBottomMargin(ask_relative, 0);
            }
        } else {
            registerCardView.setVisibility(View.GONE);
        }
        MyApplication.saveData(PreferenceKeys.HOME_SCREEN_ON_CREATE_CALLED.getKey(), false);


        setObserver();

        homeScreenViewModel.getCarouselData(true);

        if (!onCreateCalled) {

            Log.d("getstudentprogress", "onResumeCalled ");

            if (classes_in_progress != null) {
                classes_in_progress.setVisibility(View.GONE);
            }
            boolean refresh = MyApplication.getBoolData(PreferenceKeys.REFRESH.getKey());
            homeScreenViewModel.refreshHomeScreenData(refresh);

        }
        try {
            getUserSelectedDownloadQuality();
            getUserSelectedVideoQuality();
            initBottomStatusView();
        } catch (Exception e) {
            Log.d("onResume", e.getMessage());
        }

        if (!CommonMethods.isOnline(requireContext())) {
            if (noInternetDialog != null && !noInternetDialog.isShowing()) {
                noInternetDialog.show();
            }
        }
        playSequentialBorderAnimation();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d("getRoutineCheckdata", " called form on atatch ");
    }

    private void initBottomStatusView() {
        if (!isAdded() || isDetached()) return;
        if (customBottomStatusView != null) return;
//        if (MyApplication.getStringData(PreferenceKeys.CLASS_ID.getKey()) == null) return;
        customBottomStatusView = new CustomBottomStatusView(home_screen_status_card_layout, requireActivity(), new CustomBottomStatusView.CustomBottomStatusListener() {
            @Override
            public void updateSubscriptionActivation(boolean isActive) {
                try {
                    ((HomeScreen) requireActivity()).updateBottomNavForSubscription(true);
                } catch (Exception ignored) {
                }
            }
        });
    }

    private void getUserSelectedDownloadQuality() {
        try {
            if (getActivity() != null && isAdded()) {
                databaseReference3 = FirebaseDatabase.getInstance().getReference("users/UserSelectedDownloadQuality/" + MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
                databaseReference3.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("Firebase", "Error fetching data: " + databaseError.getMessage());
                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void getUserSelectedVideoQuality() {
        DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference("users/UserSelectedVideoQuality/" + MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
        databaseReference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        MyApplication.saveData(getString(R.string.user_selected_video_quality), dataSnapshot.getValue(String.class));
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

    @Override
    public void onStop() {
        super.onStop();
        if (noInternetDialog != null && noInternetDialog.isShowing()) {
            noInternetDialog.dismiss();
        }
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        if (getContext() != null) {
            MyVolleySingleton.getInstance(getContext()).getRequestQueue().cancelAll("ExamBadgeRequest");
        }
        if (getContext() != null) {
            MyVolleySingleton.getInstance(getContext()).getRequestQueue().cancelAll("SYNC_API");
        }
        if (getActivity() != null) {
            MyVolleySingleton.getInstance(getActivity()).getRequestQueue().cancelAll("TOPPERS_API_TAG");
        }

        MyVolleySingleton.getInstance(getActivity()).getRequestQueue().cancelAll(TAG);
        if (dialogHome != null) {
            dialogHome.dismiss();
            if (getActivity() != null) {
                MyVolleySingleton.getInstance(getActivity()).getRequestQueue().cancelAll(TAG);
            }
        }
    }

    @Override
    public void onDestroyView() {
        try {
            if (featuresSectionAdapter != null && featureSectionRv != null) {
                featuresSectionAdapter.stopCurrentAnimationIfBound(featureSectionRv);
                featuresSectionAdapter.clearAnimatedTag();
            }
        } catch (Exception ignored) {
        }
        try {
            if (getView() == null) {
                super.onDestroyView();
                return;
            }
            if (homeScreenViewModel != null) {
                homeScreenViewModel.getCarouselData().removeObservers(getViewLifecycleOwner());
                homeScreenViewModel.getSubjectList().removeObservers(getViewLifecycleOwner());
                homeScreenViewModel.getTestSeriesList().removeObservers(getViewLifecycleOwner());
                homeScreenViewModel.getProgress().removeObservers(getViewLifecycleOwner());
            }
        } catch (Exception e) {

        }
        super.onDestroyView();

    }

    private void reloadData() {

        MyApplication.saveData(PreferenceKeys.PROGRESS_RELOAD.getKey(), true);
        boolean refresh = MyApplication.getBoolData(PreferenceKeys.REFRESH.getKey());
        if (homeScreenViewModel != null) {
            homeScreenViewModel.refreshHomeScreenData(refresh);
        }
        getAPIDataByPhoneData();
    }


    private void saveData() {
        progressBarX.setVisibility(View.VISIBLE);
        vernacularRecyler.setVisibility(View.GONE);
//        userDto.setBoardId(selectedBoard);
//        userDto.setBoardName(selectedBoardName);
        try {
            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.PATCH,
                    PUT_UPDATE, new JSONObject(gson.toJson(userDetailsResponse)), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (Boolean.parseBoolean(response.get("success").toString())) {
                            MyApplication.saveData(PreferenceKeys.USER_BOARD.getKey(), selectedBoard);
                            MyApplication.saveData(PreferenceKeys.USER_BOARD_NAME.getKey(), selectedBoardName);
                            CommonMethods.showPositiveSnackbar(constraintLayout, getString(R.string.profile_updated_success));
                            boardDialog.dismiss();
                        }
                    } catch (JSONException e) {
                        CommonMethods.showNegativeSnackbar(constraintLayout, e.getMessage());
                    }
                }
            }, new VolleyGlobalErrorListener(getContext()) {
                @Override
                public void postError(VolleyError error) {
                    CommonMethods.showNegativeSnackbar(constraintLayout, error.getMessage());

                }
            });
            myJsonObjectRequest.setTag(TAG);
            MyVolleySingleton.getInstance(getContext()).addToRequestQueue(myJsonObjectRequest);
        } catch (JSONException e) {
            Log.d("error", e.getMessage());
        }
    }

    private void getAPIDataByPhoneData() {
        if (timer > 0 && getContext() != null) {
            Map<String, Object> map = new HashMap<>();
            String PhoneNumber = MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey());
            map.put("phoneNumber", PhoneNumber);
            map.put("key", BuildConfig.API_KEY);
            Log.d(TAG, "getAPIDataByPhoneData: " + map.toString());
            final String priorBatchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
            final String priorLang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
            MyApplication.resetBatchCodeRefreshFlag();

            MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(
                    Request.Method.GET,
                    CommonMethods.getUrlParam(AV_GET_USER_BY_PHONE, map),
                    null,
                    response -> {
                        try {
                            Log.d("USER_DTO", "User by phone response: " + response.toString());
                            dataLoaded = 1;
                            TypeToken<ApiWrappedResponse<UserDetailsResponse>> token = new TypeToken<ApiWrappedResponse<UserDetailsResponse>>() {
                            };
                            timer--;

//                            boolean lastCampaignSubscribed = MyApplication.getBoolData(PreferenceKeys.CAMPAIGN_SUBSCRIBED.getKey());
                            ApiWrappedResponse<UserDetailsResponse> apiResponse = gson.fromJson(response.toString(), token.getType());
                            userDetailsResponse = (apiResponse != null && apiResponse.isSuccess()) ? apiResponse.getData() : null;
                            Log.i("USER_DTO", userDetailsResponse != null ? userDetailsResponse.toString() : "null");

                            MyApplication.saveUserDto("userDto", userDetailsResponse);

                            String classId = MyApplication.getStringData(PreferenceKeys.CLASS_ID.getKey());
                            String courseId = MyApplication.getStringData(PreferenceKeys.SECONDARY_COURSE_ID.getKey());

                            if (userDetailsResponse != null && userDetailsResponse.getUserDto() != null
                                    && userDetailsResponse.getUserDto().getSwitchableCourseClassInfo() != null) {

                                Log.d("NEET_SWITCH", "SwitchableCourseClassInfo is available" + userDetailsResponse.getUserDto().getSwitchableCourseClassInfo());

                                switchableCourseClassInfo = userDetailsResponse.getUserDto().getSwitchableCourseClassInfo();


                                String targetCourseId = switchableCourseClassInfo.getCourseId();

                                MyApplication.saveData(PreferenceKeys.TARGET_COURSE_ID.getKey(), targetCourseId);

                                String json = gson.toJson(switchableCourseClassInfo);
                                MyApplication.saveData(PreferenceKeys.SWITCHABLE_COURSE_INFO.getKey(), json);

                                boolean isNeetMode = "3".equals(MyApplication.getStringData(PreferenceKeys.SECONDARY_COURSE_ID.getKey()));
                                MyApplication.saveData(PreferenceKeys.NEET_TOGGLE_STATE.getKey(), isNeetMode);


                            } else {
                            }

                            setToolBarUserData();
                            MyApplication.saveData(PreferenceKeys.NAME.getKey(), (userDetailsResponse != null && userDetailsResponse.getUserDto() != null) ? userDetailsResponse.getUserDto().getUsername() : null);

                            String fullName = MyApplication.getStringData(PreferenceKeys.NAME.getKey());
                            if (!StringUtils.isEmpty(fullName)) {
                                for (int i = 0; i < fullName.length(); i++) {
                                    if (fullName.charAt(i) == ' ') {
                                        fullName = fullName.substring(0, i);
                                        break;
                                    }
                                }
                            }

                            boolean editYourNameVisibility = RemoteConfigUtils.isFeatureVisible(RemoteConfigUtils.FeatureVisibilityName.EDIT_YOUR_NAME.getFeature());
                            Log.d(TAG, "getAPIDataByPhoneData---editYourNameVisibility: " + editYourNameVisibility);
                            edit_your_name.setVisibility(
                                    StringUtils.isEmpty(MyApplication.getStringData("name")) &&
                                            editYourNameVisibility
                                            ? View.VISIBLE
                                            : View.GONE
                            );

                            String subscriptionType = (userDetailsResponse != null && userDetailsResponse.getUserDto() != null && userDetailsResponse.getUserDto().getSubscriptionInfo() != null) ? userDetailsResponse.getUserDto().getSubscriptionInfo().getSubscriptionType() : null;
                            String lastSubscriptionType = MyApplication.getStringData(PreferenceKeys.USER_SUBSCRIPTION_TYPE.getKey());

                            MyApplication.saveData(PreferenceKeys.BOARD_SWITCH.getKey(), true);
                            selectedCourseId = MyApplication.getStringData(PreferenceKeys.SECONDARY_COURSE_ID.getKey());

//                            if (isCampaignSubscribed != null && isCampaignSubscribed != campaignSubscribed) {
//                                Log.d("issue", "getAPIDataByPhoneData sync api called :");
//                                mediaViewModel = MyApplication.getInstance().getMediaViewModel();
//                                mediaViewModel.getAllDownloadedMedia(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()),
//                                                DownloadStatus.DOWNLOAD_COMPLETED.name())
//                                        .observe(getViewLifecycleOwner(), list -> {
//                                            if (!list.isEmpty()) {
//                                                syncDownloadedData(list);
//                                            }
//                                        });
//                            }

                            if (subscriptionType!=null && lastSubscriptionType!=null && !subscriptionType.equals(lastSubscriptionType)) {
                                try {
                                    ((HomeScreen) requireActivity()).updateBottomNavForSubscription(false);
                                } catch (Exception ignored) {
                                }
                            }

                            if (userDetailsResponse != null) {
                                MyApplication.saveUserProfileLocally(userDetailsResponse);
                                initBottomStatusView();
                                String newBatchCode = MyApplication.extractActiveBatchCode(userDetailsResponse);
                                if (newBatchCode != null && StringUtils.isNotEmpty(priorBatchCode)
                                        && !newBatchCode.equals(priorBatchCode)
                                        && MyApplication.isBatchChangeAutoRefreshEnabled()
                                        && MyApplication.tryAcquireBatchCodeRefresh()) {
                                    String newLang = MyApplication.extractActiveBatchLanguage(userDetailsResponse);
                                    boolean langChanged = newLang != null && StringUtils.isNotEmpty(priorLang) && !newLang.equals(priorLang);
                                    Log.d(TAG, "batchCode changed: " + priorBatchCode + " → " + newBatchCode
                                            + (langChanged ? ", lang: " + priorLang + " → " + newLang : "") + " (MainDashboard)");
//                                    if (langChanged) {
                                    // Language changed with the batch — delegate full recreation to HomeScreen.
                                    // saveUserProfileLocally already persisted the new lang.
                                    try {
                                        homeScreenViewModel.removeDataModel();
//                                        homeScreenViewModel.refreshHomeScreenData(true);
                                        homeScreenViewModel.triggerBatchCodeRefresh();

                                        MyApplication.updateAppLocale(requireContext(), newLang);
                                        ((HomeScreen) requireActivity()).refreshActivity();
                                    } catch (Exception ignored) {
                                    }
                                    return;
//                                    } else {
//                                        homeScreenViewModel.removeDataModel();
//                                        homeScreenViewModel.refreshHomeScreenData(true);
//                                        homeScreenViewModel.triggerBatchCodeRefresh();
//                                    }
                                }
                                Reference_Code = userDetailsResponse.getUserDto().getMyReferCode();
                            }


                            if (mainDashboardListener != null) {
                                mainDashboardListener.userDataFetchedSuccessfully();
                                if ((userDetailsResponse.getUserDto() != null && userDetailsResponse.getUserDto().getClassId() != null && userDetailsResponse.getUserDto().getClassId().equals(classId)) || (userDetailsResponse.getUserDto() != null && userDetailsResponse.getUserDto().getSecondaryCourseId() != null && userDetailsResponse.getUserDto().getSecondaryCourseId().equals(courseId))) {
                                    mainDashboardListener.userClassCourseUpdated();
                                }
                            }

                            handleCourseSwitchSelection();
                            highlightExtraction();
                            fetchHomeScreenDataFromCache();
                            getSubjectData();
                            textSeriesExtraction();
                            toppersExtraction();
                            getCarouselData();
                            getStudentProgress();
                            getRoutineDataCheckStatus();
                            loadTodayTaskFragment();
                        } catch (Exception ex) {
                            Log.d(TAG, "getAPIDataByPhoneData cactch block : ");
                            ex.printStackTrace();
                        }

                    }, new VolleyGlobalErrorListener(getActivity()) {
                @Override
                public void postError(VolleyError error) {
                    if (!isAdded()) return;
                    Log.d(TAG, "postErrorGETPHONEAPIBYDATA: ");
                    dataLoaded = 0;
                    timer--;
                    // Hide headerShimmer even on failure so it doesn't stay visible indefinitely.
                    // setToolBarUserData() reads from SharedPreferences/RC and is safe to call here.
                    setToolBarUserData();
                    CommonMethods.showNegativeSnackbar(constraintLayout, error.getMessage());
                }
            });
            myJsonObjectRequest2.setTag(TAG);
            MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(myJsonObjectRequest2);

        }

    }


    public void syncDownloadedData(List<DownloadMedia> list) {

        if (!CommonMethods.isOnline(getActivity())) return;

        try {
            List<SyncDownloadRequest> requestList = MediaMapper.fromDownloadMediaList(list);
            JSONArray jsonBody = new JSONArray(new Gson().toJson(requestList));

            MyJsonArrayRequest request = new MyJsonArrayRequest(
                    Request.Method.POST,
                    AV_SYNC_DOWNLOADED_LECTURE_DATA,
                    jsonBody,
                    response -> {
                        try {
                            Type type = new TypeToken<ApiWrappedResponse<List<DownloadedLectureResponse>>>() {
                            }.getType();
                            ApiWrappedResponse<List<DownloadedLectureResponse>> wrapped =
                                    new Gson().fromJson(response.toString(), type);
                            if (wrapped == null || wrapped.getData() == null) return;
                            for (DownloadedLectureResponse lecture : wrapped.getData()) {
                                String microLectureId = lecture.getMicroLectureId();
                                boolean lockedFromApi = lecture.isLocked();
                                mediaViewModel.getMediaByLectureId(microLectureId)
                                        .observe(getActivity(), new Observer<MediaEntity>() {
                                            @Override
                                            public void onChanged(MediaEntity currentMedia) {
                                                if (currentMedia != null) {
                                                    if (currentMedia.isLocked() != lockedFromApi) {
                                                        mediaViewModel.updateByLockedStatusByLectureId(microLectureId, lockedFromApi);
                                                    }
                                                }
                                                mediaViewModel.getMediaByLectureId(microLectureId).removeObserver(this);
                                            }
                                        });
                            }
                        } catch (Exception e) {
                            Log.e("Sync error", "JSON parsing error: " + e.getMessage());
                        }
                    },
                    error -> Log.e("Sync error", "Sync error: " + error.getMessage())
            );
            request.setTag("SYNC_API");
            MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(request);
        } catch (Exception e) {
            Log.e("Sync error", "Request creation error: " + e.getMessage());
        }
    }

    private void getStudentProgress() {
        if (getView() == null) {
            return;
        }
        homeScreenViewModel.getProgress().observe(getViewLifecycleOwner(), this::updateStudentProgress);
    }

    private void updateStudentProgress(StudentProgressDto studentProgressDto) {
    }


    private void setFragments(Fragment analyticFragment) {
        if (isAdded()) {
            FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.main_frame, analyticFragment);
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    private void initializeAllRecyclerView(View view) {
        recyclerViewSubjectProgress = view.findViewById(R.id.progress_view);
        RecyclerViewEdgeEffect.edgeEffectRecyclerView(recyclerViewSubjectProgress, getActivity(), true);

        recyclerViewSub = view.findViewById(R.id.recyclerViewSubject);
        recyclerViewSub.setLayoutManager(new WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        RecyclerViewEdgeEffect.edgeEffectRecyclerView(recyclerViewSub, getActivity(), true);

        recyclerViewClasses = view.findViewById(R.id.recyclerViewClassesLive);
        recyclerViewClasses.setLayoutManager(new WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        RecyclerViewEdgeEffect.edgeEffectRecyclerView(recyclerViewClasses, getActivity(), true);

        recyclerViewTextSeries = view.findViewById(R.id.recyclerViewTestSeries);
        recyclerViewTextSeries.setLayoutManager(new WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        RecyclerViewEdgeEffect.edgeEffectRecyclerView(recyclerViewTextSeries, getActivity(), true);

        recyclerViewhighlight = view.findViewById(R.id.recyclerViewHighlight);
        recyclerViewhighlight.setLayoutManager(new WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        RecyclerViewEdgeEffect.edgeEffectRecyclerView(recyclerViewhighlight, getActivity(), true);
        ((androidx.recyclerview.widget.SimpleItemAnimator) recyclerViewhighlight.getItemAnimator()).setSupportsChangeAnimations(false);

        recyclerViewToppers = view.findViewById(R.id.rv_toppers);
        recyclerViewToppers.setLayoutManager(new WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        RecyclerViewEdgeEffect.edgeEffectRecyclerView(recyclerViewToppers, getActivity(), true);

        featureSectionRv = view.findViewById(R.id.rv_feature_section);
        featureSectionRv.setLayoutManager(new WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        RecyclerViewEdgeEffect.edgeEffectRecyclerView(featureSectionRv, requireContext(), true);
        featureSectionRv.setItemAnimator(null);


        recyclerViewhighlight.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);
                RecyclerView.LayoutManager lm = rv.getLayoutManager();
                if (!(lm instanceof LinearLayoutManager)) return;
                int firstVisible = ((LinearLayoutManager) lm).findFirstVisibleItemPosition();
                rv.post(() -> highlistAdapter.handleScroll(firstVisible));
            }
        });

    }

    private void setAskDoubtVisibility(boolean isVisible) {
        if (ask_relative == null) {
            return;
        }
        if (SubscriptionType.hasActiveSubscription() && isVisible) {
            ask_relative.setVisibility(View.VISIBLE);
        } else {
            ask_relative.setVisibility(View.GONE);
        }
    }

    /**
     * Single entry point for showing or hiding the FAB menu.
     * Use this instead of setting {@code fab_menu} visibility directly anywhere in this fragment.
     *
     * @param visible {@code true} → VISIBLE, {@code false} → INVISIBLE with fade-out animation
     */
    private void setFabMenuVisibility(boolean visible) {
        if (fab_menu == null) return;
        if (SubscriptionType.hasActiveSubscription()) {
            if (visible) {
                fab_menu.setVisibility(View.VISIBLE);
            } else {
                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
                fab_menu.setVisibility(View.INVISIBLE);
                fab_menu.setAnimation(animation);
            }
        } else {
            fab_menu.setVisibility(View.GONE);
        }
    }

    private void initializeAllShimmerFrames(View view) {
        subjects = view.findViewById(R.id.subjects);
        shimmerFeatureSection = view.findViewById(R.id.shimmer_feature_section);
        shimmerSubjects = view.findViewById(R.id.shimmer_subjects);
        shimmerTestSeries = view.findViewById(R.id.shimmer_test_series);
        shimmerClasses = view.findViewById(R.id.shimmer_classes);
        shimmerHighlights = view.findViewById(R.id.shimmer_highlights);
    }

    private void initializeAllAdapters() {
        subjectnameAdapter = new SubjectNameAdapter(sub, this, getActivity());
        recyclerViewSub.setAdapter(subjectnameAdapter);
        testSeriesAdapter = new TestSeriesAdapter(testseries, getActivity(), this);
        recyclerViewTextSeries.setAdapter(testSeriesAdapter);
        highlistAdapter = new HighlistAdapter(highlistModelList, this, getContext());
        recyclerViewhighlight.setAdapter(highlistAdapter);
        classesLiveAdapter = new ClassesLiveAdapter(classeslive, getContext(), this::onclassesClick);
        recyclerViewClasses.setAdapter(classesLiveAdapter);
        toppersAdapter = new ToppersAdapter(getContext(), toppersList, this);
        recyclerViewToppers.setAdapter(toppersAdapter);

        featuresSectionAdapter = new FeaturesSectionAdapter(requireContext(), featureSectionModelsList, this);
        featureSectionRv.setAdapter(featuresSectionAdapter);

        recyclerViewSub.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                updateSubjectArrowsVisibility();
            }
        });
    }


    private void getFeaturesSectionData() {
        final String logTag = "FEATURE_SECTION_LOG";
        if (isFeatureSectionLoading) {
            Log.d(logTag, "Already loading feature sections, skipping duplicate call");
            return;
        }
        isFeatureSectionLoading = true;
        Log.d(logTag, "getFeaturesSectionData: Method called");

        List<HomeScreenCardResponse> cachedData =
                MyApplication.getFeatureSectionData(HomeScreenCacheData.AV_FEATURE_SECTION_DATA.name());
        if (cachedData != null && !cachedData.isEmpty()) {
            Log.d(logTag, "FEATURE_SECTION_DATA " + cachedData.size() + " items");
            featureSectionModelsList.clear();
            featureSectionModelsList.addAll(cachedData);
            featuresSectionAdapter.notifyDataSetChanged();
            featureSectionRv.setVisibility(View.VISIBLE);
            shimmerFeatureSection.stopShimmer();
            shimmerFeatureSection.setVisibility(View.GONE);
            allFeaturesFetched = true;
            playSequentialBorderAnimation();
        } else {
            shimmerFeatureSection.startShimmer();
            Log.d(logTag, "No cached data found");
        }
        Map<String, Object> requestParameters = new HashMap<>();
        requestParameters.put("batchId", MyApplication.getIntData(PreferenceKeys.BATCH_ID.getKey()));
        requestParameters.put("versionCode", DeviceInfoUtil.getVersionCode(requireContext()));
        Log.d(logTag, "Making API request for feature sections : " + requestParameters.toString());
        MyJsonObjectRequest toppersRequest = new MyJsonObjectRequest(
                Request.Method.GET,
                CommonMethods.getUrlParam(AV_HOME_SCREEN_CARDS, requestParameters),
                null,
                response -> {
                    try {
                        TypeToken<ApiWrappedResponse<List<HomeScreenCardResponse>>> token = new TypeToken<ApiWrappedResponse<List<HomeScreenCardResponse>>>() {
                        };
                        ApiWrappedResponse<List<HomeScreenCardResponse>> apiResponse = gson.fromJson(response.toString(), token.getType());
                        List<HomeScreenCardResponse> list = (apiResponse != null && apiResponse.getData() != null) ? apiResponse.getData() : null;
                        Log.d(TAG, "getFeaturesSectionData: " + (list != null ? list.toString() : "null"));

                        if (list != null && !list.isEmpty()) {
                            MyApplication.saveFeatureSectionData(HomeScreenCacheData.AV_FEATURE_SECTION_DATA.name(), list);
                            featureSectionModelsList.clear();
                            featureSectionModelsList.addAll(list);
                            featuresSectionAdapter.notifyDataSetChanged();
                            featureSectionRv.setVisibility(View.VISIBLE);
                            allFeaturesFetched = true;
                            playSequentialBorderAnimation();
                        }
                    } catch (Exception e) {
                        Log.e(logTag, "Parsing error: " + e.getMessage(), e);
                    }

                    shimmerFeatureSection.stopShimmer();
                    shimmerFeatureSection.setVisibility(View.GONE);
                    isFeatureSectionLoading = false;
                },
                new VolleyGlobalErrorListener(getActivity()) {
                    @Override
                    public void postError(VolleyError error) {
                        if (!isAdded()) return;
                        if (CollectionUtils.isEmpty(cachedData)) {
                            Log.e(logTag, "API error: " + error.toString());
                            featureSectionRv.setVisibility(View.GONE);
                            // Stop shimmer on error — without this it stays visible forever
                            // when there is no cached data (e.g. first-time users).
                            shimmerFeatureSection.stopShimmer();
                            shimmerFeatureSection.setVisibility(View.GONE);
                            isFeatureSectionLoading = false;
                        }
                    }
                }
        );

        toppersRequest.setTag("FEATURE_SECTION_API_TAG");
        Log.d(logTag, "Adding request to queue with tag: FEATURE_SECTION_API_TAG");
        MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(toppersRequest);
    }


    private void highlightExtraction() {
        highlistModelList.clear();
        List<HighlistModel> cachedHighlights = MyApplication.getHighlistData(HomeScreenCacheData.AV_HIGHLIGHT_CACHE_KEY.name());
        if (!cachedHighlights.isEmpty()) {
            Log.d("HighlightExtraction", "Using cached highlights.");
            homeScreenViewModel.setHighlightClassList(cachedHighlights);
            highlistModelList.addAll(cachedHighlights);
            highlistAdapter.notifyDataSetChanged();
            recyclerViewhighlight.setVisibility(View.VISIBLE);
            shimmerHighlights.stopShimmer();
            shimmerHighlights.setVisibility(View.GONE);
        }
        Map<String, Object> highlightRequestParams = new HashMap<>();
        try {
            highlightRequestParams.put("type", BatchVideoType.VIRAL_VIDEOS.name());
        } catch (Exception ignored) {
        }
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(AV_DASHBOARD_VIDEO, highlightRequestParams), null,
                response -> {
                    try {
                        JSONArray arrayData = response.getJSONArray("data");
                        Gson gson = new Gson();
                        TypeToken<List<VideoResponse>> token = new TypeToken<>() {
                        };
                        List<VideoResponse> videoList = gson.fromJson(arrayData.toString(), token);
                        Log.d("HighlightExtraction", "API CALL Highlights." + videoList);
                        if (videoList == null || videoList.isEmpty()) {
                            recyclerViewhighlight.setVisibility(View.GONE);
                            highlightsText.setVisibility(View.INVISIBLE);
                            shimmerHighlights.stopShimmer();
                            shimmerHighlights.setVisibility(View.GONE);
                            return;
                        }
                        List<HighlistModel> list = new ArrayList<>();
                        for (VideoResponse video : videoList) {
                            HighlistModel model = new HighlistModel();
                            model.setId(video.getId() != null ? String.valueOf(video.getId()) : null);
                            model.setImageUrl(video.getImageUrl());
                            model.setVideoUrl(video.getVideoUrl());
                            model.setVideoType(video.getOrientation());
                            list.add(model);
                        }
                        MyApplication.saveHighlightsData(HomeScreenCacheData.AV_HIGHLIGHT_CACHE_KEY.name(), list);
                        homeScreenViewModel.setHighlightClassList(list);
                        highlistModelList.clear();
                        highlistModelList.addAll(list);
                        highlistAdapter.notifyDataSetChanged();
                        recyclerViewhighlight.setVisibility(View.VISIBLE);
                        shimmerHighlights.stopShimmer();
                        shimmerHighlights.setVisibility(View.GONE);
                    } catch (Exception e) {
                        // Parsing failed — still stop shimmer so it doesn't stay indefinitely.
                        if (isAdded()) {
                            shimmerHighlights.stopShimmer();
                            shimmerHighlights.setVisibility(View.GONE);
                        }
                    }
                }, new VolleyGlobalErrorListener(getActivity()) {
            @Override
            public void postError(VolleyError error) {
                if (!isAdded()) return;
                dataLoaded = 0;
                if (cachedHighlights.isEmpty()) {
                    // No cache and API failed — stop shimmer so it doesn't stay visible forever.
                    shimmerHighlights.stopShimmer();
                    shimmerHighlights.setVisibility(View.GONE);
                    recyclerViewhighlight.setVisibility(View.GONE);
                }
                CommonMethods.showNegativeSnackbar(constraintLayout, error.getMessage());
            }
        });
        myJsonObjectRequest2.setTag(TAG);
        MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(myJsonObjectRequest2);


    }

    private void textSeriesExtraction() {
        if (getView() == null) {
            return;
        }

        // If the API already completed before this fragment attached (e.g. re-navigation),
        // hide shimmer immediately — no emission will come for the loading state.
        if (homeScreenViewModel.isTestSeriesApiEverCompleted()) {
            shimmerTestSeries.stopShimmer();
            shimmerTestSeries.setVisibility(View.GONE);
        }

        // Loading state drives shimmer visibility exclusively
        homeScreenViewModel.getTestSeriesLoading().observeForever(loading -> {
            if (loading == null) return;
            if (loading) {
                shimmerTestSeries.setVisibility(View.VISIBLE);
                shimmerTestSeries.startShimmer();
            } else {
                shimmerTestSeries.stopShimmer();
                shimmerTestSeries.setVisibility(View.GONE);
            }
        });

        homeScreenViewModel.getTestSeriesList().observeForever(this::updateTestSeriesList);
    }

    private void updateTestSeriesList(TestSeriesCardResponse response) {
        Log.d(TAG, "updateTestSeriesList: " + response);

        List<TestSeriesCard> testSeriesCards = (response != null && response.getTestSeries() != null)
                ? response.getTestSeries() : new ArrayList<>();

        try {
            if (RemoteConfigUtils.isFeatureHidden(RemoteConfigUtils.FeatureVisibilityName.HOME_SCREEN_TEST_SERIES.getFeature())) {
                cv_test_card.setVisibility(View.GONE);
                return;
            }
        } catch (Exception ignored) {
        }

        if (testSeriesCards.isEmpty()) {
            cv_test_card.setVisibility(View.GONE);
        } else {
            testseries.clear();
            testseries.addAll(testSeriesCards);
            recyclerViewTextSeries.setVisibility(View.VISIBLE);
            totalTestsCount = 0;
            cv_test_card.setVisibility(View.VISIBLE);
            testSeriesAdapter.notifyDataSetChanged();
        }
    }

    private void getSubjectData() {
        Log.d("getSubscribedSubjectData", "onCreateObserver");

        if (getView() != null) {
            homeScreenViewModel.getSubjectList().observeForever(this::updateSubjectView);
            homeScreenViewModel.getOptionalSubjectAvailable().observeForever(available -> {
                if (!isAdded()) return;
                subjectnameAdapter.setShowAddCard(Boolean.TRUE.equals(available));
            });
            subjectnameAdapter.setOnAddSubjectClick(this::openOptionalSubjectDialog);
        } else {
            Log.w("getSubjectData", "Fragment view is not available yet.");
        }
    }

    private void openOptionalSubjectDialog() {
        if (!isAdded()) return;
        if (!MyApplication.isNetworkAvailable()) {
            Toast.makeText(requireContext(), getString(R.string.error_no_internet_connection), Toast.LENGTH_SHORT).show();
            return;
        }
        OptionalSubjectBottomSheet sheet = new OptionalSubjectBottomSheet();
        sheet.setOnSubjectsAddedListener(() -> {
            if (!isAdded()) return;
            homeScreenViewModel.getSubscribedSubjectData(true);
        });
        sheet.show(getChildFragmentManager(), "OptionalSubjectBottomSheet");
    }

    @SuppressLint("SetTextI18n")
    private void updateSubjectView(List<SubjectResponse> subjectModels) {
        sub.clear();

        if (subjectModels.isEmpty()) {
            shimmerSubjects.startShimmer();
            shimmerSubjects.setVisibility(View.VISIBLE);
            recyclerViewSub.setVisibility(View.GONE);
            subjectnameAdapter.notifyDataSetChanged();
//            cv_subjects.setVisibility(View.GONE);
            return;
        }

        sub.addAll(subjectModels);
        recyclerViewSub.post(this::updateSubjectArrowsVisibility);

        shimmerSubjects.stopShimmer();
        shimmerSubjects.setVisibility(View.GONE);
        recyclerViewSub.setVisibility(View.VISIBLE);
//        cv_subjects.setVisibility(View.VISIBLE);
        String lectureTitle = MyApplication.getInstance().getString(R.string.lecture_title);
        String gradeCode = MyApplication.getStringData(PreferenceKeys.GRADE_CODE.getKey());
        String yearCode = MyApplication.getStringData(PreferenceKeys.YEAR_CODE.getKey());
        String gradeYearKey = gradeCode + "_" + yearCode;

        if (MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey())) {
            if (isAdded()) {
                String subjectNameJsonStr = MyApplication.getFirebaseRemoteConfig()
                        .getString("subject_text_title_batch_"
                                + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));

                String lectureText = getString(R.string.lectures); // fallback

                String examCode = MyApplication.getStringData(PreferenceKeys.EXAM_CODE.getKey());

                try {
                    JSONObject mainJson = new JSONObject(subjectNameJsonStr);

                    JSONObject gradeObject = mainJson.has(gradeYearKey)
                            ? mainJson.getJSONObject(gradeYearKey)
                            : mainJson.optJSONObject("default");

                    if (gradeObject != null) {
                        if (gradeObject.has(examCode)) {
                            lectureText = gradeObject.getString(examCode);
                            Log.d(TAG, "----------> Subject name : " + lectureText);
                        } else if (gradeObject.has("default")) {
                            lectureText = gradeObject.getString("default");
                        }
                    }

                    Log.d(TAG, "----> Subject name : " + lectureText + ", " + examCode + ", " + gradeYearKey + ", " + subjectNameJsonStr);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                subjects.setText(lectureText);
            }
        } else {
        }

        try {
            subjectnameAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            recyclerViewSub.setAdapter(subjectnameAdapter);
        }
    }

    private void toppersExtraction() {
        toppersList.clear();
        List<ToppersHomeScreenModel> cachedToppers = MyApplication.getToppersData(HomeScreenCacheData.AV_TOPPERS_DATA.name());
        if (cachedToppers != null && !cachedToppers.isEmpty()) {
            Log.d("toppersExtraction", "Using cached toppers data.");
            homeScreenViewModel.setToppersDataLive(cachedToppers);
            toppersList.addAll(cachedToppers);
            toppersAdapter.notifyDataSetChanged();
            recyclerViewToppers.setVisibility(View.VISIBLE);
            toopersCardLayout.setVisibility(View.VISIBLE);
            toppersShimmer.stopShimmer();
            toppersShimmer.setVisibility(View.GONE);
            shimmerSubjects.setVisibility(View.GONE);
        }
        setToppersLayoutText();
        Map<String, Object> toppersRequestParams = new HashMap<>();
        try {
            toppersRequestParams.put("type", BatchVideoType.CHAMPIONS_OF_ARIVIHAN.name());
        } catch (Exception ignored) {
        }

        Log.d(TAG, "--> Video Request: " + toppersRequestParams.toString());
        MyJsonObjectRequest toppersRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(AV_DASHBOARD_VIDEO, toppersRequestParams), null,
                response -> {
                    try {
                        JSONArray toppersArray = response.getJSONArray("data");
                        TypeToken<List<VideoResponse>> token = new TypeToken<List<VideoResponse>>() {
                        };
                        List<VideoResponse> videoList = gson.fromJson(toppersArray.toString(), token.getType());

                        if (videoList == null || videoList.isEmpty()) {
                            recyclerViewToppers.setVisibility(View.GONE);
                            toopersCardLayout.setVisibility(View.GONE);
                            shimmerSubjects.setVisibility(View.GONE);
                            toppersShimmer.stopShimmer();
                            toppersShimmer.setVisibility(View.GONE);
                            return;
                        }
                        List<ToppersHomeScreenModel> list = new ArrayList<>();
                        for (VideoResponse video : videoList) {
                            ToppersHomeScreenModel model = new ToppersHomeScreenModel();
                            model.setPosterUrl(video.getImageUrl());
                            model.setVideoUrl(video.getVideoUrl());
                            list.add(model);
                        }
                        MyApplication.saveToppersData(HomeScreenCacheData.AV_TOPPERS_DATA.name(), list);
                        homeScreenViewModel.setToppersDataLive(list);
                        toppersList.clear();
                        toppersList.addAll(list);
                        toppersAdapter.notifyDataSetChanged();
                        recyclerViewToppers.setVisibility(View.VISIBLE);
                        toopersCardLayout.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        Log.d("toppersExtraction", "Parsing error: " + e.getMessage());
                    }

                    toppersShimmer.stopShimmer();
                    toppersShimmer.setVisibility(View.GONE);
                    shimmerSubjects.setVisibility(View.GONE);
                },
                new VolleyGlobalErrorListener(getActivity()) {
                    @Override
                    public void postError(VolleyError error) {
                        if (cachedToppers == null || cachedToppers.isEmpty()) {
                            toppersShimmer.stopShimmer();
                            toppersShimmer.setVisibility(View.GONE);
                            shimmerSubjects.setVisibility(View.GONE);
                            toopersCardLayout.setVisibility(View.GONE);
                            Log.d("toppersExtraction", "API error: " + error.getMessage());
                        }
                    }
                });

        toppersRequest.setTag("TOPPERS_API_TAG");
        MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(toppersRequest);
    }

    private void setToppersLayoutText() {
        String language = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        String examCode = MyApplication.getStringData(PreferenceKeys.EXAM_CODE.getKey());
        String gradeCode = MyApplication.getStringData(PreferenceKeys.GRADE_CODE.getKey());

        String remoteJson = MyApplication.getFirebaseRemoteConfig().getString("toppers_layout_batch_text_v2");

        try {
            JSONObject root = new JSONObject(remoteJson);

            JSONObject gradeObj = (gradeCode != null && !gradeCode.isEmpty() && root.has(gradeCode))
                    ? root.getJSONObject(gradeCode)
                    : root.optJSONObject("default");
            JSONObject examObj = (gradeObj != null && gradeObj.has(examCode))
                    ? gradeObj.getJSONObject(examCode)
                    : (gradeObj != null ? gradeObj.optJSONObject("default") : null);

            if (examObj != null && examObj.has(language)) {
                JSONObject langObj = examObj.getJSONObject(language);

                String title = langObj.optString("title", "");
                String subtitle = langObj.optString("subtitle", "");

                Log.d("TopperLayout", "Title: " + title);
                Log.d("TopperLayout", "Subtitle: " + subtitle);
                toppersLayoutTitle.setText(title);
                toppersLayoutSubTitle.setText(subtitle);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("TopperLayout", "Invalid JSON format in toppers_layout_batch_text_v2");
        }
    }

    @Override
    public void onHighlightsClick(int position) {
        try {
            if (highlistModelList != null && position < highlistModelList.size()) {
                HighlistModel highlistModel = highlistModelList.get(position);
                VideoOrientationType videoType = VideoOrientationType.fromString(highlistModel.getVideoType());
                Intent intent;
                switch (videoType) {
                    case REELS, PORTRAIT:
                        intent = new Intent(requireActivity(), ReelsActivity.class);
                        intent.putExtra("viral_video_url", highlistModel.getVideoUrl());
                        break;
                    case VIDEO, LANDSCAPE, HORIZONTAL:
                    default:
                        intent = new Intent(getActivity(), ExoPlayerActivity.class);
                        intent.putExtra(KEY_VIDEO_URI, highlistModel.getVideoUrl());
                        break;
                }
                destroyHowTo();
                startActivity(intent);
                Map<String, String> params = new HashMap<>();
                params.put("videoUrl", highlistModel.getVideoUrl());
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.clicked_highlight_section.getActionName(), params);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.hs_clicked_highlight_section.getActionName(), params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onsubjectClick(int position) {
        if (sub == null || position >= sub.size()) {
            return;
        }
        try {
            SubjectResponse subjectModel = sub.get(position);
            String subjectName = sub.get(position).getSubjectName();
            String icon = sub.get(position).getIconUrl();


            Map<String, String> params = new HashMap<>();
            params.put("SubjectName", subjectName);
            params.put("SubjectId", subjectModel.getId());
            params.put("Screen", getClass().getSimpleName());
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.subject_click.getActionName(), params);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.hs_subject_click.getActionName(), params);

            ActivityNavigationConfig navigationConfig = MyApplication.getBottomNavActivityConfig();

            String title = null;
            if (navigationConfig != null && navigationConfig.getParams() != null) {
                title = (String) navigationConfig.getParams().getOrDefault("title", getString(R.string.lectures));
            }
            Log.d(TAG, "onsubjectClick: " + title);

            CommonNavigator.navigateToChapterListByBatchActivity(requireContext(), subjectModel.getId(), subjectName, subjectModel.getSubjectCode(), TAG, StringUtils.isEmpty(title) ? getString(R.string.lectures) : title, null);
//            if(SubscriptionType.isBasicSubscription()) {
//                Intent intent = new Intent(getActivity(), ChapterListByBatchActivity.class);
//                intent.putExtra("SubjectName", subjectName);
//                intent.putExtra("SubjectId", subjectModel.getId());
//                intent.putExtra("Icon", icon);
//                intent.putExtra("title", StringUtils.isEmpty(title) ? getString(R.string.lectures) : title);
//                intent.putExtra("SubjectCode", subjectModel.getSubjectCode());
//
//                startActivity(intent);
//            } else {
//                Intent intent = new Intent(getActivity(), ChapterListSubscribedActivity.class);
//                intent.putExtra("SubjectName", subjectName);
//                intent.putExtra("SubjectId", subjectModel.getId());
//                intent.putExtra("Icon", icon);
//                intent.putExtra("title", StringUtils.isEmpty(title) ? getString(R.string.lectures) : title);
//                intent.putExtra("SubjectCode", subjectModel.getSubjectCode());
//
//                startActivity(intent);
//            }
            destroyHowTo();
        } catch (Exception e) {

        }
    }

    @Override
    public void onclassesClick(int position) {
        try {
            ClassesLiveModel cl = classeslive.get(position);
            if (getContext() != null && cl.isLocked()) {
                dialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onprogressClick(int position) {
        HomeScreen.Analytic_fragment_clicked afc = new HomeScreen.Analytic_fragment_clicked();
        afc.analytic_frag();
        setFragments(new AnalyticFragment());
    }


    @Override
    public void _onAttach(Context context) {
        this.mContext = context;
        mDataBack = (DataBack) mContext;
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
        try {
            mainDashboardListener = (MainDashboardListener) context;
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onTestSeriesSetClick(TestSeriesCard testSeriesCard) {
        destroyHowTo();
        CommonNavigator.navigateToTestSeriesListFromCard(requireActivity(), testSeriesCard, "", TAG);
    }

    @Override
    public void calculateCount(TestSeriesCard t) {
        totalTestsCount += t.getTotalTestCount() != null ? t.getTotalTestCount() : 0;
        MyApplication.saveData(PreferenceKeys.TOTAL_TESTS_COUNT.getKey(), totalTestsCount);
    }

    @Override
    public void hideContinueSection() {
        hideContinue = true;
        if (classes_in_progress != null) {
            classes_in_progress.setVisibility(View.GONE);
        }
        // Today class data just became available — re-evaluate the animation sequence so that
        // a BATCH_OVERVIEW tag that was skipped (data not loaded yet) is now handled correctly.
//        playSequentialBorderAnimation();
    }

    @Override
    public void hideBadgeView() {
    }

    @Override
    public void onAnimatedCardTagClicked(String tag) {
        markAnimationTagAsClicked(tag);
    }

    @Override
    public void onDataFetched(TodayTaskFragment source, int size) {
        try {
            // Reject stale callbacks: if showTodayTaskFragment() has already replaced this
            // fragment, todayTaskFragment points to the newer instance. Accepting a callback
            // from the old fragment would set todayTaskFetched=true while todayTaskFragment
            // has no data yet, causing hasItemWithTag() to return false and the animation
            // sequence to fall through to the features section instead of today's class.
            if (source != todayTaskFragment) return;
            todayTaskFetched = true;
            playSequentialBorderAnimation();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onTopperClick(String videoUrl) {
        try {
            if (StringUtils.isNotEmpty(videoUrl)) {
                Intent intent = new Intent(requireActivity(), ReelsActivity.class);
                intent.putExtra(KEY_VIDEO_URI, videoUrl);
                Map<String, String> params = new HashMap<>();
                params.put("video_url", videoUrl);
                params.put("open_from", "HomeScreen");
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.champions_of_arivihan_clicked.getActionName(), params);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.hs_champions_of_arivihan_clicked.getActionName(), params);
                startActivity(intent);
            }
        } catch (Exception ignored) {
        }

    }

    private void destroyHowTo() {
        try {
            if (HowToVideoView.getInstance(requireActivity()) != null)
                HowToVideoView.getInstance(requireActivity()).remove();
        } catch (Exception ignored) {
        }
    }

    public interface DataBack {
        void isClicked(boolean clicked);
    }

    public interface MainDashboardListener {
        void onChangeTodoVisibility(boolean isVisible);

        void userDataFetchedSuccessfully();

        void userClassCourseUpdated();
    }

    private void applyToggleState(boolean neetMode) {

        if (switchableCourseClassInfo != null &&
                CommonMethods.isValidJson(switchableCourseClassInfo.getSwitchText())) {

            tvSwitchLabel.setText(
                    StyledTextBuilder.fromJson(
                            requireContext(),
                            switchableCourseClassInfo.getSwitchText()
                    )
            );
        }

    }

    private void showSwitchConfirmation(boolean switchToNeet) {
        MyApplication.
                logFirebaseBundleCreation(FirebaseEvents.home_switch_to_neet_btn_click.getActionName());
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.neet_board_switch_dialog);

        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;

        dialog.getWindow().setBackgroundDrawable(
                AppCompatResources.getDrawable(requireActivity(),
                        R.drawable.bachground_custom_dialogu_typeactivity)
        );

        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        dialog.setCancelable(false);

        TextView tvTitle = dialog.findViewById(R.id.tv_logout_title);
        TextView tvBody = dialog.findViewById(R.id.tv_logout_body);
        ImageView close = dialog.findViewById(R.id.iv_close_pop_up);

        MaterialCardView cvYes = dialog.findViewById(R.id.cv_yes);
        MaterialCardView cvNo = dialog.findViewById(R.id.cv_no);

        if (switchToNeet) {

            tvTitle.setText(getString(R.string.switch_to_neet));
            tvBody.setText(getString(R.string.switch_to_neet_subtitle));

        } else {

            tvTitle.setText(getString(R.string.switch_to_board));
            tvBody.setText(getString(R.string.switch_to_baord_subtitle));
        }

        cvYes.setOnClickListener(v -> {
            dialog.dismiss();
            MyApplication.
                    logFirebaseBundleCreation(FirebaseEvents.home_switch_neet_yes_btn_click.getActionName());
            LoaderOverlay.TargetMode mode =
                    switchToNeet ?
                            LoaderOverlay.TargetMode.TO_NEET :
                            LoaderOverlay.TargetMode.TO_BOARD;

            LoaderOverlay.show(requireContext(), mode);

            SwitchToTargetCourse(switchToNeet);
        });

        cvNo.setOnClickListener(v -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.home_switch_board_no_btn_click.getActionName());
            dialog.dismiss();
        });
        close.setOnClickListener(v -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.home_switch_board_popup_cross_click.getActionName());
            dialog.dismiss();
        });

        dialog.show();
    }




    private void SwitchToTargetCourse(boolean switchToNeet) {

        if (isSwitchInProgress) return;

        isSwitchInProgress = true;

        try {

            String targetCourseId = MyApplication.getStringData(PreferenceKeys.TARGET_COURSE_ID.getKey());

            String classId = switchableCourseClassInfo != null
                    ? switchableCourseClassInfo.getClassId()
                    : null;

            JSONObject requestBody = new JSONObject();

            requestBody.put(PreferenceKeys.COURSE_ID.getKey(), targetCourseId);
            requestBody.put(PreferenceKeys.CLASS_ID.getKey(), classId);

            MyJsonObjectRequest request = new MyJsonObjectRequest(
                    Request.Method.POST,
                    SWITCH_TO_TARGET_COURSE,
                    requestBody,
                    response -> {
                        Log.d(TAG, "Switch API Success: " + response);
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {

                            LoaderOverlay.dismiss();

                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.home_switch_to_neet_completed.getActionName());

                            MyApplication.saveSharedPreferencesDoNotDelete(PreferenceKeys.SELECTED_COURSE_ID.getKey(), targetCourseId);

                            refreshHomeScreen();

                            isSwitchInProgress = false;

                        }, 3000);
                    },

                    error -> {

                        Log.e(TAG, "Switch API Failed", error);

                        LoaderOverlay.dismiss();

                        isSwitchInProgress = false;

                        String message = "Something went wrong";

                        if (error.networkResponse != null) {

                            int statusCode = error.networkResponse.statusCode;

                            if (statusCode >= 500) {
                                message = "Server error. Please try again.";
                            } else if (statusCode == 401) {
                                message = "Session expired. Please login again.";
                            } else if (statusCode == 400) {
                                message = "Invalid request.";
                            }

                        } else if (error instanceof NoConnectionError) {
                            message = "No internet connection.";
                        }

                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();

                    }
            );

            MyVolleySingleton.getInstance(requireContext())
                    .addToRequestQueue(request);

        } catch (Exception e) {

            Log.e(TAG, "Exception in SwitchToTargetCourse()", e);

            LoaderOverlay.dismiss();

            isSwitchInProgress = false;

            Toast.makeText(
                    requireContext(),
                    "Unexpected error occurred",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void refreshHomeScreen() {
        if (getActivity() == null) return;
        Intent intent = new Intent(requireActivity(), HomeScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        homeScreenViewModel.removeDataModel();

        startActivity(intent);
        requireActivity().finish();
    }

    private void showInputDialog(Context context) {
        if (context == null) return;
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_name_input);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        dialog.setCancelable(true);

        MaterialCardView cardView = dialog.findViewById(R.id.name_card);
        EditText editText = dialog.findViewById(R.id.user_name);
        editText.requestFocus();
        CardView submitCard = dialog.findViewById(R.id.next_card);
        TextView submit = dialog.findViewById(R.id.submit);
        TextView nameError = dialog.findViewById(R.id.name_error);


        //name config
        UserNameValidationConfig config = UserNameValidationConfig.fromRemoteConfig();
        updateUiState(context, config, cardView, editText, nameError, submitCard, submit, false);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateUiState(context, config, cardView, editText, nameError, submitCard, submit, true);
            }
        });

        dialog.setOnDismissListener(dialogInterface -> {
            Log.d(TAG, "showInputDialog dismissed");

            Map<String, String> map = new HashMap<>();
            map.put("source", "home_screen");

            MyApplication.logFirebaseBundleCreation(
                    FirebaseEvents.home_name_popup_close_click.getActionName()
            );
        });

        submitCard.setOnClickListener(v -> {
            String input = editText.getText().toString().trim();
            updateUiState(context, config, cardView, editText, nameError, submitCard, submit, true);
            if (!submitCard.isEnabled()) return;

            try {
                Gson gson = new Gson();
                UserDto userDto = MyApplication.getUserDto("userDto");
                if (userDto == null) userDto = new UserDto();
                userDto.setUsername(input);

                MyJsonObjectRequest request = new MyJsonObjectRequest(
                        Request.Method.PATCH,
                        PUT_UPDATE,
                        new JSONObject(gson.toJson(userDto)),
                        response -> {
                            try {
                                if (Boolean.parseBoolean(response.get("success").toString())) {
                                    dialog.dismiss();
                                    MyApplication.saveData(PreferenceKeys.NAME.getKey(), input);
                                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.name_input_popup_submit_click.getActionName());
                                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.hs_name_input_popup_submit_click.getActionName());
                                    if (!isAdded() || getContext() == null) return;
                                    fetchHomeScreenUI();
                                    edit_your_name.setVisibility(View.GONE);
                                    CommonMethods.showPositiveSnackbar(constraintLayout, getString(R.string.name_updated_mesaaage));  // safe now
                                } else {
                                    if (!isAdded() || getContext() == null) return;
                                    Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "showInputDialog: ", e);
                            }
                        },
                        new VolleyGlobalErrorListener(context) {
                            @Override
                            public void postError(VolleyError error) {
                            }
                        }
                );

                MyVolleySingleton.getInstance(context).addToRequestQueue(request);

            } catch (JSONException e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }


    private void updateUiState(
            Context context,
            UserNameValidationConfig config,
            MaterialCardView cardView,
            EditText editText,
            TextView nameError,
            CardView buttonView,
            TextView buttonText,
            boolean showError
    ) {
        String input = editText.getText().toString().trim();
        String error = config.getValidationError(input, getString(R.string.name_required_error));
        boolean isValid = error == null;

        //error
        if (showError && !isValid) {
            nameError.setText(UserNameValidationConfig.fromHtml(error));
            nameError.setVisibility(View.VISIBLE);
            cardView.setStrokeColor(ContextCompat.getColor(context, R.color.input_name_stroke_error));
            cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.input_name_bg_error));
        } else {
            nameError.setVisibility(View.INVISIBLE);
            cardView.setStrokeColor(ContextCompat.getColor(context, R.color.input_name_stroke_default));
            cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.input_name_bg_default));
        }

        //btn
        buttonView.setEnabled(isValid);
        buttonView.setCardBackgroundColor(ContextCompat.getColor(context, isValid ? R.color.btn_enabled_bg : R.color.btn_disabled_bg));
        buttonText.setTextColor(ContextCompat.getColor(context, isValid ? R.color.btn_enabled_text : R.color.btn_disabled_text));
    }


    private void startPulseAnimation() {
        pulseAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(
                requireActivity(), R.animator.pulse_animation);
        pulseAnimator.setTarget(cv_join_button_right);
        pulseAnimator.start();
    }


    private void updateHeaderChips(
            ExamUiDataResponse details,
            UserDetailsResponse userDetails
    ) {
        if (!isAdded() || llChipsRow == null) return;

        boolean isSubscribed =
                SubscriptionNavigationHelper.hasActiveSubscriptionByBatch();

        if (isSubscribed) {
            llChipsRow.setVisibility(View.GONE);
            return;
        }

        if (details == null && userDetails == null) {
            llChipsRow.setVisibility(View.GONE);
            return;
        }

        // Hide all chip cards initially
        cardChipBoard.setVisibility(View.GONE);
        cardChipStream.setVisibility(View.GONE);
        cardChipClass.setVisibility(View.GONE);

        // Use headerTags from API if available
        if (details != null
                && details.getHeaderTags() != null
                && !details.getHeaderTags().isEmpty()) {

            List<String> tags = details.getHeaderTags();

            if (tags.size() >= 1 && StringUtils.isNotEmpty(tags.get(0))) {
                chipBoard.setText(tags.get(0));
                cardChipBoard.setVisibility(View.VISIBLE);
            }

            if (tags.size() >= 2 && StringUtils.isNotEmpty(tags.get(1))) {
                chipStream.setText(tags.get(1));
                cardChipStream.setVisibility(View.VISIBLE);
            }

            if (tags.size() >= 3 && StringUtils.isNotEmpty(tags.get(2))) {
                chipClass.setText(tags.get(2));
                cardChipClass.setVisibility(View.VISIBLE);
            }

            llChipsRow.setVisibility(View.VISIBLE);
            return;
        }

        // Fallback to original logic if headerTags are not present
        UserDetailsResponse.BatchDetail activeBatch = null;

        if (userDetails != null && userDetails.getBatches() != null) {
            for (UserDetailsResponse.BatchDetail batch : userDetails.getBatches()) {
                if (Boolean.TRUE.equals(batch.getActive())) {
                    activeBatch = batch;
                    break;
                }
            }
        }

        // Board chip — onboarding name e.g. "UP Board"
        OnBoardingSelectionModel model =
                PreferenceMethods.getSelectedOnboardingModel();

        if (model != null && !StringUtils.isEmpty(model.getName())) {
            chipBoard.setText(model.getName());
            cardChipBoard.setVisibility(View.VISIBLE);
        }

        // Stream chip — streamName e.g. "PCM"
        if (activeBatch != null
                && !StringUtils.isEmpty(activeBatch.getStreamCode())) {

            chipStream.setText(activeBatch.getStreamCode());
            cardChipStream.setVisibility(View.VISIBLE);
        }

        // Class chip — gradeName e.g. "12th"
        if (activeBatch != null
                && !StringUtils.isEmpty(activeBatch.getGradeName())) {

            chipClass.setText(activeBatch.getGradeName());
            cardChipClass.setVisibility(View.VISIBLE);
        }

        boolean anyVisible =
                cardChipBoard.getVisibility() == View.VISIBLE
                        || cardChipStream.getVisibility() == View.VISIBLE
                        || cardChipClass.getVisibility() == View.VISIBLE;

        llChipsRow.setVisibility(
                anyVisible ? View.VISIBLE : View.GONE
        );
    }


}


