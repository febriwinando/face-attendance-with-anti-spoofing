package go.pemkott.appsandroidmobiletebingtinggi.login;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "APP_PREFS";
    private static final String KEY_TOKEN = "AUTH_TOKEN";
    private static final String KEY_PEGAWAI_ID = "PEGAWAI_ID";
    private static final String KEY_JABTAN = "JABATAN";
    private static final String KEY_FCM_TOKEN = "FCM_TOKEN";
    private static final String KEY_EMPLOYEE_ID = "KEY_EMPLOYEE_ID";
    private static final String KEY_SSL_BYPASS = "KEY_SSL_BYPASS";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    // -------------------------
    // PEGAWAI ID
    // -------------------------
    public void savePegawaiId(String id) {
        prefs.edit().putString(KEY_PEGAWAI_ID, id).apply();
    }

    public String getPegawaiId() {
        return prefs.getString(KEY_PEGAWAI_ID, "0"); // default 0
    }

    public void savePegawaiLevel(String level) {
        prefs.edit().putString(KEY_JABTAN, level).apply();
    }

    public String getPegawaiLevel() {
        return prefs.getString(KEY_JABTAN, "0"); // default 0
    }

    // -------------------------
    // CLEAR
    // -------------------------
    public void clearSession() {
        prefs.edit().clear().apply();
    }

    // -------------------------
    // FCM TOKEN
    // -------------------------
    public void saveFcmToken(String token) {
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply();
    }

    public String getFcmToken() {
        return prefs.getString(KEY_FCM_TOKEN, null);
    }

    public void saveEmployeeId(String token) {
        prefs.edit().putString(KEY_EMPLOYEE_ID, token).apply();
    }

    public String getEmployeeId() {
        return prefs.getString(KEY_EMPLOYEE_ID, null);
    }

    // -------------------------
    // VERSION CONTROL
    // -------------------------
    public void saveVersionCode(int versionCode) {
        prefs.edit().putInt("LAST_VERSION_CODE", versionCode).apply();
    }

    public int getVersionCode() {
        return prefs.getInt("LAST_VERSION_CODE", 0);
    }

    // -------------------------
    // SSL BYPASS CONTROL
    // -------------------------
    public void saveSslBypass(boolean enabled) {
        prefs.edit().putBoolean(KEY_SSL_BYPASS, enabled).apply();
    }

    public boolean isSslBypassEnabled() {
        return prefs.getBoolean(KEY_SSL_BYPASS, false);
    }

    // -------------------------
    // USERNAME & ROLE VERIFIKATOR
    // -------------------------
    public void saveUsername(String username) {
        prefs.edit().putString("KEY_USERNAME", username).apply();
    }

    public String getUsername() {
        return prefs.getString("KEY_USERNAME", "");
    }

    public void saveVerifikator(String verifikator) {
        prefs.edit().putString("KEY_VERIFIKATOR", verifikator).apply();
    }

    public String getVerifikator() {
        return prefs.getString("KEY_VERIFIKATOR", "");
    }

    // -------------------------
    // CHAT TERMS AGREEMENT
    // -------------------------
    public void setChatTermsAccepted(boolean accepted) {
        prefs.edit().putBoolean("KEY_CHAT_TERMS_ACCEPTED", accepted).apply();
    }

    public boolean isChatTermsAccepted() {
        return prefs.getBoolean("KEY_CHAT_TERMS_ACCEPTED", false);
    }

    // -------------------------
    // SERVER HOST CONFIG
    // -------------------------
    public void saveServerHost(String host) {
        prefs.edit().putString("KEY_SERVER_HOST", host).apply();
    }

    public String getServerHost() {
        return prefs.getString("KEY_SERVER_HOST", null);
    }
}
