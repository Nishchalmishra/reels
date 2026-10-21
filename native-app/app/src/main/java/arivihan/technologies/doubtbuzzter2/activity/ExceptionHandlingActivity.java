package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.SEND_EMAIL;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.model.EmailDto;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class ExceptionHandlingActivity extends AppCompatActivity {

    private MaterialCardView goToHome;
    private ConstraintLayout parentLayout;
    private ProgressDialog progressDialog;
    private MaterialCardView reportIssue;
    private String body;
    private Dialog dialog;
    private TextView message;
    private Handler handler;
    private Runnable runnable;
    private  Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         gson=new Gson();
        setContentView(R.layout.activity_exception_handling);
        goToHome = findViewById(R.id.go_to_home);
        goToHome.setOnClickListener(v -> {
            startActivity(new Intent(ExceptionHandlingActivity.this, HomeScreen.class));
        });
        parentLayout = findViewById(R.id.parent_layout);
        progressDialog = new ProgressDialog(this);
        reportIssue = findViewById(R.id.report_issue);
        body = getIntent().getStringExtra("body");
        dialog = new Dialog(this);
        ;
        dialog.setContentView(R.layout.dialog_finish_mock_test);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        message = dialog.findViewById(R.id.message);
        message.setText(getString(R.string.report_has_been_submitted));

        reportIssue.setOnClickListener(v -> {
            if (!isFinishing() && !isDestroyed()) {
                progressDialog.show();
                progressDialog.setMessage("Reporting the issue");
                progressDialog.setCancelable(false);
                if (getApplicationContext() != null) {
                    sendCrashEmail(body);
                }
            }
        });
    }

    private void sendCrashEmail(String emailBody) {
        try {
            String userId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
            String subject = getString(R.string.report_the_issue_subject);
            emailBody = String.format(getString(R.string.report_the_issue_body), userId) + emailBody;

            EmailDto emailDto = new EmailDto(subject, emailBody, userId);
            MyJsonObjectRequest submitReport = new MyJsonObjectRequest(
                    Request.Method.POST,
                    SEND_EMAIL, // Ensure this URL is correct
                    new JSONObject(gson.toJson(emailDto)),
                    response -> {
                        Log.i("REPORTRESPONSE", response.toString());
                        if (!isFinishing() && !isDestroyed()) {
                            dialog.show();
                            progressDialog.dismiss();
                            handleDialog();
                        }
                    },
                    new VolleyGlobalErrorListener(this) {
                        @Override
                        public void postError(VolleyError error) {
                            if (!isFinishing() && !isDestroyed()) {
                                Log.i("REPORTERRORRESPONSE", error.toString());
                                CommonMethods.showNegativeSnackbar(parentLayout, "Error occurred in submitting the report");
                                progressDialog.dismiss();
                            }
                        }
                    });

            // Add Request to Volley Queue
            MyVolleySingleton.getInstance(this).addToRequestQueue(submitReport);

        } catch (Exception e) {
            Log.e("Exception_handling_activity", "sendCrashEmail: " + e.toString());
        }
    }


    private void handleDialog() {
        handler = new Handler();
        runnable = () -> {
            dialog.dismiss();
            handler.removeCallbacksAndMessages(null);
            getOnBackPressedDispatcher().onBackPressed();
        };
        handler.postDelayed(runnable, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
    }
}