package arivihan.technologies.doubtbuzzter2.manager;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.PDF_CIRCLE_USAGE_COUNT;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.common.CommonAPIs;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionType;
import arivihan.technologies.doubtbuzzter2.model.DoubtChatHistoryData;
import arivihan.technologies.doubtbuzzter2.model.pdf_circle.PDFCircleDailyCounter;
import arivihan.technologies.doubtbuzzter2.model.pdf_circle.PDFCircleUsage;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class PDFCircleDailyCounterManager {

    private static final String TAG = PDFCircleDailyCounterManager.class.getSimpleName();
    public static final String PREF_DAILY_COUNTER_NAME = "pdf_circle_daily_counter";
    public static final String PREF_LAST_API_UPDATE = "pdf_circle_last_api_update";
    private static final String KEY_COUNTER = "counter_data";
    private static long DEFAULT_LIMIT_BASIC = 3;
    private static long DEFAULT_LIMIT_PLUS = 10;
    private static long DEFAULT_LIMIT = 10;

    public static void setPdfCircleLimit() {
        try {
            long limit = MyApplication.getFirebaseRemoteConfig().getLong("pdf_circle_daily_limit_competitive_" + (MyApplication.getStringData(PreferenceKeys.USER_SUBSCRIPTION_TYPE.getKey())));
            if(limit>0) {
                DEFAULT_LIMIT = limit;
            }
            long basic = MyApplication.getFirebaseRemoteConfig().getLong("pdf_circle_daily_limit_basic");
            if (basic > 0) DEFAULT_LIMIT_BASIC = basic;

            long plus = MyApplication.getFirebaseRemoteConfig().getLong("pdf_circle_daily_limit_plus");
            if (plus > 0) DEFAULT_LIMIT_PLUS = plus;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** Returns the daily limit for the current subscription plan (BASIC or PLUS). */
    public static long getPdfCircleLimit() {
        return MyApplication.isCompetitiveProfile() ? DEFAULT_LIMIT : (SubscriptionType.isBasicSubscription() ? DEFAULT_LIMIT_BASIC : DEFAULT_LIMIT_PLUS);
    }

    public static boolean tryIncrementPDFCircleCounter(String key, long limit) {

        if (!isPDFCircleCounterTodayValid(key, limit)) {
            return false;
        }

        PDFCircleDailyCounter counter = MyApplication.getPDFCircleDailyCounter(key);

        counter.setViewCount(counter.getViewCount() + 1);
        MyApplication.savePDFCircleDailyCounter(key, counter);
        return true;
    }

    public static boolean isPDFCircleCounterTodayValid(String key, long limit) {
        PDFCircleDailyCounter counter = MyApplication.getPDFCircleDailyCounter(key);

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        if (!today.equals(counter.getLastDateSaved())) {
            // resetting if new day
            counter.setLastDateSaved(today);
            counter.setViewCount(0);
        }

        MyApplication.savePDFCircleDailyCounter(key, counter);

        if (counter.getViewCount() >= limit) {
            return false;
        }
        return true;
    }

    public static int getCurrentPDFCircleCount(String key) {
        return MyApplication.getPDFCircleDailyCounter(key).getViewCount();
    }

    public static void resetPDFCircleCounter(String key, int count) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        PDFCircleDailyCounter counter = PDFCircleDailyCounter.builder()
                .lastDateSaved(today)
                .viewCount(count)
                .build();
        MyApplication.savePDFCircleDailyCounter(key, counter);
    }

    public static void getPdfCircleUsageApiUpdateToday(Context context) {

        if (!isTodayDifferentFromLastUpdate()) {
            Log.d(TAG, "Api update already done today. Skipping.");
            return;
        }

        Map<String, Object> map = new HashMap<>();
        MyJsonObjectRequest pdfCircleUsageRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(PDF_CIRCLE_USAGE_COUNT, map), null,
                response -> {
                    try {
                        TypeToken<PDFCircleUsage> token = new TypeToken<PDFCircleUsage>() {
                        };
                        Gson gson = new Gson();
                        PDFCircleUsage pdfCircleUsage = gson.fromJson(response.toString(), token.getType());
                        Log.d(TAG, "pdfCircleUsage:"+pdfCircleUsage.toString());
                        resetPDFCircleCounter(PREF_DAILY_COUNTER_NAME, pdfCircleUsage.getUsageCount());
                        saveTodayAsLastUpdateDate();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }, new VolleyGlobalErrorListener(context) {
            @Override
            public void postError(VolleyError error) {
//                Log.d(TAG, "pdfCircleUsage: Error: "+error.getMessage());
            }
        });
        pdfCircleUsageRequest.setTag(TAG);
        MyVolleySingleton.getInstance(context).addToRequestQueue(pdfCircleUsageRequest);

    }

    private static boolean isTodayDifferentFromLastUpdate() {
        String lastDate = MyApplication.getStringData(PREF_LAST_API_UPDATE);
        String today = getTodayDateString();

        if (lastDate == null) {
            return true;
        }
        return !today.equals(lastDate);
    }

    private static void saveTodayAsLastUpdateDate() {
        MyApplication.saveData(PREF_LAST_API_UPDATE, getTodayDateString());
    }
    private static String getTodayDateString() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
}
