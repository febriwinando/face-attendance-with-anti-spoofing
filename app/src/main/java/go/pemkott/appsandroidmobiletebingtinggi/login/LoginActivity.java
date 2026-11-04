package go.pemkott.appsandroidmobiletebingtinggi.login;


import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

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

                        if (!response.isSuccessful()) {
                            dialogproses.dismiss();
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

                        if (data.getStatus() == 202 || data.isRequire2fa()) {
                            dialogproses.dismiss();
                            show2faOtpDialog(data, username, password);
                            return;
                        }

                        if (data.getStatus() == 401 || data.getStatus() == 422) {
                            dialogproses.dismiss();
                            dialogView.viewNotifKosong(
                                    LoginActivity.this,
                                    "Login gagal",
                                    data.getMessage() != null ? data.getMessage() : "NIP/NIK atau Password/Kode 2FA tidak sesuai.");
                            return;
                        }

                        processLoginSuccessData(data, username);
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

    private void processLoginSuccessData(LoginResponse data, String username) {
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

        // Hapus sisa cache chat dari akun sebelumnya agar data bersih & privat
        try {
            new go.pemkott.appsandroidmobiletebingtinggi.database.ChatLocalDatabase(LoginActivity.this).clearAllLocalChatData();
        } catch (Exception ignored) {}

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

            fetchFcmTokenAndSend(sEmployee_id, username);
        } else {
            dialogproses.dismiss();
            dialogView.viewNotifKosong(
                    LoginActivity.this,
                    "Akun Non-Aktif",
                    "Status akun Anda belum diaktifkan oleh admin.");
        }
    }

    public static android.graphics.Bitmap generateQrCode(String text, int width, int height) {
        try {
            com.google.zxing.common.BitMatrix bitMatrix = new com.google.zxing.MultiFormatWriter()
                    .encode(text, com.google.zxing.BarcodeFormat.QR_CODE, width, height);
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
                }
            }
            android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (Exception e) {
            Log.e("QR_GEN", "Error generating local QR code: " + e.getMessage(), e);
            return null;
        }
    }

    private void show2faOtpDialog(LoginResponse data, String username, String password) {
        Dialog dialog2fa = new Dialog(LoginActivity.this, R.style.DialogStyle);
        dialog2fa.setContentView(R.layout.view_2fa_otp);
        dialog2fa.setCancelable(true);

        if (dialog2fa.getWindow() != null) {
            dialog2fa.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog2fa.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        TextView tvTitle = dialog2fa.findViewById(R.id.tv2faTitle);
        TextView tvDesc = dialog2fa.findViewById(R.id.tv2faDescription);
        View llSecretContainer = dialog2fa.findViewById(R.id.ll2faSecretContainer);
        TextView tvSecretKey = dialog2fa.findViewById(R.id.tv2faSecretKey);
        ImageView ivQrCode = dialog2fa.findViewById(R.id.iv2faQrCode);
        com.google.android.material.button.MaterialButton btnCopy = dialog2fa.findViewById(R.id.btn2faCopySecret);
        EditText etOtp = dialog2fa.findViewById(R.id.et2faOtp);
        com.google.android.material.button.MaterialButton btnVerify = dialog2fa.findViewById(R.id.btn2faVerify);

        if (data != null && data.getSecretKey() != null && !data.getSecretKey().trim().isEmpty()) {
            if (tvTitle != null) tvTitle.setText(data.isSetupRequired() ? "Setup 2FA Wajib" : "Verifikasi 2FA");
            if (tvDesc != null) tvDesc.setText("Pindai Barcode di bawah atau salin Kunci Rahasia ke Google Authenticator Anda lalu masukkan kode 6-digit:");
            if (llSecretContainer != null) llSecretContainer.setVisibility(View.VISIBLE);
            if (tvSecretKey != null) tvSecretKey.setText(data.getSecretKey());

            if (ivQrCode != null) {
                String totpUri = "otpauth://totp/E-ABSENSI%20TEBING%20TINGGI:" + username + "?secret=" + data.getSecretKey() + "&issuer=E-ABSENSI%20TEBING%20TINGGI";
                android.graphics.Bitmap localQrBitmap = generateQrCode(totpUri, 400, 400);

                if (localQrBitmap != null) {
                    ivQrCode.setImageBitmap(localQrBitmap);
                } else if (data.getQrCode() != null && !data.getQrCode().trim().isEmpty()) {
                    try {
                        byte[] decodedString = android.util.Base64.decode(data.getQrCode(), android.util.Base64.DEFAULT);
                        android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        if (decodedByte != null) {
                            ivQrCode.setImageBitmap(decodedByte);
                        } else {
                            String qrUrl = ServerConfig.getBaseUrl() + "api/2fa/qr-code?secret=" + data.getSecretKey() + "&username=" + username;
                            com.bumptech.glide.Glide.with(LoginActivity.this).load(qrUrl).into(ivQrCode);
                        }
                    } catch (Exception eQr) {
                        String qrUrl = ServerConfig.getBaseUrl() + "api/2fa/qr-code?secret=" + data.getSecretKey() + "&username=" + username;
                        com.bumptech.glide.Glide.with(LoginActivity.this).load(qrUrl).into(ivQrCode);
                    }
                } else {
                    String qrUrl = ServerConfig.getBaseUrl() + "api/2fa/qr-code?secret=" + data.getSecretKey() + "&username=" + username;
                    com.bumptech.glide.Glide.with(LoginActivity.this).load(qrUrl).into(ivQrCode);
                }
            }
            if (btnCopy != null) {
                btnCopy.setOnClickListener(v -> {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("2FA Secret Key", data.getSecretKey());
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(LoginActivity.this, "Kunci Rahasia 2FA Berhasil Disalin!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            if (btnVerify != null) btnVerify.setText("VERIFIKASI & MASUK");
        } else {
            if (tvTitle != null) tvTitle.setText("Verifikasi 2FA");
            if (tvDesc != null) tvDesc.setText("Masukkan 6 digit kode dari aplikasi Google Authenticator Anda:");
            if (llSecretContainer != null) llSecretContainer.setVisibility(View.GONE);
            if (btnVerify != null) btnVerify.setText("VERIFIKASI");
        }

        if (btnVerify != null) {
            btnVerify.setOnClickListener(v -> {
                String otpCode = etOtp != null ? etOtp.getText().toString().trim() : "";
                if (otpCode.length() != 6) {
                    dialogView.viewNotifKosong(LoginActivity.this, "Kode 2FA Tidak Valid", "Masukkan 6 digit angka dari aplikasi Google Authenticator.");
                    return;
                }
                dialog2fa.dismiss();
                loginApp2fa(username, password, otpCode);
            });
        }

        dialog2fa.show();
    }

    private void loginApp2fa(String username, String password, String otp) {
        dialogproses = new Dialog(LoginActivity.this, R.style.DialogStyle);
        dialogproses.setContentView(R.layout.view_proses);
        dialogproses.setCancelable(false);
        dialogproses.show();

        RetroClient
                .getInstance(LoginActivity.this)
                .getApi2()
                .login2fa(username, password, otp)
                .enqueue(new Callback<List<LoginResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<LoginResponse>> call, @NonNull Response<List<LoginResponse>> response) {
                        if (isFinishing()) return;
                        if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                            dialogproses.dismiss();
                            dialogView.viewNotifKosong(LoginActivity.this, "Verifikasi 2FA Gagal", "Respon server tidak valid.");
                            return;
                        }
                        LoginResponse data = response.body().get(0);
                        if (data.getStatus() == 422 || data.getStatus() == 401) {
                            dialogproses.dismiss();
                            dialogView.viewNotifKosong(LoginActivity.this, "Verifikasi 2FA Gagal", data.getMessage() != null ? data.getMessage() : "Kode OTP 2FA Salah atau Kadaluarsa.");
                            return;
                        }
                        processLoginSuccessData(data, username);
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<LoginResponse>> call, @NonNull Throwable t) {
                        dialogproses.dismiss();
                        dialogView.viewNotifKosong(LoginActivity.this, "Koneksi Gagal", t.getMessage());
                    }
                });
    }

    private boolean isNextPageOpened = false;

    private synchronized void openNextPage() {
        if (isNextPageOpened) return;
        isNextPageOpened = true;

        if (dialogproses != null && dialogproses.isShowing()) {
            try {
                dialogproses.dismiss();
            } catch (Exception ignored) {}
        }

        Intent intent = new Intent(LoginActivity.this, DownloadDataActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void fetchFcmTokenAndSend(String employeeId, String nip) {
        isNextPageOpened = false;

        // Safety fallback: Jika network lambat, maksimal 2.5 detik langsung buka halaman berikutnya
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            openNextPage();
        }, 2500);

        FcmTokenHelper.fetchAndSendTokenForUser(getApplicationContext(), employeeId, nip, success -> {
            runOnUiThread(this::openNextPage);
        });
    }

    private void showPrivacyPolicy() {
        Intent intent = new Intent(LoginActivity.this, PrivacyPolicyActivity.class);
        startActivity(intent);
    }

}