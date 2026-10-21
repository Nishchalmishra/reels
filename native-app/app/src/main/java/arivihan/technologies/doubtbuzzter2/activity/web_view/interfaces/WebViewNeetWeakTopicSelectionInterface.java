package arivihan.technologies.doubtbuzzter2.activity.web_view.interfaces;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.airbnb.lottie.LottieAnimationView;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.activity.ImageCropperActivity;
import arivihan.technologies.doubtbuzzter2.activity.web_view.WebViewNeetWeakTopicSelectionActivity;
import arivihan.technologies.doubtbuzzter2.helper.ActivityLauncher;
import arivihan.technologies.doubtbuzzter2.model.LectureContentType;
import arivihan.technologies.doubtbuzzter2.utils.PdfUtils;

public class WebViewNeetWeakTopicSelectionInterface {
    Context mContext;
    WebView webView;
    WebViewNeetWeakTopicSelectionActivity webViewNeetWeakTopicSelectionActivity;
    private ActivityResultLauncher<Intent> neetCompletionScreenLauncher;

    private static final int CAMERA_PERMISSION_CODE = 400;
    private static final int GALLERY_CODE = 410;

    public ActivityResultLauncher<Intent> galleryLauncher;
    public ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<Intent> cropActivityLauncher;
    private Uri cameraImageUri;

    public WebViewNeetWeakTopicSelectionInterface(Context context, WebView webView) {
        this.mContext = context;
        this.webView = webView;
        webViewNeetWeakTopicSelectionActivity = (WebViewNeetWeakTopicSelectionActivity) context;
    }

    @JavascriptInterface
    public void openActivity(String className, String activityParams) {
        Map<String, Object> params = new HashMap<>();

        Log.i("OPEN_ACTICITY", "PARAMS :: " + activityParams);

        if (activityParams != null && !activityParams.equals("null")) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                params = mapper.readValue(activityParams, new TypeReference<Map<String, Object>>() {
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        params.put("source", RedirectionSource.WEBVIEW_NEET_WEAK_TOPIC.name());

        ActivityLauncher.launchActivity(mContext, className, params);
    }

    @JavascriptInterface
    public void openSubscriptionScreen(String jsonDto) {
        webViewNeetWeakTopicSelectionActivity.openSubscriptionScreen(jsonDto);
    }

    @JavascriptInterface
    public void openNeetCompletionScreen(String type) {
        webViewNeetWeakTopicSelectionActivity.openNeetCompletionScreen(type);
    }

    @JavascriptInterface
    public void setTopicSelection(String topicId) {
        webViewNeetWeakTopicSelectionActivity.setTopicSelection(topicId);
    }

    @JavascriptInterface
    public void showToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void shareOnWhatsapp(String message) {
        try {

            String encodedMessage = message.replace("\\n", "\n");
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, encodedMessage);
            sendIntent.setType("text/plain");
            sendIntent.setPackage("com.whatsapp"); // Targets WhatsApp directly
            mContext.startActivity(sendIntent);
        } catch (Exception e) {
        }
    }

    @JavascriptInterface
    public void shareContent(String title, String content) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TITLE, title);
        shareIntent.putExtra(Intent.EXTRA_TEXT, content);
        mContext.startActivity(Intent.createChooser(shareIntent, ""));
    }

    @JavascriptInterface
    public void openPdf(String url, String title, String notesType, String subjectName) {
        try {
            PdfUtils.openPdfViewer(mContext, url, StringUtils.isNotEmpty(title) ? title : "", notesType, subjectName, false, "", "");
        } catch (ActivityNotFoundException e) {
            Log.d("openPdf:", e.getMessage());
        }
    }

    @JavascriptInterface
    public void backToPreviousActivity() {
        webViewNeetWeakTopicSelectionActivity.backToPreviousActivity();
    }

    @JavascriptInterface
    public void restrictBackPress(boolean isRestricted) {
        webViewNeetWeakTopicSelectionActivity.changeBackPressed(isRestricted);
    }

    @JavascriptInterface
    public void pressBack() {
        webViewNeetWeakTopicSelectionActivity.pressBack();
    }

    @JavascriptInterface
    public void setJsMethodCallBack(String methodName) {
        webViewNeetWeakTopicSelectionActivity.setJsMethodCallBack(methodName);
    }

    @JavascriptInterface
    public void showPositiveSnackBarMessage(String message) {
        webViewNeetWeakTopicSelectionActivity.showPositiveSnackBarMessage(message);
    }

    @JavascriptInterface
    public void showNegativeSnackBarMessage(String message) {
        webViewNeetWeakTopicSelectionActivity.showNegativeSnackBarMessage(message);
    }

    @JavascriptInterface
    public void setFirebaseEvent(String eventName, String eventParams) {
        Map<String, String> params = new HashMap<>();
        if (eventParams != null && !eventParams.equals("null")) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                params = mapper.readValue(eventParams, new TypeReference<Map<String, String>>() {
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        webViewNeetWeakTopicSelectionActivity.setFirebaseEvent(eventName, params);
    }

    @JavascriptInterface
    public void setOrientationLandscape(boolean isLandscape) {
        webViewNeetWeakTopicSelectionActivity.setOrientationLandscape(isLandscape);
    }

    @JavascriptInterface
    public void shareLiveClass() {
        webViewNeetWeakTopicSelectionActivity.shareLiveClass();
    }

    @JavascriptInterface
    public void shareLiveClass(String message) {
        webViewNeetWeakTopicSelectionActivity.shareLiveClass(message);
    }

    @JavascriptInterface
    public void setPreference(String key, String value) {
        MyApplication.saveData(key, value);
    }

    @JavascriptInterface
    public void getPreference(String key) {
        String value = MyApplication.getStringData(key);
        String safeValue = value == null ? "" : value.replace("'", "\\'");

        String callBack = "javascript:fetchPreferenceValue('" + key + "','" + safeValue + "')";
        webViewNeetWeakTopicSelectionActivity.runOnUiThread(() ->
                webView.evaluateJavascript(callBack, null)
        );
    }

    @JavascriptInterface
    public void setDoNotDeletePreference(String key, String value) {
        MyApplication.saveDataForDoNotDeleteSharedPreference(key, value);
    }

    @JavascriptInterface
    public void getDoNotDeletePreference(String key) {
        String value = MyApplication.getDoNotDeleteSharedPrefKey(key);
        String safeValue = value == null ? "" : value.replace("'", "\\'");

        String callBack = "javascript:fetchDoNotDeletePreferenceValue('" + key + "','" + safeValue + "')";
        webViewNeetWeakTopicSelectionActivity.runOnUiThread(() ->
                webView.evaluateJavascript(callBack, null)
        );
    }

    @JavascriptInterface
    public void getRemoteConfigValue(String key) {
        FirebaseRemoteConfig remoteConfig = MyApplication.getFirebaseRemoteConfig();

        remoteConfig.fetchAndActivate()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String value = remoteConfig.getString(key);
                        String safeValue = value.replace("'", "\\'");

                        String callback =
                                "javascript:fetchRemoteConfigValue('" + key + "','" + safeValue + "')";

                        webViewNeetWeakTopicSelectionActivity.runOnUiThread(() ->
                                webView.evaluateJavascript(callback, null)
                        );
                    }
                });
    }

    @JavascriptInterface
    public void startRealtimeDbListener(String path) {
        webViewNeetWeakTopicSelectionActivity.startRealtimeDbListener(path);
    }

    @JavascriptInterface
    public void editImage(String uriString) {
        try {
            webViewNeetWeakTopicSelectionActivity.openCropActivity(Uri.parse(uriString));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @JavascriptInterface
    public void openGallery(String maxCount) {
        try {
            webViewNeetWeakTopicSelectionActivity.openGallery(Integer.parseInt(maxCount));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @JavascriptInterface
    public void openCamera() {
        try {
            webViewNeetWeakTopicSelectionActivity.openCamera();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @JavascriptInterface
    public void minimizeKeyboard() {
        try {
            webViewNeetWeakTopicSelectionActivity.minimizeKeyboard();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @JavascriptInterface
    public void listenKeyboardEnter() {
        try {
            webViewNeetWeakTopicSelectionActivity.listenKeyboardEnter();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void goToHomeScreen() {
        try {
            webViewNeetWeakTopicSelectionActivity.goToHomeScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void openGuideVideo() {
        try {
            webViewNeetWeakTopicSelectionActivity.openGuideVideo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
