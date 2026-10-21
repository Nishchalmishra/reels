package arivihan.technologies.doubtbuzzter2.adapter;/*
 * File: ImagePagerAdapter Copyright 29/08/24, 3:15 pm Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 29/08/24, 3:15 pm
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.fragments.ImageFragment;
import arivihan.technologies.doubtbuzzter2.model.CropImageDto;

public class ImagePagerAdapter extends FragmentStatePagerAdapter {

    List<CropImageDto> urls;
    public ImagePagerAdapter(@NonNull FragmentManager fm, List<CropImageDto> urls) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.urls = urls;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return ImageFragment.newInstance(position, urls.get(position));
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        // Return the number of pages in the ViewPager
        return urls.size(); // Example: 3 pages
    }
}
