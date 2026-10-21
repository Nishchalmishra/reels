package arivihan.technologies.doubtbuzzter2.fragments;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.MyItemTouchHelper;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.BoardQuestionActivity;
import arivihan.technologies.doubtbuzzter2.adapter.MatchTheseAdapterLeft;
import arivihan.technologies.doubtbuzzter2.adapter.MatchTheseAdapterRight;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.common.FullScreenImageDialog;
import arivihan.technologies.doubtbuzzter2.constants.QuestionLevel;
import arivihan.technologies.doubtbuzzter2.enums.FullViewImageType;
import arivihan.technologies.doubtbuzzter2.helper.ImageLoaderUtil;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionType;
import arivihan.technologies.doubtbuzzter2.listener.SubmitListener;
import arivihan.technologies.doubtbuzzter2.model.FactoryShipQnADto;
import arivihan.technologies.doubtbuzzter2.model.MatchTheseLeftOption;
import arivihan.technologies.doubtbuzzter2.model.MatchTheseRightOption;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class MatchTheseTypeFragment extends Fragment {
    CardView submit;
    TextView submitText;
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
    boolean dataLoaded;
    ViewTreeObserver observer;
    List<String> titleList = new ArrayList<>();
    boolean submitted;
    TextView description;
    MatchTheseAdapterLeft matchTheseAdapterLeft;
    MatchTheseAdapterRight matchTheseAdapterRight;
    ArrayList<MatchTheseLeftOption> leftOptions = new ArrayList<>();
    ArrayList<MatchTheseRightOption> rightOptions = new ArrayList<>();
    List<String> correctAnswerList;
    List<String> userAnswerList;
    String userFinalAnswer = "1-1,1-2,1-3,1-4";
    String userAnswerOption;
    MyItemTouchHelper myItemTouchHelper;
    LinearLayout tabView;
    LinearLayout myAnswer;
    LinearLayout correctAnswer;
    TextView myAnswerText;
    TextView correctAnswerText;
    LinearLayout myAnswerUnderline;
    LinearLayout correctAnswerUnderline;
    RecyclerView leftRecyclerView;
    RecyclerView rightRecyclerView;
    LinearLayout divider;
    private boolean showMatchTheseRightAnswer;
    private String maxLengthText = "";
    FullScreenImageDialog fullScreenImageDialog;
    private String subjectCode;

    public MatchTheseTypeFragment() {
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

    public static MatchTheseTypeFragment newInstance(FactoryShipQnADto factoryShipQnADto, String number, String total,String subjectCode) {
        MatchTheseTypeFragment fragment = new MatchTheseTypeFragment();
        Bundle args = new Bundle();
        args.putParcelable("factoryShipQnADto", factoryShipQnADto);
        Log.d("MatchTheseDto", "newInstance " + factoryShipQnADto.toString());
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
            Log.d("MatchTheseDto", "onCreate " + factoryShipQnADto);
            number = getArguments().getString(PreferenceKeys.PHONE_NUMBER.getKey());
            total = getArguments().getString("total");
            subjectCode=getArguments().getString("subjectCode");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_match_these_type, container, false);
        Log.d("MatchTheseDto", "onCreateView " + factoryShipQnADto.toString());

        setUpMatchTheseLayout(view);
        setUpViews(view);
        setUpQuestionDetail();
        if (getArguments() != null) {
            view.setTag("outer_fragment_" + (Integer.parseInt(number.trim()) - 1));
        }
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                if (factoryShipQnADto.isCompleted()
                        && !factoryShipQnADto.isReattempt()
                        && !factoryShipQnADto.isHideViewSolution()
                        && (StringUtils.isNotEmpty(factoryShipQnADto.getAnswerEx()) || StringUtils.isNotEmpty(factoryShipQnADto.getAnswerSolImage()))
                        && scrollView.canScrollVertically(1)) {
                    viewSolutionCard.setVisibility(View.VISIBLE);
                } else {
                    viewSolutionCard.setVisibility(View.GONE);
                }
            });
        }

        if(StringUtils.isNotEmpty(factoryShipQnADto.getYear())) {
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
                        !factoryShipQnADto.isHideViewSolution() &&
                        (StringUtils.isNotEmpty(factoryShipQnADto.getAnswerEx()) || StringUtils.isNotEmpty(factoryShipQnADto.getAnswerSolImage()))) {
                    viewSolutionCard.setVisibility(View.VISIBLE);
                } else {
                    viewSolutionCard.setVisibility(View.GONE);
                }
                dataLoaded = true;
            }
        });
        if (factoryShipQnADto.isReview() || (factoryShipQnADto.isCompleted() && !factoryShipQnADto.isReattempt())) {
            titleList.add(getString(R.string.Correct_Answer));
            myItemTouchHelper.setDraggable(false);
            submitText.setText(R.string.next);
            description.setVisibility(View.GONE);
            divider.setVisibility(View.VISIBLE);
            tabView.setVisibility(View.VISIBLE);
            submitted = true;
        }
    }
    private void setUpMatchTheseLayout(View view) {
        tabView = view.findViewById(R.id.tab_view);
        myAnswer = view.findViewById(R.id.my_answer_layout);
        correctAnswer = view.findViewById(R.id.correct_answer_layout);
        myAnswerText = view.findViewById(R.id.my_answer_text);
        correctAnswerText = view.findViewById(R.id.correct_answer_text);
        myAnswerUnderline = view.findViewById(R.id.my_answer_underline);
        correctAnswerUnderline = view.findViewById(R.id.correct_answer_underline);
        leftRecyclerView = view.findViewById(R.id.rv_left_options);
        leftRecyclerView.setNestedScrollingEnabled(false);
        rightRecyclerView = view.findViewById(R.id.rv_right_options);
        rightRecyclerView.setNestedScrollingEnabled(false);
        divider = view.findViewById(R.id.divider);
        correctAnswerList = new ArrayList<>();
        userAnswerList = new ArrayList<>();
        userAnswerList.add("1-1");
        userAnswerList.add("2-2");
        userAnswerList.add("3-3");
        userAnswerList.add("4-4");
        setUpTouchHelper();
        setCorrectAnswerList(factoryShipQnADto.getAnswer());
        setLeftList(factoryShipQnADto.getOptions());
        setRightList(factoryShipQnADto.getAnswers());
        myAnswer.setOnClickListener(v -> {
            showMatchTheseRightAnswer = false;
            myAnswerText.setTextColor(getContext().getResources().getColor(R.color.selected_tab_match_these));
            myAnswerUnderline.setBackgroundColor(getContext().getResources().getColor(R.color.selected_tab_match_these));
            correctAnswerText.setTextColor(getContext().getResources().getColor(R.color.not_selected_tab_match_these));
            correctAnswerUnderline.setBackgroundColor(getContext().getResources().getColor(R.color.white));
            setRightList(userAnswerList);
            checkAnswer();
        });
        correctAnswer.setOnClickListener(v -> {
            showMatchTheseRightAnswer = true;
            myAnswerText.setTextColor(getContext().getResources().getColor(R.color.not_selected_tab_match_these));
            myAnswerUnderline.setBackgroundColor(getContext().getResources().getColor(R.color.white));
            correctAnswerText.setTextColor(getContext().getResources().getColor(R.color.selected_tab_match_these));
            correctAnswerUnderline.setBackgroundColor(getContext().getResources().getColor(R.color.selected_tab_match_these));
            setRightList(correctAnswerList);
            checkAnswer();
        });
        matchTheseAdapterLeft = new MatchTheseAdapterLeft(leftOptions, getContext());
        leftRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        leftRecyclerView.setAdapter(matchTheseAdapterLeft);

        rightRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        calculateHeight(view);
    }

    private void setUpTouchHelper() {
        matchTheseAdapterRight = new MatchTheseAdapterRight(rightOptions, getContext());
        myItemTouchHelper = new MyItemTouchHelper(matchTheseAdapterRight);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(myItemTouchHelper);
        matchTheseAdapterRight.setTouchHelper(itemTouchHelper);
        itemTouchHelper.attachToRecyclerView(rightRecyclerView);
        rightRecyclerView.setAdapter(matchTheseAdapterRight);
    }

    private void calculateHeight(View view) {
        TextView optionMt = view.findViewById(R.id.option_mt);
        LinearLayout layout = view.findViewById(R.id.fakeLayout);
        layout.setVisibility(View.VISIBLE);
        optionMt.setText(maxLengthText);
        optionMt.measure(0, 0);
        optionMt.getMeasuredWidth();
        getHeightOfLargestDescription(optionMt);
        int measuredHeight = optionMt.getMeasuredHeight();
        matchTheseAdapterRight.setHeight((int) (measuredHeight * 2));
        matchTheseAdapterLeft.setHeight((int) (measuredHeight * 2));
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
        final WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
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
            leftOptions.add(new MatchTheseLeftOption(i + 1, list.get(i)));
            if (maxLengthText.length() < list.get(i).length()) {
                maxLengthText = list.get(i);
            }
        }
    }

    void setRightList(List<String> list) {
        try {
            rightOptions.clear();
            if (factoryShipQnADto.isCompleted() && (!factoryShipQnADto.isReattempt() || factoryShipQnADto.isReview())) {
                BoardQuestionActivity.isMatchTheseSubmitted = true;
                userAnswerList.clear();
                String[] userAnswerArray;
                if (showMatchTheseRightAnswer)
                    userAnswerArray = factoryShipQnADto.getAnswer().split(",");
                else
                    userAnswerArray = factoryShipQnADto.getUserAnswer().split(",");
                userAnswerList = new ArrayList<>(Arrays.asList(userAnswerArray));
                for (int i = 0; i < userAnswerList.size(); i++) {
                    if (i < factoryShipQnADto.getAnswers().size()) {
                        String[] result = userAnswerList.get(i).split("-");
                        if (result.length >= 1) {
                            int current = Integer.parseInt(result[1].trim());
                            rightOptions.add(new MatchTheseRightOption(current, factoryShipQnADto.getAnswers().get(current - 1)));
                            if (maxLengthText.length() < userAnswerList.get(i).length()) {
                                maxLengthText = userAnswerList.get(i);
                            }
                        }
                    }
                }
                tabView.setVisibility(View.VISIBLE);
                checkAnswer();
            } else {
                for (int i = 0; i < list.size(); i++) {
                    rightOptions.add(new MatchTheseRightOption(i + 1, list.get(i)));
                    if (maxLengthText.length() < list.get(i).length()) {
                        maxLengthText = list.get(i);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void checkAnswer() {
        myItemTouchHelper.setDraggable(false);
        for (int i = 0; i < userAnswerList.size(); i++) {
            if (i < rightOptions.size() &&
                    i < leftOptions.size() &&
                      i<correctAnswerList.size()) {
                if (correctAnswerList.get(i).equals(userAnswerList.get(i))) {
                    try {
                        rightOptions.get(i).setCorrectlyMatched(true);
                        leftOptions.get(i).setCorrectlyMatched(true);
                    } catch (Exception e) {
                        Log.i("INDEX_OUT_OF_BOUND", "Index out of range");
                    }
                } else {
                    try {
                        rightOptions.get(i).setCorrectlyMatched(false);
                        leftOptions.get(i).setCorrectlyMatched(false);
                    } catch (Exception e) {
                        Log.i("INDEX_OUT_OF_BOUND", "Index out of range");
                    }

                }
            }
        }
        if (matchTheseAdapterLeft != null) {
            matchTheseAdapterLeft.notifyDataSetChanged();
        }
        if (matchTheseAdapterRight != null) {
            matchTheseAdapterRight.notifyDataSetChanged();
        }
    }

    private void setUpQuestionDetail() {
        questionNumber.setText("Q" + number + " of " + total);
        if (StringUtils.isNotEmpty(factoryShipQnADto.getImageQuestion())) {
            questionImage.setVisibility(View.VISIBLE);
            Glide.with(getActivity()).load(factoryShipQnADto.getImageQuestion()).into(questionImage);
            questionImage.setOnClickListener(vi -> {
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
        try{
            if (factoryShipQnADto.isCompleted()
                    && !factoryShipQnADto.isReattempt()
                    && (StringUtils.isNotEmpty(factoryShipQnADto.getAnswerEx()) || StringUtils.isNotEmpty(factoryShipQnADto.getAnswerSolImage()))
                    && StringUtils.isNotEmpty(factoryShipQnADto.getUserAnswer())) {
                solutionView.setVisibility(View.VISIBLE);
//                askDoubt.setVisibility(View.VISIBLE);
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
                    solutionImage.setOnClickListener(vi -> {
                        fullScreenImageDialog = new FullScreenImageDialog(requireContext(),
                                factoryShipQnADto.getAnswerSolImage(),FullViewImageType.SOLUTION);
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
        }catch (Exception e){
            Log.d( "setUpAnswerDetail:",e.getMessage());
        }
    }

    private void submit() {
        if (!submitted) {
            BoardQuestionActivity.isMatchTheseSubmitted = true;
            submit(Integer.parseInt(number.trim()) - 1);
            submitted = true;
            Handler handler = new Handler();
            Runnable runnable = () -> {
                description.setVisibility(View.GONE);
                divider.setVisibility(View.VISIBLE);
                submitText.setText(R.string.next);
                handler.removeCallbacksAndMessages(null);
                tabView.setVisibility(View.VISIBLE);
                setUpAnswerDetail();
            };
            handler.postDelayed(runnable, 3000);
        } else {
            submitListener.openNext();
        }
    }

    public void submit(int current) {
        userAnswerList.clear();
        userFinalAnswer = "";
        for (int i = 0; i < leftOptions.size() && i < rightOptions.size(); i++) {
            userAnswerOption = leftOptions.get(i).getOption_number() + "-" + rightOptions.get(i).getOption_number();
            userAnswerList.add(userAnswerOption);
            if (StringUtils.isNotEmpty(userFinalAnswer))
                userFinalAnswer = userFinalAnswer + "," + userAnswerOption;
            else
                userFinalAnswer = userAnswerOption;
        }
        factoryShipQnADto.setUserAnswer(userFinalAnswer);
        factoryShipQnADto.setCompleted(true);
        factoryShipQnADto.setReattempt(false);
        checkAnswer();
        boolean correct = factoryShipQnADto.getUserAnswer().equalsIgnoreCase(factoryShipQnADto.getAnswer().trim());
        submitListener.onSubmit(current, factoryShipQnADto, correct);
    }
}