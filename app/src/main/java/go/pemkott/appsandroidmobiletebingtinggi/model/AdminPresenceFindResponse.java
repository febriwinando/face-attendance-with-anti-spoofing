package go.pemkott.appsandroidmobiletebingtinggi.model;

import com.google.gson.annotations.SerializedName;

public class AdminPresenceFindResponse {
    @SerializedName("status")
    private boolean status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Presensi data;

    public boolean isStatus() { return status; }
    public String getMessage() { return message; }
    public Presensi getData() { return data; }
}
