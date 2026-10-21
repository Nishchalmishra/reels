package arivihan.technologies.doubtbuzzter2.fragments.competitive.home;

import static arivihan.technologies.doubtbuzzter2.activity.MicrolectureListActivity.NOTIFICATION_PERMISSION_REQUEST_CODE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_HOME_SCREEN_CARDS;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.CONFIGURATION;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_DOUBT_MICROLECTURE_CONTENT;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_EXAM_UI_DETAILS;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_DASHBOARD_VIDEO;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_GET_USER_BY_PHONE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.SWITCH_TO_TARGET_COURSE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_SYNC_DOWNLOADED_LECTURE_DATA;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
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
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.denzcoskun.imageslider.ImageSlider;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.HalfYearlyActivity;
import arivihan.technologies.doubtbuzzter2.activity.MyDownloadActivity;
import arivihan.technologies.doubtbuzzter2.activity.asat.ASATActivity;
import arivihan.technologies.doubtbuzzter2.ReelsActivity;
import arivihan.technologies.doubtbuzzter2.activity.ChatScreenChooseSubjectForChat;
import arivihan.technologies.doubtbuzzter2.activity.ExoPlayerActivity;
import arivihan.technologies.doubtbuzzter2.activity.GlobalSearchActivity;
import arivihan.technologies.doubtbuzzter2.activity.HomeScreen;
import arivihan.technologies.doubtbuzzter2.activity.QuickNotesActivity;
import arivihan.technologies.doubtbuzzter2.activity.VideoPlayerActivity;
import arivihan.technologies.doubtbuzzter2.activity.competitive.FeatureInfoVideoActivity;
import arivihan.technologies.doubtbuzzter2.adapter.HighlistAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.SortSubjectNameAnalyticAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.ToppersAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.competitive.FeaturesSectionAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.competitive.PersonalizedSubjectAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.common.PosterPopUp;
import arivihan.technologies.doubtbuzzter2.customView.DialogListener;
import arivihan.technologies.doubtbuzzter2.enums.AnalyticsEvent;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.batch.notes.BatchNotesScreenType;
import arivihan.technologies.doubtbuzzter2.enums.batch.videos.VideoOrientationType;
import arivihan.technologies.doubtbuzzter2.customView.CustomBottomStatusView;
import arivihan.technologies.doubtbuzzter2.customView.HowToVideoView;
import arivihan.technologies.doubtbuzzter2.fragments.AnalyticFragment;
import arivihan.technologies.doubtbuzzter2.fragments.BaseFragment;
import arivihan.technologies.doubtbuzzter2.fragments.CarouselFragment;
import arivihan.technologies.doubtbuzzter2.fragments.TodayTaskFragment;
import arivihan.technologies.doubtbuzzter2.helper.ActivityLauncher;
import arivihan.technologies.doubtbuzzter2.helper.DeviceInfoUtil;
import arivihan.technologies.doubtbuzzter2.helper.EnvironmentManager;
import arivihan.technologies.doubtbuzzter2.helper.RemoteConfigUtils;
import arivihan.technologies.doubtbuzzter2.helper.ScreenAnalyticsHelper;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.helper.WrapContentLinearLayoutManager;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
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
import arivihan.technologies.doubtbuzzter2.model.OnBoardingSelectionModel;
import arivihan.technologies.doubtbuzzter2.model.subject.SubjectResponse;
import arivihan.technologies.doubtbuzzter2.model.SwitchUserClassCourseRequestDto;
import arivihan.technologies.doubtbuzzter2.model.ToppersHomeScreenModel;
import arivihan.technologies.doubtbuzzter2.model.batch.user.UserDetailsResponse;
import arivihan.technologies.doubtbuzzter2.model.carousel.CarouselResponse;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.model.competitive.HomeScreenCardResponse;
import arivihan.technologies.doubtbuzzter2.enums.batch.videos.BatchVideoType;
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
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceMethods;

public class CompetitiveHomeScreenFragment extends BaseFragment implements
        ToppersAdapter.OnTopperClickListener,
        HighlistAdapter.OnHighlightClick,
        PersonalizedSubjectAdapter.OnSubjectClickListener,
        FeaturesSectionAdapter.OnFeatureClickListener,
        SortSubjectNameAnalyticAdapter.OnSubjectSortClick,
        PosterPopUp.OnDismissListener, TodayTaskFragment.TodayTaskListener {
    public static final String TAG = CompetitiveHomeScreenFragment.class.getSimpleName();
    // Matches the "FEATURE_TAG" the backend sets in the Pinjra Tod Analysis
    // card's activityParams — reuses the existing glow-border + tag-matching
    // machinery (FeaturesSectionAdapter.setAnimatedItemTag / getFeatureTag())
    // that already drives the Boards home-screen "hello" animation.
    private static final String PINJRATOD_FEATURE_TAG = "PINJRA_TOD_ANALYSIS";
    private static final String PARIKSHA_ADHYAYAN_FEATURE_TAG = "PARIKSHA_ADHYAYAN";
    private boolean lastKnownState = false;
    NetworkStatusLiveData networkStatusLiveData;
    ImageView release_notes, globalSearch;
    CardView queries, refer;
    String examName;
    UserDetailsResponse userDto;
    ImageView welcome_icon, footerImage;
    MaterialCardView cv_welcome_icon;
    LinearLayout footerImageLayout;
    LinearLayout linearLayoutScrollView, globalSearchLayout;

    CardView cv_feature_left_scroll, cv_feature_right_scroll;
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
    int timer = 4;
    public int appear = 0;
    ArrayList<String> titles = new ArrayList<>();
    ConstraintLayout constraintLayout;
    //    TextView jeeSwitch, boardSwitch;
    ShimmerFrameLayout shimmerSubjects, shimmerFeatureSection,
            shimmerHighlights, carouselShimmer, toppersShimmer;
    RecyclerView recyclerViewToppers;
    RecyclerView recyclerViewSub;

    RecyclerView recyclerViewhighlight;
    TextSwitcher searchTextSwitcher;
    ArrayList<SlideModel> imageList = new ArrayList<>();
    FrameLayout img_slider;

    private boolean onCreateCalled;
    private OnFragmentInteractionListener mListener;
    CardView toopersCardLayout;
    RecyclerView recyclerViewSubjectProgress;
    private List<SubjectResponse> subjectModelList = new ArrayList<>();
    private List<HighlistModel> viralVideoList = new ArrayList<>();
    public int dataLoaded = 0;
    public static final String KEY_VIDEO_URI = "video_uri";
    private Context mContext;
    ArrayList<String> searchOptionList;
    Dialog boardDialog;

    private ProgressDialog loading;
    private Dialog progressDialog;
    private LottieAnimationView lottieAnimationView, clickAskBtnLottieAnim;
    RecyclerView recyclerViewReleaseNotes;
    ToppersAdapter toppersAdapter;
    HighlistAdapter highlistAdapter;

    int searchTextAnimationPosition = 0;
    private Handler searchTextHandler;
    private Runnable searchTextRunnable;

    HomeScreenViewModel homeScreenViewModel;
    FrameLayout todayTaskContainer;
    TextView subjects;
    String examId;

    ArrayList<ToppersHomeScreenModel> toppersList = new ArrayList<>();


    DatabaseReference databaseReference3;
    private CustomBottomStatusView customBottomStatusView;
    private View home_screen_status_card_layout;
    private ImageView how_to_poster, headerBackgroundImage;
    private String posterUrl;
    private TextView highlightsText;

    private String selectedCourseId;
    private TextView toppersLayoutTitle, toppersLayoutSubTitle;
    private TextView chipBoard, chipStream, chipClass;
    private MaterialCardView cardChipBoard, cardChipStream, cardChipClass;
    private LinearLayout llChipsRow;
    private ExamUiDataResponse examUiDataResponse;
    private MediaViewModel mediaViewModel;
    private Dialog noInternetDialog;


    private PersonalizedSubjectAdapter personalizedSubjectAdapter;
    private FeaturesSectionAdapter featuresSectionAdapter;
    private List<HomeScreenCardResponse> featureSectionModelsList = new ArrayList<>();
    RecyclerView featureSectionRv;
    LinearLayout ll_header_name_layout;
    ConstraintLayout joinCampaignButton;
    private boolean isFeatureSectionLoading = false;
    private boolean isUserOnboarded = false;

    private ToggleButton toggleButton;
    private LinearLayout toggle_container;
    private TextView tvSwitchLabel;
    private SwitchUserClassCourseRequestDto switchableCourseClassInfo;
    private boolean isSwitchInProgress = false;

    boolean isNeetMode = false;

    @Override
    public void onStart() {
        super.onStart();
    }

    public void setObserver() {
        getSubjectData();
        viralVideoData();
        toppersExtraction();
        showTodayTaskFragment();
        fetchHomeScreenDataFromCache();
        getCarouselData();
        getFeaturesSectionData();
        Log.d("setCrashCourseDataFromCache", " called form onResume ");
    }


    @Override
    public void onPause() {
        super.onPause();
        if (searchTextHandler != null && searchTextRunnable != null) {
            searchTextHandler.removeCallbacks(searchTextRunnable);
        }
        if (featuresSectionAdapter != null && featureSectionRv != null) {
            featuresSectionAdapter.stopCurrentAnimationIfBound(featureSectionRv);
        }
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


    }

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.competitive_home_screen_fragment, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EdgeToEdgeHelper.padForSystemBars(view.findViewById(R.id.linear_layout_scrollView));
        Bundle args = getArguments();
        if (args != null) {
            isUserOnboarded = args.getBoolean("is_user_onboarded", false);
        }
        maybeLaunchNeetDiagnosticWebview();
        Log.d("getSubscribedSubjectData", "onCreate");
        MyApplication.saveData(PreferenceKeys.SHOW_MATCH_THESE_DIALOG.getKey(), true);


        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FIREBASE_MESSAGING_TOKEN", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();

                    // Log and use the token
                    Log.i("FIREBASE_MESSAGING_TOKEN", "TOKEN :: " + token);
                });

        MyApplication.saveData(PreferenceKeys.BOARD_SWITCH.getKey(), false);
        selectedCourseId = MyApplication.getStringData(PreferenceKeys.SECONDARY_COURSE_ID.getKey());
        Log.d("TAG", "CourseId: " + MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey()) + ", Secondary: " + MyApplication.getStringData(PreferenceKeys.SECONDARY_COURSE_ID.getKey()));
        MyApplication.saveData(PreferenceKeys.SELECTED_COURSE_ID_LOCAL.getKey(), selectedCourseId);

        how_to_poster = view.findViewById(R.id.how_to_poster);
        home_screen_status_card_layout = view.findViewById(R.id.home_screen_status_card_layout);
        ll_header_name_layout = view.findViewById(R.id.ll_header_name_layout);
        joinCampaignButton = view.findViewById(R.id.join_campaign_button);

        progressBar = view.findViewById(R.id.progress_dataLoading);
        scrollView = view.findViewById(R.id.scroll_bar);
        your_Progress = view.findViewById(R.id.your_progress);
        constraintLayout = view.findViewById(R.id.parent_layout);
        img_slider = view.findViewById(R.id.image_sliders);
        welcome_icon = view.findViewById(R.id.welcome_icon);
        cv_welcome_icon = view.findViewById(R.id.cv_welcome_icon);
        footerImageLayout = view.findViewById(R.id.footer_board_image);
        footerImage = view.findViewById(R.id.home_screen_footer_image);
        searchTextSwitcher = view.findViewById(R.id.search_text_switcher);
        searchTextStart = view.findViewById(R.id.txt_search_start);
        searchTextEnd = view.findViewById(R.id.txt_search_end);
        globalSearchLayout = view.findViewById(R.id.global_search_button);
        carouselShimmer = view.findViewById(R.id.carousel_shimmer);
        toppersShimmer = view.findViewById(R.id.shimmer_toppers);
        clickAskBtnLottieAnim = view.findViewById(R.id.click_ask_anim);
        toopersCardLayout = view.findViewById(R.id.topper_card);
        headerBackgroundImage = view.findViewById(R.id.header_background_image);
        highlightsText = view.findViewById(R.id.highlights);
        toppersLayoutTitle = view.findViewById(R.id.toppers_title);
        toppersLayoutSubTitle = view.findViewById(R.id.toppers_sub_title);

        cardChipBoard = view.findViewById(R.id.card_chip_board);
        cardChipStream = view.findViewById(R.id.card_chip_stream);
        cardChipClass = view.findViewById(R.id.card_chip_class);
        chipBoard = view.findViewById(R.id.chip_board);
        chipStream = view.findViewById(R.id.chip_stream);
        chipClass = view.findViewById(R.id.chip_class);
        llChipsRow = view.findViewById(R.id.ll_chips_row);


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

        toggleButton = view.findViewById(R.id.toggleButton1);
        toggle_container = view.findViewById(R.id.toggle_container);
        tvSwitchLabel = view.findViewById(R.id.tv_switch_label);

//        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            showSwitchConfirmation(isChecked);
//            toggleButton.setOnCheckedChangeListener(null);
//            toggleButton.setChecked(!isChecked);
//            toggleButton.setOnCheckedChangeListener(this::onToggleChanged);
//        });

        fab_menu = view.findViewById(R.id.cv_ask_doubt);
        ask_relative = view.findViewById(R.id.ask_relative);
        linearLayoutScrollView = view.findViewById(R.id.linear_layout_scrollView);
        user_name = view.findViewById(R.id.user_name);
        queries = view.findViewById(R.id.Queries);
        view_details = view.findViewById(R.id.view_details);

        release_notes = view.findViewById(R.id.release_notes);
        refer = view.findViewById(R.id.Refer_and_earn);
        globalSearch = view.findViewById(R.id.search);
        todayTaskContainer = view.findViewById(R.id.today_task_container);

        cv_feature_left_scroll = view.findViewById(R.id.cv_feature_left_scroll);
        cv_feature_right_scroll = view.findViewById(R.id.cv_feature_right_scroll);

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
        homeScreenViewModel.getPersonalizedPathData(false);

        joinCampaignButton.setOnClickListener(view1 -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.hs_campaign_Join_now_clicked.getActionName());
            destroyHowTo();
            SubscriptionNavigationHelper.navigateToSubscriptionPage(getActivity(), RedirectionSource.NEET_HOME_JOIN_NOW);
        });


        setToolBarUserSubscribedOrNot();

        scrollView.setVerticalScrollBarEnabled(false);
        globalSearchLayout.setVisibility(RemoteConfigUtils.isFeatureVisible(RemoteConfigUtils.FeatureVisibilityName.GLOBAL_SEARCH.getFeature()) ? View.VISIBLE : View.GONE);

        globalSearchLayout.setOnClickListener(ev -> {
            Intent intent = new Intent(getActivity(), GlobalSearchActivity.class);
            destroyHowTo();
            startActivity(intent);
        });

        globalSearch.setOnClickListener(view13 -> {
            Intent intent = new Intent(getActivity(), GlobalSearchActivity.class);
            destroyHowTo();
            startActivity(intent);
        });


        scrollView.setOnScrollChangeListener((view16, i, i1, i2, i3) -> {
            if (i < 120 && i1 < 120 && i2 < 120 && i3 < 120) {
                appear = 0;
                fab_menu.setVisibility(View.VISIBLE);
                clickAskBtnLottieAnim.setVisibility(View.VISIBLE);

            } else {
                if (appear == 0) {
                    clickAskBtnLottieAnim.setVisibility(View.GONE);
                    if (clicked) {
                        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);

                        fab_menu.setVisibility(View.INVISIBLE);

                        fab_menu.setAnimation(animation);
                        clicked = false;
                    } else {
                        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
                        fab_menu.setVisibility(View.INVISIBLE);
                        fab_menu.setAnimation(animation);

                    }
                }
                appear = 1;
            }
        });


        initializeAllShimmerFrames(view);

        initializeAllRecyclerView(view);

        initializeAllAdapters();


        linearLayoutScrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            Rect scrollBounds = new Rect();
            scrollView.getHitRect(scrollBounds);
            highlistAdapter.handleRecyclerVisibility(recyclerViewhighlight.getLocalVisibleRect(scrollBounds));
        });

        setBoardDialog();

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
                    hideToBottomRight(ask_relative);
                    Intent intent = new Intent(getActivity(), ChatScreenChooseSubjectForChat.class);
                    intent.putExtra("subscriptionActive", true);
                    destroyHowTo();
                    startActivity(intent);
                    requireActivity().overridePendingTransition(R.anim.slide_in_from_right_switch, R.anim.slide_out_to_left_switch);
                    if (!clicked) clicked = true;
                    else clicked = false;
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.click_ask_doubt.getActionName());
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.hs_click_ask_doubt.getActionName());
                }
            } catch (Exception e) {
                Log.d("onViewCreated: ", String.valueOf(e.getMessage()));
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

//            isOnline = Boolean.TRUE.equals(isOnline);

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
                getAPIDataByPhoneData();
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

        cv_feature_left_scroll.setOnClickListener(v -> featureSectionRv.smoothScrollBy(-featureSectionRv.getWidth(), 0));
        cv_feature_right_scroll.setOnClickListener(v -> featureSectionRv.smoothScrollBy(featureSectionRv.getWidth(), 0));

        featureSectionRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                updateSubjectArrowsVisibility();
            }
        });

        showViewFromItsBottomRight(ask_relative);

        how_to_poster.setOnClickListener(v -> {
            v.setEnabled(false);

            mListener.onHowToPosterClick();
            releaseHighlightVideo();

            v.postDelayed(() -> v.setEnabled(true), 2000);
        });

        try {
            if (!MyApplication.getFirebaseRemoteConfig().getBoolean("hideHowToVideos" + "_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()) + "_" + MyApplication.getStringData(PreferenceKeys.EXAM_CODE.getKey()))) {
                posterUrl = HowToVideoView.getHowToPosterUrl("HOME_SCREEN");
                Log.d("posterUrl", "onViewCreated: " + posterUrl);
                if (StringUtils.isNotEmpty(posterUrl) && posterUrl.startsWith("http")) {
                    how_to_poster.setVisibility(View.VISIBLE);
                    Glide.with(this).load(posterUrl).into(how_to_poster);
                } else {
                    how_to_poster.setVisibility(View.GONE);
                }
            } else {
                how_to_poster.setVisibility(View.GONE);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Launches the NEET diagnostic (stage pyramid / opportunity map / next
     * marks trajectory) inside a WebView, exactly once, right after a user
     * completes onboarding having selected NEET as their course. Just the url
     * is needed here — WebViewByUrlActivity recognises this url and attaches
     * the user's identity (userId/userName/phone) from SharedPreferences on
     * its own, so the diagnostic web app can skip its own login screen.
     */
    private void maybeLaunchNeetDiagnosticWebview() {
        if (!isUserOnboarded) return;
        try {
            OnBoardingSelectionModel sel = PreferenceMethods.getSelectedOnboardingModel();
            boolean isNeet = sel != null && "NEET".equalsIgnoreCase(sel.getId());
            if (!isNeet) return;

            String userId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
            String shownKey = PreferenceKeys.NEET_DIAGNOSTIC_WEBVIEW_SHOWN_PREFIX.getKey() + userId;
            if (MyApplication.getBooleanDataFromDoNotDeleteSharedPreference(shownKey)) return;

            String baseUrl = CommonMethods.getNeetDiagnosticUrl();
            if (StringUtils.isEmpty(baseUrl)) return;

            MyApplication.saveDataForDoNotDeleteSharedPreference(shownKey, true);

            // "source=android" is what tells WebViewByUrlActivity to attach the
            // user's identity (see appendNeetDiagnosticIdentity) — no remote-config
            // host-matching involved, just this explicit marker.
            Map<String, Object> params = new HashMap<>();
            params.put("source", "android");

            CommonNavigator.navigateToWebViewByUrlParams(requireContext(), baseUrl, params,
                    false, false, false, false, false,
                    false, null, false, false, false, false,
                    null, TAG, null);
        } catch (Exception e) {
            Log.e(TAG, "maybeLaunchNeetDiagnosticWebview failed", e);
        }
    }

    public void setToolBarUserSubscribedOrNot() {
        ll_header_name_layout.setVisibility(View.VISIBLE);
        if (!SubscriptionNavigationHelper.hasActiveSubscriptionByBatch()) {
            joinCampaignButton.setVisibility(View.VISIBLE);
        } else {
            joinCampaignButton.setVisibility(View.GONE);
        }
    }

    private void showTodayTaskFragment() {
        if (!isAdded() || todayTaskContainer == null) {
            return;
        }
        todayTaskContainer.setVisibility(View.VISIBLE);
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        TodayTaskFragment todayTaskFragment = new TodayTaskFragment(this, TAG, isUserOnboarded);
        fragmentTransaction.replace(R.id.today_task_container, todayTaskFragment);
        fragmentTransaction.commit();
    }

    public void updateCarouselData() {
        homeScreenViewModel.clearCarouselData();
        homeScreenViewModel.callCarouselApi();
    }

    private void updateSubjectArrowsVisibility() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) featureSectionRv.getLayoutManager();
        if (layoutManager == null || featureSectionRv.getAdapter() == null) return;

        int firstVisible = layoutManager.findFirstCompletelyVisibleItemPosition();
        int lastVisible = layoutManager.findLastCompletelyVisibleItemPosition();
        int total = featureSectionRv.getAdapter().getItemCount();

        cv_feature_left_scroll.setVisibility(firstVisible > 0 ? View.VISIBLE : View.GONE);
        cv_feature_right_scroll.setVisibility(lastVisible < total - 1 ? View.VISIBLE : View.GONE);
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


    private void fetchHomeScreenDataFromCache() {
        try {
            Log.d(TAG, "fetchHomeScreenDataFromCache: START");

            String welcomeText = MyApplication.getStringData(HomeScreenCacheData.AV_HOME_SCREEN_WELCOME_TEXT.name());
            Log.d(TAG, "Welcome Text: " + welcomeText);

            if (!StringUtils.isEmpty(welcomeText)) {
                user_name.setText(welcomeText);
                user_name.setVisibility(View.VISIBLE);
                Log.d(TAG, "User name set successfully");
            } else {
                Log.d(TAG, "Welcome Text is empty");
                user_name.setVisibility(View.GONE);
            }

            String footerImageUrl = MyApplication.getStringData(HomeScreenCacheData.AV_HOME_SCREEN_FOOTER_IMAGE_URL.name());
            Log.d(TAG, "Footer Image URL: " + footerImageUrl);

            if (!StringUtils.isEmpty(footerImageUrl) && footerImageUrl.startsWith("http")) {
                Glide.with(this)
                        .load(footerImageUrl)
                        .into(footerImage);
                footerImageLayout.setVisibility(View.VISIBLE);
                Log.d(TAG, "Footer image loaded and layout visible");
            } else {
                Log.d(TAG, "Footer Image URL is empty");
            }

            String welcomeIconUrl = MyApplication.getStringData(HomeScreenCacheData.AV_HOME_SCREEN_WELCOME_ICON.name());
            Log.d(TAG, "Welcome Icon URL: " + welcomeIconUrl);

            if (StringUtils.isNotEmpty(welcomeIconUrl)
                    && welcomeIconUrl.startsWith("http")) {
                cv_welcome_icon.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(welcomeIconUrl)
                        .error(R.drawable.trophy_icon)
                        .into(welcome_icon);
                Log.d(TAG, "Welcome icon loaded");
            } else {
                cv_welcome_icon.setVisibility(View.GONE);
                Log.d(TAG, "Welcome icon hidden (URL empty)");
            }

            String headerImageUrl = MyApplication.getStringData(PreferenceKeys.HEADER_IMAGE_URL.getKey());
            Log.d(TAG, "Header Image URL: " + headerImageUrl);

            if (!StringUtils.isEmpty(headerImageUrl) && headerImageUrl.startsWith("http")) {
                Glide.with(requireActivity())
                        .load(headerImageUrl)
                        .into(new CustomTarget<Drawable>() {

                            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                Log.d(TAG, "Header image loaded successfully");

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
                                Log.d(TAG, "Header image load cleared");
                            }
                        });
            } else {
                Log.d(TAG, "Header Image URL is empty");
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

            Log.d(TAG, "fetchHomeScreenDataFromCache: END");

        } catch (Exception ex) {
            Log.e(TAG, "Error in fetchHomeScreenDataFromCache", ex);
        }
    }

    private void fetchHomeScreenUI() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("examCode", MyApplication.getStringData(PreferenceKeys.EXAM_CODE.getKey()));
        MyJsonObjectRequest request = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(AV_EXAM_UI_DETAILS, map), null,
                response -> {
                    if (!isAdded()) return;
                    try {
                        Log.d("fetchHomeScreenUI", "fetchHomeScreenUI: " + response);
                        if (response != null) {
                            Gson gson = new Gson();
                            TypeToken<ApiWrappedResponse<ExamUiDataResponse>> token = new TypeToken<ApiWrappedResponse<ExamUiDataResponse>>() {
                            };
                            ApiWrappedResponse<ExamUiDataResponse> apiResponse = gson.fromJson(response.toString(), token.getType());
                            Log.d(TAG, "fetchHomeScreenUI: "+apiResponse);
                            if (apiResponse != null && apiResponse.getData() != null) {
                                Log.d(TAG, "fetchHomeScreenUI--insidemethods: ");
                                ExamUiDataResponse homeData = apiResponse.getData();
                                examUiDataResponse = homeData;
                                MyApplication.saveData(HomeScreenCacheData.AV_EXAM_UI_DATA.name(), new Gson().toJson(homeData));
                                updateUIWithHomeScreenData(homeData);
                                updateHeaderChips(homeData, userDto);
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
                        Log.e(TAG, "Failed to fetch home screen UI data", error);
                    }
                }
        );

        request.setTag(TAG);
        MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(request);
    }

    private void updateHomeScreenHeaderTitle(ExamUiDataResponse homeData) {

        Log.d(TAG, "updateHomeScreenHeaderTitle() called");

        if (homeData == null) {
            Log.e(TAG, "homeData is null");
            return;
        }

        Log.d(TAG, "Home Screen Title: " + homeData.getHomeScreenTitleText());

        ll_header_name_layout.setVisibility(View.VISIBLE);

        if (!StringUtils.isEmpty(homeData.getHomeScreenTitleText())) {
            user_name.setText(homeData.getHomeScreenTitleText());
            user_name.setVisibility(View.VISIBLE);

            Log.d(TAG, "Setting title text: " + homeData.getHomeScreenTitleText());

            MyApplication.saveData(
                    HomeScreenCacheData.AV_HOME_SCREEN_WELCOME_TEXT.name(),
                    homeData.getHomeScreenTitleText()
            );

            Log.d(TAG, "Title text saved to cache");
        } else {
            Log.d(TAG, "Home screen title is empty");
        }

        String welcomeIconUrl = homeData.getHomeScreenTitleIcon();
        Log.d(TAG, "Welcome Icon URL: " + welcomeIconUrl);

        if (StringUtils.isNotEmpty(welcomeIconUrl) && welcomeIconUrl.startsWith("http")) {

            Log.d(TAG, "Valid icon URL found. Loading image with Glide");

            cv_welcome_icon.setVisibility(View.VISIBLE);

            Glide.with(this)
                    .load(welcomeIconUrl)
                    .placeholder(R.drawable.blank)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.drawable.trophy_icon)
                    .into(welcome_icon);

            MyApplication.saveData(
                    HomeScreenCacheData.AV_HOME_SCREEN_WELCOME_ICON.name(),
                    welcomeIconUrl
            );

            Log.d(TAG, "Icon URL saved to cache");

        } else {
            Log.d(TAG, "Invalid or empty icon URL. Hiding icon view");
            cv_welcome_icon.setVisibility(View.GONE);
        }

        Log.d(TAG, "updateHomeScreenHeaderTitle() completed");
    }


    private void updateHeaderChips(
            ExamUiDataResponse details,
            UserDetailsResponse userDetails
    ) {
        if (!isAdded() || llChipsRow == null) return;

        boolean isSubscribed = MyApplication.getBoolData(
                PreferenceKeys.SUBSCRIBED.getKey()
        ) || MyApplication.getBoolData(
                PreferenceKeys.CAMPAIGN_SUBSCRIBED.getKey()
        );

        if (isSubscribed) {
            llChipsRow.setVisibility(View.GONE);
            return;
        }

        if (details == null && userDetails == null) {
            llChipsRow.setVisibility(View.GONE);
            return;
        }

        // Use headerTags from API if available
        if (details != null
                && details.getHeaderTags() != null
                && !details.getHeaderTags().isEmpty()) {

            List<String> tags = details.getHeaderTags();

            cardChipBoard.setVisibility(View.GONE);
            cardChipStream.setVisibility(View.GONE);
            cardChipClass.setVisibility(View.GONE);

            if (tags.size() >= 1 && !StringUtils.isEmpty(tags.get(0))) {
                chipBoard.setText(tags.get(0));
                cardChipBoard.setVisibility(View.VISIBLE);
            }

            if (tags.size() >= 2 && !StringUtils.isEmpty(tags.get(1))) {
                chipStream.setText(tags.get(1));
                cardChipStream.setVisibility(View.VISIBLE);
            }

            if (tags.size() >= 3 && !StringUtils.isEmpty(tags.get(2))) {
                chipClass.setText(tags.get(2));
                cardChipClass.setVisibility(View.VISIBLE);
            }

            llChipsRow.setVisibility(View.VISIBLE);
            return;
        }

        UserDetailsResponse.BatchDetail activeBatch = null;

        if (userDetails != null && userDetails.getBatches() != null) {
            for (UserDetailsResponse.BatchDetail batch : userDetails.getBatches()) {
                if (Boolean.TRUE.equals(batch.getActive())) {
                    activeBatch = batch;
                    break;
                }
            }
        }

        // Board chip — onboarding name e.g. "NEET"
        OnBoardingSelectionModel model =
                PreferenceMethods.getSelectedOnboardingModel();

        if (model != null && !StringUtils.isEmpty(model.getName())) {
            chipBoard.setText(model.getName());
            cardChipBoard.setVisibility(View.VISIBLE);
        } else {
            cardChipBoard.setVisibility(View.GONE);
        }

        // Stream chip — currently disabled
//    if (details != null && !StringUtils.isEmpty(details.getStreamName())) {
//        chipStream.setText(details.getStreamName());
//        cardChipStream.setVisibility(View.VISIBLE);
//    } else if (activeBatch != null
//            && !StringUtils.isEmpty(activeBatch.getStreamName())) {
//        chipStream.setText(activeBatch.getStreamName());
//        cardChipStream.setVisibility(View.VISIBLE);
//    } else {
//        cardChipStream.setVisibility(View.GONE);
//    }

        // Class chip — gradeName e.g. "12th"
        if (activeBatch != null
                && !StringUtils.isEmpty(activeBatch.getGradeName())) {

            chipClass.setText(activeBatch.getGradeName());
            cardChipClass.setVisibility(View.VISIBLE);

        } else {
            cardChipClass.setVisibility(View.GONE);
        }

        boolean anyVisible =
                cardChipBoard.getVisibility() == View.VISIBLE
                        || cardChipStream.getVisibility() == View.VISIBLE
                        || cardChipClass.getVisibility() == View.VISIBLE;

        llChipsRow.setVisibility(
                anyVisible ? View.VISIBLE : View.GONE
        );
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
//                            linearLayoutScrollView.setBackground(resource);
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


    private void showViewFromItsBottomRight(View targetView) {
        targetView.post(() -> {
            targetView.setVisibility(View.VISIBLE);
            targetView.setPivotX(targetView.getWidth());
            targetView.setPivotY(targetView.getHeight());
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(targetView, "scaleX", 0f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(targetView, "scaleY", 0f, 1f);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleX, scaleY);
            animatorSet.setDuration(300);
            animatorSet.start();
        });

    }

    private void hideToBottomRight(View targetView) {
        targetView.setPivotX(targetView.getWidth());
        targetView.setPivotY(targetView.getHeight());
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(targetView, "scaleX", 1f, 0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(targetView, "scaleY", 1f, 0f);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(500);

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                targetView.setVisibility(View.GONE);
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

        // Cancel any previously running animation before starting a new one
        if (searchTextHandler != null && searchTextRunnable != null) {
            searchTextHandler.removeCallbacks(searchTextRunnable);
        }
        searchTextHandler = new Handler(Looper.getMainLooper());
        searchTextRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isAdded() || getView() == null) return;
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

    @Override
    public void onSubjectClick(SubjectResponse subject) {
        if (CommonMethods.isOnline(requireContext())) {
            Map<String, Object> params = new HashMap<>();
            params.put("learningPathId", subject.getLearningPathId());
            params.put("batchPathId", subject.getBatchPathId());
            params.put("subjectId", subject.getId());
            params.put("subjectName", subject.getSubjectName());
            String videoUrl = getPersonalizedPathVideoUrl();
            Bundle bundle = new Bundle();
            String url = CommonMethods.getPersonalizedPathUrl();
            bundle.putString("url", url);

//          MyApplication.getFirebaseRemoteConfig().getString("personalised_path_base_url"));
            bundle.putBoolean("appendUserId", true);
            bundle.putBoolean("appendToken", true);
            bundle.putBoolean("appendUserName", true);
            bundle.putBoolean("appendUserGender", true);
            bundle.putSerializable("params", (Serializable) params);

            if (shouldRedirectToFeatureInfoVideo() &&
                    isPersonalisedPathFeatureEnabled() && StringUtils.isNotEmpty(videoUrl)) {
                Intent intent = new Intent(getActivity(), FeatureInfoVideoActivity.class);
                intent.putExtras(bundle);
                intent.putExtra("videoUrl", videoUrl);
                intent.putExtra("subject_id", subject.getId());
                intent.putExtra("subject_name", subject.getSubjectName());
                intent.putExtra(FeatureInfoVideoActivity.bundleNavigationFrom, FeatureInfoVideoActivity.NavigationFromEnum.NEET_HOME_SCREEN.name());
                startActivity(intent);
                return;
            }

            boolean showMiniVideo = !MyApplication.getBoolData(PreferenceKeys.HIDE_MINI_VIDEO_IN_ADAPTIVE_SCREEN.getKey()) && isPersonalisedPathFeatureEnabled();
            MyApplication.saveData(PreferenceKeys.HIDE_MINI_VIDEO_IN_ADAPTIVE_SCREEN.getKey(), true);
            bundle.putString("miniVideoUrl", showMiniVideo ? videoUrl : null);

            CommonNavigator.navigateToWebViewByUrlParams(requireContext(),
                    url,
                    params,
                    true, true,
                    false, false,
                    false, false, null, true, true, false, false, null, TAG, showMiniVideo ? videoUrl : null);

//            Intent intent = new Intent(getActivity(), WebViewByUrlActivity.class);
//            intent.putExtras(bundle);

            Map<String, String> logBundle = new HashMap<>();
            logBundle.put("learningPathId", subject.getLearningPathId());
            logBundle.put("subjectId", subject.getId());
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.home_screen_personalized_subject_clicked.getActionName(), logBundle);
            ScreenAnalyticsHelper.sendScreenVisitedEvent(AnalyticsEvent.PERSONALIZED_PATH_SCREEN.name(), "SCREEN");
//            startActivity(intent);
        } else {
            CommonMethods.showNegativeSnackbar(constraintLayout, getString(R.string.error_no_internet_connection), 3000);
        }
    }

    private String getPersonalizedPathVideoUrl() {
        String referralSource = MyApplication.getUtmCampaign();
        if (StringUtils.isNotEmpty(referralSource)) {
            String json = MyApplication.getFirebaseRemoteConfig()
                    .getString("campaign_personalized_path_video_map");
            try {
                JSONObject object = new JSONObject(json);
                String key = referralSource.trim().toLowerCase();
                if (object.has(key)) {
                    String url = object.optString(key);
                    return StringUtils.isNotEmpty(url) ? url : "";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }

        try {
            String batchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
            String json = MyApplication.getFirebaseRemoteConfig().getString("feature_info_video_url_v2");
            JSONObject object = new JSONObject(json);
            if (object.has(batchCode)) {
                return object.optString(batchCode, "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private boolean isPersonalisedPathFeatureEnabled() {
        try {
            String batchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
            String json = MyApplication.getFirebaseRemoteConfig().getString("to_show_personalised_path_feature_info_v2");
            JSONObject object = new JSONObject(json);
            return object.optBoolean(batchCode, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Decide whether to redirect to the feature info video
     **/
    private boolean shouldRedirectToFeatureInfoVideo() {
        int count = MyApplication.getIntDataFromDoNotDeleteSharedPreference(PreferenceKeys.FEATURE_INFO_VIDEO_COUNT_PREFIX.getKey() + MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
        return count < 1;
    }


    //Carousel section
    private void setCarouselData(List<CarouselResponse> carouselItems) {
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

    private void getCarouselData() {
        Log.d("ObserverCarousel ", "observer data : ");
        if (getView() == null) {
            return;
        }
        homeScreenViewModel.getCarouselData().observe(getViewLifecycleOwner(),
                (data) -> {
                    List<CarouselResponse> sliderItems = new ArrayList<>(data);
                    Log.d("ObserverCarousel ", "observer: " + data.size());
                    carouselShimmer.setVisibility(View.GONE);
                    setCarouselData(sliderItems);
                });
    }

    private void openDeeplink(String link) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            if (getContext() != null && intent.resolveActivity(getContext().getPackageManager()) != null) {
                destroyHowTo();
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Unable to redirect", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("DeeplinkError", "Failed to open deeplink", e);
        }
    }

    private void launchActivity(CarouselResponse dto) {
        try {
            Class<?> targetClass = ActivityLauncher.resolveClass(dto.getClassName());
            Intent intent = new Intent(getContext(), targetClass);
            intent.putExtra(SubscriptionNavigationHelper.EXTRA_SOURCE, RedirectionSource.NEET_HOME_SCREEN);

            if (dto.getActivityParams() != null) {
                for (Map.Entry<String, Object> entry : dto.getActivityParams().entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();

                    if (value instanceof String) {
                        intent.putExtra(key, (String) value);
                    } else if (value instanceof Integer) {
                        intent.putExtra(key, (Integer) value);
                    } else if (value instanceof Boolean) {
                        intent.putExtra(key, (Boolean) value);
                    }
                }

                if ("arivihan.technologies.doubtbuzzter2.activity.HalfYearlyActivity".equals(dto.getClassName())) {
                    if (dto.getActivityParams().containsKey("examId")) {
                        intent.putExtra("examId", String.valueOf(dto.getActivityParams().get("examId")));
                        intent.putExtra("examName", String.valueOf(dto.getActivityParams().get("examName")));
                    } else {
                        intent.putExtra("examId", examId);
                        intent.putExtra("examName", examName);
                    }

                }
            }

            try {
                PackageManager pm = getContext().getPackageManager();
                ResolveInfo resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                String resolvedActivity = resolveInfo.activityInfo.name;

                String asatActivity = ASATActivity.class.getName();
                if (resolvedActivity.equals(asatActivity)) {
                    //trigger event of asat clicked
                    Map<String, String> params = new HashMap<>();
                    params.put("screen", ImageSlider.class.getSimpleName());
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.asat_screen_click.getActionName(), params);
//                    intent.putExtra(ASATActivity.ASAT_OPEN_RESULT, MyApplication.getBoolData("asatSubscribed"));
                }
            } catch (Exception ignored) {
            }

            destroyHowTo();
            startActivity(intent);
        } catch (ClassNotFoundException e) {
            Log.e("ClassNotFound", "Activity class not found", e);
            MyApplication.openGooglePlayStore(requireActivity());
        }
    }


    public void releaseHighlightVideo() {
        highlistAdapter.stopPlayer();
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
            if (PINJRATOD_FEATURE_TAG.equals(item.getFeatureTag())) {
                String userId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
                String tappedKey = PreferenceKeys.PINJRATOD_CARD_TAPPED_PREFIX.getKey() + userId;
                if (!MyApplication.getBooleanDataFromDoNotDeleteSharedPreference(tappedKey)) {
                    MyApplication.saveDataForDoNotDeleteSharedPreference(tappedKey, true);
                }
                // stop the glow immediately — don't wait for the next data refresh
                if (featuresSectionAdapter != null) featuresSectionAdapter.clearAnimatedTag();
            }
            if (PARIKSHA_ADHYAYAN_FEATURE_TAG.equals(item.getFeatureTag())) {
                arivihan.technologies.doubtbuzzter2.model.UserDto userDto = MyApplication.getUserDto("userDto");
                String accessStatus = userDto != null && userDto.getParikshaAdhyayanAccess() != null
                        ? userDto.getParikshaAdhyayanAccess() : "UNKNOWN";
                Map<String, String> paCardParams = new HashMap<>();
                paCardParams.put("access_status", accessStatus);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.pa_card_clicked.getActionName(), paCardParams);
                if ("ACCESS_DENIED".equals(accessStatus)) {
                    new arivihan.technologies.doubtbuzzter2.common.ParikshaAdhyayanReferralDialog(requireContext()).show();
                    return;
                }
            }
            CommonMethods.navigateFromActivityConfig(requireContext(), item.getNavigationType(), item.getClassName(), item.getActivityParams());
            Map<String, String> params = new HashMap<>();
            String fullClassName = item.getClassName();
            String shortClassName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
            params.put("class_name", fullClassName);
            params.put("featureClicked", shortClassName);
            params.put("feature_name", item.getTitle());
            params.put("activity_params", item.getActivityParams().toString());
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.hs_neet_feature_section_clicked.getActionName(), params);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void hideContinueSection() {
        // TodayTask listener
    }

    @Override
    public void hideBadgeView() {
        // TodayTask Listener
    }

    @Override
    public void onAnimatedCardTagClicked(String tag) {

    }

    @Override
    public void onDataFetched(TodayTaskFragment source, int size) {

    }

    public interface OnFragmentInteractionListener {
        void onValuePass(String examId, String examName);

        void onHowToPosterClick();
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

    private void setBoardDialog() {
        boardDialog = new Dialog(getActivity());
        boardDialog.setContentView(R.layout.custom_board_dialog);
        if (boardDialog.getWindow() != null) {
            boardDialog.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
            boardDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        boardDialog.setCancelable(false);
        progressBarX = boardDialog.findViewById(R.id.progessBar);

    }

    private void getDoubtVideoContent(String selectedSubject) {
        Map<String, Object> map = new HashMap<>();
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_DOUBT_MICROLECTURE_CONTENT, map), null,
                response -> {
                    TypeToken<MicroLectureContentDto> token = new TypeToken<MicroLectureContentDto>() {
                    };
                    microLectureContentDto = gson.fromJson(response.toString(), token.getType());
                    if (StringUtils.isNotEmpty(selectedSubject)) {
                        startVideoActivity(selectedSubject);
                        loading.cancel();
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
        showViewFromItsBottomRight(ask_relative);

        setObserver();

        if (!onCreateCalled) {

            Log.d("getstudentprogress", "onResumeCalled ");

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
                            //MyApplication.saveData(getString(R.string.user_selected_download_quality), dataSnapshot.getValue(String.class));
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
        if (getActivity() != null) {
            MyVolleySingleton.getInstance(getActivity()).getRequestQueue().cancelAll("FEATURE_SECTION_API");
        }

        if (getActivity() != null) {
            MyVolleySingleton.getInstance(getActivity()).getRequestQueue().cancelAll(TAG);
        }
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
            if (getView() == null) {
                super.onDestroyView();
                return;
            }
            if (homeScreenViewModel != null) {
                homeScreenViewModel.getCarouselData().removeObservers(getViewLifecycleOwner());
                homeScreenViewModel.getSubjectList().removeObservers(getViewLifecycleOwner());
                homeScreenViewModel.getTestSeriesList().removeObservers(getViewLifecycleOwner());
                homeScreenViewModel.getCompetitivePersonalizedPath().removeObservers(getViewLifecycleOwner());
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


    private void getAPIDataByPhoneData() {
        if (timer > 0 && getContext() != null) {
            Map<String, Object> map = new HashMap<>();
            String PhoneNumber = MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey());
            map.put("phoneNumber", PhoneNumber);
            map.put("key", BuildConfig.API_KEY);
//            map.put(PreferenceKeys.SELECTED_COURSE_ID_LOCAL.getKey(), selectedCourseId);

            final String priorBatchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
            final String priorLang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
            MyApplication.resetBatchCodeRefreshFlag();

            MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                    CommonMethods.getUrlParam(AV_GET_USER_BY_PHONE, map), null,
                    response -> {
                        if (!isAdded()) return;
                        try {
                            Log.d("USER_DTO", response.toString());
                            dataLoaded = 1;
                            TypeToken<ApiWrappedResponse<UserDetailsResponse>> token = new TypeToken<ApiWrappedResponse<UserDetailsResponse>>() {
                            };
                            timer--;
                            ApiWrappedResponse<UserDetailsResponse> apiResponse = gson.fromJson(response.toString(), token.getType());
                            userDto = (apiResponse != null && apiResponse.isSuccess()) ? apiResponse.getData() : null;
                            MyApplication.saveUserDto("userDto", userDto);
                            Log.i("USER_DTO", userDto != null ? userDto.toString() : "null");

                            if (userDto != null && userDto.getUserDto() != null
                                    && userDto.getUserDto().getSwitchableCourseClassInfo() != null) {

                                Log.d("NEET_SWITCH", "SwitchableCourseClassInfo is available" + userDto.getUserDto().getSwitchableCourseClassInfo());
                                switchableCourseClassInfo = userDto.getUserDto().getSwitchableCourseClassInfo();
                                toggle_container.setVisibility(View.GONE);
                                String targetCourseId = switchableCourseClassInfo.getCourseId();
                                MyApplication.saveData(PreferenceKeys.TARGET_COURSE_ID.getKey(), targetCourseId);

                                String json = gson.toJson(switchableCourseClassInfo);
                                MyApplication.saveData(PreferenceKeys.SWITCHABLE_COURSE_INFO.getKey(), json);

                                boolean isNeetMode = "3".equals(MyApplication.getStringData(PreferenceKeys.SECONDARY_COURSE_ID.getKey()));
                                MyApplication.saveData(PreferenceKeys.NEET_TOGGLE_STATE.getKey(), isNeetMode);

                                toggleButton.setOnCheckedChangeListener(null);
                                ((HomeScreen) requireActivity()).updateBottomNavForSubscription(false);

                                toggleButton.setChecked(isNeetMode);

                                applyToggleState(isNeetMode);

                                toggleButton.setOnCheckedChangeListener(this::onToggleChanged);

                            } else {
                                toggle_container.setVisibility(View.GONE);
                            }

//                            Boolean isCampaignSubscribed = (userDto != null && userDto.getUserDto() != null) ? userDto.getUserDto().getCampaignSubscribed() : null;
//                            boolean campaignSubscribed = MyApplication.getBoolData(PreferenceKeys.CAMPAIGN_SUBSCRIBED.getKey());
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

                            if (userDto != null) {
                                MyApplication.saveUserProfileLocally(userDto);
                                initBottomStatusView();
                                String newBatchCode = MyApplication.extractActiveBatchCode(userDto);
                                if (newBatchCode != null && StringUtils.isNotEmpty(priorBatchCode)
                                        && !newBatchCode.equals(priorBatchCode)
                                        && MyApplication.isBatchChangeAutoRefreshEnabled()
                                        && MyApplication.tryAcquireBatchCodeRefresh()) {
                                    String newLang = MyApplication.extractActiveBatchLanguage(userDto);
                                    boolean langChanged = newLang != null && StringUtils.isNotEmpty(priorLang) && !newLang.equals(priorLang);
                                    Log.d(TAG, "batchCode changed: " + priorBatchCode + " → " + newBatchCode
                                            + (langChanged ? ", lang: " + priorLang + " → " + newLang : "") + " (CompetitiveHomeScreenFragment)");
                                    try {
                                        homeScreenViewModel.removeDataModel();
                                        homeScreenViewModel.triggerBatchCodeRefresh();
                                        MyApplication.updateAppLocale(requireContext(), newLang);
                                        ((HomeScreen) requireActivity()).refreshActivity();
                                    } catch (Exception ignored) {
                                    }
                                    return;
                                }
                                Reference_Code = userDto.getUserDto().getMyReferCode();
                            }

//                            loadToggleFromCache();

                            setToolBarUserSubscribedOrNot();
                            getSubjectData();
                            viralVideoData();
                            toppersExtraction();
                            showTodayTaskFragment();
                            getFeaturesSectionData();
                            fetchHomeScreenDataFromCache();
                            getCarouselData();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    }, new VolleyGlobalErrorListener(getActivity()) {
                @Override
                public void postError(VolleyError error) {
                    if (!isAdded()) return;
                    dataLoaded = 0;
                    timer--;
                    CommonMethods.showNegativeSnackbar(constraintLayout, error.getMessage());
                }
            });
            myJsonObjectRequest2.setTag(TAG);
            MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(myJsonObjectRequest2);
        }
    }

    private void hideToggle() {
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


    private void setFragments(Fragment analyticFragment) {
        if (isAdded()) {
            FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.main_frame, analyticFragment);
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    private void initializeAllRecyclerView(View view) {
        recyclerViewSubjectProgress = view.findViewById(R.id.progress_view);
        RecyclerViewEdgeEffect.edgeEffectRecyclerView(recyclerViewSubjectProgress, getActivity(), false);

        recyclerViewSub = view.findViewById(R.id.recyclerViewSubject);
        recyclerViewSub.setLayoutManager(new WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        RecyclerViewEdgeEffect.edgeEffectRecyclerView(recyclerViewSub, getActivity(), false);

        featureSectionRv = view.findViewById(R.id.rv_feature_section);
        featureSectionRv.setLayoutManager(new WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        RecyclerViewEdgeEffect.edgeEffectRecyclerView(featureSectionRv, getActivity(), true);
        featureSectionRv.setItemAnimator(null);


        recyclerViewhighlight = view.findViewById(R.id.recyclerViewHighlight);
        recyclerViewhighlight.setLayoutManager(new WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        RecyclerViewEdgeEffect.edgeEffectRecyclerView(recyclerViewhighlight, getActivity(), true);
        ((androidx.recyclerview.widget.SimpleItemAnimator) recyclerViewhighlight.getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerViewhighlight.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                try {
                    super.onScrolled(rv, dx, dy);
                    LinearLayoutManager layoutManager = (LinearLayoutManager) rv.getLayoutManager();
                    assert layoutManager != null;
                    int firstVisible = layoutManager.findFirstVisibleItemPosition();
                    Log.d("issuescrolling", "onScrolled:" + firstVisible);
                    highlistAdapter.handleScroll(firstVisible);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        recyclerViewToppers = view.findViewById(R.id.rv_toppers);
        recyclerViewToppers.setLayoutManager(new WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        RecyclerViewEdgeEffect.edgeEffectRecyclerView(recyclerViewToppers, getActivity(), true);
    }

    private void initializeAllShimmerFrames(View view) {
        subjects = view.findViewById(R.id.subjects);
        shimmerSubjects = view.findViewById(R.id.shimmer_subjects);
        shimmerFeatureSection = view.findViewById(R.id.shimmer_feature_section);
        shimmerHighlights = view.findViewById(R.id.shimmer_highlights);
    }

    private void initializeAllAdapters() {
        personalizedSubjectAdapter = new PersonalizedSubjectAdapter(requireContext(), subjectModelList, this);
        recyclerViewSub.setAdapter(personalizedSubjectAdapter);

        featuresSectionAdapter = new FeaturesSectionAdapter(requireContext(), featureSectionModelsList, this);
        featureSectionRv.setAdapter(featuresSectionAdapter);

        highlistAdapter = new HighlistAdapter(viralVideoList, this, getContext());
        recyclerViewhighlight.setAdapter(highlistAdapter);

        toppersAdapter = new ToppersAdapter(getContext(), toppersList, this);
        recyclerViewToppers.setAdapter(toppersAdapter);

    }


    @Override
    public void _onAttach(Context context) {
        this.mContext = context;
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }


    private void destroyHowTo() {
        try {
            if (HowToVideoView.getInstance(requireActivity()) != null)
                HowToVideoView.getInstance(requireActivity()).remove();
        } catch (Exception ignored) {
        }
    }

//    private void checkUserCampaignSubscriptionStatus() {
//        Map<String, Object> map = new HashMap<>();
//        String PhoneNumber = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
//        map.put(PreferenceKeys.USER_ID.getKey(), PhoneNumber);
//        map.put("key", BuildConfig.API_KEY);
//        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
//                CommonMethods.getUrlParam(GET_USER_BY_ID, map), null,
//                response -> {
//                    Log.d("issue", "checkUserCampaignSubscriptionStatus: " + response.toString());
//                    TypeToken<UserDto> token = new TypeToken<UserDto>() {
//                    };
//                    UserDto userDto = gson.fromJson(response.toString(), token.getType());
//                    MyApplication.saveUserProfileLocally(userDto);
//                }, new VolleyGlobalErrorListener(getActivity()) {
//            @Override
//            public void postError(VolleyError error) {
//                dataLoaded = 0;
//                CommonMethods.showNegativeSnackbar(constraintLayout, error.getMessage());
//            }
//        });
//        myJsonObjectRequest.setTag(TAG);
//        MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(myJsonObjectRequest);
//    }


    /**
     * Pins the Pinjra Tod Analysis card to the front of featureSectionModelsList
     * (shifting every other card back by one) while the user hasn't attempted
     * the diagnostic test yet; once they HAVE attempted it, moves the card to
     * the END of the list instead. Also drives its glow border via the SAME
     * FeaturesSectionAdapter tag-animation mechanism used for the Boards
     * "hello" animation. Call this AFTER featureSectionModelsList is freshly
     * populated (clear+addAll) and BEFORE notifyDataSetChanged().
     *
     * "Attempted" is set by the diagnostic webview itself, from the OTHER side
     * of the JS bridge (setDoNotDeletePreference — String-only), so it must be
     * read back as a String and compared to "true", not via the boolean
     * getter (see PINJRATOD_TEST_ATTEMPTED_PREFIX for why).
     */
    private static final String PINJRATOD_LOG_TAG = "PinjraTodCard";

    private void applyPinjraTodCardOrdering() {
        try {
            int idx = -1;
            StringBuilder seenTags = new StringBuilder();
            for (int i = 0; i < featureSectionModelsList.size(); i++) {
                String ft = featureSectionModelsList.get(i).getFeatureTag();
                seenTags.append(i).append(":").append(ft).append("  ");
                if (PINJRATOD_FEATURE_TAG.equals(ft)) {
                    idx = i;
                }
            }
            Log.d(PINJRATOD_LOG_TAG, "featureTags in response = [" + seenTags + "]  matchIdx=" + idx);
            if (idx < 0) {
                Log.d(PINJRATOD_LOG_TAG, "skipped: no card in this response has FEATURE_TAG=" + PINJRATOD_FEATURE_TAG
                        + " (check the backend activityParams for this card)");
                return;
            }

            String userId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
            boolean attempted = "true".equals(MyApplication.getStringDataFromDoNotDeleteSharedPreference(
                    PreferenceKeys.PINJRATOD_TEST_ATTEMPTED_PREFIX.getKey() + userId));
            boolean tapped = MyApplication.getBooleanDataFromDoNotDeleteSharedPreference(
                    PreferenceKeys.PINJRATOD_CARD_TAPPED_PREFIX.getKey() + userId);
            Log.d(PINJRATOD_LOG_TAG, "userId=" + userId + " attempted=" + attempted + " tapped=" + tapped
                    + " adapterNull=" + (featuresSectionAdapter == null));

            if (!attempted) {
                if (idx != 0) {
                    HomeScreenCardResponse card = featureSectionModelsList.remove(idx);
                    featureSectionModelsList.add(0, card);
                    Log.d(PINJRATOD_LOG_TAG, "moved card from index " + idx + " to front (0)");
                }
            } else if (idx != featureSectionModelsList.size() - 1) {
                HomeScreenCardResponse card = featureSectionModelsList.remove(idx);
                featureSectionModelsList.add(card); // append — moves it to the end of the list
                Log.d(PINJRATOD_LOG_TAG, "moved card from index " + idx + " to the end (test attempted)");
            }

            if (featuresSectionAdapter != null) {
                if (!tapped) {
                    Log.d(PINJRATOD_LOG_TAG, "calling setAnimatedItemTag(" + PINJRATOD_FEATURE_TAG + ")");
                    featuresSectionAdapter.setAnimatedItemTag(PINJRATOD_FEATURE_TAG);
                } else if (PINJRATOD_FEATURE_TAG.equals(featuresSectionAdapter.getAnimatedItemTag())) {
                    featuresSectionAdapter.clearAnimatedTag();
                }
            }
        } catch (Exception e) {
            Log.e(PINJRATOD_LOG_TAG, "applyPinjraTodCardOrdering failed", e);
        }
    }

    /**
     * Fetches the list of feature sections from cache or API and updates the UI accordingly.
     * Uses cached data if available, otherwise makes a network call to retrieve the data.
     */

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
            Log.d(logTag, "Using cached toppers data: " + cachedData.size() + " items");
            featureSectionModelsList.clear();
            featureSectionModelsList.addAll(cachedData);
            applyPinjraTodCardOrdering();
            featuresSectionAdapter.notifyDataSetChanged();
            featureSectionRv.setVisibility(View.VISIBLE);
            shimmerFeatureSection.stopShimmer();
            shimmerFeatureSection.setVisibility(View.GONE);
//            showFeatureScrollHint();
            extractExamInfo(cachedData);
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
                            applyPinjraTodCardOrdering();
                            featuresSectionAdapter.notifyDataSetChanged();
                            featureSectionRv.setVisibility(View.VISIBLE);
//                            showFeatureScrollHint();
                            extractExamInfo(list);
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
                        if (CollectionUtils.isEmpty(cachedData)) {
                            Log.e(logTag, "API error: " + error.toString());
                            featureSectionRv.setVisibility(View.GONE);
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


    private void extractExamInfo(List<HomeScreenCardResponse> featureSectionModelsList) {
        try {
            for (HomeScreenCardResponse featureSectionModel : featureSectionModelsList) {
                if (featureSectionModel != null && featureSectionModel.getClassName() != null && featureSectionModel.getClassName().contains(HalfYearlyActivity.TAG)) {
                    Map<String, Object> params = ActivityLauncher.convertJsonBackToActivityParams(featureSectionModel.getActivityParams());
                    if (params != null && params.containsKey("examId")) {
                        examId = (String) params.get("examId");
                    }
                    if (params != null && params.containsKey("examName")) {
                        examName = (String) params.get("examName");
                    }
                }
            }

            if (mListener != null) {
                mListener.onValuePass(examId, examName);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Extracts the list of toppers from cache or API and updates the UI accordingly.
     */
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
                        if (cachedToppers == null) {
                            toppersShimmer.stopShimmer();
                            toppersShimmer.setVisibility(View.GONE);
                            shimmerSubjects.setVisibility(View.GONE);
                            toopersCardLayout.setVisibility(View.GONE);
                            Log.d("toppersExtraction", "API error: " + error.getMessage());
                        }
                    }
                });

        toppersRequest.setTag("FEATURE_SECTION_API");
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

                toppersLayoutTitle.setText(title);
                toppersLayoutSubTitle.setText(subtitle);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("TopperLayout", "Invalid JSON format in toppers_layout_batch_text_v2");
        }
    }

    /**
     * Handles click events on a topper item and opens the ReelsActivity with the given video URL.
     *
     * @param videoUrl The URL of the video associated with the selected topper.
     */
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


    /**
     * Fetches viral video highlights either from cache or API,
     * updates the RecyclerView and handles shimmer visibility.
     */
    private void viralVideoData() {
        viralVideoList.clear();
        List<HighlistModel> cachedHighlights = MyApplication.getHighlistData(HomeScreenCacheData.AV_HIGHLIGHT_CACHE_KEY.name());
        if (!cachedHighlights.isEmpty()) {
            Log.d("HighlightExtraction", "Using cached highlights.");
            homeScreenViewModel.setHighlightClassList(cachedHighlights);
            viralVideoList.addAll(cachedHighlights);
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
                        TypeToken<List<VideoResponse>> token = new TypeToken<List<VideoResponse>>() {
                        };
                        List<VideoResponse> videoList = gson.fromJson(arrayData.toString(), token.getType());
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
                        viralVideoList.clear();
                        viralVideoList.addAll(list);
                        highlistAdapter.notifyDataSetChanged();
                        recyclerViewhighlight.setVisibility(View.VISIBLE);
                        shimmerHighlights.stopShimmer();
                        shimmerHighlights.setVisibility(View.GONE);
                    } catch (Exception e) {
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

    /**
     * Handles click events on highlight items.
     * Opens ReelsActivity for portrait videos and ExoPlayerActivity for horizontal videos.
     *
     * @param position the index of the clicked highlight item in the viralVideoList
     */
    @Override
    public void onHighlightsClick(int position) {
        try {
            if (viralVideoList != null && position < viralVideoList.size()) {
                HighlistModel highlistModel = viralVideoList.get(position);
                VideoOrientationType videoType = VideoOrientationType.fromString(highlistModel.getVideoType());
                Intent intent;
                switch (videoType) {
                    case PORTRAIT:
                        intent = new Intent(requireActivity(), ReelsActivity.class);
                        intent.putExtra("viral_video_url", highlistModel.getVideoUrl());
                        break;
                    case VIDEO:
                    case LANDSCAPE:
                    case HORIZONTAL:
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

    //Personalized Subject section
    private void getSubjectData() {
//        homeScreenViewModel.getSubjectList().observe(getViewLifecycleOwner(), this::updateSubjectView);
        homeScreenViewModel.getCompetitivePersonalizedPath().observe(getViewLifecycleOwner(), this::updateSubjectView);
    }

    private void updateSubjectView(List<SubjectResponse> subjectModels) {
        if (!isAdded() || getContext() == null) return;

        subjectModelList.clear();
        if (subjectModels.isEmpty()) {
            shimmerSubjects.startShimmer();
            shimmerSubjects.setVisibility(View.VISIBLE);
            recyclerViewSub.setVisibility(View.GONE);
            personalizedSubjectAdapter.notifyDataSetChanged();
            return;
        }
        String subjectName = MyApplication.getStringData(PreferenceKeys.PERSONALISED_PATH_SUBJECT_TITLE.getKey());
        subjects.setText(StringUtils.isNotEmpty(subjectName) ? subjectName : getString(R.string.personalised_path));
        subjectModelList.addAll(subjectModels);
        shimmerSubjects.stopShimmer();
        shimmerSubjects.setVisibility(View.GONE);
        recyclerViewSub.setVisibility(View.VISIBLE);
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
        toggleButton.setBackgroundResource(
                neetMode ? R.drawable.neet_toggle : R.drawable.board_toggle
        );
    }

    private void showSwitchConfirmation(boolean switchToNeet) {
        MyApplication.
                logFirebaseBundleCreation(FirebaseEvents.home_switch_to_board_btn_click.getActionName());

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

        MaterialCardView cvYes = dialog.findViewById(R.id.cv_yes);
        MaterialCardView cvNo = dialog.findViewById(R.id.cv_no);
        ImageView close = dialog.findViewById(R.id.iv_close_pop_up);

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
                    logFirebaseBundleCreation(FirebaseEvents.home_switch_board_yes_btn_click.getActionName());
            LoaderOverlay.TargetMode mode =
                    switchToNeet ?
                            LoaderOverlay.TargetMode.TO_NEET :
                            LoaderOverlay.TargetMode.TO_BOARD;

            LoaderOverlay.show(requireContext(), mode);

            SwitchToTargetCourse(switchToNeet);
        });

        cvNo.setOnClickListener(v -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.home_switch_neet_no_btn_click.getActionName());
            dialog.dismiss();
        });
        close.setOnClickListener(v -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.home_switch_neet_popup_cross_click.getActionName());
            dialog.dismiss();
        });

        dialog.show();
    }


    private void onToggleChanged(CompoundButton buttonView, boolean isChecked) {

        toggleButton.setOnCheckedChangeListener(null);
        toggleButton.setChecked(!isChecked);
        toggleButton.setOnCheckedChangeListener(this::onToggleChanged);

        showSwitchConfirmation(isChecked);
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
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.home_switch_to_board_completed.getActionName());

                            MyApplication.saveSharedPreferencesDoNotDelete(PreferenceKeys.SELECTED_COURSE_ID.getKey(), targetCourseId);

                            refreshHomeScreen();

                            isSwitchInProgress = false;

                        }, 3000); // 3 seconds delay
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

                        revertToggleState(switchToNeet);
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

            revertToggleState(switchToNeet);
        }
    }

    private void revertToggleState(boolean switchToNeet) {

        toggleButton.setOnCheckedChangeListener(null);

        toggleButton.setChecked(!switchToNeet);

        toggleButton.setOnCheckedChangeListener(this::onToggleChanged);
    }

    private void refreshHomeScreen() {
        if (getActivity() == null) return;
        homeScreenViewModel.removeDataModel();
        Intent intent = new Intent(requireActivity(), HomeScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        requireActivity().finish();
    }

    private void loadToggleFromCache() {

        String switchInfoJson = MyApplication.getStringData(PreferenceKeys.SWITCHABLE_COURSE_INFO.getKey());

        if (switchInfoJson == null || switchInfoJson.isEmpty()) {
            Log.d(TAG, "No switchable course info in cache");
            toggle_container.setVisibility(View.GONE);
            return;
        }

        switchableCourseClassInfo =
                gson.fromJson(switchInfoJson, SwitchUserClassCourseRequestDto.class);

        boolean isNeetMode = MyApplication.getBoolData(PreferenceKeys.NEET_TOGGLE_STATE.getKey());

        toggle_container.setVisibility(View.VISIBLE);

        toggleButton.setOnCheckedChangeListener(null);
        toggleButton.setChecked(isNeetMode);

        applyToggleState(isNeetMode);

        toggleButton.setOnCheckedChangeListener(this::onToggleChanged);

        Log.d(TAG, "Toggle loaded from cache: " + isNeetMode);
    }


}
