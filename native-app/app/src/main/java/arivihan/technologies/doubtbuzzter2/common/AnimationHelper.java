package arivihan.technologies.doubtbuzzter2.common;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;

public class AnimationHelper {

    public static void shakeDialog(Dialog dialog) {

        try {
            if (dialog == null || dialog.getWindow() == null) return;

            View decorView = dialog.getWindow().getDecorView();

            ObjectAnimator animator = ObjectAnimator.ofFloat(
                    decorView,
                    "translationX",
                    0, 25, -25, 20, -20, 15, -15, 10, -10, 5, -5, 0
            );

            animator.setDuration(500);
            animator.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void vibrate(Context context) {
        try {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator == null) return;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                        VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE )
                );
            } else {
                vibrator.vibrate(200);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void shakeDialogAndVibrate(
            Dialog dialog,
            Context context
    ) {
        shakeDialog(dialog);
        vibrate(context);
    }
}