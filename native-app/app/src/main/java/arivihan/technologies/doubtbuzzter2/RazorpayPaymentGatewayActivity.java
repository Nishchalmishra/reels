package arivihan.technologies.doubtbuzzter2;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.PAYMENT_INFO_RAZORPAY;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.USER_SUBSCRIPTION;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.activity.HomeScreen;
import arivihan.technologies.doubtbuzzter2.activity.QRCodePaymentActivity;
import arivihan.technologies.doubtbuzzter2.activity.SubscriptionCongatulationsActivity;
import arivihan.technologies.doubtbuzzter2.activity.competitive.FeatureInfoVideoActivity;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.model.PaymentDetails;
import arivihan.technologies.doubtbuzzter2.model.PaymentInfo;
import arivihan.technologies.doubtbuzzter2.model.PaymentStatus;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionBenefitsModel;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionPostModel;
import arivihan.technologies.doubtbuzzter2.model.TransactionType;
import arivihan.technologies.doubtbuzzter2.model.UserSubscriptionDto;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.helper.BranchAnalyticsHelper;
import arivihan.technologies.doubtbuzzter2.helper.EnvironmentManager;

public class RazorpayPaymentGatewayActivity extends AppCompatActivity implements PaymentResultListener {

    public static final String BUNDLE_COIN_FLOW = "coinFlow";

    private String orderId;
    private String planId;
    private UserSubscriptionDto userSubscriptionDto;
    Gson gson = new Gson();
    private boolean paymentSuccess;
    private boolean coinFlow = false;
    private RedirectionSource redirectionSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_screen);

        Checkout.preload(this);
        orderId = getIntent().getStringExtra("orderId");
        planId = getIntent().getStringExtra("planId");
        userSubscriptionDto = (UserSubscriptionDto) getIntent().getSerializableExtra("userSubscriptionDto");
        coinFlow = getIntent().getBooleanExtra(BUNDLE_COIN_FLOW, false);

        Object sourceExtra = getIntent().getSerializableExtra(SubscriptionNavigationHelper.EXTRA_SOURCE);
        redirectionSource = null;
        if (sourceExtra instanceof RedirectionSource) {
            redirectionSource = (RedirectionSource) sourceExtra;
        }

        String paymentAmount = getIntent().getStringExtra("amount");

        Map<String, String> params = new HashMap<>();
        params.put("Subscription_plan_Id", planId);
        params.put("mode", "sdk_payment_mode");
        params.put("amount", paymentAmount);
        if (redirectionSource != null) {
            params.put("navigation_from", redirectionSource.getFirebaseName());
        }
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.subscription_interested_customer.getActionName(), params);

        if (paymentAmount != null) {
            try {
                initiatePaymentInDb(Long.parseLong(paymentAmount));
            } catch (Exception e) {
                Log.d("onCreate    ", e.getMessage());
            }
        }
//        startPayment(Long.parseLong(paymentAmount));
    }



    public void startPayment(long paymentAmount) {
        Checkout checkout = new Checkout();
        // Razorpay is hosted in a separate activity. Avoid fullscreen rendering so it
        // reserves the status-bar inset instead of leaving a dark unused top area.
        checkout.setFullScreenDisable(true);

        String currentEnv = EnvironmentManager.getEnvironmentNameByUrl(MyApplication.getEndPoint());
        String razorpayKey;
        if ("PROD".equalsIgnoreCase(currentEnv))
            razorpayKey = MyApplication.getFirebaseRemoteConfig().getString("razorpay_key_id_prod_v2");
        else
            razorpayKey = MyApplication.getFirebaseRemoteConfig().getString("razorpay_key_id_dev_v2");

        checkout.setKeyID(razorpayKey);

        String userEmail = MyApplication.getStringData(PreferenceKeys.USER_EMAIL.getKey());
        String userPhone = MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey());

        try {
            JSONObject options = new JSONObject();

            options.put(PreferenceKeys.NAME.getKey(), "Arivihan");
            options.put("description", "Plan Id :" + planId);
            options.put("image", "https://reels-video.s3.ap-south-1.amazonaws.com/images/logo.png");
            options.put("order_id", orderId);//from response of step 3.
            options.put("theme.color", "#00838f");
            options.put("theme.hide_topbar", true);
            options.put("currency", "INR");
            options.put("amount", paymentAmount + "");//pass amount in currency subunits
            options.put("prefill.email", userEmail);
            options.put("prefill.contact", userPhone);

            JSONObject method = new JSONObject();
            method.put("netbanking", 1);
            method.put("card", 1);
            method.put("upi", 1);
            method.put("wallet", 1);

            JSONObject retryObj = new JSONObject();
            retryObj.put("enabled", true);
            retryObj.put("max_count", 4);
            options.put("retry", retryObj);
            options.put("method", method);
//            options.put("callback_url", "https://e26b-2409-408c-8e00-a90-c458-a254-4ab6-6579.ngrok-free.app/arivihan-platform/secure/payment/razorpay/callback");
            checkout.open(this, options);

        } catch (Exception e) {
            Log.e("ERROR", "Error in starting Razorpay Checkout", e);
        }
    }


    private void initiatePaymentInDb(long paymentAmount) {
        String userId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
        Gson gson = new Gson();
        PaymentDetails paymentDetails = PaymentDetails
                .builder()
                .amount(paymentAmount * 100)
                .orderId(orderId)
                .type(TransactionType.SUBSCRIPTION.name())
                .userId(userId)
                .build();
        try {
            MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.POST,
                    PAYMENT_INFO_RAZORPAY, new JSONObject(gson.toJson(paymentDetails)),
                    response -> {
                        TypeToken<PaymentInfo> token = new TypeToken<PaymentInfo>() {
                        };
                        PaymentInfo paymentInfo = gson.fromJson(response.toString(), token.getType());
                        if (paymentInfo != null)
                            Log.d("apiresponse", "" + paymentInfo.toString());
                        Log.i("ORDERIDRAZORPAY", "ORDERID :: " + paymentInfo.getOrderId());
                        Log.i("ORDERIDRAZORPAY", "ORDERID :: " + paymentAmount);

                        startPayment(paymentAmount * 100);

                    }, new VolleyGlobalErrorListener(this) {
                @Override
                public void postError(VolleyError error) {
                    Map<String, String> params = new HashMap<>();
                    params.put("Subscription_plan_Id", planId);
                    String amount = getIntent().getStringExtra("amount");
                    params.put("amount", amount);
                    params.put("Subscription_plan_Id", planId);
                    params.put("action", "PaymentError reason PAYMENT_INFO_RAZORPAY= " + error.getMessage());
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.Payment_failed.getActionName(), params);
                    try {
                        BranchAnalyticsHelper.logPaymentFailed(RazorpayPaymentGatewayActivity.this, planId, safeParseAmount(amount), "RAZOR PAY SDK", "PaymentError reason PAYMENT_INFO_RAZORPAY= " + error.getMessage());
                    } catch (Exception e) {
                        Log.e("BranchEvent", "Failed to log payment_failed event", e);
                    }
                    Toast.makeText(getApplicationContext(), getString(R.string.encountered_some_error), Toast.LENGTH_SHORT).show();

                }
            });
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), getString(R.string.encountered_some_error), Toast.LENGTH_SHORT).show();
        }

    }


    private void buyNowPostCall(String transactionId) {
        Map<String, Object> map = new HashMap<>();
        map.put("coinFlow", coinFlow);
        SubscriptionPostModel subscriptionPostModel = new SubscriptionPostModel();
        subscriptionPostModel.setPaymentStatus(PaymentStatus.SUCCESS.name());
        subscriptionPostModel.setInvoiceId(userSubscriptionDto.getInvoiceId());
        subscriptionPostModel.setTransactionId(transactionId);
        subscriptionPostModel.setSubscriptionId(userSubscriptionDto.getId());
        subscriptionPostModel.setOrderId(orderId);

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(gson.toJson(subscriptionPostModel));
        } catch (Exception e) {
            Log.d("Exception1", e.getMessage());
        }

        if (jsonObject == null) {
            Toast.makeText(this, "Sorry we faced some technical problem, Please contact us.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.PUT,
                CommonMethods.getUrlParam(USER_SUBSCRIPTION, map), jsonObject,
                response -> {
                    try {
                        Log.d("apiresultJson", response.toString());
                        boolean success = (boolean) response.get("success");
                        if (success) {

                            facebookLogPurchaseEvent(subscriptionPostModel.getSubscriptionId(), getIntent().getLongExtra("paymentAmount", 0));
                            try {
                                BranchAnalyticsHelper.logPurchase(this, planId, transactionId, getIntent().getLongExtra("paymentAmount", 0), "RAZOR PAY SDK");
                            } catch (Exception e) {
                                Log.e("BranchEvent", "Failed to log PURCHASE event", e);
                            }
                            if (!MyApplication.getFirebaseRemoteConfig().getBoolean(getString(R.string.show_subscription_benefits))) {
                                Toast.makeText(this, "Congratulation you have subscribed", Toast.LENGTH_SHORT).show();
                            }

                            new Handler().postDelayed(() -> {
                                if (MyApplication.getFirebaseRemoteConfig().getBoolean(getString(R.string.show_subscription_benefits)) &&
                                        StringUtils.isEmpty(getIntent().getStringExtra("completedVideoUrl"))) {
                                    try {
                                        Type listType = new TypeToken<List<SubscriptionBenefitsModel>>() {
                                        }.getType();
                                        ArrayList<SubscriptionBenefitsModel> list = gson.fromJson(String.valueOf(response.get("data")), listType);
                                        Intent intent = new Intent(RazorpayPaymentGatewayActivity.this, SubscriptionCongatulationsActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        intent.putParcelableArrayListExtra("benefits", list);
                                        intent.putExtra("congratulationHeader", (response.has("congratulationHeader") && !response.isNull("congratulationHeader"))?response.getString("congratulationHeader"):"");
                                        intent.putExtra("congratulationSubHeader", (response.has("congratulationSubHeader") && !response.isNull("congratulationSubHeader"))?response.getString("congratulationSubHeader"):"");
                                        intent.putExtra("planId", planId);
                                        startActivity(intent);
                                        finish();
                                    } catch (JSONException e) {
                                        //throw new RuntimeException(e);
                                        Log.d("Exception4", e.getMessage());
                                        Toast.makeText(this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                                    }
                                } else if (StringUtils.isNotEmpty(getIntent().getStringExtra("completedVideoUrl"))) {
                                    Intent intent = new Intent(RazorpayPaymentGatewayActivity.this, FeatureInfoVideoActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.putExtra("hide_bottom_layout",true);
                                    intent.putExtra("videoUrl",getIntent().getStringExtra("completedVideoUrl"));
                                    intent.putExtra(FeatureInfoVideoActivity.bundleNavigationFrom, FeatureInfoVideoActivity.NavigationFromEnum.RAZORPAY_SCREEN.name());
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Intent intent = new Intent(RazorpayPaymentGatewayActivity.this, HomeScreen.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            }, 2000);

                        } else {
                            Toast.makeText(this, "Sorry we faced some technical problem, Please contact us.", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        Log.d("Exception2", e.getMessage());
                        Toast.makeText(this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();

                    }
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                Map<String, String> params = new HashMap<>();
                params.put("Subscription_plan_Id", planId);
                String amount = getIntent().getStringExtra("amount");
                params.put("amount", amount);
                params.put("Subscription_plan_Id", planId);
                params.put("action", "PaymentError reason USER_SUBSCRIPTION= " + error.getMessage());
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.Payment_failed.getActionName(), params);
                try {
                    BranchAnalyticsHelper.logPaymentFailed(RazorpayPaymentGatewayActivity.this, planId, safeParseAmount(amount), "RAZOR PAY SDK", "PaymentError reason USER_SUBSCRIPTION= " + error.getMessage());
                } catch (Exception e) {
                    Log.e("BranchEvent", "Failed to log payment_failed event", e);
                }
                Log.d("Exception3", error.getMessage());
                Toast.makeText(getApplicationContext(), "Sorry we faced some technical problem, Please contact us.", Toast.LENGTH_SHORT).show();

            }
        });
        myJsonObjectRequest2.setTag("data");
        MyVolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(myJsonObjectRequest2);
    }


    @Override
    protected void onDestroy() {
        if (!paymentSuccess) {
            Map<String, String> params = new HashMap<>();
            params.put("Subscription_plan_Id", planId);
            String amount = getIntent().getStringExtra("amount");
            params.put("amount", amount);
            params.put("Subscription_plan_Id", planId);
            params.put("action", "PaymentError reason = user killed app");
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.Payment_failed.getActionName(), params);
            try {
                BranchAnalyticsHelper.logPaymentFailed(this, planId, safeParseAmount(amount), "RAZOR PAY SDK", "user killed app");
            } catch (Exception e) {
                Log.e("BranchEvent", "Failed to log payment_failed event", e);
            }
        }
        super.onDestroy();
        Checkout.clearUserData(this);
        MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll("");
    }

    @Override
    public void onPaymentSuccess(String s) {
        paymentSuccess = true;

        String amountStr = getIntent().getStringExtra("amount");
        int paymentAmount = 0;

        try {
            paymentAmount = Integer.parseInt(amountStr);
        } catch (Exception e) {
        }

        Map<String, String> params = new HashMap<>();
        params.put("Subscription_plan_Id", planId);
        params.put("amount", String.valueOf(paymentAmount));
        params.put("modeOfPayment", "RAZOR PAY SDK");

        String eventName = "payment_successful_" + paymentAmount;
        MyApplication.logFirebaseBundleCreation(eventName, params);

        MyApplication.logFirebaseBundleCreation(FirebaseEvents.Payment_successful.getActionName(), params);
        MyApplication.purchaseFirebaseEvent(planId, s, Double.parseDouble(getIntent().getStringExtra("amount")), "RAZOR PAY SDK", redirectionSource != null ? redirectionSource.getFirebaseName() : "unknown");

        buyNowPostCall(s);
    }


    @Override
    public void onPaymentError(int i, String s) {
        Map<String, String> params = new HashMap<>();
        params.put("Subscription_plan_Id", planId);
        String amount = getIntent().getStringExtra("amount");
        params.put("amount", amount);
        params.put("Subscription_plan_Id", planId);
        params.put("action", "PaymentError reason = " + s);
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.Payment_failed.getActionName(), params);
        try {
            BranchAnalyticsHelper.logPaymentFailed(this, planId, safeParseAmount(amount), "RAZOR PAY SDK", "PaymentError reason = " + s);
        } catch (Exception e) {
            Log.e("BranchEvent", "Failed to log payment_failed event", e);
        }

        // Log the amount for testing
        Log.d("PaymentError", "Payment failed. Amount: " + amount);

        Toast.makeText(getApplicationContext(), getString(R.string.payment_failed), Toast.LENGTH_SHORT).show();
        getOnBackPressedDispatcher().onBackPressed();
    }

    private void facebookLogPurchaseEvent(String subscriptionId, double amount) {
        AppEventsLogger logger = AppEventsLogger.newLogger(this);

        Bundle params = new Bundle();
        params.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, "INR");
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product");
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, subscriptionId);
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT, "[{'id': '" + subscriptionId + "', 'quantity': 1}]");

        logger.logEvent(AppEventsConstants.EVENT_NAME_PURCHASED,
                amount,
                params);
    }

    private double safeParseAmount(String amount) {
        try {
            return Double.parseDouble(amount);
        } catch (Exception e) {
            return 0;
        }
    }

}