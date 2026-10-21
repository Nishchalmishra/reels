package arivihan.technologies.doubtbuzzter2.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.video_screens.pdf.MockDemoActivity;
import arivihan.technologies.doubtbuzzter2.activity.PdfViewerActivity;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.helper.RemoteConfigUtils;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.manager.PdfDownloadService;
import arivihan.technologies.doubtbuzzter2.model.offline.notes.NoteEntity;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public final class PdfUtils {

    private static final String TAG = PdfUtils.class.getSimpleName();

    public static int getPageCount(@NonNull File pdfFile) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return 0;
        ParcelFileDescriptor pfd = null;
        PdfRenderer renderer = null;
        try {
            pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
            renderer = new PdfRenderer(pfd);
            return renderer.getPageCount();
        } catch (Exception ignore) {
            return 0;
        } finally {
            try {
                if (renderer != null) renderer.close();
            } catch (Exception ignored) {
            }
            try {
                if (pfd != null) pfd.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static void startPdfDownload(Context ctx, NoteEntity note) {
        if (StringUtils.isNotEmpty(note.getNotesUrl())) {
            Intent i = new Intent(ctx, PdfDownloadService.class)
                    .setAction(PdfDownloadService.ACTION_START)
                    .putExtra(PdfDownloadService.EXTRA_NOTE_ID, note.getId())
                    .putExtra(PdfDownloadService.EXTRA_URL, note.getNotesUrl())
                    .putExtra(PdfDownloadService.EXTRA_TITLE, note.getChapterTitle())
                    .putExtra(PdfDownloadService.NOTE_ENTITY, note);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(i);
            } else {
                ctx.startService(i);
            }
        }
    }

    public static void cancelAllDownloads(Context ctx) {
        Intent i = new Intent(ctx, PdfDownloadService.class).setAction(PdfDownloadService.ACTION_CANCEL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ctx.startForegroundService(i);
        } else {
            ctx.startService(i);
        }
    }

    public static String getReadableFileSize(long size) {
        try {
            if (size <= 0) return "0 B";
            final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
            int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
            return String.format(Locale.US, "%.2f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "-";
    }


    public static void openPdfViewer(Context context,
                                     String url,
                                     String title, String notesType, String subjectName, boolean isLocalPdf, String pdfUrlForOffline, String tag) {
        openPdfViewer(context, url, title, notesType, subjectName, isLocalPdf, pdfUrlForOffline, tag, -1);
    }

    public static void openPdfViewer(Context context,
                                     String url,
                                     String title, String notesType, String subjectName, boolean isLocalPdf, String pdfUrlForOffline, String tag, int noteId) {
        if (StringUtils.isEmpty(url)) {
            return;
        }
        long remoteCount = MyApplication.getFirebaseRemoteConfig().getLong("to_show_notes_video_limit");
        long toShowNotesVideoCounter = MyApplication.getLongData(PreferenceKeys.TO_SHOW_NOTES_VIDEO_COUNTER.getKey());
        Log.d(TAG, "openPdfViewe toShowNotesVideoCounterr: " + toShowNotesVideoCounter);
        Log.d(TAG, "openPdfViewer: " + remoteCount);
        Intent intent;
        boolean toShowNotesVideo = false;
        try {
            if (StringUtils.isNotEmpty(notesType)) {
                toShowNotesVideo = RemoteConfigUtils.isFeatureVisible("pdf_viewer_activity", notesType);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        if (toShowNotesVideoCounter < remoteCount && CommonMethods.isOnline(context) && toShowNotesVideo && SubscriptionNavigationHelper.isPdfPencilVisibleByBatch()) {
            intent = new Intent(context, MockDemoActivity.class);
            toShowNotesVideoCounter++;
            MyApplication.saveData(PreferenceKeys.TO_SHOW_NOTES_VIDEO_COUNTER.getKey(), toShowNotesVideoCounter);
        } else {
            intent = new Intent(context, PdfViewerActivity.class);
        }
        intent.putExtra(isLocalPdf ? "pdfPath" : "pdfUrl", url);
        intent.putExtra("pdfUrlForOffline", pdfUrlForOffline);
        intent.putExtra("notesType", notesType);
        intent.putExtra("subjectName", subjectName);
        intent.putExtra("pdfTitle", title != null ? title
                : context.getString(R.string.pdf_document));
        intent.putExtra("tag", tag);
        if (noteId > 0) intent.putExtra("noteId", noteId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        pushFirebaseEvents(url, title, notesType, subjectName, isLocalPdf);
        Log.d(TAG, "openPdfViewer: " + CommonMethods.getIntentParams(intent).toString());
        context.startActivity(intent);
    }

    public static void pushFirebaseEvents(String url,
                                          String title,
                                          String notesType,
                                          String subjectName, boolean isLocalPdf) {

        Map<String, String> params = new HashMap<>();
        params.put("url", url);
        params.put("pdf_title", title);
        params.put("notes_type", notesType);
        params.put("subject_name", subjectName);
        params.put("local_pdf", String.valueOf(isLocalPdf));
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.pdf_view_event.getActionName(), params);
    }
}
