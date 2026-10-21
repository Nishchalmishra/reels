package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.List;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.OnBoardingSelectionModel;

public class CourseSelectionAdapter extends RecyclerView.Adapter<CourseSelectionAdapter.ViewHolder> {

    private final List<OnBoardingSelectionModel> examList;
    private final CourseSelectionListener listener;
    private final Context context;
    private RecyclerView recyclerView; // ← ADDED

    public CourseSelectionAdapter(Context context, List<OnBoardingSelectionModel> examList, CourseSelectionListener listener) {
        this.context = context;
        this.examList = examList;
        this.listener = listener;
    }

    // ← ADDED
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_exam_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OnBoardingSelectionModel model = examList.get(position);
        holder.examTitle.setText(model.getName());

        holder.examCard.setBackground(
                ContextCompat.getDrawable(context, R.drawable.card_outer_glow)
        );

        // fullWidth items (e.g. NEET Dropper) are 60% width and centered.
        // Regular items fill their grid cell with default 10dp horizontal margin.
        RecyclerView.LayoutParams rootParams = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
        rootParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        rootParams.leftMargin = 0;
        rootParams.rightMargin = 0;
        holder.itemView.setLayoutParams(rootParams);

        ViewGroup.MarginLayoutParams cardParams =
                (ViewGroup.MarginLayoutParams) holder.examCard.getLayoutParams();
        if (model.isFullWidth()) {
            int screenWidth = holder.itemView.getResources().getDisplayMetrics().widthPixels;
            int horizontalMargin = screenWidth / 5; // 20% each side → 60% card
            cardParams.setMargins(horizontalMargin, cardParams.topMargin,
                    horizontalMargin, cardParams.bottomMargin);
        } else {
            int defaultMargin = (int) (holder.itemView.getResources()
                    .getDisplayMetrics().density * 10); // 10dp
            cardParams.setMargins(defaultMargin, cardParams.topMargin,
                    defaultMargin, cardParams.bottomMargin);
        }
        holder.examCard.setLayoutParams(cardParams);

        holder.examCard.setOnClickListener(v -> {
            Log.d("CourseSelectionAdapter", "Clicked exam id: " + model.getId());

            holder.examCard.setBackground(
                    ContextCompat.getDrawable(context, R.drawable.card_selected)
            );
            holder.examTitle.setTextColor(ContextCompat.getColor(context, R.color.white));

            v.postDelayed(() -> {
                // ✅ Restore glow background after delay
                holder.examCard.setBackground(
                        ContextCompat.getDrawable(context, R.drawable.card_outer_glow)
                );
                holder.examTitle.setTextColor(ContextCompat.getColor(context, R.color.arivihan_secondary_color));
                if (listener != null) listener.onExamSelected(model);
            }, 200);
        });
    }
    @Override
    public int getItemCount() {
        return examList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView examCard;
        TextView examTitle;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            examCard = itemView.findViewById(R.id.exam_card);
            examTitle = itemView.findViewById(R.id.exam_selection_item_title);
        }
    }

    public interface CourseSelectionListener {
        void onExamSelected(OnBoardingSelectionModel onBoardingSelectionModel);
    }
}