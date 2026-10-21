package arivihan.technologies.doubtbuzzter2.activity;/*
 * File: FullScreenImageActivity Copyright 25/03/24, 9:28 pm Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 25/03/24, 9:28 pm
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;

public class FullScreenImageActivity extends BaseActivity {

    private ViewPager viewPager;
    private ArrayList<String> imageUrls;
    private int currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        if (savedInstanceState != null) {
            imageUrls = savedInstanceState.getStringArrayList("imageUrls");
            currentPosition = savedInstanceState.getInt("currentPosition", 0);
        } else {
            imageUrls = getIntent().getStringArrayListExtra("imageUrls");
            currentPosition = getIntent().getIntExtra("position", 0);
        }

        viewPager = findViewById(R.id.viewPager);
        FullScreenImageAdapter adapter = new FullScreenImageAdapter();
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentPosition);
    }

    private class FullScreenImageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return imageUrls == null ? 0 : imageUrls.size();
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            LayoutInflater inflater = LayoutInflater.from(container.getContext());
            View view = inflater.inflate(R.layout.item_full_screen_image, container, false);

            ImageView imageView = view.findViewById(R.id.imageViewFullScreen);
            Glide.with(FullScreenImageActivity.this)
                    .load(imageUrls.get(position))
                    .into(imageView);

            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("imageUrls", imageUrls);
        outState.putInt("currentPosition", currentPosition);
    }
}
