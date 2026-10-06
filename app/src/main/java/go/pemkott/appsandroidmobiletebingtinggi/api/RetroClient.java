package go.pemkott.appsandroidmobiletebingtinggi.api;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import go.pemkott.appsandroidmobiletebingtinggi.MyApplication;
import go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetroClient {

    private static final String BASE_URL =
            "https://absensi.tebingtinggikota.go.id/api/";

    private static RetroClient myClient;
    private final Retrofit retrofit;
    private final boolean isBypassMode;

    private RetroClient(boolean enableBypass) {
        this.isBypassMode = enableBypass;

        HttpLoggingInterceptor logging =
                new HttpLoggingInterceptor();

        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder builder = enableBypass
                ? SSLHelper.getUnsafeOkHttpClientBuilder()
                : new OkHttpClient.Builder();

        OkHttpClient okHttpClient = builder
                        .addInterceptor(logging)
                        .retryOnConnectionFailure(true)
                        .connectTimeout(120, TimeUnit.SECONDS)
                        .readTimeout(120, TimeUnit.SECONDS)
                        .writeTimeout(120, TimeUnit.SECONDS)
                        .build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    public static synchronized RetroClient getInstance(Context context) {
        Context appContext = context != null ? context.getApplicationContext() : MyApplication.getAppContext();
        boolean enableBypass = false;
        if (appContext != null) {
            SessionManager session = new SessionManager(appContext);
            enableBypass = session.isSslBypassEnabled();
        }
        if (myClient == null || myClient.isBypassMode != enableBypass) {
            myClient = new RetroClient(enableBypass);
        }
        return myClient;
    }

    public static synchronized RetroClient getInstance() {
        return getInstance(null);
    }

    public static synchronized void resetInstance() {
        myClient = null;
    }

    public ApiAddProduk getApi() {
        return retrofit.create(ApiAddProduk.class);
    }

    public HttpService getApi2() {
        return retrofit.create(HttpService.class);
    }
}
