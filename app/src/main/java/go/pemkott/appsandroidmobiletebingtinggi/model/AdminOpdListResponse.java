package go.pemkott.appsandroidmobiletebingtinggi.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AdminOpdListResponse {
    @SerializedName("status")
    private boolean status;

    @SerializedName("data")
    private List<OpdModel> data;

    public boolean isStatus() { return status; }
    public List<OpdModel> getData() { return data; }
}
