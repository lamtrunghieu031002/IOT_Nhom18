package service;

import model.Device;
import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BluetoothClientScanner implements DiscoveryListener {

    private static BluetoothClientScanner instance;

    private StreamConnection streamConnection;
    private BufferedReader reader;
    private OutputStream os;

    private Device currentDevice;
    private Thread listeningThread;

    // Dùng volatile để đảm bảo tính nhất quán giữa các luồng
    private volatile boolean isConnected = false;

    // Biến cờ để phân biệt Ngắt chủ động hay Bị rớt mạng
    private volatile boolean isDisconnecting = false;

    private String currentDeviceName = null;
    private String currentMacAddress = null;

    private final Object scanLock = new Object();
    private List<String> foundMacs = new ArrayList<>();
    private final List<BluetoothDataListener> dataListeners = new CopyOnWriteArrayList<>();

    private BluetoothClientScanner() {}

    public static synchronized BluetoothClientScanner getInstance() {
        if (instance == null) {
            instance = new BluetoothClientScanner();
        }
        return instance;
    }

    // --- SCAN GIỮ NGUYÊN ---
    public List<String> scan() throws Exception {
        if (isConnected) {
            throw new Exception("Đang kết nối với thiết bị " + currentDeviceName + ". Vui lòng ngắt kết nối trước khi quét.");
        }
        foundMacs.clear();
        LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, this);
        synchronized (scanLock) { scanLock.wait(); }
        return foundMacs;
    }

    @Override public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
        String mac = btDevice.getBluetoothAddress();
        if (!foundMacs.contains(mac)) foundMacs.add(mac);
    }
    @Override public void inquiryCompleted(int discType) { synchronized (scanLock) { scanLock.notify(); } }
    @Override public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {}
    @Override public void serviceSearchCompleted(int transID, int respCode) {}

    // --- CONNECT ---
    public void connect(String macAddress, String deviceName) throws Exception {
        if (isConnected) {
            throw new Exception("Đang có kết nối khác. Hãy ngắt kết nối trước!");
        }

        String cleanMac = macAddress.replace(":", "").trim();
        String url = "btspp://" + cleanMac + ":1;authenticate=false;encrypt=false;master=false";

        try {
            System.out.println("Connecting to: " + url);
            streamConnection = (StreamConnection) Connector.open(url);

            // Mở stream
            os = streamConnection.openOutputStream();
            reader = new BufferedReader(new InputStreamReader(streamConnection.openInputStream()));

            this.isConnected = true;
            this.isDisconnecting = false; // Reset cờ
            this.currentMacAddress = macAddress;
            this.currentDeviceName = deviceName;

            this.currentDevice = new Device();
            this.currentDevice.setDeviceId(macAddress);
            this.currentDevice.setName(deviceName);
            this.currentDevice.setStatus("Connected");

            listeningThread = new Thread(this::listenDataLoop);
            listeningThread.start();

        } catch (IOException e) {
            disconnect();
            throw new Exception("Kết nối thất bại: " + e.getMessage());
        }
    }

    // --- DISCONNECT (ĐÃ SỬA ĐỂ CHỐNG TREO UI) ---
    public void disconnect() {
        // Nếu đã ngắt rồi thì thôi, tránh lặp
        if (!isConnected && !isDisconnecting) return;

        System.out.println("Đang thực hiện ngắt kết nối...");
        isDisconnecting = true; // Đánh dấu là người dùng chủ động ngắt
        isConnected = false;    // Đánh dấu trạng thái logic

        // QUAN TRỌNG: Chạy việc đóng kết nối trong Thread riêng
        // Lý do: stream.close() có thể bị block nếu luồng đọc đang kẹt
        new Thread(() -> {
            try {
                // 1. Ngắt luồng đọc trước (nếu nó đang ngủ)
                if (listeningThread != null) {
                    listeningThread.interrupt();
                }

                // 2. Đóng Stream Connection (Cái gốc) -> Cái này sẽ khiến readLine() bên kia bung Exception ngay
                if (streamConnection != null) {
                    streamConnection.close();
                }

                // 3. Đóng các thành phần phụ
                if (os != null) os.close();
                if (reader != null) reader.close();

            } catch (IOException e) {
                System.err.println("Lỗi khi đóng kết nối (Không quan trọng): " + e.getMessage());
            } finally {
                // Dọn dẹp biến
                streamConnection = null;
                os = null;
                reader = null;
                currentDevice = null;
                currentDeviceName = null;
                currentMacAddress = null;
                isDisconnecting = false;
                System.out.println("Đã ngắt kết nối Bluetooth hoàn toàn.");
            }
        }).start();
    }

    public void sendData(String data) {
        if (isConnected && os != null) {
            try {
                os.write((data + "\n").getBytes());
                os.flush();
                System.out.println("Đã gửi: " + data);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Lỗi gửi dữ liệu: " + e.getMessage());
                // Nếu lỗi gửi -> Tự động ngắt (coi như mất kết nối)
                if (!isDisconnecting) disconnect();
            }
        }
    }

    // --- LOOP LẮNG NGHE (ĐÃ SỬA) ---
    private void listenDataLoop() {
        String line;
        try {
            // Đọc liên tục
            while (isConnected && reader != null) {
                // readLine() sẽ block ở đây
                line = reader.readLine();

                if (line != null) {
                    System.out.println("ESP32 >> " + line);
                    notifyListeners(line);
                } else {
                    // line null nghĩa là stream đã đóng từ phía kia
                    break;
                }
            }
        } catch (IOException e) {
            // Khi disconnect() gọi stream.close(), exception sẽ ném ra ở đây
            if (isDisconnecting) {
                System.out.println("Kết nối đã đóng chủ động bởi người dùng.");
            } else {
                System.err.println("Mất kết nối đột ngột với thiết bị: " + e.getMessage());
                // Chỉ gọi disconnect() dọn dẹp nếu đây là lỗi rớt mạng thật
                disconnect();
            }
        }
    }

    // Getters & Listeners
    public boolean isConnected() { return isConnected; }
    public String getCurrentDeviceName() { return currentDeviceName; }
    public String getCurrentDeviceAddress() { return currentMacAddress; } // Thêm getter này cho UI dùng
    public Device getCurrentDevice() { return currentDevice; }

    public interface BluetoothDataListener {
        void onDataReceived(String data);
    }
    public void addDataListener(BluetoothDataListener listener) { dataListeners.add(listener); }
    public void removeDataListener(BluetoothDataListener listener) { dataListeners.remove(listener); }
    private void notifyListeners(String data) {
        for (BluetoothDataListener listener : dataListeners) {
            listener.onDataReceived(data);
        }
    }
}