package service;

import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service quản lý Bluetooth Singleton.
 * Lưu trạng thái kết nối (Session) để dùng chung cho toàn bộ ứng dụng.
 */
public class BluetoothClientScanner implements DiscoveryListener {

    private static BluetoothClientScanner instance;

    private StreamConnection streamConnection;
    private BufferedReader reader;
    private Thread listeningThread;
    private volatile boolean isConnected = false;
    private String currentDeviceName = null;
    private String currentMacAddress = null;

    // Biến cho việc Quét (Scan)
    private final Object scanLock = new Object();
    private List<String> foundMacs = new ArrayList<>();

    // Danh sách các listener (để bắn dữ liệu sang UI)
    private final List<BluetoothDataListener> dataListeners = new CopyOnWriteArrayList<>();

    // Private Constructor
    private BluetoothClientScanner() {}

    // Singleton Pattern
    public static synchronized BluetoothClientScanner getInstance() {
        if (instance == null) {
            instance = new BluetoothClientScanner();
        }
        return instance;
    }

    public List<String> scan() throws Exception {
        if (isConnected) {
            throw new Exception("Đang kết nối với thiết bị " + currentDeviceName + ". Vui lòng ngắt kết nối trước khi quét.");
        }

        foundMacs.clear();
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        DiscoveryAgent agent = localDevice.getDiscoveryAgent();

        System.out.println("BT Service: Bắt đầu quét...");
        agent.startInquiry(DiscoveryAgent.GIAC, this);

        synchronized (scanLock) {
            scanLock.wait(); // Chờ quét xong
        }

        System.out.println("BT Service: Quét xong. Tìm thấy " + foundMacs.size());
        return foundMacs;
    }

    @Override
    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
        String mac = btDevice.getBluetoothAddress();
        if (!foundMacs.contains(mac)) {
            foundMacs.add(mac);
        }
    }

    @Override
    public void inquiryCompleted(int discType) {
        synchronized (scanLock) {
            scanLock.notify();
        }
    }

    @Override public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {}
    @Override public void serviceSearchCompleted(int transID, int respCode) {}

    public void connect(String macAddress, String deviceName) throws Exception {
        if (isConnected) {
            throw new Exception("Đang có kết nối khác. Hãy ngắt kết nối trước!");
        }

        String cleanMac = macAddress.replace(":", "").trim();
        // URL chuẩn SPP Channel 1
        String url = "btspp://" + cleanMac + ":1;authenticate=false;encrypt=false;master=false";

        try {
            System.out.println("Đang kết nối tới: " + url);
            streamConnection = (StreamConnection) Connector.open(url);
            reader = new BufferedReader(new InputStreamReader(streamConnection.openInputStream()));

            // Cập nhật Session
            this.isConnected = true;
            this.currentMacAddress = macAddress;
            this.currentDeviceName = deviceName;

            // Bắt đầu lắng nghe dữ liệu
            listeningThread = new Thread(this::listenDataLoop);
            listeningThread.start();

        } catch (IOException e) {
            disconnect(); // Dọn dẹp nếu lỗi
            throw new Exception("Kết nối thất bại: " + e.getMessage());
        }
    }

    public void disconnect() {
        isConnected = false;
        currentMacAddress = null;
        currentDeviceName = null;

        try {
            if (reader != null) reader.close();
            if (streamConnection != null) streamConnection.close();
            if (listeningThread != null && listeningThread.isAlive()) {
                listeningThread.interrupt();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            reader = null;
            streamConnection = null;
            System.out.println("Đã ngắt kết nối Bluetooth.");
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public String getCurrentDeviceName() {
        return currentDeviceName;
    }

    private void listenDataLoop() {
        String line;
        try {
            while (isConnected && reader != null && (line = reader.readLine()) != null) {
                // Nhận được dữ liệu từ ESP32 -> Bắn ra cho các UI đang lắng nghe
                notifyListeners(line);
            }
        } catch (IOException e) {
            if (isConnected) {
                System.err.println("Mất kết nối đột ngột.");
                disconnect();
            }
        }
    }

    // Interface để UI implement
    public interface BluetoothDataListener {
        void onDataReceived(String data);
    }

    public void addDataListener(BluetoothDataListener listener) {
        dataListeners.add(listener);
    }

    public void removeDataListener(BluetoothDataListener listener) {
        dataListeners.remove(listener);
    }

    private void notifyListeners(String data) {
        for (BluetoothDataListener listener : dataListeners) {
            listener.onDataReceived(data);
        }
    }
}