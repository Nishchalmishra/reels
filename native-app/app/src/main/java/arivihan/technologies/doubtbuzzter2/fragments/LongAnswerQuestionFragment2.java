package arivihan.technologies.doubtbuzzter2.fragments;

import static android.app.Activity.RESULT_OK;
import static androidx.core.content.ContextCompat.checkSelfPermission;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.EXTRACT_TEXT_FROM_IMAGE_URI;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.text.HtmlCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageActivity;
import com.theartofdev.edmodo.cropper.CropImageOptions;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.MyItemTouchHelper;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.BoardQuestionActivity;
import arivihan.technologies.doubtbuzzter2.activity.ChatSupportActivity;
import arivihan.technologies.doubtbuzzter2.activity.ExoPlayerActivity;
import arivihan.technologies.doubtbuzzter2.activity.ImageCropperActivity;
import arivihan.technologies.doubtbuzzter2.activity.VideoPlayerActivity;
import arivihan.technologies.doubtbuzzter2.activity.ZoomImageActivity;
import arivihan.technologies.doubtbuzzter2.adapter.MatchTheseAdapterLeft;
import arivihan.technologies.doubtbuzzter2.adapter.MatchTheseAdapterRight;
import arivihan.technologies.doubtbuzzter2.adapter.OptionQuestionAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.UploadedImageRecyclerAdapter;
import arivihan.technologies.doubtbuzzter2.common.ArivihanCustomDialog;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.FullScreenImageDialog;
import arivihan.technologies.doubtbuzzter2.common.MultipartRequestDataPart;
import arivihan.technologies.doubtbuzzter2.constants.QuestionLevel;
import arivihan.technologies.doubtbuzzter2.customView.MyToolTip;
import arivihan.technologies.doubtbuzzter2.customView.SavBottomSheetDialog;
import arivihan.technologies.doubtbuzzter2.enums.FullViewImageType;
import arivihan.technologies.doubtbuzzter2.helper.FileUtil;
import arivihan.technologies.doubtbuzzter2.helper.ImageLoaderUtil;
import arivihan.technologies.doubtbuzzter2.helper.TooltipHelper;
import arivihan.technologies.doubtbuzzter2.listener.CaseStudyListener;
import arivihan.technologies.doubtbuzzter2.listener.GridListener;
import arivihan.technologies.doubtbuzzter2.listener.OptionListener;
import arivihan.technologies.doubtbuzzter2.model.AnswerState;
import arivihan.technologies.doubtbuzzter2.model.ImageTextExtractionResponse;
import arivihan.technologies.doubtbuzzter2.model.LectureContentType;
import arivihan.technologies.doubtbuzzter2.model.MatchTheseLeftOption;
import arivihan.technologies.doubtbuzzter2.model.MatchTheseRightOption;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureContentDto;
import arivihan.technologies.doubtbuzzter2.model.MyTooltipDto;
import arivihan.technologies.doubtbuzzter2.model.OptionModel;
import arivihan.technologies.doubtbuzzter2.model.QuestionType;
import arivihan.technologies.doubtbuzzter2.model.TestSeriesQuestionDto;
import arivihan.technologies.doubtbuzzter2.model.TooltipStatusDto;
import arivihan.technologies.doubtbuzzter2.model.TooltipType;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyMultipartRequest;
import lombok.SneakyThrows;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;


public class LongAnswerQuestionFragment2 extends Fragment implements UploadedImageRecyclerAdapter.AddClickListener,
        SavBottomSheetDialog.BottomSheetListener, MyToolTip.ToolTipClickListener {

    private static final String TAG = LongAnswerQuestionFragment2.class.getSimpleName();
    int fragNumber;
    private static final String ARG_PARAM1 = "question";
    private static final String ARG_PARAM2 = "listener";
    private static final String ARG_PARAM3 = "map";
    private static final String ARG_PARAM5 = "qNo";
    private static final String ARG_PARAM6 = "testSeriesId";

    private GridListener gridListener;
    View v;
    Map<String, Integer> map;
    Gson gson = new Gson();
    TestSeriesQuestionDto testSeriesQuestionDtoAnalytics;
    TestSeriesQuestionDto dto;
    OptionListener optionListener;
    CaseStudyListener caseStudyListener;
    boolean isReviewMode = false;
    ImageView image;
    RecyclerView uploadedImageRecyclerView;
    UploadedImageRecyclerAdapter uploadedImageRecyclerAdapter;
    RelativeLayout uploadFileLayout;
    ArrayList<Uri> list;
    private LinearLayout markLayout, markLinearlayout;
    private RelativeLayout MarkLayout;
    private ToggleButton markButton;
    List<File> imagesToUpload = new ArrayList<>();
    private static final int CAMERA_PERMISSION_CODE = 100;
    String currentPhotoPath = "";
    private Uri imageuri;
    private boolean clickedFromUpdate;
    private Uri uri;
    int imageAdded = 0;
    LinearLayout imageTextExtractionLoader, uploadImageLayoutContainer;

    RelativeLayout uploadImageLayout;
    boolean isLast;
    LinearLayout seeSubmissionResult;
    View extractedTextFromImageView;
    ConstraintLayout imageExtractionLayout;
    ArrayList<OptionModel> model = new ArrayList<>();
    ImageView questionImage, book, unbook;
    Dialog dialog;
    Dialog image_pick_dialog;
    Dialog reattemptDialog;
    String chap;
    int selectedCurrentOption = -1;
    int integerAnswer = 0;
    ScrollView scrollView;
    String submited = "-1";
    String contentUrl = "";
    RecyclerView options;
    ProgressBar progress;
    ScrollView scroll_bar;
    TextView tagText;
    CardView tagCard;
    TextView topic_name, question1, mark, ansText, yourAttempt, btnText, nextTopicMsg, nextTopicName, text_centre, questionTypeTitle, imageUploadText;
    CardView sub, nextQuestion, uploadSolutionBtn;
    CardView card, clearAnswer;
    ImageView back;
    FrameLayout ImageFrame, TextFrame;
    String quesType = "";
    String s = "";
    RelativeLayout relativeLayout;
    List<String> optionList = new ArrayList<>();
    public String id, title, ex;
    RelativeLayout ParentLayout;
    public int attempt = 0;
    private boolean editTextEnabled;
    private TextView optionMt;

    private MicroLectureContentDto microLectureContentDto;
    String userAnswerOption;
    String userAnswer;
    public static boolean isMatchTheseSubmittedMock = false;

    List<String> correctAnswerList;
    List<String> userAnswerList;

    ArrayList<Integer> finalAnswerList;
    LinearLayout matchTheseLayout;
    ArrayList<MatchTheseLeftOption> list_left = new ArrayList<>();
    ArrayList<MatchTheseRightOption> list_right = new ArrayList<>();
    MatchTheseAdapterLeft matchTheseAdapterLeft;
    MatchTheseAdapterRight matchTheseAdapterRight;
    LottieAnimationView lottieAnimationView;
    Dialog subjectiveAnalysisPopup;
    MyJsonObjectRequest submitJsonObjectRequest;
    String Type = "";
    ImageView caseStudyImageView;
    public static boolean isTextExtracting = false;
    boolean isTextExtractedFromImage = false;
    String extractedTextFromImage = "";
    MyItemTouchHelper myItemTouchHelper;
    int croppingImageIndex = -1;
    boolean isSingleImageSelection = false;
    TextView uploadImageStepGuide;
    //    long word_limit_buffer;
    Animator revealAnimator;
    RecyclerView left_recyclerView;
    RecyclerView right_recyclerView;
    private String maxLengthText = "";
    SavBottomSheetDialog savBottomSheetDialog;
    private ActivityResultLauncher<Intent> galleryLauncher;


    Handler handler = new Handler();
    Runnable runnable;
    Handler caseStudyHandler = new Handler();
    Runnable caseStudyRunnable;
    private RelativeLayout caseStudyFab;
    private MaterialCardView caseStudyCard;
    private TextView caseStudyText;
    private CardView caseStudyNumCard;
    private TextView caseStudyNumText;
    private ArivihanCustomDialog caseStudyDialog;
    private String testSeriesId;
    private RelativeLayout askDoubtFab;
    private CardView greenMarks;
    private TextView greenMarksText;
    LinearLayout tabView;
    LinearLayout divider;
    LinearLayout myAnswer;
    LinearLayout correctAnswer;
    TextView myAnswerText;
    TextView correctAnswerText;
    LinearLayout myAnswerUnderline;
    LinearLayout correctAnswerUnderline;
    private LinearLayout solutionLayout;
    CardView difficultyCard;
    TextView difficultyType;
    private CardView solutionImageCard;
    private TextView solutionTextMatchThese;
    private ImageLoaderUtil solutionImageMatchThese;
    TextView description;
    TextView matchTheseDescription;
    MaterialCardView textCard;
    CardView descriptionCard;
    ConstraintLayout textCardLayout;
    CardView editTextBack;
    EditText extractedText;
    TextView countText;
    TextView readText;
    boolean readMore;
    int maxLines = 15;
    View solutionView;
    TextView difficultyLevel;
    CardView marksCard;
    TextView marksText;
    ImageLoaderUtil solutionImage;
    TextView solutionText, analysisText;
    MaterialCardView analysisCard;
    MaterialCardView idealSolutionCard;
    ImageView reportQuestion;
    FullScreenImageDialog fullScreenImageDialog;

    LinearLayout typeAnswerLayout;
    TextView typeAnswerStatusText;
    MaterialCardView typeAnswerCard;
    EditText typeAnswer;
    TextView typedAnswerWordCount;
    CardView viewSolutionCard;
    TextView viewSolutionText;
    ViewTreeObserver observer;
    boolean dataLoaded;

    int stepCount = 1;
    int totalSteps;
    TextView analysisTitle, idealTitle;
    boolean increaseCount = true;
    CardView askDoubtCard;
    MyMultipartRequest multipartRequest;
    boolean textExtracted;
    TextView uploaded_image_text;
    private boolean isCurrentFragment;

    private long questionStartTime = 0L, questionEndTime;

    private boolean isKeyBoardActive = true;
    private boolean isKeyBoardActivePreviousState;
    private TooltipStatusDto uploadTooltipStatus = new TooltipStatusDto();
    private TooltipStatusDto reviewTooltipStatus = new TooltipStatusDto();
    private TooltipStatusDto clickTooltipStatus = new TooltipStatusDto();
    private TooltipStatusDto optionTooltipStatus = new TooltipStatusDto();
    private TooltipStatusDto submitTooltipStatus = new TooltipStatusDto();
    private TextView button_answer_save_text;
    long wordLimitBuffer =
            MyApplication.getFirebaseRemoteConfig().getLong("word_limit_buffer");

    private final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {

//        for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
//            String permission = entry.getKey();
//            boolean isGranted = entry.getValue();
//            if (permission.equals(Manifest.permission.CAMERA) && isGranted) {
//                checkCameraPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
//            }
//        }

        Boolean isCameraGranted = permissions.get(Manifest.permission.CAMERA);

        if (isCameraGranted != null && isCameraGranted) {
            // Permission granted
            captureImage();
        } else {
            // Permission denied
            if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)) {
                Dialog dialog = CommonMethods.getCameraPermissionDialog(requireContext());
                dialog.show();
            } else {
                // Denied
                Toast.makeText(requireContext(), getString(R.string.camera_permission_zaruri_hai_upload_ke_liye), Toast.LENGTH_SHORT).show();
            }
        }

    });

    public LongAnswerQuestionFragment2() {
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(ARG_PARAM1, dto);
        outState.putSerializable(ARG_PARAM2, testSeriesQuestionDtoAnalytics);
        outState.putSerializable(ARG_PARAM3, (Serializable) map);
        outState.putBoolean("isCurrentFragment", isCurrentFragment);
        outState.putInt(ARG_PARAM5, fragNumber);
        outState.putBoolean("isReviewMode", isReviewMode);
        outState.putString(ARG_PARAM6, testSeriesId);
    }

    public static LongAnswerQuestionFragment2 newInstance(int fragNumber, TestSeriesQuestionDto dto,
                                                          TestSeriesQuestionDto testSeriesQuestionDtoAnalytics,
                                                          Map<String, Integer> map, boolean isReviewMode, String testSeriesId, boolean isCurrentFragment) {
        LongAnswerQuestionFragment2 fragment = new LongAnswerQuestionFragment2();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, dto);
        args.putSerializable(ARG_PARAM2, testSeriesQuestionDtoAnalytics);
        args.putSerializable(ARG_PARAM3, (Serializable) map);
        args.putBoolean("isCurrentFragment", isCurrentFragment);
        args.putInt(ARG_PARAM5, fragNumber);
        args.putBoolean("isReviewMode", isReviewMode);
        args.putString(ARG_PARAM6, testSeriesId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            optionListener = (OptionListener) context;
        } catch (ClassCastException e) {
            //throw new ClassCastException(context.getClass().toString() + " must implement RegWizardCallback ");
        }

        try {
            caseStudyListener = (CaseStudyListener) context;
        } catch (ClassCastException e) {
            //throw new ClassCastException(context.getClass().toString() + " must implement RegWizardCallback ");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dto = (TestSeriesQuestionDto) getArguments().getSerializable(ARG_PARAM1);
            testSeriesQuestionDtoAnalytics = (TestSeriesQuestionDto) getArguments().getSerializable(ARG_PARAM2);
            map = (Map<String, Integer>) getArguments().getSerializable(ARG_PARAM3);
            fragNumber = getArguments().getInt(ARG_PARAM5);
            isCurrentFragment = getArguments().getBoolean("isCurrentFragment");
            questionStartTime = System.currentTimeMillis();
            isReviewMode = getArguments().getBoolean("isReviewMode");
            testSeriesId = getArguments().getString("testSeriesId");
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    @SneakyThrows
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_subjective_question_2, container, false);
        Type = MyApplication.getStringData(PreferenceKeys.RESPONSE.getKey());

        BoardQuestionActivity.isMatchTheseSubmitted = false;

        Log.d("QUestionDto", "" + dto);
        microLectureContentDto = new MicroLectureContentDto();
        microLectureContentDto.setAnswer(dto.getAnswer());
        microLectureContentDto.setAnswerEx(dto.getExplanation());
        microLectureContentDto.setAnswerImage(dto.getExplanationImage());
        LectureContentType lectureContentType = null;
        for (LectureContentType value : LectureContentType.values()) {
            if (value.name().equalsIgnoreCase(dto.getQuestionType())) {
                lectureContentType = value;
                break;
            }
        }

        microLectureContentDto.setLectureContentType(lectureContentType);
        microLectureContentDto.setQuestion(dto.getQuestionText());
        microLectureContentDto.setQuestionImage(dto.getQuestionImage());
        microLectureContentDto.setOptions(dto.getOptions());
        microLectureContentDto.setAnswers(dto.getAnswers());

        microLectureContentDto.setWordLimit(updateWordLimit(dto, lectureContentType));
        microLectureContentDto.setWordLimitApplicable(dto.isWordLimitApplicable());

        extractedText = v.findViewById(R.id.extracted_text);

        extractedTextFromImageView = v.findViewById(R.id.edit_text_layout);
        image = v.findViewById(R.id.extracted_image);
        imageUploadText = v.findViewById(R.id.upload_image_text);

        SpannableString spannableString;
        if (microLectureContentDto.getLectureContentType() == LectureContentType.VERY_SHORT_ANSWERS)
            spannableString = new SpannableString(getString(R.string.Answer_upload_karein));
        else
            spannableString = new SpannableString(getString(R.string.upload_answer_aur_board_expert));

        String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        if ("hi".equals(lang) || "hi-rIN".equals(lang)) {
            spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.spannable_teal_color)), 0, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.spannable_teal_color)), 0, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        imageUploadText.setText(spannableString);

        button_answer_save_text = v.findViewById(R.id.button_answer_save_text);
        imageTextExtractionLoader = v.findViewById(R.id.extracting_text_layout);
        imageExtractionLayout = v.findViewById(R.id.image_extraction_layout);
        card = v.findViewById(R.id.button_answer_save);
        card.setVisibility(View.GONE);
//        text_centre = v.findViewById(R.id.text_centre);
        uploadSolutionBtn = v.findViewById(R.id.upload_solution_btn);
        uploadImageLayout=v.findViewById(R.id.upload_image_layout);
        uploadedImageRecyclerView = v.findViewById(R.id.uploaded_image_rv);
        btnText = v.findViewById(R.id.btn_text);
        ansText = v.findViewById(R.id.CorrectAnswer);
        clearAnswer = v.findViewById(R.id.button_clear_answer);
        yourAttempt = v.findViewById(R.id.yourAttempt);
        uploadImageStepGuide = v.findViewById(R.id.steps_guide);
        uploadImageLayoutContainer = v.findViewById(R.id.upload_image_layout_container);

        scrollView = v.findViewById(R.id.scroll_view);

        // FIND VIEW BY ID DEFINE-----------------
        tagText = v.findViewById(R.id.tag_text);
        tagCard = v.findViewById(R.id.tag_card);

        left_recyclerView = v.findViewById(R.id.rv_left_options);
        right_recyclerView = v.findViewById(R.id.rv_right_options);
        matchTheseLayout = v.findViewById(R.id.match_these_layout);
        progress = v.findViewById(R.id.progress);
        ParentLayout = v.findViewById(R.id.ParentLayout);
        options = v.findViewById(R.id.optionRecyclerView);
        nextQuestion = v.findViewById(R.id.next);
        nextTopicMsg = v.findViewById(R.id.nextTopicMessage);
        nextTopicName = v.findViewById(R.id.nextTopicName);
        topic_name = v.findViewById(R.id.Topic_name);
        markLayout = v.findViewById(R.id.mark);
        mark = v.findViewById(R.id.markText);
        markButton = v.findViewById(R.id.mark_button2);
        markLinearlayout = v.findViewById(R.id.markLinearLayout);
        book = v.findViewById(R.id.bookmark_done);
        scroll_bar = v.findViewById(R.id.scroll_bar);
        back = v.findViewById(R.id.back_arrow);
        unbook = v.findViewById(R.id.bookmark_outline);
        relativeLayout = v.findViewById(R.id.relative_layout_ques);
        TextFrame = v.findViewById(R.id.Frame_with_Only_Text);
        ImageFrame = v.findViewById(R.id.Frame_with_image);
        questionImage = v.findViewById(R.id.question_image);
        question1 = v.findViewById(R.id.question);
        questionTypeTitle = v.findViewById(R.id.questionType);
        askDoubtFab = v.findViewById(R.id.ask_doubt_fab);
        caseStudyFab = v.findViewById(R.id.case_study_fab);
        caseStudyCard = v.findViewById(R.id.case_study_fab_card);
        caseStudyText = v.findViewById(R.id.case_study_fab_text);
        caseStudyNumCard = v.findViewById(R.id.case_study_num);
        caseStudyNumText = v.findViewById(R.id.case_study_num_text);
        greenMarks = v.findViewById(R.id.green_marks);
        greenMarksText = v.findViewById(R.id.green_marks_text);
        tabView = v.findViewById(R.id.tab_view);
        divider = v.findViewById(R.id.divider);
        myAnswer = v.findViewById(R.id.my_answer_layout);
        correctAnswer = v.findViewById(R.id.correct_answer_layout);
        myAnswerText = v.findViewById(R.id.my_answer_text);
        correctAnswerText = v.findViewById(R.id.correct_answer_text);
        myAnswerUnderline = v.findViewById(R.id.my_answer_underline);
        correctAnswerUnderline = v.findViewById(R.id.correct_answer_underline);
        solutionLayout = v.findViewById(R.id.solution_layout);
        solutionTextMatchThese = v.findViewById(R.id.solution_text_match_these);
        solutionImageMatchThese = v.findViewById(R.id.solution_image_match_these);
        solutionImageCard = v.findViewById(R.id.solution_image_card);
        difficultyCard = v.findViewById(R.id.difficulty_card);
        difficultyType = v.findViewById(R.id.difficulty_type);
        matchTheseDescription = v.findViewById(R.id.match_these_description);
        reportQuestion = v.findViewById(R.id.report_question);

        textCard = v.findViewById(R.id.text_card);
        textCardLayout = v.findViewById(R.id.text_card_layout);
        description = v.findViewById(R.id.description);
        descriptionCard = v.findViewById(R.id.description_card);
        countText = v.findViewById(R.id.count_text);
        readText = v.findViewById(R.id.read_text);
        editTextBack = v.findViewById(R.id.edit_text_back);

        marksText = v.findViewById(R.id.marks_text);
        marksCard = v.findViewById(R.id.marks_card);
        solutionView = v.findViewById(R.id.solution_view);
        analysisText = v.findViewById(R.id.analysis_text);
        solutionText = v.findViewById(R.id.solution_text_sav);
        solutionImage = v.findViewById(R.id.solution_image_sav);
        difficultyCard = v.findViewById(R.id.difficulty_card);
        difficultyLevel = v.findViewById(R.id.difficulty_type);
        analysisCard = v.findViewById(R.id.analysis_card);
        idealSolutionCard = v.findViewById(R.id.ideal_solution_card);
        v.findViewById(R.id.solution_text).setVisibility(View.GONE);
        v.findViewById(R.id.solution_title).setVisibility(View.GONE);

        typeAnswerLayout = v.findViewById(R.id.type_answer_layout);
        typeAnswerStatusText = v.findViewById(R.id.type_answer_status_text);
        typeAnswerCard = v.findViewById(R.id.type_answer_card);
        typeAnswer = v.findViewById(R.id.type_answer);
        typedAnswerWordCount = v.findViewById(R.id.typed_answer_word_count);
        viewSolutionCard = v.findViewById(R.id.view_solution_card);
        viewSolutionText = v.findViewById(R.id.view_solution_text);
        analysisTitle = v.findViewById(R.id.analysis_title);
        idealTitle = v.findViewById(R.id.ideal_title);
        askDoubtCard = v.findViewById(R.id.ask_doubt_card);
        uploaded_image_text = v.findViewById(R.id.uploaded_image_text);
        //-----------------------------------

        myAnswer.setOnClickListener(v -> {
            myAnswerText.setTextColor(getContext().getResources().getColor(R.color.selected_tab_match_these));
            myAnswerUnderline.setBackgroundColor(getContext().getResources().getColor(R.color.selected_tab_match_these));
            correctAnswerText.setTextColor(getContext().getResources().getColor(R.color.not_selected_tab_match_these));
            correctAnswerUnderline.setBackgroundColor(getContext().getResources().getColor(R.color.white));
            setUserAnswerList(dto.getAnswer(), dto.getUserAnswer());
            setUserMatchTheseMarks();
        });
        correctAnswer.setOnClickListener(v -> {
            myAnswerText.setTextColor(getContext().getResources().getColor(R.color.not_selected_tab_match_these));
            myAnswerUnderline.setBackgroundColor(getContext().getResources().getColor(R.color.white));
            correctAnswerText.setTextColor(getContext().getResources().getColor(R.color.selected_tab_match_these));
            correctAnswerUnderline.setBackgroundColor(getContext().getResources().getColor(R.color.selected_tab_match_these));
            setUserAnswerList(dto.getAnswer(), dto.getAnswer());
            greenMarksText.setText("+" + dto.getScore());
            greenMarksText.setTextColor(getContext().getResources().getColor(R.color.grey_500));
            greenMarks.setCardBackgroundColor(getContext().getResources().getColor(R.color.grey_100));
        });

        askDoubtFab.setOnClickListener(view -> {
            try {
                Intent intent = new Intent(getActivity(), ChatSupportActivity.class);
                intent.putExtra("isMockTestDoubt", true);
                intent.putExtra("doubtId", CommonMethods.stringToMd5("mtq_" + dto.getQuestionId() + "_" + MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()) + "_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey())));
                intent.putExtra("question", dto.getQuestionText());
                intent.putExtra("answer", dto.getAnswer());
                startActivity(intent);
                if (reviewTooltipStatus != null && reviewTooltipStatus.getStepCount() == 4)
                    goToNextTooltip();
            } catch (Exception e) {
                Log.i("ARIVIHAN_EXCEPTION", "Unable to start activity ChatSupportActivity.class");
            }
        });
        caseStudyFab.setOnClickListener(view -> openCaseStudy());
        if (StringUtils.isNotEmpty(dto.getComprehensiveQuesNo()) &&
                (StringUtils.isNotEmpty(dto.getComprehensiveCaseStudyText()) || StringUtils.isNotEmpty(dto.getComprehensiveCaseStudyImage()))) {
            caseStudyNumCard.setVisibility(View.VISIBLE);
            caseStudyNumText.setText(dto.getComprehensiveQuesNo());
            caseStudyFab.setVisibility(View.VISIBLE);
        }

//        Log.d(TAG,"Question year: "+dto.getYear());
        if (StringUtils.isNotEmpty(dto.getYear())) {
            tagCard.setVisibility(View.VISIBLE);
            tagText.setText(dto.getYear());
        } else {
            tagCard.setVisibility(View.GONE);
        }

        markLinearlayout.setOnClickListener(v1 -> {
            dto.setMarked(!dto.isMarked());
            changeMark();
        });
        // Data Fitting--------------

        optionList = microLectureContentDto.getOptions();
        if (StringUtils.isNotEmpty(microLectureContentDto.getAnswerEx())) {
            ex = microLectureContentDto.getAnswerEx().trim();
        } else {
            ex = getString(R.string.no_explanation);
        }

        if (StringUtils.isNotEmpty(dto.getQuestionImage())) {
            Glide.with(this).load(dto.getQuestionImage()).into(questionImage);
            questionImage.setVisibility(View.VISIBLE);
        }

        //ques_num.setText(getString(R.string.questions) + " " + ques_no);
        quesType = dto.getQuestionType();
        if (!StringUtils.isEmpty(dto.getQuestionText())) {
            question1.setText(dto.getQuestionText());
        } else {
            question1.setVisibility(View.GONE);
        }

        clearAnswer.setOnClickListener(v12 -> {
            TooltipHelper.getInstance().dismiss();
            dto.setUserAnswer(null);
            dto.setTypedAnswer(false);
            if (microLectureContentDto.getLectureContentType() != LectureContentType.MATCH_THE_COLUMN) {
                update2();
                list.clear();
                dto.setUriList(new ArrayList<>());
                uploadedImageRecyclerAdapter.notifyDataSetChanged();
            } else {
                setMatchThese(v);
                dto.setAnswerState(AnswerState.UNANSWERED);
                clearAnswer.setVisibility(View.INVISIBLE);
                changeAnswerState(dto.isMarked());
                card.setVisibility(View.VISIBLE);
            }
        });

        questionImage.setOnClickListener(view -> {
            fullScreenImageDialog = new FullScreenImageDialog(requireContext(),
                    dto.getQuestionImage(), FullViewImageType.QUESTION);
            fullScreenImageDialog.show();
        });

        //updareRecycerViewData();

        right_recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (isMatchTheseSubmittedMock) {
                    left_recyclerView.scrollBy(dx, dy);
                }
            }
        });

        card.setOnClickListener(view -> {
            TooltipHelper.getInstance().dismiss();
            if (isReviewMode) {
                finishReviewTooltip(false);
                optionListener.nextPage(fragNumber);
            } else {
                clickSubmit(true);
            }
        });

        if (microLectureContentDto.getLectureContentType() == LectureContentType.MATCH_THE_COLUMN) {
            setMatchThese(v);
            editTextEnabled = false;
            typeAnswerLayout.setVisibility(View.GONE);
            uploadImageLayoutContainer.setVisibility(View.GONE);
            question1.setVisibility(View.GONE);
            v.findViewById(R.id.description).setVisibility(View.VISIBLE);
            editTextEnabled = false;
            scroll_bar.setVisibility(View.VISIBLE);
            options.setVisibility(View.GONE);
            matchTheseLayout.setVisibility(View.VISIBLE);
            if (isReviewMode) {
                button_answer_save_text.setText(getString(R.string.next_with_space));
                isMatchTheseSubmittedMock = true;
                tabView.setVisibility(View.VISIBLE);
                if (StringUtils.isEmpty(dto.getUserAnswer())) {
                    myAnswer.setVisibility(View.GONE);
                    correctAnswerText.setTextColor(getContext().getResources().getColor(R.color.selected_tab_match_these));
                    correctAnswerUnderline.setBackgroundColor(getContext().getResources().getColor(R.color.selected_tab_match_these));
                    setUserAnswerList(dto.getAnswer(), dto.getAnswer());
                    matchTheseAdapterLeft.notifyDataSetChanged();
                    matchTheseAdapterRight.notifyDataSetChanged();
                }
                scrollView.setVisibility(View.VISIBLE);
                extractedTextFromImageView.setVisibility(View.GONE);
                setMatchTheseAnswerLayout();
            } else {
                isMatchTheseSubmittedMock = false;
                matchTheseDescription.setVisibility(View.VISIBLE);
                setDefaultMatchTheseAnswer();
            }

        } else if (microLectureContentDto.getLectureContentType() == LectureContentType.VERY_SHORT_ANSWERS
                || microLectureContentDto.getLectureContentType() == LectureContentType.LONG_ANSWERS
                || microLectureContentDto.getLectureContentType() == LectureContentType.SHORT_ANSWERS) {

            if (StringUtils.isEmpty(dto.getUserAnswer())) {
                uploadImageLayoutContainer.setVisibility(View.VISIBLE);
                Log.d(TAG, "UserAnswer empty → showing uploadImageLayoutContainer");
            }

            isMatchTheseSubmittedMock = false;
            scroll_bar.setVisibility(View.GONE);
            editTextEnabled = false;
            setSubjectiveLayout(v);
            Log.d(TAG, "Subjective layout set for answer type: " + microLectureContentDto.getLectureContentType());

            if (isReviewMode) {
                setUpAnswerDetail();
                Log.d(TAG, "ReviewMode: subjective answer detail set");
            }

            if (microLectureContentDto.getLectureContentType()
                    == LectureContentType.VERY_SHORT_ANSWERS
                    && StringUtils.isEmpty(dto.getUserAnswer())) {
                Log.d(TAG, "condition: subjective answer detail set oncreate");
                uploadImageStepGuide.setText(getString(R.string.Option_2_Ya_photo_click_krke_upload_karein));
                typeAnswerLayout.setVisibility(View.VISIBLE);
                typeAnswerCard.setStrokeColor(getResources().getColor(R.color.text_card_stroke));
                typeAnswerStatusText.setText(getString(R.string.Option_1_Apna_answer_yha_type_karein));
                typeAnswer.setEnabled(true);
                typeAnswer.setText("");
                typedAnswerWordCount.setVisibility(View.GONE);
            }

            handleTypeEditText();
            Log.d(TAG, "Handled subjective EditText input");
        }
        if (isReviewMode) {
            button_answer_save_text.setText(getString(R.string.next));
            askDoubtFab.setVisibility(View.VISIBLE);
            markButton.setVisibility(View.INVISIBLE);
            typeAnswerLayout.setVisibility(View.GONE);
            mark.setVisibility(View.INVISIBLE);
            uploadImageLayoutContainer.setVisibility(View.GONE);
            card.setVisibility(View.VISIBLE);
        } else if (StringUtils.isEmpty(dto.getUserAnswer())
                && microLectureContentDto.getLectureContentType() == LectureContentType.VERY_SHORT_ANSWERS) {
            typeAnswerLayout.setVisibility(View.VISIBLE);
            uploadImageStepGuide.setText(getString(R.string.Option_2_Ya_photo_click_krke_upload_karein));
        }

        if (StringUtils.isNotEmpty(dto.getUserAnswer())) {
            card.setVisibility(View.VISIBLE);
        }

        if (isReviewMode) {
            greenMarksText.setText("+" + dto.getScore());
            greenMarksText.setTextColor(getContext().getResources().getColor(R.color.grey_500));
            greenMarks.setCardBackgroundColor(getContext().getResources().getColor(R.color.grey_100));
            uploadImageLayoutContainer.setVisibility(View.GONE);
            if (microLectureContentDto.getLectureContentType() == LectureContentType.MATCH_THE_COLUMN && StringUtils.isNotEmpty(dto.getUserAnswer()))
                setUserMatchTheseMarks();
        } else {
            greenMarksText.setText("+" + dto.getScore());
        }

        handleSetQuestionTitle();
        handleViewSolution();
        observeViewModelForTooltip(v);

        return v;

    }

    private String updateWordLimit(TestSeriesQuestionDto dto, LectureContentType lectureContentType) {
        long wordLimit = MyApplication.getFirebaseRemoteConfig().getLong(lectureContentType.name() + "_word_limit");
        return dto.getWordLimit() != null ? dto.getWordLimit() : (wordLimit != 0 ? String.valueOf(wordLimit) : "350");
    }

    private void handleViewSolution() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                if (isReviewMode) {
                    setVisibility();
                }
                handleTooltipOnScroll();
            });
        }
        if (isReviewMode) {
            observer = scrollView.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(() -> {
                int viewHeight = scrollView.getMeasuredHeight();
                int contentHeight = scrollView.getChildAt(0).getHeight();
                if (viewHeight - contentHeight < 0 && !dataLoaded) {
                    setVisibility();
                    dataLoaded = true;
                }
            });
            if (microLectureContentDto.getLectureContentType() == LectureContentType.MATCH_THE_COLUMN && !dto.isHideViewSolution()) {
                viewSolutionCard.setVisibility(View.VISIBLE);
            }
        }
        viewSolutionCard.setOnClickListener(view -> {
            optionListener.onViewSolutionClick();
            viewSolutionCard.setVisibility(View.GONE);
            scrollView.fullScroll(View.FOCUS_DOWN);
            scroll_bar.fullScroll(View.FOCUS_DOWN);
        });
    }

    private void setVisibility() {
        if (microLectureContentDto.getLectureContentType() != LectureContentType.MATCH_THE_COLUMN) {
            if (scrollView.canScrollVertically(1)) {
                if (StringUtils.isNotEmpty(dto.getUserAnswer()) && !dto.isHideViewSolution()) {
                    viewSolutionCard.setVisibility(View.VISIBLE);
                    viewSolutionText.setText(getString(R.string.View_Your_Answer));
                } else viewSolutionCard.setVisibility(View.GONE);
            } else {
                viewSolutionCard.setVisibility(View.GONE);
            }
        } else if (scroll_bar.canScrollVertically(1) && !dto.isHideViewSolution()) {
            viewSolutionCard.setVisibility(View.VISIBLE);
        } else {
            viewSolutionCard.setVisibility(View.GONE);
        }
    }

//    private static final String TAG = "HandleTypeEditText";

    private void handleTypeEditText() {
        typeAnswer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(TAG, "beforeTextChanged: text=" + charSequence + " start=" + i + " count=" + i1 + " after=" + i2);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                userAnswer = charSequence.toString();
                if (!userAnswer.isEmpty()
                        && (uploadImageLayoutContainer.getVisibility() == View.VISIBLE)) {
                    Log.d(TAG, "onTextChanged condition1: ");
                    uploadImageLayoutContainer.setVisibility(View.GONE);
                    typedAnswerWordCount.setVisibility(View.VISIBLE);
                    card.setVisibility(View.VISIBLE);
                    btnText.setText(getString(R.string.save_and_continue));
                }

                if (!isReviewMode && userAnswer.isEmpty()
                        && uploadImageLayoutContainer.getVisibility() == View.GONE
                        && StringUtils.isEmpty(dto.getUserAnswer())) {
                    Log.d("issueintextChange", "Showing uploadImageLayoutContainer, hiding typedAnswerWordCount & card (since input cleared)");
                    update2();
                }
                if (!userAnswer.isEmpty()) {
                    Log.d(TAG, "onTextChanged condition3: ");
                    int wordCount = userAnswer.trim().split("\\s+").length;
                    Log.d(TAG, "Counting words in typed input: " + wordCount);
                    handleTypeAnswerCount(wordCount);
                } else if (StringUtils.isNotEmpty(dto.getUserAnswer())) {
                    int wordCount = dto.getUserAnswer().trim().split("\\s+").length;
                    Log.d(TAG, "Counting words in saved dto answer: " + wordCount);
                    handleTypeAnswerCount(wordCount);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.d(TAG, "afterTextChanged: " + editable.toString());
            }
        });
    }


    private void handleTypeAnswerCount(int count) {

        long wordLimit = Long.parseLong(microLectureContentDto.getWordLimit());
        typedAnswerWordCount.setText(String.format(count + "/" + wordLimit + " "));
        typedAnswerWordCount.setVisibility(microLectureContentDto.isWordLimitApplicable() ? View.VISIBLE : View.GONE);
        if (StringUtils.isEmpty(dto.getUserAnswer())) {
            if (count > (wordLimit + wordLimitBuffer) && microLectureContentDto.isWordLimitApplicable()) {
                typeAnswerStatusText.setText(getString(R.string.word_limit_text));
                typeAnswerStatusText.setTextColor(getResources().getColor(R.color.red));
                typedAnswerWordCount.setTextColor(getResources().getColor(R.color.red));
                typeAnswerCard.setStrokeColor(getResources().getColor(R.color.red));
            } else {
                typeAnswerStatusText.setText(getString(R.string.Option_1_Apna_answer_yha_type_karein));
                typeAnswerStatusText.setTextColor(getResources().getColor(R.color.check_text_color));
                typedAnswerWordCount.setTextColor(getResources().getColor(R.color.subscription_blue));
                typeAnswerCard.setStrokeColor(getResources().getColor(R.color.text_card_stroke));
            }
        } else if (!CollectionUtils.isEmpty(list)) {
            typeAnswerStatusText.setText(getString(R.string.your_submitted_answer));
        }
    }

    private void setUserMatchTheseMarks() {
        int percent = 0;
        try {
            percent = (dto.getUserScore() * 100) / dto.getScore();
        } catch (Exception e) {
            Log.e("AnswerCheck", e.getMessage(), e);
        }
        greenMarksText.setText("+" + dto.getUserScore());
        if (percent <= 50) {
            greenMarksText.setTextColor(getResources().getColor(R.color.red));
            greenMarks.setCardBackgroundColor(getResources().getColor(R.color.mock_light_red));
        } else if (percent < 90) {
            greenMarksText.setTextColor(getResources().getColor(R.color.medium_marks_text));
            greenMarks.setCardBackgroundColor(getResources().getColor(R.color.medium_marks_card));
        } else {
            greenMarksText.setTextColor(getResources().getColor(R.color.green));
            greenMarks.setCardBackgroundColor(getResources().getColor(R.color.mock_light_green));
        }
    }

    private void openCaseStudy() {

        if (clickedFromUpdate) {
            runnable = () -> {
                int[] location = new int[2];
                caseStudyFab.getLocationOnScreen(location);
                caseStudyListener.performClickOnCaseStudy(dto.getCaseStudyGroup(),
                        dto.getComprehensiveCaseStudyImage(), dto.getComprehensiveCaseStudyText(),
                        location, caseStudyFab.getWidth(), caseStudyFab.getHeight());
                clickedFromUpdate = false;
            };
            handler.postDelayed(runnable, 1000);
        } else {
            int[] location = new int[2];
            caseStudyFab.getLocationOnScreen(location);
            caseStudyListener.openCaseStudyDialog(dto.getComprehensiveCaseStudyImage(),
                    dto.getComprehensiveCaseStudyText(), location, caseStudyFab.getWidth(), caseStudyFab.getHeight());
        }


    }

    private void performClickOnCaseStudy() {
        List<String> group = MyApplication.getCaseStudyGroupList(testSeriesId);
        if (CollectionUtils.isEmpty(group))
            group = new ArrayList<>();
        String key = testSeriesId + dto.getCaseStudyGroup();
        Log.d("LAFragmenet", "key check " + key);
        if (!group.contains(key)) {
            handler = new Handler();
            List<String> finalGroup = group;
            runnable = () -> {
                caseStudyFab.performClick();
                finalGroup.add(key);
                MyApplication.setCaseStudyGroupList(testSeriesId, finalGroup);
            };
            handler.postDelayed(runnable, 2000);
        }
    }


    private void dismissDialogWithAnimation() {
        View dialogView = caseStudyDialog.findViewById(R.id.case_study_layout);
        int[] location = new int[2];
        caseStudyFab.getLocationOnScreen(location);
        int centerX = location[0] + (caseStudyFab.getWidth() / 2);
        int centerY = location[1] + (caseStudyFab.getHeight() / 5);
        float initialRadius = (float) Math.hypot(dialogView.getWidth(), dialogView.getHeight());
        Animator hideAnimator = ViewAnimationUtils.createCircularReveal(dialogView, centerX, centerY, initialRadius, 0);
        hideAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        hideAnimator.setDuration(500);
        hideAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {
            }

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                caseStudyDialog.dismiss();
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {

            }
        });
    }

    private void clickSubmit(boolean saveAndMoveToNextFrag) {
        if (isReviewMode) {
            //seeExplanationDialog(dto.getAnswer());
            return;
        }

        if (microLectureContentDto.getLectureContentType() == LectureContentType.MATCH_THE_COLUMN) {
            isMatchTheseSubmittedMock = true;
            setUserMatchTheseAnswer();
            update(saveAndMoveToNextFrag);
        } else if (microLectureContentDto.getLectureContentType() == LectureContentType.LONG_ANSWERS ||
                microLectureContentDto.getLectureContentType() == LectureContentType.SHORT_ANSWERS ||
                microLectureContentDto.getLectureContentType() == LectureContentType.VERY_SHORT_ANSWERS && typeAnswerLayout.getVisibility() == View.GONE) {
            isTextExtracting = true;
            if (isTextExtractedFromImage) {
                if (extractedText.getText().toString().split("\\s+").length > Integer.parseInt(microLectureContentDto.getWordLimit()) && microLectureContentDto.isWordLimitApplicable()) {
                    Toast.makeText(getContext(), getResources().getString(R.string.word_limit_text), Toast.LENGTH_LONG).show();
                } else {
                    dto.setTypedAnswer(false);
                    dto.setUserAnswer(extractedText.getText().toString());
                    description.setText(getString(R.string.your_submitted_answer));
                    update(saveAndMoveToNextFrag);
                    questionEndTime = System.currentTimeMillis();
                    if (dto.getTimeTaken() == null) {
                        dto.setTimeTaken(questionEndTime - questionStartTime);
                    } else {
                        dto.setTimeTaken(dto.getTimeTaken() + (questionEndTime - questionStartTime));
                    }
                    //CommonMethods.showPositiveSnackbar(MarkLayout, getString(R.string.saved));
                }
            } else {
                card.setVisibility(View.GONE);
                editTextBack.setVisibility(View.VISIBLE);
                resetUploadTooltip();
                extractTextFromImage(v);
                questionEndTime = System.currentTimeMillis();
                if (dto.getTimeTaken() == null) {
                    dto.setTimeTaken(questionEndTime - questionStartTime);
                } else {
                    dto.setTimeTaken(dto.getTimeTaken() + (questionEndTime - questionStartTime));
                }
            }
        } else if (microLectureContentDto.getLectureContentType() == LectureContentType.VERY_SHORT_ANSWERS && typeAnswerLayout.getVisibility() == View.VISIBLE) {
            dto.setTypedAnswer(true);
            dto.setUserAnswer(typeAnswer.getText().toString());
            typeAnswerCard.setStrokeColor(getResources().getColor(R.color.subscription_blue));
            typeAnswerStatusText.setText(getString(R.string.your_submitted_answer));
            update(saveAndMoveToNextFrag);
            questionEndTime = System.currentTimeMillis();
            if (dto.getTimeTaken() == null) {
                dto.setTimeTaken(questionEndTime - questionStartTime);
            } else {
                dto.setTimeTaken(dto.getTimeTaken() + (questionEndTime - questionStartTime));
            }
            // CommonMethods.showPositiveSnackbar(MarkLayout, getString(R.string.saved));
        }
    }

    private void handleSetQuestionTitle() {
        if (dto.getQuestionType().equals(QuestionType.SHORT_ANSWERS.name())) {
            questionTypeTitle.setText(R.string.Short_Answers);
        } else if (dto.getQuestionType().equals(QuestionType.LONG_ANSWERS.name())) {
            questionTypeTitle.setText(R.string.Long_Answers);
        } else if (dto.getQuestionType().equals(QuestionType.MATCH_THE_COLUMN.name())) {
            questionTypeTitle.setText(R.string.Match_the_column);
        } else if (dto.getQuestionType().equals(QuestionType.VERY_SHORT_ANSWERS.name())) {
            questionTypeTitle.setText(R.string.Very_Short_Answers);
        }
    }

    public void setUserMatchTheseAnswerReview(String answer, String userAnswer) {
        String[] answerSplit = answer.split(",");
        String[] userAnswerSplit = userAnswer.split(",");

        for (int i = 0; i < answerSplit.length; i++) {
            if (answerSplit[i].trim().equalsIgnoreCase(userAnswerSplit[i].trim())) {
                list_right.get(i).setCorrectlyMatched(true);
                list_left.get(i).setCorrectlyMatched(true);
            } else {
                list_right.get(i).setCorrectlyMatched(false);
                list_left.get(i).setCorrectlyMatched(false);
            }
        }
        matchTheseAdapterLeft.notifyDataSetChanged();
        matchTheseAdapterRight.notifyDataSetChanged();
    }

    public void setUserMatchTheseAnswer() {
        myItemTouchHelper.setDraggable(false);
        userAnswerList.clear();
        String userAnswer = "";
        for (int i = 0; i < list_left.size(); i++) {
            userAnswerOption = list_left.get(i).getOption_number() + "-" + list_right.get(i).getOption_number();
            userAnswerList.add(userAnswerOption);
            if (userAnswer.equalsIgnoreCase("")) {
                userAnswer = userAnswerOption;
            } else {
                userAnswer = userAnswer + "," + userAnswerOption;
            }
        }
        dto.setUserAnswer(userAnswer);
        Log.d("UserAnswerMap", userAnswer);
    }

    private void setUserAnswerList(String answer, String userAnswer) {
        String[] result = userAnswer.split(",");
        userAnswerList = new ArrayList<>(Arrays.asList(result));
        list_left.clear();
        list_right.clear();
        for (int i = 0; i < userAnswerList.size(); i++) {
            try {
                String[] item = userAnswerList.get(i).split("-");
                int left = Integer.parseInt(item[0].trim());
                int right = Integer.parseInt(item[1].trim());
                Log.d("UserAnswerMap", "Option " + left + " : " + right);
                list_left.add(new MatchTheseLeftOption(i + 1, microLectureContentDto.getOptions().get(left - 1)));
                list_right.add(new MatchTheseRightOption(right, microLectureContentDto.getAnswers().get(right - 1)));
            } catch (Exception e) {
                Log.e("setUserAnswerList", "Error in parsing", e);
            }
        }
        setCorrectAnswerList(answer);
        if (isReviewMode) {
            try {
                setUserMatchTheseAnswerReview(answer, userAnswer);
            } catch (Exception e) {

            }
        }
    }

    private void setDefaultMatchTheseAnswer() {
        userAnswer = "1-1";
        for (int i = 1; i < list_left.size(); i++) {
            userAnswer = "," + userAnswer + (i + 1) + "-" + (i + 1);
        }
    }

    private void updareRecycerViewData() {
        options.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        ArrayList<OptionModel> model = new ArrayList<>();
        OptionModel op;
        for (String s : optionList) {
            op = new OptionModel(s, submited, integerAnswer, -1, quesType);
            model.add(op);
        }
        Animation scale = AnimationUtils.loadAnimation(getContext(), R.anim.scale);
        OptionQuestionAdapter adapter = new OptionQuestionAdapter(model, (OptionQuestionAdapter.OnOptionClick) LongAnswerQuestionFragment2.this, getContext());
        options.setAdapter(adapter);
        options.startAnimation(scale);
    }

    private void extractTextFromImage(View v) {
        Log.d("ExtractText", "extractTextFromImage called");
        textExtracted = false;
        handleBottomSheet(false);
        optionListener.stopTimer();
        Log.d("ExtractText", "Timer stopped");

        Map<String, Object> params = new HashMap<>();
        params.put("savType", dto.isSavType());
        Log.d("ExtractText", "Parameters set: savType=" + dto.isSavType());

        multipartRequest = new MyMultipartRequest(Request.Method.POST, CommonMethods.getUrlParam(EXTRACT_TEXT_FROM_IMAGE_URI, params),
                response -> {
                    Log.d("ExtractText", "Response received, parsing...");
                    try {
                        TypeToken<ImageTextExtractionResponse> token = new TypeToken<>() {
                        };
                        Gson gson = new Gson();
                        ImageTextExtractionResponse extractionResponse = gson.fromJson(new String(response.data, "UTF-8"), token.getType());
                        textExtracted = true;
                        optionListener.startTimer();
                        Log.d("ExtractText", "Text extraction status: " + extractionResponse.isTextExtracted());

                        if (!extractionResponse.isTextExtracted()) {
                            Log.d("ExtractText", "No text extracted from image. Clearing images and handling bottom sheet.");
                            imagesToUpload.clear();
                            handleBottomSheet(false);
                        } else {
                            Log.d("MULTIPARTRES", "Extraction message: " + extractionResponse.getMessage());
                            showExtractedTextView(v, extractionResponse);
                            startThreeStepTooltip();
                        }
                    } catch (Exception e) {
                        Log.e("ExtractText", "Error parsing response", e);
                    }

                },
                error -> {
                    Log.d("MULTIPARTRES", "Error response: " + error.toString());
                    if(!MyApplication.isNetworkAvailable()){
                      CommonMethods.showNegativeSnackbar(ParentLayout,getString(R.string.error_no_internet_connection));
                    }
                    card.setVisibility(View.VISIBLE);
                    optionListener.startTimer();
                    if (savBottomSheetDialog != null && isAdded()) {
                        Log.d("ExtractText", "Dismissing bottom sheet due to error");
                        savBottomSheetDialog.dismissAllowingStateLoss();
                    }
                }) {
            @Override
            protected Map<String, MultipartRequestDataPart> getByteData() {
                Log.d("ExtractText", "Preparing byte data for upload, total images: " + imagesToUpload.size());
                Map<String, MultipartRequestDataPart> params = new HashMap<>();
                for (int i = 0; i < imagesToUpload.size(); i++) {
                    File file = imagesToUpload.get(i);
                    try {
                        byte[] fileData = CommonMethods.readFileToByteArray(file);
                        String fileName = file.getName();
                        String contentType = CommonMethods.getMimeType(fileName);

                        Log.d("ExtractText", "Adding file: " + fileName + ", type: " + contentType);

                        if (i == 0) {
                            params.put("file", new MultipartRequestDataPart(fileName, fileData, contentType));
                        } else {
                            params.put("file" + i, new MultipartRequestDataPart(fileName, fileData, contentType));
                        }
                    } catch (IOException e) {
                        Log.e("ExtractText", "Error reading file: " + file.getName(), e);
                    }
                }

                return params;
            }
        };

        Log.d("ExtractText", "Adding request to Volley queue");
        Volley.newRequestQueue(getContext()).add(multipartRequest).setRetryPolicy(new DefaultRetryPolicy(
                600000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }


    private void showExtractedTextView(View v, ImageTextExtractionResponse extractionResponse) {
        handleEditText();
        uploaded_image_text.setVisibility(View.GONE);
        uploadedImageRecyclerView.setVisibility(View.GONE);
        extractedTextFromImageView.setVisibility(View.VISIBLE);
        extractedText.requestFocus();
        extractedText.setEnabled(true);
        extractedText.setMaxLines(maxLines);
        extractedText.setText(extractionResponse.getMessage());
        isTextExtracting = false;
        card.setVisibility(View.VISIBLE);
        if (isReviewMode) {
            button_answer_save_text.setText(getString(R.string.next));
        } else {
            btnText.setText(getString(R.string.save_and_continue));
        }
        if (savBottomSheetDialog != null) {
            savBottomSheetDialog.dismissAllowingStateLoss();
        }
        isTextExtractedFromImage = true;
        clickSubmit(false);
    }

    private void handleCount(int count) {
        Log.d(TAG, "handleCount: "+count);
        readText.setOnClickListener(v -> {
            if (!readMore) {
                TooltipHelper.getInstance().dismiss();
                description.setVisibility(View.GONE);
                relativeLayout.setVisibility(View.GONE);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                extractedTextFromImageView.setLayoutParams(params);
                extractedText.setMaxLines(Integer.MAX_VALUE);
                readText.setText(getString(R.string.Read_Less));
                readMore = true;
                if (uploadTooltipStatus != null && uploadTooltipStatus.getStepCount() == 2 &&
                        uploadTooltipStatus.getTotalSteps() == 3 && uploadTooltipStatus.isActive() &&
                        !uploadTooltipStatus.isFinished()) {
                    scrollView.fullScroll(View.FOCUS_DOWN);
                }

            } else {
                relativeLayout.setVisibility(View.VISIBLE);
                description.setVisibility(View.VISIBLE);
                setLayoutParamsExtractedView(RelativeLayout.BELOW, R.id.relative_layout_ques);
                dismissKeyboard(getActivity());
                extractedText.setMaxLines(maxLines);
                readText.setText(getString(R.string.Read_More));
                readMore = false;
                if (extractedText.getLineCount() > maxLines) {
                    readText.setVisibility(View.VISIBLE);
                } else if (!readMore) {
                    readText.setVisibility(View.GONE);
                }
            }
        });
        int wordLimit = 300;
        try {
            wordLimit = Integer.parseInt(microLectureContentDto.getWordLimit());
        } catch (Exception e) {
            Log.e("wordLimit", e.getMessage(), e);
        }

        countText.setText(String.format(count + "/" + wordLimit));
        countText.setVisibility(microLectureContentDto.isWordLimitApplicable() ? View.VISIBLE : View.GONE);
//        long wordLimitBuffer = MyApplication.getFirebaseRemoteConfig().getLong("word_limit_buffer");

//        if (StringUtils.isEmpty(dto.getUserAnswer())) {
        if (count > (wordLimit + wordLimitBuffer) && microLectureContentDto.isWordLimitApplicable()) {
            description.setText(getString(R.string.word_limit_text));
            description.setTextColor(getResources().getColor(R.color.red));
            countText.setTextColor(getResources().getColor(R.color.red));
            textCard.setStrokeColor(getResources().getColor(R.color.red));
            btnText.setEnabled(false);
        } else {
            description.setText(getString(R.string.submit_karne_se_pehle_answer_check_krein));
            description.setTextColor(getResources().getColor(R.color.check_text_color));
            countText.setTextColor(getResources().getColor(R.color.subscription_blue));
            textCard.setStrokeColor(getResources().getColor(R.color.text_card_stroke));
            btnText.setEnabled(true);
        }
//        } else {
//            description.setText(getString(R.string.your_submitted_answer));
//        }

        extractedText.post(() -> {
            if (!isReviewMode) {
                if (extractedText.getLineCount() > maxLines) {
                    readText.setVisibility(View.VISIBLE);
                } else if (!readMore) {
                    readText.setVisibility(View.GONE);
                }
            } else {
                extractedText.setMaxLines(Integer.MAX_VALUE);
                setLayoutParamsExtractedView(RelativeLayout.BELOW, R.id.solution_view);
            }
        });
    }

    private void handleEditText() {
        extractedText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged: "+count+"charcater----"+s);
                if (extractedText.isEnabled()) {
                    String text = s.toString().trim();
                    int wordCount = text.isEmpty() ? 0 : text.split("\\s+").length;
                    handleCount(wordCount);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setUpAnswerDetail() {
        try {
            if (isReviewMode) {
                reportQuestion.setVisibility(View.GONE);
                maxLines = Integer.MAX_VALUE;
                extractedText.setMaxLines(maxLines);
                extractedText.setEnabled(false);
                extractedText.clearFocus();
                description.setVisibility(View.GONE);
                descriptionCard.setVisibility(View.VISIBLE);
                editTextBack.setVisibility(View.GONE);
                textCardLayout.setBackgroundColor(getResources().getColor(R.color.text_background));
                solutionView.setVisibility(View.VISIBLE);
                if (StringUtils.isNotEmpty(dto.getUserAnswer())) {
                    extractedTextFromImageView.setVisibility(View.VISIBLE);
                    extractedText.setText(dto.getUserAnswer());
                    setLayoutParamsExtractedView(RelativeLayout.BELOW, R.id.solution_view);
                } else {
                    extractedTextFromImageView.setVisibility(View.GONE);
                }

                if (StringUtils.isNotEmpty(dto.getUserAnswer()) && StringUtils.isNotEmpty(microLectureContentDto.getAnswerEx())) {
                    analysisCard.setVisibility(View.VISIBLE);
                    analysisText.setText(HtmlCompat.fromHtml(microLectureContentDto.getAnswerEx(), HtmlCompat.FROM_HTML_MODE_LEGACY));
                } else {
                    analysisCard.setVisibility(View.GONE);
                }

                if (StringUtils.isNotEmpty(microLectureContentDto.getAnswer())) {
                    idealSolutionCard.setVisibility(View.VISIBLE);
                    solutionText.setText(HtmlCompat.fromHtml(microLectureContentDto.getAnswer(), HtmlCompat.FROM_HTML_MODE_LEGACY));
                } else {
                    idealSolutionCard.setVisibility(View.GONE);
                }

                if (StringUtils.isNotEmpty(dto.getDifficultyLevel())) {
                    difficultyCard.setVisibility(View.VISIBLE);
                    String difficulty = getString(R.string.level_);
                    if (dto.getDifficultyLevel().equals(QuestionLevel.EASY.name())) {
                        difficulty = difficulty + getString(R.string.easy);
                    } else if (dto.getDifficultyLevel().equals(QuestionLevel.MEDIUM.name())) {
                        difficulty = difficulty + getString(R.string.medium);
                    } else if (dto.getDifficultyLevel().equals(QuestionLevel.HARD.name())) {
                        difficulty = difficulty + getString(R.string.hard);
                    }
                    difficultyLevel.setText(difficulty);
                }
                typeAnswerLayout.setVisibility(View.GONE);

                marksCard.setVisibility(View.VISIBLE);
                if (StringUtils.isNotEmpty(dto.getUserAnswer())) {
                    marksText.setText(getString(R.string.marks) + ": " + CommonMethods.cleanDecimalNumber(dto.getPrecisionUserScore()) + "/" + dto.getScore());
                    double percent = 0.0;
                    try {
                        percent = (dto.getPrecisionUserScore() * 100) / dto.getScore();
                    } catch (Exception e) {
                        Log.e("AnswerCheck", e.getMessage(), e);
                    }
                    if (percent <= 50) {
                        marksText.setTextColor(getResources().getColor(R.color.red));
                        marksCard.setCardBackgroundColor(getResources().getColor(R.color.mock_light_red));
                    } else if (percent < 90) {
                        marksText.setTextColor(getResources().getColor(R.color.medium_marks_text));
                        marksCard.setCardBackgroundColor(getResources().getColor(R.color.medium_marks_card));
                    } else {
                        marksText.setTextColor(getResources().getColor(R.color.green));
                        marksCard.setCardBackgroundColor(getResources().getColor(R.color.mock_light_green));
                    }
                } else {
                    marksText.setText(getString(R.string.Not_Attempted));
                    marksText.setTextColor(getResources().getColor(R.color.grey_500));
                    marksCard.setCardBackgroundColor(getResources().getColor(R.color.grey_100));
                }

                if (StringUtils.isNotEmpty(dto.getExplanationImage())) {
                    idealSolutionCard.setVisibility(View.VISIBLE);
                    solutionImage.setVisibility(View.VISIBLE);
                    solutionImage.loadImage(dto.getExplanationImage());
                    Log.d("soultionIMage", "setUpAnswerDetail: " + dto.getExplanationImage());
                    solutionImage.setOnClickListener(vi -> {
                        fullScreenImageDialog = new FullScreenImageDialog(requireContext(),
                                dto.getExplanationImage(), FullViewImageType.SOLUTION);
                        fullScreenImageDialog.show();
                    });
                } else {
                    solutionImage.setVisibility(View.GONE);
                }
            }

        } catch (Exception e) {
            Log.d("setUpAnswerDetail", e.getMessage());
        }
    }


    private void openGallery() {
        Intent intent = new Intent(getActivity(), ImageCropperActivity.class);
        if (LectureContentType.valueOf(dto.getQuestionType()).equals(LectureContentType.LONG_ANSWERS)) {
            if (getActivity() != null)
                Toast.makeText(getActivity().getApplicationContext(),
                        String.format(getString(R.string.You_can_select_up_to_images),
                                3 - list.size()), Toast.LENGTH_LONG).show();
            intent.putExtra("allowedImages", 3);
            intent.putExtra("remainingImages", list.size());
        } else {
            if (getActivity() != null)
                Toast.makeText(getActivity().getApplicationContext(),
                        String.format(getString(R.string.You_can_select_up_to_images), 2),
                        Toast.LENGTH_LONG).show();
            intent.putExtra("allowedImages", 2);
        }
        galleryLauncher.launch(intent);
    }

    public void checkCameraPermission(String permission, int requestCode) {
        if (getContext() == null || getActivity() == null) {
            return;
        }
        if (checkSelfPermission(getContext(), permission) == PackageManager.PERMISSION_DENIED) {
            requestPermissionLauncher.launch(new String[]{permission});
        } else {
            captureImage();
        }
    }

    private void captureImage() {
        if (getContext() == null || getActivity() == null) {
            return;
        }
        String filename = "photo";
        File storageDirectory = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File imageFile = File.createTempFile(filename, ".jpg", storageDirectory);
            currentPhotoPath = imageFile.getAbsolutePath();

            imageuri = FileProvider.getUriForFile(getContext(),
                    BuildConfig.APPLICATION_ID + ".provider", imageFile);
            imageAdded = 1;

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri);
            startActivityForResult(intent, 19);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setSubjectiveLayout(View v) {
        list = new ArrayList<>();
        if (StringUtils.isNotEmpty(dto.getUserAnswer()) && !CollectionUtils.isEmpty(dto.getUriList())) {
            list.addAll(dto.uriList);
        }
        uploadedImageRecyclerAdapter = new UploadedImageRecyclerAdapter(getContext(), list, LongAnswerQuestionFragment2.this, microLectureContentDto.getLectureContentType());
        uploadedImageRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        uploadedImageRecyclerView.setAdapter(uploadedImageRecyclerAdapter);
        btnText.setText(getResources().getString(R.string.upload));
        image_pick_dialog = new Dialog(getContext());
        image_pick_dialog.setContentView(R.layout.action_popup_layout);
        image_pick_dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
        image_pick_dialog.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        image_pick_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        image_pick_dialog.setCancelable(false);

        LottieAnimationView lottieAnimationView = image_pick_dialog.findViewById(R.id.animation);
        lottieAnimationView.setVisibility(View.GONE);
        ImageView selectionImage = image_pick_dialog.findViewById(R.id.selection_image);
        selectionImage.setVisibility(View.VISIBLE);
        TextView title = image_pick_dialog.findViewById(R.id.title);
        title.setText(getString(R.string.select_images));
        TextView subtitle = image_pick_dialog.findViewById(R.id.subtitle);
        subtitle.setVisibility(View.GONE);
        CardView btn1 = image_pick_dialog.findViewById(R.id.btn1);
        CardView btn2 = image_pick_dialog.findViewById(R.id.btn2);
        TextView btnText1 = image_pick_dialog.findViewById(R.id.btn1_text);
        btnText1.setText(getString(R.string.choose_from_gallery));
        TextView btnText2 = image_pick_dialog.findViewById(R.id.btn2_text);
        btnText2.setText(getString(R.string.click_a_photo));

        image_pick_dialog.findViewById(R.id.close_btn).setOnClickListener(view -> image_pick_dialog.cancel());

        image_pick_dialog.findViewById(R.id.btn1).setOnClickListener(view -> {
            openGallery();
            image_pick_dialog.cancel();
        });
        image_pick_dialog.findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image_pick_dialog.cancel();
                checkCameraPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
                uploadedImageRecyclerAdapter = new UploadedImageRecyclerAdapter(getContext(), list, LongAnswerQuestionFragment2.this, microLectureContentDto.getLectureContentType());
                uploadedImageRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                uploadedImageRecyclerView.setAdapter(uploadedImageRecyclerAdapter);
            }
        });
        uploadSolutionBtn.setOnClickListener(view -> {
            image_pick_dialog.show();
        });
        uploadImageLayout.setOnClickListener(View->{
            image_pick_dialog.show();
        });

        if (isReviewMode) {
            if (StringUtils.isNotEmpty(dto.getComprehensiveCaseStudyText()) || StringUtils.isNotEmpty(dto.getComprehensiveCaseStudyImage())) {
                caseStudyFab.setVisibility(View.VISIBLE);
                caseStudyCard.setCardBackgroundColor(requireContext().getColor(R.color.mock_light_blue));
                caseStudyText.setTextColor(requireContext().getColor(R.color.subscription_blue));
            }
        }

        editTextBack.setOnClickListener(view -> {
            textExtracted = false;
            TooltipHelper.getInstance().dismiss();
            handleBottomSheet(false);
            uploaded_image_text.setVisibility(View.VISIBLE);
            uploadedImageRecyclerView.setVisibility(View.VISIBLE);
            imageExtractionLayout.setVisibility(View.VISIBLE);
            if (savBottomSheetDialog != null) {
                savBottomSheetDialog.dismissAllowingStateLoss();
            }
            isTextExtractedFromImage = false;
            relativeLayout.setVisibility(View.VISIBLE);
            setLayoutParamsExtractedView(RelativeLayout.BELOW, R.id.relative_layout_ques);
            extractedTextFromImageView.setVisibility(View.GONE);
            card.setVisibility(View.VISIBLE);
            card.setClickable(true);
            btnText.setText(getResources().getString(R.string.upload));
            readMore = false;
            readText.setText(getString(R.string.Read_More));
        });

        if (microLectureContentDto.getLectureContentType() == LectureContentType.LONG_ANSWERS ||
                microLectureContentDto.getLectureContentType() == LectureContentType.SHORT_ANSWERS ||
                microLectureContentDto.getLectureContentType() == LectureContentType.VERY_SHORT_ANSWERS) {
            if (StringUtils.isNotEmpty(dto.getUserAnswer())) {
                uploadImageLayoutContainer.setVisibility(View.GONE);
                if (dto.isTypedAnswer()) {
                    typeAnswerLayout.setVisibility(View.VISIBLE);
                    typeAnswer.setText(dto.getUserAnswer());
                    typeAnswerCard.setStrokeColor(getResources().getColor(R.color.subscription_blue));
                } else {
                    typeAnswerLayout.setVisibility(View.GONE);
                    ImageTextExtractionResponse imageTextExtractionResponse = new ImageTextExtractionResponse();
                    imageTextExtractionResponse.setMessage(dto.getUserAnswer());
                    showExtractedTextView(v, imageTextExtractionResponse);
                }

            } else if (isReviewMode) {
                uploadImageLayoutContainer.setVisibility(View.GONE);
                typeAnswerLayout.setVisibility(View.GONE);
                ImageTextExtractionResponse imageTextExtractionResponse = new ImageTextExtractionResponse();
                imageTextExtractionResponse.setMessage(" ");
                showExtractedTextView(v, imageTextExtractionResponse);

            }
        }

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK) {

                        if (result.getData() != null) {

                            Log.d("handleImagePickerDialog", result.getData().getStringArrayListExtra("url_list").toString());
                            ArrayList<String> urls = result.getData().getStringArrayListExtra("url_list");
                            if (urls.size() > 0) {
                                typeAnswerLayout.setVisibility(View.GONE);
                                uploadImageLayoutContainer.setVisibility(View.GONE);
                                imageExtractionLayout.setVisibility(View.VISIBLE);
                            }

                            for (int i = 0; i < urls.size(); i++) {
                                Uri photoUri = Uri.parse(urls.get(i));
                                Log.d("photoUri", "gallery = " + photoUri);
                                if (photoUri != null) {
                                    float lengthmb = getImageSize(photoUri);

                                    File imgFile = null;
                                    try {
                                        imgFile = FileUtil.from(getContext(), photoUri);
                                    } catch (IOException e) {
                                        Log.e("Error in subscription", e.getMessage(), e);
                                    }

                                    if (lengthmb <= 15) {
                                        if (microLectureContentDto.getLectureContentType().equals(LectureContentType.LONG_ANSWERS)
                                                && list.size() < 3) {
                                            // Allow up to 3 images for LONG answers
                                            list.add(photoUri);
                                            imagesToUpload.add(imgFile);
                                            if (uploadedImageRecyclerAdapter != null) {
                                                uploadedImageRecyclerAdapter.notifyDataSetChanged();
                                            }

                                        } else if (microLectureContentDto.getLectureContentType().equals(LectureContentType.SHORT_ANSWERS) ||
                                                microLectureContentDto.getLectureContentType().equals(LectureContentType.VERY_SHORT_ANSWERS)
                                                && list.size() < 2) {
                                            // Allow up to 2 images for SHORT answers
                                            list.add(photoUri);
                                            imagesToUpload.add(imgFile);
                                            if (uploadedImageRecyclerAdapter != null) {
                                                uploadedImageRecyclerAdapter.notifyDataSetChanged();
                                            }

                                        } else if (list.isEmpty()) {
                                            // For other types → only 1 image allowed
                                            list.add(photoUri);
                                            imagesToUpload.add(imgFile);
                                            if (uploadedImageRecyclerAdapter != null) {
                                                uploadedImageRecyclerAdapter.notifyDataSetChanged();
                                            }
                                            showSubmitTooltip();

                                        } else {
                                            // Show max selection error messages
                                            if (microLectureContentDto.getLectureContentType().equals(LectureContentType.LONG_ANSWERS)) {
                                                Toast.makeText(getContext(), "Maximum selection limit is 3", Toast.LENGTH_SHORT).show();
                                            } else if (microLectureContentDto.getLectureContentType().equals(LectureContentType.SHORT_ANSWERS)||
                                                    microLectureContentDto.getLectureContentType().equals(LectureContentType.VERY_SHORT_ANSWERS)){
                                                Toast.makeText(getContext(), "Maximum selection limit is 2", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getContext(), "Maximum selection limit is 1", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                    else
                                        Toast.makeText(getContext(), "size larger than 15mb", Toast.LENGTH_LONG).show();

                                    if (list.size() > 0) {
                                        uploaded_image_text.setVisibility(View.VISIBLE);
                                        uploadedImageRecyclerView.setVisibility(View.VISIBLE);
                                        card.setVisibility(View.VISIBLE);
                                        btnText.setText(R.string.upload);
                                    }
                                }
                            }
                        }
                    }
                }
        );
    }

    public void dismissKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    void setCorrectAnswerList(String answer) {
        String[] result = answer.split(",");
        correctAnswerList = new ArrayList<>(Arrays.asList(result));
    }

    private void setMatchThese(View v) {
        isMatchTheseSubmittedMock = false;
        correctAnswerList = new ArrayList<>();
        userAnswerList = new ArrayList<>();

        list_left = new ArrayList<>();
        list_right = new ArrayList<>();
        optionMt = v.findViewById(R.id.option_mt);
        matchTheseAdapterLeft = new MatchTheseAdapterLeft(list_left, getContext());
        left_recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        left_recyclerView.setAdapter(matchTheseAdapterLeft);
        if (microLectureContentDto.getLectureContentType() == LectureContentType.MATCH_THE_COLUMN) {
            scrollView.setPadding(0, 0, 0, 0);
            card.setVisibility(View.VISIBLE);
            matchTheseAdapterRight = new MatchTheseAdapterRight(list_right, getContext());
            right_recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            right_recyclerView.setAdapter(matchTheseAdapterRight);
            myItemTouchHelper = new MyItemTouchHelper(matchTheseAdapterRight);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(myItemTouchHelper);

            if (StringUtils.isNotEmpty(dto.getUserAnswer())) {
                isMatchTheseSubmittedMock = true;
                setUserAnswerList(dto.getAnswer(), dto.getUserAnswer());
            } else {
                if (isReviewMode) {
                    setUserAnswerList(dto.getAnswer(), dto.getAnswer());
                } else {
                    setLeftList(microLectureContentDto.getOptions());
                    setRightList(microLectureContentDto.getAnswers());
                }
            }


            calculateHeight(v);
            setCorrectAnswerList(microLectureContentDto.getAnswer());
            matchTheseAdapterRight.setTouchHelper(itemTouchHelper);
            itemTouchHelper.attachToRecyclerView(right_recyclerView);
            if (isReviewMode) {
                myItemTouchHelper.setDraggable(false);
            }
        }
    }

    public int getHeightOfLargestDescription(TextView textView) {
        final WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        int deviceWidth;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
            display.getSize(size);
            deviceWidth = size.x;
        } else {
            deviceWidth = display.getWidth();
        }

        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        textView.measure(widthMeasureSpec, heightMeasureSpec);
        return textView.getMeasuredHeight();
    }

    private void calculateHeight(View v) {
        v.findViewById(R.id.description).setVisibility(View.VISIBLE);
        v.findViewById(R.id.column_layout).setVisibility(View.VISIBLE);
        LinearLayout layout = v.findViewById(R.id.fakeLayout);
        layout.setVisibility(View.VISIBLE);
        optionMt.setText(maxLengthText);
        optionMt.measure(0, 0);
        optionMt.getMeasuredWidth();
        getHeightOfLargestDescription(optionMt);
        int measuredHeight = optionMt.getMeasuredHeight();
        matchTheseAdapterRight.setHeight((int) (measuredHeight * 2.5));
        matchTheseAdapterLeft.setHeight((int) (measuredHeight * 2.5));
        if (matchTheseAdapterLeft != null) {
            matchTheseAdapterLeft.notifyDataSetChanged();
        }
        if (matchTheseAdapterRight != null) {
            matchTheseAdapterRight.notifyDataSetChanged();
        }
        layout.setVisibility(View.GONE);
        optionMt.setVisibility(View.GONE);
    }

    void setLeftList(List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            list_left.add(new MatchTheseLeftOption(i + 1, list.get(i)));
            if (maxLengthText.length() < list.get(i).length()) {
                maxLengthText = list.get(i);
            }
        }

    }

    void setRightList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            list_right.add(new MatchTheseRightOption(i + 1, list.get(i)));
            if (maxLengthText.length() < list.get(i).length()) {
                maxLengthText = list.get(i);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d("microLectureContentDto", "WORD LIMIT" + microLectureContentDto.getWordLimit());
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 118 && resultCode == Activity.RESULT_OK) {
//            if (data != null) {
//                imageuri = data.getData();
//            }
//                CropImage.activity(imageuri)
//                        .start(getContext());
            card.setVisibility(View.VISIBLE);
            uploadImageLayoutContainer.setVisibility(View.GONE);
            typeAnswerLayout.setVisibility(View.GONE);
            if (data != null && data.getClipData() != null) {

                ClipData clipData = data.getClipData();


                for (int i = 0; i < clipData.getItemCount(); i++) {

                    Uri imgUri = clipData.getItemAt(i).getUri();
                    Log.d("IMAGEURI", "Image uri forloop :: " + imgUri);

                    float lengthmb = getImageSize(imgUri);
                    File imgFile = null;
                    try {
                        imgFile = FileUtil.from(getContext(), imgUri);
                    } catch (IOException e) {
                        Log.e("Error in subscription", e.getMessage(), e);
                    }

                    if (lengthmb <= 15) {
                        if (microLectureContentDto.getLectureContentType().equals(LectureContentType.SHORT_ANSWERS)
                                && list.size() < 2) {
                            Log.d("IMAGEURI", "SHORT ANSER" + list.size());
                            list.add(imgUri);
                            imagesToUpload.add(imgFile);
                            if (uploadedImageRecyclerAdapter != null) {
                                uploadedImageRecyclerAdapter.notifyDataSetChanged();
                            }
                        } else if (microLectureContentDto.getLectureContentType().equals(LectureContentType.LONG_ANSWERS) && list.size() < 3) {
                            list.add(imgUri);
                            imagesToUpload.add(imgFile);
                            if (uploadedImageRecyclerAdapter != null) {
                                uploadedImageRecyclerAdapter.notifyDataSetChanged();
                            }
                        } else {
                            if (microLectureContentDto.getLectureContentType().equals(LectureContentType.LONG_ANSWERS)) {
                                Toast.makeText(getContext(), "Maximum selection limit is 3", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Maximum selection limit is 2", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else
                        Toast.makeText(getContext(), "size larger than 15mb", Toast.LENGTH_LONG).show();

                    if (list.size() > 0) {
                        uploaded_image_text.setVisibility(View.VISIBLE);
                        uploadedImageRecyclerView.setVisibility(View.VISIBLE);
                        card.setVisibility(View.VISIBLE);
                        btnText.setText(R.string.upload);
                    }
                }
            } else if (data != null) {
                Uri imageUri = data.getData();
                Log.d("IMAGEURI", "Image uri:: " + imageUri);


                File file = null;
                try {
                    file = FileUtil.from(getContext(), imageUri);
                } catch (IOException e) {
                    Log.e("Error ", e.getMessage(), e);
                }

                float lengthmb = (float) (file.length() / 1024) / 1024;

                Log.d("IMAGEURI", "Image size d:: " + file.length());

                if (lengthmb <= 15) {
                    list.add(imageUri);
                    imagesToUpload.add(file);
                    if (uploadedImageRecyclerAdapter != null) {
                        uploadedImageRecyclerAdapter.notifyDataSetChanged();
                    }
                } else
                    Toast.makeText(getContext(), "size larger than 15mb", Toast.LENGTH_LONG).show();

                if (list.size() > 0) {
                    uploaded_image_text.setVisibility(View.VISIBLE);
                    uploadedImageRecyclerView.setVisibility(View.VISIBLE);
                    card.setVisibility(View.VISIBLE);
                    btnText.setText(R.string.upload);
                    if (savBottomSheetDialog != null) {
                        savBottomSheetDialog.dismissAllowingStateLoss();
                    }
                }
            }


        }


        try {
            if (requestCode == 19 && resultCode == RESULT_OK && imageuri != null) {
                Intent cropIntent = new Intent(requireActivity(), CropImageActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(CropImage.CROP_IMAGE_EXTRA_SOURCE, imageuri);
                CropImageOptions cropImageOptions = new CropImageOptions();
                cropImageOptions.guidelines = CropImageView.Guidelines.ON_TOUCH;
                bundle.putParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS, cropImageOptions);
                cropIntent.putExtra(CropImage.CROP_IMAGE_EXTRA_BUNDLE, bundle);
                startActivityForResult(cropIntent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        } catch (Exception e) {
            Log.d("onActivityResult:", e.getMessage());
        }

        Log.d("CROPPINGINDEX :: ", "RESULTCODE: " + resultCode);


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            card.setVisibility(View.VISIBLE);
            uploadImageLayoutContainer.setVisibility(View.GONE);
            typeAnswerLayout.setVisibility(View.GONE);
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            Log.d("CROPPINGINDEX :: ", "INDEX ON OK: ");
            uploadImageLayoutContainer.setVisibility(View.GONE);
            try {
                if (result == null) {
                    Log.d("CROPPINGINDEX :: ", "NULL RESULT: ");
                    return;
                }
                uri = result.getUri();
                File file = new File(uri.getPath());
                float lengthmb = (float) (file.length() / 1024) / 1024;

                if (lengthmb <= 15) {
                    Log.d("IMAGEURI", "FILE URI CROP :: " + uri);
                    if (croppingImageIndex != -1) {
                        list.remove(croppingImageIndex);
                        list.add(croppingImageIndex, uri);
                        imagesToUpload.remove(croppingImageIndex);
                        imagesToUpload.add(croppingImageIndex, file);
                    } else {
                        list.add(uri);
                        imagesToUpload.add(file);
                    }
                    showSubmitTooltip();
                    Log.d("CROPPINGINDEX :: ", "INDEX ON CROPRESULT: " + list.toString());

                    if (uploadedImageRecyclerAdapter != null) {
                        uploadedImageRecyclerAdapter.notifyDataSetChanged();
                    }
                } else
                    Toast.makeText(getContext(), "size larger than 15mb", Toast.LENGTH_LONG).show();

                if (list.size() > 0) {
                    uploaded_image_text.setVisibility(View.VISIBLE);
                    uploadedImageRecyclerView.setVisibility(View.VISIBLE);
                    card.setVisibility(View.VISIBLE);
                    btnText.setText(R.string.upload);
                }

            } catch (Exception error) {
                Log.e("resultUri", error.getMessage(), error);
            }
        }

    }

    private float getImageSize(Uri imageUri) {
        if (getContext() == null) {
            return 0.0f;
        }
        String[] projection = {MediaStore.Images.Media.SIZE};
        Cursor cursor = getContext().getContentResolver().query(imageUri, projection, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int columnIndexSize = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
            long fileSizeInBytes = cursor.getLong(columnIndexSize);
            cursor.close();
            return (float) (fileSizeInBytes / 1024) / 1024;
        } else {
            return 0.0f;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureImage();
            }
        }
    }


    private void update2() {
        extractedText.setEnabled(true);
        dto.setAnswerState(AnswerState.UNANSWERED);
        changeAnswerState(dto.isMarked());
        handleBottomSheet(false);
        imageExtractionLayout.setVisibility(View.VISIBLE);
        extractedTextFromImageView.setVisibility(View.GONE);
        if (savBottomSheetDialog != null) {
            savBottomSheetDialog.dismissAllowingStateLoss();
        }
        isTextExtractedFromImage = false;
        if (StringUtils.isEmpty(dto.getUserAnswer())) {
            card.setVisibility(View.GONE);
            clearAnswer.setVisibility(View.INVISIBLE);
        }
        btnText.setText(getResources().getString(R.string.upload));

        imagesToUpload.clear();
        list.clear();

        uploadImageLayoutContainer.setVisibility(View.VISIBLE);
        if (microLectureContentDto.getLectureContentType().equals(LectureContentType.VERY_SHORT_ANSWERS)) {
            uploadImageStepGuide.setText(getString(R.string.Option_2_Ya_photo_click_krke_upload_karein));
            typeAnswerLayout.setVisibility(View.VISIBLE);
            typeAnswerCard.setStrokeColor(getResources().getColor(R.color.text_card_stroke));
            typeAnswerStatusText.setText(getString(R.string.Option_1_Apna_answer_yha_type_karein));
            typeAnswer.setEnabled(true);
            typeAnswer.setText("");
            typedAnswerWordCount.setVisibility(View.GONE);
        }

    }

    private void setLayoutParamsExtractedView(int below, int relativeLayout) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(below, relativeLayout);
        extractedTextFromImageView.setLayoutParams(params);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
        extractedText.clearFocus();
        setViewPagerDataForCrashlytics();
        if (!isReviewMode && caseStudyFab != null && caseStudyFab.getVisibility() == View.VISIBLE)
            performClickOnCaseStudy();
        if (isReviewMode && dto.isHideViewSolution())
            viewSolutionCard.setVisibility(View.GONE);
        if (textExtracted && savBottomSheetDialog != null) {
            savBottomSheetDialog.dismissAllowingStateLoss();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Log.d("LAFragmenet", "onPause " + dto.getQuestionId() + " currentItem = " + optionListener.getSelected() + " Frag Number= " + dto.getFragNumber());

        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }

        if (caseStudyHandler != null && caseStudyRunnable != null) {
            caseStudyHandler.removeCallbacks(caseStudyRunnable);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (savBottomSheetDialog != null) {
            savBottomSheetDialog.dismissAllowingStateLoss();
        }
    }

    private void setViewPagerDataForCrashlytics() {
        try {
            FirebaseCrashlytics crashlytics = MyApplication.getFirebaseInstance();

            crashlytics.setCustomKey("fragNumber", "" + fragNumber);
            if (dto == null) {
                MyApplication.getFirebaseInstance().log("DTO is null");
            } else {
                crashlytics.setCustomKey("QuestionId", "" + dto.getQuestionId());
            }
        } catch (Exception e) {
            Log.d("Error", "Error");
        }

    }

    private void changeAnswerState(boolean marked) {
        if (marked) {
            switch (dto.getAnswerState()) {
                case ANSWERED:
                    dto.setAnswerState(AnswerState.ANSWERED_MARK_FOR_REVIEW);
                    break;
                case UNANSWERED:
                    dto.setAnswerState(AnswerState.UNANSWERED_MARK_FOR_REVIEW);
                    break;

            }
        } else {
            switch (dto.getAnswerState()) {
                case ANSWERED:
                    dto.setAnswerState(AnswerState.ANSWERED);
                    break;
                case UNANSWERED:
                    dto.setAnswerState(AnswerState.UNANSWERED);
                    break;
                case UNANSWERED_MARK_FOR_REVIEW:
                    dto.setAnswerState(AnswerState.UNANSWERED);
                    break;
                case ANSWERED_MARK_FOR_REVIEW:
                    dto.setAnswerState(AnswerState.ANSWERED);
                    break;
            }
        }
    }

//    @Override
//    public void setSelected(int i, String answer) {
//    }

//    @Override
//    public TestSeriesQuestionDto getDto(int fragNumber) {
//        return null;
//    }

//    @Override
//    public void submitAnswer() {
//
//    }


    private void changeMark() {
        if (dto.isMarked()) {
            markButton.setChecked(true);
            mark.setText(getResources().getString(R.string.marked));
        } else {
            markButton.setChecked(false);
            mark.setText(getResources().getString(R.string.unmarked));
        }

        changeAnswerState(dto.isMarked());
    }

    @Override
    public void addClicked() {
        isSingleImageSelection = true;
        image_pick_dialog.show();
    }

    @Override
    public void deleteClicked(int position) {
        if (!CollectionUtils.isEmpty(imagesToUpload))
            imagesToUpload.remove(position);
        list.remove(position);
        if (list.size() == 0) {
            uploaded_image_text.setVisibility(View.GONE);
            isSingleImageSelection = false;
            update2();
        }
        if (uploadedImageRecyclerAdapter != null) {
            uploadedImageRecyclerAdapter.notifyDataSetChanged();
        }
        if (list.size() == 0) {
            uploadImageLayoutContainer.setVisibility(View.VISIBLE);
            clearAnswer.setVisibility(View.GONE);
            if (savBottomSheetDialog != null) {
                savBottomSheetDialog.dismissAllowingStateLoss();
            }
            btnText.setText(getResources().getString(R.string.upload));
            if (microLectureContentDto.getLectureContentType() == LectureContentType.VERY_SHORT_ANSWERS)
                typeAnswerLayout.setVisibility(View.VISIBLE);

        }
    }

    @Override
    public void addCropClicked(int position) {
        Log.d("CROPPINGINDEX :: ", "INDEX : " + position);
        croppingImageIndex = position;
        CropImage.activity(list.get(position)).start(getContext(), this);
    }


    private void seeExplanationDialog(String explanation) {
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.custom_dialogu_explanation);
        dialog.getWindow().setBackgroundDrawable(getContext().getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);

        TextView exp = dialog.findViewById(R.id.explanation);
        CardView cancel = dialog.findViewById(R.id.cancel);
        ImageView image = dialog.findViewById(R.id.image_explanation);
        CardView revise = dialog.findViewById(R.id.revise_concept);
        ImageView copy = dialog.findViewById(R.id.copy);

        if (StringUtils.isEmpty(microLectureContentDto.getAnswerVideo())) {
            revise.setVisibility(View.GONE);
        }
        String url = dto.getExplanationImage();
        Log.i("EXPLANATIONIMAGE", " " + url);

        if (url != null && url.length() > 0) {
            image.setVisibility(View.VISIBLE);
            Glide.with(getActivity()).load(url).into(image);
        }

        image.setOnClickListener(view12 -> {
            Intent intent = new Intent(getContext(), ZoomImageActivity.class);
            intent.putExtra("Url", url);
            startActivity(intent);
        });

        exp.setText(explanation);

        copy.setOnClickListener(view1 -> {
            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", explanation);
            clipboard.setPrimaryClip(clip);
            CommonMethods.showPositiveSnackbar(ParentLayout, getString(R.string.ExplanationCopied) + "!");
        });

        cancel.setOnClickListener(view13 -> dialog.dismiss());

        revise.setOnClickListener(v -> {

            microLectureContentDto.setVideoUrl(microLectureContentDto.getAnswerVideo());
            @OptIn(markerClass = UnstableApi.class) Intent intent =
                    intent = new Intent(getContext(),
                            VideoPlayerActivity.class);

            intent.putExtra(getResources().getString(R.string.passed_obj), microLectureContentDto);
            intent.putExtra("shouldNotResume", true);
            intent.putExtra(getString(R.string.back_to_q), true);
            //mLaunch.launch(intent);
        });

        dialog.show();
    }

    public void updateView(int position, boolean last) {
        if (isReviewMode && last) {
            card.setVisibility(View.GONE);
        }

        if (StringUtils.isNotEmpty(dto.getUserAnswer())) {
            if (!Type.equals(PreferenceKeys.RESPONSE.getKey())) {
                clearAnswer.setVisibility(View.VISIBLE);
                btnText.setText(getString(R.string.save_and_continue));
            }
        }

        Log.d("LAFragmenet", "updateView " + dto.getQuestionId() + " currentItem = " + optionListener.getSelected() + " Frag Number= " + position);

        if (!isReviewMode && caseStudyFab != null && caseStudyFab.getVisibility() == View.VISIBLE) {
            clickedFromUpdate = true;
            caseStudyFab.performClick();
        }
        if (!isReviewMode && microLectureContentDto.getLectureContentType()
                == LectureContentType.VERY_SHORT_ANSWERS
                && StringUtils.isEmpty(dto.getUserAnswer())) {
            Log.d(TAG, "condition: subjective answer detail set");
            uploadImageStepGuide.setText(getString(R.string.Option_2_Ya_photo_click_krke_upload_karein));
            typeAnswerLayout.setVisibility(View.VISIBLE);
            typeAnswerCard.setStrokeColor(getResources().getColor(R.color.text_card_stroke));
            typeAnswerStatusText.setText(getString(R.string.Option_1_Apna_answer_yha_type_karein));
            typeAnswer.setEnabled(true);
            typeAnswer.setText("");
            typedAnswerWordCount.setVisibility(View.GONE);
        }


        handleTooltipOnResume();
    }

    private void update(boolean saveAndMoveToNextFrag) {
        dto.setUriList(list);
        extractedText.clearFocus();
        dto.setAnswerState(AnswerState.ANSWERED);
        if (!Type.equals(PreferenceKeys.RESPONSE.getKey())) {
            clearAnswer.setVisibility(View.VISIBLE);
            btnText.setText(getString(R.string.save_and_continue));
        }
        changeAnswerState(dto.isMarked());
        finishUploadTooltip();
        if (saveAndMoveToNextFrag) {
            optionListener.nextPage(fragNumber);
        }
    }

    private void handleBottomSheet(boolean error) {
        if (savBottomSheetDialog != null)
            savBottomSheetDialog.dismissAllowingStateLoss();
        savBottomSheetDialog = new SavBottomSheetDialog(this);
        Bundle bundle = new Bundle();
        bundle.putBoolean("error", error);
        bundle.putBoolean("textExtracted", textExtracted);
        bundle.putString("finalText", "");
        savBottomSheetDialog.setArguments(bundle);
        savBottomSheetDialog.show(getParentFragmentManager(), "");
        savBottomSheetDialog.setCancelable(false);
        optionListener.onBottomSheetOpen();
    }
    @Override
    public void onDismiss() {
        optionListener.onBottomSheetClose();
    }

    private void setMatchTheseAnswerLayout() {
        divider.setVisibility(View.VISIBLE);
        v.findViewById(R.id.description).setVisibility(View.GONE);
        solutionLayout.setVisibility(View.VISIBLE);
        if (StringUtils.isNotEmpty(dto.getDifficultyLevel())) {
            difficultyCard.setVisibility(View.VISIBLE);
            if (dto.getDifficultyLevel().equals(QuestionLevel.EASY.name())) {
                difficultyType.setText(getString(R.string.easy));
            } else if (dto.getDifficultyLevel().equals(QuestionLevel.MEDIUM.name())) {
                difficultyType.setText(getString(R.string.medium));
            } else if (dto.getDifficultyLevel().equals(QuestionLevel.HARD.name())) {
                difficultyType.setText(getString(R.string.hard));
            }
        }
        if (dto.getVideoUrl() != null) {
            solutionImageCard.setVisibility(View.VISIBLE);
            solutionImageCard.setOnClickListener(vi -> {
                Intent intent = new Intent(getActivity(), ExoPlayerActivity.class);
                intent.putExtra("video_uri", dto.getVideoUrl());
                intent.putExtra(PreferenceKeys.VIDEO_STARTING.getKey(), dto.getVideoStartingTime());
                intent.putExtra("video_ending", dto.getVideoEndingTime());
                startActivity(intent);
            });
        }
        if (StringUtils.isNotEmpty(dto.getExplanation())) {
            solutionTextMatchThese.setText(HtmlCompat.fromHtml(dto.getExplanation().trim(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        }
        if (dto.getExplanationImage() != null) {
            solutionImageMatchThese.setVisibility(View.VISIBLE);
            solutionImage.loadImage(dto.getExplanationImage());
            solutionImageMatchThese.setOnClickListener(vi -> {
                fullScreenImageDialog = new FullScreenImageDialog(requireContext(),
                        dto.getExplanationImage(), FullViewImageType.SOLUTION);
                fullScreenImageDialog.show();
            });
        }
    }


    @Override
    public void onGalleryClick(boolean textNotFound) {
        Log.d("issue", "onGallery: " + textNotFound);
        textExtracted = false;
        if (getContext() != null) {
            if (savBottomSheetDialog != null) {
                savBottomSheetDialog.dismissAllowingStateLoss();
            }
        }
        if (textNotFound) {
            list.clear();
            uploadedImageRecyclerAdapter.notifyDataSetChanged();
            uploadImageLayoutContainer.setVisibility(View.VISIBLE);
            uploadedImageRecyclerView.setVisibility(View.GONE);
            uploaded_image_text.setVisibility(View.GONE);
        }
        openGallery();
    }

    @Override
    public void onPictureClick(boolean textNotFound) {
        Log.d("issue", "onPictureClick: " + textNotFound);
        textExtracted = false;
        if (getContext() != null) {
            if (savBottomSheetDialog != null) {
                savBottomSheetDialog.dismissAllowingStateLoss();
            }
        }
        if (textNotFound) {
            list.clear();
            uploadedImageRecyclerAdapter.notifyDataSetChanged();
            ;
            uploadImageLayoutContainer.setVisibility(View.VISIBLE);
            uploadedImageRecyclerView.setVisibility(View.GONE);
            uploaded_image_text.setVisibility(View.GONE);
        }
        checkCameraPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
    }

    @Override
    public void onCloseClick() {
        if (!textExtracted) {
            setUpActionPopup();
        } else {
            if (getContext() != null && savBottomSheetDialog != null) {
                savBottomSheetDialog.dismissAllowingStateLoss();
            }
            cancelRequest();
        }
        optionListener.onBottomSheetClose();
    }

    private void setUpActionPopup() {
        Dialog actionDialog = new Dialog(getContext());
        actionDialog.setContentView(R.layout.action_popup_layout);
        actionDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
        actionDialog.getWindow().setBackgroundDrawable(getContext().getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
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
        animationView.setAnimation(R.raw.alert);
        close.setOnClickListener(v -> actionDialog.dismiss());
        btn1.setOnClickListener(v -> {
            actionDialog.dismiss();
            cancelRequest();
            list.clear();
            dto.setUriList(new ArrayList<>());
            uploadImageLayoutContainer.setVisibility(View.VISIBLE);
            if (savBottomSheetDialog != null) {
                savBottomSheetDialog.dismissAllowingStateLoss();
            }
            card.setVisibility(View.GONE);
            if (microLectureContentDto.getLectureContentType() == LectureContentType.VERY_SHORT_ANSWERS)
                typeAnswerLayout.setVisibility(View.VISIBLE);
            if (uploadedImageRecyclerAdapter != null) {
                uploadedImageRecyclerAdapter.notifyDataSetChanged();
            }
            optionListener.startTimer();
        });
        btn2.setOnClickListener(v -> actionDialog.dismiss());
        title.setText(getResources().getString(R.string.dont_leave));
        subtitle.setText(getString(R.string.Leaving_this_page_will_cancel_your_upload));
        btn1_text.setText(getResources().getString(R.string.leave_anyway));
        btn2_text.setText(getResources().getString(R.string.Continue));
        actionDialog.show();
        MediaPlayer.create(requireContext(), R.raw.wronganswer).start();
    }

    private void cancelRequest() {
        if (multipartRequest != null)
            multipartRequest.cancel();
    }

    private void showSubmitTooltip() {
        try {
            if (submitTooltipStatus != null && !submitTooltipStatus.isActive() &&
                    TooltipHelper.getInstance().countNotCompleted(TooltipType.SUBMIT, requireActivity())) {
                MyTooltipDto myTooltipDto = MyTooltipDto.builder()
                        .text(getString(R.string.click_here))
                        .isAboveAnchor(true)
                        .alignment(RelativeLayout.CENTER_HORIZONTAL)
                        .autoDismiss(true)
                        .tooltipType(TooltipType.SUBMIT)
                        .fragmentNumber(fragNumber)
                        .isTest(true)
                        .currentFragment(MyApplication.getInstance().getTooltipViewModel().getCurrentFragment())
                        .build();
                TooltipHelper.getInstance().handleTooltip(scrollView, card,
                        myTooltipDto, this, requireActivity());
            }
        } catch (Exception e) {
            Log.d("checkClickHereTooltip", e.getMessage());
        }
    }

    private void resetUploadTooltip() {
        try {
            MyApplication.getInstance().getTooltipViewModel().resetUploadTooltip(requireActivity());
        } catch (Exception ignored) {
        }
    }

    private void resetReviewTooltip() {
        try {
            MyApplication.getInstance().getTooltipViewModel().resetReviewTooltip(requireActivity());
        } catch (Exception ignored) {
        }
    }

    private void observeViewModelForTooltip(View view) {
        try {
            if (!TooltipHelper.getInstance().isTooltipCompleted(requireActivity(), true)) {
                observeViewModelForTooltip();
                observeViewTreeForTooltip(view);
            }
        } catch (Exception ignored) {
        }
    }

    private void observeViewModelForTooltip() {
        try {
            MyApplication.getInstance().getTooltipViewModel().getSubmitTooltipStatus()
                    .observe(requireActivity(), tooltipStatusDto -> {
                        submitTooltipStatus = tooltipStatusDto;
                        if (submitTooltipStatus != null)
                            submitTooltipStatus.setTooltipType(TooltipType.SUBMIT);
                    });
            MyApplication.getInstance().getTooltipViewModel().getClickTooltipStatus()
                    .observe(requireActivity(), tooltipStatusDto -> {
                        clickTooltipStatus = tooltipStatusDto;
                        if (clickTooltipStatus != null)
                            clickTooltipStatus.setTooltipType(TooltipType.CLICK_HERE);
                    });
            MyApplication.getInstance().getTooltipViewModel().getOptionTooltipStatus()
                    .observe(requireActivity(), tooltipStatusDto -> {
                        optionTooltipStatus = tooltipStatusDto;
                        if (optionTooltipStatus != null)
                            optionTooltipStatus.setTooltipType(TooltipType.OPTION);
                    });
            MyApplication.getInstance().getTooltipViewModel().getUploadTooltipStatus()
                    .observe(requireActivity(), tooltipStatusDto -> {
                        uploadTooltipStatus = tooltipStatusDto;
                        if (uploadTooltipStatus != null)
                            uploadTooltipStatus.setTooltipType(TooltipType.UPLOAD);
                    });
            MyApplication.getInstance().getTooltipViewModel().getReviewTooltipStatus()
                    .observe(requireActivity(), tooltipStatusDto -> {
                        reviewTooltipStatus = tooltipStatusDto;
                        if (reviewTooltipStatus != null)
                            reviewTooltipStatus.setTooltipType(TooltipType.REVIEW);
                    });
        } catch (Exception ignored) {
        }
    }

    private void observeViewTreeForTooltip(View view) {
        final View rootView = view.findViewById(R.id.ParentLayout);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            int screenHeight = rootView.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;
            boolean keyboardCurrentlyActive = keypadHeight > screenHeight * 0.15;
            if (keyboardCurrentlyActive != isKeyBoardActive) {
                isKeyBoardActive = keyboardCurrentlyActive;
                if (isKeyBoardActive) {
                    Log.d("KeyboardStatus", "Keyboard is opened");
                    TooltipHelper.getInstance().dismiss();
                } else if (textExtracted) {
                    handleTooltipOnKeyBoardHide();
                }
                isKeyBoardActivePreviousState = isKeyBoardActive;
            }
        });
    }

    private boolean isCorrectFragment(TooltipStatusDto tooltipStatusDto) {
        try {
            return (tooltipStatusDto != null && (tooltipStatusDto.getFragment() == -1 || tooltipStatusDto.getFragment() == fragNumber)) &&
                    tooltipStatusDto.getCurrentFragment() == fragNumber &&
                    TooltipHelper.getInstance().countNotCompleted(tooltipStatusDto.getTooltipType(), getActivity());
        } catch (Exception ignored) {
        }
        return false;
    }

    private void handleTooltipOnKeyBoardHide() {
        if (isKeyBoardActivePreviousState && isCorrectFragment(uploadTooltipStatus)) {
            if (uploadTooltipStatus != null && uploadTooltipStatus.getStepCount() != 3) {
                uploadTooltipStatus.setStepCount(2);
            }
        }
        isKeyBoardActive = false;
        Log.d("KeyboardStatus", "Keyboard is closed");
        if (isShowThreeStepTooltip()) {
            scrollView.fullScroll(View.FOCUS_DOWN);
            startTextHandler();
        }
    }

    private void handleTooltipOnScroll() {
        TooltipHelper.getInstance().dismiss();
        if (isShowThreeStepTooltip())
            startTextHandler();
        if (isShowFiveStepTooltip())
            checkVisibility(reviewTooltipStatus.getStepCount());
    }

    private void handleTooltipOnResume() {
        if (!isReviewMode) {
            if (microLectureContentDto.getLectureContentType() == LectureContentType.LONG_ANSWERS
                    || microLectureContentDto.getLectureContentType() == LectureContentType.SHORT_ANSWERS) {
                if (isShowClickTooltip()) {
                    startHandlerForClick();
                }
            } else if (microLectureContentDto.getLectureContentType() == LectureContentType.VERY_SHORT_ANSWERS) {
                if (isShowOptionTooltip()) {
                    startHandlerForClick();
                }
            }

            if (isShowThreeStepTooltip()) {
                scrollView.fullScroll(View.FOCUS_DOWN);
                startTextHandler();
            }
        } else {
            if (isShowFiveStepTooltip()) {
                startHandlerForReview();
            }
        }
    }

    private void checkViewLoading() {
        AtomicInteger viewsReady = new AtomicInteger(0);
        Runnable onAllViewsReady = () -> {
            if (viewsReady.incrementAndGet() == 2) {
                checkClickHereTooltip();
            }
        };
        scrollView.post(onAllViewsReady);
        uploadSolutionBtn.post(onAllViewsReady);
    }

    private void checkClickHereTooltip() {
        try {
            MyTooltipDto myTooltipDto = MyTooltipDto.builder()
                    .isAboveAnchor(false)
                    .alignment(RelativeLayout.CENTER_HORIZONTAL)
                    .autoDismiss(true)
                    .fragmentNumber(fragNumber)
                    .isTest(true)
                    .currentFragment(MyApplication.getInstance().getTooltipViewModel().getCurrentFragment())
                    .build();
            if (microLectureContentDto.getLectureContentType() == LectureContentType.LONG_ANSWERS
                    || microLectureContentDto.getLectureContentType() == LectureContentType.SHORT_ANSWERS) {
                myTooltipDto.setText(getString(R.string.click_here));
                myTooltipDto.setTooltipType(TooltipType.CLICK_HERE);
                myTooltipDto.setHideArrow(false);
                TooltipHelper.getInstance().handleTooltip(scrollView, uploadSolutionBtn,
                        myTooltipDto, this, requireActivity());
            } else if (microLectureContentDto.getLectureContentType() == LectureContentType.VERY_SHORT_ANSWERS) {
                myTooltipDto.setText(getString(R.string.Choose_an_option_to_answer));
                myTooltipDto.setTooltipType(TooltipType.OPTION);
                myTooltipDto.setHideArrow(true);
                TooltipHelper.getInstance().handleTooltip(scrollView, typeAnswerCard,
                        myTooltipDto, this, requireActivity());
            }

        } catch (Exception e) {
            Log.d("checkClickHereTooltip", e.getMessage());
        }
    }

    private void startThreeStepTooltip() {
        if (isShowThreeStepTooltip()) {
            uploadTooltipStatus.setStepCount(1);
            scrollView.fullScroll(View.FOCUS_DOWN);
            startTextHandler();
        }
    }

    private boolean isShowClickTooltip() {
        try {
            return !isReviewMode && clickTooltipStatus != null && !clickTooltipStatus.isActive() &&
                    TooltipHelper.getInstance().countNotCompleted(TooltipType.CLICK_HERE, getActivity()) &&
                    scrollView != null && uploadImageLayoutContainer != null;
        } catch (Exception ignored) {
        }
        return false;
    }

    private boolean isShowOptionTooltip() {
        try {
            return !isReviewMode && optionTooltipStatus != null && !optionTooltipStatus.isActive() &&
                    TooltipHelper.getInstance().countNotCompleted(TooltipType.OPTION, getActivity()) &&
                    scrollView != null && uploadImageLayoutContainer != null;
        } catch (Exception ignored) {
        }
        return false;
    }

    private boolean isShowThreeStepTooltip() {
        if (uploadTooltipStatus != null) {
            return isCorrectFragment(uploadTooltipStatus) && !CollectionUtils.isEmpty(list) &&
                    extractedTextFromImageView.getVisibility() == View.VISIBLE &&
                    !uploadTooltipStatus.isFinished() && !isReviewMode && !isKeyBoardActive && textExtracted;
        }
        return false;
    }

    private boolean isShowFiveStepTooltip() {
        if (reviewTooltipStatus != null) {
            return isCorrectFragment(reviewTooltipStatus) && !reviewTooltipStatus.isFinished() &&
                    isReviewMode && analysisTitle != null && analysisTitle.getVisibility() == View.VISIBLE;
        }
        return false;
    }

    private void startTextHandler() {
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
        handler = new Handler();
        runnable = () -> extractedText.post(() -> {
            getCounts();
            handler.removeCallbacksAndMessages(null);
            handler = null;
        });
        handler.postDelayed(runnable, 500);
    }

    private void getCounts() {
        if (uploadTooltipStatus != null) {
            int stepCount = uploadTooltipStatus.getStepCount();
            if (stepCount <= 3) {
                stepCount = readText.getVisibility() == View.VISIBLE && readMore ? stepCount + 1 : stepCount;
                showToolTipAfterImageUpload(Math.min(stepCount, getTotalCount()), getTotalCount());
            }
        }
    }

    private int getTotalCount() {
        return readText.getVisibility() == View.VISIBLE ? 3 : 2;
    }


    private void showToolTipAfterImageUpload(int stepCount, int totalSteps) {
        uploadTooltipStatus.setStepCount(stepCount);
        switch (uploadTooltipStatus.getStepCount()) {
            case 1:
                showToolTipAfterImageUpload(extractedText, RelativeLayout.CENTER_HORIZONTAL, false, stepCount, totalSteps);
                break;
            case 2:
                if (totalSteps == 3) {
                    showToolTipAfterImageUpload(readText, RelativeLayout.ALIGN_END, false, stepCount, totalSteps);
                } else {
                    showToolTipAfterImageUpload(card, RelativeLayout.ALIGN_END, true, stepCount, totalSteps);
                }
                break;
            case 3:
                showToolTipAfterImageUpload(card, RelativeLayout.ALIGN_END, true, stepCount, totalSteps);
                break;
        }
    }

    private void showToolTipAfterImageUpload(View view, int alignment, boolean isAboveAnchor, int stepCount, int totalSteps) {
        try {
            MyTooltipDto myTooltipDto = MyTooltipDto.builder()
                    .alignment(alignment)
                    .isAboveAnchor(isAboveAnchor)
                    .autoDismiss(false)
                    .stepCount(stepCount)
                    .totalSteps(totalSteps)
                    .fragmentNumber(fragNumber)
                    .tooltipType(TooltipType.UPLOAD)
                    .isTest(true)
                    .currentFragment(MyApplication.getInstance().getTooltipViewModel().getCurrentFragment())
                    .build();
            TooltipHelper.getInstance().handleTooltip(scrollView, view,
                    myTooltipDto, this, requireActivity());
        } catch (Exception e) {
            Log.d("checkClickHereTooltip", e.getMessage());
        }

    }

    private void startHandlerForClick() {
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
        handler = new Handler();
        runnable = () -> scrollView.post(() -> {
            checkViewLoading();
            handler.removeCallbacksAndMessages(null);
            handler = null;
        });
        handler.postDelayed(runnable, 500);
    }

    private void startHandlerForReview() {
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
        handler = new Handler();
        runnable = () -> scrollView.post(() -> {
            if (reviewTooltipStatus.getStepCount() == 0 ||
                    reviewTooltipStatus.getStepCount() == 1)
                scrollView.fullScroll(View.FOCUS_UP);
            showFiveStepToolTip(reviewTooltipStatus.getStepCount());
            handler.removeCallbacksAndMessages(null);
            handler = null;
        });
        handler.postDelayed(runnable, 200);
    }

    private void checkVisibility(int stepCount) {
        reviewTooltipStatus.setStepCount(stepCount);
        switch (stepCount) {
            case 1 -> scrollView.post(() -> {
                if (TooltipHelper.getInstance().isViewFullyVisibleInScrollView(analysisCard, scrollView))
                    showFiveStepToolTip(analysisCard, RelativeLayout.ALIGN_START, 1);
            });
            case 2 -> scrollView.post(() -> {
                if (TooltipHelper.getInstance().isViewFullyVisibleInScrollView(idealSolutionCard, scrollView))
                    showFiveStepToolTip(idealTitle, RelativeLayout.ALIGN_START, 2);
            });
            case 3 -> scrollView.post(() -> {
                if (TooltipHelper.getInstance().isViewFullyVisibleInScrollView(descriptionCard, scrollView))
                    showFiveStepToolTip(descriptionCard, RelativeLayout.ALIGN_START, 3);
            });
            case 4 -> scrollView.post(() -> {
                showFiveStepToolTip(askDoubtCard, RelativeLayout.ALIGN_END, 4);
            });
            case 5 -> showFiveStepToolTip(card, RelativeLayout.ALIGN_END, 5);
        }
    }

    private void scrollFiveStepToolTip(int stepCount) {
        reviewTooltipStatus.setStepCount(stepCount);
        switch (stepCount) {
            case 1 -> scrollView.post(() -> {
                scrollView.smoothScrollTo(0, analysisCard.getTop());
            });
            case 2 -> scrollView.post(() -> {
                scrollView.smoothScrollTo(0, idealSolutionCard.getTop());
            });
            case 3 -> scrollView.post(() -> {
                scrollView.smoothScrollTo(0, extractedTextFromImageView.getTop());
            });
        }
        showFiveStepToolTip(stepCount);
    }

    private void showFiveStepToolTip(int stepCount) {
        if (stepCount == 0)
            stepCount = 1;
        reviewTooltipStatus.setStepCount(stepCount);
        switch (stepCount) {
            case 1 -> scrollView.post(() -> {
                scrollView.postDelayed(() ->
                        showFiveStepToolTip(analysisCard,
                                RelativeLayout.ALIGN_START, 1), 1);
            });
            case 2 -> scrollView.post(() -> {
                scrollView.postDelayed(() -> showFiveStepToolTip(idealSolutionCard,
                        RelativeLayout.ALIGN_START, 2), 2);
            });
            case 3 -> scrollView.post(() -> {
                scrollView.postDelayed(() -> showFiveStepToolTip(descriptionCard,
                        RelativeLayout.ALIGN_START, 3), 300);
            });
            case 4 -> scrollView.post(() -> {
                scrollView.postDelayed(() -> showFiveStepToolTip(askDoubtCard,
                        RelativeLayout.ALIGN_END, 4), 300);
            });
            case 5 -> showFiveStepToolTip(card, RelativeLayout.ALIGN_END, 5);
        }
    }

    private void showFiveStepToolTip(View view, int alignment, int stepCount) {
        try {
            MyTooltipDto myTooltipDto = MyTooltipDto.builder()
                    .alignment(alignment)
                    .isAboveAnchor(true)
                    .autoDismiss(false)
                    .stepCount(stepCount)
                    .totalSteps(5)
                    .showNext(true)
                    .tooltipType(TooltipType.REVIEW)
                    .fragmentNumber(fragNumber)
                    .isTest(true)
                    .currentFragment(MyApplication.getInstance().getTooltipViewModel().getCurrentFragment())
                    .build();
            TooltipHelper.getInstance().handleTooltip(scrollView, view,
                    myTooltipDto, this, requireActivity());
        } catch (Exception e) {
            Log.d("checkClickHereTooltip", e.getMessage());
        }
    }

    @Override
    public void onToolTipClick() {
        try {
            TooltipHelper.getInstance().dismiss();
            if (uploadTooltipStatus != null && uploadTooltipStatus.getStepCount() == uploadTooltipStatus.getTotalSteps()) {
                finishUploadTooltip();
            } else {
                if(uploadTooltipStatus!=null) {
                    showToolTipAfterImageUpload(uploadTooltipStatus.getStepCount() + 1, getTotalCount());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onToolTipNextClick() {
        goToNextTooltip();
    }

    private void goToNextTooltip() {
        TooltipHelper.getInstance().dismiss();
        if (reviewTooltipStatus != null && reviewTooltipStatus.getStepCount() == 5) {
            finishReviewTooltip(false);
        } else scrollFiveStepToolTip(reviewTooltipStatus.getStepCount() + 1);
    }

    private void finishReviewTooltip(boolean isSkip) {
        TooltipHelper.getInstance().dismiss();
        if (isReviewMode && reviewTooltipStatus != null && !reviewTooltipStatus.isFinished() &&
                (reviewTooltipStatus.getStepCount() == 5 || isSkip)) {
            reviewTooltipStatus.setFinished(true);
            MyApplication.getInstance().getTooltipViewModel().getReviewTooltipStatus().setValue(reviewTooltipStatus);
            finishTooltip(TooltipType.REVIEW);
        }
    }

    private void finishUploadTooltip() {
        if (!isReviewMode && uploadTooltipStatus != null && !uploadTooltipStatus.isFinished() &&
                uploadTooltipStatus.getStepCount() == uploadTooltipStatus.getTotalSteps()) {
            uploadTooltipStatus.setFinished(true);
            MyApplication.getInstance().getTooltipViewModel().getUploadTooltipStatus().setValue(uploadTooltipStatus);
            finishTooltip(TooltipType.UPLOAD);
        }
    }

    private void finishTooltip(TooltipType tooltipType) {
        try {
            TooltipHelper.getInstance().finish(requireActivity(), tooltipType);
        } catch (Exception e) {
            Log.d("TooltipHelper.getInstance().finish()", e.getMessage());
        }
    }

    @Override
    public void onToolTipSkipClick() {
        finishReviewTooltip(true);
    }
}