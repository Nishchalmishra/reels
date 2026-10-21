package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.activity.ExoPlayerActivity.KEY_VIDEO_URI;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_CUSTOM_CHAPTER_OF_SUBJECT;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.ChapterNameAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.customView.BehaviourDialogPopup;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.listener.ButtonAListener;
import arivihan.technologies.doubtbuzzter2.model.Behaviour;
import arivihan.technologies.doubtbuzzter2.model.ChapterListInfoDto;
import arivihan.technologies.doubtbuzzter2.model.ChapterNameModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class CustomChapterListActivity extends BaseActivity implements ChapterNameAdapter.OnChapClickListener, ButtonAListener {

    RecyclerView recyclerView;
    ShimmerFrameLayout shimmer;
    TextView sub_name;
    RelativeLayout ParentLayout;
    MaterialCardView subscriptionLayout;
    ProgressBar progress;
    LinearLayout linearLayout;
    ChapterNameAdapter Adapter;
    List<ChapterNameModel> chapterNameModels;
    LinearLayout back;
    Toolbar toolbar;
    Dialog dialog2;
    String subjectName = "";
    public int dataLoaded = 0;
    public int dataLoadedCompleted = 0;
    private String chapterId;
    private boolean isClickable;

    private ExtendedFloatingActionButton askNewLecture;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<Behaviour> behaviourList = new ArrayList<>();
    private List<String> saveList = new ArrayList<>();
    private List<String> saveListtNotClicked = new ArrayList<>();
    private BehaviourDialogPopup behaviourDialogPopup;
    private String subjectId;
    int index = 0;
    Dialog dialogHome;
    NetworkStatusLiveData networkStatusLiveData;

    @Override
    public void onResume() {
        super.onResume();
        getAndSetLectureAdapter(subjectName);

    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_chapter_list);


        shimmer = findViewById(R.id.shimmer_chapters);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        back = findViewById(R.id.back_arrow);
        linearLayout = findViewById(R.id.linearLayout);
        sub_name = findViewById(R.id.activity_title);
        ParentLayout = findViewById(R.id.ParentLayout);
        progress = findViewById(R.id.progress);
        recyclerView = findViewById(R.id.chapter_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        askNewLecture = findViewById(R.id.askNewLecture);
        progress.setProgressTintList(ColorStateList.valueOf(Color.BLACK));

        Intent intentData = getIntent();
        Uri data = intentData.getData();


        subjectName = getIntent().getStringExtra("SubjectName");
        subjectId = getIntent().getStringExtra("SubjectId");

        if (StringUtils.isEmpty(subjectId) && data != null) {
            subjectId = data.getQueryParameter("SubjectId");
            subjectName = data.getQueryParameter("SubjectName");
        }

        sub_name.setText(subjectName);


        back.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        dialog2 = new Dialog(CustomChapterListActivity.this);
        dialog2.setContentView(R.layout.custom_dialog_lock_box);
        dialog2.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        dialog2.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog2.setCancelable(false);
        subscriptionLayout = dialog2.findViewById(R.id.subscriptionLayout);

        subscriptionLayout.setOnClickListener(view -> {
            if (isClickable) {
                dialog2.cancel();
                dialog2.dismiss();
//                    Intent intent = new Intent(CustomChapterListActivity.this, CampaignSubscriptionActivity.class);
////                    intent.putExtra("plan_level", "COURSE");
//                    startActivity(intent);
                SubscriptionNavigationHelper.navigateToSubscriptionPage(this, RedirectionSource.CHAPTER_LIST_JOIN);
            }
        });

        ImageView cut2 = dialog2.findViewById(R.id.cancel);

        cut2.setOnClickListener(v -> dialog2.dismiss());

        askNewLecture.setOnClickListener(view -> {
            Intent intent = new Intent(CustomChapterListActivity.this, CustomFormActivity.class);
            intent.putExtra("SubjectName", subjectName);
            startActivity(intent);
        });

        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            if (isOnline) {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    if (dataLoaded == 0) {
                        dataLoaded = 1;
                        getAndSetLectureAdapter(subjectName);
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
                        dataLoaded = 0;
                }
            }
        });

        if (CommonMethods.isOnline(this)) {
            if (dataLoaded == 0) {
                dataLoaded = 1;
                getAndSetLectureAdapter(subjectName);
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

        if (MyApplication.getBoolData(getString(R.string.update_behaviour))
                || (MyApplication.getStringSet(getString(R.string.behaviour_set)).size() == 0
                && MyApplication.getIntData(getString(R.string.behaviour)) < 3)) {
            //getAndPrepareBehaviourQuestion();
            //int value = MyApplication.getIntData(getString(R.string.behaviour));
            //MyApplication.saveData(getString(R.string.behaviour), value == -1 ? 1 : value + 1);
        }

    }

    private void getAndPrepareBehaviourQuestion() {
        String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        String langtoAdd = "";
        if ("hi".equals(lang) || "hi-rIN".equals(lang)) {
            langtoAdd = "hindi";
        } else if ("mr".equals(lang)) {
            langtoAdd = "MR";

        } else if ("gu".equals(lang)) {
            langtoAdd = "GJ";
        }
        Set<String> behaviourSet = MyApplication.getStringSet(getString(R.string.behaviour_set));
        db.collection("adaptNew")
                .document("questions" + langtoAdd)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> data = document.getData();

                            if (data != null) {
                                try {
                                    for (Map.Entry<String, Object> objectEntry : data.entrySet()) {
                                        Map<String, String> map = (Map<String, String>) objectEntry.getValue();
                                        Behaviour behaviour = new Behaviour(map.get("behaviour"), map.get("q"));

                                        if (behaviourSet.contains(behaviour.getBehaviour())) {
                                            continue;
                                        }
                                        behaviourList.add(behaviour);
                                    }

                                    buildBehaviourPicker(behaviourList);
                                } catch (Exception e) {
                                    Log.d("error", e.getMessage());
                                }
                            }
                        }
                    }
                });

    }

    private void buildBehaviourPicker(List<Behaviour> behaviourList) {
        if (behaviourList != null && behaviourList.size() > 0) {
            behaviourDialogPopup = new BehaviourDialogPopup(CustomChapterListActivity.this);
            behaviourDialogPopup.show();
            behaviourDialogPopup.setCancelable(false);
            behaviourDialogPopup.onButtonAClickListener(this);
            Behaviour behaviour = behaviourList.get(index);
            behaviourDialogPopup.setBodyText(behaviourList.get(index).getQ());
            behaviourDialogPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            if (behaviour.getBehaviour().contains("movie")) {
                behaviourDialogPopup.setAnimationResource(R.raw.sport);
            } else if (behaviour.getBehaviour().contains("sport")) {
                behaviourDialogPopup.setAnimationResource(R.raw.sport);
            }

        }


    }


    private void getAndSetLectureAdapter(String subjectName) {
        if (subjectId != null) {
            Map<String, Object> map = new HashMap<>();
            MyJsonArrayRequest myJsonObjectRequest = new MyJsonArrayRequest(Request.Method.GET,
                    CommonMethods.getUrlParam(GET_CUSTOM_CHAPTER_OF_SUBJECT.replace("{id}", subjectId), map), null,
                    response -> {
                        try {
                            Gson gson = new Gson();
                            TypeToken<List<ChapterListInfoDto>> token = new TypeToken<List<ChapterListInfoDto>>() {
                            };
                            dataLoadedCompleted = 1;
                            List<ChapterListInfoDto> chapterListInfoDtos = gson.fromJson(response.toString(), token.getType());
                            updateDataInRecyclerView(chapterListInfoDtos);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }, new VolleyGlobalErrorListener(CustomChapterListActivity.this) {
                @Override
                public void postError(VolleyError error) {
                    shimmer.stopShimmer();
                    shimmer.setVisibility(View.GONE);
                    CommonMethods.showNegativeSnackbar(ParentLayout, error.getMessage());
                }
            });
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
        }
    }


    private void updateDataInRecyclerView(List<ChapterListInfoDto> chapterListInfoDtos) {
        recyclerView.setVisibility(View.VISIBLE);
        shimmer.stopShimmer();
        shimmer.setVisibility(View.GONE);
        chapterNameModels = new ArrayList<>();
        ChapterNameModel cm;
        for (ChapterListInfoDto chapterInfo : chapterListInfoDtos) {
            cm = ChapterNameModel
                    .builder()
                    .id(chapterInfo.getChapterId())
                    .imageUrl(chapterInfo.getImageUrl())
                    .chapter(chapterInfo.getVideoTitle())
                    .name(chapterInfo.getTeacherName() == null ? "Arivihan" : chapterInfo.getTeacherName())
                    .locked(chapterInfo.isLocked())
                    .lockMessage(chapterInfo.getLockMessage())
                    .teacherIntroImage(chapterInfo.getTeacherIntroImage())
                    .completed(chapterInfo.isCompleted())
                    .notesUrl(chapterInfo.getNotesUrl())
                    .board(chapterInfo.isBoard())
                    .unwatched(chapterInfo.isUnwatched())
                    .build();
            chapterNameModels.add(cm);
        }
        if (chapterNameModels.size() == 0) {
            linearLayout.setVisibility(View.VISIBLE);
        } else {
            linearLayout.setVisibility(View.GONE);
        }
        Adapter = new ChapterNameAdapter(this, true, chapterNameModels, this, subjectName);
        recyclerView.setAdapter(Adapter);
        Adapter.notifyDataSetChanged();
    }

    @Override
    public void onChapClick(int position) {
        TextView title = dialog2.findViewById(R.id.header);
        TextView msg = dialog2.findViewById(R.id.subs);

        title.setText(getString(R.string.OOpsThisChapIsGenerated));


        if (chapterNameModels.get(position).getLockMessage() != null) {
            if (!chapterNameModels.get(position).getLockMessage().isEmpty()) {
                msg.setText(chapterNameModels.get(position).getLockMessage());
                isClickable = chapterNameModels.get(position).isCompleted();
            }
        } else {
            msg.setText(R.string.viewSubscription);
        }

        MyApplication.saveData(PreferenceKeys.MICROLECTURE_DATA_RELOAD.getKey(), true);
        chapterNameModels.get(position);
        String id = chapterNameModels.get(position).getId();
        chapterId = id;
        String chap = chapterNameModels.get(position).getChapter();
        String introImage = chapterNameModels.get(position).getTeacherIntroImage();
            if (chapterNameModels.get(position).isLocked()) {
                dialog2.show();
            } else {
                MyApplication.saveData(subjectName, position);
                Intent intent = new Intent(CustomChapterListActivity.this, MicrolectureDetailActivity.class);
                if (StringUtils.isNotEmpty(introImage)) {
                    intent.putExtra("IntroImage", introImage);
                }
                if (StringUtils.isNotEmpty(chapterNameModels.get(position).getNotesUrl())) {
                    intent.putExtra("NotesPdfUrl", chapterNameModels.get(position).getNotesUrl());
                }
                intent.putExtra("chapterName", chap);
                intent.putExtra("chapterId", id);
                intent.putExtra("customChapter", true);
                MyApplication.saveData(PreferenceKeys.MICROLECTURE_ID.getKey(), "-1");
                startActivity(intent);
            }
        }

    @Override
    public void onInfoClicked(String imageUrl) {
        showPopupDialog(imageUrl);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialogHome != null) {
            dialogHome.dismiss();
        }
    }

    private void showPopupDialog(String url) {

        dialogHome = new Dialog(CustomChapterListActivity.this);
        dialogHome.setContentView(R.layout.custom_home_dialogue);
        dialogHome.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        dialogHome.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogHome.setCancelable(false);


        CardView cancel = dialogHome.findViewById(R.id.cancel);
        ImageView image = dialogHome.findViewById(R.id.image);

        if (StringUtils.isNotEmpty(url)) {
            Glide.with(this).load(url).into(image);
            dialogHome.show();
        } else
            Glide.with(this).asGif().load(R.raw.image_gif).into(image);

        MyApplication.saveData(PreferenceKeys.POPUP_STORE_TIME.getKey(), System.currentTimeMillis() + 43200000);


        cancel.setOnClickListener(v -> dialogHome.dismiss());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.search_menu_item, menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        SearchView searchView = menuItem.getActionView().findViewById(R.id.search_view);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint("Search Chapter");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (Adapter != null) {
                    Adapter.getFilter().filter(s);
                }
                return false;
            }
        });

        MenuItem item = menu.findItem(R.id.info_item);

        item.setOnMenuItemClickListener(menuItem1 -> {
            String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());

            if (lang == null) {
                lang = "";
            }
            String key = "video_url" + lang;
            String url = MyApplication.getStringData(key) == null ?
                    "https://d3dkidc6rv57u9.cloudfront.net/DemoVideo/demovideo_new/InShot_20221007_153358491%5B2%5D.m3u8"
                    : MyApplication.getStringData(key);

            Intent intent = new Intent(CustomChapterListActivity.this, ExoPlayerActivity.class);
            intent.putExtra(KEY_VIDEO_URI, url);
            startActivity(intent);
            return true;
        });


        return true;
    }

    @Override
    public void onButtonAListener() {
        if (index + 1 < behaviourList.size()) {
            saveList.add(behaviourList.get(index).getBehaviour());
            Behaviour behaviour = behaviourList.get(++index);
            behaviourDialogPopup.setBodyText(behaviour.getQ());
            //behaviourDialogPopup.cancelAnimation();
            if (behaviour.getBehaviour().contains("movie")) {
                behaviourDialogPopup.setAnimationResource(R.raw.movie_lot);
            } else if (behaviour.getBehaviour().contains("sport")) {
                behaviourDialogPopup.setAnimationResource(R.raw.sport);
            } else if (behaviour.getBehaviour().contains("travel")) {
                behaviourDialogPopup.setAnimationResource(R.raw.traveller);
            } else {
                behaviourDialogPopup.setAnimationResource(R.raw.like);
            }
            behaviourDialogPopup.startAnimation();
        } else {
            saveList.add(behaviourList.get(index).getBehaviour());
            behaviourDialogPopup.dismiss();
            saveStudentData();
            Snackbar.make(recyclerView, getString(R.string.thanks_for_response), 3000).show();
        }
    }

    private void saveStudentData() {
        Map<String, Object> map = new HashMap<>();
        map.put(PreferenceKeys.USER_ID.getKey(), MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
        map.put(getString(R.string.behaviour), saveList);
        map.put(getString(R.string.behaviour_not_selected), saveListtNotClicked);

        if (saveList.size() == 0) {
            MyApplication.saveData(getString(R.string.behaviour), 4);
        }

        db.collection("Student")
                .document(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()))
                .set(map, SetOptions.merge()).addOnSuccessListener(documentSnapshot -> {
                    MyApplication.saveStringSet(getString(R.string.behaviour_set), new HashSet<>(saveList));
                    MyApplication.saveStringSet(getString(R.string.behaviour_not_selected), new HashSet<>(saveListtNotClicked));

                }).addOnFailureListener(e -> {

                });
    }

    @Override
    public void onButtonBListener() {
        Behaviour behaviourNotSelected = behaviourList.get(index);
        saveListtNotClicked.add(behaviourNotSelected.getBehaviour());
        if (index + 1 < behaviourList.size()) {
            Behaviour behaviour = behaviourList.get(++index);
            behaviourDialogPopup.setBodyText(behaviour.getQ());
            if (behaviour.getBehaviour().contains("movie")) {
                behaviourDialogPopup.setAnimationResource(R.raw.movie_lot);
            } else if (behaviour.getBehaviour().contains("sport")) {
                behaviourDialogPopup.setAnimationResource(R.raw.sport);
            } else if (behaviour.getBehaviour().contains("travel")) {
                behaviourDialogPopup.setAnimationResource(R.raw.traveller);
            } else {
                behaviourDialogPopup.setAnimationResource(R.raw.like);
            }
            behaviourDialogPopup.startAnimation();
        } else {
            saveStudentData();
            behaviourDialogPopup.dismiss();
        }
    }
}
