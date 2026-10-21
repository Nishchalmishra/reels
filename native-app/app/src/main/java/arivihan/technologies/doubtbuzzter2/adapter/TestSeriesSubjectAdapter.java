package arivihan.technologies.doubtbuzzter2.adapter;

/*
 * File: TestSeriesSubjectAdapter Copyright 7/1/22 4:54 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 7/1/22 4:54 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.listener.TestSeriesListener;
import arivihan.technologies.doubtbuzzter2.model.TestSeriesTypeDto;

public class TestSeriesSubjectAdapter extends RecyclerView.Adapter<TestSeriesSubjectAdapter.TestSeriesViewHolder>{

    private final List<TestSeriesTypeDto> list;
    private final TestSeriesListener testSeriesListener;
    private final String testSeriesId, testId;

    public TestSeriesSubjectAdapter(String testId, String testSeriesId, TestSeriesListener testSeriesListener, List<TestSeriesTypeDto> list) {
        this.list = list;
        this.testSeriesListener = testSeriesListener;
        this.testSeriesId = testSeriesId;
        this.testId = testId;
    }

    @NonNull
    @Override
    public TestSeriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.test_series_subtopics,parent,false);
        return new TestSeriesViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull TestSeriesSubjectAdapter.TestSeriesViewHolder holder, int position) {
        TestSeriesTypeDto testSeriesTypeDto = list.get(position);
        holder.subjectName.setText(testSeriesTypeDto.getSubject());

        if (testSeriesTypeDto.isAttempted()) {
            holder.attempt.setVisibility(View.GONE);
            holder.attempted.setVisibility(View.VISIBLE);
            holder.subjectName.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.correct_answer_text_color));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class TestSeriesViewHolder extends RecyclerView.ViewHolder {
        private final TextView subjectName;
        ImageView attempt,attempted;
        public TestSeriesViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectName = itemView.findViewById(R.id.subjectName);
            attempt = itemView.findViewById(R.id.button_attempt);
            attempted = itemView.findViewById(R.id.button_attempted);
        }
    }
}

