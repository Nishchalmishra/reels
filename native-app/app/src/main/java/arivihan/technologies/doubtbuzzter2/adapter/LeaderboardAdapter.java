package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.RewardsScreenDto;

/**
 * Renders ranked leaderboard rows using LeaderboardSection.LeaderboardItem directly.
 * LeaderboardUser DTO is no longer needed.
 */
public class LeaderboardAdapter extends
        RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private final Context context;
    private final List<RewardsScreenDto.LeaderboardSection.LeaderboardItem> items;

    public LeaderboardAdapter(
            Context context,
            List<RewardsScreenDto.LeaderboardSection.LeaderboardItem> items) {
        this.context = context;
        this.items   = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_leader_board, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RewardsScreenDto.LeaderboardSection.LeaderboardItem item = items.get(position);

        // ── Rank ───────────────────────────────────────────────────────────────
        holder.tvRank.setText(String.valueOf(item.getRank()));

        // ── Name ───────────────────────────────────────────────────────────────
        holder.tvName.setText(buildShortName(item.getStudentName()));

        Glide.with(context)
                .load(item.getRewardImageUrl())
                .placeholder(R.drawable.laptop_icon) // shown while loading
                .error(R.drawable.laptop_icon)       // fallback if failed
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.ivReward);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    /** Replace list contents and refresh the RecyclerView. */
    public void updateData(
            List<RewardsScreenDto.LeaderboardSection.LeaderboardItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private String buildShortName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "";
        String[] parts = fullName.trim().split("\\s+");
        return parts.length > 1
                ? parts[0] + " " + parts[parts.length - 1].charAt(0) + "."
                : parts[0];
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView  tvRank, tvName;
        ImageView ivReward;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank   = itemView.findViewById(R.id.tv_rank);
            tvName   = itemView.findViewById(R.id.tv_name);
            ivReward = itemView.findViewById(R.id.iv_reward);
        }
    }
}