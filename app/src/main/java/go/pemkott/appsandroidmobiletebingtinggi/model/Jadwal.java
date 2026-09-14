package go.pemkott.appsandroidmobiletebingtinggi.model;

public class Jadwal {

    private boolean isChecked = false;
    private String inisial = null;
    private String masuk = null;
    private String pulang = null;

    public String getMasuk() {
        return masuk;
    }

    public void setMasuk(String masuk) {
        this.masuk = masuk;
    }

    public String getPulang() {
        return pulang;
    }

    public void setPulang(String pulang) {
        this.pulang = pulang;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getInisial() {
        return inisial;
    }

    public void setInisial(String inisial) {
        this.inisial = inisial;
    }

}
