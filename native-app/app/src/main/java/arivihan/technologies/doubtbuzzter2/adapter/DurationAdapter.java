package arivihan.technologies.doubtbuzzter2.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;

public class DurationAdapter extends RecyclerView.Adapter<DurationAdapter.ViewHolder> {

    ArrayList<String> arr;
    OnDurationClick onDurationClick;
    int pos;

    public DurationAdapter(ArrayList<String> arr, OnDurationClick onDurationClick,int pos) {
        this.arr = arr;
        this.onDurationClick = onDurationClick;
        this.pos = pos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.duration_layout,parent,false);
        return new DurationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.text.setText(arr.get(position));

        if (position == pos){
            holder.text.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.navy_blue));
            holder.text.setTextSize(16);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDurationClick.ondurationclick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arr.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView text;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.duration);
        }
    }

    public interface OnDurationClick{
        void ondurationclick(int position);
    }

}
