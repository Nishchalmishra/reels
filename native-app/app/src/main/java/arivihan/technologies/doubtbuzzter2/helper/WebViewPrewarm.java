package arivihan.technologies.doubtbuzzter2.helper;

import android.content.Context;
import android.util.Log;
import android.webkit.WebView;

public final class WebViewPrewarm {

    private static final String TAG = "WebViewPrewarm";

    private static boolean warmed;

    private WebViewPrewarm() {
    }

    public static void prewarm(Context context) {
        if (warmed) {
            return;
        }
        warmed = true;
        try {
            long start = System.currentTimeMillis();
            new WebView(context).destroy();
            Log.d(TAG, "WebView engine warmed in " + (System.currentTimeMillis() - start) + "ms");
        } catch (Throwable t) {
            Log.w(TAG, "WebView prewarm skipped", t);
        }
    }
}
