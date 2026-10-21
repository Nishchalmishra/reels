package arivihan.technologies.doubtbuzzter2.customView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.ActionType;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.model.batch.home.BottomSheetActionButtonResponse;
import arivihan.technologies.doubtbuzzter2.model.batch.home.BottomSheetContentResponse;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class CustomBottomSheetDialog extends BottomSheetDialogFragment implements Player.Listener {

    private static final String TAG = CustomBottomSheetDialog.class.getSimpleName();

    // UI Components
    private ImageView thumbnailView;
    private ImageView closeButton;
    private PlayerView bottomSheetPlayerView;
    private ProgressBar spinnerVideoDetails;
    private ShimmerFrameLayout imageShimmer;
    private TextView title;
    private TextView subtitle;
    private CardView cvSecondary;
    private CardView cvPrimary;
    private TextView tvSecondary;
    private TextView tvPrimary;

    // Player related
    private ExoPlayer player;
    private long videoStartingTime;


    // Data
    private Context context;
    private CustomBottomSheetListener bottomSheetListener;
    private BottomSheetContentResponse popupData;

    // Handler
    private final Handler handler = new Handler(Looper.getMainLooper());

    // Deprecated fields (kept for backward compatibility)
    @Deprecated
    private String thumbnail;
    @Deprecated
    private String videoUrl;
    @Deprecated
    private int ongoingDay;
    @Deprecated
    private boolean fromHomeScreen;

    private int watchedPercentage = 0;

    public CustomBottomSheetDialog(Context context,
                                   CustomBottomSheetListener bottomSheetListener,
                                   String thumbnailUrlKey,
                                   String videoUrlKey,
                                   int ongoingDay,
                                   boolean fromHomeScreen,
                                   BottomSheetContentResponse popupData) {
        this.context = context;
        this.bottomSheetListener = bottomSheetListener;
        this.thumbnail = thumbnailUrlKey;
        this.videoUrl = videoUrlKey;
        this.ongoingDay = ongoingDay;
        this.fromHomeScreen = fromHomeScreen;
        this.popupData = popupData;
    }

    public CustomBottomSheetDialog() {
        // Default constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.hal_yearly_bottom_sheet, container, false);

        initializeViews(view);
        setupCloseButton();

        if (popupData != null && bottomSheetListener != null) {
            handleButtonList(popupData);
            setupButtons();
            setTitleAndSubtitle();
            downloadThumbnail();
            initializePlayer();
            scheduleVideoStart();
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.hs_welcome_video_bottom_sheet_viewed.getActionName());
        }

        return view;
    }

    private void initializeViews(View view) {
        title = view.findViewById(R.id.title);
        subtitle = view.findViewById(R.id.text1);
        spinnerVideoDetails = view.findViewById(R.id.spinnerVideoDetails);
        cvSecondary = view.findViewById(R.id.cv_left_btn);
        cvPrimary = view.findViewById(R.id.cv_right_btn);
        tvSecondary = view.findViewById(R.id.txt_cv_left_btn);
        tvPrimary = view.findViewById(R.id.txt_cv_right_btn);
        thumbnailView = view.findViewById(R.id.thumbnail);
        bottomSheetPlayerView = view.findViewById(R.id.video_player);
        closeButton = view.findViewById(R.id.close_btn);
        imageShimmer = view.findViewById(R.id.image_shimmer);

        imageShimmer.startShimmer();
    }

    private void setupCloseButton() {
        closeButton.setOnClickListener(v -> {
            HashMap<String, String> params = new HashMap<>();
            if (player != null && player.getDuration() > 0) {
                watchedPercentage = (int) ((player.getCurrentPosition() * 100) / player.getDuration());
            }
            params.put("bottomSheetProgress", String.valueOf(watchedPercentage));
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.hs_closed_welcome_screen_bottom_sheet.getActionName(), params);
            if (bottomSheetListener != null) {
                bottomSheetListener.onCloseButtonClick(
                );
            }
            initiateSecondLiveEngagementReminder();
        });
    }

    private void setTitleAndSubtitle() {
        if (StringUtils.isNotEmpty(popupData.getTitle())) {
            String cleanTitle = CommonMethods.removeUnrenderableEmojis(popupData.getTitle());
            title.setText(fromHtml(cleanTitle));
        }

        if (StringUtils.isNotEmpty(popupData.getSubtitle())) {
            subtitle.setText(fromHtml(popupData.getSubtitle()));
        } else {
            subtitle.setVisibility(View.GONE);
        }
    }

    private CharSequence fromHtml(String html) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(html);
        }
    }

    private void scheduleVideoStart() {
        startPlayer();
    }

    private void handleButtonList(BottomSheetContentResponse content) {
        if (content == null || content.getButtons() == null) {
            return;
        }

        for (BottomSheetActionButtonResponse button : content.getButtons()) {
            if (button != null && ActionType.OPEN_REGISTER_DIALOG.name().equals(button.getActionType())) {
                saveRegisterDialogData(button);
            }
        }
    }

    private void saveRegisterDialogData(BottomSheetActionButtonResponse button) {
        MyApplication.saveData(PreferenceKeys.DIALOG_BANNER_IMAGE.getKey(), button.getDialogBannerImage());
        MyApplication.saveData(PreferenceKeys.DIALOG_TIMEOUT.getKey(),
                button.getDialogTimeout() != null ? button.getDialogTimeout() : 0);
        MyApplication.saveData(PreferenceKeys.DIALOG_BANNER_TEXT.getKey(), button.getDialogBannerText());
    }

    private void downloadThumbnail() {
        if (StringUtils.isNotEmpty(popupData.getThumbnail())) {
            Glide.with(context)
                    .load(popupData.getThumbnail())
                    .into(thumbnailView);
            imageShimmer.stopShimmer();
            imageShimmer.setVisibility(View.GONE);
            thumbnailView.setVisibility(View.VISIBLE);
        }
    }

    private void initializePlayer() {
        if (player == null) {
            player = new ExoPlayer.Builder(context).build();
            bottomSheetPlayerView.setPlayer(player);
        }
    }

    private void startPlayer() {
        if (StringUtils.isNotEmpty(popupData.getUrl()) && player != null) {
            buildMediaSource(Uri.parse(popupData.getUrl()));
        }
    }

    private void buildMediaSource(Uri hlsUri) {
        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(hlsUri)
                .setMimeType(MimeTypes.APPLICATION_M3U8)
                .build();

        player.setMediaItem(mediaItem);
        player.prepare();
        player.setPlayWhenReady(true);
        player.addListener(this);
        player.seekTo(videoStartingTime);
    }

    @Override
    public void onIsPlayingChanged(boolean isPlaying) {
        handlePlayerState(player.getPlaybackState());
    }

    private void handlePlayerState(int playbackState) {
        switch (playbackState) {
            case Player.STATE_BUFFERING:
                showBufferingState();
                break;
            case Player.STATE_ENDED:
                pausePlayer();
                showThumbnail();
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.hs_welcome_video_bottom_sheet_watched_successfully.getActionName());
                break;
            case Player.STATE_IDLE:
                videoStartingTime = player.getCurrentPosition();
                break;
            case Player.STATE_READY:
                showPlayer();
                break;
        }
    }

    private void showBufferingState() {
        spinnerVideoDetails.setVisibility(View.VISIBLE);
        bottomSheetPlayerView.setVisibility(View.GONE);
        thumbnailView.setVisibility(View.VISIBLE);
    }

    private void showThumbnail() {
        bottomSheetPlayerView.setVisibility(View.GONE);
        thumbnailView.setVisibility(View.VISIBLE);
    }

    private void showPlayer() {
        spinnerVideoDetails.setVisibility(View.GONE);
        thumbnailView.setVisibility(View.GONE);
        bottomSheetPlayerView.setVisibility(View.VISIBLE);
    }

    private void setupButtons() {
        List<BottomSheetActionButtonResponse> buttons = popupData.getButtons();
        if (buttons == null || buttons.isEmpty()) {
            hideAllButtons();
            return;
        }

        LinearLayout parentLayout = (LinearLayout) cvPrimary.getParent();

        if (buttons.size() == 1) {
            setupSingleButton(parentLayout, buttons.get(0));
        } else {
            setupDoubleButtons(parentLayout, buttons.get(0), buttons.get(1));
        }
    }

    private void hideAllButtons() {
        cvSecondary.setVisibility(View.GONE);
        cvPrimary.setVisibility(View.GONE);
    }

    private void setupSingleButton(LinearLayout parentLayout, BottomSheetActionButtonResponse btnData) {
        cvSecondary.setVisibility(View.GONE);
        cvPrimary.setVisibility(View.VISIBLE);
        parentLayout.setGravity(Gravity.CENTER_HORIZONTAL);

//        LinearLayout.LayoutParams params =
//                (LinearLayout.LayoutParams) cvPrimary.getLayoutParams();
//        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
//        params.weight = 0;
//        cvPrimary.setLayoutParams(params);

        ViewGroup.MarginLayoutParams params =
                (ViewGroup.MarginLayoutParams) cvPrimary.getLayoutParams();

        params.setMargins(CommonMethods.dpToPx(context, 75), 0, CommonMethods.dpToPx(context, 75), 0);
        cvPrimary.setLayoutParams(params);

        tvPrimary.setText(btnData.getButtonText());
        applyColors(cvPrimary, tvPrimary, btnData);
        cvPrimary.setOnClickListener(v -> handleButtonAction(btnData));
    }

    private void setupDoubleButtons(LinearLayout parentLayout,
                                    BottomSheetActionButtonResponse btnLeft,
                                    BottomSheetActionButtonResponse btnRight) {
        cvSecondary.setVisibility(View.VISIBLE);
        cvPrimary.setVisibility(View.VISIBLE);
        parentLayout.setGravity(Gravity.NO_GRAVITY);

        // Configure left button
        LinearLayout.LayoutParams paramsLeft =
                (LinearLayout.LayoutParams) cvSecondary.getLayoutParams();
        paramsLeft.width = 0;
        paramsLeft.weight = 1;
        cvSecondary.setLayoutParams(paramsLeft);

        // Configure right button
        LinearLayout.LayoutParams paramsRight =
                (LinearLayout.LayoutParams) cvPrimary.getLayoutParams();
        paramsRight.width = 0;
        paramsRight.weight = 1;
        cvPrimary.setLayoutParams(paramsRight);

        tvSecondary.setText(btnLeft.getButtonText());
        tvPrimary.setText(btnRight.getButtonText());

        applyColors(cvSecondary, tvSecondary, btnLeft);
        applyColors(cvPrimary, tvPrimary, btnRight);

        cvSecondary.setOnClickListener(v -> handleButtonAction(btnLeft));
        cvPrimary.setOnClickListener(v -> handleButtonAction(btnRight));
    }

    private void applyColors(CardView cardView, TextView textView, BottomSheetActionButtonResponse btnData) {
        try {
            if (btnData.getBgColorCode() != null) {
                cardView.setCardBackgroundColor(Color.parseColor(btnData.getBgColorCode()));
            }
            if (btnData.getTextColorCode() != null) {
                textView.setTextColor(Color.parseColor(btnData.getTextColorCode()));
            }
        } catch (Exception e) {
            Log.e(TAG, "Color parsing error", e);
        }
    }

    private void handleButtonAction(BottomSheetActionButtonResponse btnData) {
        Log.d(TAG, "handleButtonAction: " + btnData);
        HashMap<String, String> params = new HashMap<>();
        if (player != null && player.getDuration() > 0) {
            watchedPercentage = (int) ((player.getCurrentPosition() * 100) / player.getDuration());
            params.put("bottomSheetProgress", String.valueOf(watchedPercentage));
        }
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.hs_clicked_welcome_screen_bottom_sheet.getActionName(), params);

        if (ActionType.OPEN_ACTIVITY.name().equals(btnData.getActionType())) {
            handleOpenActivity(btnData);
        } else if (ActionType.OPEN_REGISTER_DIALOG.name().equals(btnData.getActionType())) {
            bottomSheetListener.onDismiss(btnData);
        }

        bottomSheetListener.onCloseButtonClick();
    }

    private void handleOpenActivity(BottomSheetActionButtonResponse btnData) {
        if (btnData.getClassName() != null && btnData.getActivityParams() != null) {
            bottomSheetListener.onStartNowClick(
                    btnData.getClassName(),
                    btnData.getActivityParams()
            );
            HashMap<String, String> params = new HashMap<>();
            params.put("className", btnData.getClassName());
            params.put("activityParams", btnData.getActivityParams());

            if (player != null && player.getDuration() > 0) {
                watchedPercentage = (int) ((player.getCurrentPosition() * 100) / player.getDuration());
            }
            params.put("bottomSheetProgress", String.valueOf(watchedPercentage));
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.hs_welcome_video_bottom_sheet_button_clicked.getActionName(), params);

            initiateSecondLiveEngagementReminder();
        }
    }

    private void initiateSecondLiveEngagementReminder() {
        if (popupData != null && popupData.isForLiveClass()) {
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.user_skipped_first_live_engagement_reminder.getActionName());
            MyApplication.insertLiveSessionStatus(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        resumePlayer();
    }

    @Override
    public void onPause() {
        super.onPause();
        pausePlayer();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        releasePlayer();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    private void resumePlayer() {
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    private void pausePlayer() {
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isAdded() && !isRemoving()) {
            dismissAllowingStateLoss();
        }
    }

    public interface CustomBottomSheetListener {
        void onCloseButtonClick();

        void onStartNowClick(String className, String activityParams);

        void onDismiss(BottomSheetActionButtonResponse btnData);
    }
}