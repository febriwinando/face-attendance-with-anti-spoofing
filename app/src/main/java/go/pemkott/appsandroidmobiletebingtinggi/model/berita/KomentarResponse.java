package go.pemkott.appsandroidmobiletebingtinggi.model.berita;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class KomentarResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("total")
    private int total;

    @SerializedName("data")
    private List<KomentarItem> data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getTotal() {
        return total;
    }

    public List<KomentarItem> getData() {
        return data;
    }
}
