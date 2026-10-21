package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.DELETE_YOUR_ACCOUNT;

import androidx.appcompat.widget.Toolbar;

import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.helper.firebase.FirebaseMessagingManager;
import arivihan.technologies.doubtbuzzter2.model.ApiResponse;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

/*
 * File: DeleteAccountActivity Copyright 31/12/23, 2:19 pm Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 31/12/23, 2:19 pm
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

public class DeleteAccountActivity extends BaseActivity {

    Gson gson = new Gson();
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(getResources().getString(R.string.manage_your_account));

        findViewById(R.id.back_arrow).setOnClickListener((v) -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        progressBar = findViewById(R.id.progress_bar);
        Button delete = findViewById(R.id.delete_account);
        Button cancel = findViewById(R.id.cancel);
        TextInputEditText feedback = findViewById(R.id.delete_reason);

        delete.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            deleteApiCall(feedback);
        });

        cancel.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

    }

    private void deleteApiCall(TextInputEditText feedback) {
        String reason = "";
        if (feedback != null && feedback.getText() != null && StringUtils.isNotEmpty(feedback.getText().toString())) {
            reason = feedback.getText().toString();
        }

        try {

            Map<String, Object> map = new HashMap<>();
            map.put("reason", reason);
            MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.DELETE,
                    CommonMethods.getUrlParam(DELETE_YOUR_ACCOUNT, map), null,
                    response -> {
                        TypeToken<ApiResponse> token = new TypeToken<ApiResponse>() {
                        };
                        ApiResponse apiResponse = gson.fromJson(response.toString(), token.getType());

                        if (apiResponse.isSuccess()) {
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.account_delete.getActionName());
                            FirebaseMessagingManager.unSubscribeToUserDefaultTopics();
                            FirebaseMessagingManager.detachUser(this);
                            deleteAllLocalData();
                            progressBar.setVisibility(View.GONE);
                            MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                            MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
                            Intent homeIntent = new Intent(DeleteAccountActivity.this, UserAuthenticationActivity.class);
                            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(homeIntent);
                            CommonMethods.showPositiveSnackbar(feedback, getString(R.string.SuccessfullLogout));
                            finish();
                        }
                    }, new VolleyGlobalErrorListener(this) {
                @Override
                public void postError(VolleyError error) {

                }
            });
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
        } catch (Exception e) {

        }
    }

    private void deleteAllLocalData() {
        MyApplication.getInstance().getMediaViewModel().clearUserData(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
        MyApplication.getUserSelectedVideoQualityDatabaseReference().setValue(null);
        MyApplication.getUserSelectedDownloadQualityDatabaseReference().setValue(null);
        getViewModelStore().clear();
        MyApplication.clearSharedPref();
        MyApplication.removeFromDoNotDeleteSharedPreference(PreferenceKeys.REFERRAL_CODE.getKey());
        MyApplication.removeFromDoNotDeleteSharedPreference(PreferenceKeys.REFERRAL_SOURCE.getKey());
        MyApplication.removeFromDoNotDeleteSharedPreference(PreferenceKeys.REFERRAL_DETECTED.getKey());
        MyApplication.removeFromDoNotDeleteSharedPreference(PreferenceKeys.SELECTED_COURSE_ID.getKey());
        MyApplication.removeFromDoNotDeleteSharedPreference(PreferenceKeys.DO_NOT_DELETE_SELECTED_COURSE_MODEL.getKey());

    }
}