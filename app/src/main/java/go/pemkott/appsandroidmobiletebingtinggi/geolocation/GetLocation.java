package go.pemkott.appsandroidmobiletebingtinggi.geolocation;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class GetLocation implements LocationListener {
    Context context;

    public GetLocation(Context c) {
        context = c;
    }

    public Location getLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Permission not granted", Toast.LENGTH_SHORT).show();
            return null;
        }

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean isFusedLocationEnabled = locationManager.isProviderEnabled(LocationManager.FUSED_PROVIDER);


        if (isGpsEnabled) {

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((AppCompatActivity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
            }

            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 10000, 10,this);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            return location;
        }
        else if (isNetworkEnabled) {

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((AppCompatActivity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
            }

            locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER, 10000, 10,this);
            Location locationNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            return locationNetwork;
        }
        else if(isGpsEnabled && isNetworkEnabled){

            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 10000, 10,this);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER, 10000, 10,this);
            Location locationNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (location.getAccuracy() < locationNetwork.getAccuracy()){
                return locationNetwork;
            }else if (location.getAccuracy() > locationNetwork.getAccuracy()){
                return location;
            }

        }
        else {
            Toast.makeText(context, "Please Enable GPS", Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
