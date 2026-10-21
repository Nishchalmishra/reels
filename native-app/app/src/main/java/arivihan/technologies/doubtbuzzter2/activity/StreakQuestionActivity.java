package arivihan.technologies.doubtbuzzter2.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.model.GameLevelDto;
import arivihan.technologies.doubtbuzzter2.model.GameQuestionDto;
import arivihan.technologies.doubtbuzzter2.model.QuestionsSubmissionDto;
import arivihan.technologies.doubtbuzzter2.model.UserStatisticsDto;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;

import static arivihan.technologies.doubtbuzzter2.activity.HomeScreen.BroadcastStringForAction;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_GAME_QUESTION;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_GAME_STATISTICS;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.POST_GAME_RESPONSE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.START_GAME;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

/*
 * File: StreakQuestionActivity Copyright 11/6/21 11:39 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 11/6/21 11:39 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

public class StreakQuestionActivity extends AppCompatActivity {

    TextView level,Score,ques;
    String quesId = "";
    long TimeCounter = 180000;
    int counterMin = 3;
    int counterSec = 0;
    String finalAnswer = "";
    int timeUp = 0;
    String status = "";
    int ScoreUpdate = 0;
    int ToWinUpdate = 0;
    int selectedAns1 = 0;
    int selectedAns2 = 0;
    int selectedAns3 = 0;
    int selectedAns4 = 0;
    CardView OP1_Card,OP2_Card,OP3_Card,OP4_Card;
    String SelectedAns = "";
    ImageView image;
    Long timeTaken ;
    EditText answerText,answernumericalText;
    String questionType = "";
    LinearLayout optionlayout;
    TextInputLayout answerInput,answerInputNumerical;
    String score = "-1";
    Dialog dialog;
    CountDownTimer countDownTimer;
    String levelName = "";
    String levelId = "-1";
    TextView OP1Text,OP2Text,OP3Text,OP4Text,A,B,C,D,timer;
    LinearLayout l1u,l1d,l1r,l1l,l2u,l2d,l2r,l2l,l3u,l3d,l3r,l3l,l4u,l4d,l4r,l4l;
    int dataLoaded = 0;
    ScrollView scrollView;
    RelativeLayout submit,Parentlayer;
    String ToWin = "0";
    ProgressBar progressBar,progressbar;
    String currentGameId;
    Long startedOn;
    NetworkStatusLiveData networkStatusLiveData;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Disable Screen Schots----
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_streak_question);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Back navigation is intentionally blocked during streak questions
            }
        });

        level = findViewById(R.id.level);
        progressBar = findViewById(R.id.progress_quesloading);
        Score = findViewById(R.id.score);
        answerInputNumerical = findViewById(R.id.TextAnswerNumericalType);
        answernumericalText = findViewById(R.id.AnswerNumerical);
        answerInput = findViewById(R.id.TextAnswerType);
        optionlayout = findViewById(R.id.Option_layout);
        answerText = findViewById(R.id.Answer);
        timer = findViewById(R.id.timer);
        image = findViewById(R.id.question_image);
        progressbar = findViewById(R.id.progressbar);
        scrollView = findViewById(R.id.scrollView3);
        Parentlayer = findViewById(R.id.ParentRelativeLayout);
        submit = findViewById(R.id.submit);
        ques = findViewById(R.id.question);
        A = findViewById(R.id.Option_1);
        B = findViewById(R.id.Option_2);
        C = findViewById(R.id.Option_3);
        D = findViewById(R.id.Option_4);
        OP1Text = findViewById(R.id.OP1);
        OP2Text = findViewById(R.id.OP2);
        OP3Text = findViewById(R.id.OP3);
        OP4Text = findViewById(R.id.OP4);
        l1u = findViewById(R.id.Option_A_top);
        l1d = findViewById(R.id.Option_A_down);
        l1r = findViewById(R.id.Option_A_right);
        l1l = findViewById(R.id.Option_A_left);
        l2u = findViewById(R.id.Option_B_top);
        l2d = findViewById(R.id.Option_B_down);
        l2r = findViewById(R.id.Option_B_right);
        l2l = findViewById(R.id.Option_B_left);
        l3u = findViewById(R.id.Option_C_top);
        l3d = findViewById(R.id.Option_C_down);
        l3r = findViewById(R.id.Option_C_right);
        l3l = findViewById(R.id.Option_C_left);
        l4u = findViewById(R.id.Option_D_top);
        l4d = findViewById(R.id.Option_D_down);
        l4r = findViewById(R.id.Option_D_right);
        l4l = findViewById(R.id.Option_D_left);
        OP1_Card = findViewById(R.id.Option_A);
        OP2_Card = findViewById(R.id.Option_B);
        OP3_Card = findViewById(R.id.Option_C);
        OP4_Card = findViewById(R.id.Option_D);

        score = MyApplication.getStringData(PreferenceKeys.USER_SCORE.getKey());;
        levelName = getIntent().getStringExtra("LevelName");
        levelId = getIntent().getStringExtra("LevelId");
        ToWin = getIntent().getStringExtra("ToWin");
        currentGameId = getIntent().getStringExtra("CurrentGameId");
        startedOn = getIntent().getLongExtra("StartedOn", -1);

        if(score == null){
            Score.setText(getString(R.string.Score)+" : 00");
        }
        else{
            Score.setText(getString(R.string.Score)+" : "+score);
        }
        level.setText(levelName);


        OP1_Card.setCardBackgroundColor(Color.TRANSPARENT);
        OP1_Card.setCardElevation(0);
        OP2_Card.setCardBackgroundColor(Color.TRANSPARENT);
        OP2_Card.setCardElevation(0);
        OP3_Card.setCardBackgroundColor(Color.TRANSPARENT);
        OP3_Card.setCardElevation(0);
        OP4_Card.setCardBackgroundColor(Color.TRANSPARENT);
        OP4_Card.setCardElevation(0);


        // ON OPTION SELECTION UI CHANGES------------------
        OP1_Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (timeUp == 0) {
                    if (questionType.equals("SINGLE_CHOICE")) {
                        SelectedAns = OP1Text.getText().toString();
                        colorAll();
                        colorBoarder(l1l, l1d, l1r, l1u, "#87F9F0");
                        OP1Text.setTextColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.streak_correct_option_color));
                        A.setTextColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.streak_correct_option_color));
                    } else {
                        if (selectedAns1 == 1) {
                            selectedAns1 = 0;
                            colorBoarder(l1l, l1d, l1u, l1r, "#9E9E9E");
                            OP1Text.setTextColor(Color.WHITE);
                            A.setTextColor(Color.WHITE);
                        } else {
                            selectedAns1 = 1;
                            colorBoarder(l1l, l1d, l1r, l1u, "#87F9F0");
                            OP1Text.setTextColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.streak_correct_option_color));
                            A.setTextColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.streak_correct_option_color));
                        }
                    }
                }
                else{
                    CommonMethods.showNegativeSnackbar(Parentlayer,getString(R.string.TimeUp));
                }
            }
        });
        OP2_Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(timeUp == 0){
                    if(questionType.equals("SINGLE_CHOICE")) {
                        SelectedAns = OP2Text.getText().toString();
                        colorAll();
                        colorBoarder(l2l, l2d, l2r, l2u, "#87F9F0");
                        OP2Text.setTextColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.streak_correct_option_color));
                        B.setTextColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.streak_correct_option_color));
                }
                    else{
                        if(selectedAns2 == 1){
                            selectedAns2 = 0;
                            colorBoarder(l2l,l2d,l2u,l2r,"#9E9E9E");
                            OP2Text.setTextColor(Color.WHITE);
                            B.setTextColor(Color.WHITE);
                        }
                        else{
                            selectedAns2 = 1;
                            colorBoarder(l2l, l2d, l2r, l2u, "#87F9F0");
                            OP2Text.setTextColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.streak_correct_option_color));
                            B.setTextColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.streak_correct_option_color));
                        }
                    }
                }
                else{
                CommonMethods.showNegativeSnackbar(Parentlayer,getString(R.string.TimeUp));
            }
            }
        });
        OP3_Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(timeUp == 0) {
                    if (questionType.equals("SINGLE_CHOICE")) {
                        SelectedAns = OP3Text.getText().toString();
                        colorAll();
                        colorBoarder(l3l, l3d, l3r, l3u, "#87F9F0");
                        OP3Text.setTextColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.streak_correct_option_color));
                        C.setTextColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.streak_correct_option_color));
                    } else {
                        if (selectedAns3 == 1) {
                            selectedAns3 = 0;
                            colorBoarder(l3l, l3d, l3u, l3r, "#9E9E9E");
                            OP3Text.setTextColor(Color.WHITE);
                            C.setTextColor(Color.WHITE);
                        } else {
                            selectedAns3 = 1;
                            colorBoarder(l3l, l3d, l3r, l3u, "#87F9F0");
                            OP3Text.setTextColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.streak_correct_option_color));
                            C.setTextColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.streak_correct_option_color));
                        }
                    }
                }
                else{
                CommonMethods.showNegativeSnackbar(Parentlayer,getString(R.string.TimeUp));
            }
            }
        });
        OP4_Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (timeUp == 0) {
                    if (questionType.equals("SINGLE_CHOICE")) {
                        colorAll();
                        SelectedAns = OP4Text.getText().toString();
                        colorBoarder(l4l, l4d, l4r, l4u, "#87F9F0");
                        OP4Text.setTextColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.streak_correct_option_color));
                        D.setTextColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.streak_correct_option_color));
                    } else {
                        if (selectedAns4 == 1) {
                            selectedAns4 = 0;
                            colorBoarder(l4l, l4d, l4u, l4r, "#9E9E9E");
                            OP4Text.setTextColor(Color.WHITE);
                            D.setTextColor(Color.WHITE);
                        } else {
                            selectedAns4 = 1;
                            colorBoarder(l4l, l4d, l4r, l4u, "#87F9F0");
                            OP4Text.setTextColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.streak_correct_option_color));
                            D.setTextColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.streak_correct_option_color));
                        }
                    }
                }
                else{
                    CommonMethods.showNegativeSnackbar(Parentlayer,getString(R.string.TimeUp));
                }
            }
        });
        //-------------------------------------------------

        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            if(isOnline){
//                    if(dataLoaded == 0) {
//                        getQuestionAPI();
//                    }
//                    if(timeUp == 1){
//                        timeUp = -1;
//                        PostAnswerAPI();
//                    }
                if(!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    CommonMethods.showPositiveSnackbar(Parentlayer,getString(R.string.InternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(),true);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(),false);
                }
            }
            else{
                if(!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())){
                    CommonMethods.showNegativeSnackbar(Parentlayer,getString(R.string.NoInternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(),false);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(),true);
                }
            }

        });

        getQuestionAPI();


        if(CommonMethods.isOnline(this)){
            if(!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                if(dataLoaded == 0)
                    getQuestionAPI();
                if(timeUp == 1){
                    timeUp = -1;
                    PostAnswerAPI();
                }
                MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(),true);
                MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(),false);
            }
        }
        else{
            if(!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())){
                MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(),true);
                MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(),false);
            }
        }


        // On Submission Response---
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())){
                    CommonMethods.showNegativeSnackbar(Parentlayer,getString(R.string.NoInternetConnection));
                }
                else{
                    if(questionType.equals("SUBJECTIVE")){
                        if(answerText.getText().toString().trim().isEmpty()){
                            CommonMethods.showNegativeSnackbar(Parentlayer,getString(R.string.PleaseGiveYourAnswer));
                        }
                        else{
                            try {
                                countDownTimer.cancel();
                            }catch (Exception e){ }
                            progressbar.setVisibility(View.VISIBLE);
                            submit.setVisibility(View.GONE);
                            if(timeUp == 0)
                                SelectedAns = answerText.getText().toString().trim();
                            else
                                SelectedAns = finalAnswer;
                            PostAnswerAPI();
                        }
                    }
                    else if(questionType.equals("NUMERICAL")){
                        if(answernumericalText.getText().toString().trim().isEmpty()){
                            CommonMethods.showNegativeSnackbar(Parentlayer,getString(R.string.PleaseGiveYourAnswer));
                        }
                        else{
                            try {
                                countDownTimer.cancel();
                            }catch (Exception e){ }
                            progressbar.setVisibility(View.VISIBLE);
                            submit.setVisibility(View.GONE);
                            if(timeUp == 0)
                                SelectedAns = answernumericalText.getText().toString().trim();
                            else
                                SelectedAns = finalAnswer;
                            PostAnswerAPI();
                        }
                    }
                    else {
                        if (SelectedAns.isEmpty() && selectedAns1 == 0 && selectedAns2 == 0 && selectedAns3 == 0 && selectedAns4 == 0) {
                            CommonMethods.showNegativeSnackbar(Parentlayer,getString(R.string.PleaseGiveYourAnswer));
                        } else {
                            progressbar.setVisibility(View.VISIBLE);
                            submit.setVisibility(View.GONE);
                            try {
                                countDownTimer.cancel();
                            }catch (Exception e){ }
                            if(selectedAns1 == 1)
                                SelectedAns += OP1Text.getText().toString();
                            if(selectedAns2 == 1){
                                if(SelectedAns.length() > 0)
                                    SelectedAns += "," + OP2Text.getText().toString();
                                else
                                    SelectedAns +=  OP2Text.getText().toString();
                            }
                            if(selectedAns3 == 1){
                                if(SelectedAns.length() > 0)
                                    SelectedAns += "," + OP3Text.getText().toString();
                                else
                                    SelectedAns +=  OP3Text.getText().toString();
                            }
                            if(selectedAns4 == 1){
                                if(SelectedAns.length() > 0)
                                    SelectedAns += "," + OP4Text.getText().toString();
                                else
                                    SelectedAns +=  OP4Text.getText().toString();
                            }
                            PostAnswerAPI();
                        }
                    }
                }
            }
        });

    }

    private void openNewIntent(){
        countDownTimer = new CountDownTimer(4500, 1000){
            public void onTick(long millisUntilFinished){
                // nothing to do here
            }
            public void onFinish(){
                getUserGameStatisticsAPI();
            }
        }.start();
    }

    private void PostAnswerAPI() {
        QuestionsSubmissionDto model = new QuestionsSubmissionDto();
        model.setAnswer(SelectedAns);
        model.setQuestionId(quesId);
        model.setCurrentLevelId(levelId);
        model.setUserId(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
        model.setTimeTaken(timeTaken);

        Gson gson = new Gson();
        try {
            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                    POST_GAME_RESPONSE, new JSONObject(gson.toJson(model)), response -> {
                try {
                    if(Boolean.parseBoolean(response.get("success").toString())){
                        // Correct Answer---
                        status = "Correct";
                        showDialogSuccess();
                        Log.i("Status "," "+status);
                    }
                    else{
                        // Wrong Answer---
                        status = "Incorrect";
                        showDialogWrong();
                        Vibration();
                        Log.i("Status "," "+status);
                    }
                    try {
                        countDownTimer.cancel();
                    }catch (Exception e){ }
                    submit.setVisibility(View.GONE);
                    progressbar.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    openNewIntent();
                } catch (JSONException e) {
                    submit.setVisibility(View.VISIBLE);
                    progressbar.setVisibility(View.GONE);
                    e.printStackTrace();
                }
            }, new VolleyGlobalErrorListener(this) {
                @Override
                public void postError(VolleyError error) {
                    submit.setVisibility(View.VISIBLE);
                    progressbar.setVisibility(View.GONE);
                    CommonMethods.showNegativeSnackbar(Parentlayer,error.getMessage());
                }
            });
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
        } catch (JSONException e) {
            submit.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            Log.d("error", e.getMessage());
        }
    }

    private void showDialogSuccess() {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        dialog = new Dialog(StreakQuestionActivity.this);
        dialog.setContentView(R.layout.custom_game_dialogue_submit_response);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);

        TextView points = dialog.findViewById(R.id.points);
        LottieAnimationView image2 = dialog.findViewById(R.id.wrong_ans);
        LottieAnimationView successAnimation = dialog.findViewById(R.id.celebration);

        points.setText(ToWin+" "+getString(R.string.Points));
        image2.setVisibility(View.GONE);
        successAnimation.setVisibility(View.VISIBLE);
        successAnimation.playAnimation();

        dialog.show();

    }

    private void showDialogWrong() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        dialog = new Dialog(StreakQuestionActivity.this);
        dialog.setContentView(R.layout.custom_game_dialogue_submit_response);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);

        TextView Title = dialog.findViewById(R.id.title);
        TextView points = dialog.findViewById(R.id.points);
        TextView subTitle = dialog.findViewById(R.id.sub_title);
        LottieAnimationView image = dialog.findViewById(R.id.trophy);

        points.setText("00 "+getString(R.string.Points));
        points.setTextColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.streak_points_color));
        Title.setText(getString(R.string.LevelFailed));
        image.setVisibility(View.GONE);
        subTitle.setText(getString(R.string.TryAgain));

        dialog.show();

    }

    // on wrong attempt vibrate--
    private void Vibration() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            v.vibrate(VibrationEffect.createOneShot(800, VibrationEffect.DEFAULT_AMPLITUDE));
        else
            v.vibrate(800);
    }

    private void getQuestionAPI() {
        Map<String, Object> map = new HashMap<>();
        map.put("gameId",levelId);
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_GAME_QUESTION, map), null,
                response -> {
                    if (isDestroyed() || isFinishing()) return;
                    Gson gson = new Gson();
                    dataLoaded = 1;
                    TypeToken<GameQuestionDto> token = new TypeToken<GameQuestionDto>() {};
                    GameQuestionDto gameQuestionDto = gson.fromJson(response.toString(), token.getType());
                    ques.setText(gameQuestionDto.getQuestionText());
                    quesId = gameQuestionDto.getQuestionId();
                    questionType = gameQuestionDto.getQuestionType();
                    if(questionType.equals("SUBJECTIVE")){
                        optionlayout.setVisibility(View.GONE);
                        answerInput.setVisibility(View.VISIBLE);
                        answerInputNumerical.setVisibility(View.GONE);
                    }
                    if(questionType.equals("NUMERICAL")){
                        optionlayout.setVisibility(View.GONE);
                        answerInput.setVisibility(View.GONE);
                        answerInputNumerical.setVisibility(View.VISIBLE);
                    }
                    if(gameQuestionDto.getQuestionImageUrl() != null){
                        Glide.with(this).load(gameQuestionDto.getQuestionImageUrl()).into(image);
                        image.setVisibility(View.VISIBLE);
                    }
                    if(gameQuestionDto.getQoptions() != null) {
                        OP1Text.setText(gameQuestionDto.getQoptions().get(0));
                        if(gameQuestionDto.getQoptions().size() == 1) {
                            OP2_Card.setVisibility(View.GONE);
                            OP3_Card.setVisibility(View.GONE);
                            OP4_Card.setVisibility(View.GONE);
                        }
                        if(gameQuestionDto.getQoptions().size() == 2){
                            OP2Text.setText(gameQuestionDto.getQoptions().get(1));
                            OP3_Card.setVisibility(View.GONE);
                            OP4_Card.setVisibility(View.GONE);
                        }
                        if(gameQuestionDto.getQoptions().size() == 3){
                            OP2Text.setText(gameQuestionDto.getQoptions().get(1));
                            OP3Text.setText(gameQuestionDto.getQoptions().get(2));
                            OP4_Card.setVisibility(View.GONE);
                        }
                        if(gameQuestionDto.getQoptions().size() == 4){
                            OP2Text.setText(gameQuestionDto.getQoptions().get(1));
                            OP3Text.setText(gameQuestionDto.getQoptions().get(2));
                            OP4Text.setText(gameQuestionDto.getQoptions().get(3));
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                    scrollView.setVisibility(View.VISIBLE);
                    startTimer(gameQuestionDto.getQuestionId(), currentGameId);
                    if (StringUtils.isEmpty(currentGameId) || startedOn == -1 ||
                            !gameQuestionDto.getQuestionId().equalsIgnoreCase(currentGameId) ) {
                        startGame(gameQuestionDto.getQuestionId());
                    }
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                CommonMethods.showNegativeSnackbar(Parentlayer,error.getMessage());
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);

    }

    private void startGame(String currentGameId) {
        Map<String, Object> map = new HashMap<>();
        map.put("gameQuestionId",currentGameId);
        map.put("startedOn", System.currentTimeMillis());
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(START_GAME, map), null,
                response -> {

                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);

    }


    private void getUserGameStatisticsAPI() {
        Map<String, Object> map = new HashMap<>();
        String userId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
        map.put(PreferenceKeys.USER_ID.getKey(),userId);
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_GAME_STATISTICS, map), null,
                response -> {
                    Gson gson = new Gson();
                    TypeToken<UserStatisticsDto> token = new TypeToken<UserStatisticsDto>() {};
                    UserStatisticsDto list = gson.fromJson(response.toString(), token.getType());
                    ScoreUpdate = list.getScore();
                    levelId = list.getCurrentLevelId();
                    MyApplication.saveData(PreferenceKeys.USER_SCORE.getKey(),ScoreUpdate+"");
                    List<GameLevelDto> gameList = list.getGameLevelDtoList();
                    for(GameLevelDto g : gameList){
                        if(g.getId().equals(levelId)){
                            ToWinUpdate = g.getPointToWin();
                            break;
                        }
                    }
                    Score.setText(getString(R.string.Score)+" : "+ScoreUpdate);
                    Intent intent = new Intent(StreakQuestionActivity.this,StreakHomePage.class);
                    intent.putExtra("levelId", levelId);
                    intent.putExtra("ToWin",String.valueOf(ToWinUpdate));
                    intent.putExtra("Score",ScoreUpdate+"");;
                    intent.putExtra("Status",status);
                    if (dialog != null && !isFinishing() || !isDestroyed()) {
                        dialog.dismiss();
                        startActivity(intent);
                        finish();
                    }
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                CommonMethods.showNegativeSnackbar(Parentlayer,error.getMessage());
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);

    }

    private void startTimer(String questionId, String currentGameId) {

        if (StringUtils.isNotEmpty(currentGameId) && startedOn != -1 && questionId.equalsIgnoreCase(currentGameId)) {
            if (System.currentTimeMillis() - startedOn > TimeCounter) {
                timeTaken = TimeCounter;
                CommonMethods.showNegativeSnackbar(Parentlayer, getString(R.string.TimeUp));
                timeUp = 1;
                finalAnswer = answerText.getText().toString().trim();
                if(MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    PostAnswerAPI();
                }
                return;
            } else if (System.currentTimeMillis() - startedOn < TimeCounter) {
                TimeCounter = TimeCounter - (System.currentTimeMillis() - startedOn) ;
            }
        }

        countDownTimer = new CountDownTimer(TimeCounter, 1000) {
            public void onTick(long millisUntilFinished) {
                timeTaken = millisUntilFinished;
                long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60;
                long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60;
                timer.setText(String.format(Locale.ENGLISH, " %02d : %02d ", minutes, seconds));
            }

            public void onFinish() {
                CommonMethods.showNegativeSnackbar(Parentlayer, getString(R.string.TimeUp));
                timeUp = 1;
                finalAnswer = answerText.getText().toString().trim();
                if(MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    PostAnswerAPI();
                }
            }
        }.start();

    }



    private void colorBoarder(LinearLayout l1, LinearLayout l2, LinearLayout l3, LinearLayout l4, String color) {
        l1.setBackgroundColor(Color.parseColor(color));
        l2.setBackgroundColor(Color.parseColor(color));
        l3.setBackgroundColor(Color.parseColor(color));
        l4.setBackgroundColor(Color.parseColor(color));

    }

    private void colorAll() {
        A.setTextColor(Color.WHITE);
        B.setTextColor(Color.WHITE);
        C.setTextColor(Color.WHITE);
        D.setTextColor(Color.WHITE);
        OP1Text.setTextColor(Color.WHITE);
        OP2Text.setTextColor(Color.WHITE);
        OP3Text.setTextColor(Color.WHITE);
        OP4Text.setTextColor(Color.WHITE);
        l1u.setBackgroundColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.not_selected));
        l1d.setBackgroundColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.not_selected));
        l1l.setBackgroundColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.not_selected));
        l1r.setBackgroundColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.not_selected));
        l2u.setBackgroundColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.not_selected));
        l2l.setBackgroundColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.not_selected));
        l2r.setBackgroundColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.not_selected));
        l2d.setBackgroundColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.not_selected));
        l3u.setBackgroundColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.not_selected));
        l3l.setBackgroundColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.not_selected));
        l3r.setBackgroundColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.not_selected));
        l3d.setBackgroundColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.not_selected));
        l4u.setBackgroundColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.not_selected));
        l4l.setBackgroundColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.not_selected));
        l4r.setBackgroundColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.not_selected));
        l4d.setBackgroundColor(ContextCompat.getColor(StreakQuestionActivity.this, R.color.not_selected));
    }

}