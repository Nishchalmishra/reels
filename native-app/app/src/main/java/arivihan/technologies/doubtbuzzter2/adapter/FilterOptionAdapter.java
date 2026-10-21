package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.DoubtFilterModel;
import lombok.Setter;

public class FilterOptionAdapter extends RecyclerView.Adapter<FilterOptionAdapter.FilterOptionViewHolder> {

    Context context;
    List<DoubtFilterModel> list;
    FilterListener filterListener;
    int parentPos;
    @Setter
    static boolean clearFilterApplied;

    public FilterOptionAdapter(Context context, List<DoubtFilterModel> list, FilterListener filterListener) {
        this.context = context;
        this.list = list;
        this.filterListener = filterListener;
    }

    @NonNull
    @Override
    public FilterOptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        parentPos = (int) parent.getTag();
        View view = LayoutInflater.from(context).inflate(R.layout.filter_option_layout, parent, false);
        return new FilterOptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilterOptionViewHolder holder, int position) {
        holder.option.setText(list.get(position).getTitle());
        if (position == list.size() - 1) {
            holder.divider.setVisibility(View.GONE);
        } else {
            holder.divider.setVisibility(View.VISIBLE);
        }

        if (list.get(position).getState() == 1)
            holder.radioButton.setChecked(true);
        else
            holder.radioButton.setChecked(false);

        holder.radioButton.setFocusable(false);
        holder.radioButton.setFocusableInTouchMode(false);
        holder.radioButton.setClickable(false);
        holder.option_layout.setOnClickListener(v -> {
            holder.radioButton.setChecked(!holder.radioButton.isChecked());
            filterListener.onOptionClick(holder.radioButton.isChecked(), list.get(position).getType(), list.get(position).getTitle(), list, position, parentPos);
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class FilterOptionViewHolder extends RecyclerView.ViewHolder {
        RadioButton radioButton;
        TextView option;
        LinearLayout divider, option_layout;

        public FilterOptionViewHolder(@NonNull View itemView) {
            super(itemView);
            radioButton = itemView.findViewById(R.id.radio_button);
            option = itemView.findViewById(R.id.option);
            divider = itemView.findViewById(R.id.divider);
            option_layout = itemView.findViewById(R.id.option_layout);
        }
    }

    public interface FilterListener {
        public void onOptionClick(boolean checked, String type, String title, List<DoubtFilterModel> list, int pos, int parentPos);
    }
}