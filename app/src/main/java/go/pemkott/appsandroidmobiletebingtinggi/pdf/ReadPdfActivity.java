package go.pemkott.appsandroidmobiletebingtinggi.pdf;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.button.MaterialButton;

import java.io.File;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.utils.PdfViewerFragment;

public class ReadPdfActivity extends AppCompatActivity implements PdfViewerFragment.OnPdfLoadListener {

    private boolean isErrorShown = false;
    private PdfViewerFragment pdfFragment;
    private TextView tvZoomLevel;
    private ImageButton btnDownloadPdf, btnSharePdf;
    private String currentPdfUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Task 1: Buat Halaman ReadPdfActivity Fullscreen Immersive Mode
        enableFullscreenMode();

        setContentView(R.layout.activity_read_pdf);

        // Tombol Kembali ke Halaman Sebelumnya
        ImageButton btnBackPdf = findViewById(R.id.btnBackPdf);
        if (btnBackPdf != null) {
            btnBackPdf.setOnClickListener(v -> finish());
        }

        // Tombol Unduh dan Bagikan
        btnDownloadPdf = findViewById(R.id.btnDownloadPdf);
        btnSharePdf = findViewById(R.id.btnSharePdf);

        if (btnDownloadPdf != null) {
            btnDownloadPdf.setOnClickListener(v -> downloadPdfDocument());
        }

        if (btnSharePdf != null) {
            btnSharePdf.setOnClickListener(v -> sharePdfDocument());
        }

        // Kontrol Zoom In, Zoom Out, dan Reset
        tvZoomLevel = findViewById(R.id.tvZoomLevel);
        MaterialButton btnZoomIn = findViewById(R.id.btnZoomIn);
        MaterialButton btnZoomOut = findViewById(R.id.btnZoomOut);
        MaterialButton btnZoomReset = findViewById(R.id.btnZoomReset);

        if (btnZoomIn != null) {
            btnZoomIn.setOnClickListener(v -> {
                if (pdfFragment != null) {
                    pdfFragment.zoomIn();
                    updateZoomLabel();
                }
            });
        }

        if (btnZoomOut != null) {
            btnZoomOut.setOnClickListener(v -> {
                if (pdfFragment != null) {
                    pdfFragment.zoomOut();
                    updateZoomLabel();
                }
            });
        }

        if (btnZoomReset != null) {
            btnZoomReset.setOnClickListener(v -> {
                if (pdfFragment != null) {
                    pdfFragment.resetZoom();
                    updateZoomLabel();
                }
            });
        }

        currentPdfUrl = getIntent().getStringExtra("PDF_URL");

        if (currentPdfUrl == null || currentPdfUrl.trim().isEmpty() || "null".equalsIgnoreCase(currentPdfUrl.trim())) {
            showPdfNotFoundAlertAndClose("Dokumen PDF tidak ditemukan atau URL kosong.");
        } else {
            bukaPdf(currentPdfUrl);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void enableFullscreenMode() {
        androidx.activity.EdgeToEdge.enable(this);
        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.hide(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.navigationBars() | WindowInsetsCompat.Type.systemBars());
            controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }

    private void updateZoomLabel() {
        if (tvZoomLevel != null && pdfFragment != null) {
            String zoomText = pdfFragment.getZoomPercent() + "%";
            tvZoomLevel.setText(zoomText);
        }
    }

    private void bukaPdf(String urlPdf) {
        pdfFragment = PdfViewerFragment.newInstance(urlPdf);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, pdfFragment)
                .commit();
    }

    @Override
    public void onError(String message) {
        if (!isErrorShown) {
            isErrorShown = true;
            if (btnDownloadPdf != null) btnDownloadPdf.setVisibility(View.GONE);
            if (btnSharePdf != null) btnSharePdf.setVisibility(View.GONE);
            showPdfNotFoundAlertAndClose(message);
        }
    }

    // Task 2: Jika PDF ditemukan, tampilkan tombol Unduh dan Bagikan
    @Override
    public void onSuccess() {
        if (btnDownloadPdf != null) btnDownloadPdf.setVisibility(View.VISIBLE);
        if (btnSharePdf != null) btnSharePdf.setVisibility(View.VISIBLE);
        updateZoomLabel();
    }

    private void downloadPdfDocument() {
        if (currentPdfUrl == null || currentPdfUrl.trim().isEmpty()) {
            Toast.makeText(this, "URL Dokumen tidak ditemukan.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String fileName = "Dokumen_EAbsensi_" + System.currentTimeMillis() + ".pdf";
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(currentPdfUrl));
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            request.setTitle("Mengunduh Dokumen PDF");
            request.setDescription(fileName);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if (manager != null) {
                manager.enqueue(request);
                Toast.makeText(this, "Pengunduhan dimulai. Cek panel notifikasi HP Anda.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Gagal mengunduh dokumen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sharePdfDocument() {
        try {
            File cachedPdf = new File(getCacheDir(), "file.pdf");
            if (!cachedPdf.exists()) {
                Toast.makeText(this, "File dokumen tidak ditemukan.", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri contentUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    cachedPdf
            );

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Bagikan Dokumen PDF"));
        } catch (Exception e) {
            Toast.makeText(this, "Gagal membagikan dokumen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showPdfNotFoundAlertAndClose(String message) {
        if (isFinishing()) return;

        Dialog dialogError = new Dialog(ReadPdfActivity.this, R.style.DialogStyle);
        dialogError.setContentView(R.layout.view_warning_kosong);
        dialogError.setCancelable(false);

        TextView tvWarning1 = dialogError.findViewById(R.id.tvWarning1);
        ImageView tvTutupDialog = dialogError.findViewById(R.id.tvTutupDialog);

        String fullMessage = (message != null ? message : "Dokumen tidak ditemukan.")
                + "\n\nKembali ke halaman sebelumnya dalam 3 detik...";

        if (tvWarning1 != null) {
            tvWarning1.setText(fullMessage);
        }

        if (tvTutupDialog != null) {
            tvTutupDialog.setOnClickListener(v -> {
                if (dialogError.isShowing()) {
                    dialogError.dismiss();
                }
                finish();
            });
        }

        dialogError.show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing()) {
                if (dialogError.isShowing()) {
                    dialogError.dismiss();
                }
                finish();
            }
        }, 3000);
    }
}
