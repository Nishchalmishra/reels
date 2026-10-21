package arivihan.technologies.doubtbuzzter2.adapter;

/*
 * File: ChapterDetailActivityAdapter Copyright 14/2/21 12:36 AM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 14/2/21 12:36 AM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.listener.ChapterDetailActivityListener;
import arivihan.technologies.doubtbuzzter2.model.LectureContentType;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureContentDto;

public class ChapterDetailActivityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int LAYOUT_HEADER= 0;
    private static final int LAYOUT_CONTENT= 1;
    private Context context;
    private String selectedChapter;
    private List<MicroLectureContentDto> data;
    private ChapterDetailActivityListener chapterDetailActivityListener;
    private HashMap<String, String> resumeMap = new HashMap<>();


    public ChapterDetailActivityAdapter(Context context,
                                        List<MicroLectureContentDto> microLectureContentDtoList,
                                        ChapterDetailActivityListener chapterDetailActivityListener, HashMap<String, String> resumeMap, String selectedChapter) {
        this.context = context;
        this.data = microLectureContentDtoList;
        this.chapterDetailActivityListener = chapterDetailActivityListener;
        this.resumeMap = resumeMap;
        this.selectedChapter = selectedChapter;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =null;
        RecyclerView.ViewHolder viewHolder = null;

        if(viewType==LAYOUT_HEADER)
        {
            view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.custom_layout_chapter_detail_title, parent,false);
            viewHolder = new ViewHolderHeader(view);
        }
        else
        {
            view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.custom_layout_chapter_detail, parent,false);
            viewHolder= new ViewHolderContent(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

        MicroLectureContentDto microLectureContentDto = data.get(position);

        if (holder.getItemViewType() == LAYOUT_HEADER) {
            ViewHolderHeader viewHolderHeader = (ViewHolderHeader) holder;

            if(resumeMap.containsKey("zResume"+CommonMethods.modifyString(selectedChapter.toLowerCase() + "_"
                    + microLectureContentDto.getTitle().replace(" ","_").toLowerCase()))) {
                long resumeDurationForThis = Long.valueOf(resumeMap.get("zResume"+CommonMethods.modifyString(selectedChapter.toLowerCase() + "_"
                        + microLectureContentDto.getTitle().replace(" ","_").toLowerCase())).toString());
                long total = microLectureContentDto.getVideoCompletionEndPos() - microLectureContentDto.getDefaultStartTime();
                long progress = resumeDurationForThis*100 / total;
                viewHolderHeader.mlProgressBar.setProgress((int)progress);
                viewHolderHeader.mlProgressBar.setVisibility(View.VISIBLE);
            }

            if(microLectureContentDto.getTitle().length()>30) {
                viewHolderHeader.header.setText(microLectureContentDto.getTitle().substring(0,30)+"...");
            }
            else {
                viewHolderHeader.header.setText(microLectureContentDto.getTitle());
            }
            if (microLectureContentDto.isCompleted()) {
                viewHolderHeader.check.setText("\u2713");
                viewHolderHeader.header.setPaintFlags(viewHolderHeader.header.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                viewHolderHeader.mlProgressBar.setVisibility(View.GONE);

            } else  {
                viewHolderHeader.check.setText(">");
                viewHolderHeader.image.setVisibility(View.GONE);
                viewHolderHeader.check.setVisibility(View.VISIBLE);
            }

            if (microLectureContentDto.getLectureContentType().getType().
                    equals(LectureContentType.VIDEO.getType()) &&
                    !microLectureContentDto.isCompleted()) {
                viewHolderHeader.check.setVisibility(View.GONE);
                viewHolderHeader.image.setVisibility(View.VISIBLE);
            }

            viewHolderHeader.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chapterDetailActivityListener.goToActivity(data.get(position), position);
                }
            });


        } else {
            ViewHolderContent viewHolderHeader = (ViewHolderContent) holder;
            viewHolderHeader.headerContent.setText(microLectureContentDto.getTitle());
            if (microLectureContentDto.isCompleted()) {
                viewHolderHeader.checkContent.setText("\u2713");
                viewHolderHeader.headerContent.setPaintFlags(viewHolderHeader.headerContent.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else  {
                viewHolderHeader.checkContent.setText(">");
                viewHolderHeader.imageContent.setVisibility(View.GONE);
                viewHolderHeader.checkContent.setVisibility(View.VISIBLE);
            }

            if (microLectureContentDto.getLectureContentType().getType().
                    equals(LectureContentType.VIDEO.getType()) &&
                    !microLectureContentDto.isCompleted()) {
                viewHolderHeader.checkContent.setVisibility(View.GONE);
                viewHolderHeader.imageContent.setVisibility(View.VISIBLE);
            }
            ImageView imageView = viewHolderHeader.imageContent;
            if(microLectureContentDto.getLectureContentType().getType().
                    equals(LectureContentType.CONCEPTUAL_QUESTION.getType())) {
                imageView.setImageResource(R.drawable.ic_emoji_objects_black_48dp);
            }
            else if(microLectureContentDto.getLectureContentType().getType().
                    equals(LectureContentType.NUMERICAL_QUESTION.getType())) {
                imageView.setImageResource(R.drawable.ic_calculate_black_48dp);
            }
            else if(microLectureContentDto.getLectureContentType().getType().
                    equals(LectureContentType.NOTES.getType())) {
                imageView.setImageResource(R.drawable.ic_sticky_note_2_black_48dp);
            }
            else {
                imageView.setImageResource(R.drawable.ic_sticky_note_2_black_48dp);
            }
            if (!microLectureContentDto.isHeader())
                imageView.setVisibility(View.VISIBLE);

            viewHolderHeader.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chapterDetailActivityListener.goToActivity(data.get(position), position);
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(data.get(position).isHeader())
            return LAYOUT_HEADER;
        else
            return LAYOUT_CONTENT;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolderHeader extends RecyclerView.ViewHolder {
        private TextView header;
        private TextView check;
        private ImageView image;
        private ProgressBar mlProgressBar;
        private ConstraintLayout layout;
        public ViewHolderHeader(@NonNull View itemView) {
            super(itemView);
            mlProgressBar = itemView.findViewById(R.id.microlectureProgressBar);
            header = itemView.findViewById(R.id.textView);
            check = itemView.findViewById(R.id.textView2);
            image = itemView.findViewById(R.id.imageViewTitle);
            layout = itemView.findViewById(R.id.header_layout);
        }
    }

    public static class ViewHolderContent extends RecyclerView.ViewHolder {
        private TextView headerContent;
        private TextView checkContent;
        private ImageView imageContent;
        private ConstraintLayout layout;
        public ViewHolderContent(@NonNull View itemView) {
            super(itemView);
            headerContent = itemView.findViewById(R.id.textView);
            checkContent = itemView.findViewById(R.id.textView2);
            imageContent = itemView.findViewById(R.id.imageView);
            layout = itemView.findViewById(R.id.content_layout);
        }
    }
}
