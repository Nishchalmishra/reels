package arivihan.technologies.doubtbuzzter2.model;

import java.util.ArrayList;

import lombok.Data;

@Data
public class PhoneDevicesDto {
    private ArrayList<DeviceDetails> devices = new ArrayList<>();

    public void addDevice(DeviceDetails device) {
        devices.add(device);
    }


    @Data
    public static class DeviceDetails {
        private String deviceId;
        private String manufacturer;
        private String model;
    }
}
