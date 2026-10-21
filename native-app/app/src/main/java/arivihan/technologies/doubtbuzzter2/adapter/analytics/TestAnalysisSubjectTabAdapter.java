package arivihan.technologies.doubtbuzzter2.adapter.analytics;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.constants.TestSeriesSection;
import arivihan.technologies.doubtbuzzter2.model.Tabs;
import arivihan.technologies.doubtbuzzter2.model.TestAnalyticsData;

public class TestAnalysisSubjectTabAdapter extends RecyclerView.Adapter<TestAnalysisSubjectTabAdapter.MockAnalysisSubjectTagViewHolder> {

    Context context;
    List<TestAnalyticsData> list;
    SubjectTabClick subjectTabClick;
    int selectedTab=0;

    public TestAnalysisSubjectTabAdapter(Context context, List<TestAnalyticsData> list,
                                         SubjectTabClick subjectTabClick) {
        this.context = context;
        this.list = list;
        this.subjectTabClick = subjectTabClick;
    }

    @NonNull
    @Override
    public MockAnalysisSubjectTagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.test_analysis_subject_tab_view_item, parent, false);
        return new MockAnalysisSubjectTagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MockAnalysisSubjectTagViewHolder holder, int position) {

        TestAnalyticsData tab = list.get(position);
//        if(list.size()==1)
//            list.get(position).setOn(true);

        if (selectedTab==position) {
            holder.card.setStrokeWidth(0);
            holder.card.setCardBackgroundColor(context.getResources().getColor(R.color.mock_primary_color));
            holder.title.setTextColor(context.getResources().getColor(R.color.white));
        }else{
            holder.card.setStrokeWidth(2);
            holder.card.setStrokeColor(context.getResources().getColor(R.color.mock_card_boundary));
            holder.card.setCardBackgroundColor(context.getResources().getColor(R.color.white));
            holder.title.setTextColor(context.getResources().getColor(R.color.mock_dark_grey));
        }

        holder.title.setText(CommonMethods.capitalizeWords(tab.getSubject()));
        holder.itemView.setOnClickListener(v -> {
            selectedTab = position;
            subjectTabClick.subjectClick(tab);
            this.notifyDataSetChanged();
        });

    }

    public void selectItem(int position) {
        if (position >= 0 && position < list.size()) {
            selectedTab = position;
            subjectTabClick.subjectClick(list.get(position));
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MockAnalysisSubjectTagViewHolder extends RecyclerView.ViewHolder {

        MaterialCardView card;
        TextView title;

        public MockAnalysisSubjectTagViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.tab_card);
            title = itemView.findViewById(R.id.tab_title);
        }
    }

    public interface SubjectTabClick {
        void subjectClick(TestAnalyticsData testAnalyticsData);
    }
}
