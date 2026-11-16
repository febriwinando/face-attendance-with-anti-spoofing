package go.pemkott.appsandroidmobiletebingtinggi.api;

import android.content.Context;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.module.AppGlideModule;

import java.io.IOException;
import java.io.InputStream;

import go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

@GlideModule
public final class MyAppGlideModule extends AppGlideModule {

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoaderFactory(context.getApplicationContext()));
    }

    private static class OkHttpUrlLoaderFactory implements ModelLoaderFactory<GlideUrl, InputStream> {
        private final Context context;
        private static volatile OkHttpClient safeClient;
        private static volatile OkHttpClient unsafeClient;

        public OkHttpUrlLoaderFactory(Context context) {
            this.context = context;
        }

        private static OkHttpClient getClient(boolean bypass) {
            if (bypass) {
                if (unsafeClient == null) {
                    synchronized (OkHttpUrlLoaderFactory.class) {
                        if (unsafeClient == null) {
                            unsafeClient = SSLHelper.getUnsafeOkHttpClientBuilder().build();
                        }
                    }
                }
                return unsafeClient;
            } else {
                if (safeClient == null) {
                    synchronized (OkHttpUrlLoaderFactory.class) {
                        if (safeClient == null) {
                            safeClient = new OkHttpClient.Builder().build();
                        }
                    }
                }
                return safeClient;
            }
        }

        @NonNull
        @Override
        public ModelLoader<GlideUrl, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new OkHttpUrlLoader(context);
        }

        @Override
        public void teardown() {}
    }

    private static class OkHttpUrlLoader implements ModelLoader<GlideUrl, InputStream> {
        private final Context context;

        public OkHttpUrlLoader(Context context) {
            this.context = context;
        }

        @Override
        public boolean handles(@NonNull GlideUrl glideUrl) {
            return true;
        }

        @Override
        public LoadData<InputStream> buildLoadData(@NonNull GlideUrl glideUrl, int width, int height, @NonNull Options options) {
            boolean isBypass = new SessionManager(context).isSslBypassEnabled();
            OkHttpClient client = OkHttpUrlLoaderFactory.getClient(isBypass);
            return new LoadData<>(glideUrl, new OkHttpStreamFetcher(client, glideUrl));
        }
    }

    private static class OkHttpStreamFetcher implements DataFetcher<InputStream>, okhttp3.Callback {
        private final OkHttpClient client;
        private final GlideUrl url;
        private InputStream stream;
        private ResponseBody responseBody;
        private DataCallback<? super InputStream> callback;
        private volatile Call call;

        public OkHttpStreamFetcher(OkHttpClient client, GlideUrl url) {
            this.client = client;
            this.url = url;
        }

        @Override
        public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
            this.callback = callback;
            Request.Builder requestBuilder = new Request.Builder().url(url.toStringUrl());
            for (java.util.Map.Entry<String, String> header : url.getHeaders().entrySet()) {
                requestBuilder.addHeader(header.getKey(), header.getValue());
            }
            Request request = requestBuilder.build();

            call = client.newCall(request);
            call.enqueue(this);
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            if (callback != null) {
                callback.onLoadFailed(e);
            }
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) {
            responseBody = response.body();
            if (response.isSuccessful() && responseBody != null) {
                long contentLength = responseBody.contentLength();
                stream = com.bumptech.glide.util.ContentLengthInputStream.obtain(responseBody.byteStream(), contentLength);
                if (callback != null) {
                    callback.onDataReady(stream);
                }
            } else {
                if (callback != null) {
                    callback.onLoadFailed(new CustomHttpException(response.message(), response.code()));
                }
            }
        }

        @Override
        public void cleanup() {
            try {
                if (stream != null) stream.close();
                if (responseBody != null) responseBody.close();
            } catch (IOException ignored) {}
        }

        @Override
        public void cancel() {
            Call local = call;
            if (local != null) {
                local.cancel();
            }
        }

        @NonNull
        @Override
        public Class<InputStream> getDataClass() {
            return InputStream.class;
        }

        @NonNull
        @Override
        public DataSource getDataSource() {
            return DataSource.REMOTE;
        }
    }

    public static class CustomHttpException extends IOException {
        private final int statusCode;

        public CustomHttpException(String message, int statusCode) {
            super(message + ", status code: " + statusCode);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}
