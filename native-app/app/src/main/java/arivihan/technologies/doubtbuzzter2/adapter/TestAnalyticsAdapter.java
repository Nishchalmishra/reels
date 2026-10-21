package arivihan.technologies.doubtbuzzter2.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.TestChapterAnalysisDetails;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class TestAnalyticsAdapter extends RecyclerView.Adapter<TestAnalyticsAdapter.ViewHolder> {

    List<TestChapterAnalysisDetails> analyticsModels;

    public TestAnalyticsAdapter(List<TestChapterAnalysisDetails> analyticsModels) {
        this.analyticsModels = analyticsModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.test_analy_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.topic.setText(analyticsModels.get(position).getName());

            int cPer = (analyticsModels.get(position).getCorrect()*100/analyticsModels.get(position).getTotal());
            int wPer = (analyticsModels.get(position).getWrong()*100/analyticsModels.get(position).getTotal());

            holder.correct.setText(analyticsModels.get(position).getCorrect()+"");
            holder.wrong.setText(analyticsModels.get(position).getWrong()+"");
            holder.progressCorrect.setProgress(cPer);
            holder.progressWrong.setProgress(100 - wPer);
            holder.unattempted.setText((analyticsModels.get(position).getTotal() - analyticsModels.get(position).getCorrect() - analyticsModels.get(position).getWrong())+"");

            int cutOff = 50;
            int fail = 50;

            if(MyApplication.getIntData(PreferenceKeys.CORRECT_PERCENTAGE.getKey()) != -1){
                cutOff = MyApplication.getIntData(PreferenceKeys.CORRECT_PERCENTAGE.getKey());
                fail = MyApplication.getIntData(PreferenceKeys.WRONG_PERCENTAGE.getKey());
            }

        if(wPer > fail){
                holder.card.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorAccent2));
            }
        else if(cPer > cutOff){
            holder.card.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.lightGreen));
        }
        else{
            holder.card.setCardBackgroundColor(Color.WHITE);
            }
    }

    @Override
    public int getItemCount() {
        return analyticsModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView topic,correct,wrong,unattempted;
        ProgressBar progressCorrect,progressWrong;
        MaterialCardView card;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            topic = itemView.findViewById(R.id.topic_name);
            progressCorrect = itemView.findViewById(R.id.progressBarCorrect);
            progressWrong = itemView.findViewById(R.id.progressBarWrong);
            correct = itemView.findViewById(R.id.progress_text);
            wrong = itemView.findViewById(R.id.progress_text2);
            card = itemView.findViewById(R.id.card);
            unattempted = itemView.findViewById(R.id.progress_text_unattempted);

        }
    }
}
