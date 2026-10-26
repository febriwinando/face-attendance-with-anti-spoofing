package go.pemkott.appsandroidmobiletebingtinggi.admin;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.api.ResponsePOJO;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.api.ServerConfig;
import go.pemkott.appsandroidmobiletebingtinggi.camerax.CameraXTanpaDetectionAcitvity;
import go.pemkott.appsandroidmobiletebingtinggi.dialogview.DialogView;
import go.pemkott.appsandroidmobiletebingtinggi.konstanta.AmbilFoto;
import go.pemkott.appsandroidmobiletebingtinggi.model.AdminEmployeeListResponse;
import go.pemkott.appsandroidmobiletebingtinggi.model.AdminOpdListResponse;
import go.pemkott.appsandroidmobiletebingtinggi.model.AdminPresenceFindResponse;
import go.pemkott.appsandroidmobiletebingtinggi.model.Employee;
import go.pemkott.appsandroidmobiletebingtinggi.model.OpdModel;
import go.pemkott.appsandroidmobiletebingtinggi.model.Presensi;
import go.pemkott.appsandroidmobiletebingtinggi.utils.FileUtil;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminPengolahanAbsensiActivity extends AppCompatActivity {

    private Spinner spOpd, spPegawai, spPosisiMasuk, spPosisiPulang;
    private EditText etTanggal, etAbsensiId, etJamMasuk, etKeterlambatan, etKetMasuk, etJamPulang;

    private ImageView iconFotoMasuk, iconLampiranMasuk, iconFotoPulang, iconLampiranPulang;
    private ImageView ivFotoMasukFinal, ivLampiranMasukFinal, ivFotoPulangFinal, ivLampiranPulangFinal;

    private View llPdfMasuk, llPdfPulang;
    private TextView tvPdfMasukName, tvPdfPulangName;

    private List<OpdModel> listOpd = new ArrayList<>();
    private List<Employee> listPegawai = new ArrayList<>();

    private File fileFotoMasuk = null;
    private File fileLampiranMasuk = null;
    private File fileFotoPulang = null;
    private File fileLampiranPulang = null;

    private AmbilFoto ambilFoto;
    private DialogView dialogView;
    private Calendar calendar = Calendar.getInstance();
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

    private ActivityResultLauncher<Intent> cameraFotoMasukLauncher;
    private ActivityResultLauncher<Intent> cameraLampiranMasukLauncher;
    private ActivityResultLauncher<Intent> cameraFotoPulangLauncher;
    private ActivityResultLauncher<Intent> cameraLampiranPulangLauncher;

    private ActivityResultLauncher<Intent> pdfMasukLauncher;
    private ActivityResultLauncher<Intent> pdfPulangLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_pengolahan_absensi);

        ambilFoto = new AmbilFoto(AdminPengolahanAbsensiActivity.this);
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

        MaterialCardView cardPickFotoMasuk = findViewById(R.id.cardPickFotoMasuk);
        MaterialCardView cardPickLampiranMasuk = findViewById(R.id.cardPickLampiranMasuk);
        MaterialCardView cardPickFotoPulang = findViewById(R.id.cardPickFotoPulang);
        MaterialCardView cardPickLampiranPulang = findViewById(R.id.cardPickLampiranPulang);

        iconFotoMasuk = findViewById(R.id.iconFotoMasuk);
        ivFotoMasukFinal = findViewById(R.id.ivFotoMasukFinal);
        iconLampiranMasuk = findViewById(R.id.iconLampiranMasuk);
        ivLampiranMasukFinal = findViewById(R.id.ivLampiranMasukFinal);
        llPdfMasuk = findViewById(R.id.llPdfMasuk);
        tvPdfMasukName = findViewById(R.id.tvPdfMasukName);

        iconFotoPulang = findViewById(R.id.iconFotoPulang);
        ivFotoPulangFinal = findViewById(R.id.ivFotoPulangFinal);
        iconLampiranPulang = findViewById(R.id.iconLampiranPulang);
        ivLampiranPulangFinal = findViewById(R.id.ivLampiranPulangFinal);
        llPdfPulang = findViewById(R.id.llPdfPulang);
        tvPdfPulangName = findViewById(R.id.tvPdfPulangName);

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

        // Launchers
        setupMediaLaunchers();

        cardPickFotoMasuk.setOnClickListener(v -> viewLampiran(1));
        cardPickLampiranMasuk.setOnClickListener(v -> viewLampiran(2));
        cardPickFotoPulang.setOnClickListener(v -> viewLampiran(3));
        cardPickLampiranPulang.setOnClickListener(v -> viewLampiran(4));

        btnSubmit.setOnClickListener(v -> submitPengolahan());
    }

    public void viewLampiran(int targetType) {
        Dialog dialogLampiran = new Dialog(AdminPengolahanAbsensiActivity.this, R.style.DialogStyle);
        dialogLampiran.setContentView(R.layout.view_add_lampiran);
        View llFileManager = dialogLampiran.findViewById(R.id.llFileManager);
        View llKamera = dialogLampiran.findViewById(R.id.llKamera);
        View llDokumen = dialogLampiran.findViewById(R.id.llDokumen);
        View ivTutupViewLampiran = dialogLampiran.findViewById(R.id.ivTutupViewLampiran);

        llFileManager.setOnClickListener(v -> {
            Intent i = new Intent();
            i.setType("image/*");
            i.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(i, "Pilih Gambar"), 100 + targetType);
            dialogLampiran.dismiss();
        });

        llDokumen.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (targetType == 1 || targetType == 2) {
                pdfMasukLauncher.launch(intent);
            } else {
                pdfPulangLauncher.launch(intent);
            }
            dialogLampiran.dismiss();
        });

        llKamera.setOnClickListener(v -> {
            Intent intent = new Intent(this, CameraXTanpaDetectionAcitvity.class);
            intent.putExtra("is_lampiran", true);
            switch (targetType) {
                case 1: cameraFotoMasukLauncher.launch(intent); break;
                case 2: cameraLampiranMasukLauncher.launch(intent); break;
                case 3: cameraFotoPulangLauncher.launch(intent); break;
                case 4: cameraLampiranPulangLauncher.launch(intent); break;
            }
            dialogLampiran.dismiss();
        });

        ivTutupViewLampiran.setOnClickListener(v -> dialogLampiran.dismiss());
        dialogLampiran.show();
    }

    private void setupMediaLaunchers() {
        cameraFotoMasukLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> handleCameraResult(result, 1)
        );

        cameraLampiranMasukLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> handleCameraResult(result, 2)
        );

        cameraFotoPulangLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> handleCameraResult(result, 3)
        );

        cameraLampiranPulangLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> handleCameraResult(result, 4)
        );

        pdfMasukLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        try {
                            Uri pdfUri = result.getData().getData();
                            fileLampiranMasuk = getPDFPath(pdfUri);
                            String displayName = getFileNameFromUri(pdfUri);

                            llPdfMasuk.setVisibility(View.VISIBLE);
                            tvPdfMasukName.setText(displayName);
                            ivLampiranMasukFinal.setVisibility(View.GONE);
                            iconLampiranMasuk.setVisibility(View.GONE);
                        } catch (Exception e) {
                            Log.e("PENGOLAHAN_ADMIN", "Error pdfMasukLauncher", e);
                        }
                    }
                }
        );

        pdfPulangLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        try {
                            Uri pdfUri = result.getData().getData();
                            fileLampiranPulang = getPDFPath(pdfUri);
                            String displayName = getFileNameFromUri(pdfUri);

                            llPdfPulang.setVisibility(View.VISIBLE);
                            tvPdfPulangName.setText(displayName);
                            ivLampiranPulangFinal.setVisibility(View.GONE);
                            iconLampiranPulang.setVisibility(View.GONE);
                        } catch (Exception e) {
                            Log.e("PENGOLAHAN_ADMIN", "Error pdfPulangLauncher", e);
                        }
                    }
                }
        );
    }

    private void handleCameraResult(androidx.activity.result.ActivityResult result, int targetType) {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            String fileName = result.getData().getStringExtra("namafile");
            File targetFile = null;

            if (fileName != null && (fileName.startsWith("content://") || fileName.startsWith("file://"))) {
                try {
                    targetFile = createTempFileFromUri(Uri.parse(fileName));
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Gagal memproses foto", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else if (fileName != null) {
                String myDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/eabsensi";
                targetFile = new File(myDir, fileName);
            }

            if (targetFile != null && targetFile.exists()) {
                byte[] imageBytes = ambilFoto.compressToMax80KB(targetFile);
                Bitmap previewBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                switch (targetType) {
                    case 1:
                        fileFotoMasuk = targetFile;
                        ivFotoMasukFinal.setImageBitmap(previewBitmap);
                        ivFotoMasukFinal.setVisibility(View.VISIBLE);
                        iconFotoMasuk.setVisibility(View.GONE);
                        break;
                    case 2:
                        fileLampiranMasuk = targetFile;
                        ivLampiranMasukFinal.setImageBitmap(previewBitmap);
                        ivLampiranMasukFinal.setVisibility(View.VISIBLE);
                        iconLampiranMasuk.setVisibility(View.GONE);
                        llPdfMasuk.setVisibility(View.GONE);
                        break;
                    case 3:
                        fileFotoPulang = targetFile;
                        ivFotoPulangFinal.setImageBitmap(previewBitmap);
                        ivFotoPulangFinal.setVisibility(View.VISIBLE);
                        iconFotoPulang.setVisibility(View.GONE);
                        break;
                    case 4:
                        fileLampiranPulang = targetFile;
                        ivLampiranPulangFinal.setImageBitmap(previewBitmap);
                        ivLampiranPulangFinal.setVisibility(View.VISIBLE);
                        iconLampiranPulang.setVisibility(View.GONE);
                        llPdfPulang.setVisibility(View.GONE);
                        break;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            int targetType = requestCode - 100;
            if (targetType >= 1 && targetType <= 4) {
                try {
                    Uri selectedImageUri = data.getData();
                    String FilePath2 = FileUtil.getDriveFilePath(selectedImageUri, this);
                    File originalLampiran = new File(FilePath2);
                    File filelampiran = ambilFoto.compressToFile(this, originalLampiran);

                    BitmapFactory.Options optsLampiran = new BitmapFactory.Options();
                    optsLampiran.inSampleSize = 4;
                    optsLampiran.inPreferredConfig = Bitmap.Config.RGB_565;
                    Bitmap preview = BitmapFactory.decodeFile(filelampiran.getAbsolutePath(), optsLampiran);

                    switch (targetType) {
                        case 1:
                            fileFotoMasuk = filelampiran;
                            ivFotoMasukFinal.setImageBitmap(preview);
                            ivFotoMasukFinal.setVisibility(View.VISIBLE);
                            iconFotoMasuk.setVisibility(View.GONE);
                            break;
                        case 2:
                            fileLampiranMasuk = filelampiran;
                            ivLampiranMasukFinal.setImageBitmap(preview);
                            ivLampiranMasukFinal.setVisibility(View.VISIBLE);
                            iconLampiranMasuk.setVisibility(View.GONE);
                            llPdfMasuk.setVisibility(View.GONE);
                            break;
                        case 3:
                            fileFotoPulang = filelampiran;
                            ivFotoPulangFinal.setImageBitmap(preview);
                            ivFotoPulangFinal.setVisibility(View.VISIBLE);
                            iconFotoPulang.setVisibility(View.GONE);
                            break;
                        case 4:
                            fileLampiranPulang = filelampiran;
                            ivLampiranPulangFinal.setImageBitmap(preview);
                            ivLampiranPulangFinal.setVisibility(View.VISIBLE);
                            iconLampiranPulang.setVisibility(View.GONE);
                            llPdfPulang.setVisibility(View.GONE);
                            break;
                    }
                } catch (Exception e) {
                    Log.e("PENGOLAHAN_ADMIN", "Error onActivityResult gallery image", e);
                }
            }
        }
    }

    private File createTempFileFromUri(Uri uri) throws IOException {
        InputStream is = getContentResolver().openInputStream(uri);
        File tempFile = new File(getCacheDir(), "IMG_" + System.currentTimeMillis() + ".jpg");
        FileOutputStream fos = new FileOutputStream(tempFile);
        byte[] buffer = new byte[8192];
        int len;
        if (is != null) {
            while ((len = is.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            is.close();
        }
        return tempFile;
    }

    private File getPDFPath(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            byte[] pdfInBytes = new byte[inputStream.available()];
            int readBytes = inputStream.read(pdfInBytes);

            String mPath;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                mPath = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/admin_lampiran_" + System.currentTimeMillis() + ".pdf";
            } else {
                mPath = Environment.getExternalStorageDirectory().toString() + "/admin_lampiran_" + System.currentTimeMillis() + ".pdf";
            }

            File filelampiran = new File(mPath);
            OutputStream op = new FileOutputStream(filelampiran);
            op.write(pdfInBytes, 0, readBytes > 0 ? readBytes : pdfInBytes.length);
            op.close();
            inputStream.close();

            return filelampiran;
        } catch (Exception ae) {
            Log.e("PENGOLAHAN_ADMIN", "Error getPDFPath", ae);
            return null;
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri != null && uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                Log.e("PENGOLAHAN_ADMIN", "Error getting file name", e);
            }
        }
        if (result == null && uri != null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result != null ? result : ("file_temp_" + System.currentTimeMillis() + ".pdf");
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
                dialogView.viewNotifKosong(AdminPengolahanAbsensiActivity.this, "Koneksi Server Gagal", "Gagal memuat OPD dari server:\n" + t.getMessage());
            }
        });
    }

    private void loadPegawaiList(String opdId) {
        HttpService api = RetroClient.getInstance(this).getApi2();
        api.getAdminPegawaiList(opdId).enqueue(new Callback<AdminEmployeeListResponse>() {
            @Override
            public void onResponse(Call<AdminEmployeeListResponse> call, Response<AdminEmployeeListResponse> response) {
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
                    }
                } else {
                    Log.e("PENGOLAHAN_ADMIN", "loadPegawaiList failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<AdminEmployeeListResponse> call, Throwable t) {
                Log.e("PENGOLAHAN_ADMIN", "Error loadPegawaiList", t);
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
        String employeeId = selectedEmp.getId();
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
                    bindPresensiToUI(p);
                    if (p != null) {
                        dialogView.viewSuksesNonFinish(AdminPengolahanAbsensiActivity.this, "Data presensi ditemukan (ID #" + p.getId() + ")");
                    }
                } else {
                    bindPresensiToUI(null);
                    dialogView.viewNotifKosong(AdminPengolahanAbsensiActivity.this, "Informasi", "Belum ada record presensi pada tanggal tersebut.");
                }
            }

            @Override
            public void onFailure(Call<AdminPresenceFindResponse> call, Throwable t) {
                dialogProses.dismiss();
                bindPresensiToUI(null);
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
                    bindPresensiToUI(p);
                } else {
                    bindPresensiToUI(null);
                }
            }

            @Override
            public void onFailure(Call<AdminPresenceFindResponse> call, Throwable t) {
                bindPresensiToUI(null);
            }
        });
    }

    private void bindPresensiToUI(Presensi p) {
        if (p == null) {
            etAbsensiId.setText("");
            resetMediaViews();
            return;
        }

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

        // Load existing photos/lampiran byte arrays if available
        if (p.getPhototaging_masuk() != null && p.getPhototaging_masuk().length > 0) {
            Bitmap b = BitmapFactory.decodeByteArray(p.getPhototaging_masuk(), 0, p.getPhototaging_masuk().length);
            if (b != null) {
                ivFotoMasukFinal.setImageBitmap(b);
                ivFotoMasukFinal.setVisibility(View.VISIBLE);
                iconFotoMasuk.setVisibility(View.GONE);
            }
        }

        if (p.getPhototaging_pulang() != null && p.getPhototaging_pulang().length > 0) {
            Bitmap b = BitmapFactory.decodeByteArray(p.getPhototaging_pulang(), 0, p.getPhototaging_pulang().length);
            if (b != null) {
                ivFotoPulangFinal.setImageBitmap(b);
                ivFotoPulangFinal.setVisibility(View.VISIBLE);
                iconFotoPulang.setVisibility(View.GONE);
            }
        }

        if (p.getLampiran_masuk() != null && p.getLampiran_masuk().length > 0) {
            Bitmap b = BitmapFactory.decodeByteArray(p.getLampiran_masuk(), 0, p.getLampiran_masuk().length);
            if (b != null) {
                ivLampiranMasukFinal.setImageBitmap(b);
                ivLampiranMasukFinal.setVisibility(View.VISIBLE);
                iconLampiranMasuk.setVisibility(View.GONE);
                llPdfMasuk.setVisibility(View.GONE);
            }
        }

        if (p.getLampiran_pulang() != null && p.getLampiran_pulang().length > 0) {
            Bitmap b = BitmapFactory.decodeByteArray(p.getLampiran_pulang(), 0, p.getLampiran_pulang().length);
            if (b != null) {
                ivLampiranPulangFinal.setImageBitmap(b);
                ivLampiranPulangFinal.setVisibility(View.VISIBLE);
                iconLampiranPulang.setVisibility(View.GONE);
                llPdfPulang.setVisibility(View.GONE);
            }
        }
    }

    private void resetMediaViews() {
        fileFotoMasuk = null;
        fileLampiranMasuk = null;
        fileFotoPulang = null;
        fileLampiranPulang = null;

        ivFotoMasukFinal.setVisibility(View.GONE);
        iconFotoMasuk.setVisibility(View.VISIBLE);

        ivLampiranMasukFinal.setVisibility(View.GONE);
        iconLampiranMasuk.setVisibility(View.VISIBLE);
        llPdfMasuk.setVisibility(View.GONE);

        ivFotoPulangFinal.setVisibility(View.GONE);
        iconFotoPulang.setVisibility(View.VISIBLE);

        ivLampiranPulangFinal.setVisibility(View.GONE);
        iconLampiranPulang.setVisibility(View.VISIBLE);
        llPdfPulang.setVisibility(View.GONE);
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
            dialogView.viewNotifKosong(this, "Peringatan", "ID Absensi dan Jam Masuk wajib diisi.");
            return;
        }

        int keterlambatan = 0;
        try { keterlambatan = Integer.parseInt(keterlambatanStr); } catch (Exception ignored) {}

        Dialog dialogProses = new Dialog(this, R.style.DialogStyle);
        dialogProses.setContentView(R.layout.view_proses);
        dialogProses.setCancelable(false);
        dialogProses.show();

        HttpService api = RetroClient.getInstance(this).getApi2();

        if (fileFotoMasuk == null && fileFotoPulang == null && fileLampiranMasuk == null && fileLampiranPulang == null) {
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

            MultipartBody.Part partFotoM = prepareFilePart("fototaging_masuk", fileFotoMasuk, false);
            MultipartBody.Part partFotoP = prepareFilePart("fototaging_pulang", fileFotoPulang, false);
            MultipartBody.Part partLampM = prepareFilePart("lampiran_masuk", fileLampiranMasuk, fileLampiranMasuk != null && fileLampiranMasuk.getName().endsWith(".pdf"));
            MultipartBody.Part partLampP = prepareFilePart("lampiran_pulang", fileLampiranPulang, fileLampiranPulang != null && fileLampiranPulang.getName().endsWith(".pdf"));

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

    private MultipartBody.Part prepareFilePart(String partName, File file, boolean isPdf) {
        if (file == null || !file.exists()) return null;
        try {
            String mimeType = isPdf ? "application/pdf" : "image/*";
            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), file);
            return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
        } catch (Exception e) {
            Log.e("PENGOLAHAN_ADMIN", "Error prepareFilePart", e);
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
