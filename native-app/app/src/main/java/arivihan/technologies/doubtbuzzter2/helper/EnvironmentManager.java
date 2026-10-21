package arivihan.technologies.doubtbuzzter2.helper;

import android.util.Log;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;

public final class EnvironmentManager {

    private static final String TAG = "EnvironmentManager";
    private static final String PREF_ENV_KEY = "currentActiveEnv";

    /**
     * The resolved endpoint. Once set to a valid value it is never overwritten with
     * null/empty — {@link #setEndPoint(String)} rejects blank values and
     * {@link #loadFromRemoteConfigIfRequired()} skips if already set.
     */
    private static volatile String endPoint;

    private EnvironmentManager() {
    }

    /**
     * Resolves the endpoint once per process lifetime:
     * 1. If user manually selected an environment (persisted in DoNotDelete prefs) → use that
     * 2. Else read the ACTIVE key from remote config "app_environments_v7"
     * 3. On any failure → fall back to BuildConfig.BASE_URL (compile-time default)
     *
     * The early-return guard ensures we never re-evaluate after the first successful load,
     * preventing a stale/empty remote config fetch from overwriting a valid endpoint mid-session.
     */
    private static synchronized void loadFromRemoteConfigIfRequired() {
        // Already resolved — don't re-evaluate
        if (StringUtils.isNotBlank(endPoint)) return;

        // Priority 1: user-selected environment (persisted across sessions)
        String savedEnvUrl = MyApplication.getDoNotDeleteSharedPrefKey(PREF_ENV_KEY);
        if (StringUtils.isNotBlank(savedEnvUrl)) {
            endPoint = savedEnvUrl;
            Log.d(TAG, "Endpoint loaded from saved preference: " + endPoint);
            return;
        }

        // Priority 2: remote config active environment
        try {
            FirebaseRemoteConfig remoteConfig = MyApplication.getFirebaseRemoteConfig();
            String json = remoteConfig.getString("app_environments_v8");

            if (StringUtils.isNotBlank(json)) {
                JSONObject obj = new JSONObject(json);
                String activeEnvKey = obj.optString("ACTIVE", "PROD");
                String resolved = obj.optString(activeEnvKey, "");

                if (StringUtils.isNotBlank(resolved)) {
                    endPoint = resolved;
                    persistEndpoint(resolved);
                    Log.d(TAG, "Endpoint loaded from remote config [" + activeEnvKey + "]: " + endPoint);
                    return;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Remote config parse failed", e);
        }

        // Priority 3: compile-time default (always valid)
        endPoint = BuildConfig.BASE_URL;
        persistEndpoint(endPoint);
        Log.d(TAG, "Endpoint loaded from BuildConfig default: " + endPoint);
    }

    public static String getEndPoint() {
        // Fast path: already resolved — no lock needed.
        // The volatile field guarantees visibility across threads.
        if (StringUtils.isNotBlank(endPoint)) return endPoint;
        loadFromRemoteConfigIfRequired();
        return endPoint;
    }

    /**
     * Persists the resolved endpoint so the next cold start has a reliable value
     * even if remote config is slow or unavailable.
     */
    private static void persistEndpoint(String url) {
        try {
            MyApplication.saveSharedPreferencesDoNotDelete(PREF_ENV_KEY, url);
        } catch (Exception e) {
            Log.e(TAG, "Failed to persist endpoint", e);
        }
    }

    /**
     * Called when the user manually switches environment from the Settings picker.
     * Null/empty values are rejected to prevent the app from hitting a blank URL.
     */
    public static void setEndPoint(String url) {
        if (StringUtils.isBlank(url)) {
            Log.e(TAG, "setEndPoint called with null/empty URL — ignored");
            return;
        }
        endPoint = url;
        persistEndpoint(url);
        Log.d(TAG, "Endpoint manually set to: " + url);
    }

    /**
     * Clears the saved environment so the next launch re-reads from remote config.
     * Called during logout to ensure a clean state.
     */
    public static void clearSavedEnvironment() {
        endPoint = null;
        MyApplication.removeFromDoNotDeleteSharedPreference(PREF_ENV_KEY);
        Log.d(TAG, "Saved environment cleared");
    }

    // 🔹 Get Environment Name from URL
    public static String getEnvironmentNameByUrl(String url) {
        try {
            FirebaseRemoteConfig remoteConfig = MyApplication.getFirebaseRemoteConfig();
            String json = remoteConfig.getString("app_environments_v8");

            JSONObject obj = new JSONObject(json);
            Iterator<String> keys = obj.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                if (!key.equalsIgnoreCase("ACTIVE")) {
                    if (url != null && url.equals(obj.optString(key))) {
                        return key;
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return "PROD";
    }

    // 🔹 Dynamic list for picker
    public static Map<String, String> getEnvironmentList() {
        Map<String, String> envMap = new HashMap<>();

        try {
            FirebaseRemoteConfig remoteConfig = MyApplication.getFirebaseRemoteConfig();
            String json = remoteConfig.getString("app_environments_v8");

            JSONObject obj = new JSONObject(json);
            Iterator<String> keys = obj.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                if (!"ACTIVE".equalsIgnoreCase(key)) {
                    envMap.put(key, obj.optString(key));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse env list", e);
        }

        return envMap;
    }
}
