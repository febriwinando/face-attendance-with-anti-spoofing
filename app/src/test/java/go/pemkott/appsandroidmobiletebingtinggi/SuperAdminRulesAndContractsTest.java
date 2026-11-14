package go.pemkott.appsandroidmobiletebingtinggi;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.admin.AdminFormHelper;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test JUnit 4 untuk helper validasi, pemetaan jenis absensi, koordinat, dan ekstraksi error Super Admin.
 */
public class SuperAdminRulesAndContractsTest {

    @Test
    public void buildUpdateMasukMapping_coversAllSixAttendanceTypesConsistently() {
        String[][] expected = {
                {"masuk", "hadir", "2"},
                {"tl-masuk", "dinas", "2"},
                {"pd", "dinas", "2"},
                {"sk", "sakit", "2"},
                {"kp", "pribadi", "2"},
                {"ct", "ijin", "2"}
        };

        for (int i = 0; i < expected.length; i++) {
            AdminFormHelper.AttendanceMapping mapping = AdminFormHelper.buildUpdateMasukMapping(i);
            assertEquals(expected[i][0], mapping.posisi);
            assertEquals(expected[i][1], mapping.status);
            assertEquals(expected[i][2], mapping.validasi);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildUpdateMasukMapping_rejectsOutOfBoundsIndex() {
        AdminFormHelper.buildUpdateMasukMapping(6);
    }

    @Test
    public void isValidDate_validatesStrictIsoDates() {
        assertTrue(AdminFormHelper.isValidDate("2026-11-12"));
        assertTrue(AdminFormHelper.isValidDate(" 2024-02-29 "));

        assertFalse(AdminFormHelper.isValidDate("2026-02-30"));
        assertFalse(AdminFormHelper.isValidDate("12-11-2026"));
        assertFalse(AdminFormHelper.isValidDate("2026-13-01"));
        assertFalse(AdminFormHelper.isValidDate(""));
        assertFalse(AdminFormHelper.isValidDate(null));
    }

    @Test
    public void isValidTime_validates24HourFormatAndOptionalAlphaPlaceholder() {
        assertTrue(AdminFormHelper.isValidTime("07:30", false));
        assertTrue(AdminFormHelper.isValidTime("16:45:09", false));
        assertTrue(AdminFormHelper.isValidTime("00:00", false));
        assertTrue(AdminFormHelper.isValidTime("23:59:59", false));

        assertFalse(AdminFormHelper.isValidTime("al", false));
        assertTrue(AdminFormHelper.isValidTime("al", true));
        assertTrue(AdminFormHelper.isValidTime("AL", true));

        assertFalse(AdminFormHelper.isValidTime("24:00", false));
        assertFalse(AdminFormHelper.isValidTime("07:60", false));
        assertFalse(AdminFormHelper.isValidTime("7:30", false));
        assertFalse(AdminFormHelper.isValidTime("", false));
        assertFalse(AdminFormHelper.isValidTime(null, true));
    }

    @Test
    public void isValidCoordinates_acceptsValidRangesIncludingZeroAndRejectsOutOfBounds() {
        assertTrue(AdminFormHelper.isValidCoordinates("3.3280661", "99.1129267"));
        assertTrue(AdminFormHelper.isValidCoordinates("0", "0"));
        assertTrue(AdminFormHelper.isValidCoordinates("-90", "180"));
        assertTrue(AdminFormHelper.isValidCoordinates("90", "-180"));

        assertFalse(AdminFormHelper.isValidCoordinates("90.0001", "99.11"));
        assertFalse(AdminFormHelper.isValidCoordinates("-90.1", "99.11"));
        assertFalse(AdminFormHelper.isValidCoordinates("3.32", "180.0001"));
        assertFalse(AdminFormHelper.isValidCoordinates("3.32", "-180.1"));
        assertFalse(AdminFormHelper.isValidCoordinates("NaN", "99.11"));
        assertFalse(AdminFormHelper.isValidCoordinates("abc", "99.11"));
        assertFalse(AdminFormHelper.isValidCoordinates("", "99.11"));
        assertFalse(AdminFormHelper.isValidCoordinates(null, "99.11"));
    }

    @Test
    public void parseNonNegativeInt_acceptsZeroAndPositiveIntegersOnly() {
        assertEquals(Integer.valueOf(0), AdminFormHelper.parseNonNegativeInt(""));
        assertEquals(Integer.valueOf(0), AdminFormHelper.parseNonNegativeInt("0"));
        assertEquals(Integer.valueOf(15), AdminFormHelper.parseNonNegativeInt("15"));

        assertNull(AdminFormHelper.parseNonNegativeInt("-1"));
        assertNull(AdminFormHelper.parseNonNegativeInt("12.5"));
        assertNull(AdminFormHelper.parseNonNegativeInt("abc"));
        assertNull(AdminFormHelper.parseNonNegativeInt(null));
    }

    @Test
    public void extractApiError_readsRemarksOrMessageAndFallsBackGracefully() {
        ResponseBody remarksBody = ResponseBody.create(
                MediaType.parse("application/json"),
                "{\"status\":false,\"remarks\":\"Akses Super Admin ditolak\"}"
        );
        Response<Object> forbidden = Response.error(403, remarksBody);
        assertEquals("Akses Super Admin ditolak", AdminFormHelper.extractApiError(forbidden, "HTTP 403"));

        ResponseBody messageBody = ResponseBody.create(
                MediaType.parse("application/json"),
                "{\"message\":\"Unauthenticated.\"}"
        );
        Response<Object> unauth = Response.error(401, messageBody);
        assertEquals("Unauthenticated.", AdminFormHelper.extractApiError(unauth, "HTTP 401"));

        ResponseBody invalidJson = ResponseBody.create(
                MediaType.parse("text/plain"),
                "Bad Gateway"
        );
        Response<Object> err502 = Response.error(502, invalidJson);
        assertEquals("HTTP 502", AdminFormHelper.extractApiError(err502, null));

        List<String> expectedPosisi = Arrays.asList("masuk", "tl-masuk", "pd", "sk", "kp", "ct");
        assertEquals(expectedPosisi, Arrays.asList(AdminFormHelper.POSISI_VALUES));
        assertNotNull(AdminFormHelper.STATUS_VALUES);
    }
}

