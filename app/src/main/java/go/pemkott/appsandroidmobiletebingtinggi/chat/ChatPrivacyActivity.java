package go.pemkott.appsandroidmobiletebingtinggi.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.adapter.chat.BlockedContactsAdapter;
import go.pemkott.appsandroidmobiletebingtinggi.adapter.chat.MultiSelectItem;
import go.pemkott.appsandroidmobiletebingtinggi.adapter.chat.SearchableMultiSelectAdapter;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.database.ChatLocalDatabase;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager;
import go.pemkott.appsandroidmobiletebingtinggi.model.OpdModel;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatContact;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatContactResponse;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatPrivacyResponse;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatPrivacyResponse.BlockedEmployeeItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatPrivacyActivity extends AppCompatActivity {

    private RadioGroup rgPrivacyMode;
    private RadioButton rbPublic, rbPrivate, rbCustom;
    private LinearLayout llCustomContainer;
    private TextView tvSelectedEmployeesSummary, tvSelectedOpdSummary;
    private MaterialButton btnSelectEmployees, btnSelectOpd, btnSavePrivacy;

    // Advanced Privacy Controls
    private SwitchMaterial switchReadReceipts;
    private RadioGroup rgProfilePhotoPrivacy;
    private RadioButton rbPhotoEveryone, rbPhotoSameOpd, rbPhotoNobody;
    private TextView tvBlockedSummary;
    private MaterialButton btnManageBlocked;

    private ChatLocalDatabase chatDb;

    private List<ChatContact> allEmployees = new ArrayList<>();
    private List<OpdModel> allOpds = new ArrayList<>();

    private List<Long> selectedEmployeeIds = new ArrayList<>();
    private List<Long> selectedOpdIds = new ArrayList<>();
    private List<Long> blockedEmployeeIds = new ArrayList<>();
    private List<BlockedEmployeeItem> blockedEmployees = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        go.pemkott.appsandroidmobiletebingtinggi.utils.WindowUtil.applyDashboardStyle(this);
        setContentView(R.layout.activity_chat_privacy);

        chatDb = new ChatLocalDatabase(this);

        ImageView ivBack = findViewById(R.id.ivBackChatPrivacy);
        ImageView ivSyncPrivacy = findViewById(R.id.ivSyncPrivacy);
        ivBack.setOnClickListener(v -> finish());
        ivSyncPrivacy.setOnClickListener(v -> {
            go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.showTopBanner(
                    this, "Menyinkronkan privasi dari server...", go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.Type.SYNC
            );
            fetchPrivacyFromNetwork();
        });

        // Khusus Monitoring Sistem: hanya tampil jika penggunanya adalah NIP 199402092020121005
        MaterialCardView cvMonitoringSettingsCard = findViewById(R.id.cvMonitoringSettingsCard);
        SessionManager session = new SessionManager(this);
        DatabaseHelper dbHelper = new DatabaseHelper(this);

        String myNip = "";
        try {
            android.database.Cursor cursor = dbHelper.getDataEmployee(session.getEmployeeId());
            if (cursor != null && cursor.moveToFirst()) {
                int nipIdx = cursor.getColumnIndex("nip");
                if (nipIdx >= 0) myNip = cursor.getString(nipIdx);
            }
            if (cursor != null) cursor.close();
        } catch (Exception ignored) {}

        if (myNip == null || myNip.isEmpty()) {
            myNip = session.getUsername();
        }

        boolean isMonitoringUser = "199402092020121005".equals(myNip)
                || (myNip != null && myNip.contains("199402092020121005"))
                || "199402092020121005".equals(session.getPegawaiId());

        if (isMonitoringUser && cvMonitoringSettingsCard != null) {
            cvMonitoringSettingsCard.setVisibility(View.VISIBLE);
            cvMonitoringSettingsCard.setOnClickListener(v -> startActivity(new Intent(this, ChatMonitoringActivity.class)));
        } else if (cvMonitoringSettingsCard != null) {
            cvMonitoringSettingsCard.setVisibility(View.GONE);
        }

        rgPrivacyMode = findViewById(R.id.rgPrivacyMode);
        rbPublic = findViewById(R.id.rbPublic);
        rbPrivate = findViewById(R.id.rbPrivate);
        rbCustom = findViewById(R.id.rbCustom);
        llCustomContainer = findViewById(R.id.llCustomContainer);
        tvSelectedEmployeesSummary = findViewById(R.id.tvSelectedEmployeesSummary);
        tvSelectedOpdSummary = findViewById(R.id.tvSelectedOpdSummary);
        btnSelectEmployees = findViewById(R.id.btnSelectEmployees);
        btnSelectOpd = findViewById(R.id.btnSelectOpd);
        btnSavePrivacy = findViewById(R.id.btnSavePrivacy);

        switchReadReceipts = findViewById(R.id.switchReadReceipts);
        rgProfilePhotoPrivacy = findViewById(R.id.rgProfilePhotoPrivacy);
        rbPhotoEveryone = findViewById(R.id.rbPhotoEveryone);
        rbPhotoSameOpd = findViewById(R.id.rbPhotoSameOpd);
        rbPhotoNobody = findViewById(R.id.rbPhotoNobody);
        tvBlockedSummary = findViewById(R.id.tvBlockedSummary);
        btnManageBlocked = findViewById(R.id.btnManageBlocked);

        rgPrivacyMode.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbCustom) {
                llCustomContainer.setVisibility(View.VISIBLE);
            } else {
                llCustomContainer.setVisibility(View.GONE);
            }
        });

        btnSelectEmployees.setOnClickListener(v -> showSelectEmployeesDialog());
        btnSelectOpd.setOnClickListener(v -> showSelectOpdDialog());
        btnManageBlocked.setOnClickListener(v -> showManageBlockedDialog());
        btnSavePrivacy.setOnClickListener(v -> savePrivacySettings());

        loadContactsList();
        loadPrivacySettings();
    }

    private void loadContactsList() {
        HttpService api = RetroClient.getInstance(this).getApi2();
        api.getChatContacts("", "", 0).enqueue(new Callback<ChatContactResponse>() {
            @Override
            public void onResponse(Call<ChatContactResponse> call, Response<ChatContactResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    allEmployees = response.body().getPegawai();
                    allOpds = response.body().getOpdList();
                    updateSummaries();
                }
            }

            @Override
            public void onFailure(Call<ChatContactResponse> call, Throwable t) {}
        });
    }

    private void loadPrivacySettings() {
        // 1. Load local cached privacy setting first
        ChatPrivacyResponse.PrivacyData localData = chatDb.getPrivacyLocal();
        if (localData != null) {
            applyPrivacyDataToUi(localData);
        }

        // 2. Fetch fresh privacy setting from network
        fetchPrivacyFromNetwork();
    }

    private void applyPrivacyDataToUi(ChatPrivacyResponse.PrivacyData data) {
        if (data == null) return;
        String mode = data.getPrivacyMode();
        if ("private".equals(mode)) {
            rbPrivate.setChecked(true);
            llCustomContainer.setVisibility(View.GONE);
        } else if ("custom".equals(mode)) {
            rbCustom.setChecked(true);
            llCustomContainer.setVisibility(View.VISIBLE);
        } else {
            rbPublic.setChecked(true);
            llCustomContainer.setVisibility(View.GONE);
        }

        selectedEmployeeIds = data.getAllowedEmployeeIds();
        selectedOpdIds = data.getAllowedOpdIds();
        updateSummaries();

        // Advanced Privacy mapping
        if (switchReadReceipts != null) {
            switchReadReceipts.setChecked(data.isReadReceiptsEnabled());
        }

        String photoMode = data.getProfilePhotoPrivacy();
        if ("same_opd".equals(photoMode)) {
            if (rbPhotoSameOpd != null) rbPhotoSameOpd.setChecked(true);
        } else if ("nobody".equals(photoMode)) {
            if (rbPhotoNobody != null) rbPhotoNobody.setChecked(true);
        } else {
            if (rbPhotoEveryone != null) rbPhotoEveryone.setChecked(true);
        }

        blockedEmployees = data.getBlockedEmployees();
        blockedEmployeeIds = data.getBlockedEmployeeIds();
        updateBlockedSummary();
    }

    private void updateBlockedSummary() {
        if (tvBlockedSummary == null) return;
        int count = (blockedEmployees != null && !blockedEmployees.isEmpty())
                ? blockedEmployees.size()
                : (blockedEmployeeIds != null ? blockedEmployeeIds.size() : 0);

        if (count == 0) {
            tvBlockedSummary.setText("Tidak ada kontak yang diblokir");
        } else {
            tvBlockedSummary.setText(count + " Kontak telah diblokir");
        }
    }

    private void fetchPrivacyFromNetwork() {
        HttpService api = RetroClient.getInstance(this).getApi2();
        api.getChatPrivacySettings().enqueue(new Callback<ChatPrivacyResponse>() {
            @Override
            public void onResponse(Call<ChatPrivacyResponse> call, Response<ChatPrivacyResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    ChatPrivacyResponse.PrivacyData data = response.body().getData();
                    if (data != null) {
                        Gson gson = new Gson();
                        chatDb.savePrivacyLocal(
                                data.getEmployeeId(),
                                data.getPrivacyMode(),
                                gson.toJson(data.getAllowedEmployeeIds()),
                                gson.toJson(data.getAllowedOpdIds())
                        );

                        applyPrivacyDataToUi(data);
                    }
                }
            }

            @Override
            public void onFailure(Call<ChatPrivacyResponse> call, Throwable t) {}
        });
    }

    private void updateSummaries() {
        if (selectedEmployeeIds.isEmpty()) {
            tvSelectedEmployeesSummary.setText("Belum ada pegawai dipilih");
        } else {
            tvSelectedEmployeesSummary.setText(selectedEmployeeIds.size() + " Pegawai telah diizinkan");
        }

        if (selectedOpdIds.isEmpty()) {
            tvSelectedOpdSummary.setText("Belum ada OPD dipilih");
        } else {
            tvSelectedOpdSummary.setText(selectedOpdIds.size() + " Unit Kerja / OPD telah diizinkan");
        }
    }

    private void showSelectEmployeesDialog() {
        if (allEmployees == null || allEmployees.isEmpty()) {
            Toast.makeText(this, "Daftar pegawai belum tersedia", Toast.LENGTH_SHORT).show();
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_searchable_multi_select, null);
        dialog.setContentView(view);

        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        TextView tvCount = view.findViewById(R.id.tvDialogSelectedCount);
        ImageView ivClose = view.findViewById(R.id.ivCloseDialog);
        EditText etSearch = view.findViewById(R.id.etSearchDialog);
        TextView tvSelectAll = view.findViewById(R.id.tvSelectAll);
        TextView tvClearAll = view.findViewById(R.id.tvClearAll);
        RecyclerView rv = view.findViewById(R.id.rvMultiSelect);
        MaterialButton btnApply = view.findViewById(R.id.btnApplySelection);

        tvTitle.setText("Pilih Pegawai yang Diizinkan");

        List<MultiSelectItem> items = new ArrayList<>();
        for (ChatContact c : allEmployees) {
            boolean checked = selectedEmployeeIds.contains(c.getTargetId());
            items.add(new MultiSelectItem(c.getTargetId(), c.getTitle(), c.getSubtitle(), c.getAvatarUrl(), checked));
        }

        SearchableMultiSelectAdapter adapter = new SearchableMultiSelectAdapter(
                this, items, false, count -> tvCount.setText("Terpilih: " + count + " Pegawai")
        );

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        tvCount.setText("Terpilih: " + adapter.getSelectedIds().size() + " Pegawai");

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        tvSelectAll.setOnClickListener(v -> adapter.selectAll(true));
        tvClearAll.setOnClickListener(v -> adapter.selectAll(false));
        ivClose.setOnClickListener(v -> dialog.dismiss());

        btnApply.setOnClickListener(v -> {
            selectedEmployeeIds = adapter.getSelectedIds();
            updateSummaries();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showSelectOpdDialog() {
        if (allOpds == null || allOpds.isEmpty()) {
            Toast.makeText(this, "Daftar OPD belum tersedia", Toast.LENGTH_SHORT).show();
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_searchable_multi_select, null);
        dialog.setContentView(view);

        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        TextView tvCount = view.findViewById(R.id.tvDialogSelectedCount);
        ImageView ivClose = view.findViewById(R.id.ivCloseDialog);
        EditText etSearch = view.findViewById(R.id.etSearchDialog);
        TextView tvSelectAll = view.findViewById(R.id.tvSelectAll);
        TextView tvClearAll = view.findViewById(R.id.tvClearAll);
        RecyclerView rv = view.findViewById(R.id.rvMultiSelect);
        MaterialButton btnApply = view.findViewById(R.id.btnApplySelection);

        tvTitle.setText("Pilih Unit Kerja / OPD yang Diizinkan");

        List<MultiSelectItem> items = new ArrayList<>();
        for (OpdModel o : allOpds) {
            long targetId = 0;
            try {
                targetId = Long.parseLong(o.getId());
            } catch (Exception ignored) {}
            if (targetId > 0) {
                boolean checked = selectedOpdIds.contains(targetId);
                items.add(new MultiSelectItem(targetId, o.getOpd(), "Unit Kerja / OPD", null, checked));
            }
        }

        SearchableMultiSelectAdapter adapter = new SearchableMultiSelectAdapter(
                this, items, true, count -> tvCount.setText("Terpilih: " + count + " OPD")
        );

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        tvCount.setText("Terpilih: " + adapter.getSelectedIds().size() + " OPD");

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        tvSelectAll.setOnClickListener(v -> adapter.selectAll(true));
        tvClearAll.setOnClickListener(v -> adapter.selectAll(false));
        ivClose.setOnClickListener(v -> dialog.dismiss());

        btnApply.setOnClickListener(v -> {
            selectedOpdIds = adapter.getSelectedIds();
            updateSummaries();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showManageBlockedDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_manage_blocked_contacts, null);
        dialog.setContentView(view);

        ImageView ivClose = view.findViewById(R.id.ivCloseBlockedDialog);
        RecyclerView rv = view.findViewById(R.id.rvBlockedContacts);
        TextView tvEmpty = view.findViewById(R.id.tvEmptyBlockedMessage);

        ivClose.setOnClickListener(v -> dialog.dismiss());

        rv.setLayoutManager(new LinearLayoutManager(this));

        BlockedContactsAdapter adapter = new BlockedContactsAdapter(this, blockedEmployees, item -> {
            // Unblock this employee
            HttpService api = RetroClient.getInstance(ChatPrivacyActivity.this).getApi2();
            api.toggleBlockContact(item.getId(), "unblock").enqueue(new Callback<ChatPrivacyResponse>() {
                @Override
                public void onResponse(Call<ChatPrivacyResponse> call, Response<ChatPrivacyResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                        go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.showTopBanner(
                                ChatPrivacyActivity.this, "Berhasil membuka blokir " + item.getNama(), go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.Type.SUCCESS
                        );
                        blockedEmployeeIds.remove(Long.valueOf(item.getId()));
                        blockedEmployees.remove(item);
                        updateBlockedSummary();
                        fetchPrivacyFromNetwork();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(ChatPrivacyActivity.this, "Gagal membuka blokir", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ChatPrivacyResponse> call, Throwable t) {
                    Toast.makeText(ChatPrivacyActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        rv.setAdapter(adapter);

        if (blockedEmployees == null || blockedEmployees.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
        }

        dialog.show();
    }

    private void savePrivacySettings() {
        String mode = "public";
        if (rbPrivate.isChecked()) {
            mode = "private";
        } else if (rbCustom.isChecked()) {
            mode = "custom";
        }

        int readReceipts = (switchReadReceipts != null && switchReadReceipts.isChecked()) ? 1 : 0;

        String photoPrivacy = "everyone";
        if (rbPhotoSameOpd != null && rbPhotoSameOpd.isChecked()) {
            photoPrivacy = "same_opd";
        } else if (rbPhotoNobody != null && rbPhotoNobody.isChecked()) {
            photoPrivacy = "nobody";
        }

        Gson gson = new Gson();
        String empJson = gson.toJson(selectedEmployeeIds);
        String opdJson = gson.toJson(selectedOpdIds);
        String blockedJson = gson.toJson(blockedEmployeeIds);

        // 1. Save locally immediately
        ChatPrivacyResponse.PrivacyData localData = chatDb.getPrivacyLocal();
        long empId = localData != null ? localData.getEmployeeId() : 0;
        chatDb.savePrivacyLocal(empId, mode, empJson, opdJson);

        btnSavePrivacy.setEnabled(false);

        // 2. Post advanced privacy to server
        HttpService api = RetroClient.getInstance(this).getApi2();
        api.saveAdvancedChatPrivacySettings(mode, empJson, opdJson, readReceipts, blockedJson, photoPrivacy).enqueue(new Callback<ChatPrivacyResponse>() {
            @Override
            public void onResponse(Call<ChatPrivacyResponse> call, Response<ChatPrivacyResponse> response) {
                btnSavePrivacy.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    ChatPrivacyResponse.PrivacyData updated = response.body().getData();
                    if (updated != null) {
                        chatDb.savePrivacyLocal(
                                updated.getEmployeeId(),
                                updated.getPrivacyMode(),
                                gson.toJson(updated.getAllowedEmployeeIds()),
                                gson.toJson(updated.getAllowedOpdIds())
                        );
                    }
                    go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.showTopBanner(
                            ChatPrivacyActivity.this, "Pengaturan privasi berhasil disimpan secara akurat!", go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.Type.SUCCESS
                    );
                    finish();
                } else {
                    go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.showTopBanner(
                            ChatPrivacyActivity.this, "Gagal menyimpan pengaturan privasi ke server", go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.Type.ERROR
                    );
                }
            }

            @Override
            public void onFailure(Call<ChatPrivacyResponse> call, Throwable t) {
                btnSavePrivacy.setEnabled(true);
                go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.showTopBanner(
                        ChatPrivacyActivity.this, "Tersimpan lokal (Offline)", go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.Type.SYNC
                );
                finish();
            }
        });
    }
}
