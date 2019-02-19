package com.contenidofoo1.pruebable;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class LeDeviceListAdapter extends RecyclerView.Adapter<LeDeviceListAdapter.ViewHolder> {
    public interface OnItemClickListener{
        void onItemClickListener(BluetoothDevice device);
    }

    private ArrayList<BluetoothDevice> mLeDevices;
    private OnItemClickListener listener;

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView nombre;
        public TextView direccion;
        public ViewHolder(View v) {
            super(v);
            nombre = v.findViewById(R.id.nombre);
            direccion = v.findViewById(R.id.direccion);
        }

        public void bind(final BluetoothDevice device, final OnItemClickListener listener){
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClickListener(device);
                }
            });
        }
    }

    public LeDeviceListAdapter(OnItemClickListener listener){
        super();
        mLeDevices = new ArrayList<>();
        this.listener = listener;
    }

    public void addDevice(BluetoothDevice device){
        if(!mLeDevices.contains(device)){
            mLeDevices.add(device);
        }
    }

    public void cleanDevices(){
        mLeDevices.clear();
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
        viewHolder.bind(mLeDevices.get(i), listener);
    }

    @Override
    public int getItemCount() {
        return mLeDevices.size();
    }

}
