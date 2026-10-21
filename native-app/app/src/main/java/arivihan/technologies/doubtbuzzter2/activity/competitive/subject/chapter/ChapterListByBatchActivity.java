package arivihan.technologies.doubtbuzzter2.activity.competitive.subject.chapter;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_GET_CHAPTER_USER_BATCH;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_CHAPTER_USER_BATCH;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.AnalyticsEvent;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.fragments.competitive.subject.chapter.ChapterListByBatchFragment;
import arivihan.technologies.doubtbuzzter2.helper.ScreenAnalyticsHelper;
import arivihan.technologies.doubtbuzzter2.model.batch.UserBatchDto;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class ChapterListByBatchActivity extends AppCompatActivity {

    private static final String TAG = ChapterListByBatchActivity.class.getSimpleName();

    // UI Variables
    private ShimmerFrameLayout sfl_shimmer;
    private LinearLayout ll_parent_layout, back_button_layout, ll_shimmer, ll_data;
    private TextView activity_title;
    private SearchView searchView;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ViewPagerAdapter viewPagerAdapter;

    // Data variables
    private boolean hasSearchText;
    private String subjectId, subjectCode, title;
    private String boardCode;
    private String batchId;
    private String navigationFrom;
    private String scrollToChapterId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chapter_list_by_batch);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (searchView != null && searchView.hasFocus()) {
                    InputMethodManager imm =
                            (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                    }
                    searchView.clearFocus();
                } else {
                    finish();
                }
            }
        });

        Uri data = getIntent().getData();

        if (data != null) {
            // From deep link
            title = data.getQueryParameter("title");
            subjectId = data.getQueryParameter("SubjectId");
            subjectCode = data.getQueryParameter("SubjectCode");
            batchId = data.getQueryParameter("batchId");
            boardCode = data.getQueryParameter("boardCode");
            scrollToChapterId = data.getQueryParameter("scrollToChapterId");
            navigationFrom = "deeplink";
        } else {
            // From intent extras
            title = getIntent().getStringExtra("title");
            subjectId = getIntent().getStringExtra("SubjectId");
            subjectCode = getIntent().getStringExtra("SubjectCode");
            batchId = getIntent().getStringExtra("batchId");
            boardCode = getIntent().getStringExtra("boardCode");
            String screen = getIntent().getStringExtra("Screen");
            navigationFrom = StringUtils.isNotEmpty(screen) ? screen : "unknown";
            scrollToChapterId = getIntent().getStringExtra("scrollToChapterId");
        }

        Log.d(TAG, "ScrollDebug: onCreate -> scrollToChapterId=" + scrollToChapterId + ", navigationFrom=" + navigationFrom + ", subjectId=" + subjectId + ", batchId=" + batchId);

        Map<String, String> navParams = new HashMap<>();
        navParams.put("navigation_from", navigationFrom);
        navParams.put("subjectId", subjectId != null ? subjectId : "");
        navParams.put("subjectCode", subjectCode != null ? subjectCode : "");
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.chap_list_screen_view.getActionName(), navParams);
        ScreenAnalyticsHelper.sendScreenVisitedEvent(AnalyticsEvent.CHAPTER_LIST_SCREEN.name(), AnalyticsEvent.CHAPTER_LIST_SCREEN.name());

        Log.d(TAG, "onCreate- boardCode: " + boardCode);

        ll_shimmer = findViewById(R.id.ll_shimmer);
        ll_data = findViewById(R.id.ll_data);
        sfl_shimmer = findViewById(R.id.sfl_shimmer);
        ll_parent_layout = findViewById(R.id.ll_parent_layout);
        activity_title = findViewById(R.id.activity_title);
        back_button_layout = findViewById(R.id.back_button_layout);

        setSearchView();
        back_button_layout.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        if (StringUtils.isEmpty(title)) {
            title = MyApplication.getFirebaseRemoteConfig().getString("adaptive_chapters_by_subject_screen_title_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
        }
        Log.d(TAG, "onCreate-title: " + title);
        if (StringUtils.isNotEmpty(title)) {
            activity_title.setText(title);
        }
        ll_shimmer.setVisibility(View.GONE);
        sfl_shimmer.setVisibility(View.GONE);

//        viewPagerAdapter = new ViewPagerAdapter(this);
//        viewPager.setAdapter(viewPagerAdapter);
//
//        viewPager.setOffscreenPageLimit(viewPagerAdapter.getItemCount());
//
//        new TabLayoutMediator(tabLayout, viewPager,
//                (tab, position) -> tab.setText(tabTitles[position])
//        ).attach();

        getUserBatchData();

    }

    private void getUserBatchDataCompetitive() {
        ll_shimmer.setVisibility(View.VISIBLE);
        sfl_shimmer.startShimmer();
        sfl_shimmer.setVisibility(View.VISIBLE);

        Map<String, Object> map = new HashMap<>();
        map.put("subjectId", subjectId);
        if (StringUtils.isNotEmpty(boardCode)) {
            map.put("boardCode", boardCode);
        }
        Log.d(TAG, "Request (competitive): " + map.toString());
        MyJsonArrayRequest myJsonArrayRequest = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_CHAPTER_USER_BATCH, map), null,
                response -> {
                    try {
                        if (isFinishing() || isDestroyed()) return;
                        Log.d(TAG, "---> Response: " + response);
                        ll_shimmer.setVisibility(View.GONE);
                        sfl_shimmer.stopShimmer();
                        sfl_shimmer.setVisibility(View.GONE);

                        Gson gson = new Gson();
                        TypeToken<List<UserBatchDto>> token = new TypeToken<List<UserBatchDto>>() {
                        };
                        List<UserBatchDto> userBatchDtoList = gson.fromJson(response.toString(), token.getType());
                        Log.d(TAG, "data (competitive): " + (userBatchDtoList != null ? userBatchDtoList.toString() : "null"));

                        if (userBatchDtoList == null) return;
                        setupTabsAndViewPager(userBatchDtoList);
                    } catch (Exception ex) {
                        ll_shimmer.setVisibility(View.GONE);
                        sfl_shimmer.stopShimmer();
                        sfl_shimmer.setVisibility(View.GONE);
                        ex.printStackTrace();
                    }
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                ll_shimmer.setVisibility(View.GONE);
                sfl_shimmer.stopShimmer();
                sfl_shimmer.setVisibility(View.GONE);
                CommonMethods.showNegativeSnackbar(ll_parent_layout, error.getMessage());
            }
        });

        myJsonArrayRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonArrayRequest);
    }

    private void getUserBatchData() {
        ll_shimmer.setVisibility(View.VISIBLE);
        sfl_shimmer.startShimmer();
        sfl_shimmer.setVisibility(View.VISIBLE);

        Map<String, Object> map = new HashMap<>();
        map.put("subjectId", subjectId);
        if (StringUtils.isNotEmpty(batchId)) {
            map.put("batchId", batchId);
        }
        Log.d(TAG, "Request: " + map.toString());
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(AV_GET_CHAPTER_USER_BATCH, map), null,
                response -> {
                    try {
                        ll_shimmer.setVisibility(View.GONE);
                        sfl_shimmer.stopShimmer();
                        sfl_shimmer.setVisibility(View.GONE);

                        Gson gson = new Gson();
                        TypeToken<ApiWrappedResponse<List<UserBatchDto>>> token = new TypeToken<ApiWrappedResponse<List<UserBatchDto>>>() {
                        };
                        ApiWrappedResponse<List<UserBatchDto>> apiResponse = gson.fromJson(response.toString(), token.getType());

                        if (isFinishing() || isDestroyed()) return;
                        List<UserBatchDto> userBatchDtoList = apiResponse.getData();
                        Log.d(TAG, "data: " + (userBatchDtoList != null ? userBatchDtoList.toString() : "null"));

                        if (userBatchDtoList == null) {
                            ll_shimmer.setVisibility(View.GONE);
                            sfl_shimmer.stopShimmer();
                            sfl_shimmer.setVisibility(View.GONE);
                            return;
                        }
                        setupTabsAndViewPager(userBatchDtoList);
                    } catch (Exception ex) {
                        ll_shimmer.setVisibility(View.GONE);
                        sfl_shimmer.stopShimmer();
                        sfl_shimmer.setVisibility(View.GONE);
                        ex.printStackTrace();
                    }
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                ll_shimmer.setVisibility(View.GONE);
                sfl_shimmer.stopShimmer();
                sfl_shimmer.setVisibility(View.GONE);
                CommonMethods.showNegativeSnackbar(ll_parent_layout, error.getMessage());
            }
        });

        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
    }


    private void setTabData(List<UserBatchDto> list) {
        try {
            ArrayList<String> boardCodes = new ArrayList<>();
            for (UserBatchDto model : list) {
                boardCodes.add(model.getCode());
            }
            int indexOfSelectedSubjectId = StringUtils.isNotEmpty(boardCode) ? boardCodes.indexOf(boardCode) : 0;

            Log.d(TAG, "Batch Id List: " + list.toString());
            viewPagerAdapter = new ViewPagerAdapter(this, list, subjectId, scrollToChapterId);
            viewPager.setAdapter(viewPagerAdapter);
            viewPager.setOffscreenPageLimit(viewPagerAdapter.getItemCount());

//            TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(
//                    tabLayout,
//                    viewPager,
//                    (tab, position) -> {
//                        tab.setCustomView(getCustomTab(list.get(position).getSubjectName(), position == 0));
//                    }
//            );
//            tabLayoutMediator.attach();

//            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//                @Override
//                public void onTabSelected(TabLayout.Tab tab) {
//                    updateTabUI(tab, true);
//                }
//
//                @Override
//                public void onTabUnselected(TabLayout.Tab tab) {
//                    updateTabUI(tab, false);
//                }
//
//                @Override
//                public void onTabReselected(TabLayout.Tab tab) {
//                }
//            });
            tabLayout.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.VISIBLE);
//            tabsSize = subjectIds.size();

            setSelectedTab(indexOfSelectedSubjectId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setupTabsAndViewPager(List<UserBatchDto> batchList) {

        ArrayList<String> boardCodes = new ArrayList<>();
        int index = 0;
        for (int i = 0; i < batchList.size(); i++) {
            if (batchList.get(i).isDefaultSelection())
                index = i;
        }
//        int index = StringUtils.isNotEmpty(boardCode) ? boardCodes.indexOf(boardCode) : indexOfBoard;

        Log.d(TAG, "ScrollDebug: setupTabsAndViewPager -> scrollToChapterId=" + scrollToChapterId + ", batchList.size=" + batchList.size() + ", defaultSelectedIndex=" + index);
        viewPagerAdapter =
                new ViewPagerAdapter(this, batchList, subjectId, scrollToChapterId);

        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(batchList.size());

        // Dynamic TabLayout behavior
        if (batchList.size() <= 3) {
            tabLayout.setTabMode(TabLayout.MODE_FIXED);
            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        } else {
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        }

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            View customView = LayoutInflater.from(this)
                    .inflate(R.layout.layout_tab_text_16, null);

            TextView tabText = customView.findViewById(R.id.tabText);
            tabText.setText(batchList.get(position).getName());

            tabText.setTextColor(ContextCompat.getColor(ChapterListByBatchActivity.this, R.color.text_secondary));

            tab.setCustomView(customView);
        }).attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View view = tab.getCustomView();
                if (view != null) {
                    TextView tv = view.findViewById(R.id.tabText);
                    tv.setTextColor(ContextCompat.getColor(ChapterListByBatchActivity.this, R.color.black_12)); // selected
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View view = tab.getCustomView();
                if (view != null) {
                    TextView tv = view.findViewById(R.id.tabText);
                    tv.setTextColor(ContextCompat.getColor(ChapterListByBatchActivity.this, R.color.text_secondary)); // unselected
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // no-op
            }
        });

//        new TabLayoutMediator(tabLayout, viewPager,
//                (tab, position) -> {
//                    tab.setText(batchList.get(position).getDisplayName());
//                }).attach();

        viewPager.setUserInputEnabled(false);

        if (batchList.size() > 1) {
            tabLayout.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.VISIBLE);
        } else if (batchList.size() == 1) {
            tabLayout.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);
        } else {
            tabLayout.setVisibility(View.GONE);
            viewPager.setVisibility(View.GONE);
        }

        setSelectedTab(index);
    }

    private void setSelectedTab(int selectedTab) {
        try {
            viewPager.setCurrentItem(selectedTab, true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setSearchView() {
        searchView = findViewById(R.id.search_view);
        searchView.setQueryHint(getResources().getString(R.string.search));

        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
        });

        ImageView searchButton = searchView.findViewById(R.id.search_button);
        searchButton.setOnClickListener(v -> {
            Map<String, String> p = new HashMap<>();
            p.put("navigation_from", navigationFrom);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.chap_search_btn_click.getActionName(), p);
            searchView.setIconified(false);
            searchView.setMaxWidth(Integer.MAX_VALUE);
            activity_title.setVisibility(View.GONE);
        });

        searchView.setOnCloseListener(() -> {
            Map<String, String> p = new HashMap<>();
            p.put("navigation_from", navigationFrom);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.chap_search_close_btn_click.getActionName(), p);
            activity_title.setVisibility(View.VISIBLE);
            return false;
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                setFiltersToFragments(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                hasSearchText = !query.isEmpty();
                setFiltersToFragments(query);
                return true;
            }
        });
    }


    private void setFiltersToFragments(String query) {
        try {
//            if (viewPagerAdapter != null) {
//                List<ChapterListByBatchFragment> fragments = (List<ChapterListByBatchFragment>) viewPagerAdapter.getAllFragments();
//                for (ChapterListByBatchFragment fragment : fragments) {
//                    fragment.setFilter(query);
//                }
//            }

            if (viewPagerAdapter != null) {
                List<ChapterListByBatchFragment> fragments =
                        new ArrayList<>(viewPagerAdapter.getAllFragments());

                for (ChapterListByBatchFragment fragment : fragments) {
                    if (fragment != null && fragment.isAdded()) {
                        fragment.setFilter(query);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getApplicationContext() != null) {
            MyVolleySingleton.getInstance(this.getApplicationContext()).getRequestQueue().cancelAll(TAG);
        }
    }

    static class ViewPagerAdapter extends FragmentStateAdapter {
        private List<UserBatchDto> batchDtoList;
        private final String subjectId;
        private final String scrollToChapterId;
        private final Map<Integer, ChapterListByBatchFragment> fragmentMap = new HashMap<>();

        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity,
                                List<UserBatchDto> batchDtoList, String subjectId) {
            this(fragmentActivity, batchDtoList, subjectId, null);
        }

        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity,
                                List<UserBatchDto> batchDtoList, String subjectId, String scrollToChapterId) {
            super(fragmentActivity);
            this.batchDtoList = batchDtoList;
            this.subjectId = subjectId;
            this.scrollToChapterId = scrollToChapterId;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            UserBatchDto userBatchDto = batchDtoList.get(position);
            Log.d("ChapterListByBatchActivity", "ScrollDebug: createFragment pos=" + position + ", batchId=" + userBatchDto.getId() + ", batchName=" + userBatchDto.getName() + ", passing scrollToChapterId=" + scrollToChapterId);
            ChapterListByBatchFragment fragment = ChapterListByBatchFragment.newInstance(userBatchDto.getId(), StringUtils.isNotEmpty(userBatchDto.getName()) ? userBatchDto.getName() : userBatchDto.getDisplayName(), StringUtils.isNotEmpty(userBatchDto.getCode()) ? userBatchDto.getCode() : userBatchDto.getBoardCode(), subjectId, userBatchDto.getSubjects(), userBatchDto.isBatchAddOn(), null, scrollToChapterId);
            fragmentMap.put(position, fragment);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return batchDtoList.size();
        }

        public void updateDataList(ArrayList<UserBatchDto> newList) {
            this.batchDtoList = newList;
            fragmentMap.clear();
            notifyDataSetChanged();
        }

        public UserBatchDto getTitle(int position) {
            return batchDtoList.get(position);
        }

        public Fragment getFragment(int position) {
            return fragmentMap.get(position);
        }

        public Collection<ChapterListByBatchFragment> getAllFragments() {
            return fragmentMap.values();
        }
    }
}