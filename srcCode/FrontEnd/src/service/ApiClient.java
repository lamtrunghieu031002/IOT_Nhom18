package service;

import model.Device;
import model.DevicePageResponse;
import model.MeasurementHistory;
import model.MeasurementStatics;
import model.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
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

        String url = BASE_URL + "/api/measurements?page=1&size=10";

        String response = sendHttpRequest("GET", url, null);
        JSONObject json = new JSONObject(response);

        if (!json.getBoolean("success")) {
            throw new Exception(json.optString("message", "Lỗi tải lịch sử đo"));
        }

        JSONObject data = json.getJSONObject("data");
        JSONArray arr = data.getJSONArray("measurements");

        List<MeasurementHistory> list = new ArrayList<>();

        // format datetime
        java.time.format.DateTimeFormatter input =
                java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        java.time.format.DateTimeFormatter out =
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);

            int id = o.getInt("id");
            String subjectName = o.getString("subjectName");
            double alc = o.getDouble("alcoholLevel");

            // Add mg/L
            String alcoholLevel = alc + " mg/L";

            String violationLevel = o.getString("violationLevel").toUpperCase();

            // ====== FORMAT THỜI GIAN ======
            String testTimeRaw = o.optString("testTime", null);
            String testTimeFormatted = "Không có";

            if (testTimeRaw != null && !testTimeRaw.equals("null") && testTimeRaw.length() >= 19) {
                testTimeRaw = testTimeRaw.substring(0, 19);
                testTimeFormatted = out.format(java.time.LocalDateTime.parse(testTimeRaw, input));
            }

            // ====== LOCATION LÀ TỈNH/THÀNH (bạn tự đổi theo mong muốn) ======
            String location = o.optString("location", "Không rõ");

            // ====== OFFICER ======
            String officerFullName = o.optString("officerFullName", "Không rõ");

            list.add(new MeasurementHistory(
                    id,
                    subjectName,
                    alcoholLevel,
                    violationLevel,
                    testTimeFormatted,
                    location,
                    officerFullName
            ));
        }

        return list;
    }

    public MeasurementStatics getStatistics(String startDate, String endDate) throws Exception {
        String url = BASE_URL + "/api/measurements/statistics?startDate="
                + URLEncoder.encode(startDate, StandardCharsets.UTF_8.name())
                + "&endDate="
                + URLEncoder.encode(endDate, StandardCharsets.UTF_8.name());

        // Gọi API (sendHttpRequest trả về String response body)
        String response = sendHttpRequest("GET", url, null);

        // Parse JSON response
        JSONObject json = new JSONObject(response);

        // Kiểm tra success
        boolean ok = json.optBoolean("success", false);
        if (!ok) {
            throw new Exception(json.optString("message", "Không lấy được thống kê"));
        }

        JSONObject dataJson = json.optJSONObject("data");
        if (dataJson == null) {
            throw new Exception("Response không có trường data");
        }

        MeasurementStatics stats = new MeasurementStatics();
        stats.success = true;
        stats.message = json.optString("message", null);

        MeasurementStatics.Data data = new MeasurementStatics.Data();
        data.totalTests = dataJson.optInt("totalTests", 0);
        data.violations = dataJson.optInt("violations", 0);
        data.averageLevel = dataJson.optDouble("averageLevel", 0.0);

        JSONObject byLevelJson = dataJson.optJSONObject("byLevel");
        MeasurementStatics.ByLevel by = new MeasurementStatics.ByLevel();
        if (byLevelJson != null) {
            by.high = byLevelJson.optInt("high", 0);
            by.low  = byLevelJson.optInt("low", 0);
            by.none = byLevelJson.optInt("none", 0);
        } else {
            by.high = by.low = by.none = 0;
        }
        data.byLevel = by;

        stats.data = data;
        return stats;
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
    public List<Device> scanAndCheckDevices() throws Exception {
        // Quét Bluetooth tại Client (Offline)
        BluetoothClientScanner scanner = new BluetoothClientScanner();
        List<String> scannedMacs;

        try {
            scannedMacs = scanner.scan();
        } catch (Exception e) {
            throw new Exception("Lỗi phần cứng Bluetooth: " + e.getMessage() + "\nHãy đảm bảo máy tính đã bật Bluetooth.");
        }

        if (scannedMacs.isEmpty()) {
            return new ArrayList<>(); // Không tìm thấy gì thì không cần gọi Server
        }

        // Gọi API check-batch để lọc ra thiết bị hợp lệ
        return checkBatchDevicesWithServer(scannedMacs);
    }

    // Hàm gọi API POST /api/devices/check-batch
    private List<Device> checkBatchDevicesWithServer(List<String> macAddresses) throws Exception {
        String url = BASE_URL + "/api/devices/check-batch";

        // Tạo JSON Body: { "macAddresses": ["MAC1", "MAC2"] }
        JSONObject requestBody = new JSONObject();
        JSONArray macArray = new JSONArray();
        for (String mac : macAddresses) {
            macArray.put(mac);
        }
        requestBody.put("macAddresses", macArray);

        // Gửi POST request
        String response = sendHttpRequest("POST", url, requestBody.toString());
        JSONObject json = new JSONObject(response);

        if (!json.getBoolean("success")) {
            throw new Exception(json.optString("message", "Lỗi kiểm tra thiết bị từ Server"));
        }

        // Parse kết quả trả về (Danh sách Device đã đăng ký)
        JSONArray dataArray = json.optJSONArray("data");
        List<Device> validDevices = new ArrayList<>();

        if (dataArray == null) return validDevices;

        java.time.format.DateTimeFormatter outputFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject o = dataArray.getJSONObject(i);

            String deviceId = o.getString("deviceId");
            String name = o.optString("name", "Unknown");
            String model = o.optString("model", "N/A");
            String status = o.optString("status", "UNKNOWN");

            String createdAtRaw = o.optString("createdAt", null);
            String createdAtFormatted = "N/A";
            if (createdAtRaw != null && createdAtRaw.length() >= 10) {
                createdAtFormatted = createdAtRaw.substring(0, 10); // Lấy nhanh ngày
            }

            validDevices.add(new Device(deviceId, name, model, createdAtFormatted, status));
        }

        return validDevices;
    }

    // ... Các phương thức API khác ...
}