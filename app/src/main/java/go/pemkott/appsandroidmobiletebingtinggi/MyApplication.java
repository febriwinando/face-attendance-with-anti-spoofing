package go.pemkott.appsandroidmobiletebingtinggi;

import android.app.Application;
import com.google.firebase.FirebaseApp;
import go.pemkott.appsandroidmobiletebingtinggi.api.SSLHelper;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        SSLHelper.disableSSLCertificateChecking();
    }
}