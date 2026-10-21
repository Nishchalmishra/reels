package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.DOUBT_SESSION_API;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.DoubtHistoryAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.NewDoubtListAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.helper.EnvironmentManager;
import arivihan.technologies.doubtbuzzter2.helper.MyEmojiFilter;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.model.doubt.DoubtSessionHistory;
import arivihan.technologies.doubtbuzzter2.model.doubt.DoubtSessionScreenResponse;
import arivihan.technologies.doubtbuzzter2.model.doubt.DoubtTabsModel;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.model.remote_config.MyDoubtPageConfig;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;


public class DoubtHistoryActivity extends BaseActivity {
    public static final String TAG = "NewDoubtHistoryActivity";

    ConstraintLayout parentLayout;
    LinearLayout progressBar;
    LinearLayout backButtonLayout;
    LinearLayout filterBaseLayout, filterBackground;
    ImageView filter;
    SearchView searchView;
    TextView title;
    MaterialCardView askDoubt;
    ShimmerFrameLayout shimmerFrameLayout;
    Handler handler;
    LinearLayout noResultFound;
    MaterialCardView cv_marker;
    boolean allowBack;
    LinearLayout noDoubts;
    NetworkStatusLiveData networkStatusLiveData;
    MyDoubtPageConfig myDoubtPageConfig;

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private DoubtTabPagerAdapter pagerAdapter;
    String currentSearchQuery = "";


    public enum UiState {
        LOADING,
        CONTENT,
        EMPTY
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doubt_history);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!searchView.isIconified()) {
                    searchView.setIconified(true);
                } else {
                    finish();
                }
            }
        });

        Log.d(TAG, "onCreate:");

        setViews();
        setUpSearch();
        loadTabs();


        //network
        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            if (isOnline) {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    CommonMethods.showPositiveSnackbar(parentLayout, getString(R.string.InternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
                }
            } else {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                    CommonMethods.showNegativeSnackbar(parentLayout, getString(R.string.NoInternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), false);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), true);
                }
            }
        });

    }


    private void setViews() {
        parentLayout = findViewById(R.id.parentLayout);
        noDoubts = findViewById(R.id.no_doubts_layout);
        noResultFound = findViewById(R.id.no_result_found_layout);
        backButtonLayout = findViewById(R.id.back_button_layout);
        backButtonLayout.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        searchView = findViewById(R.id.search_view);
        title = findViewById(R.id.title);
        cv_marker = findViewById(R.id.cv_marker);
        cv_marker.setVisibility(View.GONE);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        ImageView searchButton = searchView.findViewById(R.id.search_button);
        searchButton.setOnClickListener(v -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.doubt_search_btn_clicked.getActionName());
            searchView.setIconified(false);
            searchView.setMaxWidth(Integer.MAX_VALUE);
            title.setVisibility(View.GONE);
        });

        setSearchViewInputFilter(searchView, MyEmojiFilter.getFilter());

        filter = findViewById(R.id.filter_doubt);
        askDoubt = findViewById(R.id.ask_doubt_card);
        shimmerFrameLayout = findViewById(R.id.shimmer_layout);
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();
        progressBar = findViewById(R.id.progress_bar_background);
        askDoubt.setOnClickListener(v -> {
            boolean skipSubjectSelection = MyApplication.getFirebaseRemoteConfig().getBoolean("doubt_chat_web_web_view_skip_subject_selection_" + EnvironmentManager.getEnvironmentNameByUrl(MyApplication.getEndPoint()).toLowerCase());
            if (skipSubjectSelection) {
                Map<String, String> params = new HashMap<>();
                params.put("subject", "");
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.ask_doubt_subject_clicked.getActionName(), params);
                allowBack = true;
                CommonNavigator.navigateToDoubtChatWithSubject(this, "", "", "", true);
            } else {
                Intent intent = new Intent(DoubtHistoryActivity.this, ChatScreenChooseSubjectForChat.class);
                allowBack = true;
                intent.putExtra("allowBack", true);
                startActivity(intent);
            }
        });

        try {
            String configStr = MyApplication.getFirebaseRemoteConfig().getString("my_doubt_page_config");
            Gson gson = new Gson();
            myDoubtPageConfig = gson.fromJson(configStr, MyDoubtPageConfig.class);

            Log.d("TAG", "--> "+myDoubtPageConfig.toString());
            askDoubt.setVisibility(myDoubtPageConfig.isAskDoubtOptionVisible()?View.VISIBLE:View.GONE);
            filter.setVisibility(View.GONE);
            searchView.setVisibility(myDoubtPageConfig.isSearchVisible()?View.VISIBLE:View.GONE);
        } catch (Exception ignored){}

    }



    private void setSearchViewInputFilter(SearchView searchView, InputFilter[] filter) {
        try {
            Field searchField = SearchView.class.getDeclaredField("mSearchSrcTextView");
            searchField.setAccessible(true);
            EditText searchEditText = (EditText) searchField.get(searchView);
            searchEditText.setFilters(filter);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    //set ui state
    private void updateUi(UiState state) {
        boolean loading = state == UiState.LOADING;
        boolean content = state == UiState.CONTENT;
        boolean empty = state == UiState.EMPTY;

        if (loading) shimmerFrameLayout.startShimmer();
        else shimmerFrameLayout.stopShimmer();

        shimmerFrameLayout.setVisibility(loading ? View.VISIBLE : View.GONE);
        tabLayout.setVisibility(content ? View.VISIBLE : View.GONE);
        viewPager.setVisibility(content ? View.VISIBLE : View.GONE);
        noDoubts.setVisibility(empty ? View.VISIBLE : View.GONE);
        noResultFound.setVisibility(View.GONE);
    }


    private void setUpSearch() {
        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        performSearch(query);
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        performSearch(newText);
                        return true;
                    }
                });

        searchView.setOnCloseListener(() -> {
            title.setVisibility(View.VISIBLE);
            performSearch("");
            return false;
        });
    }


    private void performSearch(String query) {
        if (query.equals(currentSearchQuery)) return;
        currentSearchQuery = query;

        if (handler != null) handler.removeCallbacksAndMessages(null);

        handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            if (pagerAdapter == null) return;

            for (int i = 0; i < pagerAdapter.getItemCount(); i++) {
                Fragment fragment = pagerAdapter.getFragment(i);
                if (fragment instanceof DoubtSessionListFragment) {
                    ((DoubtSessionListFragment) fragment).search(query);
                }
            }
        }, 500);
    }




    private void loadTabs() {
        updateUi(UiState.LOADING);

        Map<String, Object> map = new HashMap<>();
        MyJsonObjectRequest request = new MyJsonObjectRequest(
                Request.Method.GET,
                CommonMethods.getUrlParam(DOUBT_SESSION_API, map),
                null,
                response -> {
                    try {
                        Gson gson = new Gson();
                        TypeToken<ApiWrappedResponse<DoubtSessionScreenResponse>> token = new TypeToken<>() {};
                        ApiWrappedResponse<DoubtSessionScreenResponse> apiResponse = gson.fromJson(response.toString(), token.getType());

                        if (apiResponse != null
                                && apiResponse.getData() != null
                                && apiResponse.getData().getDoubtTypes() != null
                                && !apiResponse.getData().getDoubtTypes().isEmpty()) {
                            setupTabs(apiResponse.getData());
                            updateUi(UiState.CONTENT);
                        } else {
                            updateUi(UiState.EMPTY);
                        }
                    } catch (Exception e) {
                        updateUi(UiState.EMPTY);
                    }
                },
                error -> {
                    updateUi(UiState.EMPTY);
                });

        request.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(request);
    }




    //set up tabs
    private void setupTabs(DoubtSessionScreenResponse response) {
        if (response == null || response.getDoubtTypes() == null || response.getDoubtTypes().isEmpty()) return;

        pagerAdapter = new DoubtTabPagerAdapter(this, response);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(response.getDoubtTypes().get(position).getTitle())).attach();
    }




//    @Override
//    public void onOptionClick(boolean checked, String type, String title, List<DoubtFilterModel> list, int pos, int parentPos) {
//        if (type.equalsIgnoreCase(DoubtSolutionStatus.IN_PROGRESS.name()) ||
//                type.equalsIgnoreCase(DoubtSolutionStatus.SOLVED_SEEN.name()) ||
//                type.equalsIgnoreCase(DoubtSolutionStatus.SOLVED_UNSEEN.name())) {
//            filterApplied = true;
//            if (checked) doubtSolutionStatus = DoubtSolutionStatus.getVal(type);
//            else doubtSolutionStatus = null;
//
//        } else if (type.equalsIgnoreCase("ASC") || type.equalsIgnoreCase("DESC")) {
//            filterApplied = true;
//            if (checked) sortOrder = type.equalsIgnoreCase("ASC");
//            else sortOrder = false;
//        } else if (type.equalsIgnoreCase(SubjectEnum.PHYSICS.getValue()) ||
//                type.equalsIgnoreCase(SubjectEnum.CHEMISTRY.getValue()) ||
//                type.equalsIgnoreCase(SubjectEnum.MATHEMATICS.getValue()) ||
//                type.equalsIgnoreCase(SubjectEnum.BIOLOGY.getValue())) {
//            filterApplied = true;
//            if (checked) subjectId = type;
//            else subjectId = "";
//        } else if (type.equalsIgnoreCase("CLEAR")) {
//            filterApplied = false;
//            clearFilterApplied = checked;
//            FilterOptionAdapter.setClearFilterApplied(checked);
//            if (checked) {
//                for (DoubtFilters doubtFilters : doubtFiltersList) {
//                    for (DoubtFilterModel doubtFilterModel : doubtFilters.getList()) {
//                        doubtFilterModel.setState(0);
//                    }
//                }
//            }
//        }
//
//        for (int i = 0; i < list.size(); i++) {
//            list.get(i).setState(0);
//        }
//        if (checked) {
//            list.get(pos).setState(1);
//            if (!type.equalsIgnoreCase("CLEAR")) {
//                clearFilterApplied = false;
//                FilterOptionAdapter.setClearFilterApplied(false);
//                doubtFiltersList.get(doubtFiltersList.size() - 1).getList().get(0).setState(0);
//            }
//        } else {
//            list.get(pos).setState(0);
//        }
//        doubtFiltersList.get(parentPos).getList().clear();
//        doubtFiltersList.get(parentPos).getList().addAll(list);
//        filterAdapter.notifyDataSetChanged();
//    }


    @Override
    protected void onResume() {
        Log.d("Ask doubt onresume", "onResume: ");
        super.onResume();
        noResultFound.setVisibility(View.GONE);
        if (allowBack) {
            allowBack = false;
            currentSearchQuery = "";
            if (searchView != null && !searchView.isIconified()) {
                searchView.setQuery("", false);
                searchView.setIconified(true);
            }
            updateUi(UiState.LOADING);
            loadTabs();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
    }


    @Override
    public void onDestroy() {
        MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll(TAG);
        super.onDestroy();
    }











    //tab viewpager adapter
    private static class DoubtTabPagerAdapter extends FragmentStateAdapter {

        private final List<DoubtTabsModel> tabs;
        private final DoubtSessionScreenResponse data;
        private final SparseArray<Fragment> fragments = new SparseArray<>();
        private final FragmentActivity activity;


        public DoubtTabPagerAdapter(@NonNull FragmentActivity fragmentActivity, DoubtSessionScreenResponse data) {
            super(fragmentActivity);
            this.activity = fragmentActivity;
            this.tabs = data.getDoubtTypes();
            this.data = data;
            Log.d(TAG, "DoubtTabPagerAdapter: " + data.getDoubtTypes().toString());
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            DoubtTabsModel tab = tabs.get(position);
            String query = "";
            if (activity instanceof DoubtHistoryActivity) {
                query = ((DoubtHistoryActivity) activity).currentSearchQuery;
            }
            DoubtSessionListFragment fragment = DoubtSessionListFragment.newInstance(tab.getType(), query);
            //for first tab data
            if (position == 0 && data != null) fragment.addData(data.getList(), data.getLastId());
            fragments.put(position, fragment);
            return fragment;
        }

        public Fragment getFragment(int position) {
            return fragments.get(position);
        }

        @Override
        public int getItemCount() {
            return tabs.size();
        }
    }





    //fragment
    public static class DoubtSessionListFragment extends Fragment implements NewDoubtListAdapter.OnDoubtClickListener {
        private static final String ARG_TYPE = "ARG_TYPE";
        private static final String ARG_SEARCH = "ARG_SEARCH";

        private RecyclerView recyclerView;
        private SpinKitView progressBar;
        private View noData;

        private String lastId = null;
        private String typeType;

        private List<DoubtSessionHistory> doubtList = new ArrayList<>();
        private NewDoubtListAdapter adapter;

        private String searchText = "";
        private boolean isLoading = false;
        private boolean isDataAdded = false;


        public enum ListState {
            LOADING,
            CONTENT,
            EMPTY
        }

        public static DoubtSessionListFragment newInstance(String typeType, String searchQuery) {
            DoubtSessionListFragment fragment = new DoubtSessionListFragment();
            Bundle bundle = new Bundle();
            bundle.putString(ARG_TYPE, typeType);
            bundle.putString(ARG_SEARCH, searchQuery);
            fragment.setArguments(bundle);
            return fragment;
        }


        public void addData(List<DoubtSessionHistory> newData, String newLastId) {
            if (newData == null) return;
            doubtList.clear();
            doubtList.addAll(newData);

            lastId = newLastId;
            isDataAdded = true;
        }


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                typeType = getArguments().getString(ARG_TYPE);
                searchText = getArguments().getString(ARG_SEARCH, "");
            }
        }


        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_doubt_session, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            recyclerView = view.findViewById(R.id.recyclerView);
            progressBar = view.findViewById(R.id.progress_dataLoading);
            noData = view.findViewById(R.id.noData);

            LottieAnimationView noDataLottie = view.findViewById(R.id.noData);
            if (noDataLottie != null) {
                noDataLottie.setAnimation(R.raw.no_results);
                noDataLottie.setRepeatCount(-1); // Infinite
                noDataLottie.playAnimation();
            }
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (!isLoading && layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == doubtList.size() - 1) {
                        if (lastId != null && !lastId.isEmpty()) {
                            getDoubtList(false);
                        }
                    }
                }
            });

            //show list
            if (isDataAdded) refreshUi();
            else getDoubtList(true);
        }


        //ui state
        private void updateState(ListState state) {
            progressBar.setVisibility(state == ListState.LOADING ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(state == ListState.CONTENT ? View.VISIBLE : View.GONE);
            noData.setVisibility(state == ListState.EMPTY ? View.VISIBLE : View.GONE);
        }


        public void search(String query) {
            searchText = query;
            getDoubtList(true);
        }



        private void getDoubtList(boolean isRefresh) {
            if (isRefresh) {
                isLoading = false;
                lastId = null;
                updateState(ListState.LOADING);
                MyVolleySingleton.getInstance(getContext()).getRequestQueue().cancelAll(this);
            }

            if (isLoading) return;
            isLoading = true;

            HashMap<String, Object> queryMap = new HashMap<>();
            queryMap.put("doubtType", typeType);
            queryMap.put("size", 15);

            try {
                if (!TextUtils.isEmpty(searchText)) queryMap.put("search", URLEncoder.encode(searchText, "UTF-8"));
                if (lastId != null) queryMap.put("lastId", URLEncoder.encode(lastId, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                if (!TextUtils.isEmpty(searchText)) queryMap.put("search", searchText);
                if (lastId != null) queryMap.put("lastId", lastId);
            }

            String url = CommonMethods.getUrlParam(DOUBT_SESSION_API, queryMap);

            Log.d(TAG, "getDoubtList: " + url);
            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                    url, null,
                    response -> {
                        isLoading = false;
                        Gson gson = new Gson();
                        TypeToken<ApiWrappedResponse<DoubtSessionScreenResponse>> token = new TypeToken<>() {};
                        ApiWrappedResponse<DoubtSessionScreenResponse> apiResponse = gson.fromJson(response.toString(), token.getType());

                        DoubtSessionScreenResponse data = apiResponse.getData();

                        if (isRefresh) doubtList.clear();

                        int startPosition = doubtList.size();
                        if (data.getList() != null) {
                            doubtList.addAll(data.getList());
                        }
                        lastId = data.getLastId();

                        if (CollectionUtils.isEmpty(doubtList)){
                            updateState(ListState.EMPTY);
                        }else {
                            updateState(ListState.CONTENT);

                            if (adapter == null) {
                                adapter = new NewDoubtListAdapter(getContext(), doubtList, this);
                                recyclerView.setAdapter(adapter);
                            } else if (isRefresh) {
                                adapter.notifyDataSetChanged();
                            } else {
                                if (data.getList() != null && !data.getList().isEmpty()) {
                                    adapter.notifyItemRangeInserted(startPosition, data.getList().size());
                                }
                            }
                        }


                    }, new VolleyGlobalErrorListener(getContext()) {
                @Override
                public void postError(VolleyError error) {
                    isLoading = false;
                    if (isRefresh) updateState(ListState.EMPTY);
                    CommonMethods.showNegativeSnackbar(getView(), error.getMessage());
                }
            });

            myJsonObjectRequest.setTag(this);
            MyVolleySingleton.getInstance(getContext()).addToRequestQueue(myJsonObjectRequest);
        }


        private void refreshUi() {
            if (CollectionUtils.isEmpty(doubtList)) {
                updateState(ListState.EMPTY);
                return;
            }

            updateState(ListState.CONTENT);

            if (adapter == null) {
                adapter = new NewDoubtListAdapter(getContext(), doubtList, this);
                recyclerView.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
            }
        }


        @Override
        public void onDoubtClick(DoubtSessionHistory doubt) {
            if (getActivity() instanceof DoubtHistoryActivity) {
                ((DoubtHistoryActivity) getActivity()).allowBack = false;
            }
            CommonNavigator.navigateToDoubtChatSession(getContext(), doubt.getChatId(), true);

        }

        @Override
        public void onDestroyView() {
            MyVolleySingleton.getInstance(getContext()).getRequestQueue().cancelAll(this);
            super.onDestroyView();
        }
    }

}

