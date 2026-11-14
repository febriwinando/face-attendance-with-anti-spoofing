package go.pemkott.appsandroidmobiletebingtinggi;

import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.api.ApiAddProduk;
import go.pemkott.appsandroidmobiletebingtinggi.api.ServerConfig;
import go.pemkott.appsandroidmobiletebingtinggi.utils.AttendanceNotificationHelper;
import go.pemkott.appsandroidmobiletebingtinggi.utils.FcmTokenHelper;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Part;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test JUnit 4 untuk memverifikasi host development lokal dan kontrak parameter @Part Retrofit V21.
 */
public class AndroidV21SecurityAndContractsTest {

    @Test
    public void defaultDevHost_pointsToAbsensiServer() {
        assertEquals("https://absensi.tebingtinggikota.go.id/", ServerConfig.DEFAULT_HOST);
        assertEquals("https://absensi.tebingtinggikota.go.id/", ServerConfig.getBaseUrl());
        assertEquals("https://absensi.tebingtinggikota.go.id/api/v22/", ServerConfig.getApiUrl());
        assertEquals("https://absensi.tebingtinggikota.go.id/api/v22/", ServerConfig.getApiV2Url());
    }

    @Test
    public void retrofitPulangMethods_declarePulangValidationPartNames() {
        List<String> shiftPulangPagiParts = extractPartNames("absensiftPulangPagi");
        assertTrue(shiftPulangPagiParts.contains("valid_pulang"));
        assertFalse(shiftPulangPagiParts.contains("valid_masuk"));

        List<String> sakitShiftPulangParts = extractPartNames("uploadizinsakitsiftpulang");
        assertTrue(sakitShiftPulangParts.contains("jam_pulang"));
        assertTrue(sakitShiftPulangParts.contains("status_pulang"));
        assertTrue(sakitShiftPulangParts.contains("valid_pulang"));
        assertFalse(sakitShiftPulangParts.contains("jam_masuk"));
        assertFalse(sakitShiftPulangParts.contains("valid_masuk"));
    }

    @Test
    public void retrofitMasukMethods_keepMasukPartNames() {
        String[] masukMethods = {
                "uploadAbsenKehadiranMasuk",
                "absensiftMasukPagi",
                "absensiftMasukMalam",
                "uploadTLMasuk",
                "uploadIzinKpMasuk",
                "uploadizinsakitmasuk"
        };

        for (String methodName : masukMethods) {
            List<String> parts = extractPartNames(methodName);
            assertTrue(methodName + " must send valid_masuk", parts.contains("valid_masuk"));
            assertTrue(methodName + " must send jam_masuk", parts.contains("jam_masuk"));
            assertFalse(methodName + " must not send valid_pulang", parts.contains("valid_pulang"));
        }
    }

    private List<String> extractPartNames(String methodName) {
        Method target = null;
        for (Method method : ApiAddProduk.class.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                target = method;
                break;
            }
        }
        if (target == null) {
            throw new AssertionError("Method not found in ApiAddProduk: " + methodName);
        }

        List<String> names = new ArrayList<>();
        for (Annotation[] paramAnnotations : target.getParameterAnnotations()) {
            for (Annotation annotation : paramAnnotations) {
                if (annotation instanceof Part) {
                    String value = ((Part) annotation).value();
                    if (value != null && !value.isEmpty()) {
                        names.add(value);
                    }
                }
            }
        }
        return names;
    }

    @Test
    public void getDeviceId_returnsFallbackWithoutThrowingWhenContextUnavailable() {
        String deviceId = FcmTokenHelper.getDeviceId(null);
        assertTrue(deviceId != null && deviceId.startsWith("DEV_"));
    }

    @Test
    public void extractHttpErrorReason_parsesJsonRemarksAndFallbackCodes() {
        Response<String> jsonErr = Response.error(
                422,
                ResponseBody.create("{\"status\":false,\"remarks\":\"Jadwal tidak cocok\"}", MediaType.parse("application/json"))
        );
        assertEquals("Jadwal tidak cocok", AttendanceNotificationHelper.extractHttpErrorReason(jsonErr));
        assertEquals(
                "Gagal mengirim absensi (HTTP 401). Silakan coba kembali.",
                AttendanceNotificationHelper.extractHttpErrorReason(401, "")
        );
        assertEquals(
                "Gagal mengirim absensi (HTTP 500). Silakan coba kembali.",
                AttendanceNotificationHelper.extractHttpErrorReason(500, "<html>Server Error</html>")
        );
    }

    @Test
    public void formatNetworkFailureReason_handlesTimeoutAndEmptyThrowables() {
        assertEquals(
                "Koneksi timeout ke server absensi. Silakan coba kembali.",
                AttendanceNotificationHelper.formatNetworkFailureReason(new java.net.SocketTimeoutException("timed out"))
        );
        assertEquals(
                "Koneksi ke server terputus saat mengirim absensi.",
                AttendanceNotificationHelper.formatNetworkFailureReason(new RuntimeException(""))
        );
    }

    @Test
    public void isWithinSuppressionWindow_matchesSameOutcomeWithinWindowOnly() {
        long now = 100_000L;
        assertTrue(AttendanceNotificationHelper.isWithinSuppressionWindow(
                now - 5_000L, now, AttendanceNotificationHelper.OUTCOME_SUCCESS, "success"
        ));
        assertFalse(AttendanceNotificationHelper.isWithinSuppressionWindow(
                now - 5_000L, now, AttendanceNotificationHelper.OUTCOME_SUCCESS, AttendanceNotificationHelper.OUTCOME_FAILED
        ));
        assertFalse(AttendanceNotificationHelper.isWithinSuppressionWindow(
                now - 20_000L, now, AttendanceNotificationHelper.OUTCOME_SUCCESS, AttendanceNotificationHelper.OUTCOME_SUCCESS
        ));
    }

    @Test
    public void attendanceNotifyMethods_handleNullContextSafely() {
        AttendanceNotificationHelper.notifySuccess(null, "Kehadiran Masuk", "Berhasil");
        AttendanceNotificationHelper.notifyFailure(null, "Kehadiran Masuk", "Gagal");
        AttendanceNotificationHelper.notifyHttpFailure(null, "Kehadiran Masuk", null);
        AttendanceNotificationHelper.notifyNetworkFailure(null, "Kehadiran Masuk", null);
    }
}
