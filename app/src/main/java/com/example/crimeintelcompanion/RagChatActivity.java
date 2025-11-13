package com.example.crimeintelcompanion;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RagChatActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    TextView welcomeTextView;
    EditText messageEditText;
    ImageButton sendButton;

    List<Message> messageList;
    MessageAdapter messageAdapter;

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)  // Increased for Ollama processing
            .writeTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .build();

    // RAG service URL - defaults for emulator and device
    private static final String EMULATOR_URL = "http://10.0.2.2:8000";
    private static final String DEFAULT_DEVICE_URL = "http://192.168.1.100:8000"; // Update with your local machine's IP address
    
    private String ragServiceUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rag_chat);

        messageList = new ArrayList<>();

        recyclerView = findViewById(R.id.recycler_view);
        welcomeTextView = findViewById(R.id.welcome_text);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_btn);

        // Ensuring keyboard resizes the layout
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        // Focus on the EditText when the activity starts
        messageEditText.requestFocus();

        // Setup recycler view
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);

        // Determine RAG service URL
        ragServiceUrl = getRagServiceUrl();

        sendButton.setOnClickListener((v) -> {
            String question = messageEditText.getText().toString().trim();
            if (question.isEmpty()) {
                Toast.makeText(this, "Please enter a question", Toast.LENGTH_SHORT).show();
                return;
            }
            addToChat(question, Message.SENT_BY_ME);
            messageEditText.setText("");
            callRagAPI(question);
            welcomeTextView.setVisibility(View.GONE);
        });
    }

    private String getRagServiceUrl() {
        // Check SharedPreferences for saved URL
        SharedPreferences prefs = getSharedPreferences("RAGConfig", Context.MODE_PRIVATE);
        String savedUrl = prefs.getString("rag_service_url", null);
        
        if (savedUrl != null && !savedUrl.isEmpty()) {
            return savedUrl;
        }
        
        // Try to detect if running on emulator
        // Emulator uses 10.0.2.2 to access host machine's localhost
        boolean isEmulator = android.os.Build.FINGERPRINT.startsWith("generic")
                || android.os.Build.FINGERPRINT.startsWith("unknown")
                || android.os.Build.MODEL.contains("google_sdk")
                || android.os.Build.MODEL.contains("Emulator")
                || android.os.Build.MODEL.contains("Android SDK built for x86")
                || android.os.Build.MANUFACTURER.contains("Genymotion")
                || (android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(android.os.Build.PRODUCT);
        
        if (isEmulator) {
            return EMULATOR_URL;
        }
        
        // For physical devices, use the configured device URL
        // Update DEFAULT_DEVICE_URL with your local machine's IP address
        // To find your IP: On Mac/Linux run 'ifconfig', on Windows run 'ipconfig'
        return DEFAULT_DEVICE_URL;
    }

    void addToChat(String message, String sentBy) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Message(message, sentBy));
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
            }
        });
    }

    void addResponse(String response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Remove "Typing..." message if present
                if (!messageList.isEmpty() && 
                    messageList.get(messageList.size() - 1).getSentBy().equals(Message.SENT_BY_BOT) &&
                    messageList.get(messageList.size() - 1).getMessage().equals("Typing...")) {
                    messageList.remove(messageList.size() - 1);
                }
                addToChat(response, Message.SENT_BY_BOT);
            }
        });
    }

    void callRagAPI(String question) {
        // Check network connectivity
        if (!isNetworkAvailable()) {
            addResponse("No network connection. Please ensure you're connected to the local network where the RAG service is running.");
            return;
        }

        // Show typing indicator
        messageList.add(new Message("Typing...", Message.SENT_BY_BOT));
        messageAdapter.notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);

        // Build request
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("query", question);
        } catch (JSONException e) {
            addResponse("Error creating request: " + e.getMessage());
            return;
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        String url = ragServiceUrl + "/ask";
        
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                String errorMsg = "Failed to connect to RAG service at " + ragServiceUrl + 
                                 ". Error: " + e.getMessage() + 
                                 "\n\nPlease ensure:\n" +
                                 "1. The RAG service is running on your local machine\n" +
                                 "2. Your device is on the same network\n" +
                                 "3. The IP address is correct";
                addResponse(errorMsg);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String result = jsonObject.getString("response");
                        addResponse(result.trim());
                    } catch (JSONException e) {
                        addResponse("Failed to parse response: " + e.getMessage());
                    }
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    try {
                        JSONObject errorJson = new JSONObject(errorBody);
                        String errorMessage = errorJson.optString("detail", errorBody);
                        addResponse("Error from RAG service: " + errorMessage);
                    } catch (JSONException e) {
                        addResponse("Error: " + errorBody);
                    }
                }
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

