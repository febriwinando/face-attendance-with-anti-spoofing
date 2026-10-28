package go.pemkott.appsandroidmobiletebingtinggi.model.chat;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import go.pemkott.appsandroidmobiletebingtinggi.model.OpdModel;

public class ChatContactResponse {
    @SerializedName("status")
    private boolean status;

    @SerializedName("message")
    private String message;

    @SerializedName("pegawai")
    private List<ChatContact> pegawai;

    @SerializedName("opd_list")
    private List<OpdModel> opdList;

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<ChatContact> getPegawai() {
        return pegawai != null ? pegawai : new ArrayList<>();
    }

    public List<OpdModel> getOpdList() {
        return opdList != null ? opdList : new ArrayList<>();
    }
}
