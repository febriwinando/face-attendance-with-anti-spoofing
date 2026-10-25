package go.pemkott.appsandroidmobiletebingtinggi.model;

import com.google.gson.annotations.SerializedName;

public class Employee {
    @SerializedName("id")
    private Object id;

    @SerializedName("atasan_id1")
    private Object atasan_id1;

    @SerializedName("atasan_id2")
    private Object atasan_id2;

    @SerializedName("position_id")
    private Object position_id;

    @SerializedName("opd_id")
    private Object opd_id;

    @SerializedName("nip")
    private String nip;

    @SerializedName("nama")
    private String nama;

    @SerializedName("email")
    private String email;

    @SerializedName("no_hp")
    private String no_hp;

    @SerializedName("kelompok")
    private String kelompok;

    @SerializedName("s_jabatan")
    private String s_jabatan;

    @SerializedName("eselon")
    private Object eselon;

    @SerializedName("foto")
    private String foto;

    public String getId() {
        if (id == null) return "";
        if (id instanceof Number) {
            return String.valueOf(((Number) id).longValue());
        }
        String str = String.valueOf(id);
        if (str.endsWith(".0")) {
            return str.substring(0, str.length() - 2);
        }
        return str;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public String getAtasan_id1() {
        return atasan_id1 != null ? String.valueOf(atasan_id1) : null;
    }

    public void setAtasan_id1(Object atasan_id1) {
        this.atasan_id1 = atasan_id1;
    }

    public String getAtasan_id2() {
        return atasan_id2 != null ? String.valueOf(atasan_id2) : null;
    }

    public void setAtasan_id2(Object atasan_id2) {
        this.atasan_id2 = atasan_id2;
    }

    public String getPosition_id() {
        return position_id != null ? String.valueOf(position_id) : null;
    }

    public void setPosition_id(Object position_id) {
        this.position_id = position_id;
    }

    public String getOpd_id() {
        if (opd_id == null) return null;
        if (opd_id instanceof Number) {
            return String.valueOf(((Number) opd_id).longValue());
        }
        String str = String.valueOf(opd_id);
        if (str.endsWith(".0")) {
            return str.substring(0, str.length() - 2);
        }
        return str;
    }

    public void setOpd_id(Object opd_id) {
        this.opd_id = opd_id;
    }

    public String getNip() {
        return nip;
    }

    public void setNip(String nip) {
        this.nip = nip;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNo_hp() {
        return no_hp;
    }

    public void setNo_hp(String no_hp) {
        this.no_hp = no_hp;
    }

    public String getKelompok() {
        return kelompok;
    }

    public void setKelompok(String kelompok) {
        this.kelompok = kelompok;
    }

    public String getS_jabatan() {
        return s_jabatan;
    }

    public void setS_jabatan(String s_jabatan) {
        this.s_jabatan = s_jabatan;
    }

    public String getEselon() {
        return eselon != null ? String.valueOf(eselon) : "0";
    }

    public void setEselon(Object eselon) {
        this.eselon = eselon;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    @Override
    public String toString() {
        if (nama != null && !nama.trim().isEmpty()) {
            if (nip != null && !nip.trim().isEmpty()) {
                return nama + " (" + nip + ")";
            }
            return nama;
        }
        return "Pegawai ID " + getId();
    }
}
