package arivihan.technologies.doubtbuzzter2.helper;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.USER_SUBSCRIPTION;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.VolleyError;
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
import arivihan.technologies.doubtbuzzter2.RazorpayPaymentGatewayActivity;
import arivihan.technologies.doubtbuzzter2.activity.CampaignSubscriptionActivity;
import arivihan.technologies.doubtbuzzter2.activity.HomeScreen;
import arivihan.technologies.doubtbuzzter2.activity.QRCodePaymentActivity;
import arivihan.technologies.doubtbuzzter2.activity.SubscriptionCongatulationsActivity;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.customView.textview.ColoredStrikeSpan;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.model.CouponDto;
import arivihan.technologies.doubtbuzzter2.model.PaymentStatus;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionBenefitsModel;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionPlans;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionPostModel;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionPreModel;
import arivihan.technologies.doubtbuzzter2.model.UserSubscriptionDto;
import arivihan.technologies.doubtbuzzter2.model.asat.ASATScreenProcessInfo;
import arivihan.technologies.doubtbuzzter2.model.firebase.rdb.SubscriptionPopUpModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class SubscriptionHelper {
    private static final String TAG = SubscriptionHelper.class.getSimpleName();

    public void startPayment(Context context, SubscriptionPlans selectedSubscription, SubscriptionPopUpModel subscriptionPopUpModel) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.subscription_payment_option_dialog_layout);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
        dialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(context, R.drawable.bachground_custom_dialogu_typeactivity));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.show();

        dialog.findViewById(R.id.close_btn).setOnClickListener(dv -> {
            dialog.dismiss();
        });

        dialog.findViewById(R.id.pay_with_qr_btn).setOnClickListener(dv -> {
//            isPayingThroughQRCode = true;
            dialog.dismiss();
            try {
                buyNowPreCall(context, selectedSubscription, true);

                Map<String, String> eventParams = getEventParameters(subscriptionPopUpModel);
                eventParams.put("selectedPaymentMethod", "QR");
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.auto_subscription_pop_up_qr.getActionName(), eventParams);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        dialog.findViewById(R.id.pay_with_payment_gateway_btn).setOnClickListener(dv -> {
//            isPayingThroughQRCode = false;
            dialog.dismiss();
            try {
                buyNowPreCall(context, selectedSubscription, false);

                Map<String, String> eventParams = getEventParameters(subscriptionPopUpModel);
                eventParams.put("selectedPaymentMethod", "RazorPay");
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.auto_subscription_pop_up_razorpay.getActionName(), eventParams);
            } catch (Exception e) {
                Toast.makeText(context, "An error occurred from our end, please try again later", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void buyNowPreCall(Context context, SubscriptionPlans selectedSubscription, boolean isPayingThroughQRCode) throws JSONException {
        if (selectedSubscription != null) {
            Map<String, Object> map = new HashMap<>();
            SubscriptionPreModel subscriptionPreModel = new SubscriptionPreModel();
            subscriptionPreModel.setCoupon(CouponDto.builder().code(selectedSubscription.getCoupon()).build());
            subscriptionPreModel.setUserId(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
            subscriptionPreModel.setSubscriptionPlanId(selectedSubscription.getPlanId());
            subscriptionPreModel.setOrderId(CampaignSubscriptionActivity.getAlphaNumericString(8));
            subscriptionPreModel.setTotalAmount(Long.parseLong(selectedSubscription.getFinalPrice()));
            subscriptionPreModel.setRequestId(selectedSubscription.getRequestId());
            subscriptionPreModel.setRedirectionFrom(RedirectionSource.LOCK_POPUP_JOIN.getFormattedValue());
            Log.d("SubscriptionSourceTrack", "Source: " + subscriptionPreModel.getRedirectionFrom() + " | Plan: " + selectedSubscription.getPlanId() + " (SubscriptionHelper)");
            Gson gson = new Gson();
            MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.POST,
                    CommonMethods.getUrlParam(USER_SUBSCRIPTION, map), new JSONObject(gson.toJson(subscriptionPreModel)),
                    response -> {
                        TypeToken<UserSubscriptionDto> token = new TypeToken<UserSubscriptionDto>() {
                        };
                        UserSubscriptionDto userSubscriptionDto;
                        try {
                            userSubscriptionDto = gson.fromJson(response.getJSONObject("data").toString(), token.getType());

                            if (Integer.parseInt(selectedSubscription.getFinalPrice()) == 0) {
                                try {
                                    buyNowPostCall(context, selectedSubscription, CampaignSubscriptionActivity.getAlphaNumericString(8), userSubscriptionDto);
                                } catch (JSONException e) {
                                    Log.e("Error in ", e.getMessage(), e);
                                }
                            } else {
                                if (isPayingThroughQRCode) {
                                    if (selectedSubscription != null) {
                                        Intent intent = new Intent(context, QRCodePaymentActivity.class);
                                        intent.putExtra("orderId", userSubscriptionDto.getOrderId());
                                        intent.putExtra("subscriptionPlanId", selectedSubscription.getPlanId());
                                        intent.putExtra("transactionId", "");
                                        intent.putExtra("costPrice", selectedSubscription.getCostPrice());
                                        intent.putExtra("planName", selectedSubscription.getName());
                                        intent.putExtra("userSubscriptionDto", userSubscriptionDto);
                                        intent.putExtra("paymentAmount", Integer.parseInt(selectedSubscription.getFinalPrice()));
                                        context.startActivity(intent);
                                    }
                                } else {
                                    doPayment(context, selectedSubscription, Integer.parseInt(selectedSubscription.getFinalPrice()), userSubscriptionDto.getOrderId(), userSubscriptionDto);
                                }
                            }
                        } catch (Exception e) {
                            Toast.makeText(context, context.getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                        }
                    }
                    , new VolleyGlobalErrorListener(context) {
                @Override
                public void postError(VolleyError error) {
                    Log.i(TAG, error.getMessage());
                    Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            MyVolleySingleton.getInstance(context).addToRequestQueue(myJsonObjectRequest2);
        } else {
            Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
        }
    }

    private void buyNowPostCall(Context context, SubscriptionPlans selectedSubscription, String transactionId, UserSubscriptionDto userSubscriptionDto) throws JSONException {
        Map<String, Object> map = new HashMap<>();
        SubscriptionPostModel subscriptionPostModel = new SubscriptionPostModel();
        subscriptionPostModel.setPaymentStatus(PaymentStatus.SUCCESS.name());
        subscriptionPostModel.setInvoiceId(userSubscriptionDto.getInvoiceId());
        subscriptionPostModel.setTransactionId(transactionId);
        subscriptionPostModel.setSubscriptionId(userSubscriptionDto.getId());
        subscriptionPostModel.setRequestId(selectedSubscription.getRequestId());

        Gson gson = new Gson();
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.PUT,
                CommonMethods.getUrlParam(USER_SUBSCRIPTION, map), new JSONObject(gson.toJson(subscriptionPostModel)),
                response -> {
                    try {
                        boolean success = (boolean) response.get("success");
                        if (success) {
                            new Handler().postDelayed(() -> {
                                if (MyApplication.getFirebaseRemoteConfig().getBoolean(context.getString(R.string.show_subscription_benefits))) {
                                    try {
                                        Type listType = new TypeToken<List<SubscriptionBenefitsModel>>() {
                                        }.getType();
                                        ArrayList<SubscriptionBenefitsModel> list = gson.fromJson(String.valueOf(response.get("data")), listType);
                                        Intent intent = new Intent(context, SubscriptionCongatulationsActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        intent.putParcelableArrayListExtra("benefits", list);
                                        intent.putExtra("congratulationHeader", (response.has("congratulationHeader") && !response.isNull("congratulationHeader")) ? response.getString("congratulationHeader") : "");
                                        intent.putExtra("congratulationSubHeader", (response.has("congratulationSubHeader") && !response.isNull("congratulationSubHeader")) ? response.getString("congratulationSubHeader") : "");
                                        intent.putExtra("planId", selectedSubscription.getPlanId());
                                        context.startActivity(intent);
                                    } catch (JSONException e) {
                                        Log.d(TAG, e.getMessage());
                                        Toast.makeText(context, context.getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Intent intent = new Intent(context, HomeScreen.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    context.startActivity(intent);
                                }
                            }, 1000);

//                            new Handler().postDelayed(() -> this.finish(), 1000);
                        } else {
                            Toast.makeText(context, "Sorry we faced some technical problem, contact us.", Toast.LENGTH_SHORT).show();
//                            CommonMethods.showNegativeSnackbar(rl_main_layout, "Sorry we faced some technical problem, contact us.");
                        }
                    } catch (Exception e) {
                        Toast.makeText(context, context.getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    }
                }, new VolleyGlobalErrorListener(context) {
            @Override
            public void postError(VolleyError error) {
                Toast.makeText(context, error.getMessage().toString(), Toast.LENGTH_SHORT).show();
//                CommonMethods.showNegativeSnackbar(rl_main_layout, error.getMessage().toString());
            }
        });
        MyVolleySingleton.getInstance(context).addToRequestQueue(myJsonObjectRequest2);
    }

    void doPayment(Context context, SubscriptionPlans selectedSubscription, int amount, String rzOrderId, UserSubscriptionDto userSubscriptionDto) {
        if (selectedSubscription != null) {
            Intent paymentGatewayIntent = new Intent(context, RazorpayPaymentGatewayActivity.class);
            paymentGatewayIntent.putExtra("planId", selectedSubscription.getPlanId());
            paymentGatewayIntent.putExtra("orderId", rzOrderId);
            paymentGatewayIntent.putExtra("amount", amount + "");
            paymentGatewayIntent.putExtra("userSubscriptionDto", userSubscriptionDto);
            context.startActivity(paymentGatewayIntent);
        }
    }

    public static String getCouponCode(SubscriptionPopUpModel subscriptionPopUpModel) {
        if (subscriptionPopUpModel != null && StringUtils.isNotEmpty(subscriptionPopUpModel.getCouponCode()) && !subscriptionPopUpModel.getCouponCode().equalsIgnoreCase("null")) {
            return subscriptionPopUpModel.getCouponCode();
        }
        return null;
    }

    public static CouponDto getCoupon(SubscriptionPopUpModel subscriptionPopUpModel) {
        if (subscriptionPopUpModel != null && StringUtils.isNotEmpty(subscriptionPopUpModel.getCouponCode()) && !subscriptionPopUpModel.getCouponCode().equalsIgnoreCase("null")) {
            return CouponDto.builder().code(subscriptionPopUpModel.getCouponCode()).build();
        }
        return null;
    }

    public static SpannableString getStrikeThroughText(Context context, String text) {
        try {
            String priceText = String.format(context.getString(R.string.price_format),
                    (text != null)
                            ? text
                            : "");

            SpannableString ss = new SpannableString(priceText);
            ss.setSpan(
                    new ColoredStrikeSpan(ContextCompat.getColor(context, R.color.arivihan_secondary_color)),
                    0,
                    ss.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            return ss;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Map<String, String> getEventParameters(SubscriptionPopUpModel subscriptionPopUpModel) {
        Map<String, String> eventParams = new HashMap<>();
        try {
            eventParams.put("time", String.valueOf(System.currentTimeMillis()));
            eventParams.put("requestId", StringUtils.isNotEmpty(subscriptionPopUpModel.getRequestId()) ? subscriptionPopUpModel.getRequestId() : "");
            eventParams.put("title", StringUtils.isNotEmpty(subscriptionPopUpModel.getTitle()) ? subscriptionPopUpModel.getTitle() : "");
            eventParams.put("upiId", StringUtils.isNotEmpty(subscriptionPopUpModel.getUpiId()) ? subscriptionPopUpModel.getUpiId() : "");
            eventParams.put("finalAmount", StringUtils.isNotEmpty(String.valueOf(subscriptionPopUpModel.getFinalAmount())) ? String.valueOf(subscriptionPopUpModel.getFinalAmount()) : "");
            eventParams.put("planId", StringUtils.isNotEmpty(subscriptionPopUpModel.getPlanId()) ? subscriptionPopUpModel.getPlanId() : "");
            eventParams.put("planName", StringUtils.isNotEmpty(subscriptionPopUpModel.getPlanName()) ? subscriptionPopUpModel.getPlanName() : "");
            eventParams.put("price", StringUtils.isNotEmpty(String.valueOf(subscriptionPopUpModel.getPrice())) ? String.valueOf(subscriptionPopUpModel.getPrice()) : "");
            eventParams.put("mentorName", StringUtils.isNotEmpty(subscriptionPopUpModel.getMentorName()) ? subscriptionPopUpModel.getMentorName() : "");
            eventParams.put("mentorId", StringUtils.isNotEmpty(subscriptionPopUpModel.getMentorId()) ? subscriptionPopUpModel.getMentorId() : "");
            eventParams.put("expiryDuration", String.valueOf(subscriptionPopUpModel.getExpiryDuration() > 0 ? subscriptionPopUpModel.getExpiryDuration() : 0));
            eventParams.put(PreferenceKeys.USER_ID.getKey(), StringUtils.isNotEmpty(subscriptionPopUpModel.getUserId()) ? subscriptionPopUpModel.getUserId() : "");
            eventParams.put("paymentReceivedBy", StringUtils.isNotEmpty(subscriptionPopUpModel.getPaymentReceivedBy()) ? subscriptionPopUpModel.getPaymentReceivedBy() : "");
            eventParams.put("paymentReceivedByMetricsUserId", StringUtils.isNotEmpty(subscriptionPopUpModel.getPaymentReceivedByMetricsUserId()) ? subscriptionPopUpModel.getPaymentReceivedByMetricsUserId() : "");
            eventParams.put("paymentReceivedByUserName", StringUtils.isNotEmpty(subscriptionPopUpModel.getPaymentReceivedByUserName()) ? subscriptionPopUpModel.getPaymentReceivedByUserName() : "");
            eventParams.put("paymentReceivedByPhone", StringUtils.isNotEmpty(subscriptionPopUpModel.getPaymentReceivedByPhone()) ? subscriptionPopUpModel.getPaymentReceivedByPhone() : "");
            eventParams.put("paymentRaisedFrom", StringUtils.isNotEmpty(subscriptionPopUpModel.getPaymentRaisedFrom()) ? subscriptionPopUpModel.getPaymentRaisedFrom() : "");
            eventParams.put("couponCode", StringUtils.isNotEmpty(subscriptionPopUpModel.getCouponCode()) ? subscriptionPopUpModel.getCouponCode() : "");
        } catch (Exception ignored) {
        }
        return eventParams;
    }
}
