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
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.listener.OptionListener;
import arivihan.technologies.doubtbuzzter2.model.OptionDto;
import arivihan.technologies.doubtbuzzter2.model.QuestionType;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class OptionAdapter extends RecyclerView.Adapter<OptionAdapter.OptionViewHolder> {

    private List<OptionDto> list;
    private Context context;
    private OptionListener optionListener;
    private String[] option = {"A) ", "B) ", "C) ", "D) ", "E) ", "F) ", "G) ", "H) ", "I) "};
    private String[] optionMultiCorrect = {"a", "b", "c", "d", "e", "f", "g", "h", "i"};
    private QuestionType questionType;
    private String questionId = "NA";
    private String question = "NA";
    private String TAG="adapterIssue";

    public OptionAdapter(List<OptionDto> list, QuestionType questionTypeEnum, Context context1, OptionListener optionListener) {
        Log.d(TAG, "OptionAdapter constructor called");

        if (list != null) {
            Log.d(TAG, "List size: " + list.size()+"listcontent---"+" "+list);
        } else {
            Log.d(TAG, "List is null");
        }

        Log.d(TAG, "QuestionType: " + (questionTypeEnum != null ? questionTypeEnum.name() : "null"));
        Log.d(TAG, "Context: " + (context1 != null ? context1.getClass().getSimpleName() : "null"));
        Log.d(TAG, "OptionListener: " + (optionListener != null ? optionListener.getClass().getSimpleName() : "null"));

        this.list = list;
        this.context = context1;
        this.optionListener = optionListener;
        this.questionType = questionTypeEnum;

        Log.d(TAG, "OptionAdapter initialized successfully");
    }

    public OptionAdapter(String questionId, String question, List<OptionDto> list,
                         QuestionType questionTypeEnum, Context context1, OptionListener optionListener) {
        this(list, questionTypeEnum, context1, optionListener);
        this.questionId = questionId;
        this.question = question;
    }

    @NonNull
    @Override
    public OptionAdapter.OptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.question_option_item, parent, false);
        return new OptionAdapter.OptionViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull final OptionAdapter.OptionViewHolder holder, int position) {

        if (list.get(position).getOptionText() != null && "nan".equals(list.get(position).getOptionText().toLowerCase())) {
            holder.optionText.setVisibility(View.GONE);
        } else {
            holder.optionNumber.setText(option[position]);
            holder.optionText.setText(String.valueOf(list.get(position).getOptionText()));
        }

        if (list.get(position).getOptionType() != null) {
            if (list.get(position).getOptionType().equals("IMAGE")) {
                holder.optionText.setVisibility(View.GONE);
                Glide.with(context).load(list.get(position).getOptionText()).into(holder.OptionImage);
                holder.OptionImage.setVisibility(View.VISIBLE);
            }
        } else {
            holder.OptionImage.setVisibility(View.GONE);
        }

        if (list.get(position).isSelected()) {
            holder.singleOption.setCardBackgroundColor(context.getResources().getColor(R.color.selected));
            holder.singleOptionContainer.setBackground(context.getResources().getDrawable(R.drawable.question_option_border_teal));
        } else {
            holder.singleOption.setCardBackgroundColor(context.getResources().getColor(R.color.white));
            holder.singleOptionContainer.setBackground(context.getResources().getDrawable(R.drawable.question_option_border));
        }

        String attemp = MyApplication.getStringData(PreferenceKeys.RESPONSE.getKey());

        if (StringUtils.isNotEmpty(attemp) && attemp.equals(PreferenceKeys.RESPONSE.getKey())) {
            if (questionType == QuestionType.MULTI_CHOICE) {
                HashSet<String> hash = new HashSet<>();
                String ans = list.get(position).getAnswer();
                String temp = "";
                for (int i = 0; i < ans.length(); i++) {
                    if (ans.charAt(i) == ',') {
                        hash.add(temp);
                        temp = "";
                    } else
                        temp += ans.charAt(i);
                }
                hash.add(temp);
                Log.i("HashSet ", "  " + hash);
                if (hash.contains(optionMultiCorrect[position])) {
                    holder.done.setVisibility(View.VISIBLE);
                    holder.optionNumber.setTextColor(ContextCompat.getColor(context, R.color.correct_answer_text_color));
                    holder.optionText.setTextColor(ContextCompat.getColor(context, R.color.correct_answer_text_color));
                }
            } else if (questionType == QuestionType.MULTI_CHOICE_IMAGE) {
                HashSet<Integer> hash = new HashSet<>();
                String temp = "";
                for (int i = 0; i < list.get(position).getAnswer().length(); i++) {
                    if (list.get(position).getAnswer().charAt(i) == ',') {
                        double d;
                        try {
                            d = Double.parseDouble(temp);
                        } catch (Exception e) {
                            d = 0;
                        }
                        int x = (int) d;
                        hash.add(x - 1);
                        temp = "";
                    } else if (list.get(position).getAnswer().charAt(i) != ' ') {
                        temp += list.get(position).getAnswer().charAt(i);
                    }
                }
                if (hash.contains(position)) {
                    holder.done.setVisibility(View.VISIBLE);
                    holder.optionNumber.setTextColor(ContextCompat.getColor(context, R.color.correct_answer_text_color));
                    holder.optionText.setTextColor(ContextCompat.getColor(context, R.color.correct_answer_text_color));

//                    holder.singleOption.setCardBackgroundColor(ContextCompat.getColor(context, R.color.correct_option_card_bg));
//                    holder.singleOptionContainer.setBackground(context.getResources().getDrawable(R.drawable.question_option_border_green));
                }
            } else if (questionType == QuestionType.SINGLE_CHOICE_IMAGE) {
                String s = list.get(position).getAnswer();
                double d;
                try {
                    d = Double.parseDouble(s);
                } catch (Exception e) {
                    d = 0;
                }
                int x = (int) d;
                x = x - 1;
                String s1 = list.get(position).getAttempted();
                int x1 = -1;
                if (s1 != null) {
                    double d1 = Double.parseDouble(s1);
                    x1 = (int) d1;
                    x1 = x1 - 1;
                }
                if (position == x) {
                    holder.singleOption.setCardBackgroundColor(ContextCompat.getColor(context, R.color.correct_option_card_bg));
                    holder.singleOptionContainer.setBackground(context.getResources().getDrawable(R.drawable.question_option_border_green));
                } else if (x1 != x && position == x1) {
                    holder.singleOption.setCardBackgroundColor(ContextCompat.getColor(context, R.color.wrong_option_card_bg));
                    holder.singleOptionContainer.setBackground(context.getResources().getDrawable(R.drawable.question_option_border_red));
                }
            } else {
                if (list.get(position).getOptionText().equals(list.get(position).getAnswer())) {
                    holder.singleOption.setCardBackgroundColor(ContextCompat.getColor(context, R.color.correct_option_card_bg));
                    holder.singleOptionContainer.setBackground(context.getResources().getDrawable(R.drawable.question_option_border_green));
                } else if (list.get(position).getOptionText().equals(list.get(position).getAttempted())) {
                    holder.singleOption.setCardBackgroundColor(ContextCompat.getColor(context, R.color.wrong_option_card_bg));
                    holder.singleOptionContainer.setBackground(context.getResources().getDrawable(R.drawable.question_option_border_red));
                }
            }
        }


        holder.optionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!attemp.equals(PreferenceKeys.RESPONSE.getKey())) {
                    if (questionType == QuestionType.SINGLE_CHOICE || questionType == QuestionType.SINGLE_CHOICE_IMAGE) {
                        updateSingleChoice(position);
                    } else {
                        updateMultiChoice(position);
                    }
                    notifyDataSetChanged();
                }
            }
        });


    }

    private void updateMultiChoice(int position) {
        list.get(position).setSelected(!list.get(position).isSelected());
    }

    private void updateSingleChoice(int position) {


        for (int i = 0; i < list.size(); i++) {
            if (i == position && list.get(i).isSelected()) {
                list.get(i).setSelected(false);
                optionListener.setSelected(-1);
                optionListener.setSelected(-1, list.get(position).getOptionText());
            } else {
                if (i == position) {
                    list.get(i).setSelected(true);
                    optionListener.setSelected(position, list.get(position).getOptionText());
                } else {
                    list.get(i).setSelected(false);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class OptionViewHolder extends RecyclerView.ViewHolder {
        private TextView optionText;
        private RelativeLayout optionLayout;
        private TextView optionNumber;
        private ImageView done, wrong, OptionImage;

        private CardView singleOption;
        private LinearLayout singleOptionContainer;

        public OptionViewHolder(@NonNull View itemView) {
            super(itemView);
            optionText = itemView.findViewById(R.id.OP);
            optionLayout = itemView.findViewById(R.id.option_layout);
            optionNumber = itemView.findViewById(R.id.Option);
            done = itemView.findViewById(R.id.done);
            OptionImage = itemView.findViewById(R.id.OPImage);
            wrong = itemView.findViewById(R.id.wrong);
            singleOption = itemView.findViewById(R.id.single_option_card);
            singleOptionContainer = itemView.findViewById(R.id.single_option_card_container);

        }
    }
}
