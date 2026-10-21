package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_CHAPTER_OF_SUBJECT;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.ActivityLauncher;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.ChapterNameAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.customView.BehaviourDialogPopup;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.listener.ButtonAListener;
import arivihan.technologies.doubtbuzzter2.listener.SubscriptionCallback;
import arivihan.technologies.doubtbuzzter2.model.Behaviour;
import arivihan.technologies.doubtbuzzter2.model.ChapterListInfoDto;
import arivihan.technologies.doubtbuzzter2.model.ChapterNameModel;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionCheckDto;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class ChapterListActivity extends
        BaseActivity implements ChapterNameAdapter.OnChapClickListener, ButtonAListener {

    RecyclerView recyclerView;
    ShimmerFrameLayout shimmer;
    TextView sub_name;
    RelativeLayout ParentLayout;
    MaterialCardView subscriptionLayout;
    ChapterNameAdapter Adapter;
    List<ChapterNameModel> chapterNameModelsAll;
    LinearLayout back;
    Dialog dialog2;
    String subjectName = "";
    String boardCode;

    TextView chapterScreenTitle;
    public int dataLoaded = 0;
    public int dataLoadedCompleted = 0;
    private IntentFilter filter;
    private String chapterId;
    private boolean isCompleted;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<Behaviour> behaviourList = new ArrayList<>();
    private List<String> saveList = new ArrayList<>();
    private List<String> saveListtNotClicked = new ArrayList<>();
    private BehaviourDialogPopup behaviourDialogPopup;
    private String subjectId;
    int index = 0;
    Dialog dialogHome;
    String subjectCode;

    SearchView searchView;
    boolean hasSearchText;

    ImageView filterImage;

    View filterView;

    LinearLayout filterViewBackground, noResultFoundLayout;
    LinearLayout backArrow;
    boolean filterVisible;

    CheckBox all;
    CheckBox syllabus;
    CheckBox solvedPapers;
    CheckBox clearAll;
    CardView apply;
    List<ChapterNameModel> chapterNameModels = new ArrayList<>();
    String filterStatus;
    boolean preExamPreparation;
    private final String TAG = ChapterListActivity.class.getSimpleName();
    NetworkStatusLiveData networkStatusLiveData;
    private TextView lockDialogText, lockDialogBtnText, lockDialogTitle;
    private Map<String, Object> activityParamsMap;
    LottieAnimationView lockAnimation;
    private LinearLayout liveClassEmptyLayout;


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
        setContentView(R.layout.activity_chapter_list);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (filterVisible) {
                    filterVisible = false;
                    filterView.setVisibility(View.GONE);
                    filterViewBackground.setVisibility(View.GONE);
                } else if (!searchView.isIconified()) {
                    searchView.setIconified(true);
                } else {
                    finish();
                }
            }
        });

        noResultFoundLayout = findViewById(R.id.no_result_found_layout);
        liveClassEmptyLayout = findViewById(R.id.live_class_empty_layout);

        shimmer = findViewById(R.id.shimmer_chapters);
        shimmer.startShimmer();

        back = findViewById(R.id.back_button_layout);
        sub_name = findViewById(R.id.activity_title);
        ParentLayout = findViewById(R.id.ParentLayout);
        recyclerView = findViewById(R.id.chapter_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Uri data = getIntent().getData();

        if (data != null) {
            // From Deep Link Parameters
            preExamPreparation = data.getBooleanQueryParameter("preExamPreparation", false);
            subjectName = data.getQueryParameter("SubjectName");
            subjectId = data.getQueryParameter("SubjectId");
            subjectCode = data.getQueryParameter("SubjectCode");
            boardCode = data.getQueryParameter(PreferenceKeys.BOARD_CODE.getKey());
        } else {
            // From Intent Extras
            preExamPreparation = getIntent().getBooleanExtra("preExamPreparation", false);
            subjectName = getIntent().getStringExtra("SubjectName");
            subjectId = getIntent().getStringExtra("SubjectId");
            subjectCode = getIntent().getStringExtra("SubjectCode");
            boardCode = getIntent().getStringExtra(PreferenceKeys.BOARD_CODE.getKey());
        }

        Log.d(TAG, "preExamPreparation:" + preExamPreparation + ", subjectName:" + subjectName + ", subjectId:" + subjectId + ", subjectCode:" + subjectCode + ", boardCode:" + boardCode);

//        preExamPreparation = getIntent().getBooleanExtra("preExamPreparation",false);
//        subjectName = getIntent().getStringExtra("SubjectName");
//        subjectId = getIntent().getStringExtra("SubjectId");
//        subjectCode = getIntent().getStringExtra("SubjectCode");
//        boardCode = getIntent().getStringExtra(PreferenceKeys.BOARD_CODE.getKey());

        if (StringUtils.isEmpty(boardCode)) {
            boardCode = MyApplication.getStringData(PreferenceKeys.BOARD_CODE.getKey());
        }
        sub_name.setText(subjectName);


        filterImage = findViewById(R.id.filter_chapter);
        filterImage.setOnClickListener(v -> {
            filterVisible = true;
            filterView.setVisibility(View.VISIBLE);
            filterViewBackground.setVisibility(View.VISIBLE);
            Map<String, String> params = new HashMap<>();
            params.put("screen", ChapterListActivity.class.getSimpleName());
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.filter_clicked.getActionName(), params);
        });
        filterView = findViewById(R.id.filter_view_layout);
        all = filterView.findViewById(R.id.filter_ALL);
        syllabus = filterView.findViewById(R.id.filter_syllabus);
        solvedPapers = filterView.findViewById(R.id.filter_solved_papers);
        clearAll = filterView.findViewById(R.id.clear);
        clearAll.setOnClickListener(view12 -> {
            if (clearAll.isChecked()) {
                all.setChecked(false);
                syllabus.setChecked(false);
                solvedPapers.setChecked(false);
            }
        });
        apply = filterView.findViewById(R.id.Apply_Filter_Card);
        apply.setOnClickListener(v -> {
            filterViewBackground.setVisibility(View.GONE);
            filterStatus = "";
            filterView.setVisibility(View.GONE);
            if (all.isChecked()) {
                filterStatus += "";
            }
            if (syllabus.isChecked()) {
                filterStatus += "SYLLABUS" + " ,";
            }
            if (solvedPapers.isChecked()) {
                filterStatus += "SOLVED_PAPERS" + " ,";
            }
            if (clearAll.isChecked()) {
                filterStatus = "";
            }
            if (!CollectionUtils.isEmpty(chapterNameModelsAll))
                applyFilter();
        });

        backArrow = filterView.findViewById(R.id.back_arrow);
        filterViewBackground = findViewById(R.id.filter_view_background);
        backArrow.setOnClickListener(view -> {
            filterVisible = false;
            filterViewBackground.setVisibility(View.GONE);
            filterView.setVisibility(View.GONE);
        });

        filterViewBackground.setOnClickListener(v -> {
            filterVisible = false;
            filterViewBackground.setVisibility(View.GONE);
            filterView.setVisibility(View.GONE);
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }

        });


        dialog2 = new Dialog(ChapterListActivity.this);
        dialog2.setContentView(R.layout.custom_dialog_lock_box);
        dialog2.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        dialog2.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog2.setCancelable(false);
        subscriptionLayout = dialog2.findViewById(R.id.subscriptionLayout);

        subscriptionLayout.setOnClickListener(view -> {
            if (isCompleted) {
                dialog2.cancel();
                dialog2.dismiss();
                SubscriptionNavigationHelper.navigateToSubscriptionPage(this, RedirectionSource.CHAPTER_LIST_JOIN);
//                Intent intent;
//                if (preExamPreparation) {
//                    intent = new Intent(ChapterListActivity.this, CampaignSubscriptionActivity.class);
////                    intent.putExtra("preExamPreparation", true);
//                } else {
//                    intent = new Intent(ChapterListActivity.this, CampaignSubscriptionActivity.class);
////                    intent.putExtra("plan_level", "COURSE");
//                }
//                startActivity(intent);
            } else
                dialog2.dismiss();
        });

        ImageView cut2 = dialog2.findViewById(R.id.cancel);

        cut2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog2.dismiss();
            }
        });

        searchView = findViewById(R.id.search_view);
        setSearchView();
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

        initializeDialog(null);

        MyApplication.getSubscriptionStatus(this, new SubscriptionCallback() {
            @Override
            public void onSuccess(SubscriptionCheckDto dto) {
                Log.d(TAG, "onSuccess API call: " + dto);
                updateDialogContent(dto);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("API_ERROR", errorMessage);
                updateDialogContent(null); // or show some fallback text
            }
        });
    }

    private void applyFilter() {
        chapterNameModels.clear();
        if (filterStatus.isEmpty()) {
            chapterNameModels.addAll(chapterNameModelsAll);
        } else {
            for (int i = 0; i < chapterNameModelsAll.size(); i++) {

                if (chapterNameModelsAll.get(i).getType() != null) {
                    if (filterStatus != null && filterStatus.contains(chapterNameModelsAll.get(i).getType())) {
                        chapterNameModels.add(chapterNameModelsAll.get(i));
                    }
                }

            }
        }
        Adapter.notifyDataSetChanged();
    }

    private void setSearchView() {
        searchView = findViewById(R.id.search_view);
        searchView.setQueryHint(getResources().getString(R.string.search));
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasSearchText) {
                    hasSearchText = false;
                    searchView.setQuery("", true);
                } else if (!hasFocus) {
                    searchView.setIconified(true);
                }
            }
        });

        ImageView searchButton = searchView.findViewById(R.id.search_button);
        searchButton.setOnClickListener(v -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.search_btn_clicked.getActionName());
            searchView.setIconified(false);
            searchView.setMaxWidth(Integer.MAX_VALUE);
            sub_name.setVisibility(View.GONE);
        });
        searchView.setOnCloseListener(() -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.search_close_btn_clicked.getActionName());
            sub_name.setVisibility(View.VISIBLE);
            return false;
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.length() > 0)
                    hasSearchText = true;
                else
                    hasSearchText = false;

                if (Adapter != null) {
                    Adapter.getFilter().filter(s);
                }

                return false;
            }
        });
    }


    private void initializeDialog(SubscriptionCheckDto dto) {
        dialog2 = new Dialog(ChapterListActivity.this);
        dialog2.setContentView(R.layout.custom_dialog_lock_box);
        dialog2.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        dialog2.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog2.setCancelable(false);

        subscriptionLayout = dialog2.findViewById(R.id.subscriptionLayout);
        lockDialogText = dialog2.findViewById(R.id.header);
        lockDialogBtnText = dialog2.findViewById(R.id.subs);
        lockAnimation = dialog2.findViewById(R.id.dialog_animation);
        lockDialogTitle = dialog2.findViewById(R.id.dialog_title);
        subscriptionLayout.setOnClickListener(view -> handleSubscriptionClick(dto));
        ImageView cut2 = dialog2.findViewById(R.id.cancel);
        cut2.setOnClickListener(v -> closeDialog());
    }

    private void updateDialogContent(SubscriptionCheckDto dto) {
        if (dto != null &&
                !MyApplication.getFirebaseRemoteConfig().getBoolean("to_show_lock_msg_from_data_side_in_chapter")) {
            if (dto.getDialogText() != null) lockDialogText.setText(dto.getDialogText());
            if (dto.getButtonText() != null) lockDialogBtnText.setText(dto.getButtonText());
            if (StringUtils.isNotEmpty(dto.getDialogTitle())) {
                lockDialogTitle.setVisibility(View.VISIBLE);
                lockDialogTitle.setText(dto.getDialogTitle());
            }

            activityParamsMap = dto.getActivityParams();
            if (activityParamsMap != null && activityParamsMap.containsKey("navigate_to")) {
                Object navigateToValue = activityParamsMap.get("navigate_to");
                if ("home_screen".equalsIgnoreCase(String.valueOf(navigateToValue))) {
                    lockAnimation.setAnimation(R.raw.alert);
                }
            }
        }
        subscriptionLayout.setOnClickListener(view -> handleSubscriptionClick(dto));
    }


    private void handleSubscriptionClick(SubscriptionCheckDto dto) {
        if (isCompleted) {
            closeDialog();
            Intent intent = null;
            if (dto != null && dto.getClassName() != null && !dto.getClassName().isEmpty()) {
                try {
                    intent = new Intent(ChapterListActivity.this, ActivityLauncher.resolveClass(dto.getClassName()));
                } catch (ClassNotFoundException e) {
                    Log.e("SubscriptionClick", "Class not found: " + dto.getClassName(), e);
                }
            }

            if (intent == null) {
                intent = SubscriptionNavigationHelper.getSubscriptionPageIntent(this, RedirectionSource.CHAPTER_LIST_LOCKED_POPUP);
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
        } else {
            closeDialog();
        }
    }

    private void closeDialog() {
        if (dialog2 != null && dialog2.isShowing()) {
            dialog2.dismiss();
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
            behaviourDialogPopup = new BehaviourDialogPopup(ChapterListActivity.this);
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
        if (StringUtils.isNotEmpty(subjectId)) {
            Map<String, Object> map = new HashMap<>();
            Log.d(PreferenceKeys.BOARD_CODE.getKey(), "data " + boardCode);
            if (StringUtils.isNotEmpty(boardCode)) {
                map.put(PreferenceKeys.BOARD_CODE.getKey(), boardCode);
            }
            MyJsonArrayRequest myJsonObjectRequest = new MyJsonArrayRequest(Request.Method.GET,
                    CommonMethods.getUrlParam(GET_CHAPTER_OF_SUBJECT.replace("{id}", subjectId), map), null,
                    response -> {
                        Gson gson = new Gson();
                        TypeToken<List<ChapterListInfoDto>> token = new TypeToken<List<ChapterListInfoDto>>() {
                        };
                        dataLoadedCompleted = 1;
                        List<ChapterListInfoDto> chapterListInfoDtos = gson.fromJson(response.toString(), token.getType());
                        Log.d("chapterList", "data " + chapterListInfoDtos.toString());
                        updateDataInRecyclerView(chapterListInfoDtos);
                    }, new VolleyGlobalErrorListener(ChapterListActivity.this) {
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
    }


    private void updateDataInRecyclerView(List<ChapterListInfoDto> chapterListInfoDtos) {
        shimmer.stopShimmer();
        shimmer.setVisibility(View.GONE);
        if(chapterListInfoDtos == null || chapterListInfoDtos.isEmpty()){
            recyclerView.setVisibility(View.GONE);
            liveClassEmptyLayout.setVisibility(View.VISIBLE);
            noResultFoundLayout.setVisibility(View.GONE);
            return;
        }
        liveClassEmptyLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        chapterNameModelsAll = new ArrayList<>();
        ChapterNameModel cm;
        for (ChapterListInfoDto chapterInfo : chapterListInfoDtos) {
            cm = ChapterNameModel
                    .builder()
                    .id(chapterInfo.getChapterId())
                    .newLecture(chapterInfo.isNewLecture())
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
                    .type(chapterInfo.getType())
                    .build();
            chapterNameModelsAll.add(cm);
        }
        chapterNameModels.clear();
        chapterNameModels.addAll(chapterNameModelsAll);

        Adapter = new ChapterNameAdapter(this, chapterNameModels, this, subjectName, noResultFoundLayout,recyclerView);
        recyclerView.setAdapter(Adapter);
        Adapter.notifyDataSetChanged();
    }


    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onChapClick(int position) {
        Adapter.notifyDataSetChanged();
//        TextView msg = dialog2.findViewById(R.id.header);
//        TextView button = dialog2.findViewById(R.id.subs);
        try {
            if (position >= 0 && chapterNameModels.get(position) != null) {
                if (chapterNameModels.get(position).getLockMessage() != null &&
                        MyApplication.getFirebaseRemoteConfig().
                                getBoolean("to_show_lock_msg_from_data_side_in_chapter")) {
                    if (!chapterNameModels.get(position).getLockMessage().isEmpty()) {
                        lockDialogText.setText(chapterNameModels.get(position).getLockMessage());
                    } else {
                        lockDialogText.setText(getString(R.string.OOpsThisChapIsLocked));
                    }
                }


                isCompleted = chapterNameModels.get(position).isCompleted();
                if (!isCompleted && MyApplication.getFirebaseRemoteConfig().
                        getBoolean("to_show_lock_msg_from_data_side_in_chapter"))
                    lockDialogBtnText.setText(getResources().getString(R.string.go_back_to_lecture));

                MyApplication.saveData(PreferenceKeys.MICROLECTURE_DATA_RELOAD.getKey(), false);
                ChapterNameModel chapterNameModel = chapterNameModels.get(position);
                String id = chapterNameModels.get(position).getId();
                chapterId = id;
                String chap = chapterNameModels.get(position).getChapter();
                String introImage = chapterNameModels.get(position).getTeacherIntroImage();
                if (chapterNameModels.get(position).isLocked()) {
                    dialog2.show();
                } else {
                    MyApplication.saveData(subjectName, position);
                    Intent intent = new Intent(ChapterListActivity.this, MicrolectureListActivity.class);
                    if (StringUtils.isNotEmpty(introImage)) {
                        intent.putExtra("IntroImage", introImage);
                    }
                    if (StringUtils.isNotEmpty(chapterNameModels.get(position).getNotesUrl())) {
                        intent.putExtra("NotesPdfUrl", chapterNameModels.get(position).getNotesUrl());
                    }
                    intent.putExtra("chapterName", chap);
                    intent.putExtra("subjectCode", subjectCode);
                    intent.putExtra("chapterId", id);
                    MyApplication.saveData(PreferenceKeys.MICROLECTURE_ID.getKey(), "-1");
                    intent.putExtra("selectedSubject", subjectName);

                    HashMap<String, String> params = new HashMap<>();
                    params.put("Chapter_Name", chap);
                    params.put("Subject_Code", subjectCode);
                    params.put("Chapter_Id", id);
                    params.put("screen", ChapterListActivity.class.getSimpleName());

                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.Chapter_clicked.getActionName(), params);
                    startActivity(intent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInfoClicked(String imageUrl) {
        showPopupDialog(imageUrl);
    }

    @Override
    public void onDestroy() {
        MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll(TAG);
        super.onDestroy();
        if (dialogHome != null) {
            dialogHome.dismiss();
        }
    }

    private void showPopupDialog(String url) {

        dialogHome = new Dialog(ChapterListActivity.this);
        dialogHome.setContentView(R.layout.custom_home_dialogue);
        dialogHome.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        dialogHome.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogHome.setCancelable(false);


        CardView cancel = dialogHome.findViewById(R.id.cancel);
        ImageView image = dialogHome.findViewById(R.id.image);

        if (StringUtils.isNotEmpty(url)) {

            try {
                Glide.with(this).load(url).into(image);
                dialogHome.show();
//                Picasso
//                        .get()
//                        .load(url)
//                        .placeholder(R.drawable.image_loader)
//                        .into(image, new Callback() {
//                            @Override
//                            public void onSuccess() {
//                                Log.d(PreferenceKeys.POPUP_STORE_TIME.getKey(), "onResourceReady ");
//
//                                try {
//                                } catch (Exception e) {
//                                    Log.d("Error", "error");
//                                }
//                            }
//
//                            @Override
//                            public void onError(Exception e) {
//                                Log.d(PreferenceKeys.POPUP_STORE_TIME.getKey(), "onLoadFailed: ");
//
//                            }
//                        });
            } catch (Exception e) {
                Glide.with(this).asGif().load(R.raw.image_gif).into(image);
            }

        } else
            Glide.with(this).asGif().load(R.raw.image_gif).into(image);

        MyApplication.saveData(PreferenceKeys.POPUP_STORE_TIME.getKey(), System.currentTimeMillis() + 43200000);


        cancel.setOnClickListener(v -> {
            dialogHome.dismiss();
        });
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
