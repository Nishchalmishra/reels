package arivihan.technologies.doubtbuzzter2.helper.refer;

import android.content.Context;
import android.util.Log;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.google.gson.Gson;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class ReferralUtils {
    private static final String TAG = ReferralUtils.class.getSimpleName();

    public static void checkInstallReferrer(Context context) {
        InstallReferrerClient referrerClient =
                InstallReferrerClient.newBuilder(context).build();

        referrerClient.startConnection(new InstallReferrerStateListener() {
            @Override
            public void onInstallReferrerSetupFinished(int responseCode) {

                if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {

                    new Thread(() -> {
                        try {
                            ReferrerDetails response = referrerClient.getInstallReferrer();
                            String referrerUrl = response.getInstallReferrer();

                            Log.d(TAG, "Raw referrer: " + referrerUrl);

                            if (referrerUrl == null || referrerUrl.isEmpty()) {
                                return;
                            }

                            String decodedReferrer =
                                    URLDecoder.decode(referrerUrl, "UTF-8");
                            Log.d(TAG, "Decoded referrer: " + decodedReferrer);

                            try{
                                Gson gson = new Gson();
                                MyApplication.saveDataForDoNotDeleteSharedPreference(PreferenceKeys.ALL_UTM_PARAMS.getKey(), gson.toJson(extractAllQueryParams(decodedReferrer)));
                            }catch (Exception e){

                            }

//                            Log.i("UTM_PARAMS_REFERER_UTILS", "saveData: " + MyApplication.getStringDataFromDoNotDeleteSharedPreference(PreferenceKeys.ALL_UTM_PARAMS.getKey()));

                            if(getQueryParam(decodedReferrer, "utm_term") != null && getQueryParam(decodedReferrer, "utm_term").equals(PreferenceKeys.COURSE_ID.getKey()) && getQueryParam(decodedReferrer, "utm_content") != null){
                                MyApplication.saveSharedPreferencesDoNotDelete(PreferenceKeys.SELECTED_COURSE_ID.getKey(), getQueryParam(decodedReferrer, "utm_content"));
                            }


                            String referralCodeEncoded =
                                    getQueryParam(decodedReferrer, PreferenceKeys.REFERRAL_CODE.getKey());

                            String utmSource =
                                    getQueryParam(decodedReferrer, "utm_source");

                            if (referralCodeEncoded != null && !referralCodeEncoded.isEmpty()) {

                                byte[] decodedBytes = android.util.Base64.decode(
                                        referralCodeEncoded,
                                        android.util.Base64.DEFAULT
                                );

                                String referralCode = new String(decodedBytes);

                                MyApplication.saveDataForDoNotDeleteSharedPreference(
                                        PreferenceKeys.REFERRAL_CODE.getKey(), referralCode
                                );
                                MyApplication.saveDataForDoNotDeleteSharedPreference(
                                        PreferenceKeys.REFERRAL_SOURCE.getKey(), utmSource
                                );
                                MyApplication.saveDataForDoNotDeleteSharedPreference(
                                        PreferenceKeys.REFERRAL_DETECTED.getKey(), true
                                );
                                MyApplication.saveDataForDoNotDeleteSharedPreference(
                                        PreferenceKeys.REFERRAL_REDIRECTED_PENDING.getKey(), true
                                );
                                MyApplication.saveDataForDoNotDeleteSharedPreference(
                                        PreferenceKeys.REFERRAL_DATA_SET.getKey(), true
                                );

                                Log.d(TAG, "Referral code: " + referralCode);
                                Log.d(TAG, "UTM Source: " + utmSource);
                            }

                        } catch (Exception e) {
                            Log.e(TAG, "Referrer error", e);
                        } finally {
                            referrerClient.endConnection();
                        }
                    }).start();
                }
            }

            @Override
            public void onInstallReferrerServiceDisconnected() {
                Log.w(TAG, "Install referrer disconnected");
            }
        });
    }




    private static String getQueryParam(String query, String key) {
        String[] params = query.split("&");
        for (String param : params) {
            if (param.startsWith(key + "=")) {
                return param.split("=", 2)[1];
            }
        }
        return null;
    }

    private static Map<String, String> extractAllQueryParams(String query) {
        Map<String, String> map = new HashMap<>();

        if (query == null || query.isEmpty()) return map;

        String[] pairs = query.split("&");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                map.put(keyValue[0], keyValue[1]);
            }
        }
        return map;
    }
}
