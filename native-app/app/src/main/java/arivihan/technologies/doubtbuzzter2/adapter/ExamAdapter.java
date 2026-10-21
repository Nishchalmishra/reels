package arivihan.technologies.doubtbuzzter2.adapter;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.ExamEnum;
import arivihan.technologies.doubtbuzzter2.model.NameIdModel;

public class ExamAdapter extends RecyclerView.Adapter<ExamAdapter.ExamViewHolder> {

    private ArrayList<NameIdModel> exams;
    private Context context;
    private String selectedCourseId;
    private final ExamClickListener listener;

    public ExamAdapter(Context context, ArrayList<NameIdModel> exams, String selectedCourseId, ExamClickListener listener) {
        this.context = context;
        this.exams = exams;
        this.selectedCourseId = selectedCourseId;
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateExams(ArrayList<NameIdModel> newExams, String newSelectedCourseId) {
        this.exams = newExams;
        this.selectedCourseId = newSelectedCourseId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.exam_item_layout, parent, false);
        return new ExamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamViewHolder holder, int position) {
        NameIdModel exam = exams.get(position);
        boolean isSelected = exam.getId().equals(selectedCourseId);
        holder.bind(holder, exam, isSelected);
    }

    @Override
    public int getItemCount() {
        return exams != null ? exams.size() : 0;
    }

    class ExamViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView examName;
        private final ImageView examIcon;
        private final LottieAnimationView selectionAnimation;
        private final CardView selectionLayout;

        public ExamViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.selection_card);
            selectionLayout = itemView.findViewById(R.id.selection_card_layout);
            examName = itemView.findViewById(R.id.exam_text);
            examIcon = itemView.findViewById(R.id.exam_icon);
            selectionAnimation = itemView.findViewById(R.id.selection_anim);

            selectionLayout.setOnClickListener(v -> {
                try {
//                    int position = getBindingAdapterPosition();
//                    if (position != RecyclerView.NO_POSITION) {
//                        selectedCourseId = exams.get(position).getId();
//                        listener.onExamClick(exams.get(position), position);
//                        startButtonPressAnimation(v);
//                        notifyDataSetChanged();
//                    }
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && position < exams.size()) {
                        selectedCourseId = exams.get(position).getId();
                        listener.onExamClick(exams.get(position), position);
                        startButtonPressAnimation(v);
                        notifyDataSetChanged();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }

        private void startButtonPressAnimation(View view) {
            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, View.SCALE_X, 0.9f);
            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 0.9f);
            ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, View.SCALE_X, 1f);
            ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f);

            scaleDownX.setDuration(100);
            scaleDownY.setDuration(100);
            scaleUpX.setDuration(100);
            scaleUpY.setDuration(100);

            AnimatorSet scaleDown = new AnimatorSet();
            scaleDown.playTogether(scaleDownX, scaleDownY);

            AnimatorSet scaleUp = new AnimatorSet();
            scaleUp.playTogether(scaleUpX, scaleUpY);

            AnimatorSet keypressEffect = new AnimatorSet();
            keypressEffect.playSequentially(scaleDown, scaleUp);
            keypressEffect.start();
        }

        public void bind(ExamViewHolder holder, NameIdModel exam, boolean isSelected) {
            Log.d("ExamAdapter", "Binding exam: " + exam.getName() + ", isSelected: " + isSelected);
            examName.setText(exam.getName());

            ExamEnum examEnum = ExamEnum.fromLocalizedName(exam.getId());
            if (examEnum != null) {
                Glide.with(context)
                        .load(AppCompatResources.getDrawable(context, examEnum.getIconResId(isSelected)))
                        .into(holder.examIcon);
            }

            if (isSelected) {
                selectionLayout.setCardBackgroundColor(itemView.getContext().getColor(R.color.subscription_blue));
                examName.setTextColor(itemView.getContext().getColor(R.color.white));
                selectionAnimation.setVisibility(View.VISIBLE);
                selectionAnimation.playAnimation();
                cardView.setStrokeWidth(0);
            } else {
                selectionLayout.setCardBackgroundColor(itemView.getContext().getColor(R.color.white));
                examName.setTextColor(itemView.getContext().getColor(R.color.medium_text_color));
                selectionAnimation.setVisibility(View.GONE);
                cardView.setStrokeWidth(2);
            }
        }
    }

    public interface ExamClickListener {
        void onExamClick(NameIdModel nameIdModel, int position);
    }
}