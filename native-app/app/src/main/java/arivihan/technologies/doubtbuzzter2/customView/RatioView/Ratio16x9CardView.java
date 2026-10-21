package arivihan.technologies.doubtbuzzter2.customView.RatioView;

import android.content.Context;
import android.util.AttributeSet;

import androidx.cardview.widget.CardView;

public class Ratio16x9CardView extends CardView {
    private static final float WIDTH_RATIO = 16f;
    private static final float HEIGHT_RATIO = 9f;

    public Ratio16x9CardView(Context context) {
        super(context);
    }

    public Ratio16x9CardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Ratio16x9CardView(Context context, AttributeSet attrs, int defStyleAttr ) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) (width * HEIGHT_RATIO / WIDTH_RATIO);
        int finalHeightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec,finalHeightSpec);
    }
}