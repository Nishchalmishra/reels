package arivihan.technologies.doubtbuzzter2.helper.subscription;

import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public enum  SubscriptionType {

    /** No subscription — user is in the free / pre-51 cohort */
    BASIC,

    /** Paid subscription — post-51 board 2026 plan */
    PLUS,

    /** Pro subscription — board 2026 plan */
    PRO;

    // ── Static lookup ────────────────────────────────────────────────────────

    /**
     * Reads {@code user_subscription_type} from SharedPreferences and returns
     * the matching {@link SubscriptionType}.
     * Falls back to {@link #BASIC} for unknown / empty values.
     */
    public static SubscriptionType getSubscriptionType() {
        String stored = MyApplication.getStringData(PreferenceKeys.USER_SUBSCRIPTION_TYPE.getKey());
        Log.d("Subscription Type", "--> stored subscription type: "+stored);
        if (StringUtils.isEmpty(stored)) {
            return BASIC;
        }
        try {
            return valueOf(stored);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return BASIC;
        }
    }

    // ── Convenience boolean checks ───────────────────────────────────────────

    /** Returns {@code true} when the user has no subscription (free cohort). */
    public static boolean isBasicSubscription() {
        return getSubscriptionType() == BASIC;
    }

    /** Returns {@code true} when the user holds the Plus plan. */
    public static boolean isPlusSubscription() {
        return getSubscriptionType() == PLUS;
    }

    /** Returns {@code true} when the user holds the Pro plan. */
    public static boolean isProSubscription() {
        return getSubscriptionType() == PRO;
    }

    /** Returns {@code true} for any paid subscription (i.e. not the free cohort). */
    public static boolean hasActiveSubscription() {
        return getSubscriptionType() != BASIC;
    }
}
