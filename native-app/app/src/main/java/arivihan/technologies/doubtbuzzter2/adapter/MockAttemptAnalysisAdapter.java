package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.AttemptAnalysisData;

public class MockAttemptAnalysisAdapter extends RecyclerView.Adapter<MockAttemptAnalysisAdapter.MockAttemptAnalysisViewHolder> {

    Context context;
    ArrayList<AttemptAnalysisData> list;

    public MockAttemptAnalysisAdapter(Context context, ArrayList<AttemptAnalysisData> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MockAttemptAnalysisViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.mock_attempt_analysis_item,parent,false);
        return new MockAttemptAnalysisViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MockAttemptAnalysisViewHolder holder, int position) {

        holder.title.setText(list.get(position).getTitle());
        holder.attemptDetail.setText(list.get(position).getSubtitle());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MockAttemptAnalysisViewHolder extends RecyclerView.ViewHolder {

        CardView tag ;
        TextView title;
        TextView attemptDetail;
        public MockAttemptAnalysisViewHolder(@NonNull View itemView) {
            super(itemView);
            tag = itemView.findViewById(R.id.color_tag);
            title = itemView.findViewById(R.id.attempt_status);
            attemptDetail = itemView.findViewById(R.id.attempt_detail);
        }
    }
}
