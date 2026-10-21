package arivihan.technologies.doubtbuzzter2.fragments;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.BOARD_ANSWER_VALIDATE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.EXTRACT_TEXT_FROM_IMAGE_URI;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageActivity;
import com.theartofdev.edmodo.cropper.CropImageOptions;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.ImageCropperActivity;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.common.FullScreenImageDialog;
import arivihan.technologies.doubtbuzzter2.common.MultipartRequestDataPart;
import arivihan.technologies.doubtbuzzter2.constants.QuestionLevel;
import arivihan.technologies.doubtbuzzter2.customView.MyToolTip;
import arivihan.technologies.doubtbuzzter2.customView.SavBottomSheetDialog;
import arivihan.technologies.doubtbuzzter2.enums.FullViewImageType;
import arivihan.technologies.doubtbuzzter2.helper.FileUtil;
import arivihan.technologies.doubtbuzzter2.helper.ImageLoaderUtil;
import arivihan.technologies.doubtbuzzter2.helper.TooltipHelper;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionType;
import arivihan.technologies.doubtbuzzter2.listener.SubmitListener;
import arivihan.technologies.doubtbuzzter2.model.FactoryShipQnADto;
import arivihan.technologies.doubtbuzzter2.model.ImageTextExtractionResponse;
import arivihan.technologies.doubtbuzzter2.model.MyTooltipDto;
import arivihan.technologies.doubtbuzzter2.model.ShortLongAnswerSubmitResponse;
import arivihan.technologies.doubtbuzzter2.model.TooltipStatusDto;
import arivihan.technologies.doubtbuzzter2.model.TooltipType;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyMultipartRequest;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class VeryShortFragment extends Fragment implements MyToolTip.ToolTipClickListener,
        SavBottomSheetDialog.BottomSheetListener {

    private static final String TAG = VeryShortFragment.class.getSimpleName();
    FactoryShipQnADto factoryShipQnADto;
    String number;
    String total;
    String subjectCode;
    LinearLayout typeAnswerLayout;
    TextView typeAnswerStatusText;
    MaterialCardView typeAnswerCard;
    EditText typeAnswer;
    TextView typedAnswerWordCount;
    RelativeLayout uploadImageLayout, uploadButtonLayout;
    ConstraintLayout bottomLayout;
    String userAnswer;
    SubmitListener submitListener;
    CardView submit;
    TextView submitText;
    ScrollView scrollView;
    ViewTreeObserver observer;
    CardView viewSolutionCard;
    boolean dataLoaded;
    SavBottomSheetDialog savBottomSheetDialog;
    ShortLongAnswerSubmitResponse submitResponse;
    MyJsonObjectRequest submitJsonObjectRequest;
    Dialog answerCheckDialog;
    TextView yourScore;
    LottieAnimationView animationAnswerCheck, doubtAnimation;
    TextView subtitleAnswerCheck;
    MediaPlayer mediaPlayer;
    TextView description;
    String requestTag = "submitRequestTag";
    private Dialog imagePickDialog;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> cropImageLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<Intent> cropActivityLauncher;
    private static final int CAMERA_PERMISSION_CODE = 100;
    LinearLayout extractedTextLayout;
    EditText extractedText;
    CardView editTextBack;
    CardView difficultyCard;
    TextView difficultyLevel;
    MaterialCardView textCard;
    ConstraintLayout textCardLayout;
    CardView marksCard;
    TextView marksText, imageUploadText;
    TextView solutionText, analysisText;
    TextView idealTitle, analysisTitle;
    ImageLoaderUtil solutionImage;
    RelativeLayout imageUploadLayout;
    LinearLayout answerBox;
    View solutionView;
    ConstraintLayout askDoubt;
    Uri currentPhotoPath;
    TextView questionNumber;
    TextView questionText;
    ImageView questionImage;
    MaterialCardView submittedAnswerLayout;
    TextView submittedAnswerText;
    ImageView reportQuestion;
    CardView uploadSolutionBtn;
    TextView btn_text;
    LinearLayout answerImageLayout;
    ImageView answerImage;
    ImageView deleteImage;
    ImageView cropImage;
    ImageView addImage;
    Uri answerImageUri;
    TextView countText;
    TextView readText;
    TextView status_text1;
    long wordLimitBuffer = MyApplication.getFirebaseRemoteConfig().getLong("word_limit_buffer_very_short");
    long wordLimit = MyApplication.getFirebaseRemoteConfig().getLong("word_limit_very_short");
    int stepCount = 1;
    int totalSteps;
    TextView tagText;
    CardView tagCard;
    MaterialCardView idealSolutionCard;
    MaterialCardView analysisCard;
    CardView descriptionCard;
    CardView askDoubtCard;
    TextView solution_title;
    Handler handler;
    Runnable runnable;
    private boolean isKeyboardVisible;
    boolean increaseCount = true;
    CardView clearAttempt;
    private boolean textExtracted;
    private boolean isKeyBoardActive;
    private TooltipStatusDto uploadTooltipStatus = new TooltipStatusDto();
    private TooltipStatusDto reviewTooltipStatus = new TooltipStatusDto();
    private TooltipStatusDto clickTooltipStatus = new TooltipStatusDto();
    private TooltipStatusDto submitTooltipStatus = new TooltipStatusDto();
    FullScreenImageDialog fullScreenImageDialog;
    List<String> userAnswerImageUrl;
    SavBottomSheetDialog.BottomSheetListener bottomSheetListener;
    private static final long SUBMIT_CLICK_DELAY = 800; // in ms
    private long lastSubmitClickTime = 0;

    String questionRequestIdFromML;
    String questionSessionIdFromML;

    public VeryShortFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            submitListener = (SubmitListener) context;
        } catch (ClassCastException e) {
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getActivity() != null && isAdded() && !isStateSaved()) {
            if (savBottomSheetDialog != null) {
                savBottomSheetDialog.dismiss();
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("factoryShipQnADto", factoryShipQnADto);
        outState.putString(PreferenceKeys.PHONE_NUMBER.getKey(), number);
        outState.putString("total", total);
        outState.putString("subjectCode", subjectCode);
    }

    public static VeryShortFragment newInstance(FactoryShipQnADto factoryShipQnADto, String number, String total, String subjectCode) {
        VeryShortFragment fragment = new VeryShortFragment();
        Bundle args = new Bundle();
        args.putParcelable("factoryShipQnADto", factoryShipQnADto);
        args.putString(PreferenceKeys.PHONE_NUMBER.getKey(), number);
        args.putString("total", total);
        args.putString("subjectCode", subjectCode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bottomSheetListener = this;
        if (getArguments() != null) {
            factoryShipQnADto = getArguments().getParcelable("factoryShipQnADto");
            number = getArguments().getString(PreferenceKeys.PHONE_NUMBER.getKey());
            total = getArguments().getString("total");
            subjectCode = getArguments().getString("subjectCode");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_very_short, container, false);
        Log.d(TAG, "DTO: " + factoryShipQnADto);
        factoryShipQnADto.setWordLimit(updateWordLimit(factoryShipQnADto));
        setUpViews(view);
        observeViewModelForTooltip(view);
        setUpQuestionDetail();
        handleTypeEditText();
        handleExtractedEditText();
        handleImagePickerDialog();
        setUpAnswerCheckDialog();
        handleCameraPermissions();
        setUpAnswerDetail();
        handleCropActivityLauncher();
        return view;
    }

    private String updateWordLimit(FactoryShipQnADto dto) {
        long defaultLimit = MyApplication.getFirebaseRemoteConfig().getLong("word_limit_very_short");
        if (dto == null)
            return String.valueOf(defaultLimit);

        long wordLimit = MyApplication.getFirebaseRemoteConfig().getLong((StringUtils.isNotEmpty(dto.getType()) ? dto.getType() : "default") + "_word_limit");
        return StringUtils.isNotEmpty(dto.getWordLimit()) ? dto.getWordLimit() : (wordLimit != 0 ? String.valueOf(wordLimit) : String.valueOf(defaultLimit));
    }

    private void handleCropActivityLauncher() {
        cropActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                        CropImage.ActivityResult activityResult = CropImage.getActivityResult(result.getData());
                        if (activityResult != null) {
                            currentPhotoPath = activityResult.getUri();
                            if (getImageSize(currentPhotoPath) <= 15) {
                                typeAnswerLayout.setVisibility(View.GONE);
                                bottomLayout.setVisibility(View.VISIBLE);
                                uploadImageLayout.setVisibility(View.GONE);
                                answerImageLayout.setVisibility(View.VISIBLE);
                                answerImageUri = currentPhotoPath;
                                Glide.with(requireContext()).load(currentPhotoPath).into(answerImage);
                                showSubmitTooltip();
                            } else {
                                Toast.makeText(getActivity(), "size larger than 15mb", Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        Log.d("CropActivity", "Cropping was canceled or failed");
                    }
                }
        );
    }

    private void setUpViews(View view) {
        solution_title = view.findViewById(R.id.solution_title);
        solution_title.setVisibility(View.GONE);
        tagText = view.findViewById(R.id.tag_text);
        tagCard = view.findViewById(R.id.tag_card);
        askDoubtCard = view.findViewById(R.id.ask_doubt_card);
        doubtAnimation = view.findViewById(R.id.animation);
        descriptionCard = view.findViewById(R.id.description_card);
        idealSolutionCard = view.findViewById(R.id.ideal_solution_card);
        analysisCard = view.findViewById(R.id.analysis_card);
        status_text1 = view.findViewById(R.id.status_text1);
        status_text1.setVisibility(View.VISIBLE);
        readText = view.findViewById(R.id.read_text);
        readText.setVisibility(View.GONE);
        countText = view.findViewById(R.id.count_text);
        addImage = view.findViewById(R.id.add_image_card);
        addImage.setVisibility(View.GONE);
        deleteImage = view.findViewById(R.id.delete_image);
        cropImage = view.findViewById(R.id.crop_image);
        answerImageLayout = view.findViewById(R.id.answer_image_layout);
        answerImage = view.findViewById(R.id.extracted_image);
        uploadSolutionBtn = view.findViewById(R.id.upload_solution_btn);
        uploadButtonLayout = view.findViewById(R.id.upload_button_relative);
        btn_text = view.findViewById(R.id.btn_text);
        reportQuestion = view.findViewById(R.id.report_question);
        submittedAnswerLayout = view.findViewById(R.id.submitted_answer_layout);
        submittedAnswerText = view.findViewById(R.id.submitted_answer_text);
        questionNumber = view.findViewById(R.id.question_number);
        questionText = view.findViewById(R.id.question_text);
        questionImage = view.findViewById(R.id.image_question);
        textCard = view.findViewById(R.id.text_card);
        textCardLayout = view.findViewById(R.id.text_card_layout);
        description = view.findViewById(R.id.description);
        extractedTextLayout = view.findViewById(R.id.text_layout);
        extractedText = view.findViewById(R.id.extracted_text);
        extractedText.setMaxEms(75);
        editTextBack = view.findViewById(R.id.edit_text_back);
        editTextBack.setOnClickListener(v -> {
            TooltipHelper.getInstance().dismiss();
            description.setVisibility(View.VISIBLE);
            extractedTextLayout.setVisibility(View.GONE);
            factoryShipQnADto.setUserAnswer("");
            answerImageLayout.setVisibility(View.VISIBLE);
            submit.setEnabled(true);
            extractedText.setEnabled(false);
        });
        typeAnswerLayout = view.findViewById(R.id.type_answer_layout);
        typeAnswerStatusText = view.findViewById(R.id.type_answer_status_text);
        typeAnswerCard = view.findViewById(R.id.type_answer_card);
        typeAnswer = view.findViewById(R.id.type_answer);
        typedAnswerWordCount = view.findViewById(R.id.typed_answer_word_count);
        uploadImageLayout = view.findViewById(R.id.upload_image_layout);
        if (StringUtils.isNotEmpty(subjectCode) && subjectCode.equals("Biology")) {
            uploadImageLayout.setVisibility(View.GONE);
            typeAnswerStatusText.setText(getString(R.string.Apna_answer_yha_type_karein));
        }
        bottomLayout = view.findViewById(R.id.bottom_layout);
        submitText = view.findViewById(R.id.submit_text);
        scrollView = view.findViewById(R.id.scroll_view);
        imageUploadLayout = view.findViewById(R.id.upload_file_layout);
        marksText = view.findViewById(R.id.marks_text);
        marksCard = view.findViewById(R.id.marks_card);
        askDoubt = view.findViewById(R.id.ask_doubt_fab);
        viewSolutionCard = view.findViewById(R.id.view_solution_card);
        solutionView = view.findViewById(R.id.solution_view);
        answerBox = view.findViewById(R.id.solutionBox);
        analysisText = view.findViewById(R.id.analysis_text);
        solutionText = view.findViewById(R.id.solution_text_sav);
        analysisTitle = view.findViewById(R.id.analysis_title);
        idealTitle = view.findViewById(R.id.ideal_title);
        view.findViewById(R.id.solution_text).setVisibility(View.GONE);
        solutionImage = view.findViewById(R.id.solution_image_sav);
        difficultyCard = view.findViewById(R.id.difficulty_card);
        difficultyLevel = view.findViewById(R.id.difficulty_type);
        imageUploadText = view.findViewById(R.id.upload_image_text);
        String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        SpannableString spannableString = new SpannableString(getString(R.string.Answer_upload_karein));
        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.spannable_teal_color)), 0, "hi".equals(lang) ? 16 : 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        imageUploadText.setText(spannableString);
        clearAttempt = view.findViewById(R.id.clear_attempt_card);
        clearAttempt.setOnClickListener(view1 -> setClearAttempt());
        submitText = view.findViewById(R.id.submit_text);
        submit = view.findViewById(R.id.submit_card);

        scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            Log.d(TAG, "On Scroll Change");
            if (scrollView.canScrollVertically(1)
                    && !factoryShipQnADto.isReattempt()
                    && factoryShipQnADto.isCompleted()
                    && !factoryShipQnADto.isHideViewSolution()) {
                viewSolutionCard.setVisibility(View.VISIBLE);
            } else {
                viewSolutionCard.setVisibility(View.GONE);
            }
            handleTooltipOnScroll();

//            if (scrollY == 0) {
//                // Scrolling up
//                askDoubtCard.setVisibility(View.VISIBLE);
//                doubtAnimation.setVisibility(View.VISIBLE);
//            } else {
//                // Scrolling down
//                askDoubtCard.setVisibility(View.GONE);
//                doubtAnimation.setVisibility(View.GONE);
//            }
        });

        if (StringUtils.isNotEmpty(factoryShipQnADto.getYear())) {
            tagCard.setVisibility(View.VISIBLE);
            tagText.setText(factoryShipQnADto.getYear());
        } else {
            tagCard.setVisibility(View.GONE);
        }

        observer = scrollView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(() -> {
            int viewHeight = scrollView.getMeasuredHeight();
            int contentHeight = scrollView.getChildAt(0).getHeight();
            if (viewHeight - contentHeight < 0 && !dataLoaded) {
                if (factoryShipQnADto.isCompleted() &&
                        !factoryShipQnADto.isReattempt() &&
                        !factoryShipQnADto.isHideViewSolution()) {
                    viewSolutionCard.setVisibility(View.VISIBLE);
                } else {
                    viewSolutionCard.setVisibility(View.GONE);
                }
                dataLoaded = true;
            }
        });

        submit.setOnClickListener(view1 -> {

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastSubmitClickTime < SUBMIT_CLICK_DELAY) {
                return;
            }
            lastSubmitClickTime = currentTime;

            TooltipHelper.getInstance().dismiss();
            if (MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                if (!factoryShipQnADto.isReattempt() && factoryShipQnADto.isCompleted()) {
                    finishReviewTooltip(false);
                    submitListener.openNext();
                } else if (typeAnswerLayout.getVisibility() == View.VISIBLE || extractedTextLayout.getVisibility() == View.VISIBLE) {
                    if ((extractedTextLayout.getVisibility() == View.VISIBLE && StringUtils.isNotEmpty(extractedText.getText().toString().trim())) || (typeAnswerLayout.getVisibility() == View.VISIBLE && StringUtils.isNotEmpty(typeAnswer.getText().toString().trim()))) {
                        if (textExtracted) {
                            finishUploadTooltip();
                        }
                        resetReviewTooltip();
                        handleBottomSheet(false, true);
                        submitAnswerAndGetSolution();
                    } else {
                        CommonMethods.showNegativeSnackbar(requireView(), getString(R.string.submit_karne_se_pehle_answer_check_krein), 2000);
                    }
                } else if (answerImageLayout.getVisibility() == View.VISIBLE) {
                    textExtracted = false;
                    resetUploadTooltip();
                    resetUploadTooltip();
                    handleBottomSheet(false, false);
                    extractTextFromImage();
                }
            } else {
                CommonMethods.showNegativeSnackbar(submit, getString(R.string.NoInternetConnection));
            }
        });

        viewSolutionCard.setOnClickListener(v -> {
            factoryShipQnADto.setHideViewSolution(true);
            submitListener.onViewSolutionClick();
            viewSolutionCard.setVisibility(View.GONE);
            scrollView.fullScroll(View.FOCUS_DOWN);
        });
        askDoubt.setOnClickListener(v -> {

            try {
                String chatSessionId = CommonMethods.stringToMd5(
                        "mtq_" + factoryShipQnADto.getQuestionId()
                                + "_" + MyApplication.getStringData(PreferenceKeys.USER_ID.getKey())
                                + "_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey())
                );

                CommonNavigator.navigateToDoubtChatFromCQNQ(
                        getActivity(),
                        chatSessionId,
                        subjectCode,                              //SubjectCode
                        factoryShipQnADto.getQuestion(),          // question
                        factoryShipQnADto.getUserAnswer(),         // studentAnswer
                        factoryShipQnADto.getAnswer(),             // idealAnswer
                        factoryShipQnADto.getAnalysis(),           // analysisOfAnswer
                        factoryShipQnADto.getAnswerEx(),           // explanationOfQuestion
                        factoryShipQnADto.getMarksObtained(),      // obtainedMarks
                        factoryShipQnADto.getMarks() ,
                        factoryShipQnADto.getType()  ,
                        factoryShipQnADto.getImageExplanationLatex(),
                        factoryShipQnADto.getImageQuestion(),
                        factoryShipQnADto.getAnswerImage(),
                        factoryShipQnADto.getChapterCode(),
                        factoryShipQnADto.getCode()
                );
                if (reviewTooltipStatus != null && reviewTooltipStatus.getStepCount() == 4)
                    goToNextTooltip();
            } catch (Exception e) {
                Log.i("ARIVIHAN_EXCEPTION", "Unable to start activity WebviewDoubtChatScreen.class");
            }
        });
        reportQuestion.setOnClickListener(v -> submitListener.reportQuestion());
        uploadImageLayout.setOnClickListener(v -> {
            TooltipHelper.getInstance().dismiss();
            imagePickDialog.show();
        });
        deleteImage.setOnClickListener(view12 -> {
            TooltipHelper.getInstance().dismiss();
            bottomLayout.setVisibility(View.GONE);
            textExtracted = false;
            typeAnswerLayout.setVisibility(View.VISIBLE);
            if (StringUtils.isNotEmpty(subjectCode) && !subjectCode.equals("Biology")) {
                uploadImageLayout.setVisibility(View.VISIBLE);
            }
            answerImageLayout.setVisibility(View.GONE);
        });
        cropImage.setOnClickListener(view13 -> startCropActivity(answerImageUri));
    }

    private void handleCameraPermissions() {
        cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean isGranted) {
                if (isGranted) {
                    captureImage();
                } else {
                    // Permission denied
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)) {
                        // User has denied permission permanently
                        Dialog dialog = CommonMethods.getCameraPermissionDialog(requireContext());
                        dialog.show();
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.camera_permission_zaruri_hai_upload_ke_liye), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void handleExtractedEditText() {
        extractedText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (extractedText.isEnabled()) {
                    userAnswer = s.toString().trim();
                    handleCount(userAnswer.split("\\s+").length);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                totalSteps = 2;
                if (stepCount > 1)
                    stepCount--;
            }
        });
    }

    private void startCropActivity(Uri imageUri) {
        Intent cropIntent = CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                .getIntent(requireActivity());
        cropImageLauncher.launch(cropIntent);
    }

    private void setUpQuestionDetail() {
        questionNumber.setText("Q" + number + " of " + total);
        if (StringUtils.isNotEmpty(factoryShipQnADto.getImageQuestion())) {
            questionImage.setVisibility(View.VISIBLE);
            Glide.with(getActivity()).load(factoryShipQnADto.getImageQuestion()).into(questionImage);
            questionImage.setOnClickListener(view -> {
                fullScreenImageDialog = new FullScreenImageDialog(requireContext(),
                        factoryShipQnADto.getImageQuestion(), FullViewImageType.QUESTION);
                fullScreenImageDialog.show();
            });
        } else {
            questionImage.setVisibility(View.GONE);
        }

        if (StringUtils.isNotEmpty(factoryShipQnADto.getQuestion())) {
            questionText.setVisibility(View.VISIBLE);
            questionText.setText(factoryShipQnADto.getQuestion().trim());
        } else {
            questionText.setVisibility(View.GONE);
        }
    }

    private void handleTypeEditText() {
        typeAnswer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                userAnswer = charSequence.toString().trim();
                if (!userAnswer.isEmpty() && (uploadImageLayout.getVisibility() == View.VISIBLE || (StringUtils.isNotEmpty(subjectCode) && subjectCode.equals("Biology")))) {
                    uploadImageLayout.setVisibility(View.GONE);
                    typedAnswerWordCount.setVisibility(View.VISIBLE);
                    bottomLayout.setVisibility(View.VISIBLE);
                }
                if (userAnswer.isEmpty() && uploadImageLayout.getVisibility() == View.GONE) {
                    if (StringUtils.isNotEmpty(subjectCode) && !subjectCode.equals("Biology")) {
                        uploadImageLayout.setVisibility(View.VISIBLE);
                    }
                    typedAnswerWordCount.setVisibility(View.GONE);
                    bottomLayout.setVisibility(View.GONE);
                }
                if (!userAnswer.isEmpty())
                    handleCount(userAnswer.split("\\s+").length);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void handleCount(int count) {
        try {
            wordLimit = Integer.parseInt(factoryShipQnADto.getWordLimit());
        } catch (Exception e) {
            Log.e("wordLimit", e.getMessage(), e);
        }
        if (typeAnswerLayout.getVisibility() == View.VISIBLE)
            handleTypeAnswerCount(count, wordLimit, wordLimitBuffer);
        else {
            handleExtractedAnswerCount(count, wordLimit, wordLimitBuffer);
        }
    }

    private void handleTypeAnswerCount(int count, long wordLimit, long wordLimitBuffer) {
        typedAnswerWordCount.setText(String.format(count + "/" + wordLimit + " "));
        typedAnswerWordCount.setVisibility(factoryShipQnADto.isWordLimitApplicable() ? View.VISIBLE : View.GONE);

        if (count > (wordLimit + wordLimitBuffer) && factoryShipQnADto.isWordLimitApplicable()) {
            typeAnswerStatusText.setText(getString(R.string.word_limit_text));
            typeAnswerStatusText.setTextColor(getResources().getColor(R.color.red));
            typedAnswerWordCount.setTextColor(getResources().getColor(R.color.red));
            typeAnswerCard.setStrokeColor(getResources().getColor(R.color.red));
            submit.setEnabled(false);
        } else {
            typeAnswerStatusText.setText(getString(R.string.Option_1_Apna_answer_yha_type_karein));
            typeAnswerStatusText.setTextColor(getResources().getColor(R.color.check_text_color));
            typedAnswerWordCount.setTextColor(getResources().getColor(R.color.subscription_blue));
            typeAnswerCard.setStrokeColor(getResources().getColor(R.color.text_card_stroke));
            submit.setEnabled(true);
        }
    }

    private void handleExtractedAnswerCount(int count, long wordLimit, long wordLimitBuffer) {
        countText.setText(String.format(count + "/" + wordLimit));
        countText.setVisibility(factoryShipQnADto.isWordLimitApplicable() ? View.VISIBLE : View.GONE);
        if (count > (wordLimit + wordLimitBuffer) && factoryShipQnADto.isWordLimitApplicable()) {
            description.setText(getString(R.string.word_limit_text));
            description.setTextColor(getResources().getColor(R.color.red));
            countText.setTextColor(getResources().getColor(R.color.red));
            textCard.setStrokeColor(getResources().getColor(R.color.red));
            submit.setEnabled(false);
        } else {
            description.setText(getString(R.string.submit_karne_se_pehle_answer_check_krein));
            description.setTextColor(getResources().getColor(R.color.check_text_color));
            countText.setTextColor(getResources().getColor(R.color.subscription_blue));
            textCard.setStrokeColor(getResources().getColor(R.color.text_card_stroke));
            submit.setEnabled(true);
        }
    }

    private void handleBottomSheet(boolean error, boolean textExtracted) {
        if (getActivity() != null && isAdded()) {
            try {
                if (savBottomSheetDialog != null) {
                    savBottomSheetDialog.dismiss();
                }
                Log.d("bottomsheetlistener", "handleBottomSheet:" +
                        " " + bottomSheetListener);
                savBottomSheetDialog = new SavBottomSheetDialog(bottomSheetListener);
                Bundle bundle = new Bundle();
                bundle.putBoolean("error", error);
                bundle.putBoolean("textExtracted", textExtracted);
                bundle.putString("finalText", userAnswer);
                savBottomSheetDialog.setArguments(bundle);
                if (isAdded() && !getParentFragmentManager().isStateSaved()) {
                    savBottomSheetDialog.show(getParentFragmentManager(), "");
                }
                savBottomSheetDialog.setCancelable(false);
                savBottomSheetDialog.setCancelable(false);
            } catch (Exception e) {
                Log.d("handleBottomSheet: ", e.getMessage());
            }
        }
    }

    private void submitAnswerAndGetSolution() {
        JSONObject params = new JSONObject();
        try {
            Log.d("SUBMIT_FLOW", "Preparing JSON for answer submission...");

            String questionId = factoryShipQnADto.getQuestionId();
            String question = factoryShipQnADto.getQuestion();
            String idealAnswer = factoryShipQnADto.getAnswer();
            String marks = factoryShipQnADto.getMarks();
            String wordLimit = factoryShipQnADto.getWordLimit();
            int wordCount = userAnswer == null ? 0 : userAnswer.trim().split("\\s+").length;

            Log.d("SUBMIT_FLOW", "questionId: " + questionId);
            Log.d("SUBMIT_FLOW", "question: " + question);
            Log.d("SUBMIT_FLOW", "userAnswer: " + userAnswer);
            Log.d("SUBMIT_FLOW", "idealAnswer (raw): " + idealAnswer);
            Log.d("SUBMIT_FLOW", "marks: " + marks);
            Log.d("SUBMIT_FLOW", "wordLimit: " + wordLimit);
            Log.d("SUBMIT_FLOW", "wordCount: " + wordCount);

            params.put("questionId", questionId);
            params.put("answerByStudents", userAnswer);
            params.put("idealAnswer", formatIdealAnswerText(idealAnswer));
            params.put("marks", marks);
            params.put("numerical", false);
            params.put("question", question);
            params.put("wordCount", wordCount);
            params.put("wordLimit", wordLimit);
            params.put("boldFormatting", true);
            params.put("requestId",questionRequestIdFromML);
            params.put("sessionId",questionSessionIdFromML);
            if (!CollectionUtils.isEmpty(userAnswerImageUrl)) {
                String commaSeparatedUrls = String.join(",", userAnswerImageUrl);
                params.put("urls", commaSeparatedUrls);
            }

            Log.d("SUBMIT_FLOW", "JSON Params: " + params.toString());

        } catch (JSONException e) {
            Log.e("SUBMIT_FLOW", "JSONException occurred: " + e.getMessage(), e);
        }

        submitJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST, BOARD_ANSWER_VALIDATE,
                params, response -> {
            Log.d("SUBMIT_FLOW", "Received Response: " + response.toString());

            try {
                TypeToken<ShortLongAnswerSubmitResponse> token = new TypeToken<ShortLongAnswerSubmitResponse>() {
                };
                Gson gson = new Gson();
                submitResponse = gson.fromJson(response.toString(), token.getType());

                factoryShipQnADto.setAnswerEx(submitResponse.getExplanation());
                factoryShipQnADto.setCompleted(true);
                factoryShipQnADto.setReattempt(false);
                factoryShipQnADto.setUserAnswer(userAnswer.trim());
                factoryShipQnADto.setMarks(submitResponse.getTotalMarks());
                factoryShipQnADto.setMarksObtained(submitResponse.getMarks());
                factoryShipQnADto.setUserSolution(submitResponse.getExplanation());
                factoryShipQnADto.setAnalysis(submitResponse.getAnalysis());


                Map<String, String> bundle = new HashMap<>();
                bundle.put("UserAnswer", "check In DynamoDB");
                bundle.put("type", factoryShipQnADto.getType());
                bundle.put("Question_Id", factoryShipQnADto.getQuestionId());
                bundle.put("Obtained_Marks", submitResponse.getMarks());
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.sav_Submission.getActionName(), bundle);

                boolean correct = factoryShipQnADto.getUserAnswer().equalsIgnoreCase(
                        factoryShipQnADto.getAnswer() == null ? "" : factoryShipQnADto.getAnswer().trim()
                );

                Log.d("SUBMIT_FLOW", "Correctness evaluated: " + correct);

                submitListener.onSubmit(Integer.parseInt(number) - 1, factoryShipQnADto, correct);
                handlePostHandler();
            } catch (Exception e) {
                Log.e("SUBMIT_FLOW", "Exception in response parsing: " + e.getMessage(), e);
            }

        }, new VolleyGlobalErrorListener(getContext()) {
            @Override
            public void postError(VolleyError error) {
                try {
                    Log.e("SUBMIT_FLOW", "Volley error: " + error.toString());

                    if (getActivity() != null && savBottomSheetDialog != null) {
                        savBottomSheetDialog.dismiss();
                    }

                    if (MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                        handleBottomSheet(true, false);
                    } else {
                        Log.e("SUBMIT_FLOW", "handleBottomSheet case");
                        savBottomSheetDialog = new SavBottomSheetDialog(bottomSheetListener);
                        Bundle bundle = new Bundle();
                        if (!CommonMethods.isOnline(getContext())) {
                            bundle.putBoolean("error", true);
                            bundle.putBoolean("backendError", false);
                        } else {
                            bundle.putBoolean("error", false);
                            bundle.putBoolean("backendError", true);
                        }
                        bundle.putBoolean("textExtracted", false);
                        bundle.putString("finalText", "");
                        savBottomSheetDialog.setArguments(bundle);
                        savBottomSheetDialog.show(getParentFragmentManager(), "");
                        savBottomSheetDialog.setCancelable(false);
                        submitListener.onBottomSheetOpen();
                    }

                    textExtracted = true;

                } catch (Exception e) {
                    Log.e("SUBMIT_FLOW", "Exception in postError: " + e.getMessage(), e);
                }
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(requireActivity());
        submitJsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                600000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        submitJsonObjectRequest.setTag(requestTag);
        requestQueue.add(submitJsonObjectRequest);
    }

    private String formatIdealAnswerText(String idealAnswer) {
        if (idealAnswer != null && !idealAnswer.isEmpty()) {
            String s = idealAnswer.replaceAll("</body></html>", "");
            s = s.replaceAll("<html><body>", "");
            return s;
        }
        return "";
    }

    private void handlePostHandler() {
        Log.d(TAG, "Answer Post handler");
        handleAnswerCheckDialog();
        handler = new Handler();
        runnable = () -> {
            handler.removeCallbacksAndMessages(null);
            answerCheckDialog.dismiss();
            setUpAnswerDetail();
            if (!MyApplication.getBoolData(PreferenceKeys.IS_REVIEW_TOOL_TIP_VISIBLE.getKey())) {
                MyApplication.saveData(PreferenceKeys.IS_REVIEW_TOOL_TIP_VISIBLE.getKey(), true);
                stepCount = 1;
            }
        };
        handler.postDelayed(runnable, 3000);
    }

    private void handleAnswerCheckDialog() {
        try {
            if (getActivity() != null && isAdded()) {
                if (savBottomSheetDialog != null) {
                    savBottomSheetDialog.dismiss();
                }
                answerCheckDialog.show();
                String totalMarks = factoryShipQnADto.getMarks();
                Log.d("AnswerCheck", "response : " + submitResponse.toString());
                yourScore.setText(getString(R.string.your_score) + ":" + CommonMethods.cleanDecimalNumber(Double.parseDouble(factoryShipQnADto.getMarksObtained())) + "/" + totalMarks);
                double percent = 0;
                try {
                    percent = ((Double.parseDouble(factoryShipQnADto.getMarksObtained()) * 100) / Integer.parseInt(totalMarks));
                } catch (Exception e) {
                    Log.e("AnswerCheck", e.getMessage(), e);
                }
                if (percent > 50) {
                    subtitleAnswerCheck.setText(getString(R.string.Bahot_Badhiya_Aise_hi_improve_karte_rahe));
                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) animationAnswerCheck.getLayoutParams();
                    layoutParams.setMargins(0, 20, 0, 20);
                    animationAnswerCheck.setAnimation(R.raw.high_marks);
                    mediaPlayer = MediaPlayer.create(requireActivity(), R.raw.rightanswer);
                } else {
                    subtitleAnswerCheck.setText(getString(R.string.Pareshan_mat_ho_Bas_practice_karte_raho));
                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) animationAnswerCheck.getLayoutParams();
                    layoutParams.setMargins(0, -80, 0, 100);
                    animationAnswerCheck.setScaleX(1.5f);
                    animationAnswerCheck.setScaleY(1.5f);
                    animationAnswerCheck.setLayoutParams(layoutParams);
                    animationAnswerCheck.setAnimation(R.raw.low_marks);
                    mediaPlayer = MediaPlayer.create(requireActivity(), R.raw.wronganswer);
                }
                mediaPlayer.start();
            }
        } catch (Exception e) {
            Log.e("AnswerCheck", e.getMessage(), e);
        }
    }

    private void setUpAnswerCheckDialog() {
        answerCheckDialog = new Dialog(requireActivity());
        answerCheckDialog.setContentView(R.layout.marks_dialog);
        answerCheckDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
        answerCheckDialog.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        answerCheckDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        answerCheckDialog.setCancelable(false);
        yourScore = answerCheckDialog.findViewById(R.id.marks_detail);
        animationAnswerCheck = answerCheckDialog.findViewById(R.id.animation);
        subtitleAnswerCheck = answerCheckDialog.findViewById(R.id.subtitle);
    }

    private void setUpAnswerDetail() {
        try {
            Log.d(TAG, "Setting Answer detail");
            if (factoryShipQnADto.isCompleted()
                    && !factoryShipQnADto.isReattempt()
                    && (StringUtils.isNotEmpty(factoryShipQnADto.getUserSolution()) || (StringUtils.isNotEmpty(factoryShipQnADto.getAnswer()) || StringUtils.isNotEmpty(factoryShipQnADto.getAnswerSolImage())))
                    && StringUtils.isNotEmpty(factoryShipQnADto.getUserAnswer())) {
                increaseCount = true;
                description.setVisibility(View.GONE);
                extractedTextLayout.setVisibility(View.GONE);
                bottomLayout.setVisibility(View.VISIBLE);
                submitText.setText(getString(R.string.next));
                clearAttempt.setVisibility(View.VISIBLE);
                answerImageLayout.setVisibility(View.GONE);
                submittedAnswerLayout.setVisibility(View.VISIBLE);
                uploadImageLayout.setVisibility(View.GONE);
                typeAnswerLayout.setVisibility(View.GONE);
                submittedAnswerText.setText(factoryShipQnADto.getUserAnswer());
                solutionView.setVisibility(View.VISIBLE);
//                askDoubt.setVisibility(View.VISIBLE);
                askDoubt.setVisibility((!MyApplication.isCompetitiveProfile() && !SubscriptionType.hasActiveSubscription()) ? View.GONE : View.VISIBLE);

                if (StringUtils.isNotEmpty(factoryShipQnADto.getQuestionLevel())) {
                    difficultyCard.setVisibility(View.VISIBLE);
                    String difficulty = getString(R.string.level_);
                    if (factoryShipQnADto.getQuestionLevel().equals(QuestionLevel.EASY.name())) {
                        difficulty = difficulty + getString(R.string.easy);
                    } else if (factoryShipQnADto.getQuestionLevel().equals(QuestionLevel.MEDIUM.name())) {
                        difficulty = difficulty + getString(R.string.medium);
                    } else if (factoryShipQnADto.getQuestionLevel().equals(QuestionLevel.HARD.name())) {
                        difficulty = difficulty + getString(R.string.hard);
                    }
                    difficultyLevel.setText(difficulty);
                }

                Log.d("factoryShipQnADto", "answerDetail :{}" + factoryShipQnADto.toString());

                if (StringUtils.isNotEmpty(factoryShipQnADto.getMarksObtained()) && StringUtils.isNotEmpty(factoryShipQnADto.getMarks())) {
                    marksCard.setVisibility(View.VISIBLE);
                    marksText.setText(getString(R.string.marks) + ": " + CommonMethods.cleanDecimalNumber(Double.parseDouble(factoryShipQnADto.getMarksObtained())) + "/" + factoryShipQnADto.getMarks());
                    double percent = 0;
                    try {
                        percent = ((Double.parseDouble(factoryShipQnADto.getMarksObtained())) * 100) / Double.parseDouble(factoryShipQnADto.getMarks());
                    } catch (Exception e) {
                        Log.e("AnswerCheck", e.getMessage(), e);
                    }
                    if (percent < 50) {
                        marksText.setTextColor(getResources().getColor(R.color.red));
                        marksCard.setCardBackgroundColor(getResources().getColor(R.color.mock_light_red));
                    } else if (percent <= 80) {
                        marksText.setTextColor(getResources().getColor(R.color.medium_marks_text));
                        marksCard.setCardBackgroundColor(getResources().getColor(R.color.medium_marks_card));
                    } else {
                        marksText.setTextColor(getResources().getColor(R.color.green));
                        marksCard.setCardBackgroundColor(getResources().getColor(R.color.mock_light_green));
                    }
                }
                if (StringUtils.isNotEmpty(factoryShipQnADto.getAnswerSolImage())) {
                    solutionImage.setVisibility(View.VISIBLE);
                    solutionImage.loadImage(factoryShipQnADto.getAnswerSolImage());
                    solutionImage.setOnClickListener(view -> {
                        fullScreenImageDialog = new FullScreenImageDialog(requireContext(),
                                factoryShipQnADto.getAnswerSolImage(), FullViewImageType.SOLUTION);
                        fullScreenImageDialog.show();
                    });
                } else {
                    solutionImage.setVisibility(View.GONE);
                }

                if (StringUtils.isNotEmpty(factoryShipQnADto.getAnalysis())) {
                    idealSolutionCard.setVisibility(View.VISIBLE);
                    analysisCard.setVisibility(View.VISIBLE);
                    solutionText.setText(HtmlCompat.fromHtml(factoryShipQnADto.getAnswer().trim(), HtmlCompat.FROM_HTML_MODE_LEGACY));
                    analysisText.setText(HtmlCompat.fromHtml(factoryShipQnADto.getAnalysis().trim(), HtmlCompat.FROM_HTML_MODE_LEGACY));
                } else if (StringUtils.isNotEmpty(factoryShipQnADto.getAnswer())) {
                    idealSolutionCard.setVisibility(View.GONE);
                    analysisCard.setVisibility(View.GONE);
                    solutionText.setVisibility(View.VISIBLE);
                    answerBox.setVisibility(View.VISIBLE);
                    solutionText.setText(HtmlCompat.fromHtml(factoryShipQnADto.getAnswer().trim(), HtmlCompat.FROM_HTML_MODE_LEGACY));
                } else {
                    answerBox.setVisibility(View.GONE);
                    solutionText.setVisibility(View.GONE);
                }
                dataLoaded = false;
                if (isShowFiveStepTooltip())
                    showFiveStepToolTip(1);
            }
        } catch (Exception e) {
            Log.d("setUpAnswerDetail:", e.getMessage());
        }
    }

    private void setIdealSolution(String idealSolution) {
        Glide.with(getContext())
                .asFile()
                .load(idealSolution)
                .into(new CustomTarget<File>() {
                    @Override
                    public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                        // Load image bounds first
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(resource.getAbsolutePath(), options);
                        int imageHeight = options.outHeight;
                        int imageWidth = options.outWidth;

                        int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;


                        solutionImage.setImage(ImageSource.uri(Uri.fromFile(resource)));
                        if (imageHeight > screenHeight) {
                            solutionImage.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_START);
                        } else {
                            solutionImage.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE);
                        }
                        solutionImage.setZoomEnabled(false);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }


    private void setClearAttempt() {

        TooltipHelper.getInstance().dismiss();
        factoryShipQnADto.setUserAnswer("");
        factoryShipQnADto.setReattempt(true);
        factoryShipQnADto.setClearAttempt(true);
        factoryShipQnADto.setReview(false);
        solutionView.setVisibility(View.GONE);
        typeAnswer.setText("");
        textExtracted = false;
        typeAnswerLayout.setVisibility(View.VISIBLE);
        extractedText.setText("");
        extractedTextLayout.setVisibility(View.GONE);
        uploadImageLayout.setVisibility(View.VISIBLE);
        imageUploadLayout.setVisibility(View.VISIBLE);
        clearAttempt.setVisibility(View.GONE);
        viewSolutionCard.setVisibility(View.GONE);
        askDoubt.setVisibility(View.GONE);
        bottomLayout.setVisibility(View.GONE);
        submitListener.onReattempt(Integer.parseInt(number) - 1);
        textCardLayout.setBackgroundColor(getResources().getColor(R.color.white));
        descriptionCard.setVisibility(View.GONE);
        description.setVisibility(View.VISIBLE);
        submitText.setText(getString(R.string.submit));
        editTextBack.setVisibility(View.VISIBLE);
        readText.setVisibility(View.GONE);
        submittedAnswerText.setText("");
        submittedAnswerLayout.setVisibility(View.GONE);
    }

    private void handleImagePickerDialog() {
        cropImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                        CropImage.ActivityResult cropResult = CropImage.getActivityResult(result.getData());
                        if (cropResult != null) {
                            Uri croppedImageUri = cropResult.getUri();
                            if (croppedImageUri != null) {
                                Log.d("croppedImageUri", "Path" + croppedImageUri.getPath());
                                float size = getImageSize(croppedImageUri);
                                if (size <= 15) {
                                    typeAnswerLayout.setVisibility(View.GONE);
                                    bottomLayout.setVisibility(View.VISIBLE);
                                    uploadImageLayout.setVisibility(View.GONE);
                                    answerImageLayout.setVisibility(View.VISIBLE);
                                    answerImageUri = croppedImageUri;
                                    Glide.with(requireContext()).load(croppedImageUri).into(answerImage);
                                    showSubmitTooltip();
                                } else {
                                    Toast.makeText(getActivity(), "size larger than 15mb", Toast.LENGTH_LONG).show();
                                }
                            }
                        } else {
                            Toast.makeText(requireContext(), getString(R.string.cropping_failed), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                        try {
                            if (currentPhotoPath != null && cropActivityLauncher != null) {
                                Intent cropIntent = new Intent(requireActivity(), CropImageActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putParcelable(CropImage.CROP_IMAGE_EXTRA_SOURCE, currentPhotoPath);
                                CropImageOptions cropImageOptions = new CropImageOptions();
                                cropImageOptions.guidelines = CropImageView.Guidelines.ON_TOUCH;
                                bundle.putParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS, cropImageOptions);
                                cropIntent.putExtra(CropImage.CROP_IMAGE_EXTRA_BUNDLE, bundle);
                                cropActivityLauncher.launch(cropIntent);
                                //CropImage.activity(currentPhotoPath).start(requireActivity(), this);
                            } else {
                                Toast.makeText(requireContext(), getString(R.string.please_try_again), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.d("handleImagePickerDialog", e.getMessage());
                        }
                    }
                }
        );

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK) {

                        if (result.getData() != null) {
                            Log.d("handleImagePickerDialog", result.getData().getStringArrayListExtra("url_list").toString());
                            ArrayList<String> urls = result.getData().getStringArrayListExtra("url_list");

                            if (!CollectionUtils.isEmpty(urls)) {
                                if (urls.size() > 1)
                                    Toast.makeText(getActivity(), "Maximum selection limit is 1", Toast.LENGTH_SHORT).show();
                                Uri photoUri = Uri.parse(urls.get(0));
                                Log.d("photoUri", "gallery = " + photoUri);
                                if (photoUri != null) {
                                    if (getImageSize(photoUri) <= 15) {
                                        typeAnswerLayout.setVisibility(View.GONE);
                                        bottomLayout.setVisibility(View.VISIBLE);
                                        uploadImageLayout.setVisibility(View.GONE);
                                        answerImageLayout.setVisibility(View.VISIBLE);
                                        answerImageUri = photoUri;
                                        Glide.with(requireContext()).load(photoUri).into(answerImage);
                                        showSubmitTooltip();
                                    } else {
                                        Toast.makeText(getActivity(), "size larger than 15mb", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        }
                    }
                }
        );

        imagePickDialog = new Dialog(requireActivity());
        imagePickDialog.setContentView(R.layout.action_popup_layout);
        imagePickDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
        imagePickDialog.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        imagePickDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        imagePickDialog.setCancelable(false);
        imagePickDialog.findViewById(R.id.close_btn).setOnClickListener(view -> imagePickDialog.cancel());
        TextView title = imagePickDialog.findViewById(R.id.title);
        title.setText(R.string.select_answer_s_images);
        TextView subtitle = imagePickDialog.findViewById(R.id.subtitle);
        subtitle.setVisibility(View.GONE);
        CardView btn1 = imagePickDialog.findViewById(R.id.btn1);
        CardView btn2 = imagePickDialog.findViewById(R.id.btn2);
        TextView btnText1 = imagePickDialog.findViewById(R.id.btn1_text);
        btnText1.setText(getString(R.string.choose_from_gallery));
        TextView btnText2 = imagePickDialog.findViewById(R.id.btn2_text);
        btnText2.setText(getString(R.string.click_a_photo));
        LottieAnimationView lottieAnimationView = imagePickDialog.findViewById(R.id.animation);
        lottieAnimationView.setVisibility(View.GONE);
        ImageView selectionImage = imagePickDialog.findViewById(R.id.selection_image);
        selectionImage.setVisibility(View.VISIBLE);

        btn1.setOnClickListener(view -> {
            openGallery();
            imagePickDialog.cancel();
        });
        btn2.setOnClickListener(view -> {
            imagePickDialog.cancel();
            checkCameraPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
        });
    }

    private float getImageSize(Uri imageUri) {
        String[] projection = {MediaStore.Images.Media.SIZE};
        Cursor cursor = getActivity().getContentResolver().query(imageUri, projection, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int columnIndexSize = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
            long fileSizeInBytes = cursor.getLong(columnIndexSize);
            cursor.close();
            return (float) (fileSizeInBytes / 1024) / 1024;
        } else {
            return 0.0f;
        }
    }

    private void openGallery() {
        Intent intent = new Intent(getActivity(), ImageCropperActivity.class);
        if (getActivity() != null)
            Toast.makeText(getActivity().getApplicationContext(), String.format(getString(R.string.You_can_select_up_to_images), 1), Toast.LENGTH_LONG).show();
        intent.putExtra("allowedImages", 1);
        galleryLauncher.launch(intent);
    }

    public void checkCameraPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(requireActivity(), permission) == PackageManager.PERMISSION_DENIED) {
            cameraPermissionLauncher.launch(permission);
        } else {
            captureImage();
        }
    }

    private void captureImage() {
        String filename = "photo";
        File storageDirectory = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            File imageFile = File.createTempFile(filename, "_" + timeStamp + "_" + ".jpg", storageDirectory);
            Uri imageUri = FileProvider.getUriForFile(
                    requireContext().getApplicationContext(),
                    BuildConfig.APPLICATION_ID + ".provider",
                    imageFile
            );
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                currentPhotoPath = imageUri;
                cameraLauncher.launch(intent);
            } else {
                Toast.makeText(requireContext(), "No camera app found!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void extractTextFromImage() {
        Map<String, Object> params = new HashMap<>();
        params.put("savType", factoryShipQnADto.isSavType());

        MyMultipartRequest multipartRequest = new MyMultipartRequest(Request.Method.POST,
                CommonMethods.getUrlParam(EXTRACT_TEXT_FROM_IMAGE_URI, params), response -> {
            try {
                if (getContext() != null) {
                    if (savBottomSheetDialog != null) {
                        savBottomSheetDialog.dismiss();
                    }
                }
                TypeToken<ImageTextExtractionResponse> token = new TypeToken<ImageTextExtractionResponse>() {
                };
                Gson gson = new Gson();
                ImageTextExtractionResponse extractionResponse = gson.fromJson(new String(response.data,
                        "UTF-8"), token.getType());
                if (StringUtils.isNotEmpty(extractionResponse.getMessage()) &&
                        extractionResponse.isTextExtracted()) {
                    Log.d("MULTIPARTRES", "RESPONSE : " + extractionResponse.getMessage());
                    answerImageLayout.setVisibility(View.GONE);
                    extractedTextLayout.setVisibility(View.VISIBLE);
                    extractedText.requestFocus();
                    extractedText.setEnabled(true);
                    extractedText.setText(extractionResponse.getMessage());
                    if (!CollectionUtils.isEmpty(extractionResponse.getUrls())) {
                        userAnswerImageUrl = extractionResponse.getUrls();
                    }
                    if(StringUtils.isNotEmpty(extractionResponse.getRequestId())) {
                        questionRequestIdFromML = extractionResponse.getRequestId();
                        questionSessionIdFromML= extractionResponse.getSessionId();
                    }
                    userAnswer = extractionResponse.getMessage().trim();
                    handleCount(extractionResponse.getMessage().split("\\s+").length);
                    textExtracted = true;
                    startThreeStepTooltip();
                } else {
                    userAnswer = "";
                    textExtracted = false;
                    handleBottomSheet(false, true);
                }
            } catch (Exception e) {
                Log.e("MULTIPARTRES", e.getMessage(), e);
            }

        }, error -> {
            Log.d("submitFlow", "extractTextFromImage: " + error.getMessage());
            Log.d("MULTIPARTRES", "RESPONSE : " + error.toString());
            if (MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                Log.d("submitFlow", "extractTextFromImageBottomsheet: " + "true");
                handleBottomSheet(true, false);
            } else {
                Log.d("submitFlow", "extractTextFromImageBottomsheet: ");
                handleBottomSheet(false, true);
            }

            textExtracted = false;

        }) {
            @Override
            protected Map<String, MultipartRequestDataPart> getByteData() {
                Map<String, MultipartRequestDataPart> params = new HashMap<>();
                try {
                    Uri uri = answerImageUri;
                    File file = FileUtil.from(getActivity(), uri);
                    byte[] fileData = CommonMethods.readFileToByteArray(file);
                    String fileName = file.getName();
                    String contentType = CommonMethods.getMimeType(fileName);
                    params.put("file", new MultipartRequestDataPart(fileName, fileData, contentType));
                } catch (Exception e) {
                    Log.e("ErrorInExtract", e.getMessage(), e);
                }
                return params;
            }
        };
        Volley.newRequestQueue(getContext()).add(multipartRequest).setRetryPolicy(new DefaultRetryPolicy(
                600000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }


    @Override
    public void onGalleryClick(boolean textNotFound) {
        Log.d("onGalleryClick", "Called with textNotFound = " + textNotFound);
        textExtracted = false;
        if (getContext() != null) {
            if (savBottomSheetDialog != null) {
                savBottomSheetDialog.dismiss();
            }
        }
//        if (textNotFound) {
//            imageUploadLayout.setVisibility(View.VISIBLE);
//            answerImageLayout.setVisibility(View.GONE);
//        }
        openGallery();
    }

    @Override
    public void onPictureClick(boolean textNotFound) {
        textExtracted = false;
        if (getContext() != null) {
            if (savBottomSheetDialog != null) {
                savBottomSheetDialog.dismiss();
            }
        }
//        if (textNotFound) {
//            imageUploadLayout.setVisibility(View.VISIBLE);
//            answerImageLayout.setVisibility(View.GONE);
//        }
        checkCameraPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
    }

    @Override
    public void onCloseClick() {
        submitListener.onBottomSheetClose();
    }

    @Override
    public void onDismiss() {
        submitListener.onBottomSheetClose();
    }

    private boolean isReviewMode() {
        return factoryShipQnADto.isCompleted()
                && !factoryShipQnADto.isReattempt()
                && (StringUtils.isNotEmpty(factoryShipQnADto.getUserSolution()))
                && StringUtils.isNotEmpty(factoryShipQnADto.getUserAnswer());
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "OnResume");
        handleTooltipOnResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (answerCheckDialog != null)
            answerCheckDialog.dismiss();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (runnable != null && handler != null) {
            handler.removeCallbacks(runnable);
        }
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
                        .fragmentNumber(Integer.parseInt(number))
                        .currentFragment(MyApplication.getInstance().getTooltipViewModel().getCurrentFragment())
                        .build();
                TooltipHelper.getInstance().handleTooltip(scrollView, submit,
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
            if (!TooltipHelper.getInstance().isTooltipCompleted(requireActivity(), false)) {
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
                            clickTooltipStatus.setTooltipType(TooltipType.OPTION);
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
        final View rootView = view.findViewById(R.id.parent_layout);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            int screenHeight = rootView.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;
            if (keypadHeight > screenHeight * 0.15) {
                isKeyBoardActive = true;
                Log.d("KeyboardStatus", "Keyboard is opened");
                TooltipHelper.getInstance().dismiss();
            } else if (textExtracted) {
                handleTooltipOnKeyBoardHide();
            }
        });
    }

    private boolean isCorrectFragment(TooltipStatusDto tooltipStatusDto) {
        try {
            return (tooltipStatusDto != null && (tooltipStatusDto.getFragment() == -1 || tooltipStatusDto.getFragment() == Integer.parseInt(number))) &&
                    tooltipStatusDto.getCurrentFragment() == Integer.parseInt(number) &&
                    TooltipHelper.getInstance().countNotCompleted(tooltipStatusDto.getTooltipType(), getActivity());
        } catch (Exception ignored) {
        }
        return false;
    }

    private void handleTooltipOnKeyBoardHide() {
        if (isKeyBoardActive && isCorrectFragment(uploadTooltipStatus)) {
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
        if (isShowThreeStepTooltip()) {
            startTextHandler();
        }
        if (isShowFiveStepTooltip() && reviewTooltipStatus != null) {
            checkVisibility(reviewTooltipStatus.getStepCount());
        }
    }

    private void handleTooltipOnResume() {
        if (isShowClickTooltip()) {
            checkViewLoading();
        }
        if (isShowFiveStepTooltip() && reviewTooltipStatus != null) {
            showFiveStepToolTip(reviewTooltipStatus.getStepCount());
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
        uploadButtonLayout.post(onAllViewsReady);
    }

    private void checkClickHereTooltip() {
        try {
            MyTooltipDto myTooltipDto = MyTooltipDto.builder()
                    .text(getString(R.string.Choose_an_option_to_answer))
                    .isAboveAnchor(false)
                    .alignment(RelativeLayout.CENTER_HORIZONTAL)
                    .autoDismiss(true)
                    .tooltipType(TooltipType.OPTION)
                    .fragmentNumber(Integer.parseInt(number))
                    .hideArrow(true)
                    .yMargin(40)
                    .currentFragment(MyApplication.getInstance().getTooltipViewModel().getCurrentFragment())
                    .build();
            TooltipHelper.getInstance().handleTooltip(scrollView, typeAnswer,
                    myTooltipDto, this, requireActivity());
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
            return !isReviewMode() && StringUtils.isNotEmpty(subjectCode) && !subjectCode.equals("Biology") &&
                    clickTooltipStatus != null && !clickTooltipStatus.isActive() &&
                    TooltipHelper.getInstance().countNotCompleted(TooltipType.OPTION, getActivity()) &&
                    scrollView != null && uploadButtonLayout != null;
        } catch (Exception ignored) {
        }
        return false;
    }

    private boolean isShowThreeStepTooltip() {
        if (uploadTooltipStatus != null) {
            return isCorrectFragment(uploadTooltipStatus) &&
                    !uploadTooltipStatus.isFinished() && !isReviewMode() && !isKeyBoardActive && textExtracted;
        }
        return false;
    }

    private boolean isShowFiveStepTooltip() {
        if (reviewTooltipStatus != null) {
            return isCorrectFragment(reviewTooltipStatus) && !reviewTooltipStatus.isFinished() &&
                    isReviewMode() && analysisTitle != null && analysisTitle.getVisibility() == View.VISIBLE;
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
            showToolTipAfterImageUpload(Math.min(stepCount, getTotalCount()), getTotalCount());
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
                    showToolTipAfterImageUpload(submit, RelativeLayout.ALIGN_END, true, stepCount, totalSteps);
                }
                break;
            case 3:
                showToolTipAfterImageUpload(submit, RelativeLayout.ALIGN_END, true, stepCount, totalSteps);
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
                    .fragmentNumber(Integer.parseInt(number))
                    .tooltipType(TooltipType.UPLOAD)
                    .currentFragment(MyApplication.getInstance().getTooltipViewModel().getCurrentFragment())
                    .build();
            TooltipHelper.getInstance().handleTooltip(scrollView, view,
                    myTooltipDto, this, requireActivity());
        } catch (Exception e) {
            Log.d("checkClickHereTooltip", e.getMessage());
        }

    }

    private void checkVisibility(int stepCount) {
        reviewTooltipStatus.setStepCount(stepCount);
        Log.d(TAG, "ToolTip checkVisibility: "+stepCount);
        TooltipHelper.getInstance().dismiss();
        switch (stepCount) {
            case 1 -> scrollView.post(() -> {
                if (TooltipHelper.getInstance().isViewFullyVisibleInScrollView(analysisTitle, scrollView)) {
                    showFiveStepToolTip(analysisTitle, RelativeLayout.ALIGN_START, 1);
                }
            });
            case 2 -> scrollView.post(() -> {
                if (TooltipHelper.getInstance().isViewFullyVisibleInScrollView(idealTitle, scrollView)) {
//                    scrollView.smoothScrollTo(0, idealSolutionCard.getTop());
                    showFiveStepToolTip(idealTitle, RelativeLayout.ALIGN_START, 2);
                }
            });
            case 3 -> scrollView.post(() -> {
                if (TooltipHelper.getInstance().isViewFullyVisibleInScrollView(submittedAnswerLayout, scrollView)) {
//                    scrollView.smoothScrollTo(0, submittedAnswerLayout.getTop());
                    showFiveStepToolTip(submittedAnswerLayout, RelativeLayout.ALIGN_START, 3);
                }
            });
            case 4 -> showFiveStepToolTip(askDoubtCard, RelativeLayout.ALIGN_END, 4);
            case 5 -> showFiveStepToolTip(submit, RelativeLayout.ALIGN_END, 5);
        }
    }

    private void scrollFiveStepToolTip(int stepCount) {
        reviewTooltipStatus.setStepCount(stepCount);
        switch (stepCount) {
            case 1 -> scrollView.post(() -> {
//                scrollView.smoothScrollTo(0, analysisCard.getTop());
                scrollView.smoothScrollTo(0, 0);
            });
            case 2 -> scrollView.post(() -> {
                scrollView.smoothScrollTo(0, idealSolutionCard.getTop());
            });
            case 3 -> scrollView.post(() -> {
                scrollView.smoothScrollTo(0, submittedAnswerLayout.getTop());
            });
        }
        showFiveStepToolTip(stepCount);
    }

    private void showFiveStepToolTip(int stepCount) {
        if (stepCount == 0)
            stepCount = 1;
        Log.d(TAG, "ToolTip: showFiveStepToolTip: "+stepCount);
        reviewTooltipStatus.setStepCount(stepCount);
//        TooltipHelper.getInstance().dismiss();
        switch (stepCount) {
            case 1 -> scrollView.post(() -> {
                scrollView.postDelayed(() ->
                        showFiveStepToolTip(analysisTitle,
                                RelativeLayout.ALIGN_START, 1), 1);
            });
            case 2 -> scrollView.post(() -> {
                scrollView.postDelayed(() -> showFiveStepToolTip(idealTitle,
                        RelativeLayout.ALIGN_START, 2), 2);
            });
            case 3 -> scrollView.post(() -> {
                scrollView.postDelayed(() -> showFiveStepToolTip(submittedAnswerLayout,
                        RelativeLayout.ALIGN_START, 3), 300);
            });
            case 4 -> showFiveStepToolTip(askDoubtCard, RelativeLayout.ALIGN_END, 4);
            case 5 -> showFiveStepToolTip(submit, RelativeLayout.ALIGN_END, 5);
        }
    }

    private void showFiveStepToolTip(View view, int alignment, int stepCount) {
        try {
            Log.d(TAG, "ToolTip showFiveStepToolTip: Alignment: "+alignment+", Step: "+stepCount);
            MyTooltipDto myTooltipDto = MyTooltipDto.builder()
                    .alignment(alignment)
                    .isAboveAnchor(true)
                    .autoDismiss(false)
                    .stepCount(stepCount)
                    .totalSteps(5)
                    .showNext(true)
                    .tooltipType(TooltipType.REVIEW)
                    .fragmentNumber(Integer.parseInt(number))
                    .currentFragment(MyApplication.getInstance().getTooltipViewModel().getCurrentFragment())
                    .build();
            Log.d(TAG, "ToolTip MyTooltipDto: "+myTooltipDto.toString());
            TooltipHelper.getInstance().handleTooltip(scrollView, view,
                    myTooltipDto, this, requireActivity());
        } catch (Exception e) {
            Log.d("checkClickHereTooltip", e.getMessage());
        }
    }

    @Override
    public void onToolTipClick() {

        TooltipHelper.getInstance().dismiss();
        if (uploadTooltipStatus != null && uploadTooltipStatus.getStepCount() == uploadTooltipStatus.getTotalSteps()) {
            finishUploadTooltip();
        } else {
            if (uploadTooltipStatus != null)
                showToolTipAfterImageUpload(uploadTooltipStatus.getStepCount() + 1, getTotalCount());
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
        } else {
            if (reviewTooltipStatus != null)
                scrollFiveStepToolTip(reviewTooltipStatus.getStepCount() + 1);
        }
    }

    private void finishReviewTooltip(boolean isSkip) {
        TooltipHelper.getInstance().dismiss();
        if (reviewTooltipStatus != null && !reviewTooltipStatus.isFinished() &&
                (reviewTooltipStatus.getStepCount() == 5 || isSkip)) {
            reviewTooltipStatus.setFinished(true);
            MyApplication.getInstance().getTooltipViewModel().getReviewTooltipStatus().setValue(reviewTooltipStatus);
            finishTooltip(TooltipType.REVIEW);
        }
    }

    private void finishUploadTooltip() {
        if (uploadTooltipStatus != null && !uploadTooltipStatus.isFinished() &&
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