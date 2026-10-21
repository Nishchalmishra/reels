package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.subject.SubjectResponse;

public class OptionalSubjectAdapter extends RecyclerView.Adapter<OptionalSubjectAdapter.ViewHolder> {

    public interface OnSelectionChangedListener {
        void onSelectionChanged(boolean hasSelection);
    }

    public interface OnSelectionLimitExceededListener {
        void onLimitExceeded();
    }

    private final List<SubjectResponse> subjects;
    private final Set<String> selectedIds = new HashSet<>();
    private final Context context;
    private final int selectionLimit;
    private OnSelectionChangedListener selectionChangedListener;
    private OnSelectionLimitExceededListener selectionLimitExceededListener;

    /**
     * @param selectionLimit 1 = radio/single-select mode; >1 = checkbox/multi-select mode
     */
    public OptionalSubjectAdapter(List<SubjectResponse> subjects, Context context, int selectionLimit) {
        this.subjects = subjects;
        this.context = context;
        this.selectionLimit = selectionLimit;
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionChangedListener = listener;
    }

    public void setOnSelectionLimitExceededListener(OnSelectionLimitExceededListener listener) {
        this.selectionLimitExceededListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_optional_subject_dialog, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SubjectResponse subject = subjects.get(position);
        boolean isSelected = selectedIds.contains(subject.getId());
        boolean isSingleSelect = (selectionLimit == 1);

        holder.tvName.setText(subject.getSubjectName());
        Glide.with(context).load(subject.getIconUrl()).into(holder.ivIcon);

        holder.cv_optional_subject_icon.setCardBackgroundColor(isSelected ? context.getColor(R.color.teal_v2) : context.getColor(R.color.gray_f6));

        // Background
        holder.container.setBackground(ContextCompat.getDrawable(context,
                isSelected
                        ? R.drawable.border_optional_subject_selected
                        : R.drawable.border_optional_subject_unselected));

        // Show correct indicator
        if (isSingleSelect) {
            holder.ivRadio.setVisibility(View.VISIBLE);
            holder.ivCheckbox.setVisibility(View.GONE);
            holder.ivRadio.setImageResource(isSelected
                    ? R.drawable.img_radio_teal_checked
                    : R.drawable.img_radio_unchecked);
        } else {
            holder.ivRadio.setVisibility(View.GONE);
            holder.ivCheckbox.setVisibility(View.VISIBLE);
            holder.ivCheckbox.setImageResource(isSelected
                    ? R.drawable.img_checkbox_teal_selected
                    : R.drawable.img_checkbox_teal_unselected);
        }

        holder.container.setOnClickListener(v -> {
            String id = subject.getId();
            if (isSingleSelect) {
                // Single-select: deselect previous, select tapped
                if (!selectedIds.contains(id)) {
                    selectedIds.clear();
                    selectedIds.add(id);
                    notifyDataSetChanged();
                    notifySelectionChanged();
                }
            } else {
                // Multi-select up to selectionLimit
                if (selectedIds.contains(id)) {
                    selectedIds.remove(id);
                    notifyItemChanged(position);
                    notifySelectionChanged();
                } else if (selectedIds.size() < selectionLimit) {
                    selectedIds.add(id);
                    notifyItemChanged(position);
                    notifySelectionChanged();
                } else {
                    if (selectionLimitExceededListener != null) {
                        selectionLimitExceededListener.onLimitExceeded();
                    }
                }
            }
        });
    }

    private void notifySelectionChanged() {
        if (selectionChangedListener != null) {
            selectionChangedListener.onSelectionChanged(!selectedIds.isEmpty());
        }
    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }

    public List<String> getSelectedIds() {
        return new ArrayList<>(selectedIds);
    }

    public boolean hasSelection() {
        return !selectedIds.isEmpty();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout container;
        final ImageView ivIcon;
        final TextView tvName;
        final ImageView ivRadio;
        final ImageView ivCheckbox;
        final CardView cv_optional_subject_icon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container_optional_subject);
            ivIcon = itemView.findViewById(R.id.iv_optional_subject_icon);
            tvName = itemView.findViewById(R.id.tv_optional_subject_name);
            ivRadio = itemView.findViewById(R.id.iv_radio_indicator);
            ivCheckbox = itemView.findViewById(R.id.iv_checkbox_indicator);
            cv_optional_subject_icon = itemView.findViewById(R.id.cv_optional_subject_icon);
        }
    }
}
