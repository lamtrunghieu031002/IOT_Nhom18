package service;

import javax.bluetooth.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BluetoothClientScanner implements DiscoveryListener {

    private final Object lock = new Object();
    private List<String> foundMacs = new ArrayList<>();

    // Hàm quét đồng bộ (sẽ chờ ~10s để quét xong rồi mới return)
    public List<String> scan() throws Exception {
        foundMacs.clear();

        // Lấy thiết bị local (Yêu cầu máy tính phải có Bluetooth)
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        DiscoveryAgent agent = localDevice.getDiscoveryAgent();

        System.out.println("Client: Bắt đầu quét Bluetooth...");
        agent.startInquiry(DiscoveryAgent.GIAC, this);

        // Chờ quét xong
        synchronized (lock) {
            lock.wait();
        }

        System.out.println("Client: Quét xong. Tìm thấy " + foundMacs.size() + " thiết bị.");
        return foundMacs;
    }

    @Override
    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
        String mac = btDevice.getBluetoothAddress();
        // Lọc trùng nếu cần
        if (!foundMacs.contains(mac)) {
            foundMacs.add(mac);
        }
    }

    @Override
    public void inquiryCompleted(int discType) {
        synchronized (lock) {
            lock.notify();
        }
    }

    @Override public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {}
    @Override public void serviceSearchCompleted(int transID, int respCode) {}
}