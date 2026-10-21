package arivihan.technologies.doubtbuzzter2.helper.refer;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import com.google.gson.Gson;

/**
 * Captures the referral code when a new user installs the app via a referral link.
 *
 * Referral link format:
 *   https://play.google.com/store/apps/details?id=arivihan.technologies.doubtbuzzter2&referrer=REFERRAL_CODE
 *
 * Play Store passes the value of the "referrer" param (REFERRAL_CODE) to the app via
 * InstallReferrerClient after install. We store it in donotdelete SharedPreferences
 * under the key "referrer" for use during onboarding registration.
 *
 * Usage:
 *   StartActivity.onCreate()  → ReferralLinkHandler.checkInstallReferrer(context)
 *   Registration API call     → ReferralLinkHandler.getStoredReferrer()
 *   After successful submit   → ReferralLinkHandler.clearStoredReferrer()
 */
public class ReferralLinkHandler {

    private static final String TAG = "ReferralLinkHandler";
    public static final String KEY_REFERRER = "referrer";

    // Single background thread — no UI references, safe as static
    private static final ExecutorService sExecutor =
            Executors.newSingleThreadExecutor(r -> new Thread(r, TAG));

    private ReferralLinkHandler() { /* utility class — no instances */ }

    // -----------------------------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------------------------

    /**
     * Connects to the Play Store Install Referrer API and stores the referral code.
     *
     * Safe to call on every app launch:
     *  - Exits immediately if a referrer is already stored.
     *  - Uses application context internally to prevent any Activity context leak.
     *  - All IPC and blocking work runs on a managed background thread.
     *  - endConnection() is always called, even on errors or early returns.
     *
     * @param context any context — application context is used internally
     */
    public static void checkInstallReferrer(@NonNull Context context) {
        // Always use application context — no Activity leak risk
        final Context appContext = context.getApplicationContext();
        final InstallReferrerClient client = InstallReferrerClient.newBuilder(appContext).build();

        client.startConnection(new InstallReferrerStateListener() {

            @Override
            public void onInstallReferrerSetupFinished(int responseCode) {
                if (responseCode != InstallReferrerClient.InstallReferrerResponse.OK) {
                    Log.d(TAG, "InstallReferrer not available, code=" + responseCode);
                    safeEndConnection(client);
                    return;
                }

                // getInstallReferrer() is a blocking IPC call — must NOT run on main thread
                sExecutor.execute(() -> {
                    try {
                        ReferrerDetails details = client.getInstallReferrer();
                        if (details == null) {
                            Log.d(TAG, "ReferrerDetails is null");
                            return;
                        }

                        String rawReferrer = details.getInstallReferrer();
                        if (rawReferrer == null || rawReferrer.trim().isEmpty()) {
                            Log.d(TAG, "Referrer value is empty");
                            return;
                        }

                        String rawValue = rawReferrer.trim();
                        Log.d(TAG, "Raw referrer: " + rawValue);

                        // ── Handle UTM params & referral code (previously in ReferralUtils) ──
                        processUtmAndReferralData(rawValue);

                        // ── Handle plain referrer code (KEY_REFERRER) ──
                        if (!hasReferrer()) {
                            String referrer;
                            if (!rawValue.contains("=")) {
                                referrer = rawValue;
                            } else {
                                referrer = extractParam(rawValue, KEY_REFERRER);
                            }

                            if (referrer != null && !referrer.isEmpty()) {
                                MyApplication.saveDataForDoNotDeleteSharedPreference(
                                        KEY_REFERRER, referrer
                                );
                                Log.d(TAG, "Referrer stored: " + referrer);
                            } else {
                                Log.d(TAG, "No referrer value found, skipping");
                            }
                        } else {
                            Log.d(TAG, "Referrer already stored, skipping");
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "Failed to read referrer details", e);
                    } finally {
                        safeEndConnection(client);
                    }
                });
            }

            @Override
            public void onInstallReferrerServiceDisconnected() {
                Log.w(TAG, "InstallReferrer service disconnected unexpectedly");
            }
        });
    }

    /**
     * Processes UTM params and base64-encoded referral code from the raw referrer string.
     * Previously handled by ReferralUtils — consolidated here to use a single
     * InstallReferrerClient connection.
     */
    private static void processUtmAndReferralData(String rawValue) {
        try {
            if (MyApplication.getBooleanDataFromDoNotDeleteSharedPreference(
                    PreferenceKeys.REFERRAL_DATA_SET.getKey())) {
                return; // already processed
            }

            String decoded = URLDecoder.decode(rawValue, "UTF-8");
            Log.d(TAG, "Decoded referrer: " + decoded);

            // Store all UTM params
            try {
                Gson gson = new Gson();
                MyApplication.saveDataForDoNotDeleteSharedPreference(
                        PreferenceKeys.ALL_UTM_PARAMS.getKey(),
                        gson.toJson(extractAllQueryParams(decoded)));
            } catch (Exception ignored) {
            }

            // Course ID from utm_term + utm_content
            String utmTerm = extractParam(decoded, "utm_term");
            String utmContent = extractParam(decoded, "utm_content");
            if (PreferenceKeys.COURSE_ID.getKey().equals(utmTerm) && utmContent != null) {
                MyApplication.saveSharedPreferencesDoNotDelete(
                        PreferenceKeys.SELECTED_COURSE_ID.getKey(), utmContent);
            }

            // Base64-encoded referral code
            String referralCodeEncoded = extractParam(decoded, PreferenceKeys.REFERRAL_CODE.getKey());
            String utmSource = extractParam(decoded, "utm_source");

            if (referralCodeEncoded != null && !referralCodeEncoded.isEmpty()) {
                byte[] decodedBytes = android.util.Base64.decode(
                        referralCodeEncoded, android.util.Base64.DEFAULT);
                String referralCode = new String(decodedBytes);

                MyApplication.saveDataForDoNotDeleteSharedPreference(
                        PreferenceKeys.REFERRAL_CODE.getKey(), referralCode);
                MyApplication.saveDataForDoNotDeleteSharedPreference(
                        PreferenceKeys.REFERRAL_SOURCE.getKey(), utmSource);
                MyApplication.saveDataForDoNotDeleteSharedPreference(
                        PreferenceKeys.REFERRAL_DETECTED.getKey(), true);
                MyApplication.saveDataForDoNotDeleteSharedPreference(
                        PreferenceKeys.REFERRAL_REDIRECTED_PENDING.getKey(), true);
                MyApplication.saveDataForDoNotDeleteSharedPreference(
                        PreferenceKeys.REFERRAL_DATA_SET.getKey(), true);

                Log.d(TAG, "Referral code: " + referralCode + ", UTM source: " + utmSource);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing UTM/referral data", e);
        }
    }

    private static Map<String, String> extractAllQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                map.put(keyValue[0], keyValue[1]);
            }
        }
        return map;
    }

    /**
     * Returns the stored raw referrer string, or an empty string if none was captured.
     * Never returns null — safe to use directly without a null check.
     */
    @NonNull
    public static String getStoredReferrer() {
        String value = MyApplication.getStringDataFromDoNotDeleteSharedPreference(KEY_REFERRER);
        return value != null ? value : "";
    }

    /**
     * Returns the referral code, handling both link formats:
     *   - Plain code:     referrer=ARI12345              → "ARI12345"
     *   - Key=value pair: referrer=referral_code=ARI12345 → "ARI12345"
     *     (Play Store URL-decodes %3D before passing to InstallReferrerClient)
     *
     * Use this instead of getStoredReferrer() when sending the code to the backend.
     * Never returns null — safe to use directly without a null check.
     */
    @NonNull
    public static String getReferralCode() {
        String referrer = getStoredReferrer();
        if (referrer.startsWith("referral_code=")) {
            return referrer.substring("referral_code=".length()).trim();
        }
        return referrer;
    }

    /**
     * @return true if a referrer code was captured and is pending submission
     */
    public static boolean hasReferrer() {
        return !getStoredReferrer().isEmpty();
    }

    /**
     * Clears the stored referrer after it has been successfully submitted to the backend.
     * Call this inside the success callback of your registration API.
     */
    public static void clearStoredReferrer() {
        MyApplication.saveDataForDoNotDeleteSharedPreference(KEY_REFERRER, "");
        Log.d(TAG, "Referrer cleared after submission");
    }

    // -----------------------------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------------------------

    /**
     * Extracts a query param value from a raw referrer string.
     * If the string has no '=' (plain code), returns null so the caller can skip.
     * e.g. "utm_source=google&referrer=3d7ad2d" → key="referrer" → "3d7ad2d"
     */
    private static String extractParam(String query, String key) {
        if (!query.contains("=")) return null; // plain string, not a query
        String[] params = query.split("&");
        for (String param : params) {
            if (param.startsWith(key + "=")) {
                return param.split("=", 2)[1];
            }
        }
        return null;
    }

    private static void safeEndConnection(@NonNull InstallReferrerClient client) {
        try {
            client.endConnection();
        } catch (Exception e) {
            // endConnection() can throw if the service was already disconnected
            Log.w(TAG, "safeEndConnection: " + e.getMessage());
        }
    }
}
