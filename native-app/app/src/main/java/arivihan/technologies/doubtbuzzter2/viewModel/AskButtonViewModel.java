package arivihan.technologies.doubtbuzzter2.viewModel;/*
 * File: AskButtonViewModel Copyright 24/09/24, 8:05 pm Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 24/09/24, 8:05 pm
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import arivihan.technologies.doubtbuzzter2.model.AskButtonState;
import arivihan.technologies.doubtbuzzter2.observer.VideoPlayerAskButtonChangeListener;

public class AskButtonViewModel {
    private AskButtonState currentState = AskButtonState.STATE_ASK;
    private final VideoPlayerAskButtonChangeListener listener;

    // Constructor to attach the listener
    public AskButtonViewModel(VideoPlayerAskButtonChangeListener listener) {
        this.listener = listener;
    }

    // Method to change the state and notify the listener
    public void changeState(AskButtonState newState) {
        currentState = newState;
        if (listener != null) {
            listener.onAskButtonStateChanged(currentState); // Notify listener about state change
        }
    }

    public AskButtonState getCurrentState() {
        return currentState;
    }
}
