package go.pemkott.appsandroidmobiletebingtinggi.admin;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
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
import go.pemkott.appsandroidmobiletebingtinggi.utils.FileUtil;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUpdateMasukActivity extends AppCompatActivity {

    private EditText etTanggal, etJamMasuk, etKeterlambatan, etKetMasuk, etPegawaiId;
    private Spinner spPosisiMasuk;

    private ImageView iconFotoMasuk, iconLampiranMasuk;
    private ImageView ivFotoMasukFinal, ivLampiranMasukFinal;
    private View llPdfMasuk;
    private TextView tvPdfMasukName;

    private Calendar calendar = Calendar.getInstance();
    private DialogView dialogView;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private File fileFotoMasuk = null;
    private File fileLampiranMasuk = null;
    private AmbilFoto ambilFoto;

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

    private ActivityResultLauncher<Intent> cameraFotoMasukLauncher;
    private ActivityResultLauncher<Intent> cameraLampiranMasukLauncher;
    private ActivityResultLauncher<Intent> pdfMasukLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_update_masuk);

        ambilFoto = new AmbilFoto(this);
        dialogView = new DialogView(this);

        ImageView ivBack = findViewById(R.id.ivAdminBack);
        spPosisiMasuk = findViewById(R.id.spAdminPosisiMasuk);
        etTanggal = findViewById(R.id.etAdminTanggal);
        etJamMasuk = findViewById(R.id.etAdminJamMasuk);
        etKeterlambatan = findViewById(R.id.etAdminKeterlambatan);
        etKetMasuk = findViewById(R.id.etAdminKetMasuk);
        etPegawaiId = findViewById(R.id.etAdminPegawaiId);

        MaterialCardView cardFotoMasuk = findViewById(R.id.cardPickFotoMasuk);
        MaterialCardView cardLampiranMasuk = findViewById(R.id.cardPickLampiranMasuk);

        iconFotoMasuk = findViewById(R.id.iconFotoMasuk);
        ivFotoMasukFinal = findViewById(R.id.ivFotoMasukFinal);
        iconLampiranMasuk = findViewById(R.id.iconLampiranMasuk);
        ivLampiranMasukFinal = findViewById(R.id.ivLampiranMasukFinal);
        llPdfMasuk = findViewById(R.id.llPdfMasuk);
        tvPdfMasukName = findViewById(R.id.tvPdfMasukName);

        MaterialButton btnSubmit = findViewById(R.id.btnAdminSubmitMasuk);

        ivBack.setOnClickListener(v -> finish());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        // Spinner Posisi
        ArrayAdapter<String> adapterPosisi = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, POSISI_TEXTS);
        adapterPosisi.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPosisiMasuk.setAdapter(adapterPosisi);

        // Defaults
        etTanggal.setText(dateFormat.format(calendar.getTime()));
        etJamMasuk.setText("07:30");
        etKeterlambatan.setText("0");
        etKetMasuk.setText("sesuai waktu");

        etTanggal.setOnClickListener(v -> showDatePicker());
        etJamMasuk.setOnClickListener(v -> showTimePicker());

        setupMediaLaunchers();

        cardFotoMasuk.setOnClickListener(v -> viewLampiran(1));
        cardLampiranMasuk.setOnClickListener(v -> viewLampiran(2));

        btnSubmit.setOnClickListener(v -> submitUpdateMasuk());
    }

    public void viewLampiran(int targetType) {
        Dialog dialogLampiran = new Dialog(AdminUpdateMasukActivity.this, R.style.DialogStyle);
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
            pdfMasukLauncher.launch(intent);
            dialogLampiran.dismiss();
        });

        llKamera.setOnClickListener(v -> {
            Intent intent = new Intent(this, CameraXTanpaDetectionAcitvity.class);
            intent.putExtra("is_lampiran", true);
            if (targetType == 1) {
                cameraFotoMasukLauncher.launch(intent);
            } else {
                cameraLampiranMasukLauncher.launch(intent);
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
                            Log.e("UPDATE_MASUK_ADMIN", "Error pdfMasukLauncher", e);
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

                if (targetType == 1) {
                    fileFotoMasuk = targetFile;
                    ivFotoMasukFinal.setImageBitmap(previewBitmap);
                    ivFotoMasukFinal.setVisibility(View.VISIBLE);
                    iconFotoMasuk.setVisibility(View.GONE);
                } else {
                    fileLampiranMasuk = targetFile;
                    ivLampiranMasukFinal.setImageBitmap(previewBitmap);
                    ivLampiranMasukFinal.setVisibility(View.VISIBLE);
                    iconLampiranMasuk.setVisibility(View.GONE);
                    llPdfMasuk.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            int targetType = requestCode - 100;
            try {
                Uri selectedImageUri = data.getData();
                String FilePath2 = FileUtil.getDriveFilePath(selectedImageUri, this);
                File originalLampiran = new File(FilePath2);
                File filelampiran = ambilFoto.compressToFile(this, originalLampiran);

                BitmapFactory.Options optsLampiran = new BitmapFactory.Options();
                optsLampiran.inSampleSize = 4;
                optsLampiran.inPreferredConfig = Bitmap.Config.RGB_565;
                Bitmap preview = BitmapFactory.decodeFile(filelampiran.getAbsolutePath(), optsLampiran);

                if (targetType == 1) {
                    fileFotoMasuk = filelampiran;
                    ivFotoMasukFinal.setImageBitmap(preview);
                    ivFotoMasukFinal.setVisibility(View.VISIBLE);
                    iconFotoMasuk.setVisibility(View.GONE);
                } else {
                    fileLampiranMasuk = filelampiran;
                    ivLampiranMasukFinal.setImageBitmap(preview);
                    ivLampiranMasukFinal.setVisibility(View.VISIBLE);
                    iconLampiranMasuk.setVisibility(View.GONE);
                    llPdfMasuk.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                Log.e("UPDATE_MASUK_ADMIN", "Error onActivityResult gallery image", e);
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
            Log.e("UPDATE_MASUK_ADMIN", "Error getPDFPath", ae);
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
                Log.e("UPDATE_MASUK_ADMIN", "Error getting file name", e);
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
        String validasiMasuk = "2";

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

        if (fileFotoMasuk == null && fileLampiranMasuk == null) {
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

            MultipartBody.Part partFoto = prepareFilePart("fototaging_masuk", fileFotoMasuk, false);
            MultipartBody.Part partLampiran = prepareFilePart("lampiran_masuk", fileLampiranMasuk, fileLampiranMasuk != null && fileLampiranMasuk.getName().endsWith(".pdf"));

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

    private MultipartBody.Part prepareFilePart(String partName, File file, boolean isPdf) {
        if (file == null || !file.exists()) return null;
        try {
            String mimeType = isPdf ? "application/pdf" : "image/*";
            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), file);
            return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
        } catch (Exception e) {
            Log.e("UPDATE_MASUK_ADMIN", "Error prepareFilePart", e);
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
