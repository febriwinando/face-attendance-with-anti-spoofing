package go.pemkott.appsandroidmobiletebingtinggi.api;

import android.content.Context;
import android.content.SharedPreferences;

import go.pemkott.appsandroidmobiletebingtinggi.MyApplication;

public class ServerConfig {

    public static final String DEFAULT_HOST = "http://192.168.1.6:8000/";
    private static final String PREF_NAME = "server_config_prefs";
    private static final String KEY_CUSTOM_HOST = "custom_base_host";

    public static String BASE_HOST = DEFAULT_HOST;

    public static void setCustomBaseHost(String host) {
        if (host == null || host.trim().isEmpty()) {
            BASE_HOST = DEFAULT_HOST;
        } else {
            String clean = host.trim();
            if (!clean.endsWith("/")) clean += "/";
            BASE_HOST = clean;
        }
        Context ctx = MyApplication.getAppContext();
        if (ctx != null) {
            SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            sp.edit().putString(KEY_CUSTOM_HOST, BASE_HOST).apply();
        }
    }

    public static String getBaseUrl() {
        Context ctx = MyApplication.getAppContext();
        if (ctx != null) {
            SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String saved = sp.getString(KEY_CUSTOM_HOST, null);
            if (saved != null && !saved.trim().isEmpty()) {
                BASE_HOST = saved;
            }
        }
        String url = BASE_HOST != null ? BASE_HOST.trim() : DEFAULT_HOST;
        if (!url.endsWith("/")) {
            return url + "/";
        }
        return url;
    }

    public static String getApiUrl() {
        return getBaseUrl() + "api/v21/";
    }

    public static String getApiV2Url() {
        return getBaseUrl() + "api/v21/";
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
