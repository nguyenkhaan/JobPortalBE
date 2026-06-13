# Cấu trúc hiện tại của dự án `JobPortalBE`

## 1. Tổng quan

Đây là backend của hệ thống tuyển dụng, viết bằng **Spring Boot + Gradle + Java 17**.  
Cấu trúc dự án đang đi theo hướng:

- **Tách theo domain/module nghiệp vụ** trong thư mục `modules`
- **Dùng tầng chung** cho cấu hình, bảo mật, exception, model, event
- **Entity đặt tập trung** trong `models`, còn xử lý nghiệp vụ đặt trong từng module

Nói ngắn gọn: dự án đang kết hợp **module-based** và **layered architecture**.

## 2. Cây thư mục chính

```text
JobPortalBE/
├── src/
│   ├── main/
│   │   ├── java/Cloudian/JobPortal/
│   │   │   ├── JobPortalApplication.java
│   │   │   ├── commons/
│   │   │   ├── configs/
│   │   │   ├── events/
│   │   │   ├── exceptions/
│   │   │   ├── filters/
│   │   │   ├── models/
│   │   │   ├── modules/
│   │   │   ├── scripts/
│   │   │   ├── security/
│   │   │   └── utilis/
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-docker.yml
│   │       ├── firebase-service-account.json
│   │       └── templates/email/
│   └── test/
│       └── java/Cloudian/JobPortal/
├── docs/
├── Dockerfile
├── docker-compose.yaml
├── build.gradle
└── settings.gradle
```

## 3. Ý nghĩa từng phần

### `src/main/java/Cloudian/JobPortal`

- `JobPortalApplication.java`: điểm khởi động chính của ứng dụng Spring Boot
- `commons/`: các thành phần dùng chung như `constants`, `enums`, `bases`, `middlewares`
- `configs/`: cấu hình hệ thống như `SecurityConfig`, `MinioConfig`, `FirebaseConfig`, `PayOSConfig`, multipart, seeder
- `events/`: xử lý event nội bộ, hiện thấy dùng cho notification
- `exceptions/`: exception custom và xử lý lỗi global
- `filters/`, `security/`: JWT, Spring Security, phân quyền, filter xác thực
- `models/`: các entity/domain model của hệ thống như `User`, `JobPost`, `JobApplication`, `Resume`, `Payment`, `Notification`...
- `scripts/`: tác vụ hỗ trợ như seeding dữ liệu
- `utilis/`: helper tiện ích

### `src/main/java/Cloudian/JobPortal/modules`

Đây là khu vực quan trọng nhất, chứa các module nghiệp vụ.  
Mỗi module thường có các lớp quen thuộc như:

- `Controller`: nhận request API
- `Service`: xử lý nghiệp vụ
- `Repository`: thao tác dữ liệu
- `dto/`: request/response object

Các nhóm module hiện có:

- Nhóm tài khoản và xác thực: `auth`, `user`, `role`, `token`, `social`, `devicetoken`
- Nhóm nghiệp vụ tuyển dụng: `jobseeker`, `employer`, `jobpost`, `jobapplication`, `resume`, `industry`, `jobindustry`, `savedcandidate`
- Nhóm hệ thống/phụ trợ: `notification`, `notificationchannel`, `email`, `audit`, `payment`, `minio`, `health`, `admin`, `test`, `base`

### `src/main/resources`

- `application.properties`: cấu hình chạy chính
- `application-docker.yml`: cấu hình khi chạy bằng Docker
- `templates/email/`: template email như reset password, verify register
- `firebase-service-account.json`: cấu hình Firebase Admin

### `src/test`

Hiện đã có test cho một số phần chính:

- `jobseeker`
- `jobpost`
- `payment`
- test khởi động ứng dụng

## 4. Root project

- `build.gradle`: khai báo dependency và build bằng Gradle
- `settings.gradle`: tên project
- `Dockerfile`: đóng gói ứng dụng thành image
- `docker-compose.yaml`: dựng nhanh môi trường local gồm API, PostgreSQL, MinIO, Mailpit, Adminer
- `docs/`: tài liệu phân tích và ghi chú kiến trúc/API

## 5. Nhận xét nhanh về cấu trúc hiện tại

Điểm dễ hiểu của dự án là phần nghiệp vụ đã được tách khá rõ theo module.  
Tuy nhiên, `models` vẫn đang đặt tập trung ở một nơi, nên kiến trúc hiện tại phù hợp để gọi là:

**Backend Spring Boot theo module nghiệp vụ, dùng chung entity/model và các tầng hỗ trợ toàn hệ thống.**
