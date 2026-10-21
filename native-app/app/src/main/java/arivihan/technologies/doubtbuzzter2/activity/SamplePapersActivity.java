package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_SAMPLE_PAPER_CONTENTS;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;

import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.PlayStoreDialog;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.fragments.sample_paper.SamplePapersFragment;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.model.SamplePaperContent;
import arivihan.technologies.doubtbuzzter2.model.SamplePaperModel;
import arivihan.technologies.doubtbuzzter2.model.offline.notes.NoteEntity;
import arivihan.technologies.doubtbuzzter2.viewModel.NotesViewModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class SamplePapersActivity extends BaseActivity {

    Toolbar toolbar;
    LinearLayout back;
    RelativeLayout ParentLayout;
    RelativeLayout lottieAnimationView;
    Gson gson = new Gson();
    ShimmerFrameLayout shimmer;
    int dataLoadedCompleted = 0;
    TextView title;
    List<SamplePaperModel> samplePaperModels = new ArrayList<>();
    ArrayList<ArrayList<SamplePaperModel>> paperList = new ArrayList<>();
    //    Dialog dialog;
//    RecyclerView recyclerView;
    String id;
    int position = -1;
    //    SortSubjectNameAnalyticAdapter DialogSubjectName;
    private RelativeLayout rl_parent;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    ArrayList<String> subjects = new ArrayList<>();
    private final String TAG = SamplePapersActivity.class.getSimpleName();
    PlayStoreDialog playStoreDialog;
    NetworkStatusLiveData networkStatusLiveData;
    String filterString;
    private RelativeLayout rl_tab_layout;
    private CardView cv_tab_left_scroll, cv_tab_right_scroll;
    private MaterialCardView cv_my_download;
    NotesViewModel notesViewModel;
    private Dialog lockDialog;

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (dataLoadedCompleted == 1) {
                syncDataWithOffline();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_paper);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        Uri data = getIntent().getData();

        if (data != null) {
            // From deep link
            id = data.getQueryParameter("id");
        } else {
            // From intent extras
            id = getIntent().getStringExtra("id");
        }

        back = findViewById(R.id.back_arrow);
        ParentLayout = findViewById(R.id.ParentRelativeLayout);
        lottieAnimationView = findViewById(R.id.noData);

        rl_tab_layout = findViewById(R.id.rl_tab_layout);
        cv_tab_left_scroll = findViewById(R.id.cv_tab_left_scroll);
        cv_tab_right_scroll = findViewById(R.id.cv_tab_right_scroll);
        cv_my_download = findViewById(R.id.cv_my_download);

//        selectSubject = findViewById(R.id.selectSubject);
        shimmer = findViewById(R.id.shimmer);
        title = findViewById(R.id.activity_title);

        rl_parent = findViewById(R.id.rl_parent);
        tabLayout = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.viewpager);

        String samplePaperName = MyApplication.getFirebaseRemoteConfig().getString("sample_paper_name");
        title.setText(R.string.previous_year_papers);

//        selectedSubject = findViewById(R.id.selectedSubjectText);
//        selectedSubject.setVisibility(View.GONE);
        initializeLockDialog();
        notesViewModel = MyApplication.getInstance().getNotesViewModel();

//        recyclerView = findViewById(R.id.recyclerViewSamplePaper);
//
//        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
//        samplePaperAdapter = new ViewSamplePaperAdapter(samplePaperModels, this, this);
//        recyclerView.setAdapter(samplePaperAdapter);
//        getSamplePaperContent(id);

        //dialog setUp--
//        dialog = new Dialog(this);
//        dialog.setContentView(R.layout.custom_dialogue_subjectselection);
//        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
//        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // sort according to the subject---
//        selectSubject.setOnClickListener(v -> {
//            dialog.show();
//            RecyclerView recyclerView = dialog.findViewById(R.id.recyclerview);
//            recyclerView.setLayoutManager(new LinearLayoutManager(SamplePapersActivity.this,
//                    LinearLayoutManager.VERTICAL, false));
//            recyclerView.setAdapter(DialogSubjectName);
//
//        });

//        selectSubject.setVisibility(View.GONE);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressedActivity();
            }
        });

        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            if (isOnline) {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    if (dataLoadedCompleted == 0) {
                        dataLoadedCompleted = 1;
                        getSamplePaperContent(id);
                    }
                    CommonMethods.showPositiveSnackbar(ParentLayout, getString(R.string.InternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
                }
            } else {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                    CommonMethods.showNegativeSnackbar(ParentLayout, getString(R.string.NoInternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), false);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), true);
                    if (dataLoadedCompleted == 0)
                        dataLoadedCompleted = 0;
                }
            }
        });

        if (CommonMethods.isOnline(this)) {
            if (dataLoadedCompleted == 0) {
                dataLoadedCompleted = 1;
                getSamplePaperContent(id);
            }
            if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
            }
        } else {
            if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
            }
        }

        cv_my_download.setOnClickListener(V -> {
            Intent intent = new Intent(this, MyDownloadActivity.class);
            intent.putExtra(MyDownloadActivity.bundleIsNoteSelected, true);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.notes_my_download_clicked.getActionName());
            startActivity(intent);
        });

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
        finish();
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

    private void initializeLockDialog() {
        lockDialog = new Dialog(this);
        lockDialog.setContentView(R.layout.custom_notes_lock_box);
        lockDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        lockDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lockDialog.setCancelable(false);

        MaterialCardView subscriptionLayout = lockDialog.findViewById(R.id.subscriptionLayout);

        subscriptionLayout.setOnClickListener(view -> {
            lockDialog.cancel();
            lockDialog.dismiss();
//            Intent intent;
//            intent = new Intent(this, CampaignSubscriptionActivity.class);
//            intent.putExtra("plan_level", "COURSE");
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.locked_view_plans_dialog_clicked.getActionName());
//            startActivity(intent);
            SubscriptionNavigationHelper.navigateToSubscriptionPage(this, RedirectionSource.CHAPTER_LIST_JOIN);
        });
        ImageView cancel = lockDialog.findViewById(R.id.cancel);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lockDialog.dismiss();
            }
        });
    }

    private void getSamplePaperContent(String id) {
        shimmer.startShimmer();
        shimmer.setVisibility(View.VISIBLE);
        Map<String, Object> map = new HashMap<>();
        map.put("samplePaperId", id!=null ? id.trim() : null);
        Log.d(TAG, "getSamplePaperContent: "+id);
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_SAMPLE_PAPER_CONTENTS, map), null,
                response -> {
                    Log.d(TAG, "getSamplePaperContent: " + response.toString());
                    Gson gson = new Gson();
                    TypeToken<SamplePaperContent> token = new TypeToken<SamplePaperContent>() {
                    };
                    dataLoadedCompleted = 1;
                    SamplePaperContent content = gson.fromJson(response.toString(), token.getType());
                    samplePaperModels.clear();
                    if (content != null && !CollectionUtils.isEmpty(content.getDataList())) {
                        samplePaperModels.addAll(content.getDataList());

                        for (SamplePaperModel samplePaperModel : samplePaperModels) {
                            if (StringUtils.isNotEmpty(samplePaperModel.getSubject())) {
                                if (!subjects.contains(samplePaperModel.getSubject())) {
                                    subjects.add(samplePaperModel.getSubject());
                                }
                            }
                        }

                        paperList = new ArrayList<>();
                        for (int i = 0; i < subjects.size(); i++) {
                            ArrayList<SamplePaperModel> list = new ArrayList<>();
                            for (SamplePaperModel samplePaperModel : samplePaperModels) {
                                if (samplePaperModel.getSubject().equals(subjects.get(i))) {
                                    list.add(samplePaperModel);
                                }
                            }
                            paperList.add(list);
                        }

                        Log.d(TAG, "--> Papers List: " + paperList.toString());
                        Log.d(TAG, "--> Subject List: " + subjects.toString());
//                        samplePaperAdapter = new ViewSamplePaperAdapter(samplePaperModels, this, this);
//                        recyclerView.setAdapter(samplePaperAdapter);
                        shimmer.stopShimmer();
                        shimmer.setVisibility(View.GONE);

                        setTabData();
                        syncDataWithOffline();
//                        recyclerView.setVisibility(View.VISIBLE);

//                        showFilterFromSubject(subjects);
                    } else if (content != null && CollectionUtils.isEmpty(content.getDataList())) {
                        shimmer.stopShimmer();
                        shimmer.setVisibility(View.GONE);
                        CommonMethods.showNegativeSnackbar(ParentLayout, getResources().getString(R.string.no_more_data));
                    } else {
                        shimmer.stopShimmer();
                        shimmer.setVisibility(View.GONE);
                        CommonMethods.showNegativeSnackbar(ParentLayout, getResources().getString(R.string.error_fetching_data));
                    }
                }, new VolleyGlobalErrorListener(SamplePapersActivity.this) {
            @Override
            public void postError(VolleyError error) {
                shimmer.stopShimmer();
                shimmer.setVisibility(View.GONE);
                CommonMethods.showNegativeSnackbar(ParentLayout, error.getMessage());
            }
        });
        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
    }

//    private void showFilterFromSubject(Set<String> subjects) {
//
//        if (!CollectionUtils.isEmpty(subjects) && subjects.size() == 1) {
//            return;
//        }
//        ArrayList<String> subjectArrayList = new ArrayList<>();
//        subjectArrayList.add(getString(R.string.subject_all));
//        subjectArrayList.addAll(subjects);
//        DialogSubjectName = new SortSubjectNameAnalyticAdapter(subjectArrayList,
//                subjectArrayList, this, position);
//
//        selectedSubject.setVisibility(View.VISIBLE);
//
//    }

//    @RequiresApi(api = Build.VERSION_CODES.O)
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        getMenuInflater().inflate(R.menu.search_menu_item, menu);
//        MenuItem menuItem = menu.findItem(R.id.search);
//        MenuItem info = menu.findItem(R.id.info_item);
//        info.setVisible(false);
//        SearchView searchView = menuItem.getActionView().findViewById(R.id.search_view);
//        searchView.setMaxWidth(Integer.MAX_VALUE);
//        searchView.setQueryHint("Search Sample Paper");
//
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String s) {
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String s) {
//                try {
//                    if (samplePaperAdapter != null) {
//                        filterString = s;
//                        samplePaperAdapter.getFilter().filter(s);
//                    }
//                } catch (Exception ignored) {
//                }
//                return false;
//            }
//        });
//
//        return true;
//    }

    private void setTabData() {
        try {
            Log.d(TAG, "Subjects List: " + subjects.toString());
            if (subjects != null && subjects.size() <= 1) {
                tabLayout.setVisibility(View.GONE);
            } else {
                tabLayout.setVisibility(View.VISIBLE);
            }
            rl_tab_layout.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.VISIBLE);

            Log.d(TAG, "Setting view pager");
            viewPagerAdapter = new ViewPagerAdapter(this, paperList);
            viewPager.setAdapter(viewPagerAdapter);

            TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(
                    tabLayout,
                    viewPager,
                    (tab, position) -> {
                        tab.setCustomView(getCustomTab(subjects.get(position), position == 0));
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
            for (SamplePaperModel model : samplePaperModels) {
                NoteEntity match = noteMap.get(model.getId());
                if (match != null) {
//                    model.setDownloadLink(match.getNotesUrl());
//                    model.setLocked(match.isLocked());
                    model.setDownloadComplete(match.getStatus().equals(NoteEntity.STATUS_COMPLETED));
                    model.setProgress(match.getProgress());
                    model.setStatus(match.getStatus());
                } else {
                    model.setDownloadComplete(false);
                    model.setProgress(0);
                    model.setStatus(null);
//                    model.setDownloadLink(null);
                }
            }

            if (viewPagerAdapter != null) {
//                viewPagerAdapter.updateDataList(this.paperList);
                List<Fragment> fragments = viewPagerAdapter.getAllFragments();
                for (Fragment fragment : fragments) {
                    ((SamplePapersFragment) fragment).updateAdapterList();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

//    @Override
//    public void onSamplePaperClick(String url, String title) {
//        try {
////            Intent intent = new Intent(this, PdfViewerActivity.class);
////            intent.putExtra("pdfUrl", url);
////            startActivity(intent);
//            PdfUtils.openPdfViewer(this, url, title, false);
//        } catch (ActivityNotFoundException e) {
//            Log.d("openPdf:", e.getMessage());
//
//        }
//    }

//    @Override
//    public void onsubjectSortClick(String Id, String name, int position) {
//        this.position = position;
//        selectedSubject.setText(name);
//        if (name.equalsIgnoreCase(getString(R.string.subject_all))) {
//            samplePaperAdapter = new ViewSamplePaperAdapter(samplePaperModels, this, this);
//            recyclerView.setAdapter(samplePaperAdapter);
//            dialog.dismiss();
//            samplePaperAdapter.getFilter().filter(filterString);
//            return;
//        }
//
//        List<SamplePaperModel> filteredList = new ArrayList<>();
//        for (SamplePaperModel samplePaperModel : samplePaperModels) {
//            if (name.equalsIgnoreCase(samplePaperModel.getSubject())) {
//                filteredList.add(samplePaperModel);
//            }
//        }
//        samplePaperAdapter = new ViewSamplePaperAdapter(filteredList, this, this);
//        recyclerView.setAdapter(samplePaperAdapter);
//        dialog.dismiss();
//        samplePaperAdapter.getFilter().filter(filterString);
//    }

    @Override
    protected void onDestroy() {
        MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll(TAG);
        if (playStoreDialog != null && playStoreDialog.isShowing()) {
            playStoreDialog.dismiss();
        }
        super.onDestroy();
    }

    public class ViewPagerAdapter extends FragmentStateAdapter {
        private ArrayList<ArrayList<SamplePaperModel>> list;
        private final Map<Integer, Fragment> fragmentMap = new HashMap<>();

        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity,
                                ArrayList<ArrayList<SamplePaperModel>> list) {
            super(fragmentActivity);
            this.list = list;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment fragment = SamplePapersFragment.newInstance(list.get(position));
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

        public void updateDataList(ArrayList<ArrayList<SamplePaperModel>> newDataList) {
            this.list = newDataList;
            notifyDataSetChanged();
        }
    }
}