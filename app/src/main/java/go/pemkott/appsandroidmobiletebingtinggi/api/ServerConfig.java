package go.pemkott.appsandroidmobiletebingtinggi.api;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.regex.Pattern;

import go.pemkott.appsandroidmobiletebingtinggi.MyApplication;

public class ServerConfig {

    public static final String DEFAULT_HOST = "https://absensi.tebingtinggikota.go.id/";
    private static final String PREF_NAME = "server_config_prefs";
    private static final String KEY_CUSTOM_HOST = "custom_base_host";
    private static final Pattern INTERNAL_HOST_PATTERN = Pattern.compile(
            "(?i)https?://(172\\.15\\.[0-9]+\\.[0-9]+|192\\.168\\.[0-9]+\\.[0-9]+|localhost|127\\.0\\.0\\.1|10\\.0\\.2\\.2|(?:[a-z0-9-]+\\.)*absensi\\.tebingtinggikota\\.go\\.id|(?:[a-z0-9-]+-)+absensi\\.tebingtinggikota\\.go\\.id)(:[0-9]+)?"
    );

    public static String BASE_HOST = DEFAULT_HOST;

    public static void setCustomBaseHost(String host) {
        if (host == null || host.trim().isEmpty()) {
            BASE_HOST = DEFAULT_HOST;
        } else {
            String clean = host.trim();
            if (!clean.startsWith("http://") && !clean.startsWith("https://")) {
                clean = (clean.contains("172.15.") || clean.contains("192.168.") || clean.contains("localhost")
                        || clean.contains("127.0.0.1") || clean.contains(":8000"))
                        ? "http://" + clean
                        : "https://" + clean;
            }
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
                if (saved.contains("svr-absensi.tebingtinggikota.go.id")
                        || saved.contains("172.15.1.65")
                        || saved.contains(":8000")
                        || saved.startsWith("http://absensi.tebingtinggikota.go.id")) {
                    BASE_HOST = DEFAULT_HOST;
                    sp.edit().putString(KEY_CUSTOM_HOST, DEFAULT_HOST).apply();
                } else {
                    BASE_HOST = saved;
                }
            } else {
                BASE_HOST = DEFAULT_HOST;
            }
        }
        String url = BASE_HOST != null ? BASE_HOST.trim() : DEFAULT_HOST;
        if (!url.endsWith("/")) {
            return url + "/";
        }
        return url;
    }

    public static String getApiUrl() {
        return getBaseUrl() + "api/v22/";
    }

    public static String getApiV2Url() {
        return getBaseUrl() + "api/v22/";
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

    /**
     * Menormalkan URL server (IP lama, localhost, 127.0.0.1, 10.0.2.2, atau domain absensi)
     * serta path relatif agar konsisten mengarah ke host aktif ({@link #getBaseUrl()}).
     */
    public static String normalizeServerUrl(String rawUrl) {
        return normalizeServerUrl(rawUrl, getBaseUrl());
    }

    public static String normalizeServerUrl(String rawUrl, String activeBaseUrl) {
        if (rawUrl == null) {
            return null;
        }
        String trimmed = rawUrl.trim();
        if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed)) {
            return trimmed.isEmpty() ? "" : trimmed;
        }
        if (trimmed.startsWith("file://") || trimmed.startsWith("content://")) {
            return trimmed;
        }

        // PENTING: URL portal berita Diskominfo (https://diskominfo.tebingtinggikota.go.id/...)
        // tidak boleh diubah/dinormalkan ke host server absensi (https://absensi.tebingtinggikota.go.id/).
        if (trimmed.contains("diskominfo.tebingtinggikota.go.id") || trimmed.contains("/api/berita")) {
            return trimmed;
        }

        String baseWithSlash = sanitizeBaseUrl(activeBaseUrl);
        String baseNoSlash = baseWithSlash.substring(0, baseWithSlash.length() - 1);

        String formatted = INTERNAL_HOST_PATTERN.matcher(trimmed).replaceAll(baseNoSlash);
        if (formatted.startsWith("/")) {
            formatted = baseNoSlash + formatted;
        } else if (!formatted.startsWith("http://")
                && !formatted.startsWith("https://")
                && !formatted.startsWith("file://")
                && !formatted.startsWith("content://")) {
            formatted = baseWithSlash + formatted;
        }

        return formatted.replaceAll("(?<!:)/{2,}", "/");
    }

    public static String resolveStorageUrl(String rawUrl) {
        return resolveRelativeOrFullUrl(rawUrl, "storage", getBaseUrl());
    }

    public static String resolveStorageUrl(String rawUrl, String activeBaseUrl) {
        return resolveRelativeOrFullUrl(rawUrl, "storage", activeBaseUrl);
    }

    public static String resolveFotoPegawaiUrl(String rawUrl) {
        return resolveRelativeOrFullUrl(rawUrl, "storage/foto-pegawai", getBaseUrl());
    }

    public static String resolveFotoPegawaiUrl(String rawUrl, String activeBaseUrl) {
        return resolveRelativeOrFullUrl(rawUrl, "storage/foto-pegawai", activeBaseUrl);
    }

    public static String resolveUploadsAbsensiUrl(String rawUrl) {
        return resolveRelativeOrFullUrl(rawUrl, "uploads-img-absensi", getBaseUrl());
    }

    public static String resolveUploadsAbsensiUrl(String rawUrl, String activeBaseUrl) {
        return resolveRelativeOrFullUrl(rawUrl, "uploads-img-absensi", activeBaseUrl);
    }

    public static String resolveUploadsLampiranUrl(String rawUrl) {
        return resolveRelativeOrFullUrl(rawUrl, "uploads-img-lampiran", getBaseUrl());
    }

    public static String resolveUploadsLampiranUrl(String rawUrl, String activeBaseUrl) {
        return resolveRelativeOrFullUrl(rawUrl, "uploads-img-lampiran", activeBaseUrl);
    }

    static String resolveRelativeOrFullUrl(String rawUrl, String defaultPrefix, String activeBaseUrl) {
        if (rawUrl == null) {
            return null;
        }
        String trimmed = rawUrl.trim();
        if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed)) {
            return "";
        }

        // PENTING: URL portal berita Diskominfo tidak boleh diubah/dinormalkan ke host server absensi
        if (trimmed.contains("diskominfo.tebingtinggikota.go.id") || trimmed.contains("/api/berita")) {
            return trimmed;
        }
        if (trimmed.startsWith("http://")
                || trimmed.startsWith("https://")
                || trimmed.startsWith("file://")
                || trimmed.startsWith("content://")) {
            return normalizeServerUrl(trimmed, activeBaseUrl);
        }

        String rel = trimmed.replaceFirst("^/+", "");
        String prefix = defaultPrefix != null ? defaultPrefix.replaceAll("^/+|/+$", "") : "";
        String baseWithSlash = sanitizeBaseUrl(activeBaseUrl);

        String resolvedPath;
        if (prefix.isEmpty()
                || rel.startsWith(prefix + "/")
                || rel.startsWith("storage/")
                || rel.startsWith("uploads-img-absensi/")
                || rel.startsWith("uploads-img-lampiran/")) {
            resolvedPath = rel;
        } else if ("storage/foto-pegawai".equals(prefix) && rel.startsWith("foto-pegawai/")) {
            resolvedPath = "storage/" + rel;
        } else {
            resolvedPath = prefix + "/" + rel;
        }

        return (baseWithSlash + resolvedPath).replaceAll("(?<!:)/{2,}", "/");
    }

    private static String sanitizeBaseUrl(String activeBaseUrl) {
        String base = (activeBaseUrl != null && !activeBaseUrl.trim().isEmpty())
                ? activeBaseUrl.trim()
                : DEFAULT_HOST;
        if (!base.endsWith("/")) {
            base += "/";
        }
        return base;
    }
}
