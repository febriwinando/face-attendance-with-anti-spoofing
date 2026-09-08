package go.pemkott.appsandroidmobiletebingtinggi.login;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import go.pemkott.appsandroidmobiletebingtinggi.NewDashboard.DashboardVersiOne;
import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.api.ResponsePOJO;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.dialogview.DialogView;
import go.pemkott.appsandroidmobiletebingtinggi.model.DataEmployee;
import go.pemkott.appsandroidmobiletebingtinggi.model.KegiatanIzin;
import go.pemkott.appsandroidmobiletebingtinggi.model.Koordinat;
import go.pemkott.appsandroidmobiletebingtinggi.model.TimeTables;
import go.pemkott.appsandroidmobiletebingtinggi.model.WaktuSift;
import go.pemkott.appsandroidmobiletebingtinggi.utils.NetworkUtils;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class DownloadDataActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvInfo, tvProgressPercentage;
    private ImageView ivSyncIllustration;
    private View vPulse1, vPulse2;

    private DatabaseHelper db;
    private DialogView dialogView;
    private HttpService api;

    private ExecutorService executor;

    private String userId;
    private String employeeId;
    private String token;
    private String opd;

    private int progressStep = 0;
    private static final int MAX_STEP = 5;
    SessionManager session;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_data);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        progressBar = findViewById(R.id.progressBarHorizontal);
        tvInfo = findViewById(R.id.tvinfoDownload);
        tvProgressPercentage = findViewById(R.id.tvProgressPercentage);
        ivSyncIllustration = findViewById(R.id.ivSyncIllustration);
        vPulse1 = findViewById(R.id.vPulse1);
        vPulse2 = findViewById(R.id.vPulse2);

        startEnhancedAnimations();

        db = new DatabaseHelper(this);
        dialogView = new DialogView(this);
        executor = Executors.newSingleThreadExecutor();

        session = new SessionManager(this);
        userId = session.getPegawaiId();

        api = RetroClient.getInstance().getApi2();

        if (!NetworkUtils.isConnectedFast(this)) {
            dialogView.viewNotifKosong(this,
                    "Tidak ada koneksi internet",
                    "Silakan cek jaringan Anda");
            return;
        }

        bacaUserLokal();
    }

    /* =============================
       STEP 0 : USER LOKAL
       ============================= */
    private void bacaUserLokal() {
        Cursor c = db.getAllData22(userId);
        if (c.moveToFirst()) {
            employeeId = c.getString(1);
            token = c.getString(5);
            c.close();
            stepPegawai();
        } else {
            c.close();
            errorStop("Data user tidak ditemukan");
        }
    }

    /* =============================
       STEP 1 : DATA PEGAWAI
       ============================= */
    private void stepPegawai() {
        updateUI("Mengunduh data pegawai...");
        api.dataEmployee(employeeId).enqueue(new Callback<DataEmployee>() {
            @Override
            public void onResponse(Call<DataEmployee> call, Response<DataEmployee> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    errorStop("Gagal unduh data pegawai");
                    return;
                }

                executor.execute(() -> {
                    DataEmployee d = res.body();
                    db.insertDataEmployee(
                            d.getId(), d.getAtasan_id1(), d.getAtasan_id2(),
                            d.getPosition_id(), d.getOpd_id(), d.getNip(),
                            d.getNama(), d.getEmail(), d.getNo_hp(),
                            d.getKelompok(), d.getS_jabatan(), d.getEselon(),
                            d.getJabatan(), d.getOpd(), d.getAlamat(),
                            d.getLet(), d.getLng(), d.getFoto(),
                            d.getAwal_waktu(), String.valueOf(d.getShift())
                    );
                    opd = d.getOpd_id();

                    runOnUiThread(() -> stepKoordinatOPD());
                });
            }

            @Override
            public void onFailure(Call<DataEmployee> call, Throwable t) {
                errorStop(t.getMessage());
            }
        });
    }

    /* =============================
       STEP 2 : KOORDINAT OPD
       ============================= */
    private void stepKoordinatOPD() {
        updateUI("Mengunduh koordinat OPD...");
        api.getUrlKoordinat(
                "https://absensi.tebingtinggikota.go.id/api/koordinat",
                RequestBody.create("", MediaType.parse("application/json"))
        ).enqueue(new Callback<List<Koordinat>>() {

            @Override
            public void onResponse(Call<List<Koordinat>> call, Response<List<Koordinat>> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    errorStop("Gagal unduh koordinat OPD");
                    return;
                }

                executor.execute(() -> {
                    for (Koordinat k : res.body()) {
                        db.insertDataKoordinat(
                                k.getId(), k.getOpd_id(),
                                k.getAlamat(), k.getLet(), k.getLng()
                        );
                    }
                    runOnUiThread(() -> koordinat_e());
                });
            }

            @Override
            public void onFailure(Call<List<Koordinat>> call, Throwable t) {
                errorStop(t.getMessage());
            }
        });
    }


        private void koordinat_e() {

            updateUI("Mengunduh koordinat pegawai ...");
            String url = "https://absensi.tebingtinggikota.go.id/api/koordinatemployee?id=" + employeeId;

            api.getUrlKoordinat(url, "Bearer " + token,
                            RequestBody.create("", MediaType.parse("application/json")))
                    .enqueue(new Callback<List<Koordinat>>() {

                        @Override
                        public void onResponse(Call<List<Koordinat>> call, Response<List<Koordinat>> response) {
                            if (!response.isSuccessful() || response.body() == null) {
                                errorStop("Gagal unduh koordinat pegawai");
                                return;
                            }

                            executor.execute(() -> {
                                for (Koordinat koordinat : response.body()) {
                                    db.insertDataKoordinatEmployee(koordinat.getId(), employeeId, koordinat.getAlamat(), koordinat.getLet(), koordinat.getLng());
                                }
                                runOnUiThread(() -> stepTimetable());
                            });
                        }

                        @Override
                        public void onFailure(Call<List<Koordinat>> call, Throwable t) {
                            errorStop(t.getMessage());
                        }
                    });

    }

    /* =============================
       STEP 3 : TIMETABLE
       ============================= */
    private void stepTimetable() {
        updateUI("Mengunduh timetable...");
        String url = "https://absensi.tebingtinggikota.go.id/api/timetable?employee_id=" + employeeId;

        api.getUrlTimeTable(url, "Bearer " + token,
                        RequestBody.create("", MediaType.parse("application/json")))
                .enqueue(new Callback<List<TimeTables>>() {

                    @Override
                    public void onResponse(Call<List<TimeTables>> call, Response<List<TimeTables>> res) {
                        if (!res.isSuccessful() || res.body() == null) {
                            errorStop("Gagal unduh timetable");
                            return;
                        }

                        executor.execute(() -> {
                            for (TimeTables t : res.body()) {
                                db.insertDataTimeTable(
                                        String.valueOf(t.getId()),
                                        t.getEmployee_id(),
                                        t.getTimetable_id(),
                                        t.getInisial(),
                                        String.valueOf(t.getHari()),
                                        t.getMasuk(),
                                        t.getPulang()
                                );
                            }
                            runOnUiThread(() -> stepKegiatan());
                        });
                    }

                    @Override
                    public void onFailure(Call<List<TimeTables>> call, Throwable t) {
                        errorStop(t.getMessage());
                    }
                });
    }

    /* =============================
       STEP 4 : KEGIATAN
       ============================= */
    private void stepKegiatan() {
        updateUI("Mengunduh kegiatan...");
        String url = "https://absensi.tebingtinggikota.go.id/api/kegiatannew?opd=" + opd;

        api.getUrlKegiatan(url, "Bearer " + token,
                        RequestBody.create("", MediaType.parse("application/json")))
                .enqueue(new Callback<List<KegiatanIzin>>() {

                    @Override
                    public void onResponse(Call<List<KegiatanIzin>> call, Response<List<KegiatanIzin>> res) {
                        if (!res.isSuccessful() || res.body() == null) {
                            errorStop("Gagal unduh kegiatan");
                            return;
                        }

                        executor.execute(() -> {
                            for (KegiatanIzin k : res.body()) {
                                db.insertResourceKegiatan(
                                        String.valueOf(k.getId()),
                                        k.getTipe(),
                                        k.getKet()
                                );
                            }
                            runOnUiThread(() -> stepSift());
                        });
                    }

                    @Override
                    public void onFailure(Call<List<KegiatanIzin>> call, Throwable t) {
                        errorStop(t.getMessage());
                    }
                });
    }

    /* =============================
       STEP 5 : SIFT
       ============================= */
    private void stepSift() {
        updateUI("Mengunduh jam sift...");
        String url = "https://absensi.tebingtinggikota.go.id/api/testsift?eOPD=" + opd;

        api.getTestSift(url,
                        RequestBody.create("", MediaType.parse("application/json")))
                .enqueue(new Callback<List<WaktuSift>>() {

                    @Override
                    public void onResponse(Call<List<WaktuSift>> call, Response<List<WaktuSift>> res) {
                        if (!res.isSuccessful() || res.body() == null) {
                            errorStop("Gagal unduh jam sift");
                            return;
                        }

                        executor.execute(() -> {
                            for (WaktuSift w : res.body()) {
                                db.insertJamShift(
                                        String.valueOf(w.getId()),
                                        String.valueOf(w.getOpd_id()),
                                        String.valueOf(w.getTipe()),
                                        w.getInisial(),
                                        w.getMasuk(),
                                        w.getPulang()
                                );
                            }
                            runOnUiThread(() -> sendFcmTokenToServer());
                        });
                    }

                    @Override
                    public void onFailure(Call<List<WaktuSift>> call, Throwable t) {
                        errorStop(t.getMessage());
                    }
                });
    }


//    private void sendFcmTokenToServer() {
//        updateUI("menyelesaikan proses...");
//        SessionManager session = new SessionManager(this);
//        String pegawaiId = session.getPegawaiId();
//        String fcmToken  = session.getFcmToken();
//
//        if (pegawaiId == null || pegawaiId.equals("0") || fcmToken == null) {
//            Log.w("FCM Keluar", "Pegawai ID / FCM Token belum siap, skip update");
//            return;
//        }
//
//        Call<ResponsePOJO> call = RetroClient.getInstance()
//                .getApi()
//                .updateFcmToken(pegawaiId, fcmToken);
//
//        call.enqueue(new Callback<ResponsePOJO>() {
//            @Override
//            public void onResponse(Call<ResponsePOJO> call, Response<ResponsePOJO> response) {
//                executor.execute(() -> {
//                    runOnUiThread(() -> finishFlow());
//                });
//            }
//
//            @Override
//            public void onFailure(Call<ResponsePOJO> call, Throwable t) {
//                Log.e("FCM", "Error update token: " + t.getMessage());
//            }
//        });
//    }

    private void sendFcmTokenToServer() {

        SessionManager session = new SessionManager(this);
        String pegawaiId = session.getPegawaiId();

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        Log.e("FCM", "Gagal mendapatkan token");
                        return;
                    }

                    String token = task.getResult();

                    Log.d("FCM", "pegawaiId = " + pegawaiId);
                    Log.d("FCM", "token = " + token);

                    if (pegawaiId == null ||
                            pegawaiId.equals("0") ||
                            token == null ||
                            token.isEmpty()) {

                        return;
                    }

                    RetroClient.getInstance()
                            .getApi()
                            .updateFcmToken(pegawaiId, token)
                            .enqueue(new Callback<ResponsePOJO>() {

                                @Override
                                public void onResponse(
                                        Call<ResponsePOJO> call,
                                        Response<ResponsePOJO> response) {

                                    finishFlow();
                                }

                                @Override
                                public void onFailure(
                                        Call<ResponsePOJO> call,
                                        Throwable t) {

                                    Log.e("FCM",
                                            "Update gagal : "
                                                    + t.getMessage());
                                }
                            });
                });
    }

    /* =============================
       FINISH
       ============================= */
    private void finishFlow() {
        updateUI("Selesai");
        startActivity(new Intent(this, DashboardVersiOne.class));
        finish();
    }


    /* =============================
       HELPER
       ============================= */
    private void startEnhancedAnimations() {
        // Pulse 1 Animation (Outer)
        ObjectAnimator p1ScaleX = ObjectAnimator.ofFloat(vPulse1, "scaleX", 1f, 1.4f);
        ObjectAnimator p1ScaleY = ObjectAnimator.ofFloat(vPulse1, "scaleY", 1f, 1.4f);
        ObjectAnimator p1Alpha = ObjectAnimator.ofFloat(vPulse1, "alpha", 0.1f, 0f);
        
        p1ScaleX.setRepeatCount(ValueAnimator.INFINITE);
        p1ScaleY.setRepeatCount(ValueAnimator.INFINITE);
        p1Alpha.setRepeatCount(ValueAnimator.INFINITE);

        // Pulse 2 Animation (Inner)
        ObjectAnimator p2ScaleX = ObjectAnimator.ofFloat(vPulse2, "scaleX", 1f, 1.3f);
        ObjectAnimator p2ScaleY = ObjectAnimator.ofFloat(vPulse2, "scaleY", 1f, 1.3f);
        ObjectAnimator p2Alpha = ObjectAnimator.ofFloat(vPulse2, "alpha", 0.15f, 0f);
        
        p2ScaleX.setRepeatCount(ValueAnimator.INFINITE);
        p2ScaleY.setRepeatCount(ValueAnimator.INFINITE);
        p2Alpha.setRepeatCount(ValueAnimator.INFINITE);
        p2ScaleX.setStartDelay(1000);
        p2ScaleY.setStartDelay(1000);
        p2Alpha.setStartDelay(1000);

        // Main Icon Breathing
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(ivSyncIllustration, "scaleX", 1f, 1.05f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(ivSyncIllustration, "scaleY", 1f, 1.05f, 1f);
        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(p1ScaleX, p1ScaleY, p1Alpha, p2ScaleX, p2ScaleY, p2Alpha, scaleX, scaleY);
        animatorSet.setDuration(2000);
        animatorSet.start();
    }

    private void updateUI(String text) {
        progressStep++;
        int percentage = (progressStep * 100) / MAX_STEP;
        tvInfo.setText(text);
        progressBar.setProgress(percentage);
        tvProgressPercentage.setText(String.format(Locale.getDefault(), "%d%%", percentage));
    }

    private void errorStop(String msg) {
        dialogView.viewNotifKosong(this, "Terjadi kesalahan", msg);
        if (executor != null) executor.shutdownNow();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) executor.shutdownNow();
    }

    private void debug(String pesan){
        Log.e("DOWNLOAD_DEBUG", pesan);

        runOnUiThread(() ->
                Toast.makeText(
                        DownloadDataActivity.this,
                        pesan,
                        Toast.LENGTH_LONG
                ).show()
        );
    }
}
