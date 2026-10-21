package arivihan.technologies.doubtbuzzter2.activity.todo;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_TODO_ORDER_STATE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.POST_TODO_EXAM_DATE_SUBMIT;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.HalfYearlyActivity;
import arivihan.technologies.doubtbuzzter2.activity.HomeScreen;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.constants.UrlRoutes;
import arivihan.technologies.doubtbuzzter2.fragments.todo.create_todo.ToDoStepExamSelectionFragment;
import arivihan.technologies.doubtbuzzter2.model.ToDoStateUser;
import arivihan.technologies.doubtbuzzter2.model.asat.CallBackResponse;
import arivihan.technologies.doubtbuzzter2.model.player.MiniVideoPlayerConfig;
import arivihan.technologies.doubtbuzzter2.model.todo.TodoExamDate;
import arivihan.technologies.doubtbuzzter2.model.todo.TodoExamDateResponse;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;

public class TodoExamDateSelectionActivity extends AppCompatActivity implements ToDoStepExamSelectionFragment.ToDoExamFragmentListener {

    private static final String TAG = TodoExamDateSelectionActivity.class.getSimpleName();
    private Bundle savedInstanceStateBundle;
    private ConstraintLayout parent_layout;
    private ImageView iv_back;
    private ShimmerFrameLayout shimmer_layout;
    private FrameLayout fragment_container_view;
    private LinearLayout bottom_layout;
    private CardView cv_submit;
    private Dialog actionDialog;
    private MediaPlayer mediaPlayer;
    private ProgressBar pb_progress_bar;
    private TextView tv_submit;
    private boolean restrictBackPress;
    private String examDate;
    String examId, examName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_exam_date_selection);
        savedInstanceStateBundle = savedInstanceState;

        Uri deepLinkData = getIntent().getData();
        if (deepLinkData != null) {
            examId = deepLinkData.getQueryParameter("examId");
            examName = deepLinkData.getQueryParameter("examName");
        } else {
            examId = getIntent().getStringExtra("examId");
            examName = getIntent().getStringExtra("examName");
        }

        initializeViews();
        setUpActionPopup();

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBackPressedActivity();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        dismissActionDialog();
        super.onDestroy();
    }

    private void initializeViews() {
        parent_layout = findViewById(R.id.parent_layout);
        iv_back = findViewById(R.id.iv_back);
        shimmer_layout = findViewById(R.id.shimmer_layout);
        fragment_container_view = findViewById(R.id.fragment_container_view);
        cv_submit = findViewById(R.id.cv_submit);
        bottom_layout = findViewById(R.id.bottom_layout);
        pb_progress_bar = findViewById(R.id.pb_progress_bar);
        tv_submit = findViewById(R.id.tv_submit);

        bottom_layout.setVisibility(View.GONE);
        fragment_container_view.setVisibility(View.GONE);
        pb_progress_bar.setVisibility(View.GONE);

        iv_back.setOnClickListener(v -> onBackPressedActivity());
        cv_submit.setOnClickListener(v -> {

            if (StringUtils.isBlank(examDate)) {
                CommonMethods.showNegativeSnackbar(parent_layout, getString(R.string.please_exam_date_select_karna_zaroori_he), 3000);
                return;
            }

            saveExamDate(examDate);
//            isExamDateSubmitted = true;
//            finishActivity(true);
        });

        getData();
    }

    private void finishActivity(boolean isUpdate) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("todo_update", isUpdate);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private void onBackPressedActivity() {

        if (restrictBackPress) {
            mediaPlayer = MediaPlayer.create(this, R.raw.wronganswer);
            mediaPlayer.start();
            actionDialog.show();
        } else {
            finishActivity(true);
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void navigateToHalfYearlyActivity() {
        Intent intent = new Intent(this, HalfYearlyActivity.class);
        intent.putExtra("examId", examId);
        intent.putExtra("examName", examName);
        startActivity(intent);
        finish();
    }

    private void getData() {
        shimmer_layout.setVisibility(View.VISIBLE);
        bottom_layout.setVisibility(View.GONE);
        fragment_container_view.setVisibility(View.GONE);
        shimmer_layout.startShimmer();
        Map<String, Object> map = new HashMap<>();
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(UrlRoutes.GET_TODO_EXAM_DATE_CHECK, map), null,
                response -> {
                    try {
                        Log.d(TAG, "Response : " + response.toString());
                        TypeToken<TodoExamDateResponse> token = new TypeToken<>() {
                        };
                        Gson gson = new Gson();
                        shimmer_layout.stopShimmer();
                        shimmer_layout.setVisibility(View.GONE);
                        restrictBackPress = false;
                        TodoExamDateResponse todoExamDateResponse = gson.fromJson(response.toString(), token.getType());
                        if (todoExamDateResponse != null && todoExamDateResponse.isSuccess()) {
                            if (todoExamDateResponse.getData() != null) {
                                TodoExamDate todoExamDate = todoExamDateResponse.getData();
                                examDate = todoExamDate.getSubmittedExamDate();
                                examId = StringUtils.isNotEmpty(todoExamDate.getExamId())?todoExamDate.getExamId():examId;
                                examName = StringUtils.isNotEmpty(todoExamDate.getExamName())?todoExamDate.getExamName():examName;
                                if (StringUtils.isEmpty(examDate) &&
                                        !MyApplication.getFirebaseRemoteConfig().getBoolean("half_yearly_screen_force_open_v2")) {
                                    restrictBackPress = true;
                                    fragment_container_view.setVisibility(View.VISIBLE);
                                    bottom_layout.setVisibility(View.VISIBLE);
//                                    Log.d(TAG, "getData:endExamDate "+todoExamDateResponse.getData().getSelectionExamEndDate()+
//                                            "startData:endExamDate "+todoExamDateResponse.getData().getSelectionExamStartDate());
                                    navigateToExamFragment(todoExamDateResponse.getData().getSelectionExamStartDate(),
                                            todoExamDateResponse.getData().getSelectionExamEndDate(), examDate, null);
                                } else {
                                    navigateToHalfYearlyActivity();
                                }
                            }
                        } else {
                            CommonMethods.showNegativeSnackbar(parent_layout, getString(R.string.something_went_wrong), 3000);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                Log.d(TAG, Objects.requireNonNull(error.getMessage()));
                shimmer_layout.stopShimmer();
                shimmer_layout.setVisibility(View.GONE);
                Toast.makeText(TodoExamDateSelectionActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
    }

    private void saveExamDate(String examDate) {
        try {
            Map<String, String> eventMap = new HashMap<>();
            eventMap.put("examDate", examDate);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.clicked_todo_exam_date_selection.getActionName(), eventMap);

            CommonMethods.enableDisableView(parent_layout, false);
            CommonMethods.enableDisableView(cv_submit,false);
            pb_progress_bar.setVisibility(View.VISIBLE);
            tv_submit.setVisibility(View.GONE);
            Map<String, Object> map = new HashMap<>();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("examDate", examDate);
            Log.d(TAG, "Request: " + jsonObject.toString());
            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                    CommonMethods.getUrlParam(POST_TODO_EXAM_DATE_SUBMIT, map), jsonObject,
                    response -> {
                        Log.d(TAG, "Response : " + response.toString());
                        TypeToken<CallBackResponse> token = new TypeToken<CallBackResponse>() {
                        };
                        Gson gson = new Gson();
                        CallBackResponse callBackResponse = gson.fromJson(response.toString(), token);
                        CommonMethods.enableDisableView(parent_layout, true);
                        CommonMethods.enableDisableView(cv_submit,true);
                        pb_progress_bar.setVisibility(View.GONE);
                        tv_submit.setVisibility(View.VISIBLE);
                        if (callBackResponse.isSuccess()) {
                            navigateToHalfYearlyActivity();
                        } else {
                            CommonMethods.showNegativeSnackbar(parent_layout, getString(R.string.something_went_wrong), 3000);
                        }
                    }, new VolleyGlobalErrorListener(this) {
                @Override
                public void postError(VolleyError error) {
                    Log.d(TAG, Objects.requireNonNull(error.getMessage()));
                    CommonMethods.enableDisableView(parent_layout, true);
                    CommonMethods.enableDisableView(cv_submit,true);
                    pb_progress_bar.setVisibility(View.GONE);
                    tv_submit.setVisibility(View.VISIBLE);
                    CommonMethods.showNegativeSnackbar(parent_layout, getString(R.string.something_went_wrong), 3000);
                }
            });
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void navigateToExamFragment(String startDate, String endDate, String examDate, MiniVideoPlayerConfig miniVideoPlayerConfig) {
        Fragment fragment =
                ToDoStepExamSelectionFragment.newInstance(startDate, endDate, 1, 1, examDate, true, miniVideoPlayerConfig);

        startTransition(fragment);

    }

    private void startTransition(Fragment fragment) {
        if (savedInstanceStateBundle == null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(
                            R.anim.fade_in_slide_in,
                            R.anim.fade_out_slide_out,
                            R.anim.slide_in_left,
                            R.anim.slide_out_right
                    )
                    .replace(R.id.fragment_container_view, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    private void setUpActionPopup() {
        try {
            dismissActionDialog();
            actionDialog = new Dialog(this);
            actionDialog.setContentView(R.layout.to_do_list_action_popup);
            Objects.requireNonNull(actionDialog.getWindow()).getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
            actionDialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(this, R.drawable.bachground_custom_dialogu_typeactivity));
            actionDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            actionDialog.setCancelable(false);
            ImageView close = actionDialog.findViewById(R.id.close_btn);
            CardView btn1 = actionDialog.findViewById(R.id.btn1);
            CardView btn2 = actionDialog.findViewById(R.id.btn2);
            LottieAnimationView animationView = actionDialog.findViewById(R.id.animation);
            animationView.setAnimation(R.raw.alert);
            close.setOnClickListener(v -> actionDialog.dismiss());
            btn1.setOnClickListener(v -> {
                navigateToHome();
            });
            btn2.setOnClickListener(v -> actionDialog.dismiss());
        } catch (Exception ignored) {
        }
    }

    private void dismissActionDialog() {
        if (actionDialog != null && actionDialog.isShowing()) {
            actionDialog.dismiss();
            actionDialog = null;
        }
    }

    @Override
    public void onExamDateChange(String date) {
        if (StringUtils.isNotEmpty(date)) {
            examDate = date;
        }
    }
}