/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package arivihan.technologies.doubtbuzzter2.manager;


import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.UPDATE_DOWNLOAD;
import static arivihan.technologies.doubtbuzzter2.manager.DemoUtil.DOWNLOAD_NOTIFICATION_CHANNEL_ID;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.media3.common.util.NotificationUtil;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.exoplayer.offline.Download;
import androidx.media3.exoplayer.offline.DownloadManager;
import androidx.media3.exoplayer.offline.DownloadNotificationHelper;
import androidx.media3.exoplayer.offline.DownloadService;
import androidx.media3.exoplayer.scheduler.PlatformScheduler;
import androidx.media3.exoplayer.scheduler.Requirements;
import androidx.media3.exoplayer.scheduler.Scheduler;

import com.android.volley.Request;
import com.android.volley.VolleyError;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.MyDownloadActivity;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.database.AppDatabase;
import arivihan.technologies.doubtbuzzter2.model.DownloadStatus;
import arivihan.technologies.doubtbuzzter2.model.MediaEntity;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;


/**
 * A service for downloading media.
 */
@UnstableApi
public class OfflineVideoDownloadService extends DownloadService {

    private static final String TAG = OfflineVideoDownloadService.class.getSimpleName();
    private static final int JOB_ID = 1;
    private static final int FOREGROUND_NOTIFICATION_ID = 1;

    @OptIn(markerClass = UnstableApi.class)
    public OfflineVideoDownloadService() {
        super(
                FOREGROUND_NOTIFICATION_ID,
                DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
                DOWNLOAD_NOTIFICATION_CHANNEL_ID,
                R.string.exo_download_notification_channel_name,
                /* channelDescriptionResourceId= */ 0);
    }

    @NonNull
    @Override
    protected DownloadManager getDownloadManager() {
        // This will only happen once, because getDownloadManager is guaranteed to be called only once
        // in the life cycle of the process.
        DownloadManager downloadManager = DemoUtil.getDownloadManager(/* context= */ this);
        DownloadNotificationHelper downloadNotificationHelper =
                DemoUtil.getDownloadNotificationHelper(/* context= */ this);

//        downloadManager.addListener(new DownloadManager.Listener() {
//            @Override
//            public void onDownloadChanged(@NonNull DownloadManager manager, @NonNull Download download, @Nullable Exception finalException) {
//                if (manager.getCurrentDownloads().isEmpty()) {
//                    stopForeground(true);
//                    stopSelf();
//                }
//            }
//
//            @Override
//            public void onDownloadRemoved(@NonNull DownloadManager manager, @NonNull Download download) {
//                if (manager.getCurrentDownloads().isEmpty()) {
//                    stopForeground(true);
//                    stopSelf();
//                }
//            }
//        });


        downloadManager.addListener(
                new TerminalStateNotificationHelper(
                        this, downloadNotificationHelper, FOREGROUND_NOTIFICATION_ID + 1));
        return downloadManager;
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected Scheduler getScheduler() {
        return Util.SDK_INT >= 21 ? new PlatformScheduler(this, JOB_ID) : null;
    }

    @NonNull
    @Override
    protected Notification getForegroundNotification(@NonNull List<Download> downloads, int notMetRequirements) {
        Intent intent = new Intent(this, MyDownloadActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (!downloads.isEmpty()) {
            for (Download download : downloads) {
                if (notMetRequirements != 0) {
                    MyApplication.getInstance().getMediaViewModel().updateById(download.request.id, DownloadStatus.DOWNLOAD_PAUSED.name());
                } else if (download.state == Download.STATE_DOWNLOADING) {
                    MyApplication.getInstance().getMediaViewModel().updateProgress((int) download.getPercentDownloaded(), download.request.id);
                }
            }
        }

        return DemoUtil.getDownloadNotificationHelper(this)
                .buildProgressNotification(
                        this,
                        R.mipmap.ic_launcher_square,
                        pendingIntent,
                        !downloads.isEmpty() ? Util.fromUtf8Bytes(downloads.get(0).request.data) : "",
                        !downloads.isEmpty() ? Collections.singletonList(downloads.get(0)) : downloads,
                        notMetRequirements
                );
    }

    /**
     * Creates and displays notifications for downloads when they complete or fail.
     *
     * <p>This helper will outlive the lifespan of a single instance of {@link OfflineVideoDownloadService}.
     * It is static to avoid leaking the first {@link OfflineVideoDownloadService} instance.
     */
    private final class TerminalStateNotificationHelper implements DownloadManager.Listener {

        private final Context context;
        private final DownloadNotificationHelper notificationHelper;

        private int nextNotificationId;

        public TerminalStateNotificationHelper(
                OfflineVideoDownloadService context, DownloadNotificationHelper notificationHelper, int firstNotificationId) {
            this.context = context;
            this.notificationHelper = notificationHelper;
            nextNotificationId = firstNotificationId;
        }

        @Override
        public void onDownloadChanged(@NonNull DownloadManager downloadManager, @NonNull Download download, @Nullable Exception finalException) {
            try {
                DownloadManager.Listener.super.onDownloadChanged(downloadManager, download, finalException);
                Intent intent = new Intent(context, MyDownloadActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
                Notification notification;
                if (download.state == Download.STATE_COMPLETED) {
                    MyApplication.getInstance().getMediaViewModel().
                            updateById(download.request.id, DownloadStatus.DOWNLOAD_COMPLETED.name());
                    Log.d(TAG, "onDownloadChanged: " + download.request.id);
                    notification =
                            notificationHelper.buildDownloadCompletedNotification(
                                    context,
                                    R.drawable.exo_ic_check,
                                    pendingIntent,
                                    Util.fromUtf8Bytes(download.request.data));
                    updateDownloadData(MyApplication.getLongData(download.request.id));
                    MyApplication.saveData(download.request.id, null);
                } else if (download.state == Download.STATE_FAILED || download.state == Download.STATE_STOPPED) {
                    notification =
                            notificationHelper.buildDownloadFailedNotification(
                                    context,
                                    R.mipmap.ic_launcher_round,
                                    pendingIntent,
                                    Util.fromUtf8Bytes(download.request.data));
                    MyApplication.getInstance().getMediaViewModel().updateById(download.request.id, DownloadStatus.DOWNLOAD_FAILED.name());
                    MyApplication.getInstance().getMediaViewModel().updateProgress(0, download.request.id);

                } else if (download.state == Download.STATE_DOWNLOADING) {
                    MyApplication.getInstance()
                            .getMediaViewModel()
                            .updateById(download.request.id, DownloadStatus.DOWNLOAD_RESUMED.name());
                    MyApplication.getInstance().getMediaViewModel().updateProgress((int) download.getPercentDownloaded(), download.request.id);
                    return;
                } else if (download.state == Download.STATE_QUEUED) {
                    MyApplication.getInstance()
                            .getMediaViewModel()
                            .updateById(download.request.id, DownloadStatus.DOWNLOAD_QUEUED.name());
                    return;
                } else {
                    return;
                }
                NotificationUtil.setNotification(context, nextNotificationId++, notification);

                try {
                    if (downloadManager.getCurrentDownloads().isEmpty()) {
                        if (context instanceof OfflineVideoDownloadService) {
                            OfflineVideoDownloadService service = (OfflineVideoDownloadService) context;
                            service.stopForeground(true);
                            service.stopSelf();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } catch (Exception exception) {
                MyApplication.getInstance().getMediaViewModel().deleteByUrl(download.request.id);
                DemoUtil.getDownloadManager(context).removeDownload(download.request.id);
            }
        }


        public void updateDownloadData(Long startTime) {
            if (startTime > 0) {
                try {
                    Map<String, Object> map = new HashMap<>();
                    map.put("startTime", startTime);
                    MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.GET,
                            CommonMethods.getUrlParam(UPDATE_DOWNLOAD, map), null,
                            response -> {
                                Log.d("updateDownloadData", "response ");
                            }, new VolleyGlobalErrorListener(context) {
                        @Override
                        public void postError(VolleyError error) {
                            Log.d("updateDownloadData", "postError " + error.getMessage());
                        }
                    });
                    MyVolleySingleton.getInstance(context).addToRequestQueue(myJsonObjectRequest);
                } catch (Exception e) {
                    Log.d("updateDownloadData", "JSONException " + e.getMessage());
                }
            }
        }
    }
}
