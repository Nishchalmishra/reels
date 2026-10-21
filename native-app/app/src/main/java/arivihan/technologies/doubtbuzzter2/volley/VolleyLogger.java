package arivihan.technologies.doubtbuzzter2.volley;

import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.util.Map;

import arivihan.technologies.doubtbuzzter2.BuildConfig;

/**
 * DEBUG-only interceptor logger for Volley requests/responses.
 * All methods are no-ops in release builds.
 *
 * Logcat filter:  tag:API_LOG
 */
public final class VolleyLogger {

    static final String TAG = "API_LOG";
    private static final int MAX_BODY_LENGTH = 3000;

    private VolleyLogger() {}

    /** Log outgoing request: method, URL, and optional body. */
    public static void logRequest(int method, String url, @Nullable Object body) {
        if (!BuildConfig.DEBUG) return;
        Log.d(TAG, "→ " + methodName(method) + " " + url);
        if (body != null) {
            Log.d(TAG, "  Body: " + body);
        }
    }

    /** Log successful response: status code, URL, and response body. */
    public static void logResponse(int statusCode, String url,
                                   byte[] data, Map<String, String> headers) {
        if (!BuildConfig.DEBUG) return;
        try {
            String body = new String(data, HttpHeaderParser.parseCharset(headers, "utf-8"));
            Log.d(TAG, "← " + statusCode + " " + url);
            Log.d(TAG, "  " + truncate(body));
        } catch (Exception ignored) {}
    }

    /** Log error response: status code (if any), URL, and error body. */
    public static void logError(String url, VolleyError error) {
        if (!BuildConfig.DEBUG) return;
        if (error.networkResponse != null) {
            try {
                String body = new String(error.networkResponse.data, "utf-8");
                Log.e(TAG, "← ERR " + error.networkResponse.statusCode + " " + url);
                Log.e(TAG, "  " + truncate(body));
            } catch (Exception ignored) {
                Log.e(TAG, "← ERR " + error.networkResponse.statusCode + " " + url);
            }
        } else {
            Log.e(TAG, "← ERR (no network response) " + url + " — " + error);
        }
    }

    private static String truncate(String s) {
        return s.length() > MAX_BODY_LENGTH ? s.substring(0, MAX_BODY_LENGTH) + "…" : s;
    }

    private static String methodName(int method) {
        switch (method) {
            case Request.Method.GET:    return "GET";
            case Request.Method.POST:   return "POST";
            case Request.Method.PUT:    return "PUT";
            case Request.Method.DELETE: return "DELETE";
            case Request.Method.PATCH:  return "PATCH";
            default:                    return "HTTP(" + method + ")";
        }
    }
}
