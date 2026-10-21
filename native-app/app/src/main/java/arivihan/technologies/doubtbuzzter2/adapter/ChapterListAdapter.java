package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.listener.ChapterListActivityListener;
import arivihan.technologies.doubtbuzzter2.model.ChapterListInfoDto;

/*
 * File: ChapterListAdapter Copyright 12/2/21 12:19 AM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 12/2/21 12:19 AM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

public class ChapterListAdapter extends RecyclerView.Adapter<ChapterListAdapter.ChapterViewHolder>{
    private final Context context;
    private List<ChapterListInfoDto> data;
    private ChapterListActivityListener chapterListActivityListener;

    public ChapterListAdapter(Context context, List<ChapterListInfoDto> data,
                               ChapterListActivityListener chapterListActivityListener) {
        this.context = context;
        this.data = data;
        this.chapterListActivityListener  = chapterListActivityListener;
    }

    @NonNull
    @Override
    public ChapterListAdapter.ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_layout_chapter_list, parent, false);
        return new ChapterViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull ChapterListAdapter.ChapterViewHolder holder, int position) {
        final ChapterListInfoDto chapterListInfoDto = data.get(position);
        holder.videoTitle.setText(chapterListInfoDto.getVideoTitle());
        holder.subTitle.setText(chapterListInfoDto.getVideoSubtitle());
        Glide.with(context).load(chapterListInfoDto.getImageUrl()).into(holder.videoImage);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chapterListActivityListener.chapterSelected(chapterListInfoDto);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ChapterViewHolder extends RecyclerView.ViewHolder {
        private final ImageView videoImage;
        private final TextView videoTitle;
        private final TextView subTitle;
        private final ProgressBar progressBar;
        private final CardView cardView;


        public ChapterViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_layout_chapter);
            videoImage = itemView.findViewById(R.id.imageViewList);
            videoTitle = itemView.findViewById(R.id.textviewList);
            subTitle = itemView.findViewById(R.id.textview2List);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}
