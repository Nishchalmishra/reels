package arivihan.technologies.doubtbuzzter2.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.subject.SubjectResponse;

/*
 * File: SubjectSelectionAdapter Copyright 25/03/24, 2:31 pm Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 25/03/24, 2:31 pm
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

public class SubjectSelectionAdapter extends RecyclerView.Adapter<SubjectSelectionAdapter.ViewHolder> {

    List<SubjectResponse> subjectModels;
    Context context;
    SubjectClickListener clickListener;
    public SubjectSelectionAdapter(Context context, List<SubjectResponse> subjectModels,
                                   SubjectClickListener subjectClickListener) {
        this.subjectModels = subjectModels;
        this.context = context;
        this.clickListener = subjectClickListener;
    }

    @NonNull
    @Override
    public SubjectSelectionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.doubt_subject_selection,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectSelectionAdapter.ViewHolder holder, int position) {
        SubjectResponse subjectModel = subjectModels.get(position);

        String colorString = subjectModel.getColor();

        if (colorString != null && !colorString.isEmpty()) {
            try {
                int color = Color.parseColor(colorString);

                Drawable background = ContextCompat.getDrawable(context, R.drawable.border_chat_choose_subject_teal);
                if (background != null) {
                    background = background.mutate();
                    background.setTint(color);
                    holder.subjectSelectionLayout.setBackground(background);
                }

            } catch (IllegalArgumentException e) {
                Log.e("Adapter", "Invalid color string: " + colorString, e);
            }
        }

        holder.subjectTextView.setText(subjectModel.getSubjectName());

        holder.subjectSelectionLayout.setOnClickListener(view -> {
            clickListener.subjectClicked(subjectModel);
        });
    }

    @Override
    public int getItemCount() {
        return subjectModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout subjectSelectionLayout;
        private TextView subjectTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectSelectionLayout = itemView.findViewById(R.id.subject_selection_layout);
            subjectTextView = itemView.findViewById(R.id.subject_text);
        }
    }


    public interface SubjectClickListener {
        void subjectClicked(SubjectResponse subjectModel);
    }
}
