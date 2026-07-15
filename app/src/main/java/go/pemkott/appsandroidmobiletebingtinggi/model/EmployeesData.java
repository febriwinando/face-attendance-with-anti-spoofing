package go.pemkott.appsandroidmobiletebingtinggi.model;

import com.google.gson.annotations.SerializedName;

public class EmployeesData {
    @SerializedName("id")
    private long id;

    @SerializedName("atasan_id1")
    private Long atasanId1;

    @SerializedName("atasan_id2")
    private Long atasanId2;

    @SerializedName("position_id")
    private Long positionId;

    @SerializedName("opd_id")
    private long opdId;

    @SerializedName("nip")
    private String nip;

    @SerializedName("nama")
    private String nama;

    @SerializedName("email")
    private String email;

    @SerializedName("no_hp")
    private String noHp;

    @SerializedName("kelompok")
    private String kelompok;

    @SerializedName("s_jabatan")
    private String sJabatan;

    @SerializedName("eselon")
    private int eselon;

    @SerializedName("foto")
    private String foto;

    @SerializedName("shift")
    private int shift;

    @SerializedName("active")
    private int active;

    @SerializedName("opd")
    private String opd;

    @SerializedName("alamat")
    private String alamat;

    @SerializedName("let")
    private String let;

    @SerializedName("lng")
    private String lng;

    @SerializedName("awal_waktu")
    private String awalWaktu;

    @SerializedName("opd_shift")
    private int opdShift;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getAtasanId1() {
        return atasanId1;
    }

    public void setAtasanId1(Long atasanId1) {
        this.atasanId1 = atasanId1;
    }

    public Long getAtasanId2() {
        return atasanId2;
    }

    public void setAtasanId2(Long atasanId2) {
        this.atasanId2 = atasanId2;
    }

    public Long getPositionId() {
        return positionId;
    }

    public void setPositionId(Long positionId) {
        this.positionId = positionId;
    }

    public long getOpdId() {
        return opdId;
    }

    public void setOpdId(long opdId) {
        this.opdId = opdId;
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

    public String getNoHp() {
        return noHp;
    }

    public void setNoHp(String noHp) {
        this.noHp = noHp;
    }

    public String getKelompok() {
        return kelompok;
    }

    public void setKelompok(String kelompok) {
        this.kelompok = kelompok;
    }

    public String getsJabatan() {
        return sJabatan;
    }

    public void setsJabatan(String sJabatan) {
        this.sJabatan = sJabatan;
    }

    public int getEselon() {
        return eselon;
    }

    public void setEselon(int eselon) {
        this.eselon = eselon;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public int getShift() {
        return shift;
    }

    public void setShift(int shift) {
        this.shift = shift;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public String getOpd() {
        return opd;
    }

    public void setOpd(String opd) {
        this.opd = opd;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public String getLet() {
        return let;
    }

    public void setLet(String let) {
        this.let = let;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getAwalWaktu() {
        return awalWaktu;
    }

    public void setAwalWaktu(String awalWaktu) {
        this.awalWaktu = awalWaktu;
    }

    public int getOpdShift() {
        return opdShift;
    }

    public void setOpdShift(int opdShift) {
        this.opdShift = opdShift;
    }
}
