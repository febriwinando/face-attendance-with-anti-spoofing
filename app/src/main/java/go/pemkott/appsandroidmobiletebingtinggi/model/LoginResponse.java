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

    @SerializedName("require_2fa")
    private boolean require2fa;

    @SerializedName("setup_required")
    private boolean setupRequired;

    @SerializedName("secret_key")
    private String secretKey;

    @SerializedName("qr_code")
    private String qrCode;

    @SerializedName("message")
    private String message;

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

    public boolean isRequire2fa() {
        return require2fa;
    }

    public boolean isSetupRequired() {
        return setupRequired;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getQrCode() {
        return qrCode;
    }

    public String getMessage() {
        return message;
    }
}