package arivihan.technologies.doubtbuzzter2.helper;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import arivihan.technologies.doubtbuzzter2.activity.CampaignSubscriptionActivity;
import arivihan.technologies.doubtbuzzter2.activity.ProPlanSubscriptionActivity;
import arivihan.technologies.doubtbuzzter2.activity.SubscriptionPaymentScreen;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class ActivityLauncher {

    /**
     * Central navigation resolver — every Class.forName() in the app goes through here.
     * To redirect a screen app-wide, add a condition here instead of touching each call site.
     */
    public static Class<?> resolveClass(String className) throws ClassNotFoundException {
        if (CampaignSubscriptionActivity.class.getName().equals(className)
                || SubscriptionPaymentScreen.class.getName().equals(className)
                || ProPlanSubscriptionActivity.class.getName().equals(className)) {
            return SubscriptionNavigationHelper.getSubscriptionPageClass();
        }
        return Class.forName(className);
    }

    public static void launchActivity(Context context, String className, Map<String, Object> activityParams) {
        try {
            Class<?> targetClass = ActivityLauncher.resolveClass(className);
            Intent intent = new Intent(context, targetClass);

            RedirectionSource source = RedirectionSource.FLASH_CARD;
            if (activityParams != null && activityParams.containsKey("source")) {
                Object src = activityParams.get("source");
                if (src instanceof String) {
                    try {
                        source = RedirectionSource.valueOf((String) src);
                    } catch (Exception ignored) {
                    }
                } else if (src instanceof RedirectionSource) {
                    source = (RedirectionSource) src;
                }
            }
            intent.putExtra(SubscriptionNavigationHelper.EXTRA_SOURCE, source);

            if (activityParams != null) {
                for (Map.Entry<String, Object> entry : activityParams.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();

                    if (value instanceof String) {
                        intent.putExtra(key, (String) value);
                    } else if (value instanceof Integer) {
                        intent.putExtra(key, (Integer) value);
                    } else if (value instanceof Boolean) {
                        intent.putExtra(key, (Boolean) value);
                    } else if (value instanceof Serializable) {
                        intent.putExtra(key, (Serializable) value);
                    }
                }
            }

            context.startActivity(intent);
        } catch (ClassNotFoundException e) {
            Log.e("ClassNotFound", "Activity class not found", e);
        }
    }


    public static Map<String, Object> convertJsonBackToActivityParams(Map<String, Object> params) {
        return params != null ? params : new HashMap<>();
    }

    public static Map<String, Object> convertJsonBackToActivityParams(String jsonString) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            if (StringUtils.isNotEmpty(jsonString)) {
                return objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {
                });
            } else {
                return new HashMap<>();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error with converting json back to params (map)" + e);
        }
    }

}
