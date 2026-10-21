package arivihan.technologies.doubtbuzzter2.adapter;

/*
 * File: CouponAdapter Copyright 22/3/22 5:01 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 22/3/22 5:01 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.NameAndScoreDto;

public class SubjectScoreAdapter extends RecyclerView.Adapter<SubjectScoreAdapter.ViewHolder> {

    List<NameAndScoreDto> nameAndScoreDtoList ;
    public SubjectScoreAdapter(List<NameAndScoreDto> nameAndScoreDtoList) {
        this.nameAndScoreDtoList = nameAndScoreDtoList;
    }

    @NonNull
    @Override
    public SubjectScoreAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.subject_score_layout,parent,false);
        return new SubjectScoreAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectScoreAdapter.ViewHolder holder, int position) {
        holder.subject.setText(nameAndScoreDtoList.get(position).getSubjectName());
        holder.score.setText(""+nameAndScoreDtoList.get(position).getScore());
    }

    @Override
    public int getItemCount() {
        return nameAndScoreDtoList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView subject, score;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            subject = itemView.findViewById(R.id.subject);
            score = itemView.findViewById(R.id.score);
        }
    }

}
