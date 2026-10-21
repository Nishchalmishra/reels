package arivihan.technologies.doubtbuzzter2.adapter;/*
 * File: ImagePeekCropAdapter Copyright 29/08/24, 4:56 pm Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 29/08/24, 4:56 pm
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.CropImageDto;

public class ImagePeekCropAdapter extends RecyclerView.Adapter<ImagePeekCropAdapter.ImagePeekViewHolder> {

    List<CropImageDto> urlList;
    Context context;
    PeekListener peekListener;
    public ImagePeekCropAdapter(List<CropImageDto> urlList, Context context, PeekListener peekListener) {
        this.urlList = urlList;
        this.context = context;
        this.peekListener = peekListener;
    }

    @NonNull
    @Override
    public ImagePeekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.peek_recycler_view_item, parent, false);
        return new ImagePeekViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImagePeekViewHolder holder, int position) {
        CropImageDto cropImageDto = urlList.get(position);
        if (cropImageDto.isSelected()) {
            holder.cropLayout.setBackground(AppCompatResources.getDrawable(context, R.drawable.border_white));
        } else {
            holder.cropLayout.setBackground(null);
        }
//        holder.peekDeleteView.setOnClickListener(view -> {
//            if (cropImageDto.isSelected()) {
//                peekListener.deletePeekImage(position);
//            }
//        });
        holder.cropLayout.setOnClickListener(view -> {
            if (cropImageDto.isSelected()) {
                peekListener.deletePeekImage(position);
            } else {
                peekListener.changePosition(position);
            }
        });
        Glide.with(context).load(cropImageDto.getUrl()).into(holder.peekImageView);
    }

    @Override
    public int getItemCount() {
        return urlList.size();
    }

    static class ImagePeekViewHolder extends RecyclerView.ViewHolder {

        ImageView peekImageView, peekDeleteView;
        RelativeLayout cropLayout;
        public ImagePeekViewHolder(View itemView) {
            super(itemView);
            cropLayout = itemView.findViewById(R.id.crop_layout);
            peekImageView = itemView.findViewById(R.id.peek_image_view);
            peekDeleteView = itemView.findViewById(R.id.peek_item_delete);
        }
    }

    public interface PeekListener {
        void deletePeekImage(int pos);

        void changePosition(int position);
    }
}
