# HƯỚNG DẪN SỬ DỤNG PUSH NOTIFICATION SERVICES 

## 1. Giới thiệu tổng quan 
- Hệ thống triển khai hai loại push notification là IN_APP và DEVICE
  - IN_APP: Đẩy thông báo trong chính web application. Người dùng nhấn vào nút chuông để xem 
  - DEVICE: Đẩy thông báo về thiết bị của người dùng 

## Thiết kế database 
- Bổ sung trường fcmToken vào bên trong bảng Users. Giúp lưu giữ thiết bị (Device Token) của người dùng đó. 
- Bảng Notification: Chứa title, targetUrl, description của các notification đã gửi 
- Bang NotificationChannel: Thông tin chi tiết về từng Notification (Loại, trạng thái) 

## In App Notification 
- Bản chất là CRUD cơ bản, méo có gì để nói 

## Device Notification 
- Sử dụng Firebase để đẩy notification

### Bước 1. Tạo Firebase Project trên Firebase 
- Truy cập https://firebase.google.com/products/extensions?utm_source=google&utm_medium=cpc&utm_campaign=Cloud-SS-DR-Firebase-FY26-global-gsem-1713590&utm_content=text-ad&utm_term=KW_firebase&gclsrc=aw.ds&gad_source=1&gad_campaignid=23417478209&gbraid=0AAAAADpUDOj7UOwmTbCS6RMlQnpX_SWvY&gclid=Cj0KCQjw_vnQBhCxARIsADcZyxK9v_hIZiMeCAF3bP6on_EpUmAxQqd77GbDs3CpBddtw_9MnsE5xjQaAtbBEALw_wcB
- Tạo một Project Firebase 
- Sau khi tạo, hãy vào Project Settings -> Service Account -> Dowload file credential.json và đặt vào bên trong
folder gốc của dự án 
- Nhớ thêm cái này vào bên trong file .gitignore. 
### Bước 2. Cấu hình cho Backend 
- Cấu hình để Backend có thể sử dụng Firebase SDK. Cái này tùy ngôn ngữ (Java, Python, Js...). Mỗi framework 
sẽ có một cách riêng, nhưng mà chung quy tụi nó đều phải dùng file firebase-credential.json

### Bước 3. Viết logic để triển khai Notification 
- Trong hệ thống Backend sẽ có nhiều Notification, mỗi cái dùng cho một trường hợp khác nhau. Việc chúng ta viết
thuần chay sẽ dẫn đến việc lặp code, xấu vcl 
- Để giải quyết, tôi đề xuất một hướng, đó là sử dụng sự kiện (event). Chúng ta sẽ gồm có một số cái như sau: 
    + Notification Publisher: Trung tâm sự kiện, tiến hành phát sự kiện gửi notification cho toàn bộ hệ thống, chứa hàm publish để phát ra sự kiện 
    + Notification Listener: Lắng nghe sự kiện phát ra 
    + Notification Service: Hàm xử lý logic chính, nó sẽ làm các nhiệm vụ để đẩy notification: gọi Firebase Message để push notification, lưu sự kiện vào databaase... 
- Luồng hoạt động: Giả sử trong một hàm service nào đó, ví dụ: createPayment() cần phải đẩy thông báo. 
    + Gọi hàm NotificationPublisher.publish() để phát sự kiện cho toàn hệ thống Backend. Nó sẽ truyền thêm 
    data chính là body của sự kiện, bao gồm: (userId - xác định đối tượng sẽ nhận sự kiện => fcmToken, notification body, channel)
    + NotificationListener: Lắng nghe được sự kiện và gọi Notification Service 
    + Notification Service được gọi, nó sẽ lưu thoong tin cơ bản của sự kiện vào bảng Notification trước.
        Sau đó, quét qua trường channel trong thông tin nhận được, xác định sự kiện này 
        là IN_APP, DEVICE hay có cả hai? Rồi gọi tiếp hàm sendInAppNotification() hoặc sendFirebaseNotification() 

### Bước 4. Lấy device token 
- Sau khi hoàn thành các hàm trên, chúng ta phải tiến hành testing 
- Để lấy device token, chúng ta phải có một Frontend Web app, hoặc một IOS Android Application... Gọi lên Firebase để
lấy được device token của thiết bị, rồi lưu vào trường FcmToken của User.

##### Tạo Application trong Firebase Project 
- Hãy quay lại Firebase Project, bây giờ chúng ta sẽ tạo thêm một App vào trong Project này 
- hãy chọn đúng loại app: Web, mobile or etc... 
- Khi tạo App thành công, chúng ta sẽ được cấp các trường thông tin liên quan đến application này. Mấy 
cái này sẽ giúp Firebase xác định app này thuộc Project nào và sẽ đẩy notification 
- Tạo vapidKey: Project Settings -> Cloud Messaging -> Generate Key Pair 

#### Tạo một trang Frontend để Demo 
- HTML + Live server -> Viết 1 cái trang HTML đơn giản để lấy Device Token. Tôi gọi Project này là project_lay_device
- Project để lấy được Device Token tôi sẽ gửi ở trên Zalo, nói chung là do tôi nắm (hoặc bạn có thể prompt GPT kêu nó tạo một cái) 
- Dán mấy cái trường do Firebase cung cấp vào bên trong project_lay_device. Kèm với vapid Key 
- Chạy project bằng liveserver, sửa thành: http://localhost:5050/..... (không để 127.0.0.1)

- Có device token rồi, nạp vào databaase thôi. 

### Bước 5. Testing 
- Tạo một route trong controller đơn giản để tiến hành testing. 
- Route của tôi: http://localhost:8080/api/notification (POST), gửi kèm JWT Token để xác định người nhận 
- Route testing này sẽ nhận vào JWT Token, giải Token ra để tìm ra người dùng => Lấy được Device Token 
- Bấm send để gửi request lên trên Backend. Bùm, OK rồi đó. Lỗi thì prompt GPT cho nó fix.  