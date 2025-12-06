package service;

import model.Device;
import model.DevicePageResponse;
import model.MeasurementHistory;
import model.User;
import org.json.JSONArray;
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
        if (accessToken != null) {
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        }
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

    // --- Phương thức Quản lý Thiết bị  ---

    public DevicePageResponse getDevicesPaging(String status, int page, int size) throws Exception {
        String url = BASE_URL + "/api/devices?status=" + status + "&page=" + page + "&size=" + size;

        String response = sendHttpRequest("GET", url, null);

        JSONObject json = new JSONObject(response);

        if (!json.getBoolean("success")) {
            throw new Exception(json.optString("message", "Lỗi tải danh sách thiết bị"));
        }

        JSONObject data = json.getJSONObject("data");
        JSONArray arr = data.getJSONArray("devices");
        List<Device> deviceList = new ArrayList<>();

        // Định dạng ngày đẹp: dd/MM/yyyy HH:mm
        java.time.format.DateTimeFormatter inputFormatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        java.time.format.DateTimeFormatter outputFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);

            String deviceId = o.getString("deviceId");
            String name = o.getString("name");
            String model = o.getString("model");
            String statusStr = o.getString("status");

            // === XỬ LÝ NGÀY TẠO ĐẸP ===
            String createdAtRaw = o.optString("createdAt", null);
            String createdAtFormatted = "Chưa xác định";

            if (createdAtRaw != null && !createdAtRaw.equals("null") && createdAtRaw.length() >= 19) {
                try {
                    // Cắt bỏ phần nano nếu có (ví dụ: .311028 → chỉ lấy đến giây)
                    String cleanIso = createdAtRaw.substring(0, 19); // 2025-11-25T08:00:45
                    java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(cleanIso);
                    createdAtFormatted = ldt.format(outputFormatter);
                } catch (Exception e) {
                    // Nếu parse lỗi → chỉ lấy ngày
                    createdAtFormatted = createdAtRaw.substring(0, 10).replace("-", "/");
                }
            }

            // Tạo Device với ngày đã được format đẹp
            Device device = new Device(deviceId, name, model, createdAtFormatted, statusStr);
            deviceList.add(device);
        }

        return new DevicePageResponse(
                deviceList,
                data.getInt("page"),
                data.getInt("size"),
                data.getInt("total"),
                data.getInt("totalPages")
        );
    }

    public boolean addDevice(String deviceId, String name, String model) throws Exception {
        String url = BASE_URL + "/api/devices/register";

        JSONObject req = new JSONObject();
        req.put("deviceId", deviceId);
        req.put("name", name);
        req.put("model", model);

        String response = sendHttpRequest("POST", url, req.toString());

        JSONObject json = new JSONObject(response);

        if (!json.getBoolean("success")) {
            throw new Exception(json.getString("message"));
        }

        return true; // chỉ cần success
    }

    public boolean deleteDevice(String deviceId) throws Exception {
        String url = BASE_URL + "/api/devices/" + deviceId;

        String response = sendHttpRequest("DELETE", url, null);

        JSONObject json = new JSONObject(response);

        if (!json.getBoolean("success")) {
            throw new Exception(json.getString("message"));
        }

        return true;
    }

//    User
    // --- THÊM TÀI KHOẢN MỚI THẬT QUA API /api/auth/register ---
    public boolean addUser(Map<String, String> data) throws Exception {
        String url = BASE_URL + "/api/auth/register";

        // Tạo request body đúng format API yêu cầu
        JSONObject requestBody = new JSONObject();
        requestBody.put("username", data.get("username"));
        requestBody.put("password", data.get("password"));
        requestBody.put("email", data.get("email"));
        requestBody.put("fullName", data.get("fullName"));

        // Chuyển role tiếng Việt → mã role backend hiểu
        String roleVi = data.get("role");
        String roleEn = roleVi.equals("Quản lý") ? "ADMIN" : "OFFICER";
        requestBody.put("role", roleEn);

        // Gửi POST request
        String response = sendHttpRequest("POST", url, requestBody.toString());

        JSONObject jsonResponse = new JSONObject(response);

        if (!jsonResponse.getBoolean("success")) {
            String msg = jsonResponse.optString("message", "Đăng ký thất bại");
            throw new Exception(msg);
        }

        return true; // Thành công
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

    public List<User> getAllUsers() throws Exception {
        String url = BASE_URL + "/api/users";

        String response = sendHttpRequest("GET", url, null);

        JSONObject jsonResponse = new JSONObject(response);

        if (!jsonResponse.getBoolean("success")) {
            String msg = jsonResponse.optString("message", "Lỗi không xác định từ server");
            throw new Exception(msg);
        }

        JSONArray dataArray = jsonResponse.getJSONArray("data");
        List<User> users = new ArrayList<>();

        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject obj = dataArray.getJSONObject(i);

            // Bỏ qua tài khoản đã bị xóa mềm
            if (obj.has("deleted") && obj.getBoolean("deleted")) {
                continue;
            }

            Integer id = obj.getInt("id");
            String username = obj.getString("username");
            String fullName = obj.getString("fullName");
            String email = obj.getString("email");
            String role = obj.getString("role");

            // Parse createdAt (ví dụ: "2025-11-25T08:00:45.311028")
            String createdAtStr = obj.optString("createdAt", null);
            String dateFormatted = "N/A";
            if (createdAtStr != null && !createdAtStr.equals("null")) {
                try {
                    // Cắt chuỗi nếu quá dài (nano → micro)
                    if (createdAtStr.length() > 19) {
                        createdAtStr = createdAtStr.substring(0, 19); // chỉ lấy đến giây
                    }
                    java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(
                            createdAtStr.replace("T", " ").substring(0, 19),
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    );
                    dateFormatted = ldt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                } catch (Exception e) {
                    dateFormatted = createdAtStr.substring(0, 10); // fallback: chỉ lấy ngày
                }
            }

            // Tạo User (dùng constructor hoặc setter đều được)
            User user = new User();
            user.setId(id);
            user.setUsername(username);
            user.setFullName(fullName);
            user.setEmail(email);
            user.setRole(role);
            user.setDateCreated(dateFormatted); // giả sử có method này trong model.User

            users.add(user);
        }

        return users;
    }

    // --- SỬA TÀI KHOẢN (CẬP NHẬT THÔNG TIN + ĐỔI MẬT KHẨU) ---
    public boolean updateUser(int userId, Map<String, String> data) throws Exception {
        String url = BASE_URL + "/api/users/" + userId;

        JSONObject requestBody = new JSONObject();

        // Chỉ thêm các field có giá trị (tránh gửi password rỗng)
        String fullName = data.get("fullName");
        String roleVi = data.get("role");
        String password = data.get("password");

        if (fullName != null && !fullName.trim().isEmpty()) {
            requestBody.put("fullName", fullName.trim());
        }

        // Chuyển role tiếng Việt → backend (ADMIN / OFFICER)
        String roleEn = roleVi.equals("Quản lý") ? "ADMIN" : "OFFICER";
        requestBody.put("role", roleEn);

        // Chỉ gửi password nếu người dùng nhập (không bắt buộc)
        if (password != null && !password.trim().isEmpty()) {
            if (password.length() < 6) {
                throw new Exception("Mật khẩu mới phải ít nhất 6 ký tự!");
            }
            requestBody.put("password", password.trim());
        }

        // Nếu không có gì để cập nhật → báo lỗi
        if (requestBody.length() == 1 && requestBody.has("role")) {
            // Chỉ có role → vẫn cho phép cập nhật
        } else if (requestBody.length() == 0) {
            throw new Exception("Không có thông tin nào để cập nhật.");
        }

        String response = sendHttpRequest("PUT", url, requestBody.toString());

        JSONObject jsonResponse = new JSONObject(response);

        if (!jsonResponse.getBoolean("success")) {
            String msg = jsonResponse.optString("message", "Cập nhật thất bại");
            throw new Exception(msg);
        }

        return true;
    }
    // --- XÓA TÀI KHOẢN THẬT QUA API ---
    public boolean deleteUser(int userId) throws Exception {
        String url = BASE_URL + "/api/users/" + userId;

        String response = sendHttpRequest("DELETE", url, null);

        JSONObject jsonResponse = new JSONObject(response);

        if (!jsonResponse.getBoolean("success")) {
            String message = jsonResponse.optString("message", "Xóa tài khoản thất bại");
            throw new Exception(message);
        }

        // Nếu success = true → trả về true để Panel biết reload bảng
        return true;
    }

    // ... Các phương thức API khác ...
}