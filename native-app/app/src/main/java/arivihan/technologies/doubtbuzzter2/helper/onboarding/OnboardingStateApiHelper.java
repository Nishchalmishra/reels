package arivihan.technologies.doubtbuzzter2.helper.onboarding;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.DELETE_USER_ONBOARDING_STATE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_USER_ONBOARDING_STATE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.SAVE_USER_ONBOARDING_STATE;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.model.onboarding.UserOnboardingStateDto;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;

public class OnboardingStateApiHelper {

    private static final String TAG = "OnboardingStateApi";
    private static final Gson gson = new Gson();

    private static final String REMOTE_CONFIG_KEY = "onboarding_resume_flow_v2_enabled";

    // ── Callbacks ────────────────────────────────────────────────────────

    public interface OnStateFetched {
        void onSuccess(@Nullable UserOnboardingStateDto state);
        void onError(String message);
    }

    public interface OnStateSaved {
        void onSuccess();
        void onError(String message);
    }

    public interface OnStateDeleted {
        void onSuccess();
        void onError(String message);
    }

    // ── Remote config guard ──────────────────────────────────────────────

    public static boolean isResumeFlowEnabled() {
        try {
            return MyApplication.getFirebaseRemoteConfig()
                    .getBoolean(REMOTE_CONFIG_KEY);
        } catch (Exception e) {
            return false;
        }
    }

    // ── GET /secure/api/v1/user-state?phoneNumber=xxx ────────────────────

    public static void fetchState(@NonNull String phoneNumber,
                                  @NonNull OnStateFetched callback) {
        if (!isResumeFlowEnabled()) {
            callback.onSuccess(null);
            return;
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("phoneNumber", phoneNumber);

            MyJsonObjectRequest request = new MyJsonObjectRequest(
                    Request.Method.GET,
                    CommonMethods.getUrlParam(GET_USER_ONBOARDING_STATE, params),
                    null,
                    response -> {
                        try {
                            TypeToken<ApiWrappedResponse<UserOnboardingStateDto>> token =
                                    new TypeToken<ApiWrappedResponse<UserOnboardingStateDto>>() {};
                            ApiWrappedResponse<UserOnboardingStateDto> apiResponse =
                                    gson.fromJson(response.toString(), token.getType());

                            if (apiResponse != null && apiResponse.isSuccess()) {
                                callback.onSuccess(apiResponse.getData());
                            } else {
                                callback.onSuccess(null);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "fetchState parse error", e);
                            callback.onError(e.getMessage());
                        }
                    },
                    error -> {
                        Log.e(TAG, "fetchState network error", error);
                        callback.onError(error != null ? error.getMessage() : "Network error");
                    });

            request.setTag(TAG);
            MyVolleySingleton.getInstance(MyApplication.getInstance()).addToRequestQueue(request);

        } catch (Exception e) {
            Log.e(TAG, "fetchState exception", e);
            callback.onError(e.getMessage());
        }
    }

    // ── POST /secure/api/v1/user-state ───────────────────────────────────

    public static void saveState(@NonNull UserOnboardingStateDto state,
                                 @Nullable OnStateSaved callback) {
        if (!isResumeFlowEnabled()) {
            if (callback != null) callback.onSuccess();
            return;
        }

        try {
            JSONObject jsonBody = new JSONObject(gson.toJson(state));

            MyJsonObjectRequest request = new MyJsonObjectRequest(
                    Request.Method.POST,
                    SAVE_USER_ONBOARDING_STATE,
                    jsonBody,
                    response -> {
                        Log.d(TAG, "saveState success");
                        if (callback != null) callback.onSuccess();
                    },
                    error -> {
                        Log.e(TAG, "saveState error", error);
                        if (callback != null)
                            callback.onError(error != null ? error.getMessage() : "Save failed");
                    });

            request.setTag(TAG);
            MyVolleySingleton.getInstance(MyApplication.getInstance()).addToRequestQueue(request);

        } catch (JSONException e) {
            Log.e(TAG, "saveState JSON exception", e);
            if (callback != null) callback.onError(e.getMessage());
        }
    }

    // ── DELETE /secure/api/v1/user-state?phoneNumber=xxx ─────────────────

    public static void deleteState(@NonNull String phoneNumber,
                                   @Nullable OnStateDeleted callback) {
        if (!isResumeFlowEnabled()) {
            if (callback != null) callback.onSuccess();
            return;
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("phoneNumber", phoneNumber);

            MyJsonObjectRequest request = new MyJsonObjectRequest(
                    Request.Method.DELETE,
                    CommonMethods.getUrlParam(DELETE_USER_ONBOARDING_STATE, params),
                    null,
                    response -> {
                        Log.d(TAG, "deleteState success");
                        if (callback != null) callback.onSuccess();
                    },
                    error -> {
                        Log.e(TAG, "deleteState error", error);
                        if (callback != null)
                            callback.onError(error != null ? error.getMessage() : "Delete failed");
                    });

            request.setTag(TAG);
            MyVolleySingleton.getInstance(MyApplication.getInstance()).addToRequestQueue(request);

        } catch (Exception e) {
            Log.e(TAG, "deleteState exception", e);
            if (callback != null) callback.onError(e.getMessage());
        }
    }

    // ── Cleanup ──────────────────────────────────────────────────────────

    public static void cancelAll() {
        MyVolleySingleton.getInstance(MyApplication.getInstance())
                .getRequestQueue().cancelAll(TAG);
    }
}
