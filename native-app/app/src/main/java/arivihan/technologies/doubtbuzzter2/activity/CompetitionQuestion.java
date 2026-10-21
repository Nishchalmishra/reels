package arivihan.technologies.doubtbuzzter2.activity;

import static android.app.PendingIntent.getActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.fragments.CompetitionQuestionFragment;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.listener.OptionListener;
import arivihan.technologies.doubtbuzzter2.model.AnswerInfoList;
import arivihan.technologies.doubtbuzzter2.model.CompetitionUserAnswer;
import arivihan.technologies.doubtbuzzter2.model.TestSeriesQuestionDto;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.COMPETITION_SUBMIT;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

/*
 * File: CompetitionQuestion Copyright 25/5/22 10:46 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 25/5/22 10:46 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

public class CompetitionQuestion extends AppCompatActivity implements OptionListener {
    private List<TestSeriesQuestionDto> list ;
    private String Type = "";
    int index = 0;
    Map<String, Integer> userAnswerMap;
    boolean end = true;
    String gameId;
    TextView time;
    CountDownTimer countDownTimer;
    boolean timeFinished = false;
    private long startTime ;
    private boolean submitted;
    private boolean backPressed;
    NetworkStatusLiveData networkStatusLiveData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_competition_question);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Type = MyApplication.getStringData(PreferenceKeys.RESPONSE.getKey());
        list = MyApplication.getTestSeriesQuestionList("gameQuestions");
        gameId = getIntent().getStringExtra("gameId");
        userAnswerMap = new HashMap<>();
        time = findViewById(R.id.timer);
        startTime = System.currentTimeMillis();
        long timeToAttempt = getIntent().getLongExtra("timeToAttempt", 3600000);
        Log.d("timeToAttempt", "v time = " + timeToAttempt);
        timeToAttempt = timeToAttempt - System.currentTimeMillis();

        Log.d("timeToAttempt", "time = " + timeToAttempt);

        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            if(isOnline){

                if (timeFinished && Type.equals("ATTEMPT") && !submitted) {
                    submitAnswer();
                }

                if(!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    CommonMethods.showPositiveSnackbar(time, MyApplication.getInstance().getString(R.string.InternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(),true);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(),false);
                }
            }
            else{
                if(!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())){
                    CommonMethods.showNegativeSnackbar(time, MyApplication.getInstance().getString(R.string.NoInternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(),false);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(),true);
                }
            }
        });

        if ("ATTEMPT".equalsIgnoreCase(Type)) {
            long finalTimeToAttempt = timeToAttempt;
            countDownTimer = new CountDownTimer(finalTimeToAttempt, 1000) {
                public void onTick(long millisUntilFinished) {
                    Log.d("countDownTime", "" + millisUntilFinished);
                    time.setText(CommonMethods.getTimeInHourAndMinute(millisUntilFinished,CompetitionQuestion.this) + " Left");

                }

                public void onFinish() {
                    timeFinished = true;
                    Log.d("countDownTime", "" + finalTimeToAttempt);
                    Vibration();
                    if (MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                        CommonMethods.showNegativeSnackbar(time, getString(R.string.TimeUp) + "!!");
                        if (Type.equals("ATTEMPT")) {
                            submitAnswer();
                        }
                    } else {
                        CommonMethods.showNegativeSnackbar(time, getString(R.string.NoInternetConnection) + "   " + getString(R.string.TimeUp) + "!!");
                    }
                }
            };
            countDownTimer.start();
        }
        
        

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!"ATTEMPT".equalsIgnoreCase(Type)) {
                    finish();
                } else {
                    if (!backPressed) {
                        backPressed = true;
                        CommonMethods.showNegativeSnackbar(time, getString(R.string.click_back_game), 1500);
                        new Handler().postDelayed(() -> backPressed = false, 2000);
                    } else {
                        finish();
                    }
                }
            }
        });

        if (!CollectionUtils.isEmpty(list)) {
            int x = index++;
            end = (x + 1) == list.size();
            CompetitionQuestionFragment competitionQuestionFragment = CompetitionQuestionFragment.newInstance(list.get(x),
                    null, userAnswerMap, end, (x+1) + " out of " +list.size());
            setFragment(competitionQuestionFragment, list.get(x).getTimeTaken());
        }
    }
    
    private void Vibration() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
        else
            v.vibrate(1000);
    }

    private void setFragment(Fragment fragment, long timeTaken) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in, R.anim.slide_out);
        fragmentTransaction.replace(R.id.fragmentContainer, fragment);
        fragmentTransaction.commitAllowingStateLoss();
        if (!"ATTEMPT".equalsIgnoreCase(Type)) {
            time.setText(CommonMethods.getTimeInHourAndMinute(timeTaken,CompetitionQuestion.this) + " Taken");
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();;
        }
    }

    @Override
    public void setSelected(int i) {
        if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
            CommonMethods.showNegativeSnackbar(time, MyApplication.getInstance().getString(R.string.NoInternetConnection));
        } else {
            int x = index++;
            end = (x + 1) == list.size();
            if (x < list.size()) {
                CompetitionQuestionFragment competitionQuestionFragment = CompetitionQuestionFragment.newInstance(list.get(x),
                        null, userAnswerMap, end, (x + 1) + " out of " + list.size());
                setFragment(competitionQuestionFragment, list.get(x).getTimeTaken());
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void submitAnswer() {

        if (index <= list.size() && !timeFinished) {
            return;
        }

        submitted = true;
        countDownTimer.cancel();

        List<AnswerInfoList> answerInfoLists = new ArrayList<>();
        for (TestSeriesQuestionDto testSeriesQuestionDto : list) {
            if (StringUtils.isEmpty(testSeriesQuestionDto.getUserAnswer())) {
                continue;
            }
            answerInfoLists.add(AnswerInfoList
                    .builder()
                    .questionId(testSeriesQuestionDto.getQuestionId())
                    .answer(testSeriesQuestionDto.getUserAnswer())
                    .timeTaken(testSeriesQuestionDto.getTimeTaken())
                    .build());
        }
        String UserId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());

        long timeTaken = System.currentTimeMillis() - startTime;
        long currentTimeMillis = System.currentTimeMillis();
        Log.d("GameSubmittedOn", "" + currentTimeMillis);

        CompetitionUserAnswer testSeriesAnswer = CompetitionUserAnswer
                .builder()
                .answerInfoList(answerInfoLists)
                .gameId(gameId)
                .timeTaken(timeTaken)
                .userId(UserId)
                .attemptedOn(currentTimeMillis)
                .answerInfoList(answerInfoLists)
                .build();
        Gson gson = new Gson();

        try {
            MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.POST,
                    COMPETITION_SUBMIT, new JSONObject(gson.toJson(testSeriesAnswer)),
                    response -> {
                        try {

                            if (response.getBoolean("success")) {
                                Toast.makeText(this, getString(R.string.submitted), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(this, LeaderboardActivity.class);
                                intent.putExtra("gameId", gameId);
                                startActivity(intent);
                                this.finish();
                            }

                        } catch (JSONException e) {
                            Toast.makeText(this, getString(R.string.please_try_again), Toast.LENGTH_SHORT).show();
                        }
                    }, new VolleyGlobalErrorListener(this) {
                @Override
                public void postError(VolleyError error) {
                    Toast.makeText(CompetitionQuestion.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
        } catch (JSONException e) {
            Toast.makeText(this, getString(R.string.please_try_again), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void nextPage(int i) {

    }

    @Override
    public void setSelected(int i, String answer) {

    }

    @Override
    public TestSeriesQuestionDto getDto(int fragNumber) {
        return null;
    }

}