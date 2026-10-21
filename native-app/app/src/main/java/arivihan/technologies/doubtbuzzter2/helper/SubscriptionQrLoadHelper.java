package arivihan.technologies.doubtbuzzter2.helper;

import static arivihan.technologies.doubtbuzzter2.activity.SubscriptionPaymentScreen.getAlphaNumericString;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.PAYMENT_INFO_RAZORPAY;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.RAZORPAY_CREATE_QR_CODE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.V2_GET_SUBSCRIPTION;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.USER_SUBSCRIPTION;

import androidx.annotation.Nullable;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;  // ← NEW IMPORT
import java.util.HashMap;
import java.util.List;       // ← NEW IMPORT
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.model.CouponDto;
import arivihan.technologies.doubtbuzzter2.model.PaymentDetails;
import arivihan.technologies.doubtbuzzter2.model.PaymentInfo;
import arivihan.technologies.doubtbuzzter2.model.RazorpayQRCreateResponse;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionModel;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionPreModel;
import arivihan.technologies.doubtbuzzter2.model.TransactionType;
import arivihan.technologies.doubtbuzzter2.model.UserSubscriptionDto;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;


public class SubscriptionQrLoadHelper {
    public static final String TAG = "SubscriptionQrLoadHelper";
    Context context;

    private static final int ORDER_ID_LENGTH = 8;

    private final Gson gson = new Gson();

    private String planId;
    private String generatedOrderId;
    private int resolvedAmount;

    private UserSubscriptionDto userSubscriptionDto;
    private RazorpayQRCreateResponse razorpayQRCreateResponse;
    private SubscriptionModel subscriptionModel;

    // ─────────────────────────────────────────────────────────────────────────
    // ← NEW: Static flag + listener list to prevent race condition
    // ─────────────────────────────────────────────────────────────────────────
    private static volatile boolean isLoadInProgress = false;
    private static final List<Runnable> pendingListeners = new ArrayList<>();

    /** Returns true if QR is already cached and ready to display */
    public static boolean isQrCacheReady() {
        return MyApplication.getStringData(PreferenceKeys.QR_CODE_IMAGE.getKey()) != null;
    }

    /** Returns true if the QR load chain is currently running */
    public static boolean isLoading() {
        return isLoadInProgress;
    }

    /**
     * Register a callback to be invoked on the calling thread's looper
     * once the QR cache is populated (or fails). Always called on main thread.
     */
    public static void addReadyListener(Runnable listener) {
        synchronized (pendingListeners) {
            pendingListeners.add(listener);
        }
    }

    /** Called internally when chain finishes (success or failure) */
    private static void notifyListeners() {
        isLoadInProgress = false;
        List<Runnable> toRun;
        synchronized (pendingListeners) {
            toRun = new ArrayList<>(pendingListeners);
            pendingListeners.clear();
        }
        for (Runnable r : toRun) r.run();
    }
    // ─────────────────────────────────────────────────────────────────────────


    public SubscriptionQrLoadHelper(Context context) {
        this.context = context;
    }


    // ← CHANGED: guard against double-start
    public void initCache() {
        Log.d(TAG, "initCache loaded : ");
        if (isLoadInProgress) {
            Log.d(TAG, "initCache: load already in progress, skipping");
            return;
        }
        if (shouldRefreshQrCache()) {
            isLoadInProgress = true;   // ← NEW: set flag before starting chain
            fetchSubscriptionDetails();
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Step 1 — Fetch subscription details  (unchanged logic)
    // ─────────────────────────────────────────────────────────────────────────
    private void fetchSubscriptionDetails() {

        MyJsonObjectRequest req = new MyJsonObjectRequest(
                Request.Method.GET,
                CommonMethods.getUrlParam(V2_GET_SUBSCRIPTION, new HashMap<>()),
                null,
                response -> {
                    Log.d(TAG, "fetchSubscriptionDetails response: " + response);
                    subscriptionModel = gson.fromJson(
                            response.toString(),
                            new TypeToken<SubscriptionModel>() {}.getType());

                    if (subscriptionModel != null && subscriptionModel.getOverview() != null) {
                        planId = subscriptionModel.getOverview().getPlanId();
                        resolvedAmount = Integer.parseInt(subscriptionModel.getOverview().getFinalPrice());
                    }

                    generatedOrderId = getAlphaNumericString(ORDER_ID_LENGTH);
                    Log.i(TAG, "Generated orderId: " + generatedOrderId);

                    subscriptionPreCall(null);
                },
                error -> {
                    Log.e(TAG, "fetchSubscriptionDetails error: " + error.toString());
                    generatedOrderId = getAlphaNumericString(ORDER_ID_LENGTH);
                    subscriptionPreCall(null);
                });

        MyVolleySingleton.getInstance(context).addToRequestQueue(req);
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Step 2 — Create subscription entry in DB  (unchanged logic)
    // ─────────────────────────────────────────────────────────────────────────
    private void subscriptionPreCall(@Nullable Runnable onSuccessOverride) {
        Log.d(TAG, "step 2 - subscriptionPreCall");
        Log.d(TAG, "subscriptionPreCall — planId=" + planId
                + "  amount=" + resolvedAmount
                + "  orderId=" + generatedOrderId);

        SubscriptionPreModel preModel = new SubscriptionPreModel();
        preModel.setUserId(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
        preModel.setSubscriptionPlanId(planId);
        preModel.setOrderId(getAlphaNumericString(8));
        preModel.setTotalAmount((long) resolvedAmount);
        preModel.setRedirectionFrom(RedirectionSource.HOME.getFormattedValue());
        Log.d("SubscriptionSourceTrack", "Source: " + preModel.getRedirectionFrom() + " | Plan: " + planId + " (SubscriptionQrLoadHelper)");

        JSONObject body;
        try {
            body = new JSONObject(gson.toJson(preModel));
            Log.d(TAG, "subscriptionPreCall-body: " + body);
        } catch (JSONException e) {
            Log.e(TAG, "subscriptionPreCall JSON error — skipping to QR", e);
            runPostPreCall(onSuccessOverride);
            return;
        }

        MyJsonObjectRequest req = new MyJsonObjectRequest(
                Request.Method.POST,
                CommonMethods.getUrlParam(USER_SUBSCRIPTION, new HashMap<>()),
                body,
                response -> {
                    Log.d(TAG, "subscriptionPreCall response: " + response);
                    try {
                        userSubscriptionDto = gson.fromJson(
                                response.getJSONObject("data").toString(),
                                new TypeToken<UserSubscriptionDto>() {}.getType());

                        Log.i(TAG, "userSubscriptionDto ready — orderId: "
                                + (userSubscriptionDto != null
                                ? userSubscriptionDto.getOrderId() : "null"));
                        generatedOrderId = userSubscriptionDto.getOrderId();
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to parse userSubscriptionDto: " + e.getMessage(), e);
                    }
                    runPostPreCall(onSuccessOverride);
                },
                new VolleyGlobalErrorListener(context) {
                    @Override
                    public void postError(VolleyError error) {
                        Log.e(TAG, "subscriptionPreCall error: " + error.getMessage());
                        runPostPreCall(onSuccessOverride);
                    }
                });

        MyVolleySingleton.getInstance(context).addToRequestQueue(req);
    }


    private void runPostPreCall(@Nullable Runnable onSuccessOverride) {
        if (onSuccessOverride != null) {
            onSuccessOverride.run();
        } else if (MyApplication.getStringData(PreferenceKeys.QR_CODE_IMAGE.getKey()) == null) {
            fetchQRCode();
        } else {
            // ← NEW: cache was populated by another path while we were in step 2, just notify
            notifyListeners();
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Step 3 — QR Code creation
    // ─────────────────────────────────────────────────────────────────────────
    private void fetchQRCode() {
        Log.i(TAG, "Requesting QR — amount=" + resolvedAmount
                + "  planId=" + planId
                + "  orderId=" + generatedOrderId);

        Map<String, Object> params = new HashMap<>();
        params.put("orderId",            generatedOrderId);
        params.put("subscriptionPlanId", planId);
        params.put("paymentAmount",      resolvedAmount);
        params.put("transactionId",      "");

        MyJsonObjectRequest req = new MyJsonObjectRequest(
                Request.Method.GET,
                CommonMethods.getUrlParam(RAZORPAY_CREATE_QR_CODE, params),
                null,
                jsonObject -> {
                    razorpayQRCreateResponse = gson.fromJson(
                            jsonObject.toString(),
                            new TypeToken<RazorpayQRCreateResponse>() {}.getType());
                    Log.i(TAG, razorpayQRCreateResponse.toString());

                    initiatePaymentInDb(resolvedAmount, generatedOrderId);

                    if (MyApplication.getStringData(PreferenceKeys.QR_CODE_IMAGE.getKey()) == null) {
                        MyApplication.saveData(PreferenceKeys.QR_CODE_IMAGE.getKey(),
                                razorpayQRCreateResponse.getImage_url());
                        MyApplication.saveData(PreferenceKeys.QR_CODE_IMAGE_TIMESTAMP.getKey(),
                                String.valueOf(System.currentTimeMillis()));
                        MyApplication.saveData(PreferenceKeys.QR_CODE_ID.getKey(),
                                razorpayQRCreateResponse.getId());
                        MyApplication.saveData(PreferenceKeys.QR_CODE_ORDER_ID.getKey(),
                                generatedOrderId);
                        if (userSubscriptionDto != null)
                            MyApplication.saveData(
                                    PreferenceKeys.QR_CODE_SUBSCRIPTION_DTO.getKey(),
                                    gson.toJson(userSubscriptionDto));
                        loadQrCodeImage();
                    } else {
                        notifyListeners();
                    }
                },
                error -> {
                    Log.e(TAG, error.toString());
                    logPaymentFailure("RAZORPAY_CREATE_QR_CODE", error.toString());
                    notifyListeners();
                });

        MyVolleySingleton.getInstance(context).addToRequestQueue(req);
    }


    private void logPaymentFailure(String source, String reason) {
        Map<String, String> p = new HashMap<>();
        p.put("Subscription_plan_Id", planId);
        p.put("amount",               String.valueOf(resolvedAmount));
        p.put("action",               "PaymentError QR = " + source + " | " + reason);
        p.put("navigation_from",      "HomeScreen PreLoad");
        MyApplication.logFirebaseBundleCreation(
                FirebaseEvents.Payment_failed.getActionName(), p);
    }


    // ─────────────────────────────────────────────────────────────────────────
    // QR Image loading
    // ─────────────────────────────────────────────────────────────────────────
    private void loadQrCodeImage() {
        Log.d(TAG, "step 4 - loadQrCodeImage");
        String url = MyApplication.getStringData(PreferenceKeys.QR_CODE_IMAGE.getKey());
        Log.d(TAG, "step 4 - url: " + url);

        if (url == null) {
            notifyListeners();
            return;
        }

        GlideUrl glideUrl = new GlideUrl(url);
        Glide.with(context.getApplicationContext())
                .load(glideUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .preload();

        notifyListeners();
    }


    private void initiatePaymentInDb(long paymentAmount, String orderId) {
        String userId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
        Gson gson = new Gson();
        PaymentDetails paymentDetails = PaymentDetails
                .builder()
                .amount(paymentAmount * 100)
                .orderId(orderId)
                .type(TransactionType.SUBSCRIPTION.name())
                .paymentMode("RAZORPAY_QR")
                .qrCodeId(razorpayQRCreateResponse.getId())
                .userId(userId)
                .build();
        try {
            MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.POST,
                    PAYMENT_INFO_RAZORPAY, new JSONObject(gson.toJson(paymentDetails)),
                    response -> {
                        TypeToken<PaymentInfo> token = new TypeToken<>() {};
                        PaymentInfo paymentInfo = gson.fromJson(response.toString(), token.getType());
                        if (paymentInfo != null)
                            Log.d("apiresponse", paymentInfo.toString());
                        Log.i("ORDERIDRAZORPAY", "ORDERID :: " + paymentInfo.getOrderId());
                        Log.i("ORDERIDRAZORPAY", "ORDERID :: " + paymentAmount);
                    }, new VolleyGlobalErrorListener(context) {
                @Override
                public void postError(VolleyError error) {
                    // existing commented-out code stays as is
                }
            });
            MyVolleySingleton.getInstance(context).addToRequestQueue(myJsonObjectRequest2);
        } catch (JSONException e) {
            Toast.makeText(context, context.getString(R.string.encountered_some_error), Toast.LENGTH_SHORT).show();
        }
    }


    private boolean shouldRefreshQrCache() {
        Log.d(TAG, "shouldRefreshQrCache: checking if QR cache needs refresh");

        String timestampStr = MyApplication.getStringData(PreferenceKeys.QR_CODE_IMAGE_TIMESTAMP.getKey());
        long expiryTimestamp = MyApplication.getFirebaseRemoteConfig()
                .getLong("qr_code_expiry_ms");

        Log.d(TAG, "shouldRefreshQrCache: timestampStr=" + timestampStr + ", expiryTimestamp=" + expiryTimestamp + "ms");

        if (timestampStr == null) {
            Log.w(TAG, "shouldRefreshQrCache: no saved timestamp found, cache refresh required");
            isLoadInProgress = false;
            return true;
        }
        try {
            long savedAt = Long.parseLong(timestampStr);
            long now = System.currentTimeMillis();
            long age = now - savedAt;
            Log.d(TAG, "shouldRefreshQrCache: cache age=" + age + "ms, expiry=" + expiryTimestamp + "ms");

            if (age > expiryTimestamp) {
                Log.i(TAG, "shouldRefreshQrCache: cache expired (age=" + age + "ms > expiry=" + expiryTimestamp + "ms), clearing cached QR data");
                MyApplication.saveData(PreferenceKeys.QR_CODE_IMAGE.getKey(), null);
                MyApplication.saveData(PreferenceKeys.QR_CODE_IMAGE_TIMESTAMP.getKey(), null);
                MyApplication.saveData(PreferenceKeys.QR_CODE_ID.getKey(), null);
                MyApplication.saveData(PreferenceKeys.QR_CODE_ORDER_ID.getKey(), null);
                MyApplication.saveData(PreferenceKeys.QR_CODE_SUBSCRIPTION_DTO.getKey(), null);
                Log.d(TAG, "shouldRefreshQrCache: cached QR data cleared successfully");
                isLoadInProgress = false;
                return true;
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "shouldRefreshQrCache: failed to parse timestamp string='" + timestampStr + "', clearing corrupt cache", e);
            MyApplication.saveData(PreferenceKeys.QR_CODE_IMAGE.getKey(), null);
            MyApplication.saveData(PreferenceKeys.QR_CODE_IMAGE_TIMESTAMP.getKey(), null);
            MyApplication.saveData(PreferenceKeys.QR_CODE_ID.getKey(), null);
            MyApplication.saveData(PreferenceKeys.QR_CODE_ORDER_ID.getKey(), null);
            MyApplication.saveData(PreferenceKeys.QR_CODE_SUBSCRIPTION_DTO.getKey(), null);
            Log.d(TAG, "shouldRefreshQrCache: corrupt cached QR data cleared successfully");
            isLoadInProgress = false;
            return true;
        }

        Log.d(TAG, "shouldRefreshQrCache: cache is still valid, no refresh needed");
        return false;
    }
}