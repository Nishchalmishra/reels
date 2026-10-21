package arivihan.technologies.doubtbuzzter2.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.facebook.shimmer.Shimmer;
import com.facebook.shimmer.ShimmerDrawable;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.subject.SubjectResponse;

public class SubjectNameAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SUBJECT = 0;
    private static final int VIEW_TYPE_ADD_CARD = 1;

    private List<SubjectResponse> sub;
    OnSubjectClick onSubjectClick;
    OnAddSubjectClick onAddSubjectClick;
    Context context;
    private boolean showAddCard = false;

    public SubjectNameAdapter(List<SubjectResponse> sub, OnSubjectClick onSubjectClick, Context context) {
        this.sub = sub;
        this.onSubjectClick = onSubjectClick;
        this.context = context;
    }

    public void setOnAddSubjectClick(OnAddSubjectClick listener) {
        this.onAddSubjectClick = listener;
    }

    public void setShowAddCard(boolean show) {
        if (this.showAddCard != show) {
            this.showAddCard = show;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (showAddCard && position == sub.size()) {
            return VIEW_TYPE_ADD_CARD;
        }
        return VIEW_TYPE_SUBJECT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_ADD_CARD) {
            View view = inflater.inflate(R.layout.item_add_optional_subject, parent, false);
            return new AddSubjectViewHolder(view);
        }
        View view = inflater.inflate(R.layout.subjects_layout, parent, false);
        return new SubjectNameViewHolder(view, onSubjectClick);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AddSubjectViewHolder) {
            ((AddSubjectViewHolder) holder).card.setOnClickListener(v -> {
                if (onAddSubjectClick != null) onAddSubjectClick.onAddSubjectClick();
            });
            return;
        }

        if (position < 0 || position >= sub.size()) {
            return;
        }
        SubjectNameViewHolder subjectHolder = (SubjectNameViewHolder) holder;
        SubjectResponse sm = sub.get(position);
        Shimmer shimmer = new Shimmer.ColorHighlightBuilder()
                .setBaseColor(ContextCompat.getColor(context, R.color.shimmer_base_color))
                .setBaseAlpha(1)
                .setHighlightColor(ContextCompat.getColor(context, R.color.shimmer_highlight_color))
                .setHighlightAlpha(1)
                .setDropoff(50)
                .build();

        ShimmerDrawable shimmerDrawable = new ShimmerDrawable();
        shimmerDrawable.setShimmer(shimmer);

        subjectHolder.chapters.setText(sm.getChapterCount() + " chapters");
        subjectHolder.subject.setText(sm.getSubjectName());
        Glide.with(context).load(sm.getIconUrl()).into(subjectHolder.image);

        if (sm.isUnwatched()) {
            subjectHolder.badge.setVisibility(View.VISIBLE);
        } else {
            subjectHolder.badge.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return sub.size() + (showAddCard ? 1 : 0);
    }

    public class SubjectNameViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView image;
        public TextView subject;
        public TextView chapters;
        public CardView card;
        public ImageView badge;
        OnSubjectClick onSubjectClick;

        public SubjectNameViewHolder(@NonNull View itemView, OnSubjectClick onSubjectClick) {
            super(itemView);

            image = (ImageView) itemView.findViewById(R.id.subject_icon);
            subject = (TextView) itemView.findViewById(R.id.subject_name);
            chapters = (TextView) itemView.findViewById(R.id.subject_chapters);
            card = (CardView) itemView.findViewById(R.id.card);
            badge = itemView.findViewById(R.id.badge);
            this.onSubjectClick = onSubjectClick;
            card.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onSubjectClick.onsubjectClick(getBindingAdapterPosition());
        }
    }

    public class AddSubjectViewHolder extends RecyclerView.ViewHolder {
        public CardView card;

        public AddSubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card_add_subject);
        }
    }

    public interface OnSubjectClick {
        void onsubjectClick(int position);
    }

    public interface OnAddSubjectClick {
        void onAddSubjectClick();
    }
}
