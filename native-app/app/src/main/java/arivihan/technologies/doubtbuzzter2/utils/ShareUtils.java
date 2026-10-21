package arivihan.technologies.doubtbuzzter2.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;

import arivihan.technologies.doubtbuzzter2.BuildConfig;

public class ShareUtils {
    private static final String TAG = ShareUtils.class.getSimpleName();
    private static final String FILE_PROVIDER_AUTHORITY_SUFFIX = ".fileprovider";
    // max dimension to avoid OOM; tweak if needed (px)
    private static final int MAX_BITMAP_DIMENSION = 1024;

    public static Intent shareDialogAsImage(Context context, Bitmap bitmap) {
        if (context == null || bitmap == null) {
            Log.e(TAG, "Context or Bitmap is null");
            return null;
        }

        try {
//            View dialogView = getDialogContentView(dialog);
//            if (dialogView == null) {
////                Toast.makeText(context, "Unable to capture dialog view", Toast.LENGTH_SHORT).show();
//                return null;
//            }
//
//            ensureViewIsLaidOut(dialogView);
//
//            Bitmap bmp = createBitmapFromView(dialogView);
//            if (bmp == null) {
//                Toast.makeText(context, "Failed to create image", Toast.LENGTH_SHORT).show();
//                return null;
//            }

            // scale down
            bitmap = scaleDownIfNeeded(bitmap, MAX_BITMAP_DIMENSION, MAX_BITMAP_DIMENSION);

            // save to cache
            File imageFile = saveBitmapToCache(context, bitmap);
            if (imageFile == null) {
                Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show();
                return null;
            }

            // content Uri via FileProvider
//            String authority = context.getPackageName() + FILE_PROVIDER_AUTHORITY_SUFFIX;
            Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", imageFile);

            // share intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Grant temporary read permission to all resolved activities
            grantUriPermissionToApps(context, contentUri, shareIntent);

            return shareIntent;
        } catch (Exception e) {
            Log.e(TAG, "Error in shareDialogAsImage", e);
            Toast.makeText(context, "Failed to share image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    // Helper: get the "content" view of a dialog reliably
    private static View getDialogContentView(Dialog dialog) {
        try {
            Window w = dialog.getWindow();
            if (w != null) {
                // Decor view contains status bar/padding; we want the content view
                View decor = w.getDecorView();
                // Usually content is android.R.id.content
                View content = decor.findViewById(android.R.id.content);
                if (content != null) return content;
                return decor;
            } else {
                // fallback to dialog.findViewById
                View content = dialog.findViewById(android.R.id.content);
                return content;
            }
        } catch (Exception e) {
            Log.w(TAG, "getDialogContentView failed", e);
            return null;
        }
    }

    // Ensure view is measured/layout so .draw(canvas) works
    private static void ensureViewIsLaidOut(View view) {
        if (view.getWidth() > 0 && view.getHeight() > 0) return;

        int widthSpec = View.MeasureSpec.makeMeasureSpec(
                getScreenWidth(view.getContext()), View.MeasureSpec.AT_MOST);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(
                getScreenHeight(view.getContext()), View.MeasureSpec.AT_MOST);

        view.measure(widthSpec, heightSpec);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
    }

    private static int getScreenWidth(Context ctx) {
        return ctx.getResources().getDisplayMetrics().widthPixels;
    }
    private static int getScreenHeight(Context ctx) {
        return ctx.getResources().getDisplayMetrics().heightPixels;
    }

    // capture view to bitmap
    private static Bitmap createBitmapFromView(View view) {
        try {
            int w = view.getWidth();
            int h = view.getHeight();

            if (w <= 0 || h <= 0) {
                // fallback to measured dimensions
                w = view.getMeasuredWidth();
                h = view.getMeasuredHeight();
                if (w <= 0 || h <= 0) {
                    Log.e(TAG, "Invalid view dimensions: " + w + "x" + h);
                    return null;
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            // Optionally set background color if view has no background
            if (view.getBackground() != null) {
                view.getBackground().draw(canvas);
            } else {
                canvas.drawColor(android.graphics.Color.WHITE);
            }
            view.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError oom) {
            Log.e(TAG, "OOM creating bitmap", oom);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "createBitmapFromView failed", e);
            return null;
        }
    }

    // scale down while preserving aspect ratio if any dimension is larger than max
    private static Bitmap scaleDownIfNeeded(Bitmap b, int maxWidth, int maxHeight) {
        if (b == null) return null;
        int w = b.getWidth();
        int h = b.getHeight();
        Log.d(TAG, ":Dialog width: "+w+", Height: "+h);
        if (w <= maxWidth && h <= maxHeight) return b;

        float ratio = Math.min((float) maxWidth / w, (float) maxHeight / h);
        int newW = Math.max(1, Math.round(w * ratio));
        int newH = Math.max(1, Math.round(h * ratio));
        try {
            Bitmap scaled = Bitmap.createScaledBitmap(b, newW, newH, true);
            if (scaled != b) {
                b.recycle();
            }
            return scaled;
        } catch (OutOfMemoryError oom) {
            Log.e(TAG, "OOM scaling bitmap", oom);
            return b; // return original if scaling fails
        }
    }

    // Save bitmap to cache and return the file
    private static File saveBitmapToCache(Context context, Bitmap bitmap) {
        if (bitmap == null) return null;
        File cacheDir = context.getCacheDir();
        if (cacheDir == null) return null;

        String filename = String.format(Locale.US, "shared_img_%d.png", System.currentTimeMillis());
        File outFile = new File(cacheDir, filename);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(outFile);
            boolean compressed = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            if (!compressed) {
                Log.w(TAG, "Bitmap.compress returned false");
            }
            return outFile;
        } catch (Exception e) {
            Log.e(TAG, "Error saving bitmap", e);
            return null;
        } finally {
            if (fos != null) try { fos.close(); } catch (Exception ignored) {}
        }
    }

    // grantUriPermission to all resolved activities to be extra safe
    private static void grantUriPermissionToApps(Context context, Uri contentUri, Intent shareIntent) {
        try {
            for (android.content.pm.ResolveInfo resInfo :
                    context.getPackageManager().queryIntentActivities(shareIntent, 0)) {
                String packageName = resInfo.activityInfo.packageName;
                try {
                    context.grantUriPermission(packageName, contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception e) {
                    Log.w(TAG, "grantUriPermission failed for " + packageName, e);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "grantUriPermissionToApps failed", e);
        }
    }

}
