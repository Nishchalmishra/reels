package arivihan.technologies.doubtbuzzter2.customView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.card.MaterialCardView;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.model.CampaignSubscriptionScreenContactDto;

public class ContactBottomSheet extends BottomSheetDialogFragment {

    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();

    private SeekBar seekBar;
    private ImageView playPauseIcon;
    private TextView tvCurrentTime, tvTotalTime;

    private ImageView posterImage;
    private TextView tvNeedHelp;
    private TextView tvPriceSection; // ✅ replaces joinText (maps to tv_price_section)

    private boolean isPlaying = false;
    private boolean wasPlayingBeforeBackground = false;

    private CampaignSubscriptionScreenContactDto contactDto;
    private ContactBottomSheetListner listner;
    private CardView btnPlayPause;
    private boolean handledByButton = false;

    public ContactBottomSheet(
            CampaignSubscriptionScreenContactDto dto,
            ContactBottomSheetListner listner
    ) {
        this.contactDto = dto;
        this.listner = listner;
    }

    public ContactBottomSheet() {
    }

    public static ContactBottomSheet newInstance(
            CampaignSubscriptionScreenContactDto dto
    ) {
        ContactBottomSheet fragment = new ContactBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable("contact_dto", dto);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ContactBottomSheetListner) {
            listner = (ContactBottomSheetListner) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog);
        try {
            if (getArguments() != null) {
                contactDto = (CampaignSubscriptionScreenContactDto)
                        getArguments().getSerializable("contact_dto");
            }
        } catch (Exception ignored) {}
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.contact_bottom_sheet_dialog_for_after_plan, container, false);

        ImageView btnClose              = view.findViewById(R.id.btnClose);
        MaterialCardView whatsAppButton = view.findViewById(R.id.mcv_contact_us);
        MaterialCardView joinNowButton  = view.findViewById(R.id.mcv_priceSection);
        btnPlayPause                    = view.findViewById(R.id.btnPlayPause);
        playPauseIcon                   = view.findViewById(R.id.img_play_pause);
        seekBar                         = view.findViewById(R.id.audioSeekBar);
        tvCurrentTime                   = view.findViewById(R.id.tvCurrentTime);
        tvTotalTime                     = view.findViewById(R.id.tvTotalTime);
        posterImage                     = view.findViewById(R.id.posterImage);
        tvNeedHelp                      = view.findViewById(R.id.tv_need_help);
        tvPriceSection                  = view.findViewById(R.id.tv_price_section);


        setBottomSheetData();
        startAudio();

        btnClose.setOnClickListener(v -> {
            handledByButton = true;
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.contact_us_popup_close_click.getActionName());
            dismiss();
            listner.onCloseButtonClick();
        });

        joinNowButton.setOnClickListener(v -> {
            handledByButton = true;
            MyApplication.logFirebaseBundleCreation(FirebaseEvents.contact_us_popup_join_click.getActionName());
            listner.onPaymentOptionClick();
        });

        whatsAppButton.setOnClickListener(v -> {
            handledByButton = true;
            openWhatsApp(contactDto.getPhoneNumber());
            dismiss();
        });


        btnPlayPause.setOnClickListener(v -> toggleAudio());

        return view;
    }

    // ─── WhatsApp Opener ──────────────────────────────────────────────────────────

    private void openWhatsApp(String phoneNumber) {
        String cleaned = phoneNumber.replaceAll("[^\\d+]", "");

        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://wa.me/" + cleaned));
        intent.setPackage("com.whatsapp");

        try {
            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                openDialer(cleaned);
            }
        } catch (Exception e) {
            openDialer(cleaned);
        }
    }

    private void openDialer(String phoneNumber) {
        Intent dialerIntent = new Intent(Intent.ACTION_DIAL,
                Uri.parse("tel:" + phoneNumber));
        startActivity(dialerIntent);
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────────

    @Override
    public void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            wasPlayingBeforeBackground = true;
            mediaPlayer.pause();
            isPlaying = false;
            playPauseIcon.setImageResource(R.drawable.media3_icon_play);
            handler.removeCallbacks(updateSeekBarRunnable);
        } else {
            wasPlayingBeforeBackground = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mediaPlayer != null && wasPlayingBeforeBackground && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            isPlaying = true;
            wasPlayingBeforeBackground = false;
            playPauseIcon.setImageResource(R.drawable.media3_icon_pause);
            handler.post(updateSeekBarRunnable);
        }
    }

    // ─── Setup Bottom Sheet Data ──────────────────────────────────────────────────

    private void setBottomSheetData() {
        if (contactDto == null) return;
        Log.d("contactDto", "setBottomSheetData: "+contactDto);
        tvPriceSection.setText(contactDto.getJoinNowText());
        Glide.with(requireContext())
                .load(contactDto.getPosterImage())
                .into(posterImage);
        if(StringUtils.isNotEmpty(contactDto.getCallBackText())) {
            tvNeedHelp.setText(contactDto.getCallBackText());
        }

    }

    // ─── Audio Player ─────────────────────────────────────────────────────────────

    private void startAudio() {
        if (contactDto == null || contactDto.getAudioUrl() == null) {
            Log.w("AudioPlayer", "Audio URL is null");
            return;
        }

        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );

            mediaPlayer.setDataSource(contactDto.getAudioUrl());
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                isPlaying = true;
                playPauseIcon.setImageResource(R.drawable.media3_icon_pause);
                int totalDuration = mp.getDuration();
                tvTotalTime.setText(formatTime(totalDuration));
                seekBar.setMax(totalDuration);
                handler.post(updateSeekBarRunnable);
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                wasPlayingBeforeBackground = false;
                handler.removeCallbacks(updateSeekBarRunnable);
                playPauseIcon.setImageResource(R.drawable.media3_icon_play);
                seekBar.setProgress(0);
                tvCurrentTime.setText("00:00");
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e("AudioPlayer", "Error: what=" + what + ", extra=" + extra);
                isPlaying = false;
                wasPlayingBeforeBackground = false;
                handler.removeCallbacks(updateSeekBarRunnable);
                return true;
            });

        } catch (Exception e) {
            Log.e("AudioPlayer", "Exception in startAudio: " + e.getMessage());
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    tvCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(updateSeekBarRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    handler.post(updateSeekBarRunnable);
                }
            }
        });
    }

    private void toggleAudio() {
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.contact_us_popup_audio_control.getActionName());
        if (mediaPlayer == null) return;

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            playPauseIcon.setImageResource(R.drawable.media3_icon_play);
            handler.removeCallbacks(updateSeekBarRunnable);
        } else {
            mediaPlayer.start();
            isPlaying = true;
            playPauseIcon.setImageResource(R.drawable.media3_icon_pause);
            handler.post(updateSeekBarRunnable);
        }
    }

    private Runnable updateSeekBarRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                int currentPos = mediaPlayer.getCurrentPosition();
                seekBar.setProgress(currentPos);
                tvCurrentTime.setText(formatTime(currentPos));
                handler.postDelayed(this, 500);
            }
        }
    };

    // ─── Helpers ──────────────────────────────────────────────────────────────────

    private String formatTime(int millis) {
        int seconds = millis / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    // ─── Release Player ───────────────────────────────────────────────────────────

    private void releaseAudioPlayer() {
        handler.removeCallbacks(updateSeekBarRunnable);
        isPlaying = false;
        wasPlayingBeforeBackground = false;
        if (mediaPlayer != null) {
            final MediaPlayer playerToRelease = mediaPlayer;
            mediaPlayer = null;
            new Thread(() -> {
                try {
                    if (playerToRelease.isPlaying()) playerToRelease.stop();
                    playerToRelease.release();
                } catch (Exception ignored) {}
            }).start();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        releaseAudioPlayer();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        releaseAudioPlayer();
        if (!handledByButton && listner != null) {      // ← ADD BLOCK
            listner.onBottomSheetDismissed();
        }
    }


    // ─── Interface ────────────────────────────────────────────────────────────────

    public interface ContactBottomSheetListner {
        void onPaymentOptionClick();
        void onCloseButtonClick();
        void onBottomSheetDismissed();
    }

}