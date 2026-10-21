package arivihan.technologies.doubtbuzzter2.helper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ReplacementSpan;

import androidx.core.content.ContextCompat;

import arivihan.technologies.doubtbuzzter2.R;

public class DottedUnderlineSpan extends ReplacementSpan {

    private final int underlineColor;

    public DottedUnderlineSpan(int color) {
        this.underlineColor = color;
    }

    public static SpannableStringBuilder applyDottedUnderline(Context context, String text) {
        int color = ContextCompat.getColor(context, R.color.arivihan_secondary_color);

        SpannableStringBuilder spannable = new SpannableStringBuilder(text);
        spannable.setSpan(
                new DottedUnderlineSpan(color),
                0,
                text.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        return spannable;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return (int) paint.measureText(text, start, end);
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, Paint paint) {
        paint.setFakeBoldText(false);
        canvas.drawText(text, start, end, x, y, paint);
        Paint underlinePaint = new Paint(paint);
        underlinePaint.setStyle(Paint.Style.STROKE);
        underlinePaint.setStrokeWidth(6);
        underlinePaint.setColor(underlineColor);
        underlinePaint.setPathEffect(new DashPathEffect(new float[]{6, 8}, 0));
        float width = paint.measureText(text, start, end);
        canvas.drawLine(x, y + 10, x + width, y + 10, underlinePaint);
    }
}