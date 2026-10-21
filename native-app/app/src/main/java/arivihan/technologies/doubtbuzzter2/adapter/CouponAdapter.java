package arivihan.technologies.doubtbuzzter2.adapter;

/*
 * File: CouponAdapter Copyright 22/3/22 5:01 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 22/3/22 5:01 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.CouponDto;

public class CouponAdapter  extends RecyclerView.Adapter<CouponAdapter.ViewHolder> {

    List<CouponDto> couponDtoList ;
    CouponListener couponListener;
    public CouponAdapter(List<CouponDto> couponDtoList, CouponListener couponListener) {
        this.couponDtoList = couponDtoList;
        this.couponListener = couponListener;
    }

    @NonNull
    @Override
    public CouponAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.coupon_list_item,parent,false);
        return new CouponAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CouponAdapter.ViewHolder holder, int position) {
        holder.couponCode.setText(couponDtoList.get(position).getCode());
        holder.info.setText(couponDtoList.get(position).getDescription());
        holder.apply.setOnClickListener(view -> {
            couponListener.verifyCoupon(couponDtoList.get(position).getId(), null);
        });
    }

    @Override
    public int getItemCount() {
        return couponDtoList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView couponCode, info;
        private Button apply;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            couponCode = itemView.findViewById(R.id.coupon_code);
            info = itemView.findViewById(R.id.description);
            apply = itemView.findViewById(R.id.couponApply);
        }
    }

    public interface CouponListener {
        void verifyCoupon(String id, String code);
    }
}
