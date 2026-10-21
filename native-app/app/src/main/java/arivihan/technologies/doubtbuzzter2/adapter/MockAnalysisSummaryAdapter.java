package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.ChapterSummaryDetail;
import lombok.Getter;
import lombok.Setter;

public class MockAnalysisSummaryAdapter extends RecyclerView.Adapter<MockAnalysisSummaryAdapter.MockAnalysisSummaryViewHolder> {

    Context context;
    ArrayList<ChapterSummaryDetail> list;
    String type;
    ChapterSummarySeeAllListener listener;
    @Getter
    @Setter
    private String summaryType="";

    public MockAnalysisSummaryAdapter(Context context,
                                      ArrayList<ChapterSummaryDetail> list,
                                      ChapterSummarySeeAllListener listener,
                                      String type) {
        this.context = context;
        this.list = list;
        this.listener = listener;
        this.type = type;
    }

    @NonNull
    @Override
    public MockAnalysisSummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.mock_analysis_summary_item, parent, false);
        return new MockAnalysisSummaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MockAnalysisSummaryViewHolder holder, int position) {

        if(!list.get(position).getDataList().isEmpty()) {
            holder.itemView.setVisibility(View.VISIBLE);
            holder.title.setText(list.get(position).getTitle());
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
            holder.recyclerView.setNestedScrollingEnabled(false);
            MockAnalysisSummaryChildAdapter mockAnalysisSummaryChildAdapter = new MockAnalysisSummaryChildAdapter(context, list.get(position).getDataList());
            holder.recyclerView.setAdapter(mockAnalysisSummaryChildAdapter);
            if (list.get(position).getType().equalsIgnoreCase("Weakness"))
                holder.recyclerView.setBackgroundColor(context.getResources().getColor(R.color.mock_light_red));
            holder.seeAllLayout.setOnClickListener(view -> {
                listener.seeAllClick(type, list.get(position).getType().equalsIgnoreCase("STRENGTH"),summaryType);
            });
        } else {
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        }

    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MockAnalysisSummaryViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        RecyclerView recyclerView;
        TextView seeAllText;
        LinearLayout seeAllLayout;
        public MockAnalysisSummaryViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.summary_title);
            seeAllText = itemView.findViewById(R.id.see_all_text);
            recyclerView = itemView.findViewById(R.id.summary_item_rv);
            seeAllLayout=itemView.findViewById(R.id.see_all_layout);
        }
    }

    public interface ChapterSummarySeeAllListener {
        void seeAllClick(String type, boolean strength, String summaryType);
    }
}
