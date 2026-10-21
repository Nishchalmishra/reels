package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.MicrolectureListActivity;
import arivihan.technologies.doubtbuzzter2.activity.VideoPlayerActivity;
import arivihan.technologies.doubtbuzzter2.model.LectureContentType;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureContentDto;
import arivihan.technologies.doubtbuzzter2.model.SearchResult;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

/*
 * File: GlobalSearchVideoAdapter Copyright 05/02/24, 4:38 pm Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 05/02/24, 4:38 pm
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

public class GlobalSearchVideoAdapter extends RecyclerView.Adapter<GlobalSearchVideoAdapter.LectureInFocusViewHolder> {

    Context context;
    ArrayList<SearchResult> list;
    GlobalSearchVideoInterface globalSearchVideoInterface;

    public GlobalSearchVideoAdapter(Context context, ArrayList<SearchResult> list, GlobalSearchVideoInterface globalSearchVideoInterface) {
        this.context = context;
        this.list = list;
        this.globalSearchVideoInterface = globalSearchVideoInterface;
    }

    public GlobalSearchVideoAdapter(Context context, ArrayList<SearchResult> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public LectureInFocusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.lecture_focus_item,parent,false);
        return new LectureInFocusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LectureInFocusViewHolder holder, int position) {
        SearchResult searchResult = list.get(position);
        Log.d("searchResult", searchResult.toString());
        if(searchResult.getTurl()!=null) {
            Glide.with(context).load(searchResult.getTurl()).into(holder.videoThumbnail);
        }
        if(StringUtils.isNotEmpty(searchResult.getT())) {
            holder.title.setVisibility(View.VISIBLE);
            holder.title.setText(searchResult.getT());
        }
        if(StringUtils.isNotEmpty(searchResult.getSt())) {
            holder.subtitle.setVisibility(View.VISIBLE);
            holder.subtitle.setText(searchResult.getSt());
        }
        if(StringUtils.isNotEmpty(searchResult.getDes())) {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(searchResult.getDes());
        }

        holder.itemView.setOnClickListener(v -> {
            globalSearchVideoInterface.onVideoItemClick(searchResult.getT());
            if (searchResult.isStrmr()) {
                return;
            }
            if (searchResult.isDoubt()) {
                @OptIn(markerClass = UnstableApi.class) Intent intent = new Intent(context, VideoPlayerActivity.class);
                MicroLectureContentDto microLectureContentDto = new MicroLectureContentDto();
                microLectureContentDto.setDefaultStartTime(Math.toIntExact(searchResult.getVideoStartPos()));
                microLectureContentDto.setVideoCompletionEndPos(Math.toIntExact(searchResult.getVideoEndPos()));
                microLectureContentDto.setVideoCompletionStartPos(Math.toIntExact(searchResult.getVideoEndPos()));
                microLectureContentDto.setContentUrl(searchResult.getCurl());
                microLectureContentDto.setVideoUrl(searchResult.getCurl());
                microLectureContentDto.setId(searchResult.getMid());
                microLectureContentDto.setLectureContentType(LectureContentType.VIDEO);
                intent.putExtra(context.getResources().getString(R.string.passed_obj), microLectureContentDto);
                intent.putExtra("position", Integer.parseInt(String.valueOf(searchResult.getVideoStartPos())));
                intent.putExtra("shouldNotResume", true);
                intent.putExtra("startAtPosition", true);
                intent.putExtra("isAskDoubtActivity", true);
                splitIdAndKey(searchResult.getId(), searchResult.getCtype(), intent);
                context.startActivity(intent);
            } else {
                Intent intent = new Intent(context, MicrolectureListActivity.class);
                intent.putExtra("chapterName", searchResult.getSt());
                intent.putExtra("openVideo", true);
                splitIdAndKey(searchResult.getId(), searchResult.getCtype(), intent);
                context.startActivity(intent);
            }
        });
        Log.d("shimmerView", searchResult.toString());
        if (searchResult.isStrmr()) {
            holder.shimmerView.setVisibility(View.VISIBLE);
            holder.shimmerView.showShimmer(true);
            holder.dataView.setVisibility(View.GONE);
        } else {
            holder.shimmerView.setVisibility(View.GONE);
            holder.shimmerView.stopShimmer();
            holder.dataView.setVisibility(View.VISIBLE);
        }
    }
    void splitIdAndKey(String ids, String keys, Intent intent){
        List<String> id;
        List<String> key;
        if(ids!=null && keys!=null) {
            id = Arrays.asList(ids.split("##"));
            key = Arrays.asList(keys.split("##"));
            for (int i = 0; i < id.size(); i++) {
                intent.putExtra(key.get(i), id.get(i));
                if (key.get(i).equalsIgnoreCase("microLectureId")) {
                    intent.putExtra(PreferenceKeys.MICROLECTURE_ID.getKey(), id.get(i));
                }
            }
        }
        //context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class LectureInFocusViewHolder extends RecyclerView.ViewHolder {

        ImageView videoThumbnail;
        TextView title;
        TextView subtitle;
        TextView description;
        ShimmerFrameLayout shimmerView;
        LinearLayout dataView;
        public LectureInFocusViewHolder(@NonNull View itemView) {
            super(itemView);
            videoThumbnail = itemView.findViewById(R.id.video_thumbnail);
            title = itemView.findViewById(R.id.title_search_adapter);
            subtitle = itemView.findViewById(R.id.video_subtitle);
            description = itemView.findViewById(R.id.description);
            shimmerView = itemView.findViewById(R.id.shimmer_view);
            dataView = itemView.findViewById(R.id.data_view);
        }
    }

    public interface GlobalSearchVideoInterface{
        void onVideoItemClick(String title);
    }
}
