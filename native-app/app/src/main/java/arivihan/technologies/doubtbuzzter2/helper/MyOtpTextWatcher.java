package arivihan.technologies.doubtbuzzter2.helper;

/*
 * File: MyOtpTextWatcher Copyright 31/12/21 9:18 AM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 31/12/21 9:18 AM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import java.util.List;

public class MyOtpTextWatcher  implements TextWatcher  {

    private final EditText etNext;
    private final EditText etPrev;
    private final EditText current;

    private List<EditText> otpFields;


    public MyOtpTextWatcher(EditText current, EditText etPrev, EditText etNext,List<EditText> otpFields)
    {
        this.etPrev=etPrev;
        this.etNext=etNext;
        this.current = current;
        this.otpFields = otpFields;
    }

    @Override
    public void afterTextChanged(Editable editable) {
        String text = editable.toString();

        if (etNext != null && text.length() == 1) {
            etNext.requestFocus();
        }
        if(text.length() >1){
            if(text.length() == 6){
                for (int i = 0; i < 6; i++) {
                    otpFields.get(i).setText(String.valueOf(text.charAt(i)));
                }
            }else{
                current.setText(String.valueOf(text.charAt(0)));
                current.setSelection(1);
                etNext.setText(String.valueOf(text.charAt(1)));
            }
        }
        if (text.length() == 0 && etPrev !=  null) {
            etPrev.requestFocus();
            etPrev.setSelection(etPrev.getText().length());
        }


    }

    @Override
    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
    }

    @Override
    public void onTextChanged(CharSequence s, int arg1, int arg2, int count) {
    }

}
