package go.pemkott.appsandroidmobiletebingtinggi.model;

import com.google.gson.annotations.SerializedName;

public class VerifikasiCountResponse {
    @SerializedName("status")
    private boolean status;
    @SerializedName("masuk_v1")
    private int masukV1;
    @SerializedName("masuk_v2")
    private int masukV2;
    @SerializedName("masuk_total")
    private int masukTotal;
    @SerializedName("pulang_v1")
    private int pulangV1;
    @SerializedName("pulang_v2")
    private int pulangV2;
    @SerializedName("pulang_total")
    private int pulangTotal;
    @SerializedName("total")
    private int total;

    public boolean isStatus() {
        return status;
    }

    public int getMasukV1() {
        return masukV1;
    }

    public int getMasukV2() {
        return masukV2;
    }

    public int getMasukTotal() {
        return masukTotal;
    }

    public int getPulangV1() {
        return pulangV1;
    }

    public int getPulangV2() {
        return pulangV2;
    }

    public int getPulangTotal() {
        return pulangTotal;
    }

    public int getTotal() {
        return total;
    }
}

