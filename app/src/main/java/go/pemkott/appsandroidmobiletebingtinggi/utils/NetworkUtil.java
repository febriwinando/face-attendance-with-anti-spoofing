package go.pemkott.appsandroidmobiletebingtinggi.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Enumeration;

public class NetworkUtil {

    /**
     * IP Lokal perangkat
     * Wifi -> 192.168.x.x
     * Hotspot -> 172.x.x.x
     * Data Seluler -> 10.x.x.x
     */
    public static String getDeviceIp(Context context) {

        try {

            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (cm == null)
                return "-";

            Network[] networks = cm.getAllNetworks();

            for (Network network : networks) {

                NetworkCapabilities capabilities =
                        cm.getNetworkCapabilities(network);

                if (capabilities == null)
                    continue;

                if (!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
                    continue;

                LinkProperties linkProperties =
                        cm.getLinkProperties(network);

                if (linkProperties == null)
                    continue;

                for (LinkAddress address : linkProperties.getLinkAddresses()) {

                    InetAddress inetAddress = address.getAddress();

                    if (inetAddress instanceof Inet4Address
                            && !inetAddress.isLoopbackAddress()) {

                        return inetAddress.getHostAddress();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "-";
    }


    /**
     * Cara alternatif apabila metode di atas gagal
     */
    public static String getDeviceIpFallback() {

        try {

            Enumeration<java.net.NetworkInterface> interfaces =
                    java.net.NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {

                java.net.NetworkInterface intf =
                        interfaces.nextElement();

                Enumeration<InetAddress> addrs =
                        intf.getInetAddresses();

                while (addrs.hasMoreElements()) {

                    InetAddress addr =
                            addrs.nextElement();

                    if (!addr.isLoopbackAddress()
                            && addr instanceof Inet4Address) {

                        return addr.getHostAddress();
                    }
                }
            }

        } catch (Exception ignored) {
        }

        return "-";
    }
}