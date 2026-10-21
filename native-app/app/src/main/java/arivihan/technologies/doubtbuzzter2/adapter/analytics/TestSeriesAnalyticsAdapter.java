package arivihan.technologies.doubtbuzzter2.adapter.analytics;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.MockAnalysisSummaryChildAdapter;
import arivihan.technologies.doubtbuzzter2.model.SectionModel;
import arivihan.technologies.doubtbuzzter2.model.TestChapterAnalysisDetails;

public class TestSeriesAnalyticsAdapter extends RecyclerView.Adapter<TestSeriesAnalyticsAdapter.ViewHolder> {


    Context context;
    List<TestChapterAnalysisDetails> list;

    public TestSeriesAnalyticsAdapter(Context context, List<TestChapterAnalysisDetails> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.test_analysis_summary_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        TestChapterAnalysisDetails testChapterAnalysisDetails = list.get(position);
        if (position == 0)
            holder.divider.setVisibility(View.GONE);

        holder.divider.setBackgroundColor(testChapterAnalysisDetails.isStrength() ? context.getResources().getColor(R.color.mock_green) : context.getResources().getColor(R.color.mock_red));

        holder.title.setText(list.get(position).getName());
        holder.questionDetail.setText(String.format(testChapterAnalysisDetails.getCorrect() + "/" + testChapterAnalysisDetails.getTotal()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout divider;
        TextView title;
        TextView questionDetail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            divider = itemView.findViewById(R.id.divider);
            title = itemView.findViewById(R.id.title);
            questionDetail = itemView.findViewById(R.id.question_detail);
        }
    }

}
