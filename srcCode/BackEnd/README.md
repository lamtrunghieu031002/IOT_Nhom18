# Hệ Thống Phát Hiện Nồng Độ Cồn (Alcohol Detection System)

Backend API cho hệ thống phát hiện và quản lý nồng độ cồn sử dụng Spring Boot, được thiết kế để hỗ trợ các thiết bị IoT đo nồng độ cồn và quản lý dữ liệu đo lường.

## 🎯 Tổng Quan

Hệ thống phát hiện nồng độ cồn là một ứng dụng backend được xây dựng để:
- Quản lý các thiết bị đo nồng độ cồn (IoT devices)
- Ghi nhận và lưu trữ kết quả đo nồng độ cồn
- Theo dõi vi phạm và tạo báo cáo thống kê
- Quản lý người dùng và phân quyền (Officer, Admin)
- Cung cấp API RESTful cho frontend và các thiết bị IoT

## 🛠️ Công Nghệ Sử Dụng

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: PostgreSQL
- **Security**: Spring Security + JWT (JSON Web Token)
- **API Documentation**: SpringDoc OpenAPI (Swagger)
- **Build Tool**: Maven

## ✨ Tính Năng

### 🔐 Authentication & Authorization
- Đăng ký và đăng nhập người dùng
- Xác thực bằng JWT token
- Phân quyền theo vai trò (Role-based access control)
- Bảo mật mật khẩu với Spring Security

### 📱 Quản Lý Thiết Bị (Device Management)
- Đăng ký thiết bị mới
- Kiểm tra trạng thái thiết bị
- Quản lý trạng thái thiết bị (ACTIVE, INACTIVE, MAINTENANCE)
- Lịch hiệu chuẩn thiết bị
- Thống kê thiết bị

### 📊 Ghi Nhận Đo Lường (Measurement Recording)
- Ghi nhận kết quả đo nồng độ cồn từ thiết bị
- Lưu trữ thông tin: nồng độ cồn, thời gian, thiết bị, người thực hiện
- Xác định vi phạm dựa trên ngưỡng cho phép
- Truy vấn lịch sử đo lường
- Thống kê đo lường theo thời gian

### 📈 Thống Kê & Báo Cáo
- Thống kê tổng quan (theo ngày/tuần/tháng)
- Thống kê vi phạm
- Thống kê thiết bị
- Thống kê đo lường theo khoảng thời gian
- Phân tích xu hướng

### 👥 Quản Lý Người Dùng
- Quản lý thông tin người dùng
- Phân quyền theo vai trò (ADMIN, OFFICER)
- Cập nhật thông tin người dùng

### ⚠️ Quản Lý Vi Phạm
- Tự động phát hiện vi phạm khi nồng độ cồn vượt ngưỡng
- Lưu trữ thông tin vi phạm
- Thống kê vi phạm chi tiết

## 💻 Yêu Cầu Hệ Thống

- **Java**: JDK 17 hoặc cao hơn
- **Maven**: 3.6+ 
- **PostgreSQL**: 12+
- **IDE**: IntelliJ IDEA

## 📦 Cài Đặt

### 1. Clone Repository

```bash
git clone <repository-url>
cd srcCode/BackEnd
```

### 2. Cài Đặt Dependencies

```bash
mvn clean install
```

### 3. Cấu Hình Database

Tạo database PostgreSQL hoặc sử dụng cloud database. Cập nhật thông tin kết nối trong file `application.yaml`.

## ⚙️ Cấu Hình

### File `application.yaml`

Cập nhật file `src/main/resources/application.yaml` với nội dung:

- url: jdbc:postgresql://iot-database-kwinn0332-a11a.c.aivencloud.com:19603/defaultdb?sslmode=require&ssl=true
- username: avnadmin
- password: AVNS_OCAKD1bJDP7ATIAn5OJ
- secretKey: 2c5dfe83271d0b206b1d4e226a3f351601b454dfb83da5465672e507ed0fabbc

Hoặc có thể tạo file .env với các giá trị

- SPRING_DATASOURCE_URL=jdbc:postgresql://iot-database-kwinn0332-a11a.c.aivencloud.com:19603/defaultdb?sslmode=require&ssl=true
- SPRING_DATASOURCE_USERNAME=avnadmin
- SPRING_DATASOURCE_PASSWORD=AVNS_OCAKD1bJDP7ATIAn5OJ

- JWT_SECRET_KEY=2c5dfe83271d0b206b1d4e226a3f351601b454dfb83da5465672e507ed0fabbc

## 🚀 Chạy Ứng Dụng

```bash
# Build JAR file
mvn clean package

# Chạy JAR file
java -jar target/alcohol-detection-system-0.0.1-SNAPSHOT.jar
```

Ứng dụng sẽ chạy tại: `http://localhost:8080`

## 📚 API Documentation

Sau khi khởi động ứng dụng, truy cập Swagger UI tại:

```
http://localhost:8080/swagger-ui.html
```

Hoặc OpenAPI JSON tại:

```
http://localhost:8080/v3/api-docs
```

### Các Endpoint Chính

#### Authentication
- `POST /api/auth/register` - Đăng ký người dùng mới
- `POST /api/auth/login` - Đăng nhập và nhận JWT token

#### Devices
- `POST /api/devices/register` - Đăng ký thiết bị mới
- `GET /api/devices` - Lấy danh sách thiết bị (có phân trang)
- `GET /api/devices/{deviceId}` - Lấy thông tin thiết bị
- `GET /api/devices/check/{deviceId}` - Kiểm tra thiết bị
- `PUT /api/devices/{deviceId}/status` - Cập nhật trạng thái thiết bị
- `POST /api/devices/{deviceId}/calibration` - Cập nhật lịch hiệu chuẩn
- `GET /api/devices/calibration/needed` - Lấy danh sách thiết bị cần hiệu chuẩn
- `GET /api/devices/statistics` - Thống kê thiết bị
- `DELETE /api/devices/{deviceId}` - Xóa thiết bị

#### Measurements
- `POST /api/measurements` - Ghi nhận kết quả đo mới
- `GET /api/measurements` - Lấy danh sách đo lường (có phân trang)
- `GET /api/measurements/{id}` - Lấy thông tin đo lường theo ID
- `GET /api/measurements/statistics` - Thống kê đo lường
- `GET /api/measurements/officer/{officerId}` - Lấy đo lường theo officer
- `GET /api/measurements/device/{deviceId}` - Lấy đo lường theo thiết bị

#### Statistics
- `GET /api/statistics` - Thống kê tổng quan
- `GET /api/statistics/violations` - Thống kê vi phạm

#### Users
- `GET /api/users` - Lấy danh sách người dùng
- `GET /api/users/{id}` - Lấy thông tin người dùng
- `PUT /api/users/{id}` - Cập nhật thông tin người dùng
- `DELETE /api/users/{id}` - Xóa người dùng

### Authentication

Hầu hết các endpoint yêu cầu JWT token trong header:

```
Authorization: Bearer <your-jwt-token>
```

## 📁 Cấu Trúc Dự Án

```
src/
├── main/
│   ├── java/
│   │   └── com/alcohol/alcoholdetectionsystem/
│   │       ├── AlcoholDetectionSystemApplication.java
│   │       ├── config/              # Cấu hình (Security, CORS, Swagger)
│   │       ├── controller/          # REST Controllers
│   │       ├── service/             # Business Logic
│   │       ├── repository/          # Data Access Layer
│   │       ├── entity/              # JPA Entities
│   │       ├── dto/                 # Data Transfer Objects
│   │       │   ├── request/         # Request DTOs
│   │       │   └── response/       # Response DTOs
│   │       ├── enums/               # Enumerations
│   │       └── exception/           # Exception Handlers
│   └── resources/
│       └── application.yaml         # Application Configuration
└── test/                            # Unit Tests
```

## 🔒 Bảo Mật

### JWT Authentication
- Token có thời hạn (mặc định 24 giờ)
- Secret key được lưu trong cấu hình
- Token được validate trên mọi request được bảo vệ

### Spring Security
- Mã hóa mật khẩu với BCrypt
- CORS được cấu hình
- Endpoints được bảo vệ theo vai trò

## 📄 License

Dự án này được phát triển cho mục đích học tập và nghiên cứu.

## 👥 Tác Giả

Nhóm 18 - IoT Project