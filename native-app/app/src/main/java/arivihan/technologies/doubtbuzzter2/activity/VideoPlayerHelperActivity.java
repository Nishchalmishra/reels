package arivihan.technologies.doubtbuzzter2.activity;/*
 * File: VideoPlayerHelperActivity Copyright 23/09/24, 1:10 pm Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 23/09/24, 1:10 pm
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_CONTEXTUALIZED_DOUBT;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_DOUBT_INFO;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_MICROLECTURE_QANS;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.MICRO_LECTURE_PRACTICE_LIST_BY_TYPE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.SEND_FEEDBACK;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.SNACKBAR_LIST;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.UPDATE_VIDEO_PROGRESS;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.VALIDATE_ANSWER;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.OptIn;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.web_view.MicrolecturePractiseWebViewActivity;
import arivihan.technologies.doubtbuzzter2.adapter.microlecture.feedback.FeedbackOptionAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.customView.CustomTimeBar;
import arivihan.technologies.doubtbuzzter2.enums.LikeDislikeEnum;
import arivihan.technologies.doubtbuzzter2.listener.feedback.OnFeedbackDialogClickListener;
import arivihan.technologies.doubtbuzzter2.model.AskButtonState;
import arivihan.technologies.doubtbuzzter2.model.ConfirmOption;
import arivihan.technologies.doubtbuzzter2.model.ContentFeedbackType;
import arivihan.technologies.doubtbuzzter2.model.DoubtContextDto;
import arivihan.technologies.doubtbuzzter2.model.DoubtInfoDto;
import arivihan.technologies.doubtbuzzter2.model.DoubtResponseDto;
import arivihan.technologies.doubtbuzzter2.model.FactoryShipQnADto;
import arivihan.technologies.doubtbuzzter2.model.LectureContentType;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureContentDto;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureDto;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureEntity;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureQuestionAnsResponseDto;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureQuestionsAnsDto;
import arivihan.technologies.doubtbuzzter2.model.PracticeStatus;
import arivihan.technologies.doubtbuzzter2.model.Questions;
import arivihan.technologies.doubtbuzzter2.model.SnackBarDto;
import arivihan.technologies.doubtbuzzter2.model.UserFeedback;
import arivihan.technologies.doubtbuzzter2.model.VideoDetails;
import arivihan.technologies.doubtbuzzter2.model.VideoPlaybackSpeedDto;
import arivihan.technologies.doubtbuzzter2.model.microlecture.FeedbackModel;
import arivihan.technologies.doubtbuzzter2.model.microlecture.FeedbackOption;
import arivihan.technologies.doubtbuzzter2.observer.VideoPlayerAskButtonChangeListener;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import lombok.Setter;

public abstract class VideoPlayerHelperActivity extends BaseActivity implements VideoPlayerAskButtonChangeListener,
        RecognitionListener {

    private static final String TAG = VideoPlayerHelperActivity.class.getName();
    protected Button askButton;
    protected LinearLayout listeningLayout;
    protected List<VideoPlaybackSpeedDto> playbackSpeeds = new ArrayList<>();
    protected Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    protected Gson gson = new Gson();
    protected LottieAnimationView apiProcessingAnimation;
    protected LinearLayout successCelebration;
    protected LottieAnimationView celebration1, celebration2, celebration3;
    protected ImageView iv_active_interactive_question;
    protected MediaPlayer confettiSound;
    protected Handler confettiHandler;
    protected Runnable confettiRunnable;
    protected boolean liked;
//    protected boolean feedbackGiven;
    protected MicroLectureContentDto microLectureContentDto;
    private boolean isDialogShowing;
    //    Continuity code //
    ArrayList<MicroLectureDto> list = new ArrayList<>();
    ConstraintLayout continuityLayout;
    ImageView closeContinuityLayout;
    LinearLayout progressionLayout;
    TextView progressionText;
    ConstraintLayout continuityPlayLayout;
    CircularProgressIndicator circularProgressIndicator;
    LinearLayout skipToNextLayout;
    TextView skipToNextText;
    ArrayList<FactoryShipQnADto> qnADtoList = new ArrayList<>();
    int firstUnattemptedIndex;
    int progressionMicroLecturePos;
    int practicePosInProgression;
    MicroLectureDto progressionDto;
    MicroLectureDto skipDto;
    int currentMicroLecturePos;
    int calculateMicroLecturePos;
    int calculatePracticePos;
    int skipMicroLecturePos;
    int practicePosInSkip;
    boolean calculateStatus;
    boolean skipToPractice;
    boolean progressionIsPractice;
    boolean showDialog = true;
    private final Handler continuityHandler = new Handler();
    private int circularProgress = 0;
    private final int totalDuration = 5000;  // 5 seconds
    private final int stepDuration = 50;
    private String chapterId;
    private Dialog dialogReview;
    private Dialog dialogFeedbackInitial, dialogFeedbackDescriptive, dialogFeedbackFinal;
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz"; // You can include uppercase as well
    private static final int STRING_LENGTH = 5;
    private static final Random RANDOM = new Random();
    public boolean feedbackDialogShown;
    public long lastUpdatedPosition;

    //send mess to video player
    @Setter
    private OnUiChangeListener onUiChangeListener;

    public static String generateRandomString() {
        StringBuilder randomString = new StringBuilder(STRING_LENGTH);
        for (int i = 0; i < STRING_LENGTH; i++) {
            int index = RANDOM.nextInt(ALPHABET.length()); // Get a random index
            randomString.append(ALPHABET.charAt(index)); // Append the character at that index
        }
        return randomString.toString(); // Return the generated random string
    }

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
    }

    public void setAskButton(Button button) {
        this.askButton = button;
    }

    public void setMicroLectureContentDto(MicroLectureContentDto microLectureContentDto) {
        this.microLectureContentDto = microLectureContentDto;
    }

    protected void setApiProcessingAnimation(LottieAnimationView apiProcessingAnimation) {
        this.apiProcessingAnimation = apiProcessingAnimation;
    }

    protected void setListeningLayout(LinearLayout listeningLayout) {
        this.listeningLayout = listeningLayout;
    }

    public void initializeCelebrationView() {
        iv_active_interactive_question = findViewById(R.id.iv_active_interactive_question);

        successCelebration = findViewById(R.id.success_celebration);
        celebration1 = findViewById(R.id.celebration1);
        celebration2 = findViewById(R.id.celebration2);
        celebration3 = findViewById(R.id.celebration3);
        confettiSound = MediaPlayer.create(this, R.raw.confetti_sound);
        confettiHandler = new Handler(Looper.getMainLooper());
    }

    public List<VideoPlaybackSpeedDto> getPlayBackSpeedData() {
        playbackSpeeds.add(VideoPlaybackSpeedDto.builder().selected(false).speedText("0.25x").speed("0.25").build());
        playbackSpeeds.add(VideoPlaybackSpeedDto.builder().selected(false).speedText("0.5x").speed("0.5").build());
        playbackSpeeds.add(VideoPlaybackSpeedDto.builder().selected(false).speedText("0.75x").speed("0.75").build());
        playbackSpeeds.add(VideoPlaybackSpeedDto.builder().selected(true).speedText(getString(R.string.Normal)).speed("1").build());
        playbackSpeeds.add(VideoPlaybackSpeedDto.builder().selected(false).speedText("1.25x").speed("1.25").build());
        playbackSpeeds.add(VideoPlaybackSpeedDto.builder().selected(false).speedText("1.5x").speed("1.5").build());
//        playbackSpeeds.add(VideoPlaybackSpeedDto.builder().selected(false).speedText("1.75x").speed("1.75").build());  //scope changed product
        playbackSpeeds.add(VideoPlaybackSpeedDto.builder().selected(false).speedText("2x").speed("2").build());
        playbackSpeeds.add(VideoPlaybackSpeedDto.builder().selected(false).speedText("3x").speed("3").build());
        return playbackSpeeds;
    }

    public void showFeedbackDialogAtVideoEnd() {
        if (!feedbackDialogShown) {
            dialogReview = new Dialog(this);
            dialogReview.setContentView(R.layout.feedback_like_dislike_dialouge);
            if(dialogReview.getWindow()!=null) {
                dialogReview.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(this, R.drawable.bachground_custom_dialogu_typeactivity));
                dialogReview.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
            dialogReview.setCancelable(false);

            LottieAnimationView yes = dialogReview.findViewById(R.id.like);
            LottieAnimationView no = dialogReview.findViewById(R.id.dislike);
            LinearLayout linearLayout = dialogReview.findViewById(R.id.emoji_container);
            ImageButton closeButton = dialogReview.findViewById(R.id.close_button);
            TextView title = dialogReview.findViewById(R.id.title);
            CardView submit = dialogReview.findViewById(R.id.submit_feedback);
            EditText text = dialogReview.findViewById(R.id.feedback_text);
            TextView text2 = dialogReview.findViewById(R.id.feedback_text2);

            closeButton.setOnClickListener(v -> {
                MyApplication.saveData(PreferenceKeys.MICROLECTURE_DATA_RELOAD.getKey(), true);
                checkContinuity();
//                firebaseLectureEvent(null, new ArrayList<>());
                dialogReview.dismiss();
            });

            submit.setOnClickListener(v -> {
                submit.setVisibility(View.GONE);
                UserFeedback userFeedback = new UserFeedback();
                userFeedback.setLiked(liked);
                userFeedback.setFeedbackType(ContentFeedbackType.VIDEO_LECTURE);

                if (!text.getText().toString().isEmpty()) {
                    userFeedback.setComment(text.getText().toString());
                } else {
                    userFeedback.setComment("");
                }

                if (microLectureContentDto != null) {
                    userFeedback.setMicrolectureId(microLectureContentDto.getId());
                    userFeedback.setContentId(microLectureContentDto.getId());
                }
//                firebaseLectureEvent(text.getText().toString(), new ArrayList<>());
                Log.d("FeedbackSubmission", "User Feedback Comment: " + text.getText().toString());
                Log.d("FeedbackSubmission", "Feedback Liked: " + liked);
                saveResponseDataAPI(userFeedback);
                dialogReview.dismiss();
            });


            yes.setOnClickListener(v -> {
                linearLayout.setVisibility(View.GONE);
                liked = true;
//                feedbackGiven=true;
                title.setText(R.string.yipee_thanks);
                text2.setVisibility(View.VISIBLE);
                text2.setText(R.string.please_share_your_feedback_positive);
                linearLayout.setVisibility(View.GONE);
                submit.setVisibility(View.VISIBLE);
                text.setVisibility(View.VISIBLE);
            });

            no.setOnClickListener(v -> {
                linearLayout.setVisibility(View.GONE);
                liked = false;
//                feedbackGiven=true;
                title.setText(String.format(getString(R.string.sorry)));
                text2.setVisibility(View.VISIBLE);
                text2.setText(R.string.please_share_your_feedback_negative);
                linearLayout.setVisibility(View.GONE);
                submit.setVisibility(View.VISIBLE);
                text.setVisibility(View.VISIBLE);
            });
            dialogReview.show();
        }
        feedbackDialogShown = true;
    }

    protected void setLectureFeedbackDialog(OnFeedbackDialogClickListener listener){
        if (isFinishing() || isDestroyed()) return;
        if(dialogFeedbackInitial!=null && dialogFeedbackInitial.isShowing())
            dialogFeedbackInitial.dismiss();
        dialogFeedbackInitial = new Dialog(this);
        dialogFeedbackInitial.setContentView(R.layout.dialog_microlecture_feedback_initial);
        if(dialogFeedbackInitial.getWindow() != null) {
            dialogFeedbackInitial.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
            dialogFeedbackInitial.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
            dialogFeedbackInitial.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            EdgeToEdgeHelper.applyToDialog(dialogFeedbackInitial);
        }
        dialogFeedbackInitial.setCancelable(true);

        dialogFeedbackInitial.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    listener.onCanceled(true, "", null);
                    return true;
                }
                return false;
            }
        });

        String firebaseJsonStr = MyApplication.getFirebaseRemoteConfig()
                .getString("lecture_feedback_" + MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));

        RelativeLayout rl_dialog_parent = dialogFeedbackInitial.findViewById(R.id.rl_dialog_parent);
        CardView cv_primary = dialogFeedbackInitial.findViewById(R.id.cv_primary);
        CardView cv_secondary = dialogFeedbackInitial.findViewById(R.id.cv_secondary);
//        RelativeLayout rl_like_background = dialog.findViewById(R.id.rl_like_background);
//        RelativeLayout rl_dislike_background = dialog.findViewById(R.id.rl_dislike_background);
        CardView cv_like_background = dialogFeedbackInitial.findViewById(R.id.cv_like_background);
        CardView cv_dislike_background = dialogFeedbackInitial.findViewById(R.id.cv_dislike_background);
        TextView tv_primary_btn = dialogFeedbackInitial.findViewById(R.id.tv_primary_btn);
        TextView tv_secondary_btn = dialogFeedbackInitial.findViewById(R.id.tv_secondary_btn);
        TextView tv_like_text = dialogFeedbackInitial.findViewById(R.id.tv_like_text);
        TextView tv_dislike_text = dialogFeedbackInitial.findViewById(R.id.tv_dislike_text);
        TextView tv_title = dialogFeedbackInitial.findViewById(R.id.tv_title);
        TextView tv_sub_title = dialogFeedbackInitial.findViewById(R.id.tv_sub_title);

        LottieAnimationView lottie_feedback_like = dialogFeedbackInitial.findViewById(R.id.lottie_feedback_like);
        LottieAnimationView lottie_feedback_dislike = dialogFeedbackInitial.findViewById(R.id.lottie_feedback_dislike);

        lottie_feedback_like.cancelAnimation();
        lottie_feedback_like.setProgress(0f);
        cv_like_background.setCardBackgroundColor(ContextCompat.getColor(VideoPlayerHelperActivity.this, R.color.color_transparent));
        lottie_feedback_dislike.cancelAnimation();
        lottie_feedback_dislike.setProgress(0f);
        cv_dislike_background.setCardBackgroundColor(ContextCompat.getColor(VideoPlayerHelperActivity.this, R.color.color_transparent));

        ImageView iv_close = dialogFeedbackInitial.findViewById(R.id.iv_close);

        Gson gson = new Gson();
        FeedbackModel feedbackModel = gson.fromJson(firebaseJsonStr, FeedbackModel.class);
        if(feedbackModel!=null) {
            tv_primary_btn.setText(feedbackModel.getPrimaryBtnText());
            tv_secondary_btn.setText(feedbackModel.getSecondaryBtnText());
            tv_like_text.setText(feedbackModel.getSecondarySubText());
            tv_dislike_text.setText(feedbackModel.getPrimarySubText());
            tv_title.setText(feedbackModel.getTitle());
            tv_sub_title.setText(feedbackModel.getSubtitle());
        }

        AtomicReference<LikeDislikeEnum> likeDislikeEnum = new AtomicReference<>(LikeDislikeEnum.NOTHING);

        cv_like_background.setOnClickListener(cardView->{

            if(likeDislikeEnum.get() == LikeDislikeEnum.LIKE){
                return;
            }

            lottie_feedback_like.cancelAnimation();
            lottie_feedback_like.setProgress(0f);
            cv_like_background.setCardBackgroundColor(ContextCompat.getColor(VideoPlayerHelperActivity.this, R.color.color_transparent));
            lottie_feedback_dislike.cancelAnimation();
            lottie_feedback_dislike.setProgress(0f);
            cv_dislike_background.setCardBackgroundColor(ContextCompat.getColor(VideoPlayerHelperActivity.this, R.color.color_transparent));

            cv_like_background.setCardBackgroundColor(ContextCompat.getColor(VideoPlayerHelperActivity.this, R.color.subscription_card_background));
            likeDislikeEnum.set(LikeDislikeEnum.LIKE);
            lottie_feedback_like.playAnimation();
        });

        cv_dislike_background.setOnClickListener(cardView->{

            if(likeDislikeEnum.get() == LikeDislikeEnum.DISLIKE){
                return;
            }
            lottie_feedback_like.cancelAnimation();
            lottie_feedback_like.setProgress(0f);
            cv_like_background.setCardBackgroundColor(ContextCompat.getColor(VideoPlayerHelperActivity.this, R.color.color_transparent));
            lottie_feedback_dislike.cancelAnimation();
            lottie_feedback_dislike.setProgress(0f);
            cv_dislike_background.setCardBackgroundColor(ContextCompat.getColor(VideoPlayerHelperActivity.this, R.color.color_transparent));

            cv_dislike_background.setCardBackgroundColor(ContextCompat.getColor(VideoPlayerHelperActivity.this, R.color.dislike_card_bg));
            likeDislikeEnum.set(LikeDislikeEnum.DISLIKE);
            lottie_feedback_dislike.playAnimation();
        });

        iv_close.setOnClickListener(imageView->{
            dialogFeedbackInitial.dismiss();
            listener.onCanceled(true, "", null);
        });

        dialogFeedbackInitial.setOnDismissListener(v->{
            lottie_feedback_like.cancelAnimation();
            lottie_feedback_like.clearAnimation();
            lottie_feedback_dislike.cancelAnimation();
            lottie_feedback_dislike.clearAnimation();

            listener.onDismissed();
        });

        cv_primary.setOnClickListener(cardView->{
            if(likeDislikeEnum==null || likeDislikeEnum.get()==LikeDislikeEnum.NOTHING) {
                CommonMethods.showNegativeSnackbar(rl_dialog_parent, ContextCompat.getString(VideoPlayerHelperActivity.this, R.string.pehle_humein_apna_feedback_dein), 3000 );
//                Toast.makeText(VideoPlayerHelperActivity.this, ContextCompat.getString(VideoPlayerHelperActivity.this, R.string.pehle_humein_apna_feedback_dein), Toast.LENGTH_SHORT).show();
                return;
            }
            dialogFeedbackInitial.dismiss();
            listener.onPrimaryButtonClick(true,"", likeDislikeEnum.get());
        });

        cv_secondary.setOnClickListener(cardView->{
            listener.onSecondaryButtonClick(true,"", likeDislikeEnum.get());
            dialogFeedbackInitial.dismiss();
        });

        dialogFeedbackInitial.show();
    }

    protected void setLectureFeedbackDescriptiveDialog(boolean isLike, OnFeedbackDialogClickListener listener){
        try {
            if(dialogFeedbackDescriptive!=null && dialogFeedbackDescriptive.isShowing())
                dialogFeedbackDescriptive.dismiss();
            dialogFeedbackDescriptive = new Dialog(this);
            dialogFeedbackDescriptive.setContentView(R.layout.dialog_microlecture_feedback_discriptive);
            dialogFeedbackDescriptive.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
            dialogFeedbackDescriptive.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
            dialogFeedbackDescriptive.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            EdgeToEdgeHelper.applyToDialog(dialogFeedbackDescriptive);
            dialogFeedbackDescriptive.setCancelable(true);

            dialogFeedbackDescriptive.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                        listener.onCanceled(true, "", null);
                        return true;
                    }
                    return false;
                }
            });

            String firebaseJsonStr = MyApplication.getFirebaseRemoteConfig()
                    .getString("lecture_feedback_desc_" + (isLike ? "like" : "dislike") + "_" +
                            MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));

            Log.d(TAG, "firebaseJsonStr: " + firebaseJsonStr);

            RelativeLayout rl_like_background = dialogFeedbackDescriptive.findViewById(R.id.rl_like_background);
            RelativeLayout rl_dislike_background = dialogFeedbackDescriptive.findViewById(R.id.rl_dislike_background);
//            CardView cv_like_background = dialog.findViewById(R.id.cv_like_background);
//            CardView cv_dislike_background = dialog.findViewById(R.id.cv_dislike_background);

            RelativeLayout rl_dialog_parent = dialogFeedbackDescriptive.findViewById(R.id.rl_dialog_parent);
            CardView cv_secondary = dialogFeedbackDescriptive.findViewById(R.id.cv_secondary);
            CardView cv_primary = dialogFeedbackDescriptive.findViewById(R.id.cv_primary);
            TextView tv_primary_btn = dialogFeedbackDescriptive.findViewById(R.id.tv_primary_btn);
            TextView tv_secondary_btn = dialogFeedbackDescriptive.findViewById(R.id.tv_secondary_btn);
            EditText et_feedback = dialogFeedbackDescriptive.findViewById(R.id.et_feedback);

            RecyclerView rv_feedback_options = dialogFeedbackDescriptive.findViewById(R.id.rv_feedback_options);

            LottieAnimationView lottie_feedback_like = dialogFeedbackDescriptive.findViewById(R.id.lottie_feedback_like);
            LottieAnimationView lottie_feedback_dislike = dialogFeedbackDescriptive.findViewById(R.id.lottie_feedback_dislike);

            TextView tv_title = dialogFeedbackDescriptive.findViewById(R.id.tv_title);
            TextView tv_sub_title = dialogFeedbackDescriptive.findViewById(R.id.tv_sub_title);

            ImageView iv_close = dialogFeedbackDescriptive.findViewById(R.id.iv_close);

            rl_like_background.setVisibility(isLike?View.VISIBLE:View.GONE);
            rl_dislike_background.setVisibility(isLike?View.GONE:View.VISIBLE);

            Gson gson = new Gson();
            FeedbackModel feedbackModel = gson.fromJson(firebaseJsonStr, FeedbackModel.class);

            tv_title.setText(feedbackModel.getTitle());
            tv_sub_title.setText(feedbackModel.getSubtitle());

            tv_primary_btn.setText(feedbackModel.getPrimaryBtnText());
            tv_secondary_btn.setText(feedbackModel.getSecondaryBtnText());

            LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            rv_feedback_options.setLayoutManager(layoutManager);
            rv_feedback_options.setAdapter(new FeedbackOptionAdapter(this, feedbackModel.getFeedbackOptions(),
                    new FeedbackOptionAdapter.OnFeedbackOptionClickListener() {
                        @Override
                        public void onFeedbackOptionClick(FeedbackOption feedbackOption) {

                        }
                    }));

            iv_close.setOnClickListener(imageView -> {
                dialogFeedbackDescriptive.dismiss();
                listener.onCanceled(true, "", null);
            });

            cv_primary.setOnClickListener(cardView -> {
                ArrayList<String> selectedFeedbacks = new ArrayList<>();
                for(FeedbackOption feedbackOption : feedbackModel.getFeedbackOptions()) {
                    if(feedbackOption.isSelected()) {
                        selectedFeedbacks.add(feedbackOption.getMessage());
                    }
                }

                if(selectedFeedbacks.isEmpty() && et_feedback.getText().toString().trim().isEmpty()) {
                    CommonMethods.showNegativeSnackbar(rl_dialog_parent, ContextCompat.getString(VideoPlayerHelperActivity.this, R.string.pehle_humein_apna_feedback_dein), 3000 );
//                    Toast.makeText(VideoPlayerHelperActivity.this, ContextCompat.getString(VideoPlayerHelperActivity.this, R.string.pehle_humein_apna_feedback_dein), Toast.LENGTH_SHORT).show();
                    return;
                }
                dialogFeedbackDescriptive.dismiss();
                listener.onPrimaryButtonClick(true, et_feedback.getText().toString().trim(), selectedFeedbacks);
            });

            cv_secondary.setOnClickListener(cardView -> {
                dialogFeedbackDescriptive.dismiss();
                listener.onSecondaryButtonClick(true, et_feedback.getText().toString().trim(), null);
            });

            dialogFeedbackDescriptive.setOnDismissListener(v->{
                lottie_feedback_like.cancelAnimation();
                lottie_feedback_like.clearAnimation();
                lottie_feedback_dislike.cancelAnimation();
                lottie_feedback_dislike.clearAnimation();

                listener.onDismissed();
            });

            dialogFeedbackDescriptive.show();

        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    protected void setLectureFeedbackFinalDialog(OnFeedbackDialogClickListener listener){
        try {
            if(dialogFeedbackFinal!=null && dialogFeedbackFinal.isShowing())
                dialogFeedbackFinal.dismiss();
            dialogFeedbackFinal = new Dialog(this);
            dialogFeedbackFinal.setContentView(R.layout.dialog_microlecture_feedback_thank_you);
            dialogFeedbackFinal.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
            dialogFeedbackFinal.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
            dialogFeedbackFinal.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialogFeedbackFinal.setCancelable(true);

            dialogFeedbackFinal.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                        listener.onDismissed();
                        return true;
                    }
                    return false;
                }
            });

            LottieAnimationView lottie_feedback = dialogFeedbackFinal.findViewById(R.id.lottie_feedback);

            TextView tv_title = dialogFeedbackFinal.findViewById(R.id.tv_title);
            TextView tv_sub_title = dialogFeedbackFinal.findViewById(R.id.tv_sub_title);

            ImageView iv_close = dialogFeedbackFinal.findViewById(R.id.iv_close);
            final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.ding);

            int duration = (int)MyApplication.getFirebaseRemoteConfig().getLong("lecture_feedback_thank_you_popup_duration_in_ms");
            iv_close.setOnClickListener(imageView -> {
                dialogFeedbackFinal.dismiss();
                listener.onDismissed();
            });

            dialogFeedbackFinal.setOnDismissListener(v->{
                lottie_feedback.cancelAnimation();
                lottie_feedback.clearAnimation();
                lottie_feedback.setProgress(0f);
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                }

                listener.onDismissed();
            });

            dialogFeedbackFinal.show();
            mediaPlayer.start();
            lottie_feedback.playAnimation();

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialogFeedbackFinal.dismiss();
                }
            }, duration);

        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    protected void firebaseLectureEvent(Boolean isLiked, String feedback, ArrayList<String> selectedFeedbacks) {
        Map<String, String> params = new HashMap<>();
        params.put("FeedbackComment", StringUtils.isEmpty(feedback)? "" : feedback);
        params.put("optionsSelected", selectedFeedbacks.toString());
        params.put("ChapterId", microLectureContentDto.getChapterId());
        params.put("ChapterName", microLectureContentDto.getChapterName());
        params.put("MicrolectureId", microLectureContentDto.getId());
        params.put("MicrolectureName", microLectureContentDto.getTitle());
        params.put("breakPosition", String.valueOf(microLectureContentDto.getVideoCompletionEndPos()));
        params.put("startPosition", String.valueOf(microLectureContentDto.getDefaultStartTime()));
        params.put("endPosition", String.valueOf(microLectureContentDto.getVideoCompletionEndPos()));
        params.put("SubjectName", getIntent().getStringExtra("selectedSubject"));
        if(isLiked!=null) {
            params.put("liked", String.valueOf(isLiked));
        }else {
            params.put("liked","");
        }
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.micro_lecture_liked.getActionName(), params);

        params.put("Completed", "true");
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.micro_lecture_completed.getActionName(), params);
        Log.d(TAG, "firebaseLectureEvent: "+params);
    }


    public Intent getRecognizerIntent() {
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        Locale aDefault = Locale.getDefault();
        Log.d("langDefault", aDefault.getDisplayCountry() + " == " + aDefault.getDisplayName());
        if ("hi-rIN".equals(lang) || "hi".equals(lang)) {
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN");
        } else if ("pa".equals(lang)) {
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pa-Guru-IN");
        } else {
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang + "-IN");
        }

        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS
                , MyApplication.getFirebaseRemoteConfig().getLong("EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS"));

        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                MyApplication.getFirebaseRemoteConfig().getLong("EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS"));


        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "arivihan.technologies.doubtbuzzter2");
        return speechRecognizerIntent;
    }

    @Override
    public void onAskButtonStateChanged(AskButtonState newState) {
        switch (newState) {
            case STATE_ASK -> {
                apiProcessingAnimation.pauseAnimation();
                apiProcessingAnimation.setVisibility(View.GONE);
                askButton.setVisibility(View.VISIBLE);
                askButton.setBackground(AppCompatResources.getDrawable(this, R.drawable.ask_back));
                askButton.setText(getString(R.string.Ask));
            }
            case STATE_CANCEL -> {
                askButton.setVisibility(View.VISIBLE);
                askButton.setBackground(AppCompatResources.getDrawable(this, R.drawable.red_ask_back));
                askButton.setText(getString(R.string.cancel));
            }
            case STATE_PAUSE -> {
                askButton.setVisibility(View.VISIBLE);
                askButton.setBackground(AppCompatResources.getDrawable(this, R.drawable.orange_ask_back));
                askButton.setText(getString(R.string.pause));
            }
            case STATE_API_CALLING -> {
                askButton.setVisibility(View.GONE);
                apiProcessingAnimation.setVisibility(View.VISIBLE);
                apiProcessingAnimation.playAnimation();
            }
            case STATE_RESUME -> {
                apiProcessingAnimation.pauseAnimation();
                apiProcessingAnimation.setVisibility(View.GONE);
                askButton.setVisibility(View.VISIBLE);
                askButton.setBackground(AppCompatResources.getDrawable(this, R.drawable.red_ask_back));
                askButton.setText(getString(R.string.resume));
            }
            case STATE_GO_BACK -> {
                askButton.setVisibility(View.VISIBLE);
                askButton.setBackground(AppCompatResources.getDrawable(this, R.drawable.ask_back));
                askButton.setText(getString(R.string.go_back));
            }
            default -> {
                apiProcessingAnimation.pauseAnimation();
                apiProcessingAnimation.setVisibility(View.GONE);
                askButton.setVisibility(View.VISIBLE);
                askButton.setBackground(AppCompatResources.getDrawable(this, R.drawable.ask_back));
            }
        }
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {
        Log.d(TAG, "onReadyForSpeech");
        listeningLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d(TAG, "onReadyForSpeech");

    }

    @Override
    public void onRmsChanged(float v) {

    }

    @Override
    public void onBufferReceived(byte[] bytes) {
    }

    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "onEndOfSpeech");
    }

    @Override
    public void onError(int i) {
        Log.d(TAG, "onError " + i);
    }

    @Override
    public void onResults(Bundle bundle) {
        listeningLayout.setVisibility(View.GONE);
    }

    @Override
    public void onPartialResults(Bundle bundle) {

    }

    @Override
    public void onEvent(int i, Bundle bundle) {
        Log.d(TAG, "onEvent");
    }

    @Override
    protected void onDestroy() {
        MyVolleySingleton.getInstance(this).getRequestQueue().cancelAll(TAG);
        if (confettiHandler != null) {
            confettiHandler.removeCallbacks(confettiRunnable);
        }
        reportPersonalisedPathProgress();
        super.onDestroy();
    }

    /**
     * Report this lecture's watch percentage to the personalised learning path.
     * No-op unless launched from the path (nodeId extra forwarded by the lecture list).
     */
    private void reportPersonalisedPathProgress() {
        try {
            long duration = microLectureContentDto != null ? microLectureContentDto.getVideoCompletionEndPos() : 0;
            arivihan.technologies.doubtbuzzter2.helper.PersonalisedPathProgressManager
                    .reportVideoFromActivity(this, lastUpdatedPosition, duration);
        } catch (Exception ignored) {
        }
    }


    public void playCelebrationAnimation() {
        successCelebration.setVisibility(View.VISIBLE);
        celebration1.setVisibility(View.VISIBLE);
        celebration1.playAnimation();
        celebration2.setVisibility(View.VISIBLE);
        celebration2.playAnimation();
        celebration3.setVisibility(View.VISIBLE);
        celebration3.playAnimation();

        if (confettiSound != null) {
            confettiSound.start();
        }

        confettiRunnable = () -> {
            celebration1.setVisibility(View.GONE);
            celebration1.cancelAnimation();
            celebration2.setVisibility(View.GONE);
            celebration2.cancelAnimation();
            celebration3.setVisibility(View.GONE);
            celebration3.cancelAnimation();
        };

        confettiHandler.postDelayed(confettiRunnable, 1300);
    }

    public void askDoubtAPI(String userQuestion, String microLectureId, String subject) {
        final DoubtInfoDto doubtInfoDto = new DoubtInfoDto();
        doubtInfoDto.setMicroLectureId(microLectureId);
        doubtInfoDto.setUserQuestion(userQuestion);
        Map<String, Object> map = new HashMap<>();
        map.put("callServer", true);
        map.put("version2", true);
        map.put("googleResults2", true);
        map.put("selectedSubject", subject == null ? "Physics" : subject);
        Log.d(TAG, "askDoubtSuccessCallback calling");

        MyJsonObjectRequest myJsonObjectRequest = null;
        try {
            myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                    CommonMethods.getUrlParam(GET_DOUBT_INFO, map), new JSONObject(gson.toJson(doubtInfoDto)), response -> {
                TypeToken<DoubtResponseDto> token = new TypeToken<>() {
                };
                DoubtResponseDto doubtResponseDto = gson.fromJson(response.toString(), token.getType());
                Log.d(TAG, "askDoubtSuccessCallback success");
                askDoubtSuccessCallback(doubtResponseDto, userQuestion);
            }, new VolleyGlobalErrorListener(VideoPlayerHelperActivity.this) {
                @Override
                public void postError(VolleyError error) {
                    Log.d(TAG, "askDoubtSuccessCallback failure");

                    askDoubtFailureCallback(error.getMessage(), "");
                }
            });
            myJsonObjectRequest.setTag(TAG);
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }

    public void contextualizedDoubtApi(String chapterId, long currentPosition, String userQuestion, String microLectureId, String subject) {
        onLectureDoubtTriggered(userQuestion, chapterId, microLectureId, subject);
    }

    protected abstract void onLectureDoubtTriggered(String userQuestion, String chapterId, String microLectureId, String subject);

    protected abstract void askDoubtContextSuccessCallback(DoubtContextDto doubtContextDto, String userQuestion);

    protected abstract void askDoubtContextFailerCallback();

    public abstract void askDoubtSuccessCallback(DoubtResponseDto doubtResponseDto, String question);

    public abstract void askDoubtFailureCallback(String error, String userQuestion);

    public void snackBarAPI(String microLectureId) {
        final Map<String, Object> map = new HashMap<>();
        map.put("microLectureId", microLectureId);
        map.put("newApk", true);
        MyJsonArrayRequest myJsonObjectRequest = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(SNACKBAR_LIST, map), null,
                response -> {
                    TypeToken<List<SnackBarDto>> token = new TypeToken<>() {
                    };
                    List<SnackBarDto> snackBarDtoList = gson.fromJson(response.toString(),
                            token.getType());
                    setCroutonInfoInUI(snackBarDtoList);

                }, new VolleyGlobalErrorListener(VideoPlayerHelperActivity.this) {
            @Override
            public void postError(VolleyError error) {

            }
        });
        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
    }

    public abstract void setCroutonInfoInUI(List<SnackBarDto> snackBarDtoList);

    private void saveResponseDataAPI(UserFeedback userFeedback) {
        try {
            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                    SEND_FEEDBACK, new JSONObject(gson.toJson(userFeedback)), null, null);
            myJsonObjectRequest.setTag(TAG);
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
            MyApplication.saveData(microLectureContentDto.getId(), true);
            checkContinuity();
        } catch (JSONException e) {
            if (getIntent().getBooleanExtra("replay", false))
                getOnBackPressedDispatcher().onBackPressed();

        }
    }

    public void saveVideoProgress(long position, boolean board) {
        if (lastUpdatedPosition == position) {
            return;
        }

        if (microLectureContentDto == null) {
            return;
        }

        final Map<String, Object> map = new HashMap<>();
        map.put("chapterId", microLectureContentDto.getChapterId());
        map.put(PreferenceKeys.MICROLECTURE_ID.getKey(), microLectureContentDto.getId());
        map.put(PreferenceKeys.USER_ID.getKey(), MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
        map.put("progress", position);
        map.put(PreferenceKeys.BOARD.getKey(), board);
        Log.d("videoProgressRequest", "ChapterId : " +
                microLectureContentDto.getChapterId() + " microlectureId : " + microLectureContentDto.getId());
        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(UPDATE_VIDEO_PROGRESS, map), null,
                response -> {
                    lastUpdatedPosition = position;
                }, new VolleyGlobalErrorListener(VideoPlayerHelperActivity.this) {
            @Override
            public void postError(VolleyError error) {
                Log.e(TAG, "error in saving video progress " + error.getMessage());
            }
        });
        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
    }

    public void getInteractiveQuestion(String microLectureId) {
        final Map<String, Object> map = new HashMap<>();
        Log.d(TAG, "interactive api " + "microLectureId " + microLectureId);

        map.put("microLectureId", microLectureId);
        MyJsonArrayRequest myJsonObjectRequest = new MyJsonArrayRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_MICROLECTURE_QANS, map), null,
                response -> {
                    TypeToken<List<MicroLectureQuestionsAnsDto>> token = new TypeToken<>() {
                    };
                    List<MicroLectureQuestionsAnsDto> questionsAnsList = gson.fromJson(response.toString(),
                            token.getType());
                    Log.d(TAG, "interactive question list size " + questionsAnsList);
                    interactiveApiSuccessCallback(questionsAnsList);

                }, new VolleyGlobalErrorListener(VideoPlayerHelperActivity.this) {
            @Override
            public void postError(VolleyError error) {
                Log.e(TAG, "error in api getInteractiveQuestion");
            }
        });
        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);
    }

    public abstract void interactiveApiSuccessCallback(List<MicroLectureQuestionsAnsDto> questionsAnsList);

    @OptIn(markerClass = UnstableApi.class)
    public void validateInteractiveAnswerAPI(String userAnswer, String microLectureQId, CustomTimeBar customTimeBar) {

        Map<String, Object> map = new HashMap<>();
        map.put("microLectureQId", microLectureQId);
        map.put("userAnswer", userAnswer.trim());
        map.put("sessionId" , UUID.randomUUID().toString());
        Log.d("InteractiveScenario", "validateInteractiveAnswerAPI "+map);

        MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(VALIDATE_ANSWER, map), null,
                response -> {
                    TypeToken<MicroLectureQuestionAnsResponseDto> token = new TypeToken<>() {
                    };
                    MicroLectureQuestionAnsResponseDto dto =
                            gson.fromJson(response.toString(), token.getType());
                    Log.d("InteractiveScenario", "response " + dto);
                    changeButtonState(AskButtonState.STATE_ASK);
                    if (dto.isFound()) {
                        validateAPIResponse(dto.getSeekTo(), dto.getTrackerCompletionEndPos(), dto.getFallbackPos(),
                                dto.isCorrectMatched(), userAnswer, microLectureQId, true, userAnswer);
                    } else {
                        if (!isDialogShowing) {
//                            if(StringUtils.isNotEmpty(dto.getQuestionText())) {
                            stopVideo();
                            if (CollectionUtils.isEmpty(dto.getConfirmOptionList())) {
                                return;
                            }
                            animateMarkerIconToCenter(() -> {
                                buildSingleChoiceV2(dto.getQuestionText(), dto.getQuestionImage(), dto.getConfirmOptionList(), 1, userAnswer, microLectureQId);
                            }, customTimeBar.getActiveMarkerScreenPosition());
//                            } else {
//                                buildSingleChoice(dto.getConfirmOptionList(), 1, userAnswer, microLectureQId);
//                            }
                        }
                    }
                }, new VolleyGlobalErrorListener(VideoPlayerHelperActivity.this) {
            @Override
            public void postError(VolleyError error) {
                Log.d("InteractiveScenario", "postError " + error.getMessage());
                changeButtonState(AskButtonState.STATE_ASK);
            }
        });
        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest);

    }

    public abstract void validateAPIResponse(long seekToPosition, Integer endPosition, Integer fallbackPosition,
                                             boolean correct, String userAnswer, String microLectureQId,
                                             boolean speech, String clickAnswer);

    public void buildSingleChoice(List<ConfirmOption> confirmOptionList, int index, String userAnswer, String microLectureQId) {
        stopVideo();
        Log.d(TAG, "buildSingleChoice: ");
        if (CollectionUtils.isEmpty(confirmOptionList)) {
            return;
        }

        Collections.shuffle(confirmOptionList);

        Dialog optionDialog = new Dialog(this);
        optionDialog.setContentView(R.layout.video_player_options_layout);
        if(optionDialog.getWindow() != null) {
            optionDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
            optionDialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(this,
                    R.drawable.bachground_custom_dialogu_typeactivity));
            optionDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        optionDialog.setCancelable(false);
        TextView optionA = optionDialog.findViewById(R.id.optionA);
        TextView optionB = optionDialog.findViewById(R.id.optionB);
        TextView optionC = optionDialog.findViewById(R.id.optionC);
        TextView optionD = optionDialog.findViewById(R.id.optionD);
        TextView alertTitle = optionDialog.findViewById(R.id.alert_title);

        if (index == 0) {
            alertTitle.setText(R.string.here_are_option);
        }
        optionA.setVisibility(View.GONE);
        optionB.setVisibility(View.GONE);
        optionC.setVisibility(View.GONE);
        optionD.setVisibility(View.GONE);


        if (!CollectionUtils.isEmpty(confirmOptionList)) {
            optionA.setText(confirmOptionList.get(0).getText());
            if (StringUtils.isNotEmpty(optionA.getText())) {
                optionA.setVisibility(View.VISIBLE);
            }

            if (confirmOptionList.size() > 1) {
                optionB.setText(confirmOptionList.get(1).getText());
                if (StringUtils.isNotEmpty(optionB.getText())) {
                    optionB.setVisibility(View.VISIBLE);
                }
            }
            if (confirmOptionList.size() > 2) {
                optionC.setText(confirmOptionList.get(2).getText());
                if (StringUtils.isNotEmpty(optionC.getText())) {
                    optionC.setVisibility(View.VISIBLE);
                }
            }
            if (confirmOptionList.size() > 3) {
                optionD.setText(confirmOptionList.get(3).getText());
                if (StringUtils.isNotEmpty(optionD.getText())) {
                    optionD.setVisibility(View.VISIBLE);
                }
            }
        }

        optionA.setOnClickListener(v -> {
            if (!CollectionUtils.isEmpty(confirmOptionList)) {
                ConfirmOption confirmOption = confirmOptionList.get(0);
                validateAPIResponse(confirmOption.getSeekTo(),
                        confirmOption.getTrackerCompletionEndPos(),
                        confirmOption.getFallbackPos(),
                        confirmOption.isCorrect(), userAnswer, microLectureQId, false, optionA.getText().toString());
                optionDialog.dismiss();
                isDialogShowing = false;
            }
        });
        optionB.setOnClickListener(v -> {
            if (confirmOptionList.size() > 1) {
                ConfirmOption confirmOption = confirmOptionList.get(1);
                validateAPIResponse(confirmOption.getSeekTo(),
                        confirmOption.getTrackerCompletionEndPos(),
                        confirmOption.getFallbackPos(),
                        confirmOption.isCorrect(), userAnswer, microLectureQId, false, optionA.getText().toString());
                optionDialog.dismiss();
            }
        });
        optionC.setOnClickListener(v -> {
            if (confirmOptionList.size() > 2) {
                ConfirmOption confirmOption = confirmOptionList.get(2);
                validateAPIResponse(confirmOption.getSeekTo(),
                        confirmOption.getTrackerCompletionEndPos(),
                        confirmOption.getFallbackPos(),
                        confirmOption.isCorrect(), userAnswer, microLectureQId, false, optionA.getText().toString());
                optionDialog.dismiss();
            }
        });
        optionD.setOnClickListener(v -> {
            if (confirmOptionList.size() > 3) {
                ConfirmOption confirmOption = confirmOptionList.get(3);
                validateAPIResponse(confirmOption.getSeekTo(),
                        confirmOption.getTrackerCompletionEndPos(),
                        confirmOption.getFallbackPos(),
                        confirmOption.isCorrect(), userAnswer, microLectureQId, false, optionA.getText().toString());
                optionDialog.dismiss();
                isDialogShowing = false;
            }
        });

        if (!isFinishing()) {
            optionDialog.show();

            //option dialog open instance
            if (onUiChangeListener != null) {
                onUiChangeListener.onOptionDialogShow();
            }
        }
    }

    public void buildSingleChoiceV2(String questionText,
                                    String questionImage,
                                    List<ConfirmOption> confirmOptionList,
                                    int index,
                                    String userAnswer,
                                    String microLectureQId) {

//        stopVideo();
//
//        if (CollectionUtils.isEmpty(confirmOptionList)) return;

        if(StringUtils.isEmpty(questionText)) {
            Collections.shuffle(confirmOptionList);
        }

        Dialog optionDialog = new Dialog(this);
        optionDialog.setContentView(R.layout.video_player_options_layout_v2);
        if(optionDialog.getWindow() != null) {
            optionDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
            optionDialog.getWindow().setBackgroundDrawable(
                    AppCompatResources.getDrawable(this, R.drawable.bachground_custom_dialogu_typeactivity));
            optionDialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        optionDialog.setCancelable(false);

        TextView tv_subtitle = optionDialog.findViewById(R.id.tv_subtitle);
//        ImageView iv_question_image = optionDialog.findViewById(R.id.iv_question_image);

        tv_subtitle.setText(
                StringUtils.isNotEmpty(questionText)
                        ? questionText
                        : getString(R.string.please_sahi_option_select_karein)
        );

        MaterialCardView[] cardViews = {
                optionDialog.findViewById(R.id.cv_option_a),
                optionDialog.findViewById(R.id.cv_option_b),
                optionDialog.findViewById(R.id.cv_option_c),
                optionDialog.findViewById(R.id.cv_option_d)
        };

        TextView[] textViews = {
                optionDialog.findViewById(R.id.tv_option_a_text),
                optionDialog.findViewById(R.id.tv_option_b_text),
                optionDialog.findViewById(R.id.tv_option_c_text),
                optionDialog.findViewById(R.id.tv_option_d_text)
        };

        TextView[] textOptionViews = {
                optionDialog.findViewById(R.id.tv_option_a),
                optionDialog.findViewById(R.id.tv_option_b),
                optionDialog.findViewById(R.id.tv_option_c),
                optionDialog.findViewById(R.id.tv_option_d)
        };

        ImageView[] imageViews = {
                optionDialog.findViewById(R.id.iv_option_a),
                optionDialog.findViewById(R.id.iv_option_b),
                optionDialog.findViewById(R.id.iv_option_c),
                optionDialog.findViewById(R.id.iv_option_d)
        };

        if(StringUtils.isEmpty(questionText)) {
            for(TextView textView : textOptionViews) {
                textView.setVisibility(View.GONE);
            }
        }

//        iv_question_image.setVisibility(StringUtils.isNotEmpty(questionImage)? View.VISIBLE : View.GONE);
//        if(StringUtils.isNotEmpty(questionImage)) {
//            Glide.with(this).load(questionImage).into(iv_question_image);
//        }

        // Hide all initially
        for (MaterialCardView card : cardViews) {
            card.setVisibility(View.GONE);
        }

        for (int i = 0; i < confirmOptionList.size() && i < cardViews.length; i++) {

            ConfirmOption confirmOption = confirmOptionList.get(i);
            String text = confirmOption.getText();

            if (StringUtils.isNotEmpty(text)) {
                textViews[i].setText(text);
                cardViews[i].setVisibility(View.VISIBLE);

                final int position = i;

                cardViews[i].setOnClickListener(v -> {

                    resetAllIcons(imageViews);

                    imageViews[position].setImageResource(
                            R.drawable.img_video_options_selected);

                    validateAPIResponse(
                            confirmOption.getSeekTo(),
                            confirmOption.getTrackerCompletionEndPos(),
                            confirmOption.getFallbackPos(),
                            confirmOption.isCorrect(),
                            userAnswer,
                            microLectureQId,
                            false,
                            textViews[position].getText().toString()
                    );

                    optionDialog.dismiss();
                });
            }
        }


        optionDialog.setOnDismissListener(v -> isDialogShowing = false);

        if (!isFinishing()) {
            isDialogShowing = true;
            optionDialog.show();

            optionDialog.getWindow().getDecorView().post(() -> {
                matchRowHeight(cardViews[0], cardViews[1]);
                matchRowHeight(cardViews[2], cardViews[3]);
            });

            //option dialog open instance
            if (onUiChangeListener != null) {
                onUiChangeListener.onOptionDialogShow();
            }
        }
    }

    private void matchRowHeight(View view1, View view2) {

        if (view1.getVisibility() != View.VISIBLE && view2.getVisibility() != View.VISIBLE)
            return;

        int h1 = view1.getVisibility() == View.VISIBLE ? view1.getHeight() : 0;
        int h2 = view2.getVisibility() == View.VISIBLE ? view2.getHeight() : 0;

        int max = Math.max(h1, h2);

        if (view1.getVisibility() == View.VISIBLE) {
            view1.getLayoutParams().height = max;
            view1.requestLayout();
        }

        if (view2.getVisibility() == View.VISIBLE) {
            view2.getLayoutParams().height = max;
            view2.requestLayout();
        }
    }

    private void resetAllIcons(ImageView[] imageViews) {
        for (ImageView imageView : imageViews) {
            imageView.setImageResource(R.drawable.img_video_options_unselected);
        }
    }

    private void animateMarkerIconToCenter(Runnable onAnimationEnd, int[] markerPos) {
//        int[] markerPos = customTimeBar.getActiveMarkerScreenPosition();
        if (markerPos == null) return;

        int markerScreenX = markerPos[0];
        int markerScreenY = markerPos[1];
        int iconSizePx    = markerPos[2];

        iv_active_interactive_question.setImageDrawable(ContextCompat.getDrawable(this,
                R.drawable.img_interactivity_question_answer_active));

        ViewGroup rootView = (ViewGroup) getWindow().getDecorView();

        int[] rootLocation = new int[2];
        rootView.getLocationOnScreen(rootLocation);

        float startX = markerScreenX - rootLocation[0] - iconSizePx / 2f;
        float startY = markerScreenY - rootLocation[1] - iconSizePx / 2f;

        iv_active_interactive_question.setVisibility(View.VISIBLE);
        iv_active_interactive_question.setScaleX(1f);
        iv_active_interactive_question.setScaleY(1f);
        iv_active_interactive_question.setX(startX);
        iv_active_interactive_question.setY(startY);

        ViewGroup.LayoutParams lp = iv_active_interactive_question.getLayoutParams();
        lp.width  = iconSizePx;
        lp.height = iconSizePx;
        iv_active_interactive_question.setLayoutParams(lp);

        float screenCenterX = rootView.getWidth()  / 2f - iconSizePx / 2f;
        float screenCenterY = rootView.getHeight() / 2f - iconSizePx / 2f;

        iv_active_interactive_question.animate()
                .x(screenCenterX)
                .y(screenCenterY)
                .scaleX(8f)
                .scaleY(8f)
                .setDuration(700)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    iv_active_interactive_question.setVisibility(View.GONE);
                    iv_active_interactive_question.setScaleX(1f);
                    iv_active_interactive_question.setScaleY(1f);

                    onAnimationEnd.run();
                })
                .start();
    }

    public void setUpContinuityLayout() {
        Log.d(TAG, "Initializing setUpContinuityLayout");

        currentMicroLecturePos = getIntent().getIntExtra("currentMicroLecturePos", 0);
        Log.d(TAG, "Retrieved currentMicroLecturePos: " + currentMicroLecturePos);

        list = MyApplication.getMicroLectureList(getString(R.string.micro_lecture_and_practice_list));
        Log.d(TAG, "Fetched micro lecture list of size: " + list.size());

        // Initializing views
        continuityLayout = findViewById(R.id.continuity_layout);
        closeContinuityLayout = findViewById(R.id.close_continuity_layout);
        progressionLayout = findViewById(R.id.progression_layout);
        progressionText = findViewById(R.id.progression_text);
        continuityPlayLayout = findViewById(R.id.play_layout);
        circularProgressIndicator = findViewById(R.id.continuity_timer);
        skipToNextLayout = findViewById(R.id.skip_to_next_layout);
        skipToNextText = findViewById(R.id.skip_to_next_text);

        continuityPlayLayout.setOnClickListener(v->{
            if(progressionDto!=null) {
                continuityHandler.removeCallbacksAndMessages(null);
                progressionLayout.performClick();
            }
        });

        closeContinuityLayout.setOnClickListener(view -> {
            Log.d(TAG, "Close button clicked");
            getOnBackPressedDispatcher().onBackPressed();
        });

        calculateMicroLecturePos = currentMicroLecturePos;

        progressionDto = practiceProgression();
        Log.d(TAG, "Retrieved progressionDto: " + progressionDto);

        progressionIsPractice = calculateStatus;
        progressionMicroLecturePos = calculateMicroLecturePos;
        Log.d(TAG, "progressionIsPractice: " + progressionIsPractice);
        Log.d(TAG, "progressionMicroLecturePos: " + progressionMicroLecturePos);

        if (progressionDto != null) {
            if (progressionIsPractice) {
                practicePosInProgression = calculatePracticePos;
                Log.d(TAG, "practicePosInProgression: " + practicePosInProgression);
                String type = LectureContentType.getLectureContentType(
                        progressionDto.getQuestions().get(practicePosInProgression).getType()).getType();
                Log.d(TAG, "Setting progressionText to practice type: " + type);
                progressionText.setText(type);
            } else {
                Log.d(TAG, "Setting progressionText to lecture title: " + progressionDto.getTitle());
                progressionText.setText(progressionDto.getTitle());
            }
        }

        if (progressionIsPractice) {
            Log.d(TAG, "Fetching skipDto from videoProgression()");
            skipDto = videoProgression();
            skipMicroLecturePos = calculateMicroLecturePos;
            skipToPractice = calculateStatus;
        } else {
            skipToPractice = true;
            while (!skipToPractice && skipMicroLecturePos < list.size()) {
                Log.d(TAG, "Trying to find next practice in loop...");
                skipDto = practiceProgression();
                if (skipDto == null) {
                    Log.d(TAG, "No further skipDto found, breaking loop");
                    break;
                }
                skipMicroLecturePos = calculateMicroLecturePos;
                skipToPractice = calculateStatus;
            }
        }

        if (skipDto != null) {
            Log.d(TAG, "skipDto found. skipToPractice: " + skipToPractice + ", skipMicroLecturePos: " + skipMicroLecturePos);
            if (skipToPractice) {
                practicePosInSkip = calculatePracticePos;
                String type = LectureContentType.getLectureContentType(
                        skipDto.getQuestions().get(practicePosInSkip).getType()).getType();
                Log.d(TAG, "Setting skipToNextText to practice type: " + type);
                skipToNextText.setText(String.format(getString(R.string.Skip_to_Next_Practice), type));
            } else {
                Log.d(TAG, "Setting skipToNextText to next lecture");
                skipToNextText.setText(String.format(getString(R.string.Skip_to_Next_Lecture)));
            }
        } else {
            Log.d(TAG, "skipDto is null. Hiding skipToNextLayout.");
            skipToNextLayout.setVisibility(View.GONE);
        }

        Log.d(TAG, "Final Values - skipMicroLecturePos: " + skipMicroLecturePos +
                ", practicePosInSkip: " + practicePosInSkip + ", skipToPractice: " + skipToPractice);

        progressionLayout.setOnClickListener(view -> {
            Log.d(TAG, "progressionLayout clicked");
            if(progressionDto==null)
                return;
            if (progressionDto.isLocked()) {
                Log.d(TAG, "progressionDto is locked with message: " + progressionDto.getLockMessage());
                MyApplication.saveData(getString(R.string.progressionIsLocked), true);
                MyApplication.saveData(getString(R.string.progressionLockedMessage), progressionDto.getLockMessage());
                getOnBackPressedDispatcher().onBackPressed();
                getOnBackPressedDispatcher().onBackPressed();
            }
            else {
                if (progressionIsPractice) {
                    Log.d(TAG, "Launching getQuestionList for progression practice");
                    Log.d(TAG, "practicePosInProgression " + practicePosInProgression);
                    Log.d(TAG, "progressionMicroLecturePos " + progressionMicroLecturePos);


                    for (Questions question : progressionDto.getQuestions()) {
                        if (question.isSkip()) {
                            continue;
                        } else {
                            getQuestionList(progressionDto.getCode(),
                                    question.getType(),
                                    progressionMicroLecturePos,
                                    practicePosInProgression);
                            break;
                        }
                    }


                } else {
                    Log.d(TAG, "Starting video for progression lecture");
                    startVideo(progressionDto, progressionMicroLecturePos);
                }
            }
        });

        skipToNextLayout.setOnClickListener(view -> {
            if(skipDto==null)
                return;
            if (skipDto.isLocked()) {
                Log.d(TAG, "skipDto is locked with message: " + skipDto.getLockMessage());
                MyApplication.saveData(getString(R.string.progressionIsLocked), true);
                MyApplication.saveData(getString(R.string.progressionLockedMessage), skipDto.getLockMessage());
                getOnBackPressedDispatcher().onBackPressed();
                getOnBackPressedDispatcher().onBackPressed();
            } else {
                if (skipToPractice) {
                    getQuestionList(skipDto.getCode(),
                            skipDto.getQuestions().get(practicePosInSkip).getType(),
                            skipMicroLecturePos,
                            practicePosInSkip);
                } else {
                    Log.d(TAG, "setUpContinuityLayout: "+skipMicroLecturePos);
                    startVideo(skipDto, skipMicroLecturePos);
                }
            }
        });

        Log.d(TAG, "setUpContinuityLayout initialization complete.");

    }

    protected void checkContinuity() {
//        if (getIntent().getBooleanExtra("replay", false) || progressionDto == null) {
//            Log.d("checkContinuity", "replay = " + progressionDto);
//            Log.d("checkContinuity", "progressionDto = " + progressionDto);
//
//            super.onBackPressed();
//        } else {
//            hideControlView();
//            showDialog = false;
//            dialogReview.dismiss();
//            continuityLayout.setVisibility(View.VISIBLE);
//            startProgress();
//        }

        hideControlView();
        showDialog = false;
        if(dialogReview!=null && dialogReview.isShowing()) {
            dialogReview.dismiss();
        }
        continuityLayout.setVisibility(View.VISIBLE);
        startProgress();
    }

    private void startProgress() {
        final long startTime = System.currentTimeMillis();
        continuityHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                long elapsedTime = System.currentTimeMillis() - startTime;
                circularProgress = (int) ((elapsedTime * 100) / totalDuration);
                circularProgressIndicator.setProgress(circularProgress);
                if (elapsedTime < totalDuration) {
                    continuityHandler.postDelayed(this, stepDuration);
                } else {
                    continuityHandler.removeCallbacksAndMessages(null);
                    progressionLayout.performClick();
                }
            }
        }, stepDuration);
    }

    private MicroLectureDto practiceProgression() {
        if (!CollectionUtils.isEmpty(list) && list.get(calculateMicroLecturePos).getQuestions().size() > calculatePracticePos) {
            for (int i = calculatePracticePos; i < list.get(calculateMicroLecturePos).getQuestions().size(); i++) {
                Questions questions = list.get(calculateMicroLecturePos).getQuestions().get(i);
                if (questions.getStatus().equalsIgnoreCase(PracticeStatus.PARTIAL_COMPLETED.name()) ||
                        questions.getStatus().equalsIgnoreCase(PracticeStatus.TO_DO.name())) {
                    calculateStatus = true;
                    calculatePracticePos = i;
                    return list.get(calculateMicroLecturePos);
                }
            }
            if (!calculateStatus)
                return videoProgression();
        }
        return videoProgression();
    }

    private MicroLectureDto videoProgression() {
        if (!CollectionUtils.isEmpty(list) && list.size() > calculateMicroLecturePos + 1) {
            for (int i = calculateMicroLecturePos + 1; i < list.size(); i++) {
                calculateMicroLecturePos = i;
                if (!list.get(calculateMicroLecturePos).isCompleted()) {
                    if (list.get(calculateMicroLecturePos).getVideoDetails().getProgress() == 100) {
                        calculatePracticePos = 0;
                        return practiceProgression();
                    } else {
                        calculateStatus = false;
                    }
                    return list.get(calculateMicroLecturePos);
                }
            }
        }
        return null;
    }

    private void getQuestionList(String microLectureCode, String type, int pos, int practicePos) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("microLectureCode", microLectureCode);
            map.put("type", type);
            map.put("boldFormatting", true);
            MyJsonArrayRequest myJsonArrayRequest = new MyJsonArrayRequest(Request.Method.GET,
                    CommonMethods.getUrlParam(MICRO_LECTURE_PRACTICE_LIST_BY_TYPE, map), null,
                    response -> {
                        TypeToken<List<FactoryShipQnADto>> token = new TypeToken<List<FactoryShipQnADto>>() {
                        };
                        qnADtoList.clear();
                        qnADtoList.addAll(gson.fromJson(response.toString(), token.getType()));
                        setUpQuestion(pos, practicePos,type);
                    }, new VolleyGlobalErrorListener(this) {
                @Override
                public void postError(VolleyError error) {
                    Log.e("postError: ", error.getMessage());
                }
            });
            myJsonArrayRequest.setTag(TAG);
            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonArrayRequest);
        } catch (Exception e) {
            Log.e("ErrorQuestionList", e.getMessage(), e);
        }
    }

    private void setUpQuestion(int pos, int practicePos, String type) {
        if (!qnADtoList.isEmpty()) {
            firstUnattemptedIndex = 0;
            for (int i = 0; i < qnADtoList.size(); i++) {
                if (!qnADtoList.get(i).isCompleted()) {
                    firstUnattemptedIndex = i;
                    break;
                }
            }
            openPracticeQuestions(pos, practicePos,type);
        }
    }

    public void startVideo(MicroLectureDto microLectureDto, int pos) {
        if (microLectureDto.isLocked()) {
            getOnBackPressedDispatcher().onBackPressed();
        } else {
            updateSelectionLocally(true, false,microLectureDto.getId(),LectureContentType.VIDEO.name());
            VideoDetails videoDetails = microLectureDto.getVideoDetails();
            playVideo(videoDetails, microLectureDto, getIntent().getBooleanExtra("isBoard", false), pos);
        }
    }


    private void updateSelectionLocally(boolean isLecture, boolean isPractice, String mlId, String lectureContentType) {
        if (StringUtils.isNotEmpty(chapterId)) {
            MicroLectureEntity selectionMicroLectureEntity = new MicroLectureEntity(chapterId, mlId,
                    MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()),lectureContentType,isLecture,isPractice);
            MyApplication.getInstance().getMicroLectureViewModel().insert(selectionMicroLectureEntity);

            MyApplication.saveData(PreferenceKeys.MICROLECTURE_DATA_RELOAD.getKey(), true);
        }
    }

    private void openPracticeQuestions(int pos, int practicePos, String practiceType) {
        try {
            updateSelectionLocally(false, true, list.get(progressionMicroLecturePos).getId(), practiceType);
            LectureContentType type = LectureContentType.valueOf(practiceType);
            if(MyApplication.isCompetitiveProfile() && MicrolectureListActivity.isAllowedLectureContentType(type)) {
                openWebViewPracticeQuestions(type, pos, practicePos);
                return;
            }

            Intent intent = new Intent(this, PracticeProblemsActivity.class);
            intent.putParcelableArrayListExtra("factoryShipQnADtoList", qnADtoList);
            intent.putExtra("microLectureId", list.get(progressionMicroLecturePos).getId());
            intent.putExtra("microLectureName", list.get(progressionMicroLecturePos).getTitle());
            intent.putExtra("chapterId", chapterId);
            intent.putExtra("review", false);
            intent.putExtra("reattempt", false);
            intent.putExtra("firstUnattemptedIndex", firstUnattemptedIndex);
            intent.putExtra("currentMicroLecturePos", pos);
            intent.putExtra("practicePosInCurrentMicroLecture", practicePos);
            intent.putExtra("subjectCode", getIntent().getStringExtra("subjectCode"));
            startActivity(intent);
            finish();
        }catch (Exception e){
            Log.e(TAG, "openPracticeQuestions: ",e);
        }
    }

    private void openWebViewPracticeQuestions(LectureContentType lectureContentType, int position, int practisePosition) {
        MyApplication.saveMicroLectureList(getString(R.string.micro_lecture_and_practice_list), list);
        Intent intent = new Intent(VideoPlayerHelperActivity.this, MicrolecturePractiseWebViewActivity.class);
        intent.putParcelableArrayListExtra("factoryShipQnADtoList", qnADtoList);
        intent.putExtra("microLectureId", list.get(progressionMicroLecturePos).getId());
        intent.putExtra("microLectureName", list.get(progressionMicroLecturePos).getTitle());
        intent.putExtra("chapterId", chapterId);
        intent.putExtra("review", false);
        intent.putExtra("reattempt", false);
        intent.putExtra("firstUnattemptedIndex", firstUnattemptedIndex);
        intent.putExtra("currentMicroLecturePos", position);
        intent.putExtra("practicePosInCurrentMicroLecture", practisePosition);
        intent.putExtra("subjectCode", getIntent().getStringExtra("subjectCode"));
        intent.putExtra("selectedSubject", "");
        intent.putExtra("isBoard", false);
        intent.putExtra("type", lectureContentType!=null?lectureContentType.name():"");
        startActivity(intent);
    }

    private void playVideo(VideoDetails videoDetails, MicroLectureDto microLectureDto, boolean board, int pos) {

        @OptIn(markerClass = UnstableApi.class) Intent intent = new Intent(this, VideoPlayerActivity.class);
        MicroLectureContentDto microLectureContentDto = new MicroLectureContentDto();
        microLectureContentDto.setDefaultStartTime(Math.toIntExact(videoDetails.getStartPos()));
        microLectureContentDto.setVideoCompletionEndPos(Math.toIntExact(videoDetails.getEndPos()));
        microLectureContentDto.setVideoCompletionStartPos(Math.toIntExact(videoDetails.getEndPos()));
        microLectureContentDto.setContentUrl(videoDetails.getUrl());
        microLectureContentDto.setVideoUrl(videoDetails.getUrl());
        microLectureContentDto.setId(microLectureDto.getCode());
        microLectureContentDto.setChapterId(chapterId);
        microLectureContentDto.setResumePosition(videoDetails.getResumePosition());
        microLectureContentDto.setLectureContentType(LectureContentType.VIDEO);
        microLectureContentDto.setVideoQualityDtos(videoDetails.getQualityDtos());
        microLectureContentDto.setLanguageAndVideoUrlDtos(videoDetails.getLanguageAndVideoUrlDtos());
        microLectureContentDto.setLikeCount(microLectureDto.getLikeCount());
        microLectureContentDto.setLiked(microLectureDto.getLiked());
        microLectureContentDto.setLikeDislikeVisible(this.microLectureContentDto.isLikeDislikeVisible());

        intent.putExtra(PreferenceKeys.BOARD.getKey(), board);
        intent.putExtra("resumePosition", microLectureContentDto.getResumePosition());
        intent.putExtra(getResources().getString(R.string.passed_obj), microLectureContentDto);
        intent.putExtra("position", Integer.parseInt(String.valueOf(videoDetails.getResumePosition())));
        intent.putExtra("shouldNotResume", !videoDetails.isResume());
        intent.putExtra("startAtPosition", true);
        intent.putExtra("isAskDoubtActivity", false);
        intent.putExtra("video_uri", videoDetails.getUrl());
        intent.putExtra(PreferenceKeys.VIDEO_STARTING.getKey(), videoDetails.getStartPos());
        intent.putExtra("video_ending", videoDetails.getEndPos());
        intent.putExtra("chapterId", chapterId);
        intent.putExtra("subjectCode", getIntent().getStringExtra("subjectCode"));
        intent.putExtra("selectedSubject", getIntent().getStringExtra("selectedSubject"));
        intent.putExtra("selected_chapter_id", microLectureDto.getCode());
        intent.putExtra("replay", microLectureDto.getVideoDetails().getProgress() == 100);
        intent.putExtra("currentMicroLecturePos", pos);
        MyApplication.saveData(PreferenceKeys.MICROLECTURE_DATA_RELOAD.getKey(), true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public abstract void changeButtonState(AskButtonState askButtonState);

    @Override
    protected void onPause() {
        super.onPause();
        if (continuityHandler != null)
            continuityHandler.removeCallbacksAndMessages(null);
        if(dialogFeedbackInitial!=null && dialogFeedbackInitial.isShowing())
            dialogFeedbackInitial.dismiss();
        if(dialogFeedbackDescriptive!=null && dialogFeedbackDescriptive.isShowing())
            dialogFeedbackDescriptive.dismiss();
        if(dialogFeedbackFinal!=null && dialogFeedbackFinal.isShowing())
            dialogFeedbackFinal.dismiss();
    }

    protected abstract void showFeedback(boolean isBackPressed);
    public abstract void hideControlView();

    public abstract void startVideo();

    public abstract void stopVideo();


    public interface OnUiChangeListener {
        void onOptionDialogShow();
    }


}
