package arivihan.technologies.doubtbuzzter2.adapter.competitive.subject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.model.subject.SubjectResponse;

public class CompetitiveSubjectChapterAdapter extends RecyclerView.Adapter<CompetitiveSubjectChapterAdapter.SubjectViewHolder> {

    private final List<SubjectResponse> subjects;
    private final String sectionType;
    private final OnSubjectClickListener listener;
    private boolean hideChapterCount;

    public interface OnSubjectClickListener {
        void onSubjectClick(SubjectResponse subject, String sectionType);
    }

    public CompetitiveSubjectChapterAdapter(List<SubjectResponse> subjects, String sectionType, boolean hideChapterCount, OnSubjectClickListener listener) {
        this.subjects = subjects;
        this.sectionType = sectionType;
        this.listener = listener;
        this.hideChapterCount = hideChapterCount;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_competitive_subject_card, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        SubjectResponse subject = subjects.get(position);

        holder.tv_sub_title.setText(StringUtils.isNotEmpty(subject.getChapterCount()) ? subject.getChapterCount() : "");
        holder.tv_title.setText(StringUtils.isNotEmpty(subject.getSubjectName()) ? subject.getSubjectName() : "");

        holder.tv_sub_title.setVisibility(hideChapterCount ? View.GONE : View.VISIBLE);
        // Subject icon
        if (StringUtils.isNotEmpty(subject.getIconUrl())) {
            Glide.with(holder.iv_image.getContext())
                    .load(subject.getIconUrl())
                    .error(R.drawable.white_logo)
                    .into(holder.iv_image);
        }
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                Map<String, String> params = new HashMap<>();
                params.put("subject_name", subject.getSubjectName());
                params.put("section_type", sectionType);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.competitive_learn_subject_btn_click.getActionName(),params);
                listener.onSubjectClick(subject, sectionType);
            }
        });
    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }

    public static class SubjectViewHolder extends RecyclerView.ViewHolder {
        CardView cv_parent_layout;
        TextView tv_title;
        TextView tv_sub_title;
        ImageView iv_image;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            cv_parent_layout = itemView.findViewById(R.id.cv_parent_layout);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_sub_title = itemView.findViewById(R.id.tv_sub_title);
            iv_image = itemView.findViewById(R.id.iv_image);
        }
    }
}
