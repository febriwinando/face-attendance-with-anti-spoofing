package go.pemkott.appsandroidmobiletebingtinggi.chat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
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

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.adapter.chat.ChatMessageAdapter;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.database.ChatLocalDatabase;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMessageItem;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMessageResponse;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRoomActivity extends AppCompatActivity {

    private long targetId;
    private String chatType;
    private String title;
    private String subtitle;
    private String avatarUrl;

    private CircleImageView civRoomAvatar;
    private TextView tvRoomTitle, tvRoomSubtitle;
    private RecyclerView rvMessages;
    private ProgressBar pbChatRoom;
    private EditText etMessageInput;
    private MaterialButton btnSendMessage;

    private ChatMessageAdapter adapter;
    private ChatLocalDatabase chatDb;
    private List<ChatMessageItem> messageList = new ArrayList<>();

    // Real-time polling every 3 seconds
    private final Handler pollHandler = new Handler(Looper.getMainLooper());
    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            loadMessages(false);
            pollHandler.postDelayed(this, 3000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_room);

        chatDb = new ChatLocalDatabase(this);

        targetId = getIntent().getLongExtra("target_id", 0);
        chatType = getIntent().getStringExtra("chat_type");
        title = getIntent().getStringExtra("title");
        subtitle = getIntent().getStringExtra("subtitle");
        avatarUrl = getIntent().getStringExtra("avatar_url");

        if (chatType == null) chatType = "personal";

        ImageView ivBack = findViewById(R.id.ivBackChatRoom);
        ImageView ivRefresh = findViewById(R.id.ivRefreshChatRoom);
        civRoomAvatar = findViewById(R.id.civRoomAvatar);
        tvRoomTitle = findViewById(R.id.tvRoomTitle);
        tvRoomSubtitle = findViewById(R.id.tvRoomSubtitle);
        rvMessages = findViewById(R.id.rvMessages);
        pbChatRoom = findViewById(R.id.pbChatRoom);
        etMessageInput = findViewById(R.id.etMessageInput);
        btnSendMessage = findViewById(R.id.btnSendMessage);

        ivBack.setOnClickListener(v -> finish());
        ivRefresh.setOnClickListener(v -> loadMessages(true));

        tvRoomTitle.setText(title != null ? title : "Chat");
        tvRoomSubtitle.setText(subtitle != null ? subtitle : "");

        if ("opd".equals(chatType)) {
            civRoomAvatar.setImageResource(R.drawable.ic_locasi_office);
        } else {
            if (!TextUtils.isEmpty(avatarUrl)) {
                Glide.with(this)
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_baseline_person_24)
                        .error(R.drawable.ic_baseline_person_24)
                        .into(civRoomAvatar);
            } else {
                civRoomAvatar.setImageResource(R.drawable.ic_baseline_person_24);
            }
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);

        adapter = new ChatMessageAdapter(this, messageList);
        rvMessages.setAdapter(adapter);

        btnSendMessage.setOnClickListener(v -> sendMessage());

        // 1. Load local messages instantly
        loadLocalMessages();

        // 2. Fetch fresh messages from network
        loadMessages(true);
    }

    private void loadLocalMessages() {
        List<ChatMessageItem> localMsgs = chatDb.getMessages(chatType, targetId);
        if (localMsgs != null && !localMsgs.isEmpty()) {
            messageList = localMsgs;
            adapter.setList(messageList);
            rvMessages.scrollToPosition(messageList.size() - 1);
        }
    }

    private void loadMessages(boolean showProgress) {
        if (showProgress && messageList.isEmpty()) {
            pbChatRoom.setVisibility(View.VISIBLE);
        }

        HttpService api = RetroClient.getInstance(this).getApi2();
        api.getChatMessages(chatType, targetId).enqueue(new Callback<ChatMessageResponse>() {
            @Override
            public void onResponse(Call<ChatMessageResponse> call, Response<ChatMessageResponse> response) {
                pbChatRoom.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    List<ChatMessageItem> newMsgs = response.body().getMessages();
                    if (newMsgs != null) {
                        // Persist messages to local SQLite
                        chatDb.saveMessages(newMsgs);

                        if (newMsgs.size() != messageList.size()) {
                            messageList = newMsgs;
                            adapter.setList(messageList);
                            if (!messageList.isEmpty()) {
                                rvMessages.smoothScrollToPosition(messageList.size() - 1);
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ChatMessageResponse> call, Throwable t) {
                pbChatRoom.setVisibility(View.GONE);
            }
        });
    }

    private void sendMessage() {
        String msg = etMessageInput.getText().toString().trim();
        if (TextUtils.isEmpty(msg)) {
            return;
        }

        etMessageInput.setText("");
        btnSendMessage.setEnabled(false);

        HttpService api = RetroClient.getInstance(this).getApi2();
        api.sendChatMessage(chatType, targetId, msg).enqueue(new Callback<SendMessageResponse>() {
            @Override
            public void onResponse(Call<SendMessageResponse> call, Response<SendMessageResponse> response) {
                btnSendMessage.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    ChatMessageItem newMsgItem = response.body().getData();
                    if (newMsgItem != null) {
                        // Save into local SQLite
                        chatDb.insertMessage(newMsgItem);

                        adapter.addMessage(newMsgItem);
                        rvMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
                    } else {
                        loadMessages(false);
                    }
                } else {
                    Toast.makeText(ChatRoomActivity.this, "Gagal mengirim pesan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SendMessageResponse> call, Throwable t) {
                btnSendMessage.setEnabled(true);
                Toast.makeText(ChatRoomActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        pollHandler.postDelayed(pollRunnable, 3000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pollHandler.removeCallbacks(pollRunnable);
    }
}
