package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.CHECK_METRICS_USER;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.app.AlertDialog;

import androidx.cardview.widget.CardView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.material.card.MaterialCardView;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.firebase.RemoteConfigKey;
import arivihan.technologies.doubtbuzzter2.helper.BranchAnalyticsHelper;
import arivihan.technologies.doubtbuzzter2.helper.PhoneInfoDialog;
import arivihan.technologies.doubtbuzzter2.helper.EnvironmentManager;
import arivihan.technologies.doubtbuzzter2.helper.refer.ReferralLinkHandler;
import arivihan.technologies.doubtbuzzter2.helper.firebase.FirebaseMessagingManager;
import arivihan.technologies.doubtbuzzter2.viewModel.HomeScreenViewModel;
import arivihan.technologies.doubtbuzzter2.volley.VolleyJsonRequest;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class SettingsActivity extends BaseActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();
    private static final String KEY_ENV_OVERRIDE_URL = "KEY_ENV_OVERRIDE_URL";

    private CardView btnClearCache;
    private ScrollView parentLayout;
    private MaterialCardView btnAppUpdate;
    private TextView tvClearCacheBtn, tvAppUpdateBtn;
    private ImageView ivBack;
    private View rootView;



    AppUpdateManager appUpdateManager;
    TextView tv_clear_cache_title, tv_clear_cache_subtitle, tv_app_update_title, tv_app_update_subtitle,tv_phone_info_subtitle,tv_phone_info_title,tv_phone_info_btn, tv_additional_settings, tv_additional_settings_subtitle;


    // Environment picker views
    TextView tv_environment_title, tv_environment_subtitle, tv_current_environment;
    CardView cv_environment_picker, cv_additional_setting;


    String clearCacheSnackBarText;
    String appUpToDateMessage;
    CardView cv_app_update,cv_phone_info;

    private InstallStateUpdatedListener stateUpdatedListener;

    private static final int UPDATE_REQUEST_CODE = 300;
    private static final int MY_REQUEST_CODE = 1001;

    // Environment constants
    private static final String PREF_ENVIRONMENT = "app_environments";
    private static final String ENV_PRODUCTION = "PROD";
    private static final String ENV_DEVELOPMENT = "DEV";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_screen);
        initViews();
        setAllTexts();
        setupListeners();
        checkForUpdates();
        setEnvironmentPicker();
        //setupDebugReferrerText();
    }



    private void initViews() {
        rootView = findViewById(android.R.id.content);
        cv_app_update=findViewById(R.id.cv_app_update);
        parentLayout=findViewById(R.id.parent);
        tv_clear_cache_title=findViewById(R.id.tv_clear_cache_title);
        tv_clear_cache_subtitle=findViewById(R.id.tv_clear_cache_subtitle);
        tvClearCacheBtn = findViewById(R.id.tv_clear_cache_btn);
        tv_app_update_subtitle=findViewById(R.id.tv_app_update_subtitle);
        tv_app_update_title=findViewById(R.id.tv_app_update_title);
        tvAppUpdateBtn = findViewById(R.id.tv_app_update_btn);
        btnClearCache = findViewById(R.id.btn_clear_cache);
        btnAppUpdate = findViewById(R.id.btn_app_update);
        ivBack = findViewById(R.id.iv_back);
        cv_phone_info = findViewById(R.id.btn_phone_info);
        tv_phone_info_subtitle=findViewById(R.id.tv_phone_info_subtitle);
        tv_phone_info_title=findViewById(R.id.tv_phone_info_title);
        tv_phone_info_btn=findViewById(R.id.tv_phone_info_btn);
        tv_additional_settings=findViewById(R.id.tv_additional_settings);
        tv_additional_settings_subtitle=findViewById(R.id.tv_additional_settings_subtitle);

        // Initialize environment picker views
        cv_additional_setting = findViewById(R.id.cv_additional_setting);
        cv_environment_picker = findViewById(R.id.cv_environment_picker);
        tv_environment_title = findViewById(R.id.tv_environment_title);
        tv_environment_subtitle = findViewById(R.id.tv_environment_subtitle);
        tv_current_environment = findViewById(R.id.tv_current_environment);
    }

    private void setupListeners() {
        findViewById(R.id.back_button_layout).setOnClickListener(v -> finish());

        btnClearCache.setOnClickListener(v -> clearCache());
        ivBack.setOnClickListener(view -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        btnAppUpdate.setOnClickListener(v -> startInAppUpdate());
        cv_phone_info.setOnClickListener(v -> showPhoneInfoDialog());

        // Environment picker click listener
        cv_environment_picker.setOnClickListener(v -> showEnvironmentPickerDialog());

        //settings
        cv_additional_setting.setOnClickListener(view -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.setting_screen_addtl_setting_click.getActionName());
            startActivity(new Intent(SettingsActivity.this, AdditionalSettingsActivity.class));
        });
    }


    private void setEnvironmentPicker() {
        Log.d(TAG, "onCreate: "+MyApplication.getFirebaseRemoteConfig().getString("app_version_eligible_for_env_mode_change"));

        //env
        if(MyApplication.getFirebaseRemoteConfig().getString("app_version_eligible_for_env_mode_change").equalsIgnoreCase(BuildConfig.VERSION_NAME) || isUserAllowedToByPass()){
            cv_environment_picker.setVisibility(View.VISIBLE);
        }else {
            checkUserElgibleForEnviornmentChangeSettings();
        }

        //env
        String currentEnv = EnvironmentManager.getEnvironmentNameByUrl(MyApplication.getEndPoint());// Update display
        updateEnvironmentDisplay(currentEnv);
    }



    private void checkUserElgibleForEnviornmentChangeSettings() {
        Map<String, Object> map = new HashMap<>();
        map.put("phoneNumber", MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey()));
        Log.d(TAG, "EnvironmentChangeSettings: Request: " + map);

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        VolleyJsonRequest myJsonObjectRequest =
                new VolleyJsonRequest(
                        Request.Method.GET,
                        BuildConfig.BASE_URL_PROD + CommonMethods.getUrlParam(CHECK_METRICS_USER, map),
                        null,
                        response -> {
                            try {
                                Log.d(TAG, "EnvironmentChangeSettings: Response: "+response);
                                boolean success = response.getBoolean("success");
                                String data = response.getString("data");
                                if (success) {
                                    if ("YES".equalsIgnoreCase(data)) {
                                        cv_environment_picker.setVisibility(View.VISIBLE);
                                    } else {
                                        cv_environment_picker.setVisibility(View.GONE);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        },
                        error -> {
                            Log.d(TAG, "Error: " + error.toString());
                            CommonMethods.showNegativeSnackbar(parentLayout, error.getMessage() );
                            cv_environment_picker.setVisibility(View.GONE);
                        }
                );

        myJsonObjectRequest.setTag("CHECK_METRICS_USER");
        requestQueue.add(myJsonObjectRequest);
    }



    @SuppressLint("SetTextI18n")
    private void updateEnvironmentDisplay(String environment) {
        tv_current_environment.setText("Current: " + environment);
    }

    private void showEnvironmentPickerDialog() {
        Map<String, String> envMap = EnvironmentManager.getEnvironmentList();
        if (envMap.isEmpty()) return;

        String[] envNames = envMap.keySet().toArray(new String[0]);

        String currentUrl = EnvironmentManager.getEndPoint();
        String currentEnv = EnvironmentManager.getEnvironmentNameByUrl(currentUrl);

        int selectedIndex = 0;
        for (int i = 0; i < envNames.length; i++) {
            if (envNames[i].equalsIgnoreCase(currentEnv)) {
                selectedIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Environment")
                .setSingleChoiceItems(envNames, selectedIndex, (dialog, which) -> {
                    String selectedKey = envNames[which];
                    String selectedUrl = envMap.get(selectedKey);
                    EnvironmentManager.setEndPoint(selectedUrl);
                    updateEnvironmentDisplay(selectedKey);
                    showEnvironmentChangeDialog(selectedKey);
                    dialog.dismiss();
                    logout();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    //logout
    private void logout() {
        FirebaseMessagingManager.unSubscribeToUserDefaultTopics();
        BranchAnalyticsHelper.logoutUser();

        clearCache();
        deleteAllLocalData();

        MyApplication application = (MyApplication) getApplication();
        HomeScreenViewModel homeScreenViewModel = application.getHomeScreenViewModel();
        homeScreenViewModel.removeDataModel();

        Intent homeIntent = new Intent(this, UserAuthenticationActivity.class);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeIntent);

        CommonMethods.showPositiveSnackbar(parentLayout, getString(R.string.SuccessfullLogout));

        finish();
    }


    private void deleteAllLocalData() {
        MyApplication.saveData(getString(R.string.user_selected_video_quality), "");
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.clicked_logout.getActionName());
        MyApplication.getTestGenerationParentDatabaseReference().setValue(null);
        MyApplication.clearSharedPref();
        getViewModelStore().clear();

        MyApplication.removeFromDoNotDeleteSharedPreference(PreferenceKeys.REFERRAL_CODE.getKey());
        MyApplication.removeFromDoNotDeleteSharedPreference(PreferenceKeys.REFERRAL_SOURCE.getKey());
        MyApplication.removeFromDoNotDeleteSharedPreference(PreferenceKeys.REFERRAL_DETECTED.getKey());
    }



    private void showEnvironmentChangeDialog(String environment) {
        String message = "Environment switched to " + (ENV_DEVELOPMENT.equals(environment) ? "Development" : "Production") + ". Please restart the app for changes to take effect.";
        CommonMethods.showPositiveSnackbar(rootView, message);
    }


    private void updateAppButton(boolean isUpdateAvailable) {
        if (isUpdateAvailable) {
            cv_app_update.setVisibility(View.VISIBLE);
            btnAppUpdate.setEnabled(true);
            btnAppUpdate.setAlpha(1f);
            tvAppUpdateBtn.setTextColor(getResources().getColor(R.color.arivihan_secondary_color));
        } else {
            cv_app_update.setVisibility(View.GONE);
            btnAppUpdate.setEnabled(false);
            btnAppUpdate.setAlpha(0.5f);
            tvAppUpdateBtn.setText(appUpToDateMessage);
            tvAppUpdateBtn.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                CommonMethods.showNegativeSnackbar(rootView, "Update canceled");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (appUpdateManager != null && stateUpdatedListener != null) {
            appUpdateManager.unregisterListener(stateUpdatedListener);
        }
    }

    private void setAllTexts() {
        try {
            String remoteJson = MyApplication.getFirebaseRemoteConfig().getString("setting_texts");
            JSONObject root = new JSONObject(remoteJson);
            String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
            JSONObject selectedLang = root.has(lang) ? root.getJSONObject(lang) : root.getJSONObject("en");

            tv_clear_cache_title.setText(selectedLang.optString("clear_cache_title"));
            tv_clear_cache_subtitle.setText(selectedLang.optString("clear_cache_subtitle"));
            tvClearCacheBtn.setText(selectedLang.optString("clear_cache_btn"));
            tv_app_update_title.setText(selectedLang.optString("app_update_title"));
            tv_app_update_subtitle.setText(selectedLang.optString("app_update_subtitle"));
            tvAppUpdateBtn.setText(selectedLang.optString("app_update_btn"));
            clearCacheSnackBarText=selectedLang.optString("clear_cache_message");
            appUpToDateMessage=selectedLang.optString("app_up_to_date_message");
            tv_phone_info_subtitle.setText(selectedLang.optString("phone_info_subtitle"));
            tv_phone_info_title.setText(selectedLang.optString("phone_info_title"));
            tv_phone_info_btn.setText(selectedLang.optString("phone_info_btn"));
            tv_additional_settings.setText(selectedLang.optString("additional_settings"));
            tv_additional_settings_subtitle.setText(selectedLang.optString("additional_settings_subtitle"));

            // Environment texts
            tv_environment_title.setText(selectedLang.optString("environment_title", "Environment Mode"));
            tv_environment_subtitle.setText(selectedLang.optString("environment_subtitle", "Switch between Production and Development"));
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    private void showPhoneInfoDialog() {
        PhoneInfoDialog dialog = new PhoneInfoDialog(this);
        dialog.show();
    }

    private void startInAppUpdate() {
        try {
            appUpdateManager = AppUpdateManagerFactory.create(this);

            appUpdateManager.getAppUpdateInfo()
                    .addOnSuccessListener(appUpdateInfo -> {

                        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {

                            try {
                                appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, MY_REQUEST_CODE);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } else {
                            CommonMethods.showNegativeSnackbar(rootView, "No update available");
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    //cache clear
    private void clearCache() {
        try {
            clearCacheFiles();
            clearHomeViewModel();

        } catch (Exception e) {
            e.printStackTrace();
            showSnackbar("Error clearing cache: " + e.getMessage(), false);
        }
    }

    private void clearHomeViewModel() {
        MyApplication application = (MyApplication) getApplication();
        HomeScreenViewModel homeScreenViewModel = application.getHomeScreenViewModel();

        homeScreenViewModel.removeDataModel();
    }

    private void clearCacheFiles() {
        File cacheDir = getCacheDir();
        File externalCacheDir = getExternalCacheDir();

        boolean success = true;

        if (cacheDir != null) success = deleteDir(cacheDir);
        if (externalCacheDir != null) success = success && deleteDir(externalCacheDir);

        if (success) {
            showSnackbar(clearCacheSnackBarText, true);
        } else {
            showSnackbar("Failed to clear some cache files", false);
        }

    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (!deleteDir(child)) return false;
                }
            }
            return true;
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        }
        return false;
    }

    private void checkForUpdates() {
        appUpdateManager = AppUpdateManagerFactory.create(this);

        try {
            appUpdateManager.getAppUpdateInfo()
                    .addOnSuccessListener(
                            appUpdateInfo -> updateAppButton(appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE)
                    );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    //bypass
    private boolean isUserAllowedToByPass() {
        try {
            String allowToByPassForUsers = MyApplication.getFirebaseRemoteConfig().getString(RemoteConfigKey.INTERNAL_DEV_USERS_TO_BY_PASS_FEATURE.getKey());
            String phoneNumber = MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey());

            return allowToByPassForUsers.contains(phoneNumber);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }



    //snackbar
    private void showSnackbar(String message, boolean isSuccess) {
        if (isSuccess) {
            CommonMethods.showPositiveSnackbar(rootView,message);
        } else {
            CommonMethods.showNegativeSnackbar(rootView,message);
        }
    }

    private void setupDebugReferrerText() {
        String currentEnv = EnvironmentManager.getEnvironmentNameByUrl(MyApplication.getEndPoint());
        if (!"STAGE".equalsIgnoreCase(currentEnv)) return;

        TextView tvDebugReferrer = findViewById(R.id.tv_debug_referrer);
        String referrer = MyApplication.getStringDataFromDoNotDeleteSharedPreference(ReferralLinkHandler.KEY_REFERRER);
        tvDebugReferrer.setText((referrer != null ? referrer : ""));
        tvDebugReferrer.setVisibility(View.VISIBLE);
    }

}