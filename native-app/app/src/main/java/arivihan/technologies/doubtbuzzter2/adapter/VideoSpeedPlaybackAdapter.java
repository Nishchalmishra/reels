package arivihan.technologies.doubtbuzzter2.adapter;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.VideoPlaybackSpeedDto;
import arivihan.technologies.doubtbuzzter2.model.VideoQualityDto;

public class VideoSpeedPlaybackAdapter extends RecyclerView.Adapter<VideoSpeedPlaybackAdapter.ViewHolder> {

    private final List<VideoPlaybackSpeedDto> playbackSpeedDtos;
    private VideoSpeedChangeListener listener;


    public VideoSpeedPlaybackAdapter(List<VideoPlaybackSpeedDto> playbackSpeedDtos, VideoSpeedChangeListener listener) {
        this.playbackSpeedDtos = playbackSpeedDtos;
        this.listener = listener;
    }


    @NonNull
    @Override
    public VideoSpeedPlaybackAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.playback_layout_recyler_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoSpeedPlaybackAdapter.ViewHolder holder, final int position) {
        VideoPlaybackSpeedDto playbackSpeedDto = playbackSpeedDtos.get(position);

        boolean isSelected = playbackSpeedDto.isSelected();

        int drawable = isSelected? R.drawable.ic_video_setting_selected : R.drawable.ic_video_setting_unselected;
        holder.checkImageView.setImageResource(drawable);

        //text
        holder.textView.setText(playbackSpeedDto.getSpeedText());
        holder.textView.setTypeface(null, isSelected ? Typeface.BOLD : Typeface.NORMAL);
        holder.textView.setTextColor(
                ContextCompat.getColor(
                        holder.itemView.getContext(),
                        isSelected ? R.color.black : R.color.video_player_setting_text_unselected
                )
        );

        //bf
        holder.cardView.setCardBackgroundColor(isSelected? ContextCompat.getColor(holder.itemView.getContext(), R.color.video_quality_selected_bg) : Color.TRANSPARENT);

        //layout
        holder.layout.setOnClickListener(view -> {
            listener.speedChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return playbackSpeedDtos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView checkImageView;
        private final  MaterialCardView cardView;
        private final TextView textView;
        private final LinearLayout layout;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.parent_card_view);
            checkImageView = itemView.findViewById(R.id.checkImageView);
            textView = itemView.findViewById(R.id.text);
            layout = itemView.findViewById(R.id.playback_speed_layout);
        }
    }

    public interface VideoSpeedChangeListener {
        void speedChanged(int position);
        void closeSpeedDialog();

    }

}