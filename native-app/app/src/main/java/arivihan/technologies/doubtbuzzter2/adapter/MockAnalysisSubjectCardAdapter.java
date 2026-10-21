package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.SectionModel;

public class MockAnalysisSubjectCardAdapter extends RecyclerView.Adapter<MockAnalysisSubjectCardAdapter.MockAnalysisSubjectCardViewHolder> {

    Context context;
    ArrayList<SectionModel> list;

    public MockAnalysisSubjectCardAdapter(Context context, ArrayList<SectionModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MockAnalysisSubjectCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.mock_subject_progress_card_item,parent,false);
        return new MockAnalysisSubjectCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MockAnalysisSubjectCardViewHolder holder, int position) {

        String colorCode = list.get(position).getColor();
        int color = Color.parseColor(colorCode);
        holder.colorTag.setBackgroundColor(color);
        holder.progressIndicator.setIndicatorColor(color);

        holder.title.setText(list.get(position).getTitle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.progressIndicator.setProgress(list.get(position).getUserScore(),true);
        }else{
            holder.progressIndicator.setProgress(list.get(position).getUserScore());
        }
        holder.scoreDetail.setText(list.get(position).getSubtitle());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MockAnalysisSubjectCardViewHolder extends RecyclerView.ViewHolder {

        LinearLayout colorTag;
        TextView title;
        LinearProgressIndicator progressIndicator;
        TextView scoreDetail;
        public MockAnalysisSubjectCardViewHolder(@NonNull View itemView) {
            super(itemView);
            colorTag = itemView.findViewById(R.id.color_tag);
            title = itemView.findViewById(R.id.card_title);
            progressIndicator = itemView.findViewById(R.id.attempt_progress);
            scoreDetail = itemView.findViewById(R.id.score_detail);

        }
    }
}
