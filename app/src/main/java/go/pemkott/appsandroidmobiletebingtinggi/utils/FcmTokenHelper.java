package go.pemkott.appsandroidmobiletebingtinggi.utils;

import android.content.Context;
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
        if (context == null) {
            if (callback != null) callback.onComplete(false);
            return;
        }

        SessionManager session = new SessionManager(context);
        String pegawaiId = session.getPegawaiId();
        String employeeId = session.getEmployeeId();
        String idToSend = (employeeId != null && !employeeId.isEmpty() && !"0".equals(employeeId)) ? employeeId : pegawaiId;

        if (idToSend == null || "0".equals(idToSend)) {
            Log.w("FCM_HELPER", "Pegawai/Employee ID belum ada, skip update FCM token");
            if (callback != null) callback.onComplete(false);
            return;
        }

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        Log.e("FCM_HELPER", "Gagal mengambil token FCM", task.getException());
                        if (callback != null) callback.onComplete(false);
                        return;
                    }

                    String token = task.getResult();
                    session.saveFcmToken(token);
                    sendTokenToServer(context, idToSend, token, callback);
                });
    }

    public static void sendTokenToServer(Context context, String pegawaiId, String token, FcmCallback callback) {
        if (pegawaiId == null || "0".equals(pegawaiId) || token == null || token.isEmpty()) {
            if (callback != null) callback.onComplete(false);
            return;
        }

        RetroClient.getInstance(context)
                .getApi()
                .updateFcmToken(pegawaiId, token)
                .enqueue(new Callback<ResponsePOJO>() {
                    @Override
                    public void onResponse(Call<ResponsePOJO> call, Response<ResponsePOJO> response) {
                        if (response.isSuccessful()) {
                            Log.d("FCM_HELPER", "Berhasil update FCM token ke server");
                            if (callback != null) callback.onComplete(true);
                        } else {
                            Log.e("FCM_HELPER", "Gagal update FCM token, response code: " + response.code());
                            if (callback != null) callback.onComplete(false);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponsePOJO> call, Throwable t) {
                        Log.e("FCM_HELPER", "Error sending FCM token to server: " + t.getMessage(), t);
                        if (callback != null) callback.onComplete(false);
                    }
                });
    }
}
