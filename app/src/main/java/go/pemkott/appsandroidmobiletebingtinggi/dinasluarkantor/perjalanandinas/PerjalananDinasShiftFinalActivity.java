package go.pemkott.appsandroidmobiletebingtinggi.dinasluarkantor.perjalanandinas;

import static go.pemkott.appsandroidmobiletebingtinggi.geolocation.model.LocationHelper.defaultLocation;
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
import android.app.DatePickerDialog;
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
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.api.ResponsePOJO;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.camerax.CameraXLActivity;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.dialogview.DialogView;
import go.pemkott.appsandroidmobiletebingtinggi.konstanta.AmbilFoto;
import go.pemkott.appsandroidmobiletebingtinggi.konstanta.AmbilFotoLampiran;
import go.pemkott.appsandroidmobiletebingtinggi.konstanta.Lokasi;
import go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager;
import go.pemkott.appsandroidmobiletebingtinggi.model.LocationViewModel;
import go.pemkott.appsandroidmobiletebingtinggi.utils.MapUtils;
import go.pemkott.appsandroidmobiletebingtinggi.utils.NetworkUtils;
import go.pemkott.appsandroidmobiletebingtinggi.utils.WeatherUtil;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerjalananDinasShiftFinalActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_CHECK_SETTINGS = 100;
    private LocationViewModel locationViewModel;
    private Location locationObj;
    private GoogleMap map;
    private ValueAnimator rippleAnimator;
    private static final String KEY_LOCATION = "location";
    double latGMap = 0, lngGMap = 0;
    double latitudeSaya = 3.327972364475644;
    double longitudeSaya = 99.16647248312584;
    Lokasi lokasi = new Lokasi();

    DatabaseHelper databaseHelper;
    ActivityResultLauncher<Intent> resultLauncher;
    private static final String TAG = PerjalananDinasShiftFinalActivity.class.getSimpleName();
    private String currentPhotoPath;
    private String rbLat;
    private String rbLng;
    private String rbTanggal;
    private String rbJam;
    private String rbPosisi;
    private String rbStatus;
    private String rbKet;
    private String rbFakeGPS = "0" ;
    private TextView tvAkurasi, tvJarak, tvTemperature, tvCondition;
    private ImageView ivWeatherIcon;
    private View cardSafeZone;
    private long lastWeatherUpdate = 0;
    String pathDokument;
    String currentDateandTimes = SIMPLE_DATE_FORMAT_TAGING_PHOTO_REPORT.format(new Date());
    String eOPD, eKelompok, eJabatan, latOffice, lngOffice;
    String sEmployeID, sToken, sAkses, sActive, sUsername;
    String kegiatanlainnya, jamMasuk, jamPulang, hariIni;
    String tanggal;
    StringBuilder keterangan;
    String sTglMulai, sTglSAmpai;
    String dariTanggal, sampaiTanggal;
    SimpleDateFormat hari;
    DatePickerDialog datePickerDialogMulai, datePickerDialogSampai;

    String ekslampiran;

    TextView tvHariMulai, tvBulanTahunMulai, tvHariSampai, tvBulanTahunSampai, tvKegiatanFinal, tvSuratPerintah, titleDinasLuar, title_content;
    LinearLayout llLampiranDinasLuarHead;
    View llPdfDinasLuar, llLampiranDinasLuar;
    ArrayList<String> kegiatans = new ArrayList<>();
    AmbilFoto ambilFoto = new AmbilFoto(PerjalananDinasShiftFinalActivity.this);
    AmbilFotoLampiran ambilFotoLampiran = new AmbilFotoLampiran(PerjalananDinasShiftFinalActivity.this);

    Bitmap rotationBitmapTag;
    Bitmap rotationBitmapSurat;

    ShapeableImageView ivFinalKegiatan, ivSuratPerintahFinal;
    ImageView iconLampiran;
    int mins, minspulang;
    DialogView dialogView = new DialogView(this);

    private boolean mockLocationsEnabled = false;
    int mock_location = 0;
    FragmentContainerView fragmentContainerView;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    File file, filelampiran;

    SessionManager session;
    String userId;

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
        setContentView(R.layout.activity_perjalanan_dinas_shift_final);

        session = new SessionManager(this);
        userId = session.getPegawaiId();

        databaseHelper = new DatabaseHelper(this);
        tvKegiatanFinal = findViewById(R.id.tvKegiatanFinalPd);
        tvSuratPerintah = findViewById(R.id.tvSuratPerintahPd);
        titleDinasLuar = findViewById(R.id.titleDinasLuarPd);
        title_content = findViewById(R.id.title_contentPd);
        tvHariMulai = findViewById(R.id.tvHariMulai);
        tvBulanTahunMulai = findViewById(R.id.tvBulanTahunMulai);
        tvHariSampai = findViewById(R.id.tvHariSampai);
        tvBulanTahunSampai = findViewById(R.id.tvBulanTahunSampai);

        fragmentContainerView = findViewById(R.id.map);
        setRoundedBackground(fragmentContainerView);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(5000)
                .build();

        ivFinalKegiatan = findViewById(R.id.ivFinalKegiatan);
        ivSuratPerintahFinal = findViewById(R.id.ivSuratPerintahFinal);
        iconLampiran = findViewById(R.id.iconLampiran);

        llPdfDinasLuar = findViewById(R.id.llPdfDinasLuar);
        llLampiranDinasLuar = findViewById(R.id.llLampiranDinasLuar);
        llLampiranDinasLuarHead = findViewById(R.id.llLampiranDinasLuarHead);

        tvAkurasi = findViewById(R.id.tvAkurasi);
        tvJarak = findViewById(R.id.tvJarak);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvCondition = findViewById(R.id.tvCondition);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);
        cardSafeZone = findViewById(R.id.cardSafeZone);

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.putih));

        if (savedInstanceState != null) {
            locationObj = savedInstanceState.getParcelable(KEY_LOCATION);
        }
        setupViews();
        setupViewModel();
        checkLocationPermission();

        datauser();

        Intent intent = getIntent();
        titleDinasLuar.setText(intent.getStringExtra("title"));
        kegiatans = intent.getStringArrayListExtra("kegiatan_checked_pd");
        kegiatanlainnya = intent.getStringExtra("kegiatan_pd_lainnya");

        String uriString = intent.getStringExtra("namafile");

        if (uriString == null) {
            Toast.makeText(this, "Foto tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Uri imageUri = Uri.parse(uriString);

        try {
            File originalFile = createTempFileFromUri(imageUri);
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
            } else {
                Toast.makeText(this, "Gagal membaca foto", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            if (originalFile.exists()) {
                originalFile.delete();
            }
        } catch (Exception e) {
            Log.e("FOTO_ERROR", "Gagal memproses foto", e);
            Toast.makeText(this, "Gagal memproses foto", Toast.LENGTH_SHORT).show();
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
                    File file= new File(sUri.getPath());
                    llLampiranDinasLuar.setVisibility(View.GONE);
                    llLampiranDinasLuarHead.setVisibility(View.GONE);
                    llPdfDinasLuar.setVisibility(View.VISIBLE);
                    String displayName;

                    try {
                        pathDokument = getPDFPath(sUri);
                    }catch (Exception e){
                        Log.d(TAG, "Exception"+e);
                    }

                    if (sUri.toString().startsWith("content://")) {
                        try (Cursor cursor = PerjalananDinasShiftFinalActivity.this.getContentResolver().query(sUri, null, null, null, null)) {
                            if (cursor != null && cursor.moveToFirst()) {
                                displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                                tvSuratPerintah.setText(displayName);
                            }
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
        datePickerMulai();
        datePickerSampai();

        if (!Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION).equals("0")) {
            mockLocationsEnabled = true;
        } else {
            mockLocationsEnabled = false;
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
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

    public void fokusLokasiPerjalananDinas(View view){
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            boolean isMock = mockLocationsEnabled || locationResult.getLastLocation().isFromMockProvider();
            if (isMock){
                mock_location = 1;
                rbFakeGPS ="1";
            }else{
                mock_location = 0;
            }
            if (map != null) {
                plotMarkers(locationResult.getLastLocation());
            }

            if (locationResult.getLastLocation() != null) {
                if (tvAkurasi != null) {
                    tvAkurasi.setText(String.format(localeID, "± %.0f m", locationResult.getLastLocation().getAccuracy()));
                }
                long now = System.currentTimeMillis();
                if (now - lastWeatherUpdate > 10 * 60 * 1000) {
                    WeatherUtil.fetchWeather(PerjalananDinasShiftFinalActivity.this, locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude(), tvTemperature, tvCondition, ivWeatherIcon);
                    lastWeatherUpdate = now;
                }
            }
        }
    };

    public void kirimDataDinasLuar(View view){
        requestPermission();
        if (mock_location == 1){
            dialogView.viewNotifKosong(PerjalananDinasShiftFinalActivity.this, "Anda terdeteksi menggunakan Fake GPS.", "Jika ditemukan berulang kali, akun anda akan terblokir otomatis dan tercatat Alpa.");
        }else{
            if (file == null || !file.exists() || file.length() == 0){
                dialogView.viewNotifKosong(PerjalananDinasShiftFinalActivity.this, "Anda harus melampirkan Foto Kegiatan dan Surat Perintah Perjalanan Dinas.", "");
            }
            else {
                uploadImages();
            }
        }
    }

    public void uploadImages(){
        periksaWaktu();
        hitungjarak();

        String rbValid;
        if (eJabatan.equals("2")){
            rbValid = "2";
        }else{
            rbValid = "0";
        }

        if (dariTanggal == null || sampaiTanggal == null){
            dialogView.viewNotifKosong(PerjalananDinasShiftFinalActivity.this, "Rentang waktu Perjalanan Dinas tidak boleh kosong.", "");
        }
        else{
            Date dateDariTgl = null, dateSampaiTgl = null;
            try {
                dateDariTgl = SIMPLE_FORMAT_TANGGAL.parse(dariTanggal);
                dateSampaiTgl = SIMPLE_FORMAT_TANGGAL.parse(sampaiTanggal);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (Objects.requireNonNull(dateDariTgl).getTime() > Objects.requireNonNull(dateSampaiTgl).getTime()){
                dialogView.viewNotifKosong(this, "Perhatikan kembali rentang waktu yang anda pilih.", "Tidak boleh terbalik.");
            }else{
                kirimdata(rbValid, "luar", "perjalanan dinas");
            }
        }
    }

    private RequestBody textPart(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }

    private MultipartBody.Part prepareFilePart(String partName, byte[] imageBytes) {
        RequestBody requestBody = RequestBody.create(imageBytes, MediaType.parse("image/jpeg"));
        return MultipartBody.Part.createFormData(partName, "fototaging.jpg", requestBody);
    }



    public void kirimdata(String valid, String posisi, String status){
        Dialog dialogproses = new Dialog(PerjalananDinasShiftFinalActivity.this, R.style.DialogStyle);
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

        Call<ResponsePOJO> call = RetroClient.getInstance().getApi().uploadAbsenPerjalananDinas(
                fotoPart,
                textPart(eJabatan),
                textPart(sEmployeID),
                textPart(JadwalPerjalananDinasShiftActivity.idsift),
                textPart(rbJam),
                textPart(posisi),
                textPart(status),
                textPart(rbLat),
                textPart(rbLng),
                textPart(rbKet),
                textPart(String.valueOf(0)),
                textPart(eOPD),
                textPart(valid),
                lampiranPart,
                textPart(ekslampiran),
                textPart(dariTanggal),
                textPart(sampaiTanggal),
                textPart(rbFakeGPS)
        );

        call.enqueue(new Callback<ResponsePOJO>() {
            @Override
            public void onResponse(@NonNull Call<ResponsePOJO> call, @NonNull Response<ResponsePOJO> response) {
                dialogproses.dismiss();
                if (!response.isSuccessful()) {
                    dialogView.viewNotifKosong(PerjalananDinasShiftFinalActivity.this, "Gagal mengisi absensi", "Silahkan coba kembali.");
                    return;
                }

                ResponsePOJO data = response.body();
                if (Objects.requireNonNull(response.body()).isStatus()){
                    databaseHelper.insertLog(sEmployeID, eOPD, tanggal, "Melakukan Absen Perjalanan Dinas (Shift)", "Perjalanan Dinas");
                    dialogView.viewSukses(PerjalananDinasShiftFinalActivity.this, data.getRemarks());
                } else {
                    dialogView.viewNotifKosong(PerjalananDinasShiftFinalActivity.this, data.getRemarks(),"");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponsePOJO> call, @NonNull Throwable t) {
                dialogproses.dismiss();
                dialogView.viewNotifKosong(PerjalananDinasShiftFinalActivity.this, "Gagal mengisi absensi,", "silahkan coba kembali.");
            }
        });
        dialogproses.show();
    }

    public void hitungjarak(){
        if(NetworkUtils.isConnected(this)){
            rbLat = String.valueOf(latGMap);
            rbLng = String.valueOf(lngGMap);
        }else{
            dialogView.viewNotifKosong(PerjalananDinasShiftFinalActivity.this, "Pastikan anda telah terhubung internet.", "");
        }
    }

    private void datauser() {
        Cursor res = databaseHelper.getAllData22(userId);
        while (res.moveToNext()) {
            sEmployeID = res.getString(1);
            sUsername = res.getString(2);
            sAkses = res.getString(2);
            sActive = res.getString(4);
            sToken = res.getString(5);
        }
        hari = new SimpleDateFormat("EEE", localeID);
        tanggal = hari.format(new Date());

        Cursor employe = databaseHelper.getDataEmployee(sEmployeID);
        while (employe.moveToNext()){
            eOPD = employe.getString(4);
            eKelompok = employe.getString(9);
            eJabatan = employe.getString(11);
            latOffice = employe.getString(15);
            lngOffice = employe.getString(16);
        }
    }

    public void dataKegiatan() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < kegiatans.size(); i++) {
            String kegiatan = kegiatans.get(i);
            if (i == kegiatans.size() - 1) {
                stringBuilder.append(kegiatan);
            } else {
                stringBuilder.append(kegiatan).append(", ");
            }
        }
        if (kegiatanlainnya != null && !kegiatanlainnya.equals("kosong")) {
            if (kegiatans.isEmpty()) {
                stringBuilder.append(kegiatanlainnya);
            } else {
                stringBuilder.append(", ").append(kegiatanlainnya);
            }
        }
        rbKet = String.valueOf(stringBuilder);
        tvKegiatanFinal.setText(stringBuilder);
    }

    public void periksaWaktu(){
        rbJam = SIMPLE_FORMAT_JAM.format(new Date());
    }

    public void addFotoSuratPerintah(View view){
        viewLampiran();
    }

    public void viewLampiran(){
        Dialog dialogLampiran = new Dialog(PerjalananDinasShiftFinalActivity.this, R.style.DialogStyle);
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
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            resultLauncher.launch(intent);
            dialogLampiran.dismiss();
        });

        llKamera.setOnClickListener(v -> {
            Intent intent = new Intent(this, CameraXLActivity.class);
            intent.putExtra("aktivitas", "perjalanandinas_shift");
            cameraLauncher.launch(intent);
            dialogLampiran.dismiss();
        });

        ivTutupViewLampiran.setOnClickListener(v -> dialogLampiran.dismiss());
        dialogLampiran.show();
    }

    private ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String fileName = result.getData().getStringExtra("namafile");
                    if (fileName != null && (fileName.startsWith("content://") || fileName.startsWith("file://"))) {
                        try {
                            filelampiran = createTempFileFromUri(Uri.parse(fileName));
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(PerjalananDinasShiftFinalActivity.this, "Gagal memproses file lampiran", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        String myDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/eabsensi";
                        filelampiran = new File(myDir, fileName);
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
            }
    );

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED){
            if (requestCode == 33 && resultCode == Activity.RESULT_OK && data != null){
                requestPermission();
                llLampiranDinasLuarHead.setVisibility(View.VISIBLE);
                llLampiranDinasLuar.setVisibility(View.VISIBLE);
                ivSuratPerintahFinal.setVisibility(View.VISIBLE);
                llPdfDinasLuar.setVisibility(View.GONE);
                iconLampiran.setVisibility(View.GONE);
                Uri selectedImageUri = data.getData();
                String FilePath2  = getDriveFilePath(selectedImageUri, PerjalananDinasShiftFinalActivity.this);
                File originalLampiran = new File(FilePath2);
                try {
                    filelampiran = ambilFoto.compressToFile(this, originalLampiran);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                BitmapFactory.Options optsLampiran = new BitmapFactory.Options();
                optsLampiran.inSampleSize = 4;
                optsLampiran.inPreferredConfig = Bitmap.Config.RGB_565;
                Bitmap preview = BitmapFactory.decodeFile(filelampiran.getAbsolutePath(), optsLampiran);
                ivSuratPerintahFinal.setImageBitmap(preview);
                ekslampiran = "jpg";
            }
        }
    }

    public void llMulaiDinasLuar(View view){
        datePickerDialogMulai.show();
    }

    private void datePickerMulai(){
        DatePickerDialog.OnDateSetListener dateSetListener = (datePicker, tahun, bulan, hari) -> {
            bulan = bulan + 1;
            String date = makeDateString(tahun, bulan);
            dariTanggal = tahun + "-" + String.format("%02d", bulan) + "-" + String.format("%02d", hari);
            tvHariMulai.setText(String.valueOf(hari));
            tvBulanTahunMulai.setText(date);
        };
        Calendar calendar = Calendar.getInstance();
        datePickerDialogMulai = new DatePickerDialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }

    public void llSampaiDinasLuar(View view){
        datePickerDialogSampai.show();
    }

    private void datePickerSampai(){
        DatePickerDialog.OnDateSetListener dateSetListener = (datePicker, tahun, bulan, hari) -> {
            bulan = bulan + 1;
            String date = makeDateString(tahun, bulan);
            sampaiTanggal = tahun + "-" + String.format("%02d", bulan) + "-" + String.format("%02d", hari);
            tvHariSampai.setText(String.valueOf(hari));
            tvBulanTahunSampai.setText(date);
        };
        Calendar calendar = Calendar.getInstance();
        datePickerDialogSampai = new DatePickerDialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }

    private String makeDateString(int year, int month) {
        return getMonthFormat(month) + " " + year;
    }

    private String getMonthFormat(int month) {
        if (month == 1) return "JAN";
        if (month == 2) return "FEB";
        if (month == 3) return "MAR";
        if (month == 4) return "APR";
        if (month == 5) return "MEI";
        if (month == 6) return "JUN";
        if (month == 7) return "JUL";
        if (month == 8) return "AGU";
        if (month == 9) return "SEP";
        if (month == 10) return "OKT";
        if (month == 11) return "NOV";
        if (month == 12) return "DES";
        return "JAN";
    }

    byte[] imageBytesDokumenPdf;
    public String getPDFPath(Uri uri){
        String absolutePath = "";
        try{
            InputStream inputStream = getContentResolver().openInputStream(uri);
            byte[] pdfInBytes = new byte[inputStream.available()];
            inputStream.read(pdfInBytes);
            String mPath = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "absensi-" + sEmployeID + "-" + currentDateandTimes + ".pdf";
            filelampiran = new File(mPath);
            OutputStream op = new FileOutputStream(filelampiran);
            op.write(pdfInBytes);
            absolutePath = filelampiran.getPath();
            InputStream finput = new FileInputStream(filelampiran);
            imageBytesDokumenPdf = new byte[(int)filelampiran.length()];
            finput.read(imageBytesDokumenPdf, 0, imageBytesDokumenPdf.length);
            finput.close();
            ekslampiran = "pdf";
        }catch (Exception ae){
            ae.printStackTrace();
        }
        return absolutePath;
    }

    private void setupViewModel() {
        locationViewModel = ViewModelProviders.of(this).get(LocationViewModel.class);
    }

    public void checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CHECK_SETTINGS);
            }
        }
    }

    public void backFinalDinasLuar(View view){
        stopLocationUpdates();
        finish();
    }

    private void setupViews() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void plotMarkers(Location locationObj) {
        if(map != null){
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
        this.map.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
        this.map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        this.map.getUiSettings().setMyLocationButtonEnabled(true);
        this.map.getUiSettings().setCompassEnabled(true);
    }

    private void requestPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        rbTanggal = SIMPLE_FORMAT_TANGGAL.format(new Date());
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }
}
