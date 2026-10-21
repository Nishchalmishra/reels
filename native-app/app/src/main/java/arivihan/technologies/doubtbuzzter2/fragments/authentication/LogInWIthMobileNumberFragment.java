package arivihan.technologies.doubtbuzzter2.fragments.authentication;
import arivihan.technologies.doubtbuzzter2.reelsdemo.*;
import arivihan.technologies.doubtbuzzter2.BuildConfig;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GENERATE_OTP;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.PUBLISH_SCREEN_VISIT_EVENT;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.event.ActionEventName;
import arivihan.technologies.doubtbuzzter2.enums.event.ActionEventScreen;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceMethods;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.listener.FragmentChangeListener;
import arivihan.technologies.doubtbuzzter2.volley.EventApiHelper;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;

/**
 * Mobile Number Input Screen.
 * Back (bottom-left) → Goal Selection. Forward (bottom-right) → OTP (enabled on 10 digits).
 */
public class LogInWIthMobileNumberFragment extends Fragment {

    public static final String TAG = LogInWIthMobileNumberFragment.class.getSimpleName();

    FragmentChangeListener listener;

    // UI
    RelativeLayout ParentLayout;
    EditText phoneNumber;
    SpinKitView spinKitView;
    private CardView btnBack;
    private CardView btnForward;

    // State
    FirebaseAuth mAuth;
    FirebaseRemoteConfig remoteConfig = MyApplication.getFirebaseRemoteConfig();
    private boolean isInputValid = false;
    // Guards against TextWatcher firing OTP during programmatic setText (prefill)
    private boolean suppressAutoTrigger = false;

    // Legacy (kept for backward compat)
    private ActivityResultLauncher<IntentSenderRequest> someActivityResultLauncher;
    private boolean doubleBackToExitPressedOnce = false;
    private Handler backPressHandler = new Handler(Looper.getMainLooper());
    private Timer timer;

    /** TextView displaying the Terms & Conditions / Privacy Policy link */
    TextView terms_condition;

    /** Flag to prevent duplicate OTP API calls while one is already in flight */
    private boolean isApiTriggered;

    /** True when OTP was triggered via Android phone hint — skips MANUAL auth_initiated event */
    private boolean isHintInitiated = false;

    /** Default no-arg constructor required by the Fragment framework. */
    public LogInWIthMobileNumberFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (FragmentChangeListener) context;
        } catch (ClassCastException e) {
            Log.e(TAG, "Host must implement FragmentChangeListener", e);
        }
    }

    private boolean phoneHintShown = false;

    // Auto-fill phone hint popup — only once, after the fragment transition settles
    @Override
    public void onResume() {
        super.onResume();
        if (!phoneHintShown && listener != null) {
            phoneHintShown = true;
            // Delay until the fragment enter animation finishes, so the
            // phone-hint system activity doesn't blink over a mid-transition screen.
            View root = getView();
            if (root != null) {
                root.postDelayed(() -> {
                    if (isAdded() && listener != null) listener.showPhoneNumberPopUp();
                }, 600);
            } else {
                listener.showPhoneNumberPopUp();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log_in_w_ith_mobile_number, container, false);

        ParentLayout = view.findViewById(R.id.ParentRelativeLayout);
        spinKitView = view.findViewById(R.id.progress);
        terms_condition = view.findViewById(R.id.terms_condition);
        phoneNumber = view.findViewById(R.id.number);
        btnBack = view.findViewById(R.id.btn_back);
        btnForward = view.findViewById(R.id.btn_forward);

        mAuth = FirebaseAuth.getInstance();
        phoneNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        CommonMethods.publishScreenVisitEvent("start", "START", PUBLISH_SCREEN_VISIT_EVENT);

        // Reset state on fragment creation
        isApiTriggered = false;
        spinKitView.setVisibility(View.GONE);

        setTermsAndCondition();
        setupBottomNavigation();
        setupPhoneInputListeners();
        prefillSavedNumber();
        trackNetworkState();

        // Analytics: screen view
        Map<String, String> eventParams = new HashMap<>();
        eventParams.put(PreferenceKeys.COURSE_NAME.getKey(), PreferenceMethods.getSelectedOnBoardingId());
        MyApplication.logFirebaseBundleCreation(
                FirebaseEvents.onboarding_mobile_number_screen_view.getActionName(), eventParams);

        return view;
    }

    // ── Prefill ─────────────────────────────────────────────────────────────

    // Restores saved number when coming back from OTP screen.
    // Uses DoNotDelete prefs since regular prefs are cleared on OTP screen.
    private void prefillSavedNumber() {
        String saved = MyApplication.getDoNotDeleteSharedPrefKey(
                PreferenceKeys.USER_ONBOARDING_LOGIN_PHONE_NUMBER.getKey());
        if (saved != null && saved.length() == 10) {
            suppressAutoTrigger = true;
            phoneNumber.setText(saved);
            phoneNumber.setSelection(saved.length());
            suppressAutoTrigger = false;
            // Sync forward button state without triggering OTP
            updateForwardButtonState(true);
        }
    }

    // ── Bottom navigation ───────────────────────────────────────────────────

    private void setupBottomNavigation() {
        updateForwardButtonState(false);

        // Back → Goal Selection
        btnBack.setOnClickListener(v -> {
            if (requireActivity() instanceof arivihan.technologies.doubtbuzzter2.activity.UserAuthenticationActivity) {
                ((arivihan.technologies.doubtbuzzter2.activity.UserAuthenticationActivity) requireActivity())
                        .setFragmentNoAnim(new LoginCourseSelectionFragment());
            } else if (listener != null) {
                listener.setFragment(new LoginCourseSelectionFragment());
            }
        });

        // Forward → trigger OTP
        btnForward.setOnClickListener(v -> {
            if (isInputValid && !isApiTriggered) {
                MyApplication.logFirebaseBundleCreation(
                        FirebaseEvents.onboarding_mobile_fwd_btn_click.getActionName(),
                        new HashMap<>());
                logPhoneEnteredEvent();
                triggerOtp();
            }
        });
    }

    // Toggles forward button: teal when enabled, grey when disabled
    private void updateForwardButtonState(boolean enabled) {
        isInputValid = enabled;
        if (isAdded() && btnForward != null) {
            btnForward.setCardBackgroundColor(ContextCompat.getColor(requireContext(),
                    enabled ? R.color.subscription_blue : R.color.disabled_next_color));
            btnForward.setClickable(enabled);
        }
    }

    // ── Phone input ─────────────────────────────────────────────────────────

    private void setupPhoneInputListeners() {
        // Keyboard Done/Enter/Go/Next → trigger OTP
        phoneNumber.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE
                    || actionId == EditorInfo.IME_ACTION_GO
                    || actionId == EditorInfo.IME_ACTION_NEXT
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN)) {
                if (isInputValid && !isApiTriggered) {
                    logPhoneEnteredEvent();
                    triggerOtp();
                }
                return true;
            }
            return false;
        });

        // Text change → update forward button + auto-trigger on 10th digit
        phoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                boolean valid = s.length() == 10;
                updateForwardButtonState(valid);

                // Auto-trigger only on user typing, not on programmatic prefill
                if (valid && !suppressAutoTrigger && !isApiTriggered) {
                    logPhoneEnteredEvent();
                    triggerOtp();
                }
            }
        });
    }

    private void logPhoneEnteredEvent() {
        Map<String, String> eventParams = new HashMap<>();
        eventParams.put(PreferenceKeys.COURSE_NAME.getKey(), PreferenceMethods.getSelectedOnBoardingId());
        eventParams.put("phone", phoneNumber.getText().toString());
        MyApplication.logFirebaseBundleCreation(
                FirebaseEvents.onboarding_mobile_num_entered.getActionName(), eventParams);
    }

    // ── Terms & Conditions ──────────────────────────────────────────────────

    private void setTermsAndCondition() {
        String termsText = MyApplication.getFirebaseRemoteConfig().getString("terms And Condition");

        if (CommonMethods.isValidJson(termsText)) {
            terms_condition.setText(StyledTextBuilder.fromJson(requireActivity(), termsText));
        } else {
            String fullText = getString(R.string.terms_privacy);
            SpannableString spannable = new SpannableString(fullText);
            int color = ContextCompat.getColor(requireActivity(), R.color.arivihan_secondary_color);

            highlightText(spannable, fullText, "Terms & Condition", color);
            highlightText(spannable, fullText, "Privacy Policies", color);
            terms_condition.setText(spannable);
        }

        terms_condition.setOnClickListener(v -> {
            String url = MyApplication.getFirebaseRemoteConfig().getString("privacy_policy_url");
            CommonMethods.openExternalLink(requireActivity(), url);
        });
    }

    private void highlightText(SpannableString spannable, String fullText, String target, int color) {
        int start = fullText.indexOf(target);
        if (start >= 0) {
            spannable.setSpan(new ForegroundColorSpan(color),
                    start, start + target.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    // ── Network state ───────────────────────────────────────────────────────

    private void trackNetworkState() {
        boolean online = CommonMethods.isOnline(getActivity());
        MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), online);
        MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), !online);
    }

    // ── OTP generation ──────────────────────────────────────────────────────

    private void triggerOtp() {
        if (BuildConfig.REELS_DEMO) {
            String demoNumber=phoneNumber.getText().toString().trim();
            if(demoNumber.length()!=10)return;
            Bundle args=new Bundle();args.putString(PreferenceKeys.NUMBER_UPPER.getKey(),demoNumber);
            VerifyNumberFragment verify=new VerifyNumberFragment();verify.setArguments(args);listener.setFragment(verify);
            android.widget.Toast.makeText(requireContext(),"SIMULATED OTP: 123456. No SMS is sent.",android.widget.Toast.LENGTH_LONG).show();
            return;
        }
        try {
            if (isApiTriggered) return;

            String number = phoneNumber.getText().toString().trim();

            if (number.length() != 10) return;

            // Hide keyboard
            View currentFocus = requireActivity().getCurrentFocus();
            if (currentFocus != null) {
                android.view.inputmethod.InputMethodManager imm =
                        (android.view.inputmethod.InputMethodManager)
                                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                currentFocus.clearFocus();
            }

            // Save to DoNotDelete prefs — regular prefs are cleared on OTP screen
            MyApplication.saveSharedPreferencesDoNotDelete(
                    PreferenceKeys.USER_ONBOARDING_LOGIN_PHONE_NUMBER.getKey(), number);

            if (!CommonMethods.isOnline(getActivity())) {
                CommonMethods.showNegativeSnackbar(ParentLayout,
                        MyApplication.getInstance().getString(R.string.NoInternetConnection));
                return;
            }

            MyApplication.saveData(PreferenceKeys.OTP.getKey(), null);
            spinKitView.setVisibility(View.VISIBLE);

            Map<String, String> params = new HashMap<>();
            params.put("phoneNumberOnBoarding", number);

            if (remoteConfig.getBoolean("enable_otp_for_all")) {
                params.put("sourceOfOtp", "external otp provider");
                generateOtpBackend(number);
            } else {
                String specificUsers = remoteConfig.getString("enable_otp_for_specific_users");
                List<String> allowed = specificUsers.isEmpty()
                        ? List.of()
                        : Arrays.asList(specificUsers.split(","));
                if (allowed.contains(number)) {
                    params.put("sourceOfOtp", "external otp provider");
                    generateOtpBackend(number);
                } else {
                    params.put("sourceOfOtp", "firebase");
                    generateOtp(number);
                }
            }

            MyApplication.logFirebaseBundleCreation(
                    FirebaseEvents.otp_generation.getActionName(), params);
        } catch (Exception ex) {
            Log.e(TAG, "triggerOtp failed", ex);
            resetApiState();
        }
    }

    // Resets API state so user can retry after error
    private void resetApiState() {
        isApiTriggered = false;
        phoneNumber.setEnabled(true);
        spinKitView.setVisibility(View.GONE);
    }

    private void fireExamSelectedIfPending(String phoneNumberStr) {
        String pendingExamCode = MyApplication.getStringData(PreferenceKeys.EXAM_SELECTED_PENDING.getKey());
        if (pendingExamCode != null && !pendingExamCode.isEmpty()) {
            MyApplication.saveData(PreferenceKeys.EXAM_SELECTED_PENDING.getKey(), "");
            EventApiHelper.sendActionEvent(
                    ActionEventName.EXAM_SELECTED.getValue(),
                    null,
                    phoneNumberStr,
                    pendingExamCode,
                    ActionEventScreen.EXAM_SCREEN.getValue());
        }
    }

    private void generateOtpBackend(String phoneNumberStr) {
        Map<String, Object> map = new HashMap<>();
        map.put("phone", phoneNumberStr);
        map.put("hashCode", MyApplication.getHashCode());
        map.put("accessKey", BuildConfig.API_KEY);
        map.put("retry", false);

        phoneNumber.setEnabled(false);
        isApiTriggered = true;

        MyJsonObjectRequest request = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GENERATE_OTP, map), null,
                response -> {
                    if (!isAdded()) return;
                    resetApiState();
                    CommonMethods.showPositiveSnackbar(ParentLayout,
                            MyApplication.getInstance().getString(R.string.otp_sent));

                    if (!isHintInitiated) {
                        EventApiHelper.sendActionEvent(ActionEventName.AUTH_INITIATED.getValue(),
                                PreferenceMethods.getSelectedOnBoardingId(), phoneNumberStr, "MANUAL", ActionEventScreen.PHONE_SCREEN.getValue());
                    }
                    isHintInitiated = false;
                    fireExamSelectedIfPending(phoneNumberStr);

                    Bundle bundle = new Bundle();
                    bundle.putString(PreferenceKeys.NUMBER_UPPER.getKey(), phoneNumberStr);
                    VerifyNumberFragment verifyFragment = new VerifyNumberFragment();
                    verifyFragment.setArguments(bundle);
                    listener.setFragment(verifyFragment);

                    Map<String, String> params = new HashMap<>();
                    params.put("phone", phoneNumberStr);
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.onboarding_otp_generation_success.getActionName(), params);
                }, new VolleyGlobalErrorListener(getActivity()) {
            @Override
            public void postError(VolleyError error) {
                if (!isAdded()) return;
                resetApiState();
                isHintInitiated = false;

                Map<String, String> params = new HashMap<>();
                params.put("phone", phoneNumberStr);
                MyApplication.logFirebaseBundleCreation(
                        FirebaseEvents.onboarding_otp_generation_failed.getActionName(), params);
                CommonMethods.showNegativeSnackbar(ParentLayout,
                        MyApplication.getInstance().getString(R.string.got_some_issue));
            }
        });
        request.setTag(TAG);
        MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(request);
    }

    // Firebase OTP
    private void generateOtp(String phoneNumber) {
        final String finalNumber = "+91" + phoneNumber;
        FragmentActivity activity = getActivity();
        if (activity == null) {
            CommonMethods.showNegativeSnackbar(ParentLayout,
                    MyApplication.getInstance().getString(R.string.please_try_again));
            resetApiState();
            return;
        }
        isApiTriggered = true;

        try {
            FirebaseApp.initializeApp(activity);
            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance());
        } catch (Exception e) {
            Log.e(TAG, "Firebase init error", e);
        }

        PhoneAuthProvider.verifyPhoneNumber(
                PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                        .setActivity(activity)
                        .setPhoneNumber(finalNumber)
                        .setTimeout(0L, TimeUnit.SECONDS)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential cred) {
                                Log.d(TAG, "onVerificationCompleted: " + cred);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                if (!isAdded()) return;
                                try {
                                    CommonMethods.showLongNegativeSnackbar(ParentLayout, e.getMessage());
                                } catch (Exception ignored) {}
                                resetApiState();
                            }

                            @Override
                            public void onCodeSent(@NonNull String verificationId,
                                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                if (!isAdded()) return;
                                resetApiState();
                                if (!isHintInitiated) {
                                    EventApiHelper.sendActionEvent(ActionEventName.AUTH_INITIATED.getValue(),
                                            PreferenceMethods.getSelectedOnBoardingId(), phoneNumber, "MANUAL", ActionEventScreen.PHONE_SCREEN.getValue());
                                }
                                isHintInitiated = false;
                                fireExamSelectedIfPending(phoneNumber);
                                listener.setPhoneNumber(finalNumber);
                                CommonMethods.publishScreenVisitEvent(phoneNumber,
                                        "PHONE_NUMBER_FRAGMENT", PUBLISH_SCREEN_VISIT_EVENT);
                                CommonMethods.showPositiveSnackbar(ParentLayout,
                                        MyApplication.getInstance().getString(R.string.otp_sent));

                                Bundle bundle = new Bundle();
                                bundle.putString(PreferenceKeys.NUMBER_UPPER.getKey(), finalNumber);
                                bundle.putString("verificationId", verificationId);
                                bundle.putBoolean("firebaseOtp", true);
                                VerifyNumberFragment verifyFragment = new VerifyNumberFragment();
                                verifyFragment.setArguments(bundle);
                                listener.setFragment(verifyFragment);
                                super.onCodeSent(finalNumber, token);
                            }
                        })
                        .build());
    }

    // ── External callback (phone hint / Truecaller) ─────────────────────────

    public void onPhoneNumberReceived(String phone) {
        if (phone != null && phone.length() >= 10) {
            Map<String, String> eventParams = new HashMap<>();
            eventParams.put(PreferenceKeys.COURSE_NAME.getKey(), PreferenceMethods.getSelectedOnBoardingId());
            eventParams.put("phone", phone);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.onboarding_mobile_auto_proceed_otp.getActionName(), eventParams);

            isHintInitiated = true;
            phoneNumber.setText(phone);
            triggerOtp();
        }
    }

    // ── Cleanup ─────────────────────────────────────────────────────────────

    @Override
    public void onDestroy() {
        if (timer != null) { timer.cancel(); timer = null; }
        if (someActivityResultLauncher != null) someActivityResultLauncher.unregister();
        try { MyVolleySingleton.getInstance(requireContext()).getRequestQueue().cancelAll(TAG); }
        catch (Exception ignored) {}
        super.onDestroy();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
    }

    public void setFragment(Fragment fragment) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        if (!fm.isDestroyed()) {
            fm.beginTransaction().add(R.id.starting_frame, fragment).commit();
        }
    }
}
