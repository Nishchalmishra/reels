package arivihan.technologies.doubtbuzzter2.adapter;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.batch.grade.GradeModel;

public class GradeAdapter extends RecyclerView.Adapter<GradeAdapter.ClassSelectionViewHolder> {

    private Context context;
    private List<GradeModel> gradeModels;
    private OnSelectionClickListener onSelectionClickListener;

    public GradeAdapter(Context context, List<GradeModel> gradeModels, OnSelectionClickListener onSelectionClickListener) {
        this.context = context;
        this.gradeModels = gradeModels;
        this.onSelectionClickListener = onSelectionClickListener;
    }

    @NonNull
    @Override
    public ClassSelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.class_item, parent, false);
        return new ClassSelectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassSelectionViewHolder holder, int position) {
        GradeModel gradeModel = gradeModels.get(position);
        holder.subject_title.setText(gradeModel.getDisplayName() != null ? gradeModel.getDisplayName() : gradeModel.getName());
        if (gradeModel.getSelected()) {
            holder.selection_card_layout.setCardBackgroundColor(context.getColor(R.color.subscription_blue));
            holder.subject_title.setTextColor(context.getColor(R.color.white));
            holder.selection_card.setStrokeWidth(0);
            holder.selection_anim.setVisibility(View.VISIBLE);
            holder.selection_anim.playAnimation();
        } else {
            holder.selection_card_layout.setCardBackgroundColor(context.getColor(R.color.white));
            holder.subject_title.setTextColor(context.getColor(R.color.medium_text_color));
            holder.selection_card.setCardBackgroundColor(context.getColor(R.color.white));
            holder.selection_card.setStrokeWidth(2);
            holder.selection_anim.setVisibility(View.GONE);
        }

        holder.selection_card_layout.setOnClickListener(view -> {
            onSelectionClickListener.onSelectionChange(gradeModel, position);
            startButtonPressAnimation(view);
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

    @Override
    public int getItemCount() {
        return gradeModels.size();
    }

    public static class ClassSelectionViewHolder extends RecyclerView.ViewHolder {
        TextView subject_title;
        MaterialCardView selection_card;
        LottieAnimationView selection_anim;
        CardView selection_card_layout;

        public ClassSelectionViewHolder(@NonNull View itemView) {
            super(itemView);
            subject_title = itemView.findViewById(R.id.subject_title);
            selection_card = itemView.findViewById(R.id.selection_card);
            selection_anim = itemView.findViewById(R.id.selection_anim);
            selection_card_layout = itemView.findViewById(R.id.selection_card_layout);
        }
    }


    public interface OnSelectionClickListener {
        default void onSelectionChange(GradeModel gradeModel, int pos) {
        }

        ;
    }
}
