package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.activity.HomeScreen.BroadcastStringForAction;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.BUY_SUBSCRIPTION_V2;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.USER_SUBSCRIPTION;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.text.HtmlCompat;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.RazorpayPaymentGatewayActivity;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.fragments.SpecialOfferBottomSheet;
import arivihan.technologies.doubtbuzzter2.helper.MyEmojiFilter;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.model.BuySubscriptionModel;
import arivihan.technologies.doubtbuzzter2.model.CouponDiscountType;
import arivihan.technologies.doubtbuzzter2.model.CouponDto;
import arivihan.technologies.doubtbuzzter2.model.PaymentStatus;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionPostModel;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionPreModel;
import arivihan.technologies.doubtbuzzter2.model.UserSubscriptionDto;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class BuySubscriptionActivity extends BaseActivity implements SpecialOfferBottomSheet.PaymentListener {

    private static final String TAG = BuySubscriptionActivity.class.getSimpleName();
    String planId;
    BuySubscriptionModel buySubscriptionModel;
    Gson gson;

    MaterialCardView planCard;
    TextView planName;
    TextView planCostPrice;
    TextView planFinalPrice;
    TextView changePlan;

    MaterialCardView comboCard;
    CheckBox comboCheckBox;
    TextView comboName;
    TextView comboFinalPrice;
    TextView comboSavePrice;

    EditText couponCode;
    TextView applyCode;
    TextView appliedText;
    ImageView removeCoupon;
    MaterialCardView apply;

    TextView subscriptionPrice;
    LinearLayout discountLayout;
    TextView discountPercent;
    TextView discountAmount;
    TextView totalAmount;

    MaterialCardView tncCheckBox;
    boolean tncChecked;
    TextView tnc;
    TextView privacyPolicy;

    TextView selectedPlanName;
    TextView selectedPlanCostPrice, proceedToPayButtonText;
    TextView selectedPlanFinalPrice;
    ConstraintLayout process;
    String discount;
    String deduction;
    int total = 0;
    //    int specialOfferFinalPrice=0;
    boolean dataLoaded;
    LinearLayout parentLayout, priceBottomLayout;
    IntentFilter intentFilter;
    String orderId;
    String couponId = "";
    String renewalCouponId = "";
    UserSubscriptionDto userSubscriptionDto;
    ProgressDialog progressDialog;
    LinearLayout backButton;
    SpecialOfferBottomSheet specialOfferBottomSheet;
    boolean showDialog;
    Bundle bundle = new Bundle();

    ShimmerFrameLayout shimmerFrameLayout;
    ScrollView scrollView;
    MaterialCardView paymentCard;

    boolean isPayingThroughQRCode = false;
    private LinearLayout extra_discount_layout;
    private TextView extra_discount_text;
    private String couponCodeText;
    private RedirectionSource redirectionSource;
    private String analyticsSource;
    //    private boolean bottomSheetTncClicked;
    NetworkStatusLiveData networkStatusLiveData;
    private ActivityResultLauncher<Intent> qrPaymentLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_subscription);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (showDialog) {
                    specialOfferBottomSheet.dismiss();
                    Map<String, String> params = new HashMap<>();
                    params.put("Subscription_plan_Id", planId);
                    params.put("action", "clicked_back");
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.subscription_interested_customer.getActionName(), params);
                    finish();
                } else if (buySubscriptionModel != null) {
                    showDialog = true;
                    specialOfferBottomSheet.setArguments(bundle);
                    specialOfferBottomSheet.show(getSupportFragmentManager(), "");
                }
            }
        });

//        MyApplication.saveData(PreferenceKeys.OTHER_PAYMENT.getKey(), false);
        planId = getIntent().getStringExtra("planId");
        orderId = getAlphaNumericString(8);
        extra_discount_layout = findViewById(R.id.extra_discount_layout);
        extra_discount_text = findViewById(R.id.extra_discount_text);
        parentLayout = findViewById(R.id.parent_layout);
        backButton = findViewById(R.id.back_button_layout);
        shimmerFrameLayout = findViewById(R.id.shimmer);
        shimmerFrameLayout.startShimmer();
        scrollView = findViewById(R.id.scroll_view);
        paymentCard = findViewById(R.id.payment_card);
        proceedToPayButtonText = findViewById(R.id.proceed_to_pay_btn_text);
        priceBottomLayout = findViewById(R.id.price_bottom_layout);
        setCouponLayout();

        Object sourceExtra = getIntent().getSerializableExtra(SubscriptionNavigationHelper.EXTRA_SOURCE);
        redirectionSource = null;
        if (sourceExtra instanceof RedirectionSource) {
            redirectionSource = (RedirectionSource) sourceExtra;
            analyticsSource = redirectionSource.getFormattedValue();
        } else if (sourceExtra instanceof String) {
            analyticsSource = "REDIRECTED_FROM_" + ((String) sourceExtra).toUpperCase();
        } else {
            analyticsSource = "UNKNOWN";
        }

        backButton.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });
        intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastStringForAction);
        setPlan();
        setSelectedPlan();
        setPrice();
        setTnC();
        setBottomSheet();
        gson = new Gson();
        progressDialog = new ProgressDialog(this);
        renewalCouponId = getIntent().getStringExtra("couponCode");
        couponId = renewalCouponId;
        couponCodeText = getIntent().getStringExtra("couponCodeText");

        Uri deeplink = getIntent().getData();
        if (deeplink != null) {
            planId = deeplink.getQueryParameter("planId");
            couponId = deeplink.getQueryParameter("couponCode");
            couponCodeText = deeplink.getQueryParameter("extraText");
//            couponCode.setText(deeplink.getQueryParameter("couponCode"));
//            if (deeplink.getQueryParameter("extraText") != null) {
//                extra_discount_layout.setVisibility(View.VISIBLE);
//                extra_discount_text.setText(deeplink.getQueryParameter("extraText"));
//            }
        }

        if (couponId == null || couponCodeText == null) {
            couponId = "";
            extra_discount_layout.setVisibility(View.GONE);
        } else {
            couponCode.setText(couponId);
            extra_discount_layout.setVisibility(View.VISIBLE);
            extra_discount_text.setText(HtmlCompat.fromHtml(couponCodeText, HtmlCompat.FROM_HTML_MODE_LEGACY));
        }

        qrPaymentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        try {
                            Intent data = result.getData();
                            int paymentAmount = data.getIntExtra("paymentAmount", 0);
                            int finalAmount = data.getIntExtra("finalAmount", 0);
                            boolean isFromBottomOfferSheet = data.getBooleanExtra("isFromBottomOfferSheet", false);
                            changePaymentMode(finalAmount, isFromBottomOfferSheet);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
        );

        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            if (isOnline) {
                if (!dataLoaded) {
                    MyVolleySingleton.getInstance(BuySubscriptionActivity.this).getRequestQueue().cancelAll("PLAN_DATA_REQUEST");
                    getPlanData();
                }
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    CommonMethods.showPositiveSnackbar(parentLayout, getString(R.string.InternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
                }
            } else {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                    CommonMethods.showNegativeSnackbar(parentLayout, getString(R.string.NoInternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), false);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), true);
                }
            }
        });

        getPlanData();
    }

    @Override
    protected void onDestroy() {
        Map<String, String> params = new HashMap<>();
        params.put("Subscription_plan_Id", planId);
        params.put("action", "killed_app");
        params.put("amount", String.valueOf(totalAmount));


        MyApplication.logFirebaseBundleCreation(FirebaseEvents.subscription_interested_customer.getActionName(), params);
        MyApplication.purchaseFirebaseEvent(planId, total);

        super.onDestroy();
    }


    private void setBottomSheet() {
        specialOfferBottomSheet = new SpecialOfferBottomSheet(this);
    }



    @Override
    protected void onResume() {
        super.onResume();

//        if (MyApplication.getBoolData(PreferenceKeys.OTHER_PAYMENT.getKey())) {
//            changePaymentMode();
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void changePaymentMode(int amount, boolean isFromBottomSheet) {
        isPayingThroughQRCode = false;
        MyApplication.saveData(PreferenceKeys.OTHER_PAYMENT.getKey(), false);
        razorPayPaymentGatewayCall(amount, isFromBottomSheet);
    }

    private void setCouponLayout() {
        couponCode = findViewById(R.id.coupon_code);
        couponCode.setFilters(MyEmojiFilter.getFilter());
        applyCode = findViewById(R.id.apply_code_text);
        apply = findViewById(R.id.apply_card);
        appliedText = findViewById(R.id.applied_text);
        removeCoupon = findViewById(R.id.remove_coupon);
        removeCoupon.setOnClickListener(v -> {
            appliedText.setVisibility(View.GONE);
            apply.setVisibility(View.VISIBLE);
            removeCoupon.setVisibility(View.GONE);
        });
        apply.setOnClickListener(v -> {
            couponId = couponCode.getText().toString().trim();
            if (couponId.length() > 0) {
                progressDialog.show();
                getPlanData();
            }
        });
    }

    public void getPlanData() {
        dataLoaded = false;
        Map<String, Object> params = new HashMap<>();
        params.put("planId", planId);
        params.put("couponCode", couponId);
        params.put("showDayPlans", true);
        MyJsonObjectRequest jsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(BUY_SUBSCRIPTION_V2, params), null,
                response -> {
                    if (isFinishing() || isDestroyed()) return;
                    dataLoaded = true;
                    progressDialog.hide();
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    priceBottomLayout.setVisibility(View.VISIBLE);
                    scrollView.setVisibility(View.VISIBLE);
                    paymentCard.setVisibility(View.VISIBLE);
                    TypeToken<BuySubscriptionModel> token = new TypeToken<BuySubscriptionModel>() {
                    };
                    buySubscriptionModel = gson.fromJson(response.toString(), token.getType());
                    if (StringUtils.isEmpty(couponId) ||
                            (buySubscriptionModel.getPricingBreakdown() != null && buySubscriptionModel.getPricingBreakdown().isCouponCodeValid())) {
                        bundle.putSerializable("data", buySubscriptionModel.getSpecialOffer());
                        updatePlan();
                        updatePrice();
                        updateSelectedPlan();
                        if (StringUtils.isEmpty(renewalCouponId)) {
                            extra_discount_layout.setVisibility(View.GONE);
                        } else {
                            extra_discount_layout.setVisibility(View.VISIBLE);
                        }
                    } else {
                        couponId = "";
                        Toast.makeText(BuySubscriptionActivity.this, getString(R.string.coupon_code_is_invalid), Toast.LENGTH_LONG).show();
                    }
                }, new VolleyGlobalErrorListener(this) {

            @Override
            public void postError(VolleyError error) {
                //Toast.makeText(BuySubscriptionActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        jsonObjectRequest.setTag("PLAN_DATA_REQUEST");
        MyVolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private void setPlan() {
        planCard = findViewById(R.id.plan_card);
        planName = findViewById(R.id.plan_name);
        planCostPrice = findViewById(R.id.cost_price);
        planFinalPrice = findViewById(R.id.final_price);
        changePlan = findViewById(R.id.change_plan);
        changePlan.setOnClickListener(v -> {
            finish();
        });
    }

    private void updatePlan() {
        planName.setText(buySubscriptionModel.getSubscriptionPlan().getName());
        planCostPrice.setText(String.format(getString(R.string.price_format), buySubscriptionModel.getSubscriptionPlan().getCostPrice()));
        planCostPrice.setPaintFlags(planCostPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        planFinalPrice.setText(String.format(getString(R.string.price_format), buySubscriptionModel.getSubscriptionPlan().getFinalPrice()));
    }

    private void setPrice() {
        subscriptionPrice = findViewById(R.id.subscription_price);
        discountLayout = findViewById(R.id.discount_layout);
        discountPercent = findViewById(R.id.discount_percent);
        discountAmount = findViewById(R.id.discount_price);
        totalAmount = findViewById(R.id.total_price);
    }

    private void updatePrice() {
        subscriptionPrice.setText(String.format(getString(R.string.price_format), buySubscriptionModel.getSubscriptionPlan().getFinalPrice()));
        if (buySubscriptionModel.getPricingBreakdown().isCouponCodeValid()) {
            discount = buySubscriptionModel.getPricingBreakdown().getCouponCodeDeduction();
            deduction = buySubscriptionModel.getPricingBreakdown().getCouponCodeValue();
            discountLayout.setVisibility(View.VISIBLE);
            discountAmount.setText(String.format(getString(R.string.price_discount), discount));
            if (buySubscriptionModel.getPricingBreakdown().getCouponDiscountType().equals(CouponDiscountType.PERCENTAGE))
                discountPercent.setText(getString(R.string.coupon_code) + " (" + deduction + "%)");
            else
                discountPercent.setText(getString(R.string.coupon_code) + " (₹" + deduction + " " + getString(R.string.off) + ")");

            appliedText.setVisibility(View.VISIBLE);
            appliedText.setText(String.format(getString(R.string.applied_successfully), couponId));
            discountLayout.setVisibility(View.VISIBLE);
        }
        totalAmount.setText(String.format(getString(R.string.price_format), buySubscriptionModel.getPricingBreakdown().getTotal()));
    }

    private void setSelectedPlan() {
        selectedPlanName = findViewById(R.id.selected_plan_name);
        selectedPlanCostPrice = findViewById(R.id.selected_cost_price);
        selectedPlanFinalPrice = findViewById(R.id.selected_final_price);
        process = findViewById(R.id.process_to_pay);
        process.setOnClickListener(v -> {
            try {
                Log.d(TAG, "Proceed To Pay Parent Click: Amount");
                if (tncChecked) {
                    paymentButtonClicked(Integer.parseInt(buySubscriptionModel.getPricingBreakdown().getTotal()), false);
                } else {
                    Toast.makeText(this, getString(R.string.terms_condition), Toast.LENGTH_LONG).show();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void paymentButtonClicked(int amount, boolean isFromBottomSheet) {

//        if (tncChecked || bottomSheetTncClicked) {
        Log.d(TAG, "Payment Button Click: Amount: "+amount+", isFromBottomSheet: "+isFromBottomSheet);
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.subscription_payment_option_dialog_layout);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.show();

        dialog.findViewById(R.id.close_btn).setOnClickListener(dv -> {
            dialog.dismiss();
        });

        dialog.findViewById(R.id.pay_with_qr_btn).setOnClickListener(dv -> {
            isPayingThroughQRCode = true;
            dialog.dismiss();
            try {
//                    String totalPrice = buySubscriptionModel.getPricingBreakdown().getTotal();
//                    if (bottomSheetTncClicked) {
//                        total = specialOfferFinalPrice;
//                    } else {
//                        if (StringUtils.isNotEmpty(totalPrice)) {
//                            total = Integer.parseInt(totalPrice);
//                        }
//                    }
                total = amount;
                buyNowPreCall(amount, isFromBottomSheet);
            } catch (Exception e) {
                Log.e("Error", "paymentButtonClicked: ", e);
            }
        });

        dialog.findViewById(R.id.pay_with_payment_gateway_btn).setOnClickListener(dv -> {
            isPayingThroughQRCode = false;
            dialog.dismiss();
            razorPayPaymentGatewayCall(amount, isFromBottomSheet);
        });
//        } else {
//            Toast.makeText(this, getString(R.string.terms_condition), Toast.LENGTH_LONG).show();
//        }
    }

    private void razorPayPaymentGatewayCall(int amount, boolean isFromBottomSheet) {
        if (dataLoaded) {
            Log.d(TAG, "RazorPay Payment Gateway Click: Amount: "+amount+", isFromBottomSheet: "+isFromBottomSheet);

//            if (tncChecked || bottomSheetTncClicked) {
            progressDialog.setMessage(getString(R.string.please_dont_go_back_for_payment));
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            try {
//                    if(bottomSheetTncClicked){
//                        total = specialOfferFinalPrice;
//                    }
//                    else{
//                        total = Integer.parseInt(buySubscriptionModel.getPricingBreakdown().getTotal());
//                    }
                total = amount;
                buyNowPreCall(amount, isFromBottomSheet);
            } catch (Exception e) {
                e.printStackTrace();
                progressDialog.dismiss();
                Toast.makeText(this, "An error occurred from our end, please try again later", Toast.LENGTH_LONG).show();
            }
//            } else {
//                Toast.makeText(this, getString(R.string.please_accept_terms_and_condition_first), Toast.LENGTH_LONG).show();
//            }
        }
    }

    private void updateSelectedPlan() {
        selectedPlanName.setText(buySubscriptionModel.getSubscriptionPlan().getName());
        selectedPlanCostPrice.setText(String.format(getString(R.string.price_format), buySubscriptionModel.getPricingBreakdown().getSubscriptionPrice()));
        selectedPlanCostPrice.setPaintFlags(selectedPlanCostPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        selectedPlanFinalPrice.setText(String.format(getString(R.string.price_format), buySubscriptionModel.getPricingBreakdown().getTotal()));
    }

    private void setTnC() {
        tncCheckBox = findViewById(R.id.tnc_checkbox);
        LinearLayout tnc_checkbox_layout = findViewById(R.id.tnc_checkbox_layout);
        tnc_checkbox_layout.setOnClickListener(view -> {
            tncChecked = !tncChecked;
            if (tncChecked) {
                tncCheckBox.setBackground(getResources().getDrawable(R.drawable.checkmark));
                tncCheckBox.setStrokeWidth(0);
            } else {
                tncCheckBox.setBackground(getResources().getDrawable(R.color.white));
                tncCheckBox.setStrokeWidth(4);
                tncCheckBox.setStrokeColor(getResources().getColor(R.color.black));
            }
        });
        tncCheckBox.setClickable(false);
        tncCheckBox.setFocusable(false);
        tncCheckBox.setFocusableInTouchMode(false);
//        tncCheckBox.setOnClickListener(null);
//        tncCheckBox.setOnClickListener(v -> {
//            tncChecked = !tncChecked;
//            if (tncChecked) {
//                tncCheckBox.setBackground(getResources().getDrawable(R.drawable.checkmark));
//                tncCheckBox.setStrokeWidth(0);
//            } else {
//                tncCheckBox.setBackground(getResources().getDrawable(R.color.white));
//                tncCheckBox.setStrokeWidth(4);
//                tncCheckBox.setStrokeColor(getResources().getColor(R.color.black));
//            }
//        });
        tnc = findViewById(R.id.tnc);
        Intent i = new Intent(Intent.ACTION_VIEW);
        tnc.setOnClickListener(v -> {
            String tncUrl = "https://drive.google.com/file/d/1tdcfPX1QaYbGG8EBmzKRM86xcSsGCyPz/view";
            i.setData(Uri.parse(tncUrl));
            startActivity(i);
        });
        privacyPolicy = findViewById(R.id.privacy_policy);
        privacyPolicy.setOnClickListener(v -> {
            String privacyPolicyUrl = "https://drive.google.com/file/d/11ITZu4AIr0EUD4oHqI7dEYd6vyYS1SZJ/view";
            i.setData(Uri.parse(privacyPolicyUrl));
            startActivity(i);
        });
    }

    private void setCombo() {
        comboCard = findViewById(R.id.combo_card);
        comboCheckBox = findViewById(R.id.check_box);
        comboName = findViewById(R.id.combo_plan_name);
        comboFinalPrice = findViewById(R.id.combo_plan_price);
        comboSavePrice = findViewById(R.id.save_amount);
        comboCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setPrice();
        });
    }

    public static String getAlphaNumericString(int n) {
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {
            int index
                    = (int) (AlphaNumericString.length()
                    * Math.random());
            sb.append(AlphaNumericString
                    .charAt(index));
        }
        return sb.toString();
    }


    private void buyNowPreCall(int amount, boolean isFromBottomSheet) throws JSONException {
        Map<String, Object> map = new HashMap<>();
        SubscriptionPreModel subscriptionPreModel = new SubscriptionPreModel();
        if (StringUtils.isNoneEmpty(couponId)) {
            CouponDto couponDto = new CouponDto();
            couponDto.setCode(couponId);
            subscriptionPreModel.setCoupon(couponDto);
        }
        subscriptionPreModel.setUserId(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
        subscriptionPreModel.setSubscriptionPlanId(planId);
        subscriptionPreModel.setOrderId(orderId);
        subscriptionPreModel.setTotalAmount(total);
        subscriptionPreModel.setRedirectionFrom(analyticsSource != null ? analyticsSource : "UNKNOWN");
        Log.d("SubscriptionSourceTrack", "Source: " + subscriptionPreModel.getRedirectionFrom() + " | Plan: " + planId + " (BuySubscriptionActivity)");
        if (BuildConfig.DEBUG) Log.d(TAG, "buyNowPreCall Body: " + gson.toJson(subscriptionPreModel));
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.POST,
                CommonMethods.getUrlParam(USER_SUBSCRIPTION, map), new JSONObject(gson.toJson(subscriptionPreModel)),
                response -> {
                    if (isFinishing() || isDestroyed()) return;
                    TypeToken<UserSubscriptionDto> token = new TypeToken<UserSubscriptionDto>() {
                    };
                    try {
                        userSubscriptionDto = gson.fromJson(response.getJSONObject("data").toString(), token.getType());
                        Log.d("USERSUBSCRIPTIONRES", "total :: " + total);

                    } catch (Exception e) {
                        Map<String, String> params = new HashMap<>();
                        params.put("Subscription_plan_Id", planId);
                        params.put("action", "server_error_pre_call_1 error " + e.getMessage());
                        params.put("amount", String.valueOf(totalAmount));
                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.Payment_failed.getActionName(), params);
                        Toast.makeText(this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    }

                    if (total == 0) {
                        try {
                            buyNowPostCall(getAlphaNumericString(8));
                        } catch (JSONException e) {
                            Map<String, String> params = new HashMap<>();
                            params.put("Subscription_plan_Id", planId);
                            params.put("action", "server_error_pre_call_2 error message " + e.getMessage());
                            params.put("amount", String.valueOf(totalAmount));
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.Payment_failed.getActionName(), params);
                            Log.e("Error in ", e.getMessage(), e);
                        }
                    } else {
                        if (isPayingThroughQRCode) {
                            progressDialog.dismiss();

                            Intent intent = new Intent(BuySubscriptionActivity.this, QRCodePaymentActivity.class);
                            intent.putExtra("orderId", userSubscriptionDto.getOrderId());
                            intent.putExtra("subscriptionPlanId", planId);
                            intent.putExtra("transactionId", "");
                            intent.putExtra("costPrice", buySubscriptionModel.getSubscriptionPlan().getCostPrice());
                            intent.putExtra("planName", buySubscriptionModel.getSubscriptionPlan().getName());
                            intent.putExtra("userSubscriptionDto", userSubscriptionDto);
                            intent.putExtra(SubscriptionNavigationHelper.EXTRA_SOURCE, redirectionSource);
                            Log.d("PaymentAmount", "amount = " + total);
                            intent.putExtra("paymentAmount", total);
                            intent.putExtra("finalAmount", amount);
                            intent.putExtra("isFromBottomOfferSheet", isFromBottomSheet);

                            qrPaymentLauncher.launch(intent);
//                            startActivity(intent);

                        } else {
                            doPayment(total, userSubscriptionDto.getOrderId());
                        }
                    }

                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                Log.i("USERSUBSCRIPTIONRES", error.getMessage());
                progressDialog.dismiss();
                Toast.makeText(BuySubscriptionActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                Map<String, String> params = new HashMap<>();
                params.put("Subscription_plan_Id", planId);
                params.put("action", "server_error_pre_call error message " + error.getMessage());
                params.put("amount", String.valueOf(totalAmount));
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.Payment_failed.getActionName(), params);
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);

    }

    private void buyNowPostCall(String transactionId) throws JSONException {
        Map<String, Object> map = new HashMap<>();
        SubscriptionPostModel subscriptionPostModel = new SubscriptionPostModel();
        subscriptionPostModel.setPaymentStatus(PaymentStatus.SUCCESS.name());
        subscriptionPostModel.setInvoiceId(userSubscriptionDto.getInvoiceId());
        subscriptionPostModel.setTransactionId(transactionId);
        subscriptionPostModel.setSubscriptionId(userSubscriptionDto.getId());
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.PUT,
                CommonMethods.getUrlParam(USER_SUBSCRIPTION, map), new JSONObject(gson.toJson(subscriptionPostModel)),
                response -> {
                    try {
                        boolean success = (boolean) response.get("success");
                        if (success) {
                            new Handler().postDelayed(() -> this.finish(), 1000);
                        } else {
                            CommonMethods.showNegativeSnackbar(parentLayout, "Sorry we faced some technical problem, contact us.");
                        }
                        progressDialog.dismiss();

                    } catch (Exception e) {
                        Map<String, String> params = new HashMap<>();
                        params.put("Subscription_plan_Id", planId);
                        params.put("action", "server_error_post_call_1 error " + e.getMessage());
                        params.put("amount", String.valueOf(totalAmount));
                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.Payment_failed.getActionName(), params);
                        Toast.makeText(this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    }
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                Map<String, String> params = new HashMap<>();
                params.put("Subscription_plan_Id", planId);
                params.put("action", "server_error_post_call error " + error.getMessage());
                params.put("amount", String.valueOf(totalAmount));
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.Payment_failed.getActionName(), params);
                CommonMethods.showNegativeSnackbar(parentLayout, error.getMessage().toString(), 3);
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
    }

    void doPayment(int amount, String rzOrderId) {
        progressDialog.dismiss();
        Intent paymentGatewayIntent = new Intent(this, RazorpayPaymentGatewayActivity.class);
        paymentGatewayIntent.putExtra("planId", planId);
        paymentGatewayIntent.putExtra("orderId", rzOrderId);
        paymentGatewayIntent.putExtra(PreferenceKeys.COUPON_ID.getKey(), couponId);
        paymentGatewayIntent.putExtra("amount", amount + "");
        paymentGatewayIntent.putExtra("userSubscriptionDto", userSubscriptionDto);
        paymentGatewayIntent.putExtra(SubscriptionNavigationHelper.EXTRA_SOURCE, redirectionSource);

        Map<String, String> params = new HashMap<>();
        params.put("Subscription_plan_Id", planId);
        params.put("Amount", Integer.toString(amount));
        params.put("Coupon_ID", couponId);
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.payment_button_clicked.getActionName(), params);
        startActivity(paymentGatewayIntent);
    }

    @Override
    public void onProcessClick(int amount, boolean fromBottomSheet) {
        try {
//            specialOfferFinalPrice = amount;
            Log.d("planpoup", "onProcessClick: " + amount);
            paymentButtonClicked(amount, true);
        } catch (Exception e) {
            progressDialog.dismiss();
            Toast.makeText(this, "An error occurred from our end, please try again later", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void tncClicked() {
//        bottomSheetTncClicked=true;
    }
}
