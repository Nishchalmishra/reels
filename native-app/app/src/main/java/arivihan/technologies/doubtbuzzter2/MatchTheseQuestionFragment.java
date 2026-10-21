package arivihan.technologies.doubtbuzzter2;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import arivihan.technologies.doubtbuzzter2.activity.ExoPlayerActivity;
import arivihan.technologies.doubtbuzzter2.adapter.MatchTheseAdapterLeft;
import arivihan.technologies.doubtbuzzter2.adapter.MatchTheseAdapterRight;
import arivihan.technologies.doubtbuzzter2.adapter.OptionAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.listener.OptionListener;
import arivihan.technologies.doubtbuzzter2.model.AnswerState;
import arivihan.technologies.doubtbuzzter2.model.InfoType;
import arivihan.technologies.doubtbuzzter2.model.MatchTheseLeftOption;
import arivihan.technologies.doubtbuzzter2.model.MatchTheseRightOption;
import arivihan.technologies.doubtbuzzter2.model.OptionDto;
import arivihan.technologies.doubtbuzzter2.model.QuestionType;
import arivihan.technologies.doubtbuzzter2.model.TestSeriesQuestionDto;
import lombok.SneakyThrows;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;


public class MatchTheseQuestionFragment extends Fragment implements OptionListener {

    int fragNumber;
    private CardView saveAnswer, clearAnswer;
    private TextView saveAnswerText, explanation, yourAttempt, correctPercentage;
    LottieAnimationView celebration;
    private RelativeLayout relativeLayoutQuestion, parentLayout;
    private EditText subjectiveAnswer, numericalAnswer;
    private TextInputLayout subjectiveAnswerInputlayout, numericalAnswerInputlayout;
    TestSeriesQuestionDto testSeriesQuestionDtoAnalytics = null;

    private String[] optionMultiCorrect = {"a", "b", "c", "d", "e", "f", "g", "h", "i"};
    private TextView question, questionType, mark, responsetext, ansText;
    String Type = "";
    private ImageView questionImage;
    private TestSeriesQuestionDto dto;
    Map<String, Integer> map;
    private OptionAdapter optionAdapter;
    Dialog dialog;
    Map<String, Integer> maped = new HashMap<>();
    TextView subText;
    private List<OptionDto> optionDtos = new ArrayList<>();
    private OptionListener optionListener;
    private LinearLayout markLayout, markLinearlayout;
    private RelativeLayout MarkLayout;
    private ToggleButton markButton;
    private static final String ARG_PARAM1 = "question";
    private static final String ARG_PARAM2 = "listener";
    private static final String ARG_PARAM3 = "map";
    private static final String ARG_PARAM5 = "qNo";

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
    View match_these_layout;

    public static boolean isMatchTheseSubmitted = false;
    private String maxLengthText;

    public MatchTheseQuestionFragment() {
    }

    public static MatchTheseQuestionFragment newInstance(int fragNumber, TestSeriesQuestionDto dto,
                                                         TestSeriesQuestionDto testSeriesQuestionDtoAnalytics,
                                                         Map<String, Integer> map) {
        MatchTheseQuestionFragment fragment = new MatchTheseQuestionFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, dto);
        args.putSerializable(ARG_PARAM2, testSeriesQuestionDtoAnalytics);
        args.putSerializable(ARG_PARAM3, (Serializable) map);
        args.putInt(ARG_PARAM5, fragNumber);
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
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dto = (TestSeriesQuestionDto) getArguments().getSerializable(ARG_PARAM1);
            testSeriesQuestionDtoAnalytics = (TestSeriesQuestionDto) getArguments().getSerializable(ARG_PARAM2);
            map = (Map<String, Integer>) getArguments().getSerializable(ARG_PARAM3);
            fragNumber = getArguments().getInt(ARG_PARAM5);
        }
    }


    @SneakyThrows
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_mock_test_question, container, false);

        Type = MyApplication.getStringData(PreferenceKeys.RESPONSE.getKey());

        markLayout = v.findViewById(R.id.mark);
        mark = v.findViewById(R.id.markText);
        ansText = v.findViewById(R.id.CorrectAnswer);
        clearAnswer = v.findViewById(R.id.button_clear_answer);
        yourAttempt = v.findViewById(R.id.yourAttempt);
        celebration = v.findViewById(R.id.celebration);
        responsetext = v.findViewById(R.id.YourResponse);
        parentLayout = v.findViewById(R.id.ParentRelativeLayout);
        subText = v.findViewById(R.id.subText);
        markLinearlayout = v.findViewById(R.id.markLinearLayout);
        markButton = v.findViewById(R.id.mark_button2);
        explanation = v.findViewById(R.id.explanation);
        correctPercentage = v.findViewById(R.id.Correctionpercentage);
        MarkLayout = v.findViewById(R.id.ParentRelativeLayout);
        questionType = v.findViewById(R.id.questionType);
        saveAnswer = v.findViewById(R.id.button_answer_save);
        saveAnswerText = v.findViewById(R.id.btn_text);
        subjectiveAnswerInputlayout = v.findViewById(R.id.integerAnswerrr);
        numericalAnswerInputlayout = v.findViewById(R.id.NumericalAnswerrr);
        subjectiveAnswer = v.findViewById(R.id.integerAnswer);
        numericalAnswer = v.findViewById(R.id.NumericalAnswer);
        relativeLayoutQuestion = v.findViewById(R.id.relative_layout_ques);

        if (dto == null) {
            Log.d("dtoObject", "Dto is null");
            dto = optionListener.getDto(fragNumber - 1);
        }

        maped = map;

        question = v.findViewById(R.id.question);
        questionImage = v.findViewById(R.id.question_image);
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
        RecyclerView recyclerView = v.findViewById(R.id.recyclerView);

        // Code Fore SubText---e
        if (dto != null && StringUtils.isNotEmpty(dto.getOptionalTag())) {
            subText.setVisibility(View.VISIBLE);
            subText.setText(dto.getSubtext());

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

            subjectiveAnswerInputlayout.setVisibility(View.GONE);
            numericalAnswerInputlayout.setVisibility(View.GONE);
        }

        if (dto.getQuestionTypeEnum() == QuestionType.NUMERICAL || dto.getQuestionTypeEnum() == QuestionType.SUBJECTIVE) {
            recyclerView.setVisibility(View.GONE);
            if (dto.getQuestionTypeEnum() == QuestionType.SUBJECTIVE) {
                subjectiveAnswerInputlayout.setVisibility(View.VISIBLE);
            } else
                numericalAnswerInputlayout.setVisibility(View.VISIBLE);
            if (Type.equals(PreferenceKeys.RESPONSE.getKey())) {
                clearAnswer.setVisibility(View.GONE);
                subjectiveAnswerInputlayout.setVisibility(View.GONE);
                numericalAnswerInputlayout.setVisibility(View.GONE);
                responsetext.setVisibility(View.VISIBLE);
                ansText.setVisibility(View.VISIBLE);
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
                            if (s.length() == 0)
                                s = "0";
                            int num = Integer.parseInt(s);
                            double numD = CommonMethods.parseDouble(dto.getUserAnswer());
                            double numI = (double) num;
                            if (numD == numI)
                                responsetext.setText("Your Response : " + num);
                            else
                                responsetext.setText("Your Response : " + dto.getUserAnswer());
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
                            if (s.length() == 0)
                                s = "0";
                            int num = Integer.parseInt(s);
                            double numD = CommonMethods.parseDouble(dto.getUserAnswer());
                            double numI = (double) num;
                            if (numD == numI)
                                responsetext.setText("Your Response : " + num);
                            else
                                responsetext.setText("Your Response : " + dto.getUserAnswer());
                        } else {
                            responsetext.setText(getResources().getString(R.string.no_attempt));
                        }
                    }
                } else
                    responsetext.setText(getResources().getString(R.string.no_response));
                ansText.setText(getResources().getString(R.string.correct_answer_1)+ " " + dto.getAnswer());
                if (dto.getQuestionTypeEnum() == QuestionType.NUMERICAL) {
                    double s = CommonMethods.parseDouble(dto.getAnswer());
                    int ss = (int) s;
                    double scheck = (double) ss;
                    if (scheck == s) {
                        ansText.setText(getResources().getString(R.string.correct_answer_1)+" "+ ss);
                    } else {
                        ansText.setText(getResources().getString(R.string.correct_answer_1)+" "+ dto.getAnswer());
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

            subjectiveAnswerInputlayout.setVisibility(View.GONE);
            numericalAnswerInputlayout.setVisibility(View.GONE);

        }


        if (dto.getQuestionTypeEnum() == QuestionType.MULTI_CHOICE || dto.getQuestionTypeEnum() == QuestionType.SINGLE_CHOICE_IMAGE
                || dto.getQuestionTypeEnum() == QuestionType.MULTI_CHOICE_IMAGE || dto.getQuestionTypeEnum() == QuestionType.SINGLE_CHOICE) {
            if (Type.equals(PreferenceKeys.RESPONSE.getKey())) {
                yourAttempt.setVisibility(View.VISIBLE);
                if (testSeriesQuestionDtoAnalytics != null || StringUtils.isNotEmpty(dto.getUserAnswer())) {
                    String userAnswer = testSeriesQuestionDtoAnalytics == null ? dto.getUserAnswer() : testSeriesQuestionDtoAnalytics.getUserAnswer();
                    if ((testSeriesQuestionDtoAnalytics != null && testSeriesQuestionDtoAnalytics.isCorrect()) || dto.isCorrect()) {
                        if (dto.getQuestionTypeEnum() != QuestionType.SINGLE_CHOICE_IMAGE)
                            yourAttempt.setText("Your Response : " + userAnswer);
                        else {
                            double userResponse = CommonMethods.parseDouble(userAnswer);
                            int userResponseInt = (int) userResponse - 1;
                            char optionSelected = (char) (userResponseInt + 'A');
                            yourAttempt.setText("Your Response : Option " + optionSelected);
                        }
                    } else {
                        if (userAnswer != null) {
                            celebration.setVisibility(View.GONE);
                            yourAttempt.setTextColor(Color.RED);
                            correctPercentage.setTextColor(Color.RED);
                            if (dto.getQuestionTypeEnum() != QuestionType.SINGLE_CHOICE_IMAGE)
                                yourAttempt.setText("Your Response : " + userAnswer);
                            else {
                                double userResponse = CommonMethods.parseDouble(userAnswer);
                                int userResponseInt = (int) userResponse - 1;
                                char optionSelected = (char) (userResponseInt + 'A');
                                yourAttempt.setText("Your Response : Option " + optionSelected);
                            }
                        } else {
                            celebration.setVisibility(View.GONE);
                            yourAttempt.setTextColor(Color.BLACK);
                            correctPercentage.setTextColor(Color.BLACK);
                            yourAttempt.setText(getResources().getString(R.string.no_response));
                        }
                    }
                } else {
                    celebration.setVisibility(View.GONE);
                    yourAttempt.setTextColor(Color.BLACK);
                    correctPercentage.setTextColor(Color.BLACK);
                    yourAttempt.setText(getResources().getString(R.string.no_response));
                }
            }
        }

        //Aditya Todo for SUBJECTIVE AND MATCHING

        if (dto.getQuestionTypeEnum() == QuestionType.MATCH_THE_COLUMN) {

            saveAnswerText.setText("Submit");

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
        }

        if (dto.getQuestionType().equals("SINGLE_CHOICE") || dto.getQuestionType().equals("SINGLE_CHOICE_IMAGE")) {
            questionType.setText(getResources().getString(R.string.single_choice));
        } else if (dto.getQuestionType().equals("MULTI_CHOICE") || dto.getQuestionType().equals("MULTI_CHOICE_IMAGE")) {
            questionType.setText("Multi-Choice ");
        } else if (dto.getQuestionType().equals("SUBJECTIVE")) {
            questionType.setText("Subjective ");
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


        saveAnswer.setOnClickListener(view -> {
            hideKeybaord(v);

            if(dto.getQuestionTypeEnum()==QuestionType.MATCH_THE_COLUMN){
                isMatchTheseSubmitted=true;
                setUserMatchTheseAnswer();
            }
            if (dto.getQuestionTypeEnum() == QuestionType.SUBJECTIVE) {

                if (StringUtils.isEmpty(subjectiveAnswer.getText())) {
                    CommonMethods.showNegativeSnackbar(MarkLayout, getString(R.string.please_enter_your_answer));
                    subjectiveAnswerInputlayout.setError("");
                } else {
                    if (StringUtils.isEmpty(dto.getOptionalTag()) || dto.getUserAnswer() != null) {
                        dto.setUserAnswer(subjectiveAnswer.getText().toString());
                        update();
                        CommonMethods.showPositiveSnackbar(MarkLayout, getString(R.string.saved));
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
                            CommonMethods.showPositiveSnackbar(MarkLayout, getString(R.string.saved));
                            update();
                        }
                    }
                }
            } else if (dto.getQuestionTypeEnum() == QuestionType.NUMERICAL) {
                if (StringUtils.isEmpty(numericalAnswer.getText())) {

                    CommonMethods.showNegativeSnackbar(MarkLayout, getString(R.string.please_enter_your_answer));
                    numericalAnswerInputlayout.setError("");
                } else {
                    if (StringUtils.isEmpty(dto.getOptionalTag()) || dto.getUserAnswer() != null) {
                        dto.setUserAnswer(numericalAnswer.getText().toString());
                        update();
                        CommonMethods.showPositiveSnackbar(MarkLayout, getString(R.string.saved));

                    } else {
                        Integer value = maped.get(dto.getOptionalTag());
                        if (value == null) {
                            value = -1;
                        }
                        if (value == 0) {
                            CommonMethods.showNegativeSnackbar(recyclerView, getString(R.string.optional_error));
                        } else {
                            maped.put(dto.getOptionalTag(), value - 1);
                            dto.setUserAnswer(numericalAnswer.getText().toString());
                            CommonMethods.showPositiveSnackbar(MarkLayout, getString(R.string.saved));
                            update();

                        }

                    }
                }
            } else if (dto.getQuestionTypeEnum() == QuestionType.SINGLE_CHOICE_IMAGE || dto.getQuestionTypeEnum() == QuestionType.MULTI_CHOICE_IMAGE) {
                if (!ifAnyOptionsSelected(optionDtos)) {
                    CommonMethods.showNegativeSnackbar(markLayout, getString(R.string.please_select_your_option));
                } else {
                    if (StringUtils.isEmpty(dto.getOptionalTag()) || dto.getUserAnswer() != null) {
                        dto.setUserAnswer(createAnswer(optionDtos, dto.getQuestionTypeEnum()));
                        update();
                        CommonMethods.showPositiveSnackbar(MarkLayout, getString(R.string.saved));

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
                            CommonMethods.showPositiveSnackbar(MarkLayout, getString(R.string.saved));
                            update();

                        }
                    }

                }
            } else if ((dto.getQuestionTypeEnum() == QuestionType.SINGLE_CHOICE || dto.getQuestionTypeEnum() == QuestionType.MULTI_CHOICE)) {
                if (!ifAnyOptionsSelected(optionDtos)) {
                    CommonMethods.showNegativeSnackbar(markLayout, getString(R.string.please_select_your_option));
                } else {
                    if (StringUtils.isEmpty(dto.getOptionalTag()) || dto.getUserAnswer() != null) {
                        dto.setUserAnswer(createAnswer(optionDtos, dto.getQuestionTypeEnum()));
                        update();
                        CommonMethods.showPositiveSnackbar(MarkLayout, getString(R.string.saved));
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
                            CommonMethods.showPositiveSnackbar(MarkLayout, getString(R.string.saved));
                            update();
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

        markLinearlayout.setOnClickListener(v1 -> {
            dto.setMarked(!dto.isMarked());
            changeMark();
        });


        if (Type.equals(PreferenceKeys.RESPONSE.getKey())) {
            //celebration.setVisibility(View.VISIBLE);
            saveAnswer.setVisibility(View.GONE);
            clearAnswer.setVisibility(View.GONE);
            markLinearlayout.setVisibility(View.GONE);
            if (dto.getExplanation() != null) {
                if (!dto.getExplanation().equalsIgnoreCase("NA")) {
                    explanation.setVisibility(View.VISIBLE);
                }
            }
        }
        dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.custom_dialogu_explanation);
        dialog.getWindow().setBackgroundDrawable(getActivity().getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);

        explanation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });

        TextView exp = dialog.findViewById(R.id.explanation);
        CardView cancel = dialog.findViewById(R.id.cancel);
        ImageView image = dialog.findViewById(R.id.image_explanation);
        CardView revise = dialog.findViewById(R.id.revise_concept);
        ImageView copy = dialog.findViewById(R.id.copy);

        image.setVisibility(View.GONE);
        if (StringUtils.isNotEmpty(dto.getExplanationImage())
                && dto.getExplanationImage().startsWith("http")) {
            Uri uri = Uri.parse(dto.getExplanationImage());
            Glide.with(getActivity()).load(uri).into(image);
            image.setVisibility(View.VISIBLE);
        }
        if (dto.getVideoUrl() == null)
            revise.setVisibility(View.GONE);

        exp.setText(dto.getExplanation());
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", dto.getExplanation());
                clipboard.setPrimaryClip(clip);
                CommonMethods.showPositiveSnackbar(parentLayout, getString(R.string.ExplanationCopied) + "!");
            }
        });
        revise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ExoPlayerActivity.class);
                intent.putExtra("video_uri", dto.getVideoUrl());
                dto.setVideoEndingTime(0);
                dto.setVideoStartingTime(0);
                intent.putExtra(PreferenceKeys.VIDEO_STARTING.getKey(), dto.getVideoStartingTime());
                intent.putExtra("video_ending", dto.getVideoEndingTime());
                startActivity(intent);
            }
        });

        return v;

    }

    private void setDefaultMatchTheseAnswer(){
        userAnswer = "A-1";
        for(int i=1;i<list_left.size();i++){
            userAnswer = ","+userAnswer+(char)(65+i)+"-"+i;
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
            if(correctAnswerList.get(i).equals(userAnswerOption)) {
                list_right.get(i).setCorrectlyMatched(true);
                list_left.get(i).setCorrectlyMatched(true);
            }
            userAnswerList.add(userAnswerOption);
            userAnswer = userAnswerOption + userAnswer;
        }
          matchTheseAdapterRight.notifyDataSetChanged();
          matchTheseAdapterLeft.notifyDataSetChanged();
    }

    void findMaxString(ArrayList<MatchTheseLeftOption> left , ArrayList<MatchTheseRightOption> right){
        String max = "";
        for(int i=0;i<left.size();i++){
            if(max.length()< left.get(i).getValue().length()){
                max=left.get(i).getValue();
            }
            if(max.length()< right.get(i).getValue().length()){
                max=right.get(i).getValue();
            }
        }
        list_left.add(0,new MatchTheseLeftOption('A',max,false));
    }
    void setLeftList(List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            char c =(char) (65+i);
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

    public void updateView() {
        try {
            if (StringUtils.isNotEmpty(dto.getUserAnswer())) {
                if (!Type.equals(PreferenceKeys.RESPONSE.getKey()))
                    clearAnswer.setVisibility(View.VISIBLE);
                if (dto.getQuestionTypeEnum() == QuestionType.NUMERICAL || dto.getQuestionTypeEnum() == QuestionType.SUBJECTIVE) {
                    //integerAnswer.setText(dto.getUserAnswer());
                    //integerAnswer.setSelection(dto.getUserAnswer().length());
                    update();
                } else if (dto.getQuestionTypeEnum() == QuestionType.SINGLE_CHOICE) {
                    for (OptionDto optionDto : optionDtos) {
                        if (optionDto.getOptionText().equalsIgnoreCase(dto.getUserAnswer())) {
                            optionDto.setSelected(true);
                            update();
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
                                update();
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
                        update();
                    }
                    for (OptionDto optionDto : optionDtos) {
                        if (userAnswers.contains(optionDto.getOptionText())) {
                            optionDto.setSelected(true);
                            update();
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void update() {
        saveAnswerText.setText(getString(R.string.update));
        dto.setAnswerState(AnswerState.ANSWERED);
        if (!Type.equals(PreferenceKeys.RESPONSE.getKey()))
            clearAnswer.setVisibility(View.VISIBLE);
        changeAnswerState(dto.isMarked());
        optionListener.setSelected(0);
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

        saveAnswerText.setText(getString(R.string.save));
        dto.setAnswerState(AnswerState.UNANSWERED);
        clearAnswer.setVisibility(View.GONE);
        changeAnswerState(dto.isMarked());
        optionListener.setSelected(-1);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
        setViewPagerDataForCrashlytics();
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