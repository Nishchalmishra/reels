package arivihan.technologies.doubtbuzzter2.adapter;

/*
 * File: MySpinnerArrayAdapter Copyright 2/1/22 8:57 AM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 2/1/22 8:57 AM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.listener.SpinnerListener;

public class MySpinnerArrayAdapter extends ArrayAdapter<String> implements AdapterView.OnItemSelectedListener {
    private final MySpinnerType spinnerType;
    private final SpinnerListener listener;

    public MySpinnerArrayAdapter(Context context, int resourceId, List<String> objects,
                                 MySpinnerType spinnerType, SpinnerListener listener) {
        super(context, resourceId, objects);
        this.spinnerType = spinnerType;
        this.listener = listener;
    }

    public void setOnSelectedListener(Spinner spinner) {
        spinner.setOnItemSelectedListener(this);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        listener.updateSelectionOfType(position, spinnerType);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        listener.onNothingSelected(spinnerType);
    }
}
