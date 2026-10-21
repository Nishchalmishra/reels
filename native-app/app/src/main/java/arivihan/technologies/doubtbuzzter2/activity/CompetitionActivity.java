package arivihan.technologies.doubtbuzzter2.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.InstructionBodyAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.SyllabusCompetitionAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.helper.firebase.FirebaseMessagingManager;
import arivihan.technologies.doubtbuzzter2.model.ChaptersInfo;
import arivihan.technologies.doubtbuzzter2.model.CompetitionQuestion;
import arivihan.technologies.doubtbuzzter2.model.GameCompetitionInfo;
import arivihan.technologies.doubtbuzzter2.model.GameSyllabus;
import arivihan.technologies.doubtbuzzter2.model.SyllabusCompetitionModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.COMPETITION;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.COMPETITION_REGISTER;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.QUESTION;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class CompetitionActivity extends LocaleActivity {

    Dialog dialog;
    Dialog dialogRegistration;
    Dialog dialogSyllabus;
    ProgressBar progressBar;
    ArrayList<SyllabusCompetitionModel> model = new ArrayList<>();
    RecyclerView syllabusRecyclerview;
    RelativeLayout play, register;
    LinearLayout help, history, syllabus;
    TextView participant, prizeInfo, btnTxt, registerTxt, header;
    CountDownTimer timer;
    private String gameId;
    boolean participated;
    private CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_competition);

        progressBar = findViewById(R.id.progressbar);
        dialog = new Dialog(CompetitionActivity.this);
        dialog.setContentView(R.layout.custom_dialogue_competition);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialogInit();

        CardView cancel = dialog.findViewById(R.id.cancel);
        CardView cancel2 = dialogRegistration.findViewById(R.id.cancel);
        checkBox = dialogRegistration.findViewById(R.id.checkbox);
        CardView cancel3 = dialogSyllabus.findViewById(R.id.cancel);
        register = dialogRegistration.findViewById(R.id.register);
        registerTxt = dialogRegistration.findViewById(R.id.register_text);
        syllabusRecyclerview = dialogSyllabus.findViewById(R.id.syllabusRecyclerview);
        history=  findViewById(R.id.history);
        help = findViewById(R.id.help);
        participant = findViewById(R.id.participant);
        syllabus = findViewById(R.id.syllabus);
        prizeInfo = findViewById(R.id.prizeInfo);
        btnTxt = findViewById(R.id.btn_text);
        play = findViewById(R.id.play);
        progressBar.setVisibility(View.GONE);
        prizeInfo.setVisibility(View.GONE);
        play.setVisibility(View.GONE);
        participant.setVisibility(View.GONE);
        help.setVisibility(View.GONE);

        header = findViewById(R.id.header);
        syllabus.setVisibility(View.GONE);

        checkBox.setOnClickListener(view -> {
            if (checkBox.isChecked()) {
                registerTxt.setTextColor(ContextCompat.getColor(this, R.color.competition_register_text));
                register.setBackgroundResource(R.drawable.ic_game_card);
            } else {
                registerTxt.setTextColor(getResources().getColor(R.color.grey_500));
                register.setBackgroundResource(R.drawable.ic_game_card_disabled);
            }
        });

        String competition_text = MyApplication.getStringData(PreferenceKeys.COMPETITION_TEXT.getKey());

        if (StringUtils.isNotEmpty(competition_text)) {
            header.setText(competition_text);
        }

        cancel.setOnClickListener(view -> dialog.dismiss());
        cancel2.setOnClickListener(v -> dialogRegistration.dismiss());
        cancel3.setOnClickListener(v -> dialogSyllabus.dismiss());
        register.setOnClickListener(v -> {
            //Register Method goes here
            if (checkBox.isChecked()) {
                registerCompetition();
            } else {
                CommonMethods.showNegativeSnackbar(help, getString(R.string.please_accept_terms_and_condition_first));

            }
        });
        help.setOnClickListener(v -> {
            dialog.show();
            subscribeForTopic(gameId);
        });
        syllabus.setOnClickListener(v -> dialogSyllabus.show());
        play.setOnClickListener(view ->  {
            if (participated) {
                getGameQuestion();
            } else {
                dialogRegistration.show();
            }
        });
        getGameInfo();

        history.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));

    }


    private void getGameInfo() {
        progressBar.setVisibility(View.VISIBLE);
        Map<String, Object> map = new HashMap<>();
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(COMPETITION, map), null,
                response -> {
                    Gson gson = new Gson();
                    TypeToken<GameCompetitionInfo> token = new TypeToken<GameCompetitionInfo>() {};
                    GameCompetitionInfo gameCompetitionInfo = gson.fromJson(response.toString(), token.getType());
                    if (gameCompetitionInfo.getSyllabus() != null) {
                        loadSyllabusData(gameCompetitionInfo.getSyllabus());
                    }
                    updateView(gameCompetitionInfo);

                }, new VolleyGlobalErrorListener(CompetitionActivity.this) {
            @Override
            public void postError(VolleyError error) {
                participant.setText(error.getMessage());
                progressBar.setVisibility(View.GONE);
                prizeInfo.setVisibility(View.GONE);
                play.setVisibility(View.GONE);
                participant.setVisibility(View.VISIBLE);
                help.setVisibility(View.GONE);
                syllabus.setVisibility(View.GONE);

            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
    }

    private void updateView(GameCompetitionInfo gameCompetitionInfo) {
        progressBar.setVisibility(View.GONE);
        prizeInfo.setVisibility(View.VISIBLE);
        play.setVisibility(View.VISIBLE);
        help.setVisibility(View.VISIBLE);
        gameId = gameCompetitionInfo.getGameId();
        if (gameCompetitionInfo.getPrize() !=null) {
            prizeInfo.setText(gameCompetitionInfo.getPrize());
        } else {
            prizeInfo.setVisibility(View.GONE);
        }
        if (gameCompetitionInfo.getSyllabus() != null) {
            syllabus.setVisibility(View.VISIBLE);
        }
        //header.setText(gameCompetitionInfo.getHeader());

        if (dialogRegistration != null) {
            TextView registrationViewById = dialogRegistration.findViewById(R.id.registrationMessage);
            registrationViewById.setText(gameCompetitionInfo.getTermsAndCondition());
        }

        if (gameCompetitionInfo.isParticipated()) {
            participant.setVisibility(View.VISIBLE);
            participated = true;
            participant.setClickable(false);
            participant.setText(gameCompetitionInfo.getNoOfParticipant() + " Participants.");
            long time = System.currentTimeMillis();
            long diff = 0;
            long UnLockTime = gameCompetitionInfo.getGameStartTime();
            if (time <= UnLockTime) {
                diff = UnLockTime - time;
                timer = new CountDownTimer(diff, 1000) {
                    public void onTick(long millisUntilFinished) {
                        long remainingTime = (millisUntilFinished) / 1000;
                        if (remainingTime <= 60) {
                            btnTxt.setText("Unlocking in " + remainingTime + " sec");
                        } else if (remainingTime > 60 && remainingTime < 60 * 60) {
                            remainingTime = remainingTime / 60;
                            btnTxt.setText("Unlocking in " + remainingTime + " min");
                        } else {
                            remainingTime = remainingTime / 3600;
                            btnTxt.setText("Unlocking in " + remainingTime + " hours");
                        }

                    }

                    public void onFinish() {
                        btnTxt.setText(getString(R.string.play));
                    }
                }.start();
            } else if (time >= UnLockTime && time <= gameCompetitionInfo.getGameEndTime()){
                btnTxt.setText(getString(R.string.play));
            } else {
                btnTxt.setText(getString(R.string.please_wait_for_next_game));
            }
        }
        updateInstructions(gameCompetitionInfo.getInstructions());
    }



    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }

    private void updateInstructions(List<List<String>> instructionsList) {
        if (dialog == null) {
            dialog = new Dialog(CompetitionActivity.this);
            dialog.setContentView(R.layout.custom_dialogue_competition);
            dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.setCancelable(false);
        }

        List<String> instructions = new ArrayList<>();
        for (List<String> ins : instructionsList) {
            instructions.addAll(ins);
        }

        InstructionBodyAdapter instructionBodyAdapter = new InstructionBodyAdapter(instructions, true, true);
        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(instructionBodyAdapter);
    }

    private void loadSyllabusData(GameSyllabus syllabus) {
        model.clear();
        syllabusRecyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL ,false));

        for (ChaptersInfo chapter : syllabus.getChapters()) {
            SyllabusCompetitionModel s = new SyllabusCompetitionModel();
            s.setChapter(chapter.getName());
            s.setChapterId(chapter.getId());
            s.setMicrolecture(chapter.getMicrolectures());
            s.setCompleted(chapter.isCompleted());
            model.add(s);
        }

        SyllabusCompetitionAdapter adapter = new SyllabusCompetitionAdapter(model,this);
        syllabusRecyclerview.setAdapter(adapter);

    }

    private void dialogInit() {

        dialogRegistration = new Dialog(CompetitionActivity.this);
        dialogRegistration.setContentView(R.layout.custom_dialogue_registration);
        dialogRegistration.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        dialogRegistration.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogRegistration.setCancelable(false);

        dialogSyllabus = new Dialog(CompetitionActivity.this);
        dialogSyllabus.setContentView(R.layout.custom_layout_syllabus);
        dialogSyllabus.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        dialogSyllabus.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogSyllabus.setCancelable(false);

    }

    private void registerCompetition() {
        participant.setVisibility(View.GONE);
        play.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        Map<String, Object> map = new HashMap<>();
        map.put("gameId", gameId);
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(COMPETITION_REGISTER, map), null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            CommonMethods.showPositiveSnackbar(help, getString(R.string.congrat_registered_for_game));
                            dialogRegistration.cancel();
                            getGameInfo();
                            subscribeForTopic(gameId);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    play.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);

                }, new VolleyGlobalErrorListener(CompetitionActivity.this) {
            @Override
            public void postError(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(),Toast.LENGTH_SHORT).show();
                participant.setVisibility(View.GONE);
                play.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
    }

    private void subscribeForTopic(String gameId) {
        FirebaseMessagingManager.subscribeToTopic(gameId);
//        FirebaseMessaging.getInstance().subscribeToTopic(gameId);
    }

    private void getGameQuestion() {
        progressBar.setVisibility(View.VISIBLE);
        play.setVisibility(View.GONE);
        Map<String, Object> map = new HashMap<>();
        map.put("gameId", gameId);
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(QUESTION, map), null,
                response -> {
                    try {
                    Gson gson = new Gson();
                    TypeToken<CompetitionQuestion> token = new TypeToken<CompetitionQuestion>() {};
                    CompetitionQuestion competitionQuestion = gson.fromJson(response.toString(), token.getType());
                    Intent intent = new Intent(this, arivihan.technologies.doubtbuzzter2.activity.CompetitionQuestion.class);
                    MyApplication.saveTestSeriesQuestions("gameQuestions", competitionQuestion.getTestSeriesQuestionDtos());
                    MyApplication.saveData(PreferenceKeys.RESPONSE.getKey(), "ATTEMPT");

                    intent.putExtra("timeToAttempt", response.getLong("gameEndTime"));

                    intent.putExtra("gameId", gameId);
                    startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        progressBar.setVisibility(View.GONE);
                        play.setVisibility(View.VISIBLE);
                    }

                }, new VolleyGlobalErrorListener(CompetitionActivity.this) {
            @Override
            public void postError(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(),Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                play.setVisibility(View.VISIBLE);
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
    }
}