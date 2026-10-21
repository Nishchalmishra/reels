package arivihan.technologies.doubtbuzzter2.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.enums.DoubtOptionType;
import arivihan.technologies.doubtbuzzter2.model.DoubtOption;

public class DoubtOptionsAdapter extends RecyclerView.Adapter<DoubtOptionsAdapter.ViewHolder> {

    private List<DoubtOption> optionList;
    private OnOptionClickListener listener;

    public interface OnOptionClickListener {
        void onOptionClicked(String optionType);
    }

    public DoubtOptionsAdapter(List<DoubtOption> optionList, OnOptionClickListener listener) {
        this.optionList = optionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.option_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DoubtOption doubtOption = optionList.get(position);

        holder.title.setText(doubtOption.getTitle());
        holder.icon.setImageResource(doubtOption.getDoubtOptionType().getIconResId());

        holder.itemView.setOnClickListener(v ->
                listener.onOptionClicked(doubtOption.getDoubtOptionType().toString())
        );
    }

    @Override
    public int getItemCount() {
        return optionList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView icon;
        TextView title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            icon = itemView.findViewById(R.id.optionIcon);
            title = itemView.findViewById(R.id.optionTitle);
        }
    }
}