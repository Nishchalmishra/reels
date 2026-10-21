package arivihan.technologies.doubtbuzzter2.customView.gradient;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

public class GradientAnimationView extends View {

    private Paint paint;
    private int[] colors;
    private float progress = 0f; // 0 -> bottom, 1 -> top
    private float alphaFactor = 1f; // 1 -> visible, 0 -> gone

    private long flowDuration = 1000; // default 1s
    private long fadeDuration = 500;  // default 0.5s
    private ValueAnimator animator;

    private OnAnimationCompleteListener listener;

    public GradientAnimationView(Context context) {
        super(context);
        init();
    }

    public GradientAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GradientAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public void setColors(int[] colors) {
        this.colors = colors;
    }

    public void setDurations(long flowDuration, long fadeDuration) {
        this.flowDuration = flowDuration;
        this.fadeDuration = fadeDuration;
    }

    public void setOnAnimationCompleteListener(OnAnimationCompleteListener listener) {
        this.listener = listener;
    }

    public void startAnimation() {
        if (animator != null && animator.isRunning()) animator.cancel();

        // Reset state
        progress = 0f;
        alphaFactor = 1f;
        invalidate();

        // First phase: flow bottom → top
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(flowDuration);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(a -> {
            progress = (float) a.getAnimatedValue();
            invalidate();
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Start fade-out after flow completes
                startFadeOut();
            }
        });

        animator.start();
    }

    private void startFadeOut() {
        ValueAnimator fadeAnimator = ValueAnimator.ofFloat(1f, 0f);
        fadeAnimator.setDuration(fadeDuration);
        fadeAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        fadeAnimator.addUpdateListener(a -> {
            alphaFactor = (float) a.getAnimatedValue();
            invalidate();
        });

        fadeAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (listener != null) listener.onAnimationComplete();
            }
        });

        fadeAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (colors == null || colors.length == 0) return;

        int w = getWidth();
        int h = getHeight();

        // Gradient start moves upward with progress
        float top = h * (1 - progress);
        float bottom = h;

        // Spread across height
        float[] positions = new float[colors.length];
        for (int i = 0; i < colors.length; i++) {
            positions[i] = (float) i / (colors.length - 1);
        }

        LinearGradient gradient = new LinearGradient(
                0, bottom,
                0, top,
                colors,
                positions,
                Shader.TileMode.CLAMP
        );

        paint.setShader(gradient);
        paint.setAlpha((int) (255 * alphaFactor)); // apply fade

        canvas.drawRect(0, 0, w, h, paint);

    }

    public interface OnAnimationCompleteListener {
        void onAnimationComplete();
    }
}

