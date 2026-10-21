package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_CHAPTER_LIST;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_USER_FREE_DOUBT;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_USER_POINTS;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.helper.NotificationHelper;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.model.ChapterListInfoDto;
import arivihan.technologies.doubtbuzzter2.model.NotificationBody;
import arivihan.technologies.doubtbuzzter2.model.UserDto;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class ClickDoubtPictureActivity extends BaseActivity {

    private static final int pic_id = 123;
    private static final int CAMERA_PERMISSION_CODE = 100;
    ImageView image, back;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String currentPhotoPath = "";
    String userEmail;
    private Spinner chapter;
    Dialog dialog2;
    CardView Ask;
    ImageView capture;
    ProgressBar progressBar;
    Uri uri;
    int imageAdded = 0;
    EditText description;
    RelativeLayout ParentLayout;
    private Uri imageuri;
    private ArrayList<String> listSub = new ArrayList<>();
    private ArrayList<String> list = new ArrayList<>();
    private ArrayList<String> bioList = new ArrayList<>();
    private ArrayList<String> phyList = new ArrayList<>();
    private ArrayList<String> chemList = new ArrayList<>();
    private ArrayList<String> mathsList = new ArrayList<>();
    private Spinner subject;
    String selectedSubject = "";
    String selectedChapter = "";
    private Boolean dataLoaded = false;
    private TextView Yourpoints;
    private MaterialCardView captureCard;

    private Handler handler;

    AlertDialog.Builder builder;
    NetworkStatusLiveData networkStatusLiveData;

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_doubt_picture);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        image = findViewById(R.id.doubt_image);
        back = findViewById(R.id.back_arrow);
        Yourpoints = findViewById(R.id.Points);
        Ask = findViewById(R.id.ask);
        progressBar = findViewById(R.id.progress_dataLoading);
        description = findViewById(R.id.description);
        ParentLayout = findViewById(R.id.ParentRelativeLayout);
        capture = findViewById(R.id.capture_Image);
        chapter = (Spinner) findViewById(R.id.chapter);
        subject = (Spinner) findViewById(R.id.subject);
        captureCard = findViewById(R.id.capture_card);
        Yourpoints.setText(getString(R.string.Your_points) + " : " + MyApplication.getIntData(PreferenceKeys.YOUR_POINTS.getKey()));


        userEmail = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        builder = new AlertDialog.Builder(this);
        builder.setMessage("Open settings to allow camera permission");
        builder.setPositiveButton("Open Settings", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        dialog2 = new Dialog(ClickDoubtPictureActivity.this);
        dialog2.setContentView(R.layout.dialog_doubt_picture_click);
        dialog2.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        dialog2.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog2.setCancelable(false);

        CardView use_points = dialog2.findViewById(R.id.ask_points);
        CardView use_free_points = dialog2.findViewById(R.id.revise_concept);
        CardView cut = dialog2.findViewById(R.id.cancel);

        cut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog2.dismiss();
            }
        });

        use_points.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MyApplication.getIntData(PreferenceKeys.YOUR_POINTS.getKey()) >= 5) {
                    if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                        CommonMethods.showNegativeSnackbar(ParentLayout, getString(R.string.NoInternetConnection));
                    } else {
                        Ask.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                        getAndUploadQuestion(0);
                    }
                } else {
                    CommonMethods.showNegativeSnackbar(ParentLayout, "You don't have enough Points");
                }
                dialog2.dismiss();
            }
        });

        use_free_points.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MyApplication.getIntData(PreferenceKeys.FREE_DOUBT.getKey()) > 0) {
                    if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                        CommonMethods.showNegativeSnackbar(ParentLayout, getString(R.string.NoInternetConnection));
                    } else {
                        Ask.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                        getAndUploadQuestion(1);
                    }
                } else {
                    CommonMethods.showNegativeSnackbar(ParentLayout, "You don't have Free Doubt");
                }
                dialog2.dismiss();
            }
        });


        Ask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageAdded == 0)
                    CommonMethods.showNegativeSnackbar(ParentLayout, getString(R.string.AddImage));
                else if (selectedSubject.length() == 0)
                    CommonMethods.showNegativeSnackbar(ParentLayout, getString(R.string.selectSubject));
                else if (selectedChapter.length() == 0)
                    CommonMethods.showNegativeSnackbar(ParentLayout, getString(R.string.selectChapter));
                else {
                    dialog2.show();
                }
            }
        });

        capture.setOnClickListener(view -> handlePermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE));

        captureCard.setOnClickListener(view -> handlePermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE));

        listSub.add("Select Chapter");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(ClickDoubtPictureActivity.this,
                android.R.layout.simple_spinner_item, listSub);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataAdapter.notifyDataSetChanged();

        String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());

        ArrayAdapter<CharSequence> adapter1;
        if ("hi-rIN".equals(lang) || "hi".equals(lang)) {
            adapter1 = ArrayAdapter.createFromResource(this, R.array.select_subject_hindi, android.R.layout.simple_spinner_item);
        } else if ("gu".equals(lang)) {
            adapter1 = ArrayAdapter.createFromResource(this, R.array.select_subject_gujarati, android.R.layout.simple_spinner_item);
        } else if ("mr".equals(lang)) {
            adapter1 = ArrayAdapter.createFromResource(this, R.array.select_subject_marathi, android.R.layout.simple_spinner_item);
        } else {
            adapter1 = ArrayAdapter.createFromResource(this, R.array.select_subject, android.R.layout.simple_spinner_item);
        }

        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subject.setAdapter(adapter1);
        subject.setSelection(0);
        subject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position > 0)
                    selectedSubject = parent.getSelectedItem().toString();

                if (position == 0) {
                    list.clear();
                    list.add(getString(R.string.select_chapters));
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(ClickDoubtPictureActivity.this,
                            android.R.layout.simple_spinner_item, list);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    dataAdapter.notifyDataSetChanged();
                    chapter.setAdapter(dataAdapter);
                }

                if (position == 1) {
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(ClickDoubtPictureActivity.this,
                            android.R.layout.simple_spinner_item, mathsList);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    dataAdapter.notifyDataSetChanged();
                    chapter.setAdapter(dataAdapter);
                } else if (position == 2) {
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(ClickDoubtPictureActivity.this,
                            android.R.layout.simple_spinner_item, phyList);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    dataAdapter.notifyDataSetChanged();
                    chapter.setAdapter(dataAdapter);
                } else if (position == 3) {
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(ClickDoubtPictureActivity.this,
                            android.R.layout.simple_spinner_item, chemList);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    dataAdapter.notifyDataSetChanged();
                    chapter.setAdapter(dataAdapter);
                } else if (position == 4) {
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(ClickDoubtPictureActivity.this,
                            android.R.layout.simple_spinner_item, bioList);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    dataAdapter.notifyDataSetChanged();
                    chapter.setAdapter(dataAdapter);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                chapter.setSelection(0);
            }
        });

        chapter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i > 0)
                    selectedChapter = adapterView.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            if (isOnline) {
                if (!dataLoaded) {
                    dataLoaded = true;
                    getChapterNameAPI();
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
            if (!dataLoaded) {
                dataLoaded = true;
                getChapterNameAPI();
            }
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

    }

    private void getAndUploadQuestion(int pos) {

        String token = MyApplication.getStringData(PreferenceKeys.DEVICE_TOKEN.getKey());

        if (token != null) {
            String fullName = getName();
            uploadQuestion(pos, token, fullName);
        } else {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                String deviceToken = task.getResult();
                MyApplication.saveData(PreferenceKeys.DEVICE_TOKEN.getKey(), deviceToken);
                String fullName = getName();
                uploadQuestion(pos, deviceToken, fullName);
            }).addOnFailureListener(e -> progressBar.setVisibility(View.GONE));
        }
    }

    private String getName() {
        String fullName = MyApplication.getStringData(PreferenceKeys.NAME.getKey());
        for (int i = 0; i < fullName.length(); i++) {
            if (fullName.charAt(i) == ' ') {
                fullName = fullName.substring(0, i);
                break;
            }
        }
        return fullName;
    }

    private void uploadQuestion(int pos, String token, String fullName) {

        String doubtIdV = userEmail + getAlphaNumericString(14);
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        final StorageReference thisImageRef = storageRef.child("images/" + userEmail + "/" + doubtIdV);
        String imageReference = "images/" + userEmail + "/" + doubtIdV;
        image.setDrawingCacheEnabled(true);
        image.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        final UploadTask uploadTask = thisImageRef.putBytes(data);


        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return thisImageRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {


            if (task.isSuccessful()) {
                Uri downUri = task.getResult();
                uri = downUri;
                Log.d("ReyMysterio", "onComplete: Url: " + downUri.toString());
                String number = MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey());

                String randomString = getAlphaNumericString(14);
                String doubtIdValue = userEmail + randomString;
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                Map<String, Object> dataToSave = new HashMap<>();
                dataToSave.put("Text", "");
                dataToSave.put("Approved", false);
                dataToSave.put("AskedAt", timeStamp);
                dataToSave.put("AskedBy", userEmail);
                dataToSave.put("Chapter", selectedChapter);
                dataToSave.put("Subject", selectedSubject);
                dataToSave.put("PreferredFormat", "Image");
                dataToSave.put("Description", description.getText().toString().trim());
                dataToSave.put("DoubtID", doubtIdValue);
                dataToSave.put("DoubtNumber", randomString);
                dataToSave.put("Solved", false);
                dataToSave.put("Taken", false);
                dataToSave.put("ImageID", String.valueOf(uri));
                dataToSave.put("DoubtPrice", 10);
                dataToSave.put("TakenBy", "");
                dataToSave.put("DoubtPoints", 5);
                dataToSave.put("Token", token);
                dataToSave.put(PreferenceKeys.NAME.getKey(), fullName);

                Log.i("URI ", String.valueOf(uri));

                if (pos == 0) {
                    db.collection("Doubt").document(doubtIdValue)
                            .set(dataToSave).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    reducePoints();
                                    NotificationHelper notificationHelper = new NotificationHelper(ClickDoubtPictureActivity.this);
                                    notificationHelper.sendNotification(NotificationBody
                                            .builder()
                                            .doubtId(doubtIdValue)
                                            .askedDoubt(true)
                                            .description("")
                                            .text(getString(R.string.doubt_Asked))
                                            .imageUrl(String.valueOf(uri))
                                            .build());
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(),
                                            getString(R.string.submission_failed),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                } else if (pos == 1) {
                    db.collection("Doubt").document(doubtIdValue)

                            .set(dataToSave).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    UseFreeDoubts();
                                    NotificationHelper notificationHelper = new NotificationHelper(ClickDoubtPictureActivity.this);
                                    notificationHelper.sendNotification(NotificationBody
                                            .builder()
                                            .doubtId(doubtIdValue)
                                            .askedDoubt(true)
                                            .description("")
                                            .text(getString(R.string.doubt_Asked))
                                            .imageUrl(String.valueOf(uri))
                                            .build());
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(),
                                            getString(R.string.submission_failed),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                }

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    private void getChapterNameAPI() {
        Map<String, Object> map = new HashMap<>();
        String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        if ("hi".equals(lang) || "hi-rIN".equals(lang)) {
            map.put("language", "HINDI");
        } else if ("mr".equals(lang)) {
            map.put("language", "MARATHI");
        } else if ("gu".equals(lang)) {
            map.put("language", "GUJARATI");
        }
        MyJsonArrayRequest myJsonObjectRequest = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_CHAPTER_LIST, map), null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Gson gson = new Gson();
                        TypeToken<List<ChapterListInfoDto>> token = new TypeToken<List<ChapterListInfoDto>>() {
                        };
                        List<ChapterListInfoDto> chapterListInfoDtos = gson.fromJson(response.toString(), token.getType());
                        updateDataInRecyclerView("Mathematics", chapterListInfoDtos, mathsList);
                        updateDataInRecyclerView("Biology", chapterListInfoDtos, bioList);
                        updateDataInRecyclerView("Physics", chapterListInfoDtos, phyList);
                        updateDataInRecyclerView("Chemistry", chapterListInfoDtos, chemList);
                    }
                }, new VolleyGlobalErrorListener(ClickDoubtPictureActivity.this) {
            @Override
            public void postError(VolleyError error) {
                dataLoaded = false;
                CommonMethods.showNegativeSnackbar(ParentLayout, error.getMessage());
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);

    }

    private void updateDataInRecyclerView(String subject, List<ChapterListInfoDto> chapterListInfoDtos, ArrayList<String> list) {
        list.add("Select Chapter");
        for (ChapterListInfoDto chapterInfo : chapterListInfoDtos) {
            if (chapterInfo.getSubject().equals(subject)) {
                list.add(chapterInfo.getVideoTitle());
            }
        }
        list.add("Don't know");
    }


//    public void checkPermission(String permission, int requestCode) {
//        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)
//            captureImage();
//        else {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
//                ActivityCompat.requestPermissions(ClickDoubtPictureActivity.this, new String[]{permission}, requestCode);
//            else
//                captureImage();
//        }
//    }


    private void reducePoints() {
        Map<String, Object> map = new HashMap<>();
        String UserId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
        map.put(PreferenceKeys.USER_ID.getKey(), UserId);
        map.put("points", -5);
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_USER_POINTS, map), null,
                response -> {
                    Gson gson = new Gson();
                    TypeToken<UserDto> token = new TypeToken<UserDto>() {
                    };
                    UserDto list = gson.fromJson(response.toString(), token.getType());
                    int points = list.getPoints();
                    MyApplication.saveData(PreferenceKeys.YOUR_POINTS.getKey(), points);
                    progressBar.setVisibility(View.GONE);
                    if (getApplicationContext() != null) {
                        Yourpoints.setText(getString(R.string.Your_points) + " : " + MyApplication.getIntData(PreferenceKeys.YOUR_POINTS.getKey()));
                    }
                    if (getApplicationContext() != null)
                        CommonMethods.showPositiveSnackbar(ParentLayout, "Your Doubt Submitted");
                    handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getOnBackPressedDispatcher().onBackPressed();
                        }
                    }, 1025);

                }, new VolleyGlobalErrorListener(ClickDoubtPictureActivity.this) {
            @Override
            public void postError(VolleyError error) {

                Ask.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                CommonMethods.showNegativeSnackbar(ParentLayout, error.getMessage());
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);

    }




    private void UseFreeDoubts() {
        Map<String, Object> map = new HashMap<>();
        String UserId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
        map.put(PreferenceKeys.USER_ID.getKey(), UserId);
        map.put("points", -1);
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_USER_FREE_DOUBT, map), null,
                response -> {
                    Gson gson = new Gson();
                    TypeToken<UserDto> token = new TypeToken<UserDto>() {
                    };
                    UserDto list = gson.fromJson(response.toString(), token.getType());
                    int doubts = list.getFreeDoubt();
                    MyApplication.saveData(PreferenceKeys.FREE_DOUBT.getKey(), doubts);
                    progressBar.setVisibility(View.GONE);
                    if (getApplicationContext() != null)
                        CommonMethods.showPositiveSnackbar(ParentLayout, "Your Doubt Submitted");
                    handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getOnBackPressedDispatcher().onBackPressed();
                        }
                    }, 1025);

                }, new VolleyGlobalErrorListener(ClickDoubtPictureActivity.this) {
            @Override
            public void postError(VolleyError error) {
                CommonMethods.showNegativeSnackbar(ParentLayout, error.getMessage());
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
    }

    private void captureImage() {
        String filename = "photo";
        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File imageFile = File.createTempFile(filename, ".jpg", storageDirectory);
            currentPhotoPath = imageFile.getAbsolutePath();

            imageuri = FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()),
                    BuildConfig.APPLICATION_ID + ".provider", imageFile);
            imageAdded = 1;

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri);

            startActivityForResult(intent, 1);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            CropImage.activity(imageuri)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                try {
                    if (result == null) {
                        return;
                    }
                    uri = result.getUri();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    image.setImageBitmap(bitmap);
                    imageAdded = 1;
                } catch (Exception error) {
                    Log.e("resultUri", error.getMessage(), error);
                }
            }
        }
    }

    public static String getAlphaNumericString(int n) {

        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int) (AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }


    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    public void handlePermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            captureImage();
        } else  if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            builder.show();
        } else {
             ActivityCompat.requestPermissions(ClickDoubtPictureActivity.this, new String[]{permission}, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureImage();
            }else{

            }
        }
    }
}