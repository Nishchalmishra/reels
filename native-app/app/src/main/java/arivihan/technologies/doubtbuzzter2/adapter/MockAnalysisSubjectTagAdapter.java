package arivihan.technologies.doubtbuzzter2.adapter;

import static arivihan.technologies.doubtbuzzter2.model.SubjectEnum.BIOLOGY;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.constants.TestSeriesSection;
import arivihan.technologies.doubtbuzzter2.model.Tabs;

public class MockAnalysisSubjectTagAdapter extends RecyclerView.Adapter<MockAnalysisSubjectTagAdapter.MockAnalysisSubjectTagViewHolder> {

    Context context;
    ArrayList<Tabs> list;
    TestSeriesSection section;
    MockAnalysisSectionTabClick clickListener;

    public MockAnalysisSubjectTagAdapter(Context context, ArrayList<Tabs> list, TestSeriesSection section,
                                         MockAnalysisSectionTabClick clickListener) {
        this.context = context;
        this.list = list;
        this.section = section;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public MockAnalysisSubjectTagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.mock_analysis_tab_view_item, parent, false);
        return new MockAnalysisSubjectTagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MockAnalysisSubjectTagViewHolder holder, int position) {

        if(list.size()==1)
            list.get(position).setOn(true);

        if (list.get(position).isOn()) {
            holder.card.setStrokeWidth(0);
            holder.card.setCardBackgroundColor(context.getResources().getColor(R.color.mock_primary_color));
            holder.title.setTextColor(context.getResources().getColor(R.color.white));
        }else{
            holder.card.setStrokeWidth(2);
            holder.card.setStrokeColor(context.getResources().getColor(R.color.mock_card_boundary));
            holder.card.setCardBackgroundColor(context.getResources().getColor(R.color.white));
            holder.title.setTextColor(context.getResources().getColor(R.color.mock_dark_grey));
        }

        holder.title.setText(list.get(position).getTitle());
        holder.itemView.setOnClickListener(v -> {
            String type = null;
            for (int i = 0; i < list.size(); i++) {
                if (i == position) {
                    type = list.get(i).getKey();
                    list.get(i).setOn(true);
                } else {
                    list.get(i).setOn(false);
                }
            }
            clickListener.sectionClicked(type, this.section);
            this.notifyDataSetChanged();
        });

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

    public interface MockAnalysisSectionTabClick {
        void sectionClicked(String type, TestSeriesSection section);
    }
}
