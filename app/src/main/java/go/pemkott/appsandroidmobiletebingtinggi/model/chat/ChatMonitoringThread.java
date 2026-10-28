package go.pemkott.appsandroidmobiletebingtinggi.model.chat;

import com.google.gson.annotations.SerializedName;

public class ChatMonitoringThread {
    @SerializedName("pair_key")
    private String pairKey;

    @SerializedName("sender_id")
    private long senderId;

    @SerializedName("sender_name")
    private String senderName;

    @SerializedName("sender_nip")
    private String senderNip;

    @SerializedName("sender_opd")
    private String senderOpd;

    @SerializedName("receiver_id")
    private long receiverId;

    @SerializedName("receiver_name")
    private String receiverName;

    @SerializedName("receiver_nip")
    private String receiverNip;

    @SerializedName("receiver_opd")
    private String receiverOpd;

    @SerializedName("last_message")
    private String lastMessage;

    @SerializedName("last_time")
    private String lastTime;

    @SerializedName("last_time_ts")
    private long lastTimeTs;

    public String getPairKey() { return pairKey; }
    public long getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getSenderNip() { return senderNip; }
    public String getSenderOpd() { return senderOpd; }
    public long getReceiverId() { return receiverId; }
    public String getReceiverName() { return receiverName; }
    public String getReceiverNip() { return receiverNip; }
    public String getReceiverOpd() { return receiverOpd; }
    public String getLastMessage() { return lastMessage; }
    public String getLastTime() { return lastTime; }
    public long getLastTimeTs() { return lastTimeTs; }
}
