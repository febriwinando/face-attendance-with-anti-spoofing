package go.pemkott.appsandroidmobiletebingtinggi.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
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
        EdgeToEdge.enable(this);
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

        fetchMonitoringData();
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
            long opdId = 0;
            try {
                opdId = Long.parseLong(o.getId());
            } catch (Exception ignored) {}
            if (opdId > 0) {
                boolean isSelected = String.valueOf(opdId).equals(selectedOpdId);
                items.add(new MultiSelectItem(opdId, o.getOpd(), "Unit Kerja / OPD", null, isSelected));
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
        startActivity(intent);
    }
}
