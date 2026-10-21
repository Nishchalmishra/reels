package arivihan.technologies.doubtbuzzter2.customView;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;

import arivihan.technologies.doubtbuzzter2.R;

public class ResponsiveTextView extends androidx.appcompat.widget.AppCompatTextView {

    private float baseTextSizeDp = 14f;  // default, can be customized from XML

    public ResponsiveTextView(Context context) {
        super(context);
        init(context, null);
    }

    public ResponsiveTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ResponsiveTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ResponsiveTextView);
            baseTextSizeDp = a.getDimension(R.styleable.ResponsiveTextView_baseTextSize, baseTextSizeDp);
            a.recycle();
        }

        setTextSizeInDp(baseTextSizeDp);
    }

    private void setTextSizeInDp(float baseSizeDp) {
        float screenWidthPx = Resources.getSystem().getDisplayMetrics().widthPixels;
        float density = Resources.getSystem().getDisplayMetrics().density;
        float screenWidthDp = screenWidthPx / density;

        // Scaling factor logic
        float scaleFactor = 1f;
        if (screenWidthDp <= 320) scaleFactor = 0.85f;
        else if (screenWidthDp <= 360) scaleFactor = 1.0f;
        else if (screenWidthDp <= 400) scaleFactor = 1.15f;
        else scaleFactor = 1.25f;

        float finalPx = baseSizeDp * scaleFactor * density;

        setTextSize(TypedValue.COMPLEX_UNIT_PX, finalPx);
    }
}
