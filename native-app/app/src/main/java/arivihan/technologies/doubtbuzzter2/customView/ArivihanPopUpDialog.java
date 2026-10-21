package arivihan.technologies.doubtbuzzter2.customView;

/*
 * File: ArivihanPopUpDialog Copyright 6/6/21 12:53 AM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 6/6/21 12:53 AM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import com.airbnb.lottie.LottieAnimationView;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.StreakQuestionActivity;
import arivihan.technologies.doubtbuzzter2.listener.ButtonAListener;

public class ArivihanPopUpDialog extends AlertDialog implements android.view.View.OnClickListener{

    public Activity c;
    public Dialog d;
    public CardView yes;
    public Button no;
    private LottieAnimationView lottieAnimationView;
    private TextView textView;
    private ButtonAListener buttonAListener;

    public ArivihanPopUpDialog(Activity a) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
    }


    public ArivihanPopUpDialog(@NonNull Context context) {
        super(context);
    }


    public ArivihanPopUpDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.arivihan_dialog);
        yes = findViewById(R.id.btn_dialog_a);
        textView = findViewById(R.id.text_dialog);
        lottieAnimationView = findViewById(R.id.lottie);
        //lottieAnimationView.setAnimation(R.raw.lottie_loader);
        yes.setOnClickListener(this);
       // no.setOnClickListener(this);
    }


    public void setBodyText(String text) {
        textView.setText(text);
    }


    public void setButtonAText(String text) {
        //yes.setText(text);
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
            case R.id.btn_dialog_a:
                if (buttonAListener != null) {
                    buttonAListener.onButtonAListener();
                }
                break;
            default:
                break;
        }
        dismiss();
    }

    public void showDialog(Activity activity, String msg, String lottieResource){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.arivihan_dialog);

//        TextView text = (TextView) dialog.findViewById(R.id.text_dialog);
//        text.setText(msg);
//
//        Button dialogButton = (Button) dialog.findViewById(R.id.btn_dialog);
//        dialogButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });

        dialog.show();

    }
}
