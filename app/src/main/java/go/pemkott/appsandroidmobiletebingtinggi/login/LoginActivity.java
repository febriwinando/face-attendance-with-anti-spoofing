package go.pemkott.appsandroidmobiletebingtinggi.login;


import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallException;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallErrorCode;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.firebase.messaging.FirebaseMessaging;


import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import go.pemkott.appsandroidmobiletebingtinggi.NewDashboard.DashboardVersiOne;
import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.api.ResponsePOJO;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.api.SSLHelper;
import go.pemkott.appsandroidmobiletebingtinggi.api.ServerConfig;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.dialogview.DialogView;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.SIMPLE_FORMAT_TANGGAL;

import javax.net.ssl.SSLHandshakeException;

import go.pemkott.appsandroidmobiletebingtinggi.model.LoginResponse;
import go.pemkott.appsandroidmobiletebingtinggi.model.User;
import go.pemkott.appsandroidmobiletebingtinggi.policy.PrivacyPolicyActivity;
import go.pemkott.appsandroidmobiletebingtinggi.resetpassword.ResetPasswprdActivity;
import go.pemkott.appsandroidmobiletebingtinggi.utils.FcmTokenHelper;
import go.pemkott.appsandroidmobiletebingtinggi.utils.NetworkUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    HttpService httpService;
    String sId;
    String sEmployee_id;
    private String sUsername;
    String sAkses;
    String sActive;
    String status;
    String sToken, sVerifikator;
    private static String URL_DATA = null;

    private TextInputEditText etUsername, etPassword;
    TextView textDb, tvResetPassword;

    //Databases
    DatabaseHelper databaseHelper;

    AppUpdateManager appUpdateManager ;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller =
                new WindowInsetsControllerCompat(
                        getWindow(),
                        getWindow().getDecorView()
                );
        // icon status bar terang/gelap
        controller.setAppearanceLightStatusBars(true);
        // icon navigation terang/gelap
        controller.setAppearanceLightNavigationBars(true);
        setContentView(R.layout.activity_login);

        appUpdateManager = AppUpdateManagerFactory.create(LoginActivity.this);

        session = new SessionManager(this);

        httpService = RetroClient.getInstance().getApi2();
        databaseHelper = new DatabaseHelper(this);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        textDb = findViewById(R.id.textdb);
        tvResetPassword = findViewById(R.id.tvResetPassword);
        TextView tvPrivacyPolicy = findViewById(R.id.tvPrivacyPolicy);

        tvPrivacyPolicy.setOnClickListener(v -> showPrivacyPolicy());

        tvResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentReset = new Intent(LoginActivity.this, ResetPasswprdActivity.class);
//                intentReset.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentReset);
            }
        });

        SwitchMaterial switchSslBypass = findViewById(R.id.switchSslBypass);
        if (switchSslBypass != null) {
            switchSslBypass.setChecked(session.isSslBypassEnabled());
            switchSslBypass.setOnCheckedChangeListener((buttonView, isChecked) -> {
                session.saveSslBypass(isChecked);
                RetroClient.resetInstance();
                if (isChecked) {
                    SSLHelper.disableSSLCertificateChecking();
                } else {
                    SSLHelper.restoreDefaultSSLCertificateChecking();
                }
                String empId = session.getEmployeeId() != null ? session.getEmployeeId() : "-";
                String logText = isChecked ? "Mengaktifkan Bypass SSL Server" : "Menonaktifkan Bypass SSL Server";
                databaseHelper.insertLog(empId, "-", SIMPLE_FORMAT_TANGGAL.format(new Date()), logText, "SSL_BYPASS");
            });
        }

        checkAppUpdate();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        connectionMonitor.registerDefaultNetworkCallback();
//        Variables.isRegistered=true;
        databaseHelper.getReadableDatabase();
        session = new SessionManager(this);
        datauser();

    }

    public void loginUser(View view){
        view.setEnabled(false);
        view.postDelayed(() -> view.setEnabled(true), 2000);

        if (etUsername.getText().toString().trim().isEmpty() || etPassword.getText().toString().trim().isEmpty()){
            dialogView.viewNotifKosong(LoginActivity.this, "Anda harus mengisi NIP/NIK Anda dan kata kunci terlebih dahulu.", "");
        }else{
            if (NetworkUtils.isConnected(this)){
                URL_DATA = ServerConfig.getApiUrl() + "login?";
                login_app();
            }else{
                dialogView.viewNotifKosong(LoginActivity.this, "Mohon periksa koneksi internet Anda.", "");
            }
        }
    }

    private void datauser(){
        if (session.getToken() != null) {
            if (checkAndHandleAppUpdate()) {
                return; // Redirected to DownloadDataActivity
            }

            Intent dashboardActivity = new Intent(LoginActivity.this, DashboardVersiOne.class);
            dashboardActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(dashboardActivity);
            finish();
        }
    }

    private boolean checkAndHandleAppUpdate() {
        int currentVersionCode = 0;
        try {
            currentVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (Exception e) {
            Log.e("UPDATE_HANDLER", "Error getting version code", e);
        }

        int lastSavedVersion = session.getVersionCode();

        if (lastSavedVersion != 0 && currentVersionCode > lastSavedVersion) {
            Log.d("UPDATE_HANDLER", "App updated from " + lastSavedVersion + " to " + currentVersionCode);
            session.saveVersionCode(currentVersionCode);

            // Redirect to DownloadDataActivity to ensure data migration and session integrity
            Intent intent = new Intent(this, DownloadDataActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        } else if (currentVersionCode != 0) {
            session.saveVersionCode(currentVersionCode);
        }
        return false;
    }

    private void checkAppUpdate() {
        appUpdateManager.getAppUpdateInfo()
                .addOnSuccessListener(appUpdateInfo -> {
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                            && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        try {
                            appUpdateManager.startUpdateFlowForResult(
                                    appUpdateInfo,
                                    AppUpdateType.IMMEDIATE,
                                    this,
                                    1001 // REQ_UPDATE
                            );
                        } catch (IntentSender.SendIntentException e) {
                            Log.e("UPDATE_HANDLER", "Failed to start update flow", e);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (e instanceof InstallException) {
                        int errorCode = ((InstallException) e).getErrorCode();
                        if (errorCode == InstallErrorCode.ERROR_APP_NOT_OWNED) {
                            Log.w("UPDATE_HANDLER", "Aplikasi tidak diinstal dari Play Store (sideloaded). Abaikan cek update.");
                            return;
                        }
                    }
                    // Log as warning since this is often an internal Play Store auth issue [6474]
                    Log.w("UPDATE_HANDLER", "Cek update gagal (non-critical): " + e.getMessage());
                });
    }

    DialogView dialogView = new DialogView(LoginActivity.this);
    Dialog dialogproses;

    private void login_app() {

        dialogproses = new Dialog(LoginActivity.this, R.style.DialogStyle);
        dialogproses.setContentView(R.layout.view_proses);
        dialogproses.setCancelable(false);
        dialogproses.show();

        String username = Objects.requireNonNull(
                etUsername.getText()).toString().trim();

        String password = Objects.requireNonNull(
                etPassword.getText()).toString().trim();

        RetroClient
                .getInstance(LoginActivity.this)
                .getApi2()
                .login(username, password)
                .enqueue(new Callback<List<LoginResponse>>() {

                    @Override
                    public void onResponse(Call<List<LoginResponse>> call,
                                           Response<List<LoginResponse>> response) {

                        if (isFinishing()) return;
                        dialogproses.dismiss();

                        if (!response.isSuccessful()) {

                            dialogView.viewNotifKosong(
                                    LoginActivity.this,
                                    "Login gagal",
                                    "Kode : " + response.code());

                            return;
                        }

                        if (response.body() == null ||
                                response.body().isEmpty()) {

                            dialogView.viewNotifKosong(
                                    LoginActivity.this,
                                    "Tidak ada data",
                                    "Silahkan coba kembali");

                            return;
                        }

                        LoginResponse data = response.body().get(0);
                        if (data == null) {
                            dialogView.viewNotifKosong(
                                    LoginActivity.this,
                                    "Login gagal",
                                    "Data respon dari server tidak valid.");
                            return;
                        }

                        if (data.getStatus() == 401) {

                            dialogView.viewNotifKosong(
                                    LoginActivity.this,
                                    "NIP/NIK atau Password tidak sesuai",
                                    "Periksa kembali!");

                            return;
                        }

                        User user = data.getUser();
                        if (user == null) {
                            dialogView.viewNotifKosong(
                                    LoginActivity.this,
                                    "Login gagal",
                                    "Data pengguna tidak ditemukan dalam respon server.");
                            return;
                        }

                        sId = user.getId();
                        sEmployee_id = user.getEmployeeId();
                        sAkses = user.getAkses();
                        sActive = user.getActive();
                        sVerifikator = user.getRole();

                        sToken = data.getToken();
                        sUsername = data.getNama();

                        session.saveToken("Bearer " + sToken);
                        session.savePegawaiId(sId);
                        session.saveEmployeeId(sEmployee_id);
                        session.saveUsername(username);
                        session.saveVerifikator(sVerifikator);
                        etPassword.setText("");

                        if ("1".equals(sActive)) {
                            databaseHelper.insertLog(sEmployee_id, "-", SIMPLE_FORMAT_TANGGAL.format(new Date()), "Melakukan Login Aplikasi", "Login");

                            databaseHelper.insertDataUserLogin(
                                    sId,
                                    sEmployee_id,
                                    sUsername,
                                    sAkses,
                                    sActive,
                                    sToken,
                                    sVerifikator
                            );

                            fetchFcmTokenAndSend();

                        } else {

                            dialogView.viewNotifKosong(
                                    LoginActivity.this,
                                    "User tidak aktif",
                                    "Silahkan hubungi bagian umum unit kerja anda");

                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<LoginResponse>> call,
                                          @NonNull Throwable t) {

                        dialogproses.dismiss();
                        Log.e("LOGIN_ERROR", "Gagal koneksi ke server", t);

                        String errorTitle = "Koneksi Gagal";
                        String errorMsg = t.getMessage() != null ? t.getMessage() : t.toString();

                        if (!NetworkUtils.isConnected(LoginActivity.this)) {
                            errorTitle = "Jaringan Mati";
                            errorMsg = "Mohon aktifkan data seluler atau Wi-Fi Anda.";
                        } else if (t instanceof SocketTimeoutException) {
                            errorTitle = "Jaringan Lambat";
                            errorMsg = "Waktu koneksi habis. Pastikan sinyal Anda kuat dan coba lagi.";
                        } else if (t instanceof ConnectException) {
                            errorTitle = "Server Gagal Terhubung";
                            errorMsg = "Tidak dapat menjangkau server. Periksa pengaturan VPN/Proxy Anda atau server sedang maintenance.";
                        } else if (t instanceof SSLHandshakeException || t.toString().contains("SSL") || t.toString().contains("Certificate")) {
                            errorTitle = "Masalah Sertifikat SSL";
                            String detailCause = t.getCause() != null ? t.getCause().getMessage() : t.getLocalizedMessage();
                            errorMsg = "Kegagalan sertifikat SSL: " + (detailCause != null ? detailCause : t.toString());
                            if (!session.isSslBypassEnabled()) {
                                errorMsg += "\n\nSilakan aktifkan sakelar 'Bypass Verifikasi SSL Server' di halaman login untuk melanjutkan.";
                            }
                            Log.e("SSL_ERROR_DETAIL", "Detail SSL: " + detailCause, t);
                        } else if (t instanceof IllegalStateException
                                || t instanceof com.google.gson.JsonParseException
                                || t.toString().contains("BEGIN_ARRAY")
                                || t.toString().contains("bukan JSON")
                                || t.toString().contains("JsonReader")
                                || t.toString().contains("JsonSyntaxException")) {
                            errorTitle = "Respon Server Tidak Sesuai";
                            errorMsg = "Format respon dari server tidak sesuai.\nMohon hubungi administrator atau coba beberapa saat lagi.";
                        } else if (t.toString().contains("Proxy") || t.toString().contains("proxy")) {
                            errorTitle = "Masalah Proxy";
                            errorMsg = "Koneksi ditolak oleh Proxy. Mohon matikan Proxy di pengaturan jaringan Anda.";
                        } else if (t.toString().contains("vpn") || t.toString().contains("VPN")) {
                            errorTitle = "Masalah VPN";
                            errorMsg = "Aplikasi mendeteksi gangguan dari VPN. Mohon matikan VPN Anda.";
                        }

                        dialogView.viewNotifKosong(
                                LoginActivity.this,
                                errorTitle,
                                errorMsg
                        );
                    }
                });
    }

    private void fetchFcmTokenAndSend() {
        FcmTokenHelper.fetchAndSendToken(this, success -> openNextPage());
    }

    private void openNextPage() {
        dialogproses.dismiss();
        Intent intent = new Intent(LoginActivity.this, DownloadDataActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showPrivacyPolicy() {
        Intent intent = new Intent(LoginActivity.this, PrivacyPolicyActivity.class);
        startActivity(intent);
    }

}