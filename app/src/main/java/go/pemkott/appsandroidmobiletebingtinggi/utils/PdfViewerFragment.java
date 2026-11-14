package go.pemkott.appsandroidmobiletebingtinggi.utils;

import android.content.Context;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import go.pemkott.appsandroidmobiletebingtinggi.R;

public class PdfViewerFragment extends Fragment {

    private static final String ARG_URL = "pdf_url";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private PdfRenderer pdfRenderer;
    private ParcelFileDescriptor fileDescriptor;
    private PdfPageAdapter adapter;

    public interface OnPdfLoadListener {
        void onError(String message);
        void onSuccess();
    }

    private OnPdfLoadListener listener;

    public static PdfViewerFragment newInstance(String url) {
        Bundle b = new Bundle();
        b.putString(ARG_URL, url);
        PdfViewerFragment f = new PdfViewerFragment();
        f.setArguments(b);
        return f;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnPdfLoadListener) {
            listener = (OnPdfLoadListener) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pdf_viewer, container, false);

        recyclerView = v.findViewById(R.id.rvPdf);
        progressBar = v.findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        String pdfUrl = getArguments() != null ? getArguments().getString(ARG_URL) : null;
        downloadPdf(pdfUrl);

        return v;
    }

    private void downloadPdf(String rawUrl) {
        if (rawUrl == null || rawUrl.trim().isEmpty() || "null".equalsIgnoreCase(rawUrl.trim())) {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            if (listener != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> listener.onError("URL Dokumen PDF tidak valid atau kosong."));
            }
            return;
        }
        String urlStr = go.pemkott.appsandroidmobiletebingtinggi.api.ServerConfig.normalizeServerUrl(rawUrl);

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(12000);
                conn.setReadTimeout(20000);
                conn.connect();

                int responseCode = conn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (progressBar != null) progressBar.setVisibility(View.GONE);
                            if (listener != null) listener.onError("Dokumen PDF tidak ditemukan di server (HTTP " + responseCode + ").");
                        });
                    }
                    return;
                }

                File file = new File(requireContext().getCacheDir(), "file.pdf");
                InputStream in = conn.getInputStream();
                FileOutputStream out = new FileOutputStream(file);

                byte[] buf = new byte[1024];
                int len;
                long totalBytes = 0;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                    totalBytes += len;
                }
                in.close();
                out.close();

                if (totalBytes < 100) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (progressBar != null) progressBar.setVisibility(View.GONE);
                            if (listener != null) listener.onError("Dokumen PDF kosong atau tidak ditemukan.");
                        });
                    }
                    return;
                }

                fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
                pdfRenderer = new PdfRenderer(fileDescriptor);

                if (pdfRenderer.getPageCount() == 0) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (progressBar != null) progressBar.setVisibility(View.GONE);
                            if (listener != null) listener.onError("Dokumen PDF tidak memiliki halaman.");
                        });
                    }
                    return;
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        adapter = new PdfPageAdapter(pdfRenderer);
                        recyclerView.setAdapter(adapter);
                        if (listener != null) listener.onSuccess();
                    });
                }

            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        if (listener != null) listener.onError("Dokumen PDF tidak ditemukan atau tidak dapat dibuka.");
                    });
                }
            }
        }).start();
    }

    public void zoomIn() {
        if (adapter != null) {
            float newScale = adapter.getScaleFactor() + 0.25f;
            adapter.setScaleFactor(newScale);
        }
    }

    public void zoomOut() {
        if (adapter != null) {
            float newScale = adapter.getScaleFactor() - 0.25f;
            adapter.setScaleFactor(newScale);
        }
    }

    public void resetZoom() {
        if (adapter != null) {
            adapter.setScaleFactor(1.0f);
        }
    }

    public int getZoomPercent() {
        if (adapter != null) {
            return Math.round(adapter.getScaleFactor() * 100);
        }
        return 100;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            if (pdfRenderer != null) pdfRenderer.close();
            if (fileDescriptor != null) fileDescriptor.close();
        } catch (IOException ignored) {}
    }
}
