package com.example.licenta30;

import static java.lang.System.in;

import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class History extends AppCompatActivity implements DeviceServiceCallback {
    RecyclerView history;
    HistoryAdappter adapter;
    Button sendMessage, connectButton;
    DeviceService deviceService;
    MessageNotificationService messageService;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);
        setBackToolBar();

        sendMessage = (Button) findViewById(R.id.unlock);
        connectButton = (Button) findViewById(R.id.connect);
        history = (RecyclerView) findViewById(R.id.history);
        messageService = MessageNotificationService.getInstance(getApplicationContext());
        deviceService = DeviceService.getInstance(getApplicationContext(), messageService, this, null);

        setHistiryList();
        setAdapterOptions();
        sendMessageMethod();
        connectMethod();
    }
    private void setEnableUnlockButton(Boolean enableLock) {
        sendMessage.setEnabled(enableLock);
    }

    private void connectMethod(){
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MainActivity", "Connecting to device: " + deviceService.getDeviceAddress());
                Boolean enableLock = deviceService.connectToDevice();
                setEnableUnlockButton(enableLock);
            }
        });
    }
    private void sendMessageMethod() {
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "Open";
                deviceService.sendMessage(message);
            }
        });
    }
    private void setBackToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setAdapterOptions() {
        String doorName = getIntent().getStringExtra("deviceName");
        FirebaseRecyclerOptions<OpeningModel> options =
                new FirebaseRecyclerOptions.Builder<OpeningModel>()
                        .setQuery(FirebaseDatabase.getInstance().getReference()
                                .child("openings").orderByChild("door").startAt(doorName)
                                .endAt(doorName + "~"), OpeningModel.class)
                        .build();
        adapter = new HistoryAdappter(options);
        history.setAdapter(adapter);
    }
    private void setHistiryList(){
        history.setLayoutManager(new LinearLayoutManager(this));
    }
    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void showToast(String message) {}

    @Override
    public void updatePairedDevicesList(String[] devices) {}
}
