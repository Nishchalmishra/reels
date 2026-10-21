package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.CollectionUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.DownloadStatus;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureDto;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureEntity;
import arivihan.technologies.doubtbuzzter2.model.PracticeStatus;
import arivihan.technologies.doubtbuzzter2.model.Questions;
import arivihan.technologies.doubtbuzzter2.model.offline.notes.NoteEntity;
import lombok.Setter;


public class MicrolectureAdapter extends RecyclerView.Adapter<MicrolectureAdapter.MicrolectureViewHolder> {

    Context context;
    List<MicroLectureDto> list;
    MicroLectureListener microLectureListener;
    PracticeAdapter.PracticeListener practiceListener;

    String microlectureId;
    @Setter
    boolean openVideo;
    boolean isLastMicrolecture;
    @Setter
    MicroLectureEntity selectionMicroLectureEntity;

    public MicrolectureAdapter(Context context, List<MicroLectureDto> list, MicroLectureListener microLectureListener,
                               PracticeAdapter.PracticeListener practiceListener, String microlectureId) {
        this.context = context;
        this.list = list;
        this.microLectureListener = microLectureListener;
        this.practiceListener = practiceListener;
        this.microlectureId = microlectureId;
    }

    public MicrolectureAdapter(Context context, List<MicroLectureDto> list, MicroLectureListener microLectureListener,
                               PracticeAdapter.PracticeListener practiceListener) {
        this.context = context;
        this.list = list;
        this.microLectureListener = microLectureListener;
        this.practiceListener = practiceListener;
    }

    @Setter
    boolean reachedPreview;
    boolean videoCompleted;
    boolean videoWatched;
    boolean nextVideoWatched;
    @Setter
    boolean allCompleted;

    @NonNull
    @Override
    public MicrolectureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.microlecture_detail_list_item, parent, false);
        return new MicrolectureViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MicrolectureViewHolder holder, int position) {
        MicroLectureDto microLectureDto = list.get(position);

        if (position == 0)
            holder.topDash.setVisibility(View.INVISIBLE);
        else {
            int prevLast = list.get(position - 1).getQuestions().size() - 1;
            if (prevLast >= 0) {
                Questions prevLastQuestion = list.get(position - 1).getQuestions().get(prevLast);
                if (microLectureDto.getVideoDetails().getProgress() > 0 && (prevLastQuestion.getStatus().equalsIgnoreCase(PracticeStatus.PARTIAL_COMPLETED.name())
                        || prevLastQuestion.getStatus().equalsIgnoreCase(PracticeStatus.COMPLETED.name()))) {
                    holder.topDash.setBackgroundColor(context.getResources().getColor(R.color.mock_primary_color));
                } else if (prevLastQuestion.getStatus().equalsIgnoreCase(PracticeStatus.COMPLETED.name())) {
                    holder.topDash.setBackgroundColor(context.getResources().getColor(R.color.mock_primary_color));
                }
            } else if (microLectureDto.getVideoDetails().getProgress() > 0 &&
                    list.get(position - 1).getVideoDetails().getProgress() > 0 &&
                    list.get(position - 1).getQuestions().isEmpty() ||
                    list.get(position - 1).isCompleted()) {
                holder.topDash.setBackgroundColor(context.getResources().getColor(R.color.mock_primary_color));
            }
        }

        if (list.size() > 1 && position < list.size() - 1 && list.get(position + 1).getVideoDetails().getProgress() > 0) {
            nextVideoWatched = true;
        } else {
            nextVideoWatched = false;
        }

        if (microLectureDto.getVideoDetails().getProgress() > 0) {
            videoWatched = true;
            holder.lectureProgress.setVisibility(View.VISIBLE);
            holder.lectureProgress.setProgress(microLectureDto.getVideoDetails().getProgress());
            holder.numberCard.setCardBackgroundColor(context.getResources().getColor(R.color.white));
            holder.numberText.setBackgroundColor(context.getResources().getColor(R.color.white));
            Typeface typeface = ResourcesCompat.getFont(context, R.font.urbanist_bold);
            holder.numberText.setTypeface(typeface);
            holder.numberText.setTextSize(9f);
            holder.numberText.setText(microLectureDto.getVideoDetails().getProgress() + "%");
            holder.numberText.setTextColor(context.getResources().getColor(R.color.black));
        } else {
            videoWatched = false;
            String value = String.valueOf((position + 1));
            if (position < 9)
                holder.numberText.setText("0" + value);
            else {
                holder.numberText.setText(value);
            }
        }

        holder.lectureTitle.setText(microLectureDto.getTitle());

        if (microLectureDto.isCompleted()) {
            holder.completed.setVisibility(View.VISIBLE);
            holder.lectureProgress.setVisibility(View.GONE);
            holder.bottomDash.setBackgroundColor(context.getResources().getColor(R.color.mock_primary_color));
            holder.connectingDot.setVisibility(View.VISIBLE);
        } else {
            holder.completed.setVisibility(View.GONE);
        }

        int last = microLectureDto.getQuestions().size() - 1;
        if (last >= 0) {
            Questions lastQuestion = list.get(position).getQuestions().get(last);
            if (lastQuestion.getStatus().equalsIgnoreCase(PracticeStatus.COMPLETED.name()) ||
                    (lastQuestion.getStatus().equalsIgnoreCase(PracticeStatus.PARTIAL_COMPLETED.name()) && nextVideoWatched)) {
                holder.connectingDot.setVisibility(View.VISIBLE);
            }
        } else if (microLectureDto.getVideoDetails().getProgress() == 100) {
            holder.connectingDot.setVisibility(View.VISIBLE);
        }

        if (microLectureDto.getVideoDetails().getProgress() == 100) {
            videoCompleted = true;
            holder.bottomDash.setBackgroundColor(context.getResources().getColor(R.color.mock_primary_color));
        } else {
            videoCompleted = false;
        }

        if (microLectureDto.getVideoDetails().getProgress() > 0 &&
                !microLectureDto.getQuestions().isEmpty() &&
                (microLectureDto.getQuestions().get(0).getStatus().equals(PracticeStatus.COMPLETED.name()) ||
                        microLectureDto.getQuestions().get(0).getStatus().equals(PracticeStatus.PARTIAL_COMPLETED.name()))) {
            holder.bottomDash.setBackgroundColor(context.getResources().getColor(R.color.mock_primary_color));
        }

        holder.videoLayout.setOnClickListener(v -> microLectureListener.startVideo(position));

        if (microLectureDto.isHighlightText()) {
            microLectureListener.setUpPreviewVideo(position);
            reachedPreview = true;
            holder.videoLayout.setBackgroundColor(context.getResources().getColor(R.color.video_layout_background_color));
            holder.lectureTitle.setTextColor(context.getResources().getColor(R.color.black));
            Typeface typeface = ResourcesCompat.getFont(context, R.font.urbanist_bold);
            holder.lectureTitle.setTypeface(typeface);
            holder.lectureTitle.setTextSize(14);
            holder.playVideo.setImageDrawable(context.getResources().getDrawable(R.drawable.play_lecture_dark));

            if (StringUtils.isEmpty(microLectureDto.getDownloadStatus()) ||
                    Objects.equals(microLectureDto.getDownloadStatus(), DownloadStatus.DOWNLOAD_FAILED.name())) {
                if (microLectureDto.isLocked()) {
                    holder.locked.setVisibility(View.VISIBLE);
                    holder.locked.setImageDrawable(context.getResources().getDrawable(R.drawable.lock));
                    holder.download.setVisibility(View.GONE);
                } else {
                    holder.locked.setVisibility(View.GONE);
                    holder.download.setImageDrawable(context.getResources().getDrawable(R.drawable.download_dark));
                    holder.download.setVisibility(View.VISIBLE);
                }
                holder.download_progress_layout.setVisibility(View.GONE);
                holder.download_video_complete.setVisibility(View.GONE);
            } else if (Objects.equals(microLectureDto.getDownloadStatus(), DownloadStatus.DOWNLOAD_RESUMED.name()) ||
                    Objects.equals(microLectureDto.getDownloadStatus(), DownloadStatus.DOWNLOAD_QUEUED.name()) ||
                    Objects.equals(microLectureDto.getDownloadStatus(), DownloadStatus.DOWNLOAD_PAUSED.name())) {
                holder.locked.setVisibility(View.GONE);
                holder.download.setVisibility(View.GONE);
                holder.download_progress_layout.setVisibility(View.VISIBLE);
                holder.download_video_complete.setVisibility(View.GONE);
                holder.download_progress_bar.setProgress(microLectureDto.getDownloadProgress());
                holder.download_progress_text.setText(microLectureDto.getDownloadProgress() + "%");
            } else if (Objects.equals(microLectureDto.getDownloadStatus(), DownloadStatus.DOWNLOAD_COMPLETED.name())) {
                holder.locked.setVisibility(View.GONE);
                holder.download.setVisibility(View.GONE);
                holder.download_progress_layout.setVisibility(View.GONE);
                holder.download_video_complete.setVisibility(View.VISIBLE);
            }
        } else {
            holder.videoLayout.setBackgroundColor(context.getResources().getColor(R.color.white));
            holder.lectureTitle.setTextSize(13);
            holder.playVideo.setImageDrawable(context.getResources().getDrawable(
                    microLectureDto.isMicroLectureSelected() ? R.drawable.play_lecture_dark : R.drawable.play_lecture_light));
            if (microLectureDto.isLocked()) {
                holder.locked.setVisibility(View.VISIBLE);
                holder.locked.setImageDrawable(context.getResources().getDrawable(
                        microLectureDto.isMicroLectureSelected() ? R.drawable.lock_dark : R.drawable.lock_light));
                holder.lectureTitle.setTextColor(context.getResources().getColor(R.color.title_color_light));
            } else {
                holder.locked.setVisibility(View.GONE);
                holder.lectureTitle.setTextColor(context.getResources().getColor(R.color.title_color));
            }
            if (StringUtils.isEmpty(microLectureDto.getDownloadStatus()) ||
                    Objects.equals(microLectureDto.getDownloadStatus(), DownloadStatus.DOWNLOAD_FAILED.name())) {
                if (microLectureDto.isLocked()) {
                    holder.download.setVisibility(View.GONE);
                } else {
                    holder.download.setImageDrawable(context.getResources().getDrawable(
                            microLectureDto.isMicroLectureSelected() ? R.drawable.download_dark : R.drawable.download_light));
                    holder.download.setVisibility(View.VISIBLE);
                }
                holder.download_progress_layout.setVisibility(View.GONE);
                holder.download_video_complete.setVisibility(View.GONE);
            } else if (Objects.equals(microLectureDto.getDownloadStatus(), DownloadStatus.DOWNLOAD_RESUMED.name()) ||
                    Objects.equals(microLectureDto.getDownloadStatus(), DownloadStatus.DOWNLOAD_QUEUED.name()) ||
                    Objects.equals(microLectureDto.getDownloadStatus(), DownloadStatus.DOWNLOAD_PAUSED.name())) {
                holder.download.setVisibility(View.GONE);
                holder.download_progress_layout.setVisibility(View.VISIBLE);
                holder.download_video_complete.setVisibility(View.GONE);
                holder.download_progress_bar.setProgress(microLectureDto.getDownloadProgress());
                holder.download_progress_text.setText(microLectureDto.getDownloadProgress() + "%");
            } else if (Objects.equals(microLectureDto.getDownloadStatus(), DownloadStatus.DOWNLOAD_COMPLETED.name())) {
                holder.download.setVisibility(View.GONE);
                holder.download_progress_layout.setVisibility(View.GONE);
                holder.download_video_complete.setVisibility(View.VISIBLE);
            }
        }

        if (position == list.size() - 1) {
            isLastMicrolecture = true;
            if (CollectionUtils.isEmpty(microLectureDto.getQuestions()))
                holder.bottomDash.setVisibility(View.INVISIBLE);
            else
                holder.bottomDash.setVisibility(View.VISIBLE);
        } else {
            isLastMicrolecture = false;
        }

        holder.practiceRecycler.setTag(position);
        holder.practiceRecycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

        PracticeAdapter practiceAdapter = new PracticeAdapter(context, microLectureDto.getQuestions(), practiceListener,
                videoWatched, nextVideoWatched, videoCompleted, isLastMicrolecture, microLectureDto.isMicroLectureSelected());
        holder.practiceRecycler.setAdapter(practiceAdapter);

        holder.download.setOnClickListener(view -> microLectureListener.initiateVideoDownload(position));
        holder.download_progress_layout.setOnClickListener(view -> microLectureListener.openBottomSheet(position));

        if (StringUtils.isNotEmpty(microLectureDto.getPdfUrl())) {
            holder.pdfNotes.setVisibility(View.VISIBLE);

            // Set theme-aware icons
            if (microLectureDto.isMicroLectureSelected()) {
                holder.notesBadgeButton.setImageDrawable(context.getResources().getDrawable(R.drawable.notes_dark));
                holder.iv_download.setImageDrawable(context.getResources().getDrawable(R.drawable.download_dark));
                holder.iv_status_lock.setImageDrawable(context.getResources().getDrawable(R.drawable.lock_dark));
            } else {
                holder.notesBadgeButton.setImageDrawable(context.getResources().getDrawable(R.drawable.notes_light));
                holder.iv_download.setImageDrawable(context.getResources().getDrawable(R.drawable.download_light));
                holder.iv_status_lock.setImageDrawable(context.getResources().getDrawable(R.drawable.lock_light));
            }

            holder.pdfNotes.setOnClickListener(view ->
                    microLectureListener.lecturesNotesOpen(microLectureDto, microLectureDto.getPdfUrl()));

            holder.iv_download.setOnClickListener(v ->
                    microLectureListener.onNoteDownload(microLectureDto, microLectureDto.getPdfUrl()));

            // ── Reset all note status views first to avoid stale state from recycled views ──
            holder.iv_status_lock.setVisibility(View.GONE);
            holder.iv_status_download_completed.setVisibility(View.GONE);
            holder.rl_download.setVisibility(View.VISIBLE);
            holder.rl_download_progress.setVisibility(View.GONE);

            // ── Use pdfLocked (independent of video locked) for notes lock state ──
            if (microLectureDto.isPdfLocked()) {
                holder.iv_status_lock.setVisibility(View.VISIBLE);
                holder.iv_status_download_completed.setVisibility(View.GONE);
                holder.rl_download.setVisibility(View.GONE);
                holder.rl_download_progress.setVisibility(View.GONE);
            } else if (microLectureDto.isPdfDownloadComplete()) {
                holder.iv_status_lock.setVisibility(View.GONE);
                holder.iv_status_download_completed.setVisibility(View.VISIBLE);
                holder.rl_download.setVisibility(View.GONE);
                holder.rl_download_progress.setVisibility(View.GONE);
            } else if (microLectureDto.getPdfProgress() > 0 ||
                    (StringUtils.isNotEmpty(microLectureDto.getPdfStatus()) &&
                            (microLectureDto.getPdfStatus().equals(NoteEntity.STATUS_IN_QUEUE)
                                    || microLectureDto.getPdfStatus().equals(NoteEntity.STATUS_PENDING)
                                    || microLectureDto.getPdfStatus().equals(NoteEntity.STATUS_DOWNLOADING)))) {
                holder.iv_status_lock.setVisibility(View.GONE);
                holder.iv_status_download_completed.setVisibility(View.GONE);
                holder.rl_download.setVisibility(View.GONE);
                holder.rl_download_progress.setVisibility(View.VISIBLE);
                holder.tv_progress.setText(microLectureDto.getPdfProgress() + "%");
                holder.pb_progress.setProgress(microLectureDto.getPdfProgress());
            }

            // ── Viewed state: color the dot and dashes ──
            if (microLectureDto.isPdfViewed()) {
                holder.notesDot.setBackgroundTintList(ColorStateList.valueOf(
                        context.getResources().getColor(R.color.mock_primary_color)));
                holder.notesBottomDash.setBackgroundColor(
                        context.getResources().getColor(R.color.mock_primary_color));
                holder.notesTopDash.setBackgroundColor(
                        context.getResources().getColor(R.color.mock_primary_color));
            }

        } else {
            holder.pdfNotes.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public List<MicroLectureDto> getList() {
        return list;
    }

    static class MicrolectureViewHolder extends RecyclerView.ViewHolder {
        LinearLayout topDash;
        LinearLayout bottomDash;
        LinearLayout notesTopDash, notesBottomDash;
        CardView notesDot;
        ImageView notesBadgeButton;
        ProgressBar lectureProgress;
        CardView numberCard;
        TextView numberText;
        TextView lectureTitle;
        LinearLayout divider;
        ImageView playVideo;
        ImageView completed;
        ImageView locked;
        LinearLayout pdfNotes;
        RecyclerView practiceRecycler;
        LinearLayout videoLayout;
        LinearLayout connectingDot;
        ImageView download;
        ConstraintLayout download_progress_layout;
        ProgressBar download_progress_bar;
        TextView download_progress_text;
        ImageView download_video_complete;
        RelativeLayout rl_download, rl_download_progress;
        ImageView iv_download, iv_status_download_completed, iv_status_lock;
        ProgressBar pb_progress;
        TextView tv_progress;

        public MicrolectureViewHolder(@NonNull View itemView) {
            super(itemView);
            notesDot = itemView.findViewById(R.id.notes_dot_card);
            notesBadgeButton = itemView.findViewById(R.id.notes_badge_btn);
            notesTopDash = itemView.findViewById(R.id.notes_top_dash);
            notesBottomDash = itemView.findViewById(R.id.notes_bottom_dash);
            pdfNotes = itemView.findViewById(R.id.notes_layout);
            topDash = itemView.findViewById(R.id.top_dash);
            bottomDash = itemView.findViewById(R.id.bottom_dash);
            lectureProgress = itemView.findViewById(R.id.lecture_progress);
            numberCard = itemView.findViewById(R.id.number_card);
            numberText = itemView.findViewById(R.id.lecture_number);
            lectureTitle = itemView.findViewById(R.id.lecture_title);
            divider = itemView.findViewById(R.id.divider);
            playVideo = itemView.findViewById(R.id.play_img);
            locked = itemView.findViewById(R.id.locked);
            completed = itemView.findViewById(R.id.check_img);
            practiceRecycler = itemView.findViewById(R.id.practice_rv);
            videoLayout = itemView.findViewById(R.id.video_layout);
            connectingDot = itemView.findViewById(R.id.connecting_dot);
            download = itemView.findViewById(R.id.download);
            download_progress_layout = itemView.findViewById(R.id.download_progress_layout);
            download_progress_bar = itemView.findViewById(R.id.download_progress_bar);
            download_progress_text = itemView.findViewById(R.id.download_progress_text);
            download_video_complete = itemView.findViewById(R.id.download_video_complete);

            rl_download = itemView.findViewById(R.id.rl_download);
            rl_download_progress = itemView.findViewById(R.id.rl_download_progress);
            iv_download = itemView.findViewById(R.id.iv_download);
            iv_status_download_completed = itemView.findViewById(R.id.iv_status_download_completed);
            pb_progress = itemView.findViewById(R.id.pb_progress);
            tv_progress = itemView.findViewById(R.id.tv_progress);
            iv_status_lock = itemView.findViewById(R.id.iv_status_lock);
        }
    }

    public interface MicroLectureListener {
        void setUpPreviewVideo(int position);

        void startVideo(int position);

        void initiateVideoDownload(int position);

        void openBottomSheet(int pos);

        void lecturesNotesOpen(MicroLectureDto microLectureDto, String notesUrl);

        void onNoteDownload(MicroLectureDto microLectureDto, String notesUrl);
    }
}
