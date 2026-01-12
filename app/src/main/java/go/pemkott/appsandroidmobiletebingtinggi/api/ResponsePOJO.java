package go.pemkott.appsandroidmobiletebingtinggi.api;

import com.google.gson.annotations.SerializedName;

public class ResponsePOJO {


    @SerializedName("status")
    private boolean status;

    @SerializedName("remarks")
    private String remarks;

    @SerializedName("terlambat")
    private int terlambat;

    @SerializedName("id_absen")
    private int idAbsen;

    public boolean isStatus() { return status; }
    public String getRemarks() { return remarks; }
    public int getTerlambat() { return terlambat; }
    public int getIdAbsen() { return idAbsen; }
//    private boolean status;
//    private String remarks;
//
//    public boolean isStatus() {
//        return status;
//    }
//
//    public String getRemarks() {
//        return remarks;
//    }
}
