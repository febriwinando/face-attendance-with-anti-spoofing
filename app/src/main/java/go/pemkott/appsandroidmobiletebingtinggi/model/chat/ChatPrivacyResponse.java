package go.pemkott.appsandroidmobiletebingtinggi.model.chat;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class ChatPrivacyResponse {

    public static class PrivacyData {
        @SerializedName("employee_id")
        private long employeeId;

        @SerializedName("privacy_mode")
        private String privacyMode; // "public", "private", "custom"

        @SerializedName("allowed_employee_ids")
        private List<Long> allowedEmployeeIds;

        @SerializedName("allowed_opd_ids")
        private List<Long> allowedOpdIds;

        @SerializedName("read_receipts_enabled")
        private boolean readReceiptsEnabled = true;

        @SerializedName("blocked_employee_ids")
        private List<Long> blockedEmployeeIds;

        @SerializedName("blocked_employees")
        private List<BlockedEmployeeItem> blockedEmployees;

        @SerializedName("profile_photo_privacy")
        private String profilePhotoPrivacy = "everyone";

        @SerializedName("terms_accepted")
        private Integer termsAccepted;

        @SerializedName("chat_terms_accepted")
        private Integer chatTermsAccepted;

        public boolean isTermsAccepted() {
            if (termsAccepted != null && termsAccepted == 1) return true;
            return chatTermsAccepted != null && chatTermsAccepted == 1;
        }

        public void setTermsAccepted(Integer termsAccepted) {
            this.termsAccepted = termsAccepted;
        }

        public boolean isReadReceiptsEnabled() {
            return readReceiptsEnabled;
        }

        public void setReadReceiptsEnabled(boolean readReceiptsEnabled) {
            this.readReceiptsEnabled = readReceiptsEnabled;
        }

        public List<Long> getBlockedEmployeeIds() {
            return blockedEmployeeIds != null ? blockedEmployeeIds : new ArrayList<>();
        }

        public void setBlockedEmployeeIds(List<Long> blockedEmployeeIds) {
            this.blockedEmployeeIds = blockedEmployeeIds;
        }

        public List<BlockedEmployeeItem> getBlockedEmployees() {
            return blockedEmployees != null ? blockedEmployees : new ArrayList<>();
        }

        public void setBlockedEmployees(List<BlockedEmployeeItem> blockedEmployees) {
            this.blockedEmployees = blockedEmployees;
        }

        public String getProfilePhotoPrivacy() {
            return profilePhotoPrivacy != null ? profilePhotoPrivacy : "everyone";
        }

        public void setProfilePhotoPrivacy(String profilePhotoPrivacy) {
            this.profilePhotoPrivacy = profilePhotoPrivacy;
        }

        public long getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(long employeeId) {
            this.employeeId = employeeId;
        }

        public String getPrivacyMode() {
            return privacyMode != null ? privacyMode : "public";
        }

        public void setPrivacyMode(String privacyMode) {
            this.privacyMode = privacyMode;
        }

        public List<Long> getAllowedEmployeeIds() {
            return allowedEmployeeIds != null ? allowedEmployeeIds : new ArrayList<>();
        }

        public void setAllowedEmployeeIds(List<Long> allowedEmployeeIds) {
            this.allowedEmployeeIds = allowedEmployeeIds;
        }

        public List<Long> getAllowedOpdIds() {
            return allowedOpdIds != null ? allowedOpdIds : new ArrayList<>();
        }

        public void setAllowedOpdIds(List<Long> allowedOpdIds) {
            this.allowedOpdIds = allowedOpdIds;
        }
    }

    @SerializedName("status")
    private boolean status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private PrivacyData data;

    public static class BlockedEmployeeItem {
        @SerializedName("id")
        private long id;
        @SerializedName("nama")
        private String nama;
        @SerializedName("nip")
        private String nip;
        @SerializedName("opd_name")
        private String opdName;
        @SerializedName("avatar_url")
        private String avatarUrl;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getNama() {
            return nama != null ? nama : "Pegawai";
        }

        public void setNama(String nama) {
            this.nama = nama;
        }

        public String getNip() {
            return nip != null ? nip : "";
        }

        public void setNip(String nip) {
            this.nip = nip;
        }

        public String getOpdName() {
            return opdName != null ? opdName : "";
        }

        public void setOpdName(String opdName) {
            this.opdName = opdName;
        }

        public String getAvatarUrl() {
            if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
                return null;
            }
            String normalized = go.pemkott.appsandroidmobiletebingtinggi.api.ServerConfig.resolveFotoPegawaiUrl(avatarUrl);
            return (normalized == null || normalized.isEmpty()) ? null : normalized;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
    }

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public PrivacyData getData() {
        return data;
    }
}
