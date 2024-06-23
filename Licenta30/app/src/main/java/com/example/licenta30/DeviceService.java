package com.example.licenta30;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DeviceService {
    private static DeviceService instance;
    private final Context context;
    private final BluetoothAdapter bluetoothAdapter;
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket socket = null;
    private final MessageNotificationService messageService;
    private final DeviceServiceCallback callback;
    private static String deviceAddress;
    private BluetoothDevice connectedDevice = null;


    public DeviceService(Context context, MessageNotificationService service, DeviceServiceCallback callback, FirebaseUser user) {
        this.context = context;
        this.messageService = service;
        this.callback = callback;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDevice(String device) {
        DeviceService.deviceAddress = device;
    }
    public static synchronized DeviceService getInstance(
            Context context,
            MessageNotificationService messageService,
            DeviceServiceCallback callback, FirebaseUser user){
        if (instance == null) {
            instance = new DeviceService(context, messageService, callback, user);
        }
        return instance;
    }

    public void registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        context.registerReceiver(receiver, filter);
    }

    public void unregisterReceiver(BroadcastReceiver receiver) {
        context.unregisterReceiver(receiver);
    }

    public void showPairedDevices() {
        Set<BluetoothDevice> bt = bluetoothAdapter.getBondedDevices();
        String[] strings = new String[bt.size()];
        int index = -1;
        verifyPaierdDevicesList(bt, index, strings);
    }

    private void verifyPaierdDevicesList(Set<BluetoothDevice> bt, int index, String[] strings) {
        if (bluetoothAdapter.isEnabled()) {
            if (bt.size() > 0) {
                extractBluetoothDevices(bt, index, strings);
                int max = strings.length - 2; // There was always an empty row at the end for some reason;
                callback.updatePairedDevicesList(java.util.Arrays.copyOfRange(strings, 0, max));
            } else
                Toast.makeText(context, "No paired devices found!", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(context, "Bluetooth must be enabled!", Toast.LENGTH_SHORT).show();
    }

    private void extractBluetoothDevices(Set<BluetoothDevice> bt, int index, String[] strings) {
        for (BluetoothDevice device : bt) {
            if (getDeviceType(device.getBluetoothClass()).equals("Uncategorized/Other")) {
                index++;
                strings[index] = device.getName() + "\n" + device.getAddress();
                Log.d("DeviceService", "Paired device: " + strings[index]);
            }
        }
    }

    public boolean connectToDevice() {
        if (bluetoothAdapter.isEnabled()) {
            connectedDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);
            try {
                socket = connectedDevice.createRfcommSocketToServiceRecord(MY_UUID);
                socket.connect();
                Toast.makeText(context, "Connected to " + connectedDevice.getName(), Toast.LENGTH_SHORT).show();
                receiveMessage();
                return true;

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "Connection failed", Toast.LENGTH_SHORT).show();
                try {
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException closeException) {
                    closeException.printStackTrace();
                }
            }
        } else Toast.makeText(context, "Bluetooth must be enabled!", Toast.LENGTH_SHORT).show();
        return false;
    }

    public void sendMessage(String message) {
        if (socket != null && socket.isConnected()) {
            try {
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(message.getBytes());
                outputStream.flush();
                Toast.makeText(context, "Message sent: " + message, Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "Failed to send message", Toast.LENGTH_SHORT).show();
            }
        } else Toast.makeText(context, "You must connect to device first!", Toast.LENGTH_SHORT).show();
    }

    private void receiveMessage() {
        Log.e("DeviceService", "Entering receiveMessage function");
        if (socket != null && socket.isConnected()) {
            new Thread(() -> {
                try {
                    handleMessages();
                } catch (IOException e) {
                    e.printStackTrace();
                    callback.showToast("Failed to receive message");
                }
            }).start();
        } else Toast.makeText(context, "Not connected to any device", Toast.LENGTH_SHORT).show();
    }

    private void handleMessages() throws IOException {
        InputStream inputStream = socket.getInputStream();
        if (inputStream == null) {
            Log.e("DeviceService", "Input stream is null");
            return;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String receivedMessage;
        while ((receivedMessage = reader.readLine()) != null) {
            Log.d("DeviceService", "Received message: " + receivedMessage);
            String finalReceivedMessage = receivedMessage;
            callback.showToast("Received: " + finalReceivedMessage);
            Log.d("DeviceService","Inainte de notificare");
            dorIsOpen(finalReceivedMessage);
        }
    }

    private void dorIsOpen(String finalReceivedMessage) {
        if (finalReceivedMessage.equals("Stranger at the door")) {
            setNotificationMessage(finalReceivedMessage,"You might be in danger!");
        }
        if (finalReceivedMessage.contains("Door is unlocked!")) {
            Boolean fingerprint = false;
            if(finalReceivedMessage.contains("ID")) fingerprint = true;
            addDataToDataBase(fingerprint);
            setNotificationMessage(finalReceivedMessage,"");
        }
        if (finalReceivedMessage.equals("Door is locked!")) {
            setNotificationMessage(finalReceivedMessage,"");
        }
    }

    private void setNotificationMessage(String finalReceivedMessage, String aditionalMessage) {
        Log.d("DeviceService", "Intra in notificare");
        Message.setMessage(finalReceivedMessage);
        messageService.showNotification(aditionalMessage, finalReceivedMessage);
    }

    private void addDataToDataBase(boolean fingerPrint){
        UserInfo userInfo = UserInfo.getInstance("");
        String userEmail = verifyUser(userInfo, fingerPrint);
        OpeningModel newOpening = new OpeningModel(connectedDevice.getName(), userEmail);
        Map<String, Object> map = new HashMap<>();
        map.put("door", newOpening.getDoor());
        map.put("time_stamp", newOpening.getTime_stamp());
        map.put("user", newOpening.getUser());
        newTableLine(map);
    }

    private String verifyUser(UserInfo userInfo, boolean fingerprint) {
        if(fingerprint)return "Opened by fingerprint or button.";
        return userInfo.getEmail();
    }

    private void newTableLine(Map<String, Object> map) {
        FirebaseDatabase.getInstance().getReference().child("openings").push()
                .setValue(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("History","Data iserted succesfuly");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("History","Error while insertion");
                    }
                });
    }

    private String getDeviceType(BluetoothClass deviceClass) {
        if (deviceClass != null) {
            switch (deviceClass.getMajorDeviceClass()) {
                case BluetoothClass.Device.Major.COMPUTER:
                    return "Computer";
                case BluetoothClass.Device.Major.PHONE:
                    return "Phone";
                case BluetoothClass.Device.Major.AUDIO_VIDEO:
                    return "Audio/Video";
                case BluetoothClass.Device.Major.PERIPHERAL:
                    return "Peripheral";
                case BluetoothClass.Device.Major.IMAGING:
                    return "Imaging";
                case BluetoothClass.Device.Major.WEARABLE:
                    return "Wearable";
                case BluetoothClass.Device.Major.TOY:
                    return "Toy";
                case BluetoothClass.Device.Major.HEALTH:
                    return "Health";
                case BluetoothClass.Device.Major.UNCATEGORIZED:
                    return "Uncategorized/Other";
                default:
                    return "Unknown";
            }
        } else {
            return "Unknown";
        }
    }

    public String extractAddress(String item) {
        Log.d("DeviceService", "Extracting address from item: " + item);
        if (item != null && item.length() >= 17) {
            String address = item.substring(item.length() - 17);
            Log.d("DeviceService", "Extracted address: " + address);
            if (BluetoothAdapter.checkBluetoothAddress(address)) {
                return address ;
            } else {
                Log.d("DeviceService", "Invalid Bluetooth address: " + address);
            }
        } else {
            Log.d("DeviceService", "Item is too short to contain a valid address");
        }
        return null;
    }
}
