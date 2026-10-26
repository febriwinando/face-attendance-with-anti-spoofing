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

        builder.addInterceptor(chain -> {
            okhttp3.Request original = chain.request();
            okhttp3.Request.Builder requestBuilder = original.newBuilder();

            if (original.header("Accept") == null) {
                requestBuilder.header("Accept", "application/json");
            }

            if (original.header("Authorization") == null) {
                Context appContext = MyApplication.getAppContext();
                if (appContext != null) {
                    SessionManager session = new SessionManager(appContext);
                    String token = session.getToken();
                    if (token != null && !token.trim().isEmpty()) {
                        requestBuilder.header("Authorization", token.startsWith("Bearer ") ? token : "Bearer " + token);
                    }
                }
            }

            okhttp3.Response response = chain.proceed(requestBuilder.build());

            // Auto-fallback V2 -> V1: Jika V2 endpoint mengembalikan 404 (Server belum daftarkan v2),
            // secara transparan alihkan request ke endpoint V1 legacy.
            if (response.code() == 404 && original.url().encodedPath().contains("/api/v2/")) {
                response.close();
                String fallbackUrl = original.url().toString().replace("/api/v2/", "/api/");
                okhttp3.Request fallbackRequest = requestBuilder.url(fallbackUrl).build();
                return chain.proceed(fallbackRequest);
            }

            // Proteksi Sisi Klien: Jika server mengembalikan 401 (Unauthenticated / Token Expired),
            // hapus token di SessionManager agar pengguna harus re-login secara aman.
            if (response.code() == 401) {
                Context appContext = MyApplication.getAppContext();
                if (appContext != null) {
                    SessionManager session = new SessionManager(appContext);
                    session.saveToken(null);
                }
            }

            return response;
        });

        OkHttpClient okHttpClient = builder
                        .addInterceptor(logging)
                        .retryOnConnectionFailure(true)
                        .connectTimeout(120, TimeUnit.SECONDS)
                        .readTimeout(120, TimeUnit.SECONDS)
                        .writeTimeout(120, TimeUnit.SECONDS)
                        .build();

        Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(new AlwaysListTypeAdapterFactory())
                .setLenient()
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(ServerConfig.getApiUrl())
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
