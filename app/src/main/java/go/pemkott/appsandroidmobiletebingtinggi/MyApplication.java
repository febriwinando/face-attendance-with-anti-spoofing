package go.pemkott.appsandroidmobiletebingtinggi;

import android.app.Application;
import android.content.Context;
import com.google.firebase.FirebaseApp;
import go.pemkott.appsandroidmobiletebingtinggi.api.SSLHelper;
import go.pemkott.appsandroidmobiletebingtinggi.api.ServerConfig;
import go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager;

public class MyApplication extends Application {

    private static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        FirebaseApp.initializeApp(this);

        if (BuildConfig.DEBUG) {
            // Enable SSL certificate checking bypass only on debug builds for HTTPS local server
            SSLHelper.disableSSLCertificateChecking();
        }

        // Pastikan default host server mengacu ke ServerConfig.DEFAULT_HOST (https://absensi.tebingtinggikota.go.id/)
        ServerConfig.setCustomBaseHost(ServerConfig.DEFAULT_HOST);
    }

    public static Context getAppContext() {
        return instance != null ? instance.getApplicationContext() : null;
    }
}
