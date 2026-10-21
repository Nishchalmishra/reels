package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.COURSES_LIST;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_CHAPTER_LIST;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_SUBSCRIBED_SUBJECT;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_USER_FREE_DOUBT;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.LEVEL_lIST;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.SUBMIT_CUSTOM_CHAPTER_REQUEST;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.model.ApiResponse;
import arivihan.technologies.doubtbuzzter2.model.ChapterListInfoDto;
import arivihan.technologies.doubtbuzzter2.model.CustomChapterRequest;
import arivihan.technologies.doubtbuzzter2.model.NameIdModel;
import arivihan.technologies.doubtbuzzter2.model.subject.SubjectResponse;
import arivihan.technologies.doubtbuzzter2.model.UserDto;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

/*
 * File: CustomFormActivity Copyright 13/05/23, 9:48 AM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 13/05/23, 9:48 AM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

public class CustomFormActivity extends BaseActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private int notificationNumber;
    LinearLayout back_arrow;

    private boolean imageDoubt;
    private String imageReference;
    private Gson gson;

    private ArrayList<String> bioList = new ArrayList<>();
    private ArrayList<String> phyList = new ArrayList<>();
    private ArrayList<String> chemList = new ArrayList<>();
    private ArrayList<String> mathsList = new ArrayList<>();
    private ArrayList<String> list = new ArrayList<>();

    RelativeLayout ParentLayout;
    private Boolean dataLoaded = false;

    private TextView Yourpoints;
    private EditText topic;
    ProgressBar progressBar;
    private TextInputLayout doubtt;
    private EditText description;
    private String selectedSubject = "";
    private String selectedChapter = "";
    private String selectedExam = "";

    private Spinner chapter, level;
    private Spinner subject;
    private Spinner selectExam;
    private CardView buttonSubmit;
    Dialog dialog2;
    private String doubtIdValue;

    private String userEmail;
    private String randomString;
    AppUpdateManager appUpdateManager;
    private static final int MY_REQUEST_CODE = 101;
    private String lang;

    private String timeStamp ;

    private List<String> subjectList = new ArrayList<>();
    private List<String> examList = new ArrayList<>();
    private List<String> levelList = new ArrayList<>();
    private ArrayAdapter<String> subjectAdapter;
    private ArrayAdapter<String> examAdapter;
    private ArrayAdapter<String> levelAdapter;
    private String selectedLevel  = "";
    private TextInputLayout descriptionInputView;

    String subjectName;
    TextView title;
    Handler handler;
    NetworkStatusLiveData networkStatusLiveData;

    @Override
    public void onPause() {
        super.onPause();
        if(handler!=null)
            handler.removeCallbacksAndMessages(null);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_form);

        String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        gson  =new Gson();
        descriptionInputView = findViewById(R.id.descriptionnn);
        description = (EditText)findViewById(R.id.description);
        doubtt = findViewById(R.id.doubttt);
        chapter = (Spinner) findViewById(R.id.chapter);
        topic = (EditText)findViewById(R.id.topic);
        back_arrow = findViewById(R.id.back_arrow);
        progressBar = findViewById(R.id.progress_dataLoading);
        subject = (Spinner) findViewById(R.id.subject);
        ParentLayout = findViewById(R.id.ParentRelativeLayout);
        selectExam = (Spinner) findViewById(R.id.select_exam);
        level = (Spinner) findViewById(R.id.select_level);

        buttonSubmit = findViewById(R.id.buttonSubmit);

        userEmail = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());

        subjectAdapter = new ArrayAdapter<>(CustomFormActivity.this, android.R.layout.simple_spinner_item, subjectList);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subject.setAdapter(subjectAdapter);
        subject.setSelection(0);

        title=findViewById(R.id.title);
        subjectName =getIntent().getStringExtra("SubjectName");
        title.setText(subjectName);

        getSubjectList();

        subject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    selectedSubject = parent.getSelectedItem().toString();
                }

                if(selectedSubject.equalsIgnoreCase("mathematics")) {
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(CustomFormActivity.this,
                            android.R.layout.simple_spinner_item, mathsList);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    dataAdapter.notifyDataSetChanged();
                    chapter.setAdapter(dataAdapter);
                }
                else if(selectedSubject.equalsIgnoreCase("physics")) {
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(CustomFormActivity.this,
                            android.R.layout.simple_spinner_item, phyList);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    dataAdapter.notifyDataSetChanged();
                    chapter.setAdapter(dataAdapter);
                }
                else if(selectedSubject.equalsIgnoreCase("chemistry")) {
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(CustomFormActivity.this,
                            android.R.layout.simple_spinner_item, chemList);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    dataAdapter.notifyDataSetChanged();
                    chapter.setAdapter(dataAdapter);
                }  else if(selectedSubject.equalsIgnoreCase("biology")) {
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(CustomFormActivity.this,
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
                if(i != 0){
                    selectedChapter = adapterView.getSelectedItem().toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        examAdapter = new ArrayAdapter<>(CustomFormActivity.this, android.R.layout.simple_spinner_item, examList);
        levelList.add(getString(R.string.select_level));
        levelAdapter = new ArrayAdapter<>(CustomFormActivity.this, android.R.layout.simple_spinner_item, levelList);

        examAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        selectExam.setAdapter(examAdapter);
        getExamList();

        level.setAdapter(levelAdapter);
        getLevelData();

        selectExam.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position != 0)
                    selectedExam = parent.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                chapter.setSelection(0);
            }
        });

        level.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0)
                    selectedLevel = parent.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                chapter.setSelection(0);
            }
        });



        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(topic.getText().toString().trim().length() == 0){
                    doubtt.setError(getString(R.string.enter_ur_topic));
                    doubtt.requestFocus();
                } else if (description.getText().toString().trim().length() == 0) {
                    descriptionInputView.setError(getString(R.string.desing_lecture));
                    descriptionInputView.requestFocus();
                } else if(selectedSubject.length() == 0){
                    CommonMethods.showNegativeSnackbar(ParentLayout,getString(R.string.selectSubject));
                }
                else if(selectedChapter.length() == 0){
                    CommonMethods.showNegativeSnackbar(ParentLayout,getString(R.string.selectChapter));
                }
                else if(selectedExam.length() == 0){
                    CommonMethods.showNegativeSnackbar(ParentLayout,getString(R.string.please_select_exam));
                }
                else if (selectedLevel.length() == 0){
                    CommonMethods.showNegativeSnackbar(ParentLayout,getString(R.string.please_select_level));
                } else {
                    submitCustomChapterRequest(topic.getText().toString(), description.getText().toString(),
                            selectedSubject, selectedChapter, selectedExam, selectedLevel);
                }
            }
        });

        back_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            if(isOnline){
                if(!dataLoaded){
                    dataLoaded = true;
                    getChapterNameAPI();
                }
                if(!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    CommonMethods.showPositiveSnackbar(ParentLayout,getString(R.string.InternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(),true);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(),false);
                }
            }
            else{
                if(!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())){
                    CommonMethods.showNegativeSnackbar(ParentLayout,getString(R.string.NoInternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(),false);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(),true);
                }
            }
        });

        if(CommonMethods.isOnline(this)){
            if(!dataLoaded){
                dataLoaded = true;
                getChapterNameAPI();
            }
            if(!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(),true);
                MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(),false);
            }
        }
        else{
            if(!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())){
                MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(),true);
                MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(),false);
            }
        }

    }

    private void submitCustomChapterRequest(String topic, String description,
                                            String selectedSubject, String selectedChapter,
                                            String selectedExam, String selectedLevel) {

        try {
            buttonSubmit.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            CustomChapterRequest customChapterRequest =
                    CustomChapterRequest
                            .builder()
                            .selectedExam(selectedExam)
                            .selectedLevel(selectedLevel)
                            .description(description.trim())
                            .topic(topic.trim())
                            .selectedSubject(selectedSubject)
                            .selectedChapter(selectedChapter)
                            .build();
            MyJsonObjectRequest myJsonObjectRequest2  = new MyJsonObjectRequest(Request.Method.POST,
                    SUBMIT_CUSTOM_CHAPTER_REQUEST, new JSONObject(gson.toJson(customChapterRequest)),
                    response -> {
                        Gson gson = new Gson();
                        TypeToken<ApiResponse> token = new TypeToken<ApiResponse>() {};
                        ApiResponse apiResponse = gson.fromJson(response.toString(), token.getType());

                        CommonMethods.showPositiveSnackbar(subject, apiResponse.getMessage(), 4000);

                        handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getOnBackPressedDispatcher().onBackPressed();
                            }
                        },1500);

                    }, new VolleyGlobalErrorListener(CustomFormActivity.this) {
                @Override
                public void postError(VolleyError error) {
                    buttonSubmit.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    CommonMethods.showNegativeSnackbar(ParentLayout,error.getMessage());
                }
            });
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);

        } catch (JSONException e) {
            Log.e("Error", e.getMessage());
        }
    }

    private void UseFreeDoubts() {
        Map<String, Object> map = new HashMap<>();
        String UserId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
        map.put(PreferenceKeys.USER_ID.getKey(),UserId);
        map.put("points",-1);
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_USER_FREE_DOUBT, map), null,
                response -> {
                    Gson gson = new Gson();
                    TypeToken<UserDto> token = new TypeToken<UserDto>() {};
                    UserDto list = gson.fromJson(response.toString(), token.getType());
                    int doubts = list.getFreeDoubt();
                    MyApplication.saveData(PreferenceKeys.FREE_DOUBT.getKey(),doubts);
                    progressBar.setVisibility(View.GONE);
                    if(getApplicationContext() != null)
                        CommonMethods.showPositiveSnackbar(ParentLayout,"Your Doubt Submitted");
//                    Handler handler = new Handler();
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            getOnBackPressedDispatcher().onBackPressed();
//                        }
//                    },1025);

                }, new VolleyGlobalErrorListener(CustomFormActivity.this) {
            @Override
            public void postError(VolleyError error) {
                buttonSubmit.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                CommonMethods.showNegativeSnackbar(ParentLayout,error.getMessage());
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
    }

    private void getChapterNameAPI() {
        Map<String, Object> map = new HashMap<>();
        MyJsonArrayRequest myJsonObjectRequest = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_CHAPTER_LIST, map), null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Gson gson = new Gson();
                        TypeToken<List<ChapterListInfoDto>> token = new TypeToken<List<ChapterListInfoDto>>() {};
                        List<ChapterListInfoDto> chapterListInfoDtos = gson.fromJson(response.toString(), token.getType());
                        chapterDataRecyclerView("Mathematics", chapterListInfoDtos,mathsList);
                        chapterDataRecyclerView("Biology", chapterListInfoDtos,bioList);
                        chapterDataRecyclerView("Physics", chapterListInfoDtos,phyList);
                        chapterDataRecyclerView("Chemistry", chapterListInfoDtos,chemList);
                    }
                }, new VolleyGlobalErrorListener(CustomFormActivity.this) {
            @Override
            public void postError(VolleyError error) {
                dataLoaded = false;
                CommonMethods.showNegativeSnackbar(ParentLayout,error.getMessage());
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);

    }

    private void getSubjectList() {
        Map<String, Object> map = new HashMap<>();
        String UserId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
        map.put(PreferenceKeys.USER_ID.getKey(),UserId);
        MyJsonArrayRequest myJsonObjectRequest2 = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_SUBSCRIBED_SUBJECT, map), null,
                response -> {
                    TypeToken<List<SubjectResponse>> token = new TypeToken<List<SubjectResponse>>() {};
                    List<SubjectResponse> list = gson.fromJson(response.toString(), token.getType());
                    subjectList.clear();
                    subjectList.add(getString(R.string.selectSubject));
                    for (SubjectResponse subjectModel : list) {
                        subjectList.add(subjectModel.getSubjectName());
                    }
                    subjectAdapter.notifyDataSetChanged();
                }, new VolleyGlobalErrorListener(CustomFormActivity.this) {
            @Override
            public void postError(VolleyError error) {
                CommonMethods.showNegativeSnackbar(subject,error.getMessage());
            }
        });

        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);

    }

    private void getExamList() {
        Map<String, Object> map = new HashMap<>();
        map.put(PreferenceKeys.CLASS_ID.getKey(),0);
        MyJsonArrayRequest myJsonObjectRequest2 = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(COURSES_LIST, map), null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        TypeToken<List<NameIdModel>> token = new TypeToken<List<NameIdModel>>() {};
                        List<NameIdModel> classlist = gson.fromJson(response.toString(), token.getType());
                        examList.add(getString(R.string.please_select_exam));
                        for(NameIdModel cm : classlist){
                            examList.add(cm.getName());
                        }
                        examAdapter.notifyDataSetChanged();
                        selectExam.setSelection(0);
                    }
                }, new VolleyGlobalErrorListener(CustomFormActivity.this) {
            @Override
            public void postError(VolleyError error) {

                Toast.makeText(CustomFormActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) ;

        MyVolleySingleton.getInstance(CustomFormActivity.this).addToRequestQueue(myJsonObjectRequest2);

    }

    private void getLevelData() {
        Map<String, Object> map = new HashMap<>();
        MyJsonArrayRequest myJsonObjectRequest2 = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(LEVEL_lIST, map), null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        TypeToken<List<NameIdModel>> token = new TypeToken<List<NameIdModel>>() {};
                        List<NameIdModel> nameIdModels = gson.fromJson(response.toString(), token.getType());
                        for(NameIdModel cm : nameIdModels){
                            levelList.add(cm.getName());
                        }
                        levelAdapter.notifyDataSetChanged();
                        level.setSelection(0);
                    }
                }, new VolleyGlobalErrorListener(CustomFormActivity.this) {
            @Override
            public void postError(VolleyError error) {

                Toast.makeText(CustomFormActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        MyVolleySingleton.getInstance(CustomFormActivity.this).addToRequestQueue(myJsonObjectRequest2);

    }

    private void chapterDataRecyclerView(String subject, List<ChapterListInfoDto> chapterListInfoDtos, ArrayList<String> list) {
        list.add(getString(R.string.selectChapter));
        for (ChapterListInfoDto chapterInfo:  chapterListInfoDtos) {
            if(chapterInfo.getSubject().equals(subject)) {
                list.add(chapterInfo.getVideoTitle());
            }
        }
        list.add(getString(R.string.dont_know));
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
            });
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


    private void uploadQuestion(int pos, String token, String fullname) {
        // upload your doubt here -----

        randomString = getAlphaNumericString(14);
        doubtIdValue = userEmail + randomString;
        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        Map<String,Object> dataToSave = new HashMap<>();
        dataToSave.put("Text", topic.getText().toString().trim());
        dataToSave.put("Approved", false);
        dataToSave.put("AskedAt", timeStamp);
        dataToSave.put("AskedBy", userEmail);
        dataToSave.put("Chapter", selectedChapter);
        dataToSave.put("Subject", selectedSubject);
        dataToSave.put("PreferredFormat", selectedExam);
        dataToSave.put("Description", description.getText().toString().trim());
        dataToSave.put("DoubtID",doubtIdValue);
        dataToSave.put("DoubtNumber",randomString);
        dataToSave.put("Solved", false);
        dataToSave.put("Taken", false);
        dataToSave.put("Token", token);
        dataToSave.put(PreferenceKeys.NAME.getKey(), fullname);
        dataToSave.put("ImageID", "");
        dataToSave.put("DoubtPrice", 10);
        dataToSave.put("TakenBy", "");
        if(selectedExam.equals("Image"))
            dataToSave.put("DoubtPoints",5);
        else
            dataToSave.put("DoubtPoints",8);

    }


    public void reducePoints(String userPoints, String doubtPoint) {

        if (!NumberUtils.isNumber(doubtPoint)) {
            doubtPoint = "0";
        }


        int remainingPoints = Integer.parseInt(userPoints) - Integer.parseInt(doubtPoint);

        if(remainingPoints < 0) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.low_points),
                    Toast.LENGTH_LONG).show();
            getOnBackPressedDispatcher().onBackPressed();
            return;
        }

        String remainingPointString = String.valueOf(remainingPoints);

        Map<String, Object> data = new HashMap<>();
        data.put("Points", remainingPoints);

        db.collection("Student").document(userEmail)
                .set(data, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.doubt_published),
                                Toast.LENGTH_LONG).show();
                        getOnBackPressedDispatcher().onBackPressed();

                    }
                });

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
                    = (int)(AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


}
