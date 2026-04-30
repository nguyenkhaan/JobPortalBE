# HƯỚNG DẪN SỬ DỤNG SPRINGBOOT 
## JPA 
Là cơ chế để giao tiếp với Database của Spring Boot 

## Một số decorator 
- @Builder() 
- @Column()  
- @Data 
- @Entity 
- @Getter / @Setter 
- @Builder.Default


**Nhận xét**: Kiến trúc thì na ná như NestJS. Cách code OOP (trả đối tượng response, viết type cho request) 
thì lại na ná như FastAPI ... 

Muốn tạo một đối tượng thì phải có constructor. Nhung mà cũng như 
thằng FasstAPI, nó hỗ trợ sẵn decorator rồi nên không cần. Nó cũng có 
repository giống như FastAPI 

Một số lưu ý trong Spring Boot 
## 1. khi thiết lập dự án 
### 1.1. Cài đặt Spring Security 
- JwtService: Chuyển đổi parse token, hàm dùng để extract claim ra từ token payload 
- UserDetail, UserDetailImpls: Trích xuất thông tin và lưu vào Spring Authentication Context 
- Security Config 
- Document: https://freedium-mirror.cfd/https://medium.com/%40sibinraziya/spring-boot-3-spring-security-6-jwt-authentication-and-authorization-e586bc186805
@PreAuthorized("hasRole("")") 
### 1.2, Global Exception 
- Handle exception ở cấp độ global 
- https://freedium-mirror.cfd/https://www.google.com/url?sa=t&source=web&rct=j&opi=89978449&url=https://medium.com/%40roshanfarakate/global-exception-handling-in-spring-boot-712593159a26&ved=2ahUKEwihsOX0sZCUAxXamVYBHU0hKmsQFnoECC4QAQ&usg=AOvVaw205i6g8_mVvAyb0s70VAXp
- Các chú chỉ cần tập trung vào logic ở tầng service thôi, không cần bọc code trong Try Catch nữa. Mọi thứ a lo hết ở Global Exception rồi.

## 1.3. 


## Setup minio chạy bằng Docker thành public 
- Bucket trong minio phải được set thành public thì mới có thể truy cập hình ảnh được 
- Vài trong Shell của minio (truy cập shell này băng lệnh docker exec -it) 

Chạy 2 lệnh sau: 

mc alias set local http://localhost:9000 minioadmin minioadmin

mc anonymous set public local/<your_bucket_name>

Hình ảnh sẽ hiển th ở: http://localhost:9000/<your_bucket>/<your_file>

## 2. Quy ước database: 
- Job Seeker Profile: là profile duy nhất của user
- Resume (CV) : 1 user có nhiều CV. 