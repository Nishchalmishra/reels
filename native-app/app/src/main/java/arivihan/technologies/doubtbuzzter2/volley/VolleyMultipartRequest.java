package arivihan.technologies.doubtbuzzter2.volley;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public abstract class VolleyMultipartRequest extends Request<NetworkResponse> {

    private static final String TAG = VolleyMultipartRequest.class.getSimpleName();
    private final Response.Listener<NetworkResponse> mListener;
    private final Map<String, String> mHeaders;
    private static final String twoHyphens = "--";
    private static final String lineEnd = "\r\n";
    private static final String boundary = UUID.randomUUID().toString();

    public VolleyMultipartRequest(int method, String url,
                                  Response.Listener<NetworkResponse> listener,
                                  Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mHeaders = null;

        Log.d(TAG, "Initialized Multipart Request for URL: " + url);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Log.d(TAG, "Getting Headers");
        return (mHeaders != null) ? mHeaders : super.getHeaders();
    }

    @Override
    public String getBodyContentType() {
        String contentType = "multipart/form-data;boundary=" + boundary;
        Log.d(TAG, "Content-Type: " + contentType);
        return contentType;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        Log.d(TAG, "Building request body...");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            Log.d(TAG, "Adding text parameters...");
            Map<String, String> params = getParams();
            if (params != null && params.size() > 0) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    Log.d(TAG, "Text param - " + entry.getKey() + ": " + entry.getValue());
                    buildTextPart(bos, entry.getKey(), entry.getValue());
                }
            }

            Log.d(TAG, "Adding file parameters...");
            Map<String, DataPart> data = getByteData();
            if (data != null && data.size() > 0) {
                for (Map.Entry<String, DataPart> entry : data.entrySet()) {
                    Log.d(TAG, "File param - " + entry.getKey() + ": " + entry.getValue().getFileName() + " (" + entry.getValue().getContent().length + " bytes)");
                    buildDataPart(bos, entry.getValue(), entry.getKey());
                }
            }

            // Closing boundary
            bos.write((twoHyphens + boundary + twoHyphens + lineEnd).getBytes());

            Log.d(TAG, "Request body built successfully.");
        } catch (IOException e) {
            Log.e(TAG, "Error while building request body", e);
        }

        return bos.toByteArray();
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        Log.d(TAG, "Parsing network response. Status code: " + response.statusCode);
        return Response.success(
                response,
                HttpHeaderParser.parseCacheHeaders(response)
        );
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        Log.d(TAG, "Delivering response to listener");
        mListener.onResponse(response);
    }

    protected abstract Map<String, DataPart> getByteData() throws AuthFailureError;

    private void buildTextPart(ByteArrayOutputStream bos, String parameterName, String parameterValue) throws IOException {
        bos.write((twoHyphens + boundary + lineEnd).getBytes());
        bos.write(("Content-Disposition: form-data; name=\"" + parameterName + "\"" + lineEnd).getBytes());
        bos.write(("Content-Type: text/plain; charset=UTF-8" + lineEnd).getBytes());
        bos.write(lineEnd.getBytes());
        bos.write(parameterValue.getBytes());
        bos.write(lineEnd.getBytes());
    }

    private void buildDataPart(ByteArrayOutputStream bos, DataPart dataFile, String inputName) throws IOException {
        bos.write((twoHyphens + boundary + lineEnd).getBytes());
        bos.write(("Content-Disposition: form-data; name=\"" + inputName + "\"; filename=\"" + dataFile.getFileName() + "\"" + lineEnd).getBytes());
        bos.write(("Content-Type: " + dataFile.getType() + lineEnd).getBytes());
        bos.write(lineEnd.getBytes());

        bos.write(dataFile.getContent());
        bos.write(lineEnd.getBytes());
    }

    public static class DataPart {
        private final String fileName;
        private final byte[] content;
        private final String type;

        public DataPart(String fileName, byte[] content, String type) {
            this.fileName = fileName;
            this.content = content;
            this.type = type;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getContent() {
            return content;
        }

        public String getType() {
            return type;
        }
    }
}
