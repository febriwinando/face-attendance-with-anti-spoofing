package go.pemkott.appsandroidmobiletebingtinggi.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import go.pemkott.appsandroidmobiletebingtinggi.R;

/**
 * Helper untuk memutar nada notifikasi khusus (Tune Berhasil & Gagal)
 * dengan batas volume tepat 50% (0.5f) sesuai arahan kedinasan,
 * serta menyediakan URI suara untuk NotificationChannel sistem Android.
 */
public final class NotificationSoundHelper {

    private static final String TAG = "NotificationSoundHelper";

    /**
     * Volume audio diset tepat 50% (0.5f).
     */
    public static final float NOTIFICATION_VOLUME = 0.5f;

    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private NotificationSoundHelper() {
    }

    /**
     * Mendapatkan URI sumber daya audio untuk nada Berhasil (tune_success).
     */
    public static Uri getSuccessSoundUri(Context context) {
        if (context == null) return null;
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + context.getPackageName() + "/" + R.raw.tune_success);
    }

    /**
     * Mendapatkan URI sumber daya audio untuk nada Gagal (tune_failure).
     */
    public static Uri getFailureSoundUri(Context context) {
        if (context == null) return null;
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + context.getPackageName() + "/" + R.raw.tune_failure);
    }

    /**
     * Memutar nada Berhasil dengan volume 50%.
     */
    public static void playSuccess(Context context) {
        playSound(context, R.raw.tune_success);
    }

    /**
     * Memutar nada Gagal dengan volume 50%.
     */
    public static void playFailure(Context context) {
        playSound(context, R.raw.tune_failure);
    }

    private static void playSound(Context context, int rawResId) {
        if (context == null) return;
        final Context appCtx = context.getApplicationContext();

        MAIN_HANDLER.post(() -> {
            MediaPlayer mediaPlayer = null;
            try {
                AudioManager audioManager = (AudioManager) appCtx.getSystemService(Context.AUDIO_SERVICE);
                if (audioManager != null) {
                    int ringerMode = audioManager.getRingerMode();
                    if (ringerMode == AudioManager.RINGER_MODE_SILENT || ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
                        // Hormati mode senyap / getar pada perangkat pengguna
                        return;
                    }
                }

                mediaPlayer = MediaPlayer.create(appCtx, rawResId);
                if (mediaPlayer == null) return;

                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();

                mediaPlayer.setAudioAttributes(audioAttributes);
                // Terapkan volume tepat 50%
                mediaPlayer.setVolume(NOTIFICATION_VOLUME, NOTIFICATION_VOLUME);

                final MediaPlayer playerToRelease = mediaPlayer;
                playerToRelease.setOnCompletionListener(mp -> {
                    try {
                        mp.reset();
                        mp.release();
                    } catch (Exception ignored) {}
                });

                playerToRelease.setOnErrorListener((mp, what, extra) -> {
                    try {
                        mp.reset();
                        mp.release();
                    } catch (Exception ignored) {}
                    return true;
                });

                playerToRelease.start();
            } catch (Exception e) {
                Log.w(TAG, "Gagal memutar audio tune: " + e.getMessage());
                if (mediaPlayer != null) {
                    try {
                        mediaPlayer.release();
                    } catch (Exception ignored) {}
                }
            }
        });
    }
}

