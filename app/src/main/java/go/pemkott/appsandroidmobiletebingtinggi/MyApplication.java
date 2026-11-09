package go.pemkott.appsandroidmobiletebingtinggi;

import android.app.Application;
import android.content.Context;
import com.google.firebase.FirebaseApp;
import go.pemkott.appsandroidmobiletebingtinggi.api.SSLHelper;
import go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager;

public class MyApplication extends Application {

    private static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        FirebaseApp.initializeApp(this);

        // Always enable SSL certificate checking bypass for HTTPS local server (172.15.1.35:8443)
        SSLHelper.disableSSLCertificateChecking();
    }

    public static Context getAppContext() {
        return instance != null ? instance.getApplicationContext() : null;
    }
}
