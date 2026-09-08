package go.pemkott.appsandroidmobiletebingtinggi.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserActivityLogRequest {
    @SerializedName("logs")
    private List<UserActivityLog> logs;

    public UserActivityLogRequest(List<UserActivityLog> logs) {
        this.logs = logs;
    }

    public List<UserActivityLog> getLogs() {
        return logs;
    }
}
