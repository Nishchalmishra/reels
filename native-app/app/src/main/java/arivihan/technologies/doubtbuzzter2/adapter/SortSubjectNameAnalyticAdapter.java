package arivihan.technologies.doubtbuzzter2.adapter;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;

public class SortSubjectNameAnalyticAdapter extends RecyclerView.Adapter<SortSubjectNameAnalyticAdapter.ViewHolder> {

    ArrayList<String> arr;
    ArrayList<String> Id;
    OnSubjectSortClick onSubjectSortClick;
    int selectedPos;

    public SortSubjectNameAnalyticAdapter(ArrayList<String> arr, ArrayList<String> Id,
                                          OnSubjectSortClick onSubjectSortClick, int selectedPos) {
        this.arr = arr;
        this.onSubjectSortClick = onSubjectSortClick;
        this.selectedPos = selectedPos;
        this.Id = Id;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.dialogue_sort_subjectname_analytics, parent, false);
        return new SortSubjectNameAnalyticAdapter.ViewHolder(view, onSubjectSortClick);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.sub.setText(arr.get(position));

        if (position == selectedPos) {
            holder.done.setVisibility(View.VISIBLE);
            holder.sub.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.rank_default));
            holder.selctedFrame.setVisibility(View.VISIBLE);
        } else {
            holder.done.setVisibility(View.GONE);
            holder.sub.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.rank_default));
            holder.selctedFrame.setVisibility(View.GONE);
        }

        holder.card.setOnClickListener(v -> {
            Log.i("ARIVIHANLOG ", "Position : " + position);
            onSubjectSortClick.onsubjectSortClick(Id.get(position), arr.get(position), position);
        });

    }

    @Override
    public int getItemCount() {
        return arr.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView sub;
        ImageView done;
        CardView card;
        FrameLayout selctedFrame;
        OnSubjectSortClick onSubjectSortClick;

        public ViewHolder(@NonNull View itemView, OnSubjectSortClick onSubjectSortClick) {
            super(itemView);
            sub = itemView.findViewById(R.id.SubjectName);
            done = itemView.findViewById(R.id.selected);
            selctedFrame = itemView.findViewById(R.id.selected_Frame);
            card = itemView.findViewById(R.id.SubjectCard);
            this.onSubjectSortClick = onSubjectSortClick;
            card.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
        }
    }

    public interface OnSubjectSortClick {
        void onsubjectSortClick(String Id, String name, int position);
    }

}
