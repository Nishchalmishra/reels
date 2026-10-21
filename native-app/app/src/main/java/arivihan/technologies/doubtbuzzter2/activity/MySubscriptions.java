package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.CHECK_PAYMENT_STATUS;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.USER_SUBSCRIPTION;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.USER_SUBSCRIPTION_BENEFITS;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.MySubscriptionAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.PlayStoreDialog;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.model.ApiResponse;
import arivihan.technologies.doubtbuzzter2.model.PaymentStatus;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionBenefitsModel;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionStatus;
import arivihan.technologies.doubtbuzzter2.model.TransactionModel;
import arivihan.technologies.doubtbuzzter2.model.UserSubscriptionDto;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class MySubscriptions extends BaseActivity implements MySubscriptionAdapter.ItemOnClick {

    private MaterialCardView allTransactions;
    private TextView allTransactionsText;
    private MaterialCardView failedTransactions;
    private TextView failedTransactionsText;
    private RecyclerView transactions_rv;
    private CardView help_card;
    private CardView susbcription_plan_card;
    private ArrayList<TransactionModel> transactionModels = new ArrayList<>();
    private ArrayList<TransactionModel> failedTransactionModels = new ArrayList<>();
    private ArrayList<TransactionModel> adapterModels = new ArrayList<>();
    private Gson gson = new Gson();
    private MySubscriptionAdapter mySubscriptionAdapter;
    private PlayStoreDialog playStoreDialog;
    private LinearLayout back_button_layout, bottom_layout, my_subscriptions;
    private ShimmerFrameLayout shimmer_layout;
    private ProgressBar progress_bar;
    private LinearLayout no_subscriptions_layout;
    private TextView no_transaction_text;
    private Dialog checkPaymentStatusDialog;
    TextView checkPaymentStatusDialogTitle, checkPaymentStatusDialogText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_subscriprions);
        no_transaction_text = findViewById(R.id.no_transaction_text);
        no_subscriptions_layout = findViewById(R.id.no_subscriptions_layout);
        progress_bar = findViewById(R.id.progress_bar);
        my_subscriptions = findViewById(R.id.my_subscriptions);
        bottom_layout = findViewById(R.id.bottom_layout);
        shimmer_layout = findViewById(R.id.shimmer_layout);
        back_button_layout = findViewById(R.id.back_button_layout);
        allTransactionsText = findViewById(R.id.all_transactions_text);
        failedTransactionsText = findViewById(R.id.failed_transactions_text);
        allTransactions = findViewById(R.id.all_transactions);
        failedTransactions = findViewById(R.id.failed_transactions);
        transactions_rv = findViewById(R.id.transactions_rv);
        help_card = findViewById(R.id.help_card);
        susbcription_plan_card = findViewById(R.id.susbcription_plan_card);
        mySubscriptionAdapter = new MySubscriptionAdapter(adapterModels, this, this);
        transactions_rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        transactions_rv.setAdapter(mySubscriptionAdapter);
        help_card.setOnClickListener(view -> {
            try {
                String url = "https://api.whatsapp.com/send?phone=" + MyApplication.getPhoneNumberWith91();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setPackage("com.whatsapp");
                intent.setData(Uri.parse(url));
                startActivity(intent);

                Bundle contactBundle = new Bundle();
                contactBundle.putString("type", "Whatsapp");
                MyApplication.logFirebaseEvent("click_profile_contact", contactBundle);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, R.string.whatsapp_not_found, Toast.LENGTH_SHORT).show();
            }
        });
        susbcription_plan_card.setOnClickListener(view -> {
            SubscriptionNavigationHelper.navigateToSubscriptionPage(this, RedirectionSource.MY_SUBSCRIPTIONS_JOIN);
        });
        allTransactions.setOnClickListener(view -> handleTabClick(true));
        failedTransactionsText.setOnClickListener(view -> handleTabClick(false));
        back_button_layout.setOnClickListener(view -> {
            getOnBackPressedDispatcher().onBackPressed();
        });
        shimmer_layout.startShimmer();
        fetchTransactionHistory();
        hideSubscriptionPlanCard();


    }

    private void hideSubscriptionPlanCard() {
        if (getIntent().getBooleanExtra("hideSubscriptionPlan",false)) {
            susbcription_plan_card.setVisibility(View.GONE);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) help_card.getLayoutParams();
            params.weight = 1f;
            params.width = 0;
            help_card.setLayoutParams(params);
        }
    }


//    private void listenForPlanStatus(String id, String subscriptionPlanId) {
//        databaseReference = FirebaseDatabase.getInstance().getReference("users/subscription_plans_status/"
//                + MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()) + "/" + id);
//        ValueEventListener valueEventListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    String status = snapshot.child("status").getValue(String.class);
//                    if (status != null && !status.equals("pending")) {
//                        dismissDialog();
//                        fetchTransactionHistory();
//                        fetchBenefits(subscriptionPlanId);
//                    }
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e("FirebaseError", "Database error: " + error.getMessage());
//            }
//        };
//
//        databaseReference.addValueEventListener(valueEventListener);
//    }


    private void dismissDialog() {
        if (checkPaymentStatusDialog != null && checkPaymentStatusDialog.isShowing()) {
            checkPaymentStatusDialog.dismiss();
            checkPaymentStatusDialog = null;
        }
    }

    private void handleTabClick(boolean isAllActive) {
        if (isAllActive) {
            allTransactions.setCardBackgroundColor(getColor(R.color.subscription_blue));
            allTransactions.setStrokeWidth(0);
            allTransactionsText.setTextColor(getColor(R.color.white));
            failedTransactions.setCardBackgroundColor(getColor(R.color.white));
            failedTransactions.setStrokeWidth(1);
            failedTransactions.setStrokeColor(ContextCompat.getColor(this, R.color.mock_card_boundary));
            failedTransactionsText.setTextColor(ContextCompat.getColor(this, R.color.check_text_color));
            adapterModels.clear();
            adapterModels.addAll(transactionModels);
            mySubscriptionAdapter.notifyDataSetChanged();
            if (CollectionUtils.isEmpty(adapterModels)) {
                transactions_rv.setVisibility(View.GONE);
                no_subscriptions_layout.setVisibility(View.VISIBLE);
                no_transaction_text.setText(getString(R.string.no_susbcriptions_purchased));
            } else {
                transactions_rv.setVisibility(View.VISIBLE);
                no_subscriptions_layout.setVisibility(View.GONE);
            }
        } else {
            failedTransactions.setCardBackgroundColor(getColor(R.color.subscription_blue));
            failedTransactions.setStrokeWidth(0);
            failedTransactionsText.setTextColor(getColor(R.color.white));
            allTransactions.setCardBackgroundColor(getColor(R.color.white));
            allTransactions.setStrokeWidth(1);
            allTransactions.setStrokeColor(ContextCompat.getColor(this, R.color.mock_card_boundary));
            allTransactionsText.setTextColor(ContextCompat.getColor(this, R.color.check_text_color));
            adapterModels.clear();
            adapterModels.addAll(failedTransactionModels);
            mySubscriptionAdapter.notifyDataSetChanged();
            if (CollectionUtils.isEmpty(adapterModels)) {
                transactions_rv.setVisibility(View.GONE);
                no_subscriptions_layout.setVisibility(View.VISIBLE);
                no_transaction_text.setText(getString(R.string.no_failed_Transactions));
            } else {
                transactions_rv.setVisibility(View.VISIBLE);
                no_subscriptions_layout.setVisibility(View.GONE);
            }
        }
    }

    private void fetchTransactionHistory() {
        Map<String, Object> map = new HashMap<>();
        map.put("hide_basic_plan",true);
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(USER_SUBSCRIPTION, map), null,
                response -> {
                    TypeToken<List<UserSubscriptionDto>> token = new TypeToken<List<UserSubscriptionDto>>() {
                    };
                    try {
                        List<UserSubscriptionDto> datas = gson.fromJson(response.getJSONArray("data").toString(), token.getType());

                        transactionModels.clear();
                        failedTransactionModels.clear();
                        adapterModels.clear();

                        if (!CollectionUtils.isEmpty(datas)) {
                            for (UserSubscriptionDto data : datas) {
                                TransactionModel tm = new TransactionModel();
                                tm.setId(data.getId());
                                tm.setTitle(data.getTitle());
                                tm.setInvoiceDownloadUrl(data.getInvoiceDownloadUrl());
                                tm.setOnGoing(data.getStatus() == SubscriptionStatus.ACTIVE);
                                tm.setSubscriptionStatus(data.getStatus());
                                tm.setPrice(CommonMethods.getRupeeText((int) data.getLastPayement()));
                                tm.setOrderNo("Transaction ID : " + data.getLastTrasactionId());
                                tm.setDate(data.getExpiryDate());
                                tm.setSubscriptionPlanLevelValue(data.getSubscriptionPlanLevelValue());
                                tm.setSubscriptionPlanLevel(data.getSubscriptionPlanLevel());
                                tm.setPaymentStatus(data.getPaymentStatus().name());
                                tm.setDuration(data.getDuration().getValue());
                                tm.setSubscriptionPlanId(data.getSubscriptionPlanId());
                                Log.d("transactionPayment", "fetchTransactionHistory: " + tm);

                                if (tm.getPaymentStatus().equals(PaymentStatus.FAILED.name())) {
                                    failedTransactionModels.add(tm);
                                } else {
                                    transactionModels.add(tm);
                                }
                            }
                            adapterModels.addAll(transactionModels);
                            mySubscriptionAdapter.notifyDataSetChanged();

                            my_subscriptions.setVisibility(View.VISIBLE);
                            shimmer_layout.stopShimmer();
                            shimmer_layout.setVisibility(View.GONE);

                            if (adapterModels.isEmpty()) {
                                transactions_rv.setVisibility(View.GONE);
                                no_subscriptions_layout.setVisibility(View.VISIBLE);
                            } else {
                                no_subscriptions_layout.setVisibility(View.GONE);
                                bottom_layout.setVisibility(View.VISIBLE);
                            }
                        } else {
                            my_subscriptions.setVisibility(View.VISIBLE);
                            transactions_rv.setVisibility(View.GONE);
                            no_subscriptions_layout.setVisibility(View.VISIBLE);
                            shimmer_layout.stopShimmer();
                            shimmer_layout.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        Log.d("fetchTransactionHistory:", e.getMessage());
                    }
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                shimmer_layout.stopShimmer();
                shimmer_layout.setVisibility(View.GONE);
                Toast.makeText(MyApplication.getInstance().getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
    }


    @Override
    public void buyAgainClick(TransactionModel transactionModel) {
//        Intent intent = new Intent(this, CampaignSubscriptionActivity.class);
////        intent.putExtra("plan_level", transactionModel.getSubscriptionPlanLevel());
////        intent.putExtra("fromMySubscription", true);
////        intent.putExtra("duration", transactionModel.getDuration());
////        intent.putExtra("boardSelected", transactionModel.getSubscriptionPlanId().toLowerCase().contains(PreferenceKeys.BOARD.getKey()));
////        intent.putExtra("comboSelected",
////                transactionModel.getSubscriptionPlanId().toLowerCase().contains(PreferenceKeys.BOARD.getKey()) &&
////                        (transactionModel.getSubscriptionPlanId().toLowerCase().contains("jee") ||
////                                transactionModel.getSubscriptionPlanId().toLowerCase().contains("neet")));
//        startActivity(intent);
        SubscriptionNavigationHelper.navigateToSubscriptionPage(this, RedirectionSource.MY_SUBSCRIPTIONS_JOIN);
    }

    @Override
    public void courseBenefitsClick(TransactionModel transactionModel) {
        if (transactionModel.getPaymentStatus().equals(PaymentStatus.FAILED.name()) ||
                transactionModel.getPaymentStatus().equals(PaymentStatus.PENDING.name())) {
            openWhatsApp();
        } else {
            fetchBenefits(transactionModel.getSubscriptionPlanId());
        }
    }

    private void openWhatsApp() {
        try {
            String url = "https://api.whatsapp.com/send?phone=" + MyApplication.getPhoneNumberWith91();
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setPackage("com.whatsapp");
            intent.setData(uri);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No Whatsapp Found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void invoiceDownloadClick(String url) {
        Log.i("INVOICEURL", "INVOICE ::" + url);
        try {
            Intent target = new Intent(Intent.ACTION_VIEW);
            target.setDataAndType(Uri.parse(url), "application/pdf");
            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            if (target.resolveActivity(this.getPackageManager()) != null) {
                Intent intent = Intent.createChooser(target, "Open File");
                startActivity(intent);
            } else {
                playStoreDialog = new PlayStoreDialog(this);
                playStoreDialog.showPlayStoreDialog();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error occurred", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void showPaymentCheckingDialog(String subscriptionId, String subscriptionPlanId) {
        {
            if (!isFinishing() && !isDestroyed()) {
                try {
                    checkPaymentStatusDialog = new Dialog(this);
                    checkPaymentStatusDialog.setContentView(R.layout.recheck_payment_dialog);
                    checkPaymentStatusDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
                    checkPaymentStatusDialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(this, R.drawable.bachground_custom_dialogu_typeactivity));
                    checkPaymentStatusDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    checkPaymentStatusDialog.setCancelable(false);
                    checkPaymentStatusDialog.show();

                    checkPaymentStatus(subscriptionId, subscriptionPlanId);

                    checkPaymentStatusDialogTitle = checkPaymentStatusDialog.findViewById(R.id.dialog_title);
                    checkPaymentStatusDialogText = checkPaymentStatusDialog.findViewById(R.id.dialog_message);
                    checkPaymentStatusDialogTitle.setText(getString(R.string.verifying_your_payment));

                    checkPaymentStatusDialogText.setText(MyApplication.getFirebaseRemoteConfig().getString(getString(R.string.check_payment_dialog_text) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey())));
                    ImageView closeDialog = checkPaymentStatusDialog.findViewById(R.id.dialog_close_btn);

                    closeDialog.setOnClickListener(v -> {
                        dismissDialog();
                    });
                } catch (Exception e) {
                    Log.d("setAnalysingDialog: ", e.getMessage());
                }
            }
        }

    }

    private void checkPaymentStatus(String subscriptionId, String subscriptionPlanId) {
        Log.d("checkPaymentStatus", "API STATUS :Api call");
        Map<String, Object> map = new HashMap<>();
        map.put("planId", subscriptionId);
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(CHECK_PAYMENT_STATUS, map), null, response -> {
            ApiResponse apiResponse = gson.fromJson(response.toString(), ApiResponse.class);
            Log.d("checkPaymentStatus", "api response: " + apiResponse);
            String paymentStatus = apiResponse.getMessage();
            Log.d("checkPaymentStatus", "checkPaymentStatus: " + paymentStatus);
            if (paymentStatus.equals(PaymentStatus.SUCCESS.name())) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    dismissDialog();
                    fetchBenefits(subscriptionPlanId);
                }, 3000);
            } else if (paymentStatus.equals(PaymentStatus.PENDING.name())) {
                checkPaymentStatusDialogTitle.setText(MyApplication.getFirebaseRemoteConfig().getString
                        (getString(R.string.check_payment_dialog_title) + MyApplication.getStringData
                                (PreferenceKeys.MY_LANG.getKey())));
                checkPaymentStatusDialogText.setVisibility(View.VISIBLE);

                new Handler(Looper.getMainLooper()).postDelayed(this::dismissDialog, 3000);
            }
        }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                Log.d("checkPaymentStatusError", error.toString());
            }
        });

        MyVolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(myJsonObjectRequest);
    }

    private void fetchBenefits(String planId) {
        progress_bar.setVisibility(View.VISIBLE);
        Map<String, Object> map = new HashMap<>();
        map.put("planId", planId);
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(USER_SUBSCRIPTION_BENEFITS, map), null,
                response -> {
                    try {
                        Type listType = new TypeToken<List<SubscriptionBenefitsModel>>() {
                        }.getType();
                        ArrayList<SubscriptionBenefitsModel> list = gson.fromJson(String.valueOf(response.get("data")), listType);
                        Intent intent = new Intent(MySubscriptions.this, SubscriptionCongatulationsActivity.class);
                        intent.putParcelableArrayListExtra("benefits", list);
                        intent.putExtra("fromMySubscriptions", true);
                        intent.putExtra("congratulationHeader", (response.has("congratulationHeader") && !response.isNull("congratulationHeader")) ? response.getString("congratulationHeader") : "");
                        intent.putExtra("congratulationSubHeader", (response.has("congratulationSubHeader") && !response.isNull("congratulationSubHeader")) ? response.getString("congratulationSubHeader") : "");
                        progress_bar.setVisibility(View.GONE);
                        startActivity(intent);
                    } catch (JSONException e) {
                        Log.d("fetchTransactionHistory:", e.getMessage());
                        Toast.makeText(this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    }

                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                Toast.makeText(MyApplication.getInstance().getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (checkPaymentStatusDialog != null) {
            checkPaymentStatusDialog.dismiss();
        }
    }

}