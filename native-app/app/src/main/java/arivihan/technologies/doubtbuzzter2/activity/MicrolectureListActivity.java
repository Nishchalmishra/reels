package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_MICROLECTURE_QUESTION_OPTIONS;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.MICRO_LECTURE_LIST;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.MICRO_LECTURE_PRACTICE_LIST_BY_TYPE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.MICRO_LECTURE_SELECTION;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.POST_FEEDBACK_LIKE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.PUT_UPDATE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.SAVE_DOWNLOAD;
import static arivihan.technologies.doubtbuzzter2.enums.HOW_TO_TYPE.QUESTION_TYPE_PRACTICE_LIST;

import android.Manifest;
import android.animation.Animator;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.activity.OnBackPressedCallback;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.RenderersFactory;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mancj.slimchart.SlimChart;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.enums.AnalyticsEvent;
import arivihan.technologies.doubtbuzzter2.enums.SubscriptionPopupType;
import arivihan.technologies.doubtbuzzter2.helper.ActivityLauncher;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.microlecture.CommentsBottomSheet;
import arivihan.technologies.doubtbuzzter2.activity.microlecture.CommentsViewModel;
import arivihan.technologies.doubtbuzzter2.activity.web_view.MicrolecturePractiseWebViewActivity;
import arivihan.technologies.doubtbuzzter2.adapter.DownloadVideoQualityAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.MicrolectureAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.PracticeAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonAPIs;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.PlayStoreDialog;
import arivihan.technologies.doubtbuzzter2.constants.QuestionLevel;
import arivihan.technologies.doubtbuzzter2.customView.CancelDownloadBottomSheet;
import arivihan.technologies.doubtbuzzter2.customView.HowToVideoView;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.LikeDislikeEnum;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.CustomFullScreenLoader;
import arivihan.technologies.doubtbuzzter2.helper.ScreenAnalyticsHelper;
import arivihan.technologies.doubtbuzzter2.helper.UserNameValidationConfig;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.listener.SnackbarMessageListener;
import arivihan.technologies.doubtbuzzter2.listener.SubscriptionCallback;
import arivihan.technologies.doubtbuzzter2.listener.feedback.OnFeedbackStatusAPIListener;
import arivihan.technologies.doubtbuzzter2.manager.DemoUtil;
import arivihan.technologies.doubtbuzzter2.manager.DownloadTracker;
import arivihan.technologies.doubtbuzzter2.model.DownloadStatus;
import arivihan.technologies.doubtbuzzter2.model.FactoryShipQnADto;
import arivihan.technologies.doubtbuzzter2.model.InteractiveQuestionResponseDto;
import arivihan.technologies.doubtbuzzter2.model.LectureContentType;
import arivihan.technologies.doubtbuzzter2.model.MediaEntity;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureContentDto;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureDetailsDto;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureDto;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureEntity;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureQuestionAnsResponseDto;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureQuestionsAnsDto;
import arivihan.technologies.doubtbuzzter2.model.NotesDto;
import arivihan.technologies.doubtbuzzter2.model.OfflineDownloadEntity;
import arivihan.technologies.doubtbuzzter2.model.PracticeStatus;
import arivihan.technologies.doubtbuzzter2.model.Questions;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionCheckDto;
import arivihan.technologies.doubtbuzzter2.model.UserDto;
import arivihan.technologies.doubtbuzzter2.model.VideoDetails;
import arivihan.technologies.doubtbuzzter2.model.VideoPreview;
import arivihan.technologies.doubtbuzzter2.model.VideoQualityDto;
import arivihan.technologies.doubtbuzzter2.model.feedback.AppFeedbackStatusModel;
import arivihan.technologies.doubtbuzzter2.model.microlecture.LikeDislikeResponse;
import arivihan.technologies.doubtbuzzter2.model.microlecture.MicrolectureMetadataModel;
import arivihan.technologies.doubtbuzzter2.model.offline.notes.NoteEntity;
import arivihan.technologies.doubtbuzzter2.utils.PdfUtils;
import arivihan.technologies.doubtbuzzter2.viewModel.MediaViewModel;
import arivihan.technologies.doubtbuzzter2.viewModel.MicroLectureViewModel;
import arivihan.technologies.doubtbuzzter2.viewModel.NotesViewModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class MicrolectureListActivity extends LocaleActivity implements Player.Listener, MicrolectureAdapter.MicroLectureListener,
        PracticeAdapter.PracticeListener, CancelDownloadBottomSheet.CancelDownloadBottomSheetListener, SnackbarMessageListener {

    private static final String TAG = MicrolectureListActivity.class.getSimpleName();
    private static final String LIKE_DISLIKE_VOLLEY_TAG = "LIKE_DISLIKE_TAG";
    private static final String COMMENT_VOLLEY_TAG = "LIKE_DISLIKE_TAG";
    private static final String DOWNLOAD_LECTURE_TAG = "DOWNLOAD_LECTURE";
    private static final int DEFAULT_INITIAL_SAMPLE = 15;
    private static final int DEFAULT_MAX_SAMPLE = 80;
    private static final double DEFAULT_CV_THRESHOLD = 0.15;
    private static final int DEFAULT_CONCURRENCY = 10;
    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 5000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 5000;
    String chapterId, qnsId;
    String chapterName;
    MicroLectureDetailsDto microLectureDetailsDto;
    ArrayList<MicroLectureDto> list = new ArrayList<>();
    List<VideoQualityDto> videoQualityDtoList;
    LinearLayout toolbar;
    Gson gson;
    private PlayerView previewVideoPlayer;
    private ImageView previewThumbnail;
    ConstraintLayout previewLayout;
    private long videoStartingTime;
    private long videoEndingTime;
    String videoUrl;
    ExoPlayer player;
    Handler mHandler = new Handler();
    Handler playerHandler;
    Runnable mRunnable;
    Runnable playerRunnable;
    RecyclerView recyclerView, videoDownloadQuality;
    MicrolectureAdapter microlectureAdapter;
    ImageView centerImage;
    LinearLayout shimmerLayout;
    LinearLayout chapterProgress;
    FrameLayout fl_scroll_view;
    CardView cv_scroll_toggle;
    TextView tv_scroll_toggle;
    ImageView iv_scroll_toggle;
    private boolean scrolledToBottom = false;
    private boolean hideScrollToggle = false;
    NestedScrollView scrollView;
    ImageView volume;
    boolean muted = true;
    SlimChart slimChart;
    TextView progressText;
    TextView title;
    TextView activityTitle;
    TextView chapterTitle;
    TextView lastWatched;
    ArrayList<FactoryShipQnADto> qnADtoList;
    int firstUnattemptedIndex = 0;
    private final int your_reques_code = 1001;
    ProgressBar spinnerVideoDetails;
    Dialog subscriptionDialog;
    MaterialCardView viewPlans;
    ImageView cancel;

    boolean dataLoaded;
    ConstraintLayout ParentLayout;
    MicroLectureDto previewDto;
    ConstraintLayout chapterTitleLayout;
    LinearLayout backButtonLayout;
    CardView cv_ncert_solutions, cv_like_dislike_card, cv_comment, cv_download;
    LinearLayout notesLayout;
    ImageView iv_like, iv_dislike;
    LottieAnimationView lottie_like, lottie_dislike;
    TextView tv_like_count;
    LinearLayout share_layout;
    Uri deepLinkData;
    String notesUrl, ncertSolutions;
    boolean previewCompleted;
    CustomFullScreenLoader screenLoader;
    boolean playerInitialized;
    String microlectureId;
    String microlectureName;
    String questionType;
    boolean openVideo, openNote, isBatchOverview;
    private long skipToPositionMs = -1;
    boolean openPractice;
    Dialog actionDialog, videoDownloadDialog, deviceMemoryReached, noLecturedialog;
    boolean reattempt;
    boolean review;
    ConstraintLayout continuityLayout;
    CardView practiceProblems;
    LinearLayout skipToNextLecture;
    int currentPos;
    int currentQuesPos;
    boolean isLocked;
    boolean previewIsPractice;
    boolean onCreateCalled;
    String subjectCode, selectedSubject;
    boolean highlightQuestion, preExamPreparation;
    DownloadVideoQualityAdapter videoQualityAdapter;
    private MicroLectureContentDto microLectureContentDto;
    PlayStoreDialog playStoreDialog;
    public static final ImmutableList<Integer> SUPPORTED_TRACK_TYPES =
            ImmutableList.of(C.TRACK_TYPE_VIDEO, C.TRACK_TYPE_AUDIO, C.TRACK_TYPE_TEXT);
    @UnstableApi
    private DownloadTracker downloadTracker;
    private MediaItem mediaItem;
    private MediaViewModel mediaViewModel;
    private NotesViewModel notesViewModel;
    private MicroLectureViewModel microLectureViewModel;
    private MicroLectureEntity selectionMicroLectureEntity;
    private LinearLayout progressLayout, contentLayout;
    private ImageView close;
    private ExecutorService executorService;
    public static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 101;
    private LiveData<MediaEntity> mediaEntityLive;
    private LinearLayout downloadShimmerLayout;
    private ShimmerFrameLayout downloadShimmer;
    private TextView downloadDataFailed;
    LottieAnimationView internetSlow;
    private MicroLectureDto downloadDto;
    private VideoQualityDto currentDownloadVideoQuality;
    private List<VideoQualityDto> downloadVideoQualityDtos = new ArrayList<>();
    private CancelDownloadBottomSheet cancelDownloadBottomSheet;
    private LiveData<MediaEntity> cancelDownloadLiveData;
    private int cancelDownloadPosition;
    private MediaEntity cancelDownloadEntity;
    private ImageView download;
    private ImageView download_completed;
    private LinearLayout download_progress_layout;
    private ProgressBar download_progress_bar;
    private TextView download_progress_text;
    private TextView download_status_text;
    private LiveData<MediaEntity> previewDownloadLiveData;
    private MediaEntity previewDownloadEntity;
    private int previewPosition;
    private CardView how_to_card;
    private HowToVideoView howToVideoView;
    private View how_to_video_view;
    TextView continuityLayoutText;
    boolean returningFromPdfViewer = false;
    NetworkStatusLiveData networkStatusLiveData;
    private MicrolectureMetadataModel metadataModel;
    private long previewVideoBufferingStartTime = -1;
    private long BUFFERING_THRESHOLD_MS = 5000; // 5 seconds
    private Handler bufferHandler = new Handler(Looper.getMainLooper());
    private Runnable checkBufferingRunnable;
    private boolean hasPreviewPlaybackStarted = false;
    TextView lockDialogText, lockDialogBtnText, lockDialogTitle;
    LottieAnimationView lockAnimation;
    private Map<String, Object> activityParamsMap;
    SubscriptionCheckDto subscriptionCheckDto;
    private long completedLectureLimitForReviewPopUp = 5;
    private boolean isCommentDialogOpen = false;
//    boolean resetCommentViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_microlecture_list);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (howToVideoView != null && howToVideoView.videoView != null
                        && howToVideoView.videoView.getVisibility() == View.VISIBLE && howToVideoView.expanded) {
                    howToVideoView.resize();
                } else {
                    destroyHowTo();
                    finish();
                    overridePendingTransition(0, 0);
                }
            }
        });

        executorService = Executors.newFixedThreadPool(5);

        String videoMetadataController = MyApplication.getFirebaseRemoteConfig().getString("microlecture_video_metadata_controller");
        completedLectureLimitForReviewPopUp = MyApplication.getFirebaseRemoteConfig().getLong("ml_app_review_lecture_count");
        Gson gson = new Gson();
        metadataModel = gson.fromJson(videoMetadataController, MicrolectureMetadataModel.class);
        if (metadataModel == null) {
            metadataModel = new MicrolectureMetadataModel(300000, 300000, false);
        }
        screenLoader = new CustomFullScreenLoader(this);
        BUFFERING_THRESHOLD_MS = metadataModel.getPreview_buffer_threshold_ms() != 0 ? metadataModel.getPreview_buffer_threshold_ms() : 4000;
        hideScrollToggle = MyApplication.getFirebaseRemoteConfig().getBoolean("micro_lecture_scroll_toggle_card_hide");
        initializeRequest();
        setUpVolume();
        initializeMemoryLimit();
        setUpRecyclerView();
        createNewRequest();
        setUpDialog();
        MyApplication.getSubscriptionStatus(this, new SubscriptionCallback() {
            @Override
            public void onSuccess(SubscriptionCheckDto dto) {
                Log.d(TAG, "onSuccess API call: " + dto);
                subscriptionCheckDto = dto;
                updateDialogContent(subscriptionCheckDto);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("API_ERROR", errorMessage);
                updateDialogContent(null); // or show some fallback text
            }
        });

        setUpActionPopup();
        onCreateCalled = true;
        setUpSelectionViewModel();
        getMicroLectureList();
        Log.d("DownloadStatus", "on Create: ");
        MyApplication.saveData(PreferenceKeys.SHOW_MATCH_THESE_DIALOG.getKey(), true);
        initializeVideoDownloadDialog();
        if (howToVideoView == null) {
            howToVideoView = HowToVideoView.getInstance(this);
            howToVideoView.setView(this, how_to_video_view);
            int count = MyApplication.getIntData(QUESTION_TYPE_PRACTICE_LIST.name() + "_COUNT" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
            Log.d("debugcount", "getHowToVideoType: " + count + "type" + QUESTION_TYPE_PRACTICE_LIST.name());
            if (count < 0)
                count = 0;
            if (count <= 1) {
                howToVideoView.setType(QUESTION_TYPE_PRACTICE_LIST, false);
                howToVideoView.setButtonText(getString(R.string.Watch_Now));
                MyApplication.saveData(QUESTION_TYPE_PRACTICE_LIST.name() + "_COUNT" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()), count + 1);
            }
        }

        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            if (isOnline) {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    CommonMethods.showPositiveSnackbar(ParentLayout, getString(R.string.InternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
                    if (!dataLoaded) {
                        createNewRequest();
                        getMicroLectureList();
                    } else {
                        setUpPreviewVideo();
                    }
                }
            } else {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                    CommonMethods.showNegativeSnackbar(ParentLayout, getString(R.string.NoInternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), false);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), true);
                }
            }
        });

        ScreenAnalyticsHelper.sendScreenVisitedEvent(AnalyticsEvent.MICRO_LECTURE_SCREEN.name(), AnalyticsEvent.MICRO_LECTURE_SCREEN.name());

    }

    private void setUpSelectionViewModel() {
        microLectureViewModel = MyApplication.getInstance().getMicroLectureViewModel();
        microLectureViewModel.getSelectionById(chapterId).observe(this, microLecture -> {
            selectionMicroLectureEntity = microLecture;
            microlectureAdapter.setSelectionMicroLectureEntity(selectionMicroLectureEntity);
        });
    }

    private void setUpDialog() {
        subscriptionDialog = new Dialog(this);
        subscriptionDialog.setContentView(R.layout.custom_dialog_lock_box);
        subscriptionDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        subscriptionDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subscriptionDialog.setCancelable(false);
        lockDialogText = subscriptionDialog.findViewById(R.id.header);
        lockDialogTitle = subscriptionDialog.findViewById(R.id.dialog_title);
        lockAnimation = subscriptionDialog.findViewById(R.id.dialog_animation);
        lockDialogBtnText = subscriptionDialog.findViewById(R.id.subs);

        cancel = subscriptionDialog.findViewById(R.id.cancel);
        cancel.setOnClickListener(v -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.subsc_dialog_cancel_btn_click.getActionName());
            subscriptionDialog.dismiss();
        });
        viewPlans = subscriptionDialog.findViewById(R.id.subscriptionLayout);
        viewPlans.setOnClickListener(view -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.subsc_dialog_view_plan_click.getActionName());
            handleSubscriptionClick(subscriptionCheckDto);
        });
    }

    private void updateDialogContent(SubscriptionCheckDto dto) {
        if (dto != null && !MyApplication.getFirebaseRemoteConfig().
                getBoolean("to_show_lock_msg_from_data_side_in_micro_lecture")) {
            if (dto.getDialogText() != null) lockDialogText.setText(dto.getDialogText());
            if (dto.getButtonText() != null) lockDialogBtnText.setText(dto.getButtonText());
            if (StringUtils.isNotEmpty(dto.getDialogTitle())) {
                lockDialogTitle.setVisibility(View.VISIBLE);
                lockDialogTitle.setText(dto.getDialogTitle());
            }

            activityParamsMap = dto.getActivityParams();
            if (activityParamsMap != null && activityParamsMap.containsKey("navigate_to")) {
                Object navigateToValue = activityParamsMap.get("navigate_to");
                if ("home_screen".equalsIgnoreCase(String.valueOf(navigateToValue))) {
                    lockAnimation.setAnimation(R.raw.alert);
                }
            }
        }
        viewPlans.setOnClickListener(view -> handleSubscriptionClick(dto));
    }


    private void handleSubscriptionClick(SubscriptionCheckDto dto) {

        closeDialog();
        Intent intent = null;

        if (dto != null && dto.getClassName() != null && !dto.getClassName().isEmpty()) {
            try {
                Class<?> targetClass = ActivityLauncher.resolveClass(dto.getClassName());
                intent = new Intent(MicrolectureListActivity.this, targetClass);
                Log.d("SubscriptionClick", "Intent created for class: " + targetClass.getSimpleName());
            } catch (ClassNotFoundException e) {
                Log.e("SubscriptionClick", "Class not found: " + dto.getClassName(), e);
            }
        }

        if (intent == null) {
            Log.w("SubscriptionClick", "Intent is null. Falling back to default activity...");
//            if (preExamPreparation) {
//                intent = new Intent(MicrolectureListActivity.this, CampaignSubscriptionActivity.class);
////                        .putExtra("preExamPreparation", true);
//            } else {
//                intent = new Intent(MicrolectureListActivity.this, CampaignSubscriptionActivity.class);
////                        .putExtra("plan_level", "COURSE")
////                        .putExtra("fromUnlockText", true);
//            }
            intent = SubscriptionNavigationHelper.getSubscriptionPageIntent(this, RedirectionSource.MICRO_LECTURE_LOCKED_POPUP);
        }

        // Log and attach extras from activityParamsMap
        if (activityParamsMap != null) {
            Log.d("SubscriptionClick", "Adding extras from activityParamsMap: " + activityParamsMap.size() + " entries");
            for (Map.Entry<String, Object> entry : activityParamsMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof Serializable) {
                    intent.putExtra(key, (Serializable) value);
                } else {
                    Log.d("SubscriptionClick", "Value for key '" + key + "' is not Serializable: " + value);
                }
            }
        } else {
            Log.w("SubscriptionClick", "activityParamsMap is null");
        }

        if (activityParamsMap != null && activityParamsMap.containsKey("navigate_to")) {
            Object navigateToValue = activityParamsMap.get("navigate_to");
            Log.d("SubscriptionClick", "navigate_to: " + navigateToValue);
            if ("home_screen".equalsIgnoreCase(String.valueOf(navigateToValue))) {
                MyApplication.saveData(PreferenceKeys.NAVIGATE_TO_HOME_SCREEN.getKey(), true);
            }
        }
        viewPlans.setOnClickListener(view -> {
            subscriptionDialog.cancel();
            subscriptionDialog.dismiss();

//            Intent clickIntent;
//            if (preExamPreparation) {
//                clickIntent = new Intent(this, CampaignSubscriptionActivity.class);
//            } else {
//                clickIntent = new Intent(this, CampaignSubscriptionActivity.class);
////                clickIntent.putExtra("plan_level", "COURSE");
//            }
//            startActivity(clickIntent);
            SubscriptionNavigationHelper.navigateToSubscriptionPage(this, RedirectionSource.MICRO_LECTURE_JOIN);
        });

        if (intent != null) {
            Log.i("SubscriptionClick", "Starting activity: " + intent.getComponent());
            startActivity(intent);
        } else {
            Log.e("SubscriptionClick", "Intent is null. Cannot start activity!");
        }
    }


    private void closeDialog() {
        if (subscriptionDialog != null && subscriptionDialog.isShowing()) {
            subscriptionDialog.dismiss();
        }
    }

    private void getDownloadsVideoData(List<MediaEntity> mediaList) {
        try {
            AtomicReference<HashMap<String, Object>> map = new AtomicReference<>();
            map.set(new HashMap<>());
            for (MediaEntity item : mediaList) {
                map.get().put(item.getMicroLectureId(), item);
            }
            for (int i = 0; i < list.size(); i++) {
                if (!list.get(i).isLocked()) {
                    if (map.get().containsKey(list.get(i).getId())) {
                        MediaEntity mediaEntity = (MediaEntity) map.get().get(list.get(i).getId());
                        if (mediaEntity != null &&
                                StringUtils.isNotEmpty(mediaEntity.getStatus())) {
                            list.get(i).setDownloadStatus(mediaEntity.getStatus());
                            list.get(i).setDownloadProgress(mediaEntity.getProgress());
                        }
                    } else {
                        list.get(i).setDownloadStatus("");
                        list.get(i).setDownloadProgress(0);
                    }
                }
            }
            microlectureAdapter.notifyDataSetChanged();
            scrollView.setVisibility(View.VISIBLE);
            fl_scroll_view.setVisibility(View.VISIBLE);
            shimmerLayout.setVisibility(View.GONE);
            MyApplication.setMap("MY_DOWNLOADS", map.get());
        } catch (Exception ignored) {
        }
    }

    private void syncNoteListWithOffline(List<NoteEntity> noteEntityList) {
        try {
            Map<String, NoteEntity> noteMap = new HashMap<>();
            for (NoteEntity entity : noteEntityList) {
                noteMap.put(entity.getId(), entity);
            }

            // Sync data
            for (MicroLectureDto model : list) {
                NoteEntity match = noteMap.get(model.getId());
                if (match != null) {
//                    model.setNotesUrl(match.getNotesUrl());
//                    model.setLocked(match.isLocked());
                    model.setPdfDownloadComplete(match.getStatus().equals(NoteEntity.STATUS_COMPLETED));
                    model.setPdfProgress(match.getProgress());
                    model.setPdfStatus(match.getStatus());
                }
            }

//            if (notesAdapter != null) {
//                notesAdapter.notifyDataSetChanged();
//            } else {
//                viewPagerAdapter.updateDataList(this.noteEntityList);
//            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void getDownloadedVideoData(List<MediaEntity> mediaList) {
        AtomicReference<HashMap<String, Object>> map = new AtomicReference<>(MyApplication.getMap("MY_DOWNLOADS"));
        if (map.get() == null || map.get().isEmpty()) {
            if (map.get() == null) {
                map.set(new HashMap<>());
            }
            for (MediaEntity item : mediaList) {
                map.get().put(item.getMicroLectureId(), item.getMicroLectureId());
            }
            for (int i = 0; i < list.size(); i++) {
                if (!list.get(i).isLocked() && map.get().containsKey(list.get(i).getId()))
                    list.get(i).setDownloaded(true);
            }
            microlectureAdapter.notifyDataSetChanged();
            MyApplication.setMap("MY_DOWNLOADS", map.get());
        }
    }

    private void setUpVolume() {
        volume.setOnClickListener(v -> {
            if (player != null) {
                if (muted) {
                    volume.setImageDrawable(getResources().getDrawable(R.drawable.volume));
                    player.setVolume(1f);
                } else {
                    volume.setImageDrawable(getResources().getDrawable(R.drawable.mute));
                    player.setVolume(0f);
                }
            }
            muted = !muted;
        });
    }

    private void initializeRequest() {
        deepLinkData = getIntent().getData();
        if (deepLinkData != null) {
            chapterId = deepLinkData.getQueryParameter("chapterId");
            chapterName = deepLinkData.getQueryParameter("chapterName");

            if (deepLinkData.getQueryParameter("microLectureId") != null) {
                microlectureId = deepLinkData.getQueryParameter("microLectureId");
                openVideo = deepLinkData.getBooleanQueryParameter("openVideo", false);
            }

            if (chapterName != null) {
                chapterName.replace("+", " ");
            }

            selectedSubject = deepLinkData.getQueryParameter("selectedSubject");
            subjectCode = deepLinkData.getQueryParameter("subjectCode");
            qnsId = deepLinkData.getQueryParameter("qnsId");
            openPractice = deepLinkData.getBooleanQueryParameter("openPractice", false);
            highlightQuestion = deepLinkData.getBooleanQueryParameter("highlightQuestion", false);
            preExamPreparation = deepLinkData.getBooleanQueryParameter("preExamPreparation", false);
            openNote = deepLinkData.getBooleanQueryParameter("openNotes", false);
            isBatchOverview = deepLinkData.getBooleanQueryParameter("isBatchOverview", false);
            skipToPositionMs = getIntent().getLongExtra("skipToPosition", -1);
        } else {
            qnsId = getIntent().getStringExtra("qnsId");
            microlectureId = getIntent().getStringExtra("microLectureId");
            openPractice = getIntent().getBooleanExtra("openPractice", false);
            openVideo = getIntent().getBooleanExtra("openVideo", false);
            chapterId = getIntent().getStringExtra("chapterId");
            chapterName = getIntent().getStringExtra("chapterName");

            selectedSubject = getIntent().getStringExtra("selectedSubject");
            subjectCode = getIntent().getStringExtra("subjectCode");
            highlightQuestion = getIntent().getBooleanExtra("highlightQuestion", false);
            preExamPreparation = getIntent().getBooleanExtra("preExamPreparation", false);
            openNote = getIntent().getBooleanExtra("openNotes", false);
            isBatchOverview = getIntent().getBooleanExtra("isBatchOverview", false);
            skipToPositionMs = getIntent().getLongExtra("skipToPosition", -1);

            Log.d(TAG, "---> ChapterId: " + chapterId + ", ChapterName: " + chapterName);
        }
        gson = new Gson();
        microLectureDetailsDto = new MicroLectureDetailsDto();
        cv_download = findViewById(R.id.cv_download);
        notesLayout = findViewById(R.id.chapter_notes);
        cv_download.setOnClickListener(view -> handlePreviewDownloadClick());
        how_to_video_view = findViewById(R.id.how_to_video_view);
        how_to_card = findViewById(R.id.how_to_card);
        if (MyApplication.getFirebaseRemoteConfig().getBoolean("hide_how_to_card_lecture") ||
                MyApplication.getFirebaseRemoteConfig().getBoolean("hideHowToVideos"))
            how_to_card.setVisibility(View.GONE);
        how_to_card.setOnClickListener(view -> {
            logFirebaseEvent();
            howToVideoView = HowToVideoView.getInstance(this);
            howToVideoView.setView(this, how_to_video_view);
            howToVideoView.setType(QUESTION_TYPE_PRACTICE_LIST, true);
            howToVideoView.setButtonText(getString(R.string.Watch_Now));
        });

        fl_scroll_view = findViewById(R.id.fl_scroll_view);
        cv_scroll_toggle = findViewById(R.id.cv_scroll_toggle);
        tv_scroll_toggle = findViewById(R.id.tv_scroll_toggle);
        iv_scroll_toggle = findViewById(R.id.iv_scroll_toggle);

        download = findViewById(R.id.download);
        download_completed = findViewById(R.id.download_completed);
        download_progress_layout = findViewById(R.id.download_progress_layout);
        download_progress_bar = findViewById(R.id.download_progress_bar);
        download_progress_text = findViewById(R.id.download_progress_text);
        download_status_text = findViewById(R.id.download_status_text);
        chapterTitle = findViewById(R.id.chapter_title);
        activityTitle = findViewById(R.id.activity_title);
        chapterTitle.setText(chapterName);
        activityTitle.setText(chapterName);
        lastWatched = findViewById(R.id.last_watched_lecture_title);
        scrollView = findViewById(R.id.scroll_view);
        chapterProgress = findViewById(R.id.chapter_progress_layout);
        shimmerLayout = findViewById(R.id.shimmer_layout);
        toolbar = findViewById(R.id.toolbar);
        previewVideoPlayer = findViewById(R.id.preview_video);
        recyclerView = findViewById(R.id.recycler_view);
        previewLayout = findViewById(R.id.preview_layout);
        centerImage = findViewById(R.id.center_image);
        previewThumbnail = findViewById(R.id.preview_thumbnail);
        previewLayout = findViewById(R.id.preview_layout);
        slimChart = findViewById(R.id.chapter_progress);
        progressText = findViewById(R.id.progress_text);
        volume = findViewById(R.id.volume);
        spinnerVideoDetails = findViewById(R.id.spinnerVideoDetails);
        ParentLayout = findViewById(R.id.parent_layout);

        chapterTitleLayout = findViewById(R.id.chapter_title_layout);
        backButtonLayout = findViewById(R.id.back_button_layout);
        backButtonLayout.setOnClickListener(v -> {
            Map<String, String> params = new HashMap<>();
            params.put("microlectureId:",microlectureId);
            params.put("microlectureName:",chapterName);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.micro_lec_chap_bck_btn_click.getActionName(),params);
            getOnBackPressedDispatcher().onBackPressed();
        });
        cv_ncert_solutions = findViewById(R.id.cv_notes);
        cv_ncert_solutions.setVisibility(View.GONE);
        cv_ncert_solutions.setOnClickListener(v -> {
            if (microLectureDetailsDto != null) {
                openPdf(ncertSolutions, microLectureDetailsDto.getChapterDetails().getTitle());
            }
        });
        share_layout = findViewById(R.id.share_layout);
        notesLayout.setOnClickListener(view -> {
            if (microLectureDetailsDto != null) {
                openPdf(notesUrl, microLectureDetailsDto.getChapterDetails().getTitle());
            }
        });
//        notesLayout.setOnClickListener(view -> openPdf(notesUrl));
        share_layout.setOnClickListener(v -> {
            share_layout.setEnabled(false);
            openLink();
            share_layout.postDelayed(() -> share_layout.setEnabled(true), 1500);
        });
        continuityLayout = findViewById(R.id.continuity_layout);
        continuityLayoutText = findViewById(R.id.lecture_content_type_Text);
        practiceProblems = findViewById(R.id.practice_problems);
        practiceProblems.setOnClickListener(view -> {
            openPractice();
        });
        skipToNextLecture = findViewById(R.id.skip_to_lecture);
        skipToNextLecture.setOnClickListener(view -> {
            Log.d(TAG, "initializeRequest Clicked skip to next lecture: ");
            startVideo(currentPos + 1);
        });

        cv_scroll_toggle.setOnClickListener(v -> {
            try {
                if (!scrolledToBottom) {
                    scrollView.post(() -> scrollView.smoothScrollTo(0, scrollView.getChildAt(0).getHeight()));
                } else {
                    scrollView.post(() -> scrollView.smoothScrollTo(0, 0));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            updateScrollCard();
        });

        cv_like_dislike_card = findViewById(R.id.cv_like_dislike_card);
        cv_comment = findViewById(R.id.cv_comment);
        iv_like = findViewById(R.id.iv_like);
        iv_dislike = findViewById(R.id.iv_dislike);
        lottie_like = findViewById(R.id.lottie_like);
        lottie_dislike = findViewById(R.id.lottie_dislike);
        tv_like_count = findViewById(R.id.tv_like_count);

        lottie_like.setVisibility(View.GONE);
        lottie_dislike.setVisibility(View.GONE);

        lottie_like.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {
                iv_like.setEnabled(false);
                iv_like.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                clearSelectLikeDislike(true, true);
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {
            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {
            }
        });

        lottie_dislike.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {
                iv_dislike.setEnabled(false);
                iv_dislike.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                clearSelectLikeDislike(false, true);
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {
            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {
            }
        });

        iv_like.setOnClickListener(imageView -> {
            if (!previewDto.isLocked()) {
                LikeDislikeEnum feedback;
                if (previewDto.getLiked() == null || !previewDto.getLiked()) {
                    feedback = LikeDislikeEnum.LIKE;
                    setLikeDislike(true);
                } else {
                    feedback = LikeDislikeEnum.NOTHING;
                    clearSelectLikeDislike(true, false);
                }

                setMicroLectureLikeDislike(feedback, chapterId, previewDto.getId());

                // Firebase event
                Map<String, String> params = new HashMap<>();
                params.put("isLiked", feedback.name());
                params.put(PreferenceKeys.MICROLECTURE_ID.getKey(), previewDto.getId());
                params.put("chapterId", chapterId);
                params.put("screen", TAG);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.ml_click_like.name(), params);

            } else {
                subscriptionDialog.show();
            }
        });

        iv_dislike.setOnClickListener(imageView -> {
            if (!previewDto.isLocked()) {
                LikeDislikeEnum feedback;
                if (previewDto.getLiked() == null || previewDto.getLiked()) {
                    feedback = LikeDislikeEnum.DISLIKE;
                    setLikeDislike(false);
                } else {
                    feedback = LikeDislikeEnum.NOTHING;
                    clearSelectLikeDislike(false, false);
                }

                setMicroLectureLikeDislike(feedback, chapterId, previewDto.getId());

                // Firebase event
                Map<String, String> params = new HashMap<>();
                params.put("isLiked", feedback.name());
                params.put(PreferenceKeys.MICROLECTURE_ID.getKey(), previewDto.getId());
                params.put("chapterId", chapterId);
                params.put("screen", TAG);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.ml_click_dislike.name(), params);

            } else {
                subscriptionDialog.show();
            }
        });

        cv_comment.setOnClickListener(cardView -> {
            if (isCommentDialogOpen) return;

            if (!previewDto.isLocked()) {

                if (StringUtils.isEmpty(MyApplication.getStringData("name"))) {
                    showInputDialog(this);
                } else {
                    openCommentsDialog(cardView);
                }

            } else {
                subscriptionDialog.show();
            }
        });


    }

    private void showInputDialog(Context context) {
        if (context == null) return;
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_name_input);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        dialog.setCancelable(true);

        MaterialCardView cardView = dialog.findViewById(R.id.name_card);
        EditText editText = dialog.findViewById(R.id.user_name);
        editText.requestFocus();
        CardView submitCard = dialog.findViewById(R.id.next_card);
        TextView submit = dialog.findViewById(R.id.submit);
        TextView nameError = dialog.findViewById(R.id.name_error);

        //name config
        UserNameValidationConfig config = UserNameValidationConfig.fromRemoteConfig();
        updateUiState(context, config, cardView, editText, nameError, submitCard, submit, false);


        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateUiState(context, config, cardView, editText, nameError, submitCard, submit, true);
            }
        });

        submitCard.setOnClickListener(v -> {
            String input = editText.getText().toString().trim();
            updateUiState(context, config, cardView, editText, nameError, submitCard, submit, true);
            if (!submitCard.isEnabled()) return;

            try {
                Gson gson = new Gson();
                UserDto userDto = MyApplication.getUserDto("userDto");
                if (userDto == null) userDto = new UserDto();
                userDto.setUsername(input);

                MyJsonObjectRequest request = new MyJsonObjectRequest(
                        Request.Method.PATCH,
                        PUT_UPDATE,
                        new JSONObject(gson.toJson(userDto)),
                        response -> {
                            try {
                                if (Boolean.parseBoolean(response.get("success").toString())) {
                                    dialog.dismiss();
                                    MyApplication.saveData(PreferenceKeys.NAME.getKey(), input);
                                    MyApplication.saveData(PreferenceKeys.IS_NAME_UPDATED.getKey(), true);
                                    Map<String, String> map = new HashMap<>();
                                    map.put("source", "comments_section");
                                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.name_input_popup_submit_click.getActionName(), map);
                                    CommonMethods.showPositiveSnackbar(
                                            ParentLayout, getString(R.string.name_updated_mesaaage)
                                    );
                                    ParentLayout.postDelayed(() -> openCommentsDialog(cv_comment), 1500);
                                } else {
                                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "showInputDialog: ", e);
                            }
                        },
                        new VolleyGlobalErrorListener(context) {
                            @Override
                            public void postError(VolleyError error) {
                            }
                        }
                );

                MyVolleySingleton.getInstance(context).addToRequestQueue(request);

            } catch (JSONException e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }


    private void updateUiState(
            Context context,
            UserNameValidationConfig config,
            MaterialCardView cardView,
            EditText editText,
            TextView nameError,
            CardView buttonView,
            TextView buttonText,
            boolean showError
    ) {
        String input = editText.getText().toString().trim();
        String error = config.getValidationError(input, getString(R.string.name_required_error));
        boolean isValid = error == null;

        //error
        if (showError && !isValid) {
            nameError.setText(UserNameValidationConfig.fromHtml(error));
            nameError.setVisibility(View.VISIBLE);
            cardView.setStrokeColor(ContextCompat.getColor(context, R.color.input_name_stroke_error));
            cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.input_name_bg_error));
        }else {
            nameError.setVisibility(View.INVISIBLE);
            cardView.setStrokeColor(ContextCompat.getColor(context, R.color.input_name_stroke_default));
            cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.input_name_bg_default));
        }

        //btn
        buttonView.setEnabled(isValid);
        buttonView.setCardBackgroundColor(ContextCompat.getColor(context, isValid ? R.color.btn_enabled_bg   : R.color.btn_disabled_bg));
        buttonText.setTextColor(ContextCompat.getColor(context, isValid ? R.color.btn_enabled_text : R.color.btn_disabled_text));
    }

    private void openCommentsDialog(View cardView) {
        isCommentDialogOpen = true;
        cardView.setClickable(false);

        setCommentDialog();

        Map<String, String> params = new HashMap<>();
        params.put("opened", Boolean.TRUE.toString());
        params.put("microlectureId", previewDto.getId());
        params.put("chapterId", chapterId);
        params.put("screen", TAG);
        MyApplication.logFirebaseBundleCreation(
                FirebaseEvents.ml_click_comment.name(), params
        );
    }

    private void updateScrollCard() {
        try {
            View contentView = scrollView.getChildAt(0);
            if (contentView != null) {
                int scrollY = scrollView.getScrollY();
                int contentHeight = contentView.getHeight();
                int scrollViewHeight = scrollView.getHeight();

                boolean atTop = scrollY == 0;
                boolean atBottom = (scrollY + scrollViewHeight) >= contentHeight;

                if (atBottom) {
                    scrolledToBottom = true;
                    iv_scroll_toggle.setImageResource(R.drawable.baseline_arrow_upward_24);
                    tv_scroll_toggle.setText(R.string.uper_jayein);
                } else if (atTop) {
                    scrolledToBottom = false;
                    iv_scroll_toggle.setImageResource(R.drawable.baseline_arrow_downward_24);
                    tv_scroll_toggle.setText(R.string.neeche_jayein);
                }

                boolean canScroll = contentHeight > scrollViewHeight;
                cv_scroll_toggle.setVisibility((canScroll && !hideScrollToggle) ? View.VISIBLE : View.GONE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void clearSelectLikeDislike(boolean isLike, boolean isAfterAnimation) {
        if (!isLike) {
            lottie_dislike.clearAnimation();
            lottie_dislike.setVisibility(View.GONE);
            iv_dislike.setEnabled(true);
            iv_dislike.setVisibility(View.VISIBLE);

            iv_dislike.setScaleX(isAfterAnimation ? 1.4f : 1.1f);
            iv_dislike.setScaleY(isAfterAnimation ? 1.4f : 1.1f);
            int tintColorDislike = ContextCompat.getColor(MicrolectureListActivity.this, isAfterAnimation ? R.color.lecture_dislike_active_color : R.color.black);
            iv_dislike.setImageDrawable(ContextCompat.getDrawable(MicrolectureListActivity.this, isAfterAnimation ? R.drawable.img_dislike_filled : R.drawable.img_dislike_outline));
            iv_dislike.setColorFilter(tintColorDislike, PorterDuff.Mode.SRC_IN);
        } else {
            lottie_like.clearAnimation();
            lottie_like.setVisibility(View.GONE);
            iv_like.setEnabled(true);
            iv_like.setVisibility(View.VISIBLE);

            iv_like.setScaleX(isAfterAnimation ? 1.4f : 1.1f);
            iv_like.setScaleY(isAfterAnimation ? 1.4f : 1.1f);
            int tintColorLike = ContextCompat.getColor(MicrolectureListActivity.this, isAfterAnimation ? R.color.lecture_like_active_color : R.color.black);
            iv_like.setImageDrawable(ContextCompat.getDrawable(MicrolectureListActivity.this, isAfterAnimation ? R.drawable.img_like_filled : R.drawable.img_like_outline));
            iv_like.setColorFilter(tintColorLike, PorterDuff.Mode.SRC_IN);
        }
    }

    private void setLikeDislike(boolean isLike) {
        clearSelectLikeDislike(isLike, false);
        clearSelectLikeDislike(!isLike, false);
        if (isLike) {
            //Like
            iv_like.setEnabled(false);
            iv_like.setVisibility(View.GONE);
            lottie_like.setVisibility(View.VISIBLE);
            lottie_like.playAnimation();
        } else {
            //Dislike
            iv_dislike.setEnabled(false);
            iv_dislike.setVisibility(View.GONE);
            lottie_dislike.setVisibility(View.VISIBLE);
            lottie_dislike.playAnimation();
        }
    }

    private void setCommentDialog() {
        scrollView.post(() -> {
            previewLayout.post(() -> {
                scrollView.scrollTo(0, 0);

                // height from videoLayout to screen bottom
                int[] previewLocation = new int[2];
                int[] toolBarLocation = new int[2];
                previewLayout.getLocationOnScreen(previewLocation);
//                toolbar.getLocationOnScreen(toolBarLocation);
                int videoBottom = previewLocation[1] + previewLayout.getHeight();
//                int toolBarBottom = toolBarLocation[1] + toolbar.getHeight();

                Rect visibleFrame = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(visibleFrame);
                int usableScreenHeight = visibleFrame.bottom;

//                int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
                int dialogHeight = usableScreenHeight - videoBottom;

//                if (resetCommentViewModel) {
                CommentsViewModel viewModel = new ViewModelProvider(this).get(CommentsViewModel.class);
                viewModel.reset();
//                }

                // bottom sheet
                CommentsBottomSheet sheet = new CommentsBottomSheet(previewDto, metadataModel);
                sheet.setTargetHeight(dialogHeight);
//                sheet.setMicroLectureDto(previewDto);
//                sheet.setMicrolectureMetadataModel(metadataModel);
                sheet.setChapterId(chapterId);
//                sheet.setSnackbarMessageListener(this);
                sheet.setOnDismissListener(() -> {
                    scrollView.setNestedScrollingEnabled(true);
                    isCommentDialogOpen = false;
                    cv_comment.setClickable(true);
                });

                scrollView.setNestedScrollingEnabled(false);
                sheet.show(getSupportFragmentManager(), CommentsBottomSheet.TAG);
            });
        });
    }

    private void checkNotificationPermissionForDownload() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            getBitratesFromUrls();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getBitratesFromUrls();
            } else {
                getBitratesFromUrls();
                Log.d("MICRO_LECTURE_ACTIVITY", "initializeRequest: permission denied for notifications");
            }
        }
    }


    private void logFirebaseEvent() {
        Map<String, String> params = new HashMap<>();
        params.put("action", "microlecture_list_how_to_card_click");
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.microlecture_list_how_to_card_click.getActionName(), params);
    }

    private void getBitratesFromUrls() {
        try {
            if (!CommonMethods.isOnline(this)) {
                CommonMethods.showNegativeSnackbar(ParentLayout, getString(R.string.internet_failure_error), 3000);
                return;
            }
            Log.d("DownloadFlow", "Entered getBitratesFromUrls()");

            if (videoDownloadDialog != null) {
                Log.d("DownloadFlow", "videoDownloadDialog is not null. Proceeding...");

                if (downloadDto == null || downloadDto.getVideoDetails() == null) {
                    return;
                }

                // Set initial UI state
                contentLayout.setVisibility(View.GONE);
                progressLayout.setVisibility(View.GONE);
                downloadShimmerLayout.setVisibility(View.VISIBLE);
                downloadShimmer.setVisibility(View.VISIBLE);
                downloadDataFailed.setVisibility(View.GONE);
                internetSlow.setVisibility(View.GONE);
                downloadShimmer.startShimmer();
                videoDownloadDialog.show();

                if (CollectionUtils.isEmpty(downloadDto.getVideoDetails().getQualityDtos())) {
                    Log.w("DownloadFlow", "No qualityDtos found. Attempting fallback using previewDto...");

                    if (previewDto == null || previewDto.getVideoDetails() == null) {
                        Log.e("DownloadFlow", "previewDto or its videoDetails is null. Aborting.");
                        return;
                    }

                    VideoQualityDto videoQualityDto = VideoQualityDto.builder()
                            .quality("1080p")
                            .videoUrl(previewDto.getVideoDetails().getUrl())
                            .selected(true)
                            .build();

                    downloadDto.getVideoDetails().getQualityDtos().add(videoQualityDto);
                    downloadVideoQualityDtos.add(videoQualityDto);
                    Log.d("DownloadFlow", "Fallback qualityDto added with URL: " + videoQualityDto.getVideoUrl());
                }

                int size = downloadDto.getVideoDetails().getQualityDtos().size();
                Log.d("DownloadFlow", "Starting bitrate fetch for " + size + " qualities");

                CountDownLatch latch = new CountDownLatch(size);

                for (int i = 0; i < size; i++) {
                    String url = downloadDto.getVideoDetails().getQualityDtos().get(i).getVideoUrl();
                    Log.d("DownloadFlow", "Launching bitrate fetch for index " + i + " with URL: " + url);
                    fetchBitrateWithExecutor(url, i, latch);
                }

                new Thread(() -> {
                    try {
                        Log.d("DownloadFlow", "Waiting for all bitrate threads to complete...");
                        boolean completed = latch.await(60, java.util.concurrent.TimeUnit.SECONDS);
                        if (!completed) {
                            Log.w("DownloadFlow", "Bitrate fetch timed out after 60s");
                        }
                        Log.d("DownloadFlow", "All bitrate fetch threads completed. Updating UI.");

                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (isFinishing() || isDestroyed()) return;
                            checkBitrateData();
                            Log.d("DownloadFlow", "UI update after bitrate fetch triggered.");
                        });
                    } catch (InterruptedException e) {
                        Log.e("DownloadFlow", "Latch interrupted while waiting for bitrate fetch", e);
                    }
                }).start();
            } else {
                Log.w("DownloadFlow", "videoDownloadDialog is null. Skipping download dialog setup.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void checkBitrateData() {
        Log.d("DownloadFlow", "Starting bitrate data check...");
        boolean correct = true;

        for (VideoQualityDto videoQualityDto : downloadDto.getVideoDetails().getQualityDtos()) {
            Log.d("DownloadFlow", "Checking quality size: " + videoQualityDto.getSize());
            if (videoQualityDto.getSize() == null || videoQualityDto.getSize().equalsIgnoreCase("0")) {
                Log.w("DownloadFlow", "Invalid bitrate found: size is " + videoQualityDto.getSize());
                correct = false;
                break;
            }
        }

        if (correct) {
            Log.d("DownloadFlow", "All bitrates are valid. Proceeding with UI update.");
            findLastSelected();
            close.setVisibility(View.VISIBLE);
            downloadShimmerLayout.setVisibility(View.GONE);
            contentLayout.setVisibility(View.VISIBLE);
            downloadShimmer.stopShimmer();
            downloadShimmer.setVisibility(View.GONE);
        } else {
            Log.e("DownloadFlow", "Bitrate check failed. Showing error UI.");
            close.setVisibility(View.VISIBLE);
            downloadShimmerLayout.setVisibility(View.VISIBLE);
            downloadShimmer.stopShimmer();
            downloadShimmer.setVisibility(View.GONE);
            downloadDataFailed.setVisibility(View.VISIBLE);
            internetSlow.setVisibility(View.VISIBLE);
        }

        Log.d("DownloadFlow", "Bitrate data check completed.");
    }

    private void fetchBitrateWithExecutor(String m3u8Url, int pos, CountDownLatch latch) {
        Log.d("Download Status", "getBitratesFromUrls: execute ");
        try {
            if (executorService == null || executorService.isShutdown()) {
                executorService = Executors.newFixedThreadPool(2);
            }
            executorService.execute(() -> {
                try {
                    int bitrate = fetchBitrate(m3u8Url);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (isFinishing() || isDestroyed()) return;
                        String approxSize = getApproxSize(bitrate);
                        Log.d("Download Status", "Bitrate: " + bitrate + ", Approx size: " + approxSize);
                        downloadDto.getVideoDetails().getQualityDtos().get(pos).setSize(approxSize);
                    });
                } catch (Exception e) {
                    Log.e("Download Status", "Error fetching bitrate", e);
                } finally {
                    latch.countDown();
                }
            });
        } catch (Exception e) {
            Log.e("Download Status", "Failed to submit bitrate task", e);
            latch.countDown();
        }
    }


    private String getApproxSize(int bitrate) {
        long totalSeconds = (downloadDto.getVideoDetails().getEndPos() - downloadDto.getVideoDetails().getStartPos()) / 1000;
        double value = (double) (totalSeconds * bitrate) / 8000000;
        DecimalFormat df = new DecimalFormat("#.##");
        String size = df.format(value);
        Log.d("Download Status", "Total time: " + totalSeconds + " seconds, Bitrate: " + bitrate + " bps, Approx size: " + size + " MB");
        return size;
    }

    private JSONObject getBitrateEstimationConfig() {
        try {
            String json = MyApplication.getFirebaseRemoteConfig().getString("m3u8_bitrate_estimation_config");
            if (json != null && !json.isEmpty()) {
                return new JSONObject(json);
            }
        } catch (Exception e) {
            Log.w("Bitrate", "Failed to parse remote config, using defaults", e);
        }
        return new JSONObject();
    }

    private int fetchBitrate(String m3u8Url) throws Exception {
        JSONObject config = getBitrateEstimationConfig();
        int initialSample = config.optInt("initial_sample", DEFAULT_INITIAL_SAMPLE);
        int maxSample = config.optInt("max_sample", DEFAULT_MAX_SAMPLE);
        double cvThreshold = config.optDouble("cv_threshold", DEFAULT_CV_THRESHOLD);
        int concurrencyLimit = config.optInt("concurrency", DEFAULT_CONCURRENCY);
        int connectTimeoutMs = config.optInt("connect_timeout_ms", DEFAULT_CONNECT_TIMEOUT_MS);
        int readTimeoutMs = config.optInt("read_timeout_ms", DEFAULT_READ_TIMEOUT_MS);

        URL url = new URL(m3u8Url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(connectTimeoutMs);
        connection.setReadTimeout(readTimeoutMs);

        List<String> tsUrls = new ArrayList<>();
        double totalDuration = 0.0;
        boolean isMasterPlaylist = false;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String basePath = m3u8Url.substring(0, m3u8Url.lastIndexOf("/") + 1);
            String line;

            while ((line = reader.readLine()) != null) {
                // Handle Master Playlist
                if (line.startsWith("#EXT-X-STREAM-INF")) {
                    isMasterPlaylist = true;
                    Log.d("Bitrate", "Processing master playlist line: " + line);
                    Pattern pattern = Pattern.compile("([A-Z\\-]+)=(\"[^\"]*\"|[^,]*)");
                    Matcher matcher = pattern.matcher(line);
                    while (matcher.find()) {
                        String key = matcher.group(1).trim();
                        String value = matcher.group(2).trim().replaceAll("\"", "");
                        if (key.equals("BANDWIDTH")) {
                            Log.d("Bitrate", "Found BANDWIDTH: " + value);
                            return Integer.parseInt(value);
                        }
                    }
                }
                // Handle Media Playlist
                else if (line.startsWith("#EXTINF:")) {
                    String durationStr = line.split(":")[1].replace(",", "").trim();
                    totalDuration += Double.parseDouble(durationStr);
                } else if (!line.startsWith("#") && line.endsWith(".ts")) {
                    tsUrls.add(basePath + line);
                }
            }
        }

        if (!isMasterPlaylist && !tsUrls.isEmpty() && totalDuration > 0) {
            Log.d("Bitrate", "Media playlist: " + tsUrls.size() + " segments, " + totalDuration + "s total");

            // Adaptive sampling: start with a small sample, widen if sizes vary too much
            List<String> sample = pickSample(tsUrls, initialSample);
            List<Long> sizes = headSegmentsParallel(sample, concurrencyLimit, connectTimeoutMs, readTimeoutMs);
            Log.d("Bitrate", "Initial sample: " + sample.size() + " segments, " + sizes.size() + " valid sizes");

            if (sizes.size() > 1) {
                double cv = coefficientOfVariation(sizes);
                if (cv > cvThreshold && sample.size() < tsUrls.size()) {
                    int bigger = Math.min(maxSample, tsUrls.size());
                    Log.d("Bitrate", "High variance (cv=" + cv + "), widening sample to " + bigger);
                    sample = pickSample(tsUrls, bigger);
                    sizes = headSegmentsParallel(sample, concurrencyLimit, connectTimeoutMs, readTimeoutMs);
                }
            }

            if (sizes.isEmpty()) {
                Log.w("Bitrate", "No valid segment sizes obtained for: " + m3u8Url);
                return 0;
            }

            double avgSize = 0;
            for (Long s : sizes) avgSize += s;
            avgSize /= sizes.size();

            double totalBytes = avgSize * tsUrls.size();
            int bitrate = (int) ((totalBytes * 8) / totalDuration);
            Log.d("Bitrate", "Estimated bitrate: " + bitrate + " bps (sampled " + sizes.size() + "/" + tsUrls.size() + " segments)");
            return bitrate;
        }

        Log.w("Bitrate", "Unable to determine bitrate for: " + m3u8Url);
        return 0;
    }

    private List<String> pickSample(List<String> urls, int n) {
        if (n >= urls.size()) return new ArrayList<>(urls);
        List<String> out = new ArrayList<>(n);
        double step = (double) urls.size() / n;
        for (int i = 0; i < n; i++) out.add(urls.get((int) (i * step)));
        return out;
    }

    private List<Long> headSegmentsParallel(List<String> urls, int concurrencyLimit, int connectTimeoutMs, int readTimeoutMs) {
        ExecutorService pool = Executors.newFixedThreadPool(Math.min(concurrencyLimit, urls.size()));
        try {
            List<Callable<Long>> tasks = new ArrayList<>();
            for (String u : urls) {
                tasks.add(() -> {
                    try {
                        HttpURLConnection c = (HttpURLConnection) new URL(u).openConnection();
                        c.setRequestMethod("HEAD");
                        c.setConnectTimeout(connectTimeoutMs);
                        c.setReadTimeout(readTimeoutMs);
                        int len = c.getContentLength();
                        c.disconnect();
                        return len > 0 ? (long) len : 0L;
                    } catch (Exception e) {
                        return 0L;
                    }
                });
            }
            List<Long> results = new ArrayList<>();
            for (Future<Long> f : pool.invokeAll(tasks)) {
                Long v = f.get();
                if (v > 0) results.add(v);
            }
            return results;
        } catch (Exception e) {
            Log.e("Bitrate", "Error in parallel HEAD requests", e);
            return new ArrayList<>();
        } finally {
            pool.shutdown();
        }
    }

    private double coefficientOfVariation(List<Long> sizes) {
        double mean = 0;
        for (Long s : sizes) mean += s;
        mean /= sizes.size();
        if (mean == 0) return 0;
        double variance = 0;
        for (Long s : sizes) variance += Math.pow(s - mean, 2);
        variance /= sizes.size();
        return Math.sqrt(variance) / mean;
    }


    private void setUpViewModel() {
        mediaViewModel = MyApplication.getInstance().getMediaViewModel();
        mediaViewModel.getAllMedia(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey())).
                observe(this, this::getDownloadsVideoData);
        notesViewModel = MyApplication.getInstance().getNotesViewModel();
        notesViewModel.getAllNotes(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey())).
                observe(this, this::syncNoteListWithOffline);
    }

    private void initializeMemoryLimit() {
        deviceMemoryReached = new Dialog(this);
        deviceMemoryReached.setContentView(R.layout.memory_limit_reached);
        deviceMemoryReached.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        deviceMemoryReached.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        deviceMemoryReached.setCancelable(false);
        ImageView cancel = deviceMemoryReached.findViewById(R.id.cancel);
        cancel.setOnClickListener(v -> deviceMemoryReached.dismiss());
    }


    private void initializeVideoDownloadDialog() {
        videoDownloadDialog = new Dialog(this);
        videoDownloadDialog.setContentView(R.layout.download_video_quality_selection_dialog);
        videoDownloadDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        videoDownloadDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        videoDownloadDialog.setCancelable(false);
        progressLayout = videoDownloadDialog.findViewById(R.id.progress_layout);
        contentLayout = videoDownloadDialog.findViewById(R.id.content_layout);

        close = videoDownloadDialog.findViewById(R.id.cancel);
        title = videoDownloadDialog.findViewById(R.id.micorlecture_name);
        close.setOnClickListener(v -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.micro_lec_download_cancel.getActionName());
            videoDownloadDialog.dismiss();
            MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll(DOWNLOAD_LECTURE_TAG);
        });

        downloadShimmerLayout = videoDownloadDialog.findViewById(R.id.download_shimmer_layout);
        downloadShimmer = videoDownloadDialog.findViewById(R.id.download_shimmer);
        internetSlow = videoDownloadDialog.findViewById(R.id.animation_no_internet);
        downloadDataFailed = videoDownloadDialog.findViewById(R.id.download_data_failed);
        RecyclerView videoDownloadQuality = videoDownloadDialog.findViewById(R.id.lecture_options_recycler_view);
        videoDownloadQuality.setLayoutManager(new LinearLayoutManager(this));

        CardView cardView = videoDownloadDialog.findViewById(R.id.download);
        final String LOG_TAG = "MICRO_LECTURE_ACTIVITY";

        cardView.setOnClickListener(view -> {
            Log.d(LOG_TAG, "CardView clicked");

            videoDownloadDialog.setCancelable(false);
            String videoSize = currentDownloadVideoQuality.getSize();
            Log.d(LOG_TAG, "Video size: " + videoSize);

            if (videoSize == null || videoSize.isEmpty()) {
                Log.w(LOG_TAG, "Video size is not available. Showing toast message.");
                Toast.makeText(this, "Video size is not available", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double videoSizeValue = Double.parseDouble(videoSize);
                Log.d(LOG_TAG, "Parsed video size: " + videoSizeValue);
                if (!canDownloadVideo(videoSizeValue, this)) {
                    Log.w(LOG_TAG, "Device memory is insufficient for download.");
                    videoDownloadDialog.dismiss();
                    deviceMemoryReached.show();
                } else {
                    Log.d(LOG_TAG, "Sufficient memory available. Proceeding with download.");
                    contentLayout.setVisibility(View.GONE);
                    close.setVisibility(View.VISIBLE);
                    progressLayout.setVisibility(View.VISIBLE);
                    getInteractiveQuestion(downloadDto.getId());
                    Log.d(LOG_TAG, "Content layout hidden and progress layout displayed.");
                }

                Map<String, String> params = new HashMap<>();
                params.put("isLocked", previewDto != null ? String.valueOf(previewDto.isLocked()) : "false");
                params.put(PreferenceKeys.MICROLECTURE_ID.getKey(), previewDto != null ? previewDto.getId() : "");
                params.put("chapterId", chapterId);
                params.put("resolution", currentDownloadVideoQuality.getQuality());
                params.put("size", currentDownloadVideoQuality.getSize());
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.ml_download_click.name(), params);

            } catch (Exception e) {
                Log.e(LOG_TAG, "Error parsing video size or downloading video", e);
            }

        });

        videoQualityAdapter = new DownloadVideoQualityAdapter(downloadVideoQualityDtos,
                new DownloadVideoQualityAdapter.VideoQualityListener() {
                    @Override
                    public void qualityClicked(int position) {
                        // Do not save the quality here; it’s already saved at the start of the download
                        currentDownloadVideoQuality = downloadVideoQualityDtos.get(position);
                        Log.d(TAG, "User selected quality: " + currentDownloadVideoQuality.getQuality());

                        for (int i = 0; i < downloadVideoQualityDtos.size(); i++) {
                            downloadVideoQualityDtos.get(i).setSelected(i == position);
                        }
                        videoQualityAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void closeQualityDialog() {
                        videoDownloadDialog.dismiss();
                    }
                });
        videoDownloadQuality.setAdapter(videoQualityAdapter);

    }

    private void findLastSelected() {
        String lastSelectedQuality = MyApplication.getStringData(getString(R.string.user_selected_download_quality));
        int selectedPosition = -1;
        if (lastSelectedQuality != null && !lastSelectedQuality.isEmpty()) {
            Log.d(TAG, "Last selected quality: " + lastSelectedQuality);
            for (int i = 0; i < downloadDto.getVideoDetails().getQualityDtos().size(); i++) {
                if (downloadDto.getVideoDetails().getQualityDtos().get(i).getQuality().equals(lastSelectedQuality)) {
                    selectedPosition = i;
                    break;
                }
            }
        }
        if (selectedPosition == -1) {
            selectedPosition = findLowestQualityPosition(downloadDto.getVideoDetails().getQualityDtos());
            Log.d(TAG, "No matching quality found. Selecting lowest quality: " + downloadDto.getVideoDetails().getQualityDtos().get(selectedPosition).getQuality());
        }
        currentDownloadVideoQuality = downloadDto.getVideoDetails().getQualityDtos().get(selectedPosition);
        Log.d(TAG, "Current selected quality: " + currentDownloadVideoQuality.getQuality());

        for (int i = 0; i < downloadVideoQualityDtos.size(); i++) {
            downloadVideoQualityDtos.get(i).setSelected(i == selectedPosition);
        }
        videoQualityAdapter.notifyDataSetChanged();
    }


    private int findLowestQualityPosition(List<VideoQualityDto> videoQualityDtos) {
        int position = 0;
        int lowestResolution = getResolutionInPixels(downloadDto.getVideoDetails().getQualityDtos().get(0).getQuality());
        Log.d(TAG, "Lowest quality resolution: " + lowestResolution);

        for (int i = 1; i < videoQualityDtos.size(); i++) {
            int currentResolution = getResolutionInPixels(videoQualityDtos.get(i).getQuality());
            Log.d(TAG, "Comparing resolution: " + currentResolution);
            if (currentResolution < lowestResolution) {
                position = i;
                lowestResolution = currentResolution;
                Log.d(TAG, "New lowest quality found: " + videoQualityDtos.get(i).getQuality());
            }
        }
        return position;
    }


    private int getResolutionInPixels(String quality) {
        try {
            return Integer.parseInt(quality.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }


    private void cacheImageWithCheck(MediaEntity mediaEntity) {
        Glide.with(this)
                .load(mediaEntity.getThumbnailUrl())
                .listener(new RequestListener<>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.e("GlideCache", "Image load failed: " + e.getMessage());
                        mediaEntity.setThumbnailLoaded(false);
                        mediaViewModel.deleteThenInsert(mediaEntity);
                        saveDownloadData(mediaEntity);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource,
                                                   Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        Log.d("GlideCache", "Image successfully loaded and cached");
                        mediaEntity.setThumbnailLoaded(true);
                        mediaViewModel.deleteThenInsert(mediaEntity);
                        saveDownloadData(mediaEntity);
                        return false;
                    }
                })
                .submit();
    }

    public void saveDownloadData(MediaEntity mediaEntity) {
        try {
            OfflineDownloadEntity offlineDownloadEntity = OfflineDownloadEntity.builder()
                    .userId(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()))
                    .startTime(System.currentTimeMillis())
                    .microLectureId(mediaEntity.getMicroLectureId())
                    .url(mediaEntity.getUrl())
                    .duration(mediaEntity.getEndPos() - mediaEntity.getStartPos())
                    .quality(currentDownloadVideoQuality.getQuality())
                    .build();
            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                    SAVE_DOWNLOAD, new JSONObject(gson.toJson(offlineDownloadEntity)),
                    response -> {
                        videoDownloadDialog.dismiss();
                        MyApplication.saveData(mediaEntity.getUrl(), offlineDownloadEntity.getStartTime());
                        completeDownload(mediaEntity);
                        Map<String, String> params = new HashMap<>();
                        params.put(PreferenceKeys.MICROLECTURE_ID.getKey(), mediaEntity.getMicroLectureId());
                        params.put("microlectureName", mediaEntity.getMicroLectureTitle());
                        params.put("downloadVideoQuality", currentDownloadVideoQuality.getQuality());
                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.micro_lec_download.getActionName(), params);
                        Log.d("lectureDownload", "saveDownloadData: " + params);
                    }, new VolleyGlobalErrorListener(MicrolectureListActivity.this) {
                @Override
                public void postError(VolleyError error) {
                    videoDownloadDialog.dismiss();
                    completeDownload(mediaEntity);
                    Log.d("saveDownloadData", "postError " + error.getMessage());
                }
            });
            myJsonObjectRequest.setTag(DOWNLOAD_LECTURE_TAG);
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
        } catch (JSONException e) {
            videoDownloadDialog.dismiss();
            completeDownload(mediaEntity);
            Log.d("saveDownloadData", "JSONException " + e.getMessage());
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void completeDownload(MediaEntity mediaEntity) {
        if (previewDto.getVideoDetails().getQualityDtos().size() > 1) {
            MyApplication.saveData(PreferenceKeys.USER_SELECTED_DOWNLOAD_QUALITY.getKey(), currentDownloadVideoQuality.getQuality());
            MyApplication.getUserSelectedDownloadQualityDatabaseReference().setValue(currentDownloadVideoQuality.getQuality());
        }

        mediaItem = new MediaItem.Builder()
                .setUri(Uri.parse(currentDownloadVideoQuality.getVideoUrl()))
                .setMediaId(currentDownloadVideoQuality.getVideoUrl())
                .setMediaMetadata(
                        new MediaMetadata.Builder()
                                .setTitle(previewDto.getTitle())
                                .build()
                )
                .build();

        downloadTracker = DemoUtil.getDownloadTracker(this);
        RenderersFactory renderersFactory = DemoUtil.buildRenderersFactory(this);
        downloadTracker.toggleDownload(
                mediaItem,
                renderersFactory
        );
    }


    public void getInteractiveQuestion(String microLectureId) {
        final Map<String, Object> map = new HashMap<>();
        Log.d(TAG, "interactive api - microLectureId: " + microLectureId);
        map.put("microLectureId", microLectureId);
        MyJsonObjectRequest myJsonArrayRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_MICROLECTURE_QUESTION_OPTIONS, map), null,
                response -> {
                    Log.d(TAG, "getInteractiveQuestion: " + response);
                    try {
                        Gson gson = new Gson();
                        InteractiveQuestionResponseDto interactiveResponse =
                                gson.fromJson(response.toString(), InteractiveQuestionResponseDto.class);

                        List<MicroLectureQuestionsAnsDto> questionsAnsList = interactiveResponse.getQuestionsAnsDtoList();
                        List<MicroLectureQuestionAnsResponseDto>
                                questionsAnsResponseList = interactiveResponse.getMicroLectureQuestionAnsResponseDtos();

                        startVideoDownload(questionsAnsList, questionsAnsResponseList);

                    } catch (Exception ignored) {
                    }
                },
                new VolleyGlobalErrorListener(MicrolectureListActivity.this) {
                    @Override
                    public void postError(VolleyError error) {
                        Log.e(TAG, "Error in API getInteractiveQuestion: ", error);
                        startVideoDownload(new ArrayList<>(), new ArrayList<>());
                    }
                });

        myJsonArrayRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonArrayRequest);
    }

    private void startVideoDownload(List<MicroLectureQuestionsAnsDto> questionsAnsList,
                                    List<MicroLectureQuestionAnsResponseDto> questionsAnsResponseList) {
        Log.d(TAG, "startVideoDownload: " + questionsAnsResponseList.toString());
        MediaEntity mediaEntity = MediaEntity.builder()
                .microLectureTitle(downloadDto.getTitle())
                .microLectureId(downloadDto.getId())
                .chapterTitle(chapterName)
                .chapterId(chapterId)
                .duration(getDuration())
                .size(currentDownloadVideoQuality.getSize())
                .progress(downloadDto.getDownloadProgress())
                .url(currentDownloadVideoQuality.getVideoUrl())
                .id(currentDownloadVideoQuality.getVideoUrl())
                .userId(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()))
                .status(DownloadStatus.DOWNLOAD_RESUMED.name())
                .questionsAnsList(!CollectionUtils.isEmpty(questionsAnsList) &&
                        !CollectionUtils.isEmpty(questionsAnsResponseList) ? questionsAnsList : null)
                .questionsAnsResponseList(!CollectionUtils.isEmpty(questionsAnsList) &&
                        !CollectionUtils.isEmpty(questionsAnsResponseList) ? questionsAnsResponseList : null)
                .startPos(downloadDto.getVideoDetails().getStartPos())
                .endPos(downloadDto.getVideoDetails().getEndPos())
                .thumbnailUrl(downloadDto.getVideoPreview().getThumbnail())
                .locked(false)
                .build();
        cacheImageWithCheck(mediaEntity);
    }


    private String getDuration() {
        return formatDuration(previewDto.getVideoDetails().getEndPos() - previewDto.getVideoDetails().getStartPos());
    }

    private String formatDuration(long durationMillis) {
        long totalSeconds = durationMillis / 1000;
        long minutes = totalSeconds / 60;
        return String.format(Locale.ENGLISH, "%02d", minutes);
    }

    public static String getStorageInfo(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Log.d("storage_phone", "getStorageInfo: manager");
            StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            StorageVolume storageVolume = storageManager.getPrimaryStorageVolume();
            long availableBytes = 0;
            long totalSpace = 0;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                availableBytes = storageVolume.getDirectory().getFreeSpace();
                totalSpace = storageVolume.getDirectory().getTotalSpace();
            }

            // Calculate available space excluding OS space
            long osSpace = totalSpace - availableBytes;
            long availableExcludingOS = totalSpace - osSpace;

            return "Available: " + formatSizeBase10(availableBytes) + ", Excluding OS: " + formatSizeBase10(availableExcludingOS) + ", Total: " + formatSizeBase10(totalSpace);
        } else {
            return getStorageInfoLegacy();
        }
    }

    public static String formatSizeBase10(long size) {
        String suffix = "MB";
        double sizeInMB = size / (1000.0 * 1000.0); // Convert bytes to GB (Base-10)
        return String.format("%.2f %s", sizeInMB, suffix);
    }

    public static String getStorageInfoLegacy() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        long availableBlocks = stat.getAvailableBlocksLong();

        long availableBytes = availableBlocks * blockSize;
        long totalBytes = totalBlocks * blockSize;

        long osSpace = (long) (totalBytes * 0.1);  // 10% is reserved for the OS
        long availableExcludingOS = totalBytes - osSpace;

        return "Available: " + formatSizeBase10(availableBytes) + ", Excluding OS: " + formatSizeBase10(availableExcludingOS) + ", Total: " + formatSizeBase10(totalBytes);
    }

    public boolean canDownloadVideo(double videoSize, Context context) {

        Log.d("DownloadStatus", "canDownloadVideo called with videoSize: " + videoSize + " and context: " + context);

        String storageInfo = getStorageInfo(context);
        Log.d("DownloadStatus", "Storage Info: " + storageInfo);

        String availableSpaceExcludingOS = storageInfo.split(",")[1].split(":")[1].trim().replace("MB", "");
        Log.d("DownloadStatus", "Available space excluding OS: " + availableSpaceExcludingOS);

        double availableExcludingOS = Double.parseDouble(availableSpaceExcludingOS);
        Log.d("DownloadStatus", "Available space in GB: " + availableExcludingOS);


        double requiredBuffer = 0.1 * availableExcludingOS;
        Log.d("DownloadStatus", "Required buffer: " + requiredBuffer);

        if ((availableExcludingOS - videoSize) < requiredBuffer) {
            Log.d("DownloadStatus", "Not enough space to download the video.");
            //for now user is having issue so removing it
            return true;
        } else {
            Log.d("DownloadStatus", "Enough space to download the video.");
            return true;
        }
    }


    private void openLink() {

        HashMap<String, String> params = new HashMap<>();
        params.put("chapterName", chapterName);
        params.put("chapterId", chapterId);
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.share_Lecture.getActionName(), params);

        String encodedUrl;
        try {
            encodedUrl = URLEncoder.encode("https://www.arivihan.com/micro/lecture" + "?chapterId=" + chapterId + "&chapterName=" + chapterName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        String deepLink = "https://doubtbuzzter2.page.link/?link=" + encodedUrl + "&apn=arivihan.technologies.doubtbuzzter2";
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TITLE, "I found a fantastic explanation of " + chapterName + "! on the Arivihan app. Download it & you won't regret it!");

        String shareMsg = "I found a fantastic explanation of " + chapterName + "! on the Arivihan app. Download it & you won't regret it! \n\n*Click on link below to View*\n" + deepLink;

        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareMsg);
        destroyHowTo();
        startActivity(Intent.createChooser(shareIntent, ""));
    }

    public void openPdf(String url, String title) {
        try {
            Log.d(TAG, "PDF URL: " + url);
            Log.d(TAG, "PDF Title: " + title);
            NotesDto notesData = microLectureDetailsDto.getChapterDetails().getNotesData();

            String notesType = (notesData != null && StringUtils.isNotEmpty(notesData.getNotesType()))
                    ? notesData.getNotesType()
                    : microLectureDetailsDto.getChapterDetails().getNotesType();

            PdfUtils.openPdfViewer(this, url, StringUtils.isNotEmpty(title) ? title : "", notesType, subjectCode, false, "", "");

            Map<String, String> params = new HashMap<>();
            params.put("notesUrl", url);
            params.put("chapterName", microLectureDetailsDto.getChapterDetails().getTitle());
            params.put("openFrom", "MicroLectureClicked");
            params.put(PreferenceKeys.MICROLECTURE_ID.getKey(), microlectureId);
            params.put("chapterId", chapterId);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.micro_lec_notes_clicked.getActionName(), params);


////            Intent target = new Intent(Intent.ACTION_VIEW);
////            target.setDataAndType(Uri.parse(url), "application/pdf");
//            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//
//            if (target.resolveActivity(this.getPackageManager()) != null) {
//                Intent intent = Intent.createChooser(target, "Open File");
//                destroyHowTo();
//                returningFromPdfViewer = true;
//                startActivity(intent);
//                Map<String, String> params = new HashMap<>();
//                params.put("notes_url",url);
//                params.put("Chapter_name",microLectureDetailsDto.getChapterDetails().getTitle());
//                params.put("open_from","MicroLectureClicked");
//                params.put(PreferenceKeys.MICROLECTURE_ID.getKey(), microlectureId);
//                params.put("chapterId", chapterId);
//                MyApplication.logFirebaseBundleCreation("notes_clicked", params);
//            } else {
//                playStoreDialog = new PlayStoreDialog(this);
//                playStoreDialog.showPlayStoreDialog();
//            }


        } catch (ActivityNotFoundException e) {
            Log.d("openPdf:", e.getMessage());
        }
    }

    private void getMicroLectureList() {
        Log.d(TAG, "getMicroLectureList called : ");
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("chapterId", chapterId);
        objectMap.put("essayAndOneLinerSupported", true);
        objectMap.put("lecturesNotesSupported", true);
        Log.d(TAG, "Request: " + objectMap.toString());
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(MICRO_LECTURE_LIST, objectMap), null,
                response -> {
                    dataLoaded = true;
                    TypeToken<MicroLectureDetailsDto> token = new TypeToken<MicroLectureDetailsDto>() {
                    };
                    microLectureDetailsDto = gson.fromJson(response.toString(), token.getType());
                    Log.d(TAG, "getMicroLectureListData: " + microLectureDetailsDto.toString());
                    updateDtoForSelectionFromRoomDb();
                    updateDtoForRoutine();
                    updateRecyclerView();
                    setUpViewModel();
                    setSlimChart(microLectureDetailsDto.getChapterDetails().getProgress());
                    if (microLectureDetailsDto.getMicroLectureDtos().isEmpty()) {
                        lastWatched.setText(chapterName);
                        previewThumbnail.setVisibility(View.VISIBLE);
                        centerImage.setVisibility(View.VISIBLE);
                        previewVideoPlayer.setVisibility(View.GONE);
                        showNoLectureDialog();
                    }
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                Log.e("postError: ", error.getMessage());
            }
        });
        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
    }

    private void showNoLectureDialog() {
        noLecturedialog = new Dialog(this);
        noLecturedialog.setContentView(R.layout.lecture_coming_soon);
        noLecturedialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        noLecturedialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        noLecturedialog.setCancelable(false);
        CardView goBack = noLecturedialog.findViewById(R.id.go_back);
        goBack.setOnClickListener(v -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.micro_lecture_coming_soon.getActionName());
            getOnBackPressedDispatcher().onBackPressed();
        });
        ImageView cancel = noLecturedialog.findViewById(R.id.cancel);
        cancel.setOnClickListener(v -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.micro_lecture_coming_soon.getActionName());
            getOnBackPressedDispatcher().onBackPressed();
        });
        noLecturedialog.show();
    }

    /**
     * This method has been written for updating selection and highlighting
     * one micro lecture icon bold so we use room db for latest
     * selection because causing some delay in updating the
     * latest selection-(ATT-1769)
     */
    private void updateDtoForSelectionFromRoomDb() {
        if (selectionMicroLectureEntity == null) {
            return;
        }
        Log.d(TAG, "MicroEntity: " + selectionMicroLectureEntity.getSelectedMicroLectureId());
        for (MicroLectureDto item : microLectureDetailsDto.getMicroLectureDtos()) {
            item.setHighlightText(false);
            item.setMicroLectureSelected(false);
            for (Questions questions : item.getQuestions()) {
                questions.setHighlightText(false);
            }
        }

        for (MicroLectureDto item : microLectureDetailsDto.getMicroLectureDtos()) {
            if (selectionMicroLectureEntity.getSelectedMicroLectureId().equals(item.getId())) {
                Log.d("MicroEntity", "updateDtoForSelectionFromRoomDb id matched : " + selectionMicroLectureEntity);
                if (selectionMicroLectureEntity.getTypeSelected() != null &&
                        selectionMicroLectureEntity.getTypeSelected().equals(LectureContentType.VIDEO.name())) {
                    Log.d("MicroEntity", "updateDtoForSelectionFromRoomDb id matched set highlightText true : ");
                    item.setHighlightText(true);
                }
                item.setMicroLectureSelected(true);
                for (Questions questions : item.getQuestions()) {
                    if (selectionMicroLectureEntity.getTypeSelected() != null && selectionMicroLectureEntity.getTypeSelected().equals(questions.getType())) {
                        questions.setHighlightText(true);
                    }
                }
            }
        }
    }

    /**
     * Routine we highlight entire practice section in a microlecture
     */
    private void updateDtoForRoutine() {
        if (highlightQuestion) {
            for (MicroLectureDto item : microLectureDetailsDto.getMicroLectureDtos()) {
                item.setHighlightText(false);
                if (item.getId().equals(microlectureId)) {
                    for (Questions qitem : item.getQuestions()) {
                        Log.d(TAG, "getMicroLectureList: " + qitem);
                        qitem.setHighlightText(true);
                    }
                }
            }
        }
    }

    private void handlePreviewDownloadClick() {
        if (previewDto != null) {
            if (!previewDto.isLocked()) {
                if (previewDownloadEntity != null) {
                    if (DownloadStatus.DOWNLOAD_RESUMED.name().equals(previewDownloadEntity.getStatus()) ||
                            DownloadStatus.DOWNLOAD_PAUSED.name().equals(previewDownloadEntity.getStatus()) ||
                            DownloadStatus.DOWNLOAD_QUEUED.name().equals(previewDownloadEntity.getStatus())) {
                        openBottomSheet(previewPosition);
                    } else if (DownloadStatus.DOWNLOAD_FAILED.name().equals(previewDownloadEntity.getStatus())) {
                        downloadDto = previewDto;
                        downloadVideoQualityDtos.clear();
                        downloadVideoQualityDtos.addAll(downloadDto.getVideoDetails().getQualityDtos());
                        checkNotificationPermissionForDownload();
                    }
                } else {
                    downloadDto = previewDto;
                    downloadVideoQualityDtos.clear();
                    downloadVideoQualityDtos.addAll(downloadDto.getVideoDetails().getQualityDtos());
                    checkNotificationPermissionForDownload();
                }
            } else {
                subscriptionDialog.show();
                Map<String, String> params = new HashMap<>();
                params.put("isLocked", String.valueOf(previewDto.isLocked()));
                params.put(PreferenceKeys.MICROLECTURE_ID.getKey(), previewDto.getId());
                params.put("chapterId", chapterId);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.ml_download_click.name(), params);
            }
        }
    }

    private void handlePreviewDownloadStatus() {
        if (previewDto != null) {
            previewDownloadLiveData = MyApplication.getInstance().getMediaViewModel().getMediaByLectureId(previewDto.getId());
            previewDownloadLiveData.observe(this, mediaEntity -> {
                previewDownloadEntity = mediaEntity;
                if (previewDownloadEntity != null) {
                    if (DownloadStatus.DOWNLOAD_COMPLETED.name().equals(previewDownloadEntity.getStatus())) {
                        download.setVisibility(View.GONE);
                        download_progress_layout.setVisibility(View.GONE);
                        download_completed.setVisibility(View.VISIBLE);
                        download_status_text.setText(getString(R.string.Downloaded));
                        download_status_text.setVisibility(View.VISIBLE);
                    } else if (DownloadStatus.DOWNLOAD_RESUMED.name().equals(previewDownloadEntity.getStatus()) ||
                            DownloadStatus.DOWNLOAD_PAUSED.name().equals(previewDownloadEntity.getStatus()) ||
                            DownloadStatus.DOWNLOAD_QUEUED.name().equals(previewDownloadEntity.getStatus())) {
                        download.setVisibility(View.GONE);
                        download_progress_layout.setVisibility(View.VISIBLE);
                        download_progress_bar.setProgress(previewDownloadEntity.getProgress());
                        download_progress_text.setText(previewDownloadEntity.getProgress() + "%");
                        download_completed.setVisibility(View.GONE);
                        download_status_text.setVisibility(View.GONE);
                    } else {
                        download.setVisibility(View.VISIBLE);
                        download_progress_layout.setVisibility(View.GONE);
                        download_completed.setVisibility(View.GONE);
                        download_status_text.setVisibility(View.VISIBLE);
                        download_status_text.setText(getString(R.string.Download));
                    }
                } else {
                    download.setVisibility(View.VISIBLE);
                    download_progress_layout.setVisibility(View.GONE);
                    download_completed.setVisibility(View.GONE);
                    download_status_text.setVisibility(View.VISIBLE);
                    download_status_text.setText(getString(R.string.Download));
                }
            });
        }
    }

    private void setUpRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        if (microlectureId != null) {
            microlectureAdapter = new MicrolectureAdapter(this, list, this, this, microlectureId);
            microlectureAdapter.setOpenVideo(openVideo);
        } else {
            microlectureAdapter = new MicrolectureAdapter(this, list, this, this);
        }
        recyclerView.setAdapter(microlectureAdapter);
        recyclerView.setNestedScrollingEnabled(false);
        updateScrollCard();
    }

    private void updateRecyclerView() {
        microlectureAdapter.setAllCompleted(false);
        for (int i = 0; i < list.size(); i++) {
            Log.d("videoOpenList", list.get(i).getId() + " code " + list.get(i).getCode());
            if (!list.get(i).isCompleted()) {
                microlectureAdapter.setAllCompleted(false);
                break;
            }
        }
        list.clear();
        if (microLectureDetailsDto == null && CollectionUtils.isEmpty(microLectureDetailsDto.getMicroLectureDtos())) {
            return;
        }

        notesUrl = (microLectureDetailsDto.getChapterDetails().getNotesData() != null
                && StringUtils.isNotEmpty(microLectureDetailsDto.getChapterDetails().getNotesData().getNotesUrl()))
                ? microLectureDetailsDto.getChapterDetails().getNotesData().getNotesUrl()
                : microLectureDetailsDto.getChapterDetails().getNotesUrl();

        String ncertSolutions = (microLectureDetailsDto.getChapterDetails().getNotesData() != null
                && StringUtils.isNotEmpty(microLectureDetailsDto.getChapterDetails().getNotesData().getNotesUrl()))
                ? microLectureDetailsDto.getChapterDetails().getNotesData().getNotesUrl()
                : microLectureDetailsDto.getChapterDetails().getNcertSolutions();


        if (StringUtils.isNotEmpty(notesUrl)) {
            notesLayout.setVisibility(View.VISIBLE);
        }
        if (StringUtils.isNotEmpty(ncertSolutions)) {
            cv_ncert_solutions.setVisibility(View.VISIBLE);
        }

        list.addAll(microLectureDetailsDto.getMicroLectureDtos());
        microlectureAdapter.notifyDataSetChanged();

        if (openVideo && list != null && StringUtils.isNotEmpty(microlectureId)) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) != null && (list.get(i).getId().equals(microlectureId) || list.get(i).getCode().equals(microlectureId))) {
                    startVideo(i);
                    openVideo = false;
                    break;
                }
            }
        }

        if (openPractice && list != null && StringUtils.isNotEmpty(qnsId)) {
            Log.d("qnsIdFound1", qnsId);

            for (int i = 0; i < list.size(); i++) {
                List<Questions> questions = list.get(i).getQuestions();

                for (int j = 0; j < questions.size(); j++) {

                    Questions questionIds = questions.get(j);
                    Set<String> ids = questionIds.getQuestionIds();

                    for (String id : ids) {
                        Log.d("qnsIdFoundSet", id);

                    }

                    if (ids.contains(qnsId)) {
                        Log.d("qnsIdFound2", list.get(i).getCode());
                        onPracticeClick(questionIds.getType(), i, questionIds.isLock(), -1);
                        break;
                    }
                }

            }
        }

        if (openNote && list != null && StringUtils.isNotEmpty(microlectureId)) {
            for (int i = 0; i < list.size(); i++) {
                MicroLectureDto microLectureDto = list.get(i);
                if (!getIntent().getBooleanExtra("note_opened", false) && microLectureDto != null && (microLectureDto.getId().equals(microlectureId) || microLectureDto.getCode().equals(microlectureId))) {
                    lecturesNotesOpen(microLectureDto, microLectureDto.getPdfUrl());
                    getIntent().putExtra("note_opened", true);
                    openNote = false;
                    break;
                }
            }
        }

    }

    private void initializePlayer() {
        if (player == null) {
            // 1. Create a default TrackSelector
//            LoadControl loadControl = new DefaultLoadControl(
//                    new DefaultAllocator(true, 16),
//                    VideoPlayerConfig.MIN_BUFFER_DURATION,
//                    VideoPlayerConfig.MAX_BUFFER_DURATION,
//                    VideoPlayerConfig.MIN_PLAYBACK_START_BUFFER,
//                    VideoPlayerConfig.MIN_PLAYBACK_RESUME_BUFFER, -1, true);
//
//            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
//            TrackSelection.Factory videoTrackSelectionFactory =
//                    new AdaptiveTrackSelection.Factory(bandwidthMeter);
//            TrackSelector trackSelector =
//                    new DefaultTrackSelector(videoTrackSelectionFactory);
            // 2. Create the player
            player = new ExoPlayer.Builder(this).build();
            player.setVolume(0f);
            previewVideoPlayer.setPlayer(player);

//            player.addListener(new Player.Listener() {
//                @Override
//                public void onPlaybackStateChanged(int state) {
//
//                }
//            });
        }
    }

    private void startBufferingTimeoutCheck() {
        stopBufferingTimeoutCheck(); // preventing duplicates
        checkBufferingRunnable = () -> {
            if (previewVideoBufferingStartTime > 0) {
                long elapsed = System.currentTimeMillis() - previewVideoBufferingStartTime;
                if (elapsed >= BUFFERING_THRESHOLD_MS) {
                    // showing preview thumbnail is it's taking time to load
                    setPreviewThumbnailAfterVideoCompletion();
                }
            }
        };
        bufferHandler.postDelayed(checkBufferingRunnable, BUFFERING_THRESHOLD_MS);
    }

    private void stopBufferingTimeoutCheck() {
        if (checkBufferingRunnable != null) {
            bufferHandler.removeCallbacks(checkBufferingRunnable);
            checkBufferingRunnable = null;
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void buildMediaSource(Uri hlsUri) {

        DefaultHttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory()
                .setConnectTimeoutMs(3000)
                .setReadTimeoutMs(3000)
                .setAllowCrossProtocolRedirects(true);

        DefaultDataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(this, httpDataSourceFactory);

        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(hlsUri)
                .setMimeType(MimeTypes.APPLICATION_M3U8)
                .build();

        HlsMediaSource hlsMediaSource = new HlsMediaSource.Factory(dataSourceFactory)
                .setLoadErrorHandlingPolicy(new DefaultLoadErrorHandlingPolicy() {
                    @Override
                    public long getRetryDelayMsFor(@NonNull LoadErrorInfo errorInfo) {
                        return C.TIME_UNSET;
                    }

                    @Override
                    public int getMinimumLoadableRetryCount(int dataType) {
                        return 0;
                    }
                })
                .createMediaSource(mediaItem);

        player.setMediaSource(hlsMediaSource);

//        MediaItem mediaItem = new MediaItem.Builder().setUri(hlsUri).setMimeType(MimeTypes.APPLICATION_M3U8).build();
//        player.setMediaItem(mediaItem);
        player.prepare();
        player.setPlayWhenReady(true);
        player.addListener(this);
        player.seekTo(videoStartingTime);

        previewVideoBufferingStartTime = System.currentTimeMillis();
        startBufferingTimeoutCheck();
    }

    private void setPreviewLikeDislike(boolean isUpdateSelection) {
        if (previewDto != null) {
            tv_like_count.setText(formatCommentCountShort(previewDto.getLikeCount()));
            if (isUpdateSelection) {
                clearSelectLikeDislike(true, false);
                clearSelectLikeDislike(false, false);
                if (previewDto.getLiked() != null) {
                    clearSelectLikeDislike(previewDto.getLiked(), true);
                }
            }
        }
    }

    private void setLikeDislikeCommentVisibility() {
        try {
            if (previewDto != null) {
                if (previewDto.isVideoCompleted()
                        || previewDto.getVideoDetails().getResumePosition() > metadataModel.getShow_like_dislike_comment_min_time_ms()
                        || (previewDto.getVideoDetails().getEndPos() < metadataModel.getShow_like_dislike_comment_min_time_ms() && metadataModel.isShow_like_dislike_for_small_video())) {

                    if (metadataModel.isShow_ml_like_dislike()) {
                        cv_like_dislike_card.setVisibility(View.VISIBLE);
                    }

                } else {
                    cv_like_dislike_card.setVisibility(View.GONE);
                }

                if (metadataModel.isShow_ml_comment()) {
                    cv_comment.setVisibility(View.VISIBLE);
                } else {
                    cv_comment.setVisibility(View.GONE);
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onPlayerError(PlaybackException error) {
        try {
            stopBufferingTimeoutCheck();
            setPreviewThumbnailAfterVideoCompletion();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onPlaybackStateChanged(int playbackState) {
        switch (playbackState) {
            case Player.STATE_BUFFERING:
                if (!hasPreviewPlaybackStarted) {
                    // First-time buffering before playback starts
                    previewThumbnail.setVisibility(View.VISIBLE);
                    previewVideoPlayer.setVisibility(View.GONE);
                    volume.setVisibility(View.GONE);
                } else {
                    // Buffering after playback started
                    previewThumbnail.setVisibility(View.GONE);
                    previewVideoPlayer.setVisibility(View.VISIBLE);
                    volume.setVisibility(View.VISIBLE);
                }
                spinnerVideoDetails.setVisibility(View.VISIBLE);
//                previewVideoPlayer.setVisibility(View.GONE);
//                previewThumbnail.setVisibility(View.VISIBLE);

                previewVideoBufferingStartTime = System.currentTimeMillis();
                startBufferingTimeoutCheck();
                break;

            case Player.STATE_READY:
                hasPreviewPlaybackStarted = true;
                spinnerVideoDetails.setVisibility(View.GONE);
                previewThumbnail.setVisibility(View.GONE);
                centerImage.setVisibility(View.GONE);
                previewVideoPlayer.setVisibility(View.VISIBLE);
                volume.setVisibility(View.VISIBLE);

                previewVideoBufferingStartTime = -1;
                stopBufferingTimeoutCheck();
                break;

            case Player.STATE_ENDED:
                pausePlayer();
                previewVideoPlayer.setVisibility(View.GONE);
                volume.setVisibility(View.GONE);
                previewThumbnail.setVisibility(View.VISIBLE);
                centerImage.setVisibility(View.VISIBLE);

                previewVideoBufferingStartTime = -1;
                stopBufferingTimeoutCheck();
                break;

            case Player.STATE_IDLE:
                videoStartingTime = player.getCurrentPosition();

                previewVideoBufferingStartTime = -1;
                stopBufferingTimeoutCheck();
                break;
        }
    }

//    @Override
//    public void onIsPlayingChanged(boolean isPlaying) {
//        switch (player.getPlaybackState()) {
//            case Player.STATE_BUFFERING:
//                spinnerVideoDetails.setVisibility(View.VISIBLE);
//                previewVideoPlayer.setVisibility(View.GONE);
//                volume.setVisibility(View.GONE);
//                previewThumbnail.setVisibility(View.VISIBLE);
//                break;
//            case Player.STATE_ENDED:
//                pausePlayer();
//                previewVideoPlayer.setVisibility(View.GONE);
//                volume.setVisibility(View.GONE);
//                previewThumbnail.setVisibility(View.VISIBLE);
//                centerImage.setVisibility(View.VISIBLE);
//                break;
//            case Player.STATE_IDLE:
//                videoStartingTime = player.getCurrentPosition();
//                break;
//            case Player.STATE_READY:
//                spinnerVideoDetails.setVisibility(View.GONE);
//                previewThumbnail.setVisibility(View.GONE);
//                centerImage.setVisibility(View.GONE);
//                previewVideoPlayer.setVisibility(View.VISIBLE);
//                volume.setVisibility(View.VISIBLE);
//            default:
//                break;
//        }
//    }

    @Override
    public void setUpPreviewVideo(int position) {
        previewPosition = position;
        MicroLectureDto newPreviewDto = microLectureDetailsDto.getMicroLectureDtos().get(position);
        if (previewDto == null || previewDto != newPreviewDto) {
            continuityLayout.setVisibility(View.GONE);
            previewIsPractice = false;
            previewDto = newPreviewDto;

            lastWatched.setText((position + 1) + ". " + previewDto.getTitle());
            handlePreviewDownloadStatus();

            VideoPreview videoPreview = previewDto.getVideoPreview();
            VideoDetails videoDetails = previewDto.getVideoDetails();

            // Set thumbnail and center play icon
            centerImage.setVisibility(View.VISIBLE);
            setPreviewThumbnailImage(videoPreview.getThumbnail());

            videoUrl = videoPreview.getUrl();
            videoStartingTime = videoPreview.getStartPos();
            videoEndingTime = videoPreview.getEndPos();

//            resetCommentViewModel = true;
            setLikeDislikeCommentVisibility();
            setPreviewLikeDislike(true);

            if (previewDto.isLocked()) {
                centerImage.setImageDrawable(getResources().getDrawable(R.drawable.lock_dark));
            } else {
                centerImage.setImageDrawable(getResources().getDrawable(R.drawable.play_dark));
                mRunnable = () -> {
                    centerImage.setVisibility(View.GONE);
                    setUpPreviewVideoPlayback();
                };
                mHandler.postDelayed(mRunnable, 2000);
            }

            previewLayout.setOnClickListener(v -> {
                if (previewDto.isLocked()) {
                    if (StringUtils.isNotEmpty(previewDto.getLockMessage()) &&
                            MyApplication.getFirebaseRemoteConfig().
                                    getBoolean("to_show_lock_msg_from_data_side_in_micro_lecture")) {
                        lockDialogText.setText(previewDto.getLockMessage());
                        viewPlans.setVisibility(View.GONE);
                    }
                    subscriptionDialog.show();
                } else {
                    playVideo(videoDetails, previewDto, microLectureDetailsDto.getChapterDetails().isBoard(), position);
                }
            });
        }
    }

    @Override
    public void setUpPreviewQuestion(String type, int position, boolean locked, int questionPos) {
        previewPosition = position;
        if (previewDto == null ||
                previewDto != microLectureDetailsDto.getMicroLectureDtos().get(position)) {
            volume.setVisibility(View.GONE);
            previewIsPractice = true;
            previewDto = microLectureDetailsDto.getMicroLectureDtos().get(position);
            handlePreviewDownloadStatus();
            Log.d("MICRO_LECTURE_ACTIVITY", "setUpPreviewQuestionCalled:" + previewDto);
            lastWatched.setText((position + 1) + ". " + microLectureDetailsDto.getMicroLectureDtos().get(position).getTitle());

            VideoPreview videoPreview = microLectureDetailsDto.getMicroLectureDtos().get(position).getVideoPreview();
            setPreviewThumbnailImage(videoPreview.getThumbnail());

            setLikeDislikeCommentVisibility();
            setPreviewLikeDislike(true);
            if (locked) {
                centerImage.setImageDrawable(getResources().getDrawable(R.drawable.lock_dark));
            } else {
                continuityLayout.setVisibility(View.VISIBLE);
                if (position == list.size() - 1)
                    skipToNextLecture.setVisibility(View.GONE);
                questionType = type;
                Log.d(TAG, "setUpPreviewQuestion: " + questionType + "value " +
                        Objects.equals(questionType, LectureContentType.NOTES.name()));
                if (Objects.equals(questionType, LectureContentType.NOTES.name())) {
                    continuityLayoutText.setText(R.string.lecture_notes_preview);
                } else {
                    continuityLayoutText.setText(R.string.Practice_Problems);
                }
                microlectureId = microLectureDetailsDto.getMicroLectureDtos().get(position).getCode();
                microlectureName = microLectureDetailsDto.getMicroLectureDtos().get(position).getTitle();
                currentPos = position;
                currentQuesPos = questionPos;
                isLocked = locked;
            }
        }
    }

    private void setPreviewThumbnailImage(String imageUrl) {
        try {
            if (isFinishing() || isDestroyed())
                return;

            Glide.with(this).clear(previewThumbnail);
            previewThumbnail.setVisibility(View.VISIBLE);
            previewVideoPlayer.setVisibility(View.GONE);

            previewThumbnail.post(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    Glide.with(MicrolectureListActivity.this)
                            .load(imageUrl)
                            .timeout(10000)
                            .error(R.drawable.microlecture_default_thumbnail)
                            .into(previewThumbnail);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void selectedMicrolectureContent(String type, String microlectureId) {
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("chapterId", chapterId);
        objectMap.put("type", type);
        objectMap.put("microLectureId", microlectureId);
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                CommonMethods.getUrlParam(MICRO_LECTURE_SELECTION, objectMap), null,
                response -> {
                    Log.d(TAG, "selectedMicrolectureContent response come: ");
                }, new VolleyGlobalErrorListener(this) {
            @Override
            public void postError(VolleyError error) {
                Log.e("postError: ", error.getMessage());
            }
        });
        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
    }

    private void setMicroLectureLikeDislike(LikeDislikeEnum isLike, String chapterId, String microLectureId) {
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("chapterId", chapterId);
        objectMap.put("liked", isLike.name());
        objectMap.put(PreferenceKeys.MICROLECTURE_ID.getKey(), microLectureId);
        cv_like_dislike_card.setEnabled(false);
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                CommonMethods.getUrlParam(POST_FEEDBACK_LIKE, objectMap), null,
                response -> {
                    Log.d(TAG, "Response: " + response.toString());
                    TypeToken<LikeDislikeResponse> token = new TypeToken<LikeDislikeResponse>() {
                    };
                    LikeDislikeResponse likeDislikeResponse = gson.fromJson(response.toString(), token);
                    if (likeDislikeResponse.isSuccess()) {
                        if (previewDto != null && previewDto.getId().equals(microLectureId)) {
                            previewDto.setLiked(isLike.getIsLike());
                            setPreviewLikeDislike(false);
                        }
                    }
                    cv_like_dislike_card.setEnabled(true);
                }, error -> {
            Log.d(TAG, "Error: " + error.toString());
            cv_like_dislike_card.setEnabled(true);
            setPreviewLikeDislike(true);
            Toast.makeText(MicrolectureListActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
        });
        myJsonObjectRequest.setTag(LIKE_DISLIKE_VOLLEY_TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
    }

    private void startHandler() {
        if (playerHandler == null) playerHandler = new Handler();

        playerRunnable = () -> {
            if (player != null && videoEndingTime != 0L && player.getCurrentPosition() >= videoEndingTime) {
                setPreviewThumbnailAfterVideoCompletion();
            } else {
                playerHandler.postDelayed(playerRunnable, 200); // Keep checking
            }
        };

        playerHandler.postDelayed(playerRunnable, 200);
    }

    private void setPreviewThumbnailAfterVideoCompletion() {
        try {

            previewVideoBufferingStartTime = -1;
            removeHandlerCallbacks();

            player.stop();
            previewVideoPlayer.setVisibility(View.GONE);
            volume.setVisibility(View.GONE);
            previewThumbnail.setVisibility(View.VISIBLE);
            centerImage.setVisibility(View.VISIBLE);
            spinnerVideoDetails.setVisibility(View.GONE);
            previewCompleted = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setUpPreviewVideoPlayback() {
        if (videoUrl == null) return;

        previewThumbnail.setVisibility(View.GONE);
        previewVideoPlayer.setVisibility(View.VISIBLE);
        volume.setVisibility(View.VISIBLE);

        initializePlayer();
        buildMediaSource(Uri.parse(videoUrl));
        startHandler();
    }

    private void setUpPreviewVideo() {
        volume.setVisibility(View.VISIBLE);
        initializePlayer();
        startHandler();
        if (videoUrl == null) {
            return;
        }
        buildMediaSource(Uri.parse(videoUrl));
    }

    @Override
    public void startVideo(int position) {
        if (isDestroyed() || isFinishing()) {
            return;
        }
        if (position < list.size() && position < microLectureDetailsDto.getMicroLectureDtos().size()) {
            if (list.get(position).isLocked()) {
                if (StringUtils.isNotEmpty(list.get(position).getLockMessage())
                        && MyApplication.getFirebaseRemoteConfig().
                        getBoolean("to_show_lock_msg_from_data_side_in_micro_lecture")) {
                    lockDialogText.setText(list.get(position).getLockMessage());
                    viewPlans.setVisibility(View.GONE);
                }
                subscriptionDialog.show();
            } else {
                selectedMicrolectureContent(LectureContentType.VIDEO.name(), list.get(position).getId());
                updateSelectionLocally(true, false, list.get(position).getId(), LectureContentType.VIDEO.name());
                if (openVideo) {
                    screenLoader = new CustomFullScreenLoader(this);
                    screenLoader.showLoader();
                }
                VideoDetails videoDetails = microLectureDetailsDto.getMicroLectureDtos().get(position).getVideoDetails();
                Log.d("VideoDetails", "Position " + position + "Detail = " + videoDetails.toString());
                playVideo(videoDetails, microLectureDetailsDto.getMicroLectureDtos().get(position), microLectureDetailsDto.getChapterDetails().isBoard(), position);
            }
        }
    }


    /**
     * Updates the micro-lecture selection locally.
     *
     * @param isLecture          indicates whether the selected item is a lecture.
     * @param isPractice         indicates whether the selected item is a practice.
     * @param microLectureId     the ID of the micro-lecture being selected.
     * @param lectureContentType the type of content (e.g.,VIDEO,NUMERICAL_QUESTION, ,
     *                           NOTES,LONG_ANSWERS, etc) associated with the micro-lecture.
     */

    private void updateSelectionLocally(boolean isLecture, boolean isPractice, String microLectureId, String lectureContentType) {

        MyApplication.saveData(PreferenceKeys.MICROLECTURE_DATA_RELOAD.getKey(), false);
        if (selectionMicroLectureEntity != null) {
            Log.d(TAG, "updateSelectionLocally: " + selectionMicroLectureEntity);
            microLectureViewModel.updateSelection(microLectureId, isLecture, isPractice, chapterId, lectureContentType);
            MyApplication.saveData(PreferenceKeys.MICROLECTURE_DATA_RELOAD.getKey(), true);
        } else if (StringUtils.isNotEmpty(chapterId)) {
            selectionMicroLectureEntity = new MicroLectureEntity(chapterId, microLectureId,
                    MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()), lectureContentType, isLecture, isPractice);
            microLectureViewModel.insert(selectionMicroLectureEntity);
            Log.d(TAG, "updateSelectionLocally isserted : " + selectionMicroLectureEntity);

            MyApplication.saveData(PreferenceKeys.MICROLECTURE_DATA_RELOAD.getKey(), true);
        }
    }

    @Override
    public void initiateVideoDownload(int position) {
        downloadDto = list.get(position);
        if (!downloadDto.isLocked()) {
            downloadVideoQualityDtos.clear();
            downloadVideoQualityDtos.addAll(downloadDto.getVideoDetails().getQualityDtos());
            checkNotificationPermissionForDownload();
        } else {
            subscriptionDialog.show();
        }
    }

    @Override
    public void openBottomSheet(int pos) {
        cancelDownloadPosition = pos;
        if (cancelDownloadBottomSheet != null) {
            cancelDownloadBottomSheet = null;
        }
        cancelDownloadBottomSheet = new CancelDownloadBottomSheet(this, this,
                list.get(pos).getTitle(), list.get(pos).getDownloadProgress(), list.get(pos).getId());
        cancelDownloadBottomSheet.setCancelable(true);
        cancelDownloadBottomSheet.show(getSupportFragmentManager(), "");
        observe(list.get(pos).getId());
    }

    public void lecturesNotesOpen(MicroLectureDto microLectureDto, String url) {
        try {
            if (microLectureDto.isLocked() || microLectureDto.isPdfLocked()) {
                Intent intent = SubscriptionNavigationHelper.getSubscriptionPageIntent(this, SubscriptionPopupType.NOTES.name(), RedirectionSource.MICRO_LECTURE_LOCKED_POPUP);
                startActivity(intent);
                return;
            }
            if (!StringUtils.isEmpty(url)) {
                selectedMicrolectureContent(LectureContentType.NOTES.name(), microLectureDto.getId());
                openPdf(url, microLectureDto.getTitle());
            }

        } catch (Exception ignored) {
        }
    }

    @Override
    public void onNoteDownload(MicroLectureDto microLectureDto, String notesUrl) {
        try {
            if (microLectureDto.isLocked()) {
                subscriptionDialog.show();
                return;
            }
            if (!StringUtils.isEmpty(notesUrl)) {
                if (CommonMethods.isOnline(this)) {

                    NoteEntity noteEntity = new NoteEntity(
                            microLectureDto.getId(),
                            microLectureDetailsDto.getChapterDetails().getSubjectId(),
                            microLectureDetailsDto.getChapterDetails().getSubjectName(),
                            microLectureDto.getTitle(),
                            "MICRO_LECTURE",
                            microLectureDto.getPdfUrl(),
                            microLectureDto.isLocked(),
                            microLectureDto.getPdfPageCount(), 0, "pdf",
                            NoteEntity.STATUS_PENDING, 0, null,
                            MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey())
                    );
                    noteEntity.setUserId(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
                    noteEntity.setNoteType(microLectureDetailsDto.getChapterDetails().getNotesType());
                    notesViewModel.insert(noteEntity);
                    PdfUtils.startPdfDownload(this, noteEntity);

                    notesViewModel.getLiveNotesById(microLectureDto.getId())
                            .observe(this, entity -> {
                                if (entity != null) {
                                    updateNoteModelAndNotify(microLectureDto, entity);
                                }
                            });
                } else {
                    CommonMethods.showNegativeSnackbar(ParentLayout, getString(R.string.NoInternetConnection), 3000);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateNoteModelAndNotify(MicroLectureDto model, NoteEntity entity) {
//        model.setNotesUrl(entity.getNotesUrl());
//        model.setLocked(entity.isLocked());
        model.setPdfDownloadComplete(entity.getStatus().equals(NoteEntity.STATUS_COMPLETED));
        model.setPdfProgress(entity.getProgress());
        model.setPdfPageCount(entity.getPageCount());
        model.setPdfStatus(entity.getStatus());

        if (microlectureAdapter != null) {
            int position = microlectureAdapter.getList().indexOf(model);
            if (position != -1) {
                microlectureAdapter.notifyItemChanged(position);
            }
        }
    }

    private void observe(String microLectureId) {
        if (cancelDownloadLiveData != null)
            cancelDownloadLiveData.removeObservers(this);
        cancelDownloadLiveData = MyApplication.getInstance().getMediaViewModel().getMediaByLectureId(microLectureId);
        cancelDownloadLiveData.observe(this, mediaEntity -> {
            cancelDownloadEntity = mediaEntity;
            if (mediaEntity != null) {
                if (mediaEntity.getStatus().equals(DownloadStatus.DOWNLOAD_COMPLETED.name())) {
                    if (cancelDownloadBottomSheet != null) {
                        cancelDownloadBottomSheet.dismiss();
                        cancelDownloadBottomSheet = null;
                    }
                } else if (cancelDownloadBottomSheet != null && cancelDownloadBottomSheet.isVisible()) {
                    cancelDownloadBottomSheet.updateProgress(mediaEntity.getProgress());
                }
            }
        });
    }

    public void clearSpecificCache(String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .signature(new ObjectKey(System.currentTimeMillis()))
                .submit();
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onCancelClick(String microLectureId) {
        try {
            if (cancelDownloadEntity != null) {
                clearSpecificCache(cancelDownloadEntity.getThumbnailUrl());
                mediaViewModel.deleteByUrl(cancelDownloadEntity.getUrl());
                mediaViewModel.deleteMicroLectureEntry(microLectureId);
                DemoUtil.getDownloadManager(this).removeDownload(cancelDownloadEntity.getUrl());
                if (cancelDownloadBottomSheet != null) {
                    cancelDownloadBottomSheet.dismiss();
                    cancelDownloadBottomSheet = null;
                }

                list.get(cancelDownloadPosition).setDownloadProgress(0);

                Map<String, String> params = new HashMap<>();
                params.put("isLocked", previewDto != null ? String.valueOf(previewDto.isLocked()) : "false");
                params.put("microlectureId", previewDto != null ? previewDto.getId() : "");
                params.put("chapterId", chapterId);
                params.put("resolution", currentDownloadVideoQuality.getQuality());
                params.put("size", currentDownloadVideoQuality.getSize());
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.ml_cancel_download_click.getActionName(), params);

            }
        } catch (Exception ignored){
        }
    }

    @Override
    public void onCloseClick() {
        if (cancelDownloadBottomSheet != null) {
            cancelDownloadBottomSheet.dismiss();
            cancelDownloadBottomSheet = null;
        }
    }

    private void playVideo(VideoDetails videoDetails, MicroLectureDto microLectureDto, boolean board, int pos) {
        MyApplication.saveMicroLectureList(getString(R.string.micro_lecture_and_practice_list), list);
        @OptIn(markerClass = UnstableApi.class) Intent intent = new Intent(this, VideoPlayerActivity.class);
        MicroLectureContentDto microLectureContentDto = new MicroLectureContentDto();
        microLectureContentDto.setDefaultStartTime(Math.toIntExact(videoDetails.getStartPos()));
        microLectureContentDto.setVideoCompletionEndPos(Math.toIntExact(videoDetails.getEndPos()));
        microLectureContentDto.setVideoCompletionStartPos(Math.toIntExact(videoDetails.getEndPos()));
        microLectureContentDto.setContentUrl(videoDetails.getUrl());
        microLectureContentDto.setVideoUrl(videoDetails.getUrl());
        microLectureContentDto.setId(microLectureDto.getCode());
        microLectureContentDto.setChapterId(chapterId);
        microLectureContentDto.setChapterName(chapterName);
        microLectureContentDto.setTitle(microLectureDto.getTitle());
        microLectureContentDto.setResumePosition(videoDetails.getResumePosition());
        microLectureContentDto.setLectureContentType(LectureContentType.VIDEO);
        microLectureContentDto.setVideoQualityDtos(videoDetails.getQualityDtos());
        microLectureContentDto.setLanguageAndVideoUrlDtos(videoDetails.getLanguageAndVideoUrlDtos());
        microLectureContentDto.setLikeCount(microLectureDto.getLikeCount());
        microLectureContentDto.setLiked(microLectureDto.getLiked());
        microLectureContentDto.setLikeDislikeVisible(true);

        intent.putExtra(PreferenceKeys.BOARD.getKey(), board);
        intent.putExtra("resumePosition", microLectureContentDto.getResumePosition());
        intent.putExtra(getResources().getString(R.string.passed_obj), microLectureContentDto);
        intent.putExtra("position", Integer.parseInt(String.valueOf(videoDetails.getResumePosition())));
        intent.putExtra("shouldNotResume", !videoDetails.isResume());
        intent.putExtra("startAtPosition", true);
        intent.putExtra("isAskDoubtActivity", false);
        intent.putExtra("video_uri", videoDetails.getUrl());
        intent.putExtra(PreferenceKeys.VIDEO_STARTING.getKey(), videoDetails.getStartPos());
        intent.putExtra("video_ending", videoDetails.getEndPos());
        intent.putExtra("chapterId", chapterId);
        intent.putExtra("subjectCode", subjectCode);
        intent.putExtra("selectedSubject", selectedSubject);
        intent.putExtra("selected_chapter_id", microLectureDto.getCode());
        intent.putExtra("replay", microLectureDto.getVideoDetails().getProgress() == 100);
        intent.putExtra("currentMicroLecturePos", pos);
//        intent.putExtra("isBatchOverview", true);
//        intent.putExtra("skipToPosition", 15455000);
        MyApplication.saveData(PreferenceKeys.MICROLECTURE_DATA_RELOAD.getKey(), true);

        Bundle bundle = new Bundle();
        bundle.putLong("skipToPosition", skipToPositionMs);
        bundle.putBoolean("isBatchOverview", isBatchOverview);

        intent.putExtras(bundle);

        // Forward personalised-path context so the video player can report watch progress.
        intent.putExtra("nodeId", getIntent().getStringExtra("nodeId"));
        intent.putExtra("learningPathId", getIntent().getStringExtra("learningPathId"));
        intent.putExtra("batchPathId", getIntent().getStringExtra("batchPathId"));
        intent.putExtra("childTaskId", getIntent().getStringExtra("childTaskId"));

        Map<String, String> params = new HashMap<>();
        params.put("chapterName", chapterName);
        params.put("subjectCode", subjectCode);
        params.put("microlectureName", microLectureDto.getTitle());
        params.put("microlectureId", microLectureDto.getId());

        MyApplication.logFirebaseBundleCreation(FirebaseEvents.micro_lecture_clicked.getActionName(), params);
        if (videoDetails.getUrl() != null) {
            destroyHowTo();
            startActivity(intent);
        }
    }

    @Override
    public void onPracticeClick(String type, int position, boolean locked, int questionPos) {
        currentPos = position;
        currentQuesPos = questionPos;
        isLocked = locked;
        questionType = type;
        try {
            openPractice();
        } catch (Exception e) {
            Log.e("ERR", "onPracticeClick: Unable to open practice problem");
        }
    }

    private void openPractice() {
        if (isLocked) {
            if (getApplicationContext() != null) {
                if (StringUtils.isNotEmpty(list.get(currentPos).getLockMessage())) {
                    lockDialogText.setText(list.get(currentPos).getLockMessage());
                    viewPlans.setVisibility(View.GONE);
                }
                subscriptionDialog.show();
            }
        } else if (currentPos < microLectureDetailsDto.getMicroLectureDtos().size()) {
            microlectureId = microLectureDetailsDto.getMicroLectureDtos().get(currentPos).getCode();
            microlectureName = microLectureDetailsDto.getMicroLectureDtos().get(currentPos).getTitle();
            LectureContentType lectureContentType = LectureContentType.valueOf(questionType);

            updateSelectionOfMicrolecture(lectureContentType, microlectureId, microlectureName);


            Log.d(TAG, "openPractice: " + "question type-" + questionType + "url"
                    + list.get(currentPos).getQuestions().get(currentQuesPos).getPdfUrl() +
                    "skip" + list.get(currentPos).getQuestions().get(currentQuesPos).isSkip() + "status pdf"
                    + list.get(currentPos).getQuestions().get(currentQuesPos).getStatus());
            switch (lectureContentType) {
                case CONCEPTUAL_QUESTION:
                case NUMERICAL_QUESTION:
                case MULTIPLE_CHOICE_QUESTION:
                case TRUE_FALSE:
                case ONE_WORD_ANSWER:
                case FILL_IN_THE_BLANKS:
                case VERY_SHORT_ANSWERS:
                case MATCH_THE_COLUMN:
                case LONG_ANSWERS:
                case SHORT_ANSWERS:
                    if (microLectureDetailsDto.getMicroLectureDtos().get(currentPos).getQuestions().get(currentQuesPos).getStatus().equalsIgnoreCase(PracticeStatus.COMPLETED.name())) {
                        if (getApplicationContext() != null) {
                            actionDialog.show();
                        }
                    } else {
                        review = false;
                        reattempt = false;
                        getQuestionList(microlectureId, questionType);
                    }
                    break;
                default:
                    getQuestionList(microlectureId, questionType);
            }

        }
    }

    private void updateSelectionOfMicrolecture(LectureContentType lectureContentType, String microlectureId, String microlectureName) {
        if ("hi".equals(MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()))) {
            microlectureId = microlectureId + "HINDI";
        }
        updateSelectionLocally(false, true, microlectureId, lectureContentType.name());

        Map<String, String> params = new HashMap<>();
        params.put("questionType", lectureContentType.name());
        params.put("subjectCode", subjectCode);
        params.put("chapterName", chapterName);
        params.put("microlectureName", microlectureName);

        MyApplication.logFirebaseBundleCreation(FirebaseEvents.Practice_question_clicked.getActionName(), params);
        selectedMicrolectureContent(lectureContentType.name(), microlectureId);
    }

    @Override
    protected void onResume() {
        if (returningFromPdfViewer) {
            recreate();
            returningFromPdfViewer = false;
        }
        Log.d(TAG, "onResume called : ");
        super.onResume();
//        resetCommentViewModel = true;
        if (howToVideoView != null && howToVideoView.videoView != null) {
            howToVideoView.resumePlayer();
        }
        if (MyApplication.getBoolData(PreferenceKeys.MICROLECTURE_DATA_RELOAD.getKey())) {
            MyApplication.saveData(PreferenceKeys.MICROLECTURE_DATA_RELOAD.getKey(), false);
            dataLoaded = false;
            createNewRequest();
            openVideo = false;
            microlectureAdapter.setOpenVideo(openVideo);
            if (!onCreateCalled) {
                getMicroLectureList();
            }
            if (MyApplication.getBoolData(getString(R.string.progressionIsLocked))) {
                if (getApplicationContext() != null) {
                    if (StringUtils.isNotEmpty(MyApplication.getStringData(getString(R.string.progressionLockedMessage)))) {
                        lockDialogText.setText(MyApplication.getStringData(getString(R.string.progressionLockedMessage)));
                        viewPlans.setVisibility(View.GONE);
                    }
                    subscriptionDialog.show();
                }
                MyApplication.saveData(getString(R.string.progressionIsLocked), false);
                MyApplication.saveData(getString(R.string.progressionLockedMessage), "");
            }
        } else if (!previewIsPractice) {
//            previewVideoPlayer.setVisibility(View.GONE);
            volume.setVisibility(View.GONE);
            centerImage.setVisibility(View.VISIBLE);
            if (previewDto != null && !previewDto.isLocked()) {
                centerImage.setVisibility(View.VISIBLE);
                previewThumbnail.setVisibility(View.VISIBLE);
                mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (!previewCompleted) {
                            resumePlayer();
                        }
                    }
                };
                mHandler.postDelayed(mRunnable, 2000);
                startHandler();
            }
        }

        getAppReviewPopUpStatus();

    }

    private void resumePlayer() {
        if (player != null) {
            player.setPlayWhenReady(true);
            player.getPlaybackState();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (howToVideoView != null && howToVideoView.videoView != null)
            howToVideoView.pausePlayer();
        if (cancelDownloadBottomSheet != null) {
            cancelDownloadBottomSheet.dismiss();
            cancelDownloadBottomSheet = null;
        }
        if (screenLoader != null)
            screenLoader.hideLoader();
        removeHandlerCallbacks();
        pausePlayer();
        onCreateCalled = false;
    }

    @Override
    protected void onDestroy() {
        MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll(TAG);
        super.onDestroy();
        removeHandlerCallbacks();
        releasePlayer();
        if (playStoreDialog != null && playStoreDialog.isShowing()) {
            playStoreDialog.dismiss();
        }
        if (noLecturedialog != null && noLecturedialog.isShowing()) {
            noLecturedialog.dismiss();
        }
        if (videoDownloadDialog != null && videoDownloadDialog.isShowing()) {
            videoDownloadDialog.dismiss();
        }
        if (deviceMemoryReached != null && deviceMemoryReached.isShowing()) {
            deviceMemoryReached.dismiss();
        }
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow(); // Shut down executor to cancel ongoing tasks
        }
        if (screenLoader != null) {
            screenLoader.hideLoader();
        }
        MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll("");
        MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll(LIKE_DISLIKE_VOLLEY_TAG);
        MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll(COMMENT_VOLLEY_TAG);
        MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll(DOWNLOAD_LECTURE_TAG);
    }

    private void destroyHowTo() {
        if (how_to_video_view != null) {
            how_to_video_view.setVisibility(View.GONE);
        }
        if (howToVideoView != null)
            howToVideoView.remove();
    }

    private void getAppReviewPopUpStatus() {
        try {
            Log.d(TAG, "----> Completed Lectures: " + MyApplication.getCompletedMicroLectureList().size());
            if (MyApplication.getCompletedMicroLectureList().size() >= completedLectureLimitForReviewPopUp) {
                CommonAPIs.getAppReviewStatusAPI(this, TAG, new OnFeedbackStatusAPIListener() {
                    @Override
                    public void onSuccess(boolean status, AppFeedbackStatusModel appFeedbackStatusModel) {
                        if (appFeedbackStatusModel.isShow_feedback_dialog() && !appFeedbackStatusModel.isReview_submitted()) {
                            CommonMethods.initAppReviewDialog(MicrolectureListActivity.this);
                        }
                        MyApplication.saveCompletedMicroLectureList(new ArrayList<>());
                    }

                    @Override
                    public void onException(boolean status, AppFeedbackStatusModel appFeedbackStatusModel) {

                    }

                    @Override
                    public void onError(boolean status, AppFeedbackStatusModel appFeedbackStatusModel) {

                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void removeHandlerCallbacks() {
        if (mHandler != null)
            mHandler.removeCallbacksAndMessages(null);
        if (playerHandler != null)
            playerHandler.removeCallbacksAndMessages(null);
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    private void pausePlayer() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player.getPlaybackState();
        }
    }

    private void setSlimChart(int progress) {
//        Log.d(TAG, "---> Progress: "+progress);
        chapterTitleLayout.setVisibility(View.VISIBLE);
        chapterProgress.setVisibility(View.VISIBLE);
        progressText.setText(progress + "%");
        int[] colors = new int[3];
        colors[0] = getResources().getColor(R.color.mock_light_blue);
        colors[1] = getResources().getColor(R.color.subscription_blue);
        slimChart.setColors(colors);
        final float[] stats = new float[3];
        stats[0] = 100;
        stats[1] = progress;
        slimChart.setStats(stats);
        slimChart.setRoundEdges(true);
        slimChart.setStartAnimationDuration(1000);
    }

    private void createNewRequest() {
        microlectureAdapter.setReachedPreview(false);
        shimmerLayout.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);
        fl_scroll_view.setVisibility(View.GONE);
    }

    private void openQuestion(FactoryShipQnADto factoryShipQnADto) {
        LectureContentType type = LectureContentType.valueOf(factoryShipQnADto.getType());
        switch (type) {
            case CONCEPTUAL_QUESTION:
            case NUMERICAL_QUESTION:
            case MULTIPLE_CHOICE_QUESTION:
            case TRUE_FALSE:
            case ONE_WORD_ANSWER:
            case FILL_IN_THE_BLANKS:
            case VERY_SHORT_ANSWERS:
            case MATCH_THE_COLUMN:
            case LONG_ANSWERS:
            case SHORT_ANSWERS:
                openPracticeQuestions(type);
                break;
            default:
                Intent intent = new Intent(this, BoardQuestionActivity.class);

                ArrayList<String> optionList = new ArrayList<>();
                MicroLectureContentDto microLectureContentDto = new MicroLectureContentDto();
                microLectureContentDto.setOptions(factoryShipQnADto.getOptions());
                microLectureContentDto.setTitle(factoryShipQnADto.getTitle());
                microLectureContentDto.setId(factoryShipQnADto.getCode());
                microLectureContentDto.setChapterId(chapterId);
                microLectureContentDto.setAnswerEx(factoryShipQnADto.getAnswerEx());
                microLectureContentDto.setLectureContentType(LectureContentType.getLectureContentType(factoryShipQnADto.getType()));
                microLectureContentDto.setAnswer(factoryShipQnADto.getAnswer());
                microLectureContentDto.setAnswerVideo(factoryShipQnADto.getAnswerVideo());
                microLectureContentDto.setQuestionImage(factoryShipQnADto.getImageQuestion());
                microLectureContentDto.setAnswerSolImage(factoryShipQnADto.getAnswerSolImage());
                microLectureContentDto.setQuestionLevel(StringUtils.isNotEmpty(factoryShipQnADto.getQuestionLevel()) ? QuestionLevel.valueOf(factoryShipQnADto.getQuestionLevel()) : QuestionLevel.MEDIUM);
                microLectureContentDto.setQuestion(factoryShipQnADto.getQuestion());
                microLectureContentDto.setOptions(factoryShipQnADto.getOptions());
                microLectureContentDto.setQuestionId(factoryShipQnADto.getQuestionId());
                microLectureContentDto.setAnswers(factoryShipQnADto.getAnswers());
                microLectureContentDto.setDefaultStartTime((int) factoryShipQnADto.getDefaultStartTime());
                microLectureContentDto.setVideoCompletionStartPos((int) factoryShipQnADto.getVideoCompletionStartPos());
                microLectureContentDto.setVideoCompletionEndPos((int) factoryShipQnADto.getVideoCompletionEndPos());
                microLectureContentDto.setAnswerImage(factoryShipQnADto.getAnswerImage());
                microLectureContentDto.setMarks(factoryShipQnADto.getMarks());
                microLectureContentDto.setUserSolution(factoryShipQnADto.getUserAnswer());
                microLectureContentDto.setVideoUrl(factoryShipQnADto.getVideoUrl());
                microLectureContentDto.setWordLimit(factoryShipQnADto.getWordLimit());
                microLectureContentDto.setWordLimitApplicable(factoryShipQnADto.isWordLimitApplicable());

                intent.putExtra(getResources().getString(R.string.passed_obj), microLectureContentDto);
                intent.putExtra(PreferenceKeys.POSITION.getKey(), String.valueOf(firstUnattemptedIndex + 1));
                intent.putExtra("Title", factoryShipQnADto.getTitle());
                intent.putExtra("Chapter", chapterName);
                intent.putExtra("Id", chapterId);
                intent.putExtra("Question", factoryShipQnADto.getQuestion().trim());
                intent.putExtra("lastPos", qnADtoList.size());
                intent.putExtra("currentPos", firstUnattemptedIndex);
                intent.putParcelableArrayListExtra("questionList", qnADtoList);
                if (factoryShipQnADto.getAnswerEx() == null) {
                    intent.putExtra("Explanation", "");
                } else {
                    intent.putExtra("Explanation", factoryShipQnADto.getAnswerEx().trim());
                }
                for (int i = 0; i < factoryShipQnADto.getOptions().size(); i++) {
                    optionList.add(factoryShipQnADto.getOptions().get(i).trim());
                }
                intent.putStringArrayListExtra("Options", optionList);
                if (factoryShipQnADto.getImageQuestion() != null)
                    intent.putExtra("ImageQuestion", factoryShipQnADto.getImageQuestion());
                else
                    intent.putExtra("ImageQuestion", "");
                if (factoryShipQnADto.getAnswer() == null)
                    CommonMethods.showNegativeSnackbar(recyclerView, getString(R.string.NoValidQuestionFound) + factoryShipQnADto.getAnswer());
                else
                    intent.putExtra("Answer", factoryShipQnADto.getAnswer().trim());

                String url = factoryShipQnADto.getAnswerSolImage();
                if (url != null)
                    intent.putExtra("AnswerImage", url);
                else
                    intent.putExtra("AnswerImage", "");
                // for further questions---
                intent.putExtra("Subject", chapterId);
                intent.putExtra("Chapter", chapterName);
                if (factoryShipQnADto.getOptionType() != null)
                    intent.putExtra("QuesType", factoryShipQnADto.getOptionType());
                else
                    intent.putExtra("QuesType", "TEXT");
                if (factoryShipQnADto.getQuestion() == null || (optionList.size() == 0 &&
                        (LectureContentType.valueOf(factoryShipQnADto.getType()) == LectureContentType.TRUE_FALSE ||
                                LectureContentType.valueOf(factoryShipQnADto.getType()) == LectureContentType.MATCH_THE_COLUMN)))
                    CommonMethods.showNegativeSnackbar(recyclerView, getString(R.string.NoValidQuestionFound));
                else {
                    destroyHowTo();
                    startActivityForResult(intent, your_reques_code);
                }
        }
    }

    private void openPracticeQuestions(LectureContentType type) {
        if (MyApplication.isCompetitiveProfile() && isAllowedLectureContentType(type)) {
            openWebViewPracticeQuestions(type);
            return;
        }
        MyApplication.saveMicroLectureList(getString(R.string.micro_lecture_and_practice_list), list);
        Intent intent = new Intent(MicrolectureListActivity.this, PracticeProblemsActivity.class);
        intent.putParcelableArrayListExtra("factoryShipQnADtoList", qnADtoList);
        intent.putExtra("microLectureId", microlectureId);
        intent.putExtra("microLectureName", microlectureName);
        intent.putExtra("chapterId", chapterId);
        intent.putExtra("review", review);
        intent.putExtra("reattempt", reattempt);
        intent.putExtra("firstUnattemptedIndex", firstUnattemptedIndex);
        intent.putExtra("currentMicroLecturePos", currentPos);
        intent.putExtra("practicePosInCurrentMicroLecture", currentQuesPos);
        intent.putExtra("subjectCode", subjectCode);
        intent.putExtra("selectedSubject", selectedSubject);
        intent.putExtra("isBoard", microLectureDetailsDto.getChapterDetails().isBoard());
        intent.putExtra("type", questionType);
        Log.d(TAG, "openPracticeQuestions: " + CommonMethods.getIntentParams(intent));
        destroyHowTo();
        startActivity(intent);
    }

    public static boolean isAllowedLectureContentType(LectureContentType type) {
        try {
            String allowedLecturetypes = MyApplication.getFirebaseRemoteConfig().getString("competitive_microlecture_practise_web_view_allowed_types_" + MyApplication.getStringData(PreferenceKeys.SELECTED_COURSE_ID_LOCAL.getKey()));
            String[] arr = allowedLecturetypes.split(",");
            for (String allowedType : arr) {
                if (allowedType.equals(type.name()))
                    return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private void openWebViewPracticeQuestions(LectureContentType lectureContentType) {
        MyApplication.saveMicroLectureList(getString(R.string.micro_lecture_and_practice_list), list);
        Intent intent = new Intent(MicrolectureListActivity.this, MicrolecturePractiseWebViewActivity.class);
        intent.putParcelableArrayListExtra("factoryShipQnADtoList", qnADtoList);
        intent.putExtra("microLectureId", microlectureId);
        intent.putExtra("microLectureName", microlectureName);
        intent.putExtra("chapterId", chapterId);
        intent.putExtra("review", review);
        intent.putExtra("reattempt", reattempt);
        intent.putExtra("firstUnattemptedIndex", firstUnattemptedIndex);
        intent.putExtra("currentMicroLecturePos", currentPos);
        intent.putExtra("practicePosInCurrentMicroLecture", currentQuesPos);
        intent.putExtra("subjectCode", subjectCode);
        intent.putExtra("selectedSubject", selectedSubject);
        intent.putExtra("isBoard", microLectureDetailsDto.getChapterDetails().isBoard());
        intent.putExtra("type", lectureContentType != null ? lectureContentType.name() : "");
        destroyHowTo();
        startActivity(intent);
    }


    private void setUpActionPopup() {
        actionDialog = new Dialog(this);
        actionDialog.setContentView(R.layout.action_popup_layout);
        actionDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
        actionDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        actionDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        actionDialog.setCancelable(false);
        ImageView close = actionDialog.findViewById(R.id.close_btn);
        TextView title = actionDialog.findViewById(R.id.title);
        TextView subtitle = actionDialog.findViewById(R.id.subtitle);
        TextView btn1_text = actionDialog.findViewById(R.id.btn1_text);
        TextView btn2_text = actionDialog.findViewById(R.id.btn2_text);
        CardView btn1 = actionDialog.findViewById(R.id.btn1);
        CardView btn2 = actionDialog.findViewById(R.id.btn2);
        LottieAnimationView animationView = actionDialog.findViewById(R.id.animation);
        animationView.setAnimation(R.raw.review_or_reattempt);
        close.setOnClickListener(v -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.micro_lec_close_btn_click.getActionName());
            actionDialog.dismiss();
        });
        btn1.setOnClickListener(v -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.micro_lec_review_reattempt_click.getActionName());
            actionDialog.dismiss();
            reattempt = true;
            review = false;
            getQuestionList(microlectureId, questionType);
        });
        btn2.setOnClickListener(v -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.micro_lec_view_solution_click.getActionName());
            actionDialog.dismiss();
            reattempt = false;
            review = true;
            getQuestionList(microlectureId, questionType);
        });

        title.setText(getResources().getString(R.string.ready_for_another_Try));
        subtitle.setText(getResources().getString(R.string.want_to_review));
        btn1_text.setText(getResources().getString(R.string.ReattemptTest));
        btn2_text.setText(getResources().getString(R.string.view_solution));
    }

    private void getQuestionList(String microLectureCode, String type) {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        Log.d("QuestionList", "called");

        try {
            // Show loader safely
            screenLoader.showLoader();

            Map<String, Object> map = new HashMap<>();
            map.put("microLectureCode", microLectureCode);
            map.put("type", type);
            map.put("boldFormatting", true);
            Log.d(TAG, "Request: " + map.toString());

            MyJsonArrayRequest myJsonArrayRequest = new MyJsonArrayRequest(
                    Request.Method.GET,
                    CommonMethods.getUrlParam(MICRO_LECTURE_PRACTICE_LIST_BY_TYPE, map),
                    null,
                    response -> {
                        TypeToken<List<FactoryShipQnADto>> token = new TypeToken<List<FactoryShipQnADto>>() {
                        };
                        Log.d(TAG, "Response: " + response.toString());
                        qnADtoList = gson.fromJson(response.toString(), token.getType());
                        setUpQuestion();
                        screenLoader.hideLoader(); // hide after success
                    },
                    new VolleyGlobalErrorListener(this) {
                        @Override
                        public void postError(VolleyError error) {
                            screenLoader.hideLoader(); // hide after error
                            Log.e("postError: ", error.getMessage());
                        }
                    }
            );

            myJsonArrayRequest.setTag(TAG);
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonArrayRequest);

        } catch (Exception e) {
            Log.e("ErrorQuestionList", e.getMessage(), e);
            screenLoader.hideLoader(); // hide in case of exception
        }
    }

    private void setUpQuestion() {
        if (!qnADtoList.isEmpty()) {
            firstUnattemptedIndex = 0;
            for (int i = 0; i < qnADtoList.size(); i++) {
                if (!qnADtoList.get(i).isCompleted()) {
                    firstUnattemptedIndex = i;
                    break;
                }
            }

            if (openPractice) {
                for (int i = 0; i < qnADtoList.size(); i++) {
                    Log.d("qnsIdFoundList", qnADtoList.get(i).getQuestionId());
                    if (qnADtoList.get(i).getQuestionId().equals(qnsId)) {
                        openQuestion(qnADtoList.get(i));
                        openPractice = false;
                        break;
                    }
                }
            } else {
                openQuestion(qnADtoList.get(firstUnattemptedIndex));
            }
        }

    }

    public static String formatCommentCountShort(long count) {
        String formatted;

        if (count < 1_000) {
            formatted = String.valueOf(count);
        } else if (count < 1_00_000) {
            formatted = String.format(Locale.US, "%.1fk", count / 1_000.0);
        } else if (count < 1_00_00_000) {
            formatted = String.format(Locale.US, "%.1fL", count / 1_00_000.0);
        } else {
            formatted = String.format(Locale.US, "%.1fcr", count / 1_00_00_000.0);
        }

        // Removing trailing ".0"
        if (formatted.contains(".0")) {
            formatted = formatted.replace(".0", "");
        }

        return formatted;
    }



    @Override
    public void onPositiveSnackMessage(String positiveMessage, int duration) {
        CommonMethods.showPositiveSnackbar(ParentLayout, positiveMessage, duration);
    }

    @Override
    public void onNegativeSnackMessage(String positiveMessage, int duration) {
        CommonMethods.showNegativeSnackbar(ParentLayout, positiveMessage, duration);
    }
}
