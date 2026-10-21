package arivihan.technologies.doubtbuzzter2.helper;

/*
 * File: APIHelper Copyright 14/4/22 5:12 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 14/4/22 5:12 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.model.NotificationBody;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.NOTIFICATION;

public class NotificationHelper {

    private final Context context;
    public NotificationHelper(Context context) {
        this.context = context;
    }

    public Gson gson = new Gson();

    public void sendNotification(NotificationBody body) {
        body.setTopic("availableNow");
        final Map<String, Object> map = new HashMap<>();

        MyJsonObjectRequest myJsonObjectRequest = null;
        try {
            myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                    CommonMethods.getUrlParam(NOTIFICATION, map), new JSONObject(gson.toJson(body)),
                    response -> {

                        Log.d("notifyDoubt", "success");
                    }, new VolleyGlobalErrorListener(context) {
                @Override
                public void postError(VolleyError error) {
                    Log.d("notifyDoubt", "Error" +  error.getMessage());
                }
            });
            MyVolleySingleton.getInstance(context).addToRequestQueue(myJsonObjectRequest);
        } catch (JSONException e) {
            //e.printStackTrace();
        }
    }


}
