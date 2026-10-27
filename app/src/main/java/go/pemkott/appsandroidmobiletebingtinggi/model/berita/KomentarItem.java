package go.pemkott.appsandroidmobiletebingtinggi.model.berita;

import com.google.gson.annotations.SerializedName;

public class KomentarItem {

    @SerializedName("id")
    private long id;

    @SerializedName("beritaId")
    private long beritaId;

    @SerializedName("nama")
    private String nama;

    @SerializedName("email")
    private String email;

    @SerializedName("isi")
    private String isi;

    @SerializedName("createdAt")
    private String createdAt;

    public long getId() {
        return id;
    }

    public long getBeritaId() {
        return beritaId;
    }

    public String getNama() {
        return nama != null && !nama.trim().isEmpty() ? nama.trim() : "Anonim";
    }

    public String getEmail() {
        return email;
    }

    public String getIsi() {
        return isi != null ? isi.trim() : "";
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
