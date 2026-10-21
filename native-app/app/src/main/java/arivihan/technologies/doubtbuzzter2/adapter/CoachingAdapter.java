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
import arivihan.technologies.doubtbuzzter2.model.CoachingEnum;
import arivihan.technologies.doubtbuzzter2.model.NameIdModel;

public class CoachingAdapter extends RecyclerView.Adapter<CoachingAdapter.CoachingViewHolder> {

    private ArrayList<NameIdModel> coachingLists;
    private Context context;
    private String selectedCoachingId;
    private final CoachingClickListener listener;

    public CoachingAdapter(Context context, ArrayList<NameIdModel> coachingLists, String selectedCoachingId, CoachingClickListener listener) {
        this.context = context;
        this.coachingLists = coachingLists;
        this.selectedCoachingId = selectedCoachingId;
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateCoachings(ArrayList<NameIdModel> coachingLists, String selectedCoachingId) {
        this.coachingLists = coachingLists;
        this.selectedCoachingId = selectedCoachingId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CoachingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.exam_item_layout, parent, false);
        return new CoachingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CoachingViewHolder holder, int position) {
        NameIdModel exam = coachingLists.get(position);
        boolean isSelected = exam.getId().equals(selectedCoachingId);
        holder.bind(holder, exam, isSelected);
    }

    @Override
    public int getItemCount() {
        return coachingLists != null ? coachingLists.size() : 0;
    }

    class CoachingViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView examName;
        private final ImageView examIcon;
        private final LottieAnimationView selectionAnimation;
        private final CardView selectionLayout;

        public CoachingViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.selection_card);
            selectionLayout = itemView.findViewById(R.id.selection_card_layout);
            examName = itemView.findViewById(R.id.exam_text);
            examIcon = itemView.findViewById(R.id.exam_icon);
            selectionAnimation = itemView.findViewById(R.id.selection_anim);

            selectionLayout.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    selectedCoachingId = coachingLists.get(position).getId();
                    listener.onCoachingClick(coachingLists.get(position), position);
                    startButtonPressAnimation(v);
                    notifyDataSetChanged();
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

        public void bind(CoachingViewHolder holder, NameIdModel coaching, boolean isSelected) {
            Log.d("ExamAdapter", "Binding exam: " + coaching.getName() + ", isSelected: " + isSelected);
            //examName.setText(coaching.getName());


            CoachingEnum examEnum = CoachingEnum.fromLocalizedName(coaching.getId());
            if (examEnum != null) {
                Glide.with(context)
                        .load(AppCompatResources.getDrawable(context, examEnum.getIconResId(isSelected)))
                        .into(holder.examIcon);
                examName.setText(examEnum.getStringResource());
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

    public interface CoachingClickListener {
        void onCoachingClick(NameIdModel nameIdModel, int position);
    }
}