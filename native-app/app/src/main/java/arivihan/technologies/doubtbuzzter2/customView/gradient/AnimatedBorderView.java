package arivihan.technologies.doubtbuzzter2.customView.gradient;

import arivihan.technologies.doubtbuzzter2.R;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.util.AttributeSet;
import android.graphics.*;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

public class AnimatedBorderView extends FrameLayout {

    private float borderWidthPx;
    private float cornerRadiusPx;
    private boolean isAnimating = true;


    //gradient colors, position
    private final int[] gradientColors = {
            0x00FF4D9E,  // transparent pink  (arc lead-in)
            0xFFFF4D9E,  // vivid pink
            0xFFAB47BC,  // pink → purple
            0xFF9333EA,  // purple / violet
            0xFF22D3EE,  // cyan
            0xFFFF6D24,  // orange
            0x80FF6D24,  // orange fading out
            0x00FFFFFF,  // fully transparent  ← halfway point, other half invisible
            0x00FFFFFF,  // transparent hold
            0x00FF4D9E   // transparent pink  (seamless wrap back)
    };

    private final float[] gradientPositions = {
            0.00f, 0.05f, 0.14f, 0.24f, 0.36f, 0.44f, 0.50f, 0.55f, 0.92f, 1.00f
    };


    //custom color
    private boolean useCustomGradient = false;
    private int[] customColors;
    private float[] customPositions;


    private final Paint borderPaint;
    private final Matrix matrix = new Matrix();
    private float angle = 0f;
    private ValueAnimator animator;
    private final Runnable stopAnimationRunnable = this::stopAnimation;


    public AnimatedBorderView(Context context) {
        this(context, null);
    }


    public AnimatedBorderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public AnimatedBorderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setWillNotDraw(false);

        float density = context.getResources().getDisplayMetrics().density;

        float defaultBorder = 4 * density;
        float defaultRadius = 12 * density;

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AnimatedBorderView);
            borderWidthPx = a.getDimension(R.styleable.AnimatedBorderView_borderWidth, defaultBorder);
            cornerRadiusPx = a.getDimension(R.styleable.AnimatedBorderView_cornerRadius, defaultRadius);

            a.recycle();
        } else {
            borderWidthPx = defaultBorder;
            cornerRadiusPx = defaultRadius;
        }

        int pad = (int) borderWidthPx;
        setPadding(pad, pad, pad, pad);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderWidthPx);
        borderPaint.setStrokeCap(Paint.Cap.ROUND);

        setVisibility(GONE);
    }


    public void setBorderWidth(float dp) {
        borderWidthPx = dp * getResources().getDisplayMetrics().density;
        borderPaint.setStrokeWidth(borderWidthPx);
        invalidate();
    }

    public void setCornerRadius(float dp) {
        cornerRadiusPx = dp * getResources().getDisplayMetrics().density;
        invalidate();
    }



    //set custom color
    public void setGradient(int[] colors, float[] positions) {
        if (colors == null || positions == null || colors.length != positions.length) return;

        this.customColors = colors.clone();
        this.customPositions = positions.clone();

        useCustomGradient = true;
        invalidate();
    }

    public void resetGradient() {
        useCustomGradient = false;
        invalidate();
    }



    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // Do NOT cancel the animator here. RecyclerView calls onDetachedFromWindow while
        // the item is still partially visible during a scroll gesture, which would freeze
        // the gradient mid-swipe. The animator continues updating `angle` in the background
        // (invalidate() calls on a detached view are no-ops but harmless). The angle stays
        // current so playAnimation() can resume seamlessly when the view reattaches.
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Safety net: if the animator was explicitly stopped (e.g. stopAnimation() called
        // while detached) but the view is still supposed to be animating, restart it.
        if (isAnimating && getVisibility() == VISIBLE && (animator == null || !animator.isRunning())) {
            startAnimator();
        }
    }

    public void playAnimation(long durationMillis) {
        setVisibility(VISIBLE);
        isAnimating = true;

        // Only (re)start if not already running — avoids cancel+restart flicker on hot rebinds.
        if (animator == null || !animator.isRunning()) {
            startAnimator();
        }

        if (durationMillis > 0) {
            removeCallbacks(stopAnimationRunnable);
            postDelayed(stopAnimationRunnable, durationMillis);
        }
    }

    private void startAnimator() {
        // Capture the current angle so the animation resumes from where it left off
        // rather than jumping back to 0 every time the view is recycled and rebound.
        final float resumeAngle = this.angle;

        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(2000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());

        animator.addUpdateListener(anim -> {
            float value = (float) anim.getAnimatedValue();
            angle = (resumeAngle + value * 360f) % 360f;
            invalidate();
        });

        animator.start();
    }


    public void stopAnimation() {
        isAnimating = false;
        removeCallbacks(stopAnimationRunnable);

        if (animator != null) {
            animator.cancel();
            animator = null;
        }

        borderPaint.setShader(null);
        invalidate();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        // Self-heal: if the window becomes visible again (app foregrounded, screen on)
        // and the animator was stopped while isAnimating=true, restart it.
        if (visibility == VISIBLE && isAnimating && getVisibility() == VISIBLE
                && (animator == null || !animator.isRunning())) {
            startAnimator();
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isAnimating) return;

        float w = getWidth();
        float h = getHeight();
        float half = borderWidthPx / 2f;

        int[] colorsToUse = useCustomGradient ? customColors : gradientColors;
        float[] positionsToUse = useCustomGradient ? customPositions : gradientPositions;

        SweepGradient shader = new SweepGradient(
                w / 2f, h / 2f,
                colorsToUse,
                positionsToUse
        );

        matrix.reset();
        matrix.setRotate(angle, w / 2f, h / 2f);
        shader.setLocalMatrix(matrix);
        borderPaint.setShader(shader);

        RectF rect = new RectF(half, half, w - half, h - half);
        canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, borderPaint);
    }
}