package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.HISTORY;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.REVIEW;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.HIstoryAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.InstructionBodyAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.SyllabusCompetitionAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.model.ChaptersInfo;
import arivihan.technologies.doubtbuzzter2.model.CompetitionHistory;
import arivihan.technologies.doubtbuzzter2.model.GameSyllabus;
import arivihan.technologies.doubtbuzzter2.model.HistoryModel;
import arivihan.technologies.doubtbuzzter2.model.SyllabusCompetitionModel;
import arivihan.technologies.doubtbuzzter2.model.TestSeriesQuestionDto;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class HistoryActivity extends LocaleActivity implements HIstoryAdapter.HistoryListener {

    RecyclerView recyclerView;
    ImageView back;
    ArrayList<HistoryModel> model = new ArrayList<>();
    ArrayList<SyllabusCompetitionModel> syllabusModel = new ArrayList<>();
    Dialog dialog;
    RecyclerView syllabusRecyclerview;
    Dialog dialogSyllabus;
    private List<CompetitionHistory> competitionHistories;
    private CardView lastCard;
    RelativeLayout history;
    TextView previousGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        history = findViewById(R.id.history);
        history.setVisibility(View.GONE);
        back = findViewById(R.id.back_arrow);
        previousGame = findViewById(R.id.previous_game);
        recyclerView = findViewById(R.id.recyclerViewHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        lastCard = findViewById(R.id.last_game_card);
        getHistoryData();
        dialogInit();
        back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        CardView cancel = dialog.findViewById(R.id.cancel);
        CardView cancel3 = dialogSyllabus.findViewById(R.id.cancel);
        cancel.setOnClickListener(view -> dialog.dismiss());

        cancel3.setOnClickListener(v -> dialogSyllabus.dismiss());
    }

    private void getHistoryData() {
        Map<String, Object> map = new HashMap<>();
        MyJsonArrayRequest myJsonObjectRequest2 = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(HISTORY, map), null,
                response -> {
                    Gson gson = new Gson();
                    TypeToken<List<CompetitionHistory>> token = new TypeToken<List<CompetitionHistory>>() {
                    };
                    competitionHistories = gson.fromJson(response.toString(), token.getType());
                    updateView(competitionHistories);
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
    }

    private void updateView(List<CompetitionHistory> competitionHistories) {

        if (CollectionUtils.isEmpty(competitionHistories)) {
            Toast.makeText(getApplicationContext(), getString(R.string.not_played), Toast.LENGTH_SHORT).show();
        } else {
            history.setVisibility(View.VISIBLE);
            int pos = 0;

            for (CompetitionHistory competitionHistory : competitionHistories) {
                if (pos++ == 0) {
                    updateCardView(competitionHistory);
                } else {
                    previousGame.setVisibility(View.VISIBLE);
                    HistoryModel m = new HistoryModel();
                    m.setGame(competitionHistory.getGameName());
                    m.setPoints(competitionHistory.getScore());
                    m.setGameId(competitionHistory.getGameId());
                    m.setDate(competitionHistory.getPlayedOn());
                    m.setInstructions(competitionHistory.getInstructions());
                    m.setGameSyllabus(competitionHistory.getGameSyllabus());
                    m.setDuration(competitionHistory.getTimeTaken());
                    model.add(m);
                }

            }
            HIstoryAdapter adapter = new HIstoryAdapter(model, this);
            recyclerView.setAdapter(adapter);
        }

    }

    private void updateCardView(CompetitionHistory competitionHistory) {
        TextView score = lastCard.findViewById(R.id.score);
        TextView time = lastCard.findViewById(R.id.time);
        TextView gameName = lastCard.findViewById(R.id.gamename);
        TextView syllabus = lastCard.findViewById(R.id.syllabus);
        //TextView date = lastCard.findViewById(R.id.date);
        LinearLayout instructions = lastCard.findViewById(R.id.instruction);
        LinearLayout review = lastCard.findViewById(R.id.review);
        LinearLayout leaderboard = lastCard.findViewById(R.id.leaderboard);

        score.setText("" + competitionHistory.getScore());
        time.setText(competitionHistory.getTimeTaken());

        gameName.setText(competitionHistory.getGameName() + " - " + competitionHistory.getPlayedOn());
        instructions.setOnClickListener(v -> viewInstructions(0));
        review.setOnClickListener(v -> viewReview(0));
        leaderboard.setOnClickListener(v -> viewLeaderBoard(0));
        syllabus.setOnClickListener(v -> viewSyllabus(0));


    }

    private void dialogInit() {
        dialogSyllabus = new Dialog(HistoryActivity.this);
        dialogSyllabus.setContentView(R.layout.custom_layout_syllabus);
        dialogSyllabus.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        dialogSyllabus.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogSyllabus.setCancelable(false);
        syllabusRecyclerview = dialogSyllabus.findViewById(R.id.syllabusRecyclerview);

        dialog = new Dialog(HistoryActivity.this);
        dialog.setContentView(R.layout.custom_dialogue_competition);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);

    }

    private void loadSyllabusData(GameSyllabus syllabus) {
        syllabusModel.clear();
        syllabusRecyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        try {
            for (ChaptersInfo chapter : syllabus.getChapters()) {
                SyllabusCompetitionModel s = new SyllabusCompetitionModel();
                s.setChapter(chapter.getName());
                s.setChapterId(chapter.getId());
                s.setMicrolecture(chapter.getMicrolectures());
                s.setCompleted(chapter.isCompleted());
                syllabusModel.add(s);
            }
        } catch (Exception e) {
            Log.e("HISTORY_ACT", "getChapters on null object");
        }

        SyllabusCompetitionAdapter adapter = new SyllabusCompetitionAdapter(syllabusModel, this);
        syllabusRecyclerview.setAdapter(adapter);
        dialogSyllabus.show();
    }

    private void updateInstructions(List<List<String>> instructionsList) {
        if (dialog == null) {
            dialog = new Dialog(HistoryActivity.this);
            dialog.setContentView(R.layout.custom_dialogue_competition);
            dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
        dialog.show();
    }


    @Override
    public void viewLeaderBoard(int i) {
        Intent intent = new Intent(HistoryActivity.this, LeaderboardActivity.class);
        CompetitionHistory competitionHistory = competitionHistories.get(i);
        intent.putExtra("gameId", competitionHistory.getGameId());
        intent.putExtra("date", competitionHistory.getGameName() + " - " + competitionHistory.getPlayedOn());
        startActivity(intent);
    }

    @Override
    public void viewSyllabus(int i) {
        loadSyllabusData(competitionHistories.get(i).getGameSyllabus());
    }

    @Override
    public void viewReview(int i) {
        getGameQuestion(competitionHistories.get(i).getGameId());
    }

    @Override
    public void viewInstructions(int i) {
        updateInstructions(competitionHistories.get(i).getInstructions());
    }

    private void getGameQuestion(String gameId) {
        ProgressDialog progressDialog = ProgressDialog.show(this, getString(R.string.loading), getString(R.string.please_wait));
        Map<String, Object> map = new HashMap<>();
        map.put("gameId", gameId);
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(REVIEW, map), null,
                response -> {
                    try {
                        Gson gson = new Gson();
                        TypeToken<List<TestSeriesQuestionDto>> token = new TypeToken<List<TestSeriesQuestionDto>>() {
                        };
                        List<TestSeriesQuestionDto> competitionQuestion = gson.fromJson(response.getJSONArray("testSeriesQuestionDtos").toString(),
                                token.getType());
                        Intent intent = new Intent(this, arivihan.technologies.doubtbuzzter2.activity.CompetitionQuestion.class);
                        MyApplication.saveTestSeriesQuestions("gameQuestions", competitionQuestion);
                        MyApplication.saveData(PreferenceKeys.RESPONSE.getKey(), PreferenceKeys.RESPONSE.getKey());
                        intent.putExtra("gameId", gameId);
                        startActivity(intent);
                        progressDialog.dismiss();
                    } catch (JSONException e) {
                        progressDialog.dismiss();

                    }
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                progressDialog.dismiss();

                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
    }
}