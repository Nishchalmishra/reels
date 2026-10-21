package arivihan.technologies.doubtbuzzter2.adapter.competitive;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.helper.CustomBackgroundTarget;
import arivihan.technologies.doubtbuzzter2.model.SubjectEnum;
import arivihan.technologies.doubtbuzzter2.model.subject.SubjectResponse;

public class PersonalizedSubjectAdapter extends RecyclerView.Adapter<PersonalizedSubjectAdapter.SubjectViewHolder> {

    private final List<SubjectResponse> subjects;
    private final OnSubjectClickListener listener;
    private Context context;

    public interface OnSubjectClickListener {
        void onSubjectClick(SubjectResponse subject);
    }

    // Constructor with listener
    public PersonalizedSubjectAdapter(Context context, List<SubjectResponse> subjects, OnSubjectClickListener listener) {
        this.context = context;
        this.subjects = subjects;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_personalized_path_subject, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        SubjectResponse subject = subjects.get(position);

        // Card background color
        if (subject.getBackgroundColor() != null) {
            try {
                holder.cardView.setCardBackgroundColor(Color.parseColor(subject.getBackgroundColor()));
            } catch (Exception e) {
                holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.personalized_subject_fallback_bg)); // fallback
            }
        }

        // Background image
        if (StringUtils.isNotEmpty(subject.getBackgroundImageUrl())) {
            Glide.with(holder.rlCardBackground.getContext())
                    .load(subject.getBackgroundImageUrl())
                    .into(new CustomBackgroundTarget(holder.rlCardBackground));
        }

        holder.tvSubjectName.setText(subject.getSubjectName());
        holder.tvSubjectName.setTextColor(StringUtils.isNotEmpty(subject.getSubjectTitleColor()) ? Color.parseColor(subject.getSubjectTitleColor()) : Color.parseColor(getSubjectColor(subject.getSubjectName())));
        // Subtitle
        holder.tvSubtitle.setText(subject.getSubtitle());
        // Subject icon
        if (StringUtils.isNotEmpty(subject.getIconUrl())) {
            Glide.with(holder.ivSubjectIcon.getContext())
                    .load(subject.getIconUrl())
                    .into(holder.ivSubjectIcon);
        }
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSubjectClick(subject);
            }
        });
    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }

    public static String getSubjectColor(String subject) {
        try {
            Log.d("Sujecgt cokor", "--->   "+subject);
            if (subject.equalsIgnoreCase(SubjectEnum.BIOLOGY.name())) {
                return "#395937";
            } else if (subject.equalsIgnoreCase(SubjectEnum.PHYSICS.name())) {
                return "#0E3450";
            } else if (subject.equalsIgnoreCase(SubjectEnum.CHEMISTRY.name())) {
                return "#4C2C01";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "#000000";
    }

    public static class SubjectViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        RelativeLayout rlCardBackground;
        TextView tvSubjectName;
        TextView tvSubtitle;
        ImageView ivSubjectIcon;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cv_item);
            rlCardBackground = itemView.findViewById(R.id.rl_card_background);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            ivSubjectIcon = itemView.findViewById(R.id.ivSubjectIcon);
        }
    }
}
