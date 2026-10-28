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
        return fileUrl;
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
}
