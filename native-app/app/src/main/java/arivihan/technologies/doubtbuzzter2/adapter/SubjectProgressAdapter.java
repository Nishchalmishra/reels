package arivihan.technologies.doubtbuzzter2.adapter;

/*
 * File: OptionAdapter Copyright 14/2/21 1:26 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 14/2/21 1:26 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.List;
import java.util.Random;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.SubjectProgressDto;

public class SubjectProgressAdapter extends RecyclerView.Adapter<SubjectProgressAdapter.OptionViewHolder>{

    private final List<SubjectProgressDto> list;
    private final Context context;
    OnProgressClick onProgressClick;
    private final String[] colorCodes = {"#FFDAD7", "#73B6ED", "#FED974", "#FAD7A0", "#EBDEF0", "#D6DBDF"};
    private final String[] darkerColorCodes = {"#FD8584", "#2D82D7", "#F39C12", "#F39C12", "#C39BD3", "#85929E"};

    public SubjectProgressAdapter(List<SubjectProgressDto> list, Context context1,OnProgressClick onProgressClick) {
        this.list = list;
        this.onProgressClick = onProgressClick;
        this.context = context1;
    }

    @NonNull
    @Override
    public SubjectProgressAdapter.OptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.subject_progress_recycler_item, parent, false);
        return new SubjectProgressAdapter.OptionViewHolder(inflate, context);
    }

    @Override
    public void onBindViewHolder(@NonNull final SubjectProgressAdapter.OptionViewHolder holder, final int position) {

       holder.subjectName.setText(list.get(position).getSubjectName());
       holder.progressBar.setProgress(list.get(position).getProgress());
       int index = 0;
       if (colorCodes.length <= position ) {
           Random rand = new Random();
           index = rand.nextInt(colorCodes.length);
       } else  {
           index = position;
       }

       holder.itemView.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               onProgressClick.onprogressClick(position);
           }
       });

       holder.progressBar.setIndicatorColor(Color.parseColor(darkerColorCodes[index]));
       holder.progressBar.setTrackColor(Color.parseColor(colorCodes[index]));
       holder.progressNumber.setBackground(getBackgroundShape(index));
       holder.Progress_text.setText(list.get(position).getProgress()+"");
    }

    private Drawable getBackgroundShape(int position) {

        ShapeDrawable shapeDrawable = new ShapeDrawable (new OvalShape());
        shapeDrawable.setIntrinsicWidth (35);
        shapeDrawable.setIntrinsicHeight (35);
        shapeDrawable.getPaint().setColor(Color.parseColor(colorCodes[position]));
        return shapeDrawable;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class OptionViewHolder extends RecyclerView.ViewHolder {
        private final TextView subjectName;
        private final CircularProgressIndicator progressBar;
        private final TextView progressNumber;
        TextView Progress_text;
        private Drawable drawable;
        public OptionViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            subjectName = itemView.findViewById(R.id.subject_name);
            Progress_text = itemView.findViewById(R.id.progress_text);
            progressBar = itemView.findViewById(R.id.progress_bar);
            progressNumber = itemView.findViewById(R.id.progress_number);
        }
    }


    public interface OnProgressClick{
        void onprogressClick(int position);
    }

}
