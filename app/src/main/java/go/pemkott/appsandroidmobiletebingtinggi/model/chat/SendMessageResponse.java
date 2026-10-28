package go.pemkott.appsandroidmobiletebingtinggi.model.chat;

import com.google.gson.annotations.SerializedName;

public class SendMessageResponse {
    @SerializedName("status")
    private boolean status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private ChatMessageItem data;

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public ChatMessageItem getData() {
        return data;
    }
}
