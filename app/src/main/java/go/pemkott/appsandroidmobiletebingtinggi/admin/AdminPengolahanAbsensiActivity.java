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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

    private Spinner spPosisiMasuk, spPosisiPulang;
    private TextView tvAdminOpdSelect, tvAdminPegawaiSelect;
    private EditText etTanggal, etAbsensiId, etJamMasuk, etKeterlambatan, etKetMasuk, etJamPulang;
    private android.widget.CheckBox cbClearPulang;

    private Presensi currentLoadedPresence = null;
    private int loadedPosMasukIdx = 0;
    private int loadedPosPulangIdx = 0;

    private ImageView iconFotoMasuk, iconLampiranMasuk, iconFotoPulang, iconLampiranPulang;
    private ImageView ivFotoMasukFinal, ivLampiranMasukFinal, ivFotoPulangFinal, ivLampiranPulangFinal;

    private View llPdfMasuk, llPdfPulang;
    private TextView tvPdfMasukName, tvPdfPulangName;

    private List<OpdModel> listOpd = new ArrayList<>();
    private List<Employee> listPegawai = new ArrayList<>();

    private OpdModel selectedOpd = null;
    private Employee selectedEmployee = null;

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
        go.pemkott.appsandroidmobiletebingtinggi.utils.WindowUtil.applyDashboardStyle(this);
        setContentView(R.layout.activity_admin_pengolahan_absensi);

        ambilFoto = new AmbilFoto(AdminPengolahanAbsensiActivity.this);
        dialogView = new DialogView(this);
        ImageView ivBack = findViewById(R.id.ivAdminPengolahanBack);

        tvAdminOpdSelect = findViewById(R.id.tvAdminOpdSelect);
        tvAdminPegawaiSelect = findViewById(R.id.tvAdminPegawaiSelect);
        MaterialCardView cardSelectOpd = findViewById(R.id.cardSelectOpd);
        MaterialCardView cardSelectPegawai = findViewById(R.id.cardSelectPegawai);

        etTanggal = findViewById(R.id.etAdminPengolahanTanggal);
        MaterialButton btnFindPresence = findViewById(R.id.btnAdminFindPresence);

        etAbsensiId = findViewById(R.id.etAdminPengolahanId);
        spPosisiMasuk = findViewById(R.id.spAdminPengolahanPosisiMasuk);
        etJamMasuk = findViewById(R.id.etAdminPengolahanJamMasuk);
        etKeterlambatan = findViewById(R.id.etAdminPengolahanKeterlambatan);
        etKetMasuk = findViewById(R.id.etAdminPengolahanKetMasuk);

        spPosisiPulang = findViewById(R.id.spAdminPengolahanPosisiPulang);
        etJamPulang = findViewById(R.id.etAdminPengolahanJamPulang);
        cbClearPulang = findViewById(R.id.cbAdminClearPulang);

        cbClearPulang.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etJamPulang.setEnabled(!isChecked);
            if (isChecked) {
                etJamPulang.setText("");
            }
        });

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

        ivBack.setOnClickListener(v -> navigateBack());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateBack();
            }
        });

        // Setup Spinners Posisi
        ArrayAdapter<String> adapterMasuk = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, POSISI_TEXTS_MASUK);
        adapterMasuk.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPosisiMasuk.setAdapter(adapterMasuk);

        ArrayAdapter<String> adapterPulang = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, POSISI_TEXTS_PULANG);
        adapterPulang.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPosisiPulang.setAdapter(adapterPulang);

        // Date Picker & initial blank state until record is loaded
        etTanggal.setText(dateFormat.format(calendar.getTime()));
        etTanggal.setOnClickListener(v -> showDatePicker());

        etJamMasuk.setText("");
        etKeterlambatan.setText("");
        etKetMasuk.setText("");
        etJamPulang.setText("");

        // Load OPD List
        loadOpdList();

        cardSelectOpd.setOnClickListener(v -> {
            if (listOpd == null || listOpd.isEmpty()) {
                loadOpdListAndShowDialog();
            } else {
                showOpdSearchDialog();
            }
        });

        cardSelectPegawai.setOnClickListener(v -> {
            if (selectedOpd == null) {
                dialogView.viewNotifKosong(this, "Peringatan", "Silakan pilih OPD terlebih dahulu.");
            } else if (listPegawai == null || listPegawai.isEmpty()) {
                loadPegawaiList(selectedOpd.getId());
            } else {
                showPegawaiSearchDialog();
            }
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

    private void showOpdSearchDialog() {
        Dialog dialog = new Dialog(this, R.style.DialogStyle);
        dialog.setContentView(R.layout.dialog_search_selection);

        TextView tvTitle = dialog.findViewById(R.id.tvSearchDialogTitle);
        ImageView ivClose = dialog.findViewById(R.id.ivCloseSearchDialog);
        EditText etSearch = dialog.findViewById(R.id.etSearchDialogInput);
        ListView lvItems = dialog.findViewById(R.id.lvSearchDialogItems);

        tvTitle.setText("Pilih OPD / Unit Kerja");
        etSearch.setHint("Ketik nama OPD...");

        List<String> opdDisplayList = new ArrayList<>();
        for (OpdModel opd : listOpd) {
            opdDisplayList.add(opd.getOpd() != null ? opd.getOpd() : opd.toString());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, opdDisplayList);
        lvItems.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        lvItems.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = adapter.getItem(position);
            for (OpdModel opd : listOpd) {
                if ((opd.getOpd() != null && opd.getOpd().equals(selectedName)) || opd.toString().equals(selectedName)) {
                    selectedOpd = opd;
                    tvAdminOpdSelect.setText(selectedName);
                    selectedEmployee = null;
                    tvAdminPegawaiSelect.setText("Klik untuk mencari pegawai...");
                    loadPegawaiList(opd.getId());
                    break;
                }
            }
            dialog.dismiss();
        });

        ivClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showPegawaiSearchDialog() {
        Dialog dialog = new Dialog(this, R.style.DialogStyle);
        dialog.setContentView(R.layout.dialog_search_selection);

        TextView tvTitle = dialog.findViewById(R.id.tvSearchDialogTitle);
        ImageView ivClose = dialog.findViewById(R.id.ivCloseSearchDialog);
        EditText etSearch = dialog.findViewById(R.id.etSearchDialogInput);
        ListView lvItems = dialog.findViewById(R.id.lvSearchDialogItems);

        tvTitle.setText("Pilih Pegawai");
        etSearch.setHint("Ketik nama atau NIP pegawai...");

        List<String> empDisplayList = new ArrayList<>();
        for (Employee emp : listPegawai) {
            String label = emp.getNama();
            if (emp.getNip() != null && !emp.getNip().isEmpty()) {
                label += " - " + emp.getNip();
            }
            empDisplayList.add(label);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, empDisplayList);
        lvItems.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        lvItems.setOnItemClickListener((parent, view, position, id) -> {
            String selectedLabel = adapter.getItem(position);
            for (Employee emp : listPegawai) {
                String label = emp.getNama();
                if (emp.getNip() != null && !emp.getNip().isEmpty()) {
                    label += " - " + emp.getNip();
                }
                if (label.equals(selectedLabel) || emp.getNama().equals(selectedLabel)) {
                    selectedEmployee = emp;
                    tvAdminPegawaiSelect.setText(selectedLabel);
                    findPresenceForSelectedEmployeeSilently();
                    break;
                }
            }
            dialog.dismiss();
        });

        ivClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
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
            findPresenceForSelectedEmployeeSilently();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadOpdListAndShowDialog() {
        Dialog dialogLoading = new Dialog(this, R.style.DialogStyle);
        dialogLoading.setContentView(R.layout.view_proses);
        dialogLoading.setCancelable(false);
        dialogLoading.show();

        HttpService api = RetroClient.getInstance(this).getApi2();
        api.getAdminOpdList().enqueue(new Callback<AdminOpdListResponse>() {
            @Override
            public void onResponse(@NonNull Call<AdminOpdListResponse> call, @NonNull Response<AdminOpdListResponse> response) {
                if (!isFinishing()) dialogLoading.dismiss();
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    listOpd = response.body().getData();
                    if (listOpd != null && !listOpd.isEmpty()) {
                        showOpdSearchDialog();
                    } else {
                        dialogView.viewNotifKosong(AdminPengolahanAbsensiActivity.this, "Data OPD Kosong", "Tidak ada data OPD pada server.");
                    }
                } else {
                    dialogView.viewNotifKosong(AdminPengolahanAbsensiActivity.this, "Gagal Memuat OPD", "Server mengembalikan HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<AdminOpdListResponse> call, @NonNull Throwable t) {
                if (!isFinishing()) dialogLoading.dismiss();
                Log.e("PENGOLAHAN_ADMIN", "Error loadOpdList", t);
                dialogView.viewNotifKosong(AdminPengolahanAbsensiActivity.this, "Koneksi Server Gagal", "Gagal memuat OPD dari server:\n" + t.getMessage());
            }
        });
    }

    private void loadOpdList() {
        Dialog dialogLoading = new Dialog(this, R.style.DialogStyle);
        dialogLoading.setContentView(R.layout.view_proses);
        dialogLoading.setCancelable(false);
        dialogLoading.show();

        HttpService api = RetroClient.getInstance(this).getApi2();
        api.getAdminOpdList().enqueue(new Callback<AdminOpdListResponse>() {
            @Override
            public void onResponse(@NonNull Call<AdminOpdListResponse> call, @NonNull Response<AdminOpdListResponse> response) {
                dialogLoading.dismiss();
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    listOpd = response.body().getData();
                    if (listOpd != null && !listOpd.isEmpty()) {
                        selectedOpd = listOpd.get(0);
                        tvAdminOpdSelect.setText(selectedOpd.getOpd() != null ? selectedOpd.getOpd() : selectedOpd.toString());
                        loadPegawaiList(selectedOpd.getId());
                    } else {
                        dialogView.viewNotifKosong(AdminPengolahanAbsensiActivity.this, "Data OPD Kosong", "Tidak ada data OPD pada server.");
                    }
                } else {
                    dialogView.viewNotifKosong(AdminPengolahanAbsensiActivity.this, "Gagal Memuat OPD", "Server mengembalikan HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<AdminOpdListResponse> call, @NonNull Throwable t) {
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
            public void onResponse(@NonNull Call<AdminEmployeeListResponse> call, @NonNull Response<AdminEmployeeListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    listPegawai = response.body().getData();
                    if (listPegawai != null && !listPegawai.isEmpty()) {
                        selectedEmployee = listPegawai.get(0);
                        String label = selectedEmployee.getNama();
                        if (selectedEmployee.getNip() != null && !selectedEmployee.getNip().isEmpty()) {
                            label += " - " + selectedEmployee.getNip();
                        }
                        tvAdminPegawaiSelect.setText(label);
                        findPresenceForSelectedEmployeeSilently();
                    } else {
                        selectedEmployee = null;
                        tvAdminPegawaiSelect.setText("Tidak ada pegawai di OPD ini");
                        bindPresensiToUI(null);
                    }
                } else {
                    Log.e("PENGOLAHAN_ADMIN", "loadPegawaiList failed: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<AdminEmployeeListResponse> call, @NonNull Throwable t) {
                Log.e("PENGOLAHAN_ADMIN", "Error loadPegawaiList", t);
            }
        });
    }

    private void findPresenceForSelectedEmployee() {
        if (selectedEmployee == null) {
            dialogView.viewNotifKosong(this, "Peringatan", "Silakan pilih pegawai terlebih dahulu.");
            return;
        }

        String employeeId = selectedEmployee.getId();
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
            public void onResponse(@NonNull Call<AdminPresenceFindResponse> call, @NonNull Response<AdminPresenceFindResponse> response) {
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
            public void onFailure(@NonNull Call<AdminPresenceFindResponse> call, @NonNull Throwable t) {
                dialogProses.dismiss();
                bindPresensiToUI(null);
                dialogView.viewNotifKosong(AdminPengolahanAbsensiActivity.this, "Koneksi Gagal", t.getMessage());
            }
        });
    }

    private void findPresenceForSelectedEmployeeSilently() {
        if (selectedEmployee == null) return;
        String employeeId = selectedEmployee.getId();
        String tanggal = etTanggal.getText().toString().trim();

        if (employeeId == null || employeeId.isEmpty() || tanggal.isEmpty()) return;

        HttpService api = RetroClient.getInstance(this).getApi2();
        api.findPresenceAdmin(employeeId, tanggal).enqueue(new Callback<AdminPresenceFindResponse>() {
            @Override
            public void onResponse(@NonNull Call<AdminPresenceFindResponse> call, @NonNull Response<AdminPresenceFindResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Presensi p = response.body().getData();
                    bindPresensiToUI(p);
                } else {
                    bindPresensiToUI(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AdminPresenceFindResponse> call, @NonNull Throwable t) {
                bindPresensiToUI(null);
            }
        });
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s.trim();
    }

    private void bindPresensiToUI(Presensi p) {
        currentLoadedPresence = p;
        loadedPosMasukIdx = 0;
        loadedPosPulangIdx = 0;

        if (cbClearPulang != null) {
            cbClearPulang.setChecked(false);
        }
        etJamPulang.setEnabled(true);

        if (p == null) {
            etAbsensiId.setText("");
            etJamMasuk.setText("");
            etKeterlambatan.setText("");
            etKetMasuk.setText("");
            etJamPulang.setText("");
            spPosisiMasuk.setSelection(0);
            spPosisiPulang.setSelection(0);
            resetMediaViews();
            return;
        }

        etAbsensiId.setText(p.getId() != null ? p.getId() : "");
        etJamMasuk.setText(p.getJam_masuk() != null ? p.getJam_masuk() : "");
        etKeterlambatan.setText(p.getKeterlambatan() != null ? p.getKeterlambatan() : "");
        etKetMasuk.setText(p.getKet_masuk() != null ? p.getKet_masuk() : "");
        etJamPulang.setText(p.getJam_pulang() != null ? p.getJam_pulang() : "");

        if (p.getPosisi_masuk() != null) {
            for (int i = 0; i < POSISI_VALUES_MASUK.length; i++) {
                if (POSISI_VALUES_MASUK[i].equalsIgnoreCase(p.getPosisi_masuk())) {
                    spPosisiMasuk.setSelection(i);
                    loadedPosMasukIdx = i;
                    break;
                }
            }
        } else {
            spPosisiMasuk.setSelection(0);
        }

        if (p.getPosisi_pulang() != null) {
            for (int i = 0; i < POSISI_VALUES_PULANG.length; i++) {
                if (POSISI_VALUES_PULANG[i].equalsIgnoreCase(p.getPosisi_pulang())) {
                    spPosisiPulang.setSelection(i);
                    loadedPosPulangIdx = i;
                    break;
                }
            }
        } else {
            spPosisiPulang.setSelection(0);
        }

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

    private RequestBody prepareOptionalTextPart(String value) {
        if (value == null) return null;
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }

    private void submitPengolahan() {
        String idAbsensi = etAbsensiId.getText().toString().trim();
        String jamMasukRaw = etJamMasuk.getText().toString().trim();
        String keterlambatanRaw = etKeterlambatan.getText().toString().trim();
        String ketMasukRaw = etKetMasuk.getText().toString().trim();
        boolean clearPulang = cbClearPulang != null && cbClearPulang.isChecked();
        String jamPulangRaw = clearPulang ? "" : etJamPulang.getText().toString().trim();

        if (idAbsensi.isEmpty()) {
            dialogView.viewNotifKosong(this, "Peringatan", "ID Absensi wajib diisi (cari presensi terlebih dahulu).");
            return;
        }

        if (!jamMasukRaw.isEmpty() && !AdminFormHelper.isValidTime(jamMasukRaw, false)) {
            dialogView.viewNotifKosong(this, "Format Jam Tidak Valid", "Format Jam Masuk harus HH:mm atau HH:mm:ss.");
            return;
        }

        if (!clearPulang && !jamPulangRaw.isEmpty() && !AdminFormHelper.isValidTime(jamPulangRaw, false)) {
            dialogView.viewNotifKosong(this, "Format Jam Tidak Valid", "Format Jam Pulang harus HH:mm atau HH:mm:ss.");
            return;
        }

        Integer keterlambatanVal = null;
        if (!keterlambatanRaw.isEmpty()) {
            keterlambatanVal = AdminFormHelper.parseNonNegativeInt(keterlambatanRaw);
            if (keterlambatanVal == null) {
                dialogView.viewNotifKosong(this, "Peringatan", "Keterlambatan harus berupa bilangan bulat >= 0.");
                return;
            }
        }

        int idxM = Math.max(0, spPosisiMasuk.getSelectedItemPosition());
        String posisiMasuk = POSISI_VALUES_MASUK[idxM];
        String statusMasuk = STATUS_VALUES_MASUK[idxM];
        String validasiMasuk = "2";

        int idxP = Math.max(0, spPosisiPulang.getSelectedItemPosition());
        String posisiPulang = POSISI_VALUES_PULANG[idxP];
        String statusPulang = STATUS_VALUES_PULANG[idxP];
        String validasiPulang = "2";

        boolean includeMasuk;
        boolean includePulang;
        if (currentLoadedPresence != null && idAbsensi.equals(nullToEmpty(currentLoadedPresence.getId()))) {
            boolean masukChanged = !jamMasukRaw.equals(nullToEmpty(currentLoadedPresence.getJam_masuk()))
                    || !keterlambatanRaw.equals(nullToEmpty(currentLoadedPresence.getKeterlambatan()))
                    || !ketMasukRaw.equals(nullToEmpty(currentLoadedPresence.getKet_masuk()))
                    || idxM != loadedPosMasukIdx
                    || fileFotoMasuk != null
                    || fileLampiranMasuk != null;
            boolean pulangChanged = clearPulang
                    || !jamPulangRaw.equals(nullToEmpty(currentLoadedPresence.getJam_pulang()))
                    || idxP != loadedPosPulangIdx
                    || fileFotoPulang != null
                    || fileLampiranPulang != null;
            includeMasuk = masukChanged;
            includePulang = pulangChanged;
        } else {
            includeMasuk = !jamMasukRaw.isEmpty()
                    || keterlambatanVal != null
                    || !ketMasukRaw.isEmpty()
                    || fileFotoMasuk != null
                    || fileLampiranMasuk != null;
            includePulang = clearPulang
                    || !jamPulangRaw.isEmpty()
                    || fileFotoPulang != null
                    || fileLampiranPulang != null;
        }

        if (!includeMasuk && !includePulang) {
            dialogView.viewNotifKosong(this, "Peringatan", "Isi atau ubah minimal satu data Masuk atau Pulang sebelum menyimpan.");
            return;
        }

        String sendJamMasuk = (includeMasuk && !jamMasukRaw.isEmpty()) ? jamMasukRaw : null;
        Integer sendKeterlambatan = includeMasuk ? keterlambatanVal : null;
        String sendPosMasuk = includeMasuk ? posisiMasuk : null;
        String sendStatMasuk = includeMasuk ? statusMasuk : null;
        String sendValMasuk = includeMasuk ? validasiMasuk : null;
        String sendKetMasuk = (includeMasuk && !ketMasukRaw.isEmpty()) ? ketMasukRaw : null;

        String sendJamPulang = (!clearPulang && includePulang && !jamPulangRaw.isEmpty()) ? jamPulangRaw : null;
        String sendPosPulang = (!clearPulang && includePulang) ? posisiPulang : null;
        String sendStatPulang = (!clearPulang && includePulang) ? statusPulang : null;
        String sendValPulang = (!clearPulang && includePulang) ? validasiPulang : null;
        String sendClearPulang = clearPulang ? "1" : null;

        Dialog dialogProses = new Dialog(this, R.style.DialogStyle);
        dialogProses.setContentView(R.layout.view_proses);
        dialogProses.setCancelable(false);
        dialogProses.show();

        HttpService api = RetroClient.getInstance(this).getApi2();

        if (fileFotoMasuk == null && fileFotoPulang == null && fileLampiranMasuk == null && fileLampiranPulang == null) {
            api.storePengolahanAbsensiAdmin(
                    idAbsensi,
                    sendJamMasuk,
                    sendKeterlambatan,
                    sendPosMasuk,
                    sendStatMasuk,
                    sendValMasuk,
                    sendKetMasuk,
                    sendJamPulang,
                    sendPosPulang,
                    sendStatPulang,
                    sendValPulang,
                    sendClearPulang
            ).enqueue(new Callback<ResponsePOJO>() {
                @Override
                public void onResponse(@NonNull Call<ResponsePOJO> call, @NonNull Response<ResponsePOJO> response) {
                    dialogProses.dismiss();
                    handleResponse(response);
                }

                @Override
                public void onFailure(@NonNull Call<ResponsePOJO> call, @NonNull Throwable t) {
                    dialogProses.dismiss();
                    dialogView.viewNotifKosong(AdminPengolahanAbsensiActivity.this, "Koneksi Gagal", t.getMessage());
                }
            });
        } else {
            RequestBody bodyId = RequestBody.create(MediaType.parse("text/plain"), idAbsensi);
            RequestBody bodyJamM = prepareOptionalTextPart(sendJamMasuk);
            RequestBody bodyKetL = prepareOptionalTextPart(sendKeterlambatan != null ? String.valueOf(sendKeterlambatan) : null);
            RequestBody bodyPosM = prepareOptionalTextPart(sendPosMasuk);
            RequestBody bodyStatM = prepareOptionalTextPart(sendStatMasuk);
            RequestBody bodyValM = prepareOptionalTextPart(sendValMasuk);
            RequestBody bodyKetM = prepareOptionalTextPart(sendKetMasuk);

            RequestBody bodyJamP = prepareOptionalTextPart(sendJamPulang);
            RequestBody bodyPosP = prepareOptionalTextPart(sendPosPulang);
            RequestBody bodyStatP = prepareOptionalTextPart(sendStatPulang);
            RequestBody bodyValP = prepareOptionalTextPart(sendValPulang);
            RequestBody bodyClearP = prepareOptionalTextPart(sendClearPulang);

            MultipartBody.Part partFotoM = prepareFilePart("fototaging_masuk", fileFotoMasuk, false);
            MultipartBody.Part partFotoP = prepareFilePart("fototaging_pulang", fileFotoPulang, false);
            MultipartBody.Part partLampM = prepareFilePart("lampiran_masuk", fileLampiranMasuk, fileLampiranMasuk != null && fileLampiranMasuk.getName().endsWith(".pdf"));
            MultipartBody.Part partLampP = prepareFilePart("lampiran_pulang", fileLampiranPulang, fileLampiranPulang != null && fileLampiranPulang.getName().endsWith(".pdf"));

            api.storePengolahanAbsensiAdminMultipart(
                    bodyId, bodyJamM, bodyKetL, bodyPosM, bodyStatM, bodyValM, bodyKetM,
                    bodyJamP, bodyPosP, bodyStatP, bodyValP, bodyClearP,
                    partFotoM, partFotoP, partLampM, partLampP
            ).enqueue(new Callback<ResponsePOJO>() {
                @Override
                public void onResponse(@NonNull Call<ResponsePOJO> call, @NonNull Response<ResponsePOJO> response) {
                    dialogProses.dismiss();
                    handleResponse(response);
                }

                @Override
                public void onFailure(@NonNull Call<ResponsePOJO> call, @NonNull Throwable t) {
                    dialogProses.dismiss();
                    dialogView.viewNotifKosong(AdminPengolahanAbsensiActivity.this, "Koneksi Gagal", t.getMessage());
                }
            });
        }
    }

    private MultipartBody.Part prepareFilePart(String partName, File file, boolean isPdf) {
        if (file == null || !file.exists()) return null;
        try {
            String mimeType = isPdf ? "application/pdf" : "image/jpeg";
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
                findPresenceForSelectedEmployeeSilently();
                dialogView.viewSuksesNonFinish(AdminPengolahanAbsensiActivity.this, pojo.getRemarks() != null ? pojo.getRemarks() : "Data pengolahan absensi berhasil diubah!");
            } else {
                dialogView.viewNotifKosong(AdminPengolahanAbsensiActivity.this, "Gagal Ubah", pojo.getRemarks() != null ? pojo.getRemarks() : "Gagal mengubah data absensi.");
            }
        } else {
            String err = AdminFormHelper.extractApiError(response, "HTTP " + response.code());
            dialogView.viewNotifKosong(AdminPengolahanAbsensiActivity.this, "Gagal Server", err);
        }
    }

    private void navigateBack() {
        if (isTaskRoot()) {
            Intent intent = new Intent(this, go.pemkott.appsandroidmobiletebingtinggi.NewDashboard.DashboardVersiOne.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
        finish();
    }
}
