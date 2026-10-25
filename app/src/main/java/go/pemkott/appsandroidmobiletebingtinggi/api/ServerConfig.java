package go.pemkott.appsandroidmobiletebingtinggi.api;

import android.os.Build;

public class ServerConfig {

    public static String BASE_HOST = "http://127.0.0.1:8000/";

    public static boolean isEmulator() {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT));
    }

    public static String getBaseUrl() {
        if (BASE_HOST == null || BASE_HOST.isEmpty()) {
            return isEmulator() ? "http://10.0.2.2:8000/" : "http://127.0.0.1:8000/";
        }

        String url = BASE_HOST;
        if (isEmulator() && url.contains("127.0.0.1")) {
            url = url.replace("127.0.0.1", "10.0.2.2");
        }

        if (!url.endsWith("/")) {
            return url + "/";
        }
        return url;
    }

    public static String getApiUrl() {
        return getBaseUrl() + "api/";
    }

    public static String getStorageUrl() {
        return getBaseUrl() + "storage/";
    }

    public static String getUploadsAbsensiUrl() {
        return getBaseUrl() + "uploads-img-absensi/";
    }

    public static String getUploadsLampiranUrl() {
        return getBaseUrl() + "uploads-img-lampiran/";
    }
}
