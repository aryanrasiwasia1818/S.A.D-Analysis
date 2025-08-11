package com.example.mypersonalityapp;



import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatActivity extends AppCompatActivity {
    private Socket mSocket;
    private EditText inputMessage;
    private Button sendButton;
    private TextView chatDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        inputMessage = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendBtn);
        chatDisplay = findViewById(R.id.chatView);



        try {
            // Connect to the server (Update with your server URL)
            mSocket = IO.socket("https://back-test-3.onrender.com");
            mSocket.connect();

            // Set up listeners
            mSocket.on(Socket.EVENT_CONNECT, args -> runOnUiThread(() -> {
                // Notify when connected
                chatDisplay.append("Connected to chat server.\n");
            }));

            mSocket.on("chat message", args -> runOnUiThread(() -> {
                // Append received messages (chat message from the server)
                JSONObject data = (JSONObject) args[0];
                try {
                    String message = data.getString("message");
                    String sender = data.getString("sender");
                    chatDisplay.append(sender + ": " + message + "\n");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set up send button
        sendButton.setOnClickListener(v -> {
            String message = inputMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                try {
                    // Emit message to server
                    JSONObject msg = new JSONObject();
                    msg.put("message", message);
                    mSocket.emit("chat message", msg);

                    // Display user's message
                    chatDisplay.append("You: " + message + "\n");

                    // Clear input field
                    inputMessage.setText("");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSocket != null) {
            mSocket.disconnect();
        }
    }
}
