package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.view.ContentInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;

public class ChapterFilterAdapter extends RecyclerView.Adapter<ChapterFilterAdapter.ChapterFilterViewHolder>{

    Context context;
    ArrayList<String> list;

    public ChapterFilterAdapter(Context context, ArrayList<String> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ChapterFilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.filter_list_item,parent,false);
        return new ChapterFilterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChapterFilterViewHolder holder, int position) {
         holder.checkBox.setText(list.get(position));
         holder.checkBox.setOnClickListener(v -> {

         });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ChapterFilterViewHolder extends RecyclerView.ViewHolder {

        CheckBox checkBox;
        public ChapterFilterViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.filter_tag);
        }
    }
}
