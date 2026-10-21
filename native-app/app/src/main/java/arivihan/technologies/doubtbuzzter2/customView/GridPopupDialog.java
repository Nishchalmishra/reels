package arivihan.technologies.doubtbuzzter2.customView;

/*
 * File: GridPopupDialog Copyright 8/1/22 5:11 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 8/1/22 5:11 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.MessageFormat;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.GridAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.InstructionHeaderAdapter;
import arivihan.technologies.doubtbuzzter2.listener.GridListener;
import arivihan.technologies.doubtbuzzter2.model.TestSeriesQuestionDto;
import lombok.NonNull;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class GridPopupDialog extends DialogFragment implements GridListener {

    private GridListener dialogListener;
    private List<TestSeriesQuestionDto> list;

    private LinearLayout instructionLayout;
    private InstructionHeaderAdapter instructionHeaderAdapter;

    private TextView answered, marked, unanswered, unseen;
    private CardView markedUnansweredDot, markedAnsweredDot;

    public GridPopupDialog(List<TestSeriesQuestionDto> list, GridListener dialogListener, InstructionHeaderAdapter instructionHeaderAdapter) {
        this.dialogListener = dialogListener;
        this.list = list;
        this.instructionHeaderAdapter = instructionHeaderAdapter;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_dialog_layout, container, false);
        boolean isReviewMode = getActivity().getIntent().getStringExtra(PreferenceKeys.RESPONSE.getKey()).equals(PreferenceKeys.RESPONSE.getKey()) ? true : false;

        instructionLayout = view.findViewById(R.id.instruction);
        answered = view.findViewById(R.id.overview_answerted_text);
        marked = view.findViewById(R.id.overview_marked_text);
        unanswered = view.findViewById(R.id.overview_unanswered_text);
        unseen = view.findViewById(R.id.overview_unseen_text);
        markedAnsweredDot = view.findViewById(R.id.marked_and_answered_dot);
        markedUnansweredDot = view.findViewById(R.id.marked_and_unanswered_dot);
        // Initialize counts for each category
        int answeredCount = 0;
        int markedAnsweredCount = 0;
        int unansweredCount = 0;
        int markedUnansweredCount = 0;

        // Iterate through the list and count based on conditions
        for (TestSeriesQuestionDto item : list) {
            if (item.getUserAnswer() != null) {
                answeredCount++;
                if (item.isMarked()) {
                    markedAnsweredCount++;
                }
            } else {
                unansweredCount++;
                if (item.isMarked()) {
                    markedUnansweredCount++;
                }
            }
        }

// Set the text using the counts
        answered.setText(MessageFormat.format("{0} {1}", answeredCount, getString(R.string.answered)));
        marked.setText(MessageFormat.format("{0} {1}", markedAnsweredCount, getString(R.string.Marked_Answered)));
        unanswered.setText(MessageFormat.format("{0} {1}", unansweredCount, getString(R.string.Unanswered)));
        unseen.setText(MessageFormat.format("{0} {1}", markedUnansweredCount, getString(R.string.Marked_Unanswered)));


        if (isReviewMode) {
            unseen.setVisibility(View.GONE);
            marked.setVisibility(View.GONE);
            markedUnansweredDot.setVisibility(View.GONE);
            markedAnsweredDot.setVisibility(View.GONE);
        }

        RecyclerView gv = view.findViewById(R.id.grid);
        gv.setLayoutManager(new GridLayoutManager(getContext(), 5));
        gv.setNestedScrollingEnabled(false);
        GridAdapter gridAdapter = new GridAdapter(list, this, isReviewMode);
        gv.setAdapter(gridAdapter);
        view.findViewById(R.id.close_overview).setOnClickListener(v -> {
            this.dismiss();
        });

        getDialog().getWindow()
                .getAttributes().windowAnimations = R.style.DialogAnimation;


        instructionLayout.setOnClickListener(v -> {
            RecyclerView instructionRecyclerView = view.findViewById(R.id.instruction_recycler_view);
            ImageView instructionIcon = view.findViewById(R.id.instruction_icon);
            TextView instructionText = view.findViewById(R.id.instruction_text);
            if (gv.getVisibility() == View.VISIBLE) {
                gv.setVisibility(View.GONE);
                instructionIcon.setVisibility(View.GONE);
                instructionText.setText(getString(R.string.questions));
                view.findViewById(R.id.up).setVisibility(View.GONE);
                instructionRecyclerView.setVisibility(View.VISIBLE);
                instructionRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                instructionRecyclerView.setAdapter(instructionHeaderAdapter);
            } else {
                view.findViewById(R.id.up).setVisibility(View.VISIBLE);
                gv.setVisibility(View.VISIBLE);
                instructionRecyclerView.setVisibility(View.GONE);
                instructionIcon.setVisibility(View.VISIBLE);
                instructionText.setText(getString(R.string.instructions));
            }
        });

        return view;
    }

    @Override
    public void clicked(int position) {
        dialogListener.clicked(position);
        this.dismiss();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
//            int width = ViewGroup.LayoutParams.MATCH_PARENT;
//            int height = ViewGroup.LayoutParams.MATCH_PARENT;
//            dialog.getWindow().setLayout(width, height);


        }

    }
}