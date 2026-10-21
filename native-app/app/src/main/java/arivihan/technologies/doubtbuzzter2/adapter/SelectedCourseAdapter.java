package arivihan.technologies.doubtbuzzter2.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.NameAndIdDto;
import arivihan.technologies.doubtbuzzter2.model.NameIdModel;

public class SelectedCourseAdapter extends RecyclerView.Adapter<SelectedCourseAdapter.ViewHolder> {

    ArrayList<NameIdModel> subject;
    OnClick onClick;

    public SelectedCourseAdapter(ArrayList<NameIdModel> subject, OnClick onCourseSelection) {
        this.subject = subject;
        this.onClick = onCourseSelection;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.selected_course_layout, parent, false);
        return new SelectedCourseAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(subject.get(position).getName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClick.onClick(position, subject.get(position).getSubTypeList());
            }
        });

        if (subject.get(position).isSelected()) {
            holder.name.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.navy_blue));
            holder.name.setTextSize(16);
            holder.cardView.setStrokeColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.navy_blue));
        } else {
            holder.name.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.album_title));
            holder.cardView.setStrokeColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.album_title));
            holder.name.setTextSize(15);
        }

    }

    @Override
    public int getItemCount() {
        return subject.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        MaterialCardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.Course_Name);
            cardView = itemView.findViewById(R.id.materialCard);
        }
    }

    public interface OnClick {
        void onClick(int position, List<NameAndIdDto> nameAndIdDtoList);
    }

}
