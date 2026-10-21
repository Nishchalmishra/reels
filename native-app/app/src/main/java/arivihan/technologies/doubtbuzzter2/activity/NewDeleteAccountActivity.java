package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.DELETE_ACCOUNT_OTP_GENERATE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.DELETE_ACCOUNT_OTP_VALIDATE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.DELETE_YOUR_ACCOUNT;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GENERATE_OTP;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.LOGOUT_YOUR_ACCOUNT;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.PUBLISH_SCREEN_VISIT_EVENT;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.firebase.RemoteConfigKey;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.fragments.authentication.VerifyNumberFragment;
import arivihan.technologies.doubtbuzzter2.helper.BranchAnalyticsHelper;
import arivihan.technologies.doubtbuzzter2.helper.MyOtpTextWatcher;
import arivihan.technologies.doubtbuzzter2.helper.firebase.FirebaseMessagingManager;
import arivihan.technologies.doubtbuzzter2.model.ApiResponse;
import arivihan.technologies.doubtbuzzter2.viewModel.HomeScreenViewModel;
import arivihan.technologies.doubtbuzzter2.volley.EventApiHelper;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;


public class NewDeleteAccountActivity extends BaseActivity {
    private static final String TAG = NewDeleteAccountActivity.class.getSimpleName();

    Gson gson = new Gson();

    private View rootLayout;

    private ImageView ivBack;

    private TextView tvTitle, tvSubTitle;
    private CardView feedbackCard;
    private EditText etFeedbackEditText;

    private LinearLayout cbConfirmationLayout;
    private CheckBox cbConfirmation;
    private TextView cbConfirmationText;

    private LinearLayout llReasons;
    private CardView cvPrimaryDelete, cvSecondaryDelete;
    private TextView txSecondary;

    private RadioButton selectedRadioButton;

    //data
    private JSONObject selectedReason;


    private enum ActionType{
        LOGOUT,
        DELETE
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_delete_account);
        rootLayout = findViewById(android.R.id.content);

        initViews();
        initListeners();

        //show view
        JSONObject config = getDeleteAccountConfig();
        setDeleteAccountData(config); //set view
        assert config != null;

        createReasonViews(config); //create reason views
    }


    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        tvTitle = findViewById(R.id.tv_title);
        tvSubTitle = findViewById(R.id.tvSubTitle);
        feedbackCard = findViewById(R.id.feedback_card);
        etFeedbackEditText = findViewById(R.id.et_feedback_edit_text);
        cbConfirmationLayout = findViewById(R.id.cb_confirmation_layout);
        cbConfirmation = findViewById(R.id.cb_confirmation);
        cbConfirmationText = findViewById(R.id.cb_confirmation_text);
        llReasons = findViewById(R.id.llReasons);
        cvPrimaryDelete = findViewById(R.id.cv_primary);
        cvSecondaryDelete = findViewById(R.id.cv_secondary);
        txSecondary = findViewById(R.id.tx_secondary);

        //enable false till other click
        etFeedbackEditText.setEnabled(false);
        feedbackCard.setVisibility(View.GONE);

        updateDeleteButton();
    }


    private void initListeners() {
        ivBack.setOnClickListener(v -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.addtl_set_del_acc_back_btn_click.getActionName());
            finish();
        });

        findViewById(R.id.back_button_layout).setOnClickListener(v -> finish());
        cvPrimaryDelete.setOnClickListener(v -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.addtl_set_del_acc_cancel_btn_click.getActionName());
            finish();
        });

        cvSecondaryDelete.setOnClickListener(v -> {
            if (selectedReason == null) return;

            try {
                String action = selectedReason.getString("action");
                Map<String, String> params = new HashMap<>();
                params.put("reasonSelected", selectedReason.optJSONObject("title").optString(Locale.getDefault().getLanguage().equals("hi") ? "hi" : "en"));
                params.put("actionType", action);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.addtl_set_del_acc_action_taken.getActionName(), params);

                if (ActionType.LOGOUT.name().equalsIgnoreCase(action)) logout();
                if (ActionType.DELETE.name().equalsIgnoreCase(action)) handleOtpFlow();
            } catch (JSONException ignored) {
            }

        });

        cbConfirmation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.addtl_set_del_acc_checkbox_click.getActionName());
            updateDeleteButton();
        });
        cbConfirmationText.setOnClickListener(v -> cbConfirmation.setChecked(!cbConfirmation.isChecked()));
    }



    //handle bypass
    private void handleOtpFlow(){
        if (isUserAllowedToByPass()){
            showDeleteAccountDialog();
            return;
        }

        showOtpDialog();
    }



    //bypass
    private boolean isUserAllowedToByPass() {
        try {
            String allowToByPassForUsers = MyApplication.getFirebaseRemoteConfig().getString(RemoteConfigKey.INTERNAL_DEV_USERS_TO_BY_PASS_FEATURE.getKey());
            String phoneNumber = MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey());

            if (phoneNumber == null || phoneNumber.isEmpty()) return false;
            return allowToByPassForUsers.contains(phoneNumber);
        } catch (Exception ex) {
            Log.e(TAG, "error = " + ex.getMessage());
        }

        return false;
    }




    private JSONObject getDeleteAccountConfig() {
        try {
            String json = FirebaseRemoteConfig.getInstance().getString("account_delete_config");
            return new JSONObject(json).getJSONObject("account_delete_config");
        } catch (Exception e) {
            return null;
        }
    }


    private void setDeleteAccountData(JSONObject config) {
        if (config == null) return;

        try {
            String lang = Locale.getDefault().getLanguage();
            if (!lang.equals("hi")) lang = "en";

            tvTitle.setText(config.getJSONObject("title").optString(lang));
            tvSubTitle.setText(config.getJSONObject("subTitle").optString(lang));
            etFeedbackEditText.setHint(config.getJSONObject("feedbackHint").optString(lang));
            cbConfirmationText.setText(config.getJSONObject("confirmationText").optString(lang));
        } catch (Exception e) {
            Log.e(TAG, "error = " + e.getMessage());
        }
    }



    //create reason list
    private void createReasonViews(JSONObject config) {
        llReasons.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        JSONArray items = config.optJSONArray("items");
        if (items == null) return;

        for (int i = 0; i < items.length(); i++) {
            try {
                JSONObject item = items.getJSONObject(i);

                View view = inflater.inflate(R.layout.item_delete_reason, llReasons, false);
                RadioButton rbReason = view.findViewById(R.id.rbReason);
                TextView tvReason = view.findViewById(R.id.tvReason);

                String language = Locale.getDefault().getLanguage();
                if (!language.equals("hi")) language = "en";

                String title = item.getJSONObject("title").optString(language);
                tvReason.setText(title);

                view.setOnClickListener(v -> selectReason(rbReason, config, item));
                rbReason.setOnClickListener(v -> selectReason(rbReason, config, item));

                llReasons.addView(view);
            } catch (Exception e) {
                Log.e(TAG, "error = " + e.getMessage());
            }
        }
    }



    //on select
    private void selectReason(RadioButton radioButton, JSONObject config, JSONObject item) {
        if (selectedRadioButton != null) selectedRadioButton.setChecked(false);
        radioButton.setChecked(true);

        selectedRadioButton = radioButton;
        selectedReason = item;

        try {
            boolean showFeedback = item.optBoolean("showFeedback", false);
            etFeedbackEditText.setEnabled(showFeedback);
            feedbackCard.setVisibility(showFeedback ? View.VISIBLE : View.GONE);

            if (showFeedback) etFeedbackEditText.requestFocus();
            else etFeedbackEditText.clearFocus();

            String action = item.optString("action");

            if (ActionType.LOGOUT.name().equalsIgnoreCase(action)) {
                txSecondary.setText(getButtonText(config, "logout"));
                cbConfirmation.setChecked(false);
                cbConfirmationLayout.setVisibility(View.GONE);
            }

            if (ActionType.DELETE.name().equalsIgnoreCase(action)) {
                txSecondary.setText(getButtonText(config, "delete"));
                cbConfirmationLayout.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
            Log.e(TAG, "error = " + e.getMessage());
        }

        updateDeleteButton();
    }


    private String getButtonText(JSONObject config, String key) {
        try {
            String lang = Locale.getDefault().getLanguage();
            if (!lang.equals("hi")) lang = "en";

            return config.getJSONObject("buttons").getJSONObject(key).optString(lang);
        } catch (Exception e) {
            Log.e(TAG, "error = " + e.getMessage());
        }

        return "";
    }


    private void updateDeleteButton() {
        boolean enable = false;

        if (selectedReason != null) {
            String action = selectedReason.optString("action");
            enable = ActionType.LOGOUT.name().equalsIgnoreCase(action) || cbConfirmation.isChecked();
        }

        cvSecondaryDelete.setEnabled(enable);
        if (enable) {
            cvSecondaryDelete.setCardBackgroundColor(ContextCompat.getColor(this, R.color.error_btn_secondary_bg));
            txSecondary.setTextColor(ContextCompat.getColor(this, R.color.error_btn_secondary_tx));
        } else {
            cvSecondaryDelete.setCardBackgroundColor(ContextCompat.getColor(this, R.color.error_btn_secondary_bg_disable));
            txSecondary.setTextColor(ContextCompat.getColor(this, R.color.error_btn_secondary_tx_disable));
        }
    }





    //delete account
    private void deleteApiCall(String reason) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("reason", reason);
            MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.DELETE,
                    CommonMethods.getUrlParam(DELETE_YOUR_ACCOUNT, map), null,
                    response -> {
                        TypeToken<ApiResponse> token = new TypeToken<>() {};
                        ApiResponse apiResponse = gson.fromJson(response.toString(), token.getType());

                        if (apiResponse.isSuccess()) {
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.account_delete.getActionName());
                            FirebaseMessagingManager.unSubscribeToUserDefaultTopics();
                            FirebaseMessagingManager.detachUser(this);
                            BranchAnalyticsHelper.logoutUser();
                            deleteAllLocalData();
                            MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                            MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);

                            //login
                            Intent homeIntent = new Intent(NewDeleteAccountActivity.this, UserAuthenticationActivity.class);
                            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(homeIntent);
                            CommonMethods.showPositiveSnackbar(rootLayout, getString(R.string.SuccessfullLogout));
                            finish();
                        }
                    }, new VolleyGlobalErrorListener(this) {
                @Override
                public void postError(VolleyError error) {

                }
            });
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
        } catch (Exception ignored) {

        }
    }

    private void logout() {
        try {
            FirebaseMessagingManager.detachUser(this);
            FirebaseMessagingManager.unSubscribeToUserDefaultTopics();
            BranchAnalyticsHelper.logoutUser();

            //logout api
            try {
                MyJsonObjectRequest logoutRequest = new MyJsonObjectRequest(
                        Request.Method.DELETE,
                        CommonMethods.getUrlParam(LOGOUT_YOUR_ACCOUNT, Collections.emptyMap()),
                        null,
                        response -> {},
                        error -> {}
                );
                MyVolleySingleton.getInstance(this).addToRequestQueue(logoutRequest);
            } catch (Exception e) {
                Log.e(TAG, "error = " + e.getMessage());
            }

            deleteAllLocalData();

            MyApplication application = (MyApplication) getApplication();
            HomeScreenViewModel homeScreenViewModel = application.getHomeScreenViewModel();
            homeScreenViewModel.removeDataModel();

            MyApplication.logFirebaseBundleCreation(FirebaseEvents.addtl_set_del_acc_logout_btn_click.getActionName());
            Intent homeIntent = new Intent(this, UserAuthenticationActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(homeIntent);

            CommonMethods.showPositiveSnackbar(rootLayout, getString(R.string.SuccessfullLogout));
            finish();
        } catch (Exception ex) {
            Log.e(TAG, "error = " + ex.getMessage());
        }
    }


    private void deleteAllLocalData() {
        MyApplication.getInstance().getMediaViewModel().clearUserData(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
        MyApplication.getInstance().getHomeScreenViewModel().removeDataModel();
        MyApplication.getUserSelectedVideoQualityDatabaseReference().setValue(null);
        MyApplication.getUserSelectedDownloadQualityDatabaseReference().setValue(null);
        MyApplication.getTestGenerationParentDatabaseReference().setValue(null);
        getViewModelStore().clear();
        MyApplication.clearSharedPref();
        MyApplication.removeFromDoNotDeleteSharedPreference(PreferenceKeys.REFERRAL_CODE.getKey());
        MyApplication.removeFromDoNotDeleteSharedPreference(PreferenceKeys.REFERRAL_SOURCE.getKey());
        MyApplication.removeFromDoNotDeleteSharedPreference(PreferenceKeys.REFERRAL_DETECTED.getKey());
        MyApplication.removeFromDoNotDeleteSharedPreference(PreferenceKeys.SELECTED_COURSE_ID.getKey());
        MyApplication.removeFromDoNotDeleteSharedPreference(PreferenceKeys.DO_NOT_DELETE_SELECTED_COURSE_MODEL.getKey());
    }





    //////////// OTP FLOW ////////

    //otp dialog
    EditText one, two, three, four, five, six;
    EditText[] editTexts;
    String ansOtp = "";
    SpinKitView spinKitView;
    TextView resend, ResendMessage;
    private CardView cvPrimaryOTP;
    private TextView txPrimaryOTP;

    //timer
    private CountDownTimer countDownTimer;
    private long remainingTime = 60000L;



    public void showOtpDialog() {
        //send otp
        generateOtpBackend(MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey()));

        try {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_account_delete_otp);

            if (dialog.getWindow() != null) {
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }

            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(false);

            TextView tvSubTitle = dialog.findViewById(R.id.subtitle);
            cvPrimaryOTP = dialog.findViewById(R.id.cv_primary);
            txPrimaryOTP = dialog.findViewById(R.id.tx_primary);
            spinKitView = dialog.findViewById(R.id.progress);

            //phone no
            String displayPhoneNumber = "+91 " + MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey());
            String message = getString(R.string.otp_sent_message, displayPhoneNumber);

            SpannableString spannable = new SpannableString(message);
            spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, displayPhoneNumber.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvSubTitle.setText(spannable);


            //otp
            resend = dialog.findViewById(R.id.resend_otp);
            ResendMessage = dialog.findViewById(R.id.Resend_Text);
            one = dialog.findViewById(R.id.text1);
            two = dialog.findViewById(R.id.text2);
            three = dialog.findViewById(R.id.text3);
            four = dialog.findViewById(R.id.text4);
            five = dialog.findViewById(R.id.text5);
            six = dialog.findViewById(R.id.text6);
            editTexts = new EditText[]{one, two, three, four, five, six};

            one.requestFocus();

            //start timer
            resend.setVisibility(View.GONE);
            startResendTimer();

            List<EditText> otpFields = Arrays.asList(one, two, three, four, five, six);
            one.addTextChangedListener(new MyOtpTextWatcher(one, null, two, otpFields));
            two.addTextChangedListener(new MyOtpTextWatcher(two, one, three, otpFields));
            three.addTextChangedListener(new MyOtpTextWatcher(three, two, four, otpFields));
            four.addTextChangedListener(new MyOtpTextWatcher(four, three, five, otpFields));
            five.addTextChangedListener(new MyOtpTextWatcher(five, four, six, otpFields));
            six.addTextChangedListener(new MyOtpTextWatcher(six, five, null, otpFields));

            one.setOnKeyListener(new PinOnKeyListener(0));
            two.setOnKeyListener(new PinOnKeyListener(1));
            three.setOnKeyListener(new PinOnKeyListener(2));
            four.setOnKeyListener(new PinOnKeyListener(3));
            five.setOnKeyListener(new PinOnKeyListener(4));
            six.setOnKeyListener(new PinOnKeyListener(5));

            //for btn verify
            TextWatcher autoVerifyWatcher = new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(android.text.Editable s) { checkAndAutoVerify(); }
            };

            for (EditText field : editTexts) field.addTextChangedListener(autoVerifyWatcher);

            setVerifyButtonEnabled(false);


            cvPrimaryOTP.setOnClickListener(view12 -> {
                Map<String, String> params = new HashMap<>();
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    CommonMethods.showNegativeSnackbar(rootLayout, MyApplication.getInstance().getString(R.string.NoInternetConnection));
                } else {
                    ansOtp = one.getText().toString().trim() + two.getText().toString().trim() + three.getText().toString().trim() +
                            four.getText().toString().trim() + five.getText().toString().trim() + six.getText().toString().trim();
                    Log.i("Respond otp ", " " + ansOtp);

                    if (ansOtp.length() == 6) {
                        spinKitView.setVisibility(View.VISIBLE);
                        params.put("phoneNumberOnBoarding", displayPhoneNumber);
                        params.put("VerifyMethod", "externalMethod");
                        validateOTP(ansOtp, dialog);
                    } else {
                        CommonMethods.showNegativeSnackbar(rootLayout, MyApplication.getInstance().getString(R.string.valid_otp));
                    }

                    params.put("otp_entered", ansOtp != null ? ansOtp : "");
                    params.put(PreferenceKeys.USER_ONBOARDING_LOGIN_PHONE_NUMBER.getKey(), displayPhoneNumber);
                }
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.addtl_set_del_acc_otp_verify_btn_click.getActionName(), params);
            });


            resend.setOnClickListener(view13 -> {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    CommonMethods.showNegativeSnackbar(rootLayout, MyApplication.getInstance().getString(R.string.NoInternetConnection));
                } else {
                    spinKitView.setVisibility(View.VISIBLE);
                    resend.setVisibility(View.GONE);
                    ResendMessage.setVisibility(View.GONE);
                    resendOtpAgain();

                    Map<String, String> params = new HashMap<>();
                    params.put(PreferenceKeys.USER_ONBOARDING_LOGIN_PHONE_NUMBER.getKey(), displayPhoneNumber);
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.addtl_set_del_acc_otp_resend_btn_click.getActionName(), params);

                }
            });

            ImageView ivClose = dialog.findViewById(R.id.iv_close);
            ivClose.setOnClickListener(v -> {
                Map<String,String> params = new HashMap<>();
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.addtl_set_del_acc_otp_dialog_close_click.getActionName(), params);
                dialog.dismiss();
            });

            dialog.show();

        } catch (Exception e) {
            Log.e(TAG, "error = " + e.getMessage());
        }
    }


    private void checkAndAutoVerify() {
        String otp = one.getText().toString().trim() + two.getText().toString().trim() + three.getText().toString().trim()
                + four.getText().toString().trim() + five.getText().toString().trim() + six.getText().toString().trim();

        setVerifyButtonEnabled(otp.length() == 6);
    }


    private void setVerifyButtonEnabled(boolean enabled) {
        cvPrimaryOTP.setEnabled(enabled);

        if (enabled) {
            cvPrimaryOTP.setCardBackgroundColor(ContextCompat.getColor(this, R.color.btn_enabled_bg));
            txPrimaryOTP.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        } else {
            cvPrimaryOTP.setCardBackgroundColor(ContextCompat.getColor(this, R.color.btn_disabled_bg));
            txPrimaryOTP.setTextColor(ContextCompat.getColor(this, R.color.delete_verify_btn_tx));
        }
    }



    public class PinOnKeyListener implements View.OnKeyListener {
        private final int currentIndex;

        PinOnKeyListener(int currentIndex) {
            this.currentIndex = currentIndex;
        }

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (editTexts[currentIndex].getText().toString().isEmpty() && currentIndex != 0) {
                    editTexts[currentIndex - 1].requestFocus();
                    editTexts[currentIndex - 1].setSelection(editTexts[currentIndex - 1].getText().length());
                }
            }
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                if (editTexts[currentIndex].getText().toString().length() == 1 && currentIndex == 5) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                }
            }
            return false;
        }

    }


    private void generateOtpBackend(String phoneNumberStr) {
        Map<String, Object> map = new HashMap<>();
        map.put("phone", phoneNumberStr);

        String hashCode = MyApplication.getHashCode();
        map.put("hashCode", hashCode);
        map.put("accessKey", BuildConfig.API_KEY);
        map.put("retry", false);
        map.put("deleteAccountRequest", true);

        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(DELETE_ACCOUNT_OTP_GENERATE, map), null,
                response -> {
                    Log.d(TAG, "onResponse: api call ");
                    CommonMethods.showPositiveSnackbar(rootLayout, MyApplication.getInstance().getString(R.string.otp_sent));
                    Bundle bundle = new Bundle();
                    bundle.putString(PreferenceKeys.NUMBER_UPPER.getKey(), phoneNumberStr);
                    VerifyNumberFragment verifyNumberFragment = new VerifyNumberFragment();
                    verifyNumberFragment.setArguments(bundle);
                    Map<String, String> params = new HashMap<>();
                    params.put("phone", phoneNumberStr);
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.acc_del_otp_generation_success.getActionName(), params);
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                Log.d("debugging", "onResponse: api fail ");
                spinKitView.setVisibility(View.GONE);
                Map<String, String> params = new HashMap<>();
                params.put("phone", phoneNumberStr);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.acc_del_otp_generation_failed.getActionName(), params);
                CommonMethods.showNegativeSnackbar(rootLayout, MyApplication.getInstance().getString(R.string.got_some_issue));
            }
        });
        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
    }


    private void resendOtpAgain() {
        String phoneNumber = MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey());

        spinKitView.setVisibility(View.VISIBLE);
        resend.setVisibility(View.GONE);

        Map<String, Object> map = new HashMap<>();
        map.put("phone", phoneNumber);
        map.put("hashCode", MyApplication.getHashCode());
        map.put("accessKey", BuildConfig.API_KEY);
        map.put("retry", true);
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GENERATE_OTP, map), null,
                response -> {
                    spinKitView.setVisibility(View.GONE);
                    resend.setVisibility(View.VISIBLE);
                    ResendMessage.setVisibility(View.VISIBLE);
                    startResendTimer();
                    CommonMethods.showPositiveSnackbar(rootLayout, MyApplication.getInstance().getString(R.string.otp_sent));
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                spinKitView.setVisibility(View.GONE);
                resend.setVisibility(View.VISIBLE);
                ResendMessage.setVisibility(View.VISIBLE);
                CommonMethods.showNegativeSnackbar(rootLayout, MyApplication.getInstance().getString(R.string.got_some_issue));
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
    }


    private void startResendTimer() {
        spinKitView.setVisibility(View.GONE);
        resend.setVisibility(View.GONE);
        ResendMessage.setVisibility(View.VISIBLE);

        timerResendOtp();
    }

    private void stopResendTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    private void timerResendOtp() {
        if (countDownTimer != null) countDownTimer.cancel();
        remainingTime = 60000L;

        countDownTimer = new CountDownTimer(remainingTime, 1000) {

            @SuppressLint({"SetTextI18n", "StringFormatInvalid"})
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTime = millisUntilFinished;
                int seconds = (int) ((millisUntilFinished + 999) / 1000);
                ResendMessage.setText(getString(R.string.resend_otp_after, seconds));
            }

            @Override
            public void onFinish() {
                remainingTime = 60000L;
                countDownTimer = null;
                MyApplication.saveData(PreferenceKeys.OTP.getKey(), null);
                resend.setVisibility(View.VISIBLE);
                ResendMessage.setText(getString(R.string.didn_t_receive_otp));
            }
        }.start();
    }


    public void validateOTP(String otp, Dialog dialog) {
        String phoneNumber = MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey());

        Map<String, Object> map = new HashMap<>();
        map.put("phone", phoneNumber);
        map.put("accessKey", BuildConfig.API_KEY);
        map.put("otp", otp);
        map.put("deleteAccountValidation", true);

        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(DELETE_ACCOUNT_OTP_VALIDATE, map), null,
                response -> {
                    CommonMethods.publishScreenVisitEvent(phoneNumber, "OTP_PASSED", PUBLISH_SCREEN_VISIT_EVENT);

                    Gson gson = new Gson();
                    TypeToken<ApiResponse> token = new TypeToken<>() {};
                    ApiResponse apiResponse = gson.fromJson(response.toString(), token.getType());

                    //event
                    Map<String, String> eventParams = new HashMap<>();
                    eventParams.put("otp", otp);
                    eventParams.put("phone", phoneNumber);

                    spinKitView.setVisibility(View.GONE);

                    if (!apiResponse.isSuccess()) {
                        CommonMethods.showNegativeSnackbar(rootLayout, apiResponse.getMessage());
                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.acc_del_otp_verified_failed.getActionName(), eventParams);
                        return;
                    }

                    try {
                        if (dialog != null && dialog.isShowing()) dialog.dismiss();
                    } catch (IllegalArgumentException ignored) {}

                    //final delete dialog
                    showDeleteAccountDialog();

                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.acc_del_otp_verified.getActionName(), eventParams);

                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                CommonMethods.showNegativeSnackbar(rootLayout, error.getMessage());
                spinKitView.setVisibility(View.GONE);
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
    }



    //////////// OTP Flow END ///////////



    //dialog
    public JSONObject getAccountDeleteJson() {
        try {
            String json = MyApplication.getFirebaseRemoteConfig().getString("re_conform_delete_account_config");
            if (StringUtils.isEmpty(json)) return null;

            JSONObject root = new JSONObject(json);
            return root.optJSONObject("delete_account");
        } catch (Exception e) {
            Log.e(TAG, "error = " + e.getMessage());
            return null;
        }
    }


    public void showDeleteAccountDialog() {
        try {
            JSONObject popupData = getAccountDeleteJson();
            if (popupData == null) return;

            String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
            if (StringUtils.isEmpty(lang)) lang = "en";

            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_account_delete);

            if (dialog.getWindow() != null) {
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }

            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);

            TextView tvTitle = dialog.findViewById(R.id.title);
            TextView tvSubTitle = dialog.findViewById(R.id.subtitle);

            CardView cvPrimary = dialog.findViewById(R.id.cv_primary);
            TextView tvPrimaryBtn = dialog.findViewById(R.id.tx_primary);
            CardView cvSecondary = dialog.findViewById(R.id.cv_secondary);
            TextView tvSecondaryBtn = dialog.findViewById(R.id.tx_secondary);

            ImageView centerImg = dialog.findViewById(R.id.center_img);
            centerImg.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_circle_delete));

            JSONObject titleObj = popupData.optJSONObject("title");
            tvTitle.setText(titleObj != null ? titleObj.optString(lang) : "");

            JSONObject subTitleObj = popupData.optJSONObject("sub_title");
            tvSubTitle.setText(subTitleObj != null ? subTitleObj.optString(lang) : "");

            JSONObject primaryBtnObj = popupData.optJSONObject("primary_btn");
            tvPrimaryBtn.setText(primaryBtnObj != null ? primaryBtnObj.optString(lang) : "");

            JSONObject secondaryBtnObj = popupData.optJSONObject("secondary_btn");
            tvSecondaryBtn.setText(secondaryBtnObj != null ? secondaryBtnObj.optString(lang) : "");

            ImageView ivClose = dialog.findViewById(R.id.iv_close);
            ivClose.setOnClickListener(v -> {
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.del_acc_dialog_close_btn_click.getActionName());
                dialog.dismiss();
            });

            LinearLayout buttonLayout = dialog.findViewById(R.id.button_layout);
            buttonLayout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);

            cvPrimary.setOnClickListener(v -> {
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.del_acc_dialog_cancel_btn_click.getActionName());
                dialog.dismiss();
            });

            // Secondary Button (Delete Account)
            String finalLang = lang;
            cvSecondary.setOnClickListener(v -> {
                Map<String,String> params = new HashMap<>();
                params.put("totalUserActiveDay",String.valueOf(MyApplication.getLongData(PreferenceKeys.USER_ACTIVE_DAYS.getKey())));
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.account_delete.getActionName(),params);
                dialog.dismiss();

                //send delete account
                String reason;
                if (selectedReason != null) {
                    String selectedText = selectedReason.optJSONObject("title").optString(finalLang);
                    if ("Other".equalsIgnoreCase(selectedText)) {
                        String typedReason = etFeedbackEditText.getText().toString().trim();
                        reason = "Other / " + typedReason;
                    } else {
                        reason = selectedText;
                    }
                } else {
                    reason = etFeedbackEditText.getText().toString().trim();
                }

                deleteApiCall(reason);
            });

            dialog.show();

        } catch (Exception e) {
            Log.e(TAG, "error = " + e.getMessage());
        }
    }


    @Override
    public void onDestroy() {
        stopResendTimer();
        super.onDestroy();
    }


}