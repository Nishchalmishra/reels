package arivihan.technologies.doubtbuzzter2.helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;

import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.ArivihanCustomDialog;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class SpeechRecognitionHelper {

    Activity activity;
    Context context;

    public SpeechRecognitionHelper(Activity activity) {
        this.activity = activity;
        this.context = activity;
    }


    SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;

    ArivihanCustomDialog voiceSearchDialog;

    LottieAnimationView lottieAnimationView;

    FloatingActionButton voiceFab;

    TextView txtSpeechInput;

    boolean isResultObtained;

    long recognizerStartTime = 0;

    int recognitionStartFrequency = 0;

    public void setVoiceSearchDialog() {
        voiceSearchDialog = new ArivihanCustomDialog(context);
        voiceSearchDialog.setContentView(R.layout.voice_search_animation_layout);
        voiceSearchDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        voiceSearchDialog.getWindow().setGravity(Gravity.BOTTOM);
        voiceSearchDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        EdgeToEdgeHelper.applyToDialog(voiceSearchDialog);
        lottieAnimationView = voiceSearchDialog.findViewById(R.id.voice_search_animation);
        voiceFab = voiceSearchDialog.findViewById(R.id.fab_voice);

        voiceFab.setOnClickListener((v) -> {
            startListening();
        });
        voiceSearchDialog.findViewById(R.id.close).setOnClickListener(v -> {
            voiceSearchDialog.dismiss();
            speechRecognizer.stopListening();
            lottieAnimationView.pauseAnimation();
        });
        txtSpeechInput = voiceSearchDialog.findViewById(R.id.txt_speech_input);
        voiceSearchDialog.setOnDismissListener(dialog -> {
            speechRecognizer.stopListening();
            txtSpeechInput.setText("");
        });
    }

    public void startListening() {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            isResultObtained = false;
            voiceSearchDialog.show();
            voiceFab.setVisibility(View.GONE);
            lottieAnimationView.setVisibility(View.VISIBLE);
            lottieAnimationView.playAnimation();
            txtSpeechInput.setText(context.getResources().getString(R.string.listening));
            recognizerStartTime = System.currentTimeMillis();
            recognitionStartFrequency = 1;
            speechRecognizer.startListening(speechRecognizerIntent);
        } else {
            askPermission();

        }



    }



    public void askPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO},
                    0);

            if(ContextCompat.checkSelfPermission(context,
                    Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
                startListening();
            }
        }
    }

    public void initializeSpeechRecognition(EditText editText) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
        String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        if (lang == null) {
            lang = "";
        }

        if ("hi-rIN".equals(lang) || "hi".equals(lang)) {
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN");
        } else if ("mr".equals(lang)) {
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "mr-IN");
        } else if ("gu".equals(lang)) {
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "gu-IN");
        } else if ("bn".equals(lang)) {
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-IN");
        } else if ("te".equals(lang)) {
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "te-IN");
        } else if ("kn".equals(lang)) {
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "kn-IN");
        } else if ("ta".equals(lang)) {
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ta-IN");
        } else if ("ml".equals(lang)) {
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ml-IN");
        } else if ("pa".equals(lang)) {
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pa-Guru-IN");
        } else {
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN");
        }

        speechRecognizer.setRecognitionListener(new SpeechRecognitionListenerHelper() {
            @Override
            public void onReadyForSpeechCallback(Bundle params) {
                Log.d("SPEECRECOG", "onReadyForSpeechCallback");
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d("SPEECRECOG", "onBeginningOfSpeech");
                editText.setText("");
                super.onBeginningOfSpeech();
            }

            @Override
            public void onEndOfSpeech() {

                super.onEndOfSpeech();
                Log.d("SPEECRECOG", "onEndOfSpeech before is result obtained");

//                if (!isResultObtained) {
//                    restartVoice("onEndOfSpeech");
//                }
            }

            @Override
            public void onResultsCallback(Bundle params) {
                Log.d("SPEECRECOG", "onResultsCallback");

                isResultObtained = true;
                speechRecognizer.stopListening();
                ArrayList<String> result = params.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                txtSpeechInput.setText(result.get(0));
                Log.d("SPEECRECOG", "onResultsCallback result = " + Arrays.toString(new ArrayList[]{result}));


                voiceSearchDialog.dismiss();
                lottieAnimationView.pauseAnimation();

                editText.setText(result.get(0));
            }

            @Override
            public void onError(int error) {
                Log.d("SPEECRECOG", "onError == " + error);
                if (error == SpeechRecognizer.ERROR_NO_MATCH) {
                    Log.d("SPEECRECOG", "onError == Again starting " + error);

//                    restartVoice("onError");
                }
            }
        });
    }
}
