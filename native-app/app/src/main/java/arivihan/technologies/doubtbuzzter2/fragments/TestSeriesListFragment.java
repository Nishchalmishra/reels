package arivihan.technologies.doubtbuzzter2.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.helper.ActivityLauncher;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.ActivityTestIntro;
import arivihan.technologies.doubtbuzzter2.activity.CampaignSubscriptionActivity;
import arivihan.technologies.doubtbuzzter2.activity.MockTestAnalysisActivity;
import arivihan.technologies.doubtbuzzter2.activity.SubscriptionActivityNew;
import arivihan.technologies.doubtbuzzter2.adapter.TestSeriesListAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.customView.HowToVideoView;
import arivihan.technologies.doubtbuzzter2.enums.competitive.AdaptivePath;
import arivihan.technologies.doubtbuzzter2.enums.test.TestSeriesType;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.listener.SubscriptionCallback;
import arivihan.technologies.doubtbuzzter2.listener.TestSeriesClickListener;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionCheckDto;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionLockPopupDto;
import arivihan.technologies.doubtbuzzter2.model.TestEvaluationStatusEnum;
import arivihan.technologies.doubtbuzzter2.model.TestSeriesSetDto;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class TestSeriesListFragment extends Fragment implements
        TestSeriesListAdapter.MockTestClickListener {
    private static final String TAG = TestSeriesListFragment.class.getSimpleName();
    private String testId;
    private String videoUrl;
    private String videoUrlKey;
    RecyclerView recyclerView;
    ScrollView scrollView;
    private LottieAnimationView lottieAnimationView;
    private Dialog progressDialog;
    List<TestSeriesSetDto> testSeriesSetDtoList;
    TestSeriesListAdapter testSeriesListAdapter;
    LinearLayout noResultFoundLayout;
    Dialog dialog;
    TestSeriesClickListener clickListener;
    String subject;
    boolean scrollTo = true;
    String testTitle;
    private Map<String, Object> activityParamsMap;
    TextView lockDialogText, lockDialogBtnText, lockDialogTitle;
    LottieAnimationView lockAnimation;
    MaterialCardView card;
    String testSeriesType;

    public TestSeriesListFragment(String testId, String videoUrl, List<TestSeriesSetDto> testSeriesSetDtoList, String subject, String videoUrlKey, String testTitle, String testSeriesType) {
        this.testId = testId;
        this.videoUrl = videoUrl;
        this.videoUrlKey = videoUrlKey;
        this.testSeriesSetDtoList = testSeriesSetDtoList;
        this.subject = subject;
        this.testTitle = testTitle;
        this.testSeriesType = testSeriesType;
    }

    public TestSeriesListFragment() {
        //required empty constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            clickListener = (TestSeriesClickListener) context;
        } catch (ClassCastException e) {
            //throw new ClassCastException(context.getClass().toString() + " must implement RegWizardCallback ");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_test_series_list, container, false);
        recyclerView = view.findViewById(R.id.test_list_rv);
//        scrollView = view.findViewById(R.id.scroll_view);
        noResultFoundLayout = view.findViewById(R.id.no_result_found_layout);
        if (CollectionUtils.isEmpty(testSeriesSetDtoList)) {
            noResultFoundLayout.setVisibility(View.VISIBLE);
        } else {
//            scrollView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            noResultFoundLayout.setVisibility(View.GONE);
            setList();
        }
        setInProgressDialog();

        setDialog();
        MyApplication.getSubscriptionStatus(requireActivity(), new SubscriptionCallback() {
            @Override
            public void onSuccess(SubscriptionCheckDto dto) {
                updateDialogContent(dto);
            }

            @Override
            public void onError(String errorMessage) {
                updateDialogContent(null);
            }
        });
        return view;
    }

    @Override
    public void onLockedMockTestClicked() {

        try {
//            Intent intent = new Intent(requireContext(), CampaignSubscriptionActivity.class);
            Intent intent = SubscriptionNavigationHelper.getSubscriptionPageIntent(requireContext(), RedirectionSource.TEST_SERIES_JOIN);

            SubscriptionLockPopupDto subscriptionLockPopupDto =
                    CampaignSubscriptionActivity.getSubscriptionLockPopupDto("test_series_lock_"+MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));

            if (subscriptionLockPopupDto != null) {
                if(subscriptionLockPopupDto.getSubTitle()!=null && testTitle!=null) {
                    subscriptionLockPopupDto.setSubTitle(subscriptionLockPopupDto.getSubTitle().replace("{0}", testTitle));
                } else if(subscriptionLockPopupDto.getSubTitle()!=null) {
                    subscriptionLockPopupDto.setSubTitle(subscriptionLockPopupDto.getSubTitle().replace("{0}", ""));
                }
                intent.putExtra(CampaignSubscriptionActivity.BUNDLE_SUBSCRIPTION_DATA, subscriptionLockPopupDto);
            }
            startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

//        dialog.show();
    }

    @Override
    public void onAvailabilityStatusClicked(TestSeriesSetDto testSeriesSetDto) {
        MyApplication.saveData(String.format(getString(R.string.lastTestAttemptedInSubject),
                        testId,
                        testSeriesSetDto.getTestSeriesTypes().get(0).getSubject(),
                        MyApplication.getStringData(PreferenceKeys.CLASS_ID.getKey()),
                        MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey()),
                        MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey())),
                testSeriesSetDto.getTestSeriesId());
        if (testSeriesSetDto.isAttempted()) {
            if (testSeriesSetDto.isNewTest()) {
                checkAndDeleteEvaluationEntry(testSeriesSetDto.getTestSeriesId());
                Intent intent = new Intent(requireActivity(), MockTestAnalysisActivity.class);
                intent.putExtra("testSeriesId", testSeriesSetDto.getTestSeriesId());
                intent.putExtra(PreferenceKeys.BOARD.getKey(), testSeriesSetDto.isBoard());
                intent.putExtra("combined", true);
                intent.putExtra("Id", testSeriesSetDto.getId());
                intent.putExtra("testSeriesListId", testId);
                intent.putExtra("testSeriesName", testSeriesSetDto.getTestSeriesName());
                intent.putExtra("testSeriesType", testSeriesType);
                MyApplication.saveTestSeriesQuestions("questions", null);
                destroyHowTo();
                startActivity(intent);
            } else {
                Intent intent = new Intent(requireActivity(), ActivityTestIntro.class);
                intent.putExtra("testId", testSeriesSetDto.getTestSeriesId());
                intent.putExtra("Id", testId);
                intent.putExtra("combined", true);
                intent.putExtra(PreferenceKeys.TYPE.getKey(), PreferenceKeys.RESPONSE.getKey());
                intent.putExtra("startTest", true);
                intent.putExtra("testTitle", testTitle);
                destroyHowTo();
                if (StringUtils.isNotEmpty(testSeriesType) && testSeriesType.equalsIgnoreCase(TestSeriesType.adaptive.name())) {
                    Map<String, Object> params = CommonMethods.getIntentParams(intent);
                    String url = CommonMethods.getPersonalizedPathUrl() + AdaptivePath.TEST_SERIES_INSTRUCTIONS.path();
                    CommonNavigator.navigateToWebViewByUrlParams(requireContext(),
                            url,
                            params,
                            true, true,
                            false, false,
                            false, false,
                            getString(R.string.not_allowed_to_back), true, true, false, false, null,TAG,null);
                } else {
                    startActivity(intent);
                }
//                startActivity(intent);
            }
        }
    }

    private void checkAndDeleteEvaluationEntry(String testSeriesId) {
        MyApplication.getTestStatusDatabaseReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String testId = snapshot.child("testId").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);
                    if (testSeriesId.equals(testId) && TestEvaluationStatusEnum.REVIEW_PENDING.name().equals(status))
                        MyApplication.getTestStatusDatabaseReference().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        MyApplication.getTestListEvaluationDatabaseReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String testId = snapshot.child("testId").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);
                    if (testSeriesId.equals(testId) && TestEvaluationStatusEnum.REVIEW_PENDING.name().equals(status))
                        MyApplication.getTestListEvaluationDatabaseReference().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onItemClicked(TestSeriesSetDto testSeriesSetDto, int position) {
        Log.d("test_clicked", "onItemClicked fragment: " + testSeriesSetDto);
        MyApplication.saveData(String.format(getString(R.string.lastTestAttemptedInSubject),
                        testId,
                        testSeriesSetDto.getTestSeriesTypes().get(0).getSubject(),
                        MyApplication.getStringData(PreferenceKeys.CLASS_ID.getKey()),
                        MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey()),
                        MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey())),
                testSeriesSetDto.getTestSeriesId());
        Intent intent = new Intent(requireActivity(), ActivityTestIntro.class);
//        intent.putExtra("testSeriesDto", testSeriesSetDto);
        intent.putExtra("Id", testId);
        intent.putExtra("testId", testSeriesSetDto.getTestSeriesId());
        intent.putExtra("subject", subject);
        intent.putExtra("combined", true);
        intent.putExtra("position", position);
        intent.putExtra("startTest", true);
        if (testSeriesSetDto.isAttempted()) {
            intent.putExtra(PreferenceKeys.TYPE.getKey(), "REATTEMPT");
        } else {
            MyApplication.saveData(PreferenceKeys.TYPE.getKey(), "ATTEMPT");
            intent.putExtra(PreferenceKeys.TYPE.getKey(), "ATTEMPT");
        }
        intent.putExtra("testSeriesListId", testId);
        intent.putExtra("testTitle", testTitle);
        destroyHowTo();
        if (StringUtils.isNotEmpty(testSeriesType) && testSeriesType.equalsIgnoreCase(TestSeriesType.adaptive.name())) {
            Map<String, Object> params = CommonMethods.getIntentParams(intent);
            String url = CommonMethods.getPersonalizedPathUrl() + AdaptivePath.TEST_SERIES_INSTRUCTIONS.path();
            CommonNavigator.navigateToWebViewByUrlParams(requireContext(),
                    url,
                    params,
                    true, true,
                    false, false,
                    false, false,
                    getString(R.string.not_allowed_to_back), true, true, false, false, null,TAG,null);
        } else {
            startActivity(intent);
        }
    }


    private void setList() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false));
        testSeriesListAdapter = new TestSeriesListAdapter(requireActivity(), testSeriesSetDtoList, this, testId);
        recyclerView.setAdapter(testSeriesListAdapter);
    }

    private void setInProgressDialog() {
        progressDialog = new Dialog(requireActivity());
        progressDialog.setContentView(R.layout.arivihan_dialog);
        progressDialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(requireActivity(), R.drawable.bachground_custom_dialogu_typeactivity));
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lottieAnimationView = progressDialog.findViewById(R.id.lottie);
        lottieAnimationView.setAnimation(R.raw.edit_routine_lottie_star);
    }


    @Override
    public void onResume() {
        super.onResume();
        scrollTo(testSeriesSetDtoList);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void setDialog() {
        try {
            dialog = new Dialog(requireActivity());
            dialog.setContentView(R.layout.custom_dialog_lock_box);
            dialog.getWindow().setBackgroundDrawable(requireActivity()
                    .getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.setCancelable(false);

            lockDialogText = dialog.findViewById(R.id.header);
            lockDialogBtnText = dialog.findViewById(R.id.subs);
            lockDialogTitle = dialog.findViewById(R.id.dialog_title);
            lockAnimation = dialog.findViewById(R.id.dialog_animation);

            card = dialog.findViewById(R.id.subscriptionLayout);
            card.setOnClickListener(view -> {
                dialog.dismiss();
                Intent intent = getTargetIntent(null);

                if (activityParamsMap != null) {
                    for (Map.Entry<String, Object> entry : activityParamsMap.entrySet()) {
                        String k = entry.getKey();
                        Object v = entry.getValue();

                        if (v instanceof Serializable) {
                            intent.putExtra(k, (Serializable) v);
                        }
                    }
                }

//                if (requireActivity().getIntent().getBooleanExtra("fromHalfYearly", false)) {
//                    intent = new Intent(getActivity(), CampaignSubscriptionActivity.class);
//                } else {
//                    intent = new Intent(getActivity(), CampaignSubscriptionActivity.class);
////                    intent.putExtra("plan_level", "COURSE");
//                }
                intent = SubscriptionNavigationHelper.getSubscriptionPageIntent(getActivity(), RedirectionSource.TEST_SERIES_JOIN);

                startActivity(intent);
            });

            dialog.findViewById(R.id.cancel).setOnClickListener(v -> dialog.dismiss());
        } catch (Exception error) {
            Log.e("Error", "setDialog: ", error);
        }
    }

    private void updateDialogContent(SubscriptionCheckDto dto) {

        if (dto != null) {
            if (dto.getDialogText() != null) lockDialogText.setText(dto.getDialogText());
            if (dto.getButtonText() != null) lockDialogBtnText.setText(dto.getButtonText());
            activityParamsMap = dto.getActivityParams();
            if (StringUtils.isNotEmpty(dto.getDialogTitle())) {
                lockDialogTitle.setVisibility(View.VISIBLE);
                lockDialogTitle.setText(dto.getDialogTitle());
            }
        }
        if (activityParamsMap != null && activityParamsMap.containsKey("navigate_to")) {
            Object navigateToValue = activityParamsMap.get("navigate_to");
            if ("home_screen".equalsIgnoreCase(String.valueOf(navigateToValue))) {
                lockAnimation.setAnimation(R.raw.alert);
            }
        }
        card.setOnClickListener(view -> handleSubscriptionClick(dto));
    }

    private void handleSubscriptionClick(SubscriptionCheckDto dto) {
        closeDialog();
        Intent intent = null;

        if (dto != null && dto.getClassName() != null && !dto.getClassName().isEmpty()) {
            try {
                intent = new Intent(requireActivity(), ActivityLauncher.resolveClass(dto.getClassName()));
            } catch (ClassNotFoundException e) {
                Log.e("SubscriptionClick", "Class not found: " + dto.getClassName(), e);
            }
        }

        if (intent == null) {
//            intent = new Intent(requireActivity(), CampaignSubscriptionActivity.class);
            intent = SubscriptionNavigationHelper.getSubscriptionPageIntent(requireActivity(), RedirectionSource.TEST_SERIES_JOIN);
//                    .putExtra("plan_level", "COURSE")
//                    .putExtra("fromUnlockText", true);
        }

        if (activityParamsMap != null) {
            Intent finalIntent = intent;
            activityParamsMap.forEach((key, value) -> {
                if (value instanceof Serializable) {
                    finalIntent.putExtra(key, (Serializable) value);
                }
            });
        }
        if (activityParamsMap != null && activityParamsMap.containsKey("navigate_to")) {
            Object navigateToValue = activityParamsMap.get("navigate_to");
            if ("home_screen".equalsIgnoreCase(String.valueOf(navigateToValue))) {
                MyApplication.saveData(PreferenceKeys.NAVIGATE_TO_HOME_SCREEN.getKey(), true);
            }
        }
        startActivity(intent);
    }

    private void closeDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private Intent getTargetIntent(SubscriptionCheckDto dto) {
        if (dto != null && dto.getClassName() != null && !dto.getClassName().isEmpty()) {
            try {
                return new Intent(getActivity(), ActivityLauncher.resolveClass(dto.getClassName()));
            } catch (ClassNotFoundException e) {
                Log.e("LockDialog", "Class not found: " + dto.getClassName(), e);
            }
        }
        return SubscriptionNavigationHelper.getSubscriptionPageIntent(getActivity(), RedirectionSource.TEST_SERIES_JOIN);
//                .putExtra("plan_level", "COURSE");
    }

    public void showLockDialog() {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }


    private void destroyHowTo() {
        try {
            if (HowToVideoView.getInstance(requireActivity()) != null)
                HowToVideoView.getInstance(requireActivity()).remove();
        } catch (Exception ignored) {
        }
    }

    private void scrollTo(List<TestSeriesSetDto> list) {
        if (CollectionUtils.isEmpty(list)) {
            Log.e("ScrollTo", "List is null or empty");
            return;
        }

        String lastTestAttemptedInSubject = MyApplication.getStringData(String.format(getString(R.string.lastTestAttemptedInSubject),
                testId,
                list.get(0).getTestSeriesTypes().get(0).getSubject(),
                MyApplication.getStringData(PreferenceKeys.CLASS_ID.getKey()),
                MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey()),
                MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey())));

        boolean contains = false;

        if (StringUtils.isNotEmpty(lastTestAttemptedInSubject)) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getTestSeriesId().equals(lastTestAttemptedInSubject)) {
                    contains = true;
                    handleRVScroll(i);
                    break;
                }
            }
        }

        if (!contains && !CollectionUtils.isEmpty(list)) {
            lastTestAttemptedInSubject = MyApplication.getStringData(String.format(getString(R.string.lastTestAttemptedInSubject),
                    testId,
                    list.get(0).getTestSeriesTypes().get(0).getSubject(),
                    MyApplication.getStringData(PreferenceKeys.CLASS_ID.getKey()),
                    MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey()),
                    MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey())));
            if (StringUtils.isNotEmpty(lastTestAttemptedInSubject)) {
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getTestSeriesId().equals(lastTestAttemptedInSubject)) {
                        handleRVScroll(i);
                        break;
                    }
                }
            }
        }
    }

    private void handleRVScroll(int position) {
        new Handler().postDelayed(() -> {
            recyclerView.smoothScrollToPosition(position);
        }, 500);
    }

}