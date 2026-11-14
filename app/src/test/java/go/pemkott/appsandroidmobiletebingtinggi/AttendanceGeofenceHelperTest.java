package go.pemkott.appsandroidmobiletebingtinggi;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import go.pemkott.appsandroidmobiletebingtinggi.utils.AttendanceGeofenceHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AttendanceGeofenceHelperTest {

    // Titik referensi di Kota Tebing Tinggi (~3.328027, 99.166501)
    private static final double OFFICE_LAT = 3.328027;
    private static final double OFFICE_LNG = 99.166501;

    @Test
    public void getRadiusAbsensi_respectsMondayAndCtRule() {
        assertEquals(150.0, AttendanceGeofenceHelper.getRadiusAbsensi("asn", false), 0.001);
        assertEquals(200.0, AttendanceGeofenceHelper.getRadiusAbsensi("asn", true), 0.001);
        assertEquals(150.0, AttendanceGeofenceHelper.getRadiusAbsensi("ct", true), 0.001);
        assertEquals(150.0, AttendanceGeofenceHelper.getRadiusAbsensi("CT", false), 0.001);
    }

    @Test
    public void identicalCoordinates_returnsZeroNotNaN() {
        double distance = AttendanceGeofenceHelper.calculateDistanceMeters(
                OFFICE_LAT, OFFICE_LNG, OFFICE_LAT, OFFICE_LNG
        );
        assertFalse(Double.isNaN(distance));
        assertEquals(0.0, distance, 0.001);
        assertTrue(AttendanceGeofenceHelper.isWithinAttendanceRadius(distance, AttendanceGeofenceHelper.RADIUS_NORMAL));
    }

    @Test
    public void distance125Meters_isWithinNormal150Radius_fixingOld100MeterHardcode() {
        // +0.00112 deg latitude ~ 124.5 meters north
        double userLat = OFFICE_LAT + 0.00112;
        double userLng = OFFICE_LNG;

        AttendanceGeofenceHelper.DistanceResult result = AttendanceGeofenceHelper.resolveAttendanceDistance(
                Collections.emptyList(), Collections.emptyList(),
                Collections.singletonList(String.valueOf(OFFICE_LAT)),
                Collections.singletonList(String.valueOf(OFFICE_LNG)),
                null, null,
                userLat, userLng
        );

        assertTrue(result.found);
        assertEquals(AttendanceGeofenceHelper.SOURCE_OPD_POINT, result.source);
        assertTrue(result.distanceMeters > 100.0 && result.distanceMeters <= 150.0);
        assertTrue(AttendanceGeofenceHelper.isWithinAttendanceRadius(
                result.distanceMeters,
                AttendanceGeofenceHelper.getRadiusAbsensi("asn", false)
        ));
    }

    @Test
    public void distance180Meters_acceptedOnMondayAndRejectedOnNormalDay() {
        // +0.00162 deg latitude ~ 180 meters
        double userLat = OFFICE_LAT + 0.00162;
        double userLng = OFFICE_LNG;

        AttendanceGeofenceHelper.DistanceResult result = AttendanceGeofenceHelper.resolveAttendanceDistance(
                Collections.emptyList(), Collections.emptyList(),
                Collections.singletonList(String.valueOf(OFFICE_LAT)),
                Collections.singletonList(String.valueOf(OFFICE_LNG)),
                null, null,
                userLat, userLng
        );

        assertTrue(result.found);
        assertTrue(result.distanceMeters > 150.0 && result.distanceMeters <= 200.0);
        assertTrue(AttendanceGeofenceHelper.isWithinAttendanceRadius(
                result.distanceMeters,
                AttendanceGeofenceHelper.getRadiusAbsensi("asn", true)
        ));
        assertFalse(AttendanceGeofenceHelper.isWithinAttendanceRadius(
                result.distanceMeters,
                AttendanceGeofenceHelper.getRadiusAbsensi("asn", false)
        ));
    }

    @Test
    public void invalidOrKosongEmployeePoints_fallsBackToOpdInsteadOfReturningDoubleMax() {
        AttendanceGeofenceHelper.DistanceResult result = AttendanceGeofenceHelper.resolveAttendanceDistance(
                Arrays.asList(null, "null", "kosong", "0.0", ""),
                Arrays.asList(null, "null", "kosong", "0.0", ""),
                Collections.singletonList(String.valueOf(OFFICE_LAT)),
                Collections.singletonList(String.valueOf(OFFICE_LNG)),
                null, null,
                OFFICE_LAT, OFFICE_LNG
        );

        assertTrue(result.found);
        assertEquals(AttendanceGeofenceHelper.SOURCE_OPD_POINT, result.source);
        assertEquals(0.0, result.distanceMeters, 0.1);
    }

    @Test
    public void emptyEmployeeAndOpdLists_usesOfficeFallbackCoordinates() {
        AttendanceGeofenceHelper.DistanceResult result = AttendanceGeofenceHelper.resolveAttendanceDistance(
                Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(),
                String.valueOf(OFFICE_LAT), String.valueOf(OFFICE_LNG),
                OFFICE_LAT + 0.0002, OFFICE_LNG
        );

        assertTrue(result.found);
        assertEquals(AttendanceGeofenceHelper.SOURCE_OFFICE_FALLBACK, result.source);
        assertTrue(result.distanceMeters > 0.0 && result.distanceMeters < 50.0);
    }

    @Test
    public void employeePointTakesPriorityOverOpdPoint() {
        AttendanceGeofenceHelper.DistanceResult result = AttendanceGeofenceHelper.resolveAttendanceDistance(
                Collections.singletonList("3.330000"),
                Collections.singletonList("99.170000"),
                Collections.singletonList(String.valueOf(OFFICE_LAT)),
                Collections.singletonList(String.valueOf(OFFICE_LNG)),
                String.valueOf(OFFICE_LAT), String.valueOf(OFFICE_LNG),
                3.330000, 99.170000
        );

        assertTrue(result.found);
        assertEquals(AttendanceGeofenceHelper.SOURCE_EMPLOYEE_POINT, result.source);
        assertEquals(0.0, result.distanceMeters, 0.1);
    }

    @Test
    public void zeroUserCoordinatesAndMissingTargets_rejectedSafely() {
        AttendanceGeofenceHelper.DistanceResult zeroUser = AttendanceGeofenceHelper.resolveAttendanceDistance(
                Collections.emptyList(), Collections.emptyList(),
                Collections.singletonList(String.valueOf(OFFICE_LAT)),
                Collections.singletonList(String.valueOf(OFFICE_LNG)),
                null, null,
                0.0, 0.0
        );
        assertFalse(zeroUser.found);
        assertEquals(AttendanceGeofenceHelper.JARAK_TIDAK_VALID, zeroUser.distanceMeters, 0.0);
        assertFalse(AttendanceGeofenceHelper.isWithinAttendanceRadius(zeroUser.distanceMeters, 150.0));

        String noTargetMsg = AttendanceGeofenceHelper.buildOutOfRadiusMessage(zeroUser, 150.0);
        assertTrue(noTargetMsg.contains("sinkronisasi lokasi"));
    }

    @Test
    public void optionA_picksNearestAcrossAllEmployeeAndOpdPoints() {
        // Simulasi daftar koordinat nyata NIP 199402092020121005:
        // e_loc_points: id=9877 (3.3285, 99.1625), id=9878 (3.3279492, 99.1664886)
        // loc_points (OPD 1): id=1 Diskominfo Imam Bonjol (3.3350454, 99.1711986),
        //                     id=4 Walikota (3.3279492, 99.1664886),
        //                     id=69 Lokasi Test (3.343457549293085, 99.16175535007382)
        java.util.List<String> empLats = Arrays.asList("3.3285", "3.3279492");
        java.util.List<String> empLngs = Arrays.asList("99.1625", "99.1664886");
        java.util.List<String> opdLats = Arrays.asList("3.3350454", "3.3279492", "3.343457549293085");
        java.util.List<String> opdLngs = Arrays.asList("99.1711986", "99.1664886", "99.16175535007382");

        // 1. User berada di Kantor Diskominfo Jl. Imam Bonjol -> harus memilih loc_points id=1 (~0 m), bukan e_loc_points (~900 m)
        AttendanceGeofenceHelper.DistanceResult nearDiskominfoOpd = AttendanceGeofenceHelper.resolveAttendanceDistance(
                empLats, empLngs, opdLats, opdLngs, null, null,
                3.3350454, 99.1711986
        );
        assertTrue(nearDiskominfoOpd.found);
        assertEquals(AttendanceGeofenceHelper.SOURCE_OPD_POINT, nearDiskominfoOpd.source);
        assertEquals(0.0, nearDiskominfoOpd.distanceMeters, 0.5);
        assertEquals(3.3350454, nearDiskominfoOpd.targetLat, 1e-6);

        // 2. User berada di titik e_loc_points id=9877 -> harus memilih titik tersebut (~0 m)
        AttendanceGeofenceHelper.DistanceResult nearEmp9877 = AttendanceGeofenceHelper.resolveAttendanceDistance(
                empLats, empLngs, opdLats, opdLngs, null, null,
                3.3285, 99.1625
        );
        assertTrue(nearEmp9877.found);
        assertEquals(AttendanceGeofenceHelper.SOURCE_EMPLOYEE_POINT, nearEmp9877.source);
        assertEquals(0.0, nearEmp9877.distanceMeters, 0.5);
        assertEquals(3.3285, nearEmp9877.targetLat, 1e-6);

        // 3. User di titik kembar (3.3279492, 99.1664886) -> jarak sama 0 m, tie-break memilih e_loc_points
        AttendanceGeofenceHelper.DistanceResult tieAtWalikota = AttendanceGeofenceHelper.resolveAttendanceDistance(
                empLats, empLngs, opdLats, opdLngs, null, null,
                3.3279492, 99.1664886
        );
        assertTrue(tieAtWalikota.found);
        assertEquals(AttendanceGeofenceHelper.SOURCE_EMPLOYEE_POINT, tieAtWalikota.source);
        assertEquals(0.0, tieAtWalikota.distanceMeters, 0.5);
    }
}

