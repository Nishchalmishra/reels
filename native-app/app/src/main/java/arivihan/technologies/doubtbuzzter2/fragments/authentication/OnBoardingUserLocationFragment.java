package arivihan.technologies.doubtbuzzter2.fragments.authentication;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.REGISTER;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.HomeScreen;
import arivihan.technologies.doubtbuzzter2.activity.OnboardingActivityNew;
import arivihan.technologies.doubtbuzzter2.adapter.onboarding.PlaceSuggestionAdapter;
import arivihan.technologies.doubtbuzzter2.common.BundleParamConstants;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.customView.textview.SimpleTextWatcher;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceMethods;
import arivihan.technologies.doubtbuzzter2.listener.FragmentChangeListener;
import arivihan.technologies.doubtbuzzter2.listener.onboarding.OnBoardingUIListener;
import arivihan.technologies.doubtbuzzter2.model.ArivihanLanguageEnum;
import arivihan.technologies.doubtbuzzter2.model.UserDto;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;

public class OnBoardingUserLocationFragment extends Fragment {

    public static String TAG = OnBoardingUserLocationFragment.class.getSimpleName();

    FragmentChangeListener listener;
    OnBoardingUIListener onBoardingUIListener;
    private FrameLayout fl_parent_layout;
    private TextView tv_location;
    private TextInputLayout tl_location;
    private TextInputEditText et_location;
    private CardView cv_next;
    private RecyclerView rv_location;

    private PlacesClient placesClient;
    private AutocompleteSessionToken sessionToken;

    private PlaceSuggestionAdapter adapter;
    private CardView cv_suggestions;
    private ProgressBar pb_loader;
    private TextView tv_next;
    private Dialog dialogLoading;
    // debounce handler
    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private final int DEBOUNCE_MS = 300;

    // track the last query to avoid flicker
    private final AtomicReference<String> lastQuery = new AtomicReference<>("");
    private Place selectedPlace = null;
    private String selectedPlaceId = null;
    private double selectedPlaceLat, selectedPlaceLong;
    private String selectedAddress;

    private String userName, userPhoneNumber, userId, email;
    private boolean isSelectingPlace = false;

    public OnBoardingUserLocationFragment() {
    }

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_on_boarding_user_location, container, false);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            userName = bundle.getString(PreferenceKeys.NAME.getKey());
            userPhoneNumber = bundle.getString(PreferenceKeys.PHONE_NUMBER.getKey());
            userId = bundle.getString(PreferenceKeys.USER_ID.getKey());

            Log.d(TAG, "User: " + userName+", "+userPhoneNumber+", "+userId);
        }
        listener.playAnimation();
        fl_parent_layout = view.findViewById(R.id.fl_parent_layout);
        tv_location = view.findViewById(R.id.tv_location);
        tl_location = view.findViewById(R.id.tl_location);
        et_location = view.findViewById(R.id.et_location);
        cv_next = view.findViewById(R.id.cv_next);
        rv_location = view.findViewById(R.id.rv_location);
        cv_suggestions = view.findViewById(R.id.cv_suggestions);
        pb_loader = view.findViewById(R.id.pb_loader);
        tv_next = view.findViewById(R.id.tv_next);

        dismissLoader();
        // init places
        if (!Places.isInitialized()) {
            Places.initialize(getActivity().getApplicationContext(), BuildConfig.PLACES_API_KEY);
        }
        placesClient = Places.createClient(requireContext());
        sessionToken = AutocompleteSessionToken.newInstance();

        adapter = new PlaceSuggestionAdapter(this::onPlaceClicked);
        rv_location.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv_location.setAdapter(adapter);

        et_location.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isSelectingPlace) {
                    return;
                }
                final String q = s == null ? "" : s.toString().trim();
                selectedPlace = null;
                debounceHandler.removeCallbacksAndMessages(null);
                if (q.length() > 2) {
                    debounceHandler.postDelayed(() -> {
                        // avoid duplicate calls for same query
                        if (!q.equals(lastQuery.get())) {
                            lastQuery.set(q);
                            fetchPredictions(q);
                        }
                    }, DEBOUNCE_MS);
                } else {
                    lastQuery.set("");
                    hideSuggestions();
                }
            }
        });



        et_location.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) hideSuggestions();
        });

        fl_parent_layout.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (!isTouchInsideView(et_location, event) && !isTouchInsideView(rv_location, event)) {
                    hideSuggestionsAndKeyboard();
                }
            }
            return false;
        });

        cv_next.setOnClickListener(view1 -> {
            try {
                hideKeyboard();
                hideSuggestions();
                tl_location.setError(null);

                String typedText = et_location.getText() != null ? et_location.getText().toString().trim() : "";

                if (selectedPlace == null) {
                    CommonMethods.showPositiveSnackbar(fl_parent_layout, "Please ek location select karein", 3000);
//                    Toast.makeText(requireContext(), "Please ek location select karein", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Proceed with your logic using selectedPlace
//                double lat = Objects.requireNonNull(selectedPlace.getLatLng()).latitude;
//                double lng = Objects.requireNonNull(selectedPlace.getLatLng()).longitude;
//                String address = selectedPlace.getAddress();

                navigateToNextStep();

            } catch (Exception ex) {
                ex.printStackTrace();
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

        new Handler().postDelayed(() -> tl_location.setVisibility(View.VISIBLE), 500);

        return view;
    }

    public void hideSuggestionsAndKeyboard() {
        hideSuggestions();
        hideKeyboard();
    }
    private void navigateToNextStep() {
        if(CommonMethods.isOnline(requireContext())) {
            Intent intent = new Intent(getActivity(), OnboardingActivityNew.class);
            MyApplication.saveData(PreferenceKeys.NAME.getKey(), userName);
            intent.putExtra("Name", userName);
            intent.putExtra(PreferenceKeys.PHONE_NUMBER.getKey(), userPhoneNumber);
            intent.putExtra(PreferenceKeys.USER_ID.getKey(), userId);
//            intent.putExtra("email", email);
            intent.putExtra("lat", selectedPlaceLat);
            intent.putExtra("long", selectedPlaceLong);
            intent.putExtra("address", selectedAddress);

            String matchingIds = FirebaseRemoteConfig.
                    getInstance().getString("on_boarding_selected_course_id_v2");
            String courseId = PreferenceMethods.getSelectedOnBoardingId();
            if (Arrays.asList(matchingIds.split(","))
                    .contains(courseId)) {
                completeCompetitiveExamRegistration(courseId);
            } else {
                startActivity(intent);
            }
        } else {
            CommonMethods.showNegativeSnackbar(requireView(), getString(R.string.error_no_internet_connection));
        }
    }

    private void completeCompetitiveExamRegistration(String courseId) {
        try {

            showLoadingDialog();

            UserDto user = new UserDto();
            user.setUserId(userId);
            user.setUsername(userName);
            user.setPhoneNumber(Long.parseLong(userPhoneNumber));
            user.setCourseId(courseId);
            user.setSecondaryCourseId(courseId);
            user.setSelectedCourseId(courseId);
            user.setLatitude(selectedPlaceLat);
            user.setLongitude(selectedPlaceLong);
            user.setAddress(selectedAddress);
//            CommonMethods.publishScreenVisitEvent(userPhoneNumber, "REGISTER_COMPLETE", PUBLISH_SCREEN_VISIT_EVENT);

            if (StringUtils.isNotEmpty(MyApplication.getStringDataFromDoNotDeleteSharedPreference(PreferenceKeys.ALL_UTM_PARAMS.getKey()))) {

                String utmString = MyApplication.getStringDataFromDoNotDeleteSharedPreference(PreferenceKeys.ALL_UTM_PARAMS.getKey());

                Type type = new TypeToken<Map<String, String>>() {}.getType();
                Map<String, String> utmMap = new Gson().fromJson(utmString, type);
                user.setAllUtmParams(utmMap);
            }

            Gson gson = new Gson();
            String requestBody = gson.toJson(user);
            Log.d(TAG, requestBody);

            Log.d(TAG, "Register Request: "+user.toString());
            MyApplication.updateAppLocale(requireContext(), ArivihanLanguageEnum.ENGLISH.getCode());
            MyJsonObjectRequest request = new MyJsonObjectRequest(
                    Request.Method.POST,
                    REGISTER,
                    new JSONObject(requestBody),
                    response -> {
                        Log.d(TAG, "Registration response: " + response);
                        try {
                            if (response.has("success") &&
                                    Boolean.parseBoolean(response.getString("success"))) {
                                MyApplication.saveData(PreferenceKeys.ON_BOARDING_NUMBER.getKey(), userPhoneNumber);
                                MyApplication.saveData(PreferenceKeys.USER_ID.getKey(), response.getString("message"));
                                MyApplication.saveData(PreferenceKeys.SELECTED_COURSE_ID_LOCAL.getKey(), courseId);

                                dismissLoader();

                                if (isAdded() && getActivity() != null) {
                                    Intent intent = new Intent(getActivity(), HomeScreen.class);
                                    intent.putExtra("NewUser", true);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.putExtra(BundleParamConstants.LOCATION_SHOW_LOCATION_PERMISSION, true);

                                    MyApplication.saveData(PreferenceKeys.LAUNCH_COUNT.getKey(), 1);
                                    MyApplication.saveData(PreferenceKeys.ON_BOARDING_COMPLETED.getKey(), true);

                                    String matchingIds = MyApplication.getFirebaseRemoteConfig()
                                            .getString("competitive_selected_batch_id_v1");
                                    if (Arrays.asList(matchingIds.split(",")).contains(courseId)) {
//                                        MyApplication.saveData(PreferenceKeys.BOARD_SWITCH.getKey(), false);
                                    }

                                    startActivity(intent);
                                    getActivity().finish();
                                }
                            } else {
                                WelcomeToArivihanFragment.logOnboardingFailure(userName, userPhoneNumber);
                                dismissLoader();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            WelcomeToArivihanFragment.logOnboardingFailure(userName, userPhoneNumber);
                            dismissLoader();
                        }
                    },
                    error -> {
                        WelcomeToArivihanFragment.logOnboardingFailure(userName, userPhoneNumber);
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
    private void fetchPredictions(String query) {
        FindAutocompletePredictionsRequest request =
                FindAutocompletePredictionsRequest.builder()
                        .setSessionToken(sessionToken)
                        .setQuery(query)
                        .setCountries(Collections.singletonList("IN"))
                        .build();

        placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener(response -> {
                    List<AutocompletePrediction> list = response.getAutocompletePredictions();
                    if (list == null || list.isEmpty()) {
                        hideSuggestions();
                    } else {
                        adapter.setList(list);
                        showSuggestions();
                    }
                })
                .addOnFailureListener(e -> {
                    // log or show error
                    hideSuggestions();
                });
    }

    private void onPlaceClicked(AutocompletePrediction prediction) {
        selectedPlaceId = prediction.getPlaceId();
        String address = prediction.getFullText(null).toString();

        // Set flag to prevent TextWatcher from triggering
        isSelectingPlace = true;

        et_location.setText(prediction.getPrimaryText(null).toString());
        et_location.setSelection(et_location.getText().length());

        // Reset flag after a short delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            isSelectingPlace = false;
        }, 100);

        et_location.clearFocus();
        fl_parent_layout.requestFocus();
        hideKeyboard();
        hideSuggestions();

        fetchPlaceDetails(selectedPlaceId, address);
    }

    private void fetchPlaceDetails(String placeId, String address) {
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG
        );

        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, fields);

        placesClient.fetchPlace(request)
                .addOnSuccessListener(response -> {
                    selectedPlace = response.getPlace();
                    if (selectedPlace.getLatLng() != null) {
                        selectedPlaceLat = selectedPlace.getLatLng().latitude;
                        selectedPlaceLong = selectedPlace.getLatLng().longitude;
                        selectedAddress = address;
                        String addr = selectedPlace.getAddress();
                        // just for debug/logging
                        System.out.println("✅ Selected: " + addr + " (" + selectedPlaceLat + ", " + selectedPlaceLong + ")");
                    }
                })
                .addOnFailureListener(e -> {
                    selectedPlace = null;
                    selectedPlaceId = null;
                    if(isAdded()) {
                        Toast.makeText(requireContext(), "Failed to fetch place details", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showSuggestions() {
        rv_location.setVisibility(View.VISIBLE);
        if(onBoardingUIListener!=null)
            onBoardingUIListener.setGradientVisibility(false);
    }

    private void hideSuggestions() {
        rv_location.setVisibility(View.GONE);
        if(onBoardingUIListener!=null)
            onBoardingUIListener.setGradientVisibility(true);
        adapter.setList(null);
    }

    private boolean isTouchInsideView(View view, MotionEvent event) {
        try {
            if (view == null || event == null) return false;
            int[] loc = new int[2];
            view.getLocationOnScreen(loc);
            Rect r = new Rect(loc[0], loc[1], loc[0] + view.getWidth(), loc[1] + view.getHeight());
            return r.contains((int) event.getRawX(), (int) event.getRawY());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            View v = requireActivity().getCurrentFocus();
            if (imm != null && v != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showLoadingDialog() {
        dismissLoader();

        pb_loader.setVisibility(View.VISIBLE);
        tv_next.setVisibility(View.GONE);
        cv_next.setEnabled(false);
        cv_next.setAlpha(0.7f);
        tl_location.setEnabled(false);
        tl_location.setAlpha(0.7f);

    }
    private void dismissLoader() {
        pb_loader.setVisibility(View.GONE);
        tv_next.setVisibility(View.VISIBLE);
        cv_next.setEnabled(true);
        cv_next.setAlpha(1.0f);
        tl_location.setEnabled(true);
        tl_location.setAlpha(1.0f);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        debounceHandler.removeCallbacksAndMessages(null);
    }
}