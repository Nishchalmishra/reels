package arivihan.technologies.doubtbuzzter2.common.cache;

import android.content.Context;
import android.webkit.WebView;

import java.io.File;

public class CleanCache
{
    private static final String TAG = CleanCache.class.getSimpleName();

    public static void clearInternalCache(Context context) {
        try {
            deleteDir(context.getCacheDir());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearExternalCache(Context context) {
        try {
            deleteDir(context.getExternalCacheDir());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearWebViewCache(Context context) {
        try {
            WebView webView = new WebView(context);
            webView.clearCache(true);
            webView.clearHistory();
            webView.clearFormData();

            // Also delete the WebView cache folder manually
            File cacheDir = new File(context.getCacheDir(), "webview");
            deleteDir(cacheDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearFiles(Context context) {
        deleteDir(context.getFilesDir());
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) return false;
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }


}
