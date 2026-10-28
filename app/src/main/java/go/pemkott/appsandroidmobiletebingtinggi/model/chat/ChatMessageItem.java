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

    @SerializedName("is_me")
    private boolean isMe;

    @SerializedName("is_read")
    private boolean isRead;

    @SerializedName("created_at_formatted")
    private String createdAtFormatted;

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
}
