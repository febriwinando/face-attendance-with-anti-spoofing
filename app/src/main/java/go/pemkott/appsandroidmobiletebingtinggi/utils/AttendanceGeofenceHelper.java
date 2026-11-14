package go.pemkott.appsandroidmobiletebingtinggi.utils;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Helper terpusat untuk resolusi titik koordinat presensi terdekat,
 * perhitungan jarak Haversine yang aman terhadap presisi floating-point,
 * dan validasi radius presensi.
 *
 * Pemilihan titik (Opsi A):
 * - Bandingkan seluruh titik lokasi khusus pegawai (e_loc_points) dan lokasi OPD (loc_points),
 *   lalu pilih jarak paling dekat terhadap posisi pegawai saat melakukan absen.
 * - Fallback koordinat kantor (latOffice / lngOffice) dipakai bila kedua daftar kosong.
 */
public final class AttendanceGeofenceHelper {

    public static final double JARAK_TIDAK_VALID = -1.0;
    public static final double RADIUS_NORMAL = 150.0;
    public static final double RADIUS_SENIN = 200.0;

    public static final String SOURCE_NONE = "none";
    public static final String SOURCE_EMPLOYEE_POINT = "e_loc_points";
    public static final String SOURCE_OPD_POINT = "loc_points";
    public static final String SOURCE_OFFICE_FALLBACK = "office_fallback";

    private static final double EARTH_RADIUS_METERS = 6371000.0;

    private AttendanceGeofenceHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final class DistanceResult {
        public final double distanceMeters;
        public final double targetLat;
        public final double targetLng;
        public final boolean found;
        public final String source;

        public DistanceResult(double distanceMeters, double targetLat, double targetLng, boolean found, String source) {
            this.distanceMeters = distanceMeters;
            this.targetLat = targetLat;
            this.targetLng = targetLng;
            this.found = found;
            this.source = source != null ? source : SOURCE_NONE;
        }

        public static DistanceResult notFound() {
            return new DistanceResult(JARAK_TIDAK_VALID, 0.0, 0.0, false, SOURCE_NONE);
        }
    }

    public static boolean isMondayNow() {
        return Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY;
    }

    public static double getRadiusAbsensi(String kelompok) {
        return getRadiusAbsensi(kelompok, isMondayNow());
    }

    public static double getRadiusAbsensi(String kelompok, boolean isMonday) {
        if ("ct".equalsIgnoreCase(kelompok != null ? kelompok.trim() : "")) {
            return RADIUS_NORMAL;
        }
        return isMonday ? RADIUS_SENIN : RADIUS_NORMAL;
    }

    public static boolean isValidCoordinate(double lat, double lng) {
        if (Double.isNaN(lat) || Double.isNaN(lng) || Double.isInfinite(lat) || Double.isInfinite(lng)) {
            return false;
        }
        if (lat < -90.0 || lat > 90.0 || lng < -180.0 || lng > 180.0) {
            return false;
        }
        // Tolak koordinat default/belum terisi (0.0, 0.0)
        return !(Math.abs(lat) < 1e-6 && Math.abs(lng) < 1e-6);
    }

    public static Double parseDoubleStrict(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed) || "kosong".equalsIgnoreCase(trimmed)) {
            return null;
        }
        try {
            double val = Double.parseDouble(trimmed);
            if (Double.isNaN(val) || Double.isInfinite(val)) {
                return null;
            }
            return val;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static boolean isValidCoordinateStrings(String latStr, String lngStr) {
        Double lat = parseDoubleStrict(latStr);
        Double lng = parseDoubleStrict(lngStr);
        return lat != null && lng != null && isValidCoordinate(lat, lng);
    }

    /**
     * Menghitung jarak dalam meter menggunakan Haversine formula dengan clamping
     * agar tidak menghasilkan NaN ketika user berada sangat dekat / tepat di titik pin.
     */
    public static double calculateDistanceMeters(double latTarget, double lngTarget, double latUser, double lngUser) {
        if (!isValidCoordinate(latTarget, lngTarget) || !isValidCoordinate(latUser, lngUser)) {
            return JARAK_TIDAK_VALID;
        }

        double lat1Rad = Math.toRadians(latTarget);
        double lat2Rad = Math.toRadians(latUser);
        double dLat = Math.toRadians(latUser - latTarget);
        double dLng = Math.toRadians(lngUser - lngTarget);

        double sinDLat = Math.sin(dLat / 2.0);
        double sinDLng = Math.sin(dLng / 2.0);
        double a = (sinDLat * sinDLat)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad) * (sinDLng * sinDLng);
        double clampedA = Math.min(1.0, Math.max(0.0, a));
        double c = 2.0 * Math.atan2(Math.sqrt(clampedA), Math.sqrt(1.0 - clampedA));
        return EARTH_RADIUS_METERS * c;
    }

    public static DistanceResult findNearestDistance(
            List<String> latList,
            List<String> lngList,
            double latUser,
            double lngUser,
            String source
    ) {
        if (!isValidCoordinate(latUser, lngUser) || latList == null || lngList == null || latList.isEmpty() || lngList.isEmpty()) {
            return DistanceResult.notFound();
        }

        int count = Math.min(latList.size(), lngList.size());
        DistanceResult best = null;

        for (int i = 0; i < count; i++) {
            Double lat = parseDoubleStrict(latList.get(i));
            Double lng = parseDoubleStrict(lngList.get(i));
            if (lat == null || lng == null || !isValidCoordinate(lat, lng)) {
                continue;
            }

            double distance = calculateDistanceMeters(lat, lng, latUser, lngUser);
            if (distance == JARAK_TIDAK_VALID || Double.isNaN(distance) || Double.isInfinite(distance)) {
                continue;
            }

            if (best == null || distance < best.distanceMeters) {
                best = new DistanceResult(distance, lat, lng, true, source);
            }
        }

        return best != null ? best : DistanceResult.notFound();
    }

    public static DistanceResult resolveAttendanceDistance(
            List<String> empLatList,
            List<String> empLngList,
            List<String> opdLatList,
            List<String> opdLngList,
            String officeFallbackLat,
            String officeFallbackLng,
            double latUser,
            double lngUser
    ) {
        if (!isValidCoordinate(latUser, lngUser)) {
            return DistanceResult.notFound();
        }

        // Bandingkan seluruh titik khusus pegawai (e_loc_points) dan seluruh titik OPD (loc_points),
        // lalu pilih titik dengan jarak terdekat terhadap posisi user saat ini.
        DistanceResult empResult = findNearestDistance(empLatList, empLngList, latUser, lngUser, SOURCE_EMPLOYEE_POINT);
        DistanceResult opdResult = findNearestDistance(opdLatList, opdLngList, latUser, lngUser, SOURCE_OPD_POINT);

        if (empResult.found && opdResult.found) {
            return (empResult.distanceMeters <= opdResult.distanceMeters) ? empResult : opdResult;
        }
        if (empResult.found) {
            return empResult;
        }
        if (opdResult.found) {
            return opdResult;
        }

        // Fallback koordinat kantor dari data pegawai (opds.let / lng) bila kedua daftar di atas kosong
        Double fallbackLat = parseDoubleStrict(officeFallbackLat);
        Double fallbackLng = parseDoubleStrict(officeFallbackLng);
        if (fallbackLat != null && fallbackLng != null && isValidCoordinate(fallbackLat, fallbackLng)) {
            double fallbackDistance = calculateDistanceMeters(fallbackLat, fallbackLng, latUser, lngUser);
            if (fallbackDistance != JARAK_TIDAK_VALID) {
                return new DistanceResult(fallbackDistance, fallbackLat, fallbackLng, true, SOURCE_OFFICE_FALLBACK);
            }
        }

        return DistanceResult.notFound();
    }

    public static boolean isWithinAttendanceRadius(double distanceMeters, double radiusMeters) {
        return distanceMeters >= 0.0
                && !Double.isNaN(distanceMeters)
                && !Double.isInfinite(distanceMeters)
                && distanceMeters <= radiusMeters;
    }

    public static String buildOutOfRadiusMessage(DistanceResult result, double radiusMeters) {
        if (result == null || !result.found || result.distanceMeters == JARAK_TIDAK_VALID) {
            return "Koordinat lokasi presensi tidak tersedia atau belum tersinkron. Silakan lakukan sinkronisasi lokasi terlebih dahulu.";
        }
        return String.format(
                Locale.US,
                "Jarak terukur %.0f m dari titik lokasi (%s), melebihi batas radius %.0f m.",
                result.distanceMeters,
                describeSource(result.source),
                radiusMeters
        );
    }

    private static String describeSource(String source) {
        if (SOURCE_EMPLOYEE_POINT.equals(source)) {
            return "Lokasi Khusus Pegawai";
        }
        if (SOURCE_OPD_POINT.equals(source)) {
            return "Lokasi OPD";
        }
        if (SOURCE_OFFICE_FALLBACK.equals(source)) {
            return "Lokasi Kantor Default";
        }
        return "Tidak Diketahui";
    }
}

