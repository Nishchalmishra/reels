package arivihan.technologies.doubtbuzzter2.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OTPReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Log.d("OTPReceiver", "on receive called");

            if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
                Bundle extras = intent.getExtras();
                Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);

                switch (status.getStatusCode()) {
                    case CommonStatusCodes.SUCCESS:
                        // (Optional) Get SMS Sender address - only available in
                        // GMS version 24.20 onwards, else it will return null
                        // Get SMS message contents
                        String message = extras.getString(SmsRetriever.EXTRA_SMS_MESSAGE);
                        if (message != null) {
                            Log.d("OTPReceiver", message);
                            Intent otpIntent = new Intent("otp_received");
                            otpIntent.putExtra("otp", extractVerificationCode(message));
                            LocalBroadcastManager.getInstance(context).sendBroadcast(otpIntent);
                        } else {
                            Log.e("OTPReceiver", "No SMS message received");
                        }
                        break;
                    case CommonStatusCodes.TIMEOUT:
                        // Waiting for SMS timed out (5 minutes)
                        // Handle the error ...
                        break;
                }
            }
        } catch (Exception e) {
            Log.e("OTPReceiver", "Exception in onReceive", e);
        }

    }

    private String extractVerificationCode(String message) {
        try {
            Pattern pattern = Pattern.compile("\\d{6}");
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                return matcher.group(0);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }


}
