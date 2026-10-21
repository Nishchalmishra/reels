package arivihan.technologies.doubtbuzzter2.activity.competitive.subject;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_COMPETITIVE_ADAPTIVE_CHAPTER_LIST;

import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.BaseActivity;
import arivihan.technologies.doubtbuzzter2.activity.TestSeriesListActivity;
import arivihan.technologies.doubtbuzzter2.adapter.CheckBoxItemAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.fragments.competitive.learn.CompetitiveLearnFragment;
import arivihan.technologies.doubtbuzzter2.fragments.competitive.subject.CompetitiveSubjectChaptersFragment;
import arivihan.technologies.doubtbuzzter2.helper.CompetitiveBannerView;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.listener.competitive.RefreshActivityListener;
import arivihan.technologies.doubtbuzzter2.model.ItemSelectionDto;
import arivihan.technologies.doubtbuzzter2.model.NameAndIdDto;
import arivihan.technologies.doubtbuzzter2.model.competitive.subject.TopicWiseChapterListResponse;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class AdaptiveSubjectPracticeActivity extends BaseActivity implements CheckBoxItemAdapter.OnItemSelectionListener, RefreshActivityListener {

    private static final String TAG = AdaptiveSubjectPracticeActivity.class.getSimpleName();
    private ShimmerFrameLayout shimmer;
    private LinearLayout toolbar;
    SearchView searchView;
    ImageView filter_chapter;
    RelativeLayout rl_filter;
    private LinearLayout ll_back, ll_back_arrow;
    private LinearLayout ll_parent_layout;
    private RelativeLayout rl_tab_layout;
    private TextView tv_title, tv_filter_name, tv_filter_count;
    private TabLayout tabs;

    private LinearLayout ll_clear_filter_options;
    private CardView cv_tab_left_scroll, cv_tab_right_scroll, cv_apply, cv_filter_count;
    private ViewPager2 viewpager;
    private ViewPagerAdapter viewPagerAdapter;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private RecyclerView rv_filter;

    private boolean applyClicked = false;

    // Data Variables
    boolean apiDataFetched;
    boolean hasSearchText;
    String searchText = "";
    private Gson gson = new Gson();
    NetworkStatusLiveData networkStatusLiveData;
    private int tabsSize = 0;
    private String title;
    boolean filterVisible;
    private ArrayList<ItemSelectionDto> filterItemSelectionDtos;
    //    private ArrayList<ItemSelectionDto> selectedFilterItemDtos;
    private boolean refreshPage;
    private ArrayList<ItemSelectionDto> selectedFilterItemSelection = new ArrayList<>();
    private TopicWiseChapterListResponse topicWiseChapterListResponse;
    private int currentTabPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adaptive_subject_practice);

        if (savedInstanceState != null) {
            currentTabPosition = savedInstanceState.getInt("currentTabPosition", 0);
        }

        Uri data = getIntent().getData();

        if (data != null) {
            // From deep link
            title = data.getQueryParameter("title");
        } else {
            // From intent extras
            title = getIntent().getStringExtra("title");
        }

        if (StringUtils.isEmpty(title)) {
            title = MyApplication.getFirebaseRemoteConfig().getString("adaptive_subject_practise_screen_title_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
        }

        shimmer = findViewById(R.id.shimmer);
        toolbar = findViewById(R.id.toolbar);
        ll_back = findViewById(R.id.ll_back);
        searchView = findViewById(R.id.search_view);

        ll_clear_filter_options =
                findViewById(R.id.ll_clear_filter_options);
        ll_clear_filter_options.setOnClickListener(v -> {
            clearAllFilters();
        });
        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);

        filter_chapter = findViewById(R.id.filter_chapter);
        ll_parent_layout = findViewById(R.id.ll_parent_layout);
        rl_tab_layout = findViewById(R.id.rl_tab_layout);
        tv_title = findViewById(R.id.tv_title);
        tabs = findViewById(R.id.tabs);
        cv_tab_left_scroll = findViewById(R.id.cv_tab_left_scroll);
        cv_tab_right_scroll = findViewById(R.id.cv_tab_right_scroll);
        viewpager = findViewById(R.id.view_pager_chapter);
        rl_filter = findViewById(R.id.rl_filter);
        ll_back_arrow = findViewById(R.id.ll_back_arrow);
        tv_filter_name = findViewById(R.id.tv_filter_name);
        cv_apply = findViewById(R.id.cv_apply);
        cv_filter_count = findViewById(R.id.cv_filter_count);
        tv_filter_count = findViewById(R.id.tv_filter_count);
        rv_filter = findViewById(R.id.rv_filter);

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                filterVisible = true;
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                filterVisible = false;
                setApplyFilter(true);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

        setSearchView();
        shimmer.setVisibility(View.GONE);
//        setFilterLayout();

        lockUnlockDrawer(true);
        rl_filter.setVisibility(View.GONE);

        if (StringUtils.isNotEmpty(title)) {
            tv_title.setText(title);
        }
        cv_tab_left_scroll.setOnClickListener(v -> tabs.smoothScrollBy(-tabs.getWidth(), 0));
        cv_tab_right_scroll.setOnClickListener(v -> tabs.smoothScrollBy(tabs.getWidth(), 0));

        tabs.getViewTreeObserver().addOnScrollChangedListener(this::updateTabArrowsVisibility);

        ll_back.setOnClickListener(v -> {
            onBackPressedActivity("UI_BACK");
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBackPressedActivity("NATIVE");
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        cv_apply.setOnClickListener(v -> {
            closeDrawer();
            setApplyFilter(true);
        });

        ll_back_arrow.setOnClickListener(v -> closeDrawer());

        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            if (isOnline) {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    CommonMethods.showPositiveSnackbar(ll_parent_layout, getString(R.string.InternetConnection));
                    CommonMethods.setInternetConnectStatusToSharedPreference(true);
                }
            } else {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                    CommonMethods.showNegativeSnackbar(ll_parent_layout, getString(R.string.NoInternetConnection));
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
        }

        getDataFromApi(false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (refreshPage) {
            getDataFromApi(true);
            refreshPage = false;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentTabPosition", currentTabPosition);
    }

    private void getDataFromApi(boolean isRefresh) {
        getChapters(isRefresh);
    }

    private void setFilterLayout(List<String> priorityFilterOptions) {

        if (filterItemSelectionDtos != null) {
            return;
        }

        lockUnlockDrawer(false);
        rl_filter.setVisibility(View.VISIBLE);
        filter_chapter.setOnClickListener(v -> {
            try {
                filterVisible = true;
                openDrawer();
                Map<String, String> params = new HashMap<>();
                params.put("filterOptions", topicWiseChapterListResponse != null ? (topicWiseChapterListResponse.getPriorityFilterOptions() != null ? topicWiseChapterListResponse.getPriorityFilterOptions().toString() : "") : "");
                params.put("remainingTopicCount", topicWiseChapterListResponse != null ? (topicWiseChapterListResponse.getBanner() != null ? String.valueOf(topicWiseChapterListResponse.getBanner().getRemainingCount()) : "") : "");
                params.put("totalTopicCount", topicWiseChapterListResponse != null ? (topicWiseChapterListResponse.getBanner() != null ? String.valueOf(topicWiseChapterListResponse.getBanner().getTotalTopicCount()) : "") : "");
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.adaptive_chapter_list_filter_clicked.getActionName(), params);
            } catch (Exception ignored) {
            }
        });

        filterItemSelectionDtos = new ArrayList<>();
        selectedFilterItemSelection = new ArrayList<>();
        for (String filterItem : priorityFilterOptions) {

            ItemSelectionDto itemSelectionDto =
                    new ItemSelectionDto(filterItem, filterItem, false);

            itemSelectionDto.setSelected(false);

            filterItemSelectionDtos.add(itemSelectionDto);
        }
        CheckBoxItemAdapter checkBoxItemAdapter = new CheckBoxItemAdapter(this, filterItemSelectionDtos, this);
        rv_filter.setAdapter(checkBoxItemAdapter);

        setApplyFilter(false);
    }

    private void clearAllFilters() {

        try {
            Log.d(TAG, "Clear Filter Clicked");
            selectedFilterItemSelection.clear();

            if (filterItemSelectionDtos != null) {
                for (ItemSelectionDto item : filterItemSelectionDtos) {
                    item.setSelected(false);
                }
            }
            if (rv_filter.getAdapter() != null) {
                rv_filter.getAdapter().notifyDataSetChanged();
            }
            cv_filter_count.setVisibility(View.GONE);
            ll_clear_filter_options.setVisibility(View.GONE);

            setFilterToFragment(searchText, true);

            closeDrawer();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void setApplyFilter(boolean isButtonClicked) {

        cv_filter_count.setVisibility(selectedFilterItemSelection.isEmpty()
                ? View.GONE
                : View.VISIBLE);

        tv_filter_count.setText(String.valueOf(selectedFilterItemSelection.size()));

        ll_clear_filter_options.setVisibility(
                selectedFilterItemSelection.isEmpty()
                        ? View.GONE
                        : View.VISIBLE
        );

        setFilterToFragment(searchText, isButtonClicked);
    }


    public void openDrawer() {
        if (drawerLayout != null && !drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.openDrawer(GravityCompat.END);
        }
    }

    public void closeDrawer() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        }
    }

    public void lockUnlockDrawer(boolean lock) {
        if (lock) {
            drawerLayout.setDrawerLockMode(
                    DrawerLayout.LOCK_MODE_LOCKED_CLOSED
            );
        } else {
            drawerLayout.setDrawerLockMode(
                    DrawerLayout.LOCK_MODE_UNLOCKED
            );
        }
    }

    private void setSearchView() {
        searchView = findViewById(R.id.search_view);
        searchView.setQueryHint(getResources().getString(R.string.search));
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasSearchText) {
//                    hasSearchText = false;
//                    searchText = "";
//                    searchView.setQuery("", true);
//                } else if (!hasFocus) {
//                    searchView.setIconified(true);
//                }
            }
        });

        ImageView searchButton = searchView.findViewById(R.id.search_button);
        searchButton.setOnClickListener(v -> {
            try {
                searchView.setIconified(false);
                searchView.setMaxWidth(Integer.MAX_VALUE);
                tv_title.setVisibility(View.GONE);

                Map<String, String> params = new HashMap<>();
                params.put("selectedFilterCount", selectedFilterItemSelection != null ? String.valueOf(selectedFilterItemSelection.size()) : "");
                params.put("remainingTopicCount", topicWiseChapterListResponse != null ? (topicWiseChapterListResponse.getBanner() != null ? String.valueOf(topicWiseChapterListResponse.getBanner().getRemainingCount()) : "") : "");
                params.put("totalTopicCount", topicWiseChapterListResponse != null ? (topicWiseChapterListResponse.getBanner() != null ? String.valueOf(topicWiseChapterListResponse.getBanner().getTotalTopicCount()) : "") : "");
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.adaptive_chapter_list_search_clicked.getActionName(), params);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        searchView.setOnCloseListener(() -> {
            tv_title.setVisibility(View.VISIBLE);
            return false;
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                hasSearchText = !text.isEmpty();
                searchText = text;
                setFilterToFragment(text, false);
                return false;
            }
        });
    }

    private void setFilterToFragment(String text, boolean isApplyClick) {
        ArrayList<String> filters = new ArrayList<>();
        if (viewPagerAdapter != null) {
            Collection<Fragment> map = viewPagerAdapter.getAllFragments();
            for (ItemSelectionDto itemSelectionDto : selectedFilterItemSelection) {
                filters.add(itemSelectionDto.getId());
            }
            for (Fragment fragment : map) {
                if (fragment instanceof CompetitiveSubjectChaptersFragment) {
                    Log.d(TAG, "Filter Activity: " + text + ", " + selectedFilterItemSelection.size());
                    ((CompetitiveSubjectChaptersFragment) fragment).setFilter(text, filters);
                }
            }
        }

        Map<String, String> params = new HashMap<>();
        params.put("selectedFilter", filters != null ? filters.toString() : "");
        params.put("selectedFilterCount", filters != null ? String.valueOf(filters.size()) : "");
        params.put("remainingTopicCount", topicWiseChapterListResponse != null ? (topicWiseChapterListResponse.getBanner() != null ? String.valueOf(topicWiseChapterListResponse.getBanner().getRemainingCount()) : "") : "");
        params.put("totalTopicCount", topicWiseChapterListResponse != null ? (topicWiseChapterListResponse.getBanner() != null ? String.valueOf(topicWiseChapterListResponse.getBanner().getTotalTopicCount()) : "") : "");
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.adaptive_chapter_list_filter_apply_clicked.getActionName(), params);
    }

    private void setUpBanner(TopicWiseChapterListResponse topicWiseChapterListResponse) {
//        Map<String, Object> params = new HashMap<>();
//        NavigationConfig navigationConfig = new NavigationConfig();
//        navigationConfig.setClassName(
//                "arivihan.technologies.doubtbuzzter2.activity.CampaignSubscriptionActivity"
//        );
//        navigationConfig.setParams(params);
//        bannerDto apiBanner = bannerDto.builder()
//                .bannerType(BannerType.DYNAMIC_TEXT)
//                .title("Master any 2 topics for free")
//                .subtitle("2 topics remaining")
//                .imageUrl("https://dm80t6147awlm.cloudfront.net/json-files/Changing+text.json")
//                .backgroundColor("#00ACC1")
//                .isVisible(true)
//                .navigationConfig(navigationConfig)
//                .build();


        CompetitiveBannerView bannerView = findViewById(R.id.bannerView);
        if (topicWiseChapterListResponse.getBanner() != null) {
            bannerView.setBannerData(topicWiseChapterListResponse.getBanner());
            bannerView.setVisibility(View.VISIBLE);
        } else {
            bannerView.setVisibility(View.GONE);
        }
    }

    private void onBackPressedActivity(String type) {
        try {
            Log.d(TAG, "onBackPressedActivity: " + getIntent().getStringExtra("Screen"));

            Map<String, String> params = new HashMap<>();
            params.put("drawerOpen", String.valueOf(filterVisible));
            params.put("type", type != null ? type : "");
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.adaptive_chapter_list_back_clicked.getActionName(), params);

            if (filterVisible) {
                filterVisible = false;
                closeDrawer();
            }
            finish();
        } catch (Exception ignored){}
    }

    private void getChapters(boolean isRefresh) {
        shimmer.startShimmer();
        shimmer.setVisibility(View.VISIBLE);
        Map<String, Object> map = new HashMap<>();
        Log.d(TAG, "Request: " + map.toString());
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_COMPETITIVE_ADAPTIVE_CHAPTER_LIST, map), null,
                response -> {
                    try {
                        Log.d(TAG, "Response: " + response.toString());
                        Gson gson = new Gson();
                        TypeToken<ApiWrappedResponse<TopicWiseChapterListResponse>> token = new TypeToken<ApiWrappedResponse<TopicWiseChapterListResponse>>() {
                        };
                        ApiWrappedResponse<TopicWiseChapterListResponse> apiWrappedResponse = gson.fromJson(response.toString(), token.getType());
                        topicWiseChapterListResponse = apiWrappedResponse.getData();

                        shimmer.stopShimmer();
                        shimmer.setVisibility(View.GONE);
                        setUIData(topicWiseChapterListResponse, isRefresh);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                shimmer.stopShimmer();
                shimmer.setVisibility(View.GONE);
                CommonMethods.showNegativeSnackbar(ll_parent_layout, error.getMessage());
            }
        });
        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
    }

    private void setUIData(TopicWiseChapterListResponse topicWiseChapterListResponse, boolean isRefresh) {
        try {
            apiDataFetched = true;
            setTabData(topicWiseChapterListResponse, isRefresh);
            if (topicWiseChapterListResponse.getPriorityFilterOptions() != null && !topicWiseChapterListResponse.getPriorityFilterOptions().isEmpty()) {
                setFilterLayout(topicWiseChapterListResponse.getPriorityFilterOptions());
            }

            setUpBanner(topicWiseChapterListResponse);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setTabData(TopicWiseChapterListResponse topicWiseChapterListResponse, boolean isRefresh) {

        Log.d(TAG, "TABs Refresh : " + isRefresh);
        List<NameAndIdDto> subjectList = topicWiseChapterListResponse.getSubjects();

        if (viewPagerAdapter == null) {
            viewPagerAdapter = new ViewPagerAdapter(this, subjectList);
            viewpager.setAdapter(viewPagerAdapter);
            viewpager.setOffscreenPageLimit(viewPagerAdapter.getItemCount());
        }

        if (currentTabPosition >= subjectList.size()) currentTabPosition = 0;

        tabs.clearOnTabSelectedListeners();
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(
                tabs, viewpager,
                (tab, position) -> tab.setCustomView(getCustomTab(subjectList.get(position).getName(), position == currentTabPosition))
        );
        tabLayoutMediator.attach();

        viewpager.setCurrentItem(currentTabPosition, false);

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateTabUI(tab, true);
                currentTabPosition = tab.getPosition();
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
        tabsSize = topicWiseChapterListResponse.getSubjects().size();

        if (isRefresh) {
            Log.d(TAG, "TAB Set Filter: ");
            viewpager.post(() -> setApplyFilter(false));
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
        View tabView = LayoutInflater.from(this).inflate(R.layout.competitive_tab_layout, null);
        MaterialCardView tabCard = tabView.findViewById(R.id.tab_text_card);
        TextView tabText = tabView.findViewById(R.id.tab_text);
        Typeface customFontUnselected = ResourcesCompat.getFont(this, R.font.nunito_regular);
        Typeface customFontSelected = ResourcesCompat.getFont(this, R.font.nunito_semibold);
        tabText.setText(title);
//        tabText.setText(CommonMethods.getShortName(title));
        tabText.setText(title);
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

    @Override
    public void onItemSelected(ItemSelectionDto item, int position, boolean isSelected) {
        try {
            Log.d(TAG, "Filter Option Selection change: " + isSelected + ", " + position);
            if (isSelected) {
                // Add only if not already present
                if (!selectedFilterItemSelection.contains(item)) {
                    selectedFilterItemSelection.add(item);
                }
            } else {
                // Remove if unchecked
                selectedFilterItemSelection.remove(item);
            }
            Log.d(TAG, "Filter Option Selection change List Size:" + selectedFilterItemSelection.size());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void refreshPage(boolean refresh) {
        refreshPage = refresh;
    }

    @Override
    public void doRefreshNow(boolean refresh) {
        if(refresh) {

        }
    }


    static class ViewPagerAdapter extends FragmentStateAdapter {
        private List<NameAndIdDto> subjectList;
        private final Map<Integer, Fragment> fragmentMap = new HashMap<>();

        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<NameAndIdDto> subjectList) {
            super(fragmentActivity);
            this.subjectList = subjectList;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment fragment = CompetitiveSubjectChaptersFragment.newInstance(subjectList.get(position).getId(), subjectList.get(position).getName());
            fragmentMap.put(position, fragment);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return subjectList.size();
        }

        public String getTitle(int position) {
            return StringUtils.isNotEmpty(subjectList.get(position).getName()) ? subjectList.get(position).getName() : "Subject";
        }

        public Fragment getFragment(int position) {
            return fragmentMap.get(position);
        }

        public Collection<Fragment> getAllFragments() {
            return fragmentMap.values();
        }
    }

}