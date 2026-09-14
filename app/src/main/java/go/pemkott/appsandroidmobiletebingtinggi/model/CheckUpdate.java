package go.pemkott.appsandroidmobiletebingtinggi.model;

public class CheckUpdate {
    boolean status;
    String message;
    String namapackage;
    int version;


    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNamapackage() {
        return namapackage;
    }

    public void setNamapackage(String namapackage) {
        this.namapackage = namapackage;
    }
}
