package service;

import model.Device; // Đảm bảo đã import model Device

import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.io.*;
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

    // --- THÊM: Biến Output Stream toàn cục để sửa lỗi 10053 ---
    private OutputStream os;

    // --- THÊM: Biến lưu trữ đối tượng Device để hiển thị lại UI ---
    private Device currentDevice;

    private Thread listeningThread;
    private volatile boolean isConnected = false;

    // Các biến cũ (có thể giữ lại hoặc dùng getter từ currentDevice)
    private String currentDeviceName = null;
    private String currentMacAddress = null;

    // Biến cho việc Quét (Scan)
    private final Object scanLock = new Object();
    private List<String> foundMacs = new ArrayList<>();

    // Danh sách các listener
    private final List<BluetoothDataListener> dataListeners = new CopyOnWriteArrayList<>();

    private BluetoothClientScanner() {}

    public static synchronized BluetoothClientScanner getInstance() {
        if (instance == null) {
            instance = new BluetoothClientScanner();
        }
        return instance;
    }

    // --- HÀM SCAN (GIỮ NGUYÊN) ---
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
            scanLock.wait();
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

    @Override public void inquiryCompleted(int discType) { synchronized (scanLock) { scanLock.notify(); } }
    @Override public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {}
    @Override public void serviceSearchCompleted(int transID, int respCode) {}

    public void connect(String macAddress, String deviceName) throws Exception {
        if (isConnected) {
            throw new Exception("Đang có kết nối khác. Hãy ngắt kết nối trước!");
        }

        String cleanMac = macAddress.replace(":", "").trim();
        String url = "btspp://" + cleanMac + ":1;authenticate=false;encrypt=false;master=false";

        try {
            System.out.println("Đang kết nối tới: " + url);
            streamConnection = (StreamConnection) Connector.open(url);

            // --- QUAN TRỌNG: Mở luồng Output (os) MỘT LẦN duy nhất tại đây ---
            os = streamConnection.openOutputStream();
            reader = new BufferedReader(new InputStreamReader(streamConnection.openInputStream()));

            // Cập nhật trạng thái
            this.isConnected = true;
            this.currentMacAddress = macAddress;
            this.currentDeviceName = deviceName;

            // --- TẠO OBJECT DEVICE ĐỂ LƯU SESSION CHO UI ---
            // Giúp ConnectionPanel khôi phục hiển thị khi chuyển tab
            this.currentDevice = new Device();
            this.currentDevice.setDeviceId(macAddress); // Hoặc setMacAddress tùy model
            this.currentDevice.setName(deviceName);
            this.currentDevice.setStatus("Connected");

            // Bắt đầu lắng nghe dữ liệu
            listeningThread = new Thread(this::listenDataLoop);
            listeningThread.start();

        } catch (IOException e) {
            disconnect(); // Dọn dẹp nếu lỗi
            throw new Exception("Kết nối thất bại: " + e.getMessage());
        }
    }

    // --- HÀM DISCONNECT (ĐÃ SỬA ĐỂ ĐÓNG OS) ---
    public void disconnect() {
        isConnected = false;
        currentMacAddress = null;
        currentDeviceName = null;
        currentDevice = null; // Xóa session thiết bị

        try {
            // Đóng Output Stream
            if (os != null) {
                os.close();
                os = null;
            }
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

    public void sendData(String data) {
        // Kiểm tra biến os (đã được mở ở hàm connect)
        if (isConnected && os != null) {
            try {
                os.write((data + "\n").getBytes()); // Thêm \n để Arduino nhận diện kết thúc chuỗi nhanh hơn
                os.flush();
                System.out.println("Đã gửi xuống thiết bị: " + data);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Lỗi gửi dữ liệu (Socket có thể đã đóng): " + e.getMessage());
                disconnect(); // Tự động ngắt kết nối nếu gửi lỗi
            }
        } else {
            System.err.println("Không thể gửi: Chưa kết nối hoặc luồng ghi chưa sẵn sàng.");
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public String getCurrentDeviceName() {
        return currentDeviceName;
    }

    // Getter lấy object Device cho UI restore session
    public Device getCurrentDevice() {
        return currentDevice;
    }

    private void listenDataLoop() {
        String line;
        try {
            while (isConnected && reader != null && (line = reader.readLine()) != null) {
                System.out.println(">>> [SERVICE] Nhận thô từ ESP32: " + line);
                notifyListeners(line);
            }
        } catch (IOException e) {
            if (isConnected) {
                System.err.println("Mất kết nối đột ngột.");
                disconnect();
            }
        }
    }

    // Interface
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