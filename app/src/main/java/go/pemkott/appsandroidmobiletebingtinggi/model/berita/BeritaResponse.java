package go.pemkott.appsandroidmobiletebingtinggi.model.berita;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

public class BeritaResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("site_url")
    private String siteUrl;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private JsonElement rawData;

    public boolean isSuccess() {
        return success;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public String getMessage() {
        return message;
    }

    public BeritaData getData() {
        if (rawData != null && rawData.isJsonObject()) {
            try {
                return new Gson().fromJson(rawData, BeritaData.class);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public BeritaItem getDetailItem() {
        if (rawData != null && rawData.isJsonObject()) {
            try {
                return new Gson().fromJson(rawData, BeritaItem.class);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public java.util.List<BeritaItem> getItemList() {
        if (rawData != null && rawData.isJsonArray()) {
            try {
                java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<java.util.List<BeritaItem>>(){}.getType();
                return new Gson().fromJson(rawData, listType);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
