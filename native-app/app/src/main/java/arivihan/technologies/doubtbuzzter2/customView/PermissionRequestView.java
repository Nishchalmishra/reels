package arivihan.technologies.doubtbuzzter2.customView;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.enums.PermissionType;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class PermissionRequestView {

    private Context context;
    private Dialog dialog;

    public PermissionRequestView(Context context) {
        if (!(context instanceof Activity)) return;
        this.context = context;
    }

    public void initPermission(PermissionType type, boolean showPopup){
        if (!showPopup || isPermissionGranted(type)) return;

        switch (type) {
            case MICROPHONE:
                showMicrophonePermissionDialog();
                break;

            case NOTIFICATION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    showNotificationPermissionDialog();
                }
                break;
        }
    }

    private boolean isPermissionGranted(PermissionType type) {
        if (type == PermissionType.NOTIFICATION && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }

        String permission = (type == PermissionType.MICROPHONE)
                ? Manifest.permission.RECORD_AUDIO
                : Manifest.permission.POST_NOTIFICATIONS;

        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void showNotificationPermissionDialog() {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.notification_permission_popup);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
        dialog.getWindow().setBackgroundDrawable(context.getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);

        ImageView close = dialog.findViewById(R.id.close_btn);
        CardView btnYes = dialog.findViewById(R.id.btn_Yes);
        CardView btnNo = dialog.findViewById(R.id.btn_No);
        LottieAnimationView animationView = dialog.findViewById(R.id.animation);

        setupAnimation(animationView);

        close.setOnClickListener(v -> closeDialog(animationView, PreferenceKeys.HIDE_NOTIFICATION_POPUP.getKey()));
        btnYes.setOnClickListener(v -> {
            MyApplication.saveData(PreferenceKeys.HIDE_NOTIFICATION_POPUP.getKey(), true);
            animationView.cancelAnimation();
            dialog.dismiss();
            openAppSettings(context);
        });
        btnNo.setOnClickListener(v -> closeDialog(animationView, PreferenceKeys.HIDE_NOTIFICATION_POPUP.getKey()));

        if (isActivityValid()) dialog.show();
    }

    private void showMicrophonePermissionDialog() {
        if (!(context instanceof Activity)) return;

        if(dialog==null) {
            dialog = new Dialog(context);
            dialog.setContentView(R.layout.microphone_permission_popup);
            setupDialogWindow();

            ImageView close = dialog.findViewById(R.id.dialog_close_btn);
            ImageView centerImageView = dialog.findViewById(R.id.center_img);
            CardView goToSettings = dialog.findViewById(R.id.go_to_settings);
            TextView dialogTitle = dialog.findViewById(R.id.dialog_title);
            TextView buttonText = dialog.findViewById(R.id.button_text);

            //ani
            ImageView arrowIcon = dialog.findViewById(R.id.arrow_icon);
            startArrowLoopAnimation(arrowIcon);

            String language = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
            if (language == null || (!language.equals("hi") && !language.equals("en"))) language = "en";


            //get data from remote config
            try {
                String json = MyApplication.getFirebaseRemoteConfig().getString("mic_permission_popup_data");
                if (StringUtils.isEmpty(json)) return;

                //data
                JSONObject data = new JSONObject(json).optJSONObject("mic_permission_popup_data");
                if (data == null) return;

                String title   = data.optJSONObject("title").optString(language);
                String imageUrl   = data.optJSONObject("image_url").optString(language);
                String btnText = data.optJSONObject("btn_text").optString(language);

                dialogTitle.setText(title);
                buttonText.setText(btnText);

                Glide.with(context)
                        .load(imageUrl)
                        .error(R.drawable.white_logo)
                        .into(centerImageView);

            } catch (Exception e) {
                Log.e("PermissionPopup", "JSON parse error: " + e.getMessage());
                return;
            }

            close.setOnClickListener(v -> {
                if (listener != null) listener.onDialogClose();
                stopArrowLoopAnimation(arrowIcon);
                dialog.dismiss();
                dialog = null;
            });

            goToSettings.setOnClickListener(v -> {
                MyApplication.saveData(PreferenceKeys.HIDE_MICROPHONE_POPUP.getKey(), true);
                if (listener != null) listener.goToSettings();
                stopArrowLoopAnimation(arrowIcon);
                dialog.dismiss();
                dialog=null;
                openAppSettings(context);
            });
        }

        if (isActivityValid()) dialog.show();
    }


    private boolean isActivityValid() {
        if (!(context instanceof Activity)) return false;
        Activity activity = (Activity) context;
        return !activity.isFinishing() && !activity.isDestroyed();
    }

    //ani
    public static void startArrowLoopAnimation(ImageView arrow) {
        if (arrow == null) return;
        arrow.setVisibility(View.VISIBLE);

        Runnable loop = new Runnable() {
            @Override
            public void run() {
                if (arrow.getWindowToken() == null) return;
                float travel = arrow.getWidth() / 2f;
                arrow.setTranslationX(-travel);
                arrow.setAlpha(0f);
                arrow.animate()
                        .translationX(0f)
                        .alpha(1f)
                        .setDuration(400)
                        .withEndAction(() -> arrow.postDelayed(() ->
                                        arrow.animate()
                                                .translationX(travel)
                                                .alpha(0f)
                                                .setDuration(400)
                                                .withEndAction(this)
                                                .start()
                                , 400))
                        .start();
            }
        };

        arrow.post(loop);
    }

    public static void stopArrowLoopAnimation(ImageView arrow) {
        if (arrow == null) return;
        arrow.animate().cancel();
        arrow.removeCallbacks(null);
        arrow.setVisibility(View.GONE);
    }


    private void setupDialogWindow() {
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationUpDown;
        dialog.getWindow().setBackgroundDrawable(context.getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.90f);
        dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
    }

    private void setupAnimation(LottieAnimationView animationView) {
        animationView.playAnimation();
        animationView.loop(true);
        animationView.setSpeed(1.5f);
    }

    private void closeDialog(LottieAnimationView animationView, String preferenceKey) {
        MyApplication.saveData(preferenceKey, true);
        animationView.cancelAnimation();
        dialog.dismiss();
    }
    public void closeDialog(){
        if(dialog!=null){
            dialog.dismiss();
            dialog = null;
        }
    }

    public static void openAppSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
    }

    public interface PermissionListener {
        void onDialogClose();
        void goToSettings();
    }

    private PermissionListener listener;

    public PermissionRequestView setPermissionListener(PermissionListener listener) {
        this.listener = listener;
        return this;
    }
}
