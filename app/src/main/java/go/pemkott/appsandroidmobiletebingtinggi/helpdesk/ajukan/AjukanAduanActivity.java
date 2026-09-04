package go.pemkott.appsandroidmobiletebingtinggi.helpdesk.ajukan;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager;
import go.pemkott.appsandroidmobiletebingtinggi.model.FileModel;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AjukanAduanActivity extends AppCompatActivity {

    private TextInputEditText etLokasi, etJudul, etDeskripsi;
    private AutoCompleteTextView actvKategori, actvPrioritas;
    private DatabaseHelper databaseHelper;
    private SessionManager session;
    private String userId, sEmployee_id;
    
    // Variables for storage
    private String varNama, varNip, varOpdid;
    
    private HttpService httpService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ajukan_aduan);

        View mainView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://absensi.tebingtinggikota.go.id/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        httpService = retrofit.create(HttpService.class);

        initViews();
        loadUserData();
        setupDropdowns();

        findViewById(R.id.rlBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnKirimAduan).setOnClickListener(v -> kirimAduan());
    }

    private void initViews() {
        etLokasi = findViewById(R.id.etLokasi);
        etJudul = findViewById(R.id.etJudul);
        etDeskripsi = findViewById(R.id.etDeskripsi);
        actvKategori = findViewById(R.id.actvKategori);
        actvPrioritas = findViewById(R.id.actvPrioritas);
    }

    private void loadUserData() {
        session = new SessionManager(this);
        userId = session.getPegawaiId();
        databaseHelper = new DatabaseHelper(this);

        Cursor tUser = databaseHelper.getAllData22(userId);
        if (tUser.moveToNext()) {
            sEmployee_id = tUser.getString(1);
        }

        Cursor employee = databaseHelper.getDataEmployee(sEmployee_id);
        if (employee.moveToNext()) {
            varOpdid = employee.getString(4);
            varNip = employee.getString(5);
            varNama = employee.getString(6);
        }
    }

    private void setupDropdowns() {
        String[] kategoriItems = {
                "Kendala Otentikasi & Akun",
                "Masalah Presensi & Fitur Absensi",
                "Pengajuan Izin, Cuti, & Koreksi",
                "Gangguan Sistem & Teknis (Sisi Aplikasi)",
                "Kendala Laporan & Data Rekap",
                "Kendala lainnya"
        };
        ArrayAdapter<String> adapterKategori = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, kategoriItems);
        actvKategori.setAdapter(adapterKategori);

        String[] prioritasItems = {"rendah", "normal", "tinggi"};
        ArrayAdapter<String> adapterPrioritas = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, prioritasItems);
        actvPrioritas.setAdapter(adapterPrioritas);
        actvPrioritas.setText("normal", false);
    }

    private void kirimAduan() {
        String judul = etJudul.getText().toString().trim();
        String deskripsi = etDeskripsi.getText().toString().trim();
        String kategori = actvKategori.getText().toString();
        String prioritas = actvPrioritas.getText().toString();
        String lokasi_aduan = etLokasi.getText().toString().trim();

        if (judul.isEmpty() || deskripsi.isEmpty() || kategori.isEmpty()) {
            Toast.makeText(this, "Mohon lengkapi judul, deskripsi, dan kategori.", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody rbNama = RequestBody.create(MediaType.parse("text/plain"), varNama);
        RequestBody rbNip = RequestBody.create(MediaType.parse("text/plain"), varNip);
        RequestBody rbOpdid = RequestBody.create(MediaType.parse("text/plain"), varOpdid);
        RequestBody rbKategori = RequestBody.create(MediaType.parse("text/plain"), kategori);
        RequestBody rbPrioritas = RequestBody.create(MediaType.parse("text/plain"), prioritas);
        RequestBody rbLokasi = RequestBody.create(MediaType.parse("text/plain"), lokasi_aduan);
        RequestBody rbJudul = RequestBody.create(MediaType.parse("text/plain"), judul);
        RequestBody rbDeskripsi = RequestBody.create(MediaType.parse("text/plain"), deskripsi);

        List<MultipartBody.Part> images = new ArrayList<>();
        // Note: Logic for picking and adding images can be added here.
        // For now, sending empty list as per Laravel form optional image upload.

        Call<FileModel> call = httpService.kirimAduan(
                rbNama, rbNip, rbOpdid, rbKategori, rbPrioritas, rbLokasi, rbJudul, rbDeskripsi, images
        );

        call.enqueue(new Callback<FileModel>() {
            @Override
            public void onResponse(@NonNull Call<FileModel> call, @NonNull Response<FileModel> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AjukanAduanActivity.this, "Aduan Anda telah terkirim. Mohon tunggu respon admin.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(AjukanAduanActivity.this, "Gagal mengirim aduan. Silakan coba lagi.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<FileModel> call, @NonNull Throwable t) {
                Log.e("API_ERROR", "Error: " + t.getMessage());
                Toast.makeText(AjukanAduanActivity.this, "Terjadi kesalahan jaringan.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
