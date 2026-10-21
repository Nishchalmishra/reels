package arivihan.technologies.doubtbuzzter2.fragments;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_CHANGE_USER_LANGUAGE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.CONFIGURATION;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_GET_USER_BY_PHONE;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.MySpinnerArrayAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.MySpinnerType;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.fragments.authentication.WelcomeToArivihanFragment;
import arivihan.technologies.doubtbuzzter2.listener.FragmentChangeListener;
import arivihan.technologies.doubtbuzzter2.listener.SpinnerListener;
import arivihan.technologies.doubtbuzzter2.model.ArivihanLanguageEnum;
import arivihan.technologies.doubtbuzzter2.model.ConfigurationResult;
import arivihan.technologies.doubtbuzzter2.model.batch.user.UserDetailsResponse;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.model.VernacularItem;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OnboardingLanguageSelectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OnboardingLanguageSelectionFragment extends Fragment implements SpinnerListener {

    private LottieAnimationView lottieAnimationView;
    private Spinner languageSpinner;
    List<String> languages = new ArrayList<>();
    List<VernacularItem> languageList = new ArrayList<>();
    CardView next;
    FragmentChangeListener listener;
    int selectedLanguagePosition = 0;
    Gson gson = new Gson();



    public OnboardingLanguageSelectionFragment() {
        // Required empty public constructor
    }

    public static OnboardingLanguageSelectionFragment newInstance(String param1, String param2) {
        OnboardingLanguageSelectionFragment fragment = new OnboardingLanguageSelectionFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (FragmentChangeListener) context;
        } catch (ClassCastException e) {
            Log.e("Error in ", e.getMessage(), e);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding_language_selection, container, false);
        languageSpinner = view.findViewById(R.id.language_spinner);
        next = view.findViewById(R.id.next_card);

        getClassSpinnerData();

        next.setOnClickListener(view1 -> {
            if(!CollectionUtils.isEmpty(languageList)) {
                String lang = languageList.get(selectedLanguagePosition).getId();
                saveLanguageOfUser(lang);
                MyApplication.saveData(PreferenceKeys.MY_LANG.getKey(), ArivihanLanguageEnum.fromStringToCode(lang));
                listener.setCurrentDefaultLocaleStr(lang);
                checkUserExistence();
            }
        });

        return view;
    }

    private void getClassSpinnerData() {
        Map<String, Object> map = new HashMap<>();
        map.put("featureKey", "VERNACULAR");
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(CONFIGURATION, map), null,
                (response -> {
                    TypeToken<ConfigurationResult> token = new TypeToken<ConfigurationResult>() {};
                    Gson gson = new Gson();
                    ConfigurationResult configurationResult = gson.fromJson(response.toString(), token.getType());

                    Map<String, String> keyValues = configurationResult.getData().getKeyValues();
                    Map<String, String> treeMap = new HashMap<>(keyValues);

                    for (String code : treeMap.keySet()) {
                        VernacularItem vernacularItem = new VernacularItem(code, keyValues.get(code));
                        languageList.add(vernacularItem);
                        languages.add(keyValues.get(code) + " Medium");
                    }
                    Log.d("ONBOARDING_LANG", "getClassSpinnerData: " + languages.toString());

                    if (getContext() != null) {
                        MySpinnerArrayAdapter adapter = null;
                        adapter = new MySpinnerArrayAdapter(requireContext(),
                                android.R.layout.simple_spinner_item, languages,
                                MySpinnerType.CLASS, (SpinnerListener) OnboardingLanguageSelectionFragment.this);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        languageSpinner.setAdapter(adapter);
                        languageSpinner.performClick();
                        adapter.setOnSelectedListener(languageSpinner);
                        adapter.notifyDataSetChanged();
                    }
//                    updateClassSpinnerSelection();
                }), new VolleyGlobalErrorListener(getContext()) {
            @Override
            public void postError(VolleyError error) {
            }
        });
        MyVolleySingleton.getInstance(getContext()).addToRequestQueue(myJsonObjectRequest2);
    }

    @Override
    public void updateSelectionOfType(int i, MySpinnerType spinnerType) {
        Log.d("ONBOARDING_LANGUAGE_SELECTION", "updateSelectionOfType: " + languageList.get(i).getId());
        selectedLanguagePosition = i;
    }

    @Override
    public void onNothingSelected(MySpinnerType spinnerType) {

    }


    private void saveLanguageOfUser(String lang) {
        listener.setCurrentDefaultLocaleStr(lang);
        try {
            MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.PATCH,
                    CommonMethods.getUrlParam(AV_CHANGE_USER_LANGUAGE, null), null,
                    response -> {
                    }, new VolleyGlobalErrorListener(getContext()) {
                @Override
                public void postError(VolleyError error) {
                }
            });
            myJsonObjectRequest2.setTag("ONBOARDING_LANGUAGE_SELECTION");
            MyVolleySingleton.getInstance(getContext()).addToRequestQueue(myJsonObjectRequest2);
        } catch (Throwable e) {

        }
    }

    private void nextFragment() {
        Map<String, String> params = new HashMap<>();
        params.put("Steps", "Onboarding_language_selection");
        Bundle bundle = new Bundle();
        bundle.putBoolean("NewUser", true);
        bundle.putString("phoneNumber", this.getArguments().getString("phoneNumber"));
        bundle.putString(PreferenceKeys.PHONE_NUMBER.getKey(), this.getArguments().getString("phoneNumber"));
        bundle.putString(PreferenceKeys.USER_ID.getKey(), this.getArguments().getString("uuid"));
        bundle.putString("email", this.getArguments().getString("email"));
        bundle.putBoolean("fromVerificationFragment", true);
        Log.d("ONBOARDING_LANGUAGE_SELECTION", "onCreateView: " + bundle.toString());
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.Onboarding_language_selection.getActionName(), params);
        WelcomeToArivihanFragment welcomeToArivihanFragment = new WelcomeToArivihanFragment();
        welcomeToArivihanFragment.setArguments(bundle);
        listener.setFragment(welcomeToArivihanFragment);
    }


    private void checkUserExistence() {
            MyApplication.saveData(PreferenceKeys.OTP.getKey(), null);
            Map<String, Object> map = new HashMap<>();
            String finalNumber;
            if(this.getArguments()!=null)
             finalNumber = this.getArguments().getString("phoneNumber");
            else {
                finalNumber = "";
            }
        if(StringUtils.isNotEmpty(finalNumber)) {
                map.put("phoneNumber", finalNumber);
                map.put("key", BuildConfig.API_KEY);


                Log.i("VERNACUALARLOG", "checkUserExistence: " + map.toString());


                MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                        CommonMethods.getUrlParam(AV_GET_USER_BY_PHONE, map), null,
                        response -> {
                            Gson gson = new Gson();

                            Log.d("PHONENUMBER", " userdto before typetoken ");

                            TypeToken<ApiWrappedResponse<UserDetailsResponse>> token = new TypeToken<ApiWrappedResponse<UserDetailsResponse>>() {
                            };

                            ApiWrappedResponse<UserDetailsResponse> apiResponse = gson.fromJson(response.toString(), token.getType());
                            UserDetailsResponse user = (apiResponse != null && apiResponse.isSuccess()) ? apiResponse.getData() : null;

                            Log.d("PHONENUMBER", " userdto typetoken converted");
                            if (user != null) {
                                MyApplication.saveUserProfileLocally(user);
                            }

                            MyApplication.saveData(PreferenceKeys.NUMBER_UPPER.getKey(), finalNumber);
                            MyApplication.saveData(PreferenceKeys.PHONE_NUMBER.getKey(), String.valueOf(finalNumber));
                            MyApplication.saveData(PreferenceKeys.USER_LOGGED_IN.getKey(), true);

                            if (user != null && user.getUserDto() != null) {
                                MyApplication.saveData(PreferenceKeys.USER_ID.getKey(), user.getUserDto().getUserId());
                            }
                            MyApplication.saveData(PreferenceKeys.COUNT_POP_UP.getKey(), 0);
                            if (getActivity() != null && isAdded()) {
                            }
                            listener.moveToHomeScreen();
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
    public void onDestroyView() {
        super.onDestroyView();
        if (getContext() != null) {
            MyVolleySingleton.getInstance(getContext()).getRequestQueue().cancelAll("ONBOARDING_LANGUAGE_SELECTION_FRAGMENT");
        }
    }

}