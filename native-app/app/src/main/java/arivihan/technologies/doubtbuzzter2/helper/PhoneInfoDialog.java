package arivihan.technologies.doubtbuzzter2.helper;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.firebase.client.Firebase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.io.File;
import java.text.DecimalFormat;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.helper.DeviceInfoUtil;
import arivihan.technologies.doubtbuzzter2.utils.ShareUtils;

public class PhoneInfoDialog extends Dialog {

    private TextView tvModelName, tvAndroidVersion, tvRam, tvStorage;
    private LinearLayout ll_phone_specification;
    private ImageView btnClose;
    private CardView btnShare;
    private Context context;

    public PhoneInfoDialog(@NonNull Context context) {
        super(context);
        this.context = context;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.phone_info_layout);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            setCancelable(true);
        }

        initViews();
        loadDeviceInfo();
        setupListeners();
    }

    private void initViews() {
        tvModelName = findViewById(R.id.tvModelName);
        tvAndroidVersion = findViewById(R.id.tvAndroidVersion);
        tvRam = findViewById(R.id.tvRam);
        tvStorage = findViewById(R.id.tvStorage);
        ll_phone_specification = findViewById(R.id.ll_phone_specification);
        btnClose = findViewById(R.id.btnClose);
        btnShare = findViewById(R.id.cv_share);
    }

    private void loadDeviceInfo() {
        // Get device model
        String deviceModel = DeviceInfoUtil.getDeviceModel();
        tvModelName.setText(deviceModel);

        // Get Android version
        String androidVersion = DeviceInfoUtil.getAndroidVersion();
        tvAndroidVersion.setText(androidVersion);

        // Get RAM info
        long totalRamMB = getTotalRAM();
        tvRam.setText(formatMemory(totalRamMB));

        long totalStorageGB = getTotalStorage();
        tvStorage.setText(formatStorage(totalStorageGB));
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> dismiss());

        btnShare.setOnClickListener(v -> shareDeviceInfo());
    }

    private long getTotalRAM() {
        try {
            android.app.ActivityManager actManager =
                    (android.app.ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            android.app.ActivityManager.MemoryInfo memInfo = new android.app.ActivityManager.MemoryInfo();
            actManager.getMemoryInfo(memInfo);

            // Convert bytes to MB
            long totalMemoryMB = memInfo.totalMem / (1024 * 1024);
            return totalMemoryMB;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private long getTotalStorage() {
        try {
            File path = android.os.Environment.getDataDirectory();
            android.os.StatFs stat = new android.os.StatFs(path.getPath());

            long blockSize = stat.getBlockSizeLong();
            long totalBlocks = stat.getBlockCountLong();

            // Convert bytes to GB
            long totalStorageGB = (totalBlocks * blockSize) / (1024 * 1024 * 1024);
            return totalStorageGB;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private String formatMemory(long totalMemoryMB) {
        if (totalMemoryMB >= 1024) {
            double memoryGB = totalMemoryMB / 1024.0;
            DecimalFormat df = new DecimalFormat("#.#");
            return df.format(memoryGB) + " GB";
        } else {
            return totalMemoryMB + " MB";
        }
    }

    private String formatStorage(long totalStorageGB) {
        if (totalStorageGB >= 1024) {
            double storageTB = totalStorageGB / 1024.0;
            DecimalFormat df = new DecimalFormat("#.#");
            return df.format(storageTB) + " TB";
        } else {
            return totalStorageGB + " GB";
        }
    }

    private void shareDeviceInfo() {
        String deviceModel = tvModelName.getText().toString();
        String androidVersion = tvAndroidVersion.getText().toString();
        String ram = tvRam.getText().toString();
        String storage = tvStorage.getText().toString();

        String shareText = "📱 My Phone Specifications\n\n" +
                "Model Name: " + deviceModel + "\n" +
                "Android Version: " + androidVersion + "\n" +
                "RAM: " + ram + "\n" +
                "Storage: " + storage + "\n\n";

        Intent shareIntent = ShareUtils.shareDialogAsImage(context, getLinearLayoutBitmap(ll_phone_specification));

        if (shareIntent == null || MyApplication.getFirebaseRemoteConfig().getBoolean("settings_phone_info_force_text_enabled")) {
            // fallback to text-only
            Intent textIntent = new Intent(Intent.ACTION_SEND);
            textIntent.setType("text/plain");
            textIntent.putExtra(Intent.EXTRA_TEXT, shareText);

            Intent chooser = Intent.createChooser(textIntent, "Share phone info");
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chooser);
            return;
        }

        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_TITLE, shareText);
        shareIntent.setType("image/png");

        Intent chooser = Intent.createChooser(shareIntent, "Share");
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(chooser);
        } catch (Exception e) {
            Toast.makeText(context, "No app available to share image", Toast.LENGTH_SHORT).show();
        }
    }

    public static Bitmap getLinearLayoutBitmap(LinearLayout linearLayout)
    {
        linearLayout.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(linearLayout.getWidth(), linearLayout.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        linearLayout.draw(canvas);
        linearLayout.setDrawingCacheEnabled(false);
        return bitmap;
    }
}