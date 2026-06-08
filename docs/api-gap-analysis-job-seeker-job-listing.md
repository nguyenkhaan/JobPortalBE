# Báo Cáo Phân Tích GAP API — Job Listing & Detail cho Job Seeker

**Ngày:** 08/06/2026
**Tác giả:** Cline (AI Analysis)
**Phạm vi:** So sánh hợp đồng FE (`fe-jobseeker-api-contracts.ts`) với Backend Java Spring Boot hiện tại (modules `jobpost`, `employer`)

---

## 1. [API CẦN TẠO MỚI]

### 1.1 API Detail Job trả về `JobDetailType` đầy đủ (bao gồm CompanyProfile + JobOverview)

**Endpoint hiện tại:** `GET /jobpost/{id}` → trả về `JobPostResponse`

**Vấn đề:** Hiện tại BE trả về một DTO duy nhất (`JobPostResponse`) cho **cả list lẫn detail**. FE yêu cầu 2 cấu trúc riêng biệt:
- **List:** `Job` interface (gọn nhẹ, có `daysRemaining`, `salary` là string)
- **Detail:** `JobDetailType` interface (mở rộng, thêm `overview`, `companyProfile`, `website`, `phone`, `email`)

**Kết luận:** ❌ **Cần tạo DTO mới** hoặc mở rộng response cho endpoint detail. BE thiếu hẳn section `companyProfile` và `overview` trong response hiện tại.

### 1.2 API Employer Detail / Public Profile

**FE Interface:** `EmployerDetail` (id, name, logo, category, description, benefits, vision, overview, contact)

**BE hiện tại:** Không có public endpoint cho Employer detail dành cho Job Seeker. `EmployerController` và `AdminEmployerController` chỉ phục vụ employer/admin.

**Kết luận:** ❌ **Cần tạo API mới** (ví dụ: `GET /employers/{id}/profile`) phục vụ public.

### 1.3 API Danh sách Employer (cho Employer Listing Page)

**FE Interface:** `EmployerFilterParams` (keyword, location, category, page, limit)

**BE hiện tại:** Không có endpoint public listing employer.

**Kết luận:** ❌ **Cần tạo API mới** (ví dụ: `GET /employers`).

---

## 2. [TRƯỜNG BỊ THIẾU/LỆCH TRONG DTO]

### 2.1 So sánh `Job` (FE List Card) vs `JobPostResponse` (BE hiện tại)

| # | FE Field | BE Field | Trạng thái | Ghi chú |
|---|----------|----------|-----------|---------|
| 1 | `id` (`string`) | `id` (`Long`) | ✅ OK | FE dùng string, BE dùng Long — cần `.toString()` khi map |
| 2 | `title` | `title` | ✅ OK | |
| 3 | `companyName` | `employer.companyName` | ✅ OK | |
| 4 | `type` (`string`) | `employmentType` (`Enum`) | ⚠️ **Lệch kiểu** | BE trả về enum name (VD: `FULL_TIME`), FE cần string hiển thị (VD: `"Full Time"`) |
| 5 | `isFeatured` (`boolean`) | `isFeatured` (`Boolean`) | ✅ OK | |
| 6 | `logo` | `employer.logo` (qua Minio URL) | ✅ OK | |
| 7 | `location` | `employer.address` (từ EmployerProfile) | ⚠️ **Thiếu mapping** | BE chưa expose `address` của employer vào response |
| 8 | `salary` (`string`) | `salaryMin` + `salaryMax` (`BigDecimal`) | ❌ **Thiếu** | FE cần string như `"15-20 triệu"` hoặc `"Thỏa thuận"` |
| 9 | `daysRemaining` (`string`) | ❌ **Không có** | ❌ **Thiếu** | Phải tính từ `expiresAt` |
| 10 | `experience` (`string?`) | `experience` (`Integer`) | ⚠️ Lệch kiểu | BE là số năm, FE cần string như `"1-2 năm"` |
| 11 | `education` (`string?`) | `educationLevel` (`Enum`) | ⚠️ Lệch kiểu | BE enum, FE cần string |
| 12 | `jobLevel` (`string?`) | `jobLevel` (`Enum`) | ⚠️ Lệch kiểu | BE enum, FE cần string |

### 2.2 So sánh `JobDetailType` (FE Detail) vs `JobPostResponse` (BE hiện tại)

| # | FE Field | BE Field | Trạng thái | Ghi chú |
|---|----------|----------|-----------|---------|
| 1 | `id` | `id` | ✅ OK | |
| 2 | `title` | `title` | ✅ OK | |
| 3 | `companyName` | `employer.companyName` | ✅ OK | |
| 4 | `logo` | `employer.logo` (Minio URL) | ✅ OK | |
| 5 | `type` (`string`) | `employmentType` | ⚠️ Lệch kiểu | |
| 6 | `isFeatured` | `isFeatured` | ✅ OK | |
| 7 | `website` | `employer.companyWebsite` | ⚠️ **Chưa expose** | Có trong EmployerProfile nhưng chưa vào response |
| 8 | `phone` | `employer.phone` | ❌ **Thiếu** | Có trong EmployerProfile nhưng chưa vào response |
| 9 | `email` | `employer.email` | ❌ **Thiếu** | Có trong EmployerProfile nhưng chưa vào response |
| 10 | `expireDate` | `expiresAt` | ✅ OK | Khác tên field (FE: `expireDate`, BE: `expiresAt`) |
| 11 | `description` (`string[]`) | `description` (`TEXT` - single string) | ⚠️ **Lệch kiểu** | FE cần mảng các đoạn văn, BE là 1 string duy nhất — cần split theo `\n\n` |
| 12 | `responsibilities` (`string[]`) | `responsibilities` (`TEXT`) | ⚠️ **Lệch kiểu** | Tương tự, cần split thành mảng |
| 13 | `overview` | ❌ **Thiếu hẳn section** | ❌ **Cần tạo mới** | |
| 14 | `companyProfile` | ❌ **Thiếu hẳn section** | ❌ **Cần tạo mới** | |

### 2.3 Chi tiết section `overview` (thiếu toàn bộ)

| FE Field | Nguồn BE | Trạng thái |
|----------|---------|-----------|
| `postedDate` | `createdAt` | ✅ Có thể map |
| `expireIn` | Tính từ `expiresAt` | ❌ Cần tính toán |
| `education` | `educationLevel` | ⚠️ Cần string mapping |
| `salary` | `salaryMin` + `salaryMax` | ❌ Cần format string |
| `location` | `employer.address` | ❌ Chưa expose |
| `jobType` | `employmentType` | ⚠️ Cần string mapping |
| `experience` | `experience` | ⚠️ Cần string mapping |

### 2.4 Chi tiết section `companyProfile` (thiếu toàn bộ)

| FE Field | Nguồn BE | Trạng thái |
|----------|---------|-----------|
| `industry` | `employer.industry` (String) | ❌ Chưa expose trong response |
| `foundedIn` | `employer.founded` (LocalDate) | ❌ Chưa expose |
| `orgType` | `employer.organizationType` (Enum) | ❌ Chưa expose |
| `companySize` | `employer.teamSize` (String) | ❌ Chưa expose |

### 2.5 So sánh `JobFilterParams` (FE) vs `JobPostFilterRequest` (BE)

| FE Field | BE Filter Field | Trạng thái |
|----------|---------------|-----------|
| `keyword` | `keyword` | ✅ OK |
| `location` | ❌ **Không có** | ❌ Thiếu |
| `category` | `industryIds` (`List<Long>`) | ⚠️ FE là string category name, BE là list industry ID |
| `jobType` | ❌ **Không có** | ❌ Thiếu |
| `salaryMin` | `salaryMin` | ✅ OK |
| `salaryMax` | `salaryMax` | ✅ OK |
| `experience` | ❌ **Không có** | ❌ Thiếu |
| `salaryRange` | ❌ **Không có** | ❌ Thiếu |
| `jobTypes[]` | ❌ **Không có** | ❌ Thiếu |
| `education[]` (array) | `educationLevel` (single enum) | ⚠️ Lệch kiểu |
| `jobLevel` | `jobLevel` | ✅ OK |
| `page` | `offset` (offset = page × limit) | ⚠️ Cần convert |
| `limit` | `limit` | ✅ OK |

---

## 3. [ĐỀ XUẤT MAPPING]

### 3.1 Các trường ảo cần tính toán

| Trường FE | Công thức đề xuất | Ghi chú |
|-----------|------------------|---------|
| `daysRemaining` | `ChronoUnit.DAYS.between(LocalDate.now(), expiresAt.toLocalDate()) + " ngày trước"` | Nếu còn >1 ngày. Nếu hết hạn: `"Đã hết hạn"` |
| `expireIn` | Tương tự `daysRemaining` nhưng format khác: `"Còn 5 ngày"` | |
| `salary` | `salaryMin + " - " + salaryMax + " " + salaryType.getDisplayName()` | VD: `"15.000.000 - 20.000.000 VND / tháng"` |
| `postedDate` | `TimeAgo.format(createdAt)` | VD: `"3 ngày trước"` |
| `foundedIn` | `employer.founded.getYear()` | FE cần string năm thành lập |
| `companySize` | `employer.teamSize` | Đã có sẵn, chỉ cần expose |

### 3.2 Xử lý enum → string display name

Các enum BE hiện tại dùng `.name()` (VD: `FULL_TIME`) trong khi FE cần display name (VD: `"Full Time"`). Cần:
- Sử dụng annotation `@JsonValue` trên enum field `displayName`
- HOẶC map thủ công trong Service/DTO

Các enum cần xử lý:
- `EmploymentType` → `type` / `jobType`
- `EducationLevel` → `education`
- `JobLevel` → `jobLevel`
- `OrganizationType` → `orgType`
- `SalaryType` → dùng trong format `salary`

### 3.3 Xử lý TEXT → string[] cho description & responsibilities

FE cần `description: string[]` và `responsibilities: string[]` (mảng các đoạn). BE lưu TEXT dạng string với dấu xuống dòng.

**Đề xuất:** Tách bằng `\\n\\n` (double newline) hoặc dùng markdown list với dấu `-` hoặc số.

---

## 4. [TÓM TẮT MỨC ĐỘ ƯU TIÊN]

### P0 — Phải làm ngay (block FE)
1. ✅ Tách riêng response DTO cho **list** (`Job`) và **detail** (`JobDetailType`)
2. ✅ Thêm section `companyProfile` (industry, foundedIn, orgType, companySize) vào detail
3. ✅ Thêm section `overview` (postedDate, expireIn, education, salary, location, jobType, experience) vào detail
4. ✅ Expose `phone`, `email`, `website` của employer trong detail response
5. ✅ Thêm field `daysRemaining` vào list response
6. ✅ Thêm field `location` (address) vào cả list và detail response
7. ✅ Format `salary` thành string thay vì 2 số riêng biệt

### P1 — Cần làm trong sprint này
1. ✅ Thêm filter params: `location`, `jobType`, `experience`, `jobTypes[]`, `education[]`
2. ✅ Chuyển `educationLevel` từ single enum sang hỗ trợ multiple trong filter
3. ✅ Xử lý `category` trong filter — nếu FE gửi string thì BE cần lookup Industry
4. ✅ Hỗ trợ `page` param bên cạnh `offset`

### P2 — Có thể làm sau
1. ✅ Tạo API Employer Detail (`GET /employers/{id}/profile`)
2. ✅ Tạo API Employer Listing (`GET /employers`)

---

## 5. [FILE CẦN THAY ĐỔI — DỰ KIẾN]

| File | Thay đổi |
|------|---------|
| `JobPostResponse.java` | ⚠️ Giữ cho list, hoặc tạo DTO mới riêng cho list |
| `JobPostDetailResponse.java` | 🆕 Tạo mới — chứa đầy đủ `JobDetailType`, `JobOverview`, `CompanyProfile` |
| `JobPostFilterRequest.java` | ⚠️ Thêm các filter còn thiếu |
| `JobPostService.java` | ⚠️ Cập nhật `toResponse()`, thêm logic filter mới, thêm query phân trang mới |
| `JobPostController.java` | ⚠️ Có thể cần thêm endpoint mới hoặc sửa response hiện tại |

---

*Báo cáo kết thúc. Chờ quyết định của bạn trước khi triển khai code.*