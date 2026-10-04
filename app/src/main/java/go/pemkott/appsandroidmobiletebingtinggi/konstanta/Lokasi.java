package go.pemkott.appsandroidmobiletebingtinggi.konstanta;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.util.List;
import java.util.Locale;

public class Lokasi {

    public static Double getDistance(Double latitudeTujuan, Double longitudeTujuan, Double latitudeUser, Double longitudeUser) {
        if (latitudeTujuan == null || longitudeTujuan == null || latitudeUser == null || longitudeUser == null) {
            return 0.0;
        }
        Double pi = 3.14159265358979;
        Double lat1 = latitudeTujuan;
        Double lon1 = longitudeTujuan;
        Double lat2 = latitudeUser;
        Double lon2 = longitudeUser;
        Double R = 6371e3;

        Double latRad1 = lat1 * (pi / 180);
        Double latRad2 = lat2 * (pi / 180);
        Double deltaLatRad = (lat2 - lat1) * (pi / 180);
        Double deltaLonRad = (lon2 - lon1) * (pi / 180);

        Double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) + Math.cos(latRad1) * Math.cos(latRad2) * Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        Double s = R * c;
        return s;
    }

    public String getAddress(Context context, double lat, double lng) {
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address obj = addresses.get(0);
                if (obj != null) {
                    String add = obj.getAddressLine(0);
                    if (add != null && !add.trim().isEmpty()) {
                        return add;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.format(Locale.US, "Lokasi: %.5f, %.5f", lat, lng);
    }
}
