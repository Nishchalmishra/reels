package arivihan.technologies.doubtbuzzter2.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.HistoryModel;

public class HIstoryAdapter extends RecyclerView.Adapter<HIstoryAdapter.ViewHolder> {

    ArrayList<HistoryModel> model;
    HistoryListener historyListener;

    public HIstoryAdapter(ArrayList<HistoryModel> model, HistoryListener historyListener) {
        this.model = model;
        this.historyListener = historyListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.history_layout,parent,false);
        return new HIstoryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.score.setText(""+model.get(position).getPoints());
            holder.date.setText(model.get(position).getDate());
            holder.time.setText(model.get(position).getDuration());
            holder.review.setOnClickListener(v -> {
                historyListener.viewReview(position);
            });
            holder.syllabus.setOnClickListener(v -> {
                historyListener.viewSyllabus(position);
            });
            holder.leaderBoard.setOnClickListener(v -> {
                historyListener.viewLeaderBoard(position);
            });
            holder.instructions.setOnClickListener(v -> {
                historyListener.viewInstructions(position);
            });
            holder.position.setText("" + (position + 1));
    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView score,date,time, instructions, position;
        TextView review, leaderBoard, syllabus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            score = itemView.findViewById(R.id.score);
            date = itemView.findViewById(R.id.date);
            time = itemView.findViewById(R.id.timeTaken);
            position = itemView.findViewById(R.id.position);
            review = itemView.findViewById(R.id.review);
            leaderBoard = itemView.findViewById(R.id.leaderboard);
            instructions = itemView.findViewById(R.id.instruction);
            syllabus = itemView.findViewById(R.id.syllabus);

        }
    }

    public interface HistoryListener {
        void viewLeaderBoard(int i);
        void viewSyllabus(int i);
        void viewReview(int i);
        void viewInstructions(int i);


    }
}
