## Hướng dẫn setup và chạy ứng dụng

### 1. Cấu hình Database
- Tạo database MySQL với tên: `category_management`
- Cập nhật thông tin kết nối database trong `application.properties`:
  ```properties
  spring.datasource.username=root
  spring.datasource.password=123456
  ```

### 2. Tạo thư mục upload
- Tạo thư mục: `D:/upload/category-images/`
- Hoặc thay đổi đường dẫn trong `application.properties`:
  ```properties
  app.upload-dir=D:/upload/category-images
  ```

### 3. Thêm hình ảnh profile
- Thêm file hình ảnh của bạn vào: `src/main/resources/static/image/profile.jpg`
- Hoặc cập nhật đường dẫn trong `application.properties`:
  ```properties
  app.header-image=/image/profile.jpg
  ```

### 4. Chạy ứng dụng
```bash
mvn spring-boot:run
```

### 5. Truy cập ứng dụng
- URL: http://localhost:8092/admin/categories
- Trang quản lý danh mục với đầy đủ chức năng CRUD, search, pagination

### 6. Tính năng chính
- ✅ CRUD operations (Create, Read, Update, Delete)
- ✅ Tìm kiếm theo tên/mô tả với pagination
- ✅ Upload và quản lý icon cho danh mục
- ✅ Thymeleaf Layout Dialect với header/footer
- ✅ Bootstrap responsive design
- ✅ Validation và error handling
- ✅ Thông tin sinh viên trong footer

### 7. Cấu trúc database
```sql
CREATE TABLE categories (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    icon VARCHAR(255),
    status BOOLEAN DEFAULT TRUE,
    created_at DATETIME,
    updated_at DATETIME
);
```