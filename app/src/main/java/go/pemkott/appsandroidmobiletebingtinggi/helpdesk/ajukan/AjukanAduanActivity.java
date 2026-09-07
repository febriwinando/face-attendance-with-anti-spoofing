package go.pemkott.appsandroidmobiletebingtinggi.helpdesk.ajukan;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager;
import go.pemkott.appsandroidmobiletebingtinggi.model.FileModel;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AjukanAduanActivity extends AppCompatActivity {

    private TextInputEditText etLokasi, etJudul, etDeskripsi;
    private AutoCompleteTextView actvKategori, actvPrioritas;
    private DatabaseHelper databaseHelper;
    private SessionManager session;
    private String userId, sEmployee_id;
    
    // Variables for storage
    private String varNama, varNip, varOpdid;

    private HttpService httpService;

    private Map<String, String> kategoriMap;
    private LinearLayout llImageContainer;
    private final List<Uri> selectedMediaUris = new ArrayList<>();
    private ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia;

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

        httpService = RetroClient.getInstance().getApi2();

        initViews();
        loadUserData();
        setupDropdowns();
        setupMediaPicker();

        findViewById(R.id.rlBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnKirimAduan).setOnClickListener(v -> kirimAduan());
        findViewById(R.id.cvAddPhoto).setOnClickListener(v -> {
            if (selectedMediaUris.size() >= 3) {
                Toast.makeText(this, "Maksimal 3 file media.", Toast.LENGTH_SHORT).show();
            } else {
                pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                        .build());
            }
        });
    }

    private void initViews() {
        etLokasi = findViewById(R.id.etLokasi);
        etJudul = findViewById(R.id.etJudul);
        etDeskripsi = findViewById(R.id.etDeskripsi);
        actvKategori = findViewById(R.id.actvKategori);
        actvPrioritas = findViewById(R.id.actvPrioritas);
        llImageContainer = findViewById(R.id.llImageContainer);
    }

    private void setupMediaPicker() {
        pickMultipleMedia = registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(3), uris -> {
            if (!uris.isEmpty()) {
                for (Uri uri : uris) {
                    if (selectedMediaUris.size() < 3 && !selectedMediaUris.contains(uri)) {
                        selectedMediaUris.add(uri);
                    }
                }
                updateMediaContainer();
            }
        });
    }

    private void updateMediaContainer() {
        llImageContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (Uri uri : selectedMediaUris) {
            View itemView = inflater.inflate(R.layout.item_selected_media, llImageContainer, false);
            ImageView ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            View ivRemove = itemView.findViewById(R.id.ivRemove);
            View ivPlayIcon = itemView.findViewById(R.id.ivPlayIcon);

            try {
                String mimeType = getContentResolver().getType(uri);
                if (mimeType != null && mimeType.startsWith("video")) {
                    ivPlayIcon.setVisibility(View.VISIBLE);
                } else {
                    ivPlayIcon.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                ivPlayIcon.setVisibility(View.GONE);
            }

            Glide.with(this).load(uri).centerCrop().into(ivThumbnail);

            ivThumbnail.setOnClickListener(v -> {
                Intent intent = new Intent(this, MediaViewerActivity.class);
                intent.setData(uri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            });

            ivRemove.setOnClickListener(v -> {
                selectedMediaUris.remove(uri);
                updateMediaContainer();
            });

            llImageContainer.addView(itemView);
        }
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
        kategoriMap = new LinkedHashMap<>();
        kategoriMap.put("Kendala Otentikasi & Akun", "otentikasi");
        kategoriMap.put("Masalah Presensi & Fitur Absensi", "presensi");
        kategoriMap.put("Pengajuan Izin, Cuti, & Koreksi", "izin_cuti");
        kategoriMap.put("Gangguan Sistem & Teknis (Sisi Aplikasi)", "sistem");
        kategoriMap.put("Kendala Laporan & Data Rekap", "laporan");
        kategoriMap.put("Kendala lainnya", "lainnya");

        List<String> kategoriLabels = new ArrayList<>(kategoriMap.keySet());
        ArrayAdapter<String> adapterKategori = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, kategoriLabels);
        actvKategori.setAdapter(adapterKategori);

        String[] prioritasItems = {"rendah", "normal", "tinggi"};
        ArrayAdapter<String> adapterPrioritas = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, prioritasItems);
        actvPrioritas.setAdapter(adapterPrioritas);
        actvPrioritas.setText("normal", false);
    }

    private void kirimAduan() {
        String judul = etJudul.getText().toString().trim();
        String deskripsi = etDeskripsi.getText().toString().trim();
        String kategoriLabel = actvKategori.getText().toString();
        String prioritas = actvPrioritas.getText().toString();
        String lokasi_aduan = etLokasi.getText().toString().trim();

        if (judul.isEmpty() || deskripsi.isEmpty() || kategoriLabel.isEmpty()) {
            Toast.makeText(this, "Mohon lengkapi judul, deskripsi, dan kategori.", Toast.LENGTH_SHORT).show();
            return;
        }

        String kategoriKey = kategoriMap.get(kategoriLabel);
        if (kategoriKey == null) kategoriKey = "lainnya";

        RequestBody rbEmployee_id = RequestBody.create(MediaType.parse("text/plain"), sEmployee_id);
        RequestBody rbNama = RequestBody.create(MediaType.parse("text/plain"), varNama);
        RequestBody rbNip = RequestBody.create(MediaType.parse("text/plain"), varNip);
        RequestBody rbOpdid = RequestBody.create(MediaType.parse("text/plain"), varOpdid);
        RequestBody rbKategori = RequestBody.create(MediaType.parse("text/plain"), kategoriKey);
        RequestBody rbPrioritas = RequestBody.create(MediaType.parse("text/plain"), prioritas);
        RequestBody rbLokasi = RequestBody.create(MediaType.parse("text/plain"), lokasi_aduan);
        RequestBody rbJudul = RequestBody.create(MediaType.parse("text/plain"), judul);
        RequestBody rbDeskripsi = RequestBody.create(MediaType.parse("text/plain"), deskripsi);

        List<MultipartBody.Part> mediaParts = new ArrayList<>();
        for (Uri uri : selectedMediaUris) {
            MultipartBody.Part part = prepareFilePart("gambar[]", uri);
            if (part != null) {
                mediaParts.add(part);
            }
        }

        String token = "Bearer " + session.getToken();
        Call<FileModel> call = httpService.kirimAduan(
                token, rbEmployee_id, rbNama, rbNip, rbOpdid, rbKategori, rbPrioritas, rbLokasi, rbJudul, rbDeskripsi, mediaParts
        );

        call.enqueue(new Callback<FileModel>() {
            @Override
            public void onResponse(@NonNull Call<FileModel> call, @NonNull Response<FileModel> response) {
                if (response.isSuccessful()) {

                    Log.d("API_ADUAN", "SUCCESS");
                    Log.d("API_ADUAN", "CODE: " + response.code());

                    if (response.body() != null) {
                        Log.d("API_ADUAN", "BODY: " + new Gson().toJson(response.body()));
                    }

                    Toast.makeText(
                            AjukanAduanActivity.this,
                            "Aduan Anda telah terkirim.",
                            Toast.LENGTH_LONG
                    ).show();

                    finish();

                } else {

                    Log.e("API_ADUAN", "FAILED");
                    Log.e("API_ADUAN", "CODE: " + response.code());

                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();

                            Log.e("API_ADUAN", "ERROR BODY: " + errorBody);

                            Toast.makeText(
                                    AjukanAduanActivity.this,
                                    "Gagal: HTTP " + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }

                    } catch (IOException e) {
                        Log.e("API_ADUAN", "ERROR READING BODY", e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<FileModel> call, @NonNull Throwable t) {
                Log.e("API_ERROR", "Error: " + t.getMessage());
                Toast.makeText(AjukanAduanActivity.this, "Terjadi kesalahan jaringan.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            if (inputStream == null) return null;

            byte[] fileBytes = getBytes(inputStream);
            String fileName = getFileName(fileUri);
            String mimeType = getContentResolver().getType(fileUri);

            RequestBody requestFile = RequestBody.create(
                    MediaType.parse(mimeType != null ? mimeType : "application/octet-stream"),
                    fileBytes
            );

            return MultipartBody.Part.createFormData(partName, fileName, requestFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) result = cursor.getString(index);
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result;
    }
}
