package arivihan.technologies.doubtbuzzter2.helper;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.File;

public class ImageLoaderUtil extends SubsamplingScaleImageView {

    public ImageLoaderUtil(Context context) {
        super(context);
    }

    public ImageLoaderUtil(Context context, AttributeSet attr) {
        super(context,attr);
    }

    public void loadImage(String imageUrl) {
        Glide.with(getContext())
                .asFile()
                .load(imageUrl)
                .into(new CustomTarget<File>() {
                    @Override
                    public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(resource.getAbsolutePath(), options);
                        int imageHeight = options.outHeight;

                        int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

                        setImage(ImageSource.uri(Uri.fromFile(resource)));
                        if (imageHeight > screenHeight) {
                            setMinimumScaleType(SCALE_TYPE_START);
                        } else {
                            setMinimumScaleType(SCALE_TYPE_CENTER_INSIDE);
                        }
                        setZoomEnabled(false);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }

                });
    }
}