package arivihan.technologies.doubtbuzzter2.activity;

import static android.view.View.GONE;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.SearchView;

import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.NotesAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.SortSubjectNameAnalyticAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.model.NotesModel;
import arivihan.technologies.doubtbuzzter2.model.QuickNotesContents;
import arivihan.technologies.doubtbuzzter2.model.QuickNotesModel;
import arivihan.technologies.doubtbuzzter2.model.subject.SubjectResponse;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_SUBSCRIBED_SUBJECT;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.QUICK_NOTES;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class QuickNotesActivity extends BaseActivity implements SortSubjectNameAnalyticAdapter.OnSubjectSortClick, NotesAdapter.OnNotesClickListener {

    LinearLayout back;
    Toolbar toolbar;
    RelativeLayout selectSubject, ParentLayout;
    RelativeLayout lottieAnimationView;
    LinearLayout noResultFoundLayout;
    Gson gson = new Gson();
    ShimmerFrameLayout shimmer;
    int dataLoadedCompleted = 0;
    TextView selectedSubject, title;
    List<QuickNotesModel> chapterListInfoDtos = new ArrayList<>();
    ArrayList<String> Usersubject = new ArrayList<>();
    ArrayList<String> subjectId = new ArrayList<>();
    Dialog dialog;
    ArrayList<NotesModel> models = new ArrayList<>();
    RecyclerView recyclerView;
    NotesAdapter adapter;
    SortSubjectNameAnalyticAdapter DialogSubjectName;
    private final String TAG = QuickNotesActivity.class.getSimpleName();
    NetworkStatusLiveData networkStatusLiveData;
    String filterString = "";

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_notes);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        title = findViewById(R.id.activity_title);
        title.setText(getResources().getString(R.string.QuickNotes));
        back = findViewById(R.id.back_arrow);
        ParentLayout = findViewById(R.id.ParentRelativeLayout);
        lottieAnimationView = findViewById(R.id.noData);
        selectSubject = findViewById(R.id.selectSubject);
        shimmer = findViewById(R.id.shimmer);
        selectedSubject = findViewById(R.id.selectedSubject);
        recyclerView = findViewById(R.id.recyclerViewNotes);
        noResultFoundLayout = findViewById(R.id.no_result_found_layout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        getAndSetLectureAdapter(getString(R.string.ten_min_notes_all_subjects));
        getYourSubscribedSubject();

        //dialog setUp--
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialogue_subjectselection);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // sort according to the subject---
        selectSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                RecyclerView recyclerView = dialog.findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(new LinearLayoutManager(QuickNotesActivity.this, LinearLayoutManager.VERTICAL, false));
                recyclerView.setAdapter(DialogSubjectName);
            }
        });


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            if (isOnline) {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    if (dataLoadedCompleted == 0) {
                        dataLoadedCompleted = 1;
                        getAndSetLectureAdapter(getString(R.string.ten_min_notes_all_subjects));
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
                getAndSetLectureAdapter(getString(R.string.ten_min_notes_all_subjects));
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

    }

    private void getAndSetLectureAdapter(String subjectName) {
        Map<String, Object> map = new HashMap<>();
        String UserId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
        map.put(PreferenceKeys.USER_ID.getKey(), UserId);
        MyJsonArrayRequest myJsonObjectRequest = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(QUICK_NOTES, map), null,
                response -> {
                    Gson gson = new Gson();
                    TypeToken<List<QuickNotesModel>> token = new TypeToken<List<QuickNotesModel>>() {
                    };
                    dataLoadedCompleted = 1;
                    chapterListInfoDtos = gson.fromJson(response.toString(), token.getType());
                    if (subjectName.equals("Maths"))
                        updateDataInRecyclerView("Mathematics", chapterListInfoDtos);
                    else
                        updateDataInRecyclerView(subjectName, chapterListInfoDtos);
                }, new VolleyGlobalErrorListener(QuickNotesActivity.this) {
            @Override
            public void postError(VolleyError error) {
                shimmer.stopShimmer();
                shimmer.setVisibility(GONE);
                CommonMethods.showNegativeSnackbar(ParentLayout, error.getMessage());
            }
        });
        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
    }


    private void updateDataInRecyclerView(String subject, List<QuickNotesModel> chapterListInfoDtos) {
        models = new ArrayList<>();
        int pos = 0;
        for (QuickNotesModel chapterInfo : chapterListInfoDtos) {
            if (subject.equals(getString(R.string.ten_min_notes_all_subjects))) {
                NotesModel nm = new NotesModel();
                if (chapterInfo.isFromArivihan())
                    nm.setCreatedBy(getString(R.string.by_arivihan));
                else
                    nm.setCreatedBy(MyApplication.getStringData(PreferenceKeys.NAME.getKey()));
                nm.setChapterName(chapterInfo.getSubject());
                nm.setTopicName(chapterInfo.getTitle());
                nm.setType("Auto");
                nm.setPosition(pos);
                models.add(nm);
            } else if (chapterInfo.getSubject().equals(subject)) {
                NotesModel nm = new NotesModel();
                if (chapterInfo.isFromArivihan())
                    nm.setCreatedBy(getString(R.string.by_arivihan));
                else
                    nm.setCreatedBy(MyApplication.getStringData(PreferenceKeys.NAME.getKey()));
                nm.setChapterName(chapterInfo.getSubject());
                nm.setTopicName(chapterInfo.getTitle());
                nm.setType("Auto");
                nm.setPosition(pos);
                models.add(nm);

            }
            pos++;
        }
        if (models.isEmpty()) {
            CommonMethods.showNegativeSnackbar(ParentLayout, getString(R.string.no_notes_found));
            noResultFoundLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noResultFoundLayout.setVisibility(GONE);
        }
        shimmer.stopShimmer();
        shimmer.setVisibility(GONE);
        adapter = new NotesAdapter(models, this::onNotesClick, noResultFoundLayout);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void setSubjectModel(String subject) {
        models.clear();
        if (chapterListInfoDtos.isEmpty()) {
            CommonMethods.showNegativeSnackbar(ParentLayout, getString(R.string.try_again));
        } else {
            models.clear();
            int pos = 0;
            for (QuickNotesModel chapterInfo : chapterListInfoDtos) {

                if (subject.equals(getString(R.string.ten_min_notes_all_subjects))) {
                    NotesModel nm = new NotesModel();
                    if (chapterInfo.isFromArivihan())
                        nm.setCreatedBy(getString(R.string.by_arivihan));
                    else
                        nm.setCreatedBy(MyApplication.getStringData(PreferenceKeys.NAME.getKey()));
                    nm.setChapterName(chapterInfo.getSubject());
                    nm.setTopicName(chapterInfo.getTitle());
                    nm.setType("Auto");
                    nm.setPosition(pos);
                    models.add(nm);
                } else if (chapterInfo.getSubject().equals(subject)) {
                    NotesModel nm = new NotesModel();
                    if (chapterInfo.isFromArivihan())
                        nm.setCreatedBy(getString(R.string.by_arivihan));
                    else
                        nm.setCreatedBy(MyApplication.getStringData(PreferenceKeys.NAME.getKey()));
                    nm.setChapterName(chapterInfo.getSubject());
                    nm.setTopicName(chapterInfo.getTitle());
                    nm.setType("Auto");
                    nm.setPosition(pos);
                    models.add(nm);

                }
                pos++;
            }
            if (models.isEmpty()) {
                noResultFoundLayout.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(GONE);
                CommonMethods.showNegativeSnackbar(ParentLayout, getString(R.string.no_notes_found));
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                noResultFoundLayout.setVisibility(GONE);
            }
            shimmer.stopShimmer();
            shimmer.setVisibility(GONE);
            adapter = new NotesAdapter(models, this::onNotesClick, noResultFoundLayout);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            if(!filterString.isEmpty()) {
                adapter.getFilter().filter(filterString);
            }

        }
    }

    private void getYourSubscribedSubject() {
        Map<String, Object> map = new HashMap<>();
        String UserId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
        map.put(PreferenceKeys.USER_ID.getKey(), UserId);
        MyJsonArrayRequest myJsonObjectRequest2 = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_SUBSCRIBED_SUBJECT, map), null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        TypeToken<List<SubjectResponse>> token = new TypeToken<List<SubjectResponse>>() {
                        };
                        List<SubjectResponse> list = gson.fromJson(response.toString(), token.getType());
                        for (SubjectResponse sm : list) {
                            subjectId.add(sm.getId());
                            Usersubject.add(sm.getSubjectName());
                        }
                        subjectId.add("Overall");
                        Usersubject.add(getString(R.string.ten_min_notes_all_subjects));
                        DialogSubjectName = new SortSubjectNameAnalyticAdapter(Usersubject, subjectId, QuickNotesActivity.this::onsubjectSortClick, Usersubject.size() - 1);
                    }
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                CommonMethods.showNegativeSnackbar(ParentLayout, error.getMessage());
            }
        });
        myJsonObjectRequest2.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.search_menu_item, menu);
        menu.findItem(R.id.info_item).setVisible(false);
        MenuItem menuItem = menu.findItem(R.id.search);
        SearchView searchView = menuItem.getActionView().findViewById(R.id.search_view);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint("Search Notes");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                filterString = s;
                if (adapter != null) {
                    adapter.getFilter().filter(s);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                filterString = s;
                if (adapter != null) {
                    adapter.getFilter().filter(s);
                }
                return false;
            }
        });

        searchView.setOnCloseListener(() -> {
            title.setVisibility(View.VISIBLE);
            if (!StringUtils.isEmpty(filterString)) {
                filterString = "";
                if (adapter != null) {
                    adapter.getFilter().filter(filterString);
                }
            }
            return false;
        });
//        if(!MyApplication.getBoolData("QuickNotesActivity")) {
//            new TapTargetSequence(this).targets(
//                    TapTarget.forView(searchView, getString(R.string.search_handwritten_notes),
//                            getString(R.string.revise_weak_topics))
//                            .outerCircleColor(R.color.arivihan_primary_color)
//                            .outerCircleAlpha(0.96f)
//                            .targetCircleColor(R.color.navy_blue)
//                            .titleTextSize(18)
//                            .titleTextColor(R.color.white)
//                            .descriptionTextSize(15)
//                            .titleTypeface(Typeface.DEFAULT_BOLD)
//                            .descriptionTextColor(R.color.white)
//                            .textColor(R.color.white)
//                            .textTypeface(Typeface.SANS_SERIF)
//                            .dimColor(R.color.black)
//                            .drawShadow(true)
//                            .cancelable(false)
//                            .tintTarget(true)
//                            .transparentTarget(true)
//                            .targetRadius(50)).listener(new TapTargetSequence.Listener() {
//                @Override
//                public void onSequenceFinish() {
//                    MyApplication.saveData("QuickNotesActivity", true);
//                }
//
//                @Override
//                public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
//                }
//
//                @Override
//                public void onSequenceCanceled(TapTarget lastTarget) {
//                }
//            }).start();
//        }

        return true;
    }

    @Override
    public void onsubjectSortClick(String Id, String name, int position) {
        setSubjectModel(name);
        if (name.equalsIgnoreCase(getString(R.string.ten_min_notes_all_subjects)))
            selectedSubject.setText(R.string.subject_all);
        else
            selectedSubject.setText(name);
        DialogSubjectName = new SortSubjectNameAnalyticAdapter(Usersubject, subjectId, this::onsubjectSortClick, position);
        dialog.dismiss();
    }

    @Override
    public void onNotesClick(int position) {
        String title = chapterListInfoDtos.get(position).getTitle();
        String image[] = new String[chapterListInfoDtos.get(position).getQuickNotesContents().size()];
        int i = 0;

        for (QuickNotesContents s : chapterListInfoDtos.get(position).getQuickNotesContents()) {
            image[i] = s.getContentUrl();
            i++;
        }

//        Log.d(TAG, "Quick Note: "+image.length+", "+i);
        Intent intent = new Intent(QuickNotesActivity.this,ViewNotesActivity.class);
        intent.putExtra("images", image);
        intent.putExtra(PreferenceKeys.TYPE.getKey(), "Auto");
        intent.putExtra("Name", title);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll(TAG);
        super.onDestroy();
        // Stop auto swipe when activity is destroyed
    }
}