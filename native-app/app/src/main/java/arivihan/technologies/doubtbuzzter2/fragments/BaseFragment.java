package arivihan.technologies.doubtbuzzter2.fragments;

/*
 * File: BaseFragment Copyright 5/5/22 1:16 AM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 5/5/22 1:16 AM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */


import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.Locale;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.helper.ContextWrapper;

public abstract class BaseFragment extends Fragment {

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        _onAttach(activity);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(updateLocale(context));
        _onAttach(context);
    }

    public abstract void _onAttach(Context context);

    private Context updateLocale(Context newBase) {
        return ContextWrapper.wrap(newBase, MyApplication.getLocale());
    }
}
