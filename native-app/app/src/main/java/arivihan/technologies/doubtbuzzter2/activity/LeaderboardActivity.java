package arivihan.technologies.doubtbuzzter2.activity;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.LeaderboardAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.model.LeaderBoard;
import arivihan.technologies.doubtbuzzter2.model.LeaderBoardInfo;
import arivihan.technologies.doubtbuzzter2.model.LeaderboardModel;
import arivihan.technologies.doubtbuzzter2.model.LeaderboardUser;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.LEADER_BOARD;

public class LeaderboardActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<LeaderboardModel> model = new ArrayList<>();
    private CardView card;
    private LinearLayout thirdLayout, secondLayout;
    private TextView firstName, firstTime, firstPoint;
    private TextView secondName, secondTime, secondPoint;
    private TextView thirdName, thirdTime, thirdPoint, name;
    ImageView refresh;
    String gameId;
    String date;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        gameId = getIntent().getStringExtra("gameId");
        date = getIntent().getStringExtra("date");
        recyclerView = findViewById(R.id.userPerformaceRecyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(LeaderboardActivity.this, LinearLayoutManager.VERTICAL ,false));

        secondLayout = findViewById(R.id.second);
        secondLayout.setVisibility(View.GONE);
        thirdLayout = findViewById(R.id.third);
        thirdLayout.setVisibility(View.GONE);
        card = findViewById(R.id.card);
        firstName = findViewById(R.id.firstName);
        firstPoint = findViewById(R.id.firstPoint);
        firstTime = findViewById(R.id.firstTime);
        secondName = findViewById(R.id.secondName);
        secondPoint = findViewById(R.id.secondPoint);
        secondTime = findViewById(R.id.secondTime);
        thirdName = findViewById(R.id.thirdName);
        thirdPoint = findViewById(R.id.thirdPoint);
        thirdTime = findViewById(R.id.thirdTime);
        refresh = findViewById(R.id.refresh);
        name = findViewById(R.id.name);

        name.setText(date);

        firstName.setVisibility(View.GONE);
        firstPoint.setVisibility(View.GONE);
        firstTime.setVisibility(View.GONE);

        refresh.setOnClickListener(v -> {
            getGameLeaderBoard();
        });

        getGameLeaderBoard();

    }

    private void animate() {
        refresh.clearAnimation();
        Animation animation = new RotateAnimation(0.0f, 360.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        animation.setRepeatCount(-1);
        animation.setDuration(2000);
        refresh.startAnimation(animation);
    }

    public void getGameLeaderBoard() {
        animate();
        Map<String, Object> map = new HashMap<>();
        map.put("gameId", gameId);
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(LEADER_BOARD, map), null,
                response -> {
                    Gson gson = new Gson();
                    TypeToken<LeaderBoard> token = new TypeToken<LeaderBoard>() {};
                    LeaderBoard leaderBoard = gson.fromJson(response.toString(), token.getType());

                    updateLeaderBoard(leaderBoard);

                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                refresh.clearAnimation();
                Toast.makeText(getApplicationContext(), error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
    }

    private void updateLeaderBoard(LeaderBoard leaderBoard) {
        if (leaderBoard == null || CollectionUtils.isEmpty(leaderBoard.getLeaderBoardInfoList())) {
            card.setVisibility(View.GONE);
            refresh.clearAnimation();
            return;
        }

        List<LeaderBoardInfo> leaderBoardInfoList = leaderBoard.getLeaderBoardInfoList();

        if (leaderBoardInfoList.size() == 1) {
            secondLayout.setVisibility(View.GONE);
            thirdLayout.setVisibility(View.GONE);
        } else if (leaderBoardInfoList.size() == 2) {
            thirdLayout.setVisibility(View.GONE);
        }
        model.clear();

        int index = 1;
        for (LeaderBoardInfo leaderBoardInfo : leaderBoardInfoList) {

            if (index == 1) {
                firstName.setText(leaderBoardInfo.getUserName());
                firstTime.setText(leaderBoardInfo.getReadableTimeTaken());
                firstPoint.setText("" + leaderBoardInfo.getScoreObtained());
            } else if (index == 2) {
                secondLayout.setVisibility(View.VISIBLE);
                secondName.setText(leaderBoardInfo.getUserName());
                secondTime.setText(leaderBoardInfo.getReadableTimeTaken());
                secondPoint.setText("" + leaderBoardInfo.getScoreObtained());
            } else if (index == 3) {
                thirdLayout.setVisibility(View.VISIBLE);
                thirdName.setText(leaderBoardInfo.getUserName());
                thirdTime.setText(leaderBoardInfo.getReadableTimeTaken());
                thirdPoint.setText("" + leaderBoardInfo.getScoreObtained());
            } else {
                LeaderboardModel l = new LeaderboardModel();
                l.setRank(index);
                l.setName(leaderBoardInfo.getUserName());
                l.setPoint((int) leaderBoardInfo.getScoreObtained());
                l.setTimeTaken(leaderBoardInfo.getReadableTimeTaken());
                model.add(l);
            }
            index++;
        }

//        LeaderboardAdapter adapter = new LeaderboardAdapter(this,model);
//        recyclerView.setAdapter(adapter);
//        refresh.clearAnimation();
//        firstName.setVisibility(View.VISIBLE);
//        firstPoint.setVisibility(View.VISIBLE);
//        firstTime.setVisibility(View.VISIBLE);
    }


}