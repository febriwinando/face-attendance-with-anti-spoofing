package go.pemkott.appsandroidmobiletebingtinggi.admin;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

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
import go.pemkott.appsandroidmobiletebingtinggi.model.AdminEmployeeListResponse;
import go.pemkott.appsandroidmobiletebingtinggi.model.AdminOpdListResponse;
import go.pemkott.appsandroidmobiletebingtinggi.model.AdminPresenceFindResponse;
import go.pemkott.appsandroidmobiletebingtinggi.model.Employee;
import go.pemkott.appsandroidmobiletebingtinggi.model.OpdModel;
import go.pemkott.appsandroidmobiletebingtinggi.model.Presensi;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminPengolahanAbsensiActivity extends AppCompatActivity {

    private Spinner spOpd, spPegawai, spPosisiMasuk, spPosisiPulang;
    private EditText etTanggal, etAbsensiId, etJamMasuk, etKeterlambatan, etKetMasuk, etJamPulang;

    private List<OpdModel> listOpd = new ArrayList<>();
    private List<Employee> listPegawai = new ArrayList<>();

    private Uri uriFotoMasuk = null;
    private Uri uriFotoPulang = null;
    private Uri uriLampiranMasuk = null;
    private Uri uriLampiranPulang = null;

    private Calendar calendar = Calendar.getInstance();
    private DialogView dialogView;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private final String[] POSISI_TEXTS_MASUK = {
            "Kehadiran Normal (masuk)",
            "Tugas Lapangan (tl-masuk)",
            "Perjalanan Dinas (pd)",
            "Sakit (sk)",
            "Keperluan Pribadi (kp)",
            "Cuti (ct)"
    };

    private final String[] POSISI_VALUES_MASUK = {
            "masuk", "tl-masuk", "pd", "sk", "kp", "ct"
    };

    private final String[] STATUS_VALUES_MASUK = {
            "hadir", "dinas", "dinas", "sakit", "pribadi", "ijin"
    };

    private final String[] POSISI_TEXTS_PULANG = {
            "Kehadiran Normal (pulang)",
            "Tugas Lapangan (tl-pulang)",
            "Perjalanan Dinas (pd)",
            "Sakit (sk)",
            "Keperluan Pribadi (kp)",
            "Cuti (ct)"
    };

    private final String[] POSISI_VALUES_PULANG = {
            "pulang", "tl-pulang", "pd", "sk", "kp", "ct"
    };

    private final String[] STATUS_VALUES_PULANG = {
            "hadir", "dinas", "dinas", "sakit", "pribadi", "ijin"
    };

    private ActivityResultLauncher<Intent> fotoMasukLauncher;
    private ActivityResultLauncher<Intent> fotoPulangLauncher;
    private ActivityResultLauncher<Intent> lampiranMasukLauncher;
    private ActivityResultLauncher<Intent> lampiranPulangLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_pengolahan_absensi);

        dialogView = new DialogView(this);
        ImageView ivBack = findViewById(R.id.ivAdminPengolahanBack);

        spOpd = findViewById(R.id.spAdminPengolahanOpd);
        spPegawai = findViewById(R.id.spAdminPengolahanPegawai);
        etTanggal = findViewById(R.id.etAdminPengolahanTanggal);
        MaterialButton btnFindPresence = findViewById(R.id.btnAdminFindPresence);

        etAbsensiId = findViewById(R.id.etAdminPengolahanId);
        spPosisiMasuk = findViewById(R.id.spAdminPengolahanPosisiMasuk);
        etJamMasuk = findViewById(R.id.etAdminPengolahanJamMasuk);
        etKeterlambatan = findViewById(R.id.etAdminPengolahanKeterlambatan);
        etKetMasuk = findViewById(R.id.etAdminPengolahanKetMasuk);

        spPosisiPulang = findViewById(R.id.spAdminPengolahanPosisiPulang);
        etJamPulang = findViewById(R.id.etAdminPengolahanJamPulang);

        MaterialButton btnFotoMasuk = findViewById(R.id.btnPickFotoMasukPengolahan);
        MaterialButton btnLampiranMasuk = findViewById(R.id.btnPickLampiranMasukPengolahan);
        MaterialButton btnFotoPulang = findViewById(R.id.btnPickFotoPulangPengolahan);
        MaterialButton btnLampiranPulang = findViewById(R.id.btnPickLampiranPulangPengolahan);

        MaterialButton btnSubmit = findViewById(R.id.btnAdminPengolahanSubmit);

        ivBack.setOnClickListener(v -> finish());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        // Setup Spinners Posisi
        ArrayAdapter<String> adapterMasuk = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, POSISI_TEXTS_MASUK);
        adapterMasuk.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPosisiMasuk.setAdapter(adapterMasuk);

        ArrayAdapter<String> adapterPulang = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, POSISI_TEXTS_PULANG);
        adapterPulang.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPosisiPulang.setAdapter(adapterPulang);

        // Date Picker & Defaults
        etTanggal.setText(dateFormat.format(calendar.getTime()));
        etTanggal.setOnClickListener(v -> showDatePicker());

        etJamMasuk.setText("07:30");
        etKeterlambatan.setText("0");
        etKetMasuk.setText("sesuai waktu");
        etJamPulang.setText("16:30");

        // Load OPDs
        loadOpdList();

        spOpd.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                v.performClick();
                if (listOpd == null || listOpd.isEmpty()) {
                    loadOpdList();
                }
            }
            return false;
        });

        spPegawai.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                v.performClick();
                if (listPegawai == null || listPegawai.isEmpty()) {
                    if (spOpd.getSelectedItemPosition() >= 0 && listOpd != null && !listOpd.isEmpty()) {
                        loadPegawaiList(listOpd.get(spOpd.getSelectedItemPosition()).getId());
                    } else {
                        loadOpdList();
                    }
                }
            }
            return false;
        });

        btnFindPresence.setOnClickListener(v -> findPresenceForSelectedEmployee());

        // File Launchers
        fotoMasukLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        uriFotoMasuk = result.getData().getData();
                    }
                }
        );

        fotoPulangLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        uriFotoPulang = result.getData().getData();
                    }
                }
        );

        lampiranMasukLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        uriLampiranMasuk = result.getData().getData();
                    }
                }
        );

        lampiranPulangLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        uriLampiranPulang = result.getData().getData();
                    }
                }
        );

        btnFotoMasuk.setOnClickListener(v -> pickFile("image/*", fotoMasukLauncher));
        btnFotoPulang.setOnClickListener(v -> pickFile("image/*", fotoPulangLauncher));
        btnLampiranMasuk.setOnClickListener(v -> pickFile("*/*", lampiranMasukLauncher));
        btnLampiranPulang.setOnClickListener(v -> pickFile("*/*", lampiranPulangLauncher));

        btnSubmit.setOnClickListener(v -> submitPengolahan());
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            etTanggal.setText(dateFormat.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadOpdList() {
        Dialog dialogLoading = new Dialog(this, R.style.DialogStyle);
        dialogLoading.setContentView(R.layout.view_proses);
        dialogLoading.setCancelable(false);
        dialogLoading.show();

        HttpService api = RetroClient.getInstance(this).getApi2();
        api.getAdminOpdList().enqueue(new Callback<AdminOpdListResponse>() {
            @Override
            public void onResponse(Call<AdminOpdListResponse> call, Response<AdminOpdListResponse> response) {
                dialogLoading.dismiss();
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    listOpd = response.body().getData();
                    if (listOpd != null && !listOpd.isEmpty()) {
                        ArrayAdapter<OpdModel> adapter = new ArrayAdapter<>(AdminPengolahanAbsensiActivity.this, android.R.layout.simple_spinner_dropdown_item, listOpd);
                        spOpd.setAdapter(adapter);

                        spOpd.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                if (listOpd != null && position >= 0 && position < listOpd.size()) {
                                    OpdModel selectedOpd = listOpd.get(position);
                                    if (selectedOpd != null && selectedOpd.getId() != null) {
                                        loadPegawaiList(selectedOpd.getId());
                                    }
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {}
                        });

                        if (listOpd.get(0) != null && listOpd.get(0).getId() != null) {
                            loadPegawaiList(listOpd.get(0).getId());
                        }
                    } else {
                        dialogView.viewNotifKosong(AdminPengolahanAbsensiActivity.this, "Data OPD Kosong", "Tidak ada data OPD pada server.");
                    }
                } else {
                    dialogView.viewNotifKosong(AdminPengolahanAbsensiActivity.this, "Gagal Memuat OPD", "Server mengembalikan HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<AdminOpdListResponse> call, Throwable t) {
                dialogLoading.dismiss();
                Log.e("PENGOLAHAN_ADMIN", "Error loadOpdList", t);
                dialogView.viewNotifKosong(AdminPengolahanAbsensiActivity.this, "Koneksi Server Gagal", "Gagal memuat OPD dari server (" + go.pemkott.appsandroidmobiletebingtinggi.api.ServerConfig.getApiUrl() + "):\n" + t.getMessage() + "\n\nPastikan Laravel server aktif (`php artisan serve`) dan perintah `adb reverse tcp:8000 tcp:8000` telah dijalankan.");
            }
        });
    }

    private void loadPegawaiList(String opdId) {
        Dialog dialogLoading = new Dialog(this, R.style.DialogStyle);
        dialogLoading.setContentView(R.layout.view_proses);
        dialogLoading.setCancelable(false);
        dialogLoading.show();

        HttpService api = RetroClient.getInstance(this).getApi2();
        api.getAdminPegawaiList(opdId).enqueue(new Callback<AdminEmployeeListResponse>() {
            @Override
            public void onResponse(Call<AdminEmployeeListResponse> call, Response<AdminEmployeeListResponse> response) {
                dialogLoading.dismiss();
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    listPegawai = response.body().getData();
                    if (listPegawai != null && !listPegawai.isEmpty()) {
                        ArrayAdapter<Employee> adapter = new ArrayAdapter<>(AdminPengolahanAbsensiActivity.this, android.R.layout.simple_spinner_dropdown_item, listPegawai);
                        spPegawai.setAdapter(adapter);

                        spPegawai.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                findPresenceForSelectedEmployeeSilently();
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {}
                        });

                        findPresenceForSelectedEmployeeSilently();
                    } else {
                        spPegawai.setAdapter(null);
                        dialogView.viewNotifKosong(AdminPengolahanAbsensiActivity.this, "Daftar Pegawai", "Tidak ada pegawai aktif pada OPD ini.");
                    }
                } else {
                    dialogView.viewNotifKosong(AdminPengolahanAbsensiActivity.this, "Gagal Memuat Pegawai", "Server mengembalikan HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<AdminEmployeeListResponse> call, Throwable t) {
                dialogLoading.dismiss();
                Log.e("PENGOLAHAN_ADMIN", "Error loadPegawaiList", t);
                dialogView.viewNotifKosong(AdminPengolahanAbsensiActivity.this, "Koneksi Gagal", "Gagal memuat pegawai dari server: " + t.getMessage());
            }
        });
    }

    private void findPresenceForSelectedEmployee() {
        int selectedEmpIdx = spPegawai.getSelectedItemPosition();
        if (selectedEmpIdx < 0 || selectedEmpIdx >= listPegawai.size()) {
            dialogView.viewNotifKosong(this, "Peringatan", "Silakan pilih pegawai terlebih dahulu.");
            return;
        }

        Employee selectedEmp = listPegawai.get(selectedEmpIdx);
        String employeeId = String.valueOf(selectedEmp.getId());
        String tanggal = etTanggal.getText().toString().trim();

        if (tanggal.isEmpty()) {
            dialogView.viewNotifKosong(this, "Peringatan", "Silakan pilih tanggal absensi.");
            return;
        }

        Dialog dialogProses = new Dialog(this, R.style.DialogStyle);
        dialogProses.setContentView(R.layout.view_proses);
        dialogProses.setCancelable(false);
        dialogProses.show();

        HttpService api = RetroClient.getInstance(this).getApi2();
        api.findPresenceAdmin(employeeId, tanggal).enqueue(new Callback<AdminPresenceFindResponse>() {
            @Override
            public void onResponse(Call<AdminPresenceFindResponse> call, Response<AdminPresenceFindResponse> response) {
                dialogProses.dismiss();
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Presensi p = response.body().getData();
                    if (p != null) {
                        etAbsensiId.setText(String.valueOf(p.getId()));
                        if (p.getJam_masuk() != null) etJamMasuk.setText(p.getJam_masuk());
                        if (p.getKeterlambatan() != null) etKeterlambatan.setText(p.getKeterlambatan());
                        if (p.getKet_masuk() != null) etKetMasuk.setText(p.getKet_masuk());
                        if (p.getJam_pulang() != null) etJamPulang.setText(p.getJam_pulang());

                        // Match posisi
                        if (p.getPosisi_masuk() != null) {
                            for (int i = 0; i < POSISI_VALUES_MASUK.length; i++) {
                                if (POSISI_VALUES_MASUK[i].equalsIgnoreCase(p.getPosisi_masuk())) {
                                    spPosisiMasuk.setSelection(i);
                                    break;
                                }
                            }
                        }

                        if (p.getPosisi_pulang() != null) {
                            for (int i = 0; i < POSISI_VALUES_PULANG.length; i++) {
                                if (POSISI_VALUES_PULANG[i].equalsIgnoreCase(p.getPosisi_pulang())) {
                                    spPosisiPulang.setSelection(i);
                                    break;
                                }
                            }
                        }

                        dialogView.viewSuksesNonFinish(AdminPengolahanAbsensiActivity.this, "Data presensi ditemukan (ID #" + p.getId() + ")");
                    }
                } else {
                    dialogView.viewNotifKosong(AdminPengolahanAbsensiActivity.this, "Informasi", "Belum ada record presensi pada tanggal tersebut.");
                }
            }

            @Override
            public void onFailure(Call<AdminPresenceFindResponse> call, Throwable t) {
                dialogProses.dismiss();
                dialogView.viewNotifKosong(AdminPengolahanAbsensiActivity.this, "Koneksi Gagal", t.getMessage());
            }
        });
    }

    private void findPresenceForSelectedEmployeeSilently() {
        if (listPegawai == null || listPegawai.isEmpty()) return;
        int selectedEmpIdx = spPegawai.getSelectedItemPosition();
        if (selectedEmpIdx < 0 || selectedEmpIdx >= listPegawai.size()) return;

        Employee selectedEmp = listPegawai.get(selectedEmpIdx);
        if (selectedEmp == null) return;
        String employeeId = selectedEmp.getId();
        String tanggal = etTanggal.getText().toString().trim();

        if (employeeId == null || employeeId.isEmpty() || tanggal.isEmpty()) return;

        HttpService api = RetroClient.getInstance(this).getApi2();
        api.findPresenceAdmin(employeeId, tanggal).enqueue(new Callback<AdminPresenceFindResponse>() {
            @Override
            public void onResponse(Call<AdminPresenceFindResponse> call, Response<AdminPresenceFindResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Presensi p = response.body().getData();
                    if (p != null) {
                        etAbsensiId.setText(p.getId() != null ? p.getId() : "");
                        if (p.getJam_masuk() != null) etJamMasuk.setText(p.getJam_masuk());
                        if (p.getKeterlambatan() != null) etKeterlambatan.setText(p.getKeterlambatan());
                        if (p.getKet_masuk() != null) etKetMasuk.setText(p.getKet_masuk());
                        if (p.getJam_pulang() != null) etJamPulang.setText(p.getJam_pulang());

                        if (p.getPosisi_masuk() != null) {
                            for (int i = 0; i < POSISI_VALUES_MASUK.length; i++) {
                                if (POSISI_VALUES_MASUK[i].equalsIgnoreCase(p.getPosisi_masuk())) {
                                    spPosisiMasuk.setSelection(i);
                                    break;
                                }
                            }
                        }

                        if (p.getPosisi_pulang() != null) {
                            for (int i = 0; i < POSISI_VALUES_PULANG.length; i++) {
                                if (POSISI_VALUES_PULANG[i].equalsIgnoreCase(p.getPosisi_pulang())) {
                                    spPosisiPulang.setSelection(i);
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    etAbsensiId.setText("");
                }
            }

            @Override
            public void onFailure(Call<AdminPresenceFindResponse> call, Throwable t) {
                etAbsensiId.setText("");
            }
        });
    }

    private void pickFile(String mimeType, ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(mimeType);
        launcher.launch(intent);
    }

    private void submitPengolahan() {
        String idAbsensi = etAbsensiId.getText().toString().trim();
        String jamMasuk = etJamMasuk.getText().toString().trim();
        String keterlambatanStr = etKeterlambatan.getText().toString().trim();
        String ketMasuk = etKetMasuk.getText().toString().trim();
        String jamPulang = etJamPulang.getText().toString().trim();

        int idxM = spPosisiMasuk.getSelectedItemPosition();
        String posisiMasuk = POSISI_VALUES_MASUK[idxM];
        String statusMasuk = STATUS_VALUES_MASUK[idxM];
        String validasiMasuk = "2";

        int idxP = spPosisiPulang.getSelectedItemPosition();
        String posisiPulang = POSISI_VALUES_PULANG[idxP];
        String statusPulang = STATUS_VALUES_PULANG[idxP];
        String validasiPulang = "2";

        if (idAbsensi.isEmpty() || jamMasuk.isEmpty()) {
            dialogView.viewNotifKosong(this, "Peringatan", "ID Absensi dan Jam Masuk wajib diisi (Gunakan tombol 'Cari Data' untuk memuat ID otomatis).");
            return;
        }

        int keterlambatan = 0;
        try { keterlambatan = Integer.parseInt(keterlambatanStr); } catch (Exception ignored) {}

        Dialog dialogProses = new Dialog(this, R.style.DialogStyle);
        dialogProses.setContentView(R.layout.view_proses);
        dialogProses.setCancelable(false);
        dialogProses.show();

        HttpService api = RetroClient.getInstance(this).getApi2();

        if (uriFotoMasuk == null && uriFotoPulang == null && uriLampiranMasuk == null && uriLampiranPulang == null) {
            // Mode Form
            api.storePengolahanAbsensiAdmin(
                    idAbsensi, jamMasuk, keterlambatan, posisiMasuk, statusMasuk, validasiMasuk, ketMasuk,
                    jamPulang, posisiPulang, statusPulang, validasiPulang
            ).enqueue(new Callback<ResponsePOJO>() {
                @Override
                public void onResponse(Call<ResponsePOJO> call, Response<ResponsePOJO> response) {
                    dialogProses.dismiss();
                    handleResponse(response);
                }

                @Override
                public void onFailure(Call<ResponsePOJO> call, Throwable t) {
                    dialogProses.dismiss();
                    dialogView.viewNotifKosong(AdminPengolahanAbsensiActivity.this, "Koneksi Gagal", t.getMessage());
                }
            });
        } else {
            // Mode Multipart
            RequestBody bodyId = RequestBody.create(MediaType.parse("text/plain"), idAbsensi);
            RequestBody bodyJamM = RequestBody.create(MediaType.parse("text/plain"), jamMasuk);
            RequestBody bodyKetL = RequestBody.create(MediaType.parse("text/plain"), keterlambatanStr);
            RequestBody bodyPosM = RequestBody.create(MediaType.parse("text/plain"), posisiMasuk);
            RequestBody bodyStatM = RequestBody.create(MediaType.parse("text/plain"), statusMasuk);
            RequestBody bodyValM = RequestBody.create(MediaType.parse("text/plain"), validasiMasuk);
            RequestBody bodyKetM = RequestBody.create(MediaType.parse("text/plain"), ketMasuk);

            RequestBody bodyJamP = RequestBody.create(MediaType.parse("text/plain"), jamPulang);
            RequestBody bodyPosP = RequestBody.create(MediaType.parse("text/plain"), posisiPulang);
            RequestBody bodyStatP = RequestBody.create(MediaType.parse("text/plain"), statusPulang);
            RequestBody bodyValP = RequestBody.create(MediaType.parse("text/plain"), validasiPulang);

            MultipartBody.Part partFotoM = prepareFilePart("fototaging_masuk", uriFotoMasuk);
            MultipartBody.Part partFotoP = prepareFilePart("fototaging_pulang", uriFotoPulang);
            MultipartBody.Part partLampM = prepareFilePart("lampiran_masuk", uriLampiranMasuk);
            MultipartBody.Part partLampP = prepareFilePart("lampiran_pulang", uriLampiranPulang);

            api.storePengolahanAbsensiAdminMultipart(
                    bodyId, bodyJamM, bodyKetL, bodyPosM, bodyStatM, bodyValM, bodyKetM,
                    bodyJamP, bodyPosP, bodyStatP, bodyValP,
                    partFotoM, partFotoP, partLampM, partLampP
            ).enqueue(new Callback<ResponsePOJO>() {
                @Override
                public void onResponse(Call<ResponsePOJO> call, Response<ResponsePOJO> response) {
                    dialogProses.dismiss();
                    handleResponse(response);
                }

                @Override
                public void onFailure(Call<ResponsePOJO> call, Throwable t) {
                    dialogProses.dismiss();
                    dialogView.viewNotifKosong(AdminPengolahanAbsensiActivity.this, "Koneksi Gagal", t.getMessage());
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
            Log.e("PENGOLAHAN_ADMIN", "Error prepareFilePart", e);
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
            Log.e("PENGOLAHAN_ADMIN", "Error getFileFromUri", e);
            return null;
        }
    }

    private void handleResponse(Response<ResponsePOJO> response) {
        if (response.isSuccessful() && response.body() != null) {
            ResponsePOJO pojo = response.body();
            if (pojo.isStatus()) {
                dialogView.viewSuksesNonFinish(AdminPengolahanAbsensiActivity.this, pojo.getRemarks() != null ? pojo.getRemarks() : "Data pengolahan absensi berhasil diubah!");
            } else {
                dialogView.viewNotifKosong(AdminPengolahanAbsensiActivity.this, "Gagal Ubah", pojo.getRemarks() != null ? pojo.getRemarks() : "Gagal mengubah data absensi.");
            }
        } else {
            dialogView.viewNotifKosong(AdminPengolahanAbsensiActivity.this, "Gagal Server", "HTTP " + response.code());
        }
    }
}
