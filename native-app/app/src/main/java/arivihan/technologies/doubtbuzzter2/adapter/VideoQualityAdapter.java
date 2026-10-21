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
import arivihan.technologies.doubtbuzzter2.model.VideoQualityDto;

public class VideoQualityAdapter extends RecyclerView.Adapter<VideoQualityAdapter.ViewHolder> {

    private final List<VideoQualityDto> videoQualityDtos;
    private VideoQualityListener listener;


    public VideoQualityAdapter(List<VideoQualityDto> videoQualityDtos, VideoQualityListener listener) {
        this.videoQualityDtos = videoQualityDtos;
        this.listener = listener;
    }


    @NonNull
    @Override
    public VideoQualityAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_quality_recycler_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoQualityAdapter.ViewHolder holder, final int position) {
        VideoQualityDto videoQualityDto = videoQualityDtos.get(position);

        boolean isSelected = videoQualityDto.isSelected();

        int drawable = isSelected ? R.drawable.ic_video_setting_selected : R.drawable.ic_video_setting_unselected;
        holder.checkImageView.setImageResource(drawable);

        //text
        holder.textView.setText(videoQualityDto.getQuality());
        holder.textView.setTypeface(null, isSelected ? Typeface.BOLD : Typeface.NORMAL);
        holder.textView.setTextColor(
                ContextCompat.getColor(
                        holder.itemView.getContext(),
                        isSelected ? R.color.black : R.color.video_player_setting_text_unselected
                )
        );

        //bf
        holder.cardView.setCardBackgroundColor(isSelected? ContextCompat.getColor(holder.itemView.getContext(), R.color.video_quality_selected_bg) : Color.TRANSPARENT);

        holder.layout.setOnClickListener(view -> {
            if (!videoQualityDto.isSelected()) {
                listener.qualityClicked(position);

            } else {
                listener.closeQualityDialog();
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoQualityDtos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView checkImageView;
        private final MaterialCardView cardView;
        private final TextView textView;
        private final LinearLayout layout;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.parent_card_view);
            checkImageView = itemView.findViewById(R.id.checkImageView);
            textView = itemView.findViewById(R.id.text);
            layout = itemView.findViewById(R.id.video_quality_recycler_view_id);
        }
    }

    public interface VideoQualityListener {
        void qualityClicked(int position);
        void closeQualityDialog();
    }
}