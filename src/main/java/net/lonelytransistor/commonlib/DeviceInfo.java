package net.lonelytransistor.commonlib;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeviceInfo {
    public static String manufacturer = Build.MANUFACTURER;
    public static String model = Build.MODEL;
    public static String deviceType = "Unknown";
    public static String androidVersion = getAndroidVersion();
    public static int sdkVersion = Build.VERSION.SDK_INT;

    public static class Other {
        public final String manufacturer;
        public final String model;
        public final String deviceType;
        public final String androidVersion;
        public final int sdkVersion;
        public Other() {
            deviceType = "Unknown";
            androidVersion = "Android";
            sdkVersion = 0;
            manufacturer = "";
            model = "";
        }
        public Other(String data) {
            Pattern pattern = Pattern.compile(
                      "Device Type: (Unknown|Wearable|TV|Foldable Tablet|Tablet|Foldable Phone|Phone) " +
                            "Android Version: (Android [0-9]+(?:\\.[0-9]+)?|Android) " +
                            "SDK Version: ([0-9]+) " +
                            "Device Model: (\\w+) (\\w+)");
            Matcher matcher = pattern.matcher(data);
            if (matcher.find()) {
                deviceType = matcher.group(1);
                androidVersion = matcher.group(2);
                sdkVersion = Integer.parseInt(matcher.group(3));
                manufacturer = matcher.group(3);
                model = matcher.group(4);
            } else {
                deviceType = "Unknown";
                androidVersion = "Android";
                sdkVersion = 0;
                manufacturer = "";
                model = "";
            }
        }
        public String getString() {
            return toString();
        }
        @NonNull
        @Override
        public String toString() {
            return  "Device Type: " + deviceType + " " +
                    "Android Version: " + androidVersion + " " +
                    "SDK Version: " + sdkVersion + " " +
                    "Device Model: " + manufacturer + " " + model;
        }
    }

    public static void initialize(Context context) {
        deviceType = deviceType == null ? getDeviceType(context) : deviceType;
    }

    private static String getAndroidVersion() {
        int androidVersion = Build.VERSION.SDK_INT;
        String versionNumber;
        switch (androidVersion) {
            case Build.VERSION_CODES.CUPCAKE:
                versionNumber = "1.5";
                break;
            case Build.VERSION_CODES.DONUT:
                versionNumber = "1.6";
                break;
            case Build.VERSION_CODES.ECLAIR:
                if ("ECLAIR_MR1".equals(Build.VERSION.INCREMENTAL)) {
                    versionNumber = "2.1";
                } else {
                    versionNumber = "2.0";
                }
                break;
            case Build.VERSION_CODES.FROYO:
                versionNumber = "2.2";
                break;
            case Build.VERSION_CODES.GINGERBREAD:
                versionNumber = "2.3";
                break;
            case Build.VERSION_CODES.HONEYCOMB:
                if ("HONEYCOMB_MR1".equals(Build.VERSION.INCREMENTAL)) {
                    versionNumber = "3.0";
                } else if ("HONEYCOMB_MR2".equals(Build.VERSION.INCREMENTAL)) {
                    versionNumber = "3.1";
                } else {
                    versionNumber = "3.2";
                }
                break;
            case Build.VERSION_CODES.ICE_CREAM_SANDWICH:
                versionNumber = "4.0";
                break;
            case Build.VERSION_CODES.JELLY_BEAN:
                if ("JELLY_BEAN_MR1".equals(Build.VERSION.INCREMENTAL)) {
                    versionNumber = "4.1";
                } else if ("JELLY_BEAN_MR2".equals(Build.VERSION.INCREMENTAL)) {
                    versionNumber = "4.2";
                } else {
                    versionNumber = "4.3";
                }
                break;
            case Build.VERSION_CODES.KITKAT:
                versionNumber = "4.4";
                break;
            case Build.VERSION_CODES.LOLLIPOP:
                if ("LOLLIPOP_MR1".equals(Build.VERSION.INCREMENTAL)) {
                    versionNumber = "5.1";
                } else {
                    versionNumber = "2.0";
                }
                break;
            case Build.VERSION_CODES.M:
                versionNumber = "6.0";
                break;
            case Build.VERSION_CODES.N:
                if ("N_MR1".equals(Build.VERSION.INCREMENTAL)) {
                    versionNumber = "7.1";
                } else {
                    versionNumber = "7.0";
                }
                break;
            case Build.VERSION_CODES.O:
                if ("O_MR1".equals(Build.VERSION.INCREMENTAL)) {
                    versionNumber = "8.1";
                } else {
                    versionNumber = "8.0";
                }
                break;
            case Build.VERSION_CODES.P:
                versionNumber = "9.0";
                break;
            case Build.VERSION_CODES.Q:
                versionNumber = "10";
                break;
            case Build.VERSION_CODES.R:
                versionNumber = "11";
                break;
            case Build.VERSION_CODES.S:
                versionNumber = "12";
                break;
            case Build.VERSION_CODES.S_V2:
                versionNumber = "12L";
                break;
            case Build.VERSION_CODES.TIRAMISU:
                versionNumber = "13";
                break;
            case Build.VERSION_CODES.UPSIDE_DOWN_CAKE:
                versionNumber = "14";
                break;
            default:
                versionNumber = null;
                break;
        }
        return versionNumber != null ? "Android " + versionNumber : "Android";
    }
    private static int getSmallestWidth(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
            return (int) (Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels)
                    / displayMetrics.density);
        } else {
            return 0;
        }
    }
    private static String getDeviceType(Context context) {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                && (Build.DEVICE.equals("glass_1") || Build.DEVICE.equals("glass_1_5")))
                || Build.DEVICE.equals("wearable")) {
            return "Wearable";
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && Build.TYPE.equals("tv")) {
            return "TV";
        } else if (getSmallestWidth(context) >= 600) {
            PackageManager packageManager = context.getPackageManager();
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_HINGE_ANGLE)) {
                return "Foldable Tablet";
            } else {
                return "Tablet";
            }
        } else {
            PackageManager packageManager = context.getPackageManager();
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_HINGE_ANGLE)) {
                return "Foldable Phone";
            } else {
                return "Phone";
            }
        }
    }
    @NonNull
    public static String getString() {
        return  "Device Type: " + deviceType + " " +
                "Android Version: " + androidVersion + " " +
                "SDK Version: " + sdkVersion + " " +
                "Device Model: " + manufacturer + " " + model;
    }
}
