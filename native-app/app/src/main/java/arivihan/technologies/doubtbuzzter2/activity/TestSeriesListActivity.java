package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_CAROUSEL;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_TEST_SERIES_LIST;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.TEST_SERIES_LIST;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.UPDATE_MOCK_VIDEO_WATCH_COUNT;
import static arivihan.technologies.doubtbuzzter2.enums.HOW_TO_TYPE.TEST_BOARD;
import static arivihan.technologies.doubtbuzzter2.enums.HOW_TO_TYPE.TEST_BOARD_CHAPETR;
import static arivihan.technologies.doubtbuzzter2.enums.HOW_TO_TYPE.TEST_JEE;
import static arivihan.technologies.doubtbuzzter2.enums.HOW_TO_TYPE.TEST_JEE_CHAPETR;
import static arivihan.technologies.doubtbuzzter2.enums.HOW_TO_TYPE.TEST_NEET;
import static arivihan.technologies.doubtbuzzter2.enums.HOW_TO_TYPE.TEST_NEET_CHAPETR;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.helper.widget.Carousel;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.interfaces.ItemClickListener;
import com.denzcoskun.imageslider.models.SlideModel;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.ActivityLauncher;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.TestSeriesListAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.customView.HowToVideoView;
import arivihan.technologies.doubtbuzzter2.enums.HOW_TO_TYPE;
import arivihan.technologies.doubtbuzzter2.enums.competitive.AdaptivePath;
import arivihan.technologies.doubtbuzzter2.enums.test.TestSeriesType;
import arivihan.technologies.doubtbuzzter2.fragments.TestSeriesListFragment;
import arivihan.technologies.doubtbuzzter2.fragments.competitive.learn.CompetitiveLearnFragment;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.listener.SubscriptionCallback;
import arivihan.technologies.doubtbuzzter2.listener.TestSeriesClickListener;
import arivihan.technologies.doubtbuzzter2.model.carousel.CarouselResponse;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.model.HomeScreenCacheData;
import arivihan.technologies.doubtbuzzter2.model.SubjectEnum;
import arivihan.technologies.doubtbuzzter2.model.subject.SubjectResponse;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionCheckDto;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionLockPopupDto;
import arivihan.technologies.doubtbuzzter2.model.TestEvaluationStatusEnum;
import arivihan.technologies.doubtbuzzter2.model.TestSeriesSetDto;
import arivihan.technologies.doubtbuzzter2.model.TestSeriesSetResulList;
import arivihan.technologies.doubtbuzzter2.model.UserDto;
import arivihan.technologies.doubtbuzzter2.viewModel.HomeScreenViewModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class TestSeriesListActivity extends BaseActivity implements TestSeriesClickListener, TestSeriesListAdapter.MockTestClickListener {
    private Carousel carousel;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private Dialog dialog;
    private MaterialCardView card;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private CardView tabCard;
    private boolean separated;
    private Dialog progressDialog;
    private boolean selected;
    TestSeriesClickListener clickListener;
    private ViewPagerAdapter viewPagerAdapter;
    String testId;
    String testSeriesType;
    String testTitle;
    boolean fromHalfYearly;
    int selectedId = 0;
    RadioButton attempt, unattempt;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    CardView apply;
    SearchView searchView;
    String videoUrl;
    boolean hasSearchText;
    LinearLayout back;
    ImageView filterMock;
    boolean filterVisible;
    List<CarouselResponse> carouselDto = new ArrayList<>();
    String filterStatus = "";
    String filterSubject = "";
    MaterialCardView imageSlider;
    TextView title;
    String course = MyApplication.getStringData(PreferenceKeys.COURSE_NAME.getKey());
    MaterialCheckBox clear;
    private boolean pauseClicked;
    LinearLayout backButton;
    Gson gson;
    ArrayList<List<TestSeriesSetDto>> testSeriesSetDtoList = new ArrayList<>();
    ArrayList<List<TestSeriesSetDto>> testSeriesSetDtoListTemp = new ArrayList<>();
    ArrayList<TestSeriesSetDto> list = new ArrayList<>();
    ArrayList<TestSeriesSetDto> filteredList = new ArrayList<>();
    ArrayList<TestSeriesSetDto> filteredListTemp = new ArrayList<>();
    ArrayList<String> subjectNameList = new ArrayList<>();
    ArrayList<String> subjectCodeList = new ArrayList<>();
    ArrayList<String> subjectCodes = new ArrayList<>();
    ShimmerFrameLayout carouselShimmer;
    int selectedTab = 0;
    int lastSelected = 0;
    ShimmerFrameLayout shimmerFrameLayout;
    RecyclerView recyclerView;
    TestSeriesListAdapter testSeriesListAdapter;
    HomeScreenViewModel homeScreenViewModel;
    private Handler handler;
    MotionLayout motionLayout;
    private Runnable autoSwipeRunnable;
    private final long AUTO_SWIPE_DELAY = 2000; // Auto swipe delay in milliseconds (3 seconds)
    private final long AUTO_SWIPE_INTERVAL = 3000;
    ArrayList<SlideModel> imageList = new ArrayList<>();
    ImageSlider img_slider;
    private String TAG = TestSeriesListActivity.class.getSimpleName();
    String videoUrlKey;
    LinearLayout unlock_bottom_layout;
    TextView unlock_bottom_layout_text;
    TestSeriesSetResulList testSeriesSetResulList;
    DatabaseReference databaseReference;
    LinearLayout noResultFoundLayout;
    private HowToVideoView howToVideoView;
    private View how_to_video_view;
    private HOW_TO_TYPE howToType;
    NetworkStatusLiveData networkStatusLiveData;
    private Map<String, Object> activityParamsMap;
    TextView lockDialogText, lockDialogBtnText, lockDialogTitle;
    LottieAnimationView lockAnimation;

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("testSeriesType", testSeriesType);
        outState.putString("Id", testId);
        outState.putString("title", testTitle);
        outState.putBoolean("fromHalfYearly", fromHalfYearly);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        testSeriesType = savedInstanceState.getString("testSeriesType");
        testId = savedInstanceState.getString("Id");
        testTitle = savedInstanceState.getString("title");
        fromHalfYearly = savedInstanceState.getBoolean("fromHalfYearly");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_series_list);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (filterVisible) {
                    filterVisible = false;
                    closeDrawer();
                } else if (!searchView.isIconified()) {
                    if (!CollectionUtils.isEmpty(carouselDto)) {
                        imageSlider.setVisibility(View.VISIBLE);
                    }
                    searchView.setIconified(true);
                } else if (howToVideoView != null && howToVideoView.videoView != null
                        && howToVideoView.videoView.getVisibility() == View.VISIBLE && howToVideoView.expanded) {
                    howToVideoView.resize();
                }
                {
                    finish();
                }
            }
        });

        MyApplication.saveData(PreferenceKeys.RELOAD_TEST_LIST_DATA.getKey(), false);
        how_to_video_view = findViewById(R.id.how_to_video_view);
        noResultFoundLayout = findViewById(R.id.no_result_found_layout);
        recyclerView = findViewById(R.id.test_series_list_view);
        unlock_bottom_layout = findViewById(R.id.unlock_bottom_layout);
        unlock_bottom_layout_text = findViewById(R.id.unlock_bottom_layout_text);
        tabCard = findViewById(R.id.tab_card);

        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);

        tabLayout = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.viewpager);
        title = findViewById(R.id.title);
        filterMock = findViewById(R.id.filter_mock);
        if (getIntent().getBooleanExtra("hidefilterSection", false)) {
            filterMock.setVisibility(View.GONE);
        }
        backButton = findViewById(R.id.back_button_layout);
        backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        imageSlider = findViewById(R.id.image_slider);
        carouselShimmer = findViewById(R.id.carousel_shimmer);
        carouselShimmer.setVisibility(View.VISIBLE);
        carouselShimmer.startShimmer();
        shimmerFrameLayout = findViewById(R.id.list_shimmer);
        shimmerFrameLayout.startShimmer();

        if (savedInstanceState != null) {
            // Restore from savedInstanceState if Activity recreated
            restoreFromBundle(savedInstanceState);
        } else {
            // Otherwise, extract from Intent or Deep Link
            restoreFromIntent(getIntent());
        }

//        Uri data = getIntent().getData();
//
//        if (data != null) {
//            // From deeplink query parameters
//            testSeriesType = data.getQueryParameter("testSeriesType");
//            testId = data.getQueryParameter("Id");
//            testTitle = data.getQueryParameter("title");
//            fromHalfYearly = data.getBooleanQueryParameter("fromHalfYearly", false);
//        } else {
//            // From intent extras
//            testSeriesType = getIntent().getStringExtra("testSeriesType");
//            testId = getIntent().getStringExtra("Id");
//            testTitle = getIntent().getStringExtra("title");
//            fromHalfYearly = getIntent().getBooleanExtra("fromHalfYearly", false);
//        }

        getHowToVideoType(testId);
        MyApplication.saveData("selectedTab" + "_" + testId, 0);

        img_slider = findViewById(R.id.image_sliders);

        getCarousel();

        MyApplication.saveData(PreferenceKeys.SELECTED_TEST_SERIES_ID.getKey(), testId);
        MyApplication.saveData(PreferenceKeys.SELECTED_TEST_SERIES_TITLE.getKey(), testTitle);


        MyApplication application = (MyApplication) getApplication();
        homeScreenViewModel = application.getHomeScreenViewModel();
        homeScreenViewModel.getSubscribedSubjectData(false);
//        getSubscribedSubjects();

        title.setText(testTitle);

        searchView = findViewById(R.id.search_view);
        searchView.setQueryHint(getString(R.string.search));

        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasSearchText) {
                hasSearchText = false;
            } else if (!hasFocus) {
                if (!CollectionUtils.isEmpty(carouselDto)) {
                    imageSlider.setVisibility(View.VISIBLE);
                }
                searchView.setIconified(true);
            }
        });

        ImageView searchButton = searchView.findViewById(R.id.search_button);
        searchButton.setOnClickListener(v -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.test_search_btn_click.getActionName());
            imageSlider.setVisibility(View.GONE);
            searchView.setIconified(false);
            title.setVisibility(View.GONE);
        });
        searchView.setOnCloseListener(() -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.test_search_close_btn_click.getActionName());
            searchView.setQuery("", false);
            title.setVisibility(View.VISIBLE);
            return false;
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                hasSearchText = s.length() > 0;
                if (viewPagerAdapter != null) {
                    separateSubject(s);
                    viewPagerAdapter.notifyDataSetChanged();
                }
                if (testSeriesListAdapter != null) {
                    searchTest(s);
                    if (CollectionUtils.isEmpty(filteredList)) {
                        noResultFoundLayout.setVisibility(View.VISIBLE);
                        tabCard.setVisibility(View.GONE);
                    } else {
                        noResultFoundLayout.setVisibility(View.GONE);
                        tabCard.setVisibility(View.VISIBLE);
                        testSeriesListAdapter.notifyDataSetChanged();
                    }
                }

                return false;
            }
        });

        gson = new Gson();
        setFilterLayout();
//      CommonMethods.showPositiveSnackbar(backButton, getString(R.string.apply_filters_to_filter), 3500);
        selectedTab = 0;
        getTestSeriesSets();
        setDialog();

        MyApplication.getSubscriptionStatus(this, new SubscriptionCallback() {
            @Override
            public void onSuccess(SubscriptionCheckDto dto) {
                updateDialogContent(dto);
            }

            @Override
            public void onError(String errorMessage) {
                updateDialogContent(null);
            }
        });

        unlock_bottom_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent;
//                if (fromHalfYearly) {
//                    intent = new Intent(TestSeriesListActivity.this, CampaignSubscriptionActivity.class);
//                } else {
//                    intent = new Intent(TestSeriesListActivity.this, CampaignSubscriptionActivity.class);
////                    intent.putExtra("plan_level", "COURSE");
//                }
                destroyHowTo();
//                startActivity(intent);
                SubscriptionNavigationHelper.navigateToSubscriptionPage(TestSeriesListActivity.this, RedirectionSource.TEST_SERIES_JOIN);
            }
        });

        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            if (isOnline) {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {

                    if (CollectionUtils.isEmpty(list)) {
                        getTestSeriesSets();
                    }
                    if (CollectionUtils.isEmpty(carouselDto)) getCarousel();

                    CommonMethods.showPositiveSnackbar(searchView, getString(R.string.InternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
                }
            } else {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                    CommonMethods.showNegativeSnackbar(searchView, getString(R.string.NoInternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), false);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), true);
                }
            }
        });

    }

    private void restoreFromBundle(Bundle savedInstanceState) {
        testSeriesType = savedInstanceState.getString("testSeriesType");
        testId = savedInstanceState.getString("Id");
        testTitle = savedInstanceState.getString("title");
        fromHalfYearly = savedInstanceState.getBoolean("fromHalfYearly", false);
    }

    private void restoreFromIntent(Intent intent) {
        Uri data = intent.getData();
        if (data != null) {
            // From deeplink query parameters
            testSeriesType = data.getQueryParameter("testSeriesType");
            testId = data.getQueryParameter("Id");
            testTitle = data.getQueryParameter("title");
            fromHalfYearly = data.getBooleanQueryParameter("fromHalfYearly", false);
        } else {
            // From intent extras
            testSeriesType = intent.getStringExtra("testSeriesType");
            testId = intent.getStringExtra("Id");
            testTitle = intent.getStringExtra("title");
            fromHalfYearly = intent.getBooleanExtra("fromHalfYearly", false);
        }
    }

    private void getSubscribedSubjects() {
        homeScreenViewModel.getSubjectList().observeForever(this::updateSubject);
    }

    private void updateSubject(List<SubjectResponse> models) {

        Log.d(TAG, "---> Received Subjects: "+models.toString());
        List<SubjectResponse> subjectModels = new ArrayList<>();

        for (SubjectResponse model : models) {
            if (StringUtils.isNotEmpty(model.getSubjectCode()) && (subjectCodes.contains(model.getSubjectCode().toLowerCase()))) {
                subjectModels.add(model);
            }

            if(!subjectModels.contains(model) && model.getSubjectCode()!=null && model.getSubjectCode().equalsIgnoreCase(SubjectEnum.MATHEMATICS.getValue()) && subjectCodes.contains(SubjectEnum.MATHEMATICS.getAlternateValue().toLowerCase())) {
                subjectModels.add(model);
            }
        }

        subjectNameList.clear();
        subjectCodeList.clear();
        if (!CollectionUtils.isEmpty(subjectModels)) {
            for (SubjectResponse subjectModel : subjectModels) {
                subjectNameList.add(subjectModel.getSubjectName());
                subjectCodeList.add(subjectModel.getSubjectCode());
            }

            for (int i = 0; i < subjectCodeList.size(); i++) {
                if (subjectCodeList.get(i).equals(SubjectEnum.MATHEMATICS.getValue())) {
                    subjectCodeList.set(i, SubjectEnum.MATHEMATICS.getAlternateValue());
                }
            }
        }

        Log.d(TAG, "Subject Models: "+subjectModels.toString());
        Log.d(TAG, "Global Subject Name List: "+subjectNameList.toString());
        Log.d(TAG, "Global Subject Code List: "+subjectCodeList.toString());

    }


    private void setUpViewPager(boolean filter) {
        viewPager.setAdapter(null);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),
                testSeriesSetDtoListTemp, testId, videoUrl, subjectNameList, videoUrlKey, testTitle, testSeriesType);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(4);
        if (!filter) {
            tabLayout.removeAllTabs();
            for (int i = 0; i < viewPagerAdapter.getCount(); i++) {
                setupTab(i);
            }
            viewPager.setCurrentItem(lastSelected);
            tabLayout.selectTab(tabLayout.getTabAt(lastSelected));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTab = tab.getPosition();
                selected = true;
                //MyApplication.saveData("selectedTab"+"_"+testId,selectedTab);
                viewPager.setCurrentItem(selectedTab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (!selected) {
                    Log.d("tabTest", "onPageScrolled scolled " + position);

                    selectedTab = position;

                    for (int i = 0; i < viewPagerAdapter.getCount(); i++) {
                        //MyApplication.saveData("selectedTab"+"_"+testId,selectedTab);
                        updateTab(i);
                    }
                    tabLayout.selectTab(tabLayout.getTabAt(position));

                } else {
                    Log.d("tabTest", "onPageScrolled selected" + selectedTab);
                    tabLayout.selectTab(tabLayout.getTabAt(selectedTab));
                    for (int i = 0; i < viewPagerAdapter.getCount(); i++) {
                        updateTab(i);

                    }
                }
            }

            @Override
            public void onPageSelected(int position) {
                Log.d("tabTest", "onPageSelected " + position);

            }


            @Override
            public void onPageScrollStateChanged(int state) {
                Log.d("tabTest", "scrollstateChanged " + state);
                if (state == 0) {
                    selected = false;
                }
            }
        });
    }

    private void setupTab(int pos) {
        TabLayout.Tab tab = tabLayout.newTab();
        View customTab = LayoutInflater.from(this).inflate(R.layout.text_tab_layout, null);
        TextView tabText = customTab.findViewById(R.id.tab_text);
        tabText.setText(subjectNameList.get(pos));
        tab.setCustomView(customTab);
        tabLayout.addTab(tab, pos);
        View tabView = ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(pos);
        if (tabView != null) {
            tabView.setBackground(null);
            tabView.setForeground(null);
            tabView.setStateListAnimator(null);
        }
        updateTab(pos);
    }

    private void updateTab(int pos) {
        TabLayout.Tab tab = tabLayout.getTabAt(pos);
        if (tab != null) {
            MaterialCardView tabCard = tab.getCustomView().findViewById(R.id.tab_text_card);
            TextView tabText = tab.getCustomView().findViewById(R.id.tab_text);
            Typeface customFontUnselected = ResourcesCompat.getFont(this, R.font.nunito_regular);
            Typeface customFontSelected = ResourcesCompat.getFont(this, R.font.nunito_semibold);
            if (pos == selectedTab) {
                tabCard.setCardBackgroundColor(getResources().getColor(R.color.subscription_blue));
                tabCard.setStrokeWidth(0);
                tabText.setTextColor(getResources().getColor(R.color.white));
                tabText.setTypeface(customFontSelected);
            } else {
                tabCard.setCardBackgroundColor(getResources().getColor(R.color.white));
                tabCard.setStrokeWidth(2);
                tabCard.setStrokeColor(getResources().getColor(R.color.tab_not_selected_text_boundary));
                tabText.setTextColor(getResources().getColor(R.color.tab_not_selected_text));
                tabText.setTypeface(customFontUnselected);
            }
        }
    }


    private void setFilterLayout() {

        filterMock.setOnClickListener(v -> {
            Log.d(TAG, "Filter clicked");
            filterVisible = true;
            openDrawer();
            Map<String, String> params = new HashMap<>();
            params.put("screen", TestSeriesListActivity.class.getSimpleName());
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.filter_clicked.getActionName(), params);
        });

        apply = findViewById(R.id.Apply_Filter_Card);
        back = findViewById(R.id.back_arrow);
        attempt = findViewById(R.id.attempted);
        unattempt = findViewById(R.id.unattempted);
        clear = findViewById(R.id.clear);
        radioButton = findViewById(selectedId);
        radioGroup = findViewById(R.id.radio);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.attempted || checkedId == R.id.unattempted) {
                // If user selects any radio button, uncheck clear
                if (clear.isChecked()) {
                    clear.setChecked(false);
                }
            }
        });

        clear.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                attempt.setChecked(false);
                unattempt.setChecked(false);
                radioGroup.clearCheck();
                clear.setChecked(true);
            }
        });

//        clear.setOnClickListener(v -> {
//            boolean isChecked = clear.isChecked();
//
//            if (isChecked) {
////                clear.setChecked(true);
//                attempt.setChecked(false);
//                unattempt.setChecked(false);
//                radioGroup.clearCheck();
//            }
//
//        });

        apply.setOnClickListener(view1 -> {
            closeDrawer();
            filterStatus = "";
            filterSubject = "";
            String selectedString = "";
            String radioText = "";
            selectedId = radioGroup.getCheckedRadioButtonId();
            radioButton = findViewById(selectedId);
            if (radioButton != null)
                radioText = radioButton.getText().toString();
            if (clear.isChecked()) {
                selectedString = "";
                radioText = "";
            }

            filterStatus = radioText;
            filterSubject = selectedString;
            applyFilter();
        });
        back.setOnClickListener(view -> {
            filterVisible = false;
            closeDrawer();
        });
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

    public void applyFilter() {
        if (separated) {
            testSeriesSetDtoListTemp.clear();
            if (filterStatus.isEmpty()) {
                testSeriesSetDtoListTemp.addAll(testSeriesSetDtoList);
            } else {
                for (int i = 0; i < testSeriesSetDtoList.size(); i++) {
                    ArrayList<TestSeriesSetDto> testSeriesSetDtoArrayList = new ArrayList<>();
                    for (TestSeriesSetDto l : testSeriesSetDtoList.get(i)) {
                        String attempted = getString(R.string.Attempted);
                        String unattempted = getString(R.string.Unattempted);

                        if (filterStatus.equalsIgnoreCase(attempted) && l.isAttempted())
                            testSeriesSetDtoArrayList.add(l);
                        else if (filterStatus.equalsIgnoreCase(unattempted) && !l.isAttempted())
                            testSeriesSetDtoArrayList.add(l);
                    }
                    testSeriesSetDtoListTemp.add(i, testSeriesSetDtoArrayList);
                }
            }
            if (viewPagerAdapter != null) {
                viewPagerAdapter.updateDataList(testSeriesSetDtoListTemp);
            }
        } else {
            filteredList.clear();
            if (filterStatus.isEmpty()) {
                filteredList.addAll(list);
            } else {
                for (TestSeriesSetDto l : list) {
                    String attempted = getString(R.string.Attempted);
                    String unattempted = getString(R.string.Unattempted);

                    if (filterStatus.equalsIgnoreCase(attempted) && l.isAttempted())
                        filteredList.add(l);
                    else if (filterStatus.equalsIgnoreCase(unattempted) && !l.isAttempted())
                        filteredList.add(l);
                }
            }
            if (testSeriesListAdapter != null) {
                testSeriesListAdapter.notifyDataSetChanged();
            }
        }
    }

    private void getCarousel() {
        List<CarouselResponse> localData = MyApplication.getCarouselDataFromLocal(HomeScreenCacheData.AV_TEST_SERIES_CAROUSEL.name());
        if (!CollectionUtils.isEmpty(localData)) {
            carouselDto.clear();
            carouselDto.addAll(localData);
            setCarouselData();
            carouselShimmer.setVisibility(View.GONE);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("screenType", "TEST_SERIES");
        Integer batchId = MyApplication.getIntData(PreferenceKeys.BATCH_ID.getKey());
        if (batchId != null && batchId != 0) {
            map.put("batchId", batchId);
        }
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(AV_CAROUSEL, map), null,
                response -> {
                    TypeToken<ApiWrappedResponse<List<CarouselResponse>>> token = new TypeToken<ApiWrappedResponse<List<CarouselResponse>>>() {
                    };
                    ApiWrappedResponse<List<CarouselResponse>> apiResponse = gson.fromJson(response.toString(), token.getType());
                    List<CarouselResponse> data = (apiResponse != null && apiResponse.isSuccess()) ? apiResponse.getData() : null;
                    carouselDto.clear();
                    if (data != null) {
                        carouselDto.addAll(data);
                    }
                    if (!CollectionUtils.isEmpty(carouselDto)) {
                        MyApplication.saveCarouselData(HomeScreenCacheData.AV_TEST_SERIES_CAROUSEL.name(), data);
                        setCarouselData();
                        carouselShimmer.setVisibility(View.GONE);
                    } else {
                        imageSlider.setVisibility(View.GONE);
                    }
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                CommonMethods.showNegativeSnackbar(backButton, error.getMessage());
            }
        });
        myJsonObjectRequest2.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
    }

    private void setCarouselData() {
        imageList.clear();

        for (CarouselResponse carousel : carouselDto) {
            imageList.add(new SlideModel(carousel.getUrl(), ScaleTypes.CENTER_INSIDE));
        }

        // Set the image list to the ImageSlider
        if (!imageList.isEmpty()) {
            img_slider.setImageList(imageList);
            img_slider.setItemClickListener(new ItemClickListener() {
                @Override
                public void onItemSelected(int i) {
                    CarouselResponse dto = carouselDto.get(i);

                    if (dto.getClassName() != null) {
                        if (dto.getClassName().equalsIgnoreCase("external_link")) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(Objects.requireNonNull(dto.getActivityParams().get("link")).toString()));

                            if (intent.resolveActivity(getPackageManager()) != null) {
                                destroyHowTo();
                                startActivity(intent);
                            } else {
                                Toast.makeText(TestSeriesListActivity.this, "Unable to redirect", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            try {
                                if (StringUtils.isNotEmpty(dto.getClassName()) && !dto.getClassName().contains(TAG)) {
                                    Intent intent = new Intent(TestSeriesListActivity.this, ActivityLauncher.resolveClass(dto.getClassName()));
                                    if (dto.getActivityParams() != null) {
                                        for (String key : dto.getActivityParams().keySet()) {
                                            Object value = dto.getActivityParams().get(key);
                                            if (value instanceof String) {
                                                intent.putExtra(key, (String) dto.getActivityParams().get(key));
                                            } else if (value instanceof Integer) {
                                                intent.putExtra(key, (Integer) dto.getActivityParams().get(key));
                                            } else if (value instanceof Boolean) {
                                                intent.putExtra(key, (Boolean) dto.getActivityParams().get(key));
                                            }
                                        }
                                    }
                                    if (dto.getClassName().contains("MockDemoActivity")) {
                                        logFirebaseEvent();
                                        if (howToType != null)
                                            if (howToVideoView != null && howToVideoView.videoView != null) {
                                                howToVideoView.setType(howToType, true);
                                            } else {
                                                howToVideoView = HowToVideoView.getInstance(TestSeriesListActivity.this);
                                                howToVideoView.setView(TestSeriesListActivity.this, how_to_video_view);
                                                howToVideoView.setType(howToType, true);
                                            }
                                    } else {
                                        destroyHowTo();
                                        startActivity(intent);
                                    }
                                }
                            } catch (ClassNotFoundException e) {
                                Log.e("Error", e.toString());
                                MyApplication.openGooglePlayStore(TestSeriesListActivity.this);
                            }
                        }
                    }
                }

                @Override
                public void doubleClick(int i) {

                }

            });
        }
    }

    private void logFirebaseEvent() {
        Map<String, String> params = new HashMap<>();
        params.put("action", "test_how_to_carousel_click");
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.test_how_to_carousel_click.getActionName(), params);
    }

    private void getTestSeriesSets() {
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        tabCard.setVisibility(View.GONE);
        Map<String, Object> map = new HashMap<>();
        String userId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
        map.put(PreferenceKeys.USER_ID.getKey(), userId);
        map.put("testSeriesId", testId);
//        map.put("cbseBoardSupported", true);
        Log.d(TAG, "Test Series Request: "+map.toString());
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(TEST_SERIES_LIST, map), null,
                response -> {
            Log.d(TAG, "Test Series Response: "+response);
                    TypeToken<TestSeriesSetResulList> token = new TypeToken<TestSeriesSetResulList>() {
                    };
                    testSeriesSetResulList = gson.fromJson(response.toString(), token.getType());
//                    testSeriesSetResulList.setSeparate(false);
                    list = (ArrayList<TestSeriesSetDto>) testSeriesSetResulList.getTestSeriesList();
                    if (list.isEmpty()) {
                        CommonMethods.showNegativeSnackbar(backButton, getString(R.string.NoValidMockTestFound));
                    } else {
                        shimmerFrameLayout.setVisibility(View.GONE);
                        tabCard.setVisibility(View.VISIBLE);
                        for (TestSeriesSetDto testSeriesSetDto : list) {
                            if (testSeriesSetDto.isPremium() && testSeriesSetDto.isLock()) {
                                if (howToVideoView != null)
                                    howToVideoView.setBottomSheetVisibleForTestList(true);
                                unlock_bottom_layout.setVisibility(View.VISIBLE);
                                break;
                            }
                        }

                        subjectCodes = new ArrayList<>();
                        for (TestSeriesSetDto testSeriesSetDto : list) {
                            String subjectCode = testSeriesSetDto.getTestSeriesTypes().get(0).getSubject();
                            if (StringUtils.isNotEmpty(subjectCode) && !subjectCodes.contains(subjectCode.toLowerCase())) {
                                subjectCodes.add(subjectCode.toLowerCase());
                            }
                        }

                        Log.d(TAG, "test Series Size: Codes: " + subjectCodes.toString());
                        getSubscribedSubjects();

                        int testCount = MyApplication.getIntData(PreferenceKeys.TOTAL_TESTS_COUNT.getKey());
                        if (fromHalfYearly)
                            unlock_bottom_layout_text.setText(String.format(getString(R.string.Unlock_All_Tests), testCount + list.size()));
                        else
                            unlock_bottom_layout_text.setText(String.format(getString(R.string.Unlock_All_Tests), testCount != -1 ? testCount : list.size()));

                        if (testSeriesSetResulList.isSeparate()) {
                            separated = true;
                            tabLayout.setVisibility(View.VISIBLE);
                            viewPager.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                            divideTestSeriesIntoSubjects();
                        } else {
                            filteredList = new ArrayList<>(list);
                            tabLayout.setVisibility(View.GONE);
                            viewPager.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
                            testSeriesListAdapter = new TestSeriesListAdapter(this, filteredList, this, testId);
                            recyclerView.setAdapter(testSeriesListAdapter);
                        }
                        getScrollPosition(list);
                    }
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                CommonMethods.showNegativeSnackbar(backButton, error.getMessage());
            }
        });
        myJsonObjectRequest2.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
    }

    private void getScrollPosition(ArrayList<TestSeriesSetDto> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        String lastSelectedId = getIntent().getStringExtra("testSeriesId");
        if (StringUtils.isEmpty(lastSelectedId)) {
            lastSelectedId = MyApplication.getStringData(String.format(getString(R.string.lastTestAttempted),
                    testId,
                    MyApplication.getStringData(PreferenceKeys.CLASS_ID.getKey()),
                    MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey()),
                    MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey())));
        }
        if (StringUtils.isNotEmpty(lastSelectedId)) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getTestSeriesId().equals(lastSelectedId)) {
                    scrollTo(i);
                    break;
                }
            }
        }
    }

    private void scrollTo(int scrollPosition) {
        try {
            if (testSeriesSetResulList.isSeparate()) {
                for (int i = 0; i < subjectNameList.size(); i++) {
                    if (!CollectionUtils.isEmpty(testSeriesSetResulList.getTestSeriesList()) &&
                            testSeriesSetResulList.getTestSeriesList().get(scrollPosition).getTestSeriesTypes().get(0).getSubject().
                                    equalsIgnoreCase(subjectCodeList.get(i))) {
                        lastSelected = i;
                        viewPager.setCurrentItem(lastSelected);
                        tabLayout.selectTab(tabLayout.getTabAt(lastSelected));
                        break;
                    }
                }
            } else {
                handleRVScroll(scrollPosition);
            }
        } catch (Exception e) {

        }
    }

    private void handleRVScroll(int position) {
        try {
            new Handler().postDelayed(() -> {
                recyclerView.smoothScrollToPosition(position);
            }, 500);
        } catch (Exception e) {

        }
    }


    private void divideTestSeriesIntoSubjects() {
        separateSubject(null);
        setUpViewPager(false);
    }

    private void searchTest(String query) {
        if (filteredList == null) {
            return;
        }
        filteredList.clear();
        String trimmedQuery = query.trim().toLowerCase();
        Log.d("TAG", "----> Trimmed Pattern: " + trimmedQuery);
        for (int j = 0; j < list.size(); j++) {
            try {
                TestSeriesSetDto testSeriesSetDto = list.get(j);
                String testSeriesName = CommonMethods.normalizeSpaces(testSeriesSetDto.getTestSeriesName().trim());
                Log.d("TAG", "----> TestSeriesName: " + testSeriesName);
                if (testSeriesName != null &&
                        testSeriesName.toLowerCase().contains(trimmedQuery.trim())) {
                    filteredList.add(testSeriesSetDto);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void separateSubject(String query) {
        if (testSeriesSetDtoList == null || testSeriesSetDtoListTemp == null) {
            return;
        }

        testSeriesSetDtoList.clear();
        testSeriesSetDtoListTemp.clear();
        Map<String, ArrayList<TestSeriesSetDto>> map = new HashMap<>();
        String trimmedQuery = StringUtils.isNotEmpty(query) ? CommonMethods.normalizeSpaces(query.trim().toLowerCase()) : "";
        Log.d(TAG, "Filter Query: " + trimmedQuery);
        for (int j = 0; j < list.size(); j++) {
            try {
                TestSeriesSetDto testSeriesSetDto = list.get(j);
                if (testSeriesSetDto != null) {
                    Log.d("TestListData", testSeriesSetDto.toString());
                    String subject = testSeriesSetDto.getTestSeriesTypes().get(0).getSubject();
                    String testSeriesName = testSeriesSetDto.getTestSeriesName() != null ? CommonMethods.normalizeSpaces(testSeriesSetDto.getTestSeriesName().trim()) : "";

                    if (testSeriesName.toLowerCase().contains(trimmedQuery)) {

                        if (map.containsKey(subject.toUpperCase())) {
                            ArrayList<TestSeriesSetDto> testSeriesSetDtos = map.get(subject.toUpperCase());
                            testSeriesSetDtos.add(testSeriesSetDto);
                            map.put(subject.toUpperCase(), testSeriesSetDtos);
                        } else {
                            ArrayList<TestSeriesSetDto> testSeriesSetDtos = new ArrayList<>();
                            testSeriesSetDtos.add(testSeriesSetDto);
                            map.put(subject.toUpperCase(), testSeriesSetDtos);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        for (int i = 0; i < subjectCodeList.size(); i++) {
            ArrayList<TestSeriesSetDto> subjectDto = new ArrayList<>();

            String code = subjectCodeList.get(i);

            if (map.containsKey(code.toUpperCase())) {
                subjectDto.addAll(map.get(code.toUpperCase()));
            }

            Log.d("TotalSize", "subject code = " + code + " size = " + subjectDto.size());


            testSeriesSetDtoList.add(subjectDto);
            testSeriesSetDtoListTemp.add(subjectDto);

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkTestEvaluation();

        if (MyApplication.getBoolData(PreferenceKeys.RELOAD_TEST_LIST_DATA.getKey())) {
            MyApplication.saveData(PreferenceKeys.RELOAD_TEST_LIST_DATA.getKey(), false);
            getTestSeriesSets();
            pauseClicked = false;
        } else if (testSeriesSetResulList != null) {
            if (testSeriesSetResulList.isSeparate()) {
                if (viewPagerAdapter != null)
                    viewPagerAdapter.notifyDataSetChanged();
            } else {
                if (testSeriesListAdapter != null)
                    testSeriesListAdapter.notifyDataSetChanged();
                getScrollPosition(list);
            }
        }

        try {
            if (howToVideoView != null && howToVideoView.videoView != null) {
                howToVideoView.resumePlayer();
            }
        } catch (Exception e) {
            Log.e("TestSeriesListActivity", "Error resuming video player", e);
        }

    }

    private void checkTestEvaluation() {
        MyApplication.getTestListEvaluationDatabaseReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    getTestSeriesSets();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error fetching data: " + databaseError.getMessage());
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        lastSelected = selectedTab;
        pauseClicked = true;
        if (howToVideoView != null && howToVideoView.videoView != null)
            howToVideoView.pausePlayer();
    }

    private void getHowToVideoType(String testId) {
        Log.d(TAG, "getHowToVideoType: " + MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey()));
        try {
            if (testId == null || testId.isEmpty()) {
                return;
            }
            if (MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey())) {
                if (testId.toLowerCase().contains("chapter"))
                    howToType = TEST_BOARD_CHAPETR;
                else
                    howToType = TEST_BOARD;
            } else {
                if (testId.toLowerCase().contains("chapter")) {
                    if (("1").equals(MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey())))
                        howToType = TEST_JEE_CHAPETR;
                    else if (("3").equals(MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey())))
                        howToType = TEST_NEET_CHAPETR;
                } else {
                    if (("1").equals(MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey())))
                        howToType = TEST_JEE;
                    else if (("3").equals(MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey())))
                        howToType = TEST_NEET;
                }
            }

        } catch (Exception e) {
            howToType = null;
            Log.e("VideoUrlError", "Error getting video URL", e);
        }

        if (howToType != null) {
            howToVideoView = HowToVideoView.getInstance(TestSeriesListActivity.this);
            howToVideoView.setView(TestSeriesListActivity.this, how_to_video_view);
        }

        if (howToType != null && howToVideoView != null) {
            videoUrl = howToVideoView.getUrlBasedOnType(howToType);
            int count = MyApplication.getIntData(howToType.name() + "_COUNT" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
            Log.d("debugcount", "getHowToVideoType: " + count + "type" + howToType.name());
            if (count < 0)
                count = 0;
            if (count == 0) {
                howToVideoView.setType(howToType, false);
                MyApplication.saveData(howToType.name() + "_COUNT" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()), 1);
            }
        }
    }

    @Override
    public void updateWatchCount() {
        updateCount();
    }

    private void updateCount() {
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(UPDATE_MOCK_VIDEO_WATCH_COUNT, null), null,
                response -> {
                    TypeToken<UserDto> token = new TypeToken<UserDto>() {
                    };
                    UserDto userDto = gson.fromJson(response.toString(), token.getType());
                    MyApplication.saveData(PreferenceKeys.SHOW_MOCK_VIDEO.getKey(), userDto.isShowMockVideo());

                }, error -> {
            Log.d("updateCount: ", "" + error.getMessage());
        });
        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
    }

    @Override
    public void onLockedMockTestClicked() {

        try {
//            Intent intent = new Intent(this, CampaignSubscriptionActivity.class);

            Intent intent = SubscriptionNavigationHelper.getSubscriptionPageIntent(this, RedirectionSource.TEST_SERIES_JOIN);

            SubscriptionLockPopupDto subscriptionLockPopupDto =
                    CampaignSubscriptionActivity.getSubscriptionLockPopupDto("test_series_lock_"+MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));

            if (subscriptionLockPopupDto != null) {
                if(subscriptionLockPopupDto.getSubTitle()!=null && testTitle!=null) {
                    subscriptionLockPopupDto.setSubTitle(subscriptionLockPopupDto.getSubTitle().replace("{0}", testTitle));
                } else if(subscriptionLockPopupDto.getSubTitle()!=null) {
                    subscriptionLockPopupDto.setSubTitle(subscriptionLockPopupDto.getSubTitle().replace("{0}", ""));
                }
                intent.putExtra(CampaignSubscriptionActivity.BUNDLE_SUBSCRIPTION_DATA, subscriptionLockPopupDto);
            }
            startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Log.d(TAG, "onLockedMockTestClicked: ");
//        dialog.show();
    }

    @Override
    public void onAvailabilityStatusClicked(TestSeriesSetDto testSeriesSetDto) {
        MyApplication.saveData(String.format(getString(R.string.lastTestAttempted),
                        testId,
                        MyApplication.getStringData(PreferenceKeys.CLASS_ID.getKey()),
                        MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey()),
                        MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey())),
                testSeriesSetDto.getTestSeriesId());
        Log.d(TAG, "Test-Series-Type: Analysis: " + ((testSeriesType != null) ? testSeriesType : ""));
        if (testSeriesSetDto.isAttempted()) {
            if (testSeriesSetDto.isNewTest()) {
                checkAndDeleteEvaluationEntry(testSeriesSetDto.getTestSeriesId());
                Intent intent = new Intent(this, MockTestAnalysisActivity.class);
                intent.putExtra("testSeriesId", testSeriesSetDto.getTestSeriesId());
                intent.putExtra(PreferenceKeys.BOARD.getKey(), testSeriesSetDto.isBoard());
                intent.putExtra("combined", true);
                intent.putExtra("Id", testSeriesSetDto.getId());
                intent.putExtra("testSeriesType",
                        testSeriesSetDto.isAdaptiveTestSeries() ? TestSeriesType.adaptive.name() : "");
                intent.putExtra("testSeriesListId", testId);
                intent.putExtra("testSeriesName", testSeriesSetDto.getTestSeriesName());
                intent.putExtra("testSeriesType", testSeriesType);
                MyApplication.saveTestSeriesQuestions("questions", null);
                destroyHowTo();
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, ActivityTestIntro.class);
                intent.putExtra("testId", testSeriesSetDto.getTestSeriesId());
                intent.putExtra("Id", testId);
                intent.putExtra("combined", true);
                intent.putExtra(PreferenceKeys.TYPE.getKey(), PreferenceKeys.RESPONSE.getKey());
                intent.putExtra("startTest", true);
                intent.putExtra("testTitle", testTitle);
                destroyHowTo();

                if (StringUtils.isNotEmpty(testSeriesType) && testSeriesType.equalsIgnoreCase(TestSeriesType.adaptive.name())) {
                    Map<String, Object> params = CommonMethods.getIntentParams(intent);
                    String url = CommonMethods.getPersonalizedPathUrl() + AdaptivePath.TEST_SERIES_INSTRUCTIONS.path();
                    CommonNavigator.navigateToWebViewByUrlParams(this,
                            url,
                            params,
                            true, true,
                            false, false,
                            false, false,
                            getString(R.string.not_allowed_to_back), true, true, false, false, null,TAG,null);
                } else {
                    startActivity(intent);
                }
//                startActivity(intent);
            }
        }
    }

    private void checkAndDeleteEvaluationEntry(String testSeriesId) {
        MyApplication.getTestStatusDatabaseReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String testId = snapshot.child("testId").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);
                    if (testSeriesId.equals(testId) && TestEvaluationStatusEnum.REVIEW_PENDING.name().equals(status))
                        MyApplication.getTestStatusDatabaseReference().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        MyApplication.getTestListEvaluationDatabaseReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String testId = snapshot.child("testId").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);
                    if (testSeriesId.equals(testId) && TestEvaluationStatusEnum.REVIEW_PENDING.name().equals(status))
                        MyApplication.getTestListEvaluationDatabaseReference().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onItemClicked(TestSeriesSetDto testSeriesSetDto, int position) {
        if (testSeriesSetDto.isAdaptiveTestSeries()) {
            testSeriesType = TestSeriesType.adaptive.name();
        }
        Log.d("test_clicked", "onItemClicked activity: " + testSeriesSetDto);
        MyApplication.saveData(String.format(getString(R.string.lastTestAttempted),
                        testId,
                        MyApplication.getStringData(PreferenceKeys.CLASS_ID.getKey()),
                        MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey()),
                        MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey())),
                testSeriesSetDto.getTestSeriesId());

        Intent intent = new Intent(this, ActivityTestIntro.class);
        intent.putExtra("Id", testId);
        intent.putExtra("testId", testSeriesSetDto.getTestSeriesId());
        intent.putExtra("subject", "overall");
        intent.putExtra("combined", true);
        intent.putExtra("position", position);
        intent.putExtra("startTest", true);
        if (testSeriesSetDto.isAttempted()) {
            intent.putExtra(PreferenceKeys.TYPE.getKey(), "REATTEMPT");
        } else {
            MyApplication.saveData(PreferenceKeys.TYPE.getKey(), "ATTEMPT");
            intent.putExtra(PreferenceKeys.TYPE.getKey(), "ATTEMPT");
        }
        intent.putExtra("testSeriesListId", testId);
        intent.putExtra("testTitle", testTitle);
        destroyHowTo();

        Map<String, Object> params = CommonMethods.getIntentParams(intent);
        params.put("reviewMode", false);
        Log.d(TAG, "onItemClicked: " + params);
//        testSeriesType="adaptive";
        Log.d(TAG, "Test-Series-Type: Test: " + ((testSeriesType != null) ? testSeriesType : ""));
        if (StringUtils.isNotEmpty(testSeriesType) && testSeriesType.equalsIgnoreCase(TestSeriesType.adaptive.name())) {
            String url = CommonMethods.getPersonalizedPathUrl() + AdaptivePath.TEST_SERIES_INSTRUCTIONS.path();
            CommonNavigator.navigateToWebViewByUrlParams(this,
                    url,
                    params,
                    true, true,
                    false, false,
                    false, false,
                    getString(R.string.not_allowed_to_back), true, true, false,
                    false, null,TAG,null);
        } else {
            startActivity(intent);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void setDialog() {
        try {
            Log.d("bhsbhjsb", "setDialog: ");
            dialog = new Dialog(this);
            dialog.setContentView(R.layout.custom_dialog_lock_box);
            dialog.getWindow().setBackgroundDrawable(this
                    .getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.setCancelable(false);

            lockDialogText = dialog.findViewById(R.id.header);
            lockDialogBtnText = dialog.findViewById(R.id.subs);
            lockDialogTitle = dialog.findViewById(R.id.dialog_title);
            lockAnimation = dialog.findViewById(R.id.dialog_animation);

            card = dialog.findViewById(R.id.subscriptionLayout);
            card.setOnClickListener(view -> {
                dialog.dismiss();
                Intent intent = getTargetIntent(null);

                if (activityParamsMap != null) {
                    for (Map.Entry<String, Object> entry : activityParamsMap.entrySet()) {
                        String k = entry.getKey();
                        Object v = entry.getValue();

                        if (v instanceof Serializable) {
                            intent.putExtra(k, (Serializable) v);
                        }
                    }
                }

//                if (getIntent().getBooleanExtra("fromHalfYearly", false)) {
//                    intent = new Intent(this, CampaignSubscriptionActivity.class);
//                } else {
//                    intent = new Intent(this, CampaignSubscriptionActivity.class);
////                    intent.putExtra("plan_level", "COURSE");
//                }
                intent = SubscriptionNavigationHelper.getSubscriptionPageIntent(this, RedirectionSource.TEST_SERIES_JOIN);

                startActivity(intent);
            });

            dialog.findViewById(R.id.cancel).setOnClickListener(v -> dialog.dismiss());
        } catch (Exception error) {
            Log.e("Error", "setDialog: ", error);
        }
    }

    private void updateDialogContent(SubscriptionCheckDto dto) {

        if (dto != null) {
            if (dto.getDialogText() != null) lockDialogText.setText(dto.getDialogText());
            if (dto.getButtonText() != null) lockDialogBtnText.setText(dto.getButtonText());
            activityParamsMap = dto.getActivityParams();
            if (StringUtils.isNotEmpty(dto.getDialogTitle())) {
                lockDialogTitle.setVisibility(View.VISIBLE);
                lockDialogTitle.setText(dto.getDialogTitle());
            }
        }
        if (activityParamsMap != null && activityParamsMap.containsKey("navigate_to")) {
            Object navigateToValue = activityParamsMap.get("navigate_to");
            if ("home_screen".equalsIgnoreCase(String.valueOf(navigateToValue))) {
                lockAnimation.setAnimation(R.raw.alert);
            }
        }
        card.setOnClickListener(view -> handleSubscriptionClick(dto));
    }

    private void handleSubscriptionClick(SubscriptionCheckDto dto) {
        closeDialog();
        Intent intent = null;

        if (dto != null && dto.getClassName() != null && !dto.getClassName().isEmpty()) {
            try {
                intent = new Intent(this, ActivityLauncher.resolveClass(dto.getClassName()));
            } catch (ClassNotFoundException e) {
                Log.e("SubscriptionClick", "Class not found: " + dto.getClassName(), e);
            }
        }

        if (intent == null) {
//            intent = new Intent(this, CampaignSubscriptionActivity.class);
            intent = SubscriptionNavigationHelper.getSubscriptionPageIntent(this, RedirectionSource.TEST_SERIES_JOIN);
//                    .putExtra("plan_level", "COURSE")
//                    .putExtra("fromUnlockText", true);
        }

        if (activityParamsMap != null) {
            Intent finalIntent = intent;
            activityParamsMap.forEach((key, value) -> {
                if (value instanceof Serializable) {
                    finalIntent.putExtra(key, (Serializable) value);
                }
            });
        }
        if (activityParamsMap != null && activityParamsMap.containsKey("navigate_to")) {
            Object navigateToValue = activityParamsMap.get("navigate_to");
            if ("home_screen".equalsIgnoreCase(String.valueOf(navigateToValue))) {
                MyApplication.saveData(PreferenceKeys.NAVIGATE_TO_HOME_SCREEN.getKey(), true);
            }
        }
        startActivity(intent);
    }

    private void closeDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private Intent getTargetIntent(SubscriptionCheckDto dto) {
        if (dto != null && dto.getClassName() != null && !dto.getClassName().isEmpty()) {
            try {
                return new Intent(this, ActivityLauncher.resolveClass(dto.getClassName()));
            } catch (ClassNotFoundException e) {
                Log.e("LockDialog", "Class not found: " + dto.getClassName(), e);
            }
        }
//        return new Intent(this, CampaignSubscriptionActivity.class);
        return SubscriptionNavigationHelper.getSubscriptionPageIntent(this, RedirectionSource.TEST_SERIES_JOIN);
//                .putExtra("plan_level", "COURSE");
    }

    static class ViewPagerAdapter extends FragmentStatePagerAdapter {
        ArrayList<List<TestSeriesSetDto>> list;
        ArrayList<String> subjects;
        String testId;
        String videoUrl;
        String videoUrlKey;
        String testTitle;
        String testSeriesType;

        public ViewPagerAdapter(@NonNull FragmentManager fm,
                                ArrayList<List<TestSeriesSetDto>> testSeriesSetDtoListTemp, String testId,
                                String videoUrl, ArrayList<String> subjectNameList, String videoUrlKey, String testTitle, String testSeriesType) {
            super(fm);
            this.list = testSeriesSetDtoListTemp;
            this.testId = testId;
            this.videoUrlKey = videoUrlKey;
            this.videoUrl = videoUrl;
            this.subjects = subjectNameList;
            this.testTitle = testTitle;
            this.testSeriesType = testSeriesType;
        }


        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        public void updateDataList(ArrayList<List<TestSeriesSetDto>> newDataList) {
            this.list = newDataList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return new TestSeriesListFragment(testId, videoUrl, list.get(position), subjects.get(position), videoUrlKey, testTitle, testSeriesType);
        }

        @Override
        public int getCount() {
            return list.size();
        }

    }


    @Override
    public void reloadData() {
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();
        getTestSeriesSets();
    }

    @Override
    protected void onDestroy() {
        MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll(TAG);
        if (homeScreenViewModel != null) {
            homeScreenViewModel.getSubjectList().removeObservers(this);
        }

        super.onDestroy();
    }

    private void destroyHowTo() {
        how_to_video_view.setVisibility(View.GONE);
        if (howToVideoView != null)
            howToVideoView.remove();
    }

}
