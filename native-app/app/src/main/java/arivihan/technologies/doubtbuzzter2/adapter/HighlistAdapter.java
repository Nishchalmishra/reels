package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.facebook.shimmer.Shimmer;
import com.facebook.shimmer.ShimmerDrawable;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.HighlistModel;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class HighlistAdapter extends RecyclerView.Adapter<HighlistAdapter.HighlightViewHolder> {
    private static final String TAG = "HighlistAdapter";
    private final List<HighlistModel> highlistModelList;
    private final OnHighlightClick onHighlightClick;
    private final Context context;
    private ExoPlayer firstVideoPlayer;
    private boolean isFirstVideoVisible = false;
    private boolean isRecyclerVisible = false;


    public HighlistAdapter(List<HighlistModel> highlistModelList, OnHighlightClick onHighlightClick, Context context) {
        this.highlistModelList = highlistModelList;
        this.onHighlightClick = onHighlightClick;
        this.context = context;
    }

    @NonNull
    @Override
    public HighlightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.highlist_layout, parent, false);
        return new HighlightViewHolder(view, onHighlightClick);
    }

    @Override
    public void onBindViewHolder(@NonNull HighlightViewHolder holder, int position) {
        HighlistModel highlistModel = highlistModelList.get(position);

        Shimmer shimmer = new Shimmer.ColorHighlightBuilder()
                .setBaseColor(ContextCompat.getColor(context, R.color.shimmer_base_color))
                .setHighlightColor(ContextCompat.getColor(context, R.color.shimmer_highlight_color))
                .setDropoff(50)
                .build();

        ShimmerDrawable shimmerDrawable = new ShimmerDrawable();
        shimmerDrawable.setShimmer(shimmer);

        if (position == 0) {
            holder.highlightsImageView.setVisibility(View.VISIBLE);
            holder.playerView.setVisibility(View.GONE);

            Glide.with(context)
                    .load(highlistModel.getImageUrl())
                    .into(holder.highlightsImageView);
//            if (isFirstVideoVisible && isRecyclerVisible && StringUtils.isNotEmpty(highlistModel.getVideoUrl())) {
//                setupFirstVideoPlayer(holder, highlistModel);
//            }

            holder.itemView.setOnClickListener(v -> {
                if (firstVideoPlayer != null) {
                    firstVideoPlayer.seekTo(0);
                    firstVideoPlayer.play();
                }
                if (onHighlightClick != null) {
                    onHighlightClick.onHighlightsClick(holder.getAdapterPosition());
                }
            });
        } else {
            holder.playerView.setVisibility(View.GONE);
            holder.highlightsImageView.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load(highlistModel.getImageUrl())
                    .into(holder.highlightsImageView);

            holder.itemView.setOnClickListener(v -> {
                if (onHighlightClick != null) {
                    onHighlightClick.onHighlightsClick(holder.getAdapterPosition());
                }
            });
        }
    }

    private void setupFirstVideoPlayer(@NonNull HighlightViewHolder holder, HighlistModel h) {
        boolean howToVideoVisible = MyApplication.getBoolData(PreferenceKeys.HOW_TO_VIDEO_VISIBLE.getKey());

        // If how_to_video_visible is true, do not play the first video
        if (howToVideoVisible) {
            Log.d(TAG, "How-to video is visible, skipping first video setup");
            return; // Skip the video setup
        }
        holder.playerView.postDelayed(() -> {
            if (holder.getAdapterPosition() == 0) {
                Log.d(TAG, "Setting up first video player");
                holder.highlightsImageView.setVisibility(View.GONE);
                holder.playerView.setVisibility(View.VISIBLE);

                releaseFirstVideoPlayer();

                firstVideoPlayer = new ExoPlayer.Builder(context).build();
                holder.playerView.setPlayer(firstVideoPlayer);
                firstVideoPlayer.setVolume(0f);

                holder.playerView.setUseController(false);

                firstVideoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);

                MediaItem mediaItem = MediaItem.fromUri(Uri.parse(h.getVideoUrl()));
                firstVideoPlayer.setMediaItem(mediaItem);
                firstVideoPlayer.prepare();
                firstVideoPlayer.play();
            }
        }, 1500);
    }

    @Override
    public int getItemCount() {
        return highlistModelList.size();
    }

    public void handleScroll(int firstVisibleItemPosition) {
        try {
            Log.d(TAG, "handleScroll called with position: " + firstVisibleItemPosition);
            if (firstVisibleItemPosition == 0) {
                if (!isFirstVideoVisible) {
                    isFirstVideoVisible = true;
                    notifyItemChanged(0);
                }
            } else {
                if (isFirstVideoVisible) {
                    isFirstVideoVisible = false;
                    releaseFirstVideoPlayer();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void releaseFirstVideoPlayer() {
        if (firstVideoPlayer != null) {
            firstVideoPlayer.stop();
            firstVideoPlayer.release();
            firstVideoPlayer = null;
        }
    }

    public void handleRecyclerVisibility(boolean isVisible) {
        if (this.isRecyclerVisible != isVisible) {
            this.isRecyclerVisible = isVisible;
            notifyItemChanged(0);
        }
    }

    public void stopPlayer() {
        releaseFirstVideoPlayer();
        isFirstVideoVisible = false;
        notifyItemChanged(0);
    }


    public static class HighlightViewHolder extends RecyclerView.ViewHolder {
        ImageView highlightsImageView;
        PlayerView playerView;
        OnHighlightClick onHighlightClick;

        public HighlightViewHolder(@NonNull View itemView, OnHighlightClick onHighlightClick) {
            super(itemView);
            this.onHighlightClick = onHighlightClick;
            highlightsImageView = itemView.findViewById(R.id.highlight_image_view);
            playerView = itemView.findViewById(R.id.player_view);
        }
    }
    public interface OnHighlightClick {
        void onHighlightsClick(int position);
    }

}