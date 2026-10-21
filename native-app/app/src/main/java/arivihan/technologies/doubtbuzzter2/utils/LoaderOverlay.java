package arivihan.technologies.doubtbuzzter2.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import arivihan.technologies.doubtbuzzter2.R;

public class LoaderOverlay {

    private static Dialog dialog;

    // 1. Define the modes to easily switch between them
    public enum TargetMode {
        TO_BOARD,
        TO_NEET
    }

    // 2. Pass the mode into the show method instead of a plain string
    public static void show(Context context, TargetMode mode) {

        if (dialog != null && dialog.isShowing()) return;

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.loader_overlay);

        // Find the views
        FrameLayout rootLayout = dialog.findViewById(R.id.parent);
        LottieAnimationView lottieAnimationView = dialog.findViewById(R.id.lottieAnimationView);
        TextView tvLoaderText = dialog.findViewById(R.id.tvLoaderText);

        // 3. Dynamically set the assets based on the target mode
        if (mode == TargetMode.TO_BOARD) {
            // Set Board Background
            rootLayout.setBackgroundResource(R.drawable.loader_gradient_bg);
            // Set Board Animation (Make sure you have a board_anim.json in res/raw/)
            lottieAnimationView.setAnimation(R.raw.circle_loader_animation);
            // Set Board Text
            tvLoaderText.setText("Switching to \n Board Mode...");

        } else if (mode == TargetMode.TO_NEET) {
            // Set NEET Background
            rootLayout.setBackgroundResource(R.drawable.loader_gradient_bg_2);
            lottieAnimationView.setAnimation(R.raw.circle_loader_animation_2);
            tvLoaderText.setText("Switching to \n NEET Mode...");
        }

        dialog.setCancelable(false);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();
    }

    public static void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }
}