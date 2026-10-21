package arivihan.technologies.doubtbuzzter2.helper;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import androidx.activity.result.ActivityResultLauncher;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.activity.web_view.WebViewByUrlActivity;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.helper.firebase.FirebaseMessagingManager;
import arivihan.technologies.doubtbuzzter2.model.HomeScreenCacheData;
import arivihan.technologies.doubtbuzzter2.model.subject.SubjectResponse;
import arivihan.technologies.doubtbuzzter2.utils.PdfUtils;

public class WebviewByUrlInterface {
    Context mContext;
    WebView webView;
    WebViewByUrlActivity webViewByUrlActivity;
    private ActivityResultLauncher<Intent> neetCompletionScreenLauncher;

    public WebviewByUrlInterface(Context context, WebView webView) {
        this.mContext = context;
        this.webView = webView;
        webViewByUrlActivity = (WebViewByUrlActivity) context;
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
        params.put("source", RedirectionSource.WEBVIEW_BY_URL_INTERFACE.name());

        ActivityLauncher.launchActivity(mContext, className, params);
    }

    /**
     * Opens the REAL native "Personalized Path" webview for the given subject
     * (e.g. "Physics") — used by the NEET diagnostic's "Show My Personalized
     * Path" button, which otherwise has no way to resolve the learningPathId/
     * batchPathId/subjectId a fresh webview needs. Resolves the subject from
     * the SAME SharedPreferences-backed cache HomeScreenViewModel.
     * getPersonalizedPathData() writes (MyApplication.getSubjectModelList /
     * HomeScreenCacheData.AV_COMPETITIVE_PERSONALIZED_PATH) — NOT the plain
     * AV_SUBJECTS_V2 subject list, whose /v2/subjects payload never includes
     * learningPathId/batchPathId at all (see SubjectResponse's "not in new
     * API" fields), which is why using that cache made this page load with
     * every item empty. So it works without the Fragment being on screen —
     * but that also means it can come up empty if Home's Personalized Path
     * section hasn't loaded at least once yet. Mirrors
     * CompetitiveHomeScreenFragment.onSubjectClick's core navigation (minus
     * its first-time mini-video pre-roll, which needs Fragment-only state and
     * isn't essential here).
     */
    @JavascriptInterface
    public void openPersonalizedPath(String subjectName) {
        try {
            List<SubjectResponse> subjects = MyApplication.getSubjectModelList(HomeScreenCacheData.AV_COMPETITIVE_PERSONALIZED_PATH.name());
            SubjectResponse match = null;
            for (SubjectResponse s : subjects) {
                if (subjectName != null && (subjectName.equalsIgnoreCase(s.getSubjectName())
                        || subjectName.equalsIgnoreCase(s.getSubjectCode()))) {
                    match = s;
                    break;
                }
            }
            if (match == null) {
                Log.w("PinjraTodDiagnostic", "openPersonalizedPath: no cached subject matched '" + subjectName
                        + "' (cached list size=" + subjects.size() + ") — has Home's Personalized Path section loaded yet?");
                webViewByUrlActivity.runOnUiThread(() ->
                        Toast.makeText(mContext, "Please open Home once, then try again.", Toast.LENGTH_SHORT).show());
                return;
            }
            Log.d("PinjraTodDiagnostic", "openPersonalizedPath: matched subject=" + match.getSubjectName()
                    + " learningPathId=" + match.getLearningPathId() + " batchPathId=" + match.getBatchPathId() + " subjectId=" + match.getId());

            Map<String, Object> params = new HashMap<>();
            params.put("learningPathId", match.getLearningPathId());
            params.put("batchPathId", match.getBatchPathId());
            params.put("subjectId", match.getId());
            params.put("subjectName", match.getSubjectName());

            webViewByUrlActivity.runOnUiThread(() ->
                    CommonNavigator.navigateToWebViewByUrlParams(mContext,
                            CommonMethods.getPersonalizedPathUrl(),
                            params,
                            true, true,
                            false, false,
                            false, false, null, true, true, false, false, null,
                            "PinjraTodDiagnostic", null)
            );
        } catch (Exception e) {
            Log.e("PinjraTodDiagnostic", "openPersonalizedPath failed", e);
        }
    }

    @JavascriptInterface
    public void openSubscriptionScreen(String jsonDto) {
        webViewByUrlActivity.openSubscriptionScreen(jsonDto);
    }

    @JavascriptInterface
    public void openNeetCompletionScreen(String type) {
        webViewByUrlActivity.openNeetCompletionScreen(type);
    }

    @JavascriptInterface
    public void setTopicSelection(String topicId) {
        webViewByUrlActivity.setTopicSelection(topicId);
    }

    @JavascriptInterface
    public void openGallery(String maxCount) {
        try {
            webViewByUrlActivity.openGallery(Integer.parseInt(maxCount));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @JavascriptInterface
    public void openCamera() {
        try {
            webViewByUrlActivity.openCamera();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @JavascriptInterface
    public void editImage(String uriString) {
        try {
            webViewByUrlActivity.openCropActivity(Uri.parse(uriString));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @JavascriptInterface
    public void openFilePicker(String maxCount) {
        try {
            webViewByUrlActivity.openFilePicker(Integer.parseInt(maxCount));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
        webViewByUrlActivity.runOnUiThread(() -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TITLE, title);
            shareIntent.putExtra(Intent.EXTRA_TEXT, content);
            mContext.startActivity(Intent.createChooser(shareIntent, ""));
        });
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
        webViewByUrlActivity.backToPreviousActivity();
    }

    @JavascriptInterface
    public void restrictBackPress(boolean isRestricted) {
        webViewByUrlActivity.changeBackPressed(isRestricted);
    }

    @JavascriptInterface
    public void pressBack() {
        webViewByUrlActivity.pressBack();
    }

    @JavascriptInterface
    public void setJsMethodCallBack(String methodName) {
        webViewByUrlActivity.setJsMethodCallBack(methodName);
    }

    @JavascriptInterface
    public void showPositiveSnackBarMessage(String message) {
        webViewByUrlActivity.showPositiveSnackBarMessage(message);
    }

    @JavascriptInterface
    public void showNegativeSnackBarMessage(String message) {
        webViewByUrlActivity.showNegativeSnackBarMessage(message);
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
        webViewByUrlActivity.setFirebaseEvent(eventName, params);
    }

    @JavascriptInterface
    public void setOrientationLandscape(boolean isLandscape) {
        webViewByUrlActivity.setOrientationLandscape(isLandscape);
    }

    @JavascriptInterface
    public void shareLiveClass() {
        webViewByUrlActivity.shareLiveClass();
    }

    @JavascriptInterface
    public void shareLiveClass(String message) {
        webViewByUrlActivity.shareLiveClass(message);
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
        webViewByUrlActivity.runOnUiThread(() ->
                webView.evaluateJavascript(callBack, null)
        );
    }

    @JavascriptInterface
    public void openInBrowser(String url) {
        webViewByUrlActivity.runOnUiThread(() -> webViewByUrlActivity.openExternalUrl(url));
    }

    @JavascriptInterface
    public void openMeet(String meetLink) {
        webViewByUrlActivity.runOnUiThread(() -> webViewByUrlActivity.openMeet(meetLink));
    }

    @JavascriptInterface
    public void setDoNotDeletePreference(String key, String value) {
//        Log.d("PinjraTodCard", "setDoNotDeletePreference: key=" + key + " value=" + value);
        MyApplication.saveDataForDoNotDeleteSharedPreference(key, value);
    }

    @JavascriptInterface
    public void subscribeToVirtualBatchTopic(String batchId) {
        if (StringUtils.isEmpty(batchId)) return;
        String prefKey = "batch_topic_subscribed_" + batchId;
        if (!MyApplication.getBoolData(prefKey)) {
            FirebaseMessagingManager.subscribeToTopic(batchId);
            MyApplication.saveData(prefKey, true);
        }
    }

    @JavascriptInterface
    public void unsubscribeFromVirtualBatchTopic(String batchId) {
        if (StringUtils.isEmpty(batchId)) return;
        String prefKey = "batch_topic_subscribed_" + batchId;
        FirebaseMessagingManager.unsubscribeFromTopic(batchId);
        MyApplication.saveData(prefKey, false);
    }

    @JavascriptInterface
    public void getDoNotDeletePreference(String key) {
        String value = MyApplication.getDoNotDeleteSharedPrefKey(key);
        String safeValue = value == null ? "" : value.replace("'", "\\'");

        String callBack = "javascript:fetchDoNotDeletePreferenceValue('" + key + "','" + safeValue + "')";
        webViewByUrlActivity.runOnUiThread(() ->
                webView.evaluateJavascript(callBack, null)
        );
    }

    @JavascriptInterface
    public boolean hasMediaPermission() {
        return ContextCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }

    @JavascriptInterface
    public String mediaPermissionState() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            return "granted";
        }
        boolean everRequested = MyApplication.getBoolData("mic_permission_ever_requested");
        boolean shouldShow = webViewByUrlActivity.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO);
        if (!everRequested || shouldShow) {
            return "askable";
        }
        return "blocked";
    }

    @JavascriptInterface
    public void requestMediaPermission(boolean video) {
        webViewByUrlActivity.runOnUiThread(() -> webViewByUrlActivity.requestMediaPermission(video));
    }

    @JavascriptInterface
    public void openAppSettings() {
        webViewByUrlActivity.runOnUiThread(() -> webViewByUrlActivity.openAppSettings());
    }

    @JavascriptInterface
    public void onTestStarted(String requestId) {
        webViewByUrlActivity.onTestStarted(requestId);
    }

    @JavascriptInterface
    public void getRemoteConfigValue(String key) {
        String value = MyApplication.getFirebaseRemoteConfig().getString(key);
        String safeValue = value == null ? "" : value.replace("'", "\\'");

        String callback = "javascript:fetchRemoteConfigValue('" + key + "','" + safeValue + "')";
        webViewByUrlActivity.runOnUiThread(() ->
                webView.evaluateJavascript(callback, null)
        );
    }
}
