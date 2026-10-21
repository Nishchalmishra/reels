package arivihan.technologies.doubtbuzzter2.helper;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.PROFILE_IMAGE_UPLOAD;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.common.MultipartRequestDataPart;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.model.generic.ProfileImageUploadResponse;
import arivihan.technologies.doubtbuzzter2.volley.MyMultipartRequest;

public class ProfileImageUploadHelper {

    private static final String TAG = "ProfileImageUploadHelper";
    private static final int UPLOAD_TIMEOUT_MS = 120_000;
    private static final int JPEG_QUALITY = 70;

    public interface OnProfileImageUploadListener {
        void onSuccess(String imageUrl);
        void onFailure(String errorMessage);
    }

    public static void uploadProfileImage(Activity activity, Bitmap bitmap, OnProfileImageUploadListener listener) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        if (bitmap == null) {
            if (listener != null) listener.onFailure("No image to upload");
            return;
        }

        WeakReference<Activity> activityRef = new WeakReference<>(activity);

        byte[] imageData = compressBitmap(bitmap);
        if (imageData == null || imageData.length == 0) {
            if (listener != null) listener.onFailure("Failed to compress image");
            return;
        }

        MyMultipartRequest multipartRequest = new MyMultipartRequest(
                Request.Method.POST,
                PROFILE_IMAGE_UPLOAD,
                response -> {
                    Activity act = activityRef.get();
                    if (act == null || act.isFinishing() || act.isDestroyed()) return;

                    try {
                        String responseBody = new String(response.data, StandardCharsets.UTF_8);
                        Log.d(TAG, "Response: " + responseBody);

                        Gson gson = new Gson();
                        TypeToken<ProfileImageUploadResponse> token = new TypeToken<ProfileImageUploadResponse>() {};
                        ProfileImageUploadResponse uploadResponse = gson.fromJson(responseBody, token.getType());

                        if (uploadResponse != null && uploadResponse.isSuccess()
                                && uploadResponse.getData() != null) {
                            if (listener != null) listener.onSuccess(uploadResponse.getData());
                        } else {
                            String msg = (uploadResponse != null && uploadResponse.getMessage() != null)
                                    ? uploadResponse.getMessage()
                                    : "Image upload failed";
                            if (listener != null) listener.onFailure(msg);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing response", e);
                        if (listener != null) listener.onFailure("Failed to parse upload response");
                    }
                },
                error -> {
                    Activity act = activityRef.get();
                    if (act == null || act.isFinishing() || act.isDestroyed()) return;

                    String errorMsg = getVolleyErrorMessage(error);
                    Log.e(TAG, "Upload error: " + errorMsg);
                    if (listener != null) listener.onFailure(errorMsg);
                }
        ) {
            @Override
            protected Map<String, MultipartRequestDataPart> getByteData() {
                Map<String, MultipartRequestDataPart> params = new HashMap<>();
                params.put("file", new MultipartRequestDataPart("profile_"+MyApplication.getStringData(PreferenceKeys.USER_ID.getKey())+"_"+System.currentTimeMillis()+".jpg", imageData, "image/jpeg"));
                return params;
            }
        };

        multipartRequest.setTag(TAG);
        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                UPLOAD_TIMEOUT_MS,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Volley.newRequestQueue(activity).add(multipartRequest);
    }

    private static byte[] compressBitmap(Bitmap bitmap) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            Log.e(TAG, "Bitmap compression failed", e);
            return null;
        }
    }

    private static String getVolleyErrorMessage(VolleyError error) {
        if (error == null) return "Unknown error";
        if (error.networkResponse != null) {
            try {
                String body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                return "Server error (" + error.networkResponse.statusCode + "): " + body;
            } catch (Exception ignored) {}
            return "Server error: " + error.networkResponse.statusCode;
        }
        if (error.getMessage() != null) return error.getMessage();
        return "Network error";
    }
}
