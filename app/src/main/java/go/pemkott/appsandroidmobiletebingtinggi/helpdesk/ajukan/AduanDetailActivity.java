package go.pemkott.appsandroidmobiletebingtinggi.helpdesk.ajukan;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat;
import go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager;
import go.pemkott.appsandroidmobiletebingtinggi.model.AduanDetailResponse;
import go.pemkott.appsandroidmobiletebingtinggi.model.AduanHelpdesk;
import go.pemkott.appsandroidmobiletebingtinggi.utils.ClsGlobal;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AduanDetailActivity extends AppCompatActivity {

    private int aduanId;
    private String sEmployeeId;
    private HttpService httpService;
    private SessionManager session;

    private TextView tvNomor, tvStatus, tvJudul, tvKategori, tvPrioritas, tvDeskripsi, tvLokasi, tvTanggal;
    private LinearLayout llMediaContainer, llAdminResponse;
    private View cvCatatanOpd, cvCatatanKota, cvAlasanTolak;
    private TextView tvCatatanOpd, tvCatatanKota, tvAlasanTolak;
    private RecyclerView rvRiwayat;
    private RiwayatAdapter riwayatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_aduan_detail);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        aduanId = getIntent().getIntExtra("aduan_id", -1);
        sEmployeeId = getIntent().getStringExtra("employee_id");

        if (aduanId == -1) {
            finish();
            return;
        }

        initViews();
        setupData();
        fetchDetail();
    }

    private void initViews() {
        tvNomor = findViewById(R.id.tvNomorDetail);
        tvStatus = findViewById(R.id.tvStatusDetail);
        tvJudul = findViewById(R.id.tvJudulDetail);
        tvKategori = findViewById(R.id.tvKategoriDetail);
        tvPrioritas = findViewById(R.id.tvPrioritasDetail);
        tvDeskripsi = findViewById(R.id.tvDeskripsiDetail);
        tvLokasi = findViewById(R.id.tvLokasiDetail);
        tvTanggal = findViewById(R.id.tvTanggalDetail);
        llMediaContainer = findViewById(R.id.llMediaContainer);
        llAdminResponse = findViewById(R.id.llAdminResponse);
        
        cvCatatanOpd = findViewById(R.id.cvCatatanOpd);
        cvCatatanKota = findViewById(R.id.cvCatatanKota);
        cvAlasanTolak = findViewById(R.id.cvAlasanTolak);
        tvCatatanOpd = findViewById(R.id.tvCatatanOpd);
        tvCatatanKota = findViewById(R.id.tvCatatanKota);
        tvAlasanTolak = findViewById(R.id.tvAlasanTolak);

        rvRiwayat = findViewById(R.id.rvRiwayat);
        rvRiwayat.setLayoutManager(new LinearLayoutManager(this));
        riwayatAdapter = new RiwayatAdapter();
        rvRiwayat.setAdapter(riwayatAdapter);

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());
    }

    private void setupData() {
        session = new SessionManager(this);
        httpService = RetroClient.getInstance().getApi2();
    }

    private void fetchDetail() {
        if (sEmployeeId == null || sEmployeeId.isEmpty()) return;
        
        String token = "Bearer " + session.getToken();
        int empId;
        try {
            empId = Integer.parseInt(sEmployeeId);
        } catch (NumberFormatException e) {
            return;
        }

        httpService.getAduanDetail(aduanId, token, empId).enqueue(new Callback<AduanDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<AduanDetailResponse> call, @NonNull Response<AduanDetailResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                
                if (response.isSuccessful() && response.body() != null) {
                    displayData(response.body().getData());
                } else {
                    Toast.makeText(AduanDetailActivity.this, "Gagal Memuat Detail Laporan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AduanDetailResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(AduanDetailActivity.this, "Terjadi Kesalahan Pada Koneksi Jaringan", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayData(AduanHelpdesk aduan) {
        tvNomor.setText(aduan.getNomor());
        tvJudul.setText(aduan.getJudul());
        tvTanggal.setText("Waktu Pelaporan: " + TimeFormat.formatTimestampAduan(aduan.getCreatedAt()));
        tvDeskripsi.setText(aduan.getDeskripsi());
        tvKategori.setText(ClsGlobal.capitalizeEveryWord(aduan.getKategori().replace("_", " ")));
        tvPrioritas.setText("Tingkat Prioritas: " + ClsGlobal.capitalizeEveryWord(aduan.getPrioritas()));
        tvLokasi.setText(aduan.getLokasiAduan() != null && !aduan.getLokasiAduan().isEmpty() ? aduan.getLokasiAduan() : "-");

        // Status
        String status = aduan.getStatus();
        tvStatus.setText(status.replace("_", " ").toUpperCase());
        int color;
        switch (status) {
            case "baru": color = ContextCompat.getColor(this, R.color.primary_brand); break;
            case "diproses": color = ContextCompat.getColor(this, R.color.kuning); break;
            case "diteruskan_kota": color = Color.parseColor("#9C27B0"); break;
            case "selesai": color = ContextCompat.getColor(this, R.color.hijau); break;
            case "ditolak": color = ContextCompat.getColor(this, R.color.merah); break;
            default: color = ContextCompat.getColor(this, R.color.text_hint);
        }
        tvStatus.setBackgroundTintList(ColorStateList.valueOf(color));

        // Media
        displayMedia(aduan.getGambar());

        // Admin Responses
        boolean hasResponse = false;
        if (aduan.getCatatanOpd() != null && !aduan.getCatatanOpd().isEmpty()) {
            cvCatatanOpd.setVisibility(View.VISIBLE);
            tvCatatanOpd.setText(aduan.getCatatanOpd());
            hasResponse = true;
        }
        if (aduan.getCatatanKota() != null && !aduan.getCatatanKota().isEmpty()) {
            cvCatatanKota.setVisibility(View.VISIBLE);
            tvCatatanKota.setText(aduan.getCatatanKota());
            hasResponse = true;
        }
        if (aduan.getAlasanTolak() != null && !aduan.getAlasanTolak().isEmpty()) {
            cvAlasanTolak.setVisibility(View.VISIBLE);
            tvAlasanTolak.setText(aduan.getAlasanTolak());
            hasResponse = true;
        }
        llAdminResponse.setVisibility(hasResponse ? View.VISIBLE : View.GONE);

        // Riwayat
        riwayatAdapter.setRiwayats(aduan.getRiwayat());
    }

    private void displayMedia(List<String> gambar) {
        llMediaContainer.removeAllViews();
        if (gambar == null || gambar.isEmpty()) {
            findViewById(R.id.tvMediaTitle).setVisibility(View.GONE);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        String baseUrl = "https://absensi.tebingtinggikota.go.id/storage/";

        for (String path : gambar) {
            View itemView = inflater.inflate(R.layout.item_image_detail, llMediaContainer, false);
            ImageView ivThumbnail = itemView.findViewById(R.id.ivThumbnailDetail);
            String fullUrl = baseUrl + path;

            Glide.with(this).load(fullUrl).centerCrop().into(ivThumbnail);

            ivThumbnail.setOnClickListener(v -> {
                Intent intent = new Intent(this, MediaViewerActivity.class);
                intent.setData(Uri.parse(fullUrl));
                startActivity(intent);
            });

            llMediaContainer.addView(itemView);
        }
    }
}
