package go.pemkott.appsandroidmobiletebingtinggi.model.berita;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BeritaData {

    @SerializedName("berita_utama")
    private List<BeritaItem> beritaUtama;

    @SerializedName("berita_terbaru")
    private List<BeritaItem> beritaTerbaru;

    @SerializedName("berita_trending")
    private List<BeritaItem> beritaTrending;

    @SerializedName("berita_lainnya")
    private List<BeritaItem> beritaLainnya;

    public List<BeritaItem> getBeritaUtama() {
        return beritaUtama;
    }

    public List<BeritaItem> getBeritaTerbaru() {
        return beritaTerbaru;
    }

    public List<BeritaItem> getBeritaTrending() {
        return beritaTrending;
    }

    public List<BeritaItem> getBeritaLainnya() {
        return beritaLainnya;
    }
}
