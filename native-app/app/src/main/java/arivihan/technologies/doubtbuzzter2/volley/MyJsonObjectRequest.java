package arivihan.technologies.doubtbuzzter2.volley;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.Map;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;

public class MyJsonObjectRequest extends JsonObjectRequest {

    // Existing constructor - uses MyApplication.getEndPoint() + url
    public MyJsonObjectRequest(int method, String url, @Nullable JSONObject jsonRequest,
                               Response.Listener<JSONObject> listener,
                               @Nullable Response.ErrorListener errorListener) {
        this(method, url, jsonRequest, listener, errorListener, false);
        if (BuildConfig.DEBUG) VolleyLogger.logRequest(method, url, jsonRequest);
    }

    // New overload - lets you choose whether to prepend base endpoint or not
    public MyJsonObjectRequest(int method, String url, @Nullable JSONObject jsonRequest,
                               Response.Listener<JSONObject> listener,
                               @Nullable Response.ErrorListener errorListener,
                               boolean isFullUrl) {
        super(method, isFullUrl ? url : MyApplication.getEndPoint() + url, jsonRequest, listener, errorListener);
        String finalUrl = isFullUrl ? url : MyApplication.getEndPoint() + url;
        if (BuildConfig.DEBUG) VolleyLogger.logRequest(method, finalUrl, jsonRequest);
    }

    public MyJsonObjectRequest(String endPoint, int method, String url, @Nullable JSONObject jsonRequest,
                               Response.Listener<JSONObject> listener,
                               @Nullable Response.ErrorListener errorListener) {
        super(method, endPoint + url, jsonRequest, listener, errorListener);
        if(BuildConfig.DEBUG) VolleyLogger.logRequest(method, MyApplication.getEndPoint() + url, jsonRequest);
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        if (BuildConfig.DEBUG) VolleyLogger.logResponse(response.statusCode, getUrl(), response.data, response.headers);
        if (response.data == null || response.data.length == 0) {
            return Response.success(new JSONObject(), HttpHeaderParser.parseCacheHeaders(response));
        }
        return super.parseNetworkResponse(response);
    }

    @Override
    public void deliverError(VolleyError error) {
        if(BuildConfig.DEBUG) VolleyLogger.logError(getUrl(), error);
//        if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
//            MyApplication.clearSharedPref();
//            android.content.Intent homeIntent = new android.content.Intent(
//                    MyApplication.getInstance(),
//                    arivihan.technologies.doubtbuzzter2.activity.UserAuthenticationActivity.class
//            );
//            homeIntent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            MyApplication.getInstance().startActivity(homeIntent);
//            return;
//        }
        super.deliverError(error);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return VolleyRequestHelper.getHeaders();
    }
}