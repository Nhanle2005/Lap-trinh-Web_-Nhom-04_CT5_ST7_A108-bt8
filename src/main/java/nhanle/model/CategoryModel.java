package nhanle.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class CategoryModel {

  private Long categoryId;

  @NotBlank(message = "Tên danh mục không được trống")
  @Size(max = 200, message = "Tối đa 200 ký tự")
  private String categoryName;

  @Size(max = 500, message = "Tối đa 500 ký tự")
  private String description;

  private String icon;
  private Boolean status = true;
  private boolean edit;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // ===== Getter & Setter =====
  public Long getCategoryId() { return categoryId; }
  public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

  public String getCategoryName() { return categoryName; }
  public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public String getIcon() { return icon; }
  public void setIcon(String icon) { this.icon = icon; }

  public Boolean getStatus() { return status; }
  public void setStatus(Boolean status) { this.status = status; }

  public boolean isEdit() { return edit; }
  public void setEdit(boolean edit) { this.edit = edit; }

  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

  public LocalDateTime getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
