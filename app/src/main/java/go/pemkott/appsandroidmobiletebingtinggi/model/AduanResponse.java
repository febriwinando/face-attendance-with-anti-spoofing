package go.pemkott.appsandroidmobiletebingtinggi.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AduanResponse {
    @SerializedName("status")
    private boolean status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<AduanHelpdesk> data;

    @SerializedName("pagination")
    private Pagination pagination;

    public boolean isStatus() { return status; }
    public String getMessage() { return message; }
    public List<AduanHelpdesk> getData() { return data; }
    public Pagination getPagination() { return pagination; }
}
