package go.pemkott.appsandroidmobiletebingtinggi.api;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import go.pemkott.appsandroidmobiletebingtinggi.MyApplication;
import go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitBuilder {

    private static Retrofit retrofit = null;
    private static Boolean lastBypassState = null;

    public static Retrofit getClient(Context context){
        Context appContext = context != null ? context.getApplicationContext() : MyApplication.getAppContext();
        boolean enableBypass = false;
        if (appContext != null) {
            SessionManager session = new SessionManager(appContext);
            enableBypass = session.isSslBypassEnabled();
        }

        if (retrofit == null || lastBypassState == null || lastBypassState != enableBypass) {
            lastBypassState = enableBypass;
            OkHttpClient.Builder builder = enableBypass
                    ? SSLHelper.getUnsafeOkHttpClientBuilder()
                    : new OkHttpClient.Builder();

            final OkHttpClient okHttpClient = builder
                    .readTimeout(60, TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl("https://absensi.tebingtinggikota.go.id/api/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
        }
        return retrofit;
    }

    public static Retrofit getClient(){
        return getClient(null);
    }
}
