package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.listener.OnQuestionListener;
import arivihan.technologies.doubtbuzzter2.model.BoardLectureExpandedModel;
import arivihan.technologies.doubtbuzzter2.model.BoardLectureScreenModel;
import arivihan.technologies.doubtbuzzter2.model.LectureContentType;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureContentDto;
import arivihan.technologies.doubtbuzzter2.model.MicrolectureBoardContentInfoDto;

public class BoardLectureScreenAdapter extends RecyclerView.Adapter<BoardLectureScreenAdapter.BoardLectureScreenViewHolder> {

    ArrayList<BoardLectureScreenModel> list;
    ArrayList<BoardLectureExpandedModel> expanded_list = new ArrayList<>();

    Context context;

    Boolean isExpanded = false;
    OnQuestionListener onQuestionListener;

    public BoardLectureScreenAdapter(ArrayList<BoardLectureScreenModel> list, Context context, OnQuestionListener onQuestionListener) {
        this.list = list;
        this.context = context;
        this.onQuestionListener = onQuestionListener;
    }

    @NonNull
    @Override
    public BoardLectureScreenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.board_lecture_screen_item, parent, false);
        return new BoardLectureScreenViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BoardLectureScreenViewHolder holder, int position) {
        MicrolectureBoardContentInfoDto data = list.get(position).getData();

        if (data.getType() == LectureContentType.NOTES) {
            holder.download.setVisibility(View.VISIBLE);
            holder.progress.setVisibility(View.GONE);
        } else {
            holder.download.setVisibility(View.GONE);
            holder.progress.setVisibility(View.VISIBLE);
        }

        isExpanded = list.get(position).getData().isExpanded();
        if (isExpanded) {
            expanded_list = new ArrayList<>();

            for (int i = 0; i < data.getData().size(); i++) {
                MicroLectureContentDto microLectureContentDto = data.getData().get(i);
                expanded_list.add(new BoardLectureExpandedModel(microLectureContentDto.getProgress(), microLectureContentDto.getTitle(), microLectureContentDto));
            }
            BoardLectureExpandableAdapter boardLectureExpandableAdapter = new BoardLectureExpandableAdapter(expanded_list, context, onQuestionListener);
            holder.expanded_rv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
            holder.expanded_rv.setAdapter(boardLectureExpandableAdapter);
            holder.expanded_rv.setVisibility(View.VISIBLE);
        } else {
            holder.expanded_rv.setVisibility(View.GONE);
        }

        holder.title.setText(data.getTitle());
        holder.icon.setImageDrawable(list.get(position).getIcon());
        holder.progress.setProgress(data.getProgress());
        holder.layout.setOnClickListener(view -> {
            if (list.get(position).getData().getType() == LectureContentType.NOTES) {
                onQuestionListener.notesClicked(list.get(position).getData().getData().get(0).getContentUrl());
            } else {
                isExpanded = !list.get(position).getData().isExpanded();
                list.get(position).getData().setExpanded(isExpanded);
                if (isExpanded) {
                    onQuestionListener.lastExpanded(position);
                    expanded_list = new ArrayList<>();

                    for (int i = 0; i < data.getData().size(); i++) {
                        MicroLectureContentDto microLectureContentDto = data.getData().get(i);
                        expanded_list.add(new BoardLectureExpandedModel(microLectureContentDto.getProgress(), microLectureContentDto.getTitle(), microLectureContentDto));
                    }
                    BoardLectureExpandableAdapter boardLectureExpandableAdapter = new BoardLectureExpandableAdapter(expanded_list, context, onQuestionListener);
                    holder.expanded_rv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
                    holder.expanded_rv.setAdapter(boardLectureExpandableAdapter);
                    holder.expanded_rv.setVisibility(View.VISIBLE);
                    int[] originalPos = new int[2];
                    holder.layout.getLocationInWindow(originalPos);
                    onQuestionListener.oneExpandClicked(originalPos[0], originalPos[1]);
                } else {
                    holder.expanded_rv.setVisibility(View.GONE);
                }
            }
        });


    }

    public void expandView(int position) {
        for (int i = 0; i < list.size(); i++) {
            list.get(position).getData().setExpanded(false);
        }
        list.get(position).getData().setExpanded(true);
        notifyDataSetChanged();

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class BoardLectureScreenViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        ImageView icon, download;

        RecyclerView expanded_rv;
        CircularProgressIndicator progress;
        ConstraintLayout layout;

        public BoardLectureScreenViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title_text);
            layout = itemView.findViewById(R.id.layout);
            icon = itemView.findViewById(R.id.icon_image);
            progress = itemView.findViewById(R.id.progress_indicator);
            expanded_rv = itemView.findViewById(R.id.expanded_rv);
            download = itemView.findViewById(R.id.download);
        }
    }
}
