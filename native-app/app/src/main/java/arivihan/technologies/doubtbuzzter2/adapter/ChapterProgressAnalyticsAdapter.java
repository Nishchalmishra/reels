package arivihan.technologies.doubtbuzzter2.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.ChapterProgressDto;

public class ChapterProgressAnalyticsAdapter extends RecyclerView.Adapter<ChapterProgressAnalyticsAdapter.ViewHolder> {

    private ArrayList<ChapterProgressDto> model;

    public ChapterProgressAnalyticsAdapter(ArrayList<ChapterProgressDto> model) {
        this.model = model == null ? new ArrayList<>() : new ArrayList<>(model);
    }

    public void updateData(ArrayList<ChapterProgressDto> newModel) {
        if (newModel == null) {
            newModel = new ArrayList<>();
        }

        this.model = new ArrayList<>(newModel);

        notifyDataSetChanged();
    }

    public void addItem(ChapterProgressDto item) {
        if (item != null) {
            model.add(item);
            notifyItemInserted(model.size() - 1);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.chapter_progress_analytics_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position < 0 || position >= model.size()) {
            return;
        }

        ChapterProgressDto currentItem = model.get(position);
        if (currentItem != null) {
            holder.chapter.setText(currentItem.getChapterName());
            holder.progressBar.setProgress(currentItem.getProgress());
            holder.progressText.setText(String.format("%d %%", currentItem.getProgress()));
        }
    }

    @Override
    public int getItemCount() {
        return model == null ? 0 : model.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView chapter, progressText;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            chapter = itemView.findViewById(R.id.topic_name);
            progressBar = itemView.findViewById(R.id.progressBar);
            progressText = itemView.findViewById(R.id.progress_text);
        }
    }
}