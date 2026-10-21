package arivihan.technologies.doubtbuzzter2.fragments;/*
 * File: ImageFragment Copyright 29/08/24, 3:16 pm Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 29/08/24, 3:16 pm
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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.CropImageDto;

public class ImageFragment extends Fragment {

    private static final String ARG_POSITION = "position";
    private static final String ARG_URL = "CropImageDto";

    public static ImageFragment newInstance(int position, CropImageDto dto) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        args.putString(ARG_URL, String.valueOf(dto.getUrl()));
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_image, container, false);
        ImageView imageView = view.findViewById(R.id.image_crop_view);
        if (getArguments() != null) {
            String url = getArguments().getString(ARG_URL);
            Glide.with(this).load(url).into(imageView);

        } else {
            Glide.with(this).load("https://picsum.photos/200/300").into(imageView);

        }
        return view;
    }
}
