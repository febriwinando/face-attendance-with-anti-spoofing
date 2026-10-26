package go.pemkott.appsandroidmobiletebingtinggi;

import android.app.Dialog;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.api.ServerConfig;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.dialogview.DialogView;
import go.pemkott.appsandroidmobiletebingtinggi.model.AdminOpdListResponse;
import go.pemkott.appsandroidmobiletebingtinggi.model.EmployeeResponse;
import go.pemkott.appsandroidmobiletebingtinggi.model.EmployeesData;
import go.pemkott.appsandroidmobiletebingtinggi.model.OpdModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeSyncActivity extends AppCompatActivity {

    private Spinner spOpd;
    private EditText etSearch;
    private TextView tvEmployeeCount, tvSyncInfo;
    private RecyclerView rvEmployeeList;
    private View llEmptyState;

    private DatabaseHelper db;
    private DialogView dialogView;

    private List<OpdModel> listOpd = new ArrayList<>();
    private List<EmployeesData> masterEmployees = new ArrayList<>();
    private final List<EmployeesData> displayEmployees = new ArrayList<>();
    private EmployeeAdapter adapter;

    private String selectedOpdId = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_sync);

        db = new DatabaseHelper(this);
        dialogView = new DialogView(this);

        ImageView ivBack = findViewById(R.id.ivSyncBack);
        ImageView ivServerSetting = findViewById(R.id.ivServerSetting);
        MaterialButton btnSync = findViewById(R.id.btnSyncEmployee);
        tvSyncInfo = findViewById(R.id.tvSyncInfo);
        spOpd = findViewById(R.id.spSyncOpd);
        etSearch = findViewById(R.id.etSearchSyncEmployee);
        tvEmployeeCount = findViewById(R.id.tvSyncEmployeeCount);
        rvEmployeeList = findViewById(R.id.rvSyncEmployeeList);
        llEmptyState = findViewById(R.id.llSyncEmptyState);

        ivBack.setOnClickListener(v -> finish());
        if (ivServerSetting != null) {
            ivServerSetting.setOnClickListener(v -> showServerConfigDialog());
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        // Setup RecyclerView
        rvEmployeeList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EmployeeAdapter(displayEmployees);
        rvEmployeeList.setAdapter(adapter);

        // Search Filter Watcher
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEmployeeList();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnSync.setOnClickListener(v -> syncEmployeeFromServer());

        // Load OPDs and Employee List
        loadOpdList();
        fetchEmployeesFromApi();
    }

    private void loadOpdList() {
        RetroClient.getInstance().getApi2().getAdminOpdList().enqueue(new Callback<AdminOpdListResponse>() {
            @Override
            public void onResponse(Call<AdminOpdListResponse> call, Response<AdminOpdListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    listOpd = new ArrayList<>();
                    OpdModel allOpd = new OpdModel();
                    allOpd.setId("ALL");
                    allOpd.setOpd("Semua Unit Kerja / OPD");
                    listOpd.add(allOpd);

                    if (response.body().getData() != null) {
                        listOpd.addAll(response.body().getData());
                    }

                    ArrayAdapter<OpdModel> adapterOpd = new ArrayAdapter<>(EmployeeSyncActivity.this, android.R.layout.simple_spinner_dropdown_item, listOpd);
                    spOpd.setAdapter(adapterOpd);

                    spOpd.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (listOpd != null && position >= 0 && position < listOpd.size()) {
                                OpdModel selected = listOpd.get(position);
                                selectedOpdId = selected != null && selected.getId() != null ? selected.getId() : "ALL";
                                filterEmployeeList();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                }
            }

            @Override
            public void onFailure(Call<AdminOpdListResponse> call, Throwable t) {
                Log.e("EMPLOYEE_SYNC", "Error loadOpdList", t);
            }
        });
    }

    private void fetchEmployeesFromApi() {
        Dialog dialogLoading = new Dialog(this, R.style.DialogStyle);
        dialogLoading.setContentView(R.layout.view_proses);
        dialogLoading.setCancelable(false);
        dialogLoading.show();

        RetroClient.getInstance().getApi2().getEmployees().enqueue(new Callback<EmployeeResponse>() {
            @Override
            public void onResponse(Call<EmployeeResponse> call, Response<EmployeeResponse> response) {
                dialogLoading.dismiss();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    masterEmployees = response.body().getData();
                    if (masterEmployees == null) masterEmployees = new ArrayList<>();
                    tvSyncInfo.setText("Status: Total " + masterEmployees.size() + " data pegawai dimuat dari server.");
                    filterEmployeeList();
                } else {
                    dialogView.viewNotifKosong(EmployeeSyncActivity.this, "Gagal Memuat", "Server mengembalikan HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<EmployeeResponse> call, Throwable t) {
                dialogLoading.dismiss();
                Log.e("EMPLOYEE_SYNC", "Error fetchEmployeesFromApi", t);
                handleConnectionError(t.getMessage());
            }
        });
    }

    private void filterEmployeeList() {
        String query = etSearch.getText().toString().trim().toLowerCase(Locale.ROOT);
        displayEmployees.clear();

        for (EmployeesData emp : masterEmployees) {
            if (emp == null) continue;

            // OPD Filter
            boolean matchesOpd = "ALL".equalsIgnoreCase(selectedOpdId)
                    || String.valueOf(emp.getOpdId()).equalsIgnoreCase(selectedOpdId);

            // Text Search Filter
            String nama = emp.getNama() != null ? emp.getNama().toLowerCase(Locale.ROOT) : "";
            String nip = emp.getNip() != null ? emp.getNip().toLowerCase(Locale.ROOT) : "";
            boolean matchesSearch = query.isEmpty() || nama.contains(query) || nip.contains(query);

            if (matchesOpd && matchesSearch) {
                displayEmployees.add(emp);
            }
        }

        tvEmployeeCount.setText("Menampilkan " + displayEmployees.size() + " Pegawai");
        if (llEmptyState != null) {
            llEmptyState.setVisibility(displayEmployees.isEmpty() ? View.VISIBLE : View.GONE);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void syncEmployeeFromServer() {
        Dialog dialogProses = new Dialog(this, R.style.DialogStyle);
        dialogProses.setContentView(R.layout.view_proses);
        dialogProses.setCancelable(false);
        dialogProses.show();

        RetroClient.getInstance().getApi2().getEmployees().enqueue(new Callback<EmployeeResponse>() {
            @Override
            public void onResponse(Call<EmployeeResponse> call, Response<EmployeeResponse> response) {
                dialogProses.dismiss();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    db.deleteAllEmployees();
                    SQLiteDatabase database = db.getWritableDatabase();
                    database.beginTransaction();

                    try {
                        for (EmployeesData e : response.body().getData()) {
                            db.insertEmployee(e);
                        }
                        database.setTransactionSuccessful();
                    } finally {
                        database.endTransaction();
                    }

                    masterEmployees = response.body().getData();
                    if (masterEmployees == null) masterEmployees = new ArrayList<>();
                    filterEmployeeList();

                    dialogView.viewSuksesNonFinish(EmployeeSyncActivity.this, "Sinkronisasi berhasil!\nTotal " + response.body().getTotal() + " data pegawai diperbarui.");
                    tvSyncInfo.setText("Status: Terakhir disinkronkan (" + response.body().getTotal() + " pegawai)");
                } else {
                    dialogView.viewNotifKosong(EmployeeSyncActivity.this, "Sinkronisasi Gagal", "Server mengembalikan HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<EmployeeResponse> call, Throwable t) {
                dialogProses.dismiss();
                handleConnectionError(t.getMessage());
            }
        });
    }

    private void handleConnectionError(String msg) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
        builder.setTitle("❌ Gagal Terkoneksi ke Server");
        builder.setMessage("Tidak dapat terhubung ke server:\n" + ServerConfig.getBaseUrl() + "\n\nDetail Error: " + (msg != null ? msg : "Timeout/Unreachable") + "\n\nApakah Anda ingin mengubah Alamat / IP Server?");
        builder.setPositiveButton("Ubah Alamat Server", (dialog, which) -> showServerConfigDialog());
        builder.setNegativeButton("Tutup", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showServerConfigDialog() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
        builder.setTitle("⚡ Pengaturan Alamat Server");

        go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager session = new go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager(this);
        String currentUrl = ServerConfig.getBaseUrl();

        final EditText input = new EditText(this);
        input.setHint("http://192.168.1.x:8000/ atau domain server");
        input.setText(currentUrl);
        input.setPadding(40, 30, 40, 30);
        builder.setView(input);

        builder.setPositiveButton("Simpan & Coba Lagi", (dialog, which) -> {
            String newHost = input.getText().toString().trim();
            if (!newHost.isEmpty()) {
                session.saveServerHost(newHost);
                RetroClient.resetInstance();
                tvSyncInfo.setText("Server diubah ke: " + ServerConfig.getBaseUrl());
                loadOpdList();
                fetchEmployeesFromApi();
            }
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showEmployeeDetailDialog(EmployeesData emp) {
        if (emp == null) return;

        Dialog dialogDetail = new Dialog(this, R.style.DialogStyle);
        dialogDetail.setContentView(R.layout.dialog_employee_detail);

        ImageView ivFoto = dialogDetail.findViewById(R.id.ivDetailFoto);
        ImageView ivClose = dialogDetail.findViewById(R.id.ivCloseDetail);

        TextView tvEmpId = dialogDetail.findViewById(R.id.tvDetailEmpId);
        TextView tvNama = dialogDetail.findViewById(R.id.tvDetailNama);
        TextView tvNip = dialogDetail.findViewById(R.id.tvDetailNip);
        TextView tvOpd = dialogDetail.findViewById(R.id.tvDetailOpd);
        TextView tvKelompok = dialogDetail.findViewById(R.id.tvDetailKelompok);
        TextView tvEmail = dialogDetail.findViewById(R.id.tvDetailEmail);
        TextView tvNoHp = dialogDetail.findViewById(R.id.tvDetailNoHp);
        TextView tvShift = dialogDetail.findViewById(R.id.tvDetailShift);
        TextView tvStatusAkun = dialogDetail.findViewById(R.id.tvDetailStatusAkun);
        TextView tvAtasan = dialogDetail.findViewById(R.id.tvDetailAtasan);
        TextView tvAlamatOpd = dialogDetail.findViewById(R.id.tvDetailAlamatOpd);

        String fotoUrl = (emp.getFoto() != null && !emp.getFoto().trim().isEmpty())
                ? (ServerConfig.getBaseUrl() + "storage/" + emp.getFoto())
                : null;

        if (tvEmpId != null) tvEmpId.setText("#" + emp.getId());
        if (tvNama != null) tvNama.setText(emp.getNama() != null ? emp.getNama() : "-");
        if (tvNip != null) tvNip.setText(emp.getNip() != null ? "NIP: " + emp.getNip() : "NIP: -");
        if (tvOpd != null) tvOpd.setText((emp.getOpd() != null ? emp.getOpd() : "Unit Kerja ID") + " (ID #" + emp.getOpdId() + ")");
        if (tvKelompok != null) tvKelompok.setText((emp.getKelompok() != null ? emp.getKelompok().toUpperCase(Locale.ROOT) : "PNS") + " • Eselon " + emp.getEselon());
        if (tvEmail != null) tvEmail.setText(emp.getEmail() != null ? emp.getEmail() : "-");
        if (tvNoHp != null) tvNoHp.setText(emp.getNoHp() != null ? emp.getNoHp() : "-");
        if (tvShift != null) tvShift.setText(emp.getShift() == 1 ? "Shift (Jam Shift Khusus)" : "Non-Shift (Jam Kantor Normal)");
        if (tvStatusAkun != null) tvStatusAkun.setText(emp.getActive() == 1 ? "Aktif (1)" : "Non-Aktif (0)");
        if (tvAtasan != null) tvAtasan.setText("Atasan 1: " + (emp.getAtasanId1() != null ? "#" + emp.getAtasanId1() : "-") + " • Atasan 2: " + (emp.getAtasanId2() != null ? "#" + emp.getAtasanId2() : "-"));
        if (tvAlamatOpd != null) tvAlamatOpd.setText(emp.getAlamat() != null ? emp.getAlamat() : "-");

        if (fotoUrl != null) {
            Glide.with(this)
                    .load(fotoUrl)
                    .placeholder(R.drawable.profil_pic)
                    .error(R.drawable.profil_pic)
                    .into(ivFoto);

            ivFoto.setOnClickListener(v -> showFullPhotoDialog(fotoUrl));
        } else {
            ivFoto.setImageResource(R.drawable.profil_pic);
        }

        if (ivClose != null) {
            ivClose.setOnClickListener(v -> dialogDetail.dismiss());
        }

        dialogDetail.show();
    }

    private void showFullPhotoDialog(String fotoUrl) {
        if (fotoUrl == null || fotoUrl.trim().isEmpty()) return;

        Dialog dialogFoto = new Dialog(this, R.style.DialogStyle);
        dialogFoto.setContentView(R.layout.item_view_photo);
        dialogFoto.setCancelable(true);

        ImageView ivFotoFull = dialogFoto.findViewById(R.id.ivFoto);
        TextView tvCloseFull = dialogFoto.findViewById(R.id.tvCloseViewFoto);

        if (ivFotoFull != null) {
            Glide.with(this)
                    .load(fotoUrl)
                    .placeholder(R.drawable.profil_pic)
                    .error(R.drawable.profil_pic)
                    .into(ivFotoFull);
        }

        if (tvCloseFull != null) {
            tvCloseFull.setOnClickListener(v -> dialogFoto.dismiss());
        }

        dialogFoto.show();
    }

    // RecyclerView Adapter
    private class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder> {

        private final List<EmployeesData> list;

        public EmployeeAdapter(List<EmployeesData> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_employee_sync, parent, false);
            return new EmployeeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {
            EmployeesData item = list.get(position);
            if (item == null) return;

            holder.tvNama.setText(item.getNama() != null ? item.getNama() : "-");
            holder.tvNip.setText(item.getNip() != null ? "NIP: " + item.getNip() : "NIP: -");
            holder.tvOpd.setText(item.getOpd() != null ? item.getOpd() : "OPD ID #" + item.getOpdId());
            holder.tvKelompok.setText(item.getKelompok() != null ? item.getKelompok().toUpperCase(Locale.ROOT) : "PNS");

            if (item.getFoto() != null && !item.getFoto().trim().isEmpty()) {
                String fotoUrl = ServerConfig.getBaseUrl() + "storage/" + item.getFoto();
                Glide.with(EmployeeSyncActivity.this)
                        .load(fotoUrl)
                        .placeholder(R.drawable.profil_pic)
                        .error(R.drawable.profil_pic)
                        .into(holder.ivFoto);
            } else {
                holder.ivFoto.setImageResource(R.drawable.profil_pic);
            }

            holder.itemView.setOnClickListener(v -> showEmployeeDetailDialog(item));
        }

        @Override
        public int getItemCount() {
            return list != null ? list.size() : 0;
        }

        class EmployeeViewHolder extends RecyclerView.ViewHolder {
            ImageView ivFoto;
            TextView tvNama, tvNip, tvOpd, tvKelompok;

            public EmployeeViewHolder(@NonNull View itemView) {
                super(itemView);
                ivFoto = itemView.findViewById(R.id.ivItemEmpFoto);
                tvNama = itemView.findViewById(R.id.tvItemEmpNama);
                tvNip = itemView.findViewById(R.id.tvItemEmpNip);
                tvOpd = itemView.findViewById(R.id.tvItemEmpOpd);
                tvKelompok = itemView.findViewById(R.id.tvItemEmpKelompok);
            }
        }
    }
}
