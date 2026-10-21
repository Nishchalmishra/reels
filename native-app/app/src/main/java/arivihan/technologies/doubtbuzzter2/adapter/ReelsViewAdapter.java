package arivihan.technologies.doubtbuzzter2.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.MyReelsListener;
import arivihan.technologies.doubtbuzzter2.fragments.ReelsFragment;
import arivihan.technologies.doubtbuzzter2.model.ReelsDto;

/*
 * File: ReelsViewAdapter Copyright 30/5/21 11:00 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 30/5/21 11:00 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

public class ReelsViewAdapter extends FragmentStateAdapter {

    private List<ReelsDto> reelsDtoList;
    private MyReelsListener myReelsListener;

    public ReelsViewAdapter(@NonNull FragmentActivity fragmentActivity, List<ReelsDto> reelsDtoList, MyReelsListener myReelsListener) {
        super(fragmentActivity);
        this.myReelsListener = myReelsListener;
        this.reelsDtoList = reelsDtoList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return ReelsFragment.newInstance(reelsDtoList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return reelsDtoList.size();
    }

    public List<ReelsDto> getReelsDtoList() {
        return reelsDtoList;
    }

    public void setReelsDtoList(List<ReelsDto> reelsDtoList) {
        this.reelsDtoList = reelsDtoList;
    }


}
