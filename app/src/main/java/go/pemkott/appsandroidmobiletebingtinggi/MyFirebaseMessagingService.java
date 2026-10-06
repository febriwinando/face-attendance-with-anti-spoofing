package go.pemkott.appsandroidmobiletebingtinggi;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;
import java.util.Map;

import go.pemkott.appsandroidmobiletebingtinggi.NewDashboard.DashboardVersiOne;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.database.ModelDataPagawai;
import go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager;
import go.pemkott.appsandroidmobiletebingtinggi.model.DataEmployee;
import go.pemkott.appsandroidmobiletebingtinggi.model.KegiatanIzin;
import go.pemkott.appsandroidmobiletebingtinggi.model.Koordinat;
import go.pemkott.appsandroidmobiletebingtinggi.model.TimeTables;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import go.pemkott.appsandroidmobiletebingtinggi.utils.FcmTokenHelper;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    DatabaseHelper databaseHelper = new DatabaseHelper(this);

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FCM", "Token: " + token);
        SessionManager session = new SessionManager(this);
        session.saveFcmToken(token);
        String pegawaiId = session.getPegawaiId();
        if (pegawaiId != null && !"0".equals(pegawaiId)) {
            FcmTokenHelper.sendTokenToServer(this, pegawaiId, token, null);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d("FCM_SERVICE", "Message Received from: " + remoteMessage.getFrom());

        String title = null;
        String body = null;

        // 1. Check Notification Payload (Sangat penting jika dikirim bersamaan)
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
            Log.d("FCM_SERVICE", "Notification Payload - Title: " + title + ", Body: " + body);
        }

        // 2. Check Data Payload
        if (remoteMessage.getData().size() > 0) {
            Log.d("FCM_SERVICE", "Data Payload: " + remoteMessage.getData().toString());
            String dataTitle = remoteMessage.getData().get("title");
            String dataBody = remoteMessage.getData().get("body");

            if (title == null) title = dataTitle;
            if (body == null) body = dataBody;
        }

        // 3. Ambil keputusan perintah sinkronisasi dari data payload atau judul notifikasi visual
        String finalCommand = null;
        if (remoteMessage.getData().size() > 0) {
            finalCommand = remoteMessage.getData().get("title");
            if (finalCommand == null) finalCommand = remoteMessage.getData().get("body");
        }
        
        // Jika di dalam data payload tidak ada command, gunakan title/body dari objek notifikasi Laravel
        if (finalCommand == null) {
            finalCommand = title;
        }

        if (finalCommand != null) {
            finalCommand = finalCommand.trim();
            Log.d("FCM_SERVICE", "Processing Final Command: " + finalCommand);
            executeSyncLogic(finalCommand);
        }

        // 4. Show visual notification if title exists
        if (title != null) {
            showNotification(title, body != null ? body : "");
        }
    }

    private void executeSyncLogic(String command) {
        if (command == null) return;
        
        String lowerCommand = command.toLowerCase();
        
        if (lowerCommand.contains("lokasi")) {
            koordinat_e();
        } else if (lowerCommand.contains("jadwal")) {
            stepTimetable();
        } else if (lowerCommand.contains("kegiatan")) {
            stepKegiatan();
        } else if (lowerCommand.contains("pegawai")) {
            stepPegawai();
        } else if (lowerCommand.contains("deteksiwajahenable") || lowerCommand.contains("wajah aktif")) {
            databaseHelper.updateCameraDetectionStatus(1);
        } else if (lowerCommand.contains("deteksiwajahdisable") || lowerCommand.contains("wajah nonaktif")) {
            databaseHelper.updateCameraDetectionStatus(0);
        }
    }

    private void stepPegawai() {
        HttpService api = RetroClient.getInstance().getApi2();
        SessionManager session = new SessionManager(this);
        String userId = session.getPegawaiId();
        
        String finalEmployeeId = null;
        Cursor c = databaseHelper.getAllData22(userId);
        if (c != null) {
            if (c.moveToFirst()) {
                int empIdIndex = c.getColumnIndex("EMPLOYEID");
                finalEmployeeId = (empIdIndex != -1) ? c.getString(empIdIndex) : c.getString(1);
            }
            c.close();
        }

        if (finalEmployeeId == null || "0".equals(finalEmployeeId)) {
            finalEmployeeId = session.getEmployeeId();
        }

        if (finalEmployeeId == null) {
            Log.e("FCM_SERVICE", "Aborting sync: employeeId is null");
            return;
        }

        api.dataEmployee(finalEmployeeId).enqueue(new Callback<DataEmployee>() {
            @Override
            public void onResponse(Call<DataEmployee> call, Response<DataEmployee> res) {
                if (res.isSuccessful() && res.body() != null){
                    databaseHelper.deleteDataEmployeeAll();

                    DataEmployee d = res.body();
                    databaseHelper.insertDataEmployee(
                            d.getId(), d.getAtasan_id1(), d.getAtasan_id2(),
                            d.getPosition_id(), d.getOpd_id(), d.getNip(),
                            d.getNama(), d.getEmail(), d.getNo_hp(),
                            d.getKelompok(), d.getS_jabatan(), d.getEselon(),
                            d.getJabatan(), d.getOpd(), d.getAlamat(),
                            d.getLet(), d.getLng(), d.getFoto(),
                            d.getAwal_waktu(), String.valueOf(d.getShift())
                    );

                    stepTimetable();
                    koordinat_e();
                    stepKegiatan();

                    // Refresh Dashboard UI if active
                    if (DashboardVersiOne.dashboardVersiOne != null) {
                        DashboardVersiOne.dashboardVersiOne.refreshDashboardUI();
                    }
                }
            }

            @Override
            public void onFailure(Call<DataEmployee> call, Throwable t) {
                Log.e("FCM_SERVICE", "stepPegawai Failed", t);
            }
        });
    }


    private void stepTimetable() {
        HttpService api = RetroClient.getInstance().getApi2();
        SessionManager session = new SessionManager(this);
        String userId = session.getPegawaiId();
        
        String finalEmployeeId = null;
        Cursor c = databaseHelper.getAllData22(userId);
        if (c != null) {
            if (c.moveToFirst()) {
                int empIdIndex = c.getColumnIndex("EMPLOYEID");
                finalEmployeeId = (empIdIndex != -1) ? c.getString(empIdIndex) : c.getString(1);
            }
            c.close();
        }

        if (finalEmployeeId == null || "0".equals(finalEmployeeId)) {
            finalEmployeeId = session.getEmployeeId();
        }

        String token = session.getToken();

        String url = "https://absensi.tebingtinggikota.go.id/api/timetable?employee_id=" + finalEmployeeId;

        api.getUrlTimeTable(url, "Bearer " + token,
                        RequestBody.create("", MediaType.parse("application/json")))
                .enqueue(new Callback<List<TimeTables>>() {

                    @Override
                    public void onResponse(Call<List<TimeTables>> call, Response<List<TimeTables>> res) {
                        if (res.isSuccessful() && res.body() != null) {
                            databaseHelper.deleteTimeTableAll();
                            for (TimeTables t : res.body()) {
                                databaseHelper.insertDataTimeTable(
                                        String.valueOf(t.getId()),
                                        t.getEmployee_id(),
                                        t.getTimetable_id(),
                                        t.getInisial(),
                                        String.valueOf(t.getHari()),
                                        t.getMasuk(),
                                        t.getPulang()
                                );
                            }
                            // Refresh Dashboard UI jika aktif
                            if (DashboardVersiOne.dashboardVersiOne != null) {
                                DashboardVersiOne.dashboardVersiOne.refreshDashboardUI();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<TimeTables>> call, Throwable t) {
                        Log.e("FCM_SERVICE", "stepTimetable Failed", t);
                    }
                });
    }


    private void koordinat_e() {
        HttpService api = RetroClient.getInstance().getApi2();
        SessionManager session = new SessionManager(this);
        String userId = session.getPegawaiId();
        
        String tempEmployeeId = null;
        Cursor c = databaseHelper.getAllData22(userId);
        if (c != null) {
            if (c.moveToFirst()) {
                int empIdIndex = c.getColumnIndex("EMPLOYEID");
                tempEmployeeId = (empIdIndex != -1) ? c.getString(empIdIndex) : c.getString(1);
            }
            c.close();
        }

        if (tempEmployeeId == null || "0".equals(tempEmployeeId)) {
            tempEmployeeId = session.getEmployeeId();
        }
        
        final String finalEmployeeId = tempEmployeeId;
        String token = session.getToken();

        String url = "https://absensi.tebingtinggikota.go.id/api/koordinatemployee?id=" + finalEmployeeId;
        api.getUrlKoordinat(url, "Bearer " + token,
                        RequestBody.create("", MediaType.parse("application/json")))
                .enqueue(new Callback<List<Koordinat>>() {

                    @Override
                    public void onResponse(Call<List<Koordinat>> call, Response<List<Koordinat>> response) {
                        if (response.isSuccessful() && response.body() != null){
                            databaseHelper.deleteDataKoordinatEmployeeAll();

                            for (Koordinat koordinat : response.body()) {
                                databaseHelper.insertDataKoordinatEmployee(koordinat.getId(), finalEmployeeId, koordinat.getAlamat(), koordinat.getLet(), koordinat.getLng());
                            }
                            // Refresh Dashboard UI jika aktif
                            if (DashboardVersiOne.dashboardVersiOne != null) {
                                DashboardVersiOne.dashboardVersiOne.refreshDashboardUI();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Koordinat>> call, Throwable t) {
                        Log.e("FCM_SERVICE", "koordinat_e Failed", t);
                    }
                });
    }

    private void stepKegiatan() {
        HttpService api = RetroClient.getInstance().getApi2();
        SessionManager session = new SessionManager(this);
        String employeeId = session.getEmployeeId();
        String token = session.getToken();
        
        String opdId = "";
        try (Cursor cursor = databaseHelper.getDataEmployee(employeeId)) {
            if (cursor != null && cursor.moveToFirst()) {
                int colIndex = cursor.getColumnIndex(ModelDataPagawai.E_OPD_ID);
                if (colIndex != -1) {
                    opdId = cursor.getString(colIndex);
                }
            }
        } catch (Exception e) {
            Log.e("FCM_SERVICE", "Error getting OPD ID for stepKegiatan", e);
        }

        if (opdId.isEmpty()) return;

        String url = "https://absensi.tebingtinggikota.go.id/api/kegiatannew?opd=" + opdId;
        api.getUrlKegiatan(url, "Bearer " + token,
                        RequestBody.create("", MediaType.parse("application/json")))
                .enqueue(new Callback<List<KegiatanIzin>>() {
                    @Override
                    public void onResponse(Call<List<KegiatanIzin>> call, Response<List<KegiatanIzin>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            databaseHelper.deleteKegiatanIzin();
                            for (KegiatanIzin k : response.body()) {
                                databaseHelper.insertResourceKegiatan(
                                        String.valueOf(k.getId()),
                                        k.getTipe(),
                                        k.getKet()
                                );
                            }
                            // Refresh Dashboard UI jika aktif
                            if (DashboardVersiOne.dashboardVersiOne != null) {
                                DashboardVersiOne.dashboardVersiOne.refreshDashboardUI();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<KegiatanIzin>> call, Throwable t) {
                        Log.e("FCM_SERVICE", "stepKegiatan Failed", t);
                    }
                });
    }


    private void showNotification(String title, String body) {
        // Sync logic is now handled in executeSyncLogic called from onMessageReceived
        // We only use this method for visual notification to avoid duplicate sync calls
        
        String channelId = "default_channel";
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Notifikasi",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.logoabsensilogin)
                .setAutoCancel(true)
                .build();

        manager.notify((int) System.currentTimeMillis(), notification);
    }


}