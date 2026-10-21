package arivihan.technologies.doubtbuzzter2.manager;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import arivihan.technologies.doubtbuzzter2.database.dao.NotesDao;
import arivihan.technologies.doubtbuzzter2.database.AppDatabase;
import arivihan.technologies.doubtbuzzter2.model.offline.notes.NoteEntity;
import arivihan.technologies.doubtbuzzter2.notification.DownloadNotification;
import arivihan.technologies.doubtbuzzter2.utils.PdfUtils;

public class PdfDownloadService extends Service {

    private static final String TAG = PdfDownloadService.class.getSimpleName();
    public static final String ACTION_START = "notes.download.START";
    public static final String ACTION_CANCEL = "notes.download.CANCEL";
    public static final String EXTRA_NOTE_ID = "extra_note_id";
    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String NOTE_ENTITY = "note_entity";

    private final AtomicBoolean cancelFlag = new AtomicBoolean(false);
    private NotesDao notesDao;
//    private int notifId = (int) System.currentTimeMillis();

    @Override
    public void onCreate() {
        super.onCreate();
        notesDao = AppDatabase.getDatabase(getApplicationContext()).notesDao();
        DownloadNotification.ensureChannel(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            try {
                startForeground(1, DownloadNotification.base(this, "Preparing…").build());
                stopSelf();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return START_NOT_STICKY;
        }

        String action = intent.getAction();
        if (ACTION_CANCEL.equals(action)) {
            cancelFlag.set(true);
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }

        if (ACTION_START.equals(action)) {
            String noteId = intent.getStringExtra(EXTRA_NOTE_ID);
            String url = intent.getStringExtra(EXTRA_URL);
            String title = intent.getStringExtra(EXTRA_TITLE);
            NoteEntity noteEntity = (NoteEntity) intent.getSerializableExtra(NOTE_ENTITY);

            int notificationId = StringUtils.isNotEmpty(noteId) ? noteId.hashCode() : 1;
            NotificationCompat.Builder nb = DownloadNotification.base(this, "Downloading: " + title)
                    .setContentText("Starting…")
                    .setProgress(0, 0, true);

            startForeground(notificationId, nb.build());

            if (noteId == null || url == null) {
                stopSelf();
                return START_NOT_STICKY;
            }

            Intent cancelIntent = new Intent(this, PdfDownloadService.class).setAction(ACTION_CANCEL);
            PendingIntent piCancel = PendingIntent.getService(
                    this, notificationId, cancelIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            nb.addAction(0, "Cancel", piCancel);

            new Thread(() -> {
                notesDao.insert(noteEntity);
                notesDao.updateStatus(noteId, NoteEntity.STATUS_IN_QUEUE);
                notesDao.updateProgress(0, noteId);
            }).start();

            new Thread(() -> runDownload(noteId, url, title, noteEntity, nb, notificationId)).start();
        }
        return START_NOT_STICKY;
    }

    private void runDownload(String noteId, String urlStr, String title, NoteEntity noteEntity, NotificationCompat.Builder nb, int notificationId) {
        File outDir = new File(getFilesDir(), "notes");
        if (!outDir.exists()) outDir.mkdirs();
        File tmp = new File(outDir, noteId + ".part");
        File dest = new File(outDir, noteId + ".pdf");

        Log.d(TAG, "Preparing to download Note ID: " + noteId + ", URL: " + urlStr);

//        NoteEntity entity = notesDao.getNotesById(noteId);
////        if (entity == null) {
////            Log.d(TAG, "NoteEntity not found in DB. Inserting new entity for ID: " + noteId);
////            entity = noteEntity;
////            notesDao.insert(entity);
////        } else {
////            Log.d(TAG, "NoteEntity found in DB for ID: " + noteId);
////        }
//        notesDao.insert(entity);

        // DOWNLOADING
        notesDao.updateStatus(noteId, NoteEntity.STATUS_DOWNLOADING);
        notesDao.updateProgress(0, noteId);

        HttpURLConnection conn = null;
        InputStream in = null;
        FileOutputStream out = null;

        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);
            conn.connect();

            int code = conn.getResponseCode();
            if (code / 100 != 2) throw new IOException("HTTP " + code);

            int contentLength = conn.getContentLength(); // may be -1
            in = new BufferedInputStream(conn.getInputStream());
            out = new FileOutputStream(tmp);

            byte[] buf = new byte[16 * 1024];
            long total = 0;
            long lastUi = 0;
            int read;

            // making progress
            nb.setProgress(100, 0, contentLength <= 0);
            updateNotification(nb.setContentText("Downloading…"), notificationId);

            while (!cancelFlag.get() && (read = in.read(buf)) != -1) {
                out.write(buf, 0, read);
                total += read;

                if (contentLength > 0) {
                    int pct = (int) ((total * 100L) / contentLength);
                    long now = SystemClock.uptimeMillis();
                    if (pct == 100 || now - lastUi > 500) {
                        lastUi = now;
                        notesDao.updateProgress(pct, noteId);
                        updateNotification(nb.setProgress(100, pct, false)
                                .setContentText(pct + "%"), notificationId);
                    }
                }
            }
            out.flush();

            if (cancelFlag.get()) throw new IOException("Canceled");

            // Move to final file
            if (dest.exists()) dest.delete();
            if (!tmp.renameTo(dest)) throw new IOException("Rename failed");

            long size = dest.length();
            int pages = PdfUtils.getPageCount(dest);

            Log.d(TAG, "File Info: " + size + " " + pages);
            // Update DB
            notesDao.updateFileMeta(noteId, size, pages, dest.getAbsolutePath());
            notesDao.updateProgress(100, noteId);
            notesDao.updateStatus(noteId, NoteEntity.STATUS_COMPLETED);

            NotificationCompat.Builder done = DownloadNotification.base(this, "Download complete: " + title)
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .setContentText("Saved")
                    .setOngoing(false);
            updateNotification(done, notificationId);

            // Final notification
//            updateNotification(
//                    nb.setOngoing(false)
//                            .setSmallIcon(android.R.drawable.stat_sys_download_done)
//                            .setContentText("Download complete")
//                            .setProgress(0, 0, false), notificationId
//            );
        } catch (Exception e) {
            e.printStackTrace();
            // Failed
            notesDao.updateStatus(noteId, NoteEntity.STATUS_FAILED);
            notesDao.updateProgress(0, noteId);
            if (tmp.exists()) tmp.delete();

            updateNotification(
                    nb.setOngoing(false)
                            .setSmallIcon(android.R.drawable.stat_notify_error)
                            .setContentText("Download failed")
                            .setProgress(0, 0, false), notificationId
            );
        } finally {
            try {
                if (in != null) in.close();
            } catch (Exception ignored) {
            }
            try {
                if (out != null) out.close();
            } catch (Exception ignored) {
            }
            if (conn != null) conn.disconnect();
            stopForeground(false);
            stopSelf();
        }
    }

    private void updateNotification(NotificationCompat.Builder nb, int notificationId) {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(notificationId, nb.build());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}