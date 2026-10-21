package arivihan.technologies.doubtbuzzter2.repository;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.constants.UrlRoutes;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.helper.EnvironmentManager;
import arivihan.technologies.doubtbuzzter2.model.order_success.OrderConfirmResponse;
import arivihan.technologies.doubtbuzzter2.model.order_success.OrderView;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;

public class OrderConfirmRepository {

    private static final String TAG = OrderConfirmRepository.class.getSimpleName();
    private final Application application;
    private final Gson gson = new Gson();

    public static class CheckoutData {
        public final String orderId;
        public final int payable;
        public final boolean paymentRequired;
        @Nullable public final String gatewayOrderId;
        @Nullable public final String providerKey;
        @Nullable public final String currency;

        public CheckoutData(String orderId, int payable, boolean paymentRequired,
                            String gatewayOrderId, String providerKey, String currency) {
            this.orderId = orderId;
            this.payable = payable;
            this.paymentRequired = paymentRequired;
            this.gatewayOrderId = gatewayOrderId;
            this.providerKey = providerKey;
            this.currency = currency;
        }
    }

    public interface CheckoutCallback {
        void onSuccess(@NonNull CheckoutData data);
        void onError(@NonNull String message);
    }

    public interface Callback {
        void onSuccess(@NonNull OrderView order);
        void onError(@NonNull String message);
    }

    public OrderConfirmRepository(Application application) {
        this.application = application;
    }

    public void getOrderById(@NonNull String orderId, Callback callback) {
        try {
            String path = UrlRoutes.ORDER_HISTORY + "/" + orderId;
            MyJsonObjectRequest request = new MyJsonObjectRequest(
                    Request.Method.GET,
                    path,
                    null,
                    response -> {
                        try {
                            OrderConfirmResponse parsed = gson.fromJson(
                                    response.toString(), OrderConfirmResponse.class);
                            if (parsed != null && parsed.getData() != null) {
                                callback.onSuccess(parsed.getData());
                            } else {
                                callback.onError("Empty response");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "getOrderById parse error", e);
                            callback.onError("Failed to fetch order");
                        }
                    },
                    error -> {
                        Log.w(TAG, "getOrderById failed", error);
                        callback.onError("Failed to fetch order");
                    }
            );
            MyVolleySingleton.getInstance(application).addToRequestQueue(request);
        } catch (Exception e) {
            Log.e(TAG, "getOrderById error", e);
            callback.onError(e.getMessage() != null ? e.getMessage() : "Unknown error");
        }
    }

    public void initiateCheckout(
            @Nullable String addressId,
            @NonNull List<String> freeSubjectCodes,
            CheckoutCallback callback
    ) {
        try {
            String userId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
            String idempotencyKey = userId + "_" + System.currentTimeMillis();

            JSONArray codesArray = new JSONArray();
            for (String code : freeSubjectCodes) {
                codesArray.put(code);
            }

            JSONObject body = new JSONObject();
            body.put("addressId", addressId != null ? addressId : JSONObject.NULL);
            body.put("idempotencyKey", idempotencyKey);

            MyJsonObjectRequest request = new MyJsonObjectRequest(
                    Request.Method.POST,
                    UrlRoutes.BOOK_ORDER_CHECKOUT,
                    body,
                    response -> {
                        try {
                            JSONObject data = response.getJSONObject("data");
                            CheckoutData result = new CheckoutData(
                                    data.optString("orderId"),
                                    data.optInt("payable"),
                                    data.optBoolean("paymentRequired"),
                                    data.optString("gatewayOrderId", null),
                                    data.optString("providerKey", null),
                                    data.optString("currency", null)
                            );
                            callback.onSuccess(result);
                        } catch (Exception e) {
                            Log.e(TAG, "initiateCheckout parse error", e);
                            callback.onError("Failed to initiate checkout");
                        }
                    },
                    error -> {
                        Log.w(TAG, "initiateCheckout failed", error);
                        callback.onError("Failed to initiate checkout");
                    }
            );
            MyVolleySingleton.getInstance(application).addToRequestQueue(request);
        } catch (Exception e) {
            Log.e(TAG, "initiateCheckout error", e);
            callback.onError(e.getMessage() != null ? e.getMessage() : "Unknown error");
        }
    }

    public void redeemOrder(@NonNull List<String> subjectCodes, @Nullable String addressId, Callback callback) {
        try {
            String userId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
            String idempotencyKey = "free-order-" + userId + "_" + System.currentTimeMillis();

            JSONArray codesArray = new JSONArray();
            for (String code : subjectCodes) {
                codesArray.put(code);
            }

            JSONObject body = new JSONObject();
            body.put("subjectCodes", codesArray);
            body.put("addressId", addressId != null ? addressId : JSONObject.NULL);
            body.put("idempotencyKey", idempotencyKey);

            MyJsonObjectRequest request = new MyJsonObjectRequest(
                    Request.Method.POST,
                    UrlRoutes.REDEEM_ORDER,
                    body,
                    response -> {
                        try {
                            OrderConfirmResponse parsed = gson.fromJson(
                                    response.toString(), OrderConfirmResponse.class);
                            if (parsed != null && parsed.getData() != null) {
                                callback.onSuccess(parsed.getData());
                            } else {
                                callback.onError("Empty response");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "redeemOrder parse error", e);
                            callback.onError("Failed to place order");
                        }
                    },
                    error -> {
                        Log.w(TAG, "redeemOrder failed", error);
                        callback.onError("Failed to place order");
                    }
            );
            MyVolleySingleton.getInstance(application).addToRequestQueue(request);
        } catch (Exception e) {
            Log.e(TAG, "redeemOrder error", e);
            callback.onError(e.getMessage() != null ? e.getMessage() : "Unknown error");
        }
    }

    public void confirmOrder(@NonNull String orderId, @NonNull String paymentId,
                             @NonNull String signature, Callback callback) {
        try {
            JSONObject body = new JSONObject();
            body.put("orderId", orderId);
            body.put("paymentId", paymentId);
            body.put("signature", signature);

            MyJsonObjectRequest request = new MyJsonObjectRequest(
                    Request.Method.POST,
                    UrlRoutes.ORDER_CONFIRM,
                    body,
                    response -> {
                        try {
                            OrderConfirmResponse parsed = gson.fromJson(
                                    response.toString(), OrderConfirmResponse.class);
                            if (parsed != null && parsed.getData() != null) {
                                callback.onSuccess(parsed.getData());
                            } else {
                                callback.onError("Empty response");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "confirmOrder parse error", e);
                            callback.onError("Failed to confirm order");
                        }
                    },
                    error -> {
                        Log.w(TAG, "confirmOrder failed", error);
                        callback.onError("Failed to confirm order");
                    }
            );
            MyVolleySingleton.getInstance(application).addToRequestQueue(request);
        } catch (Exception e) {
            Log.e(TAG, "confirmOrder error", e);
            callback.onError(e.getMessage() != null ? e.getMessage() : "Unknown error");
        }
    }
}
