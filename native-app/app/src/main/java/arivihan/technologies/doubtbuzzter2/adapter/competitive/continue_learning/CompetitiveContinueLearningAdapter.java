package arivihan.technologies.doubtbuzzter2.adapter.competitive.continue_learning;

import android.content.Context;
import android.os.Build;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.enums.competitive.AdaptivePath;
import arivihan.technologies.doubtbuzzter2.enums.competitive.CompetitiveContinueLearningType;
import arivihan.technologies.doubtbuzzter2.model.competitive.continue_learning.ContinueLearningDto;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;

public class CompetitiveContinueLearningAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = CompetitiveContinueLearningAdapter.class.getSimpleName();
    List<ContinueLearningDto> continueLearningDtoList;
    Context context;
    CompetitiveContinueLearningAdapterListener competitiveContinueLearningAdapterListener;

    public CompetitiveContinueLearningAdapter(Context context, List<ContinueLearningDto> list,
                                              CompetitiveContinueLearningAdapterListener competitiveContinueLearningAdapterListener) {
        this.continueLearningDtoList = list;
        this.context = context;
        this.competitiveContinueLearningAdapterListener = competitiveContinueLearningAdapterListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        if (viewType == CompetitiveContinueLearningType.MICROLECTURE.type()) {
            View view = layoutInflater.inflate(R.layout.continue_learning_microlecture_layout, viewGroup, false);
            return new MicrolectureVideoViewHolder(view);
        } else if (viewType == CompetitiveContinueLearningType.ADAPTIVE_PRACTICE.type()) {
            View view = layoutInflater.inflate(R.layout.continue_learning_practise_layout, viewGroup, false);
            return new PracticeQuestionViewHolder(view);
        } else if (viewType == CompetitiveContinueLearningType.NOTES.type()) {
            View view = layoutInflater.inflate(R.layout.continue_learning_note_layout, viewGroup, false);
            return new NoteViewHolder(view);
        } else {
            View view = layoutInflater.inflate(R.layout.continue_learning_practise_layout, viewGroup, false);
            return new CommonViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ContinueLearningDto continueLearningDto = continueLearningDtoList.get(i);
        if (viewHolder instanceof MicrolectureVideoViewHolder) {

            MicrolectureVideoViewHolder lectureViewHolder = (MicrolectureVideoViewHolder) viewHolder;
            Glide.with(context).load(continueLearningDto.getThumbnailUrl()).into(lectureViewHolder.microLectureThumbnail);
            lectureViewHolder.taskPosition.setText(continueLearningDto.getPosition() + "/" + continueLearningDtoList.size());

            lectureViewHolder.rl_position.setVisibility(continueLearningDto.isShowPosition() ? View.VISIBLE : View.GONE);
            if (continueLearningDto.getProgressPercentage() >= 0) {
                lectureViewHolder.progressIndicator.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    lectureViewHolder.progressIndicator.setProgress(continueLearningDto.getProgressPercentage(), true);
                } else {
                    lectureViewHolder.progressIndicator.setProgress(continueLearningDto.getProgressPercentage());
                }
            } else {
                lectureViewHolder.progressIndicator.setVisibility(View.GONE);
            }
            if (continueLearningDto.isNew()) {
                lectureViewHolder.newBadge.setVisibility(View.VISIBLE);
            } else {
                lectureViewHolder.newBadge.setVisibility(View.GONE);
            }

            lectureViewHolder.tv_video_duration.setText(StringUtils.isNotEmpty(continueLearningDto.getDurationText()) ? continueLearningDto.getDurationText() : "");
            lectureViewHolder.completedStatus.setVisibility(continueLearningDto.isCompleted() ? View.VISIBLE : View.GONE);

            if (StringUtils.isNotEmpty(continueLearningDto.getMicrolectureViews())) {
                lectureViewHolder.ll_view_count.setVisibility(View.VISIBLE);
                lectureViewHolder.tv_view_count.setText(continueLearningDto.getMicrolectureViews());
            } else {
                lectureViewHolder.ll_view_count.setVisibility(View.GONE);
            }

            lectureViewHolder.itemView.setOnClickListener(v -> {

                String className = continueLearningDto.getClassName();
                if (StringUtils.isNotEmpty(className)) {
                    openActivityWithParams(continueLearningDto.getClassName(), continueLearningDto.getActivityParams(), CompetitiveContinueLearningType.MICROLECTURE.name());
                }
//                Intent intent = new Intent(context.getApplicationContext(), MicrolectureListActivity.class);
//                intent.putExtra("chapterId", continueLearningDto.getChapterId());
//                intent.putExtra("chapterName", continueLearningDto.getChapterName());
//                intent.putExtra("hideTapTarget", true);
//                intent.putExtra("microLectureId", continueLearningDto.getMicroLectureId());
//                intent.putExtra("openVideo", true);
//                Map<String, String> params = new HashMap<>();
//                params.put("parametersDetail", String.valueOf(intent));
//                params.put("cardType", "lecture");
//                MyApplication.logFirebaseBundleCreation("todays_task_card_clicked", params);
//                context.startActivity(intent);
            });

            try {
                if (continueLearningDto.getProgressPercentage() >= 10 && continueLearningDto.getProgressPercentage() <= 90) {

                    int progress = continueLearningDto.getProgressPercentage();

                    lectureViewHolder.progressIndicator.setProgress(progress);

                    lectureViewHolder.progressIndicator.post(() -> {
                        int progressInt = lectureViewHolder.progressIndicator.getProgress();
                        int max = lectureViewHolder.progressIndicator.getMax();

                        if (max <= 0) return;

                        float percent = progressInt / (float) max;
                        int barWidth = lectureViewHolder.progressIndicator.getWidth();

                        float x = (barWidth * percent)
                                - (lectureViewHolder.progress_dot.getWidth() / 2f);

                        x = Math.max(0, Math.min(x, barWidth - lectureViewHolder.progress_dot.getWidth()));

                        lectureViewHolder.progress_dot.setTranslationX(x);

                        lectureViewHolder.progressIndicator.setVisibility(View.VISIBLE);
                        lectureViewHolder.progress_dot.setVisibility(View.VISIBLE);
                        lectureViewHolder.rl_dot_layout.setVisibility(View.VISIBLE);

                    });

                } else {
                    int progress = continueLearningDto.getProgressPercentage();
                    lectureViewHolder.progressIndicator.setProgress(progress);
                    lectureViewHolder.progressIndicator.setVisibility(View.VISIBLE);
                    lectureViewHolder.progress_dot.setVisibility(View.GONE);
                    lectureViewHolder.rl_dot_layout.setVisibility(View.GONE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                lectureViewHolder.progress_dot.setVisibility(View.GONE);
                lectureViewHolder.rl_dot_layout.setVisibility(View.GONE);
            }
        }
        else if (viewHolder instanceof PracticeQuestionViewHolder) {

            PracticeQuestionViewHolder holder = (PracticeQuestionViewHolder) viewHolder;

            Glide.with(context)
                    .load(continueLearningDto.getSubjectIcon())
                    .placeholder(R.drawable.ic_physics)
                    .error(R.drawable.white_logo)
                    .into(holder.iv_subject_icon);

            holder.tv_task_position.setText(continueLearningDto.getPosition() + "/" + continueLearningDtoList.size());

            holder.rl_position.setVisibility(continueLearningDto.isShowPosition() ? View.VISIBLE : View.GONE);

            holder.tv_title.setText(StringUtils.isNotEmpty(continueLearningDto.getTitle()) ? CommonMethods.capitalizeWords(continueLearningDto.getTitle()) : context.getString(R.string.practice));
            holder.tv_subtitle.setText(StringUtils.isNotEmpty(continueLearningDto.getSubtitle()) ? continueLearningDto.getSubtitle() : "");
            holder.tv_subtitle2.setText(StringUtils.isNotEmpty(continueLearningDto.getBodyText()) ? continueLearningDto.getBodyText() : "Finish Level");
            holder.iv_completed_status.setVisibility(continueLearningDto.isCompleted() ? View.VISIBLE : View.GONE);
            holder.tv_bottom_text.setText(StringUtils.isNotEmpty(continueLearningDto.getBtnText()) ? continueLearningDto.getBtnText() : context.getString(R.string.continue_practice));

            holder.itemView.setOnClickListener(v -> {
                openActivityWithParams(continueLearningDto.getClassName(), continueLearningDto.getActivityParams(), CompetitiveContinueLearningType.ADAPTIVE_PRACTICE.name());
            });

//            holder.itemView.setOnClickListener(v -> {
//                openActivityWithParams(continueLearningDto.getClassName(), continueLearningDto.getActivityParams(), CompetitiveContinueLearningType.QUESTION.name());
//                Intent intent = new Intent(context.getApplicationContext(), MicrolectureListActivity.class);
//                intent.putExtra("chapterId", continueLearningDto.getChapterId());
//                intent.putExtra("chapterName", continueLearningDto.getChapterName());
//                intent.putExtra("hideTapTarget", true);
//                intent.putExtra("microLectureId", continueLearningDto.getMicroLectureId());
//                intent.putExtra("openVideo", false);
//                intent.putExtra("highlightQuestion", true);
//                Map<String, String> params = new HashMap<>();
//                params.put("parametersDetail", String.valueOf(intent));
//                params.put("cardType", "practice");
//                MyApplication.logFirebaseBundleCreation("todays_task_card_clicked", params);
//                context.startActivity(intent);
//            });
        }
        else if (viewHolder instanceof NoteViewHolder) {

            NoteViewHolder holder = (NoteViewHolder) viewHolder;

            Glide.with(context)
                    .load(continueLearningDto.getSubjectIcon())
                    .placeholder(R.drawable.ic_physics)
                    .error(R.drawable.white_logo)
                    .into(holder.iv_subject_icon);

            holder.tv_task_position.setText(continueLearningDto.getPosition() + "/" + continueLearningDtoList.size());

            holder.rl_position.setVisibility(continueLearningDto.isShowPosition() ? View.VISIBLE : View.GONE);

            holder.tv_title.setText(StringUtils.isNotEmpty(continueLearningDto.getTitle()) ? continueLearningDto.getTitle() : "");
            holder.tv_subtitle.setText(StringUtils.isNotEmpty(continueLearningDto.getSubtitle()) ? continueLearningDto.getSubtitle() : "");
            holder.iv_completed_status.setVisibility(continueLearningDto.isCompleted() ? View.VISIBLE : View.GONE);
            holder.tv_bottom_text.setText(StringUtils.isNotEmpty(continueLearningDto.getBtnText()) ? continueLearningDto.getBtnText() : "");

            holder.pb_progress.setProgress(continueLearningDto.getProgressPercentage());
            holder.tv_progress.setText(continueLearningDto.getProgressPercentage()+"%");

            holder.itemView.setOnClickListener(v -> {
                openActivityWithParams(continueLearningDto.getClassName(), continueLearningDto.getActivityParams(), CompetitiveContinueLearningType.NOTES.name());
            });

//            holder.itemView.setOnClickListener(v -> {
//                openActivityWithParams(continueLearningDto.getClassName(), continueLearningDto.getActivityParams(), CompetitiveContinueLearningType.QUESTION.name());
//                Intent intent = new Intent(context.getApplicationContext(), MicrolectureListActivity.class);
//                intent.putExtra("chapterId", continueLearningDto.getChapterId());
//                intent.putExtra("chapterName", continueLearningDto.getChapterName());
//                intent.putExtra("hideTapTarget", true);
//                intent.putExtra("microLectureId", continueLearningDto.getMicroLectureId());
//                intent.putExtra("openVideo", false);
//                intent.putExtra("highlightQuestion", true);
//                Map<String, String> params = new HashMap<>();
//                params.put("parametersDetail", String.valueOf(intent));
//                params.put("cardType", "practice");
//                MyApplication.logFirebaseBundleCreation("todays_task_card_clicked", params);
//                context.startActivity(intent);
//            });
        }
    }

    private void openActivityWithParams(String className, Map<String, Object> activityParams, String type) {
        MyApplication.saveData(PreferenceKeys.REDIRECTED_FROM_CONTINUE_LEARNING.getKey(), true);
        try {
            if (StringUtils.isNotEmpty(className)) {
                Log.d(TAG, "--> ClassName: " + className + ", " + activityParams.toString());
                if (className.equals("openAdaptivePractiseWebView")) {
                    Object levelObj = activityParams.get("level");
                    Object topicIdObj = activityParams.get("topicId");
                    if (levelObj == null || topicIdObj == null) {
                        Log.e(TAG, "openAdaptivePractiseWebView: missing level or topicId in activityParams: " + activityParams);
                        Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String levelNo = levelObj.toString();
                    String topicId = topicIdObj.toString();
                    String pathUrl = AdaptivePath.ADAPTIVE_PRACTISE_FALSE.path().replace("{level}", levelNo).replace("{topicId}", topicId);
                    Map<String, Object> webViewParams = new HashMap<>(activityParams);
                    webViewParams.remove("level");
                    webViewParams.remove("topicId");
                    CommonNavigator.navigateToWebViewByUrlParams(context, CommonMethods.getPersonalizedPathUrl() + pathUrl, webViewParams,
                            true, true, false, false, false, false, null, true, true, false, false, null,TAG,null);
                } else {
                    MyApplication.openActivity(context, className, activityParams);
                }
                Map<String, String> params = new HashMap<>();
                params.put("className", className);
                params.put("type", type);
                params.put("params", activityParams != null ? activityParams.toString() : "");

                if (type.equals(CompetitiveContinueLearningType.MICROLECTURE.name())) {
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.continue_learning_lecture_card_clicked.getActionName(), params);
                } else if (type.equals(CompetitiveContinueLearningType.ADAPTIVE_PRACTICE.name())) {
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.continue_learning_practise_card_clicked.getActionName(), params);
                } else if (type.equals(CompetitiveContinueLearningType.NOTES.name())) {
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.continue_learning_notes_card_clicked.getActionName(), params);
                }
            } else {
                Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return continueLearningDtoList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (continueLearningDtoList.get(position).getType().equals(CompetitiveContinueLearningType.MICROLECTURE.name())) {
            return CompetitiveContinueLearningType.MICROLECTURE.type();
        } else if (continueLearningDtoList.get(position).getType().equals(CompetitiveContinueLearningType.ADAPTIVE_PRACTICE.name())) {
            return CompetitiveContinueLearningType.ADAPTIVE_PRACTICE.type();
        } else if (continueLearningDtoList.get(position).getType().equals(CompetitiveContinueLearningType.NOTES.name())) {
            return CompetitiveContinueLearningType.NOTES.type();
        } else if (continueLearningDtoList.get(position).getType().equals(CompetitiveContinueLearningType.QUESTION.name())) {
            return CompetitiveContinueLearningType.QUESTION.type();
        } else if (continueLearningDtoList.get(position).getType().equals(CompetitiveContinueLearningType.TEST.name())) {
            return CompetitiveContinueLearningType.TEST.type();
        } else {
            return CompetitiveContinueLearningType.QUESTION.type();
        }

    }

    public static class PracticeQuestionViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_completed_status, iv_bottom, iv_subject_icon;
        TextView tv_bottom_text, tv_task_position, tv_title, tv_subtitle, tv_subtitle2;
        CardView new_badge, main_card;
        RelativeLayout rl_position;

        public PracticeQuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_completed_status = itemView.findViewById(R.id.iv_completed_status);
            iv_subject_icon = itemView.findViewById(R.id.iv_subject_icon);
            tv_title = itemView.findViewById(R.id.tv_title);

            new_badge = itemView.findViewById(R.id.new_badge);
            main_card = itemView.findViewById(R.id.main_card);
            tv_bottom_text = itemView.findViewById(R.id.tv_bottom_text);
            iv_bottom = itemView.findViewById(R.id.iv_bottom);
            tv_task_position = itemView.findViewById(R.id.tv_task_position);
            rl_position = itemView.findViewById(R.id.rl_position);
            tv_subtitle = itemView.findViewById(R.id.tv_subtitle);
            tv_subtitle2 = itemView.findViewById(R.id.tv_subtitle2);
        }
    }

    public static class MicrolectureVideoViewHolder extends RecyclerView.ViewHolder {
        ImageView microLectureThumbnail, completedStatus;
        TextView taskPosition, tv_video_duration, tv_view_count;
        LinearProgressIndicator progressIndicator;
        CardView newBadge;
        ImageView iv_play, iv_view;
        RelativeLayout rl_position, rl_dot_layout;
        LinearLayout ll_view_count;
        View progress_dot;

        public MicrolectureVideoViewHolder(@NonNull View itemView) {
            super(itemView);
            microLectureThumbnail = itemView.findViewById(R.id.micro_lecture_thumbnail);
            newBadge = itemView.findViewById(R.id.new_badge);
            taskPosition = itemView.findViewById(R.id.task_position);
            progressIndicator = itemView.findViewById(R.id.micro_lecture_progress);
            completedStatus = itemView.findViewById(R.id.completed_status);
            iv_play = itemView.findViewById(R.id.iv_play);
            iv_view = itemView.findViewById(R.id.iv_view);
            tv_video_duration = itemView.findViewById(R.id.tv_video_duration);
            tv_view_count = itemView.findViewById(R.id.tv_view_count);
            rl_position = itemView.findViewById(R.id.rl_position);
            ll_view_count = itemView.findViewById(R.id.ll_view_count);
            rl_dot_layout = itemView.findViewById(R.id.rl_dot_layout);
            progress_dot = itemView.findViewById(R.id.progress_dot);
        }
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_completed_status, iv_bottom, iv_subject_icon;
        TextView tv_bottom_text, tv_task_position, tv_title, tv_subtitle;
        CardView new_badge, main_card;
        RelativeLayout rl_position;
        ConstraintLayout cl_progress;
        ProgressBar pb_progress;
        TextView tv_progress, tv_body;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_completed_status = itemView.findViewById(R.id.iv_completed_status);
            iv_subject_icon = itemView.findViewById(R.id.iv_subject_icon);
            tv_title = itemView.findViewById(R.id.tv_title);

            new_badge = itemView.findViewById(R.id.new_badge);
            main_card = itemView.findViewById(R.id.main_card);
            tv_bottom_text = itemView.findViewById(R.id.tv_bottom_text);
            iv_bottom = itemView.findViewById(R.id.iv_bottom);
            tv_task_position = itemView.findViewById(R.id.tv_task_position);
            rl_position = itemView.findViewById(R.id.rl_position);
            tv_subtitle = itemView.findViewById(R.id.tv_subtitle);
            cl_progress = itemView.findViewById(R.id.cl_progress);
            pb_progress = itemView.findViewById(R.id.pb_progress);
            tv_progress = itemView.findViewById(R.id.tv_progress);
            tv_body = itemView.findViewById(R.id.tv_body);
        }
    }

    public static class CommonViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_completed_status, iv_subject_icon_img, iv_bottom;
        TextView tv_text1, tv_text2, tv_text3, tv_bottom_text, tv_task_position;
        CardView new_badge, main_card;

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
        }
    }

    public static class CommonVideoViewHolder extends RecyclerView.ViewHolder {
        ImageView microLectureThumbnail, completedStatus;
        TextView taskPosition, microLectureDuration, viewCountText;
        TextView microLectureTitle;
        LinearLayout viewCount;
        LinearProgressIndicator progressIndicator;
        CardView newBadge;

        public CommonVideoViewHolder(@NonNull View itemView) {
            super(itemView);
            microLectureThumbnail = itemView.findViewById(R.id.micro_lecture_thumbnail);
            newBadge = itemView.findViewById(R.id.new_badge);
            microLectureDuration = itemView.findViewById(R.id.microlecture_duration);
            taskPosition = itemView.findViewById(R.id.task_position);
            microLectureTitle = itemView.findViewById(R.id.micro_lecture_title);
            progressIndicator = itemView.findViewById(R.id.micro_lecture_progress);
            completedStatus = itemView.findViewById(R.id.completed_status);
            viewCount = itemView.findViewById(R.id.view_count);
            viewCountText = itemView.findViewById(R.id.view_count_text);
        }
    }

    public interface CompetitiveContinueLearningAdapterListener {
        public void videoClick(String identifier);
    }

}
