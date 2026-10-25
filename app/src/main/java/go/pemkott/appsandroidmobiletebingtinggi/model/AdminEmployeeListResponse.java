package go.pemkott.appsandroidmobiletebingtinggi.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AdminEmployeeListResponse {
    @SerializedName("status")
    private boolean status;

    @SerializedName("data")
    private List<Employee> data;

    public boolean isStatus() { return status; }
    public List<Employee> getData() { return data; }
}
