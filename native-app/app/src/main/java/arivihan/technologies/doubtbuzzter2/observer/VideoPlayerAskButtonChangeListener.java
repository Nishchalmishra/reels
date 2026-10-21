package arivihan.technologies.doubtbuzzter2.observer;/*
 * File: VideoPlayerAskButtonChangeListener Copyright 24/09/24, 8:03 pm Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 24/09/24, 8:03 pm
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import arivihan.technologies.doubtbuzzter2.model.AskButtonState;

public interface VideoPlayerAskButtonChangeListener {
    void onAskButtonStateChanged(AskButtonState newState);
}
