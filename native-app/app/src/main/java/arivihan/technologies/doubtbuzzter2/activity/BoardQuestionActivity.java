package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.BOARD_ANSWER_VALIDATE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.EXTRACT_TEXT_FROM_IMAGE_URI;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.POST_MICROLECTURE_ANSWER;

import android.Manifest;
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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.theartofdev.edmodo.cropper.CropImage;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.MyItemTouchHelper;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.MatchTheseAdapterLeft;
import arivihan.technologies.doubtbuzzter2.adapter.MatchTheseAdapterRight;
import arivihan.technologies.doubtbuzzter2.adapter.OptionQuestionAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.UploadedImageRecyclerAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.MultipartRequestDataPart;
import arivihan.technologies.doubtbuzzter2.constants.QuestionLevel;
import arivihan.technologies.doubtbuzzter2.constants.QuestionType;
import arivihan.technologies.doubtbuzzter2.fragments.Correct_Answer_AnimationFragment;
import arivihan.technologies.doubtbuzzter2.fragments.LongAnswerQuestionFragment2;
import arivihan.technologies.doubtbuzzter2.fragments.Wrong_Answer_AnimationFragment;
import arivihan.technologies.doubtbuzzter2.helper.FileUtil;
import arivihan.technologies.doubtbuzzter2.model.FactoryShipQnADto;
import arivihan.technologies.doubtbuzzter2.model.ImageTextExtractionResponse;
import arivihan.technologies.doubtbuzzter2.model.LectureContentType;
import arivihan.technologies.doubtbuzzter2.model.MatchTheseLeftOption;
import arivihan.technologies.doubtbuzzter2.model.MatchTheseRightOption;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureContentDto;
import arivihan.technologies.doubtbuzzter2.model.OptionModel;
import arivihan.technologies.doubtbuzzter2.model.ShortLongAnswerSubmitResponse;
import arivihan.technologies.doubtbuzzter2.model.UserMicrolectureAnswer;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyMultipartRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class BoardQuestionActivity extends LocaleActivity implements OptionQuestionAdapter.OnOptionClick, UploadedImageRecyclerAdapter.AddClickListener {

    private static final String TAG = BoardQuestionActivity.class.getSimpleName();
    Gson gson = new Gson();
    ImageView image;
    RecyclerView uploadedImageRecyclerView;
    UploadedImageRecyclerAdapter uploadedImageRecyclerAdapter;
    RelativeLayout uploadFileLayout;
    ArrayList<Uri> list;
    EditText extractedText;
    LinearLayout extractedLinearLayout;
    List<File> imagesToUpload = new ArrayList<>();
    private static final int CAMERA_PERMISSION_CODE = 100;
    String currentPhotoPath = "";
    private Uri imageuri;
    private Uri uri;
    int imageAdded = 0;
    RelativeLayout imageUploadLayout;
    LinearLayout imageTextExtractionLoader;
    boolean isLast;
    LinearLayout seeSubmissionResult;
    View extractedTextFromImageView;
    ConstraintLayout imageExtractionLayout;
    ArrayList<OptionModel> model = new ArrayList<>();
    ImageView questionImage, book, unbook;
    CardView expandImage, collapseImage, backImage;
    Dialog dialog;
    Dialog image_pick_dialog;
    Dialog reattemptDialog;
    String chap;
    int selectedCurrentOption = -1;
    int integerAnswer = 0;
    String submited = "-1";
    String contentUrl = "";
    RecyclerView options;
    ProgressBar progress;
    ScrollView scroll;
    ScrollView scroll_bar;
    TextView topic_name, question1, question2, btnText, nextTopicMsg, nextTopicName, ques_num, text_centre, cardText;
    String topicName;
    private final int your_reques_code = 1001;
    CardView sub, nextQuestion;
    CardView card,viewImageBtn;
    ImageView back,next;
    private Dialog matchTheseDialog;
    FrameLayout ImageFrame, TextFrame;
    TextView explanation;
    String quesType = "";
    String s = "";
    String idMicroLecture = "";
    RelativeLayout relativeLayout;
    List<String> optionList = new ArrayList<>();
    public String ques_no, Answer, id, title, ex;
    int lastPos;
    int currPos;
    ArrayList<FactoryShipQnADto> qnADtoList;
    RelativeLayout ParentLayout;
    public int attempt = 0;
    public int data_loaded = 0;
    private boolean editTextEnabled;
    TextInputLayout textInputLayout;
    TextInputEditText textInputEditText;
    EditText editText;
    private TextView optionMt;
    private LinearLayout numberOfWordsLayout;

    private MicroLectureContentDto microLectureContentDto;
    String userAnswerOption;
    String userAnswer;
    public static boolean isMatchTheseSubmitted = false;

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

    public static boolean isTextExtracting = false;
    boolean isTextExtractedFromImage = false;
    String extractedTextFromImage = "";
    MyItemTouchHelper myItemTouchHelper;
    int wordCount;
    int croppingImageIndex = -1;
    boolean isSingleImageSelection = false;
    TextView wordCounter, checkBeforeSubmitMsg;
    long word_limit_buffer = MyApplication.getFirebaseRemoteConfig().getLong("word_limit_buffer");

    RecyclerView left_recyclerView;
    RecyclerView right_recyclerView;
    Correct_Answer_AnimationFragment bottomSheetDialogFragment = new Correct_Answer_AnimationFragment();
    Wrong_Answer_AnimationFragment bottomSheetDialogFragment2 = new Wrong_Answer_AnimationFragment();
    ActivityResultLauncher<Intent> mLaunch = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // Handle the Intent
                }
            });
    private String maxLengthText = "";

    boolean backPress;
    List<String> userAnswerImageUrl;

    // save from app crashing-----
    @Override
    protected void onPause() {
        super.onPause();
        if (bottomSheetDialogFragment.isVisible())
            bottomSheetDialogFragment.dismiss();
        if (bottomSheetDialogFragment2.isVisible())
            bottomSheetDialogFragment2.dismiss();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isMatchTheseSubmitted = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Disable Screen Schots----
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_question);

        ParentLayout = findViewById(R.id.ParentLayout);
        ParentLayout.setKeepScreenOn(true);
        editText = findViewById(R.id.extracted_edit_text);
        image = findViewById(R.id.extracted_image);

        LongAnswerQuestionFragment2.isMatchTheseSubmittedMock = false;
        microLectureContentDto = (MicroLectureContentDto) getIntent().
                getSerializableExtra(getResources().getString(R.string.passed_obj));

        chap = getIntent().getStringExtra("Chapter");
        id = getIntent().getStringExtra("Subject");
        title = getIntent().getStringExtra("Title");

        setQIdInCustomKeys();

        extractedTextFromImageView = findViewById(R.id.edit_text_layout);

        imageUploadLayout = findViewById(R.id.upload_image_layout);
        imageTextExtractionLoader = findViewById(R.id.extracting_text_layout);
        imageExtractionLayout = findViewById(R.id.image_extraction_layout);
        extractedLinearLayout = findViewById(R.id.extracted_text_linear_layout);
        expandImage = findViewById(R.id.expand_view_card);
        collapseImage = findViewById(R.id.collapse_view_card);
        backImage = findViewById(R.id.back_image_card);
        card = findViewById(R.id.card_enable);
//        text_centre = findViewById(R.id.text_centre);
        uploadedImageRecyclerView = findViewById(R.id.uploaded_image_rv);
        numberOfWordsLayout = findViewById(R.id.number_of_words_layout);
        btnText = findViewById(R.id.btn_text);
        cardText = findViewById(R.id.card_text);
        setSubjectiveLayout();
        extractedText = findViewById(R.id.extracted_edit_text);
        wordCounter = findViewById(R.id.number_of_words);
        checkBeforeSubmitMsg = findViewById(R.id.check_text);


        // FIND VIEW BY ID DEFINE-----------------

        left_recyclerView = findViewById(R.id.rv_left_options);
        right_recyclerView = findViewById(R.id.rv_right_options);
        matchTheseLayout = findViewById(R.id.match_these_layout);
        setMatchThese();
        explanation = findViewById(R.id.explanation);
        progress = findViewById(R.id.progress);
        options = findViewById(R.id.optionRecyclerView);
        nextQuestion = findViewById(R.id.next);
        nextTopicMsg = findViewById(R.id.nextTopicMessage);
        nextTopicName = findViewById(R.id.nextTopicName);
        topic_name = findViewById(R.id.Topic_name);
        sub = findViewById(R.id.sub_btn);
        book = findViewById(R.id.bookmark_done);
        scroll = findViewById(R.id.scroll);
        scroll_bar = findViewById(R.id.scroll_bar);
        back = findViewById(R.id.back_arrow);
        next = findViewById(R.id.next_arrow);
        unbook = findViewById(R.id.bookmark_outline);
        relativeLayout = findViewById(R.id.relativeLayout);
        TextFrame = findViewById(R.id.Frame_with_Only_Text);
        ImageFrame = findViewById(R.id.Frame_with_image);
        questionImage = findViewById(R.id.image_question);
        ques_num = findViewById(R.id.question_number);
        question1 = findViewById(R.id.question_with_only_text);
        question2 = findViewById(R.id.question_with_image);
        textInputLayout = findViewById(R.id.textAnswerLayout);
        textInputEditText = findViewById(R.id.textAnswer);
        viewImageBtn = findViewById(R.id.view_image_btn);
        //-----------------------------------

        Animation animation = AnimationUtils.loadAnimation(BoardQuestionActivity.this, R.anim.top_bottom);
        relativeLayout.startAnimation(animation);

        // Data Fitting--------------
        TextFrame.setVisibility(View.VISIBLE);
        topicName = getIntent().getStringExtra("Title");
        topic_name.setText(topicName);
        optionList = microLectureContentDto.getOptions();
        if (StringUtils.isNotEmpty(microLectureContentDto.getAnswerEx())) {
            ex = microLectureContentDto.getAnswerEx().trim();
        } else if (StringUtils.isEmpty(microLectureContentDto.getAnswerEx()) && StringUtils.isEmpty(microLectureContentDto.getAnswerImage())) {
            ex = getString(R.string.no_explanation);
        }
        ques_no = getIntent().getExtras().getString(PreferenceKeys.POSITION.getKey());
        currPos = getIntent().getIntExtra("currentPos",0);
        qnADtoList=getIntent().getParcelableArrayListExtra("questionList");
        lastPos = getIntent().getIntExtra("lastPos",0);
        if(ques_no.equals(String.valueOf(1)))
            back.setVisibility(View.INVISIBLE);
        else if(ques_no.equals(String.valueOf(lastPos)))
            next.setVisibility(View.INVISIBLE);

        if (CollectionUtils.isEmpty(qnADtoList)) {
            Toast.makeText(this, getString(R.string.please_try_again_later), Toast.LENGTH_SHORT).show();
            getOnBackPressedDispatcher().onBackPressed();
        }

        if (!CollectionUtils.isEmpty(qnADtoList) && qnADtoList.size() == 1) {
            next.setVisibility(View.INVISIBLE);
        }
        Log.d("PosBoard", ques_no);
        isLast = getIntent().getExtras().getBoolean("isLast");

        if (ques_no == null) {
            ques_no = "1";
        }

        try {
            Integer.parseInt(ques_no);
        } catch (NumberFormatException e) {
            MyApplication.getFirebaseInstance().log("Generating custom exception");
            MyApplication.getFirebaseInstance().recordException(e);
            getOnBackPressedDispatcher().onBackPressed();
        }

        ques_num.setText(getString(R.string.questions) + " " + ques_no);
        //ques_num.setText(getString(R.string.questions) + " " + ques_no);
        quesType = getIntent().getStringExtra("QuesType");
        question1.setText(getIntent().getStringExtra("Question"));
        question2.setText(getIntent().getStringExtra("Question"));
        contentUrl = getIntent().getStringExtra("ImageQuestion");
        Answer = getIntent().getStringExtra("Answer");
        if (Answer != null) {
            //ans = Integer.parseInt(Answer);
            integerAnswer = 2;
        }

        if (contentUrl != null && !contentUrl.isEmpty()) {
            ImageFrame.setVisibility(View.VISIBLE);
            TextFrame.setVisibility(View.GONE);
            Glide.with(this).load(contentUrl).into(questionImage);
        } else {
            TextFrame.setVisibility(View.VISIBLE);
            ImageFrame.setVisibility(View.GONE);
        }


        questionImage.setOnClickListener(view -> {
            Intent intent = new Intent(BoardQuestionActivity.this, ZoomImageActivity.class);
            intent.putExtra("Url", contentUrl);
            startActivity(intent);
        });

        viewImageBtn.setOnClickListener(view -> {
            Intent intent = new Intent(BoardQuestionActivity.this, ZoomImageActivity.class);
            intent.putExtra("Url", contentUrl);
            startActivity(intent);
        });

        updareRecycerViewData();

        unbook.setOnClickListener(view -> {
            book.setVisibility(View.GONE);
            unbook.setVisibility(View.GONE);
        });
        book.setOnClickListener(view -> {
            book.setVisibility(View.GONE);
            unbook.setVisibility(View.GONE);
        });

        right_recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (isMatchTheseSubmitted) {
                    left_recyclerView.scrollBy(dx, dy);
                }
            }
        });

        sub.setOnClickListener(view -> {
            if (microLectureContentDto.getLectureContentType() == LectureContentType.MATCH_THE_COLUMN) {
                isMatchTheseSubmitted = true;
                setUserMatchTheseAnswer();
            } else if (microLectureContentDto.getLectureContentType() == LectureContentType.LONG_ANSWERS ||
                    microLectureContentDto.getLectureContentType() == LectureContentType.SHORT_ANSWERS) {
                isTextExtracting = true;
                if (list.size() >= 1) {
                    imageTextExtractionLoader.setVisibility(View.VISIBLE);
//                    layout3.setVisibility(View.VISIBLE);
                    if (isTextExtractedFromImage) {
                        openAnalysisDialog();
                        submitAnswerAndGetSolution();
                    } else {
                        sub.setVisibility(View.GONE);
                        extractTextFromImage();
                    }

                }
            } else {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    CommonMethods.showNegativeSnackbar(ParentLayout, getString(R.string.NoInternetConnection));
                } else if (editTextEnabled) {
                    sub.setVisibility(View.GONE);
                    progress.setVisibility(View.VISIBLE);
                    boolean isCorrect = textInputEditText.getText() != null &&
                            textInputEditText.getText().toString().trim().equalsIgnoreCase(microLectureContentDto.getAnswer());
                    submitAnswer(microLectureContentDto, isCorrect,
                            OptionModel.builder().option(microLectureContentDto.getAnswer()).build(),
                            OptionModel.builder().option(textInputEditText.getText().toString()).build());
                } else if (attempt == 0) {
                    submited = "1";
                    OptionModel op;
                    for (String s : optionList) {
                        op = new OptionModel(s, submited, integerAnswer, selectedCurrentOption, quesType);
                        model.add(op);
                    }
                    submited = "0";
                    if (integerAnswer >= 0 && selectedCurrentOption >= 0) {
                        if (selectedCurrentOption != integerAnswer) {
                            sub.setVisibility(View.GONE);
                            progress.setVisibility(View.VISIBLE);
                            submitAnswer(microLectureContentDto, false, model.get(integerAnswer), model.get(selectedCurrentOption));
                        } else {
                            sub.setVisibility(View.GONE);
                            progress.setVisibility(View.VISIBLE);
                            submitAnswer(microLectureContentDto, true, model.get(integerAnswer), model.get(selectedCurrentOption));
                        }
                    }
                }
            }


        });

        s = getIntent().getStringExtra("Chapter");
        idMicroLecture = getIntent().getStringExtra("Id");

        back.setOnClickListener(v -> {
            currPos--;
            openQuestion(qnADtoList.get(currPos));
        });
        next.setOnClickListener(v -> {
            currPos++;
            openQuestion(qnADtoList.get(currPos));
        });

        explanation.setOnClickListener(view -> {
            dialog = new Dialog(BoardQuestionActivity.this);
            dialog.setContentView(R.layout.custom_dialogu_explanation);
            dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
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
            String url = getIntent().getStringExtra("AnswerImage");
            Log.i("URL  ", " " + url);

            if (url != null && url.length() > 0) {
                image.setVisibility(View.VISIBLE);
                Glide.with(getApplicationContext()).load(url).into(image);
            }

            image.setOnClickListener(view12 -> {
                Intent intent = new Intent(BoardQuestionActivity.this, ZoomImageActivity.class);
                intent.putExtra("Url", url);
                startActivity(intent);
            });

            exp.setText(ex);

            copy.setOnClickListener(view1 -> {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", ex);
                clipboard.setPrimaryClip(clip);
                CommonMethods.showPositiveSnackbar(ParentLayout, getString(R.string.ExplanationCopied) + "!");
            });

            cancel.setOnClickListener(view13 -> dialog.dismiss());

            revise.setOnClickListener(v -> {

                microLectureContentDto.setVideoUrl(microLectureContentDto.getAnswerVideo());
                @OptIn(markerClass = UnstableApi.class) Intent intent  =new Intent(BoardQuestionActivity.this,
                            VideoPlayerActivity.class);
                intent.putExtra(getResources().getString(R.string.passed_obj), microLectureContentDto);
                intent.putExtra("shouldNotResume", true);
                intent.putExtra(getString(R.string.back_to_q), true);
                mLaunch.launch(intent);
            });

            dialog.show();
        });

        nextQuestion.setOnClickListener(view -> {
            isTextExtracting = false;
            currPos++;
            if (currPos == qnADtoList.size()) {
                nextQuestion();
            } else {
                openQuestion(qnADtoList.get(currPos));
            }

        });

        nextQuestion.setVisibility(View.GONE);

        textInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (StringUtils.isNotEmpty(charSequence)) {
                    card.setVisibility(View.GONE);
                    sub.setVisibility(View.VISIBLE);
                } else {
                    card.setVisibility(View.VISIBLE);
                    sub.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        if (microLectureContentDto.getLectureContentType() == LectureContentType.FILL_IN_THE_BLANKS
                || microLectureContentDto.getLectureContentType() == LectureContentType.ONE_WORD_ANSWER
                || microLectureContentDto.getLectureContentType() == LectureContentType.VERY_SHORT_ANSWERS) {
            isMatchTheseSubmitted = false;
            scroll_bar.setVisibility(View.VISIBLE);
            editTextEnabled = true;
            options.setVisibility(View.GONE);
            textInputLayout.setVisibility(View.VISIBLE);
            textInputEditText.setVisibility(View.VISIBLE);
            cardText.setText(getResources().getString(R.string.submit));
        } else if (microLectureContentDto.getLectureContentType() == LectureContentType.MATCH_THE_COLUMN) {
            isMatchTheseSubmitted = false;
            editTextEnabled = false;
            setDefaultMatchTheseAnswer();
            card.setVisibility(View.GONE);
            sub.setVisibility(View.VISIBLE);
            scroll_bar.setVisibility(View.VISIBLE);
            options.setVisibility(View.GONE);
            textInputEditText.setVisibility(View.GONE);
            textInputLayout.setVisibility(View.GONE);
            matchTheseLayout.setVisibility(View.VISIBLE);

            if (question2 != null) {
                question2.setVisibility(View.GONE);
            }
        } else if (microLectureContentDto.getLectureContentType() == LectureContentType.LONG_ANSWERS
                || microLectureContentDto.getLectureContentType() == LectureContentType.SHORT_ANSWERS) {
            isMatchTheseSubmitted = false;
            scroll_bar.setVisibility(View.GONE);
            editTextEnabled = false;
            textInputLayout.setVisibility(View.GONE);
            textInputEditText.setVisibility(View.GONE);
            imageUploadLayout.setVisibility(View.VISIBLE);
            setReattemptAttemptPopUpView();
        } else {
            isMatchTheseSubmitted = false;
            editTextEnabled = false;
            textInputLayout.setVisibility(View.GONE);
            textInputEditText.setVisibility(View.GONE);
            scroll_bar.setVisibility(View.VISIBLE);
            try {
                integerAnswer = Integer.parseInt(Answer);
            } catch (Exception e) {
                integerAnswer = 0;
            }
            integerAnswer--;
            options.setVisibility(View.VISIBLE);
            cardText.setText(getResources().getString(R.string.submit));
        }


        if (microLectureContentDto.getLectureContentType() == LectureContentType.FILL_IN_THE_BLANKS) {
            textInputEditText.setMaxLines(1);
            textInputEditText.setSingleLine(true);
            textInputEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        }

    }

    private void openAnalysisDialog() {
        subjectiveAnalysisPopup = new Dialog(this){
            @Override
            public void onBackPressed() {
                if (!backPress) {
                    backPress = true;
                    Toast.makeText(BoardQuestionActivity.this,R.string.analysis_progress, Toast.LENGTH_LONG).show();
                    new Handler().postDelayed(() -> backPress = false, 2000);
                } else
                    super.onBackPressed();
            }
        };
        subjectiveAnalysisPopup.setCanceledOnTouchOutside(false);
        subjectiveAnalysisPopup.setContentView(R.layout.popup_after_subjective_submit_layout);
        subjectiveAnalysisPopup.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(this, R.drawable.bachground_custom_dialogu_typeactivity));
        subjectiveAnalysisPopup.getWindow().setDimAmount(0.8f);
        subjectiveAnalysisPopup.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subjectiveAnalysisPopup.getWindow().setGravity(Gravity.BOTTOM);
        lottieAnimationView=subjectiveAnalysisPopup.findViewById(R.id.subjective_analysis_anim);
        lottieAnimationView.setAnimation(R.raw.subjective_analysis);
        lottieAnimationView.playAnimation();
        subjectiveAnalysisPopup.findViewById(R.id.close_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subjectiveAnalysisPopup.dismiss();
                submitJsonObjectRequest.cancel();
            }
        });
        subjectiveAnalysisPopup.show();
    }

    private void showExplanationView() {
        ShortLongAnswerSubmitResponse submitResponse = new ShortLongAnswerSubmitResponse();
        submitResponse.setExplanation(microLectureContentDto.getUserSolution());
        submitResponse.setMarks(microLectureContentDto.getMarksObtained());
        submitResponse.setTotalMarks(microLectureContentDto.getMarks());
        imageUploadLayout.setVisibility(View.GONE);
        extractedTextFromImageView.setVisibility(View.VISIBLE);
        imageTextExtractionLoader.setVisibility(View.GONE);
        imageExtractionLayout.setVisibility(View.GONE);
        showSubjectiveExplanationView(submitResponse, false);
        seeSubmissionResult.performClick();
        sub.setVisibility(View.GONE);
        btnText.setText(getText(R.string.next));
        progress.setVisibility(View.GONE);
        nextQuestion.setVisibility(View.VISIBLE);
    }

    private void extractTextFromImage() {
        MyMultipartRequest multipartRequest = new MyMultipartRequest(Request.Method.POST, EXTRACT_TEXT_FROM_IMAGE_URI, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                try {
                    TypeToken<ImageTextExtractionResponse> token = new TypeToken<ImageTextExtractionResponse>() {
                    };
                    Gson gson = new Gson();
                    ImageTextExtractionResponse extractionResponse = gson.fromJson(new String(response.data, "UTF-8"), token.getType());
                    Log.d("MULTIPARTRES", "RESPONSE : " + extractionResponse.getMessage());

                    extractedText.setText(extractionResponse.getMessage());
                    extractedText.requestFocus();
                    extractedText.setSelection(0);
                    extractedText.setCursorVisible(true);
                    isTextExtracting = false;
                    if(!CollectionUtils.isEmpty(extractionResponse.getUrls())) {
                        userAnswerImageUrl=extractionResponse.getUrls();
                    }
                    TextView wordLimit = findViewById(R.id.word_limit);
                    wordCount = extractionResponse.getMessage().trim().split("\\s+").length;
                    wordCounter.setText(String.format(wordCount + " "));
                    wordLimit.setText(String.format("/ " + microLectureContentDto.getWordLimit() + " "));
                    numberOfWordsLayout.setVisibility(microLectureContentDto.isWordLimitApplicable()?View.VISIBLE:View.GONE);
                    if (wordCount > (Integer.parseInt(microLectureContentDto.getWordLimit()) + word_limit_buffer) && microLectureContentDto.isWordLimitApplicable()) {
                        wordCounter.setTextColor(ContextCompat.getColor(BoardQuestionActivity.this, R.color.match_red));
                        sub.setVisibility(View.GONE);
                        card.setVisibility(View.VISIBLE);
                        cardText.setText(getResources().getString(R.string.submit));
                        card.setClickable(true);
                        checkBeforeSubmitMsg.setText(getResources().getString(R.string.word_limit_text));
                        checkBeforeSubmitMsg.setTextColor(getResources().getColor(R.color.match_red));
                    } else if (wordCount > Integer.parseInt(microLectureContentDto.getWordLimit()) && microLectureContentDto.isWordLimitApplicable()) {
                        wordCounter.setTextColor(getResources().getColor(R.color.match_red));
                        sub.setVisibility(View.VISIBLE);
                        btnText.setText(R.string.submit);
                        card.setVisibility(View.GONE);
                    } else {
                        wordCounter.setTextColor(getResources().getColor(R.color.marks_color));
                        sub.setVisibility(View.VISIBLE);
                        btnText.setText(R.string.submit);
                        card.setVisibility(View.GONE);
                    }

                    checkBeforeSubmitMsg.setVisibility(View.VISIBLE);

                    imageTextExtractionLoader.setVisibility(View.GONE);
                    imageExtractionLayout.setVisibility(View.GONE);
                    extractedTextFromImageView.setVisibility(View.VISIBLE);
                    isTextExtractedFromImage = true;
                    extractedTextFromImage = extractionResponse.getMessage();

                    extractedText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            wordCount = s.toString().trim().split("\\s+").length;
                            wordCounter.setText(String.format(wordCount + " "));
                            wordCounter.setVisibility(microLectureContentDto.isWordLimitApplicable()?View.VISIBLE:View.GONE);
                            Log.e(TAG, "---> word limit changed");
                            if (wordCount > (Integer.parseInt(microLectureContentDto.getWordLimit()) + word_limit_buffer) && microLectureContentDto.isWordLimitApplicable()) {
                                wordCounter.setTextColor(getResources().getColor(R.color.match_red));
                                sub.setVisibility(View.GONE);
                                card.setVisibility(View.VISIBLE);
                                cardText.setText(getResources().getString(R.string.submit));
                                card.setClickable(true);
                                checkBeforeSubmitMsg.setText(getResources().getString(R.string.word_limit_text));
                                checkBeforeSubmitMsg.setTextColor(getResources().getColor(R.color.match_red));
                            } else if (wordCount > Integer.parseInt(microLectureContentDto.getWordLimit()) && microLectureContentDto.isWordLimitApplicable()) {
                                wordCounter.setTextColor(getResources().getColor(R.color.match_red));
                                sub.setVisibility(View.VISIBLE);
                                btnText.setText(R.string.submit);
                                card.setVisibility(View.GONE);
                                card.setClickable(false);
                                checkBeforeSubmitMsg.setText(getResources().getString(R.string.check_your_answer_before_submitting));
                                checkBeforeSubmitMsg.setTextColor(getResources().getColor(R.color.check_text_color));
                            } else {
                                wordCounter.setTextColor(getResources().getColor(R.color.marks_color));
                                sub.setVisibility(View.VISIBLE);
                                btnText.setText(R.string.submit);
                                card.setVisibility(View.GONE);
                                card.setClickable(false);
                                checkBeforeSubmitMsg.setText(getResources().getString(R.string.check_your_answer_before_submitting));
                                checkBeforeSubmitMsg.setTextColor(getResources().getColor(R.color.check_text_color));
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    });

                    card.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.word_limit_text), Toast.LENGTH_LONG).show();
                        }
                    });

                } catch (Exception e) {
                    Log.e("Error in subscription", e.getMessage(), e);

                }

            }
        }, error -> Log.d("MULTIPARTRES", "RESPONSE : " + error.toString())) {
            @Override
            protected Map<String, MultipartRequestDataPart> getByteData() {
                Map<String, MultipartRequestDataPart> params = new HashMap<>();
                for (int i = 0; i < imagesToUpload.size(); i++) {
                    File file = imagesToUpload.get(i);
                    try {
                        byte[] fileData = CommonMethods.readFileToByteArray(file);
                        String fileName = file.getName();
                        String contentType = CommonMethods.getMimeType(fileName);

                        if (i == 0) {
                            params.put("file", new MultipartRequestDataPart(fileName, fileData, contentType));
                        } else {
                            params.put("file" + i, new MultipartRequestDataPart(fileName, fileData, contentType));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                return params;
            }
        };


        Volley.newRequestQueue(getApplicationContext()).add(multipartRequest).setRetryPolicy(new DefaultRetryPolicy(
                600000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void submitAnswerAndGetSolution() {
        JSONObject params = new JSONObject();
        try {
            params.put("questionId", microLectureContentDto.getQuestionId());
            params.put("answerByStudents", extractedText.getText());
            params.put("idealAnswer", microLectureContentDto.getAnswer());
            params.put("marks", microLectureContentDto.getMarks());
            params.put("numerical", false);
            params.put("question", microLectureContentDto.getQuestion());
            params.put("wordCount", String.valueOf(wordCount));
            params.put("wordLimit", microLectureContentDto.getWordLimit());
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
            subjectiveAnalysisPopup.dismiss();
            TypeToken<ShortLongAnswerSubmitResponse> token = new TypeToken<ShortLongAnswerSubmitResponse>() {
            };
            Gson gson = new Gson();
            ShortLongAnswerSubmitResponse submitResponse = gson.fromJson(response.toString(), token.getType());

            if (isFinishing() || isDestroyed()) {
                return;
            }
            showSubjectiveExplanationView(submitResponse, true);

        }, new VolleyGlobalErrorListener(getApplicationContext()) {
            @Override
            public void postError(VolleyError error) {
                sub.setVisibility(View.VISIBLE);
            }
        });

        Volley.newRequestQueue(getApplicationContext()).add(submitJsonObjectRequest).setRetryPolicy(new DefaultRetryPolicy(
                600000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void showSubjectiveExplanationView(ShortLongAnswerSubmitResponse submitResponse, boolean canSubmitAnswer) {
        TextView marksObtained = findViewById(R.id.marks_obtained);
        marksObtained.setVisibility(View.VISIBLE);
        marksObtained.setText(getString(R.string.your_marks) + submitResponse.getMarks() + "/" + submitResponse.getTotalMarks());

        extractedText.setEnabled(true);
        extractedText.setCursorVisible(false);
        extractedText.setTextColor(getResources().getColor(R.color.black));
        extractedText.setFocusableInTouchMode(false);
        extractedText.clearFocus();
        extractedLinearLayout.setBackgroundColor(getResources().getColor(R.color.grey_extracted_layout));
        expandImage.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.white));
        backImage.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.white));
        collapseImage.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.white));
        seeSubmissionResult = findViewById(R.id.see_submission_result);
        seeSubmissionResult.setVisibility(View.VISIBLE);

        LinearLayout copySubmissionResult = findViewById(R.id.copy_submitted_solution);

        seeSubmissionResult.setOnClickListener(v -> {
            backImage.setVisibility(View.GONE);

            extractedLinearLayout.setBackgroundColor(ContextCompat.getColor(BoardQuestionActivity.this, R.color.board_question_highlight));

            TextView solutionHeading = findViewById(R.id.solution_heading);
            solutionHeading.setVisibility(View.VISIBLE);

            editText.setText(submitResponse.getExplanation());
            numberOfWordsLayout.setVisibility(View.GONE);
            seeSubmissionResult.setVisibility(View.GONE);
            copySubmissionResult.setVisibility(View.VISIBLE);


        });

        if (canSubmitAnswer) {
            microLectureContentDto.setMarks(submitResponse.getMarks());
            microLectureContentDto.setMarksObtained(submitResponse.getTotalMarks());
            microLectureContentDto.setUserSolution(submitResponse.getExplanation());
            submitAnswer(microLectureContentDto, true,
                    OptionModel.builder().option(microLectureContentDto.getAnswer()).build(),
                    OptionModel.builder().option(extractedTextFromImage).build());
        }

        copySubmissionResult.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(getString(R.string.solution_copied), submitResponse.getExplanation());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getApplicationContext(), getString(R.string.copied), Toast.LENGTH_LONG).show();
        });

        checkBeforeSubmitMsg.setVisibility(View.GONE);

//            btnText.setText(R.string.next);
        sub.setVisibility(View.VISIBLE);
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

    private void setMatchTheseDialog() {
        matchTheseDialog = new Dialog(this);
        matchTheseDialog.setContentView(R.layout.match_these_dialog);
        matchTheseDialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(this, R.drawable.bachground_custom_dialogu_typeactivity));
        matchTheseDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        lottieAnimationView = matchTheseDialog.findViewById(R.id.lottie);
        lottieAnimationView.setRotation(90);
        lottieAnimationView.setAnimation(R.raw.drag_and_drop);
    }

    private void showAnimationView() {

        lottieAnimationView.playAnimation();
        matchTheseDialog.findViewById(R.id.close).setOnClickListener(view -> {
            lottieAnimationView.pauseAnimation();
            matchTheseDialog.dismiss();
        });

        boolean showMatchTheseDialog = MyApplication.getBoolData(PreferenceKeys.SHOW_MATCH_THESE_DIALOG.getKey());

        if (showMatchTheseDialog) {
            matchTheseDialog.show();
            MyApplication.saveData(PreferenceKeys.SHOW_MATCH_THESE_DIALOG.getKey(), false);
        }

    }

    private void setReattemptAttemptPopUpView() {

        if (microLectureContentDto.isCompleted() &&
                (microLectureContentDto.getLectureContentType() == LectureContentType.SHORT_ANSWERS ||
                        microLectureContentDto.getLectureContentType() == LectureContentType.LONG_ANSWERS) &&
                StringUtils.isNotEmpty(microLectureContentDto.getUserSolution())) {

            reattemptDialog = new Dialog(this);
            reattemptDialog.setContentView(R.layout.activity_select_reattempt_or_show);
            reattemptDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            reattemptDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            reattemptDialog.findViewById(R.id.cross).setOnClickListener(view -> reattemptDialog.cancel());

            reattemptDialog.findViewById(R.id.attempt).setOnClickListener(view -> {
                reattemptDialog.cancel();
                reattemptDialog.dismiss();
            });
            reattemptDialog.findViewById(R.id.see_solution).setOnClickListener(view -> {
                reattemptDialog.cancel();
                reattemptDialog.dismiss();
                showExplanationView();
            });
            reattemptDialog.show();
        }
    }

    private void setSubjectiveLayout() {
        list = new ArrayList<>();
        cardText.setText(getResources().getString(R.string.upload));
        image_pick_dialog = new Dialog(this);
        image_pick_dialog.setContentView(R.layout.activity_select_photo);
        image_pick_dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        image_pick_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        image_pick_dialog.findViewById(R.id.cross).setOnClickListener(view -> image_pick_dialog.cancel());

        image_pick_dialog.findViewById(R.id.Choose_from_gallery).setOnClickListener(view -> {
            openGallery();
            image_pick_dialog.cancel();
            uploadedImageRecyclerAdapter = new UploadedImageRecyclerAdapter(BoardQuestionActivity.this, list, BoardQuestionActivity.this, microLectureContentDto.getLectureContentType());
            uploadedImageRecyclerView.setLayoutManager(new LinearLayoutManager(BoardQuestionActivity.this, LinearLayoutManager.VERTICAL, false));
            uploadedImageRecyclerView.setAdapter(uploadedImageRecyclerAdapter);
        });
        image_pick_dialog.findViewById(R.id.click_a_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image_pick_dialog.cancel();
                checkCameraPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
                uploadedImageRecyclerAdapter = new UploadedImageRecyclerAdapter(BoardQuestionActivity.this, list, BoardQuestionActivity.this, microLectureContentDto.getLectureContentType());
                uploadedImageRecyclerView.setLayoutManager(new LinearLayoutManager(BoardQuestionActivity.this, LinearLayoutManager.VERTICAL, false));
                uploadedImageRecyclerView.setAdapter(uploadedImageRecyclerAdapter);
            }
        });
        uploadFileLayout = findViewById(R.id.upload_file_layout);
//        text_centre.setOnClickListener(view -> image_pick_dialog.show());

        uploadFileLayout.setOnClickListener(view -> image_pick_dialog.show());

        backImage.setOnClickListener(view -> {
            imageTextExtractionLoader.setVisibility(View.VISIBLE);
            extractedTextFromImageView.setVisibility(View.GONE);
            imageExtractionLayout.setVisibility(View.VISIBLE);
            imageTextExtractionLoader.setVisibility(View.GONE);
            isTextExtractedFromImage = false;
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.BELOW, R.id.relativeLayout);
            params.addRule(RelativeLayout.ABOVE, R.id.bottom_card);
            extractedTextFromImageView.setLayoutParams(params);
            card.setVisibility(View.GONE);
            sub.setVisibility(View.VISIBLE);
            btnText.setText(getResources().getString(R.string.upload));
        });

        expandImage.setOnClickListener(view -> {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.BELOW, R.id.ParentLayout);
            params.addRule(RelativeLayout.ABOVE, R.id.bottom_card);
            extractedTextFromImageView.setLayoutParams(params);
            expandImage.setVisibility(View.GONE);
            collapseImage.setVisibility(View.VISIBLE);
        });

        collapseImage.setOnClickListener(view -> {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.BELOW, R.id.relativeLayout);
            params.addRule(RelativeLayout.ABOVE, R.id.bottom_card);
            extractedTextFromImageView.setLayoutParams(params);
            expandImage.setVisibility(View.VISIBLE);
            collapseImage.setVisibility(View.GONE);
        });

    }

    private void setMatchThese() {
//        if (microLectureContentDto.getLectureContentType() != LectureContentType.MATCH_THE_COLUMN) {
//            return;
//        }

        correctAnswerList = new ArrayList<>();
        userAnswerList = new ArrayList<>();

        list_left = new ArrayList<>();
        list_right = new ArrayList<>();
        optionMt = findViewById(R.id.option_mt);
        matchTheseAdapterLeft = new MatchTheseAdapterLeft(list_left, this);
        left_recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        left_recyclerView.setAdapter(matchTheseAdapterLeft);
        if (microLectureContentDto.getLectureContentType() == LectureContentType.MATCH_THE_COLUMN) {
            setMatchTheseDialog();
            showAnimationView();
            matchTheseAdapterRight = new MatchTheseAdapterRight(list_right, this);
            right_recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            right_recyclerView.setAdapter(matchTheseAdapterRight);
            myItemTouchHelper = new MyItemTouchHelper(matchTheseAdapterRight);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(myItemTouchHelper);
            setLeftList(microLectureContentDto.getOptions());
            setRightList(microLectureContentDto.getAnswers());
            calculateHeight();
            setCorrectAnswerList(microLectureContentDto.getAnswer());
            matchTheseAdapterRight.setTouchHelper(itemTouchHelper);
            itemTouchHelper.attachToRecyclerView(right_recyclerView);
        }
    }

    private void calculateHeight() {
        findViewById(R.id.description).setVisibility(View.VISIBLE);
        findViewById(R.id.column_layout).setVisibility(View.VISIBLE);
        LinearLayout layout = findViewById(R.id.fakeLayout);
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

    public int getHeightOfLargestDescription(TextView textView) {
        final WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
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

    void setCorrectAnswerList(String answer) {
        String[] result = answer.split(",");
        correctAnswerList = Arrays.asList(result);
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

    private void setDefaultMatchTheseAnswer() {
        userAnswer = "1-1";
        for (int i = 1; i < list_left.size(); i++) {
            userAnswer = "," + userAnswer + (i + 1) + "-" + (i + 1);
        }
    }

    public void setUserMatchTheseAnswer() {
        myItemTouchHelper.setDraggable(false);
        userAnswerList.clear();
        finalAnswerList = new ArrayList<>();
        boolean correct = true;
        for (int i = 0; i < list_left.size(); i++) {
            userAnswerOption = list_left.get(i).getOption_number() + "-" + list_right.get(i).getOption_number();
            if (correctAnswerList.get(i).equals(userAnswerOption)) {
                list_right.get(i).setCorrectlyMatched(true);
                list_left.get(i).setCorrectlyMatched(true);
            } else {
                correct = false;
                list_right.get(i).setCorrectlyMatched(false);
                list_left.get(i).setCorrectlyMatched(false);
            }
            userAnswerList.add(userAnswerOption);
            userAnswer = userAnswerOption + userAnswer;
        }
        submitAnswer(microLectureContentDto, correct,
                OptionModel.builder().option(microLectureContentDto.getAnswer()).build(),
                OptionModel.builder().option(userAnswer).build());

        if (matchTheseAdapterLeft != null) {
            matchTheseAdapterLeft.notifyDataSetChanged();
        }
        if (matchTheseAdapterRight != null) {
            matchTheseAdapterRight.notifyDataSetChanged();
        }

        LinearLayout seeMatchTheseSubmissionResult = findViewById(R.id.see_match_these_submission_result);
        seeMatchTheseSubmissionResult.setVisibility(View.VISIBLE);

        seeMatchTheseSubmissionResult.setOnClickListener(v -> {

            seeMatchTheseSubmissionResult.setVisibility(View.GONE);
            String[] answer = microLectureContentDto.getAnswer().split(",");
            list_right.clear();
            for (int i = 0; i < answer.length; i++) {
                list_right.add(new MatchTheseRightOption(Integer.parseInt(answer[i].split("-")[1]), microLectureContentDto.getAnswers().get(Integer.parseInt(answer[i].split("-")[1]) - 1), true));
                list_left.get(i).setCorrectlyMatched(true);
            }

            if (matchTheseAdapterLeft != null) {
                matchTheseAdapterLeft.notifyDataSetChanged();
            }
            if (matchTheseAdapterRight != null) {
                matchTheseAdapterRight.notifyDataSetChanged();
            }

            if (StringUtils.isNotEmpty(microLectureContentDto.getAnswerEx())) {

                LinearLayout seeMatchTheseExplanation = findViewById(R.id.see_match_these_explanation);
                seeMatchTheseExplanation.setVisibility(View.VISIBLE);

                seeMatchTheseExplanation.setOnClickListener(e -> {
                     Log.d("SEEEXPLANATION", "TEST ::" + microLectureContentDto.getAnswerEx());
                    seeExplanationDialog(microLectureContentDto.getAnswerEx());
                });
            }

        });
    }

    public void checkCameraPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(BoardQuestionActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(BoardQuestionActivity.this, new String[]{permission}, requestCode);
        } else {
            captureImage();
        }
    }

    private void openGallery() {

        Intent intent = new Intent();
        intent.setType("image/*");
        if (!isSingleImageSelection) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 118);
    }

    private void captureImage() {
        String filename = "photo";
        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File imageFile = File.createTempFile(filename, ".jpg", storageDirectory);
            currentPhotoPath = imageFile.getAbsolutePath();

            imageuri = FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()),
                    BuildConfig.APPLICATION_ID + ".provider", imageFile);
            imageAdded = 1;

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri);

            startActivityForResult(intent, 19);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d("microLectureContentDto", "WORD LIMIT" + microLectureContentDto.getWordLimit());
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 118 && resultCode == Activity.RESULT_OK) {

            imageUploadLayout.setVisibility(View.GONE);
            if (data.getClipData() != null) {

                ClipData clipData = data.getClipData();


                for (int i = 0; i < clipData.getItemCount(); i++) {

                    Uri imgUri = clipData.getItemAt(i).getUri();
                    Log.d("IMAGEURI", "Image uri forloop :: " + imgUri);

                    float lengthmb = getImageSize(imgUri);
                    File imgFile = null;
                    try {
                        imgFile = FileUtil.from(BoardQuestionActivity.this, imgUri);
                    } catch (IOException e) {
                        Log.e("Error in subscription", e.getMessage(), e);
                    }

                    if (lengthmb <= 15) {
                        if (microLectureContentDto.getLectureContentType().equals(LectureContentType.SHORT_ANSWERS) && list.size() < 2) {
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
                                Toast.makeText(getApplicationContext(), "Maximum selection limit is 3", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Maximum selection limit is 2", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else
                        Toast.makeText(BoardQuestionActivity.this, "size larger than 15mb", Toast.LENGTH_LONG).show();

                    if (list.size() > 0) {
                        card.setVisibility(View.GONE);
                        sub.setVisibility(View.VISIBLE);
                        btnText.setText(R.string.upload);
                        imageTextExtractionLoader.setVisibility(View.GONE);
                    }
                }
            } else {
                Uri imageUri = data.getData();
                Log.d("IMAGEURI", "Image uri:: " + imageUri);


                File file = null;
                try {
                    file = FileUtil.from(BoardQuestionActivity.this, imageUri);
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
                    Toast.makeText(BoardQuestionActivity.this, "size larger than 15mb", Toast.LENGTH_LONG).show();

                if (list.size() > 0) {
                    card.setVisibility(View.GONE);
                    sub.setVisibility(View.VISIBLE);
                    btnText.setText(R.string.upload);
                    imageTextExtractionLoader.setVisibility(View.GONE);
                }
            }


        }


        if (requestCode == 19 && resultCode == RESULT_OK) {
            CropImage.activity(imageuri)
                    .start(this);


        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUploadLayout.setVisibility(View.GONE);
                try {
                    if (result == null) {
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
                        if (uploadedImageRecyclerAdapter != null) {
                            uploadedImageRecyclerAdapter.notifyDataSetChanged();
                        }
                    } else
                        Toast.makeText(BoardQuestionActivity.this, "size larger than 15mb", Toast.LENGTH_LONG).show();

                    if (list.size() > 0) {
                        card.setVisibility(View.GONE);
                        sub.setVisibility(View.VISIBLE);
                        btnText.setText(R.string.upload);
                        imageTextExtractionLoader.setVisibility(View.GONE);
                    }

                } catch (Exception error) {
                    Log.e("resultUri", error.getMessage(), error);
                }
            }
        }
    }


    private float getImageSize(Uri imageUri) {
        String[] projection = {MediaStore.Images.Media.SIZE};
        Cursor cursor = getApplicationContext().getContentResolver().query(imageUri, projection, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int columnIndexSize = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
            long fileSizeInBytes = cursor.getLong(columnIndexSize);
            cursor.close();
            return (float) (fileSizeInBytes / 1024) / 1024;
        } else {
            return 0.0f;
        }
    }

    private void setQIdInCustomKeys() {
        try {
            FirebaseCrashlytics crashlytics = MyApplication.getFirebaseInstance();

            crashlytics.setCustomKey(PreferenceKeys.MICROLECTURE_ID.getKey(), "" + microLectureContentDto.getId());
            crashlytics.setCustomKey("microlectureQId", "" + microLectureContentDto.getQuestionId());


        } catch (Exception e) {
            Log.d("Error", "Error");

        }
    }

    private void submitAnswer(MicroLectureContentDto microLectureContentDto, boolean isCorrect, OptionModel ans, OptionModel correctAns) {
        try {
            if (getIntent().getStringExtra("Question") == null) {
                return;
            }

            if (microLectureContentDto.getQuestionId() == null) {
                microLectureContentDto.setQuestionId(microLectureContentDto.getId() + microLectureContentDto.getLangTitle());
            }

            UserMicrolectureAnswer userMicrolectureAnswer = UserMicrolectureAnswer
                    .builder()
                    .chapterId(microLectureContentDto.getChapterId())
                    .microlectureId(microLectureContentDto.getId())
                    .correct(isCorrect)
                    .obtainedMarks(microLectureContentDto.getMarksObtained())
                    .totalMarks(microLectureContentDto.getMarks())
                    .explanation(microLectureContentDto.getUserSolution())
                    .wrongAnswer(!isCorrect ? ans.getOption() : "")
                    .correctAnswer(correctAns.getOption())
                    .options(String.join(",", microLectureContentDto.getOptions()))
                    .userAnswer(ans.getOption())
                    .question(getIntent().getStringExtra("Question"))
                    .questionId(microLectureContentDto.getQuestionId())
                    .questionType(convertIntoQuestionType(microLectureContentDto.getLectureContentType()))
                    .questionLevel(microLectureContentDto.getQuestionLevel())
                    .build();

            Map<String, Object> map = new HashMap<>();
            map.put("personalised", microLectureContentDto.isPersonalised());

            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                    CommonMethods.getUrlParam(POST_MICROLECTURE_ANSWER, map), new JSONObject(gson.toJson(userMicrolectureAnswer)), response -> {

                MyApplication.saveData(PreferenceKeys.HOME_DASHBOARD_DATA_RELOAD.getKey(), true);
                MyApplication.saveData(PreferenceKeys.MICROLECTURE_DATA_RELOAD.getKey(), true);

                sub.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);

                OptionQuestionAdapter adapter = new OptionQuestionAdapter(model, BoardQuestionActivity.this, this);
                options.setAdapter(adapter);
                if ((StringUtils.isNotEmpty(microLectureContentDto.getAnswerEx()) && !"nan".equalsIgnoreCase(microLectureContentDto.getAnswerEx()))
                        || StringUtils.isNotEmpty(microLectureContentDto.getAnswerImage())
                        || StringUtils.isNotEmpty(microLectureContentDto.getAnswerVideo()))
                    explanation.setVisibility(View.VISIBLE);
                else
                    explanation.setVisibility(View.GONE);
                btnText.setText(getText(R.string.next));
                attempt = 1;
                Log.d("QUESTIONATTEMP", "post api result = " + response.toString());
                FragmentManager fragmentManager = getSupportFragmentManager();

                if (!fragmentManager.isDestroyed() && !isFinishing()) {
                    if (isCorrect) {
                        if (!bottomSheetDialogFragment.isAdded()) {
                            try {
                                bottomSheetDialogFragment.show(fragmentManager, "Add_Fragment");
                            } catch (Exception e) {

                            }
                        }
                    } else {
                        book.setVisibility(View.GONE);
                        unbook.setVisibility(View.GONE);
                        if (!bottomSheetDialogFragment2.isAdded()) {
                            try {
                                bottomSheetDialogFragment2.show(fragmentManager, "Add_Fragment");
                            } catch (Exception e) {

                            }
                        }
                    }
                }
                sub.setVisibility(View.GONE);
                if (data_loaded == 1)
                    if (!getIntent().getStringExtra("Chapter").equals(PreferenceKeys.PERSONALISED_TYPE.getKey())) {
                        nextQuestion.setVisibility(View.VISIBLE);
                    }

                progress.setVisibility(View.GONE);
                nextQuestion.setVisibility(View.VISIBLE);
                data_loaded = 1;
                textInputEditText.setEnabled(false);
                textInputEditText.setFocusable(false);
            }, new VolleyGlobalErrorListener(this) {
                @Override
                public void postError(VolleyError error) {
                    sub.setVisibility(View.VISIBLE);
                    progress.setVisibility(View.GONE);
                    CommonMethods.showNegativeSnackbar(progress, getString(R.string.please_try_again));
                }
            });
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
        } catch (Exception e) {
            CommonMethods.showNegativeSnackbar(progress, getString(R.string.please_try_again));
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
        }

        return QuestionType.NUMERICAL;
    }

    private void updareRecycerViewData() {
        options.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        ArrayList<OptionModel> model = new ArrayList<>();
        OptionModel op;
        for (String s : optionList) {
            op = new OptionModel(s, submited, integerAnswer, -1, quesType);
            model.add(op);
        }
        Animation scale = AnimationUtils.loadAnimation(this, R.anim.scale);
        OptionQuestionAdapter adapter = new OptionQuestionAdapter(model, this, this);
        options.setAdapter(adapter);
        options.startAnimation(scale);
    }

    private void nextQuestion() {
        Intent data = new Intent();
        setResult(RESULT_OK, data);
        finish();
    }


    @Override
    public void onClick(int position) {
        if (submited.equals("-1")) {
            card.setVisibility(View.GONE);
            sub.setVisibility(View.VISIBLE);
            selectedCurrentOption = position;
            Log.i("Selected Option is :", "" + position);
            ArrayList<OptionModel> model = new ArrayList<>();
            OptionModel op;
            for (String s : optionList) {
                op = new OptionModel(s, submited, integerAnswer, position, quesType);
                model.add(op);
            }
            OptionQuestionAdapter adapter = new OptionQuestionAdapter(model, this, this);
            if (quesType != null) {
                if (quesType.equals("IMAGE"))
                    options.scrollToPosition(position);
            }
            options.setAdapter(adapter);
        }
    }

    @Override
    public void addClicked() {
        isSingleImageSelection = true;
        image_pick_dialog.show();
    }

    @Override
    public void deleteClicked(int position) {
        imagesToUpload.remove(position);
        list.remove(position);
        if (list.size() == 0) {
            isSingleImageSelection = false;
        }
        if (uploadedImageRecyclerAdapter != null) {
            uploadedImageRecyclerAdapter.notifyDataSetChanged();
        }
        if (list.size() == 0) {
            imageUploadLayout.setVisibility(View.VISIBLE);
            imageTextExtractionLoader.setVisibility(View.GONE);
            sub.setVisibility(View.GONE);
            card.setVisibility(View.VISIBLE);
            card.setClickable(false);
            cardText.setText(getResources().getString(R.string.upload));
        }
    }

    @Override
    public void addCropClicked(int position) {
        croppingImageIndex = position;
        CropImage.activity(list.get(position)).start(this);
    }


    private void seeExplanationDialog(String explanation) {
        dialog = new Dialog(BoardQuestionActivity.this);
        dialog.setContentView(R.layout.custom_dialogu_explanation);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
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
        String url = getIntent().getStringExtra("AnswerImage");
        Log.i("URL  ", " " + url);

        if (url != null && url.length() > 0) {
            image.setVisibility(View.VISIBLE);
            Glide.with(getApplicationContext()).load(url).into(image);
        }

        image.setOnClickListener(view12 -> {
            Intent intent = new Intent(BoardQuestionActivity.this, ZoomImageActivity.class);
            intent.putExtra("Url", url);
            startActivity(intent);
        });

        exp.setText(explanation);

        copy.setOnClickListener(view1 -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", explanation);
            clipboard.setPrimaryClip(clip);
            CommonMethods.showPositiveSnackbar(ParentLayout, getString(R.string.ExplanationCopied) + "!");
        });

        cancel.setOnClickListener(view13 -> dialog.dismiss());

        revise.setOnClickListener(v -> {

            microLectureContentDto.setVideoUrl(microLectureContentDto.getAnswerVideo());
            @OptIn(markerClass = UnstableApi.class) Intent intent  = new Intent(BoardQuestionActivity.this,
                        VideoPlayerActivity.class);

            intent.putExtra(getResources().getString(R.string.passed_obj), microLectureContentDto);
            intent.putExtra("shouldNotResume", true);
            intent.putExtra(getString(R.string.back_to_q), true);
            mLaunch.launch(intent);
        });

        dialog.show();
    }

    private void openQuestion(FactoryShipQnADto factoryShipQnADto) {
        Intent intent = new Intent(this, BoardQuestionActivity.class);

        ArrayList<String> optionList = new ArrayList<>();
        MicroLectureContentDto microLectureContentDto = new MicroLectureContentDto();
        microLectureContentDto.setOptions(factoryShipQnADto.getOptions());
        microLectureContentDto.setTitle(factoryShipQnADto.getTitle());
        microLectureContentDto.setId(factoryShipQnADto.getCode());
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
        intent.putExtra(PreferenceKeys.POSITION.getKey(), String.valueOf(currPos + 1));
        intent.putExtra("Title", factoryShipQnADto.getTitle());
        intent.putExtra("Chapter", topicName);
        intent.putExtra("Id", id);
        intent.putExtra("Question", factoryShipQnADto.getQuestion().trim());
        intent.putExtra("lastPos", qnADtoList.size());
        intent.putParcelableArrayListExtra("questionList", qnADtoList);
        intent.putExtra("lastPos", qnADtoList.size());
        intent.putExtra("currentPos", currPos);
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
        if (factoryShipQnADto.getAnswer() == null){
        CommonMethods.showNegativeSnackbar(relativeLayout, getString(R.string.NoValidQuestionFound) + factoryShipQnADto.getAnswer());
    }else
            intent.putExtra("Answer", factoryShipQnADto.getAnswer().trim());

        String url = factoryShipQnADto.getAnswerSolImage();
        if (url != null)
            intent.putExtra("AnswerImage", url);
        else
            intent.putExtra("AnswerImage", "");
        // for further questions---
        intent.putExtra("Subject", id);
        intent.putExtra("Chapter", topicName);
        if (factoryShipQnADto.getOptionType() != null)
            intent.putExtra("QuesType", factoryShipQnADto.getOptionType());
        else
            intent.putExtra("QuesType", "TEXT");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (factoryShipQnADto.getQuestion() == null || (optionList.size() == 0 &&
                (LectureContentType.valueOf(factoryShipQnADto.getType()) == LectureContentType.TRUE_FALSE ||
                        LectureContentType.valueOf(factoryShipQnADto.getType()) == LectureContentType.MATCH_THE_COLUMN)))
            CommonMethods.showNegativeSnackbar(relativeLayout, getString(R.string.NoValidQuestionFound));
        else {
            startActivityForResult(intent, your_reques_code);
        }

    }


}


