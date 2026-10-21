package arivihan.technologies.doubtbuzzter2.adapter;

import android.annotation.SuppressLint;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.GameLevelModel;

public class GameLevelAdapter extends RecyclerView.Adapter<GameLevelAdapter.ViewHolder> {

    long finalDiff = 0;
    private ArrayList<GameLevelModel> models;
    OnGameLevelClick onGameLevelClick;

    public GameLevelAdapter(ArrayList<GameLevelModel> models,OnGameLevelClick onGameLevelClick) {
        this.models = models;
        this.onGameLevelClick = onGameLevelClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.game_layout,parent,false);
        return new GameLevelAdapter.ViewHolder(view,onGameLevelClick);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            GameLevelModel m = models.get(position);
            long diff = 0;
            if(position == 0) {
                holder.points.setText("TO WIN : " + m.getToWin());
                holder.lock.setVisibility(View.GONE);
                holder.btn_text.setVisibility(View.VISIBLE);
                holder.level.setText(m.getLevelName());
                long time = System.currentTimeMillis();
                long UnLockTime = Long.parseLong(m.getUnLockTime());
                if (time < UnLockTime) {
                    diff = UnLockTime - time;
                    long fixedDiff = diff;
                    holder.timer = new CountDownTimer(diff, 1000) {
                        public void onTick(long millisUntilFinished) {
                            long remainingTime = (millisUntilFinished) / 1000;
                            long Remain = remainingTime;
                            holder.btn_text.setText("Unlock level in " + remainingTime + " sec");
                            if(remainingTime > 60){
                                remainingTime = remainingTime / 60;
                                holder.btn_text.setText("Unlock level in " + remainingTime + " min");
                            }
                            if(remainingTime > 60 && Remain > 60*60) {
                                remainingTime = remainingTime / 60;
                                holder.btn_text.setText("Unlock level in " + remainingTime + " hour");
                            }
                            if(remainingTime > 24 && Remain > 60*60*60){
                                remainingTime = remainingTime / 24;
                                holder.btn_text.setText("Unlock level in " + remainingTime + " day");
                            }
                        }

                        public void onFinish() {
                            holder.btn_text.setText(MyApplication.getInstance().getString(R.string.play));
                            finalDiff = 1;
                        }
                    }.start();
                }
                else{
                    finalDiff = 1;
                }
            }
            else {
                holder.help.setVisibility(View.GONE);
                holder.points.setText("TO WIN : "+m.getToWin());
                holder.btn_text.setVisibility(View.GONE);
                holder.level.setText(m.getLevelName());
            }
            if(position == models.size() - 1){
                holder.points.setText("");
                holder.btn_text.setVisibility(View.VISIBLE);
                holder.btn_text.setText(MyApplication.getInstance().getString(R.string.please_wait));
                holder.level.setText(MyApplication.getInstance().getString(R.string.more_level_coming_soon));
            }
        holder.play_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(position != models.size() - 1 && position == 0 && finalDiff == 1)
                        onGameLevelClick.onlevelClick(0,0);
                }
            });
            holder.help.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(position == 0)
                        onGameLevelClick.onlevelClick(0,1);
                }
            });
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView lock,help;
        TextView points,level,btn_text;
        OnGameLevelClick onGameLevelClick;
        RelativeLayout play_btn;
        CountDownTimer timer;

        public ViewHolder(@NonNull View itemView,OnGameLevelClick onGameLevelClick) {
            super(itemView);
            lock = itemView.findViewById(R.id.btn_lock);
            points = itemView.findViewById(R.id.points);
            level = itemView.findViewById(R.id.level);
            help = itemView.findViewById(R.id.help);
            btn_text = itemView.findViewById(R.id.btn_text);
            play_btn = itemView.findViewById(R.id.play);
            this.onGameLevelClick = onGameLevelClick;
        }

        @Override
        public void onClick(View view) {

        }
    }

    public interface OnGameLevelClick{
        void onlevelClick(int position,int help);
    }
}

