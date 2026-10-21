package arivihan.technologies.doubtbuzzter2.fragments.competitive.subject.chapter;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_GET_CHAPTER_OF_SUBJECT;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_GET_CHAPTER_OF_SUBJECT_BATCH_ADD_ON;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_CHAPTER_OF_SUBJECT_V2;

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
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.MicrolectureListActivity;
import arivihan.technologies.doubtbuzzter2.activity.competitive.subject.chapter.ChapterListSubscribedActivity;
import arivihan.technologies.doubtbuzzter2.adapter.ChapterNameAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.common.bypass.ChapterLiveByPass;
import arivihan.technologies.doubtbuzzter2.enums.ChapterEnum.ChapterStatus;
import arivihan.technologies.doubtbuzzter2.enums.ChapterEnum.ChapterType;
import arivihan.technologies.doubtbuzzter2.enums.SubscriptionPopupType;
import arivihan.technologies.doubtbuzzter2.fragments.competitive.subject.CompetitiveSubjectChaptersFragment;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.model.ChapterListInfoDto;
import arivihan.technologies.doubtbuzzter2.model.ChapterNameModel;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionCheckDto;
import arivihan.technologies.doubtbuzzter2.model.ThemeEnum;
import arivihan.technologies.doubtbuzzter2.model.chapter.live.ChapterPopUpDto;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import lombok.Setter;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class ChaptersBySubjectFragment extends
        Fragment implements ChapterNameAdapter.OnChapClickListener {

    private static final String TAG = CompetitiveSubjectChaptersFragment.class.getSimpleName();

    // UI Variables
    private FrameLayout fl_parent_layout;
    private RecyclerView rv_topics;
    private ChapterNameAdapter adapter;
    private LinearLayout ll_no_data;
    private ShimmerFrameLayout shimmer;
    private ImageView iv_no_data;

    // Data Variables
    private String title, subjectId, subjectName, subjectCode, boardCode, type, scrollToChapterId;
    private Integer batchId;
    private boolean isBatchAddOn;
    private List<ChapterNameModel> chapterNameModels = new ArrayList<>();
    private List<String> chapterIdList = new ArrayList<>();
    private String queryText;
    @Setter
    private SubscriptionCheckDto subscriptionCheckDto;
    private Dialog liveDialog;

    private final Map<String, ValueEventListener> chapterListeners = new HashMap<>();
    private final Map<String, DatabaseReference> chapterRefs = new HashMap<>();

    private long lastApiCallTime = 0;
    private static final long API_COOLDOWN = 5000;

    private LinearLayout llGroupedContainer;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private boolean isNightMode = false;
    boolean hasGrouping = false;

    private ChapterTabAdapter viewPagerAdapter;

    // Activity theme
    private ThemeEnum currActivityTheme = ThemeEnum.LIGHT;

    private int pendingScrollIndex = -1;
    private View.OnLayoutChangeListener pendingScrollLayoutListener;


    public ChaptersBySubjectFragment() {
        // Required empty public constructor for Fragment restoration
    }

    public ChaptersBySubjectFragment(ThemeEnum currActivityTheme) {
        this.currActivityTheme = currActivityTheme;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("title", title);
        outState.putString("id", subjectId);
        outState.putString(PreferenceKeys.NAME.getKey(), subjectName);
        outState.putString("code", subjectCode);
        outState.putInt("batchId", batchId);
        outState.putBoolean("isBatchAddOn", isBatchAddOn);
        outState.putBoolean("isNightMode", false);
        outState.putString(PreferenceKeys.BOARD_CODE.getKey(), boardCode);
        outState.putString("type", type);
    }

    public static ChaptersBySubjectFragment newInstance(String title, String id, String name, String code, String boardCode, Integer batchId, boolean isBatchAddOn, String type, boolean isNightMode, ThemeEnum currActivityTheme) {
        return newInstance(title, id, name, code, boardCode, batchId, isBatchAddOn, type, isNightMode, currActivityTheme, null);
    }

    public static ChaptersBySubjectFragment newInstance(String title, String id, String name, String code, String boardCode, Integer batchId, boolean isBatchAddOn, String type, boolean isNightMode, ThemeEnum currActivityTheme, String scrollToChapterId) {
        ChaptersBySubjectFragment fragment = new ChaptersBySubjectFragment(currActivityTheme);
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("id", id);
        args.putString(PreferenceKeys.NAME.getKey(), name);
        args.putString("code", code);
        args.putBoolean("isNightMode", isNightMode);
        args.putString(PreferenceKeys.BOARD_CODE.getKey(), boardCode);
        args.putBoolean("isBatchAddOn", isBatchAddOn);
        args.putInt("batchId", batchId);
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
                subjectId = getArguments().getString("id");
                boardCode = getArguments().getString(PreferenceKeys.BOARD_CODE.getKey());
                subjectName = getArguments().getString(PreferenceKeys.NAME.getKey());
                subjectCode = getArguments().getString("code");
                title = getArguments().getString("title");
                batchId = getArguments().getInt("batchId");
                isBatchAddOn = getArguments().getBoolean("isBatchAddOn");
                type = getArguments().getString("type");
                isNightMode = getArguments().getBoolean("isNightMode");
                scrollToChapterId = getArguments().getString("scrollToChapterId");
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chapters_by_subject, container, false);
        Log.d(TAG, "Id: " + (StringUtils.isNotEmpty(subjectId) ? subjectId : ""));
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
    }

//    @Override
//    public void onConfigurationChanged(@NonNull Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        onThemeChanged();
//    }

    private void initViews(View view) {
        fl_parent_layout = view.findViewById(R.id.fl_parent_layout);
        rv_topics = view.findViewById(R.id.recycler);
        ll_no_data = view.findViewById(R.id.no_result_found_layout);
        shimmer = view.findViewById(R.id.shimmer_chapters);
        iv_no_data = view.findViewById(R.id.iv_no_data);   // ✅ added
        llGroupedContainer = view.findViewById(R.id.ll_grouped_container);
        tabLayout = view.findViewById(R.id.tab_layout_subjects);
        viewPager = view.findViewById(R.id.vp_subject_groups);

        rv_topics.setVisibility(View.GONE);
        ll_no_data.setVisibility(View.GONE);
        shimmer.setVisibility(View.GONE);

        rv_topics.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    pendingScrollIndex = -1;
                    if (pendingScrollLayoutListener != null) {
                        rv_topics.removeOnLayoutChangeListener(pendingScrollLayoutListener);
                        pendingScrollLayoutListener = null;
                    }
                }
            }
        });

        getChaptersBySubjectId(batchId, subjectId);
    }

    // ─────────────────────────────────────────────────────────────
    // Theme + Language
    // ─────────────────────────────────────────────────────────────

    public void onThemeChanged() {
        if (type != null && type.equals(ChapterListSubscribedActivity.SectionType.PREMIUM.name())) {
            currActivityTheme = ThemeEnum.DARK;
        } else {
            currActivityTheme = ThemeEnum.LIGHT;
        }

        try {
            if (getContext() == null || getView() == null) return;

            boolean isNight = (currActivityTheme == ThemeEnum.DARK);
            int bgColor     = ContextCompat.getColor(requireContext(), R.color.bg_color);
            int textColor   = ContextCompat.getColor(requireContext(), R.color.text_primary);

            if (fl_parent_layout != null)
                fl_parent_layout.setBackgroundColor(bgColor);

            if (rv_topics != null)
                rv_topics.setBackgroundColor(bgColor);

            if (llGroupedContainer != null)
                llGroupedContainer.setBackgroundColor(bgColor);

            if (ll_no_data != null) {
                ll_no_data.setBackgroundColor(bgColor);
                TextView tvNoDataTitle = ll_no_data.findViewById(R.id.tv_no_data_title);
                if (tvNoDataTitle != null) {
                    tvNoDataTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
                }
            }

            updateNoDataImage();

            if (tabLayout != null && tabLayout.getVisibility() == View.VISIBLE) {
//                int tabBg = isNight
//                        ? 0xFF2C2C2C
//                        : ContextCompat.getColor(requireContext(), R.color.white);
//                tabLayout.setBackgroundColor(tabBg);

                tabLayout.setBackgroundColor(bgColor);

                tabLayout.post(() -> {
                    for (int i = 0; i < tabLayout.getTabCount(); i++) {
                        TabLayout.Tab tab = tabLayout.getTabAt(i);
                        if (tab != null) {
                            updateInnerTabUI(tab, tab.isSelected());
                        }
                    }
                });
            }

            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateNoDataImage() {
        if (iv_no_data == null) return;

        String lang;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            lang = getResources().getConfiguration().getLocales().get(0).getLanguage();
        } else {
            lang = getResources().getConfiguration().locale.getLanguage();
        }

        boolean isHindi = lang.equals("hi");
        boolean isDark  = (currActivityTheme == ThemeEnum.DARK);

        // --- Image ---
        int drawableRes;
        if (isDark && isHindi) {
            drawableRes = R.drawable.premium_lecture_coming_soon_hi;
        } else if (isDark) {
            drawableRes = R.drawable.premium_lecture_coming_soon_en;
        } else if (isHindi) {
            drawableRes = R.drawable.coming_soon_live_hi;
        } else {
            drawableRes = R.drawable.coming_soon_live_en;
        }
        iv_no_data.setImageResource(drawableRes);

        // --- Text ---
        if (ll_no_data == null) return;
        TextView tvNoDataTitle = ll_no_data.findViewById(R.id.tv_no_data_title);
        if (tvNoDataTitle == null) return;

        int noDataStringRes;
        if (isDark && isHindi) {
            noDataStringRes = R.string.no_data_premium_hi;
        } else if (isDark) {
            noDataStringRes = R.string.no_data_premium_en;
        } else if (isHindi) {
            noDataStringRes = R.string.no_data_live_hi;
        } else {
            noDataStringRes = R.string.no_data_live_en;
        }
        tvNoDataTitle.setText(getString(noDataStringRes));
    }

    // ─────────────────────────────────────────────────────────────
    // Filter
    // ─────────────────────────────────────────────────────────────

    public void setFilter(String filter) {
        try {
            queryText = filter;
            if (hasGrouping){
                Collection<Fragment> map = viewPagerAdapter.getAllFragments();
                for (Fragment fragment : map) {
                    if (fragment instanceof ChapterListFragment) {
                        ((ChapterListFragment) fragment).setFilter(filter);
                    }
                }
            }else{
                adapter.getFilter().filter(filter);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean canCallApi() {
        long now = System.currentTimeMillis();
        if (now - lastApiCallTime < API_COOLDOWN) return false;
        lastApiCallTime = now;
        return true;
    }

    private void getChaptersBySubjectIdCompetitive(String subjectId) {
        try {
            if (!canCallApi()) return;
            shimmer.startShimmer();
            shimmer.setVisibility(View.VISIBLE);
            detachAllChapterListeners();

            Map<String, Object> map = new HashMap<>();
            map.put("id", subjectId);
            if (StringUtils.isNotEmpty(boardCode)) {
                map.put("boardCode", boardCode);
            }
            Log.d(TAG, "Chapter Request (competitive): " + map.toString());

            MyJsonArrayRequest myJsonArrayRequest = new MyJsonArrayRequest(Request.Method.GET,
                    CommonMethods.getUrlParam(GET_CHAPTER_OF_SUBJECT_V2.replace("{id}", subjectId), map),
                    null,
                    response -> {
                        try {
                            Gson gson = new Gson();
                            TypeToken<List<ChapterListInfoDto>> token = new TypeToken<List<ChapterListInfoDto>>() {
                            };
                            List<ChapterListInfoDto> chapterListInfoDtos =
                                    gson.fromJson(response.toString(), token.getType());
                            updateDataInRecyclerView(chapterListInfoDtos);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }, new VolleyGlobalErrorListener(requireContext()) {
                @Override
                public void postError(VolleyError error) {
                    Log.e(TAG, "--> ChapterList Error (competitive): " + error.getMessage());
                    shimmer.stopShimmer();
                    shimmer.setVisibility(View.GONE);
                    CommonMethods.showNegativeSnackbar(fl_parent_layout, error.getMessage());
                }
            });

            myJsonArrayRequest.setTag(TAG);
            MyVolleySingleton.getInstance(requireContext()).addToRequestQueue(myJsonArrayRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void getChaptersBySubjectId(Integer batchId, String subjectId) {
        try {
            if (!canCallApi()) return;
            shimmer.startShimmer();
            shimmer.setVisibility(View.VISIBLE);
            detachAllChapterListeners();

            Map<String, Object> requestParams = new HashMap<>();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("subjectId", subjectId);

            if (isBatchAddOn) {
                jsonObject.put("batchAddOnId", batchId);
            }
            if (StringUtils.isNotEmpty(type) &&
                    type.equals(ChapterListSubscribedActivity.SectionType.PREMIUM.name())) {
                jsonObject.put("chapterCategoryType", type);
            }

            Log.d(TAG, "Chapter Request: " + jsonObject.toString());

            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                    CommonMethods.getUrlParam(
                            isBatchAddOn ? AV_GET_CHAPTER_OF_SUBJECT_BATCH_ADD_ON : AV_GET_CHAPTER_OF_SUBJECT,
                            requestParams),
                    jsonObject,
                    response -> {
                        try {
                            Gson gson = new Gson();
                            TypeToken<ApiWrappedResponse<List<ChapterListInfoDto>>> token =
                                    new TypeToken<ApiWrappedResponse<List<ChapterListInfoDto>>>() {};
                            ApiWrappedResponse<List<ChapterListInfoDto>> apiResponse =
                                    gson.fromJson(response.toString(), token.getType());
                            updateDataInRecyclerView(apiResponse.getData());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }, new VolleyGlobalErrorListener(requireContext()) {
                @Override
                public void postError(VolleyError error) {
                    Log.e(TAG, "--> ChapterList Error: " + error.getMessage());
                    shimmer.stopShimmer();
                    shimmer.setVisibility(View.GONE);
                }
            });

            myJsonObjectRequest.setTag(TAG);
            MyVolleySingleton.getInstance(requireContext()).addToRequestQueue(myJsonObjectRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void updateDataInRecyclerView(List<ChapterListInfoDto> chapterListInfoDtos) {
        shimmer.stopShimmer();
        shimmer.setVisibility(View.GONE);

        if (chapterListInfoDtos == null || chapterListInfoDtos.isEmpty()) {
            rv_topics.setVisibility(View.GONE);
            llGroupedContainer.setVisibility(View.GONE);
            ll_no_data.setVisibility(View.VISIBLE);
//            onThemeChanged();
            return;
        }

        hasGrouping = false;
        for (ChapterListInfoDto dto : chapterListInfoDtos) {
            if (dto.getSubjectGroupId() != null && dto.getSubjectGroupName() != null) {
                hasGrouping = true;
                break;
            }
        }

        if (hasGrouping) {
            handleGroupedData(chapterListInfoDtos);
        } else {
            handleFlatList(chapterListInfoDtos);
        }
    }

    private void handleGroupedData(List<ChapterListInfoDto> list) {
        rv_topics.setVisibility(View.GONE);
        llGroupedContainer.setVisibility(View.VISIBLE);
        ll_no_data.setVisibility(View.GONE);

        Map<String, List<ChapterListInfoDto>> groupedMap = new LinkedHashMap<>();
        for (ChapterListInfoDto dto : list) {
            String key = dto.getSubjectGroupName();
            if (!groupedMap.containsKey(key)) {
                groupedMap.put(key, new ArrayList<>());
            }
            groupedMap.get(key).add(dto);
        }

        if (groupedMap.size() == 1) {
            handleFlatList(list);
            return;
        }

        setupTabs(groupedMap);
    }

    private void handleFlatList(List<ChapterListInfoDto> list) {
        llGroupedContainer.setVisibility(View.GONE);
        rv_topics.setVisibility(View.VISIBLE);
        ll_no_data.setVisibility(View.GONE);
        setFlatAdapter(list);
    }

    private void setFlatAdapter(List<ChapterListInfoDto> list) {
        List<ChapterNameModel> models = new ArrayList<>();
        chapterIdList.clear();

        for (ChapterListInfoDto chapterInfo : list) {
            ChapterNameModel cm = ChapterNameModel.builder()
                    .id(chapterInfo.getChapterId())
                    .newLecture(chapterInfo.isNewLecture())
                    .rescheduledMessage(chapterInfo.getRescheduledMessage())
                    .imageUrl(chapterInfo.getImageUrl())
                    .chapter(chapterInfo.getVideoTitle())
                    .name(chapterInfo.getTeacherName() == null ? "Arivihan" : chapterInfo.getTeacherName())
                    .locked(chapterInfo.isLocked())
                    .lockMessage(chapterInfo.getLockMessage())
                    .teacherIntroImage(chapterInfo.getTeacherIntroImage())
                    .completed(chapterInfo.isCompleted())
                    .notesUrl(chapterInfo.getNotesUrl())
                    .board(chapterInfo.isBoard())
                    .chapterType(chapterInfo.getChapterType())
                    .chapterStatus(chapterInfo.getChapterStatus())
                    .chapterCode(chapterInfo.getChapterCode())
                    .unwatched(chapterInfo.isUnwatched())
                    .type(chapterInfo.getType())
                    .chapterLiveDto(chapterInfo.getChapterLiveDto())
                    .build();

            models.add(cm);
            chapterIdList.add(chapterInfo.getChapterId());
        }

        chapterNameModels.clear();
        chapterNameModels.addAll(models);

        // Resolve the scroll target once from scrollToChapterId; persist it across re-loads
        if (pendingScrollIndex < 0 && org.apache.commons.lang3.StringUtils.isNotEmpty(scrollToChapterId)) {
            for (int i = 0; i < chapterNameModels.size(); i++) {
                if (scrollToChapterId.equals(chapterNameModels.get(i).getId())) {
                    pendingScrollIndex = i;
                    scrollToChapterId = null;
                    break;
                }
            }
            Log.d("ChaptersBySubjectFrag", "ScrollDebug: resolved pendingScrollIndex=" + pendingScrollIndex + ", totalItems=" + chapterNameModels.size());
        }

        adapter = new ChapterNameAdapter(requireContext(), chapterNameModels, this,
                subjectName, ll_no_data, rv_topics, currActivityTheme);
        if (rv_topics.getLayoutManager() == null) {
            rv_topics.setLayoutManager(new LinearLayoutManager(requireContext()));
        }
        rv_topics.setAdapter(adapter);

        if (pendingScrollIndex >= 0) {
            Log.d("ScrollDebug", "ENTERED scroll block, target=" + pendingScrollIndex);
            final int target = pendingScrollIndex;
            pendingScrollIndex = -1;

            if (pendingScrollLayoutListener != null) {
                rv_topics.removeOnLayoutChangeListener(pendingScrollLayoutListener);
                pendingScrollLayoutListener = null;
            }

            adapter.setHighlightPosition(target);

            rv_topics.post(() -> performScrollToTarget(target));

            pendingScrollLayoutListener = new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                           int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (bottom - top > 0 && bottom - top != oldBottom - oldTop) {
                        rv_topics.removeOnLayoutChangeListener(this);
                        pendingScrollLayoutListener = null;
                        performScrollToTarget(target);
                    }
                }
            };
            rv_topics.addOnLayoutChangeListener(pendingScrollLayoutListener);
        }

        attachObserversForChapters(subjectId, chapterIdList);
    }

    private void setupTabs(Map<String, List<ChapterListInfoDto>> groupedMap) {
        List<String> titles = new ArrayList<>(groupedMap.keySet());
        List<List<ChapterListInfoDto>> data = new ArrayList<>(groupedMap.values());

        viewPagerAdapter = new ChapterTabAdapter(this, titles, data);
        viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setCustomView(
                        getCustomInnerTab(titles.get(position), position == 0))
        ).attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab)   { updateInnerTabUI(tab, true);  }
            @Override public void onTabUnselected(TabLayout.Tab tab) { updateInnerTabUI(tab, false); }
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        tabLayout.setTabRippleColor(null);
        tabLayout.setSelectedTabIndicator(null);
        viewPager.setOffscreenPageLimit(titles.size());
    }

    private View getCustomInnerTab(String title, boolean isSelected) {
        View tabView = LayoutInflater.from(requireContext())
                .inflate(R.layout.text_tab_layout, null);

        MaterialCardView tabCard = tabView.findViewById(R.id.tab_text_card);
        TextView tabText         = tabView.findViewById(R.id.tab_text);

        tabText.setText(CommonMethods.getShortNameForSubjects(title));

        Typeface normal = ResourcesCompat.getFont(requireContext(), R.font.nunito_regular);
        Typeface bold   = ResourcesCompat.getFont(requireContext(), R.font.nunito_semibold);

        if (isSelected) {
            tabCard.setCardBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.subscription_blue));
            tabCard.setStrokeWidth(0);
            tabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            tabText.setTypeface(bold);
        } else {
            applyUnselectedTabStyle(tabCard, tabText, normal);
        }
        return tabView;
    }

    private void updateInnerTabUI(TabLayout.Tab tab, boolean isSelected) {
        if (!isAdded() || getContext() == null) return;
        View customView = tab.getCustomView();
        if (customView == null) return;

        MaterialCardView tabCard = customView.findViewById(R.id.tab_text_card);
        TextView tabText         = customView.findViewById(R.id.tab_text);

        Typeface normal = ResourcesCompat.getFont(requireContext(), R.font.nunito_regular);
        Typeface bold   = ResourcesCompat.getFont(requireContext(), R.font.nunito_semibold);

        if (isSelected) {
            tabCard.setCardBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.subscription_blue));
            tabCard.setStrokeWidth(0);
            tabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            tabText.setTypeface(bold);
        } else {
            applyUnselectedTabStyle(tabCard, tabText, normal);
        }
    }

    private void applyUnselectedTabStyle(MaterialCardView tabCard, TextView tabText, Typeface normal) {
        boolean isNight = currActivityTheme == ThemeEnum.DARK;

        int unselectedBg   = isNight ? 0xFF2C2C2C : ContextCompat.getColor(requireContext(), R.color.white);
        int unselectedText = isNight ? android.graphics.Color.WHITE : ContextCompat.getColor(requireContext(), R.color.tab_not_selected_text);

        tabCard.setCardBackgroundColor(unselectedBg);
        tabCard.setStrokeWidth(2);
        tabCard.setStrokeColor(
                ContextCompat.getColor(requireContext(), R.color.tab_not_selected_text_boundary));
        tabText.setTextColor(unselectedText);
        tabText.setTypeface(normal);
    }

    @Override
    public void onChapClick(int position) {
        try {
            if (adapter != null) adapter.notifyDataSetChanged();

            ChapterNameModel chapterNameModel = chapterNameModels.get(position);
            HashMap<String, String> lectureParams = new HashMap<>();
            lectureParams.put("chapter_id", chapterNameModel.getId());
            lectureParams.put("chapter_name", chapterNameModel.getChapter());
            lectureParams.put("chapter_type", chapterNameModel.getChapterType());
            lectureParams.put("chapter_status", chapterNameModel.getChapterStatus());
            lectureParams.put("is_locked", String.valueOf(chapterNameModel.isLocked()));
            lectureParams.put("subject_code", subjectCode);

            MyApplication.logFirebaseBundleCreation(FirebaseEvents.chap_lecture_selected.getActionName(), lectureParams);

            String lockDialogText = null;

            if (position >= 0 && chapterNameModel != null) {
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

                String lockDialogBtnText = null;
                if (isCompleted && MyApplication.getFirebaseRemoteConfig().getBoolean("to_show_lock_msg_from_data_side_in_chapter")) {
                    lockDialogBtnText = getResources().getString(R.string.go_back_to_lecture);
                }

                MyApplication.saveData(PreferenceKeys.MICROLECTURE_DATA_RELOAD.getKey(), false);

                String id          = chapterNameModel.getId();
                String chapterName = chapterNameModel.getChapter();
                String introImage  = chapterNameModel.getTeacherIntroImage();
                String chapterCode = chapterNameModel.getChapterCode();

                if (!isRecordedLecture) isCompleted = true;

                if (ChapterLiveByPass.isChapterStatusAllowedToByPass(
                        chapterNameModel.getChapterType(), chapterNameModel.getChapterStatus())
                        && ChapterLiveByPass.isByPassedUser()) {
                    CommonNavigator.navigateToLiveClassWebView(requireContext(), chapterName,
                            subjectName, subjectCode, id, chapterCode,
                            chapterNameModel.getImageUrl(), chapterNameModel.getName(),
                            TAG, getDataBundle());
                    return;
                }

                if (chapterNameModel.isLocked()) {
                    HashMap<String, String> lockParams = new HashMap<>();
                    lockParams.put("chapter_id", id);
                    lockParams.put("chapter_name", chapterName);
                    lockParams.put("chapter_type", chapterNameModel.getChapterType());
                    lockParams.put("chapter_status", chapterNameModel.getChapterStatus());
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.chap_locked_lecture_clicked.getActionName(), lockParams);
                    showLockPopup(isCompleted, lockDialogText, lockDialogBtnText);

                } else if (StringUtils.isNotEmpty(chapterNameModel.getChapterType())
                        && chapterNameModel.getChapterType().equals(ChapterType.LIVE_CHAPTER.name())
                        && StringUtils.isNotEmpty(chapterNameModel.getChapterStatus())
                        && (chapterNameModel.getChapterStatus().equals(ChapterStatus.LIVE_SCHEDULED.name())
                        || chapterNameModel.getChapterStatus().equals(ChapterStatus.CONVERTING.name())
                        || chapterNameModel.getChapterStatus().equals(ChapterStatus.RESCHEDULED.name())
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
                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.chap_upcoming_lecture_clicked.getActionName(), upcomingParams);
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
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.chap_live_lecture_join_attempt.getActionName(), liveParams);

                    if (chapterNameModel.getChapterLiveDto() != null
                            && StringUtils.isNotEmpty(chapterNameModel.getChapterLiveDto().getLiveUrl())) {
                        CommonMethods.openUrlExternally(requireContext(),
                                chapterNameModel.getChapterLiveDto().getLiveUrl());
                    } else {
                        CommonNavigator.navigateToLiveClassWebView(requireContext(), chapterName,
                                subjectName, subjectCode, id, chapterCode,
                                chapterNameModel.getImageUrl(), chapterNameModel.getName(),
                                TAG, getDataBundle());
                        HashMap<String, String> params = new HashMap<>();
                        params.put("chapterName", chapterName);
                        params.put("subjectCode", subjectCode);
                        params.put("chapterId", id);
                        params.put("chapterCode", chapterCode);
                        params.put("navigatingFrom", TAG);
                        MyApplication.logFirebaseBundleCreation(
                                FirebaseEvents.live_chapter_clicked.getActionName(), params);
                    }

                } else {
                    MyApplication.saveData(subjectName, position);
                    Intent intent = new Intent(requireContext(), MicrolectureListActivity.class);
                    if (StringUtils.isNotEmpty(introImage))
                        intent.putExtra("IntroImage", introImage);
                    if (StringUtils.isNotEmpty(chapterNameModel.getNotesUrl()))
                        intent.putExtra("NotesPdfUrl", chapterNameModel.getNotesUrl());
                    intent.putExtra("chapterName", chapterName);
                    intent.putExtra("subjectCode", subjectCode);
                    intent.putExtra("chapterId", id);
                    MyApplication.saveData(PreferenceKeys.MICROLECTURE_ID.getKey(), "-1");
                    intent.putExtra("selectedSubject", subjectName);

                    HashMap<String, String> params = new HashMap<>();
                    params.put("Chapter_Name", chapterName);
                    params.put("Subject_Code", subjectCode);
                    params.put("Chapter_Id", id);
                    params.put("chapterCode", chapterCode);
                    params.put("navigating_from", TAG);
                    MyApplication.logFirebaseBundleCreation(
                            FirebaseEvents.Chapter_clicked.getActionName(), params);

//                    if(chapterNameModel.getChapterStatus().equals(ChapterStatus.LIVE)){
//                        Map<String, Object> webViewParams = CommonMethods.getIntentParams(intent);
//                        String url = CommonMethods.getPersonalizedPathUrl() +
//                                AdaptivePath.TEST_SERIES_INSTRUCTIONS.path();
//                        CommonNavigator.navigateToWebViewByUrlParams(requireActivity(),
//                                url,
//                                webViewParams,
//                                true, true,
//                                false, false,
//                                false, false,
//                                getString(R.string.not_allowed_to_back), true, true);
//                    }else {
                    Log.d(TAG, "onChapClick: ");
                    HashMap<String, String> recordedParams = new HashMap<>();
                    recordedParams.put("chapter_id", id);
                    recordedParams.put("chapter_name", chapterName);
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.chap_recorded_lecture_started.getActionName(), recordedParams);
                    startActivity(intent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showPopupDialog(String url) {
        try {
            Dialog dialogHome = new Dialog(requireContext());
            dialogHome.setContentView(R.layout.custom_home_dialogue);

            Window window = dialogHome.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(
                        requireContext().getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
                window.setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }

            dialogHome.setCancelable(false);

            CardView cancel   = dialogHome.findViewById(R.id.cancel);
            ImageView image   = dialogHome.findViewById(R.id.image);

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
            cancel.setOnClickListener(v -> {
                Map<String, String> params = new HashMap<>();
                params.put("subject_code",subjectCode);
                params.put("subject_id",subjectId);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.chap_list_info_popup_cancel_click.getActionName(),params);
                dialogHome.dismiss();
            });
            dialogHome.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showLockPopup(boolean isCompleted, String lockText, String lockBtnText) {
//        SubscriptionNavigationHelper.navigateToSubscriptionPage(requireContext());
        Log.d(TAG, "showLockPopup-called-(): ");
        HashMap<String, String> subscriptionParams = new HashMap<>();
        subscriptionParams.put("source_screen", TAG);
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.chap_locked_subs_screen_opened.getActionName(), subscriptionParams);
        Intent intent = SubscriptionNavigationHelper.getSubscriptionPageIntent(requireActivity(), SubscriptionPopupType.LECTURES.name(), RedirectionSource.CHAPTER_LIST_LOCKED_POPUP);
        startActivity(intent);
    }

    private void showLivePop(ChapterPopUpDto chapterPopUpDto, String chapterStatus) {
        if (chapterPopUpDto == null || (liveDialog != null && liveDialog.isShowing())) return;

        try {
            liveDialog = new Dialog(requireActivity());
            liveDialog.setContentView(R.layout.dialog_alert_title_subtitle_button);
            liveDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
            liveDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            liveDialog.setCancelable(true);
            liveDialog.setCanceledOnTouchOutside(true);

            TextView title        = liveDialog.findViewById(R.id.title);
            TextView subtitle     = liveDialog.findViewById(R.id.subtitle);
            TextView btnTextView  = liveDialog.findViewById(R.id.btnText);
            CardView cv_confirm   = liveDialog.findViewById(R.id.cv_okay);
            ImageView iv_close    = liveDialog.findViewById(R.id.iv_close);
            ImageView dialogImage= liveDialog.findViewById(R.id.selection_image);
            LottieAnimationView animation = liveDialog.findViewById(R.id.animation);
            Log.d(TAG, "Views bound successfully--fragment-"+chapterStatus);

            if(chapterStatus.equals(ChapterStatus.RESCHEDULED.name())){
                dialogImage.setVisibility(View.VISIBLE);
                Glide.with(requireContext())
                        .load(R.drawable.live_rescheduled)
                        .into(dialogImage);
                animation.setVisibility(View.GONE);
            }else {
                animation.setAnimation(R.raw.live_lottie_anim);
                animation.setVisibility(View.VISIBLE);
                dialogImage.setVisibility(View.GONE);
                Log.d(TAG, "Lottie animation set: live_lottie_anim");
            }
            title.setVisibility(View.GONE);
            subtitle.setVisibility(View.GONE);
            btnTextView.setVisibility(View.GONE);

            String titleText  = chapterPopUpDto.getTitle();
            String subTitle   = chapterPopUpDto.getSubTitle();
            String buttonText = chapterPopUpDto.getBtnName();

            if (StringUtils.isNotEmpty(titleText)) {
                title.setText(CommonMethods.isValidJson(titleText)
                        ? StyledTextBuilder.fromJson(requireActivity(), titleText) : titleText);
                title.setVisibility(View.VISIBLE);
            }
            if (StringUtils.isNotEmpty(subTitle)) {
                subtitle.setText(CommonMethods.isValidJson(subTitle)
                        ? StyledTextBuilder.fromJson(requireActivity(), subTitle) : subTitle);
                subtitle.setVisibility(View.VISIBLE);
            }
            if (StringUtils.isNotEmpty(buttonText)) {
                btnTextView.setText(CommonMethods.isValidJson(buttonText)
                        ? StyledTextBuilder.fromJson(requireActivity(), buttonText) : buttonText);
                btnTextView.setVisibility(View.VISIBLE);
            }

            cv_confirm.setOnClickListener(v -> dismissLivePopUp());
            iv_close.setOnClickListener(v -> dismissLivePopUp());
            cv_confirm.setOnClickListener(view -> {
                dismissLivePopUp();
            });

            iv_close.setOnClickListener(v -> {
                //close
                HashMap<String, String> popupCloseParams = new HashMap<>();
                popupCloseParams.put("popup_type", "upcoming_lecture");
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.chap_upcoming_popup_closed.getActionName(), popupCloseParams);
                dismissLivePopUp();
            });

            //view
            HashMap<String, String> popupParams = new HashMap<>();
            popupParams.put("popup_type", "upcoming_lecture");
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.chap_upcoming_popup_viewed.getActionName(), popupParams);

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

    private void attachObserversForChapters(String subjectId, List<String> chapterIds) {
        if (StringUtils.isNotEmpty(subjectId)) {
            for (String chapterId : chapterIds) {
                attachChapterListener(subjectId, chapterId);
            }
        }
    }

    private void attachChapterListener(String subjectId, String chapterId) {
        String key = subjectId + "_" + chapterId;

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("chapter_status_update")
                .child(subjectId)
                .child(chapterId);

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    if (!snapshot.exists()) return;
                    String status = snapshot.getValue(String.class);

                    if (StringUtils.isNotEmpty(status) && chapterIdList.contains(chapterId)) {
                        int index = chapterIdList.indexOf(chapterId);
                        ChapterNameModel chapterNameModel = chapterNameModels.get(index);
                        if (StringUtils.isNotEmpty(chapterNameModel.getChapterStatus())
                                && !chapterNameModel.getChapterStatus().equals(status)) {
                            getChaptersBySubjectId(batchId, subjectId);
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
        Log.d("CHAPTER_OBSERVER", "Attached listener for " + key);
    }

    private void detachAllChapterListeners() {
        try {
            for (String key : chapterListeners.keySet()) {
                DatabaseReference ref     = chapterRefs.get(key);
                ValueEventListener listener = chapterListeners.get(key);
                if (ref != null && listener != null) {
                    ref.removeEventListener(listener);
                }
            }
            chapterListeners.clear();
            chapterRefs.clear();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Bundle getDataBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("id", subjectId);
        bundle.putString(PreferenceKeys.NAME.getKey(), subjectName);
        bundle.putString("code", subjectCode);
        bundle.putString(PreferenceKeys.BOARD_CODE.getKey(), boardCode);
        return bundle;
    }

    @Override
    public void onInfoClicked(String imageUrl) {
        showPopupDialog(imageUrl);
    }

    public class ChapterTabAdapter extends FragmentStateAdapter {

        private final List<String> titles;
        private final List<List<ChapterListInfoDto>> data;
        private final Map<Integer, Fragment> fragmentMap = new HashMap<>();

        public ChapterTabAdapter(@NonNull Fragment fragment,
                                 List<String> titles,
                                 List<List<ChapterListInfoDto>> data) {
            super(fragment);
            this.titles = titles;
            this.data   = data;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment fragment = ChapterListFragment.newInstance(data.get(position), currActivityTheme);
            fragmentMap.put(position, fragment);
            return fragment;
        }

        public Collection<Fragment> getAllFragments() {
            return fragmentMap.values();
        }

        @Override
        public int getItemCount() {
            return titles.size();
        }
    }

    public static class ChapterListFragment extends Fragment {

        private String queryText;
        private ChapterNameAdapter adapter;
        private List<ChapterNameModel> models = new ArrayList<>();
        private ThemeEnum currActivityTheme;

        public ChapterListFragment() {
            // Required empty public constructor for Fragment restoration
        }

        public ChapterListFragment(ThemeEnum currActivityTheme){
            this.currActivityTheme = currActivityTheme;
        }

        public static ChapterListFragment newInstance(List<ChapterListInfoDto> list, ThemeEnum currActivityTheme) {
            ChapterListFragment fragment = new ChapterListFragment(currActivityTheme);
            Bundle bundle = new Bundle();
            bundle.putString("data", new Gson().toJson(list));
            bundle.putString("theme", currActivityTheme != null ? currActivityTheme.name() : ThemeEnum.LIGHT.name());
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view        = inflater.inflate(R.layout.your_recycler_layout, container, false);
            LinearLayout ll_no_data = view.findViewById(R.id.no_result_found_layout);
            ImageView iv_no_data = view.findViewById(R.id.iv_no_data);
            RecyclerView rv  = view.findViewById(R.id.recycler);

            //no data
            if (ll_no_data != null) {
                TextView tvNoDataTitle = ll_no_data.findViewById(R.id.tv_no_data_title);
                if (tvNoDataTitle != null) {
                    tvNoDataTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
                }
            }
            updateNoDataImage(ll_no_data, iv_no_data);

            if (getArguments() != null && currActivityTheme == null) {
                String themeName = getArguments().getString("theme", ThemeEnum.LIGHT.name());
                try { currActivityTheme = ThemeEnum.valueOf(themeName); } catch (Exception ignored) { currActivityTheme = ThemeEnum.LIGHT; }
            }
            if (currActivityTheme == null) currActivityTheme = ThemeEnum.LIGHT;

            String json = getArguments().getString("data");
            List<ChapterListInfoDto> list = new Gson().fromJson(
                    json, new TypeToken<List<ChapterListInfoDto>>() {}.getType());

            models.clear();
            for (ChapterListInfoDto chapterInfo : list) {
                ChapterNameModel cm = ChapterNameModel.builder()
                        .id(chapterInfo.getChapterId())
                        .newLecture(chapterInfo.isNewLecture())
                        .rescheduledMessage(chapterInfo.getRescheduledMessage())
                        .imageUrl(chapterInfo.getImageUrl())
                        .chapter(chapterInfo.getVideoTitle())
                        .name(chapterInfo.getTeacherName() == null ? "Arivihan" : chapterInfo.getTeacherName())
                        .locked(chapterInfo.isLocked())
                        .lockMessage(chapterInfo.getLockMessage())
                        .teacherIntroImage(chapterInfo.getTeacherIntroImage())
                        .completed(chapterInfo.isCompleted())
                        .notesUrl(chapterInfo.getNotesUrl())
                        .board(chapterInfo.isBoard())
                        .chapterType(chapterInfo.getChapterType())
                        .chapterStatus(chapterInfo.getChapterStatus())
                        .chapterCode(chapterInfo.getChapterCode())
                        .unwatched(chapterInfo.isUnwatched())
                        .type(chapterInfo.getType())
                        .chapterLiveDto(chapterInfo.getChapterLiveDto())
                        .build();
                models.add(cm);
            }

            ChapterNameAdapter.OnChapClickListener clickListener =
                    new ChapterNameAdapter.OnChapClickListener() {
                        @Override
                        public void onChapClick(int position) {
                            Fragment parent = getParentFragment();
                            if (parent instanceof ChaptersBySubjectFragment) {
                                ChaptersBySubjectFragment p = (ChaptersBySubjectFragment) parent;
                                p.chapterNameModels.clear();
                                p.chapterNameModels.addAll(models);
                                p.chapterIdList.clear();
                                for (ChapterNameModel m : models) p.chapterIdList.add(m.getId());
                                p.onChapClick(position);
                            }
                        }

                        @Override
                        public void onInfoClicked(String imageUrl) {
                            Fragment parent = getParentFragment();
                            if (parent instanceof ChaptersBySubjectFragment) {
                                ((ChaptersBySubjectFragment) parent).onInfoClicked(imageUrl);
                            }
                        }
                    };

            adapter = new ChapterNameAdapter(
                    requireContext(),
                    models,
                    clickListener,
                    "",
                    ll_no_data,
                    rv,
                    currActivityTheme
            );

            rv.setLayoutManager(new LinearLayoutManager(requireContext()));
            rv.setAdapter(adapter);
            return view;
        }


        private void updateNoDataImage(LinearLayout ll_no_data, ImageView iv_no_data) {
            if (iv_no_data == null) return;

            String lang;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                lang = getResources().getConfiguration().getLocales().get(0).getLanguage();
            } else {
                lang = getResources().getConfiguration().locale.getLanguage();
            }

            boolean isHindi = lang.equals("hi");
            boolean isDark  = (currActivityTheme == ThemeEnum.DARK);

            // --- Image ---
            int drawableRes;
            if (isDark && isHindi) {
                drawableRes = R.drawable.premium_lecture_coming_soon_hi;
            } else if (isDark) {
                drawableRes = R.drawable.premium_lecture_coming_soon_en;
            } else if (isHindi) {
                drawableRes = R.drawable.coming_soon_live_hi;
            } else {
                drawableRes = R.drawable.coming_soon_live_en;
            }
            iv_no_data.setImageResource(drawableRes);

            // --- Text ---
            if (ll_no_data == null) return;
            TextView tvNoDataTitle = ll_no_data.findViewById(R.id.tv_no_data_title);
            if (tvNoDataTitle == null) return;

            int noDataStringRes;
            if (isDark && isHindi) {
                noDataStringRes = R.string.no_data_premium_hi;
            } else if (isDark) {
                noDataStringRes = R.string.no_data_premium_en;
            } else if (isHindi) {
                noDataStringRes = R.string.no_data_live_hi;
            } else {
                noDataStringRes = R.string.no_data_live_en;
            }
            tvNoDataTitle.setText(getString(noDataStringRes));
        }


        public void setFilter(String filter) {
            try {
                queryText = filter;
                adapter.getFilter().filter(filter);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void performScrollToTarget(int target) {
        if (!(rv_topics.getLayoutManager() instanceof LinearLayoutManager)) return;
        LinearLayoutManager lm = (LinearLayoutManager) rv_topics.getLayoutManager();

        // Step 1: land the item's top at the RecyclerView's top.
        lm.scrollToPositionWithOffset(target, 0);

        // Step 2: after layout settles, check if the item's bottom is
        // actually visible within rv_topics' bounds. If not, nudge the
        // scroll up by exactly the amount still hidden.
        rv_topics.post(() -> {
            RecyclerView.ViewHolder vh = rv_topics.findViewHolderForAdapterPosition(target);
            if (vh == null || vh.itemView == null) return;

            int itemBottom = vh.itemView.getBottom();
            int rvHeight = rv_topics.getHeight();

            if (itemBottom > rvHeight) {
                int hiddenPx = itemBottom - rvHeight;
                rv_topics.scrollBy(0, hiddenPx);
                Log.d("ScrollDebug", "Corrected by " + hiddenPx + "px, itemBottom=" + itemBottom + ", rvHeight=" + rvHeight);
            }
        });
    }
}