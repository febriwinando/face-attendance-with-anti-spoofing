package go.pemkott.appsandroidmobiletebingtinggi.model;

import com.google.gson.annotations.SerializedName;

public class AduanDetailResponse {
    @SerializedName("status")
    private boolean status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private AduanHelpdesk data;

    public boolean isStatus() { return status; }
    public String getMessage() { return message; }
    public AduanHelpdesk getData() { return data; }
}
