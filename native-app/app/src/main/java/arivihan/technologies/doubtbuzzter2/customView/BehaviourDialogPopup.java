package arivihan.technologies.doubtbuzzter2.customView;

/*
 * File: BehaviourDialogPopup Copyright 27/6/21 12:50 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 27/6/21 12:50 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.airbnb.lottie.LottieAnimationView;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.listener.ButtonAListener;

public class BehaviourDialogPopup extends AlertDialog implements android.view.View.OnClickListener{

    public Activity c;
    public Dialog d;
    public Button yes, no, skip;
    private LottieAnimationView lottieAnimationView;
    private TextView textView;
    private ButtonAListener buttonAListener;

    public BehaviourDialogPopup(Activity a) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
    }


    public BehaviourDialogPopup(@NonNull Context context) {
        super(context);
    }


    public BehaviourDialogPopup(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.arivihan_dialog_question);
        yes = (Button) findViewById(R.id.btn_dialog_yes);
        no = (Button) findViewById(R.id.btn_dialog_no);
        textView = findViewById(R.id.text_dialog);
        lottieAnimationView = findViewById(R.id.lottie);
        setCancelable(false);
        skip = findViewById(R.id.skip);
        //lottieAnimationView.setAnimation(R.raw.lottie_loader);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);
        skip.setOnClickListener(this);
    }

    public void cancelAnimation() {
        lottieAnimationView.cancelAnimation();
    }

    public void startAnimation() {
        lottieAnimationView.playAnimation();
    }


    public void setBodyText(String text) {
        textView.setText(text);
    }


    public void setButtonAText(String text) {
        yes.setText(text);
    }

    public void setButtonBText(String text) {
        no.setText(text);
    }

    public void setAnimationResource(int resource) {
        lottieAnimationView.setAnimation(c.getResources().openRawResource(resource), null);
    }

    public void onButtonAClickListener(ButtonAListener a) {
        buttonAListener = a;
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_dialog_yes:
                if (buttonAListener != null) {
                    buttonAListener.onButtonAListener();
                }
                break;
            case R.id.btn_dialog_no:
                if (buttonAListener != null) {
                    buttonAListener.onButtonBListener();
                }
                break;
            case R.id.skip:
                dismiss();
                break;
            default:
                break;
        }
    }
}
