package arivihan.technologies.doubtbuzzter2.customView.squareview;

import android.content.Context;
import android.util.AttributeSet;

import androidx.cardview.widget.CardView;

public class SquareCardView extends CardView {

    public SquareCardView(Context context) {
        super(context);
    }

    public SquareCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(widthMeasureSpec<heightMeasureSpec)
        {
            heightMeasureSpec = widthMeasureSpec;
        }
        else
        {
            widthMeasureSpec = heightMeasureSpec;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec); // Maintain 1:1 aspect ratio
    }
}
