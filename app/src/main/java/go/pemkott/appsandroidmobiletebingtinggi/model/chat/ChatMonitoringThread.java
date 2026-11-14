package go.pemkott.appsandroidmobiletebingtinggi.model.chat;

import com.google.gson.annotations.SerializedName;

public class ChatMonitoringThread {
    @SerializedName(value = "pair_key", alternate = {"thread_key"})
    private String pairKey;

    @SerializedName("chat_type")
    private String chatType;

    @SerializedName("opd_id")
    private long opdId;

    @SerializedName("opd_name")
    private String opdName;

    @SerializedName("sender_id")
    private long senderId;

    @SerializedName("sender_name")
    private String senderName;

    @SerializedName("sender_nip")
    private String senderNip;

    @SerializedName(value = "sender_opd", alternate = {"sender_opd_name"})
    private String senderOpd;

    @SerializedName("receiver_id")
    private long receiverId;

    @SerializedName("receiver_name")
    private String receiverName;

    @SerializedName("receiver_nip")
    private String receiverNip;

    @SerializedName(value = "receiver_opd", alternate = {"receiver_opd_name"})
    private String receiverOpd;

    @SerializedName("last_message")
    private String lastMessage;

    @SerializedName("last_message_id")
    private long lastMessageId;

    @SerializedName("last_message_status")
    private String lastMessageStatus;

    @SerializedName("last_message_is_deleted")
    private int lastMessageIsDeleted;

    @SerializedName(value = "last_time", alternate = {"last_message_at"})
    private String lastTime;

    @SerializedName("last_time_ts")
    private long lastTimeTs;

    @SerializedName("total_messages")
    private int totalMessages;

    @SerializedName("deleted_count")
    private int deletedCount;

    @SerializedName("unread_count")
    private int unreadCount;

    @SerializedName("deleted_for_everyone_count")
    private int deletedForEveryoneCount;

    public String getPairKey() { return pairKey; }
    public String getChatType() { return chatType != null ? chatType : "personal"; }
    public long getOpdId() { return opdId; }
    public String getOpdName() { return opdName; }
    public long getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getSenderNip() { return senderNip; }
    public String getSenderOpd() { return senderOpd; }
    public long getReceiverId() { return receiverId; }
    public String getReceiverName() { return receiverName; }
    public String getReceiverNip() { return receiverNip; }
    public String getReceiverOpd() { return receiverOpd; }
    public String getLastMessage() { return lastMessage; }
    public long getLastMessageId() { return lastMessageId; }
    public String getLastMessageStatus() { return lastMessageStatus; }
    public boolean isLastMessageDeleted() { return lastMessageIsDeleted == 1; }
    public String getLastTime() { return lastTime; }
    public long getLastTimeTs() { return lastTimeTs; }
    public int getTotalMessages() { return totalMessages; }
    public int getDeletedCount() { return deletedCount; }
    public int getUnreadCount() { return unreadCount; }
    public int getDeletedForEveryoneCount() { return deletedForEveryoneCount; }

    public boolean isOpdGroup() {
        return "opd".equalsIgnoreCase(getChatType());
    }
}
