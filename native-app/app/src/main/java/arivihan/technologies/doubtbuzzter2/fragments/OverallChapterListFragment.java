package arivihan.technologies.doubtbuzzter2.fragments;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_OVERALL_CHAPTERS;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.MicrolectureListActivity;
import arivihan.technologies.doubtbuzzter2.adapter.ChapterNameAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.enums.ChapterEnum.ChapterStatus;
import arivihan.technologies.doubtbuzzter2.enums.ChapterEnum.ChapterType;
import arivihan.technologies.doubtbuzzter2.enums.SubscriptionPopupType;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.model.ChapterListInfoDto;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.model.ChapterNameModel;
import arivihan.technologies.doubtbuzzter2.model.ThemeEnum;
import arivihan.technologies.doubtbuzzter2.model.chapter.live.ChapterPopUpDto;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class OverallChapterListFragment extends Fragment
        implements ChapterNameAdapter.OnChapClickListener {

    private static final String TAG = OverallChapterListFragment.class.getSimpleName();

    // Flat-mode views
    private LinearLayout fl_parent_layout;
    private RecyclerView rv_topics;
    private ChapterNameAdapter adapter;
    private LinearLayout ll_no_data;
    private ShimmerFrameLayout shimmer;
    private FrameLayout fl_flat_content;

    // Grouped-mode views
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private RelativeLayout rl_tab_section;
    private CardView cv_tab_left_scroll, cv_tab_right_scroll;
    private TabLayoutMediator tabLayoutMediator;

    // Data
    private String batchAddOnCode;
    private boolean overall;
    private String featureTag;
    private List<ChapterNameModel> chapterNameModels = new ArrayList<>();
    private List<String> chapterIdList = new ArrayList<>();
    private String queryText;
    private Dialog liveDialog;

    // Grouped data (overall=false)
    private final List<List<ChapterNameModel>> groupedChapters = new ArrayList<>();
    private final List<String> subjectTabNames = new ArrayList<>();

    private final Map<String, ValueEventListener> chapterListeners = new HashMap<>();
    private final Map<String, DatabaseReference> chapterRefs = new HashMap<>();

    private long lastApiCallTime = 0;
    private static final long API_COOLDOWN = 5000;

    private ThemeEnum currActivityTheme = ThemeEnum.LIGHT;

    public OverallChapterListFragment() {}

    public static OverallChapterListFragment newInstance(String batchAddOnCode, boolean overall, String featureTag) {
        OverallChapterListFragment fragment = new OverallChapterListFragment();
        Bundle args = new Bundle();
        args.putString("batchAddOnCode", batchAddOnCode);
        args.putBoolean("overall", overall);
        args.putString("featureTag", featureTag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            batchAddOnCode = getArguments().getString("batchAddOnCode");
            overall = getArguments().getBoolean("overall", true);
            featureTag = getArguments().getString("featureTag");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overall_chapter_list, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onThemeChanged();
    }

    @Override
    public void onDestroy() {
        MyVolleySingleton.getInstance(requireContext()).getRequestQueue().cancelAll(TAG);
        dismissLivePopUp();
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        detachAllChapterListeners();
        if (tabLayoutMediator != null) {
            tabLayoutMediator.detach();
            tabLayoutMediator = null;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Init
    // ─────────────────────────────────────────────────────────────

    private void initViews(View view) {
        fl_parent_layout  = view.findViewById(R.id.fl_parent_layout);
        fl_flat_content   = view.findViewById(R.id.fl_flat_content);
        rv_topics         = view.findViewById(R.id.recycler);
        ll_no_data        = view.findViewById(R.id.no_result_found_layout);
        shimmer           = view.findViewById(R.id.shimmer_chapters);

        // Grouped-mode views
        rl_tab_section    = view.findViewById(R.id.rl_tab_section);
        tabLayout         = view.findViewById(R.id.tabs);
        viewPager         = view.findViewById(R.id.viewpager);
        cv_tab_left_scroll  = view.findViewById(R.id.cv_tab_left_scroll);
        cv_tab_right_scroll = view.findViewById(R.id.cv_tab_right_scroll);

        rv_topics.setVisibility(View.GONE);
        ll_no_data.setVisibility(View.GONE);
        shimmer.setVisibility(View.GONE);

        // Always show fl_flat_content during load so the shimmer is visible in both modes
        fl_flat_content.setVisibility(View.VISIBLE);
        rl_tab_section.setVisibility(View.GONE);
        viewPager.setVisibility(View.GONE);

        tabLayout.getViewTreeObserver().addOnScrollChangedListener(this::updateTabArrowsVisibility);
        cv_tab_left_scroll.setOnClickListener(v -> tabLayout.smoothScrollBy(-tabLayout.getWidth(), 0));
        cv_tab_right_scroll.setOnClickListener(v -> tabLayout.smoothScrollBy(tabLayout.getWidth(), 0));

        loadChapters();

        if ("PARIKSHA_ADHYAYAN".equals(featureTag)) {
            HashMap<String, String> screenParams = new HashMap<>();
            screenParams.put("batch_addon_code", batchAddOnCode != null ? batchAddOnCode : "");
            MyApplication.logFirebaseBundleCreation(
                    FirebaseEvents.pa_chapter_list_screen_view.getActionName(), screenParams);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // API
    // ─────────────────────────────────────────────────────────────

    private boolean canCallApi() {
        long now = System.currentTimeMillis();
        if (now - lastApiCallTime < API_COOLDOWN) return false;
        lastApiCallTime = now;
        return true;
    }

    private void loadChapters() {
        try {
            if (!canCallApi()) return;

            shimmer.startShimmer();
            shimmer.setVisibility(View.VISIBLE);
            detachAllChapterListeners();

            org.json.JSONObject body = new org.json.JSONObject();
            body.put("batchAddonCode", batchAddOnCode);
            body.put("subjectId", "");

            String url = CommonMethods.getUrlParam(GET_OVERALL_CHAPTERS, new HashMap<>());

            Log.d(TAG, "Overall Chapter API — POST " + url + " | body: " + body);

            MyJsonObjectRequest request = new MyJsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    response -> {
                        try {
                            Log.d(TAG, "Overall Chapter API response: " + response);

                            Gson gson = new Gson();
                            TypeToken<ApiWrappedResponse<List<ChapterListInfoDto>>> token =
                                    new TypeToken<ApiWrappedResponse<List<ChapterListInfoDto>>>() {};
                            ApiWrappedResponse<List<ChapterListInfoDto>> apiResponse =
                                    gson.fromJson(response.toString(), token.getType());

                            List<ChapterListInfoDto> data = apiResponse != null ? apiResponse.getData() : null;

                            if (overall) {
                                updateDataInRecyclerView(data);
                            } else {
                                updateDataGrouped(data);
                            }

                        } catch (Exception ex) {
                            Log.e(TAG, "Overall Chapter API parse error: " + ex.getMessage());
                            shimmer.stopShimmer();
                            shimmer.setVisibility(View.GONE);
                        }
                    },
                    new VolleyGlobalErrorListener(requireContext()) {
                        @Override
                        public void postError(VolleyError error) {
                            Log.e(TAG, "Overall Chapter API error: " + error.getMessage());
                            shimmer.stopShimmer();
                            shimmer.setVisibility(View.GONE);
                            CommonMethods.showNegativeSnackbar(fl_parent_layout, error.getMessage());
                        }
                    });

            request.setTag(TAG);
            MyVolleySingleton.getInstance(requireContext()).addToRequestQueue(request);

        } catch (Exception ex) {
            Log.e(TAG, "Overall Chapter API exception: " + ex.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Render — flat (overall=true)
    // ─────────────────────────────────────────────────────────────

    private void updateDataInRecyclerView(List<ChapterListInfoDto> list) {
        shimmer.stopShimmer();
        shimmer.setVisibility(View.GONE);

        if (list == null || list.isEmpty()) {
            rv_topics.setVisibility(View.GONE);
            ll_no_data.setVisibility(View.VISIBLE);
            return;
        }

        rv_topics.setVisibility(View.VISIBLE);
        ll_no_data.setVisibility(View.GONE);
        setFlatAdapter(list);
    }

    private void setFlatAdapter(List<ChapterListInfoDto> list) {
        List<ChapterNameModel> models = buildModels(list);

        chapterNameModels.clear();
        chapterNameModels.addAll(models);
        chapterIdList.clear();
        for (ChapterListInfoDto dto : list) chapterIdList.add(dto.getChapterId());

        String adapterSubjectName = !models.isEmpty() && models.get(0).getSubjectName() != null
                ? models.get(0).getSubjectName() : "";

        adapter = new ChapterNameAdapter(requireContext(), chapterNameModels, this,
                adapterSubjectName, ll_no_data, rv_topics, currActivityTheme);
        rv_topics.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv_topics.setAdapter(adapter);

        attachObserversForChapters(batchAddOnCode, chapterIdList);
    }

    // ─────────────────────────────────────────────────────────────
    // Render — grouped tabs (overall=false)
    // ─────────────────────────────────────────────────────────────

    private void updateDataGrouped(List<ChapterListInfoDto> list) {
        shimmer.stopShimmer();
        shimmer.setVisibility(View.GONE);

        if (list == null || list.isEmpty()) {
            fl_flat_content.setVisibility(View.VISIBLE);
            ll_no_data.setVisibility(View.VISIBLE);
            return;
        }

        LinkedHashMap<String, List<ChapterNameModel>> grouped = new LinkedHashMap<>();
        for (ChapterListInfoDto dto : list) {
            String key = StringUtils.isNotEmpty(dto.getSubjectGroupName())
                    ? dto.getSubjectGroupName()
                    : (StringUtils.isNotEmpty(dto.getSubjectName()) ? dto.getSubjectName() : "Other");
            if (!grouped.containsKey(key)) grouped.put(key, new ArrayList<>());
            grouped.get(key).add(buildModel(dto));
        }

        subjectTabNames.clear();
        groupedChapters.clear();
        subjectTabNames.addAll(grouped.keySet());
        groupedChapters.addAll(grouped.values());

        rl_tab_section.setVisibility(View.VISIBLE);
        viewPager.setVisibility(View.VISIBLE);
        fl_flat_content.setVisibility(View.GONE);

        viewPager.setAdapter(null);
        if (tabLayoutMediator != null) {
            tabLayoutMediator.detach();
            tabLayoutMediator = null;
        }

        SubjectPagerAdapter pagerAdapter = new SubjectPagerAdapter(this, groupedChapters);
        viewPager.setAdapter(pagerAdapter);

        tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setCustomView(
                        buildCustomTab(subjectTabNames.get(position), position == 0)));
        tabLayoutMediator.attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { updateTabUI(tab, true); }
            @Override public void onTabUnselected(TabLayout.Tab tab) { updateTabUI(tab, false); }
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        handleTabVisibility();
    }

    private View buildCustomTab(String title, boolean isSelected) {
        View tabView = LayoutInflater.from(requireContext())
                .inflate(R.layout.text_tab_layout, null);
        TextView tabText = tabView.findViewById(R.id.tab_text);
        tabText.setText(CommonMethods.getShortName(title));
        applyTabStyle(tabView, isSelected);
        return tabView;
    }

    private void updateTabUI(TabLayout.Tab tab, boolean isSelected) {
        View customView = tab.getCustomView();
        if (customView == null) return;
        applyTabStyle(customView, isSelected);
    }

    private void applyTabStyle(View tabView, boolean isSelected) {
        MaterialCardView tabCard = tabView.findViewById(R.id.tab_text_card);
        TextView tabText = tabView.findViewById(R.id.tab_text);
        if (isSelected) {
            tabCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.subscription_blue));
            tabCard.setStrokeWidth(0);
            tabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            tabText.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.nunito_semibold));
        } else {
            tabCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white));
            tabCard.setStrokeWidth(2);
            tabCard.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.tab_not_selected_text_boundary));
            tabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.tab_not_selected_text));
            tabText.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.nunito_regular));
        }
    }

    private void handleTabVisibility() {
        int subjectsWithData = 0;
        for (List<ChapterNameModel> s : groupedChapters) {
            if (s != null && !s.isEmpty()) subjectsWithData++;
        }
        if (subjectsWithData > 1) {
            tabLayout.setVisibility(View.VISIBLE);
            rl_tab_section.setVisibility(View.VISIBLE);
        } else {
            tabLayout.setVisibility(View.GONE);
            rl_tab_section.setVisibility(View.GONE);
        }
        viewPager.setVisibility(subjectsWithData > 0 ? View.VISIBLE : View.GONE);
    }

    private void updateTabArrowsVisibility() {
        try {
            int scrollX = tabLayout.getScrollX();
            int maxScroll = tabLayout.getChildAt(0).getMeasuredWidth() - tabLayout.getMeasuredWidth();
            cv_tab_left_scroll.setVisibility(scrollX > 0 ? View.VISIBLE : View.GONE);
            cv_tab_right_scroll.setVisibility(scrollX < maxScroll ? View.VISIBLE : View.GONE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Model builder
    // ─────────────────────────────────────────────────────────────

    private List<ChapterNameModel> buildModels(List<ChapterListInfoDto> list) {
        List<ChapterNameModel> models = new ArrayList<>();
        for (ChapterListInfoDto dto : list) models.add(buildModel(dto));
        return models;
    }

    private ChapterNameModel buildModel(ChapterListInfoDto dto) {
        return ChapterNameModel.builder()
                .id(dto.getChapterId())
                .newLecture(dto.isNewLecture())
                .imageUrl(dto.getImageUrl())
                .chapter(dto.getVideoTitle())
                .name(dto.getTeacherName() == null ? "Arivihan" : dto.getTeacherName())
                .locked(dto.isLocked())
                .lockMessage(dto.getLockMessage())
                .teacherIntroImage(dto.getTeacherIntroImage())
                .completed(dto.isCompleted())
                .notesUrl(dto.getNotesUrl())
                .board(dto.isBoard())
                .chapterType(dto.getChapterType())
                .chapterStatus(dto.getChapterStatus())
                .chapterCode(dto.getChapterCode())
                .unwatched(dto.isUnwatched())
                .type(dto.getType())
                .chapterLiveDto(dto.getChapterLiveDto())
                .subjectCode(dto.getSubjectCode())
                .subjectName(dto.getSubjectName())
                .subjectGroupName(dto.getSubjectGroupName())
                .subjectGroupId(dto.getSubjectGroupId())
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // Chapter click — shared by flat list and inner tab fragments
    // ─────────────────────────────────────────────────────────────

    @Override
    public void onChapClick(int position) {
        handleChapterClick(chapterNameModels.get(position));
    }

    public void handleChapterClick(ChapterNameModel chapterNameModel) {
        try {
            if (adapter != null) adapter.notifyDataSetChanged();

            HashMap<String, String> lectureParams = new HashMap<>();
            lectureParams.put("chapter_id", chapterNameModel.getId());
            lectureParams.put("chapter_name", chapterNameModel.getChapter());
            lectureParams.put("chapter_type", chapterNameModel.getChapterType());
            lectureParams.put("chapter_status", chapterNameModel.getChapterStatus());
            lectureParams.put("is_locked", String.valueOf(chapterNameModel.isLocked()));
            lectureParams.put("subject_code", chapterNameModel.getSubjectCode() != null ? chapterNameModel.getSubjectCode() : "");
            MyApplication.logFirebaseBundleCreation(
                    FirebaseEvents.chap_lecture_selected.getActionName(), lectureParams);

            if ("PARIKSHA_ADHYAYAN".equals(featureTag)) {
                HashMap<String, String> paChapParams = new HashMap<>();
                paChapParams.put("chapter_id", chapterNameModel.getId());
                paChapParams.put("chapter_name", chapterNameModel.getChapter());
                paChapParams.put("subject_code", chapterNameModel.getSubjectCode() != null ? chapterNameModel.getSubjectCode() : "");
                paChapParams.put("chapter_status", chapterNameModel.getChapterStatus() != null ? chapterNameModel.getChapterStatus() : "");
                paChapParams.put("is_locked", String.valueOf(chapterNameModel.isLocked()));
                MyApplication.logFirebaseBundleCreation(
                        FirebaseEvents.pa_chapter_clicked.getActionName(), paChapParams);
            }

            String lockDialogText    = null;
            String lockDialogBtnText = null;

            if (chapterNameModel.getLockMessage() != null &&
                    MyApplication.getFirebaseRemoteConfig()
                            .getBoolean("to_show_lock_msg_from_data_side_in_chapter")) {
                lockDialogText = chapterNameModel.getLockMessage().isEmpty()
                        ? getString(R.string.OOpsThisChapIsLocked)
                        : chapterNameModel.getLockMessage();
            }

            boolean isRecordedLecture = chapterNameModel.getType() == null
                    || chapterNameModel.getType().equalsIgnoreCase(ChapterType.RECORDED_CHAPTER.name());
            boolean isCompleted = chapterNameModel.isCompleted();

            if (isCompleted && MyApplication.getFirebaseRemoteConfig()
                    .getBoolean("to_show_lock_msg_from_data_side_in_chapter")) {
                lockDialogBtnText = getResources().getString(R.string.go_back_to_lecture);
            }

            MyApplication.saveData(PreferenceKeys.MICROLECTURE_DATA_RELOAD.getKey(), false);

            String id          = chapterNameModel.getId();
            String chapterName = chapterNameModel.getChapter();
            String introImage  = chapterNameModel.getTeacherIntroImage();
            String chapterCode = chapterNameModel.getChapterCode();
            String subjectCode = chapterNameModel.getSubjectCode();
            String subjectName = chapterNameModel.getSubjectName();

            if (!isRecordedLecture) isCompleted = true;

            if (chapterNameModel.isLocked()) {
                HashMap<String, String> lockParams = new HashMap<>();
                lockParams.put("chapter_id", id);
                lockParams.put("chapter_name", chapterName);
                MyApplication.logFirebaseBundleCreation(
                        FirebaseEvents.chap_locked_lecture_clicked.getActionName(), lockParams);
                showLockPopup();

            } else if (StringUtils.isNotEmpty(chapterNameModel.getChapterType())
                    && chapterNameModel.getChapterType().equals(ChapterType.RECORDED_CHAPTER.name())
                    && StringUtils.isNotEmpty(chapterNameModel.getChapterStatus())
                    && chapterNameModel.getChapterStatus().equals(ChapterStatus.COMING_SOON.name())) {

                if (chapterNameModel.getChapterLiveDto() != null) {
                    ChapterPopUpDto chapterPopUpDto =
                            chapterNameModel.getChapterLiveDto().getUpcomingStatusPopUp();
                    if (chapterPopUpDto != null
                            && StringUtils.isNotEmpty(chapterPopUpDto.getTitle())
                            && StringUtils.isNotEmpty(chapterPopUpDto.getSubTitle())) {
                        showLivePop(chapterPopUpDto, chapterNameModel.getChapterStatus());
                    }
                }

            } else if (StringUtils.isNotEmpty(chapterNameModel.getChapterType())
                    && chapterNameModel.getChapterType().equals(ChapterType.LIVE_CHAPTER.name())
                    && StringUtils.isNotEmpty(chapterNameModel.getChapterStatus())
                    && (chapterNameModel.getChapterStatus().equals(ChapterStatus.LIVE_SCHEDULED.name())
                    || chapterNameModel.getChapterStatus().equals(ChapterStatus.CONVERTING.name())
                    || chapterNameModel.getChapterStatus().equals(ChapterStatus.UPCOMING.name()))) {

                ChapterPopUpDto chapterPopUpDto =
                        chapterNameModel.getChapterLiveDto().getUpcomingStatusPopUp();
                if (chapterPopUpDto != null
                        && StringUtils.isNotEmpty(chapterPopUpDto.getTitle())
                        && StringUtils.isNotEmpty(chapterPopUpDto.getSubTitle())) {
                    HashMap<String, String> upcomingParams = new HashMap<>();
                    upcomingParams.put("chapter_id", id);
                    upcomingParams.put("chapter_name", chapterName);
                    upcomingParams.put("chapter_status", chapterNameModel.getChapterStatus());
                    MyApplication.logFirebaseBundleCreation(
                            FirebaseEvents.chap_upcoming_lecture_clicked.getActionName(), upcomingParams);
                    showLivePop(chapterPopUpDto, chapterNameModel.getChapterStatus());
                } else if (chapterNameModel.getChapterStatus()
                        .equalsIgnoreCase(ChapterStatus.CONVERTING.name())) {
                    CommonMethods.showPositiveSnackbar(fl_parent_layout,
                            getString(R.string.aapka_lecture_thodi_der_aa_jayega), 3000);
                }

            } else if (StringUtils.isNotEmpty(chapterNameModel.getChapterType())
                    && chapterNameModel.getChapterType().equals(ChapterType.LIVE_CHAPTER.name())
                    && StringUtils.isNotEmpty(chapterNameModel.getChapterStatus())
                    && chapterNameModel.getChapterStatus().equals(ChapterStatus.LIVE.name())) {

                HashMap<String, String> liveParams = new HashMap<>();
                liveParams.put("chapter_id", id);
                liveParams.put("chapter_name", chapterName);
                MyApplication.logFirebaseBundleCreation(
                        FirebaseEvents.chap_live_lecture_join_attempt.getActionName(), liveParams);

                if (chapterNameModel.getChapterLiveDto() != null
                        && StringUtils.isNotEmpty(chapterNameModel.getChapterLiveDto().getLiveUrl())) {
                    CommonMethods.openUrlExternally(requireContext(),
                            chapterNameModel.getChapterLiveDto().getLiveUrl());
                } else {
                    CommonNavigator.navigateToLiveClassWebView(requireContext(), chapterName,
                            subjectName, subjectCode, id, chapterCode,
                            chapterNameModel.getImageUrl(), chapterNameModel.getName(),
                            TAG, new Bundle());
                }

            } else {
                MyApplication.saveData("overall_chapters", 0);
                Intent intent = new Intent(requireContext(), MicrolectureListActivity.class);
                if (StringUtils.isNotEmpty(introImage))
                    intent.putExtra("IntroImage", introImage);
                if (StringUtils.isNotEmpty(chapterNameModel.getNotesUrl()))
                    intent.putExtra("NotesPdfUrl", chapterNameModel.getNotesUrl());
                intent.putExtra("chapterName", chapterName);
                intent.putExtra("subjectCode", subjectCode);
                intent.putExtra("chapterId", id);
                intent.putExtra("selectedSubject", subjectName);
                MyApplication.saveData(PreferenceKeys.MICROLECTURE_ID.getKey(), "-1");

                HashMap<String, String> params = new HashMap<>();
                params.put("Chapter_Name", chapterName);
                params.put("Subject_Code", subjectCode);
                params.put("Chapter_Id", id);
                params.put("chapterCode", chapterCode);
                params.put("navigating_from", TAG);
                MyApplication.logFirebaseBundleCreation(
                        FirebaseEvents.Chapter_clicked.getActionName(), params);

                HashMap<String, String> recordedParams = new HashMap<>();
                recordedParams.put("chapter_id", id);
                recordedParams.put("chapter_name", chapterName);
                MyApplication.logFirebaseBundleCreation(
                        FirebaseEvents.chap_recorded_lecture_started.getActionName(), recordedParams);

                startActivity(intent);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInfoClicked(String imageUrl) {
        showPopupDialog(imageUrl);
    }

    // ─────────────────────────────────────────────────────────────
    // Theme
    // ─────────────────────────────────────────────────────────────

    public void onThemeChanged() {
        try {
            if (getContext() == null || getView() == null) return;

            int bgColor = ContextCompat.getColor(requireContext(), R.color.white);

            if (fl_parent_layout != null) fl_parent_layout.setBackgroundColor(bgColor);
            if (rv_topics        != null) rv_topics.setBackgroundColor(bgColor);

            if (ll_no_data != null) {
                ll_no_data.setBackgroundColor(bgColor);
                TextView tvNoDataTitle = ll_no_data.findViewById(R.id.tv_no_data_title);
                if (tvNoDataTitle != null)
                    tvNoDataTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            }

            if (adapter != null) adapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Filter
    // ─────────────────────────────────────────────────────────────

    public void setFilter(String filter) {
        try {
            queryText = filter;
            if (overall) {
                if (adapter != null) adapter.getFilter().filter(filter);
            } else {
                for (Fragment f : getChildFragmentManager().getFragments()) {
                    if (f instanceof SubjectPageFragment) {
                        ((SubjectPageFragment) f).applyFilter(filter);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Dialogs
    // ─────────────────────────────────────────────────────────────

    private void showPopupDialog(String url) {
        try {
            Dialog dialogHome = new Dialog(requireContext());
            dialogHome.setContentView(R.layout.custom_home_dialogue);

            Window window = dialogHome.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(
                        requireContext().getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
            dialogHome.setCancelable(false);

            CardView cancel = dialogHome.findViewById(R.id.cancel);
            ImageView image = dialogHome.findViewById(R.id.image);

            if (StringUtils.isNotEmpty(url)) {
                try {
                    Glide.with(this).load(url).into(image);
                } catch (Exception e) {
                    Glide.with(this).asGif().load(R.raw.image_gif).into(image);
                }
            } else {
                Glide.with(this).asGif().load(R.raw.image_gif).into(image);
            }

            MyApplication.saveData(PreferenceKeys.POPUP_STORE_TIME.getKey(),
                    System.currentTimeMillis() + 43200000);
            cancel.setOnClickListener(v -> dialogHome.dismiss());
            dialogHome.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showLockPopup() {
        HashMap<String, String> subscriptionParams = new HashMap<>();
        subscriptionParams.put("source_screen", TAG);
        MyApplication.logFirebaseBundleCreation(
                FirebaseEvents.chap_locked_subs_screen_opened.getActionName(), subscriptionParams);
        String popupType = "PARIKSHA_ADHYAYAN".equals(featureTag)
                ? SubscriptionPopupType.PARIKSHA_ADHYAYAN.name()
                : SubscriptionPopupType.LECTURES.name();
        Intent intent = SubscriptionNavigationHelper.getSubscriptionPageIntent(
                requireActivity(), popupType, RedirectionSource.CHAPTER_LIST_JOIN);
        startActivity(intent);
    }

    private void showLivePop(ChapterPopUpDto chapterPopUpDto, String chapterStatus) {
        if (chapterPopUpDto == null || (liveDialog != null && liveDialog.isShowing())) return;
        try {
            liveDialog = new Dialog(requireActivity());
            liveDialog.setContentView(R.layout.dialog_alert_title_subtitle_button);
            if (liveDialog.getWindow() != null) {
                liveDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
                liveDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
            liveDialog.setCancelable(true);
            liveDialog.setCanceledOnTouchOutside(true);
            TextView title       = liveDialog.findViewById(R.id.title);
            TextView subtitle    = liveDialog.findViewById(R.id.subtitle);
            TextView btnTextView = liveDialog.findViewById(R.id.btnText);
            CardView cv_confirm  = liveDialog.findViewById(R.id.cv_okay);
            ImageView iv_close   = liveDialog.findViewById(R.id.iv_close);
            ImageView dialogImage = liveDialog.findViewById(R.id.selection_image);
            LottieAnimationView animation = liveDialog.findViewById(R.id.animation);

            if (chapterStatus.equals(ChapterStatus.RESCHEDULED.name())) {
                dialogImage.setVisibility(View.VISIBLE);
                Glide.with(requireContext()).load(R.drawable.live_rescheduled).into(dialogImage);
                animation.setVisibility(View.GONE);
            } else if (chapterStatus.equals(ChapterStatus.COMING_SOON.name())) {
                dialogImage.setVisibility(View.VISIBLE);
                Glide.with(requireContext()).load(R.drawable.lecture_coming_soon).into(dialogImage);
                animation.setVisibility(View.GONE);
            } else {
                animation.setAnimation(R.raw.live_lottie_anim);
                dialogImage.setVisibility(View.GONE);
                animation.setVisibility(View.VISIBLE);
            }

            title.setVisibility(View.GONE);
            subtitle.setVisibility(View.GONE);
            btnTextView.setVisibility(View.GONE);

            if (StringUtils.isNotEmpty(chapterPopUpDto.getTitle())) {
                title.setText(CommonMethods.isValidJson(chapterPopUpDto.getTitle())
                        ? StyledTextBuilder.fromJson(requireActivity(), chapterPopUpDto.getTitle())
                        : chapterPopUpDto.getTitle());
                title.setVisibility(View.VISIBLE);
            }
            if (StringUtils.isNotEmpty(chapterPopUpDto.getSubTitle())) {
                subtitle.setText(CommonMethods.isValidJson(chapterPopUpDto.getSubTitle())
                        ? StyledTextBuilder.fromJson(requireActivity(), chapterPopUpDto.getSubTitle())
                        : chapterPopUpDto.getSubTitle());
                subtitle.setVisibility(View.VISIBLE);
            }
            if (StringUtils.isNotEmpty(chapterPopUpDto.getBtnName())) {
                btnTextView.setText(CommonMethods.isValidJson(chapterPopUpDto.getBtnName())
                        ? StyledTextBuilder.fromJson(requireActivity(), chapterPopUpDto.getBtnName())
                        : chapterPopUpDto.getBtnName());
                btnTextView.setVisibility(View.VISIBLE);
            }

            cv_confirm.setOnClickListener(v -> dismissLivePopUp());
            iv_close.setOnClickListener(v -> {
                HashMap<String, String> params = new HashMap<>();
                params.put("popup_type", "upcoming_lecture");
                MyApplication.logFirebaseBundleCreation(
                        FirebaseEvents.chap_upcoming_popup_closed.getActionName(), params);
                dismissLivePopUp();
            });

            HashMap<String, String> p = new HashMap<>();
            p.put("popup_type", "upcoming_lecture");
            MyApplication.logFirebaseBundleCreation(
                    FirebaseEvents.chap_upcoming_popup_viewed.getActionName(), p);

            liveDialog.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void dismissLivePopUp() {
        if (liveDialog != null && liveDialog.isShowing()) {
            liveDialog.dismiss();
            liveDialog = null;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Firebase realtime listeners
    // ─────────────────────────────────────────────────────────────

    private void attachObserversForChapters(String groupKey, List<String> chapterIds) {
        if (StringUtils.isEmpty(groupKey)) return;
        for (String chapterId : chapterIds) attachChapterListener(groupKey, chapterId);
    }

    private void attachChapterListener(String groupKey, String chapterId) {
        String key = groupKey + "_" + chapterId;

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("chapter_status_update")
                .child(groupKey)
                .child(chapterId);

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    if (!snapshot.exists()) return;
                    String status = snapshot.getValue(String.class);
                    if (StringUtils.isNotEmpty(status) && chapterIdList.contains(chapterId)) {
                        int index = chapterIdList.indexOf(chapterId);
                        ChapterNameModel model = chapterNameModels.get(index);
                        if (StringUtils.isNotEmpty(model.getChapterStatus())
                                && !model.getChapterStatus().equals(status)) {
                            loadChapters();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CHAPTER_OBSERVER", "Error for " + key + " -> " + error.getMessage());
            }
        };

        ref.addValueEventListener(listener);
        chapterListeners.put(key, listener);
        chapterRefs.put(key, ref);
    }

    private void detachAllChapterListeners() {
        try {
            for (String key : chapterListeners.keySet()) {
                DatabaseReference ref       = chapterRefs.get(key);
                ValueEventListener listener = chapterListeners.get(key);
                if (ref != null && listener != null) ref.removeEventListener(listener);
            }
            chapterListeners.clear();
            chapterRefs.clear();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Inner: ViewPager2 adapter
    // ─────────────────────────────────────────────────────────────

    private static class SubjectPagerAdapter extends FragmentStateAdapter {
        private final List<List<ChapterNameModel>> groupedChapters;

        SubjectPagerAdapter(@NonNull Fragment fragment,
                            List<List<ChapterNameModel>> groupedChapters) {
            super(fragment);
            this.groupedChapters = groupedChapters;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return SubjectPageFragment.newInstance(
                    new ArrayList<>(groupedChapters.get(position)));
        }

        @Override
        public int getItemCount() {
            return groupedChapters != null ? groupedChapters.size() : 0;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Inner: per-subject tab page fragment
    // ─────────────────────────────────────────────────────────────

    public static class SubjectPageFragment extends Fragment
            implements ChapterNameAdapter.OnChapClickListener {

        private ArrayList<ChapterNameModel> chapters;
        private ChapterNameAdapter pageAdapter;
        private RecyclerView recyclerView;
        private LinearLayout llNoData;

        public static SubjectPageFragment newInstance(ArrayList<ChapterNameModel> chapters) {
            SubjectPageFragment f = new SubjectPageFragment();
            Bundle args = new Bundle();
            args.putSerializable("chapters", chapters);
            f.setArguments(args);
            return f;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                chapters = (ArrayList<ChapterNameModel>) getArguments().getSerializable("chapters");
            }
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_subject_chapters_page, container, false);
            recyclerView = view.findViewById(R.id.rv_subject_chapters);
            llNoData     = view.findViewById(R.id.ll_no_data);

            if (chapters == null || chapters.isEmpty()) {
                llNoData.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                return view;
            }

            String subjectName = chapters.get(0).getSubjectName() != null
                    ? chapters.get(0).getSubjectName() : "";

            pageAdapter = new ChapterNameAdapter(requireContext(), chapters, this,
                    subjectName, llNoData, recyclerView, ThemeEnum.LIGHT);
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            recyclerView.setAdapter(pageAdapter);
            return view;
        }

        @Override
        public void onChapClick(int position) {
            if (chapters == null || position >= chapters.size()) return;
            Fragment parent = getParentFragment();
            if (parent instanceof OverallChapterListFragment) {
                ((OverallChapterListFragment) parent).handleChapterClick(chapters.get(position));
            }
        }

        @Override
        public void onInfoClicked(String imageUrl) {
            Fragment parent = getParentFragment();
            if (parent instanceof OverallChapterListFragment) {
                ((OverallChapterListFragment) parent).onInfoClicked(imageUrl);
            }
        }

        public void applyFilter(String filter) {
            if (pageAdapter != null) pageAdapter.getFilter().filter(filter);
        }
    }
}