package go.pemkott.appsandroidmobiletebingtinggi.model.chat;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ChatMonitoringMessage {
    @SerializedName("id")
    private long id;

    @SerializedName("chat_type")
    private String chatType;

    @SerializedName("sender_id")
    private long senderId;

    @SerializedName("sender_name")
    private String senderName;

    @SerializedName("sender_nip")
    private String senderNip;

    @SerializedName("sender_opd_name")
    private String senderOpdName;

    @SerializedName("receiver_id")
    private long receiverId;

    @SerializedName("receiver_name")
    private String receiverName;

    @SerializedName("receiver_nip")
    private String receiverNip;

    @SerializedName("receiver_opd_name")
    private String receiverOpdName;

    @SerializedName("opd_id")
    private long opdId;

    @SerializedName("opd_name")
    private String opdName;

    @SerializedName("message")
    private String message;

    @SerializedName("msg_type")
    private String msgType;

    @SerializedName("file_url")
    private String fileUrl;

    @SerializedName("file_name")
    private String fileName;

    @SerializedName("is_read")
    private int isRead;

    @SerializedName("read_at")
    private String readAt;

    @SerializedName("deleted_for_sender")
    private int deletedForSender;

    @SerializedName("deleted_for_receiver")
    private int deletedForReceiver;

    @SerializedName("deleted_for_everyone")
    private int deletedForEveryone;

    @SerializedName("deleted_by")
    private String deletedBy;

    @SerializedName("deleted_at")
    private String deletedAt;

    @SerializedName("delete_status")
    private String deleteStatus;

    @SerializedName("is_deleted_any")
    private int isDeletedAny;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("created_at_formatted")
    private String createdAtFormatted;

    @SerializedName("deletion_history")
    private List<ChatAuditLogItem> deletionHistory;

    private transient boolean selected = false;

    public long getId() {
        return id;
    }

    public String getChatType() {
        return chatType;
    }

    public long getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getSenderNip() {
        return senderNip;
    }

    public String getSenderOpdName() {
        return senderOpdName;
    }

    public long getReceiverId() {
        return receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getReceiverNip() {
        return receiverNip;
    }

    public String getReceiverOpdName() {
        return receiverOpdName;
    }

    public long getOpdId() {
        return opdId;
    }

    public String getOpdName() {
        return opdName;
    }

    public String getMessage() {
        return message;
    }

    public String getMsgType() {
        return msgType;
    }

    public String getFileUrl() {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            return null;
        }
        String normalized = go.pemkott.appsandroidmobiletebingtinggi.api.ServerConfig.normalizeServerUrl(fileUrl);
        return (normalized == null || normalized.isEmpty()) ? null : normalized;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isRead() {
        return isRead == 1 || (readAt != null && !readAt.trim().isEmpty());
    }

    public String getReadAt() {
        return readAt;
    }

    public boolean isDeletedForSender() {
        return deletedForSender == 1;
    }

    public boolean isDeletedForReceiver() {
        return deletedForReceiver == 1;
    }

    public boolean isDeletedForEveryone() {
        return deletedForEveryone == 1;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public String getDeletedAt() {
        return deletedAt;
    }

    public String getDeleteStatus() {
        return deleteStatus;
    }

    public boolean isDeletedAny() {
        return isDeletedAny == 1 || (deleteStatus != null && !"active".equalsIgnoreCase(deleteStatus));
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getCreatedAtFormatted() {
        return createdAtFormatted;
    }

    public List<ChatAuditLogItem> getDeletionHistory() {
        return deletionHistory;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getReadableDeleteStatusLabel() {
        if ("deleted_for_everyone".equalsIgnoreCase(deleteStatus)) {
            return "Dihapus untuk Semua";
        }
        if ("deleted_for_both".equalsIgnoreCase(deleteStatus)) {
            return "Dihapus Pengirim & Penerima";
        }
        if ("deleted_for_sender".equalsIgnoreCase(deleteStatus)) {
            return "Dihapus Pengirim";
        }
        if ("deleted_for_receiver".equalsIgnoreCase(deleteStatus)) {
            return "Dihapus Penerima";
        }
        return "Aktif";
    }
}

