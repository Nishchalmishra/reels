package arivihan.technologies.doubtbuzzter2.activity.asat;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.ASAT_CHECK_ATTEMPTED_WITH_ROLL_NO;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.EXTRACT_TEXT_FROM_IMAGE_URI;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.SAVE_ASAT_USER_MARKSHEET_INFO;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.FragmentManager;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageActivity;
import com.theartofdev.edmodo.cropper.CropImageOptions;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.BaseActivity;
import arivihan.technologies.doubtbuzzter2.activity.ImageCropperActivity;
import arivihan.technologies.doubtbuzzter2.adapter.TodayClassAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.MultipartRequestDataPart;
import arivihan.technologies.doubtbuzzter2.customView.SavBottomSheetDialog;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.helper.MyEmojiFilter;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.model.ImageTextExtractionResponse;
import arivihan.technologies.doubtbuzzter2.model.UserAsatInfo;
import arivihan.technologies.doubtbuzzter2.model.asat.UploadMarksheetResponse;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyMultipartRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class UploadMarksheetActivity extends BaseActivity implements SavBottomSheetDialog.BottomSheetListener {

    private static final String TAG = UploadMarksheetActivity.class.getSimpleName();
    EditText roll_no;
    EditText exam_percent;
    MaterialCardView cv_upload_file;
    CardView submit;
    Dialog imagePickDialog;
    private ImageView iv_back;
    private LottieAnimationView lottie_animation;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> cropImageLauncher;
    private ActivityResultLauncher<Intent> cropActivityLauncher;
    RelativeLayout rl_main_layout, image_layout;
    ImageView extracted_image;
    ImageView delete_image;
    ImageView crop_image;
    Uri currentPhotoPath;
    TextView upload_image_text, tv_note, tv_header, tv_sub_header;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    UserAsatInfo userAsatInfo;
    private Dialog progressDialog;
    private SavBottomSheetDialog savBottomSheetDialog;
    private String scholarShipInfoId;
    private int restrictedRollNoLengthMax, restrictedRollNoLengthMin;

    private boolean isCheckingRollNo = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_upload_marksheet);

        Uri deepLinkData = getIntent().getData();
        if (deepLinkData != null) {
            scholarShipInfoId = deepLinkData.getQueryParameter("scholarShipInfoId");
        } else {
            scholarShipInfoId = getIntent().getStringExtra("scholarShipInfoId");
        }

        iv_back = findViewById(R.id.iv_back);
        rl_main_layout = findViewById(R.id.rl_main_layout);
        image_layout = findViewById(R.id.image_layout);
        extracted_image = findViewById(R.id.extracted_image);
        delete_image = findViewById(R.id.delete_image);
        crop_image = findViewById(R.id.crop_image);
        roll_no = findViewById(R.id.tenth_roll);

        roll_no.setFilters(MyEmojiFilter.getFilter());
        exam_percent = findViewById(R.id.tenth_percent);
        cv_upload_file = findViewById(R.id.cv_upload_file);
        submit = findViewById(R.id.submit);
        upload_image_text = findViewById(R.id.upload_image_text);
        tv_note = findViewById(R.id.tv_note);
        tv_header = findViewById(R.id.tv_header);
        tv_sub_header = findViewById(R.id.tv_sub_header);

        lottie_animation = findViewById(R.id.lottie_animation);
        lottie_animation.playAnimation();

        upload_image_text.setPaintFlags(upload_image_text.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        int[] rollNumberLength = getRollNumberLength();
        restrictedRollNoLengthMin = rollNumberLength[0];
        restrictedRollNoLengthMax = rollNumberLength[1];
        roll_no.setFilters(new InputFilter[]{new InputFilter.LengthFilter(restrictedRollNoLengthMax)});
        exam_percent.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

        setNoteStr();
        initializeImagePickDialog();
        handleCropActivityLauncher();
        handleCameraPermissions();

        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                try {
                    String newValue = dest.toString().substring(0, dstart)
                            + source.subSequence(start, end)
                            + dest.toString().substring(dend);

                    if (newValue.isEmpty()) return null;

                    int input = Integer.parseInt(newValue);
                    if (input > 100) {
                        CommonMethods.showNegativeSnackbar(rl_main_layout, getString(R.string.please_correct_percentage_enter_karein), 3000);
                        return "";
                    }
                } catch (NumberFormatException e) {
                    CommonMethods.showNegativeSnackbar(rl_main_layout, getString(R.string.please_correct_percentage_enter_karein), 3000);
                    return "";
                }
                return null;
            }
        };
        exam_percent.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(3)});

        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                tv_header,
                12,
                25,
                1,
                TypedValue.COMPLEX_UNIT_SP
        );

        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                tv_sub_header,
                6,
                15,
                1,
                TypedValue.COMPLEX_UNIT_SP
        );

        cv_upload_file.setOnClickListener(view12 -> imagePickDialog.show());

        iv_back.setOnClickListener(v -> {
            onBack();
        });

        delete_image.setOnClickListener(view1 -> {
            cv_upload_file.setVisibility(View.VISIBLE);
            image_layout.setVisibility(View.GONE);
            currentPhotoPath = null;
        });

        crop_image.setOnClickListener(view14 -> startCropActivity(currentPhotoPath));

        submit.setOnClickListener(view13 -> {
            try {
                if (StringUtils.isEmpty(roll_no.getText().toString()) || (roll_no.getText().toString().length() < restrictedRollNoLengthMin || roll_no.getText().toString().length() > restrictedRollNoLengthMax)) {
                    if (StringUtils.isEmpty(roll_no.getText().toString())) {
                        CommonMethods.showNegativeSnackbar(rl_main_layout, getString(R.string.please_number_enter_karein), 3000);
                        return;
                    }
                    CommonMethods.showNegativeSnackbar(rl_main_layout, getString(R.string.please_correct_roll_number_enter_karein), 3000);
                    return;
                } else if (StringUtils.isEmpty(exam_percent.getText().toString()) || (exam_percent.getText().toString().length() > 3 || (StringUtils.isNumeric(exam_percent.getText().toString()) && Integer.parseInt(exam_percent.getText().toString()) > 100))) {
                    CommonMethods.showNegativeSnackbar(rl_main_layout, getString(R.string.please_correct_percentage_enter_karein), 3000);
                    return;
                } else if (!(currentPhotoPath != null && StringUtils.isNotEmpty(currentPhotoPath.toString()))) {
                    CommonMethods.showNegativeSnackbar(rl_main_layout, getString(R.string.please_image_upload_karein), 3000);
                    return;
                }

                if (CommonMethods.isOnline(this)) {
                    checkRollNumberOnServer(roll_no.getText().toString());
//                    saveUserMarksheet();
                } else {
                    CommonMethods.showNegativeSnackbar(rl_main_layout, getString(R.string.internet_failure_error), 2000);
                }
            } catch (Exception ignored) {
            }
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBack();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

    }


    private void checkRollNumberOnServer(String rollNumber) {
        if (StringUtils.isEmpty(rollNumber)) {
            return;
        }

        dismissProgressDialog();
        progressDialog = getProgressDialog(this, getString(R.string.please_wait_while_we_check_your_data));
        progressDialog.show();

        Map<String, Object> params = new HashMap<>();
        params.put("rollNo", rollNumber);

        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET, CommonMethods.getUrlParam(ASAT_CHECK_ATTEMPTED_WITH_ROLL_NO, params), null,
                response -> {
                    try {
                        dismissProgressDialog();
                        boolean alreadyRegistered = response.optBoolean("alreadyRegistered");
                        boolean testAttempted = response.optBoolean("testAttempted");

                        if (testAttempted) {
                            String message = getString(R.string.test_already_attempted);
                            showRollNumberStatusSnackbar(message, true);
                        } else if (alreadyRegistered) {
                            String message = getString(R.string.roll_no_already_submitted);
                            showRollNumberStatusSnackbar(message, true);
                        } else {
                            extractTextFromImage();
                        }
                    } catch (Exception e) {
                    }
                },
                error -> {
                    dismissProgressDialog();
                    if (error instanceof NoConnectionError) {
                        CommonMethods.showNegativeSnackbar(rl_main_layout, getString(R.string.internet_failure_error), 2000);
                    } else {
                        CommonMethods.showNegativeSnackbar(rl_main_layout, getString(R.string.failed_to_check_roll_no), 3000);
                    }
                }) {
        };

        myJsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
    }


    private void showRollNumberStatusSnackbar(String message, boolean isError) {
        if (isError) {
            CommonMethods.showNegativeSnackbar(rl_main_layout, message, 3000);
        } else {
            CommonMethods.showPositiveSnackbar(rl_main_layout, message, 3000);
        }
    }

    @Override
    protected void onDestroy() {
        if (lottie_animation != null) {
            lottie_animation.cancelAnimation();
            lottie_animation.clearAnimation();
        }
        if (imagePickDialog != null && imagePickDialog.isShowing()) {
            imagePickDialog.dismiss();
        }
        dismissProgressDialog();
        dismissSavBottomSheetDialog();
        savBottomSheetDialog = null;
        MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll(TAG);
        super.onDestroy();
    }

    private void onBack() {
        Intent intent = new Intent(this, ASATActivity.class);
        startActivity(intent);
        finish();
    }

    public static int[] getRollNumberLength() {
        int[] rollNumberLength = new int[2];
        rollNumberLength[0] = 8;    // Default Min
        rollNumberLength[1] = 10;   //Default Max
        try {
            String rollNumberJsonStr = MyApplication.getFirebaseRemoteConfig().getString("asat_upload_marksheet_roll_number_length");
            JSONObject rollNumberJson = new JSONObject(rollNumberJsonStr);

            String courseId = MyApplication.getStringData(PreferenceKeys.SELECTED_COURSE_ID_LOCAL.getKey());

            JSONObject courseData = rollNumberJson.optJSONObject(courseId);
            if (courseData == null) {
                courseData = rollNumberJson.getJSONObject("default"); // fallback to default
            }

            rollNumberLength[0] = courseData.getInt("roll_no_length_min");
            rollNumberLength[1] = courseData.getInt("roll_no_length_max");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rollNumberLength;
    }

    private void startCropActivity(Uri imageUri) {
        Intent cropIntent = CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                .getIntent(this);
        cropImageLauncher.launch(cropIntent);
    }

    private void initializeImagePickDialog() {

        cropImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                        CropImage.ActivityResult cropResult = CropImage.getActivityResult(result.getData());
                        if (cropResult != null) {
                            Uri croppedImageUri = cropResult.getUri();
                            if (croppedImageUri != null) {
                                Log.d("croppedImageUri", "Path" + croppedImageUri.getPath());
                                float size = getImageSize(croppedImageUri);
                                if (size <= 5) {
                                    cv_upload_file.setVisibility(View.GONE);
                                    image_layout.setVisibility(View.VISIBLE);
                                    currentPhotoPath = croppedImageUri;
                                    Glide.with(this).load(currentPhotoPath).into(extracted_image);
                                } else {
                                    Toast.makeText(this, "size larger than 5mb", Toast.LENGTH_LONG).show();
                                }
                            }
                        } else {
                            Toast.makeText(this, getString(R.string.cropping_failed), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );


        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                        try {
                            if (currentPhotoPath != null && cropActivityLauncher != null) {
                                Intent cropIntent = new Intent(this, CropImageActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putParcelable(CropImage.CROP_IMAGE_EXTRA_SOURCE, currentPhotoPath);
                                CropImageOptions cropImageOptions = new CropImageOptions();
                                cropImageOptions.guidelines = CropImageView.Guidelines.ON_TOUCH;
                                bundle.putParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS, cropImageOptions);
                                cropIntent.putExtra(CropImage.CROP_IMAGE_EXTRA_BUNDLE, bundle);
                                cropActivityLauncher.launch(cropIntent);
                            } else {
                                Toast.makeText(this, getString(R.string.please_try_again), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.d("handleImagePickerDialog", e.getMessage());
                        }
                    } else {
                        currentPhotoPath = null;
                    }
                }
        );

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK) {

                        if (result.getData() != null) {

                            Log.d("handleImagePickerDialog", result.getData().getStringArrayListExtra("url_list").toString());
                            ArrayList<String> urls = result.getData().getStringArrayListExtra("url_list");
                            for (int i = 0; i < urls.size(); i++) {
                                Uri photoUri = Uri.parse(urls.get(i));
                                Log.d("photoUri", "gallery = " + photoUri);
                                if (photoUri != null) {
                                    if (getImageSize(photoUri) <= 5) {
                                        cv_upload_file.setVisibility(View.GONE);
                                        image_layout.setVisibility(View.VISIBLE);
                                        currentPhotoPath = photoUri;
                                        Glide.with(this).load(currentPhotoPath).into(extracted_image);
                                    } else {
                                        Toast.makeText(this, "size larger than 5mb", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        }

                    }
                }
        );

        imagePickDialog = new Dialog(this);
        imagePickDialog.setContentView(R.layout.action_popup_layout);
        imagePickDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
        imagePickDialog.getWindow().setBackgroundDrawable(this.getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        imagePickDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        imagePickDialog.setCancelable(false);
        imagePickDialog.findViewById(R.id.close_btn).setOnClickListener(view -> imagePickDialog.cancel());
        TextView title = imagePickDialog.findViewById(R.id.title);
        title.setText(R.string.select_mesrksheet_image);
        TextView subtitle = imagePickDialog.findViewById(R.id.subtitle);
        subtitle.setVisibility(View.GONE);
        CardView btn1 = imagePickDialog.findViewById(R.id.btn1);
        CardView btn2 = imagePickDialog.findViewById(R.id.btn2);
        TextView btnText1 = imagePickDialog.findViewById(R.id.btn1_text);
        btnText1.setText(getString(R.string.Choose_from_Gallery));
        TextView btnText2 = imagePickDialog.findViewById(R.id.btn2_text);
        btnText2.setText(getString(R.string.Click_a_Picture));
        LottieAnimationView lottieAnimationView = imagePickDialog.findViewById(R.id.animation);
        lottieAnimationView.setVisibility(View.GONE);
        ImageView selectionImage = imagePickDialog.findViewById(R.id.selection_image);
        selectionImage.setVisibility(View.VISIBLE);

        btn1.setOnClickListener(view -> {
            openGallery();
            imagePickDialog.cancel();
        });
        btn2.setOnClickListener(view -> {
            imagePickDialog.cancel();
            checkCameraPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
        });
    }

    private void setNoteStr() {
        String title = getString(R.string.note);
        String subtitle = getString(R.string.note_text);

        String noteStr = String.format("""
                [
                  {
                    "text": "%s",
                    "font": "urbanist_semi_bold",
                    "color": "#26C6DA",
                    "style": "normal",
                    "size": 13
                  },
                  {
                    "text": "%s",
                    "font": "urbanist_medium",
                    "color": "#80000000",
                    "style": "normal",
                    "size": 13
                  }
                ]
                """, title, subtitle);
        tv_note.setText(StyledTextBuilder.fromJson(this, noteStr));
    }

    private float getImageSize(Uri imageUri) {
        String[] projection = {MediaStore.Images.Media.SIZE};
        Cursor cursor = getContentResolver().query(imageUri, projection, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int columnIndexSize = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
            long fileSizeInBytes = cursor.getLong(columnIndexSize);
            cursor.close();
            return (float) (fileSizeInBytes / 1024) / 1024;
        } else {
            return 0.0f;
        }
    }

    private void handleCropActivityLauncher() {
        cropActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                        CropImage.ActivityResult activityResult = CropImage.getActivityResult(result.getData());
                        if (activityResult != null) {
                            cv_upload_file.setVisibility(View.GONE);
                            image_layout.setVisibility(View.VISIBLE);
                            currentPhotoPath = activityResult.getUri();
                            Glide.with(this).load(currentPhotoPath).into(extracted_image);
                        }
                    } else {
                        Log.d("CropActivity", "Cropping was canceled or failed");
                    }
                }
        );
    }

    private void openGallery() {
        Intent intent = new Intent(this, ImageCropperActivity.class);
        Toast.makeText(getApplicationContext(), String.format(getString(R.string.You_can_select_up_to_images), 1), Toast.LENGTH_LONG).show();
        intent.putExtra("allowedImages", 1);
        galleryLauncher.launch(intent);
    }

    private void handleCameraPermissions() {
        cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean isGranted) {
                if (isGranted) {
                    captureImage();
                } else {
                    // Permission denied
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(UploadMarksheetActivity.this, Manifest.permission.CAMERA)) {
                        // User has denied permission permanently
                        Dialog dialog = CommonMethods.getCameraPermissionDialog(UploadMarksheetActivity.this);
                        dialog.show();
                    } else {
                        Toast.makeText(UploadMarksheetActivity.this, getString(R.string.camera_permission_zaruri_hai_upload_ke_liye), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void captureImage() {
        String filename = "photo";
        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            File imageFile = File.createTempFile(filename, "_" + timeStamp + "_" + ".jpg", storageDirectory);
            Uri imageuri = FileProvider.getUriForFile(getApplicationContext(),
                    BuildConfig.APPLICATION_ID + ".provider", imageFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri);
            currentPhotoPath = imageuri;
            cameraLauncher.launch(intent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void checkCameraPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
            cameraPermissionLauncher.launch(permission);
        } else {
            captureImage();
        }
    }

    private void saveUserMarksheet() {

        dismissSavBottomSheetDialog();
        dismissProgressDialog();
        progressDialog = getProgressDialog(this, getString(R.string.please_wait_while_we_upload_your_data));
        progressDialog.show();

//        showSavBottomSheetDialog();

        Map<String, Object> params = new HashMap<>();
        params.put("percent", exam_percent.getText().toString());
        params.put("roll", roll_no.getText().toString());
        params.put("scholarShipInfoId", scholarShipInfoId);
        MyMultipartRequest multipartRequest = new MyMultipartRequest(Request.Method.POST,
                CommonMethods.getUrlParam(SAVE_ASAT_USER_MARKSHEET_INFO, params),
                response -> {
                    try {

//                dismissSavBottomSheetDialog();
                        dismissProgressDialog();

                        TypeToken<UploadMarksheetResponse> token = new TypeToken<UploadMarksheetResponse>() {
                        };
                        Gson gson = new Gson();
                        UploadMarksheetResponse uploadMarksheetResponse = gson.fromJson(new String(response.data, StandardCharsets.UTF_8), token.getType());

                        if (uploadMarksheetResponse.isSuccess()) {
//                    Intent resultIntent = new Intent();
//                    resultIntent.putExtra(ASATActivity.ASAT_UPLOAD_MARKSHEET, uploadMarksheetResponse);
//                    setResult(Activity.RESULT_OK, resultIntent);

                            //trigger event of asat marksheet submitted
                            Map<String, String> param = new HashMap<>();
                            param.put("screen", UploadMarksheetActivity.class.getSimpleName());
                            param.put("roll_no", roll_no.getText().toString());
                            param.put("percentage", exam_percent.getText().toString());
                            param.put("scholarShipInfoId", scholarShipInfoId);
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.asat_upload_marksheet_submit.getActionName(), param);

                            Intent intent = new Intent(this, ASATActivity.class);
                            startActivity(intent);

                            finish();
                        } else {
                            String message = uploadMarksheetResponse.getMessage() != null ? uploadMarksheetResponse.getMessage() : getString(R.string.something_went_wrong);
                            CommonMethods.showNegativeSnackbar(rl_main_layout, message, 3000);
                        }

                        Log.d(TAG, "Response: " + uploadMarksheetResponse.toString());
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage(), e);
                        CommonMethods.showNegativeSnackbar(rl_main_layout, getString(R.string.something_went_wrong));
                    }
                }, error -> {
            Log.d(TAG, "Response : " + error.toString());
            if (error instanceof NoConnectionError) {
                CommonMethods.showNegativeSnackbar(rl_main_layout, getString(R.string.internet_failure_error));
            }
            dismissProgressDialog();
//            dismissSavBottomSheetDialog();
        }) {
            @Override
            protected Map<String, MultipartRequestDataPart> getByteData() {

                Map<String, MultipartRequestDataPart> params = new HashMap<>();
                File file = new File(currentPhotoPath.getPath());
                try {
                    byte[] fileData = CommonMethods.readFileToByteArray(file);
                    String fileName = file.getName();
                    String contentType = CommonMethods.getMimeType(fileName);
                    params.put("file", new MultipartRequestDataPart(fileName, fileData, contentType));

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return params;
            }
        };
        multipartRequest.setTag(TAG);

        Volley.newRequestQueue(this).add(multipartRequest).setRetryPolicy(new DefaultRetryPolicy(
                300000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void extractTextFromImage() {
        Map<String, Object> params = new HashMap<>();
        params.put("savType", true);
        showSavBottomSheetDialog();
        MyMultipartRequest multipartRequest = new MyMultipartRequest(Request.Method.POST,
                CommonMethods.getUrlParam(EXTRACT_TEXT_FROM_IMAGE_URI, params), response -> {
            try {
                TypeToken<ImageTextExtractionResponse> token = new TypeToken<ImageTextExtractionResponse>() {
                };
                Gson gson = new Gson();
                ImageTextExtractionResponse extractionResponse = gson.fromJson(new String(response.data, StandardCharsets.UTF_8), token.getType());
                Log.d(TAG, "RESPONSE : " + extractionResponse.toString());

                dismissSavBottomSheetDialog();
                if (extractionResponse.isTextExtracted()) {
                    saveUserMarksheet();
                } else {
                    CommonMethods.showNegativeSnackbar(rl_main_layout, getString(R.string.image_clear_nahi_hai_marksheet_eupload_karein));
                }

            } catch (Exception e) {
                Log.e("MULTIPARTRES", e.getMessage(), e);
            }

        }, error -> {
            Log.d(TAG, "Response : " + error.toString());
            if (error instanceof NoConnectionError) {
                CommonMethods.showNegativeSnackbar(rl_main_layout, getString(R.string.internet_failure_error), 2000);
            }
            dismissSavBottomSheetDialog();

        }) {
            @Override
            protected Map<String, MultipartRequestDataPart> getByteData() {

                Map<String, MultipartRequestDataPart> params = new HashMap<>();
                File file = new File(currentPhotoPath.getPath());
                try {
                    byte[] fileData = CommonMethods.readFileToByteArray(file);
                    String fileName = file.getName();
                    String contentType = CommonMethods.getMimeType(fileName);
                    params.put("file", new MultipartRequestDataPart(fileName, fileData, contentType));

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return params;
            }
        };

        multipartRequest.setTag(TAG);
        Volley.newRequestQueue(this).add(multipartRequest).setRetryPolicy(new DefaultRetryPolicy(
                300000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    public static Dialog getProgressDialog(Context context, String status) {

        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_progress_status);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(AppCompatResources.getDrawable(context,
                R.drawable.bachground_custom_dialogu_typeactivity));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);

        TextView tv_status = dialog.findViewById(R.id.tv_status);
        tv_status.setText(StringUtils.isNotEmpty(status) ? status : context.getString(R.string.please_wait));

        return dialog;
    }

    private void showSavBottomSheetDialog() {
        try {
            if (savBottomSheetDialog != null && savBottomSheetDialog.isAdded()) {
                // Already shown or in fragment manager
                return;
            }

            String asatTextExtractionString = MyApplication.getFirebaseRemoteConfig().getString("asat_text_extraction_string_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
            savBottomSheetDialog = new SavBottomSheetDialog(this);
            ArrayList<String> textList = new ArrayList<>();
            if (StringUtils.isNotEmpty(asatTextExtractionString)) {
                textList.addAll(new ArrayList<>(Arrays.asList(asatTextExtractionString.split("::"))));
            } else {
                textList.add(getResources().getString(R.string.text_extraction_1));
                textList.add(getResources().getString(R.string.text_extraction_2));
                textList.add(getResources().getString(R.string.text_extraction_10));
                textList.add(getResources().getString(R.string.text_extraction_4));
//        textList.add(getResources().getString(R.string.text_extraction_5));
            }

            String strList = StringUtils.join(textList, "::");
            Bundle bundle = new Bundle();
            bundle.putString("finalText", "");
            bundle.putString("textList", strList);
            savBottomSheetDialog.setArguments(bundle);
            savBottomSheetDialog.setCancelable(false);

            FragmentManager fragmentManager = getSupportFragmentManager();
            if (!fragmentManager.isStateSaved()) {
                savBottomSheetDialog.show(fragmentManager, SavBottomSheetDialog.TAG);
            }
        } catch (Exception ignored) {
        }
    }

    private void dismissSavBottomSheetDialog() {
        if (savBottomSheetDialog != null &&
                savBottomSheetDialog.isAdded() &&
                savBottomSheetDialog.getDialog() != null &&
                savBottomSheetDialog.getDialog().isShowing()) {
            savBottomSheetDialog.dismissAllowingStateLoss(); // using dismissAllowingStateLoss to avoid crash
        }
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    public void onGalleryClick(boolean textNotFound) {

    }

    @Override
    public void onPictureClick(boolean textNotFound) {

    }

    @Override
    public void onCloseClick() {
        dismissSavBottomSheetDialog();
        MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll(TAG);
    }
}