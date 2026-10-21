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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import arivihan.technologies.doubtbuzzter2.adapter.UploadedImageRecyclerAdapter;
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
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionType;
import arivihan.technologies.doubtbuzzter2.listener.SubmitListener;
import arivihan.technologies.doubtbuzzter2.model.FactoryShipQnADto;
import arivihan.technologies.doubtbuzzter2.model.ImageTextExtractionResponse;
import arivihan.technologies.doubtbuzzter2.model.LectureContentType;
import arivihan.technologies.doubtbuzzter2.model.MyTooltipDto;
import arivihan.technologies.doubtbuzzter2.model.ShortLongAnswerSubmitResponse;
import arivihan.technologies.doubtbuzzter2.model.TooltipStatusDto;
import arivihan.technologies.doubtbuzzter2.model.TooltipType;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyMultipartRequest;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class SubjectiveTypeFragment extends Fragment implements MyToolTip.ToolTipClickListener, UploadedImageRecyclerAdapter.AddClickListener,
        SavBottomSheetDialog.BottomSheetListener {
    private static final String TAG = SubjectiveTypeFragment.class.getSimpleName();
    CardView submit;
    CardView clearAttempt;
    TextView submitText;
    FactoryShipQnADto factoryShipQnADto;
    String number;
    String total;
    SubmitListener submitListener;
    TextView questionNumber;
    TextView tagText;
    RelativeLayout upload_image_layout;
    CardView tagCard, uploadSolutionBtn;
    TextView questionText;
    ImageView questionImage;
    View solutionView;
    LinearLayout answerBox;
    TextView solutionText, analysisText;
    ImageLoaderUtil solutionImage;
    CardView difficultyCard;
    TextView difficultyLevel;
    ImageView reportQuestion;
    CardView viewSolutionCard;
    TextView viewSolutionCardText;
    ScrollView scrollView;
    ConstraintLayout askDoubt;
    CardView askDoubtCard;
    CardView marksCard;
    TextView marksText, imageUploadText;
    ViewTreeObserver observer;
    boolean dataLoaded;
    SavBottomSheetDialog.BottomSheetListener bottomSheetListener;

    LinearLayout uploadTextLayout;
    LinearLayout imageUploadLayout;
    LinearLayout answerImageLayout;
    FullScreenImageDialog fullScreenImageDialog;
    MaterialCardView cv_view_solution;
    TextView tv_view_solution, tv_solution;
    RelativeLayout rl_solution;
    CardView cv_solution;
    ImageLoaderUtil ilu_solution_image_loader;

    RelativeLayout uploadButtonRelative;
    RecyclerView answerImageRv;
    UploadedImageRecyclerAdapter uploadedImageRecyclerAdapter;
    ArrayList<Uri> list = new ArrayList<>();
    Dialog imagePickDialog;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> cropImageLauncher;
    private ActivityResultLauncher<Intent> cropActivityLauncher;
    boolean isSingleImageSelection;
    int croppingImageIndex = -1;
    boolean crop;
    Uri currentPhotoPath;
    boolean textExtracted;
    LinearLayout extractedTextLayout;
    EditText extractedText;
    MaterialCardView editTextCard;
    CardView editTextBack;
    String finalText;
    TextView countText;
    TextView description;
    CardView descriptionCard;
    MaterialCardView textCard;
    ConstraintLayout textCardLayout;
    TextView readText;
    ShortLongAnswerSubmitResponse submitResponse;
    Dialog answerCheckDialog;
    LottieAnimationView animationAnswerCheck, doubtAnimation;
    TextView yourScore;
    TextView subtitleAnswerCheck;
    SavBottomSheetDialog savBottomSheetDialog;
    boolean readMore;
    MediaPlayer mediaPlayer;

    int maxLines = 15;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    String requestTag = "submitRequestTag";
    MyJsonObjectRequest submitJsonObjectRequest;
    MyMultipartRequest multipartRequest;
    boolean showToast = true;

    Handler handler;
    Runnable runnable;
    Dialog actionDialog;
    MaterialCardView analysisCard;
    MaterialCardView idealSolutionCard;
    CardView cv_ideal_solution_close;
    TextView solution_title;
    TextView analysisTitle, idealTitle;
    private ConstraintLayout bottomLayout;
    private boolean isKeyBoardActive;
    private TooltipStatusDto uploadTooltipStatus = new TooltipStatusDto();
    private TooltipStatusDto reviewTooltipStatus = new TooltipStatusDto();
    private TooltipStatusDto clickTooltipStatus = new TooltipStatusDto();
    private TooltipStatusDto submitTooltipStatus = new TooltipStatusDto();
    NetworkStatusLiveData networkStatusLiveData;
    List<String> userAnswerImageUrl;
    boolean isViewSolutionOpened = false;
    String subjectCode;
    String questionRequestIdFromML;
    String questionSessionIdFromML;


    public SubjectiveTypeFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            submitListener = (SubmitListener) context;
        } catch (ClassCastException e) {
            //throw new ClassCastException(context.getClass().toString() + " must implement RegWizardCallback ");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getActivity() != null && isAdded() && !getActivity().isFinishing()) {
            if (savBottomSheetDialog != null && savBottomSheetDialog.isAdded()) {
                savBottomSheetDialog.dismissAllowingStateLoss();
                savBottomSheetDialog = null;
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

    public static SubjectiveTypeFragment newInstance(FactoryShipQnADto factoryShipQnADto, String number, String total,String subjectCode) {
        SubjectiveTypeFragment fragment = new SubjectiveTypeFragment();
        Bundle args = new Bundle();
        args.putParcelable("factoryShipQnADto", factoryShipQnADto);
        args.putString(PreferenceKeys.PHONE_NUMBER.getKey(), number);
        args.putString("total", total);
        args.putString("subjectCode", subjectCode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            factoryShipQnADto = getArguments().getParcelable("factoryShipQnADto");
            number = getArguments().getString(PreferenceKeys.PHONE_NUMBER.getKey());
            total = getArguments().getString("total");
            subjectCode=getArguments().getString("subjectCode");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_subjective_type, container, false);
        factoryShipQnADto.setWordLimit(updateWordLimit(factoryShipQnADto));
        setUpViews(view);
        Log.d(TAG, "DTO: " + factoryShipQnADto);
        bottomSheetListener = this;
        observeViewModelForTooltip(view);
        setUpQuestionDetail();
        handleImagePickerDialog();
        handleEditText();
        setUpRecyclerView();
        setUpAnswerCheckDialog();
        handleCameraPermissions();
        setUpAnswerDetail();
        handleCropActivityLauncher();
        return view;
    }

    private String updateWordLimit(FactoryShipQnADto dto) {
        long defaultLimit = 300;
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
                            addImage(currentPhotoPath, getImageSize(currentPhotoPath), list.size());
                            if (uploadedImageRecyclerAdapter != null) {
                                uploadedImageRecyclerAdapter.notifyDataSetChanged();
                            }
                            showSubmitTooltip();
                        }
                    } else {
                        Log.d("CropActivity", "Cropping was canceled or failed");
                    }
                }
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        handleTooltipOnResume();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    private void setUpViews(View view) {
        bottomLayout = view.findViewById(R.id.bottom_layout);
        analysisTitle = view.findViewById(R.id.analysis_title);
        idealTitle = view.findViewById(R.id.ideal_title);
        solution_title = view.findViewById(R.id.solution_title);
        solution_title.setVisibility(View.GONE);
        analysisCard = view.findViewById(R.id.analysis_card);
        idealSolutionCard = view.findViewById(R.id.ideal_solution_card);
        cv_ideal_solution_close = view.findViewById(R.id.cv_ideal_solution_close);
        uploadButtonRelative = view.findViewById(R.id.upload_button_relative);
//        parentLayout = view.findViewById(R.id.parent_layout);
        textCard = view.findViewById(R.id.text_card);
        textCardLayout = view.findViewById(R.id.text_card_layout);
        description = view.findViewById(R.id.description);
        descriptionCard = view.findViewById(R.id.description_card);
        countText = view.findViewById(R.id.count_text);
        extractedTextLayout = view.findViewById(R.id.text_layout);
        readText = view.findViewById(R.id.read_text);
        extractedText = view.findViewById(R.id.extracted_text);
        editTextBack = view.findViewById(R.id.edit_text_back);
        editTextBack.setOnClickListener(v -> {
            TooltipHelper.getInstance().dismiss();
            textExtracted = false;
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.BELOW, R.id.solution_view);
            description.setVisibility(View.VISIBLE);
            extractedTextLayout.setLayoutParams(params);
            extractedText.setMaxLines(maxLines);
            readText.setText(getString(R.string.Read_More));
            readMore = false;
            extractedTextLayout.setVisibility(View.GONE);
            factoryShipQnADto.setUserAnswer("");
            answerImageLayout.setVisibility(View.VISIBLE);
            submit.setEnabled(true);
            submit.setCardBackgroundColor(getResources().getColor(R.color.subscription_blue));
        });
        answerImageRv = view.findViewById(R.id.answer_image_rv);
        answerImageLayout = view.findViewById(R.id.answer_image_layout);
        imageUploadLayout = view.findViewById(R.id.image_upload_layout);
        uploadSolutionBtn = view.findViewById(R.id.upload_solution_btn);
        upload_image_layout = view.findViewById(R.id.upload_image_layout);
//        imageUploadLayout.setOnClickListener(v -> imagePickDialog.show());
        upload_image_layout.setOnClickListener(v -> {
            TooltipHelper.getInstance().dismiss();
            imagePickDialog.show();
        });
        marksText = view.findViewById(R.id.marks_text);
        marksCard = view.findViewById(R.id.marks_card);
        askDoubt = view.findViewById(R.id.ask_doubt_fab);
        askDoubtCard = view.findViewById(R.id.ask_doubt_card);
        doubtAnimation = view.findViewById(R.id.animation);
        viewSolutionCard = view.findViewById(R.id.view_solution_card);
        viewSolutionCardText = view.findViewById(R.id.view_solution_card_text);
        viewSolutionCardText.setText(getString(R.string.View_Your_Answer));
        questionNumber = view.findViewById(R.id.question_number);
        tagText = view.findViewById(R.id.tag_text);
        tagCard = view.findViewById(R.id.tag_card);
        questionText = view.findViewById(R.id.question_text);
        questionImage = view.findViewById(R.id.image_question);
        solutionView = view.findViewById(R.id.solution_view);
        answerBox = view.findViewById(R.id.solutionBox);
        analysisText = view.findViewById(R.id.analysis_text);
        solutionText = view.findViewById(R.id.solution_text_sav);
        view.findViewById(R.id.solution_text).setVisibility(View.GONE);
        solutionImage = view.findViewById(R.id.solution_image_sav);
        difficultyCard = view.findViewById(R.id.difficulty_card);
        difficultyLevel = view.findViewById(R.id.difficulty_type);
        reportQuestion = view.findViewById(R.id.report_question);
        imageUploadText = view.findViewById(R.id.upload_image_text);

        cv_view_solution = view.findViewById(R.id.cv_view_solution);
        tv_view_solution = view.findViewById(R.id.tv_view_solution);
        tv_solution = view.findViewById(R.id.tv_solution);
        rl_solution = view.findViewById(R.id.rl_solution);
        cv_solution = view.findViewById(R.id.cv_solution);
        ilu_solution_image_loader = view.findViewById(R.id.ilu_solution_image_loader);

        String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        SpannableString spannableString = new SpannableString(getString(R.string.upload_answer_aur_board_expert));
        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.spannable_teal_color)), 0, "hi".equals(lang) ? 16 : 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        imageUploadText.setText(spannableString);

        if (StringUtils.isNotEmpty(factoryShipQnADto.getYear())) {
            tagCard.setVisibility(View.VISIBLE);
            tagText.setText(factoryShipQnADto.getYear());
        } else {
            tagCard.setVisibility(View.GONE);
        }

        questionImage.setOnClickListener(V -> {
            Log.d("questionImage", "setUpViews: clicked questionImage");
            fullScreenImageDialog = new FullScreenImageDialog(requireContext(),
                    factoryShipQnADto.getImageQuestion(), FullViewImageType.QUESTION);
            fullScreenImageDialog.show();
        });

        reportQuestion.setOnClickListener(v -> submitListener.reportQuestion());
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
                        subjectCode,
                        factoryShipQnADto.getQuestion(),
                        factoryShipQnADto.getUserAnswer(),
                        factoryShipQnADto.getAnswer(),
                        factoryShipQnADto.getAnalysis(),
                        factoryShipQnADto.getAnswerEx(),
                        factoryShipQnADto.getMarksObtained(),
                        factoryShipQnADto.getMarks() ,
                        factoryShipQnADto.getType()  ,
                        factoryShipQnADto.getImageExplanationLatex(),
                        factoryShipQnADto.getImageQuestion(),
                        factoryShipQnADto.getAnswerSolImage(),
                        factoryShipQnADto.getChapterCode(),
                        factoryShipQnADto.getCode()
                );

                if (reviewTooltipStatus != null && reviewTooltipStatus.getStepCount() == 4)
                    goToNextTooltip();
            } catch (Exception e) {
                Log.i("ARIVIHAN_EXCEPTION", "Unable to start activity WebviewDoubtChatScreen.class");
            }
        });
        scrollView = view.findViewById(R.id.scroll_view);
        submitText = view.findViewById(R.id.submit_text);
        clearAttempt = view.findViewById(R.id.clear_attempt_card);
        clearAttempt.setOnClickListener(view1 -> setClearAttempt());
        submit = view.findViewById(R.id.submit_card);
        submit.setOnClickListener(v -> {
            TooltipHelper.getInstance().dismiss();
            if (!MyApplication.isNetworkAvailable()) {
                handleBottomSheet(true, false);
            } else {
                if (MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    if(isViewSolutionOpened) {
                        submitListener.openNext();
                        return;
                    }
                    if (!factoryShipQnADto.isReattempt() && factoryShipQnADto.isCompleted()) {
                        finishReviewTooltip(false);
                        submitListener.openNext();
                    } else {
                        if (!textExtracted) {
                            if (list.isEmpty()) {
                                factoryShipQnADto.setUserAnswer("");
                                boolean correct = factoryShipQnADto.getUserAnswer().equalsIgnoreCase(factoryShipQnADto.getAnswer().trim());
                                submitListener.onSubmit(Integer.parseInt(number), factoryShipQnADto, correct);
                            } else {
                                resetUploadTooltip();
                                handleBottomSheet(false, false);
                                extractTextFromImage();
                            }
                        } else {
                            if (StringUtils.isNotEmpty(extractedText.getText().toString().trim())) {
                                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                                params.addRule(RelativeLayout.BELOW, R.id.solution_view);
                                extractedTextLayout.setLayoutParams(params);
                                showToast = true;
                                finishUploadTooltip();
                                resetReviewTooltip();
                                handleBottomSheet(false, false);
                                submitAnswerAndGetSolution();
                            } else {
                                CommonMethods.showNegativeSnackbar(requireView(), getString(R.string.submit_karne_se_pehle_answer_check_krein), 2000);
                            }

                        }
                    }
                } else {
                    CommonMethods.showNegativeSnackbar(submit, getString(R.string.NoInternetConnection));
                }
            }
        });
        if (StringUtils.isNotEmpty(finalText)) {
            imageUploadLayout.setVisibility(View.GONE);
            cv_view_solution.setVisibility(View.GONE);
            answerImageLayout.setVisibility(View.GONE);
            extractedTextLayout.setVisibility(View.VISIBLE);
            extractedText.setText(finalText.trim());
            handleCount(finalText.split("_").length);
        } else if (!CollectionUtils.isEmpty(list)) {
            if (textExtracted) {
                imageUploadLayout.setVisibility(View.GONE);
                cv_view_solution.setVisibility(View.GONE);
                answerImageLayout.setVisibility(View.GONE);
            } else {
                imageUploadLayout.setVisibility(View.GONE);
                cv_view_solution.setVisibility(View.GONE);
                answerImageLayout.setVisibility(View.VISIBLE);
            }
        }
        if (factoryShipQnADto.isCompleted() && !factoryShipQnADto.isReattempt()) {
            imageUploadLayout.setVisibility(View.GONE);
            cv_view_solution.setVisibility(View.GONE);
            description.setVisibility(View.GONE);
            descriptionCard.setVisibility(View.VISIBLE);
            answerImageLayout.setVisibility(View.GONE);
            extractedTextLayout.setVisibility(View.VISIBLE);
            extractedText.setText(factoryShipQnADto.getUserAnswer());
            extractedText.setEnabled(false);
            extractedText.clearFocus();
            submitText.setText(R.string.next);
            bottomLayout.setVisibility(View.VISIBLE);
            clearAttempt.setVisibility(View.VISIBLE);
            editTextBack.setVisibility(View.GONE);
            textCardLayout.setBackgroundColor(getResources().getColor(R.color.text_background));
            finalText = factoryShipQnADto.getUserAnswer();
            if (StringUtils.isNotEmpty(finalText)) {
                handleCount(finalText.split("\\s+").length);
            }
            Log.d("factoryShipQnADto", "UserSolution : " + factoryShipQnADto.getUserSolution() + " Marks Obtained :" +
                    factoryShipQnADto.getMarksObtained());
        }

        viewSolutionCard.setOnClickListener(v -> {
            factoryShipQnADto.setHideViewSolution(true);
            submitListener.onViewSolutionClick();
            viewSolutionCard.setVisibility(View.GONE);
            scrollView.fullScroll(View.FOCUS_DOWN);
        });

        cv_view_solution.setVisibility(MyApplication.getFirebaseRemoteConfig().getBoolean("practise_problem_view_solution_visibility")?View.VISIBLE:View.GONE);

        cv_view_solution.setOnClickListener(v->{
            setViewSolutionCard();
        });

        cv_ideal_solution_close.setOnClickListener(v->{
            setViewSolutionCard();
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                if (scrollView.canScrollVertically(1)
                        && !factoryShipQnADto.isReattempt()
                        && factoryShipQnADto.isCompleted()
                        && !factoryShipQnADto.isHideViewSolution()) {
                    viewSolutionCard.setVisibility(View.VISIBLE);
                } else {
                    viewSolutionCard.setVisibility(View.GONE);
                }
                handleTooltipOnScroll();

//                 if (scrollY == 0) {
//                     // Scrolling up
//                     askDoubtCard.setVisibility(View.VISIBLE);
//                     doubtAnimation.setVisibility(View.VISIBLE);
//                 } else {
//                     // Scrolling down
//                     askDoubtCard.setVisibility(View.GONE);
//                     doubtAnimation.setVisibility(View.GONE);
//                 }

            });
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

        networkStatusLiveData = new NetworkStatusLiveData(requireContext());
        networkStatusLiveData.observe(getViewLifecycleOwner(), isOnline -> {
            if (isOnline) {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
                    if (savBottomSheetDialog != null) {
                        savBottomSheetDialog.dismiss();
                    }
                }
            } else {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), false);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), true);
                    if (savBottomSheetDialog != null) {
                        savBottomSheetDialog.dismiss();
                        savBottomSheetDialog = new SavBottomSheetDialog(bottomSheetListener);
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("error", true);
                        bundle.putBoolean("textExtracted", false);
                        bundle.putString("finalText", finalText);
                        savBottomSheetDialog.setArguments(bundle);
                        if (isAdded() && !getParentFragmentManager().isStateSaved()) {
                            savBottomSheetDialog.show(getParentFragmentManager(), "");
                        }
                        savBottomSheetDialog.setCancelable(false);
                    }
                }
            }
        });
    }

    private void setViewSolutionCard(){
        try {
            isViewSolutionOpened = !isViewSolutionOpened;
            if (isViewSolutionOpened) {
                tv_view_solution.setText(getString(R.string.back_to_practise));
//                askDoubt.setVisibility(View.VISIBLE);
                askDoubt.setVisibility((!MyApplication.isCompetitiveProfile() && !SubscriptionType.hasActiveSubscription()) ? View.GONE : View.VISIBLE);
                bottomLayout.setVisibility(View.VISIBLE);
//                submit.setVisibility(View.VISIBLE);
                submitText.setText(getString(R.string.next));
                flipCards(imageUploadLayout, rl_solution);
//                imageUploadLayout.setVisibility(View.GONE);
//                rl_solution.setVisibility(View.VISIBLE);

                tv_solution.setText((factoryShipQnADto!=null && StringUtils.isNotEmpty(factoryShipQnADto.getAnswer()))?HtmlCompat.fromHtml(factoryShipQnADto.getAnswer().trim(), HtmlCompat.FROM_HTML_MODE_LEGACY):"");
//                submit.setEnabled(false);
//                clearAttempt.setEnabled(false);

                if (StringUtils.isNotEmpty(factoryShipQnADto.getAnswerSolImage())) {
                    ilu_solution_image_loader.setVisibility(View.VISIBLE);
                    ilu_solution_image_loader.loadImage(factoryShipQnADto.getAnswerSolImage());
                    ilu_solution_image_loader.setOnClickListener(view -> {
                        fullScreenImageDialog = new FullScreenImageDialog(requireContext(),
                                factoryShipQnADto.getAnswerSolImage(), FullViewImageType.SOLUTION);
                        fullScreenImageDialog.show();
                    });
                } else {
                    ilu_solution_image_loader.setVisibility(View.GONE);
                }

            }
            else {
                tv_view_solution.setText(getString(R.string.view_solution_practise));
                askDoubt.setVisibility(View.GONE);
//                submit.setVisibility(View.GONE);
                bottomLayout.setVisibility(View.GONE);
                submitText.setText(getString(R.string.submit));

                flipCards(rl_solution, imageUploadLayout);
//                imageUploadLayout.setVisibility(View.VISIBLE);
//                rl_solution.setVisibility(View.GONE);

//                submit.setEnabled(true);
//                clearAttempt.setEnabled(true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setUpQuestionDetail() {
        questionNumber.setText("Q" + number + " of " + total);
        if (StringUtils.isNotEmpty(factoryShipQnADto.getImageQuestion())) {
            questionImage.setVisibility(View.VISIBLE);
            Glide.with(getActivity()).load(factoryShipQnADto.getImageQuestion()).into(questionImage);
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

    private void setUpAnswerDetail() {
        try {
            if (isReviewMode()) {
                finalText = factoryShipQnADto.getUserAnswer();
                maxLines = Integer.MAX_VALUE;
                extractedText.setMaxLines(maxLines);
                extractedText.setEnabled(false);
                extractedText.clearFocus();
                extractedText.setText(factoryShipQnADto.getUserAnswer());
                handleCount(factoryShipQnADto.getUserAnswer().split("\\s+").length);
                description.setVisibility(View.GONE);
                descriptionCard.setVisibility(View.VISIBLE);
                answerImageLayout.setVisibility(View.GONE);
                extractedTextLayout.setVisibility(View.VISIBLE);
                editTextBack.setVisibility(View.GONE);
                textCardLayout.setBackgroundColor(getResources().getColor(R.color.text_background));
                imageUploadLayout.setVisibility(View.GONE);
                submitText.setText(R.string.next);
                clearAttempt.setVisibility(View.VISIBLE);
                bottomLayout.setVisibility(View.VISIBLE);
                solutionView.setVisibility(View.VISIBLE);
//                askDoubt.setVisibility(View.VISIBLE);
                askDoubt.setVisibility((!MyApplication.isCompetitiveProfile() && !SubscriptionType.hasActiveSubscription()) ? View.GONE : View.VISIBLE);
                cv_view_solution.setVisibility(View.GONE);
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
                } else {
                    idealSolutionCard.setVisibility(View.GONE);
                    analysisCard.setVisibility(View.GONE);
                }
                dataLoaded = false;
                if (isShowFiveStepTooltip())
                    showFiveStepToolTip(1);
            }
        } catch (Exception e) {
            Log.d("setUpAnswerDetail:", e.getMessage());
        }
    }

    private void switchCards(final View cardToHide, final View cardToShow) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        // If already visible, no need to animate
        if (cardToShow.getVisibility() == View.VISIBLE) return;

        // Animate the card to hide (fade out)
        cardToHide.animate()
                .alpha(0f)
                .setDuration(shortAnimTime)
                .withEndAction(() -> {
                    cardToHide.setVisibility(View.GONE);
                    cardToHide.setAlpha(1f); // reset for next time
                });

        // Animate the card to show (fade in)
        cardToShow.setAlpha(0f);
        cardToShow.setVisibility(View.VISIBLE);
        cardToShow.animate()
                .alpha(1f)
                .setDuration(shortAnimTime)
                .setListener(null);
    }

    private void flipCards(View cardToHide, View cardToShow) {
        int duration = 100;

        float width = cardToHide.getWidth();
        cardToHide.animate()
                .translationX(-width)
                .setDuration(duration)
                .withEndAction(() -> {
                    cardToHide.setVisibility(View.GONE);
                    cardToHide.setTranslationX(0);
                    cardToShow.setTranslationX(width);
                    cardToShow.setVisibility(View.VISIBLE);
                    cardToShow.animate()
                            .translationX(0)
                            .setDuration(duration)
                            .start();
                }).start();
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
        maxLines = 15;
        TooltipHelper.getInstance().dismiss();
        textExtracted = false;
        list.clear();
        factoryShipQnADto.setReattempt(true);
        factoryShipQnADto.setUserAnswer("");
        factoryShipQnADto.setClearAttempt(true);
        factoryShipQnADto.setReview(false);
        solutionView.setVisibility(View.GONE);
        extractedText.setText("");
        extractedTextLayout.setVisibility(View.GONE);
        imageUploadLayout.setVisibility(View.VISIBLE);
        cv_view_solution.setVisibility(View.VISIBLE);
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
        cv_view_solution.setVisibility(View.VISIBLE);
    }

    private boolean isReviewMode() {
        return factoryShipQnADto.isCompleted()
                && !factoryShipQnADto.isReattempt()
                && (StringUtils.isNotEmpty(factoryShipQnADto.getUserSolution()))
                && StringUtils.isNotEmpty(factoryShipQnADto.getUserAnswer());
    }

    private void setUpRecyclerView() {
        answerImageRv.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        uploadedImageRecyclerAdapter = new UploadedImageRecyclerAdapter(getActivity(), list, this, LectureContentType.valueOf(factoryShipQnADto.getType()));
        answerImageRv.setAdapter(uploadedImageRecyclerAdapter);
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
                                    list.remove(croppingImageIndex);
                                    list.add(croppingImageIndex, croppedImageUri);
                                    if (uploadedImageRecyclerAdapter != null) {
                                        uploadedImageRecyclerAdapter.notifyDataSetChanged();
                                    }
                                    showSubmitTooltip();
                                } else {
                                    Toast.makeText(getActivity(), "size larger than 15mb", Toast.LENGTH_LONG).show();
                                }
                            }
                        } else {
                            Toast.makeText(getContext(), getString(R.string.cropping_failed), Toast.LENGTH_SHORT).show();
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
                            int start = list.size();

                            Log.d("handleImagePickerDialog", result.getData().getStringArrayListExtra("url_list").toString());
                            ArrayList<String> urls = result.getData().getStringArrayListExtra("url_list");
                            for (int i = 0; i < urls.size(); i++) {
                                Uri photoUri = Uri.parse(urls.get(i));
                                Log.d("photoUri", "gallery = " + photoUri);
                                if (photoUri != null) {
                                    addImage(photoUri, getImageSize(photoUri), start + i);
                                    if (uploadedImageRecyclerAdapter != null) {
                                        uploadedImageRecyclerAdapter.notifyDataSetChanged();
                                    }
                                    showSubmitTooltip();
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

    private void handleEditText() {
        extractedText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (textExtracted && extractedText.isEnabled()) {
                    finalText = s.toString();
                    handleCount(finalText.split("\\s+").length);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(getActivity(), ImageCropperActivity.class);
        if (LectureContentType.valueOf(factoryShipQnADto.getType()).
                equals(LectureContentType.LONG_ANSWERS)) {
            if (getActivity() != null)
                Toast.makeText(getActivity().getApplicationContext(),
                        String.format(getString(R.string.You_can_select_up_to_images),
                                3 - list.size()), Toast.LENGTH_LONG).show();
            intent.putExtra("allowedImages", 3);
            intent.putExtra("remainingImages", list.size());
        } else {
            if (getActivity() != null)
                Toast.makeText(getActivity().getApplicationContext(),
                        String.format(getString(R.string.You_can_select_up_to_images), 2), Toast.LENGTH_LONG).show();
            intent.putExtra("allowedImages", 2);
        }
        galleryLauncher.launch(intent);
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
            Uri imageuri = FileProvider.getUriForFile(requireContext().getApplicationContext(),
                    BuildConfig.APPLICATION_ID + ".provider", imageFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri);
            currentPhotoPath = imageuri;
            cameraLauncher.launch(intent);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void addImage(Uri imgUri, float length, int position) {
        Log.d(TAG, "--> Adding image");
        answerImageLayout.setVisibility(View.VISIBLE);
        bottomLayout.setVisibility(View.VISIBLE);
        imageUploadLayout.setVisibility(View.GONE);
        cv_view_solution.setVisibility(View.GONE);

        // Check file size first
        if (length > 15) {
            Toast.makeText(getActivity(), "Size larger than 15MB", Toast.LENGTH_LONG).show();
            return;
        }

        LectureContentType type = LectureContentType.valueOf(factoryShipQnADto.getType());
        int maxLimit = (type == LectureContentType.SHORT_ANSWERS) ? 2 : 3;

        if (list.size() < maxLimit) {
            list.add(position, imgUri);
            crop = false;
        } else {
            Toast.makeText(getActivity(), "Maximum selection limit is " + maxLimit, Toast.LENGTH_SHORT).show();
        }

        if (!list.isEmpty()) {
            submit.setCardBackgroundColor(getResources().getColor(R.color.subscription_blue));
            submit.setEnabled(true);
        }
    }


    @Override
    public void addClicked() {
        isSingleImageSelection = true;
        imagePickDialog.show();
    }

    @Override
    public void deleteClicked(int position) {
        TooltipHelper.getInstance().dismiss();
        list.remove(position);
        if (list.isEmpty()) {
            bottomLayout.setVisibility(View.GONE);
            isSingleImageSelection = false;
            imageUploadLayout.setVisibility(View.VISIBLE);
            cv_view_solution.setVisibility(View.VISIBLE);
            answerImageLayout.setVisibility(View.GONE);
        }
        if (uploadedImageRecyclerAdapter != null) {
            uploadedImageRecyclerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void addCropClicked(int position) {
        croppingImageIndex = position;
        startCropActivity(list.get(position));
        crop = true;
    }

    private void startCropActivity(Uri imageUri) {
        Intent cropIntent = CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                .getIntent(requireActivity());
        cropImageLauncher.launch(cropIntent);
    }


    private void extractTextFromImage() {
        Log.d("ExtractText", "Initiating text extraction from image.");
        Map<String, Object> params = new HashMap<>();
        params.put("savType", factoryShipQnADto.isSavType());
        params.put("questionId",factoryShipQnADto.getQuestionId());

        Log.d(TAG, "extractTextFromImage: "+params.toString());

        multipartRequest = new MyMultipartRequest(Request.Method.POST,
                CommonMethods.getUrlParam(EXTRACT_TEXT_FROM_IMAGE_URI, params), response -> {
            try {
                Log.d("ExtractText", "Response received from server.");
                if (getContext() != null) {
                    Log.d("ExtractText", "Context is valid.");
                    if (savBottomSheetDialog != null) {
                        Log.d("ExtractText", "Dismissing bottom sheet dialog.");
                        savBottomSheetDialog.dismiss();
                    }
                }

                TypeToken<ImageTextExtractionResponse> token = new TypeToken<ImageTextExtractionResponse>() {
                };
                Gson gson = new Gson();
                ImageTextExtractionResponse extractionResponse = gson.fromJson(new String(response.data, "UTF-8"), token.getType());
                textExtracted = true;
                Log.d("ExtractText", "Parsed response: " + extractionResponse);

                if (!extractionResponse.isTextExtracted()) {
                    Log.d("ExtractText", "No text extracted from image.");
                    finalText = "";
                    handleBottomSheet(false, false);
                } else {
                    Log.d("ExtractText", "Text extracted successfully: " + extractionResponse.getMessage());
                    extractedTextLayout.setVisibility(View.VISIBLE);
                    answerImageLayout.setVisibility(View.GONE);
                    extractedText.requestFocus();
                    extractedText.setEnabled(true);
                    Log.d("issue_sav_ImageURls", "extractTextFromImage:   "
                            + extractionResponse.getUrls());
                    if (!CollectionUtils.isEmpty(extractionResponse.getUrls())) {
                        userAnswerImageUrl = extractionResponse.getUrls();
                    }
                    extractedText.setMaxLines(maxLines);
                    extractedText.setText(extractionResponse.getMessage());
                    if(StringUtils.isNotEmpty(extractionResponse.getRequestId())) {
                        questionRequestIdFromML = extractionResponse.getRequestId();
                        questionSessionIdFromML= extractionResponse.getSessionId();
                    }
                    finalText = extractionResponse.getMessage();
                    startThreeStepTooltip();
                }
            } catch (Exception e) {
                Log.e("ExtractText", "Error processing response: " + e.getMessage(), e);
                if (MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                    Log.d("ExtractText", "Connection disconnected.");
                    handleBottomSheet(true, false);
                } else {
                    Log.d("ExtractText", "Unexpected error occurred.");
                    handleBottomSheet(false, true);
                }
            }
        }, error -> {
            Log.e("ExtractText", "Error response from server: " + error.toString());
            if (savBottomSheetDialog != null && isAdded() && !getParentFragmentManager().isStateSaved()) {
                Log.d("ExtractText", "Safely dismissing bottom sheet dialog.");
                savBottomSheetDialog.dismissAllowingStateLoss(); // Safe alternative
            }

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
            if (isAdded() && !getParentFragmentManager().isStateSaved()) {
                savBottomSheetDialog.show(getParentFragmentManager(), "");
            }
            savBottomSheetDialog.setCancelable(false);
            submitListener.onBottomSheetOpen();
        }) {
            @Override
            protected Map<String, MultipartRequestDataPart> getByteData() {
                Log.d("ExtractText", "Preparing byte data for request.");
                Map<String, MultipartRequestDataPart> params = new HashMap<>();
                try {
                    for (int i = 0; i < list.size(); i++) {
                        Uri uri = list.get(i);
                        File file = FileUtil.from(getActivity(), uri);
                        byte[] fileData = CommonMethods.readFileToByteArray(file);
                        String fileName = file.getName();
                        String contentType = CommonMethods.getMimeType(fileName);

                        if (i == 0) {
                            params.put("file", new MultipartRequestDataPart(fileName, fileData, contentType));
                        } else {
                            params.put("file" + i, new MultipartRequestDataPart(fileName, fileData, contentType));
                        }
                    }
                } catch (Exception e) {
                    Log.e("ExtractText", "Error preparing byte data: " + e.getMessage(), e);
                    if (MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                        handleBottomSheet(true, false);
                    } else {
                        handleBottomSheet(false, true);
                    }
                }
                return params;
            }
        };

        Log.d("ExtractText", "Adding request to the queue.");
        Volley.newRequestQueue(getContext()).add(multipartRequest).setRetryPolicy(new DefaultRetryPolicy(
                600000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void handleCount(int count) {
        readText.setOnClickListener(v -> {
            if (!readMore) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.BELOW, R.id.scroll_view);
                description.setVisibility(View.GONE);
                extractedTextLayout.setLayoutParams(params);
                extractedText.setMaxLines(Integer.MAX_VALUE);
                extractedText.setText(finalText);
                readText.setText(getString(R.string.Read_Less));
                readMore = true;
                solutionView.setVisibility(View.GONE);
            } else {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                params.addRule(RelativeLayout.BELOW, R.id.solution_view);
                if (isReviewMode()) {
                    description.setVisibility(View.GONE);
                    solutionView.setVisibility(View.VISIBLE);
                } else {
                    description.setVisibility(View.VISIBLE);
                }
                extractedTextLayout.setLayoutParams(params);
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
            wordLimit = Integer.parseInt(factoryShipQnADto.getWordLimit());
        } catch (Exception e) {
            Log.e("wordLimit", e.getMessage(), e);
        }
        countText.setText(String.format(count + "/" + wordLimit));
        countText.setVisibility(factoryShipQnADto.isWordLimitApplicable() ? View.VISIBLE : View.GONE);
        long wordLimitBuffer = MyApplication.getFirebaseRemoteConfig().getLong("word_limit_buffer");

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
            submit.setCardBackgroundColor(getResources().getColor(R.color.subscription_blue));
        }

        extractedText.post(() -> {
            if (extractedText.getLineCount() > maxLines) {
                readText.setVisibility(View.VISIBLE);
            } else if (!readMore) {
                readText.setVisibility(View.GONE);
            }
        });
    }

    private void handleBottomSheet(boolean error, boolean backendError) {
        if (getActivity() != null && isAdded()) {
            try {
                if (savBottomSheetDialog != null) {
                    savBottomSheetDialog.dismiss();
                }
                savBottomSheetDialog = new SavBottomSheetDialog(bottomSheetListener);
                Bundle bundle = new Bundle();
                bundle.putBoolean("error", error);
                bundle.putBoolean("textExtracted", textExtracted);
                bundle.putString("finalText", finalText);
                savBottomSheetDialog.setArguments(bundle);
                if (isAdded() && !getParentFragmentManager().isStateSaved()) {
                    savBottomSheetDialog.show(getParentFragmentManager(), "");
                }
                savBottomSheetDialog.setCancelable(false);
                submitListener.onBottomSheetOpen();
            } catch (Exception e) {
                Log.d("handleBottomSheet: ", e.getMessage());
            }
        }
    }

    private void submitAnswerAndGetSolution() {
        JSONObject params = new JSONObject();
        try {
            params.put("questionId", factoryShipQnADto.getQuestionId());
            params.put("answerByStudents", extractedText.getText());
            params.put("idealAnswer", formatIdealAnswerText(factoryShipQnADto.getAnswer()));
            params.put("marks", factoryShipQnADto.getMarks());
            params.put("numerical", false);
            params.put("question", factoryShipQnADto.getQuestion());
            params.put("wordCount", extractedText.getText().toString().split("\\s+").length);
            params.put("wordLimit", factoryShipQnADto.getWordLimit());
            params.put("boldFormatting", true);
            params.put("requestId",questionRequestIdFromML);
            params.put("sessionId",questionSessionIdFromML);
            if (!CollectionUtils.isEmpty(userAnswerImageUrl)) {
                String commaSeparatedUrls = String.join(",", userAnswerImageUrl);
                params.put("urls", commaSeparatedUrls);
            }
        } catch (JSONException e) {
            Log.e("Error in subscription", e.getMessage(), e);
        }
        submitJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                BOARD_ANSWER_VALIDATE, params, response -> {
            Log.d("SUBMISSIONRESPONE", "SUBMISSION: " + response.toString());

            TypeToken<ShortLongAnswerSubmitResponse> token = new TypeToken<ShortLongAnswerSubmitResponse>() {
            };
            Gson gson = new Gson();
            submitResponse = gson.fromJson(response.toString(), token.getType());
            factoryShipQnADto.setAnswerEx(submitResponse.getExplanation());
            factoryShipQnADto.setCompleted(true);
            factoryShipQnADto.setReattempt(false);
            factoryShipQnADto.setUserAnswer(extractedText.getText().toString().trim());
            factoryShipQnADto.setMarks(submitResponse.getTotalMarks());
            factoryShipQnADto.setMarksObtained(submitResponse.getMarks());
            factoryShipQnADto.setUserSolution(submitResponse.getExplanation());
            factoryShipQnADto.setAnalysis(submitResponse.getAnalysis());

            Map<String, String> bundle = new HashMap<>();
            bundle.put("UserAnswer", extractedText.getText().toString().trim());
            bundle.put("type", factoryShipQnADto.getType());
            bundle.put("Question_Id", factoryShipQnADto.getQuestionId());
            bundle.put("Obtained_Marks", submitResponse.getMarks());
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.sav_Submission.getActionName(), bundle);


            if (savBottomSheetDialog != null) {
                savBottomSheetDialog.dismiss();
            }
//            extractedText.clearFocus();
//            extractedText.setEnabled(false);
//            description.setText(getString(R.string.your_submitted_answer));
//            editTextBack.setVisibility(View.GONE);
//            textCard.setCardBackgroundColor(getResources().getColor(R.color.text_background));
            boolean correct = factoryShipQnADto.getUserAnswer()
                    .equalsIgnoreCase(factoryShipQnADto.getAnswer() != null
                            ? factoryShipQnADto.getAnswer()
                            : "");
            submitListener.onSubmit(Integer.parseInt(number) - 1, factoryShipQnADto, correct);
            handlePostHandler();

        }, new VolleyGlobalErrorListener(getContext()) {
            @Override
            public void postError(VolleyError error) {
                if (getContext() != null) {
                    if (savBottomSheetDialog != null) {
                        savBottomSheetDialog.dismiss();
                    }
                }
                if (MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                    handleBottomSheet(true, false);
                } else {
                    handleBottomSheet(false, true);
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
        String s = "";
        if (idealAnswer != null) {
            s = idealAnswer.replaceAll("</body></html>", "");
            s = s.replaceAll("<html><body>", "");
        }
        return s;
    }

    private void cancelRequest() {
        if (submitJsonObjectRequest != null) {
            submitJsonObjectRequest.cancel();
        }
        if (multipartRequest != null)
            multipartRequest.cancel();
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

    private void handlePostHandler() {
        handleAnswerCheckDialog();
        answerCheckDialog.show();
        Handler handler = new Handler();
        Runnable runnable = () -> {
            handler.removeCallbacksAndMessages(null);
            maxLines = 10;
            extractedText.setMaxLines(maxLines);
            answerCheckDialog.dismiss();
            setUpAnswerDetail();
            if (!MyApplication.getBoolData(PreferenceKeys.IS_REVIEW_TOOL_TIP_VISIBLE.getKey())) {
                MyApplication.saveData(PreferenceKeys.IS_REVIEW_TOOL_TIP_VISIBLE.getKey(), true);
            }
        };
        handler.postDelayed(runnable, 3000);
    }

    private void handleAnswerCheckDialog() {
        try {
            String totalMarks = factoryShipQnADto.getMarks();
            Log.d("AnswerCheck", "response : " + submitResponse.toString());
            yourScore.setText(getString(R.string.your_score) + ":" + CommonMethods.cleanDecimalNumber(Double.parseDouble(factoryShipQnADto.getMarksObtained())) + "/" + totalMarks);
            double percent;
            percent = ((Double.parseDouble(factoryShipQnADto.getMarksObtained()) * 100) / Integer.parseInt(totalMarks));

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
        } catch (Exception e) {
            Log.e("AnswerCheck", e.getMessage(), e);
        }
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
    public void onGalleryClick(boolean textNotFound) {
        Log.d("onGalleryClick", "Called with textNotFound = " + textNotFound);
        textExtracted = false;
        if (getContext() != null) {
            if (savBottomSheetDialog != null) {
                savBottomSheetDialog.dismiss();
            }
        }
        if (textNotFound) {
            list.clear();
            uploadedImageRecyclerAdapter.notifyDataSetChanged();
            imageUploadLayout.setVisibility(View.VISIBLE);
            answerImageLayout.setVisibility(View.GONE);
        }
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
        if (textNotFound) {
            list.clear();
            uploadedImageRecyclerAdapter.notifyDataSetChanged();
            imageUploadLayout.setVisibility(View.VISIBLE);
            cv_view_solution.setVisibility(View.VISIBLE);
            answerImageLayout.setVisibility(View.GONE);
        }
        checkCameraPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
    }

    @Override
    public void onCloseClick() {
        if (showToast && textExtracted) {
            showToast = false;
            Toast.makeText(requireContext(), getString(R.string.analysis_progress), Toast.LENGTH_LONG).show();
        } else if (!textExtracted) {
            setUpActionPopup();
        } else {
            if (getContext() != null && savBottomSheetDialog != null) {
                savBottomSheetDialog.dismiss();
            }
            cancelRequest();
        }
        submitListener.onBottomSheetClose();
    }


    private void setUpActionPopup() {
        actionDialog = new Dialog(getContext());
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
        close.setOnClickListener(v -> {
            if (actionDialog != null) {
                actionDialog.dismiss();
            }
        });
        btn1.setOnClickListener(v -> {

            if (savBottomSheetDialog != null) {
                savBottomSheetDialog.dismiss();
            }
            cancelRequest();
            if (list != null) {
                list.clear();
            }
            if (imageUploadLayout != null) {
                imageUploadLayout.setVisibility(View.VISIBLE);
                cv_view_solution.setVisibility(View.VISIBLE);
            }
            if (answerImageLayout != null) {
                answerImageLayout.setVisibility(View.GONE);
            }
        });
        btn2.setOnClickListener(v -> {
            if (actionDialog != null) {
                actionDialog.dismiss();
            }
        });
        title.setText(getResources().getString(R.string.dont_leave));
        subtitle.setText(getString(R.string.Leaving_this_page_will_cancel_your_upload));
        btn1_text.setText(getResources().getString(R.string.leave_anyway));
        btn2_text.setText(getResources().getString(R.string.Continue));
        actionDialog.show();
        MediaPlayer.create(requireContext(), R.raw.wronganswer).start();
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
                            clickTooltipStatus.setTooltipType(TooltipType.CLICK_HERE);
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
            if (uploadTooltipStatus.getStepCount() != 3) {
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
        if (isShowClickTooltip()) {
            checkViewLoading();
        }
        if (isShowFiveStepTooltip()) {
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
        uploadButtonRelative.post(onAllViewsReady);
    }

    private void checkClickHereTooltip() {
        try {
            MyTooltipDto myTooltipDto = MyTooltipDto.builder()
                    .text(getString(R.string.click_here))
                    .isAboveAnchor(false)
                    .alignment(RelativeLayout.CENTER_HORIZONTAL)
                    .autoDismiss(true)
                    .tooltipType(TooltipType.CLICK_HERE)
                    .fragmentNumber(Integer.parseInt(number))
                    .currentFragment(MyApplication.getInstance().getTooltipViewModel().getCurrentFragment())
                    .build();
            TooltipHelper.getInstance().handleTooltip(scrollView, uploadButtonRelative,
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
            return !isReviewMode() && clickTooltipStatus != null && !clickTooltipStatus.isActive() &&
                    TooltipHelper.getInstance().countNotCompleted(TooltipType.CLICK_HERE, getActivity()) &&
                    scrollView != null && uploadButtonRelative != null;
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
        int stepCount = uploadTooltipStatus.getStepCount();
        if (stepCount <= 3) {
            stepCount = readText.getVisibility() == View.VISIBLE && readMore ? stepCount + 1 : stepCount;
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
        switch (stepCount) {
            case 1 -> scrollView.post(() -> {
                if (TooltipHelper.getInstance().isViewFullyVisibleInScrollView(analysisTitle, scrollView))
                    showFiveStepToolTip(analysisTitle, RelativeLayout.ALIGN_START, 1);
            });
            case 2 -> scrollView.post(() -> {
                if (TooltipHelper.getInstance().isViewFullyVisibleInScrollView(idealTitle, scrollView))
                    showFiveStepToolTip(idealTitle, RelativeLayout.ALIGN_START, 2);
            });
            case 3 -> scrollView.post(() -> {
                if (TooltipHelper.getInstance().isViewFullyVisibleInScrollView(descriptionCard, scrollView))
                    showFiveStepToolTip(descriptionCard, RelativeLayout.ALIGN_START, 3);
            });
            case 4 -> showFiveStepToolTip(askDoubtCard, RelativeLayout.ALIGN_END, 4);
            case 5 -> showFiveStepToolTip(submit, RelativeLayout.ALIGN_END, 5);
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
                scrollView.smoothScrollTo(0, extractedTextLayout.getTop());
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
                        showFiveStepToolTip(analysisTitle,
                                RelativeLayout.ALIGN_START, 1), 1);
            });
            case 2 -> scrollView.post(() -> {
                scrollView.postDelayed(() -> showFiveStepToolTip(idealTitle,
                        RelativeLayout.ALIGN_START, 2), 2);
            });
            case 3 -> scrollView.post(() -> {
                scrollView.postDelayed(() -> showFiveStepToolTip(descriptionCard,
                        RelativeLayout.ALIGN_START, 3), 300);
            });
            case 4 -> showFiveStepToolTip(askDoubtCard, RelativeLayout.ALIGN_END, 4);
            case 5 -> {
                showFiveStepToolTip(submit, RelativeLayout.ALIGN_END, 5);
            }
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
                    .fragmentNumber(Integer.parseInt(number))
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
        TooltipHelper.getInstance().dismiss();
        if (uploadTooltipStatus != null && uploadTooltipStatus.getStepCount() == uploadTooltipStatus.getTotalSteps()) {
            finishUploadTooltip();
        } else showToolTipAfterImageUpload(uploadTooltipStatus.getStepCount() + 1, getTotalCount());
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
    public void onDismiss() {
        submitListener.onBottomSheetClose();
    }

    @Override
    public void onToolTipSkipClick() {
        finishReviewTooltip(true);
    }
}