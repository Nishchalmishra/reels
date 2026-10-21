package arivihan.technologies.doubtbuzzter2.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;

public class AudioManager {

    private static AudioManager instance;
    private MediaPlayer mediaPlayer;
    private boolean isMuted = false;
    private boolean isRepeat = false;
    private boolean isPrepared = false;
    private String currentUrl = "";

    private AudioManager() {}

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    public boolean isMuted() { return isMuted; }
    public boolean isRepeat() { return isRepeat; }
    public void setRepeat(boolean repeat) { isRepeat = repeat; }

    public void toggleMute() {
        isMuted = !isMuted;
        if (mediaPlayer != null && isPrepared) {
            if (isMuted) {
                mediaPlayer.setVolume(0f, 0f);
            } else {
                mediaPlayer.setVolume(1f, 1f);
            }
        }
    }

    public void setMuted(boolean muted) {
        isMuted = muted;
        if (mediaPlayer != null && isPrepared) {
            mediaPlayer.setVolume(isMuted ? 0f : 1f, isMuted ? 0f : 1f);
        }
    }

    public void playAudio(String url, Context context) {
        if (url == null || url.isEmpty()) {
            release();
            return;
        }

        // same url already playing, just apply mute state
        if (url.equals(currentUrl) && mediaPlayer != null && isPrepared) {
            mediaPlayer.setVolume(isMuted ? 0f : 1f, isMuted ? 0f : 1f);
            if (!mediaPlayer.isPlaying()) mediaPlayer.start();
            return;
        }

        // new url — release old and start new
        release();
        currentUrl = url;

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );

            mediaPlayer.setDataSource(url);
            mediaPlayer.setLooping(isRepeat);
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> {
                isPrepared = true;
                mp.setVolume(isMuted ? 0f : 1f, isMuted ? 0f : 1f);
                mp.start();
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                if (isRepeat) {
                    mp.seekTo(0);
                    mp.start();
                } else {
                    isPrepared = false;
                }
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                release();
                return true;
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        if (mediaPlayer != null && isPrepared && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void resume() {
        if (mediaPlayer != null && isPrepared && !mediaPlayer.isPlaying()) {
            mediaPlayer.setVolume(isMuted ? 0f : 1f, isMuted ? 0f : 1f);
            mediaPlayer.start();
        }
    }

    public void release() {
        isPrepared = false;
        currentUrl = "";
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
}

