package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_TODO_ORDER_STATE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_GET_USER_BY_PHONE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.UPDATE_SYLLABUS_ORDER;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.fragments.todo.create_todo.ToDoStepExamSelectionFragment;
import arivihan.technologies.doubtbuzzter2.adapter.ToDoStepSelectionAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.ToDoListCreationStatus;
import arivihan.technologies.doubtbuzzter2.fragments.CreatingRoutineFragment;
import arivihan.technologies.doubtbuzzter2.fragments.RocketFragment;
import arivihan.technologies.doubtbuzzter2.fragments.TimeTableReadyFragment;
import arivihan.technologies.doubtbuzzter2.fragments.todo.create_todo.ToDoStepTimeSelectionFragment;
import arivihan.technologies.doubtbuzzter2.fragments.todo.create_todo.congratulation_screen.ToDoFiveLeftFragment;
import arivihan.technologies.doubtbuzzter2.fragments.todo.create_todo.congratulation_screen.ToDoFourLeft;
import arivihan.technologies.doubtbuzzter2.fragments.todo.create_todo.congratulation_screen.ToDoOneLeft;
import arivihan.technologies.doubtbuzzter2.fragments.todo.create_todo.ToDoStepSubjectSelectionFragment;
import arivihan.technologies.doubtbuzzter2.fragments.todo.create_todo.ToDoStepChapterSelectionFragment;
import arivihan.technologies.doubtbuzzter2.fragments.todo.create_todo.congratulation_screen.ToDoThreeLeft;
import arivihan.technologies.doubtbuzzter2.fragments.todo.create_todo.congratulation_screen.ToDoTwoLeft;
import arivihan.technologies.doubtbuzzter2.model.GenerateSyllabusDto;
import arivihan.technologies.doubtbuzzter2.model.ToDoStateUser;
import arivihan.technologies.doubtbuzzter2.model.ToDoSubjectSelectionModel;
import arivihan.technologies.doubtbuzzter2.model.batch.user.UserDetailsResponse;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.model.UserSyllabusOrder;
import arivihan.technologies.doubtbuzzter2.viewModel.HomeScreenViewModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class ToDoListSteps extends BaseActivity implements ToDoStepSubjectSelectionFragment.OneFragmentListener,
        ToDoStepExamSelectionFragment.ToDoExamFragmentListener, ToDoStepChapterSelectionFragment.ThreeFragmentListener, CreatingRoutineFragment.GoToHomeListener,
        RocketFragment.GoToTimeTableReadyScreenListener, TimeTableReadyFragment.GoToRoutineListener,
ToDoFiveLeftFragment.GoToNextSubjectListenerFive, ToDoFourLeft.GoToNextSubjectListenerFour, ToDoThreeLeft.GoToNextSubjectListenerThree,
ToDoTwoLeft.GoToNextSubjectListenerTwo, ToDoOneLeft.GoToNextSubjectListener, ToDoStepTimeSelectionFragment.ToDoTimeFragmentListener{

    private ConstraintLayout parent_layout;
    private LinearLayout back_button_layout;
    private LinearLayout bottom_layout;
    private LinearLayout toolbar;
    private LinearProgressIndicator progress;
    private RecyclerView step_selection_rv;
    private CardView next_card;
    private CardView previous_card;
    private ToDoStepSelectionAdapter toDoStepSelectionAdapter;
    private int stepCount;
    private ArrayList<Boolean> list = new ArrayList<>();
    int stepNumber;
    int progressPercent;
    private Bundle savedInstanceStateBundle;
    private ToDoStateUser toDoStateUser;
    private final Gson gson = new Gson();
    private Fragment fragment;
    private ShimmerFrameLayout shimmerFrameLayout;
    private FrameLayout fragment_container_view;
    private int index=-1;
    private int selectedSubject;
    private int remainingSelectedSubject;
    private boolean isCongratulations;
    private boolean isCreating;
    private boolean isCreated;
    private ValueEventListener valueEventListener;
    private ProgressDialog routineLoading;
    private Dialog actionDialog;
    private boolean fromProfileFragment;
    private MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_list_steps);

        savedInstanceStateBundle = savedInstanceState;
        initializeViews();
        setUpActionPopup();
        initializeStepSelectionRecycler();
        observeForRoutine();

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBackPressedActivity();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void onBackPressedActivity() {
//        if (stepNumber > 0) {
//            if (!isCongratulations) {
//                stepNumber -= 1;
//            }
//            switchFragmentBack(stepNumber);
//        } else {
//            goToHomeScreen();
//        }
//        toDoStateUser.setStepNumber(stepNumber);

        //----------

//        if(isCreating || isCreated || stepNumber==0) {
        if(isCreating || isCreated) {
            finishActivity(true);
        } else if(stepNumber>=0){
            mediaPlayer = MediaPlayer.create(this, R.raw.wronganswer);
            mediaPlayer.start();
            actionDialog.show();
        }else{
            finishActivity(false);
        }
    }

    private void finishActivity(boolean isUpdate) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("todo_update", isUpdate);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    // The status bar is transparent app wide now, so there is no colour to set here: the
    // layout's own to_do_step_abstract_background is what paints behind it.

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
            if (stepNumber > 0)
                fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    private void startTransitionBack(Fragment fragment) {
        if (savedInstanceStateBundle == null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(
                            R.anim.slide_in_left,
                            R.anim.slide_out_right,
                            R.anim.slide_in_left,
                            R.anim.slide_out_right
                    )
                    .replace(R.id.fragment_container_view, fragment);
            if (stepNumber > 0)
                fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    private void initializeViews() {
        parent_layout = findViewById(R.id.parent_layout);
        toolbar = findViewById(R.id.toolbar);
        bottom_layout = findViewById(R.id.bottom_layout);
        fragment_container_view = findViewById(R.id.fragment_container_view);
        shimmerFrameLayout = findViewById(R.id.shimmer_layout);
        back_button_layout = findViewById(R.id.back_button_layout);
        progress = findViewById(R.id.progress);
        step_selection_rv = findViewById(R.id.step_selection_rv);
        next_card = findViewById(R.id.next_card);
        previous_card = findViewById(R.id.previous_card);
        back_button_layout.setOnClickListener(view -> {
            onBackPressedActivity();
        });

        previous_card.setOnClickListener(view -> {
            onBackPressedActivity();
        });

        next_card.setOnClickListener(view -> {
            if(validateCurrentFragment()) {
                if (stepCount > 1) {
                    startButtonPressAnimation(view);
                    if (stepNumber >= 1 && !isCongratulations) {
                        isCongratulations = true;
                        List<ToDoSubjectSelectionModel> list = toDoStateUser.getSubjectModelList();
                        if (index >= 0 && index < list.size()) {
                            getCongratulationsScreen(list.get(index).getSubjectName());
                        }
//                        getCongratulationsScreen(toDoStateUser.getSubjectModelList().get(index).getSubjectName());
                    } else if (stepNumber < stepCount - 1) {
                        isCongratulations = false;
                        stepNumber = stepNumber + 1;
                        switchFragment(stepNumber);
                    }
                    toDoStateUser.setStepNumber(stepNumber);
                }
            }
        });
    }

    private boolean validateCurrentFragment() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container_view);

        if(currentFragment==null)
            return true;

        boolean isValid = true;
        if (currentFragment instanceof ToDoStepSubjectSelectionFragment) {
            isValid = ((ToDoStepSubjectSelectionFragment) currentFragment).validateTodoSelection();
            if(!isValid){
                CommonMethods.showNegativeSnackbar(parent_layout, getString(R.string.please_ek_subject_select_karna_zaroori_he), 3000);
            }
        }
        else if (currentFragment instanceof ToDoStepChapterSelectionFragment) {
            isValid = ((ToDoStepChapterSelectionFragment) currentFragment).validateTodoSelection();
            if(!isValid){
                String message = getString(R.string.please_do_chapter_select_karna_zaroori_he);
                if(((ToDoStepChapterSelectionFragment) currentFragment).getEnabledChapterCount()<=1)
                    message = getString(R.string.please_chapter_select_karna_zaroori_he);
                CommonMethods.showNegativeSnackbar(parent_layout, message, 3000);
            }
        }
        else if(currentFragment instanceof ToDoStepExamSelectionFragment) {
            isValid = ((ToDoStepExamSelectionFragment) currentFragment).validateTodoSelection();
            if(!isValid){
                CommonMethods.showNegativeSnackbar(parent_layout, getString(R.string.please_exam_date_select_karna_zaroori_he), 3000);
            }
        }
        else if(currentFragment instanceof ToDoStepTimeSelectionFragment) {
            isValid = ((ToDoStepTimeSelectionFragment) currentFragment).validateTodoSelection();
            if(!isValid){
                CommonMethods.showNegativeSnackbar(parent_layout, "", 3000);
            }
        }

        return isValid;
    }
    private void switchFragmentBack(int stepNumber) {
        switch (stepNumber) {
            case 0-> {
                index=-1;
                fragment =  ToDoStepSubjectSelectionFragment.newInstance(toDoStateUser.getSubjectModelList(), toDoStateUser.getMiniVideoList()!=null?toDoStateUser.getMiniVideoList().get("SUBJECT"):null);
            }
            case 1,2,3,4,5,6,7 -> {
                int position = getSelectedSubjectIndexBack();
                if (position >= 0) {
                    remainingSelectedSubject++;
                    fragment = ToDoStepChapterSelectionFragment.newInstance(
                            toDoStateUser.getList().get(position),
                            toDoStateUser.getSubjectModelList().get(position).getSubjectName(),
                            stepNumber+1,
                            stepCount,
                            toDoStateUser.getMiniVideoList()!=null ? toDoStateUser.getMiniVideoList().get(toDoStateUser.getSubjectModelList().get(position).getSubjectCode()):null);
                }
            }
        }
        handleStepSelectionList(stepNumber);
        handleLinearProgress(stepNumber);
        startTransitionBack(fragment);
        toolbar.setVisibility(View.VISIBLE);
    }

    private int getSelectedSubjectIndexBack(){
        if(isCongratulations) {
            index = index + 1;
            isCongratulations=false;
        }
        for(int i=index-1;i>=0;i--){
            if(toDoStateUser.getSubjectModelList().get(i).isSelected()) {
                index=i;
                return index;
            }
        }
        return -1;
    }

    private void switchFragment(int stepNumber) {
        try {
            switch (stepNumber) {
                case 0 -> {
                    fragment =
                            ToDoStepSubjectSelectionFragment.newInstance(toDoStateUser.getSubjectModelList(), toDoStateUser.getMiniVideoList()!=null?toDoStateUser.getMiniVideoList().get("SUBJECT"):null);
                    handleStepSelectionList(stepNumber);
                    handleLinearProgress(stepNumber);
                }
                case 1 -> {
                    fragment =
                            ToDoStepExamSelectionFragment.newInstance("08/01/2025", "08/20/2025", 1, 2, "08/05/2025", false, toDoStateUser.getMiniVideoList()!=null?toDoStateUser.getMiniVideoList().get("EXAM"):null);
                    handleStepSelectionList(stepNumber);
                    handleLinearProgress(stepNumber);
                }
                case 2, 3, 4, 5, 6, 7 -> {
                    int position = getSelectedSubjectIndex();
                    if (position >= 0) {
                        fragment = ToDoStepChapterSelectionFragment.newInstance(
                                toDoStateUser.getList().get(position),
                                toDoStateUser.getSubjectModelList().get(position).getSubjectName(),
                                stepNumber + 1,
                                stepCount,
                                toDoStateUser.getMiniVideoList()!=null ? toDoStateUser.getMiniVideoList().get(toDoStateUser.getSubjectModelList().get(position).getSubjectCode()):null);
                    }
                    handleStepSelectionList(stepNumber);
                    handleLinearProgress(stepNumber);
                }
            }
            if (fragment != null)
                startTransition(fragment);
            toolbar.setVisibility(View.VISIBLE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void getCongratulationsScreen(String subject){
        switch (remainingSelectedSubject){
            case 0-> {
                isCreating=true;
                bottom_layout.setVisibility(View.GONE);
                MyApplication.updateToDoState(toDoStateUser);
                fragment =  new CreatingRoutineFragment();
                callGenerateApi(createMap());
                remainingSelectedSubject-=1;
                MyApplication.getReferenceForUserRoutine().setValue(ToDoListCreationStatus.CREATING.name());
            }
            case 1-> {
                fragment = ToDoOneLeft.newInstance(subject);
                remainingSelectedSubject-=1;
            }
            case 2-> {
                fragment = ToDoTwoLeft.newInstance(subject);
                remainingSelectedSubject-=1;
            }
            case 3-> {
                fragment = ToDoThreeLeft.newInstance(subject);
                remainingSelectedSubject-=1;
            }
            case 4-> {
                fragment = ToDoFourLeft.newInstance(subject);
                remainingSelectedSubject-=1;
            }
            case 5-> {
                fragment = ToDoFiveLeftFragment.newInstance(subject);
                remainingSelectedSubject-=1;
            }
        }
        if (fragment != null && remainingSelectedSubject>=0)
            startTransition(fragment);
        toolbar.setVisibility(View.GONE);
    }

    private Map<String, List<UserSyllabusOrder>> createMap(){
        Map<String, List<UserSyllabusOrder>> datas =  new HashMap<>();
        for(int i=0;i<toDoStateUser.getSubjectModelList().size();i++){
            if(toDoStateUser.getSubjectModelList().get(i).isSelected()){
                datas.put(toDoStateUser.getSubjectModelList().get(i).getSubjectName(),toDoStateUser.getList().get(i));
            }
        }
        return datas;
    }

    private int getSelectedSubjectIndex(){
        index++;
        for(int i=index;i<toDoStateUser.getList().size();i++){
            if(toDoStateUser.getSubjectModelList().get(i).isSelected()) {
                index=i;
                return index;
            }
        }
        return -1;
    }

    private void startButtonPressAnimation(View view) {
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, View.SCALE_X, 0.9f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 0.9f);
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, View.SCALE_X, 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f);

        scaleDownX.setDuration(100);
        scaleDownY.setDuration(100);
        scaleUpX.setDuration(100);
        scaleUpY.setDuration(100);

        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.playTogether(scaleDownX, scaleDownY);

        AnimatorSet scaleUp = new AnimatorSet();
        scaleUp.playTogether(scaleUpX, scaleUpY);

        AnimatorSet keypressEffect = new AnimatorSet();
        keypressEffect.playSequentially(scaleDown, scaleUp);
        keypressEffect.start();
    }

    private void initializeStepSelectionList() {
        for (int i = 0; i < stepCount; i++) {
            list.add(false);
        }
    }

    private void handleLinearProgress(int stepNumber) {
        progressPercent = ((stepNumber + 1) * 100) / stepCount;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progress.setProgress(progressPercent, true);
        } else {
            progress.setProgress(progressPercent);
        }
    }

    private void handleStepSelectionList(int stepNumber) {
        list.clear();
        for (int i = 0; i < stepCount; i++) {
            if (i == stepNumber) {
                list.add(i, true);
            } else
                list.add(i, false);
        }
        toDoStepSelectionAdapter.notifyDataSetChanged();
    }

    private void initializeStepSelectionRecycler() {
        toDoStepSelectionAdapter = new ToDoStepSelectionAdapter(this, list);
        step_selection_rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        step_selection_rv.setAdapter(toDoStepSelectionAdapter);
    }

    private void getData() {
        shimmerFrameLayout.startShimmer();
        Map<String, Object> map = new HashMap<>();
        map.put(PreferenceKeys.BOARD.getKey(), MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey()));
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_TODO_ORDER_STATE, map), null,
                response -> {
            Log.d("TAG", "Response TODO - Todo State : "+response.toString());
                    TypeToken<ToDoStateUser> token = new TypeToken<>() {};
                    toDoStateUser = gson.fromJson(response.toString(), token.getType());
                    getSelectedSubjectCount();
                    if(!isCreating) {
                        shimmerFrameLayout.stopShimmer();
                        shimmerFrameLayout.setVisibility(View.GONE);
                        toolbar.setVisibility(View.VISIBLE);
                        fragment_container_view.setVisibility(View.VISIBLE);
                        bottom_layout.setVisibility(View.VISIBLE);

                        initializeStepSelectionList();
                        handleLinearProgress(stepNumber);
                        switchFragment(stepNumber);
                    } else{
                        fragment= new CreatingRoutineFragment();
                        startTransition(fragment);
                    }
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                Log.d( "postError: ",error.getMessage());
            }
        });
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
    }

    private void getSelectedSubjectCount(){
        index=-1;
        selectedSubject=0;
        stepCount=1;
        stepNumber=toDoStateUser.getStepNumber();
        for(int i=0;i<toDoStateUser.getSubjectModelList().size();i++){
            if(toDoStateUser.getSubjectModelList().get(i).isSelected()) {
                selectedSubject++;
                stepCount++;
                if(stepNumber==stepCount-1){
                    index=i-1;
                }
            }

        }
        if(index>=0){
            getRemainingSelectedSubject();
        }else remainingSelectedSubject = selectedSubject-1;
    }

    private void getRemainingSelectedSubject(){
        for(int i=index+2;i<toDoStateUser.getSubjectModelList().size();i++){
            if(toDoStateUser.getSubjectModelList().get(i).isSelected()) {
                remainingSelectedSubject++;
            }
        }
    }

    private void callGenerateApi(Map<String, List<UserSyllabusOrder>> datas) {
        Map<String, Object> map = new HashMap<>();
        map.put(PreferenceKeys.BOARD.getKey(), MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey()));

        MyJsonObjectRequest myJsonObjectRequest2 = null;
        GenerateSyllabusDto generateSyllabusDto = GenerateSyllabusDto.builder()
                .datas(datas)
                .startDate(convertDateFormat(toDoStateUser.getStartDate()))
                .examDate(convertDateFormat(toDoStateUser.getEndDate()))
                .build();
        String data = gson.toJson(generateSyllabusDto);
        try {
            myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.POST,
                    CommonMethods.getUrlParam(UPDATE_SYLLABUS_ORDER, map), new JSONObject(gson.toJson(generateSyllabusDto)),
                    response -> {
                        Log.d("TAG", "Response TODO - Todo Update Order : "+response.toString());

                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.toDo_list_created.getActionName());

                    }, new VolleyGlobalErrorListener(this) {
                @Override
                public void postError(VolleyError error) {
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), getString(R.string.server_error), Toast.LENGTH_SHORT).show();
        }

    }

    public static String convertDateFormat(String inputDate) {
        try {
            // Define input and output date formats
            SimpleDateFormat inputFormat = new SimpleDateFormat("MM/dd/yyyy");
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");

            // Parse input date and format it into new pattern
            Date date = inputFormat.parse(inputDate);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null; // Handle the error appropriately
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(bottom_layout.getVisibility()==View.VISIBLE)
           MyApplication.updateToDoState(toDoStateUser);
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onDestroy() {
        if(valueEventListener!=null)
          MyApplication.getReferenceForUserRoutine().removeEventListener(valueEventListener);
        super.onDestroy();
    }

    private void goToHomeScreen() {
        MyApplication.getReferenceForUserRoutine().removeEventListener(valueEventListener);
        Intent intent = new Intent(this, HomeScreen.class);
        intent.putExtra("incrementVisitCount", true);
        startActivity(intent);
        finish();
    }

    @Override
    public void onSubjectListChange(ArrayList<ToDoSubjectSelectionModel> subjectModelList) {
        toDoStateUser.setSubjectModelList(subjectModelList);
        getSelectedSubjectCount();
        handleStepSelectionList(stepNumber);
        handleLinearProgress(stepNumber);
    }

//    @Override
//    public void onStartDateChange(String date) {
//        toDoStateUser.setStartDate(date);
//    }
//
//    @Override
//    public void onEndDateChange(String date) {
//        toDoStateUser.setEndDate(date);
//    }

    @Override
    public void onExamDateChange(String date) {
        toDoStateUser.setExamDate(date);
    }


    @Override
    public void onChapterListChange(ArrayList<UserSyllabusOrder> userSyllabusOrders) {
        if(index>=0 && index<toDoStateUser.getList().size()) {
            toDoStateUser.getList().set(index, userSyllabusOrders);
        }
    }

    private void observeForRoutine(){
         valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                        getData();
                }else if(snapshot.exists()){
                    String status = snapshot.getValue(String.class);
                    toolbar.setVisibility(View.GONE);
                    bottom_layout.setVisibility(View.GONE);
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    fragment_container_view.setVisibility(View.VISIBLE);
                    if(ToDoListCreationStatus.CREATED.name().equals(status)){
                        isCreated = true;
                        if(valueEventListener!=null)
                            MyApplication.getReferenceForUserRoutine().removeEventListener(valueEventListener);
                        if(isCreating)
                             fragment= new RocketFragment();
                        else
                            fragment= new TimeTableReadyFragment();
                        startTransition(fragment);
                    }else if(ToDoListCreationStatus.CREATING.name().equals(status)){
                        isCreating = true;
                        fragment= new CreatingRoutineFragment();
                        startTransition(fragment);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
         MyApplication.getReferenceForUserRoutine().addValueEventListener(valueEventListener);
    }

    @Override
    public void goToHome() {
        goToHomeScreen();
    }

    @Override
    public void goToTimeTableReadyScreen() {
        fragment = new TimeTableReadyFragment();
        startTransition(fragment);
    }

    @Override
    public void goToRoutine() {
        routineLoading = new ProgressDialog(this);
        routineLoading.setMessage(getString(R.string.please_wait));
        routineLoading.setCanceledOnTouchOutside(false);
        routineLoading.show();
        getAPIDataByPhoneData();
    }

    private void getAPIDataByPhoneData() {
        Map<String, Object> map = new HashMap<>();
        String PhoneNumber = MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey());
        map.put("phoneNumber", PhoneNumber);

        map.put("key", BuildConfig.API_KEY);
        map.put(PreferenceKeys.BOARD.getKey(), MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey()));
        MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(AV_GET_USER_BY_PHONE, map), null,
                response -> {
                    Log.d("TAG", "Response TODO - User DTO : "+response.toString());
                    Gson gson = new Gson();
                    Log.i("Api called", "Profile Fragment");
                    TypeToken<ApiWrappedResponse<UserDetailsResponse>> token = new TypeToken<ApiWrappedResponse<UserDetailsResponse>>() {
                    };
                    ApiWrappedResponse<UserDetailsResponse> apiResponse = gson.fromJson(response.toString(), token.getType());
                    UserDetailsResponse user = (apiResponse != null && apiResponse.isSuccess()) ? apiResponse.getData() : null;
                    if (user != null) {
                        MyApplication.saveUserProfileLocally(user);
                    }
                    boolean syllabusGenerated = user != null && user.getUserDto() != null && Boolean.TRUE.equals(user.getUserDto().getSyllabusGenerated());
                    routineLoading.dismiss();
                    if (syllabusGenerated) {
                        Intent intent = new Intent(this, ToDoListMain.class);
                        intent.putExtra("redirect_to_home", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                        MyApplication application = (MyApplication) this.getApplication();
                        HomeScreenViewModel homeScreenViewModel = application.getHomeScreenViewModel();
                        homeScreenViewModel.getStudentProgressData(true);

                    } else {
                        Intent intent = new Intent(this, ToDoListSteps.class);
                        intent.putExtra("redirect_to_home", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                routineLoading.dismiss();
//                CommonMethods.showNegativeSnackbar(parentLayout, error.getMessage());
//                if (checkInRoutine) {
//                    routineLoading.dismiss();
//                }
            }
        });
        myJsonObjectRequest2.setTag("ToDoListSteps");
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);

    }

    private void setUpActionPopup() {
        actionDialog = new Dialog(this);
        actionDialog.setContentView(R.layout.to_do_list_action_popup);
        actionDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
        actionDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        actionDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        actionDialog.setCancelable(false);
        ImageView close = actionDialog.findViewById(R.id.close_btn);
        CardView btn1 = actionDialog.findViewById(R.id.btn1);
        CardView btn2 = actionDialog.findViewById(R.id.btn2);
        LottieAnimationView animationView = actionDialog.findViewById(R.id.animation);
        animationView.setAnimation(R.raw.alert);
        close.setOnClickListener(v -> actionDialog.dismiss());
        btn1.setOnClickListener(v -> {
            goToHomeScreen();
        });
        btn2.setOnClickListener(v -> actionDialog.dismiss());
    }

    @Override
    public void goToNextSubjectFive() {
        next_card.performClick();
    }

    @Override
    public void goToNextSubjectFour() {
        next_card.performClick();
    }

    @Override
    public void goToNextSubject() {
        next_card.performClick();
    }

    @Override
    public void goToNextSubjectThree() {
        next_card.performClick();
    }

    @Override
    public void goToNextSubjectTwo() {
        next_card.performClick();
    }

    @Override
    public void onStudyModeChange(String selected) {

    }

    @Override
    public void onMorningTimeChange(String startTime, String endTime) {

    }

    @Override
    public void onAfternoonTimeChange(String startTime, String endTime) {

    }

    @Override
    public void onEveningTimeChange(String startTime, String endTime) {

    }

    @Override
    public void onNightTimeChange(String startTime, String endTime) {

    }
}
