package arivihan.technologies.doubtbuzzter2.common.bypass;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.apache.commons.lang3.StringUtils;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.enums.ChapterEnum.ChapterType;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class ChapterLiveByPass {
    private static final String TAG = ChapterLiveByPass.class.getSimpleName();
    public static boolean isByPassedUser() {
        try{
            String bypassLive = MyApplication.getFirebaseRemoteConfig().getString("chapter_live_by_pass_user");
            String myPhone = MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey());
            return bypassLive.contains(myPhone);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static boolean isChapterStatusAllowedToByPass(String type, String status) {
        try{
            if(ChapterType.LIVE_CHAPTER.name().equalsIgnoreCase(type)) {
                String bypassLive = MyApplication.getFirebaseRemoteConfig().getString("chapter_live_by_pass_status");
                return bypassLive.contains(status);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
