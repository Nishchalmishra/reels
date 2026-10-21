package arivihan.technologies.doubtbuzzter2.customView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.StepViewModel;

public class CustomStepView extends LinearLayout {

    private MaterialCardView boundary;
    private MaterialCardView dot;
    private ImageView check;

    public CustomStepView(Context context) {
        super(context);
        init(context);
    }

    public CustomStepView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomStepView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomStepView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.step_view_layout, this, true);
        setOrientation(HORIZONTAL);

        boundary = view.findViewById(R.id.boundary);
        dot = view.findViewById(R.id.dot);
        check = view.findViewById(R.id.check);

        view.setOnClickListener(v -> {

        });
    }

    public void changeState(StepViewModel stepViewModel){
        if (stepViewModel.isDone()) {
            boundary.setVisibility(View.GONE);
            check.setVisibility(View.VISIBLE);
        } else if (stepViewModel.isCurrent()) {
            check.setVisibility(View.GONE);
            boundary.setVisibility(View.VISIBLE);
            boundary.setStrokeColor(getResources().getColor(R.color.mock_primary_color));
            dot.setVisibility(View.VISIBLE);
            dot.setCardBackgroundColor(getResources().getColor(R.color.mock_primary_color));
        } else if (stepViewModel.isNext()) {
            check.setVisibility(View.GONE);
            boundary.setVisibility(View.VISIBLE);
            boundary.setStrokeColor(getResources().getColor(R.color.step_view_dot));
            dot.setCardBackgroundColor(getResources().getColor(R.color.step_dot));
            dot.setVisibility(View.VISIBLE);
        } else {
            boundary.setVisibility(View.VISIBLE);
            boundary.setStrokeColor(getResources().getColor(R.color.step_view_dot));
            check.setVisibility(View.GONE);
            dot.setVisibility(View.GONE);
        }
    }

    public void onStepClickListener(OnClickListener clickListener){
        this.setOnClickListener(clickListener);
    }
}
