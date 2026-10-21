package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.helper.FileDownloadHelper;
import arivihan.technologies.doubtbuzzter2.model.batch.note.ToppersNotesModel;
import arivihan.technologies.doubtbuzzter2.model.offline.notes.NoteEntity;
import lombok.Getter;

public class ToppersNotesAdapter extends RecyclerView.Adapter<ToppersNotesAdapter.ViewHolder> {

    @Getter
    private final ArrayList<ToppersNotesModel> list;
    private final OnNotesClickListener onNotesClickListener;

    // Track active downloads to avoid duplicate taps
    private final Map<String, Long> activeDownloads = new HashMap<>();

    public ToppersNotesAdapter(ArrayList<ToppersNotesModel> list, OnNotesClickListener onNotesClickListener) {
        this.list = list;
        this.onNotesClickListener = onNotesClickListener;
    }

    @NonNull
    @Override
    public ToppersNotesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ToppersNotesAdapter.ViewHolder holder, int position) {
        ToppersNotesModel item = list.get(position);
        Context context = holder.itemView.getContext();

        holder.tv_name.setText(item.getChapterTitle());
//        boolean isLocked = item.isLocked();
        holder.rl_download.setVisibility(View.VISIBLE);
        holder.rl_download_progress.setVisibility(View.GONE);
        holder.iv_status_lock.setVisibility(View.GONE);
        holder.iv_status_download_completed.setVisibility(View.GONE);

        if (item.getProgress() > 0 || (StringUtils.isNotEmpty(item.getStatus()) && (item.getStatus().equals(NoteEntity.STATUS_IN_QUEUE)
                || item.getStatus().equals(NoteEntity.STATUS_PENDING) || item.getStatus().equals(NoteEntity.STATUS_DOWNLOADING)))) {
            holder.iv_status_lock.setVisibility(View.GONE);
            holder.iv_status_download_completed.setVisibility(View.GONE);
            holder.rl_download.setVisibility(View.GONE);
            holder.rl_download_progress.setVisibility(View.VISIBLE);

            holder.tv_progress.setText(item.getProgress() + "%");
            holder.pb_progress.setProgress(item.getProgress());
        }

        if (item.isDownloadComplete()) {
            holder.iv_status_lock.setVisibility(View.GONE);
            holder.iv_status_download_completed.setVisibility(View.VISIBLE);
            holder.rl_download.setVisibility(View.GONE);
            holder.rl_download_progress.setVisibility(View.GONE);
        }

        if (item.isLocked()) {
            holder.tv_name.setTextColor(ContextCompat.getColor(context, R.color.gray_8a));
            holder.iv_status_lock.setVisibility(View.VISIBLE);
            holder.iv_status_download_completed.setVisibility(View.GONE);
            holder.rl_download.setVisibility(View.GONE);
            holder.rl_download_progress.setVisibility(View.GONE);
        } else {
            holder.tv_name.setTextColor(ContextCompat.getColor(context, R.color.black));
        }

        final String fileName = FileDownloadHelper.buildSafeFileName(item.getChapterTitle(), item.getNotesUrl());
        final boolean alreadyDownloaded = FileDownloadHelper.isFileDownloaded(context, fileName);

//        if (!isLocked) {
//            holder.downloadIcon.setVisibility(View.VISIBLE);
//            holder.downloadProgress.setVisibility(View.GONE);
//        } else {
//            holder.downloadIcon.setVisibility(View.GONE);
//            holder.downloadProgress.setVisibility(View.GONE);
//        }
//        holder.downloadIcon.setOnClickListener(View->{
//            holder.downloadIcon.setOnClickListener(v -> {
//                if (!isLocked && !alreadyDownloaded) {
//                    holder.downloadIcon.setVisibility(View.GONE);
//                    holder.downloadProgress.setVisibility(View.VISIBLE);
//
//                    NotesRepository repository = new NotesRepository((Application) context.getApplicationContext());
//                    repository.downloadNotes(context, new NoteEntity(
//                            item.getChapterTitle(),
//                            item.getSubjectId(),
//                            item.getSubjectName(),
//                            item.getChapterTitle(),
//                            item.getNotesBy(),
//                            item.getNotesUrl(),
//                            item.isLocked(),
//                            item.getPageCount(),
//                            0,
//                            "pdf",
//                            NoteEntity.STATUS_PENDING,
//                            0,
//                            null
//                    ));
//                }
//            });
//
//        });

        holder.itemView.setOnClickListener(view -> {
            onNotesClickListener.onNotesClick(item.getChapterTitle(), item.getNotesUrl(),item.getNotesType(),item.getSubjectName(),item.isLocked());
        });

        holder.rl_download.setOnClickListener(v -> {
            onNotesClickListener.onNoteDownloadClick(position, item.getNotesUrl(), item.isLocked(), item);
        });

        holder.rl_download_progress.setOnClickListener(v -> {
            onNotesClickListener.onNoteStatusClick(position, item);
        });

        holder.iv_status_download_completed.setOnClickListener(v -> {
            onNotesClickListener.onNoteStatusClick(position, item);
        });

        holder.iv_status_lock.setOnClickListener(v -> {
            onNotesClickListener.onNotesClick(item.getChapterTitle(),
                    item.getNotesUrl(),item.getNotesType(),item.getSubjectName(),item.isLocked());
        });

    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_name, tv_progress;
        private ProgressBar pb_progress;
        private RelativeLayout rl_download, rl_download_progress;
        private LinearLayout ll_name;
        private ImageView iv_status_lock, iv_status_download_completed;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_progress = itemView.findViewById(R.id.tv_progress);
            pb_progress = itemView.findViewById(R.id.pb_progress);
            rl_download = itemView.findViewById(R.id.rl_download);
            rl_download_progress = itemView.findViewById(R.id.rl_download_progress);
            iv_status_lock = itemView.findViewById(R.id.iv_status_lock);
            iv_status_download_completed = itemView.findViewById(R.id.iv_status_download_completed);
            ll_name = itemView.findViewById(R.id.ll_name);
        }
    }

    public interface OnNotesClickListener {
        void onNotesClick(String chapterTitle, String notesUrl, String notesType, String subjectName, boolean locked);
        void onNoteDownloadClick(int position, String url, boolean isLocked, Object object);
        void onNoteStatusClick(int position, Object object);

    }
}
