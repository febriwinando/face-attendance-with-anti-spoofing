package go.pemkott.appsandroidmobiletebingtinggi.dinasluarkantor.tugaslapangan;

import static go.pemkott.appsandroidmobiletebingtinggi.geolocation.model.LocationHelper.defaultLocation;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.SIMPLE_DATE_FORMAT_TAGING;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.SIMPLE_DATE_FORMAT_TAGING_PHOTO_REPORT;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.SIMPLE_FORMAT_JAM;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.SIMPLE_FORMAT_JAM_TAGING;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.SIMPLE_FORMAT_TANGGAL;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.hari;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.localeID;
import static go.pemkott.appsandroidmobiletebingtinggi.utils.FileUtil.getDriveFilePath;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.FragmentContainerView;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.api.ResponsePOJO;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.kehadiransift.JadwalShiftActivity;
import go.pemkott.appsandroidmobiletebingtinggi.utils.MapUtils;
import go.pemkott.appsandroidmobiletebingtinggi.camerax.CameraXTanpaDetectionAcitvity;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.dialogview.DialogView;
import go.pemkott.appsandroidmobiletebingtinggi.geolocation.GetLocation;
import go.pemkott.appsandroidmobiletebingtinggi.konstanta.AmbilFoto;
import go.pemkott.appsandroidmobiletebingtinggi.konstanta.AmbilFotoLampiran;
import go.pemkott.appsandroidmobiletebingtinggi.konstanta.Lokasi;
import go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager;
import go.pemkott.appsandroidmobiletebingtinggi.utils.NetworkUtils;
import go.pemkott.appsandroidmobiletebingtinggi.model.LocationViewModel;
import go.pemkott.appsandroidmobiletebingtinggi.utils.WeatherUtil;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TugasLapanganShiftFinalActivity extends AppCompatActivity implements OnMapReadyCallback {

    //Gmaps
    private static final int REQUEST_CHECK_SETTINGS = 100;
    private Context mContext;
    private LocationViewModel locationViewModel;
    private GoogleMap map;
    private ValueAnimator rippleAnimator;
    double latGMap = 0, lngGMap = 0;
    double latitudeSaya = 3.327972364475644;
    double longitudeSaya = 99.16647248312584;
    Lokasi lokasi = new Lokasi();
    //Gmaps

    DatabaseHelper databaseHelper;
    ActivityResultLauncher<Intent> resultLauncher;
    private static final String TAG = TugasLapanganShiftFinalActivity.class.getSimpleName();
    File imageFile;
    private String currentPhotoPath;
    private String rbLat;
    private String rbLng;
    private String rbTanggal;
    private String rbJam;
    private String rbKet;
    private String rbFakeGPS ="0" ;
    private TextView tvAkurasi, tvJarak, tvTemperature, tvCondition;
    private ImageView ivWeatherIcon;
    private View cardSafeZone;
    private long lastWeatherUpdate = 0;
    String currentDateandTimes = SIMPLE_DATE_FORMAT_TAGING_PHOTO_REPORT.format(new Date());
    String eOPD, eJabatan;
    String sEmployeID, timetableid;
    String kegiatanlainnya, jamMasuk, jamPulang;
    String tanggal;
    StringBuilder keterangan;
    String batasWaktu, statushift;

    String ekslampiran;

    TextView tvKegiatanFinal, tvSuratPerintah, titleDinasLuar;
    MaterialCardView llPdfDinasLuar;
    View llLampiranDinasLuar;
    ArrayList<String> kegiatans = new ArrayList<>();
    AmbilFoto ambilFoto = new AmbilFoto(TugasLapanganShiftFinalActivity.this);

    ShapeableImageView ivFinalKegiatan, ivSuratPerintahFinal;
    ImageView iconLampiran;
    int mins, minspulang;
    DialogView dialogView = new DialogView(this);

    RadioGroup rgKehadiran;
    RadioButton radioSelectedKehadiran;
    View llUpload;
    int selected;

    Date jamMasukDate, jamPulangDate;
    Calendar cal = Calendar.getInstance();
    Date dateBatasWaktu;
    private boolean mockLocationsEnabled = false;
    int mock_location = 0;
    FragmentContainerView fragmentContainerView;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    File file, filelampiran;
    SessionManager session;
    String userId;
    private Uri capturedImageUri;

    // Shift specific
    String idshift, inisialshift, tipeshift;

    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        WindowInsetsControllerCompat controller =
                new WindowInsetsControllerCompat(
                        getWindow(),
                        getWindow().getDecorView()
                );

        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);

        setContentView(R.layout.activity_tugas_lapangan_one);

        session = new SessionManager(this);
        userId = session.getPegawaiId();

        databaseHelper = new DatabaseHelper(this);
        tvKegiatanFinal = findViewById(R.id.tvKegiatanFinal);
        tvSuratPerintah = findViewById(R.id.tvSuratPerintah);
        titleDinasLuar = findViewById(R.id.titleDinasLuar);
        rgKehadiran = findViewById(R.id.rgKehadiran);
        llUpload = findViewById(R.id.llUpload);

        fragmentContainerView = findViewById(R.id.map);
        setRoundedBackground(fragmentContainerView);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(5000)
                .build();

        ivFinalKegiatan = findViewById(R.id.ivFinalKegiatanTl);
        ivSuratPerintahFinal = findViewById(R.id.ivSuratPerintahFinalTl);
        iconLampiran = findViewById(R.id.iconLampiran);
        llPdfDinasLuar = findViewById(R.id.llPdfDinasLuar);
        llLampiranDinasLuar = findViewById(R.id.llLampiranDinasLuarOne);

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        tvAkurasi = findViewById(R.id.tvAkurasi);
        tvJarak = findViewById(R.id.tvJarak);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvCondition = findViewById(R.id.tvCondition);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);
        cardSafeZone = findViewById(R.id.cardSafeZone);

        mContext = this;
        setupViews();
        setupViewModel();
        checkLocationPermission();

        datauser();

        Intent intent = getIntent();
        titleDinasLuar.setText(intent.getStringExtra("title"));

        kegiatans = intent.getStringArrayListExtra("kegiatan_checked");
        if (kegiatans == null) kegiatans = new ArrayList<>();
        kegiatanlainnya = intent.getStringExtra("kegiatans_lainnya");

        // Shift info from Intent
        rbTanggal = intent.getStringExtra("tanggalShift");
        idshift = intent.getStringExtra("idShift");
        inisialshift = intent.getStringExtra("inisialShift");
        tipeshift = intent.getStringExtra("tipeShift");
        jamMasuk = intent.getStringExtra("masukShift");
        jamPulang = intent.getStringExtra("pulangShift");

        String uriString = intent.getStringExtra("namafile");

        if (uriString == null) {
            Toast.makeText(this, "Foto tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        capturedImageUri = Uri.parse(uriString);
        try {
            File originalFile = createTempFileFromUri(capturedImageUri);
            file = ambilFoto.compressToFile(this, originalFile);
            if (file == null || !file.exists()) {
                Toast.makeText(this, "Gagal memproses foto", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 4;
            opts.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap preview = BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
            if (preview != null) {
                ivFinalKegiatan.setImageBitmap(preview);
            }
            if (originalFile.exists()) {
                originalFile.delete();
            }
        } catch (Exception e) {
            Log.e("FOTO_ERROR", "Gagal memproses foto", e);
            finish();
            return;
        }

        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @SuppressLint("Range")
            @Override
            public void onActivityResult(ActivityResult result) {
                Intent data = result.getData();
                if (data != null){
                    Uri sUri = data.getData();
                    File file = new File(sUri.getPath());
                    llLampiranDinasLuar.setVisibility(View.GONE);
                    llPdfDinasLuar.setVisibility(View.VISIBLE);
                    String displayName = null;

                    getPDFPath(sUri);

                    if (sUri.toString().startsWith("content://")) {
                        Cursor cursor = null;
                        try {
                            cursor = TugasLapanganShiftFinalActivity.this.getContentResolver().query(sUri, null, null, null, null);
                            if (cursor != null && cursor.moveToFirst()) {
                                displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                                tvSuratPerintah.setText(displayName);
                            }
                        } finally {
                            if (cursor != null) cursor.close();
                        }
                    } else if (sUri.toString().startsWith("file://")) {
                        displayName = file.getName();
                        tvSuratPerintah.setText(displayName);
                    }
                }
            }
        });

        requestPermission();
        dataKegiatan();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    public void fokusLokasiTugasLapangan(View view){
        startLocationUpdates();
    }

    public void kirimDataDinasLuar(View view){
        if (llUpload != null) llUpload.setEnabled(false);
        requestPermission();
        if (mock_location == 1){
            dialogView.viewNotifKosong(TugasLapanganShiftFinalActivity.this, "Anda terdeteksi menggunakan Fake GPS.", "Jika ditemukan berulang kali, akun anda akan terblokir otomatis dan tercatat Alpa.");
            if (llUpload != null) llUpload.setEnabled(true);
        }else{
            if (file == null || !file.exists() || file.length() == 0){
                dialogView.viewNotifKosong(TugasLapanganShiftFinalActivity.this, "Anda harus melampirkan Foto Kegiatan and Surat Perintah Perjalanan Dinas.", "");
                if (llUpload != null) llUpload.setEnabled(true);
            }
            else {
                uploadImages();
            }
        }
    }

    public void uploadImages(){
        if (jamMasuk == null || jamPulang == null ){
            dialogView.viewNotifKosong(TugasLapanganShiftFinalActivity.this, "Anda tidak memiliki Jadwal Kerja untuk hari ini", "");
            if (llUpload != null) llUpload.setEnabled(true);
        }
        else{
            periksaWaktu();
            hitungjarak();

            String rbValid = eJabatan.equals("2") ? "2" : "0";

            selected = rgKehadiran.getCheckedRadioButtonId();
            radioSelectedKehadiran = findViewById(selected);

            String rbStatus = "hadir";

            Date pulangPeriksa = null, tagingTimePeriksa = null;
            try {
                pulangPeriksa = SIMPLE_FORMAT_JAM.parse(jamPulang);
                String jamTagingPeriksa = SIMPLE_FORMAT_JAM_TAGING.format(new Date());
                tagingTimePeriksa = SIMPLE_FORMAT_JAM_TAGING.parse(jamTagingPeriksa);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (tagingTimePeriksa.getTime() < dateBatasWaktu.getTime()){
                dialogView.viewNotifKosong(TugasLapanganShiftFinalActivity.this, "Anda hanya dapat mengisi absen masuk, "+batasWaktu+" menit sebelum Jam Masuk", "");
                if (llUpload != null) llUpload.setEnabled(true);
            }else{
                String rbPosisi = "";
                if (radioSelectedKehadiran.getText().toString().trim().equals("MASUK")){
                    rbPosisi = "tl-masuk";
                    if (tagingTimePeriksa.getTime() >= pulangPeriksa.getTime()){
                        dialogView.viewNotifKosong(TugasLapanganShiftFinalActivity.this, "Anda tidak dapat melakukan absensi masuk pada jam pulang kerja.","");
                        if (llUpload != null) llUpload.setEnabled(true);
                    }else{
                        kirimdataMasuk(rbValid, rbPosisi, rbStatus, "masuk", jamMasuk);
                    }
                } else if (radioSelectedKehadiran.getText().toString().trim().equals("PULANG")) {
                    rbPosisi = "tl-pulang";
                    if (tagingTimePeriksa.getTime() < pulangPeriksa.getTime()){
                        dialogView.viewNotifKosong(TugasLapanganShiftFinalActivity.this, "Belum memasuki jam pulang kerja.", "Anda belum dapat melakukan absensi pulang.");
                        if (llUpload != null) llUpload.setEnabled(true);
                    } else {
                        kirimdataPulang(rbValid, rbPosisi, rbStatus, "pulang", jamPulang);
                    }
                }
            }
        }
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Location location = locationResult.getLastLocation();
            if (location == null) return;

            boolean isMock = mockLocationsEnabled || location.isFromMockProvider();
            if (isMock){
                mock_location = 1;
                rbFakeGPS ="1";
            }else{
                mock_location = 0;
            }
            if (map != null) {
                plotMarkers(location);
            }
            if (tvAkurasi != null) {
                tvAkurasi.setText(String.format(localeID, "± %.0f m", location.getAccuracy()));
            }
            long now = System.currentTimeMillis();
            if (now - lastWeatherUpdate > 10 * 60 * 1000) {
                WeatherUtil.fetchWeather(TugasLapanganShiftFinalActivity.this, location.getLatitude(), location.getLongitude(), tvTemperature, tvCondition, ivWeatherIcon);
                lastWeatherUpdate = now;
            }
        }
    };

    private void setRoundedBackground(FragmentContainerView view) {
        float radius = getResources().getDimension(R.dimen.radius);
        view.setBackgroundResource(R.drawable.backgoundbase);
        view.setClipToOutline(true);
        view.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
            }
        });
    }

    private MultipartBody.Part prepareFilePart(String partName, byte[] imageBytes) {
        RequestBody requestBody = RequestBody.create(imageBytes, MediaType.parse("image/jpeg"));
        return MultipartBody.Part.createFormData(partName, "fototaging.jpg", requestBody);
    }

    private RequestBody textPart(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }

    public void kirimdataMasuk(String valid, String posisi, String status, String ketKehadiran, String jampegawai){
        Dialog dialogproses = new Dialog(TugasLapanganShiftFinalActivity.this, R.style.DialogStyle);
        dialogproses.setContentView(R.layout.view_proses);
        dialogproses.setCancelable(false);

        byte[] imageBytes = ambilFoto.compressToMax80KB(file);
        MultipartBody.Part fotoPart = prepareFilePart("fototaging", imageBytes);

        MultipartBody.Part lampiranPart;
        if ("pdf".equals(ekslampiran)) {
            lampiranPart = prepareFilePart("lampiran", imageBytesDokumenPdf);
        } else {
            byte[] imageBytesLampiran = ambilFoto.compressToMax80KB(filelampiran);
            lampiranPart = prepareFilePart("lampiran", imageBytesLampiran);
        }

        Call<ResponsePOJO> call = RetroClient.getInstance().getApi().uploadTLMasuk(
                fotoPart,
                textPart(ketKehadiran),
                textPart(eJabatan),
                textPart(sEmployeID),
                textPart(timetableid), // Passes shift timetable ID
                textPart(rbTanggal),
                textPart(rbJam),
                textPart(posisi),
                textPart(status),
                textPart(rbLat),
                textPart(rbLng),
                textPart(rbKet),
                textPart(String.valueOf(mins)),
                textPart(eOPD),
                textPart(jampegawai),
                textPart(valid),
                lampiranPart,
                textPart(ekslampiran),
                textPart(rbFakeGPS),
                textPart(batasWaktu),
                textPart("1") // Shift status = 1
        );

        call.enqueue(new Callback<ResponsePOJO>() {
            @Override
            public void onResponse(@NonNull Call<ResponsePOJO> call, @NonNull Response<ResponsePOJO> response) {
                dialogproses.dismiss();
                if (llUpload != null) llUpload.setEnabled(true);
                if (!response.isSuccessful()) {
                    dialogView.viewNotifKosong(TugasLapanganShiftFinalActivity.this, "Gagal mengisi absensi", "Silahkan coba kembali.");
                    return;
                }
                ResponsePOJO data = response.body();
                if (data != null && data.isStatus()){
                    databaseHelper.insertLog(sEmployeID, eOPD, rbTanggal, "Melakukan Absen Tugas Lapangan (Masuk - Shift)", "Masuk");
                    hapusFileSementara();
                    dialogView.viewSukses(TugasLapanganShiftFinalActivity.this, data.getRemarks());
                }else if (data != null) {
                    dialogView.viewNotifKosong(TugasLapanganShiftFinalActivity.this, data.getRemarks(),"");
                }
            }
            @Override
            public void onFailure(@NonNull Call<ResponsePOJO> call, @NonNull Throwable t) {
                dialogproses.dismiss();
                if (llUpload != null) llUpload.setEnabled(true);
                dialogView.pesanError(TugasLapanganShiftFinalActivity.this);
            }
        });
        dialogproses.show();
    }

    public void kirimdataPulang(String valid, String posisi, String status, String ketKehadiran, String jampegawai){
        Dialog dialogproses = new Dialog(TugasLapanganShiftFinalActivity.this, R.style.DialogStyle);
        dialogproses.setContentView(R.layout.view_proses);
        dialogproses.setCancelable(false);

        byte[] imageBytes = ambilFoto.compressToMax80KB(file);
        MultipartBody.Part fotoPart = prepareFilePart("fototaging", imageBytes);

        MultipartBody.Part lampiranPart;
        if ("pdf".equals(ekslampiran)) {
            lampiranPart = prepareFilePart("lampiran", imageBytesDokumenPdf);
        } else {
            byte[] imageBytesLampiran = ambilFoto.compressToMax80KB(filelampiran);
            lampiranPart = prepareFilePart("lampiran", imageBytesLampiran);
        }

        Call<ResponsePOJO> call = RetroClient.getInstance().getApi().uploadTLPulang(
                fotoPart,
                textPart(ketKehadiran),
                textPart(eJabatan),
                textPart(sEmployeID),
                textPart(timetableid),
                textPart(rbTanggal),
                textPart(rbJam),
                textPart(posisi),
                textPart(status),
                textPart(rbLat),
                textPart(rbLng),
                textPart(rbKet),
                textPart(String.valueOf(mins)),
                textPart(eOPD),
                textPart(jampegawai),
                textPart(valid),
                lampiranPart,
                textPart(ekslampiran),
                textPart(rbFakeGPS),
                textPart(batasWaktu),
                textPart("1") // Shift status = 1
        );

        call.enqueue(new Callback<ResponsePOJO>() {
            @Override
            public void onResponse(@NonNull Call<ResponsePOJO> call, @NonNull Response<ResponsePOJO> response) {
                dialogproses.dismiss();
                if (llUpload != null) llUpload.setEnabled(true);
                if (!response.isSuccessful()) {
                    dialogView.viewNotifKosong(TugasLapanganShiftFinalActivity.this, "Gagal mengisi absensi", "Silahkan coba kembali.");
                    return;
                }
                ResponsePOJO data = response.body();
                if (data != null && data.isStatus()){
                    databaseHelper.insertLog(sEmployeID, eOPD, rbTanggal, "Melakukan Absen Tugas Lapangan (Pulang - Shift)", "Pulang");
                    hapusFileSementara();
                    dialogView.viewSukses(TugasLapanganShiftFinalActivity.this, data.getRemarks());
                }else if (data != null) {
                    dialogView.viewNotifKosong(TugasLapanganShiftFinalActivity.this, data.getRemarks(),"");
                }
            }
            @Override
            public void onFailure(@NonNull Call<ResponsePOJO> call, @NonNull Throwable t) {
                dialogproses.dismiss();
                if (llUpload != null) llUpload.setEnabled(true);
                dialogView.pesanError(TugasLapanganShiftFinalActivity.this);
            }
        });
        dialogproses.show();
    }

    private void hapusFileSementara() {
        if (capturedImageUri != null) {
            getContentResolver().delete(capturedImageUri, null, null);
        }
        if (file != null && file.exists()) {
            file.delete();
            file = null;
        }
    }

    private File createTempFileFromUri(Uri uri) throws IOException {
        InputStream is = getContentResolver().openInputStream(uri);
        File tempFile = new File(getCacheDir(), "IMG_" + System.currentTimeMillis() + ".jpg");
        FileOutputStream fos = new FileOutputStream(tempFile);
        byte[] buffer = new byte[8192];
        int len;
        while ((len = is.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
        }
        fos.close();
        is.close();
        return tempFile;
    }

    public void hitungjarak(){
        GetLocation getLocation = new GetLocation(getApplicationContext());
        Location location = getLocation.getLocation();
        if(NetworkUtils.isConnected(this)){
            latitudeSaya = latGMap;
            longitudeSaya = lngGMap;
            rbLat = String.valueOf(latitudeSaya);
            rbLng = String.valueOf(longitudeSaya);
        }else{
            dialogView.viewNotifKosong(TugasLapanganShiftFinalActivity.this, "Pastikan anda telah terhubung internet.", "");
        }
    }

    private void datauser(){
        Cursor res = databaseHelper.getAllData22(userId);
        if (res != null && res.getCount() > 0) {
            while (res.moveToNext()){
                sEmployeID = res.getString(1);
            }
            res.close();
        }

        Cursor resShift = databaseHelper.getJadwalShiftByTanggal(sEmployeID, JadwalShiftActivity.tanggalShift);
        if (resShift != null && resShift.getCount() > 0) {
            while (resShift.moveToNext()) {
                timetableid = resShift.getString(2);
            }
            resShift.close();
        }

        Cursor employe = databaseHelper.getDataEmployee(sEmployeID);
        if (employe != null && employe.moveToNext()){
            eOPD = employe.getString(4);
            eJabatan = employe.getString(11);
            batasWaktu = employe.getString(18);
            statushift = employe.getString(19);
            employe.close();
        }
    }

    public void dataKegiatan(){
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0 ; i < kegiatans.size(); i++){
            if (i == kegiatans.size()-1 ){
                stringBuilder.append(kegiatans.get(i));
            }else{
                stringBuilder.append(kegiatans.get(i)).append(", ");
            }
        }
        if (kegiatanlainnya != null && !kegiatanlainnya.equals("kosong")){
            if (kegiatans.size() == 0){
                stringBuilder.append(kegiatanlainnya);
            }else{
                stringBuilder.append(", ").append(kegiatanlainnya);
            }
        }
        rbKet = String.valueOf(stringBuilder);
        tvKegiatanFinal.setText(stringBuilder);
    }

    public void addFotoKegiatan(View view){
        ambilFoto("kegiatan");
    }

    public void addFotoSuratPerintah(View view){
        viewLampiran();
    }

    public void viewLampiran(){
        Dialog dialogLampiran = new Dialog(TugasLapanganShiftFinalActivity.this, R.style.DialogStyle);
        dialogLampiran.setContentView(R.layout.view_add_lampiran);
        View llFileManager = dialogLampiran.findViewById(R.id.llFileManager);
        View llKamera = dialogLampiran.findViewById(R.id.llKamera);
        View llDokumen = dialogLampiran.findViewById(R.id.llDokumen);
        View ivTutupViewLampiran = dialogLampiran.findViewById(R.id.ivTutupViewLampiran);

        llFileManager.setOnClickListener(v -> {
            Intent i = new Intent();
            i.setType("image/*");
            i.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(i, "Pilih Gambar"), 33);
            dialogLampiran.dismiss();
        });

        llDokumen.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            resultLauncher.launch(intent);
            dialogLampiran.dismiss();
        });

        llKamera.setOnClickListener(v -> {
            Intent intent = new Intent(this, CameraXTanpaDetectionAcitvity.class);
            intent.putExtra("is_lampiran", true);
            cameraLauncher.launch(intent);
            dialogLampiran.dismiss();
        });
        ivTutupViewLampiran.setOnClickListener(v -> dialogLampiran.dismiss());
        dialogLampiran.show();
    }

    private ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String fileName = result.getData().getStringExtra("namafile");
                    try {
                        filelampiran = createTempFileFromUri(Uri.parse(fileName));
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                    byte[] imageBytes = ambilFoto.compressToMax80KB(filelampiran);
                    Bitmap previewLampiran = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    llLampiranDinasLuar.setVisibility(View.VISIBLE);
                    ivSuratPerintahFinal.setVisibility(View.VISIBLE);
                    llPdfDinasLuar.setVisibility(View.GONE);
                    iconLampiran.setVisibility(View.GONE);
                    ekslampiran = "jpg";
                    ivSuratPerintahFinal.setImageBitmap(previewLampiran);
                }
            });

    private void ambilFoto(String addFoto){
        String filename = "photo";
        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            imageFile = File.createTempFile(filename, ".png", storageDirectory);
            currentPhotoPath = imageFile.getAbsolutePath();
            Uri imageUri = FileProvider.getUriForFile(TugasLapanganShiftFinalActivity.this, "go.pemkott.appsandroidmobiletebingtinggi.fileprovider", imageFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, addFoto.equals("kegiatan") ? 1 : 2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                periksaWaktu();
            } else if (requestCode == 2) {
                processImageLampiran(currentPhotoPath);
            } else if (requestCode == 33 && data != null) {
                processImageUriLampiran(data.getData());
            }
        }
    }

    private void processImageLampiran(String path) {
        llLampiranDinasLuar.setVisibility(View.VISIBLE);
        ivSuratPerintahFinal.setVisibility(View.VISIBLE);
        llPdfDinasLuar.setVisibility(View.GONE);
        iconLampiran.setVisibility(View.GONE);
        File file = new File(path);
        Bitmap bitmap = ambilFoto.compressBitmapTo80KB(file);
        ivSuratPerintahFinal.setImageBitmap(bitmap);
        ekslampiran = "jpg";
    }

    private void processImageUriLampiran(Uri uri) {
        llLampiranDinasLuar.setVisibility(View.VISIBLE);
        ivSuratPerintahFinal.setVisibility(View.VISIBLE);
        llPdfDinasLuar.setVisibility(View.GONE);
        iconLampiran.setVisibility(View.GONE);
        String FilePath2 = getDriveFilePath(uri, TugasLapanganShiftFinalActivity.this);
        File originalLampiran = new File(FilePath2);
        try {
            filelampiran = ambilFoto.compressToFile(this, originalLampiran);
            Bitmap preview = BitmapFactory.decodeFile(filelampiran.getAbsolutePath());
            ivSuratPerintahFinal.setImageBitmap(preview);
            ekslampiran = "jpg";
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void periksaWaktu(){
        try {
            jamMasukDate = SIMPLE_FORMAT_JAM.parse(jamMasuk);
            jamPulangDate = SIMPLE_FORMAT_JAM.parse(jamPulang);
            String jamTaging = SIMPLE_FORMAT_JAM_TAGING.format(new Date());
            Date tagingTime = SIMPLE_FORMAT_JAM_TAGING.parse(jamTaging);

            SimpleDateFormat df = new SimpleDateFormat("HH:mm", localeID);
            Date d = df.parse(jamMasuk);
            cal.setTime(d);
            int minutesToSubtract = 0;
            if (batasWaktu != null && !batasWaktu.trim().isEmpty()) {
                try { minutesToSubtract = Integer.parseInt(batasWaktu.trim()); } catch (NumberFormatException ignored) {}
            }
            cal.add(Calendar.MINUTE, -minutesToSubtract);
            String newTime = df.format(cal.getTime());
            dateBatasWaktu = SIMPLE_FORMAT_JAM_TAGING.parse(newTime);

            long millis = tagingTime.getTime() - jamMasukDate.getTime();
            mins = (int) ((millis / (1000 * 60)));
            if (mins <= 0) mins = 0;
            rbJam = SIMPLE_FORMAT_JAM.format(new Date());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    byte[] imageBytesDokumenPdf;
    public String getPDFPath(Uri uri){
        String absolutePath = "";
        try{
            InputStream inputStream = getContentResolver().openInputStream(uri);
            byte[] pdfInBytes = new byte[inputStream.available()];
            inputStream.read(pdfInBytes);
            String mPath = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/tl-lampiran-" + System.currentTimeMillis() + ".pdf";
            filelampiran = new File(mPath);
            OutputStream op = new FileOutputStream(filelampiran);
            op.write(pdfInBytes);
            absolutePath = filelampiran.getPath();
            imageBytesDokumenPdf = pdfInBytes;
            ekslampiran = "pdf";
            inputStream.close();
            op.close();
        }catch (Exception ae){
            ae.printStackTrace();
        }
        return absolutePath;
    }

    private void requestPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
        }
    }

    private boolean statusAbsenMode = false;

    public void handlerTutupActivity() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            if (!statusAbsenMode && !isFinishing() && !isDestroyed()) {
                cobarekamkembali();
            }
        }, 3 * 60 * 1000);
    }

    public void cobarekamkembali() {
        if (isFinishing() || isDestroyed()) return;
        Dialog dataKosong = new Dialog(this, R.style.DialogStyle);
        dataKosong.setContentView(R.layout.view_warning_kosong);
        TextView tvWarning1 = dataKosong.findViewById(R.id.tvWarning1);
        ImageView tvTutupDialog = dataKosong.findViewById(R.id.tvTutupDialog);
        TextView tvCobaKembali = dataKosong.findViewById(R.id.tvCobaKembali);

        if (tvWarning1 != null) {
            tvWarning1.setText("Proses terlalu lama, coba ulang kembali!");
        }
        dataKosong.setCancelable(false);
        if (tvTutupDialog != null) {
            tvTutupDialog.setOnClickListener(v -> {
                dataKosong.dismiss();
                finish();
            });
        }
        if (tvCobaKembali != null) {
            tvCobaKembali.setVisibility(View.VISIBLE);
            tvCobaKembali.setOnClickListener(v -> {
                dataKosong.dismiss();
                finish();
            });
        }
        dataKosong.show();
    }

    protected void onResume() {
        super.onResume();
        handlerTutupActivity();
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void setupViewModel() {
        locationViewModel = ViewModelProviders.of(this).get(LocationViewModel.class);
    }

    public void checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CHECK_SETTINGS);
            }
        }
    }

    private void setupViews() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private void stopLocationUpdates() {
        if (fusedLocationProviderClient != null) fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        if (context == null) return BitmapDescriptorFactory.defaultMarker();
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        if (vectorDrawable == null) return BitmapDescriptorFactory.defaultMarker();
        int width = vectorDrawable.getIntrinsicWidth();
        int height = vectorDrawable.getIntrinsicHeight();
        if (width <= 0 || height <= 0) {
            width = 48;
            height = 48;
        }
        vectorDrawable.setBounds(0, 0, width, height);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void plotMarkers(Location locationObj) {
        if(map != null && locationObj != null){
            if (rippleAnimator != null) rippleAnimator.cancel();
            map.clear();
            LatLng position = new LatLng(locationObj.getLatitude(), locationObj.getLongitude());
            map.addMarker(new MarkerOptions().position(position).icon(bitmapDescriptorFromVector(this, R.drawable.asn_lk)).title(lokasi.getAddress(this, locationObj.getLatitude(), locationObj.getLongitude())));
            rippleAnimator = MapUtils.showRippleEffect(map, position);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 19f));
            latGMap = locationObj.getLatitude();
            lngGMap = locationObj.getLongitude();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.map = googleMap;
        this.map.setPadding(0, 0, 0, 1050);
        this.map.getUiSettings().setMyLocationButtonEnabled(false);
        this.map.getUiSettings().setRotateGesturesEnabled(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }
}
