package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_CAROUSEL;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_TOPPER_NOTES;
import static arivihan.technologies.doubtbuzzter2.enums.HOW_TO_TYPE.TOPPERS_NOTES;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.interfaces.ItemClickListener;
import com.denzcoskun.imageslider.models.SlideModel;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.helper.ActivityLauncher;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.ToppersNotesAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.PlayStoreDialog;
import arivihan.technologies.doubtbuzzter2.customView.HowToVideoView;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.enums.SubscriptionPopupType;
import arivihan.technologies.doubtbuzzter2.enums.batch.notes.BatchNotesScreenType;
import arivihan.technologies.doubtbuzzter2.fragments.ToppersNotesFragment;
import arivihan.technologies.doubtbuzzter2.helper.ScreenAnalyticsHelper;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.model.carousel.CarouselResponse;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.model.HomeScreenCacheData;
import arivihan.technologies.doubtbuzzter2.model.subject.SubjectResponse;
import arivihan.technologies.doubtbuzzter2.model.batch.note.NotesResponse;
import arivihan.technologies.doubtbuzzter2.model.batch.note.ToppersNotesModel;
import arivihan.technologies.doubtbuzzter2.model.offline.notes.NoteEntity;
import arivihan.technologies.doubtbuzzter2.utils.PdfUtils;
import arivihan.technologies.doubtbuzzter2.viewModel.NotesViewModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class ToppersNotesActivity extends BaseActivity implements ToppersNotesAdapter.OnNotesClickListener {

    public static final String TAG = ToppersNotesActivity.class.getSimpleName();
    private ConstraintLayout cl_parent;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private ArrayList<ArrayList<ToppersNotesModel>> list = new ArrayList<>();
    private ArrayList<ArrayList<ToppersNotesModel>> listTemp = new ArrayList<>();
    private ArrayList<ToppersNotesModel> notesListMaster = new ArrayList<>();
    private ArrayList<ToppersNotesModel> notesList = new ArrayList<>();
    private int selectedTab = 0;
    private ArrayList<String> subjectNameList = new ArrayList<>();
    private ArrayList<String> subjectCodeList = new ArrayList<>();
    private SearchView searchView;
    private TextView title;
    private boolean hasSearchText;
    private ImageSlider img_slider;
    private MaterialCardView imgSliderCard, myDownloadsCard;
    private List<CarouselResponse> carouselDto = new ArrayList<>();
    private ArrayList<SlideModel> imageList = new ArrayList<>();
    private Gson gson;
    private ShimmerFrameLayout carouselShimmer;
    private ShimmerFrameLayout shimmerFrameLayout;
    private CardView tabCard;
    Dialog dialog;
    MaterialCardView subscriptionLayout;
    private boolean selected, preExamPreparation;
    private LinearLayout backButton;
    boolean overall;
    String categoryId;
    RecyclerView recyclerView;
    ToppersNotesAdapter notesAdapter;
    private Dialog lockDialog;
    private String lastClickedNotesType;
    PlayStoreDialog playStoreDialog;
    LinearLayout carouselLayout;
    private RelativeLayout rl_tab_layout;
    private CardView cv_tab_left_scroll, cv_tab_right_scroll;
    NotesViewModel notesViewModel;
    private HowToVideoView howToVideoView;
    private View how_to_video_view;
    String screenTitle = "";
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private boolean dataLoaded = false, isNcertSolutions;
    private TabLayoutMediator tabLayoutMediator;

    @SuppressLint({"UseCompatLoadingForDrawables", "CutPasteId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toppers_notes);
        cl_parent = findViewById(R.id.cl_parent);

        try {
//            boolean isBoard = MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey());
            List<SubjectResponse> subjectList =
                    MyApplication.getSubjectModelList(HomeScreenCacheData.AV_SUBJECTS_V2.name());

            subjectNameList = new ArrayList<>();
            subjectCodeList = new ArrayList<>();

            for (SubjectResponse model : subjectList) {
                subjectNameList.add(model.subjectName);
                subjectCodeList.add(model.subjectName);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        how_to_video_view = findViewById(R.id.how_to_video_view);
        notesViewModel = MyApplication.getInstance().getNotesViewModel();
        tabLayout = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.viewpager);
        title = findViewById(R.id.title);
        screenTitle = getIntent().getStringExtra("screenTitle");
        if (screenTitle != null && !screenTitle.isEmpty()) {
            title.setText(screenTitle);
        }
        img_slider = findViewById(R.id.image_sliders);
        carouselLayout = findViewById(R.id.carousel_layout);
        imgSliderCard = findViewById(R.id.image_slider);
        carouselShimmer = findViewById(R.id.carousel_shimmer);
        carouselShimmer.setVisibility(View.VISIBLE);
        carouselShimmer.startShimmer();
        shimmerFrameLayout = findViewById(R.id.list_shimmer);
        shimmerFrameLayout.startShimmer();
        tabCard = findViewById(R.id.tab_card);
        backButton = findViewById(R.id.back_button_layout);
        backButton.setOnClickListener(v -> onBackPressedActivity());
        myDownloadsCard = findViewById(R.id.cv_my_download);
        rl_tab_layout = findViewById(R.id.rl_tab_layout);
        cv_tab_left_scroll = findViewById(R.id.cv_tab_left_scroll);
        cv_tab_right_scroll = findViewById(R.id.cv_tab_right_scroll);


        myDownloadsCard.setOnClickListener(V -> {
            Intent intent = new Intent(this, MyDownloadActivity.class);
            intent.putExtra(MyDownloadActivity.bundleIsNoteSelected, true);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.notes_my_download_clicked.getActionName());
            startActivity(intent);
        });
        Uri data = getIntent().getData();

        if (data != null) {
            // Deep link parameters
            categoryId = data.getQueryParameter("categoryId");
            isNcertSolutions = data.getBooleanQueryParameter("ncertSolutions", false);
            // Booleans: default to false if not present
            overall = data.getBooleanQueryParameter("overall", false);
            preExamPreparation = data.getBooleanQueryParameter("preExamPreparation", false);
        } else {
            isNcertSolutions = getIntent().getBooleanExtra("ncertSolutions", false);
            categoryId = getIntent().getStringExtra("categoryId");
            overall = getIntent().getBooleanExtra("overall", false);
            preExamPreparation = getIntent().getBooleanExtra("preExamPreparation", false);
        }
        registerNetworkListener();

        if (isNcertSolutions && categoryId != null) {
            categoryId = BatchNotesScreenType.NCERT_SOLUTION.name();
        }
        Map<String, String> params = new HashMap<>();
        params.put("categoryId", categoryId);
//        params.put("ncertSolutions", ""+isNcertSolutions);
        params.put("overall", "" + overall);
//        params.put("preExamPreparation", ""+preExamPreparation);
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.note_toppers_page_opened.getActionName(), params);

        Log.d(TAG, "----> ToppersNotesActivity: " + categoryId + ", " + overall);

        if (savedInstanceState == null) {
            ScreenAnalyticsHelper.sendScreenVisitedEvent(categoryId != null ? categoryId : BatchNotesScreenType.TOPPER_NOTES.name(), "SCREEN");
        }

        if (categoryId != null && categoryId.equalsIgnoreCase(BatchNotesScreenType.TOPPER_NOTES.name()) && MyApplication.isNetworkAvailable()) {
            int count = MyApplication.getIntData(TOPPERS_NOTES.name() + "_COUNT" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
            Log.d("debugcount", "getHowToVideoType: "
                    + count + " type: " + TOPPERS_NOTES.name());
            if (count < 0) count = 0;
            if (count <= 1) {
                howToVideoView = HowToVideoView.getInstance(this);

                if (howToVideoView.videoView == null || howToVideoView.videoView != how_to_video_view) {
                    howToVideoView.setView(this, how_to_video_view);
                }

                if (howToVideoView.videoView.getVisibility() != View.VISIBLE) {
                    howToVideoView.setType(TOPPERS_NOTES, false);
                    howToVideoView.setButtonText(getString(R.string.start_now));

                    // Added events for the short videos for the toppers notes screen.
                    howToVideoView.setRemoveListener(new HowToVideoView.RemoveListener() {
                        @Override
                        public void onClose() {
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.toppers_notes_how_to_close_clicked.getActionName());
                        }

                        @Override
                        public void onExpand() {
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.toppers_notes_how_to_expand_clicked.getActionName());
                        }

                        @Override
                        public void onVolumeToggle(boolean muted) {
                            Map<String, String> params = new HashMap<>();
                            params.put("isMuted", String.valueOf(muted));
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.toppers_notes_how_to_volume_clicked.getActionName(), params);
                        }

                        @Override
                        public void onButtonClick() {
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.toppers_notes_how_to_start_now_clicked.getActionName());
                        }
                    });

                    MyApplication.saveData(TOPPERS_NOTES.name() + "_COUNT" +
                            MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()), count + 1);
                }
            }
        }

        gson = new Gson();
//        setUpSearchView();
        getCarousel();

        // Prevent ViewPager2 from saving/restoring fragment state.
        viewPager.setSaveEnabled(false);

        cv_tab_left_scroll.setOnClickListener(v -> tabLayout.smoothScrollBy(-tabLayout.getWidth(), 0));
        cv_tab_right_scroll.setOnClickListener(v -> tabLayout.smoothScrollBy(tabLayout.getWidth(), 0));

        tabLayout.getViewTreeObserver().addOnScrollChangedListener(() -> {
            updateTabArrowsVisibility();
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBackPressedActivity();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

    }

    private void onBackPressedActivity() {
        if (howToVideoView != null && howToVideoView.videoView != null
                && howToVideoView.videoView.getVisibility() == View.VISIBLE && howToVideoView.expanded) {
            howToVideoView.resize();
            return;
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        destroyHowTo();
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

    private void registerNetworkListener() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        networkCallback = new ConnectivityManager.NetworkCallback() {

            @Override
            public void onAvailable(@NonNull Network network) {
                if (!dataLoaded) {
                    runOnUiThread(() -> {
                        Log.d(TAG, "Network restored — retrying API calls");
                        shimmerFrameLayout.setVisibility(View.VISIBLE);
                        shimmerFrameLayout.startShimmer();
                        getTopperNotes();
                    });
                }
            }
        };

        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        connectivityManager.registerNetworkCallback(request, networkCallback);
    }

    private void unregisterNetworkListener() {
        try {
            if (connectivityManager != null && networkCallback != null) {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

//    private void getHalfYearlyCarousel() {
//        List<CarouselResponse> localData = MyApplication.getCarouselDataFromLocal(HomeScreenCacheData.AV_HALF_YEARLY_CAROUSEL.name());
//        if (!CollectionUtils.isEmpty(localData)) {
//            carouselDto.clear();
//            carouselDto.addAll(localData);
//            setCarouselData();
//            carouselShimmer.setVisibility(View.GONE);
//        }
//        Map<String, Object> map = new HashMap<>();
//        map.put("screenType", "PRE_EXAM");
//        Integer batchId = MyApplication.getIntData(PreferenceKeys.BATCH_ID.getKey());
//        if (batchId != null && batchId != 0) {
//            map.put("batchId", batchId);
//        }
//        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
//                CommonMethods.getUrlParam(AV_CAROUSEL, map), null,
//                response -> {
//                    TypeToken<ApiWrappedResponse<List<CarouselResponse>>> token = new TypeToken<ApiWrappedResponse<List<CarouselResponse>>>() {
//                    };
//                    ApiWrappedResponse<List<CarouselResponse>> apiResponse = gson.fromJson(response.toString(), token.getType());
//                    List<CarouselResponse> data = (apiResponse != null && apiResponse.isSuccess()) ? apiResponse.getData() : null;
//                    carouselDto.clear();
//                    if (data != null) {
//                        carouselDto.addAll(data);
//                    }
//                    if (!CollectionUtils.isEmpty(carouselDto)) {
//                        MyApplication.saveCarouselData(HomeScreenCacheData.AV_HALF_YEARLY_CAROUSEL.name(), data);
//                        setCarouselData();
//                        carouselShimmer.setVisibility(View.GONE);
//                        imgSliderCard.setVisibility(View.VISIBLE);
//                    } else {
//                        carouselShimmer.setVisibility(View.GONE);
//                        imgSliderCard.setVisibility(View.GONE);
//                    }
//                }, new VolleyGlobalErrorListener(this) {
//            @Override
//            public void postError(VolleyError error) {
//                carouselShimmer.setVisibility(View.GONE);
//                imgSliderCard.setVisibility(View.GONE);
//                carouselLayout.setVisibility(View.GONE);
//                CommonMethods.showNegativeSnackbar(tabLayout, error.getMessage(), 3);
//            }
//        });
//        myJsonObjectRequest2.setTag(TAG);
//        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
//    }

    private void getCarousel() {
        if (!MyApplication.isNetworkAvailable()) {
            CommonMethods.showNegativeSnackbar(cl_parent, getString(R.string.slow_internet_error), 5000);
        }
        List<CarouselResponse> localData = MyApplication.getCarouselDataFromLocal(HomeScreenCacheData.AV_TOPPER_NOTES_CAROUSEL.name());
        if (!CollectionUtils.isEmpty(localData)) {
            carouselDto.clear();
            carouselDto.addAll(localData);
            setCarouselData();
            carouselShimmer.setVisibility(View.GONE);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("screenType", categoryId != null ? categoryId : BatchNotesScreenType.TOPPER_NOTES.name());
        Log.d(TAG, "getCarousel: " + map.toString());
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(AV_CAROUSEL, map), null,
                response -> {
                    if (isFinishing() || isDestroyed()) return;
                    TypeToken<ApiWrappedResponse<List<CarouselResponse>>> token = new TypeToken<ApiWrappedResponse<List<CarouselResponse>>>() {
                    };
                    ApiWrappedResponse<List<CarouselResponse>> apiResponse = gson.fromJson(response.toString(), token.getType());
                    List<CarouselResponse> data = (apiResponse != null && apiResponse.isSuccess()) ? apiResponse.getData() : null;
                    carouselDto.clear();
                    if (data != null) {
                        carouselDto.addAll(data);
                    }
                    if (!CollectionUtils.isEmpty(carouselDto)) {
                        MyApplication.saveCarouselData(HomeScreenCacheData.AV_TOPPER_NOTES_CAROUSEL.name(), data);
                        setCarouselData();
                        carouselShimmer.setVisibility(View.GONE);
                    } else {
                        carouselLayout.setVisibility(View.GONE);
                    }
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                carouselShimmer.setVisibility(View.GONE);
                carouselLayout.setVisibility(View.GONE);
                CommonMethods.showNegativeSnackbar(tabLayout, error.getMessage(), 3);
            }
        });
        myJsonObjectRequest2.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
    }

    private void setCarouselData() {
        img_slider.setVisibility(View.VISIBLE);
        imageList.clear();
        for (CarouselResponse carousel : carouselDto) {
            imageList.add(new SlideModel(carousel.getUrl(), ScaleTypes.CENTER_INSIDE));
        }
        img_slider.setImageList(imageList);
        img_slider.setItemClickListener(new ItemClickListener() {
            @Override
            public void onItemSelected(int i) {
                try {

                    CarouselResponse dto = carouselDto.get(i);

                    if (dto.getClassName() != null) {
                        if (dto.getClassName().equalsIgnoreCase("external_link")) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(Objects.requireNonNull(dto.getActivityParams().get("link")).toString()));

                            if (intent.resolveActivity(getPackageManager()) != null) {
                                startActivity(intent);
                            } else {
                                Toast.makeText(ToppersNotesActivity.this, "Unable to redirect", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            try {
                                if (StringUtils.isNotEmpty(dto.getClassName()) && !dto.getClassName().contains(TAG)) {
                                    Intent intent = new Intent(ToppersNotesActivity.this, ActivityLauncher.resolveClass(dto.getClassName()));
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
                                    startActivity(intent);
                                }

                            } catch (ClassNotFoundException e) {
                                Log.e("Error", e.toString());
                                MyApplication.openGooglePlayStore(ToppersNotesActivity.this);
                            }
                        }
                    }
                } catch (Exception e) {
                }
            }

            @Override
            public void doubleClick(int i) {
            }
        });
    }

    private void setUpSearchView() {
        searchView = findViewById(R.id.search_view);
        searchView.setQueryHint(getString(R.string.search));

        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasSearchText) {
                hasSearchText = false;
                searchView.setQuery("", true);
            } else if (!hasFocus) {
                searchView.setIconified(true);
            }
        });

        ImageView searchButton = searchView.findViewById(R.id.search_button);
        searchButton.setOnClickListener(v -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.search_view_clicked_notes.getActionName());
            searchView.setIconified(false);
            searchView.setMaxWidth(Integer.MAX_VALUE);
            title.setVisibility(View.GONE);
        });
        searchView.setOnCloseListener(() -> {
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
                hasSearchText = !s.isEmpty();

                // Global search implementation
                if (overall) {
                    // For overall view (RecyclerView)
                    if (notesListMaster != null) {
                        notesList.clear();
                        for (ToppersNotesModel toppersNotesModel : notesListMaster) {
                            if (toppersNotesModel.getChapterTitle().toLowerCase().contains(s.toLowerCase().trim())) {
                                notesList.add(toppersNotesModel);
                            }
                        }
                        if (notesAdapter != null) {
                            notesAdapter.notifyDataSetChanged();
                        }
                    }
                } else {
                    // For tabbed view (ViewPager)
                    if (listTemp != null && !listTemp.isEmpty()) {
                        list.clear();
                        for (ArrayList<ToppersNotesModel> subjectList : listTemp) {
                            ArrayList<ToppersNotesModel> filteredList = new ArrayList<>();
                            for (ToppersNotesModel note : subjectList) {
                                if (note.getChapterTitle().toLowerCase().contains(s.toLowerCase().trim())) {
                                    filteredList.add(note);
                                }
                            }
                            list.add(filteredList);
                        }
                        if (viewPagerAdapter != null) {
                            viewPagerAdapter.updateDataList(list);
                        }
                    }
                }
                return false;
            }
        });
    }

    private void getTopperNotes() {

        if (!MyApplication.isNetworkAvailable()) {
            CommonMethods.showNegativeSnackbar(cl_parent, getString(R.string.NoInternetConnection), 3000);
        }
        Log.d(TAG, "getTopperNotes: Method started, categoryId = " + categoryId);

        getTopperNotesApiCall();

    }

    private void getTopperNotesApiCall() {
        Map<String, Object> requestParams = new HashMap<>();
        try {
//            requestParams.put("batchId", MyApplication.getIntData(PreferenceKeys.BATCH_ID.getKey()));
            requestParams.put("type", categoryId != null ? categoryId : BatchNotesScreenType.TOPPER_NOTES.name());
        } catch (Exception e) {
            Log.e(TAG, "getTopperNotesNonCompetitive: error building request body", e);
        }

        Log.d(TAG, "getTopperNotesNonCompetitive: Request = " + requestParams.toString());

        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(AV_TOPPER_NOTES, requestParams), null,
                response -> {
                    if (isFinishing() || isDestroyed()) return;
                    Log.d(TAG, "getTopperNotesNonCompetitive: API response received: " + response);

                    TypeToken<ApiWrappedResponse<List<NotesResponse>>> token = new TypeToken<ApiWrappedResponse<List<NotesResponse>>>() {
                    };
                    ApiWrappedResponse<List<NotesResponse>> apiResponse = gson.fromJson(response.toString(), token.getType());
                    List<NotesResponse> data = (apiResponse != null && apiResponse.isSuccess() && apiResponse.getData() != null)
                            ? apiResponse.getData() : new ArrayList<>();

                    notesList = new ArrayList<>();
                    for (NotesResponse note : data) {
                        ToppersNotesModel model = new ToppersNotesModel();
                        model.setId(note.getId() != null ? String.valueOf(note.getId()) : null);
                        model.setChapterTitle(note.getTitle());
                        model.setNotesUrl(note.getUrl());
                        model.setNotesType(note.getType());
                        model.setSubjectName(note.getSubjectName());
                        model.setNotesBy(note.getNotesBy());
                        model.setLocked(note.getLocked() != null && note.getLocked());
                        notesList.add(model);
                    }

                    notesListMaster.addAll(notesList);
                    Log.d(TAG, "getTopperNotesNonCompetitive: notesList size = " + notesList.size());

                    if (!CollectionUtils.isEmpty(notesList)) {
                        dataLoaded = true;
                        shimmerFrameLayout.setVisibility(View.GONE);

                        if (overall) {
                            recyclerView = findViewById(R.id.notes_list_view);
                            recyclerView.setVisibility(View.VISIBLE);
                            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
                            notesAdapter = new ToppersNotesAdapter(notesList, this);
                            recyclerView.setAdapter(notesAdapter);
                            initializeLockDialog();
                        } else {
                            separateSubject("", true);
                            setUpViewPager();
                        }
                        syncDataWithOffline();
                    } else {
                        Log.w(TAG, "getTopperNotesNonCompetitive: notesList is empty, nothing to display");
                    }
                }, new VolleyGlobalErrorListener(ToppersNotesActivity.this) {
            @Override
            public void postError(VolleyError error) {
                Log.e(TAG, "getTopperNotesNonCompetitive: API request failed - " + error.getMessage(), error);
                CommonMethods.showNegativeSnackbar(tabLayout, error.getMessage(), 3);
            }
        });

        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);

        Log.d(TAG, "getTopperNotesNonCompetitive: Request added to queue");
    }


    private void setUpViewPager() {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        viewPager.setAdapter(null);
        if (tabLayoutMediator != null) {
            tabLayoutMediator.detach();
            tabLayoutMediator = null;
        }
        viewPager.setAdapter(null);
        viewPagerAdapter = new ViewPagerAdapter(this, list, categoryId);
        viewPager.setAdapter(viewPagerAdapter);

         tabLayoutMediator = new TabLayoutMediator(
                tabLayout,
                viewPager,
                (tab, position) -> {
                    tab.setCustomView(getCustomTab(subjectNameList.get(position), position == 0));
                }
        );
        tabLayoutMediator.attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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

        handleTabVisibility();
    }

    private View getCustomTab(String title, boolean isSelected) {
        View tabView = LayoutInflater.from(this).inflate(R.layout.text_tab_layout, null);
        MaterialCardView tabCard = tabView.findViewById(R.id.tab_text_card);
        TextView tabText = tabView.findViewById(R.id.tab_text);
        Typeface customFontUnselected = ResourcesCompat.getFont(this, R.font.nunito_regular);
        Typeface customFontSelected = ResourcesCompat.getFont(this, R.font.nunito_semibold);
        tabText.setText(CommonMethods.getShortName(title));
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

    private void handleTabVisibility() {
        Log.d("TabVisibility", "Handling tab visibility...");
        if (overall) {
            Log.d("TabVisibility", "Overall mode - Hiding tabs, showing viewPager");
            tabLayout.setVisibility(View.GONE);
            rl_tab_layout.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);
            return;
        }

        int subjectsWithData = 0;
        for (int i = 0; i < list.size(); i++) {
            ArrayList<ToppersNotesModel> subjectList = list.get(i);
            if (!CollectionUtils.isEmpty(subjectList)) {
                subjectsWithData++;
                Log.d("TabVisibility", "Subject at index " + i + " has data.");
            }
        }

        Log.d("TabVisibility", "Subjects with data: " + subjectsWithData);
        if (subjectsWithData > 1) {
            tabLayout.setVisibility(View.VISIBLE);
            rl_tab_layout.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.VISIBLE);
        } else if (subjectsWithData == 1) {
            tabLayout.setVisibility(View.GONE);
            rl_tab_layout.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);
        } else {
            tabLayout.setVisibility(View.GONE);
            rl_tab_layout.setVisibility(View.GONE);
            viewPager.setVisibility(View.GONE);
        }
    }

    private void setupTab(int pos) {
        TabLayout.Tab tab = tabLayout.newTab();
        View customTab = LayoutInflater.from(this).inflate(R.layout.text_tab_layout, null);
        TextView tabText = customTab.findViewById(R.id.tab_text);
        Log.d(TAG, "setupTab: " + subjectNameList.get(pos).toString());

        String shortName = CommonMethods.getShortName(subjectNameList.get(pos));
        if (!StringUtils.isEmpty(shortName)) {
            tabText.setText(shortName);
        } else {
            tabText.setText(subjectNameList.get(pos));
        }
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

    private void separateSubject(String query, boolean check) {
        if (list == null || listTemp == null) {
            return;
        }

        list.clear();
        listTemp.clear();
        Map<String, ArrayList<ToppersNotesModel>> map = new HashMap<>();

        for (int j = 0; j < notesList.size(); j++) {
            ToppersNotesModel toppersNotesModel = notesList.get(j);
            String subject = toppersNotesModel.getSubjectName();

            if (StringUtils.isEmpty(query) || (StringUtils.isNotEmpty(query)) &&
                    toppersNotesModel.getChapterTitle().toLowerCase().contains(query.trim())) {

                if (map.containsKey(subject)) {
                    ArrayList<ToppersNotesModel> notesModels = map.get(subject);
                    notesModels.add(toppersNotesModel);
                    map.put(subject, notesModels);
                } else {
                    ArrayList<ToppersNotesModel> notesModels = new ArrayList<>();
                    notesModels.add(toppersNotesModel);
                    map.put(subject, notesModels);
                }
            }
        }

        // Create a temporary list to track subjects with data
        ArrayList<String> validSubjectCodes = new ArrayList<>();
        ArrayList<String> validSubjectNames = new ArrayList<>();

        for (int i = 0; i < subjectCodeList.size(); i++) {
            ArrayList<ToppersNotesModel> notesModels = new ArrayList<>();
            String code = subjectCodeList.get(i);
            if (map.containsKey(code)) {
                notesModels.addAll(map.get(code));
            }
            Log.d("TotalSize", "subject code = " + code + " size = " + notesModels.size());

            if (!CollectionUtils.isEmpty(notesModels)) {
                // Only add subjects that have data
                validSubjectCodes.add(code);
                validSubjectNames.add(subjectNameList.get(i));

                for (ToppersNotesModel toppersNotesModel : notesModels) {
                    toppersNotesModel.setSubjectName(subjectNameList.get(i));
                }
                list.add(notesModels);
                listTemp.add(notesModels);
            }
        }

        // Update the subject lists with only valid subjects
        if (check) {
            subjectCodeList.clear();
            subjectNameList.clear();
            subjectCodeList.addAll(validSubjectCodes);
            subjectNameList.addAll(validSubjectNames);
        }
    }

    private void syncDataWithOffline() {
//        notesViewModel.getAllNotes(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey())).
//                observe(this, this::syncListWithOffline);

        notesViewModel.getAllNotes(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()))
                .observe(this, new Observer<List<NoteEntity>>() {
                    @Override
                    public void onChanged(List<NoteEntity> notes) {
                        syncListWithOffline(notes);
                        notesViewModel.getAllNotes(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()))
                                .removeObserver(this);
                    }
                });
    }

    private void syncListWithOffline(List<NoteEntity> list) {
        try {
            Map<String, NoteEntity> noteMap = new HashMap<>();
            for (NoteEntity entity : list) {
                noteMap.put(entity.getId(), entity);
            }

            // Sync data
            for (ToppersNotesModel model : notesList) {
                NoteEntity match = noteMap.get(model.getId());
                if (match != null) {
//                    model.setNotesUrl(match.getNotesUrl());
//                    model.setLocked(match.isLocked());
                    model.setDownloadComplete(match.getStatus().equals(NoteEntity.STATUS_COMPLETED));
                    model.setProgress(match.getProgress());
                    model.setStatus(match.getStatus());
                } else {
                    model.setDownloadComplete(false);
                    model.setProgress(0);
                    model.setStatus(null);
//                    model.setNotesUrl(null);
                }
            }

            if (notesAdapter != null) {
                notesAdapter.notifyDataSetChanged();
            } else {
                if (viewPagerAdapter != null) {
//                viewPagerAdapter.updateDataList(this.paperList);
                    List<Fragment> fragments = viewPagerAdapter.getAllFragments();
                    for (Fragment fragment : fragments) {
                        ((ToppersNotesFragment) fragment).updateAdapterList();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateModelAndNotify(ToppersNotesModel model, NoteEntity entity) {
        model.setNotesUrl(entity.getNotesUrl());
        model.setLocked(entity.isLocked());
        model.setDownloadComplete(entity.getStatus().equals(NoteEntity.STATUS_COMPLETED));
        model.setProgress(entity.getProgress());
        model.setPageCount(entity.getPageCount());
        model.setStatus(entity.getStatus());

        if (notesAdapter != null) {
            int position = notesAdapter.getList().indexOf(model);
            if (position != -1) {
                notesAdapter.notifyItemChanged(position);
            }
        }
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        finish();
//    }

    @Override
    public void onNotesClick(String chapterTitle, String url, String notesType, String subjectName, boolean isLocked) {
        try {
            if (isLocked) {
                if (lockDialog != null && !lockDialog.isShowing()) {
                    Map<String, String> params = new HashMap<>();
                    params.put("chapterTitle", chapterTitle);
                    params.put("subjectName", subjectName);
                    params.put("notesType", notesType);
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.toppers_notes_lock_click.getActionName(), params);
                    lastClickedNotesType = notesType;
                    handleSubscriptionClick();
                }
                return;
            }
            PdfUtils.openPdfViewer(this, url,
                    chapterTitle, notesType, subjectName, false, "", "");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleSubscriptionClick() {
        String popupType = SubscriptionPopupType.NOTES.name();

        if (StringUtils.isNotEmpty(lastClickedNotesType)) {
            try {
                // Try to use the specific note type if it's defined in SubscriptionPopupType
                popupType = SubscriptionPopupType.valueOf(lastClickedNotesType.toUpperCase()).name();
            } catch (IllegalArgumentException e) {
                
            }
        }

        Object source = StringUtils.isNotEmpty(categoryId) ? categoryId : RedirectionSource.TOPPERS_NOTES_JOIN;
        Log.d("ToppersNotesActivity", "handleSubscriptionClick: Navigating to subscription page | source: " + source + " | categoryId: " + categoryId);
        Intent intent = SubscriptionNavigationHelper.getSubscriptionPageIntentWithSource(this, source);
        intent.putExtra("navigatedFromScreen", popupType);
        if (lockDialog != null && lockDialog.isShowing()) {
            lockDialog.dismiss();
        }
        startActivity(intent);
    }

    @Override
    public void onNoteDownloadClick(int position, String url, boolean isLocked, Object object) {
        try {
            if (CommonMethods.isOnline(this)) {
                if (StringUtils.isNotEmpty(url)) {
                    ToppersNotesModel model = (ToppersNotesModel) object;

                    NoteEntity noteEntity = new NoteEntity(
                            model.getId(),
                            model.getSubjectId(),
                            model.getSubjectName(),
                            model.getChapterTitle(),
                            model.getNotesBy(),
                            model.getNotesUrl(),
                            model.isLocked(),
                            model.getPageCount(), 0, "pdf", NoteEntity.STATUS_PENDING, 0, null,
                            MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
                    noteEntity.setUserId(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
                    noteEntity.setNoteType(model.getNotesType());
                    notesViewModel.insert(noteEntity);
                    PdfUtils.startPdfDownload(this, noteEntity);

                    notesViewModel.getLiveNotesById(model.getId())
                            .observe(this, entity -> {
                                if (entity != null) {
                                    updateModelAndNotify(model, entity);
                                }
                            });

                    Map<String, String> params = new HashMap<>();
                    params.put("page", TAG);
                    params.put("id", model.getId());
                    params.put("subjectName", model.getSubjectName());
                    params.put("subjectId", model.getSubjectId());
                    params.put("chapterTitle", model.getChapterTitle());
                    params.put("noteType", model.getNotesType());
                    params.put("isLocked", "" + model.isLocked());
                    params.put("pageCount", "" + model.getPageCount());
                    params.put("size", "" + model.getFileSize());
                    params.put("notesUrl", model.getNotesUrl());

                    params.put("categoryId", categoryId);
//                    params.put("ncertSolutions", "" + isNcertSolutions);
                    params.put("overall", "" + overall);
//                    params.put("preExamPreparation", "" + preExamPreparation);
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.note_download_click.getActionName(), params);

                }

            } else {
                CommonMethods.showNegativeSnackbar(cl_parent, getString(R.string.NoInternetConnection), 3000);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onNoteStatusClick(int position, Object object) {
        try {

        } catch (Exception ex) {

        }
    }


    private void initializeLockDialog() {
        lockDialog = new Dialog(this);
        lockDialog.setContentView(R.layout.custom_notes_lock_box);
        lockDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        lockDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lockDialog.setCancelable(false);

        subscriptionLayout = lockDialog.findViewById(R.id.subscriptionLayout);

        subscriptionLayout.setOnClickListener(view -> {
            lockDialog.cancel();
            lockDialog.dismiss();
//            Intent intent;
//            if (preExamPreparation) {
//                intent = new Intent(this, CampaignSubscriptionActivity.class);
//            } else {
//            intent = new Intent(this, CampaignSubscriptionActivity.class);
//                intent.putExtra("plan_level", "COURSE");
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.locked_view_plans_dialog_clicked.getActionName());
            Object source = StringUtils.isNotEmpty(categoryId) ? categoryId : RedirectionSource.TOPPERS_NOTES_JOIN;
            Log.d("ToppersNotesActivity", "Navigating to subscription page from lockDialog | source: " + source + " | categoryId: " + categoryId);
            SubscriptionNavigationHelper.navigateToSubscriptionPage(this, source);
        });
        ImageView cancel = lockDialog.findViewById(R.id.cancel);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lockDialog.dismiss();
            }
        });
    }

    private static class ViewPagerAdapter extends FragmentStateAdapter {
        private ArrayList<ArrayList<ToppersNotesModel>> list;
        private final Map<Integer, Fragment> fragmentMap = new HashMap<>();
        private final String categoryId;

        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity,
                                ArrayList<ArrayList<ToppersNotesModel>> list, String categoryId) {
            super(fragmentActivity);
            this.list = list;
            this.categoryId = categoryId;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment fragment = ToppersNotesFragment.newInstance(list.get(position), categoryId);
            fragmentMap.put(position, fragment);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return list != null ? list.size() : 0;
        }

        public Fragment getFragment(int position) {
            return fragmentMap.get(position);
        }

        public List<Fragment> getAllFragments() {
            return new ArrayList<>(fragmentMap.values());
        }

        public void updateDataList(ArrayList<ArrayList<ToppersNotesModel>> newDataList) {
            this.list = newDataList;
            notifyDataSetChanged(); // works but not as powerful as DiffUtil
        }
    }

    private void destroyHowTo() {
        how_to_video_view.setVisibility(View.GONE);
        if (howToVideoView != null)
            howToVideoView.remove();
    }


    @Override
    protected void onDestroy() {
        MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll(TAG);
        unregisterNetworkListener();
        if (playStoreDialog != null && playStoreDialog.isShowing()) {
            playStoreDialog.dismiss();
        }
        destroyHowTo();
        super.onDestroy();
    }
}