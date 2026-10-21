package arivihan.technologies.doubtbuzzter2.adapter.competitive.topic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.competitive.topic.CompetitiveTopicDto;

public class CompetitiveTopicListCardAdapter extends RecyclerView.Adapter<CompetitiveTopicListCardAdapter.CompetitiveTopicCardHolder> {

    private final static String TAG = CompetitiveTopicListCardAdapter.class.getSimpleName();
    List<CompetitiveTopicDto> models;
    //    List<CompetitiveTopicDto> listFull;
    OnCompetitiveTopicClickListener listener;
    Context context;

    public CompetitiveTopicListCardAdapter(List<CompetitiveTopicDto> models, OnCompetitiveTopicClickListener listener, Context context) {
        this.models = new ArrayList<>(models);
//        this.listFull = new ArrayList<>(models);
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public CompetitiveTopicCardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_competitive_topic_list_card, parent, false);
        return new CompetitiveTopicCardHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull CompetitiveTopicCardHolder holder, int position) {
        CompetitiveTopicDto item = models.get(position);

        holder.tv_name.setText(item.getName());
        holder.cv_item_parent_layout.setOnClickListener(view -> {
            listener.onTopicClick(position, item);
        });

    }

    public List<CompetitiveTopicDto> getList() {
        return models;
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

//    @Override
//    public Filter getFilter() {
//        return filter;
//    }
//
//    private final Filter filter = new Filter() {
//
//        @Override
//        protected FilterResults performFiltering(CharSequence constraint) {
//
//            List<SamplePaperModel> filteredlist = new ArrayList<>();
//
//            if(constraint == null || constraint.length() == 0){
//                filteredlist.addAll(listFull);
//            } else{
//                String pattern = constraint.toString().toLowerCase().trim();
//                int index = 0;
//                for(SamplePaperModel cp : listFull){
//                    String samplePaper = cp.getTitle();
//                    if(samplePaper.toLowerCase().contains(pattern))
//                        filteredlist.add(cp);
//                    index++;
//                }
//            }
//
//            FilterResults results = new FilterResults();
//            results.values = filteredlist;
//            results.count = filteredlist.size();
//            return results;
//
//        }
//
//        @SuppressLint("NotifyDataSetChanged")
//        @Override
//        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
//            models.clear();
//            models.addAll((Collection<? extends SamplePaperModel>) filterResults.values);
//            notifyDataSetChanged();
//        }
//    };

    public static class CompetitiveTopicCardHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cv_item_parent_layout;
        private LinearLayout ll_item_parent_layout;
        private TextView tv_name;
        private ImageView iv_right_arrow;


        public CompetitiveTopicCardHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            iv_right_arrow = itemView.findViewById(R.id.iv_right_arrow);
            cv_item_parent_layout = itemView.findViewById(R.id.cv_item_parent_layout);
            ll_item_parent_layout = itemView.findViewById(R.id.ll_item_parent_layout);
        }
    }

    public interface OnCompetitiveTopicClickListener {
        void onTopicClick(int position, CompetitiveTopicDto competitiveTopicDto);
    }

}
