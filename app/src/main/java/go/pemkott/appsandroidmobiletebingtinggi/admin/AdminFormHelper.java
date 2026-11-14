package go.pemkott.appsandroidmobiletebingtinggi.admin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Pattern;

import retrofit2.Response;

/**
 * Pure helper functions for Super Admin forms: input validation, attendance type mapping,
 * coordinate validation, and structured API error extraction.
 */
public final class AdminFormHelper {

    public static final String[] POSISI_VALUES = {
            "masuk",
            "tl-masuk",
            "pd",
            "sk",
            "kp",
            "ct"
    };

    public static final String[] STATUS_VALUES = {
            "hadir",
            "dinas",
            "dinas",
            "sakit",
            "pribadi",
            "ijin"
    };

    private static final Pattern TIME_PATTERN = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d(:[0-5]\\d)?$");
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    private AdminFormHelper() {
    }

    public static final class AttendanceMapping {
        public final String posisi;
        public final String status;
        public final String validasi;

        public AttendanceMapping(String posisi, String status, String validasi) {
            this.posisi = posisi;
            this.status = status;
            this.validasi = validasi;
        }
    }

    public static AttendanceMapping buildUpdateMasukMapping(int index) {
        if (index < 0 || index >= POSISI_VALUES.length) {
            throw new IllegalArgumentException("Indeks jenis absensi tidak valid: " + index);
        }
        return new AttendanceMapping(POSISI_VALUES[index], STATUS_VALUES[index], "2");
    }

    public static boolean isValidDate(String date) {
        if (date == null || !DATE_PATTERN.matcher(date.trim()).matches()) {
            return false;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sdf.setLenient(false);
        try {
            sdf.parse(date.trim());
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static boolean isValidTime(String time, boolean allowAlphaPlaceholder) {
        if (time == null) {
            return false;
        }
        String trimmed = time.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        if (allowAlphaPlaceholder && "al".equalsIgnoreCase(trimmed)) {
            return true;
        }
        return TIME_PATTERN.matcher(trimmed).matches();
    }

    public static boolean isValidCoordinates(String latStr, String lngStr) {
        if (latStr == null || lngStr == null) {
            return false;
        }
        String latTrim = latStr.trim();
        String lngTrim = lngStr.trim();
        if (latTrim.isEmpty() || lngTrim.isEmpty()) {
            return false;
        }
        try {
            double lat = Double.parseDouble(latTrim);
            double lng = Double.parseDouble(lngTrim);
            if (Double.isNaN(lat) || Double.isInfinite(lat) || Double.isNaN(lng) || Double.isInfinite(lng)) {
                return false;
            }
            return lat >= -90.0 && lat <= 90.0 && lng >= -180.0 && lng <= 180.0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static Integer parseNonNegativeInt(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return 0;
        }
        try {
            int parsed = Integer.parseInt(trimmed);
            return parsed >= 0 ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String extractErrorMessageFromJson(String body, String fallback) {
        String defaultMsg = (fallback == null || fallback.trim().isEmpty()) ? "Terjadi kesalahan pada server" : fallback;
        if (body == null || body.trim().isEmpty()) {
            return defaultMsg;
        }
        try {
            JsonObject obj = JsonParser.parseString(body).getAsJsonObject();
            if (obj.has("remarks") && !obj.get("remarks").isJsonNull()) {
                String remarks = obj.get("remarks").getAsString().trim();
                if (!remarks.isEmpty()) {
                    return remarks;
                }
            }
            if (obj.has("message") && !obj.get("message").isJsonNull()) {
                String msg = obj.get("message").getAsString().trim();
                if (!msg.isEmpty()) {
                    return msg;
                }
            }
        } catch (Exception ignored) {
            // Keep default fallback when response is not valid JSON.
        }
        return defaultMsg;
    }

    public static String extractApiError(Response<?> response, String fallback) {
        if (response == null) {
            return fallback;
        }
        String defaultMsg = (fallback == null || fallback.trim().isEmpty())
                ? ("HTTP " + response.code())
                : fallback;
        try {
            if (response.errorBody() != null) {
                String body = response.errorBody().string();
                return extractErrorMessageFromJson(body, defaultMsg);
            }
        } catch (Exception ignored) {
            // Fall through to HTTP code message.
        }
        return defaultMsg;
    }
}

