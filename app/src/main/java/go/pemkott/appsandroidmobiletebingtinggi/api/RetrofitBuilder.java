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

            builder.addInterceptor(chain -> {
                okhttp3.Request original = chain.request();
                okhttp3.Request.Builder requestBuilder = original.newBuilder();

                if (original.header("Accept") == null) {
                    requestBuilder.header("Accept", "application/json");
                }

                if (original.header("Authorization") == null) {
                    Context ctx = appContext != null ? appContext : MyApplication.getAppContext();
                    if (ctx != null) {
                        SessionManager session = new SessionManager(ctx);
                        String token = session.getToken();
                        if (token != null && !token.trim().isEmpty()) {
                            requestBuilder.header("Authorization", token.startsWith("Bearer ") ? token : "Bearer " + token);
                        }
                    }
                }

                okhttp3.Response response = chain.proceed(requestBuilder.build());

                // Proteksi Sisi Klien: Jika server mengembalikan 401 (Unauthenticated / Token Expired),
                // hapus token di SessionManager agar pengguna harus re-login secara aman.
                if (response.code() == 401) {
                    Context ctx = appContext != null ? appContext : MyApplication.getAppContext();
                    if (ctx != null) {
                        SessionManager session = new SessionManager(ctx);
                        session.saveToken(null);
                    }
                }

                return response;
            });

            final OkHttpClient okHttpClient = builder
                    .readTimeout(60, TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(ServerConfig.getApiUrl())
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
