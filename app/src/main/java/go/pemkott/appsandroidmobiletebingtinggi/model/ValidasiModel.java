package go.pemkott.appsandroidmobiletebingtinggi.model;

import com.google.gson.annotations.SerializedName;

public class ValidasiModel {
    @SerializedName("status")
    private boolean status;
    @SerializedName("message")
    private String message;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
