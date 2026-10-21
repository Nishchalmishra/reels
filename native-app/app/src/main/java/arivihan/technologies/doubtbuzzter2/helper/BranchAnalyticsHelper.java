package arivihan.technologies.doubtbuzzter2.helper;

import android.content.Context;
import android.util.Log;

import io.branch.referral.Branch;
import io.branch.referral.util.BRANCH_STANDARD_EVENT;
import io.branch.referral.util.BranchEvent;
import io.branch.referral.util.CurrencyType;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

/**
 * Central helper for all Branch.io event tracking related to payments/subscriptions.
 * All methods are static and fire-and-forget.
 */
public class BranchAnalyticsHelper {

    private static final String TAG = "BranchAnalyticsHelper";
    private static final String CUSTOM_EVENT_PAYMENT_FAILED = "payment_failed";

    /**
     * Attach the Arivihan userId as the Branch identity so all events and
     * attribution persist across devices and uninstall/reinstall cycles.
     * Identity persists across sessions; calling again with the same id is a no-op.
     */
    public static void setUserIdentity(String userId) {
        try {
            Branch branch = Branch.getInstance();
            if (branch == null || userId == null || userId.isEmpty()) return;
            branch.setIdentity(userId);
        } catch (Exception e) {
            Log.e(TAG, "Failed to set Branch identity", e);
        }
    }

    /**
     * Re-apply the persisted identity for an already-logged-in user. Covers users
     * who logged in before setIdentity was introduced, or reinstalled the app.
     */
    public static void setUserIdentityFromPrefs() {
        setUserIdentity(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
    }

    /** Detach the Branch identity so the next user's events are not attributed to this one. */
    public static void logoutUser() {
        try {
            Branch branch = Branch.getInstance();
            if (branch == null) return;
            branch.logout();
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear Branch identity on logout", e);
        }
    }

    /** Fire once a brand-new user's registration (onboarding form + account creation) has completed. */
    public static void logCompleteRegistration(Context context, String userId, String phoneNumber, String batchCode) {
        try {
            setUserIdentity(userId);
            new BranchEvent(BRANCH_STANDARD_EVENT.COMPLETE_REGISTRATION)
                    .setCustomerEventAlias("COMPLETE_REGISTRATION")
                    .setDescription("User completed registration")
                    .addCustomDataProperty("userId", userId != null ? userId : "")
                    .addCustomDataProperty("batchCode", batchCode != null ? batchCode : "")
                    .logEvent(context);
        } catch (Exception e) {
            Log.e(TAG, "Failed to log COMPLETE_REGISTRATION event", e);
        }
    }

    /** Fire once the backend has confirmed the subscription purchase. */
    public static void logPurchase(Context context, String planId, String transactionId, double amount, String modeOfPayment) {
        try {
            new BranchEvent(BRANCH_STANDARD_EVENT.PURCHASE)
                    .setCustomerEventAlias("PURCHASE")
                    .setCurrency(CurrencyType.INR)
                    .setRevenue(amount)
                    .setTransactionID(transactionId)
                    .setDescription("Subscription purchase successful")
                    .addCustomDataProperty("Subscription_plan_Id", planId)
                    .addCustomDataProperty("modeOfPayment", modeOfPayment)
                    .addCustomDataProperty("userId", currentUserId())
                    .addCustomDataProperty("batchCode", currentBatchCode())
                    .logEvent(context);
        } catch (Exception e) {
            Log.e(TAG, "Failed to log PURCHASE event", e);
        }
    }

    /** Fire when a payment attempt fails at any stage (SDK error, server rejection, app killed mid-payment). */
    public static void logPaymentFailed(Context context, String planId, double amount, String modeOfPayment, String reason) {
        try {
            new BranchEvent(CUSTOM_EVENT_PAYMENT_FAILED)
                    .setCustomerEventAlias(CUSTOM_EVENT_PAYMENT_FAILED)
                    .setCurrency(CurrencyType.INR)
                    .setRevenue(amount)
                    .setDescription(reason)
                    .addCustomDataProperty("Subscription_plan_Id", planId)
                    .addCustomDataProperty("modeOfPayment", modeOfPayment)
                    .addCustomDataProperty("userId", currentUserId())
                    .addCustomDataProperty("batchCode", currentBatchCode())
                    .logEvent(context);
        } catch (Exception e) {
            Log.e(TAG, "Failed to log payment_failed event", e);
        }
    }

    // Read fresh at fire time rather than threaded in from each call site
    // (Razorpay SDK checkout, QR code, and the QR-poll payment screen) — they'd
    // all just be reading the same two session prefs anyway.
    private static String currentUserId() {
        String userId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
        return userId != null ? userId : "";
    }

    private static String currentBatchCode() {
        String batchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
        return batchCode != null ? batchCode : "";
    }
}
