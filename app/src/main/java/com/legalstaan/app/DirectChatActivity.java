package com.legalstaan.app;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DirectChatActivity extends AppCompatActivity {

    private final List<ChatMessage> messages = new ArrayList<>();
    private ChatAdapter adapter;
    private RecyclerView recyclerView;
    private EditText etInput;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String contactEmail;
    private String chatId;
    private ListenerRegistration messageListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direct_chat);

        String contactName = getIntent().getStringExtra("contact_name");
        contactEmail = getIntent().getStringExtra("contact_email");

        Toolbar toolbar = findViewById(R.id.toolbar_chat);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(contactName != null ? contactName : "Chat");
            getSupportActionBar().setSubtitle(contactEmail);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null || contactEmail == null) {
            Toast.makeText(this, "Unable to open chat", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Deterministic chat ID from both emails (sorted)
        String myEmail = currentUser.getEmail() != null ? currentUser.getEmail() : currentUser.getUid();
        List<String> participants = Arrays.asList(myEmail.toLowerCase(), contactEmail.toLowerCase());
        Collections.sort(participants);
        chatId = participants.get(0).hashCode() + "_" + participants.get(1).hashCode();

        recyclerView = findViewById(R.id.rv_chat_messages);
        etInput = findViewById(R.id.et_message_input);
        ImageButton btnSend = findViewById(R.id.btn_send_msg);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);
        adapter = new ChatAdapter(messages);
        recyclerView.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendMessage());

        listenForMessages();
    }

    private void sendMessage() {
        String text = etInput.getText().toString().trim();
        if (text.isEmpty()) return;
        etInput.setText("");

        Map<String, Object> msg = new HashMap<>();
        msg.put("senderId", currentUser.getUid());
        msg.put("senderEmail", currentUser.getEmail());
        msg.put("text", text);
        msg.put("timestamp", System.currentTimeMillis());

        db.collection("chats").document(chatId)
                .collection("messages")
                .add(msg)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to send", Toast.LENGTH_SHORT).show());

        // Also update the chat metadata for both parties
        Map<String, Object> meta = new HashMap<>();
        meta.put("participants", Arrays.asList(
                currentUser.getEmail() != null ? currentUser.getEmail() : "",
                contactEmail));
        meta.put("lastMessage", text);
        meta.put("lastTimestamp", System.currentTimeMillis());
        db.collection("chats").document(chatId).set(meta);
    }

    private void listenForMessages() {
        messageListener = db.collection("chats").document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;
                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            String senderId = dc.getDocument().getString("senderId");
                            String text = dc.getDocument().getString("text");
                            boolean isMe = currentUser.getUid().equals(senderId);
                            messages.add(new ChatMessage(text != null ? text : "", isMe));
                            adapter.notifyItemInserted(messages.size() - 1);
                            recyclerView.scrollToPosition(messages.size() - 1);
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        if (messageListener != null) messageListener.remove();
        super.onDestroy();
    }
}
