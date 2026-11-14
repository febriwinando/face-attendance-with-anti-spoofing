package go.pemkott.appsandroidmobiletebingtinggi.chat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
    private View llSearchChatContainer;
    private ImageView ivSearchChatToggle;
    private ImageView ivClearSearchChat;
    private EditText etSearchChat;
    private View cvOpdFilterSelector;
    private TextView tvSelectedOpdFilterName;
    private View btnNewChatFab;
    private ImageView btnHeaderNewChat;

    private TextView tvFilterAll, tvFilterUnread, tvFilterOpd;
    private String activeTabFilter = "all";

    private ChatContactAdapter adapter;
    private ChatLocalDatabase chatDb;

    private List<ChatContact> listPegawai = new ArrayList<>();
    private List<OpdModel> listOpd = new ArrayList<>();

    private String selectedOpdId = ""; // Empty = All OPDs
    private android.app.Dialog chatTermsDialog = null;

    public static volatile boolean isChatListActive = false;
    private static java.lang.ref.WeakReference<ChatListActivity> activeListRef = null;

    public static void notifyIncomingMessageFromFCM() {
        if (isChatListActive && activeListRef != null) {
            ChatListActivity activity = activeListRef.get();
            if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                activity.runOnUiThread(activity::fetchContactsFromNetwork);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager session =
                new go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager(this);
        String myNip = session.getNip();
        boolean isMonitoringOfficer = "199402092020121005".equals(myNip);

        if (!isMonitoringOfficer && !session.isChatGlobalEnabled()) {
            Toast.makeText(this, "Layanan chat saat ini dinonaktifkan oleh Administrator.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        go.pemkott.appsandroidmobiletebingtinggi.utils.WindowUtil.applyDashboardStyle(this);
        setContentView(R.layout.activity_chat_list);

        chatDb = new ChatLocalDatabase(this);

        ImageView ivBack = findViewById(R.id.ivBackChatList);
        ivSearchChatToggle = findViewById(R.id.ivSearchChatToggle);
        ImageView ivRefresh = findViewById(R.id.ivRefreshChatList);
        btnHeaderNewChat = findViewById(R.id.btnHeaderNewChat);

        llSearchChatContainer = findViewById(R.id.llSearchChatContainer);
        etSearchChat = findViewById(R.id.etSearchChat);
        ivClearSearchChat = findViewById(R.id.ivClearSearchChat);
        cvOpdFilterSelector = findViewById(R.id.cvOpdFilterSelector);
        tvSelectedOpdFilterName = findViewById(R.id.tvSelectedOpdFilterName);
        rvChatList = findViewById(R.id.rvChatList);
        pbChatList = findViewById(R.id.pbChatList);
        llEmptyChatState = findViewById(R.id.llEmptyChatState);
        btnNewChatFab = findViewById(R.id.btnNewChatFab);

        tvFilterAll = findViewById(R.id.tvFilterAll);
        tvFilterUnread = findViewById(R.id.tvFilterUnread);
        tvFilterOpd = findViewById(R.id.tvFilterOpd);

        View tabCalls = findViewById(R.id.llTabCalls);
        View tabSettings = findViewById(R.id.llTabSettings);

        if (tabCalls != null) {
            tabCalls.setOnClickListener(v -> startActivity(new Intent(this, ChatContactPickerActivity.class)));
        }
        if (tabSettings != null) {
            tabSettings.setOnClickListener(v -> startActivity(new Intent(this, ChatPrivacyActivity.class)));
        }

        if (tvFilterAll != null) {
            tvFilterAll.setOnClickListener(v -> setTabFilter("all"));
        }
        if (tvFilterUnread != null) {
            tvFilterUnread.setOnClickListener(v -> setTabFilter("unread"));
        }
        if (tvFilterOpd != null) {
            tvFilterOpd.setOnClickListener(v -> setTabFilter("opd"));
        }

        if (btnHeaderNewChat != null) {
            btnHeaderNewChat.setOnClickListener(v -> startActivity(new Intent(this, ChatContactPickerActivity.class)));
        }

        ivBack.setOnClickListener(v -> finish());

        if (ivSearchChatToggle != null) {
            ivSearchChatToggle.setOnClickListener(v -> toggleSearchBar());
        }

        if (ivClearSearchChat != null) {
            ivClearSearchChat.setOnClickListener(v -> {
                if (etSearchChat != null) {
                    if (etSearchChat.getText().length() > 0) {
                        etSearchChat.setText("");
                    } else {
                        toggleSearchBar();
                    }
                }
            });
        }

        ivRefresh.setOnClickListener(v -> {
            go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.showTopBanner(
                    this, "Menyinkronkan data chat dari server...", go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.Type.SYNC
            );
            fetchContactsFromNetwork();
        });
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
                if (ivClearSearchChat != null) {
                    ivClearSearchChat.setVisibility((s != null && s.length() > 0) ? View.VISIBLE : View.GONE);
                }
                filterList();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (llSearchChatContainer != null && llSearchChatContainer.getVisibility() == View.VISIBLE) {
                    toggleSearchBar();
                } else {
                    finish();
                }
            }
        });

        // 1. Load local cached conversations instantly and sync in background
        loadLocalContacts();
        fetchContactsFromNetwork();

        // 2. Tampilkan dialog persetujuan Syarat & Ketentuan jika belum disetujui
        checkAndShowChatTermsDialog();
    }

    private void checkAndShowChatTermsDialog() {
        go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager session = new go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager(this);
        if (session.isChatTermsAccepted()) {
            return;
        }

        // Cek status persetujuan dari server database website
        HttpService api = RetroClient.getInstance(this).getApi2();
        api.getChatTermsStatus().enqueue(new Callback<go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse>() {
            @Override
            public void onResponse(Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse> call, Response<go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isStatus()) {
                        session.setChatTermsAccepted(true);
                        if (chatTermsDialog != null && chatTermsDialog.isShowing()) {
                            chatTermsDialog.dismiss();
                        }
                        return;
                    }
                    displayChatTermsDialog(session);
                }
            }

            @Override
            public void onFailure(Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse> call, Throwable t) {
                // Sinkronisasi lanjutan akan ditangani oleh fetchContactsFromNetwork jika koneksi kembali pulih
            }
        });
    }

    private void displayChatTermsDialog(go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager session) {
        if (isFinishing() || isDestroyed() || session.isChatTermsAccepted()) {
            return;
        }
        if (chatTermsDialog != null && chatTermsDialog.isShowing()) {
            return;
        }

        chatTermsDialog = new android.app.Dialog(this, R.style.DialogStyle);
        chatTermsDialog.setContentView(R.layout.dialog_chat_terms);
        chatTermsDialog.setCancelable(false);

        android.widget.CheckBox cbAgree = chatTermsDialog.findViewById(R.id.cbAgreeTerms);
        View btnDecline = chatTermsDialog.findViewById(R.id.btnDeclineTerms);
        View btnAccept = chatTermsDialog.findViewById(R.id.btnAcceptTerms);

        if (cbAgree != null && btnAccept != null && btnDecline != null) {
            cbAgree.setOnCheckedChangeListener((buttonView, isChecked) -> {
                btnAccept.setEnabled(isChecked);
            });

            btnDecline.setOnClickListener(v -> {
                if (chatTermsDialog != null && chatTermsDialog.isShowing()) {
                    chatTermsDialog.dismiss();
                }
                finish();
            });

            btnAccept.setOnClickListener(v -> {
                btnAccept.setEnabled(false);
                if (btnAccept instanceof TextView) {
                    ((TextView) btnAccept).setText("Menyimpan...");
                }

                // Kirim dan pastikan persetujuan benar-benar tersimpan ke database website
                HttpService api = RetroClient.getInstance(this).getApi2();
                api.acceptChatTerms(1).enqueue(new Callback<go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse>() {
                    @Override
                    public void onResponse(Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse> call, Response<go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse> response) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                            session.setChatTermsAccepted(true);
                            if (chatTermsDialog != null && chatTermsDialog.isShowing()) {
                                chatTermsDialog.dismiss();
                            }
                            go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.showTopBanner(
                                    ChatListActivity.this,
                                    "Persetujuan Syarat & Ketentuan berhasil disimpan",
                                    go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.Type.SUCCESS
                            );
                        } else {
                            btnAccept.setEnabled(true);
                            if (btnAccept instanceof TextView) {
                                ((TextView) btnAccept).setText("Setuju & Lanjutkan");
                            }
                            go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.showTopBanner(
                                    ChatListActivity.this,
                                    "Gagal menyimpan persetujuan ke server. Silakan coba lagi.",
                                    go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.Type.ERROR
                            );
                        }
                    }

                    @Override
                    public void onFailure(Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse> call, Throwable t) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        btnAccept.setEnabled(true);
                        if (btnAccept instanceof TextView) {
                            ((TextView) btnAccept).setText("Setuju & Lanjutkan");
                        }
                        go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.showTopBanner(
                                ChatListActivity.this,
                                "Koneksi gagal saat menyimpan persetujuan ke server.",
                                go.pemkott.appsandroidmobiletebingtinggi.utils.CustomUiFeedback.Type.ERROR
                        );
                    }
                });
            });
        }

        chatTermsDialog.show();
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
        List<ChatContact> localContacts = chatDb.getRecentConversations(false);
        if (localContacts != null && !localContacts.isEmpty()) {
            listPegawai = localContacts;
            filterList();
            if (pbChatList != null) pbChatList.setVisibility(View.GONE);
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
                    go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager session =
                            new go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager(ChatListActivity.this);
                    String myNip = session.getNip();
                    boolean isMonitoringOfficer = "199402092020121005".equals(myNip);

                    Boolean chatEnabled = response.body().getChatEnabled();
                    if (Boolean.FALSE.equals(chatEnabled)) {
                        session.saveChatGlobalEnabled(false);
                        if (!isMonitoringOfficer) {
                            Toast.makeText(ChatListActivity.this, "Layanan chat saat ini dinonaktifkan oleh Administrator.", Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                    } else if (Boolean.TRUE.equals(chatEnabled)) {
                        session.saveChatGlobalEnabled(true);
                    }

                    listPegawai = response.body().getPegawai();
                    listOpd = response.body().getOpdList();

                    // Sinkronkan status persetujuan Syarat & Ketentuan langsung dari database server
                    Boolean termsAccepted = response.body().getTermsAccepted();
                    if (termsAccepted != null) {
                        if (Boolean.TRUE.equals(termsAccepted)) {
                            session.setChatTermsAccepted(true);
                            if (chatTermsDialog != null && chatTermsDialog.isShowing()) {
                                chatTermsDialog.dismiss();
                            }
                        } else {
                            session.setChatTermsAccepted(false);
                            displayChatTermsDialog(session);
                        }
                    }

                    chatDb.saveContacts(listPegawai);
                    for (ChatContact c : listPegawai) {
                        chatDb.syncLastMessageFromLocal(c.getChatType(), c.getTargetId(), false);
                    }
                    List<ChatContact> syncedContacts = chatDb.getRecentConversations(false);
                    if (syncedContacts != null) {
                        listPegawai = syncedContacts;
                    }
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

    private void setTabFilter(String mode) {
        activeTabFilter = mode;
        if (tvFilterAll == null || tvFilterUnread == null || tvFilterOpd == null) return;

        int activeTextColor = ContextCompat.getColor(this, R.color.chat_filter_active_text);
        int inactiveTextColor = ContextCompat.getColor(this, R.color.text_secondary);

        if ("all".equals(mode)) {
            tvFilterAll.setBackgroundResource(R.drawable.bg_pill_filter_active);
            tvFilterAll.setTextColor(activeTextColor);
            tvFilterUnread.setBackgroundResource(R.drawable.bg_pill_filter_inactive);
            tvFilterUnread.setTextColor(inactiveTextColor);
            tvFilterOpd.setBackgroundResource(R.drawable.bg_pill_filter_inactive);
            tvFilterOpd.setTextColor(inactiveTextColor);
        } else if ("unread".equals(mode)) {
            tvFilterAll.setBackgroundResource(R.drawable.bg_pill_filter_inactive);
            tvFilterAll.setTextColor(inactiveTextColor);
            tvFilterUnread.setBackgroundResource(R.drawable.bg_pill_filter_active);
            tvFilterUnread.setTextColor(activeTextColor);
            tvFilterOpd.setBackgroundResource(R.drawable.bg_pill_filter_inactive);
            tvFilterOpd.setTextColor(inactiveTextColor);
        } else if ("opd".equals(mode)) {
            tvFilterAll.setBackgroundResource(R.drawable.bg_pill_filter_inactive);
            tvFilterAll.setTextColor(inactiveTextColor);
            tvFilterUnread.setBackgroundResource(R.drawable.bg_pill_filter_inactive);
            tvFilterUnread.setTextColor(inactiveTextColor);
            tvFilterOpd.setBackgroundResource(R.drawable.bg_pill_filter_active);
            tvFilterOpd.setTextColor(activeTextColor);
        }
        filterList();
    }

    private void filterList() {
        String query = etSearchChat.getText() != null ? etSearchChat.getText().toString().toLowerCase().trim() : "";
        List<ChatContact> filtered = new ArrayList<>();

        for (ChatContact c : listPegawai) {
            boolean isOpdGroup = "opd".equals(c.getChatType());
            if (!isOpdGroup && TextUtils.isEmpty(c.getLastMessage())) {
                continue;
            }

            if ("unread".equals(activeTabFilter) && c.getUnreadCount() <= 0) {
                continue;
            }

            if ("opd".equals(activeTabFilter) && !isOpdGroup) {
                continue;
            }

            if (!selectedOpdId.isEmpty()) {
                if (c.getOpdId() != Long.parseLong(selectedOpdId)) {
                    continue;
                }
            }

            if (!query.isEmpty()) {
                boolean matchName = c.getTitle() != null && c.getTitle().toLowerCase().contains(query);
                boolean matchSub = c.getSubtitle() != null && c.getSubtitle().toLowerCase().contains(query);
                boolean matchNip = c.getNip() != null && c.getNip().contains(query);
                boolean matchLast = c.getLastMessage() != null && c.getLastMessage().toLowerCase().contains(query);
                if (!matchName && !matchSub && !matchNip && !matchLast) {
                    continue;
                }
            }

            filtered.add(c);
        }

        showList(filtered);
    }

    private void toggleSearchBar() {
        if (llSearchChatContainer == null) return;

        if (llSearchChatContainer.getVisibility() == View.VISIBLE) {
            llSearchChatContainer.setVisibility(View.GONE);
            if (ivSearchChatToggle != null) {
                ivSearchChatToggle.setColorFilter(ContextCompat.getColor(this, R.color.text_secondary));
            }
            if (etSearchChat != null) {
                etSearchChat.setText("");
                hideKeyboard(etSearchChat);
            }
        } else {
            llSearchChatContainer.setVisibility(View.VISIBLE);
            if (ivSearchChatToggle != null) {
                ivSearchChatToggle.setColorFilter(ContextCompat.getColor(this, R.color.primary_brand));
            }
            if (etSearchChat != null) {
                etSearchChat.requestFocus();
                showKeyboard(etSearchChat);
            }
        }
    }

    private void showKeyboard(View view) {
        if (view == null) return;
        view.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 150);
    }

    private void hideKeyboard(View view) {
        if (view == null) return;
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onContactClick(ChatContact contact) {
        contact.setUnreadCount(0);
        adapter.notifyDataSetChanged();
        chatDb.resetUnreadCount(contact.getChatType(), contact.getTargetId());

        Intent intent = new Intent(this, ChatRoomActivity.class);
        intent.putExtra("target_id", contact.getTargetId());
        intent.putExtra("chat_type", contact.getChatType());
        intent.putExtra("title", contact.getTitle());
        intent.putExtra("subtitle", contact.getSubtitle());
        intent.putExtra("avatar_url", contact.getAvatarUrl());
        intent.putExtra("is_blocked", contact.isBlocked());
        startActivity(intent);
    }

    @Override
    public void onContactLongClick(ChatContact contact) {
        if (contact == null) return;

        go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMessageItem latestMsg =
                chatDb.getLatestVisibleMessageForContact(contact.getChatType(), contact.getTargetId());

        List<String> optList = new ArrayList<>();
        optList.add("Buka Obrolan");
        if (latestMsg != null) {
            optList.add("Hapus Pesan Terakhir");
        }
        optList.add("Hapus Percakapan");

        String[] options = optList.toArray(new String[0]);
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(contact.getTitle())
                .setItems(options, (dialog, which) -> {
                    String selected = options[which];
                    if ("Buka Obrolan".equals(selected)) {
                        onContactClick(contact);
                    } else if ("Hapus Pesan Terakhir".equals(selected) && latestMsg != null) {
                        ChatDeletionUiHelper.showScopePickerThenDeleteSingle(
                                this,
                                chatDb,
                                latestMsg,
                                contact.getChatType(),
                                contact.getTargetId(),
                                this::loadLocalContacts,
                                this::fetchContactsFromNetwork
                        );
                    } else if ("Hapus Percakapan".equals(selected)) {
                        ChatDeletionUiHelper.showClearConversationFlow(
                                this,
                                chatDb,
                                contact.getChatType(),
                                contact.getTargetId(),
                                this::loadLocalContacts
                        );
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isChatListActive = true;
        activeListRef = new java.lang.ref.WeakReference<>(this);
        loadLocalContacts();
        fetchContactsFromNetwork();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isChatListActive = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isChatListActive = false;
    }
}
