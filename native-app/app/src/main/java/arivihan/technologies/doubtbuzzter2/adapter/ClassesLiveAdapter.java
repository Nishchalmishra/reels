package arivihan.technologies.doubtbuzzter2.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.facebook.shimmer.Shimmer;
import com.facebook.shimmer.ShimmerDrawable;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.MicrolectureListActivity;
import arivihan.technologies.doubtbuzzter2.model.ClassesLiveModel;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class ClassesLiveAdapter extends RecyclerView.Adapter<ClassesLiveAdapter.ClassesLiveViewHolder> {

    private List<ClassesLiveModel> cm;
    Context context;
    OnClassesClick onClassesClick;

    public ClassesLiveAdapter(List<ClassesLiveModel> cm, Context context, OnClassesClick onClassesClick) {
        this.cm = cm;
        this.context = context;
        this.onClassesClick = onClassesClick;
    }

    @NonNull
    @Override
    public ClassesLiveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.classes_live_layout, parent, false);
        return new ClassesLiveViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassesLiveViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Shimmer shimmer = new Shimmer.ColorHighlightBuilder()
                .setBaseColor(ContextCompat.getColor(context, R.color.shimmer_base_color))
                .setBaseAlpha(1)
                .setHighlightColor(ContextCompat.getColor(context, R.color.shimmer_highlight_color))
                .setHighlightAlpha(1)
                .setDropoff(50)
                .build();

        ShimmerDrawable shimmerDrawable = new ShimmerDrawable();
        shimmerDrawable.setShimmer(shimmer);


        holder.Video_Progress.setVisibility(View.VISIBLE);
        holder.Video_Progress.setProgress(cm.get(position).getProgress());
        holder.progressValue.setText(context.getResources().getString(R.string.overall_progress_text)+" " + cm.get(position).getProgress() + "%");

        holder.subTitle.setText(cm.get(position).getVideoSubtitle());
        if (cm.get(position).getImageUrl() != null) {
            Glide.with(context).load(cm.get(position).getImageUrl()).into(holder.subjectIcon);
        }
        holder.title.setText(cm.get(position).getVideoTitle());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cm.get(position).isLocked()) {
                    onClassesClick.onclassesClick(position);
                } else {
                    Intent intent = new Intent(context.getApplicationContext(), MicrolectureListActivity.class);
                    String chapterName = cm.get(position).getVideoSubtitle();
                    String Id = cm.get(position).getId();
                    intent.putExtra("chapterId", Id);
                    intent.putExtra("chapterName", chapterName);
                    MyApplication.saveData(chapterName + PreferenceKeys.TYPE.getKey(), 0);
                    MyApplication.saveData(PreferenceKeys.MICROLECTURE_ID.getKey(), cm.get(position).getMicrolectureId());
                    context.startActivity(intent);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return cm.size();
    }


    public class ClassesLiveViewHolder extends RecyclerView.ViewHolder {

        public ImageView subjectIcon;
        public TextView title, subTitle, progressValue;
        public CardView card;
        public ProgressBar Video_Progress;

        public ClassesLiveViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectIcon = itemView.findViewById(R.id.subject_icon_img);
            title = itemView.findViewById(R.id.title);
            subTitle = itemView.findViewById(R.id.sub_title);
            Video_Progress = itemView.findViewById(R.id.Video_Progress);
            progressValue = itemView.findViewById(R.id.progress_value);
        }
    }

    public interface OnClassesClick {
        void onclassesClick(int position);
    }

}
