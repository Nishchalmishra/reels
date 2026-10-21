package arivihan.technologies.doubtbuzzter2.adapter;

import static arivihan.technologies.doubtbuzzter2.constants.QuestionType.ESSAY;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.LectureContentType;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureEntity;
import arivihan.technologies.doubtbuzzter2.model.PracticeStatus;
import arivihan.technologies.doubtbuzzter2.model.Questions;
import lombok.Setter;

public class PracticeAdapter extends RecyclerView.Adapter<PracticeAdapter.PracticeViewHolder> {

    Context context;
    List<Questions> list;
    PracticeListener practiceListener;
    boolean isMicrolectureSelected;
    int parentPosition;
    boolean isLastMicrolecture;

//    @Setter

//    MicroLectureEntity selectionMicroLectureEntity;

    public PracticeAdapter(Context context, List<Questions> list, PracticeListener practiceListener, boolean videoWatched, boolean nextVideoWatched, boolean videoCompleted, boolean isLastMicrolecture,boolean isMicrolectureSelected) {
        this.context = context;
        this.list = list;
        this.practiceListener = practiceListener;
        this.videoWatched = videoWatched;
        this.nextVideoWatched = nextVideoWatched;
        this.videoCompleted = videoCompleted;
        this.isLastMicrolecture = isLastMicrolecture;
        this.isMicrolectureSelected=isMicrolectureSelected;
    }

    boolean videoWatched;
    boolean nextVideoWatched;
    boolean videoCompleted;

    @NonNull
    @Override
    public PracticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        parentPosition = (int) parent.getTag();
        View view = LayoutInflater.from(context).inflate(R.layout.practice_question_item, parent, false);
        return new PracticeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PracticeViewHolder holder, int position) {
        Questions questions = list.get(position);
        Questions previousQuestion = null;
        Questions nextQuestion = null;
        if (position > 0) previousQuestion = list.get(position - 1);
        if (position < list.size() - 1) nextQuestion = list.get(position + 1);

        String practiceTypeText = questions.getTitle();
        SpannableString spannableString = new SpannableString(practiceTypeText);
        Typeface typeface2 = ResourcesCompat.getFont(context, R.font.urbanist_bold);

        Pattern numberPattern = Pattern.compile("\\d+/\\d+|\\d+");
        Matcher matcher = numberPattern.matcher(practiceTypeText);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                assert typeface2 != null;
                spannableString.setSpan(
                        new TypefaceSpan(typeface2),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        }

        holder.practiceType.setText(spannableString);
        if (questions.getStatus().equalsIgnoreCase(PracticeStatus.PARTIAL_COMPLETED.name())) {
            holder.dotCard.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.practice_partial_completed)));
        } else if (questions.getStatus().equalsIgnoreCase(PracticeStatus.COMPLETED.name())) {
            holder.dotCard.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.mock_primary_color)));
        }

        if (position == 0 && videoCompleted) {
            holder.topDash.setBackgroundColor(context.getResources().getColor(R.color.mock_primary_color));
        }

        if (isLastMicrolecture && position == list.size() - 1) {
            holder.bottomDash.setVisibility(View.INVISIBLE);
        } else if (questions.getStatus().equalsIgnoreCase(PracticeStatus.COMPLETED.name())) {
            holder.bottomDash.setVisibility(View.VISIBLE);
            holder.bottomDash.setBackgroundColor(context.getResources().getColor(R.color.mock_primary_color));
        } else {
            holder.bottomDash.setVisibility(View.VISIBLE);
        }

        if (questions.getStatus().equalsIgnoreCase(PracticeStatus.PARTIAL_COMPLETED.name())
                || questions.getStatus().equalsIgnoreCase(PracticeStatus.COMPLETED.name())) {

            if ((position == 0 && videoWatched) || (previousQuestion != null && ((previousQuestion.getStatus().equalsIgnoreCase(PracticeStatus.PARTIAL_COMPLETED.name())
                    || previousQuestion.getStatus().equalsIgnoreCase(PracticeStatus.COMPLETED.name()))))) {
                holder.topDash.setBackgroundColor(context.getResources().getColor(R.color.mock_primary_color));
            }
            if ((position == list.size() - 1 && nextVideoWatched) || (nextQuestion != null && ((nextQuestion.getStatus().equalsIgnoreCase(PracticeStatus.PARTIAL_COMPLETED.name()))
                    || nextQuestion.getStatus().equalsIgnoreCase(PracticeStatus.COMPLETED.name())))) {
                holder.bottomDash.setBackgroundColor(context.getResources().getColor(R.color.mock_primary_color));
            }
        } else if (previousQuestion != null && previousQuestion.getStatus().equalsIgnoreCase(PracticeStatus.COMPLETED.name())) {
            holder.topDash.setBackgroundColor(context.getResources().getColor(R.color.mock_primary_color));
        }
        Typeface typeface = ResourcesCompat.getFont(context, R.font.urbanist_bold);
        holder.itemView.setOnClickListener(v -> practiceListener.onPracticeClick(questions.getType(), parentPosition, questions.isLock(), position));
        if (questions.isLock()) {
            if (questions.isHighlightText()) {
                practiceListener.setUpPreviewQuestion(questions.getType(), parentPosition, questions.isLock(), position);
                holder.parentLayout.setBackgroundColor(context.getResources().getColor(R.color.video_layout_background_color));
                holder.practiceType.setTypeface(typeface);
                holder.practiceType.setTextSize(13);
                holder.practiceType.setTextColor(context.getResources().getColor(R.color.black));
                holder.practiceTypeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.lock));
            } else {
                holder.parentLayout.setBackgroundColor(context.getResources().getColor(R.color.white));
                holder.practiceType.setTextSize(12);
                holder.practiceTypeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.lock_light));
                holder.practiceType.setTextColor(context.getResources().getColor(R.color.title_color_light));
            }
        } else {
            if (questions.isHighlightText()) {
                holder.parentLayout.setBackgroundColor(context.getResources().getColor(R.color.video_layout_background_color));
                holder.practiceType.setTextColor(context.getResources().getColor(R.color.black));
                holder.practiceType.setTypeface(typeface);
                holder.practiceType.setTextSize(13);
                setIcon(true, LectureContentType.valueOf(questions.getType()), holder);practiceListener.setUpPreviewQuestion(questions.getType(), parentPosition, questions.isLock(), position);
            } else {
                holder.parentLayout.setBackgroundColor(context.getResources().getColor(R.color.white));
                holder.practiceType.setTextSize(12);
                setIcon(false, LectureContentType.valueOf(questions.getType()), holder);
            }
        }
    }

    public void setIcon(boolean dark, LectureContentType type, PracticeViewHolder holder) {
        dark = dark;
        Log.d("room_db", "setIcon: "+dark+"section"+isMicrolectureSelected);
        switch (type) {
            case CONCEPTUAL_QUESTION:
            case MULTIPLE_CHOICE_QUESTION:
            case NUMERICAL_QUESTION:
                if (dark || isMicrolectureSelected)
                    holder.practiceTypeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.clipboard_check));
                else
                    holder.practiceTypeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.clipboard_light));
                break;
            case FILL_IN_THE_BLANKS:
                holder.practiceTypeIcon.setScaleY(1.1f);
                holder.practiceTypeIcon.setScaleX(1.1f);
                if (dark || isMicrolectureSelected)
                    holder.practiceTypeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.fill_dark));
                else
                    holder.practiceTypeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.fill_light));
                break;
            case ONE_WORD_ANSWER:
                if (dark || isMicrolectureSelected)
                    holder.practiceTypeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.one_word_dark));
                else
                    holder.practiceTypeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.one_word_light));
                break;
            case TRUE_FALSE:
                holder.practiceTypeIcon.setScaleY(1.1f);
                holder.practiceTypeIcon.setScaleX(1.1f);
                if (dark || isMicrolectureSelected)
                    holder.practiceTypeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.tnf_dark));
                else
                    holder.practiceTypeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.tnf_light));
                break;
            case MATCH_THE_COLUMN:
                if (dark || isMicrolectureSelected)
                    holder.practiceTypeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.mtc_dark));
                else
                    holder.practiceTypeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.mtc_light));
                break;
            case VERY_SHORT_ANSWERS:
                holder.practiceTypeIcon.setScaleY(1.2f);
                holder.practiceTypeIcon.setScaleX(1.2f);
                if (dark || isMicrolectureSelected)
                    holder.practiceTypeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.vs_dark));
                else
                    holder.practiceTypeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.vs_light));
                break;
            case ONE_LINER:
                holder.practiceTypeIcon.setScaleY(1.2f);
                holder.practiceTypeIcon.setScaleX(1.2f);
                if (dark || isMicrolectureSelected)
                    holder.practiceTypeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.vs_dark));
                else
                    holder.practiceTypeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.vs_light));
                break;
            case SHORT_ANSWERS:
                holder.practiceTypeIcon.setScaleY(1.3f);
                holder.practiceTypeIcon.setScaleX(1.3f);
                if (dark || isMicrolectureSelected)
                    holder.practiceTypeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.s_dark));
                else
                    holder.practiceTypeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.s_light));
                break;
            case LONG_ANSWERS, ESSAY:
                if (dark || isMicrolectureSelected)
                    holder.practiceTypeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.l_dark));
                else
                    holder.practiceTypeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.l_light));
                break;
//            case NOTES:
//                if (dark || isMicrolectureSelected)
//                    holder.practiceTypeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.notes_dark));
//                else
//                    holder.practiceTypeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.notes_light));
//                break;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class PracticeViewHolder extends RecyclerView.ViewHolder {
        LinearLayout topDash;
        LinearLayout bottomDash;
        CardView dotCard;
        TextView practiceType;
        ImageView practiceTypeIcon;
        LinearLayout parentLayout;

        public PracticeViewHolder(@NonNull View itemView) {
            super(itemView);
            topDash = itemView.findViewById(R.id.top_dash);
            bottomDash = itemView.findViewById(R.id.bottom_dash);
            dotCard = itemView.findViewById(R.id.dot_card);
            practiceType = itemView.findViewById(R.id.practice_type);
            practiceTypeIcon = itemView.findViewById(R.id.practice_type_icon);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }

    public interface PracticeListener {
        void setUpPreviewQuestion(String type, int position, boolean locked, int questionPos);

        void onPracticeClick(String type, int position, boolean locked, int questionPos);
    }
}
