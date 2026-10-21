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

import static com.google.common.base.Preconditions.checkNotNull;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.fragment.app.FragmentManager;
import androidx.media3.common.DrmInitData;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.common.Tracks;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.datasource.DataSource;
import androidx.media3.exoplayer.RenderersFactory;
import androidx.media3.exoplayer.drm.DrmSession;
import androidx.media3.exoplayer.drm.DrmSessionEventListener;
import androidx.media3.exoplayer.drm.OfflineLicenseHelper;
import androidx.media3.exoplayer.offline.Download;
import androidx.media3.exoplayer.offline.DownloadCursor;
import androidx.media3.exoplayer.offline.DownloadHelper;
import androidx.media3.exoplayer.offline.DownloadIndex;
import androidx.media3.exoplayer.offline.DownloadManager;
import androidx.media3.exoplayer.offline.DownloadRequest;
import androidx.media3.exoplayer.offline.DownloadService;
import androidx.media3.exoplayer.source.TrackGroupArray;
import androidx.media3.exoplayer.trackselection.MappingTrackSelector;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.DownloadStatus;

/**
 * Tracks media that has been downloaded.
 */
@UnstableApi
public class DownloadTracker {

    /**
     * Listens for changes in the tracked downloads.
     */
    public interface Listener {

        /**
         * Called when the tracked downloads changed.
         */
        void onDownloadsChanged();
    }

    private static final String TAG = DownloadTracker.class.getSimpleName();

    private final Context context;
    private final DataSource.Factory dataSourceFactory;
    private final CopyOnWriteArraySet<Listener> listeners;
    private final HashMap<Uri, Download> downloads;
    private final DownloadIndex downloadIndex;

    @Nullable
    private StartDownloadDialogHelper startDownloadDialogHelper;

    public DownloadTracker(
            Context context, DataSource.Factory dataSourceFactory, DownloadManager downloadManager) {
        this.context = context.getApplicationContext();
        this.dataSourceFactory = dataSourceFactory;
        listeners = new CopyOnWriteArraySet<>();
        downloads = new HashMap<>();
        downloadIndex = downloadManager.getDownloadIndex();
        downloadManager.addListener(new DownloadManagerListener());
        loadDownloads();
    }

    public void addListener(Listener listener) {
        listeners.add(checkNotNull(listener));
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public boolean isDownloaded(MediaItem mediaItem) {
        @Nullable Download download = downloads.get(checkNotNull(mediaItem.localConfiguration).uri);
        return download != null && download.state != Download.STATE_FAILED;
    }

    @Nullable
    public DownloadRequest getDownloadRequest(Uri uri) {
        @Nullable Download download = downloads.get(uri);
        return download != null && download.state != Download.STATE_FAILED ? download.request : null;
    }

    public Download getDownload(Uri uri) {
        return downloads.get(uri);
    }

    public void toggleDownload(MediaItem mediaItem, RenderersFactory renderersFactory) {
        @Nullable Download download = downloads.get(checkNotNull(mediaItem.localConfiguration).uri);
        if (download != null && download.state != Download.STATE_FAILED) {
            DownloadService.sendRemoveDownload(
                    context, OfflineVideoDownloadService.class, download.request.id, /* foreground= */ false);
        } else {
            if (startDownloadDialogHelper != null) {
                startDownloadDialogHelper.release();
                startDownloadDialogHelper = null;
            }

            DownloadHelper downloadHelper =
                    DownloadHelper.forMediaItem(context, mediaItem, renderersFactory, dataSourceFactory);

            startDownloadDialogHelper = new StartDownloadDialogHelper(downloadHelper, mediaItem);
        }
    }

    private void loadDownloads() {
        try (DownloadCursor loadedDownloads = downloadIndex.getDownloads()) {
            while (loadedDownloads.moveToNext()) {
                Download download = loadedDownloads.getDownload();
                downloads.put(download.request.uri, download);
            }
        } catch (IOException e) {
            Log.w(TAG, "Failed to query downloads", e);
        }
    }

    private class DownloadManagerListener implements DownloadManager.Listener {

        @Override
        public void onDownloadChanged(
                DownloadManager downloadManager, Download download, @Nullable Exception finalException) {
            downloads.put(download.request.uri, download);
            for (Listener listener : listeners) {
                listener.onDownloadsChanged();
            }
        }

        @Override
        public void onDownloadRemoved(DownloadManager downloadManager, Download download) {
            downloads.remove(download.request.uri);
            for (Listener listener : listeners) {
                listener.onDownloadsChanged();
            }
        }
    }

    @UnstableApi
    private final class StartDownloadDialogHelper
            implements DownloadHelper.Callback,
            DialogInterface.OnDismissListener {

        private final DownloadHelper downloadHelper;
        private final MediaItem mediaItem;

        private WidevineOfflineLicenseFetchTask widevineOfflineLicenseFetchTask;
        @Nullable
        private byte[] keySetId;

        public StartDownloadDialogHelper(
                DownloadHelper downloadHelper, MediaItem mediaItem) {
            this.downloadHelper = downloadHelper;
            this.mediaItem = mediaItem;
            downloadHelper.prepare(this);
        }

        public void release() {
            downloadHelper.release();
            if (widevineOfflineLicenseFetchTask != null) {
                widevineOfflineLicenseFetchTask.cancel(false);
                widevineOfflineLicenseFetchTask = null;
            }
        }


        // DownloadHelper.Callback implementation.

        @Override
        public void onPrepared(DownloadHelper helper, boolean tracksInfoAvailable) {
            @Nullable Format format = getFirstFormatWithDrmInitData(helper);
            if (format == null) {
                onDownloadPrepared(helper);
                return;
            }

            // The content is DRM protected. We need to acquire an offline license.
            if (Util.SDK_INT < 18) {
                Toast.makeText(context, R.string.error_drm_unsupported_before_api_18, Toast.LENGTH_LONG)
                        .show();
                Log.e(TAG, "Downloading DRM protected content is not supported on API versions below 18");
                return;
            }
            // TODO(internal b/163107948): Support cases where DrmInitData are not in the manifest.
            if (!hasSchemaData(format.drmInitData)) {
                Toast.makeText(context, R.string.download_start_error_offline_license, Toast.LENGTH_LONG)
                        .show();
                Log.e(
                        TAG,
                        "Downloading content where DRM scheme data is not located in the manifest is not"
                                + " supported");
                return;
            }
            widevineOfflineLicenseFetchTask =
                    new WidevineOfflineLicenseFetchTask(
                            format,
                            mediaItem.localConfiguration.drmConfiguration,
                            dataSourceFactory,
                            /* dialogHelper= */ this,
                            helper);
            widevineOfflineLicenseFetchTask.execute();
        }

        @Override
        public void onPrepareError(DownloadHelper helper, IOException e) {
            boolean isLiveContent = e instanceof DownloadHelper.LiveContentUnsupportedException;
            int toastStringId =
                    isLiveContent ? R.string.download_live_unsupported : R.string.download_start_error;
            String logMessage =
                    isLiveContent ? "Downloading live content unsupported" : "Failed to start download.";
            Toast.makeText(context, toastStringId, Toast.LENGTH_LONG).show();
            MyApplication.getInstance().getMediaViewModel().updateById(mediaItem.mediaId, DownloadStatus.DOWNLOAD_FAILED.name());
            Log.e(TAG, logMessage, e);
        }

        // TrackSelectionListener implementation.


        // DialogInterface.OnDismissListener implementation.

        @Override
        public void onDismiss(DialogInterface dialogInterface) {
            downloadHelper.release();
        }

        // Internal methods.

        /**
         * Returns the first {@link Format} with a non-null {@link Format#drmInitData} found in the
         * content's tracks, or null if none is found.
         */
        @Nullable
        private Format getFirstFormatWithDrmInitData(DownloadHelper helper) {
            for (int periodIndex = 0; periodIndex < helper.getPeriodCount(); periodIndex++) {
                MappingTrackSelector.MappedTrackInfo mappedTrackInfo = helper.getMappedTrackInfo(periodIndex);
                for (int rendererIndex = 0;
                     rendererIndex < mappedTrackInfo.getRendererCount();
                     rendererIndex++) {
                    TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(rendererIndex);
                    for (int trackGroupIndex = 0; trackGroupIndex < trackGroups.length; trackGroupIndex++) {
                        TrackGroup trackGroup = trackGroups.get(trackGroupIndex);
                        for (int formatIndex = 0; formatIndex < trackGroup.length; formatIndex++) {
                            Format format = trackGroup.getFormat(formatIndex);
                            if (format.drmInitData != null) {
                                return format;
                            }
                        }
                    }
                }
            }
            return null;
        }

        private void onOfflineLicenseFetched(DownloadHelper helper, byte[] keySetId) {
            this.keySetId = keySetId;
            onDownloadPrepared(helper);
        }

        private void onOfflineLicenseFetchedError(DrmSession.DrmSessionException e) {
            Toast.makeText(context, R.string.download_start_error_offline_license, Toast.LENGTH_LONG)
                    .show();
            Log.e(TAG, "Failed to fetch offline DRM license", e);
        }

        @OptIn(markerClass = UnstableApi.class)
        private void onDownloadPrepared(DownloadHelper helper) {
            if (helper.getPeriodCount() == 0) {
                Log.d(TAG, "No periods found. Downloading entire stream.");
                startDownload();
                downloadHelper.release();
                return;
            }

            startDownload();
            downloadHelper.release();

//        Tracks tracks = downloadHelper.getTracks(/* periodIndex= */ 0);
//        if (!TrackSelectionDialog.willHaveContent(tracks)) {
//          Log.d(TAG, "No dialog content. Downloading entire stream.");
//        startDownload();
//        downloadHelper.release();
//      }
        }

        /**
         * Returns whether any the {@link DrmInitData.SchemeData} contained in {@code drmInitData} has
         * non-null {@link DrmInitData.SchemeData#data}.
         */
        @OptIn(markerClass = UnstableApi.class)
        private boolean hasSchemaData(DrmInitData drmInitData) {
            for (int i = 0; i < drmInitData.schemeDataCount; i++) {
                if (drmInitData.get(i).hasData()) {
                    return true;
                }
            }
            return false;
        }

        private void startDownload() {
            try {
                startDownload(buildDownloadRequest());
            } catch (Exception e) {
                Log.e("DownloadTracker", "Failed to start download", e);
            }
        }

        @OptIn(markerClass = UnstableApi.class)
        private void startDownload(DownloadRequest downloadRequest) {
            if (downloads.containsKey(downloadRequest.uri)) {
//                Log.w("DOWNLOAD_TRACKER",
//                        "Download already exists for: " + downloadRequest.uri);
                return;
            }
//            Log.d("DOWNLOAD_TRACKER",
//                    "Starting download: " + new String(downloadRequest.data));
            addDownload(downloadRequest);
        }

        /**
         * Starts the download service in foreground mode so the platform grants it the foreground
         * allow-list that comes with startForegroundService(). Started in background mode instead,
         * the service later hits the startForeground() that media3 issues the first time a download
         * changes state - typically once the user has already left the screen - which Android 12+
         * refuses with ForegroundServiceStartNotAllowedException. media3 rethrows that on the main
         * looper, where it becomes an uncaught exception and takes the whole process down.
         */
        @OptIn(markerClass = UnstableApi.class)
        private void addDownload(DownloadRequest downloadRequest) {
            try {
                DownloadService.sendAddDownload(
                        context,
                        OfflineVideoDownloadService.class,
                        downloadRequest,
                        /* foreground= */ true);
            } catch (RuntimeException e) {
                // Starting a foreground service is itself refused if the app has already dropped to
                // the background. Queue it instead: DownloadManager persists the request and the
                // scheduler resumes it, so the download survives even though this start was denied.
                Log.w(TAG, "Foreground download start refused, queueing instead", e);
                try {
                    DownloadService.sendAddDownload(
                            context,
                            OfflineVideoDownloadService.class,
                            downloadRequest,
                            /* foreground= */ false);
                } catch (RuntimeException fallbackFailure) {
                    Log.w(TAG, "Unable to queue download", fallbackFailure);
                }
            }
        }

        @OptIn(markerClass = UnstableApi.class)
        private DownloadRequest buildDownloadRequest() {
            String title;
            if (mediaItem != null && mediaItem.mediaMetadata.title != null) {
                title = mediaItem.mediaMetadata.title.toString();
            } else {
                title = "untitled_media";
                Log.w("DownloadTracker", "Media title is null. Using fallback title: " + title);
            }
            return downloadHelper
                    .getDownloadRequest(Util.getUtf8Bytes(title))
                    .copyWithKeySetId(keySetId);
        }
    }

    /**
     * Downloads a Widevine offline license in a background thread.
     */
    @UnstableApi
    private static final class WidevineOfflineLicenseFetchTask extends AsyncTask<Void, Void, Void> {

        private final Format format;
        private final MediaItem.DrmConfiguration drmConfiguration;
        private final DataSource.Factory dataSourceFactory;
        private final StartDownloadDialogHelper dialogHelper;
        private final DownloadHelper downloadHelper;

        @Nullable
        private byte[] keySetId;
        @Nullable
        private DrmSession.DrmSessionException drmSessionException;

        public WidevineOfflineLicenseFetchTask(
                Format format,
                MediaItem.DrmConfiguration drmConfiguration,
                DataSource.Factory dataSourceFactory,
                StartDownloadDialogHelper dialogHelper,
                DownloadHelper downloadHelper) {
            this.format = format;
            this.drmConfiguration = drmConfiguration;
            this.dataSourceFactory = dataSourceFactory;
            this.dialogHelper = dialogHelper;
            this.downloadHelper = downloadHelper;
        }

        @OptIn(markerClass = UnstableApi.class)
        @Override
        protected Void doInBackground(Void... voids) {
            OfflineLicenseHelper offlineLicenseHelper =
                    OfflineLicenseHelper.newWidevineInstance(
                            drmConfiguration.licenseUri.toString(),
                            drmConfiguration.forceDefaultLicenseUri,
                            dataSourceFactory,
                            drmConfiguration.licenseRequestHeaders,
                            new DrmSessionEventListener.EventDispatcher());
            try {
                keySetId = offlineLicenseHelper.downloadLicense(format);
            } catch (DrmSession.DrmSessionException e) {
                drmSessionException = e;
            } finally {
                offlineLicenseHelper.release();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (drmSessionException != null) {
                dialogHelper.onOfflineLicenseFetchedError(drmSessionException);
            } else {
                dialogHelper.onOfflineLicenseFetched(downloadHelper, checkNotNull(keySetId));
            }
        }
    }
}
