package go.pemkott.appsandroidmobiletebingtinggi.chat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.adapter.chat.ChatMonitoringMessageAdapter;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.database.ChatLocalDatabase;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMonitoringMessage;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMonitoringMessagesResponse;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatPagination;
import go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback;
import go.pemkott.appsandroidmobiletebingtinggi.utils.WindowUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Halaman Detail Percakapan Monitoring khusus Administrator (NIP 199402092020121005).
 * Menampilkan seluruh pesan dalam percakapan (aktif maupun sudah dihapus secara logis) secara read-only
 * tanpa mengubah status dibaca pegawai, serta menyediakan penghapusan permanen dua tahap.
 */
public class ChatMonitoringDetailActivity extends AppCompatActivity implements ChatMonitoringMessageAdapter.Listener {

    private String chatType = "personal";
    private long participantA = 0L;
    private long participantB = 0L;
    private long opdId = 0L;

    private String selectedStatus = "all";
    private int currentPage = 1;
    private int lastPage = 1;
    private static final int PER_PAGE = 50;

    private EditText etSearch;
    private TextView btnStatusFilter, tvDetailSelectedCount, tvDetailPageInfo, tvDetailEmpty;
    private LinearLayout llMultiSelectBar;
    private RecyclerView rvMessages;
    private ProgressBar pbLoading;

    private ChatMonitoringMessageAdapter adapter;
    private ChatLocalDatabase chatDb;
    private final Handler searchDebounceHandler = new Handler(Looper.getMainLooper());
    private Runnable searchDebounceRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowUtil.applyDashboardStyle(this);

        if (!ChatLocalDatabase.isSuperAdminUser(this)) {
            Toast.makeText(this, "Akses ditolak. Halaman ini khusus Administrator Monitoring (NIP 199402092020121005).", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_chat_monitoring_detail);
        chatDb = new ChatLocalDatabase(this);

        chatType = getIntent().getStringExtra("chat_type");
        if (TextUtils.isEmpty(chatType)) chatType = "personal";
        participantA = getIntent().getLongExtra("participant_a", 0L);
        participantB = getIntent().getLongExtra("participant_b", 0L);
        opdId = getIntent().getLongExtra("opd_id", 0L);

        String senderName = getIntent().getStringExtra("sender_name");
        String receiverName = getIntent().getStringExtra("receiver_name");
        String opdName = getIntent().getStringExtra("opd_name");
        String senderNip = getIntent().getStringExtra("sender_nip");
        String receiverNip = getIntent().getStringExtra("receiver_nip");

        ImageView ivBack = findViewById(R.id.ivBackMonDetail);
        ImageView ivRefresh = findViewById(R.id.ivRefreshMonDetail);
        TextView tvTitle = findViewById(R.id.tvMonDetailTitle);
        TextView tvSubtitle = findViewById(R.id.tvMonDetailSubtitle);

        etSearch = findViewById(R.id.etSearchMonDetail);
        btnStatusFilter = findViewById(R.id.btnDetailStatusFilter);
        llMultiSelectBar = findViewById(R.id.llDetailMultiSelectBar);
        tvDetailSelectedCount = findViewById(R.id.tvDetailSelectedCount);
        TextView btnClearSelect = findViewById(R.id.btnDetailClearSelect);
        TextView btnBatchDelete = findViewById(R.id.btnDetailBatchPermanentDelete);
        rvMessages = findViewById(R.id.rvMonDetailMessages);
        pbLoading = findViewById(R.id.pbMonDetail);
        tvDetailEmpty = findViewById(R.id.tvMonDetailEmpty);

        TextView btnPrev = findViewById(R.id.btnDetailPrevPage);
        TextView btnNext = findViewById(R.id.btnDetailNextPage);
        tvDetailPageInfo = findViewById(R.id.tvDetailPageInfo);

        if ("opd".equalsIgnoreCase(chatType)) {
            tvTitle.setText(!TextUtils.isEmpty(senderName) ? senderName : ("Grup OPD #" + opdId));
            tvSubtitle.setText("OPD: " + (!TextUtils.isEmpty(opdName) ? opdName : "-") + " • Pesan Aktif & Terhapus");
        } else {
            tvTitle.setText((!TextUtils.isEmpty(senderName) ? senderName : "-")
                    + " ↔ "
                    + (!TextUtils.isEmpty(receiverName) ? receiverName : "-"));
            tvSubtitle.setText("NIP: " + (!TextUtils.isEmpty(senderNip) ? senderNip : "-")
                    + " / " + (!TextUtils.isEmpty(receiverNip) ? receiverNip : "-"));
        }

        ivBack.setOnClickListener(v -> finish());
        ivRefresh.setOnClickListener(v -> loadConversationDetail());

        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatMonitoringMessageAdapter(this, new ArrayList<>(), this);
        rvMessages.setAdapter(adapter);

        btnStatusFilter.setOnClickListener(v -> ChatMonitoringPermanentDeleteHelper.showStatusPickerDialog(this, (code, label) -> {
            selectedStatus = code;
            btnStatusFilter.setText("Status: " + label);
            currentPage = 1;
            loadConversationDetail();
        }));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchDebounceRunnable != null) searchDebounceHandler.removeCallbacks(searchDebounceRunnable);
                searchDebounceRunnable = () -> {
                    currentPage = 1;
                    loadConversationDetail();
                };
                searchDebounceHandler.postDelayed(searchDebounceRunnable, 350);
            }
        });

        btnClearSelect.setOnClickListener(v -> adapter.clearSelections());
        btnBatchDelete.setOnClickListener(v -> ChatMonitoringPermanentDeleteHelper.showTwoStagePermanentDelete(
                this, adapter.getSelectedMessages(), chatDb, () -> {
                    adapter.clearSelections();
                    loadConversationDetail();
                }
        ));

        btnPrev.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                loadConversationDetail();
            }
        });
        btnNext.setOnClickListener(v -> {
            if (currentPage < lastPage) {
                currentPage++;
                loadConversationDetail();
            }
        });

        loadConversationDetail();
    }

    private void loadConversationDetail() {
        pbLoading.setVisibility(View.VISIBLE);
        String search = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";

        HttpService api = RetroClient.getInstance(this).getApi2();
        api.getMonitoringConversationDetail(chatType, participantA, participantB, opdId, selectedStatus, search, currentPage, PER_PAGE)
                .enqueue(new Callback<ChatMonitoringMessagesResponse>() {
                    @Override
                    public void onResponse(Call<ChatMonitoringMessagesResponse> call, Response<ChatMonitoringMessagesResponse> response) {
                        pbLoading.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                            List<ChatMonitoringMessage> items = response.body().getData();
                            if (items == null) items = new ArrayList<>();
                            adapter.setList(items);

                            ChatPagination pag = response.body().getPagination();
                            if (pag != null) {
                                currentPage = Math.max(1, pag.getCurrentPage());
                                lastPage = Math.max(1, pag.getLastPage());
                                tvDetailPageInfo.setText("Hal " + currentPage + " / " + lastPage + " (" + pag.getTotal() + " pesan)");
                            } else {
                                tvDetailPageInfo.setText("Total: " + items.size() + " pesan");
                            }

                            tvDetailEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                        } else {
                            tvDetailEmpty.setVisibility(View.VISIBLE);
                            CustomUiFeedback.showTopBanner(
                                    ChatMonitoringDetailActivity.this,
                                    ChatDeletionUiHelper.extractErrorMessage(response, "Gagal memuat detail percakapan."),
                                    CustomUiFeedback.Type.ERROR
                            );
                        }
                    }

                    @Override
                    public void onFailure(Call<ChatMonitoringMessagesResponse> call, Throwable t) {
                        pbLoading.setVisibility(View.GONE);
                        tvDetailEmpty.setVisibility(View.VISIBLE);
                        CustomUiFeedback.showTopBanner(
                                ChatMonitoringDetailActivity.this, "Kesalahan jaringan: " + t.getMessage(), CustomUiFeedback.Type.ERROR
                        );
                    }
                });
    }

    @Override
    public void onInspectMessage(ChatMonitoringMessage message) {
        ChatMonitoringPermanentDeleteHelper.showMessageInspectorDialog(this, message, chatDb, this::loadConversationDetail);
    }

    @Override
    public void onPermanentDeleteSingle(ChatMonitoringMessage message) {
        ChatMonitoringPermanentDeleteHelper.showSinglePermanentDelete(this, message, chatDb, this::loadConversationDetail);
    }

    @Override
    public void onSelectionCountChanged(int selectedCount) {
        if (selectedCount > 0) {
            llMultiSelectBar.setVisibility(View.VISIBLE);
            tvDetailSelectedCount.setText(selectedCount + " pesan dipilih untuk hapus permanen");
        } else {
            llMultiSelectBar.setVisibility(View.GONE);
        }
    }
}

