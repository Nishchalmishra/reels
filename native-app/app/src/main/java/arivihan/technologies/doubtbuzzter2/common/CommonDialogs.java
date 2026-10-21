package arivihan.technologies.doubtbuzzter2.common;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.PUT_UPDATE;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.listener.dialog.DialogPositiveNegativeOptionClickListener;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionLockPopupDto;
import arivihan.technologies.doubtbuzzter2.model.UserDto;
import arivihan.technologies.doubtbuzzter2.model.dialog.PopUpOptionsDto;
import arivihan.technologies.doubtbuzzter2.model.microlecture.FeedbackModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;

public class CommonDialogs {
    private static final String TAG = CommonDialogs.class.getSimpleName();

    public static Dialog getAppPositiveReviewDialog(Context context, int starCount, DialogPositiveNegativeOptionClickListener listener) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_app_positive_review);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
        dialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(context, R.drawable.bachground_custom_dialogu_typeactivity));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        String firebaseJsonStr = MyApplication.getFirebaseRemoteConfig()
                .getString("app_positive_review_pop_up_" +
                        MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));

        Log.d(TAG, "firebaseJsonStr: " + firebaseJsonStr);

        ImageView iv_close = dialog.findViewById(R.id.iv_close);
        CardView cv_secondary = dialog.findViewById(R.id.cv_secondary);
        CardView cv_primary = dialog.findViewById(R.id.cv_primary);
        TextView tv_secondary_btn = dialog.findViewById(R.id.tv_secondary_btn);
        TextView tv_primary_btn = dialog.findViewById(R.id.tv_primary_btn);

        TextView tv_title = dialog.findViewById(R.id.tv_title);
        TextView tv_sub_title = dialog.findViewById(R.id.tv_sub_title);

        LottieAnimationView lottie_animation = dialog.findViewById(R.id.lottie_animation);

        try {
            Gson gson = new Gson();
            FeedbackModel feedbackModel = gson.fromJson(firebaseJsonStr, FeedbackModel.class);

            tv_primary_btn.setText(StringUtils.isNotEmpty(feedbackModel.getPrimaryBtnText()) ? feedbackModel.getPrimaryBtnText() : context.getString(R.string.yes));
            tv_secondary_btn.setText(StringUtils.isNotEmpty(feedbackModel.getSecondaryBtnText()) ? feedbackModel.getSecondaryBtnText() : context.getString(R.string.no));
            tv_title.setText(StringUtils.isNotEmpty(feedbackModel.getTitle()) ? feedbackModel.getTitle() : context.getString(R.string.thank_you_for_your_feedback));
            tv_sub_title.setText(StringUtils.isNotEmpty(feedbackModel.getSubtitle()) ? feedbackModel.getSubtitle() : context.getString(R.string.kya_aap_play_store_per_review_dena_chahte_he));

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        dialog.setOnDismissListener(v -> {
            lottie_animation.cancelAnimation();
            lottie_animation.clearAnimation();
            lottie_animation.setProgress(0f);
        });

        iv_close.setOnClickListener(v -> {
            dialog.dismiss();
            listener.onNegativeButtonClick(false, "", null);
        });

        cv_primary.setOnClickListener(v -> {
            dialog.dismiss();
            listener.onPositiveButtonClick(true, "", null);
        });

        cv_secondary.setOnClickListener(v -> {
            dialog.dismiss();
            listener.onNegativeButtonClick(false, "", null);
        });

        return dialog;
    }

    public static void setFeedbackThankYouDialog(Context context, DialogPositiveNegativeOptionClickListener listener) {
        try {
            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.dialog_feedback_thank_you);
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
            dialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(context, R.drawable.bachground_custom_dialogu_typeactivity));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.setCancelable(true);

            LottieAnimationView lottie_feedback = dialog.findViewById(R.id.lottie_feedback);

            TextView tv_title = dialog.findViewById(R.id.tv_title);
            TextView tv_sub_title = dialog.findViewById(R.id.tv_sub_title);

            ImageView iv_close = dialog.findViewById(R.id.iv_close);
            final MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.ding);

            int duration = (int) MyApplication.getFirebaseRemoteConfig().getLong("lecture_feedback_thank_you_popup_duration_in_ms");
            iv_close.setOnClickListener(imageView -> {
                dialog.dismiss();
            });

            dialog.setOnDismissListener(v -> {
                lottie_feedback.cancelAnimation();
                lottie_feedback.clearAnimation();
                lottie_feedback.setProgress(0f);
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                }
            });

            dialog.show();
            mediaPlayer.start();
            lottie_feedback.playAnimation();

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                }
            }, duration);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void setLocationServiceDialog(Context context, PopUpOptionsDto popUpOptionsDto, DialogPositiveNegativeOptionClickListener listener) {
        try {
            if (popUpOptionsDto == null)
                return;
            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.dialog_location_service_request);
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
            dialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(context, R.drawable.bachground_custom_dialogu_typeactivity));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.setCancelable(true);

            LottieAnimationView animation = dialog.findViewById(R.id.animation);

            TextView tv_title = dialog.findViewById(R.id.tv_title);
            TextView tv_sub_title = dialog.findViewById(R.id.tv_sub_title);
            TextView tv_highlight_text = dialog.findViewById(R.id.tv_highlight_text);
            TextView tv_negative = dialog.findViewById(R.id.tv_negative);
            TextView tv_positive = dialog.findViewById(R.id.tv_positive);

            MaterialCardView cv_negative = dialog.findViewById(R.id.cv_negative);
            MaterialCardView cv_positive = dialog.findViewById(R.id.cv_positive);

            String title = popUpOptionsDto.getTitle();
            if (StringUtils.isNotEmpty(title)) {
                tv_title.setText(CommonMethods.isValidJson(title) ? StyledTextBuilder.fromJson(context, title) : title);
            }

            String subTitle = popUpOptionsDto.getSubTitle();
            if (StringUtils.isNotEmpty(subTitle)) {
                tv_sub_title.setText(CommonMethods.isValidJson(subTitle) ? StyledTextBuilder.fromJson(context, subTitle) : subTitle);
            }

            String bodyText = popUpOptionsDto.getBodyText();
            if (StringUtils.isNotEmpty(bodyText)) {
                tv_highlight_text.setText(CommonMethods.isValidJson(bodyText) ? StyledTextBuilder.fromJson(context, bodyText) : bodyText);
            }

            String primaryButtonText = popUpOptionsDto.getPrimaryButtonName();
            if (StringUtils.isNotEmpty(primaryButtonText)) {
                tv_positive.setText(CommonMethods.isValidJson(primaryButtonText) ? StyledTextBuilder.fromJson(context, primaryButtonText) : primaryButtonText);
            }

            String secondaryButtonText = popUpOptionsDto.getSecondaryButtonName();
            if (StringUtils.isNotEmpty(secondaryButtonText)) {
                tv_negative.setText(CommonMethods.isValidJson(secondaryButtonText) ? StyledTextBuilder.fromJson(context, secondaryButtonText) : secondaryButtonText);
            }

            ImageView ivCancel = dialog.findViewById(R.id.iv_cancel);

            ivCancel.setOnClickListener(imageView -> dialog.dismiss());

            dialog.setOnDismissListener(v -> {
                animation.cancelAnimation();
                animation.clearAnimation();
                animation.setProgress(0f);
            });

            cv_positive.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPositiveButtonClick(true, "", null);
                }
                dialog.dismiss();
            });

            cv_negative.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNegativeButtonClick(true, "", null);
                }
                dialog.dismiss();
            });

            dialog.show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Dialog showLockPopupDialog(
            Context context,
            SubscriptionLockPopupDto dto,
            SubscriptionPopupListener listener
    ) {

        Log.d(TAG, "showLockPopupDialog: " + dto);

        try {

            Dialog dialog = new Dialog(context);

            dialog.setContentView(R.layout.common_subscription_lock_dialog);

            if (dialog.getWindow() != null) {

                dialog.getWindow().getAttributes().windowAnimations =
                        R.style.DialogAnimationUpDown;

                dialog.getWindow().setBackgroundDrawable(
                        AppCompatResources.getDrawable(
                                context,
                                R.drawable.bachground_custom_dialogu_typeactivity
                        )
                );

                dialog.getWindow().setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
            }

            dialog.setCancelable(true);

            TextView header = dialog.findViewById(R.id.header);
            TextView dialogTitle = dialog.findViewById(R.id.dialog_title);
            MaterialCardView subscriptionLayout =
                    dialog.findViewById(R.id.subscriptionLayout);
            TextView subs = dialog.findViewById(R.id.subs);
            ImageView iv_close = dialog.findViewById(R.id.iv_close);

            dialogTitle.setVisibility(View.GONE);
            subscriptionLayout.setVisibility(View.GONE);

            setStyledTextIfNotEmpty(context, dialogTitle, dto.getTitle());

            if (StringUtils.isNotEmpty(dto.getBtnName())) {

                subscriptionLayout.setVisibility(View.VISIBLE);

                subs.setText(
                        getStyledText(context, dto.getBtnName())
                );
            }

            setStyledTextIfNotEmpty(context, header, dto.getSubTitle());

            // =========================
            // DISMISS CALLBACK
            // =========================

            dialog.setOnDismissListener(v -> {

                try {

                    Map<String, String> params = new HashMap<>();

                    params.put(
                            "title",
                            dto.getTitle() != null
                                    ? dto.getTitle()
                                    : ""
                    );

                    params.put(
                            "subTitle",
                            dto.getSubTitle() != null
                                    ? dto.getSubTitle()
                                    : ""
                    );

                    MyApplication.logFirebaseBundleCreation(
                            FirebaseEvents
                                    .subscription_screen_lock_pop_up_close
                                    .getActionName(),
                            params
                    );

                } catch (Exception ignored) {
                }

                // CALLBACK
                try {
                    if (listener != null) {
                        listener.onPopupClosed();
                    }
                } catch (Exception ignored) {
                }
            });

            // =========================
            // JOIN NOW CLICK
            // =========================

            subscriptionLayout.setOnClickListener(v -> {

                try {
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.subscription_locked_popup_join_click.getActionName());

                    Map<String, String> params = new HashMap<>();

                    params.put(
                            "title",
                            dto.getTitle() != null
                                    ? dto.getTitle()
                                    : ""
                    );

                    params.put(
                            "subTitle",
                            dto.getSubTitle() != null
                                    ? dto.getSubTitle()
                                    : ""
                    );

                    MyApplication.logFirebaseBundleCreation(
                            FirebaseEvents
                                    .subscription_screen_lock_pop_up_join_now_click
                                    .getActionName(),
                            params
                    );

                } catch (Exception ignored) {
                }

                // CALLBACK
                try {
                    if (listener != null) {
                        listener.onJoinNowClicked();
                    }
                } catch (Exception ignored) {
                }

                try {

                    if (StringUtils.isNotEmpty(dto.getClassName())) {

                        MyApplication.openActivity(
                                context,
                                dto.getClassName(),
                                dto.getActivityParams()
                        );
                    }

                    dialog.dismiss();

                } catch (Exception ignored) {
                }
            });

            // =========================
            // CLOSE BUTTON
            // =========================

            iv_close.setOnClickListener(v -> {
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.subscription_locked_popup_join_click.getActionName());
                dialog.dismiss();
            });

            dialog.show();

            // =========================
            // AUTO CLOSE
            // =========================

            if (dto.getAutoCloseDialogTimer() > 0) {

                new Handler(Looper.getMainLooper()).postDelayed(() -> {

                    try {

                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }

                    } catch (Exception ignored) {
                    }

                }, dto.getAutoCloseDialogTimer());
            }

            return dialog;

        } catch (Exception e) {

            Log.e(TAG, "showLockPopupDialog: ", e);

            return null;
        }
    }


    public static Dialog showLockPopupDialog(
            Context context,
            SubscriptionLockPopupDto dto
    ) {
        Log.d(TAG, "showLockPopupDialog: "+dto);
        try {
            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.common_subscription_lock_dialog);
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
            dialog.getWindow().setBackgroundDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.bachground_custom_dialogu_typeactivity)
            );
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            dialog.setCancelable(true);

            TextView header = dialog.findViewById(R.id.header);
            TextView dialogTitle = dialog.findViewById(R.id.dialog_title);
            MaterialCardView subscriptionLayout = dialog.findViewById(R.id.subscriptionLayout);
            TextView subs = dialog.findViewById(R.id.subs);
            ImageView iv_close = dialog.findViewById(R.id.iv_close);

            dialogTitle.setVisibility(View.GONE);
            subscriptionLayout.setVisibility(View.GONE);

            setStyledTextIfNotEmpty(context, dialogTitle, dto.getTitle());

            if (StringUtils.isNotEmpty(dto.getBtnName())) {
                subscriptionLayout.setVisibility(View.VISIBLE);
                subs.setText(getStyledText(context, dto.getBtnName()));
            }

            setStyledTextIfNotEmpty(context, header, dto.getSubTitle());

            dialog.setOnDismissListener(v -> {
                try {
                    Map<String, String> params = new HashMap<>();
                    params.put("title", dto.getTitle() != null ? dto.getTitle() : "");
                    params.put("subTitle", dto.getSubTitle() != null ? dto.getSubTitle() : "");
                    MyApplication.logFirebaseBundleCreation(
                            FirebaseEvents.subscription_screen_lock_pop_up_close.getActionName(),
                            params
                    );
                } catch (Exception ignored) {
                }
            });

            subscriptionLayout.setOnClickListener(v -> {
                try {
                    Map<String, String> params = new HashMap<>();
                    params.put("title", dto.getTitle() != null ? dto.getTitle() : "");
                    params.put("subTitle", dto.getSubTitle() != null ? dto.getSubTitle() : "");
                    MyApplication.logFirebaseBundleCreation(
                            FirebaseEvents.subscription_screen_lock_pop_up_join_now_click.getActionName(),
                            params
                    );
                } catch (Exception ignored) {
                }

                try {
                    if (StringUtils.isNotEmpty(dto.getClassName())) {
                        MyApplication.openActivity(
                                context,
                                dto.getClassName(),
                                dto.getActivityParams()
                        );
                    }
                    dialog.dismiss();
                } catch (Exception ignored) {
                }
            });

            iv_close.setOnClickListener(v -> dialog.dismiss());

            dialog.show();

            if (dto.getAutoCloseDialogTimer() > 0) {
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }, dto.getAutoCloseDialogTimer());
            }

            return dialog;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Dialog showPremiumLockPopupDialog(
            Context context,
            SubscriptionLockPopupDto dto
    ) {
        try {
            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.dialog_common_subscription_lock_premium);
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
            dialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(context, R.drawable.bachground_custom_dialogu_typeactivity));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.setCancelable(true);

            TextView header = dialog.findViewById(R.id.header);
            TextView dialogTitle = dialog.findViewById(R.id.dialog_title);
            MaterialCardView subscriptionLayout = dialog.findViewById(R.id.subscriptionLayout);
            TextView subs = dialog.findViewById(R.id.subs);

            ImageView iv_close = dialog.findViewById(R.id.iv_close);

            dialogTitle.setVisibility(View.GONE);
            subscriptionLayout.setVisibility(View.GONE);

            setStyledTextIfNotEmpty(context, dialogTitle, dto.getTitle());

            if (StringUtils.isNotEmpty(dto.getBtnName())) {
                subscriptionLayout.setVisibility(View.VISIBLE);
                subs.setText(getStyledText(context, dto.getBtnName()));
            }

            setStyledTextIfNotEmpty(context, header, dto.getSubTitle());

            dialog.setOnDismissListener(v -> {
                try {
                    Map<String, String> params = new HashMap<>();
                    params.put("title", dto.getTitle() != null ? dto.getTitle() : "");
                    params.put("subTitle", dto.getSubTitle() != null ? dto.getSubTitle() : "");
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.subscription_screen_lock_pop_up_close.getActionName(), params);
                } catch (Exception ignored) {
                }
            });

            subscriptionLayout.setOnClickListener(v -> {
                try {
                    Map<String, String> params = new HashMap<>();
                    params.put("title", dto.getTitle() != null ? dto.getTitle() : "");
                    params.put("subTitle", dto.getSubTitle() != null ? dto.getSubTitle() : "");
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.subscription_screen_lock_pop_up_join_now_click.getActionName(), params);
                } catch (Exception ignored) {
                }

                try {
                    if (StringUtils.isNotEmpty(dto.getClassName())) {
                        MyApplication.openActivity(
                                context,
                                dto.getClassName(),
                                dto.getActivityParams()
                        );
                    }
                    dialog.dismiss();
                } catch (Exception ignored) {
                }
            });

            iv_close.setOnClickListener(v -> {
                dialog.dismiss();
            });

            dialog.show();
            return dialog;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Shows the doubt-limit lock dialog ({@code custom_dialog_lock_box}).
     * <p>
     * The message body is resolved automatically from the local config using the
     * user's {@code grade_code} and {@code My_Lang} SharedPreference values.
     *
     * @param context calling context
     * @param title   bold heading shown at the top of the dialog
     * @param btnText label for the subscription CTA button
     */
    public static void showDoubtLimitDialog(Context context, String title, String btnText) {
        try {
            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.custom_dialog_lock_box);
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
            dialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(context, R.drawable.bachground_custom_dialogu_typeactivity));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.setCancelable(true);

            TextView tvTitle = dialog.findViewById(R.id.dialog_title);
            TextView tvMessage = dialog.findViewById(R.id.header);
            MaterialCardView cvCta = dialog.findViewById(R.id.subscriptionLayout);
            TextView tvCta = dialog.findViewById(R.id.subs);
            ImageView ivClose = dialog.findViewById(R.id.cancel);
            LottieAnimationView lottie = dialog.findViewById(R.id.dialog_animation);

            // Title
            tvTitle.setText(title);
            tvTitle.setVisibility(StringUtils.isNotEmpty(title) ? View.VISIBLE : View.GONE);

            // Message body — resolved from local config
            String gradeCode = MyApplication.getStringData(PreferenceKeys.GRADE_CODE.getKey());
            String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
            tvMessage.setText(getDoubtLimitMessage(gradeCode, lang));

            // CTA button
            tvCta.setText(btnText);
            cvCta.setOnClickListener(v -> {
                dialog.dismiss();
                SubscriptionNavigationHelper.navigateToSubscriptionPage(context, RedirectionSource.JOIN_51_CLICK);
            });

            ivClose.setOnClickListener(v -> dialog.dismiss());

            dialog.setOnDismissListener(v -> {
                lottie.cancelAnimation();
                lottie.clearAnimation();
                lottie.setProgress(0f);
            });

            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Remote config fetch ───────────────────────────────────────────────────

    /**
     * Fetches the doubt-limit body message from the single {@code doubt_limit_message}
     * Remote Config key (a JSON object keyed by gradeCode → lang → message).
     * Local defaults are declared in {@code res/xml/remote_config_defaults.xml}.
     */
    private static String getDoubtLimitMessage(String gradeCode, String lang) {
        try {
            String json = MyApplication.getFirebaseRemoteConfig().getString("doubt_limit_message");
            JSONObject root = new JSONObject(json);
            // Try the specific gradeCode first, fall back to DEFAULT
            JSONObject gradeObj = root.optJSONObject(gradeCode);
            if (gradeObj == null) {
                gradeObj = root.optJSONObject("DEFAULT");
            }
            if (gradeObj != null) {
                String message = gradeObj.optString(lang, "");
                if (StringUtils.isNotEmpty(message)) return message;
                // Fallback to English if requested lang key is missing
                return gradeObj.optString("en", "");
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private static void setStyledTextIfNotEmpty(Context context, TextView textView, String value) {
        if (StringUtils.isNotEmpty(value)) {
            textView.setText(getStyledText(context, value));
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    private static CharSequence getStyledText(Context context, String value) {
        if (CommonMethods.isValidJson(value)) {
            return StyledTextBuilder.fromJson(context, value);
        } else {
            return value;
        }
    }

    public interface SubscriptionPopupListener {
        void onPopupClosed();
        void onJoinNowClicked();
    }

    /**
     * Configurable limit-exhausted dialog.
     *
     * Usage – Doubt session:
     *   MessageLimitDialog.show(context, MessageLimitDialog.Type.DOUBT_SESSION);
     *
     * Usage – Virtual Batch:
     *   MessageLimitDialog.show(context, MessageLimitDialog.Type.VIRTUAL_BATCH);
     */
    public final class MessageLimitDialog {

        // ── Dialog type ────────────────────────────────────────────────────────────
        public enum Type {
            DOUBT_SESSION,
            VIRTUAL_BATCH
        }

        private MessageLimitDialog() {}          // utility class — no instances

        // ── Public entry-point ─────────────────────────────────────────────────────
        public static void show(Context context, Type type) {
            try {
                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.dialog_doubt_session_limit);   // reuse same layout
                dialog.getWindow().getAttributes().windowAnimations =
                        R.style.DialogAnimationUpDown;
                dialog.getWindow().setBackgroundDrawable(
                        AppCompatResources.getDrawable(
                                context, R.drawable.bachground_custom_dialogu_typeactivity));
                dialog.getWindow().setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.setCancelable(true);

                // ── Views ──────────────────────────────────────────────────────────
                ImageView            ivCancel = dialog.findViewById(R.id.cancel);
                LottieAnimationView  lottie   = dialog.findViewById(R.id.dialog_animation);
                TextView             tvHeader = dialog.findViewById(R.id.header);
                MaterialCardView     cvCta    = dialog.findViewById(R.id.subscriptionLayout);
                TextView             tvCta    = dialog.findViewById(R.id.subs);

                // ── Language ───────────────────────────────────────────────────────
                String  lang    = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
                boolean isHindi = "hi".equalsIgnoreCase(lang);

                // ── Per-type copy (fully configurable here) ────────────────────────
                String ctaText    = resolveCta(type, isHindi);
                String prefix     = resolvePrefix(type, isHindi);
                String suffix     = resolveSuffix(type, isHindi);

                tvCta.setText(ctaText);

                // ── Countdown: resets at 12:00 AM ──────────────────────────────────
                java.util.Calendar midnight = java.util.Calendar.getInstance();
                midnight.set(java.util.Calendar.HOUR_OF_DAY, 0);
                midnight.set(java.util.Calendar.MINUTE, 0);
                midnight.set(java.util.Calendar.SECOND, 0);
                midnight.set(java.util.Calendar.MILLISECOND, 0);
                midnight.add(java.util.Calendar.DAY_OF_MONTH, 1);

                long millisUntilReset =
                        midnight.getTimeInMillis() - System.currentTimeMillis();

                AtomicReference<CountDownTimer> timerRef = new AtomicReference<>();

                CountDownTimer timer = new CountDownTimer(millisUntilReset, 1_000) {

                    @Override
                    public void onTick(long millisUntilFinished) {
                        long h = millisUntilFinished / 3_600_000;
                        long m = (millisUntilFinished % 3_600_000) / 60_000;
                        long s = (millisUntilFinished % 60_000) / 1_000;

                        String timerText = String.format(
                                java.util.Locale.getDefault(),
                                "%02d:%02d:%02d", h, m, s);

                        String fullText = prefix + timerText + suffix;

                        SpannableString spannable = new SpannableString(fullText);
                        int start = prefix.length();
                        int end   = start + timerText.length();
                        spannable.setSpan(
                                new StyleSpan(Typeface.BOLD),
                                start, end,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        spannable.setSpan(
                                new ForegroundColorSpan(
                                        ContextCompat.getColor(context, R.color.arivihan_secondary_color)),
                                start, end,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        tvHeader.post(() -> tvHeader.setText(spannable));
                    }

                    @Override
                    public void onFinish() {
                        // Limit resets at 12:00 AM — auto-dismiss
                        if (dialog.isShowing()) dialog.dismiss();
                    }
                };

                timerRef.set(timer);
                timer.start();

                // ── Listeners — stay on current screen ─────────────────────────────
                cvCta.setOnClickListener(v -> dialog.dismiss());
                ivCancel.setOnClickListener(v -> dialog.dismiss());

                // ── Cleanup on dismiss ─────────────────────────────────────────────
                dialog.setOnDismissListener(d -> {
                    CountDownTimer t = timerRef.get();
                    if (t != null) t.cancel();
                    lottie.cancelAnimation();
                    lottie.clearAnimation();
                    lottie.setProgress(0f);
                });

                dialog.show();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private static String resolveCta(Type type, boolean isHindi) {
            switch (type) {
                case VIRTUAL_BATCH:
                    return isHindi ? "ठीक है, समझ गया" : "OK, understood";
                case DOUBT_SESSION:
                default:
                    return isHindi ? "ठीक है, समझ गया" : "OK, understood";
            }
        }

        private static String resolvePrefix(Type type, boolean isHindi) {
            switch (type) {
                case VIRTUAL_BATCH:
                    return isHindi
                            ? "आपकी आज की मैसेज लिमिट पूरी हो गई है, "
                            : "Aapki aaj ki message limit poori ho gayi hai, ";
                case DOUBT_SESSION:
                default:
                    return isHindi
                            ? "आज की डाउट सेशन लिमिट पूरी हो गई है, "
                            : "Aaj ki doubt session limit poori ho gayi hai, ";
            }
        }

        private static String resolveSuffix(Type type, boolean isHindi) {
            switch (type) {
                case VIRTUAL_BATCH:
                    return isHindi
                            ? " के बाद फिर मैसेज कर पाओगे।"
                            : " ke baad phir message kar paoge.";
                case DOUBT_SESSION:
                default:
                    return isHindi
                            ? " के बाद और सवाल पूछ पाओगे!"
                            : " ke baad aur questions pooch paoge!";
            }
        }
    }

}
