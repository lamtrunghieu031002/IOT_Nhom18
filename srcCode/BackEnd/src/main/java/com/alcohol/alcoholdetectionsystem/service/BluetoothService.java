package com.alcohol.alcoholdetectionsystem.service;

import org.springframework.stereotype.Service;

import javax.bluetooth.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class BluetoothService implements DiscoveryListener {

    private final Object inquiryLock = new Object();
    private List<String> scannedMacAddresses = new ArrayList<>(); // List để chứa kết quả

    /**
     * Hàm này thay thế cho quy trình startScan() cũ.
     * Nó sẽ block (chờ) khoảng 10s cho đến khi quét xong.
     */
    public List<String> scanDevices() {
        scannedMacAddresses.clear(); // Xóa kết quả cũ
        try {
            LocalDevice localDevice = LocalDevice.getLocalDevice();
            DiscoveryAgent agent = localDevice.getDiscoveryAgent();

            // Bắt đầu quét
            agent.startInquiry(DiscoveryAgent.GIAC, this);

            // Chờ quét xong (giữ nguyên logic synchronized wait của bạn)
            synchronized (inquiryLock) {
                inquiryLock.wait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi Bluetooth: " + e.getMessage());
        }
        return scannedMacAddresses; // Trả về danh sách MAC
    }

    // --- CÁC CALLBACK CỦA BLUETOOTH (Sửa lại để add vào List) ---

    @Override
    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
        String mac = btDevice.getBluetoothAddress();

        // Bạn có thể lọc theo tên ở đây nếu muốn
         try {
            String name = btDevice.getFriendlyName(false);
            if (name != null && name.contains("ESP32")) {
                scannedMacAddresses.add(mac);
            }
         } catch (IOException e) {}
    }

    @Override
    public void inquiryCompleted(int discType) {
        // Báo hiệu đã quét xong để hàm scanDevices() chạy tiếp
        synchronized (inquiryLock) {
            inquiryLock.notify();
        }
    }

    @Override public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {}
    @Override public void serviceSearchCompleted(int transID, int respCode) {}
}