package com.contenidofoo1.pruebable;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {
    public static final String SERVICE_UUID = "32c64438-23c1-40e3-8a85-2dddc120e432";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000;
    private static final String TAG = "ListMainActivity";
    private static final int REQUEST_FINE_LOCATION = 2;
    BluetoothAdapter bluetoothAdapter;
    BluetoothGatt bluetoothGatt;
    Button startScanButton;
    TextView mTextView;
    private Handler handler = new Handler();
    private boolean mScanning;
    private RecyclerView recyclerView;
    private LeDeviceListAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private BtleScanCallBack mScanCallBack;
    private BluetoothLeScanner mbluetoothLeScanner;
    private Handler mHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startScanButton = findViewById(R.id.start_scan_button);
        mTextView = findViewById(R.id.letrero);

        recyclerView = findViewById(R.id.reciclador);
        recyclerView.setHasFixedSize(false);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new LeDeviceListAdapter(new LeDeviceListAdapter.OnItemClickListener() {
            @Override
            public void onItemClickListener(BluetoothDevice device) {
                Log.d(TAG, "onItemClickListener: " + device.getName());
                final Intent intent = new Intent(getBaseContext(), DeviceControlActivity.class);
                intent.putExtra("name", device.getName());
                intent.putExtra("address", device.getAddress());
                stopScan();
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();


        startScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan();
                /*mTextView.setText("Escaneando Dispositivos!");
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mScanning = false;
                        bluetoothAdapter.stopLeScan(leScanCallback);
                        mTextView.setText("Escaneo finalizado en 10s");
                    }
                }, SCAN_PERIOD);

                adapter.cleanDevices();
                adapter.notifyDataSetChanged();
                mScanning = true;
                bluetoothAdapter.startLeScan(leScanCallback);*/
            }
        });
    }

    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.d(TAG, "Device discovered!");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String name = device.getName();
                    Log.d(TAG, "run: " + name);
                    adapter.addDevice(device);
                    adapter.notifyDataSetChanged();
                }
            });
        }
    };

    private void startScan(){
        if(!hasPermissions() && mScanning){
            return;
        }
        mTextView.setText("Escaneando Dispositivos!");
        List<ScanFilter> filters = new ArrayList<>();
        ScanFilter scanFilter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(UUID.fromString(SERVICE_UUID)))
                .build();
        //filters.add(scanFilter);

        ScanSettings settings = new ScanSettings.Builder()
                                                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                                                .build();

        mScanCallBack = new BtleScanCallBack();
        mbluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        mbluetoothLeScanner.startScan(filters, settings, mScanCallBack);
        mScanning = true;
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan();
            }
        }, SCAN_PERIOD);

    }

    private boolean hasPermissions(){
        if(bluetoothAdapter == null || !bluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Log.d(TAG, "Requested user enables Bluetooth. Try starting the scan again.");
            return false;
        }else if(!hasLocationPermission()){
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
            return false;
        }
        return true;
    }

    private boolean hasLocationPermission(){
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private class BtleScanCallBack extends ScanCallback{
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            adapter.addDevice(result.getDevice());
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results){
                adapter.addDevice(result.getDevice());
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "onScanFailed: BLE Scan Failed with Code" + errorCode);
        }
    }

    private void stopScan(){
        if(mScanning && bluetoothAdapter != null && bluetoothAdapter.isEnabled() && mbluetoothLeScanner != null){
            mbluetoothLeScanner.stopScan(mScanCallBack);
            scanComplete();
        }

        mScanCallBack = null;
        mScanning = false;
        mHandler = null;
    }

    private void scanComplete(){
        mTextView.setText("Escaneo finalizado en 10s");
    }

}
