package arivihan.technologies.doubtbuzzter2.activity.web_view.interfaces;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.activity.web_view.MicrolecturePractiseWebViewActivity;
import arivihan.technologies.doubtbuzzter2.activity.web_view.WebViewByUrlActivity;
import arivihan.technologies.doubtbuzzter2.helper.ActivityLauncher;
import arivihan.technologies.doubtbuzzter2.utils.PdfUtils;

public class MicrolecturePractiseWebviewInterface {
    Context mContext;
    WebView webView;
    MicrolecturePractiseWebViewActivity microlecturePractiseWebViewActivity;

    public MicrolecturePractiseWebviewInterface(Context context, WebView webView) {
        this.mContext = context;
        this.webView = webView;
        microlecturePractiseWebViewActivity = (MicrolecturePractiseWebViewActivity) context;
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
        params.put("source", RedirectionSource.MICROLECTURE_PRACTISE_WEBVIEW_INTERFACE.name());

        ActivityLauncher.launchActivity(mContext, className, params);
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
            PdfUtils.openPdfViewer(mContext, url, StringUtils.isNotEmpty(title) ? title : "",notesType,subjectName,false, "", "");
        } catch (ActivityNotFoundException e) {
            Log.d("openPdf:", e.getMessage());
        }
    }

    @JavascriptInterface
    public void backToPreviousActivity() {
        microlecturePractiseWebViewActivity.backToPreviousActivity();
    }

    @JavascriptInterface
    public void restrictBackPress(boolean isRestricted) {
        microlecturePractiseWebViewActivity.changeBackPressed(isRestricted);
    }

    @JavascriptInterface
    public void pressBack() {
        microlecturePractiseWebViewActivity.pressBack();
    }

    @JavascriptInterface
    public void setJsMethodCallBack(String methodName) {
        microlecturePractiseWebViewActivity.setJsMethodCallBack(methodName);
    }

    @JavascriptInterface
    public void showPositiveSnackBarMessage(String message) {
        microlecturePractiseWebViewActivity.showPositiveSnackBarMessage(message);
    }

    @JavascriptInterface
    public void showNegativeSnackBarMessage(String message) {
        microlecturePractiseWebViewActivity.showNegativeSnackBarMessage(message);
    }

    @JavascriptInterface
    public void showContinuity() {
        microlecturePractiseWebViewActivity.showContinuity();
    }

    @JavascriptInterface
    public void getRemoteConfigValue(String key) {
        String value = MyApplication.getFirebaseRemoteConfig().getString(key);
        String safeValue = value == null ? "" : value.replace("'", "\\'");

        String callback = "javascript:fetchRemoteConfigValue('" + key + "','" + safeValue + "')";
        microlecturePractiseWebViewActivity.runOnUiThread(() ->
                webView.evaluateJavascript(callback, null)
        );
    }

}
