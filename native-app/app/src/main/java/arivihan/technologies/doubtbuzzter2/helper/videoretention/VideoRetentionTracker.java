package arivihan.technologies.doubtbuzzter2.helper.videoretention;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.DIRECT_VIDEO_PROGRESS;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.Request;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureContentDto;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;

/**
 * Tracks the user's active watch time on a video and fires the
 * direct-video-progress API exactly once per video (persisted across sessions).
 *
 * <p>Every public method is internally try-catch guarded so this class
 * can <b>never</b> crash the host Activity.</p>
 *
 * <p>Use the factory method {@link #create(Context, MicroLectureContentDto)}
 * instead of the constructor — it returns {@code null} on any init failure.</p>
 */
public final class VideoRetentionTracker {

    private static final String TAG = "VideoRetentionTracker";
    private static final String PREF_NAME = "video_retention_progress";
    private static final String KEY_PREFIX = "retention_done_";

    private final MicroLectureContentDto contentDto;
    private final Context appContext;
    private final String videoKey;

    private long accumulatedActiveMs;
    private long activeSegmentStartRealtime = -1;

    private boolean apiSucceeded;
    private boolean apiInFlight;
    private boolean thresholdReached;

    private final long thresholdPercent;
    private final long videoDurationMs;

    // ---- Factory (crash-safe entry point) ----

    /**
     * Creates a tracker instance. Returns {@code null} if anything goes wrong
     * during initialisation (bad data, corrupt prefs, etc.) so the caller
     * can simply null-check without ever catching.
     */
    @Nullable
    public static VideoRetentionTracker create(Context context, MicroLectureContentDto contentDto) {
        try {
            if (context == null || contentDto == null) return null;

            // Kill-switch: set "disable_video_retention_tracking" to true in
            // Firebase Remote Config to disable this feature entirely.
            boolean disabled = MyApplication.getFirebaseRemoteConfig()
                    .getBoolean("disable_video_retention_tracking");
            if (disabled) {
                Log.d(TAG, "Video retention tracking is disabled via remote config");
                return null;
            }

            return new VideoRetentionTracker(context, contentDto);
        } catch (Exception e) {
            Log.e(TAG, "Failed to create VideoRetentionTracker", e);
            return null;
        }
    }

    private VideoRetentionTracker(Context context, MicroLectureContentDto contentDto) {
        this.appContext = context.getApplicationContext();
        this.contentDto = contentDto;

        String chapterId = contentDto.getChapterId() != null ? contentDto.getChapterId() : "";
        String mlId = contentDto.getId() != null ? contentDto.getId() : "";
        this.videoKey = KEY_PREFIX + chapterId + "_" + mlId;

        this.apiSucceeded = isAlreadyReported();

        long remoteThreshold = 30;
        try {
            remoteThreshold = MyApplication.getFirebaseRemoteConfig()
                    .getLong("video_retention_threshold_percent");
        } catch (Exception ignored) { }
        this.thresholdPercent = (remoteThreshold > 0 && remoteThreshold <= 100) ? remoteThreshold : 30;

        long endPos = contentDto.getVideoCompletionEndPos();
        long startPos = contentDto.getDefaultStartTime();
        this.videoDurationMs = Math.max(endPos - startPos, 0);

        Log.d(TAG, "init: videoKey=" + videoKey
                + ", duration=" + videoDurationMs + "ms"
                + ", threshold=" + thresholdPercent + "%"
                + ", alreadyReported=" + apiSucceeded);
    }

    // ---- Public API (every method is crash-safe) ----

    public void onPlaybackStarted() {
        try {
            if (apiSucceeded) return;
            if (activeSegmentStartRealtime < 0) {
                activeSegmentStartRealtime = SystemClock.elapsedRealtime();
            }
        } catch (Exception e) {
            Log.e(TAG, "onPlaybackStarted error", e);
        }
    }

    public void onPlaybackPaused() {
        try {
            if (apiSucceeded) return;
            flushActiveSegment();
        } catch (Exception e) {
            Log.e(TAG, "onPlaybackPaused error", e);
        }
    }

    public void tick() {
        try {
            if (apiSucceeded || videoDurationMs <= 0) return;

            long currentActive = accumulatedActiveMs;
            if (activeSegmentStartRealtime >= 0) {
                currentActive += SystemClock.elapsedRealtime() - activeSegmentStartRealtime;
            }

            long requiredMs = (videoDurationMs * thresholdPercent) / 100;

            if (currentActive >= requiredMs) {
                thresholdReached = true;
                if (!apiInFlight) {
                    flushActiveSegment();
                    Log.d(TAG, "Threshold reached: " + currentActive + "ms >= " + requiredMs + "ms. Firing API.");
                    fireRetentionApi(currentActive);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "tick error", e);
        }
    }

    public void release() {
        try {
            flushActiveSegment();
        } catch (Exception e) {
            Log.e(TAG, "release error", e);
        }
    }

    public boolean isApiSucceeded() {
        return apiSucceeded;
    }

    // ---- Internals ----

    private boolean isAlreadyReported() {
        try {
            return getPrefs().getBoolean(videoKey, false);
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences read error", e);
            return false;
        }
    }

    @Nullable
    private SharedPreferences getPrefs() {
        return appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private void markVideoAsReported() {
        try {
            getPrefs().edit().putBoolean(videoKey, true).apply();
        } catch (Exception e) {
            Log.e(TAG, "SharedPreferences write error", e);
        }
    }

    private void flushActiveSegment() {
        if (activeSegmentStartRealtime >= 0) {
            accumulatedActiveMs += SystemClock.elapsedRealtime() - activeSegmentStartRealtime;
            activeSegmentStartRealtime = -1;
        }
    }

    private void fireRetentionApi(long activeWatchTimeMs) {
        apiInFlight = true;
        try {
            String chapterId = contentDto.getChapterId();
            String microLectureId = contentDto.getId();

            int retentionPercent = (int) ((activeWatchTimeMs * 100) / videoDurationMs);

            JSONObject body = new JSONObject();
            body.put("chapterId", chapterId != null ? chapterId : "");
            body.put("microLectureId", microLectureId != null ? microLectureId : JSONObject.NULL);
            body.put("progress", Math.min(retentionPercent, 100));

            String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
            if (lang != null && (lang.contains("hi") || lang.equals("hi-rIN"))) {
                body.put("language", "HINDI");
            } else {
                body.put("language", "ENGLISH");
            }

            Map<String, Object> queryParams = new HashMap<>();
            String batchId = MyApplication.getStringData(PreferenceKeys.BATCH_ID.getKey());
            if (batchId != null && !batchId.isEmpty()) {
                queryParams.put("batchId", batchId);
            }

            String url = queryParams.isEmpty()
                    ? DIRECT_VIDEO_PROGRESS
                    : CommonMethods.getUrlParam(DIRECT_VIDEO_PROGRESS, queryParams);

            Log.d(TAG, "fireRetentionApi: url=" + url + ", body=" + body);

            MyJsonObjectRequest request = new MyJsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    response -> {
                        try {
                            apiInFlight = false;
                            apiSucceeded = true;
                            markVideoAsReported();
                            Log.d(TAG, "Retention API success for " + videoKey);
                        } catch (Exception e) {
                            Log.e(TAG, "Success callback error", e);
                        }
                    },
                    error -> {
                        try {
                            apiInFlight = false;
                            Log.e(TAG, "Retention API failed for " + videoKey
                                    + ", will retry. Error: "
                                    + (error != null ? error.getMessage() : "null"));
                        } catch (Exception e) {
                            Log.e(TAG, "Error callback error", e);
                        }
                    }
            );
            request.setTag(TAG);
            MyVolleySingleton.getInstance(appContext).addToRequestQueue(request);

        } catch (Exception e) {
            apiInFlight = false;
            Log.e(TAG, "fireRetentionApi error", e);
        }
    }
}
