package arivihan.technologies.doubtbuzzter2.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.OptIn;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.OptionQuestionAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.constants.QuestionType;
import arivihan.technologies.doubtbuzzter2.fragments.Correct_Answer_AnimationFragment;
import arivihan.technologies.doubtbuzzter2.fragments.Wrong_Answer_AnimationFragment;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.model.LectureContentType;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureContentDto;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureInfoDto;
import arivihan.technologies.doubtbuzzter2.model.OptionModel;
import arivihan.technologies.doubtbuzzter2.model.TopicModel;
import arivihan.technologies.doubtbuzzter2.model.UserMicrolectureAnswer;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_MICROLECTURE_CONTENT;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.POST_MICROLECTURE_ANSWER;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class QuestionActivity extends LocaleActivity implements OptionQuestionAdapter.OnOptionClick {

    Gson gson = new Gson();

    ArrayList<TopicModel> models;
    ArrayList<OptionModel> model = new ArrayList<>();
    ImageView questionImage, book, unbook;
    Dialog dialog;
    String chap;
    int selectedCurrentOption = -1;
    int ans = 0;
    String submited = "-1";
    int nextTopicPosition = 0;
    String contentUrl = "";
    RecyclerView options;
    ProgressBar progress;
    ScrollView scroll;
    private CardView revise;
    TextView topic_name, question1, question2, btnText, nextTopicMsg, nextTopicName, ques_num;
    CardView sub, nextQuestion;
    CardView card;
    ImageView back;
    FrameLayout ImageFrame, TextFrame;

    TextView explanation;
    int dataloadedAPI = 0;
    String quesType = "";
    String s = "";
    String idMicroLecture = "";
    RelativeLayout relativeLayout;
    ArrayList<String> optionList = new ArrayList<>();
    public String selectedOption, ques_no, Answer, id, title, ex;
    RelativeLayout ParentLayout;
    public int attempt = 0;
    public int data_loaded = 0;
    private MicroLectureContentDto microLectureContentDto;
    NetworkStatusLiveData networkStatusLiveData;
    Correct_Answer_AnimationFragment bottomSheetDialogFragment = new Correct_Answer_AnimationFragment();
    Wrong_Answer_AnimationFragment bottomSheetDialogFragment2 = new Wrong_Answer_AnimationFragment();
    ActivityResultLauncher<Intent> mLaunch = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // Handle the Intent
                }
            });

    private boolean openNext;

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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Disable Screen Schots----
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_question);
        ParentLayout = findViewById(R.id.ParentLayout);
        ParentLayout.setKeepScreenOn(true);

        microLectureContentDto = (MicroLectureContentDto) getIntent().
                getSerializableExtra(getResources().getString(R.string.passed_obj));
        chap = getIntent().getStringExtra("Chapter");
        id = getIntent().getStringExtra("Subject");
        title = getIntent().getStringExtra("Title");
        setQIdInCustomKeys();

        // FIND VIEW BY ID DEFINE-----------------
        explanation = findViewById(R.id.explanation);
        progress = findViewById(R.id.progress);
        options = findViewById(R.id.optionRecyclerView);
        options.setVisibility(View.VISIBLE);
        nextQuestion = findViewById(R.id.next);
        nextTopicMsg = findViewById(R.id.nextTopicMessage);
        nextTopicName = findViewById(R.id.nextTopicName);
        topic_name = findViewById(R.id.Topic_name);
        sub = findViewById(R.id.sub_btn);
        book = findViewById(R.id.bookmark_done);
        scroll = findViewById(R.id.scroll);
        back = findViewById(R.id.back_arrow);
        unbook = findViewById(R.id.bookmark_outline);
        relativeLayout = findViewById(R.id.relativeLayout);
        btnText = findViewById(R.id.btn_text);
        card = findViewById(R.id.card_enable);
        TextFrame = findViewById(R.id.Frame_with_Only_Text);
        ImageFrame = findViewById(R.id.Frame_with_image);
        questionImage = findViewById(R.id.image_question);
        ques_num = findViewById(R.id.question_number);
        question1 = findViewById(R.id.question_with_only_text);
        question2 = findViewById(R.id.question_with_image);

        //-----------------------------------


        Animation animation = AnimationUtils.loadAnimation(QuestionActivity.this, R.anim.top_bottom);
        relativeLayout.startAnimation(animation);


        // Data Fitting--------------
        TextFrame.setVisibility(View.VISIBLE);
        String ss = getIntent().getStringExtra("Title");
        if (ss.length() > 20) {
            ss = ss.substring(0, 20) + "...";
        }
        topic_name.setText(ss);
        optionList = getIntent().getStringArrayListExtra("Options");
        openNext = getIntent().getExtras().getBoolean("openNext");
        if (getIntent().getExtras().getString("Explanation") != null)
            ex = getIntent().getExtras().getString("Explanation");
        else
            ex = getString(R.string.no_explanation);
        ques_no = getIntent().getExtras().getString(PreferenceKeys.POSITION.getKey());

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
        quesType = getIntent().getStringExtra("QuesType");
        question1.setText(getIntent().getStringExtra("Question"));
        question2.setText(getIntent().getStringExtra("Question"));
        contentUrl = getIntent().getStringExtra("ImageQuestion");
        Answer = getIntent().getStringExtra("Answer");
        if (Answer != null) {
            ans = Integer.parseInt(Answer);
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
            Intent intent = new Intent(QuestionActivity.this, ZoomImageActivity.class);
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

        sub.setOnClickListener(view -> {
            card.setVisibility(View.GONE);
            if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                CommonMethods.showNegativeSnackbar(ParentLayout, getString(R.string.NoInternetConnection));
            } else if (attempt == 0) {
                submited = "1";
                OptionModel op;
                for (String s : optionList) {
                    op = new OptionModel(s, submited, ans, selectedCurrentOption, quesType);
                    model.add(op);
                }
                submited = "0";
                if (ans >= 0 && selectedCurrentOption >= 0) {
                    if (selectedCurrentOption != ans) {
                        sub.setVisibility(View.GONE);
                        progress.setVisibility(View.VISIBLE);
                        submitAnswer(microLectureContentDto, false, model.get(ans), model.get(selectedCurrentOption));
                    } else {
                        sub.setVisibility(View.GONE);
                        progress.setVisibility(View.VISIBLE);
                        submitAnswer(microLectureContentDto, true, model.get(ans), model.get(selectedCurrentOption));
                    }
                }
            }

        });


        s = getIntent().getStringExtra("Chapter");
        idMicroLecture = getIntent().getStringExtra("Id");

        back.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());


        explanation.setOnClickListener(view -> {
            dialog = new Dialog(QuestionActivity.this);
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

            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(QuestionActivity.this, ZoomImageActivity.class);
                    intent.putExtra("Url", url);
                    startActivity(intent);
                }
            });

            exp.setText(ex);

            copy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", ex);
                    clipboard.setPrimaryClip(clip);
                    CommonMethods.showPositiveSnackbar(ParentLayout, getString(R.string.ExplanationCopied) + "!");
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            revise.setOnClickListener(v -> {

                microLectureContentDto.setVideoUrl(microLectureContentDto.getAnswerVideo());
                @OptIn(markerClass = UnstableApi.class) Intent intent = new Intent(QuestionActivity.this,
                        VideoPlayerActivity.class);

                intent.putExtra(getResources().getString(R.string.passed_obj), microLectureContentDto);
                intent.putExtra("shouldNotResume", true);
                intent.putExtra(getString(R.string.back_to_q), true);
                mLaunch.launch(intent);
            });

            dialog.show();
        });

        nextQuestion.setOnClickListener(view -> {
            if (MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                int i = 0;
                for (i = 0; i < models.size(); i++) {
                    if (models.get(i).getTitle().equals(title))
                        break;
                }
//                if (!getIntent().getStringExtra("Chapter").equals(PreferenceKeys.PERSONALISED_TYPE.getKey())) {
                if (Integer.parseInt(ques_no) < Integer.parseInt(models.get(i).noOfQuestions)) {
                    nextQuestion();
                }
//                }
            } else {
                CommonMethods.showNegativeSnackbar(ParentLayout, getString(R.string.NoInternetConnection));
            }
        });

        nextTopicName.setOnClickListener(view -> {
            if (MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {

                OpenNextTopicQuestions();
            } else {
                CommonMethods.showNegativeSnackbar(ParentLayout, getString(R.string.NoInternetConnection));
            }
        });

        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            if (isOnline) {
                if (dataloadedAPI == 0) {
                    dataloadedAPI = 1;
                    if (openNext) {
                        getData(id);
                    }
                }
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    CommonMethods.showPositiveSnackbar(ParentLayout, getString(R.string.InternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
                }
            } else {
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                    CommonMethods.showNegativeSnackbar(ParentLayout, getString(R.string.NoInternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), false);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), true);
                }
            }
        });


        if (CommonMethods.isOnline(this)) {
            if (dataloadedAPI == 0) {
                dataloadedAPI = 1;
                if (openNext) {
                    getData(id);
                }
            }
            if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
            }
        } else {
            if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                CommonMethods.showNegativeSnackbar(ParentLayout, getString(R.string.NoInternetConnection));
                MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
            }
        }

        nextQuestion.setVisibility(View.GONE);

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
                    .microlectureId(microLectureContentDto.getId())
                    .correct(isCorrect)
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

                OptionQuestionAdapter adapter = new OptionQuestionAdapter(model, QuestionActivity.this, this);
                options.setAdapter(adapter);
                if (StringUtils.isNotEmpty(microLectureContentDto.getAnswerEx())
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
                        bottomSheetDialogFragment.show(fragmentManager, "Add_Fragment");
                    } else {
                        book.setVisibility(View.GONE);
                        unbook.setVisibility(View.GONE);
                        bottomSheetDialogFragment2.show(fragmentManager, "Add_Fragment");
                    }
                }
                sub.setVisibility(View.GONE);
                if (data_loaded == 1)
//                    if (!getIntent().getStringExtra("Chapter").equals(PreferenceKeys.PERSONALISED_TYPE.getKey())) {
                    nextQuestion.setVisibility(View.VISIBLE);
//                    }
                if (data_loaded == -1 || data_loaded == -2)
                    progress.setVisibility(View.GONE);
                if (data_loaded == 0)
                    progress.setVisibility(View.VISIBLE);
                if (data_loaded == -2) {
                    nextTopicMsg.setVisibility(View.VISIBLE);
                    nextTopicName.setVisibility(View.VISIBLE);
                    nextTopicName.setText(models.get(nextTopicPosition + 1).getTitle());
                }
                data_loaded = 1;

                if (!openNext) {
                    new Handler().postDelayed(() -> {
                        if (!isFinishing() && !isDestroyed()) {
                            getOnBackPressedDispatcher().onBackPressed();
                        }
                    }, 2000);
                }
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
        }

        return QuestionType.NUMERICAL;
    }

    private void updareRecycerViewData() {
        options.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        ArrayList<OptionModel> model = new ArrayList<>();
        ans--;
        OptionModel op;
        for (String s : optionList) {
            op = new OptionModel(s, submited, ans, -1, quesType);
            model.add(op);
        }
        Animation scale = AnimationUtils.loadAnimation(this, R.anim.scale);
        OptionQuestionAdapter adapter = new OptionQuestionAdapter(model, this, this);
        options.setAdapter(adapter);
        options.startAnimation(scale);
    }

    private void nextQuestion() {
        ArrayList<String> optionS = new ArrayList<>();
        int i = 0;
        for (i = 0; i < models.size(); i++) {
            if (models.get(i).getTitle().equals(title))
                break;
        }
        TopicModel topicModel = models.get(i);
        int position = Integer.parseInt(ques_no);
        if (topicModel.getNoOfQuestions().equals(position)) {
            sub.setVisibility(View.INVISIBLE);
        } else {
            MicroLectureContentDto MLCD = topicModel.getQuestionsDtos().get(position);
            Intent intent = new Intent(QuestionActivity.this, QuestionActivity.class);
            String p = (position + 1) + "";
            if (MLCD.getQuestion() == null)
                Toast.makeText(getApplicationContext(), "" + getString(R.string.NoValidQuestionFound), Toast.LENGTH_SHORT).show();
            else {
                if (getIntent().getStringExtra("Chapter").equals(PreferenceKeys.PERSONALISED_TYPE.getKey())) {
                    intent.putExtra("allQuestions", new Gson().toJson(models));
                    intent.putExtra("Chapter", PreferenceKeys.PERSONALISED_TYPE.getKey());
                } else {
                    intent.putExtra("Chapter", s);

                }

                intent.putExtra(getResources().getString(R.string.passed_obj), MLCD);
                intent.putExtra(PreferenceKeys.POSITION.getKey(), p);
                intent.putExtra("Title", title);
                intent.putExtra("Question", MLCD.getQuestion().trim());
                for (i = 0; i < MLCD.getOptions().size(); i++) {
                    optionS.add(MLCD.getOptions().get(i).trim());
                }
                intent.putStringArrayListExtra("Options", optionS);
                if (MLCD.getContentUrl() != null)
                    intent.putExtra("ImageQuestion", MLCD.getContentUrl());
                else
                    intent.putExtra("ImageQuestion", "");
                intent.putExtra("Answer", MLCD.getAnswer().trim());

                if (MLCD.getOptionType() != null)
                    intent.putExtra("QuesType", MLCD.getOptionType());
                else
                    intent.putExtra("QuesTye", "TEXT");

                if (MLCD.getAnswerEx() == null) {
                    intent.putExtra("Explanation", "");
                } else {
                    intent.putExtra("Explanation", MLCD.getAnswerEx().trim());
                }

                String url = MLCD.getAnswerSolImage();
                if (url != null)
                    intent.putExtra("AnswerImage", url);
                else
                    intent.putExtra("AnswerImage", "");

                intent.putExtra("Id", idMicroLecture);

                // for further questions---
                intent.putExtra("Subject", id);
                startActivity(intent);
                finish();
            }
        }
    }

    private void getData(String id) {

        if (getIntent().getStringExtra("Chapter").equals(PreferenceKeys.PERSONALISED_TYPE.getKey())) {
            TypeToken<ArrayList<TopicModel>> token = new TypeToken<ArrayList<TopicModel>>() {
            };
            Gson gson = new Gson();

            ArrayList<TopicModel> topicsModels = gson.fromJson(getIntent().getStringExtra("allQuestions"), token.getType());
//            updateRecyclerView(microLectureInfoDtos);
            updateRecyclerViewUsingContentDto(topicsModels);

        } else {

            Map<String, Object> map = new HashMap<>();
            map.put("chapterId", id);
            map.put("personalization", true);
            MyJsonArrayRequest myJsonObjectRequest = new MyJsonArrayRequest(Request.Method.GET,
                    CommonMethods.getUrlParam(GET_MICROLECTURE_CONTENT, map), null,
                    response -> {
                        Log.d("PERSONINDEX", response.toString());
                        Gson gson = new Gson();
                        TypeToken<List<MicroLectureInfoDto>> token = new TypeToken<List<MicroLectureInfoDto>>() {
                        };
                        List<MicroLectureInfoDto> microLectureInfoDtos = gson.fromJson(response.toString(), token.getType());
                        updateRecyclerView(microLectureInfoDtos);
                    }, new VolleyGlobalErrorListener(QuestionActivity.this) {
                @Override
                public void postError(VolleyError error) {
                    CommonMethods.showNegativeSnackbar(ParentLayout, error.getMessage());
                }
            });
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
        }
    }

    private void updateRecyclerViewUsingContentDto(ArrayList<TopicModel> topicModels) {
//        Log.d("QUESTIONDTOLENGTH", microLectureContentDtos.size() + "");
        models = topicModels;

//        TopicModel tm = new TopicModel();
//        tm.setTitle(title);
//        ArrayList<MicroLectureContentDto> questionsDtos = new ArrayList<>();
//
//        for (MicroLectureContentDto microLectureContentDto : microLectureContentDtos) {
//            if (microLectureContentDto.getLectureContentType().getType().equalsIgnoreCase(LectureContentType.NUMERICAL_QUESTION.getType()) ||
//                    microLectureContentDto.getLectureContentType().getType().equalsIgnoreCase(LectureContentType.CONCEPTUAL_QUESTION.getType())) {
//                microLectureContentDto.setHeader_title(PreferenceKeys.PERSONALISED_TYPE.getKey());
//                questionsDtos.add(microLectureContentDto);
//            }
//        }
//        tm.setNoOfQuestions(questionsDtos.size() + "");
//        tm.setQuestionsDtos(questionsDtos);
//        models.add(tm);

        if (data_loaded == 1) {
//            if (!getIntent().getStringExtra("Chapter").equals(PreferenceKeys.PERSONALISED_TYPE.getKey())) {
            progress.setVisibility(View.GONE);
            nextQuestion.setVisibility(View.VISIBLE);
//            }
        }
        data_loaded = 1;


        int quePos = 0;
        for (int i = 0; i < models.size(); i++) {
            if (models.get(i).getTitle().equals(title))
                quePos = i;
            break;
        }


//        if (!getIntent().getStringExtra("Chapter").equals(PreferenceKeys.PERSONALISED_TYPE.getKey())) {
        TopicModel topicModel = models.get(quePos);

        int position = Integer.parseInt(ques_no);
        int l = Integer.parseInt(topicModel.getNoOfQuestions());
        if (position == l) {
            nextQuestion.setVisibility(View.INVISIBLE);
            data_loaded = -1;
            if (quePos != models.size() - 1 && models.get(quePos).getNoOfQuestions() != null) {
                if (Integer.parseInt(models.get(quePos + 1).getNoOfQuestions()) != 0) {
                    data_loaded = -2;
                    nextTopicPosition = quePos;
                } else {
                    data_loaded = -1;
                }
            }
        }
    }

    private void updateRecyclerView(List<MicroLectureInfoDto> microLectureInfoDtos) {
        int index = 0;
        models = new ArrayList<>();

        for (MicroLectureInfoDto microLectureInfoDto : microLectureInfoDtos) {

            if (microLectureInfoDto.getTitle().equalsIgnoreCase("Lectures Coming Soon")) {
                continue;
            }
            TopicModel tm = new TopicModel();

            tm.setTitle(microLectureInfoDto.getTitle());
            tm.setNoOfLecture("1");
            ArrayList<MicroLectureContentDto> lectureContentDtos = new ArrayList<>();
            ArrayList<MicroLectureContentDto> questionsDtos = new ArrayList<>();

            for (MicroLectureContentDto microLectureContentDto : microLectureInfoDto.getMicroLectureContentDtoList()) {

                if (microLectureContentDto.getLectureContentType().getType().equalsIgnoreCase(LectureContentType.VIDEO.getType())) {
                    microLectureContentDto.setHeader_title(microLectureInfoDto.getTitle());
                    lectureContentDtos.add(microLectureContentDto);
                } else if (microLectureContentDto.getLectureContentType().getType().equalsIgnoreCase(LectureContentType.NUMERICAL_QUESTION.getType()) ||
                        microLectureContentDto.getLectureContentType().getType().equalsIgnoreCase(LectureContentType.CONCEPTUAL_QUESTION.getType())) {
                    microLectureContentDto.setHeader_title(microLectureInfoDto.getTitle());
                    questionsDtos.add(microLectureContentDto);
                } else if (microLectureContentDto.getLectureContentType().getType().equalsIgnoreCase(LectureContentType.NOTES.getType())) {
                    //TODO have to add
                }
            }


            tm.setNoOfLecture(lectureContentDtos.size() + "");
            tm.setNoOfQuestions(questionsDtos.size() + "");
            tm.setLecturesDtos(lectureContentDtos);
            tm.setQuestionsDtos(questionsDtos);
            models.add(tm);
            Log.i("Model title", "  " + models.get(index).getTitle());
            index++;
        }

        if (data_loaded == 1) {
//            if (!getIntent().getStringExtra("Chapter").equals(PreferenceKeys.PERSONALISED_TYPE.getKey())) {
            progress.setVisibility(View.GONE);
            nextQuestion.setVisibility(View.VISIBLE);
//            }
        }
        data_loaded = 1;


        int i = 0;
        for (i = 0; i < models.size(); i++) {
            if (models.get(i).getTitle().equals(title))
                break;
        }



//        if (!getIntent().getStringExtra("Chapter").equals(PreferenceKeys.PERSONALISED_TYPE.getKey())) {
        TopicModel topicModel = models.get(i);

        int position = Integer.parseInt(ques_no);
        int l = Integer.parseInt(topicModel.getNoOfQuestions());
        if (position == l) {
            nextQuestion.setVisibility(View.INVISIBLE);
            data_loaded = -1;
            if (i != models.size() - 1 && models.get(i).getNoOfQuestions() != null) {
                if (Integer.parseInt(models.get(i + 1).getNoOfQuestions()) != 0) {
                    data_loaded = -2;
                    nextTopicPosition = i;
                } else {
                    data_loaded = -1;
                }
            }
        }

//        }


    }

    private void OpenNextTopicQuestions() {
        ArrayList<String> optionS = new ArrayList<>();
        int i = 0;
        String topicName = nextTopicName.getText().toString();
        for (i = 0; i < models.size(); i++) {
            if (models.get(i).getTitle().equals(topicName))
                break;
        }
        TopicModel topicModel = models.get(i);
        int position = 0;

        if (topicModel.getNoOfQuestions().equals(position)) {
            sub.setVisibility(View.INVISIBLE);
        } else {
            MicroLectureContentDto MLCD = topicModel.getQuestionsDtos().get(position);
            Intent intent = new Intent(QuestionActivity.this, QuestionActivity.class);
            String p = (position + 1) + "";
            if (MLCD.getQuestion() == null)
                Toast.makeText(getApplicationContext(), "" + getString(R.string.NoValidQuestionFound), Toast.LENGTH_SHORT).show();
            else {
                Log.i("Question  ", "" + MLCD.getQuestion());
                Log.i("Answer  ", "" + MLCD.getAnswer());
                intent.putExtra(PreferenceKeys.POSITION.getKey(), p);
                intent.putExtra("Title", topicName);
                intent.putExtra("Question", MLCD.getQuestion());
                for (i = 0; i < MLCD.getOptions().size(); i++) {
                    optionS.add(MLCD.getOptions().get(i));
                }
                if (MLCD.getOptionType() != null)
                    intent.putExtra("QuesType", MLCD.getOptionType());
                else
                    intent.putExtra("QuesType", "TEXT");

                intent.putStringArrayListExtra("Options", optionS);
                intent.putExtra("Image_Question", MLCD.getContentUrl());
                intent.putExtra("Answer", MLCD.getAnswer());

                String url = MLCD.getAnswerSolImage();
                if (url != null)
                    intent.putExtra("AnswerImage", url);
                else
                    intent.putExtra("AnswerImage", "no");


                // for further questions---
                intent.putExtra("Subject", id);

                // startActivity(intent);
                Intent intent1 = new Intent(QuestionActivity.this, MicrolectureListActivity.class);
                intent1.putExtra("chapterName", chap);
                intent1.putExtra("chapterId", id);
                startActivity(intent1);
                finish();
            }
        }
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
                op = new OptionModel(s, submited, ans, position, quesType);
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
}


