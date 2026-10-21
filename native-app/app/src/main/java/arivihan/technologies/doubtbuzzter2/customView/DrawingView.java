package arivihan.technologies.doubtbuzzter2.customView;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.model.remote_config.pdf_circle.PdfCircleConfigurationModel;
import lombok.Setter;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class DrawingView extends View {
    private static final String TAG = DrawingView.class.getSimpleName();

    private static final int REQUEST_WRITE_PERMISSION = 101;
    private Paint paint;       // inner white stroke
    private Paint glowPaint;   // cyan glow with gradient
    private Paint outerGlow;   // blurred outer glow
    private Paint dotPaint;    // end dots
    private Path path;

    private float lastX, lastY;
    private float startX, startY;
    private boolean hasMoved = false;

    // class level (allocate once, reuse)
    private final Paint fingerGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RadialGradient fingerGradient = null;

    // fields for finger position
    private float fingerX = -1;
    private float fingerY = -1;
    private static final float WHITE_SIZE_MULTIPLIER = 0.2f;
    private static final float TEAL_SIZE_MULTIPLIER = 2.1f;
    private VelocityTracker velocityTracker;
    private static final float MIN_STROKE = 4f;
    private static final float MAX_STROKE = 22f;
    private static final float MIN_DRAW_DISTANCE = 8f;

    @Setter
    private float CLOSE_THRESHOLD = 300f;

    private Bitmap capturedBitmap, captureBitmapWithDrawing;
    private boolean isDrawingEnabled = true;
    private FragmentActivity activity;

    @Setter
    private PdfCircleConfigurationModel pdfCircleConfigurationModel;

    public interface DoubtFlowListener {
        void onDoubtCaptured(File file);

        void onDrawingImageCaptured(File file);

        void onWrongCircleDraw();

        void onError();
    }

    @Setter
    private DoubtFlowListener doubtFlowListener;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (context instanceof FragmentActivity) {
            activity = (FragmentActivity) context;
        } else {
            throw new IllegalArgumentException("Context must be a FragmentActivity");
        }
        init();
    }

    private void init() {
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        // Inner white stroke
        paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(MIN_STROKE);

        // Cyan gradient glow
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeCap(Paint.Cap.ROUND);
        glowPaint.setStrokeJoin(Paint.Join.ROUND);
        glowPaint.setStrokeWidth(MIN_STROKE);

        Shader gradient = new LinearGradient(
                0, 0, 800, 800,
                new int[]{ContextCompat.getColor(getContext(), R.color.arivihan_secondary_color), ContextCompat.getColor(getContext(), R.color.timetable_today_bg), ContextCompat.getColor(getContext(), R.color.material_blue)},
                null,
                Shader.TileMode.CLAMP
        );
        glowPaint.setShader(gradient);

        // Outer blurred glow
        outerGlow = new Paint(glowPaint);
        outerGlow.setMaskFilter(new BlurMaskFilter(15f, BlurMaskFilter.Blur.NORMAL));
        outerGlow.setAlpha(120);

        // Dot paint (start & end points)
        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setColor(ContextCompat.getColor(getContext(), R.color.arivihan_secondary_color));

        path = new Path();
    }

    public void setDrawingEnabled(boolean enabled) {
        isDrawingEnabled = enabled;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (path.isEmpty()) return;

        // Glow layers + stroke
        canvas.drawPath(path, outerGlow);
        canvas.drawPath(path, glowPaint);
        canvas.drawPath(path, paint);

        try {
            if (fingerX >= 0 && fingerY >= 0) {
                float baseRadius = paint.getStrokeWidth() * 8f; // base radius

                float tealRadius = baseRadius * TEAL_SIZE_MULTIPLIER;

                if (fingerGradient == null) {
                    fingerGradient = new RadialGradient(
                            0, 0,
                            tealRadius,
                            new int[]{
                                    ContextCompat.getColor(getContext(), R.color.arivihan_secondary_color),
                                    Color.TRANSPARENT
                            },
                            new float[]{0f, 1f},
                            Shader.TileMode.CLAMP
                    );
                    fingerGlowPaint.setShader(fingerGradient);
                }

                Matrix m = new Matrix();
                m.setTranslate(fingerX, fingerY);
                fingerGradient.setLocalMatrix(m);

                canvas.drawCircle(fingerX, fingerY, tealRadius, fingerGlowPaint);

                float whiteRadius = baseRadius * WHITE_SIZE_MULTIPLIER;
                Paint whitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                whitePaint.setColor(Color.WHITE);
                canvas.drawCircle(fingerX, fingerY, whiteRadius, whitePaint);

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isDrawingEnabled) return false;

        float x = event.getX();
        float y = event.getY();

        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                path.reset();
                path.moveTo(x, y);
                lastX = startX = x;
                lastY = startY = y;
                fingerX = x;
                fingerY = y;
                hasMoved = false;

                paint.setStrokeWidth(MIN_STROKE);
                glowPaint.setStrokeWidth(MIN_STROKE);
                outerGlow.setStrokeWidth(MIN_STROKE + 6f);

                velocityTracker.clear();
                velocityTracker.addMovement(event);
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(x - lastX);
                float dy = Math.abs(y - lastY);
                fingerX = x;
                fingerY = y;
                if (dx > MIN_DRAW_DISTANCE || dy > MIN_DRAW_DISTANCE) {
                    hasMoved = true;
                    float cx = (x + lastX) / 2f;
                    float cy = (y + lastY) / 2f;
                    path.quadTo(lastX, lastY, cx, cy);

                    velocityTracker.computeCurrentVelocity(1000);
                    float v = (float) Math.hypot(
                            velocityTracker.getXVelocity(),
                            velocityTracker.getYVelocity()
                    );

                    float width = map(v, 0f, 5000f, MAX_STROKE, MIN_STROKE);
                    width = clamp(width, MIN_STROKE, MAX_STROKE);
                    paint.setStrokeWidth(width);
                    glowPaint.setStrokeWidth(width);
                    outerGlow.setStrokeWidth(width + 6f);

                    lastX = x;
                    lastY = y;
                    invalidate();
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                fingerX = -1;
                fingerY = -1;
                if (!hasMoved) {
                    path.reset();
                    path.addCircle(x, y, MIN_STROKE, Path.Direction.CW);
                    invalidate();
                    postDelayed(this::resetDrawing, 150);
                    recycleVelocityTracker();
                    return true;
                }

                path.lineTo(x, y);

                boolean closed = isPathClosed(x, y);
                if (closed) {
                    path.close();
                    View pdfView = activity.findViewById(R.id.pdfView);
                    if (pdfView != null) {
                        capturedBitmap = capturePdfWithInvisibleDrawing(pdfView);
                        captureBitmapWithDrawing = capturePdfWithDrawing(pdfView);
                        if (capturedBitmap != null) {
                            saveCapturedBitmap(capturedBitmap, captureBitmapWithDrawing);
                        }
                    }
                } else {
                    if (doubtFlowListener != null) {
                        doubtFlowListener.onWrongCircleDraw();
                    }
                }

                fadeOutStroke();
                recycleVelocityTracker();
                return true;
        }
        return false;
    }

    private void fadeOutStroke() {
        ValueAnimator animator = ValueAnimator.ofInt(255, 0);
        animator.setDuration(250);
        animator.addUpdateListener(a -> {
            int alpha = (int) a.getAnimatedValue();
            paint.setAlpha(alpha);
            glowPaint.setAlpha(alpha);
            outerGlow.setAlpha(alpha / 2);
            invalidate();
            if (alpha == 0) resetDrawing();
        });
        animator.start();
    }

    private void recycleVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    private boolean isPathClosed(float endX, float endY) {
        Log.d(TAG, "Checking if path is closed...");

        // Step 1: Basic distance check
        float dx = endX - startX;
        float dy = endY - startY;
        double distance = Math.hypot(dx, dy);

        Log.d(TAG, "Step 1 → Distance check: " + distance + " (Threshold: " + CLOSE_THRESHOLD + ")");
        if (distance > CLOSE_THRESHOLD) {
            Log.d(TAG, "❌ Step 1 failed: End point too far from start.");
            return false;
        }
        Log.d(TAG, "✅ Step 1 passed: End point is close enough.");

        // Step 2: Area check
        RectF bounds = new RectF();
        path.computeBounds(bounds, true);

        float area = bounds.width() * bounds.height();
        Log.d(TAG, "Step 2 → Area check: " + area + " (Minimum required: 2000)");
        if (area < getWidth() * getHeight() * ((pdfCircleConfigurationModel != null && pdfCircleConfigurationModel.getPdfClosedCircleAreaThreshold() != 0) ? pdfCircleConfigurationModel.getPdfClosedCircleAreaThreshold() : 0.01f)) { // Default 1% of screen area
            Log.d(TAG, "❌ Step 2 failed: Enclosed area too small.");
            return false;
        }
        Log.d(TAG, "✅ Step 2 passed: Area is large enough.");

        // Step 3: Path closure check using PathMeasure
        PathMeasure pathMeasure = new PathMeasure(path, false);
        if (pathMeasure.getLength() < ((pdfCircleConfigurationModel != null && pdfCircleConfigurationModel.getPdfClosedPathLength() != 0) ? pdfCircleConfigurationModel.getPdfClosedPathLength() : 100f)) {
            Log.d(TAG, "❌ Step 3 failed: Path length is zero.");
            return false;
        }

        float[] startPos = new float[2];
        float[] endPos = new float[2];

        pathMeasure.getPosTan(0, startPos, null);
        pathMeasure.getPosTan(pathMeasure.getLength(), endPos, null);

        float closureDistance = (float) Math.hypot(startPos[0] - endPos[0], startPos[1] - endPos[1]);
        Log.d(TAG, "Step 3 → Closure distance: " + closureDistance + " (Threshold: " + CLOSE_THRESHOLD + ")");

        if (closureDistance > CLOSE_THRESHOLD / 2) {
            Log.d(TAG, "❌ Step 3 failed: Start and end positions do not match.");
            return false;
        }

        Log.d(TAG, "✅ Step 3 passed: Path is truly closed.");
        Log.d(TAG, "🎯 Path is closed → valid selection.");
        return true;
    }

    public void resetDrawing() {
        path.reset();
        paint.setAlpha(255);
        glowPaint.setAlpha(255);
        outerGlow.setAlpha(120);
        invalidate();
    }

    public Bitmap capturePdfWithInvisibleDrawing(View pdfView) {
        try {
            setVisibility(View.INVISIBLE);

            RectF bounds = new RectF();
            path.computeBounds(bounds, true);

            bounds.left = Math.max(bounds.left - 16, 0);
            bounds.top = Math.max(bounds.top - 16, 0);
            bounds.right = Math.min(bounds.right + 16, pdfView.getWidth());
            bounds.bottom = Math.min(bounds.bottom + 16, pdfView.getHeight());

            Bitmap fullBitmap = Bitmap.createBitmap(
                    pdfView.getWidth(), pdfView.getHeight(),
                    Bitmap.Config.ARGB_8888
            );
            Canvas canvas = new Canvas(fullBitmap);
            pdfView.draw(canvas);

            setVisibility(View.VISIBLE);

            return Bitmap.createBitmap(fullBitmap,
                    (int) bounds.left,
                    (int) bounds.top,
                    (int) bounds.width(),
                    (int) bounds.height());
        } catch (Exception e) {
            Log.e(TAG, "Error capturing PDF view", e);
            setVisibility(View.VISIBLE);
            return null;
        }
    }

    public void saveCapturedBitmap(Bitmap bitmap, Bitmap bitmapWithDrawing) {
        try {
            File originalFile = saveBitmapToCache(bitmap, "captured_content_");
            if (doubtFlowListener != null) {
                doubtFlowListener.onDoubtCaptured(originalFile);
            }
            File drawingFile = null;
            if (MyApplication.getFirebaseRemoteConfig().
                    getBoolean("to_save_drawing_image_in_firebase_store")) {
                drawingFile = saveBitmapToCache(bitmapWithDrawing, "captured_drawing_");
            }
            if (doubtFlowListener != null && MyApplication.getFirebaseRemoteConfig().
                    getBoolean("to_save_drawing_image_in_firebase_store")) {
                doubtFlowListener.onDrawingImageCaptured(drawingFile);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to save captured bitmap", e);
            if (doubtFlowListener != null) {
                doubtFlowListener.onError();
            }
            Toast.makeText(getContext(), getContext().getString(R.string.failed_to_save_content), Toast.LENGTH_SHORT).show();
        }
        try {
            String allowedPhone = (pdfCircleConfigurationModel != null) ?
                    pdfCircleConfigurationModel.getAllowedPhoneForImageSavedGallery() : "";

            saveCapturedBitmap(bitmap, allowedPhone);

        } catch (Exception ex) {
            Log.e(TAG, "Failed to save bitmap to gallery", ex);
        }
    }

    private File saveBitmapToCache(Bitmap bitmap, String prefix) throws IOException {
        if (bitmap == null) {
            throw new IllegalArgumentException("Bitmap cannot be null");
        }

        String filename = prefix + System.currentTimeMillis() + ".png";
        File savedFile = new File(getContext().getCacheDir(), filename);

        try (FileOutputStream fos = new FileOutputStream(savedFile)) {
            int quality = (pdfCircleConfigurationModel != null
                    && pdfCircleConfigurationModel.getPdfCircleCompressQuality() != 0)
                    ? pdfCircleConfigurationModel.getPdfCircleCompressQuality()
                    : 100;

            if (!bitmap.compress(Bitmap.CompressFormat.PNG, quality, fos)) {
                throw new IOException("Bitmap compression failed");
            }
        }

        return savedFile;
    }


    public void saveCapturedBitmap(Bitmap bitmap, String allowedPhonesCsv) {

        if (bitmap == null || StringUtils.isEmpty(allowedPhonesCsv)) {
            return;
        }
        String[] allowedPhones = allowedPhonesCsv.split(",");
        String myPhone = MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey());
        boolean isAllowed = false;
        for (String phone : allowedPhones) {
            if (phone.trim().equals(myPhone)) {
                isAllowed = true;
                break;
            }
        }
//        Log.d(TAG, ":---> "+myPhone+" | "+allowedPhonesCsv+" | "+isAllowed);
        if (isAllowed) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                // Need permission for Android 9 and below
                if (ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_WRITE_PERMISSION);
                    return; // wait for user response
                }
            }
            saveToGallery(bitmap);
        }
    }

    private void saveToGallery(Bitmap bitmap) {
        String filename = "captured_content_" + System.currentTimeMillis() + ".png";

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Arivihan");

                Uri uri = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    try (OutputStream out = getContext().getContentResolver().openOutputStream(uri)) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                        Toast.makeText(getContext(), "Saved to Gallery", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                // Legacy external storage
                File dir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), "Arivihan");
                if (!dir.exists()) dir.mkdirs();
                File file = new File(dir, filename);

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    Toast.makeText(getContext(), "Saved to Gallery", Toast.LENGTH_SHORT).show();
                }

                // Make sure Gallery picks it up
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(file));
                getContext().sendBroadcast(intent);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to save bitmap in gallery", e);
            Toast.makeText(getContext(), "Failed to save in gallery", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveToCache(Bitmap bitmap) {
        String filename = "captured_content_" + System.currentTimeMillis() + ".png";
        File savedFile = new File(getContext().getCacheDir(), filename);
        try (FileOutputStream fos = new FileOutputStream(savedFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            Toast.makeText(getContext(), "Saved in cache", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Failed to save bitmap in cache", e);
            Toast.makeText(getContext(), "Failed to save in cache", Toast.LENGTH_SHORT).show();
        }
    }

    public Bitmap getCapturedBitmap() {
        return capturedBitmap;
    }

    // Utility mapping & clamp
    private float map(float value, float inMin, float inMax, float outMin, float outMax) {
        if (value <= inMin) return outMin;
        if (value >= inMax) return outMax;
        float fraction = (value - inMin) / (inMax - inMin);
        return outMin + fraction * (outMax - outMin);
    }

    private float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }


    public Bitmap capturePdfWithDrawing(View pdfView) {
        try {
            // Step 1 — Capture the PDF view along with the visible drawing
            RectF bounds = new RectF();
            path.computeBounds(bounds, true);

            bounds.left = Math.max(bounds.left - 16, 0);
            bounds.top = Math.max(bounds.top - 16, 0);
            bounds.right = Math.min(bounds.right + 16, pdfView.getWidth());
            bounds.bottom = Math.min(bounds.bottom + 16, pdfView.getHeight());

            // Create a bitmap of the full PDF view
            Bitmap fullBitmap = Bitmap.createBitmap(
                    pdfView.getWidth(),
                    pdfView.getHeight(),
                    Bitmap.Config.ARGB_8888
            );

            Canvas canvas = new Canvas(fullBitmap);

            // Step 2 — Draw the PDF view first
            pdfView.draw(canvas);

            // Step 3 — Draw the visible drawing path over it
            canvas.drawPath(path, outerGlow);
            canvas.drawPath(path, glowPaint);
            canvas.drawPath(path, paint);

            // Step 4 — Crop to the drawn area
            return Bitmap.createBitmap(
                    fullBitmap,
                    (int) bounds.left,
                    (int) bounds.top,
                    (int) bounds.width(),
                    (int) bounds.height()
            );
        } catch (Exception e) {
            Log.e(TAG, "Error capturing PDF view with drawing", e);
            return null;
        }
    }


}
