package arivihan.technologies.doubtbuzzter2.adapter;

import static arivihan.technologies.doubtbuzzter2.MyApplication.getPendingSubscriptionDatabaseReference;

import android.content.Context;
import android.graphics.Typeface;
import android.os.CountDownTimer;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;                          // add Glide dep if not present

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.model.DoubtSolutionStatus;
import arivihan.technologies.doubtbuzzter2.model.DoubtStatusDto;
import arivihan.technologies.doubtbuzzter2.model.TestGenerationStatus;
import arivihan.technologies.doubtbuzzter2.model.TestGenerationStatusDto;
import arivihan.technologies.doubtbuzzter2.model.PendingSubscriptionEntry;
import arivihan.technologies.doubtbuzzter2.model.RescheduledLectureStatusDto;   // ← new
import arivihan.technologies.doubtbuzzter2.model.SubscriptionStatus;
import arivihan.technologies.doubtbuzzter2.model.TestEvaluationStatusEnum;
import arivihan.technologies.doubtbuzzter2.model.TopperNoteDownloadedStatusDto;
import arivihan.technologies.doubtbuzzter2.model.UserTestEvaluationStautsObject;
import arivihan.technologies.doubtbuzzter2.model.asat.UserAsatFirebaseData;
import lombok.Getter;
import lombok.Setter;

public class HomeStatusAdapter extends RecyclerView.Adapter<HomeStatusAdapter.HomeStatusViewHolder> {

    private final ArrayList<Object> list;
    private final Context context;
    private final ClickListener clickListener;
    @Getter
    @Setter
    private boolean collapsed = true;
    private CountDownTimer globalCountDownTimer;

    // ── view-type constants ────────────────────────────────────────────────────
    private static final int TYPE_DEFAULT            = 0;
    private static final int TYPE_TOPPER_NOTE        = 1;
    private static final int TYPE_RESCHEDULED_CLASS  = 2;
    private static final int TYPE_TEST_GENERATION    = 3;

    // ── view-type dispatch ─────────────────────────────────────────────────────
    @Override
    public int getItemViewType(int position) {
        Object item = list.get(position);
        if (item instanceof TopperNoteDownloadedStatusDto)  return TYPE_TOPPER_NOTE;
        if (item instanceof RescheduledLectureStatusDto)    return TYPE_RESCHEDULED_CLASS;
        if (item instanceof TestGenerationStatusDto)        return TYPE_TEST_GENERATION;
        return TYPE_DEFAULT;
    }

    public HomeStatusAdapter(ArrayList<Object> list, Context context, ClickListener clickListener) {
        this.list = list;
        this.context = context;
        this.clickListener = clickListener;
        startGlobalTimer();
    }

    // ── inflate the correct layout per type ───────────────────────────────────
    @NonNull
    @Override
    public HomeStatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes;
        switch (viewType) {
            case TYPE_TOPPER_NOTE:       layoutRes = R.layout.topper_note_status_layout;     break;
            case TYPE_RESCHEDULED_CLASS: layoutRes = R.layout.rescheduled_class_layout;      break;
            case TYPE_TEST_GENERATION:   layoutRes = R.layout.test_generation_status_layout; break;
            default:                     layoutRes = R.layout.test_result_status_layout;      break;
        }
        View view = LayoutInflater.from(context).inflate(layoutRes, parent, false);
        return new HomeStatusViewHolder(view, viewType);
    }

    // ── bind ──────────────────────────────────────────────────────────────────
    @Override
    public void onBindViewHolder(@NonNull HomeStatusViewHolder holder, int position) {
        if (holder.countDownTimer != null) holder.countDownTimer.cancel();

        // collapsed: hide every card except the last one
        if (position != list.size() - 1 && collapsed) {
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        } else {
            holder.itemView.setLayoutParams(
                    new RecyclerView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
            holder.itemView.setVisibility(View.VISIBLE);
        }

        Object item = list.get(position);

        if (item instanceof RescheduledLectureStatusDto lecture) {

            Log.d("HomeStatusAdapter", "Binding RescheduledLectureStatusDto");
            Log.d("HomeStatusAdapter", "Title: " + lecture.getTitle());
            Log.d("HomeStatusAdapter", "Subtitle: " + lecture.getSubTitle());
            Log.d("HomeStatusAdapter", "NewStartTime: " + lecture.getNewStartTime());
            Log.d("HomeStatusAdapter", "ThumbnailUrl: " + lecture.getThumbnailUrl());
            Log.d("HomeStatusAdapter", "SubjectId: " + lecture.getSubjectId());
            Log.d("HomeStatusAdapter", "SubjectCode: " + lecture.getSubjectCode());
            Log.d("HomeStatusAdapter", "ChapterId: " + lecture.getChapterId());

            if (holder.testTitle != null && StringUtils.isNotEmpty(lecture.getTitle())) {
                holder.testTitle.setText(lecture.getTitle());
                Log.d("HomeStatusAdapter", "Set title: " + lecture.getTitle());
            }

            if (holder.resultStatus != null && StringUtils.isNotEmpty(lecture.getSubTitle())) {
                holder.resultStatus.setText(boldCurlyBraces(lecture.getSubTitle()));
                Log.d("HomeStatusAdapter", "Set subtitle: " + lecture.getSubTitle());
            }

            if (lecture.getNewStartTime() > 0 && holder.text2 != null) {
                String formatted = new java.text.SimpleDateFormat(
                        "dd MMM, hh:mm a", java.util.Locale.getDefault())
                        .format(new java.util.Date(lecture.getNewStartTime()));

                holder.text2.setText(formatted);
                Log.d("HomeStatusAdapter", "Formatted time: " + formatted);
            }

            if (holder.lectureThumbnail != null) {
                if (StringUtils.isNotEmpty(lecture.getThumbnailUrl())) {
                    Log.d("HomeStatusAdapter", "Loading thumbnail: " + lecture.getThumbnailUrl());

                    Glide.with(context)
                            .load(lecture.getThumbnailUrl())
                            .placeholder(R.drawable.ic_launcher_background)
                            .centerCrop()
                            .into(holder.lectureThumbnail);
                } else {
                    Log.d("HomeStatusAdapter", "Thumbnail URL is empty. Using placeholder.");
                    holder.lectureThumbnail.setImageResource(R.drawable.ic_launcher_background);
                }
            }

            if (holder.btnView != null) {
                holder.btnView.setOnClickListener(v -> {
                    Log.d("HomeStatusAdapter", "View clicked");
                    Log.d("HomeStatusAdapter", "Navigating with SubjectId=" + lecture.getSubjectId()
                            + ", SubjectCode=" + lecture.getSubjectCode()
                            + ", ChapterId=" + lecture.getChapterId());

                    CommonNavigator.navigateToChapterListByBatchActivity(
                            context,
                            lecture.getSubjectId(),
                            "d",
                            lecture.getSubjectCode(),
                            "HomeStatusAdapter",
                            "lectures",
                            null,
                            lecture.getChapterId());

                    Log.d("HomeStatusAdapter", "Deleting lecture after navigation");
                    delete(lecture);
                });
            }
        }

        // ── Doubt ─────────────────────────────────────────────────────────────
        else if (item instanceof DoubtStatusDto doubtStatusDto) {
            if (StringUtils.isNotEmpty(doubtStatusDto.getDoubtText())) {
                holder.testTitle.setText(doubtStatusDto.getDoubtText().trim().replace("\n", ""));
            }
            if (DoubtSolutionStatus.SOLVED_UNSEEN.equals(doubtStatusDto.getStatus())) {
                holder.resultStatus.setText(context.getString(R.string.Aapka_solution_ab_ready_hai));
                holder.text1.setText(context.getString(R.string.View_your));
                holder.text2.setText(context.getString(R.string.solution));
                holder.animationView.setAnimation(R.raw.tick_mark_bottom_status);
            } else {
                holder.resultStatus.setText(context.getString(R.string.Apka_solution_ban_rha_hai));
                holder.text1.setText(context.getString(R.string.solution_in));
                holder.animationView.setAnimation(R.raw.no_results);
                long remaining = doubtStatusDto.getCloseTime() - System.currentTimeMillis() + 60000;
                holder.text2.setText(
                        String.format(context.getString(R.string.solution_in_2_min), formatTime(remaining)));
                if (remaining <= 0) delete(doubtStatusDto);
            }
        }

        // ── Test generation ───────────────────────────────────────────────────
        else if (item instanceof TestGenerationStatusDto dto) {
            if (TestGenerationStatus.COMPLETED_UNSEEN.equals(dto.getStatus())) {
                holder.testTitle.setText(context.getString(R.string.test_gen_completed_title));
                holder.resultStatus.setText(context.getString(R.string.test_gen_completed_subtitle));
                holder.animationView.setAnimation(R.raw.tick_mark_bottom_status);
                setAnimationTopMarginDp(holder.animationView, 0);
                if (holder.chipTime != null) holder.chipTime.setVisibility(View.GONE);
                if (holder.chipStart != null) holder.chipStart.setVisibility(View.VISIBLE);
            } else {
                holder.testTitle.setText(context.getString(R.string.test_gen_in_progress_title));
                holder.resultStatus.setText(context.getString(R.string.test_gen_in_progress_subtitle));
                holder.animationView.setAnimation(R.raw.no_results);
                setAnimationTopMarginDp(holder.animationView, -10);
                if (holder.chipStart != null) holder.chipStart.setVisibility(View.GONE);
                if (holder.chipTime != null) holder.chipTime.setVisibility(View.VISIBLE);
                holder.text1.setText(context.getString(R.string.test_gen_est_time));
                long remaining = dto.getCloseTime() - System.currentTimeMillis() + 60000;
                if (remaining > 0) {
                    holder.text2.setText(
                            String.format(context.getString(R.string.solution_in_2_min), formatTime(remaining)));
                } else {
                    holder.text2.setText(context.getString(R.string.test_gen_est_time_value));
                }
            }
        }

        // ── Test evaluation ───────────────────────────────────────────────────
        else if (item instanceof UserTestEvaluationStautsObject obj) {
            if (StringUtils.isNotEmpty(obj.getTestName())) {
                holder.testTitle.setText(obj.getTestName());
            }
            if (TestEvaluationStatusEnum.REVIEW_PENDING.equals(obj.getTestEvaluationStatusEnum())) {
                holder.resultStatus.setText(context.getString(R.string.Aapka_result_ab_ready_hai));
                holder.text1.setText(context.getString(R.string.View_your));
                holder.text2.setText(context.getString(R.string.Result));
                holder.animationView.setAnimation(R.raw.tick_mark_bottom_status);
            } else {
                holder.resultStatus.setText(context.getString(R.string.Apka_test_check_ho_rha_hai));
                holder.text1.setText(context.getString(R.string.result_in));
                holder.animationView.setAnimation(R.raw.no_results);
                long remaining = obj.getCloseTime() - System.currentTimeMillis() + 60000;
                if (remaining > 0) {
                    holder.text2.setText(
                            String.format(context.getString(R.string.Result_time), formatTime(remaining)));
                } else {
                    delete(obj);
                }
            }
        }

        // ── Pending subscription ──────────────────────────────────────────────
        else if (item instanceof PendingSubscriptionEntry sub) {
            if (sub.getStatus().equals(SubscriptionStatus.ACTIVE.name())) {
                if (StringUtils.isNotEmpty(sub.getName())) holder.testTitle.setText(sub.getName());
                holder.resultStatus.setText(context.getString(R.string.Apka_plan_active_ho_gya_hai));
                holder.text1.setText(context.getString(R.string.View_Plan));
                holder.text2.setText(context.getString(R.string.Benefits));
                holder.animationView.setAnimation(R.raw.tick_mark_bottom_status);
            }
        }

        // ── ASAT ──────────────────────────────────────────────────────────────
        else if (item instanceof UserAsatFirebaseData asat) {
            holder.testTitle.setText(asat.getTitle());
            holder.resultStatus.setText(asat.getSubTitle());
            holder.text1.setText(context.getString(R.string.View_your));
            holder.text2.setText(context.getString(R.string.Result));
            holder.animationView.setAnimation(R.raw.tick_mark_bottom_status);
        }

        // ── Topper note ───────────────────────────────────────────────────────
        else if (item instanceof TopperNoteDownloadedStatusDto note) {
            holder.testTitle.setText(note.getTitle());
            holder.resultStatus.setText(note.getSubTitle());
            holder.text1.setText(context.getString(R.string.open));
            holder.animationView.setAnimation(R.raw.tick_mark_bottom_status);
        }

        // ── click listeners ───────────────────────────────────────────────────
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION || pos >= list.size()) return;
            clickListener.onItemClick(list.get(pos));
        });

        if (holder.close != null) {
            holder.close.setOnClickListener(v -> {
                int pos = holder.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION || pos >= list.size()) return;
                Object obj = list.get(pos);
                list.remove(pos);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos, list.size());
                delete(obj);
                clickListener.onItemDeleted();
            });
        }
    }

    private void setAnimationTopMarginDp(LottieAnimationView view, int dp) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        params.topMargin = Math.round(dp * context.getResources().getDisplayMetrics().density);
        view.setLayoutParams(params);
    }

    // ── timer ─────────────────────────────────────────────────────────────────
    private void startGlobalTimer() {
        if (globalCountDownTimer != null) globalCountDownTimer.cancel();
        globalCountDownTimer = new CountDownTimer(Long.MAX_VALUE, 60000) {
            @Override public void onTick(long ms)  { notifyDataSetChanged(); }
            @Override public void onFinish()       { startGlobalTimer(); }
        }.start();
    }

    public void stopTimer() {
        if (globalCountDownTimer != null) globalCountDownTimer.cancel();
    }

    public static int formatTime(long millis) {
        return (int) Math.round(((double) millis / (1000 * 60)) % 60);
    }

    // text between { } is rendered bold; braces are stripped
    private static SpannableStringBuilder boldCurlyBraces(String text) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        int i = 0;
        while (i < text.length()) {
            int open = text.indexOf('{', i);
            if (open == -1) { sb.append(text.substring(i)); break; }
            sb.append(text.substring(i, open));
            int close = text.indexOf('}', open + 1);
            if (close == -1) { sb.append(text.substring(open)); break; }
            int start = sb.length();
            sb.append(text, open + 1, close);
            sb.setSpan(new StyleSpan(Typeface.BOLD), start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            i = close + 1;
        }
        return sb;
    }

    // ── Firebase cleanup ──────────────────────────────────────────────────────
    private void delete(Object object) {
        if (object instanceof DoubtStatusDto dto) {
            MyApplication.getDoubtStatusDatabaseReferenceFotChat(dto.getSessionId()).removeValue();
        } else if (object instanceof TestGenerationStatusDto dto) {
            java.util.Map<String, Object> update = new java.util.HashMap<>();
            if (TestGenerationStatus.COMPLETED_UNSEEN.equals(dto.getStatus())) {
                update.put("status", TestGenerationStatus.COMPLETED_SEEN.name());
            } else {
                update.put("status", TestGenerationStatus.DISMISSED.name());
            }
            MyApplication.getTestGenerationDatabaseReference(dto.getRequestId()).updateChildren(update);
        } else if (object instanceof UserTestEvaluationStautsObject) {
            MyApplication.getTestStatusDatabaseReference().removeValue();
        } else if (object instanceof PendingSubscriptionEntry sub) {
            MyApplication.getPendingSubscriptionDatabaseReference(sub.getPlanId()).removeValue();
        } else if (object instanceof UserAsatFirebaseData) {
            MyApplication.getASATStatusDatabaseReference().removeValue();
        } else if (object instanceof TopperNoteDownloadedStatusDto) {
            MyApplication.logFirebaseBundleCreation(
                    FirebaseEvents.hs_toppers_notes_card_cross_btn_click.getActionName());
            MyApplication.getToppersNotesStatusDatabaseReference().removeValue();
        } else if (object instanceof RescheduledLectureStatusDto lecture) {
        MyApplication.markRescheduledLectureViewed(lecture.getChapterId());
        }
    }

    @Override
    public int getItemCount() { return list.size(); }

    // ── ViewHolder ────────────────────────────────────────────────────────────
    public static class HomeStatusViewHolder extends RecyclerView.ViewHolder {
        // shared across all types
        TextView  testTitle;
        TextView  resultStatus;
        CardView  close;
        CountDownTimer countDownTimer;

        // default / topper-note types
        LottieAnimationView animationView;
        TextView  text1;
        TextView  text2;

        // rescheduled-class type only
        ImageView lectureThumbnail;
        CardView btnView;

        // test-generation type only
        View chipTime;
        View chipStart;

        public HomeStatusViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            // views present in every layout
            testTitle    = itemView.findViewById(R.id.test_title);
            resultStatus = itemView.findViewById(R.id.result_status);
            close        = itemView.findViewById(R.id.close);

            if (viewType == TYPE_RESCHEDULED_CLASS) {
                lectureThumbnail = itemView.findViewById(R.id.lecture_thumbnail);
                btnView = itemView.findViewById(R.id.btn_view);
            } else {
                // default + topper-note + test-generation layouts share these
                animationView = itemView.findViewById(R.id.animation);
                text1         = itemView.findViewById(R.id.text1);
                text2         = itemView.findViewById(R.id.text2);
                if (viewType == TYPE_TEST_GENERATION) {
                    chipTime  = itemView.findViewById(R.id.chip_time);
                    chipStart = itemView.findViewById(R.id.chip_start);
                }
            }
        }

        // keep backward-compat constructor (existing call sites)
        public HomeStatusViewHolder(@NonNull View itemView) {
            this(itemView, TYPE_DEFAULT);
        }

        private static final int TYPE_DEFAULT           = 0;
        private static final int TYPE_RESCHEDULED_CLASS = 2;
        private static final int TYPE_TEST_GENERATION   = 3;
    }

    // ── click callback ────────────────────────────────────────────────────────
    public interface ClickListener {
        default void onItemClick(Object object) {}
        default void onItemDeleted() {}
    }
}