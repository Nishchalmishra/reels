package arivihan.technologies.doubtbuzzter2.common;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.PUT_UPDATE;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.BaseActivity;
import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.helper.ActivityLauncher;
import arivihan.technologies.doubtbuzzter2.helper.UserNameValidationConfig;
import arivihan.technologies.doubtbuzzter2.model.NameInputDto;
import arivihan.technologies.doubtbuzzter2.model.UserDto;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;

public class NameInputActivity extends BaseActivity implements EdgeToEdgeHelper.SelfManagedInsets {

    private EditText nameEditText;
    private CardView continueBtn;
    private TextView continueText;
    private TextView titleText;
    private ImageView topImage;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_name_screen_layout);

        Log.d("NameScreen", "onCreate started");

        nameEditText = findViewById(R.id.nameEditText);
        continueBtn = findViewById(R.id.cv_continue);
        continueText = findViewById(R.id.tv_continue);
        titleText = findViewById(R.id.titleText);
        topImage = findViewById(R.id.topImage);

        applyEdgeToEdgeInsets();


        NameInputDto dto = (NameInputDto) getIntent().getSerializableExtra("activity_params");
        Log.d("NameScreen", "Initial DTO from intent: " + dto);
        String navigation=dto.getNavigationType();
        Map<String,String>map = new HashMap<>();
        map.put("source",dto.getScreen());
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.hs_name_input_bottom_sheet_view.getActionName(),map);
        nameEditText.requestFocus();
        nameEditText.post(() -> {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(nameEditText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        });

        String remoteJson = MyApplication.getFirebaseRemoteConfig()
                .getString("enter_your_name_screen");

        Log.d("NameScreen", "Remote JSON: " + remoteJson);

        try {
            if (!TextUtils.isEmpty(remoteJson)) {

                JSONObject root = new JSONObject(remoteJson);
                String screenKey;

                if (dto != null && !TextUtils.isEmpty(dto.getScreen())) {
                    screenKey = dto.getScreen();
                    Log.d("NameScreen", "Using nextScreenClass as screenKey: " + screenKey);
                } else {
                    screenKey = getClass().getSimpleName();
                    Log.d("NameScreen", "Using current class as screenKey: " + screenKey);
                }
                String lang = MyApplication.getStringData("My_Lang");
                Log.d("NameScreen", "Language from storage: " + lang);

                if (TextUtils.isEmpty(lang)) {
                    lang = "en";
                    Log.d("NameScreen", "Language empty, defaulting to EN");
                }

                if (root.has(screenKey)) {
                    Log.d("NameScreen", "Screen key found in remote config");

                    JSONObject screenObj = root.getJSONObject(screenKey);

                    if (!screenObj.has(lang)) {
                        Log.d("NameScreen", "Language not found, fallback to EN");
                        lang = "en";
                    }

                    if (screenObj.has(lang)) {
                        JSONObject langObj = screenObj.getJSONObject(lang);
                        Log.d("NameScreen", "Language object found: " + langObj.toString());

                        NameInputDto remoteDto = new Gson().fromJson(
                                langObj.toString(),
                                NameInputDto.class
                        );

                        Log.d("NameScreen", "Parsed Remote DTO: " + remoteDto);

                        if (remoteDto != null) {
                            dto = remoteDto;
                            Log.d("NameScreen", "DTO overridden with remote config");
                        }
                    } else {
                        Log.w("NameScreen", "Language key not found even after fallback");
                    }

                } else {
                    Log.w("NameScreen", "Screen key NOT found in remote config");
                }
            } else {
                Log.w("NameScreen", "Remote JSON is empty");
            }

        } catch (Exception e) {
            Log.e("NameScreen", "Error parsing remote config", e);
        }

        if (dto != null) {

            Log.d("NameScreen", "Final DTO used: " + dto);

            if (!TextUtils.isEmpty(dto.getTitle())) {
                titleText.setText(dto.getTitle());
                Log.d("NameScreen", "Title set: " + dto.getTitle());
            }

            if (!TextUtils.isEmpty(dto.getPosterUrl())) {
                Log.d("NameScreen", "Loading image: " + dto.getPosterUrl());

                Glide.with(this)
                        .load(dto.getPosterUrl())
                        .placeholder(R.drawable.on_boarding_background_new)
                        .into(topImage);
            }
        } else {
            Log.w("NameScreen", "DTO is NULL after processing");
        }


        //name config
        UserNameValidationConfig config = UserNameValidationConfig.fromRemoteConfig();

        updateUiState(config, false);

        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateUiState(config, true);
            }
        });

        continueBtn.setOnClickListener(v -> {
            String input = nameEditText.getText().toString().trim();

            updateUiState(config, true);
            if (!continueBtn.isEnabled()) return;

            Log.d("NameScreen", "Saving name: " + input);
            saveData(input, navigation);
        });



    }


    private void updateUiState(UserNameValidationConfig config, Boolean show) {
        String input = nameEditText.getText().toString().trim();
        String error = config.getValidationError(input, getString(R.string.name_required_error));

        boolean isValid = error == null;

        if (show && StringUtils.isNotEmpty(error)) {
            showError(String.valueOf(UserNameValidationConfig.fromHtml(error)));
        }

        continueBtn.setEnabled(isValid);
        continueBtn.setCardBackgroundColor(ContextCompat.getColor(this, isValid ? R.color.btn_enabled_bg   : R.color.btn_disabled_bg));
        continueText.setTextColor(ContextCompat.getColor(this, isValid ? R.color.btn_enabled_text : R.color.btn_disabled_text));
    }

    private void saveData(String name,String navigation) {
        try {
            Gson gson = new Gson();
            UserDto userDto = MyApplication.getUserDto("userDto");
            if (userDto == null) {
                userDto = new UserDto();
            }
            if (name != null && !name.trim().isEmpty()) {
                userDto.setUsername(name.trim());
            } else {
                Log.e("API_ERROR", "Name is null or empty");
                return;
            }
            if (BuildConfig.DEBUG) Log.d("API_REQUEST", String.valueOf(new JSONObject(gson.toJson(userDto))));
            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.PATCH,
                    PUT_UPDATE, new JSONObject(gson.toJson(userDto)), response -> {
                        Log.d("saveData", "saveData: "+response.toString());
                        try {
                            if (Boolean.parseBoolean(response.get("success").toString())) {
                                MyApplication.saveData(PreferenceKeys.NAME.name(), name);
                                MyApplication.saveData(PreferenceKeys.IS_NAME_UPDATED.name(), true);
                                MyApplication.logFirebaseBundleCreation(FirebaseEvents.name_input_continue_btn_click.getActionName());
                                handleNavigation(navigation);
                            } else {
                                showError("Something went wrong");
                            }
                        } catch (JSONException e) {
                            showError(e.getMessage());
                        }
                    },
                    new VolleyGlobalErrorListener(this) {
                        @Override
                        public void postError(VolleyError error) {
                            showError(error.getMessage());
                        }
                    }
            );

            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);

        } catch (JSONException e) {
            showError(e.getMessage());
        }
    }
    private void handleNavigation(String navigation) {
        NameInputDto dto = (NameInputDto) getIntent().getSerializableExtra("activity_params");
        Log.d("handleNavigation", "handleNavigation: " + dto);

        if (dto != null && dto.getNextScreenClass() != null) {
            try {
                Log.d("NavigationFlow", "Starting navigation handling");

                Map<String, Object> extras = dto.getExtras();
                Log.d("NavigationFlow", "Extras: " + extras);

                Object paramsJsonObj = null;
                Object urlObj = null;

                if (extras != null) {
                    if (extras.containsKey("params")) {
                        paramsJsonObj = extras.get("params");
                        Log.d("NavigationFlow", "Using key: params");
                    } else if (extras.containsKey("activity_params_json")) {
                        paramsJsonObj = extras.get("activity_params_json");
                        Log.d("NavigationFlow", "Using key: activity_params_json");
                    }
                    urlObj = extras.get("url");
                }
                String activityParamsJson = paramsJsonObj != null ? paramsJsonObj.toString() : null;
                String url = urlObj != null ? urlObj.toString() : null;

                Log.d("NavigationFlow", "Final params JSON: " + activityParamsJson);
                Log.d("NavigationFlow", "Final url-bnvigation: " + navigation);

                if (activityParamsJson != null) {
                CommonMethods.navigateFromActivityConfig(
                        this,
                        null,
                        dto.getNextScreenClass(),
                        StringUtils.isEmpty(navigation)?extras:ActivityLauncher.convertJsonBackToActivityParams(activityParamsJson)
                );
                    finish();

                } else if (url != null) {
                    Log.d("NavigationFlow", "Fallback: navigating using URL only");

                    Intent intent = new Intent(this,
                            ActivityLauncher.resolveClass(dto.getNextScreenClass()));
                    startActivity(intent);
                    finish();

                } else {
                    Log.w("NavigationFlow", "No valid navigation data found");
                }

            } catch (Exception e) {
                Log.e("NavigationFlow", "Navigation failed", e);
            }
        } else {
            Intent resultIntent = new Intent();
            setResult(Activity.RESULT_OK, resultIntent);
        }
    }

    /**
     * Type-safe helper — Intent has no putExtra(String, Object) overload,
     * so we dispatch to the correct typed variant at runtime.
     */
    private void putExtraFromObject(Intent intent, String key, Object value) {
        if (value == null) return;

        if      (value instanceof String)      intent.putExtra(key, (String)      value);
        else if (value instanceof Boolean)     intent.putExtra(key, (Boolean)     value);
        else if (value instanceof Integer)     intent.putExtra(key, (Integer)     value);
        else if (value instanceof Long)        intent.putExtra(key, (Long)        value);
        else if (value instanceof Double)      intent.putExtra(key, (Double)      value);
        else if (value instanceof Float)       intent.putExtra(key, (Float)       value);
        else if (value instanceof Bundle)      intent.putExtra(key, (Bundle)      value);
        else if (value instanceof Parcelable)  intent.putExtra(key, (Parcelable)  value);
        else if (value instanceof Serializable)intent.putExtra(key, (Serializable)value);
        else Log.w("handleNavigation", "Skipping unserializable extra: " + key + " (" + value.getClass() + ")");
    }

    /** The banner draws behind the status bar; the form receives the bottom system-bar/IME inset. */
    private void applyEdgeToEdgeInsets() {
        EdgeToEdgeHelper.applyArtworkEdgeToEdge(
                findViewById(R.id.root_layout),
                new View[] {findViewById(R.id.bottomContainer)},
                null,
                nameEditText);
    }

    private void showError(String message) {
        CommonMethods.showNegativeSnackbar(findViewById(android.R.id.content), message);
    }
}
