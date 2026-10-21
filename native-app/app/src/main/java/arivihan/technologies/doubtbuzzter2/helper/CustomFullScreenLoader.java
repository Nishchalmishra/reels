package arivihan.technologies.doubtbuzzter2.helper;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.view.ViewGroup;

import arivihan.technologies.doubtbuzzter2.R;

public class CustomFullScreenLoader {
    private Context context;
    private Dialog loadingDialog;

    public CustomFullScreenLoader(Context context) {
        this.context = context;
    }

    public void showLoader() {
        if (context == null) return;

        // Check if context is an activity and it's not finishing or destroyed
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity.isFinishing() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed())) {
                return; // Don't show dialog if activity is gone
            }
        }

        if (loadingDialog == null) {
            loadingDialog = new Dialog(context);
            loadingDialog.setContentView(R.layout.custom_dialog_loading);
            if (loadingDialog.getWindow() != null) {
                loadingDialog.getWindow().setBackgroundDrawable(context.getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
                loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
            loadingDialog.setCancelable(false);
        }

        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    public boolean isShowing(){
        return loadingDialog != null && loadingDialog.isShowing();
    }

    public void hideLoader() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

}
