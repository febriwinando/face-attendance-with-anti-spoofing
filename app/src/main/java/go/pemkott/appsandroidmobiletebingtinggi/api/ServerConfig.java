package go.pemkott.appsandroidmobiletebingtinggi.api;

import android.content.Context;
import android.os.Build;
import go.pemkott.appsandroidmobiletebingtinggi.MyApplication;
import go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager;

public class ServerConfig {

    public static String BASE_HOST = "https://svr-absensi.tebingtinggikota.go.id/";

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
        Context context = MyApplication.getAppContext();
        if (context != null) {
            SessionManager session = new SessionManager(context);
            String savedHost = session.getServerHost();
            if (savedHost != null && !savedHost.trim().isEmpty()) {
                String url = savedHost.trim();
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "http://" + url;
                }
                if (!url.endsWith("/")) {
                    url = url + "/";
                }
                return url;
            }
        }

        if (isEmulator()) {
            return "https://svr-absensi.tebingtinggikota.go.id/";
        }

        if (BASE_HOST == null || BASE_HOST.trim().isEmpty()) {
            return "https://svr-absensi.tebingtinggikota.go.id/";
        }

        String url = BASE_HOST.trim();
        if (!url.endsWith("/")) {
            return url + "/";
        }
        return url;
    }

    public static String getApiUrl() {
        return getBaseUrl() + "api/";
    }

    public static String getApiV2Url() {
        return getBaseUrl() + "api/v2/";
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
