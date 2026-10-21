package arivihan.technologies.doubtbuzzter2.adapter;/*
 * File: InstantVideoRecyclerAdapter Copyright 04/10/24, 3:25 am Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 04/10/24, 3:25 am
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.MLResponse;

public class InstantVideoRecyclerAdapter extends RecyclerView.Adapter<InstantVideoRecyclerAdapter.ViewHolder> {

    private Context context;
    private List<MLResponse> mlResponseList;
    private InstantVideoRecyclerListener listener;
    public InstantVideoRecyclerAdapter(Context context, List<MLResponse> mlResponseList, InstantVideoRecyclerListener listener) {
        this.context = context;
        this.mlResponseList = mlResponseList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InstantVideoRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.video_card_view, parent, false);
        return new InstantVideoRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InstantVideoRecyclerAdapter.ViewHolder holder, int position) {
        MLResponse mlResponse = mlResponseList.get(position);
        holder.microLectureTitle.setText(mlResponse.getTextToDisplay());
        holder.timer.setText(convertMillisToMinutesAndSeconds(mlResponse.getLength()));
        holder.imageView.setOnClickListener(view -> {
            listener.intentClickedFromView(mlResponse);
        });
    }

    public static String convertMillisToMinutesAndSeconds(long millis) {
        long minutes = millis / 1000 / 60;  // Convert to minutes
        long seconds = (millis / 1000) % 60;  // Get remaining seconds

        // If the total time is less than 1 minute, return "~1 min"
        if (minutes == 0 && seconds > 0) {
            return "~1 min";
        }

        // Format as "MM:SS min" for times 1 minute and above
        return String.format(Locale.ENGLISH,"%02d:%02d min", minutes, seconds);
    }

    @Override
    public int getItemCount() {
        return mlResponseList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView microLectureTitle;
        TextView timer;
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            microLectureTitle = itemView.findViewById(R.id.micro_lecture_title);
            timer = itemView.findViewById(R.id.microlecture_duration);
            imageView = itemView.findViewById(R.id.micro_lecture_thumbnail);
        }
    }

    public interface InstantVideoRecyclerListener {
        void intentClickedFromView(MLResponse mlResponse);
    }
}
