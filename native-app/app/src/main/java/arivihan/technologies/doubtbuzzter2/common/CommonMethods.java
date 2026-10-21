package arivihan.technologies.doubtbuzzter2.common;

import static androidx.media3.common.util.Util.getDrawable;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.CALL_ME_BACK;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.UPDATE_UNWATCHED_VIDEO;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.internal.StringUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.CampaignSubscriptionActivity;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.customView.DialogListener;

import arivihan.technologies.doubtbuzzter2.common.feedback.AppReviewDialog;
import arivihan.technologies.doubtbuzzter2.enums.ChapterEnum.ChapterStatus;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.LocalNavigationType;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.helper.EnvironmentManager;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.listener.ApiCallBackListener;
import arivihan.technologies.doubtbuzzter2.listener.dialog.DialogPositiveNegativeOptionClickListener;
import arivihan.technologies.doubtbuzzter2.customView.PermissionRequestView;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.model.ApiResponse;
import arivihan.technologies.doubtbuzzter2.model.NavigationConfig;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionLockPopupDto;
import arivihan.technologies.doubtbuzzter2.model.asat.CallBackResponse;
import arivihan.technologies.doubtbuzzter2.model.remote_config.ActivityNavigationConfig;
import arivihan.technologies.doubtbuzzter2.model.remote_config.CameraPermissionModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;

/*
 * File: CommonMethods Copyright 12/2/21 12:19 AM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 12/2/21 12:19 AM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

public class CommonMethods {

    private static final String TAG = CommonMethods.class.getSimpleName();

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();

    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    public static String format(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + format(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    public static String decapitalizeString(String string) {
        return string == null || string.isEmpty() ? "" : Character.toLowerCase(string.charAt(0)) +
                string.substring(1);
    }

    public static boolean isInteger(String number) {
        try {
            int num = Integer.parseInt(number);
            return true;
        } catch (Exception ignored) {
        }
        return false;
    }

    public static String modifyString(String string) {
        if (StringUtil.isBlank(string)) {
            return "";
        }
        return string.replace(" ", "_").toLowerCase();
    }

    public static <T> JSONObject getJsonObject(T object) {
        Gson gson = new GsonBuilder().create();
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(gson.toJson(object));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static String getUrlParam(String url, Map<String, Object> params) {
        StringBuilder sb = new StringBuilder(url);
        if (null == params) {
            params = new HashMap<>();
        }

        Integer batchId = MyApplication.getIntData(PreferenceKeys.BATCH_ID.getKey());
        if (batchId != null && !params.containsKey("batchId")) {
            params.put("batchId", batchId);
        }

        if (params.size() > 0) {
            sb.append("?");
        }

        for (String param : params.keySet()) {
            sb.append(param).append("=").append(params.get(param)).append("&");
        }

        return params.size() == 0 ? sb.toString() : sb.substring(0, sb.length() - 1);
    }

//    public static String getUrlParam(String url, Map<String, Object> params) {
//        StringBuilder sb = new StringBuilder(url);
//        if (null == params) {
//            params = new HashMap<>();
//        }
//
//        if (params.size() > 0) {
//            sb.append("?");
//        }
//
//        for (String param : params.keySet()) {
//            sb.append(param).append("=").append(params.get(param)).append("&");
//        }
//
//        return params.size() == 0 ? sb.toString() : sb.substring(0, sb.length() - 1);
//    }

    public static void incrementNoOfQuestionAttempted(Map<String, Object> dataToSave) {
        dataToSave.put("noOfQuestionAttempted", FieldValue.increment(1));
    }

    public static void incrementNoOfCorrectAnswer(Map<String, Object> dataToSave) {
        dataToSave.put("noOfCorrectAnswer", FieldValue.increment(1));
    }

    public static void incrementNoOfQuestionAsked(Map<String, Object> dataToSave, String id) {
        dataToSave.put("noOfCorrectAnswer", FieldValue.increment(1));
    }

    /** Shows a snackbar above the navigation bar even when the activity draws edge to edge. */
    private static void showSnackbarWithSystemBarInsets(@NonNull Snackbar snackbar) {
        View snackbarView = snackbar.getView();
        ViewGroup.LayoutParams rawParams = snackbarView.getLayoutParams();
        if (rawParams instanceof ViewGroup.MarginLayoutParams) {
            final int baseBottomMargin = ((ViewGroup.MarginLayoutParams) rawParams).bottomMargin;
            ViewCompat.setOnApplyWindowInsetsListener(snackbarView, (view, insets) -> {
                WindowInsetsCompat rootInsets = ViewCompat.getRootWindowInsets(view);
                WindowInsetsCompat effectiveInsets = rootInsets != null ? rootInsets : insets;
                Insets navigation = effectiveInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
                Insets keyboard = effectiveInsets.getInsets(WindowInsetsCompat.Type.ime());
                ViewGroup.MarginLayoutParams params =
                        (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                params.bottomMargin = baseBottomMargin + Math.max(navigation.bottom, keyboard.bottom);
                view.setLayoutParams(params);
                return insets;
            });
        }

        snackbar.show();
        ViewCompat.requestApplyInsets(snackbarView);
    }

    public static void showPositiveSnackbar(View ParentLayout, String Message) {
        try {
            if (ParentLayout.getParent() != null && !(ParentLayout instanceof ScrollView)) {
                Snackbar snackbar = Snackbar.make(ParentLayout, Message, 1000)
                        .setBackgroundTint(ContextCompat.getColor(ParentLayout.getContext(), R.color.navy_blue))
                        .setTextColor(Color.WHITE);
                showSnackbarWithSystemBarInsets(snackbar);
            }
        } catch (Exception e) {

        }
    }

    public static Snackbar showPositiveSnackbar(View ParentLayout, String Message, int duration) {
        try {
            if (ParentLayout.getParent() != null && !(ParentLayout instanceof ScrollView)) {
                Snackbar snackbar = Snackbar.make(ParentLayout, Message, duration)
                        .setBackgroundTint(ContextCompat.getColor(ParentLayout.getContext(), R.color.navy_blue))
                        .setTextColor(Color.WHITE);
                showSnackbarWithSystemBarInsets(snackbar);
                return snackbar;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getAlphaNumericString(int n) {

        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int) (AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }

    public static Snackbar showLongPositiveSnackbar(View ParentLayout, String Message) {
        try {
            Snackbar snackbar = Snackbar.make(ParentLayout, Message, 2000)
                    .setBackgroundTint(ContextCompat.getColor(ParentLayout.getContext(), R.color.navy_blue))
                    .setTextColor(Color.WHITE);
            showSnackbarWithSystemBarInsets(snackbar);
            return snackbar;
        } catch (Exception e) {

        }
        return null;

    }

    public static void showLongNegativeSnackbar(View ParentLayout, String Message) {
        try {
            Snackbar snackbar = Snackbar.make(ParentLayout, Message, 3200)
                    .setBackgroundTint(ContextCompat.getColor(ParentLayout.getContext(), R.color.error_button_color))
                    .setTextColor(Color.WHITE);
            showSnackbarWithSystemBarInsets(snackbar);
        } catch (Exception e) {

        }
    }


    public static Snackbar showNegativeSnackbar(View ParentLayout, String Message) {
        try {
            if (ParentLayout.getParent() != null && !(ParentLayout instanceof ScrollView)) {
                Snackbar snackbar = Snackbar.make(ParentLayout, Message, 5000)
                        .setBackgroundTint(ContextCompat.getColor(ParentLayout.getContext(), R.color.error_button_color))
                        .setTextColor(Color.WHITE);
                showSnackbarWithSystemBarInsets(snackbar);
                return snackbar;
            }
        } catch (Exception e) {

        }
        return null;
    }

    public static Snackbar showNegativeSnackbar(View ParentLayout, String Message, int duration) {
        try {
            if (ParentLayout.getParent() != null && !(ParentLayout instanceof ScrollView)) {
                Snackbar snackbar = Snackbar.make(ParentLayout, Message, duration)
                        .setBackgroundTint(ContextCompat.getColor(ParentLayout.getContext(), R.color.error_button_color))
                        .setTextColor(Color.WHITE);
                showSnackbarWithSystemBarInsets(snackbar);
                return snackbar;
            }
        } catch (Exception e) {

        }
        return null;
    }

    public static boolean isOnline(Context context) {
        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni != null && ni.isConnectedOrConnecting()) {
                return true;
            } else
                return false;
        }
        return false;
    }

    public static String getDate(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM HH:mm a", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("IST"));
        return sdf.format(time);
    }

    public static String getTimeInHourAndMinute(long time, Context context) {
        long hours = TimeUnit.MILLISECONDS.toHours(time) % 60;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(time) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(time) % 60;
        if (hours == 0)
            return String.format(Locale.ENGLISH, "%02d : %02d ", minutes, seconds) + " " + context.getString(R.string.min);
        else
            return String.format(Locale.ENGLISH, "%02d : %02d : %02d", hours, minutes, seconds) + " " + context.getString(R.string.hr);
    }

    public static String getTimeInHrMinSec(long time) {
        long hours = TimeUnit.MILLISECONDS.toHours(time) % 60;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(time) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(time) % 60;
        return String.format(Locale.ENGLISH, "%02d : %02d : %02d", hours, minutes, seconds);
    }

    public static String getTimeInMinutes(long time) {
        long minutes = time / 60000;
        return String.valueOf(minutes);
    }

    public static String getTimeInHrAndMin(long time, Context context) {
        long hours = TimeUnit.MILLISECONDS.toHours(time) % 60;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(time) % 60;
        if (hours > 0 && minutes > 0)
            return hours + context.getString(R.string.hr) + " " + minutes + context.getString(R.string.min);
        else if (hours > 0)
            return hours + context.getString(R.string.hr);
        else
            return minutes + context.getString(R.string.min);
    }

    public static String convertToString(List<List<String>> instructionList) {

        if (instructionList == null || CollectionUtils.isEmpty(instructionList)) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (List<String> strings : instructionList) {
            for (String string : strings) {
                sb.append(string).append("~");
            }
            sb.append("::");
        }

        return sb.toString();
    }

    public static List<List<String>> convertToList(String string) {

        List<List<String>> list = new ArrayList<>();
        if (StringUtil.isBlank(string)) {
            return list;
        }

        for (String x : string.split("::")) {
            if (x.endsWith("~")) {
                x = x.substring(0, x.lastIndexOf("~"));
            }

            String[] split = x.split("~");
            List<String> sublist = new ArrayList<>();

            for (String s : split) {
                sublist.add(s.trim());
            }

            list.add(sublist);

        }


        return list;
    }

    public static String getRupeeText(int i) {
        if (i < 0) {
            return "- ₹ " + Math.abs(i);
        }
        return "₹ " + i;
    }

    public static double parseDouble(String userAnswer) {
        try {
            return Double.parseDouble(userAnswer);
        } catch (Exception e) {
            return 0;
        }

    }

    public static void updateUnwatched(String videoId) {
        try {
            MyApplication instance = MyApplication.getInstance();
            Map<String, Object> map = new HashMap<>();
            map.put("id", videoId);
            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                    CommonMethods.getUrlParam(UPDATE_UNWATCHED_VIDEO, map), null,
                    response -> {
                        try {
                            Gson gson = new Gson();
                            TypeToken<ApiResponse> token = new TypeToken<ApiResponse>() {
                            };
                            ApiResponse apiResponse = gson.fromJson(response.toString(), token.getType());

                            if (apiResponse.isSuccess()) {

                                Set<String> unwatchedSet = MyApplication.getStringSet(PreferenceKeys.UNWATCHED_SET.getKey());

                                if (!CollectionUtils.isEmpty(unwatchedSet)) {
                                    unwatchedSet.remove(videoId);
                                    MyApplication.saveStringSet(PreferenceKeys.UNWATCHED_SET.getKey(), unwatchedSet);

                                    if (CollectionUtils.isEmpty(unwatchedSet)) {
                                        MyApplication.saveData(PreferenceKeys.UNWATCHED.getKey(), false);
                                    }
                                }

                            }

                        } catch (Exception e) {

                        }
                    }, new VolleyGlobalErrorListener(instance) {
                @Override
                public void postError(VolleyError error) {

                }
            });
            MyVolleySingleton.getInstance(instance).addToRequestQueue(myJsonObjectRequest);
        } catch (Exception e) {

        }
    }

    public static String getMimeType(String filePath) {
        Path path = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            path = Paths.get(filePath);
        }
        String mimeType = "application/octet-stream"; // Default MIME type

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mimeType = Files.probeContentType(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mimeType;
    }

    public static byte[] readFileToByteArray(File file) throws IOException {
        InputStream inputStream = new FileInputStream(file);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[4096];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        outputStream.close();

        return outputStream.toByteArray();
    }


    public static String getObjectFromRemoteConfig(String key) {
        String text;
        String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        String appendText = "_" + lang;
        if ("en".equalsIgnoreCase(lang)) {
            appendText = "";
        }

        text = MyApplication.getFirebaseRemoteConfig().getString(key + appendText);
        if (!"".equalsIgnoreCase(text) && StringUtils.isEmpty(text)) {
            text = MyApplication.getFirebaseRemoteConfig().getString(key);
        }
        return text;
    }

    public static void publishScreenVisitEvent(String phoneNumber, String screen, String url) {
        try {
            MyApplication instance = MyApplication.getInstance();
            Map<String, Object> map = new HashMap<>();
            map.put("phoneNumber", phoneNumber);
            map.put("screen", screen);

//            Bundle shareBundle = new Bundle();
//            shareBundle.putString("phoneNumber", phoneNumber);
//            shareBundle.putString("screen", screen);
//            MyApplication.logFirebaseEvent("user_onboarding_screen", shareBundle);

            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                    CommonMethods.getUrlParam(url, map), null,
                    response -> {
                    }, new VolleyGlobalErrorListener(instance) {
                @Override
                public void postError(VolleyError error) {

                }
            });
            MyVolleySingleton.getInstance(instance).addToRequestQueue(myJsonObjectRequest);
        } catch (Exception e) {

        }
    }

    public static String stringToMd5(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = digest.digest(input.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public static Number cleanDecimalNumber(double number) {
        if (number % 1 == 0) {
            return (long) number;
        } else {
            return number;
        }
    }

    public static void enableDisableView(View view, boolean isEnable) {
        try {
            if (isEnable) {
                view.setAlpha(1.0f);
                view.setEnabled(true);
            } else {
                view.setAlpha(0.4f);
                view.setEnabled(false);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.d(TAG, "Error: " + ex.getMessage());
        }
    }

    public static void hideKeyboard(@NonNull Context context, @Nullable View view) {
        if (view == null) {
            view = new View(context);
        }
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static int dpToPx(Context context, float dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    public static boolean isValidJson(String input) {
        try {
            input = input.trim();
            if (input.startsWith("{")) {
                new JSONObject(input);
            } else if (input.startsWith("[")) {
                new JSONArray(input);
            } else {
                return false;
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static String getShortName(String key) {

        final String TAG = "SubjectUtils";

        FirebaseRemoteConfig config = MyApplication.getFirebaseRemoteConfig();

        try {
            String userExamCode = MyApplication.getStringData(PreferenceKeys.EXAM_CODE.getKey());

            Log.d(TAG, "getShortName called | key=" + key + " | userExamCode=" + userExamCode);

            String json = config.getString("subjects_batch_v2");

            if (json == null || json.isEmpty()) {
                Log.w(TAG, "subjects_batch_v2 config is empty or null");
                return key;
            }

            JSONArray array = new JSONArray(json);
            Log.d(TAG, "subjects_batch_v2 parsed | totalSubjects=" + array.length());

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String subjectKey = obj.optString("key");

                Log.d(TAG, "Checking subject | index=" + i + " | subjectKey=" + subjectKey);

                if (!key.equalsIgnoreCase(subjectKey)) {
                    continue;
                }

                Log.d(TAG, "Key matched | subjectKey=" + subjectKey);

                JSONArray examCodes = obj.optJSONArray("exam-codes");
                boolean isExamMatched = false;

                if (examCodes != null) {
                    for (int j = 0; j < examCodes.length(); j++) {
                        String examCode = examCodes.optString(j);
                        Log.d(TAG, "Checking examCode | subjectKey=" + subjectKey + " | examCode=" + examCode);

                        if (userExamCode.equalsIgnoreCase(examCode)) {
                            isExamMatched = true;
                            break;
                        }
                    }
                } else {
                    Log.w(TAG, "No examCodes found for subjectKey=" + subjectKey);
                }

                if (isExamMatched) {
                    Log.d(TAG, "Exam matched | Returning full name=" + key);
                    return key;
                }

                String shortName = obj.optString("short-name", key);
                Log.d(TAG, "Exam not matched | Returning short-name=" + shortName);
                return shortName;
            }

            Log.w(TAG, "No matching subject key found | key=" + key);

        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing failed in getShortName", e);
        }

        Log.d(TAG, "Fallback return | key=" + key);
        return key;
    }

    public static String getShortNameForSubjects(String key) {

        final String TAG = "SubjectUtils";

        FirebaseRemoteConfig config = MyApplication.getFirebaseRemoteConfig();
        String userCourseId = MyApplication.getStringData("selectedCourseId");

        Log.d(TAG, "getShortName called | key=" + key + " | userCourseId=" + userCourseId);

        try {
            String json = config.getString("subjectsV2_for_subjects_scetions");

            if (json == null || json.isEmpty()) {
                Log.w(TAG, "subjectsV2 config is empty or null");
                return key;
            }

            JSONArray array = new JSONArray(json);
            Log.d(TAG, "subjectsV2 parsed | totalSubjects=" + array.length());

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String subjectKey = obj.optString("key");

                Log.d(TAG, "Checking subject | index=" + i + " | subjectKey=" + subjectKey);

                if (!key.equalsIgnoreCase(subjectKey)) {
                    continue;
                }

                Log.d(TAG, "Key matched | subjectKey=" + subjectKey);

                JSONArray courseIds = obj.optJSONArray("full-form-course-ids");
                boolean isCourseMatched = false;

                if (courseIds != null) {
                    for (int j = 0; j < courseIds.length(); j++) {
                        String courseId = courseIds.optString(j);
                        Log.d(TAG, "Checking courseId | subjectKey=" + subjectKey + " | courseId=" + courseId);

                        if (userCourseId.equalsIgnoreCase(courseId)) {
                            isCourseMatched = true;
                            break;
                        }
                    }
                } else {
                    Log.w(TAG, "No courseIds found for subjectKey=" + subjectKey);
                }

                if (isCourseMatched) {
                    String name = obj.optString("name", key);
                    Log.d(TAG, "Course matched | Returning full name=" + name);
                    return name;
                }

                String shortName = obj.optString("short-name", key);
                Log.d(TAG, "Course not matched | Returning short-name=" + shortName);
                return shortName;
            }

            Log.w(TAG, "No matching subject key found | key=" + key);

        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing failed in getShortName", e);
        }

        Log.d(TAG, "Fallback return | key=" + key);
        return key;
    }


    public static String normalizeSpaces(String input) {
        if (input == null) return null;
        // Replace multiple spaces, tabs, or newlines with a single space
        return input.trim().replaceAll("\\s+", " ");
    }

    public static Dialog getOfflineNoInternetDialog(Context context, DialogListener listener) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_no_internet_my_download);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
        dialog.getWindow().setBackgroundDrawable(context.getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        CardView cv_primary = dialog.findViewById(R.id.cv_primary);
        CardView cv_secondary = dialog.findViewById(R.id.cv_secondary);
        LottieAnimationView lottie = dialog.findViewById(R.id.lottie);

        ImageView iv_close = dialog.findViewById(R.id.iv_close);

        iv_close.setOnClickListener(imageView -> {
            dialog.dismiss();
            listener.buttonBCallback();
        });

        dialog.setOnDismissListener(v -> {
            lottie.cancelAnimation();
            lottie.clearAnimation();
        });

        cv_primary.setOnClickListener(cardView -> {
            dialog.dismiss();
            listener.buttonACallback();
        });

        cv_secondary.setOnClickListener(cardView -> {
            dialog.dismiss();
            listener.buttonBCallback();
        });

        return dialog;
    }

    public static void initAppReviewDialog(Context context) {
        Dialog dialog = new AppReviewDialog(context, new AppReviewDialog.OnAppReviewDialogClickListener() {
            @Override
            public void onPrimaryButtonClick(boolean status, int starCount, String feedback, Object object) {
                try {
                    ArrayList<String> list = (ArrayList<String>) object;
                    if (starCount == 5) {
                        Dialog appReviewDialog = CommonDialogs.getAppPositiveReviewDialog(context, starCount, new DialogPositiveNegativeOptionClickListener() {
                            @Override
                            public void onPositiveButtonClick(boolean status, String feedback, Object object) {
                                AppReviewDialog.openPlayStore(context);
                            }

                            @Override
                            public void onNegativeButtonClick(boolean status, String feedback, Object object) {
                            }
                        });
                        appReviewDialog.show();
                    } else {
                        CommonDialogs.setFeedbackThankYouDialog(context, null);
                    }
                    CommonAPIs.sendAppReviewAPI(context, TAG, starCount, list, feedback);
//                    CommonAPIs.getAppReviewStatusAPI(context, TAG, null);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onSecondaryButtonClick(boolean status, int starCount, String feedback, Object object) {

            }
        });

        dialog.show();
    }


    public static Dialog getCameraPermissionDialog(Context context) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_camera_permission_setting);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
        dialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(context, R.drawable.bachground_custom_dialogu_typeactivity));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP;
            }
        });

        String firebaseJsonStr = MyApplication.getFirebaseRemoteConfig()
                .getString("camera_permission_setting_" +
                        MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));

        Log.d(TAG, "firebaseJsonStr: " + firebaseJsonStr);

        CardView cv_skip = dialog.findViewById(R.id.cv_skip);
        CardView cv_settings = dialog.findViewById(R.id.cv_settings);
        TextView tv_header_text = dialog.findViewById(R.id.tv_header_text);
        TextView tv_body_text = dialog.findViewById(R.id.tv_body_text);

        ImageView iv_close = dialog.findViewById(R.id.iv_close);

        Gson gson = new Gson();
        CameraPermissionModel cameraPermissionModel = gson.fromJson(firebaseJsonStr, CameraPermissionModel.class);

        tv_body_text.setText(cameraPermissionModel.getBodyText());
        tv_header_text.setText(StringUtils.isNotEmpty(cameraPermissionModel.getHeaderText()) ? CommonMethods.isValidJson(cameraPermissionModel.getHeaderText()) ? StyledTextBuilder.fromJson(context, cameraPermissionModel.getHeaderText()) : cameraPermissionModel.getHeaderText() : context.getString(R.string.camera_permission_zaruri_hai_upload_ke_liye));
        tv_body_text.setText(StringUtils.isNotEmpty(cameraPermissionModel.getBodyText()) ? CommonMethods.isValidJson(cameraPermissionModel.getBodyText()) ? StyledTextBuilder.fromJson(context, cameraPermissionModel.getBodyText()) : cameraPermissionModel.getBodyText() : context.getString(R.string.go_to_settings));

        cv_skip.setOnClickListener(v -> dialog.dismiss());
        cv_settings.setOnClickListener(v -> {
            dialog.dismiss();
            PermissionRequestView.openAppSettings(context);
        });
        iv_close.setOnClickListener(v -> dialog.dismiss());

        return dialog;
    }

    public static String removeUnrenderableEmojis(String text) {
        if (StringUtils.isEmpty(text))
            return text;
        StringBuilder sb = new StringBuilder();
        Paint paint = new Paint();

        int i = 0;
        while (i < text.length()) {
            int codePoint = text.codePointAt(i);
            String character = new String(Character.toChars(codePoint));

            if (paint.hasGlyph(character)) {
                sb.append(character);
            }
            i += Character.charCount(codePoint);
        }
        return sb.toString();
    }

    public static void callMeBack(Context context, String tag, String activityName, String userComment, ApiCallBackListener apiCallBackListener) {

        try {
            Gson gson = new Gson();
            final Map<String, Object> map = new HashMap<>();
            JSONObject requestBody = new JSONObject();
            requestBody.put("phoneNumber", MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey()));
            requestBody.put("userComment", StringUtils.isNotEmpty(userComment) ? userComment : "");
            requestBody.put(PreferenceKeys.USER_ID.getKey(), MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
            requestBody.put("raisedFrom", activityName);
            Log.d(tag, "Request Body: " + requestBody.toString());
            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                    CommonMethods.getUrlParam(CALL_ME_BACK, map), requestBody,
                    response -> {
                        try {
                            TypeToken<CallBackResponse> token = new TypeToken<CallBackResponse>() {
                            };
                            CallBackResponse callBackResponse = gson.fromJson(response.toString(), token);
                            if (callBackResponse.isSuccess()) {
                                if (apiCallBackListener != null) {
                                    apiCallBackListener.onSuccess(true, "", callBackResponse);
                                }
                            } else {
                                if (apiCallBackListener != null) {
                                    apiCallBackListener.onError(false, "", callBackResponse);
                                }
                            }
                            Log.d(tag, "Response: " + response.toString());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    },
                    new VolleyGlobalErrorListener(context) {
                        @Override
                        public void postError(VolleyError error) {
                            if (apiCallBackListener != null) {
                                apiCallBackListener.onError(false, "", null);
                            }
                            Log.e(tag, "Error: " + error.getMessage());
                        }
                    });
            myJsonObjectRequest.setTag(tag);
            MyVolleySingleton.getInstance(context).addToRequestQueue(myJsonObjectRequest);
        } catch (Exception ignored) {
        }
    }

    public static boolean isProdRelease() {
        return (!BuildConfig.DEBUG && (BuildConfig.BASE_URL.equals(BuildConfig.BASE_URL_PROD)));
    }

    public static boolean isEnvironmentProdRelease() {
        return EnvironmentManager.getEnvironmentNameByUrl(EnvironmentManager.getEndPoint()).equalsIgnoreCase("prod");
    }

    public static boolean isEnvironmentStage() {
        return EnvironmentManager.getEnvironmentNameByUrl(EnvironmentManager.getEndPoint()).equalsIgnoreCase("stage");
    }

    public static void setInternetConnectStatusToSharedPreference(boolean isConnected) {
        if (isConnected) {
            MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
            MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
        } else {
            MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), false);
            MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), true);
        }
    }

    public static Map<String, Object> getIntentParams(Intent intent) {
        return getBundleParams(intent != null ? intent.getExtras() : null);
    }


    public static HashMap<String, String> convertToStringMap(Map<String, Object> map) {
        HashMap<String, String> stringMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                stringMap.put(entry.getKey(), entry.getValue().toString());
            }
        }

        return stringMap;
    }

    public static Map<String, Object> getBundleParams(Bundle extras) {
        Map<String, Object> params = new HashMap<>();
        if (extras == null) return params;

        for (String key : extras.keySet()) {
            Object value = extras.get(key);

            if (value instanceof Bundle) {
                // Recursive call for nested bundles
                params.put(key, getBundleParams((Bundle) value));
            } else if (value instanceof ArrayList<?>) {
                // ArrayList
                ArrayList<?> list = (ArrayList<?>) value;
                List<Object> safeList = new ArrayList<>();
                for (Object item : list) {
                    safeList.add(processValue(item));
                }
                params.put(key, safeList);
            } else if (value != null && value.getClass().isArray()) {
                // arrays
                int length = Array.getLength(value);
                List<Object> arrayList = new ArrayList<>();
                for (int i = 0; i < length; i++) {
                    arrayList.add(Array.get(value, i));
                }
                params.put(key, arrayList);
            } else {
                // Generic fallback for primitives, strings, etc.
                params.put(key, processValue(value));
            }
        }
        return params;
    }

    private static Object processValue(Object value) {

        if (value == null) return "null";

        if (value instanceof String ||
                value instanceof Number ||
                value instanceof Boolean) {
            return value;
        }

        if (value instanceof Parcelable || value instanceof Serializable) {
            return value.getClass().getSimpleName();
        }

        return String.valueOf(value);
    }

    public static String capitalizeWords(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }
        StringBuilder result = new StringBuilder();
        String[] words = input.trim().toLowerCase().split("\\s+");

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1));
                if (i < words.length - 1) {
                    result.append(" "); // only add space between words
                }
            }
        }

        return result.toString();
    }


    public static Dialog showGenericPopupInformation(
            Context context,
            String titleText,
            String subtitleText,
            String buttonText, String chapterStatus
    ) {
        Log.d(TAG, "showGenericPopupInformation() called with: titleText=" + titleText
                + ", subtitleText=" + subtitleText
                + ", buttonText=" + buttonText
                + ", chapterStatus=" + chapterStatus);
        try {
            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.dialog_alert_title_subtitle_button);
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            Log.d(TAG, "Dialog initialized and content view set");

            TextView title = dialog.findViewById(R.id.title);
            TextView subtitle = dialog.findViewById(R.id.subtitle);
            TextView btnTextView = dialog.findViewById(R.id.btnText);
            CardView cv_confirm = dialog.findViewById(R.id.cv_okay);
            ImageView iv_close = dialog.findViewById(R.id.iv_close);
            ImageView dialogImage=dialog.findViewById(R.id.selection_image);



            LottieAnimationView animation = dialog.findViewById(R.id.animation);
            Log.d(TAG, "Views bound successfully--COMMONMETHODS"+chapterStatus);

            if(chapterStatus.equals(ChapterStatus.RESCHEDULED.name())){
                dialogImage.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(R.drawable.live_rescheduled)
                        .into(dialogImage);
                animation.setVisibility(View.GONE);
            }else {
                animation.setAnimation(R.raw.live_lottie_anim);
                dialogImage.setVisibility(View.GONE);
                animation.setVisibility(View.VISIBLE);
                Log.d(TAG, "Lottie animation set: live_lottie_anim");
            }

            title.setVisibility(View.GONE);
            subtitle.setVisibility(View.GONE);
            btnTextView.setVisibility(View.GONE);

            if (StringUtils.isNotEmpty(titleText)) {
                boolean isJson = CommonMethods.isValidJson(titleText);
                Log.d(TAG, "Setting title. isValidJson=" + isJson);
                title.setText(isJson ?
                        StyledTextBuilder.fromJson(context, titleText) : titleText);
                title.setVisibility(View.VISIBLE);
            } else {
                Log.d(TAG, "titleText is empty, title hidden");
            }

            if (StringUtils.isNotEmpty(subtitleText)) {
                boolean isJson = CommonMethods.isValidJson(subtitleText);
                Log.d(TAG, "Setting subtitle. isValidJson=" + isJson);
                subtitle.setText(isJson ?
                        StyledTextBuilder.fromJson(context, subtitleText) : subtitleText);
                subtitle.setVisibility(View.VISIBLE);
            } else {
                Log.d(TAG, "subtitleText is empty, subtitle hidden");
            }

            if (StringUtils.isNotEmpty(buttonText)) {
                boolean isJson = CommonMethods.isValidJson(buttonText);
                Log.d(TAG, "Setting button text. isValidJson=" + isJson);
                btnTextView.setText(isJson ?
                        StyledTextBuilder.fromJson(context, buttonText) : buttonText);
                btnTextView.setVisibility(View.VISIBLE);
            } else {
                Log.d(TAG, "buttonText is empty, button hidden");
            }

            cv_confirm.setOnClickListener(view -> {
                Log.d(TAG, "Confirm button clicked, dismissing dialog");
                dialog.dismiss();
            });

            iv_close.setOnClickListener(v -> {
                Log.d(TAG, "Close icon clicked, dismissing dialog");
                dialog.dismiss();
            });

            dialog.show();
            Log.d(TAG, "Dialog shown successfully");

            return dialog;
        } catch (Exception e) {
            Log.e(TAG, "Error in showGenericPopupInformation: " + e.getMessage(), e);
            e.printStackTrace();
        }

        Log.w(TAG, "Returning null dialog");
        return null;
    }

    public static Dialog showLockPopupDialog(
            Context context,
            SubscriptionLockPopupDto subscriptionLockPopupDto
    ) {
        try {
            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.custom_dialog_lock_box);
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);

            TextView header = dialog.findViewById(R.id.header);
            TextView dialog_title = dialog.findViewById(R.id.dialog_title);
            MaterialCardView subscriptionLayout = dialog.findViewById(R.id.subscriptionLayout);
            TextView subs = dialog.findViewById(R.id.subs);

            ImageView cancel = dialog.findViewById(R.id.cancel);

            dialog_title.setVisibility(View.GONE);
            subscriptionLayout.setVisibility(View.GONE);
            if (StringUtils.isNotEmpty(subscriptionLockPopupDto.getTitle())) {
                dialog_title.setText(subscriptionLockPopupDto.getTitle());
                dialog_title.setVisibility(View.VISIBLE);
            }
            if (StringUtils.isNotEmpty(subscriptionLockPopupDto.getBtnName())) {
                subscriptionLayout.setVisibility(View.VISIBLE);
                subs.setText(subscriptionLockPopupDto.getBtnName());
            }
            if (StringUtils.isNotEmpty(subscriptionLockPopupDto.getSubTitle())) {
                header.setText(subscriptionLockPopupDto.getSubTitle());
            }
            subscriptionLayout.setOnClickListener(v -> {

                Log.d(TAG, "Subscription layout clicked");

                MyApplication.logFirebaseBundleCreation(
                        FirebaseEvents.subscription_layout_btn_click.getActionName()
                );

                try {

                    if (subscriptionLockPopupDto == null) {
                        Log.e(TAG, "subscriptionLockPopupDto is null");
                        return;
                    }

                    Log.d(TAG, "subscriptionLockPopupDto: " + subscriptionLockPopupDto.toString());

                    if (StringUtils.isNotEmpty(subscriptionLockPopupDto.getClassName())) {

                        Log.d(TAG,
                                "Opening activity from className: "
                                        + subscriptionLockPopupDto.getClassName());

                        Log.d(TAG,
                                "Activity params: "
                                        + subscriptionLockPopupDto.getActivityParams());

                        MyApplication.openActivity(
                                context,
                                subscriptionLockPopupDto.getClassName(),
                                subscriptionLockPopupDto.getActivityParams()
                        );

                    } else {

                        Log.d(TAG,
                                "ClassName is empty, navigating to subscription page");

                        SubscriptionNavigationHelper.navigateToSubscriptionPage(context, RedirectionSource.LOCK_POPUP_JOIN);

//            Intent intent = new Intent(context, CampaignSubscriptionActivity.class);
//            context.startActivity(intent);
                    }

                    Log.d(TAG, "Dismissing dialog");
                    dialog.dismiss();

                } catch (Exception e) {
                    Log.e(TAG, "Exception occurred while handling subscription layout click", e);
                    e.printStackTrace();
                }
            });

            cancel.setOnClickListener(v -> {
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.lock_popup_cancel_btn_click.getActionName());
                dialog.dismiss();
            });

            dialog.show();
            return dialog;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void applyStyledOrNormalText(Context context, TextView textView, String text) {
        if (CommonMethods.isValidJson(text)) {
            textView.setText(StyledTextBuilder.fromJson(context, text));
        } else {
            textView.setText(text);
        }
    }

    public static String getPersonalizedPathUrl() {
        return MyApplication.getFirebaseRemoteConfig().getString("personalised_path_base_url" + (isEnvironmentProdRelease() ? "_prod" : (isEnvironmentStage() ? "_stage" : "_dev")));
    }

    // Base URL for the NEET diagnostic (stage pyramid / opportunity map / next
    // marks trajectory) webview, shown once right after onboarding for NEET
    // students. Set the remote-config keys the same way as personalised_path_base_url.
    public static String getNeetDiagnosticUrl() {
        return MyApplication.getFirebaseRemoteConfig().getString("neet_diagnostic_base_url" + (isEnvironmentProdRelease() ? "_prod" : (isEnvironmentStage() ? "_stage" : "_dev")));
    }

    public static String getTestGeneratorUrl() {
        return MyApplication.getFirebaseRemoteConfig().getString("test_generator_base_url" +
                (isEnvironmentProdRelease() ? "_prod" : (isEnvironmentStage() ? "_stage" : "_dev")));
    }

    public static String encodeUrl(String url) {
        try {
            return URLEncoder.encode(url, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void shareText(Context context, String content) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, content);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Share via");
        context.startActivity(shareIntent);
    }

    public static Bundle mapToBundle(Map<String, Object> map) {
        Bundle bundle = new Bundle();
        if (map == null) return bundle;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value == null) {
                // skip or store empty string
                bundle.putString(key, null);
            } else if (value instanceof String) {
                bundle.putString(key, (String) value);
            } else if (value instanceof Integer) {
                bundle.putInt(key, (Integer) value);
            } else if (value instanceof Long) {
                bundle.putLong(key, (Long) value);
            } else if (value instanceof Boolean) {
                bundle.putBoolean(key, (Boolean) value);
            } else if (value instanceof Float) {
                bundle.putFloat(key, (Float) value);
            } else if (value instanceof Double) {
                bundle.putDouble(key, (Double) value);
            } else if (value instanceof Map) {
                // nested map
                bundle.putBundle(key, mapToBundle((Map<String, Object>) value));
            } else if (value instanceof ArrayList) {
                // handle list of strings/ints/etc
                bundle.putSerializable(key, (Serializable) value);
            } else if (value instanceof Parcelable) {
                bundle.putParcelable(key, (Parcelable) value);
            } else if (value instanceof Serializable) {
                bundle.putSerializable(key, (Serializable) value);
            } else {
                // fallback: convert to string
                bundle.putString(key, value.toString());
            }
        }

        return bundle;
    }

    public static void copyBundle(Bundle source, Bundle target) {
        if (source == null || target == null) return;

        for (String key : source.keySet()) {
            Object value = source.get(key);

            if (value instanceof String) {
                target.putString(key, (String) value);
            } else if (value instanceof Integer) {
                target.putInt(key, (Integer) value);
            } else if (value instanceof Boolean) {
                target.putBoolean(key, (Boolean) value);
            } else if (value instanceof Long) {
                target.putLong(key, (Long) value);
            } else if (value instanceof Double) {
                target.putDouble(key, (Double) value);
            } else if (value instanceof Float) {
                target.putFloat(key, (Float) value);
            } else if (value instanceof Bundle) {
                target.putBundle(key, (Bundle) value);
            } else if (value instanceof Serializable) {
                target.putSerializable(key, (Serializable) value);
            } else if (value instanceof Parcelable) {
                target.putParcelable(key, (Parcelable) value);
            } else {
                // fallback
                target.putString(key, String.valueOf(value));
            }
        }
    }

    public static void openUrlExternally(Context context, String url) {
        try {
            if(StringUtils.isNotEmpty(url)) {
                Uri uri = Uri.parse(url);

                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                // Ask Android to choose best app (YouTube / Browser / Social app)
                Intent chooser = Intent.createChooser(intent, "Open with");
                context.startActivity(chooser);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void openExternalImage(Context context, String url) {
        try {
            if(StringUtils.isNotEmpty(url)) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(url), "image/*");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                context.startActivity(Intent.createChooser(intent, "Open Image"));
            }
        } catch (Exception e) {
            e.printStackTrace();

            // Fallback to browser
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(browserIntent);
        }
    }

    public static void openExternalPdf(Context context, String pdfUrl) {
        try {
            if(StringUtils.isNotEmpty(pdfUrl)) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(pdfUrl), "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                context.startActivity(Intent.createChooser(intent, "Open PDF"));
            }
        } catch (Exception e) {
            e.printStackTrace();

            // Fallback to browser
            Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl));
            browser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(browser);
        }
    }

    public static void openExternalLink(Context context, String url) {
        try {
            if(StringUtils.isNotEmpty(url)) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                // chooser only for browser-type links
                context.startActivity(Intent.createChooser(intent, "Open with"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void navigateFromActivityConfig(Context context, NavigationConfig activityNavigationConfig) {
        if (activityNavigationConfig == null)
            return;
        Log.d(TAG, "navigateFromActivityConfig: "+activityNavigationConfig);
        if(StringUtils.isNotEmpty(activityNavigationConfig.getNavigationType())) {
            LocalNavigationType localNavigationType;
            try {
                localNavigationType = LocalNavigationType.valueOf(activityNavigationConfig.getNavigationType());
            } catch (IllegalArgumentException e) {
                // Unknown navigationType — fall back to opening by className
                if (StringUtils.isNotEmpty(activityNavigationConfig.getClassName())) {
                    MyApplication.openActivity(context, activityNavigationConfig.getClassName(), activityNavigationConfig.getParams());
                }
                return;
            }
            switch (localNavigationType) {
                case EXTERNAL_IMAGE:
                    openExternalImage(context, activityNavigationConfig.getClassName());
                    break;

                case EXTERNAL_PDF:
                    openExternalPdf(context, activityNavigationConfig.getClassName());
                    break;

                case EXTERNAL_LINK, DEEPLINK:
                    openExternalLink(context, activityNavigationConfig.getClassName());
                    break;

                case CLASS_PARAMS, INTERNAL_LINK, ACTIVITY:
                    MyApplication.openActivity(context, activityNavigationConfig.getClassName(), activityNavigationConfig.getParams());
                    break;

                case INTERNAL_NOTE:
                    Map<String, Object> params = activityNavigationConfig.getParams();
                    String pdfUrl = (String) params.get("pdfUrl");
                    String title = (String) params.get("title");
                    String noteType = (String) params.get("noteType");
                    String subjectName = (String) params.get("subjectName");
                    String tag = (String) params.get("tag");
                    MyApplication.openInAppPdfFromUrl(context, pdfUrl, title, noteType, subjectName, tag);
                    break;

                case LOCAL_SUBSCRIPTION_PAGE:
                    SubscriptionNavigationHelper.navigateToSubscriptionPage(context, RedirectionSource.DEEPLINK);
                    break;
                case LOCAL_VIRTUAL_BATCH:
                    CommonNavigator.navigateToVirtualChat(context);
                    break;
            }
        } else {
            if (StringUtils.isNotEmpty(activityNavigationConfig.getClassName())) {
                MyApplication.openActivity(context, activityNavigationConfig.getClassName(), activityNavigationConfig.getParams());
            }
        }
    }

    public static boolean isGifUrl(String url) {
        if (url == null) return false;
        return url.toLowerCase().endsWith(".gif");
    }

    public static boolean isImageUrl(String url) {
        if (url == null) return false;
        return url.toLowerCase().matches(".*\\.(jpg|jpeg|png|webp|bmp)$");
    }

    public static void navigateFromActivityConfig(Context context, String navType,
                                                  String className, Map<String, Object> params) {
        Log.d("NavigationFlow", "Class params: " + params);
        NavigationConfig navigationConfig = new NavigationConfig();
        navigationConfig.setNavigationType(navType);
        navigationConfig.setClassName(className);
        navigationConfig.setParams(params);
        Log.d("NavigationFlow", "naviagtion params: " + navigationConfig.getParams());
        navigateFromActivityConfig(context, navigationConfig);
    }

    public static boolean isActivitySafe(Activity activity) {
        return activity != null
                && !activity.isFinishing()
                && !activity.isDestroyed();
    }

    public static void showCourseChangeInformationDialog(Context context) {
        try {
            Dialog courseChangeDialog = new Dialog(context);
            courseChangeDialog.setContentView(R.layout.home_screen_switch);

            if (courseChangeDialog.getWindow() != null) {
                courseChangeDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
                courseChangeDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                courseChangeDialog.setCancelable(false);
                courseChangeDialog.setCanceledOnTouchOutside(false);
            }
            MyApplication.
                    logFirebaseBundleCreation(FirebaseEvents.home_neet_sw_popup_ok_undstd_click.getActionName());

            TextView title = courseChangeDialog.findViewById(R.id.title);
            TextView subtitle = courseChangeDialog.findViewById(R.id.subtitle);
            TextView btnTextView = courseChangeDialog.findViewById(R.id.bnt_text);
            CardView cvConfirm = courseChangeDialog.findViewById(R.id.cv_okay);
            ImageView btnClose = courseChangeDialog.findViewById(R.id.btnClose);

            Log.d("CourseChangeDialog", "Fetching popup config from Firebase");

            String remoteJson = FirebaseRemoteConfig
                    .getInstance()
                    .getString("home_screen_Course_change_dialog_information");

            Log.d("CourseChangeDialog", "Remote JSON: " + remoteJson);

            JSONObject root = new JSONObject(remoteJson);

            String titleText = root.optString("title");
            String subTitle = root.optString("subtitle");
            String buttonText = root.optString("btnText");
            long popupDuration = root.optLong("timer", 3000);

            Log.d("CourseChangeDialog", "Parsed Values -> Title: " + titleText +
                    " Subtitle: " + subTitle +
                    " Button: " + buttonText +
                    " Timer: " + popupDuration);

            title.setText(CommonMethods.isValidJson(titleText)
                    ? StyledTextBuilder.fromJson(context, titleText)
                    : titleText);

            subtitle.setText(CommonMethods.isValidJson(subTitle)
                    ? StyledTextBuilder.fromJson(context, subTitle)
                    : subTitle);

            btnTextView.setText(CommonMethods.isValidJson(buttonText)
                    ? StyledTextBuilder.fromJson(context, buttonText)
                    : buttonText);

            // Button click
            cvConfirm.setOnClickListener(view -> {
                Log.d("CourseChangeDialog", "Popup dismissed by user");
                if (courseChangeDialog.isShowing()) {
                    courseChangeDialog.dismiss();
                }
            });

            // Close icon click
            btnClose.setOnClickListener(v -> {
                Log.d("CourseChangeDialog", "Popup closed using X button");
                if (courseChangeDialog.isShowing()) {
                    courseChangeDialog.dismiss();
                }
            });

            // Auto dismiss timer
            new Handler(Looper.getMainLooper()).postDelayed(() -> {

                if (courseChangeDialog.isShowing()) {
                    Log.d("CourseChangeDialog", "Popup auto dismissed after timer");
                    courseChangeDialog.dismiss();
                }

            }, popupDuration);

            courseChangeDialog.show();

            Log.d("CourseChangeDialog", "Popup displayed successfully");

        } catch (Exception ex) {
            Log.e("CourseChangeDialog", "Error showing popup", ex);
        }
    }
}
