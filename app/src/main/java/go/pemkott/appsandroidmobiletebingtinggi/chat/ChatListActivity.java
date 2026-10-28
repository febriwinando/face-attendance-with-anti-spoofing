package go.pemkott.appsandroidmobiletebingtinggi.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.adapter.chat.ChatContactAdapter;
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
    private TextView tvEmptyChatList;
    private EditText etSearchChat;
    private Spinner spOpdFilter;

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
        spOpdFilter = findViewById(R.id.spOpdFilter);
        rvChatList = findViewById(R.id.rvChatList);
        pbChatList = findViewById(R.id.pbChatList);
        tvEmptyChatList = findViewById(R.id.tvEmptyChatList);

        ivBack.setOnClickListener(v -> finish());
        ivRefresh.setOnClickListener(v -> fetchContactsFromNetwork());
        ivPrivacy.setOnClickListener(v -> startActivity(new Intent(this, ChatPrivacyActivity.class)));

        rvChatList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatContactAdapter(this, new ArrayList<>(), this);
        rvChatList.setAdapter(adapter);

        spOpdFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedOpdId = "";
                } else if (position - 1 < listOpd.size()) {
                    selectedOpdId = listOpd.get(position - 1).getId();
                }
                filterList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

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

        // 1. Load local cached contacts instantly
        loadLocalContacts();

        // 2. Fetch fresh contacts in background
        fetchContactsFromNetwork();
    }

    private void loadLocalContacts() {
        List<ChatContact> localContacts = chatDb.getContacts();
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
        api.getChatContacts("", "").enqueue(new Callback<ChatContactResponse>() {
            @Override
            public void onResponse(Call<ChatContactResponse> call, Response<ChatContactResponse> response) {
                pbChatList.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    listPegawai = response.body().getPegawai();
                    listOpd = response.body().getOpdList();

                    // Persist contacts into local SQLite
                    chatDb.saveContacts(listPegawai);

                    setupOpdSpinner();
                    filterList();
                } else {
                    if (listPegawai.isEmpty()) {
                        tvEmptyChatList.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ChatContactResponse> call, Throwable t) {
                pbChatList.setVisibility(View.GONE);
                if (listPegawai.isEmpty()) {
                    tvEmptyChatList.setVisibility(View.VISIBLE);
                    Toast.makeText(ChatListActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupOpdSpinner() {
        List<String> spinnerItems = new ArrayList<>();
        spinnerItems.add("Semua Unit Kerja / OPD");
        for (OpdModel o : listOpd) {
            spinnerItems.add(o.getOpd());
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, spinnerItems
        );
        spOpdFilter.setAdapter(spinnerAdapter);
    }

    private void showList(List<ChatContact> contacts) {
        if (contacts == null || contacts.isEmpty()) {
            tvEmptyChatList.setVisibility(View.VISIBLE);
            adapter.setList(new ArrayList<>());
        } else {
            tvEmptyChatList.setVisibility(View.GONE);
            adapter.setList(contacts);
        }
    }

    private void filterList() {
        String query = etSearchChat.getText() != null ? etSearchChat.getText().toString().toLowerCase().trim() : "";
        List<ChatContact> filtered = new ArrayList<>();

        for (ChatContact c : listPegawai) {
            // OPD Filter
            if (!selectedOpdId.isEmpty()) {
                if (c.getOpdId() != Long.parseLong(selectedOpdId)) {
                    continue;
                }
            }

            // Search Filter
            if (!query.isEmpty()) {
                boolean matchName = c.getTitle().toLowerCase().contains(query);
                boolean matchSub = c.getSubtitle().toLowerCase().contains(query);
                boolean matchNip = c.getNip().contains(query);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchContactsFromNetwork();
    }
}
