package arivihan.technologies.doubtbuzzter2.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.listener.DeviceSelectListener;
import arivihan.technologies.doubtbuzzter2.model.PhoneDevicesDto;

public class DeviceThresholdDialogDeviceListAdapter extends RecyclerView.Adapter<DeviceThresholdDialogDeviceListAdapter.DeviceViewHolder> {

    private ArrayList<PhoneDevicesDto.DeviceDetails> devices;
    private DeviceSelectListener deviceSelectListener = null;
    private ArrayList<String> selectedDevices = new ArrayList<>();

    public DeviceThresholdDialogDeviceListAdapter(ArrayList<PhoneDevicesDto.DeviceDetails> devices) {
        this.devices = devices;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.device_threshold_dialog_recycler_view_item, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        holder.deviceName.setText(devices.get(position).getManufacturer() + " (" + devices.get(position).getModel() + ")");

        holder.deviceItem.setOnClickListener(V -> {
            if (selectedDevices.contains(devices.get(position).getDeviceId())) {
                selectedDevices.remove(devices.get(position).getDeviceId());
                holder.deviceItem.setCardBackgroundColor(holder.itemView.getResources().getColor(R.color.white));
            } else {
                holder.deviceItem.setCardBackgroundColor(holder.itemView.getResources().getColor(R.color.grey_300));
                selectedDevices.add(devices.get(position).getDeviceId());
            }
            deviceSelectListener.setSelectedDevice(selectedDevices);
        });
    }

    public void onDeviceSelected(DeviceSelectListener deviceSelectListener) {
        this.deviceSelectListener = deviceSelectListener;
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    protected static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName;
        ImageView deleteDeviceBtn;
        CardView deviceItem;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.device_name);
            deleteDeviceBtn = itemView.findViewById(R.id.delete_btn);
            deviceItem = itemView.findViewById(R.id.device_item);
        }
    }
}
