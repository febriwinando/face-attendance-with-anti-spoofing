package go.pemkott.appsandroidmobiletebingtinggi;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
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
import go.pemkott.appsandroidmobiletebingtinggi.api.ServerConfig;
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

        Map<String, String> dataMap = remoteMessage.getData();

        // 0. PRIORITY CHECK: Remote Logout Command (Eksekusi langsung sebelum validasi session)
        boolean isRemoteLogout = false;
        if (dataMap != null && !dataMap.isEmpty()) {
            String type = dataMap.get("type");
            String command = dataMap.get("command");
            String title = dataMap.get("title");
            String screen = dataMap.get("screen");

            if ("remote_logout".equalsIgnoreCase(type)
                    || "remote_logout".equalsIgnoreCase(command)
                    || "LOGOUT".equalsIgnoreCase(title)
                    || "LOGOUT".equalsIgnoreCase(command)
                    || "login".equalsIgnoreCase(screen)) {
                isRemoteLogout = true;
            }
        }
        if (remoteMessage.getNotification() != null) {
            String notifTitle = remoteMessage.getNotification().getTitle();
            if (notifTitle != null && notifTitle.equalsIgnoreCase("LOGOUT")) {
                isRemoteLogout = true;
            }
        }

        if (isRemoteLogout) {
            Log.w("FCM_SERVICE", "Menerima perintah PUSH LOGOUT dari Administrator Web! Eksekusi logout...");
            showNotification("Sesi Diakhiri", "Sesi login Anda telah diakhiri secara remote dari Administrator Web.", dataMap);
            executeRemoteLogout();
            return;
        }

        SessionManager session = new SessionManager(this);
        String currentPegawaiId = session.getPegawaiId();
        String currentEmployeeId = session.getEmployeeId();

        boolean hasActiveUser = (currentEmployeeId != null && !currentEmployeeId.trim().isEmpty() && !"0".equals(currentEmployeeId.trim()))
                || (currentPegawaiId != null && !currentPegawaiId.trim().isEmpty() && !"0".equals(currentPegawaiId.trim()));

        // 1. Validasi: Jika tidak ada user yang sedang login di perangkat ini, abaikan notifikasi
        if (!hasActiveUser) {
            Log.w("FCM_SERVICE", "Abaikan notifikasi: Tidak ada user aktif yang login di perangkat ini.");
            return;
        }

        // 2. Validasi Penerima: Mencegah salah kirim jika perangkat pernah dipakai akun lain
        if (dataMap != null && !dataMap.isEmpty()) {
            String targetEmpId = dataMap.get("target_employee_id");
            if (targetEmpId == null) targetEmpId = dataMap.get("employee_id");
            if (targetEmpId == null) targetEmpId = dataMap.get("pegawai_id");

            if (targetEmpId != null && !targetEmpId.trim().isEmpty() && !"0".equals(targetEmpId.trim())) {
                String cleanTarget = targetEmpId.trim().replace(".0", "");
                String cleanEmpId = currentEmployeeId != null ? currentEmployeeId.trim().replace(".0", "") : "";
                String cleanPegId = currentPegawaiId != null ? currentPegawaiId.trim().replace(".0", "") : "";

                boolean isMatch = cleanTarget.equalsIgnoreCase(cleanEmpId) || cleanTarget.equalsIgnoreCase(cleanPegId);
                
                if (!isMatch) {
                    Log.w("FCM_SERVICE", "Abaikan notifikasi: Pesan ditujukan untuk ID " + targetEmpId + 
                          ", tetapi user yang login adalah " + currentEmployeeId + "/" + currentPegawaiId);
                    return;
                }
            }
        }

        String title = null;
        String body = null;

        // 3. Check Notification Payload
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
            Log.d("FCM_SERVICE", "Notification Payload - Title: " + title + ", Body: " + body);
        }

        // 4. Check Data Payload
        if (dataMap != null && !dataMap.isEmpty()) {
            Log.d("FCM_SERVICE", "Data Payload: " + dataMap.toString());
            String dataTitle = dataMap.get("title");
            String dataBody = dataMap.get("body");

            if (title == null) title = dataTitle;
            if (body == null) body = dataBody;
        }

        // 5. Ambil keputusan perintah sinkronisasi
        String finalCommand = null;
        if (dataMap != null && !dataMap.isEmpty()) {
            finalCommand = dataMap.get("title");
            if (finalCommand == null) finalCommand = dataMap.get("body");
            if (finalCommand == null) finalCommand = dataMap.get("command");
        }
        
        if (finalCommand == null) {
            finalCommand = title;
        }

        if (finalCommand != null) {
            finalCommand = finalCommand.trim();
            Log.d("FCM_SERVICE", "Processing Final Command: " + finalCommand);
            executeSyncLogic(finalCommand);
        }

        // 6. Tampilkan notifikasi visual jika judul ada
        if (title != null) {
            showNotification(title, body != null ? body : "", dataMap);
        }
    }

    private void executeSyncLogic(String command) {
        if (command == null) return;
        
        String lowerCommand = command.toLowerCase();
        
        if (lowerCommand.contains("logout")) {
            executeRemoteLogout();
        } else if (lowerCommand.contains("lokasi")) {
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

    private void executeRemoteLogout() {
        Log.w("FCM_SERVICE", "Menerima perintah PUSH LOGOUT dari Administrator Web! Membersihkan seluruh data lokal...");

        try {
            // 1. Hapus seluruh data SQLite lokal
            databaseHelper.deleteAllDataUser();
            databaseHelper.deleteDataEmployeeAll();
            new go.pemkott.appsandroidmobiletebingtinggi.database.ChatLocalDatabase(this).clearAllLocalChatData();

            // 2. Clear Session Manager
            SessionManager session = new SessionManager(this);
            session.clearSession();

            // 3. Delete Firebase instance token
            com.google.firebase.messaging.FirebaseMessaging.getInstance().deleteToken();

            // 4. Buka LoginActivity dan bersihkan Task Stack
            Intent intent = new Intent(this, go.pemkott.appsandroidmobiletebingtinggi.login.LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("FCM_SERVICE", "Error executing remote logout: " + e.getMessage(), e);
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

        String url = ServerConfig.getApiUrl() + "timetable?employee_id=" + finalEmployeeId;

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

        String url = ServerConfig.getApiUrl() + "koordinatemployee?id=" + finalEmployeeId;
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

        String url = ServerConfig.getApiUrl() + "kegiatannew?opd=" + opdId;
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


    private void showNotification(String title, String body, Map<String, String> dataMap) {
        String channelId = "default_channel";
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Notifikasi Absensi",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifikasi penting aplikasi absensi");
            channel.enableVibration(true);
            channel.enableLights(true);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        android.content.Intent intent;
        String type = dataMap != null ? dataMap.get("type") : null;
        if ("chat".equals(type) || (title != null && title.toLowerCase().contains("pesan"))) {
            String targetIdStr = dataMap != null ? dataMap.get("target_id") : null;
            String chatType = dataMap != null ? dataMap.get("chat_type") : "personal";
            long targetId = 0;
            try {
                if (targetIdStr != null) targetId = Long.parseLong(targetIdStr);
            } catch (Exception ignored) {}

            if (targetId > 0) {
                intent = new android.content.Intent(this, go.pemkott.appsandroidmobiletebingtinggi.chat.ChatRoomActivity.class);
                intent.putExtra("target_id", targetId);
                intent.putExtra("chat_type", chatType != null ? chatType : "personal");
                intent.putExtra("title", title != null ? title : "Pesan Baru");
            } else {
                intent = new android.content.Intent(this, go.pemkott.appsandroidmobiletebingtinggi.chat.ChatListActivity.class);
            }
        } else {
            intent = new android.content.Intent(this, DashboardVersiOne.class);
        }

        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);

        int flags = android.app.PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= android.app.PendingIntent.FLAG_IMMUTABLE;
        }

        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, flags);

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.logoabsensilogin)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), notification);
        }
    }


}