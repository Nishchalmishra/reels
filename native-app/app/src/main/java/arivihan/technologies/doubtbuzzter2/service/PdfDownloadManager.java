package arivihan.technologies.doubtbuzzter2.service;

import android.os.Build;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.Getter;

public class PdfDownloadManager {

    private static final String TAG = PdfDownloadManager.class.getSimpleName();
    private static PdfDownloadManager instance;

    public static synchronized PdfDownloadManager getInstance() {
        if (instance == null) {
            instance = new PdfDownloadManager();
        }
        return instance;
    }

    private final Map<String, DownloadTask> taskMap = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public static class DownloadTask {

        private volatile boolean isDownloading;
        @Getter
        private volatile boolean isCompleted;
        @Getter
        private volatile int progress;
        @Getter
        private File file;
        private final List<DownloadListener> listeners = new ArrayList<>();

        public boolean isDownloading() {
            return isDownloading;
        }

    }


    public interface DownloadListener {
        void onProgress(int progress);
        void onSuccess(File file);
        void onError(Exception e);
    }

    public void addListener(String key, DownloadListener listener) {
        DownloadTask task = taskMap.get(key);
        if (task != null) {
            task.listeners.add(listener);

            // last known progress
            listener.onProgress(task.progress);

            if (!task.isDownloading && task.isCompleted && task.file != null && task.file.exists()) {
                listener.onSuccess(task.file);
            }
        }
    }

    public void removeListener(String key, DownloadListener listener) {
        DownloadTask task = taskMap.get(key);
        if (task != null) {
            task.listeners.remove(listener);
        }
    }

    public DownloadTask getTask(String key) {
        Log.d(TAG,"---> Total Tasks: "+taskMap.toString());
        return taskMap.get(key);
    }

    public void download(
            String key,
            String url,
            File file,
            DownloadListener listener
    ) {

        DownloadTask task = taskMap.computeIfAbsent(key, k -> new DownloadTask());

        if (listener != null) {
            task.listeners.add(listener);
        }

        if (task.isDownloading) {
            Log.d(TAG, "Task is Downloading: "+key+", Url: "+url);
            return;
        }

        task.isDownloading = true;
        task.isCompleted = false;
        task.file = file;

        executor.execute(() -> {

            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection) new URL(url).openConnection();
                long existingLength = file.exists() ? file.length() : 0L;
                boolean appendMode = existingLength > 0L;

                if (appendMode) {
                    connection.setRequestProperty("Range", "bytes=" + existingLength + "-");
                }

                connection.connect();

                int responseCode = connection.getResponseCode();
                long remoteContentLength = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    remoteContentLength = connection.getContentLengthLong();
                }

                if (appendMode && responseCode == HttpURLConnection.HTTP_OK) {
                    appendMode = false;
                    existingLength = 0L;
                }

                long totalFileLength = remoteContentLength;
                if (appendMode && responseCode == HttpURLConnection.HTTP_PARTIAL && remoteContentLength > 0) {
                    totalFileLength = existingLength + remoteContentLength;
                }

                InputStream input = new BufferedInputStream(connection.getInputStream());
                FileOutputStream output = new FileOutputStream(file, appendMode);

                byte[] buffer = new byte[8192];
                long total = existingLength;
                int count;

                if (totalFileLength > 0) {
                    task.progress = (int) (total * 100L / totalFileLength);
                    notifyProgress(task, task.progress);
                } else if (existingLength > 0) {
                    task.progress = (int) (existingLength / 1024);
                    notifyProgress(task, task.progress);
                }

                while ((count = input.read(buffer)) != -1) {
                    total += count;
                    output.write(buffer, 0, count);

                    if (totalFileLength > 0) {
                        int progress = (int) (total * 100L / totalFileLength);
                        task.progress = progress;
                        notifyProgress(task, progress);
                    } else {
                        task.progress = -1;
                        notifyProgress(task, (int)(total / 1024));
                    }

                }

                output.close();
                input.close();

                task.isDownloading = false;
                task.isCompleted = true;
                notifySuccess(task, file);

            } catch (Exception e) {
                task.isDownloading = false;
                task.isCompleted = false;
                if (file.exists() && file.length() == 0L) file.delete();
                notifyError(task, e);
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    private void notifyProgress(DownloadTask task, int progress) {
        for (DownloadListener l : task.listeners) {
            l.onProgress(progress);
        }
    }

    private void notifySuccess(DownloadTask task, File file) {
        for (DownloadListener l : task.listeners) {
            l.onSuccess(file);
        }
        task.listeners.clear();
    }

    private void notifyError(DownloadTask task, Exception e) {
        for (DownloadListener l : task.listeners) {
            l.onError(e);
        }
        task.listeners.clear();
    }
}
