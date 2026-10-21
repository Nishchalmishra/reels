package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.activity.ExoPlayerActivity.KEY_VIDEO_URI;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_MICROLECTURE_CONTENT;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.NEW_ACTIVITY_ENABLED;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.QuestionsAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.TopicDetailAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.customView.QuestionDialogPopup;
import arivihan.technologies.doubtbuzzter2.helper.EncryptDecryptServiceImpl;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.listener.ButtonAListener;
import arivihan.technologies.doubtbuzzter2.listener.ChapterDetailActivityListener;
import arivihan.technologies.doubtbuzzter2.model.ApiResult;
import arivihan.technologies.doubtbuzzter2.model.LectureContentType;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureContentDto;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureInfoDto;
import arivihan.technologies.doubtbuzzter2.model.TopicModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

/*
 * File: Chapter_Details_Final2 Copyright 7/12/21 7:50 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 7/12/21 7:50 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

public class MicrolectureDetailActivity extends BaseActivity implements QuestionsAdapter.OnQuestionClick, ChapterDetailActivityListener, TopicDetailAdapter.OnTopicClick, ButtonAListener {

    RecyclerView recyclerView;
    ArrayList<TopicModel> models;
    int expandedPosition = 0;
    TopicDetailAdapter Adapter;
    ShimmerFrameLayout shimmer;
    TextView sub_name;
    LottieAnimationView animation;
    LinearLayout back;
    Toolbar toolbar;
    String id;
    String s, notesUrl;
    Dialog dialog;

    Gson gson = new Gson();
    RelativeLayout ParentLayout;
    MaterialCardView subscriptionLayout;
    private int dataLoaded = 0;
    private int dataLoadedComplete = 0;
    private IntentFilter filter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private QuestionDialogPopup popup;
    private MicroLectureContentDto microLectureContentDto;
    private int position;
    private LottieAnimationView celebration;
    private MenuItem menuItem;
    private String introImage;
    Dialog dialogHome;
    FloatingActionButton fab;
    private boolean customChapter;
    boolean startVideo;
    boolean openQuestion;
    private ProgressDialog loading;
    NetworkStatusLiveData networkStatusLiveData;

    @Override
    public void onResume() {
        super.onResume();

        expandedPosition = MyApplication.getIntData(s);
        if (expandedPosition == -1)
            expandedPosition = 0;
        if (MyApplication.getBoolData(PreferenceKeys.MICROLECTURE_DATA_RELOAD.getKey())) {
            dataLoaded = 1;
            getData(id);
            isNewActivityEnabled();

        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_cahpter_details_final2);
        startVideo = getIntent().getBooleanExtra("startVideo", false);
        openQuestion = getIntent().getBooleanExtra("openQuestion", false);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        recyclerView = findViewById(R.id.recyclerview);
        shimmer = findViewById(R.id.shimmer_topics);
        animation = findViewById(R.id.lotti_no_data);
        back = findViewById(R.id.back_arrow);
        sub_name = findViewById(R.id.activity_title);
        ParentLayout = findViewById(R.id.ParentRelativeLayout);
//        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        celebration = findViewById(R.id.celebration);
        fab = findViewById(R.id.floatingActionButton);

        loading = new ProgressDialog(this);
        loading.setMessage(getString(R.string.loading));
        loading.setCanceledOnTouchOutside(false);
        if (openQuestion || startVideo)
            loading.show();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        s = getIntent().getStringExtra("chapterName");
        id = getIntent().getStringExtra("chapterId");
        String microlectureId = getIntent().getStringExtra(PreferenceKeys.MICROLECTURE_ID.getKey());
        if (StringUtils.isNotEmpty(microlectureId)) {
            MyApplication.saveData(PreferenceKeys.MICROLECTURE_ID.getKey(), microlectureId);
        }
        notesUrl = getIntent().getStringExtra("NotesPdfUrl");

        if (StringUtils.isEmpty(notesUrl)) {
            fab.setVisibility(View.GONE);
        } else {
            fab.setVisibility(View.VISIBLE);
        }

        fab.setOnClickListener(view -> {
//            Intent target = new Intent(this, PdfViewerActivity.class);
//            target.putExtra("pdfUrl", notesUrl);
//            startActivity(target);
        });
        customChapter = getIntent().getBooleanExtra("customChapter", false);
        introImage = getIntent().getStringExtra("IntroImage");

        if (MyApplication.getBoolData(PreferenceKeys.TOPIC_DETAIL_ADAPTER.getKey()) && !MyApplication.getBoolData(s + "ch")) {
            showPopupDialog(introImage);
            MyApplication.saveData(s + "ch", true);
        }
        sub_name.setText(s);

        dialog = new Dialog(MicrolectureDetailActivity.this);
        dialog.setContentView(R.layout.custom_dialog_lock_box);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        subscriptionLayout = dialog.findViewById(R.id.subscriptionLayout);

        subscriptionLayout.setOnClickListener(view -> {
            dialog.cancel();
            dialog.dismiss();

            startActivity(SubscriptionNavigationHelper.getSubscriptionPageIntent(MicrolectureDetailActivity.this, RedirectionSource.MICRO_LECTURE_LOCKED_POPUP));
//                Intent intent = new Intent(MicrolectureDetailActivity.this, CampaignSubscriptionActivity.class);
//                intent.putExtra("plan_level", "COURSE");
//                startActivity(intent);

            ImageView close = dialog.findViewById(R.id.cancel);


            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.micro_lec_img_close_btn_clicked.getActionName());
                    dialog.dismiss();
                }
            });


            // Last open Card and Type-----
            expandedPosition = MyApplication.getIntData(s);

            if (expandedPosition == -1) {
                expandedPosition = 0;
            }

            networkStatusLiveData = new NetworkStatusLiveData(this);
            networkStatusLiveData.observe(this, isOnline -> {
                if (isOnline) {
                    if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                        if (dataLoaded != 1) {
                            dataLoaded = 1;
                            getData(id);
                            isNewActivityEnabled();
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
                        if (dataLoadedComplete == 0)
                            dataLoaded = 0;
                    }
                }
            });


            if (CommonMethods.isOnline(this)) {
                if (dataLoaded != 1) {
                    dataLoaded = 1;
                    getData(id);
                    isNewActivityEnabled();
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
        });

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialogHome != null) {
            dialogHome.dismiss();
        }
    }


    private void isNewActivityEnabled() {
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(NEW_ACTIVITY_ENABLED, null), null,
                response -> {
                    Gson gson = new Gson();
                    TypeToken<ApiResult> token = new TypeToken<ApiResult>() {
                    };
                    ApiResult apiResult = gson.fromJson(response.toString(), token.getType());
                    MyApplication.saveData(PreferenceKeys.NEW_ACTIVITY_ENABLED.getKey(), apiResult.isSuccess());

                }, new VolleyGlobalErrorListener(MicrolectureDetailActivity.this) {
            @Override
            public void postError(VolleyError error) {

                Toast.makeText(MicrolectureDetailActivity.this,
                        R.string.server_error, Toast.LENGTH_SHORT).show();
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
    }

    private void showPopupDialog(String url) {

        dialogHome = new Dialog(MicrolectureDetailActivity.this);
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

        cancel.setOnClickListener(v -> dialogHome.dismiss());
    }


    private void getData(String id) {
        Map<String, Object> map = new HashMap<>();
        map.put("chapterId", id);
        map.put("personalization", true);
        map.put("customChapter", customChapter);
        MyJsonArrayRequest myJsonObjectRequest = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_MICROLECTURE_CONTENT, map), null,
                response -> {
                    MyApplication.saveData(PreferenceKeys.MICROLECTURE_DATA_RELOAD.getKey(), false);
                    TypeToken<List<MicroLectureInfoDto>> token = new TypeToken<List<MicroLectureInfoDto>>() {
                    };
                    List<MicroLectureInfoDto> microLectureInfoDtos = gson.fromJson(response.toString(), token.getType());
                    dataLoadedComplete = 1;
                    updateRecyclerView(microLectureInfoDtos);
                }, new VolleyGlobalErrorListener(MicrolectureDetailActivity.this) {
            @Override
            public void postError(VolleyError error) {
                dataLoaded = 0;
                CommonMethods.showNegativeSnackbar(ParentLayout, getString(R.string.server_error));
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
    }

    private void updateRecyclerView(List<MicroLectureInfoDto> microLectureInfoDtos) {
        recyclerView.setVisibility(View.VISIBLE);
        shimmer.stopShimmer();
        shimmer.setVisibility(View.GONE);
        int scroll_to = -1;
        int index = 0;
        models = new ArrayList<>();
        String message = null;
        boolean personalized = false;
        for (MicroLectureInfoDto microLectureInfoDto : microLectureInfoDtos) {

            if (microLectureInfoDto == null || "Lectures Coming Soon".equalsIgnoreCase(microLectureInfoDto.getTitle())) {
                continue;
            }

            if (microLectureInfoDto.isPersonalized() && !personalized) {
                personalized = true;
                message = microLectureInfoDto.getMessage();
            }

            TopicModel tm = new TopicModel();
            if (index == expandedPosition && MyApplication.getStringData(PreferenceKeys.MICROLECTURE_ID.getKey()).equals("-1")) {
                tm.setExpandable(true);
                scroll_to = index;
            } else {
                tm.setExpandable(false);
            }

            if (MyApplication.getStringData(PreferenceKeys.MICROLECTURE_ID.getKey()) != "-1") {
                if (MyApplication.getStringData(PreferenceKeys.MICROLECTURE_ID.getKey()).equals(microLectureInfoDto.getId())) {
                    tm.setExpandable(true);
                    scroll_to = index;
                }
            }

            tm.setTitle(microLectureInfoDto.getTitle());
            tm.setNoOfLecture("1");
            tm.setLockMessage(microLectureInfoDto.getLockMessage());
            ArrayList<MicroLectureContentDto> lectureContentDtos = new ArrayList<>();
            ArrayList<MicroLectureContentDto> questionsDtos = new ArrayList<>();
            for (MicroLectureContentDto microLectureContentDto : microLectureInfoDto.getMicroLectureContentDtoList()) {

                if (microLectureContentDto.getLectureContentType().getType().equalsIgnoreCase(LectureContentType.VIDEO.getType())) {
                    if (microLectureContentDto.isEncrypted()) {
                        String decContentUrl = EncryptDecryptServiceImpl.decrypt(microLectureContentDto.getContentUrl());
                        if (decContentUrl != null) microLectureContentDto.setContentUrl(decContentUrl);
                        String decVideoUrl = EncryptDecryptServiceImpl.decrypt(microLectureContentDto.getVideoUrl());
                        if (decVideoUrl != null) microLectureContentDto.setVideoUrl(decVideoUrl);
                    }
                    microLectureContentDto.setHeader_title(microLectureInfoDto.getTitle());
                    lectureContentDtos.add(microLectureContentDto);
                } else if (microLectureContentDto.getLectureContentType().getType().equalsIgnoreCase(LectureContentType.NUMERICAL_QUESTION.getType()) ||
                        microLectureContentDto.getLectureContentType().getType().equalsIgnoreCase(LectureContentType.CONCEPTUAL_QUESTION.getType())) {
                    if (microLectureContentDto.isEncrypted()) {
                        String decVideoUrl = EncryptDecryptServiceImpl.decrypt(microLectureContentDto.getVideoUrl());
                        if (decVideoUrl != null) microLectureContentDto.setVideoUrl(decVideoUrl);
                    }
                    microLectureContentDto.setHeader_title(microLectureInfoDto.getTitle());
                    questionsDtos.add(microLectureContentDto);
                } else if (microLectureContentDto.getLectureContentType().getType().equalsIgnoreCase(LectureContentType.NOTES.getType())) {
                    //TODO have to add
                }
            }

            tm.setNoOfLecture(lectureContentDtos.size() + "");
            tm.setNoOfQuestions(questionsDtos.size() + "");
            tm.setLecturesDtos(lectureContentDtos);
            tm.setQuestionsDtos(questionsDtos);
            tm.setProgress_value(String.valueOf(microLectureInfoDto.getProgress()));
            tm.setLocked(microLectureInfoDto.isLocked());
//            Log.i( "ARIVIHANMICROLECTURE", tm.toString());
            models.add(tm);

            index++;
        }

        if (personalized) {
            celebration.setVisibility(View.VISIBLE);
            celebration.playAnimation();

            if (StringUtils.isNotEmpty(message)) {
                CommonMethods.showPositiveSnackbar(ParentLayout, message);
            }
        }


        recyclerView.setVisibility(View.VISIBLE);
        shimmer.stopShimmer();
        shimmer.setVisibility(View.GONE);

        if (models.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            animation.setVisibility(View.VISIBLE);
        } else {
            Adapter = new TopicDetailAdapter(models, this, this, this, s, this);
            recyclerView.scrollToPosition(scroll_to);
            recyclerView.setAdapter(Adapter);
            Adapter.notifyDataSetChanged();
        }

        if (startVideo) {
            startVideo = false;
            if (scroll_to > -1 && microLectureInfoDtos.get(scroll_to) != null &&
                    !CollectionUtils.isEmpty(microLectureInfoDtos.get(scroll_to).getMicroLectureContentDtoList())) {
                MicroLectureInfoDto microLectureInfoDto = microLectureInfoDtos.get(scroll_to);
                loading.hide();
                if (microLectureInfoDto.isLocked()) {
                    TextView msg = dialog.findViewById(R.id.header);

                    if (microLectureInfoDto.getLockMessage() != null) {
                        if (!microLectureInfoDto.getLockMessage().isEmpty()) {
                            msg.setText(microLectureInfoDto.getLockMessage());
                        }
                    } else {
                        msg.setText(R.string.OOpsThisMicroIsLocked);
                    }

                    dialog.show();
                } else {
                    switchToVideo(microLectureInfoDto.getMicroLectureContentDtoList().get(0), scroll_to);
                }
            }
        }

        if (openQuestion) {
            String qnsId = getIntent().getStringExtra("qnsId");
            openQuestion = false;


            for (MicroLectureInfoDto microLectureInfoDto : microLectureInfoDtos) {
                for (MicroLectureContentDto lectureContentDto : microLectureInfoDto.getMicroLectureContentDtoList()) {
                    Log.d("lectureContentDto", "ID checking : " + qnsId + " with  = " + lectureContentDto.getQuestionId());
                    if (lectureContentDto.getQuestionId() != null &&
                            lectureContentDto.getQuestionId().equalsIgnoreCase(qnsId)) {
                        Log.d("lectureContentDto", "ID matched ");

                        openQuestion(scroll_to, lectureContentDto.getTitle(), lectureContentDto, false);
                        break;
                    }
                }
            }
        }

    }


    @Override
    public void onquestionclick(int position, String title) {
        int i = 0;
        MyApplication.saveData(s + PreferenceKeys.TYPE.getKey(), 1);
        ArrayList<String> optionList = new ArrayList<>();
        for (i = 0; i < models.size(); i++) {
            if (models.get(i).getTitle().equals(title))
                break;
        }
        TopicModel topicModel = models.get(i);
        MicroLectureContentDto MLCD = topicModel.getQuestionsDtos().get(position);
        openQuestion(position, title, MLCD, true);
    }

    private void openQuestion(int position, String title, MicroLectureContentDto MLCD, boolean openNext) {
        loading.hide();
        int i;
        Intent intent = new Intent(MicrolectureDetailActivity.this, QuestionActivity.class);
        String p = (position + 1) + "";
        if (MLCD.getQuestion() == null)
            CommonMethods.showNegativeSnackbar(ParentLayout, getString(R.string.NoValidQuestionFound));

        else {
            intent.putExtra(getResources().getString(R.string.passed_obj), MLCD);
            intent.putExtra(PreferenceKeys.POSITION.getKey(), p);
            intent.putExtra("Title", title);
            intent.putExtra("Chapter", s);
            intent.putExtra("Id", id);
            intent.putExtra("openNext", openNext);
            intent.putExtra("Question", MLCD.getQuestion().trim());
            if (MLCD.getAnswerEx() == null) {
                intent.putExtra("Explanation", "");
            } else {
                intent.putExtra("Explanation", MLCD.getAnswerEx().trim());
            }
            ArrayList<String> optionList = new ArrayList<>();
            for (i = 0; i < MLCD.getOptions().size(); i++) {
                optionList.add(MLCD.getOptions().get(i).trim());
            }
            intent.putStringArrayListExtra("Options", optionList);
            if (MLCD.getContentUrl() != null)
                intent.putExtra("ImageQuestion", MLCD.getContentUrl());
            else
                intent.putExtra("ImageQuestion", "");
            if (MLCD.getAnswer() == null)
                CommonMethods.showNegativeSnackbar(ParentLayout, getString(R.string.NoValidQuestionFound) + MLCD.getAnswer());
            else
                intent.putExtra("Answer", MLCD.getAnswer().trim());

            String url = MLCD.getAnswerSolImage();
            if (url != null)
                intent.putExtra("AnswerImage", url);
            else
                intent.putExtra("AnswerImage", "");
            // for further questions---
            intent.putExtra("Subject", id);
            intent.putExtra("Chapter", sub_name.getText().toString());

            if (MLCD.getQuestion() == null || optionList.size() == 0)
                CommonMethods.showNegativeSnackbar(ParentLayout, getString(R.string.NoValidQuestionFound));
            else {
                startActivity(intent);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.search_menu_item, menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        SearchView searchView = menuItem.getActionView().findViewById(R.id.search_view);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint("Search Topic");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (models != null && models.size() != 0)
                    Adapter.getFilter().filter(s);
                return false;
            }
        });

        menuItem = menu.findItem(R.id.info_item);

        menuItem.setOnMenuItemClickListener(menuItem1 -> {
            String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());

            if (lang == null) {
                lang = "";
            }
            String key = "video_url" + lang;
            String url = MyApplication.getStringData(key) == null ?
                    "https://d3dkidc6rv57u9.cloudfront.net/DemoVideo/demovideo_new/InShot_20221007_153358491%5B2%5D.m3u8"
                    : MyApplication.getStringData(key);

            Intent intent = new Intent(MicrolectureDetailActivity.this, ExoPlayerActivity.class);
            intent.putExtra(KEY_VIDEO_URI, url);
            startActivity(intent);
            return true;
        });


        return true;
    }

    public void switchToVideo(MicroLectureContentDto microLectureContentDto, int position) {
        if (popup != null && popup.isShowing()) {
            popup.cancel();
        }
        MyApplication.saveData(s + PreferenceKeys.TYPE.getKey(), 0);
        MyApplication.saveData(PreferenceKeys.HOME_DASHBOARD_DATA_RELOAD.getKey(), true);
        MyApplication.saveData(PreferenceKeys.MICROLECTURE_DATA_RELOAD.getKey(), true);
        @OptIn(markerClass = UnstableApi.class) Intent intent = new Intent(MicrolectureDetailActivity.this,
                VideoPlayerActivity.class);
        intent.putExtra(getResources().getString(R.string.passed_obj), microLectureContentDto);
        intent.putExtra("position", position);
        if (microLectureContentDto.getResumePosition() != microLectureContentDto.getDefaultStartTime())
            intent.putExtra("shouldNotResume", false);
        else
            intent.putExtra("shouldNotResume", true);
        intent.putExtra("chapterId", id);
        intent.putExtra("resumePosition", microLectureContentDto.getResumePosition());
        intent.putExtra(getString(R.string.selected_chpter_id), microLectureContentDto.getId());
        startActivity(intent);
        CommonMethods.updateUnwatched(id);
    }

    @Override
    public void goToActivity(MicroLectureContentDto microLectureContentDto, int position) {
        this.microLectureContentDto = microLectureContentDto;
        this.position = position;
        if (!microLectureContentDto.isConverted()) {
            showPopup();
        } else {
            switchToVideo(microLectureContentDto, position);
        }
    }

    private void showPopup() {
        popup = new QuestionDialogPopup(MicrolectureDetailActivity.this);
        popup.show();
        popup.setCancelable(false);
        popup.setButtonAText(getString(R.string.text_continue));
        popup.setButtonBText(getString(R.string.Cancel));
        popup.onButtonAClickListener(this);
        popup.setBodyText(getString(R.string.available_in_language) +
                getString(R.string.hinglish_language));
        popup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setAnimationResource(R.raw.multi_language);
    }

    @Override
    public void ontopicClick(int position) {
        TopicModel topicModel = models.get(position);
        if (topicModel.getLocked()) {
            TextView msg = dialog.findViewById(R.id.header);

            if (topicModel.getLockMessage() != null) {
                if (!topicModel.getLockMessage().isEmpty()) {
                    msg.setText(topicModel.getLockMessage());
                }
            } else {
                msg.setText(R.string.OOpsThisMicroIsLocked);
            }

            dialog.show();
        }
    }


    @Override
    public void onFinish() {
        View view = findViewById(R.id.info_item);
//
//        TapTargetSequence tapTargetSequence = new TapTargetSequence(this).targets(
//                TapTarget.forView(view, getString(R.string.info_video_title),
//                                getString(R.string.info_video_body))
//                        .outerCircleColor(R.color.arivihan_primary_color)
//                        .outerCircleAlpha(0.96f)
//                        .targetCircleColor(R.color.navy_blue)
//                        .titleTextSize(18)
//                        .titleTextColor(R.color.white)
//                        .descriptionTextSize(15)
//                        .titleTypeface(Typeface.DEFAULT_BOLD)
//                        .descriptionTextColor(R.color.white)
//                        .textColor(R.color.white)
//                        .textTypeface(Typeface.SANS_SERIF)
//                        .dimColor(R.color.black)
//                        .drawShadow(true)
//                        .cancelable(false)
//                        .tintTarget(true)
//                        .transparentTarget(true)
//                        .targetRadius(60)).listener(new TapTargetSequence.Listener() {
//            @Override
//            public void onSequenceFinish() {
//                MyApplication.saveData(PreferenceKeys.TOPIC_DETAIL_ADAPTER.getKey(), true);
//                showPopupDialog(introImage);
//                MyApplication.saveData(s + "ch", true);
//
//            }
//
//            @Override
//            public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
//
//
//            }
//
//            @Override
//            public void onSequenceCanceled(TapTarget lastTarget) {
//            }
//        });

        // tapTargetSequence.start();
//        boolean isFromToDoListDialog = getIntent().getBooleanExtra("redirected_from_dialog", false);
//        if (isFromToDoListDialog) {
//            tapTargetSequence.cancel();
//            Log.i("ARIVIHANLOGTEST","FROM DIALOG");
//        } else {
//        }

    }

    @Override
    public void onButtonAListener() {
        switchToVideo(microLectureContentDto, position);
    }

    @Override
    public void onButtonBListener() {
        popup.cancel();
    }
}