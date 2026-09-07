package go.pemkott.appsandroidmobiletebingtinggi.model;

public class LogAktivitas {
    private int id;
    private String employeeId;
    private String opdId;
    private String employeeName;
    private String opdName;
    private String tanggal;
    private String kegiatan;
    private String jenisAbsen;
    private String deviceId;
    private String timestamp;

    public LogAktivitas(int id, String employeeId, String opdId, String employeeName, String opdName, String tanggal, String kegiatan, String jenisAbsen, String deviceId, String timestamp) {
        this.id = id;
        this.employeeId = employeeId;
        this.opdId = opdId;
        this.employeeName = employeeName;
        this.opdName = opdName;
        this.tanggal = tanggal;
        this.kegiatan = kegiatan;
        this.jenisAbsen = jenisAbsen;
        this.deviceId = deviceId;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public String getEmployeeId() { return employeeId; }
    public String getOpdId() { return opdId; }
    public String getEmployeeName() { return employeeName; }
    public String getOpdName() { return opdName; }
    public String getTanggal() { return tanggal; }
    public String getKegiatan() { return kegiatan; }
    public String getJenisAbsen() { return jenisAbsen; }
    public String getDeviceId() { return deviceId; }
    public String getTimestamp() { return timestamp; }
}
