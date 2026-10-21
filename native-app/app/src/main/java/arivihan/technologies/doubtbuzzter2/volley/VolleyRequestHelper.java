package arivihan.technologies.doubtbuzzter2.volley;

import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.helper.DeviceInfoUtil;
import arivihan.technologies.doubtbuzzter2.helper.EncryptDecryptServiceImpl;
import arivihan.technologies.doubtbuzzter2.model.ArivihanLanguageEnum;

public class VolleyRequestHelper {

    private static final String TAG = VolleyRequestHelper.class.getSimpleName();
    public static Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        try {
            //String fcmToken = MyApplication.getFCMToken();
            String fcmToken = MyApplication.getStringData(PreferenceKeys.AUTH_TOKEN.getKey());

            Log.d("VolleyHeader", "fcm = " + fcmToken);
            String userid = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
            Log.d("VolleyHeader", "userId = " + userid);
            if (StringUtils.isNotEmpty(fcmToken)) {
                headers.put("token", fcmToken);
            }
            if (userid != null) {
                headers.put(PreferenceKeys.USER_ID.getKey(), userid);
            }

            headers.put("versionCode", String.valueOf(
                    DeviceInfoUtil.getVersionCode(MyApplication.getInstance().getApplicationContext())
            ));

            Boolean board = MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey());
            headers.put(PreferenceKeys.BOARD.getKey(), String.valueOf(board));

            String encryptedKey = EncryptDecryptServiceImpl.encrypt(BuildConfig.API_KEY);
            if (StringUtils.isNotBlank(encryptedKey)) {
                headers.put("access-key", encryptedKey);
            } else {
                Log.e(TAG, "access-key encryption failed — header will be missing");
            }
//            headers.put("batchId", MyApplication.getIntData(PreferenceKeys.BATCH_ID.getKey()));

        } catch (Exception e) {
            if (e.getMessage() != null)
                Log.e("error", e.getMessage());
        }

        headers.put("platform", "ANDROID");

        String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        if (lang == null) {
            headers.put("Accept-Language", ArivihanLanguageEnum.ENGLISH.name());
        } else if (ArivihanLanguageEnum.HINDI.getCode().equals(lang) || "hi-rIN".equals(lang)) {
            headers.put("Accept-Language", ArivihanLanguageEnum.HINDI.name());
        } else {
            headers.put("Accept-Language", ArivihanLanguageEnum.ENGLISH.name());
        }

        if (BuildConfig.DEBUG) Log.d(TAG, "Volley Header: " + headers.toString());
        return headers;
    }
}
