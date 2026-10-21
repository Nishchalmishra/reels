package arivihan.technologies.doubtbuzzter2.customView.textview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.ReplacementSpan;

import androidx.annotation.NonNull;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.LineBackgroundSpan;

public class ColoredStrikeSpan implements LineBackgroundSpan {

    private final int color;

    public ColoredStrikeSpan(int color) {
        this.color = color;
    }

    @Override
    public void drawBackground(Canvas canvas, Paint paint,
                               int left, int right, int top, int baseline, int bottom,
                               CharSequence text, int start, int end, int lineNumber) {

        // Calculate strike-through position (middle of the text)
        float strikeY = (top + bottom) / 2f;

        // Save original paint color
        int oldColor = paint.getColor();

        // Set strike color and stroke width
        paint.setColor(color);
        paint.setStrokeWidth(4f);

        // Draw strike line
        canvas.drawLine(left, strikeY, right, strikeY, paint);

        // Restore paint color
        paint.setColor(oldColor);
    }
}


