package arivihan.technologies.doubtbuzzter2.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.Shimmer;

import java.util.ArrayList;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.NotesModel;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> implements Filterable {

    ArrayList<NotesModel> models;
    ArrayList<NotesModel> listFull;
    OnNotesClickListener onNotesClickListener;

    LinearLayout noResultFoundLayout;

    public NotesAdapter(ArrayList<NotesModel> models,OnNotesClickListener onNotesClickListener,LinearLayout noResultFoundLayout) {
        this.models = models;
        this.listFull = new ArrayList<>(models);
        this.onNotesClickListener = onNotesClickListener;
        this.noResultFoundLayout = noResultFoundLayout;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.quick_notes_layout,parent,false);
        return new NotesAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Shimmer shimmer = new Shimmer.ColorHighlightBuilder()
                .setBaseColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.shimmer_base_color))
                .setBaseAlpha(1)
                .setHighlightColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.shimmer_highlight_color))
                .setHighlightAlpha(1)
                .setDropoff(50)
                .build();

        holder.createdBy.setText(models.get(position).getCreatedBy());
        holder.chapter.setText(models.get(position).getChapterName());
        holder.topic.setText(models.get(position).getTopicName());

        holder.itemView.setOnClickListener(view -> {
            models.get(position).setType("Auto");
            onNotesClickListener.onNotesClick(models.get(position).getPosition());
        });


    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private final Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<NotesModel> filteredlist = new ArrayList<>();

            if(constraint == null || constraint.length() == 0){
                filteredlist.addAll(listFull);
            }
            else{
                String pattern = constraint.toString().toLowerCase().trim();
                int index = 0;
                for(NotesModel cp : listFull){
                    String chap = cp.getTopicName();
                    if(chap.toLowerCase().contains(pattern))
                        filteredlist.add(cp);
                    index++;
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredlist;
            results.count = filteredlist.size();
            return results;

        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            models.clear();
            if (filterResults == null || filterResults.values == null) {
                notifyDataSetChanged();
                return;
            }
            models.addAll((List)filterResults.values);
            if(filterResults.count == 0){
                noResultFoundLayout.setVisibility(View.VISIBLE);
            }else{
                noResultFoundLayout.setVisibility(View.GONE);
            }
            notifyDataSetChanged();
        }
    };

    @Override
    public int getItemCount() {
        return models.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView topic,chapter,createdBy;
        ConstraintLayout typeCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            topic = itemView.findViewById(R.id.topic_name);
            chapter = itemView.findViewById(R.id.chapter_name);
            createdBy = itemView.findViewById(R.id.created_by);
            typeCard = itemView.findViewById(R.id.type_card);
        }

        @Override
        public void onClick(View v) {

        }
    }

    public interface OnNotesClickListener{
        void onNotesClick(int position);

    }

}
