package go.pemkott.appsandroidmobiletebingtinggi.model.chat;

import com.google.gson.annotations.SerializedName;

public class ChatContact {
    @SerializedName("target_id")
    private long targetId;

    @SerializedName("opd_id")
    private long opdId;

    @SerializedName("chat_type")
    private String chatType; // "personal" or "opd"

    @SerializedName("title")
    private String title;

    @SerializedName("subtitle")
    private String subtitle;

    @SerializedName("nip")
    private String nip;

    @SerializedName("avatar_url")
    private String avatarUrl;

    @SerializedName("last_message")
    private String lastMessage;

    @SerializedName("last_time")
    private String lastTime;

    @SerializedName("unread_count")
    private int unreadCount;

    public long getTargetId() {
        return targetId;
    }

    public void setTargetId(long targetId) {
        this.targetId = targetId;
    }

    public long getOpdId() {
        return opdId;
    }

    public void setOpdId(long opdId) {
        this.opdId = opdId;
    }

    public String getChatType() {
        return chatType != null ? chatType : "personal";
    }

    public void setChatType(String chatType) {
        this.chatType = chatType;
    }

    public String getTitle() {
        return title != null ? title : "";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle != null ? subtitle : "";
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getNip() {
        return nip != null ? nip : "";
    }

    public void setNip(String nip) {
        this.nip = nip;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getLastMessage() {
        return lastMessage != null ? lastMessage : "";
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastTime() {
        return lastTime != null ? lastTime : "";
    }

    public void setLastTime(String lastTime) {
        this.lastTime = lastTime;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}
