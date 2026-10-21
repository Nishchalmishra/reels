package arivihan.technologies.doubtbuzzter2.adapter.offline;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.DownloadStatus;
import arivihan.technologies.doubtbuzzter2.model.MediaEntity;

public class MyDownloadVideoAdapter extends RecyclerView.Adapter<MyDownloadVideoAdapter.DownloadViewHolder> {

    private Context context;
    private List<MediaEntity> itemList;
    private final List<MediaEntity> fullList;
    private final DialogCallback dialogCallback;
    private final LinearLayout noResultFoundLayout;

    private static final String TAG = MyDownloadVideoAdapter.class.getSimpleName();


    public MyDownloadVideoAdapter(Context context, List<MediaEntity> itemList, DialogCallback dialogCallback, LinearLayout noResultFoundLayout) {
        this.context = context;
        this.itemList = itemList;
        this.fullList = itemList;
        this.dialogCallback = dialogCallback;
        this.noResultFoundLayout = noResultFoundLayout;
        Log.d(TAG, "Adapter initialized with item list size: " + itemList.size());
    }

    @NonNull
    @Override
    public DownloadViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: Inflating view for item at position " + viewType);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_download_lecture_list, parent, false);
        return new DownloadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DownloadViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MediaEntity item = itemList.get(position);
        Log.d(TAG, "onBindViewHolder: Binding data for item at position" + position);

        holder.chapterName.setText(item.getChapterTitle());
        holder.microLectureName.setText(item.getMicroLectureTitle());
        holder.videoLength.setText(
                getFormattedVideoLength(context, Integer.parseInt(item.getDuration()))
        );
        holder.videoSize.setText(String.format(context.getString(R.string.video_size), item.getSize()));
        holder.option.setOnClickListener(v -> dialogCallback.showDialog(item,position));
        String status = item.getStatus();
        if(item.isThumbnailLoaded()){
                Glide.with(context)
                        .load(item.getThumbnailUrl())
                        .into(holder.thumbnailImage);
            }else{
                Glide.with(context)
                        .load(R.drawable.microlecture_default_thumbnail)
                        .into(holder.thumbnailImage);
        }
        if (DownloadStatus.DOWNLOAD_RESUMED.name().equals(status) ||
                DownloadStatus.DOWNLOAD_PAUSED.name().equals(status) ||
                DownloadStatus.DOWNLOAD_QUEUED.name().equals(status) ) {
            holder.download_progress_layout.setVisibility(View.VISIBLE);
            holder.download_progress_bar.setProgress(item.getProgress());
            holder.download_progress_text.setText(item.getProgress() + "%");
            holder.option.setVisibility(View.GONE);
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.grey_300));
        }else if(DownloadStatus.DOWNLOAD_FAILED.name().equals(status)) {
            holder.download_progress_layout.setVisibility(View.GONE);
            holder.option.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.grey_300));
        }else{
            if(item.isLocked()){
                holder.lockImage.setVisibility(View.VISIBLE);
                holder.chapterName.setTextColor(context.getResources().
                        getColor(R.color.mock_dark_grey));
            }else {
                holder.lockImage.setVisibility(View.GONE);
                holder.chapterName.setTextColor(context.getResources().
                        getColor(R.color.black));
            }
            holder.download_progress_layout.setVisibility(View.GONE);
            holder.option.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.white));
        }
        Log.d(TAG, "Chapter Name: " + item.getChapterTitle());
        Log.d(TAG, "Micro Lecture Name: " + item.getMicroLectureTitle());
        Log.d(TAG, "Video Length: " + item.getDuration());
        Log.d(TAG, "Video Size: " + item.getSize());

        holder.option.setOnClickListener(v -> dialogCallback.showDialog(item, position));
        holder.itemView.setOnClickListener(view -> {
            if (DownloadStatus.DOWNLOAD_RESUMED.name().equals(status) ||
                    DownloadStatus.DOWNLOAD_PAUSED.name().equals(status) ||
                    DownloadStatus.DOWNLOAD_QUEUED.name().equals(status) ){
                dialogCallback.openBottomSheet(item,position);
            } else if(DownloadStatus.DOWNLOAD_COMPLETED.name().equals(status))
                dialogCallback.onItemClick(item, position,item.isLocked());
        });
    }

    private String getFormattedVideoLength(Context context, int totalMinutes) {
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;

        if (hours > 0) {
            if (minutes > 0) {
                return context.getString(
                        R.string.video_length_hours_minutes,
                        hours,
                        minutes
                );
            } else {
                return context.getString(
                        R.string.video_length_hours,
                        hours
                );
            }
        } else {
            return context.getString(
                    R.string.video_length_minutes,
                    minutes
            );
        }
    }


    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<MediaEntity> newList) {
        this.itemList = new ArrayList<>(newList);
        notifyDataSetChanged();
        Log.d(TAG, "updateList: Adapter updated with filtered list size: " + itemList.size());
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filter(String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                itemList = new ArrayList<>(fullList);
            } else {
                List<MediaEntity> filteredList = new ArrayList<>();
                String trimmedQuery = query.trim().toLowerCase();

                for (MediaEntity item : fullList) {
                    if (item != null && (StringUtils.isNotEmpty(item.getChapterTitle()) && item.getChapterTitle().toLowerCase().contains(trimmedQuery) ||
                            (StringUtils.isNotEmpty(item.getMicroLectureTitle()) && item.getMicroLectureTitle().toLowerCase().contains(trimmedQuery)))) {
                        filteredList.add(item);
                    }
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public static class DownloadViewHolder extends RecyclerView.ViewHolder {
        TextView chapterName, microLectureName, videoLength, videoSize;
        ImageView option, thumbnailImage;
        ConstraintLayout download_progress_layout;
        ProgressBar download_progress_bar;
        TextView download_progress_text;
        private LinearLayout lockImage;

        public DownloadViewHolder(View itemView) {
            super(itemView);
            chapterName = itemView.findViewById(R.id.chapter_name);
            microLectureName = itemView.findViewById(R.id.mico_lecture_name);
            videoLength = itemView.findViewById(R.id.duration_text);
            videoSize = itemView.findViewById(R.id.size_text);
            option = itemView.findViewById(R.id.options);
            thumbnailImage = itemView.findViewById(R.id.thumbnail_image);
            lockImage =itemView.findViewById(R.id.thumbnail_locked_image);
            download_progress_layout=itemView.findViewById(R.id.download_progress_layout);
            download_progress_bar=itemView.findViewById(R.id.download_progress_bar);
            download_progress_text=itemView.findViewById(R.id.download_progress_text);

            Log.d(TAG, "ViewHolder: Views initialized.");
        }
    }

    public interface DialogCallback {
        void showDialog(MediaEntity mediaEntity, int pos);
        void onItemClick(MediaEntity mediaEntity, int pos, boolean locked);
        void openBottomSheet(MediaEntity mediaEntity, int pos);
    }
}
