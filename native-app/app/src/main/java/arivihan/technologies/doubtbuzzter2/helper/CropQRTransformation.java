package arivihan.technologies.doubtbuzzter2.helper;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import java.security.MessageDigest;
import arivihan.technologies.doubtbuzzter2.enums.firebase.RemoteConfigKey;

public class CropQRTransformation extends BitmapTransformation {

    // Defaults — used when Remote Config hasn't fetched yet or key is absent
    private static final float DEFAULT_LEFT   = 0.18f;
    private static final float DEFAULT_TOP    = 0.38f;
    private static final float DEFAULT_RIGHT  = 0.82f;
    private static final float DEFAULT_BOTTOM = 0.65f;

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool,
                               @NonNull Bitmap toTransform,
                               int outWidth, int outHeight) {

        FirebaseRemoteConfig rc = FirebaseRemoteConfig.getInstance();

        float left   = getFloat(rc, RemoteConfigKey.QR_CROP_LEFT,   DEFAULT_LEFT);
        float top    = getFloat(rc, RemoteConfigKey.QR_CROP_TOP,    DEFAULT_TOP);
        float right  = getFloat(rc, RemoteConfigKey.QR_CROP_RIGHT,  DEFAULT_RIGHT);
        float bottom = getFloat(rc, RemoteConfigKey.QR_CROP_BOTTOM, DEFAULT_BOTTOM);

        int w     = toTransform.getWidth();
        int h     = toTransform.getHeight();
        int x     = (int) (w * left);
        int y     = (int) (h * top);
        int cropW = (int) (w * (right  - left));
        int cropH = (int) (h * (bottom - top));

        return Bitmap.createBitmap(toTransform, x, y, cropW, cropH);
    }

    private float getFloat(FirebaseRemoteConfig rc, RemoteConfigKey key, float defaultValue) {
        double value = rc.getDouble(key.getKey());
        // getDouble returns 0.0 when the key is absent/unset
        return (value != 0.0) ? (float) value : defaultValue;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest md) {
        FirebaseRemoteConfig rc = FirebaseRemoteConfig.getInstance();
        String cacheKey = "CropQRTransformation"
                + rc.getDouble(RemoteConfigKey.QR_CROP_LEFT.getKey())
                + rc.getDouble(RemoteConfigKey.QR_CROP_TOP.getKey())
                + rc.getDouble(RemoteConfigKey.QR_CROP_RIGHT.getKey())
                + rc.getDouble(RemoteConfigKey.QR_CROP_BOTTOM.getKey());
        md.update(cacheKey.getBytes());
    }
}