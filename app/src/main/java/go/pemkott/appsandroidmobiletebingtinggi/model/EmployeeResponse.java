package go.pemkott.appsandroidmobiletebingtinggi.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EmployeeResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("total")
    private int total;

    @SerializedName("data")

    private List<EmployeesData> data;

    public boolean isSuccess() {
        return success;
    }

    public int getTotal() {
        return total;
    }

    public List<EmployeesData> getData() {
        return data;

    }
}
