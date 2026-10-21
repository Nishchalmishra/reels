package arivihan.technologies.doubtbuzzter2.fragments;

import static android.app.Activity.RESULT_OK;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_GET_TODAY_CLASS;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.MARKED_VIDEO_COMPLETION;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.POST_CHAPTER_DEMO_PROGRESS;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.VIDEO_COMPLETION;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.CampaignSubscriptionActivity;
import arivihan.technologies.doubtbuzzter2.activity.ExoPlayerActivity;
import arivihan.technologies.doubtbuzzter2.activity.MicrolectureListActivity;
import arivihan.technologies.doubtbuzzter2.adapter.TodayClassAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.enums.DppStatus;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.enums.SubscriptionPopupType;
import arivihan.technologies.doubtbuzzter2.enums.TodayClassType;
import arivihan.technologies.doubtbuzzter2.fragments.competitive.home.CompetitiveHomeScreenFragment;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.helper.WrapContentLinearLayoutManager;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.listener.TodayClassApiCallback;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionLockPopupDto;
import arivihan.technologies.doubtbuzzter2.model.chapter.live.ChapterPopUpDto;
import arivihan.technologies.doubtbuzzter2.enums.TodayTaskListType;
import arivihan.technologies.doubtbuzzter2.model.TodayClassDto;
import arivihan.technologies.doubtbuzzter2.model.crash_course.TodayClassItemResponse;
import arivihan.technologies.doubtbuzzter2.model.crash_course.TodayClassResponse;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.model.remote_config.ActivityNavigationConfig;
import arivihan.technologies.doubtbuzzter2.model.todo.TodayClassGuideVideoDto;
import arivihan.technologies.doubtbuzzter2.model.todo.TodayClassListModel;
import arivihan.technologies.doubtbuzzter2.utils.RecyclerViewEdgeEffect;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class TodayTaskFragment extends Fragment
        implements TodayClassAdapter.TodayClassAdapterListener, TodayClassApiCallback {

    private static final String TAG = TodayTaskFragment.class.getSimpleName();
    private RecyclerView todayTaskClassRecyclerView;
    private CardView hourCountDownBackground, minuteCountDownBackground, secondCountDownBackground;
    private TextView hourCountDown, minuteCountDown, secondCountDown, fragmentTitle;
    private LinearLayout ll_sub_title;
    private TextView tv_today_task_status;
    private ImageView iv_today_task_status;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    TodayTaskListener todayTaskListener;
    private String screenCallingFrom;
    private TodayClassAdapter todayClassAdapter;
    private boolean showScrollHint = false;
    private boolean scrollHintDone = false;

    private RelativeLayout todayClassesLayout;
    private CardView cv_left_scroll, cv_right_scroll;

    private List<TodayClassItemResponse> todayClassDtos = new ArrayList<>();

    private Integer completedTasks = 0;
    MyJsonObjectRequest todayClassData;
    private TodayClassGuideVideoDto guideVideoDto;
    private Dialog lectureLiveUpcomingDialog;
    private Dialog lockPopUpDialog;
    private HorizontalScrollView hs_countdown;

    private LinearLayout ll_see_all;
    private TextView tv_see_all;

    private final ActivityResultLauncher<Intent> subscriptionScreenLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    try {
                        //isSubscribed
                        Intent data = result.getData();
                        boolean isSubscribed = data.getBooleanExtra("isSubscribed", false);
                        if (isSubscribed) {
                            fetchTodayClassData();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
    );

    public TodayTaskFragment() {
    }

    public TodayTaskFragment(TodayTaskListener todayTaskListener, String screenCallingFrom) {
        this.todayTaskListener = todayTaskListener;
        this.screenCallingFrom = screenCallingFrom;
    }

    public TodayTaskFragment(TodayTaskListener todayTaskListener, String screenCallingFrom, boolean showScrollHint) {
        this.todayTaskListener = todayTaskListener;
        this.screenCallingFrom = screenCallingFrom;
        this.showScrollHint = showScrollHint;
    }

    @Override
    public void onStart() {
        super.onStart();
        MyApplication.setTodayClassApiCallback(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        MyApplication.setTodayClassApiCallback(null);
        dismissLiveUpcomingDialog();
        dismissLockPopUpDialog();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_today_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: Method started");
        hourCountDownBackground = view.findViewById(R.id.hour_countdown_background);
        minuteCountDownBackground = view.findViewById(R.id.minute_countdown_background);
        secondCountDownBackground = view.findViewById(R.id.second_countdown_background);
        fragmentTitle = view.findViewById(R.id.title_text);
        hs_countdown = view.findViewById(R.id.hs_countdown);
        cv_right_scroll = view.findViewById(R.id.cv_right_scroll);
        cv_left_scroll = view.findViewById(R.id.cv_left_scroll);

        if (MyApplication.getFirebaseRemoteConfig().getBoolean("hide_countDown")) {
            hs_countdown.setVisibility(View.GONE);
        } else {
            hs_countdown.setVisibility(View.VISIBLE);
        }

        hourCountDown = view.findViewById(R.id.hour_countdown);
        minuteCountDown = view.findViewById(R.id.minute_countdown);
        secondCountDown = view.findViewById(R.id.second_countdown);

        todayClassesLayout = view.findViewById(R.id.today_classes_view);

        ll_sub_title = view.findViewById(R.id.ll_sub_title);
        tv_today_task_status = view.findViewById(R.id.tv_today_task_status);
        iv_today_task_status = view.findViewById(R.id.iv_today_task_status);

        // See All section
        ll_see_all = view.findViewById(R.id.ll_see_all);
        tv_see_all = view.findViewById(R.id.tv_see_all);
        Log.d(TAG, "onViewCreated: todayClassesLayout initialized");

        todayTaskClassRecyclerView = view.findViewById(R.id.today_class_recycler_view);
        todayTaskClassRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        RecyclerViewEdgeEffect.edgeEffectRecyclerView(todayTaskClassRecyclerView, getActivity(), true);

        ll_sub_title.setVisibility(View.GONE);
        ll_see_all.setVisibility(View.GONE);
        fragmentTitle.setVisibility(View.GONE);

        iv_today_task_status.setOnClickListener(v -> openGuideVideo());

        tv_today_task_status.setOnClickListener(v -> openGuideVideo());

        todayTaskClassRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                updateFeatureSectionArrowVisibility();
            }
        });

        cv_left_scroll.setOnClickListener(v -> todayTaskClassRecyclerView.smoothScrollBy(-todayTaskClassRecyclerView.getWidth(), 0));
        cv_right_scroll.setOnClickListener(v -> todayTaskClassRecyclerView.smoothScrollBy(todayTaskClassRecyclerView.getWidth(), 0));

        fetchTodayClassData();
        startCountdown();
    }

    private void updateFeatureSectionArrowVisibility() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) todayTaskClassRecyclerView.getLayoutManager();
        if (layoutManager == null || todayTaskClassRecyclerView.getAdapter() == null) return;

        int firstVisible = layoutManager.findFirstCompletelyVisibleItemPosition();
        int lastVisible = layoutManager.findLastCompletelyVisibleItemPosition();
        int total = todayTaskClassRecyclerView.getAdapter().getItemCount();

        cv_left_scroll.setVisibility(firstVisible > 0 ? View.VISIBLE : View.GONE);
        cv_right_scroll.setVisibility(lastVisible < total - 1 ? View.VISIBLE : View.GONE);
    }

    private void openGuideVideo() {
        try {
            if (guideVideoDto != null) {
                if (StringUtils.isNotEmpty(guideVideoDto.getClassName())) {
                    MyApplication.openActivity(requireContext(), guideVideoDto.getClassName(), guideVideoDto.getActivityParams());

                    if (guideVideoDto.getClassName().equals(ExoPlayerActivity.CLASS_NAME) && guideVideoDto.getActivityParams() != null && guideVideoDto.getActivityParams().containsKey("identifier")) {
                        videoClick((String) guideVideoDto.getActivityParams().get("identifier"));
                    }
                } else if (StringUtils.isNotEmpty(guideVideoDto.getDeepLink())) {
                    MyApplication.openDeepLink(requireContext(), guideVideoDto.getDeepLink());
                }
            }

//            if (guideVideoDto!=null && StringUtils.isNotEmpty(guideVideoDto.getVideoUrl())) {
//                Intent intent = new Intent(requireContext(), ExoPlayerActivity.class);
//                intent.putExtra("video_uri", guideVideoDto.getVideoUrl());
//                MyApplication.logFirebaseBundleCreation("clicked_todays_task_info");
//                startActivity(intent);
//            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void fetchTodayClassData() {
        loadTodayClassFromCacheIfAvailable();
        getTodayClassData();
    }

    private void getTodayClassData() {
        MyVolleySingleton.getInstance(requireContext())
                .getRequestQueue()
                .cancelAll(TAG);

        Log.d(TAG, "getTodayClassData: API call started");

        getTodayClassDataNonCompetitive();

    }

    private void getTodayClassDataNonCompetitive() {
        Map<String, Object> params = new HashMap<>();
        if ((screenCallingFrom != null && screenCallingFrom.equals(CompetitiveHomeScreenFragment.TAG)) || MyApplication.isCompetitiveProfile()) {
            params.put("isNeet", true);
        }
//        params.put("batchId", MyApplication.getIntData(PreferenceKeys.BATCH_ID.getKey()));

        Log.d(TAG, "getTodayClassDataNonCompetitive Request: " + params.toString());

        todayClassData = new MyJsonObjectRequest(
                Request.Method.GET,
                CommonMethods.getUrlParam(AV_GET_TODAY_CLASS, params),
                null,
                response -> {
                    try {
                        Log.d(TAG, "getTodayClassDataNonCompetitive: response received: "+response);
                        Gson gson = new Gson();
                        TypeToken<ApiWrappedResponse<TodayClassResponse>> token = new TypeToken<ApiWrappedResponse<TodayClassResponse>>() {
                        };
                        ApiWrappedResponse<TodayClassResponse> apiResponse = gson.fromJson(response.toString(), token.getType());
                        TodayClassResponse todayClassResponse = apiResponse.getData();
                        Log.d(TAG, "getTodayClassDataNonCompetitive-----status: " + todayClassResponse);
                        String cacheKey = getTodayClassCacheKey();
                        MyApplication.saveTodayClassesData(cacheKey, todayClassResponse);
                        renderTodayClassUI(todayClassResponse);

                    } catch (Exception e) {
                        Log.e(TAG, "getTodayClassDataNonCompetitive: Parsing error", e);
                    }
                },
                new VolleyGlobalErrorListener(getContext()) {
                    @Override
                    public void postError(VolleyError error) {
                        Log.e(TAG, "getTodayClassDataNonCompetitive: API failed", error);
                    }
                }
        );

        todayClassData.setTag(TAG);
        MyVolleySingleton.getInstance(getContext()).addToRequestQueue(todayClassData);
    }

    private TodayClassResponse mapToTodayClassResponse(TodayClassListModel model) {
        List<TodayClassItemResponse> items = new ArrayList<>();
        if (model.getTodayClassDto() != null) {
            for (TodayClassDto dto : model.getTodayClassDto()) {
                TodayClassItemResponse item = new TodayClassItemResponse();
                item.setType(dto.getType());
                item.setPosition(dto.getPosition() != null ? dto.getPosition() : 0);
                item.setIsCompleted(dto.getIsCompleted());
                item.setProgress(dto.getProgress());
                item.setDemoLecture(dto.isDemoLecture());
                item.setRoutineId(dto.getRoutineId());
                item.setChapterId(dto.getChapterId());
                item.setChapterCode(dto.getChapterCode());
                item.setSubjectId(dto.getSubjectId());
                item.setChapterName(dto.getChapterName());
                item.setChapterThumbnailUrl(dto.getChapterThumbnailUrl());
                item.setChapterType(dto.getChapterType());
                item.setChapterStatus(dto.getChapterStatus());
                item.setChapterLiveDto(dto.getChapterLiveDto());
                item.setLiveNavigationBundle(dto.getLiveNavigationBundle());
                item.setSubjectIconUrl(dto.getSubjectIconUrl());
                item.setMicroLectureId(dto.getMicroLectureId());
                item.setMicroLectureTitle(dto.getMicroLectureTitle());
                item.setMicroLectureViewCount(dto.getMicroLectureViewCount());
                item.setMicroLectureViewCountStr(dto.getMicroLectureViewCountStr());
                item.setViewCountText(dto.getViewCountText());
                item.setThumbnailUrl(dto.getThumbnailUrl());
                item.setDuration(dto.getDuration());
                item.setVideoDuration(dto.getVideoDuration());
                item.setLiveClassMeetingLink(dto.getLiveClassMeetingLink());
                item.setLiveClassDate(dto.getLiveClassDate());
                item.setLiveClassDuration(dto.getLiveClassDuration());
                item.setLiveClassStatus(dto.getLiveClassStatus());
                item.setRecordedLectureUrl(dto.getRecordedLectureUrl());
                item.setLiveClassSnackBarMessage(dto.getLiveClassSnackBarMessage());
                item.setTitle(dto.getTitle());
                item.setSubTitle(dto.getSubTitle());
                item.setHeading(dto.getHeading());
                item.setBtnText(dto.getBtnText());
                item.setIcon(dto.getIcon());
                item.setDeeplink(dto.getDeeplink());
                item.setNotesPdfUrl(dto.getNotesPdfUrl());
                item.setClassName(dto.getClassName());
                item.setNavigationType(dto.getNavigationType());
                item.setActivityParams(dto.getActivityParams());
                item.setNotesData(dto.getNotesData());
                item.setNumberOfQuestions(dto.getNumberOfQuestions());
                item.setSubjectName(dto.getSubjectName());
                item.setTeacherName(dto.getTeacherName());
                item.setNewContent(dto.isNewContent());
                item.setLocked(dto.isLocked());
                item.setSubscriptionLockPopupDto(dto.getSubscriptionLockPopupDto());
                items.add(item);
            }
        }

        TodayClassResponse response = new TodayClassResponse();
        response.setLayoutTitle(model.getLayoutTitle());
        response.setLayoutDescription(model.getLayoutDescription());
        response.setFragmentType(model.getFragmentType());
        response.setShowTimer(model.isShowTimer());
        response.setGuideVideo(model.getGuideVideo());
        response.setSeeAllConfig(model.getSeeAllConfig());
        response.setClasses(items);
        return response;
    }

    private void setDemoVideoOpen(String chapterCode, String chapterId) {
        try {

            Map<String, Object> requestParameters = new HashMap<>();
            requestParameters.put("chapterId", chapterId);
            Log.d(TAG, "Progress API Request: " + requestParameters.toString());

            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                    CommonMethods.getUrlParam(POST_CHAPTER_DEMO_PROGRESS, requestParameters), null,
                    response -> {
                        Log.d(TAG, "Progress API Response: " + response.toString());
                        if (isFragmentSafe()) {
                            fetchTodayClassData();
                        } else {
                            Log.w(TAG, "Fragment not in valid state, skipping fetchTodayClassData()");
                        }
                    }, error -> {
                Log.d(TAG, "Progress API Error Response: " + error.toString());
            });
            myJsonObjectRequest.setTag(TAG);
            MyVolleySingleton.getInstance(requireContext()).addToRequestQueue(myJsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isFragmentSafe() {
        return isAdded()
                && getActivity() != null
                && !isDetached()
                && getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED);
    }

    private void renderTodayClassUI(TodayClassResponse todayClassResponse) {

        // Prepend the board batch overview video (from Remote Config) at position 0
//        TodayClassItemResponse batchOverviewItem = buildBatchOverviewItem();

        if (todayClassResponse == null ||
                todayClassResponse.getClasses() == null ||
                todayClassResponse.getClasses().isEmpty()) {
            Log.d(TAG, "No today class data to render");

            todayClassesLayout.setVisibility(View.GONE);

            if (isAdded() && todayTaskListener != null) {
                todayTaskListener.onDataFetched(this, 0);
            }
            return;

        }

        List<TodayClassItemResponse> dtos = todayClassResponse.getClasses();

        todayClassesLayout.setVisibility(View.VISIBLE);

        if (StringUtils.isNotEmpty(todayClassResponse.getLayoutTitle())) {
            fragmentTitle.setText(todayClassResponse.getLayoutTitle());
            fragmentTitle.setVisibility(View.VISIBLE);
        }

        hs_countdown.setVisibility(todayClassResponse.isShowTimer() ? View.VISIBLE : View.GONE);

        if (StringUtils.isNotEmpty(todayClassResponse.getLayoutDescription())) {
            ll_sub_title.setVisibility(View.VISIBLE);
            if (CommonMethods.isValidJson(todayClassResponse.getLayoutDescription())) {
                tv_today_task_status.setText(
                        StyledTextBuilder.fromJson(
                                requireContext(),
                                todayClassResponse.getLayoutDescription()
                        )
                );
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    tv_today_task_status.setText(
                            Html.fromHtml(todayClassResponse.getLayoutDescription(),
                                    Html.FROM_HTML_MODE_COMPACT)
                    );
                }
            }
        }

        // Set See All Section if available
        if (todayClassResponse.getSeeAllConfig() != null && todayClassResponse.getSeeAllConfig().getClassName() != null) {
            Log.d(TAG, "SEE All : " + todayClassResponse.getSeeAllConfig());
            ActivityNavigationConfig seeAllConfig = todayClassResponse.getSeeAllConfig();
            String seeAllBtnText = seeAllConfig.getFeatureName();
            tv_see_all.setText(CommonMethods.isValidJson(seeAllBtnText) ? StyledTextBuilder.fromJson(requireContext(), seeAllBtnText) : seeAllBtnText);
            ll_see_all.setOnClickListener(v -> {
                CommonMethods.navigateFromActivityConfig(requireContext(), seeAllConfig.getNavigationType(), seeAllConfig.getClassName(), seeAllConfig.getParams());
                HashMap<String, String> eventParams = new HashMap<>();
                eventParams.put("source", TAG);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.todays_classes_see_all_section_clicked.getActionName(), eventParams);
            });
            ll_see_all.setVisibility(View.VISIBLE);
        }

        guideVideoDto = todayClassResponse.getGuideVideo();

//        if (batchOverviewItem != null) {
//            dtos.add(0, batchOverviewItem);
//            for (TodayClassItemResponse dto : dtos) {
//                dto.setPosition(dtos.indexOf(dto) + 1);
//            }
//        }

        checkForDemoLecture(dtos);
        todayClassDtos = dtos;
//        TodayClassAdapter.moveBatchOverviewToEnd(dtos);
        todayClassAdapter = new TodayClassAdapter(
                getContext(),
                todayClassResponse.getFragmentType() != null ? todayClassResponse.getFragmentType() : TodayTaskListType.TODAY_TASK_FRAGMENT,
                dtos,
                this
        );

        todayTaskClassRecyclerView.setAdapter(todayClassAdapter);
//        maybeShowTodayTaskScrollHint();

        if (isAdded() && todayTaskListener != null) {
            todayTaskListener.onDataFetched(this, todayClassResponse.getClasses() != null ? todayClassResponse.getClasses().size() : 0);
        }

        if (isAdded() && todayTaskListener != null) {
            todayTaskListener.hideContinueSection();
        }
    }

    private void loadTodayClassFromCacheIfAvailable() {

        String cacheKey = getTodayClassCacheKey();
        TodayClassResponse cachedModel = MyApplication.getTodayClassesData(cacheKey);
        Log.d(TAG, "loadTodayClassFromCacheIfAvailable: " + cachedModel);

        if (cachedModel == null) {
            Log.d(TAG, "No cached TodayClass data");
            return;
        }

        Log.d(TAG, "Rendering TodayClass data from cache");
        renderTodayClassUI(cachedModel);
    }


    private void maybeShowTodayTaskScrollHint() {
        if (!showScrollHint || scrollHintDone) return;
        if (todayClassDtos.size() < 4) return;
        scrollHintDone = true;
        todayTaskClassRecyclerView.post(() -> {
            Log.d(TAG, "--> Auto Scrolling : ");
            if (!isAdded() || todayTaskClassRecyclerView == null) return;
            LinearLayoutManager lm = (LinearLayoutManager) todayTaskClassRecyclerView.getLayoutManager();
            if (lm == null) return;
            if (lm.findLastCompletelyVisibleItemPosition() >= 4) return;
            todayTaskClassRecyclerView.smoothScrollToPosition(4);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!isAdded() || todayTaskClassRecyclerView == null) return;
                todayTaskClassRecyclerView.smoothScrollToPosition(0);
            }, 1800);
        });
    }

    private String getTodayClassCacheKey() {
        String courseId = MyApplication.getStringData(PreferenceKeys.SELECTED_COURSE_ID_LOCAL.getKey());
        String language = MyApplication.getStringData(PreferenceKeys.MY_LANG_CAMEL.getKey());
        boolean isBoard = MyApplication.getBoolData(PreferenceKeys.BOARD_SWITCH.getKey());

        return "TODAY_CLASS_" + courseId + "_" + language + "_" + isBoard;
    }

    /**
     * Reads the {@code board_batch_overview_video} Remote Config key and builds a
     * {@link TodayClassItemResponse} of type {@code CONTINUE_LECTURE} to be inserted at
     * position 0 of the today-class list.
     * Returns {@code null} if the key is empty or the JSON is malformed.
     */
    public static void checkForDemoLecture(List<TodayClassItemResponse> todayClassDtoList) {
        List<TodayClassItemResponse> list = new ArrayList<>();

        Iterator<TodayClassItemResponse> iterator = todayClassDtoList.iterator();

        while (iterator.hasNext()) {
            TodayClassItemResponse todayClass = iterator.next();

            if (todayClass.isDemoLecture()) {

                Log.d(TAG, "Completed Task : demo lecture ");

                String userId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
                String key = userId + "_" + todayClass.getMicroLectureId();
                boolean isCompletedStored = MyApplication.getBooleanDataFromDoNotDeleteSharedPreference(key);

                todayClass.setIsCompleted(isCompletedStored);

                if (isCompletedStored) {
                    list.add(todayClass);
                    iterator.remove();
                }
            }
        }

        if (!list.isEmpty()) {
            todayClassDtoList.addAll(list);
            int index = 1;
            for (TodayClassItemResponse item : todayClassDtoList)
                item.setPosition(index++);
        }
    }

    private void startCountdown() {
        runnable = new Runnable() {
            @Override
            public void run() {
                updateCountdown();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    private void updateCountdown() {
        Calendar now = Calendar.getInstance();
        long currentTimeInMillis = now.getTimeInMillis();

        Calendar targetTime = Calendar.getInstance();
        targetTime.set(Calendar.HOUR_OF_DAY, 24);
        targetTime.set(Calendar.MINUTE, 0);
        targetTime.set(Calendar.SECOND, 0);
        targetTime.set(Calendar.MILLISECOND, 0);

        if (currentTimeInMillis > targetTime.getTimeInMillis()) {
            targetTime.add(Calendar.DAY_OF_YEAR, 1);
        }

        long targetTimeInMillis = targetTime.getTimeInMillis();
        long countdownMillis = targetTimeInMillis - currentTimeInMillis;

        long hours = TimeUnit.MILLISECONDS.toHours(countdownMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(countdownMillis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(countdownMillis) % 60;


        String textColor = "";
        String backgroundColor = "";

        if (hours < 6) {
            //red
            textColor = "#ED5588";
            backgroundColor = "#FBDDE7";
        } else if (hours < 12) {
            //yellow
            textColor = "#FABC4B";
            backgroundColor = "#FEF2DB";
        } else {
            //green
            textColor = "#28BC84";
            backgroundColor = "#D4F1E6";
        }

        hourCountDown.setTextColor(Color.parseColor(textColor));
        minuteCountDown.setTextColor(Color.parseColor(textColor));
        secondCountDown.setTextColor(Color.parseColor(textColor));

        hourCountDownBackground.setCardBackgroundColor(Color.parseColor(backgroundColor));
        minuteCountDownBackground.setCardBackgroundColor(Color.parseColor(backgroundColor));
        secondCountDownBackground.setCardBackgroundColor(Color.parseColor(backgroundColor));


        hourCountDown.setText(String.format(Locale.ENGLISH, "%02dh", hours));
        minuteCountDown.setText(String.format(Locale.ENGLISH, "%02dm", minutes));
        secondCountDown.setText(String.format(Locale.ENGLISH, "%02ds", seconds));
    }

    private void showLiveUpcomingDialog(ChapterPopUpDto chapterPopUpDto, String chapterStatus) {
        try {
            if (chapterPopUpDto == null)
                return;
            dismissLiveUpcomingDialog();
            lectureLiveUpcomingDialog = CommonMethods.showGenericPopupInformation(requireContext(), chapterPopUpDto.getTitle(), chapterPopUpDto.getSubTitle(), chapterPopUpDto.getBtnName(),chapterStatus);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showDppUpcomingDialog(SubscriptionLockPopupDto dto) {
        try {
            if (dto == null) return;
            dismissLiveUpcomingDialog();
            lectureLiveUpcomingDialog = CommonMethods.showGenericPopupInformation(
                    requireContext(), dto.getTitle(), dto.getSubTitle(), dto.getBtnName(), "UPCOMING");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showLockDialog(SubscriptionLockPopupDto subscriptionLockPopupDto) {
        MyApplication.logFirebaseBundleCreation("home_live_lock_dialog_click");
        try {
            Log.d(TAG, "----> Dialog Lock: ");
            if (subscriptionLockPopupDto == null)
                subscriptionLockPopupDto = new SubscriptionLockPopupDto();
            dismissLockPopUpDialog();
            lockPopUpDialog = CommonMethods.showLockPopupDialog(requireContext(), subscriptionLockPopupDto);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void dismissLiveUpcomingDialog() {
        if (lectureLiveUpcomingDialog != null && lectureLiveUpcomingDialog.isShowing()) {
            lectureLiveUpcomingDialog.dismiss();
            lectureLiveUpcomingDialog = null;
        }
    }

    private void dismissLockPopUpDialog() {
        if (lockPopUpDialog != null && lockPopUpDialog.isShowing()) {
            lockPopUpDialog.dismiss();
            lockPopUpDialog = null;
        }
    }

    private void navigateToSubscriptionLockScreen(SubscriptionLockPopupDto subscriptionLockPopupDto) {
//        Intent intent = new Intent(requireContext(), CampaignSubscriptionActivity.class);
        Intent intent = SubscriptionNavigationHelper.getSubscriptionPageIntent(requireContext(), RedirectionSource.LIVE_LOCKED_POPUP);
        if (subscriptionLockPopupDto != null) {
            intent.putExtra(CampaignSubscriptionActivity.BUNDLE_SUBSCRIPTION_DATA, subscriptionLockPopupDto);
        }
        Map<String, String> eventParams = new HashMap<>();
        eventParams.put("source", TAG);
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.navigate_to_sub_plan_screen.getActionName(), eventParams);
        subscriptionScreenLauncher.launch(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
        if (todayClassData != null) {
            todayClassData.cancel();
        }
        dismissLiveUpcomingDialog();
        dismissLockPopUpDialog();
    }

    @Override
    public void showSnackBar(String liveClassSnackBarMessage) {
        CommonMethods.showLongPositiveSnackbar(requireView(), liveClassSnackBarMessage);
    }

    @Override
    public void markVideoCompletedStatus() {
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                MARKED_VIDEO_COMPLETION, null,
                response -> {
                    Log.d("MARKED_VIDEO_COMPLETION", "Successfully updated.");
                },
                new VolleyGlobalErrorListener(requireContext()) {
                    @Override
                    public void postError(VolleyError error) {
                        Log.e("UPDATE_TODO_ORDER_STATE", error.getMessage());
                    }
                });

        myJsonObjectRequest.setTag("updateVideoSelection");
        MyVolleySingleton.getInstance(requireContext()).addToRequestQueue(myJsonObjectRequest);
    }

    @Override
    public void videoClick(String identifier) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("identifier", identifier);
            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                    VIDEO_COMPLETION, jsonObject,
                    response -> {
                        Log.d("VIDEO_COMPLETION", "Successfully updated.");
                    },
                    new VolleyGlobalErrorListener(requireContext()) {
                        @Override
                        public void postError(VolleyError error) {
                            Log.e("UPDATE_TODO_ORDER_STATE", error.getMessage());
                        }
                    });

            myJsonObjectRequest.setTag("videoCompletion");
            MyVolleySingleton.getInstance(requireContext()).addToRequestQueue(myJsonObjectRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void showPopUp(TodayClassAdapter.TodayClassPopUpTypes type, TodayClassItemResponse item) {
        try {
            if (type.equals(TodayClassAdapter.TodayClassPopUpTypes.LIVE_UPCOMING)) {
                if (item != null && item.getChapterLiveDto() != null && item.getChapterLiveDto().getUpcomingStatusPopUp() != null) {
                    showLiveUpcomingDialog(item.getChapterLiveDto().getUpcomingStatusPopUp(),item.getChapterStatus());
                }
            } else if (type.equals(TodayClassAdapter.TodayClassPopUpTypes.LOCK_POPUP)) {
                if (item != null && item.isLocked()) {
                  Intent intent=SubscriptionNavigationHelper.getSubscriptionPageIntent(requireContext(), SubscriptionPopupType.LECTURES.name(), RedirectionSource.LIVE_LOCKED_POPUP);
                  startActivity(intent);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onTodayClassClick(TodayClassItemResponse item, int position) {

        try {
            // Notify listener if this card was the animated one
            String itemTag = item.getItemTag();
            if (todayClassAdapter != null && itemTag != null
                    && itemTag.equals(todayClassAdapter.animatedItemTag)
                    && todayTaskListener != null) {
                todayClassAdapter.clearAnimatedItemTag();
                todayTaskListener.onAnimatedCardTagClicked(itemTag);
            }

            if (item.isLocked()) {
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.lock_popup_clicked.getActionName());
                navigateToSubscriptionLockScreen(item.getSubscriptionLockPopupDto());
            } else if (TodayClassType.DPP_LIVE.name().equals(item.getType())) {
                if (DppStatus.UPCOMING.equals(DppStatus.fromString(item.getDppStatus()))) {
                    if (item.getSubscriptionLockPopupDto() != null) {
                        showDppUpcomingDialog(item.getSubscriptionLockPopupDto());
                    }
                } else if (!item.isZoomEnabled() && StringUtils.isNotEmpty(item.getMeetLink())) {
                    CommonNavigator.openGoogleMeet(requireContext(), item.getMeetLink());
                } else {
                    CommonNavigator.navigateToDppGuidelines(requireContext(),
                            item.getDppBatchId(), item.getSubjectId(), item.getTitle());
                }
                Map<String, String> params = new HashMap<>();
                params.put("cardType", "dpp_live");
                params.put("dppBatchId", item.getDppBatchId());
                params.put("subjectId", item.getSubjectId());
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.todays_task_card_clicked.getActionName(), params);
            } else if (TodayClassType.DPP_PRACTICE.name().equals(item.getType())) {
                CommonNavigator.navigateToDppPractice(requireContext(),
                        item.getDppBatchId(), item.getSubjectId(), item.getTitle());
                Map<String, String> params = new HashMap<>();
                params.put("cardType", "dpp_practice");
                params.put("dppBatchId", item.getDppBatchId());
                params.put("subjectId", item.getSubjectId());
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.todays_task_card_clicked.getActionName(), params);
            } else {
                Intent intent = new Intent(requireContext().getApplicationContext(),
                        MicrolectureListActivity.class);
                intent.putExtra("chapterId", item.getChapterId());
                intent.putExtra("chapterName", item.getChapterName());
                intent.putExtra("hideTapTarget", true);
                intent.putExtra("microLectureId", item.getMicroLectureId());
                intent.putExtra("openVideo", true);
                Map<String, String> params = new HashMap<>();
                params.put("parametersDetail", String.valueOf(intent));
                params.put("cardType", "lecture");
                params.put("demo_lecture", String.valueOf(item.isDemoLecture()));
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.todays_task_card_clicked.getActionName(), params);
                requireContext().startActivity(intent);

                if (item.isDemoLecture()) {
                    MyApplication.saveSharedPreferencesDoNotDelete(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()) + "_" + item.getMicroLectureId(), true);
                    setDemoVideoOpen(item.getChapterCode(), item.getChapterId());
                    HashMap<String, String> eventParams = new HashMap<>();
                    eventParams.put("chapter_code", item.getChapterCode());
                    eventParams.put("chapter_id", item.getChapterId());
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.demo_lecture_clicked.getActionName(), eventParams);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setAnimatedItemTag(String tag) {
//        Log.d(TAG, "---> Setting animated tag: "+tag);
        if (todayClassAdapter != null) {
            todayClassAdapter.setAnimatedItemTag(tag);
        }
    }

    public void clearAnimatedItemTag() {
        if (todayClassAdapter != null) {
            todayClassAdapter.clearAnimatedItemTag();
        }
    }

    public String getAnimatedItemTag() {
        return todayClassAdapter != null ? todayClassAdapter.animatedItemTag : null;
    }

    /**
     * Returns true if the current today-class list contains a CONTINUE_LECTURE item with the given tag.
     */
    public boolean hasItemWithTag(String tag) {
//        Log.d(TAG, "Finding Today Task tag : "+tag);
        if (tag == null || todayClassAdapter == null) return false;
        for (TodayClassItemResponse item : todayClassAdapter.todayClasses) {
//            Log.d(TAG, "---> TASK TAG : "+item.getItemTag()+" | "+item.getPosition());
            if (tag.equals(item.getItemTag()) && (item.getPosition() == 0 || item.getPosition() == 1)) return true;
        }
        return false;
    }

    /**
     * Returns true if the batch overview video card is present and currently showing its
     * border animation (i.e. not yet watched and has no progress).
     * Used by MainDashboard to prevent starting a sequence animation on another section
     * while the batch overview animation is already visible.
     */
    /**
     * Returns true once the today-class API (or cache) has delivered data to the adapter.
     */
    public boolean isTodayClassDataLoaded() {
        return todayClassAdapter != null;
    }

    public boolean isBatchOverviewAnimating() {
        try {
            if (todayClassAdapter == null || todayClassAdapter.todayClasses == null) return false;
            for (TodayClassItemResponse item : todayClassAdapter.todayClasses) {
                if (!item.isBatchOverViewLecture()) continue;
                String key = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey())
                        + "_batch_overview_" + item.getMicroLectureId();
                boolean watched = MyApplication.getBooleanDataFromDoNotDeleteSharedPreference(key);
                boolean hasProgress = item.getProgress() != null && item.getProgress() > 0;
                return !watched && !hasProgress;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onApiCompleted() {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                Log.d("Live session API completed", "onApiCompleted: ");
                getTodayClassData();
            });
        }
    }

    public interface TodayTaskListener {
        void hideContinueSection();

        void hideBadgeView();

        void onAnimatedCardTagClicked(String tag);

        /**
         * Called when today-class data has been delivered (from cache or API).
         * @param source the TodayTaskFragment instance that loaded the data,
         *               so the host can detect stale callbacks from replaced fragments.
         */
        void onDataFetched(TodayTaskFragment source, int size);
    }
}