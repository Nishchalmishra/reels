package arivihan.technologies.doubtbuzzter2.volley;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class VolleyJsonRequest extends Request<JSONObject> {

    private static final String TAG = VolleyJsonRequest.class.getSimpleName();
    private final Response.Listener<JSONObject> mListener;
    private final JSONObject mRequestBody;
    private final Map<String, String> mHeaders;

    public VolleyJsonRequest(int method, String url,
                             JSONObject requestBody,
                             Response.Listener<JSONObject> listener,
                             Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mRequestBody = requestBody;
        this.mHeaders = new HashMap<>();
        Log.d(TAG, "Initialized JSON Request for URL: " + url);
    }

    @Override
    public String getBodyContentType() {
        return "application/json; charset=utf-8";
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        if (mRequestBody != null) {
            try {
                return mRequestBody.toString().getBytes("utf-8");
            } catch (Exception e) {
                Log.e(TAG, "Error converting request body", e);
            }
        }
        return null;
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers, "utf-8"));
            Log.d(TAG, "Response: " + jsonString);
            return Response.success(new JSONObject(jsonString),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            Log.e(TAG, "Parse error", e);
            return Response.error(new com.android.volley.ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(JSONObject response) {
        mListener.onResponse(response);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        mHeaders.put("Content-Type", "application/json");
        mHeaders.put("accept", "application/json");
        return mHeaders;
    }

    public void addHeader(String key, String value) {
        mHeaders.put(key, value);
    }
}
