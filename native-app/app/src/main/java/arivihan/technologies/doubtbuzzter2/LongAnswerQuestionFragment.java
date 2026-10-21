package arivihan.technologies.doubtbuzzter2;

import static android.app.Activity.RESULT_OK;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.EXTRACT_TEXT_FROM_IMAGE_URI;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.theartofdev.edmodo.cropper.CropImage;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.activity.ExoPlayerActivity;
import arivihan.technologies.doubtbuzzter2.adapter.OptionAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.OptionQuestionAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.UploadedImageRecyclerAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.MultipartRequestDataPart;
import arivihan.technologies.doubtbuzzter2.customView.SavBottomSheetDialog;
import arivihan.technologies.doubtbuzzter2.helper.FileUtil;
import arivihan.technologies.doubtbuzzter2.listener.OptionListener;
import arivihan.technologies.doubtbuzzter2.model.AnswerState;
import arivihan.technologies.doubtbuzzter2.model.ImageTextExtractionResponse;
import arivihan.technologies.doubtbuzzter2.model.InfoType;
import arivihan.technologies.doubtbuzzter2.model.LectureContentType;
import arivihan.technologies.doubtbuzzter2.model.OptionDto;
import arivihan.technologies.doubtbuzzter2.model.QuestionType;
import arivihan.technologies.doubtbuzzter2.model.TestSeriesQuestionDto;
import arivihan.technologies.doubtbuzzter2.volley.MyMultipartRequest;
import lombok.SneakyThrows;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;


public class LongAnswerQuestionFragment extends Fragment implements OptionListener, OptionQuestionAdapter.OnOptionClick, UploadedImageRecyclerAdapter.AddClickListener {

    int fragNumber;
    LinearLayout imageTextExtractionLoader;
    private CardView saveAnswer, clearAnswer;
    private TextView saveAnswerText, explanation, yourAttempt, correctPercentage;
    LottieAnimationView celebration;
    private EditText subjectiveAnswer, numericalAnswer;
    TestSeriesQuestionDto testSeriesQuestionDtoAnalytics = null;
    List<File> imagesToUpload = new ArrayList<>();
    private Uri imageuri;
    private Uri uri;
    CardView card;
    private boolean uploadReady = false;
    private TextView question, questionType, mark, responsetext, ansText, btnText, cardText;
    ArrayList<Uri> list;
    EditText extractedText;
    LinearLayout extractedLinearLayout;
    String Type = "";
    private static final int CAMERA_PERMISSION_CODE = 100;
    private ImageView questionImage;
    private TestSeriesQuestionDto dto;
    TextView wordCounter, checkBeforeSubmitMsg;
    Map<String, Integer> map;
    private OptionAdapter optionAdapter;
    Dialog dialog;
    int croppingImageIndex = -1;
    Map<String, Integer> maped = new HashMap<>();
    TextView subText, text_centre;
    private List<OptionDto> optionDtos = new ArrayList<>();
    private OptionListener optionListener;
    private LinearLayout markLayout, markLinearlayout;
    private ToggleButton markButton;
    private static final String ARG_PARAM1 = "question";
    private static final String ARG_PARAM2 = "listener";
    private static final String ARG_PARAM3 = "map";
    private static final String ARG_PARAM5 = "qNo";
    ConstraintLayout uploadFileLayout;
    int imageAdded = 0;
    private LinearLayout numberOfWordsLayout;
    View extractedTextFromImageView;
    ConstraintLayout imageUploadLayout;
    CardView expandImage, collapseImage, backImage;
    Dialog image_pick_dialog;
    ConstraintLayout imageExtractionLayout;
    RecyclerView uploadedImageRecyclerView;
    UploadedImageRecyclerAdapter uploadedImageRecyclerAdapter;
    String currentPhotoPath = "";

    public LongAnswerQuestionFragment() {
    }

    public static LongAnswerQuestionFragment newInstance(int fragNumber, TestSeriesQuestionDto dto,
                                                         TestSeriesQuestionDto testSeriesQuestionDtoAnalytics,
                                                         Map<String, Integer> map) {
        LongAnswerQuestionFragment fragment = new LongAnswerQuestionFragment();
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

        View v = inflater.inflate(R.layout.fragment_subjective_question, container, false);

        Type = MyApplication.getStringData(PreferenceKeys.RESPONSE.getKey());

        markLayout = v.findViewById(R.id.mark);
        mark = v.findViewById(R.id.markText);
        ansText = v.findViewById(R.id.CorrectAnswer);
        clearAnswer = v.findViewById(R.id.button_clear_answer);
        yourAttempt = v.findViewById(R.id.yourAttempt);
        celebration = v.findViewById(R.id.celebration);
        responsetext = v.findViewById(R.id.YourResponse);
        subText = v.findViewById(R.id.subText);
        markLinearlayout = v.findViewById(R.id.markLinearLayout);
        markButton = v.findViewById(R.id.mark_button2);
        explanation = v.findViewById(R.id.explanation);
        correctPercentage = v.findViewById(R.id.Correctionpercentage);
        questionType = v.findViewById(R.id.questionType);
        saveAnswer = v.findViewById(R.id.button_answer_save);
        saveAnswerText = v.findViewById(R.id.btn_text);
        card = v.findViewById(R.id.card_enable);
        subjectiveAnswer = v.findViewById(R.id.integerAnswer);
        numericalAnswer = v.findViewById(R.id.NumericalAnswer);
        imageTextExtractionLoader = v.findViewById(R.id.extracting_text_layout);
        extractedTextFromImageView = v.findViewById(R.id.edit_text_layout);


        imageUploadLayout = v.findViewById(R.id.upload_image_layout);
        imageExtractionLayout = v.findViewById(R.id.image_extraction_layout);
        extractedLinearLayout = v.findViewById(R.id.extracted_text_linear_layout);
        expandImage = v.findViewById(R.id.expand_view_card);
        collapseImage = v.findViewById(R.id.collapse_view_card);
        backImage = v.findViewById(R.id.back_image_card);
//        text_centre = v.findViewById(R.id.text_centre);
        uploadedImageRecyclerView = v.findViewById(R.id.uploaded_image_rv);
        numberOfWordsLayout = v.findViewById(R.id.number_of_words_layout);
        cardText = v.findViewById(R.id.card_text);
        setSubjectiveLayout(v);
        extractedText = v.findViewById(R.id.extracted_edit_text);
        wordCounter = v.findViewById(R.id.number_of_words);
        checkBeforeSubmitMsg = v.findViewById(R.id.check_text);

        Log.i("MARKSONQUESTION","MARKS :: " + dto.getScore());

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

        if (Type.equals(PreferenceKeys.RESPONSE.getKey())) {
            yourAttempt.setVisibility(View.VISIBLE);
        }

        subjectiveAnswer.setOnKeyListener((v13, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getKeyCode() ==       KeyEvent.KEYCODE_ENTER)
            {
                Log.i("numerical", "captured");

                return false;
            }

            return false;
        });

        numericalAnswer.setOnEditorActionListener((vw, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                return true;
            }
            return false;
        });




        saveAnswer.setOnClickListener(view -> {
            hideKeybaord(v);

            if (uploadReady) {
                extractTextFromImage();
            }

        });

        extractedText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                wordCount = s.toString().split(" ").length;
//                wordCounter.setText(wordCount + " ");
//                if (wordCount > Integer.parseInt(microLectureContentDto.getWordLimit()) + word_limit_buffer) {
//                    wordCounter.setTextColor(getResources().getColor(R.color.match_red));
//                    sub.setVisibility(View.GONE);
//                    card.setVisibility(View.VISIBLE);
//                    cardText.setText(getResources().getString(R.string.submit));
//                    card.setClickable(true);
//                    checkBeforeSubmitMsg.setText(getResources().getString(R.string.word_limit_text));
//                    checkBeforeSubmitMsg.setTextColor(getResources().getColor(R.color.match_red));
//                } else if (wordCount > Integer.parseInt(microLectureContentDto.getWordLimit())) {
//                    wordCounter.setTextColor(getResources().getColor(R.color.match_red));
//                    sub.setVisibility(View.VISIBLE);
//                    btnText.setText(R.string.submit);
//                    card.setVisibility(View.GONE);
//                    card.setClickable(false);
//                    checkBeforeSubmitMsg.setText(getResources().getString(R.string.check_your_answer_before_submitting));
//                    checkBeforeSubmitMsg.setTextColor(getResources().getColor(R.color.check_text_color));
//                } else {
//                    wordCounter.setTextColor(getResources().getColor(R.color.marks_color));
//                    sub.setVisibility(View.VISIBLE);
//                    btnText.setText(R.string.submit);
//                    card.setVisibility(View.GONE);
//                    card.setClickable(false);
//                    checkBeforeSubmitMsg.setText(getResources().getString(R.string.check_your_answer_before_submitting));
//                    checkBeforeSubmitMsg.setTextColor(getResources().getColor(R.color.check_text_color));
//                }
            }

            @Override
            public void afterTextChanged(Editable s) {

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

    private void extractTextFromImage() {
        imageTextExtractionLoader.setVisibility(View.VISIBLE);
        saveAnswer.setVisibility(View.GONE);
        MyMultipartRequest multipartRequest = new MyMultipartRequest(Request.Method.POST, EXTRACT_TEXT_FROM_IMAGE_URI, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                try {
                    TypeToken<ImageTextExtractionResponse> token = new TypeToken<ImageTextExtractionResponse>() {
                    };
                    Gson gson = new Gson();
                    ImageTextExtractionResponse extractionResponse = gson.fromJson(new String(response.data, "UTF-8"), token.getType());
                    Log.d("MULTIPARTRES", "RESPONSE : " + extractionResponse.getMessage());

//
//                    if(!extractionResponse.isTextExtracted()){
//                        SavBottomSheetDialog savBottomSheetDialog = new SavBottomSheetDialog(this);
//                        Bundle bundle = new Bundle();
//                        bundle.putBoolean("error", null);
//                        bundle.putBoolean("textExtracted", extractionResponse.isTextExtracted());
//                        bundle.putString("finalText", extractionResponse.getMessage());
//                        try {
//                            savBottomSheetDialog.setArguments(bundle);
//                            savBottomSheetDialog.show(getParentFragmentManager(), "");
//                            savBottomSheetDialog.setCancelable(false);
//                        } catch (Exception e) {
//                            Log.i("BOTTOM_SHEET", "Fragment not associated with a fragment manager");
//                        }
//                        return;
//                    }

                    extractedText.setText(extractionResponse.getMessage());
                    extractedText.requestFocus();
                    extractedText.setSelection(0);
                    extractedText.setCursorVisible(true);
//                    isTextExtracting = false;
//
//                    TextView wordLimit = findViewById(R.id.word_limit);
//                    wordCounter.setText(extractionResponse.getMessage().split(" ").length + " ");
//                    wordLimit.setText("/ " + microLectureContentDto.getWordLimit() + " ");
//
//                    word_limit_buffer = MyApplication.getFirebaseRemoteConfig().getLong("word_limit_buffer");
//                    wordCount = extractionResponse.getMessage().split(" ").length;
//                    if (wordCount > Integer.parseInt(microLectureContentDto.getWordLimit()) + word_limit_buffer) {
//                        wordCounter.setTextColor(getResources().getColor(R.color.match_red));
//                        sub.setVisibility(View.GONE);
//                        card.setVisibility(View.VISIBLE);
//                        cardText.setText(getResources().getString(R.string.submit));
//                        card.setClickable(true);
//                        checkBeforeSubmitMsg.setText(getResources().getString(R.string.word_limit_text));
//                        checkBeforeSubmitMsg.setTextColor(getResources().getColor(R.color.match_red));
//                    } else if (wordCount > Integer.parseInt(microLectureContentDto.getWordLimit())) {
//                        wordCounter.setTextColor(getResources().getColor(R.color.match_red));
//                        sub.setVisibility(View.VISIBLE);
//                        btnText.setText(R.string.submit);
//                        card.setVisibility(View.GONE);
//                    } else {
//                        wordCounter.setTextColor(getResources().getColor(R.color.marks_color));
//                        sub.setVisibility(View.VISIBLE);
//                        btnText.setText(R.string.submit);
//                        card.setVisibility(View.GONE);
//                    }


                    checkBeforeSubmitMsg.setVisibility(View.VISIBLE);

                    saveAnswer.setVisibility(View.VISIBLE);
                    saveAnswerText.setText(getString(R.string.save));
                    uploadReady =  false;
                    imageTextExtractionLoader.setVisibility(View.GONE);
                    imageExtractionLayout.setVisibility(View.GONE);
                    extractedTextFromImageView.setVisibility(View.VISIBLE);
//                    isTextExtractedFromImage = true;
//                    extractedTextFromImage = extractionResponse.getMessage();


                } catch (Exception e) {
                    Log.e("Error in subscription", e.getMessage(), e);
                    imageTextExtractionLoader.setVisibility(View.GONE);
                    saveAnswer.setVisibility(View.VISIBLE);
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


        Volley.newRequestQueue(getContext()).add(multipartRequest).setRetryPolicy(new DefaultRetryPolicy(
                600000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }


    private void openGallery() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 118);
    }

    public void checkCameraPermission(String permission, int requestCode) {
        if (getContext()!= null || getActivity() != null) {
            return;
        }
        if (ContextCompat.checkSelfPermission(getContext(), permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, requestCode);
        } else {
            captureImage();
        }
    }

    private void captureImage() {
        if (getContext() != null || getActivity() != null) {
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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void setSubjectiveLayout(View v) {
        list = new ArrayList<>();
        image_pick_dialog = new Dialog(getContext());
        image_pick_dialog.setContentView(R.layout.activity_select_photo);
        image_pick_dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        image_pick_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        image_pick_dialog.findViewById(R.id.cross).setOnClickListener(view -> image_pick_dialog.cancel());

        image_pick_dialog.findViewById(R.id.Choose_from_gallery).setOnClickListener(view -> {
            openGallery();
            image_pick_dialog.cancel();
            uploadedImageRecyclerAdapter = new UploadedImageRecyclerAdapter(getContext(), list, LongAnswerQuestionFragment.this, LectureContentType.LONG_ANSWERS);
            uploadedImageRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            uploadedImageRecyclerView.setAdapter(uploadedImageRecyclerAdapter);
        });
        image_pick_dialog.findViewById(R.id.click_a_photo).setOnClickListener(view -> {
            image_pick_dialog.cancel();
            checkCameraPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
            uploadedImageRecyclerAdapter = new UploadedImageRecyclerAdapter(getContext(), list, LongAnswerQuestionFragment.this, LectureContentType.LONG_ANSWERS);
            uploadedImageRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            uploadedImageRecyclerView.setAdapter(uploadedImageRecyclerAdapter);
        });
        uploadFileLayout = v.findViewById(R.id.upload_file_layout);
//        text_centre.setOnClickListener(view -> image_pick_dialog.show());

        uploadFileLayout.setOnClickListener(view -> image_pick_dialog.show());

        backImage.setOnClickListener(view -> {
            extractedTextFromImageView.setVisibility(View.GONE);
            imageExtractionLayout.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.BELOW, R.id.relativeLayout);
            params.addRule(RelativeLayout.ABOVE, R.id.bottom_card);
            extractedTextFromImageView.setLayoutParams(params);
            card.setVisibility(View.GONE);
        });

        expandImage.setOnClickListener(view -> {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//            params.addRule(RelativeLayout.BELOW, R.id.subText);
//            params.addRule(RelativeLayout.ABOVE, R.id.button_answer_save);
            extractedTextFromImageView.setLayoutParams(params);
            expandImage.setVisibility(View.GONE);
            collapseImage.setVisibility(View.VISIBLE);
        });

        collapseImage.setOnClickListener(view -> {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//            params.addRule(RelativeLayout.BELOW, R.id.subText);
//            params.addRule(RelativeLayout.ABOVE, R.id.button_answer_save);
            extractedTextFromImageView.setLayoutParams(params);
            expandImage.setVisibility(View.VISIBLE);
            collapseImage.setVisibility(View.GONE);
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (getActivity() == null || getContext() == null) {
            return;
        }

        if (requestCode == 118 && resultCode == RESULT_OK) {

            imageUploadLayout.setVisibility(View.GONE);
            if (data != null && data.getClipData() != null) {

                ClipData clipData = data.getClipData();


                for (int i = 0; i < clipData.getItemCount(); i++) {

                    Uri imgUri = clipData.getItemAt(i).getUri();
                    Log.d("IMAGEURI", "Image uri forloop :: " + imgUri);

                    float lengthmb = getImageSize(imgUri);
                    File imgFile = null;
                    try {
                        imgFile = FileUtil.from(getActivity(), imgUri);
                    } catch (IOException e) {
                        Log.e("Error in subscription", e.getMessage(), e);
                    }

                    if (lengthmb <= 15) {
                        if (dto.getQuestionType().equalsIgnoreCase(QuestionType.LONG_ANSWERS.name()) && list.size() < 2) {
                            Log.d("IMAGEURI", "SHORT ANSER" + list.size());
                            list.add(imgUri);
                            imagesToUpload.add(imgFile);
                            if (uploadedImageRecyclerAdapter != null) {
                                uploadedImageRecyclerAdapter.notifyDataSetChanged();
                            }
                        } else if (dto.getQuestionType().equalsIgnoreCase(QuestionType.LONG_ANSWERS.name()) && list.size() < 3) {
                            list.add(imgUri);
                            imagesToUpload.add(imgFile);
                            if (uploadedImageRecyclerAdapter != null) {
                                uploadedImageRecyclerAdapter.notifyDataSetChanged();
                            }
                        }
                    } else
                        Toast.makeText(getContext(), "size larger than 15mb", Toast.LENGTH_LONG).show();

                    if (list.size() > 0) {
                        imageTextExtractionLoader.setVisibility(View.GONE);
                    }
                }
            } else {
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
                    imageExtractionLayout.setVisibility(View.VISIBLE);
                }
            }

            saveAnswerText.setText(getString(R.string.upload));
            uploadReady = true;
        }


        if (requestCode == 19 && resultCode == RESULT_OK) {
            CropImage.activity(imageuri).start(getActivity());

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
                        Toast.makeText(getContext(), "size larger than 15mb", Toast.LENGTH_LONG).show();

                    if (list.size() > 0) {
                        imageExtractionLayout.setVisibility(View.VISIBLE);
                    }

                } catch (Exception error) {
                    Log.e("resultUri", error.getMessage(), error);
                }
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
    private void hideKeybaord(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
    }


    private void update() {
        saveAnswerText.setText(getString(R.string.update));
        dto.setAnswerState(AnswerState.ANSWERED);
        if (!Type.equals(PreferenceKeys.RESPONSE.getKey()))
            clearAnswer.setVisibility(View.VISIBLE);
        changeAnswerState(dto.isMarked());
    }

    private void update2() {

        saveAnswerText.setText(getString(R.string.save));
        dto.setAnswerState(AnswerState.UNANSWERED);
        clearAnswer.setVisibility(View.GONE);
        changeAnswerState(dto.isMarked());
    }

    @Override
    public void onResume() {
        super.onResume();
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

    @Override
    public void addClicked() {

    }

    @Override
    public void deleteClicked(int position) {

    }

    @Override
    public void addCropClicked(int position) {

    }

    @Override
    public void onClick(int position) {

    }

    public void updateView() {

    }
}