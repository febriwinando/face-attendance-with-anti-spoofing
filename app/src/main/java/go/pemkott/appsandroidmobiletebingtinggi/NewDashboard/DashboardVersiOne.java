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

import com.bumptech.glide.signature.ObjectKey;
import com.google.android.material.card.MaterialCardView;
import com.bumptech.glide.Glide;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallException;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallErrorCode;
import com.google.android.play.core.install.model.UpdateAvailability;


import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import go.pemkott.appsandroidmobiletebingtinggi.ProfileActivity;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.api.ServerConfig;
import go.pemkott.appsandroidmobiletebingtinggi.helpdesk.HelpdeskActivity;
import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.dialogview.DialogView;
import go.pemkott.appsandroidmobiletebingtinggi.dinasluarkantor.perjalanandinas.JadwalPerjalananDinasShiftActivity;
import go.pemkott.appsandroidmobiletebingtinggi.dinasluarkantor.perjalanandinas.SppdActivity;
import go.pemkott.appsandroidmobiletebingtinggi.izinshift.JadwalIzinShiftActivity;
import go.pemkott.appsandroidmobiletebingtinggi.kehadiransift.JadwalShiftActivity;
import go.pemkott.appsandroidmobiletebingtinggi.login.DownloadDataActivity;
import go.pemkott.appsandroidmobiletebingtinggi.login.LoginActivity;
import go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager;

import go.pemkott.appsandroidmobiletebingtinggi.model.KegiatanIzin;
import go.pemkott.appsandroidmobiletebingtinggi.model.Koordinat;
import go.pemkott.appsandroidmobiletebingtinggi.model.ValidasiData;
import go.pemkott.appsandroidmobiletebingtinggi.rekap.RekapAbsensActivity;
import go.pemkott.appsandroidmobiletebingtinggi.singkronjadwal.SettingAdapter;
import go.pemkott.appsandroidmobiletebingtinggi.singkronjadwal.TimeTebleSetting;
import go.pemkott.appsandroidmobiletebingtinggi.singkronjadwalsift.CalendarJadwalShiftActivity;
import go.pemkott.appsandroidmobiletebingtinggi.utils.FcmTokenHelper;
import go.pemkott.appsandroidmobiletebingtinggi.utils.NetworkUtil;
import go.pemkott.appsandroidmobiletebingtinggi.verifikasi.ValidasiNewActivity;
import go.pemkott.appsandroidmobiletebingtinggi.adapter.berita.BeritaCardAdapter;
import go.pemkott.appsandroidmobiletebingtinggi.adapter.berita.BeritaUtamaAdapter;
import go.pemkott.appsandroidmobiletebingtinggi.model.berita.BeritaData;
import go.pemkott.appsandroidmobiletebingtinggi.model.berita.BeritaItem;
import go.pemkott.appsandroidmobiletebingtinggi.model.berita.BeritaResponse;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.net.Uri;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DashboardVersiOne extends AppCompatActivity {

    CardView cvKehadiran, cvJadwal, cvLokasi, cvKegiatan, cvMenuIzin, cvMenuPerjalananDinas;
    private static final long MIN_STORAGE_MB = 300;
//    private static final long MIN_STORAGE_GB = 40;
    LocationManager manager;
    DatabaseHelper databaseHelper;
    HttpService httpService;

    ConstraintLayout clSectionBeritaDiskominfo;
    RecyclerView rvBeritaUtama, rvBeritaTerbaru, rvBeritaTrending;
    TextView tvLihatSemuaBerita;
    BeritaUtamaAdapter beritaUtamaAdapter;
    BeritaCardAdapter beritaTerbaruAdapter;
    BeritaCardAdapter beritaTrendingAdapter;
    private static boolean hasShownHeadlineModal = false;

    public static String statusSift, fotoProfile, jam_masuk, jam_pulang, sOPD, sNip, sJabatan, sKantor, sEmployee_id, sUsername, sAkses, sActive,  sToken, sVerifikator;

    TextView tvNamaUser, tvTanggalHariIni, tvJamBerjalan;
    public static int jenisabsensi;
    DialogView dialogView = new DialogView(DashboardVersiOne.this);

    private final Handler clockHandler = new Handler(Looper.getMainLooper());
    private Runnable clockRunnable;

    ProgressBar pgSingkronLokasi, pgSingkronKegiatan, pgSingkronJadwal;
    ImageView ivStatusJadwal, ivStatusLokasi, ivStatusKegiatan;
    CircleImageView ciUser;

    CardView cvSyncStatusToast;
    ImageView ivSyncStatusIcon;
    TextView tvSyncStatusMessage;
    MaterialCardView clVerifikasi, cvSuperAdminHub;
    public static DashboardVersiOne dashboardVersiOne;
    SwipeRefreshLayout swipeRefreshLayout;

    ConstraintLayout clCariRekap;
    SessionManager session;

    AppUpdateManager appUpdateManager;
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
        appUpdateManager = AppUpdateManagerFactory.create(this);
        checkAndHandleAppUpdate();
        
        userid = session.getPegawaiId();
        dashboardVersiOne = this;
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        databaseHelper = new DatabaseHelper(this);
        httpService = RetroClient.getInstance().getApi2();

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
        cvSuperAdminHub = findViewById(R.id.cvSuperAdminHub);
        clCariRekap = findViewById(R.id.clCariRekap);
        tvTanggalHariIni = findViewById(R.id.tvTanggalHariIni);
        tvJamBerjalan = findViewById(R.id.tvJamBerjalan);
        pgSingkronLokasi = findViewById(R.id.pgSingkronLokasi);
        pgSingkronKegiatan = findViewById(R.id.pgSingkronKegiatan);
        pgSingkronJadwal = findViewById(R.id.pgSingkronJadwal);
        ivStatusJadwal = findViewById(R.id.ivStatusJadwal);
        ivStatusLokasi = findViewById(R.id.ivStatusLokasi);
        ivStatusKegiatan = findViewById(R.id.ivStatusKegiatan);

        cvSyncStatusToast = findViewById(R.id.cvSyncStatusToast);
        ivSyncStatusIcon = findViewById(R.id.ivSyncStatusIcon);
        tvSyncStatusMessage = findViewById(R.id.tvSyncStatusMessage);

        clSectionBeritaDiskominfo = findViewById(R.id.clSectionBeritaDiskominfo);
        rvBeritaUtama = findViewById(R.id.rvBeritaUtama);
        rvBeritaTerbaru = findViewById(R.id.rvBeritaTerbaru);
        rvBeritaTrending = findViewById(R.id.rvBeritaTrending);
        tvLihatSemuaBerita = findViewById(R.id.tvLihatSemuaBerita);

        if (rvBeritaUtama != null) {
            rvBeritaUtama.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            beritaUtamaAdapter = new BeritaUtamaAdapter(this);
            rvBeritaUtama.setAdapter(beritaUtamaAdapter);
        }

        if (rvBeritaTerbaru != null) {
            rvBeritaTerbaru.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            beritaTerbaruAdapter = new BeritaCardAdapter(this);
            rvBeritaTerbaru.setAdapter(beritaTerbaruAdapter);
        }

        if (rvBeritaTrending != null) {
            rvBeritaTrending.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            beritaTrendingAdapter = new BeritaCardAdapter(this);
            rvBeritaTrending.setAdapter(beritaTrendingAdapter);
        }

        if (tvLihatSemuaBerita != null) {
            tvLihatSemuaBerita.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardVersiOne.this, go.pemkott.appsandroidmobiletebingtinggi.berita.ListBeritaActivity.class);
                startActivity(intent);
            });
        }

        loadBeritaDiskominfo();

        datauser();

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
            private long lastClickTime = 0;
            @Override
            public void onClick(View v) {
                if (System.currentTimeMillis() - lastClickTime < 1000) return;
                lastClickTime = System.currentTimeMillis();

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


        updateUserInfoUI();

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

                if ("0".equals(statusSift)) {
                    jenisabsensi = 3;
                    startActivity(new Intent(DashboardVersiOne.this, SppdActivity.class));
                } else {
                    jenisabsensi = 12;
                    startActivity(new Intent(DashboardVersiOne.this, JadwalPerjalananDinasShiftActivity.class));
                }
            }
        });


        clVerifikasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardVersiOne.this, ValidasiNewActivity.class));
            }
        });

        if (cvSuperAdminHub != null) {
            cvSuperAdminHub.setOnClickListener(v -> showSuperAdminMenuDialog());
        }

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
                loadBeritaDiskominfo();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        initLiveClock();
    }

    private void loadBeritaDiskominfo() {
        if (httpService == null) return;
        String localUrl = "http://172.15.1.13:4321/api/berita";
        String publicUrl = "https://diskominfo.tebingtinggikota.go.id/api/berita";

        fetchBeritaFromUrl(localUrl, () -> fetchBeritaFromUrl(publicUrl, null));
    }

    private void fetchBeritaFromUrl(String url, Runnable onFailureFallback) {
        httpService.getBeritaDiskominfo(url).enqueue(new Callback<BeritaResponse>() {
            @Override
            public void onResponse(@NonNull Call<BeritaResponse> call, @NonNull Response<BeritaResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    BeritaData data = response.body().getData();
                    if (data != null) {
                        boolean hasData = false;
                        if (data.getBeritaUtama() != null && !data.getBeritaUtama().isEmpty()) {
                            if (beritaUtamaAdapter != null) {
                                beritaUtamaAdapter.setList(data.getBeritaUtama());
                            }
                            hasData = true;
                        }
                        if (data.getBeritaTerbaru() != null && !data.getBeritaTerbaru().isEmpty()) {
                            if (beritaTerbaruAdapter != null) {
                                beritaTerbaruAdapter.setList(data.getBeritaTerbaru());
                            }
                            hasData = true;
                        }
                        if (data.getBeritaTrending() != null && !data.getBeritaTrending().isEmpty()) {
                            if (beritaTrendingAdapter != null) {
                                beritaTrendingAdapter.setList(data.getBeritaTrending());
                            }
                            hasData = true;
                        }
                        if (hasData && clSectionBeritaDiskominfo != null) {
                            clSectionBeritaDiskominfo.setVisibility(View.VISIBLE);
                        }

                        if (!hasShownHeadlineModal && data.getBeritaUtama() != null && !data.getBeritaUtama().isEmpty()) {
                            hasShownHeadlineModal = true;
                            BeritaItem headline = data.getBeritaUtama().get(0);
                            go.pemkott.appsandroidmobiletebingtinggi.berita.DialogBeritaKhusus.showModal(DashboardVersiOne.this, headline);
                        }
                    }
                } else if (onFailureFallback != null) {
                    onFailureFallback.run();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BeritaResponse> call, @NonNull Throwable t) {
                Log.w("BeritaDiskominfo", "Gagal load dari " + url + ": " + t.getMessage());
                if (onFailureFallback != null) {
                    onFailureFallback.run();
                }
            }
        });
    }

    private void initLiveClock() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("id", "ID"));
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        clockRunnable = new Runnable() {
            @Override
            public void run() {
                Date now = new Date();
                tvTanggalHariIni.setText(dateFormat.format(now));
                tvJamBerjalan.setText(timeFormat.format(now));
                clockHandler.postDelayed(this, 1000);
            }
        };
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

    private void checkAndHandleAppUpdate() {
        int currentVersionCode = 0;
        try {
            currentVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (Exception e) {
            Log.e("UPDATE_HANDLER", "Error getting version code", e);
        }

        int lastSavedVersion = session.getVersionCode();

        if (lastSavedVersion != 0 && currentVersionCode > lastSavedVersion) {
            Log.d("UPDATE_HANDLER", "App updated from " + lastSavedVersion + " to " + currentVersionCode);
            session.saveVersionCode(currentVersionCode);
            
            // Redirect to DownloadDataActivity to ensure data migration and session integrity
            Intent intent = new Intent(this, DownloadDataActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else if (currentVersionCode != 0) {
            session.saveVersionCode(currentVersionCode);
        }
    }

    private void checkAppUpdate() {
        if (appUpdateManager == null) {
            appUpdateManager = AppUpdateManagerFactory.create(this);
        }

        appUpdateManager.getAppUpdateInfo()
                .addOnSuccessListener(appUpdateInfo -> {

                    Log.d("UPDATE_HANDLER",
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
                            Log.e("UPDATE_HANDLER", "Failed to start update flow", e);
                        }
                    }

                })
                .addOnFailureListener(e -> {
                    if (e instanceof InstallException) {
                        int errorCode = ((InstallException) e).getErrorCode();
                        if (errorCode == InstallErrorCode.ERROR_APP_NOT_OWNED) {
                            Log.w("UPDATE_HANDLER", "Aplikasi tidak diinstal dari Play Store (sideloaded). Abaikan cek update.");
                            return;
                        }
                    }

                    // Log as warning since this is often an internal Play Store auth issue [6474]
                    // that doesn't affect app functionality.
                    Log.w("UPDATE_HANDLER", "Cek update gagal (non-critical): " + e.getMessage());
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

        FcmTokenHelper.fetchAndSendToken(this, null);

        datauser();

        if (sVerifikator != null && sEmployee_id != null){
            dataValidasi(sVerifikator, sEmployee_id);
        }

        if (clockRunnable != null) {
            clockHandler.post(clockRunnable);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (clockRunnable != null) {
            clockHandler.removeCallbacks(clockRunnable);
        }
    }

    private void showSuperAdminMenuDialog() {
        String[] options = {
            "Update Data Masuk Absensi",
            "Input Absensi Massal",
            "Kelola & Sinkron Data Pegawai",
            "Kelola Lokasi Pegawai",
            "Pengolahan & Edit Absensi Pegawai"
        };

        new AlertDialog.Builder(this)
                .setTitle("Panel Pengelolaan Super Admin")
                .setItems(options, (dialog, which) -> {
                    Intent intent;
                    switch (which) {
                        case 0:
                            intent = new Intent(DashboardVersiOne.this, go.pemkott.appsandroidmobiletebingtinggi.admin.AdminUpdateMasukActivity.class);
                            break;
                        case 1:
                            intent = new Intent(DashboardVersiOne.this, go.pemkott.appsandroidmobiletebingtinggi.admin.AdminInsertMassalActivity.class);
                            break;
                        case 2:
                            intent = new Intent(DashboardVersiOne.this, go.pemkott.appsandroidmobiletebingtinggi.EmployeeSyncActivity.class);
                            break;
                        case 3:
                            intent = new Intent(DashboardVersiOne.this, go.pemkott.appsandroidmobiletebingtinggi.admin.AdminLokasiPegawaiActivity.class);
                            break;
                        default:
                            intent = new Intent(DashboardVersiOne.this, go.pemkott.appsandroidmobiletebingtinggi.admin.AdminPengolahanAbsensiActivity.class);
                            break;
                    }
                    startActivity(intent);
                })
                .setNegativeButton("Tutup", null)
                .show();
    }

    public void dataValidasi(String verifikator, String idE){
        if ("verifikator1".equalsIgnoreCase(verifikator) || "verifikator2".equalsIgnoreCase(verifikator)){
            Call<List<ValidasiData>> callKegiatan = httpService.getUrlListValidasi(ServerConfig.getApiUrl() + "newVeriFragment?verifikator="+verifikator+"&id="+idE);
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
        if (userid == null || "0".equals(userid)) {
            redirectToLogin("Session tidak valid, silakan login kembali.");
            return;
        }

        Cursor res = databaseHelper.getAllData22(userid);
        if (res.getCount() == 0){
            res.close();
            redirectToLogin("Data pengguna tidak sinkron, silakan login kembali.");
            return;
        }

        String loginUser = "";
        try {
            if (res.moveToFirst()){
                int empIdIdx = res.getColumnIndex(DatabaseHelper.COL_2);
                int nameIdx = res.getColumnIndex(DatabaseHelper.COL_3);
                int aksesIdx = res.getColumnIndex(DatabaseHelper.COL_4);
                int activeIdx = res.getColumnIndex(DatabaseHelper.COL_5);
                int tokenIdx = res.getColumnIndex(DatabaseHelper.COL_6);
                int verifIdx = res.getColumnIndex(DatabaseHelper.COL_7);

                sEmployee_id = (empIdIdx != -1) ? res.getString(empIdIdx) : res.getString(1);
                loginUser = (nameIdx != -1) ? res.getString(nameIdx) : res.getString(2);
                sAkses = (aksesIdx != -1) ? res.getString(aksesIdx) : res.getString(3);
                sActive = (activeIdx != -1) ? res.getString(activeIdx) : res.getString(4);
                sToken = (tokenIdx != -1) ? res.getString(tokenIdx) : res.getString(5);
                sVerifikator = (verifIdx != -1) ? res.getString(verifIdx) : res.getString(6);
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

        boolean isSuperAdminExclusive = "199402092020121005".equalsIgnoreCase(sNip)
                || "199402092020121005".equalsIgnoreCase(sUsername)
                || "199402092020121005".equalsIgnoreCase(loginUser)
                || "199402092020121005".equalsIgnoreCase(session.getUsername())
                || "199402092020121005".equalsIgnoreCase(session.getEmployeeId());

        if (isSuperAdminExclusive) {
            if (cvSuperAdminHub != null) {
                cvSuperAdminHub.setVisibility(View.VISIBLE);
            }
        } else {
            if (cvSuperAdminHub != null) {
                cvSuperAdminHub.setVisibility(View.GONE);
            }
        }
    }


    List<TimeTebleSetting> timeTable = new ArrayList<>();

    public void viewJadwalKerja(){
        timeTable.clear();
        Toast.makeText(dashboardVersiOne, "jadwal", Toast.LENGTH_SHORT).show();

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

                Call<List<TimeTebleSetting>> callKegiatan = httpService.getUrlTimeTableSetting(ServerConfig.getApiUrl() + "timetable?employee_id="+sEmployee_id, "Bearer "+sToken);
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

        Call<List<Koordinat>> calllokasi = httpService.getUrlKoordinat(ServerConfig.getApiUrl() + "koordinat?opdid="+sOPD);
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

        Call<List<Koordinat>> callKegiatan = httpService.getUrlKoordinat(ServerConfig.getApiUrl() + "koordinatemployee?id="+sEmployee_id);
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

        Call<List<KegiatanIzin>> callKegiatan = httpService.getUrlKegiatanNew(ServerConfig.getApiUrl() + "kegiatannew?opd="+sOPD);
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

    public void updateProfileUI() {
        refreshDashboardUI();
    }

    public void refreshDashboardUI() {
        datauser(); // Reload static variables from DB
        runOnUiThread(() -> {
            if (sUsername != null && sUsername.length() > 13) {
                String truncatedName = sUsername.substring(0, 13) + "...";
                tvNamaUser.setText(truncatedName);
            } else {
                tvNamaUser.setText(sUsername);
            }

            Glide.with(this)
                    .load(ServerConfig.getStorageUrl() + "foto-pegawai/" + fotoProfile)
                    .placeholder(R.drawable.profil_pic)
                    .error(R.drawable.profil_pic)
                    .signature(new ObjectKey(System.currentTimeMillis()))
                    .into(ciUser);
        });
    }

    private void redirectToLogin(String pesan) {
        if (session != null) {
            session.clearSession();
        }
        runOnUiThread(() -> {
            Toast.makeText(DashboardVersiOne.this, pesan, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(DashboardVersiOne.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void updateUserInfoUI() {
        if (sUsername != null && sUsername.length() > 13) {
            String truncatedName = sUsername.substring(0, 13) + "...";
            tvNamaUser.setText(truncatedName);
        } else {
            tvNamaUser.setText(sUsername);
        }

        Glide.with(this)
                .load(ServerConfig.getStorageUrl() + "foto-pegawai/" + fotoProfile)
                .placeholder(R.drawable.profil_pic)
                .error(R.drawable.profil_pic)
                .into(ciUser);
    }

}