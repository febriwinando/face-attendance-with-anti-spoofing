package go.pemkott.appsandroidmobiletebingtinggi.model.chat;

import com.google.gson.annotations.SerializedName;

public class ChatGlobalStatusResponse {
    @SerializedName("status")
    private boolean status;

    @SerializedName("chat_enabled")
    private boolean chatEnabled;

    @SerializedName("is_master_user")
    private boolean isMasterUser;

    @SerializedName("message")
    private String message;

    public boolean isStatus() { return status; }
    public boolean isChatEnabled() { return chatEnabled; }
    public boolean isMasterUser() { return isMasterUser; }
    public String getMessage() { return message; }
}
