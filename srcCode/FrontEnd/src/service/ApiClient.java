package service;
import model.Device;
import model.MeasurementHistory;
import model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiClient {

    private static ApiClient instance;

    // Constructor private để đảm bảo Singleton
    private ApiClient() {}

    public static ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    // --- Phương thức Đăng nhập (Giả định) ---
    public String login(String username, String password) throws Exception {
        // Thực hiện API POST /api/auth/login
        Thread.sleep(500); // Giả lập độ trễ mạng
        if ("admin".equals(username) && "123".equals(password)) {
            return "admin"; // Trả về vai trò (Role)
        } else if ("user".equals(username) && "123".equals(password)) {
            return "user"; // Trả về vai trò (Role)
        } else {
            throw new Exception("Thông tin đăng nhập không hợp lệ.");
        }
    }

    // --- Phương thức Quản lý Thiết bị (Giả định) ---
    public List<Device> getAllDevices() throws Exception {
        // Thực hiện API GET /api/devices
        Thread.sleep(1000);
        List<Device> devices = new ArrayList<>();
        devices.add(new Device("00:1A:2B:3C:4D:5E", "AlcoMaster 1", "AM-100", "2025-11-01", "Hoạt động"));
        devices.add(new Device("AA:BB:CC:DD:EE:FF", "AlcoTester Pro", "AT-200", "2025-11-15", "Không hoạt động"));
        return devices;
    }

    public boolean deleteDevice(String macAddress) throws Exception {
        // Thực hiện API DELETE /api/devices/{macAddress}
        Thread.sleep(500);
        return true;
    }
    public boolean addUser(Map<String, String> data) throws Exception {
        // Thực hiện API POST /api/users/add hoặc tương tự
        System.out.println("API Call: POST /api/users/add with data: " + data.keySet());
        Thread.sleep(1000); // Giả lập độ trễ API
        return true;
    }
    public List<MeasurementHistory> getAllHistory() throws Exception {
        // Thực hiện API GET /api/history
        System.out.println("API Call: GET /api/history");
        Thread.sleep(1000);

        // Dữ liệu giả định
        List<MeasurementHistory> mockHistory = new ArrayList<>();
        mockHistory.add(new MeasurementHistory(1, "Nguyễn C", "12345", "HN", "2025-11-20 10:00", "AM-100", 0.25));
        mockHistory.add(new MeasurementHistory(2, "Trần D", "67890", "HCM", "2025-11-20 14:30", "AT-200", 0.00));
        mockHistory.add(new MeasurementHistory(3, "Phạm E", "11223", "ĐN", "2025-11-21 08:00", "AM-100", 0.55));
        return mockHistory;
    }
    public Map<String, Double> getAgeBasedStatistics() throws Exception {
        // Thực hiện API GET /api/statistics/age-based
        System.out.println("API Call: GET /api/statistics/age-based");
        Thread.sleep(1000);

        // Dữ liệu giả định
        Map<String, Double> mockData = new HashMap<>();
        mockData.put("18-25", 0.15);
        mockData.put("26-35", 0.30);
        mockData.put("36-45", 0.22);
        mockData.put("46+", 0.10);
        return mockData;
    }
    public boolean submitMeasurement(Map<String, Object> data) throws Exception {
        // Thực hiện API POST /api/measurement/submit
        System.out.println("API Call: POST /api/measurement/submit with data: " + data.keySet());
        Thread.sleep(1000);
        return true;
    }

    // --- Phương thức Quản lý Tài khoản (Giả định) ---
    public List<User> getAllUsers() throws Exception {
        // Thực hiện API GET /api/users
        Thread.sleep(1000);
        List<User> users = new ArrayList<>();
        users.add(new User(1, "admin", "Nguyễn Văn A", "a@example.com", "Quản lý", "2025-11-01"));
        users.add(new User(2, "user01", "Trần Thị B", "b@example.com", "Người đo", "2025-11-10"));
        return users;
    }

    public boolean deleteUser(int userId) throws Exception {
        // Thực hiện API DELETE /api/users/{userId}
        Thread.sleep(500);
        return true;
    }

    // ... Các phương thức API khác ...
}