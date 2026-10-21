package arivihan.technologies.doubtbuzzter2.fragments.authentication;
import arivihan.technologies.doubtbuzzter2.reelsdemo.*;
import arivihan.technologies.doubtbuzzter2.BuildConfig;

import static android.app.Activity.RESULT_OK;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_GET_USER_BY_PHONE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GENERATE_OTP;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.PUBLISH_SCREEN_VISIT_EVENT;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.SIGN_IN;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.DeviceThresholdDialogDeviceListAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.event.ActionEventName;
import arivihan.technologies.doubtbuzzter2.enums.event.ActionEventScreen;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceMethods;
import arivihan.technologies.doubtbuzzter2.helper.DeviceInfoUtil;
import arivihan.technologies.doubtbuzzter2.helper.MyOtpTextWatcher;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.listener.FragmentChangeListener;
import arivihan.technologies.doubtbuzzter2.model.ApiResponse;
import arivihan.technologies.doubtbuzzter2.model.OnBoardingSelectionModel;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.model.PhoneDevicesDto;
import arivihan.technologies.doubtbuzzter2.model.batch.user.UserDetailsResponse;
import arivihan.technologies.doubtbuzzter2.volley.EventApiHelper;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;

public class VerifyNumberFragment extends Fragment {

    public static final String TAG = VerifyNumberFragment.class.getSimpleName();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ActivityResultLauncher<Intent> smsConsentLauncher;
    IntentFilter intentFilter;
    private BroadcastReceiver smsBroadcastReceiver;
    FirebaseRemoteConfig remoteConfig = MyApplication.getFirebaseRemoteConfig();
    private static final int SMS_CONSENT_REQUEST = 2;  // Set to an unused request code

    private final BroadcastReceiver smsVerificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
                Bundle extras = intent.getExtras();
                if (extras == null) return;

                Status smsRetrieverStatus = (Status) extras.get(SmsRetriever.EXTRA_STATUS);
                if (smsRetrieverStatus == null) return;

                switch (smsRetrieverStatus.getStatusCode()) {
                    case CommonStatusCodes.SUCCESS:
                        Intent consentIntent = extras.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT);
                        if (consentIntent != null) {
                            try {
                                if (isAdded()) {
                                    // Launch the intent using ActivityResultLauncher
                                    smsConsentLauncher.launch(consentIntent);
                                }
                            } catch (ActivityNotFoundException e) {
                                e.printStackTrace();
                            }
                        } else {
                            // Handle null consentIntent case
                        }
                        break;

                    case CommonStatusCodes.TIMEOUT:
                        // Handle timeout
                        break;

                    default:
                        // Handle any other status code
                        break;
                }
            }
        }
    };


    // verify button removed — logic moved to verifyOtp()
    FrameLayout frameLayout;
    SpinKitView spinKitView;
    public String phoneNumber = "";
    String userEmail = "";
    int TimeCounter = 60000;
    String ansOtp = "";
    int counter = 60;
    String verificationId = "";
    EditText one, two, three, four, five, six;
    private EditText[] editTexts;
    TextView number, resend, ResendMessage, tv_user_number, tv_otp_sent, edit_number;
    LinearLayout skip, ll_edit_phone_number, ll_edit, ll_resend_otp, ll_otp_layout;
    CardView migrate;
    CountDownTimer countDownTimer;
    String finalNumber = "";
    String uuid = "";
    NetworkStatusLiveData networkStatusLiveData;
    RelativeLayout ParentLayout;
    private LottieAnimationView lottieAnimationView;

    private ProgressDialog dialog;
    private static final int RC_SIGN_IN = 234;
    GoogleSignInClient mGoogleSignInClient;
    FragmentChangeListener listener;
    boolean isPhoneNumberVerifiedWithOTP = false;
    HashSet<String> selectedDevices;
    PhoneDevicesDto devices;
    Dialog deviceThresholdDialog;

    TextView termAndCondition;
    boolean isAutoDetected;
    private boolean isTokenRequestInProgress = false;
    ImageView iv_edit_icon;

    // OTP verification support
    private MaterialCardView cardOtpSupport;
    private TextView otp_support_text;
    private String supportNumber = "";

    // Bottom nav buttons
    private CardView btnBack;
    private CardView btnForward;
    private boolean isOtpValid = false;

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requireActivity().registerReceiver(smsVerificationReceiver, intentFilter, Context.RECEIVER_EXPORTED);
                    } else {
                        requireActivity().registerReceiver(smsVerificationReceiver, intentFilter);
                    }
                } catch (Exception e) {

                }
            }
        } catch (Exception e) {
            Log.d("Error", "error");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (!BuildConfig.REELS_DEMO) setSmsRetrieverClient(context);
        try {
            listener = (FragmentChangeListener) context;
        } catch (ClassCastException e) {
            Log.e("Error in ", e.getMessage(), e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (getContext() == null) {
            return;
        }
        try {
            if (smsBroadcastReceiver != null) {
                requireContext().unregisterReceiver(smsBroadcastReceiver);
            }
        } catch (Exception e) {
            Log.d("Error", "error");
        }
        try {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(otpReceiver);
        } catch (Exception e) {
            Log.d("Error", "error");
        }
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(otpReceiver,
                new IntentFilter("otp_received"));
    }

    private final BroadcastReceiver otpReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String otp = intent.getStringExtra("otp");
            Log.d("otpReceiver", "Received OTP: " + otp);
            if (otp != null) {
                if (isAdded()) {
                    automaticallySetOtp(otp);
                } else {
                    Log.w("OtpReceiver", "Fragment not attached, skipping OTP auto-fill.");
                }
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MyApplication.clearSharedPref();
        View view = inflater.inflate(R.layout.fragment_verify_number, container, false);

        Bundle bundle = this.getArguments();
        phoneNumber = bundle.getString(PreferenceKeys.NUMBER_UPPER.getKey());
        verificationId = bundle.getString("verificationId");
        ParentLayout = view.findViewById(R.id.ParentRelativeLayout);
        CommonMethods.publishScreenVisitEvent(phoneNumber, "OTP_SCREEN", PUBLISH_SCREEN_VISIT_EVENT);
        smsConsentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {

                        try {
                            Intent data = result.getData();
                            String message;
                            if (data != null) {
                                message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE);
                                String otpCode = extractDigits(message.trim());
                                String[] otpNumber = otpCode.split("");
                                if (otpCode.length() == 6) {
                                    one.setText(otpNumber[0]);
                                    two.setText(otpNumber[1]);
                                    three.setText(otpNumber[2]);
                                    four.setText(otpNumber[3]);
                                    five.setText(otpNumber[4]);
                                    six.setText(otpNumber[5]);
                                    six.setSelection(six.getText().length());

                                    spinKitView.setVisibility(View.VISIBLE);
                                    signIn(verificationId, otpCode);
                                }
                            }
                        } catch (Exception e) {

                        }
                        // send one time code to the server
                    }

                }
        );

        one = view.findViewById(R.id.text1);
        two = view.findViewById(R.id.text2);
        three = view.findViewById(R.id.text3);
        four = view.findViewById(R.id.text4);
        five = view.findViewById(R.id.text5);
        six = view.findViewById(R.id.text6);
        editTexts = new EditText[]{one, two, three, four, five, six};

        one.requestFocus();
        frameLayout = view.findViewById(R.id.frame);
        ResendMessage = view.findViewById(R.id.Resend_Text);
        tv_user_number = view.findViewById(R.id.tv_user_number);
        resend = view.findViewById(R.id.resend_otp);
        spinKitView = view.findViewById(R.id.progress);
        tv_otp_sent = view.findViewById(R.id.tv_otp_sent);
        edit_number = view.findViewById(R.id.edit_number);
        ll_edit_phone_number = view.findViewById(R.id.ll_edit_phone_number);
        ll_resend_otp = view.findViewById(R.id.ll_resend_otp);
        cardOtpSupport = view.findViewById(R.id.cardOtpSupport);
        otp_support_text = view.findViewById(R.id.otp_support_text);
        ll_otp_layout = view.findViewById(R.id.ll_otp_layout);
        btnBack = view.findViewById(R.id.btn_back);
        btnForward = view.findViewById(R.id.btn_forward);

        tv_user_number.setText("+91 " + phoneNumber);
        tv_user_number.setVisibility(View.VISIBLE);

        android.text.TextWatcher autoVerifyWatcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                updateForwardButtonState();
                checkAndAutoVerify();
            }
        };

        for (EditText field : editTexts) {
            field.addTextChangedListener(autoVerifyWatcher);
        }

        resend.setVisibility(View.GONE);
//        termAndCondition = view.findViewById(R.id.terms_and_conditions);

//        number.setText(phoneNumber);
//
//        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.fragment_animation_come);
//        frameLayout.startAnimation(animation);


        ll_edit = view.findViewById(R.id.ll_edit);
        iv_edit_icon = view.findViewById(R.id.iv_edit_icon);

        ll_edit_phone_number.post(this::syncTextSize);

        String fullText = "By logging in, you agree to Arivihan's Terms & Conditions ";
        String clickablePart = "Arivihan's Terms & Conditions ";

        SpannableString spannableString = new SpannableString(fullText);

        int start = fullText.indexOf(clickablePart);
        int end = start + clickablePart.length();

        spannableString.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        termAndCondition.setText(spannableString);

//        termAndCondition.setOnClickListener(v -> {
//            String url = MyApplication.getFirebaseRemoteConfig().getString("privacy_policy_url");
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setData(Uri.parse(url));
//            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
//                startActivity(intent);
//            }
//        });

        ll_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    countDownTimer.cancel();
                } catch (Exception e) {
                }
                listener.setFragment(new LogInWIthMobileNumberFragment());
                Map<String, String> params = new HashMap<>();
                params.put(PreferenceKeys.USER_ONBOARDING_LOGIN_PHONE_NUMBER.getKey(), phoneNumber != null ? phoneNumber : "");
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.onboarding_edit_number_btn_clicked.getActionName(), params);
                EventApiHelper.sendActionEvent(
                        ActionEventName.EDIT_PHONE_NUMBER_CLICK.getValue(),
                        PreferenceMethods.getSelectedOnBoardingId(),
                        phoneNumber != null ? phoneNumber : "",
                        "",
                        ActionEventScreen.PHONE_NUMBER.getValue());
            }
        });

        resend.setOnClickListener(view13 -> {
            if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                CommonMethods.showNegativeSnackbar(ParentLayout, MyApplication.getInstance().getString(R.string.NoInternetConnection));
            } else {
                EventApiHelper.sendActionEvent(
                        ActionEventName.RESEND_OTP_CLICK.getValue(),
                        PreferenceMethods.getSelectedOnBoardingId(),
                        phoneNumber != null ? phoneNumber : "",
                        "",
                        ActionEventScreen.OTP.getValue());
                spinKitView.setVisibility(View.VISIBLE);
                resend.setVisibility(View.GONE);
                ResendMessage.setVisibility(View.GONE);
                cardOtpSupport.setVisibility(View.GONE);
                if (remoteConfig.getBoolean("enable_otp_for_all")) {
                    resendOtpAgain(phoneNumber);
                } else {
                    String specificUsers = remoteConfig.getString("enable_otp_for_specific_users");
                    if (!specificUsers.isEmpty()) {
                        List<String> allowedNumbers = Arrays.asList(specificUsers.split(","));
                        if (allowedNumbers.contains(phoneNumber)) {
                            resendOtpAgain(phoneNumber);
                        } else {
                            resendOtp(phoneNumber);
                        }
                    } else {
                        resendOtp(phoneNumber);
                    }
                }

                Map<String, String> params = new HashMap<>();
                params.put(PreferenceKeys.USER_ONBOARDING_LOGIN_PHONE_NUMBER.getKey(), phoneNumber != null ? phoneNumber : "");
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.onboarding_resend_otp_btn_click.getActionName(), params);

            }
        });

        timerResendOtp();

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

        Map<String, String> eventParams = new HashMap<>();
        eventParams.put(PreferenceKeys.COURSE_NAME.getKey(), PreferenceMethods.getSelectedOnBoardingId());
        eventParams.put("phone", phoneNumber);
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.onboarding_otp_screen_view.getActionName(), eventParams);

        networkStatusLiveData = new NetworkStatusLiveData(requireContext());
        networkStatusLiveData.observe(getViewLifecycleOwner(), isOnline -> {
            if (isOnline) {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    CommonMethods.showPositiveSnackbar(ParentLayout, MyApplication.getInstance().getString(R.string.InternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
                }
            } else {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                    CommonMethods.showNegativeSnackbar(ParentLayout, MyApplication.getInstance().getString(R.string.NoInternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), false);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), true);
                }
            }
        });

        if (CommonMethods.isOnline(getActivity())) {
            if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
            }
        } else {
            if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
            }
        }
        String consent_number = MyApplication.getFirebaseRemoteConfig().getString("consent_number");
        if (StringUtils.isNotEmpty(consent_number)) {
            consent_number = MyApplication.getFirebaseRemoteConfig().getString("consent_number");
        } else {
            consent_number = "59029414";
        }
        SmsRetriever.getClient(getActivity()).startSmsUserConsent(consent_number /* or null */);
        intentFilter = new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION);
        //OTP Verification
        supportNumber = getSupportNumber();
        cardOtpSupport.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL, android.net.Uri.parse("tel:" + supportNumber));
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.onboarding_otp_support_card_click.getActionName());
            EventApiHelper.sendActionEvent(
                    ActionEventName.CONTACT_US_CLICK.getValue(),
                    PreferenceMethods.getSelectedOnBoardingId(),
                    phoneNumber != null ? phoneNumber : "",
                    "PROBLEM_IN_ONBOARDING",
                    ActionEventScreen.OTP.getValue());
            startActivity(intent);
        });

        // Bottom nav buttons
        setupBottomNavigation();
        if(BuildConfig.REELS_DEMO) tv_otp_sent.setText("SIMULATED OTP ? Code 123456 ? No SMS sent");

        return view;
    }
    private String getSupportNumber() {

        try {
            OnBoardingSelectionModel model = PreferenceMethods.getSelectedOnboardingModel();

            if (model == null) {
                return null;
            }

            String examCode = model.getId();
            String remoteJson = MyApplication.getFirebaseRemoteConfig().getString("otp_verification_numbers_v1");

            JSONObject json = new JSONObject(remoteJson);
            String number = json.optString(examCode, json.optString("DEFAULT", ""));
            return number;

        } catch (Exception e) {
            Log.e(TAG, "Error fetching support number", e);
            return "";
        }
    }


    private void hideKeybaord(View v) {
        if (getActivity() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        }
    }

    private void timerResendOtp() {
        countDownTimer = new CountDownTimer(TimeCounter, 1000) {
            public void onTick(long millisUntilFinished) {
                if (!isAdded()) return;
                ResendMessage.setText(
                        String.format(getString(R.string.resend_otp_after), counter)
                );
                counter--;
            }

            public void onFinish() {
                MyApplication.saveData(PreferenceKeys.OTP.getKey(), null);
                resend.setVisibility(View.VISIBLE);
                ResendMessage.setText(MyApplication.getInstance().getString(R.string.not_received_otp));
                cardOtpSupport.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    public class PinOnKeyListener implements View.OnKeyListener {

        private int currentIndex;

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
                    hideKeybaord(v);
                }
            }
            return false;
        }

    }

    private void resendOtpAgain(String phoneNumber) {
        if (BuildConfig.REELS_DEMO) { android.widget.Toast.makeText(requireContext(),"Simulated OTP remains 123456. No SMS sent.",android.widget.Toast.LENGTH_LONG).show(); timerResendOtp(); return; }
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
                    CommonMethods.showPositiveSnackbar(ParentLayout, MyApplication.getInstance().getString(R.string.otp_sent));
                }, new VolleyGlobalErrorListener(getActivity()) {
            @Override
            public void postError(VolleyError error) {
                spinKitView.setVisibility(View.GONE);
                resend.setVisibility(View.VISIBLE);
                ResendMessage.setVisibility(View.VISIBLE);
                startResendTimer();
                CommonMethods.showNegativeSnackbar(ParentLayout, MyApplication.getInstance().getString(R.string.got_some_issue));
            }
        });
        MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(myJsonObjectRequest2);
    }

    public void generateToken(String phoneNumber, String otp) {
        if (isTokenRequestInProgress) {
            Log.w(TAG, "generateToken: request already in progress, ignoring duplicate call");
            return;
        }
        isTokenRequestInProgress = true;
        Map<String, Object> map = new HashMap<>();
        map.put("phone", phoneNumber);
        map.put("accessKey", BuildConfig.API_KEY);
        map.put("otp", otp);
        map.put("deviceId",DeviceInfoUtil.getDeviceId(getContext()));
        map.put("fcmToken",MyApplication.getDoNotDeleteSharedPrefKey(PreferenceKeys.FCM_TOKEN.getKey()));

        Log.d("fcmTokenPassed", "register"+MyApplication.getDoNotDeleteSharedPrefKey(
                PreferenceKeys.FCM_TOKEN.getKey()));

        Log.d(TAG, "-requestBody: "+map);
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(SIGN_IN, map), null,
                response -> {
                    isTokenRequestInProgress = false;
                    CommonMethods.publishScreenVisitEvent(phoneNumber, "OTP_PASSED", PUBLISH_SCREEN_VISIT_EVENT);
                    Gson gson = new Gson();
                    TypeToken<ApiResponse> token = new TypeToken<>() {
                    };
                    ApiResponse apiResponse = gson.fromJson(response.toString(), token.getType());
                    Log.d("AuthToken", apiResponse.getToken());
                    signInAnonymously(apiResponse);

                    Map<String, String> eventParams = new HashMap<>();
                    eventParams.put("otp", otp);
                    eventParams.put("phone", phoneNumber);
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.onboarding_otp_verified.getActionName(), eventParams);

                }, new VolleyGlobalErrorListener(getActivity()) {
            @Override
            public void postError(VolleyError error) {
                isTokenRequestInProgress = false;
                CommonMethods.showNegativeSnackbar(ParentLayout, error.getMessage());
                spinKitView.setVisibility(View.GONE);
            }
        });
        MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(myJsonObjectRequest2);
    }

    private void setSmsRetrieverClient(Context context) {
        SmsRetrieverClient client = SmsRetriever.getClient(context);
        Task<Void> task = client.startSmsRetriever();

        task.addOnSuccessListener(aVoid -> {
            Log.d("SmsRetriever", "SMS Retriever started here.");
            startSmsListener(context); // Register the receiver
        });

        task.addOnFailureListener(e -> Log.e("SmsRetriever", "Failed to start SMS Retriever", e));
    }

    private void startSmsListener(Context context) {
        smsBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);
                        if (status != null) {
                            switch (status.getStatusCode()) {
                                case CommonStatusCodes.SUCCESS:
                                    String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                                    if (message != null) {
                                        Log.d("SmsRetriever123", "Received SMS: " + message);
                                        String verificationCode = extractVerificationCode(message);
                                        assert verificationCode != null;
                                        if (isAdded() && getActivity() != null) {
                                            automaticallySetOtp(verificationCode);
                                        }
                                        Log.d("SmsRetriever123", "Verification Code: " + verificationCode);
                                    }
                                    break;
                                case CommonStatusCodes.TIMEOUT:
                                    Log.e("SmsRetriever123", "SMS retrieval timed out.");
                                    break;
                            }
                        }
                    }
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.registerReceiver(smsBroadcastReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
        }
    }

    private String extractVerificationCode(String message) {
        Pattern pattern = Pattern.compile("\\d{6}");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }


    // Call this after setting text
    private void syncTextSize() {
        try {
            List<TextView> textViews = Arrays.asList(tv_otp_sent, tv_user_number, edit_number);

            float density = getResources().getDisplayMetrics().density;
            float scaledDensity = getResources().getDisplayMetrics().scaledDensity;
            int totalWidth = ll_edit_phone_number.getWidth();
            final float defaultTextSize = 16f;
            final float defaultIconSizeDp = 18f;
            float textSize = defaultTextSize;

            while (textSize > 10f) {
                float totalTextWidth = 0;
                for (TextView tv : textViews) {
                    Paint paint = new Paint(tv.getPaint());
                    paint.setTextSize(textSize * scaledDensity);
                    totalTextWidth += paint.measureText(tv.getText().toString());
                }

                // icon width + its end margin (5dp) + ll_edit start/end margins (4dp + 4dp), all in px
                float scaledIconSizePx = defaultIconSizeDp * density * (textSize / defaultTextSize);
                totalTextWidth += scaledIconSizePx + (5 + 4 + 4) * density;

                if (totalTextWidth <= totalWidth) break;
                textSize -= 0.5f;
            }

            for (TextView tv : textViews) {
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            }

            // scale the edit icon proportionally
            float scaledIconSizePx = defaultIconSizeDp * density * (textSize / defaultTextSize);
            int iconPx = Math.round(scaledIconSizePx);
            ViewGroup.LayoutParams params = iv_edit_icon.getLayoutParams();
            params.width = iconPx;
            params.height = iconPx;
            iv_edit_icon.setLayoutParams(params);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void resendOtp(String phoneNumber) {
        if (BuildConfig.REELS_DEMO) { android.widget.Toast.makeText(requireContext(),"Simulated OTP remains 123456. No SMS sent.",android.widget.Toast.LENGTH_LONG).show(); timerResendOtp(); return; }
        counter = 60;
        try {
            countDownTimer.cancel();
        } catch (Exception e) {
        }
        FragmentActivity activity = getActivity();
        if (activity == null) {
            CommonMethods.showNegativeSnackbar(ParentLayout, MyApplication.getInstance().getString(R.string.please_try_again));
            spinKitView.setVisibility(View.GONE);
            resend.setVisibility(View.VISIBLE);
            ResendMessage.setVisibility(View.VISIBLE);
            return;
        }
        PhoneAuthProvider.verifyPhoneNumber(
                PhoneAuthOptions
                        .newBuilder(FirebaseAuth.getInstance())
                        .setActivity(requireActivity())
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(0L, TimeUnit.SECONDS)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                CommonMethods.showLongNegativeSnackbar(ParentLayout, e.getMessage());
                                spinKitView.setVisibility(View.GONE);
                                resend.setVisibility(View.VISIBLE);
                                ResendMessage.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onCodeSent(@NonNull String otp, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                CommonMethods.showPositiveSnackbar(ParentLayout, MyApplication.getInstance().getString(R.string.otp_sent));
                                Log.i("code", otp);
                                verificationId = otp;
                                spinKitView.setVisibility(View.GONE);
                                resend.setVisibility(View.GONE);
                                ResendMessage.setVisibility(View.VISIBLE);
                                TimeCounter = 60000;
                                counter = 60;
                                timerResendOtp();
                            }
                        })
                        .build());

    }

    private void startResendTimer() {
        spinKitView.setVisibility(View.GONE);
        resend.setVisibility(View.GONE);
        ResendMessage.setVisibility(View.VISIBLE);
        TimeCounter = 60000;
        counter = 60;
        timerResendOtp();
    }

    private void signIn(String verificationId, String ansOtp) {
        PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(verificationId, ansOtp);
        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                CommonMethods.publishScreenVisitEvent(phoneNumber, "OTP_PASSED", PUBLISH_SCREEN_VISIT_EVENT);
                FirebaseUser user = task.getResult().getUser();
                String uuid = null;
                if (user != null) {
                    uuid = user.getUid();
                }
                try {
                    countDownTimer.cancel();
                } catch (Exception e) {
                }
                isPhoneNumberVerifiedWithOTP = true;
                Log.i("Task is ", " Successfull");

            } else {
                Log.i("Task is ", " Unsuccessfull");
                CommonMethods.showNegativeSnackbar(ParentLayout, MyApplication.getInstance().getString(R.string.auth_error));
                spinKitView.setVisibility(View.GONE);
                CommonMethods.publishScreenVisitEvent(phoneNumber, "OTP_FAILED", PUBLISH_SCREEN_VISIT_EVENT);
            }

        }).addOnFailureListener(e -> {
            CommonMethods.showLongNegativeSnackbar(ParentLayout, MyApplication.getInstance().getString(R.string.invalid_otp));
            spinKitView.setVisibility(View.GONE);
        });
    }

    private void getFcmToken() {
        MyApplication.getFCMToken(new MyApplication.TokenCallback() {
            @Override
            public void onTokenReceived(String token) {
                goToVernacularActivity(token);
                Log.d("AuthTokenOTP", "FCMTOKEN" + token);
            }

            @Override
            public void onTokenError(Exception e) {

            }
        });

    }

    private void goToVernacularActivity(String token) {
        //just a hack to not send alert in slack
        if (getActivity() != null) {
            Log.d("VolleyHeaderV", token);

            if (phoneNumber.length() > 10) {
                phoneNumber = StringUtils.substring(phoneNumber, 3);
            }

            listener.registered(true);
            MyApplication.clearSharedPref();
            MyApplication.saveData(PreferenceKeys.AUTH_TOKEN.getKey(), token);
//            Bundle bundle = new Bundle();
//            bundle.putBoolean("NewUser", false);
//            bundle.putString("phoneNumber", phoneNumber);
//            bundle.putString(PreferenceKeys.PHONE_NUMBER.getKey(), phoneNumber);
//            bundle.putString("uuid", uuid);
//            bundle.putString("email", userEmail);
//            bundle.putBoolean("fromVerificationFragment", true);
//            OnboardingLanguageSelectionFragment onboardingLanguageSelectionFragment = new OnboardingLanguageSelectionFragment();
//            onboardingLanguageSelectionFragment.setArguments(bundle);
//            listener.setFragment(onboardingLanguageSelectionFragment);
            //nextFragment();
            checkUserExistence();
        }
    }

    public void nextFragment() {
//        Bundle bundle = new Bundle();
//        bundle.putBoolean("NewUser", true);
//        bundle.putString("phoneNumber", phoneNumber);
//        bundle.putString(PreferenceKeys.PHONE_NUMBER.getKey(), phoneNumber);
//        bundle.putString(PreferenceKeys.USER_ID.getKey(), uuid);
//        bundle.putString("email", userEmail);
//        bundle.putBoolean("fromVerificationFragment", true);
//        Log.d("ONBOARDING_LANGUAGE_SELECTION", "onCreateView: " + bundle.toString());
//        //MyApplication.logFirebaseBundleCreation("Onboarding_language_selection", params);
//        WelcomeToArivihanFragment welcomeToArivihanFragment = new WelcomeToArivihanFragment();
//        welcomeToArivihanFragment.setArguments(bundle);
////        OnBoardingUserLocationFragment onBoardingUserLocationFragment = new OnBoardingUserLocationFragment();
////        onBoardingUserLocationFragment.setArguments(bundle);
//        listener.setFragment(welcomeToArivihanFragment);
//        MyApplication.saveData(PreferenceKeys.AUTHENTICATION_DONE.getKey(),true);
//        MyApplication.saveData(PreferenceKeys.USER_PHONE_NUMBER_ENTERED_DURING_ONBOARDING.getKey()
//                ,phoneNumber);

//        Intent intent = new Intent(getActivity(), OnboardingActivityNew.class);
//
////        intent.putExtra("Name", userName);
//        intent.putExtra(PreferenceKeys.PHONE_NUMBER.getKey(), phoneNumber);
//        intent.putExtra(PreferenceKeys.USER_ID.getKey(), uuid);
//
//        startActivity(intent);
//        getActivity().finish();
        if (listener != null) {
            listener.moveToOnBoardingActivity(phoneNumber, userEmail, uuid);
        }

    }

    public void checkUserExistence() {
        MyApplication.saveData(PreferenceKeys.OTP.getKey(), null);
        Map<String, Object> map = new HashMap<>();
        String finalNumber;
        if (this.getArguments() != null)
            finalNumber = phoneNumber;
        else {
            finalNumber = "";
        }
        if (StringUtils.isNotEmpty(finalNumber)) {
            map.put("phoneNumber", finalNumber);
            map.put("key", BuildConfig.API_KEY);
            Log.i(TAG, "checkUserExistence: " + map.toString());

            MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                    CommonMethods.getUrlParam(AV_GET_USER_BY_PHONE, map), null,
                    response -> {
                        Gson gson = new Gson();

                        Log.d(TAG, "User By Phone Response : " + response);

                        TypeToken<ApiWrappedResponse<UserDetailsResponse>> token =
                                new TypeToken<ApiWrappedResponse<UserDetailsResponse>>() {
                                };
                        ApiWrappedResponse<UserDetailsResponse> apiResponse =
                                gson.fromJson(response.toString(), token.getType());

                        Map<String, String> params = new HashMap<>();
                        params.put("user_phone_number", finalNumber);

                        if (!apiResponse.isSuccess() || apiResponse.getCode() == 404 || apiResponse.getData() == null) {
                            Log.e(TAG, "checkUserExistence: user not found (code=" + apiResponse.getCode() + "), redirecting to onboarding");
                            nextFragment();
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.user_onboarding_process.getActionName(), params);
                            return;
                        }

                        UserDetailsResponse user = apiResponse.getData();

                        MyApplication.saveUserProfileLocally(user);

                        MyApplication.saveData(PreferenceKeys.NUMBER_UPPER.getKey(), finalNumber);
                        MyApplication.saveData(PreferenceKeys.PHONE_NUMBER.getKey(), String.valueOf(finalNumber));
                        MyApplication.saveData(PreferenceKeys.USER_LOGGED_IN.getKey(), true);

//                        try {
//                            String examCode = MyApplication.getStringData(PreferenceKeys.EXAM_CODE.getKey());
//                            PreferenceMethods.setSelectedOnBoardingModelById(StringUtils.isNotEmpty(examCode) ? examCode : user.getBatches().get(0).getExamCode());
//                        } catch (Exception ex) {
//                            ex.printStackTrace();
//                        }

                        if (user.getUserDto() != null) {
                            MyApplication.saveData(PreferenceKeys.USER_ID.getKey(), user.getUserDto().getUserId());
                        }
                        MyApplication.saveData(PreferenceKeys.COUNT_POP_UP.getKey(), 0);
                        MyApplication.saveData(PreferenceKeys.BOARD_SWITCH.getKey(), true);

                        listener.moveToHomeScreen();

                        params.put("steps", "otp_verified");
                        params.put("user_on_boarding_status", "Registered");
                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.user_login_process.getActionName(), params);
                    }, new VolleyGlobalErrorListener(getContext()) {
                @Override
                public void postError(VolleyError error) {
                    Log.e("VERNACUALARLOG", "postError: " + error.getMessage());
                    try {
                    } catch (Exception e) {
                        Log.e("VERNACUALARLOG", "postError: " + e.getMessage());
                    }
                    nextFragment();
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("VERNACUALARLOG", "postError: " + error.getMessage());
                    try {
                    } catch (Exception e) {
                        Log.e("VERNACUALARLOG", "postError: " + e.getMessage());
                    }
                    nextFragment();
                }
            });
            myJsonObjectRequest2.setTag("ONBOARDING_LANGUAGE_SELECTION_FRAGMENT");
            MyVolleySingleton.getInstance(getContext()).addToRequestQueue(myJsonObjectRequest2);
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (null != dialog) {
            dialog.dismiss();
        }
        if (requestCode == RC_SIGN_IN) {
            //Getting the GoogleSignIn Task
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    String email = account.getEmail();
                    userEmail = email;
                    Log.i("Email : ", " " + email);
//                    nextFragment();
                } else {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            } catch (ApiException e) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                if (e.getMessage() != null) {
                    Log.d("Errrrror", e.getMessage());
                }
            }
        } else if (requestCode == SMS_CONSENT_REQUEST) {
            if (resultCode == RESULT_OK) {
                // Get SMS message content
                String message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE);
                // Extract one-time code from the message and complete verification
                // `sms` contains the entire text of the SMS message, so you will need
                // to parse the string.
                try {
                    String otpCode = extractDigits(message.trim());
                    String[] otpNumber = otpCode.split("");
                    if (otpCode.length() == 6) {
                        one.setText(otpNumber[0]);
                        two.setText(otpNumber[1]);
                        three.setText(otpNumber[2]);
                        four.setText(otpNumber[3]);
                        five.setText(otpNumber[4]);
                        six.setText(otpNumber[5]);
                        six.setSelection(six.getText().length());
                        spinKitView.setVisibility(View.VISIBLE);
                        signIn(verificationId, otpCode);
                    }
                } catch (Exception e) {

                }
                // send one time code to the server
            } else {
                // Consent canceled, handle the error ...
            }
        }
    }

    public String extractDigits(final String in) {
        final Pattern p = Pattern.compile("(\\d{6})");
        final Matcher m = p.matcher(in);
        if (m.find()) {
            return m.group(0).trim();
        }
        return "";
    }

    private void signInAnonymously(ApiResponse apiResponse) {
        if (isAdded() && getContext() != null) {
            FirebaseAuth.getInstance().signInAnonymously()
                    .addOnSuccessListener(authResult -> {
                        if (isAdded()) { // Recheck inside callback
                            goToVernacularActivity(apiResponse.getToken());
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), getString(R.string.connectivity_issue), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }


    private void showDeviceThresholdDialog(String phoneNumber, PhoneDevicesDto devices) {
        if (getContext() == null) {
            return;
        }
        deviceThresholdDialog = new Dialog(getContext());
        deviceThresholdDialog.setContentView(R.layout.dialog_device_threshold_reached);
        deviceThresholdDialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(getContext(),
                R.drawable.bachground_custom_dialogu_typeactivity));
        deviceThresholdDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ImageView closeBtn = deviceThresholdDialog.findViewById(R.id.close_btn);
        closeBtn.setOnClickListener(v -> {
            deviceThresholdDialog.dismiss();
        });

        RecyclerView deviceRecyclerView = deviceThresholdDialog.findViewById(R.id.devices_list);

        DeviceThresholdDialogDeviceListAdapter deviceListAdapter = new DeviceThresholdDialogDeviceListAdapter(devices.getDevices());
        CardView removeDevices = deviceThresholdDialog.findViewById(R.id.chat_feedback_skip);

        selectedDevices = new HashSet<>();

        deviceListAdapter.onDeviceSelected(deviceId -> {
            selectedDevices.addAll(deviceId);
            if (deviceId.size() > 0) {
                removeDevices.setEnabled(true);
                removeDevices.setCardBackgroundColor(getResources().getColor(R.color.arivihan_secondary_color));
            } else {
                removeDevices.setEnabled(false);
                removeDevices.setCardBackgroundColor(getResources().getColor(R.color.grey_500));
            }
        });

        deviceRecyclerView.setAdapter(deviceListAdapter);
        deviceRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        deviceThresholdDialog.setCanceledOnTouchOutside(false);

        removeDevices.setOnClickListener(V -> {
            deviceThresholdDialog.dismiss();
            Toast.makeText(MyApplication.getInstance(), "Successfully removed your device.", Toast.LENGTH_SHORT).show();
        });

        deviceThresholdDialog.show();
    }

    private void removeDevice() {
        PhoneDevicesDto updateDevices = new PhoneDevicesDto();
        for (PhoneDevicesDto.DeviceDetails device : devices.getDevices()) {
            if (!selectedDevices.contains(device.getDeviceId())) {
                updateDevices.addDevice(device);
            }
        }
        db.collection("deviceCountPerPhone")
                .document(phoneNumber)
                .set(updateDevices);

    }

    private void automaticallySetOtp(String verificationCode) {
        String[] otpNumber = verificationCode.trim().split("");
        if (verificationCode.length() == 6) {
            one.requestFocus();
            one.setText(otpNumber[0]);
            two.setText(otpNumber[1]);
            three.setText(otpNumber[2]);
            four.setText(otpNumber[3]);
            five.setText(otpNumber[4]);
            six.setText(otpNumber[5]);
            six.setSelection(six.getText().length());
            Map<String, String> params = new HashMap<>();
            params.put("phoneNumberOnBoarding", phoneNumber);
            params.put("automaticallyFetched", "Yes");
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.onboarding_otp_auto_detected.getActionName(), params);
            isAutoDetected = true;
            spinKitView.setVisibility(View.VISIBLE);
            generateToken(phoneNumber, verificationCode);

        }
    }

    private void checkAndAutoVerify() {
        String otp = one.getText().toString().trim()
                + two.getText().toString().trim()
                + three.getText().toString().trim()
                + four.getText().toString().trim()
                + five.getText().toString().trim()
                + six.getText().toString().trim();

        if (otp.length() == 6) {
            ansOtp = otp;

            if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                CommonMethods.showNegativeSnackbar(ParentLayout,
                        MyApplication.getInstance().getString(R.string.NoInternetConnection));
                return;
            }

            spinKitView.setVisibility(View.VISIBLE);

            Bundle bundle = this.getArguments();
            Map<String, String> params = new HashMap<>();

            if (bundle != null && bundle.getBoolean("firebaseOtp")) {
                params.put("VerifyMethod", "firbase");
                signIn(verificationId, ansOtp);
            } else {
                params.put("phoneNumberOnBoarding", phoneNumber);
                params.put("VerifyMethod", "externalMethod");
                generateToken(phoneNumber, ansOtp);
            }

            params.put("otp_entered", ansOtp);
            params.put(PreferenceKeys.USER_ONBOARDING_LOGIN_PHONE_NUMBER.getKey(), phoneNumber != null ? phoneNumber : "");
            MyApplication.logFirebaseBundleCreation(
                    FirebaseEvents.onboarding_otp_verify.getActionName(), params);

            if (!isAutoDetected) {
                MyApplication.logFirebaseBundleCreation(
                        FirebaseEvents.onboarding_otp_manual_entered.getActionName(), params);
            }
        }
    }

    // ── Bottom navigation ───────────────────────────────────────────────────

    private void setupBottomNavigation() {
        updateForwardButtonState();

        // Back → Phone Number screen
        btnBack.setOnClickListener(v -> {
            try { countDownTimer.cancel(); } catch (Exception ignored) {}
            if (listener != null) listener.setFragment(new LogInWIthMobileNumberFragment());
        });

        // Forward → verify OTP (only when 6 digits entered and not already verifying)
        btnForward.setOnClickListener(v -> {
            if (isOtpValid && !isTokenRequestInProgress) {
                MyApplication.logFirebaseBundleCreation(
                        FirebaseEvents.onboarding_otp_fwd_btn_click.getActionName(),
                        new HashMap<>());
                verifyOtp();
            }
        });
    }

    // Verifies OTP — extracted from the old verify button click listener
    private void verifyOtp() {
        if (BuildConfig.REELS_DEMO) {
            String code=one.getText().toString()+two.getText().toString()+three.getText().toString()+four.getText().toString()+five.getText().toString()+six.getText().toString();
            if(!DemoEnvironment.OTP.equals(code)){CommonMethods.showNegativeSnackbar(ParentLayout,"Simulated OTP is 123456. No real SMS is sent.");return;}
            if(isTokenRequestInProgress)return;isTokenRequestInProgress=true;
            NativeReelsService.get(requireContext()).authenticate(new NativeReelsService.Callback(){
                public void success(Object ignored){isTokenRequestInProgress=false;if(!isAdded())return;String id=NativeReelsService.get(requireContext()).userId();MyApplication.saveData(PreferenceKeys.USER_ID.getKey(),id);listener.moveToOnBoardingActivity(phoneNumber,"",id);}
                public void failure(String error){isTokenRequestInProgress=false;if(isAdded())CommonMethods.showNegativeSnackbar(ParentLayout,error);}
            });return;
        }
        Map<String, String> params = new HashMap<>();
        if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
            CommonMethods.showNegativeSnackbar(ParentLayout,
                    MyApplication.getInstance().getString(R.string.NoInternetConnection));
            MyApplication.logFirebaseBundleCreation(
                    FirebaseEvents.onboarding_otp_verify_btn_click.getActionName(), params);
            return;
        }

        ansOtp = one.getText().toString().trim() + two.getText().toString().trim()
                + three.getText().toString().trim() + four.getText().toString().trim()
                + five.getText().toString().trim() + six.getText().toString().trim();

        if (ansOtp.length() == 6) {
            spinKitView.setVisibility(View.VISIBLE);
            Bundle bundle = this.getArguments();
            if (bundle != null && bundle.getBoolean("firebaseOtp")) {
                params.put("VerifyMethod", "firbase");
                signIn(verificationId, ansOtp);
            } else {
                params.put("phoneNumberOnBoarding", phoneNumber);
                params.put("VerifyMethod", "externalMethod");
                generateToken(phoneNumber, ansOtp);
            }
        } else {
            CommonMethods.showNegativeSnackbar(ParentLayout,
                    MyApplication.getInstance().getString(R.string.valid_otp));
        }

        params.put("otp_entered", ansOtp != null ? ansOtp : "");
        params.put(PreferenceKeys.USER_ONBOARDING_LOGIN_PHONE_NUMBER.getKey(),
                phoneNumber != null ? phoneNumber : "");
        MyApplication.logFirebaseBundleCreation(
                FirebaseEvents.onboarding_otp_verify_btn_click.getActionName(), params);
    }

    // Toggles forward button: teal when 6 digits entered, grey otherwise
    private void updateForwardButtonState() {
        if (!isAdded() || btnForward == null) return;
        String otp = one.getText().toString().trim()
                + two.getText().toString().trim()
                + three.getText().toString().trim()
                + four.getText().toString().trim()
                + five.getText().toString().trim()
                + six.getText().toString().trim();
        isOtpValid = otp.length() == 6;
        btnForward.setCardBackgroundColor(
                ContextCompat.getColor(requireContext(),
                        isOtpValid ? R.color.subscription_blue : R.color.disabled_next_color));
        btnForward.setClickable(isOtpValid);
    }

}