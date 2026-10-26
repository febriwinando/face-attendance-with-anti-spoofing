package go.pemkott.appsandroidmobiletebingtinggi.model;

import com.google.gson.annotations.SerializedName;

public class OpdModel {
    @SerializedName("id")
    private Object id;

    @SerializedName("opd")
    private String opd;

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

    public String getOpd() {
        return opd != null ? opd : "";
    }

    public void setOpd(String opd) {
        this.opd = opd;
    }

    @Override
    public String toString() {
        return getOpd().isEmpty() ? ("OPD ID " + getId()) : getOpd();
    }
}
