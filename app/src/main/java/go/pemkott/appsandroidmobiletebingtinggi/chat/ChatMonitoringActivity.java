package go.pemkott.appsandroidmobiletebingtinggi.chat;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.adapter.chat.ChatMonitoringAdapter;
import go.pemkott.appsandroidmobiletebingtinggi.adapter.chat.ChatMonitoringMessageAdapter;
import go.pemkott.appsandroidmobiletebingtinggi.adapter.chat.MultiSelectItem;
import go.pemkott.appsandroidmobiletebingtinggi.adapter.chat.SearchableMultiSelectAdapter;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.database.ChatLocalDatabase;
import go.pemkott.appsandroidmobiletebingtinggi.model.OpdModel;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatContact;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatContactResponse;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatGlobalStatusResponse;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMonitoringMessage;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMonitoringMessagesResponse;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMonitoringResponse;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMonitoringThread;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatPagination;
import go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback;
import go.pemkott.appsandroidmobiletebingtinggi.utils.WindowUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Halaman Monitoring Chat Administrator Khusus NIP 199402092020121005.
 * Mendukung monitoring seluruh percakapan & seluruh pesan (personal & grup OPD, aktif & terhapus),
 * filter lengkap (OPD, pegawai, NIP, tanggal, status, pencarian), paginasi, audit log,
 * serta penghapusan permanen satu atau banyak pesan dengan konfirmasi dua tahap.
 */
public class ChatMonitoringActivity extends AppCompatActivity
        implements ChatMonitoringAdapter.OnThreadClickListener, ChatMonitoringMessageAdapter.Listener {

    private static final int MODE_THREADS = 0;
    private static final int MODE_ALL_MESSAGES = 1;
    private static final int PER_PAGE = 25;

    private int activeMode = MODE_THREADS;
    private int currentPage = 1;
    private int lastPage = 1;

    private String selectedOpdId = "";
    private String selectedEmployeeId = "";
    private String selectedDate = "";
    private String selectedStatus = "all";

    private EditText etSearchMonitoring, etFilterNipMonitoring;
    private TextView tabThreads, tabMessages, tvSelectedOpdName, btnFilterEmployee, btnFilterDate, btnFilterStatus;
    private TextView tvMonitoringSelectedCount, tvMonitoringPageInfo;
    private LinearLayout llMultiSelectBar, llEmptyMonitoring;
    private RecyclerView rvMonitoringList;
    private ProgressBar pbMonitoring;

    private ChatMonitoringAdapter threadAdapter;
    private ChatMonitoringMessageAdapter messageAdapter;
    private ChatLocalDatabase chatDb;

    private List<OpdModel> listOpd = new ArrayList<>();
    private List<ChatContact> listEmployeesCache = new ArrayList<>();

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowUtil.applyDashboardStyle(this);

        if (!ChatLocalDatabase.isSuperAdminUser(this)) {
            Toast.makeText(this, "Akses ditolak: Hanya akun Monitoring (NIP 199402092020121005) yang berwenang mengakses halaman ini.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_chat_monitoring);
        chatDb = new ChatLocalDatabase(this);

        bindViews();
        setupListeners();
        checkAndSetupMasterSwitch();
        updateTabUi();
        fetchMonitoringData();
    }

    private void bindViews() {
        etSearchMonitoring = findViewById(R.id.etSearchMonitoring);
        etFilterNipMonitoring = findViewById(R.id.etFilterNipMonitoring);
        tabThreads = findViewById(R.id.tabMonitoringThreads);
        tabMessages = findViewById(R.id.tabMonitoringMessages);
        tvSelectedOpdName = findViewById(R.id.tvSelectedOpdMonitoringName);
        btnFilterEmployee = findViewById(R.id.btnFilterEmployeeMonitoring);
        btnFilterDate = findViewById(R.id.btnFilterDateMonitoring);
        btnFilterStatus = findViewById(R.id.btnFilterStatusMonitoring);
        llMultiSelectBar = findViewById(R.id.llMonitoringMultiSelectBar);
        tvMonitoringSelectedCount = findViewById(R.id.tvMonitoringSelectedCount);
        tvMonitoringPageInfo = findViewById(R.id.tvMonitoringPageInfo);
        llEmptyMonitoring = findViewById(R.id.llEmptyMonitoring);
        rvMonitoringList = findViewById(R.id.rvMonitoringList);
        pbMonitoring = findViewById(R.id.pbMonitoring);

        rvMonitoringList.setLayoutManager(new LinearLayoutManager(this));
        threadAdapter = new ChatMonitoringAdapter(this, new ArrayList<>(), this);
        messageAdapter = new ChatMonitoringMessageAdapter(this, new ArrayList<>(), this);
        rvMonitoringList.setAdapter(threadAdapter);
    }

    private void setupListeners() {
        ImageView ivBack = findViewById(R.id.ivBackMonitoring);
        ImageView ivRefresh = findViewById(R.id.ivRefreshMonitoring);
        TextView btnAudit = findViewById(R.id.btnViewAuditLogs);
        MaterialCardView cvOpd = findViewById(R.id.cvOpdFilterMonitoring);
        TextView btnReset = findViewById(R.id.btnResetFiltersMonitoring);
        TextView btnClearSelect = findViewById(R.id.btnMonitoringClearSelect);
        TextView btnBatchDelete = findViewById(R.id.btnMonitoringBatchPermanentDelete);
        TextView btnPrev = findViewById(R.id.btnMonitoringPrevPage);
        TextView btnNext = findViewById(R.id.btnMonitoringNextPage);

        ivBack.setOnClickListener(v -> finish());
        ivRefresh.setOnClickListener(v -> {
            CustomUiFeedback.showTopBanner(this, "Menyinkronkan data monitoring...", CustomUiFeedback.Type.SYNC);
            fetchMonitoringData();
        });
        btnAudit.setOnClickListener(v -> ChatMonitoringPermanentDeleteHelper.showAuditLogsDialog(this));

        tabThreads.setOnClickListener(v -> switchMode(MODE_THREADS));
        tabMessages.setOnClickListener(v -> switchMode(MODE_ALL_MESSAGES));

        cvOpd.setOnClickListener(v -> showOpdFilterBottomSheet());
        btnFilterEmployee.setOnClickListener(v -> showEmployeeFilterBottomSheet());
        btnFilterDate.setOnClickListener(v -> showDatePickerFilter());
        btnFilterStatus.setOnClickListener(v -> ChatMonitoringPermanentDeleteHelper.showStatusPickerDialog(this, (code, label) -> {
            selectedStatus = code;
            btnFilterStatus.setText("Status: " + label);
            currentPage = 1;
            fetchMonitoringData();
        }));

        btnReset.setOnClickListener(v -> resetAllFilters());

        TextWatcher debouncedWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                scheduleFilterReload();
            }
        };
        etSearchMonitoring.addTextChangedListener(debouncedWatcher);
        etFilterNipMonitoring.addTextChangedListener(debouncedWatcher);

        btnClearSelect.setOnClickListener(v -> messageAdapter.clearSelections());
        btnBatchDelete.setOnClickListener(v -> ChatMonitoringPermanentDeleteHelper.showTwoStagePermanentDelete(
                this,
                messageAdapter.getSelectedMessages(),
                chatDb,
                () -> {
                    messageAdapter.clearSelections();
                    fetchMonitoringData();
                }
        ));

        btnPrev.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                fetchMonitoringData();
            }
        });
        btnNext.setOnClickListener(v -> {
            if (currentPage < lastPage) {
                currentPage++;
                fetchMonitoringData();
            }
        });
    }

    private void switchMode(int mode) {
        if (activeMode == mode) return;
        activeMode = mode;
        currentPage = 1;
        messageAdapter.clearSelections();
        updateTabUi();
        rvMonitoringList.setAdapter(activeMode == MODE_THREADS ? threadAdapter : messageAdapter);
        fetchMonitoringData();
    }

    private void updateTabUi() {
        int activeBg = ContextCompat.getColor(this, R.color.primary_brand);
        int activeText = ContextCompat.getColor(this, R.color.fixed_white);
        int inactiveBg = ContextCompat.getColor(this, R.color.surface_variant);
        int inactiveText = ContextCompat.getColor(this, R.color.text_secondary);

        if (activeMode == MODE_THREADS) {
            tabThreads.setBackgroundColor(activeBg);
            tabThreads.setTextColor(activeText);
            tabMessages.setBackgroundColor(inactiveBg);
            tabMessages.setTextColor(inactiveText);
            llMultiSelectBar.setVisibility(View.GONE);
        } else {
            tabMessages.setBackgroundColor(activeBg);
            tabMessages.setTextColor(activeText);
            tabThreads.setBackgroundColor(inactiveBg);
            tabThreads.setTextColor(inactiveText);
        }
    }

    private void scheduleFilterReload() {
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
        searchRunnable = () -> {
            currentPage = 1;
            fetchMonitoringData();
        };
        searchHandler.postDelayed(searchRunnable, 350);
    }

    private void resetAllFilters() {
        selectedOpdId = "";
        selectedEmployeeId = "";
        selectedDate = "";
        selectedStatus = "all";
        currentPage = 1;
        etSearchMonitoring.setText("");
        etFilterNipMonitoring.setText("");
        tvSelectedOpdName.setText("OPD: Semua");
        btnFilterEmployee.setText("Pegawai: Semua");
        btnFilterDate.setText("Tanggal: Semua");
        btnFilterStatus.setText("Status: Semua");
        fetchMonitoringData();
    }

    private void fetchMonitoringData() {
        pbMonitoring.setVisibility(View.VISIBLE);
        String search = etSearchMonitoring.getText() != null ? etSearchMonitoring.getText().toString().trim() : "";
        String nip = etFilterNipMonitoring.getText() != null ? etFilterNipMonitoring.getText().toString().trim() : "";

        if (activeMode == MODE_THREADS) {
            fetchMonitoringConversations(search, nip);
        } else {
            fetchMonitoringAllMessages(search, nip);
        }
    }

    private void fetchMonitoringConversations(String search, String nip) {
        HttpService api = RetroClient.getInstance(this).getApi2();
        api.getMonitoringConversations(search, selectedOpdId, selectedEmployeeId, nip, selectedDate, selectedStatus, "all", currentPage, PER_PAGE)
                .enqueue(new Callback<ChatMonitoringResponse>() {
                    @Override
                    public void onResponse(Call<ChatMonitoringResponse> call, Response<ChatMonitoringResponse> response) {
                        pbMonitoring.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                            if (response.body().getOpdList() != null && !response.body().getOpdList().isEmpty()) {
                                listOpd = response.body().getOpdList();
                            }
                            List<ChatMonitoringThread> threads = response.body().getThreads();
                            if (threads == null) threads = new ArrayList<>();
                            threadAdapter.setList(threads);
                            llEmptyMonitoring.setVisibility(threads.isEmpty() ? View.VISIBLE : View.GONE);
                            updatePaginationLabel(response.body().getPagination(), threads.size(), "percakapan");
                        } else {
                            threadAdapter.setList(new ArrayList<>());
                            llEmptyMonitoring.setVisibility(View.VISIBLE);
                            CustomUiFeedback.showTopBanner(
                                    ChatMonitoringActivity.this,
                                    ChatDeletionUiHelper.extractErrorMessage(response, "Gagal memuat percakapan monitoring."),
                                    CustomUiFeedback.Type.ERROR
                            );
                        }
                    }

                    @Override
                    public void onFailure(Call<ChatMonitoringResponse> call, Throwable t) {
                        pbMonitoring.setVisibility(View.GONE);
                        llEmptyMonitoring.setVisibility(View.VISIBLE);
                        CustomUiFeedback.showTopBanner(
                                ChatMonitoringActivity.this, "Koneksi gagal: " + t.getMessage(), CustomUiFeedback.Type.ERROR
                        );
                    }
                });
    }

    private void fetchMonitoringAllMessages(String search, String nip) {
        HttpService api = RetroClient.getInstance(this).getApi2();
        api.getMonitoringMessages(search, selectedOpdId, selectedEmployeeId, nip, selectedDate, selectedStatus, "all", currentPage, PER_PAGE)
                .enqueue(new Callback<ChatMonitoringMessagesResponse>() {
                    @Override
                    public void onResponse(Call<ChatMonitoringMessagesResponse> call, Response<ChatMonitoringMessagesResponse> response) {
                        pbMonitoring.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                            List<ChatMonitoringMessage> msgs = response.body().getData();
                            if (msgs == null) msgs = new ArrayList<>();
                            messageAdapter.setList(msgs);
                            llEmptyMonitoring.setVisibility(msgs.isEmpty() ? View.VISIBLE : View.GONE);
                            updatePaginationLabel(response.body().getPagination(), msgs.size(), "pesan");
                        } else {
                            messageAdapter.setList(new ArrayList<>());
                            llEmptyMonitoring.setVisibility(View.VISIBLE);
                            CustomUiFeedback.showTopBanner(
                                    ChatMonitoringActivity.this,
                                    ChatDeletionUiHelper.extractErrorMessage(response, "Gagal memuat daftar pesan monitoring."),
                                    CustomUiFeedback.Type.ERROR
                            );
                        }
                    }

                    @Override
                    public void onFailure(Call<ChatMonitoringMessagesResponse> call, Throwable t) {
                        pbMonitoring.setVisibility(View.GONE);
                        llEmptyMonitoring.setVisibility(View.VISIBLE);
                        CustomUiFeedback.showTopBanner(
                                ChatMonitoringActivity.this, "Koneksi gagal: " + t.getMessage(), CustomUiFeedback.Type.ERROR
                        );
                    }
                });
    }

    private void updatePaginationLabel(ChatPagination pag, int visibleCount, String unitLabel) {
        if (pag != null) {
            currentPage = Math.max(1, pag.getCurrentPage());
            lastPage = Math.max(1, pag.getLastPage());
            tvMonitoringPageInfo.setText("Hal " + currentPage + " / " + lastPage + " (Total " + pag.getTotal() + " " + unitLabel + ")");
        } else {
            lastPage = 1;
            tvMonitoringPageInfo.setText("Menampilkan " + visibleCount + " " + unitLabel);
        }
    }

    private void showDatePickerFilter() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dlg = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            btnFilterDate.setText("Tgl: " + selectedDate);
            currentPage = 1;
            fetchMonitoringData();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        dlg.setButton(DatePickerDialog.BUTTON_NEUTRAL, "Semua Tanggal", (dialog, which) -> {
            selectedDate = "";
            btnFilterDate.setText("Tanggal: Semua");
            currentPage = 1;
            fetchMonitoringData();
        });
        dlg.show();
    }

    private void showOpdFilterBottomSheet() {
        if (listOpd == null || listOpd.isEmpty()) {
            Toast.makeText(this, "Daftar Unit Kerja / OPD sedang dimuat", Toast.LENGTH_SHORT).show();
            return;
        }

        List<MultiSelectItem> items = new ArrayList<>();
        items.add(new MultiSelectItem(0, "Semua Unit Kerja / OPD", "Tampilkan seluruh OPD", null, selectedOpdId.isEmpty()));
        for (OpdModel o : listOpd) {
            long opdIdVal = 0;
            try {
                opdIdVal = (long) Double.parseDouble(o.getId().trim());
            } catch (Exception ignored) {}
            if (opdIdVal > 0) {
                items.add(new MultiSelectItem(opdIdVal, o.getOpd(), "Unit Kerja / OPD", null, String.valueOf(opdIdVal).equals(selectedOpdId)));
            }
        }

        showPickerBottomSheet("Filter Unit Kerja / OPD", items, selected -> {
            if (selected == null || selected.getId() == 0) {
                selectedOpdId = "";
                tvSelectedOpdName.setText("OPD: Semua");
            } else {
                selectedOpdId = String.valueOf(selected.getId());
                tvSelectedOpdName.setText("OPD: " + selected.getTitle());
            }
            currentPage = 1;
            fetchMonitoringData();
        });
    }

    private void showEmployeeFilterBottomSheet() {
        if (!listEmployeesCache.isEmpty()) {
            openEmployeePickerWithList(listEmployeesCache);
            return;
        }

        HttpService api = RetroClient.getInstance(this).getApi2();
        api.getChatContacts("", "", 1).enqueue(new Callback<ChatContactResponse>() {
            @Override
            public void onResponse(Call<ChatContactResponse> call, Response<ChatContactResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getPegawai() != null) {
                    List<ChatContact> empOnly = new ArrayList<>();
                    for (ChatContact c : response.body().getPegawai()) {
                        if ("personal".equalsIgnoreCase(c.getChatType())) {
                            empOnly.add(c);
                        }
                    }
                    listEmployeesCache = empOnly;
                    if ((listOpd == null || listOpd.isEmpty()) && response.body().getOpdList() != null) {
                        listOpd = response.body().getOpdList();
                    }
                    openEmployeePickerWithList(empOnly);
                } else {
                    Toast.makeText(ChatMonitoringActivity.this, "Gagal memuat daftar pegawai", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ChatContactResponse> call, Throwable t) {
                Toast.makeText(ChatMonitoringActivity.this, "Koneksi gagal memuat daftar pegawai", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openEmployeePickerWithList(List<ChatContact> employees) {
        List<MultiSelectItem> items = new ArrayList<>();
        items.add(new MultiSelectItem(0, "Semua Pegawai", "Tampilkan seluruh pegawai", null, selectedEmployeeId.isEmpty()));
        for (ChatContact c : employees) {
            String sub = (!TextUtils.isEmpty(c.getNip()) ? c.getNip() + " • " : "") + (!TextUtils.isEmpty(c.getSubtitle()) ? c.getSubtitle() : "");
            items.add(new MultiSelectItem(c.getTargetId(), c.getTitle(), sub, c.getAvatarUrl(), String.valueOf(c.getTargetId()).equals(selectedEmployeeId)));
        }

        showPickerBottomSheet("Filter Pegawai Monitoring", items, selected -> {
            if (selected == null || selected.getId() == 0) {
                selectedEmployeeId = "";
                btnFilterEmployee.setText("Pegawai: Semua");
            } else {
                selectedEmployeeId = String.valueOf(selected.getId());
                btnFilterEmployee.setText("Pegawai: " + selected.getTitle());
            }
            currentPage = 1;
            fetchMonitoringData();
        });
    }

    private interface OnSinglePickerApply {
        void onApplied(MultiSelectItem picked);
    }

    private void showPickerBottomSheet(String title, List<MultiSelectItem> items, OnSinglePickerApply onApply) {
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

        tvTitle.setText(title);
        tvCount.setVisibility(View.GONE);
        tvSelectAll.setText("Tampilkan Semua");
        tvClearAll.setVisibility(View.GONE);

        SearchableMultiSelectAdapter multiAdapter = new SearchableMultiSelectAdapter(this, items, true, count -> {});
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(multiAdapter);

        ivClose.setOnClickListener(v -> dialog.dismiss());
        tvSelectAll.setOnClickListener(v -> {
            onApply.onApplied(null);
            dialog.dismiss();
        });
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                multiAdapter.filter(s.toString());
            }
        });
        btnApply.setOnClickListener(v -> {
            MultiSelectItem picked = multiAdapter.getSingleSelectedItem();
            onApply.onApplied(picked);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void checkAndSetupMasterSwitch() {
        SwitchMaterial switchMaster = findViewById(R.id.switchGlobalChatMaster);
        TextView tvDesc = findViewById(R.id.tvMasterToggleDesc);
        if (switchMaster == null) return;

        HttpService api = RetroClient.getInstance(this).getApi2();
        CompoundButton.OnCheckedChangeListener[] listenerHolder = new CompoundButton.OnCheckedChangeListener[1];

        listenerHolder[0] = (buttonView, isChecked) ->
                api.toggleChatGlobalStatus(isChecked ? "1" : "0").enqueue(new Callback<ChatGlobalStatusResponse>() {
                    @Override
                    public void onResponse(Call<ChatGlobalStatusResponse> call, Response<ChatGlobalStatusResponse> response) {
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
                            CustomUiFeedback.showTopBanner(
                                    ChatMonitoringActivity.this, response.body().getMessage(), CustomUiFeedback.Type.SUCCESS
                            );
                        }
                    }

                    @Override
                    public void onFailure(Call<ChatGlobalStatusResponse> call, Throwable t) {
                        CustomUiFeedback.showTopBanner(
                                ChatMonitoringActivity.this, "Gagal mengubah status global: " + t.getMessage(), CustomUiFeedback.Type.ERROR
                        );
                    }
                });

        api.getChatGlobalStatus().enqueue(new Callback<ChatGlobalStatusResponse>() {
            @Override
            public void onResponse(Call<ChatGlobalStatusResponse> call, Response<ChatGlobalStatusResponse> response) {
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
            public void onFailure(Call<ChatGlobalStatusResponse> call, Throwable t) {
                switchMaster.setOnCheckedChangeListener(listenerHolder[0]);
            }
        });
    }

    @Override
    public void onThreadClick(ChatMonitoringThread thread) {
        if (thread == null) return;
        Intent intent = new Intent(this, ChatMonitoringDetailActivity.class);
        intent.putExtra("chat_type", thread.getChatType());
        intent.putExtra("participant_a", thread.getSenderId());
        intent.putExtra("participant_b", thread.getReceiverId());
        intent.putExtra("opd_id", thread.getOpdId());
        intent.putExtra("sender_name", thread.getSenderName());
        intent.putExtra("receiver_name", thread.getReceiverName());
        intent.putExtra("sender_nip", thread.getSenderNip());
        intent.putExtra("receiver_nip", thread.getReceiverNip());
        intent.putExtra("opd_name", thread.getOpdName());
        startActivity(intent);
    }

    @Override
    public void onThreadLongClick(ChatMonitoringThread thread) {
        onThreadClick(thread);
    }

    @Override
    public void onInspectMessage(ChatMonitoringMessage message) {
        ChatMonitoringPermanentDeleteHelper.showMessageInspectorDialog(this, message, chatDb, this::fetchMonitoringData);
    }

    @Override
    public void onPermanentDeleteSingle(ChatMonitoringMessage message) {
        ChatMonitoringPermanentDeleteHelper.showSinglePermanentDelete(this, message, chatDb, this::fetchMonitoringData);
    }

    @Override
    public void onSelectionCountChanged(int selectedCount) {
        if (activeMode == MODE_ALL_MESSAGES && selectedCount > 0) {
            llMultiSelectBar.setVisibility(View.VISIBLE);
            tvMonitoringSelectedCount.setText(selectedCount + " pesan dipilih untuk hapus permanen");
        } else {
            llMultiSelectBar.setVisibility(View.GONE);
        }
    }
}
