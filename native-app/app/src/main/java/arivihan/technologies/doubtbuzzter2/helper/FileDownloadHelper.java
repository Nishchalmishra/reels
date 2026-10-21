package arivihan.technologies.doubtbuzzter2.helper;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.File;

public class FileDownloadHelper {

    // Cached once — getExternalFilesDir() triggers a blocking IPC (StorageManager.mkdirs)
    // and must never be called on the main thread repeatedly (e.g. in onBindViewHolder).
    private static File cachedDownloadsDir = null;

    private static File getDownloadsDir(Context context) {
        if (cachedDownloadsDir != null) return cachedDownloadsDir;
        File dir = context.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (dir == null) {
            dir = new File(context.getApplicationContext().getFilesDir(), "downloads");
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
        cachedDownloadsDir = dir;
        return cachedDownloadsDir;
    }

    public static String buildSafeFileName(String chapterTitle, String url) {
        String cleanTitle = chapterTitle == null ? "note" : chapterTitle
                .replaceAll("[^a-zA-Z0-9._-]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
        int hash = (url == null ? 0 : url.hashCode());
        return cleanTitle + "_" + Math.abs(hash) + ".pdf";
    }

//    public static File getAppDownloadsDirFile(Context context, String fileName) {
//        return new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName);
//    }
//
//    public static boolean isFileDownloaded(Context context, String fileName) {
//        return getAppDownloadsDirFile(context, fileName).exists();
//    }

    public static File getAppDownloadsDirFile(Context context, String fileName) {
        return new File(getDownloadsDir(context), fileName);
    }

    // Check if file is already downloaded safely
    public static boolean isFileDownloaded(Context context, String fileName) {
        try {
            File file = getAppDownloadsDirFile(context, fileName);
            return file.exists() && file.length() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Prevent crash
        }
    }

    public static long startDownload(Context context, String url, String fileName, String titleForNotification) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(titleForNotification != null ? titleForNotification : fileName);
        request.setDescription("Downloading note…");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setMimeType("application/pdf");
        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName);

        DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        return dm.enqueue(request);
    }

    public static int getStatus(Context context, long downloadId) {
        DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query q = new DownloadManager.Query().setFilterById(downloadId);
        Cursor c = dm.query(q);
        if (c == null) return DownloadManager.STATUS_FAILED;
        int status = DownloadManager.STATUS_FAILED;
        if (c.moveToFirst()) {
            status = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
        }
        c.close();
        return status;
    }

    public static int getPdfPageCountWithRenderer(String path) {
        try {
            File file = new File(path);
            ParcelFileDescriptor fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer renderer = new PdfRenderer(fd);
            int pages = renderer.getPageCount();
            renderer.close();
            fd.close();
            Log.d("pdfCount", "getPdfPageCountWithRenderer: " + pages);
            return pages;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
