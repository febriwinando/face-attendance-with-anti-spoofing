package go.pemkott.appsandroidmobiletebingtinggi.model.chat;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class ChatMessageResponse {
    @SerializedName("status")
    private boolean status;

    @SerializedName("message")
    private String message;

    @SerializedName("messages")
    private List<ChatMessageItem> messages;

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<ChatMessageItem> getMessages() {
        return messages != null ? messages : new ArrayList<>();
    }
}
