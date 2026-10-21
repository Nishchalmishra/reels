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
import arivihan.technologies.doubtbuzzter2.model.NeetClassOption;

/**
 * Adapter for NEET class selection list.
 * Identical selection pattern to GradeAdapter and StreamsAdapter:
 * - Reads isSelected() from model
 * - Delegates click to fragment via OnSelectionClickListener
 * - Fragment updates model state + calls notifyDataSetChanged()
 */
public class NeetClassOptionAdapter extends RecyclerView.Adapter<NeetClassOptionAdapter.ViewHolder> {

    private final Context context;
    private final List<NeetClassOption> options;
    private final OnSelectionClickListener listener;

    public NeetClassOptionAdapter(Context context, List<NeetClassOption> options,
                                  OnSelectionClickListener listener) {
        this.context = context;
        this.options = options;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_neet_class_option, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NeetClassOption option = options.get(position);
        holder.subjectTitle.setText(option.getLabel());

        // Selection state — same as GradeAdapter/StreamsAdapter
        if (option.isSelected()) {
            holder.selectionCardLayout.setCardBackgroundColor(context.getColor(R.color.subscription_blue));
            holder.subjectTitle.setTextColor(context.getColor(R.color.white));
            holder.selectionCard.setStrokeWidth(0);
            holder.selectionAnim.setVisibility(View.VISIBLE);
            holder.selectionAnim.playAnimation();
        } else {
            holder.selectionCardLayout.setCardBackgroundColor(context.getColor(R.color.white));
            holder.subjectTitle.setTextColor(context.getColor(R.color.medium_text_color));
            holder.selectionCard.setCardBackgroundColor(context.getColor(R.color.white));
            holder.selectionCard.setStrokeWidth(2);
            holder.selectionAnim.setVisibility(View.GONE);
        }

        holder.selectionCardLayout.setOnClickListener(view -> {
            listener.onSelectionChange(option, position);
            startButtonPressAnimation(view);
        });
    }

    // Same press animation as GradeAdapter/StreamsAdapter
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
        return options.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView subjectTitle;
        MaterialCardView selectionCard;
        LottieAnimationView selectionAnim;
        CardView selectionCardLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectTitle = itemView.findViewById(R.id.tv_option_label);
            selectionCard = itemView.findViewById(R.id.selection_card);
            selectionAnim = itemView.findViewById(R.id.selection_anim);
            selectionCardLayout = itemView.findViewById(R.id.selection_card_layout);
        }
    }

    public interface OnSelectionClickListener {
        void onSelectionChange(NeetClassOption option, int position);
    }
}
