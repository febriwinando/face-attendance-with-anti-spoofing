package go.pemkott.appsandroidmobiletebingtinggi.model.chat;

import com.google.gson.annotations.SerializedName;

public class ChatMessageItem {
    @SerializedName("id")
    private long id;

    @SerializedName("sender_id")
    private long senderId;

    @SerializedName("sender_name")
    private String senderName;

    @SerializedName("receiver_id")
    private Long receiverId;

    @SerializedName("opd_id")
    private Long opdId;

    @SerializedName("chat_type")
    private String chatType;

    @SerializedName("message")
    private String message;

    @SerializedName("msg_type")
    private String msgType; // "text", "image", "document"

    @SerializedName("file_url")
    private String fileUrl;

    @SerializedName("file_name")
    private String fileName;

    @SerializedName("reply_to_id")
    private Long replyToId;

    @SerializedName("reply_to_sender_name")
    private String replyToSenderName;

    @SerializedName("reply_to_message")
    private String replyToMessage;

    @SerializedName("is_me")
    private boolean isMe;

    @SerializedName("is_read")
    private boolean isRead;

    @SerializedName("created_at_formatted")
    private String createdAtFormatted;

    @SerializedName("created_at_ts")
    private long createdAtTs;

    @com.google.gson.annotations.JsonAdapter(go.pemkott.appsandroidmobiletebingtinggi.api.FlexibleFlagIntAdapter.class)
    @SerializedName("is_deleted")
    private int isDeleted;

    @SerializedName("deleted_for_users")
    private String deletedForUsers;

    @com.google.gson.annotations.JsonAdapter(go.pemkott.appsandroidmobiletebingtinggi.api.FlexibleFlagIntAdapter.class)
    @SerializedName("is_deleted_global")
    private int isDeletedGlobal;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSenderId() {
        return senderId;
    }

    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName != null ? senderName : "Pegawai";
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public Long getOpdId() {
        return opdId;
    }

    public void setOpdId(Long opdId) {
        this.opdId = opdId;
    }

    public String getChatType() {
        return chatType;
    }

    public void setChatType(String chatType) {
        this.chatType = chatType;
    }

    public String getMessage() {
        return message != null ? message : "";
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMsgType() {
        return msgType != null ? msgType : "text";
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getFileUrl() {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            return null;
        }
        String normalized = go.pemkott.appsandroidmobiletebingtinggi.api.ServerConfig.normalizeServerUrl(fileUrl);
        return (normalized == null || normalized.isEmpty()) ? null : normalized;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileName() {
        return fileName != null ? fileName : "Dokumen";
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getReplyToId() {
        return replyToId;
    }

    public void setReplyToId(Long replyToId) {
        this.replyToId = replyToId;
    }

    public String getReplyToSenderName() {
        return replyToSenderName != null ? replyToSenderName : "";
    }

    public void setReplyToSenderName(String replyToSenderName) {
        this.replyToSenderName = replyToSenderName;
    }

    public String getReplyToMessage() {
        return replyToMessage != null ? replyToMessage : "";
    }

    public void setReplyToMessage(String replyToMessage) {
        this.replyToMessage = replyToMessage;
    }

    public boolean isMe() {
        return isMe;
    }

    public void setMe(boolean me) {
        isMe = me;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getCreatedAtFormatted() {
        return createdAtFormatted != null ? createdAtFormatted : "";
    }

    public void setCreatedAtFormatted(String createdAtFormatted) {
        this.createdAtFormatted = createdAtFormatted;
    }

    public long getCreatedAtTs() {
        return createdAtTs;
    }

    public void setCreatedAtTs(long createdAtTs) {
        this.createdAtTs = createdAtTs;
    }

    public int getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(int isDeleted) {
        this.isDeleted = isDeleted;
    }

    public boolean isDeleted() {
        return isDeleted == 1 || isDeletedGlobal == 1;
    }

    public String getDeletedForUsers() {
        return deletedForUsers;
    }

    public void setDeletedForUsers(String deletedForUsers) {
        this.deletedForUsers = deletedForUsers;
    }

    public int getIsDeletedGlobal() {
        return isDeletedGlobal;
    }

    public void setIsDeletedGlobal(int isDeletedGlobal) {
        this.isDeletedGlobal = isDeletedGlobal;
    }

    @com.google.gson.annotations.SerializedName("read_at")
    private String readAt;

    @com.google.gson.annotations.JsonAdapter(go.pemkott.appsandroidmobiletebingtinggi.api.FlexibleFlagIntAdapter.class)
    @com.google.gson.annotations.SerializedName("deleted_for_sender")
    private int deletedForSender;

    @com.google.gson.annotations.JsonAdapter(go.pemkott.appsandroidmobiletebingtinggi.api.FlexibleFlagIntAdapter.class)
    @com.google.gson.annotations.SerializedName("deleted_for_receiver")
    private int deletedForReceiver;

    @com.google.gson.annotations.JsonAdapter(go.pemkott.appsandroidmobiletebingtinggi.api.FlexibleFlagIntAdapter.class)
    @com.google.gson.annotations.SerializedName("deleted_for_everyone")
    private int deletedForEveryone;

    @com.google.gson.annotations.SerializedName("deleted_by")
    private String deletedBy;

    @com.google.gson.annotations.SerializedName("deleted_at")
    private String deletedAt;

    public String getReadAt() {
        return readAt;
    }

    public void setReadAt(String readAt) {
        this.readAt = readAt;
    }

    public int getDeletedForSender() {
        return deletedForSender;
    }

    public void setDeletedForSender(int deletedForSender) {
        this.deletedForSender = deletedForSender;
    }

    public int getDeletedForReceiver() {
        return deletedForReceiver;
    }

    public void setDeletedForReceiver(int deletedForReceiver) {
        this.deletedForReceiver = deletedForReceiver;
    }

    public int getDeletedForEveryone() {
        return deletedForEveryone;
    }

    public void setDeletedForEveryone(int deletedForEveryone) {
        this.deletedForEveryone = deletedForEveryone;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public String getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(String deletedAt) {
        this.deletedAt = deletedAt;
    }

    public boolean isDeletedForEveryone() {
        return deletedForEveryone == 1 || isDeletedGlobal == 1;
    }

    public boolean isDeletedGlobal() {
        return isDeletedGlobal == 1 || deletedForEveryone == 1;
    }

    public boolean canDeleteForEveryone() {
        return isMe() && !isRead() && (readAt == null || readAt.trim().isEmpty()) && !isDeleted() && !isDeletedGlobal();
    }

    public boolean isDeletedForUser(String userId) {
        if (deletedForEveryone == 1 || isDeletedGlobal == 1) return true;
        if (isMe() && (deletedForSender == 1 || isDeleted == 1)) return true;
        if (!isMe() && "personal".equalsIgnoreCase(getChatType()) && deletedForReceiver == 1) return true;
        if (userId != null && !userId.trim().isEmpty() && deletedForUsers != null && !deletedForUsers.trim().isEmpty()) {
            String trimmed = userId.trim();
            String padded = deletedForUsers.startsWith(",") ? deletedForUsers : "," + deletedForUsers;
            if (!padded.endsWith(",")) padded = padded + ",";
            return padded.contains("," + trimmed + ",");
        }
        return isDeleted == 1;
    }
}
