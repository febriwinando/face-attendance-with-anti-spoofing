package go.pemkott.appsandroidmobiletebingtinggi.model;

public class LokasiPegawaiModel {
    private String id;
    private String namaAlamat;
    private String tipeLokasi;
    private String lat;
    private String lng;

    public LokasiPegawaiModel(String id, String namaAlamat, String tipeLokasi, String lat, String lng) {
        this.id = id;
        this.namaAlamat = namaAlamat;
        this.tipeLokasi = tipeLokasi;
        this.lat = lat;
        this.lng = lng;
    }

    public String getId() { return id; }
    public String getNamaAlamat() { return namaAlamat; }
    public String getTipeLokasi() { return tipeLokasi; }
    public String getLat() { return lat; }
    public String getLng() { return lng; }
}
