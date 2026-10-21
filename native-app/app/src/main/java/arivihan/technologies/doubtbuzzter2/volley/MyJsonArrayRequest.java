package arivihan.technologies.doubtbuzzter2.volley;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;

import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;

public class MyJsonArrayRequest extends JsonArrayRequest {

    public MyJsonArrayRequest(int method, String url, @Nullable JSONArray jsonRequest,
                              Response.Listener<JSONArray> listener,
                              @Nullable Response.ErrorListener errorListener) {
        super(method, MyApplication.getEndPoint() + url, jsonRequest, listener, errorListener);
//        VolleyLogger.logRequest(method, MyApplication.getEndPoint() + url, jsonRequest);
    }

    @Override
    protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
//        VolleyLogger.logResponse(response.statusCode, getUrl(), response.data, response.headers);
        return super.parseNetworkResponse(response);
    }

    @Override
    public void deliverError(VolleyError error) {
//        VolleyLogger.logError(getUrl(), error);
        super.deliverError(error);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return VolleyRequestHelper.getHeaders();
    }
}
