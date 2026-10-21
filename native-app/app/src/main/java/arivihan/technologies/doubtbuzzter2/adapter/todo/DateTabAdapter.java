package arivihan.technologies.doubtbuzzter2.adapter.todo;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.model.TodoDateSelectionModel;

public class DateTabAdapter extends RecyclerView.Adapter<DateTabAdapter.DateViewHolder> {

    private Context context;
    private final List<TodoDateSelectionModel> dateList;
    private int selectedPosition = 0;
    private final OnDateSelectedListener listener;
    private int itemHorizontalMarginPx, itemContentWidthPx, itemHeightPx;
    public interface OnDateSelectedListener {
        void onDateSelected(TodoDateSelectionModel todoDateSelectionModel, int position);
    }

    public DateTabAdapter(Context context, List<TodoDateSelectionModel> dateList, OnDateSelectedListener listener) {
        this.context = context;
        this.dateList = dateList;
        this.listener = listener;
//        this.selectedPosition = selectedPosition;

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int screenWidthPx = displayMetrics.widthPixels - CommonMethods.dpToPx(context, 8);

        final int itemsToShow = 7;

        int itemHorizontalMarginDp = 4;
        itemHorizontalMarginPx = CommonMethods.dpToPx(context, itemHorizontalMarginDp);

        int totalMarginSpacePx = 2 * itemsToShow * itemHorizontalMarginPx;

        int totalContentWidthForItems = screenWidthPx - totalMarginSpacePx;

        itemContentWidthPx = totalContentWidthForItems / itemsToShow;

        if (itemContentWidthPx <= 0) {
            itemContentWidthPx = CommonMethods.dpToPx(context, 40);
        }

        itemHeightPx = CommonMethods.dpToPx(context, 63);

    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_todo_tab_layout, parent, false);

        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(
                itemContentWidthPx,
                itemHeightPx
        );

        layoutParams.setMargins(
                itemHorizontalMarginPx,
                CommonMethods.dpToPx(parent.getContext(), 2),
                itemHorizontalMarginPx,
                CommonMethods.dpToPx(parent.getContext(), 2)
        );

        view.setLayoutParams(layoutParams);

        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        try {
            TodoDateSelectionModel date = dateList.get(position);

            String day = date.getDay();
            String dayOfMonth = String.valueOf(date.getDate());

            String dayText = date.getDayText();
            String dayCount = date.getDayCount();

            holder.tvDay.setText(StringUtils.isNotEmpty(dayText)?dayText:day);
            holder.tvDate.setText(StringUtils.isNotEmpty(dayCount)?dayCount:dayOfMonth);

            boolean isSelected = position == selectedPosition;

            if (isSelected) {
                holder.cv_parent.setCardBackgroundColor(context.getColor(R.color.arivihan_secondary_color));
                holder.tvDate.setTextColor(context.getColor(R.color.white));
                holder.tvDay.setTextColor(context.getColor(R.color.white));
                holder.tvDate.setTextColor(Color.WHITE);
                holder.tvDay.setTextColor(Color.WHITE);
            } else {
                holder.cv_parent.setCardBackgroundColor(context.getColor(R.color.white));
                holder.tvDate.setTextColor(context.getColor(R.color.black));
                holder.tvDay.setTextColor(context.getColor(R.color.black));
                holder.tvDate.setTextColor(Color.BLACK);
                holder.tvDay.setTextColor(Color.BLACK);
            }

            holder.cv_parent.setOnClickListener(v -> {
                int previous = selectedPosition;
                selectedPosition = holder.getAdapterPosition();
                notifyItemChanged(previous);
                notifyItemChanged(selectedPosition);
                listener.onDateSelected(date, selectedPosition);
            });

//            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
//                    holder.tvDate, 9, 18, 1, TypedValue.COMPLEX_UNIT_SP
//            );
//            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
//                    holder.tvDay, 9, 18, 1, TypedValue.COMPLEX_UNIT_SP
//            );

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return dateList.size();
    }

    public void selectDate(TodoDateSelectionModel selectedDate) {
        int previous = selectedPosition;
        for (int i = 0; i < dateList.size(); i++) {
            if (dateList.get(i).equals(selectedDate)) {
                selectedPosition = i;
                break;
            }
        }
        notifyItemChanged(previous);
        notifyItemChanged(selectedPosition);
    }

    public void selectDateByPosition(int position) {
        int previous = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(previous);
        notifyItemChanged(selectedPosition);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay, tvDate;
        CardView cv_parent;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tv_day);
            tvDate = itemView.findViewById(R.id.tv_date);
            cv_parent = itemView.findViewById(R.id.cv_parent);
        }
    }
}

