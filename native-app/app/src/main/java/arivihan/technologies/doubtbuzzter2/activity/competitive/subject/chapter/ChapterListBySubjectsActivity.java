package arivihan.technologies.doubtbuzzter2.activity.competitive.subject.chapter;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_ONE_SHOT_LECTURES;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_TODAY_CLASS_BY_BATCH_ADD_ON;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.MARKED_VIDEO_COMPLETION;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.POST_CHAPTER_DEMO_PROGRESS;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.VIDEO_COMPLETION;

import android.app.Dialog;
import android.content.Intent;
import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.BaseActivity;
import arivihan.technologies.doubtbuzzter2.activity.CampaignSubscriptionActivity;
import arivihan.technologies.doubtbuzzter2.activity.MicrolectureListActivity;
import arivihan.technologies.doubtbuzzter2.activity.UnlockActivity;
import arivihan.technologies.doubtbuzzter2.adapter.TodayClassAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.TodayTaskListType;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.fragments.TodayTaskFragment;
import arivihan.technologies.doubtbuzzter2.fragments.competitive.subject.chapter.CompetitiveChapterBySubjectFragment;
import arivihan.technologies.doubtbuzzter2.helper.CompetitiveBannerView;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.listener.SubscriptionCallback;
import arivihan.technologies.doubtbuzzter2.listener.competitive.RefreshActivityListener;
import arivihan.technologies.doubtbuzzter2.model.NameAndIdDto;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionCheckDto;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionLockPopupDto;
import arivihan.technologies.doubtbuzzter2.model.chapter.live.ChapterPopUpDto;
import arivihan.technologies.doubtbuzzter2.model.competitive.banner.BannerDto;
import arivihan.technologies.doubtbuzzter2.model.competitive.lecture.OneShotChapterListResponse;
import arivihan.technologies.doubtbuzzter2.model.TodayClassDto;
import arivihan.technologies.doubtbuzzter2.model.crash_course.TodayClassItemResponse;
import arivihan.technologies.doubtbuzzter2.model.crash_course.TodayClassResponse;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.model.todo.TodayClassListModel;
import arivihan.technologies.doubtbuzzter2.model.remote_config.ActivityNavigationConfig;
import arivihan.technologies.doubtbuzzter2.viewModel.HomeScreenViewModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;

public class ChapterListBySubjectsActivity extends BaseActivity implements RefreshActivityListener, TodayClassAdapter.TodayClassAdapterListener {

    private static final String TAG = ChapterListBySubjectsActivity.class.getSimpleName();

    // UI Variables
    private LinearLayout toolbar;
    private ShimmerFrameLayout shimmer, shimmer_live_classes;
    private LinearLayout back_button_layout;
    private RelativeLayout rl_parent_layout, rl_content;
    private RelativeLayout rl_tab_layout, rl_live_classes, rl_live_content;
    private TextView activity_title, tv_live_classes, tv_see_all;
    private LinearLayout ll_see_all;
    private RecyclerView today_class_recycler_view;
    private TabLayout tabs;
    private CardView cv_tab_left_scroll, cv_tab_right_scroll;
    private ViewPager2 viewpager;
    private ViewPagerAdapter viewPagerAdapter;
    private SearchView searchView;

    // UI
    private Dialog lectureLiveUpcomingDialog;
    private Dialog lockPopUpDialog;

    // Data Variables
    boolean apiDataFetched;
    private Gson gson = new Gson();
    NetworkStatusLiveData networkStatusLiveData;
    private int tabsSize = 0;
    private String title;
    private String subjectId, subjectCode;
    private boolean hasSearchText, refreshPage;
    private String queryText = null;
    private HomeScreenViewModel homeScreenViewModel;
    private String boardCode, batchName;
    private String scrollToChapterId;

    private final ActivityResultLauncher<Intent> subscriptionScreenLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    try {
                        //isSubscribed
                        Intent data = result.getData();
                        boolean isSubscribed = data.getBooleanExtra("isSubscribed", false);
                        if (isSubscribed) {
                            getTodayClassData();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_list_by_subjects);

//        setSupportActionBar(toolbar);
//        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        Uri data = getIntent().getData();

        if (data != null) {
            // From deep link
            title = data.getQueryParameter("title");
            subjectId = data.getQueryParameter("SubjectId");
            subjectCode = data.getQueryParameter("SubjectCode");
            boardCode = data.getQueryParameter(PreferenceKeys.BOARD_CODE.getKey());
            batchName = data.getQueryParameter("batchName");
            scrollToChapterId = data.getQueryParameter("scrollToChapterId");

        } else {
            // From intent extras
            title = getIntent().getStringExtra("title");
            subjectId = getIntent().getStringExtra("SubjectId");
            subjectCode = getIntent().getStringExtra("SubjectCode");
            boardCode = getIntent().getStringExtra(PreferenceKeys.BOARD_CODE.getKey());
            batchName = getIntent().getStringExtra("batchName");
            scrollToChapterId = getIntent().getStringExtra("scrollToChapterId");
        }
        Log.d(TAG, "onCreate: " + boardCode);

        MyApplication myapp = (MyApplication) getApplication();
        homeScreenViewModel = myapp.getHomeScreenViewModel();
        if (StringUtils.isEmpty(title)) {
            title = MyApplication.getFirebaseRemoteConfig().getString("adaptive_chapters_by_subject_screen_title_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey())+"_v2");
        }

        toolbar = findViewById(R.id.toolbar);
        shimmer = findViewById(R.id.shimmer);
        shimmer_live_classes = findViewById(R.id.shimmer_live_classes);

        back_button_layout = findViewById(R.id.back_button_layout);
        rl_parent_layout = findViewById(R.id.rl_parent_layout);
        rl_content = findViewById(R.id.rl_content);
        rl_tab_layout = findViewById(R.id.rl_tab_layout);
        activity_title = findViewById(R.id.activity_title);
        tabs = findViewById(R.id.tabs);
        cv_tab_left_scroll = findViewById(R.id.cv_tab_left_scroll);
        cv_tab_right_scroll = findViewById(R.id.cv_tab_right_scroll);
        viewpager = findViewById(R.id.viewpager);

        rl_live_classes = findViewById(R.id.rl_live_classes);
        rl_live_content = findViewById(R.id.rl_live_content);
        tv_live_classes = findViewById(R.id.tv_live_classes);
        ll_see_all = findViewById(R.id.ll_see_all);
        tv_see_all = findViewById(R.id.tv_see_all);
        today_class_recycler_view = findViewById(R.id.today_class_recycler_view);

        ll_see_all.setVisibility(View.GONE);
        rl_live_classes.setVisibility(View.GONE);
        rl_live_content.setVisibility(View.GONE);

        shimmer.setVisibility(View.GONE);
        shimmer_live_classes.setVisibility(View.GONE);

        refreshPage = true;

        cv_tab_left_scroll.setOnClickListener(v -> tabs.smoothScrollBy(-tabs.getWidth(), 0));
        cv_tab_right_scroll.setOnClickListener(v -> tabs.smoothScrollBy(tabs.getWidth(), 0));

        if (StringUtils.isNotEmpty(title)) {
            activity_title.setText(title);
        }
        tabs.getViewTreeObserver().addOnScrollChangedListener(this::updateTabArrowsVisibility);

        back_button_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressedActivity("UI_BACK");
            }
        });
        setSearchView();
        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            if (isOnline) {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    CommonMethods.showPositiveSnackbar(rl_parent_layout, getString(R.string.InternetConnection));
                    CommonMethods.setInternetConnectStatusToSharedPreference(true);
                }
            } else {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                    CommonMethods.showNegativeSnackbar(rl_parent_layout, getString(R.string.NoInternetConnection));
                    CommonMethods.setInternetConnectStatusToSharedPreference(false);
                }
            }
        });

        if (CommonMethods.isOnline(this)) {
            if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                CommonMethods.setInternetConnectStatusToSharedPreference(true);
            }
        } else {
            if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                CommonMethods.setInternetConnectStatusToSharedPreference(false);
            }
            CommonMethods.showNegativeSnackbar(rl_parent_layout, getString(R.string.NoInternetConnection));
        }

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBackPressedActivity("NATIVE");
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        getTodayClassData();
    }

    @Override
    protected void onResume() {
        super.onResume();

        setChapterCompletionScreen();
        if (refreshPage) {
            fetchSubjects();
            refreshPage = false;
        }
    }

    private void getTodayClassData() {
        Map<String, Object> params = new HashMap<>();
        Log.d(TAG, "getTodayClassDataNonCompetitive request: " + params.toString());

        shimmer_live_classes.startShimmer();
        shimmer_live_classes.setVisibility(View.VISIBLE);
        rl_live_content.setVisibility(View.GONE);

        MyJsonObjectRequest todayClassData = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_TODAY_CLASS_BY_BATCH_ADD_ON, params), null,
                response -> {
                    if (isFinishing() || isDestroyed()) return;
                    Log.d(TAG, "getTodayClassDataNonCompetitive response: " + response);
                    Gson gson = new Gson();
                    TypeToken<ApiWrappedResponse<TodayClassResponse>> token = new TypeToken<ApiWrappedResponse<TodayClassResponse>>() {
                    };
                    ApiWrappedResponse<TodayClassResponse> apiResponse = gson.fromJson(response.toString(), token.getType());

                    TodayClassResponse todayClassResponse = apiResponse.getData();
                    if (todayClassResponse == null) {
                        shimmer_live_classes.stopShimmer();
                        shimmer_live_classes.setVisibility(View.GONE);
                        rl_live_content.setVisibility(View.VISIBLE);
                        return;
                    }
                    List<TodayClassItemResponse> dtos = todayClassResponse.getClasses();
                    if (dtos != null) TodayTaskFragment.checkForDemoLecture(dtos);
                    renderTodayClassUI(todayClassResponse);
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                Log.e(TAG, "getTodayClassDataNonCompetitive error", error);
                shimmer_live_classes.stopShimmer();
                shimmer_live_classes.setVisibility(View.GONE);
                rl_live_content.setVisibility(View.VISIBLE);
            }
        });
        todayClassData.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(todayClassData);
    }

    private void renderTodayClassUI(TodayClassResponse todayClassResponse) {
        if (todayClassResponse == null) {
            shimmer_live_classes.stopShimmer();
            shimmer_live_classes.setVisibility(View.GONE);
            rl_live_content.setVisibility(View.VISIBLE);
            return;
        }

        List<TodayClassItemResponse> dtos = todayClassResponse.getClasses();

        if (StringUtils.isNotEmpty(todayClassResponse.getLayoutTitle())) {
            tv_live_classes.setText(todayClassResponse.getLayoutTitle());
        }

        if (dtos != null) TodayTaskFragment.checkForDemoLecture(dtos);

        if (dtos != null && !dtos.isEmpty() && StringUtils.isNotEmpty(scrollToChapterId)) {
            rl_live_classes.setVisibility(View.VISIBLE);
            TodayTaskListType fragmentType = todayClassResponse.getFragmentType() != null
                    ? todayClassResponse.getFragmentType() : TodayTaskListType.TODAY_TASK_FRAGMENT;
            TodayClassAdapter todayClassAdapter = new TodayClassAdapter(this, fragmentType, dtos, this);
            today_class_recycler_view.setAdapter(null);
            today_class_recycler_view.setAdapter(todayClassAdapter);

            int targetIndex = -1;
            for (int i = 0; i < dtos.size(); i++) {
                if (scrollToChapterId.equals(dtos.get(i).getChapterId())) {
                    targetIndex = i;
                    break;
                }
            }
            if (targetIndex >= 0) {
                final int finalIndex = targetIndex;
                today_class_recycler_view.post(() -> {
                    RecyclerView.LayoutManager lm = today_class_recycler_view.getLayoutManager();
                    if (lm instanceof LinearLayoutManager) {
                        ((LinearLayoutManager) lm).scrollToPositionWithOffset(finalIndex, 0);
                    }
                });
            }
        }

        if (todayClassResponse.getSeeAllConfig() != null && todayClassResponse.getSeeAllConfig().getClassName() != null) {
            ActivityNavigationConfig seeAllConfig = todayClassResponse.getSeeAllConfig();
            String seeAllBtnText = seeAllConfig.getFeatureName();
            tv_see_all.setText(CommonMethods.isValidJson(seeAllBtnText) ? StyledTextBuilder.fromJson(this, seeAllBtnText) : seeAllBtnText);
            ll_see_all.setOnClickListener(v -> {
                HashMap<String, String> eventParams = new HashMap<>();
                eventParams.put("source", TAG);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.todays_classes_see_all_section_clicked.getActionName(), eventParams);
                CommonMethods.navigateFromActivityConfig(this, seeAllConfig.getNavigationType(),
                        seeAllConfig.getClassName(), seeAllConfig.getParams());
            });
            ll_see_all.setVisibility(View.VISIBLE);
        }

        shimmer_live_classes.stopShimmer();
        shimmer_live_classes.setVisibility(View.GONE);
        rl_live_content.setVisibility(View.VISIBLE);
    }

    private TodayClassResponse mapToTodayClassResponse(TodayClassListModel model) {
        List<TodayClassItemResponse> items = new ArrayList<>();
        if (model != null && model.getTodayClassDto() != null) {
            for (TodayClassDto dto : model.getTodayClassDto()) {
                items.add(TodayClassItemResponse.builder()
                        .chapterId(dto.getChapterId())
                        .chapterCode(dto.getChapterCode())
                        .chapterName(dto.getChapterName())
                        .chapterThumbnailUrl(dto.getChapterThumbnailUrl())
                        .chapterType(dto.getChapterType())
                        .chapterStatus(dto.getChapterStatus())
                        .chapterLiveDto(dto.getChapterLiveDto())
                        .microLectureId(dto.getMicroLectureId())
                        .microLectureTitle(dto.getMicroLectureTitle())
                        .microLectureViewCount(dto.getMicroLectureViewCount())
                        .microLectureViewCountStr(dto.getMicroLectureViewCountStr())
                        .viewCountText(dto.getViewCountText())
                        .thumbnailUrl(dto.getThumbnailUrl())
                        .subjectIconUrl(dto.getSubjectIconUrl())
                        .subjectId(dto.getSubjectId())
                        .subjectName(dto.getSubjectName())
                        .teacherName(dto.getTeacherName())
                        .position(dto.getPosition())
                        .duration(dto.getDuration())
                        .videoDuration(dto.getVideoDuration())
                        .type(dto.getType())
                        .isCompleted(dto.getIsCompleted())
                        .progress(dto.getProgress())
                        .locked(dto.isLocked())
                        .subscriptionLockPopupDto(dto.getSubscriptionLockPopupDto())
                        .demoLecture(dto.isDemoLecture())
                        .routineId(dto.getRoutineId())
                        .liveClassMeetingLink(dto.getLiveClassMeetingLink())
                        .liveClassDate(dto.getLiveClassDate())
                        .liveClassStatus(dto.getLiveClassStatus())
                        .recordedLectureUrl(dto.getRecordedLectureUrl())
                        .liveClassDuration(dto.getLiveClassDuration())
                        .liveClassSnackBarMessage(dto.getLiveClassSnackBarMessage())
                        .liveNavigationBundle(dto.getLiveNavigationBundle())
                        .title(dto.getTitle())
                        .subTitle(dto.getSubTitle())
                        .heading(dto.getHeading())
                        .btnText(dto.getBtnText())
                        .icon(dto.getIcon())
                        .deeplink(dto.getDeeplink())
                        .notesPdfUrl(dto.getNotesPdfUrl())
                        .className(dto.getClassName())
                        .navigationType(dto.getNavigationType())
                        .activityParams(dto.getActivityParams())
                        .notesData(dto.getNotesData())
                        .numberOfQuestions(dto.getNumberOfQuestions())
                        .newContent(dto.isNewContent())
                        .build());
            }
        }
        return TodayClassResponse.builder()
                .layoutTitle(model != null ? model.getLayoutTitle() : null)
                .fragmentType(model != null ? model.getFragmentType() : null)
                .layoutDescription(model != null ? model.getLayoutDescription() : null)
                .showTimer(model != null && model.isShowTimer())
                .guideVideo(model != null ? model.getGuideVideo() : null)
                .seeAllConfig(model != null ? model.getSeeAllConfig() : null)
                .classes(items)
                .build();
    }

    private void fetchSubjects() {

        shimmer.startShimmer();
        shimmer.setVisibility(View.VISIBLE);
        rl_content.setVisibility(View.GONE);

        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                CommonMethods.getUrlParam(GET_ONE_SHOT_LECTURES, new HashMap<>()), new JSONObject(),
                response -> {
                    try {
                        if (isFinishing() || isDestroyed()) return;
                        Log.d(TAG, "getChaptersBySubjectId: " + response);
                        Gson gson = new Gson();
                        TypeToken<ApiWrappedResponse<OneShotChapterListResponse>> token = new TypeToken<ApiWrappedResponse<OneShotChapterListResponse>>() {
                        };
                        ApiWrappedResponse<OneShotChapterListResponse> oneShotChapterListResponseApiWrappedResponse = gson.fromJson(response.toString(), token.getType());
                        OneShotChapterListResponse oneShotChapterListResponse = oneShotChapterListResponseApiWrappedResponse.getData();
                        Log.d("chapterList", "data " + oneShotChapterListResponse.toString());

                        shimmer.stopShimmer();
                        shimmer.setVisibility(View.GONE);

                        rl_content.setVisibility(View.VISIBLE);
                        setSubjects(oneShotChapterListResponse.getSubjects());
                        setUpBanner(oneShotChapterListResponse.getBanner());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
//                shimmer.stopShimmer();
//                shimmer.setVisibility(View.GONE);
                CommonMethods.showNegativeSnackbar(rl_parent_layout, error.getMessage());
            }
        });

        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
    }

//    private void getSubscribedSubjects() {
//        boolean isBoard = MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey());
//        Log.d(TAG, "getSubscribedSubjects: " + isBoard);
//        if (isBoard) {
//            homeScreenViewModel.getNonBoardSubjectList().removeObservers(this);
//            homeScreenViewModel.getBoardSubjectList().observeForever(this::setSubjects);
//        } else {
//            homeScreenViewModel.getBoardChatSubjectList().removeObservers(this);
//            homeScreenViewModel.getNonBoardSubjectList().observeForever(this::setSubjects);
//        }
//    }

    private void setChapterCompletionScreen() {
        boolean showChapterCompletionScreen = MyApplication.getBoolData(PreferenceKeys.ADAPTIVE_CHAPTER_COMPLETION_SCREEN_VISIBLE.getKey());
        if (showChapterCompletionScreen) {
            Intent intent = new Intent(this, UnlockActivity.class);
            intent.putExtra(UnlockActivity.BUNDLE_CONDITION_TYPE, "chapter");
            startActivity(intent);

            MyApplication.saveData(PreferenceKeys.ADAPTIVE_CHAPTER_COMPLETION_SCREEN_VISIBLE.getKey(), false);
        }
//        MyApplication.saveData(PreferenceKeys.ADAPTIVE_CHAPTER_COMPLETION_SCREEN_VISIBLE.getKey(), true);

    }

    private void onBackPressedActivity(String type) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("type", type != null ? type : "");
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.one_shot_lecture_back_click.getActionName(), params);
        } catch (Exception ignored) {
        }

        if (searchView != null && searchView.hasFocus()) {

            InputMethodManager imm =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
            }

            searchView.clearFocus();
            return;
        }
        finish();
    }


    private void setSearchView() {
        searchView = findViewById(R.id.search_view);
        searchView.setQueryHint(getResources().getString(R.string.search));

        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            // intentionally empty
        });

        ImageView searchButton = searchView.findViewById(R.id.search_button);
        searchButton.setOnClickListener(v -> {
            try {
                searchView.setIconified(false);
                searchView.setMaxWidth(Integer.MAX_VALUE);
                activity_title.setVisibility(View.GONE);

                Map<String, String> params = new HashMap<>();
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.one_shot_lecture_search_click.getActionName(), params);
            } catch (Exception ignored) {
            }
        });

        searchView.setOnCloseListener(() -> {
            activity_title.setVisibility(View.VISIBLE);
            return false;
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                setFilter(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                hasSearchText = StringUtils.isNotEmpty(s);
                setFilter(s);
                return true;
            }
        });
    }


    private void setUpBanner(BannerDto banner) {

        CompetitiveBannerView bannerView = findViewById(R.id.bannerView);
        if (banner != null) {
            bannerView.setBannerData(banner);
            bannerView.setVisibility(View.VISIBLE);
        } else {
            bannerView.setVisibility(View.GONE);
        }
    }

    public void setFilter(String filter) {
        try {
            Log.d(TAG, "Notes filter: " + filter);
            queryText = filter;
            Collection<Fragment> map = viewPagerAdapter.getAllFragments();
            for (Fragment fragment : map) {
                if (fragment instanceof CompetitiveChapterBySubjectFragment) {
                    ((CompetitiveChapterBySubjectFragment) fragment).setFilter(filter);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setSubscriptionCheckDtoToFragments(SubscriptionCheckDto dto) {
        try {
            Collection<Fragment> map = viewPagerAdapter.getAllFragments();
            for (Fragment fragment : map) {
                if (fragment instanceof CompetitiveChapterBySubjectFragment) {
                    ((CompetitiveChapterBySubjectFragment) fragment).setSubscriptionCheckDto(dto);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void setSubjects(List<NameAndIdDto> subjectList) {
//        List<SubjectResponse> localSubjectList = new ArrayList<>(subjectList);
//        localSubjectList.sort(Comparator.comparing(SubjectResponse::getPos));
        Log.d(TAG, "Subjects: " + subjectList.toString());
        apiDataFetched = true;
        setTabData(subjectList);
    }

    private void setTabData(List<NameAndIdDto> list) {
        try {
            ArrayList<String> subjectIds = new ArrayList<>();
            for (NameAndIdDto model : list) {
                subjectIds.add(model.getId());
            }
            int indexOfSelectedSubjectId = StringUtils.isNotEmpty(subjectId) ? subjectIds.indexOf(subjectId) : 0;

            Log.d(TAG, "Subject Id List: " + subjectIds.toString());
            viewPagerAdapter = new ViewPagerAdapter(this, list, title);
            viewpager.setAdapter(viewPagerAdapter);
            viewpager.setOffscreenPageLimit(viewPagerAdapter.getItemCount());

            TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(
                    tabs,
                    viewpager,
                    (tab, position) -> {
                        tab.setCustomView(getCustomTab(list.get(position).getName(), position == 0));
                    }
            );
            tabLayoutMediator.attach();

            tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    updateTabUI(tab, true);
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                    updateTabUI(tab, false);
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            });

            tabs.setVisibility(View.VISIBLE);
            viewpager.setVisibility(View.VISIBLE);
            tabsSize = subjectIds.size();

            MyApplication.getSubscriptionStatus(this, new SubscriptionCallback() {
                @Override
                public void onSuccess(SubscriptionCheckDto dto) {
                    Log.d(TAG, "onSuccess API call: " + dto);
                    setSubscriptionCheckDtoToFragments(dto);
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e("API_ERROR", errorMessage);
                }
            });

            viewpager.post(() -> setSelectedTab(indexOfSelectedSubjectId));
//            setSelectedTab(indexOfSelectedSubjectId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setSelectedTab(int index) {
        try {
            Log.d(TAG, "Selected Tab : " + index + ", " + subjectId);
            viewpager.setCurrentItem(index, true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateTabArrowsVisibility() {
        try {
            int scrollX = tabs.getScrollX();
            int maxScroll = tabs.getChildAt(0).getMeasuredWidth() - tabs.getMeasuredWidth();

            cv_tab_left_scroll.setVisibility(scrollX > 0 ? View.VISIBLE : View.GONE);
            cv_tab_right_scroll.setVisibility(scrollX < maxScroll ? View.VISIBLE : View.GONE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private View getCustomTab(String title, boolean isSelected) {
        View tabView = LayoutInflater.from(this).inflate(R.layout.text_tab_layout, null);
        MaterialCardView tabCard = tabView.findViewById(R.id.tab_text_card);
        TextView tabText = tabView.findViewById(R.id.tab_text);
        Typeface customFontUnselected = ResourcesCompat.getFont(this, R.font.nunito_regular);
        Typeface customFontSelected = ResourcesCompat.getFont(this, R.font.nunito_semibold);
//        tabText.setText(title);
        String shortName = CommonMethods.getShortName(title);
        tabText.setText(shortName);
//        tabText.setText(title);
        if (isSelected) {
            tabCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.subscription_blue));
            tabCard.setStrokeWidth(0);
            tabText.setTextColor(ContextCompat.getColor(this, R.color.white));
            tabText.setTypeface(customFontSelected);
        } else {
            tabCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white));
            tabCard.setStrokeWidth(2);
            tabCard.setStrokeColor(ContextCompat.getColor(this, R.color.tab_not_selected_text_boundary));
            tabText.setTextColor(ContextCompat.getColor(this, R.color.tab_not_selected_text));
            tabText.setTypeface(customFontUnselected);
        }
        return tabView;
    }

    private void updateTabUI(TabLayout.Tab tab, boolean isSelected) {
        View customView = tab.getCustomView();
        if (customView != null) {
            MaterialCardView tabCard = customView.findViewById(R.id.tab_text_card);
            TextView tabText = customView.findViewById(R.id.tab_text);
            Typeface customFontUnselected = ResourcesCompat.getFont(this, R.font.nunito_regular);
            Typeface customFontSelected = ResourcesCompat.getFont(this, R.font.nunito_semibold);
            if (isSelected) {
                tabCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.subscription_blue));
                tabCard.setStrokeWidth(0);
                tabText.setTextColor(ContextCompat.getColor(this, R.color.white));
                tabText.setTypeface(customFontSelected);
            } else {
                tabCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white));
                tabCard.setStrokeWidth(2);
                tabCard.setStrokeColor(ContextCompat.getColor(this, R.color.tab_not_selected_text_boundary));
                tabText.setTextColor(ContextCompat.getColor(this, R.color.tab_not_selected_text));
                tabText.setTypeface(customFontUnselected);
            }
        }
    }

    private void setDemoVideoOpen(String chapterCode, String chapterId) {
        try {
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("chapterCode", chapterCode != null ? chapterCode : "");
//            jsonObject.put("nodeId", "");
//            jsonObject.put("chapterId", chapterId != null ? chapterId : "");
//            jsonObject.put("demoLecture", true);

            Map<String, Object> requestParameters = new HashMap<>();
            requestParameters.put("chapterId", chapterId);
            Log.d(TAG, "Progress API Request: " + requestParameters.toString());

            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                    CommonMethods.getUrlParam(POST_CHAPTER_DEMO_PROGRESS, requestParameters), null,
                    response -> {
                        Log.d(TAG, "Progress API Response: " + response.toString());
                        if (CommonMethods.isActivitySafe(this)) {
                            getTodayClassData();
                        } else {
                            Log.w(TAG, "Fragment not in valid state, skipping fetchTodayClassData()");
                        }
                    }, error -> {
                Log.d(TAG, "Progress API Error Response: " + error.toString());
            });
            myJsonObjectRequest.setTag(TAG);
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showLiveUpcomingDialog(ChapterPopUpDto chapterPopUpDto, String chapterStatus) {
        try {
            if (chapterPopUpDto == null)
                return;
            dismissLiveUpcomingDialog();
            lectureLiveUpcomingDialog = CommonMethods.showGenericPopupInformation(this, chapterPopUpDto.getTitle(), chapterPopUpDto.getSubTitle(), chapterPopUpDto.getBtnName(),chapterStatus);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showLockDialog(SubscriptionLockPopupDto subscriptionLockPopupDto) {
        try {
            Log.d(TAG, "----> Dialog Lock: ");
            if (subscriptionLockPopupDto == null)
                subscriptionLockPopupDto = new SubscriptionLockPopupDto();
            dismissLockPopUpDialog();
            lockPopUpDialog = CommonMethods.showLockPopupDialog(this, subscriptionLockPopupDto);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void navigateToSubscriptionLockScreen(SubscriptionLockPopupDto subscriptionLockPopupDto) {
//        Intent intent = new Intent(this, CampaignSubscriptionActivity.class);
        Intent intent = SubscriptionNavigationHelper.getSubscriptionPageIntent(this, RedirectionSource.CHAPTER_LIST_LOCKED_POPUP);
        if (subscriptionLockPopupDto != null) {
            intent.putExtra(CampaignSubscriptionActivity.BUNDLE_SUBSCRIPTION_DATA, subscriptionLockPopupDto);
        }
        subscriptionScreenLauncher.launch(intent);
    }

    private void dismissLiveUpcomingDialog() {
        if (lectureLiveUpcomingDialog != null && lectureLiveUpcomingDialog.isShowing()) {
            lectureLiveUpcomingDialog.dismiss();
            lectureLiveUpcomingDialog = null;
        }
    }

    private void dismissLockPopUpDialog() {
        if (lockPopUpDialog != null && lockPopUpDialog.isShowing()) {
            lockPopUpDialog.dismiss();
            lockPopUpDialog = null;
        }
    }

    @Override
    public void refreshPage(boolean refresh) {
        refreshPage = refresh;
    }

    @Override
    public void doRefreshNow(boolean refresh) {
        if (refresh) {
            fetchSubjects();
        }
    }

    @Override
    public void showSnackBar(String liveClassSnackBarMessage) {
        CommonMethods.showLongPositiveSnackbar(rl_parent_layout, liveClassSnackBarMessage);
    }

    @Override
    public void markVideoCompletedStatus() {
        try {
            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                    MARKED_VIDEO_COMPLETION, null,
                    response -> {
                        Log.d("MARKED_VIDEO_COMPLETION", "Successfully updated.");
                    },
                    new VolleyGlobalErrorListener(this) {
                        @Override
                        public void postError(VolleyError error) {
                            Log.e("UPDATE_TODO_ORDER_STATE", error.getMessage());
                        }
                    });

            myJsonObjectRequest.setTag(TAG);
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void videoClick(String identifier) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("identifier", identifier);
            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                    VIDEO_COMPLETION, jsonObject,
                    response -> {
                        Log.d("VIDEO_COMPLETION", "Successfully updated.");
                    },
                    new VolleyGlobalErrorListener(this) {
                        @Override
                        public void postError(VolleyError error) {
                            Log.e("UPDATE_TODO_ORDER_STATE", error.getMessage());
                        }
                    });

            myJsonObjectRequest.setTag(TAG);
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void showPopUp(TodayClassAdapter.TodayClassPopUpTypes type, TodayClassItemResponse item) {
        try {
            if (type.equals(TodayClassAdapter.TodayClassPopUpTypes.LIVE_UPCOMING)) {
                if (item != null && item.getChapterLiveDto() != null && item.getChapterLiveDto().getUpcomingStatusPopUp() != null) {
                    showLiveUpcomingDialog(item.getChapterLiveDto().getUpcomingStatusPopUp(),item.getChapterStatus());
                }
            } else if (type.equals(TodayClassAdapter.TodayClassPopUpTypes.LOCK_POPUP)) {
                if (item != null && item.isLocked()) {
                    showLockDialog(item.getSubscriptionLockPopupDto());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onTodayClassClick(TodayClassItemResponse item, int position) {
        if (item.isLocked()) {
            navigateToSubscriptionLockScreen(item.getSubscriptionLockPopupDto());
        } else {
            Intent intent = new Intent(this,
                    MicrolectureListActivity.class);
            intent.putExtra("chapterId", item.getChapterId());
            intent.putExtra("chapterName", item.getChapterName());
            intent.putExtra("hideTapTarget", true);
            intent.putExtra("microLectureId", item.getMicroLectureId());
            intent.putExtra("openVideo", true);
            Map<String, String> params = new HashMap<>();
            params.put("parametersDetail", String.valueOf(intent));
            params.put("cardType", "lecture");
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.todays_task_card_clicked.getActionName(), params);
            startActivity(intent);

            if (item.isDemoLecture()) {
                MyApplication.saveSharedPreferencesDoNotDelete(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()) + "_" + item.getMicroLectureId(), true);
                setDemoVideoOpen(item.getChapterCode(), item.getChapterId());
            }
        }
    }


    class ViewPagerAdapter extends FragmentStateAdapter {
        private List<NameAndIdDto> subjectList;
        private String title;
        private final Map<Integer, Fragment> fragmentMap = new HashMap<>();

        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity,
                                List<NameAndIdDto> subjectList, String title) {
            super(fragmentActivity);
            this.subjectList = subjectList;
            this.title = title;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment fragment = CompetitiveChapterBySubjectFragment.newInstance(title, subjectList.get(position).getId(), subjectList.get(position).getName(), subjectList.get(position).getName(), boardCode);
            fragmentMap.put(position, fragment);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return subjectList.size();
        }

        public NameAndIdDto getTitle(int position) {
            return subjectList.get(position);
        }

        public Fragment getFragment(int position) {
            return fragmentMap.get(position);
        }

        public Collection<Fragment> getAllFragments() {
            return fragmentMap.values();
        }
    }
}