package arivihan.technologies.doubtbuzzter2.helper;

/*
 * File: SpeechRecognitionListenerHelper Copyright 15/2/21 8:56 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 15/2/21 8:56 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import android.os.Bundle;
import android.speech.RecognitionListener;


public abstract class SpeechRecognitionListenerHelper implements RecognitionListener {

    public abstract void onReadyForSpeechCallback(Bundle params);
    public abstract void onResultsCallback(Bundle params);




    @Override
    public void onReadyForSpeech(Bundle params) {
        onReadyForSpeechCallback(params);
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int error) {

    }

    @Override
    public void onResults(Bundle results) {
        onResultsCallback(results);
    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }
}
