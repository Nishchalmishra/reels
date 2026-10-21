package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.listener.ChapterDetailActivityListener;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureContentDto;

public class LecturesAdapter extends RecyclerView.Adapter<LecturesAdapter.ViewHolder> {

    ArrayList<MicroLectureContentDto> lectureModels;
    Context context;
    private final ChapterDetailActivityListener listener;

    public LecturesAdapter(Context context,ArrayList<MicroLectureContentDto> lectureModels, ChapterDetailActivityListener listener) {
        this.lectureModels = lectureModels;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.lecture_ui,parent,false);
        return new LecturesAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MicroLectureContentDto lm = lectureModels.get(position);

        if(lm.getMicrolectureThumbnail() != null)
            Glide.with(context).load(lm.getMicrolectureThumbnail()).into(holder.image);

        holder.image.setOnClickListener(v -> {
            listener.goToActivity(lm, position);
        });

        holder.videoProgress.setProgress(lm.getProgress());
    }

    @Override
    public int getItemCount() {
        return lectureModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        ProgressBar videoProgress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.Video_Image);
            videoProgress = itemView.findViewById(R.id.Video_Progress);
        }
    }
}
