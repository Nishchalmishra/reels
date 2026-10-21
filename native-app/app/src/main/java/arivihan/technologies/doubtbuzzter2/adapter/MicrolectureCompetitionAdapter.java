package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.MicrolectureListActivity;
import arivihan.technologies.doubtbuzzter2.model.NameAndIdDto;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class MicrolectureCompetitionAdapter extends RecyclerView.Adapter<MicrolectureCompetitionAdapter.ViewHolder> {

    List<NameAndIdDto> arr;
    Context context;
    String chapter, chapterId;

    public MicrolectureCompetitionAdapter(List<NameAndIdDto> arr, Context context, String chapter, String chapterId) {
        this.arr = arr;
        this.context = context;
        this.chapter = chapter;
        this.chapterId = chapterId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.syllabus_microlecturename_layout,parent,false);
        return new MicrolectureCompetitionAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.text.setText(arr.get(position).getName());
        holder.text.setOnClickListener(view -> {
            Intent intent = new Intent(context, MicrolectureListActivity.class);
            intent.putExtra("chapterName", chapter);
            intent.putExtra("chapterId",  chapterId);
            MyApplication.saveData(PreferenceKeys.MICROLECTURE_ID.getKey(), arr.get(position).getId());
            context.startActivity(intent);
        });
        if (arr.get(position).isCompleted()) {
            holder.completed.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return arr.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView text;
        ImageView completed;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.chapterName);
            completed = itemView.findViewById(R.id.completed);
        }
    }
}
