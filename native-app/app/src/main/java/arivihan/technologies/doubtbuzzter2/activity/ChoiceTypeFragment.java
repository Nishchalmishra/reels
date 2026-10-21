package arivihan.technologies.doubtbuzzter2.activity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.ChoiceTypeAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.common.FullScreenImageDialog;
import arivihan.technologies.doubtbuzzter2.constants.QuestionLevel;
import arivihan.technologies.doubtbuzzter2.enums.FullViewImageType;
import arivihan.technologies.doubtbuzzter2.helper.ImageLoaderUtil;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionType;
import arivihan.technologies.doubtbuzzter2.listener.OptionListener;
import arivihan.technologies.doubtbuzzter2.listener.SubmitListener;
import arivihan.technologies.doubtbuzzter2.model.FactoryShipQnADto;
import arivihan.technologies.doubtbuzzter2.model.TestSeriesQuestionDto;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class ChoiceTypeFragment extends Fragment implements OptionListener {

    RecyclerView recyclerView;
    ChoiceTypeAdapter choiceTypeAdapter;
    CardView submit;
    TextView submitText;
    int userAnsOption;
    String userAnswer;
    Handler handler;
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
    boolean optionSelected;
    ConstraintLayout askDoubt;
    boolean dataLoaded;
    ViewTreeObserver observer;
    TextView description;
    FullScreenImageDialog fullScreenImageDialog;
     private String subjectCode;

    public ChoiceTypeFragment() {
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
        outState.putString("subjectCode", subjectCode);

    }

    public static ChoiceTypeFragment newInstance(FactoryShipQnADto factoryShipQnADto, String number, String total,String subjectCode) {
        ChoiceTypeFragment fragment = new ChoiceTypeFragment();
        Bundle args = new Bundle();
        args.putParcelable("factoryShipQnADto", factoryShipQnADto);
        args.putString(PreferenceKeys.PHONE_NUMBER.getKey(), number);
        args.putString("total", total);
        args.putString("subjectCode",subjectCode);
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
        View view = inflater.inflate(R.layout.fragment_choice_type, container, false);
        setUpViews(view);
        setUpQuestionDetail();
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        choiceTypeAdapter = new ChoiceTypeAdapter(getActivity(), factoryShipQnADto.getOptions(), factoryShipQnADto, this);
        recyclerView.setAdapter(choiceTypeAdapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpAnswerDetail();
    }

    private void setUpViews(View view) {
        description = view.findViewById(R.id.description);
        askDoubt = view.findViewById(R.id.ask_doubt_fab);
        askDoubt.setOnClickListener(v -> {
            try {
                String chatSessionId = CommonMethods.stringToMd5(
                        "mtq_" + factoryShipQnADto.getQuestionId()
                                + "_" + MyApplication.getStringData(PreferenceKeys.USER_ID.getKey())
                                + "_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey())
                );
                String dtoAnswer = factoryShipQnADto.getAnswer();
                if (dtoAnswer != null)
                    dtoAnswer = dtoAnswer.trim();
                String obtainedMarks = factoryShipQnADto.getUserAnswer().equalsIgnoreCase(dtoAnswer)
                        ? factoryShipQnADto.getMarks()
                        : "0";

                CommonNavigator.navigateToDoubtChatFromCQNQ(
                        getActivity(),
                        chatSessionId,
                        subjectCode,
                        factoryShipQnADto.getQuestion(),
                        factoryShipQnADto.getUserAnswer(),
                        factoryShipQnADto.getAnswer(),
                        factoryShipQnADto.getAnalysis(),
                        factoryShipQnADto.getAnswerEx(),
                       obtainedMarks,
                        factoryShipQnADto.getMarks(),
                        factoryShipQnADto.getType(),
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
        submit = view.findViewById(R.id.submit_card);
        submit.setOnClickListener(v -> {
            if (MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                submit();
            } else {
                CommonMethods.showNegativeSnackbar(submit, getString(R.string.NoInternetConnection));
            }
        });
        if (factoryShipQnADto.isCompleted() && !factoryShipQnADto.isReattempt()) {
            submitText.setText(R.string.next);
            description.setText(getString(R.string.your_submitted_answer));
        }
        recyclerView = view.findViewById(R.id.choice_rv);
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
        viewSolutionCard.setOnClickListener(v -> {
            factoryShipQnADto.setHideViewSolution(true);
            submitListener.onViewSolutionClick();
            viewSolutionCard.setVisibility(View.GONE);
            scrollView.fullScroll(View.FOCUS_DOWN);
        });

        if (StringUtils.isNotEmpty(factoryShipQnADto.getYear())) {
            tagCard.setVisibility(View.VISIBLE);
            tagText.setText(factoryShipQnADto.getYear());
        } else {
            tagCard.setVisibility(View.GONE);
        }

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
        Log.d("numerical question called ", "setUpQuestionDetail numerical questuion alled : ");
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


    private void setUpAnswerDetail() {
        try {
            if (factoryShipQnADto.isCompleted()
                    && !factoryShipQnADto.isReattempt()
                    && (StringUtils.isNotEmpty(factoryShipQnADto.getAnswerEx()) || StringUtils.isNotEmpty(factoryShipQnADto.getAnswerSolImage()))
                    && StringUtils.isNotEmpty(factoryShipQnADto.getUserAnswer())) {
                Log.d("factoryShipAnswer", "EX: " + factoryShipQnADto.getAnswerEx() + " Sol: " + factoryShipQnADto.getAnswerSolImage());
                solutionView.setVisibility(View.VISIBLE);
                askDoubt.setVisibility((!MyApplication.isCompetitiveProfile() && !SubscriptionType.hasActiveSubscription()) ? View.GONE : View.VISIBLE);
                if (StringUtils.isNotEmpty(factoryShipQnADto.getQuestionLevel())) {
                    difficultyCard.setVisibility(View.VISIBLE);
                    if (factoryShipQnADto.getQuestionLevel().equals(QuestionLevel.EASY.name())) {
                        difficultyLevel.setText(getString(R.string.easy));
                    } else if (factoryShipQnADto.getQuestionLevel().equals(QuestionLevel.MEDIUM.name())) {
                        difficultyLevel.setText(getString(R.string.medium));
                    } else if (factoryShipQnADto.getQuestionLevel().equals(QuestionLevel.HARD.name())) {
                        difficultyLevel.setText(getString(R.string.hard));
                    }
                }
                if (StringUtils.isNotEmpty(factoryShipQnADto.getAnswerSolImage())) {
                    solutionImage.setVisibility(View.VISIBLE);
                    solutionImage.loadImage(factoryShipQnADto.getAnswerSolImage());
                    solutionImage.setOnClickListener(view -> {
                        fullScreenImageDialog = new FullScreenImageDialog(requireContext(), factoryShipQnADto.getAnswerSolImage(),
                                FullViewImageType.SOLUTION);
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
                dataLoaded = false;
            }
        } catch (Exception e) {
            Log.d("setUpAnswerDetail:", e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    private void submit() {
        if (factoryShipQnADto.isReattempt() || !factoryShipQnADto.isCompleted()) {
            if (optionSelected && StringUtils.isNotEmpty(factoryShipQnADto.getUserAnswer())) {
                String dtoAnswer = factoryShipQnADto.getAnswer();
                if (dtoAnswer != null)
                    dtoAnswer = dtoAnswer.trim();
                boolean correct = factoryShipQnADto.getUserAnswer().equalsIgnoreCase(dtoAnswer);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.ml_lec_ques_submit.getActionName());
                submitListener.onSubmit(Integer.parseInt(number) - 1, factoryShipQnADto, correct);
                factoryShipQnADto.setCompleted(true);
                factoryShipQnADto.setReattempt(false);
                handler = new Handler();
                Runnable runnable = () -> {
                    handler.removeCallbacksAndMessages(null);
                    setUpAnswerDetail();
                };
                handler.postDelayed(runnable, 3000);
                submitText.setText(R.string.next);
                description.setText(getString(R.string.your_submitted_answer));
                choiceTypeAdapter.notifyDataSetChanged();
            } else {
                factoryShipQnADto.setUserAnswer("");
                String dtoAnswer = factoryShipQnADto.getAnswer();
                if (dtoAnswer != null)
                    dtoAnswer = dtoAnswer.trim();
                boolean correct = factoryShipQnADto.getUserAnswer().equalsIgnoreCase(dtoAnswer);
                submitListener.onSubmit(Integer.parseInt(number) - 1, factoryShipQnADto, correct);
            }
        } else if (!factoryShipQnADto.isReattempt() && factoryShipQnADto.isCompleted()) {
            submitListener.openNext();
        }
    }

    @Override
    public void setSelected(int i) {
        optionSelected = true;
        userAnsOption = i;
        userAnswer = String.valueOf(i + 1);
        factoryShipQnADto.setUserAnswer(userAnswer);
        choiceTypeAdapter.notifyDataSetChanged();
    }

    @Override
    public void setSelected(int i, String answer) {
        OptionListener.super.setSelected(i, answer);
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
