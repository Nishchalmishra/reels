package arivihan.technologies.doubtbuzzter2.common;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.google.android.material.card.MaterialCardView;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.HomeScreen;

public class SlowInternetPopUp extends Dialog {

    public SlowInternetPopUp(@NonNull Context context) {
        super(context);
        setContentView(R.layout.slow_internet_pop_up); // Ensure this layout exists
        getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        setCancelable(false);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        initializeViews(context);
    }

    private void initializeViews(Context context) {
        if (context == null) {
            return; // Return early if context is null to avoid crashes
        }

        MaterialCardView retryButton = findViewById(R.id.retry_button);
        if (retryButton != null) {
            retryButton.setOnClickListener(v -> {
                dismiss();
                if (context instanceof Activity currentActivity) {
                    Intent restartIntent = currentActivity.getIntent();
                    restartIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    currentActivity.finish();
                    currentActivity.startActivity(restartIntent);
                }
            });
        }
    }

    public void showSlowInternetDialog() {
        if (!isShowing()) {
            try {
                show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}
