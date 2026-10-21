package arivihan.technologies.doubtbuzzter2.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.VideoQualityDto;

public class DownloadVideoQualityAdapter extends RecyclerView.Adapter<DownloadVideoQualityAdapter.ViewHolder> {

    private final List<VideoQualityDto> videoQualityDtos;
    private VideoQualityListener listener;

    public DownloadVideoQualityAdapter(List<VideoQualityDto> videoQualityDtos, VideoQualityListener listener) {
        this.videoQualityDtos = videoQualityDtos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DownloadVideoQualityAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.download_video_resolution, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull DownloadVideoQualityAdapter.ViewHolder holder, final int position) {
        VideoQualityDto videoQualityDto = videoQualityDtos.get(position);

        if (videoQualityDto.isSelected()) {
            //holder.checked.setVisibility(View.VISIBLE);
            holder.resolutionText.setTextColor(holder.resolutionText.getContext().getResources().getColor(R.color.arivihan_secondary_color));
            holder.sizeText.setTextColor(holder.sizeText.getContext().getResources().getColor(R.color.arivihan_secondary_color));
        } else {
            holder.checked.setVisibility(View.GONE);
            holder.resolutionText.setTextColor(holder.resolutionText.getContext().getResources().getColor(android.R.color.black));
            holder.sizeText.setTextColor(holder.sizeText.getContext().getResources().getColor(android.R.color.black));
        }

        holder.resolutionText.setText(videoQualityDto.getQuality());
        holder.sizeText.setText("(" + videoQualityDto.getSize() + " MB)");

        holder.layout.setOnClickListener(view -> {
            if (!videoQualityDto.isSelected()) {
                listener.qualityClicked(position);
            }
        });
    }


    @Override
    public int getItemCount() {
        return videoQualityDtos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView checked;
        private final TextView resolutionText;
        private final TextView sizeText;
        private final ConstraintLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checked = itemView.findViewById(R.id.checked);
            resolutionText = itemView.findViewById(R.id.resolution_text);
            sizeText = itemView.findViewById(R.id.size_text);
            layout = itemView.findViewById(R.id.video_quality_recycler_view_id);
        }
    }

    public interface VideoQualityListener {
        void qualityClicked(int position);

        void closeQualityDialog();
    }
}
