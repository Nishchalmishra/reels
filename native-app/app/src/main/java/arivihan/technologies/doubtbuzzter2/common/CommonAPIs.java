package arivihan.technologies.doubtbuzzter2.common;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.CHECK_SUBSCRIPTION;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.HALF_YEARLY_USER_TASK_UPDATE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.PDF_CIRCLE_USAGE_COUNT;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.SEND_FEEDBACK;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.UPDATE_VIDEO_SELECTION;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.USER_FEEDBACK_STATUS;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.USER_FEEDBACK_SUBMIT;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.listener.feedback.OnFeedbackStatusAPIListener;
import arivihan.technologies.doubtbuzzter2.model.ContentFeedbackType;
import arivihan.technologies.doubtbuzzter2.model.HalfYearlyItem;
import arivihan.technologies.doubtbuzzter2.model.HalfYearlyTaskStatus;
import arivihan.technologies.doubtbuzzter2.model.ReelsApiResult;
import arivihan.technologies.doubtbuzzter2.model.UserFeedback;
import arivihan.technologies.doubtbuzzter2.model.feedback.AppFeedbackStatusModel;
import arivihan.technologies.doubtbuzzter2.model.feedback.AppReviewFeedbackModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class CommonAPIs {
    private static final String TAG = CommonAPIs.class.getSimpleName();

    public static void sendAppReviewAPI(Context context, String apiTAG, int stars, List<String> modulesLikes, String feedback) {
        try {
            Gson gson = new Gson();
            AppReviewFeedbackModel model = new AppReviewFeedbackModel();
            model.setStars(stars);
            model.setFeedback(feedback);
            model.setModulesLiked(String.join(",", modulesLikes));

            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                    USER_FEEDBACK_SUBMIT, new JSONObject(gson.toJson(model)),
                    response -> {
                        Log.d(TAG, "Response: " + response.toString());
                    }, error -> {
                Log.d(TAG, "Response: " + error.toString());
            });
            myJsonObjectRequest.setTag(apiTAG);
            MyVolleySingleton.getInstance(context).addToRequestQueue(myJsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public static void getAppReviewStatusAPI(Context context, String apiTAG, OnFeedbackStatusAPIListener listener) {
        try {
            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put(PreferenceKeys.USER_ID.getKey(), MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                    CommonMethods.getUrlParam(USER_FEEDBACK_STATUS, queryParams), null,
                    response -> {
                        Log.d(TAG, "Response: " + response.toString());
                        try {
                            Gson gson = new Gson();
                            TypeToken<AppFeedbackStatusModel> token = new TypeToken<AppFeedbackStatusModel>() {
                            };
                            AppFeedbackStatusModel appFeedbackStatusModel = gson.fromJson(response.toString(), token.getType());
                            if(listener!=null) {
                                listener.onSuccess(true, appFeedbackStatusModel);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            if(listener!=null) {
                                listener.onException(false, null);
                            }
                        }
                    }, error -> {
                Log.d(TAG, "Response: " + error.toString());
                if(listener!=null) {
                    listener.onError(false, null);
                }
            });
            myJsonObjectRequest.setTag(apiTAG);
            MyVolleySingleton.getInstance(context).addToRequestQueue(myJsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
