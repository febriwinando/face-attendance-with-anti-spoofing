package go.pemkott.appsandroidmobiletebingtinggi.model.chat;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OpdGroupProfileResponse {
    @SerializedName("status")
    private boolean status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private OpdGroupData data;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public OpdGroupData getData() {
        return data;
    }

    public void setData(OpdGroupData data) {
        this.data = data;
    }

    public static class OpdGroupData {
        @SerializedName("opd_id")
        private long opdId;

        @SerializedName("opd_name")
        private String opdName;

        @SerializedName("description")
        private String description;

        @SerializedName("member_count")
        private int memberCount;

        @SerializedName("members")
        private List<OpdMemberItem> members;

        public long getOpdId() {
            return opdId;
        }

        public void setOpdId(long opdId) {
            this.opdId = opdId;
        }

        public String getOpdName() {
            return opdName;
        }

        public void setOpdName(String opdName) {
            this.opdName = opdName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getMemberCount() {
            return memberCount;
        }

        public void setMemberCount(int memberCount) {
            this.memberCount = memberCount;
        }

        public List<OpdMemberItem> getMembers() {
            return members;
        }

        public void setMembers(List<OpdMemberItem> members) {
            this.members = members;
        }
    }

    public static class OpdMemberItem {
        @SerializedName("id")
        private long id;

        @SerializedName("nama")
        private String nama;

        @SerializedName("nip")
        private String nip;

        @SerializedName("jabatan")
        private String jabatan;

        @SerializedName("avatar_url")
        private String avatarUrl;

        @SerializedName("is_me")
        private boolean isMe;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getNama() {
            return nama;
        }

        public void setNama(String nama) {
            this.nama = nama;
        }

        public String getNip() {
            return nip;
        }

        public void setNip(String nip) {
            this.nip = nip;
        }

        public String getJabatan() {
            return jabatan;
        }

        public void setJabatan(String jabatan) {
            this.jabatan = jabatan;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }

        public boolean isMe() {
            return isMe;
        }

        public void setMe(boolean me) {
            isMe = me;
        }
    }
}

