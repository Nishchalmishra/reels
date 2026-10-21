package arivihan.technologies.doubtbuzzter2.customView;/*
 * File: MyTabLayout Copyright 25/09/23, 9:56 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 25/09/23, 9:56 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.tabs.TabLayout;

public class MyTabLayout extends TabLayout {
    public MyTabLayout(@NonNull Context context) {
        super(context);
    }

    public MyTabLayout(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyTabLayout(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void addTab(@NonNull Tab tab) {
        super.addTab(tab);

        ViewGroup mainView = (ViewGroup) getChildAt(0);
        ViewGroup tabView = (ViewGroup) mainView.getChildAt(tab.getPosition());

        int tabChildCount = tabView.getChildCount();
        for (int i = 0; i < tabChildCount; i++) {
            View tabViewChild = tabView.getChildAt(i);
            if (tabViewChild instanceof TextView) {
                TextView textTabView = (TextView) tabViewChild;
                textTabView.setText(Html.fromHtml(""));
            }
        }
    }
}
