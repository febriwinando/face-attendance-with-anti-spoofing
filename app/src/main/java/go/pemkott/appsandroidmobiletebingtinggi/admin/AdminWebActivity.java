package go.pemkott.appsandroidmobiletebingtinggi.admin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.api.ServerConfig;
import go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager;

public class AdminWebActivity extends AppCompatActivity {

    public static final String EXTRA_TARGET_URL = "EXTRA_TARGET_URL";
    public static final String EXTRA_TITLE = "EXTRA_TITLE";
    private static final int FILECHOOSER_RESULTCODE = 1002;

    private WebView webView;
    private ProgressBar progressBar;
    private TextView tvTitle;
    private ValueCallback<Uri[]> uploadMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_web);

        tvTitle = findViewById(R.id.tvAdminWebTitle);
        ImageView ivBack = findViewById(R.id.ivAdminWebBack);
        ImageView ivRefresh = findViewById(R.id.ivAdminWebRefresh);
        webView = findViewById(R.id.wvAdminWeb);
        progressBar = findViewById(R.id.pbAdminWeb);

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        String targetUrlPath = getIntent().getStringExtra(EXTRA_TARGET_URL);

        if (title != null && !title.isEmpty()) {
            tvTitle.setText(title);
        } else {
            tvTitle.setText("Panel Pengelolaan Admin");
        }

        ivBack.setOnClickListener(v -> finish());
        ivRefresh.setOnClickListener(v -> webView.reload());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    finish();
                }
            }
        });

        setupWebView();
        loadTargetAdminPage(targetUrlPath);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.e("ADMIN_WEB", "Error loading page: " + description);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                }
                uploadMessage = filePathCallback;

                Intent intent = fileChooserParams.createIntent();
                try {
                    startActivityForResult(intent, FILECHOOSER_RESULTCODE);
                } catch (Exception e) {
                    uploadMessage = null;
                    Toast.makeText(AdminWebActivity.this, "Tidak dapat membuka pengunggah file", Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;
            }
        });
    }

    private void loadTargetAdminPage(String targetPath) {
        SessionManager session = new SessionManager(this);
        String token = session.getToken();

        if (targetPath == null || targetPath.isEmpty()) {
            targetPath = "/mrlabsensi";
        }

        String autoLoginUrl = ServerConfig.getBaseUrl() + "admin/auto-login?token=" +
                Uri.encode(token) + "&redirect=" + Uri.encode(targetPath);

        Log.d("ADMIN_WEB", "Loading auto-login URL: " + autoLoginUrl);
        webView.loadUrl(autoLoginUrl);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (uploadMessage == null) return;
            uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            uploadMessage = null;
        }
    }
}
