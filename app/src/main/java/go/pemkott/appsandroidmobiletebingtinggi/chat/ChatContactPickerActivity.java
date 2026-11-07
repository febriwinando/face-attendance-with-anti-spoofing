package go.pemkott.appsandroidmobiletebingtinggi.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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
import go.pemkott.appsandroidmobiletebingtinggi.adapter.chat.ChatContactAdapter;
import go.pemkott.appsandroidmobiletebingtinggi.adapter.chat.MultiSelectItem;
import go.pemkott.appsandroidmobiletebingtinggi.adapter.chat.SearchableMultiSelectAdapter;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.database.ChatLocalDatabase;
import go.pemkott.appsandroidmobiletebingtinggi.model.OpdModel;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatContact;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatContactResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatContactPickerActivity extends AppCompatActivity implements ChatContactAdapter.OnContactClickListener {

    private RecyclerView rvPickerList;
    private ProgressBar pbPicker;
    private TextView tvEmptyPicker;
    private EditText etSearchPicker;
    private MaterialCardView cvOpdFilterPicker;
    private TextView tvOpdFilterSelectedName;

    private ChatContactAdapter adapter;
    private ChatLocalDatabase chatDb;

    private List<ChatContact> listPegawai = new ArrayList<>();
    private List<OpdModel> listOpd = new ArrayList<>();

    private String selectedOpdId = "";
    private String selectedOpdName = "Semua Unit Kerja / OPD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_contact_picker);

        chatDb = new ChatLocalDatabase(this);

        ImageView ivBack = findViewById(R.id.ivBackPicker);
        etSearchPicker = findViewById(R.id.etSearchPicker);
        cvOpdFilterPicker = findViewById(R.id.cvOpdFilterPicker);
        tvOpdFilterSelectedName = findViewById(R.id.tvOpdFilterSelectedName);
        rvPickerList = findViewById(R.id.rvPickerList);
        pbPicker = findViewById(R.id.pbPicker);
        tvEmptyPicker = findViewById(R.id.tvEmptyPicker);

        ivBack.setOnClickListener(v -> finish());

        rvPickerList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatContactAdapter(this, new ArrayList<>(), this);
        rvPickerList.setAdapter(adapter);

        cvOpdFilterPicker.setOnClickListener(v -> showOpdFilterBottomSheet());

        etSearchPicker.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadAllContacts();
    }

    private void loadAllContacts() {
        pbPicker.setVisibility(View.VISIBLE);
        tvEmptyPicker.setVisibility(View.GONE);

        HttpService api = RetroClient.getInstance(this).getApi2();
        api.getChatContacts("", "", 0).enqueue(new Callback<ChatContactResponse>() {
            @Override
            public void onResponse(Call<ChatContactResponse> call, Response<ChatContactResponse> response) {
                pbPicker.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    listPegawai = response.body().getPegawai();
                    listOpd = response.body().getOpdList();
                    filterList();
                } else {
                    tvEmptyPicker.setVisibility(View.VISIBLE);
                    Toast.makeText(ChatContactPickerActivity.this, "Gagal memuat kontak", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ChatContactResponse> call, Throwable t) {
                pbPicker.setVisibility(View.GONE);
                tvEmptyPicker.setVisibility(View.VISIBLE);
                Toast.makeText(ChatContactPickerActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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

        if (tvTitle != null) tvTitle.setText("Pilih Unit Kerja / OPD");
        if (tvCount != null) tvCount.setVisibility(View.GONE);
        if (tvSelectAll != null) tvSelectAll.setText("Tampilkan Semua OPD");
        if (tvClearAll != null) tvClearAll.setVisibility(View.GONE);

        List<MultiSelectItem> items = new ArrayList<>();
        items.add(new MultiSelectItem(0, "Semua Unit Kerja / OPD", "Tampilkan seluruh pegawai", null, selectedOpdId.isEmpty()));

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
        if (rv != null) {
            rv.setLayoutManager(new LinearLayoutManager(this));
            rv.setAdapter(multiAdapter);
        }

        if (etSearch != null) {
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
        }

        if (ivClose != null) {
            ivClose.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnApply != null) {
            btnApply.setOnClickListener(v -> {
                dialog.dismiss();
                MultiSelectItem selected = multiAdapter.getSingleSelectedItem();
                if (selected != null) {
                    if (selected.getId() == 0) {
                        selectedOpdId = "";
                        selectedOpdName = "Semua Unit Kerja / OPD";
                    } else {
                        selectedOpdId = String.valueOf(selected.getId());
                        selectedOpdName = selected.getTitle();
                    }
                    if (tvOpdFilterSelectedName != null) {
                        tvOpdFilterSelectedName.setText(selectedOpdName);
                    }
                    filterList();
                }
            });
        }

        dialog.show();
    }

    private void showList(List<ChatContact> contacts) {
        if (contacts == null || contacts.isEmpty()) {
            tvEmptyPicker.setVisibility(View.VISIBLE);
            adapter.setList(new ArrayList<>());
        } else {
            tvEmptyPicker.setVisibility(View.GONE);
            adapter.setList(contacts);
        }
    }

    private void filterList() {
        String query = etSearchPicker.getText() != null ? etSearchPicker.getText().toString().toLowerCase().trim() : "";
        List<ChatContact> filtered = new ArrayList<>();

        long opdIdFilter = -1;
        if (selectedOpdId != null && !selectedOpdId.trim().isEmpty()) {
            try {
                opdIdFilter = (long) Double.parseDouble(selectedOpdId.trim());
            } catch (Exception ignored) {}
        }

        for (ChatContact c : listPegawai) {
            if (c == null) continue;

            if (opdIdFilter != -1) {
                if (c.getOpdId() != opdIdFilter) {
                    continue;
                }
            }

            if (!query.isEmpty()) {
                boolean matchName = c.getTitle() != null && c.getTitle().toLowerCase().contains(query);
                boolean matchSub = c.getSubtitle() != null && c.getSubtitle().toLowerCase().contains(query);
                boolean matchNip = c.getNip() != null && c.getNip().contains(query);
                if (!matchName && !matchSub && !matchNip) {
                    continue;
                }
            }

            filtered.add(c);
        }

        showList(filtered);
    }

    @Override
    public void onContactClick(ChatContact contact) {
        Intent intent = new Intent(this, ChatRoomActivity.class);
        intent.putExtra("target_id", contact.getTargetId());
        intent.putExtra("chat_type", contact.getChatType());
        intent.putExtra("title", contact.getTitle());
        intent.putExtra("subtitle", contact.getSubtitle());
        intent.putExtra("avatar_url", contact.getAvatarUrl());
        startActivity(intent);
        finish();
    }
}
