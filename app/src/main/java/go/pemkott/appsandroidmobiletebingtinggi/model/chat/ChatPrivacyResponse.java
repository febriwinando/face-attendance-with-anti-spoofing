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
