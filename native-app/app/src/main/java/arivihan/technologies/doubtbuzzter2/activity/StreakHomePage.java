package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_GAME_STATISTICS;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.GameLevelAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.model.GameLevelDto;
import arivihan.technologies.doubtbuzzter2.model.GameLevelModel;
import arivihan.technologies.doubtbuzzter2.model.UserStatisticsDto;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

/*
 * File: StreakHomePage Copyright 6/6/21 7:36 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 6/6/21 7:36 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

public class StreakHomePage extends LocaleActivity implements GameLevelAdapter.OnGameLevelClick {


    Dialog dialog;
    List<GameLevelDto> gameList = new ArrayList<>();
    String status = "";
    RecyclerView recyclerView;
    String ToWin = "-1";
    String levelId = "-1";
    String Score = "-1";
    ProgressBar progressBar;
    int recyclerViewLoaded = 0;
    ArrayList<GameLevelModel> model = new ArrayList<>();
    String unlockTime = "";
    RelativeLayout ParentLayout;
    int dataloaded = 0;
    private UserStatisticsDto userStatisticsDto;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streak_home_page);

        levelId = getIntent().getStringExtra("levelId");
        Score = getIntent().getStringExtra("Score");
        ToWin = getIntent().getStringExtra("ToWin");
        status = getIntent().getStringExtra("Status");
        boolean isFirst = getIntent().getBooleanExtra("isFirst", false);

        ParentLayout = findViewById(R.id.ParentRelativeLayout);
        progressBar = findViewById(R.id.progressbar);
        recyclerView = findViewById(R.id.recyclerview);

        dialog = new Dialog(StreakHomePage.this);
        dialog.setContentView(R.layout.custom_dialogue_game_intro);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);


        CardView cancel = dialog.findViewById(R.id.cancel);

        if(isFirst)
            dialog.show();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });



        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL ,false));

        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            if(isOnline){
                if(dataloaded == 0)
                    getUserGameStatisticsAPI();
                if(!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    CommonMethods.showPositiveSnackbar(ParentLayout,getString(R.string.InternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(),true);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(),false);
                }
            }
            else{
                if(!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())){
                    CommonMethods.showNegativeSnackbar(ParentLayout,getString(R.string.NoInternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(),false);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(),true);
                }
            }
        });

        if(CommonMethods.isOnline(this)){
            if(dataloaded == 0)
                getUserGameStatisticsAPI();
            if(!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(),true);
                MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(),false);
            }
        }
        else{
            if(!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())){
                CommonMethods.showNegativeSnackbar(ParentLayout,getString(R.string.NoInternetConnection));
                MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(),true);
                MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(),false);
            }
        }


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
                    userStatisticsDto = gson.fromJson(response.toString(), token.getType());
                    dataloaded = 1;
                    MyApplication.saveData(PreferenceKeys.USER_SCORE_LOWER.getKey(),Score);
                    gameList = userStatisticsDto.getGameLevelDtoList();
                    unlockTime = userStatisticsDto.getNextUnlockTime();
                    loadDataToRecyclerView();
                }, new VolleyGlobalErrorListener(StreakHomePage.this) {
            @Override
            public void postError(VolleyError error) {
                CommonMethods.showNegativeSnackbar(ParentLayout,error.getMessage());
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);

    }


    private void loadDataToRecyclerView() {
        model.clear();
        if (StringUtils.isNotEmpty(levelId)) {
            int LEVEL_ID = Integer.parseInt(levelId);
            for (GameLevelDto game : gameList) {
                int GAME_LEVEL_ID = Integer.parseInt(game.getId());
                if (LEVEL_ID <= GAME_LEVEL_ID) {
                    GameLevelModel gameModel = new GameLevelModel();
                    gameModel.setLevelId(game.getId());
                    gameModel.setToWin(game.getPointToWin() + "");
                    gameModel.setUnLockTime(unlockTime);
                    gameModel.setLevelName(game.getName());
                    model.add(gameModel);
                }
            }
        }
        GameLevelModel gameModel = new GameLevelModel();
        gameModel.setLevelId("More Levels are coming soon");
        gameModel.setUnLockTime(unlockTime);
        gameModel.setLevelName("Level");
        gameModel.setToWin("");
        model.add(gameModel);
        if(recyclerViewLoaded == 0) {
            recyclerViewLoaded = 1;
            progressBar.setVisibility(View.GONE);
            GameLevelAdapter Adapter = new GameLevelAdapter(model, StreakHomePage.this);
            recyclerView.setAdapter(Adapter);
        }
    }


    @Override
    public void onlevelClick(int position,int help) {
        if(!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())){
            CommonMethods.showNegativeSnackbar(ParentLayout,getString(R.string.NoInternetConnection));
        }
        else if (position == 0 && help == 0) {
            GameLevelModel gameLevelModel = model.get(position);
            Intent intent = new Intent(StreakHomePage.this,StreakQuestionActivity.class);
            intent.putExtra("CurrentGameId", userStatisticsDto.getCurrentGameId());
            intent.putExtra("StartedOn", userStatisticsDto.getStartedOn());
            intent.putExtra("LevelName",gameLevelModel.getLevelName());
            intent.putExtra("LevelId",gameLevelModel.getLevelId());
            intent.putExtra("ToWin",gameLevelModel.getToWin());
            intent.putExtra("ToWin",gameLevelModel.getToWin());
            startActivity(intent);
            finish();
        }
        if(help == 1 && position == 0){
            dialog.show();
        }
        else if(position != 0){
            CommonMethods.showNegativeSnackbar(ParentLayout,getString(R.string.PleaseClearPrevLevel));
        }
    }
}