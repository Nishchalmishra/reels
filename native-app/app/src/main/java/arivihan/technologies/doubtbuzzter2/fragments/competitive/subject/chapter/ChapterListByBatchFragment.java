package arivihan.technologies.doubtbuzzter2.fragments.competitive.subject.chapter;

import static android.app.Activity.RESULT_OK;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_TODAY_CLASS_BY_BATCH_ADD_ON;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_TODAY_CLASS_BY_BOARD_CODE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.MARKED_VIDEO_COMPLETION;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.POST_CHAPTER_DEMO_PROGRESS;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.VIDEO_COMPLETION;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.CampaignSubscriptionActivity;
import arivihan.technologies.doubtbuzzter2.activity.MicrolectureListActivity;
import arivihan.technologies.doubtbuzzter2.activity.competitive.subject.chapter.ChapterListSubscribedActivity;
import arivihan.technologies.doubtbuzzter2.adapter.TodayClassAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.TodayTaskListType;
import arivihan.technologies.doubtbuzzter2.fragments.TodayTaskFragment;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.listener.SubscriptionCallback;
import arivihan.technologies.doubtbuzzter2.model.ThemeEnum;
import arivihan.technologies.doubtbuzzter2.model.TodayClassDto;
import arivihan.technologies.doubtbuzzter2.model.crash_course.TodayClassResponse;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.model.todo.TodayClassListModel;
import arivihan.technologies.doubtbuzzter2.model.subject.SubjectResponse;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionCheckDto;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionLockPopupDto;
import arivihan.technologies.doubtbuzzter2.model.chapter.live.ChapterPopUpDto;
import arivihan.technologies.doubtbuzzter2.model.crash_course.TodayClassItemResponse;
import arivihan.technologies.doubtbuzzter2.model.remote_config.ActivityNavigationConfig;
import arivihan.technologies.doubtbuzzter2.viewModel.HomeScreenViewModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class ChapterListByBatchFragment extends Fragment implements TodayClassAdapter.TodayClassAdapterListener {

    private static final String TAG = ChapterListByBatchFragment.class.getSimpleName();

    private View ll_parent_layout;
    private com.google.android.material.appbar.AppBarLayout app_bar_layout;
    private TextView tv_live_classes;
    private ShimmerFrameLayout sfl_shimmer;
    private LinearLayout ll_shimmer;
    private RecyclerView today_class_recycler_view;
    private RelativeLayout rl_tab_layout, ll_live_classes;
    private TabLayout tabs;
    private CardView cv_tab_left_scroll, cv_tab_right_scroll;
    private ImageView iv_tab_left_arrow, iv_tab_right_arrow;
    private ViewPager2 viewpager;
    private Dialog lectureLiveUpcomingDialog;
    private Dialog lockPopUpDialog;
    private ViewPagerAdapter viewPagerAdapter;
    private LinearLayout ll_see_all;
    private TextView tv_see_all;

    private HomeScreenViewModel homeScreenViewModel;
    private String queryText;
    private boolean isBatchAddOn;
    // Data Variables
    private String batchName, batchBoardCode, subjectId;
    private Integer batchId;
    private String type; // "LIVE", "PREMIUM", or null
    private String scrollToChapterId;
    private List<SubjectResponse> subjectModels;

    //theme
    public ThemeEnum currActivityTheme;

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

    public ChapterListByBatchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("id", batchId != null ? batchId : 0);
        outState.putString(PreferenceKeys.NAME.getKey(), batchName);
        outState.putString(PreferenceKeys.BOARD_CODE.getKey(), batchBoardCode);
        outState.putString("subjectId", subjectId);
        outState.putSerializable("subjects", (Serializable) subjectModels);
        outState.putBoolean("isBatchAddOn", isBatchAddOn);
        outState.putString("type", type);
    }

    public static ChapterListByBatchFragment newInstance(Integer id, String name, String boardCode, String subjectId, List<SubjectResponse> subjects, boolean isBatchAddOn) {
        return newInstance(id, name, boardCode, subjectId, subjects, isBatchAddOn, null);
    }

    public static ChapterListByBatchFragment newInstance(Integer id, String name, String boardCode, String subjectId, List<SubjectResponse> subjects, boolean isBatchAddOn, String type) {
        return newInstance(id, name, boardCode, subjectId, subjects, isBatchAddOn, type, null);
    }

    public static ChapterListByBatchFragment newInstance(Integer id, String name, String boardCode, String subjectId, List<SubjectResponse> subjects, boolean isBatchAddOn, String type, String scrollToChapterId) {
        ChapterListByBatchFragment fragment = new ChapterListByBatchFragment();
        Bundle args = new Bundle();
        args.putInt("id", id != null ? id : 0);
        args.putString(PreferenceKeys.NAME.getKey(), name);
        args.putString(PreferenceKeys.BOARD_CODE.getKey(), boardCode);
        args.putString("subjectId", subjectId);
        args.putSerializable("subjects", (Serializable) subjects);
        args.putBoolean("isBatchAddOn", isBatchAddOn);
        args.putString("type", type);
        args.putString("scrollToChapterId", scrollToChapterId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            try {
                batchId = getArguments().getInt("id");
                batchBoardCode = getArguments().getString(PreferenceKeys.BOARD_CODE.getKey());
                batchName = getArguments().getString(PreferenceKeys.NAME.getKey());
                subjectId = getArguments().getString("subjectId");
                subjectModels = (List<SubjectResponse>) getArguments().getSerializable("subjects");
                isBatchAddOn = getArguments().getBoolean("isBatchAddOn");
                type = getArguments().getString("type");
                scrollToChapterId = getArguments().getString("scrollToChapterId");
                Log.d(TAG, "ScrollTrace: ChapterListByBatchFragment.onCreate -> scrollToChapterId=" + scrollToChapterId + ", subjectId=" + subjectId + ", type=" + type);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        MyVolleySingleton.getInstance(requireContext()).getRequestQueue().cancelAll(TAG);
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chapter_list_by_batch, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        ll_live_classes = view.findViewById(R.id.rl_live_classes);
        ll_parent_layout = view.findViewById(R.id.ll_parent_layout);
        app_bar_layout = view.findViewById(R.id.app_bar_layout);
        tv_live_classes = view.findViewById(R.id.tv_live_classes);
        today_class_recycler_view = view.findViewById(R.id.today_class_recycler_view);
        rl_tab_layout = view.findViewById(R.id.rl_tab_layout);
        tabs = view.findViewById(R.id.tabs);
        cv_tab_left_scroll = view.findViewById(R.id.cv_tab_left_scroll);
        cv_tab_right_scroll = view.findViewById(R.id.cv_tab_right_scroll);
        iv_tab_left_arrow = view.findViewById(R.id.iv_tab_left_arrow);
        iv_tab_right_arrow = view.findViewById(R.id.iv_tab_right_arrow);
        viewpager = view.findViewById(R.id.viewpager);
        ll_shimmer = view.findViewById(R.id.ll_shimmer);
        sfl_shimmer = view.findViewById(R.id.sfl_shimmer);
        ll_see_all = view.findViewById(R.id.ll_see_all);
        tv_see_all = view.findViewById(R.id.tv_see_all);

        ll_live_classes.setVisibility(View.GONE);
        ll_shimmer.setVisibility(View.GONE);
        sfl_shimmer.setVisibility(View.GONE);
        ll_see_all.setVisibility(View.GONE);

        setShimmer(true);

        if (StringUtils.isNotEmpty(batchName)) {
            tv_live_classes.setText(String.format(batchName + getString(R.string.classes)));
        }

        MyApplication myapp = (MyApplication) requireContext().getApplicationContext();
        homeScreenViewModel = myapp.getHomeScreenViewModel();

        if(type==null || !type.equals(ChapterListSubscribedActivity.SectionType.PREMIUM.name())) {
            getTodayClassData();
        }
        getSubscribedSubjects();

    }

    private void getTodayClassData() {
        Map<String, Object> params = new HashMap<>();
        if (isBatchAddOn) {
            params.put("batchAddOnId", batchId);
        }
        Log.d(TAG, "--> Today's Task Request: " + params.toString());
        MyJsonObjectRequest todayClassData = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_TODAY_CLASS_BY_BATCH_ADD_ON, params),
                null,
                response -> {
                    try {
                        Log.d(TAG, "getTodayClassDataNonCompetitive: response received: " + response);
                        Gson gson = new Gson();
                        TypeToken<ApiWrappedResponse<TodayClassResponse>> token = new TypeToken<ApiWrappedResponse<TodayClassResponse>>() {
                        };
                        ApiWrappedResponse<TodayClassResponse> apiResponse = gson.fromJson(response.toString(), token.getType());

                        TodayClassResponse todayClassResponse = apiResponse.getData();
                        renderTodayClassUI(todayClassResponse);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        setShimmer(false);
                    }
                }, new VolleyGlobalErrorListener(getContext()) {
            @Override
            public void postError(VolleyError error) {
                Log.e(TAG, "getTodayClassData: Volley error occurred", error);
                setShimmer(false);
            }
        });
        todayClassData.setTag(TAG);
        MyVolleySingleton.getInstance(getContext()).addToRequestQueue(todayClassData);
    }

    private void renderTodayClassUI(TodayClassResponse liveClassResponse) {
        if (liveClassResponse == null) {
            setShimmer(false);
            return;
        }

        List<TodayClassItemResponse> dtos = liveClassResponse.getClasses();

        if (StringUtils.isNotEmpty(liveClassResponse.getLayoutTitle())) {
            tv_live_classes.setText(liveClassResponse.getLayoutTitle());
        }

        if (dtos != null) TodayTaskFragment.checkForDemoLecture(dtos);

        if (dtos != null && !dtos.isEmpty()) {
            ll_live_classes.setVisibility(View.VISIBLE);
            if (StringUtils.isNotEmpty(scrollToChapterId) && app_bar_layout != null) {
                app_bar_layout.post(() -> app_bar_layout.setExpanded(false, false));
            }
            TodayTaskListType fragmentType = liveClassResponse.getFragmentType() != null
                    ? liveClassResponse.getFragmentType() : TodayTaskListType.TODAY_TASK_FRAGMENT;
            TodayClassAdapter todayClassAdapter = new TodayClassAdapter(getContext(), fragmentType, dtos, this);
            today_class_recycler_view.setAdapter(null);
            today_class_recycler_view.setAdapter(todayClassAdapter);
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(scrollToChapterId)) {
                int scrollIndex = -1;
                for (int i = 0; i < dtos.size(); i++) {
                    Log.d("ChapterListByBatchFragment", "ScrollDebug: item[" + i + "] chapterId=" + dtos.get(i).getChapterId() + ", microLectureId=" + dtos.get(i).getMicroLectureId());
                    if (scrollToChapterId.equals(dtos.get(i).getChapterId())) {
                        scrollIndex = i;
                        break;
                    }
                }
                Log.d("ChapterListByBatchFragment", "ScrollDebug: scrollToChapterId=" + scrollToChapterId + ", resolvedIndex=" + scrollIndex + ", adapterItemCount=" + dtos.size());
                if (scrollIndex >= 0) {
                    final int targetIndex = scrollIndex;
                    today_class_recycler_view.post(() -> {
                        RecyclerView.LayoutManager lm = today_class_recycler_view.getLayoutManager();
                        if (lm instanceof LinearLayoutManager) {
                            ((LinearLayoutManager) lm).scrollToPositionWithOffset(targetIndex, 0);
                        } else {
                            today_class_recycler_view.smoothScrollToPosition(targetIndex);
                        }
                        Log.d("ChapterListByBatchFragment", "ScrollDebug: scrolled to index=" + targetIndex);
                    });
                }
            }
        }

        if (liveClassResponse.getSeeAllConfig() != null && liveClassResponse.getSeeAllConfig().getClassName() != null) {
            ActivityNavigationConfig seeAllConfig = liveClassResponse.getSeeAllConfig();
            String seeAllBtnText = seeAllConfig.getFeatureName();
            tv_see_all.setText(CommonMethods.isValidJson(seeAllBtnText) ? StyledTextBuilder.fromJson(requireContext(), seeAllBtnText) : seeAllBtnText);
            ll_see_all.setOnClickListener(v -> CommonMethods.navigateFromActivityConfig(requireContext(), seeAllConfig.getNavigationType(), seeAllConfig.getClassName(), seeAllConfig.getParams()));
            ll_see_all.setVisibility(View.VISIBLE);
        }

        setShimmer(false);
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
                        .duration(dto.getDuration())
                        .videoDuration(dto.getVideoDuration())
                        .type(dto.getType())
                        .isCompleted(dto.getIsCompleted())
                        .progress(dto.getProgress())
                        .locked(dto.isLocked())
                        .position(dto.getPosition())
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

    private void setShimmer(boolean turnOn) {
        Log.d(TAG, "Shimmer : " + turnOn);
        if (turnOn) {
            ll_shimmer.setVisibility(View.VISIBLE);
            sfl_shimmer.startShimmer();
            sfl_shimmer.setVisibility(View.VISIBLE);
        } else {
            ll_shimmer.setVisibility(View.GONE);
            sfl_shimmer.stopShimmer();
            sfl_shimmer.setVisibility(View.GONE);
        }
    }

    public void setFilter(String filter) {
        try {
            Log.d(TAG, "Notes filter: " + filter);
            queryText = filter;
            Collection<Fragment> map = viewPagerAdapter.getAllFragments();
            for (Fragment fragment : map) {
                if (fragment instanceof ChaptersBySubjectFragment) {
                    ((ChaptersBySubjectFragment) fragment).setFilter(filter);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Called by the host activity's onConfigurationChanged() after a night mode switch.
    // Refreshes existing inflated views that don't auto-update when Resources change.
    public void onThemeChanged(ThemeEnum theme) {
        currActivityTheme = theme;
        boolean isNightMode = (theme == ThemeEnum.DARK);

        try {
            if (getContext() == null) return;

            int bgColor = ContextCompat.getColor(requireContext(), R.color.bg_color);

            // Fragment root and tab container backgrounds
            if (ll_parent_layout != null)
                ll_parent_layout.setBackgroundColor(bgColor);
            if (app_bar_layout != null)
                app_bar_layout.setBackgroundColor(bgColor);
            if (rl_tab_layout != null)
                rl_tab_layout.setBackgroundColor(bgColor);
            if (tabs != null)
                tabs.setBackgroundColor(bgColor);
            if (viewpager != null)
                viewpager.setBackgroundColor(bgColor);
            Log.d(TAG, "onThemeChanged-ChapterListByBAtch: "+isNightMode);
            int scrollCardBg = isNightMode ? 0xFF2C2C2C : ContextCompat.getColor(requireContext(), R.color.white);
            int arrowTint   = isNightMode ? android.graphics.Color.WHITE : android.graphics.Color.BLACK;
            if (cv_tab_left_scroll != null)
                cv_tab_left_scroll.setCardBackgroundColor(scrollCardBg);
            if (cv_tab_right_scroll != null)
                cv_tab_right_scroll.setCardBackgroundColor(scrollCardBg);
            if (iv_tab_left_arrow != null)
                iv_tab_left_arrow.setColorFilter(arrowTint);
            if (iv_tab_right_arrow != null)
                iv_tab_right_arrow.setColorFilter(arrowTint);

            RecyclerView.Adapter<?> adapter = today_class_recycler_view != null
                    ? today_class_recycler_view.getAdapter() : null;
            if (adapter != null) adapter.notifyDataSetChanged();

            if (tv_live_classes != null)
                tv_live_classes.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));

            Collection<Fragment> frags = viewPagerAdapter != null
                    ? viewPagerAdapter.getAllFragments() : null;
            if (frags != null) {
                for (Fragment fragment : frags) {
                    if (fragment instanceof ChaptersBySubjectFragment) {
                        ((ChaptersBySubjectFragment) fragment).onThemeChanged();
                    }
                }
            }

            // Update subject tab text colors
            if (tabs != null) {
                tabs.setTabTextColors(
                        ContextCompat.getColor(requireContext(), R.color.text_secondary),
                        ContextCompat.getColor(requireContext(), R.color.text_primary));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getSubscribedSubjects() {
        if (subjectModels != null) {
            getSubjects(subjectModels);
        } else {
            homeScreenViewModel.getSubjectList().observeForever(this::getSubjects);
        }
    }

    public void setSubscriptionCheckDtoToFragments(SubscriptionCheckDto dto) {
        try {
            Collection<Fragment> map = viewPagerAdapter.getAllFragments();
            for (Fragment fragment : map) {
                if (fragment instanceof ChaptersBySubjectFragment) {
                    ((ChaptersBySubjectFragment) fragment).setSubscriptionCheckDto(dto);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void getSubjects(List<SubjectResponse> subjectList) {
        List<SubjectResponse> localSubjectList = new ArrayList<>(subjectList);
        localSubjectList.sort(Comparator.comparing(SubjectResponse::getPos));
        Log.d(TAG, "Subjects: " + subjectList.toString());
        setTabData(localSubjectList);
    }

    private void setTabData(List<SubjectResponse> list) {
        try {
            ArrayList<String> subjectIds = new ArrayList<>();
            for (SubjectResponse model : list) {
                subjectIds.add(model.getId());
            }
            int indexOfSelectedSubjectId = StringUtils.isNotEmpty(subjectId) ? subjectIds.indexOf(subjectId) : 0;

            Log.d(TAG, "Subjects: " + subjectId + ", index: " + indexOfSelectedSubjectId);
            Log.d(TAG, "ScrollTrace: setTabData -> scrollToChapterId=" + scrollToChapterId + ", selectedTabIndex=" + indexOfSelectedSubjectId + ", totalSubjects=" + list.size());

            viewPagerAdapter = new ViewPagerAdapter(requireActivity(), list, "", batchBoardCode, batchId, isBatchAddOn, type, currActivityTheme, scrollToChapterId);
            viewpager.setAdapter(viewPagerAdapter);
            viewpager.setOffscreenPageLimit(viewPagerAdapter.getItemCount());
            viewpager.setCurrentItem(indexOfSelectedSubjectId, false);

            new TabLayoutMediator(tabs, viewpager,
                    (tab, position) ->
                            tab.setText(CommonMethods.getShortName(
                                    list.get(position).getSubjectName()))
            ).attach();

            tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if (tab.view == null) return;
                    tab.view.setElevation(8f);
                    tab.view.setTranslationZ(8f);
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                    if (tab.view == null) return;
                    tab.view.setElevation(0f);
                    tab.view.setTranslationZ(0f);
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            });

            tabs.getViewTreeObserver().addOnScrollChangedListener(() -> {
                updateTabArrowsVisibility();
            });

            // Initial selection + centering
            tabs.post(() -> {
                TabLayout.Tab selectedTab = tabs.getTabAt(indexOfSelectedSubjectId);
                if (selectedTab != null && selectedTab.view != null) {
                    selectedTab.view.setElevation(8f);
                    selectedTab.view.setTranslationZ(8f);
                    selectedTab.select();

                    // Center the selected tab
                    int scrollX = selectedTab.view.getLeft()
                            - (tabs.getWidth() - selectedTab.view.getWidth()) / 2;

                    tabs.smoothScrollTo(scrollX, 0);
                }

                updateTabArrowsVisibility();
            });

            // ViewPager page change
            viewpager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    TabLayout.Tab tab = tabs.getTabAt(position);
                    if (tab != null) {
                        tab.select();

                        // Center tab on swipe
                        tabs.post(() -> {
                            if (tab.view == null) return;
                            int scrollX = tab.view.getLeft()
                                    - (tabs.getWidth() - tab.view.getWidth()) / 2;

                            tabs.smoothScrollTo(scrollX, 0);
                        });
                    }
                }
            });

            // Arrow click listeners (smooth scroll)
            cv_tab_left_scroll.setOnClickListener(v ->
                    tabs.smoothScrollBy(-200, 0));

            cv_tab_right_scroll.setOnClickListener(v ->
                    tabs.smoothScrollBy(200, 0));

            tabs.setVisibility(View.VISIBLE);
            viewpager.setVisibility(View.VISIBLE);

            // API call
            MyApplication.getSubscriptionStatus(requireContext(), new SubscriptionCallback() {
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

            setShimmer(false);

        } catch (Exception ex) {
            ex.printStackTrace();
            setShimmer(false);
        }
    }

    private void setSelectedTab(int index) {
        try {
//            Log.d(TAG, "Selected Tab : " + index + ", " + subjectId);
            viewpager.setCurrentItem(index, true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateTabArrowsVisibility() {
        if (tabs == null || tabs.getChildCount() == 0) return;

        int scrollX = tabs.getScrollX();
        int width = tabs.getWidth();
        int contentWidth = tabs.getChildAt(0).getWidth();
        cv_tab_left_scroll.setVisibility(scrollX > 0 ? View.VISIBLE : View.GONE);
        // Right Arrow
        cv_tab_right_scroll.setVisibility(
                scrollX + width < contentWidth ? View.VISIBLE : View.GONE
        );
    }

    private View getCustomTab(String title, boolean isSelected) {
        View tabView = LayoutInflater.from(requireContext()).inflate(R.layout.text_tab_layout, null);
        MaterialCardView tabCard = tabView.findViewById(R.id.tab_text_card);
        TextView tabText = tabView.findViewById(R.id.tab_text);
        Typeface customFontUnselected = ResourcesCompat.getFont(requireContext(), R.font.nunito_regular);
        Typeface customFontSelected = ResourcesCompat.getFont(requireContext(), R.font.nunito_semibold);
//        tabText.setText(title);
        String shortName = CommonMethods.getShortName(title);
        tabText.setText(shortName);
//        tabText.setText(title);
        if (isSelected) {
            tabCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.subscription_blue));
            tabCard.setStrokeWidth(0);
            tabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            tabText.setTypeface(customFontSelected);
        } else {
            tabCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white));
            tabCard.setStrokeWidth(2);
            tabCard.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.tab_not_selected_text_boundary));
            tabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.tab_not_selected_text));
            tabText.setTypeface(customFontUnselected);
        }
        return tabView;
    }

    private void updateTabUI(TabLayout.Tab tab, boolean isSelected) {
        View customView = tab.getCustomView();
        if (customView != null) {
            MaterialCardView tabCard = customView.findViewById(R.id.tab_text_card);
            TextView tabText = customView.findViewById(R.id.tab_text);
            Typeface customFontUnselected = ResourcesCompat.getFont(requireContext(), R.font.nunito_regular);
            Typeface customFontSelected = ResourcesCompat.getFont(requireContext(), R.font.nunito_semibold);
            if (isSelected) {
                tabCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.subscription_blue));
                tabCard.setStrokeWidth(0);
                tabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                tabText.setTypeface(customFontSelected);
            } else {
                tabCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white));
                tabCard.setStrokeWidth(2);
                tabCard.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.tab_not_selected_text_boundary));
                tabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.tab_not_selected_text));
                tabText.setTypeface(customFontUnselected);
            }
        }
    }

    private void showLiveUpcomingDialog(ChapterPopUpDto chapterPopUpDto,String chapterStatus) {
        Log.d(TAG, "showLiveUpcomingDialog: "+chapterStatus);
        try {
            if (chapterPopUpDto == null)
                return;
            dismissLiveUpcomingDialog();
            lectureLiveUpcomingDialog = CommonMethods.showGenericPopupInformation(requireContext(),
                    chapterPopUpDto.getTitle(), chapterPopUpDto.getSubTitle(),
                    chapterPopUpDto.getBtnName(),chapterStatus);
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
            lockPopUpDialog = CommonMethods.showLockPopupDialog(requireContext(), subscriptionLockPopupDto);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

    private void setDemoVideoOpen(String chapterCode, String chapterId) {
        try {

            Map<String, Object> requestParameters = new HashMap<>();
            requestParameters.put("chapterId", chapterId);
            Log.d(TAG, "Progress API Request: " + requestParameters.toString());

            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                    CommonMethods.getUrlParam(POST_CHAPTER_DEMO_PROGRESS, requestParameters), null,
                    response -> {
                        Log.d(TAG, "Progress API Response: " + response.toString());
                        if (isFragmentSafe()) {
                            getTodayClassData();
                        } else {
                            Log.w(TAG, "Fragment not in valid state, skipping fetchTodayClassData()");
                        }
                    }, error -> {
                Log.d(TAG, "Progress API Error Response: " + error.toString());
            });
            myJsonObjectRequest.setTag(TAG);
            MyVolleySingleton.getInstance(requireContext()).addToRequestQueue(myJsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isFragmentSafe() {
        return isAdded()
                && getActivity() != null
                && !isDetached()
                && getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED);
    }

    private void navigateToSubscriptionLockScreen(SubscriptionLockPopupDto subscriptionLockPopupDto) {
//        Intent intent = new Intent(requireContext(), CampaignSubscriptionActivity.class);
        Intent intent = SubscriptionNavigationHelper.getSubscriptionPageIntent(requireContext(), RedirectionSource.CHAPTER_LIST_LOCKED_POPUP);
        if (subscriptionLockPopupDto != null) {
            intent.putExtra(CampaignSubscriptionActivity.BUNDLE_SUBSCRIPTION_DATA, subscriptionLockPopupDto);
        }
        subscriptionScreenLauncher.launch(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MyVolleySingleton.getInstance(requireContext()).getRequestQueue().cancelAll(TAG);
        dismissLiveUpcomingDialog();
        dismissLockPopUpDialog();
    }

    @Override
    public void showSnackBar(String liveClassSnackBarMessage) {
        CommonMethods.showLongPositiveSnackbar(ll_parent_layout, liveClassSnackBarMessage);
    }

    @Override
    public void markVideoCompletedStatus() {
        try {
            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                    MARKED_VIDEO_COMPLETION, null,
                    response -> {
                        Log.d("MARKED_VIDEO_COMPLETION", "Successfully updated.");
                    },
                    new VolleyGlobalErrorListener(requireContext()) {
                        @Override
                        public void postError(VolleyError error) {
                            Log.e("UPDATE_TODO_ORDER_STATE", error.getMessage());
                        }
                    });

            myJsonObjectRequest.setTag(TAG);
            MyVolleySingleton.getInstance(requireContext()).addToRequestQueue(myJsonObjectRequest);
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
                    new VolleyGlobalErrorListener(requireContext()) {
                        @Override
                        public void postError(VolleyError error) {
                            Log.e("UPDATE_TODO_ORDER_STATE", error.getMessage());
                        }
                    });

            myJsonObjectRequest.setTag(TAG);
            MyVolleySingleton.getInstance(requireContext()).addToRequestQueue(myJsonObjectRequest);
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
            Intent intent = new Intent(requireContext().getApplicationContext(),
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
            requireContext().startActivity(intent);

            if (item.isDemoLecture()) {
                MyApplication.saveSharedPreferencesDoNotDelete(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()) + "_" + item.getMicroLectureId(), true);
                setDemoVideoOpen(item.getChapterCode(), item.getChapterId());
            }
        }
    }

    public static class ViewPagerAdapter extends FragmentStateAdapter {
        private List<SubjectResponse> subjectList;
        private String title;
        private String boardCode;
        private Integer batchId;
        private boolean isBatchAddOn;
        private String sectionType;
        private ThemeEnum currActivityTheme;
        private final String scrollToChapterId;
        private final Map<Integer, Fragment> fragmentMap = new HashMap<>();

        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity,
                                List<SubjectResponse> subjectList, String title, String boardCode, Integer batchId, boolean isBatchAddOn, String type, ThemeEnum currActivityTheme) {
            this(fragmentActivity, subjectList, title, boardCode, batchId, isBatchAddOn, type, currActivityTheme, null);
        }

        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity,
                                List<SubjectResponse> subjectList, String title, String boardCode, Integer batchId, boolean isBatchAddOn, String type, ThemeEnum currActivityTheme, String scrollToChapterId) {
            super(fragmentActivity);
            this.subjectList = subjectList;
            this.title = title;
            this.boardCode = boardCode;
            this.batchId = batchId;
            this.isBatchAddOn = isBatchAddOn;
            this.sectionType = type;
            this.currActivityTheme = currActivityTheme;
            this.scrollToChapterId = scrollToChapterId;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment fragment = ChaptersBySubjectFragment.newInstance(title, subjectList.get(position).getId(), subjectList.get(position).getSubjectName(), subjectList.get(position).getSubjectCode(), boardCode, batchId, isBatchAddOn, sectionType, true, currActivityTheme, scrollToChapterId);
            fragmentMap.put(position, fragment);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return subjectList.size();
        }

        public void updateDataList(ArrayList<SubjectResponse> newDataList) {
            this.subjectList = newDataList;
            fragmentMap.clear();
            notifyDataSetChanged();
        }

        public SubjectResponse getTitle(int position) {
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