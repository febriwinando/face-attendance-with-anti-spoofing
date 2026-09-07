package go.pemkott.appsandroidmobiletebingtinggi.model;

import com.google.gson.annotations.SerializedName;

public class AduanRiwayat {
    @SerializedName("aksi")
    private String aksi;

    @SerializedName("catatan")
    private String catatan;

    @SerializedName("aktor")
    private String aktor;

    @SerializedName("peran")
    private String peran;

    @SerializedName("created_at")
    private String createdAt;

    public String getAksi() { return aksi; }
    public String getCatatan() { return catatan; }
    public String getAktor() { return aktor; }
    public String getPeran() { return peran; }
    public String getCreatedAt() { return createdAt; }
}
