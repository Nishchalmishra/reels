package arivihan.technologies.doubtbuzzter2.listener;/*
 * File: CaseStudyListener Copyright 14/11/24, 1:03 am Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 14/11/24, 1:03 am
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

public interface CaseStudyListener {
    void performClickOnCaseStudy(String caseStudyGroup, String imageUrl,
                                 String caseStudyText, int[] location, int width, int height);

    void openCaseStudyDialog(String imageUrl, String caseStudyText, int[] location, int width, int height);
    void dismissDialogWithAnimation(int[] location, int width, int height);
}
