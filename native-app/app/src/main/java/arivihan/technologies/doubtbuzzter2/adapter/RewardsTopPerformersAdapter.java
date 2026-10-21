package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.RewardsScreenDto;

public class RewardsTopPerformersAdapter
        extends RecyclerView.Adapter<RewardsTopPerformersAdapter.ViewHolder> {

    public interface OnPerformerVideoClickListener {
        void onPerformerVideoClick(String videoUrl);
    }

    private final Context context;
    private final OnPerformerVideoClickListener listener;
    private final List<RewardsScreenDto.TopPerformersSection.Performer> items;

    public RewardsTopPerformersAdapter(Context context,
                                       OnPerformerVideoClickListener listener,
                                       List<RewardsScreenDto.TopPerformersSection.Performer> items) {
        this.context  = context;
        this.listener = listener;
        this.items    = items;
    }

    public void updateData(List<RewardsScreenDto.TopPerformersSection.Performer> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.toppers_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RewardsScreenDto.TopPerformersSection.Performer item = items.get(position); // ← fixed

        Glide.with(context)
                .load(item.getThumbnailUrl())
                .into(holder.toppersImage);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && item.getVideoUrl() != null
                    && !item.getVideoUrl().isEmpty()) {
                listener.onPerformerVideoClick(item.getVideoUrl());
            }
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0; // ← fixed
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView toppersImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            toppersImage = itemView.findViewById(R.id.toppers_image);
        }
    }
}