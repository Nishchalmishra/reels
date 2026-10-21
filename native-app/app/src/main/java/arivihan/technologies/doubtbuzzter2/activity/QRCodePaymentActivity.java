package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.PAYMENT_INFO_RAZORPAY;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.RAZORPAY_CHECK_QR_PAYMENT_STATUS;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.RAZORPAY_CREATE_QR_CODE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.USER_SUBSCRIPTION;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.widget.TextViewCompat;

import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.competitive.FeatureInfoVideoActivity;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.model.PaymentDetails;
import arivihan.technologies.doubtbuzzter2.model.PaymentInfo;
import arivihan.technologies.doubtbuzzter2.model.PaymentStatus;
import arivihan.technologies.doubtbuzzter2.model.RazorpayQRCreateResponse;
import arivihan.technologies.doubtbuzzter2.model.RazorpayQRPaymentResponse;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionBenefitsModel;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionPostModel;
import arivihan.technologies.doubtbuzzter2.model.TransactionType;
import arivihan.technologies.doubtbuzzter2.model.UserSubscriptionDto;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.helper.BranchAnalyticsHelper;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;

public class QRCodePaymentActivity extends AppCompatActivity {

    public static final String BUNDLE_COIN_FLOW = "coinFlow";
    public static final String BUNDLE_DISPLAY_PAYABLE_AMOUNT = "displayPayableAmount";

    ImageView closeBtn, qrCodeImage;
    LinearProgressIndicator progressIndicator;
    private static final long COUNTDOWN_IN_MILLIS =MyApplication.getFirebaseRemoteConfig().getLong("qr_code_count_down_timer");
    MyJsonObjectRequest getQRCodeForPayment;
    CardView otherPaymentOption;
    UserSubscriptionDto userSubscriptionDto;

    ShimmerFrameLayout shimmerFrameLayout;
    TextView subscriptionPlanName, planCostPrice, planFinalPrice, countDownTimer;
    Gson gson = new Gson();
    RazorpayQRCreateResponse razorpayQRCreateResponse;

    Handler handler = new Handler();
    Handler imageHandler;
    Runnable imageRunnable;
    Runnable buyNowPostRunnable;
    private boolean coinFlow = false;

    CountDownTimer uiCountDownTimer;
    String planId;
    private RedirectionSource redirectionSource;

    private static final String PREF_QR_IMAGE_URL = "cpa_qr_image_url";
    private static final String PREF_QR_ID = "cpa_qr_id";
    private static final String PREF_QR_TIMESTAMP = "cpa_qr_timestamp";
    private static final String PREF_QR_ORDER_ID = "cpa_qr_order_id";
    private static final long QR_CACHE_DURATION_MS = 24 * 60 * 60 * 1000L; // 24 hours


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_payment);
        qrCodeImage = findViewById(R.id.qr_code_payment_image);
        subscriptionPlanName = findViewById(R.id.subscription_plan_name);
        planCostPrice = findViewById(R.id.cost_price);
        planFinalPrice = findViewById(R.id.final_price);
        progressIndicator = findViewById(R.id.progress_indicator);
        countDownTimer = findViewById(R.id.countdown_timer);
        otherPaymentOption = findViewById(R.id.other_payment_option);
        shimmerFrameLayout = findViewById(R.id.qr_code_shimmer);

        planId = getIntent().getStringExtra("subscriptionPlanId");
        coinFlow = getIntent().getBooleanExtra(BUNDLE_COIN_FLOW, false);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                planCostPrice, 8, 14, 1, TypedValue.COMPLEX_UNIT_SP);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                planFinalPrice, 8, 14, 1, TypedValue.COMPLEX_UNIT_SP);

        subscriptionPlanName.setText(getIntent().getStringExtra("planName"));
        planCostPrice.setPaintFlags(planCostPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        planCostPrice.setText("₹" + getIntent().getStringExtra("costPrice"));
        planCostPrice.setTextColor(getResources().getColor(R.color.grey_500));

        // When coinFlow is active and coins are deducted, show the actual payable amount (after coin deduction)
        int displayPayable = getIntent().getIntExtra(BUNDLE_DISPLAY_PAYABLE_AMOUNT, -1);
        if (coinFlow && displayPayable >= 0) {
            planFinalPrice.setText("₹" + displayPayable);
        } else {
            planFinalPrice.setText("₹" + getIntent().getIntExtra("paymentAmount", 0));
        }

        Object sourceExtra = getIntent().getSerializableExtra(SubscriptionNavigationHelper.EXTRA_SOURCE);
        redirectionSource = null;
        if (sourceExtra instanceof RedirectionSource) {
            redirectionSource = (RedirectionSource) sourceExtra;
        }

        Map<String,String>params= new HashMap<>();
        params.put("Subscription_plan_Id", getIntent().getStringExtra("planName"));
        params.put("mode", "qr_payment_mode");
        params.put("amount", "" + getIntent().getIntExtra("paymentAmount", 0));
        if (redirectionSource != null) {
            params.put("navigation_from", redirectionSource.getFirebaseName());
        }
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.subscription_interested_customer.getActionName(),params);

        userSubscriptionDto = (UserSubscriptionDto) getIntent().getSerializableExtra("userSubscriptionDto");

        otherPaymentOption.setOnClickListener(v -> {
            MyApplication.saveData(PreferenceKeys.OTHER_PAYMENT.getKey(), true);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("paymentAmount", getIntent().getIntExtra("paymentAmount", 0));
            resultIntent.putExtra("finalAmount", getIntent().getIntExtra("finalAmount",0));
            resultIntent.putExtra("isFromBottomOfferSheet", getIntent().getBooleanExtra("isFromBottomOfferSheet", false));
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        closeBtn = findViewById(R.id.close_btn);

        closeBtn.setOnClickListener(v -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.on_back_btn_click_or_swipe.getActionName());
            finish();
        });

        startCountdown(COUNTDOWN_IN_MILLIS);
        fetchQRCode();

        buyNowPostRunnable = new Runnable() {
            @Override
            public void run() {
                checkStatusOfQRPayment();
                handler.postDelayed(this, 5000);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.removeCallbacks(buyNowPostRunnable);
        handler.post(buyNowPostRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(buyNowPostRunnable);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && buyNowPostRunnable != null) {
            handler.removeCallbacks(buyNowPostRunnable);
        }

        if (imageHandler != null && imageRunnable != null) {
            imageHandler.removeCallbacks(imageRunnable);
        }
        if (uiCountDownTimer != null) {
            uiCountDownTimer.cancel();
        }
    }

//    private void fetchQRCode() {
//        shimmerFrameLayout.setVisibility(View.VISIBLE);
//
//        Log.i("QRCODEPAYMENTData", "TOTAL AMOUNT :: " + getIntent().getIntExtra("paymentAmount", 0));
//        Map<String, Object> requestData = new HashMap<>();
//        requestData.put("orderId",            getIntent().getStringExtra("orderId"));
//        requestData.put("subscriptionPlanId", getIntent().getStringExtra("subscriptionPlanId"));
//        requestData.put("paymentAmount",      getIntent().getIntExtra("paymentAmount", 0));
//        requestData.put("transactionId",      getIntent().getStringExtra("transactionId"));
//
//        getQRCodeForPayment = new MyJsonObjectRequest(
//                Request.Method.GET,
//                CommonMethods.getUrlParam(RAZORPAY_CREATE_QR_CODE, requestData),
//                null,
//                jsonObject -> {
//                    TypeToken<RazorpayQRCreateResponse> token = new TypeToken<RazorpayQRCreateResponse>() {};
//                    razorpayQRCreateResponse = new Gson().fromJson(jsonObject.toString(), token.getType());
//                    Log.i("QRCODEPAYMENTData", razorpayQRCreateResponse.toString());
//
//
//                    initiatePaymentInDb(getIntent().getIntExtra("paymentAmount", 0));
//                    showQrCode();
//                },
//                volleyError -> {
//                    Log.i("QRCODEPAYMENT", volleyError.toString());
//                    Map<String, String> params = new HashMap<>();
//                    params.put("Subscription_plan_Id", getIntent().getStringExtra("subscriptionPlanId"));
//                    params.put("amount",               getIntent().getStringExtra("amount"));
//                    params.put("action", "PaymentError reason QR Code = RAZORPAY_CREATE_QR_CODE" + volleyError);
//                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.Payment_failed.getActionName(), params);
//                }
//        );
//
//        MyVolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(getQRCodeForPayment);
//    }


    private void fetchQRCode() {
        shimmerFrameLayout.setVisibility(View.VISIBLE);

        String currentOrderId = getIntent().getStringExtra("orderId");

        String cachedOrderId  = MyApplication.getStringData(PREF_QR_ORDER_ID);
        String cachedImageUrl = MyApplication.getStringData(PREF_QR_IMAGE_URL);
        String cachedQrId     = MyApplication.getStringData(PREF_QR_ID);
        long   cachedTime     = MyApplication.getLongData(PREF_QR_TIMESTAMP);

        boolean isSameOrder   = currentOrderId != null && currentOrderId.equals(cachedOrderId);
        boolean isNotExpired  = (System.currentTimeMillis() - cachedTime) < QR_CACHE_DURATION_MS;
        boolean isCacheValid  = isSameOrder && isNotExpired
                && cachedImageUrl != null && cachedQrId != null;

        Log.i("RAZORPAY_QR_RESPONSE", "orderId:  " + currentOrderId  );


//        if (isCacheValid) {
//            Log.i("QRCODEPAYMENTData", "Using cached QR Code");
//            razorpayQRCreateResponse = new RazorpayQRCreateResponse();
//            razorpayQRCreateResponse.setId(cachedQrId);
//            razorpayQRCreateResponse.setImage_url(cachedImageUrl);
//
//            Log.i("RAZORPAY_QR_RESPONSE", "fetchQRCode: " + razorpayQRCreateResponse);
//            showQrCode();
//            return;
//        }

        Log.i("RAZORPAY_QR_RESPONSE", "fetchQRCode: NOT FALUDFKJ "  );


        Log.i("QRCODEPAYMENTData", "TOTAL AMOUNT :: " + getIntent().getIntExtra("paymentAmount", 0));
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("orderId",            currentOrderId);
        requestData.put("subscriptionPlanId", getIntent().getStringExtra("subscriptionPlanId"));
        requestData.put("paymentAmount",      getIntent().getIntExtra("paymentAmount", 0));
        requestData.put("transactionId",      getIntent().getStringExtra("transactionId"));
        requestData.put("coinFlow",           coinFlow);

        getQRCodeForPayment = new MyJsonObjectRequest(
                Request.Method.GET,
                CommonMethods.getUrlParam(RAZORPAY_CREATE_QR_CODE, requestData),
                null,
                jsonObject -> {
                    TypeToken<RazorpayQRCreateResponse> token = new TypeToken<RazorpayQRCreateResponse>() {};
                    razorpayQRCreateResponse = new Gson().fromJson(jsonObject.toString(), token.getType());
                    Log.i("QRCODEPAYMENTData", razorpayQRCreateResponse.toString());

                    MyApplication.saveData(PREF_QR_IMAGE_URL, razorpayQRCreateResponse.getImage_url());
                    MyApplication.saveData(PREF_QR_ID,        razorpayQRCreateResponse.getId());
                    MyApplication.saveData(PREF_QR_ORDER_ID,  currentOrderId);
                    MyApplication.saveData(PREF_QR_TIMESTAMP,   System.currentTimeMillis());

                    initiatePaymentInDb(getIntent().getIntExtra("paymentAmount", 0));
                    showQrCode();
                },
                volleyError -> {
                    Log.i("QRCODEPAYMENT", volleyError.toString());
                    Map<String, String> params = new HashMap<>();
                    params.put("Subscription_plan_Id", getIntent().getStringExtra("subscriptionPlanId"));
                    params.put("amount",               getIntent().getStringExtra("amount"));
                    params.put("action", "PaymentError reason QR Code = RAZORPAY_CREATE_QR_CODE" + volleyError);
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.Payment_failed.getActionName(), params);
                    try {
                        BranchAnalyticsHelper.logPaymentFailed(this, planId, safeParseAmount(getIntent().getStringExtra("amount")), "RAZOR PAY QR CODE", "PaymentError reason QR Code = RAZORPAY_CREATE_QR_CODE" + volleyError);
                    } catch (Exception e) {
                        Log.e("BranchEvent", "Failed to log payment_failed event", e);
                    }
                }
        );

        MyVolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(getQRCodeForPayment);
    }

    private void showQrCode() {
        GlideUrl glideUrl = new GlideUrl(razorpayQRCreateResponse.getImage_url());
        Glide.with(getApplicationContext()).load(glideUrl).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object o, Target<Drawable> target, boolean b) {
                if (e != null) {
                    Log.e("ImageLoadCheck", "Error" + e.getMessage(), e);
                }

                imageHandler = new Handler(Looper.getMainLooper());
                imageRunnable = () -> showQrCode();
                imageHandler.postDelayed(imageRunnable, 1000);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable drawable, Object o, Target<Drawable> target, DataSource dataSource, boolean b) {
                shimmerFrameLayout.setVisibility(View.GONE);
                qrCodeImage.setVisibility(View.VISIBLE);
                Log.e("ImageLoadCheck", "Success");

                return false;
            }
        }).into(qrCodeImage);
    }


    private void checkStatusOfQRPayment() {

        if (razorpayQRCreateResponse != null) {
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("qrCodeId",razorpayQRCreateResponse.getId());
            requestData.put("coinFlow", coinFlow);
            MyJsonObjectRequest checkQRPaymentStatus = new MyJsonObjectRequest(Request.Method.GET, CommonMethods.getUrlParam(RAZORPAY_CHECK_QR_PAYMENT_STATUS, requestData), null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    TypeToken<RazorpayQRPaymentResponse> token = new TypeToken<>() {
                    };
                    Gson gson = new Gson();
                    RazorpayQRPaymentResponse qrPaymentResponse = gson.fromJson(jsonObject.toString(), token.getType());
                    Log.i("QRCODEPAYMENT", "QRCodePaymentStatus" + qrPaymentResponse.toString());

                    if (qrPaymentResponse != null && qrPaymentResponse.getItems() != null && !qrPaymentResponse.getItems().isEmpty()) {
                        buyNowPostCall(qrPaymentResponse.getItems().get(0).getId());
                    }

                }
            }, volleyError -> {
                Log.i("QRCODEPAYMENT", volleyError.toString());
                Map<String,String> params = new HashMap<>();
                params.put("Subscription_plan_Id", getIntent().getStringExtra("subscriptionPlanId"));
                String amount = getIntent().getStringExtra("amount");
                params.put("amount", amount);
                params.put("action", "PaymentError reason QR Code = RAZORPAY_CREATE_QR_CODE" + volleyError);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.Payment_failed.getActionName(), params);
                try {
                    BranchAnalyticsHelper.logPaymentFailed(this, planId, safeParseAmount(amount), "RAZOR PAY QR CODE", "PaymentError reason QR Code = RAZORPAY_CHECK_QR_PAYMENT_STATUS" + volleyError);
                } catch (Exception e) {
                    Log.e("BranchEvent", "Failed to log payment_failed event", e);
                }
            });

            MyVolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(checkQRPaymentStatus);
        }
    }


    private void startCountdown(long durationInMillis) {
        uiCountDownTimer = new CountDownTimer(durationInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                countDownTimer.setText(String.format("%02d:%02d", minutes, seconds));

                int progress = (int) ((millisUntilFinished / (float) durationInMillis) * 100);
                progressIndicator.setProgress(progress);
            }

            @Override
            public void onFinish() {
                countDownTimer.setText("00:00");
                progressIndicator.setProgress(0);
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.payment_failed), Toast.LENGTH_SHORT).show();
                finish();
            }
        }.start();
    }


    private void initiatePaymentInDb(long paymentAmount) {
        String userId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
        Gson gson = new Gson();
        PaymentDetails paymentDetails = PaymentDetails
                .builder()
                .amount(paymentAmount * 100)
                .orderId(getIntent().getStringExtra("orderId"))
                .type(TransactionType.SUBSCRIPTION.name())
                .paymentMode("RAZORPAY_QR")
                .qrCodeId(razorpayQRCreateResponse.getId())
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

//                        fetchQRCode();

                    }, new VolleyGlobalErrorListener(this) {
                @Override
                public void postError(VolleyError error) {
                    Map<String,String> params = new HashMap<>();
                    params.put("Subscription_plan_Id", getIntent().getStringExtra("subscriptionPlanId"));
                    String amount = getIntent().getStringExtra("amount");
                    params.put("amount", amount);
                    params.put("action", "PaymentError reason QR Code = PAYMENT_INFO_RAZORPAY " + error.getMessage());
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.Payment_failed.getActionName(), params);
                    try {
                        BranchAnalyticsHelper.logPaymentFailed(QRCodePaymentActivity.this, planId, safeParseAmount(amount), "RAZOR PAY QR CODE", "PaymentError reason QR Code = PAYMENT_INFO_RAZORPAY " + error.getMessage());
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
        handler.removeCallbacks(buyNowPostRunnable);
        Log.d("issue", "buyNowPostCall: ");
        Map<String, Object> map = new HashMap<>();
        map.put("coinFlow", coinFlow);
        SubscriptionPostModel subscriptionPostModel = new SubscriptionPostModel();
        subscriptionPostModel.setPaymentStatus(PaymentStatus.SUCCESS.name());
        subscriptionPostModel.setInvoiceId(userSubscriptionDto.getInvoiceId());
        subscriptionPostModel.setTransactionId(transactionId);
        subscriptionPostModel.setSubscriptionId(userSubscriptionDto.getId());
        subscriptionPostModel.setOrderId(getIntent().getStringExtra("orderId"));

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(gson.toJson(subscriptionPostModel));
        } catch (JSONException e) {
            Log.d("apiresultJson", e.getMessage());
            return;
        }

        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.PUT,
                CommonMethods.getUrlParam(USER_SUBSCRIPTION, map), jsonObject,
                response -> {
                    try {
                        boolean success = (boolean) response.get("success");
                        if (success) {
                            // Facebook logging
                            facebookLogPurchaseEvent(subscriptionPostModel.getSubscriptionId(), getIntent().getLongExtra("paymentAmount", 0));
                            try {
                                BranchAnalyticsHelper.logPurchase(this, planId, transactionId, getIntent().getLongExtra("paymentAmount", 0), "RAZOR PAY QR CODE");
                            } catch (Exception e) {
                                Log.e("BranchEvent", "Failed to log PURCHASE event", e);
                            }

                            if (!MyApplication.getFirebaseRemoteConfig().getBoolean(getString(R.string.show_subscription_benefits))) {
                                Toast.makeText(this, "Congratulations! You have subscribed", Toast.LENGTH_SHORT).show();
                            }

                            int paymentAmount = getIntent().getIntExtra("paymentAmount", 0);

                            Map<String, String> params = new HashMap<>();
                            params.put("Subscription_plan_Id", planId);
                            params.put("amount", String.valueOf(paymentAmount));
                            params.put("modeOfPayment", "RAZOR PAY SDK");
                            if (redirectionSource != null) {
                                params.put("navigation_from", redirectionSource.getFirebaseName());
                            }

                            String eventName = "payment_successful_" + paymentAmount;
                            MyApplication.logFirebaseBundleCreation(eventName, params);

                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.Payment_successful.getActionName(), params);
                            MyApplication.purchaseFirebaseEvent(planId, transactionId, Double.parseDouble(String.valueOf(getIntent().getIntExtra("paymentAmount", 0))), "RAZOR PAY QR CODE", redirectionSource != null ? redirectionSource.getFirebaseName() : "unknown");

                            new Handler().postDelayed(() -> {
                                if (MyApplication.getFirebaseRemoteConfig().getBoolean(getString(R.string.show_subscription_benefits)) &&
                                        StringUtils.isEmpty(getIntent().getStringExtra("completedVideoUrl"))) {
                                    try {
                                        Type listType = new TypeToken<List<SubscriptionBenefitsModel>>() {}.getType();
                                        ArrayList<SubscriptionBenefitsModel> list = gson.fromJson(String.valueOf(response.get("data")), listType);
                                        Intent intent = new Intent(QRCodePaymentActivity.this, SubscriptionCongatulationsActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        intent.putParcelableArrayListExtra("benefits", list);
                                        intent.putExtra("congratulationHeader", response.has("congratulationHeader") && !response.isNull("congratulationHeader") ? response.getString("congratulationHeader") : "");
                                        intent.putExtra("congratulationSubHeader", response.has("congratulationSubHeader") && !response.isNull("congratulationSubHeader") ? response.getString("congratulationSubHeader") : "");
                                        intent.putExtra("planId", planId);
                                        startActivity(intent);
                                        finish();
                                    } catch (JSONException e) {
                                        Log.d("Exception4", e.getMessage());
                                        Toast.makeText(this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                                    }
                                } else if(StringUtils.isNotEmpty(getIntent().getStringExtra("completedVideoUrl"))) {
                                    Intent intent = new Intent(QRCodePaymentActivity.this, FeatureInfoVideoActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.putExtra("hide_bottom_layout",true);
                                    intent.putExtra("videoUrl",getIntent().getStringExtra("completedVideoUrl"));
                                    intent.putExtra(FeatureInfoVideoActivity.bundleNavigationFrom, FeatureInfoVideoActivity.NavigationFromEnum.QR_SCREEN.name());
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Intent intent = new Intent(QRCodePaymentActivity.this, HomeScreen.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            }, 2000);

                        } else {
                            Toast.makeText(this, "Sorry we faced some technical problem, Please contact us.", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        Toast.makeText(this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    }
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                Map<String, String> params = new HashMap<>();
                params.put("Subscription_plan_Id", getIntent().getStringExtra("subscriptionPlanId"));
                params.put("amount", getIntent().getStringExtra("amount"));
                params.put("action", "PaymentError reason QR Code = USER_SUBSCRIPTION " + error.getMessage());
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.Payment_failed.getActionName(), params);
                try {
                    BranchAnalyticsHelper.logPaymentFailed(QRCodePaymentActivity.this, planId, safeParseAmount(getIntent().getStringExtra("amount")), "RAZOR PAY QR CODE", "PaymentError reason QR Code = USER_SUBSCRIPTION " + error.getMessage());
                } catch (Exception e) {
                    Log.e("BranchEvent", "Failed to log payment_failed event", e);
                }
                Toast.makeText(getApplicationContext(), "Sorry we faced some technical problem, Please contact us.", Toast.LENGTH_SHORT).show();
            }
        });
        MyVolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(myJsonObjectRequest2);
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
