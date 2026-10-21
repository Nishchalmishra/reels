package arivihan.technologies.doubtbuzzter2.adapter;

import android.annotation.SuppressLint;
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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.MicrolectureListActivity;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.enums.ChapterEnum.ChapterStatus;
import arivihan.technologies.doubtbuzzter2.model.HalfYearlyContentType;
import arivihan.technologies.doubtbuzzter2.model.HalfYearlyItem;
import arivihan.technologies.doubtbuzzter2.model.HalfYearlyTaskStatus;

public class HalfYearlyCompetitiveHorizontalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = HalfYearlyCompetitiveHorizontalAdapter.class.getSimpleName();
    ArrayList<HalfYearlyItem> itemList;
    Context context;
    CompetitiveHorizontalInterface competitiveHorizontalInterface;
    private static final int VIEW_TYPE_VIDEO = 0;
    private static final int VIEW_TYPE_PRACTICE = 2;
    private static final int VIEW_TYPE_ASAT = 3;
    private static final int VIEW_TYPE_TEST_AND_NOTES = 1;
    private static final int VIEW_TYPE_LIVE_CLASSES = 4;
    private static final int VIEW_TYPE_MICROLECTURE = 5;

    public HalfYearlyCompetitiveHorizontalAdapter(ArrayList<HalfYearlyItem> itemList, Context context,
                                                   CompetitiveHorizontalInterface competitiveHorizontalInterface) {
        this.itemList = itemList;
        this.context = context;
        this.competitiveHorizontalInterface = competitiveHorizontalInterface;

        Log.d(TAG, "--> Data list: "+itemList.size());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        if (viewType == VIEW_TYPE_VIDEO) {
            View view = layoutInflater.inflate(R.layout.half_yearly_class_item, viewGroup, false);
            return new MicrolectureVideoViewHolder(view);
        } else if (viewType == VIEW_TYPE_PRACTICE || viewType == VIEW_TYPE_ASAT) {
            View view = layoutInflater.inflate(R.layout.half_yearly_practice_question_item, viewGroup, false);
            return new PracticeQuestionViewHolder(view);
        } else if (viewType == VIEW_TYPE_LIVE_CLASSES) {
            View view = layoutInflater.inflate(R.layout.today_class_live_lecture_layout, viewGroup, false);
            return new TodayClassAdapter.LiveClassViewHolder(view);
        } else if (viewType == VIEW_TYPE_MICROLECTURE) {
            View view = layoutInflater.inflate(R.layout.today_class_microlecture_layout, viewGroup, false);
            return new TodayClassAdapter.MicrolectureVideoViewHolder(view);
        } else {
            View view = layoutInflater.inflate(R.layout.half_yearly_test_series_item, viewGroup, false);
            return new HalfYearlyHorizontalAdapter.TestAndNotesViewHolder(view);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        HalfYearlyItem item = itemList.get(position);

        if (viewHolder instanceof MicrolectureVideoViewHolder lectureViewHolder) {
            Glide.with(context).load(item.getThumbnailUrl()).into(lectureViewHolder.microLectureThumbnail);
            lectureViewHolder.microLectureTitle.setText(item.getMicroLectureTitle());
            lectureViewHolder.taskPosition.setText(item.getPosition() + "/" + itemList.size());
            String duration = item.getDuration();
            lectureViewHolder.microLectureDuration.setText(
                    (StringUtils.isNotEmpty(duration) ? duration : "0") + " " + context.getResources().getString(R.string.min));
            lectureViewHolder.completedStatus.setVisibility(item.isShowTaskCompleteIcon() ? View.VISIBLE : View.GONE);

            String viewCount = item.getMicroLectureViewCount();
            try {
                if (StringUtils.isNotEmpty(viewCount) && Integer.parseInt(viewCount) > 0) {
                    lectureViewHolder.viewCount.setVisibility(View.VISIBLE);
                    lectureViewHolder.viewCountText.setText(viewCount + " " + context.getResources().getString(R.string.students));
                } else {
                    lectureViewHolder.viewCount.setVisibility(View.GONE);
                }
            } catch (NumberFormatException e) {
                lectureViewHolder.viewCount.setVisibility(View.GONE);
            }

            Long progress = item.getVideoProgress();
            if (progress != null && progress > 0) {
                lectureViewHolder.progressIndicator.setVisibility(View.VISIBLE);
                lectureViewHolder.progressIndicator.setProgress(progress.intValue());
            } else {
                lectureViewHolder.progressIndicator.setVisibility(View.GONE);
            }

            lectureViewHolder.itemView.setOnClickListener(v -> competitiveHorizontalInterface.onItemClick(item));

        } else if (viewHolder instanceof HalfYearlyHorizontalAdapter.TestAndNotesViewHolder testAndNotesViewHolder) {
            testAndNotesViewHolder.subject.setText(item.getSubject());
            testAndNotesViewHolder.title.setText(item.getTitle());
            testAndNotesViewHolder.subtitle.setText(item.getSubTitle());
            testAndNotesViewHolder.taskPosition.setText(item.getPosition() + "/" + itemList.size());
            boolean testNotesCompleted = item.getRoutineTaskStatus() == HalfYearlyTaskStatus.COMPLETED;
            testAndNotesViewHolder.completedStatus.setVisibility(item.isShowTaskCompleteIcon() ? View.VISIBLE : View.GONE);
            if (testNotesCompleted) {
                testAndNotesViewHolder.arrowTextLayout.setVisibility(View.GONE);
                testAndNotesViewHolder.doneMarkText.setVisibility(View.VISIBLE);
                if (item.getContentType() == HalfYearlyContentType.TEST_SERIES) {
                    testAndNotesViewHolder.doneMarkText.setText(context.getString(R.string.Completed));
                } else {
                    testAndNotesViewHolder.doneMarkText.setText(context.getString(R.string.Watched));
                }
            } else {
                testAndNotesViewHolder.arrowTextLayout.setVisibility(View.VISIBLE);
                testAndNotesViewHolder.doneMarkText.setVisibility(View.GONE);
                if (item.getContentType() == HalfYearlyContentType.TEST_SERIES || item.getContentType() == HalfYearlyContentType.ASAT) {
                    testAndNotesViewHolder.arrowText.setText(context.getString(R.string.Start_Test_Now));
                } else {
                    testAndNotesViewHolder.arrowText.setText(context.getString(R.string.Read_Now));
                }
            }
            testAndNotesViewHolder.itemView.setOnClickListener(v -> competitiveHorizontalInterface.onItemClick(item));

        } else if (viewHolder instanceof PracticeQuestionViewHolder practiceViewHolder) {
            practiceViewHolder.microLectureTitle.setText(item.getMicroLectureTitle());
            practiceViewHolder.chapterName.setText(item.getChapterName());
            practiceViewHolder.taskPosition.setText(item.getPosition() + "/" + itemList.size());
            practiceViewHolder.no_of_question.setText(item.getNumberOfQuestions() + " " + context.getString(R.string.practice_problems));

            if (item.getRoutineTaskStatus() == HalfYearlyTaskStatus.COMPLETED) {
                practiceViewHolder.arrowTextLayout.setVisibility(View.GONE);
                practiceViewHolder.doneMarkText.setVisibility(View.VISIBLE);
            } else {
                practiceViewHolder.arrowTextLayout.setVisibility(View.VISIBLE);
                practiceViewHolder.doneMarkText.setVisibility(View.GONE);
            }

            Glide.with(context).load(item.getSubjectIconUrl()).into(practiceViewHolder.subjectIcon);
            practiceViewHolder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context.getApplicationContext(), MicrolectureListActivity.class);
                intent.putExtra("chapterId", item.getChapterId());
                intent.putExtra("chapterName", item.getChapterName());
                intent.putExtra("hideTapTarget", true);
                intent.putExtra("microLectureId", item.getMicroLectureId());
                intent.putExtra("openVideo", false);
                intent.putExtra("highlightQuestion", true);
                context.startActivity(intent);
            });

        } else if (viewHolder instanceof TodayClassAdapter.LiveClassViewHolder holder) {
            if (item == null) return;
            String positionText = item.getPosition() + "/" + (itemList != null ? itemList.size() : 0);
            holder.taskPosition.setText(positionText);
            holder.live_class_time.setText(StringUtils.isNotEmpty(item.getLiveClassDuration()) ? item.getLiveClassDuration() : "");
            holder.tv_live_date_and_time.setText(StringUtils.isNotEmpty(item.getLiveClassDate()) ? item.getLiveClassDate() : "");
            holder.live_animation.setVisibility(View.GONE);
            holder.live_img.setVisibility(View.GONE);
            holder.live_class_time.setVisibility(StringUtils.isNotEmpty(item.getLiveClassDuration()) ? View.VISIBLE : View.GONE);

            ChapterStatus status = ChapterStatus.fromString(item.getChapterStatus());
            holder.completedStatus.setVisibility(BooleanUtils.isTrue(item.getIsCompleted()) ? View.VISIBLE : View.GONE);
            holder.live_date_and_time.setVisibility(
                    (StringUtils.isNotEmpty(item.getLiveClassDate()) || StringUtils.isNotEmpty(item.getLiveClassDuration())) ? View.VISIBLE : View.GONE);
            Glide.with(context)
                    .load(StringUtils.isNotEmpty(item.getThumbnailUrl()) ? item.getThumbnailUrl() : item.getChapterThumbnailUrl())
                    .error(R.drawable.microlecture_default_thumbnail)
                    .into(holder.microLectureThumbnail);
            holder.view_divider.setVisibility(
                    (holder.tv_live_date_and_time.getVisibility() == View.VISIBLE && holder.live_class_time.getVisibility() == View.VISIBLE)
                            ? View.VISIBLE : View.GONE);
            holder.iv_play.setVisibility(holder.live_class_time.getVisibility());
            if (ChapterStatus.LIVE.equals(status) || ChapterStatus.LIVE_SCHEDULED.equals(status)) {
                holder.live_img.setVisibility(View.GONE);
                holder.live_animation.setVisibility(View.VISIBLE);
            }

            holder.itemView.setOnClickListener(v -> {
                if (item.isLocked()) {
                    if (competitiveHorizontalInterface != null) {
                        competitiveHorizontalInterface.showPopUp(TodayClassAdapter.TodayClassPopUpTypes.LOCK_POPUP, item);
                    }
                } else {
                    if (ChapterStatus.LIVE_SCHEDULED.equals(status)) {
                        if (competitiveHorizontalInterface != null) {
                            competitiveHorizontalInterface.showPopUp(TodayClassAdapter.TodayClassPopUpTypes.LIVE_UPCOMING, item);
                        }
                    } else if (ChapterStatus.LIVE.equals(status)) {
                        CommonNavigator.navigateToLiveClassWebView(context, item.getChapterName(),
                                StringUtils.isNotEmpty(item.getSubjectName()) ? item.getSubjectName() : item.getSubject(),
                                "", item.getChapterId(), item.getChapterCode(),
                                StringUtils.isNotEmpty(item.getThumbnailUrl()) ? item.getThumbnailUrl() : item.getChapterThumbnailUrl(),
                                item.getTeacherName(), TAG, CommonMethods.mapToBundle(item.getLiveNavigationBundle()));
                    }
                }
            });

        } else if (viewHolder instanceof TodayClassAdapter.MicrolectureVideoViewHolder lectureViewHolder) {
            Glide.with(context).load(item.getThumbnailUrl()).into(lectureViewHolder.microLectureThumbnail);
            lectureViewHolder.taskPosition.setText(item.getPosition() + "/" + itemList.size());

            Long mlProgress = item.getVideoProgress();
            lectureViewHolder.progressIndicator.setVisibility((mlProgress != null && mlProgress != 0) ? View.VISIBLE : View.GONE);
            if (mlProgress != null && mlProgress != 0) {
                lectureViewHolder.progressIndicator.setProgress(mlProgress.intValue());
            }
            lectureViewHolder.newBadge.setVisibility(item.isNewContent() ? View.VISIBLE : View.GONE);

            String duration = item.getDuration();
            lectureViewHolder.microLectureDuration.setText(
                    (StringUtils.isNotEmpty(duration) ? duration : "0") + " " + context.getString(R.string.min));
            lectureViewHolder.completedStatus.setVisibility(BooleanUtils.isTrue(item.getIsCompleted()) ? View.VISIBLE : View.GONE);

            try {
                if (StringUtils.isNotEmpty(item.getViewCountText())) {
                    lectureViewHolder.viewCount.setVisibility(View.VISIBLE);
                    lectureViewHolder.viewCountText.setText(item.getViewCountText());
                } else {
                    lectureViewHolder.viewCount.setVisibility(View.GONE);
                }
            } catch (NumberFormatException e) {
                lectureViewHolder.viewCount.setVisibility(View.GONE);
            }

            lectureViewHolder.itemView.setOnClickListener(v -> competitiveHorizontalInterface.onItemClick(item));
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        HalfYearlyContentType contentType = itemList.get(position).getContentType();
        if (contentType == HalfYearlyContentType.VIDEO) {
            return VIEW_TYPE_VIDEO;
        } else if (contentType == HalfYearlyContentType.PRACTICE) {
            return VIEW_TYPE_PRACTICE;
        } else if (contentType == HalfYearlyContentType.ASAT) {
            return VIEW_TYPE_ASAT;
        } else if (contentType == HalfYearlyContentType.LIVE_CLASSES) {
            return VIEW_TYPE_LIVE_CLASSES;
        } else if (contentType == HalfYearlyContentType.MICROLECTURE_VIDEO) {
            return VIEW_TYPE_MICROLECTURE;
        } else {
            return VIEW_TYPE_TEST_AND_NOTES;
        }
    }

    public static class MicrolectureVideoViewHolder extends RecyclerView.ViewHolder {
        ImageView microLectureThumbnail, completedStatus;
        TextView taskPosition, microLectureDuration, viewCountText;
        TextView microLectureTitle;
        LinearLayout viewCount;
        LinearProgressIndicator progressIndicator;

        public MicrolectureVideoViewHolder(@NonNull View itemView) {
            super(itemView);
            microLectureThumbnail = itemView.findViewById(R.id.micro_lecture_thumbnail);
            microLectureDuration = itemView.findViewById(R.id.microlecture_duration);
            taskPosition = itemView.findViewById(R.id.task_position);
            microLectureTitle = itemView.findViewById(R.id.micro_lecture_title);
            progressIndicator = itemView.findViewById(R.id.micro_lecture_progress);
            completedStatus = itemView.findViewById(R.id.completed_status);
            viewCount = itemView.findViewById(R.id.view_count);
            viewCountText = itemView.findViewById(R.id.view_count_text);
        }
    }

    public static class PracticeQuestionViewHolder extends RecyclerView.ViewHolder {
        TextView taskPosition, chapterName, microLectureTitle, no_of_question, doneMarkText, arrowText;
        ImageView subjectIcon, completedStatus;
        LinearLayout arrowTextLayout;

        public PracticeQuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            taskPosition = itemView.findViewById(R.id.task_position);
            chapterName = itemView.findViewById(R.id.chapter_name);
            microLectureTitle = itemView.findViewById(R.id.micro_lecture_title);
            no_of_question = itemView.findViewById(R.id.no_of_question);
            doneMarkText = itemView.findViewById(R.id.done_mark_text);
            arrowText = itemView.findViewById(R.id.arrow_text);
            subjectIcon = itemView.findViewById(R.id.subject_icon_img);
            completedStatus = itemView.findViewById(R.id.completed_status);
            arrowTextLayout = itemView.findViewById(R.id.arrow_text_layout);
        }
    }

    public interface CompetitiveHorizontalInterface {
        void onItemClick(HalfYearlyItem item);
        void showPopUp(TodayClassAdapter.TodayClassPopUpTypes type, HalfYearlyItem item);
    }
}
