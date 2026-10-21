package arivihan.technologies.doubtbuzzter2.activity;

/*
 * File: BaseActivity Copyright 23/5/21 1:07 AM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 23/5/21 1:07 AM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import static arivihan.technologies.doubtbuzzter2.common.feedback.AppReviewDialog.openPlayStore;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.FORCE_UPDATE_HIT_COUNT;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.AnimationHelper;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.PosterPopUp;
import arivihan.technologies.doubtbuzzter2.customView.textview.ColoredStrikeSpan;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.helper.ContextWrapper;
import arivihan.technologies.doubtbuzzter2.helper.SubscriptionHelper;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionPlans;
import arivihan.technologies.doubtbuzzter2.model.firebase.rdb.SubscriptionPopUpModel;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;

public class BaseActivity extends AppCompatActivity {
    private static final String TAG = BaseActivity.class.getSimpleName();
    private PosterPopUp posterPopUp;
    private DatabaseReference subscriptionPopUpDatabaseRef;
    private ValueEventListener subscriptionPopUpListener;
    private Dialog subscriptionDialog;


    public BaseActivity() {
        //MyApplication.setLocale();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //MyApplication.setLocale();
        setStatusBarGradiant(this);
        attachSubscriptionPopUpListener();

//        if (!isForceUpdateShow()) {
        checkAndShowPosterPopup();
//        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        detachSubscriptionPopUpListener();
    }

    public static void setStatusBarGradiant(Activity activity) {
        Window window = activity.getWindow();
        Drawable background = AppCompatResources.getDrawable(activity.getApplication(), R.drawable.theme_white_color);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent, activity.getTheme()));
        window.setBackgroundDrawable(background);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Context context = ContextWrapper.wrap(newBase, MyApplication.getLocale());
        super.attachBaseContext(context);
    }

    /**
     * Hides the system bars for the players.
     *
     * <p>The screen already draws edge to edge, so this only has to hide the bars: the insets go
     * to zero while they are hidden and EdgeToEdgeHelper's padding follows them down on its own.
     * The legacy {@code SYSTEM_UI_FLAG_*} route this replaces is deprecated and does not survive
     * the switch.
     */
    public void enableImmersiveMode() {
        Window window = getWindow();
        if (window == null) return;

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, window.getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    public void secureScreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (BuildConfig.APP_UPDATE_POPUP) {
            String recordingAllowedUsers = MyApplication.getFirebaseRemoteConfig().getString("allow_screen_recording_for_users");
            if (StringUtils.isNotEmpty(recordingAllowedUsers)) {
                String phoneNumber = MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey());
                String[] allowedUsers = recordingAllowedUsers.split(",");

                // Initialize a flag to indicate if the phone number is allowed
                boolean isPhoneNumberAllowed = false;

                // Iterate over the array and check if the phone number exists
                for (String item : allowedUsers) {
                    if (item.trim().equals(phoneNumber)) {
                        isPhoneNumberAllowed = true;
                        break; // Exit loop early if a match is found
                    }
                }

                // If the phone number is not allowed, set the FLAG_SECURE flag on the window
                if (!isPhoneNumberAllowed) {
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
                }
            } else {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
            }
        }
    }

    private void checkAndShowPosterPopup() {
        String screenName = this.getClass().getSimpleName();
        Log.d("PosterPopup", "Checking poster for screen: " + screenName);
        FirebaseRemoteConfig remoteConfig = MyApplication.getFirebaseRemoteConfig();
        try {
            String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
            if (lang != null) {
                String selectedCourseId = MyApplication.getStringData(PreferenceKeys.SECONDARY_COURSE_ID.getKey());
                String configString = remoteConfig.getString("dynamic_poster_pop_up_home_screen_" + lang);
                JSONObject allConfig = new JSONObject(configString);
                Log.d("dynamic pop up", "onCreate: " + selectedCourseId + "--" + remoteConfig.getString("dynamic_poster_pop_up_home_screen_" + lang));
                if (StringUtils.isNotEmpty(selectedCourseId) && allConfig.has(selectedCourseId)) {
                    JSONObject courseConfig = allConfig.getJSONObject(selectedCourseId);
                    if (courseConfig.has(screenName)) {
                        JSONObject screenConfig = courseConfig.getJSONObject(screenName);
                        boolean isEnabled = screenConfig.optBoolean("enable", false);
                        String posterUrl = screenConfig.optString("poster_url", "");
                        int maxCount = screenConfig.optInt("count", 0);
                        long popUpTimer = screenConfig.optLong("pop_up_timer", 4000);
                        String targetActivity = screenConfig.optString("target_activity", "");
                        JSONArray showAtArray = screenConfig.optJSONArray("show_at");

                        Log.d("PosterPopup", "Poster config - enabled: " + isEnabled + ", url: " + posterUrl + ", maxCount: " + maxCount);

                        if (!isEnabled || StringUtils.isEmpty(posterUrl)) {
                            return;
                        }

                        int currentCount = MyApplication.getIntData(PreferenceKeys.POSTER_COUNT_PREFIX.getKey() + screenName);
                        int newCount = currentCount + 1;

                        boolean shouldShow = false;

                        if (showAtArray != null) {
                            for (int i = 0; i < showAtArray.length(); i++) {
                                if (newCount == showAtArray.optInt(i)) {
                                    shouldShow = true;
                                    break;
                                }
                            }
                        } else {
                            shouldShow = newCount <= maxCount;
                        }

                        if (shouldShow) {
                            MyApplication.saveData(PreferenceKeys.POSTER_COUNT_PREFIX.getKey() + screenName, newCount);

                            posterPopUp = new PosterPopUp(this);

                            if (!StringUtils.isEmpty(targetActivity)) {
                                posterPopUp.setTargetActivity(targetActivity);

                                if (screenConfig.has("intent_extras")) {
                                    JSONObject extras = screenConfig.getJSONObject("intent_extras");
                                    Iterator<String> keys = extras.keys();
                                    while (keys.hasNext()) {
                                        String key = keys.next();
                                        String value = extras.optString(key, "");
                                        posterPopUp.addIntentExtra(key, value);
                                    }
                                }
                            }

                            posterPopUp.showPosterPopUp("", posterUrl, popUpTimer);
                        } else {
                            Log.d("PosterPopup", "Poster not shown. Not in show_at list or over max count.");
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e("PosterPopup", "JSON parsing error: ", e);
        }
    }


    private void attachSubscriptionPopUpListener() {
        if (subscriptionPopUpListener != null) return; // already attached

        subscriptionPopUpDatabaseRef = FirebaseDatabase.getInstance().getReference("users/SubscriptionPopUp/" + MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));

        subscriptionPopUpListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    if (!snapshot.exists()) {
                        Log.d(TAG, "No model data found.");
                        return;
                    }

                    SubscriptionPopUpModel model = snapshot.getValue(SubscriptionPopUpModel.class);
                    if (model == null) {
                        Log.w(TAG, "Poster data is null or invalid");
                        return;
                    }

                    model.setExpiryDuration(model.getCreatedAt() + model.getExpiryDuration());
                    long now = System.currentTimeMillis();
                    long expiry = model.getExpiryDuration();

                    if (now > expiry) {
                        Log.d(TAG, "Poster expired, removing...");
                        removeSubscriptionPopUpFromFirebaseRDB();
                        return;
                    }

//                    if (subscriptionDialog != null && subscriptionDialog.isShowing())
//                        return;

                    showPosterDialog(model);

                } catch (Exception e) {
                    Log.e(TAG, "Error reading poster data", e);
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Poster listener cancelled: " + error.getMessage());
            }
        };

        subscriptionPopUpDatabaseRef.addValueEventListener(subscriptionPopUpListener);
    }

    private void detachSubscriptionPopUpListener() {
        if (subscriptionPopUpDatabaseRef != null && subscriptionPopUpListener != null) {
            subscriptionPopUpDatabaseRef.removeEventListener(subscriptionPopUpListener);
            subscriptionPopUpListener = null;
        }
    }

    private void removeSubscriptionPopUpFromFirebaseRDB() {
        if (subscriptionPopUpDatabaseRef == null)
            subscriptionPopUpDatabaseRef = FirebaseDatabase.getInstance().getReference("users/SubscriptionPopUp/" + MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
        subscriptionPopUpDatabaseRef.removeValue((error, ref) -> {
            if (error != null) {
                Log.e(TAG, "Failed to remove expired poster: " + error.getMessage());
            } else {
                Log.d(TAG, "Poster removed successfully");
            }
        });
    }


    public void showPosterDialog(SubscriptionPopUpModel subscriptionPopUpModel) {
        if (isFinishing() || isDestroyed() || subscriptionPopUpModel == null) return;
        try {
            dismissPosterDialog();

            subscriptionDialog = new Dialog(this);
            subscriptionDialog.setContentView(R.layout.dialog_mentor_subscription_pop_up);
            subscriptionDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
            subscriptionDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
            subscriptionDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            subscriptionDialog.setCancelable(false);

            TextView tv_header = subscriptionDialog.findViewById(R.id.tv_header);
            TextView tv_title = subscriptionDialog.findViewById(R.id.tv_title);
            TextView tv_amount = subscriptionDialog.findViewById(R.id.tv_amount);
            TextView tv_subscription_title = subscriptionDialog.findViewById(R.id.tv_subscription_title);
            TextView tv_discount_price = subscriptionDialog.findViewById(R.id.tv_discount_price);
            TextView tv_cost_price = subscriptionDialog.findViewById(R.id.tv_cost_price);
            TextView tv_mentor_name = subscriptionDialog.findViewById(R.id.tv_mentor_name);
            TextView tv_timer = subscriptionDialog.findViewById(R.id.tv_timer);

            if (StringUtils.isNotEmpty(subscriptionPopUpModel.getTitle())) {
                tv_header.setText(subscriptionPopUpModel.getTitle());
            }
            tv_mentor_name.setText(StringUtils.isNotEmpty(subscriptionPopUpModel.getPaymentReceivedByUserName()) ? subscriptionPopUpModel.getPaymentReceivedByUserName() : "");
            tv_subscription_title.setText(subscriptionPopUpModel.getPlanName());

            tv_title.setText(StringUtils.isNotEmpty(subscriptionPopUpModel.getUpiId()) ? subscriptionPopUpModel.getUpiId() : "");
            tv_amount.setText(String.format(getString(R.string.price_format), (subscriptionPopUpModel.getFinalAmount() != null) ? subscriptionPopUpModel.getFinalAmount() : ""));
            tv_cost_price.setText(String.format(getString(R.string.price_format), (subscriptionPopUpModel.getPrice() != null) ? subscriptionPopUpModel.getPrice() : ""));
            tv_discount_price.setText(String.format(getString(R.string.price_format), (subscriptionPopUpModel.getFinalAmount() != null) ? subscriptionPopUpModel.getFinalAmount() : ""));

            SpannableString ss = SubscriptionHelper.getStrikeThroughText(this, subscriptionPopUpModel.getPrice() != null ? String.valueOf(subscriptionPopUpModel.getPrice()) : "");
            if (ss != null) {
                tv_cost_price.setText(ss);
            }

            long currentTimeMillis = System.currentTimeMillis();
            long millisUntilFinished = subscriptionPopUpModel.getExpiryDuration() - currentTimeMillis;

            CountDownTimer countDownTimer;

            if (millisUntilFinished <= 0) {
                tv_timer.setText("00:00");
                tv_timer.setTextColor(getColor(R.color.red));
                return;
            }

            countDownTimer = new CountDownTimer(millisUntilFinished, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    long seconds = millisUntilFinished / 1000;
                    long minutes = (seconds % 3600) / 60;
                    long secs = seconds % 60;

                    String time = String.format(Locale.getDefault(), "%02d:%02d", minutes, secs);
                    tv_timer.setText(time);
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "Timer On Finish");
                    tv_timer.setText("00:00");
                    tv_timer.setTextColor(getColor(R.color.red));
                }
            }.start();

            subscriptionDialog.findViewById(R.id.cv_close).setOnClickListener(dv -> {
                removeSubscriptionPopUpFromFirebaseRDB();
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.auto_subscription_pop_up_canceled.getActionName(), SubscriptionHelper.getEventParameters(subscriptionPopUpModel));
                subscriptionDialog.dismiss();
            });

            subscriptionDialog.findViewById(R.id.cv_decline).setOnClickListener(dv -> {
                removeSubscriptionPopUpFromFirebaseRDB();
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.auto_subscription_pop_up_decline.getActionName(), SubscriptionHelper.getEventParameters(subscriptionPopUpModel));
                subscriptionDialog.dismiss();
            });

            subscriptionDialog.findViewById(R.id.cv_pay_now).setOnClickListener(dv -> {
                try {
                    long currentTime = System.currentTimeMillis();
                    long timeDiff = subscriptionPopUpModel.getExpiryDuration() - currentTime;
                    if (timeDiff < 0) {
                        removeSubscriptionPopUpFromFirebaseRDB();
                        Toast.makeText(this, getString(R.string.time_out), Toast.LENGTH_SHORT).show();
                        subscriptionDialog.dismiss();
                        return;
                    }
                    int amount = (subscriptionPopUpModel.getFinalAmount() != null && subscriptionPopUpModel.getFinalAmount() > 0) ? (subscriptionPopUpModel.getFinalAmount().intValue()) : 0;
                    if (amount > 0) {
                        removeSubscriptionPopUpFromFirebaseRDB();
                        SubscriptionPlans subscriptionPlans = new SubscriptionPlans();
                        subscriptionPlans.setPlanId(subscriptionPopUpModel.getPlanId());
                        subscriptionPlans.setFinalPrice(String.valueOf(subscriptionPopUpModel.getFinalAmount()));
                        subscriptionPlans.setCostPrice(String.valueOf(subscriptionPopUpModel.getPrice()));
                        subscriptionPlans.setName(subscriptionPopUpModel.getPlanName());
                        subscriptionPlans.setRequestId(subscriptionPopUpModel.getRequestId());
                        subscriptionPlans.setCoupon(SubscriptionHelper.getCouponCode(subscriptionPopUpModel));
                        new SubscriptionHelper().startPayment(this, subscriptionPlans, subscriptionPopUpModel);
                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.auto_subscription_pop_up_accepted.getActionName(), SubscriptionHelper.getEventParameters(subscriptionPopUpModel));
                    } else {
                        Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                subscriptionDialog.dismiss();
            });

            subscriptionDialog.setOnDismissListener(v -> {
                try {
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            subscriptionDialog.show();

            MyApplication.logFirebaseBundleCreation(FirebaseEvents.auto_subscription_pop_up_viewed.getActionName(), SubscriptionHelper.getEventParameters(subscriptionPopUpModel));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void dismissPosterDialog() {
        try {
            if (subscriptionDialog != null && subscriptionDialog.isShowing()) {
                subscriptionDialog.dismiss();
            }
        } catch (Exception ignored) {
        } finally {
            subscriptionDialog = null;
        }
    }


    public boolean isForceUpdateShow() {
        JSONObject result = getForceUpdateData();
        return result != null;
    }


    //force update - null if not show
    public JSONObject getForceUpdateData() {
        Log.d(TAG, "checkForceUpdate: ");

        // force update bypass config
        if (forceUpdateBypass()) return null;


        try {
            // remote config json
            String json = MyApplication.getFirebaseRemoteConfig().getString("force_update_config");
            if (StringUtils.isEmpty(json)) return null;

            JSONObject root = new JSONObject(json);

            JSONObject forceUpdate = root.optJSONObject("force_update");
            if (forceUpdate == null) return null;

            // enable check
            boolean enable = forceUpdate.optBoolean("enable", false);
            if (!enable) return null;

            boolean isDismissAllowed;

            // allowed version list
            JSONObject versionObject = forceUpdate.optJSONObject("enable_version_list");
            if (versionObject != null) {
                JSONObject currentVersionObj = versionObject.optJSONObject(BuildConfig.VERSION_NAME);

                if (currentVersionObj != null) {
                    isDismissAllowed = currentVersionObj.optBoolean("dismiss", false);
                } else {
                    return null;
                }
            } else {
                return null;
            }

            // popup data
            JSONObject popupData = forceUpdate.optJSONObject("popup_data");
            if (popupData == null) return null;

            //put isDismissAllowed
            popupData.put("isDismissAllowed", isDismissAllowed);

            return popupData;

        } catch (Exception e) {
            Log.e(TAG, "error = " + e.getMessage());
            return null;
        }

    }


    private boolean forceUpdateBypass() {

        try {
            // bypass config
            String bypassJson = MyApplication.getFirebaseRemoteConfig()
                    .getString("force_update_bypass_config");

            if (StringUtils.isNotEmpty(bypassJson)) {
                JSONObject bypassObj = new JSONObject(bypassJson);
                boolean bypassEnable = bypassObj.optBoolean("enable", false);

                if (bypassEnable) {
                    String batchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
                    String examCode = MyApplication.getStringData(PreferenceKeys.EXAM_CODE.getKey());
                    String phone = MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey());

                    // batch bypass
                    JSONArray batchArray = bypassObj.optJSONArray("batch_codes");
                    if (batchArray != null) {
                        for (int i = 0; i < batchArray.length(); i++) {
                            if (batchArray.optString(i).equals(batchCode)) return true;
                        }
                    }

                    // exam bypass
                    JSONArray examArray = bypassObj.optJSONArray("exam_codes");
                    if (examArray != null) {
                        for (int i = 0; i < examArray.length(); i++) {
                            if (examArray.optString(i).equals(examCode)) return true;
                        }
                    }

                    // phone bypass
                    JSONArray phoneArray = bypassObj.optJSONArray("phone_numbers");
                    if (phoneArray != null) {
                        for (int i = 0; i < phoneArray.length(); i++) {
                            if (phoneArray.optString(i).equals(phone)) return true;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            return false;
        }

        return false;
    }


    public void showForceUpdateDialog(JSONObject popupData) {
        try {
            boolean isDismissAllowed = popupData.optBoolean("isDismissAllowed", false);

            // language
            String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
            if (StringUtils.isEmpty(lang)) lang = "en";

            // dialog
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_app_force_update);


//            //extra
//            if (dialog.getWindow() != null) {
//                Window window = dialog.getWindow();
//                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//                window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//            }


            if (dialog.getWindow() != null) {
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }

            // cannot close
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);


            // views
            TextView tvTitle = dialog.findViewById(R.id.title);
            TextView tvSubTitle = dialog.findViewById(R.id.subtitle);

            CardView cvUpdate = dialog.findViewById(R.id.cv_okay);
            TextView tvButton = dialog.findViewById(R.id.btnText);

            //close
            ImageView ivClose = dialog.findViewById(R.id.iv_close);
            ivClose.setVisibility(View.GONE);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    ivClose.setVisibility(View.VISIBLE);
                }
            }, 5000);

            // snack bar mess
            JSONObject snackBarMess = popupData.optJSONObject("snack_bar_mess");

            String finalLang = lang;
            ivClose.setOnClickListener(view -> {
                if (isDismissAllowed) {
                    dialog.dismiss();
                } else {
                    View parentLayout = findViewById(android.R.id.content);
                    CommonMethods.showNegativeSnackbar(parentLayout, snackBarMess != null ? snackBarMess.optString(finalLang) : "");
                    AnimationHelper.shakeDialogAndVibrate(dialog, this);
                }

                //send event to server
                setForceUpdateHitCount();
            });


            // title
            JSONObject titleObj = popupData.optJSONObject("title");
            tvTitle.setText(titleObj != null ? titleObj.optString(lang) : "");

            // subtitle
            JSONObject subTitleObj = popupData.optJSONObject("sub_title");
            tvSubTitle.setText(subTitleObj != null ? subTitleObj.optString(lang) : "");

            // button
            JSONObject buttonObj = popupData.optJSONObject("button_text");
            tvButton.setText(buttonObj != null ? buttonObj.optString(lang) : "");


            // update click
            cvUpdate.setOnClickListener(v -> openPlayStore(this));

            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "error = " + e.getMessage());
        }
    }


    private void setForceUpdateHitCount() {
        String appVersion = BuildConfig.VERSION_NAME;

        Map<String, Object> map = new HashMap<>();
        map.put("appVersion", appVersion);

        MyJsonObjectRequest getDataRequest = new MyJsonObjectRequest(
                Request.Method.GET,
                CommonMethods.getUrlParam(FORCE_UPDATE_HIT_COUNT, map),
                null,
                response -> Log.d(TAG, "Response: " + response),
                new VolleyGlobalErrorListener(this) {
                    @Override
                    public void postError(VolleyError error) {
                        Log.d(TAG, "postError: " + error.getMessage());
                    }
                }
        );
        getDataRequest.setTag("HomeScreen");
        MyVolleySingleton.getInstance(this).addToRequestQueue(getDataRequest);
    }


}

