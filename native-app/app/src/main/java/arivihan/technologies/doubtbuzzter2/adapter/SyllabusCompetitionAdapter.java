package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.MicrolectureListActivity;
import arivihan.technologies.doubtbuzzter2.model.NameAndIdDto;
import arivihan.technologies.doubtbuzzter2.model.SyllabusCompetitionModel;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class SyllabusCompetitionAdapter extends RecyclerView.Adapter<SyllabusCompetitionAdapter.ViewHolder> {

    ArrayList<SyllabusCompetitionModel> model;
    Context context;

    public SyllabusCompetitionAdapter(ArrayList<SyllabusCompetitionModel> model, Context context) {
        this.model = model;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.syllabus_layout,parent,false);
        return new SyllabusCompetitionAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL ,false));
        List<NameAndIdDto> microlectures = model.get(position).getMicrolecture();
        MicrolectureCompetitionAdapter adapter = new MicrolectureCompetitionAdapter(microlectures, context, model.get(position).getChapter(), model.get(position).getChapterId());
        holder.recyclerView.setAdapter(adapter);

        holder.chapter.setText(model.get(position).getChapter());

        holder.chapter.setOnClickListener(view -> {
            Intent intent = new Intent(context, MicrolectureListActivity.class);
            intent.putExtra("chapterName", model.get(position).getChapter());
            intent.putExtra("chapterId",  model.get(position).getChapterId());
            MyApplication.saveData(PreferenceKeys.MICROLECTURE_ID.getKey(), "-1");
            context.startActivity(intent);
        });

        if (model.get(position).isCompleted()) {
            holder.completed.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView chapter;
        RecyclerView recyclerView;
        ImageView completed;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            chapter = itemView.findViewById(R.id.chapter);
            recyclerView = itemView.findViewById(R.id.recyclerView);
            completed = itemView.findViewById(R.id.completed);
        }
    }
}
