package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.enums.ChapterEnum.ChapterStatus;
import arivihan.technologies.doubtbuzzter2.model.UiSession;
import arivihan.technologies.doubtbuzzter2.model.UiTimetableDay;

public class TimeTableAdapter extends RecyclerView.Adapter<TimeTableAdapter.ViewHolder> {

    // ── Date-column colours ───────────────────────────────────────────────────
    private static final int COLOR_TODAY_BG    = Color.parseColor("#00BCD4");
    private static final int COLOR_TODAY_TEXT  = Color.WHITE;
    private static final int COLOR_NORMAL_BG   = Color.parseColor("#F4F5F9");
    private static final int COLOR_NORMAL_TEXT = Color.parseColor("#1A1A1A");
    private static final int COLOR_DIVIDER     = Color.parseColor("#D9D9D9");

    // ── Rescheduled background ────────────────────────────────────────────────
    private static final int COLOR_RESCHEDULED_BG = Color.parseColor("#F8F3F1");

    // ── Missed background (greyed out) ────────────────────────────────────────
    private static final int COLOR_MISSED_BG = Color.parseColor("#F4F4F4");

    // ── Interface ─────────────────────────────────────────────────────────────
    public interface OnItemClickListener {
        void onPlayClick(UiTimetableDay day, UiSession session, int index);
        void onItemClick(UiTimetableDay day, UiSession session, int index);
        void onLockedClick(UiTimetableDay day, UiSession session, int index);
    }

    private final Context context;
    private final List<UiTimetableDay> items;
    private OnItemClickListener listener;

    public TimeTableAdapter(Context context, List<UiTimetableDay> items) {
        this.context = context;
        this.items   = items;
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.listener = l;
    }

    // ── Adapter overrides ─────────────────────────────────────────────────────

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_timetable, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        UiTimetableDay day = items.get(position);

        h.tvDayNumber.setText(String.valueOf(day.getDayNumber()));
        h.tvDayName.setText(day.getDayLabel());

        if (day.isToday()) {
            h.layoutDate.setBackgroundColor(COLOR_TODAY_BG);
            h.tvDayNumber.setTextColor(COLOR_TODAY_TEXT);
            h.tvDayName.setTextColor(COLOR_TODAY_TEXT);
        } else {
            h.layoutDate.setBackgroundColor(COLOR_NORMAL_BG);
            h.tvDayNumber.setTextColor(COLOR_NORMAL_TEXT);
            h.tvDayName.setTextColor(COLOR_NORMAL_TEXT);
        }

        h.layoutClasses.removeAllViews();

        List<UiSession> sessions = day.getSessions();
        for (int i = 0; i < sessions.size(); i++) {
            if (i > 0) h.layoutClasses.addView(makeDivider());
            View row = LayoutInflater.from(context)
                    .inflate(R.layout.item_class_entry, h.layoutClasses, false);
            bindSession(row, day, sessions.get(i), i);
            h.layoutClasses.addView(row);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ── Bind one session row ──────────────────────────────────────────────────

    private void bindSession(View row, UiTimetableDay day, UiSession session, int index) {

        TextView            tvClassName          = row.findViewById(R.id.tvClassName);
        TextView            tvSubject            = row.findViewById(R.id.tvSubject);
        View                dotStatus            = row.findViewById(R.id.dotStatus);
        TextView            tvTimeSlot           = row.findViewById(R.id.tvTimeSlot);
        CardView            cvSubject            = row.findViewById(R.id.cvSubject);
        CardView            cvMissed             = row.findViewById(R.id.cv_missed);
        FrameLayout         layoutTeacherSection = row.findViewById(R.id.layoutTeacherSection);
        ImageView           ivPlay               = row.findViewById(R.id.ivPlay);
        LottieAnimationView lottieView           = row.findViewById(R.id.dialog_animation);
        CardView            cvUpcoming           = row.findViewById(R.id.cv_upcoming);
        View                viewTeacherBg        = row.findViewById(R.id.viewTeacherBg);
        ImageView           ivTeacher            = row.findViewById(R.id.ivTeacher);
        LinearLayout        layoutRescheduled    = row.findViewById(R.id.layoutRescheduled);
        TextView            tvRescheduledTag     = row.findViewById(R.id.tvRescheduledTag);
        TextView            tvRescheduledDate    = row.findViewById(R.id.tvRescheduledDate);
        TextView            tvRescheduledTime    = row.findViewById(R.id.tvRescheduledTime);

        // ── Base content ──────────────────────────────────────────────────────
        String rawColor = session.getSubjectColor();
        int subjectColor = (rawColor != null && !rawColor.isEmpty())
                ? Color.parseColor(rawColor)
                : Color.parseColor("#00BCD4");
        tvClassName.setText(session.getTitle());
        tvSubject.setText(session.getSubject());
        tvTimeSlot.setText(session.getDuration());
        cvSubject.setCardBackgroundColor(subjectColor);
        dotStatus.getBackground().mutate().setTint(subjectColor);
        viewTeacherBg.setBackgroundColor(subjectColor);

        // ── Teacher image ─────────────────────────────────────────────────────
        Glide.with(context)
                .load(session.getTeacherInfoImageUrl())
                .placeholder(R.drawable.teacher_icon)
                .error(R.drawable.teacher_icon)
                .into(ivTeacher);

        // ── Reset all overlays ────────────────────────────────────────────────
        ivPlay.setVisibility(View.GONE);
        lottieView.setVisibility(View.GONE);
        cvUpcoming.setVisibility(View.GONE);
        cvMissed.setVisibility(View.GONE);
        layoutRescheduled.setVisibility(View.GONE);
        clearAllFades(tvClassName, tvTimeSlot, dotStatus, layoutTeacherSection, cvSubject);
        row.setBackgroundColor(Color.TRANSPARENT);

        // ── Status dispatch ───────────────────────────────────────────────────
        if (session.getStatus() == null) return;
        switch (session.getStatus()) {

            case RESCHEDULED:
                applyRescheduledStyle(row, tvClassName, tvTimeSlot,
                        layoutRescheduled, tvRescheduledTag,
                        tvRescheduledDate, tvRescheduledTime,
                        layoutTeacherSection, cvSubject, session);
                row.setOnClickListener(v -> {
                    if (listener != null) listener.onItemClick(day, session, index);
                });
                break;

            case MISSED:
                // Greyed-out card — student missed a past live class, no playback
                applyMissedStyle(row, tvClassName, tvTimeSlot,
                        cvMissed, layoutTeacherSection, cvSubject, dotStatus);
                row.setOnClickListener(v -> {
                    if (listener != null) listener.onItemClick(day, session, index);
                });
                break;

            case BACKLOG:
                applyBacklogStyle(row, cvMissed, subjectColor, viewTeacherBg);
                ivPlay.setVisibility(View.GONE);
                row.setOnClickListener(v -> {
                    if (listener != null) listener.onItemClick(day, session, index);
                });
                ivPlay.setOnClickListener(v -> {
                    if (listener != null) listener.onPlayClick(day, session, index);
                });
                break;

            case LIVE:
            case LIVE_SCHEDULED:
                lottieView.setVisibility(View.VISIBLE);
                lottieView.playAnimation();
                row.setOnClickListener(v -> {
                    if (listener != null) listener.onItemClick(day, session, index);
                });
                break;

            case UPCOMING:
                cvUpcoming.setVisibility(View.VISIBLE);
                row.setOnClickListener(v -> {
                    if (listener != null) listener.onItemClick(day, session, index);
                });
                break;

            case RECORDED:
            default:
                ivPlay.setVisibility(View.VISIBLE);
                row.setOnClickListener(v -> {
                    if (listener != null) listener.onItemClick(day, session, index);
                });
                ivPlay.setOnClickListener(v -> {
                    if (listener != null) listener.onPlayClick(day, session, index);
                });
                break;
        }
    }

    // ── MISSED: greyed-out, faded, "Missed Class" tag, no play ───────────────

    private void applyMissedStyle(View row,
                                  TextView tvClassName,
                                  TextView tvTimeSlot,
                                  CardView cvMissed,
                                  FrameLayout layoutTeacherSection,
                                  CardView cvSubject,
                                  View dotStatus) {
        row.setBackgroundColor(COLOR_MISSED_BG);
        tvClassName.setAlpha(0.45f);
        tvTimeSlot.setAlpha(0.45f);
        dotStatus.setAlpha(0.45f);
        layoutTeacherSection.setAlpha(0.6f);
        cvSubject.setAlpha(0.5f);
        cvMissed.setVisibility(View.VISIBLE);
    }

    // ── BACKLOG: subject-color bg, "Missed Class" tag, play available ─────────

    private void applyBacklogStyle(View row,
                                   CardView cvMissed,
                                   int subjectColor,
                                   View viewTeacherBg) {
        viewTeacherBg.setBackgroundColor(subjectColor);
        cvMissed.setVisibility(View.VISIBLE);

    }

    // ── RESCHEDULED ───────────────────────────────────────────────────────────

    private void applyRescheduledStyle(View row,
                                       TextView tvClassName,
                                       TextView tvTimeSlot,
                                       LinearLayout layoutRescheduled,
                                       TextView tvRescheduledTag,
                                       TextView tvRescheduledDate,
                                       TextView tvRescheduledTime,
                                       FrameLayout layoutTeacherSection,
                                       CardView cvSubject,
                                       UiSession session) {
        row.setBackgroundColor(COLOR_RESCHEDULED_BG);
        tvClassName.setPaintFlags(tvClassName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        tvClassName.setAlpha(0.45f);
        tvTimeSlot.setPaintFlags(tvTimeSlot.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        tvTimeSlot.setAlpha(0.45f);
        layoutTeacherSection.setVisibility(View.GONE);
        cvSubject.setVisibility(View.GONE);
        layoutRescheduled.setVisibility(View.VISIBLE);
        tvRescheduledTag.setText(context.getString(R.string.rescheduled));
        tvRescheduledDate.setText(
                session.getRescheduledDate() != null ? session.getRescheduledDate() : "");
        tvRescheduledTime.setText(
                session.getRescheduledTime() != null ? session.getRescheduledTime() : "");
    }

    // ── Reset all fades/flags for recycled views ──────────────────────────────

    private void clearAllFades(TextView tvClassName,
                               TextView tvTimeSlot,
                               View dotStatus,
                               FrameLayout layoutTeacherSection,
                               CardView cvSubject) {
        tvClassName.setPaintFlags(tvClassName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        tvClassName.setAlpha(1.0f);
        tvTimeSlot.setPaintFlags(tvTimeSlot.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        tvTimeSlot.setAlpha(1.0f);
        dotStatus.setAlpha(1.0f);
        layoutTeacherSection.setVisibility(View.VISIBLE);
        layoutTeacherSection.setAlpha(1.0f);
        cvSubject.setVisibility(View.VISIBLE);
        cvSubject.setAlpha(1.0f);
    }

    // ── Divider ───────────────────────────────────────────────────────────────

    private View makeDivider() {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        View startSpacer = new View(context);
        startSpacer.setLayoutParams(new LinearLayout.LayoutParams(0, 2, 1f));

        View divider = new View(context);
        divider.setLayoutParams(new LinearLayout.LayoutParams(0, 2, 14f));
        divider.setBackgroundColor(COLOR_DIVIDER);

        View endSpacer = new View(context);
        endSpacer.setLayoutParams(new LinearLayout.LayoutParams(0, 2, 1f));

        container.addView(startSpacer);
        container.addView(divider);
        container.addView(endSpacer);
        return container;
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────
    static class ViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout layoutDate;
        final TextView tvDayNumber;
        final TextView tvDayName;
        final LinearLayout layoutClasses;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutDate = itemView.findViewById(R.id.layoutDate);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
            tvDayName = itemView.findViewById(R.id.tvDayName);
            layoutClasses = itemView.findViewById(R.id.layoutClasses);
        }
    }
}