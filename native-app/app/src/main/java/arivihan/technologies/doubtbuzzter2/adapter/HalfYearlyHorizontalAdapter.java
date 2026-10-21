package arivihan.technologies.doubtbuzzter2.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.MicrolectureListActivity;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.enums.ChapterEnum.ChapterStatus;
import arivihan.technologies.doubtbuzzter2.enums.TodayClassType;
import arivihan.technologies.doubtbuzzter2.model.HalfYearlyContentType;
import arivihan.technologies.doubtbuzzter2.model.crash_course.TodayClassItemResponse;

public class HalfYearlyHorizontalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static String TAG = HalfYearlyHorizontalAdapter.class.getSimpleName();
    List<TodayClassItemResponse> itemList;
    Context context;
    HalfYearlyHorizontalInterface halfYearlyHorizontalInterface;
    private static final int VIEW_TYPE_VIDEO = 0;
    private static final int VIEW_TYPE_PRACTICE = 2;
    private static final int VIEW_TYPE_ASAT = 3;
    private static final int VIEW_TYPE_TEST_AND_NOTES = 1;
    private static final int VIEW_TYPE_LIVE_CLASSES = 4;
    private static final int VIEW_TYPE_MICROLECTURE = 5;
    private static final int VIEW_TYPE_CONTINUE_LECTURE = 5;
    public HalfYearlyHorizontalAdapter(List<TodayClassItemResponse> itemList, Context context, HalfYearlyHorizontalInterface halfYearlyHorizontalInterface) {
        this.itemList = itemList;
        this.context = context;
        this.halfYearlyHorizontalInterface = halfYearlyHorizontalInterface;
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
        } else if (viewType == VIEW_TYPE_CONTINUE_LECTURE) {
            return new TodayClassAdapter.ContinueLearningViewHolder(layoutInflater.inflate(R.layout.today_class_continue_learning_layout, viewGroup, false));
        } else {
            View view = layoutInflater.inflate(R.layout.half_yearly_test_series_item, viewGroup, false);
            return new TestAndNotesViewHolder(view);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        TodayClassItemResponse item = itemList.get(position);

        if (viewHolder instanceof MicrolectureVideoViewHolder lectureViewHolder) {
            Glide.with(context).load(item.getThumbnailUrl()).into(lectureViewHolder.microLectureThumbnail);
            lectureViewHolder.microLectureTitle.setText(item.getMicroLectureTitle());
            lectureViewHolder.taskPosition.setText(item.getPosition() + "/" + itemList.size());
            Integer duration = item.getDuration();
            lectureViewHolder.microLectureDuration.setText((duration != null ? duration : 0) + " " + context.getResources().getString(R.string.min));
            lectureViewHolder.completedStatus.setVisibility(item.getIsCompleted() ? View.VISIBLE : View.GONE);

            Integer viewCount = item.getMicroLectureViewCount();
            if (viewCount != null && viewCount > 0) {
                lectureViewHolder.viewCount.setVisibility(View.VISIBLE);
                lectureViewHolder.viewCountText.setText(viewCount + " " + context.getResources().getString(R.string.students));
            } else {
                lectureViewHolder.viewCount.setVisibility(View.GONE);
            }

            Double progress = item.getProgress();
            if (progress != null && progress > 0) {
                lectureViewHolder.progressIndicator.setVisibility(View.VISIBLE);
                lectureViewHolder.progressIndicator.setProgress(progress.intValue());
            }

            lectureViewHolder.itemView.setOnClickListener(v -> halfYearlyHorizontalInterface.onItemClick(item));

        }
        else if (viewHolder instanceof TestAndNotesViewHolder testAndNotesViewHolder) {
            testAndNotesViewHolder.subject.setText(item.getSubject());
            testAndNotesViewHolder.title.setText(item.getTitle());
            testAndNotesViewHolder.subtitle.setText(item.getSubTitle());
            testAndNotesViewHolder.taskPosition.setText(item.getPosition() + "/" + itemList.size());
            testAndNotesViewHolder.completedStatus.setVisibility(item.getIsCompleted() ? View.VISIBLE : View.GONE);
            if (item.getIsCompleted()) {
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
                if (item.getContentType() == HalfYearlyContentType.TEST_SERIES) {
                    testAndNotesViewHolder.arrowText.setText(context.getString(R.string.Start_Test_Now));
                } else {
                    testAndNotesViewHolder.arrowText.setText(context.getString(R.string.Read_Now));
                }
            }
            testAndNotesViewHolder.itemView.setOnClickListener(v -> halfYearlyHorizontalInterface.onItemClick(item));

        }
        else if (viewHolder instanceof PracticeQuestionViewHolder practiceViewHolder) {
            practiceViewHolder.microLectureTitle.setText(item.getMicroLectureTitle());
            practiceViewHolder.chapterName.setText(item.getChapterName());
            practiceViewHolder.taskPosition.setText(item.getPosition() + "/" + itemList.size());
            Integer numQ = item.getNumberOfQuestions();
            practiceViewHolder.no_of_question.setText((numQ != null ? numQ : 0) + " " + context.getString(R.string.practice_problems));

            if (item.getIsCompleted()) {
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
        }
        else if (viewHolder instanceof TodayClassAdapter.LiveClassViewHolder holder) {
            if (item == null) return;
            String positionText = item.getPosition()
                    + "/" + (itemList != null ? itemList.size() : 0);
            holder.taskPosition.setText(positionText);
            holder.live_class_time.setText(StringUtils.isNotEmpty(item.getLiveClassDuration()) ? item.getLiveClassDuration() : "");
            holder.tv_live_date_and_time.setText(StringUtils.isNotEmpty(item.getLiveClassDate()) ? item.getLiveClassDate() : "");
            holder.live_animation.setVisibility(View.GONE);
            holder.live_img.setVisibility(View.GONE);
            holder.live_class_time.setVisibility(StringUtils.isNotEmpty(item.getLiveClassDuration()) ? View.VISIBLE : View.GONE);

            ChapterStatus status = ChapterStatus.fromString(item.getChapterStatus());
            holder.completedStatus.setVisibility(item.getIsCompleted() ? View.VISIBLE : View.GONE);
            holder.live_date_and_time.setVisibility((StringUtils.isNotEmpty(item.getLiveClassDate()) || StringUtils.isNotEmpty(item.getLiveClassDuration())) ? View.VISIBLE : View.GONE);
            Glide.with(context)
                    .load(StringUtils.isNotEmpty(item.getThumbnailUrl()) ? item.getThumbnailUrl() : item.getChapterThumbnailUrl())
                    .error(R.drawable.microlecture_default_thumbnail)
                    .into(holder.microLectureThumbnail);
            holder.view_divider.setVisibility((holder.tv_live_date_and_time.getVisibility() == View.VISIBLE && holder.live_class_time.getVisibility() == View.VISIBLE) ? View.VISIBLE : View.GONE);
            holder.iv_play.setVisibility(holder.live_class_time.getVisibility());
//            if (ChapterStatus.LIVE_SCHEDULED.equals(status)) {
//                holder.live_img.setVisibility(View.VISIBLE);
//                holder.live_animation.setVisibility(View.GONE);
//            } else
            if (ChapterStatus.LIVE.equals(status) || ChapterStatus.LIVE_SCHEDULED.equals(status)) {
                holder.live_img.setVisibility(View.GONE);
                holder.live_animation.setVisibility(View.VISIBLE);
            }

            holder.itemView.setOnClickListener(v -> {
                if (item.isLocked()) {
                    if (halfYearlyHorizontalInterface != null) {
                        halfYearlyHorizontalInterface.showPopUp(TodayClassAdapter.TodayClassPopUpTypes.LOCK_POPUP, item);
                    }
                } else {
                    if (ChapterStatus.LIVE_SCHEDULED.equals(status)) {
                        if (halfYearlyHorizontalInterface != null) {
                            halfYearlyHorizontalInterface.showPopUp(TodayClassAdapter.TodayClassPopUpTypes.LIVE_UPCOMING, item);
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
        }
        else if (viewHolder instanceof TodayClassAdapter.MicrolectureVideoViewHolder lectureViewHolder) {
            Glide.with(context).load(item.getThumbnailUrl()).into(lectureViewHolder.microLectureThumbnail);
            lectureViewHolder.taskPosition.setText(item.getPosition() + "/" + itemList.size());

            Double progress = item.getProgress();
            lectureViewHolder.progressIndicator.setVisibility((progress != null && progress != 0) ? View.VISIBLE : View.GONE);

            lectureViewHolder.newBadge.setVisibility(item.isNewContent() ? View.VISIBLE : View.GONE);

            Integer duration = item.getDuration();
            lectureViewHolder.microLectureDuration.setText((duration != null ? duration : 0) + " " + context.getString(R.string.min));

            lectureViewHolder.completedStatus.setVisibility(item.getIsCompleted() ? View.VISIBLE : View.GONE);

            if (StringUtils.isNotEmpty(item.getMicroLectureViewCountStr())) {
                lectureViewHolder.viewCount.setVisibility(View.VISIBLE);
                String viewText = item.getMicroLectureViewCountStr() + " " + context.getString(R.string.students);
                if (StringUtils.isNotEmpty(item.getViewCountText())) {
                    viewText = item.getViewCountText();
                }
                lectureViewHolder.viewCountText.setText(viewText);
            } else {
                lectureViewHolder.viewCount.setVisibility(View.GONE);
            }

            lectureViewHolder.itemView.setOnClickListener(v -> halfYearlyHorizontalInterface.onItemClick(item));
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
        } else if(contentType == HalfYearlyContentType.CONTINUE_LECTURE) {
            return VIEW_TYPE_CONTINUE_LECTURE;
        } else {
            return VIEW_TYPE_TEST_AND_NOTES;
        }
    }

    public static class TestAndNotesViewHolder extends RecyclerView.ViewHolder {
        TextView taskPosition;
        ImageView completedStatus;
        ImageView subjectIcon;
        TextView subject, title, subtitle, arrowText, doneMarkText;
        LinearLayout arrowTextLayout;

        public TestAndNotesViewHolder(@NonNull View itemView) {
            super(itemView);
            taskPosition = itemView.findViewById(R.id.task_position);
            completedStatus = itemView.findViewById(R.id.completed_status);
            subjectIcon = itemView.findViewById(R.id.subject_icon_img);
            subject = itemView.findViewById(R.id.subject);
            subtitle = itemView.findViewById(R.id.subtitle);
            title = itemView.findViewById(R.id.title);
            arrowText = itemView.findViewById(R.id.arrow_text);
            doneMarkText = itemView.findViewById(R.id.done_mark_text);
            arrowTextLayout = itemView.findViewById(R.id.arrow_text_layout);
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
        ImageView subjectIcon, completedStatus, arrowIcon;
        LinearLayout detailLayout, arrowTextLayout;

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
            detailLayout = itemView.findViewById(R.id.detail_layout);
            arrowTextLayout = itemView.findViewById(R.id.arrow_text_layout);
        }
    }

    public interface HalfYearlyHorizontalInterface {
        void onItemClick(TodayClassItemResponse item);
        void showPopUp(TodayClassAdapter.TodayClassPopUpTypes type, TodayClassItemResponse item);
    }
}
