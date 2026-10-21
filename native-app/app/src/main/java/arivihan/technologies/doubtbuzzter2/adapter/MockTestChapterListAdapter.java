package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.activity.ActivityTestIntro;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.MockTestChapterListModel;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class MockTestChapterListAdapter extends RecyclerView.Adapter<MockTestChapterListAdapter.ViewHolder> {

    private Context context;
    private List<MockTestChapterListModel> model;
    private boolean isCombined;
    String id;
    boolean isLocked;
    MockListener mockListener;

    public MockTestChapterListAdapter(String id,Context context, List<MockTestChapterListModel> model,
                                      boolean isCombined, boolean isLocked, MockListener mockListener) {
        this.context = context;
        this.model = model;
        this.isCombined = isCombined;
        this.id=id;
        this.isLocked = isLocked;
        this.mockListener = mockListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.mock_test_chapterlist_layout, parent, false);
        return new MockTestChapterListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MockTestChapterListModel m = model.get(position);

        long time = m.getTime() / 60000;
        holder.question.setText(m.getQuestions() + " " + context.getString(R.string.Questions));
        if (time >= 60) {
            long Time = time / 60;
            if (time % 60 == 0)
                holder.time.setText(Time + " " + context.getString(R.string.hours));
            else
                holder.time.setText(Time + ":" + time % 60 + " " + context.getString(R.string.hours));
        } else
            holder.time.setText(time + " " + context.getString(R.string.min));
        holder.chapter.setText(m.getChapter());
        if (m.getAttempted()) {
            holder.TextStatus.setText(context.getString(R.string.ReAttempt));
            holder.cardReview.setVisibility(View.VISIBLE);
            holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.mock_chapter_card_bg));
            holder.score.setVisibility(View.VISIBLE);
            holder.score.setText(context.getText(R.string.YourScore) + " : " + m.getYourScore() + " / " + m.getTotalScore());
        } else {
            holder.score.setText(context.getText(R.string.YourScore) + " : 0 / " + m.getTotalScore());
        }

        holder.card.setOnClickListener(view -> {
            if (isLocked) {
                mockListener.onLockClick();
            } else {
                Intent intent = new Intent(context, ActivityTestIntro.class);
                intent.putExtra("Id", id);
                intent.putExtra("testId", m.getTestId());
                intent.putExtra("subject", m.getChapter());
                intent.putExtra("combined", isCombined);
                if (m.getAttempted() == true)
                    intent.putExtra(PreferenceKeys.TYPE.getKey(), "REATTEMPT");
                else {
                    intent.putExtra(PreferenceKeys.TYPE.getKey(), "ATTEMPT");
                }
                context.startActivity(intent);
            }
        });

        holder.cardReview.setOnClickListener(view -> {
            if (isLocked) {
                mockListener.onLockClick();
            } else {
                Intent intent = new Intent(context, ActivityTestIntro.class);
                intent.putExtra("Id", id);
                intent.putExtra("testId", m.getTestId());
                intent.putExtra("subject", m.getChapter());
                intent.putExtra("combined", isCombined);
                intent.putExtra(PreferenceKeys.TYPE.getKey(), PreferenceKeys.RESPONSE.getKey());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView chapter, score, question, time, TextStatus;
        CardView card, cardReview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            chapter = itemView.findViewById(R.id.chapterName);
            score = itemView.findViewById(R.id.YourScore);
            cardReview = itemView.findViewById(R.id.ReviewCard);
            question = itemView.findViewById(R.id.questions);
            time = itemView.findViewById(R.id.Time);
            TextStatus = itemView.findViewById(R.id.TestStatus);
            card = itemView.findViewById(R.id.TestCard);

        }

    }

    public interface MockListener {
        void onLockClick();
    }

}
