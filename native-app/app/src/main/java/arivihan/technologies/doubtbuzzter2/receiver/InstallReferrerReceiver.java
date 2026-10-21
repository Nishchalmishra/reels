package arivihan.technologies.doubtbuzzter2.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.net.URLDecoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.SubscriptionActivityNew;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class InstallReferrerReceiver extends BroadcastReceiver {

    private static final String TAG = InstallReferrerReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String referrer = intent.getStringExtra("referrer");

            if (referrer != null) {
                try {
                    String decodedReferrer = URLDecoder.decode(referrer, "UTF-8");
//                    String decodedReferralCode = URLDecoder.decode(referralCode, "UTF-8");
                    Log.d(TAG, "REGISTER referrer: " + decodedReferrer);

                    Log.d(TAG, "Decoded referrer: " + decodedReferrer);

                    Map<String, String> allParams = extractAllQueryParams(decodedReferrer);


                    try{
                        Gson gson = new Gson();
                        MyApplication.saveDataForDoNotDeleteSharedPreference(PreferenceKeys.ALL_UTM_PARAMS.getKey(), gson.toJson(extractAllQueryParams(decodedReferrer)));
                    }catch (Exception e){

                    }

                    if(getQueryParam(decodedReferrer, "utm_term") != null && getQueryParam(decodedReferrer, "utm_term").equals(PreferenceKeys.COURSE_ID.getKey()) && getQueryParam(decodedReferrer, "utm_content") != null){
                        MyApplication.saveDataForDoNotDeleteSharedPreference(PreferenceKeys.SELECTED_COURSE_ID.getKey(), getQueryParam(decodedReferrer, "utm_content"));
                    }

                    // Extract referral_code and utm_source
                    String referralCodeEncoded = getQueryParam(decodedReferrer, PreferenceKeys.REFERRAL_CODE.getKey());

                    Base64.Decoder decoder = Base64.getDecoder();
                    byte[] decodedBytes = decoder.decode(referralCodeEncoded);
                    String referralCode = new String(decodedBytes);

                    String utmSource = getQueryParam(decodedReferrer, "utm_source");

                    if (referralCode != null) {
                        MyApplication.saveDataForDoNotDeleteSharedPreference(PreferenceKeys.REFERRAL_CODE.getKey(), referralCode);
                        MyApplication.saveDataForDoNotDeleteSharedPreference(PreferenceKeys.REFERRAL_SOURCE.getKey(), utmSource);
                        MyApplication.saveDataForDoNotDeleteSharedPreference(PreferenceKeys.REFERRAL_DETECTED.getKey(), true);
                        MyApplication.saveDataForDoNotDeleteSharedPreference(PreferenceKeys.REFERRAL_REDIRECTED_PENDING.getKey(), true);

                        handleReferralCodeBatch(context, referralCode);

//                        Intent startActivityIntent = new Intent(context, SubscriptionActivityNew.class);
//                        startActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        context.startActivity(startActivityIntent);
                    }

                    Log.d(TAG, "Referral code: " + referralCode);
                    Log.d(TAG, "UTM Source: " + utmSource);

                } catch (Exception e) {
                    Log.e(TAG, "Failed to decode referrer", e);
                }
            } else {
                Log.w(TAG, "No referrer received");
            }
        }
    }

    private static void handleReferralCodeBatch(Context context, String referralCode) {
        if (referralCode == null) return;

        FirebaseRemoteConfig remoteConfig = MyApplication.getFirebaseRemoteConfig();

        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        remoteConfig.setConfigSettingsAsync(configSettings);

        remoteConfig.fetchAndActivate().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Remote config fetch successful");
            } else {
                Log.w(TAG, "Remote config fetch failed, using defaults from XML");
            }

            String mapJson = remoteConfig.getString("referral_course_map");
//            Log.d(TAG, "referral_course_map: " + mapJson);

            try {
                JSONObject mapObject = new JSONObject(mapJson);

                if (mapObject.has(referralCode)) {
                    String courseId = mapObject.getString(referralCode);
                    MyApplication.saveDataForDoNotDeleteSharedPreference(PreferenceKeys.SELECTED_COURSE_ID.getKey(), courseId);
                    Log.d(TAG, "Saved course_id: " + courseId + " for referral: " + referralCode);
                } else {
                    Log.w(TAG, "No course mapping found for referral code: " + referralCode);
                }

            } catch (JSONException e) {
                Log.e(TAG, "Failed to parse referral_course_map JSON", e);
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
        Map<String, String> paramMap = new HashMap<>();

        if (query == null || query.isEmpty()) return paramMap;

        if (query.contains("?")) {
            query = query.substring(query.indexOf("?") + 1);
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                if (!key.isEmpty()) {
                    paramMap.put(key, value);
                }
            } else if (keyValue.length == 1 && !keyValue[0].isEmpty()) {
                paramMap.put(keyValue[0].trim(), "");
            }
        }

        return paramMap;
    }

}