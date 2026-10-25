package go.pemkott.appsandroidmobiletebingtinggi.admin;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.api.ResponsePOJO;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.dialogview.DialogView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUpdateMasukActivity extends AppCompatActivity {

    private EditText etTanggal, etJamMasuk, etKeterlambatan, etKetMasuk, etPegawaiId;
    private Spinner spPosisiMasuk;
    private ImageView ivPreviewFoto;
    private TextView tvNameLampiran;

    private Calendar calendar = Calendar.getInstance();
    private DialogView dialogView;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private Uri uriFoto = null;
    private Uri uriLampiran = null;

    private final String[] POSISI_TEXTS = {
            "Kehadiran Normal (masuk)",
            "Tugas Lapangan (tl-masuk)",
            "Perjalanan Dinas (pd)",
            "Sakit (sk)",
            "Keperluan Pribadi (kp)",
            "Cuti (ct)"
    };

    private final String[] POSISI_VALUES = {
            "masuk", "tl-masuk", "pd", "sk", "kp", "ct"
    };

    private final String[] STATUS_VALUES = {
            "hadir", "dinas", "dinas", "sakit", "pribadi", "ijin"
    };

    private ActivityResultLauncher<Intent> photoLauncher;
    private ActivityResultLauncher<Intent> fileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_update_masuk);

        dialogView = new DialogView(this);
        ImageView ivBack = findViewById(R.id.ivAdminBack);
        spPosisiMasuk = findViewById(R.id.spAdminPosisiMasuk);
        etTanggal = findViewById(R.id.etAdminTanggal);
        etJamMasuk = findViewById(R.id.etAdminJamMasuk);
        etKeterlambatan = findViewById(R.id.etAdminKeterlambatan);
        etKetMasuk = findViewById(R.id.etAdminKetMasuk);
        etPegawaiId = findViewById(R.id.etAdminPegawaiId);

        MaterialButton btnPickFoto = findViewById(R.id.btnPickFotoMasuk);
        ivPreviewFoto = findViewById(R.id.ivPreviewFotoMasuk);
        MaterialButton btnPickLampiran = findViewById(R.id.btnPickLampiranMasuk);
        tvNameLampiran = findViewById(R.id.tvNameLampiranMasuk);
        MaterialButton btnSubmit = findViewById(R.id.btnAdminSubmitMasuk);

        ivBack.setOnClickListener(v -> finish());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        // Setup Spinner Posisi
        ArrayAdapter<String> adapterPosisi = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, POSISI_TEXTS);
        adapterPosisi.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPosisiMasuk.setAdapter(adapterPosisi);

        // Set default values
        etTanggal.setText(dateFormat.format(calendar.getTime()));
        etJamMasuk.setText("07:30");
        etKeterlambatan.setText("0");
        etKetMasuk.setText("sesuai waktu");

        etTanggal.setOnClickListener(v -> showDatePicker());
        etJamMasuk.setOnClickListener(v -> showTimePicker());

        // File pickers
        photoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        uriFoto = result.getData().getData();
                        if (uriFoto != null) {
                            ivPreviewFoto.setImageURI(uriFoto);
                            ivPreviewFoto.setVisibility(View.VISIBLE);
                        }
                    }
                }
        );

        fileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        uriLampiran = result.getData().getData();
                        if (uriLampiran != null) {
                            tvNameLampiran.setText(uriLampiran.getLastPathSegment());
                        }
                    }
                }
        );

        btnPickFoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            photoLauncher.launch(intent);
        });

        btnPickLampiran.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            fileLauncher.launch(intent);
        });

        btnSubmit.setOnClickListener(v -> submitUpdateMasuk());
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            etTanggal.setText(dateFormat.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            String timeStr = String.format(Locale.US, "%02d:%02d", hourOfDay, minute1);
            etJamMasuk.setText(timeStr);
        }, hour, minute, true).show();
    }

    private void submitUpdateMasuk() {
        String tanggal = etTanggal.getText().toString().trim();
        String jamMasuk = etJamMasuk.getText().toString().trim();
        String keterlambatanStr = etKeterlambatan.getText().toString().trim();
        String ketMasuk = etKetMasuk.getText().toString().trim();
        String pegawaiInput = etPegawaiId.getText().toString().trim();

        int selectedIdx = spPosisiMasuk.getSelectedItemPosition();
        String posisiMasuk = POSISI_VALUES[selectedIdx];
        String statusMasuk = STATUS_VALUES[selectedIdx];
        String validasiMasuk = "2"; // Terverifikasi

        if (tanggal.isEmpty() || jamMasuk.isEmpty() || pegawaiInput.isEmpty()) {
            dialogView.viewNotifKosong(this, "Peringatan", "Tanggal, Jam Masuk, dan ID/NIP Pegawai wajib diisi.");
            return;
        }

        List<String> pegawaiList = new ArrayList<>();
        for (String p : pegawaiInput.split("[,\\n\\s]+")) {
            String clean = p.trim();
            if (!clean.isEmpty()) {
                pegawaiList.add(clean);
            }
        }

        if (pegawaiList.isEmpty()) {
            dialogView.viewNotifKosong(this, "Peringatan", "Masukkan minimal satu ID/NIP Pegawai.");
            return;
        }

        Dialog dialogProses = new Dialog(this, R.style.DialogStyle);
        dialogProses.setContentView(R.layout.view_proses);
        dialogProses.setCancelable(false);
        dialogProses.show();

        HttpService api = RetroClient.getInstance(this).getApi2();

        if (uriFoto == null && uriLampiran == null) {
            // Mode normal tanpa file
            int keterlambatan = 0;
            try { keterlambatan = Integer.parseInt(keterlambatanStr); } catch (Exception ignored) {}

            api.updateDataMasukAdmin(tanggal, jamMasuk, keterlambatan, ketMasuk, pegawaiList)
                    .enqueue(new Callback<ResponsePOJO>() {
                        @Override
                        public void onResponse(Call<ResponsePOJO> call, Response<ResponsePOJO> response) {
                            dialogProses.dismiss();
                            handleResponse(response);
                        }

                        @Override
                        public void onFailure(Call<ResponsePOJO> call, Throwable t) {
                            dialogProses.dismiss();
                            dialogView.viewNotifKosong(AdminUpdateMasukActivity.this, "Koneksi Gagal", t.getMessage());
                        }
                    });
        } else {
            // Mode Multipart dengan Foto / Lampiran
            RequestBody bodyTanggal = RequestBody.create(MediaType.parse("text/plain"), tanggal);
            RequestBody bodyMasuk = RequestBody.create(MediaType.parse("text/plain"), jamMasuk);
            RequestBody bodyKeterlambatan = RequestBody.create(MediaType.parse("text/plain"), keterlambatanStr);
            RequestBody bodyKetMasuk = RequestBody.create(MediaType.parse("text/plain"), ketMasuk);
            RequestBody bodyPosisi = RequestBody.create(MediaType.parse("text/plain"), posisiMasuk);
            RequestBody bodyStatus = RequestBody.create(MediaType.parse("text/plain"), statusMasuk);
            RequestBody bodyValidasi = RequestBody.create(MediaType.parse("text/plain"), validasiMasuk);

            List<RequestBody> bodyPegawaiList = new ArrayList<>();
            for (String p : pegawaiList) {
                bodyPegawaiList.add(RequestBody.create(MediaType.parse("text/plain"), p));
            }

            MultipartBody.Part partFoto = prepareFilePart("fototaging_masuk", uriFoto);
            MultipartBody.Part partLampiran = prepareFilePart("lampiran_masuk", uriLampiran);

            api.updateDataMasukAdminMultipart(
                    bodyTanggal, bodyMasuk, bodyKeterlambatan, bodyKetMasuk,
                    bodyPosisi, bodyStatus, bodyValidasi, bodyPegawaiList,
                    partFoto, partLampiran
            ).enqueue(new Callback<ResponsePOJO>() {
                @Override
                public void onResponse(Call<ResponsePOJO> call, Response<ResponsePOJO> response) {
                    dialogProses.dismiss();
                    handleResponse(response);
                }

                @Override
                public void onFailure(Call<ResponsePOJO> call, Throwable t) {
                    dialogProses.dismiss();
                    dialogView.viewNotifKosong(AdminUpdateMasukActivity.this, "Koneksi Gagal", t.getMessage());
                }
            });
        }
    }

    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        if (fileUri == null) return null;
        try {
            File file = getFileFromUri(fileUri);
            if (file != null && file.exists()) {
                RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(fileUri) != null ? getContentResolver().getType(fileUri) : "*/*"), file);
                return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
            }
        } catch (Exception e) {
            Log.e("ADMIN_UPDATE", "Error prepareFilePart", e);
        }
        return null;
    }

    private File getFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File file = new File(getCacheDir(), "upload_temp_" + System.currentTimeMillis());
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int read;
            if (inputStream != null) {
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                inputStream.close();
            }
            outputStream.close();
            return file;
        } catch (Exception e) {
            Log.e("ADMIN_UPDATE", "Error getFileFromUri", e);
            return null;
        }
    }

    private void handleResponse(Response<ResponsePOJO> response) {
        if (response.isSuccessful() && response.body() != null) {
            ResponsePOJO pojo = response.body();
            if (pojo.isStatus()) {
                dialogView.viewSuksesNonFinish(AdminUpdateMasukActivity.this, pojo.getRemarks() != null ? pojo.getRemarks() : "Update data masuk berhasil!");
            } else {
                dialogView.viewNotifKosong(AdminUpdateMasukActivity.this, "Gagal Update", pojo.getRemarks() != null ? pojo.getRemarks() : "Proses gagal dilakukan.");
            }
        } else {
            dialogView.viewNotifKosong(AdminUpdateMasukActivity.this, "Gagal Server", "HTTP " + response.code());
        }
    }
}
