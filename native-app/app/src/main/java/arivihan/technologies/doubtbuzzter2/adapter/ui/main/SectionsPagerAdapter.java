package arivihan.technologies.doubtbuzzter2.adapter.ui.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.model.UserDailyRoutineDto;


/*
 * File: SectionsPagerAdapter Copyright 19/07/23, 1:28 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 19/07/23, 1:28 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

    private List<String> tabList;
    private List<List<UserDailyRoutineDto>> routineDtos;
    private final Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm, List<String> tabList,
                                List<List<UserDailyRoutineDto>> userDailyRoutineDtos) {
        super(fm);
        mContext = context;
        this.tabList = tabList;
        this.routineDtos = userDailyRoutineDtos;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a ToDoListFragment (defined as a static inner class below).
        return ToDoListFragment.newInstance(position, routineDtos.get(position));
    }

//    public void notifyData() {
//        for (ToDoListFragment fragment : fragments) {
//            fragment.updateData();
//        }
//
//    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
//        return tabList.get(position);
        return null;
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return routineDtos.size();
    }
}