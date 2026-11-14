package go.pemkott.appsandroidmobiletebingtinggi.model.chat;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import go.pemkott.appsandroidmobiletebingtinggi.model.OpdModel;

public class ChatMonitoringResponse {
    @SerializedName(value = "status", alternate = {"success"})
    private boolean status;

    @SerializedName("message")
    private String message;

    @SerializedName(value = "threads", alternate = {"data"})
    private List<ChatMonitoringThread> threads;

    @SerializedName("opd_list")
    private List<OpdModel> opdList;

    @SerializedName("pagination")
    private ChatPagination pagination;

    public boolean isStatus() { return status; }
    public boolean isSuccess() { return status; }
    public String getMessage() { return message; }
    public List<ChatMonitoringThread> getThreads() { return threads; }
    public List<OpdModel> getOpdList() { return opdList; }
    public ChatPagination getPagination() { return pagination; }
}
