package arivihan.technologies.doubtbuzzter2.common;

import static arivihan.technologies.doubtbuzzter2.enums.FullViewImageType.QUESTION;
import static arivihan.technologies.doubtbuzzter2.enums.FullViewImageType.SOLUTION;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.File;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.enums.FullViewImageType;

public class FullScreenImageDialog extends Dialog {
    private static final String TAG = "FullScreenImageDialog";

    public FullScreenImageDialog(@NonNull Context context, @NonNull String imageUrl, FullViewImageType type) {
        super(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        Log.d(TAG, "Creating FullScreenImageDialog with URL: " + imageUrl);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_full_view_image);
        getWindow().setBackgroundDrawableResource(android.R.color.black);
        setCancelable(true);

        SubsamplingScaleImageView imageView = findViewById(R.id.fullscreen_image);
        ImageView closeBtn = findViewById(R.id.close_dialog_btn);
        switch (type) {
            case QUESTION:
                imageView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.black));
                closeBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.close_filled_icon));
                break;

            case SOLUTION:
                imageView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
                closeBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_close_black));
                break;
        }


        if (imageView == null) {
            Log.e(TAG, "Failed to find fullscreen_image view");
            return;
        }
        if (closeBtn == null) {
            Log.e(TAG, "Failed to find close_btn view");
            return;
        }

        Log.d(TAG, "Starting Glide image load");
        Glide.with(getContext())
                .asFile()
                .load(imageUrl)
                .into(new CustomTarget<File>() {
                    @Override
                    public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                        Log.d(TAG, "Image loaded successfully from URL: " + imageUrl);
                        Log.d(TAG, "Image file path: " + resource.getAbsolutePath());

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(resource.getAbsolutePath(), options);

                        int imageHeight = options.outHeight;
                        int imageWidth = options.outWidth;

                        int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
                        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

                        Log.d(TAG, "Image dimensions - Width: " + imageWidth + ", Height: " + imageHeight);
                        Log.d(TAG, "Screen dimensions - Width: " + screenWidth + ", Height: " + screenHeight);

                        float imageAspectRatio = (float) imageHeight / imageWidth;
                        float screenAspectRatio = (float) screenHeight / screenWidth;

                        Log.d(TAG, "Image Aspect Ratio: " + imageAspectRatio);
                        Log.d(TAG, "Screen Aspect Ratio: " + screenAspectRatio);

                        imageView.setImage(ImageSource.uri(Uri.fromFile(resource)));

                        if (imageAspectRatio > screenAspectRatio * 1.1f) {
                            Log.d(TAG, "Tall image compared to screen – using SCALE_TYPE_START");
                            imageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_START);
                            if (type == FullViewImageType.QUESTION) {
                                closeBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.close_filled_icon));
                            }
                        } else {
                            Log.d(TAG, "Normal/wide image – using SCALE_TYPE_CENTER_INSIDE");
                            imageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE);
                        }

                        imageView.setMaxScale(5.0f);
                        imageView.setMinScale(1.0f);
                        imageView.setDoubleTapZoomScale(2.0f);
                        imageView.setZoomEnabled(true);
                        Log.d(TAG, "Image setup completed");
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        Log.d(TAG, "Image load cleared");
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        Log.e(TAG, "Failed to load image from URL: " + imageUrl);
                        super.onLoadFailed(errorDrawable);
                    }
                });

        closeBtn.setOnClickListener(v -> {
            Log.d(TAG, "Close button clicked");
            dismiss();
        });
    }

}