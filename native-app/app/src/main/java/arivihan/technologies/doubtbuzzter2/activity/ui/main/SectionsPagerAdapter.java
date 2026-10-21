package arivihan.technologies.doubtbuzzter2.activity.ui.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.fragments.EditRoutineFragment;
import arivihan.technologies.doubtbuzzter2.model.UserSyllabusOrder;


/*
 * File: SectionsPagerAdapter Copyright 23/08/23, 12:49 AM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 23/08/23, 12:49 AM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private final Context mContext;
    private final List<String> tabList;
    private Map<String, List<UserSyllabusOrder>> datas;

    public SectionsPagerAdapter(Context context, FragmentManager fm, List<String> tabList,
                                Map<String, List<UserSyllabusOrder>> datas) {
        super(fm);
        mContext = context;
        this.tabList = tabList;
        this.datas = datas;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a EditFragment (defined as a static inner class below).
        return EditRoutineFragment.newInstance(mContext, datas.get(tabList.get(position)));
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return tabList.get(position);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return tabList.size();
    }
}