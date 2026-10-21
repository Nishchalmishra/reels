package arivihan.technologies.doubtbuzzter2.volley;
import arivihan.technologies.doubtbuzzter2.reelsdemo.*;
import arivihan.technologies.doubtbuzzter2.BuildConfig;

import com.android.volley.AuthFailureError;
import com.android.volley.Header;
import com.android.volley.Request;
import com.android.volley.toolbox.BaseHttpStack;
import com.android.volley.toolbox.HttpResponse;
import com.android.volley.toolbox.HurlStack;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Custom HurlStack that supports sending a request body with DELETE requests.
 * Android's HttpURLConnection does not write body for DELETE by default in Volley.
 */
public class CustomHurlStack extends HurlStack {

    @Override
    public HttpResponse executeRequest(Request<?> request, Map<String, String> additionalHeaders)
            throws IOException, AuthFailureError {

        if (BuildConfig.REELS_DEMO && !"pbaegeqjargtsfortsyh.supabase.co".equals(new java.net.URL(request.getUrl()).getHost())) {
            byte[] body="{\"message\":\"This app service is unavailable in the isolated demo. No production request was sent.\"}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            return new HttpResponse(503,java.util.Collections.singletonList(new Header("Content-Type","application/json")),body.length,new java.io.ByteArrayInputStream(body));
        }
        if (request.getMethod() == Request.Method.DELETE && request.getBody() != null) {
            HttpURLConnection connection = (HttpURLConnection) new URL(request.getUrl()).openConnection();
            connection.setInstanceFollowRedirects(HttpURLConnection.getFollowRedirects());
            connection.setRequestMethod("DELETE");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setConnectTimeout(request.getTimeoutMs());
            connection.setReadTimeout(request.getTimeoutMs());
            connection.setUseCaches(false);

            for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
            for (Map.Entry<String, String> entry : additionalHeaders.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }

            connection.setRequestProperty("Content-Type", request.getBodyContentType());

            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.write(request.getBody());
            out.close();

            int responseCode = connection.getResponseCode();
            if (responseCode == -1) {
                throw new IOException("Could not retrieve response code from HttpUrlConnection.");
            }

            InputStream inputStream;
            try {
                inputStream = connection.getInputStream();
            } catch (IOException e) {
                inputStream = connection.getErrorStream();
            }

            List<Header> headers = new ArrayList<>();
            Map<String, List<String>> headerFields = connection.getHeaderFields();
            for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
                if (entry.getKey() != null) {
                    for (String value : entry.getValue()) {
                        headers.add(new Header(entry.getKey(), value));
                    }
                }
            }

            int contentLength = connection.getContentLength();
            return new HttpResponse(responseCode, headers, contentLength, inputStream);
        }

        return super.executeRequest(request, additionalHeaders);
    }
}
