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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.NameIdModel;

public class MediumAdapter extends RecyclerView.Adapter<MediumAdapter.ViewHolder> {

    private Context context;
    private ArrayList<NameIdModel> list;
    private OnSelectionClickListener listener;

    public interface OnSelectionClickListener {
        void onSelectionChange(NameIdModel item, int position);
    }

    public MediumAdapter(Context context, ArrayList<NameIdModel> list,
                         OnSelectionClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.class_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        NameIdModel item = list.get(position);
        holder.tvName.setText(item.getName());
        boolean isSelected = item.isSelected();
        if (isSelected) {
            holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.subscription_blue));
            holder.tvName.setTextColor(
                    ContextCompat.getColor(context, R.color.white));
            holder.selectionAnim.setVisibility(View.VISIBLE);
            holder.selection_card.setStrokeWidth(0);
            holder.selectionAnim.playAnimation();
        } else {
            holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.white));
            holder.tvName.setTextColor(
                    ContextCompat.getColor(context, R.color.medium_text_color));
            holder.selection_card.setStrokeWidth(2);
            holder.selectionAnim.setVisibility(View.GONE);
            holder.selectionAnim.cancelAnimation();
        }

        holder.itemView.setOnClickListener(v -> {
            startPressAnimation(holder.itemView);

            if (listener != null) {
                listener.onSelectionChange(item, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void startPressAnimation(View view) {
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

        AnimatorSet animation = new AnimatorSet();
        animation.playSequentially(scaleDown, scaleUp);
        animation.start();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName;
        CardView cardView;
        LottieAnimationView selectionAnim;
        MaterialCardView selection_card;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.subject_title);
            cardView = itemView.findViewById(R.id.selection_card_layout);
            selectionAnim = itemView.findViewById(R.id.selection_anim);
            selection_card = itemView.findViewById(R.id.selection_card);
        }
    }
}