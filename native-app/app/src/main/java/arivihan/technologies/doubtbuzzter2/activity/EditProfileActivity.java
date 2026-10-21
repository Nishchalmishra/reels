package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_GET_USER_BY_PHONE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.PUT_UPDATE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.helper.ProfileImageUploadHelper;
import arivihan.technologies.doubtbuzzter2.helper.UserNameValidationConfig;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionType;
import arivihan.technologies.doubtbuzzter2.model.UpdateUserRequest;
import arivihan.technologies.doubtbuzzter2.model.UserDto;
import arivihan.technologies.doubtbuzzter2.model.batch.user.UserDetailsResponse;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.viewModel.HomeScreenViewModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class EditProfileActivity extends LocaleActivity {

    private static final String TAG = "EditProfileActivity";
    LinearLayout back;
    ImageView profileIcon, coachingIdImage;
    ProgressBar progressBar;
    TextView tv_app_version;
    ScrollView scrollView;
    TextInputLayout userNumberInputLayout;
    ProgressBar progressBarDataLoading;
    String previousImageUrl = "";
    LinearLayout linearLayout;
    TextView deleteAccount;
    TextView userNumber;
    CardView image_card, uploadFileImage, save;
    TextInputLayout nameInput, addresInput, batchInputLayout;
    EditText name, address;
    TextInputEditText etBatch;
    int data_loaded = 0;
    int data_saved = 0;
    RelativeLayout ParentLayout;
    Gson gson = new Gson();
    UserDto user = new UserDto();
    TextView text_upload;
    Uri uri;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private boolean imageChanged;
    private int dataLoaded = 0;

    HomeScreenViewModel homeScreenViewModel;
    NetworkStatusLiveData networkStatusLiveData;

    private ActivityResultLauncher<Intent> cropActivityLauncher;
    Uri imageUri;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int GALLERY_CODE = 118;

    private boolean isProfileImage = true;

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void showPermissionExtraInfoDialog(String permission) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(R.string.allow_permission);

        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.yes), (arg0, arg1) -> {
            openAppSettings();
        });

        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.no), (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }


    public void openImagePicker(boolean forProfile) {
        isProfileImage = forProfile; // track which image is being updated

        Dialog image_pick_dialog = new Dialog(this);
        image_pick_dialog.setContentView(R.layout.action_popup_layout);
        image_pick_dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
        image_pick_dialog.getWindow().setBackgroundDrawable(this.getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        image_pick_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        image_pick_dialog.setCancelable(true);

        image_pick_dialog.findViewById(R.id.close_btn).setOnClickListener(view -> image_pick_dialog.cancel());

        TextView title = image_pick_dialog.findViewById(R.id.title);
        title.setText(this.getString(R.string.select_images));
        TextView subtitle = image_pick_dialog.findViewById(R.id.subtitle);
        subtitle.setVisibility(View.GONE);

        ImageView selection_image = image_pick_dialog.findViewById(R.id.selection_image);
        LottieAnimationView lottieAnimationView = image_pick_dialog.findViewById(R.id.animation);
        lottieAnimationView.setVisibility(View.GONE);
        selection_image.setVisibility(View.VISIBLE);

        CardView galleryBtn = image_pick_dialog.findViewById(R.id.btn1);
        CardView cameraBtn = image_pick_dialog.findViewById(R.id.btn2);
        TextView galleryBtnText = image_pick_dialog.findViewById(R.id.btn1_text);
        galleryBtnText.setText(this.getString(R.string.choose_from_gallery));
        TextView cameraBtnText = image_pick_dialog.findViewById(R.id.btn2_text);
        cameraBtnText.setText(this.getString(R.string.click_a_photo));

        galleryBtn.setOnClickListener(view -> {
            openGallery();
            image_pick_dialog.cancel();
        });
        cameraBtn.setOnClickListener(view -> {
            image_pick_dialog.cancel();
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                checkCameraPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    showPermissionExtraInfoDialog(Manifest.permission.CAMERA);
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            CAMERA_PERMISSION_CODE);
                }
            }
        });
        image_pick_dialog.show();
    }



    private void openGallery() {
        Log.d("issuePermission", "openGallery: ");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), GALLERY_CODE);
    }

    public void checkCameraPermission(String permission, int requestCode) {

        String filename = "photo";
        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File imageFile = File.createTempFile(filename, ".jpg", storageDirectory);

            Uri imageUri = FileProvider.getUriForFile(this,
                    BuildConfig.APPLICATION_ID + ".provider", imageFile);
            MyApplication.saveData(PreferenceKeys.CAMERA_CAPTURE_URI.getKey(), imageUri.toString());

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, 19);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void startCrop(Uri imageUri) {
        imageChanged = true;
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                .setMultiTouchEnabled(true)
                .start(this);
    }


    private void uploadImage() {
        try {
            Drawable drawable = isProfileImage ? profileIcon.getDrawable() : coachingIdImage.getDrawable();

            if (drawable == null) {
                Toast.makeText(this, getString(R.string.no_image_selected), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                linearLayout.setVisibility(View.VISIBLE);
                return;
            }

            Toast.makeText(this, getString(R.string.uploading_image), Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.GONE);

            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

            ProfileImageUploadHelper.uploadProfileImage(this, bitmap, new ProfileImageUploadHelper.OnProfileImageUploadListener() {
                @Override
                public void onSuccess(String imageUrl) {
                    if (isFinishing() || isDestroyed()) return;

                    if (isProfileImage) user.setImageUrl(imageUrl);
                    else user.setIdUrl(imageUrl);

                    saveData();
                }

                @Override
                public void onFailure(String errorMessage) {
                    if (isFinishing() || isDestroyed()) return;

                    Log.e(TAG, "Image upload failed: " + errorMessage);
                    Toast.makeText(EditProfileActivity.this, getString(R.string.upload_failed, errorMessage), Toast.LENGTH_SHORT).show();
                    // Fallback: save other profile data even if image upload fails
                    saveData();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "uploadImage exception: " + e.getMessage());
            Toast.makeText(this, getString(R.string.please_change_image), Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            linearLayout.setVisibility(View.VISIBLE);
        }
    }


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        back = findViewById(R.id.back_arrow);
        profileIcon = findViewById(R.id.profile_icon);
        linearLayout = findViewById(R.id.bottom_linear_layout);
        scrollView = findViewById(R.id.scroll_bar);
        progressBarDataLoading = findViewById(R.id.progress_dataLoading);
        ParentLayout = findViewById(R.id.ParentRelativeLayout);
        userNumberInputLayout = findViewById(R.id.user_number);
        progressBar = findViewById(R.id.progress);
        userNumber = findViewById(R.id.User_Number);
        nameInput = findViewById(R.id.nameee);
        addresInput = findViewById(R.id.addresss);
        batchInputLayout = findViewById(R.id.batchInputLayout);
        etBatch = findViewById(R.id.etBatch);
        name = findViewById(R.id.Name);
        address = findViewById(R.id.Address);
        save = findViewById(R.id.save_card);
        image_card = findViewById(R.id.image_card);
        text_upload = findViewById(R.id.text_upload_image);
        coachingIdImage = findViewById(R.id.file_image);
        uploadFileImage = findViewById(R.id.upload_file);
        deleteAccount = findViewById(R.id.open_delete_account);
        tv_app_version = findViewById(R.id.tv_app_version);

        String appVersion = MyApplication.getAppVersion();
        tv_app_version.setText(appVersion);
        tv_app_version.setVisibility(appVersion.isEmpty()?View.GONE:View.VISIBLE);

        name.setText(MyApplication.getStringData(PreferenceKeys.NAME.getKey()));
        userNumber.setText(MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey()));

        MyApplication application = (MyApplication) getApplication();
        homeScreenViewModel = application.getHomeScreenViewModel();

//        homeScreenViewModel = new ViewModelProvider(this).get(HomeScreenViewModel.class);


        deleteAccount.setOnClickListener(view -> {
            try {
                Map<String, String> map = new HashMap<>();
                map.put("token", MyApplication.getFCMToken());
                db.collection("Admin").document(String.valueOf(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey())))
                        .set(map, SetOptions.merge()).addOnSuccessListener(aVoid -> Log.d("Notification", " Set"));
                Log.i("FCM TOKEN ", MyApplication.getFCMToken());
            } catch (Exception ignored) {
                Log.d("error", "error");
            }
            Intent intent = new Intent(this, DeleteAccountActivity.class);
            startActivity(intent);
        });

        //name config
        UserNameValidationConfig config = UserNameValidationConfig.fromRemoteConfig();

        image_card.setOnClickListener(view -> openImagePicker(true));
        uploadFileImage.setOnClickListener(view -> openImagePicker(false));
        back.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
        name.setOnClickListener(view -> nameInput.setError(null));
        address.setOnClickListener(view -> addresInput.setError(null));

        //save
        save.setOnClickListener(view -> {
            nameInput.clearFocus();
            if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                CommonMethods.showNegativeSnackbar(ParentLayout, getString(R.string.NoInternetConnection));
            } else if (data_loaded == 0) {
                CommonMethods.showNegativeSnackbar(ParentLayout, getString(R.string.PleaseWaitForDataToLoad));
            } else {
                String input = name.getText().toString().trim();
                String error = config.getValidationError(input, getString(R.string.name_required_error));

                if (error != null) {
                    nameInput.setError(UserNameValidationConfig.fromHtml(error));
                    nameInput.requestFocus();
                } else if (address.getText().toString().length() > 180) {
                    addresInput.setError(getString(R.string.too_long_address));
                    addresInput.requestFocus();
                } else if (!isValidAddress(address.getText().toString()) && addresInput.isEnabled()) {
                    addresInput.setError(getString(R.string.invalid_address));
                    addresInput.requestFocus();
                } else {
                    if (imageChanged) {
                        uploadImage();
                        imageChanged = false;
                    } else {
                        progressBar.setVisibility(View.VISIBLE);
                        linearLayout.setVisibility(View.GONE);
                        saveData();
                    }
                }
            }
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.edit_prof_save_btn_click.getActionName());
        });

        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            if (isOnline) {
                if (dataLoaded != 1) {
                    dataLoaded = 1;
                    getAPIDataByPhoneData();
                }
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    CommonMethods.showPositiveSnackbar(ParentLayout, getString(R.string.InternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
                }
            } else {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                    CommonMethods.showNegativeSnackbar(ParentLayout, getString(R.string.NoInternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), false);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), true);
                }
            }
        });

        if (CommonMethods.isOnline(this)) {
            if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                if (dataLoaded != 1) {
                    dataLoaded = 1;
                    getAPIDataByPhoneData();
                }
                MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
            }
        } else {
            if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                CommonMethods.showNegativeSnackbar(ParentLayout, getString(R.string.NoInternetConnection));
                MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
            }
        }


    }

    private boolean checkIfPhoneNumberCorrect(String phone) {

        try {
            Long.parseLong(phone);
        } catch (Exception ignored) {
            return true;
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            startCrop(selectedImage);
        } else if (requestCode == 19 && resultCode == RESULT_OK) {
            Uri capturedImage = Uri.parse(MyApplication.getStringData(PreferenceKeys.CAMERA_CAPTURE_URI.getKey()));
            startCrop(capturedImage);
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                if (isProfileImage) profileIcon.setImageURI(resultUri);
                else coachingIdImage.setImageURI(resultUri);
            }
        }
    }

    private void saveData() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername(name.getText().toString().trim());
        request.setAddress(address.getText().toString().trim());
        if (user.getImageUrl() != null) {
            request.setImageUrl(user.getImageUrl());
        }

        try {
            Log.d("saveData", "saveData: api hit");

            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.PATCH,
                    PUT_UPDATE, new JSONObject(gson.toJson(request)), response -> {
                try {
                    if (Boolean.parseBoolean(response.get("success").toString())) {
                        data_saved = 1;
                        TypeToken<UserDetailsResponse> token = new TypeToken<>() {};
                        UserDetailsResponse updatedUser = gson.fromJson(response.getJSONObject("data").toString(), token.getType());

                        if (updatedUser != null && updatedUser.getUserDto() != null && StringUtils.isNotEmpty(updatedUser.getUserDto().getImageUrl())) {
                            user.setImageUrl(updatedUser.getUserDto().getImageUrl());
                            MyApplication.saveData(PreferenceKeys.IMAGE_URI.getKey(), updatedUser.getUserDto().getImageUrl());
                        }

                        //save username
                        if (updatedUser != null && updatedUser.getUserDto() != null) {
                            MyApplication.saveData(PreferenceKeys.NAME.getKey(), updatedUser.getUserDto().getUsername());
                            MyApplication.saveData(PreferenceKeys.DISPLAY_NAME.getKey(), updatedUser.getUserDto().getDisplayName());
                        }

//                        getAPIDataByPhoneData();
                        CommonMethods.showPositiveSnackbar(ParentLayout, getString(R.string.profile_updated_success));
                        linearLayout.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        homeScreenViewModel.removeDataModel();
                    }
                } catch (JSONException e) {
                    CommonMethods.showNegativeSnackbar(ParentLayout, e.getMessage());
                }
            }, new VolleyGlobalErrorListener(this) {
                @Override
                public void postError(VolleyError error) {
                    Log.d("saveData", "onErrorResponse: " + error.getMessage());
                    linearLayout.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    CommonMethods.showNegativeSnackbar(ParentLayout, error.getMessage());
                }
            });
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
        } catch (JSONException e) {
            save.setVisibility(View.VISIBLE);
            Log.d("error", e.getMessage());
        }
    }

    private void getAPIDataByPhoneData() {
        Map<String, Object> map = new HashMap<>();
        String phoneNumber = MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey());
        map.put("phoneNumber", phoneNumber);
        map.put("key", BuildConfig.API_KEY);
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(AV_GET_USER_BY_PHONE, map), null,
                response -> {
                    TypeToken<ApiWrappedResponse<UserDetailsResponse>> token = new TypeToken<ApiWrappedResponse<UserDetailsResponse>>() {};
                    ApiWrappedResponse<UserDetailsResponse> apiResponse = gson.fromJson(response.toString(), token.getType());
                    UserDetailsResponse userDetails = (apiResponse != null && apiResponse.isSuccess()) ? apiResponse.getData() : null;
                    if (userDetails != null && userDetails.getUserDto() != null) {
                        name.setText(userDetails.getUserDto().getUsername());

                        if (userDetails.getUserDto().getAddress() != null) {
                            address.setText(userDetails.getUserDto().getAddress());
                        }
                        if (StringUtils.isNotEmpty(userDetails.getUserDto().getImageUrl())) {
                            previousImageUrl = userDetails.getUserDto().getImageUrl();
                            user.setImageUrl(userDetails.getUserDto().getImageUrl());
                        }
                        data_loaded = 1;
                        progressBarDataLoading.setVisibility(View.GONE);
                        scrollView.setVisibility(View.VISIBLE);
                        if (data_saved == 0) {
                            updateImage();
                        }
                        MyApplication.saveUserProfileLocally(userDetails);

                        //set batch input layout
                        setBatchInputLayout();

                        homeScreenViewModel.removeDataModel();
                    }
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                dataLoaded = 0;
                CommonMethods.showNegativeSnackbar(ParentLayout, error.getMessage());
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
    }


    private void setBatchInputLayout(){
        batchInputLayout.setVisibility(View.GONE);

        //batch name
        String userBatchName = MyApplication.getStringData(PreferenceKeys.BATCH_DISPLAY_NAME.getKey());
        if (userBatchName == null || userBatchName.isBlank()) return;

        //NEET
        if(MyApplication.isCompetitiveProfile()) {
            boolean showBadge = MyApplication.getFirebaseRemoteConfig().getBoolean("show_profile_badge_for_neet");
            if (!showBadge || !SubscriptionNavigationHelper.hasActiveSubscriptionByBatch()) return;

            etBatch.setText(userBatchName);
        }
        //Board
        else {
            SubscriptionType subscriptionType = SubscriptionType.getSubscriptionType();
            if (subscriptionType == SubscriptionType.BASIC) return;

            etBatch.setText(subscriptionType == SubscriptionType.PRO ? getString(R.string.pro_plan) : userBatchName);
        }
        batchInputLayout.setVisibility(View.VISIBLE);
    }




    private void updateImage() {
        if (isDestroyed() || isFinishing()) {
            return;
        }

        if (StringUtils.isNotEmpty(user.getIdUrl())) {
            Glide.with(getApplicationContext()).load(user.getIdUrl()).into(coachingIdImage);
            coachingIdImage.setVisibility(View.VISIBLE);
            text_upload.setVisibility(View.GONE);
        }

        if (StringUtils.isNotEmpty(user.getImageUrl())) {
            Glide.with(getApplicationContext()).load(user.getImageUrl()).into(profileIcon);
        }

    }

    public static boolean isValidString(String s) {
        if (StringUtils.isEmpty(s)) {
            return false;
        }

        return s.trim().matches("^[a-zA-Z0-9_,. ]*$");
    }


    public static boolean isValidNumber(String s) {

        if (StringUtils.isEmpty(s))
            return true;
        else
            return s.trim().matches("^[0-9]*$");
    }

    public static boolean isValidAddress(String s) {

        if (StringUtils.isEmpty(s))
            return true;
        else
            return s.trim().matches("^[a-zA-Z0-9_,-.=%&*/\\n ]*$");
    }


}