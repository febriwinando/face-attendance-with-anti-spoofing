package go.pemkott.appsandroidmobiletebingtinggi.model.chat;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ChatAuditLogsResponse {
    @SerializedName(value = "status", alternate = {"success"})
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<ChatAuditLogItem> data;

    @SerializedName("pagination")
    private ChatPagination pagination;

    public boolean isStatus() {
        return success;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<ChatAuditLogItem> getData() {
        return data;
    }

    public ChatPagination getPagination() {
        return pagination;
    }
}
