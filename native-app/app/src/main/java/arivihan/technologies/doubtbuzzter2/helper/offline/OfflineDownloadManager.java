package arivihan.technologies.doubtbuzzter2.helper.offline;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.model.offline.type.LanguageNameModel;
import arivihan.technologies.doubtbuzzter2.model.offline.type.NoteTypeColorModel;
import arivihan.technologies.doubtbuzzter2.model.offline.type.OfflineNoteConfigModel;
import arivihan.technologies.doubtbuzzter2.model.offline.type.SubjectColorModel;

public class OfflineDownloadManager {
    private static final String TAG = OfflineDownloadManager.class.getSimpleName();

    public static final String PREF_LAST_SAVED_DATE = "PREF_LAST_OFFLINE_TAGS_SAVED_DATE";
    public static final String PREF_OFFLINE_DOWNLOAD_TAG = "PREF_OFFLINE_DOWNLOAD_TAG";
    private static final String DEFAULT_COLOR = "#26C6DA";
    public static void getFirebaseRemoteConfigOfflineDownloadTag() {
        try {
            if (!isTodayDifferentFromLastUpdate()) {
                return;
            }

            String data = MyApplication.getFirebaseRemoteConfig().getString("offline_download_tags");
            Gson gson = new Gson();
            OfflineNoteConfigModel offlineNoteConfigModel = gson.fromJson(data, OfflineNoteConfigModel.class);

            MyApplication.saveOfflineNoteConfig(PREF_OFFLINE_DOWNLOAD_TAG, offlineNoteConfigModel);
            if(offlineNoteConfigModel!=null) {
                saveTodayAsLastUpdateDate();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Pair<String, String> getSubjectInfo(
            OfflineNoteConfigModel config,
            String subjectKey,
            String langCode
    ) {
        try {
//            Log.d(TAG, "Checking subject type: "+subjectKey+", --> "+(config!=null?config.toString():""));
            if (config != null && config.getCategories() != null) {
                SubjectColorModel model = config.getCategories().get(subjectKey);
                if (model != null) {
//                    Log.d(TAG, "Checking subject type:--> "+model.toString());
                    String name = getLocalizedName(model.getName(), langCode, subjectKey);
                    String color = model.getColorHex() != null ? model.getColorHex() : DEFAULT_COLOR;
                    return new Pair<>(name, color);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new Pair<>(subjectKey, DEFAULT_COLOR);
    }

    public static Pair<String, String> getNoteTypeInfo(
            OfflineNoteConfigModel config,
            String noteTypeKey,
            String langCode
    ) {
        try {
            Log.d(TAG, "Checking note type: "+noteTypeKey);
            if (config != null && config.getNoteTypes() != null) {
                NoteTypeColorModel model = config.getNoteTypes().get(noteTypeKey);
                if (model != null) {
                    String name = getLocalizedName(model.getName(), langCode, noteTypeKey);
                    String color = model.getColorHex() != null ? model.getColorHex() : DEFAULT_COLOR;
                    return new Pair<>(name, color);
                }
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return new Pair<>(noteTypeKey, DEFAULT_COLOR);
    }

    private static String getLocalizedName(LanguageNameModel nameModel, String langCode, String defaultValue) {

        if (nameModel == null) return defaultValue;

        switch (langCode.toLowerCase()) {
            case "hi":
                return nameModel.getHi() != null ? nameModel.getHi() : defaultValue;
            case "en":
            default:
                return nameModel.getEn() != null ? nameModel.getEn() : defaultValue;
        }
    }

    private static boolean isTodayDifferentFromLastUpdate() {
        String lastDate = MyApplication.getStringData(PREF_LAST_SAVED_DATE);
        String today = getTodayDateString();

        if (lastDate == null) {
            return true;
        }
        return !today.equals(lastDate);
    }

    private static void saveTodayAsLastUpdateDate() {
        MyApplication.saveData(PREF_LAST_SAVED_DATE, getTodayDateString());
    }

    private static String getTodayDateString() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
}
