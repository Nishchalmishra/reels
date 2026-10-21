package arivihan.technologies.doubtbuzzter2.listener;

/*
 * File: OptionListener Copyright 14/2/21 1:28 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 14/2/21 1:28 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import android.view.View;

import arivihan.technologies.doubtbuzzter2.model.TestSeriesQuestionDto;

public interface OptionListener {
    default void setSelected(int i) {

    }
    default void setSelected(int i, String answer) {

    }

    default int getSelected() {
        return 0;
    }

    TestSeriesQuestionDto getDto(int fragNumber);

    void submitAnswer();

    void nextPage(int i);
    default void stopTimer(){};
    default void startTimer(){};
    default void onViewSolutionClick(){};
    default void onBottomSheetOpen(){};
    default void onBottomSheetClose(){};
}
