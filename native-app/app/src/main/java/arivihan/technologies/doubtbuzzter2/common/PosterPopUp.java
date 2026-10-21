package arivihan.technologies.doubtbuzzter2.common;


import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.helper.ActivityLauncher;
import arivihan.technologies.doubtbuzzter2.customView.RegisterCardView;
import lombok.Setter;

public class PosterPopUp extends Dialog {

    private static final String TAG = PosterPopUp.class.getSimpleName();
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1000;
    private static long COUNTDOWN_IN_MILLIS = 4000;

    private CardView cancel;
    private LinearProgressIndicator progressIndicator;
    private LottieAnimationView confettiAnimation1, confettiAnimation2;
    private MediaPlayer mediaPlayer;
    private ImageView posterImage;
    private TextView tv_title_text;
    private Class<?> targetActivityClass;
    private ObjectAnimator progressAnimator;
    @Setter
    OnDismissListener onDismissListener;

    @Setter
    PosterClickListener posterClickListener;
    private final java.util.Map<String, String> intentExtras = new java.util.HashMap<>();

    public PosterPopUp(Context context) {
        super(context);
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.poster_pop_up);
            if (getWindow() != null) {
                getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
            setCancelable(false);
            initializeViews();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing PosterPopUp", e);
        }
    }

    private void initializeViews() {
        try {
            cancel = findViewById(R.id.cancel_poster_popup);
            confettiAnimation1 = findViewById(R.id.confetti_1);
            tv_title_text=findViewById(R.id.tv_title_text);
            confettiAnimation2 = findViewById(R.id.confetti_2);
            progressIndicator = findViewById(R.id.progress_indicator);
            posterImage = findViewById(R.id.poster_image);

            if (progressIndicator != null) {
                progressIndicator.setMax((int) COUNTDOWN_IN_MILLIS);
                progressIndicator.setProgress(0);
            }

            if (cancel != null) {
                cancel.setOnClickListener(v -> {

                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.poster_popup_cancel_btn_click.getActionName());

                    stopProgressAnimation();
                    dismiss();
                });
            }

            if (posterImage != null) {
                posterImage.setOnClickListener(v -> {
                    if (isShowing()) {
                        if (targetActivityClass != null) {
                            try {
                                Intent intent = new Intent(getContext(), targetActivityClass);

                                MyApplication.logFirebaseBundleCreation(FirebaseEvents.poster_popup_click.getActionName());

                                for (Map.Entry<String, String> entry : intentExtras.entrySet()) {
                                    intent.putExtra(entry.getKey(), entry.getValue());
                                }
                                getContext().startActivity(intent);
                            } catch (Exception e) {
                                Log.e(TAG, "Error starting target activity", e);
//                                Toast.makeText(getContext(), "Failed to open activity", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if(posterClickListener!=null) {
                                posterClickListener.onCallBackClick();
                            }
//                            Toast.makeText(getContext(), "No target activity found", Toast.LENGTH_SHORT).show();
                        }
                        dismiss();
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
        }
    }

    public void showPosterPopUp(String bannerText,String posterUrl, long popUpTimer) {
        try {
            if (!isShowing()) {
                if (StringUtils.isNotEmpty(bannerText)) {
                    tv_title_text.setVisibility(View.VISIBLE);
                    Spanned formatted = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        formatted = Html.fromHtml(bannerText, Html.FROM_HTML_MODE_LEGACY);
                    }else {
                        formatted=Html.fromHtml(bannerText);
                    }
                    tv_title_text.setText(formatted);
                    posterImage.setOnClickListener(null);
                } else {
                    tv_title_text.setVisibility(View.GONE);
                }
                Glide.with(getContext()).load(posterUrl).into(posterImage);
                COUNTDOWN_IN_MILLIS = popUpTimer;
                show();
                startSmoothCountdown();
                startAnimationAndSound();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing PosterPopUp", e);
        }
    }

    private void startAnimationAndSound() {
        try {
            if (confettiAnimation1 != null) confettiAnimation1.playAnimation();
            if (confettiAnimation2 != null) confettiAnimation2.playAnimation();

            mediaPlayer = MediaPlayer.create(getContext(), R.raw.ding);
            if (mediaPlayer != null) {
                mediaPlayer.start();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing animation or sound", e);
        }
    }

    private void startSmoothCountdown() {
        if (progressIndicator == null) return;

        progressAnimator = ObjectAnimator.ofInt(
                progressIndicator,
                "progress",
                0,
                (int) COUNTDOWN_IN_MILLIS
        );
        progressAnimator.setDuration(COUNTDOWN_IN_MILLIS);
        progressAnimator.setInterpolator(new android.view.animation.LinearInterpolator());
        progressAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
              dismiss();
            }
        });
        progressAnimator.start();
    }

    private void stopProgressAnimation() {
        if (progressAnimator != null && progressAnimator.isRunning()) {
            progressAnimator.cancel();
        }
    }

    @Override
    public void dismiss() {
        try {
            super.dismiss();
            Log.e(TAG, "calling PosterPopUp");
            if(onDismissListener!=null) {
                onDismissListener.onDismissListener();
            }
            stopProgressAnimation();
            releaseMediaPlayer();
        } catch (Exception e) {
            Log.e(TAG, "Error dismissing PosterPopUp", e);
        }
    }

    private void releaseMediaPlayer() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error releasing MediaPlayer", e);
        }
    }

    public void setTargetActivity(String targetActivity) {
        try {
            this.targetActivityClass = ActivityLauncher.resolveClass(targetActivity);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Target activity class not found", e);
            this.targetActivityClass = null;
        }
    }

    public void addIntentExtra(String key, String value) {
        intentExtras.put(key, value);
    }


    public interface OnDismissListener{
        void onDismissListener();
    }

    public interface PosterClickListener {
        void onCallBackClick();
    }
}