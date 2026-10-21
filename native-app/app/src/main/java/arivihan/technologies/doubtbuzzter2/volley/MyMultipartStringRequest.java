//package arivihan.technologies.doubtbuzzter2.volley;
//
//import android.util.Log;
//
//import com.android.volley.NetworkResponse;
//import com.android.volley.Response;
//import com.android.volley.VolleyError;
//import com.android.volley.toolbox.HttpHeaderParser;
//
//import java.util.Map;
//
//import arivihan.technologies.doubtbuzzter2.common.MultipartRequestDataPart;
//
//public class MyMultipartStringRequest {
//
//    private final Response.Listener<String> stringListener;
//
//    public MyMultipartStringRequest(
//            int method,
//            String url,
//            Map<String, String> textParams,
//            Response.Listener<String> listener,
//            Response.ErrorListener errorListener) {
//        super(method, url, response -> {}, errorListener); // actual delivery handled below
//        this.stringListener = listener;
//
//        // Add text params
//        if (textParams != null) {
//            for (Map.Entry<String, String> entry : textParams.entrySet()) {
//                try {
//                    addTextParam(entry.getKey(), entry.getValue());
//                } catch (Exception e) {
//                    Log.e("MyMultipartStringRequestFixed", "Failed to add text param: " + entry.getKey(), e);
//                }
//            }
//        }
//    }
//
//    // Add a text parameter (optional, can be called outside)
//    public void addTextParam(String key, String value) {
//        // For logging
//        Log.d("MyMultipartStringRequestFixed", "Adding text param: " + key + " = " + value);
//        // MyMultipartRequestFixed encodes text params automatically in getBody()
//        // so we just keep them in getParams()
//    }
//
//    // Add file param (delegates to parent)
//    public void addFileParam(String key, MultipartRequestDataPart filePart) {
//        Log.d("MyMultipartStringRequestFixed", "Adding file param: " + key + " -> " + filePart.getFileName() +
//                ", size=" + filePart.getContent().length);
//        super.addFilePart(key, filePart);
//    }
//
//    @Override
//    protected void deliverResponse(NetworkResponse response) {
//        try {
//            String parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "UTF-8"));
//            Log.d("MyMultipartStringRequestFixed", "API Response: " + parsed);
//            stringListener.onResponse(parsed);
//        } catch (Exception e) {
//            Log.e("MyMultipartStringRequestFixed", "Error parsing response", e);
//            deliverError(new VolleyError(e));
//        }
//    }
//
//    @Override
//    public void deliverError(VolleyError error) {
//        if (error.networkResponse != null) {
//            Log.e("MyMultipartStringRequest", "Status code: " + error.networkResponse.statusCode);
//            Log.e("MyMultipartStringRequest", "Response body: " + new String(error.networkResponse.data));
//        } else {
//            Log.e("MyMultipartStringRequest", "No network response available");
//        }
//
//        Log.e("MyMultipartStringRequest", "VolleyError: ", error);
//        super.deliverError(error);
//    }
//
//}
