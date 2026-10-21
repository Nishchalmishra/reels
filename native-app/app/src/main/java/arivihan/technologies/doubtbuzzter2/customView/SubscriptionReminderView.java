package arivihan.technologies.doubtbuzzter2.customView;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.SUBSCRIPTION_EXPIRATION_REMINDER_GET;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.SUBSCRIPTION_EXPIRATION_REMINDER_UPDATE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;


import arivihan.technologies.doubtbuzzter2.activity.CampaignSubscriptionActivity;
import arivihan.technologies.doubtbuzzter2.activity.SubscriptionActivityNew;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionRenewalReminder;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionRenewalReminderSheet;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;

public class SubscriptionReminderView implements SubscriptionRenewalReminderSheet.BottomSheetListener {

    Context context;
    SubscriptionRenewalReminder subscriptionRenewalReminder;
    Gson gson = new Gson();
    SubscriptionRenewalReminderSheet subscriptionRenewalReminderSheet;
    FragmentManager fragmentManager;

    public SubscriptionReminderView(Context context, FragmentManager fragmentManager) {
        this.context = context;
        this.fragmentManager=fragmentManager;
        get();
    }

    private void get() {
        Map<String, Object> map = new HashMap<>();
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(SUBSCRIPTION_EXPIRATION_REMINDER_GET, map), null,
                response -> {
                    TypeToken<SubscriptionRenewalReminder> token = new TypeToken<>() {};
                    subscriptionRenewalReminder = gson.fromJson(response.toString(),token.getType());
                    if(subscriptionRenewalReminder!=null && StringUtils.isNotEmpty(subscriptionRenewalReminder.getReminderTitle())){
                        subscriptionRenewalReminderSheet =
                                SubscriptionRenewalReminderSheet.newInstance(
                                        subscriptionRenewalReminder,
                                        this
                                );
                        if (context instanceof Activity activity) {
                            if (!activity.isFinishing() && !activity.isDestroyed() && !fragmentManager.isStateSaved()) {
                                subscriptionRenewalReminderSheet.show(fragmentManager, "");
                                subscriptionRenewalReminderSheet.setCancelable(false);
                            }
                        }
                    }
                    Log.d("SUBSCRIPTION_EXPIRATION_REMINDER_GET", "success: " + response.toString());

                }, new VolleyGlobalErrorListener(context.getApplicationContext()) {
            @Override
            public void postError(VolleyError error) {
                Log.e("SUBSCRIPTION_EXPIRATION_REMINDER_GET", "Error: " + error.getMessage());
                //Toast.makeText(context.getApplicationContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });
        //myJsonObjectRequest2.setTag(TAG);
        MyVolleySingleton.getInstance(context.getApplicationContext()).addToRequestQueue(myJsonObjectRequest);
    }

    private void update(boolean delete) {
        Map<String, Object> map = new HashMap<>();
        map.put("delete",delete);
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(SUBSCRIPTION_EXPIRATION_REMINDER_UPDATE, map), null,
                response -> {
                }, new VolleyGlobalErrorListener(context.getApplicationContext()) {
            @Override
            public void postError(VolleyError error) {
                Log.e("VolleyError", "Error: " + error.getMessage());
                //Toast.makeText(context.getApplicationContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });
        MyVolleySingleton.getInstance(context.getApplicationContext()).addToRequestQueue(myJsonObjectRequest);
    }

    @Override
    public void onRenewClick() {
        subscriptionRenewalReminderSheet.dismiss();
        update(true);
//        Intent intent = new Intent(context, CampaignSubscriptionActivity.class);
////        intent.putExtra("plan_level", subscriptionRenewalReminder.getPlanLevel());
////        intent.putExtra("fromMySubscription",true);
//        context.startActivity(intent);
        SubscriptionNavigationHelper.navigateToSubscriptionPage(context, RedirectionSource.RENEW_REMINDER);
    }

    @Override
    public void onLaterClick() {
        subscriptionRenewalReminderSheet.dismiss();
        update(false);
    }

    @Override
    public void onCloseClick() {
        subscriptionRenewalReminderSheet.dismiss();
       update(false);
    }
}
