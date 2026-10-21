package arivihan.technologies.doubtbuzzter2.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureContentDto;

public class QuestionsAdapter extends RecyclerView.Adapter<QuestionsAdapter.ViewHolder> {

    ArrayList<MicroLectureContentDto> questionModels;
    OnQuestionClick onQuestionClick;

    public QuestionsAdapter(ArrayList<MicroLectureContentDto> questionModels,OnQuestionClick onQuestionClick) {
        this.questionModels = questionModels;
        this.onQuestionClick = onQuestionClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.question_ui,parent,false);
        return new QuestionsAdapter.ViewHolder(view,onQuestionClick);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MicroLectureContentDto qm = questionModels.get(position);

        if(position == questionModels.size() - 1)
            holder.line.setVisibility(View.GONE);

        holder.question.setText(qm.getTitle()+"");
        if(qm.isCompleted()) {
            holder.image_done.setVisibility(View.VISIBLE);
            holder.image_notdone.setVisibility(View.INVISIBLE);
            holder.detail.setVisibility(View.INVISIBLE);
        }
        else {
            holder.image_notdone.setVisibility(View.VISIBLE);
            holder.image_done.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return questionModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView question;
        ImageView image_done,image_notdone,detail;
        OnQuestionClick onQuestionClick;
        LinearLayout line;

        public ViewHolder(@NonNull View itemView,OnQuestionClick onQuestionClick) {
            super(itemView);
            question = itemView.findViewById(R.id.question);
            image_done = itemView.findViewById(R.id.question_done);
            line = itemView.findViewById(R.id.lineEnd);
            image_notdone = itemView.findViewById(R.id.question_notdone);
            detail = itemView.findViewById(R.id.details);
            itemView.setOnClickListener(this);
            this.onQuestionClick = onQuestionClick;
        }

        @Override
        public void onClick(View view) {
            MicroLectureContentDto qm = questionModels.get(getAdapterPosition());
            onQuestionClick.onquestionclick(getAdapterPosition(),qm.getHeader_title());
        }
    }

    public interface OnQuestionClick{
        void onquestionclick(int position,String id);
    }

}
