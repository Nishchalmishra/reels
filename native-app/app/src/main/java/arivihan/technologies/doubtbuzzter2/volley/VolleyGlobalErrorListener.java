
package arivihan.technologies.doubtbuzzter2.volley;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;

import java.util.Arrays;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.UserAuthenticationActivity;
import arivihan.technologies.doubtbuzzter2.common.SlowInternetPopUp;
import arivihan.technologies.doubtbuzzter2.model.ErrorDetails;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public abstract class VolleyGlobalErrorListener implements Response.ErrorListener {

    public abstract void postError(VolleyError error);

    private Context context;
    SlowInternetPopUp slowInternetPopUp;

    public VolleyGlobalErrorListener(Context context) {
        this.context = context;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d("VolleyGlobalErrorListener", "onErrorResponse: Entered"+error.toString());

        byte[] htmlBodyBytes = new byte[0];
        boolean posted = false;
        try {
            htmlBodyBytes = error.networkResponse.data;
            Log.d("VolleyGlobalErrorListener", "onErrorResponse: Network response data retrieved" + Arrays.toString(htmlBodyBytes));
        } catch (NullPointerException e) {
            Log.e("VolleyGlobalErrorListener", "onErrorResponse: NullPointerException - No network response data", e);
        }
        try {
            String errorMessage = new String(htmlBodyBytes);
            Gson gson = new Gson();
            ErrorDetails errorDetails = gson.fromJson(errorMessage, ErrorDetails.class);
            Log.d("errorDetails", "onErrorResponse: "+errorDetails);

            if (errorDetails != null) {
                if (errorDetails.getStatus() == 401) {
//                    Log.d("volleyHeader", "401 received");
                    MyApplication.clearSharedPref();
                    Intent homeIntent = new Intent(context, UserAuthenticationActivity.class);
                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(homeIntent);
                }
                error = new VolleyError(errorDetails.getMessage());
                posted = true;
                postError(error);
            } else {
                Log.e("VolleyGlobalErrorListener", "handle genericError_Listener form else : Exception while parsing error response");
                posted = true;
                handleGenericError(error);
            }
        } catch (Exception e) {
            Log.e("VolleyGlobalErrorListener", "onErrorResponse: Exception while parsing error response", e);
        }

        if (!posted) {
            Log.e("VolleyGlobalErrorListener", "from posted: Exception while parsing error response");
            handleGenericError(error);
        }
    }

    private void handleGenericError(VolleyError error) {
        if (error instanceof NoConnectionError && !MyApplication.isNetworkAvailable()) {
            error = new VolleyError(MyApplication.getInstance().getString(R.string.internet_failure_error));
            postError(error);
            Log.d("VolleyGlobalErrorListener", "no internet  : Displaying dialog...");
        } else {
            error = new VolleyError(MyApplication.getInstance().getString(R.string.slow_internet_error));
            Log.d("VolleyGlobalErrorListener", "handleGeneric error : Displaying dialog...");
            if (MyApplication.getFirebaseRemoteConfig().getBoolean("show_slow_internet_pop_up")) {
                showDialogOnce();
            } else {
                long currentTime = System.currentTimeMillis();
                long lastCheckedTime = MyApplication.getLongData(PreferenceKeys.LAST_SELECTED_CHECKED_TIME.getKey());

                if (currentTime > lastCheckedTime || (currentTime - lastCheckedTime) >= 5000) {
                    Log.d("VolleyGlobalError", "handleGenericError: show error - " + error.toString());

                    MyApplication.saveData(PreferenceKeys.LAST_SELECTED_CHECKED_TIME.getKey(), currentTime);

                    postError(error);
                } else {
                    Log.d("VolleyGlobalError", "Skipping error post as it's within the 5-second window.");
                }

            }
        }
    }

    private void showDialogOnce() {
        Log.d("VolleyGlobalErrorListener", "showDialogOnce: Displaying dialog...");
        slowInternetPopUp = new SlowInternetPopUp(context);
        slowInternetPopUp.showSlowInternetDialog();
    }
}