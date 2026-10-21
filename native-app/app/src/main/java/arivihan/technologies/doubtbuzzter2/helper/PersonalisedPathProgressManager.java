package arivihan.technologies.doubtbuzzter2.helper;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.POST_CHAPTER_PROGRESS_V2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Request;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;

/**
 * Reports per-node / per-child-task completion for the personalised learning path to
 * POST /secure/user-personalised-learning-path/v2/progress.
 *
 * <p>Each child-task activity (video, DPP, test, live class, notes) calls this when it
 * finishes / closes. The backend aggregates child-task completions into the node-level
 * percentage — e.g. completing 1 of 3 child tasks makes the node show 33%.</p>
 *
 * <p>The identifiers below are passed into every node activity from the web app via
 * {@code activityParams} (see the React {@code personalisedPathProgress} util). When
 * {@link #EXTRA_NODE_ID} is absent the activity was launched outside the path, so
 * reporting is a safe no-op and existing flows are unaffected.</p>
 */
public final class PersonalisedPathProgressManager {

    private static final String TAG = "PathProgress";

    // Intent-extra keys (must match the keys the web app injects into activityParams).
    public static final String EXTRA_PATH_ID = "learningPathId";
    public static final String EXTRA_PATH_ID_ALT = "pathId";
    public static final String EXTRA_NODE_ID = "nodeId";
    public static final String EXTRA_CHILD_TASK_ID = "childTaskId";

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_COMPLETED = "COMPLETED";

    /** A video watched at/above this percentage counts as completed. */
    public static final int VIDEO_COMPLETE_THRESHOLD = 90;

    private PersonalisedPathProgressManager() {
    }

    /**
     * Pull the path identifiers from an activity's intent and report progress.
     * No-op when the activity was not launched from the personalised path.
     */
    public static void reportFromActivity(Activity activity, String nodeStatus,
                                          int completionPercentage, int attemptCountDelta) {
        if (activity == null || activity.getIntent() == null) return;
        Intent intent = activity.getIntent();

        String nodeId = intent.getStringExtra(EXTRA_NODE_ID);
        if (StringUtils.isEmpty(nodeId)) return; // not a personalised-path launch

        String pathId = intent.getStringExtra(EXTRA_PATH_ID);
        if (StringUtils.isEmpty(pathId)) pathId = intent.getStringExtra(EXTRA_PATH_ID_ALT);
        String childTaskId = intent.getStringExtra(EXTRA_CHILD_TASK_ID);

        reportProgress(activity, pathId, nodeId, childTaskId, nodeStatus,
                completionPercentage, attemptCountDelta);
    }

    /**
     * Report a video child-task from its final position/duration (ms). Marked COMPLETED
     * once {@link #VIDEO_COMPLETE_THRESHOLD} is crossed, otherwise ACTIVE with the
     * fractional watch percentage.
     */
    public static void reportVideoFromActivity(Activity activity, long positionMs, long durationMs) {
        int pct = computePercentage(positionMs, durationMs);
        if (pct <= 0) return; // nothing watched / duration unknown — don't overwrite progress
        String status = pct >= VIDEO_COMPLETE_THRESHOLD ? STATUS_COMPLETED : STATUS_ACTIVE;
        reportFromActivity(activity, status, pct, 1);
    }

    /**
     * Report a count-based child-task (DPP / test questions). Marked COMPLETED only when
     * every item is done, otherwise ACTIVE with the completed/total percentage.
     */
    public static void reportCountFromActivity(Activity activity, int completed, int total) {
        if (total <= 0) return;
        int pct = (int) Math.round((Math.max(0, Math.min(completed, total)) * 100.0) / total);
        String status = completed >= total ? STATUS_COMPLETED : STATUS_ACTIVE;
        reportFromActivity(activity, status, pct, 1);
    }

    /** Low-level POST. Safe no-op when nodeId is missing. */
    public static void reportProgress(Context context, String pathId, String nodeId, String childTaskId,
                                      String nodeStatus, int completionPercentage, int attemptCountDelta) {
        if (context == null || StringUtils.isEmpty(nodeId)) return;
        try {
            int pct = Math.max(0, Math.min(100, completionPercentage));

            JSONObject body = new JSONObject();
            body.put("pathId", pathId == null ? "" : pathId);
            body.put("nodeId", nodeId);
            if (StringUtils.isNotEmpty(childTaskId)) {
                body.put("childTaskId", childTaskId);
            }
            body.put("nodeStatus", nodeStatus);
            body.put("completionPercentage", pct);
            body.put("attemptCountDelta", attemptCountDelta);

            Log.d(TAG, "v2/progress request: " + body);
            MyJsonObjectRequest request = new MyJsonObjectRequest(Request.Method.POST,
                    POST_CHAPTER_PROGRESS_V2, body,
                    response -> Log.d(TAG, "v2/progress response: " + response),
                    error -> Log.e(TAG, "v2/progress error: " + error));
            request.setTag(TAG);
            MyVolleySingleton.getInstance(context).addToRequestQueue(request);
        } catch (JSONException e) {
            Log.e(TAG, "v2/progress build error", e);
        }
    }

    /** Watch percentage (0..100) from a position/duration in ms; 0 when duration unknown. */
    public static int computePercentage(long positionMs, long durationMs) {
        if (durationMs <= 0) return 0;
        long clamped = Math.max(0, Math.min(positionMs, durationMs));
        return (int) Math.round((clamped * 100.0) / durationMs);
    }
}
