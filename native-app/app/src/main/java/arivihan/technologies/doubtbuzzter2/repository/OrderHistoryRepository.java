package arivihan.technologies.doubtbuzzter2.repository;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.google.gson.Gson;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.constants.UrlRoutes;
import arivihan.technologies.doubtbuzzter2.helper.EnvironmentManager;
import arivihan.technologies.doubtbuzzter2.model.order_history.OrderHistoryEntry;
import arivihan.technologies.doubtbuzzter2.model.order_history.OrderHistoryResponse;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;

public class OrderHistoryRepository {

    private static final String TAG = OrderHistoryRepository.class.getSimpleName();
    private final Application application;
    private final Gson gson = new Gson();

    public interface Callback {
        void onSuccess(@NonNull List<OrderHistoryEntry> orders);
        void onError(@NonNull String message);
    }

    public OrderHistoryRepository(Application application) {
        this.application = application;
    }

    public void fetchOrders(Callback callback) {
        MyJsonObjectRequest request = new MyJsonObjectRequest(
                Request.Method.GET,
                UrlRoutes.ORDER_HISTORY,
                null,
                response -> {
                    try {
                        OrderHistoryResponse parsed = gson.fromJson(response.toString(), OrderHistoryResponse.class);
                        if (parsed != null && parsed.getData() != null) {
                            callback.onSuccess(parsed.getData());
                        } else {
                            callback.onSuccess(java.util.Collections.emptyList());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "fetchOrders parse error", e);
                        callback.onError("Failed to load orders");
                    }
                },
                error -> {
                    Log.w(TAG, "fetchOrders failed", error);
                    callback.onError("Failed to load orders");
                }
        );
        MyVolleySingleton.getInstance(application).addToRequestQueue(request);
    }
}
