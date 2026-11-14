package go.pemkott.appsandroidmobiletebingtinggi.verifikasi;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.model.ValidasiModel;
import go.pemkott.appsandroidmobiletebingtinggi.utils.NetworkUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class VerificationBatchHelper {

    public interface BatchCallback {
        void onSuccess(String message);

        void onError(String message, boolean fromServerSuccessFalse);
    }

    private VerificationBatchHelper() {
    }

    public static void bindSectionToolbar(
            Activity activity,
            View batchBar,
            CheckBox cbSelectAll,
            MaterialButton btnApprove,
            MaterialButton btnReject,
            BaseVerifikasiAdapter<?> adapter,
            HttpService holderAPI,
            String posisi,
            String tahap,
            BatchCallback callback
    ) {
        if (batchBar == null || adapter == null) {
            return;
        }

        if (adapter.getItemCount() == 0) {
            batchBar.setVisibility(View.GONE);
            return;
        }
        batchBar.setVisibility(View.VISIBLE);

        Runnable syncUi = () -> {
            int selected = adapter.getSelectedCount();
            int total = adapter.getItemCount();
            boolean hasSelection = selected > 0;

            if (cbSelectAll != null) {
                cbSelectAll.setOnCheckedChangeListener(null);
                cbSelectAll.setChecked(adapter.isAllSelected());
                cbSelectAll.setText("Pilih Semua (" + selected + "/" + total + ")");
                cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> adapter.setAllSelected(isChecked));
            }
            if (btnApprove != null) {
                btnApprove.setText(hasSelection ? "Terima (" + selected + ")" : "Terima");
                btnApprove.setAlpha(hasSelection ? 1.0f : 0.5f);
            }
            if (btnReject != null) {
                btnReject.setText(hasSelection ? "Tolak (" + selected + ")" : "Tolak");
                btnReject.setAlpha(hasSelection ? 1.0f : 0.5f);
            }
        };

        adapter.setOnSelectionChangeListener((selectedCount, totalCount) -> syncUi.run());
        syncUi.run();

        String approveStatus = "1".equals(tahap) ? "1" : "2";
        String rejectStatus = "1".equals(tahap) ? "3" : "4";

        if (btnApprove != null) {
            btnApprove.setOnClickListener(v -> triggerBatchConfirm(
                    activity, adapter, holderAPI, posisi, tahap, approveStatus, callback
            ));
        }
        if (btnReject != null) {
            btnReject.setOnClickListener(v -> triggerBatchConfirm(
                    activity, adapter, holderAPI, posisi, tahap, rejectStatus, callback
            ));
        }
    }

    private static void triggerBatchConfirm(
            Activity activity,
            BaseVerifikasiAdapter<?> adapter,
            HttpService holderAPI,
            String posisi,
            String tahap,
            String targetStatus,
            BatchCallback callback
    ) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        List<String> selectedIds = adapter.getSelectedIds();
        if (selectedIds.isEmpty()) {
            Toast.makeText(activity, "Centang minimal 1 data atau klik Pilih Semua terlebih dahulu.", Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog confirmDialog = new Dialog(activity, R.style.DialogStyle);
        confirmDialog.setContentView(R.layout.view_kirm_validasi);
        confirmDialog.setCancelable(true);

        TextView tvWarning1 = confirmDialog.findViewById(R.id.tvWarning1);
        ImageView tvTutupDialog = confirmDialog.findViewById(R.id.tvTutupDialog);
        TextView title = confirmDialog.findViewById(R.id.tvTitleNotif);
        EditText cttValidator = confirmDialog.findViewById(R.id.catatanvalidator);
        Button btnkirimvalidasi = confirmDialog.findViewById(R.id.btnkirimvalidasi);

        if (title != null) {
            title.setText("Konfirmasi Validasi Sekaligus");
        }
        boolean isReject = "3".equals(targetStatus) || "4".equals(targetStatus);
        if (tvWarning1 != null) {
            tvWarning1.setText(isReject
                    ? "Apakah anda yakin akan menolak " + selectedIds.size() + " data terpilih sekaligus?"
                    : "Apakah anda yakin akan menyetujui " + selectedIds.size() + " data terpilih sekaligus?");
        }
        if (tvTutupDialog != null) {
            tvTutupDialog.setOnClickListener(v -> confirmDialog.dismiss());
        }
        if (btnkirimvalidasi != null) {
            btnkirimvalidasi.setOnClickListener(v -> {
                String catatan = cttValidator != null ? cttValidator.getText().toString() : "";
                confirmDialog.dismiss();
                executeBatchCall(activity, holderAPI, selectedIds, posisi, tahap, targetStatus, catatan, callback);
            });
        }
        confirmDialog.show();
    }

    private static void executeBatchCall(
            Activity activity,
            HttpService holderAPI,
            List<String> ids,
            String posisi,
            String tahap,
            String targetStatus,
            String ctt,
            BatchCallback callback
    ) {
        Dialog progressDialog = new Dialog(activity, R.style.DialogStyle);
        progressDialog.setContentView(R.layout.view_proses);
        progressDialog.setCancelable(false);
        progressDialog.show();

        String ipPerangkat = NetworkUtil.getDeviceIpFallback();

        holderAPI.validasiBatch(ids, posisi, tahap, targetStatus, ctt, ipPerangkat)
                .enqueue(new Callback<ValidasiModel>() {
                    @Override
                    public void onResponse(@NonNull Call<ValidasiModel> call,
                                           @NonNull Response<ValidasiModel> response) {
                        if (!activity.isFinishing() && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        if (!response.isSuccessful()) {
                            callback.onError(extractErrorMessage(response, "Silakan coba kembali."), false);
                            return;
                        }
                        ValidasiModel body = response.body();
                        if (body == null) {
                            callback.onError("Response server kosong.", false);
                            return;
                        }
                        if (body.isStatus()) {
                            callback.onSuccess(body.getMessage());
                        } else {
                            callback.onError(body.getMessage(), true);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ValidasiModel> call, @NonNull Throwable t) {
                        if (!activity.isFinishing() && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        callback.onError("Gagal terhubung ke server. Silakan coba kembali.", false);
                    }
                });
    }

    private static String extractErrorMessage(Response<?> response, String fallback) {
        try {
            if (response.errorBody() != null) {
                String error = response.errorBody().string();
                JSONObject json = new JSONObject(error);
                if (json.has("message")) {
                    return json.getString("message");
                }
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }
}

