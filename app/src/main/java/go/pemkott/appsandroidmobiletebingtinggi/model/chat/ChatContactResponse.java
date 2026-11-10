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

    @SerializedName("terms_accepted")
    private Boolean termsAccepted;

    @SerializedName("is_terms_accepted")
    private Boolean isTermsAccepted;

    @SerializedName("chat_terms_accepted")
    private Boolean chatTermsAccepted;

    public Boolean getTermsAccepted() {
        if (termsAccepted != null) return termsAccepted;
        if (isTermsAccepted != null) return isTermsAccepted;
        return chatTermsAccepted;
    }

    public void setTermsAccepted(Boolean termsAccepted) {
        this.termsAccepted = termsAccepted;
    }

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
