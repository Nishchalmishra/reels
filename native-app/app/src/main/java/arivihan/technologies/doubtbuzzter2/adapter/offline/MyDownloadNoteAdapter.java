package arivihan.technologies.doubtbuzzter2.adapter.offline;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.helper.offline.OfflineDownloadManager;
import arivihan.technologies.doubtbuzzter2.model.DownloadStatus;
import arivihan.technologies.doubtbuzzter2.model.offline.notes.NoteEntity;
import arivihan.technologies.doubtbuzzter2.model.offline.type.OfflineNoteConfigModel;
import arivihan.technologies.doubtbuzzter2.utils.PdfUtils;

public class MyDownloadNoteAdapter extends RecyclerView.Adapter<MyDownloadNoteAdapter.DownloadViewHolder> {

    private Context context;
    private List<NoteEntity> itemList;
    private final List<NoteEntity> fullList;
    private OfflineNoteConfigModel offlineNoteConfigModel;
    private final OnNoteClickListener noteClickListener;
    private final LinearLayout noResultFoundLayout;

    private static final String TAG = MyDownloadNoteAdapter.class.getSimpleName();


    public MyDownloadNoteAdapter(Context context, List<NoteEntity> itemList, OfflineNoteConfigModel offlineNoteConfigModel,
                                 OnNoteClickListener noteClickListener, LinearLayout noResultFoundLayout) {
        this.context = context;
        this.itemList = itemList;
        this.fullList = itemList;
        this.offlineNoteConfigModel = offlineNoteConfigModel;
        this.noteClickListener = noteClickListener;
        this.noResultFoundLayout = noResultFoundLayout;
        Log.d(TAG, "Adapter initialized with item list size: " + itemList.size());
    }

    @NonNull
    @Override
    public DownloadViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: Inflating view for item at position " + viewType);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_offline_note_list, parent, false);
        return new DownloadViewHolder(view);
    }

    @SuppressLint("StringFormatMatches")
    @Override
    public void onBindViewHolder(DownloadViewHolder holder, @SuppressLint("RecyclerView") int position) {
        NoteEntity item = itemList.get(position);
        Log.d(TAG, "onBindViewHolder: Binding data for item at position" + position);

        holder.tv_name.setText(item.getChapterTitle());
        holder.tv_page_count.setText(String.format(context.getString(R.string.pages_count), item.getPageCount()));
        holder.tv_size.setText(PdfUtils.getReadableFileSize(item.getFileSize()));
//        holder.tv_subject.setText(StringUtils.isNotEmpty(item.getSubjectName())?item.getSubjectName():"-");
//        holder.tv_type.setText(StringUtils.isNotEmpty(item.getNoteType())?item.getNoteType(): context.getString(R.string.note));
        holder.option.setOnClickListener(v -> noteClickListener.showDialog(item, position));
        String status = item.getStatus();

        Pair<String, String> noteTypeInfo = OfflineDownloadManager.getNoteTypeInfo(offlineNoteConfigModel, item.getNoteType(), StringUtils.isNotEmpty(item.getLanguage()) ? item.getLanguage() : "en");
        Pair<String, String> subjectInfo = OfflineDownloadManager.getSubjectInfo(offlineNoteConfigModel, item.getSubjectName(), StringUtils.isNotEmpty(item.getLanguage()) ? item.getLanguage() : "en");
        Log.d(TAG, "-->Note: " + noteTypeInfo.toString());
        Log.d(TAG, "-->Subject: " + subjectInfo.toString());
        holder.tv_subject.setText(StringUtils.isNotEmpty(subjectInfo.first) ? subjectInfo.first : "-");
        holder.tv_type.setText(StringUtils.isNotEmpty(noteTypeInfo.first) ? noteTypeInfo.first : context.getString(R.string.Notes));

        if (item.isLocked() || NoteEntity.STATUS_IN_QUEUE.equals(status) || NoteEntity.STATUS_PENDING.equals(status) ||
                NoteEntity.STATUS_DOWNLOADING.equals(status) || NoteEntity.STATUS_FAILED.equals(status)) {
            holder.tv_type.setTextColor(ContextCompat.getColor(context, R.color.gray_8a));
            holder.tv_subject.setTextColor(ContextCompat.getColor(context, R.color.gray_8a));
        } else {
            holder.tv_type.setTextColor(Color.parseColor(noteTypeInfo.second));
            holder.tv_subject.setTextColor(Color.parseColor(subjectInfo.second));
        }
        if (NoteEntity.STATUS_IN_QUEUE.equals(status) || NoteEntity.STATUS_PENDING.equals(status) ||
                NoteEntity.STATUS_DOWNLOADING.equals(status)) {
            holder.download_progress_layout.setVisibility(View.VISIBLE);
            holder.download_progress_bar.setProgress(item.getProgress());
            holder.download_progress_text.setText(item.getProgress() + "%");
            holder.option.setVisibility(View.GONE);
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.grey_300));
        } else if (NoteEntity.STATUS_FAILED.equals(status)) {
            holder.download_progress_layout.setVisibility(View.GONE);
            holder.option.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.grey_300));
        } else {
            if (item.isLocked()) {
                holder.iv_thumbnail.setColorFilter(ContextCompat.getColor(context, R.color.gray_e6), PorterDuff.Mode.SRC_IN);
//                holder.iv_thumbnail.setImageDrawable(context.getResources().getDrawable(R.drawable.pdf_lock_grey));
                holder.lockImage.setVisibility(View.VISIBLE);
//                holder.tv_name.setTextColor(ContextCompat.getColor(context, R.color.mock_dark_grey));
                holder.tv_name.setTextColor(ContextCompat.getColor(context, R.color.gray_8a));
                holder.option.setVisibility(View.GONE);
            } else {
                holder.iv_thumbnail.setColorFilter(ContextCompat.getColor(context, R.color.arivihan_secondary_color), PorterDuff.Mode.SRC_IN);
//                holder.iv_thumbnail.setImageDrawable(context.getResources().getDrawable(R.drawable.pdf_icon));
                holder.lockImage.setVisibility(View.GONE);
                holder.tv_name.setTextColor(ContextCompat.getColor(context, R.color.black));
                holder.option.setVisibility(View.VISIBLE);
            }
            holder.download_progress_layout.setVisibility(View.GONE);
//            holder.option.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.white));
        }

        holder.option.setOnClickListener(v -> noteClickListener.showDialog(item, position));
        holder.itemView.setOnClickListener(view -> {
            if (NoteEntity.STATUS_IN_QUEUE.equals(status) || NoteEntity.STATUS_PENDING.equals(status) ||
                    NoteEntity.STATUS_DOWNLOADING.equals(status) ||
                    NoteEntity.STATUS_FAILED.equals(status)) {
                noteClickListener.openBottomSheet(item, position);
            } else if (NoteEntity.STATUS_COMPLETED.equals(status)) {
                noteClickListener.onItemClick(item, position, item.isLocked());
            }
        });

        if (item.getStatus().equals(NoteEntity.STATUS_COMPLETED) && item.isSelected()) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.download_note_selected_bg));
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<NoteEntity> newList) {
        this.itemList = new ArrayList<>(newList);
        notifyDataSetChanged();
        Log.d(TAG, "updateList: Adapter updated with filtered list size: " + itemList.size());
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            itemList = new ArrayList<>(fullList);
        } else {
            List<NoteEntity> filteredList = new ArrayList<>();
            String trimmedQuery = CommonMethods.normalizeSpaces(query.trim().toLowerCase());
            Log.d(TAG, "Note Query: "+trimmedQuery);
            for (NoteEntity item : fullList) {
                try {
                    if (item != null) {
                        Pair<String, String> noteTypeInfo = OfflineDownloadManager.getNoteTypeInfo(offlineNoteConfigModel, item.getNoteType(), StringUtils.isNotEmpty(item.getLanguage()) ? item.getLanguage() : "en");
                        String chapterTitle = CommonMethods.normalizeSpaces(item.getChapterTitle().trim().toLowerCase());
                        String noteType = CommonMethods.normalizeSpaces((StringUtils.isNotEmpty(noteTypeInfo.first) ? noteTypeInfo.first : context.getString(R.string.Notes)).trim().toLowerCase());
//                    String subjectName = item.getSubjectName();
//                        Log.d(TAG, "ChapterTitle: "+chapterTitle+", NoteType: "+noteType);
                        if ((chapterTitle != null && !chapterTitle.isEmpty()
                                && chapterTitle.contains(trimmedQuery)) ||
                                (noteType != null && !noteType.isEmpty()
                                        && noteType.contains(trimmedQuery))) {
                            filteredList.add(item);
                        }
                    }
                } catch (Exception ignored) {}
            }
            itemList = filteredList;
        }

        if (!fullList.isEmpty() && itemList.isEmpty()) {
            noResultFoundLayout.setVisibility(View.VISIBLE);
            Log.d(TAG, "filter: No results found. Showing noResultFoundLayout.");
        } else {
            noResultFoundLayout.setVisibility(View.GONE);
            Log.d(TAG, "filter: Results found. Hiding noResultFoundLayout.");
        }

        notifyDataSetChanged();
        Log.d(TAG, "filter: Filtered list size: " + itemList.size());
    }


    public static class DownloadViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name, tv_subject, tv_type, tv_page_count, tv_size;
        ImageView option, iv_thumbnail;
        ConstraintLayout download_progress_layout;
        ProgressBar download_progress_bar;
        TextView download_progress_text;
        private ImageView lockImage;

        public DownloadViewHolder(View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_subject = itemView.findViewById(R.id.tv_subject);
            tv_type = itemView.findViewById(R.id.tv_type);
            tv_page_count = itemView.findViewById(R.id.tv_page_count);
            tv_size = itemView.findViewById(R.id.tv_size);
            option = itemView.findViewById(R.id.options);
            iv_thumbnail = itemView.findViewById(R.id.iv_thumbnail);
            lockImage = itemView.findViewById(R.id.img_lock);
            download_progress_layout = itemView.findViewById(R.id.download_progress_layout);
            download_progress_bar = itemView.findViewById(R.id.download_progress_bar);
            download_progress_text = itemView.findViewById(R.id.download_progress_text);

            Log.d(TAG, "ViewHolder: Views initialized.");
        }
    }

    public interface OnNoteClickListener {
        void showDialog(NoteEntity noteEntity, int pos);

        void onItemClick(NoteEntity noteEntity, int pos, boolean locked);

        void openBottomSheet(NoteEntity noteEntity, int pos);
    }
}
