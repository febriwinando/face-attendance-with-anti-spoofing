package go.pemkott.appsandroidmobiletebingtinggi.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AduanHelpdesk {
    @SerializedName("id")
    private int id;

    @SerializedName("nomor")
    private String nomor;

    @SerializedName("judul")
    private String judul;

    @SerializedName("kategori")
    private String kategori;

    @SerializedName("deskripsi")
    private String deskripsi;

    @SerializedName("prioritas")
    private String prioritas;

    @SerializedName("status")
    private String status;

    @SerializedName("lokasi_aduan")
    private String lokasiAduan;

    @SerializedName("gambar")
    private List<String> gambar;

    @SerializedName("created_at")
    private String createdAt;

    // Detail fields
    @SerializedName("catatan_opd")
    private String catatanOpd;

    @SerializedName("catatan_kota")
    private String catatanKota;

    @SerializedName("alasan_tolak")
    private String alasanTolak;

    @SerializedName("riwayat")
    private List<AduanRiwayat> riwayat;

    // Getters and Seters
    public int getId() { return id; }
    public String getNomor() { return nomor; }
    public String getJudul() { return judul; }
    public String getKategori() { return kategori; }
    public String getDeskripsi() { return deskripsi; }
    public String getPrioritas() { return prioritas; }
    public String getStatus() { return status; }
    public String getLokasiAduan() { return lokasiAduan; }
    public List<String> getGambar() { return gambar; }
    public String getCreatedAt() { return createdAt; }
    public String getCatatanOpd() { return catatanOpd; }
    public String getCatatanKota() { return catatanKota; }
    public String getAlasanTolak() { return alasanTolak; }
    public List<AduanRiwayat> getRiwayat() { return riwayat; }
}
