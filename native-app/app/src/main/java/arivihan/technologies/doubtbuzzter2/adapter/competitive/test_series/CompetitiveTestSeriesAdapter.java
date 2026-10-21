package arivihan.technologies.doubtbuzzter2.adapter.competitive.test_series;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.competitive.test_series.CompetitiveTestSeriesDto;

public class CompetitiveTestSeriesAdapter extends RecyclerView.Adapter<CompetitiveTestSeriesAdapter.ViewHolder> {

    private final List<CompetitiveTestSeriesDto> list;
    private final OnTestSeriesClickListener listener;

    public interface OnTestSeriesClickListener {
        void onTestSeriesClick(CompetitiveTestSeriesDto testSeriesDto);
    }

    // Constructor with listener
    public CompetitiveTestSeriesAdapter(List<CompetitiveTestSeriesDto> list, OnTestSeriesClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.test_series_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CompetitiveTestSeriesDto testSeriesDto = list.get(position);

        holder.tv_sub_title.setText(StringUtils.isNotEmpty(testSeriesDto.getTestCountStr()) ? testSeriesDto.getTestCountStr() : "");
        holder.tv_title.setText(StringUtils.isNotEmpty(testSeriesDto.getName()) ? testSeriesDto.getName() : "");
        // Subject icon
        if (StringUtils.isNotEmpty(testSeriesDto.getIconUrl())) {
            Glide.with(holder.iv_image.getContext())
                    .load(testSeriesDto.getIconUrl())
                    .error(R.drawable.white_logo)
                    .into(holder.iv_image);
        }
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTestSeriesClick(testSeriesDto);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cv_parent_layout;
        TextView tv_title;
        TextView tv_sub_title;
        ImageView iv_image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cv_parent_layout = itemView.findViewById(R.id.card);
            tv_title = itemView.findViewById(R.id.exam_text);
            tv_sub_title = itemView.findViewById(R.id.chapters);
            iv_image = itemView.findViewById(R.id.profile_icon);
        }
    }
}
