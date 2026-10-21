package arivihan.technologies.doubtbuzzter2.customView.squareview;

import android.content.Context;
import android.util.AttributeSet;

import androidx.cardview.widget.CardView;

public class SquareCardViewWidth extends CardView {

    public SquareCardViewWidth(Context context) {
        super(context);
    }

    public SquareCardViewWidth(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareCardViewWidth(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec); // Maintain 1:1 aspect ratio
    }
}
