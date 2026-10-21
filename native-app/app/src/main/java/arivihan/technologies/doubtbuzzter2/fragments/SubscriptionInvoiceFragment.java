package arivihan.technologies.doubtbuzzter2.fragments;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.FacilitySubscriptionAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.model.PlanDescription;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionDescriptionModel;
import arivihan.technologies.doubtbuzzter2.model.UserSubscriptionDto;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.USER_SUBSCRIPTION_DETAIL;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class SubscriptionInvoiceFragment extends Fragment {


    MaterialCardView cardView;
    boolean testSeries = false;
    ImageView selectedTest,unselectedTest;
    private ArrayList<String> arr = new ArrayList<>();
    ProgressBar progressBar;
    private RecyclerView recyclerViewFeature;
    int dataLoaded = 0;
    RelativeLayout coupan;
    RelativeLayout relativeData;
    CardView submit;
    RelativeLayout parentLayout;
    private String planId;
    private boolean newPlan;
    private Gson gson = new Gson();
    private RelativeLayout pointsLayout;
    private TextView arivihanPoints, duration, payment_money, course;
    private int total, costPrice, internalDiscount, couponApplied, pointsUsed;
    private TextView internl_offer, transactionId, date;
    private TextView total_amount, cost_price_text, internal_discount_text, coupon_applied_text, points_used_text, coupon_apply, paid;
    private RelativeLayout cost_price, internal_discount, coupon_applied, points_used;
    private int yourPoints;
    private CheckBox pointsCheckbox;
    private FacilitySubscriptionAdapter adapter;
    private String couponId = null;
    private UserSubscriptionDto userSubscriptionDto;
    NetworkStatusLiveData networkStatusLiveData;

    public SubscriptionInvoiceFragment() {

    }

    public SubscriptionInvoiceFragment(String planId) {
        this.planId = planId;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }



    @SuppressLint("ResourceType")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_subscription_invoice, container, false);

        getSubscriptionPlan();
        parentLayout = view.findViewById(R.id.ParentRelativeLayout);
        relativeData = view.findViewById(R.id.data);
        progressBar = view.findViewById(R.id.progress_dataLoading);
        coupan = view.findViewById(R.id.coupan);
        recyclerViewFeature = view.findViewById(R.id.features_of_subscription);
        pointsLayout= view.findViewById(R.id.arivihan_points_layout);
        arivihanPoints= view.findViewById(R.id.use_arivihan_points);
        pointsCheckbox = view.findViewById(R.id.use_points);
        yourPoints = MyApplication.getIntData(PreferenceKeys.YOUR_POINTS.getKey());
        initializeBillDetails(view);
        internl_offer = view.findViewById(R.id.internl_offer);
        transactionId = view.findViewById(R.id.transactionId);
        date = view.findViewById(R.id.date);
        recyclerViewFeature.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL ,false));
        duration = view.findViewById(R.id.duration);
        payment_money = view.findViewById(R.id.payment_money);
        course = view.findViewById(R.id.course);

        adapter = new FacilitySubscriptionAdapter(arr);
        recyclerViewFeature.setAdapter(adapter);


        networkStatusLiveData = new NetworkStatusLiveData(requireContext());
        networkStatusLiveData.observe(getViewLifecycleOwner(), isOnline -> {
            if(isOnline){
                if(dataLoaded != 1) {
                    dataLoaded = 1;
                }
                if(!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    CommonMethods.showPositiveSnackbar(parentLayout,getActivity().getString(R.string.InternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(),true);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(),false);
                }
            }
            else{
                if(!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())){
                    CommonMethods.showNegativeSnackbar(parentLayout,getActivity().getString(R.string.NoInternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(),false);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(),true);
                }
            }
        });

        if(CommonMethods.isOnline(getActivity())){
            if(dataLoaded != 1) {
                dataLoaded = 1;

            }
            if(!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(),true);
                MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(),false);
            }
        }
        else{
            if(!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())){
                CommonMethods.showNegativeSnackbar(parentLayout,getActivity().getString(R.string.NoInternetConnection));
                MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(),true);
                MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(),false);
            }
        }

        return view;
    }

    private void initializeBillDetails(View view) {
        paid = view.findViewById(R.id.paid);
        total_amount= view.findViewById(R.id.total_amount);
        cost_price_text= view.findViewById(R.id.cost_price_text);
        internal_discount_text= view.findViewById(R.id.internal_discount_text);
        coupon_applied_text= view.findViewById(R.id.coupon_applied_text);
        points_used_text= view.findViewById(R.id.points_used_text);
        cost_price= view.findViewById(R.id.cost_price);
        internal_discount= view.findViewById(R.id.internal_discount);
        coupon_applied= view.findViewById(R.id.coupon_applied);
        points_used= view.findViewById(R.id.points_used);
        coupon_apply= view.findViewById(R.id.apply);
        internal_discount_text.setText(getRupeeText(0));
        total_amount.setText(getRupeeText(0));
        cost_price_text.setText(getRupeeText(0));
        coupon_applied_text.setText(getRupeeText(0));
        points_used_text.setText(getRupeeText(0));
    }

    private String getRupeeText(int i) {
        if (i < 0) {
            return "- ₹ " + Math.abs(i) ;
        }
        return "₹ " + i;
    }


    private void getSubscriptionPlan() {
        Map<String, Object> map = new HashMap<>();
        map.put("invoiceId", planId);
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(USER_SUBSCRIPTION_DETAIL, map), null,
                response -> {
                    TypeToken<SubscriptionDescriptionModel> token = new TypeToken<SubscriptionDescriptionModel>() {};
                    try {
                        SubscriptionDescriptionModel data = gson.fromJson(response.getJSONObject("data").toString(), token.getType());
                        updateTopView(data);
                        updateFeatures(data);
                        costPrice = (int) data.getPrice();
                        if (data.getCoupon() != null) {
                            couponApplied = data.getInvoice().getDiscountApplied() == null ?
                                    0 : (int) Double.parseDouble(String.valueOf(data.getInvoice().getDiscountApplied()));
                            coupon_apply.setText(data.getCoupon().getCode() + " coupon applied");
                        } else {
                            coupan.setVisibility(View.GONE);
                        }
                        course.setText(data.getSubtypeName());
                        paid.setText(data.getInvoice().getPaymentStatus().name());
                        pointsUsed = data.getArivihanPointsUsed();
                        internalDiscount = data.getCouponDiscount();
                        internl_offer.setText(getRupeeText(internalDiscount) + " Off");
                        if (StringUtils.isNotEmpty(data.getInvoice().getTransactionId())) {
                            transactionId.setText("Transaction ID : " + data.getInvoice().getTransactionId());
                        } else {
                            transactionId.setText("Transaction ID : NA");
                        }
                        date.setText("Date : " + data.getInvoice().getPurchasedDate());
                        arivihanPoints.setText("XX arivihan points used".replace("XX", ""+ pointsUsed));
                        pointsCheckbox.setChecked(true);

                        relativeData.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);

                        updateBillDetails();

                    } catch (Exception e) {
                        Toast.makeText(getActivity(), getString(R.string.server_error), Toast.LENGTH_SHORT).show();

                    }
                }, new VolleyGlobalErrorListener(getActivity()) {
            @Override
            public void postError(VolleyError error) {

                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(myJsonObjectRequest2);
    }

    private void updateFeatures(SubscriptionDescriptionModel data) {
        arr.clear();
        PlanDescription planDescription = data.getPlanDescription();
        arr.addAll(planDescription.getFeatures());
        adapter.notifyDataSetChanged();
    }

    private void updateTopView(SubscriptionDescriptionModel data) {
        duration.setText(data.getDuration().getValue() + " " + data.getDuration().getDurationUnit());
        payment_money.setText("₹ " + data.getPrice());
    }

    private void PDFConvert() {
        PdfDocument myPdfDocument = new PdfDocument();
        Paint myPaint = new Paint();

        PdfDocument.PageInfo myPageInfo1 = new PdfDocument.PageInfo.Builder(400,600,1).create();
        PdfDocument.Page myPage1 = myPdfDocument.startPage(myPageInfo1);
        Canvas canvas = myPage1.getCanvas();
        canvas.drawText("Welcome To Arivihan Technology Invoice Page",40,50,myPaint);
        myPdfDocument.finishPage(myPage1);


        File file = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),"/Invoice.pdf");


        try {
            myPdfDocument.writeTo(new FileOutputStream(file));
        }catch (IOException e){
            Log.i("PDFException ",""+e.getMessage().toString());
        }

        myPdfDocument.close();

    }

    public void updateBillDetails() {
        cost_price_text.setText(getRupeeText(costPrice));
        coupon_applied_text.setText(getRupeeText( - couponApplied));
        internal_discount_text.setText(getRupeeText(- internalDiscount));
        points_used_text.setText(getRupeeText(- pointsUsed));
        int total = costPrice - internalDiscount;
        if (total > 0) {
            total = total - couponApplied;
        }
        if (total > 0) {
            total =  total - pointsUsed;
        }
        if (total < 0) {
            total = 0;
        }
        total_amount.setText(getRupeeText(total));
    }

}