package com.example.aiassistance;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    // TODO: Replace with your actual Supabase Project URL and Anon Key
    private static final String SUPABASE_URL = "https://your-project-id.supabase.co";
    private static final String SUPABASE_ANON_KEY = "your-supabase-anon-key";

    private RecyclerView recyclerViewChat;
    private EditText etMessage;
    private ImageButton btnSend;
    private LinearLayout layoutThinking;

    private List<ChatMessage> messageList;
    private ChatAdapter chatAdapter;
    private JSONArray conversationHistory;

    private OkHttpClient httpClient;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        layoutThinking = findViewById(R.id.layoutThinking);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        conversationHistory = new JSONArray();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewChat.setLayoutManager(layoutManager);
        recyclerViewChat.setAdapter(chatAdapter);

        httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        mainHandler = new Handler(Looper.getMainLooper());

        // Add welcome message from CycloneSafe AI Assistant
        addAssistantMessage("Hello! I am your CycloneSafe AI Assistant. How can I help you prepare for or stay safe during a cyclone today?");

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        if (SUPABASE_URL.contains("your-project-id") || SUPABASE_ANON_KEY.contains("your-supabase-anon-key")) {
            addUserMessage(messageText);
            etMessage.setText("");
            addAssistantMessage("Please configure your SUPABASE_URL and SUPABASE_ANON_KEY in MainActivity.java with your actual Supabase project credentials.");
            return;
        }

        // Add user message to UI
        addUserMessage(messageText);
        etMessage.setText("");

        // Show thinking animation
        showThinking(true);

        try {
            // Append user message to conversation history for context
            JSONObject userObj = new JSONObject();
            userObj.put("role", "user");
            userObj.put("content", messageText);
            conversationHistory.put(userObj);

            // Build JSON payload for Supabase Edge Function
            JSONObject payload = new JSONObject();
            payload.put("message", messageText);
            payload.put("history", conversationHistory);

            RequestBody body = RequestBody.create(
                    payload.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            String baseUrl = SUPABASE_URL.replaceAll("/+$", "");
            Request request = new Request.Builder()
                    .url(baseUrl + "/functions/v1/cyclone-ai-chat")
                    .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    mainHandler.post(() -> {
                        showThinking(false);
                        addAssistantMessage("Network error: Unable to connect to CycloneSafe backend. Please check your internet connection.");
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            // Support both 'response' and 'reply' keys from edge function
                            String reply = jsonResponse.optString("response", jsonResponse.optString("reply", "No response received."));

                            // Append model response to conversation history
                            JSONObject modelObj = new JSONObject();
                            modelObj.put("role", "model");
                            modelObj.put("content", reply);
                            conversationHistory.put(modelObj);

                            mainHandler.post(() -> {
                                showThinking(false);
                                addAssistantMessage(reply);
                            });
                        } catch (JSONException e) {
                            mainHandler.post(() -> {
                                showThinking(false);
                                addAssistantMessage("Error parsing AI response.");
                            });
                        }
                    } else {
                        mainHandler.post(() -> {
                            showThinking(false);
                            addAssistantMessage("Server error (" + response.code() + "): Please verify your Supabase function and Gemini API key configuration.");
                        });
                    }
                }
            });

        } catch (JSONException e) {
            showThinking(false);
            Toast.makeText(this, "Error building request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void addUserMessage(String message) {
        messageList.add(new ChatMessage(message, true));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerViewChat.scrollToPosition(messageList.size() - 1);
    }

    private void addAssistantMessage(String message) {
        messageList.add(new ChatMessage(message, false));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerViewChat.scrollToPosition(messageList.size() - 1);
    }

    private void showThinking(boolean show) {
        if (show) {
            layoutThinking.setVisibility(View.VISIBLE);
            btnSend.setEnabled(false);
            etMessage.setEnabled(false);
        } else {
            layoutThinking.setVisibility(View.GONE);
            btnSend.setEnabled(true);
            etMessage.setEnabled(true);
            etMessage.requestFocus();
        }
    }
}
