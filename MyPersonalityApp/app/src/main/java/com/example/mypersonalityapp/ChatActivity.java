package com.example.mypersonalityapp;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONObject;
import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {
    private Socket socket;
    private ListView chatListView;
    private EditText messageInput;
    private Button sendButton;
    private ArrayList<Message> messages;
    private ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatListView = findViewById(R.id.chatListView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messages);
        chatListView.setAdapter(chatAdapter);

        try {
            socket = IO.socket("https://back-test-3.onrender.com"); // Use your Render URL here
            socket.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        socket.on("chat message", onMessageReceived);

        sendButton.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString().trim();
            if (!messageText.isEmpty()) {
                Message userMessage = new Message("You", messageText);
                messages.add(userMessage);
                chatAdapter.notifyDataSetChanged();
                socket.emit("chat message", messageText);
                messageInput.setText("");
            }
        });
    }

    private final Emitter.Listener onMessageReceived = args -> runOnUiThread(() -> {
        JSONObject data = (JSONObject) args[0];
        String sender = data.optString("sender");
        String text = data.optString("text");
        Message aiMessage = new Message(sender, text);
        messages.add(aiMessage);
        chatAdapter.notifyDataSetChanged();
    });
}
