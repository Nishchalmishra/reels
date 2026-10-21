package arivihan.technologies.doubtbuzzter2.helper;

import java.util.Map;

public interface DoubtChatHost {
    void backToPreviousActivity();
    void showDialogForSubscription();
    void setFirebaseEvent(String eventName, Map<String, String> params);
    void launchGalleryPicker();
    void checkCameraPermission(String permission, int requestCode);
    void setChatSessionId(String chatSessionId);
}