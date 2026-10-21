package arivihan.technologies.doubtbuzzter2.utils;

import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.view.View;

public class GradientUtils {

    public static void applyHorizontalGradient(View targetView, int[] colors, float[] stops) {
        if (colors == null || stops == null || colors.length != stops.length) {
            throw new IllegalArgumentException("Colors and stops must be non-null and same length.");
        }

        // ShapeDrawable with a Rect
        ShapeDrawable shapeDrawable = new ShapeDrawable(new RectShape());

        // LinearGradient (horizontal → left to right)
        LinearGradient linearGradient = new LinearGradient(
                0, 0, targetView.getWidth(), 0, // x0,y0 to x1,y1 (horizontal)
                colors,
                stops,
                Shader.TileMode.CLAMP
        );

        shapeDrawable.getPaint().setShader(linearGradient);
        targetView.setBackground(shapeDrawable);
    }
}
