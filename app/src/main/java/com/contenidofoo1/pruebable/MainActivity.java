package com.contenidofoo1.pruebable;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
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



public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000;
    private static final String TAG = "Debug";
    private static final int REQUEST_FINE_LOCATION = 2;
    BluetoothAdapter bluetoothAdapter;
    Button startScanButton;
    TextView mTextView;
    private Handler handler = new Handler();
    private boolean mScanning;
    private RecyclerView recyclerView;
    private LeDeviceListAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

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

        adapter = new LeDeviceListAdapter();
        recyclerView.setAdapter(adapter);

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if(bluetoothAdapter == null || !bluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        startScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextView.setText("Escaneando Dispositivos!");
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mScanning = false;
                        bluetoothAdapter.stopLeScan(leScanCallback);
                        mTextView.setText("Escaneo finalizado en 10s");
                    }
                }, SCAN_PERIOD);

                mScanning = true;
                bluetoothAdapter.startLeScan(leScanCallback);
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

    public class LeDeviceListAdapter extends RecyclerView.Adapter<LeDeviceListAdapter.ViewHolder> {
        private ArrayList<BluetoothDevice> mLeDevices;

        public class ViewHolder extends RecyclerView.ViewHolder{
            public TextView nombre;
            public TextView direccion;
            public ViewHolder(View v) {
                super(v);
                nombre = v.findViewById(R.id.nombre);
                direccion = v.findViewById(R.id.direccion);
            }
        }

        public LeDeviceListAdapter(){
            super();
            mLeDevices = new ArrayList<>();
        }

        public void addDevice(BluetoothDevice device){
            if(!mLeDevices.contains(device)){
                mLeDevices.add(device);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.my_card,viewGroup, false);
            return  new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            viewHolder.nombre.setText(mLeDevices.get(i).getName());
            viewHolder.direccion.setText(mLeDevices.get(i).getAddress());
        }

        @Override
        public int getItemCount() {
            return mLeDevices.size();
        }

    }

}
