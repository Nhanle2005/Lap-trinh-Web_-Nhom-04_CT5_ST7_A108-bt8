# Lap-trinh-Web_-Nhom-04_CT5_ST7_A108-bt8
## Hướng dẫn setup và chạy ứng dụng

1. Cấu hình Database
- Tạo database MySQL với tên: `category_management`
- Cập nhật thông tin kết nối database trong `application.properties`:
  ```properties
  spring.datasource.username=user
  spring.datasource.password=pass123
  ```

2. Tạo thư mục upload
- Tạo thư mục: `D:/upload/category-images/`
- Hoặc thay đổi đường dẫn trong `application.properties`:
  ```properties
  app.upload-dir=D:/upload/category-images
  ```

3. Thêm hình ảnh profile
- Thêm file hình ảnh của bạn vào: `src/main/resources/static/image/profile.jpg`
- Hoặc cập nhật đường dẫn trong `application.properties`:
  ```properties
  app.header-image=/image/profile.jpg
  ```

4. Chạy ứng dụng
mvn spring-boot:run


5. Truy cập ứng dụng
- URL: http://localhost:8092/admin/categories
- Trang quản lý danh mục với đầy đủ chức năng CRUD, search, pagination
- URL: http://localhost:8092/api/categories //xem api
- URL: http://localhost:8092/api/products //xem api
