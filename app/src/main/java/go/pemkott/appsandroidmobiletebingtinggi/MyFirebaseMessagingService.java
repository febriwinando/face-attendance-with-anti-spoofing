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

import go.pemkott.appsandroidmobiletebingtinggi.utils.AttendanceNotificationHelper;
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

            if ("chat_sync".equalsIgnoreCase(dataMap.get("type"))) {
                handleSilentChatSyncEvent(dataMap);
                return;
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

        // 6. Tampilkan notifikasi visual jika judul ada (lewati jika baru saja tampil via notifikasi lokal absensi)
        if (title != null) {
            String fcmType = dataMap != null ? dataMap.get("type") : null;
            String fcmOutcome = dataMap != null ? dataMap.get("outcome") : null;
            boolean isAttendanceFcm = "absensi".equalsIgnoreCase(fcmType)
                    || "absensi_gagal".equalsIgnoreCase(fcmType)
                    || title.toLowerCase().startsWith("absensi berhasil")
                    || title.toLowerCase().startsWith("absensi gagal");

            if (isAttendanceFcm) {
                if (fcmOutcome == null || fcmOutcome.trim().isEmpty()) {
                    fcmOutcome = ("absensi_gagal".equalsIgnoreCase(fcmType) || title.toLowerCase().startsWith("absensi gagal"))
                            ? AttendanceNotificationHelper.OUTCOME_FAILED
                            : AttendanceNotificationHelper.OUTCOME_SUCCESS;
                }
                if (AttendanceNotificationHelper.shouldSuppressFcmAttendanceNotification(this, fcmOutcome)) {
                    return;
                }
            }

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
            koordinat_opd();
        } else if (lowerCommand.contains("jadwal")) {
            stepTimetable();
        } else if (lowerCommand.contains("kegiatan") || lowerCommand.contains("agenda")) {
            stepKegiatan();
        } else if (lowerCommand.contains("pegawai") || lowerCommand.contains("profil")) {
            stepPegawai();
        } else if (lowerCommand.contains("semua") || lowerCommand.contains("sync_all")) {
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
                    koordinat_opd();
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
                            databaseHelper.deleteDataKoordinatEmployee(finalEmployeeId);

                            for (Koordinat koordinat : response.body()) {
                                if (koordinat == null || "kosong".equalsIgnoreCase(koordinat.getStatus())) {
                                    continue;
                                }
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

    private void koordinat_opd() {
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
            Log.e("FCM_SERVICE", "Error getting OPD ID for koordinat_opd", e);
        }

        if (opdId.isEmpty()) return;

        final String targetOpdId = opdId;
        String url = ServerConfig.getApiUrl() + "koordinat?opdid=" + targetOpdId;
        api.getUrlKoordinat(url, "Bearer " + token,
                        RequestBody.create("", MediaType.parse("application/json")))
                .enqueue(new Callback<List<Koordinat>>() {
                    @Override
                    public void onResponse(Call<List<Koordinat>> call, Response<List<Koordinat>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            databaseHelper.deleteDataKoordinatOPD(targetOpdId);
                            for (Koordinat k : response.body()) {
                                if (k == null || "kosong".equalsIgnoreCase(k.getStatus())) continue;
                                databaseHelper.insertDataKoordinat(k.getId(), targetOpdId, k.getAlamat(), k.getLet(), k.getLng());
                            }
                            if (DashboardVersiOne.dashboardVersiOne != null) {
                                DashboardVersiOne.dashboardVersiOne.refreshDashboardUI();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Koordinat>> call, Throwable t) {
                        Log.e("FCM_SERVICE", "koordinat_opd Failed", t);
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
        String type = dataMap != null ? dataMap.get("type") : null;
        String outcome = dataMap != null ? dataMap.get("outcome") : null;
        boolean isChat = "chat".equals(type) || (title != null && (title.toLowerCase().contains("pesan") || title.toLowerCase().contains("sahut") || title.toLowerCase().contains("chat")));

        boolean isFailure = !isChat && (
                "failed".equalsIgnoreCase(outcome)
                || "absensi_gagal".equalsIgnoreCase(type)
                || (title != null && (title.toLowerCase().contains("gagal") || title.toLowerCase().contains("error") || title.toLowerCase().contains("ditolak")))
                || (body != null && (body.toLowerCase().contains("gagal diproses") || body.toLowerCase().contains("ditolak")))
        );

        String channelId;
        android.net.Uri soundUri = null;

        if (isChat) {
            channelId = "channel_sahut_chat";
        } else if (isFailure) {
            channelId = AttendanceNotificationHelper.CHANNEL_ID_FAILURE;
            soundUri = go.pemkott.appsandroidmobiletebingtinggi.utils.NotificationSoundHelper.getFailureSoundUri(this);
            go.pemkott.appsandroidmobiletebingtinggi.utils.NotificationSoundHelper.playFailure(this);
        } else {
            channelId = AttendanceNotificationHelper.CHANNEL_ID_SUCCESS;
            soundUri = go.pemkott.appsandroidmobiletebingtinggi.utils.NotificationSoundHelper.getSuccessSoundUri(this);
            go.pemkott.appsandroidmobiletebingtinggi.utils.NotificationSoundHelper.playSuccess(this);
        }

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName;
            String channelDesc;

            if (isChat) {
                channelName = "SAHUT (Sarana Hubungan Terpadu)";
                channelDesc = "Pemberitahuan pesan masuk layanan SAHUT";
            } else if (isFailure) {
                channelName = "Notifikasi Absensi Gagal";
                channelDesc = "Pemberitahuan saat absensi atau permohonan dinas gagal";
            } else {
                channelName = "Notifikasi Absensi & Pembaruan Sistem";
                channelDesc = "Pemberitahuan status absensi berhasil dan pembaruan data sistem";
            }

            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(channelDesc);
            channel.enableVibration(true);
            channel.enableLights(true);

            if (soundUri != null) {
                android.media.AudioAttributes audioAttributes = new android.media.AudioAttributes.Builder()
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_EVENT)
                        .build();
                channel.setSound(soundUri, audioAttributes);
            }

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        android.content.Intent intent;
        if (isChat) {
            String targetIdStr = dataMap != null ? dataMap.get("target_id") : null;
            String chatType = dataMap != null ? dataMap.get("chat_type") : "personal";
            long targetId = 0;
            try {
                if (targetIdStr != null) targetId = Long.parseLong(targetIdStr);
            } catch (Exception ignored) {}

            // 1. Redam notifikasi sistem bar jika pengguna sedang aktif di ruang chat lawan bicara ini
            if (go.pemkott.appsandroidmobiletebingtinggi.chat.ChatRoomActivity.isChatRoomActive
                    && go.pemkott.appsandroidmobiletebingtinggi.chat.ChatRoomActivity.activeTargetId == targetId
                    && (chatType == null || chatType.equalsIgnoreCase(go.pemkott.appsandroidmobiletebingtinggi.chat.ChatRoomActivity.activeChatType))) {
                go.pemkott.appsandroidmobiletebingtinggi.chat.ChatRoomActivity.notifyIncomingMessageFromFCM(targetId, chatType);
                return;
            }

            // 2. Beri tahu ChatListActivity jika sedang aktif di latar depan
            if (go.pemkott.appsandroidmobiletebingtinggi.chat.ChatListActivity.isChatListActive) {
                go.pemkott.appsandroidmobiletebingtinggi.chat.ChatListActivity.notifyIncomingMessageFromFCM();
            }

            if (targetId > 0) {
                intent = new android.content.Intent(this, go.pemkott.appsandroidmobiletebingtinggi.chat.ChatRoomActivity.class);
                intent.putExtra("target_id", targetId);
                intent.putExtra("chat_type", chatType != null ? chatType : "personal");
                intent.putExtra("title", title != null ? title : "Pesan SAHUT");
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

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setSmallIcon(R.drawable.logoabsensilogin)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (soundUri != null) {
            builder.setSound(soundUri);
        } else {
            builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        }

        Notification notification = builder.build();

        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), notification);
        }
    }

    private void handleSilentChatSyncEvent(Map<String, String> dataMap) {
        try {
            String event = dataMap.get("event");
            String chatType = dataMap.get("chat_type");
            if (chatType == null || chatType.trim().isEmpty()) chatType = "personal";
            long senderId = parseLongSafe(dataMap.get("sender_id"));
            long receiverId = parseLongSafe(dataMap.get("receiver_id"));
            long opdId = parseLongSafe(dataMap.get("opd_id"));
            long singleMsgId = parseLongSafe(dataMap.get("message_id"));
            String deletedBy = dataMap.get("deleted_by");

            java.util.List<Long> ids = new java.util.ArrayList<>();
            if (singleMsgId > 0) ids.add(singleMsgId);
            String csvIds = dataMap.get("message_ids");
            if (csvIds != null && !csvIds.trim().isEmpty()) {
                for (String token : csvIds.split(",")) {
                    long parsed = parseLongSafe(token);
                    if (parsed > 0 && !ids.contains(parsed)) {
                        ids.add(parsed);
                    }
                }
            }

            go.pemkott.appsandroidmobiletebingtinggi.database.ChatLocalDatabase localDb =
                    new go.pemkott.appsandroidmobiletebingtinggi.database.ChatLocalDatabase(getApplicationContext());
            if ("deleted_for_everyone".equalsIgnoreCase(event) || "conversation_deleted_for_everyone".equalsIgnoreCase(event)) {
                for (Long id : ids) {
                    localDb.softDeleteForEveryoneLocal(id, deletedBy != null ? deletedBy : "");
                }
            } else if ("permanent_delete".equalsIgnoreCase(event)) {
                localDb.hardDeleteMessagesLocal(ids);
            }

            if (go.pemkott.appsandroidmobiletebingtinggi.chat.ChatRoomActivity.isChatRoomActive) {
                go.pemkott.appsandroidmobiletebingtinggi.chat.ChatRoomActivity.notifySyncEventFromFCM(
                        event, chatType, senderId, receiverId, opdId
                );
            }
            if (go.pemkott.appsandroidmobiletebingtinggi.chat.ChatListActivity.isChatListActive) {
                go.pemkott.appsandroidmobiletebingtinggi.chat.ChatListActivity.notifyIncomingMessageFromFCM();
            }
        } catch (Exception e) {
            Log.e("FCM_SERVICE", "Gagal memproses silent chat sync: " + e.getMessage());
        }
    }

    private long parseLongSafe(String raw) {
        if (raw == null || raw.trim().isEmpty()) return 0L;
        try {
            return Long.parseLong(raw.trim());
        } catch (Exception e) {
            return 0L;
        }
    }

}