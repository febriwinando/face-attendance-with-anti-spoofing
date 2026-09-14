package go.pemkott.appsandroidmobiletebingtinggi.model;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("id")
    private String id;

    @SerializedName("employee_id")
    private String employeeId;

    @SerializedName("username")
    private String username;

    @SerializedName("akses")
    private String akses;

    @SerializedName("role")
    private String role;

    @SerializedName("active")
    private String active;

    public String getId() {
        return id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getUsername() {
        return username;
    }

    public String getAkses() {
        return akses;
    }

    public String getRole() {
        return role;
    }

    public String getActive() {
        return active;
    }
}