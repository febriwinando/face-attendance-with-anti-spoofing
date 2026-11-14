package go.pemkott.appsandroidmobiletebingtinggi.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLHandshakeException;

import go.pemkott.appsandroidmobiletebingtinggi.NewDashboard.DashboardVersiOne;
import go.pemkott.appsandroidmobiletebingtinggi.R;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Helper terpusat untuk menampilkan notifikasi sistem saat proses absensi pegawai
 * berhasil maupun gagal, serta mencegah duplikasi banner jika FCM tiba sesaat setelahnya.
 */
public final class AttendanceNotificationHelper {

    public static final String CHANNEL_ID_SUCCESS = "channel_attendance_success";
    public static final String CHANNEL_ID_FAILURE = "channel_attendance_failure";
    public static final String CHANNEL_ID = CHANNEL_ID_SUCCESS;
    public static final String OUTCOME_SUCCESS = "success";
    public static final String OUTCOME_FAILED = "failed";

    private static final String PREF_NAME = "attendance_notif_dedup";
    private static final String KEY_LAST_TIME = "last_local_notif_ms";
    private static final String KEY_LAST_OUTCOME = "last_local_notif_outcome";
    static final long FCM_DEDUP_WINDOW_MS = 15_000L;

    private static final AtomicInteger NOTIFICATION_SEQ = new AtomicInteger(1000);

    private AttendanceNotificationHelper() {
    }

    public static void notifySuccess(Context context, String attendanceLabel, String remarks) {
        String title = buildTitle(true, attendanceLabel);
        String body = (remarks != null && !remarks.trim().isEmpty())
                ? remarks.trim()
                : "Proses absensi berhasil tercatat.";
        postNotification(context, title, body, OUTCOME_SUCCESS);
    }

    public static void notifyFailure(Context context, String attendanceLabel, String reason) {
        String title = buildTitle(false, attendanceLabel);
        String body = (reason != null && !reason.trim().isEmpty())
                ? reason.trim()
                : "Proses absensi gagal. Silakan coba kembali.";
        postNotification(context, title, body, OUTCOME_FAILED);
    }

    public static void notifyHttpFailure(Context context, String attendanceLabel, Response<?> response) {
        String reason = extractHttpErrorReason(response);
        notifyFailure(context, attendanceLabel, reason);
    }

    public static void notifyHttpFailure(Context context, String attendanceLabel, int httpCode, String rawErrorBody) {
        String reason = extractHttpErrorReason(httpCode, rawErrorBody);
        notifyFailure(context, attendanceLabel, reason);
    }

    public static void notifyNetworkFailure(Context context, String attendanceLabel, Throwable t) {
        notifyFailure(context, attendanceLabel, formatNetworkFailureReason(t));
    }

    public static String extractHttpErrorReason(Response<?> response) {
        if (response == null) {
            return "Terjadi kesalahan saat menghubungi server.";
        }
        String rawBody = null;
        try {
            ResponseBody err = response.errorBody();
            if (err != null) {
                rawBody = err.string();
            }
        } catch (Exception ignored) {
        }
        return extractHttpErrorReason(response.code(), rawBody);
    }

    public static String extractHttpErrorReason(int httpCode, String rawErrorBody) {
        if (rawErrorBody != null && !rawErrorBody.trim().isEmpty()) {
            String trimmed = rawErrorBody.trim();
            try {
                JSONObject obj = new JSONObject(trimmed);
                String remarks = obj.optString("remarks", "").trim();
                if (!remarks.isEmpty()) {
                    return remarks;
                }
                String message = obj.optString("message", "").trim();
                if (!message.isEmpty()) {
                    return message;
                }
            } catch (Exception ignored) {
                String remarks = extractJsonStringFallback(trimmed, "remarks");
                if (!remarks.isEmpty()) {
                    return remarks;
                }
                String message = extractJsonStringFallback(trimmed, "message");
                if (!message.isEmpty()) {
                    return message;
                }
                if (trimmed.length() <= 160 && !trimmed.startsWith("<") && !trimmed.startsWith("{")) {
                    return trimmed;
                }
            }
        }
        if (httpCode > 0) {
            return "Gagal mengirim absensi (HTTP " + httpCode + "). Silakan coba kembali.";
        }
        return "Gagal mengirim absensi. Silakan coba kembali.";
    }

    private static String extractJsonStringFallback(String json, String key) {
        if (json == null || !json.startsWith("{")) {
            return "";
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("\"" + java.util.regex.Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
                .matcher(json);
        if (matcher.find()) {
            return matcher.group(1).replace("\\\"", "\"").replace("\\n", " ").trim();
        }
        return "";
    }

    public static String formatNetworkFailureReason(Throwable t) {
        if (t instanceof SocketTimeoutException) {
            return "Koneksi timeout ke server absensi. Silakan coba kembali.";
        }
        if (t instanceof UnknownHostException || t instanceof ConnectException) {
            return "Tidak dapat terhubung ke server absensi. Periksa koneksi jaringan Anda.";
        }
        if (t instanceof SSLHandshakeException) {
            return "Verifikasi SSL server gagal. Periksa pengaturan koneksi Anda.";
        }
        String msg = t != null && t.getMessage() != null ? t.getMessage().trim() : "";
        if (!msg.isEmpty()) {
            return "Gagal mengirim absensi: " + msg;
        }
        return "Koneksi ke server terputus saat mengirim absensi.";
    }

    public static boolean shouldSuppressFcmAttendanceNotification(Context context, String incomingOutcome) {
        if (context == null) {
            return false;
        }
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        long lastTime = prefs.getLong(KEY_LAST_TIME, 0L);
        String lastOutcome = prefs.getString(KEY_LAST_OUTCOME, "");
        return isWithinSuppressionWindow(lastTime, System.currentTimeMillis(), lastOutcome, incomingOutcome);
    }

    public static boolean isWithinSuppressionWindow(long lastTimeMs, long nowMs, String lastOutcome, String incomingOutcome) {
        if (lastTimeMs <= 0L || nowMs < lastTimeMs) {
            return false;
        }
        if (nowMs - lastTimeMs > FCM_DEDUP_WINDOW_MS) {
            return false;
        }
        if (incomingOutcome == null || incomingOutcome.trim().isEmpty()
                || lastOutcome == null || lastOutcome.trim().isEmpty()) {
            return true;
        }
        return lastOutcome.trim().equalsIgnoreCase(incomingOutcome.trim());
    }

    static String buildTitle(boolean success, String attendanceLabel) {
        String base = success ? "Absensi Berhasil" : "Absensi Gagal";
        if (attendanceLabel == null || attendanceLabel.trim().isEmpty()) {
            return base;
        }
        return base + " • " + attendanceLabel.trim();
    }

    private static void postNotification(Context context, String title, String body, String outcome) {
        if (context == null) {
            return;
        }
        Context appCtx = context.getApplicationContext();

        recordLocalNotification(appCtx, outcome);

        boolean isSuccess = OUTCOME_SUCCESS.equalsIgnoreCase(outcome);

        // Putar audio tune sesuai jenisnya (Berhasil vs Gagal) pada volume 50%
        if (isSuccess) {
            NotificationSoundHelper.playSuccess(appCtx);
        } else {
            NotificationSoundHelper.playFailure(appCtx);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(appCtx, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        NotificationManager manager = (NotificationManager) appCtx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return;
        }

        String channelId = isSuccess ? CHANNEL_ID_SUCCESS : CHANNEL_ID_FAILURE;
        android.net.Uri soundUri = isSuccess ? NotificationSoundHelper.getSuccessSoundUri(appCtx)
                : NotificationSoundHelper.getFailureSoundUri(appCtx);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.media.AudioAttributes audioAttributes = new android.media.AudioAttributes.Builder()
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_EVENT)
                    .build();

            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    isSuccess ? "Notifikasi Absensi Berhasil" : "Notifikasi Absensi Gagal",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(isSuccess ? "Notifikasi saat absensi berhasil tercatat" : "Notifikasi saat absensi gagal tercatat");
            channel.enableVibration(true);
            channel.enableLights(true);
            if (soundUri != null) {
                channel.setSound(soundUri, audioAttributes);
            }
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(appCtx, DashboardVersiOne.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        int notificationId = NOTIFICATION_SEQ.incrementAndGet();
        PendingIntent pendingIntent = PendingIntent.getActivity(appCtx, notificationId, intent, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(appCtx, channelId)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setSmallIcon(R.drawable.logoabsensilogin)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSound(soundUri)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        try {
            manager.notify(notificationId, builder.build());
        } catch (RuntimeException ignored) {
        }
    }

    private static void recordLocalNotification(Context appCtx, String outcome) {
        try {
            appCtx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putLong(KEY_LAST_TIME, System.currentTimeMillis())
                    .putString(KEY_LAST_OUTCOME, outcome != null ? outcome : "")
                    .apply();
        } catch (Exception ignored) {
        }
    }
}
