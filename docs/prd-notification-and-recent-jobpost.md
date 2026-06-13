# PRD: Notification Expansion and Recent Job Posts

## 1. Mục tiêu

Backend hiện đã có sẵn hệ thống notification và các module nghiệp vụ chính đang chạy ổn định.  
Mục tiêu của đợt chỉnh sửa này là:

- Bổ sung notification vào đúng các điểm nghiệp vụ quan trọng
- Chuẩn hóa việc gửi notification từ tầng service
- Mở thêm API trả về danh sách `JobPost` mới trong 7 ngày gần đây cho `JobSeeker` đã đăng nhập

## 2. Phạm vi triển khai

Phạm vi của PRD này gồm 2 nhóm tính năng:

1. Mở rộng notification cho các nghiệp vụ hiện có
2. Bổ sung endpoint recent job posts trong module `jobpost`

Ngoài phạm vi:

- Không xây recommendation engine cá nhân hóa
- Không thay đổi cấu trúc database notification hiện tại nếu không thật sự cần
- Không thay đổi response contract hiện tại của các API cũ ngoài phần bổ sung notification side-effect

## 3. Quyết định đã chốt

- Tất cả notification phải được gửi qua cả `IN_APP` và `DEVICE`
- Nội dung notification phải viết bằng tiếng Anh
- Notification phải được đặt trong tầng `Service`, không đặt logic nghiệp vụ trong `Controller`
- Trường hợp `JobSeeker` apply job phải hỗ trợ ở cả 2 luồng:
  - `JobSeekerService.applyJob(...)`
  - `JobApplicationService.createJobApplication(...)`
- Notification cho trạng thái hồ sơ ứng tuyển chỉ gửi khi `ACCEPTED` hoặc `REJECTED`
- Notification cho `Employer Profile` approval phải gửi ở cả `APPROVED` và `REJECTED`
- “Employer thanh toán thì báo cho admin” được hiểu là mốc `confirmPayment(...)`
- Người nhận notification phía admin là toàn bộ `User` có role `ADMIN`
- API recent job posts chỉ dành cho `JobSeeker` đã đăng nhập
- API recent job posts tái sử dụng `JobPostResponse`
- API recent job posts hỗ trợ `limit` và `offset`

## 4. Functional Requirements

### 4.1. Notification cho Employer Profile creation

Khi employer tạo mới `EmployerProfile` thành công trong `EmployerService.createEmployer(...)`:

- Hệ thống phải gửi notification đến toàn bộ `ADMIN`
- Notification phải được phát từ tầng service thông qua notification flow hiện có
- Nội dung gợi ý:
  - Title: `New employer profile submitted`
  - Message: `A new employer profile from {companyName} is waiting for review.`
- `targetUrl` nên trỏ đến màn hình admin employer review

### 4.2. Notification cho Employer approval decision

Khi admin cập nhật `approvalStatus` trong `EmployerService.updateApprovalStatus(...)`:

- Nếu `APPROVED`, gửi notification cho employer sở hữu profile
- Nếu `REJECTED`, gửi notification cho employer sở hữu profile
- Không yêu cầu gửi notification cho trạng thái `PENDING`
- Nội dung gợi ý:
  - Approved title: `Employer profile approved`
  - Approved message: `Your employer profile has been approved by the admin.`
  - Rejected title: `Employer profile rejected`
  - Rejected message: `Your employer profile has been rejected by the admin. Please review the feedback and update your profile.`
- Nếu có `rejectionReason`, hệ thống có thể nối thêm vào message hoặc hiển thị ở màn hình đích

### 4.3. Notification cho Employer payment submission

Khi employer xác nhận đã chuyển khoản trong `PaymentService.confirmPayment(...)`:

- Hệ thống phải gửi notification đến toàn bộ `ADMIN`
- Đây là thời điểm duy nhất được xem là “employer submitted payment for review”
- Nội dung gợi ý:
  - Title: `New payment submitted`
  - Message: `Employer {companyName} submitted a payment for the {planName} plan and is waiting for approval.`
- `targetUrl` nên trỏ đến màn hình admin payment review

Lưu ý nghiệp vụ:

- Trong code hiện tại, `confirmPayment(...)` vẫn giữ `PaymentStatus` ở `PENDING`
- PRD này không yêu cầu thêm trạng thái `CONFIRMED`
- Nếu team muốn chuẩn hóa flow status sau này thì xử lý ở task riêng

### 4.4. Notification cho Admin payment approval

Khi admin duyệt thanh toán thành công trong `PaymentService.approvePayment(...)`:

- Hệ thống phải gửi notification cho employer đã thanh toán
- Nội dung gợi ý:
  - Title: `Payment approved`
  - Message: `Your payment for the {planName} plan has been approved successfully.`
- `targetUrl` nên trỏ đến trang billing/subscription của employer

### 4.5. Notification cho Job Application submission

Khi `JobSeeker` tạo mới `JobApplication`, hệ thống phải gửi notification cho employer sở hữu `JobPost`.

Phạm vi bắt buộc:

- `JobSeekerService.applyJob(...)`
- `JobApplicationService.createJobApplication(...)`

Yêu cầu:

- Mỗi lần submit thành công chỉ gửi đúng một notification cho employer liên quan
- Nội dung gợi ý:
  - Title: `New job application received`
  - Message: `{jobSeekerName} applied for your job post {jobTitle}.`
- `targetUrl` nên trỏ đến màn hình employer xem danh sách ứng viên hoặc chi tiết application

### 4.6. Notification cho Application decision

Khi employer cập nhật trạng thái application trong `JobApplicationService.updateApplicationStatusForEmployer(...)`:

- Nếu trạng thái mới là `ACCEPTED`, gửi notification cho `JobSeeker`
- Nếu trạng thái mới là `REJECTED`, gửi notification cho `JobSeeker`
- Không gửi notification cho các trạng thái khác như `PENDING`, `REVIEWING`, `INTERVIEW`, `OFFER`

Nội dung gợi ý:

- Accepted title: `Application accepted`
- Accepted message: `Your application for {jobTitle} has been accepted by the employer.`
- Rejected title: `Application rejected`
- Rejected message: `Your application for {jobTitle} has been rejected by the employer.`

## 5. Notification Integration Rules

### 5.1. Kênh gửi

Mọi notification trong phạm vi PRD này phải dùng đồng thời:

- `Channel.IN_APP`
- `Channel.DEVICE`

### 5.2. Cách tích hợp

- Ưu tiên dùng `NotificationPublisher` hoặc một helper dùng chung trong service layer
- Không gọi trực tiếp logic test trong `NotificationController`
- Không hardcode logic notification vào controller
- Nội dung message phải nằm gần business action để dễ bảo trì

### 5.3. Notification types

Code hiện tại mới có một số `NotificationType` cơ bản.  
Đợt triển khai này được phép:

- Bổ sung thêm `NotificationType` mới nếu cần để phản ánh đúng nghiệp vụ
- Giữ naming rõ ràng theo business event, ví dụ:
  - `EMPLOYER_PROFILE_SUBMITTED`
  - `EMPLOYER_PROFILE_APPROVED`
  - `EMPLOYER_PROFILE_REJECTED`
  - `PAYMENT_SUBMITTED`
  - `PAYMENT_APPROVED`
  - `APPLICATION_ACCEPTED`
  - `APPLICATION_REJECTED`

## 6. Recent Job Posts API

### 6.1. Mục tiêu

Tạo thêm một API trong module `jobpost` để trả về danh sách `JobPost` mới trong vòng 7 ngày gần nhất cho `JobSeeker` đã đăng nhập.

### 6.2. API contract đề xuất

- Method: `GET`
- Route đề xuất: `/jobpost/recent`
- Authorization: `hasRole('SEEKER')`
- Query params:
  - `limit` default `20`
  - `offset` default `0`
- Response:
  - `ApiResponse<PageResponse<JobPostResponse>>`

### 6.3. Luật lọc dữ liệu

Danh sách trả về phải thỏa các điều kiện:

- `JobPost.createdAt >= now - 7 days`
- `JobPost.status = OPEN`
- `EmployerProfile.approvalStatus = APPROVED`
- `EmployerProfile.active = true`
- Job chưa hết hạn ứng tuyển tại thời điểm query

### 6.4. Luật sắp xếp

- Sắp xếp `createdAt DESC`
- Job mới nhất đứng trước

### 6.5. Tái sử dụng response

- Không tạo DTO rút gọn mới
- Tái sử dụng `JobPostResponse` để frontend dùng ngay

## 7. Affected Modules

Các khu vực dự kiến bị tác động:

- `modules/notification`
- `events/notification`
- `modules/employer`
- `modules/payment`
- `modules/jobseeker`
- `modules/jobapplication`
- `modules/jobpost`
- Có thể cần cập nhật `repository` hoặc thêm query hỗ trợ cho recent jobs

## 8. Acceptance Criteria

### 8.1. Notification

- Khi employer tạo `EmployerProfile`, tất cả admin nhận được notification qua `IN_APP` và `DEVICE`
- Khi admin duyệt hoặc từ chối `EmployerProfile`, employer nhận được notification tương ứng qua `IN_APP` và `DEVICE`
- Khi employer gọi `confirmPayment(...)`, tất cả admin nhận được notification qua `IN_APP` và `DEVICE`
- Khi admin duyệt payment, employer nhận được notification qua `IN_APP` và `DEVICE`
- Khi `JobSeeker` apply job ở cả 2 luồng hiện có, employer nhận được notification qua `IN_APP` và `DEVICE`
- Khi employer chuyển application sang `ACCEPTED` hoặc `REJECTED`, job seeker nhận được notification tương ứng qua `IN_APP` và `DEVICE`
- Tất cả notification mới đều dùng tiếng Anh

### 8.2. Recent job posts

- `JobSeeker` đã đăng nhập gọi được API recent jobs
- User không có role `SEEKER` không truy cập được API này
- API chỉ trả về job được tạo trong 7 ngày gần đây
- API hỗ trợ `limit` và `offset`
- API trả về đúng `JobPostResponse` theo format hiện có

## 9. Ghi chú triển khai

- Ưu tiên thay đổi tối thiểu, không phá vỡ API hiện tại
- Nên gom logic tạo notification event vào helper/private method để tránh lặp code giữa các service
- Cần tránh gửi trùng notification trong cùng một business action
- Sau khi triển khai nên bổ sung test cho:
  - employer profile notifications
  - payment notifications
  - application notifications
  - recent job posts filtering
