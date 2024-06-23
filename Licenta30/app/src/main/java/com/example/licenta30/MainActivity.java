package com.example.licenta30;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MainActivity extends ComponentActivity implements DeviceServiceCallback {
    Button bluetoothOn, bluetoothOff, showPaierdDevices, logOut;
    BluetoothAdapter myBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 2;

    ListView devices;
    ArrayList<String> pairedDevicesList = new ArrayList<String>();
    ArrayAdapter<String> pairedDevicesAdapter;
    Intent enableBluetooth;
    MessageNotificationService messageService;
    DeviceService deviceService;
    FirebaseAuth mAuth;
    FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        bluetoothOff = (Button) findViewById(R.id.bluetoothOff);
        bluetoothOn = (Button) findViewById(R.id.bluetoothOn);
        showPaierdDevices = (Button) findViewById(R.id.show);
        logOut = (Button) findViewById(R.id.logOut);
        devices = (ListView) findViewById(R.id.devices);
        pairedDevicesAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, pairedDevicesList);
        devices.setAdapter(pairedDevicesAdapter);
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        checkPermissions();

        setEnableShowButton();
        verifyUserExists();

        IntentFilter discoverDevices = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoverReciver, discoverDevices);


        deviceService.registerReceiver(discoverReciver, discoverDevices);

        bluetoothOnMethod();
        bluetoothOffMethod();
        showPaierdDevices();
        logOutMethod();

        devices.setOnItemClickListener((parent, view, position, id) -> {
            String item = (String) parent.getItemAtPosition(position);
            Log.d("MainActivity", "Selected item: " + item);
            String deviceAddress = deviceService.extractAddress(item);
            if (deviceAddress != null) {
                deviceService.setDevice(deviceAddress);
                BluetoothDevice device = myBluetoothAdapter.getRemoteDevice(deviceAddress);
                startHistoryPage(device.getName());
            } else {
                Log.d("MainActivity", "Failed to extract Bluetooth address from: " + item);
                Toast.makeText(getApplicationContext(), "Failed to extract Bluetooth address", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
            }, REQUEST_BLUETOOTH_PERMISSIONS);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void setEnableShowButton() {
        showPaierdDevices.setEnabled(myBluetoothAdapter.isEnabled());
    }

    BroadcastReceiver discoverReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    String deviceInfo = device.getName() + "\n" + device.getAddress();
                    Log.d("MainActivity", "Discovered device: " + deviceInfo);
                    //disciverableDevices.add(device.getName() + "\n" + device.getAddress());
                    //descoverableDevacesAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    private void verifyUserExists() {
        if (user == null) {
            startLogInPage();
        }else{
            messageService = new MessageNotificationService(getApplicationContext());
            deviceService = new DeviceService(getApplicationContext(), messageService, this, user);
        }
    }

    private void startLogInPage() {
        Intent intent = new Intent(getApplicationContext(), LogIn.class);
        startActivity(intent);
        finish();
    }
    private void startHistoryPage(String deviceName) {
        Intent intent = new Intent(getApplicationContext(), History.class);
        intent.putExtra("deviceName", deviceName);
        startActivity(intent);
    }

    private void logOutMethod() {
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                UserInfo.setInstance(null);
                startLogInPage();
            }
        });
    }

    private void showPaierdDevices() {
        showPaierdDevices.setOnClickListener(v -> {
            deviceService.showPairedDevices();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (requestCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Bluetooth is enabled", Toast.LENGTH_LONG).show();
            } else {
                if (requestCode == RESULT_CANCELED) {
                    Toast.makeText(getApplicationContext(), "Bluetooth enableing canceled", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void bluetoothOnMethod() {
        bluetoothOn.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Verificam daca dispozitivul suporta conexiune bluetooth
                if (myBluetoothAdapter == null) {
                    //daca nu e suportat va aparea un toast
                    Toast.makeText(getApplicationContext(), "Bluetooth is not supported on this device", Toast.LENGTH_LONG).show();
                } else {
                    if (!myBluetoothAdapter.isEnabled()) {
                        //daca nu este enabled
                        enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        enableBluetooth.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 10);
                        startActivityForResult(enableBluetooth, REQUEST_ENABLE_BT);
                        showPaierdDevices.setEnabled(true);
                    }
                }
            }
        }));
    }

    private void bluetoothOffMethod() {
        bluetoothOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myBluetoothAdapter.isEnabled() &&
                    ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        myBluetoothAdapter.disable();
                        devices.setAdapter(pairedDevicesAdapter);
                        //setEnableUnlockButton(false);
                        pairedDevicesAdapter.clear();
                        pairedDevicesAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(discoverReciver);
        deviceService.unregisterReceiver(discoverReciver);
    }

    @Override
    public void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void updatePairedDevicesList(String[] devices) {
        runOnUiThread(() -> {
            pairedDevicesAdapter.clear();
            pairedDevicesAdapter.addAll(devices);
            pairedDevicesAdapter.notifyDataSetChanged();
        });
    }
}