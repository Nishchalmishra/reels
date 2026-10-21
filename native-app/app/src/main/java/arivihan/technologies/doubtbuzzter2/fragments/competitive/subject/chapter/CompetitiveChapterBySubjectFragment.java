package arivihan.technologies.doubtbuzzter2.fragments.competitive.subject.chapter;

import static android.app.Activity.RESULT_OK;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_ONE_SHOT_LECTURES;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.POST_CHAPTER_PROGRESS;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.CampaignSubscriptionActivity;
import arivihan.technologies.doubtbuzzter2.activity.MicrolectureListActivity;
import arivihan.technologies.doubtbuzzter2.adapter.competitive.subject.CompetitiveChapterNameAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.common.bypass.ChapterLiveByPass;
import arivihan.technologies.doubtbuzzter2.enums.ChapterEnum.ChapterStatus;
import arivihan.technologies.doubtbuzzter2.enums.ChapterEnum.ChapterType;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.fragments.competitive.subject.CompetitiveSubjectChaptersFragment;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.listener.competitive.RefreshActivityListener;
import arivihan.technologies.doubtbuzzter2.listener.dialog.DialogPositiveNegativeOptionClickListener;
import arivihan.technologies.doubtbuzzter2.model.ChapterNameModel;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionCheckDto;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionLockPopupDto;
import arivihan.technologies.doubtbuzzter2.model.chapter.live.ChapterPopUpDto;
import arivihan.technologies.doubtbuzzter2.model.competitive.lecture.OneShotChapterListResponse;
import arivihan.technologies.doubtbuzzter2.model.dialog.PopUpOptionsDto;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import lombok.Setter;

public class CompetitiveChapterBySubjectFragment extends
        Fragment implements CompetitiveChapterNameAdapter.OnChapClickListener {

    private static final String TAG = CompetitiveSubjectChaptersFragment.class.getSimpleName();

    // UI Variable
    private FrameLayout fl_parent_layout;
    private RecyclerView rv_topics;
    private CompetitiveChapterNameAdapter adapter;
    private LinearLayout ll_no_data;
    private ShimmerFrameLayout shimmer;
//    private TextView tv_no_result_title, tv_no_result_subtitle;

    // Data Variable
    private String title, subjectId, subjectName, subjectCode, chapterType;
    private List<ChapterNameModel> chapterNameModels = new ArrayList<>();
    private List<String> chapterIdList = new ArrayList<>();
    private String queryText;
    @Setter
    private SubscriptionCheckDto subscriptionCheckDto;
    private Dialog liveDialog;

    private final Map<String, ValueEventListener> chapterListeners = new HashMap<>();
    private final Map<String, DatabaseReference> chapterRefs = new HashMap<>();

    private long lastApiCallTime = 0;
    private static final long API_COOLDOWN = 5000;
    private SubscriptionLockPopupDto subscriptionLockPopupDto;
    private PopUpOptionsDto warningPopUpDialogDto;
    private RefreshActivityListener refreshActivityListener;
    private String featureType;

    private final ActivityResultLauncher<Intent> subscriptionScreenLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    try {
                        //isSubscribed
                        Intent data = result.getData();
                        boolean isSubscribed = data.getBooleanExtra("isSubscribed", false);
                        if (isSubscribed) {
//                            getChaptersBySubjectId(subjectId);

                            if (refreshActivityListener != null) {
                                Log.d(TAG, "Refresh Page : true");
                                refreshActivityListener.doRefreshNow(true);
                            } else {
                                getChaptersBySubjectId(subjectId);
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
    );

    public CompetitiveChapterBySubjectFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            if (getActivity() instanceof RefreshActivityListener) {
                refreshActivityListener = (RefreshActivityListener) getActivity();
            }
        } catch (Exception exception) {
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("title", title);
        outState.putString("id", subjectId);
        outState.putString(PreferenceKeys.NAME.getKey(), subjectName);
        outState.putString("code", subjectCode);
        outState.putString("chapterType", chapterType);
    }

    public static CompetitiveChapterBySubjectFragment newInstance(String title, String id, String name, String code, String boardCode) {
        return newInstance(title, id, name, code, boardCode, null,null);
    }

    public static CompetitiveChapterBySubjectFragment newInstance(String title, String id, String name, String code, String boardCode, String chapterType, String featureType) {
        CompetitiveChapterBySubjectFragment fragment = new CompetitiveChapterBySubjectFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("id", id);
        args.putString(PreferenceKeys.NAME.getKey(), name);
        args.putString("code", code);
        args.putString("chapterType", chapterType);
        args.putString("feature_type", featureType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            try {
                subjectId = getArguments().getString("id");
                subjectName = getArguments().getString(PreferenceKeys.NAME.getKey());
                subjectCode = getArguments().getString("code");
                title = getArguments().getString("title");
                chapterType = getArguments().getString("chapterType");
                featureType = getArguments().getString("feature_type");
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chapters_by_subject, container, false);
        Log.d(TAG, "Id: " + (StringUtils.isNotEmpty(subjectId) ? subjectId : ""));
        initViews(view);
        Map<String, String> params = new HashMap<>();
        params.put("feature_type", featureType);
        params.put("subject_id", subjectId);
        params.put("subject_name", subjectName);
        params.put("screen_name", TAG);
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.neet_lecture_screen_view.getActionName(), params);
        return view;
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
    public void onDestroy() {
        MyVolleySingleton.getInstance(requireContext()).getRequestQueue().cancelAll(TAG);
        dismissLivePopUp();
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
//        detachAllChapterListeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        detachAllChapterListeners();
    }

    private void initViews(View view) {
        fl_parent_layout = view.findViewById(R.id.fl_parent_layout);
        rv_topics = view.findViewById(R.id.recycler);
        ll_no_data = view.findViewById(R.id.no_result_found_layout);
        shimmer = view.findViewById(R.id.shimmer_chapters);

        rv_topics.setVisibility(View.GONE);
        ll_no_data.setVisibility(View.GONE);
        shimmer.setVisibility(View.GONE);

        getChaptersBySubjectId(subjectId);
    }

    public void setFilter(String filter) {
        try {
            queryText = filter;
            adapter.getFilter().filter(filter);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean canCallApi() {
        long now = System.currentTimeMillis();

        // If last call was recent
        if (now - lastApiCallTime < API_COOLDOWN) {
            return false;
        }

        // Update lock time
        lastApiCallTime = now;
        return true;
    }

    private void getChaptersBySubjectId(String subjectId) {
        try {
            if (!canCallApi())
                return;
            shimmer.startShimmer();
            shimmer.setVisibility(View.VISIBLE);
            detachAllChapterListeners();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("subjectId", subjectId);
            if (chapterType != null && !chapterType.isEmpty()) {
                jsonObject.put("chapterCategoryType", chapterType);
            }
            subscriptionLockPopupDto = null;
            warningPopUpDialogDto = null;
            Log.d(TAG, "Request: " + jsonObject.toString());

            HashMap<String, Object> params = new HashMap<>();
            if(MyApplication.isCompetitiveProfile()) {
                params.put("isNeet", true);
            }

            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                    CommonMethods.getUrlParam(GET_ONE_SHOT_LECTURES, params), jsonObject,
                    response -> {
                        try {
                            Log.d(TAG, "Competitive getChaptersBySubjectId: " + response);
                            Gson gson = new Gson();
                            TypeToken<ApiWrappedResponse<OneShotChapterListResponse>> token = new TypeToken<ApiWrappedResponse<OneShotChapterListResponse>>() {
                            };
                            ApiWrappedResponse<OneShotChapterListResponse> oneShotChapterListResponseApiWrappedResponse = gson.fromJson(response.toString(), token.getType());
                            OneShotChapterListResponse oneShotChapterListResponse = oneShotChapterListResponseApiWrappedResponse.getData();
                            Log.d("chapterList", "data " + oneShotChapterListResponse.toString());
                            subscriptionLockPopupDto = oneShotChapterListResponse.getLockConfig();
                            warningPopUpDialogDto = oneShotChapterListResponse.getWarningPopUp();
                            updateDataInRecyclerView(oneShotChapterListResponse);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }, new VolleyGlobalErrorListener(requireContext()) {
                @Override
                public void postError(VolleyError error) {
//                shimmer.stopShimmer();
//                shimmer.setVisibility(View.GONE);
                    CommonMethods.showNegativeSnackbar(fl_parent_layout, error.getMessage());
                }
            });

            myJsonObjectRequest.setTag(TAG);
            MyVolleySingleton.getInstance(requireContext()).addToRequestQueue(myJsonObjectRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void setVideoProgress(String chapterCode) {
        try {
            Gson gson = new Gson();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("chapterCode", chapterCode);
            jsonObject.put("nodeId", "");
            jsonObject.put("chapterId", "");
            Log.d(TAG, "Progress API Request: " + jsonObject.toString());

            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                    CommonMethods.getUrlParam(POST_CHAPTER_PROGRESS, new HashMap<>()), jsonObject,
                    response -> {
                        Log.d(TAG, "Progress API Response: " + response.toString());
                    }, error -> {
                Log.d(TAG, "Progress API Error Response: " + error.toString());
            });
            myJsonObjectRequest.setTag(TAG);
            MyVolleySingleton.getInstance(requireContext()).addToRequestQueue(myJsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void updateDataInRecyclerView(OneShotChapterListResponse oneShotChapterListResponse) {
        rv_topics.setVisibility(View.VISIBLE);
        shimmer.stopShimmer();
        shimmer.setVisibility(View.GONE);
        List<ChapterNameModel> chapterNameModelsAll = new ArrayList<>();
        List<String> chapterIds = new ArrayList<>();
        for (OneShotChapterListResponse.ChapterItem chapterItem : oneShotChapterListResponse.getChapters()) {
            ChapterNameModel cm = ChapterNameModel
                    .builder()
                    .id(chapterItem.getChapterId())
                    .newLecture(chapterItem.isNewLecture())
                    .imageUrl(chapterItem.getImageUrl())
                    .chapter(chapterItem.getVideoTitle())
                    .name(chapterItem.getTeacherName() == null ? "Arivihan" : chapterItem.getTeacherName())
                    .locked(chapterItem.getIsLocked())
                    .rescheduledMessage(chapterItem.getRescheduledMessage())
                    .lockMessage(chapterItem.getLockMessage())
                    .teacherIntroImage(chapterItem.getTeacherIntroImage())
                    .completed(chapterItem.isCompleted())
                    .notesUrl(chapterItem.getNotesUrl())
                    .board(chapterItem.isBoard())
                    .chapterType(chapterItem.getChapterType())
                    .chapterStatus(chapterItem.getChapterStatus())
                    .chapterCode(chapterItem.getChapterCode())
                    .type(chapterItem.getType())
                    .chapterType(chapterItem.getChapterType())
                    .chapterLiveDto(chapterItem.getChapterLiveDto())
                    .build();
            chapterNameModelsAll.add(cm);
            chapterIds.add(chapterItem.getChapterId());
        }
        chapterNameModels.clear();
        chapterNameModels.addAll(chapterNameModelsAll);

        chapterIdList.clear();
        chapterIdList.addAll(chapterIds);

        if (chapterNameModelsAll.isEmpty()) {
            rv_topics.setVisibility(View.GONE);
            ll_no_data.setVisibility(View.VISIBLE);
            return;
        } else {
            adapter = new CompetitiveChapterNameAdapter(requireContext(), chapterNameModels, this, subjectName, ll_no_data, rv_topics);
            rv_topics.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
            rv_topics.setAdapter(adapter);
            attachObserversForChapters(subjectId, chapterIdList);
        }
    }

//    @Override
//    public void onChapterClick(int position, CompetitiveChapterDto competitiveChapterDto) {
//        CommonNavigator.navigateToCompetitiveTopicListActivity(requireContext(), competitiveChapterDto.getId());
//    }

    private void showPopupDialog(String url) {
        try {
            Dialog dialogHome = new Dialog(requireContext());
            dialogHome.setContentView(R.layout.custom_home_dialogue);
            dialogHome.getWindow().setBackgroundDrawable(requireContext().getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
            dialogHome.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialogHome.setCancelable(false);

            CardView cancel = dialogHome.findViewById(R.id.cancel);
            ImageView image = dialogHome.findViewById(R.id.image);

            if (StringUtils.isNotEmpty(url)) {
                try {
                    Glide.with(this).load(url).into(image);
                } catch (Exception e) {
                    Glide.with(this).asGif().load(R.raw.image_gif).into(image);
                }
            } else {
                Glide.with(this).asGif().load(R.raw.image_gif).into(image);
            }

            MyApplication.saveData(PreferenceKeys.POPUP_STORE_TIME.getKey(), System.currentTimeMillis() + 43200000);
            cancel.setOnClickListener(v -> dialogHome.dismiss());
            dialogHome.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showLectureStartWarning(PopUpOptionsDto popUpOptionsDto, Map<String, String> params, DialogPositiveNegativeOptionClickListener dialogPositiveNegativeOptionClickListener) {
        try {
            if (popUpOptionsDto == null) {
                if (dialogPositiveNegativeOptionClickListener != null) {
                    dialogPositiveNegativeOptionClickListener.onPositiveButtonClick(true, null, null);
                }
                return;
            }

            Dialog dialog = new Dialog(requireContext());
            dialog.setContentView(R.layout.dialog_competitive_lecture_start_warning);
            dialog.getWindow().setBackgroundDrawable(requireContext().getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.setCancelable(true);

            TextView tv_title = dialog.findViewById(R.id.tv_title);
            TextView tv_sub_title = dialog.findViewById(R.id.tv_sub_title);
            CardView cv_negative = dialog.findViewById(R.id.cv_negative);
            CardView cv_positive = dialog.findViewById(R.id.cv_positive);
            TextView tv_negative = dialog.findViewById(R.id.tv_negative);
            TextView tv_positive = dialog.findViewById(R.id.tv_positive);
            ImageView iv_close = dialog.findViewById(R.id.iv_close);

            try {
//                String lectureStartPopUpConfigStr = MyApplication.getFirebaseRemoteConfig().getString("competitive_chapter_list_lecture_start_popup_"+MyApplication.getStringData(PreferenceKeys.SELECTED_COURSE_ID_LOCAL.getKey()));
//                Gson gson = new Gson();
//                PopUpOptionsDto popUpOptionsDto = gson.fromJson(lectureStartPopUpConfigStr, PopUpOptionsDto.class);

                String titleText = popUpOptionsDto.getTitle();
                if (StringUtils.isNotEmpty(titleText)) {
                    tv_title.setText(CommonMethods.isValidJson(titleText) ? StyledTextBuilder.fromJson(requireContext(), titleText) : titleText);
                }

                String subTitle = popUpOptionsDto.getSubTitle();
                if (StringUtils.isNotEmpty(subTitle)) {
                    tv_sub_title.setText(CommonMethods.isValidJson(subTitle) ? StyledTextBuilder.fromJson(requireContext(), subTitle) : subTitle);
                }

                String btnName = popUpOptionsDto.getPrimaryButtonName();
                if (StringUtils.isNotEmpty(btnName)) {
                    tv_positive.setText(CommonMethods.isValidJson(btnName) ? StyledTextBuilder.fromJson(requireContext(), btnName) : btnName);
                }

                String secondaryBtnName = popUpOptionsDto.getSecondaryButtonName();
                if (StringUtils.isNotEmpty(secondaryBtnName)) {
                    tv_negative.setText(CommonMethods.isValidJson(secondaryBtnName) ? StyledTextBuilder.fromJson(requireContext(), secondaryBtnName) : secondaryBtnName);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            cv_negative.setOnClickListener(v -> {

                try {
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.one_shot_lecture_warning_dialog_secondary_click.getActionName(), params);
                } catch (Exception ignored) {
                }

                dialog.dismiss();
                if (dialogPositiveNegativeOptionClickListener != null) {
                    dialogPositiveNegativeOptionClickListener.onNegativeButtonClick(true, null, null);
                }
            });

            cv_positive.setOnClickListener(v -> {

                try {
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.one_shot_lecture_warning_dialog_primary_click.getActionName(), params);
                } catch (Exception ignored) {
                }

                dialog.dismiss();
                if (dialogPositiveNegativeOptionClickListener != null) {
                    dialogPositiveNegativeOptionClickListener.onPositiveButtonClick(true, null, null);
                }
            });

            iv_close.setOnClickListener(v -> {

                try {
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.one_shot_lecture_warning_dialog_close_btn_click.getActionName(), params);
                } catch (Exception ignored) {
                }

                dialog.dismiss();
            });

            dialog.show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showLockPopup(boolean isCompleted, String lockText, String lockBtnText) {

        if (!isAdded() || requireActivity().isFinishing()) {
            Log.w(TAG, "Fragment not attached or activity finishing. Popup skipped.");
            return;
        }

        Log.i(TAG, "showLockPopup | isCompleted=" + isCompleted +
                ", lockText=" + lockText + ", lockBtnText=" + lockBtnText);

        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.custom_dialog_lock_box);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    requireContext().getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        dialog.setCancelable(false);

        MaterialCardView subscriptionLayout = dialog.findViewById(R.id.subscriptionLayout);
        ImageView cancelBtn = dialog.findViewById(R.id.cancel);
        TextView lockDialogText = dialog.findViewById(R.id.header);
        TextView lockDialogBtnText = dialog.findViewById(R.id.subs);
        TextView lockDialogTitle = dialog.findViewById(R.id.dialog_title);
        LottieAnimationView lockAnimation = dialog.findViewById(R.id.dialog_animation);

        cancelBtn.setOnClickListener(v -> {
            Log.d(TAG, "Cancel clicked → dialog dismissed");
            dialog.dismiss();
        });

        subscriptionLayout.setOnClickListener(v -> {
            Log.d(TAG, "SubscriptionLayout clicked | isCompleted=" + isCompleted);
            dialog.dismiss();

            if (isCompleted) {
                Log.i(TAG, "Navigating to CampaignSubscriptionActivity");
//                startActivity(new Intent(requireContext(), CampaignSubscriptionActivity.class));
                startActivity(SubscriptionNavigationHelper.getSubscriptionPageIntent(requireContext(), RedirectionSource.CHAPTER_LIST_LOCKED_POPUP));
            }
        });

        /* ---------- TEXT HANDLING ---------- */

        String finalDialogText = lockText;
        String finalButtonText = lockBtnText;
        String finalDialogTitle = null;

        if (subscriptionCheckDto != null) {
            Log.d(TAG, "subscriptionCheckDto found");

            if (StringUtils.isNotEmpty(subscriptionCheckDto.getDialogText()))
                finalDialogText = subscriptionCheckDto.getDialogText();

            if (StringUtils.isNotEmpty(subscriptionCheckDto.getButtonText()))
                finalButtonText = subscriptionCheckDto.getButtonText();

            if (StringUtils.isNotEmpty(subscriptionCheckDto.getDialogTitle()))
                finalDialogTitle = subscriptionCheckDto.getDialogTitle();
        }

        if (StringUtils.isNotEmpty(finalDialogText)) {
            lockDialogText.setText(finalDialogText);
            Log.d(TAG, "DialogText=" + finalDialogText);
        }

        if (StringUtils.isNotEmpty(finalButtonText)) {
            lockDialogBtnText.setText(finalButtonText);
            subscriptionLayout.setVisibility(View.VISIBLE);
            Log.d(TAG, "ButtonText=" + finalButtonText);
        } else {
            subscriptionLayout.setVisibility(View.GONE);
            Log.d(TAG, "Button hidden due to empty text");
        }

        if (StringUtils.isNotEmpty(finalDialogTitle)) {
            lockDialogTitle.setVisibility(View.VISIBLE);
            lockDialogTitle.setText(finalDialogTitle);
            Log.d(TAG, "DialogTitle=" + finalDialogTitle);
        } else {
            lockDialogTitle.setVisibility(View.GONE);
        }

        /* ---------- ANIMATION HANDLING ---------- */

        if (subscriptionCheckDto != null && subscriptionCheckDto.getActivityParams() != null) {
            Object navigateTo = subscriptionCheckDto.getActivityParams().get("navigate_to");

            Log.d(TAG, "navigate_to param=" + navigateTo);

            if ("home_screen".equalsIgnoreCase(String.valueOf(navigateTo))) {
                lockAnimation.setAnimation(R.raw.alert);
                Log.i(TAG, "Alert animation set for home_screen navigation");
            }
        }

        dialog.show();
        Log.i(TAG, "Lock popup displayed successfully");
    }


    @Override
    public void onChapClick(int position) {
        try {
            adapter.notifyDataSetChanged();
            ChapterNameModel chapterNameModel = chapterNameModels.get(position);
            if (position >= 0 && chapterNameModel != null) {
                MyApplication.saveData(PreferenceKeys.MICROLECTURE_DATA_RELOAD.getKey(), false);
                String id = chapterNameModel.getId();
                String chapterName = chapterNameModel.getChapter();
                String chapterCode = chapterNameModel.getChapterCode();

                if (ChapterLiveByPass.isChapterStatusAllowedToByPass(chapterNameModel.getChapterType(), chapterNameModel.getChapterStatus()) && ChapterLiveByPass.isByPassedUser()) {
                    CommonNavigator.navigateToLiveClassWebView(requireContext(), chapterName, subjectName, subjectCode, id, chapterCode, chapterNameModel.getImageUrl(), chapterNameModel.getName(), TAG, getDataBundle());
                    return;
                }

                if (chapterNameModel.isLocked()) {
                    navigateToSubscriptionLockScreen(chapterNameModel);
                } else if (StringUtils.isNotEmpty(chapterNameModel.getChapterType())
                        && chapterNameModel.getChapterType().equals(ChapterType.LIVE_CHAPTER.name())
                        && StringUtils.isNotEmpty(chapterNameModel.getChapterStatus())
                        && (chapterNameModel.getChapterStatus().equals(ChapterStatus.LIVE_SCHEDULED.name())
                        || (chapterNameModel.getChapterStatus().equals(ChapterStatus.CONVERTING.name()))
                        || chapterNameModel.getChapterStatus().equals(ChapterStatus.RESCHEDULED.name())
                        || (chapterNameModel.getChapterStatus().equals(ChapterStatus.UPCOMING.name())))) {
                    ChapterPopUpDto chapterPopUpDto = chapterNameModel.getChapterLiveDto().getUpcomingStatusPopUp();
                    ;
                    if (chapterPopUpDto != null && StringUtils.isNotEmpty(chapterPopUpDto.getTitle()) && StringUtils.isNotEmpty(chapterPopUpDto.getSubTitle())) {
                        HashMap<String, String> params = new HashMap<>();
                        params.put("chapter_name", chapterName);
                        params.put("chapter_id", id);
                        params.put("status", chapterNameModel.getChapterStatus());
                        params.put("subject_code", subjectCode);

                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.live_closed_or_upcoming_lect_click.getActionName(), params);
                        showLivePop(chapterPopUpDto,chapterNameModel.getChapterStatus());
                    } else if (chapterNameModel.getChapterStatus().equalsIgnoreCase(ChapterStatus.CONVERTING.name())) {
                        CommonMethods.showPositiveSnackbar(fl_parent_layout, getString(R.string.aapka_lecture_thodi_der_aa_jayega), 3000);
                    }
                } else if (StringUtils.isNotEmpty(chapterNameModel.getChapterType()) && chapterNameModel.getChapterType().equals(ChapterType.LIVE_CHAPTER.name())
                        && StringUtils.isNotEmpty(chapterNameModel.getChapterStatus()) && (chapterNameModel.getChapterStatus().equals(ChapterStatus.LIVE.name()))) {
                    if (chapterNameModel.getChapterLiveDto() != null && StringUtils.isNotEmpty(chapterNameModel.getChapterLiveDto().getLiveUrl())) {
                        String ytUrl = chapterNameModel.getChapterLiveDto().getLiveUrl();
                        Log.d(TAG, "URL: " + ytUrl);
                        CommonMethods.openUrlExternally(requireContext(), ytUrl);
                    } else {
                        CommonNavigator.navigateToLiveClassWebView(requireContext(), chapterName, subjectName, subjectCode, id, chapterCode, chapterNameModel.getImageUrl(), chapterNameModel.getName(), TAG, getDataBundle());
                        HashMap<String, String> params = new HashMap<>();
                        params.put("chapterName", chapterName);
                        params.put("subjectCode", subjectCode);
                        params.put("chapterId", id);
                        params.put("chapterCode", chapterCode);
                        params.put("navigatingFrom", TAG);
                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.live_chapter_clicked.getActionName(), params);
                    }
                } else {
                    if (warningPopUpDialogDto != null) {
                        try {
                            HashMap<String, String> params = new HashMap<>();
                            params.put("Chapter_Name", chapterName);
                            params.put("Subject_Code", subjectCode);
                            params.put("Chapter_Id", id);
                            params.put("chapterCode", chapterCode);
                            params.put("navigating_from", TAG);

                            showLectureStartWarning(warningPopUpDialogDto, params, new DialogPositiveNegativeOptionClickListener() {
                                @Override
                                public void onPositiveButtonClick(boolean status, String feedback, Object object) {
                                    Log.d(TAG, "Primary Button click");
                                    setVideoProgress(chapterNameModel.getChapterCode());
                                    MyApplication.saveData(PreferenceKeys.ADAPTIVE_CHAPTER_COMPLETION_SCREEN_VISIBLE.getKey(), true);
                                    navigateToMicrolectureActivity(chapterNameModel);

                                    if (refreshActivityListener != null) {
                                        Log.d(TAG, "Refresh Page : true");
                                        refreshActivityListener.refreshPage(true);
                                    }
                                }

                                @Override
                                public void onNegativeButtonClick(boolean status, String feedback, Object object) {
                                    Log.d(TAG, "Secondary Button click");
                                }
                            });

                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.one_shot_lecture_warning_dialog_viewed.getActionName(), params);
                        } catch (Exception ignored) {
                        }
                    } else {
                        navigateToMicrolectureActivity(chapterNameModel);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bundle getDataBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("id", subjectId);
        bundle.putString(PreferenceKeys.NAME.getKey(), subjectName);
        bundle.putString("code", subjectCode);
//        bundle.putString(PreferenceKeys.BOARD_CODE.getKey(), boardCode);
        return bundle;
    }

    private void showLivePop(ChapterPopUpDto chapterPopUpDto, String chapterStatus) {
        if (chapterPopUpDto == null || liveDialog != null && liveDialog.isShowing())
            return;

        if (liveDialog != null && liveDialog.isShowing()) {
            return;
        }
        try {
            liveDialog = new Dialog(requireActivity());
            liveDialog.setContentView(R.layout.dialog_alert_title_subtitle_button);
            liveDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
            liveDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            liveDialog.setCancelable(true);
            liveDialog.setCanceledOnTouchOutside(true);

            TextView title = liveDialog.findViewById(R.id.title);
            TextView subtitle = liveDialog.findViewById(R.id.subtitle);
            TextView btnTextView = liveDialog.findViewById(R.id.btnText);
            CardView cv_confirm = liveDialog.findViewById(R.id.cv_okay);
            ImageView iv_close = liveDialog.findViewById(R.id.iv_close);
            ImageView dialogImage= liveDialog.findViewById(R.id.selection_image);
            LottieAnimationView animation = liveDialog.findViewById(R.id.animation);
            Log.d(TAG, "Views bound successfully"+chapterStatus);

            if(chapterStatus.equals(ChapterStatus.RESCHEDULED.name())){
                dialogImage.setVisibility(View.VISIBLE);
                Glide.with(requireContext())
                        .load(R.drawable.live_rescheduled)
                        .into(dialogImage);
                animation.setVisibility(View.GONE);
            }else {
                animation.setAnimation(R.raw.live_lottie_anim);
                animation.setVisibility(View.VISIBLE);
                dialogImage.setVisibility(View.GONE);
                Log.d(TAG, "Lottie animation set: live_lottie_anim");
            }

            String titleText = chapterPopUpDto.getTitle();
            String subTitle = chapterPopUpDto.getSubTitle();
            String buttonText = chapterPopUpDto.getBtnName();

            title.setVisibility(View.GONE);
            subtitle.setVisibility(View.GONE);
            btnTextView.setVisibility(View.GONE);

            if (StringUtils.isNotEmpty(titleText)) {
                title.setText(CommonMethods.isValidJson(titleText) ?
                        StyledTextBuilder.fromJson(requireActivity(), titleText) : titleText);
                title.setVisibility(View.VISIBLE);
            }

            if (StringUtils.isNotEmpty(subTitle)) {
                subtitle.setText(CommonMethods.isValidJson(subTitle) ?
                        StyledTextBuilder.fromJson(requireActivity(), subTitle) : subTitle);
                subtitle.setVisibility(View.VISIBLE);
            }

            if (StringUtils.isNotEmpty(buttonText)) {
                btnTextView.setText(CommonMethods.isValidJson(buttonText) ?
                        StyledTextBuilder.fromJson(requireActivity(), buttonText) : buttonText);
                btnTextView.setVisibility(View.VISIBLE);
            }

            cv_confirm.setOnClickListener(view -> {
                dismissLivePopUp();
            });

            iv_close.setOnClickListener(v -> {
                dismissLivePopUp();
            });

            liveDialog.show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void navigateToMicrolectureActivity(ChapterNameModel chapterNameModel) {
//        MyApplication.saveData(subjectName, position);
        String id = chapterNameModel.getId();
        String chapterName = chapterNameModel.getChapter();
        String introImage = chapterNameModel.getTeacherIntroImage();
        String chapterCode = chapterNameModel.getChapterCode();

        Intent intent = new Intent(requireContext(), MicrolectureListActivity.class);
        if (StringUtils.isNotEmpty(introImage)) {
            intent.putExtra("IntroImage", introImage);
        }
        if (StringUtils.isNotEmpty(chapterNameModel.getNotesUrl())) {
            intent.putExtra("NotesPdfUrl", chapterNameModel.getNotesUrl());
        }
        intent.putExtra("chapterName", chapterName);
        intent.putExtra("subjectCode", subjectCode);
        intent.putExtra("chapterId", id);
        MyApplication.saveData(PreferenceKeys.MICROLECTURE_ID.getKey(), "-1");
        intent.putExtra("selectedSubject", subjectName);

        HashMap<String, String> params = new HashMap<>();
        params.put("Chapter_Name", chapterName);
        params.put("Subject_Code", subjectCode);
        params.put("Chapter_Id", id);
        params.put("chapterCode", chapterCode);
        params.put("navigating_from", TAG);
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.one_shot_lecture_chapter_click.getActionName(), params);

        startActivity(intent);
    }

    private void navigateToSubscriptionLockScreen(ChapterNameModel chapterNameModel) {
//        Intent intent = new Intent(requireContext(), CampaignSubscriptionActivity.class);
        Intent intent = SubscriptionNavigationHelper.getSubscriptionPageIntent(requireContext(), RedirectionSource.CHAPTER_LIST_LOCKED_POPUP);
        intent.putExtra(CampaignSubscriptionActivity.BUNDLE_SUBSCRIPTION_DATA, subscriptionLockPopupDto);
//            startActivity(intent);
        HashMap<String, String> params = new HashMap<>();
        params.put("chapter_name", chapterNameModel.getChapter());
        params.put("chapter_id", chapterNameModel.getId());
        params.put("subject_code", subjectCode);
        params.put("source_screen", TAG);
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.sub_screen_opened.getActionName(), params);
        subscriptionScreenLauncher.launch(intent);
    }

    private void dismissLivePopUp() {
        if (liveDialog != null &&
                liveDialog.isShowing()) {
            liveDialog.dismiss();

            liveDialog = null;
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.neet_ll_locked_popup_close_click.getActionName());
        }
    }

    private void attachObserversForChapters(String subjectId, List<String> chapterIds) {
        if (StringUtils.isNotEmpty(subjectId)) {
            for (String chapterId : chapterIds) {
                attachChapterListener(subjectId, chapterId);
            }
        }
    }

    private void attachChapterListener(String subjectId, String chapterId) {

        String key = subjectId + "_" + chapterId;

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("chapter_status_update")
                .child(subjectId)
                .child(chapterId);

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    if (!snapshot.exists()) return;

//                Long updatedAt = snapshot.child("updatedAt").getValue(Long.class);
//                String status = snapshot.child("status").getValue(String.class);
                    String status = snapshot.getValue(String.class);

                    Log.d("CHAPTER_OBSERVER", "Update: " + key +
                            " status=" + status + " updatedAt=");

                    if (StringUtils.isNotEmpty(status) && chapterIdList.contains(chapterId)) {
                        int index = chapterIdList.indexOf(chapterId);
                        ChapterNameModel chapterNameModel = chapterNameModels.get(index);
                        if (StringUtils.isNotEmpty(chapterNameModel.getChapterStatus()) && !chapterNameModel.getChapterStatus().equals(status)) {
                            Log.d(TAG, "---> Status: " + status + ", " + chapterNameModel.getChapterStatus());
                            getChaptersBySubjectId(subjectId);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CHAPTER_OBSERVER", "Error for " + key + " -> " + error.getMessage());
            }
        };

        // Attach listener
        ref.addValueEventListener(listener);

        // Save for detaching later
        chapterListeners.put(key, listener);
        chapterRefs.put(key, ref);

        Log.d("CHAPTER_OBSERVER", "Attached listener for " + key);
    }

    private void detachAllChapterListeners() {
        try {
            for (String key : chapterListeners.keySet()) {

                DatabaseReference ref = chapterRefs.get(key);
                ValueEventListener listener = chapterListeners.get(key);

                if (ref != null && listener != null) {
                    ref.removeEventListener(listener);
                    Log.d("CHAPTER_OBSERVER", "Detached observer for " + key);
                }
            }

            chapterListeners.clear();
            chapterRefs.clear();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onInfoClicked(String imageUrl) {
        showPopupDialog(imageUrl);
    }

}