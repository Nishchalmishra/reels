package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.POST_FEEDBACK_LIKE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.PUT_UPDATE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.SEND_FEEDBACK;
import static arivihan.technologies.doubtbuzzter2.enums.HOW_TO_TYPE.LECTURE;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.net.Uri;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.SeekParameters;
import androidx.media3.exoplayer.analytics.AnalyticsListener;
import androidx.media3.ui.PlayerView;
import androidx.media3.ui.TimeBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.DisambiguationAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.InstantVideoRecyclerAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.VideoQualityAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.VideoSpeedPlaybackAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonDialogs;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.customView.CustomTimeBar;
import arivihan.technologies.doubtbuzzter2.customView.HowToVideoView;
import arivihan.technologies.doubtbuzzter2.customView.MyToolTip;
import arivihan.technologies.doubtbuzzter2.customView.PermissionRequestView;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.doutchat.DoutChatPath;
import arivihan.technologies.doubtbuzzter2.enums.LikeDislikeEnum;
import arivihan.technologies.doubtbuzzter2.enums.PermissionType;
import arivihan.technologies.doubtbuzzter2.enums.feature.FeatureEnum;
import arivihan.technologies.doubtbuzzter2.helper.DoubtChatHost;
import arivihan.technologies.doubtbuzzter2.helper.WebViewDoubtChatInterface;
import arivihan.technologies.doubtbuzzter2.helper.TooltipHelper;
import arivihan.technologies.doubtbuzzter2.helper.UserNameValidationConfig;
import arivihan.technologies.doubtbuzzter2.helper.videoretention.VideoRetentionTracker;
import arivihan.technologies.doubtbuzzter2.helper.WrapContentLinearLayoutManager;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.helper.EnvironmentManager;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionType;
import arivihan.technologies.doubtbuzzter2.listener.feedback.OnFeedbackDialogClickListener;
import arivihan.technologies.doubtbuzzter2.model.ArivihanLanguageEnum;
import arivihan.technologies.doubtbuzzter2.model.AskButtonState;
import arivihan.technologies.doubtbuzzter2.model.ContentFeedbackType;
import arivihan.technologies.doubtbuzzter2.model.DoubtContextDto;
import arivihan.technologies.doubtbuzzter2.model.DoubtResponseDto;
import arivihan.technologies.doubtbuzzter2.model.MLResponse;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureContentDto;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureQuestionsAnsDto;
import arivihan.technologies.doubtbuzzter2.model.SnackBarDto;
import arivihan.technologies.doubtbuzzter2.model.TimeBarThumbData;
import arivihan.technologies.doubtbuzzter2.model.UserDto;
import arivihan.technologies.doubtbuzzter2.model.UserFeedback;
import arivihan.technologies.doubtbuzzter2.model.VideoPlaybackSpeedDto;
import arivihan.technologies.doubtbuzzter2.model.VideoQualityDto;
import arivihan.technologies.doubtbuzzter2.model.microlecture.LikeDislikeResponse;
import arivihan.technologies.doubtbuzzter2.model.microlecture.MicrolectureMetadataModel;
import arivihan.technologies.doubtbuzzter2.observer.VideoPlayerAskButtonChangeListener;
import arivihan.technologies.doubtbuzzter2.viewModel.AskButtonViewModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

/*
 * File: VideoPlayerActivity Copyright 23/09/24, 12:28 pm Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 23/09/24, 12:28 pm
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */
@UnstableApi
public class VideoPlayerActivity extends VideoPlayerHelperActivity implements Player.Listener,
        VideoPlayerAskButtonChangeListener,
        VideoQualityAdapter.VideoQualityListener,
        VideoSpeedPlaybackAdapter.VideoSpeedChangeListener,
        DisambiguationAdapter.OptionOnClick,
        AnalyticsListener,
        InstantVideoRecyclerAdapter.InstantVideoRecyclerListener,
        MyToolTip.ToolTipClickListener,
        VideoPlayerHelperActivity.OnUiChangeListener,
        DoubtChatHost,
        EdgeToEdgeHelper.SelfManagedInsets {
    private static final String TAG = VideoPlayerHelperActivity.class.getName();
    private static final String LIKE_DISLIKE_VOLLEY_TAG = "LIKE_DISLIKE_TAG";
    private boolean onCreate;
    private RelativeLayout rl_parent_layout;
    List<VideoQualityDto> videoQualityDtoList;
    private ImageView previewImage;
    private ProgressBar progressBar;
    public ExoPlayer player;
    public PlayerView videoFullScreenPlayer;
    private ImageButton rewind, forward;
    private ImageView arivihanLogo, videoPlayerSettings;
    RecyclerView settingsRecyclerView;
    private CardView videoPlayerSettingsLayout;
    private TextView videoPlayerSettingTitle;
    private ImageView videoPlayerSettingClose;
    private LottieAnimationView playerBufferingLogo;
    private AskButtonViewModel askButtonViewModel;
    private Handler handler, animationHandler;
    private Button askButton;
    private Dialog disambiguation_dialog;
    private int lastTrackedMilestone = -1;
    private TextView currentSpeed, currentQuality1;
    private SpeechRecognizer speechRecognizer;
    private Runnable positionUpdatingRunnable, animationRunnable;
    private StringBuilder speechTextDetected;
    private CustomTimeBar customTimeBar;
    private LinearLayout optionLayout, playbackSpeed, videoQuality;
    private VideoSpeedPlaybackAdapter videoSpeedPlaybackAdapter;
    private VideoQualityAdapter videoQualityAdapter;
    ObjectAnimator animationTransitionLeft, animationTransitionLeftFinish,
            animationTransitionRight, animationTransitionRightFinish;
    private Crouton crouton;
    private Style style;
    private Dialog resumeLectureLayoutDialog, relativeLayoutEnterSubmitDoubt, relativeLayoutEditSubmitDoubt, solutionOptionDialog;
    private Dialog nameEnterDialog;
    private MaterialCardView submitSolutionTextOption, submitSolutionVideoOption;
    private List<MicroLectureQuestionsAnsDto> questionsAnsList = new ArrayList<>();
    private boolean controllerInitialized = false;
    private boolean board, shouldNotResume, isBatchOverview;
    private String selectedSubject;
    private boolean runnableStarted;
    private MicroLectureQuestionsAnsDto currentInteractiveQuestion;
    private boolean interactiveQuestionActive;
    private MicroLectureContentDto microLectureContentDto;
    private boolean disableControllerAction;
    private int endPosition, fallbackPosition;
    private int speechRecognizerErrorCount = 1;
    private final int SEEK_TIMER = 10000;
    private final int TIME_BUFFER_DIFFERENCE = 500;
    private final int SPEECH_PERMISSION_REQUEST_CODE = 101;
    private final Map<String, Long> previousStackPosition = new HashMap<>();
    private final List<TimeBarThumbData> thumbDataList = new ArrayList<>();
    private TextView duration, backToPreviousMedia;
    private EditText doubtTextEditText_Enter, doubtTextEditText_Edit;
    private MaterialCardView submitMyDoubt_Enter, doNotSubmitMyDoubt_Enter;
    private MaterialCardView submitMyDoubt_Edit, doNotSubmitMyDoubt_Edit;
    private TextView submitMyDoubtText_Enter, submitMyDoubtText_Edit;
    private TextInputLayout correctedDoubtError_Enter, correctedDoubtError_Edit;
    private String correctedDoubt;
    private Handler permissionHandler, endPositionTrackingHanlder;
    private Runnable permissionRunnable;
    public static final String BroadcastStringForAction = "checkinternet";
    private boolean speechRecognizerInitialized;
    private CardView instantDoubtCardView, instantDoubtVideoCardView;
    private CardView videoCloseView;
    private WebView contextualizedDoubtWebView;
    private View doubtWebViewLoader;
    private LottieAnimationView doubtLoadingAnimation;
    private String pendingLectureDoubtQuestion;
    private String pendingLectureDoubtChapterId;
    private String pendingLectureDoubtMlId;
    private String pendingLectureDoubtSubject;
    private String doubtCurrentUrl = null;
    private List<MLResponse> mlResponses = new ArrayList<>();
    private InstantVideoRecyclerAdapter instantVideoRecyclerAdapter;
    private DoubtResponseDto doubtResponseDto;
    private long playlistStartPosition = -1;
    private long currentVideoPosition = 0;
    private String selectedPlaybackSpeed = "1";
    private MaterialCardView longPressSpeedCard;
    private boolean isLongPressActive = false;
    private Handler tooltipHandler;
    private Runnable tooltipRunnable;
    private MyToolTip tooltip;
    private String selectedQuality = null;
    private String savedSelectedQuality = null;
    private VideoQualityDto selectedVideoQualityDto;
    private ImageView how_to_video_icon;
    private HowToVideoView howToVideoView;
    //    private View how_to_video_view;
    private ImageView iv_like, iv_dislike;
    private LinearLayout ll_like_dislike;
    private LottieAnimationView lottie_like, lottie_dislike;

    //gesture handle, tap handle
    private final Handler tapHandler = new Handler(Looper.getMainLooper());
    private final Handler longPressHandler = new Handler(Looper.getMainLooper());
    private final Handler seekResetHandler = new Handler(Looper.getMainLooper());
    private Runnable skipFadeRunnable;

    private Runnable singleTapRunnable;
    private Runnable longPressRunnable;
    private Runnable seekResetRunnable;

    private static final long TAP_DELAY = 250L;
    private static final long LONG_PRESS_DELAY = 400L;
    private static final long SEEK_RESET_DELAY = 700L;

    private boolean isSeekMode = false;
    private int seekCount = 0;
    private long lastTapTime = 0L;
    private boolean lastTapWasLeft = false;
    private boolean lastAppliedTapWasLeft = false;


    private boolean isSpeechActive = false;


    private LinearLayout exoplayerControlLayout, exoplayerDurationLayout;
    private CardView cvSkip;
    private LottieAnimationView lottie_buy_batch;
    private ImageView iv_buy_batch;
    private RelativeLayout rl_buy_batch;
    private long skipToPositionMs = -1;
    private NetworkStatusLiveData networkStatusLiveData;
    private MicrolectureMetadataModel metadataModel;
    private PermissionRequestView notificationPermissionView;
    private boolean permissionHandled = false;
    private boolean permissionSettingsHandled = false;
    private boolean isVideoReachedToApplicableReviewLimit = false;

    private boolean lecture80PercentTracked = false;
    private boolean lectureCompletedTracked = false;
    // Gesture Detector
    GestureDetector gestureDetector;

    private boolean nameDialogShown = false;
    private boolean feedbackDialogShown = false;


    //controller
    private TextView rewindLabel, forwardLabel;
    ImageView rewindArrow, forwardArrow;

    boolean askButtonPausePlayer = false;
    boolean isCustomMicPermissionShown = false;

    private long longPressStartTime = 0L;

    // Retention tracker: fires direct-video-progress API once per video
    // when user's active watch time crosses the configured threshold (remote config).
    private VideoRetentionTracker videoRetentionTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        microLectureContentDto = (MicroLectureContentDto) getIntent().getSerializableExtra(getString(R.string.passed_obj));
        // Init retention tracker — returns null safely if contentDto is null or init fails.
        videoRetentionTracker = VideoRetentionTracker.create(this, microLectureContentDto);
        shouldNotResume = getIntent().getBooleanExtra("shouldNotResume", false);
        selectedSubject = getIntent().getStringExtra("selectedSubject");
        board = getIntent().getBooleanExtra(PreferenceKeys.BOARD.getKey(), false);
        isBatchOverview = getIntent().getBooleanExtra("isBatchOverview", false);
        skipToPositionMs = getIntent().getLongExtra("skipToPosition", -1);

        String videoMetadataController = MyApplication.getFirebaseRemoteConfig().getString("microlecture_video_metadata_controller");
        Gson gson = new Gson();
        metadataModel = gson.fromJson(videoMetadataController, MicrolectureMetadataModel.class);
        if (metadataModel == null) {
            metadataModel = new MicrolectureMetadataModel(300000, 300000, false);
        }

        setContentView(R.layout.activity_video_player);
        enableImmersiveMode();
        secureScreen();
        initializeView();
        initializeInstantVideoCardView();
        initializePlayerControllerView();
        setVideoLikeDislike(true);
        onClickListener();
        MyApplication.updateVideoSelection(microLectureContentDto.getChapterId(), microLectureContentDto.getId());
        initializePlayer();
        playVideo();
        controllerClickListener();
        initializeTransitionAnimation();
        initializeHandler();
        initializeCelebrationView();
        getInteractiveQuestion(microLectureContentDto.getId());
        snackBarAPI(microLectureContentDto.getId());
        setUpContinuityLayout();

        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            if (isOnline) {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {

                    if (CollectionUtils.isEmpty(questionsAnsList)) {
                        getInteractiveQuestion(microLectureContentDto.getId());
                        snackBarAPI(microLectureContentDto.getId());
                    }
                    if (!controllerInitialized) {
                        player.prepare();
                        player.setPlayWhenReady(true);
                    }

                    CommonMethods.showPositiveSnackbar(arivihanLogo, getString(R.string.InternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);

                    if (doubtCurrentUrl != null) {
                        showContextualizedDoubt(doubtCurrentUrl);
                    }
                }
            } else {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                    CommonMethods.showNegativeSnackbar(arivihanLogo, getString(R.string.NoInternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), false);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), true);
                }
            }
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBackPressedActivity();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);


        //permission call back
        notificationPermissionView = new PermissionRequestView(this);
        notificationPermissionView.setPermissionListener(new PermissionRequestView.PermissionListener() {
            @Override
            public void onDialogClose() {

                Map<String, String> params = new HashMap<>();
                params.put("triggerSource", interactiveQuestionActive ? "interactiveQuestion" : "askDoubtButton");
                params.put("fallbackScreen", interactiveQuestionActive ? "optionsForQuestion" : "manualDoubtPopup");
                params.put("chapterId", microLectureContentDto.getChapterId());
                params.put("chapterName", microLectureContentDto.getChapterName());
                params.put("microlectureId", microLectureContentDto.getId());
                params.put("microlectureName", microLectureContentDto.getTitle());
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.vp_mic_perm_settings_popup_cross_click.getActionName(), params);

                handleMicPermissionDeny();
            }

            @Override
            public void goToSettings() {

                Map<String, String> params = new HashMap<>();
                params.put("triggerSource", interactiveQuestionActive ? "interactiveQuestion" : "askDoubtButton");
                params.put("chapterId", microLectureContentDto.getChapterId());
                params.put("chapterName", microLectureContentDto.getChapterName());
                params.put("microlectureId", microLectureContentDto.getId());
                params.put("microlectureName", microLectureContentDto.getTitle());
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.vp_mic_perm_go_to_settings_click.getActionName(), params);

                permissionSettingsHandled = true;
            }
        });


        //play video
        player.play();

        //set listener of video player helper
        setOnUiChangeListener(this);
    }


    //handle mic permission dialog show or not and first show for first time then show enter dailog

    private void handlePermission() {
        Map<String, String> params = new HashMap<>();
        params.put("triggerSource", interactiveQuestionActive ? "interactiveQuestion" : "askDoubtButton");
        params.put("isFirstRequestinSession", String.valueOf(permissionHandled));
        params.put("chapterId", microLectureContentDto.getChapterId());
        params.put("chapterName", microLectureContentDto.getChapterName());
        params.put("microlectureId", microLectureContentDto.getId());
        params.put("microlectureName", microLectureContentDto.getTitle());
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.vp_mic_perm_request_triggered.getActionName(), params);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, SPEECH_PERMISSION_REQUEST_CODE);
    }


    private void showCustomMicPermissionDialog() {
        if (isCustomMicPermissionShown) {
            handleMicPermissionDeny();
        } else {
            isCustomMicPermissionShown = true;

            Map<String, String> params = new HashMap<>();
            params.put("triggerSource", interactiveQuestionActive ? "interactiveQuestion" : "askDoubtButton");
            params.put("chapterId", microLectureContentDto.getChapterId());
            params.put("chapterName", microLectureContentDto.getChapterName());
            params.put("microlectureId", microLectureContentDto.getId());
            params.put("microlectureName", microLectureContentDto.getTitle());
//          params.put("is_new_lecture_in_session", "yes");
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.vp_mic_perm_settings_popup_view.getActionName(), params);


            checkNameEnterDialogDismissed(); //edge for name popup;

            notificationPermissionView.initPermission(PermissionType.MICROPHONE, true);
        }
    }


    private void handleMicPermissionDeny() {
        //interactive qn
        if (interactiveQuestionActive) {
            speechRecognizerErrorCount = 1;
            setLayoutAndChildrenEnabled(exoplayerControlLayout, false);
            pausePlayerAndStartSpeechRecognizer();

            askButtonViewModel.changeState(AskButtonState.STATE_API_CALLING);
            validateInteractiveAnswerAPI("xyxyxy", currentInteractiveQuestion.getId(), customTimeBar);
        }
        //ask doubt
        else {
            showEnterAskDoubtDialog();
        }
    }

    private void handleMicPermissionGranted() {
        setupSpeechRecognizer();

        if (interactiveQuestionActive) {
            speechRecognizerErrorCount = 1;
            setLayoutAndChildrenEnabled(exoplayerControlLayout, false);
            pausePlayerAndStartSpeechRecognizer();
        }
        //ask doubt
        else {
            askButton.performClick();
        }
    }


    //show enter dialog
    private void showEnterAskDoubtDialog() {
        doubtTextEditText_Enter.setText("");
        relativeLayoutEnterSubmitDoubt.show();

        relativeLayoutEnterSubmitDoubt.getWindow().getDecorView().post(() -> {
            hideNavigationBar();
        });

        //edge case if name dialog is showing
        checkNameEnterDialogDismissed();
    }


    private void onBackPressedActivity() {

        if (player != null && microLectureContentDto != null) {
            long currentPos = player.getCurrentPosition();
            long totalDuration = player.getDuration();
            int progressPercent = totalDuration > 0 ? (int) ((currentPos * 100) / totalDuration) : 0;

            // TRACKING: Log the actual exit progress
            Map<String, String> exitParams = new HashMap<>();
            exitParams.put("exitProgressPercent", String.valueOf(progressPercent));
            exitParams.put("microlectureId", microLectureContentDto.getId());
            exitParams.put("timeSpentms", String.valueOf(currentPos));
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.ml_lec_exit_progress.getActionName(), exitParams);
        }

        boolean isFeedbackEligible = player != null && player.getCurrentPosition() >= metadataModel.getShow_feedback_on_back_min_time_ms();

        Log.d(TAG, "onBackPressedActivity: nameDialogShown=" + nameDialogShown + ", name=" + MyApplication.getStringData("name"));


        //is mic active do these if user back button
        if (isSpeechActive) {
            if (interactiveQuestionActive) {
                speechRecognizerErrorCount = 1;
                setLayoutAndChildrenEnabled(exoplayerControlLayout, false);
                pausePlayerAndStartSpeechRecognizer();

                askButtonViewModel.changeState(AskButtonState.STATE_API_CALLING);
                validateInteractiveAnswerAPI("xyxyxy", currentInteractiveQuestion.getId(), customTimeBar);
            } else {
                speechRecognizer.stopListening();
                askButtonViewModel.changeState(AskButtonState.STATE_ASK);
                setLayoutAndChildrenEnabled(exoplayerControlLayout, true);
                startVideo();
            }

            isSpeechActive = false;
            return;
        }

        //name dialog
        if (!nameDialogShown && StringUtils.isEmpty(MyApplication.getStringData("name"))) {
            nameDialogShown = true;
            showInputDialog(this);
            return;
        }

        //feedback
        if (isFeedbackEligible && !feedbackDialogShown && StringUtils.isNotEmpty(MyApplication.getStringData("name"))) {
            feedbackDialogShown = true;
            showFeedback(true);
            return;
        }

        logVideoPositionEvent();
        finish();
    }


    private void showInputDialog(Context context) {
        if (context == null) return;

        nameEnterDialog = new Dialog(context);
        nameEnterDialog.setContentView(R.layout.dialog_name_input_landscape);
        nameEnterDialog.setCancelable(true);

        Window window = nameEnterDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.getAttributes().windowAnimations = R.style.DialogAnimationUpDown;

            window.setGravity(Gravity.CENTER);

            window.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        MaterialCardView cardView = nameEnterDialog.findViewById(R.id.name_card);
        EditText editText = nameEnterDialog.findViewById(R.id.user_name);
        editText.requestFocus();
        CardView submitCard = nameEnterDialog.findViewById(R.id.next_card);
        TextView submit = nameEnterDialog.findViewById(R.id.submit);
        TextView nameError = nameEnterDialog.findViewById(R.id.name_error);


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
                                    nameEnterDialog.dismiss();
                                    MyApplication.saveData(PreferenceKeys.NAME.getKey(), input);
                                    MyApplication.saveData(PreferenceKeys.IS_NAME_UPDATED.getKey(), true);
                                    Map<String, String> map = new HashMap<>();
                                    map.put("source", "lecture_video_player");
                                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.name_input_popup_submit_click.getActionName(), map);
                                    CommonMethods.showPositiveSnackbar(
                                            arivihanLogo,
                                            getString(R.string.name_updated_mesaaage)
                                    );

                                    // ✅ Feedback condition
                                    if (!isFinishing() && !isDestroyed() &&
                                            player != null &&
                                            player.getCurrentPosition() >= metadataModel.getShow_feedback_on_back_min_time_ms() &&
                                            !feedbackDialogShown) {

                                        feedbackDialogShown = true;
                                        showFeedback(true);
                                    }

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
                                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();
                            }
                        }
                );

                MyVolleySingleton.getInstance(context).addToRequestQueue(request);

            } catch (JSONException e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        nameEnterDialog.show();
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
        } else {
            nameError.setVisibility(View.INVISIBLE);
            cardView.setStrokeColor(ContextCompat.getColor(context, R.color.input_name_stroke_default));
            cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.input_name_bg_default));
        }

        //btn
        buttonView.setEnabled(isValid);
        buttonView.setCardBackgroundColor(ContextCompat.getColor(context, isValid ? R.color.btn_enabled_bg : R.color.btn_disabled_bg));
        buttonText.setTextColor(ContextCompat.getColor(context, isValid ? R.color.btn_enabled_text : R.color.btn_disabled_text));
    }

    private void initializeInstantVideoCardView() {
        instantDoubtCardView = findViewById(R.id.context_doubt_layout);
        instantDoubtVideoCardView = findViewById(R.id.instant_videos_recycler_layout);
        videoCloseView = findViewById(R.id.instant_videos_close);

        contextualizedDoubtWebView = instantDoubtCardView.findViewById(R.id.contextualized_doubt_webview);
        doubtWebViewLoader = instantDoubtCardView.findViewById(R.id.doubt_webview_loader);
        doubtLoadingAnimation = instantDoubtCardView.findViewById(R.id.doubt_loading_animation);
        WebSettings webSettings = contextualizedDoubtWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        contextualizedDoubtWebView.addJavascriptInterface(
                new WebViewDoubtChatInterface(this, this, contextualizedDoubtWebView),
                "AndroidInterface");

        contextualizedDoubtWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                String[] resources = request.getResources();
                if (resources.length == 1 && PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(resources[0])) {
                    if (ContextCompat.checkSelfPermission(VideoPlayerActivity.this, Manifest.permission.RECORD_AUDIO)
                            == PackageManager.PERMISSION_GRANTED) {
                        request.grant(request.getResources());
                    } else {
                        ActivityCompat.requestPermissions(VideoPlayerActivity.this,
                                new String[]{Manifest.permission.RECORD_AUDIO},
                                SPEECH_PERMISSION_REQUEST_CODE);
                    }
                } else {
                    super.onPermissionRequest(request);
                }
            }
        });

        ImageView closeDoubtCard = instantDoubtCardView.findViewById(R.id.instant_doubt_close);
        closeDoubtCard.setOnClickListener(view -> {
            doubtCurrentUrl = null;
            instantDoubtCardView.setVisibility(View.GONE);
        });

        videoCloseView.setOnClickListener(view -> {
            Map<String, String> params = new HashMap<>();
            params.put(PreferenceKeys.MICROLECTURE_ID.getKey(), microLectureContentDto.getId());
            params.put("chapterId", microLectureContentDto.getChapterId());
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.vp_doubt_video_close.getActionName(), params);
            instantDoubtVideoCardView.setVisibility(View.GONE);
            videoCloseView.setVisibility(View.GONE);
        });

        RecyclerView recyclerView = instantDoubtVideoCardView.findViewById(R.id.recycler_view_intent_videos);
        recyclerView.setLayoutManager(new WrapContentLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        instantVideoRecyclerAdapter = new InstantVideoRecyclerAdapter(this, mlResponses, this);
        recyclerView.setAdapter(instantVideoRecyclerAdapter);
    }

    private void showContextualizedDoubt(String url) {
        Log.d(TAG, "showContextualizedDoubt-finalURL: " + url);
        doubtCurrentUrl = url;
        if (!MyApplication.isNetworkAvailable()) {
            CommonMethods.showNegativeSnackbar(arivihanLogo, getString(R.string.NoInternetConnection));
            return;
        }
        instantDoubtCardView.setVisibility(View.VISIBLE);
        doubtWebViewLoader.setVisibility(View.VISIBLE);
        doubtLoadingAnimation.playAnimation();
        contextualizedDoubtWebView.post(() -> contextualizedDoubtWebView.loadUrl(url));
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            doubtLoadingAnimation.cancelAnimation();
            doubtWebViewLoader.setVisibility(View.GONE);
        }, 2000);
    }

    private void handleDoubtWebViewError(WebView view) {
        runOnUiThread(() -> {
            doubtLoadingAnimation.cancelAnimation();
            doubtWebViewLoader.setVisibility(View.GONE);
            view.loadUrl("about:blank");
            instantDoubtCardView.setVisibility(View.GONE);
            CommonMethods.showNegativeSnackbar(arivihanLogo, getString(R.string.NoInternetConnection));
        });
    }

    @Override
    protected void onLectureDoubtTriggered(String userQuestion, String chapterId, String microLectureId, String subject) {
        pendingLectureDoubtQuestion = userQuestion;
        pendingLectureDoubtChapterId = chapterId;
        pendingLectureDoubtMlId = microLectureId;
        pendingLectureDoubtSubject = subject;

        contextualizedDoubtWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                injectLectureDoubtContext();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && request.isForMainFrame()) {
                    handleDoubtWebViewError(view);
                }
            }

            @Override
            @SuppressWarnings("deprecation")
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    handleDoubtWebViewError(view);
                }
            }
        });

        askButtonViewModel.changeState(AskButtonState.STATE_ASK);
        showContextualizedDoubt(buildLectureDoubtWebViewUrl(subject, microLectureId, chapterId));
    }

    private String buildLectureDoubtWebViewUrl(String subject, String mlId, String chapterId) {
        String env = EnvironmentManager.getEnvironmentNameByUrl(MyApplication.getEndPoint()).toLowerCase();
        String remoteKey = "doubt_chat_web_web_view_url" + "_" + env + "_v2";
        String baseUrl = MyApplication.getFirebaseRemoteConfig().getString(remoteKey)
                + "/" + DoutChatPath.LECTURE_DOUBT.path();
        return String.valueOf(Uri.parse(baseUrl).buildUpon()
                .appendQueryParameter("token", MyApplication.getFCMToken())
                .appendQueryParameter("userId", MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()))
                .appendQueryParameter("subject", subject != null ? subject : "")
                .appendQueryParameter("microLectureId", mlId != null ? mlId : "")
                .appendQueryParameter("time", String.valueOf(currentVideoPosition))
                .appendQueryParameter("chapterId", chapterId != null ? chapterId : "")
                .appendQueryParameter("lang", MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()))
                .appendQueryParameter("batchId", String.valueOf(MyApplication.getIntData(PreferenceKeys.BATCH_ID.getKey())))
                .appendQueryParameter("courseId", MyApplication.getStringData(PreferenceKeys.EXAM_CODE.getKey()))
                .appendQueryParameter("env", env)
                .build());
    }

    private void injectLectureDoubtContext() {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("topic", microLectureContentDto != null ? microLectureContentDto.getTitle() : "");
        ctx.put("context", "");
        ctx.put("slideContext", "");
        ctx.put("summary", "");
        ctx.put("course", MyApplication.getStringData(PreferenceKeys.EXAM_NAME.getKey()));
        ctx.put("subject", pendingLectureDoubtSubject != null ? pendingLectureDoubtSubject : "");
        ctx.put("class", MyApplication.getStringData(PreferenceKeys.CLASS_NAME.getKey()));
        ctx.put("microlecture_code", pendingLectureDoubtMlId != null ? pendingLectureDoubtMlId : "");
        ctx.put("chapter_code", pendingLectureDoubtChapterId != null ? pendingLectureDoubtChapterId : "");
        ctx.put("batchId", String.valueOf(MyApplication.getIntData(PreferenceKeys.BATCH_ID.getKey())));
        ctx.put("lang", MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
        ctx.put("sessionId", UUID.randomUUID().toString());
        ctx.put("studentName", MyApplication.getStringData(PreferenceKeys.NAME.getKey()));
        ctx.put("phoneNumber", MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey()));
        ctx.put("question", pendingLectureDoubtQuestion != null ? pendingLectureDoubtQuestion : "");
        String json = new Gson().toJson(ctx);
        Log.d(TAG, "injectLectureDoubtContext: "+json);
        contextualizedDoubtWebView.evaluateJavascript("javascript:showLectureDoubtContext(" + json + ");", null);
    }

    // -------------------------------------------------------------------------
    // DoubtChatHost implementation — bridges WebViewDoubtChatInterface calls
    // -------------------------------------------------------------------------
    @Override
    public void backToPreviousActivity() {
        runOnUiThread(() -> {
            doubtCurrentUrl = null;
            instantDoubtCardView.setVisibility(View.GONE);
        });
    }

    @Override
    public void showDialogForSubscription() {
        SubscriptionNavigationHelper.navigateToSubscriptionPage(this, RedirectionSource.VIDEO_PLAYER);
    }

    @Override
    public void setFirebaseEvent(String eventName, Map<String, String> params) {
        try {
            if (params == null || params.isEmpty()) {
                MyApplication.logFirebaseBundleCreation(eventName);
            } else {
                MyApplication.logFirebaseBundleCreation(eventName, params);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void launchGalleryPicker() { /* no-op in video player context */ }

    @Override
    public void checkCameraPermission(String permission, int requestCode) { /* no-op in video player context */ }

    @Override
    public void setChatSessionId(String chatSessionId) { /* no-op in video player context */ }

    private void initializePlayerControllerView() {
        String previewUrl = getVideoPreviewUrl();
        rl_parent_layout = findViewById(R.id.rl_parent_layout);
        playerBufferingLogo = findViewById(R.id.logo_loading);
        arivihanLogo = findViewById(R.id.logo);
        previewImage = findViewById(R.id.videoPreview);
        try {
            Glide.with(this)
                    .load(StringUtils.isNotEmpty(previewUrl) ? previewUrl : R.drawable.loading_lecture_image)
                    .error(R.drawable.loading_lecture_image)
                    .into(previewImage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        progressBar = findViewById(R.id.loading_progress_bar);
        rewind = findViewById(R.id.rewind);
        forward = findViewById(R.id.forward);
        rewindLabel = findViewById(R.id.rewind_label);
        forwardLabel = findViewById(R.id.forward_label);
        rewindArrow = findViewById(R.id.rewind_arrow);
        forwardArrow = findViewById(R.id.forward_arrow);
        longPressSpeedCard = findViewById(R.id.long_press_speed_card);


        exoplayerControlLayout = findViewById(R.id.exoplayer_control_layout);
        exoplayerDurationLayout = findViewById(R.id.exoplayer_duration_layout);

        videoFullScreenPlayer = findViewById(R.id.videoFullScreenPlayer);
        customTimeBar = videoFullScreenPlayer.findViewById(R.id.exo_progress);
        customTimeBar.addListener(new TimeBar.OnScrubListener() {
            @Override
            public void onScrubStart(@NonNull TimeBar timeBar, long position) {
            }

            @Override
            public void onScrubMove(@NonNull TimeBar timeBar, long position) {
                Log.i("SEEK_POSITION", String.valueOf(position));
                if (position < fallbackPosition) {
                    long seekPosition = player.getBufferedPosition();
                    player.seekTo(seekPosition);
                    customTimeBar.setPosition(seekPosition);
                }
            }

            @Override
            public void onScrubStop(@NonNull TimeBar timeBar, long position, boolean canceled) {
                long seekPosition = player.getBufferedPosition();
                player.seekTo(seekPosition);
                customTimeBar.setPosition(seekPosition);
            }
        });
        duration = videoFullScreenPlayer.findViewById(R.id.custom_exo_duration);
        videoPlayerSettings = videoFullScreenPlayer.findViewById(R.id.setting);
        videoPlayerSettingsLayout = videoFullScreenPlayer.findViewById(R.id.video_settings);
        settingsRecyclerView = findViewById(R.id.settings_recycler_view);
        settingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        videoSpeedPlaybackAdapter = new VideoSpeedPlaybackAdapter(getPlayBackSpeedData(), this);
        playbackSpeed = videoPlayerSettingsLayout.findViewById(R.id.playback_speed);
        currentSpeed = videoPlayerSettingsLayout.findViewById(R.id.current_speed);
        backToPreviousMedia = videoFullScreenPlayer.findViewById(R.id.go_back_to_previous_media);

        optionLayout = videoPlayerSettingsLayout.findViewById(R.id.option_layout);
        videoQuality = videoPlayerSettingsLayout.findViewById(R.id.video_quality);
        videoPlayerSettingTitle = videoPlayerSettingsLayout.findViewById(R.id.video_settings_layout_title);
        currentQuality1 = videoPlayerSettingsLayout.findViewById(R.id.current_quality_1);

        videoPlayerSettingClose = videoPlayerSettingsLayout.findViewById(R.id.iv_close);
        videoPlayerSettingClose.setOnClickListener(view -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.vp_settings_popup_cross_click.getActionName());
            hideSettingLayout();
        });

        ll_like_dislike = videoFullScreenPlayer.findViewById(R.id.ll_like_dislike);
        iv_like = videoFullScreenPlayer.findViewById(R.id.iv_like);
        iv_dislike = videoFullScreenPlayer.findViewById(R.id.iv_dislike);
        lottie_like = videoFullScreenPlayer.findViewById(R.id.lottie_like);
        lottie_dislike = videoFullScreenPlayer.findViewById(R.id.lottie_dislike);

        cvSkip = videoFullScreenPlayer.findViewById(R.id.cv_skip);
        lottie_buy_batch = videoFullScreenPlayer.findViewById(R.id.lottie_buy_batch);
        iv_buy_batch = videoFullScreenPlayer.findViewById(R.id.iv_buy_batch);
        rl_buy_batch = videoFullScreenPlayer.findViewById(R.id.rl_buy_batch);

        rl_buy_batch.setVisibility(View.GONE);

        if (isBatchOverview) {
            askButton.setVisibility(View.GONE);
            String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
            String imageUrl = getJoinNowImageUrl(lang);

            int defaultImgDrawable = ArivihanLanguageEnum.HINDI.getCode().equals(lang) ? R.drawable.img_join_now_51_hi : R.drawable.img_join_now_51_en;
            Glide.with(this)
                    .load(StringUtils.isNotEmpty(imageUrl) ? imageUrl : defaultImgDrawable)
                    .placeholder(defaultImgDrawable)
                    .error(defaultImgDrawable)
                    .into(iv_buy_batch);

            lottie_buy_batch.playAnimation();
            rl_buy_batch.setVisibility(SubscriptionType.hasActiveSubscription() ? View.GONE : View.VISIBLE);
            ViewGroup.MarginLayoutParams timeBarParams = (ViewGroup.MarginLayoutParams) customTimeBar.getLayoutParams();
            timeBarParams.setMarginStart((int) (24 * getResources().getDisplayMetrics().density));
            customTimeBar.setLayoutParams(timeBarParams);
            if (skipToPositionMs > 0) {
                cvSkip.setOnClickListener(v -> player.seekTo(skipToPositionMs));
            }

            lottie_buy_batch.setOnClickListener(v -> {
                SubscriptionNavigationHelper.navigateToSubscriptionPage(this, RedirectionSource.BATCH_OVERVIEW);
            });
        }

        lottie_like.setVisibility(View.GONE);
        lottie_dislike.setVisibility(View.GONE);

        // Push controller controls and the ask button above the navigation bar / gesture handle.
        // SelfManagedInsets means the frame does not consume insets, so they reach here intact.
        View controllerRoot = videoFullScreenPlayer.findViewById(R.id.player_controller_root);
        final float density = getResources().getDisplayMetrics().density;
        // Base margins declared in XML (dp → px).
        final int askBaseBottom = Math.round(8 * density);
        final int askBaseStart = Math.round(15 * density);
        ViewCompat.setOnApplyWindowInsetsListener(rl_parent_layout, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.navigationBars()
                            | WindowInsetsCompat.Type.displayCutout());
            if (controllerRoot != null) {
                controllerRoot.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            }
            if (askButton != null) {
                ViewGroup.MarginLayoutParams lp =
                        (ViewGroup.MarginLayoutParams) askButton.getLayoutParams();
                lp.bottomMargin = askBaseBottom + bars.bottom;
                lp.setMarginStart(askBaseStart + bars.left);
                askButton.setLayoutParams(lp);
            }
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(rl_parent_layout);

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
            LikeDislikeEnum feedback;
            if (microLectureContentDto.getLiked() == null || !microLectureContentDto.getLiked()) {
                feedback = LikeDislikeEnum.LIKE;
                setLikeDislike(true);
            } else {
                feedback = LikeDislikeEnum.NOTHING;
                clearSelectLikeDislike(true, false);
            }

            setMicroLectureLikeDislike(feedback, microLectureContentDto.getChapterId(), microLectureContentDto.getId());

            // Firebase Event
            Map<String, String> params = new HashMap<>();
            params.put("isLiked", feedback.name());
            params.put(PreferenceKeys.MICROLECTURE_ID.getKey(), microLectureContentDto.getId());
            params.put("chapterId", microLectureContentDto.getChapterId());
            params.put("screen", TAG);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.ml_click_like.name(), params);
        });

        iv_dislike.setOnClickListener(imageView -> {
            LikeDislikeEnum feedback;
            if (microLectureContentDto.getLiked() == null || microLectureContentDto.getLiked()) {
                feedback = LikeDislikeEnum.DISLIKE;
                setLikeDislike(false);
            } else {
                feedback = LikeDislikeEnum.NOTHING;
                clearSelectLikeDislike(false, false);
            }

            setMicroLectureLikeDislike(feedback, microLectureContentDto.getChapterId(), microLectureContentDto.getId());

            // Firebase Event
            Map<String, String> params = new HashMap<>();
            params.put("isLiked", feedback.name());
            params.put(PreferenceKeys.MICROLECTURE_ID.getKey(), microLectureContentDto.getId());
            params.put("chapterId", microLectureContentDto.getChapterId());
            params.put("screen", TAG);
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.ml_click_dislike.name(), params);
        });

        if (microLectureContentDto != null && microLectureContentDto.getVideoQualityDtos() != null) {
            videoQualityDtoList = microLectureContentDto.getVideoQualityDtos();
            Log.d(TAG, "Quality count " + videoQualityDtoList.size());
            selectedQuality = MyApplication.getStringData(getString(R.string.user_selected_video_quality));
            if (StringUtils.isNotEmpty(selectedQuality)) {
                for (VideoQualityDto qualityDto : videoQualityDtoList) {
                    if (qualityDto.getQuality().equals(selectedQuality)) {
                        selectedVideoQualityDto = qualityDto;
                        break;
                    }
                }
            }

            if (selectedVideoQualityDto != null) {
                for (VideoQualityDto qualityDto : videoQualityDtoList) {
                    qualityDto.setSelected(qualityDto.getQuality().equals(selectedQuality));
                }
            } else {
                for (VideoQualityDto qualityDto : videoQualityDtoList) {
                    if (qualityDto.isSelected()) {
                        selectedVideoQualityDto = qualityDto;

                        String previousQuality = selectedQuality;
                        Map<String, String> params = new HashMap<>();
                        params.put("chapterId", microLectureContentDto.getChapterId());
                        params.put("chapterName", microLectureContentDto.getChapterName());
                        params.put("microlectureId", microLectureContentDto.getId());
                        params.put("microlectureName", microLectureContentDto.getTitle());
                        params.put("selectedQuality", qualityDto.getQuality());
                        params.put("previousQuality", previousQuality);
                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.vp_video_quality_option_select_click.getActionName(), params);

                        selectedQuality = qualityDto.getQuality();
                        break;
                    }
                }
            }
            if (selectedVideoQualityDto != null) {
                currentQuality1.setText(selectedQuality);
            } else {
                selectedQuality = getString(R.string._1080p);
                currentQuality1.setText(R.string._1080p);
            }
        } else {
            Log.d(TAG, "Quality count 0");

            videoQualityDtoList = new ArrayList<>();
            currentQuality1.setText(R.string._1080p);
        }
        videoQualityAdapter = new VideoQualityAdapter(videoQualityDtoList, this);

        disambiguation_dialog = new Dialog(this);
        disambiguation_dialog.setContentView(R.layout.intent_option);
        Window disambiguation_dialog_Window = disambiguation_dialog.getWindow();
        if (disambiguation_dialog_Window != null) {
            disambiguation_dialog_Window.getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
            disambiguation_dialog_Window.setBackgroundDrawable(AppCompatResources.getDrawable(this,
                    R.drawable.bachground_custom_dialogu_typeactivity));
            disambiguation_dialog_Window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        disambiguation_dialog.setCancelable(false);
        MaterialCardView fallback = disambiguation_dialog.findViewById(R.id.card_dis);
        MLResponse mlResponse = new MLResponse();
        mlResponse.setIntentid("fallback");
        fallback.setOnClickListener(view -> intentClicked(mlResponse, speechTextDetected.toString()));
        setDurationText(-1);

        // relativeLayoutEnterSubmitDoubt, relativeLayoutEditSubmitDoubt

        //dialog submit - enter
        relativeLayoutEnterSubmitDoubt = new Dialog(this);
        relativeLayoutEnterSubmitDoubt.setContentView(R.layout.submit_doubt_layout);
        Window relativeLayoutEnterSubmitDoubt_Window = relativeLayoutEnterSubmitDoubt.getWindow();
        if (relativeLayoutEnterSubmitDoubt_Window != null) {
            relativeLayoutEnterSubmitDoubt_Window.getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
            relativeLayoutEnterSubmitDoubt_Window.setBackgroundDrawable(AppCompatResources.getDrawable(this, R.drawable.bachground_custom_dialogu_typeactivity));
            relativeLayoutEnterSubmitDoubt_Window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        relativeLayoutEnterSubmitDoubt.setCancelable(false);
        TextView topText_Enter = relativeLayoutEnterSubmitDoubt.findViewById(R.id.topText);
        topText_Enter.setText(getString(R.string.question_enter_bubble_title));
        ImageView closeButton_Enter = relativeLayoutEnterSubmitDoubt.findViewById(R.id.close_btn);
        submitMyDoubt_Enter = relativeLayoutEnterSubmitDoubt.findViewById(R.id.submit_my_doubt);
        submitMyDoubtText_Enter = relativeLayoutEnterSubmitDoubt.findViewById(R.id.submit_my_doubt_text);
        doNotSubmitMyDoubt_Enter = relativeLayoutEnterSubmitDoubt.findViewById(R.id.dont_submit_doubt);
        doubtTextEditText_Enter = relativeLayoutEnterSubmitDoubt.findViewById(R.id.corrected_doubt);
        doubtTextEditText_Enter.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        boolean isValid = StringUtils.isNotEmpty(s.toString().trim());
                        submitMyDoubt_Enter.setCardBackgroundColor(ContextCompat.getColor(VideoPlayerActivity.this, isValid ? R.color.btn_enabled_bg : R.color.btn_disabled_bg));
                        submitMyDoubtText_Enter.setTextColor(ContextCompat.getColor(VideoPlayerActivity.this, isValid ? R.color.btn_enabled_text : R.color.btn_disabled_text));
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                }
        );
        doubtTextEditText_Enter.post(() -> {
            doubtTextEditText_Enter.requestFocus();
            doubtTextEditText_Enter.setCursorVisible(true);
        });
        correctedDoubtError_Enter = relativeLayoutEnterSubmitDoubt.findViewById(R.id.corrected_doubt_error);
        closeButton_Enter.setOnClickListener(view -> {
            Map<String, String> params = new HashMap<>();
            params.put(PreferenceKeys.MICROLECTURE_ID.getKey(), microLectureContentDto.getId());
            params.put("chapterId", microLectureContentDto.getChapterId());
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.vp_doubt_submit_popup_close_btn_click.getActionName(), params);
            relativeLayoutEnterSubmitDoubt.dismiss();
            correctedDoubtError_Enter.setErrorEnabled(false);
            player.setPlayWhenReady(true);
        });


        //dialog submit - edit
        relativeLayoutEditSubmitDoubt = new Dialog(this);
        relativeLayoutEditSubmitDoubt.setContentView(R.layout.submit_doubt_layout);
        Window relativeLayoutEditSubmitDoubt_Window = relativeLayoutEditSubmitDoubt.getWindow();
        if (relativeLayoutEditSubmitDoubt_Window != null) {
            relativeLayoutEditSubmitDoubt_Window.getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
            relativeLayoutEditSubmitDoubt_Window.setBackgroundDrawable(AppCompatResources.getDrawable(this, R.drawable.bachground_custom_dialogu_typeactivity));
            relativeLayoutEditSubmitDoubt_Window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        relativeLayoutEditSubmitDoubt.setCancelable(false);
        TextView topText_Exit = relativeLayoutEditSubmitDoubt.findViewById(R.id.topText);
        topText_Exit.setText(getString(R.string.question_edit_bubble_title));
        ImageView closeButton_Edit = relativeLayoutEditSubmitDoubt.findViewById(R.id.close_btn);
        submitMyDoubt_Edit = relativeLayoutEditSubmitDoubt.findViewById(R.id.submit_my_doubt);
        submitMyDoubtText_Edit = relativeLayoutEditSubmitDoubt.findViewById(R.id.submit_my_doubt_text);
        doNotSubmitMyDoubt_Edit = relativeLayoutEditSubmitDoubt.findViewById(R.id.dont_submit_doubt);
        doubtTextEditText_Edit = relativeLayoutEditSubmitDoubt.findViewById(R.id.corrected_doubt);
        doubtTextEditText_Edit.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        boolean isValid = StringUtils.isNotEmpty(s.toString().trim());
                        submitMyDoubt_Edit.setCardBackgroundColor(ContextCompat.getColor(VideoPlayerActivity.this, isValid ? R.color.btn_enabled_bg : R.color.btn_disabled_bg));
                        submitMyDoubtText_Edit.setTextColor(ContextCompat.getColor(VideoPlayerActivity.this, isValid ? R.color.btn_enabled_text : R.color.btn_disabled_text));
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                }
        );
        doubtTextEditText_Edit.post(() -> {
            doubtTextEditText_Edit.requestFocus();
            doubtTextEditText_Edit.setCursorVisible(true);
        });
        correctedDoubtError_Edit = relativeLayoutEditSubmitDoubt.findViewById(R.id.corrected_doubt_error);
        closeButton_Edit.setOnClickListener(view -> {
            Map<String, String> params = new HashMap<>();
            params.put(PreferenceKeys.MICROLECTURE_ID.getKey(), microLectureContentDto.getId());
            params.put("chapterId", microLectureContentDto.getChapterId());
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.vp_doubt_submit_popup_close_btn_click.getActionName(), params);
            relativeLayoutEditSubmitDoubt.dismiss();
            correctedDoubtError_Edit.setErrorEnabled(false);
            player.setPlayWhenReady(true);
        });

        solutionOptionDialog = new Dialog(this);
        solutionOptionDialog.setContentView(R.layout.doubt_solution_option_layout);
        if (solutionOptionDialog.getWindow() != null) {
            solutionOptionDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
            solutionOptionDialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(this, R.drawable.bachground_custom_dialogu_typeactivity));
            solutionOptionDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        solutionOptionDialog.setCancelable(false);

        submitSolutionTextOption = solutionOptionDialog.findViewById(R.id.text_solution);
        submitSolutionVideoOption = solutionOptionDialog.findViewById(R.id.video_solution);

//        long visibleDuration = microLectureContentDto.getVideoCompletionEndPos();
//        customTimeBar.setDuration(visibleDuration);
    }

    private void trackLectureCompletionMilestone() {

        if (player == null || microLectureContentDto == null) return;

        long currentPosition = player.getCurrentPosition();
        long totalDuration = player.getDuration();

        if (totalDuration <= 0) return;

        int progressPercent = (int) ((currentPosition * 100) / totalDuration);

        // FRD completion milestone = 80%
        if (progressPercent >= 80 && !lecture80PercentTracked) {

            lecture80PercentTracked = true;

            HashMap<String, String> params = new HashMap<>();
            params.put("watch_percent", "80");
            params.put("microlecture_id", microLectureContentDto.getId());
            params.put("chapter_id", microLectureContentDto.getChapterId());
            params.put("lecture_title", microLectureContentDto.getTitle());

            MyApplication.logFirebaseBundleCreation(FirebaseEvents.ml_80_percent_watched.getActionName(), params);

            markLectureCompleted();
        }
    }

    private void markLectureCompleted() {

        if (lectureCompletedTracked) return;

        lectureCompletedTracked = true;

        HashMap<String, String> params = new HashMap<>();
        params.put("completion_threshold", "80");
        params.put("completion_status", "completed");
        params.put("microlecture_id", microLectureContentDto.getId());
        params.put("chapter_id", microLectureContentDto.getChapterId());

        MyApplication.logFirebaseBundleCreation(FirebaseEvents.ml_marked_completed.getActionName(), params);
    }

    private String getJoinNowImageUrl(String lang) {
        try {
            String jsonString = MyApplication.getFirebaseRemoteConfig()
                    .getString("video_player_join_now_anim_image_v1");
            if (StringUtils.isEmpty(jsonString)) return "";
            JSONObject root = new JSONObject(jsonString);
            String batchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
            if (StringUtils.isNotEmpty(batchCode)) {
                String batchUrl = root.optString(batchCode, "");
                if (StringUtils.isNotEmpty(batchUrl)) return batchUrl;
            }
            return root.optString(lang, "");
        } catch (Exception e) {
            return "";
        }
    }

    private String getVideoPreviewUrl() {

        try {
            String jsonString = MyApplication.getFirebaseRemoteConfig()
                    .getString("lecture_preview_url_v1");

            if (StringUtils.isEmpty(jsonString)) {
                return "";
            }
            JSONObject root = new JSONObject(jsonString);

            String batchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());

            if (StringUtils.isEmpty(batchCode)) {
                return "";
            }

            String videoUrl = root.optString(batchCode, "");

            if (StringUtils.isEmpty(videoUrl)) {
                Log.w("previewUrl", "No video preview url found for batchCode=" + batchCode);
            } else {
                Log.d("previewUrl", "Video preview url Extracted video URL: " + videoUrl);
            }

            Log.d("previewUrl", "Video preview URL fetching video URL successfully");
            return videoUrl.trim();

        } catch (Exception e) {
            return "";
        }
    }

    private void clearSelectLikeDislike(boolean isLike, boolean isAfterAnimation) {
        if (!isLike) {
            lottie_dislike.clearAnimation();
            lottie_dislike.setVisibility(View.GONE);
            iv_dislike.setEnabled(true);
            iv_dislike.setVisibility(View.VISIBLE);

            iv_dislike.setScaleX(isAfterAnimation ? 0.8f : 0.8f);
            iv_dislike.setScaleY(isAfterAnimation ? 0.8f : 0.8f);
            int tintColorDislike = ContextCompat.getColor(VideoPlayerActivity.this, R.color.white);
            iv_dislike.setImageDrawable(ContextCompat.getDrawable(VideoPlayerActivity.this, isAfterAnimation ? R.drawable.img_dislike_video_fill : R.drawable.img_dislike_video_outline));
            iv_dislike.setColorFilter(tintColorDislike, PorterDuff.Mode.SRC_IN);
        } else {
            lottie_like.clearAnimation();
            lottie_like.setVisibility(View.GONE);
            iv_like.setEnabled(true);
            iv_like.setVisibility(View.VISIBLE);

            iv_like.setScaleX(isAfterAnimation ? 0.8f : 0.8f);
            iv_like.setScaleY(isAfterAnimation ? 0.8f : 0.8f);
            int tintColorLike = ContextCompat.getColor(VideoPlayerActivity.this, R.color.white);
            iv_like.setImageDrawable(ContextCompat.getDrawable(VideoPlayerActivity.this, isAfterAnimation ? R.drawable.img_like_video_fill : R.drawable.img_like_video_outline));
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

    private void setDurationText(long totalDuration) {
        if (totalDuration > 0) {
            String formattedDuration = formatDuration(totalDuration);
            duration.setText(formattedDuration);
        } else {
            duration.setText("--:--");
        }
    }

    private String formatDuration(long durationMillis) {
        long totalSeconds = durationMillis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            // Return duration in hh:mm:ss format
            return String.format(Locale.ENGLISH, "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            // Return duration in mm:ss format
            return String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds);
        }
    }

    private void initializeTransitionAnimation() {
        ImageView transitionLeftImage = findViewById(R.id.transition_image_left);
        ImageView transitionRightImage = findViewById(R.id.transition_image_right);

        animationTransitionLeft = ObjectAnimator.ofFloat(transitionLeftImage, "translationX", 0f);
        animationTransitionLeft.setDuration(800);
        animationTransitionLeft.setInterpolator(new AccelerateDecelerateInterpolator());

        animationTransitionRight = ObjectAnimator.ofFloat(transitionRightImage, "translationX", 0f);
        animationTransitionRight.setDuration(800);
        animationTransitionRight.setInterpolator(new AccelerateDecelerateInterpolator());

        animationTransitionLeftFinish = ObjectAnimator.ofFloat(transitionLeftImage, "translationX", -2000f);
        animationTransitionLeftFinish.setDuration(560);
        animationTransitionLeftFinish.setInterpolator(new AccelerateDecelerateInterpolator());


        animationTransitionRightFinish = ObjectAnimator.ofFloat(transitionRightImage, "translationX", 2000f);
        animationTransitionRightFinish.setDuration(560);
        animationTransitionRightFinish.setInterpolator(new AccelerateDecelerateInterpolator());
    }

    private void initializeHandler() {
        animationHandler = new Handler(Looper.getMainLooper());
    }

    private void removeHandlerCallback() {
        if (animationHandler != null && animationRunnable != null) {
            animationHandler.removeCallbacks(animationRunnable);
        }

        if (handler != null && positionUpdatingRunnable != null) {
            handler.removeCallbacks(positionUpdatingRunnable);
        }
        if (permissionHandler != null && permissionRunnable != null) {
            permissionHandler.removeCallbacks(permissionRunnable);
        }
    }

    private void startAnimationOnVideoChange() {
        animationTransitionRight.start();
        animationTransitionLeft.start();
        animationRunnable = () -> {
            animationTransitionRightFinish.start();
            animationTransitionLeftFinish.start();
        };
        animationHandler.postDelayed(animationRunnable, 850);
    }


    private void initializeView() {
        onCreate = true;
        if (microLectureContentDto == null) {
            Log.e(TAG, "initializeView: microLectureContentDto is null");
            finish();
            return;
        }
        speechTextDetected = new StringBuilder();
        askButton = findViewById(R.id.askButton);
        LottieAnimationView apiProcessingAnimation = findViewById(R.id.api_processing);
        LinearLayout listeningView = findViewById(R.id.ll_txtSpeechInput);
        askButtonViewModel = new AskButtonViewModel(this);
        findViewById(R.id.api_processing);
        setAskButton(askButton);
        setChapterId(microLectureContentDto.getChapterId());
        setMicroLectureContentDto(microLectureContentDto);
        setApiProcessingAnimation(apiProcessingAnimation);
        setListeningLayout(listeningView);

        Configuration croutonConfiguration = new Configuration.Builder().setDuration(5000).build();
        style = new Style.Builder()
                .setTextColor(R.color.white)
                .setBackgroundColorValue(R.color.black)
                .setTextSize(14)
                .setHeight(120)
                .setGravity(Gravity.CENTER_HORIZONTAL).setConfiguration(croutonConfiguration)
                .build();
//        how_to_video_view = findViewById(R.id.how_to_video_view);
        how_to_video_icon = findViewById(R.id.how_to_video_icon);
        if (MyApplication.getFirebaseRemoteConfig().getBoolean("hide_how_to_video_icon_video") ||
                MyApplication.getFirebaseRemoteConfig().getBoolean("hideHowToVideos"))
            how_to_video_icon.setVisibility(View.GONE);
        how_to_video_icon.setOnClickListener(view -> {
            logFirebaseEvent();
            openExpandedVideo();
        });
    }

    private void logFirebaseEvent() {
        Map<String, String> params = new HashMap<>();
        params.put("action", "video_player_how_to_card_click");
        ;
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.video_player_how_to_card_click.getActionName(), params);
    }

    private void openExpandedVideo() {
        Log.d("openExpandedVideoIssue", "openExpandedVideo: ");
        player.pause();
//        howToVideoView = HowToVideoView.getInstance(this);
////        howToVideoView.setView(this, how_to_video_view);
//        howToVideoView.setType(LECTURE, true);
//        howToVideoView.setButtonText(getString(R.string.Watch_Now));
    }

    private void initializeSpeechRecognizer() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.d("micIssue pop up", "initializeSpeechRecognizer speech recognition : ");
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.RECORD_AUDIO},
//                    SPEECH_PERMISSION_REQUEST_CODE);
        } else {
            setupSpeechRecognizer();
        }
    }

    private void sendFeedbackAPI(boolean isLiked, ArrayList<String> selectedFeedbacks, String feedbackMessage) {
        try {
            Log.d(TAG, "Liked: " + isLiked + ", sendFeedbackAPI: " + selectedFeedbacks + ", feedbackMessage: " + feedbackMessage);
            UserFeedback userFeedback = new UserFeedback();
            userFeedback.setLiked(isLiked);
            userFeedback.setFeedbackType(ContentFeedbackType.VIDEO_LECTURE);
            userFeedback.setComment(feedbackMessage);
            userFeedback.setFeedbacks(selectedFeedbacks);
            if (microLectureContentDto != null) {
                userFeedback.setMicrolectureId(microLectureContentDto.getId());
                userFeedback.setContentId(microLectureContentDto.getId());
                userFeedback.setChapterId(microLectureContentDto.getChapterId());
            }

            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                    SEND_FEEDBACK, new JSONObject(gson.toJson(userFeedback)),
                    response -> {
                        Log.d(TAG, "Response: " + response.toString());
                    }, error -> {
                Log.d(TAG, "Response: " + error.toString());
            });
            myJsonObjectRequest.setTag(TAG);
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
            MyApplication.saveData(microLectureContentDto.getId(), true);
//            checkContinuity();
        } catch (JSONException e) {
            e.printStackTrace();
            if (getIntent().getBooleanExtra("replay", false))
                onBackPressedActivity();
        }

    }

    private void setVideoLikeDislike(boolean isUpdateSelection) {
        if (microLectureContentDto != null && isUpdateSelection) {
            clearSelectLikeDislike(true, false);
            clearSelectLikeDislike(false, false);
            if (microLectureContentDto.getLiked() != null) {
                clearSelectLikeDislike(microLectureContentDto.getLiked(), true);
            }
        }
    }

    private void setMicroLectureLikeDislike(LikeDislikeEnum isLike, String chapterId, String microLectureId) {
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("chapterId", chapterId);
        objectMap.put("liked", isLike.name());
        objectMap.put(PreferenceKeys.MICROLECTURE_ID.getKey(), microLectureId);
        ll_like_dislike.setEnabled(false);
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                CommonMethods.getUrlParam(POST_FEEDBACK_LIKE, objectMap), null,
                response -> {
                    Log.d(TAG, "Response: " + response.toString());
                    TypeToken<LikeDislikeResponse> token = new TypeToken<LikeDislikeResponse>() {
                    };
                    LikeDislikeResponse likeDislikeResponse = gson.fromJson(response.toString(), token);
                    if (likeDislikeResponse.isSuccess()) {
                        if (microLectureContentDto != null && microLectureContentDto.getId().equals(microLectureId)) {
                            microLectureContentDto.setLiked(isLike.getIsLike());
                            setVideoLikeDislike(false);
                        }
                    }
                    ll_like_dislike.setEnabled(true);
                }, error -> {
//            Log.d(TAG, "Error: "+error.toString());
            ll_like_dislike.setEnabled(true);
            setVideoLikeDislike(true);
            Toast.makeText(VideoPlayerActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
        });
        myJsonObjectRequest.setTag(LIKE_DISLIKE_VOLLEY_TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
    }

    private void showFeedbackDialog(boolean isBackPressed) {

        if (isBackPressed)
            player.pause();

        setLectureFeedbackDialog(
                new OnFeedbackDialogClickListener() {
                    @Override
                    public void onPrimaryButtonClick(boolean status, String feedback, Object object) {
                        // On Feedback Given
                        LikeDislikeEnum likeDislikeEnum = (LikeDislikeEnum) object;
//                        feedbackGiven = true;
                        showFeedbackDescriptiveDialog(likeDislikeEnum == LikeDislikeEnum.LIKE, isBackPressed);
                    }

                    @Override
                    public void onSecondaryButtonClick(boolean status, String feedback, Object object) {
                        // Cancels feedback & Continue
                        if (isBackPressed)
                            player.play();
                        else
                            checkContinuity();
                        firebaseLectureEvent(null, "", new ArrayList<>());
                    }

                    @Override
                    public void onDismissed() {
                        // Dismiss feedback
                    }

                    @Override
                    public void onCanceled(boolean status, String feedback, Object object) {
                        // Cancels feedback
                        if (isBackPressed)
                            onBackPressedActivity();
                        else
                            checkContinuity();
                        firebaseLectureEvent(null, "", new ArrayList<>());
                    }
                }
        );

        feedbackDialogShown = true;
    }

    private void showFeedbackDescriptiveDialog(boolean isLike, boolean isBackPressed) {

        setLectureFeedbackDescriptiveDialog(
                isLike,
                new OnFeedbackDialogClickListener() {
                    @Override
                    public void onPrimaryButtonClick(boolean status, String feedback, Object object) {
                        // On Feedback Given
                        ArrayList<String> selectedFeedbacks = (ArrayList<String>) object;
                        sendFeedbackAPI(isLike, selectedFeedbacks, feedback);
                        showFeedbackThankYouDialog(isBackPressed);
                        firebaseLectureEvent(isLike, feedback, selectedFeedbacks);
                    }

                    @Override
                    public void onSecondaryButtonClick(boolean status, String feedback, Object object) {
                        // Cancels feedback & Continue
                        if (isBackPressed)
                            player.play();
                        else
                            checkContinuity();
                        firebaseLectureEvent(isLike, "", new ArrayList<>());
                        sendFeedbackAPI(isLike, new ArrayList<>(), "");
                    }

                    @Override
                    public void onDismissed() {
                        // Dismiss feedback

                    }

                    @Override
                    public void onCanceled(boolean status, String feedback, Object object) {
                        // Cancels feedback
                        if (isBackPressed)
                            onBackPressedActivity();
                        else
                            checkContinuity();
                        firebaseLectureEvent(isLike, "", new ArrayList<>());
                        sendFeedbackAPI(isLike, new ArrayList<>(), "");
                    }
                }
        );
    }

    private void showFeedbackThankYouDialog(boolean isBackPressed) {
        setLectureFeedbackFinalDialog(
                new OnFeedbackDialogClickListener() {
                    @Override
                    public void onPrimaryButtonClick(boolean status, String feedback, Object object) {
                    }

                    @Override
                    public void onSecondaryButtonClick(boolean status, String feedback, Object object) {
                    }

                    @Override
                    public void onDismissed() {
                        if (isBackPressed)
                            onBackPressedActivity();
                        else
                            checkContinuity();
                    }

                    @Override
                    public void onCanceled(boolean status, String feedback, Object object) {
//                        player.play();
                    }
                }
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Map<String, String> params = new HashMap<>();
        params.put("result", String.valueOf(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED));
        params.put("triggerSource", interactiveQuestionActive ? "interactiveQuestion" : "askDoubtButton");
        params.put("ChapterId", microLectureContentDto.getChapterId());
        params.put("ChapterName", microLectureContentDto.getChapterName());
        params.put("MicrolectureId", microLectureContentDto.getId());
        params.put("MicrolectureName", microLectureContentDto.getTitle());
        params.put("SubjectName", getIntent().getStringExtra("selectedSubject"));
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.vp_mic_perm_request_result.getActionName(), params);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            permissionHandled = true;
            handleMicPermissionGranted();
        } else {
            if (!isFinishing() && !isDestroyed()) {
                showCustomMicPermissionDialog();
            }
        }
    }

    @Override
    public void onError(int i) {
        super.onError(i);

        if (isFinishing() || isDestroyed()) return; //

        if (interactiveQuestionActive) {
            askButtonViewModel.changeState(AskButtonState.STATE_API_CALLING);
            validateInteractiveAnswerAPI("xyxyxy", currentInteractiveQuestion.getId(), customTimeBar);
        } else if (askButtonViewModel.getCurrentState() != AskButtonState.STATE_RESUME) {
            askButtonViewModel.changeState(AskButtonState.STATE_ASK);
//            if (!isFinishing() && !isDestroyed()) {
//                relativeLayoutSubmitDoubt.show();
//            }
            if (!player.isPlaying() && player.getPlaybackState() != Player.STATE_BUFFERING) {
                showEnterAskDoubtDialog();
                setLayoutAndChildrenEnabled(exoplayerControlLayout, true);
            }
        }
        listeningLayout.setVisibility(View.GONE);

    }

    private void setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(this);
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {
        if (isFinishing() || isDestroyed()) return;

        isSpeechActive = true;
        askButtonViewModel.changeState(AskButtonState.STATE_CANCEL);
        super.onReadyForSpeech(bundle);
    }

    @Override
    public void onEndOfSpeech() {
        if (isFinishing() || isDestroyed()) return;

        isSpeechActive = false;
        super.onEndOfSpeech();
    }

    @Override
    public void onPartialResults(Bundle bundle) {
        if (isFinishing() || isDestroyed()) return;

        ArrayList<String> transcripts = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (transcripts != null && !transcripts.isEmpty()) {
            String result = transcripts.get(0);
            speechTextDetected.append(" ").append(result);
        }
        super.onPartialResults(bundle);
    }

    @Override
    public void askDoubtSuccessCallback(DoubtResponseDto doubtResponseDto, String questionFromStudent) {
        askButtonViewModel.changeState(AskButtonState.STATE_ASK);
        Log.d(TAG, "askDoubtSuccessCallback success " + doubtResponseDto);
        if (doubtResponseDto != null && doubtResponseDto.isFound() && !doubtResponseDto.isDisambiguation() && !doubtResponseDto.isTextSol()) {
            showVideoSolution(doubtResponseDto.getContentUrl(), doubtResponseDto.getPosition(), doubtResponseDto.getLength());
        } else if (doubtResponseDto != null && doubtResponseDto.isFound() && doubtResponseDto.isDisambiguation()) {
            showDisambiguationOptions(doubtResponseDto.getAllResults(), questionFromStudent);
        } else {
            Toast.makeText(this, getString(R.string.get_solution_from_tutors), Toast.LENGTH_SHORT).show();
        }
    }

    private void showDisambiguationOptions(List<MLResponse> topThreeResults, String questionFromStudent) {
        try {
            List<MLResponse> topThree = new ArrayList<>();
            for (MLResponse topThreeResult : topThreeResults) {
                if (topThreeResult.getTextToDisplay() != null) {
                    topThree.add(topThreeResult);
                }
            }

            RecyclerView rlv = disambiguation_dialog.findViewById(R.id.rlview);
            DisambiguationAdapter disambiguationAdapter = new DisambiguationAdapter(topThree,
                    this, questionFromStudent);
            rlv.setLayoutManager(new LinearLayoutManager(this));
            rlv.setAdapter(disambiguationAdapter);
            disambiguation_dialog.show();
            disambiguation_dialog.getWindow().getDecorView().post(() -> {
                hideNavigationBar();
            });
        } catch (Exception e) {
            player.play();
        }

    }

    private void showVideoSolution(String contentUrl, long position, int length) {
        addPlayList(contentUrl, position, position + length);
        crouton = Crouton.makeText(VideoPlayerActivity.this, getString(R.string.instant_solution), style);
        crouton.show();
    }

    @Override
    public void askDoubtFailureCallback(String error, String userQuestion) {
        askButtonViewModel.changeState(AskButtonState.STATE_ASK);

        if (StringUtils.isNotEmpty(userQuestion)) {
            relativeLayoutEnterSubmitDoubt.show();
            relativeLayoutEnterSubmitDoubt.getWindow().getDecorView().post(() -> {
                        hideNavigationBar();
            });
        }

        //show error if api failed
        if (!MyApplication.isNetworkAvailable()) {
            Toast.makeText(this, getString(R.string.internet_failure_error), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
        }

        Log.d(TAG, "askDoubtSuccessCallback failed " + error);
    }

    @Override
    public void setCroutonInfoInUI(List<SnackBarDto> snackBarDtoList) {
        for (SnackBarDto snackBarDto : snackBarDtoList) {
            player.createMessage(
                            (messageType, payload) -> {
                                SnackBarDto data = (SnackBarDto) payload;
                                crouton = Crouton.makeText(VideoPlayerActivity.this, data.getSnackBarText(), style);
                                crouton.show();
                            })
                    .setLooper(Looper.getMainLooper())
                    .setPosition(0, snackBarDto.getStartPos())
                    .setPayload(snackBarDto)
                    .setDeleteAfterDelivery(false)
                    .send();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResults(Bundle bundle) {

        if (isFinishing() || isDestroyed()) return;

        setLayoutAndChildrenEnabled(exoplayerControlLayout, true);
        askButtonViewModel.changeState(AskButtonState.STATE_ASK);

        ArrayList<String> transcripts = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        Log.d(TAG, "Received transcripts: " + transcripts);

        String speechRecognitionResult;
        if (transcripts != null && !transcripts.isEmpty()) {
            speechRecognitionResult = transcripts.get(0);
        } else {
            speechRecognitionResult = speechTextDetected.toString();
        }

      
        handleOnResultSuccess(speechRecognitionResult);

        
        super.onResults(bundle);
    }


    private void handleOnResultSuccess(String result) {

        Map<String, String> params = new HashMap<>();
        Log.d("InteractiveScenario", "data = " + interactiveQuestionActive);
        if (interactiveQuestionActive) {
            params.put("QuestionId", currentInteractiveQuestion.getId());
            params.put("UserAnswer", result);
            params.put("ChapterId", microLectureContentDto.getChapterId());
            params.put("ChapterName", microLectureContentDto.getChapterName());
            params.put("MicrolectureId", microLectureContentDto.getId());
            params.put("MicrolectureName", microLectureContentDto.getTitle());
            params.put("SubjectName", getIntent().getStringExtra("selectedSubject"));
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.user_interactive_stt.getActionName(), params);
            askButtonViewModel.changeState(AskButtonState.STATE_API_CALLING);
            validateInteractiveAnswerAPI(result, currentInteractiveQuestion.getId(), customTimeBar);
        } else {

            params.put("UserDoubt", result);
            params.put("ChapterId", microLectureContentDto.getChapterId());
            params.put("ChapterName", microLectureContentDto.getChapterName());
            params.put("MicrolectureId", microLectureContentDto.getId());
            params.put("MicrolectureName", microLectureContentDto.getTitle());
            params.put("SubjectName", getIntent().getStringExtra("selectedSubject"));
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.user_ask_doubt_stt.getActionName(), params);

            mlResponses.clear();
            instantVideoRecyclerAdapter.notifyDataSetChanged();
            askButtonViewModel.changeState(AskButtonState.STATE_API_CALLING);
            contextualizedDoubtApi(microLectureContentDto.getChapterId(), currentVideoPosition == 0 ? player.getCurrentPosition() : currentVideoPosition, result, microLectureContentDto.getId(), selectedSubject);
        }
    }

    public void controllerClickListener() {
        rewind.setOnClickListener(view -> {
            if (disableControllerAction) {
                Toast.makeText(this, getString(R.string.cannot_go_backward), Toast.LENGTH_SHORT).show();
            } else {
                if (player != null) {
                    triggerSkipAnimation(rewindLabel, rewind, rewindArrow, true);
                    long currentPosition = player.getCurrentPosition();
                    player.seekTo(currentPosition - SEEK_TIMER);
                }
            }
        });

        forward.setOnClickListener(view -> {
            if (disableControllerAction) {
                Toast.makeText(this, getString(R.string.cannot_go_forward), Toast.LENGTH_SHORT).show();
            } else if (player != null) {
                triggerSkipAnimation(forwardLabel, forward, forwardArrow, false);
                long currentPosition = player.getCurrentPosition();
                player.seekTo(currentPosition + SEEK_TIMER);
            }
        });

//        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
//
//            @Override
//            public boolean onDoubleTap(MotionEvent e) {
//
//                Log.d("VideoInteractivityTesting", "On Double Tap: "+disableControllerAction);
//                if (disableControllerAction) {
////                    Toast.makeText(this, getString(R.string.cannot_go_forward), Toast.LENGTH_SHORT).show();
//                } else if (player != null) {
//                    float x = e.getX();
//                    int width = videoFullScreenPlayer.getWidth();
//                    long currentPosition = player.getCurrentPosition();
//
//                    if (x < width / 2f) {
//                        player.seekTo(currentPosition - SEEK_TIMER);
//                    } else {
//                        player.seekTo(currentPosition + SEEK_TIMER);
//                    }
//                }
//
//                return true;
//            }
//        });

        gestureDetector = new GestureDetector(
                this,
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        return false;
                    }

                    @Override
                    public void onLongPress(@NonNull MotionEvent e) {
                        super.onLongPress(e);
                    }
                }
        );

        videoPlayerSettings.setOnClickListener(view -> {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.vp_settings_click.getActionName());
//            setLectureFeedbackFinalDialog();
            closeSettingLayout();
        });

        playbackSpeed.setOnClickListener(view -> {
            if (settingsRecyclerView.getVisibility() == View.GONE) {
                //set title
                videoPlayerSettingTitle.setText(getString(R.string.choose_playback_speed));

                optionLayout.setVisibility(View.GONE);
                Map<String, String> params = new HashMap<>();
                params.put("currentPlaybackSpeed", selectedPlaybackSpeed + "x");
                params.put("ChapterId", microLectureContentDto.getChapterId());
                params.put("ChapterName", microLectureContentDto.getChapterName());
                params.put("MicrolectureId", microLectureContentDto.getId());
                params.put("MicrolectureName", microLectureContentDto.getTitle());
                params.put("SubjectName", getIntent().getStringExtra("selectedSubject"));
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.vp_settings_popup_playback_speed_click.getActionName(), params);
                settingsRecyclerView.setAdapter(videoSpeedPlaybackAdapter);
                setAdapterHeight(settingsRecyclerView);
                settingsRecyclerView.setVisibility(View.VISIBLE);
            } else {
                settingsRecyclerView.setVisibility(View.GONE);
            }
        });

        videoQuality.setOnClickListener(view -> {
            if (player.getMediaItemCount() > 1) {
                Toast.makeText(this, getString(R.string.cannot_change_quality), Toast.LENGTH_SHORT).show();
            } else if (videoQualityDtoList.size() == 0) {
                Toast.makeText(this, getString(R.string.only_one_quality), Toast.LENGTH_SHORT).show();
            } else {
                if (settingsRecyclerView.getVisibility() == View.GONE) {
                    //set title
                    videoPlayerSettingTitle.setText(getString(R.string.choose_video_quality));

                    optionLayout.setVisibility(View.GONE);
                    Map<String, String> params = new HashMap<>();
                    params.put("currentVideoQuality", selectedQuality);
                    params.put("ChapterId", microLectureContentDto.getChapterId());
                    params.put("ChapterName", microLectureContentDto.getChapterName());
                    params.put("MicrolectureId", microLectureContentDto.getId());
                    params.put("MicrolectureName", microLectureContentDto.getTitle());
                    params.put("SubjectName", getIntent().getStringExtra("selectedSubject"));
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.vp_settings_popup_video_quality_click.getActionName(), params);
                    settingsRecyclerView.setAdapter(videoQualityAdapter);
                    setAdapterHeight(settingsRecyclerView);
                    settingsRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    settingsRecyclerView.setVisibility(View.GONE);
                }
            }
        });

        submitSolutionTextOption.setOnClickListener(view -> {
            solutionOptionDialog.dismiss();
            String doubtChatSessionId = CommonMethods.stringToMd5(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()) + "_" + microLectureContentDto.getId() + "_" + Math.random());
            CommonNavigator.navigateToDoubtChatPseudoLive(this, doubtChatSessionId, selectedSubject, correctedDoubt, false);
        });

        submitSolutionVideoOption.setOnClickListener(view -> {
            solutionOptionDialog.dismiss();
            String doubtChatSessionId = CommonMethods.stringToMd5(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()) + "_" + microLectureContentDto.getId() + "_" + Math.random());
            CommonNavigator.navigateToDoubtChatPseudoLive(this, doubtChatSessionId, selectedSubject, correctedDoubt, true);
        });

        backToPreviousMedia.setOnClickListener(view -> {
            if (player.getMediaItemCount() > 1) {
                seekToPreviousVideo(player.getMediaItemCount());
            }
        });

        videoFullScreenPlayer.setControllerVisibilityListener((PlayerView.ControllerVisibilityListener) visibility -> {
            if (visibility == View.GONE) {
                closeSpeedDialog();
            }
        });


        videoFullScreenPlayer.setOnTouchListener((v, event) -> {
            float x = event.getX();
            boolean isLeft = x < videoFullScreenPlayer.getWidth() / 2f;
            long now = System.currentTimeMillis();


            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN: {
                    longPressRunnable = () -> {
                        if (player.isPlaying()) {
                            isLongPressActive = true;
                            set2xPlayerSpeed(); //speed 2x
                        }
                    };
                    longPressHandler.postDelayed(longPressRunnable, LONG_PRESS_DELAY);
                    return true;
                }

                case MotionEvent.ACTION_UP: {
                    longPressHandler.removeCallbacks(longPressRunnable);

                    if (isLongPressActive) {
                        isLongPressActive = false;
                        resetPlaybackSpeed(); //speed reset
                        return true;
                    }


                    //double tap
                    boolean sameSideSingleTap = (isLeft == lastTapWasLeft);
                    boolean sameSideAction = (isLeft == lastAppliedTapWasLeft);
                    boolean withinGap = (now - lastTapTime) < TAP_DELAY;
                    boolean inSeekMode = isSeekMode && sameSideAction && (now - lastTapTime) < SEEK_RESET_DELAY;

                    if (!sameSideAction || !sameSideSingleTap) {
                        seekCount = 0;
                        rewindArrow.setVisibility(View.INVISIBLE);
                        rewindLabel.setVisibility(View.INVISIBLE);
                        rewind.setVisibility(View.VISIBLE);

                        forwardArrow.setVisibility(View.INVISIBLE);
                        forwardLabel.setVisibility(View.INVISIBLE);
                        forward.setVisibility(View.VISIBLE);
                    }
                    lastTapWasLeft = isLeft;


                    if ((withinGap && sameSideAction && sameSideSingleTap) || inSeekMode) {
                        tapHandler.removeCallbacks(singleTapRunnable);
                        isSeekMode = true;

                        if (!disableControllerAction) {
                            int cumulativeSeekSeconds = (((seekCount + 1) * 10) / 2);

                            if (isLeft) {
                                triggerSkipAnimation(rewindLabel, rewind, rewindArrow, true);
                                player.seekTo(Math.max(player.getCurrentPosition() - SEEK_TIMER, 0));
                                Map<String, String> params = new HashMap<>();
                                params.put("chapterId", microLectureContentDto.getChapterId());
                                params.put("chapterName", microLectureContentDto.getChapterName());
                                params.put("microlectureId", microLectureContentDto.getId());
                                params.put("microlectureName", microLectureContentDto.getTitle());
                                params.put("skipSeconds", String.valueOf(cumulativeSeekSeconds));
                                MyApplication.logFirebaseBundleCreation(FirebaseEvents.vp_backward_seek_click.getActionName(), params);
                            } else {
                                triggerSkipAnimation(forwardLabel, forward, forwardArrow, false);
                                player.seekTo(player.getCurrentPosition() + SEEK_TIMER);
                                Map<String, String> params = new HashMap<>();
                                params.put("chapterId", microLectureContentDto.getChapterId());
                                params.put("chapterName", microLectureContentDto.getChapterName());
                                params.put("microlectureId", microLectureContentDto.getId());
                                params.put("microlectureName", microLectureContentDto.getTitle());
                                params.put("skipSeconds", String.valueOf(cumulativeSeekSeconds));
                                MyApplication.logFirebaseBundleCreation(FirebaseEvents.vp_forward_seek_click.getActionName(), params);
                            }
                            // reset cumulative burst tracking
//                            seekResetHandler.removeCallbacksAndMessages(null);
//                            seekResetRunnable = () -> seekCount = 0;
//                            seekResetHandler.postDelayed(seekResetRunnable, SEEK_RESET_DELAY);
                        }

                        seekResetHandler.removeCallbacks(seekResetRunnable);
                        seekResetRunnable = () -> {
                            isSeekMode = false;
                            seekCount = 0;
                        };
                        seekResetHandler.postDelayed(seekResetRunnable, SEEK_RESET_DELAY);

                        lastTapTime = now;
                        lastAppliedTapWasLeft = isLeft;
                        return true;
                    }


                    //single tap
                    singleTapRunnable = () -> {
                        isSeekMode = false;
                        seekCount = 0;
                        Map<String, String> params = new HashMap<>();
                        params.put("chapterId", microLectureContentDto.getChapterId());
                        params.put("chapterName", microLectureContentDto.getChapterName());
                        params.put("microlectureId", microLectureContentDto.getId());
                        params.put("microlectureName", microLectureContentDto.getTitle());
                        params.put("tapSide", isLeft ? "left" : "right");
                        params.put("skipSeconds", String.valueOf(10));
                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.vp_single_tap.getActionName(), params);
                        v.performClick();
                    };
                    tapHandler.postDelayed(singleTapRunnable, TAP_DELAY);

                    lastTapTime = now;
                    lastAppliedTapWasLeft = isLeft;
                    return true;
                }


                case MotionEvent.ACTION_CANCEL: {
                    longPressHandler.removeCallbacks(longPressRunnable);
                    if (isLongPressActive) {
                        isLongPressActive = false;
                        resetPlaybackSpeed(); //default speed
                    }
                    return true;
                }
            }

            return false;
        });

    }


    private void triggerSkipAnimation(TextView label, ImageButton btn, ImageView arrow, boolean isLeft) {
        seekCount += isLeft ? -1 : 1;
        label.setText(String.format("%+ds", seekCount * 10));

        //restart
        if (skipFadeRunnable != null) {
            label.removeCallbacks(skipFadeRunnable);
        }
        label.animate().cancel();
        arrow.animate().cancel();

        // show views
        btn.setVisibility(View.INVISIBLE);
        label.setVisibility(View.VISIBLE);
        label.setAlpha(1f);
        arrow.setVisibility(View.VISIBLE);
        arrow.setAlpha(1f);

        label.setScaleX(0.8f);
        label.setScaleY(0.8f);

        label.animate()
                .scaleX(1.15f)
                .scaleY(1.15f)
                .setDuration(120)
                .withEndAction(() ->
                        label.animate()
                                .scaleX(1f).scaleY(1f)
                                .setDuration(120)
                                .start()
                )
                .start();

        arrow.setTranslationX(isLeft ? 30f : -30f);
        arrow.animate().translationX(isLeft ? -30f : 30f).setDuration(350).start();

        skipFadeRunnable = () -> label.postDelayed(() -> {
            arrow.animate()
                    .alpha(0f)
                    .setDuration(250)
                    .withEndAction(() -> {
                        arrow.setVisibility(View.INVISIBLE);
                        arrow.setTranslationX(0f);
                        arrow.setAlpha(1f);
                    })
                    .start();

            label.animate()
                    .alpha(0f)
                    .setDuration(260)
                    .withEndAction(() -> {
                        label.setVisibility(View.INVISIBLE);
                        btn.setVisibility(View.VISIBLE);
                    })
                    .start();
        }, 2000);

        label.postDelayed(skipFadeRunnable, 700);
    }


    private void setAdapterHeight(RecyclerView recyclerView) {
        ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
        int maxHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 220, getResources().getDisplayMetrics());

        recyclerView.post(() -> {
            if (recyclerView.computeVerticalScrollRange() > maxHeight) {
                params.height = maxHeight;
            } else {
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
            recyclerView.setLayoutParams(params);
        });
    }


    private void hideSettingLayout() {
        TooltipHelper.getInstance().dismiss();
        videoPlayerSettingsLayout.setVisibility(View.GONE);
        settingsRecyclerView.setVisibility(View.GONE);
    }

    private void closeSettingLayout() {
        if (View.VISIBLE == videoPlayerSettingsLayout.getVisibility()) {
            hideSettingLayout();
        } else {
            //set title
            videoPlayerSettingTitle.setText(getString(R.string.settings));

            videoPlayerSettingsLayout.setVisibility(View.VISIBLE);
            optionLayout.setVisibility(View.VISIBLE);
        }
    }

    public void setLayoutAndChildrenEnabled(ViewGroup layout, boolean enabled) {
        layout.setEnabled(enabled);
        layout.setClickable(enabled);
        layout.setFocusable(enabled);

        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setEnabled(enabled);
            child.setClickable(enabled);
            child.setFocusable(enabled);

            if (child instanceof ViewGroup) {
                setLayoutAndChildrenEnabled((ViewGroup) child, enabled);
            }
        }
    }

    private void onClickListener() {

        askButton.setOnClickListener(view -> {
            Map<String, String> params = new HashMap<>();
            params.put(PreferenceKeys.MICROLECTURE_ID.getKey(), microLectureContentDto.getId());
            params.put("chapterId", microLectureContentDto.getChapterId());
            params.put("chapterName", microLectureContentDto.getChapterName());
            params.put("microlectureId", microLectureContentDto.getId());
            params.put("microlectureName", microLectureContentDto.getTitle());
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.vp_ask_doubt_click.getActionName(), params);

            if (speechRecognizer != null) {
                if (askButtonViewModel.getCurrentState() == AskButtonState.STATE_ASK) {
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        player.pause();
                        handlePermission();
                    } else {
                        speechRecognizerErrorCount = 1;
                        setLayoutAndChildrenEnabled(exoplayerControlLayout, false);
                        pausePlayerAndStartSpeechRecognizer();
                    }
                } else if (askButtonViewModel.getCurrentState() == AskButtonState.STATE_LISTENING) {
                    speechRecognizer.stopListening();
                    setLayoutAndChildrenEnabled(exoplayerControlLayout, true);
                    askButtonViewModel.changeState(AskButtonState.STATE_ASK);
                } else if (askButtonViewModel.getCurrentState() == AskButtonState.STATE_PAUSE) {
                    speechRecognizer.stopListening();
                    setLayoutAndChildrenEnabled(exoplayerControlLayout, true);
                    askButtonViewModel.changeState(AskButtonState.STATE_RESUME);
                } else if (askButtonViewModel.getCurrentState() == AskButtonState.STATE_RESUME) {
                    speechRecognizerErrorCount = 1;
                    setLayoutAndChildrenEnabled(exoplayerControlLayout, false);
                    pausePlayerAndStartSpeechRecognizer();
                    askButtonViewModel.changeState(AskButtonState.STATE_PAUSE);
                } else if (askButtonViewModel.getCurrentState() == AskButtonState.STATE_CANCEL) {
                    speechRecognizer.stopListening();
                    askButtonViewModel.changeState(AskButtonState.STATE_ASK);
                    setLayoutAndChildrenEnabled(exoplayerControlLayout, true);
                    startVideo();
                }
            } else {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    player.pause();
                    handlePermission();
                }
            }
            hideNavigationBar();
        });


        //enter
        submitMyDoubt_Enter.setOnClickListener(view -> {
            correctedDoubtError_Enter.setErrorEnabled(false);
            correctedDoubt = doubtTextEditText_Enter.getText().toString();
            if (correctedDoubt.trim().isEmpty()) {
                Map<String, String> params = new HashMap<>();
                params.put(PreferenceKeys.MICROLECTURE_ID.getKey(), microLectureContentDto.getId());
                params.put("chapterId", microLectureContentDto.getChapterId());
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.vp_submit_my_doubt_click.getActionName(), params);
                correctedDoubtError_Enter.setError(getString(R.string.More_details_Needed));
                correctedDoubtError_Enter.requestFocus();
            } else {
                if (MyApplication.isNetworkAvailable()) {
//                  MyApplication.logFirebaseBundleCreation();
                    doubtTextEditText_Enter.requestFocus();
                    relativeLayoutEnterSubmitDoubt.dismiss();
                    // solutionOptionDialog.show();
                    mlResponses.clear();
                    instantVideoRecyclerAdapter.notifyDataSetChanged();
                    askButtonViewModel.changeState(AskButtonState.STATE_API_CALLING);
                    contextualizedDoubtApi(microLectureContentDto.getChapterId(), currentVideoPosition == 0 ? player.getCurrentPosition() : currentVideoPosition, correctedDoubt, microLectureContentDto.getId(), selectedSubject);
                } else {
                    Toast.makeText(this, getString(R.string.apna_internet_connection_check_karke_dobara_koshish_karein), Toast.LENGTH_SHORT).show();
                }
            }
        });

        doNotSubmitMyDoubt_Enter.setOnClickListener(view -> {
            Map<String, String> params = new HashMap<>();
            params.put(PreferenceKeys.MICROLECTURE_ID.getKey(), microLectureContentDto.getId());
            params.put("chapterId", microLectureContentDto.getChapterId());
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.lec_doubt_dialog_cancel_click.getActionName());

            correctedDoubtError_Enter.setErrorEnabled(false);
            relativeLayoutEnterSubmitDoubt.dismiss();
            player.setPlayWhenReady(true);
        });


        //edit
        submitMyDoubt_Edit.setOnClickListener(view -> {
            String result = doubtTextEditText_Edit.getText().toString();
            relativeLayoutEditSubmitDoubt.dismiss();
            handleOnResultSuccess(result);
        });

        doNotSubmitMyDoubt_Edit.setOnClickListener(view -> {
            Map<String, String> params = new HashMap<>();
            params.put(PreferenceKeys.MICROLECTURE_ID.getKey(), microLectureContentDto.getId());
            params.put("chapterId", microLectureContentDto.getChapterId());
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.lec_doubt_dialog_cancel_click.getActionName());

            correctedDoubtError_Edit.setErrorEnabled(false);
            relativeLayoutEditSubmitDoubt.dismiss();
            player.setPlayWhenReady(true);
        });

        doubtTextEditText_Enter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() != 0) {
                    correctedDoubtError_Enter.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        doubtTextEditText_Edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() != 0) {
                    correctedDoubtError_Edit.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void pausePlayerAndStartSpeechRecognizer() {
        // For non-competitive profiles: enforce the daily doubt ask limit before doing anything
        if (!interactiveQuestionActive && !MyApplication.isCompetitiveProfile() && !SubscriptionType.isProSubscription()) {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String countKey = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()) + "_doubt_count_" + today;
            int usedCount = MyApplication.getIntDataFromDoNotDeleteSharedPreference(countKey);
            if (usedCount < 0) usedCount = 0;
            // Read per-plan limits from Remote Config; fall back to safe defaults
            long limit;
            if (SubscriptionType.isBasicSubscription()) {
                limit = MyApplication.getFirebaseRemoteConfig().getLong("ask_doubt_daily_limit_basic");
                if (limit <= 0) limit = 3;
            } else {
                // PLUS subscription
                limit = MyApplication.getFirebaseRemoteConfig().getLong("ask_doubt_daily_limit_plus");
                if (limit <= 0) limit = 10;
            }
            if (usedCount >= limit) {
                if (SubscriptionType.isBasicSubscription()) {
                    String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                    boolean isHindi = "hi".equalsIgnoreCase(lang);
                    String btnText = isHindi ? "₹51 में जॉइन करें" : "Join at Rs 51";
                    CommonDialogs.showDoubtLimitDialog(this, "", btnText);
                } else if(SubscriptionNavigationHelper.hideProPlanSubscription()){
                    CommonDialogs.MessageLimitDialog.show(this, CommonDialogs.MessageLimitDialog.Type.DOUBT_SESSION);
                }else {
                    // PLUS subscription — show upgrade popup
                    SubscriptionNavigationHelper.navigateToSubscriptionScreenWithLimitExceed(this, FeatureEnum.ASK_DOUBT_VIDEO, (int) limit);
                }
                setLayoutAndChildrenEnabled(exoplayerControlLayout, true);
                askButtonViewModel.changeState(AskButtonState.STATE_ASK);
                return;
            }
        }

        player.pause();
        speechTextDetected.setLength(0);
//        Log.d("VideoInteractivityTesting", "Mic Listening: "+currentInteractiveQuestion.toString());

        if (interactiveQuestionActive) {
            for (TimeBarThumbData thumbData : thumbDataList) {
                thumbData.setElevation(0);
                thumbData.setActive(false);
                if (thumbData.getQuestionId().equals(currentInteractiveQuestion.getId())) {
                    thumbData.setActive(true);
                    thumbData.setElevation(1);
                }
            }
            customTimeBar.setAdMarkerPositions(thumbDataList, player.getDuration(), 0);
        }

        if (speechRecognizer != null) {
            speechRecognizer.startListening(getRecognizerIntent());
            if (interactiveQuestionActive) {
                askButtonViewModel.changeState(AskButtonState.STATE_PAUSE);
            } else {
                askButtonViewModel.changeState(AskButtonState.STATE_CANCEL);
            }
        }
    }


    private void playHowToVideo() {
        int count = MyApplication.getIntData(LECTURE.name() + "_COUNT" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
        Log.d("debugcount", "getHowToVideoType: " + count + "type" + LECTURE.name());
        if (count < 0)
            count = 0;
        if (count <= 1 && !MyApplication.getFirebaseRemoteConfig().getBoolean("hideHowToVideos")) {
            player.pause();
            Log.d("openExpandedVideoIssue", "paly how to video : ");
//            howToVideoView = HowToVideoView.getInstance(this);
////            howToVideoView.setView(this, how_to_video_view);
//            howToVideoView.setType(LECTURE, false);
//            howToVideoView.setButtonText(getString(R.string.Watch_Now));
//            MyApplication.saveData(LECTURE.name() + "_COUNT" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()), count + 1);
        }
    }

    public void playVideo() {
        String url = "";
        if (selectedVideoQualityDto != null)
            url = selectedVideoQualityDto.getVideoUrl();
        else
            url = microLectureContentDto.getVideoUrl();

        MediaItem mediaItem = new MediaItem
                .Builder()
                .setMediaId(generateRandomString())
                .setMimeType(MimeTypes.APPLICATION_M3U8)
                .setUri(url).build();
        player.setMediaItem(mediaItem);

        player.prepare();
        player.setRepeatMode(Player.REPEAT_MODE_OFF);
//        player.setPlayWhenReady(howToVideoView == null || howToVideoView.player == null || !howToVideoView.player.isPlaying());
        player.addListener(this);
        player.seekTo(microLectureContentDto.getDefaultStartTime());
        player.addAnalyticsListener(this);
        handler = new Handler(Looper.getMainLooper());
        endPositionTrackingHanlder = new Handler(Looper.getMainLooper());

        runPositionUpdateRunnable();
    }

    // @Override
    // public void onVideoDecoderInitialized(
    //         EventTime eventTime,
    //         String decoderName,
    //         long initializedTimestampMs,
    //         long initializationDurationMs
    // ) {
    //     Log.d("EXO_DECODER", "Decoder initialized: " + decoderName);
    // }

    @Override
    public void onMediaItemTransition(@Nullable @org.jetbrains.annotations.Nullable MediaItem mediaItem, int reason) {
        Log.d(TAG, "MediaItemChanged");
        if (player.getMediaItemCount() == 1 && handler != null && positionUpdatingRunnable != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (!handler.hasCallbacks(positionUpdatingRunnable)) {
                    Log.d(TAG, "MediaItemChanged callback started");
                    runPositionUpdateRunnable();
                }
            } else {
                Log.d(TAG, "MediaItemChanged callback started");
                handler.removeCallbacks(positionUpdatingRunnable);
                runPositionUpdateRunnable();
            }
            long trimmedDuration = microLectureContentDto.getVideoCompletionEndPos() - microLectureContentDto.getDefaultStartTime();
            customTimeBar.setAdMarkerPositions(thumbDataList, player.getDuration(), trimmedDuration);
        }
        Player.Listener.super.onMediaItemTransition(mediaItem, reason);
    }


    private void playListChangeHandler() {
        startAnimationOnVideoChange();
    }


    private void runPositionUpdateRunnable() {
        if (runnableStarted) {
            return;
        }
        positionUpdatingRunnable = new Runnable() {
            @Override
            public void run() {
                long currentPosition = player.getCurrentPosition();
                long endPos = microLectureContentDto.getVideoCompletionEndPos();
                customTimeBar.setDuration(endPos);

                if (player != null && player.isPlaying() && !isVideoReachedToApplicableReviewLimit) {
                    long duration = player.getDuration();

                    if (duration > 0) {
                        float progress = (float) currentPosition / duration;
                        Log.d(TAG, "Video Progress: " + progress);
                        trackVideoMilestones((int) (progress * 100));
                        trackLectureCompletionMilestone();
                        if (progress >= 0.80f) {
                            isVideoReachedToApplicableReviewLimit = true;
                            MyApplication.appendCompletedMicroLectureList(microLectureContentDto.getId());
                        }
                    }
                }

                // Retention tracker: periodically check if active watch time crossed threshold.
                // Called every 500ms; internally no-ops once the API has succeeded.
                if (videoRetentionTracker != null) {
                    videoRetentionTracker.tick();
                }

                setLikeDislikeCommentVisibility(currentPosition);
                updateSkipButtonVisibility(currentPosition);

                if (disableControllerAction) {
                    if (currentPosition >= endPosition) {
                        Log.d(TAG, "seeking to fallback " + fallbackPosition);
                        player.seekTo(fallbackPosition);
                        disableControllerAction = false;
                        customTimeBar.setVisibility(View.VISIBLE);
                        exoplayerDurationLayout.setVisibility(View.VISIBLE);
                        askButtonViewModel.changeState(AskButtonState.STATE_ASK);
                    }
                    handler.postDelayed(this, TIME_BUFFER_DIFFERENCE);
                } else if (player != null && player.getMediaItemCount() == 1 && currentPosition <
                        microLectureContentDto.getVideoCompletionEndPos()) {
                    if (Math.abs(lastUpdatedPosition - currentPosition) >= 3000) {
                        saveVideoProgress(currentPosition, board);
                    }
                    currentVideoPosition = currentPosition;

                    Log.i(TAG, "contextualizedDoubtApi currentPosition<:: " + currentPosition);

                    handler.postDelayed(this, TIME_BUFFER_DIFFERENCE);
                } else if (player != null && player.getMediaItemCount() == 1 && currentPosition >=
                        microLectureContentDto.getVideoCompletionEndPos()) {
                    exoplayerDurationLayout.setVisibility(View.GONE);
                    customTimeBar.setVisibility(View.GONE);
                    if (Math.abs(lastUpdatedPosition - currentPosition) >= 3000) {
                        saveVideoProgress(currentPosition, board);
                    }
                    currentVideoPosition = currentPosition;

                    Log.i(TAG, "contextualizedDoubtApi currentPosition>=:: " + currentPosition);

                    player.pause();
                    Log.d("interactivityIssue", "run: ");
                    showFeedback(false);
//                    showFeedbackDialogAtVideoEnd();

                }
            }
        };
        runnableStarted = true;
        positionUpdatingRunnable.run();
    }

    private void trackVideoMilestones(int progress) {
        int milestone = -1;
        if (progress >= 100) milestone = 100;
        else if (progress >= 80) milestone = 100;
        else if (progress >= 50) milestone = 60;
        else if (progress >= 25) milestone = 25;

        if (milestone != -1 && milestone > lastTrackedMilestone) {
            lastTrackedMilestone = milestone;

            Map<String, String> params = new HashMap<>();
            params.put("milestone", String.valueOf(milestone));
            params.put("microlecture_id", microLectureContentDto.getId());
            params.put("microlecture_name", microLectureContentDto.getTitle());
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.video_playback_milestone.getActionName(), params);
        }
    }

    private void setLikeDislikeCommentVisibility(long currentVideoPosition) {
        try {
            if (microLectureContentDto != null) {
                if (microLectureContentDto.getResumePosition() > metadataModel.getShow_like_dislike_comment_min_time_ms()
                        || currentVideoPosition > metadataModel.getShow_like_dislike_comment_min_time_ms()
                        || (microLectureContentDto.getVideoCompletionEndPos() < metadataModel.getShow_like_dislike_comment_min_time_ms() && metadataModel.isShow_like_dislike_for_small_video())) {
                    // player.getMediaItemCount()==1
                    ll_like_dislike.setVisibility((metadataModel.isShow_video_like_dislike() && microLectureContentDto.isLikeDislikeVisible()) ? View.VISIBLE : View.GONE);
                    Log.d(TAG, "metadata: " + metadataModel.isShow_video_like_dislike() + ", dto: " + microLectureContentDto.isLikeDislikeVisible() + ", player media: " + player.getMediaItemCount());
                } else {
                    ll_like_dislike.setVisibility(View.GONE);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void updateSkipButtonVisibility(long currentPosition) {
        if (!isBatchOverview || cvSkip == null || skipToPositionMs <= 0) return;
        boolean shouldShow = currentPosition < skipToPositionMs;
        cvSkip.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
//        lottie_buy_batch.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
    }

    private void initializePlayer() {
        if (player == null) {
            DefaultLoadControl loadControl = new DefaultLoadControl.Builder()
                    .setBufferDurationsMs(
                            5000,  // minBufferMs: Minimum buffer required before starting playback
                            15000, // maxBufferMs: Maximum buffer allowed
                            5000,  // bufferForPlaybackMs: Buffer required before starting playback (audio or video)
                            5000   // bufferForPlaybackAfterReBufferMs: Buffer required after a reBuffer event
                    )
                    .build();
            player = new ExoPlayer.Builder(this)
                    .setLoadControl(loadControl)
                    .setSeekBackIncrementMs(5000)
                    .setSeekForwardIncrementMs(5000)
                    .build();
            videoFullScreenPlayer.setPlayer(player);
        }
    }

    @Override
    public void onIsPlayingChanged(boolean isPlaying) {
        if (isPlaying && onCreate) {
            onCreate = false;
            previewImage.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            if (!isBatchOverview) askButton.setVisibility(View.VISIBLE);
        }

        if (isPlaying && askButtonViewModel.getCurrentState() == AskButtonState.STATE_RESUME) {
            Log.d("PlayingDoubt", "changed");
            interactiveQuestionActive = false;
            askButtonViewModel.changeState(AskButtonState.STATE_ASK);
        }

        // Retention tracker: start/stop the active-time stopwatch based on actual playback state.
        // Pauses, buffering, phone calls, minimize — all correctly stop the clock.
        if (videoRetentionTracker != null) {
            if (isPlaying) {
                videoRetentionTracker.onPlaybackStarted();
            } else {
                videoRetentionTracker.onPlaybackPaused();
            }
        }

        Player.Listener.super.onIsPlayingChanged(isPlaying);
    }

    private void handleSettingsTooltip() {
        savedSelectedQuality = MyApplication.getStringData(getString(R.string.user_selected_video_quality));
        if (!videoQualityDtoList.isEmpty() && savedSelectedQuality == null) {
            tooltipHandler = new Handler();
            tooltipRunnable = () -> {
                player.pause();
                videoPlayerSettings.performClick();
                videoQuality.performClick();
//                MyTooltipDto myTooltipDto = MyTooltipDto.builder()
//                        .text(getString(R.string.Video_quality_select_karein))
//                        .isRightAnchor(true)
//                        .alignment(RelativeLayout.ALIGN_PARENT_TOP)
//                        .autoDismiss(true)
//                        .hideArrow(true)
//                        .build();
//                TooltipHelper.getInstance().showTooltip(videoPlayerSettingsLayout, myTooltipDto, this);
                tooltip = new MyToolTip(videoPlayerSettingsLayout, VideoPlayerActivity.this);
                MyApplication.saveData(getString(R.string.user_selected_video_quality), selectedQuality);
                MyApplication.getUserSelectedVideoQualityDatabaseReference().setValue(selectedQuality);
            };
            tooltipHandler.postDelayed(tooltipRunnable, 1000);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!permissionHandled && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            permissionHandled = true;
            setupSpeechRecognizer();
        }

        if (permissionSettingsHandled) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                handleMicPermissionGranted();
            } else {
                handleMicPermissionDeny();
            }
        }

//        else if (shouldShowPermissionDialog && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            Log.d("TAG", "onResumecalled: " + notificationPermissionView);
//            if (notificationPermissionView != null) {
//                notificationPermissionView = new PermissionRequestView(this);
//                notificationPermissionView.initPermission(PermissionType.MICROPHONE, true);
//            }
//        }

        // onPause() stops the position-tracking runnable (runnableStarted = false).
        // Restart it here so that end-of-wrong-video detection and video completion
        // tracking both work correctly after returning from the background.
        if (player != null && !runnableStarted) {
            runPositionUpdateRunnable();
        }

        resumeLottieAnimations();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Retention tracker: stop the active-time clock before player pauses,
        // so background/minimize/call time is never counted.
        if (videoRetentionTracker != null) {
            videoRetentionTracker.onPlaybackPaused();
        }

        if (player != null) {
            player.pause();
        }

        if (handler != null && positionUpdatingRunnable != null) {
            runnableStarted = false;
            handler.removeCallbacks(positionUpdatingRunnable);
        }

        if (animationHandler != null && animationRunnable != null) {
            animationHandler.removeCallbacks(animationRunnable);
        }

        pauseAllLottieAnimations();
    }

    @Override
    protected void showFeedback(boolean isBackPressed) {
        showFeedbackDialog(isBackPressed);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Retention tracker: flush any remaining active segment and clean up.
        if (videoRetentionTracker != null) {
            videoRetentionTracker.release();
        }
        logVideoPositionEvent();
        removeHandlerCallback();
        if (player != null) {
            player.release();
        }

        if (tooltipHandler != null)
            tooltipHandler.removeCallbacksAndMessages(null);

        if (resumeLectureLayoutDialog != null && resumeLectureLayoutDialog.isShowing()) {
            resumeLectureLayoutDialog.dismiss();
        }

        if (tapHandler != null) tapHandler.removeCallbacksAndMessages(null);
        if (longPressHandler != null) longPressHandler.removeCallbacksAndMessages(null);
        if (seekResetHandler != null) seekResetHandler.removeCallbacksAndMessages(null);

        cancelAllLottieAnimations();
        MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll(LIKE_DISLIKE_VOLLEY_TAG);
    }

    // ── Lottie lifecycle helpers ──────────────────────────────────────────────

    private void pauseAllLottieAnimations() {
        try {
            if (playerBufferingLogo != null) playerBufferingLogo.pauseAnimation();
            if (lottie_buy_batch != null) lottie_buy_batch.pauseAnimation();
            if (lottie_like != null && lottie_like.isAnimating()) lottie_like.pauseAnimation();
            if (lottie_dislike != null && lottie_dislike.isAnimating())
                lottie_dislike.pauseAnimation();
            if (apiProcessingAnimation != null) apiProcessingAnimation.pauseAnimation();
            if (celebration1 != null) celebration1.pauseAnimation();
            if (celebration2 != null) celebration2.pauseAnimation();
            if (celebration3 != null) celebration3.pauseAnimation();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void resumeLottieAnimations() {
        try {
            if (playerBufferingLogo != null && playerBufferingLogo.getVisibility() == View.VISIBLE)
                playerBufferingLogo.resumeAnimation();
            if (lottie_buy_batch != null && lottie_buy_batch.getVisibility() == View.VISIBLE)
                lottie_buy_batch.resumeAnimation();
            if (apiProcessingAnimation != null && apiProcessingAnimation.getVisibility() == View.VISIBLE)
                apiProcessingAnimation.resumeAnimation();
            if (celebration1 != null && celebration1.getVisibility() == View.VISIBLE)
                celebration1.resumeAnimation();
            if (celebration2 != null && celebration2.getVisibility() == View.VISIBLE)
                celebration2.resumeAnimation();
            if (celebration3 != null && celebration3.getVisibility() == View.VISIBLE)
                celebration3.resumeAnimation();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void cancelAllLottieAnimations() {
        try {
            if (playerBufferingLogo != null) playerBufferingLogo.cancelAnimation();
            if (lottie_buy_batch != null) lottie_buy_batch.cancelAnimation();
            if (lottie_like != null) lottie_like.cancelAnimation();
            if (lottie_dislike != null) lottie_dislike.cancelAnimation();
            if (apiProcessingAnimation != null) apiProcessingAnimation.cancelAnimation();
            if (celebration1 != null) celebration1.cancelAnimation();
            if (celebration2 != null) celebration2.cancelAnimation();
            if (celebration3 != null) celebration3.cancelAnimation();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void logVideoPositionEvent() {
        if (player != null &&
                microLectureContentDto != null &&
                !feedbackDialogShown &&
                player.getMediaItemCount() == 1 &&
                player.getCurrentPosition() < microLectureContentDto.getVideoCompletionEndPos()) {
            Map<String, String> params = new HashMap<>();
            params.put("ChapterId", microLectureContentDto.getChapterId());
            params.put("ChapterName", microLectureContentDto.getChapterName());
            params.put("MicrolectureId", microLectureContentDto.getId());
            params.put("MicrolectureName", microLectureContentDto.getTitle());
            params.put("SubjectName", getIntent().getStringExtra("selectedSubject"));
            params.put("breakPosition", String.valueOf(player.getCurrentPosition()));
            params.put("startPosition", String.valueOf(microLectureContentDto.getDefaultStartTime()));
            params.put("endPosition", String.valueOf(microLectureContentDto.getVideoCompletionEndPos()));
            params.put("Completed", String.valueOf(isVideoReachedToApplicableReviewLimit));

            MyApplication.logFirebaseBundleCreation(FirebaseEvents.micro_lecture_completed.getActionName(), params);
        }
    }


    @Override
    public void onPlaybackStateChanged(int playbackState) {

        if (playbackState == Player.STATE_BUFFERING) {
            if (playerBufferingLogo != null) {
                playerBufferingLogo.setVisibility(View.VISIBLE);
                playerBufferingLogo.playAnimation();
                arivihanLogo.setVisibility(View.GONE);

            }
        } else if (playbackState == Player.STATE_READY) {

            long endPos = microLectureContentDto.getVideoCompletionEndPos();
            customTimeBar.setDuration(endPos);

            if (playerBufferingLogo != null) {
                playerBufferingLogo.setVisibility(View.GONE);
                playerBufferingLogo.pauseAnimation();
                arivihanLogo.setVisibility(View.VISIBLE);
            }

            if (playlistStartPosition > 0) {
                player.seekTo(playlistStartPosition);
                playlistStartPosition = -1;
            }

            if (player.getMediaItemCount() == 1) {
                setDurationText(microLectureContentDto.getVideoCompletionEndPos() - microLectureContentDto.getDefaultStartTime());
            } else {
                setDurationText(player.getDuration());
                backToPreviousMedia.setVisibility(View.VISIBLE);
            }

            if (!controllerInitialized && player.getMediaItemCount() == 1) {
                if (!shouldNotResume) {
                    shouldNotResume = true;
                    showResumeLecture(microLectureContentDto.getResumePosition());
                } else {
                    //                   playHowToVideo();
                    handleSettingsTooltip();
                }
                controllerInitialized = true;
                customTimeBar.setAdMarkerPositions(thumbDataList, player.getDuration(), microLectureContentDto.getVideoCompletionEndPos());

                if (!speechRecognizerInitialized) {
                    permissionHandler = new Handler();
                    permissionRunnable = () -> {
                        if (player != null) {
                            initializeSpeechRecognizer();
                        }
                    };
                    permissionHandler.postDelayed(permissionRunnable, 500);
                    speechRecognizerInitialized = true;
                }
            }
        } else if (playbackState == Player.STATE_ENDED && !disableControllerAction) {
            int mediaItemCount = player.getMediaItemCount();
            long currentPosition = player.getCurrentPosition();
            long videoCompletionEndPos = microLectureContentDto.getVideoCompletionEndPos();

            Log.d(TAG, "Playback state changed to STATE_ENDED");
            Log.d(TAG, "Total media items: " + mediaItemCount);
            Log.d(TAG, "Current media item index: " + player.getCurrentMediaItemIndex());
            Log.d(TAG, "Current position: " + currentPosition);
            Log.d(TAG, "Video completion end position: " + videoCompletionEndPos);

            if (mediaItemCount > 1 && player.hasPreviousMediaItem()) {
                Log.d(TAG, "Seeking to previous video");
                seekToPreviousVideo(mediaItemCount);
            } else if (currentPosition >= videoCompletionEndPos) {
                exoplayerDurationLayout.setVisibility(View.GONE);
                customTimeBar.setVisibility(View.GONE);
                Log.d(TAG, "Pausing player and showing feedback dialog" + currentPosition + "endpoistion-" + videoCompletionEndPos);
                player.pause();
                Log.d("interactivityIssue", ":onPlaybackStateChanged Playback ended, triggering feedback dialog");
                showFeedback(false);
//                showFeedbackDialogAtVideoEnd();
            }

            Log.d(TAG, "Media item sync update: " + mediaItemCount);
        } else if (playbackState == Player.STATE_ENDED && disableControllerAction) {
            // Wrong-answer explanation video ended (STATE_ENDED). Resume main lecture.
            // player.play() is needed because the player stopped at STATE_ENDED and
            // seekTo alone does not restart playback.
            player.seekTo(fallbackPosition);
            player.play();
            disableControllerAction = false;
            customTimeBar.setVisibility(View.VISIBLE);
            exoplayerDurationLayout.setVisibility(View.VISIBLE);
            if (!isBatchOverview) askButton.setVisibility(View.VISIBLE);
            askButtonViewModel.changeState(AskButtonState.STATE_ASK);
        }

        Player.Listener.super.onPlaybackStateChanged(playbackState);
    }

    public void addPlayList(String videoUrl, long startPosition, long endPosition) {
        playlistStartPosition = startPosition;

        MediaItem current = player.getMediaItemAt(player.getCurrentMediaItemIndex());
        if (current != null) {
            previousStackPosition.put(current.mediaId, player.getCurrentPosition());
        }

        MediaItem.ClippingConfiguration clipConfig = new MediaItem.ClippingConfiguration.Builder()
                .setStartPositionMs(0)
                .setEndPositionMs(endPosition)
                .build();

        MediaItem mediaItem = new MediaItem.Builder()
                .setMediaId(generateRandomString())
                .setMimeType(MimeTypes.APPLICATION_M3U8)
                .setUri(videoUrl)
                .setClippingConfiguration(clipConfig)
                .build();

        player.addMediaItem(mediaItem);
        player.prepare();
        player.seekToNextMediaItem();
        player.setPlayWhenReady(true);

        // ✅ solution mode UI
        customTimeBar.setVisibility(View.GONE);
        backToPreviousMedia.setVisibility(View.VISIBLE);
    }

    private void seekToPreviousVideo(int mediaItemCount) {
        player.seekToPreviousMediaItem();
        player.removeMediaItem(mediaItemCount - 1);

        if (player.getCurrentMediaItem() != null) {
            String id = player.getCurrentMediaItem().mediaId;
            if (previousStackPosition.containsKey(id)) {
                long restorePos = previousStackPosition.get(id);
                player.seekTo(restorePos);
                player.setSeekParameters(SeekParameters.EXACT);
                previousStackPosition.remove(id);
            } else {
                player.seekTo(0);
            }
        }

        customTimeBar.setVisibility(View.VISIBLE);
        exoplayerDurationLayout.setVisibility(View.VISIBLE);
        if (!isBatchOverview) askButton.setVisibility(View.VISIBLE);
        disableControllerAction = false;

        if (player.getMediaItemCount() == 1) {
            backToPreviousMedia.setVisibility(View.GONE);
        }
        player.play();
    }


    public void showResumeLecture(long resumePosition) {
        player.pause();
        resumeLectureLayoutDialog = new Dialog(this);
        resumeLectureLayoutDialog.setContentView(R.layout.continue_lecture_layout);
        if (resumeLectureLayoutDialog.getWindow() != null) {
            resumeLectureLayoutDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
            resumeLectureLayoutDialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(this, R.drawable.bachground_custom_dialogu_typeactivity));
            resumeLectureLayoutDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        resumeLectureLayoutDialog.setCancelable(false);
        resumeLectureLayoutDialog.show();
        resumeLectureLayoutDialog.getWindow().getDecorView().post(() -> {
            hideNavigationBar();
        });

        CardView resumeLecture = resumeLectureLayoutDialog.findViewById(R.id.resume_lecture);
        CardView watchFromBeginning = resumeLectureLayoutDialog.findViewById(R.id.watch_from_beginning);
        ImageView closeButton = resumeLectureLayoutDialog.findViewById(R.id.close_btn);
        closeButton.setOnClickListener(view -> {
            Map<String, String> params = new HashMap<>();
            params.put(PreferenceKeys.MICROLECTURE_ID.getKey(), microLectureContentDto.getId());
            params.put("chapterId", microLectureContentDto.getChapterId());
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.ml_close_btn_click.getActionName(), params);
            player.seekTo(resumePosition);
            player.setPlayWhenReady(true);
            resumeLectureLayoutDialog.dismiss();
//            playHowToVideo();
            handleSettingsTooltip();
        });

        resumeLecture.setOnClickListener(view -> {
            Map<String, String> params = new HashMap<>();
            params.put(PreferenceKeys.MICROLECTURE_ID.getKey(), microLectureContentDto.getId());
            params.put("chapterId", microLectureContentDto.getChapterId());
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.ml_resume_lecture_click.getActionName(), params);
            player.seekTo(resumePosition);
            player.setPlayWhenReady(true);
            resumeLectureLayoutDialog.dismiss();
//           playHowToVideo();
            handleSettingsTooltip();
        });

        watchFromBeginning.setOnClickListener(view -> {
            Map<String, String> params = new HashMap<>();
            params.put(PreferenceKeys.MICROLECTURE_ID.getKey(), microLectureContentDto.getId());
            params.put("chapterId", microLectureContentDto.getChapterId());
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.ml_watch_from_beg_click.getActionName(), params);
            player.play();
            resumeLectureLayoutDialog.dismiss();
//            playHowToVideo();
            handleSettingsTooltip();
        });
    }


    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void qualityClicked(int position) {

        if (position < 0 || position >= videoQualityDtoList.size()) {
            return;
        }

        Log.d(TAG, "Quality change before media item count and index " + player.getMediaItemCount() + " current " + player.getCurrentMediaItemIndex());

        boolean wasPlaying = player.isPlaying();
        player.setPlayWhenReady(false);
        player.pause();

        VideoQualityDto videoQualityDto = videoQualityDtoList.get(position);
        for (int i = 0; i < videoQualityDtoList.size(); i++) {
            videoQualityDtoList.get(i).setSelected(false);
        }
        videoQualityDto.setSelected(true);
        videoQualityDtoList.set(position, videoQualityDto);
        videoQualityAdapter.notifyDataSetChanged();

        long currentPosition = player.getCurrentPosition();
        MediaItem newMediaItem = MediaItem.fromUri(videoQualityDto.getVideoUrl());
        selectedQuality = videoQualityDto.getQuality();

        MyApplication.saveData(getString(R.string.user_selected_video_quality), selectedQuality);
        MyApplication.getUserSelectedVideoQualityDatabaseReference().setValue(selectedQuality);

        currentQuality1.setText(selectedQuality);

        player.replaceMediaItem(player.getCurrentMediaItemIndex(), newMediaItem);
        player.prepare();

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY) {
                    player.removeListener(this);

                    player.seekTo(currentPosition);
                    player.setPlayWhenReady(wasPlaying);


                    controllerInitialized = false;


                    if (player.getDuration() > 0) {
                        customTimeBar.setAdMarkerPositions(thumbDataList, player.getDuration(),
                                microLectureContentDto.getVideoCompletionEndPos());
                    }

                    closeQualityDialog();

                    Map<String, String> params = new HashMap<>();
                    params.put("Quality", videoQualityDto.getQuality());
                    params.put("ChapterId", microLectureContentDto.getChapterId());
                    params.put("ChapterName", microLectureContentDto.getChapterName());
                    params.put("MicrolectureId", microLectureContentDto.getId());
                    params.put("MicrolectureName", microLectureContentDto.getTitle());
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.vp_quality_interested_in.getActionName(), params);

                    Log.d(TAG, "Quality change media item count and index " + player.getMediaItemCount() + " current " + player.getCurrentMediaItemIndex());
                }
            }
        });
    }

    @Override
    public void closeQualityDialog() {
        hideSettingLayout();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void speedChanged(int position) {
        VideoPlaybackSpeedDto speedDto = playbackSpeeds.get(position);

        for (int i = 0; i < playbackSpeeds.size(); i++) {
            playbackSpeeds.get(i).setSelected(false);
        }
        playbackSpeeds.get(position).setSelected(true);
        videoSpeedPlaybackAdapter.notifyDataSetChanged();
        currentSpeed.setText(playbackSpeeds.get(position).getSpeedText());

        String previousSpeed = selectedPlaybackSpeed;
        Map<String, String> nparams = new HashMap<>();
        nparams.put("selectedSpeed", speedDto.getSpeed() + "x");
        nparams.put("previousSpeed", previousSpeed + "x");
        nparams.put("chapterId", microLectureContentDto.getChapterId());
        nparams.put("chapterName", microLectureContentDto.getChapterName());
        nparams.put("microlectureId", microLectureContentDto.getId());
        nparams.put("microlectureName", microLectureContentDto.getTitle());
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.vp_setting_playback_opt_select_click.getActionName(), nparams);
        //save
        selectedPlaybackSpeed = speedDto.getSpeed();

        Log.d(TAG, "speedChanged() called with: position = [" + position + "]   " + speedDto.getSpeed());
        PlaybackParameters params = new PlaybackParameters(Float.parseFloat(speedDto.getSpeed()), 1f);
        player.setPlaybackParameters(params);
        closeSpeedDialog();
    }

    private void set2xPlayerSpeed() {
        longPressSpeedCard.setVisibility(View.VISIBLE);

        PlaybackParameters params = new PlaybackParameters(Float.parseFloat("2"), 1f);

        Map<String, String> nparams = new HashMap<>();
//        nparams.put("pressSide", lastTapWasLeft ? "left" : "right");
        nparams.put("previousSpeed", selectedPlaybackSpeed + "x");
        nparams.put("chapterId", microLectureContentDto.getChapterId());
        nparams.put("chapterName", microLectureContentDto.getChapterName());
        nparams.put("microlectureId", microLectureContentDto.getId());
        nparams.put("microlectureName", microLectureContentDto.getTitle());
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.vp_long_press_start.getActionName(), nparams);
        longPressStartTime = System.currentTimeMillis();

        player.setPlaybackParameters(params);
    }

    private void resetPlaybackSpeed() {
        longPressSpeedCard.setVisibility(View.GONE);

        long duration = (System.currentTimeMillis() - longPressStartTime) / 1000;
        Map<String, String> nparams = new HashMap<>();
        nparams.put("previousSpeed", selectedPlaybackSpeed + "x");
        nparams.put("holdDurationSec", String.valueOf(duration));
        nparams.put("chapterId", microLectureContentDto.getChapterId());
        nparams.put("chapterName", microLectureContentDto.getChapterName());
        nparams.put("microlectureId", microLectureContentDto.getId());
        nparams.put("microlectureName", microLectureContentDto.getTitle());
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.vp_long_press_release.getActionName(), nparams);

        PlaybackParameters params = new PlaybackParameters(Float.parseFloat(selectedPlaybackSpeed), 1f);
        player.setPlaybackParameters(params);
    }


    @Override
    public void closeSpeedDialog() {
        hideSettingLayout();
    }

    @Override
    public void intentClickedFromView(MLResponse mlResponse) {
        addPlayList(mlResponse.getContentUrl(), mlResponse.getPosition(), mlResponse.getPosition() + mlResponse.getLength());
        crouton = Crouton.makeText(VideoPlayerActivity.this, getString(R.string.instant_solution), style);
        crouton.show();
        mlResponses.clear();
        instantDoubtVideoCardView.setVisibility(View.GONE);
        videoCloseView.setVisibility(View.GONE);
        instantDoubtCardView.setVisibility(View.GONE);
    }

    @Override
    public void intentClicked(MLResponse mlResponse, String doubt) {
        disambiguation_dialog.dismiss();
        if (mlResponse.getIntentid().equalsIgnoreCase("fallback")) {
            relativeLayoutEnterSubmitDoubt.show();
            relativeLayoutEnterSubmitDoubt.getWindow().getDecorView().post(() -> {
                hideNavigationBar();
            });
            doubtTextEditText_Enter.setText(doubt);
            doubtTextEditText_Enter.setFocusable(true);
            doubtTextEditText_Enter.requestFocus();
        } else {
            addPlayList(mlResponse.getContentUrl(), mlResponse.getPosition(), mlResponse.getPosition() + mlResponse.getLength());
            crouton = Crouton.makeText(VideoPlayerActivity.this, getString(R.string.instant_solution), style);
            crouton.show();
        }
    }


    @Override
    public void interactiveApiSuccessCallback(List<MicroLectureQuestionsAnsDto> questionsAnsList) {
        thumbDataList.clear();
        this.questionsAnsList.clear();
        this.questionsAnsList = questionsAnsList;
        updatePlayerForInteraction(questionsAnsList);
    }

    private void updatePlayerForInteraction(List<MicroLectureQuestionsAnsDto> questionsAnsList) {
        for (MicroLectureQuestionsAnsDto questionsAnsDto : questionsAnsList) {
            TimeBarThumbData thumbData = TimeBarThumbData.builder().completed(false)
                    .position(questionsAnsDto.getStartPosition() + 600).questionId(questionsAnsDto.getId())
                    .build();
            thumbDataList.add(thumbData);

//            long triggerPosition =
//                    questionsAnsDto.getStartPosition()
//                            + 600
//                            + customTimeBar.getScrubberTimeOffset();

            player.createMessage(
                            (messageType, payload) -> {

                                // Guard: skip if wrong-answer video is already playing or another
                                // question is already being handled. Without this guard, seeking
                                // backward to play a wrong-answer explanation can re-trigger
                                // all question markers that lie in the playback path, causing
                                // every wrong-answer video in the video to chain-fire.
                                if (disableControllerAction || interactiveQuestionActive) {
                                    Log.d(TAG, "Interactive message skipped: disableControllerAction="
                                            + disableControllerAction
                                            + " interactiveQuestionActive=" + interactiveQuestionActive);
                                    return;
                                }

                                currentInteractiveQuestion = (MicroLectureQuestionsAnsDto) payload;
                                player.pause();
                                askButtonViewModel.changeState(AskButtonState.STATE_LISTENING);
                                interactiveQuestionActive = true;
                                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                                    Log.d("micIssue", "updatePlayerForInteraction: ");
                                    handlePermission();
                                } else if (speechRecognizer != null) {
                                    speechRecognizerErrorCount = 1;
                                    setLayoutAndChildrenEnabled(exoplayerControlLayout, false);
//                                    Log.d("VideoInteractivityTesting", "Player Interactive position matched: "+currentInteractiveQuestion.toString());
                                    pausePlayerAndStartSpeechRecognizer();
                                }
                            })
                    .setLooper(Looper.getMainLooper())
                    .setPosition(0, questionsAnsDto.getStartPosition() + 600)
                    .setPayload(questionsAnsDto)
                    .setDeleteAfterDelivery(false)
                    .send();
        }
        if (player.getDuration() > 0) {
            customTimeBar.setAdMarkerPositions(thumbDataList, player.getDuration(), microLectureContentDto.getVideoCompletionEndPos());
        }
    }

    @Override
    public void validateAPIResponse(long seekToPosition, Integer endPosition, Integer fallbackPosition,
                                    boolean correct, String userAnswer, String microLectureQId, boolean speech, String clickAnswer) {
        Log.d(TAG, "validateAPIResponse --> " + seekToPosition + ", " + endPosition + ", " + fallbackPosition + ", " + correct + ", " + userAnswer + ", " + microLectureQId + ", " + speech + ", " + clickAnswer);
        for (TimeBarThumbData thumbData : thumbDataList) {
            thumbData.setActive(false);
            if (thumbData.getQuestionId().equals(currentInteractiveQuestion.getId())) {
                thumbData.setCompleted(true);
                thumbData.setCorrect(correct);
            }
        }
        customTimeBar.setAdMarkerPositions(thumbDataList, player.getDuration(), 0);
        interactiveQuestionActive = false;
        Map<String, String> params = new HashMap<>();
        params.put("userAnswer", userAnswer);
        params.put("QuestionId", microLectureQId);
        params.put("answerType", speech ? "speech" : "clicked");
        params.put("clickAnswer", clickAnswer);
        params.put("ChapterId", microLectureContentDto.getChapterId());
        params.put("ChapterName", microLectureContentDto.getChapterName());
        params.put("MicrolectureId", microLectureContentDto.getId());
        params.put("MicrolectureName", microLectureContentDto.getTitle());
        params.put("SubjectName", getIntent().getStringExtra("selectedSubject"));
        if (correct) {
            params.put("correct", "true");
            player.seekTo(seekToPosition);
            player.play();
            playCelebrationAnimation();
        } else {
            exoplayerDurationLayout.setVisibility(View.GONE);
            params.put("correct", "false");
            askButton.setVisibility(View.GONE);
            disableControllerAction = true;
            customTimeBar.setVisibility(View.GONE);
            this.endPosition = endPosition != null ? endPosition : (int) seekToPosition;
            this.fallbackPosition = fallbackPosition != null ? fallbackPosition : (int) seekToPosition;
            player.seekTo(seekToPosition);
            player.play();
        }
        setLayoutAndChildrenEnabled(exoplayerControlLayout, true);
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.user_interactivity_response.getActionName(), params);
    }


    @Override
    public void changeButtonState(AskButtonState askButtonState) {
        askButtonViewModel.changeState(askButtonState);
    }

    @Override
    protected void askDoubtContextSuccessCallback(DoubtContextDto doubtContextDto, String userQuestion) {
        // no-op: contextualizedDoubtApi no longer makes an API call
    }

    @Override
    protected void askDoubtContextFailerCallback() {
        // no-op: contextualizedDoubtApi no longer makes an API call
    }

    public SpannableStringBuilder convertToRichText(String text) {
        String[] list = text.split("<b>");
        SpannableStringBuilder finalResponse = new SpannableStringBuilder();
        int startingIndex = 0;
        int endingIndex = 0;
        for (int i = 0; i < list.length; i++) {
            if (list[i].contains("(bold)")) {
                String add = list[i].replace("(bold)", "");
                finalResponse.append(add);
                endingIndex = finalResponse.length();
                finalResponse.setSpan(new StyleSpan(Typeface.BOLD), startingIndex, endingIndex, 0);
            } else {
                finalResponse.append(list[i]);
                startingIndex = finalResponse.length();
            }
        }

        return finalResponse;
    }

    @Override
    public void startVideo() {
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    public void stopVideo() {
        if (player != null) {
            player.pause();
        }
    }

    @Override
    public void hideControlView() {
        videoFullScreenPlayer.setUseController(false);
    }

    public void showControlView() {
        customTimeBar.setVisibility(View.VISIBLE);
        videoFullScreenPlayer.setUseController(true);
    }

    private void hideNavigationBar() {
        if (getWindow() == null) {
            return;
        }
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideNavigationBar();
        }
    }

    @Override
    public void onToolTipClick() {
    }

    @Override
    public void onToolTipNextClick() {
    }

    @Override
    public void onToolTipSkipClick() {
    }

    @Override
    public void onOptionDialogShow() {
        //edge case if name dialog is showing
        checkNameEnterDialogDismissed();
    }


    //edge case if name dialog is showing
    void checkNameEnterDialogDismissed(){
        if (nameEnterDialog != null && nameEnterDialog.isShowing()) {
            nameEnterDialog.dismiss();
        }
    }

}
