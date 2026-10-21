package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.activity.HomeScreen.BroadcastStringForAction;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.MICRO_LECTURE_PRACTICE_LIST_BY_TYPE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.POST_MICROLECTURE_ANSWER;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.TEST_REPORT_QUESTION;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.media3.common.util.UnstableApi;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.web_view.MicrolecturePractiseWebViewActivity;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.constants.QuestionLevel;
import arivihan.technologies.doubtbuzzter2.constants.QuestionType;
import arivihan.technologies.doubtbuzzter2.customView.HowToVideoView;
import arivihan.technologies.doubtbuzzter2.enums.HOW_TO_TYPE;
import arivihan.technologies.doubtbuzzter2.fragments.MatchTheseTypeFragment;
import arivihan.technologies.doubtbuzzter2.fragments.SubjectiveTypeFragment;
import arivihan.technologies.doubtbuzzter2.fragments.ValueTypeFragment;
import arivihan.technologies.doubtbuzzter2.fragments.VeryShortFragment;
import arivihan.technologies.doubtbuzzter2.helper.TooltipHelper;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.listener.SubmitListener;
import arivihan.technologies.doubtbuzzter2.model.FactoryShipQnADto;
import arivihan.technologies.doubtbuzzter2.model.LectureContentType;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureContentDto;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureDto;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureEntity;
import arivihan.technologies.doubtbuzzter2.model.PracticeStatus;
import arivihan.technologies.doubtbuzzter2.model.Questions;
import arivihan.technologies.doubtbuzzter2.model.UserMicrolectureAnswer;
import arivihan.technologies.doubtbuzzter2.model.VideoDetails;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class PracticeProblemsActivity extends BaseActivity implements SubmitListener, HowToVideoView.RemoveListener {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ViewPagerAdapter adapter;
    ArrayList<FactoryShipQnADto> qnADtoList;
    int selectedTab = 0;
    LottieAnimationView animationView;
    String microLectureId;
    String microLectureName;
    String chapterId;
    Gson gson;
    LinearLayout backButtonLayout;
    TextView title;
    boolean reattempt;
    boolean review;
    boolean back;
    Dialog actionDialog;
    Dialog messageDialog;
    Dialog matchTheseMessageDialog;
    int firstUnattemptedIndex;
    Handler messageHandler;
    Runnable messageRunnable;
    TextView message;
    LottieAnimationView messageAnimation;
    boolean moreThanSevenSeconds;
    MediaPlayer mediaPlayer;
    String reportText;
    Dialog reportDialog;
    RelativeLayout progress;
    private Dialog matchTheseDialog;
    public ImageView longShortQuestionInfoBtn;

    int attemptedQuestions;
    int totalQuestions;

    int practicePosInCurrentMicroLecture;
    ConstraintLayout continuity_layout;
    ImageView closeContinuityLayout;
    LinearLayout progressionLayout;
    TextView progressionText;
    ConstraintLayout continuityPlayLayout;
    CircularProgressIndicator circularProgressIndicator;
    LinearLayout skipToNextLayout;
    TextView skipToNextText;
    ArrayList<MicroLectureDto> list = new ArrayList<>();
    MicroLectureDto progressionDto;
    MicroLectureDto skipDto;
    int currentMicroLecturePos;
    int calculateMicroLecturePos;
    int calculatePracticePos;
    int progressionMicroLecturePos;
    int skipMicroLecturePos;
    int practicePosInProgression;
    int practicePosInSkip;
    boolean calculateStatus;
    boolean skipToPractice;
    boolean progressionIsPractice;
    private Handler handler = new Handler();
    private int circularProgress = 0;
    private final int totalDuration = 5000;  // 5 seconds
    private final int stepDuration = 50;
    int start;
    private final String TAG = "PracticeProblemsDebug";
    private CardView how_to_card;
    private HowToVideoView howToVideoView;
    private View how_to_video_view;
    private Handler howToHandler;
    private Runnable howToRunnable;
    private boolean show = true;
    private boolean showMatch = true;
    NetworkStatusLiveData networkStatusLiveData;
    private String subjectCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice_problems);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d(TAG, "onBackPressed: ");
                if (howToVideoView != null && howToVideoView.videoView != null
                        && howToVideoView.videoView.getVisibility() == View.VISIBLE && howToVideoView.expanded) {
                    howToVideoView.resize();
                } else if (attemptedQuestions != totalQuestions && !review && !back) {
                    mediaPlayer = MediaPlayer.create(PracticeProblemsActivity.this, R.raw.wronganswer);
                    if (mediaPlayer != null) {
                        mediaPlayer.start();
                    }
                    if (actionDialog != null && !actionDialog.isShowing()) {
                        actionDialog.show();
                    }
                } else {
                    destroyHowTo();
                    if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                        getSupportFragmentManager().popBackStack();
                    }
                    finish();
                }
            }
        });

        MyApplication.getInstance().getTooltipViewModel().updateTooltipViewModel();
        updateFragmentForTooltip();
        MyApplication.saveData(PreferenceKeys.PROGRESS_RELOAD.getKey(), true);
        setUpViews();
        secureScreen();
        setUpActionPopup();
        setUpMessagePopup();
        setUpReportDialog();
        setUpViewPager();
        setMatchTheseDialog();
        gson = new Gson();

    }

    private void setUpViews() {

        firstUnattemptedIndex = getIntent().getIntExtra("firstUnattemptedIndex", 0);
        selectedTab = firstUnattemptedIndex;
        reattempt = getIntent().getBooleanExtra("reattempt", false);
        review = getIntent().getBooleanExtra("review", false);
        microLectureId = getIntent().getStringExtra("microLectureId");
        microLectureName = getIntent().getStringExtra("microLectureName");
        chapterId = getIntent().getStringExtra("chapterId");
        title = findViewById(R.id.title);
        how_to_video_view = findViewById(R.id.how_to_video_view);
        how_to_card = findViewById(R.id.how_to_card);
        if (MyApplication.getFirebaseRemoteConfig().getBoolean("hide_how_to_card_practice") ||
                MyApplication.getFirebaseRemoteConfig().getBoolean("hideHowToVideos"))
            how_to_card.setVisibility(View.GONE);
        how_to_card.setOnClickListener(view -> {
            openExpandedVideo();
        });
        title.setText(microLectureName);
        backButtonLayout = findViewById(R.id.back_button_layout);
        backButtonLayout.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        tabLayout = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.viewpager);
        longShortQuestionInfoBtn = findViewById(R.id.long_short_question_info_btn);
        qnADtoList = getIntent().getParcelableArrayListExtra("factoryShipQnADtoList");
        subjectCode=getIntent().getStringExtra("subjectCode");
        Log.d(TAG, "setUpViews-subjectCode: "+subjectCode);
        if (!CollectionUtils.isEmpty(qnADtoList))
            totalQuestions = qnADtoList.size();

        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            if(isOnline){
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
                    CommonMethods.showPositiveSnackbar(backButtonLayout, getString(R.string.InternetConnection));
                }
            }
            else{
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                    CommonMethods.showNegativeSnackbar(backButtonLayout, getString(R.string.NoInternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), false);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), true);
                }
            }
        });

    }

    private void setUpContinuityLayout() {
        list = MyApplication.getMicroLectureList(getString(R.string.micro_lecture_and_practice_list));
        currentMicroLecturePos = getIntent().getIntExtra("currentMicroLecturePos", 0);
        practicePosInCurrentMicroLecture = getIntent().getIntExtra("practicePosInCurrentMicroLecture", 0);
        continuity_layout = findViewById(R.id.continuity_layout);
        closeContinuityLayout = findViewById(R.id.close_continuity_layout);
        progressionLayout = findViewById(R.id.progression_layout);
        progressionText = findViewById(R.id.progression_text);
        continuityPlayLayout = findViewById(R.id.play_layout);
        circularProgressIndicator = findViewById(R.id.continuity_timer);
        skipToNextLayout = findViewById(R.id.skip_to_next_layout);
        skipToNextText = findViewById(R.id.skip_to_next_text);
        closeContinuityLayout.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        calculateMicroLecturePos = currentMicroLecturePos;
        calculatePracticePos = practicePosInCurrentMicroLecture;
        progressionDto = practiceProgression();

        if (progressionDto == null) {
            finish();
            return;
        } else {
            continuity_layout.setVisibility(View.VISIBLE);
            startProgress();
        }

        progressionMicroLecturePos = calculateMicroLecturePos;
        progressionIsPractice = calculateStatus;

        if (progressionDto != null) {
            if (progressionIsPractice) {
                practicePosInProgression = calculatePracticePos;
                progressionText.setText(LectureContentType.getLectureContentType(progressionDto.getQuestions().get(practicePosInProgression).getType()).getType());
            } else {
                progressionText.setText(progressionDto.getTitle());
            }
        }

        Log.d("progressionMicroLectur", String.valueOf(calculateMicroLecturePos));
        Log.d("practicePosInProgre", String.valueOf(practicePosInProgression));
        Log.d("progressionIsPractice", String.valueOf(progressionIsPractice));


        if (progressionIsPractice) {

            Log.d(TAG, "videoProgression starts 1");
            skipDto = videoProgression();
            Log.d(TAG, "videoProgression starts 2");

            skipToPractice = calculateStatus;
            skipMicroLecturePos = calculateMicroLecturePos;
        }
        else {
            Log.d(TAG, "videoProgression while starts");
            Log.d(TAG, "skipToPractice " + skipToPractice);
            Log.d(TAG, "skipMicroLecturePos " + skipMicroLecturePos);
            Log.d(TAG, "list " + list.size());


            while (!skipToPractice && skipMicroLecturePos < list.size()) {
                calculatePracticePos = -1;
                skipDto = practiceProgression();
                skipToPractice = calculateStatus;
                skipMicroLecturePos = calculateMicroLecturePos;
                if (skipDto == null) {
                    break;
                }
                Log.d(TAG, "skipDto " + skipDto);
                Log.d(TAG, "skipToPractice " + skipToPractice);
                Log.d(TAG, "skipMicroLecturePos " + calculateMicroLecturePos);
            }
        }

        if (skipDto != null && skipDto.getQuestions() != null && practicePosInSkip < skipDto.getQuestions().size()) {
            if (skipToPractice) {
                practicePosInSkip = calculatePracticePos;
                skipToNextText.setText(String.format(getString(R.string.Skip_to_Next_Practice), LectureContentType.getLectureContentType(skipDto.getQuestions().get(practicePosInSkip).getType()).getType()));
            } else {
                skipToNextText.setText(String.format(getString(R.string.Skip_to_Next_Lecture)));
            }
        }
        else {
            skipToNextLayout.setVisibility(View.GONE);
        }

        Log.d("skipMicroLecturePos", String.valueOf(skipMicroLecturePos));
        Log.d("practicePosInSkip", String.valueOf(practicePosInSkip));
        Log.d("skipIsPractice", String.valueOf(calculateStatus));

        continuityPlayLayout.setOnClickListener(v->{
            if(progressionDto!=null) {
                handler.removeCallbacksAndMessages(null);
                progressionLayout.performClick();
            }
        });

        progressionLayout.setOnClickListener(view -> {
            if (progressionDto == null || progressionDto.isLocked()) {
                MyApplication.saveData(getString(R.string.progressionIsLocked), true);
                MyApplication.saveData(getString(R.string.progressionLockedMessage), progressionDto.getLockMessage());
                getOnBackPressedDispatcher().onBackPressed();
            } else {
                if (progressionIsPractice) {
                    getQuestionList(progressionDto.getCode(), progressionDto.getQuestions().get(practicePosInProgression).getType(), progressionMicroLecturePos, practicePosInProgression);
                } else {
                    startVideo(progressionDto, progressionMicroLecturePos);
                }
            }
        });
        skipToNextLayout.setOnClickListener(view -> {
            if (skipDto.isLocked()) {
                MyApplication.saveData(getString(R.string.progressionIsLocked), true);
                MyApplication.saveData(getString(R.string.progressionLockedMessage), skipDto.getLockMessage());
                getOnBackPressedDispatcher().onBackPressed();
            } else {
                if (skipToPractice) {
                    getQuestionList(skipDto.getCode(), skipDto.getQuestions().get(practicePosInSkip).getType(), skipMicroLecturePos, practicePosInSkip);
                } else {
                    startVideo(skipDto, skipMicroLecturePos);
                }
            }
        });
    }


    private MicroLectureDto practiceProgression() {
        if (calculateMicroLecturePos < list.size() && list.get(calculateMicroLecturePos)!=null && list.get(calculateMicroLecturePos).getQuestions().size() > calculatePracticePos) {
            for (int i = calculatePracticePos + 1; i < list.get(calculateMicroLecturePos).getQuestions().size(); i++) {
                Questions questions = list.get(calculateMicroLecturePos).getQuestions().get(i);
                if (questions.getStatus().equalsIgnoreCase(PracticeStatus.PARTIAL_COMPLETED.name()) ||
                        questions.getStatus().equalsIgnoreCase(PracticeStatus.TO_DO.name())) {
                    calculateStatus = true;
                    calculatePracticePos = i;
                    return list.get(calculateMicroLecturePos);
                }
            }
            if (!calculateStatus)
                return videoProgression();
        }
        return videoProgression();
    }

    private MicroLectureDto videoProgression() {
        if (list.size() > calculateMicroLecturePos) {
            for (int i = calculateMicroLecturePos + 1; i < list.size(); i++) {
                calculateMicroLecturePos = i;
                if (!list.get(calculateMicroLecturePos).isCompleted()) {
                    if (list.get(calculateMicroLecturePos).getVideoDetails().getProgress() == 100) {
                        calculatePracticePos = 0;
                        return practiceProgression();
                    } else {
                        calculateStatus = false;
                    }
                    return list.get(calculateMicroLecturePos);
                }
            }
        }
        return null;
    }

    private void getQuestionList(String microLectureCode, String type, int pos, int practicePos) {
        Log.d(TAG, "getQuestionList() called | microLectureCode=" + microLectureCode
                + ", type=" + type + ", pos=" + pos + ", practicePos=" + practicePos);
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("microLectureCode", microLectureCode);
            map.put("type", type);
            map.put("boldFormatting", true);
            Log.d(TAG, "getQuestionList: building request with params=" + map);

            MyJsonArrayRequest myJsonArrayRequest = new MyJsonArrayRequest(Request.Method.GET,
                    CommonMethods.getUrlParam(MICRO_LECTURE_PRACTICE_LIST_BY_TYPE, map), null,
                    response -> {
                        Log.d(TAG, "getQuestionList: response received | length=" + response.length());
                        TypeToken<List<FactoryShipQnADto>> token = new TypeToken<List<FactoryShipQnADto>>() {
                        };
                        qnADtoList.clear();
                        qnADtoList.addAll(gson.fromJson(response.toString(), token.getType()));
                        Log.d(TAG, "getQuestionList: parsed " + qnADtoList.size() + " items | calling setUpQuestion()");
                        setUpQuestion(pos, practicePos, type);
                    }, new VolleyGlobalErrorListener(this) {
                @Override
                public void postError(VolleyError error) {
                    Log.e(TAG, "getQuestionList: postError | " + error.getMessage(), error);
                }
            });

            myJsonArrayRequest.setTag(TAG);
            Log.d(TAG, "getQuestionList: adding request to queue | tag=" + TAG);
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonArrayRequest);

        } catch (Exception e) {
            Log.e(TAG, "getQuestionList: exception caught | " + e.getMessage(), e);
        }
    }

    private void setUpQuestion(int pos, int practicePos,String type) {
        if (!qnADtoList.isEmpty()) {
            firstUnattemptedIndex = 0;
            for (int i = 0; i < qnADtoList.size(); i++) {
                if (!qnADtoList.get(i).isCompleted()) {
                    firstUnattemptedIndex = i;
                    break;
                }
            }
            openPracticeQuestions(pos, practicePos,type);
        }
    }

    private void openPracticeQuestions(int pos, int practicePos,String practiceType) {
        Log.d("PracticeLaunch", "Opening PracticeProblemsActivity...");
        Log.d("PracticeLaunch", "MicroLecture Position: " + pos);
        Log.d("PracticeLaunch", "Practice Position: " + practicePos);

        String microLectureId = list.get(progressionMicroLecturePos).getId();
        Log.d("PracticeLaunch", "MicroLecture ID: " + microLectureId);
        String microLectureTitle = list.get(progressionMicroLecturePos).getTitle();
        Log.d("PracticeLaunch", "MicroLecture Title: " + microLectureTitle);

        Log.d("PracticeLaunch", "Practice Type: " + practiceType);

        updateSelectionLocally(false, true, microLectureId, practiceType);

        LectureContentType type = LectureContentType.valueOf(practiceType);
        if(MyApplication.isCompetitiveProfile() && MicrolectureListActivity.isAllowedLectureContentType(type)) {
            openWebViewPracticeQuestions(type, pos, practicePos);
            return;
        }

        Intent intent = new Intent(this, PracticeProblemsActivity.class);
        intent.putParcelableArrayListExtra("factoryShipQnADtoList", qnADtoList);
        intent.putExtra("microLectureId", microLectureId);
        intent.putExtra("microLectureName", microLectureTitle);
        intent.putExtra("chapterId", chapterId);
        intent.putExtra("review", review);
        intent.putExtra("reattempt", reattempt);
        intent.putExtra("firstUnattemptedIndex", firstUnattemptedIndex);
        intent.putExtra("currentMicroLecturePos", pos);
        intent.putExtra("practicePosInCurrentMicroLecture", practicePos);

         subjectCode = getIntent().getStringExtra("subjectCode");
        Log.d("PracticeLaunch", "Subject Code: " + subjectCode);
        intent.putExtra("subjectCode", subjectCode);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        destroyHowTo();
        Log.d("PracticeLaunch", "Starting PracticeProblemsActivity...");
        startActivity(intent);
    }

    private void openWebViewPracticeQuestions(LectureContentType lectureContentType, int pos, int practisePos) {
        Intent intent = new Intent(PracticeProblemsActivity.this, MicrolecturePractiseWebViewActivity.class);
        intent.putParcelableArrayListExtra("factoryShipQnADtoList", qnADtoList);
        intent.putExtra("microLectureId", microLectureId);
        intent.putExtra("microLectureName", microLectureName);
        intent.putExtra("chapterId", chapterId);
        intent.putExtra("review", review);
        intent.putExtra("reattempt", reattempt);
        intent.putExtra("firstUnattemptedIndex", firstUnattemptedIndex);
        intent.putExtra("currentMicroLecturePos", pos);
        intent.putExtra("practicePosInCurrentMicroLecture", practisePos);
        String subjectCode = getIntent().getStringExtra("subjectCode");
        intent.putExtra("subjectCode", subjectCode);
        intent.putExtra("selectedSubject", "");
        intent.putExtra("isBoard", false);
        intent.putExtra("type", lectureContentType!=null?lectureContentType.name():"");
        destroyHowTo();
        startActivity(intent);
    }

    private void destroyHowTo() {
        if (howToHandler != null)
            howToHandler.removeCallbacksAndMessages(null);
        how_to_video_view.setVisibility(View.GONE);
        if (howToVideoView != null)
            howToVideoView.remove();
    }

    public void startVideo(MicroLectureDto microLectureDto, int pos) {
        if (microLectureDto.isLocked()) {
            getOnBackPressedDispatcher().onBackPressed();
        } else {
            updateSelectionLocally(true, false, microLectureDto.getId(),LectureContentType.VIDEO.name());
            VideoDetails videoDetails = microLectureDto.getVideoDetails();
            playVideo(videoDetails, microLectureDto, getIntent().getBooleanExtra("isBoard", false), pos);
        }
    }

    private void updateSelectionLocally(boolean isLecture, boolean isPractice, String mlId, String lectureContentType) {
        if (StringUtils.isNotEmpty(chapterId)) {
            MicroLectureEntity selectionMicroLectureEntity = new MicroLectureEntity(chapterId, mlId,
                    MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()),lectureContentType,isLecture,isPractice);
            MyApplication.getInstance().getMicroLectureViewModel().insert(selectionMicroLectureEntity);

            MyApplication.saveData(PreferenceKeys.MICROLECTURE_DATA_RELOAD.getKey(), true);
        }
    }

    private void playVideo(VideoDetails videoDetails, MicroLectureDto microLectureDto, boolean board, int pos) {

        @OptIn(markerClass = UnstableApi.class) Intent intent = new Intent(this, VideoPlayerActivity.class);
        MicroLectureContentDto microLectureContentDto = new MicroLectureContentDto();
        microLectureContentDto.setDefaultStartTime(Math.toIntExact(videoDetails.getStartPos()));
        microLectureContentDto.setVideoCompletionEndPos(Math.toIntExact(videoDetails.getEndPos()));
        microLectureContentDto.setVideoCompletionStartPos(Math.toIntExact(videoDetails.getEndPos()));
        microLectureContentDto.setContentUrl(videoDetails.getUrl());
        microLectureContentDto.setVideoUrl(videoDetails.getUrl());
        microLectureContentDto.setId(microLectureDto.getCode());
        microLectureContentDto.setChapterId(chapterId);
        microLectureContentDto.setResumePosition(videoDetails.getResumePosition());
        microLectureContentDto.setLectureContentType(LectureContentType.VIDEO);
        microLectureContentDto.setVideoQualityDtos(videoDetails.getQualityDtos());
        microLectureContentDto.setLanguageAndVideoUrlDtos(videoDetails.getLanguageAndVideoUrlDtos());
        microLectureContentDto.setLikeCount(microLectureDto.getLikeCount());
        microLectureContentDto.setLiked(microLectureDto.getLiked());

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
        intent.putExtra("subjectCode", getIntent().getStringExtra("subjectCode"));
        intent.putExtra("selectedSubject", getIntent().getStringExtra("selectedSubject"));
        intent.putExtra("selected_chapter_id", microLectureDto.getCode());
        intent.putExtra("replay", microLectureDto.getVideoDetails().getProgress() == 100);
        intent.putExtra("currentMicroLecturePos", pos);
        MyApplication.saveData(PreferenceKeys.MICROLECTURE_DATA_RELOAD.getKey(), true);
        startActivity(intent);
        destroyHowTo();
        finish();
    }

    private void setUpViewPager() {
        Log.d(TAG, "setUpViewPager--subjectCode: "+subjectCode);
        adapter = new ViewPagerAdapter(this, qnADtoList, reattempt, review, this);
        viewPager.setAdapter(adapter);
        for (int i = 0; i < adapter.getItemCount(); i++) {
            setupTab(i);
        }
        viewPager.setOffscreenPageLimit(3);
        new Handler(Looper.getMainLooper()).postDelayed(() -> viewPager.setCurrentItem(firstUnattemptedIndex, true), 300);
        if (reattempt || !review)
            setUpPreHandler();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTab = tab.getPosition();
                BoardQuestionActivity.isMatchTheseSubmitted = false;
                viewPager.setCurrentItem(selectedTab);
                setUpPreHandler();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                selectedTab = position;
                startHowToHandler();
                updateFragmentForTooltip();
                BoardQuestionActivity.isMatchTheseSubmitted = false;
                tabLayout.selectTab(tabLayout.getTabAt(position));
                applyCustomFont(position);
                setUpPreHandler();
            }
        });
    }

    @Override
    public void onBottomSheetOpen() {
        if (howToVideoView != null)
            howToVideoView.pausePlayer();
    }

    @Override
    public void onBottomSheetClose() {
        if (howToVideoView != null)
            howToVideoView.resumePlayer();
    }

    private void startHowToHandler() {
        if (howToHandler != null) {
            howToHandler.removeCallbacksAndMessages(null);
        }

        FactoryShipQnADto factoryQnADto = qnADtoList.get(selectedTab);
        LectureContentType lectureType = LectureContentType.valueOf(factoryQnADto.getType());

        if (!MyApplication.getFirebaseRemoteConfig().getBoolean("hideHowToVideos") &&
                (howToVideoView == null || howToVideoView.videoView == null || howToVideoView.player == null)) {

            initHowToView();

            boolean isSAVType = isSAVContentType(lectureType);

            if (review || factoryQnADto.isReview()) {
                if (isSAVType && show) {
                    initializeHowToVideoView(HOW_TO_TYPE.QUESTION_TYPE_SAV_PRACTICE_ANALYSIS, R.string.Analyse_Now, "_ANALYSIS_COUNT");
                    show = false;
                } else if (lectureType == LectureContentType.MATCH_THE_COLUMN && showMatch) {
                    initializeHowToVideoView(HOW_TO_TYPE.QUESTION_TYPE_MATCH_PRACTICE_ANALYSIS, R.string.Analyse_Now, "_ANALYSIS_COUNT");
                    showMatch = false;
                }
            } else {
                if (isSAVType && show) {
                    initializeHowToVideoView(HOW_TO_TYPE.QUESTION_TYPE_SAV_PRACTICE_ATTEMPT, R.string.start_now, "_COUNT");
                    show = false;
                } else if (lectureType == LectureContentType.MATCH_THE_COLUMN && showMatch) {
                    initializeHowToVideoView(HOW_TO_TYPE.QUESTION_TYPE_MATCH_PRACTICE_ATTEMPT, R.string.start_now, "_COUNT");
                    showMatch = false;
                }
            }
        }
    }

    private void initHowToView() {
        howToVideoView = HowToVideoView.getInstance(this);
        howToVideoView.setRemoveListener(this);
        howToVideoView.setView(this, how_to_video_view);
    }

    private boolean isSAVContentType(LectureContentType type) {
        return type == LectureContentType.LONG_ANSWERS ||
                type == LectureContentType.SHORT_ANSWERS ||
                type == LectureContentType.VERY_SHORT_ANSWERS;
    }

    private boolean isVideoAvailable(HOW_TO_TYPE howToType) {
        return StringUtils.isNotEmpty(howToVideoView.getUrlBasedOnType(howToType));
    }

    private void initializeHowToVideoView(HOW_TO_TYPE type, int buttonTextResId, String countSuffix) {
        String key = type.name() + countSuffix + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        int count = MyApplication.getIntData(key);
        Log.d("debugcount", "getHowToVideoType: " + count + " type: " + type.name());
        if (count <= 0 && isVideoAvailable(type)) {
            MyApplication.saveData(PreferenceKeys.HIDE_TOOLTIP_FOR_HOW_TO.getKey(), true);
            howToHandler = new Handler();
            howToRunnable = () -> {
                howToVideoView.setType(type, false);
                howToVideoView.setButtonText(getString(buttonTextResId));
                MyApplication.saveData(key, 1);
            };
            howToHandler.postDelayed(howToRunnable, 2000);
        }
    }


    private void logFirebaseEvent(String contentType, String mode) {
        Map<String, String> params = new HashMap<>();
        params.put("action", "practice_how_to_card_click");
        params.put("contentType", contentType);
        params.put("mode", mode);
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.practice_how_to_card_click.getActionName(), params);
    }

    private void openExpandedVideo() {
        MyApplication.saveData(PreferenceKeys.HIDE_TOOLTIP_FOR_HOW_TO.getKey(), true);
        TooltipHelper.getInstance().onHowToRemove();
        FactoryShipQnADto factoryShipQnADto = qnADtoList.get(selectedTab);
        LectureContentType lectureContentType = LectureContentType.valueOf(factoryShipQnADto.getType());
        howToVideoView = HowToVideoView.getInstance(this);
        howToVideoView.setRemoveListener(this);
        howToVideoView.setView(this, how_to_video_view);
        boolean b = lectureContentType == LectureContentType.LONG_ANSWERS ||
                lectureContentType == LectureContentType.SHORT_ANSWERS ||
                lectureContentType == LectureContentType.VERY_SHORT_ANSWERS;
        if (review || factoryShipQnADto.isReview()) {
            logFirebaseEvent(lectureContentType.name(), "analysis");
            if (b) {
                howToVideoView.setType(HOW_TO_TYPE.QUESTION_TYPE_SAV_PRACTICE_ANALYSIS, true);
            } else if (lectureContentType == LectureContentType.MATCH_THE_COLUMN) {
                howToVideoView.setType(HOW_TO_TYPE.QUESTION_TYPE_MATCH_PRACTICE_ANALYSIS, true);
            } else {
                howToVideoView.setType(HOW_TO_TYPE.QUESTION_TYPE_A_ANALYSIS_PRACTICE, true);
            }
            howToVideoView.setButtonText(getString(R.string.Analyse_Now));
        } else {
            logFirebaseEvent(lectureContentType.name(), "attempt");
            if (b) {
                howToVideoView.setType(HOW_TO_TYPE.QUESTION_TYPE_SAV_PRACTICE_ATTEMPT, true);
            } else if (lectureContentType == LectureContentType.MATCH_THE_COLUMN) {
                howToVideoView.setType(HOW_TO_TYPE.QUESTION_TYPE_MATCH_PRACTICE_ATTEMPT, true);
            } else {
                howToVideoView.setType(HOW_TO_TYPE.QUESTION_TYPE_A_ATTEMPT_PRACTICE, true);
            }
            howToVideoView.setButtonText(getString(R.string.start_now));
        }
    }

    @Override
    public void onRemove() {
        MyApplication.saveData(PreferenceKeys.HIDE_TOOLTIP_FOR_HOW_TO.getKey(), false);
        TooltipHelper.getInstance().onHowToRemove();
    }

    private void updateFragmentForTooltip() {
        TooltipHelper.getInstance().dismiss();
        MyApplication.getInstance().getTooltipViewModel().updateFragment(selectedTab, this);
    }

    private void applyCustomFont(int position) {
        try {
            Typeface customFontSelected = ResourcesCompat.getFont(this, R.font.urbanist_bold);
            TextView tabNumber = tabLayout.getTabAt(position).getCustomView().findViewById(R.id.tab_text);
            tabNumber.setTypeface(customFontSelected);
            Typeface customFontUnselected = ResourcesCompat.getFont(this, R.font.urbanist_medium);
            for (int i = 0; i < qnADtoList.size(); i++) {
                if (i != position) {
                    TextView tabNumberUnselected = tabLayout.getTabAt(i).getCustomView().findViewById(R.id.tab_text);
                    tabNumberUnselected.setTypeface(customFontUnselected);
                }
            }
        } catch (Exception e) {

        }
    }
    private void setupTab(int pos) {
        TabLayout.Tab tab = tabLayout.newTab();
        View customTab = LayoutInflater.from(this).inflate(R.layout.custom_tab_view, null);
        TextView tabNumber = customTab.findViewById(R.id.tab_text);
        tabNumber.setText(String.valueOf(pos + 1));
        CardView cardView = customTab.findViewById(R.id.tab_card);
        FactoryShipQnADto factoryShipQnADto = qnADtoList.get(pos);
        LectureContentType lectureContentType = LectureContentType.valueOf(factoryShipQnADto.getType());
        switch (lectureContentType) {
            case LONG_ANSWERS:
            case SHORT_ANSWERS:
                //longShortQuestionInfoBtn.setVisibility(View.VISIBLE);
                if ((review || !reattempt) && StringUtils.isNotEmpty(factoryShipQnADto.getUserAnswer())) {
                    qnADtoList.get(pos).setReview(true);
                    if (factoryShipQnADto.isCompleted())
                        attemptedQuestions++;
                    double percent = 0;
                    try {
                        percent = ((Double.parseDouble(factoryShipQnADto.getMarksObtained())) * 100) / Double.parseDouble(factoryShipQnADto.getMarks());
                        if (percent > 50)
                            cardView.setCardBackgroundColor(getResources().getColor(R.color.right_tab));
                        else
                            cardView.setCardBackgroundColor(getResources().getColor(R.color.wrong_tab));
                    } catch (Exception e) {
                        Log.e("updateTab", e.getMessage(), e);
                    }
                }
                break;
            case VERY_SHORT_ANSWERS:
                //longShortQuestionInfoBtn.setVisibility(View.VISIBLE);
                if ((review || !reattempt) && StringUtils.isNotEmpty(factoryShipQnADto.getUserAnswer())) {
                    qnADtoList.get(pos).setReview(true);
                    if (factoryShipQnADto.isCompleted())
                        attemptedQuestions++;
                    double percent = 0;
                    try {
                        percent = ((Double.parseDouble(factoryShipQnADto.getMarksObtained())) * 100) / Double.parseDouble(factoryShipQnADto.getMarks());
                        if (percent >= 50)
                            cardView.setCardBackgroundColor(getResources().getColor(R.color.right_tab));
                        else
                            cardView.setCardBackgroundColor(getResources().getColor(R.color.wrong_tab));
                    } catch (Exception e) {
                        Log.e("updateTab", e.getMessage(), e);
                        if (factoryShipQnADto.isUserAnswerCorrect())
                            cardView.setCardBackgroundColor(getResources().getColor(R.color.right_tab));
                        else
                            cardView.setCardBackgroundColor(getResources().getColor(R.color.wrong_tab));
                    }
                }
                break;
            default:
                if ((review || !reattempt) && StringUtils.isNotEmpty(factoryShipQnADto.getUserAnswer())) {
                    qnADtoList.get(pos).setReview(true);
                    if (factoryShipQnADto.isCompleted())
                        attemptedQuestions++;
                    if (LectureContentType.valueOf(factoryShipQnADto.getType()) == LectureContentType.FILL_IN_THE_BLANKS ||
                            LectureContentType.valueOf(factoryShipQnADto.getType()) == LectureContentType.ONE_WORD_ANSWER) {
                        if (factoryShipQnADto.isUserAnswerCorrect()) {
                            cardView.setCardBackgroundColor(getResources().getColor(R.color.right_tab));
                        } else {
                            cardView.setCardBackgroundColor(getResources().getColor(R.color.wrong_tab));
                        }
                    }
                    else if (factoryShipQnADto.getAnswer().equalsIgnoreCase(factoryShipQnADto.getUserAnswer())) {
                        cardView.setCardBackgroundColor(getResources().getColor(R.color.right_tab));
                    }
                    else {
                        cardView.setCardBackgroundColor(getResources().getColor(R.color.wrong_tab));
                    }
                }
        }
        tab.setCustomView(customTab);
        tabLayout.addTab(tab, pos);
    }

    private void updateTab(int pos, FactoryShipQnADto factoryShipQnADto, boolean correct) {
        TabLayout.Tab tab = tabLayout.getTabAt(pos);
        CardView cardView = tab.getCustomView().findViewById(R.id.tab_card);
        LectureContentType lectureContentType = LectureContentType.valueOf(factoryShipQnADto.getType());
        double percent = 0;
        switch (lectureContentType) {
            case LONG_ANSWERS:
            case SHORT_ANSWERS:
                try {
                    percent = ((Double.parseDouble(factoryShipQnADto.getMarksObtained())) * 100) / Double.parseDouble(factoryShipQnADto.getMarks());
                } catch (Exception e) {
                    Log.e("updateTab", e.getMessage(), e);
                }
                if (percent > 50)
                    cardView.setCardBackgroundColor(getResources().getColor(R.color.right_tab));
                else
                    cardView.setCardBackgroundColor(getResources().getColor(R.color.wrong_tab));
                break;

            case VERY_SHORT_ANSWERS:
                try {
                    percent = ((Double.parseDouble(factoryShipQnADto.getMarksObtained())) * 100) / Double.parseDouble(factoryShipQnADto.getMarks());
                } catch (Exception e) {
                    Log.e("updateTab", e.getMessage(), e);
                }
                if (percent >= 50)
                    cardView.setCardBackgroundColor(getResources().getColor(R.color.right_tab));
                else
                    cardView.setCardBackgroundColor(getResources().getColor(R.color.wrong_tab));
                break;
            default:
                if (StringUtils.isNotEmpty(qnADtoList.get(pos).getUserAnswer())) {
                    if (correct)
                        cardView.setCardBackgroundColor(getResources().getColor(R.color.right_tab));
                    else
                        cardView.setCardBackgroundColor(getResources().getColor(R.color.wrong_tab));
                }
        }
    }

    @Override
    public void onSubmit(int pos, FactoryShipQnADto factoryShipQnADto, boolean correct) {
        Log.d(TAG, "onSubmit: ");
        if (StringUtils.isNotEmpty(factoryShipQnADto.getUserAnswer())) {
            if (!factoryShipQnADto.isClearAttempt())
                attemptedQuestions++;
            qnADtoList.set(pos, factoryShipQnADto);
            updateTab(pos, factoryShipQnADto, correct);
            LectureContentType lectureContentType = LectureContentType.valueOf(factoryShipQnADto.getType());
            switch (lectureContentType) {
                case CONCEPTUAL_QUESTION:
                case NUMERICAL_QUESTION:
                case MULTIPLE_CHOICE_QUESTION:
                case TRUE_FALSE:
                case ONE_WORD_ANSWER:
                case FILL_IN_THE_BLANKS:
                case VERY_SHORT_ANSWERS:
                    showMessageForType1(factoryShipQnADto, correct);
                    break;
                case MATCH_THE_COLUMN:
                    showMessageForType2();
            }
            setUpPostHandler();
            submitAnswer(factoryShipQnADto, correct);
            show = true;
            qnADtoList.get(selectedTab).setReview(true);

        } else {
            showSnackBar(factoryShipQnADto.getType());
        }

        showMatch = true;
        if (howToVideoView != null)
            howToVideoView.remove();
        startHowToHandler();
    }

    private void showMessageForType1(FactoryShipQnADto factoryShipQnADto, boolean correct) {
        String userName = MyApplication.getStringData("name");
        if (userName == null || userName.trim().isEmpty()) {
            userName = getString(R.string.beta);
        }
        boolean isQuick = !moreThanSevenSeconds;

        // Identify type
        LectureContentType type = LectureContentType.valueOf(factoryShipQnADto.getType());
        boolean isSpecialType = (type == LectureContentType.FILL_IN_THE_BLANKS ||
                type == LectureContentType.ONE_WORD_ANSWER ||
                type == LectureContentType.VERY_SHORT_ANSWERS);

        // Determine correctness safely
        boolean isAnswerCorrect;
        if (isSpecialType) {
            isAnswerCorrect = correct;
        } else {
            String userAns = factoryShipQnADto.getUserAnswer();
            String correctAns = factoryShipQnADto.getAnswer();

            isAnswerCorrect = userAns != null && correctAns != null &&
                    userAns.equalsIgnoreCase(correctAns);
        }

        // Set message text
        if (isQuick) {
            if (isAnswerCorrect) {
                message.setText(R.string.quick_right);
            } else {
                message.setText(String.format(getString(R.string.quick_wrong), userName));
            }
        } else {
            if (isAnswerCorrect) {
                message.setText(R.string.normal_right);
            } else {
                message.setText(String.format(getString(R.string.normal_wrong), userName));
            }
        }

        // Common UI updates
        messageAnimation.setAnimation(isAnswerCorrect ? R.raw.right_tick : R.raw.wrong_tick);
        mediaPlayer = MediaPlayer.create(this,
                isAnswerCorrect ? R.raw.rightanswer : R.raw.wronganswer);

        try {
            mediaPlayer.start();
            message.setVisibility(View.VISIBLE);

            if (getApplicationContext() != null) {
                messageDialog.show();
            }

        } catch (Exception e) {
            Log.e("ERR", "showMessageForType1: Unable to show dialog", e);
        }
    }


    private void showMessageForType2() {
        matchTheseMessageDialog = new Dialog(PracticeProblemsActivity.this);
        matchTheseMessageDialog.setContentView(R.layout.match_these_message_popup);
        matchTheseMessageDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
        matchTheseMessageDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        matchTheseMessageDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        matchTheseMessageDialog.setCancelable(false);
        TextView message = matchTheseMessageDialog.findViewById(R.id.message);
        if (moreThanSevenSeconds) {
            message.setText(R.string.normal_match_these);
        } else {
            message.setText(R.string.quick_match_these);
        }
        mediaPlayer = MediaPlayer.create(this, R.raw.rightanswer);
        matchTheseMessageDialog.show();
    }

    private void showSnackBar(String type) {
        LectureContentType lectureContentType = LectureContentType.valueOf(type);
        switch (lectureContentType) {
            case CONCEPTUAL_QUESTION:
            case NUMERICAL_QUESTION:
            case MULTIPLE_CHOICE_QUESTION:
            case TRUE_FALSE:
                CommonMethods.showNegativeSnackbar(backButtonLayout, getString(R.string.please_select_your_option));
                break;
            case ONE_WORD_ANSWER:
            case FILL_IN_THE_BLANKS:
            case VERY_SHORT_ANSWERS:
                CommonMethods.showNegativeSnackbar(backButtonLayout, getString(R.string.please_enter_your_answer));
                break;
            case LONG_ANSWERS:
            case SHORT_ANSWERS:
                CommonMethods.showNegativeSnackbar(backButtonLayout, getString(R.string.Please_upload_the_Image_first));
                break;
        }
    }

    @Override
    public void openNext() {
        Log.d(TAG, "selectedTab" + selectedTab);
        Log.d(TAG, "qnADtoList size " + qnADtoList.size());

        if (selectedTab + 1 == qnADtoList.size()) {

            Log.d(TAG, "attemptedQuestions " + totalQuestions);
            Log.d(TAG, "review " + review);
            Log.d(TAG, "reattempt " + reattempt);
            Log.d(TAG, "progressionDto " + progressionDto);

            if (attemptedQuestions == totalQuestions && !review && !reattempt) {
                try {
                    setUpContinuityLayout();
                } catch (Exception e) {
                    back = true;
                    getOnBackPressedDispatcher().onBackPressed();
                }

            } else {
                back = true;
                getOnBackPressedDispatcher().onBackPressed();
            }
        } else {
            if (reattempt || !review)
                setUpPreHandler();
            viewPager.setCurrentItem(selectedTab + 1);
        }
    }

    private void startProgress() {
        final long startTime = System.currentTimeMillis();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                long elapsedTime = System.currentTimeMillis() - startTime;
                circularProgress = (int) ((elapsedTime * 100) / totalDuration);
                circularProgressIndicator.setProgress(circularProgress);
                if (elapsedTime < totalDuration) {
                    handler.postDelayed(this, stepDuration);
                } else {
                    handler.removeCallbacksAndMessages(null);
                    progressionLayout.performClick();
                }
            }
        }, stepDuration);
    }

    private void submitAnswer(FactoryShipQnADto factoryShipQnADto, boolean correct) {
        try {
            UserMicrolectureAnswer userMicrolectureAnswer = UserMicrolectureAnswer
                    .builder()
                    .chapterId(chapterId)
                    .correct(correct)
                    .correctAnswer(factoryShipQnADto.getAnswer())
                    .explanation(factoryShipQnADto.getAnswerEx())
                    .microlectureId(microLectureId)
                    .obtainedMarks("")
                    .options(String.join(",", factoryShipQnADto.getOptions()))
                    .question(factoryShipQnADto.getQuestion())
                    .questionId(factoryShipQnADto.getQuestionId())
                    .questionLevel(QuestionLevel.valueOf(factoryShipQnADto.getQuestionLevel()))
                    .questionType(convertIntoQuestionType(LectureContentType.getLectureContentType(factoryShipQnADto.getType())))
                    .timeTaken(0L)
                    .title(factoryShipQnADto.getTitle())
                    .obtainedMarks(factoryShipQnADto.getMarksObtained())
                    .totalMarks(factoryShipQnADto.getMarks())
                    .userAnswer(factoryShipQnADto.getUserAnswer())
                    .wrongAnswer(correct ? "" : factoryShipQnADto.getUserAnswer())
                    .build();

            Map<String, Object> map = new HashMap<>();
            map.put("personalised", false);
            String requestUrl = CommonMethods.getUrlParam(POST_MICROLECTURE_ANSWER, map);

            // ✅ Structured Logging of Complete Params
            Log.d("SubmitAnswer", "====== SUBMIT ANSWER REQUEST ======");
            Log.d("SubmitAnswer", "Request URL: " + requestUrl);
            if (BuildConfig.DEBUG) Log.d("SubmitAnswer", "Request Body: " + gson.toJson(userMicrolectureAnswer));
            Log.d("SubmitAnswer", "===================================");
            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                    requestUrl, new JSONObject(gson.toJson(userMicrolectureAnswer)), response -> {

                MyApplication.saveData(PreferenceKeys.HOME_DASHBOARD_DATA_RELOAD.getKey(), true);
                MyApplication.saveData(PreferenceKeys.MICROLECTURE_DATA_RELOAD.getKey(), true);
                Log.d("QUESTIONATTEMP", "post api result = " + response.toString());

            }, new VolleyGlobalErrorListener(this) {
                @Override
                public void postError(VolleyError error) {
                    Log.d("QUESTIONATTEMP", "post api result = " + error.getMessage());
                    CommonMethods.showNegativeSnackbar(backButtonLayout, error.getMessage());
                }
            });
            myJsonObjectRequest.setTag(TAG);
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
        } catch (Exception e) {
            Log.d("QUESTIONATTEMP", "post api result = " + e.getMessage());
            CommonMethods.showNegativeSnackbar(backButtonLayout, getString(R.string.please_try_again));
        }

    }

    @Override
    public void onReattempt(int pos) {
        SubmitListener.super.onReattempt(pos);
        TabLayout.Tab tab = tabLayout.getTabAt(pos);
        CardView cardView = tab.getCustomView().findViewById(R.id.tab_card);
        cardView.setCardBackgroundColor(getResources().getColor(R.color.white));
    }

    // onBackPressed logic moved to OnBackPressedCallback in onCreate

    private void setUpActionPopup() {
        actionDialog = new Dialog(PracticeProblemsActivity.this);
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
        animationView = actionDialog.findViewById(R.id.animation);
        animationView.setAnimation(R.raw.alert);
        close.setOnClickListener(v -> actionDialog.dismiss());
        btn1.setOnClickListener(v -> {
            back = true;
            getOnBackPressedDispatcher().onBackPressed();
        });
        btn2.setOnClickListener(v -> actionDialog.dismiss());
        title.setText(getResources().getString(R.string.dont_leave));
        try {
            LectureContentType type = LectureContentType.valueOf(qnADtoList.get(selectedTab).getType());
            if (type.equals(LectureContentType.LONG_ANSWERS) || type.equals(LectureContentType.SHORT_ANSWERS)) {
                subtitle.setText(getString(R.string.Leaving_this_page_will_cancel_your_upload));
            } else {
                subtitle.setText(getString(R.string.complete_all_questions_for_better_marks));
            }
            btn1_text.setText(getResources().getString(R.string.leave_anyway));
            btn2_text.setText(getResources().getString(R.string.Continue));
        } catch (Exception e) {
            Log.e("PRACPROBACT", "unable to get data");
        }
    }

    private void setUpPreHandler() {
        moreThanSevenSeconds = false;
        if (messageHandler != null)
            messageHandler.removeCallbacksAndMessages(null);
        messageHandler = new Handler();
        messageRunnable = () -> {
            moreThanSevenSeconds = true;
            if (messageHandler != null) {
                messageHandler.removeCallbacksAndMessages(null);
            }
        };
        messageHandler.postDelayed(messageRunnable, 7000);
    }

    private void setUpPostHandler() {
        if (messageHandler != null) {
            messageHandler.removeCallbacksAndMessages(null);
        }
        messageHandler = new Handler();
        messageRunnable = () -> {
            if (messageDialog != null)
                messageDialog.dismiss();
            if (getApplicationContext() != null && !PracticeProblemsActivity.this.isFinishing() && matchTheseMessageDialog != null)
                matchTheseMessageDialog.dismiss();
//            if(selectedTab==qnADtoList.size()-1){
//                back=true;
//                onBackPressed();
//            }
        };
        messageHandler.postDelayed(messageRunnable, 3000);
    }

    private void setUpMessagePopup() {
        messageDialog = new Dialog(PracticeProblemsActivity.this);
        messageDialog.setContentView(R.layout.message_popup_layout);
        messageDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
        messageDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        messageDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        messageDialog.setCancelable(false);
        message = messageDialog.findViewById(R.id.message);
        messageAnimation = messageDialog.findViewById(R.id.animation);
    }

    @Override
    public void reportQuestion() {
        reportDialog.show();
    }

    @Override
    public void onViewSolutionClick() {
        for (FactoryShipQnADto factoryShipQnADto : qnADtoList) {
            factoryShipQnADto.setHideViewSolution(true);
        }
        adapter.notifyDataSetChanged();
    }

    private void setUpReportDialog() {
        reportDialog = new Dialog(PracticeProblemsActivity.this);
        reportDialog.setContentView(R.layout.dialog_report_mock_test_solution);
        reportDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
        reportDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        reportDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        reportDialog.setCancelable(false);

        reportDialog.findViewById(R.id.close_btn).setOnClickListener(v -> reportDialog.dismiss());

        TextView reportText1 = reportDialog.findViewById(R.id.report_text_1);
        TextView reportText2 = reportDialog.findViewById(R.id.report_text_2);
        TextView reportText3 = reportDialog.findViewById(R.id.report_text_3);
        progress = reportDialog.findViewById(R.id.progress);

        reportText1.setOnClickListener(v -> {
            reportText1.setTextColor(getResources().getColor(R.color.arivihan_secondary_color));
            reportText = reportText1.getText().toString();
            reportText2.setTextColor(getResources().getColor(R.color.black));
            reportText3.setTextColor(getResources().getColor(R.color.black));
        });

        reportText2.setOnClickListener(v -> {
            reportText1.setTextColor(getResources().getColor(R.color.black));
            reportText2.setTextColor(getResources().getColor(R.color.arivihan_secondary_color));
            reportText = reportText2.getText().toString();
            reportText3.setTextColor(getResources().getColor(R.color.black));
        });

        reportText3.setOnClickListener(v -> {
            reportText1.setTextColor(getResources().getColor(R.color.black));
            reportText2.setTextColor(getResources().getColor(R.color.black));
            reportText3.setTextColor(getResources().getColor(R.color.arivihan_secondary_color));
            reportText = reportText3.getText().toString();
        });

        EditText reportInput = reportDialog.findViewById(R.id.report_text_input);
        reportInput.setOnFocusChangeListener((v, w) -> {
            reportText1.setTextColor(getResources().getColor(R.color.black));
            reportText2.setTextColor(getResources().getColor(R.color.black));
            reportText3.setTextColor(getResources().getColor(R.color.arivihan_secondary_color));
        });

        reportDialog.findViewById(R.id.report_question).setOnClickListener(v -> {
            progress.setVisibility(View.VISIBLE);
            makeReportRequest(String.valueOf(reportInput.getText()));
        });
    }

    private void makeReportRequest(String text) {
        JSONObject body = new JSONObject();
        try {
            body.put("description", "Question ID :: " + qnADtoList.get(selectedTab).getQuestionId());
            body.put("issue", reportText == null ? text : reportText);
            body.put("mockTest", false);
            body.put("microLectureId", microLectureId);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        MyJsonObjectRequest submitReport = new MyJsonObjectRequest(Request.Method.POST,
                TEST_REPORT_QUESTION,
                body,
                response -> {
                    Log.i("REPORTRESPONSE", response.toString());
                    CommonMethods.showPositiveSnackbar(backButtonLayout, getString(R.string.Report_Submitted));
                    progress.setVisibility(View.GONE);
                    reportDialog.dismiss();
                },
                new VolleyGlobalErrorListener(getApplicationContext()) {
                    @Override
                    public void postError(VolleyError error) {
                        Log.i("REPORTRESPONSE", error.toString());
                        CommonMethods.showNegativeSnackbar(backButtonLayout, error.getMessage());
                    }
                });
        submitReport.setTag(TAG);
        MyVolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(submitReport);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (howToVideoView != null && howToVideoView.videoView != null)
            howToVideoView.pausePlayer();
        if (messageHandler != null)
            messageHandler.removeCallbacksAndMessages(null);
        if (matchTheseMessageDialog != null)
            matchTheseMessageDialog.dismiss();
        if (messageDialog != null)
            messageDialog.dismiss();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (howToVideoView != null && howToVideoView.videoView != null) {
            howToVideoView.resumePlayer();
        }
    }

    private void setMatchTheseDialog() {
        matchTheseDialog = new Dialog(PracticeProblemsActivity.this);
        matchTheseDialog.setContentView(R.layout.match_these_dialog);
        reportDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
        matchTheseDialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(this, R.drawable.bachground_custom_dialogu_typeactivity));
        matchTheseDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LottieAnimationView lottieAnimationView = matchTheseDialog.findViewById(R.id.lottie);
        lottieAnimationView.setRotation(90);
        lottieAnimationView.setAnimation(R.raw.drag_and_drop);
        lottieAnimationView.playAnimation();
        matchTheseDialog.findViewById(R.id.close_btn).setOnClickListener(v -> {
            lottieAnimationView.pauseAnimation();
            matchTheseDialog.dismiss();
        });
        matchTheseDialog.findViewById(R.id.close).setOnClickListener(view -> {
            lottieAnimationView.pauseAnimation();
            matchTheseDialog.dismiss();
        });
        if (!CollectionUtils.isEmpty(qnADtoList) &&
                LectureContentType.valueOf(qnADtoList.get(selectedTab).getType()).equals(LectureContentType.MATCH_THE_COLUMN)
                && MyApplication.getBoolData(PreferenceKeys.SHOW_MATCH_THESE_DIALOG.getKey()) && !review) {
            matchTheseDialog.show();
            MyApplication.saveData(PreferenceKeys.SHOW_MATCH_THESE_DIALOG.getKey(), false);
        }
    }

    static class ViewPagerAdapter extends FragmentStateAdapter {
        List<FactoryShipQnADto> list;
        SubmitListener submitListener;
        boolean reattempt;
        boolean review;
        FragmentActivity activity;

        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<FactoryShipQnADto> list, boolean reattempt, boolean review, SubmitListener submitListener) {
            super(fragmentActivity);
            this.activity = fragmentActivity;
            this.list = list;
            this.submitListener = submitListener;
            this.reattempt = reattempt;
            this.review = review;
        }

        @Override
        public Fragment createFragment(int position) {
            Fragment fragment = null;
            String subjectCode = activity.getIntent().getStringExtra("subjectCode");
            Log.d("subjectCodeIssue", "createFragment: " + subjectCode);
            LectureContentType type = LectureContentType.valueOf(list.get(position).getType());
            FactoryShipQnADto factoryShipQnADto = list.get(position);
            factoryShipQnADto.setReattempt(reattempt);
            boolean reviewStatus = factoryShipQnADto.isReview();
            factoryShipQnADto.setReview(review);
            switch (type) {
                case CONCEPTUAL_QUESTION:
                case NUMERICAL_QUESTION:
                case MULTIPLE_CHOICE_QUESTION:
                case TRUE_FALSE:
                    fragment = ChoiceTypeFragment.newInstance(factoryShipQnADto, String.valueOf(position + 1), String.valueOf(list.size()), subjectCode);
                    break;
                case ONE_WORD_ANSWER:
                case FILL_IN_THE_BLANKS:
                    fragment = ValueTypeFragment.newInstance(factoryShipQnADto, String.valueOf(position + 1), String.valueOf(list.size()), subjectCode);
                    break;
                case VERY_SHORT_ANSWERS:
                    fragment = VeryShortFragment.newInstance(factoryShipQnADto, String.valueOf(position + 1), String.valueOf(list.size()), subjectCode);
                    break;
                case MATCH_THE_COLUMN:
                    if (factoryShipQnADto.isCompleted() && (!factoryShipQnADto.isReattempt() || factoryShipQnADto.isReview())) {
                        BoardQuestionActivity.isMatchTheseSubmitted = true;
                    } else {
                        BoardQuestionActivity.isMatchTheseSubmitted = false;
                    }
                    fragment = MatchTheseTypeFragment.newInstance(factoryShipQnADto, String.valueOf(position + 1), String.valueOf(list.size()), subjectCode);
                    break;
                case SHORT_ANSWERS:
                case LONG_ANSWERS:
                    Log.d("subjectCodeIssue-fragment", "createFragment: " + subjectCode);
                    fragment = SubjectiveTypeFragment.newInstance(factoryShipQnADto, String.valueOf(position + 1), String.valueOf(list.size()), subjectCode);
            }
            factoryShipQnADto.setReview(reviewStatus);
            return fragment;
        }

        @Override
        public int getItemCount() {
            if (list != null) {
                return list.size();
            } else {
                return 0;
            }
        }
    }

    private QuestionType convertIntoQuestionType(LectureContentType lectureContentType) {

        switch (lectureContentType) {
            case NUMERICAL_QUESTION:
                return QuestionType.NUMERICAL;
            case CONCEPTUAL_QUESTION:
                return QuestionType.CONCEPTUAL;
            case TRUE_FALSE:
                return QuestionType.TRUE_FALSE;
            case LONG_ANSWERS:
                return QuestionType.LONG_ANSWERS;
            case SHORT_ANSWERS:
                return QuestionType.SHORT_ANSWERS;
            case VERY_SHORT_ANSWERS:
                return QuestionType.VERY_SHORT_ANSWERS;
            case FILL_IN_THE_BLANKS:
                return QuestionType.FILL_IN_THE_BLANKS;
            case ONE_WORD_ANSWER:
                return QuestionType.ONE_WORD_ANSWER;
            case MULTIPLE_CHOICE_QUESTION:
                return QuestionType.MULTIPLE_CHOICE_QUESTION;
            case MATCH_THE_COLUMN:
                return QuestionType.MATCH_THE_COLUMN;
        }

        return QuestionType.NUMERICAL;
    }


    @Override
    protected void onDestroy() {
        MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll(TAG);
        removeAllDialog();
        super.onDestroy();
    }

    private void removeAllDialog() {

        if (actionDialog != null && actionDialog.isShowing()) {
            actionDialog.dismiss();
            actionDialog.cancel();
        }
        actionDialog = null;

        if (matchTheseMessageDialog != null && matchTheseMessageDialog.isShowing()) {
            matchTheseMessageDialog.dismiss();
            matchTheseMessageDialog.cancel();
        }
        matchTheseMessageDialog = null;

        if (messageDialog != null && messageDialog.isShowing()) {
            messageDialog.dismiss();
            messageDialog.cancel();
        }
        messageDialog = null;

        if (reportDialog != null && reportDialog.isShowing()) {
            reportDialog.dismiss();
            reportDialog.cancel();
        }

        reportDialog = null;

        if (matchTheseDialog != null && matchTheseDialog.isShowing()) {
            matchTheseDialog.dismiss();
            matchTheseDialog.cancel();
        }

        matchTheseDialog = null;
    }
}