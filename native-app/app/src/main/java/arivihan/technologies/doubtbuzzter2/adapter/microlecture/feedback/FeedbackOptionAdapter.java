package arivihan.technologies.doubtbuzzter2.adapter.microlecture.feedback;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.ChapterListAdapter;
import arivihan.technologies.doubtbuzzter2.listener.ChapterListActivityListener;
import arivihan.technologies.doubtbuzzter2.model.ChapterListInfoDto;
import arivihan.technologies.doubtbuzzter2.model.microlecture.FeedbackOption;

public class FeedbackOptionAdapter extends RecyclerView.Adapter<FeedbackOptionAdapter.ViewHolder> {

    private final Context context;
    private List<FeedbackOption> list;
    private OnFeedbackOptionClickListener listener;

    public FeedbackOptionAdapter(Context context, List<FeedbackOption> list,
                                 OnFeedbackOptionClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener  = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feedback_option, parent, false);
        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FeedbackOption feedbackOption = list.get(position);

        Glide.with(context)
                .load(feedbackOption.getIcon())
                .placeholder(R.drawable.img_like_outline)
                .error(R.drawable.img_like_outline)
                .into(holder.iv_icon);

        holder.tv_message.setText(feedbackOption.getMessage());

        holder.cv_parent.setOnClickListener(cardView->{

            feedbackOption.setSelected(!feedbackOption.isSelected());
            if(feedbackOption.isSelected()) {
                holder.cv_parent.setCardBackgroundColor(ContextCompat.getColor(cardView.getContext(), R.color.selected));
                holder.cv_parent.setStrokeWidth(0);
                holder.tv_message.setTextColor(ContextCompat.getColor(cardView.getContext(), R.color.arivihan_secondary_color));
                holder.cv_parent.setCardElevation(0.0f);
            } else {
                holder.cv_parent.setCardBackgroundColor(ContextCompat.getColor(cardView.getContext(), R.color.white));
                holder.cv_parent.setStrokeWidth(1);
                holder.cv_parent.setStrokeColor(ContextCompat.getColor(cardView.getContext(), R.color.gray_e5));
                holder.tv_message.setTextColor(ContextCompat.getColor(cardView.getContext(), R.color.black));
                holder.cv_parent.setCardElevation(2.0f);
            }
            listener.onFeedbackOptionClick(feedbackOption);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cv_parent;
        private final ImageView iv_icon;
        private final TextView tv_message;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cv_parent = itemView.findViewById(R.id.cv_parent);
            iv_icon = itemView.findViewById(R.id.iv_icon);
            tv_message = itemView.findViewById(R.id.tv_message);
        }
    }

    public interface OnFeedbackOptionClickListener {
        void onFeedbackOptionClick(FeedbackOption feedbackOption);
    }
}


