package com.example.androidchatapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ChatActivity extends AppCompatActivity implements TextWatcher {
    private String name;
    private WebSocket webSocket;
    private String SERVER_PATH = "ws://10.0.2.2:3000";
    private EditText etMessage;
    private View btnSend;
    private MessageAdapter messageAdapter;
    private RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        name = getIntent().getStringExtra("name");
        initSocketConnection();


    }


    private void initSocketConnection() {

        OkHttpClient client = new OkHttpClient();
      //  String SERVER_PATH = "ws://echo.websocket.ord";
        Request request = new Request.Builder().url(SERVER_PATH).build();


       // Request request = new Request.Builder().url("ws://192.168.1.1:3000").build();
        webSocket = client.newWebSocket(request, new SocketListener());



    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String st = s.toString().trim();

        if (st.isEmpty()) {
            resetEtMessage();
           // btnSend.setVisibility(View.INVISIBLE);
        } else {
            btnSend.setVisibility(View.VISIBLE);

        }


    }

    private void resetEtMessage() {

        etMessage.removeTextChangedListener(this);
        etMessage.setText("");
        btnSend.setVisibility(View.INVISIBLE);
        etMessage.addTextChangedListener(this);
    }

    private class SocketListener extends WebSocketListener {
        @Override
        public void onOpen( WebSocket webSocket,Response response) {
            super.onOpen(webSocket, response);
            runOnUiThread(() -> {
                Toast.makeText(ChatActivity.this,
                        "Socket Connection Successfull", Toast.LENGTH_LONG).show();

                initView();
            });
        }

        @Override
        public void onMessage( WebSocket webSocket,  String text) {
            super.onMessage(webSocket, text);

            runOnUiThread(() -> {

               // RecyclerView recyclerView = findViewById(R.id.reclycleView);

                messageAdapter = new MessageAdapter(getLayoutInflater());
                recyclerView.setAdapter(messageAdapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(ChatActivity.this));




                try {
                    JSONObject jsonObject = new JSONObject(text);
                    jsonObject.put("isSent", false);

                    messageAdapter.addItem(jsonObject);

                    recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() + 1 );

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            });
        }
    }

  //  @SuppressLint("WrongViewCast")
    private void initView() {
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnsend);
        recyclerView = findViewById(R.id.reclycleView);

        messageAdapter = new MessageAdapter(getLayoutInflater());
        recyclerView.setAdapter(messageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        etMessage.addTextChangedListener(this);
        btnSend.setOnClickListener(v -> {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("name", name);
                jsonObject.put("message", etMessage.getText().toString());
                webSocket.send(jsonObject.toString());
                jsonObject.put("isSent", true);

                messageAdapter.addItem(jsonObject);
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() + 1);

                resetEtMessage();

            } catch (JSONException e) {
                e.printStackTrace();
            }

        });


    }
}