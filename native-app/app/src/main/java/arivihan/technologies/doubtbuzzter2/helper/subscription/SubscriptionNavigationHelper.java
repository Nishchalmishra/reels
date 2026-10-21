package arivihan.technologies.doubtbuzzter2.helper.subscription;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.activity.CampaignSubscriptionActivity;
import arivihan.technologies.doubtbuzzter2.activity.ProPlanSubscriptionActivity;
import arivihan.technologies.doubtbuzzter2.activity.SubscriptionPaymentScreen;
import arivihan.technologies.doubtbuzzter2.enums.AnalyticsEvent;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.enums.feature.FeatureEnum;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.helper.ScreenAnalyticsHelper;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionLockPopupDto;

public class SubscriptionNavigationHelper {

    /**
     * Intent extra key carrying the RedirectionSource enum.
     */
    public static final String EXTRA_SOURCE = "sub_nav_source";

    /**
     * Navigate to the appropriate subscription page with RedirectionSource.UNKNOWN.
     */
    public static void navigateToSubscriptionPage(Context context) {
        Log.w("SubNavHelper", "navigateToSubscriptionPage called without RedirectionSource! Defaulting to UNKNOWN.");
        navigateToSubscriptionPage(context, RedirectionSource.UNKNOWN);
    }

    /**
     * Navigate to the subscription page with a specific RedirectionSource or a custom String source.
     *
     * @param redirectionSource the enum or string representing the navigation source
     */
    public static void navigateToSubscriptionPage(Context context, Object redirectionSource) {
        Log.d("SubNavHelper", "navigateToSubscriptionPage() called | source: " + (redirectionSource != null ? redirectionSource.toString() : "null") + " | context: " + context.getClass().getSimpleName());
        if (MyApplication.isCompetitiveProfile()) {
            navigateToCampaignSubscription(context, null, redirectionSource);
        } else {
            navigateToBoardSubscription(context, null, redirectionSource);
        }
    }

    public static void navigateToSubscriptionPage(Context context, RedirectionSource redirectionSource) {
        navigateToSubscriptionPage(context, (Object) redirectionSource);
    }

    public static void navigateToSubscriptionPageFromWebView(Context context, String source) {
        navigateToSubscriptionPageFromWebView(context, null, source);
    }

    public static void navigateToSubscriptionPageFromWebView(Context context, String data, String source) {
        String finalSource = (StringUtils.isNotEmpty(source) ? source : "WEBVIEW") + "_webview";
        navigateToSubscriptionPageWithData(context, data, (Object) finalSource);
    }

    public static Intent getSubscriptionPageIntent(Context context) {
        Log.w("SubNavHelper", "getSubscriptionPageIntent called without RedirectionSource! Defaulting to UNKNOWN.");
        return getSubscriptionPageIntentWithSource(context, RedirectionSource.UNKNOWN);
    }

    public static Intent getSubscriptionPageIntentWithSource(Context context, Object redirectionSource) {
        Intent intent = MyApplication.isCompetitiveProfile()
                ? getCampaignSubscriptionIntent(context)
                : getBoardSubscriptionIntent(context);

        if (redirectionSource instanceof RedirectionSource) {
            intent.putExtra(EXTRA_SOURCE, (RedirectionSource) redirectionSource);
        } else if (redirectionSource instanceof String) {
            intent.putExtra(EXTRA_SOURCE, (String) redirectionSource);
        }

        return intent;
    }

    public static Intent getSubscriptionPageIntent(Context context, RedirectionSource redirectionSource) {
        return getSubscriptionPageIntentWithSource(context, (Object) redirectionSource);
    }

    public static Intent getSubscriptionPageIntent(Context context, String screenName) {
        Log.w("SubNavHelper", "getSubscriptionPageIntent (legacy) called for screen [" + screenName + "]! Use version with RedirectionSource if possible.");
        Intent intent = getSubscriptionPageIntentWithSource(context, RedirectionSource.UNKNOWN);
        intent.putExtra("navigatedFromScreen", screenName);
        return intent;
    }

    public static Intent getSubscriptionPageIntent(Context context, String screenName, RedirectionSource redirectionSource) {
        // Keeping for compatibility
        Intent intent = getSubscriptionPageIntentWithSource(context, (Object) redirectionSource);
        intent.putExtra("navigatedFromScreen", screenName);
        return intent;
    }

    /**
     * Returns the appropriate subscription Activity class based on the user's profile type.
     */
    public static Class<?> getSubscriptionPageClass() {
        return MyApplication.isCompetitiveProfile()
                ? CampaignSubscriptionActivity.class
                : getBoardSubscriptionClass();
    }

    private static Class<?> getBoardSubscriptionClass() {
        if (SubscriptionType.isBasicSubscription()) {
            return SubscriptionPaymentScreen.class;
        } else {
            return ProPlanSubscriptionActivity.class;
        }
    }

    public static void navigateToSubscriptionPageWithData(Context context, String data) {
        navigateToSubscriptionPageWithData(context, data, RedirectionSource.UNKNOWN);
    }

    public static void navigateToSubscriptionPageWithData(Context context, String data, Object redirectionSource) {
        if (MyApplication.isCompetitiveProfile()) {
            navigateToCampaignSubscription(context, data, redirectionSource);
        } else {
            navigateToBoardSubscription(context, data, redirectionSource);
        }
    }

    public static void navigateToSubscriptionPageWithData(Context context, String data, RedirectionSource redirectionSource) {
        navigateToSubscriptionPageWithData(context, data, (Object) redirectionSource);
    }

    private static void navigateToCampaignSubscription(Context context, String jsonStr, Object redirectionSource) {
        if (!MyApplication.isCompetitiveProfile()) {
            navigateToBoardSubscription(context, jsonStr, redirectionSource);
            return;
        }
        Log.d("SubNavHelper", "navigateToCampaignSubscription() | source: " + (redirectionSource != null ? redirectionSource.toString() : "null") + " | jsonStr: " + jsonStr);
        Intent intent = getCampaignSubscriptionIntent(context);
        if (redirectionSource instanceof RedirectionSource) {
            intent.putExtra(EXTRA_SOURCE, (RedirectionSource) redirectionSource);
        } else if (redirectionSource instanceof String) {
            intent.putExtra(EXTRA_SOURCE, (String) redirectionSource);
        }
        if(StringUtils.isNotEmpty(jsonStr)) {
            try {
                Gson gson = new Gson();
                SubscriptionLockPopupDto subscriptionLockPopupDto = gson.fromJson(jsonStr, SubscriptionLockPopupDto.class);
                intent.putExtra(CampaignSubscriptionActivity.BUNDLE_SUBSCRIPTION_DATA, subscriptionLockPopupDto);
            } catch (Exception ignored) {
            }
        }
        context.startActivity(intent);
    }

    private static void navigateToBoardSubscription(Context context, String jsonStr, Object redirectionSource) {
        if (MyApplication.isCompetitiveProfile()) {
            navigateToCampaignSubscription(context, jsonStr, redirectionSource);
            return;
        }
        Log.d("SubNavHelper", "navigateToBoardSubscription() | source: " + (redirectionSource != null ? redirectionSource.toString() : "null") + " | jsonStr: " + jsonStr);

        Intent intent = getBoardSubscriptionIntent(context);
        if (redirectionSource instanceof RedirectionSource) {
            intent.putExtra(EXTRA_SOURCE, (RedirectionSource) redirectionSource);
        } else if (redirectionSource instanceof String) {
            intent.putExtra(EXTRA_SOURCE, (String) redirectionSource);
        }
        if(StringUtils.isNotEmpty(jsonStr)) {
            try {
                Gson gson = new Gson();
                SubscriptionLockPopupDto subscriptionLockPopupDto = gson.fromJson(jsonStr, SubscriptionLockPopupDto.class);
                intent.putExtra(CampaignSubscriptionActivity.BUNDLE_SUBSCRIPTION_DATA, subscriptionLockPopupDto);
            } catch (Exception ignored) {
            }
        }
        context.startActivity(intent);
    }

    private static Intent getCampaignSubscriptionIntent(Context context) {
        return new Intent(context, CampaignSubscriptionActivity.class);
    }

    private static Intent getBoardSubscriptionIntent(Context context) {
        if(SubscriptionType.isBasicSubscription()) {
            return new Intent(context, SubscriptionPaymentScreen.class);
        } else {
            return new Intent(context, ProPlanSubscriptionActivity.class);
        }
    }

    /**
     * Navigates to the subscription screen with a localised limit-exceeded message.
     */
    public static void navigateToSubscriptionScreenWithLimitExceed(Context context, FeatureEnum featureEnum, int maxLimit) {
        try {
            String json = MyApplication.getFirebaseRemoteConfig()
                    .getString("feature_limit_exceed_config");

            String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
            if (StringUtils.isEmpty(lang)) lang = "en";

            String message = null;
            String btnText = null;

            if (StringUtils.isNotEmpty(json)) {
                JSONObject root = new JSONObject(json);
                JSONObject featureObj = root.optJSONObject(featureEnum.name());
                if (featureObj != null) {
                    JSONObject langObj = featureObj.optJSONObject(lang);
                    if (langObj == null) langObj = featureObj.optJSONObject("en");
                    if (langObj != null) {
                        message = langObj.optString("message", null);
                        btnText = langObj.optString("btnText", null);
                    }
                }
            }

            if (StringUtils.isNotEmpty(message)) {
                message = message.replace("{maxLimit}", String.valueOf(maxLimit));
            }

            SubscriptionLockPopupDto dto = new SubscriptionLockPopupDto();
            dto.setSubTitle(message);
            dto.setBtnName(btnText);

            Intent intent = getSubscriptionPageIntent(context, RedirectionSource.LIMIT_EXCEED);
            intent.putExtra(CampaignSubscriptionActivity.BUNDLE_SUBSCRIPTION_DATA, dto);
            context.startActivity(intent);

        } catch (Exception e) {
            Log.e("SubNavHelper", "navigateToSubscriptionScreenWithLimitExceed failed", e);
            navigateToSubscriptionPage(context, RedirectionSource.LIMIT_EXCEED);
        }
    }

    public static boolean hasActiveSubscriptionByBatch() {
        return SubscriptionType.hasActiveSubscription();
    }

    public static boolean isPdfPencilVisibleByBatch() {
        SubscriptionType type = SubscriptionType.getSubscriptionType();
        if (type != SubscriptionType.PRO && type != SubscriptionType.PLUS) {
            return false;
        }

        String batchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
        if (StringUtils.isEmpty(batchCode)) {
            return false;
        }

        String config = MyApplication.getFirebaseRemoteConfig().getString("pdf_pencil_batch_config_v2");
        if (StringUtils.isEmpty(config)) {
            return false;
        }

        try {
            JSONArray unallowedBatches = new JSONArray(config);
            for (int i = 0; i < unallowedBatches.length(); i++) {
                if (batchCode.equalsIgnoreCase(unallowedBatches.optString(i))) {
                    return false;
                }
            }
        } catch (Exception e) {
            Log.e("SubNavHelper", "Error parsing pdf_pencil_batch_config_v2", e);
        }

        return true;
    }

    public static boolean hideProPlanSubscription() {
        String batchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
        String config = MyApplication.getFirebaseRemoteConfig()
                .getString("hide_pro_plan_configuration");

        if (SubscriptionType.isProSubscription()) return false;

        List<String> hiddenBatches = Arrays.asList(config.split(":"));
        return hiddenBatches.contains(batchCode);
    }
}
