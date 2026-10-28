package go.pemkott.appsandroidmobiletebingtinggi.chat;

import android.content.Intent;
import android.os.Bundle;
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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

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

public class ChatListActivity extends AppCompatActivity implements ChatContactAdapter.OnContactClickListener {

    private RecyclerView rvChatList;
    private ProgressBar pbChatList;
    private LinearLayout llEmptyChatState;
    private EditText etSearchChat;
    private MaterialCardView cvOpdFilterSelector;
    private TextView tvSelectedOpdFilterName;
    private ExtendedFloatingActionButton btnNewChatFab;

    private ChatContactAdapter adapter;
    private ChatLocalDatabase chatDb;

    private List<ChatContact> listPegawai = new ArrayList<>();
    private List<OpdModel> listOpd = new ArrayList<>();

    private String selectedOpdId = ""; // Empty = All OPDs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_list);

        chatDb = new ChatLocalDatabase(this);

        ImageView ivBack = findViewById(R.id.ivBackChatList);
        ImageView ivRefresh = findViewById(R.id.ivRefreshChatList);
        ImageView ivPrivacy = findViewById(R.id.ivPrivacyChatList);

        etSearchChat = findViewById(R.id.etSearchChat);
        cvOpdFilterSelector = findViewById(R.id.cvOpdFilterSelector);
        tvSelectedOpdFilterName = findViewById(R.id.tvSelectedOpdFilterName);
        rvChatList = findViewById(R.id.rvChatList);
        pbChatList = findViewById(R.id.pbChatList);
        llEmptyChatState = findViewById(R.id.llEmptyChatState);
        btnNewChatFab = findViewById(R.id.btnNewChatFab);

        MaterialCardView cvMonitoringBanner = findViewById(R.id.cvMonitoringBanner);

        go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager session = new go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager(this);
        go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper dbHelper = new go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper(this);

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

        if (isMonitoringUser && cvMonitoringBanner != null) {
            cvMonitoringBanner.setVisibility(View.VISIBLE);
            cvMonitoringBanner.setOnClickListener(v -> startActivity(new Intent(this, ChatMonitoringActivity.class)));
        }

        ivBack.setOnClickListener(v -> finish());
        ivRefresh.setOnClickListener(v -> {
            go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.showTopBanner(
                    this, "Menyinkronkan data chat dari server...", go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.Type.SYNC
            );
            fetchContactsFromNetwork();
        });
        ivPrivacy.setOnClickListener(v -> startActivity(new Intent(this, ChatPrivacyActivity.class)));
        btnNewChatFab.setOnClickListener(v -> startActivity(new Intent(this, ChatContactPickerActivity.class)));
        cvOpdFilterSelector.setOnClickListener(v -> showOpdFilterBottomSheet());

        rvChatList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatContactAdapter(this, new ArrayList<>(), this);
        rvChatList.setAdapter(adapter);

        etSearchChat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 1. Load local cached conversations instantly (No automatic background network call)
        loadLocalContacts();
        if (listPegawai == null || listPegawai.isEmpty()) {
            fetchContactsFromNetwork();
        }

        // 2. Tampilkan dialog persetujuan Syarat & Ketentuan jika belum disetujui
        checkAndShowChatTermsDialog();
    }

    private void checkAndShowChatTermsDialog() {
        go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager session = new go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager(this);
        if (session.isChatTermsAccepted()) {
            return;
        }

        android.app.Dialog dialog = new android.app.Dialog(this, R.style.DialogStyle);
        dialog.setContentView(R.layout.dialog_chat_terms);
        dialog.setCancelable(false);

        android.widget.CheckBox cbAgree = dialog.findViewById(R.id.cbAgreeTerms);
        View btnDecline = dialog.findViewById(R.id.btnDeclineTerms);
        View btnAccept = dialog.findViewById(R.id.btnAcceptTerms);

        if (cbAgree != null && btnAccept != null && btnDecline != null) {
            cbAgree.setOnCheckedChangeListener((buttonView, isChecked) -> {
                btnAccept.setEnabled(isChecked);
            });

            btnDecline.setOnClickListener(v -> {
                dialog.dismiss();
                finish();
            });

            btnAccept.setOnClickListener(v -> {
                session.setChatTermsAccepted(true);
                dialog.dismiss();
                go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.showTopBanner(
                        this, "Persetujuan Syarat & Ketentuan berhasil disimpan", go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.Type.SUCCESS
                );
            });
        }

        dialog.show();
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

        tvTitle.setText("Filter Berdasarkan Unit Kerja / OPD");
        tvCount.setVisibility(View.GONE);
        tvSelectAll.setText("Tampilkan Semua OPD");
        tvClearAll.setVisibility(View.GONE);

        List<MultiSelectItem> items = new ArrayList<>();
        items.add(new MultiSelectItem(0, "Semua Unit Kerja / OPD", "Tampilkan seluruh riwayat percakapan", null, selectedOpdId.isEmpty()));

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

        SearchableMultiSelectAdapter adapter = new SearchableMultiSelectAdapter(this, items, true, count -> {});
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

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

        tvSelectAll.setOnClickListener(v -> {
            selectedOpdId = "";
            tvSelectedOpdFilterName.setText("Semua Unit Kerja / OPD");
            filterList();
            dialog.dismiss();
        });

        ivClose.setOnClickListener(v -> dialog.dismiss());

        btnApply.setOnClickListener(v -> {
            List<Long> selectedIds = adapter.getSelectedIds();
            if (selectedIds.isEmpty() || selectedIds.contains(0L)) {
                selectedOpdId = "";
                tvSelectedOpdFilterName.setText("Semua Unit Kerja / OPD");
            } else {
                selectedOpdId = String.valueOf(selectedIds.get(0));
                for (OpdModel o : listOpd) {
                    if (o.getId().equals(selectedOpdId)) {
                        tvSelectedOpdFilterName.setText(o.getOpd());
                        break;
                    }
                }
            }
            filterList();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void loadLocalContacts() {
        List<ChatContact> localContacts = chatDb.getRecentConversations();
        if (localContacts != null && !localContacts.isEmpty()) {
            listPegawai = localContacts;
            filterList();
        }
    }

    private void fetchContactsFromNetwork() {
        if (listPegawai.isEmpty()) {
            pbChatList.setVisibility(View.VISIBLE);
        }

        HttpService api = RetroClient.getInstance(this).getApi2();
        api.getChatContacts("", "", 1).enqueue(new Callback<ChatContactResponse>() {
            @Override
            public void onResponse(Call<ChatContactResponse> call, Response<ChatContactResponse> response) {
                pbChatList.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    listPegawai = response.body().getPegawai();
                    listOpd = response.body().getOpdList();

                    chatDb.saveContacts(listPegawai);
                    filterList();
                } else {
                    if (listPegawai.isEmpty()) {
                        llEmptyChatState.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ChatContactResponse> call, Throwable t) {
                pbChatList.setVisibility(View.GONE);
                if (listPegawai.isEmpty()) {
                    llEmptyChatState.setVisibility(View.VISIBLE);
                    Toast.makeText(ChatListActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showList(List<ChatContact> contacts) {
        if (contacts == null || contacts.isEmpty()) {
            llEmptyChatState.setVisibility(View.VISIBLE);
            adapter.setList(new ArrayList<>());
        } else {
            llEmptyChatState.setVisibility(View.GONE);
            adapter.setList(contacts);
        }
    }

    private void filterList() {
        String query = etSearchChat.getText() != null ? etSearchChat.getText().toString().toLowerCase().trim() : "";
        List<ChatContact> filtered = new ArrayList<>();

        for (ChatContact c : listPegawai) {
            if (TextUtils.isEmpty(c.getLastMessage())) {
                continue;
            }

            if (!selectedOpdId.isEmpty()) {
                if (c.getOpdId() != Long.parseLong(selectedOpdId)) {
                    continue;
                }
            }

            if (!query.isEmpty()) {
                boolean matchName = c.getTitle().toLowerCase().contains(query);
                boolean matchSub = c.getSubtitle().toLowerCase().contains(query);
                boolean matchNip = c.getNip().contains(query);
                boolean matchLast = c.getLastMessage().toLowerCase().contains(query);
                if (!matchName && !matchSub && !matchNip && !matchLast) {
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLocalContacts();
    }
}
