package arivihan.technologies.doubtbuzzter2.fragments;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.FIB_PROBABLE_ANSWER_VALIDATE;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.common.FullScreenImageDialog;
import arivihan.technologies.doubtbuzzter2.constants.QuestionLevel;
import arivihan.technologies.doubtbuzzter2.enums.FullViewImageType;
import arivihan.technologies.doubtbuzzter2.helper.ImageLoaderUtil;
import arivihan.technologies.doubtbuzzter2.helper.MyEmojiFilter;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionType;
import arivihan.technologies.doubtbuzzter2.listener.SubmitListener;
import arivihan.technologies.doubtbuzzter2.model.FactoryShipQnADto;
import arivihan.technologies.doubtbuzzter2.model.ShortAnswerResult;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class ValueTypeFragment extends Fragment {

    private static final String TAG = ValueTypeFragment.class.getSimpleName();
    private MaterialCardView cv_user_answer;
    CardView submit;
    TextView submitText;
    String userAnswer;
    FactoryShipQnADto factoryShipQnADto;
    String number;
    String total;
    SubmitListener submitListener;
    TextView questionNumber;
    TextView tagText;
    CardView tagCard;
    TextView questionText;
    ImageView questionImage;
    View solutionView;
    TextView solutionText;
    ImageLoaderUtil solutionImage;
    CardView difficultyCard;
    TextView difficultyLevel;
    ImageView reportQuestion;
    CardView viewSolutionCard;
    ScrollView scrollView;
    ConstraintLayout askDoubt;
    EditText userAnswerText;
    TextView answerStatusText;
    ViewTreeObserver observer;
    boolean dataLoaded;
    LinearLayout correctAnswerLayout;
    TextView correctAnswer;
    Handler handler;
    FullScreenImageDialog fullScreenImageDialog;
    private String subjectCode;

    public ValueTypeFragment() {
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("factoryShipQnADto", factoryShipQnADto);
        outState.putString(PreferenceKeys.PHONE_NUMBER.getKey(), number);
        outState.putString("total", total);
        outState.putString("subjectCode", subjectCode); // ✅ added
    }

    public static ValueTypeFragment newInstance(FactoryShipQnADto factoryShipQnADto, String number, String total, String subjectCode) { // ✅ added param
        ValueTypeFragment fragment = new ValueTypeFragment();
        Bundle args = new Bundle();
        args.putParcelable("factoryShipQnADto", factoryShipQnADto);
        args.putString(PreferenceKeys.PHONE_NUMBER.getKey(), number);
        args.putString("total", total);
        args.putString("subjectCode", subjectCode); // ✅ added
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
            subjectCode = getArguments().getString("subjectCode"); // ✅ added
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_value_type, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(factoryShipQnADto!=null)
            factoryShipQnADto.setWordLimit(updateWordLimit(factoryShipQnADto));
        setUpViews(view);
        setUpQuestionDetail();
        handleAttempt(factoryShipQnADto.isUserAnswerCorrect());
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpAnswerDetail();
    }

    private void setUpViews(View view) {
        answerStatusText = view.findViewById(R.id.answer_status_text);
        userAnswerText = view.findViewById(R.id.user_answer);
        userAnswerText.setFilters(MyEmojiFilter.getFilter());
        askDoubt = view.findViewById(R.id.ask_doubt_fab);
        correctAnswerLayout = view.findViewById(R.id.correct_answer_layout);
        correctAnswer = view.findViewById(R.id.correct_answer);
        askDoubt.setOnClickListener(v -> {
            try {
                String chatSessionId = CommonMethods.stringToMd5(
                        "mtq_" + factoryShipQnADto.getQuestionId()
                                + "_" + MyApplication.getStringData(PreferenceKeys.USER_ID.getKey())
                                + "_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey())
                );

                String obtainedMarks = factoryShipQnADto.isUserAnswerCorrect()
                        ? factoryShipQnADto.getMarks()
                        : "0";

                CommonNavigator.navigateToDoubtChatFromCQNQ(
                        getActivity(),
                        chatSessionId,
                        subjectCode,                              // SubjectCode
                        factoryShipQnADto.getQuestion(),          // question
                        factoryShipQnADto.getUserAnswer(),        // studentAnswer
                        factoryShipQnADto.getAnswer(),            // idealAnswer
                        factoryShipQnADto.getAnalysis(),          // analysisOfAnswer
                        factoryShipQnADto.getAnswerEx(),
                        obtainedMarks,             // obtainedMarks (computed)
                        factoryShipQnADto.getMarks(),
                        factoryShipQnADto.getType()  ,
                        factoryShipQnADto.getImageExplanationLatex(),
                        factoryShipQnADto.getImageQuestion(),
                        factoryShipQnADto.getAnswerSolImage(),
                        factoryShipQnADto.getChapterCode(),
                        factoryShipQnADto.getCode()
                );
            } catch (Exception e) {
                Log.i("ARIVIHAN_EXCEPTION", "Unable to start activity WebviewDoubtChatScreen.class");
            }
        });
        scrollView = view.findViewById(R.id.scroll_view);
        submitText = view.findViewById(R.id.submit_text);
        cv_user_answer = view.findViewById(R.id.cv_user_answer);
        submit = view.findViewById(R.id.submit_card);
        submit.setOnClickListener(v -> {
            if (MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                userAnswer = userAnswerText.getText().toString().trim();
                factoryShipQnADto.setUserAnswer(userAnswer);
                if (userAnswerText.isEnabled()) {
                    validateShortAnswer(factoryShipQnADto);
                } else if (!factoryShipQnADto.isReattempt() && factoryShipQnADto.isCompleted()) {
                    submitListener.openNext();
                }
            } else {
                CommonMethods.showNegativeSnackbar(submit, getString(R.string.NoInternetConnection));
            }
        });
        if (factoryShipQnADto.isCompleted() && !factoryShipQnADto.isReattempt())
            submitText.setText(R.string.next);
        questionNumber = view.findViewById(R.id.question_number);
        tagText = view.findViewById(R.id.tag_text);
        tagCard = view.findViewById(R.id.tag_card);
        questionText = view.findViewById(R.id.question_text);
        questionImage = view.findViewById(R.id.image_question);
        solutionView = view.findViewById(R.id.solution_view);
        solutionText = view.findViewById(R.id.solution_text);
        solutionImage = view.findViewById(R.id.solution_image);
        difficultyCard = view.findViewById(R.id.difficulty_card);
        difficultyLevel = view.findViewById(R.id.difficulty_type);
        reportQuestion = view.findViewById(R.id.report_question);
        reportQuestion.setOnClickListener(v -> submitListener.reportQuestion());
        viewSolutionCard = view.findViewById(R.id.view_solution_card);

        if(StringUtils.isNotEmpty(factoryShipQnADto.getYear())) {
            tagCard.setVisibility(View.VISIBLE);
            tagText.setText(factoryShipQnADto.getYear());
        } else {
            tagCard.setVisibility(View.GONE);
        }

        userAnswerText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (userAnswerText.isEnabled()) {
                    userAnswer = s.toString().trim();
                    handleWordCount(userAnswer.trim().split("\\s+").length);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        viewSolutionCard.setOnClickListener(v -> {
            factoryShipQnADto.setHideViewSolution(true);
            submitListener.onViewSolutionClick();
            viewSolutionCard.setVisibility(View.GONE);
            scrollView.fullScroll(View.FOCUS_DOWN);
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                setVisibility();
            });
        }
        observer = scrollView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(() -> {
            int viewHeight = scrollView.getMeasuredHeight();
            int contentHeight = scrollView.getChildAt(0).getHeight();
            if (viewHeight - contentHeight < 0 && !dataLoaded) {
                setVisibility();
                dataLoaded = true;
            }
        });
    }

    private String updateWordLimit(FactoryShipQnADto dto) {
        long wordLimit = MyApplication.getFirebaseRemoteConfig().getLong((StringUtils.isNotEmpty(dto.getType())?dto.getType():"default_one") + "_word_limit");
        return StringUtils.isNotEmpty(dto.getWordLimit()) ? dto.getWordLimit() : (wordLimit != 0 ? String.valueOf(wordLimit) : String.valueOf(10));
    }

    private void submitShortAnswer(boolean correct) {
        try {

            if (submitListener == null && getActivity() != null) {
                // Try to re-attach by casting to the activity again
                try {
                    submitListener = (SubmitListener) getActivity();
                } catch (Exception e) {
                    Log.e("SubmitShortAnswer", "Activity must implement SubmitListener");
                }
            }

            if (submitListener == null && getContext() != null) {
                Toast.makeText(getContext(), getString(R.string.please_try_again_later), Toast.LENGTH_SHORT).show();
            }

            submitListener.onSubmit(Integer.parseInt(number) - 1, factoryShipQnADto, correct);
            if (StringUtils.isNotEmpty(userAnswer)) {
                factoryShipQnADto.setCompleted(true);
                factoryShipQnADto.setReattempt(false);
                submit(correct);
                handler = new Handler();
                Runnable runnable = () -> {
                    handler.removeCallbacksAndMessages(null);
                    setUpAnswerDetail();
                };
                handler.postDelayed(runnable, 3000);
                submitText.setText(R.string.next);
            }
        } catch (Exception e) {

        }
    }

    private void handleWordCount(int count) {
        try {
            int wordLimit = Integer.parseInt(factoryShipQnADto.getWordLimit());

            if (count > (wordLimit) && factoryShipQnADto.isWordLimitApplicable()) {
                answerStatusText.setText(getString(R.string.word_limit_text));
                answerStatusText.setTextColor(ContextCompat.getColor(requireContext(), R.color.red));
                cv_user_answer.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.red));
                submit.setEnabled(false);
            } else {
                answerStatusText.setText(getString(R.string.type_your_answer));
                answerStatusText.setTextColor(ContextCompat.getColor(requireContext(), R.color.mock_dark_grey));
                cv_user_answer.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.text_card_stroke));
                submit.setEnabled(true);
            }

        } catch (Exception e) {
            Log.e("wordLimit", e.getMessage(), e);
        }
    }

    public void setVisibility() {
        if (StringUtils.isNotEmpty(factoryShipQnADto.getAnswerEx()) ||
                StringUtils.isNotEmpty(factoryShipQnADto.getAnswerSolImage())) {
            if (factoryShipQnADto.isCompleted()
                    && !factoryShipQnADto.isReattempt()
                    && !factoryShipQnADto.isHideViewSolution()
                    && scrollView.canScrollVertically(1)) {
                viewSolutionCard.setVisibility(View.VISIBLE);
            } else {
                viewSolutionCard.setVisibility(View.GONE);
            }
        }
    }

    private void setUpQuestionDetail() {
        questionNumber.setText("Q" + number + " of " + total);
        if (StringUtils.isNotEmpty(factoryShipQnADto.getImageQuestion())) {
            questionImage.setVisibility(View.VISIBLE);
            Glide.with(getActivity()).load(factoryShipQnADto.getImageQuestion()).into(questionImage);
            questionImage.setOnClickListener(view->{
                fullScreenImageDialog = new FullScreenImageDialog(requireContext(),factoryShipQnADto.getImageQuestion(),
                        FullViewImageType.QUESTION);
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

    private void setUpAnswerDetail() {
        try {
            if (factoryShipQnADto.isCompleted()
                    && !factoryShipQnADto.isReattempt()
                    && StringUtils.isNotEmpty(factoryShipQnADto.getUserAnswer())) {

                if (StringUtils.isNotEmpty(factoryShipQnADto.getAnswer())) {
                    correctAnswerLayout.setVisibility(View.VISIBLE);
                    correctAnswer.setText(factoryShipQnADto.getAnswer());
                }
                if (StringUtils.isNotEmpty(factoryShipQnADto.getAnswerEx()) || StringUtils.isNotEmpty(factoryShipQnADto.getAnswerSolImage())) {
                    solutionView.setVisibility(View.VISIBLE);
//                    askDoubt.setVisibility(View.VISIBLE);
                    askDoubt.setVisibility((!MyApplication.isCompetitiveProfile() && !SubscriptionType.hasActiveSubscription()) ? View.GONE : View.VISIBLE);
                    if (StringUtils.isNotEmpty(factoryShipQnADto.getQuestionLevel())) {
                        difficultyCard.setVisibility(View.VISIBLE);
                        if (getContext() != null) {
                            if (factoryShipQnADto.getQuestionLevel().equals(QuestionLevel.EASY.name())) {
                                difficultyLevel.setText(getContext().getString(R.string.easy));
                            } else if (factoryShipQnADto.getQuestionLevel().equals(QuestionLevel.MEDIUM.name())) {
                                difficultyLevel.setText(getContext().getString(R.string.medium));
                            } else if (factoryShipQnADto.getQuestionLevel().equals(QuestionLevel.HARD.name())) {
                                difficultyLevel.setText(getContext().getString(R.string.hard));
                            }
                        }
                    }
                    if (StringUtils.isNotEmpty(factoryShipQnADto.getAnswerSolImage())) {
                        solutionImage.setVisibility(View.VISIBLE);
                        solutionImage.loadImage(factoryShipQnADto.getAnswerSolImage());
                        solutionImage.setOnClickListener(view->{
                            fullScreenImageDialog = new FullScreenImageDialog(requireContext()
                                    ,factoryShipQnADto.getAnswerSolImage(),FullViewImageType.SOLUTION);
                            fullScreenImageDialog.show();
                        });
                    } else {
                        solutionImage.setVisibility(View.GONE);
                    }

                    if (StringUtils.isNotEmpty(factoryShipQnADto.getAnswerEx())) {
                        solutionText.setVisibility(View.VISIBLE);
                        solutionText.setText(HtmlCompat.fromHtml(factoryShipQnADto.getAnswerEx().trim(), HtmlCompat.FROM_HTML_MODE_LEGACY));
                    } else {
                        solutionText.setVisibility(View.GONE);
                    }
                } else {
                    viewSolutionCard.setVisibility(View.GONE);
                }
                dataLoaded = false;
            }
        }catch (Exception e){
            Log.d( "setUpAnswerDetail: ", e.getMessage());
        }
    }

    private void handleAttempt(boolean correct) {
        if (factoryShipQnADto.isCompleted() && StringUtils.isNotEmpty(factoryShipQnADto.getUserAnswer())) {
            if (!factoryShipQnADto.isReattempt() || factoryShipQnADto.isReview()) {
                userAnswer = factoryShipQnADto.getUserAnswer();
                submit(correct);
            }
        }
    }

    private void submit(boolean correct) {
        userAnswerText.setEnabled(false);
        answerStatusText.setText(R.string.your_submitted_answer);
        userAnswerText.setText(factoryShipQnADto.getUserAnswer());
        if (correct) {
            userAnswerText.setBackground(getResources().getDrawable(R.drawable.question_option_border_green));
        } else {
            userAnswerText.setBackground(getResources().getDrawable(R.drawable.question_option_border_red));
        }
    }

    private void validateShortAnswer(FactoryShipQnADto qnADto) {
        Map<String, Object> map = new HashMap<>();
        map.put("microLectureQId", qnADto.getQuestionId());
        map.put("userAnswer", qnADto.getUserAnswer());
        map.put("qAns", qnADto.getAnswer());
        Gson gson = new Gson();
        MyJsonObjectRequest submitReport = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(FIB_PROBABLE_ANSWER_VALIDATE, map),
                null,
                response -> {
                    TypeToken<ShortAnswerResult> token = new TypeToken<>() {
                    };
                    ShortAnswerResult shortAnswerResult = gson.fromJson(response.toString(), token.getType());
                    factoryShipQnADto.setUserAnswerCorrect(shortAnswerResult.isCorrect());
                    submitShortAnswer(shortAnswerResult.isCorrect());

                },
                new VolleyGlobalErrorListener(getContext()) {
                    @Override
                    public void postError(VolleyError error) {
                        Log.i("REPORTRESPONSE", error.toString());
                        boolean correct = factoryShipQnADto.getUserAnswer().equalsIgnoreCase(factoryShipQnADto.getAnswer().trim());
                        submitShortAnswer(correct);
                    }
                });

        MyVolleySingleton.getInstance(getContext()).addToRequestQueue(submitReport);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}