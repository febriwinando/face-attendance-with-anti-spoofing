package go.pemkott.appsandroidmobiletebingtinggi.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("user")
    private User user;

    @SerializedName("nama")
    private String nama;

    @SerializedName("token")
    private String token;

    @SerializedName("status")
    private int status;

    public User getUser() {
        return user;
    }

    public String getNama() {
        return nama;
    }

    public String getToken() {
        return token;
    }

    public int getStatus() {
        return status;
    }
}