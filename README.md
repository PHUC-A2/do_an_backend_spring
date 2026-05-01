# TBU Sport - Hệ thống Đặt Lịch Sân Cỏ Nhân Tạo tại Trường Đại học Tây Bắc

## Mục lục

1. [Tổng quan dự án](#1-tổng-quan-dự-án)
2. [Công nghệ sử dụng](#2-công-nghệ-sử-dụng)
3. [Cấu trúc dự án](#3-cấu-trúc-dự-án)
4. [Cơ sở dữ liệu](#4-cơ-sở-dữ-liệu)
5. [API Endpoints - Danh sách đầy đủ](#5-api-endpoints---danh-sách-đầy-đủ)
6. [Chức năng hệ thống](#6-chức-năng-hệ-thống)
7. [Frontend - Pages & Components](#7-frontend---pages--components)
8. [Cấu hình & Môi trường](#8-cấu-hình--môi-trường)
9. [Hướng dẫn cài đặt](#9-hướng-dẫn-cài-đặt)

---

## 1. Tổng quan dự án

**Tên dự án:** TBU Sport
**Mục tiêu:** Xây dựng hệ thống đặt lịch sân cỏ nhân tạo tại Trường Đại học Tây Bắc, giúp sinh viên, giảng viên và người dùng dễ dàng xem lịch, đặt sân, thanh toán và quản lý toàn bộ hoạt động sân bóng.

**Kiến trúc:**
- **Backend:** RESTful API - Java Spring Boot 3
- **Frontend:** Single Page Application - React + TypeScript
- **Database:** MySQL 8.0+
- **Authentication:** JWT (OAuth2 Resource Server) + Refresh Token (HttpOnly Cookie)
- **Notification:** Firebase Cloud Messaging (FCM)
- **AI Chat:** Groq / Gemini / Cloudflare AI
- **File Storage:** Local file system

---

## 2. Công nghệ sử dụng

### Backend

| Công nghệ / Thư viện | Phiên bản | Mục đích sử dụng |
|---|---|---|
| Java | 21 | Ngôn ngữ lập trình chính |
| Spring Boot | 3.5.11 | Framework backend chính |
| Spring Security | (tích hợp Spring Boot) | Bảo mật, phân quyền |
| Spring OAuth2 Resource Server | (tích hợp Spring Boot) | Xác thực JWT |
| Spring Data JPA | (tích hợp Spring Boot) | ORM, truy vấn database |
| Spring Boot Starter Mail | (tích hợp Spring Boot) | Gửi email (OTP, reset password) |
| Spring Boot Starter Thymeleaf | (tích hợp Spring Boot) | Template email HTML |
| Firebase Admin SDK | (mới nhất) | Push notification (FCM) |
| Spring Filter | (mới nhất) | Lọc dữ liệu động JPA |
| Lombok | 1.18.42 | Giảm boilerplate code |
| Java Dotenv | 5.2.2 | Đọc file .env |
| MySQL Connector/J | (tương thích MySQL 8) | Kết nối MySQL |
| Maven | (build tool) | Quản lý dependencies, build |

### Frontend

| Công nghệ / Thư viện | Phiên bản | Mục đích sử dụng |
|---|---|---|
| React | 19.2.0 | UI framework chính |
| TypeScript | 5.9.3 | Type-safe JavaScript |
| Vite | 7.2.4 | Build tool, dev server |
| Redux Toolkit | 2.11.0 | Quản lý state toàn cục |
| React Router | 7.9.6 | Điều hướng client-side |
| Axios | 1.13.2 | HTTP client, gọi API |
| Ant Design | 6.0.0 | UI components (Admin) |
| React Bootstrap | 2.10.10 | UI components (Client) |
| Bootstrap | 5.x | CSS framework |
| ECharts + React ECharts | 6.0.0 | Biểu đồ thống kê doanh thu |
| Firebase | 12.10.0 | FCM push notification |
| XLSX | 0.18.5 | Xuất file Excel |
| File-Saver | 2.0.5 | Tải file về máy |
| Framer Motion | 12.23.24 | Animation, hiệu ứng chuyển trang |
| Day.js | 1.11.19 | Xử lý ngày giờ |
| React Toastify | (mới nhất) | Toast notification UI |
| React Icons | 5.5.0 | Bộ icon phong phú |
| React Player | (mới nhất) | Phát video |

### Môi trường & Công cụ

| Công cụ | Mục đích |
|---|---|
| MySQL 8.0+ | Database chính |
| Node.js 18+ | Chạy frontend dev server |
| Git | Quản lý mã nguồn |
| Firebase Console | Cấu hình FCM |
| Gmail SMTP | Gửi email hệ thống |
| Agribank (QR) | Cổng thanh toán chuyển khoản |
| Groq AI | AI Chat (model llama-3.3-70b-versatile) |
| Google Gemini | AI Chat (model gemini-2.5-flash) |
| Cloudflare AI | AI Chat (model @cf/meta/llama-3-8b-instruct) |

---

## 3. Cấu trúc dự án

```
test_01/
├── backend/                         # Spring Boot Application
│   ├── src/main/java/com/example/backend/
│   │   ├── controller/
│   │   │   ├── admin/               # 12 Admin Controllers
│   │   │   │   ├── AdminAiController.java
│   │   │   │   ├── AdminAiKeyController.java
│   │   │   │   ├── BookingController.java
│   │   │   │   ├── BookingEquipmentController.java
│   │   │   │   ├── EquipmentController.java
│   │   │   │   ├── PaymentController.java
│   │   │   │   ├── PermissionController.java
│   │   │   │   ├── PitchController.java
│   │   │   │   ├── PitchEquipmentController.java
│   │   │   │   ├── RevenueController.java
│   │   │   │   ├── RoleController.java
│   │   │   │   └── UserController.java
│   │   │   ├── auth/
│   │   │   │   └── AuthController.java
│   │   │   ├── client/              # 8 Client Controllers
│   │   │   │   ├── ClientAiController.java
│   │   │   │   ├── ClientBookingController.java
│   │   │   │   ├── ClientBookingEquipmentController.java
│   │   │   │   ├── ClientNotificationController.java
│   │   │   │   ├── ClientPaymentController.java
│   │   │   │   ├── ClientPublicEquipmentController.java
│   │   │   │   ├── ClientPublicPitchEquipmentController.java
│   │   │   │   └── PublicPitchBookingController.java
│   │   │   ├── FileController.java
│   │   │   └── HelloController.java
│   │   ├── service/                 # 18 Services
│   │   │   ├── AuthService.java
│   │   │   ├── UserService.java
│   │   │   ├── RoleService.java
│   │   │   ├── PermissionService.java
│   │   │   ├── PitchService.java
│   │   │   ├── PitchEquipmentService.java
│   │   │   ├── BookingService.java
│   │   │   ├── BookingEquipmentService.java
│   │   │   ├── EquipmentService.java
│   │   │   ├── PaymentService.java
│   │   │   ├── RevenueService.java
│   │   │   ├── FileService.java
│   │   │   ├── EmailService.java
│   │   │   ├── NotificationService.java
│   │   │   ├── PublicPitchBookingService.java
│   │   │   ├── PasswordResetTokenService.java
│   │   │   ├── AiService.java
│   │   │   └── AiApiKeyService.java
│   │   ├── repository/              # Spring Data JPA Repositories
│   │   │   ├── UserRepository.java
│   │   │   ├── RoleRepository.java
│   │   │   ├── PermissionRepository.java
│   │   │   ├── PitchRepository.java
│   │   │   ├── BookingRepository.java
│   │   │   ├── EquipmentRepository.java
│   │   │   ├── BookingEquipmentRepository.java
│   │   │   ├── PaymentRepository.java
│   │   │   ├── PitchEquipmentRepository.java
│   │   │   ├── NotificationRepository.java
│   │   │   ├── PasswordResetTokenRepository.java
│   │   │   ├── AiApiKeyRepository.java
│   │   │   └── AiChatSessionRepository.java
│   │   ├── domain/
│   │   │   ├── entity/              # 13 JPA Entities
│   │   │   │   ├── User.java
│   │   │   │   ├── Role.java
│   │   │   │   ├── Permission.java
│   │   │   │   ├── Pitch.java
│   │   │   │   ├── Booking.java
│   │   │   │   ├── Equipment.java
│   │   │   │   ├── BookingEquipment.java
│   │   │   │   ├── Payment.java
│   │   │   │   ├── PitchEquipment.java
│   │   │   │   ├── Notification.java
│   │   │   │   ├── PasswordResetToken.java
│   │   │   │   ├── AiApiKey.java
│   │   │   │   └── AiChatSession.java
│   │   │   ├── request/             # Request DTOs
│   │   │   └── response/            # Response DTOs
│   │   ├── config/                  # Security & Config
│   │   │   ├── CustomAccessDeniedHandler.java
│   │   │   ├── CustomAuthenticationEntryPoint.java
│   │   │   ├── AccessTokenValidatorGrantedAuthoritiesConverter.java
│   │   │   ├── UserDetailsCustom.java
│   │   │   ├── StaticResourcesWebConfiguration.java
│   │   │   └── BackendProperties.java
│   │   └── util/
│   │       ├── annotation/          # Custom annotations (ApiMessage)
│   │       ├── error/               # Exception classes
│   │       ├── constant/            # Enums
│   │       ├── SecurityUtil.java
│   │       └── FormatRestResponse.java
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── .env
│   ├── .env.example
│   └── pom.xml
│
└── frontend/                        # React Application
    ├── src/
    │   ├── pages/
    │   │   ├── admin/               # 8+ Admin Pages
    │   │   ├── auth/                # LoginPage, RegisterPage, VerifyEmailPage
    │   │   ├── client/              # HomePage, PitchPage, BookingPage, etc.
    │   │   └── error/               # 404, 403
    │   ├── components/
    │   │   ├── admin/               # AdminSidebar, Wrappers
    │   │   ├── client/              # Header, Footer, ChatBot
    │   │   └── common/              # ProgressBar, LogoGlow, etc.
    │   ├── layouts/
    │   │   ├── AdminLayout.tsx
    │   │   └── ClientLayout.tsx
    │   ├── redux/
    │   │   ├── features/            # 12 Redux Slices
    │   │   ├── store.ts
    │   │   └── hooks.ts
    │   ├── types/                   # 25+ TypeScript interfaces
    │   ├── config/
    │   │   ├── Api.ts               # API endpoint constants
    │   │   ├── customAxios.ts       # Axios interceptors
    │   │   └── firebase.ts          # Firebase config
    │   ├── hooks/
    │   │   ├── common/              # 7 custom hooks
    │   │   └── init/                # 2 init hooks
    │   ├── utils/
    │   │   ├── constants/           # 10 constant files
    │   │   ├── export/              # Excel, Revenue export
    │   │   ├── format/              # Date, Price format
    │   │   └── permission.ts, role.ts, sound.ts
    │   ├── routes/
    │   │   └── AppRouter.tsx
    │   ├── App.tsx
    │   └── main.tsx
    ├── .env
    ├── .env.example
    ├── package.json
    ├── vite.config.ts
    └── tsconfig.json
```

---

## 4. Cơ sở dữ liệu

### Danh sách bảng (MySQL)

| STT | Tên bảng | Mô tả |
|---|---|---|
| 1 | `users` | Người dùng hệ thống |
| 2 | `roles` | Vai trò (ADMIN, USER, ...) |
| 3 | `permissions` | Quyền hạn chi tiết |
| 4 | `user_roles` | Liên kết nhiều-nhiều user ↔ role |
| 5 | `role_permissions` | Liên kết nhiều-nhiều role ↔ permission |
| 6 | `pitches` | Sân bóng đá |
| 7 | `bookings` | Đơn đặt sân |
| 8 | `equipments` | Thiết bị (bóng, áo, ...) |
| 9 | `booking_equipments` | Thiết bị mượn theo đơn đặt |
| 10 | `pitch_equipments` | Thiết bị gắn với sân |
| 11 | `payments` | Thanh toán |
| 12 | `notifications` | Thông báo |
| 13 | `password_reset_tokens` | Token reset mật khẩu |
| 14 | `ai_api_keys` | API keys cho AI chat |
| 15 | `ai_chat_sessions` | Phiên chat AI |

### Chi tiết các Entity chính

#### User (Người dùng)
| Trường | Kiểu | Mô tả |
|---|---|---|
| id | Long (PK) | ID tự động |
| name | String | Tên đăng nhập |
| fullName | String | Họ và tên đầy đủ |
| email | String (unique) | Email đăng nhập |
| password | String | Mật khẩu (đã hash) |
| phoneNumber | String | Số điện thoại |
| avatarUrl | String | URL ảnh đại diện |
| status | Enum | ACTIVE / PENDING_VERIFICATION / BANNED / INACTIVE |
| refreshToken | String | Refresh token hiện tại |
| fcmToken | String | Firebase FCM token |
| bannedReason | String | Lý do bị khóa |
| bannedAt | LocalDateTime | Thời điểm bị khóa |
| roles | Set\<Role\> | Vai trò (Many-to-Many) |
| createdAt / updatedAt | LocalDateTime | Audit timestamp |
| createdBy / updatedBy | String | Audit user |

#### Role (Vai trò)
| Trường | Kiểu | Mô tả |
|---|---|---|
| id | Long (PK) | ID tự động |
| name | String (unique) | Tên vai trò |
| description | String | Mô tả |
| permissions | Set\<Permission\> | Quyền hạn (Many-to-Many) |

#### Permission (Quyền hạn)
| Trường | Kiểu | Mô tả |
|---|---|---|
| id | Long (PK) | ID tự động |
| name | String (unique) | Tên quyền |
| description | String | Mô tả |

#### Pitch (Sân bóng)
| Trường | Kiểu | Mô tả |
|---|---|---|
| id | Long (PK) | ID tự động |
| name | String | Tên sân |
| pitchType (FK) | Long | Tham chiếu `pitch_types.id` |
| pricePerHour | BigDecimal | Giá thuê mỗi giờ |
| pitchUrl | String | URL trang sân |
| imageUrl | String | URL ảnh sân |
| openTime | LocalTime | Giờ mở cửa |
| closeTime | LocalTime | Giờ đóng cửa |
| open24h | Boolean | Mở 24/7 |
| status | Enum | ACTIVE / INACTIVE |
| address | String | Địa chỉ |
| latitude | Double | Vĩ độ |
| longitude | Double | Kinh độ |
| length | Double | Chiều dài sân |
| width | Double | Chiều rộng sân |
| height | Double | Chiều cao sân |

#### Booking (Đặt sân)
| Trường | Kiểu | Mô tả |
|---|---|---|
| id | Long (PK) | ID tự động |
| user | User (FK) | Người đặt sân |
| pitch | Pitch (FK) | Sân được đặt |
| startDateTime | LocalDateTime | Thời gian bắt đầu |
| endDateTime | LocalDateTime | Thời gian kết thúc |
| durationMinutes | Integer | Thời lượng (phút) |
| shirtOption | Enum | WITHOUT_PITCH_SHIRT / WITH_PITCH_SHIRT |
| contactPhone | String | SĐT liên hệ |
| totalPrice | BigDecimal | Tổng tiền |
| status | Enum | ACTIVE / APPROVED / REJECTED / COMPLETED / CANCELLED |
| deletedByUser | Boolean | Đã xóa bởi user |

#### Equipment (Thiết bị)
| Trường | Kiểu | Mô tả |
|---|---|---|
| id | Long (PK) | ID tự động |
| name | String | Tên thiết bị |
| description | String | Mô tả |
| totalQuantity | Integer | Tổng số lượng |
| availableQuantity | Integer | Số lượng còn sẵn |
| price | BigDecimal | Giá mượn |
| imageUrl | String | URL ảnh |
| status | Enum | ACTIVE / INACTIVE |

#### Payment (Thanh toán)
| Trường | Kiểu | Mô tả |
|---|---|---|
| id | Long (PK) | ID tự động |
| booking | Booking (FK) | Đơn đặt sân liên quan |
| amount | BigDecimal | Số tiền |
| content | String | Nội dung chuyển khoản (BOOKING_XX) |
| paymentCode | String (unique) | Mã giao dịch |
| status | Enum | PENDING / PAID / CANCELLED |
| method | Enum | BANK_TRANSFER / CASH |
| proofUrl | String | URL ảnh chứng từ |
| paidAt | LocalDateTime | Thời điểm thanh toán |

#### Notification (Thông báo)
| Trường | Kiểu | Mô tả |
|---|---|---|
| id | Long (PK) | ID tự động |
| content | String | Nội dung thông báo |
| user | User (FK) | Người nhận |
| isRead | Boolean | Đã đọc hay chưa |
| type | String | Loại thông báo |

---

## 5. API Endpoints - Danh sách đầy đủ

> **Base URL:** `http://localhost:8080`
> **Version:** `/api/v1`

### 5.1 Authentication (`/api/v1/auth`)

| Method | Endpoint | Mô tả | Xác thực |
|---|---|---|---|
| POST | `/api/v1/auth/register` | Đăng ký tài khoản mới | Không |
| POST | `/api/v1/auth/login` | Đăng nhập, lấy access token + refresh token | Không |
| POST | `/api/v1/auth/verify-email` | Xác thực email bằng mã OTP | Không |
| POST | `/api/v1/auth/resend-otp` | Gửi lại mã OTP xác thực email | Không |
| POST | `/api/v1/auth/forgot-password` | Gửi email link reset mật khẩu | Không |
| PATCH | `/api/v1/auth/reset-password` | Đặt lại mật khẩu bằng token | Không |
| GET | `/api/v1/auth/account` | Lấy thông tin tài khoản đang đăng nhập | Có (JWT) |
| PATCH | `/api/v1/auth/account/me` | Cập nhật thông tin cá nhân | Có (JWT) |
| GET | `/api/v1/auth/refresh` | Làm mới access token (dùng refresh token cookie) | Có (Cookie) |
| POST | `/api/v1/auth/logout` | Đăng xuất, xóa refresh token | Có (JWT) |

### 5.2 Quản lý Người dùng - Admin (`/api/v1/users`)

| Method | Endpoint | Mô tả | Quyền yêu cầu |
|---|---|---|---|
| POST | `/api/v1/users` | Tạo người dùng mới | ALL hoặc USER_CREATE |
| GET | `/api/v1/users` | Danh sách người dùng (phân trang, lọc) | ALL hoặc USER_VIEW_LIST |
| GET | `/api/v1/users/{id}` | Chi tiết thông tin người dùng | ALL hoặc USER_VIEW_DETAIL |
| PUT | `/api/v1/users/{id}` | Cập nhật thông tin người dùng | ALL hoặc USER_UPDATE |
| PATCH | `/api/v1/users/{id}/status` | Cập nhật trạng thái (ACTIVE/BANNED/INACTIVE) | ALL hoặc USER_UPDATE |
| DELETE | `/api/v1/users/{id}` | Xóa người dùng | ALL hoặc USER_DELETE |
| PUT | `/api/v1/users/{id}/assign-roles` | Gán vai trò cho người dùng | ALL hoặc USER_ASSIGN_ROLE |

### 5.3 Quản lý Vai trò - Admin (`/api/v1/roles`)

| Method | Endpoint | Mô tả | Quyền yêu cầu |
|---|---|---|---|
| POST | `/api/v1/roles` | Tạo vai trò mới | ALL hoặc ROLE_CREATE |
| GET | `/api/v1/roles` | Danh sách vai trò (phân trang, lọc) | ALL hoặc ROLE_VIEW_LIST |
| GET | `/api/v1/roles/{id}` | Chi tiết vai trò | ALL hoặc ROLE_VIEW_DETAIL |
| PUT | `/api/v1/roles/{id}` | Cập nhật vai trò | ALL hoặc ROLE_UPDATE |
| DELETE | `/api/v1/roles/{id}` | Xóa vai trò | ALL hoặc ROLE_DELETE |
| PUT | `/api/v1/roles/{id}/assign-permissions` | Gán quyền hạn cho vai trò | ALL hoặc ROLE_ASSIGN_PERMISSION |

### 5.4 Quản lý Quyền hạn - Admin (`/api/v1/permissions`)

| Method | Endpoint | Mô tả | Quyền yêu cầu |
|---|---|---|---|
| POST | `/api/v1/permissions` | Tạo quyền mới | ALL hoặc PERMISSION_CREATE |
| GET | `/api/v1/permissions` | Danh sách quyền (phân trang, lọc) | ALL hoặc PERMISSION_VIEW_LIST |
| GET | `/api/v1/permissions/{id}` | Chi tiết quyền | ALL hoặc PERMISSION_VIEW_DETAIL |
| PUT | `/api/v1/permissions/{id}` | Cập nhật quyền | ALL hoặc PERMISSION_UPDATE |
| DELETE | `/api/v1/permissions/{id}` | Xóa quyền | ALL hoặc PERMISSION_DELETE |

### 5.5 Quản lý Sân bóng - Admin (`/api/v1/pitches`)

| Method | Endpoint | Mô tả | Quyền yêu cầu |
|---|---|---|---|
| POST | `/api/v1/pitches` | Tạo sân bóng mới | ALL hoặc PITCH_CREATE |
| GET | `/api/v1/pitches` | Danh sách sân bóng (phân trang, lọc) | Public (không cần xác thực) |
| GET | `/api/v1/pitches/{id}` | Chi tiết sân bóng | Public (không cần xác thực) |
| PUT | `/api/v1/pitches/{id}` | Cập nhật sân bóng | ALL hoặc PITCH_UPDATE |
| DELETE | `/api/v1/pitches/{id}` | Xóa sân bóng | ALL hoặc PITCH_DELETE |

### 5.6 Quản lý Thiết bị Sân - Admin (`/api/v1/pitches/{pitchId}/pitch-equipments`)

| Method | Endpoint | Mô tả | Quyền yêu cầu |
|---|---|---|---|
| GET | `/api/v1/pitches/{pitchId}/pitch-equipments` | Danh sách thiết bị của sân (admin) | Xác thực |
| PUT | `/api/v1/pitches/{pitchId}/pitch-equipments` | Thêm / cập nhật thiết bị cho sân | ALL hoặc EQUIPMENT_UPDATE |
| DELETE | `/api/v1/pitches/{pitchId}/pitch-equipments/{equipmentId}` | Xóa thiết bị khỏi sân | Xác thực |

### 5.7 Quản lý Đặt sân - Admin (`/api/v1/bookings`)

| Method | Endpoint | Mô tả | Quyền yêu cầu |
|---|---|---|---|
| POST | `/api/v1/bookings` | Tạo đặt sân (admin tạo thay user) | ALL hoặc BOOKING_CREATE |
| GET | `/api/v1/bookings` | Danh sách đặt sân (phân trang, lọc) | ALL hoặc BOOKING_VIEW_LIST |
| GET | `/api/v1/bookings/{id}` | Chi tiết đặt sân | ALL hoặc BOOKING_VIEW_DETAIL |
| PUT | `/api/v1/bookings/{id}` | Cập nhật đặt sân | ALL hoặc BOOKING_UPDATE |
| DELETE | `/api/v1/bookings/{id}` | Xóa đặt sân | ALL hoặc BOOKING_DELETE |
| PATCH | `/api/v1/bookings/{id}/approve` | Xác nhận / duyệt đặt sân | ALL hoặc BOOKING_UPDATE |
| PATCH | `/api/v1/bookings/{id}/reject` | Từ chối đặt sân | ALL hoặc BOOKING_UPDATE |

### 5.8 Quản lý Thiết bị - Admin (`/api/v1/equipments`)

| Method | Endpoint | Mô tả | Quyền yêu cầu |
|---|---|---|---|
| POST | `/api/v1/equipments` | Tạo thiết bị mới | ALL hoặc EQUIPMENT_CREATE |
| GET | `/api/v1/equipments` | Danh sách thiết bị (phân trang, lọc) | ALL hoặc EQUIPMENT_VIEW_LIST |
| GET | `/api/v1/equipments/{id}` | Chi tiết thiết bị | ALL hoặc EQUIPMENT_VIEW_DETAIL |
| PUT | `/api/v1/equipments/{id}` | Cập nhật thiết bị | ALL hoặc EQUIPMENT_UPDATE |
| DELETE | `/api/v1/equipments/{id}` | Xóa thiết bị | ALL hoặc EQUIPMENT_DELETE |

### 5.9 Quản lý Mượn Thiết bị - Admin (`/api/v1/booking-equipments`)

| Method | Endpoint | Mô tả | Quyền yêu cầu |
|---|---|---|---|
| POST | `/api/v1/booking-equipments` | Tạo bản ghi mượn thiết bị | Xác thực (Admin) |
| GET | `/api/v1/booking-equipments` | Danh sách mượn thiết bị (phân trang, lọc) | Xác thực (Admin) |
| PATCH | `/api/v1/booking-equipments/{id}/status` | Cập nhật trạng thái mượn thiết bị | Xác thực (Admin) |
| DELETE | `/api/v1/booking-equipments/{id}` | Xóa bản ghi mượn thiết bị | Xác thực (Admin) |

### 5.10 Quản lý Thanh toán - Admin (`/api/v1/payments`)

| Method | Endpoint | Mô tả | Quyền yêu cầu |
|---|---|---|---|
| GET | `/api/v1/payments` | Danh sách thanh toán chờ xác nhận | ALL hoặc PAYMENT_VIEW_LIST |
| PUT | `/api/v1/payments/{id}/confirm` | Xác nhận thanh toán đã nhận tiền | ALL hoặc PAYMENT_UPDATE |

### 5.11 Thống kê Doanh thu - Admin (`/api/v1/revenues`)

| Method | Endpoint | Mô tả | Quyền yêu cầu |
|---|---|---|---|
| GET | `/api/v1/revenues?from=YYYY-MM-DD&to=YYYY-MM-DD` | Thống kê doanh thu theo khoảng thời gian | ALL hoặc REVENUE_VIEW_DETAIL |

### 5.12 Quản lý AI - Admin (`/api/v1/admin/ai`)

| Method | Endpoint | Mô tả | Quyền yêu cầu |
|---|---|---|---|
| GET | `/api/v1/admin/ai` | Lấy cấu hình AI hiện tại | Xác thực (Admin) |
| PUT | `/api/v1/admin/ai` | Cập nhật cấu hình AI | Xác thực (Admin) |
| GET | `/api/v1/admin/ai/keys` | Danh sách AI API keys | Xác thực (Admin) |
| POST | `/api/v1/admin/ai/keys` | Thêm AI API key | Xác thực (Admin) |
| DELETE | `/api/v1/admin/ai/keys/{id}` | Xóa AI API key | Xác thực (Admin) |

### 5.13 Client - Đặt sân (`/api/v1/client/bookings`)

| Method | Endpoint | Mô tả | Xác thực |
|---|---|---|---|
| POST | `/api/v1/client/bookings` | Khách hàng tạo đặt sân | Có (JWT) |
| GET | `/api/v1/client/bookings` | Danh sách đặt sân của user hiện tại | Có (JWT) |
| GET | `/api/v1/client/bookings/{id}` | Chi tiết đặt sân của user | Có (JWT) |
| PUT | `/api/v1/client/bookings/{id}` | Cập nhật đặt sân của user | Có (JWT) |
| DELETE | `/api/v1/client/bookings/{id}` | Xóa đặt sân của user | Có (JWT) |
| PATCH | `/api/v1/client/bookings/{id}/cancel` | Hủy đặt sân của user | Có (JWT) |

### 5.14 Client - Thanh toán (`/api/v1/client/payments`)

| Method | Endpoint | Mô tả | Xác thực |
|---|---|---|---|
| POST | `/api/v1/client/payments` | Tạo thanh toán + nhận mã QR | Có (JWT) |
| GET | `/api/v1/client/payments/{paymentCode}/qr` | Lấy QR code thanh toán | Có (JWT) |
| PATCH | `/api/v1/client/payments/{paymentId}/proof` | Upload ảnh chứng từ thanh toán | Có (JWT) |

### 5.15 Client - Mượn Thiết bị (`/api/v1/client/booking-equipments`)

| Method | Endpoint | Mô tả | Xác thực |
|---|---|---|---|
| GET | `/api/v1/client/booking-equipments` | Danh sách thiết bị đã mượn của user | Có (JWT) |

### 5.16 Client - Thông báo (`/api/v1/client/notifications`)

| Method | Endpoint | Mô tả | Xác thực |
|---|---|---|---|
| GET | `/api/v1/client/notifications` | Danh sách thông báo của user | Có (JWT) |
| PATCH | `/api/v1/client/notifications/{id}/read` | Đánh dấu thông báo đã đọc | Có (JWT) |
| PATCH | `/api/v1/client/notifications/read-all` | Đánh dấu tất cả thông báo đã đọc | Có (JWT) |

### 5.17 Client - AI Chat (`/api/v1/client/ai`)

| Method | Endpoint | Mô tả | Xác thực |
|---|---|---|---|
| POST | `/api/v1/client/ai/chat` | Gửi tin nhắn chat AI | Có (JWT) |
| GET | `/api/v1/client/ai/chat/history` | Lấy lịch sử chat AI | Có (JWT) |
| DELETE | `/api/v1/client/ai/chat/history` | Xóa lịch sử chat AI | Có (JWT) |

### 5.18 Public - Sân bóng & Timeline

| Method | Endpoint | Mô tả | Xác thực |
|---|---|---|---|
| GET | `/api/v1/client/public/pitches/{pitchId}/timeline?date=YYYY-MM-DD` | Lấy timeline đặt sân theo ngày (ai đã đặt, slot nào còn trống) | Không |
| GET | `/api/v1/client/public/pitches/{pitchId}/pitch-equipments` | Danh sách thiết bị của sân (public) | Không |
| GET | `/api/v1/client/public/equipments` | Danh sách thiết bị có thể mượn (public) | Không |

### 5.19 Upload File

| Method | Endpoint | Mô tả | Xác thực |
|---|---|---|---|
| POST | `/api/v1/files/upload?folder=products` | Upload ảnh (jpg, jpeg, png, webp, max 50MB) | Có (JWT) |

### 5.20 Test / Health Check

| Method | Endpoint | Mô tả | Xác thực |
|---|---|---|---|
| GET | `/api/v1/hello` | API test, kiểm tra server hoạt động | Không |

---

## 6. Chức năng hệ thống

### 6.1 Xác thực & Phân quyền

- **Đăng ký tài khoản** với xác thực email OTP
- **Đăng nhập** với JWT (Access Token + Refresh Token lưu HttpOnly cookie)
- **Làm mới token** tự động khi access token hết hạn
- **Đăng xuất** xóa refresh token
- **Quên mật khẩu** - gửi email link reset
- **Đặt lại mật khẩu** bằng token bảo mật
- **Phân quyền theo vai trò (RBAC)** - Role-Based Access Control
- **Phân quyền theo permission** - kiểm tra từng quyền cụ thể
- **Bảo vệ API** với Spring Security + PreAuthorize annotations
- **Cấp quyền ALL** - ADMIN có toàn bộ quyền

### 6.2 Quản lý Người dùng (Admin)

- Xem danh sách người dùng với phân trang và lọc
- Tạo tài khoản người dùng mới
- Xem chi tiết thông tin người dùng
- Cập nhật thông tin người dùng
- Khóa / mở khóa tài khoản (ACTIVE / BANNED / INACTIVE)
- Xóa người dùng
- Gán vai trò cho người dùng
- Quản lý trạng thái người dùng (với lý do khóa, thời gian khóa)

### 6.3 Quản lý Vai trò & Quyền hạn (Admin)

- CRUD đầy đủ cho vai trò
- CRUD đầy đủ cho quyền hạn
- Gán / thu hồi quyền hạn cho vai trò
- Gán / thu hồi vai trò cho người dùng
- Hệ thống phân quyền linh hoạt nhiều lớp

### 6.4 Quản lý Sân bóng (Admin)

- Tạo / sửa / xóa sân bóng
- Upload ảnh sân bóng
- Cấu hình loại sân (5 người / 7 người / 11 người)
- Cấu hình giá thuê mỗi giờ
- Cấu hình giờ mở / đóng cửa (hoặc mở 24/7)
- Cấu hình trạng thái sân (ACTIVE / INACTIVE)
- Lưu tọa độ địa lý (latitude, longitude)
- Lưu kích thước sân (dài, rộng, cao)
- Quản lý thiết bị gắn với sân

### 6.5 Quản lý Đặt sân (Admin)

- Xem toàn bộ danh sách đặt sân với bộ lọc mạnh
- Tạo đặt sân thay cho người dùng
- Xem chi tiết đặt sân
- Cập nhật thông tin đặt sân
- **Duyệt / từ chối đặt sân**
- Xóa đặt sân
- Xem timeline đặt sân theo từng ngày

### 6.6 Đặt sân (Client / Người dùng)

- Xem danh sách sân bóng
- Xem chi tiết sân bóng (ảnh, giá, giờ mở cửa, kích thước)
- **Xem timeline đặt sân theo ngày** - thấy slot nào đã đặt, còn trống
- **Tạo đặt sân** với các tùy chọn:
  - Chọn sân, ngày, giờ bắt đầu và kết thúc
  - Tùy chọn mượn áo sân (WITH / WITHOUT)
  - Nhập SĐT liên hệ
  - Mượn thiết bị thêm (bóng, ...)
- Xem lịch sử đặt sân
- Cập nhật thông tin đặt sân
- **Hủy đặt sân**
- Xóa đặt sân khỏi danh sách cá nhân

### 6.7 Quản lý Thiết bị (Admin)

- CRUD thiết bị (bóng, áo, dụng cụ...)
- Upload ảnh thiết bị
- Theo dõi số lượng tổng và số lượng còn sẵn
- Quản lý trạng thái thiết bị (ACTIVE / INACTIVE)
- Gắn thiết bị cho sân cụ thể
- Quản lý bản ghi mượn thiết bị theo đơn đặt

### 6.8 Quản lý Thanh toán

**Admin:**
- Xem danh sách thanh toán chờ xác nhận
- Xác nhận thanh toán đã nhận tiền

**Client:**
- Tạo thanh toán cho đơn đặt sân
- **Nhận mã QR chuyển khoản ngân hàng** (Agribank)
- Lấy lại QR code khi cần
- **Upload ảnh chứng từ thanh toán**
- Hỗ trợ 2 phương thức: BANK_TRANSFER / CASH
- Theo dõi trạng thái thanh toán (PENDING / PAID / CANCELLED)

### 6.9 Thống kê Doanh thu (Admin)

- Thống kê doanh thu theo khoảng thời gian (from → to)
- Doanh thu tổng hợp
- Doanh thu theo từng sân bóng
- Biểu đồ xu hướng doanh thu (ECharts)
- **Xuất báo cáo doanh thu ra file Excel**

### 6.10 Thông báo

- **Firebase FCM push notification** - thông báo đẩy tới trình duyệt
- Thông báo trong app (in-app notification)
- Lưu lịch sử thông báo
- Đánh dấu đã đọc từng thông báo
- Đánh dấu tất cả thông báo đã đọc
- Thông báo khi đặt sân được duyệt / từ chối

### 6.11 AI Chat Bot

**Client:**
- Chat hỏi đáp về sân bóng, dịch vụ
- Lịch sử chat lưu theo session
- Xóa lịch sử chat
- Giới hạn tin nhắn: 15 tin off-topic / ngày, 100 tin on-topic / ngày

**Admin:**
- Quản lý cấu hình AI (model, provider)
- Quản lý API keys cho Groq / Gemini / Cloudflare AI
- Xem và quản lý các phiên chat

**AI Providers tích hợp:**
- **Groq AI** - model llama-3.3-70b-versatile
- **Google Gemini** - model gemini-2.5-flash
- **Cloudflare AI** - model @cf/meta/llama-3-8b-instruct

### 6.12 Upload File

- Upload ảnh (jpg, jpeg, png, webp)
- Giới hạn kích thước: 50MB mỗi file
- Lưu file theo thư mục (folder parameter)
- Trả về URL để lưu vào database

### 6.13 Chức năng Tài khoản Cá nhân (Client)

- Xem thông tin tài khoản
- Cập nhật họ tên, SĐT, ảnh đại diện
- Lịch sử đặt sân cá nhân
- Lịch sử thông báo cá nhân

---

## 7. Frontend - Pages & Components

### 7.1 Routes & Pages

#### Authentication
| Route | Component | Chức năng |
|---|---|---|
| `/` (root) | LoginPage | Form đăng nhập với email / mật khẩu |
| `/register` | RegisterPage | Form đăng ký tài khoản mới |
| `/verify-email` | VerifyEmailPage | Nhập mã OTP xác thực email |

#### Client (Người dùng)
| Route | Component | Chức năng |
|---|---|---|
| `/home` | HomePage | Trang chủ, giới thiệu hệ thống |
| `/pitches` | PitchPage | Danh sách sân bóng có thể đặt |
| `/pitches/:id` | PitchDetailsPage | Chi tiết sân bóng |
| `/booking` | BookingPage | Form đặt sân + xem timeline lịch trống |
| `/about` | AboutPage | Trang giới thiệu về hệ thống |
| `/terms-of-service` | TermsOfService | Điều khoản dịch vụ |

#### Admin
| Route | Component | Chức năng |
|---|---|---|
| `/admin` | AdminPage | Dashboard tổng quan + thống kê doanh thu |
| `/admin/users` | AdminUserPage | Quản lý người dùng (CRUD + phân quyền) |
| `/admin/pitches` | AdminPitchPage | Quản lý sân bóng (CRUD) |
| `/admin/bookings` | AdminBookingPage | Quản lý đặt sân (duyệt / từ chối) |
| `/admin/payments` | AdminPaymentPage | Xác nhận thanh toán |
| `/admin/equipments` | AdminEquipmentPage | Quản lý thiết bị |
| `/admin/booking-equipments` | AdminBookingEquipmentPage | Quản lý mượn thiết bị |
| `/admin/roles` | AdminRolePage | Quản lý vai trò |
| `/admin/permissions` | AdminPermissionPage | Quản lý quyền hạn |
| `/admin/ai` | AdminAiPage | Cấu hình AI chat |
| `/admin/support` | AdminSupportPage | Hỗ trợ |

#### Error Pages
| Route | Component | Chức năng |
|---|---|---|
| `/404` | NotFoundPage | Trang không tồn tại |
| `/403` | ForbiddenPage | Không có quyền truy cập |

### 7.2 Components

#### Layout
- `AdminLayout.tsx` - Layout khung admin (sidebar + header)
- `ClientLayout.tsx` - Layout khung client (header + footer)
- `AdminSidebar.tsx` - Menu điều hướng admin
- `Header.tsx` - Header chung (logo, menu, user info)
- `Footer.tsx` - Footer chung

#### Wrappers / Guards
- `AdminWrapper.tsx` - Kiểm tra quyền admin trước khi render
- `PermissionWrapper.tsx` - Render có điều kiện theo permission

#### Common
- `TopProgressBar.tsx` - Thanh tiến trình tải trang ở đầu
- `LogoGlow.tsx` - Logo với hiệu ứng glow
- `BackToTop.tsx` - Nút cuộn lên đầu trang
- `MessageButton.tsx` - Nút mở messenger / chat

#### AI Chat
- `ChatBot.tsx` - Giao diện chat AI cho client
- `AdminChatBot.tsx` - Giao diện chat AI cho admin

### 7.3 Redux Store

| Slice | Quản lý state |
|---|---|
| `authSlice` | isAuthenticated, accessToken, expiresIn |
| `accountSlice` | name, email, phone, avatarUrl, roles, permissions |
| `userSlice` | users[], total, currentUser, loading |
| `pitchSlice` | pitches[], currentPitch, loading |
| `bookingSlice` | bookings[], currentBooking (admin) |
| `bookingClientSlice` | clientBookings[], currentClientBooking |
| `bookingUiSlice` | isModalAddOpen, isModalUpdateOpen, ... |
| `roleSlice` | roles[], currentRole |
| `permissionSlice` | permissions[], currentPermission |
| `paymentSlice` | payments[], currentPayment |
| `equipmentSlice` | equipments[], currentEquipment |
| `messengerButtonUiSlice` | isOpen |

### 7.4 Custom Hooks

| Hook | Chức năng |
|---|---|
| `useAdminAccess()` | Kiểm tra và redirect nếu không có quyền admin |
| `useBrowserNotification()` | Xin quyền và hiển thị browser notification |
| `useFcmToken()` | Lấy và lưu Firebase FCM token |
| `useOutsideClick(ref, callback)` | Detect click ra ngoài element |
| `usePermission(permissionName)` | Kiểm tra user có permission cụ thể không |
| `useRole(roleName)` | Kiểm tra user có role cụ thể không |
| `useTopProgress()` | Điều khiển top progress bar |
| `useAccountInit()` | Khởi tạo thông tin tài khoản khi app load |
| `useAuthInit()` | Khởi tạo trạng thái auth khi app load |
| `useBookingTimeline()` | Fetch và xử lý timeline đặt sân |

### 7.5 Utilities

#### Format
- `localdatetime.ts` - Format ngày giờ theo chuẩn Việt Nam
- `price.ts` - Format giá tiền (VND)

#### Export
- `exportExcelFromTable.ts` - Xuất dữ liệu bảng ra file Excel (.xlsx)
- `exportRevenueReport.ts` - Xuất báo cáo doanh thu ra Excel

#### Permission Helpers
- `permission.ts` - Utility functions kiểm tra quyền hạn
- `role.ts` - Utility functions kiểm tra vai trò

#### Sound
- `sound.ts` - Phát âm thanh thông báo

### 7.6 API Config (`src/config/Api.ts`)

Tổng hợp tất cả endpoint constants:

```typescript
// Auth
AUTH_LOGIN, AUTH_REGISTER, AUTH_VERIFY_EMAIL, AUTH_RESEND_OTP
AUTH_FORGOT_PASSWORD, AUTH_RESET_PASSWORD
AUTH_ACCOUNT, AUTH_ME, AUTH_REFRESH, AUTH_LOGOUT

// Admin resources
USERS, USER_BY_ID, USER_STATUS, USER_ASSIGN_ROLES
ROLES, ROLE_BY_ID, ROLE_ASSIGN_PERMISSIONS
PERMISSIONS, PERMISSION_BY_ID
PITCHES, PITCH_BY_ID, PITCH_EQUIPMENTS, PITCH_EQUIPMENT_BY_ID
BOOKINGS, BOOKING_BY_ID, BOOKING_APPROVE, BOOKING_REJECT
EQUIPMENTS, EQUIPMENT_BY_ID
BOOKING_EQUIPMENTS, BOOKING_EQUIPMENT_STATUS
PAYMENTS, PAYMENT_CONFIRM
REVENUES

// Client
CLIENT_BOOKINGS, CLIENT_BOOKING_BY_ID, CLIENT_BOOKING_CANCEL
CLIENT_PAYMENTS, CLIENT_PAYMENT_QR, CLIENT_PAYMENT_PROOF
CLIENT_BOOKING_EQUIPMENTS
CLIENT_NOTIFICATIONS, CLIENT_NOTIFICATION_READ, CLIENT_NOTIFICATION_READ_ALL
CLIENT_AI_CHAT, CLIENT_AI_CHAT_HISTORY

// Public
PUBLIC_PITCH_TIMELINE, PUBLIC_PITCH_EQUIPMENTS, PUBLIC_EQUIPMENTS

// File
FILE_UPLOAD
```

---

## 8. Cấu hình & Môi trường

### 8.1 Backend (`application.yml`)

```yaml
server:
  port: 8080
  address: 0.0.0.0

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/doan
    username: root
    password: ${DATABASE_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update    # Tự động cập nhật schema
    show-sql: false
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB

jwt:
  access-token-expiration: 604800   # 7 ngày (giây)
  refresh-token-expiration: 604800  # 7 ngày (giây)
  secret: ${JWT_SECRET}

payment:
  bank:
    code: ${BANK_CODE}              # Agribank
    account-no: ${BANK_ACCOUNT_NO}
    account-name: ${BANK_ACCOUNT_NAME}

admin:
  default:
    email: ${ADMIN_EMAIL}
    name: ${ADMIN_NAME}
    password: ${ADMIN_PASSWORD}

ai:
  groq:
    api-key: ${GROQ_API_KEY}
    base-url: https://api.groq.com/openai/v1
    model: llama-3.3-70b-versatile
  gemini:
    api-key: ${GEMINI_API_KEY}
    model: gemini-2.5-flash
  cloudflare:
    api-token: ${CLOUDFLARE_API_TOKEN}
    account-id: ${CLOUDFLARE_ACCOUNT_ID}
    model: "@cf/meta/llama-3-8b-instruct"
  limits:
    off-topic-per-day: 15
    on-topic-per-day: 100

firebase:
  service-account-json: ${FIREBASE_SERVICE_ACCOUNT_JSON}

upload:
  base-uri: ${UPLOAD_DIR}
```

### 8.2 Backend Environment Variables (`.env`)

```env
DATABASE_PASSWORD=your_mysql_password
JWT_SECRET=your_jwt_secret_key
MAIL_USERNAME=your_gmail@gmail.com
MAIL_PASSWORD=your_gmail_app_password
BANK_CODE=agribank_bank_code
BANK_ACCOUNT_NO=your_bank_account_number
BANK_ACCOUNT_NAME=your_account_name
ADMIN_EMAIL=admin@example.com
ADMIN_NAME=Admin
ADMIN_PASSWORD=your_admin_password
GROQ_API_KEY=your_groq_api_key
GEMINI_API_KEY=your_gemini_api_key
CLOUDFLARE_API_TOKEN=your_cloudflare_token
CLOUDFLARE_ACCOUNT_ID=your_cloudflare_account_id
FIREBASE_SERVICE_ACCOUNT_JSON={"type":"service_account",...}
UPLOAD_DIR=/path/to/upload/directory
```

### 8.3 Frontend Environment Variables (`.env`)

```env
VITE_BACKEND_URL=http://localhost:8080
VITE_PAGE_ID=886239791243320

# Firebase Configuration
VITE_FIREBASE_API_KEY=your_firebase_api_key
VITE_FIREBASE_AUTH_DOMAIN=your_project.firebaseapp.com
VITE_FIREBASE_PROJECT_ID=your_project_id
VITE_FIREBASE_STORAGE_BUCKET=your_project.appspot.com
VITE_FIREBASE_MESSAGING_SENDER_ID=your_sender_id
VITE_FIREBASE_APP_ID=your_app_id
VITE_FIREBASE_VAPID_KEY=your_vapid_key
```

---

## 9. Hướng dẫn cài đặt

### 9.1 Yêu cầu hệ thống

- Java 21+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+

### 9.2 Cài đặt Backend

```bash
cd backend

# 1. Copy và cấu hình biến môi trường
cp .env.example .env
# Chỉnh sửa .env với thông tin thực tế

# 2. Tạo database MySQL
mysql -u root -p
CREATE DATABASE doan CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 3. Build và chạy
mvn clean install
mvn spring-boot:run
# Server chạy tại http://localhost:8080
```

### 9.3 Cài đặt Frontend

```bash
cd frontend

# 1. Copy và cấu hình biến môi trường
cp .env.example .env
# Chỉnh sửa .env với thông tin thực tế

# 2. Cài đặt dependencies
npm install

# 3. Chạy dev server
npm run dev
# Frontend chạy tại http://localhost:5173

# 4. Build production
npm run build
```

---

## Tóm tắt thống kê

| Hạng mục | Số lượng |
|---|---|
| Controllers (Backend) | 23 (12 Admin + 8 Client + Auth + File + Hello) |
| Services (Backend) | 18 |
| Entities (Backend) | 13 |
| Repositories (Backend) | 13 |
| API Endpoints | ~65+ endpoints |
| Redux Slices (Frontend) | 12 |
| Pages (Frontend) | ~20 trang |
| Custom Hooks (Frontend) | 10 |
| TypeScript Interfaces | 25+ |
| Database Tables | 15 |

---

*Hệ thống TBU Sport - Là đồ án sinh viên được phát triển bởi sinh viên Bàn Văn Phúc Trường Đại học Tây Bắc*
