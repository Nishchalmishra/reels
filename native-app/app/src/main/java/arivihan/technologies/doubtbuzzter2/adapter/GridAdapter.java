package arivihan.technologies.doubtbuzzter2.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.listener.GridListener;
import arivihan.technologies.doubtbuzzter2.model.TestSeriesQuestionDto;

public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {

    private List<TestSeriesQuestionDto> list;
    private GridListener listener;

    private Boolean isReviewMode = false;

    public GridAdapter(List<TestSeriesQuestionDto> list, GridListener listener, Boolean isReviewMode) {
        this.list = list;
        this.listener = listener;
        this.isReviewMode = isReviewMode;
    }

    @NonNull
    @Override
    public GridAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.grid_single_item, parent, false);
        return new GridAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GridAdapter.ViewHolder holder, int position) {
        holder.text.setText(list.get(position).getTitle());
        if (isReviewMode) {
            if (list.get(position).getUserAnswer() != null) {
                holder.relativeLayout.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.grid_selected_bg));

            } else {
                holder.relativeLayout.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.grid_disabled_bg));
            }
        } else {
            switch (list.get(position).getAnswerState()) {
                case ANSWERED:
                    holder.relativeLayout.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.grid_selected_bg));
                    break;
                case ANSWERED_MARK_FOR_REVIEW:
                    holder.relativeLayout.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.mock_mark));
                    break;
                case UNANSWERED_MARK_FOR_REVIEW:
                    holder.relativeLayout.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.grid_wrong_bg));
                    break;
                default:
                    holder.relativeLayout.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.grid_disabled_bg));
                    break;
            }

        }
        holder.relativeLayout.setOnClickListener(v -> listener.clicked(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final RelativeLayout relativeLayout;
        private final TextView text;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            relativeLayout = itemView.findViewById(R.id.grid_item_layout);
            text = itemView.findViewById(R.id.grid_item_text);
        }
    }
}
