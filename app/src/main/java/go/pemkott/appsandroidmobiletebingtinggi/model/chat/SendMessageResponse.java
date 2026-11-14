package go.pemkott.appsandroidmobiletebingtinggi.model.chat;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SendMessageResponse {
    @SerializedName(value = "status", alternate = {"success"})
    private boolean status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private ChatMessageItem data;

    @SerializedName("deleted_for_everyone_count")
    private int deletedForEveryoneCount;

    @SerializedName("deleted_for_me_count")
    private int deletedForMeCount;

    @SerializedName("deleted_for_everyone_ids")
    private List<Long> deletedForEveryoneIds;

    @SerializedName("deleted_count")
    private int deletedCount;

    @SerializedName("deleted_ids")
    private List<Long> deletedIds;

    public boolean isStatus() {
        return status;
    }

    public boolean isSuccess() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public ChatMessageItem getData() {
        return data;
    }

    public int getDeletedForEveryoneCount() {
        return deletedForEveryoneCount;
    }

    public int getDeletedForMeCount() {
        return deletedForMeCount;
    }

    public List<Long> getDeletedForEveryoneIds() {
        return deletedForEveryoneIds;
    }

    public int getDeletedCount() {
        return deletedCount;
    }

    public List<Long> getDeletedIds() {
        return deletedIds;
    }
}
