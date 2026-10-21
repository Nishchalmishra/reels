package arivihan.technologies.doubtbuzzter2.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.facebook.shimmer.Shimmer;
import com.facebook.shimmer.ShimmerDrawable;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.subject.SubjectResponse;

public class SamplePaperAdapter extends RecyclerView.Adapter<SamplePaperAdapter.SamplePaperViewHolder> {

    private List<SubjectResponse> sub;
    SamplePaperListener samplePaperListener;
    Context context;

    public SamplePaperAdapter(List<SubjectResponse> sub, SamplePaperListener samplePaperListener, Context context) {
        this.sub = sub;
        this.samplePaperListener = samplePaperListener;
        this.context = context;
    }

    @NonNull
    @Override
    public SamplePaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.subjects_layout, parent, false);
        return new SamplePaperViewHolder(view, samplePaperListener);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull SamplePaperViewHolder holder, int position) {
        SubjectResponse sm = sub.get(position);
        Shimmer shimmer = new Shimmer.ColorHighlightBuilder()
                .setBaseColor(ContextCompat.getColor(context, R.color.shimmer_base_color))
                .setBaseAlpha(1)
                .setHighlightColor(ContextCompat.getColor(context, R.color.shimmer_highlight_color))
                .setHighlightAlpha(1)
                .setDropoff(50)
                .build();

        ShimmerDrawable shimmerDrawable = new ShimmerDrawable();
        shimmerDrawable.setShimmer(shimmer);

        holder.chapters.setText(sm.getChapterCount() + " chapters");
        holder.subject.setText(sm.getSubjectName());
        Glide.with(context).load(sm.getIconUrl()).into(holder.image);
//        holder.card.setCardBackgroundColor(Color.parseColor(sm.getColor()));


    }

    @Override
    public int getItemCount() {
        return sub.size();
    }

    public static class SamplePaperViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView image;
        public TextView subject;
        public TextView chapters;
        public CardView card;
        SamplePaperListener samplePaperListener;

        public SamplePaperViewHolder(@NonNull View itemView, SamplePaperListener samplePaperListener) {
            super(itemView);

            image = (ImageView) itemView.findViewById(R.id.subject_icon);
            subject = (TextView) itemView.findViewById(R.id.subject_name);
            chapters = (TextView) itemView.findViewById(R.id.subject_chapters);
            card = (CardView) itemView.findViewById(R.id.card);
            this.samplePaperListener = samplePaperListener;
            card.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            samplePaperListener.onSamplePaperClick(getAdapterPosition());
        }
    }

    public interface SamplePaperListener {
        void onSamplePaperClick(int position);
    }
}
