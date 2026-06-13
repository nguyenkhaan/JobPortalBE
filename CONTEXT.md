# Job Portal Backend

Ngữ cảnh này quản lý nghiệp vụ tuyển dụng giữa Job Seeker, Employer, Admin, Job Post, Job Application và các tác vụ hỗ trợ như approval, payment, notification.

## Language

**Job Application**:
Một hồ sơ ứng tuyển của một Job Seeker vào một Job Post cụ thể, gắn với đúng một Resume.
_Avoid_: Apply job, application record

**Application Submission**:
Hành động tạo mới một Job Application từ phía Job Seeker, bất kể đi qua luồng API ứng tuyển nào.
_Avoid_: Apply, submit CV

**Application Decision**:
Kết quả xử lý Job Application từ phía Employer được dùng để thông báo cho Job Seeker trong phạm vi hiện tại chỉ gồm `ACCEPTED` và `REJECTED`.
_Avoid_: Review state, processing step

**Recent Job Recommendation**:
Danh sách Job Post mới được tạo trong vòng 7 ngày gần nhất, chỉ dành cho Job Seeker đã đăng nhập và không bao gồm cá nhân hóa trong phạm vi tính năng hiện tại.
_Avoid_: Public recent jobs, trending jobs, personalized recommendation

**Employer Approval Decision**:
Kết quả xét duyệt Employer Profile từ phía Admin, trong phạm vi hiện tại cần thông báo cho Employer ở cả `APPROVED` và `REJECTED`.
_Avoid_: Employer activation, account verification

**Employer Payment Submission**:
Mốc Employer xác nhận đã chuyển khoản và gửi yêu cầu chờ Admin duyệt thanh toán, tương ứng với luồng `confirmPayment`.
_Avoid_: Checkout creation, payment approval

**Admin Recipient Group**:
Nhóm người nhận thông báo quản trị trong phạm vi hiện tại là toàn bộ User có role `ADMIN`.
_Avoid_: Assigned admin, approval owner
