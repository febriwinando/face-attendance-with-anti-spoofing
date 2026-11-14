package go.pemkott.appsandroidmobiletebingtinggi.chat;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import org.json.JSONObject;

import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.database.ChatLocalDatabase;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMessageItem;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse;
import go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Helper UI & sinkronisasi bersama untuk penghapusan pesan logis ('for_me' dan 'for_everyone')
 * di halaman Daftar Chat maupun Ruang Obrolan tanpa opsi hapus permanen.
 */
public final class ChatDeletionUiHelper {

    private ChatDeletionUiHelper() {}

    public static void confirmAndExecuteSingleDelete(
            Activity activity,
            ChatLocalDatabase chatDb,
            ChatMessageItem item,
            String scope,
            String chatType,
            long targetId,
            Runnable onDeletedLocally,
            Runnable onServerRejectedNeedReload
    ) {
        if (activity == null || item == null) return;

        final String resolvedScope = ("for_everyone".equalsIgnoreCase(scope) && item.canDeleteForEveryone())
                ? "for_everyone"
                : "for_me";

        String title = "for_everyone".equals(resolvedScope)
                ? "Hapus Pesan untuk Semua"
                : "Hapus Pesan untuk Saya";
        String msg;
        if ("for_everyone".equals(resolvedScope)) {
            msg = "opd".equalsIgnoreCase(chatType)
                    ? "Pesan ini belum dibaca oleh anggota lain dan akan dihapus untuk Anda serta seluruh anggota grup OPD. Lanjutkan?"
                    : "Pesan ini belum dibaca oleh penerima dan akan dihapus dari tampilan Anda maupun penerima. Lanjutkan?";
        } else {
            msg = "opd".equalsIgnoreCase(chatType)
                    ? "Pesan ini hanya akan dihapus dari tampilan Anda. Anggota grup OPD lainnya tetap dapat membaca pesan ini."
                    : "Pesan ini hanya akan dihapus dari tampilan Anda. Pegawai lawan bicara tetap dapat membaca pesan ini.";
        }

        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("for_everyone".equals(resolvedScope) ? "Hapus untuk Semua" : "Hapus", (dialog, which) ->
                        executeSingleDelete(activity, chatDb, item, resolvedScope, chatType, targetId, onDeletedLocally, onServerRejectedNeedReload))
                .setNegativeButton("Batal", null)
                .show();
    }

    public static void showScopePickerThenDeleteSingle(
            Activity activity,
            ChatLocalDatabase chatDb,
            ChatMessageItem item,
            String chatType,
            long targetId,
            Runnable onDeletedLocally,
            Runnable onServerRejectedNeedReload
    ) {
        if (activity == null || item == null) return;

        if (item.canDeleteForEveryone()) {
            String[] options = new String[]{
                    "Hapus untuk Saya",
                    "Hapus untuk Semua (Pesan Belum Dibaca)"
            };
            new AlertDialog.Builder(activity)
                    .setTitle("Pilih Metode Hapus Pesan")
                    .setItems(options, (dialog, which) -> confirmAndExecuteSingleDelete(
                            activity,
                            chatDb,
                            item,
                            which == 1 ? "for_everyone" : "for_me",
                            chatType,
                            targetId,
                            onDeletedLocally,
                            onServerRejectedNeedReload
                    ))
                    .setNegativeButton("Batal", null)
                    .show();
        } else {
            confirmAndExecuteSingleDelete(
                    activity, chatDb, item, "for_me", chatType, targetId, onDeletedLocally, onServerRejectedNeedReload
            );
        }
    }

    private static void executeSingleDelete(
            Activity activity,
            ChatLocalDatabase chatDb,
            ChatMessageItem item,
            String scope,
            String chatType,
            long targetId,
            Runnable onDeletedLocally,
            Runnable onServerRejectedNeedReload
    ) {
        String myNip = resolveCurrentNip(activity);
        HttpService api = RetroClient.getInstance(activity).getApi2();

        if ("for_everyone".equals(scope)) {
            api.deleteChatMessageForEveryone(item.getId()).enqueue(new Callback<SendMessageResponse>() {
                @Override
                public void onResponse(Call<SendMessageResponse> call, Response<SendMessageResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                        chatDb.softDeleteForEveryoneLocal(item.getId(), myNip);
                        chatDb.syncLastMessageFromLocal(chatType, targetId, false);
                        if (onDeletedLocally != null) onDeletedLocally.run();
                        CustomUiFeedback.showTopBanner(
                                activity, "Pesan dihapus untuk pengirim dan penerima", CustomUiFeedback.Type.SUCCESS
                        );
                    } else {
                        String errMsg = extractErrorMessage(response, "Pesan sudah dibaca oleh penerima sehingga hanya dapat dihapus untuk Anda.");
                        CustomUiFeedback.showTopBanner(activity, errMsg, CustomUiFeedback.Type.ERROR);
                        if (onServerRejectedNeedReload != null) onServerRejectedNeedReload.run();
                    }
                }

                @Override
                public void onFailure(Call<SendMessageResponse> call, Throwable t) {
                    Log.e("CHAT_DELETE", "delete-for-everyone error", t);
                    CustomUiFeedback.showTopBanner(
                            activity, "Gagal menghubungi server. Periksa koneksi Anda.", CustomUiFeedback.Type.ERROR
                    );
                }
            });
            return;
        }

        // Hapus untuk Saya (Soft delete logis per pengguna)
        api.deleteChatMessage(item.getId(), "for_me").enqueue(new Callback<SendMessageResponse>() {
            @Override
            public void onResponse(Call<SendMessageResponse> call, Response<SendMessageResponse> response) {
                if (!response.isSuccessful()) {
                    CustomUiFeedback.showTopBanner(
                            activity,
                            extractErrorMessage(response, "Sinkronisasi hapus pesan tertunda."),
                            CustomUiFeedback.Type.ERROR
                    );
                }
            }

            @Override
            public void onFailure(Call<SendMessageResponse> call, Throwable t) {}
        });

        chatDb.softDeleteForSenderOrReceiverLocal(item.getId(), item.isMe(), myNip);
        chatDb.syncLastMessageFromLocal(chatType, targetId, false);
        if (onDeletedLocally != null) onDeletedLocally.run();
        CustomUiFeedback.showTopBanner(
                activity, "Pesan dihapus dari tampilan Anda", CustomUiFeedback.Type.SUCCESS
        );
    }

    public static void showClearConversationFlow(
            Activity activity,
            ChatLocalDatabase chatDb,
            String chatType,
            long targetId,
            Runnable onClearedLocally
    ) {
        if (activity == null) return;
        int unreadSent = chatDb.countUnreadSentMessages(chatType, targetId);

        if (unreadSent > 0) {
            String[] options = new String[]{
                    "Hapus untuk Saya Saja",
                    "Hapus Pesan Belum Dibaca untuk Semua (" + unreadSent + " pesan)"
            };
            new AlertDialog.Builder(activity)
                    .setTitle("Hapus Percakapan")
                    .setItems(options, (dialog, which) -> confirmExecuteClearConversation(
                            activity,
                            chatDb,
                            chatType,
                            targetId,
                            which == 1 ? "for_everyone" : "for_me",
                            unreadSent,
                            onClearedLocally
                    ))
                    .setNegativeButton("Batal", null)
                    .show();
        } else {
            confirmExecuteClearConversation(activity, chatDb, chatType, targetId, "for_me", 0, onClearedLocally);
        }
    }

    private static void confirmExecuteClearConversation(
            Activity activity,
            ChatLocalDatabase chatDb,
            String chatType,
            long targetId,
            String clearType,
            int unreadSent,
            Runnable onClearedLocally
    ) {
        String msg;
        if ("for_everyone".equals(clearType)) {
            msg = unreadSent + " pesan Anda yang belum dibaca akan dihapus untuk semua pihak. Pesan yang sudah dibaca maupun pesan masuk hanya akan dihapus dari tampilan Anda. Lanjutkan?";
        } else {
            msg = "opd".equalsIgnoreCase(chatType)
                    ? "Riwayat obrolan grup OPD ini akan dibersihkan hanya dari akun Anda. Anggota grup OPD lain tetap dapat membaca riwayat pesan."
                    : "Riwayat obrolan ini akan dibersihkan hanya dari akun Anda. Pegawai lawan bicara tetap dapat membaca riwayat pesan.";
        }

        new AlertDialog.Builder(activity)
                .setTitle("for_everyone".equals(clearType) ? "Hapus Percakapan & Tarik Pesan Belum Dibaca" : "Bersihkan Percakapan")
                .setMessage(msg)
                .setPositiveButton("Lanjutkan", (dialog, which) ->
                        executeClearConversation(activity, chatDb, chatType, targetId, clearType, onClearedLocally))
                .setNegativeButton("Batal", null)
                .show();
    }

    private static void executeClearConversation(
            Activity activity,
            ChatLocalDatabase chatDb,
            String chatType,
            long targetId,
            String clearType,
            Runnable onClearedLocally
    ) {
        String myNip = resolveCurrentNip(activity);
        HttpService api = RetroClient.getInstance(activity).getApi2();

        api.clearConversationHistory(targetId, chatType, clearType).enqueue(new Callback<SendMessageResponse>() {
            @Override
            public void onResponse(Call<SendMessageResponse> call, Response<SendMessageResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    List<Long> globalIds = response.body().getDeletedForEveryoneIds();
                    if (globalIds != null) {
                        for (Long gid : globalIds) {
                            if (gid != null && gid > 0) {
                                chatDb.softDeleteForEveryoneLocal(gid, myNip);
                            }
                        }
                    }
                    chatDb.softClearConversation(chatType, targetId);
                    chatDb.syncLastMessageFromLocal(chatType, targetId, false);
                    if (onClearedLocally != null) onClearedLocally.run();
                    if ("for_everyone".equals(clearType)) {
                        CustomUiFeedback.showTopBanner(
                                activity, "Percakapan dibersihkan & pesan belum dibaca dihapus untuk semua", CustomUiFeedback.Type.SUCCESS
                        );
                    }
                } else {
                    CustomUiFeedback.showTopBanner(
                            activity,
                            extractErrorMessage(response, "Gagal membersihkan percakapan."),
                            CustomUiFeedback.Type.ERROR
                    );
                }
            }

            @Override
            public void onFailure(Call<SendMessageResponse> call, Throwable t) {
                if (!"for_me".equals(clearType)) {
                    CustomUiFeedback.showTopBanner(
                            activity, "Koneksi gagal saat menghapus pesan untuk semua.", CustomUiFeedback.Type.ERROR
                    );
                }
            }
        });

        if ("for_me".equals(clearType)) {
            chatDb.softClearConversation(chatType, targetId);
            chatDb.syncLastMessageFromLocal(chatType, targetId, false);
            if (onClearedLocally != null) onClearedLocally.run();
            CustomUiFeedback.showTopBanner(
                    activity, "Percakapan dibersihkan dari tampilan Anda", CustomUiFeedback.Type.SUCCESS
            );
        }
    }

    public static String extractErrorMessage(Response<?> response, String fallback) {
        try {
            if (response != null && response.errorBody() != null) {
                String raw = response.errorBody().string();
                if (!TextUtils.isEmpty(raw)) {
                    JSONObject obj = new JSONObject(raw);
                    String msg = obj.optString("message", "");
                    if (!msg.trim().isEmpty()) return msg;
                }
            }
        } catch (Exception ignored) {}
        return fallback;
    }

    private static String resolveCurrentNip(Activity activity) {
        try {
            DatabaseHelper dbHelper = new DatabaseHelper(activity);
            String nip = dbHelper.getCurrentEmployeeNip();
            if (!TextUtils.isEmpty(nip)) return nip.trim();
        } catch (Exception ignored) {}
        return "";
    }
}
