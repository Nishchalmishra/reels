package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.SectionModel;

public class MockAnalysisSummaryChildAdapter extends RecyclerView.Adapter<MockAnalysisSummaryChildAdapter.MockAnalysisSummaryChildViewHolder> {

    Context context;
    ArrayList<SectionModel> list;

    public MockAnalysisSummaryChildAdapter(Context context, ArrayList<SectionModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MockAnalysisSummaryChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.mock_analysis_summary_title_item,parent,false);
        return new MockAnalysisSummaryChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MockAnalysisSummaryChildViewHolder holder, int position) {

        if(position==0)
            holder.divider.setVisibility(View.GONE);

        if(list.get(position).getType().equalsIgnoreCase("Weakness"))
            holder.divider.setBackground(context.getResources().getDrawable(R.color.mock_red));

        holder.title.setText(list.get(position).getTitle());
        holder.questionDetail.setText(list.get(position).getSubtitle());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MockAnalysisSummaryChildViewHolder extends RecyclerView.ViewHolder {

        LinearLayout divider;
        TextView title;
        TextView questionDetail;
        public MockAnalysisSummaryChildViewHolder(@NonNull View itemView) {
            super(itemView);
            divider = itemView.findViewById(R.id.divider);
            title=itemView.findViewById(R.id.title);
            questionDetail=itemView.findViewById(R.id.question_detail);
        }
    }
}
