package go.pemkott.appsandroidmobiletebingtinggi.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;

import go.pemkott.appsandroidmobiletebingtinggi.api.ResponsePOJO;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FcmTokenHelper {

    public interface FcmCallback {
        void onComplete(boolean success);
    }

    public static void fetchAndSendToken(Context context, FcmCallback callback) {
        SessionManager session = new SessionManager(context);
        fetchAndSendTokenForUser(context, session.getEmployeeId(), session.getNip(), callback);
    }

    public static void fetchAndSendTokenForUser(Context context, String employeeId, String nip, FcmCallback callback) {
        if (context == null) {
            if (callback != null) callback.onComplete(false);
            return;
        }

        SessionManager session = new SessionManager(context);
        if (employeeId != null && !employeeId.isEmpty() && !"0".equals(employeeId)) {
            session.saveEmployeeId(employeeId);
        }
        if (nip != null && !nip.isEmpty()) {
            session.saveUsername(nip);
        }

        String cachedToken = session.getFcmToken();
        String finalEmpId = (employeeId != null && !employeeId.isEmpty() && !"0".equals(employeeId))
                ? employeeId : session.getEmployeeId();
        String finalNip = (nip != null && !nip.isEmpty()) ? nip : session.getNip();

        // 1. Langsung kirim pendataan perangkat ke server tanpa menunggu Firebase
        sendDeviceAndTokenToServer(context, finalEmpId, finalNip, cachedToken, callback);

        // 2. Minta token baru dari Firebase SDK secara asinkron di latar belakang
        try {
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String freshToken = task.getResult();
                            if (!freshToken.equals(cachedToken)) {
                                session.saveFcmToken(freshToken);
                                sendDeviceAndTokenToServer(context, finalEmpId, finalNip, freshToken, null);
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e("FCM_HELPER", "Error requesting Firebase token", e);
        }
    }

    public static void sendTokenToServer(Context context, String pegawaiId, String token, FcmCallback callback) {
        SessionManager session = new SessionManager(context);
        String nip = session.getNip();
        sendDeviceAndTokenToServer(context, pegawaiId, nip, token, callback);
    }

    public static void sendDeviceAndTokenToServer(Context context, String employeeId, String nip, String token, FcmCallback callback) {
        boolean hasEmpId = employeeId != null && !employeeId.trim().isEmpty() && !"0".equals(employeeId.trim());
        boolean hasNip = nip != null && !nip.trim().isEmpty();

        if (context == null || (!hasEmpId && !hasNip)) {
            Log.w("FCM_HELPER", "Employee ID dan NIP dua-duanya kosong, skip pendaftaran perangkat.");
            if (callback != null) callback.onComplete(false);
            return;
        }

        SessionManager session = new SessionManager(context);
        if (token == null || token.trim().isEmpty()) {
            token = session.getFcmToken();
        }

        String deviceId = getDeviceId(context);
        String deviceName = getDeviceName();
        String deviceModel = Build.MODEL != null ? Build.MODEL : "Device";
        String osVersion = "Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")";
        String appVersion = "1.0.0";
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            appVersion = pInfo.versionName;
        } catch (Exception ignored) {}

        String empIdVal = hasEmpId ? employeeId : session.getEmployeeId();
        String pegIdVal = session.getPegawaiId();
        String nipVal = hasNip ? nip : session.getNip();

        RetroClient.getInstance(context)
                .getApi()
                .registerEmployeeDevice(
                        empIdVal != null ? empIdVal : "",
                        pegIdVal != null ? pegIdVal : "",
                        nipVal != null ? nipVal : "",
                        deviceId,
                        deviceName,
                        deviceModel,
                        osVersion,
                        appVersion,
                        token != null ? token : ""
                )
                .enqueue(new Callback<ResponsePOJO>() {
                    @Override
                    public void onResponse(Call<ResponsePOJO> call, Response<ResponsePOJO> response) {
                        if (response.isSuccessful()) {
                            Log.d("FCM_HELPER", "Berhasil pendataan perangkat & FCM token ke server (" + deviceName + " - " + deviceId + ")");
                            if (callback != null) callback.onComplete(true);
                        } else {
                            Log.e("FCM_HELPER", "Gagal register device, response code: " + response.code());
                            if (callback != null) callback.onComplete(false);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponsePOJO> call, Throwable t) {
                        Log.e("FCM_HELPER", "Error sending device info to server: " + t.getMessage(), t);
                        if (callback != null) callback.onComplete(false);
                    }
                });
    }

    public static void logoutDevice(Context context, FcmCallback callback) {
        if (context == null) {
            if (callback != null) callback.onComplete(false);
            return;
        }

        SessionManager session = new SessionManager(context);
        String pegawaiId = session.getPegawaiId();
        String employeeId = session.getEmployeeId();
        String nip = session.getNip();
        String idToSend = (employeeId != null && !employeeId.isEmpty() && !"0".equals(employeeId)) ? employeeId : pegawaiId;
        String token = session.getFcmToken();

        String deviceId = "UNKNOWN";
        try {
            deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception ignored) {}

        RetroClient.getInstance(context)
                .getApi()
                .logoutEmployeeDevice(idToSend, nip, deviceId, token)
                .enqueue(new Callback<ResponsePOJO>() {
                    @Override
                    public void onResponse(Call<ResponsePOJO> call, Response<ResponsePOJO> response) {
                        Log.d("FCM_HELPER", "Device status updated to inactive on logout");
                        if (callback != null) callback.onComplete(true);
                    }

                    @Override
                    public void onFailure(Call<ResponsePOJO> call, Throwable t) {
                        Log.e("FCM_HELPER", "Error updating device logout status: " + t.getMessage());
                        if (callback != null) callback.onComplete(false);
                    }
                });
    }

    private static String getDeviceId(Context context) {
        String androidId = null;
        if (context != null) {
            try {
                androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            } catch (Exception ignored) {}
        }

        if (androidId != null && !androidId.trim().isEmpty() && !"null".equalsIgnoreCase(androidId) && !"9774d56d682e549c".equals(androidId)) {
            return androidId.trim();
        }

        String fallback = (Build.MANUFACTURER + "_" + Build.MODEL + "_" + Build.BOARD);
        return "DEV_" + fallback.replaceAll("[^a-zA-Z0-9_]", "_");
    }

    private static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER != null ? Build.MANUFACTURER : "Unknown";
        String model = Build.MODEL != null ? Build.MODEL : "Device";
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        char first = str.charAt(0);
        if (Character.isUpperCase(first)) return str;
        return Character.toUpperCase(first) + str.substring(1);
    }
}
