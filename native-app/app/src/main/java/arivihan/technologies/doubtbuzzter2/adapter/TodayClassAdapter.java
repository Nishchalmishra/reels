package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.ExoPlayerActivity;
import arivihan.technologies.doubtbuzzter2.activity.MicrolectureListActivity;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.customView.gradient.AnimatedBorderView;
import arivihan.technologies.doubtbuzzter2.enums.ChapterEnum.ChapterStatus;
import arivihan.technologies.doubtbuzzter2.enums.DppStatus;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.TodayClassType;
import arivihan.technologies.doubtbuzzter2.enums.TodayTaskListType;
import arivihan.technologies.doubtbuzzter2.fragments.PreExamFragment;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.model.HalfYearlyTaskStatus;
import arivihan.technologies.doubtbuzzter2.model.LiveClassStatus;
import arivihan.technologies.doubtbuzzter2.model.NotesDto;
import arivihan.technologies.doubtbuzzter2.model.crash_course.TodayClassItemResponse;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import lombok.Getter;

public class TodayClassAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = TodayClassAdapter.class.getSimpleName();
//    public static final String BATCH_OVERVIEW_TAG = "batch_overview_video";

    public List<TodayClassItemResponse> todayClasses;
    Context context;
    TodayTaskListType todayTaskListType = TodayTaskListType.TODAY_TASK_FRAGMENT;
    TodayClassAdapterListener todayClassAdapterListener;
    @Getter
    public String animatedItemTag = null;

    public TodayClassAdapter(Context context, TodayTaskListType todayTaskListType,
                             List<TodayClassItemResponse> list,
                             TodayClassAdapterListener todayClassAdapterListener) {
        this.todayClasses = list;
        this.todayTaskListType = todayTaskListType;
        this.context = context;
        this.todayClassAdapterListener = todayClassAdapterListener;
    }

    // ─── onCreateViewHolder ───────────────────────────────────────────────────

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TodayClassType.VIDEO.type()) {
            return new MicrolectureVideoViewHolder(inflater.inflate(R.layout.today_class_microlecture_layout, parent, false));
        } else if (viewType == TodayClassType.LIVE_CLASSES.type()) {
            return new LiveClassViewHolder(inflater.inflate(R.layout.today_class_live_lecture_layout, parent, false));
        } else if (viewType == TodayClassType.PRACTICE_QUESTION.type()) {
            return new PracticeQuestionViewHolder(inflater.inflate(R.layout.today_class_practice_question_layout, parent, false));
        } else if (viewType == TodayClassType.ASAT.type()) {
            return new AsatStepsViewHolder(inflater.inflate(R.layout.today_class_asat_layout, parent, false));
        } else if (viewType == TodayClassType.NOTES.type() || viewType == TodayClassType.TEST.type()) {
            return new CommonViewHolder(inflater.inflate(R.layout.today_class_common_layout, parent, false));
        } else if (viewType == TodayClassType.ENGAGEMENT.type()) {
            return new LiveEnagementSessionViewHolder(inflater.inflate(R.layout.todays_class_live_engagement_layout, parent, false));
        } else if (viewType == TodayClassType.DYNAMIC2.type()) {
            return new CommonViewHolder2(inflater.inflate(R.layout.today_class_common_layout, parent, false));
        } else if (viewType == TodayClassType.DYNAMIC_VIDEO.type()) {
            return new CommonVideoViewHolder(inflater.inflate(R.layout.today_class_microlecture_layout, parent, false));
        } else if (viewType == TodayClassType.VIDEO_NAVIGATION.type()) {
            return new MicrolectureVideoNavigationViewHolder(inflater.inflate(R.layout.today_class_microlecture_navigation_layout, parent, false));
        } else if (viewType == TodayClassType.CONTINUE_LECTURE.type()) {
            return new ContinueLearningViewHolder(inflater.inflate(R.layout.today_class_continue_learning_layout, parent, false));
        } else if (viewType == TodayClassType.DPP_LIVE.type()) {
            return new DppLiveViewHolder(inflater.inflate(R.layout.today_class_live_lecture_layout, parent, false));
        } else if (viewType == TodayClassType.DPP_PRACTICE.type()) {
            return new DppPracticeViewHolder(inflater.inflate(R.layout.today_class_dpp_practice_layout, parent, false));
        } else {
            return new CommonViewHolder(inflater.inflate(R.layout.today_class_common_layout, parent, false));
        }
    }

    // ─── onBindViewHolder ─────────────────────────────────────────────────────

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        TodayClassItemResponse todayClass = todayClasses.get(i);
        if (viewHolder instanceof MicrolectureVideoViewHolder v) {
            Log.d("TodayClassAdapter", "Non-demo lecture | isCompleted from API: " + todayClass.getIsCompleted());
            bindLectureVideoHolder(v, todayClass, i);
        } else if (viewHolder instanceof MicrolectureVideoNavigationViewHolder v) {
            bindLectureVideoHolder(v, todayClass, i);
        } else if (viewHolder instanceof ContinueLearningViewHolder v) {
            bindContinueLearningHolder(v, todayClass, i);
        } else if (viewHolder instanceof DppLiveViewHolder v) {
            bindDppLiveClassHolder(v, todayClass, i);
        } else if (viewHolder instanceof DppPracticeViewHolder v) {
            bindDppPracticeHolder(v, todayClass, i);
        } else if (viewHolder instanceof LiveClassViewHolder v) {
            bindLiveClassHolder(v, todayClass);
        } else if (viewHolder instanceof PracticeQuestionViewHolder v) {
            bindPracticeHolder(v, todayClass);
        } else if (viewHolder instanceof AsatStepsViewHolder v) {
            bindAsatHolder(v, todayClass, i);
        } else if (viewHolder instanceof LiveEnagementSessionViewHolder v) {
            bindEngagementHolder(v, todayClass);
        } else if (viewHolder instanceof CommonViewHolder2 v) {
            bindCommonHolder2(v, todayClass);
        } else if (viewHolder instanceof CommonVideoViewHolder v) {
            bindCommonVideoHolder(v, todayClass);
        } else if (viewHolder instanceof CommonViewHolder v) {
            bindCommonHolder(v, todayClass);
        }
    }

    // ─── Bind methods ─────────────────────────────────────────────────────────

    /**
     * Shared bind for MicrolectureVideoViewHolder and MicrolectureVideoNavigationViewHolder
     */
    private void bindLectureVideoHolder(LectureVideoHolder holder, TodayClassItemResponse item, int index) {
        Glide.with(context).load(item.getThumbnailUrl()).into(holder.getThumbnail());
        holder.getTaskPosition().setText(item.getPosition() + "/" + todayClasses.size());
        if (item.getProgress() != null) {
            setProgress(holder.getProgressBar(), item.getProgress().longValue());
        }
        setNewBadge(holder.getNewBadge(), item.isNewContent());
        holder.getDurationText().setText(String.format(item.getDuration() + " " + context.getResources().getString(R.string.min)));
        holder.getCompletedStatus().setVisibility(BooleanUtils.isTrue(item.getIsCompleted()) ? View.VISIBLE : View.GONE);
        setViewCount(holder.getViewCountLayout(), holder.getViewCountTextView(), item.getMicroLectureViewCountStr(), item.getViewCountText());
        holder.getRootView().setOnClickListener(v -> openMicrolectureActivity(item, index));
    }

    private void bindContinueLearningHolder(ContinueLearningViewHolder holder, TodayClassItemResponse item, int index) {
        // Reset animation state on every bind so recycled views never carry stale borders.
        holder.borderAnimation.stopAnimation();
        holder.borderAnimation.setVisibility(View.GONE);

        Glide.with(context).load(item.getThumbnailUrl()).into(holder.microLectureThumbnail);
        holder.taskPosition.setText(item.getPosition() + "/" + todayClasses.size());
        String duration = (item.getVideoDuration() != null ? item.getVideoDuration() : "");
        if (item.getProgress() != null) {
            setProgress(holder.progressIndicator, item.getProgress().longValue());
        }
        if (item.getProgress() != null && item.getProgress() > 30 && BooleanUtils.isFalse(item.getIsCompleted())) {
//            holder.progressIndicator.setVisibility(View.VISIBLE);
            holder.rlContinueLearning.setVisibility(View.VISIBLE);
            holder.rlDemoLecture.setVisibility(View.GONE);
            holder.tvVideoDuration.setText(duration);

            holder.progressIndicator.post(() -> {
                int progressInt = holder.progressIndicator.getProgress();
                int max = holder.progressIndicator.getMax();

                if (max <= 0) return;

                float percent = progressInt / (float) max;
                int barWidth = holder.progressIndicator.getWidth();

                float x = (barWidth * percent)
                        - (holder.progress_dot.getWidth() / 2f);

                x = Math.max(0, Math.min(x, barWidth - holder.progress_dot.getWidth()));

                holder.progress_dot.setTranslationX(x);

                holder.progressIndicator.setVisibility(View.VISIBLE);
                holder.progress_dot.setVisibility(View.VISIBLE);
            });
        } else {
            holder.rlContinueLearning.setVisibility(View.GONE);
            holder.rlDemoLecture.setVisibility(View.VISIBLE);
            holder.tvDemoVideoDuration.setText(duration);

            holder.progressIndicator.setVisibility(View.GONE);
            holder.progress_dot.setVisibility(View.GONE);
        }
        setNewBadge(holder.newBadge, item.isNewContent());
        holder.completedStatus.setVisibility(BooleanUtils.isTrue(item.getIsCompleted()) ? View.VISIBLE : View.GONE);

        // Handle batch overview lecture: animation, watched tracking, and move-to-last
        if (item.isBatchOverViewLecture()) {
            String batchOverviewKey = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey())
                    + "_batch_overview_" + item.getMicroLectureId();
            boolean alreadyWatched = MyApplication.getBooleanDataFromDoNotDeleteSharedPreference(batchOverviewKey);
            boolean hasProgress = item.getProgress() != null && item.getProgress() > 0;

            if (alreadyWatched || hasProgress) {
                // Mark as watched and complete — hide animation
                MyApplication.saveSharedPreferencesDoNotDelete(batchOverviewKey, true);
                item.setIsCompleted(true);
                holder.completedStatus.setVisibility(View.VISIBLE);
                holder.borderAnimation.stopAnimation();
                holder.borderAnimation.setVisibility(View.GONE);
            } else {
                // Not yet watched — animate regardless of list position.
                // (The item may land at any index after checkForDemoLecture reordering
                //  or after the network response replaces the cached adapter.)
                String itemTag = item.getItemTag();
                if (animatedItemTag != null && animatedItemTag.equals(itemTag)) {
                    holder.borderAnimation.playAnimation(0);
                }
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (item.isBatchOverViewLecture()) {
                String batchOverviewKey = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey())
                        + "_batch_overview_" + item.getMicroLectureId();
                MyApplication.saveSharedPreferencesDoNotDelete(batchOverviewKey, true);
            }
            openMicrolectureActivity(item, index);
        });
    }

    public void setAnimatedItemTag(String tag) {
        this.animatedItemTag = tag;
        notifyDataSetChanged();
    }

    public void clearAnimatedItemTag() {
        if (animatedItemTag != null) {
            animatedItemTag = null;
            notifyDataSetChanged();
        }
    }

    private void bindLiveClassHolder(LiveClassViewHolder holder, TodayClassItemResponse todayClass) {
        if (todayClass == null) return;

        holder.taskPosition.setText(todayClass.getPosition() + "/" + (todayClasses != null ? todayClasses.size() : 0));
        holder.live_class_time.setText(StringUtils.isNotEmpty(todayClass.getLiveClassDuration()) ? todayClass.getLiveClassDuration() : "");
        holder.tv_live_date_and_time.setText(StringUtils.isNotEmpty(todayClass.getLiveClassDate()) ? todayClass.getLiveClassDate() : "");
        holder.live_animation.setVisibility(View.GONE);
        holder.live_img.setVisibility(View.GONE);
        holder.live_class_time.setVisibility(StringUtils.isNotEmpty(todayClass.getLiveClassDuration()) ? View.VISIBLE : View.GONE);

        ChapterStatus status = ChapterStatus.fromString(todayClass.getChapterStatus());
        holder.completedStatus.setVisibility(BooleanUtils.isTrue(todayClass.getIsCompleted()) ? View.VISIBLE : View.GONE);
        holder.live_date_and_time.setVisibility(
                (StringUtils.isNotEmpty(todayClass.getLiveClassDate()) || StringUtils.isNotEmpty(todayClass.getLiveClassDuration()))
                        ? View.VISIBLE : View.GONE);

        Glide.with(context)
                .load(StringUtils.isNotEmpty(todayClass.getThumbnailUrl()) ? todayClass.getThumbnailUrl() : todayClass.getChapterThumbnailUrl())
                .error(R.drawable.microlecture_default_thumbnail)
                .into(holder.microLectureThumbnail);
        holder.view_divider.setVisibility(
                (holder.tv_live_date_and_time.getVisibility() == View.VISIBLE && holder.live_class_time.getVisibility() == View.VISIBLE)
                        ? View.VISIBLE : View.GONE);
        holder.iv_play.setVisibility(holder.live_class_time.getVisibility());

        // ---- Rescheduled handling ----
        boolean isRescheduled = StringUtils.isNotEmpty(todayClass.getRescheduledDate())
                && StringUtils.isNotEmpty(todayClass.getRescheduledStartTime());

        if (isRescheduled) {
            // centered overlay with the NEW date/time
            holder.rescheduled_container.setVisibility(View.VISIBLE);
            holder.rescheduled_overlay.setVisibility(View.VISIBLE);
            holder.tv_rescheduled_date_and_time.setText(todayClass.getRescheduledDate());
            holder.tv_rescheduled_video_duration.setText(todayClass.getRescheduledStartTime());
            // strike through + grey out the ORIGINAL bottom row
            applyStrikeThrough(holder.tv_live_date_and_time, true);
            applyStrikeThrough(holder.live_class_time, true);
            int muted = ContextCompat.getColor(context, R.color.medium_text_color);
            holder.tv_live_date_and_time.setTextColor(muted);
            holder.live_class_time.setTextColor(muted);

            // a rescheduled class is not live
            holder.live_img.setVisibility(View.GONE);
            holder.live_animation.setVisibility(View.GONE);
        } else {
            // reset everything (RecyclerView reuse!)
            holder.rescheduled_overlay.setVisibility(View.GONE);
            holder.rescheduled_container.setVisibility(View.GONE);
            applyStrikeThrough(holder.tv_live_date_and_time, false);
            applyStrikeThrough(holder.live_class_time, false);
            int normal = ContextCompat.getColor(context, android.R.color.black);
            holder.tv_live_date_and_time.setTextColor(normal);
            holder.live_class_time.setTextColor(normal);
            holder.iv_play.setAlpha(1f);

            if (ChapterStatus.LIVE.equals(status) || ChapterStatus.LIVE_SCHEDULED.equals(status)) {
                holder.live_img.setVisibility(View.GONE);
                holder.live_animation.setVisibility(View.VISIBLE);
            }
        }
        // -------------------------------

        holder.itemView.setOnClickListener(v -> {
            if (todayClass.isLocked()) {
                if (todayClassAdapterListener != null) {
                    todayClassAdapterListener.showPopUp(TodayClassPopUpTypes.LOCK_POPUP, todayClass);
                }
            } else if (isRescheduled) {
                // route rescheduled taps to the upcoming/rescheduled popup
                if (todayClassAdapterListener != null) {
                    todayClassAdapterListener.showPopUp(TodayClassPopUpTypes.LIVE_UPCOMING, todayClass);
                }
            } else if (ChapterStatus.LIVE_SCHEDULED.equals(status)) {
                if (todayClassAdapterListener != null) {
                    todayClassAdapterListener.showPopUp(TodayClassPopUpTypes.LIVE_UPCOMING, todayClass);
                    HashMap<String, String> eventParams = new HashMap<>();
                    eventParams.put("chapter_code", todayClass.getChapterId());
                    eventParams.put("chapter_name", todayClass.getChapterName());
                    eventParams.put("chapter_status", status.toString());
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.today_class_upcoming_live_lecture_clicked.getActionName(), eventParams);
                }
            } else if (ChapterStatus.LIVE.equals(status)) {
                CommonNavigator.navigateToLiveClassWebView(context,
                        todayClass.getChapterName(), todayClass.getSubjectName(), "",
                        todayClass.getChapterId(), todayClass.getChapterCode(),
                        StringUtils.isNotEmpty(todayClass.getThumbnailUrl()) ? todayClass.getThumbnailUrl() : todayClass.getChapterThumbnailUrl(),
                        todayClass.getTeacherName(), TAG,
                        CommonMethods.mapToBundle(todayClass.getLiveNavigationBundle()));
            }
        });
    }

    private void bindDppLiveClassHolder(DppLiveViewHolder holder, TodayClassItemResponse todayClass, int index) {
        if (todayClass == null) return;

        holder.taskPosition.setText(todayClass.getPosition() + "/" + (todayClasses != null ? todayClasses.size() : 0));
        holder.live_class_time.setText(StringUtils.isNotEmpty(todayClass.getLiveClassDuration()) ? todayClass.getLiveClassDuration() : "");
        holder.tv_live_date_and_time.setText(StringUtils.isNotEmpty(todayClass.getLiveClassDate()) ? todayClass.getLiveClassDate() : "");
        holder.live_animation.setVisibility(View.GONE);
        holder.live_img.setVisibility(View.GONE);
        holder.live_class_time.setVisibility(StringUtils.isNotEmpty(todayClass.getLiveClassDuration()) ? View.VISIBLE : View.GONE);

        holder.completedStatus.setVisibility(BooleanUtils.isTrue(todayClass.getIsCompleted()) ? View.VISIBLE : View.GONE);
        holder.live_date_and_time.setVisibility(
                (StringUtils.isNotEmpty(todayClass.getLiveClassDate()) || StringUtils.isNotEmpty(todayClass.getLiveClassDuration()))
                        ? View.VISIBLE : View.GONE);

        Glide.with(context)
                .load(StringUtils.isNotEmpty(todayClass.getThumbnailUrl()) ? todayClass.getThumbnailUrl() : todayClass.getChapterThumbnailUrl())
                .error(R.drawable.microlecture_default_thumbnail)
                .into(holder.microLectureThumbnail);
        holder.view_divider.setVisibility(
                (holder.tv_live_date_and_time.getVisibility() == View.VISIBLE && holder.live_class_time.getVisibility() == View.VISIBLE)
                        ? View.VISIBLE : View.GONE);
        holder.iv_play.setVisibility(holder.live_class_time.getVisibility());

        // No rescheduling logic for DPP_LIVE — always reset overlay state
        holder.rescheduled_overlay.setVisibility(View.GONE);
        holder.rescheduled_container.setVisibility(View.GONE);

        boolean isDppUpcoming = DppStatus.UPCOMING.equals(DppStatus.fromString(todayClass.getDppStatus()));

        holder.live_img.setVisibility(View.GONE);
        holder.live_animation.setVisibility(isDppUpcoming ? View.GONE : View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if (todayClassAdapterListener != null) {
                todayClassAdapterListener.onTodayClassClick(todayClass, index);
            }
        });
    }

    private void bindDppPracticeHolder(DppPracticeViewHolder holder, TodayClassItemResponse todayClass, int index) {
        if (todayClass == null) return;

        holder.tv_task_position.setText(todayClass.getPosition() + "/" + (todayClasses != null ? todayClasses.size() : 0));
        holder.tv_title.setText(StringUtils.isNotEmpty(todayClass.getTitle()) ? todayClass.getTitle() : "");
        holder.tv_sub_title.setText(StringUtils.isNotEmpty(todayClass.getSubTitle()) ? todayClass.getSubTitle() : "");
        holder.tv_subject_name.setText(StringUtils.isNotEmpty(todayClass.getSubject()) ? todayClass.getSubject() : "");
        holder.tv_bottom_text.setText(StringUtils.isNotEmpty(todayClass.getBtnText()) ? todayClass.getBtnText() : "");

        String iconUrl = todayClass.getSubjectIconUrl();
        if (StringUtils.isNotEmpty(iconUrl) && iconUrl.startsWith("http")) {
            Glide.with(context)
                    .load(iconUrl)
                    .placeholder(R.drawable.ic_physics)
                    .error(R.drawable.ic_physics)
                    .into(holder.iv_subject_icon_img);
        } else {
            holder.iv_subject_icon_img.setImageResource(R.drawable.ic_physics);
        }

        holder.tv_subject_name.setVisibility(StringUtils.isNotEmpty(todayClass.getSubject()) ? View.VISIBLE : View.GONE);
//        holder.iv_subject_icon_img.setVisibility(StringUtils.isNotEmpty(todayClass.getSubjectIconUrl()) ? View.VISIBLE : View.GONE);
        holder.iv_subject_icon_img.setVisibility(View.GONE);

        // Circular progress: current/total
        int total = todayClass.getNumberOfQuestions() != null ? todayClass.getNumberOfQuestions() : 0;
        int progressPercent = todayClass.getProgress() != null ? todayClass.getProgress().intValue() : 0;
        int current = total > 0 ? progressPercent : 0;

        holder.dpp_progress.setMax(total > 0 ? total : 1);
        holder.dpp_progress.setProgress(current);
        holder.tv_dpp_progress_text.setText(current + "/" + total);

        holder.new_badge.setVisibility(todayClass.isNewContent() ? View.VISIBLE : View.GONE);
        holder.iv_completed_status.setVisibility(BooleanUtils.isTrue(todayClass.getIsCompleted()) ? View.VISIBLE : View.GONE);
        holder.iv_bottom.setVisibility(BooleanUtils.isTrue(todayClass.getIsCompleted()) ? View.GONE : View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if (todayClassAdapterListener != null) {
                todayClassAdapterListener.onTodayClassClick(todayClass, index);
            }
        });
    }

    private void applyStrikeThrough(TextView tv, boolean strike) {
        if (strike) {
            tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            tv.setPaintFlags(tv.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    private void bindPracticeHolder(PracticeQuestionViewHolder holder, TodayClassItemResponse item) {
        holder.microLectureTitle.setText(item.getMicroLectureTitle());
        holder.chapterName.setText(item.getChapterName());
        holder.taskPosition.setText(item.getPosition() + "/" + todayClasses.size());
        holder.noOfQuestions.setText(item.getNumberOfQuestions() + " " + "Homework Problems");

        int progress = item.getProgress() != null ? item.getProgress().intValue() : 0;
        holder.progressText.setText(context.getResources().getString(R.string.overall_progress_text) + " " + progress + "%");

        Glide.with(context).load(item.getSubjectIconUrl()).into(holder.subjectIcon);
        setNewBadge(holder.newBadge, item.isNewContent());

        if (item.getProgress() != null) {
            if (item.getProgress().intValue() == 0 || item.getProgress().intValue() == 100) {
                holder.resumePracticeProblem.setVisibility(View.GONE);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                holder.progressIndicator.setProgress(item.getProgress().intValue(), true);
            } else {
                holder.progressIndicator.setProgress(item.getProgress().intValue());
            }
        } else {
            holder.resumePracticeProblem.setVisibility(View.GONE);
            holder.progressIndicator.setProgress(0);
        }

        holder.completedStatus.setVisibility(BooleanUtils.isTrue(item.getIsCompleted()) ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context.getApplicationContext(), MicrolectureListActivity.class);
            intent.putExtra("chapterId", item.getChapterId());
            intent.putExtra("chapterName", item.getChapterName());
            intent.putExtra("hideTapTarget", true);
            intent.putExtra("microLectureId", item.getMicroLectureId());
            intent.putExtra("openVideo", false);
            intent.putExtra("highlightQuestion", true);
            Map<String, String> params = new HashMap<>();
            params.put("parametersDetail", String.valueOf(intent));
            params.put("cardType", "practice");
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.todays_task_card_clicked.getActionName(), params);
            context.startActivity(intent);
        });
    }

    private void bindAsatHolder(AsatStepsViewHolder holder, TodayClassItemResponse item, int index) {
        holder.tv_text1.setText(item.getHeading());
        holder.tv_text2.setText(item.getTitle());
        holder.tv_task_position.setText(String.format(item.getPosition() + "/" + todayClasses.size()));
        holder.tv_text3.setText(item.getSubTitle());

        Glide.with(context)
                .load(item.getIcon())
                .placeholder(R.drawable.img_file_edit)
                .error(R.drawable.img_file_edit)
                .into(holder.iv_subject_icon_img);

        setNewBadge(holder.new_badge, item.isNewContent());
        holder.iv_completed_status.setVisibility(BooleanUtils.isTrue(item.getIsCompleted()) ? View.VISIBLE : View.GONE);
        holder.tv_bottom_text.setText(item.getBtnText());

        holder.itemView.setOnClickListener(v -> {
            try {
                if (StringUtils.isNotEmpty(item.getClassName())) {
                    MyApplication.openActivity(context, item.getClassName(), item.getActivityParams());
                } else if (StringUtils.isNotEmpty(item.getDeeplink())) {
                    MyApplication.openDeepLink(context, item.getDeeplink());
                }
                Map<String, String> params = new HashMap<>();
                params.put("parametersDetail", "");
                params.put("className", item.getClassName());
                params.put("activityParams", item.getActivityParams().toString());
                params.put("deeplink", item.getDeeplink());
                params.put("cardType", "asat");
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.todays_task_card_clicked.getActionName(), params);
            } catch (Exception ignored) {
            }
        });
    }

    private void bindEngagementHolder(LiveEnagementSessionViewHolder holder, TodayClassItemResponse todayClass) {
        if (todayClass == null) return;

        holder.tvTaskPosition.setText(todayClass.getPosition() + "/" + (todayClasses != null ? todayClasses.size() : 0));
        holder.microLectureDuration.setText(StringUtils.defaultString(todayClass.getLiveClassDuration(), ""));
        holder.tv_live_date.setText(StringUtils.defaultString(todayClass.getLiveClassDate(), ""));

        holder.cvLive.setVisibility(View.GONE);
        holder.cv_live_date.setVisibility(View.GONE);
        holder.microLectureDuration.setVisibility(View.GONE);

        LiveClassStatus status = todayClass.getLiveClassStatus();

        if (LiveClassStatus.LIVE.equals(status)) {
            holder.cv_live_date.setVisibility(View.VISIBLE);
            holder.cvLive.setVisibility(View.VISIBLE);
            holder.microLectureDuration.setVisibility(View.VISIBLE);
            Glide.with(context).load(todayClass.getThumbnailUrl()).error(R.drawable.microlecture_default_thumbnail).into(holder.microLectureThumbnail);
            holder.itemView.setOnClickListener(v -> openLiveMeetingLink(todayClass.getLiveClassMeetingLink(), FirebaseEvents.clicked_todays_task_live_card));

        } else if (LiveClassStatus.YOUTUBE_LIVE.equals(status)) {
            holder.bottomLayout.setVisibility(View.GONE);
            Glide.with(context).load(todayClass.getThumbnailUrl()).error(R.drawable.microlecture_default_thumbnail).into(holder.microLectureThumbnail);
            holder.itemView.setOnClickListener(v -> openLiveMeetingLink(todayClass.getLiveClassMeetingLink(), FirebaseEvents.clicked_todays_task_live_card));

        } else if (LiveClassStatus.SCHEDULED.equals(status)) {
            holder.cv_live_date.setVisibility(View.VISIBLE);
            holder.cvLive.setVisibility(View.VISIBLE);
            holder.microLectureDuration.setVisibility(View.VISIBLE);
            Glide.with(context).load(todayClass.getThumbnailUrl()).error(R.drawable.microlecture_default_thumbnail).into(holder.microLectureThumbnail);
            holder.itemView.setOnClickListener(v -> {
                if (StringUtils.isNotEmpty(todayClass.getLiveClassSnackBarMessage())) {
                    todayClassAdapterListener.showSnackBar(todayClass.getLiveClassSnackBarMessage());
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.clicked_todays_task_scheduled_card.getActionName());
                }
            });

        } else if (LiveClassStatus.RECORDED.equals(status)) {
            Glide.with(context).load(todayClass.getThumbnailUrl()).error(R.drawable.microlecture_default_thumbnail).into(holder.microLectureThumbnail);
            holder.microLectureDuration.setVisibility(View.VISIBLE);
            String recordedUrl = todayClass.getRecordedLectureUrl();
            holder.itemView.setOnClickListener(v -> {
                Log.d(TAG, "onBindViewHolder: " + todayClass.getThumbnailUrl());
                if (StringUtils.isNotEmpty(recordedUrl)) {
                    Intent intent = new Intent(context, ExoPlayerActivity.class);
                    intent.putExtra("video_uri", recordedUrl);
                    todayClassAdapterListener.markVideoCompletedStatus();
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.clicked_todays_task_recorder_card.getActionName());
                    context.startActivity(intent);
                }
            });
        }
    }

    private void bindCommonHolder2(CommonViewHolder2 holder, TodayClassItemResponse item) {
        try {
            holder.tv_text1.setVisibility(StringUtils.isNotEmpty(item.getHeading()) ? View.VISIBLE : View.GONE);
            holder.tv_text2.setVisibility(StringUtils.isNotEmpty(item.getTitle()) ? View.VISIBLE : View.GONE);
            holder.tv_text3.setVisibility(StringUtils.isNotEmpty(item.getSubTitle()) ? View.VISIBLE : View.GONE);

            holder.tv_text1.setText(StringUtils.isNotEmpty(item.getHeading())
                    ? (CommonMethods.isValidJson(item.getHeading()) ? StyledTextBuilder.fromJson(context, item.getHeading()) : item.getHeading()) : "");
            holder.tv_text2.setText(StringUtils.isNotEmpty(item.getTitle())
                    ? (CommonMethods.isValidJson(item.getTitle()) ? StyledTextBuilder.fromJson(context, item.getTitle()) : item.getTitle()) : "");
            holder.tv_task_position.setText(String.format(item.getPosition() + "/" + todayClasses.size()));
            holder.tv_text3.setText(StringUtils.isNotEmpty(item.getSubTitle())
                    ? (CommonMethods.isValidJson(item.getSubTitle()) ? StyledTextBuilder.fromJson(context, item.getSubTitle()) : item.getSubTitle()) : "");

            Glide.with(context)
                    .load(item.getIcon())
                    .placeholder(R.drawable.lecture_default)
                    .error(R.drawable.lecture_default)
                    .into(holder.iv_subject_icon_img);

            holder.new_badge.setVisibility(item.isNewContent() ? View.VISIBLE : View.GONE);
            holder.iv_completed_status.setVisibility(BooleanUtils.isTrue(item.getIsCompleted()) ? View.VISIBLE : View.GONE);
            holder.iv_bottom.setVisibility(BooleanUtils.isTrue(item.getIsCompleted()) ? View.GONE : View.VISIBLE);
            holder.tv_bottom_text.setText(item.getBtnText());

            holder.itemView.setOnClickListener(v -> handleCommonNavigationClick(item));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void bindCommonVideoHolder(CommonVideoViewHolder holder, TodayClassItemResponse item) {
        Glide.with(context).load(item.getThumbnailUrl()).into(holder.microLectureThumbnail);
        holder.taskPosition.setText(String.format(item.getPosition() + "/" + todayClasses.size()));
        if (item.getProgress() != null) {
            setProgress(holder.progressIndicator, item.getProgress().longValue());
        }
        setNewBadge(holder.newBadge, item.isNewContent());
        holder.microLectureDuration.setText(StringUtils.isNotEmpty(item.getVideoDuration()) ? item.getVideoDuration() : "");
        holder.completedStatus.setVisibility(BooleanUtils.isTrue(item.getIsCompleted()) ? View.VISIBLE : View.GONE);

        if (StringUtils.isNotEmpty(item.getViewCountText())) {
            holder.viewCount.setVisibility(View.VISIBLE);
            holder.viewCountText.setText(item.getViewCountText());
        } else {
            holder.viewCount.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            try {
                handleActivityOrDeeplinkClick(item);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void bindCommonHolder(CommonViewHolder holder, TodayClassItemResponse item) {
        holder.tv_text1.setVisibility(StringUtils.isNotEmpty(item.getHeading()) ? View.VISIBLE : View.GONE);
        holder.tv_text2.setVisibility(StringUtils.isNotEmpty(item.getTitle()) ? View.VISIBLE : View.GONE);
        holder.tv_text3.setVisibility(StringUtils.isNotEmpty(item.getSubTitle()) ? View.VISIBLE : View.GONE);

        holder.tv_text1.setText(StringUtils.isNotEmpty(item.getHeading()) ? item.getHeading() : "");
        holder.tv_text2.setText(StringUtils.isNotEmpty(item.getTitle()) ? item.getTitle() : "");
        holder.tv_task_position.setText(String.format(item.getPosition() + "/" + todayClasses.size()));
        holder.tv_text3.setText(StringUtils.isNotEmpty(item.getSubTitle()) ? item.getSubTitle() : "");

        Glide.with(context)
                .load(item.getIcon())
                .placeholder(R.drawable.lecture_default)
                .error(R.drawable.lecture_default)
                .into(holder.iv_subject_icon_img);

        holder.new_badge.setVisibility(item.isNewContent() ? View.VISIBLE : View.GONE);
        holder.iv_completed_status.setVisibility(BooleanUtils.isTrue(item.getIsCompleted()) ? View.VISIBLE : View.GONE);
        holder.iv_bottom.setVisibility(BooleanUtils.isTrue(item.getIsCompleted()) ? View.GONE : View.VISIBLE);
        holder.tv_bottom_text.setText(item.getBtnText());

        holder.itemView.setOnClickListener(v -> handleCommonNavigationClick(item));
    }

    // ─── Helper methods ───────────────────────────────────────────────────────

    private void setProgress(LinearProgressIndicator indicator, Long progress) {
        if (progress != null && progress != 0) {
            indicator.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                indicator.setProgress(progress.intValue(), true);
            } else {
                indicator.setProgress(progress.intValue());
            }
        } else {
            indicator.setVisibility(View.GONE);
        }
    }

    private void setNewBadge(CardView badge, boolean isNew) {
        badge.setVisibility(isNew ? View.VISIBLE : View.GONE);
    }

    private void setViewCount(LinearLayout container, TextView textView, String countStr, String overrideText) {
        if (StringUtils.isNotEmpty(countStr)) {
            container.setVisibility(View.VISIBLE);
            textView.setText(String.format(countStr + " " + context.getResources().getString(R.string.students)));
            if (StringUtils.isNotEmpty(overrideText)) {
                textView.setText(overrideText);
            }
        } else {
            container.setVisibility(View.GONE);
        }
    }

    private void openMicrolectureActivity(TodayClassItemResponse item, int index) {
        if (item.getClassName() != null && todayClassAdapterListener != null) {
            todayClassAdapterListener.onTodayClassClick(item, index);
            return;
        }
        if (item.isDemoLecture()) {
            MyApplication.saveSharedPreferencesDoNotDelete(
                    MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()) + "_" + item.getMicroLectureId(), true);
        }
        Intent intent = new Intent(context.getApplicationContext(), MicrolectureListActivity.class);
        intent.putExtra("chapterId", item.getChapterId());
        intent.putExtra("chapterName", item.getChapterName());
        intent.putExtra("hideTapTarget", true);
        intent.putExtra("microLectureId", item.getMicroLectureId());
        intent.putExtra("openVideo", true);
//        intent.putExtra("isBatchOverview", item.isBatchOverViewLecture());

        Log.d(TAG, "---> Demo Clicked : " + item.isBatchOverViewLecture());
        if (item.isBatchOverViewLecture()) {
            Bundle bundle = new Bundle();
            bundle.putLong("skipToPosition", item.getSkipVideoPosition());
            bundle.putBoolean("isBatchOverview", item.isBatchOverViewLecture());

            intent.putExtras(bundle);
        }

        Map<String, String> params = new HashMap<>();
        params.put("parametersDetail", String.valueOf(intent));
        params.put("cardType", "lecture");
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.todays_task_card_clicked.getActionName(), params);
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.hs_todays_task_card_clicked.getActionName(), params);
        context.startActivity(intent);
    }

    /**
     * Shared click logic for CommonViewHolder and CommonViewHolder2
     */
    private void handleCommonNavigationClick(TodayClassItemResponse item) {
        try {
            if (StringUtils.isNotEmpty(item.getClassName())) {
                if ("external_pdf".equalsIgnoreCase(item.getClassName())) {
                    MyApplication.openPdfFromUrl(context, String.valueOf(item.getActivityParams().get("url")));
                } else {
                    MyApplication.openActivity(context, item.getClassName(), item.getActivityParams());
                }
                if (item.getClassName().equals(ExoPlayerActivity.CLASS_NAME)
                        && item.getActivityParams() != null
                        && item.getActivityParams().containsKey("identifier")
                        && todayClassAdapterListener != null) {
                    todayClassAdapterListener.videoClick((String) item.getActivityParams().get("identifier"));
                }
            } else if (StringUtils.isNotEmpty(item.getDeeplink())) {
                MyApplication.openDeepLink(context, item.getDeeplink());
            } else if (StringUtils.isNotEmpty(item.getNotesPdfUrl())) {
                if (todayTaskListType.equals(TodayTaskListType.PRE_EXAM_FRAGMENT)) {
                    PreExamFragment.updateStatus(context, item.getRoutineId(), HalfYearlyTaskStatus.COMPLETED.name());
                }
                NotesDto notesData = item.getNotesData();
                String notesUrl = (notesData != null && StringUtils.isNotEmpty(notesData.getNotesUrl()))
                        ? notesData.getNotesUrl() : item.getNotesPdfUrl();
                String notesType = (notesData != null && StringUtils.isNotEmpty(notesData.getNotesType()))
                        ? notesData.getNotesType() : "lecture.notes";
                String notesSubjectName = (notesData != null && StringUtils.isNotEmpty(notesData.getNotesSubjectName()))
                        ? notesData.getNotesSubjectName() : "";
                MyApplication.openInAppPdfFromUrl(context, notesUrl, item.getTitle(), notesType, notesSubjectName, "");
            }
            Map<String, String> params = new HashMap<>();
            params.put("parametersDetail", "");
            params.put("cardType", "dynamic");
            params.put("className", item.getClassName());
            params.put("activityParams", item.getActivityParams().toString());
            params.put("deeplink", item.getDeeplink());
            params.put("noteUrl", item.getNotesPdfUrl());
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.todays_task_card_clicked.getActionName(), params);
        } catch (Exception ignored) {
        }
    }

    /**
     * Shared click logic for CommonVideoViewHolder
     */
    private void handleActivityOrDeeplinkClick(TodayClassItemResponse item) {
        if (StringUtils.isNotEmpty(item.getClassName())) {
            if ("external_pdf".equalsIgnoreCase(item.getClassName())) {
                MyApplication.openPdfFromUrl(context, String.valueOf(item.getActivityParams().get("url")));
            } else {
                MyApplication.openActivity(context, item.getClassName(), item.getActivityParams());
            }
            if (item.getClassName().equals(ExoPlayerActivity.CLASS_NAME)
                    && item.getActivityParams() != null
                    && item.getActivityParams().containsKey("identifier")
                    && todayClassAdapterListener != null) {
                todayClassAdapterListener.videoClick((String) item.getActivityParams().get("identifier"));
            }
        } else if (StringUtils.isNotEmpty(item.getDeeplink())) {
            MyApplication.openDeepLink(context, item.getDeeplink());
        }
    }

    /**
     * Shared click logic for LIVE and YOUTUBE_LIVE engagement statuses
     */
    private void openLiveMeetingLink(String link, FirebaseEvents event) {
        if (StringUtils.isNotEmpty(link)) {
            MyApplication.openDeepLink(context, link);
            MyApplication.logFirebaseBundleCreation(event.getActionName());
        } else {
            Toast.makeText(context, context.getString(R.string.meeting_link_not_available), Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Pre-sort: moves any already-watched batch overview lecture to the end.
     * Call this BEFORE setting the adapter, not from inside onBindViewHolder.
     */
//    public static void moveBatchOverviewToEnd(List<TodayClassItemResponse> list) {
//        for (int i = list.size() - 1; i >= 0; i--) {
//            TodayClassItemResponse item = list.get(i);
//            if (!item.isBatchOverViewLecture()) continue;
//
//            String key = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey())
//                    + "_batch_overview_" + item.getMicroLectureId();
//            boolean alreadyWatched = MyApplication.getBooleanDataFromDoNotDeleteSharedPreference(key);
//            boolean hasProgress = item.getProgress() != null && item.getProgress() > 0;
//
//            if ((alreadyWatched || hasProgress) && i != list.size() - 1) {
//                list.remove(i);
//                list.add(item);
//            }
//        }
//    }

    // ─── getItemCount / getItemViewType ───────────────────────────────────────
    @Override
    public int getItemCount() {
        return todayClasses.size();
    }

    @Override
    public int getItemViewType(int position) {
        try {
            return TodayClassType.valueOf(todayClasses.get(position).getType()).type();
        } catch (IllegalArgumentException e) {
            return TodayClassType.DYNAMIC.type();
        }
    }

    // ─── LectureVideoHolder interface ─────────────────────────────────────────

    /**
     * Common interface for the three video lecture ViewHolders with identical fields
     */
    interface LectureVideoHolder {
        ImageView getThumbnail();

        TextView getTaskPosition();

        LinearProgressIndicator getProgressBar();

        CardView getNewBadge();

        TextView getDurationText();

        ImageView getCompletedStatus();

        LinearLayout getViewCountLayout();

        TextView getViewCountTextView();

        View getRootView();
    }

    // ─── ViewHolder classes ───────────────────────────────────────────────────

    public static class PracticeQuestionViewHolder extends RecyclerView.ViewHolder {
        ImageView subjectIcon, completedStatus;
        TextView taskPosition;
        TextView microLectureTitle, noOfQuestions, chapterName, progressText, resumePracticeProblem;
        ProgressBar progressIndicator;
        CardView newBadge;
        AnimatedBorderView borderAnimation;

        public PracticeQuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectIcon = itemView.findViewById(R.id.subject_icon_img);
            newBadge = itemView.findViewById(R.id.new_badge);
            microLectureTitle = itemView.findViewById(R.id.micro_lecture_title);
            taskPosition = itemView.findViewById(R.id.task_position);
            progressIndicator = itemView.findViewById(R.id.practice_progress);
            completedStatus = itemView.findViewById(R.id.completed_status);
            chapterName = itemView.findViewById(R.id.chapter_name);
            noOfQuestions = itemView.findViewById(R.id.no_of_questions);
            progressText = itemView.findViewById(R.id.progress_value);
            resumePracticeProblem = itemView.findViewById(R.id.resume_practice_problem);
            borderAnimation = itemView.findViewById(R.id.border_animation);
        }
    }

    public static class MicrolectureVideoViewHolder extends RecyclerView.ViewHolder implements LectureVideoHolder {
        ImageView microLectureThumbnail, completedStatus;
        TextView taskPosition, microLectureDuration, viewCountText;
        LinearLayout viewCount;
        LinearProgressIndicator progressIndicator;
        CardView newBadge;
        AnimatedBorderView borderAnimation;

        public MicrolectureVideoViewHolder(@NonNull View itemView) {
            super(itemView);
            microLectureThumbnail = itemView.findViewById(R.id.micro_lecture_thumbnail);
            newBadge = itemView.findViewById(R.id.new_badge);
            microLectureDuration = itemView.findViewById(R.id.tv_video_duration);
            taskPosition = itemView.findViewById(R.id.task_position);
            progressIndicator = itemView.findViewById(R.id.micro_lecture_progress);
            completedStatus = itemView.findViewById(R.id.completed_status);
            viewCount = itemView.findViewById(R.id.ll_view_count);
            viewCountText = itemView.findViewById(R.id.tv_view_count);
            borderAnimation = itemView.findViewById(R.id.border_animation);
        }

        @Override
        public ImageView getThumbnail() {
            return microLectureThumbnail;
        }

        @Override
        public TextView getTaskPosition() {
            return taskPosition;
        }

        @Override
        public LinearProgressIndicator getProgressBar() {
            return progressIndicator;
        }

        @Override
        public CardView getNewBadge() {
            return newBadge;
        }

        @Override
        public TextView getDurationText() {
            return microLectureDuration;
        }

        @Override
        public ImageView getCompletedStatus() {
            return completedStatus;
        }

        @Override
        public LinearLayout getViewCountLayout() {
            return viewCount;
        }

        @Override
        public TextView getViewCountTextView() {
            return viewCountText;
        }

        @Override
        public View getRootView() {
            return itemView;
        }
    }

    public static class MicrolectureVideoNavigationViewHolder extends RecyclerView.ViewHolder implements LectureVideoHolder {
        ImageView microLectureThumbnail, completedStatus;
        TextView taskPosition, microLectureDuration, viewCountText;
        LinearLayout viewCount;
        LinearProgressIndicator progressIndicator;
        CardView newBadge;
        AnimatedBorderView borderAnimation;

        public MicrolectureVideoNavigationViewHolder(@NonNull View itemView) {
            super(itemView);
            microLectureThumbnail = itemView.findViewById(R.id.micro_lecture_thumbnail);
            newBadge = itemView.findViewById(R.id.new_badge);
            microLectureDuration = itemView.findViewById(R.id.tv_video_duration);
            taskPosition = itemView.findViewById(R.id.task_position);
            progressIndicator = itemView.findViewById(R.id.micro_lecture_progress);
            completedStatus = itemView.findViewById(R.id.completed_status);
            viewCount = itemView.findViewById(R.id.ll_view_count);
            viewCountText = itemView.findViewById(R.id.tv_view_count);
            borderAnimation = itemView.findViewById(R.id.border_animation);
        }

        @Override
        public ImageView getThumbnail() {
            return microLectureThumbnail;
        }

        @Override
        public TextView getTaskPosition() {
            return taskPosition;
        }

        @Override
        public LinearProgressIndicator getProgressBar() {
            return progressIndicator;
        }

        @Override
        public CardView getNewBadge() {
            return newBadge;
        }

        @Override
        public TextView getDurationText() {
            return microLectureDuration;
        }

        @Override
        public ImageView getCompletedStatus() {
            return completedStatus;
        }

        @Override
        public LinearLayout getViewCountLayout() {
            return viewCount;
        }

        @Override
        public TextView getViewCountTextView() {
            return viewCountText;
        }

        @Override
        public View getRootView() {
            return itemView;
        }
    }

    public static class ContinueLearningViewHolder extends RecyclerView.ViewHolder {
        ImageView microLectureThumbnail, completedStatus, ivPlay, ivDemoPlay;
        TextView taskPosition, tvVideoDuration, tvDemoVideoDuration, tvDemoClass, tvContinueLearning;
        LinearProgressIndicator progressIndicator;
        CardView newBadge, mainCard;
        RelativeLayout rlContinueLearning, rlDemoLecture;
        AnimatedBorderView borderAnimation;
        View progress_dot;

        public ContinueLearningViewHolder(@NonNull View itemView) {
            super(itemView);
            microLectureThumbnail = itemView.findViewById(R.id.micro_lecture_thumbnail);
            newBadge = itemView.findViewById(R.id.new_badge);
            mainCard = itemView.findViewById(R.id.main_card);
            taskPosition = itemView.findViewById(R.id.task_position);
            progressIndicator = itemView.findViewById(R.id.micro_lecture_progress);
            completedStatus = itemView.findViewById(R.id.completed_status);
            borderAnimation = itemView.findViewById(R.id.border_animation);
            rlContinueLearning = itemView.findViewById(R.id.rl_continue_learning);
            tvContinueLearning = itemView.findViewById(R.id.tv_continue_learning);
            ivPlay = itemView.findViewById(R.id.iv_play);
            tvVideoDuration = itemView.findViewById(R.id.tv_video_duration);
            rlDemoLecture = itemView.findViewById(R.id.rl_demo_lecture);
            ivDemoPlay = itemView.findViewById(R.id.iv_demo_play);
            tvDemoVideoDuration = itemView.findViewById(R.id.tv_demo_video_duration);
            tvDemoClass = itemView.findViewById(R.id.tv_demo_class);
            progress_dot = itemView.findViewById(R.id.progress_dot);
        }
    }

    public static class LiveClassViewHolder extends RecyclerView.ViewHolder {
        ImageView microLectureThumbnail, completedStatus;
        View rescheduled_overlay;

        TextView taskPosition, live_class_time, tv_live_date_and_time;
        LinearLayout live_date_and_time;
        LottieAnimationView live_animation;
        CardView live_img;
        LinearProgressIndicator progressIndicator;
        CardView newBadge;
        ImageView iv_play;
        View view_divider;
        AnimatedBorderView borderAnimation;

        // rescheduled overlay
        LinearLayout rescheduled_container;
        TextView tv_rescheduled_date_and_time, tv_rescheduled_video_duration;

        public LiveClassViewHolder(@NonNull View itemView) {
            super(itemView);
            microLectureThumbnail = itemView.findViewById(R.id.micro_lecture_thumbnail);
            rescheduled_overlay = itemView.findViewById(R.id.rescheduled_overlay);
            newBadge = itemView.findViewById(R.id.new_badge);
            live_img = itemView.findViewById(R.id.img_live);
            live_animation = itemView.findViewById(R.id.live_animation);
            live_class_time = itemView.findViewById(R.id.tv_video_duration);
            taskPosition = itemView.findViewById(R.id.task_position);
            progressIndicator = itemView.findViewById(R.id.micro_lecture_progress);
            completedStatus = itemView.findViewById(R.id.completed_status);
            live_date_and_time = itemView.findViewById(R.id.live_date_and_time);
            tv_live_date_and_time = itemView.findViewById(R.id.tv_live_date_and_time);
            iv_play = itemView.findViewById(R.id.iv_play);
            view_divider = itemView.findViewById(R.id.view_divider);
            borderAnimation = itemView.findViewById(R.id.border_animation);

            rescheduled_container = itemView.findViewById(R.id.rescheduled_container);
            tv_rescheduled_date_and_time = itemView.findViewById(R.id.tv_rescheduled_date_and_time);
            tv_rescheduled_video_duration = itemView.findViewById(R.id.tv_rescheduled_video_duration);
        }
    }

    public static class AsatStepsViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_completed_status, iv_subject_icon_img, iv_bottom;
        TextView tv_text1, tv_text2, tv_text3, tv_bottom_text, tv_task_position;
        CardView new_badge, main_card;
        AnimatedBorderView borderAnimation;

        public AsatStepsViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_subject_icon_img = itemView.findViewById(R.id.iv_subject_icon_img);
            iv_completed_status = itemView.findViewById(R.id.iv_completed_status);
            new_badge = itemView.findViewById(R.id.new_badge);
            main_card = itemView.findViewById(R.id.main_card);
            tv_text1 = itemView.findViewById(R.id.tv_text1);
            tv_text2 = itemView.findViewById(R.id.tv_text2);
            tv_text3 = itemView.findViewById(R.id.tv_text3);
            tv_bottom_text = itemView.findViewById(R.id.tv_bottom_text);
            iv_bottom = itemView.findViewById(R.id.iv_bottom);
            tv_task_position = itemView.findViewById(R.id.tv_task_position);
            borderAnimation = itemView.findViewById(R.id.border_animation);
        }
    }

    public static class CommonViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_completed_status, iv_subject_icon_img, iv_bottom;
        TextView tv_text1, tv_text2, tv_text3, tv_bottom_text, tv_task_position;
        CardView new_badge, main_card;
        AnimatedBorderView borderAnimation;

        public CommonViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_subject_icon_img = itemView.findViewById(R.id.iv_subject_icon_img);
            iv_completed_status = itemView.findViewById(R.id.iv_completed_status);
            new_badge = itemView.findViewById(R.id.new_badge);
            main_card = itemView.findViewById(R.id.main_card);
            tv_text1 = itemView.findViewById(R.id.tv_text1);
            tv_text2 = itemView.findViewById(R.id.tv_text2);
            tv_text3 = itemView.findViewById(R.id.tv_text3);
            tv_bottom_text = itemView.findViewById(R.id.tv_bottom_text);
            iv_bottom = itemView.findViewById(R.id.iv_bottom);
            tv_task_position = itemView.findViewById(R.id.tv_task_position);
            borderAnimation = itemView.findViewById(R.id.border_animation);
        }
    }

    public static class CommonViewHolder2 extends RecyclerView.ViewHolder {
        ImageView iv_completed_status, iv_subject_icon_img, iv_bottom;
        TextView tv_text1, tv_text2, tv_text3, tv_bottom_text, tv_task_position;
        CardView new_badge, main_card;
        AnimatedBorderView borderAnimation;

        public CommonViewHolder2(@NonNull View itemView) {
            super(itemView);
            iv_subject_icon_img = itemView.findViewById(R.id.iv_subject_icon_img);
            iv_completed_status = itemView.findViewById(R.id.iv_completed_status);
            new_badge = itemView.findViewById(R.id.new_badge);
            main_card = itemView.findViewById(R.id.main_card);
            tv_text1 = itemView.findViewById(R.id.tv_text1);
            tv_text2 = itemView.findViewById(R.id.tv_text2);
            tv_text3 = itemView.findViewById(R.id.tv_text3);
            tv_bottom_text = itemView.findViewById(R.id.tv_bottom_text);
            iv_bottom = itemView.findViewById(R.id.iv_bottom);
            tv_task_position = itemView.findViewById(R.id.tv_task_position);
            borderAnimation = itemView.findViewById(R.id.border_animation);
        }
    }

    public static class LiveEnagementSessionViewHolder extends RecyclerView.ViewHolder {
        ImageView microLectureThumbnail, imgLive, watchTimeIcon;
        TextView microLectureDuration, tvTaskPosition, tvNewBadgeText, tv_live_date;
        CardView mainCard, cv_live_date, cvLive;
        RelativeLayout bottomLayout;
        LinearLayout watchTime;
        AnimatedBorderView borderAnimation;

        public LiveEnagementSessionViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_live_date = itemView.findViewById(R.id.tv_live_date);
            mainCard = itemView.findViewById(R.id.main_card);
            cv_live_date = itemView.findViewById(R.id.cv_live_date);
            cvLive = itemView.findViewById(R.id.cv_live);
            bottomLayout = itemView.findViewById(R.id.linearlayout);
            microLectureThumbnail = itemView.findViewById(R.id.micro_lecture_thumbnail);
            imgLive = itemView.findViewById(R.id.img_live);
            watchTimeIcon = itemView.findViewById(R.id.watch_time) != null ?
                    (ImageView) ((LinearLayout) itemView.findViewById(R.id.watch_time)).getChildAt(0) : null;
            microLectureDuration = itemView.findViewById(R.id.microlecture_duration);
            tvTaskPosition = itemView.findViewById(R.id.task_position);
            watchTime = itemView.findViewById(R.id.watch_time);
            borderAnimation = itemView.findViewById(R.id.border_animation);
        }
    }

    public static class CommonVideoViewHolder extends RecyclerView.ViewHolder {
        ImageView microLectureThumbnail, completedStatus;
        TextView taskPosition, microLectureDuration, viewCountText;
        LinearLayout viewCount;
        LinearProgressIndicator progressIndicator;
        CardView newBadge;
        AnimatedBorderView borderAnimation;

        public CommonVideoViewHolder(@NonNull View itemView) {
            super(itemView);
            microLectureThumbnail = itemView.findViewById(R.id.micro_lecture_thumbnail);
            newBadge = itemView.findViewById(R.id.new_badge);
            microLectureDuration = itemView.findViewById(R.id.tv_video_duration);
            taskPosition = itemView.findViewById(R.id.task_position);
            progressIndicator = itemView.findViewById(R.id.micro_lecture_progress);
            completedStatus = itemView.findViewById(R.id.completed_status);
            viewCount = itemView.findViewById(R.id.ll_view_count);
            viewCountText = itemView.findViewById(R.id.tv_view_count);
            borderAnimation = itemView.findViewById(R.id.border_animation);
        }
    }

    public static class DppLiveViewHolder extends LiveClassViewHolder {
        public DppLiveViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public static class DppPracticeViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_completed_status, iv_subject_icon_img, iv_bottom;
        TextView tv_title, tv_sub_title, tv_bottom_text, tv_task_position, tv_subject_name;
        TextView tv_dpp_progress_text;
        com.google.android.material.progressindicator.CircularProgressIndicator dpp_progress;
        CardView new_badge, main_card;
        AnimatedBorderView borderAnimation;

        public DppPracticeViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_completed_status = itemView.findViewById(R.id.iv_completed_status);
            iv_subject_icon_img = itemView.findViewById(R.id.iv_subject_icon_img);
            iv_bottom = itemView.findViewById(R.id.iv_bottom);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_sub_title = itemView.findViewById(R.id.tv_sub_title);
            tv_bottom_text = itemView.findViewById(R.id.tv_bottom_text);
            tv_task_position = itemView.findViewById(R.id.tv_task_position);
            tv_subject_name = itemView.findViewById(R.id.tv_subject_name);
            tv_dpp_progress_text = itemView.findViewById(R.id.tv_dpp_progress_text);
            dpp_progress = itemView.findViewById(R.id.dpp_progress);
            new_badge = itemView.findViewById(R.id.new_badge);
            main_card = itemView.findViewById(R.id.main_card);
            borderAnimation = itemView.findViewById(R.id.border_animation);
        }
    }

    // ─── Listener types ───────────────────────────────────────────────────────

    public enum TodayClassPopUpTypes {
        LIVE_UPCOMING,
        LOCK_POPUP
    }

    public interface TodayClassAdapterListener {
        void showSnackBar(String liveClassSnackBarMessage);

        void markVideoCompletedStatus();

        void videoClick(String identifier);

        void showPopUp(TodayClassPopUpTypes type, TodayClassItemResponse liveClassItem);

        void onTodayClassClick(TodayClassItemResponse liveClassItem, int position);
    }
}
