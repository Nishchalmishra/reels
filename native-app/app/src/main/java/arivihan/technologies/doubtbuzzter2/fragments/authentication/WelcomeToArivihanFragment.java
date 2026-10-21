package arivihan.technologies.doubtbuzzter2.fragments.authentication;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_REGISTER;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.PUBLISH_SCREEN_VISIT_EVENT;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.HomeScreen;
import arivihan.technologies.doubtbuzzter2.activity.OnboardingActivityNew;
import arivihan.technologies.doubtbuzzter2.common.BundleParamConstants;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.listener.FragmentChangeListener;
import arivihan.technologies.doubtbuzzter2.listener.onboarding.OnBoardingUIListener;
import arivihan.technologies.doubtbuzzter2.model.ArivihanLanguageEnum;
import arivihan.technologies.doubtbuzzter2.model.OnBoardingSelectionModel;
import arivihan.technologies.doubtbuzzter2.model.user.UserDetailsResponse;
import arivihan.technologies.doubtbuzzter2.model.user.UserRegisterRequest;
import arivihan.technologies.doubtbuzzter2.model.user.batch.BatchDetail;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;

public class WelcomeToArivihanFragment extends Fragment {

    public static final String TAG = WelcomeToArivihanFragment.class.getSimpleName();
    CardView next;
    EditText name;
    String userPhoneNumber, userId, email, userName;
    Bundle bundle = new Bundle();
    TextInputLayout name_card;
    FrameLayout frameLayout;
    FragmentChangeListener listener;
    OnBoardingUIListener onBoardingUIListener;
    private final Gson gson = new Gson();
    private ProgressDialog loading;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    String restrictedNames, restrictedMessage;

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (FragmentChangeListener) context;
            onBoardingUIListener = (OnBoardingUIListener) context;
        } catch (ClassCastException e) {
            Log.e("Error in ", e.getMessage(), e);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    // Permission Received
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_welcome_to_arivihan, container, false);

        frameLayout = view.findViewById(R.id.frame);
        bundle = this.getArguments();
        listener.playAnimation();
//        checkNotificationPermission();
        if (bundle != null) {
            try {
                userName = bundle.getString(PreferenceKeys.NAME.getKey());
                userPhoneNumber = bundle.getString(PreferenceKeys.PHONE_NUMBER.getKey());
                MyApplication.saveData(PreferenceKeys.PHONE_NUMBER.getKey(), userPhoneNumber);
                userId = bundle.getString(PreferenceKeys.USER_ID.getKey());
                if (bundle.getString("email") != null)
                    email = bundle.getString("email");

                if (userPhoneNumber != null && !userPhoneNumber.isEmpty()) {
                    FirebaseMessaging.getInstance()
                            .subscribeToTopic(userPhoneNumber)
                            .addOnSuccessListener(aVoid -> MyApplication.saveData(PreferenceKeys.SUBSCRIBED_FIREBASE_NOTIFICATION.getKey(), true));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        loading = new ProgressDialog(requireContext());
        loading.setCancelable(false);
        loading.setMessage(getString(R.string.please_wait));
        loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        CommonMethods.publishScreenVisitEvent(userPhoneNumber, "REGISTER_SCREEN", PUBLISH_SCREEN_VISIT_EVENT);

        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.scale);
        frameLayout.startAnimation(animation);

        next = view.findViewById(R.id.next_card);
        name_card = view.findViewById(R.id.name_card);
        name = view.findViewById(R.id.user_name);

        if (StringUtils.isNotEmpty(userName)) {
            name.setText(userName);
        } else {
            name.setText(MyApplication.getStringData(PreferenceKeys.USER_NAME_ENTERED_DURING_ONBOARDING.getKey()));
        }

        String regex = ".*[@#\\d].*";
        String restrictedNameRegex = "[^a-z0-9]";
        String errorMessage = getString(R.string.not_allowed_message);
        restrictedMessage = getString(R.string.username_restriction_message);
        boolean enableValidation = true;
        boolean enableUserNameRestrictionValidation = true;
        try {
            String configJson = MyApplication.getFirebaseRemoteConfig().getString("username_validation_config");
            JSONObject jsonObject = new JSONObject(configJson);
            regex = jsonObject.optString("regex", regex);
            errorMessage = jsonObject.optString("message", errorMessage);
            restrictedNameRegex = jsonObject.optString("restrictedRegex", restrictedNameRegex);
            restrictedMessage = jsonObject.optString("restrictedMessage", restrictedMessage);
            restrictedNames = jsonObject.optString("restrictedNames", restrictedNames);

            enableValidation = jsonObject.optBoolean("enableValidation", true);
            enableUserNameRestrictionValidation = jsonObject.optBoolean("enableUserNameRestrictionValidation", true);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String finalRegex = regex;
        String finalErrorMessage = errorMessage;
        boolean finalEnableValidation = enableValidation;

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isInvalidUserName(s.toString(), finalRegex, finalEnableValidation)) {
                    name_card.setError(getHtmlError(finalErrorMessage));
                } else {
                    name_card.setError(null);
                }
            }
        });

        boolean finalEnableUserNameRestrictionValidation = enableUserNameRestrictionValidation;
        String finalRestrictedNameRegex = restrictedNameRegex;
        String finalRestrictedNames = restrictedNames;
        next.setOnClickListener(view1 -> {
            userName = name.getText().toString().trim();

            Map<String, String> params = new HashMap<>();
            params.put(PreferenceKeys.NAME.getKey(), userName != null ? userName : "");

            if (userName.isEmpty()) {
                name_card.setError(getString(R.string.name_required_error));
                name_card.requestFocus();
                return;
            }

            if (isInvalidUserName(userName, finalRegex, finalEnableValidation)) {
                name_card.setError(getHtmlError(finalErrorMessage));
                name_card.requestFocus();
                return;
            }

            if (finalEnableUserNameRestrictionValidation && checkUserNameRestriction(userName, finalRestrictedNameRegex, finalRestrictedNames)) {
                String errorMsg = restrictedMessage.replace("{name}", userName);
                name_card.setError(errorMsg);
                name_card.requestFocus();
                return;
            }

            name_card.setError(null);

            params.put("Steps", "User_name_inserted");
            params.put(PreferenceKeys.USER_ONBOARDING_LOGIN_PHONE_NUMBER.getKey(), userPhoneNumber != null ? userPhoneNumber : "");
//            MyApplication.logFirebaseBundleCreation("User_onBoarding_process", params);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.onboarding_name_screen_next_btn_clicked.getActionName(), params);
            navigateToNextStep(userName);
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

        new Handler().postDelayed(() -> name_card.setVisibility(View.VISIBLE), 500);

        return view;
    }


    private void navigateToNextStep(String userName) {

        Log.d(TAG, "navigateToNextStep called");
        Log.d(TAG, "UserName: " + userName);
        Log.d(TAG, "PhoneNumber: " + userPhoneNumber);
        Log.d(TAG, "UserId: " + userId);

        if (CommonMethods.isOnline(requireContext())) {


            boolean isLocationScreenVisible =
                    MyApplication.getFirebaseRemoteConfig()
                            .getBoolean("onboarding_user_location_screen_visible_v5");

            Log.d(TAG, "RemoteConfig onboarding_user_location_screen_visible_v5 = "
                    + isLocationScreenVisible);

            if (isLocationScreenVisible) {

                Log.d(TAG, "Location screen is enabled → Navigating to Location Fragment");

                Bundle locationBundle = new Bundle();
                locationBundle.putString(PreferenceKeys.NAME.getKey(), userName);
                locationBundle.putString(PreferenceKeys.PHONE_NUMBER.getKey(), userPhoneNumber);
                locationBundle.putString(PreferenceKeys.USER_ID.getKey(), userId);
                locationBundle.putString("email", email);

                Log.d(TAG, "Location Bundle Created: " + locationBundle);

                OnBoardingUserLocationFragment fragment =
                        new OnBoardingUserLocationFragment();
                fragment.setArguments(locationBundle);

                // Save bundle in listener
                if (onBoardingUIListener != null) {
                    Log.d(TAG, "onBoardingUIListener is not null → Saving bundle");

                    onBoardingUIListener.saveWelcomeFragmentBundle(locationBundle);

                } else {
                    Log.w(TAG, "onBoardingUIListener is NULL ️ Cannot save bundle");
                }

                // Fragment transition
                if (listener != null) {
                    Log.d(TAG, "Listener is not null → Setting fragment");
                    listener.setFragment(fragment);
                } else {
                    Log.e(TAG, "Listener is NULL  Fragment cannot be opened");
                }

                return;
            }

            Log.d(TAG, "Location screen disabled → Moving to OnboardingActivityNew");

            Intent intent = new Intent(getActivity(), OnboardingActivityNew.class);

            MyApplication.saveData(PreferenceKeys.NAME.getKey(), userName);
            Log.d(TAG, "Saved name in SharedPref: " + userName);


            intent.putExtra("Name", userName);
            intent.putExtra(PreferenceKeys.PHONE_NUMBER.getKey(), userPhoneNumber);
            intent.putExtra(PreferenceKeys.USER_ID.getKey(), userId);

            Log.d(TAG, "Intent extras added: Name, number, userId");

            if (bundle != null && bundle.getString("email") != null) {
                intent.putExtra("email", bundle.getString("email"));
                Log.d(TAG, "Email found in bundle → Added to intent: "
                        + bundle.getString("email"));
            } else {
                Log.w(TAG, "Email not found in bundle");
            }

            Map<String, String> logBundle = new HashMap<>();
            logBundle.put("steps", "user_name_entered");
            logBundle.put("user_phone_number", userPhoneNumber);

            Log.d(TAG, "Logging Firebase Event: user_on_boarding_process → " + logBundle);

            MyApplication.logFirebaseBundleCreation(FirebaseEvents.user_on_boarding_process.getActionName(), logBundle);

            Log.d(TAG, "Saved onboarding pref key: USER_NAME_ENTERED_DURING_ONBOARDING");

            String matchingIds =
                    MyApplication.getFirebaseRemoteConfig()
                            .getString("on_boarding_selected_course_id_v2");

            String courseModel =
                    MyApplication.getDoNotDeleteSharedPrefKey(PreferenceKeys.DO_NOT_DELETE_SELECTED_COURSE_MODEL.getKey());

            Log.d(TAG, "RemoteConfig Matching IDs: " + matchingIds);
            Log.d(TAG, "User Selected Course: " + courseModel);

            OnBoardingSelectionModel onBoardingSelectionModel =
                    new Gson().fromJson(
                            courseModel,
                            OnBoardingSelectionModel.class
                    );

            if (Arrays.asList(matchingIds.split(",")).contains(onBoardingSelectionModel.getId())) {

                Log.d(TAG, "CourseId matched  Completing Competitive Exam Registration");

                completeCompetitiveExamRegistration(onBoardingSelectionModel);

            } else {

                Log.d(TAG, "CourseId not matched → Starting Activity Normally");

                MyApplication.saveData(
                        PreferenceKeys.USER_NAME_ENTERED_DURING_ONBOARDING.getKey(),
                        userName
                );
                startActivity(intent);
            }

        } else {

            Log.e(TAG, "No Internet Connection Showing Snackbar");

            CommonMethods.showNegativeSnackbar(
                    requireView(),
                    getString(R.string.error_no_internet_connection)
            );
        }
    }


    private boolean checkUserNameRestriction(String name, String regex, String restrictedNamesStr) {
        try {

            Log.d(TAG, "--> " + name);
            Log.d(TAG, "--> " + regex);
            Log.d(TAG, "--> " + restrictedNamesStr);
            String cleaned = normalize(name, regex);

//            String restrictedStr = MyApplication.getFirebaseRemoteConfig().getString("onboarding_restricted_names");
            String[] restrictedNames = restrictedNamesStr.split("::");

            for (String keyword : restrictedNames) {
                String normalizedKeyword = normalize(keyword == null ? "" : keyword.trim(), regex);
                if (!normalizedKeyword.isEmpty() && cleaned.contains(normalizedKeyword)) {
                    return true;
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private String normalize(String input, String regex) {
        return input
                .toLowerCase()
                .replaceAll(StringUtils.isNotEmpty(regex) ? regex : "[^a-z0-9]", ""); // keep only letters + numbers
    }

    private boolean isInvalidUserName(String input, String regex, boolean enableValidation) {
        return enableValidation && regex != null && !regex.isEmpty() && input.matches(regex);
    }

    private Spanned getHtmlError(String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(message);
        }
    }

    private void completeCompetitiveExamRegistration(OnBoardingSelectionModel onBoardingSelectionModel) {
        try {
            if (loading != null && !loading.isShowing()) loading.show();
            String userName = "";
            if (StringUtils.isEmpty(MyApplication.getStringData(PreferenceKeys.NAME.getKey()))) {
                userName = MyApplication.getStringData(PreferenceKeys.USER_NAME_ENTERED_DURING_ONBOARDING.getKey());
            } else {
                userName = MyApplication.getStringData(PreferenceKeys.NAME.getKey());
            }
            if (userPhoneNumber == null) {
                userPhoneNumber = MyApplication.getStringData(PreferenceKeys.
                        USER_PHONE_NUMBER_ENTERED_DURING_ONBOARDING.getKey());
            }
            UserRegisterRequest userRegisterRequest = new UserRegisterRequest();
            userRegisterRequest.setUserName(userName);
            userRegisterRequest.setPhoneNumber(Long.parseLong(userPhoneNumber));
            userRegisterRequest.setExamCode(onBoardingSelectionModel.getExamCode());
            userRegisterRequest.setGradeCode(onBoardingSelectionModel.getGradeCode());
            userRegisterRequest.setStreamCode(onBoardingSelectionModel.getStreamCode());
            userRegisterRequest.setBatchCode(onBoardingSelectionModel.getBatchCode());

//            UserDto user = new UserDto();
//            user.setUserId(userId);
//            user.setUsername(userName);
//            user.setPhoneNumber(Long.parseLong(userPhoneNumber));
//            user.setCourseId(courseId);
//            user.setSecondaryCourseId(courseId);
//            user.setSelectedCourseId(courseId);
//            CommonMethods.publishScreenVisitEvent(userPhoneNumber, "REGISTER_COMPLETE", PUBLISH_SCREEN_VISIT_EVENT);

            String requestBody = gson.toJson(userRegisterRequest);
//            Log.i("UTM_PARAMS_WELCOME_FRAG", "saveData: " + MyApplication.getStringDataFromDoNotDeleteSharedPreference(PreferenceKeys.ALL_UTM_PARAMS.getKey()));

            if (StringUtils.isNotEmpty(MyApplication.getStringDataFromDoNotDeleteSharedPreference(PreferenceKeys.ALL_UTM_PARAMS.getKey()))) {
                String utmString = MyApplication.getStringDataFromDoNotDeleteSharedPreference(PreferenceKeys.ALL_UTM_PARAMS.getKey());
                Type type = new TypeToken<Map<String, String>>() {}.getType();
                Map<String, String> utmMap = new Gson().fromJson(utmString, type);
                userRegisterRequest.setAllUtmParams(utmMap);
            }

//            String requestBody = gson.toJson(userRegisterRequest);
            Log.d(TAG, requestBody);

            MyApplication.updateAppLocale(requireContext(), ArivihanLanguageEnum.ENGLISH.getCode());
            String finalUserName = userName;
            String finalUserName1 = userName;
            String finalUserName2 = userName;
            MyJsonObjectRequest request = new MyJsonObjectRequest(
                    Request.Method.POST,
                    AV_REGISTER,
                    new JSONObject(requestBody),
                    response -> {
                        Log.d(TAG, "Registration response: " + response);
                        try {

                                Gson gson = new Gson();
                                UserDetailsResponse userDetailsResponse = gson.fromJson(response.toString(), UserDetailsResponse.class);

                                MyApplication.saveData(PreferenceKeys.ON_BOARDING_NUMBER.getKey(), userPhoneNumber);
                                MyApplication.saveData(PreferenceKeys.USER_ID.getKey(), userDetailsResponse.getUserName());
                                MyApplication.saveSharedPreferencesDoNotDelete(PreferenceKeys.DO_NOT_DELETE_SELECTED_COURSE_MODEL.getKey(), new Gson().toJson(onBoardingSelectionModel));

                                MyApplication.saveData(PreferenceKeys.USER_LOGGED_IN.getKey(), true);
                                MyApplication.removeOnboardingSharedPreferencesKey();
                                dismissLoader();

                                Intent intent = new Intent(requireActivity(), HomeScreen.class);
                                intent.putExtra("NewUser", true);
                                intent.putExtra("open_new_session_popup", true);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra(BundleParamConstants.LOCATION_SHOW_LOCATION_PERMISSION, true);

                                MyApplication.saveData(PreferenceKeys.LAUNCH_COUNT.getKey(), 1);
                                MyApplication.saveData(PreferenceKeys.ON_BOARDING_COMPLETED.getKey(), true);
                                logFirebaseOnBoardingEvent(userDetailsResponse);
                                String matchingIds = FirebaseRemoteConfig.
                                        getInstance().getString("competitive_selected_batch_id_v1");
                                if (Arrays.asList(matchingIds.split(","))
                                        .contains(onBoardingSelectionModel.getId())) {
//                                    MyApplication.saveData(PreferenceKeys.BOARD_SWITCH.getKey(), false);
                                }
                                Log.d(PreferenceKeys.BOARD_SWITCH.getKey(), "completeCompetitiveExamRegistration-boardSwitch: " + MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey()));
                                MyApplication.saveData(
                                        PreferenceKeys.USER_NAME_ENTERED_DURING_ONBOARDING.getKey(),
                                        finalUserName2
                                );

                                startActivity(intent);
                                requireActivity().finish();

                        } catch (Exception e) {
                            e.printStackTrace();
                            logOnboardingFailure(finalUserName, userPhoneNumber);
                            dismissLoader();
                        }
                    },
                    error -> {
                        logOnboardingFailure(finalUserName1, userPhoneNumber);
                        dismissLoader();
                        Toast.makeText(requireActivity(), error.getMessage() != null ? error.getMessage() : "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
            );

            MyVolleySingleton.getInstance(requireActivity()).addToRequestQueue(request);
        } catch (Exception ex) {
            ex.printStackTrace();
            dismissLoader();
        }
    }

    private void logFirebaseOnBoardingEvent(UserDetailsResponse user) {
        BatchDetail batchDetail = getActiveBatch(user.getBatches());
        Map<String, String> params = new HashMap<>();
        params.put("userName", userName);
        params.put("phone_number", String.valueOf(user.getPhoneNumber()));
        params.put("steps", "user_onBoarding_completed");
        params.put("active_batch_code", batchDetail!=null? batchDetail.getBatchCode() : "");
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.user_on_boarding_process_new.getActionName(), params);
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.neet_onboarding_completed.getActionName(), params);
    }

    private BatchDetail getActiveBatch(List<BatchDetail> batchDetailList) {
        for(BatchDetail batchDetail : batchDetailList) {
            if(Boolean.TRUE.equals(batchDetail.getActive())) {
                return batchDetail;
            }
        }
        return null;
    }

    private void dismissLoader() {
        if (loading != null && loading.isShowing() && isAdded()) {
            loading.dismiss();
        }
    }

    public static void logOnboardingFailure(String name, String num) {
        Map<String, String> params = new HashMap<>();
        params.put("userName", name);
        params.put("phone_number", num);
        params.put("Steps", "user_onBoarding_failed");
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.user_on_boarding_process_new.getActionName(), params);
        MyApplication.removeOnboardingSharedPreferencesKey();
    }


}