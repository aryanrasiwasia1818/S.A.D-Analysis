package com.example.mypersonalityapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// Import ChatActivity here
import com.example.mypersonalityapp.ChatActivity;

public class ResultActivity extends AppCompatActivity {

    private TextView textViewResult;
    private TextView textViewScore;
    private Button buttonRetake;

    // Method to send answers to the ML server
    public void sendToMLServer(int[] answers) {
        OkHttpClient client = new OkHttpClient();

        // Creating the JSON array of answers
        JSONArray jsonArray = new JSONArray();
        for (int ans : answers) {
            jsonArray.put(ans);
        }

        // Preparing the request body in JSON format
        JSONObject bodyJson = new JSONObject();
        try {
            bodyJson.put("answers", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        // Creating the request body
        RequestBody requestBody = RequestBody.create(
                bodyJson.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        // Preparing the POST request
        Request request = new Request.Builder()
                .url("https://274c-2409-40e3-20c5-5d10-540b-c8ba-6ec3-bc5e.ngrok-free.app/predict")  // Flask server IP and port
                .post(requestBody)
                .build();

        // Sending the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ResultActivity.this, "❌ Server connection failed", Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                Log.d("ML_RESPONSE", res);  // Log the raw JSON response

                if (response.isSuccessful()) {
                    try {
                        // Parse the server response
                        JSONObject resultJson = new JSONObject(res);
                        String stress = resultJson.getString("Stress");
                        String anxiety = resultJson.getString("Anxiety");
                        String depression = resultJson.getString("Depression");

                        runOnUiThread(() -> {
                            // Display results
                            String existingText = textViewResult.getText().toString();
                            String mlText = "\n\n🧠 ML Prediction:\n" +
                                    "Stress: " + stress + "\n" +
                                    "Anxiety: " + anxiety + "\n" +
                                    "Depression: " + depression;

                            textViewResult.setText(existingText + mlText);
                            Toast.makeText(ResultActivity.this, "✅ ML predictions received", Toast.LENGTH_SHORT).show();
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(ResultActivity.this, "❌ JSON parsing error", Toast.LENGTH_LONG).show());
                    }
                } else {
                    // Handle server error response
                    runOnUiThread(() -> Toast.makeText(ResultActivity.this, "❌ Server returned error: " + response.code(), Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Initialize UI components
        textViewResult = findViewById(R.id.textViewResult);
        textViewScore = findViewById(R.id.textViewScore);
        buttonRetake = findViewById(R.id.buttonRetake);

        // Retrieve data from the previous activity
        Intent intent = getIntent();
        double extraversion = intent.getDoubleExtra("Extraversion", 0.0);
        double agreeableness = intent.getDoubleExtra("Agreeableness", 0.0);
        double conscientiousness = intent.getDoubleExtra("Conscientiousness", 0.0);
        double emotionalStability = intent.getDoubleExtra("Emotional Stability", 0.0);
        double openness = intent.getDoubleExtra("Openness", 0.0);
        String personalityType = intent.getStringExtra("PERSONALITY_TYPE");

        // Set the personality type and scores
        textViewResult.setText("Your Personality Type: " + personalityType);
        String breakdown = String.format(
                "Extraversion: %.2f\nAgreeableness: %.2f\nConscientiousness: %.2f\nEmotional Stability: %.2f\nOpenness: %.2f",
                extraversion, agreeableness, conscientiousness, emotionalStability, openness
        );
        textViewScore.setText(breakdown);

        // Restart quiz on button click
        buttonRetake.setOnClickListener(view -> {
            Intent restartIntent = new Intent(ResultActivity.this, QuizActivity.class);
            startActivity(restartIntent);
            finish();
        });

        // Set up the chat button to launch ChatActivity
        Button buttonChat = findViewById(R.id.buttonChat);
        buttonChat.setOnClickListener(view -> {
            Intent chatIntent = new Intent(ResultActivity.this, ChatActivity.class);
            startActivity(chatIntent);
        });

        // Send answers to the ML server
        int[] allAnswers = getIntent().getIntArrayExtra("ALL_ANSWERS");
        if (allAnswers != null && allAnswers.length == 31) {
            Toast.makeText(this, "📡 Sending answers to ML server...", Toast.LENGTH_SHORT).show();
            sendToMLServer(allAnswers);
        } else {
            Toast.makeText(this, "❌ Answers missing or incomplete", Toast.LENGTH_LONG).show();
        }
    }
}
