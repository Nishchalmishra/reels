package arivihan.technologies.doubtbuzzter2.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.event.ActionEventDirection;
import arivihan.technologies.doubtbuzzter2.enums.event.ActionEventName;
import arivihan.technologies.doubtbuzzter2.enums.event.ActionEventScreen;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceMethods;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.model.ArivihanLanguageEnum;
import arivihan.technologies.doubtbuzzter2.model.OnBoardingSelectionModel;
import arivihan.technologies.doubtbuzzter2.volley.EventApiHelper;

public class NameFragment extends Fragment {

    public static final String TAG = NameFragment.class.getSimpleName();
    private static final String ARG_NAME = "userName";
    private static final String ARG_PHONE = "phone";
    private static final String ARG_NAVIGATION_DIRECTION = "navigationDirection";

    TextView tvWelcome;
    ImageView ivCharacter;
    CardView cardName;
    EditText etName;
    TextView tvNameError;

    // Data variables
    String userName = "";
    String phone = "";
    String nameRegex, restrictedNameRegex;
    String finalErrorMessage;
    String restrictedMessage;
    String restrictedNames;
    boolean finalEnableValidation;
    boolean enableUserNameRestrictionValidation = true;
    NameClickListener nameClickListener;
    private String navigationDirection = ActionEventDirection.FORWARD.getValue();

    public NameFragment() {
        // Required empty constructor
    }

    public static NameFragment newInstance(String userName, String phone, String navigationDirection) {
        NameFragment fragment = new NameFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, userName);
        args.putString(ARG_PHONE, phone);
        args.putString(ARG_NAVIGATION_DIRECTION, navigationDirection);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            nameClickListener = (NameClickListener) context;
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userName = getArguments().getString(ARG_NAME, "");
            phone = getArguments().getString(ARG_PHONE, "");
            navigationDirection = getArguments().getString(ARG_NAVIGATION_DIRECTION,
                    ActionEventDirection.FORWARD.getValue());
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_name, container, false);

        tvWelcome = view.findViewById(R.id.tvWelcome);
        ivCharacter = view.findViewById(R.id.ivCharacter);
        cardName = view.findViewById(R.id.cardName);
        etName = view.findViewById(R.id.etName);
        tvNameError = view.findViewById(R.id.tvNameError);
        String language = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        String json = MyApplication.getFirebaseRemoteConfig().getString("onboarding_name_welcome_text_" + (language != null ? language : ArivihanLanguageEnum.ENGLISH.getCode()));

        if (CommonMethods.isValidJson(json)) {

            tvWelcome.setText(
                    StyledTextBuilder.fromJson(requireActivity(), json)
            );

        }
        if (!userName.isEmpty()) {
            etName.setText(userName);
        }

        nameRegex = ".*[@#\\d].*";
        restrictedNameRegex = "[^a-z0-9]";
        String errorMessage = getString(R.string.not_allowed_message);
        restrictedMessage = getString(R.string.username_restriction_message);
        boolean enableValidation = true;
        try {
            String configJson = MyApplication.getFirebaseRemoteConfig().getString("username_validation_config");
            JSONObject jsonObject = new JSONObject(configJson);
            nameRegex = jsonObject.optString("regex", nameRegex);
            errorMessage = jsonObject.optString("message", errorMessage);
            restrictedNameRegex = jsonObject.optString("restrictedRegex", restrictedNameRegex);
            restrictedMessage = jsonObject.optString("restrictedMessage", restrictedMessage);
            restrictedNames = jsonObject.optString("restrictedNames", restrictedNames);

            enableValidation = jsonObject.optBoolean("enableValidation", true);
            enableUserNameRestrictionValidation = jsonObject.optBoolean("enableUserNameRestrictionValidation", true);
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        nameRegex = regex;
        finalErrorMessage = errorMessage;
        finalEnableValidation = enableValidation;

        handleTextChange();
        startAnimation();

        Map<String, String> eventParams = new HashMap<>();
        eventParams.put("phone", phone!=null ? phone : "");
        eventParams.put(PreferenceKeys.NAME.getKey(), userName!=null ? userName : "");

        OnBoardingSelectionModel sel = PreferenceMethods.getSelectedOnboardingModel();
        EventApiHelper.sendActionEvent(
                ActionEventName.NAME_SCREEN_VIEWED.getValue(),
                (sel!=null && sel.getId()!=null)? sel.getId() : "",
                 phone,
                navigationDirection,
                ActionEventScreen.NAME_SCREEN.getValue()
        );

        MyApplication.logFirebaseBundleCreation(FirebaseEvents.onboarding_name_screen_view.getActionName(), eventParams);

        return view;
    }

    // Show error
    private void showError(String message) {
        tvNameError.setVisibility(View.VISIBLE);
//        tvNameError.setText(message);
        if (isHtml(message)) {
            tvNameError.setText(getHtmlError(message));
        } else {
            tvNameError.setText(message);
        }
//        cardName.setStrokeColor(ContextCompat.getColor(this, R.color.red)); // optional red border
    }

    // Clear error
    private void clearError() {
        tvNameError.setVisibility(View.GONE);
        tvNameError.setText("");
    }

    private void handleTextChange() {
        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String localName = s.toString().trim();
                userName = localName;

                if (isInvalidUserName(s.toString(), nameRegex, finalEnableValidation)) {
//                    name_card.setError(getHtmlError(finalErrorMessage));
                    showError(finalErrorMessage);
                    localName = "";
                } else {
//                    name_card.setError(null);

                    if (enableUserNameRestrictionValidation && checkUserNameRestriction(userName, restrictedNameRegex, restrictedNames)) {
                        String errorMsg = restrictedMessage.replace("{name}", userName);
                        showError(errorMsg);
                        localName = "";
//                        name_card.setError(errorMsg);
//                        name_card.requestFocus();
                    } else {
                        clearError();
                    }
                }

                if (nameClickListener != null) {
                    nameClickListener.onNameEntered(localName);
                }
//                EventApiHelper.sendActionEvent("onboarding_name_input",
//                        null,
//                        phone,
//                        "Name:" +userName,
//                        "Name Screen");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private boolean isInvalidUserName(String input, String regex, boolean enableValidation) {
        return enableValidation && regex != null && !regex.isEmpty() && input.matches(regex);
    }

    private boolean isHtml(String message) {
        return message != null && message.matches(".*<[^>]+>.*");
    }

    private Spanned getHtmlError(String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(message);
        }
    }

    private boolean checkUserNameRestriction(String name, String regex, String restrictedNamesStr) {
        try {

//            Log.d(TAG, "--> "+name);
//            Log.d(TAG, "--> "+regex);
//            Log.d(TAG, "--> "+restrictedNamesStr);
            String cleaned = normalize(name, regex);

//            String restrictedStr = MyApplication.getFirebaseRemoteConfig().getString("onboarding_restricted_names");
            String[] restrictedNamesArr = restrictedNamesStr.split("::");

            for (String keyword : restrictedNamesArr) {
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

    private void startAnimation() {
        Animation fadeIn = AnimationUtils.loadAnimation(requireContext(),
                R.anim.slide_in_from_right_switch);
        tvWelcome.startAnimation(fadeIn);
        cardName.startAnimation(fadeIn);
    }

    public interface NameClickListener {
        void onNameEntered(String name);
    }
}