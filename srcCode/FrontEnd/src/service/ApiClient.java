package service;

import model.Device;
import model.MeasurementHistory;
import model.User;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiClient {

    private static ApiClient instance;
    private static final String BASE_URL = "http://localhost:8080";
    
    // Store authentication info
    private String accessToken;
    private String role;
    private Integer userId;

    // Constructor private để đảm bảo Singleton
    private ApiClient() {}

    public static ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    // --- Phương thức Đăng nhập (Kết nối API thực) ---
    public String login(String username, String password) throws Exception {
        // Thực hiện API POST /api/auth/login
        String url = BASE_URL + "/api/auth/login";
        
        // Tạo request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("username", username);
        requestBody.put("password", password);
        
        // Gửi HTTP POST request
        String responseBody = sendHttpRequest("POST", url, requestBody.toString());
        
        // Parse response JSON
        JSONObject responseJson = new JSONObject(responseBody);
        
        if (responseJson.getBoolean("success")) {
            JSONObject data = responseJson.getJSONObject("data");
            this.accessToken = data.getString("accessToken");
            this.role = data.getString("role");
            this.userId = data.getInt("userId");
            
            return this.role; // Trả về vai trò (Role)
        } else {
            throw new Exception(responseJson.getString("message"));
        }
    }
    
    // --- Phương thức gửi HTTP request ---
    private String sendHttpRequest(String method, String urlString, String requestBody) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        // Cấu hình request
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/json");
        
        // Gửi request body nếu có
        if (requestBody != null) {
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }
        
        // Đọc response
        int statusCode = connection.getResponseCode();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        statusCode >= 400 ? connection.getErrorStream() : connection.getInputStream(),
                        StandardCharsets.UTF_8
                )
        );
        
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        
        if (statusCode >= 400) {
            throw new Exception("HTTP Error " + statusCode + ": " + response.toString());
        }
        
        return response.toString();
    }
    
    // --- Getters cho token và user info ---
    public String getAccessToken() {
        return accessToken;
    }
    
    public String getRole() {
        return role;
    }
    
    public Integer getUserId() {
        return userId;
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