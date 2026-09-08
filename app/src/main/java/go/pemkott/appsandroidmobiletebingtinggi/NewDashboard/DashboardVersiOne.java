package go.pemkott.appsandroidmobiletebingtinggi.NewDashboard;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.view.animation.OvershootInterpolator;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.card.MaterialCardView;
import com.bumptech.glide.Glide;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;


import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import go.pemkott.appsandroidmobiletebingtinggi.ProfileActivity;
import go.pemkott.appsandroidmobiletebingtinggi.helpdesk.HelpdeskActivity;
import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.dialogview.DialogView;
import go.pemkott.appsandroidmobiletebingtinggi.dinasluarkantor.perjalanandinas.SppdActivity;
import go.pemkott.appsandroidmobiletebingtinggi.izinshift.JadwalIzinShiftActivity;
import go.pemkott.appsandroidmobiletebingtinggi.kehadiransift.JadwalShiftActivity;
import go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager;

import go.pemkott.appsandroidmobiletebingtinggi.model.KegiatanIzin;
import go.pemkott.appsandroidmobiletebingtinggi.model.Koordinat;
import go.pemkott.appsandroidmobiletebingtinggi.model.ValidasiData;
import go.pemkott.appsandroidmobiletebingtinggi.rekap.RekapAbsensActivity;
import go.pemkott.appsandroidmobiletebingtinggi.singkronjadwal.SettingAdapter;
import go.pemkott.appsandroidmobiletebingtinggi.singkronjadwal.TimeTebleSetting;
import go.pemkott.appsandroidmobiletebingtinggi.singkronjadwalsift.CalendarJadwalShiftActivity;
import go.pemkott.appsandroidmobiletebingtinggi.utils.NetworkUtil;
import go.pemkott.appsandroidmobiletebingtinggi.verifikasi.ValidasiNewActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.os.Environment;
import android.os.StatFs;

public class DashboardVersiOne extends AppCompatActivity {

    CardView cvKehadiran, cvJadwal, cvLokasi, cvKegiatan, cvMenuIzin, cvMenuPerjalananDinas;
    private static final long MIN_STORAGE_MB = 300;
//    private static final long MIN_STORAGE_GB = 40;
    LocationManager manager;
    DatabaseHelper databaseHelper;
    HttpService httpService;

    public static String statusSift, fotoProfile, jam_masuk, jam_pulang, sOPD, sNip, sJabatan, sKantor, sEmployee_id, sUsername, sAkses, sActive,  sToken, sVerifikator;

    TextView tvNamaUser, tvTanggalHariIni;
    public static int jenisabsensi;
    DialogView dialogView = new DialogView(DashboardVersiOne.this);

    ProgressBar pgSingkronLokasi, pgSingkronKegiatan, pgSingkronJadwal;
    ImageView ivStatusJadwal, ivStatusLokasi, ivStatusKegiatan;
    CircleImageView ciUser;

    CardView cvSyncStatusToast;
    ImageView ivSyncStatusIcon;
    TextView tvSyncStatusMessage;
    MaterialCardView clVerifikasi;
    public static DashboardVersiOne dashboardVersiOne;
    SwipeRefreshLayout swipeRefreshLayout;

    ConstraintLayout clCariRekap;
    SessionManager session;

    String userid;
    private static final int REQ_NOTIFICATION = 1001;
    private static final int REQ_APP_PERMISSION = 1002;

    private static final String CAMERA_PERMISSION =
            Manifest.permission.CAMERA;

    private static final String LOCATION_PERMISSION =
            Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int REQ_UPDATE = 2001;

    Retrofit retrofit;

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

        String ipPerangkat = NetworkUtil.getDeviceIp(this);
        if ("-".equals(ipPerangkat)) {
            ipPerangkat = NetworkUtil.getDeviceIpFallback();
        }

        boolean isDarkMode = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        controller.setAppearanceLightStatusBars(!isDarkMode);
        controller.setAppearanceLightNavigationBars(!isDarkMode);

        setContentView(R.layout.activity_dashboard_versi_one);

        createNotificationChannel();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission();
        } else {
            requestAppPermissions(); // Android < 13
        }

        checkAppUpdate();

        session = new SessionManager(this);
        userid = session.getPegawaiId();
        dashboardVersiOne = this;
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        databaseHelper = new DatabaseHelper(this);

        datauser();

        retrofit = new Retrofit.Builder()
                .baseUrl("https://absensi.tebingtinggikota.go.id/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        httpService = retrofit.create(HttpService.class);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        cvKehadiran = findViewById(R.id.cvKehadiran);
        tvNamaUser = findViewById(R.id.tvNamaUser);
        ciUser = findViewById(R.id.ciUser);
        cvJadwal = findViewById(R.id.cvJadwal);
        cvLokasi = findViewById(R.id.cvLokasi);
        pgSingkronKegiatan = findViewById(R.id.pgSingkronKegiatan);
        cvKegiatan = findViewById(R.id.cvKegiatan);
        cvMenuIzin = findViewById(R.id.cvMenuIzin);
        cvMenuPerjalananDinas = findViewById(R.id.cvMenuPerjalananDinas);
        clVerifikasi = findViewById(R.id.clVerifikasi);
        clCariRekap = findViewById(R.id.clCariRekap);
        tvTanggalHariIni = findViewById(R.id.tvTanggalHariIni);
        pgSingkronLokasi = findViewById(R.id.pgSingkronLokasi);
        pgSingkronKegiatan = findViewById(R.id.pgSingkronKegiatan);
        pgSingkronJadwal = findViewById(R.id.pgSingkronJadwal);
        ivStatusJadwal = findViewById(R.id.ivStatusJadwal);
        ivStatusLokasi = findViewById(R.id.ivStatusLokasi);
        ivStatusKegiatan = findViewById(R.id.ivStatusKegiatan);

        cvSyncStatusToast = findViewById(R.id.cvSyncStatusToast);
        ivSyncStatusIcon = findViewById(R.id.ivSyncStatusIcon);
        tvSyncStatusMessage = findViewById(R.id.tvSyncStatusMessage);

        clCariRekap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardVersiOne.this, RekapAbsensActivity.class));
            }
        });

        ciUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardVersiOne.this, ProfileActivity.class));
            }
        });

        cvKehadiran.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isStorageEnough()) {

                    showStorageFullDialog();

                    return;
                }

                if ("0".equals(statusSift)) {

                    bukaKehadiran();

                } else {

                    jenisabsensi = 7;

                    Intent intentJadwalSifting =
                            new Intent(
                                    DashboardVersiOne.this,
                                    JadwalShiftActivity.class
                            );

                    intentJadwalSifting.putExtra(
                            "jam_masuk",
                            jam_masuk
                    );

                    intentJadwalSifting.putExtra(
                            "jam_pulang",
                            jam_pulang
                    );

                    startActivity(intentJadwalSifting);
                }
            }
        });


        if (sUsername != null && sUsername.length() > 13) {
            String truncatedName = sUsername.substring(0, 13) + "...";
            tvNamaUser.setText(truncatedName);
        } else {
            tvNamaUser.setText(sUsername);
        }

        Glide.with(this)
                .load("https://absensi.tebingtinggikota.go.id/storage/foto-pegawai/" + fotoProfile)
                .placeholder(R.drawable.profil_pic)
                .error(R.drawable.profil_pic)
                .into(ciUser);

        cvJadwal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ("0".equals(statusSift)){
                    viewJadwalKerja();
                }else{
                    Intent intentJadwalSifting = new Intent(DashboardVersiOne.this, CalendarJadwalShiftActivity.class);
                    intentJadwalSifting.putExtra("jam_masuk", jam_masuk);
                    intentJadwalSifting.putExtra("jam_pulang", jam_pulang);
                    startActivity(intentJadwalSifting);
                }
            }
        });

        cvLokasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pgSingkronLokasi.setVisibility(View.VISIBLE);
                koordinatOPD();
//                koordintaEmployee();
            }
        });

        cvKegiatan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pgSingkronKegiatan.setVisibility(View.VISIBLE);

                singkronKegiatan();
            }
        });


        cvMenuIzin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isStorageEnough()) {

                    showStorageFullDialog();

                    return;
                }

                if ("0".equals(statusSift)) {

                    bukaIzin();

                } else {

                    jenisabsensi = 8;

                    Intent intentJadwalSifting =
                            new Intent(
                                    DashboardVersiOne.this,
                                    JadwalIzinShiftActivity.class
                            );

                    startActivity(intentJadwalSifting);
                }
            }
        });

        cvMenuPerjalananDinas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isStorageEnough()) {

                    showStorageFullDialog();

                    return;
                }

                jenisabsensi = 3;

                startActivity(
                        new Intent(
                                DashboardVersiOne.this,
                                SppdActivity.class
                        )
                );
            }
        });


        clVerifikasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardVersiOne.this, ValidasiNewActivity.class));
            }
        });

        ImageView fabHelpdesk = findViewById(R.id.fabHelpdesk);
        fabHelpdesk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardVersiOne.this, HelpdeskActivity.class));
            }
        });

        // Animasi muncul (Pop-in dengan Overshoot)
        fabHelpdesk.setAlpha(0f);
        fabHelpdesk.setScaleX(0.2f);
        fabHelpdesk.setScaleY(0.2f);
        fabHelpdesk.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1000)
                .setStartDelay(500)
                .setInterpolator(new OvershootInterpolator())
                .start();

        // Animasi bernapas (Floating subtle) agar terlihat hidup
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(fabHelpdesk, "scaleX", 1f, 1.08f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(fabHelpdesk, "scaleY", 1f, 1.08f);
        scaleX.setRepeatCount(ObjectAnimator.INFINITE);
        scaleX.setRepeatMode(ObjectAnimator.REVERSE);
        scaleY.setRepeatCount(ObjectAnimator.INFINITE);
        scaleY.setRepeatMode(ObjectAnimator.REVERSE);

        AnimatorSet breathingAnim = new AnimatorSet();
        breathingAnim.playTogether(scaleX, scaleY);
        breathingAnim.setDuration(1200);
        breathingAnim.start();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onResume();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }

    private boolean isStorageEnough() {

        StatFs stat = new StatFs(
                Environment.getDataDirectory().getPath()
        );

        long bytesAvailable;

        bytesAvailable = stat.getAvailableBytes();

        long megaAvailable =
                bytesAvailable / (1024 * 1024);



//        long gigaAvailable =
//
//                bytesAvailable / (1024L * 1024L * 1024L);

        Log.d(
                "STORAGE_CHECK",
                "Sisa Storage: " + megaAvailable + " MB"
        );

        return megaAvailable >= MIN_STORAGE_MB;
    }


    private void showStorageFullDialog() {

        new AlertDialog.Builder(this)
                .setTitle("Storage Tidak Cukup")
                .setMessage(
                        "Penyimpanan anda telah penuh.\n" +
                                "Silakan kosongkan penyimpanan terlebih dahulu sebelum melakukan absensi."
                )
                .setCancelable(false)
                .setPositiveButton(
                        "Buka Pengaturan",
                        (dialog, which) -> {

                            Intent intent =
                                    new Intent(
                                            Settings.ACTION_INTERNAL_STORAGE_SETTINGS
                                    );

                            startActivity(intent);
                        })
                .setNegativeButton(
                        "Tutup",
                        (dialog, which) -> dialog.dismiss()
                )
                .show();
    }
    private void bukaKehadiran() {
        KehadiranBottomSheet bottomSheet = new KehadiranBottomSheet();
        bottomSheet.show(getSupportFragmentManager(), "KEHADIRAN_BOTTOM_SHEET");
    }

    private void bukaIzin() {
        IzinBottomSheet bottomSheet = new IzinBottomSheet();
        bottomSheet.show(getSupportFragmentManager(), "IZIN_BOTTOM_SHEET");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_UPDATE) {
            if (resultCode != RESULT_OK) {
                finish();
            }
        }
    }

    private void checkAppUpdate() {

        AppUpdateManager appUpdateManager =
                AppUpdateManagerFactory.create(this);

        appUpdateManager.getAppUpdateInfo()
                .addOnSuccessListener(appUpdateInfo -> {

                    Log.d("UPDATE Versi Aplikasi",
                            "Availability=" + appUpdateInfo.updateAvailability()
                                    + " | InstallStatus=" + appUpdateInfo.installStatus()
                    );

                    if (appUpdateInfo.updateAvailability()
                            == UpdateAvailability.UPDATE_AVAILABLE
                            && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {

                        try {
                            appUpdateManager.startUpdateFlowForResult(
                                    appUpdateInfo,
                                    AppUpdateType.IMMEDIATE,
                                    DashboardVersiOne.this,
                                    REQ_UPDATE
                            );
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                    }

                })
                .addOnFailureListener(e -> {
                    Log.e("UPDATE Versi Aplikasi", "Gagal cek update", e);

                    Toast.makeText(
                            DashboardVersiOne.this,
                            "Gagal cek update",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_NOTIFICATION) {

            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Log.d("NOTIF", "Izin notifikasi diberikan");

            } else {
                Log.w("NOTIF", "Izin notifikasi ditolak");
            }

            // ⚠️ APAPUN HASILNYA → LANJUT
            requestAppPermissions();
        }

        else if (requestCode == REQ_APP_PERMISSION) {

            boolean allGranted = true;

            for (int result : grantResults) {

                if (result != PackageManager.PERMISSION_GRANTED) {

                    allGranted = false;

                    break;

                }

            }

            if (!allGranted) {

                Toast.makeText(

                        this,

                        "Sebagian izin ditolak",

                        Toast.LENGTH_SHORT

                ).show();

            }

        }
    }


//    private void requestNotificationPermission() {
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//
//            if (ContextCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.POST_NOTIFICATIONS
//            ) != PackageManager.PERMISSION_GRANTED) {
//
//                ActivityCompat.requestPermissions(
//                        this,
//                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
//                        1001
//                );
//            }
//        }
//    }

    private void requestNotificationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.POST_NOTIFICATIONS
                        },
                        REQ_NOTIFICATION
                );

            } else {
                requestAppPermissions();
            }

        } else {
            requestAppPermissions();
        }
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    "default_channel",
                    "Notifikasi Umum",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription("Notifikasi aplikasi absensi");

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }


    private void requestAppPermissions() {

        List<String> permissionsNeeded = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(
                this,
                CAMERA_PERMISSION)
                != PackageManager.PERMISSION_GRANTED) {

            permissionsNeeded.add(CAMERA_PERMISSION);
        }

        if (ContextCompat.checkSelfPermission(
                this,
                LOCATION_PERMISSION)
                != PackageManager.PERMISSION_GRANTED) {

            permissionsNeeded.add(LOCATION_PERMISSION);
        }

        if (!permissionsNeeded.isEmpty()) {


            ActivityCompat.requestPermissions(
                    this,
                    permissionsNeeded.toArray(new String[0]),
                    REQ_APP_PERMISSION
            );
        }

        // HAPUS else
    }
    @Override
    protected void onResume() {
        super.onResume();

        if (sVerifikator != null && sEmployee_id != null){
            dataValidasi(sVerifikator, sEmployee_id);
        }

    }

    public void dataValidasi(String verifikator, String idE){
        if ("verifikator1".equals(verifikator) || "verifikator2".equals(verifikator)){
            Call<List<ValidasiData>> callKegiatan = httpService.getUrlListValidasi("https://absensi.tebingtinggikota.go.id/api/newVeriFragment?verifikator="+verifikator+"&id="+idE);
            callKegiatan.enqueue(new Callback<List<ValidasiData>>() {
                @Override
                public void onResponse(@NonNull Call<List<ValidasiData>> call, @NonNull Response<List<ValidasiData>> response) {
                    if (!response.isSuccessful()){

                        return;
                    }

                    List<ValidasiData> validasiDatas = response.body();
                    if (validasiDatas != null && !validasiDatas.isEmpty()) {
                        clVerifikasi.setVisibility(View.VISIBLE);
                    }else{
                        clVerifikasi.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<ValidasiData>> call, @NonNull Throwable t) {
//                    pesanError();
                }
            });
        }

    }


    public void checkGPS(){
        if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            AlertDialog.Builder builder = new AlertDialog.Builder(DashboardVersiOne.this, R.style.ThemeOverlay_App_MaterialAlertDialog);
            builder.setCancelable(false);
            builder.setTitle("Peringatan!");
            builder.setMessage("GPS anda tidak aktif, anda harus mengaktifkan GPS terlebih dahulu!");
            builder.setNegativeButton("Nyalakan",
                    (dialogInterface, i) -> {

                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        dialogInterface.dismiss();
                    }
            );

            AlertDialog alert = builder.create();
            alert.show();
        }else{
            getLocation();
        }
    }

    public void getLocation(){
        if (ActivityCompat.checkSelfPermission(DashboardVersiOne.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DashboardVersiOne.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DashboardVersiOne.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }
    public void datauser(){

        Cursor res = databaseHelper.getAllData22(userid);
        if (res.getCount()==0){
            return;
        }


        try {
            while (res.moveToNext()){
                sEmployee_id = res.getString(1);
                sAkses = res.getString(3);
                sActive = res.getString(4);
                sToken = res.getString(5);
                sVerifikator = res.getString(6);
            }
        } finally {
            res.close();

        }

        Cursor dataPegawai = databaseHelper.getDataEmployee(sEmployee_id);
        try {
            while (dataPegawai.moveToNext()){
                sNip = dataPegawai.getString(5);
                sUsername = dataPegawai.getString(6);
                sJabatan = dataPegawai.getString(12);
                sKantor = dataPegawai.getString(13);
                sOPD = dataPegawai.getString(4);
                fotoProfile = dataPegawai.getString(17);
                statusSift = dataPegawai.getString(19);

            }

        } finally {
            dataPegawai.close();

        }
    }


    List<TimeTebleSetting> timeTable = new ArrayList<>();

    public void viewJadwalKerja(){
        timeTable.clear();

        Dialog dialogJadwalKerja = new Dialog(DashboardVersiOne.this, R.style.DialogStyle);
        dialogJadwalKerja.setContentView(R.layout.view_jadwal_kerja);
        dialogJadwalKerja.setCancelable(true);

        RecyclerView rvJadwalKerja= dialogJadwalKerja.findViewById(R.id.rvJadwalKerja);
        TextView tvInfoJamKerja= dialogJadwalKerja.findViewById(R.id.tvInfoJamKerja);
        LinearLayout btnSingkronJadwalKerja= dialogJadwalKerja.findViewById(R.id.btnSingkronJadwalKerja);
        ImageView ivCloseDaftarJadwalKerja= dialogJadwalKerja.findViewById(R.id.ivCloseDaftarJadwalKerja);
        ProgressBar progressBarSingkron= dialogJadwalKerja.findViewById(R.id.progressBarSingkron);
        rvJadwalKerja.setHasFixedSize(false);

        Cursor jadwal = databaseHelper.getKegiatanTimeTableCheck(sEmployee_id);
        if (jadwal.getCount() == 0){
            tvInfoJamKerja.setVisibility(View.VISIBLE);
        }
        try{
            while (jadwal.moveToNext()){
                TimeTebleSetting timeTables = new TimeTebleSetting();
                timeTables.setId(jadwal.getString(0));
                timeTables.setEmployee_id(jadwal.getString(1));
                timeTables.setTimetable_id(jadwal.getString(2));
                timeTables.setInisial(jadwal.getString(3));
                timeTables.setHari(jadwal.getString(4));
                timeTables.setMasuk(jadwal.getString(5));
                timeTables.setPulang(jadwal.getString(6));
                timeTable.add(timeTables);
            }
        } finally {
                jadwal.close();

        }

        rvJadwalKerja.setLayoutManager(new GridLayoutManager(DashboardVersiOne.this, 1));
        SettingAdapter settingAdapter = new SettingAdapter(timeTable);
        rvJadwalKerja.setAdapter(settingAdapter);

        ivCloseDaftarJadwalKerja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogJadwalKerja.dismiss();
            }
        });

        btnSingkronJadwalKerja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBarSingkron.setVisibility(View.VISIBLE);
                btnSingkronJadwalKerja.setVisibility(View.GONE);

                Call<List<TimeTebleSetting>> callKegiatan = httpService.getUrlTimeTableSetting("https://absensi.tebingtinggikota.go.id/api/timetable?employee_id="+sEmployee_id, "Bearer "+sToken);
                ivStatusJadwal.setVisibility(View.GONE);
                pgSingkronJadwal.setVisibility(View.VISIBLE);

                callKegiatan.enqueue(new Callback<List<TimeTebleSetting>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<TimeTebleSetting>> call, @NonNull Response<List<TimeTebleSetting>> response) {
                        List<TimeTebleSetting> timeTables = response.body();
                        if (!response.isSuccessful()){
                            dialogView.viewNotifKosong(DashboardVersiOne.this, "Gagal melakukan singkronisasi jadwal.","Silahkan coba kembali.");
                            pgSingkronJadwal.setVisibility(View.GONE);
                            showSyncStatus(ivStatusJadwal, false, "Sinkron Jadwal Gagal");
                            return;
                        }

                        Integer deleteTimeTable = databaseHelper.deleteTimeTable(sEmployee_id);
                        if (deleteTimeTable > 0){
                            timeTable.clear();
                            timeTable = timeTables;
                        }

//                        timeTables.size();
                        for (TimeTebleSetting timeTable : timeTables){
                            databaseHelper.insertDataTimeTable(timeTable.getId(), timeTable.getEmployee_id(), timeTable.getTimetable_id(),
                                    timeTable.getInisial(), timeTable.getHari(), timeTable.getMasuk(), timeTable.getPulang());
                        }


                        rvJadwalKerja.setLayoutManager(new GridLayoutManager(DashboardVersiOne.this, 1));
                        SettingAdapter settingAdapter = new SettingAdapter(timeTables);
                        rvJadwalKerja.setAdapter(settingAdapter);


                        btnSingkronJadwalKerja.setVisibility(View.VISIBLE);
                        progressBarSingkron.setVisibility(View.GONE);

                        pgSingkronJadwal.setVisibility(View.GONE);
                        showSyncStatus(ivStatusJadwal, true, "Sinkron Jadwal Berhasil");

                    }

                    @Override
                    public void onFailure(@NonNull Call<List<TimeTebleSetting>> call, @NonNull Throwable t) {

                        dialogView.viewNotifKosong(DashboardVersiOne.this, "Gagal mengakses server.", "Silahkan coba kembali.");
                        pgSingkronJadwal.setVisibility(View.GONE);
                        showSyncStatus(ivStatusJadwal, false, "Sinkron Jadwal Gagal");
                    }
                });

            }
        });

        dialogJadwalKerja.show();
    }


    public void koordinatOPD(){
        ivStatusLokasi.setVisibility(View.GONE);
        pgSingkronLokasi.setVisibility(View.VISIBLE);

        Call<List<Koordinat>> calllokasi = httpService.getUrlKoordinat("https://absensi.tebingtinggikota.go.id/api/koordinat?opdid="+sOPD);
        calllokasi.enqueue(new Callback<List<Koordinat>>() {
            @Override
            public void onResponse(@NonNull Call<List<Koordinat>> call, @NonNull Response<List<Koordinat>> response) {
                List<Koordinat> koordinats = response.body();
                if (!response.isSuccessful()){
                    dialogView.viewNotifKosong(DashboardVersiOne.this, "Gagal melakukan singkronisasi lokasi.","Silahkan coba kembali.");
                    pgSingkronLokasi.setVisibility(View.GONE);
                    showSyncStatus(ivStatusLokasi, false, "Sinkron Lokasi Gagal");
                    return;
                }

                Integer deleteKoordinatOPD = databaseHelper.deleteDataKoordinatOPD(sOPD);
                if (deleteKoordinatOPD > 0){
                    for (Koordinat koordinat : koordinats){
                        databaseHelper.insertDataKoordinat(koordinat.getId(), koordinat.getOpd_id(), koordinat.getAlamat(), koordinat.getLet(), koordinat.getLng());

                    }
                }

                koordintaEmployee();

            }

            @Override
            public void onFailure(@NonNull Call<List<Koordinat>> call, @NonNull Throwable t) {
                dialogView.viewNotifKosong(DashboardVersiOne.this, "Gagal mengakses server.", "Silahkan coba kembali.");
                pgSingkronLokasi.setVisibility(View.GONE);
                showSyncStatus(ivStatusLokasi, false, "Sinkron Lokasi Gagal");
            }
        });

    }

    public void koordintaEmployee(){

        Call<List<Koordinat>> callKegiatan = httpService.getUrlKoordinat("https://absensi.tebingtinggikota.go.id/api/koordinatemployee?id="+sEmployee_id);
        callKegiatan.enqueue(new Callback<List<Koordinat>>() {
            @Override
            public void onResponse(@NonNull Call<List<Koordinat>> call, @NonNull Response<List<Koordinat>> response) {
                List<Koordinat> koordinats = response.body();
                if (!response.isSuccessful()){
                    dialogView.viewNotifKosong(DashboardVersiOne.this, "Gagal melakukan singkronisasi lokasi.","Silahkan coba kembali.");
                    pgSingkronLokasi.setVisibility(View.GONE);
                    showSyncStatus(ivStatusLokasi, false, "Sinkron Lokasi Gagal");
                    return;
                }

//                Log.d("TESTING KOORDINAT", String.valueOf(koordinats.size()));

                if (koordinats == null || koordinats.isEmpty()) {

                    pgSingkronLokasi.setVisibility(View.GONE);
                    showSyncStatus(ivStatusLokasi, true, "Sinkron Lokasi Berhasil");
                    return;

                }
                if ("kosong".equals(response.body().get(0).getStatus())){
                    databaseHelper.deleteDataKoordinatEmployee(sEmployee_id);
                }else{
                    Cursor koorditaemplyeData = databaseHelper.getDataKoordinatEmp(sEmployee_id);
                    if (koorditaemplyeData.getCount() == 0){
                        for (Koordinat koordinat : koordinats){
                            databaseHelper.insertDataKoordinatEmployee(koordinat.getId(), sEmployee_id, koordinat.getAlamat(), koordinat.getLet(), koordinat.getLng());

                        }
                    }else{
                        Integer deleteKoordinatE = databaseHelper.deleteDataKoordinatEmployee(sEmployee_id);
                        if (deleteKoordinatE > 0){

                            for (Koordinat koordinat : koordinats){
                                databaseHelper.insertDataKoordinatEmployee(koordinat.getId(), sEmployee_id, koordinat.getAlamat(), koordinat.getLet(), koordinat.getLng());
                            }

                        }
                    }

                }

                pgSingkronLokasi.setVisibility(View.GONE);
                showSyncStatus(ivStatusLokasi, true, "Sinkron Lokasi Berhasil");
            }

            @Override
            public void onFailure(@NonNull Call<List<Koordinat>> call, @NonNull Throwable t) {

                dialogView.viewNotifKosong(DashboardVersiOne.this, "Gagal mengakses server.", "Silahkan coba kembali.");
                pgSingkronLokasi.setVisibility(View.GONE);
                showSyncStatus(ivStatusLokasi, false, "Sinkron Lokasi Gagal");

            }
        });
    }


    public void singkronKegiatan(){
        ivStatusKegiatan.setVisibility(View.GONE);
        pgSingkronKegiatan.setVisibility(View.VISIBLE);

        Call<List<KegiatanIzin>> callKegiatan = httpService.getUrlKegiatanNew("https://absensi.tebingtinggikota.go.id/api/kegiatannew?opd="+sOPD);
        callKegiatan.enqueue(new Callback<List<KegiatanIzin>>() {

            @Override
            public void onResponse(@NonNull Call<List<KegiatanIzin>> call, @NonNull Response<List<KegiatanIzin>> response) {
                List<KegiatanIzin> kegiatanIzins = response.body();
                if (!response.isSuccessful()){
                    dialogView.viewNotifKosong(DashboardVersiOne.this, "Gagal melakukan singkronisasi kegiatan.","Silahkan coba kembali.");
                    pgSingkronKegiatan.setVisibility(View.GONE);
                    showSyncStatus(ivStatusKegiatan, false, "Sinkron Kegiatan Gagal");
                    return;
                }

                Integer deleteKegiatan = databaseHelper.deleteKegiatanIzin();
                if (deleteKegiatan>0){
                    for (KegiatanIzin kegiatanIzin : kegiatanIzins){
                        databaseHelper.insertResourceKegiatan(String.valueOf(kegiatanIzin.getId()), kegiatanIzin.getTipe(), kegiatanIzin.getKet());
                    }

                }

                pgSingkronKegiatan.setVisibility(View.GONE);
                showSyncStatus(ivStatusKegiatan, true, "Sinkron Kegiatan Berhasil");
            }

            @Override
            public void onFailure(@NonNull Call<List<KegiatanIzin>> call, @NonNull Throwable t) {
                dialogView.viewNotifKosong(DashboardVersiOne.this, "Gagal mengakses server.", "Silahkan coba kembali.");
                pgSingkronKegiatan.setVisibility(View.GONE);
                showSyncStatus(ivStatusKegiatan, false, "Sinkron Kegiatan Gagal");
            }
        });
    }

    private void showSyncStatus(ImageView statusIcon, boolean isSuccess, String message) {
        statusIcon.setVisibility(View.VISIBLE);
        cvSyncStatusToast.setVisibility(View.VISIBLE);
        tvSyncStatusMessage.setText(message);

        if (isSuccess) {
            statusIcon.setImageResource(R.drawable.ic_check);
            statusIcon.setColorFilter(ContextCompat.getColor(this, R.color.hijau));

            ivSyncStatusIcon.setImageResource(R.drawable.ic_check);
            ivSyncStatusIcon.setColorFilter(ContextCompat.getColor(this, R.color.hijau));
        } else {
            statusIcon.setImageResource(R.drawable.one_warning);
            statusIcon.setColorFilter(ContextCompat.getColor(this, R.color.merah));

            ivSyncStatusIcon.setImageResource(R.drawable.one_warning);
            ivSyncStatusIcon.setColorFilter(ContextCompat.getColor(this, R.color.merah));
        }

        // Sembunyikan ikon dan toast setelah 5 detik agar UI tetap bersih
        statusIcon.postDelayed(() -> {
            statusIcon.setVisibility(View.GONE);
            cvSyncStatusToast.setVisibility(View.GONE);
        }, 5000);
    }

}