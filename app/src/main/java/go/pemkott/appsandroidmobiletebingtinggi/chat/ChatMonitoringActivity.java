package go.pemkott.appsandroidmobiletebingtinggi.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.adapter.chat.ChatMonitoringAdapter;
import go.pemkott.appsandroidmobiletebingtinggi.adapter.chat.MultiSelectItem;
import go.pemkott.appsandroidmobiletebingtinggi.adapter.chat.SearchableMultiSelectAdapter;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.model.OpdModel;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMonitoringResponse;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMonitoringThread;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatMonitoringActivity extends AppCompatActivity implements ChatMonitoringAdapter.OnThreadClickListener {

    private RecyclerView rvMonitoringList;
    private ProgressBar pbMonitoring;
    private LinearLayout llEmptyMonitoring;
    private EditText etSearchMonitoring;
    private MaterialCardView cvOpdFilterMonitoring;
    private TextView tvSelectedOpdMonitoringName;

    private ChatMonitoringAdapter adapter;
    private List<ChatMonitoringThread> listThreads = new ArrayList<>();
    private List<OpdModel> listOpd = new ArrayList<>();

    private String selectedOpdId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        go.pemkott.appsandroidmobiletebingtinggi.utils.WindowUtil.applyDashboardStyle(this);

        if (!go.pemkott.appsandroidmobiletebingtinggi.database.ChatLocalDatabase.isSuperAdminUser(this)) {
            android.widget.Toast.makeText(this, "Akses ditolak: Hanya akun Monitoring (NIP 199402092020121005) yang berwenang mengakses halaman ini.", android.widget.Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_chat_monitoring);

        ImageView ivBack = findViewById(R.id.ivBackMonitoring);
        ImageView ivRefresh = findViewById(R.id.ivRefreshMonitoring);

        etSearchMonitoring = findViewById(R.id.etSearchMonitoring);
        cvOpdFilterMonitoring = findViewById(R.id.cvOpdFilterMonitoring);
        tvSelectedOpdMonitoringName = findViewById(R.id.tvSelectedOpdMonitoringName);
        rvMonitoringList = findViewById(R.id.rvMonitoringList);
        pbMonitoring = findViewById(R.id.pbMonitoring);
        llEmptyMonitoring = findViewById(R.id.llEmptyMonitoring);

        ivBack.setOnClickListener(v -> finish());
        ivRefresh.setOnClickListener(v -> {
            go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.showTopBanner(
                    this, "Menyinkronkan data monitoring dari server...", go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.Type.SYNC
            );
            fetchMonitoringData();
        });

        cvOpdFilterMonitoring.setOnClickListener(v -> showOpdFilterBottomSheet());

        rvMonitoringList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatMonitoringAdapter(this, new ArrayList<>(), this);
        rvMonitoringList.setAdapter(adapter);

        etSearchMonitoring.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                fetchMonitoringData();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        checkAndSetupMasterSwitch();
        fetchMonitoringData();
    }

    private void checkAndSetupMasterSwitch() {
        com.google.android.material.switchmaterial.SwitchMaterial switchMaster = findViewById(R.id.switchGlobalChatMaster);
        TextView tvDesc = findViewById(R.id.tvMasterToggleDesc);
        if (switchMaster == null) return;

        HttpService api = RetroClient.getInstance(this).getApi2();

        CompoundButton.OnCheckedChangeListener[] listenerHolder = new CompoundButton.OnCheckedChangeListener[1];

        listenerHolder[0] = (buttonView, isChecked) -> {
            api.toggleChatGlobalStatus(isChecked ? "1" : "0").enqueue(new Callback<go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatGlobalStatusResponse>() {
                @Override
                public void onResponse(Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatGlobalStatusResponse> call, Response<go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatGlobalStatusResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                        boolean nowEnabled = response.body().isChatEnabled();
                        switchMaster.setOnCheckedChangeListener(null);
                        switchMaster.setChecked(nowEnabled);
                        switchMaster.setOnCheckedChangeListener(listenerHolder[0]);

                        if (tvDesc != null) {
                            tvDesc.setText(nowEnabled 
                                    ? "Status: Menu Chat TAMPIL di seluruh perangkat pegawai" 
                                    : "Status: Menu Chat TERSEMBUNYI dari seluruh perangkat pegawai");
                        }
                        go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.showTopBanner(
                                ChatMonitoringActivity.this, response.body().getMessage(), go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.Type.SUCCESS
                        );
                    }
                }

                @Override
                public void onFailure(Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatGlobalStatusResponse> call, Throwable t) {
                    go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.showTopBanner(
                            ChatMonitoringActivity.this, "Gagal mengubah status global: " + t.getMessage(), go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.Type.ERROR
                    );
                }
            });
        };

        api.getChatGlobalStatus().enqueue(new Callback<go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatGlobalStatusResponse>() {
            @Override
            public void onResponse(Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatGlobalStatusResponse> call, Response<go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatGlobalStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    boolean enabled = response.body().isChatEnabled();
                    switchMaster.setOnCheckedChangeListener(null);
                    switchMaster.setChecked(enabled);
                    switchMaster.setOnCheckedChangeListener(listenerHolder[0]);
                    if (tvDesc != null) {
                        tvDesc.setText(enabled 
                                ? "Status: Menu Chat TAMPIL di seluruh perangkat pegawai" 
                                : "Status: Menu Chat TERSEMBUNYI dari seluruh perangkat pegawai");
                    }
                } else {
                    switchMaster.setOnCheckedChangeListener(null);
                    switchMaster.setChecked(false);
                    switchMaster.setOnCheckedChangeListener(listenerHolder[0]);
                }
            }

            @Override
            public void onFailure(Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatGlobalStatusResponse> call, Throwable t) {
                switchMaster.setOnCheckedChangeListener(listenerHolder[0]);
            }
        });
    }

    private void showOpdFilterBottomSheet() {
        if (listOpd == null || listOpd.isEmpty()) {
            Toast.makeText(this, "Daftar Unit Kerja / OPD belum tersedia", Toast.LENGTH_SHORT).show();
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
        View btnApply = view.findViewById(R.id.btnApplySelection);

        tvTitle.setText("Filter OPD Monitoring");
        tvCount.setVisibility(View.GONE);
        tvSelectAll.setText("Tampilkan Semua OPD");
        tvClearAll.setVisibility(View.GONE);

        List<MultiSelectItem> items = new ArrayList<>();
        items.add(new MultiSelectItem(0, "Semua Unit Kerja / OPD", "Tampilkan seluruh riwayat pesan pegawai", null, selectedOpdId.isEmpty()));

        for (OpdModel o : listOpd) {
            long opdIdVal = 0;
            try {
                opdIdVal = (long) Double.parseDouble(o.getId().trim());
            } catch (Exception ignored) {}
            if (opdIdVal > 0) {
                boolean isSelected = String.valueOf(opdIdVal).equals(selectedOpdId);
                items.add(new MultiSelectItem(opdIdVal, o.getOpd(), "Unit Kerja / OPD", null, isSelected));
            }
        }

        SearchableMultiSelectAdapter multiAdapter = new SearchableMultiSelectAdapter(this, items, true, count -> {});
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(multiAdapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                multiAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        tvSelectAll.setOnClickListener(v -> {
            selectedOpdId = "";
            tvSelectedOpdMonitoringName.setText("Semua Unit Kerja / OPD");
            fetchMonitoringData();
            dialog.dismiss();
        });

        ivClose.setOnClickListener(v -> dialog.dismiss());

        btnApply.setOnClickListener(v -> {
            List<Long> selectedIds = multiAdapter.getSelectedIds();
            if (selectedIds.isEmpty() || selectedIds.contains(0L)) {
                selectedOpdId = "";
                tvSelectedOpdMonitoringName.setText("Semua Unit Kerja / OPD");
            } else {
                selectedOpdId = String.valueOf(selectedIds.get(0));
                for (OpdModel o : listOpd) {
                    if (o.getId().equals(selectedOpdId)) {
                        tvSelectedOpdMonitoringName.setText(o.getOpd());
                        break;
                    }
                }
            }
            fetchMonitoringData();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void fetchMonitoringData() {
        pbMonitoring.setVisibility(View.VISIBLE);
        String search = etSearchMonitoring.getText() != null ? etSearchMonitoring.getText().toString().trim() : "";

        HttpService api = RetroClient.getInstance(this).getApi2();
        api.getMonitoringConversations(search, selectedOpdId).enqueue(new Callback<ChatMonitoringResponse>() {
            @Override
            public void onResponse(Call<ChatMonitoringResponse> call, Response<ChatMonitoringResponse> response) {
                pbMonitoring.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    listThreads = response.body().getThreads();
                    listOpd = response.body().getOpdList();

                    if (listThreads == null || listThreads.isEmpty()) {
                        llEmptyMonitoring.setVisibility(View.VISIBLE);
                        adapter.setList(new ArrayList<>());
                    } else {
                        llEmptyMonitoring.setVisibility(View.GONE);
                        adapter.setList(listThreads);
                    }
                } else {
                    llEmptyMonitoring.setVisibility(View.VISIBLE);
                    adapter.setList(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<ChatMonitoringResponse> call, Throwable t) {
                pbMonitoring.setVisibility(View.GONE);
                llEmptyMonitoring.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onThreadClick(ChatMonitoringThread thread) {
        Intent intent = new Intent(this, ChatRoomActivity.class);
        intent.putExtra("target_id", thread.getReceiverId());
        intent.putExtra("chat_type", "personal");
        intent.putExtra("title", thread.getSenderName() + " ➔ " + thread.getReceiverName());
        intent.putExtra("subtitle", "Monitoring Obrolan Kedinasan");
        intent.putExtra("avatar_url", "");
        intent.putExtra("is_monitoring_mode", true);
        startActivity(intent);
    }

    @Override
    public void onThreadLongClick(ChatMonitoringThread thread) {
        if (thread == null) return;
        String[] options = new String[]{
                "Buka Obrolan Monitoring",
                "Tandai Bersihkan Obrolan (Soft Clear Global)",
                "Hapus Riwayat Obrolan Permanen dari Database"
        };

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Kelola Obrolan (" + thread.getSenderName() + " ➔ " + thread.getReceiverName() + ")")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        onThreadClick(thread);
                    } else if (which == 1) {
                        confirmGlobalClearThread(thread);
                    } else if (which == 2) {
                        confirmHardClearThread(thread);
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void confirmGlobalClearThread(ChatMonitoringThread thread) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Tandai Bersihkan Obrolan (Global)")
                .setMessage("Apakah Anda ingin menandai seluruh riwayat obrolan ini dibersihkan? Riwayat pesan akan disembunyikan dari kedua pegawai, namun tetap tersimpan di database.")
                .setPositiveButton("Tandai Bersihkan", (dialog, which) -> {
                    HttpService api = RetroClient.getInstance(this).getApi2();
                    api.clearConversationHistory(thread.getReceiverId(), "personal", "global").enqueue(new Callback<go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse>() {
                        @Override
                        public void onResponse(Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse> call, Response<go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse> response) {}
                        @Override
                        public void onFailure(Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse> call, Throwable t) {}
                    });

                    go.pemkott.appsandroidmobiletebingtinggi.database.ChatLocalDatabase chatDb =
                            new go.pemkott.appsandroidmobiletebingtinggi.database.ChatLocalDatabase(this);
                    chatDb.softClearConversation("personal", thread.getReceiverId());
                    chatDb.syncLastMessageFromLocal("personal", thread.getReceiverId(), true);

                    fetchMonitoringData();
                    Toast.makeText(this, "Obrolan ditandai dibersihkan kedinasan (Global)", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void confirmHardClearThread(ChatMonitoringThread thread) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Hapus Riwayat Permanen dari Database")
                .setMessage("PERINGATAN: Seluruh percakapan antara kedua pegawai ini akan dihapus secara fisik dan permanen dari database. Tindakan ini tidak dapat dibatalkan (irreversible). Lanjutkan?")
                .setPositiveButton("Hapus Permanen", (dialog, which) -> {
                    HttpService api = RetroClient.getInstance(this).getApi2();
                    api.clearConversationHistory(thread.getReceiverId(), "personal", "hard_delete").enqueue(new Callback<go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse>() {
                        @Override
                        public void onResponse(Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse> call, Response<go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse> response) {}
                        @Override
                        public void onFailure(Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse> call, Throwable t) {}
                    });

                    go.pemkott.appsandroidmobiletebingtinggi.database.ChatLocalDatabase chatDb =
                            new go.pemkott.appsandroidmobiletebingtinggi.database.ChatLocalDatabase(this);
                    chatDb.hardClearConversation("personal", thread.getReceiverId());
                    chatDb.syncLastMessageFromLocal("personal", thread.getReceiverId(), true);

                    fetchMonitoringData();
                    Toast.makeText(this, "Seluruh riwayat obrolan telah dihapus permanen dari database", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}
