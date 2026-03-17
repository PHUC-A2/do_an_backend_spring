## /api/v1/auth/register [POST]
### Request
```json
{
  "email": "nguyenvana@utb.edu.vn",
  "password": "123456"
}
```
### Response 201
```json
{
  "statusCode": 201,
  "error": null,
  "message": "Đăng ký tài khoản",
  "data": {
    "message": {
      "message": "Đăng ký tài khoản thành công. Vui lòng xác thực email bằng OTP đã gửi.",
      "userId": 101,
      "email": "nguyenvana@utb.edu.vn"
    }
  }
}
```
### Response Error (email đã tồn tại)
```json
{
  "statusCode": 400,
  "error": "Email không hợp lệ",
  "message": "Email: nguyenvana@utb.edu.vn đã tồn tại",
  "data": null
}
```

## /api/v1/auth/verify-email [POST]
### Request
```json
{
  "userId": 101,
  "email": "nguyenvana@utb.edu.vn",
  "otp": "123456"
}
```
### Response 200
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Xác thực email",
  "data": {
    "message": "Xác thực email thành công. Vui lòng đăng nhập."
  }
}
```
### Response Error (OTP sai)
```json
{
  "statusCode": 400,
  "error": "Yêu cầu không hợp lệ",
  "message": "OTP không hợp lệ",
  "data": null
}
```
### Response Error (OTP hết hạn)
```json
{
  "statusCode": 400,
  "error": "Email không hợp lệ",
  "message": "OTP đã hết hạn",
  "data": null
}
```

## /api/v1/auth/resend-otp [POST]
### Request
```json
{
  "userId": 101,
  "email": "nguyenvana@utb.edu.vn"
}
```
### Response 200
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Gửi lại OTP xác thực email",
  "data": {
    "message": "Đã gửi lại OTP xác thực email"
  }
}
```
### Response Error (chưa đủ 60s)
```json
{
  "statusCode": 400,
  "error": "Yêu cầu không hợp lệ",
  "message": "Vui lòng thử lại sau 42 giây",
  "data": null
}
```

## /api/v1/auth/login [POST]
### Request
```json
{
  "username": "nguyenvana@utb.edu.vn",
  "password": "123456"
}
```
### Response Error (PENDING_VERIFICATION)
```json
{
  "statusCode": 400,
  "error": "Yêu cầu không hợp lệ",
  "message": "Tài khoản chưa xác thực email. Vui lòng kiểm tra hộp thư.",
  "data": null
}
```
### Response Error (BANNED)
```json
{
  "statusCode": 400,
  "error": "Yêu cầu không hợp lệ",
  "message": "Tài khoản đã bị khóa.",
  "data": null
}
```
### Response Error (INACTIVE)
```json
{
  "statusCode": 400,
  "error": "Yêu cầu không hợp lệ",
  "message": "Tài khoản đã bị vô hiệu hóa.",
  "data": null
}
```

## /api/v1/users/5/status [PATCH]
### Request (khóa tài khoản)
```json
{
  "status": "BANNED",
  "reason": "Vi phạm quy định đặt sân 3 lần liên tiếp"
}
```
### Response 200
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Cập nhật trạng thái người dùng",
  "data": {
    "id": 5,
    "status": "BANNED",
    "bannedReason": "Vi phạm quy định đặt sân 3 lần liên tiếp",
    "bannedAt": "2026-03-16T09:45:00Z",
    "updatedAt": "2026-03-16T09:45:00Z"
  }
}
```

## /api/v1/users/5/status [PATCH]
### Request (mở khóa tài khoản)
```json
{
  "status": "ACTIVE",
  "reason": null
}
```
### Response 200
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Cập nhật trạng thái người dùng",
  "data": {
    "id": 5,
    "status": "ACTIVE",
    "bannedReason": null,
    "bannedAt": null,
    "updatedAt": "2026-03-16T10:00:00Z"
  }
}
```

## /api/v1/users/5/status [PATCH]
### Request (lỗi khóa chính mình)
```json
{
  "status": "BANNED",
  "reason": "Tự khóa"
}
```
### Response Error
```json
{
  "statusCode": 400,
  "error": "Yêu cầu không hợp lệ",
  "message": "Không thể khóa chính mình",
  "data": null
}
```

## /api/v1/users/2/status [PATCH]
### Request (lỗi khóa tài khoản ADMIN)
```json
{
  "status": "BANNED",
  "reason": "Người dung co nhan khong hop le"
}
```
### Response Error
```json
{
  "statusCode": 400,
  "error": "Yêu cầu không hợp lệ",
  "message": "Không thể khóa tài khoản ADMIN",
  "data": null
}
```

## /api/v1/users?status=BANNED [GET]
### Response 200
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Lấy danh sách người dùng",
  "data": {
    "meta": {
      "page": 1,
      "pageSize": 10,
      "pages": 1,
      "total": 1
    },
    "result": [
      {
        "id": 5,
        "name": "nguyenvana",
        "fullName": "Nguyen Van A",
        "email": "nguyenvana@utb.edu.vn",
        "phoneNumber": "0987654321",
        "avatarUrl": null,
        "status": "BANNED",
        "bannedReason": "Vi phạm quy định đặt sân 3 lần liên tiếp",
        "bannedAt": "2026-03-16T09:45:00Z",
        "createdAt": "2026-03-10T08:00:00Z",
        "updatedAt": "2026-03-16T09:45:00Z",
        "createdBy": "admin@utb.edu.vn",
        "updatedBy": "admin@utb.edu.vn",
        "roles": [
          {
            "id": 2,
            "name": "VIEW",
            "description": "Người dùng thường"
          }
        ]
      }
    ]
  }
}
```

## /api/v1/auth/refresh [GET]
### Response Error (user đã bị khóa)
```json
{
  "statusCode": 401,
  "error": "Unauthorized",
  "message": "Tài khoản đã bị khóa.",
  "data": null
}
```

## /api/v1/facility-categories [GET]
### Response 200
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Lấy danh sách danh mục cơ sở vật chất",
  "data": {
    "meta": {
      "page": 1,
      "pageSize": 10,
      "pages": 1,
      "total": 3
    },
    "result": [
      {
        "id": 1,
        "name": "Hệ thống chiếu sáng",
        "description": "Đèn cho sân cỏ",
        "categoryType": "PITCH",
        "createdAt": "2026-03-17T00:30:00Z",
        "updatedAt": null,
        "createdBy": "admin@utb.edu.vn",
        "updatedBy": null
      }
    ]
  }
}
```

## /api/v1/facility-categories [POST]
### Request
```json
{
  "name": "Lưới",
  "description": "Lưới khung thành và lưới bao sân",
  "categoryType": "PITCH"
}
```
### Response 201
```json
{
  "statusCode": 201,
  "error": null,
  "message": "Tạo danh mục cơ sở vật chất",
  "data": {
    "id": 2,
    "name": "Lưới",
    "description": "Lưới khung thành và lưới bao sân",
    "categoryType": "PITCH",
    "createdAt": "2026-03-17T00:35:00Z",
    "updatedAt": null,
    "createdBy": "admin@utb.edu.vn",
    "updatedBy": null
  }
}
```

## /api/v1/facility-categories/{id} [PUT]
### Request
```json
{
  "name": "Lưới sân bóng",
  "description": "Lưới PE cho sân 7 người",
  "categoryType": "PITCH"
}
```
### Response 200
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Cập nhật danh mục cơ sở vật chất",
  "data": {
    "id": 2,
    "name": "Lưới sân bóng",
    "description": "Lưới PE cho sân 7 người",
    "categoryType": "PITCH",
    "createdAt": "2026-03-17T00:35:00Z",
    "updatedAt": "2026-03-17T00:45:00Z",
    "createdBy": "admin@utb.edu.vn",
    "updatedBy": "admin@utb.edu.vn"
  }
}
```

## /api/v1/facility-categories/{id} [DELETE]
### Response 200
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Xóa danh mục cơ sở vật chất",
  "data": null
}
```

## /api/v1/facilities [GET]
### Request Query
```json
{
  "pitchId": 1,
  "categoryId": 1,
  "condition": "GOOD",
  "page": 1,
  "pageSize": 10
}
```
### Response 200
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Lấy danh sách cơ sở vật chất",
  "data": {
    "meta": {
      "page": 1,
      "pageSize": 10,
      "pages": 1,
      "total": 1
    },
    "result": [
      {
        "id": 1,
        "name": "Đèn LED 200W",
        "description": "Đèn cho sân chính",
        "quantity": 8,
        "condition": "GOOD",
        "specifications": "200W, LED, IP65",
        "location": "Xung quanh sân A",
        "category": {
          "id": 1,
          "name": "Hệ thống chiếu sáng"
        },
        "pitch": {
          "id": 1,
          "name": "Sân A"
        },
        "roomId": null,
        "purchaseDate": "2024-06-15",
        "warrantyExpiry": "2026-06-15",
        "purchasePrice": 2500000,
        "createdAt": "2026-03-17T00:50:00Z",
        "updatedAt": null,
        "createdBy": "admin@utb.edu.vn",
        "updatedBy": null
      }
    ]
  }
}
```

## /api/v1/facilities/{id} [GET]
### Response 200
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Lấy chi tiết cơ sở vật chất",
  "data": {
    "id": 1,
    "name": "Đèn LED 200W",
    "description": "Đèn cho sân chính",
    "quantity": 8,
    "condition": "GOOD",
    "specifications": "200W, LED, IP65",
    "location": "Xung quanh sân A",
    "category": {
      "id": 1,
      "name": "Hệ thống chiếu sáng"
    },
    "pitch": {
      "id": 1,
      "name": "Sân A"
    },
    "roomId": null,
    "purchaseDate": "2024-06-15",
    "warrantyExpiry": "2026-06-15",
    "purchasePrice": 2500000,
    "createdAt": "2026-03-17T00:50:00Z",
    "updatedAt": null,
    "createdBy": "admin@utb.edu.vn",
    "updatedBy": null
  }
}
```

## /api/v1/facilities [POST]
### Request
```json
{
  "name": "Đèn LED 200W",
  "description": "Đèn cho sân chính",
  "quantity": 8,
  "condition": "GOOD",
  "specifications": "200W, LED, IP65",
  "location": "Xung quanh sân A",
  "categoryId": 1,
  "pitchId": 1,
  "roomId": null,
  "purchaseDate": "2024-06-15",
  "warrantyExpiry": "2026-06-15",
  "purchasePrice": 2500000
}
```
### Response 201
```json
{
  "statusCode": 201,
  "error": null,
  "message": "Tạo cơ sở vật chất",
  "data": {
    "id": 1,
    "name": "Đèn LED 200W",
    "description": "Đèn cho sân chính",
    "quantity": 8,
    "condition": "GOOD",
    "specifications": "200W, LED, IP65",
    "location": "Xung quanh sân A",
    "category": {
      "id": 1,
      "name": "Hệ thống chiếu sáng"
    },
    "pitch": {
      "id": 1,
      "name": "Sân A"
    },
    "roomId": null,
    "purchaseDate": "2024-06-15",
    "warrantyExpiry": "2026-06-15",
    "purchasePrice": 2500000,
    "createdAt": "2026-03-17T00:50:00Z",
    "updatedAt": null,
    "createdBy": "admin@utb.edu.vn",
    "updatedBy": null
  }
}
```

## /api/v1/facilities/{id} [PUT]
### Request
```json
{
  "name": "Đèn LED 250W",
  "description": "Nâng cấp đèn sân chính",
  "quantity": 10,
  "condition": "UNDER_REPAIR",
  "specifications": "250W, LED, IP65",
  "location": "Sân A",
  "categoryId": 1,
  "pitchId": 1,
  "roomId": null,
  "purchaseDate": "2024-06-15",
  "warrantyExpiry": "2027-06-15",
  "purchasePrice": 3200000
}
```
### Response 200
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Cập nhật cơ sở vật chất",
  "data": {
    "id": 1,
    "name": "Đèn LED 250W",
    "description": "Nâng cấp đèn sân chính",
    "quantity": 10,
    "condition": "UNDER_REPAIR",
    "specifications": "250W, LED, IP65",
    "location": "Sân A",
    "category": {
      "id": 1,
      "name": "Hệ thống chiếu sáng"
    },
    "pitch": {
      "id": 1,
      "name": "Sân A"
    },
    "roomId": null,
    "purchaseDate": "2024-06-15",
    "warrantyExpiry": "2027-06-15",
    "purchasePrice": 3200000,
    "createdAt": "2026-03-17T00:50:00Z",
    "updatedAt": "2026-03-17T01:00:00Z",
    "createdBy": "admin@utb.edu.vn",
    "updatedBy": "admin@utb.edu.vn"
  }
}
```

## /api/v1/facilities/{id} [DELETE]
### Response 200
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Xóa cơ sở vật chất",
  "data": null
}
```

## /api/v1/facilities/{id}/condition [PATCH]
### Request
```json
{
  "condition": "DAMAGED"
}
```
### Response 200
```json
{
  "statusCode": 200,
  "error": null,
  "message": "Cập nhật tình trạng cơ sở vật chất",
  "data": {
    "id": 1,
    "name": "Đèn LED 250W",
    "description": "Nâng cấp đèn sân chính",
    "quantity": 10,
    "condition": "DAMAGED",
    "specifications": "250W, LED, IP65",
    "location": "Sân A",
    "category": {
      "id": 1,
      "name": "Hệ thống chiếu sáng"
    },
    "pitch": {
      "id": 1,
      "name": "Sân A"
    },
    "roomId": null,
    "purchaseDate": "2024-06-15",
    "warrantyExpiry": "2027-06-15",
    "purchasePrice": 3200000,
    "createdAt": "2026-03-17T00:50:00Z",
    "updatedAt": "2026-03-17T01:05:00Z",
    "createdBy": "admin@utb.edu.vn",
    "updatedBy": "admin@utb.edu.vn"
  }
}
```
