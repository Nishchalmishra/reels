package arivihan.technologies.doubtbuzzter2.adapter.competitive;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import arivihan.technologies.doubtbuzzter2.R;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.customView.gradient.AnimatedBorderView;
import arivihan.technologies.doubtbuzzter2.enums.AllFeatureSectionBadgeType;
import arivihan.technologies.doubtbuzzter2.model.competitive.HomeScreenCardResponse;

public class FeaturesSectionAdapter extends RecyclerView.Adapter<FeaturesSectionAdapter.FeatureViewHolder> {

    private final Context context;
    private final List<HomeScreenCardResponse> featureList;
    private final OnFeatureClickListener listener;
    private String animatedItemTag = null;

    public interface OnFeatureClickListener {
        void onFeatureClick(HomeScreenCardResponse item);
    }

    public FeaturesSectionAdapter(Context context, List<HomeScreenCardResponse> featureList,
                                  OnFeatureClickListener listener) {
        this.context = context;
        this.featureList = featureList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FeatureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_feature_section, parent, false);
        return new FeatureViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeatureViewHolder holder, int position) {
        HomeScreenCardResponse item = featureList.get(position);

        holder.ivFeatureIcon.setVisibility(View.GONE);
        holder.featureProgressIndicator.setVisibility(View.GONE);
        holder.featureProgressPercentage.setVisibility(View.GONE);

        // Set title
        holder.tvFeatureTitle.setText(item.getTitle());

        String mediaType = item.getIconMediaType();
        if (mediaType == null) {
            mediaType = AllFeatureSectionBadgeType.IMAGE.name();
        }

        if (AllFeatureSectionBadgeType.DYNAMIC_MEDIA.name()
                .equalsIgnoreCase(mediaType)) {
            holder.ivFeatureIcon.setVisibility(View.GONE);
            holder.featureProgressIndicator.setVisibility(View.VISIBLE);
            holder.featureProgressPercentage.setVisibility(View.VISIBLE);
            long progress = item.getProgress() != null ? item.getProgress() : 0;

            holder.featureProgressIndicator.setProgress(0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                holder.featureProgressIndicator.setProgress((int) progress, true);
            }
            holder.featureProgressPercentage.setText(progress + "%");
        } else {

            holder.ivFeatureIcon.setVisibility(View.VISIBLE);
            holder.featureProgressIndicator.setVisibility(View.GONE);
            holder.featureProgressPercentage.setVisibility(View.GONE);

            if (AllFeatureSectionBadgeType.GIF.name().equalsIgnoreCase(mediaType)) {
                holder.ivFeatureIcon.setScaleX(1.3f);
                holder.ivFeatureIcon.setScaleY(1.3f);
                Glide.with(context)
                        .asGif()
                        .load(item.getIcon())
                        .error(R.drawable.white_logo)
                        .into(holder.ivFeatureIcon);
            } else {
                Glide.with(context)
                        .load(item.getIcon())
                        .error(R.drawable.white_logo)
                        .into(holder.ivFeatureIcon);
            }
        }

        if (StringUtils.isNotEmpty(item.getIconBackgroundColor())) {
            try {
                holder.cvFeatureIconContainer.setCardBackgroundColor(
                        Color.parseColor(item.getIconBackgroundColor()));
            } catch (IllegalArgumentException e) {
                holder.cvFeatureIconContainer.setCardBackgroundColor(Color.LTGRAY);
            }
        }

        if (StringUtils.isNotEmpty(item.getTag())) {
            holder.cvFeatureBadge.setVisibility(View.VISIBLE);
            holder.tvFeatureBadgeText.setText(item.getTag());

            if (StringUtils.isNotEmpty(item.getTagBackgroundColor())) {
                try {
                    holder.cvFeatureBadge.setCardBackgroundColor(Color.parseColor(item.getTagBackgroundColor()));
                } catch (IllegalArgumentException e) {
                    holder.cvFeatureBadge.setCardBackgroundColor(ContextCompat.getColor(context, R.color.feature_badge_yellow));
                }
            }
        } else {
            holder.cvFeatureBadge.setVisibility(View.GONE);
        }

        holder.parentLayout.setOnClickListener(v -> {
            if (listener != null) listener.onFeatureClick(item);
        });

        String featureTag = item.getFeatureTag();
        if (animatedItemTag != null && animatedItemTag.equals(featureTag)) {
            holder.borderAnimation.playAnimation(0);
        } else {
            holder.borderAnimation.stopAnimation();
            holder.borderAnimation.setVisibility(View.GONE);
        }
    }

    @Override
    public void onViewRecycled(@NonNull FeatureViewHolder holder) {
        super.onViewRecycled(holder);
        holder.borderAnimation.stopAnimation();
        holder.borderAnimation.setVisibility(View.GONE);
    }

    // --- Animation control API ---
    public void setAnimatedItemTag(String tag) {
        if (StringUtils.equals(animatedItemTag, tag)) return;
        String oldTag = animatedItemTag;
        animatedItemTag = tag;
        // Notify old and new animated items
        for (int i = 0; i < featureList.size(); i++) {
            String ft = featureList.get(i).getFeatureTag();
            if (StringUtils.equals(ft, oldTag) || StringUtils.equals(ft, tag)) {
                notifyItemChanged(i);
            }
        }
    }

    public String getAnimatedItemTag() {
        return animatedItemTag;
    }

    public void clearAnimatedTag() {
        String oldTag = animatedItemTag;
        animatedItemTag = null;
        if (oldTag != null) {
            for (int i = 0; i < featureList.size(); i++) {
                if (oldTag.equals(featureList.get(i).getFeatureTag())) {
                    notifyItemChanged(i);
                    break;
                }
            }
        }
    }

    /** Call in onPause — directly stops the animator on the currently visible ViewHolder. */
    public void stopCurrentAnimationIfBound(RecyclerView recyclerView) {
        if (animatedItemTag == null) return;
        for (int i = 0; i < featureList.size(); i++) {
            if (animatedItemTag.equals(featureList.get(i).getFeatureTag())) {
                RecyclerView.ViewHolder vh = recyclerView.findViewHolderForAdapterPosition(i);
                if (vh instanceof FeatureViewHolder) {
                    FeatureViewHolder fvh = (FeatureViewHolder) vh;
                    fvh.borderAnimation.stopAnimation();
                    fvh.borderAnimation.setVisibility(View.GONE);
                }
                break;
            }
        }
    }

    /** Call in onResume — restarts the animation on the current animated item. */
    public void refreshAnimatedTag(RecyclerView recyclerView) {
        if (animatedItemTag == null) return;
        for (int i = 0; i < featureList.size(); i++) {
            if (animatedItemTag.equals(featureList.get(i).getFeatureTag())) {
                // Directly restart on the visible ViewHolder (no layout pass needed)
                if (recyclerView != null) {
                    RecyclerView.ViewHolder vh = recyclerView.findViewHolderForAdapterPosition(i);
                    if (vh instanceof FeatureViewHolder) {
                        ((FeatureViewHolder) vh).borderAnimation.playAnimation(0);
                        return;
                    }
                }
                // Fallback: ViewHolder not bound yet, trigger a rebind
                notifyItemChanged(i);
                break;
            }
        }
    }

    /** @deprecated Use {@link #refreshAnimatedTag(RecyclerView)} for immediate restart. */
    @Deprecated
    public void refreshAnimatedTag() {
        refreshAnimatedTag(null);
    }

    @Override
    public int getItemCount() {
        return featureList.size();
    }

    public static class FeatureViewHolder extends RecyclerView.ViewHolder {
        CardView cvFeatureBadge, cvFeatureItem;
        ImageView ivFeatureIcon;
        TextView tvFeatureTitle, tvFeatureBadgeText;
        ConstraintLayout parentLayout;
        CircularProgressIndicator featureProgressIndicator;
        TextView featureProgressPercentage;
        CardView cvFeatureIconContainer;
        AnimatedBorderView borderAnimation;

        public FeatureViewHolder(@NonNull View itemView) {
            super(itemView);
            parentLayout = itemView.findViewById(R.id.Parent_Layout);
            cvFeatureBadge = itemView.findViewById(R.id.cv_feature_badge);
            cvFeatureItem = itemView.findViewById(R.id.cv_feature_item);
            ivFeatureIcon = itemView.findViewById(R.id.ivFeatureIcon);
            tvFeatureTitle = itemView.findViewById(R.id.tvFeatureTitle);
            tvFeatureBadgeText = itemView.findViewById(R.id.tv_featureBadge_text);
            borderAnimation = itemView.findViewById(R.id.border_animation);

            featureProgressIndicator = itemView.findViewById(R.id.feature_progress_indicator);
            featureProgressPercentage = itemView.findViewById(R.id.feature_progress_percentage);
            cvFeatureIconContainer = itemView.findViewById(R.id.cv_feature_icon_container);
        }
    }
}
