package go.pemkott.appsandroidmobiletebingtinggi.api;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import go.pemkott.appsandroidmobiletebingtinggi.BuildConfig;
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
    private final String baseUrl;

    private RetroClient(boolean enableBypass, String apiBaseUrl) {
        this.isBypassMode = enableBypass;
        this.baseUrl = apiBaseUrl;

        HttpLoggingInterceptor logging =
                new HttpLoggingInterceptor();

        logging.redactHeader("Authorization");
        logging.redactHeader("Cookie");
        logging.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);

        OkHttpClient.Builder builder = (BuildConfig.DEBUG && enableBypass)
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
                        String cleanToken = token.startsWith("Bearer ") ? token.substring(7).trim() : token.trim();
                        requestBuilder.header("Authorization", "Bearer " + cleanToken);
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
                .baseUrl(apiBaseUrl)
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
        String currentApiUrl = ServerConfig.getApiUrl();
        if (myClient == null || myClient.isBypassMode != enableBypass || !currentApiUrl.equals(myClient.baseUrl)) {
            myClient = new RetroClient(enableBypass, currentApiUrl);
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
