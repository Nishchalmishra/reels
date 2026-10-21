package arivihan.technologies.doubtbuzzter2.helper;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

/**
 * Resolves PDF loader animation texts from Firebase Remote Config.
 *
 * Remote config key: "pdf_loader_text_v2"
 *
 * JSON structure:
 * {
 *   "EXAM_CODE": {
 *     "NOTES_TYPE": {
 *       "en": ["line1", "line2", ...],
 *       "hi": ["line1", "line2", ...]
 *     }
 *   },
 *   "general": {
 *     "default": {
 *       "en": ["general fallback en"],
 *       "hi": ["general fallback hi"]
 *     }
 *   }
 * }
 *
 * Fallback chain:
 * 1. examCode + notesType + lang
 * 2. examCode + "default" + lang
 * 3. "general" + notesType + lang
 * 4. "general" + "default" + lang
 * 5. Empty list (caller handles final string-resource fallback)
 */
public class PdfLoaderTextProvider {

    private static final String TAG = "PdfLoaderTextProvider";
    private static final String CONFIG_KEY = "pdf_loader_text_v2";
    private static final String FALLBACK_EXAM = "general";
    private static final String FALLBACK_TYPE = "default";

    private PdfLoaderTextProvider() {}

    public static List<String> getLoaderTexts(String notesType) {
        String examCode = MyApplication.getStringData(PreferenceKeys.EXAM_CODE.getKey());
        String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        return getLoaderTexts(examCode, notesType, lang);
    }

    public static List<String> getLoaderTexts(String examCode, String notesType, String lang) {
        if (lang == null || lang.isEmpty()) lang = "en";
        if (examCode == null) examCode = "";
        if (notesType == null) notesType = "";

        try {
            String jsonString = MyApplication.getFirebaseRemoteConfig().getString(CONFIG_KEY);
            if (jsonString == null || jsonString.isEmpty()) return Collections.emptyList();

            JSONObject root = new JSONObject(jsonString);

            // 1. examCode + notesType + lang
            List<String> result = resolve(root, examCode, notesType, lang);
            if (!result.isEmpty()) return result;

            // 2. examCode + "default" + lang
            result = resolve(root, examCode, FALLBACK_TYPE, lang);
            if (!result.isEmpty()) return result;

            // 3. "general" + notesType + lang
            result = resolve(root, FALLBACK_EXAM, notesType, lang);
            if (!result.isEmpty()) return result;

            // 4. "general" + "default" + lang
            return resolve(root, FALLBACK_EXAM, FALLBACK_TYPE, lang);

        } catch (Exception e) {
            Log.e(TAG, "Error parsing pdf loader text config", e);
            return Collections.emptyList();
        }
    }

    private static List<String> resolve(JSONObject root, String examCode, String notesType, String lang) {
        try {
            if (!root.has(examCode)) return Collections.emptyList();
            JSONObject examObj = root.getJSONObject(examCode);

            if (!examObj.has(notesType)) return Collections.emptyList();
            JSONObject typeObj = examObj.getJSONObject(notesType);

            if (!typeObj.has(lang)) return Collections.emptyList();
            JSONArray arr = typeObj.getJSONArray(lang);

            List<String> texts = new ArrayList<>(arr.length());
            for (int i = 0; i < arr.length(); i++) {
                String text = arr.optString(i, "").trim();
                if (!text.isEmpty()) texts.add(text);
            }
            return texts;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
