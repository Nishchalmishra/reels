package arivihan.technologies.doubtbuzzter2.repository;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;


import java.util.List;

import arivihan.technologies.doubtbuzzter2.constants.UrlRoutes;
import arivihan.technologies.doubtbuzzter2.helper.EnvironmentManager;
import arivihan.technologies.doubtbuzzter2.model.book_order.CartApiResponse;
import arivihan.technologies.doubtbuzzter2.model.book_order.CartData;
import arivihan.technologies.doubtbuzzter2.model.book_order.CartItemRequest;
import arivihan.technologies.doubtbuzzter2.model.book_order.CatalogApiResponse;
import arivihan.technologies.doubtbuzzter2.model.book_order.CatalogData;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;

public class BookOrderRepository {

    private static final String TAG = BookOrderRepository.class.getSimpleName();
    private final Application application;
    private final Gson gson = new Gson();

    public interface CartCallback {
        void onSuccess(@NonNull CartData data);
        void onError(@NonNull String message);
    }

    public interface CatalogCallback {
        void onSuccess(@NonNull CatalogData data);
        void onError(@NonNull String message);
    }

    public BookOrderRepository(Application application) {
        this.application = application;
    }

    public void fetchCart(CartCallback callback) {
        MyJsonObjectRequest request = new MyJsonObjectRequest(
                Request.Method.GET,
                UrlRoutes.BOOK_ORDER_CART,
                null,
                response -> {
                    try {
                        CartApiResponse parsed = gson.fromJson(response.toString(), CartApiResponse.class);
                        if (parsed != null && parsed.getData() != null) {
                            callback.onSuccess(parsed.getData());
                        } else {
                            callback.onError("Empty cart response");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "fetchCart parse error", e);
                        callback.onError("Failed to load cart");
                    }
                },
                error -> {
                    Log.w(TAG, "fetchCart failed", error);
                    callback.onError("Failed to load cart");
                }
        );
        MyVolleySingleton.getInstance(application).addToRequestQueue(request);
    }




    public void addItem(
            @NonNull CartItemRequest item,
            CartCallback callback
    ) {
        try {
            JSONObject body = new JSONObject();
            body.put("subjectCode", item.getSubjectCode());
            body.put("qty", item.getQty());
            if (item.getFree()) body.put("free", true);

            android.util.Log.d("BookOrderRepository", "onBody: $subjectCode" + body);


            MyJsonObjectRequest request = new MyJsonObjectRequest(
                    Request.Method.POST,
                    UrlRoutes.BOOK_ORDER_CART_ITEMS,
                    body,
                    response -> {
                        try {
                            CartApiResponse parsed = gson.fromJson(response.toString(), CartApiResponse.class);
                            if (parsed != null && parsed.getData() != null) {
                                callback.onSuccess(parsed.getData());
                            } else {
                                callback.onError("Empty response");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "addItem parse error", e);
                            callback.onError("Failed to add book");
                        }
                    },
                    error -> {
                        Log.w(TAG, "addItem failed", error);
                        callback.onError("Failed to add book");
                    }
            );
            MyVolleySingleton.getInstance(application).addToRequestQueue(request);
        } catch (Exception e) {
            Log.e(TAG, "addItem error", e);
            callback.onError(e.getMessage() != null ? e.getMessage() : "Unknown error");
        }
    }



    public void updateCartItems(
            @NonNull List<CartItemRequest> items,
            CartCallback callback
    ) {
        try {
            JSONArray itemsArray = new JSONArray();
            for (CartItemRequest item : items) {
                JSONObject itemObj = new JSONObject();
                itemObj.put("subjectCode", item.getSubjectCode());
                itemObj.put("qty", item.getQty());
                if (item.getFree()) itemObj.put("free", true);
                itemsArray.put(itemObj);
            }

            JSONObject body = new JSONObject();
            body.put("items", itemsArray);

            MyJsonObjectRequest request = new MyJsonObjectRequest(
                    Request.Method.PUT,
                    UrlRoutes.BOOK_ORDER_CART_ITEMS,
                    body,
                    response -> {
                        try {
                            CartApiResponse parsed = gson.fromJson(response.toString(), CartApiResponse.class);
                            if (parsed != null && parsed.getData() != null) {
                                callback.onSuccess(parsed.getData());
                            } else {
                                callback.onError("Empty response");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "updateCartItems parse error", e);
                            callback.onError("Failed to update cart");
                        }
                    },
                    error -> {
                        Log.w(TAG, "updateCartItems failed", error);
                        callback.onError("Failed to update cart");
                    }
            );
            MyVolleySingleton.getInstance(application).addToRequestQueue(request);
        } catch (Exception e) {
            Log.e(TAG, "updateCartItems error", e);
            callback.onError(e.getMessage() != null ? e.getMessage() : "Unknown error");
        }
    }




    public void deleteItem(
            @NonNull CartItemRequest item,
            CartCallback callback
    ) {
        try {
            JSONArray itemsArray = new JSONArray();
            JSONObject itemObj = new JSONObject();
            itemObj.put("subjectCode", item.getSubjectCode());
            itemObj.put("qty", item.getQty());
            if (item.getFree()) itemObj.put("free", true);
            itemsArray.put(itemObj);

            JSONObject body = new JSONObject();
            body.put("items", itemsArray);

            MyJsonObjectRequest request = new MyJsonObjectRequest(
                    Request.Method.DELETE,
                    UrlRoutes.BOOK_ORDER_CART_ITEMS,
                    body,
                    response -> {
                        try {
                            CartApiResponse parsed = gson.fromJson(response.toString(), CartApiResponse.class);
                            if (parsed != null && parsed.getData() != null) {
                                callback.onSuccess(parsed.getData());
                            } else {
                                callback.onError("Empty response");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "deleteItem parse error", e);
                            callback.onError("Failed to remove book");
                        }
                    },
                    error -> {
                        Log.w(TAG, "deleteItem failed", error);
                        callback.onError("Failed to remove book");
                    }
            );
            MyVolleySingleton.getInstance(application).addToRequestQueue(request);
        } catch (Exception e) {
            Log.e(TAG, "deleteItem error", e);
            callback.onError(e.getMessage() != null ? e.getMessage() : "Unknown error");
        }
    }


    public void fetchCatalog(CatalogCallback callback) {
        MyJsonObjectRequest request = new MyJsonObjectRequest(
                Request.Method.GET,
                UrlRoutes.BOOK_ORDER_CATALOG,
                null,
                response -> {
                    try {
                        CatalogApiResponse parsed = gson.fromJson(response.toString(), CatalogApiResponse.class);
                        if (parsed != null && parsed.getData() != null) {
                            callback.onSuccess(parsed.getData());
                        } else {
                            callback.onError("Empty catalog response");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "fetchCatalog parse error", e);
                        callback.onError("Failed to load catalog");
                    }
                },
                error -> {
                    Log.w(TAG, "fetchCatalog failed", error);
                    callback.onError("Failed to load catalog");
                }
        );
        MyVolleySingleton.getInstance(application).addToRequestQueue(request);
    }
}
