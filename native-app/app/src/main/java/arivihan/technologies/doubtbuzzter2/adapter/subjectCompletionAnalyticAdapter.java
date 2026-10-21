package arivihan.technologies.doubtbuzzter2.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.SubjectProgressDto;

public class subjectCompletionAnalyticAdapter extends RecyclerView.Adapter<subjectCompletionAnalyticAdapter.ViewHolder> {

    ArrayList<SubjectProgressDto> model;

    public subjectCompletionAnalyticAdapter(ArrayList<SubjectProgressDto> model) {
        this.model = model;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.subject_completion_analytics_layout,parent,false);
        return new subjectCompletionAnalyticAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.sub.setText(model.get(position).getSubjectName());
        holder.completion.setText(model.get(position).getProgress()+"%");
        holder.topic.setText(model.get(position).getChapterCovered()+"/"+model.get(position).getTotalChapter());

        if(position % 2 == 1){
            holder.relativeLayout.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.lighter_navy_blue));
        }
        else{
            holder.relativeLayout.setBackgroundColor(Color.WHITE);
        }

    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView sub,completion,topic;
        RelativeLayout relativeLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sub = itemView.findViewById(R.id.subjectName);
            completion = itemView.findViewById(R.id.completionPercentage);
            topic = itemView.findViewById(R.id.topicCovered);
            relativeLayout = itemView.findViewById(R.id.relativeLayoutBackground);
        }
    }
}
