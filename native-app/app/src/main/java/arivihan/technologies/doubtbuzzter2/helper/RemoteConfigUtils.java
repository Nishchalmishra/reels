package arivihan.technologies.doubtbuzzter2.helper;

import android.util.Log;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class RemoteConfigUtils {

    public enum FeatureVisibilityName {
        GLOBAL_SEARCH("global_search"),
        LANGUAGE_CHANGE("language_change"),
        TODAY_CLASS("today_class"),
        HOME_SCREEN_TEST_SERIES("home_screen_test_series"),
        EDIT_ROUTINE("editYourRoutine"),
        EDIT_YOUR_NAME("edit_your_name");

        private String feature;

        FeatureVisibilityName(String feature) {
            this.feature = feature;
        }

        public String getFeature() {
            return feature;
        }
    }

    public static boolean isFeatureVisible(String featureKey) {
        try {
            String batchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
            Log.d("FeatureCheck", "Checking feature: " + featureKey +
                    ", batchCode: " + batchCode);

            String jsonStr = MyApplication.getFirebaseRemoteConfig().getString("featureVisibilityBatch");

            if (jsonStr.isEmpty()) {
                Log.w("FeatureCheck", "Remote config 'featureVisibility' is empty.");
                return false;
            }

            JSONObject root = new JSONObject(jsonStr);

            if (!root.has(featureKey)) {
                Log.w("FeatureCheck", "Feature key not found in config: " + featureKey);
                return false;
            }

            JSONArray featureConfig = root.getJSONArray(featureKey);

            for (int i = 0; i < featureConfig.length(); i++) {
                String batchCodeStr = featureConfig.getString(i);
                if (batchCodeStr!=null && batchCodeStr.equals(batchCode)) {
                    Log.i("FeatureCheck", "Feature is visible for given batchCode and examCode.");
                    return true;
                }
            }

            Log.i("FeatureCheck", "Feature not visible for given batchCode and examCode.");
            return false;

        } catch (Exception e) {
            Log.e("FeatureCheck", "Exception occurred while checking feature visibility: ", e);
            return false;
        }
    }

    public static boolean isFeatureHidden(String featureKey) {
        try {
            String batchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
            Log.d("FeatureCheck", "Checking feature: " + featureKey +
                    ", batchCode: " + batchCode);

            String jsonStr = MyApplication.getFirebaseRemoteConfig().getString("featureHiddenBatch");

            if (jsonStr.isEmpty()) {
                Log.w("FeatureCheck", "Remote config 'featureVisibility' is empty.");
                return false;
            }

            JSONObject root = new JSONObject(jsonStr);

            if (!root.has(featureKey)) {
                Log.w("FeatureCheck", "Feature key not found in config: " + featureKey);
                return false;
            }

            JSONArray featureConfig = root.getJSONArray(featureKey);

            for (int i = 0; i < featureConfig.length(); i++) {
                String batchCodeStr = featureConfig.getString(i);
                if (batchCodeStr!=null && batchCodeStr.equals(batchCode)) {
                    Log.i("FeatureCheck", "Feature is visible for given batchCode and examCode.");
                    return true;
                }
            }

            Log.i("FeatureCheck", "Feature not visible for given batchCode and examCode.");
            return false;

        } catch (Exception e) {
            Log.e("FeatureCheck", "Exception occurred while checking feature visibility: ", e);
            return false;
        }
    }

    public static String getDialogText(String type, String key) {
        try {

            String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
            String jsonStr = MyApplication.getFirebaseRemoteConfig().getString("alert_dialog_text");

            if (jsonStr.isEmpty()) {
                return "";
            }

            JSONObject root = new JSONObject(jsonStr);

            if (!root.has(type)) {
                return "";
            }

            JSONObject typeConfig = root.getJSONObject(type);

            if (!typeConfig.has(lang)) {
                return "";
            }

            JSONObject langConfig = typeConfig.getJSONObject(lang);

            if (!langConfig.has(key)) {
                return "";
            }

            return langConfig.getString(key);

        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Returns whether a feature is visible for a given category based on Firebase Remote Config.
     *
     * <p>The "is_feature_visible" Remote Config parameter should be a JSON mapping feature names
     * to comma-separated category lists, e.g. {"feature1":"category1,category2"}.
     *
     * @param featureName The feature to check.
     * @param category    The category to check against.
     * @return true if the feature is visible for the category, false otherwise.
     * @throws JSONException if the Remote Config JSON is invalid.
     */
    public static boolean isFeatureVisible(String featureName, String category) throws JSONException {
        String jsonStr = MyApplication.getFirebaseRemoteConfig().getString("is_feature_visible");

        if (jsonStr.isEmpty()) {
            return true;
        }
        JSONObject root = new JSONObject(jsonStr);

        if (!root.has(featureName)) {
            return true;
        }
        String categoriesStr = root.getString(featureName);
        String[] categories = categoriesStr.split(",");
        
        for (String value : categories) {
            if (value.trim().equalsIgnoreCase(category)) {
                return false; // category explicitly hidden
            }
        }

        return true;
    }

    public static String getOnBoardingData(String key) throws JSONException {
        return null;
    }

    public static String getFirebaseOnBoardingKey(){
        return MyApplication.getFirebaseRemoteConfig().getString("on_boarding_conditions");
    }


    public static String getHomeScreenHowToVideoPoster(String selectedCourseId) throws JSONException {
        String jsonStr = MyApplication.getFirebaseRemoteConfig().getString("home_screen_how_to_video");
        JSONObject root = new JSONObject(jsonStr);

        if (root.has(selectedCourseId)) {
            JSONObject courseObj = root.getJSONObject(selectedCourseId);
            if (courseObj.has("url")) {
                return courseObj.getString("url");
            }
        }
        return "";
    }

    public static String getFeatureText(String featureName) {
        try {
            String jsonStr = MyApplication.getFirebaseRemoteConfig().getString("feature_text");
            String examCode = MyApplication.getStringData(PreferenceKeys.EXAM_CODE.getKey());

            if (jsonStr.isEmpty()) {
                Log.w("FeatureText", "Remote config 'feature_text' is empty.");
                return "";
            }

            JSONObject root = new JSONObject(jsonStr);

            if (!root.has(featureName)) {
                Log.w("FeatureText", "Feature key not found: " + featureName);
                return "";
            }
            JSONObject featureObj = root.getJSONObject(featureName);
            JSONObject courseObj;
            if (featureObj.has(examCode)) {
                courseObj = featureObj.getJSONObject(examCode);
            } else if (featureObj.has("default")) {
                courseObj = featureObj.getJSONObject("default");
            } else {
                Log.w("FeatureText", "Exam code not found under feature: " + examCode);
                return "";
            }
            String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
            if (lang == null || lang.isEmpty()) {
                lang = "en";
            }

            if (courseObj.has(lang)) {
                return courseObj.getString(lang);
            }

            Log.w("FeatureText", "Language text not found for: " + lang);
            return "";

        } catch (Exception e) {
            Log.e("FeatureText", "Exception while getting feature text: ", e);
            return "";
        }
    }



}


