package arivihan.technologies.doubtbuzzter2.adapter;

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

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.batch.test_series.TestSeriesCard;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class TestSeriesAdapter extends RecyclerView.Adapter<TestSeriesAdapter.TestSeriesViewHolder> {

    public List<TestSeriesCard> tm;
    Context context;
    TestSeriesSetClickListener testSeriesSetClickListener;

    public TestSeriesAdapter(List<TestSeriesCard> tm, Context context, TestSeriesSetClickListener testSeriesSetClickListener) {
        this.tm = tm;
        this.context = context;
        this.testSeriesSetClickListener = testSeriesSetClickListener;
    }

    @NonNull
    @Override
    public TestSeriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.test_series_layout, parent, false);
        return new TestSeriesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TestSeriesViewHolder holder, int position) {
        TestSeriesCard t = tm.get(position);
        testSeriesSetClickListener.calculateCount(t);

        Shimmer shimmer = new Shimmer.ColorHighlightBuilder()
                .setBaseColor(ContextCompat.getColor(context, R.color.shimmer_base_color))
                .setBaseAlpha(1)
                .setHighlightColor(ContextCompat.getColor(context, R.color.shimmer_highlight_color))
                .setHighlightAlpha(1)
                .setDropoff(50)
                .build();

        ShimmerDrawable shimmerDrawable = new ShimmerDrawable();
        shimmerDrawable.setShimmer(shimmer);

        holder.subject.setText(t.getName());
        int count = t.getTotalTestCount() != null ? t.getTotalTestCount() : 0;
        holder.chapters.setText(count + " " + context.getString(R.string.tests));
        holder.card.setOnClickListener(v -> {
            MyApplication.saveData(PreferenceKeys.TEST_SERIES_COURSE_TITLE.getKey(), t.getName());
            testSeriesSetClickListener.onTestSeriesSetClick(t);
        });
        if (holder.image.getContext() != null) {
            Glide.with(holder.image.getContext()).load(t.getIconUrl()).into(holder.image);
        }
    }

    @Override
    public int getItemCount() {
        return tm.size();
    }

    public class TestSeriesViewHolder extends RecyclerView.ViewHolder {

        public ImageView image;
        public TextView subject;
        public TextView chapters;
        public CardView card;

        public TestSeriesViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.profile_icon);
            subject = itemView.findViewById(R.id.exam_text);
            chapters = itemView.findViewById(R.id.chapters);
            card = itemView.findViewById(R.id.card);
        }
    }

    public interface TestSeriesSetClickListener {
        void onTestSeriesSetClick(TestSeriesCard t);
        void calculateCount(TestSeriesCard t);
    }
}
