# Tài liệu tra cứu nhanh: Quy tắc đặt tên Query Methods trong Spring Data JPA

Việc đặt tên phương thức đúng chuẩn giúp Spring Data JPA tự động tạo câu lệnh SQL mà không cần viết code triển khai.

## 1. Cấu trúc cơ bản
**[Tiền tố]** + **[Thuộc tính Entity]** + **[Điều kiện]**

*Ví dụ:* `findBy` (Tiền tố) + `Email` (Thuộc tính) + `Containing` (Điều kiện) -> `findByEmailContaining(String email)`

---

## 2. Bảng tổng hợp các Keyword điều kiện

| Nhóm | Keyword | Ví dụ phương thức | Giải thích / SQL tương ứng |
| :--- | :--- | :--- | :--- |
| **Cơ bản** | `Is`, `Equals` | `findByUsername(String u)` | `where username = ?` |
| | `IsNot`, `Not` | `findByStatusNot(String s)` | `where status <> ?` |
| **Logic** | `And` | `findByNameAndAge(String n, int a)` | `where name = ? and age = ?` |
| | `Or` | `findByNameOrAge(String n, int a)` | `where name = ? or age = ?` |
| **So sánh số** | `Between` | `findByPriceBetween(double min, double max)` | `where price between ? and ?` |
| | `LessThan` | `findByAgeLessThan(int age)` | `where age < ?` |
| | `GreaterThan` | `findByAgeGreaterThan(int age)` | `where age > ?` |
| | `LessThanEqual` | `findByAgeLessThanEqual(int age)` | `where age <= ?` |
| **Kiểm tra Null** | `IsNull` | `findByEmailIsNull()` | `where email is null` |
| | `IsNotNull`, `NotNull`| `findByEmailIsNotNull()` | `where email is not null` |

---

## 3. Truy vấn Chuỗi (String) - Tìm kiếm linh hoạt

| Keyword | Ví dụ phương thức | SQL tương ứng |
| :--- | :--- | :--- |
| `StartingWith` | `findByNameStartingWith(String prefix)` | `where name like 'prefix%'` |
| `EndingWith` | `findByNameEndingWith(String suffix)` | `where name like '%suffix'` |
| `Containing` | `findByNameContaining(String part)` | `where name like '%part%'` |
| `Like` | `findByNameLike(String pattern)` | `where name like ?` |
| `IgnoreCase` | `findByNameIgnoreCase(String name)` | `where UPPER(name) = UPPER(?)` |

---

## 4. Sắp xếp và Giới hạn (Sorting & Paging)

| Mục đích | Keyword | Ví dụ phương thức |
| :--- | :--- | :--- |
| **Sắp xếp** | `OrderBy` | `findByStatusOrderByCreatedAtDesc(String s)` |
| **Lấy 1 bản ghi** | `First`, `Top` | `findFirstByOrderByAgeAsc()` |
| **Lấy N bản ghi** | `Top10`, `First5` | `findTop10ByAgeGreaterThan(int age)` |
| **Phân trang** | `Pageable` | `findAll(Pageable pageable)` (Tham số truyền vào) |

---

## 5. Các thao tác khác (Delete, Count, Exists)

| Keyword | Ý nghĩa | Ví dụ phương thức |
| :--- | :--- | :--- |
| `Count` | Đếm số lượng | `long countByStatus(String status)` |
| `Delete` | Xóa bản ghi | `void deleteByEmail(String email)` |
| `Exists` | Kiểm tra tồn tại | `boolean existsByUsername(String username)` |
| `Distinct` | Lọc trùng | `List<User> findDistinctByLastName(String name)` |

---

## 6. Lưu ý quan trọng
1. **Tên thuộc tính:** Phải viết hoa chữ cái đầu của thuộc tính trong Entity (Ví dụ: `firstName` -> `findByFirstName`).
2. **Thứ tự tham số:** Phải khớp với thứ tự các thuộc tính xuất hiện trong tên phương thức.
3. **Độ dài:** Nếu tên phương thức quá dài (trên 3 điều kiện), hãy cân nhắc dùng `@Query` để code gọn gàng hơn.