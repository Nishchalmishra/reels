package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_CHANGE_USER_LANGUAGE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.CONFIGURATION;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.DELETE_YOUR_ROUTINE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_GET_USER_BY_PHONE;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.VernacularGridAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.helper.RemoteConfigUtils;
import arivihan.technologies.doubtbuzzter2.model.ArivihanLanguageEnum;
import arivihan.technologies.doubtbuzzter2.model.ConfigurationResult;
import arivihan.technologies.doubtbuzzter2.model.batch.user.UserDetailsResponse;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.model.VernacularItem;
import arivihan.technologies.doubtbuzzter2.viewModel.HomeScreenViewModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class VernacularActivity extends BaseActivity implements VernacularGridAdapter.VernacularAction {

    private ArrayList<VernacularItem> languageList = new ArrayList<>();
    private ListView listView;
    LinearLayout back;
    TextView title;
    Dialog dialogbox;
    private boolean isFromSignIn;
    private ProgressDialog dialog;
    private RecyclerView vernacularRecyler;
    private VernacularGridAdapter vernacularGridAdapter;
    private ImageView background;
    private boolean newUser;
    private String previousSelectedLang;

    String finalNumber;

    private HomeScreenViewModel homeScreenViewModel;
    private final String TAG = VernacularActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_vernacular);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isFromSignIn) {
                    Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                    homeIntent.addCategory(Intent.CATEGORY_HOME);
                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(homeIntent);
                }
                finish();
            }
        });

        title = findViewById(R.id.activity_title);
        title.setText(getResources().getString(R.string.choose_your_language));
        back = findViewById(R.id.back_arrow);
        back.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
        background = findViewById(R.id.background);
        listView = findViewById(R.id.language_list_view);
        vernacularRecyler = findViewById(R.id.vernacular_recycler);
        vernacularGridAdapter = new VernacularGridAdapter(languageList, this, this::changeImage);
        vernacularRecyler.setAdapter(vernacularGridAdapter);

        MyApplication myapp = (MyApplication) getApplication();
        homeScreenViewModel = myapp.getHomeScreenViewModel();

        dialogbox = new Dialog(VernacularActivity.this);
        dialogbox.setContentView(R.layout.profile_change_alert_dialog);
        dialogbox.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialogbox.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogbox.setCancelable(false);

        if (null != getIntent()) {
            isFromSignIn = getIntent().getBooleanExtra("isFromSignIn", false);
        }

        if (null != getIntent()) {
            newUser = getIntent().getBooleanExtra("NewUser", false);
        }


        listView.setOnItemClickListener((parent, view, position, id) -> {
            setLocale(languageList.get(position).getId());

            int childCount = listView.getChildCount();

            for (int i = 0; i < childCount; i++) {
                if (i == position) {
                    listView.getChildAt(i).findViewById(R.id.textView_language_point).setVisibility(View.VISIBLE);
                } else {
                    listView.getChildAt(i).findViewById(R.id.textView_language_point).setVisibility(View.GONE);
                }

            }
        });

        getLanguageList();

    }

    private void getLanguageList() {

        try {
            final Map<String, Object> map = new HashMap<>();
            map.put("featureKey", "VERNACULAR");
            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                    CommonMethods.getUrlParam(CONFIGURATION, map), null,
                    response -> {
                        try {
                            Gson gson = new Gson();
                            TypeToken<ConfigurationResult> token = new TypeToken<ConfigurationResult>() {
                            };
                            ConfigurationResult configurationResult = gson.fromJson(response.toString(), token.getType());
                            Log.d("vernacularApi", configurationResult.getData().toString());
                            String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()) == null ? "en" : MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());


                            if (configurationResult != null && configurationResult.getData() != null) {
                                Map<String, String> keyValues = configurationResult.getData().getKeyValues();

                                Map<String, String> treeMap = new TreeMap<>(keyValues);
                                for (String code : treeMap.keySet()) {
                                    if (!code.equalsIgnoreCase("en") && !code.equalsIgnoreCase("hi")) {
                                        continue;
                                    }

                                    VernacularItem vernacularItem = new VernacularItem(code, keyValues.get(code));
                                    languageList.add(vernacularItem);
                                    if (("".equalsIgnoreCase(lang) && "en".equalsIgnoreCase(code)) || code.equalsIgnoreCase(lang)) {
                                        vernacularItem.setChecked(true);
                                        Log.d("ImageUrl", " == " + vernacularItem.getUrl());
                                    }
                                }

                            }
                            vernacularGridAdapter.notifyDataSetChanged();

                        } catch (Exception e) {

                        }

                        if (!isFinishing() && dialog != null) {
                            dialog.cancel();
                        }

                    }, new VolleyGlobalErrorListener(this) {
                @Override
                public void postError(VolleyError error) {
                    if (!isFinishing() && dialog != null) {
                        dialog.cancel();
                    }
                }
            });
            myJsonObjectRequest.setTag(TAG);
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
        } catch (Exception e) {

        }
    }


    @Override
    public void changeImage(VernacularItem vernacularItem) {
        setLocale(vernacularItem.getId());
    }

    private void setLocale(String lang) {

        String currentLang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        if (currentLang != null && currentLang.equals(lang)) {
            return; // skip dialog
        }

        previousSelectedLang = currentLang;

        CardView confirmCard = dialogbox.findViewById(R.id.btn_Yes);
        CardView cancelCard = dialogbox.findViewById(R.id.btn_No);
        TextView subTitle = dialogbox.findViewById(R.id.subtitle);
        TextView Title = dialogbox.findViewById(R.id.title);
        ImageView cancel = dialogbox.findViewById(R.id.close_btn);
        Title.setText(RemoteConfigUtils.getDialogText("LANGUAGE", "title"));
        subTitle.setText(RemoteConfigUtils.getDialogText("LANGUAGE", "subtitle"));
        dialogbox.show();

        View.OnClickListener cancelAction = v -> {
            dialogbox.dismiss();

            for (VernacularItem item : languageList) {
                item.setChecked(item.getId().equals(previousSelectedLang));
            }

            if (vernacularGridAdapter != null) {
                vernacularGridAdapter.notifyDataSetChanged();
            }
        };

        cancelCard.setOnClickListener(cancelAction);
        cancel.setOnClickListener(cancelAction);

        confirmCard.setOnClickListener(view -> {
            updateRoutineStatus();
            changeLanguageInApp(lang);
            saveLanguageOfUser();

            for (VernacularItem item : languageList) {
                item.setChecked(item.getId().equals(lang));
            }

            if (vernacularGridAdapter != null) {
                vernacularGridAdapter.notifyDataSetChanged();
            }
            dialogbox.dismiss();

//            if (getIntent().getBooleanExtra("fromVerificationFragment", false)) {
//                checkUserExistence();
//            } else {
                homeScreenViewModel.removeDataModel();
                Intent intent = new Intent(VernacularActivity.this, HomeScreen.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
//            }
        });
    }

    private void updateRoutineStatus() {
        try {
            MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.DELETE,
                    CommonMethods.getUrlParam(DELETE_YOUR_ROUTINE, null), null,
                    response -> {
                        Log.d("response", "updateRoutineStatus: " + response.toString());
                    }, new VolleyGlobalErrorListener(getApplication()) {
                @Override
                public void postError(VolleyError error) {
                }
            });
            MyVolleySingleton.getInstance(getApplication()).addToRequestQueue(myJsonObjectRequest2);
        } catch (Exception e) {
        }
    }

    private void changeLanguageInApp(String lang) {

//        List<UserDetailsResponse.BatchDetail> batchList = PreferenceMethods.getUserBatchList();
//        if(batchList!=null) {
//            for (UserDetailsResponse.BatchDetail batchDetail : batchList) {
//                if(batchDetail!=null && batchDetail.getLanguage()!=null && batchDetail.getLanguage().equals(ArivihanLanguageEnum.fromString(lang).name())) {
//                    Log.d(TAG, "Changing language : "+lang);
//                    MyApplication.saveUserActiveBatch(batchDetail);
//                }
//            }
//        }

        setCurrentDefaultLocaleStr(this, lang);
    }

    private void nextFragment() {
        Intent startIntent = new Intent(VernacularActivity.this, UserAuthenticationActivity.class);
        startIntent.putExtra(PreferenceKeys.PHONE_NUMBER.getKey(), finalNumber);
        startIntent.putExtra(PreferenceKeys.USER_ID.getKey(), getIntent().getStringExtra("uuid"));
        if (!getIntent().getStringExtra("email").isEmpty())
            startIntent.putExtra("email", getIntent().getStringExtra("email"));
        startIntent.putExtra("isOpenWelcomeFragment", true);
        startActivity(startIntent);
    }


    private void checkUserExistence() {
        MyApplication.saveData(PreferenceKeys.OTP.getKey(), null);
        Map<String, Object> map = new HashMap<>();
        String number = getIntent().getStringExtra("phoneNumber");
        map.put("phoneNumber", number);
        map.put("key", BuildConfig.API_KEY);
        finalNumber = number;

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

                    if (user != null && user.getUserDto() != null) {
                        MyApplication.saveData(PreferenceKeys.USER_ID.getKey(), user.getUserDto().getUserId());
                    }
                    MyApplication.saveData(PreferenceKeys.COUNT_POP_UP.getKey(), 0);
//                    if (user.getCourseId().equals("2")) {
//                        MyApplication.saveData(PreferenceKeys.BOARD_SWITCH.getKey(), true);
//                    }

                    Intent intent = new Intent(VernacularActivity.this, HomeScreen.class);
                    startActivity(intent);
                    this.finish();
//                    handleRedirectionAfterNumberVerification();
                }, new VolleyGlobalErrorListener(getApplicationContext()) {
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
        myJsonObjectRequest2.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);

    }


    public void handleRedirectionAfterNumberVerification() {
        if (getApplicationContext() != null) {
            int visitCount = MyApplication.getIntData(PreferenceKeys.EXPLORE_SCREEN_VISIT_COUNT.getKey());
            boolean exploreScreenCompleted = MyApplication.getBoolData(PreferenceKeys.EXPLORE_SCREEN_COMPLETED.getKey());
            boolean editRoutineCompleted = MyApplication.getBoolData(PreferenceKeys.EDIT_ROUTINE_COMPLETED.getKey()) || MyApplication.getBoolData(PreferenceKeys.SYLLABUS_GENERATED.getKey());

            if ((!exploreScreenCompleted || !editRoutineCompleted) && visitCount < 6) {
                if (MyApplication.getStringData(PreferenceKeys.SHOWN_AFTER_INTRO.getKey()) != null && MyApplication.getStringData(PreferenceKeys.SHOWN_AFTER_INTRO.getKey()).equals("ExploreScreen")) {
                    if (editRoutineCompleted) {
                        startActivity(new Intent(getApplicationContext(), ExploreActivity.class));
                    } else {
                        Intent todoListIntent = new Intent(VernacularActivity.this, ToDoListMain.class);
                        todoListIntent.putExtra("redirect_to_home", true);
                        startActivity(todoListIntent);
                        MyApplication.saveData(PreferenceKeys.SHOWN_AFTER_INTRO.getKey(), "ToDoListMain");
                    }
                } else {
                    if (exploreScreenCompleted) {
                        Intent todoListIntent = new Intent(VernacularActivity.this, ToDoListMain.class);
                        todoListIntent.putExtra("redirect_to_home", true);
                        startActivity(todoListIntent);
                    } else {
                        startActivity(new Intent(getApplicationContext(), ExploreActivity.class));
                        MyApplication.saveData(PreferenceKeys.SHOWN_AFTER_INTRO.getKey(), "ExploreScreen");
                    }
                }
                MyApplication.saveData(PreferenceKeys.EXPLORE_SCREEN_VISIT_COUNT.getKey(), visitCount + 1);
            } else {
                Intent intent = new Intent(getApplicationContext(), HomeScreen.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }
    }

    private void setCurrentDefaultLocaleStr(Context context, String my_lang) {
        MyApplication.saveData(PreferenceKeys.MY_LANG.getKey(), ArivihanLanguageEnum.fromStringToCode(my_lang));
        Locale newLang = new Locale(my_lang);
        Locale.setDefault(newLang);
        Configuration configuration = getBaseContext().getResources().getConfiguration();
        configuration.setLocale(newLang);
        context.createConfigurationContext(configuration);
    }

    private void saveLanguageOfUser() {

        try {
            MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.PATCH,
                    CommonMethods.getUrlParam(AV_CHANGE_USER_LANGUAGE, null), null,
                    response -> {
                        TypeToken<ApiWrappedResponse<UserDetailsResponse>> token = new TypeToken<ApiWrappedResponse<UserDetailsResponse>>() {};
                        ApiWrappedResponse<UserDetailsResponse> apiResponse = new Gson().fromJson(response.toString(), token.getType());
                        if (apiResponse != null && apiResponse.isSuccess() && apiResponse.getData() != null) {
                            MyApplication.saveUserProfileLocally(apiResponse.getData());
                        }
                    }, new VolleyGlobalErrorListener(VernacularActivity.this) {
                @Override
                public void postError(VolleyError error) {
                }
            });
            myJsonObjectRequest2.setTag(TAG);
            MyVolleySingleton.getInstance(VernacularActivity.this).addToRequestQueue(myJsonObjectRequest2);
        } catch (Throwable e) {

        }
    }

    @Override
    protected void onDestroy() {
        MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll(TAG);
        super.onDestroy();
    }

    static class DescOrder implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o2.compareTo(o1);
        }
    }

}