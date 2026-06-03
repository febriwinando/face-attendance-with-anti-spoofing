package go.pemkott.appsandroidmobiletebingtinggi.api;

import java.util.concurrent.TimeUnit;

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

        OkHttpClient okHttpClient =
                new OkHttpClient.Builder()
                        .addInterceptor(logging)
                        .retryOnConnectionFailure(true)
                        .connectTimeout(120, TimeUnit.SECONDS)
                        .readTimeout(120, TimeUnit.SECONDS)
                        .writeTimeout(120, TimeUnit.SECONDS)
                        .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
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

//package go.pemkott.appsandroidmobiletebingtinggi.api;
//
//import java.security.SecureRandom;
//import java.security.cert.X509Certificate;
//import java.util.concurrent.TimeUnit;
//
//import javax.net.ssl.HostnameVerifier;
//import javax.net.ssl.SSLContext;
//import javax.net.ssl.SSLSession;
//import javax.net.ssl.SSLSocketFactory;
//import javax.net.ssl.TrustManager;
//import javax.net.ssl.X509TrustManager;
//
//import okhttp3.CertificatePinner;
//import okhttp3.OkHttpClient;
//import okhttp3.logging.HttpLoggingInterceptor;
//import retrofit2.Retrofit;
//import retrofit2.converter.gson.GsonConverterFactory;
//
//public class RetroClient {
//
//    private static final String BASE_URL =
//            "https://absensi.tebingtinggikota.go.id/api/";
//
//    private static RetroClient myClient;
//
//    private final Retrofit retrofit;
//
//    private RetroClient() {
//
//        HttpLoggingInterceptor logging =
//                new HttpLoggingInterceptor();
//
//        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
//        CertificatePinner certificatePinner =
//                new CertificatePinner.Builder()
//                        .add(
//                                "absensi.tebingtinggikota.go.id",
//                                "sha256/mXguNvbA3yBfSKkrZSHuzRPq91UaHhSCJVjMECPJGAg="
//                        )
//                        .build();
//
//        OkHttpClient okHttpClient =
//                new OkHttpClient.Builder()
//                        .certificatePinner(certificatePinner)
//                        .addInterceptor(logging)
//                        .retryOnConnectionFailure(true)
//                        .connectTimeout(120, TimeUnit.SECONDS)
//                        .readTimeout(120, TimeUnit.SECONDS)
//                        .writeTimeout(120, TimeUnit.SECONDS)
//                        .build();
//
////        OkHttpClient okHttpClient =
////                getUnsafeOkHttpClient()
////                        .newBuilder()
////                        .addInterceptor(logging)
////                        .retryOnConnectionFailure(true)
////                        .connectTimeout(120, TimeUnit.SECONDS)
////                        .readTimeout(120, TimeUnit.SECONDS)
////                        .writeTimeout(120, TimeUnit.SECONDS)
////                        .build();
//
//        retrofit = new Retrofit.Builder()
//                .baseUrl(BASE_URL)
//                .client(okHttpClient)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//    }
//
//    public static synchronized RetroClient getInstance() {
//        if (myClient == null) {
//            myClient = new RetroClient();
//        }
//        return myClient;
//    }
//
//    public ApiAddProduk getApi() {
//        return retrofit.create(ApiAddProduk.class);
//    }
//
//    public HttpService getApi2() {
//        return retrofit.create(HttpService.class);
//    }
//
//    /**
//     * HANYA UNTUK TESTING
//     * Jangan dipakai di production
//     */
//    private OkHttpClient getUnsafeOkHttpClient() {
//
//        try {
//            final TrustManager[] trustAllCerts =
//                    new TrustManager[]{
//                            new X509TrustManager() {
//
//                                @Override
//                                public void checkClientTrusted(
//                                        X509Certificate[] chain,
//                                        String authType) {
//                                }
//
//                                @Override
//                                public void checkServerTrusted(
//                                        X509Certificate[] chain,
//                                        String authType) {
//                                }
//
//                                @Override
//                                public X509Certificate[] getAcceptedIssuers() {
//                                    return new X509Certificate[]{};
//                                }
//                            }
//                    };
//
//            SSLContext sslContext =
//                    SSLContext.getInstance("TLS");
//
//            sslContext.init(
//                    null,
//                    trustAllCerts,
//                    new SecureRandom()
//            );
//
//            SSLSocketFactory sslSocketFactory =
//                    sslContext.getSocketFactory();
//
//            OkHttpClient.Builder builder =
//                    new OkHttpClient.Builder();
//
//            builder.sslSocketFactory(
//                    sslSocketFactory,
//                    (X509TrustManager) trustAllCerts[0]
//            );
//
//            builder.hostnameVerifier(
//                    new HostnameVerifier() {
//                        @Override
//                        public boolean verify(
//                                String hostname,
//                                SSLSession session
//                        ) {
//                            return true;
//                        }
//                    });
//
//            return builder.build();
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//}