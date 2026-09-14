package go.pemkott.appsandroidmobiletebingtinggi.model;

import com.google.gson.annotations.SerializedName;

public class UserActivityLog {
    private transient int id; // ID lokal, tidak dikirim ke server

    @SerializedName("employee_id")
    private String employeeId;

    @SerializedName("opd_id")
    private String opdId;

    @SerializedName("tanggal")
    private String tanggal;

    @SerializedName("kegiatan")
    private String kegiatan;

    @SerializedName("jenis_absen")
    private String jenisAbsen;

    @SerializedName("device_id")
    private String deviceId;

    @SerializedName("timestamp")
    private String timestamp;

    public UserActivityLog(int id, String employeeId, String opdId, String tanggal, String kegiatan, String jenisAbsen, String deviceId, String timestamp) {
        this.id = id;
        this.employeeId = employeeId;
        this.opdId = opdId;
        this.tanggal = tanggal;
        this.kegiatan = kegiatan;
        this.jenisAbsen = jenisAbsen;
        this.deviceId = deviceId;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getOpdId() {
        return opdId;
    }

    public String getTanggal() {
        return tanggal;
    }

    public String getKegiatan() {
        return kegiatan;
    }

    public String getJenisAbsen() {
        return jenisAbsen;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
