package arivihan.technologies.doubtbuzzter2.fragments;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.CLASS_LIST;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.COURSES_LISTV2;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.COURSES_LISTV3;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_BOARD_LIST;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_USER_FREE_DOUBT;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_USER_POINTS;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.PUBLISH_SCREEN_VISIT_EVENT;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.REGISTER;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.SECONDARY_COURSES_LIST;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.SUBJECT_LIST;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.HomeScreen;
import arivihan.technologies.doubtbuzzter2.activity.VernacularActivity;
import arivihan.technologies.doubtbuzzter2.adapter.AddSubjectAboutFragmentAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.ClassInAboutFragmentAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.ExamInAboutFragmentAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.VernacularGridAdapter2;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.helper.slack.SlackOnboardingAlert;
import arivihan.technologies.doubtbuzzter2.listener.FragmentChangeListener;
import arivihan.technologies.doubtbuzzter2.model.AddSubjectAboutFragmentModel;
import arivihan.technologies.doubtbuzzter2.model.NameIdModel;
import arivihan.technologies.doubtbuzzter2.model.UserDto;
import arivihan.technologies.doubtbuzzter2.model.VernacularItem;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class AboutUserFragment extends Fragment implements ExamInAboutFragmentAdapter.OnExamPreparationClick,
        ClassInAboutFragmentAdapter.OnClassClick, AddSubjectAboutFragmentAdapter.OnSubjectClick, VernacularGridAdapter2.VernacularAction2 {

    CardView skip,next;
    Dialog dialog;
    CardView parent, student;
    TextView parent_text, student_text, text_card;
    Slider targetPercentageSlider;
    CountDownTimer countDownTimer;
    ImageView unselected11, unselected12, selected11, selected12;
    List<String> subjectIds = new ArrayList<>();
    List<String> subjectName = new ArrayList<>();
    int minSkipClick = 10;
    boolean board = false;
    String selectedBoardCode;
    String selectedBoard;
    String selectedBoardName;
    String ClassId = "";
    String ClassName = "";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String Name = "";
    String Course = "";
    String SecondaryCourse = "";
    String CourseId = "";
    String SecondaryCourseId = "";
    String maths = "0";
    String bio = "0";
    String neetId = "";
    String mathsId = "";
    String userEmail = "";
    Long number = 0L;
    private int dataLoaded = 0;
    String userid;
    Gson gson = new Gson();
    TextView name, skipping;
    HorizontalScrollView scrollView;
    LinearLayout containerLayout;
    ProgressBar spin1, spin2, spin3, spin5, progressBar, secondaryExamProgress;
    RecyclerView classrecyclerview, subjectrecyclerview, addsubjectrecyclerview, boardrecyclerview, secondaryExamRecyclerView;
    ArrayList<NameIdModel> class_model2 = new ArrayList<>();
    ArrayList<NameIdModel> exam_model = new ArrayList<>();
    ArrayList<NameIdModel> secondary_exam_model = new ArrayList<>();

    ArrayList<AddSubjectAboutFragmentModel> sub_model = new ArrayList<>();
    FrameLayout frame;
    CardView class_card, exam_card, secondary_exam_card, card_target_percentage, subject_card;
    RelativeLayout ParentLayout;
    SlackOnboardingAlert slackOnboardingAlert;
    Dialog boardDialog;
    RecyclerView vernacularRecyler;
    VernacularGridAdapter2 vernacularGridAdapter;
    private ArrayList<VernacularItem> languageList = new ArrayList<>();
    FragmentChangeListener listener;

    boolean boardSelected = false;
    float defaultSliderValue =85;
    boolean userInteractedWithSlider=false;
    Handler handler;
    Runnable runnable;
    CharSequence formattedText = null;

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
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about_user, container, false);

        frame = view.findViewById(R.id.frameLayout);

        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.scale);
        frame.startAnimation(animation);
        slackOnboardingAlert = new SlackOnboardingAlert(getContext());

        skip = view.findViewById(R.id.skip);
        next=view.findViewById(R.id.next);
        parent = view.findViewById(R.id.parent);
        name = view.findViewById(R.id.name);
        ParentLayout = view.findViewById(R.id.ParentRelativeLayout);
        text_card = view.findViewById(R.id.text_change);
        spin1 = view.findViewById(R.id.progress_class);
        scrollView = view.findViewById(R.id.horizontalScrollView);
        containerLayout = view.findViewById(R.id.container_layout);
        spin2 = view.findViewById(R.id.progress_preparation);
        skipping = view.findViewById(R.id.text_change);
        spin3 = view.findViewById(R.id.progress_subject);
        secondaryExamProgress = view.findViewById(R.id.progress_secondary_exam);
        targetPercentageSlider = view.findViewById(R.id.target_percentage_slider);
        card_target_percentage = view.findViewById(R.id.card_target_percentage);

        progressBar = view.findViewById(R.id.progress);
        class_card = view.findViewById(R.id.card2);
        spin5 = view.findViewById(R.id.progress_board);
        exam_card = view.findViewById(R.id.card3);
        subject_card = view.findViewById(R.id.card4);
        secondary_exam_card = view.findViewById(R.id.card7);
        student = view.findViewById(R.id.student);

        skip.setVisibility(View.GONE);

        setBoardDialog();

        Bundle bundle = this.getArguments();
        name.setText(bundle.getString("Name"));
        String num = bundle.getString(PreferenceKeys.PHONE_NUMBER.getKey());
//        userid = "2c9fab4b90c0c9e80190c60fa69d0012";
//        name.setText("TEst");
//        number = 1234567890L;
        userid = bundle.getString(PreferenceKeys.USER_ID.getKey());
        if (bundle.getString("email") != null)
            userEmail = bundle.getString("email");
        number = Long.parseLong(bundle.getString(PreferenceKeys.PHONE_NUMBER.getKey()));
        listener.setPhoneNumber(num);
        listener.registered(false);


        Name = name.getText().toString();

        // Recyclerview ---
        classrecyclerview = view.findViewById(R.id.class_recyclerview);
        classrecyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        ClassInAboutFragmentAdapter Adapter = new ClassInAboutFragmentAdapter(class_model2, this);
        classrecyclerview.setAdapter(Adapter);

        subjectrecyclerview = view.findViewById(R.id.preparation_recyclerview);
        subjectrecyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        ExamInAboutFragmentAdapter Adapter2 = new ExamInAboutFragmentAdapter(secondary_exam_model, this, false);
        classrecyclerview.setAdapter(Adapter2);

        secondaryExamRecyclerView = view.findViewById(R.id.secondary_exam_recycler_view);
        secondaryExamRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        ExamInAboutFragmentAdapter secondaryExamAdapter = new ExamInAboutFragmentAdapter(exam_model, this, true);
        secondaryExamRecyclerView.setAdapter(secondaryExamAdapter);

        addsubjectrecyclerview = view.findViewById(R.id.subject_recyclerview);
        addsubjectrecyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        AddSubjectAboutFragmentAdapter Adapter3 = new AddSubjectAboutFragmentAdapter(sub_model, this);
        addsubjectrecyclerview.setAdapter(Adapter3);


        boardrecyclerview = view.findViewById(R.id.board_recyclerview);
        boardrecyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        vernacularGridAdapter = new VernacularGridAdapter2(languageList, getContext(), this::changeImage, true);
        boardrecyclerview.setAdapter(vernacularGridAdapter);


        unselected11 = view.findViewById(R.id.status1_unselected);
        unselected12 = view.findViewById(R.id.status2_unselected);
        selected11 = view.findViewById(R.id.status1_selected);
        selected12 = view.findViewById(R.id.status2_selected);
        parent_text = view.findViewById(R.id.parent_option);
        student_text = view.findViewById(R.id.student_option);

        parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selected12.setVisibility(View.VISIBLE);
                selected11.setVisibility(View.GONE);
                unselected11.setVisibility(View.VISIBLE);
                unselected12.setVisibility(View.GONE);
                parent.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.navy_blue));
                parent_text.setTextColor(Color.WHITE);
                student.setCardBackgroundColor(Color.WHITE);
                student_text.setTextColor(ContextCompat.getColor(requireContext(), R.color.medium_grey_text));
                class_card.setVisibility(View.VISIBLE);
                setToLastScroll();
            }
        });
        student.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selected11.setVisibility(View.VISIBLE);
                selected12.setVisibility(View.GONE);
                unselected12.setVisibility(View.VISIBLE);
                unselected11.setVisibility(View.GONE);
                student.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.navy_blue));
                student_text.setTextColor(Color.WHITE);
                class_card.setVisibility(View.VISIBLE);
                parent.setCardBackgroundColor(Color.WHITE);
                parent_text.setTextColor(ContextCompat.getColor(requireContext(), R.color.medium_grey_text));
                setToLastScroll();
            }
        });

        targetPercentageSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                Log.d("Slider", "onStartTrackingTouch");
                userInteractedWithSlider=true;
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                Log.d("Slider", "onStopTrackingTouch");
                if (handler != null && runnable != null) {
                    handler.removeCallbacks(runnable);
                }
                Map<String, String> params = new HashMap<>();
                params.put("Steps", "user_onBoarding_slider_section");
                params.put("slider_Value", String.valueOf(slider.getValue()));
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.User_onBoarding_process.getActionName(), params);
                Log.d("Slider", "Value: " + slider.getValue());
                handler = new Handler();
                next.setVisibility(View.GONE);
                subject_card.setVisibility(View.VISIBLE);
                getSubject();
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        setToLastScroll();
                    }
                };
                handler.postDelayed(runnable, 300);
            }

        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Slider", "Next button clicked"+targetPercentageSlider.getValue());
                if (!userInteractedWithSlider) {
                    processSliderValue(targetPercentageSlider.getValue());
                } else {
                    processSliderValue(defaultSliderValue);
                }
            }

            private void processSliderValue(float value) {
                Log.d("Slider", "Value: " + value);
                if (handler != null && runnable != null) {
                    handler.removeCallbacks(runnable);
                }

                Map<String, String> params = new HashMap<>();
                params.put("Steps", "user_onBoarding_slider_section");
                params.put("slider_Value", String.valueOf(value));
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.User_onBoarding_process.getActionName(), params);

                handler = new Handler();
                next.setVisibility(View.GONE);
                subject_card.setVisibility(View.VISIBLE);
                getSubject();

                runnable = new Runnable() {
                    @Override
                    public void run() {
                        setToLastScroll();
                    }
                };
                handler.postDelayed(runnable, 300);
            }
        });



        targetPercentageSlider.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float v) {
                return (int) v + "%";
            }
        });


        skip.setOnClickListener(view1 -> {
            if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                CommonMethods.showNegativeSnackbar(ParentLayout, getString(R.string.NoInternetConnection));
            } else {
                progressBar.setVisibility(View.VISIBLE);
                skip.setVisibility(View.GONE);
                if (board && selectedBoardName == null) {
                    Toast.makeText(MyApplication.getInstance(), "Please " + R.string.select_board, Toast.LENGTH_SHORT).show();
                } else {
                    saveData();
                }
            }
        });



        if (CommonMethods.isOnline(getActivity())) {
            if (dataLoaded == 0) {
                setClassData();
//                setBoardData();
                dataLoaded = 1;
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

        return view;
    }

    // here points is 'extra points' to be added NOT TOTAL!!
    private void gainPoints(int points) {
        Map<String, Object> map = new HashMap<>();
        String UserId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
        map.put(PreferenceKeys.USER_ID.getKey(), UserId);
        map.put("points", points);
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_USER_POINTS, map), null,
                response -> {
                    Gson gson = new Gson();
                    TypeToken<UserDto> token = new TypeToken<>() {
                    };
                    UserDto list = gson.fromJson(response.toString(), token.getType());
                    int point = list.getPoints();
                    MyApplication.saveData(PreferenceKeys.YOUR_POINTS.getKey(), point);
                }, new VolleyGlobalErrorListener(getActivity()) {
            @Override
            public void postError(VolleyError error) {
                CommonMethods.showNegativeSnackbar(ParentLayout, "Server Error");
                slackOnboardingAlert.sendOnboardingFailureAlert(name.getText().toString(), number.toString(),
                        ClassId, Course, SecondaryCourse, "API FAILED : " + GET_USER_POINTS);
            }
        });
        MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(myJsonObjectRequest2);

    }

    private void gainFreeDoubts(int points) {
        Map<String, Object> map = new HashMap<>();
        String UserId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
        map.put(PreferenceKeys.USER_ID.getKey(), UserId);
        map.put("points", points);
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_USER_FREE_DOUBT, map), null,
                response -> {
                    Gson gson = new Gson();
                    TypeToken<UserDto> token = new TypeToken<UserDto>() {
                    };
                    UserDto list = gson.fromJson(response.toString(), token.getType());
                    int doubts = list.getFreeDoubt();
                    MyApplication.saveData(PreferenceKeys.FREE_DOUBT.getKey(), doubts);
                    // Here after adding points and FreeDoubt move to Home Screen Activity
                    Toast.makeText(getActivity(), "" + getString(R.string.success_register), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), VernacularActivity.class);
                    intent.putExtra("NewUser", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    MyApplication.getFCMToken();
                    startActivity(intent);
                    getActivity().finish();
                }, new VolleyGlobalErrorListener(getActivity()) {
            @Override
            public void postError(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                CommonMethods.showNegativeSnackbar(ParentLayout, "Server Error");
            }
        });
        MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(myJsonObjectRequest2);
    }

    private void fetchDataFromFirebase(String email) {
        db.collection("Student")
                .document(email).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            // Document found in the offline cache
                            DocumentSnapshot document = task.getResult();
                            if (document.getData().get("Points") != null) {
                                int p = Integer.parseInt(document.getData().get("Points").toString());
                                // Here p point is to be added in Backend
                                gainPoints(p);
                            }
                            if (document.getData().get("FreeDoubts") != null) {
                                int p = Integer.parseInt(document.getData().get("FreeDoubts").toString());
                                // Here p FreeDoubt is to be added in Backend
                                gainFreeDoubts(p);
                            }
                        } else {
                            Intent intent = new Intent(getActivity(), VernacularActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    }


                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        CommonMethods.showNegativeSnackbar(ParentLayout, "No user with this Email FOUND!!");
                    }
                });

    }

    private void setBoardData() {
        Map<String, Object> map = new HashMap<>();
        map.put(PreferenceKeys.USER_ID.getKey(), "ss");
        MyJsonArrayRequest myJsonObjectRequest2 = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_BOARD_LIST, map), null,
                response -> {
                    TypeToken<List<VernacularItem>> token = new TypeToken<List<VernacularItem>>() {
                    };
                    List<VernacularItem> vernacularItemList = gson.fromJson(response.toString(), token.getType());
                    for (VernacularItem cm : vernacularItemList) {
                        cm.board = true;
                        languageList.add(cm);
                    }
                    spin5.setVisibility(View.GONE);
                }, new VolleyGlobalErrorListener(getActivity()) {
            @Override
            public void postError(VolleyError error) {
                Toast.makeText(MyApplication.getInstance(), error.getMessage(), Toast.LENGTH_SHORT).show();
                slackOnboardingAlert.sendOnboardingFailureAlert(name.getText().toString(), number.toString(),
                        ClassName, Course, SecondaryCourse, "API FAILED : " + GET_BOARD_LIST);
            }
        });

        if (CollectionUtils.isEmpty(languageList)) {
            getSubject();
        } else {
            vernacularGridAdapter.notifyDataSetChanged();
            boardrecyclerview.setVisibility(View.VISIBLE);
        }

        MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(myJsonObjectRequest2);


    }


    private void setBoardDialog() {
        boardDialog = new Dialog(getActivity());
        boardDialog.setContentView(R.layout.custom_board_dialog);
        boardDialog.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        boardDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        boardDialog.setCancelable(false);

        vernacularRecyler = boardDialog.findViewById(R.id.recyclerView);
        vernacularRecyler.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        vernacularGridAdapter = new VernacularGridAdapter2(languageList, getContext(), this::changeImage, true);
        vernacularRecyler.setAdapter(vernacularGridAdapter);

        vernacularRecyler.setVisibility(View.VISIBLE);

    }


    private void saveData() {
        UserDto user = new UserDto();
        user.setBoardName(selectedBoardName);
        user.setBoardId(selectedBoard);
        user.setUserId(userid);
        user.setUsername(Name);
        user.setCourseId(CourseId);
        user.setClassId(ClassId);
        user.setSecondaryCourseId(SecondaryCourseId);
        user.setSubjectIds(subjectIds);
        user.setPhoneNumber(number);
        user.setBoardCode(selectedBoardCode);
        user.setBoardTargetPercentage((int) targetPercentageSlider.getValue());

        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(user::setFcmToken);

        Log.d("UserDTO", user.toString());

        MyApplication.saveData(PreferenceKeys.COURSE_ID.getKey(), SecondaryCourseId == null ? CourseId : SecondaryCourseId);
        MyApplication.saveData(PreferenceKeys.COURSE_NAME.getKey(), Course);
        MyApplication.saveData(PreferenceKeys.CLASS_ID.getKey(), ClassId);

//        CommonMethods.publishScreenVisitEvent(String.valueOf(number), "REGISTER_COMPLETE", PUBLISH_SCREEN_VISIT_EVENT);

        Log.d("REGISTRATION_DATA", "saveData: " + user.toString());

        if (!userEmail.isEmpty())
            user.setEmail(userEmail);
        try {
            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                    REGISTER, new JSONObject(gson.toJson(user)), response -> {
                try {
                    if (!isAdded() || getActivity() == null) {
                        Log.e("AboutUserFragment", "Fragment is not attached to an Activity. Aborting operation.");
                        return;
                    }
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                    }
                    if (Boolean.parseBoolean(response.get("success").toString())) {
                        listener.registered(true);
                        MyApplication.saveData(PreferenceKeys.ON_BOARDING_NUMBER.getKey(), String.valueOf(number));
                        MyApplication.saveData(PreferenceKeys.USER_ID.getKey(), response.get("message").toString());

                        if (user.getCourseId().equalsIgnoreCase("2")) {
                            MyApplication.saveData(PreferenceKeys.BOARD_SWITCH.getKey(), true);
                        }
                        String fcmToken = MyApplication.getFCMToken();
                        // If email is not null then
                        // first fetch points and freedoubt from firebasefirestore
                        // second update those points and freedoubt in backnend
                        if (user.getEmail() != null) {
                            fetchDataFromFirebase(user.getEmail());
                        } else {
                            Log.d("REGISTERuserId", response.get("message").toString());
                            Log.d("REGISTERphone", "" + number);
                            Log.d("REGISTERFcm", "" + fcmToken);
//                            Intent intent = new Intent(getActivity(), VernacularActivity.class);

                            Intent intent = new Intent(getActivity(), HomeScreen.class);
                            intent.putExtra("NewUser", true);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                            Map<String, String> params = new HashMap<>();
                            params.put("userName",Name);
                            params.put("Steps", "user_onBoarding_completed");
                            params.put("secondary_exam", SecondaryCourse);
                            params.put("class_id", CourseId);
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.User_onBoarding_process.getActionName(), params);
                            startActivity(intent);
                            requireActivity().finish();
                        }

                    }
                } catch (JSONException e) {

                    Map<String, String> params = new HashMap<>();
                    params.put("Steps", "user_onBoarding_failed");
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.User_onBoarding_process.getActionName(), params);
                    progressBar.setVisibility(View.GONE);
                    skip.setVisibility(View.VISIBLE);
                    e.printStackTrace();
                    slackOnboardingAlert.sendOnboardingFailureAlert(name.getText().toString(), number.toString(),
                            ClassName, Course, SecondaryCourse, "REGISTER FAILED : API FAILED : " + REGISTER);
                }
            }, new VolleyGlobalErrorListener(getActivity()) {
                @Override
                public void postError(VolleyError error) {
                    Map<String, String> params = new HashMap<>();
                    params.put("Steps", "user_onBoarding_failed");
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.User_onBoarding_process.getActionName(), params);
                    skip.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MyApplication.getInstance(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                    slackOnboardingAlert.sendOnboardingFailureAlert(name.getText().toString(), number.toString(),
                            ClassName, Course, SecondaryCourse, "API FAILED : " + REGISTER);
                }
            });
            MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(myJsonObjectRequest);
        } catch (JSONException e) {
            skip.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            Log.d("error", e.getMessage());
            slackOnboardingAlert.sendOnboardingFailureAlert(name.getText().toString(), number.toString(),
                    ClassName, Course, SecondaryCourse, "API FAILED : " + REGISTER);
        }
    }


    private void setToLastScroll() {
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    if(getCurrentVisibleView()!=null)
                        scrollView.smoothScrollTo(getCurrentVisibleView().getRight(),0);
                }
            });
    }

    private View getCurrentVisibleView() {
        int scrollX = scrollView.getScrollX();
        int width = scrollView.getWidth();
        int centerPosition = scrollX + (width / 2);

        for (int i = 0; i < containerLayout.getChildCount(); i++) {
            View child = containerLayout.getChildAt(i);
            int childStart = child.getLeft();
            int childEnd = child.getRight();

            if (centerPosition >= childStart && centerPosition <= childEnd) {
                return child;
            }
        }

        return null;
    }

    private void getCourseData() {
        exam_model.clear();
        Map<String, Object> map = new HashMap<>();
        map.put("boardAvailable", true);
        map.put(PreferenceKeys.CLASS_ID.getKey(), ClassId);
        map.put("cbseOption", true);
        MyJsonArrayRequest myJsonObjectRequest2 = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(COURSES_LISTV3, map), null,
                response -> {
                    TypeToken<List<NameIdModel>> token = new TypeToken<>() {
                    };
                    List<NameIdModel> classlist = gson.fromJson(response.toString(), token.getType());

                    for (NameIdModel cm : classlist) {
                        cm.setSelected(cm.getSelectedPosition() == 1);
//                        if (cm.getSelectedPosition() == 1 && cm.getId().equals("2")) {
//                            boardSelected = true;
//                        }
                        if(cm.getNextCardOnClick().equals("SECONDARY_COURSE_CARD")){
                            boardSelected = true;
                        }
                        exam_model.add(cm);
                    }

                    ExamInAboutFragmentAdapter adapterx = new ExamInAboutFragmentAdapter(exam_model, AboutUserFragment.this::onexampreparationclick, false);
                    subjectrecyclerview.setAdapter(adapterx);
                    spin2.setVisibility(View.GONE);

                    if (boardSelected) {
                        MyApplication.saveData(PreferenceKeys.BOARD_SWITCH.getKey(), true);
                        getSecondaryExam();
                        secondary_exam_card.setVisibility(View.VISIBLE);
                        subject_card.setVisibility(View.GONE);
                    } else {
                        setToLastScroll();
                        MyApplication.saveData(PreferenceKeys.BOARD_SWITCH.getKey(), false);
                    }

                }, new VolleyGlobalErrorListener(getActivity()) {
            @Override
            public void postError(VolleyError error) {
                Toast.makeText(MyApplication.getInstance(), error.getMessage(), Toast.LENGTH_SHORT).show();
                slackOnboardingAlert.sendOnboardingFailureAlert(name.getText().toString(), number.toString(),
                        ClassName, Course, SecondaryCourse, "API FAILED : " + COURSES_LISTV2);
            }
        });
        MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(myJsonObjectRequest2);

    }


    private void getSecondaryExam() {
        secondary_exam_model.clear();
        Map<String, Object> map = new HashMap<>();
        map.put(PreferenceKeys.CLASS_ID.getKey(), SecondaryCourseId);
        MyJsonArrayRequest myJsonObjectRequest2 = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(SECONDARY_COURSES_LIST, map), null,
                response -> {
                    TypeToken<List<NameIdModel>> token = new TypeToken<>() {
                    };
                    List<NameIdModel> classlist = gson.fromJson(response.toString(), token.getType());
                    for (NameIdModel cm : classlist) {
                        cm.setSelected(false);
                        secondary_exam_model.add(cm);
                    }
                    ExamInAboutFragmentAdapter adapterx = new ExamInAboutFragmentAdapter(secondary_exam_model, AboutUserFragment.this::onexampreparationclick, true);
                    secondaryExamRecyclerView.setAdapter(adapterx);
                    secondaryExamProgress.setVisibility(View.GONE);
                }, new VolleyGlobalErrorListener(getActivity()) {
            @Override
            public void postError(VolleyError error) {
                Toast.makeText(MyApplication.getInstance(), error.getMessage(), Toast.LENGTH_SHORT).show();
                slackOnboardingAlert.sendOnboardingFailureAlert(name.getText().toString(), number.toString(),
                        ClassName, Course, SecondaryCourse, "API FAILED : " + SECONDARY_COURSES_LIST);
            }
        });
        MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(myJsonObjectRequest2);

    }

    private void setClassData() {
        class_model2.clear();
        Map<String, Object> map = new HashMap<>();
        MyJsonArrayRequest myJsonObjectRequest2 = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(CLASS_LIST, map), null,
                response -> {
                    TypeToken<List<NameIdModel>> token = new TypeToken<List<NameIdModel>>() {
                    };
                    List<NameIdModel> classlist = gson.fromJson(response.toString(), token.getType());
                    for (NameIdModel cm : classlist) {
                        cm.setSelected(false);
                        class_model2.add(cm);
                    }
                    ClassInAboutFragmentAdapter adapterx = new ClassInAboutFragmentAdapter(class_model2, AboutUserFragment.this::onclassclick);
                    classrecyclerview.setAdapter(adapterx);
                    spin1.setVisibility(View.GONE);
                    setToLastScroll();
                }, new VolleyGlobalErrorListener(getActivity()) {
            @Override
            public void postError(VolleyError error) {

                Toast.makeText(MyApplication.getInstance(), error.getMessage(), Toast.LENGTH_SHORT).show();
                slackOnboardingAlert.sendOnboardingFailureAlert(name.getText().toString(), number.toString(),
                        ClassName, Course, SecondaryCourse, "API FAILED : " + CLASS_LIST);
            }
        });
        MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(myJsonObjectRequest2);


    }

    private void getSubject() {
        sub_model.clear();
        subjectIds.clear();
        subjectName.clear();
        Map<String, Object> map = new HashMap<>();
        map.put(PreferenceKeys.COURSE_ID.getKey(), SecondaryCourseId == null ? CourseId : SecondaryCourseId);
        MyJsonArrayRequest myJsonObjectRequest2 = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(SUBJECT_LIST, map), null,
                response -> {
                    TypeToken<List<AddSubjectAboutFragmentModel>> token = new TypeToken<List<AddSubjectAboutFragmentModel>>() {
                    };
                    List<AddSubjectAboutFragmentModel> classlist = gson.fromJson(response.toString(), token.getType());
                    for (AddSubjectAboutFragmentModel cm : classlist) {
                        cm.setSelected("0");
                        if (!cm.getAdditional()) {
                            subjectName.add(cm.getName());
                            subjectIds.add(cm.getId());
                        }
                        sub_model.add(cm);
                    }
                    AddSubjectAboutFragmentAdapter adapterx = new AddSubjectAboutFragmentAdapter(sub_model, AboutUserFragment.this::onsubjectclick);
                    addsubjectrecyclerview.setAdapter(adapterx);
                    spin3.setVisibility(View.GONE);

                }, new VolleyGlobalErrorListener(getActivity()) {
            @Override
            public void postError(VolleyError error) {
                Toast.makeText(MyApplication.getInstance(), error.getMessage(), Toast.LENGTH_SHORT).show();
                slackOnboardingAlert.sendOnboardingFailureAlert(name.getText().toString(), number.toString(),
                        ClassName, Course, SecondaryCourse, "API FAILED : " + SUBJECT_LIST);
            }
        });
        MyVolleySingleton.getInstance(getActivity()).addToRequestQueue(myJsonObjectRequest2);
        text_card.setText(R.string.save);
        skip.setVisibility(View.VISIBLE);

    }


    @Override
    public void onexampreparationclick(int position, String courseId, String course_name, boolean isSecondaryExam, String boardCode,String nextCardToOpen) {
        if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
            CommonMethods.showNegativeSnackbar(ParentLayout, getString(R.string.NoInternetConnection));
        } else {

            subjectName.clear();
            subjectIds.clear();
          /*  if(course_name.equals("Board")) {
                board = true;
            }
            else board = false;*/

            if (isSecondaryExam) {
                MyApplication.saveData(PreferenceKeys.BOARD_SWITCH.getKey(), true);
                MyApplication.saveData(PreferenceKeys.SECONDARY_COURSE_ID.getKey(), courseId);
                SecondaryCourse = course_name;
                SecondaryCourseId = courseId;
//                Course = "Board";
//                CourseId = "2";

                if (MyApplication.getFirebaseRemoteConfig().getBoolean("show_board_target_percentage_input_on_onboarding")) {
                    next.setVisibility(View.VISIBLE);
                    card_target_percentage.setVisibility(View.VISIBLE);
                } else {
                    subject_card.setVisibility(View.VISIBLE);
                    getSubject();
                }
                setToLastScroll();
            } else {
                selectedBoardCode = boardCode;
                secondary_exam_card.setVisibility(View.GONE);
                Course = course_name;
                CourseId = courseId;
                SecondaryCourseId = null;
                maths = "0";
                bio = "0";

                if (nextCardToOpen.equals("SECONDARY_COURSE_CARD")) {
                    boardSelected = true;
                    secondary_exam_card.setVisibility(View.VISIBLE);
                    subject_card.setVisibility(View.GONE);
                    getSecondaryExam();
                    setToLastScroll();
//                    MyApplication.saveData(PreferenceKeys.BOARD_SWITCH.getKey(), true);
                    //                setToLastScroll();
                } else {
                    boardSelected = false;
                    card_target_percentage.setVisibility(View.GONE);
//                    MyApplication.saveData(PreferenceKeys.BOARD_SWITCH.getKey(), false);
                    subject_card.setVisibility(View.VISIBLE);
                    //                card6.setVisibility(View.VISIBLE);
                    setToLastScroll();
                    if (!isSecondaryExam) {
                        getSubject();
                    }
                }
            }
            listener.setCourse(Course);
            listener.setExam(SecondaryCourse);


        }
    }

    @Override
    public void onclassclick(String position, String Class_) {
        if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
            CommonMethods.showNegativeSnackbar(ParentLayout, getString(R.string.NoInternetConnection));
        } else {
            if(MyApplication.getFirebaseRemoteConfig().getBoolean("to_show_class_info_pop_up") &&
                    position.equals(FirebaseRemoteConfig.
                            getInstance().getString("to_show_pop_for_class_id"))){
                 showDialog();
            }else {
                exam_card.setVisibility(View.VISIBLE);
                setToLastScroll();
                getCourseData();
            }
            Log.d("onclassclick", "onclassclick- classId : "+position);
            ClassId = position;
            ClassName = Class_;
            listener.setClass(ClassName);

        }
    }

    private void showDialog() {
        dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.custom_info_pop_up);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
        dialog.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        ImageView close = dialog.findViewById(R.id.close_btn);
        CardView btnNo = dialog.findViewById(R.id.btn_No);
        CardView btnYes = dialog.findViewById(R.id.btn_Yes);
        TextView infoDialogText=dialog.findViewById(R.id.info_dialog_text);
        String dynamicText=MyApplication.getFirebaseRemoteConfig().getString(getString(R.string.dialog_info_text) + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            formattedText=(Html.fromHtml(dynamicText, Html.FROM_HTML_MODE_LEGACY));
        } else {
            formattedText=(Html.fromHtml(dynamicText));
        }
        infoDialogText.setText(formattedText);
        LottieAnimationView animationView = dialog.findViewById(R.id.animation);
        animationView.playAnimation();
        animationView.loop(true);
        animationView.setSpeed(1.5f);
        close.setOnClickListener(v -> dialog.dismiss());
        btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            exam_card.setVisibility(View.VISIBLE);
            setToLastScroll();
            getCourseData();
        });
        btnNo.setOnClickListener(view -> {
            animationView.cancelAnimation();
            if(dialog!=null) {
                dialog.dismiss();
            }
        });
        close.setOnClickListener(view -> {
            animationView.cancelAnimation();
            if(dialog!=null) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onsubjectclick(String Class, String id) {
        if (Class.equals(getString(R.string.mathematics))) {
            if (maths.equals("0")) {
                mathsId = id;
                subjectIds.add(id);
                subjectName.add(Class);
                maths = "1";
            } else {
                for (int i = 0; i < subjectIds.size(); i++) {
                    if (subjectIds.get(i).equals(id)) {
                        subjectIds.remove(i);
                        subjectName.remove(i);
                        break;
                    }
                }
                maths = "0";
            }
        }
        if (Class.equals(getString(R.string.biology))) {
            if (bio.equals("0")) {
                neetId = id;
                subjectIds.add(id);
                subjectName.add(Class);
                bio = "1";
            } else {
                for (int i = 0; i < subjectIds.size(); i++) {
                    if (subjectIds.get(i).equals(id)) {
                        subjectIds.remove(i);
                        subjectName.remove(i);
                        break;
                    }
                }
                bio = "0";
            }
        }
        setToLastScroll();
    }

    @Override
    public void changeImage(VernacularItem item) {
        selectedBoard = item.id;
        selectedBoardName = item.name;
        skip.setVisibility(View.VISIBLE);
        subject_card.setVisibility(View.VISIBLE);
        setToLastScroll();
    }

}