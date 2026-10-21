package arivihan.technologies.doubtbuzzter2.adapter;

/*
 * File: TestSeriesExpandableAdapter Copyright 7/1/22 3:04 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 7/1/22 3:04 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.Shimmer;
import com.facebook.shimmer.ShimmerDrawable;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.MockTestDetailActivity;
import arivihan.technologies.doubtbuzzter2.listener.TestSeriesListener;
import arivihan.technologies.doubtbuzzter2.model.TestSeriesSetDto;

import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class TestSeriesExpandableAdapter extends RecyclerView.Adapter<TestSeriesExpandableAdapter.TestSeriesViewHolder> {

    private final Context context;
    List<TestSeriesSetDto> testSeriesSetDtoList;
    private final TestSeriesListener testSeriesListener;
    private final String testId;
    Dialog dialog2;
    OnTestClickListener onTestClickListener;

    public TestSeriesExpandableAdapter(String testId, Context context, TestSeriesListener testSeriesListener, List<TestSeriesSetDto> testSeriesSetDtoList, OnTestClickListener onTestClickListener) {
        this.testSeriesListener = testSeriesListener;
        this.testSeriesSetDtoList = testSeriesSetDtoList;
        this.context = context;
        this.onTestClickListener = onTestClickListener;
        this.testId = testId;
    }

    @NonNull
    @Override
    public TestSeriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.test_series_expandable_item, parent, false);
        return new TestSeriesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TestSeriesViewHolder holder, int position) {
        TestSeriesSetDto tm = testSeriesSetDtoList.get(position);

        Shimmer shimmer = new Shimmer.ColorHighlightBuilder()
                .setBaseColor(ContextCompat.getColor(context, R.color.shimmer_base_color))
                .setBaseAlpha(1)
                .setHighlightColor(ContextCompat.getColor(context, R.color.shimmer_highlight_color))
                .setHighlightAlpha(1)
                .setDropoff(50)
                .build();

        ShimmerDrawable shimmerDrawable = new ShimmerDrawable();
        shimmerDrawable.setShimmer(shimmer);
        TestSeriesSetDto testSeriesSetDto = testSeriesSetDtoList.get(position);

        TestSeriesSubjectAdapter testSeriesSubjectAdapter = new TestSeriesSubjectAdapter(testId, testSeriesSetDto.getTestSeriesId(),
                testSeriesListener, testSeriesSetDtoList.get(position).getTestSeriesTypes());

        holder.mockTestName.setText(tm.getTestSeriesName());

        holder.syllabus.setText(tm.getSyllabusInfo());

//        holder.recyclerView.setHasFixedSize(true);
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.recyclerView.setAdapter(testSeriesSubjectAdapter);

        holder.lockText.setText(testSeriesSetDto.getTestSeriesName());
        long time = tm.getTotalTimeToAttempt() / (60000);
        if (time > 60) {
            long Time = time / 60;
            if (time % 60 == 0)
                holder.time.setText(Time + " " + context.getString(R.string.hours));
            else
                holder.time.setText(Time + ":" + time % 60 + " " + context.getString(R.string.hours));
        } else{
            holder.time.setText(String.valueOf(time) + " " + context.getString(R.string.min));
        }
        holder.questions.setText(tm.getTotalQuestions() + " " + context.getString(R.string.Questions));

        if (StringUtils.isEmpty(tm.getSyllabusInfo())) {
            holder.tag.setVisibility(View.GONE);
        }


        // Mock Test Lock Feature--
        if (tm.isLock()) {
            holder.nonLockFrame.setAlpha(0.5f);
            holder.relativeLayoutNotCompleted.setVisibility(View.GONE);
            holder.lockFrame.setVisibility(View.VISIBLE);
            holder.PercentageText.setVisibility(View.GONE);
            holder.Score.setVisibility(View.GONE);
            holder.done.setVisibility(View.GONE);

        } else {
            holder.nonLockFrame.setAlpha(1.0f);
            holder.relativeLayoutNotCompleted.setVisibility(View.VISIBLE);
            holder.lockFrame.setVisibility(View.GONE);
            holder.PercentageText.setVisibility(View.VISIBLE);
            if (tm.isAttempted()) {
                holder.done.setVisibility(View.VISIBLE);
                holder.Score.setVisibility(View.VISIBLE);
                holder.done_Mock.setVisibility(View.VISIBLE);
                holder.relativeLayoutNotCompleted.setVisibility(View.GONE);
                holder.Score.setText(context.getString(R.string.YourScore) + " : " + tm.getUserScore() + " / " + tm.getTotalScore());
            } else {
                holder.relativeLayoutNotCompleted.setVisibility(View.VISIBLE);
                holder.progress.setProgress(tm.getProgress());
                holder.done_Mock.setVisibility(View.GONE);
                holder.Score.setVisibility(View.GONE);
                holder.done.setVisibility(View.GONE);
                holder.PercentageText.setText(tm.getProgress() + "%");
            }

            if (tm.isEvaluationInProgress()) {
                holder.progress.setVisibility(View.GONE);
                holder.PercentageText.setVisibility(View.GONE);
                holder.Score.setVisibility(View.VISIBLE);
                holder.Score.setText(R.string.analysing_mock_test);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (!tm.isLock()) {
                Intent intent = new Intent(context, MockTestDetailActivity.class);
                intent.putExtra("testId", tm.getTestSeriesId());
                intent.putExtra("Title", tm.getTestSeriesName());
                intent.putExtra("isEvaluationInProgress", tm.isEvaluationInProgress());
                MyApplication.saveData(PreferenceKeys.POSITION.getKey(), tm.getTestSeriesId());
                context.startActivity(intent);
            } else {
                onTestClickListener.ontestClick(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return testSeriesSetDtoList.size();
    }

    static class TestSeriesViewHolder extends RecyclerView.ViewHolder {

        private final RecyclerView recyclerView;
        private final LinearLayout expandLayout;
        private final TextView mockTestName, finalScore, lockText;
        TextView questions, time, Score, PercentageText, syllabus, rank;
        ProgressBar progress;
        ImageView done, first_place;
        RelativeLayout relativeLayoutNotCompleted;
        RelativeLayout nonLockFrame, first;
        FrameLayout lockFrame, done_Mock;
        ImageView tag;

        public TestSeriesViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.test_series_recycler);
            lockText = itemView.findViewById(R.id.mockTestNameLock);
            rank = itemView.findViewById(R.id.rank);
            syllabus = itemView.findViewById(R.id.syllabus);
            first_place = itemView.findViewById(R.id.first_place);
            first = itemView.findViewById(R.id.first);
            tag = itemView.findViewById(R.id.tag);
            expandLayout = itemView.findViewById(R.id.expand);
            progress = itemView.findViewById(R.id.progress);
            relativeLayoutNotCompleted = itemView.findViewById(R.id.NotCompleted);
            nonLockFrame = itemView.findViewById(R.id.nonLockFrame);
            done_Mock = itemView.findViewById(R.id.mock_given);
            PercentageText = itemView.findViewById(R.id.progress_text);
            mockTestName = itemView.findViewById(R.id.mockTestName);
            questions = itemView.findViewById(R.id.questions);
            lockFrame = itemView.findViewById(R.id.lock_Frame);
            Score = itemView.findViewById(R.id.Score);
            time = itemView.findViewById(R.id.Time);
            done = itemView.findViewById(R.id.done);
            finalScore = itemView.findViewById(R.id.finalScore);
        }
    }

    public interface OnTestClickListener {
        void ontestClick(int position);

    }

}
