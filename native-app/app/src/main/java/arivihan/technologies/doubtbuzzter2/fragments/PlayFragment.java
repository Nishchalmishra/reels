package arivihan.technologies.doubtbuzzter2.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.CompetitionActivity;
import arivihan.technologies.doubtbuzzter2.activity.StreakHomePage;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.listener.TapTargetListener;
import arivihan.technologies.doubtbuzzter2.model.GameLevelDto;
import arivihan.technologies.doubtbuzzter2.model.UserStatisticsDto;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_GAME_STATISTICS;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class PlayFragment extends Fragment implements TapTargetListener{

    RelativeLayout ParentLayout,Play,layoutStarts,competition, streak;
    TextView score;
    int ToWin = 0;
    ImageView histroy;
    int dataloaded = 0;
    List<GameLevelDto> gameList = new ArrayList<>();
    String levelId = "-1";
    int Score = 0;
    private boolean isFirst;
    NetworkStatusLiveData networkStatusLiveData;

    @Override
    public void onResume() {
        super.onResume();
//        if(MyApplication.getStringData(PreferenceKeys.USER_SCORE.getKey()) != null)
//            score.setText(getString(R.string.YourScore)+" : "+MyApplication.getStringData(PreferenceKeys.USER_SCORE.getKey()));
        if(dataloaded == 0 && CommonMethods.isOnline(getActivity())) {
            getUserGameStatisticsAPI();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_play, container, false);

        ParentLayout = view.findViewById(R.id.ParentRelativeLayout);
        Play = view.findViewById(R.id.play);
        competition = view.findViewById(R.id.compete);
        streak = view.findViewById(R.id.streak);
        histroy = view.findViewById(R.id.history);
        histroy.setVisibility(View.GONE);
        score = view.findViewById(R.id.score);
        //layoutStarts = view.findViewById(R.id.relativeLayout4);

        //Animation scale = AnimationUtils.loadAnimation(getActivity(), R.anim.scale);
        //layoutStarts.startAnimation(scale);

        // API call only once----
//        if(MyApplication.getStringData(PreferenceKeys.USER_SCORE.getKey()) != null) {
//            String Score = MyApplication.getStringData(PreferenceKeys.USER_SCORE.getKey()) + "";
//            score.setText("Your Score : " + Score);
//        }


        competition.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CompetitionActivity.class);
            startActivity(intent);
        });

        networkStatusLiveData = new NetworkStatusLiveData(requireContext());
        networkStatusLiveData.observe(getViewLifecycleOwner(), isOnline -> {
            if(isOnline){
                if(dataloaded == 0)
                    getUserGameStatisticsAPI();
                if(!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    CommonMethods.showPositiveSnackbar(ParentLayout,getActivity().getString(R.string.InternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(),true);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(),false);
                }
            }
            else{
                if(!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())){
                    CommonMethods.showNegativeSnackbar(ParentLayout,getActivity().getString(R.string.NoInternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(),false);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(),true);
                }
            }
        });

        if(CommonMethods.isOnline(getActivity())){
            if(dataloaded == 0)
                getUserGameStatisticsAPI();
            if(!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(),true);
                MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(),false);
            }
        }
        else{
            if(!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())){
                CommonMethods.showNegativeSnackbar(ParentLayout,getActivity().getString(R.string.NoInternetConnection));
                MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(),true);
                MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(),false);
            }
        }


        streak.setOnClickListener(view1 -> {
            if(MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                dataloaded = 0;
                Intent intent = new Intent(getActivity(), StreakHomePage.class);
                intent.putExtra("isFirst", isFirst);
                intent.putExtra("levelId", levelId);
                intent.putExtra("ToWin",String.valueOf(ToWin));
                intent.putExtra("Score",Score+"");
                intent.putExtra("Status","");
                startActivity(intent);
            }
            else{
                CommonMethods.showNegativeSnackbar(ParentLayout,getString(R.string.NoInternetConnection));
            }
        });


        return view;
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
                    Score = list.getScore();
                    dataloaded = 1;
                    levelId = list.getCurrentLevelId();
                    MyApplication.saveData(PreferenceKeys.USER_SCORE.getKey(),String.valueOf(Score));
                    gameList = list.getGameLevelDtoList();
                    isFirst = false;
                    int index = 0;
                    for(GameLevelDto g : gameList){
                        if(g.getId().equals(levelId)){
                            ToWin = g.getPointToWin();
                            if (index == 0) {
                               isFirst = true;
                            }
                            break;
                        }
                        index++;
                    }

                }, new VolleyGlobalErrorListener(getActivity()) {
            @Override
            public void postError(VolleyError error) {
                CommonMethods.showNegativeSnackbar(ParentLayout,error.getMessage());
            }
        });
        MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(myJsonObjectRequest2);

    }


    @Override
    public void backPressed() {

    }
}