package arivihan.technologies.doubtbuzzter2.fragments;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_COUPONS;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.VERIFY_COUPON;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.CouponAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.model.CouponDto;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

/*
 * File: CouponFragment Copyright 21/3/22 3:49 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 21/3/22 3:49 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 *
 */

public class CouponFragment extends Fragment implements CouponAdapter.CouponListener {

    private String planId;
    private List<CouponDto> couponDtoList = new ArrayList<>();
    private RecyclerView couponRecyclerview;
    ProgressBar progressBar;
    private CouponAdapter couponAdapter;
    private Gson gson = new Gson();
    private TextInputEditText coupon_code;
    private Button applyButton;
    private boolean clicked;

    Correct_Answer_AnimationFragment bottomSheetDialogFragment = new Correct_Answer_AnimationFragment();
    Wrong_CoupanFragment bottomSheetDialogFragmentWrong = new Wrong_CoupanFragment();

    @Override
    public void onPause() {
        super.onPause();
        if(bottomSheetDialogFragment.isVisible())
            bottomSheetDialogFragment.dismiss();
        if(bottomSheetDialogFragmentWrong.isVisible())
            bottomSheetDialogFragmentWrong.dismiss();
    }

    public CouponFragment(String planId) {
        this.planId = planId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_coupon, container, false);
        couponRecyclerview = view.findViewById(R.id.couponlist);
        couponRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
        couponAdapter = new CouponAdapter(couponDtoList, this::verifyCoupon);
        couponRecyclerview.setAdapter(couponAdapter);
        clicked = false;
        getCoupon();
        coupon_code = view.findViewById(R.id.coupon_code);
        progressBar = view.findViewById(R.id.progress_dataLoading);
        applyButton = view.findViewById(R.id.apply_button);
        applyButton.setOnClickListener(view1 -> {
            if (coupon_code.getText().length() == 0) {
                coupon_code.setError("Invalid coupon code");
            } else {
                applyButton.setVisibility(View.GONE);
                verifyCoupon("", coupon_code.getText().toString());
            }
        });
        return view;
    }

    private void getCoupon() {
        Map<String, Object> map = new HashMap<>();
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_COUPONS, map), null,
                response -> {
                    TypeToken<List<CouponDto>> token = new TypeToken<List<CouponDto>>() {};
                    try {
                       List<CouponDto> data = gson.fromJson(response.getJSONArray("data").toString(), token.getType());
                       if (CollectionUtils.isEmpty(data)) {
                           CommonMethods.showPositiveSnackbar(couponRecyclerview, "No Coupon available");
                       } else {
                           couponDtoList.addAll(data);
                           progressBar.setVisibility(View.GONE);
                           couponRecyclerview.setVisibility(View.VISIBLE);
                           couponAdapter.notifyDataSetChanged();
                       }

                    } catch (Exception e) {
                        progressBar.setVisibility(View.GONE);
                        CommonMethods.showPositiveSnackbar(couponRecyclerview, "No Coupon available");

                    }
                }, new VolleyGlobalErrorListener(getActivity()) {
            @Override
            public void postError(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                CommonMethods.showNegativeSnackbar(couponRecyclerview, "Sorry Coupon not found.");
            }
        });
        MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(myJsonObjectRequest2);
    }

    @Override
    public void verifyCoupon(String id, String code) {
        if (clicked) {
            CommonMethods.showPositiveSnackbar(couponRecyclerview, "Please wait.");
            return;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("planId", planId);
        map.put("id", id);
        map.put("code", code);
        clicked = true;

        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(VERIFY_COUPON, map), null,
                response -> {
                    try {
                        boolean success = (boolean) response.get("success");
                        if (success) {
                            CommonMethods.showPositiveSnackbar(couponRecyclerview, "Coupon applied successfully");
                            TypeToken<CouponDto> token = new TypeToken<CouponDto>() {};
                            CouponDto data = gson.fromJson(response.getJSONObject("data").toString(), token.getType());
                            MyApplication.saveData(PreferenceKeys.COUPON_ID.getKey(), data.getId());
                            MyApplication.saveData(PreferenceKeys.DISCOUNT.getKey(), data.getDiscountApplied());
                            bottomSheetDialogFragment.show(getParentFragmentManager(), "Add_Fragment");
                            new Handler().postDelayed(() -> getParentFragmentManager().popBackStack(), 1000);
                        } else {
                            clicked = false;
                            applyButton.setVisibility(View.VISIBLE);
                            bottomSheetDialogFragmentWrong.show(getParentFragmentManager(), "Add_Fragment");
                        }
                    } catch (JSONException e) {
                        clicked = false;
                        e.printStackTrace();
                        bottomSheetDialogFragmentWrong.show(getParentFragmentManager(), "Add_Fragment");

                    }

                }, new VolleyGlobalErrorListener(getActivity()) {
            @Override
            public void postError(VolleyError error) {
                clicked = false;
                CommonMethods.showNegativeSnackbar(couponRecyclerview, error.getMessage());
            }
        });
        MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(myJsonObjectRequest2);
    }
}