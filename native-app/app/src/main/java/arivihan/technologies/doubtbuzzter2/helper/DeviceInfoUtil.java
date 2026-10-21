package arivihan.technologies.doubtbuzzter2.helper;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.provider.Settings;
import android.util.Log;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class DeviceInfoUtil {

    public interface DeviceInfoCallback {
        void onDeviceInfoReady(Map<String, Object> deviceInfo);
    }

    public static void getDeviceInfoAsync(Context context, DeviceInfoCallback callback) {
        new Thread(() -> {
            Map<String, Object> deviceInfo = new HashMap<>();
            deviceInfo.put("androidVersion", getAndroidVersion());
            deviceInfo.put("appVersion", getVersionName(context));
            deviceInfo.put("versionCode", getVersionCode(context));
            deviceInfo.put("deviceModel", getDeviceModel());
            deviceInfo.put("deviceBrand", getDeviceBrand());
            deviceInfo.put("deviceManufacturer", getDeviceManufacturer());
            deviceInfo.put("totalRAM", getTotalRAM(context));
            deviceInfo.put("totalStorage", getTotalStorage());
            deviceInfo.put("availableStorage", getAvailableStorage());
            deviceInfo.put("sdkVersion", Build.VERSION.SDK_INT);

            // Return to UI thread
            new Handler(Looper.getMainLooper()).post(() -> callback.onDeviceInfoReady(deviceInfo));
        }).start();
    }

    public static String getVersionName(Context context) {
        try {
            PackageInfo packageInfo = getPackageInfo(context);
            return packageInfo.versionName != null ? packageInfo.versionName : "unknown";
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown";
        }
    }

    public static int getVersionCode(Context context) {
        try {
            PackageInfo packageInfo = getPackageInfo(context);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return (int) packageInfo.getLongVersionCode();
            } else {
                return packageInfo.versionCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static PackageInfo getPackageInfo(Context context) throws PackageManager.NameNotFoundException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return context.getPackageManager()
                    .getPackageInfo(context.getPackageName(),
                            PackageManager.PackageInfoFlags.of(0));
        } else {
            return context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
        }
    }

    public static String getAndroidVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getDeviceModel() {
        return Build.MODEL;
    }

    public static String getDeviceBrand() {
        return Build.BRAND;
    }

    public static String getDeviceManufacturer() {
        return Build.MANUFACTURER;
    }

    public static String getDeviceId(Context context) {
        try {
            String id = Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );
            if (id == null || id.isEmpty() || "9774d56d682e549c".equals(id)) {
                return "unknown";
            }
            return id;
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * Get total RAM in MB
     */
    public static long getTotalRAM(Context context) {
        try {
            ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            actManager.getMemoryInfo(memInfo);

            // Convert bytes to MB
            return memInfo.totalMem / (1024 * 1024);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Get available RAM in MB
     */
    public static long getAvailableRAM(Context context) {
        try {
            ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            actManager.getMemoryInfo(memInfo);

            // Convert bytes to MB
            return memInfo.availMem / (1024 * 1024);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Get total internal storage in GB
     */
    public static long getTotalStorage() {
        try {
            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());

            long blockSize = stat.getBlockSizeLong();
            long totalBlocks = stat.getBlockCountLong();

            // Convert bytes to GB
            return (totalBlocks * blockSize) / (1024 * 1024 * 1024);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Get available internal storage in GB
     */
    public static long getAvailableStorage() {
        try {
            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());

            long blockSize = stat.getBlockSizeLong();
            long availableBlocks = stat.getAvailableBlocksLong();

            // Convert bytes to GB
            return (availableBlocks * blockSize) / (1024 * 1024 * 1024);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Format memory in MB or GB
     */
    public static String formatMemory(long totalMemoryMB) {
        if (totalMemoryMB >= 1024) {
            double memoryGB = totalMemoryMB / 1024.0;
            DecimalFormat df = new DecimalFormat("#.#");
            return df.format(memoryGB) + " GB";
        } else {
            return totalMemoryMB + " MB";
        }
    }

    /**
     * Format storage in GB or TB
     */
    public static String formatStorage(long totalStorageGB) {
        if (totalStorageGB >= 1024) {
            double storageTB = totalStorageGB / 1024.0;
            DecimalFormat df = new DecimalFormat("#.#");
            return df.format(storageTB) + " TB";
        } else {
            return totalStorageGB + " GB";
        }
    }

    /**
     * Get complete device info as formatted string
     */
    public static String getDeviceInfoAsString(Context context) {
        StringBuilder info = new StringBuilder();
        info.append("Device Model: ").append(getDeviceModel()).append("\n");
        info.append("Brand: ").append(getDeviceBrand()).append("\n");
        info.append("Manufacturer: ").append(getDeviceManufacturer()).append("\n");
        info.append("Android Version: ").append(getAndroidVersion()).append("\n");
        info.append("SDK Version: ").append(Build.VERSION.SDK_INT).append("\n");
        info.append("RAM: ").append(formatMemory(getTotalRAM(context))).append("\n");
        info.append("Storage: ").append(formatStorage(getTotalStorage())).append("\n");
        info.append("App Version: ").append(getVersionName(context)).append("\n");
        info.append("Version Code: ").append(getVersionCode(context));
        return info.toString();
    }
}