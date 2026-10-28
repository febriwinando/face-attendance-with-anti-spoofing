package go.pemkott.appsandroidmobiletebingtinggi.model.chat;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import go.pemkott.appsandroidmobiletebingtinggi.model.OpdModel;

public class ChatMonitoringResponse {
    @SerializedName("status")
    private boolean status;

    @SerializedName("message")
    private String message;

    @SerializedName("threads")
    private List<ChatMonitoringThread> threads;

    @SerializedName("opd_list")
    private List<OpdModel> opdList;

    public boolean isStatus() { return status; }
    public String getMessage() { return message; }
    public List<ChatMonitoringThread> getThreads() { return threads; }
    public List<OpdModel> getOpdList() { return opdList; }
}
