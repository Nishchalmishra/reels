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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.AddSubjectAboutFragmentModel;
import arivihan.technologies.doubtbuzzter2.model.OnboardingSubjectEnum;
import arivihan.technologies.doubtbuzzter2.model.SubjectEnum;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectSelectionViewHolder> {

    private final Context context;
    private final List<AddSubjectAboutFragmentModel> boardList;
    private final OnSelectionClickListener onSelectionClickListener;

    public SubjectAdapter(Context context, List<AddSubjectAboutFragmentModel> boardList,
                          OnSelectionClickListener onSelectionClickListener) {
        this.context = context;
        this.boardList = boardList;
        this.onSelectionClickListener = onSelectionClickListener;
    }

    @NonNull
    @Override
    public SubjectSelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.subject_item, parent, false);
        return new SubjectSelectionViewHolder(view);
    }

    @SuppressLint("CheckResult")
    @Override
    public void onBindViewHolder(@NonNull SubjectSelectionViewHolder holder, int position) {
        AddSubjectAboutFragmentModel model = boardList.get(position);
        OnboardingSubjectEnum subject = OnboardingSubjectEnum.fromLocalizedName(model.getCode(), context);


        holder.subject_text.setText(model.getName());

        if (model.isChecked()) {
            holder.selection_card_layout.setCardBackgroundColor(context.getColor(R.color.subscription_blue));
            holder.subject_text.setTextColor(context.getColor(R.color.white));
            holder.selection_card.setStrokeWidth(0);
            holder.selection_anim.setVisibility(View.VISIBLE);
            holder.selection_anim.playAnimation();
        } else {
            holder.selection_card_layout.setCardBackgroundColor(context.getColor(R.color.white));
            holder.subject_text.setTextColor(context.getColor(R.color.medium_text_color));
            holder.selection_card.setStrokeWidth(2);
            holder.selection_anim.setVisibility(View.GONE);
        }

        Glide.with(context)
                .load(AppCompatResources.getDrawable(context, subject.getIconResId(model.isChecked())))
                .into(holder.subject_icon);

        holder.selection_card_layout.setOnClickListener(view -> {
            if (model.additional) {
                onSelectionClickListener.onSelectionChange(model, position);
                startButtonPressAnimation(view);
            } else {
                Toast.makeText(context,
                        context.getString(R.string.Aap_sirf_ek_naya_subject_add_kar_sakte_hain),
                        Toast.LENGTH_LONG).show();
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

    @Override
    public int getItemCount() {
        return boardList.size();
    }

    public static class SubjectSelectionViewHolder extends RecyclerView.ViewHolder {
        ImageView subject_icon;
        TextView subject_text;
        MaterialCardView selection_card;
        LottieAnimationView selection_anim;
        CardView selection_card_layout;

        public SubjectSelectionViewHolder(@NonNull View itemView) {
            super(itemView);
            subject_icon = itemView.findViewById(R.id.subject_icon);
            subject_text = itemView.findViewById(R.id.subject_text);
            selection_card = itemView.findViewById(R.id.selection_card);
            selection_anim = itemView.findViewById(R.id.selection_anim);
            selection_card_layout = itemView.findViewById(R.id.selection_card_layout);
        }
    }

    public interface OnSelectionClickListener {
        void onSelectionChange(AddSubjectAboutFragmentModel aboutFragmentModel, int pos);
    }
}