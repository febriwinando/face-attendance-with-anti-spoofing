package go.pemkott.appsandroidmobiletebingtinggi.model.chat;

import com.google.gson.annotations.SerializedName;

public class ChatAuditLogItem {
    @SerializedName("id")
    private long id;

    @SerializedName("message_id")
    private long messageId;

    @SerializedName("chat_type")
    private String chatType;

    @SerializedName("action")
    private String action;

    @SerializedName("actor_nip")
    private String actorNip;

    @SerializedName("actor_name")
    private String actorName;

    @SerializedName("message_snapshot")
    private String messageSnapshot;

    @SerializedName("file_url_snapshot")
    private String fileUrlSnapshot;

    @SerializedName("was_read")
    private boolean wasRead;

    @SerializedName("is_permanent")
    private boolean isPermanent;

    @SerializedName("created_at")
    private String createdAt;

    public long getId() {
        return id;
    }

    public long getMessageId() {
        return messageId;
    }

    public String getChatType() {
        return chatType;
    }

    public String getAction() {
        return action;
    }

    public String getActorNip() {
        return actorNip;
    }

    public String getActorName() {
        return actorName;
    }

    public String getMessageSnapshot() {
        return messageSnapshot;
    }

    public String getFileUrlSnapshot() {
        if (fileUrlSnapshot == null || fileUrlSnapshot.trim().isEmpty()) {
            return null;
        }
        String normalized = go.pemkott.appsandroidmobiletebingtinggi.api.ServerConfig.normalizeServerUrl(fileUrlSnapshot);
        return (normalized == null || normalized.isEmpty()) ? null : normalized;
    }

    public boolean isWasRead() {
        return wasRead;
    }

    public boolean isPermanent() {
        return isPermanent;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getActionLabel() {
        if (action == null) return "-";
        switch (action) {
            case "delete_for_sender":
                return "Dihapus Pengirim (Untuk Saya)";
            case "delete_for_receiver":
                return "Dihapus Penerima (Untuk Saya)";
            case "delete_for_everyone":
            case "clear_for_everyone":
                return "Dihapus untuk Semua";
            case "clear_for_me":
                return "Bersihkan Obrolan (Untuk Saya)";
            case "permanent_delete":
                return "HAPUS PERMANEN";
            case "restore_message":
                return "Dipulihkan Admin";
            default:
                return action;
        }
    }
}

