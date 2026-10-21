package arivihan.technologies.doubtbuzzter2.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.widget.Toolbar;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.developer.spoti.vspoti.VSpotView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.activity.OnBackPressedCallback;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.ChapterDetailActivityAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.customView.BehaviourDialogPopup;
import arivihan.technologies.doubtbuzzter2.listener.ButtonAListener;
import arivihan.technologies.doubtbuzzter2.listener.ChapterDetailActivityListener;
import arivihan.technologies.doubtbuzzter2.model.Behaviour;
import arivihan.technologies.doubtbuzzter2.model.LectureContentType;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureContentDto;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureInfoDto;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_MICROLECTURE_CONTENT;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

/*
 * File: ChapterDetailActivityFinal Copyright 12/2/21 12:27 AM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 12/2/21 12:27 AM
 * Created by: Sonu Prashant
 *
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

public class ChapterDetailActivityFinal extends BaseActivity implements ChapterDetailActivityListener, ButtonAListener {

    private RecyclerView recyclerView;
    private LottieAnimationView lottieAnimationView;
    private String chapterCompletion,id,selectedChapter, userPoints, userEmail,toolBarTitle;
    private RelativeLayout relativeLayoutConfirmation,relativeLayoutPseudoLiveInfo, relativeLayoutNewLearning;
    private Button letsProgressButton, leaveItButton,pseudoliveInfoGotIt, newLearningGotIt;
    private ImageView whatIsPseudoLiveButton;
    private boolean isFromAdaptor;
    private int isFirstTimeUser;
    private long resumePosition;
    private HashMap<String, String> resumeMap = new HashMap<>();
    private Toolbar toolbar;
    private boolean snackBarShowed;
    private ProgressBar progressBarLinear, progressBarCircular;
    private ProgressDialog dialog;
    private ImageView pseudoliveInfoImage,learningImage;
    private VideoView videoViewPseudoLive;
    private BehaviourDialogPopup behaviourDialogPopup;
    private TextView nextChapter, previousChapter, progressbarText;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<Behaviour> behaviourList = new ArrayList<>();
    private List<String> saveList = new ArrayList<>();
    private List<String> saveListtNotClicked = new ArrayList<>();
    int index = 0;
    ActivityResultLauncher<Intent> mLaunch = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if(result.getData().getStringExtra("chapterCompletion")!=null) {
                        chapterCompletion = result.getData().getStringExtra("chapterCompletion");
                    }
                    else {
                        chapterCompletion="";
                    }

                    isFirstTimeUser = result.getData().getIntExtra("isFirstTimeUser",4);

                    if(result.getData().getStringExtra("selectedChapter")!=null) {
                        selectedChapter = result.getData().getStringExtra("selectedChapter");
                    }
                    else {
                        selectedChapter="";
                    }
                    if(result.getData().getStringExtra("userPoints")!=null) {
                        userPoints = result.getData().getStringExtra("userPoints");
                    }
                    else {
                        userPoints="";
                    }
                    if(result.getData().getStringExtra(getResources().getString(R.string.selected_chpter_id)) != null) {
                        id = result.getData().getStringExtra(getResources().
                                getString(R.string.selected_chpter_id));
                        getData(id);
                    }

                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_detail_final);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (behaviourDialogPopup != null) {
                    behaviourDialogPopup.dismiss();
                }
                finish();
            }
        });
        whatIsPseudoLiveButton = findViewById(R.id.what_is_pseudolive_button);
        recyclerView = findViewById(R.id.chapter_detail_list);
        recyclerView.setAlpha(0.0f);
        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        relativeLayoutPseudoLiveInfo=findViewById(R.id.relativeLayoutPseudoLiveInfo);
        pseudoliveInfoGotIt = findViewById(R.id.pseudolive_info_got_it);
        lottieAnimationView = findViewById(R.id.lottie);
        letsProgressButton = findViewById(R.id.lets_progress);
        pseudoliveInfoImage = findViewById(R.id.pseudolive_info_info_image);
        videoViewPseudoLive = findViewById(R.id.videoViewPseudoLive);
        leaveItButton = findViewById(R.id.leave_it);
        relativeLayoutConfirmation = findViewById(R.id.relativeLayoutConfirmation);
        relativeLayoutNewLearning = findViewById(R.id.relativeLayoutLearning);
        nextChapter = findViewById(R.id.nextChapter);
        previousChapter = findViewById(R.id.previousChapter);
        progressbarText = findViewById(R.id.progressBarText);
        newLearningGotIt = findViewById(R.id.learning_level_got_it);
        learningImage = findViewById(R.id.learningGif);
        progressBarCircular = findViewById(R.id.progressBarLearningCircular);
        progressBarLinear = findViewById(R.id.progressBarLearningLine);
        snackBarShowed=false;

        if(null!=getIntent() && null != getIntent().getExtras() && null!=getIntent().getExtras().get("resumeMap")) {
                resumeMap = (HashMap<String,String>)getIntent().getExtras().get("resumeMap");
        }

        relativeLayoutNewLearning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        newLearningGotIt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relativeLayoutNewLearning.setVisibility(View.GONE);
            }
        });




        Glide.with(this)
                .load(R.drawable.learning_path_white_one)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        if (resource instanceof GifDrawable) {
                            ((GifDrawable) resource).setLoopCount(1);
                        }
                        return false;
                    }
                })
                .into(learningImage);



        isFromAdaptor=false;
        isFromAdaptor = getIntent().getBooleanExtra("isFromAdaptor",false);
        if(getIntent().getStringExtra("chapterCompletion")!=null) {
            chapterCompletion = getIntent().getStringExtra("chapterCompletion");
        }
        else {
            chapterCompletion="";
        }

        isFirstTimeUser = getIntent().getIntExtra("isFirstTimeUser",0);

        if(getIntent().getStringExtra("selectedChapter")!=null) {
            selectedChapter = getIntent().getStringExtra("selectedChapter");
        }
        else {
            selectedChapter="";
        }
        if(getIntent().getStringExtra("userPoints")!=null) {
            userPoints = getIntent().getStringExtra("userPoints");
        }
        else {
            userPoints="";
        }if(getIntent().getStringExtra(getResources().getString(R.string.selected_chpter_id)) != null) {
            id = getIntent().getStringExtra(getResources().
                    getString(R.string.selected_chpter_id));
            getData(id);
        }
        toolbar = (Toolbar) findViewById(R.id.toolbar_chapter_detail);
        toolBarTitle = getProperToolBarTitle(selectedChapter);
        toolbar.setTitle(toolBarTitle);

        SharedPreferences preferences = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        boolean onceShowedChapter = preferences.getBoolean("onceShowedChapter", true);

        if(isFirstTimeUser<3 && !onceShowedChapter) {
            if(isFirstTimeUser==2) {
                new VSpotView.Builder(ChapterDetailActivityFinal.this)
                        .setTitle(getString(R.string.tour_title_pseudolive))
                        .setContentText(getString(R.string.tour_description_pseudolive))
                        .setGravity(VSpotView.Gravity.auto) //optional
                        .setDismissType(VSpotView.DismissType.anywhere) //optional - default dismissable by TargetView
                        .setTargetView(whatIsPseudoLiveButton)
                        .setContentTextSize(14)//optional
                        .setTitleTextSize(18)//optional
                        .setVSpotListener(new VSpotView.VSpotListener() {
                            @Override
                            public void onDismiss(View view) {

                                int tempProgress = progressBarCircular.getProgress();
                                progressBarCircular.setProgress(100);

                                new VSpotView.Builder(ChapterDetailActivityFinal.this)
                                        .setTitle(getString(R.string.progress_title_pseudolive))
                                        .setContentText(getString(R.string.progress_description_pseudolive))
                                        .setGravity(VSpotView.Gravity.auto) //optional
                                        .setDismissType(VSpotView.DismissType.anywhere) //optional - default dismissable by TargetView
                                        .setTargetView(progressBarCircular)
                                        .setContentTextSize(14)//optional
                                        .setTitleTextSize(18)//optional
                                        .setVSpotListener(new VSpotView.VSpotListener() {
                                            @Override
                                            public void onDismiss(View view) {
                                                progressBarCircular.setProgress(tempProgress);
                                                SharedPreferences.Editor editor = getSharedPreferences("Settings",MODE_PRIVATE).edit();
                                                editor.putBoolean("onceShowedChapter",true);
                                                editor.apply();

                                            }
                                        })
                                        .build()
                                        .show();

                            }
                        })
                        .build()
                        .show();
            }
            else {
                new VSpotView.Builder(ChapterDetailActivityFinal.this)
                        .setTitle(getString(R.string.tour_title_pseudolive_two))
                        .setContentText(getString(R.string.tour_description_pseudolive_two))
                        .setGravity(VSpotView.Gravity.auto) //optional
                        .setDismissType(VSpotView.DismissType.anywhere) //optional - default dismissable by TargetView
                        .setTargetView(whatIsPseudoLiveButton)
                        .setContentTextSize(14)//optional
                        .setTitleTextSize(18)//optional
                        .setVSpotListener(new VSpotView.VSpotListener() {
                            @Override
                            public void onDismiss(View view) {

                                int tempProgress = progressBarCircular.getProgress();
                                progressBarCircular.setProgress(100);

                                new VSpotView.Builder(ChapterDetailActivityFinal.this)
                                        .setTitle(getString(R.string.progress_title_pseudolive))
                                        .setContentText(getString(R.string.progress_description_pseudolive))
                                        .setGravity(VSpotView.Gravity.auto) //optional
                                        .setDismissType(VSpotView.DismissType.anywhere) //optional - default dismissable by TargetView
                                        .setTargetView(progressBarCircular)
                                        .setContentTextSize(14)//optional
                                        .setTitleTextSize(18)//optional
                                        .setVSpotListener(new VSpotView.VSpotListener() {
                                            @Override
                                            public void onDismiss(View view) {
                                                progressBarCircular.setProgress(tempProgress);
                                                SharedPreferences.Editor editor = getSharedPreferences("Settings",MODE_PRIVATE).edit();
                                                editor.putBoolean("onceShowedChapter",true);
                                                editor.apply();
                                            }
                                        })
                                        .build()
                                        .show();

                            }
                        })
                        .build()
                        .show();
            }

        }

        whatIsPseudoLiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = "https://firebasestorage.googleapis.com/v0/b/doubtbuzzzter.appspot.com/o/VideoContent%2FWhat%20is%20a%20pseduolive%20lecture.mp4?alt=media&token=3050dfe0-aab1-4f4d-8078-e3ffdc63f9ae";
                Intent mIntent = ExoPlayerActivity.getStartIntent(ChapterDetailActivityFinal.this, path);
                mIntent.putExtra("isFromChapterDetail",true);
                startActivity(mIntent);
            }
        });

        if (MyApplication.getBoolData(getString(R.string.update_behaviour))
                ||  (MyApplication.getStringSet(getString(R.string.behaviour_set)).size() == 0
                && MyApplication.getIntData(getString(R.string.behaviour)) < 3 )) {
            getAndPrepareBehaviourQuestion();
            int value = MyApplication.getIntData(getString(R.string.behaviour));
            MyApplication.saveData(getString(R.string.behaviour), value == -1 ? 1 : value + 1);
        }
    }

    private void getAndPrepareBehaviourQuestion() {
        String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        String langtoAdd="";
        if("hi".equals(lang) || "hi-rIN".equals(lang)) {
            langtoAdd="hindi";
        }
        else if("mr".equals(lang) ) {
            langtoAdd="MR";

        }
        else if("gu".equals(lang) ) {
            langtoAdd="GJ";
        }
        Set<String> behaviourSet = MyApplication.getStringSet(getString(R.string.behaviour_set));
        db.collection("adaptNew")
                .document("questions"+langtoAdd)
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
            behaviourDialogPopup = new BehaviourDialogPopup(ChapterDetailActivityFinal.this);
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

    private void getData(String id) {
        lottieAnimationView.playAnimation();
        lottieAnimationView.setVisibility(View.VISIBLE);
        Map<String, Object> map = new HashMap<>();
        map.put("chapterId", id);
        map.put("personalization", true);
        MyJsonArrayRequest myJsonObjectRequest = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_MICROLECTURE_CONTENT, map), null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Gson gson = new Gson();
                        TypeToken<List<MicroLectureInfoDto>> token = new TypeToken<List<MicroLectureInfoDto>>() {};
                        List<MicroLectureInfoDto> newList = gson.fromJson(response.toString(), token.getType());
                        updateView(newList);
                        lottieAnimationView.cancelAnimation();
                        lottieAnimationView.setVisibility(View.GONE);

                    }
                }, new VolleyGlobalErrorListener(ChapterDetailActivityFinal.this) {
            @Override
            public void postError(VolleyError error) {
                lottieAnimationView.cancelAnimation();
                lottieAnimationView.setVisibility(View.GONE);
                Toast.makeText(ChapterDetailActivityFinal.this,
                        R.string.server_error, Toast.LENGTH_SHORT).show();
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
    }

    private void updateView(List<MicroLectureInfoDto> newList) {
        List<MicroLectureContentDto> list = new ArrayList<>();

        for (MicroLectureInfoDto microLectureInfoDto: newList) {
            if (microLectureInfoDto.getMicroLectureContentDtoList() != null)
                list.addAll(microLectureInfoDto.getMicroLectureContentDtoList());
        }



        if (null!=list && list.size()>0) {

            for (int i=0; i<list.size();i++) {
                if(list.get(i).isHeader()) {

                }
            }

            int total = 0;
            int completed = 0;
            for (int i=0; i<list.size(); i++) {
                total++;
                if(i < chapterCompletion.length() && chapterCompletion.charAt(i)=='1') {
                    list.get(i).setCompleted(true);
                    completed++;
                }
                else {
                    list.get(i).setCompleted(false);
                }
            }

            if(total != 0) {
                int progress = completed*100/total;
                progressBarCircular.setMax(total);
                progressBarLinear.setMax(total);
                progressbarText.setText(String.valueOf(progress)+"%");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progressBarCircular.setProgress(completed,true);
                }
                else {
                    progressBarCircular.setProgress(completed);
                }

                progressBarLinear.animate()
                        .alpha(1.0f)
                        .setDuration(1200);
                progressbarText.animate()
                        .alpha(1.0f)
                        .setDuration(1200);
                progressBarLinear.animate().scaleX(1.1f).setDuration(800);
                progressBarLinear.animate().scaleY(1.1f).setDuration(800);

                progressbarText.animate().scaleX(1.1f).setDuration(800);
                progressbarText.animate().scaleY(1.1f).setDuration(800);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progressBarLinear.setProgress(completed,true);
                }
                else {
                    progressBarLinear.setProgress(completed);
                }

            }

            else {
                relativeLayoutNewLearning.setVisibility(View.GONE);
            }


            if(null != chapterCompletion  && !chapterCompletion.isEmpty()) {
                if (chapterCompletion.length() > 1) {
                    int j = 0;
                    for (int i=0; i<chapterCompletion.length()-1; i++) {
                        j++;
                        if(chapterCompletion.charAt(i) == '0' ) {
                            if(i==0) {
                                previousChapter.setText(getString(R.string.lets_begin)+" "+selectedChapter);
                                nextChapter.setText(list.get(i).getTitle());
                                relativeLayoutNewLearning.setVisibility(View.VISIBLE);
                            }
                            else {
                                previousChapter.setText(list.get(i-1).getTitle());
                                nextChapter.setText(list.get(i).getTitle());
                                relativeLayoutNewLearning.setVisibility(View.VISIBLE);
                            }
                            break;
                        }
                        else {
                            relativeLayoutNewLearning.setVisibility(View.GONE);
                        }
                    }
                    if (j==chapterCompletion.length()-1) {
                        previousChapter.setText(list.get(j).getTitle());
                        nextChapter.setText(list.get(j+1).getTitle());
                        relativeLayoutNewLearning.setVisibility(View.VISIBLE);
                    }
                }

                else {

                    if (chapterCompletion.charAt(0) == '1') {
                        previousChapter.setText(list.get(0).getTitle());
                        nextChapter.setText(list.get(1).getTitle());
                        relativeLayoutNewLearning.setVisibility(View.VISIBLE);
                    }
                    else {
                        previousChapter.setText(getString(R.string.lets_begin)+" "+selectedChapter);
                        nextChapter.setText(list.get(0).getTitle());
                        relativeLayoutNewLearning.setVisibility(View.VISIBLE);

                    }

                }

            }
            else {
                previousChapter.setText(getString(R.string.lets_begin)+" "+selectedChapter);
                nextChapter.setText(list.get(0).getTitle());
                relativeLayoutNewLearning.setVisibility(View.VISIBLE);
            }

            nextChapter.animate().scaleX(1.1f).setDuration(800);
            nextChapter.animate().scaleY(1.1f).setDuration(800);

        }
        else {
            Snackbar.make(recyclerView, getString(R.string.error_fetching_data) , 4000).show();
        }


        ChapterDetailActivityAdapter chapterDetailActivityAdapter = new
                ChapterDetailActivityAdapter(this, list, this, resumeMap, selectedChapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chapterDetailActivityAdapter);
        recyclerView.animate().alpha(1.0f).setDuration(800);


        leaveItButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                relativeLayoutConfirmation.setVisibility(View.GONE);
            }
        });

        pseudoliveInfoGotIt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                relativeLayoutPseudoLiveInfo.setVisibility(View.GONE);
                videoViewPseudoLive.pause();
                videoViewPseudoLive.setVisibility(View.GONE);
            }
        });

        pseudoliveInfoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //Setting MediaController and URI, then starting the videoView
             /*   videoViewPseudoLive.setVideoURI(Uri.parse(path));
                videoViewPseudoLive.start();
                videoViewPseudoLive.seekTo(1);
                videoViewPseudoLive.setKeepScreenOn(true);
                videoViewPseudoLive.setVisibility(View.VISIBLE);*/

            }
        });

        relativeLayoutPseudoLiveInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        /*if(isFromAdaptor) {
            relativeLayoutPseudoLiveInfo.setVisibility(View.VISIBLE);
        }*/
    }

    @Override
    public void goToActivity(final MicroLectureContentDto microLectureContentDto, int position) {

        if (microLectureContentDto.getLectureContentType().
                equals(LectureContentType.NUMERICAL_QUESTION) || microLectureContentDto.
                getLectureContentType().equals(LectureContentType.CONCEPTUAL_QUESTION)) {

            //Launch Question Answer section
//            Intent intent = new Intent(ChapterDetailActivityFinal.this,
//                    QuestionDetailActivityFinal.class);
//            intent.putExtra(getResources().getString(R.string.passed_obj), microLectureContentDto);
//            intent.putExtra(getResources().getString(R.string.position), position);
//            intent.putExtra(getString(R.string.chapterCompletion), chapterCompletion);
//            intent.putExtra(getString(R.string.selected_chpter_id), id);
//            intent.putExtra("userPoints", userPoints);
//            intent.putExtra("selectedChapter", selectedChapter);
//            startActivity(intent);

        }
        else if (microLectureContentDto.getLectureContentType().equals(LectureContentType.VIDEO)
                && microLectureContentDto.getVideoUrl() != null && !"null".equals(microLectureContentDto.getVideoUrl())
                && (!"NaN".equals(microLectureContentDto.getVideoUrl()))
                && (!"N/A".equals(microLectureContentDto.getVideoUrl()))) {

            String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
            if(!snackBarShowed) {
                if("hi".equals(lang) || "hi-rIN".equals(lang) && microLectureContentDto.getVideoUrl().contains("Hindi")) {
                    goToLecture(microLectureContentDto, position);
                }
                else if("mr".equals(lang)  && microLectureContentDto.getVideoUrl().contains("Marathi")) {
                    goToLecture(microLectureContentDto, position);
                }
                else if("gu".equals(lang)  && microLectureContentDto.getVideoUrl().contains("Gujarati")) {
                    goToLecture(microLectureContentDto, position);
                }
                else if("".equals(lang)) {
                    goToLecture(microLectureContentDto, position);
                }
                else {
                    snackBarShowed=true;
                    Snackbar.make(recyclerView, R.string.lecture_not_in_desired_language, 4000).show();
                }
            }
            else {
                goToLecture(microLectureContentDto, position);
            }

        } else if (microLectureContentDto.getLectureContentType().equals(LectureContentType.NOTES)){
            Snackbar.make(recyclerView,getString(R.string.notes_will_be_mailed),4000).show();
        }
        else if (microLectureContentDto.getLectureContentType().equals(LectureContentType.PDF)){
            Snackbar.make(recyclerView,getString(R.string.notes_will_be_mailed),4000).show();
        }
    }

    private void deducePoint(final MicroLectureContentDto microLectureContentDto, int position, String videoId, String userPoints) {

        if(userPoints.isEmpty()) {
            Toast.makeText(ChapterDetailActivityFinal.this,
                    getString(R.string.got_discount), Toast.LENGTH_SHORT).show();
            getVideoResumePosition(microLectureContentDto, position, CommonMethods.modifyString(selectedChapter.toLowerCase() + "_"
                    + microLectureContentDto.getTitle()));
            lottieAnimationView.setVisibility(View.GONE);
            return;
        }

        int points;
        try {
            points = Integer.valueOf(userPoints);
        }
        catch (Exception e) {
            Toast.makeText(ChapterDetailActivityFinal.this,
                    getString(R.string.got_discount), Toast.LENGTH_SHORT).show();
            getVideoResumePosition(microLectureContentDto, position, CommonMethods.modifyString(selectedChapter.toLowerCase() + "_"
                    + microLectureContentDto.getTitle()));
            lottieAnimationView.cancelAnimation();
            lottieAnimationView.setVisibility(View.GONE);
            return;
        }

        if(points < 1) {
            Toast.makeText(ChapterDetailActivityFinal.this,
                    getString(R.string.not_enough_points), Toast.LENGTH_SHORT).show();
            return;
        }
        points=points-1;

        Map<String, Object> data = new HashMap<>();
        data.put("Points", String.valueOf(points));

        db.collection("Student").document(userEmail)
                .set(data, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                getVideoResumePosition(microLectureContentDto, position, CommonMethods.modifyString(selectedChapter.toLowerCase() + "_"
                        + microLectureContentDto.getTitle()));
                lottieAnimationView.cancelAnimation();
                lottieAnimationView.setVisibility(View.GONE);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                getVideoResumePosition(microLectureContentDto, position, CommonMethods.modifyString(selectedChapter.toLowerCase() + "_"
                        + microLectureContentDto.getTitle()));
                lottieAnimationView.cancelAnimation();
                lottieAnimationView.setVisibility(View.GONE);
            }

        });

    }


    private String getProperToolBarTitle(String selectedChapter) {
        String capitalLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String properTitle = selectedChapter.substring(0,1);
        for (int i=1; i<selectedChapter.length()-1; i++) {
            if(capitalLetters.contains(selectedChapter.substring(i,i+1))) {
                properTitle+=" "+selectedChapter.charAt(i);
            }
            else {
                properTitle+=selectedChapter.charAt(i);
            }
        }
        return properTitle+selectedChapter.substring(selectedChapter.length()-1);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (behaviourDialogPopup != null) {
            behaviourDialogPopup.dismiss();
        }
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
            Snackbar.make(recyclerView, getString(R.string.thanks_for_response) , 3000).show();
        }
    }

    private void saveStudentData() {
        Map<String, Object> map = new HashMap<>();
        map.put(getString(R.string.behaviour), saveList);
        map.put(getString(R.string.behaviour_not_selected), saveListtNotClicked);

        if (saveList.size() == 0) {
            MyApplication.saveData(getString(R.string.behaviour), 4);
        }

        db.collection("Student")
                .document(userEmail)
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


    public void getVideoResumePosition(MicroLectureContentDto microLectureContentDto, int position, String videoId) {
        db.collection("Student").document(userEmail).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    if (document.getData().get("zResume"+videoId) != null) {
                        try {
                            resumePosition = Long.parseLong(document.getData().get("zResume"+videoId).toString());
                            @OptIn(markerClass = UnstableApi.class) Intent intent = new Intent(ChapterDetailActivityFinal.this,
                                    VideoPlayerActivity.class);
                            intent.putExtra(getResources().getString(R.string.passed_obj), microLectureContentDto);
                            intent.putExtra(getString(R.string.chapterCompletion), chapterCompletion);
                            intent.putExtra("selectedChapter", selectedChapter);
                            intent.putExtra("userPoints", userPoints);
                            intent.putExtra(getString(R.string.selected_chpter_id), id);
                            intent.putExtra("position", position);
                            intent.putExtra("shouldNotResume", false);
                            intent.putExtra("resumePosition", resumePosition);
                            startActivity(intent);

                        } catch (NumberFormatException e)  {
                            Log.e(getString(R.string.error), e.getMessage());
                            Toast.makeText(ChapterDetailActivityFinal.this,
                                    R.string.error+" "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    else if(document.getData().get(videoId) != null) {
                        try {
                            resumePosition = Long.parseLong(document.getData().get(videoId).toString());
                            @OptIn(markerClass = UnstableApi.class) Intent intent = new Intent(ChapterDetailActivityFinal.this,
                                    VideoPlayerActivity.class);
                            intent.putExtra(getResources().getString(R.string.passed_obj), microLectureContentDto);
                            intent.putExtra(getString(R.string.chapterCompletion), chapterCompletion);
                            intent.putExtra("selectedChapter", selectedChapter);
                            intent.putExtra("userPoints", userPoints);
                            intent.putExtra(getString(R.string.selected_chpter_id), id);
                            intent.putExtra("position", position);
                            intent.putExtra("shouldNotResume", false);
                            intent.putExtra("resumePosition", resumePosition);
                            startActivity(intent);

                        } catch (NumberFormatException e)  {
                            Log.e(getString(R.string.error), e.getMessage());
                            Toast.makeText(ChapterDetailActivityFinal.this,
                                    R.string.error+" "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    else {
                        @OptIn(markerClass = UnstableApi.class) Intent intent = new Intent(ChapterDetailActivityFinal.this,
                                VideoPlayerActivity.class);
                        intent.putExtra(getResources().getString(R.string.passed_obj), microLectureContentDto);
                        intent.putExtra(getString(R.string.chapterCompletion), chapterCompletion);
                        intent.putExtra("selectedChapter", selectedChapter);
                        intent.putExtra("userPoints", userPoints);
                        intent.putExtra(getString(R.string.selected_chpter_id), id);
                        intent.putExtra("position", position);
                        intent.putExtra("shouldNotResume", true);
                        startActivity(intent);
                    }
                }
            }
        });
    }

    private void goToLecture(MicroLectureContentDto microLectureContentDto, int position) {

        if(microLectureContentDto.getVideoUrl()!=null && microLectureContentDto.getVideoUrl().toLowerCase().contains("ml3")
                || microLectureContentDto.getVideoUrl().toLowerCase().contains("ml4")
                || microLectureContentDto.getVideoUrl().toLowerCase().contains("ml5")
                || microLectureContentDto.getVideoUrl().toLowerCase().contains("ml6")
                || microLectureContentDto.getVideoUrl().toLowerCase().contains("ml7")
                || microLectureContentDto.getVideoUrl().toLowerCase().contains("ml8")
                || microLectureContentDto.getVideoUrl().toLowerCase().contains("ml9")) {

            relativeLayoutConfirmation.setVisibility(View.VISIBLE);
            letsProgressButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    lottieAnimationView.playAnimation();
                    lottieAnimationView.setVisibility(View.GONE);
                    relativeLayoutConfirmation.setVisibility(View.GONE);
                    deducePoint(microLectureContentDto, position, CommonMethods.modifyString(selectedChapter.toLowerCase() + "_"
                            + microLectureContentDto.getTitle()),userPoints);
                }
            });
            return;


        }

        getVideoResumePosition(microLectureContentDto, position, CommonMethods.modifyString(selectedChapter.toLowerCase() + "_"
                + microLectureContentDto.getTitle()));

    }

}