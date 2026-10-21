package arivihan.technologies.doubtbuzzter2.fragments.competitive.subject.chapter;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_ONE_SHOT_LECTURES;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_TODAY_CLASS_BY_BATCH_ADD_ON;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
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
import arivihan.technologies.doubtbuzzter2.activity.CampaignSubscriptionActivity;
import arivihan.technologies.doubtbuzzter2.activity.MicrolectureListActivity;
import arivihan.technologies.doubtbuzzter2.adapter.TodayClassAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.TodayTaskListType;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.fragments.TodayTaskFragment;
import arivihan.technologies.doubtbuzzter2.helper.CompetitiveBannerView;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
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
import arivihan.technologies.doubtbuzzter2.model.crash_course.TodayClassItemResponse;
import arivihan.technologies.doubtbuzzter2.model.crash_course.TodayClassResponse;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.model.remote_config.ActivityNavigationConfig;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;

/**
 * Replicates the full {@code ChapterListBySubjectsActivity} UX inside a fragment.
 * Hosts the live-classes section, banner, subject tabs and inner
 * {@link CompetitiveChapterBySubjectFragment} instances, passing {@code chapterType}
 * through to every API call so results are filtered to the correct content type.
 */
public class ChapterListByTypeFragment extends Fragment
        implements RefreshActivityListener, TodayClassAdapter.TodayClassAdapterListener {

    private static final String TAG = ChapterListByTypeFragment.class.getSimpleName();

    private static final String ARG_TITLE = "title";
    private static final String ARG_TYPE = "chapterType";
    private static final String ARG_SUBJECT_ID = "subjectId";
    private static final String ARG_TODAYS_CLASS_VISIBILITY = "todaysClassVisibility";
    private static final String ARG_FEATURE_TYPE = "feature_type";
    // UI
    private ShimmerFrameLayout shimmer, shimmer_live_classes;
    private RelativeLayout rl_live_classes, rl_live_content;
    private TextView tv_live_classes, tv_see_all;
    private LinearLayout ll_see_all;
    private RecyclerView today_class_recycler_view;
    private CompetitiveBannerView bannerView;
    private TabLayout subject_tabs;
    private CardView cv_subject_tab_left_scroll, cv_subject_tab_right_scroll;
    private ViewPager2 subject_viewpager;
    private RelativeLayout rl_content;

    // Data
    private String title, chapterType, initialSubjectId;
    private boolean todaysClassVisibility;
    private SubjectViewPagerAdapter subjectPagerAdapter;
    private RefreshActivityListener refreshActivityListener;
    private String featureType;

    private final ActivityResultLauncher<Intent> subscriptionScreenLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK
                                && result.getData() != null) {
                            try {
                                boolean isSubscribed = result.getData()
                                        .getBooleanExtra("isSubscribed", false);
                                if (isSubscribed) {
                                    if (refreshActivityListener != null) {
                                        refreshActivityListener.refreshPage(true);
                                    } else {
                                        if (todaysClassVisibility) getTodayClassData();
                                        fetchSubjects();
                                    }
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });

    public ChapterListByTypeFragment() {}

    public static ChapterListByTypeFragment newInstance(String title, String chapterType) {
        return newInstance(title, chapterType, null, false,null);
    }

    public static ChapterListByTypeFragment newInstance(String title, String chapterType, String subjectId) {
        return newInstance(title, chapterType, subjectId, false,null );
    }

    public static ChapterListByTypeFragment newInstance(String title, String chapterType, String subjectId, boolean todaysClassVisibility,String featureType) {
        ChapterListByTypeFragment fragment = new ChapterListByTypeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_TYPE, chapterType);
        args.putString(ARG_SUBJECT_ID, subjectId);
        args.putBoolean(ARG_TODAYS_CLASS_VISIBILITY, todaysClassVisibility);
        args.putString(ARG_FEATURE_TYPE, featureType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(ARG_TITLE);
            chapterType = getArguments().getString(ARG_TYPE);
            initialSubjectId = getArguments().getString(ARG_SUBJECT_ID);
            todaysClassVisibility = getArguments().getBoolean(ARG_TODAYS_CLASS_VISIBILITY, false);
            featureType = getArguments().getString(ARG_FEATURE_TYPE);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            if (getActivity() instanceof RefreshActivityListener) {
                refreshActivityListener = (RefreshActivityListener) getActivity();
            }
        } catch (Exception exception) {
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chapter_list_by_type, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        shimmer = view.findViewById(R.id.shimmer);
        shimmer_live_classes = view.findViewById(R.id.shimmer_live_classes);
        rl_live_classes = view.findViewById(R.id.rl_live_classes);
        rl_live_content = view.findViewById(R.id.rl_live_content);
        tv_live_classes = view.findViewById(R.id.tv_live_classes);
        ll_see_all = view.findViewById(R.id.ll_see_all);
        tv_see_all = view.findViewById(R.id.tv_see_all);
        today_class_recycler_view = view.findViewById(R.id.today_class_recycler_view);
        today_class_recycler_view.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            private int initialX, initialY;

            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = (int) e.getX();
                        initialY = (int) e.getY();
                        rv.getParent().requestDisallowInterceptTouchEvent(true);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int dx = Math.abs((int) e.getX() - initialX);
                        int dy = Math.abs((int) e.getY() - initialY);
                        // Let parent handle vertical scrolls; child owns horizontal
                        rv.getParent().requestDisallowInterceptTouchEvent(dx > dy);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        rv.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {}

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
        });

        bannerView = view.findViewById(R.id.bannerView);
        rl_content = view.findViewById(R.id.rl_content);
        subject_tabs = view.findViewById(R.id.subject_tabs);
        cv_subject_tab_left_scroll = view.findViewById(R.id.cv_subject_tab_left_scroll);
        cv_subject_tab_right_scroll = view.findViewById(R.id.cv_subject_tab_right_scroll);
        subject_viewpager = view.findViewById(R.id.subject_viewpager);

        ll_see_all.setVisibility(View.GONE);
        rl_live_classes.setVisibility(View.GONE);
        rl_live_content.setVisibility(View.GONE);
        shimmer.setVisibility(View.GONE);
        shimmer_live_classes.setVisibility(View.GONE);

        cv_subject_tab_left_scroll.setOnClickListener(
                v -> subject_tabs.smoothScrollBy(-subject_tabs.getWidth(), 0));
        cv_subject_tab_right_scroll.setOnClickListener(
                v -> subject_tabs.smoothScrollBy(subject_tabs.getWidth(), 0));
        subject_tabs.getViewTreeObserver()
                .addOnScrollChangedListener(this::updateTabArrowsVisibility);

        if (todaysClassVisibility) {
            getTodayClassData();
        }
        fetchSubjects();
    }

    @Override
    public void onDestroy() {
        if (getContext() != null) {
            MyVolleySingleton.getInstance(requireContext()).getRequestQueue().cancelAll(TAG);
        }
        super.onDestroy();
    }

    // -------------------------------------------------------------------------
    // Today's class (live classes section)
    // -------------------------------------------------------------------------

    private void getTodayClassData() {
        shimmer_live_classes.startShimmer();
        shimmer_live_classes.setVisibility(View.VISIBLE);
        rl_live_content.setVisibility(View.GONE);

        HashMap<String, Object> params = new HashMap<>();
        if(MyApplication.isCompetitiveProfile()) {
            params.put("isNeet", true);
        }

        MyJsonObjectRequest request = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_TODAY_CLASS_BY_BATCH_ADD_ON, params), null,
                response -> {
                    if (!isAdded()) return;
                    TypeToken<ApiWrappedResponse<TodayClassResponse>> token = new TypeToken<ApiWrappedResponse<TodayClassResponse>>() {
                    };
                    ApiWrappedResponse<TodayClassResponse> apiResponse = new Gson().fromJson(response.toString(), token.getType());

                    TodayClassResponse todayClassResponse = apiResponse.getData();
                    renderTodayClassUI(todayClassResponse);
                },
                new VolleyGlobalErrorListener(requireContext()) {
                    @Override
                    public void postError(VolleyError error) {
                        if (!isAdded()) return;
                        Log.e(TAG, "getTodayClassData error", error);
                        shimmer_live_classes.stopShimmer();
                        shimmer_live_classes.setVisibility(View.GONE);
                        rl_live_content.setVisibility(View.VISIBLE);
                    }
                });
        request.setTag(TAG);
        MyVolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }

    private void renderTodayClassUI(TodayClassResponse todayClassResponse) {
        shimmer_live_classes.stopShimmer();
        shimmer_live_classes.setVisibility(View.GONE);
        rl_live_content.setVisibility(View.VISIBLE);

        if (todayClassResponse == null) return;

        List<TodayClassItemResponse> dtos = todayClassResponse.getClasses();
        if (StringUtils.isNotEmpty(todayClassResponse.getLayoutTitle())) {
            tv_live_classes.setText(todayClassResponse.getLayoutTitle());
        }
        if (dtos != null) TodayTaskFragment.checkForDemoLecture(dtos);

        if (dtos != null && !dtos.isEmpty()) {
            rl_live_classes.setVisibility(View.VISIBLE);
            TodayTaskListType fragmentType = todayClassResponse.getFragmentType() != null
                    ? todayClassResponse.getFragmentType() : TodayTaskListType.TODAY_TASK_FRAGMENT;
            TodayClassAdapter adapter = new TodayClassAdapter(requireActivity(), fragmentType, dtos, this);
            today_class_recycler_view.setAdapter(null);
            today_class_recycler_view.setAdapter(adapter);
        }

        if (todayClassResponse.getSeeAllConfig() != null
                && todayClassResponse.getSeeAllConfig().getClassName() != null) {
            ActivityNavigationConfig seeAllConfig = todayClassResponse.getSeeAllConfig();
            String seeAllBtnText = seeAllConfig.getFeatureName();
            tv_see_all.setText(CommonMethods.isValidJson(seeAllBtnText)
                    ? StyledTextBuilder.fromJson(requireContext(), seeAllBtnText) : seeAllBtnText);
            ll_see_all.setOnClickListener(v ->
                    CommonMethods.navigateFromActivityConfig(requireActivity(),
                            seeAllConfig.getNavigationType(),
                            seeAllConfig.getClassName(),
                            seeAllConfig.getParams()));
            ll_see_all.setVisibility(View.VISIBLE);
        }
    }

    // -------------------------------------------------------------------------
    // Subjects
    // -------------------------------------------------------------------------

    private void fetchSubjects() {
        shimmer.startShimmer();
        shimmer.setVisibility(View.VISIBLE);
        rl_content.setVisibility(View.GONE);

        try {
            JSONObject jsonObject = new JSONObject();
            if (StringUtils.isNotEmpty(chapterType)) {
                jsonObject.put("chapterCategoryType", chapterType);
            }
            HashMap<String, Object> params = new HashMap<>();
            if(MyApplication.isCompetitiveProfile()) {
                params.put("isNeet", true);
            }

            MyJsonObjectRequest request = new MyJsonObjectRequest(Request.Method.POST,
                    CommonMethods.getUrlParam(GET_ONE_SHOT_LECTURES, params), jsonObject,
                    response -> {
                        try {
                            if (!isAdded()) return;
                            Log.d(TAG, "fetchSubjects response: " + response);
                            ApiWrappedResponse<OneShotChapterListResponse> wrapped =
                                    new Gson().fromJson(response.toString(),
                                            new TypeToken<ApiWrappedResponse<OneShotChapterListResponse>>() {}.getType());
                            OneShotChapterListResponse data = wrapped.getData();

                            shimmer.stopShimmer();
                            shimmer.setVisibility(View.GONE);
                            rl_content.setVisibility(View.VISIBLE);

                            setUpBanner(data.getBanner());
                            setSubjects(data.getSubjects());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    },
                    new VolleyGlobalErrorListener(requireContext()) {
                        @Override
                        public void postError(VolleyError error) {
                            if (!isAdded()) return;
                            Log.e(TAG, "fetchSubjects error", error);
                            shimmer.stopShimmer();
                            shimmer.setVisibility(View.GONE);
                            rl_content.setVisibility(View.VISIBLE);
                        }
                    });
            request.setTag(TAG);
            MyVolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setUpBanner(BannerDto banner) {
        if (banner != null) {
            bannerView.setBannerData(banner);
            bannerView.setVisibility(View.VISIBLE);
        } else {
            bannerView.setVisibility(View.GONE);
        }
    }

    private void setSubjects(List<NameAndIdDto> subjectList) {
        if (subjectList == null || subjectList.isEmpty()) return;
        Log.d(TAG, "Subjects: " + subjectList.toString());

        subjectPagerAdapter = new SubjectViewPagerAdapter(subjectList);
        subject_viewpager.setAdapter(subjectPagerAdapter);
        subject_viewpager.setOffscreenPageLimit(subjectPagerAdapter.getItemCount());

        TabLayoutMediator mediator = new TabLayoutMediator(subject_tabs, subject_viewpager,
                (tab, position) ->
                        tab.setCustomView(getCustomTab(subjectList.get(position).getName(), position == 0)));
        mediator.attach();

        subject_tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateTabUI(tab, true);
                int position = tab.getPosition();
                if (position >= 0 && position < subjectList.size()) {
                    NameAndIdDto subject = subjectList.get(position);
                    Map<String, String> params = new HashMap<>();
                    params.put("subject_id", subject.getId());
                    params.put("subject_name", subject.getName());
                    params.put("tab_type", chapterType);
                    params.put("source_screen", TAG);
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.neet_lectures_subject_selected.getActionName(), params);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                updateTabUI(tab, false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        subject_tabs.setVisibility(View.VISIBLE);
        subject_viewpager.setVisibility(View.VISIBLE);

        // Pre-select subject tab matching initialSubjectId (if provided)
        if (StringUtils.isNotEmpty(initialSubjectId)) {
            int targetIndex = 0;
            for (int i = 0; i < subjectList.size(); i++) {
                if (initialSubjectId.equals(subjectList.get(i).getId())) {
                    targetIndex = i;
                    break;
                }
            }
            final int finalIndex = targetIndex;
            subject_viewpager.post(() -> subject_viewpager.setCurrentItem(finalIndex, true));
        }

        MyApplication.getSubscriptionStatus(getActivity(), new SubscriptionCallback() {
            @Override
            public void onSuccess(SubscriptionCheckDto dto) {
                if (!isAdded()) return;
                for (Fragment frag : subjectPagerAdapter.getAllFragments()) {
                    if (frag instanceof CompetitiveChapterBySubjectFragment) {
                        ((CompetitiveChapterBySubjectFragment) frag).setSubscriptionCheckDto(dto);
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "getSubscriptionStatus error: " + errorMessage);
            }
        });
    }

    // -------------------------------------------------------------------------
    // Search — called by the activity, propagates to all subject fragments
    // -------------------------------------------------------------------------

    public void setFilter(String filter) {
        try {
            if (subjectPagerAdapter == null) return;
            for (Fragment frag : subjectPagerAdapter.getAllFragments()) {
                if (frag instanceof CompetitiveChapterBySubjectFragment) {
                    ((CompetitiveChapterBySubjectFragment) frag).setFilter(filter);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // -------------------------------------------------------------------------
    // Tab UI helpers
    // -------------------------------------------------------------------------

    private View getCustomTab(String tabTitle, boolean isSelected) {
        View tabView = LayoutInflater.from(requireContext()).inflate(R.layout.text_tab_layout, null);
        MaterialCardView tabCard = tabView.findViewById(R.id.tab_text_card);
        TextView tabText = tabView.findViewById(R.id.tab_text);
        Typeface fontUnselected = ResourcesCompat.getFont(requireContext(), R.font.nunito_regular);
        Typeface fontSelected = ResourcesCompat.getFont(requireContext(), R.font.nunito_semibold);
        String shortName = CommonMethods.getShortName(tabTitle);
        tabText.setText(shortName);
        if (isSelected) {
            tabCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.subscription_blue));
            tabCard.setStrokeWidth(0);
            tabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            tabText.setTypeface(fontSelected);
        } else {
            tabCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white));
            tabCard.setStrokeWidth(2);
            tabCard.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.tab_not_selected_text_boundary));
            tabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.tab_not_selected_text));
            tabText.setTypeface(fontUnselected);
        }
        return tabView;
    }

    private void updateTabUI(TabLayout.Tab tab, boolean isSelected) {
        View customView = tab.getCustomView();
        if (customView == null) return;
        MaterialCardView tabCard = customView.findViewById(R.id.tab_text_card);
        TextView tabText = customView.findViewById(R.id.tab_text);
        Typeface fontUnselected = ResourcesCompat.getFont(requireContext(), R.font.nunito_regular);
        Typeface fontSelected = ResourcesCompat.getFont(requireContext(), R.font.nunito_semibold);
        if (isSelected) {
            tabCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.subscription_blue));
            tabCard.setStrokeWidth(0);
            tabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            tabText.setTypeface(fontSelected);
        } else {
            tabCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white));
            tabCard.setStrokeWidth(2);
            tabCard.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.tab_not_selected_text_boundary));
            tabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.tab_not_selected_text));
            tabText.setTypeface(fontUnselected);
        }
    }

    private void updateTabArrowsVisibility() {
        try {
            int scrollX = subject_tabs.getScrollX();
            int maxScroll = subject_tabs.getChildAt(0).getMeasuredWidth() - subject_tabs.getMeasuredWidth();
            cv_subject_tab_left_scroll.setVisibility(scrollX > 0 ? View.VISIBLE : View.GONE);
            cv_subject_tab_right_scroll.setVisibility(scrollX < maxScroll ? View.VISIBLE : View.GONE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // -------------------------------------------------------------------------
    // Subscription navigation
    // -------------------------------------------------------------------------

    private void navigateToSubscriptionLockScreen(SubscriptionLockPopupDto dto) {
        if (isFinishing()) return;
        Intent intent = SubscriptionNavigationHelper.getSubscriptionPageIntent(requireActivity(), RedirectionSource.CHAPTER_LIST_LOCKED_POPUP);
        if (dto != null) {
            intent.putExtra(CampaignSubscriptionActivity.BUNDLE_SUBSCRIPTION_DATA, dto);
        }
        subscriptionScreenLauncher.launch(intent);
    }

    private boolean isFinishing() {
        return getActivity() == null || getActivity().isFinishing() || getActivity().isDestroyed();
    }

    // -------------------------------------------------------------------------
    // RefreshActivityListener
    // -------------------------------------------------------------------------

    @Override
    public void refreshPage(boolean refresh) {
        // handled by parent activity if needed
    }

    @Override
    public void doRefreshNow(boolean refresh) {
        if (refresh) fetchSubjects();
    }

    // -------------------------------------------------------------------------
    // TodayClassAdapter.TodayClassAdapterListener
    // -------------------------------------------------------------------------

    @Override
    public void showSnackBar(String message) {
        try {
            View root = getView();
            if (root != null) {
                CommonMethods.showLongPositiveSnackbar(root.findViewById(R.id.rl_fragment_parent), message);
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void markVideoCompletedStatus() {
        // no-op: completion tracking handled in subject fragment
    }

    @Override
    public void videoClick(String identifier) {
        // no-op: handled in subject fragment
    }

    @Override
    public void showPopUp(TodayClassAdapter.TodayClassPopUpTypes type, TodayClassItemResponse item) {
        try {
            if (!isAdded()) return;
            if (type == TodayClassAdapter.TodayClassPopUpTypes.LIVE_UPCOMING) {
                if (item != null && item.getChapterLiveDto() != null
                        && item.getChapterLiveDto().getUpcomingStatusPopUp() != null) {
                    ChapterPopUpDto dto = item.getChapterLiveDto().getUpcomingStatusPopUp();
                    CommonMethods.showGenericPopupInformation(
                            requireActivity(), dto.getTitle(), dto.getSubTitle(), dto.getBtnName(),item.getChapterStatus());
                }
            } else if (type == TodayClassAdapter.TodayClassPopUpTypes.LOCK_POPUP) {
                if (item != null && item.isLocked()) {
                    CommonMethods.showLockPopupDialog(requireActivity(),
                            item.getSubscriptionLockPopupDto() != null
                                    ? item.getSubscriptionLockPopupDto()
                                    : new SubscriptionLockPopupDto());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onTodayClassClick(TodayClassItemResponse item, int position) {
        if (isFinishing()) return;
        if (item.isLocked()) {
            navigateToSubscriptionLockScreen(item.getSubscriptionLockPopupDto());
        } else {
            Intent intent = new Intent(requireActivity(), MicrolectureListActivity.class);
            intent.putExtra("chapterId", item.getChapterId());
            intent.putExtra("chapterName", item.getChapterName());
            intent.putExtra("hideTapTarget", true);
            intent.putExtra("microLectureId", item.getMicroLectureId());
            intent.putExtra("openVideo", true);
            startActivity(intent);
        }
    }

    // -------------------------------------------------------------------------
    // Subject ViewPager adapter
    // -------------------------------------------------------------------------

    class SubjectViewPagerAdapter extends FragmentStateAdapter {

        private final List<NameAndIdDto> subjectList;
        private final Map<Integer, Fragment> fragmentMap = new HashMap<>();

        SubjectViewPagerAdapter(List<NameAndIdDto> subjectList) {
            super(ChapterListByTypeFragment.this);
            this.subjectList = subjectList;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            NameAndIdDto subject = subjectList.get(position);
            Fragment fragment = CompetitiveChapterBySubjectFragment.newInstance(
                    title,
                    subject.getId(),
                    subject.getName(),
                    subject.getName(),
                    null,
                    chapterType,
                    featureType
            );
            fragmentMap.put(position, fragment);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return subjectList.size();
        }

        public Collection<Fragment> getAllFragments() {
            return fragmentMap.values();
        }
    }
}
