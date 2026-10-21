package arivihan.technologies.doubtbuzzter2;

import android.animation.Animator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.core.text.HtmlCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import arivihan.technologies.doubtbuzzter2.activity.ChatSupportActivity;
import arivihan.technologies.doubtbuzzter2.activity.ExoPlayerActivity;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.adapter.MatchTheseAdapterLeft;
import arivihan.technologies.doubtbuzzter2.adapter.MatchTheseAdapterRight;
import arivihan.technologies.doubtbuzzter2.adapter.OptionAdapter;
import arivihan.technologies.doubtbuzzter2.common.ArivihanCustomDialog;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.FullScreenImageDialog;
import arivihan.technologies.doubtbuzzter2.constants.QuestionLevel;
import arivihan.technologies.doubtbuzzter2.enums.FullViewImageType;
import arivihan.technologies.doubtbuzzter2.helper.ImageLoaderUtil;
import arivihan.technologies.doubtbuzzter2.listener.CaseStudyListener;
import arivihan.technologies.doubtbuzzter2.listener.OptionListener;
import arivihan.technologies.doubtbuzzter2.model.AnswerState;
import arivihan.technologies.doubtbuzzter2.model.InfoType;
import arivihan.technologies.doubtbuzzter2.model.LectureContentType;
import arivihan.technologies.doubtbuzzter2.model.MatchTheseLeftOption;
import arivihan.technologies.doubtbuzzter2.model.MatchTheseRightOption;
import arivihan.technologies.doubtbuzzter2.model.OptionDto;
import arivihan.technologies.doubtbuzzter2.model.QuestionType;
import arivihan.technologies.doubtbuzzter2.model.TestSeriesQuestionDto;
import lombok.SneakyThrows;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;


public class MCQAndSubjectiveQuestionFragment extends Fragment implements OptionListener {

    private static final String TAG = MCQAndSubjectiveQuestionFragment.class.getSimpleName();
    int fragNumber;
    private CardView saveAnswer, clearAnswer, greenMarks, redMarks, solutionImageCard;
    private TextView saveAnswerText, explanation, yourAttempt, correctPercentage, marksObtained, greenMarksText, redMarksText;
    LottieAnimationView celebration;
    private RelativeLayout relativeLayoutQuestion, parentLayout;
    private EditText subjectiveAnswer, numericalAnswer;
    private TextInputLayout subjectiveAnswerInputlayout;

    private EditText numericalAnswerInputlayout;
    TestSeriesQuestionDto testSeriesQuestionDtoAnalytics = null;

    private String[] optionMultiCorrect = {"a", "b", "c", "d", "e", "f", "g", "h", "i"};
    private TextView question, questionType, mark, responsetext, ansText, ansTextLabel, solutionText;
    String Type = "";
    private ImageView questionImage;
    private ImageLoaderUtil solutionImage;
    private TestSeriesQuestionDto dto;
    Map<String, Integer> map;
    private OptionAdapter optionAdapter;
    Dialog dialog;
    ImageView caseStudyImageView;
    private  Animator revealAnimator;
    Map<String, Integer> maped = new HashMap<>();
    TextView subText, yourSubmissionResponseText;
    private List<OptionDto> optionDtos = new ArrayList<>();
    private OptionListener optionListener;
    private LinearLayout markLayout, markLinearlayout, solutionLayout;
    private ToggleButton markButton;
    private static final String ARG_PARAM1 = "question";
    private static final String ARG_PARAM2 = "listener";
    private static final String ARG_PARAM3 = "map";
    private static final String ARG_PARAM5 = "qNo";
    private static final String ARG_PARAM6 = "testSeriesId";
    private RelativeLayout askDoubtFab;
    private RelativeLayout caseStudyFab;
    private MaterialCardView caseStudyCard;
    private TextView caseStudyText;
    private CardView caseStudyNumCard;
    private TextView caseStudyNumText;
    private FloatingActionButton askDoubtFabBtn;
    private CaseStudyListener caseStudyListener;
    FullScreenImageDialog fullScreenImageDialog;


    private ActivityResultLauncher<Intent> takePictureLauncher;
    String userAnswer;
    String userAnswerOption;
    List<String> correctAnswerList;
    List<String> userAnswerList;
    ArrayList<Integer> finalAnswerList;
    ArrayList<MatchTheseLeftOption> list_left = new ArrayList<>();
    ArrayList<MatchTheseRightOption> list_right = new ArrayList<>();
    MatchTheseAdapterLeft matchTheseAdapterLeft;
    MatchTheseAdapterRight matchTheseAdapterRight;
    RecyclerView left_recyclerView;
    RecyclerView right_recyclerView;

    NestedScrollView questionScrollView;
    View match_these_layout;
    Boolean isBoard;

    public static boolean isMatchTheseSubmitted = false;
    private String maxLengthText;

    private long questionStartTime = 0L, questionEndTime;

    CardView difficultyCard;
    TextView difficultyType;
    private ArivihanCustomDialog caseStudyDialog;
    private String testSeriesId;
    Handler handler = new Handler();
    Runnable runnable;
    TextView tagText;
    CardView tagCard;
    LinearLayout correct_answer_layout;
    CardView viewSolutionCard;
    ViewTreeObserver observer;
    boolean dataLoaded;
    private boolean clickedFromUpdate;

    public MCQAndSubjectiveQuestionFragment() {
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(ARG_PARAM1, dto);
        outState.putSerializable(ARG_PARAM2, testSeriesQuestionDtoAnalytics);
        outState.putSerializable(ARG_PARAM3, (Serializable) map);
        outState.putInt(ARG_PARAM5, fragNumber);
        outState.putBoolean("isBoard", isBoard);
        outState.putString(ARG_PARAM6, testSeriesId);
    }

    public static MCQAndSubjectiveQuestionFragment newInstance(int fragNumber, TestSeriesQuestionDto dto,
                                                               TestSeriesQuestionDto testSeriesQuestionDtoAnalytics,
                                                               Map<String, Integer> map, Boolean isBoard, String testSeriesId, boolean isCurrentFragment) {
        MCQAndSubjectiveQuestionFragment fragment = new MCQAndSubjectiveQuestionFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, dto);
        args.putSerializable(ARG_PARAM2, testSeriesQuestionDtoAnalytics);
        args.putSerializable(ARG_PARAM3, (Serializable) map);
        args.putInt(ARG_PARAM5, fragNumber);
        args.putBoolean("isBoard", isBoard);
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
            isBoard = getArguments().getBoolean("isBoard");
            questionStartTime = System.currentTimeMillis();
            testSeriesId = getArguments().getString("testSeriesId");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mock_test_question, container, false);
    }

    @SneakyThrows
    @Override
    public void onViewCreated(@NonNull View fview, @Nullable Bundle savedInstanceState) {

//        View v = inflater.inflate(R.layout.fragment_mock_test_question, container, false);

        View v = fview;
        Type = MyApplication.getStringData(PreferenceKeys.RESPONSE.getKey());

        viewSolutionCard = v.findViewById(R.id.view_solution_card);
        correct_answer_layout = v.findViewById(R.id.correct_answer_layout);
        markLayout = v.findViewById(R.id.mark);
        mark = v.findViewById(R.id.markText);
        markButton = v.findViewById(R.id.mark_button2);
        ansText = v.findViewById(R.id.CorrectAnswer);
        ansTextLabel = v.findViewById(R.id.CorrectAnswerLabel);

        clearAnswer = v.findViewById(R.id.button_clear_answer);
        celebration = v.findViewById(R.id.celebration);
        responsetext = v.findViewById(R.id.YourResponse);
        parentLayout = v.findViewById(R.id.ParentRelativeLayout);
        subText = v.findViewById(R.id.subText);
        markLinearlayout = v.findViewById(R.id.markLinearLayout);
        correctPercentage = v.findViewById(R.id.Correctionpercentage);
        questionType = v.findViewById(R.id.questionType);
        saveAnswer = v.findViewById(R.id.button_answer_save);
        saveAnswerText = v.findViewById(R.id.btn_text);
        subjectiveAnswerInputlayout = v.findViewById(R.id.integerAnswerrr);
        subjectiveAnswer = v.findViewById(R.id.integerAnswer);
        numericalAnswer = v.findViewById(R.id.NumericalAnswer);
        relativeLayoutQuestion = v.findViewById(R.id.relative_layout_ques);
        greenMarks = v.findViewById(R.id.green_marks);
        greenMarksText = v.findViewById(R.id.green_marks_text);
        redMarks = v.findViewById(R.id.red_marks);
        redMarksText = v.findViewById(R.id.red_marks_text);
        solutionLayout = v.findViewById(R.id.solution_layout);
        solutionText = v.findViewById(R.id.solution_text);
        questionScrollView = v.findViewById(R.id.question_scroll_view);
        solutionImage = v.findViewById(R.id.solution_image);
        solutionImageCard = v.findViewById(R.id.solution_image_card);
        askDoubtFab = v.findViewById(R.id.ask_doubt_fab);
        caseStudyFab = v.findViewById(R.id.case_study_fab);
        caseStudyCard = v.findViewById(R.id.case_study_fab_card);
        caseStudyText = v.findViewById(R.id.case_study_fab_text);
        caseStudyNumCard = v.findViewById(R.id.case_study_num);
        caseStudyNumText = v.findViewById(R.id.case_study_num_text);

        tagText = v.findViewById(R.id.tag_text);
        tagCard = v.findViewById(R.id.tag_card);

        //askDoubtFabBtn = v.findViewById(R.id.ask_doubt_fab_button);
        subjectiveAnswer.setOnEditorActionListener((v13, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                saveAnswer.performClick();
                return true;
            }
            return false;
        });

        numericalAnswer.setOnEditorActionListener((v13, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                saveAnswer.performClick();
                return true;
            }
            return false;
        });

        askDoubtFab.setOnClickListener(view -> {
            try {
                String chatSessionId = CommonMethods.stringToMd5("mtq_" + dto.getQuestionId() + "_" + MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()) + "_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
                CommonNavigator.navigateToDoubtChatMockTest(getActivity(), chatSessionId, "physics", dto.getQuestionText(), dto.getAnswer());
            } catch (Exception e) {
                Log.i("ARIVIHAN_EXCEPTION", "Unable to start activity ChatSupportActivity.class");
            }
        });

        yourSubmissionResponseText = v.findViewById(R.id.response_your_submission);

        if (dto == null) {
            Log.d("dtoObject", "Dto is null");
            dto = optionListener.getDto(fragNumber - 1);
        }

        maped = map;

//        Log.d(TAG,"Question year: "+((dto.getYear()!=null)?dto.getYear():""));
        if(StringUtils.isNotEmpty(dto.getYear())) {
            tagCard.setVisibility(View.VISIBLE);
            tagText.setText(dto.getYear());
        } else {
            tagCard.setVisibility(View.GONE);
        }
        question = v.findViewById(R.id.question);
        questionImage = v.findViewById(R.id.question_image);
        difficultyCard = v.findViewById(R.id.difficulty_card);
        difficultyType = v.findViewById(R.id.difficulty_type);
        try {
            dto.getQuestionText();
        } catch (Exception e) {
            setViewPagerDataForCrashlytics();
            MyApplication.getFirebaseInstance().log("Generating custom exception");
            MyApplication.getFirebaseInstance().recordException(e);
            dto = new TestSeriesQuestionDto();
        }

        if (dto != null && StringUtils.isNotEmpty(dto.getQuestionText()))
            question.setText(dto.getQuestionText());
        else
            question.setVisibility(View.GONE);
        RecyclerView recyclerView = v.findViewById(R.id.recyclerView);

        // Code Fore SubText---e
//        dto != null && (StringUtils.isNotEmpty(dto.getSubtext()) ||
        if (dto != null && (StringUtils.isNotEmpty(dto.getOptionalTag()) || StringUtils.isNotEmpty(dto.getSubtext()))) {
            if (StringUtils.isNotEmpty(dto.getSubtext()) && !dto.getSubtext().contains("Marks")) {
                subText.setVisibility(View.VISIBLE);
                subText.setText(dto.getSubtext());
            } else {
                subText.setVisibility(View.GONE);
            }

            if (dto.getInfoType() == null || dto.getInfoType() == InfoType.INFO) {
                subText.setBackgroundColor(Color.WHITE);
            } else {
                subText.setBackgroundColor(Color.WHITE);
            }
        } else {
            subText.setVisibility(View.GONE);
        }


        if ((dto.getQuestionTypeEnum() == QuestionType.SINGLE_CHOICE
                || dto.getQuestionTypeEnum() == QuestionType.MULTI_CHOICE) && !CollectionUtils.isEmpty(dto.getOptions())) {
            yourSubmissionResponseText.setText(getString(R.string.select_a_option));

            if (dto.getOptionDtos() == null) {
                optionDtos = new ArrayList<>();
                for (String option : dto.getOptions()) {
                    if (testSeriesQuestionDtoAnalytics != null) {
                        optionDtos.add(new OptionDto(option, false, dto.getAnswer(), testSeriesQuestionDtoAnalytics.getUserAnswer(), "TEXT"));
                    } else {
                        optionDtos.add(new OptionDto(option, false, dto.getAnswer(), dto.getUserAnswer(), "TEXT"));
                    }
                }
                dto.setOptionDtos(optionDtos);
            } else {
                optionDtos = dto.getOptionDtos();
            }
            if (dto.getQuestionText() != null) {
                optionAdapter = new OptionAdapter(dto.getQuestionId(), dto.getQuestionText(), optionDtos, dto.getQuestionTypeEnum(), getContext(), this);
            } else {
                optionAdapter = new OptionAdapter(dto.getQuestionId(), "", optionDtos, dto.getQuestionTypeEnum(), getContext(), this);
            }

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(optionAdapter);

//            subjectiveAnswerInputlayout.setVisibility(View.GONE);
        }

        if (dto.getQuestionTypeEnum() == QuestionType.NUMERICAL || dto.getQuestionTypeEnum() == QuestionType.SUBJECTIVE || dto.getQuestionTypeEnum() == QuestionType.FILL_IN_THE_BLANKS || dto.getQuestionTypeEnum() == QuestionType.ONE_WORD_ANSWER) {
            yourSubmissionResponseText.setText(getString(R.string.type_your_answer));

            recyclerView.setVisibility(View.GONE);
            if (dto.getQuestionTypeEnum() == QuestionType.SUBJECTIVE || dto.getQuestionTypeEnum() == QuestionType.FILL_IN_THE_BLANKS || dto.getQuestionTypeEnum() == QuestionType.ONE_WORD_ANSWER) {
//                subjectiveAnswerInputlayout.setVisibility(View.VISIBLE);
                subjectiveAnswer.setVisibility(View.VISIBLE);
                subjectiveAnswer.requestFocus();
            } else {
                numericalAnswer.setVisibility(View.VISIBLE);
                numericalAnswer.requestFocus();
            }
            if (Type.equals(PreferenceKeys.RESPONSE.getKey())) {
                clearAnswer.setVisibility(View.INVISIBLE);
                if (dto.getQuestionTypeEnum() == QuestionType.SUBJECTIVE || dto.getQuestionTypeEnum() == QuestionType.FILL_IN_THE_BLANKS || dto.getQuestionTypeEnum() == QuestionType.ONE_WORD_ANSWER) {
                    subjectiveAnswer.setVisibility(View.VISIBLE);
                    subjectiveAnswer.setEnabled(false);
//                    subjectiveAnswerInputlayout.setVisibility(View.VISIBLE);
//                    subjectiveAnswerInputlayout.setEnabled(false);
                    if (StringUtils.isNotEmpty(dto.getUserAnswer())) {
//                        subjectiveAnswerInputlayout.setHint(getString(R.string.your_submitted_answer));
//                        subjectiveAnswerInputlayout.getEditText().setText(dto.getUserAnswer());
                        subjectiveAnswer.setText(dto.getUserAnswer());
                    } else {
//                        subjectiveAnswerInputlayout.setHint("");
//                        subjectiveAnswerInputlayout.getEditText().setText("");
                        subjectiveAnswer.setText("");
                    }
                }

                if (dto.getQuestionTypeEnum() == QuestionType.NUMERICAL) {
                    if (dto.getUserAnswer() == null) {
                        numericalAnswer.setBackground(getResources().getDrawable(R.drawable.question_option_border));
                    } else if (dto.isCorrect()) {
                        numericalAnswer.setBackground(getResources().getDrawable(R.drawable.question_option_border_green));
                    } else {
                        numericalAnswer.setBackground(getResources().getDrawable(R.drawable.question_option_border_red));
                    }
                    numericalAnswer.setText(dto.getUserAnswer());
                    numericalAnswer.setEnabled(false);
                }else{
                    if (dto.getUserAnswer() == null) {
                        subjectiveAnswer.setBackground(getResources().getDrawable(R.drawable.question_option_border));
                    } else if (dto.isCorrect()) {
                        subjectiveAnswer.setBackground(getResources().getDrawable(R.drawable.question_option_border_green));
                    } else {
                        subjectiveAnswer.setBackground(getResources().getDrawable(R.drawable.question_option_border_red));
                    }
                    subjectiveAnswer.setText(dto.getUserAnswer());
                    subjectiveAnswer.setEnabled(false);
                }

                if (dto.isCorrect()) {
                    correct_answer_layout.setVisibility(View.GONE);
                } else {
                    correct_answer_layout.setVisibility(View.VISIBLE);
                }

                ansTextLabel.setVisibility(View.VISIBLE);
                ansText.setVisibility(View.VISIBLE);
                ansText.setText(dto.getAnswer());
                if (testSeriesQuestionDtoAnalytics != null) {
                    if (dto.getQuestionTypeEnum() == QuestionType.NUMERICAL) {
                        if (dto.getUserAnswer() != null) {
                            String S = dto.getUserAnswer();
                            String s = "";
                            for (int i = 0; i < S.length(); i++) {
                                if (S.charAt(i) == '.')
                                    break;
                                s += S.charAt(i);
                            }
                            if (s.isEmpty())
                                s = "0";
                            double num = Double.parseDouble(s);
                            double numD = CommonMethods.parseDouble(dto.getUserAnswer());
                            if (numD == num)
                                responsetext.setText(String.format(getResources().getString(R.string.your_response) + num));
                            else
                                responsetext.setText(String.format(getResources().getString(R.string.your_response) + dto.getUserAnswer()));
                        } else {
                            responsetext.setText(getResources().getString(R.string.no_attempt));
                        }
                    }
                } else if (dto.getUserAnswer() != null) {
                    if (dto.getQuestionTypeEnum() == QuestionType.NUMERICAL) {
                        if (dto.getUserAnswer() != null) {
                            String S = dto.getUserAnswer();
                            String s = "";
                            for (int i = 0; i < S.length(); i++) {
                                if (S.charAt(i) == '.')
                                    break;
                                s += S.charAt(i);
                            }
                            if (s.isEmpty())
                                s = "0";
                            double num = Double.parseDouble(s);
                            double numD = CommonMethods.parseDouble(dto.getUserAnswer());
                            if (numD == num)
                                responsetext.setText(String.format(getResources().getString(R.string.your_response) + num));
                            else
                                responsetext.setText(String.format(getResources().getString(R.string.your_response) + dto.getUserAnswer()));
                        } else {
                            responsetext.setText(getResources().getString(R.string.no_attempt));
                        }
                    }
                }


                if (dto.getQuestionTypeEnum() == QuestionType.NUMERICAL) {
                    double s = CommonMethods.parseDouble(dto.getAnswer());
                    int ss = (int) s;
                    if ((double) ss == s) {
                        ansText.setText(String.valueOf(ss));
                    } else {
                        ansText.setText(dto.getAnswer());
                    }
                }

                if ((testSeriesQuestionDtoAnalytics != null && testSeriesQuestionDtoAnalytics.isCorrect()) || dto.isCorrect()) {
                    responsetext.setTextColor(ContextCompat.getColor(requireContext(), R.color.correct_answer_text_color));
                } else {

                    if ((testSeriesQuestionDtoAnalytics != null && StringUtils.isNotEmpty(testSeriesQuestionDtoAnalytics.getUserAnswer()))
                            || StringUtils.isNotEmpty(dto.getUserAnswer()))
                        responsetext.setTextColor(ContextCompat.getColor(requireContext(), R.color.wrong_answer_text_color));
                    else
                        responsetext.setTextColor(Color.BLACK);
                }
            }
        }

        if (dto.getQuestionTypeEnum() == QuestionType.SINGLE_CHOICE_IMAGE || dto.getQuestionTypeEnum() == QuestionType.MULTI_CHOICE_IMAGE) {
            yourSubmissionResponseText.setText(getString(R.string.select_a_option));

            optionDtos = new ArrayList<>();
            if (dto.getImageOptions() != null) {
                for (String option : dto.getImageOptions()) {
                    if (testSeriesQuestionDtoAnalytics != null) {
                        optionDtos.add(new OptionDto(option, false, dto.getAnswer(), testSeriesQuestionDtoAnalytics.getUserAnswer(), "IMAGE"));
                    } else {
                        optionDtos.add(new OptionDto(option, false, dto.getAnswer(), dto.getUserAnswer(), "IMAGE"));
                    }
                }
            }


            if (dto.getQuestionText() != null) {
                optionAdapter = new OptionAdapter(dto.getQuestionId(), dto.getQuestionText(), optionDtos, dto.getQuestionTypeEnum(), getContext(), this);
            } else {
                optionAdapter = new OptionAdapter(dto.getQuestionId(), "", optionDtos, dto.getQuestionTypeEnum(), getContext(), this);
            }
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(optionAdapter);

//            subjectiveAnswerInputlayout.setVisibility(View.GONE);
            subjectiveAnswer.setVisibility(View.GONE);
//            numericalAnswerInputlayout.setVisibility(View.GONE);

        }


        if (dto.getQuestionTypeEnum() == QuestionType.MATCH_THE_COLUMN) {

//            saveAnswerText.setText("Submit");

            match_these_layout = v.findViewById(R.id.match_these);
            match_these_layout.setVisibility(View.VISIBLE);


            left_recyclerView = v.findViewById(R.id.rv_left_options);
            right_recyclerView = v.findViewById(R.id.rv_right_options);

            setDefaultMatchTheseAnswer();
            correctAnswerList = new ArrayList<>();
            userAnswerList = new ArrayList<>();
            list_left = new ArrayList<>();
            list_right = new ArrayList<>();
            maxLengthText = "";
            setLeftList(dto.options);
            setRightList(dto.answers);
            //findMaxString(list_left,list_right);

            matchTheseAdapterLeft = new MatchTheseAdapterLeft(list_left, getContext());
            left_recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            left_recyclerView.setAdapter(matchTheseAdapterLeft);
            setCorrectAnswerList(dto.answer);

            right_recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            rearrangeOrder();
        }


    if (StringUtils.isNotEmpty(dto.getQuestionImage())) {
        Glide.with(this).load(dto.getQuestionImage()).into(questionImage);
        questionImage.setVisibility(View.VISIBLE);
        questionImage.setOnClickListener(vi -> {
            fullScreenImageDialog = new FullScreenImageDialog(requireContext(),
                    dto.getQuestionImage(), FullViewImageType.QUESTION);
            fullScreenImageDialog.show();
        });
    }


        if (dto.getQuestionType().equals("SINGLE_CHOICE") || dto.getQuestionType().equals("SINGLE_CHOICE_IMAGE")) {
            questionType.setText(getResources().getString(R.string.single_choice));
        } else if (dto.getQuestionType().equals("MULTI_CHOICE") || dto.getQuestionType().equals("MULTI_CHOICE_IMAGE")) {
            questionType.setText(getResources().getString(R.string.multi_choice));
        } else if (dto.getQuestionType().equals("ONE_WORD_ANSWER")) {
            questionType.setText(getResources().getString(R.string.one_word_answer));
        } else if (dto.getQuestionType().equals("FILL_IN_THE_BLANKS")) {
            questionType.setText(getResources().getString(R.string.Fill_in_the_blank));
        } else if (dto.getQuestionType().equals("TRUE_FALSE")) {
            questionType.setText(getResources().getString(R.string.True_or_False));
        } else if (dto.getQuestionType().equals("SUBJECTIVE")) {
            questionType.setText(getResources().getString(R.string.subjective));
        } else if (dto.getQuestionType().equals("NUMERICAL")) {
            questionType.setText(getResources().getString(R.string.numerical));
        }


        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Intent data = result.getData();
                    if (data != null) {
                        Bundle extras = data.getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        ImageView subjectiveImageView = v.findViewById(R.id.subjective_answer_image);

                        subjectiveImageView.setVisibility(View.VISIBLE);
                        subjectiveImageView.setImageBitmap(imageBitmap);
                        saveAnswerText.setText("Save");

                    }
                }
        );


        saveAnswer.setOnClickListener(vivew -> {
            if(Type.equals(PreferenceKeys.RESPONSE.getKey())){
                optionListener.nextPage(fragNumber);
            }else {
                questionEndTime = System.currentTimeMillis();

                if (dto.getTimeTaken() == null) {
                    dto.setTimeTaken(questionEndTime - questionStartTime);
                } else {
                    dto.setTimeTaken(dto.getTimeTaken() + (questionEndTime - questionStartTime));
                }
                hideKeybaord(v);

                if (dto.getQuestionTypeEnum() == QuestionType.MATCH_THE_COLUMN) {
                    isMatchTheseSubmitted = true;
                    setUserMatchTheseAnswer();
                }
                if (dto.getQuestionTypeEnum() == QuestionType.SUBJECTIVE || dto.getQuestionType().equals(QuestionType.ONE_WORD_ANSWER.name()) || dto.getQuestionType().equals(QuestionType.FILL_IN_THE_BLANKS.name())) {
                    if (StringUtils.isEmpty(subjectiveAnswer.getText())) {
                        CommonMethods.showNegativeSnackbar(relativeLayoutQuestion, getString(R.string.Please_enter_an_answer_first_or_Swipe));
//                    subjectiveAnswerInputlayout.setError("");
                    } else {
                        if (StringUtils.isEmpty(dto.getOptionalTag()) || dto.getUserAnswer() != null) {
                            dto.setUserAnswer(subjectiveAnswer.getText().toString());
                            update(true);
                            //CommonMethods.showPositiveSnackbar(relativeLayoutQuestion, getString(R.string.saved));
                            subjectiveAnswer.setBackground(getResources().getDrawable(R.drawable.button_border));
                        } else {
                            Integer value = maped.get(dto.getOptionalTag());
                            if (value == null) {
                                value = -1;
                            }
                            if (value == 0) {
                                CommonMethods.showLongNegativeSnackbar(recyclerView, getString(R.string.optional_error));
                            } else {
                                maped.put(dto.getOptionalTag(), value - 1);
                                dto.setUserAnswer(subjectiveAnswer.getText().toString());
                                //CommonMethods.showPositiveSnackbar(relativeLayoutQuestion, getString(R.string.saved));
                                update(true);
                            }
                        }
                    }
                } else if (dto.getQuestionTypeEnum() == QuestionType.NUMERICAL) {
                    if (StringUtils.isEmpty(numericalAnswer.getText())) {

                        CommonMethods.showNegativeSnackbar(relativeLayoutQuestion, getString(R.string.Please_enter_an_answer_first_or_Swipe));
//                    numericalAnswerInputlayout.setError("");
                    } else {
                        if (StringUtils.isEmpty(dto.getOptionalTag()) || dto.getUserAnswer() != null) {
                            //CommonMethods.showPositiveSnackbar(relativeLayoutQuestion, getString(R.string.saved));
                            dto.setUserAnswer(numericalAnswer.getText().toString());
                            update(true);

                        } else {
                            Integer value = maped.get(dto.getOptionalTag());
                            if (value == null) {
                                value = -1;
                            }
                            if (value == 0) {
                                CommonMethods.showNegativeSnackbar(relativeLayoutQuestion, getString(R.string.optional_error));
                            } else {
                                numericalAnswer.setBackground(getResources().getDrawable(R.drawable.button_border));
                                maped.put(dto.getOptionalTag(), value - 1);
                                dto.setUserAnswer(numericalAnswer.getText().toString());
                                //CommonMethods.showPositiveSnackbar(relativeLayoutQuestion, getString(R.string.saved));
                                update(true);

                            }

                        }
                    }
                } else if (dto.getQuestionTypeEnum() == QuestionType.SINGLE_CHOICE_IMAGE || dto.getQuestionTypeEnum() == QuestionType.MULTI_CHOICE_IMAGE) {
                    if (!ifAnyOptionsSelected(optionDtos)) {
                        CommonMethods.showNegativeSnackbar(markLayout, getString(R.string.please_select_your_option));
                    } else {
                        if (StringUtils.isEmpty(dto.getOptionalTag()) || dto.getUserAnswer() != null) {
                            dto.setUserAnswer(createAnswer(optionDtos, dto.getQuestionTypeEnum()));
                            update(true);
                            //CommonMethods.showPositiveSnackbar(relativeLayoutQuestion, getString(R.string.saved));

                        } else {
                            Integer value = maped.get(dto.getOptionalTag());
                            if (value == null) {
                                value = -1;
                            }
                            if (value == 0) {
                                CommonMethods.showNegativeSnackbar(recyclerView, getString(R.string.optional_error));
                            } else {
                                maped.put(dto.getOptionalTag(), value - 1);
                                dto.setUserAnswer(createAnswer(optionDtos, dto.getQuestionTypeEnum()));
                                //CommonMethods.showPositiveSnackbar(relativeLayoutQuestion, getString(R.string.saved));
                                update(true);

                            }
                        }

                    }
                } else if ((dto.getQuestionTypeEnum() == QuestionType.SINGLE_CHOICE || dto.getQuestionTypeEnum() == QuestionType.MULTI_CHOICE)) {
                    if (!ifAnyOptionsSelected(optionDtos)) {
                        CommonMethods.showNegativeSnackbar(markLayout, getString(R.string.please_select_your_option));
                    } else {
                        if (StringUtils.isEmpty(dto.getOptionalTag()) || dto.getUserAnswer() != null) {
                            dto.setUserAnswer(createAnswer(optionDtos, dto.getQuestionTypeEnum()));
                            update(true);
                           // CommonMethods.showPositiveSnackbar(relativeLayoutQuestion, getString(R.string.saved));
                        } else {
                            Integer value = maped.get(dto.getOptionalTag());
                            if (value == null) {
                                value = -1;
                            }
                            if (value == 0) {
                                CommonMethods.showNegativeSnackbar(recyclerView, getString(R.string.optional_error));
                            } else {
                                maped.put(dto.getOptionalTag(), value - 1);
                                dto.setUserAnswer(createAnswer(optionDtos, dto.getQuestionTypeEnum()));
                               // CommonMethods.showPositiveSnackbar(relativeLayoutQuestion, getString(R.string.saved));
                                update(true);
                            }
                        }
                    }
                }
            }
        });

        clearAnswer.setOnClickListener(v12 -> {
            dto.setUserAnswer(null);
            if (StringUtils.isNotEmpty(dto.getOptionalTag())) {
                Integer value = maped.get(dto.getOptionalTag());
                if (value == null) {
                    value = -1;
                }
                maped.put(dto.getOptionalTag(), value + 1);
            }
            update2();
        });

        if (dto.isMarked()) {
            changeMark();
        }

        markLayout.setOnClickListener(v1 -> {
            dto.setMarked(!dto.isMarked());
            changeMark();
        });

        if (dto.getNegativeScore() != 0) {
            redMarks.setVisibility(View.VISIBLE);
            redMarksText.setText("-" + dto.getNegativeScore());
        } else {
            redMarks.setVisibility(View.GONE);
        }
        greenMarksText.setText("+" + dto.getScore());
        caseStudyFab.setOnClickListener(view -> openCaseStudy());
        if(StringUtils.isNotEmpty(dto.getComprehensiveQuesNo()) &&
                (StringUtils.isNotEmpty(dto.getComprehensiveCaseStudyText()) || StringUtils.isNotEmpty(dto.getComprehensiveCaseStudyImage()))) {
            caseStudyNumCard.setVisibility(View.VISIBLE);
            caseStudyNumText.setText(dto.getComprehensiveQuesNo());
            caseStudyFab.setVisibility(View.VISIBLE);
        }
            if (Type.equals(PreferenceKeys.RESPONSE.getKey())) {

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
            if (dto.getExplanationImage() != null) {
                solutionLayout.setVisibility(View.VISIBLE);
                solutionImage.setVisibility(View.VISIBLE);
                solutionImage.loadImage( dto.getExplanationImage());
                solutionImage.setOnClickListener(vi -> {
                    fullScreenImageDialog = new FullScreenImageDialog(requireContext(),
                            dto.getExplanationImage(),FullViewImageType.SOLUTION);
                    fullScreenImageDialog.show();
                });
            }
            askDoubtFab.setVisibility(View.VISIBLE);
            if(StringUtils.isNotEmpty(dto.getComprehensiveCaseStudyText()) || StringUtils.isNotEmpty(dto.getComprehensiveCaseStudyImage())) {
                caseStudyFab.setVisibility(View.VISIBLE);
                caseStudyCard.setCardBackgroundColor(requireContext().getColor(R.color.mock_light_blue));
                caseStudyText.setTextColor(requireContext().getColor(R.color.subscription_blue));
            }

            //celebration.setVisibility(View.VISIBLE);
            saveAnswerText.setText(getString(R.string.next_with_space));
            clearAnswer.setVisibility(View.INVISIBLE);
            markButton.setVisibility(View.GONE);
            mark.setVisibility(View.GONE);
            if (StringUtils.isNotEmpty(dto.getExplanation())) {
                solutionLayout.setVisibility(View.VISIBLE);
                solutionText.setText(HtmlCompat.fromHtml(dto.getExplanation().trim(), HtmlCompat.FROM_HTML_MODE_LEGACY));
            }

            yourSubmissionResponseText.setVisibility(View.VISIBLE);
            if (dto.getVideoUrl() != null) {
                solutionLayout.setVisibility(View.VISIBLE);
                solutionImageCard.setVisibility(View.VISIBLE);
                solutionImageCard.setOnClickListener(vi -> {
                    Intent intent = new Intent(getActivity(), ExoPlayerActivity.class);
                    intent.putExtra("video_uri", dto.getVideoUrl());
                    intent.putExtra(PreferenceKeys.VIDEO_STARTING.getKey(), dto.getVideoStartingTime());
                    intent.putExtra("video_ending", dto.getVideoEndingTime());
                    startActivity(intent);
                });
            }

            if (dto.getUserAnswer() == null) {
                yourSubmissionResponseText.setText(getString(R.string.no_response));
                if (dto.getNegativeScore() != 0) {
                    redMarks.setVisibility(View.VISIBLE);
                    redMarksText.setText("-" + dto.getNegativeScore());
                } else {
                    redMarks.setVisibility(View.GONE);
                }
                redMarksText.setTextColor(getContext().getResources().getColor(R.color.grey_500));
                redMarks.setCardBackgroundColor(getContext().getResources().getColor(R.color.grey_100));
                greenMarksText.setTextColor(getContext().getResources().getColor(R.color.grey_500));
                greenMarks.setCardBackgroundColor(getContext().getResources().getColor(R.color.grey_100));
                greenMarksText.setText("+" + dto.getScore());
            } else {
                yourSubmissionResponseText.setText(getString(R.string.your_submitted_answer));

                if (dto.isCorrect()) {
                    greenMarks.setCardBackgroundColor(getContext().getResources().getColor(R.color.mock_light_green));
                    greenMarksText.setTextColor(getContext().getResources().getColor(R.color.subscription_green));
                    greenMarksText.setText("+" + dto.getUserScore());
                    redMarks.setCardBackgroundColor(getContext().getResources().getColor(R.color.grey_100));
                    redMarksText.setTextColor(getContext().getResources().getColor(R.color.grey_500));

                } else {
                    if (dto.getNegativeScore() != 0) {
                        redMarks.setVisibility(View.VISIBLE);
                        redMarksText.setText("-" + dto.getNegativeScore());
                    } else {
                        redMarks.setVisibility(View.GONE);
                    }
                    greenMarksText.setTextColor(getContext().getResources().getColor(R.color.grey_500));
                    greenMarks.setCardBackgroundColor(getContext().getResources().getColor(R.color.grey_100));
                }
            }

        }
        handleViewSolution();
//        return v;

    }

    private void handleViewSolution(){
        if (Type.equals(PreferenceKeys.RESPONSE.getKey())) {
            observer = questionScrollView.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(() -> {
                int viewHeight = questionScrollView.getMeasuredHeight();
                int contentHeight = questionScrollView.getChildAt(0).getHeight();
                if (viewHeight - contentHeight < 0 && !dataLoaded) {
                    setVisibility();
                    dataLoaded=true;
                }
            });

            questionScrollView.setOnScrollChangeListener((View.OnScrollChangeListener) (view, i, i1, i2, i3) -> setVisibility());
        }
        viewSolutionCard.setOnClickListener(view -> {
            optionListener.onViewSolutionClick();
            viewSolutionCard.setVisibility(View.GONE);
            questionScrollView.fullScroll(View.FOCUS_DOWN);
        });
    }

    private void setVisibility() {
        if (questionScrollView.canScrollVertically(1) && !dto.isHideViewSolution()) {
             viewSolutionCard.setVisibility(View.VISIBLE);
        } else {
            viewSolutionCard.setVisibility(View.GONE);
        }
    }

    private void performClickOnCaseStudy(){
        List<String> group = MyApplication.getCaseStudyGroupList(testSeriesId);
        if(CollectionUtils.isEmpty(group))
            group=new ArrayList<>();
        String key = testSeriesId+dto.getCaseStudyGroup();
        Log.d("LAFragmenet", "key check " + key);
        if(!group.contains(key)){
            handler = new Handler();
            List<String> finalGroup = group;
            runnable = () -> {
                caseStudyFab.performClick();
                finalGroup.add(key);
                MyApplication.setCaseStudyGroupList(testSeriesId, finalGroup);
            };
            handler.postDelayed(runnable,2000);
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



    private void setDefaultMatchTheseAnswer() {
        userAnswer = "A-1";
        for (int i = 1; i < list_left.size(); i++) {
            userAnswer = "," + userAnswer + (char) (65 + i) + "-" + i;
        }
    }

    private void rearrangeOrder() {

        matchTheseAdapterRight = new MatchTheseAdapterRight(list_right, getContext());
        ItemTouchHelper.Callback callback = new MyItemTouchHelper(matchTheseAdapterRight);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        matchTheseAdapterRight.setTouchHelper(itemTouchHelper);
        itemTouchHelper.attachToRecyclerView(right_recyclerView);
        right_recyclerView.setAdapter(matchTheseAdapterRight);
    }

    void setCorrectAnswerList(String answer) {
        String[] result = answer.split(",");
        correctAnswerList.clear();
        correctAnswerList = Arrays.asList(result);
    }


    public void setUserMatchTheseAnswer() {
        userAnswerList.clear();
        for (int i = 0; i < list_left.size(); i++) {
            userAnswerOption = list_left.get(i).getOption_number() + "-" + list_right.get(i).getOption_number();
            if (correctAnswerList.get(i).equals(userAnswerOption)) {
                list_right.get(i).setCorrectlyMatched(true);
                list_left.get(i).setCorrectlyMatched(true);
            }
            userAnswerList.add(userAnswerOption);
            userAnswer = userAnswerOption + userAnswer;
        }
        matchTheseAdapterRight.notifyDataSetChanged();
        matchTheseAdapterLeft.notifyDataSetChanged();
    }

    void findMaxString(ArrayList<MatchTheseLeftOption> left, ArrayList<MatchTheseRightOption> right) {
        String max = "";
        for (int i = 0; i < left.size(); i++) {
            if (max.length() < left.get(i).getValue().length()) {
                max = left.get(i).getValue();
            }
            if (max.length() < right.get(i).getValue().length()) {
                max = right.get(i).getValue();
            }
        }
        list_left.add(0, new MatchTheseLeftOption('A', max, false));
    }

    void setLeftList(List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            char c = (char) (65 + i);
            list_left.add(new MatchTheseLeftOption(c, list.get(i)));
            int max = Math.max(maxLengthText.length(), list.get(i).length());
            if (max == list.get(i).length()) {
                maxLengthText = list.get(i);
            }
        }
    }

    void setRightList(List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            list_right.add(new MatchTheseRightOption(i + 1, list.get(i)));
            int max = Math.max(maxLengthText.length(), list.get(i).length());
            if (max == list.get(i).length()) {
                maxLengthText = list.get(i);
            }
        }
    }


    private void hideKeybaord(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
    }

    public void updateView(int position, boolean last) {
        try {
            if(Type.equals(PreferenceKeys.RESPONSE.getKey()) && last){
                saveAnswer.setVisibility(View.GONE);
            }
            if (StringUtils.isNotEmpty(dto.getUserAnswer())) {
                subjectiveAnswer.clearFocus();
                numericalAnswer.clearFocus();
                if (!Type.equals(PreferenceKeys.RESPONSE.getKey())) {
                    clearAnswer.setVisibility(View.VISIBLE);
                }
                if (dto.getQuestionTypeEnum() == QuestionType.NUMERICAL || dto.getQuestionTypeEnum() == QuestionType.SUBJECTIVE) {
                    //integerAnswer.setText(dto.getUserAnswer());
                    //integerAnswer.setSelection(dto.getUserAnswer().length());
                    // update();
                } else if (dto.getQuestionTypeEnum() == QuestionType.SINGLE_CHOICE) {
                    for (OptionDto optionDto : optionDtos) {
                        if (optionDto.getOptionText().equalsIgnoreCase(dto.getUserAnswer())) {
                            optionDto.setSelected(true);
                            update(false);
                        } else {
                            optionDto.setSelected(false);
                        }
                    }
                } else if (dto.getQuestionTypeEnum() == QuestionType.SINGLE_CHOICE_IMAGE) {
                    for (double i = 0; i <= optionDtos.size(); i++) {
                        try {
                            String s = String.valueOf(i).substring(0, 3);
                            String st = s.substring(0, 1);
                            int index = Integer.parseInt(st) - 1;
                            if (dto.getUserAnswer().equals(s)) {
                                optionDtos.get(index).setSelected(true);
                                update(false);
                            } else {
                                optionDtos.get(index).setSelected(false);
                            }
                        } catch (Exception e) {
                            Log.d("Error", e.getMessage());
                        }
                    }

                } else if (dto.getQuestionTypeEnum() == QuestionType.MULTI_CHOICE || dto.getQuestionTypeEnum() == QuestionType.MULTI_CHOICE_IMAGE) {
                    String userAnswer = dto.getUserAnswer();
                    String[] split = userAnswer.split(",");
                    Set<String> userAnswers = new HashSet<>();
                    for (String s : split) {
                        userAnswers.add(s.trim());
                        update(false);
                    }
                    for (OptionDto optionDto : optionDtos) {
                        if (userAnswers.contains(optionDto.getOptionText())) {
                            optionDto.setSelected(true);
                            update(false);
                        } else {
                            optionDto.setSelected(false);
                        }
                    }
                }
                //saveAnswerText.setText(getString(R.string.update));
            } else {
                subjectiveAnswer.setText("");
                numericalAnswer.setText("");
                if (!CollectionUtils.isEmpty(optionDtos)) {
                    for (OptionDto optionDto : optionDtos) {
                        optionDto.setSelected(false);
                    }
                }
            }
            if (optionAdapter != null) {
                optionAdapter.notifyDataSetChanged();
            }

            if (!Type.equals(PreferenceKeys.RESPONSE.getKey()) && caseStudyFab!=null && caseStudyFab.getVisibility()==View.VISIBLE) {
                clickedFromUpdate = true;
                caseStudyFab.performClick();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void update(boolean changePage) {
//        saveAnswerText.setText(getString(R.string.save_and_continue));
        dto.setAnswerState(AnswerState.ANSWERED);
        subjectiveAnswer.clearFocus();
        numericalAnswer.clearFocus();
        if (!Type.equals(PreferenceKeys.RESPONSE.getKey())) {
            clearAnswer.setVisibility(View.VISIBLE);
        }
        changeAnswerState(dto.isMarked());

        if (changePage) {
            optionListener.nextPage(fragNumber);
        }
    }

    private void update2() {
        subjectiveAnswer.setText("");
        numericalAnswer.setText("");
        if (dto.getQuestionTypeEnum() == QuestionType.SINGLE_CHOICE ||
                dto.getQuestionTypeEnum() == QuestionType.MULTI_CHOICE) {
            for (OptionDto optionDto : optionDtos) {
                optionDto.setSelected(false);
            }
        }
        if (optionAdapter != null)
            optionAdapter.notifyDataSetChanged();
        if (dto.getQuestionTypeEnum() == QuestionType.SINGLE_CHOICE_IMAGE) {
            for (OptionDto optionDto : optionDtos) {
                optionDto.setSelected(false);
            }
        }
        if (optionAdapter != null)
            optionAdapter.notifyDataSetChanged();

        saveAnswerText.setText(getString(R.string.save_and_continue));
        dto.setAnswerState(AnswerState.UNANSWERED);
        clearAnswer.setVisibility(View.INVISIBLE);
        changeAnswerState(dto.isMarked());
        optionListener.setSelected(-1);
        subjectiveAnswer.setBackground(getResources().getDrawable(R.drawable.question_option_border));
        numericalAnswer.setBackground(getResources().getDrawable(R.drawable.question_option_border));

    }

    @Override
    public void onResume() {
        super.onResume();
//        updateView(-1);
        subjectiveAnswer.clearFocus();
        numericalAnswer.clearFocus();
        setViewPagerDataForCrashlytics();
        if(!Type.equals(PreferenceKeys.RESPONSE.getKey()) && caseStudyFab!=null && caseStudyFab.getVisibility()==View.VISIBLE)
            performClickOnCaseStudy();
        if(Type.equals(PreferenceKeys.RESPONSE.getKey()) && dto.isHideViewSolution())
            viewSolutionCard.setVisibility(View.GONE);

    }

    @Override
    public void onPause() {
        super.onPause();

        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
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


    private String createAnswer(List<OptionDto> optionDtos, QuestionType questionTypeEnum) {
        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (OptionDto optionDto : optionDtos) {
            if (optionDto.isSelected()) {
                if (questionTypeEnum == QuestionType.SINGLE_CHOICE) {
                    return optionDto.getOptionText();
                } else if (questionTypeEnum == QuestionType.MULTI_CHOICE) {
                    sb.append(optionMultiCorrect[index - 1]).append(", ");
                } else if (questionTypeEnum == QuestionType.SINGLE_CHOICE_IMAGE) {
                    double x = index;
                    String s = String.valueOf(x);
                    return s;
                } else {
                    double x = index;
                    String s = String.valueOf(x);
                    sb.append(s + ", ");
                }
            }
            index++;
        }

        if (sb.toString().length() > 0) {
            int i = sb.lastIndexOf(", ");
            return sb.substring(0, i);
        }

        return sb.toString();
    }

    private boolean ifAnyOptionsSelected(List<OptionDto> optionDtos) {
        for (OptionDto optionDto : optionDtos) {
            if (optionDto.isSelected()) {
                return true;
            }
        }
        return false;
    }

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

    @Override
    public void setSelected(int i, String answer) {
//        recycler
//        Toast.makeText(getContext(), "OPTION CLICKED", Toast.LENGTH_SHORT).show();
        questionScrollView.smoothScrollTo(0, questionScrollView.getMeasuredHeight());
    }

    @Override
    public TestSeriesQuestionDto getDto(int fragNumber) {
        return null;
    }

    @Override
    public void submitAnswer() {

    }

    @Override
    public void nextPage(int i) {

    }
}