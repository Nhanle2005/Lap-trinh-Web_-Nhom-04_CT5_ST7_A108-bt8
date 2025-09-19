package nhanle.constant;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class UploadProps {
    /**
     * Thư mục lưu file upload, cấu hình ở application.properties: app.upload-dir=...
     */
    private String uploadDir;

    public String getUploadDir() { return uploadDir; }
    public void setUploadDir(String uploadDir) { this.uploadDir = uploadDir; }
}
