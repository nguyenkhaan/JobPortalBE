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
## Tổng kết về biến môi trường trong Springboot 
- Chúng ta có thể tách ra một file .env và ghi các biến môi trường vào bên trong đ 
- Chúng ta sẽ thay thế giá trị được ghi trong file .env vào bên trong application.properties 
- Cơ chế chạy: Khi spring boot được chạy, nó sẽ tìm kiếm các biến môi trường được khai báo 
bên trong hệ điều hành, và ghi vào bên trong file application.properties. Nhiệm vụ còn lại của chúng ta là
làm sao chỉnh sửa, thay vì để cho nó lấy các biến môi trường từ hệ tống thì nó sẽ lấy từ file .env đã khai báo

## Cách config 
- Springboot không tự động đọc file .env. Vì vậy, trước tiến chúng ta phải cài đặt một package để làm việc này 
- Package: `implementation("me.paulschwarz:spring-dotenv:4.0.0")`
- Sau đó, bên trong application.property, chúng ta sẽ thêm dòng sau để nó mò đọc file .env 

spring.config.import=optional:file:.env[.properties]

## Build docker image 
- OK, giờ tới một cái ngu dốt. Nếu như chúng ta chỉ làm bình thường, một Dockerfile thật bình thưởng như thế này 
- ```yaml 
FROM gradle:8.7-jdk17 AS build
WORKDIR /app

COPY . .

RUN chmod +x gradlew
RUN ./gradlew clean build

FROM eclipse-temurin:17-jre AS release
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```
- Thì khi build ra nó sẽ báo lỗi. Uh lỗi là không tìm thấy mấy cái biến môi trường để nạp vào code 
- Nguyên nhân: Do code chúng ta đang cấu hình để tìm file .env và đọc. Nhưng docker-compose.yaml lại dùng env_file: .env
Thay vì tạo 1 file .env, mấy biến môi trường sẽ inject thẳng vào Docker Container luôn. Do đó, application 
sẽ không nhìn thấy .env mà chỉ thấy các biến môi trường 
- Giải pháp: Copy luôn cái .env vào container (hơi tục nhưng kệ đi). 