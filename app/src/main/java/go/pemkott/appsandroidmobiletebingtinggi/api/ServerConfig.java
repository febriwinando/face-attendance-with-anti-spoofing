package go.pemkott.appsandroidmobiletebingtinggi.api;

public class ServerConfig {

    public static String BASE_HOST = "http://172.15.1.35:8000/";

    public static String getBaseUrl() {
        String url = BASE_HOST.trim();
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
