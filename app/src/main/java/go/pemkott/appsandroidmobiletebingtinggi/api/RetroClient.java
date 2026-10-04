package go.pemkott.appsandroidmobiletebingtinggi.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetroClient {

    private static final String BASE_URL =
            "https://absensi.tebingtinggikota.go.id/api/";

    private static RetroClient myClient;
    private final Retrofit retrofit;

    private RetroClient() {

        HttpLoggingInterceptor logging =
                new HttpLoggingInterceptor();

        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = SSLHelper.getUnsafeOkHttpClientBuilder()
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

    public static synchronized RetroClient getInstance() {
        if (myClient == null) {
            myClient = new RetroClient();
        }
        return myClient;
    }

    public ApiAddProduk getApi() {
        return retrofit.create(ApiAddProduk.class);
    }

    public HttpService getApi2() {
        return retrofit.create(HttpService.class);
    }
}
