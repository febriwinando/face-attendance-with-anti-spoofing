package go.pemkott.appsandroidmobiletebingtinggi.chat;

import android.app.Activity;
import android.graphics.Color;
import android.text.TextUtils;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.database.ChatLocalDatabase;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatAuditLogItem;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatAuditLogsResponse;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMonitoringMessage;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse;
import go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Helper bersama halaman Monitoring Administrator (NIP 199402092020121005)
 * untuk menampilkan inspeksi pesan lengkap, audit log, filter status, serta
 * dialog konfirmasi dua tahap sebelum melakukan penghapusan permanen (SQL DELETE).
 */
public final class ChatMonitoringPermanentDeleteHelper {

    public interface OnStatusSelectedListener {
        void onStatusSelected(String statusCode, String label);
    }

    private ChatMonitoringPermanentDeleteHelper() {}

    public static void showSinglePermanentDelete(
            Activity activity,
            ChatMonitoringMessage message,
            ChatLocalDatabase chatDb,
            Runnable onDeletedSuccess
    ) {
        if (message == null) return;
        showTwoStagePermanentDelete(activity, Collections.singletonList(message), chatDb, onDeletedSuccess);
    }

    public static void showTwoStagePermanentDelete(
            Activity activity,
            List<ChatMonitoringMessage> selectedMessages,
            ChatLocalDatabase chatDb,
            Runnable onDeletedSuccess
    ) {
        if (activity == null || selectedMessages == null || selectedMessages.isEmpty()) return;

        if (!ChatLocalDatabase.isSuperAdminUser(activity)) {
            CustomUiFeedback.showTopBanner(
                    activity,
                    "Akses ditolak. Penghapusan permanen khusus NIP 199402092020121005.",
                    CustomUiFeedback.Type.ERROR
            );
            return;
        }

        String previewSummary = buildSelectedPreviewSummary(selectedMessages);

        // TAHAP KONFIRMASI PERTAMA (Teks & tombol sesuai ketentuan)
        String stage1Message = "Apakah Anda yakin ingin menghapus pesan ini secara permanen? Pesan akan dihapus dari database dan tidak dapat dipulihkan."
                + "\n\nDaftar Pesan Dipilih (" + selectedMessages.size() + "):\n"
                + previewSummary;

        new AlertDialog.Builder(activity)
                .setTitle("Konfirmasi Hapus Permanen (1/2)")
                .setMessage(stage1Message)
                .setNegativeButton("Batal", null)
                .setPositiveButton("Lanjutkan", (dialog, which) ->
                        showStageTwoConfirmation(activity, selectedMessages, chatDb, onDeletedSuccess))
                .show();
    }

    private static void showStageTwoConfirmation(
            Activity activity,
            List<ChatMonitoringMessage> selectedMessages,
            ChatLocalDatabase chatDb,
            Runnable onDeletedSuccess
    ) {
        // TAHAP KONFIRMASI KEDUA (Teks peringatan tegas & tombol sesuai ketentuan)
        String stage2Message = "PERINGATAN! Anda akan menghapus pesan secara permanen. Tindakan ini tidak dapat dibatalkan. Pesan tidak akan dapat dilihat kembali, termasuk oleh Administrator Monitoring Chat."
                + "\n\nTotal pesan yang akan dihapus dari database: " + selectedMessages.size() + " pesan.";

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("PERINGATAN KERAS (2/2)")
                .setMessage(stage2Message)
                .setNegativeButton("Batal", null)
                .setPositiveButton("Hapus Permanen", (d, w) ->
                        executePermanentDelete(activity, selectedMessages, chatDb, onDeletedSuccess))
                .create();

        dialog.show();
        Button positiveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (positiveBtn != null) {
            positiveBtn.setTextColor(Color.parseColor("#B91C1C"));
        }
    }

    private static void executePermanentDelete(
            Activity activity,
            List<ChatMonitoringMessage> selectedMessages,
            ChatLocalDatabase chatDb,
            Runnable onDeletedSuccess
    ) {
        List<Long> idList = new ArrayList<>();
        StringBuilder csv = new StringBuilder();
        for (ChatMonitoringMessage m : selectedMessages) {
            if (m != null && m.getId() > 0) {
                idList.add(m.getId());
                if (csv.length() > 0) csv.append(",");
                csv.append(m.getId());
            }
        }

        if (idList.isEmpty()) return;

        HttpService api = RetroClient.getInstance(activity).getApi2();
        api.permanentDeleteMonitoringMessages(csv.toString()).enqueue(new Callback<SendMessageResponse>() {
            @Override
            public void onResponse(Call<SendMessageResponse> call, Response<SendMessageResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    if (chatDb != null) {
                        chatDb.hardDeleteMessagesLocal(idList);
                    }
                    CustomUiFeedback.showTopBanner(
                            activity,
                            response.body().getMessage() != null
                                    ? response.body().getMessage()
                                    : (idList.size() + " pesan dihapus permanen dari database"),
                            CustomUiFeedback.Type.SUCCESS
                    );
                    if (onDeletedSuccess != null) onDeletedSuccess.run();
                } else {
                    String err = ChatDeletionUiHelper.extractErrorMessage(
                            response, "Gagal melakukan penghapusan permanen di server."
                    );
                    CustomUiFeedback.showTopBanner(activity, err, CustomUiFeedback.Type.ERROR);
                }
            }

            @Override
            public void onFailure(Call<SendMessageResponse> call, Throwable t) {
                CustomUiFeedback.showTopBanner(
                        activity, "Koneksi gagal saat menghapus permanen: " + t.getMessage(), CustomUiFeedback.Type.ERROR
                );
            }
        });
    }

    private static String buildSelectedPreviewSummary(List<ChatMonitoringMessage> messages) {
        StringBuilder sb = new StringBuilder();
        int maxShown = Math.min(5, messages.size());
        for (int i = 0; i < maxShown; i++) {
            ChatMonitoringMessage m = messages.get(i);
            String body = !TextUtils.isEmpty(m.getMessage()) ? m.getMessage().trim()
                    : (!TextUtils.isEmpty(m.getFileName()) ? "[File: " + m.getFileName() + "]" : "[Media]");
            if (body.length() > 70) {
                body = body.substring(0, 67) + "...";
            }
            sb.append("• #").append(m.getId())
                    .append(" (").append(safe(m.getSenderName())).append("): \"")
                    .append(body).append("\" [").append(m.getReadableDeleteStatusLabel()).append("]\n");
        }
        if (messages.size() > maxShown) {
            sb.append("...dan ").append(messages.size() - maxShown).append(" pesan lainnya.\n");
        }
        return sb.toString().trim();
    }

    public static void showMessageInspectorDialog(
            Activity activity,
            ChatMonitoringMessage msg,
            ChatLocalDatabase chatDb,
            Runnable onChangedCallback
    ) {
        if (activity == null || msg == null) return;

        StringBuilder info = new StringBuilder();
        info.append("ID Pesan: #").append(msg.getId())
                .append(" (").append("opd".equalsIgnoreCase(msg.getChatType()) ? "Grup OPD" : "Personal").append(")\n");
        info.append("Pengirim: ").append(safe(msg.getSenderName()));
        if (!TextUtils.isEmpty(msg.getSenderNip())) info.append(" (NIP: ").append(msg.getSenderNip()).append(")");
        if (!TextUtils.isEmpty(msg.getSenderOpdName())) info.append("\nOPD Pengirim: ").append(msg.getSenderOpdName());

        info.append("\n\nPenerima: ").append(safe(msg.getReceiverName()));
        if (!TextUtils.isEmpty(msg.getReceiverNip())) info.append(" (NIP: ").append(msg.getReceiverNip()).append(")");
        if (!TextUtils.isEmpty(msg.getReceiverOpdName())) info.append("\nOPD Penerima: ").append(msg.getReceiverOpdName());
        else if (!TextUtils.isEmpty(msg.getOpdName())) info.append("\nOPD: ").append(msg.getOpdName());

        info.append("\n\nWaktu Kirim: ").append(safe(msg.getCreatedAt()));
        info.append("\nStatus Baca: ").append(msg.isRead() ? "Sudah Dibaca" : "Belum Dibaca");
        if (!TextUtils.isEmpty(msg.getReadAt())) {
            info.append(" (").append(msg.getReadAt()).append(")");
        }
        info.append("\nStatus Hapus: ").append(msg.getReadableDeleteStatusLabel());
        if (!TextUtils.isEmpty(msg.getDeletedBy())) {
            info.append("\nDihapus Oleh (NIP): ").append(msg.getDeletedBy());
        }
        if (!TextUtils.isEmpty(msg.getDeletedAt())) {
            info.append("\nWaktu Penghapusan: ").append(msg.getDeletedAt());
        }

        info.append("\n\nIsi Pesan Asli:\n");
        info.append(!TextUtils.isEmpty(msg.getMessage()) ? msg.getMessage() : "[Tanpa teks]");
        if (!TextUtils.isEmpty(msg.getFileUrl()) || !TextUtils.isEmpty(msg.getFileName())) {
            info.append("\nLampiran: ").append(safe(msg.getFileName())).append(" (").append(safe(msg.getFileUrl())).append(")");
        }

        List<ChatAuditLogItem> history = msg.getDeletionHistory();
        if (history != null && !history.isEmpty()) {
            info.append("\n\nRiwayat Penghapusan:");
            for (ChatAuditLogItem log : history) {
                info.append("\n- ").append(log.getActionLabel())
                        .append(" oleh ").append(safe(log.getActorNip()))
                        .append(" pada ").append(safe(log.getCreatedAt()));
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle("Detail Pesan & Audit")
                .setMessage(info.toString())
                .setNegativeButton("Tutup", null)
                .setPositiveButton("Hapus Permanen", (d, w) ->
                        showSinglePermanentDelete(activity, msg, chatDb, onChangedCallback));

        if (msg.isDeletedAny()) {
            builder.setNeutralButton("Pulihkan Pesan", (d, w) -> restoreMessage(activity, msg, chatDb, onChangedCallback));
        }

        AlertDialog dialog = builder.create();
        dialog.show();
        Button pos = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (pos != null) pos.setTextColor(Color.parseColor("#B91C1C"));
    }

    private static void restoreMessage(
            Activity activity,
            ChatMonitoringMessage msg,
            ChatLocalDatabase chatDb,
            Runnable onChangedCallback
    ) {
        HttpService api = RetroClient.getInstance(activity).getApi2();
        api.restoreChatMessage(msg.getId()).enqueue(new Callback<SendMessageResponse>() {
            @Override
            public void onResponse(Call<SendMessageResponse> call, Response<SendMessageResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    if (chatDb != null) chatDb.restoreMessage(msg.getId());
                    CustomUiFeedback.showTopBanner(activity, "Pesan berhasil dipulihkan", CustomUiFeedback.Type.SUCCESS);
                    if (onChangedCallback != null) onChangedCallback.run();
                } else {
                    CustomUiFeedback.showTopBanner(
                            activity,
                            ChatDeletionUiHelper.extractErrorMessage(response, "Gagal memulihkan pesan."),
                            CustomUiFeedback.Type.ERROR
                    );
                }
            }

            @Override
            public void onFailure(Call<SendMessageResponse> call, Throwable t) {
                CustomUiFeedback.showTopBanner(activity, "Gagal menghubungi server.", CustomUiFeedback.Type.ERROR);
            }
        });
    }

    public static void showAuditLogsDialog(Activity activity) {
        if (activity == null || !ChatLocalDatabase.isSuperAdminUser(activity)) return;

        HttpService api = RetroClient.getInstance(activity).getApi2();
        api.getMonitoringAuditLogs(0, "", 1, 30).enqueue(new Callback<ChatAuditLogsResponse>() {
            @Override
            public void onResponse(Call<ChatAuditLogsResponse> call, Response<ChatAuditLogsResponse> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().isStatus()) {
                    CustomUiFeedback.showTopBanner(activity, "Gagal memuat audit log penghapusan.", CustomUiFeedback.Type.ERROR);
                    return;
                }

                List<ChatAuditLogItem> logs = response.body().getData();
                StringBuilder sb = new StringBuilder();
                if (logs == null || logs.isEmpty()) {
                    sb.append("Belum ada riwayat penghapusan pesan tercatat.");
                } else {
                    for (ChatAuditLogItem log : logs) {
                        String snippet = !TextUtils.isEmpty(log.getMessageSnapshot()) ? log.getMessageSnapshot() : "[Media/File]";
                        if (snippet.length() > 60) snippet = snippet.substring(0, 57) + "...";
                        sb.append("#").append(log.getMessageId())
                                .append(" • ").append(log.getActionLabel())
                                .append("\nPelaku: ").append(safe(log.getActorName())).append(" (").append(safe(log.getActorNip())).append(")")
                                .append("\nIsi: \"").append(snippet).append("\"")
                                .append("\nWaktu: ").append(safe(log.getCreatedAt()))
                                .append("\n----------------------\n");
                    }
                }

                new AlertDialog.Builder(activity)
                        .setTitle("Audit Log Penghapusan Chat (30 Terbaru)")
                        .setMessage(sb.toString().trim())
                        .setPositiveButton("Tutup", null)
                        .show();
            }

            @Override
            public void onFailure(Call<ChatAuditLogsResponse> call, Throwable t) {
                CustomUiFeedback.showTopBanner(activity, "Koneksi gagal saat memuat audit log.", CustomUiFeedback.Type.ERROR);
            }
        });
    }

    public static void showStatusPickerDialog(Activity activity, OnStatusSelectedListener listener) {
        final String[] labels = new String[]{
                "Semua Status",
                "Aktif (Belum Dihapus)",
                "Semua Terhapus (Logis)",
                "Dihapus Pengirim",
                "Dihapus Penerima",
                "Dihapus untuk Semua",
                "Sudah Dibaca",
                "Belum Dibaca"
        };
        final String[] codes = new String[]{
                "all",
                "active",
                "deleted",
                "deleted_for_sender",
                "deleted_for_receiver",
                "deleted_for_everyone",
                "read",
                "unread"
        };

        new AlertDialog.Builder(activity)
                .setTitle("Filter Status Pesan")
                .setItems(labels, (dialog, which) -> {
                    if (listener != null) {
                        listener.onStatusSelected(codes[which], labels[which]);
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private static String safe(String v) {
        return v == null || v.trim().isEmpty() ? "-" : v.trim();
    }
}

