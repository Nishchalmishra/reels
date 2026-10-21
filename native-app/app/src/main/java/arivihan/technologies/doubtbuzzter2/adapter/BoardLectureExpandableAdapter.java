package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.listener.OnQuestionListener;
import arivihan.technologies.doubtbuzzter2.model.BoardLectureExpandedModel;

public class BoardLectureExpandableAdapter extends RecyclerView.Adapter<BoardLectureExpandableAdapter.BoardLectureExpandableViewHolder> {

        ArrayList<BoardLectureExpandedModel> list;
        Context context;
        OnQuestionListener onQuestionListener;

        public BoardLectureExpandableAdapter(ArrayList<BoardLectureExpandedModel> list, Context context, OnQuestionListener onQuestionListener) {
            this.list = list;
            this.context = context;
            this.onQuestionListener = onQuestionListener;
        }

    @NonNull
    @Override
    public BoardLectureExpandableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.board_lecture_expanded_item,parent,false);
        return new BoardLectureExpandableViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull BoardLectureExpandableViewHolder holder, int position) {
        BoardLectureExpandedModel boardLectureExpandedModel = list.get(position);
        holder.title.setText(boardLectureExpandedModel.getTitle());
        if (boardLectureExpandedModel.getMicroLectureContentDto().isCompleted()) {
            holder.completed.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_baseline_check_circle_24));
        } else {
            holder.completed.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_ring_ellipse));
        }

        holder.constraintLayout.setOnClickListener(v -> {
            onQuestionListener.questionClicked(boardLectureExpandedModel.getMicroLectureContentDto(), position,
                    list);
        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class BoardLectureExpandableViewHolder extends RecyclerView.ViewHolder {

            TextView title;
            ImageView completed;
            ConstraintLayout constraintLayout;

            public BoardLectureExpandableViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.title_text_long_answer);
                completed = itemView.findViewById(R.id.question_notdone);
                constraintLayout = itemView.findViewById(R.id.constraint);
            }
        }


}
