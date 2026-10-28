package go.pemkott.appsandroidmobiletebingtinggi.utils;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.provider.Settings;

public class FakeGpsDetector {

    /**
     * Memeriksa secara akurat apakah lokasi yang diperoleh berasal dari aplikasi Fake GPS / Mock Location
     */
    public static boolean isFakeGps(Context context, Location location) {
        if (location == null) return false;

        // 1. Android 12+ (Build.VERSION_CODES.S) Official API check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (location.isMock()) {
                return true;
            }
        } else {
            // 2. Android 6.0 - 11 (API 23 - 30)
            if (location.isFromMockProvider()) {
                return true;
            }
        }

        // 3. Check Location Extras Bundle
        if (location.getExtras() != null) {
            if (location.getExtras().getBoolean("mockLocation", false)) {
                return true;
            }
        }

        // 4. Check Secure Settings Allow Mock Location (Legacy devices)
        if (context != null && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                String mockSetting = Settings.Secure.getString(
                        context.getContentResolver(),
                        Settings.Secure.ALLOW_MOCK_LOCATION
                );
                if (!"0".equals(mockSetting) && mockSetting != null) {
                    return true;
                }
            } catch (Exception ignored) {}
        }

        return false;
    }
}
