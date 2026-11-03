package go.pemkott.appsandroidmobiletebingtinggi.model;

public class PreviewItemModel {
    private String nip;
    private String nama;
    private String opd;
    private String tipePegawai;
    private String status;
    private String badge;
    private String keterangan;
    private String jamMasuk;
    private String jamPulang;
    private boolean canExecute;
    private String actionType;
    private String presensiId;

    public PreviewItemModel() {}

    public String getNip() { return nip != null ? nip : "-"; }
    public void setNip(String nip) { this.nip = nip; }

    public String getNama() { return nama != null ? nama : "-"; }
    public void setNama(String nama) { this.nama = nama; }

    public String getOpd() { return opd != null ? opd : "-"; }
    public void setOpd(String opd) { this.opd = opd; }

    public String getTipePegawai() { return tipePegawai != null ? tipePegawai : "Non-Shift"; }
    public void setTipePegawai(String tipePegawai) { this.tipePegawai = tipePegawai; }

    public String getStatus() { return status != null ? status : "Preview"; }
    public void setStatus(String status) { this.status = status; }

    public String getBadge() { return badge != null ? badge : "info"; }
    public void setBadge(String badge) { this.badge = badge; }

    public String getKeterangan() { return keterangan != null ? keterangan : ""; }
    public void setKeterangan(String keterangan) { this.keterangan = keterangan; }

    public String getJamMasuk() { return jamMasuk != null ? jamMasuk : "-"; }
    public void setJamMasuk(String jamMasuk) { this.jamMasuk = jamMasuk; }

    public String getJamPulang() { return jamPulang != null ? jamPulang : "-"; }
    public void setJamPulang(String jamPulang) { this.jamPulang = jamPulang; }

    public boolean isCanExecute() { return canExecute; }
    public void setCanExecute(boolean canExecute) { this.canExecute = canExecute; }

    public String getActionType() { return actionType != null ? actionType : "skip"; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getPresensiId() { return presensiId != null ? presensiId : "-"; }
    public void setPresensiId(String presensiId) { this.presensiId = presensiId; }
}
