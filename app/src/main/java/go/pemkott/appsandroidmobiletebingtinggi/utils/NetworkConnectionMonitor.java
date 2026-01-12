package go.pemkott.appsandroidmobiletebingtinggi.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

public class NetworkConnectionMonitor extends LiveData<Boolean> {
    Context context;
    ConnectionNetworkCallback callback;

    public NetworkConnectionMonitor(Context context) {
        this.context = context;
    }
    public void unregisterDefaultNetworkCallback(){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        connectivityManager.unregisterNetworkCallback(callback);
    }

    public void registerDefaultNetworkCallback() {

        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            postValue(checkConnection(connectivityManager));
            assert connectivityManager != null;
            callback = new ConnectionNetworkCallback();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager.registerDefaultNetworkCallback(callback);
            }
        } catch (Exception e) {

            postValue(false);
        }
    }
    private boolean checkConnection(@NonNull ConnectivityManager connectivityManager){
        Network network = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            network = connectivityManager.getActiveNetwork();
        }
        if (network == null){
            return false;
        }
        else{
            NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(network);
            return actNw != null
                    && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                    || actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
            );
        }
    }

    private class ConnectionNetworkCallback extends ConnectivityManager.NetworkCallback{

        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            postValue(true);
            Log.d("Connection:", "onAvailable");
        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            postValue(false);
            Log.d("Connection:", "onLost");
        }

        @Override
        public void onBlockedStatusChanged(@NonNull Network network, boolean blocked) {
            super.onBlockedStatusChanged(network, blocked);
            Log.d("Connection:", "onBlockedStatusChanged");

        }

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            Log.d("Connection:", "onCapabilitiesChanged");
        }

        @Override
        public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties) {
            super.onLinkPropertiesChanged(network, linkProperties);
            Log.d("Connection:", "onLinkPropertiesChanged");
        }

        @Override
        public void onLosing(@NonNull Network network, int maxMsToLive) {
            super.onLosing(network, maxMsToLive);
            Log.d("Connection:", "onLosing");
        }

        @Override
        public void onUnavailable() {
            super.onUnavailable();
            Log.d("Connection:", "onUnavailable");
        }
    }
}