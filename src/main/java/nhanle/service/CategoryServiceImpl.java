package nhanle.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nhanle.entity.Category;
import nhanle.repository.CategoryRepository;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

  private final CategoryRepository repository;

  public CategoryServiceImpl(CategoryRepository repository) {
    this.repository = repository;
  }

  // ===== Truy vấn phân trang / tìm kiếm =====
  @Override
  public Page<Category> findAll(Pageable pageable) {
    return repository.findAll(pageable);
  }

  @Override
  public Page<Category> findByKeyword(String keyword, Pageable pageable) {
    if (keyword == null || keyword.isBlank()) {
      return repository.findAll(pageable);
    }
    return repository.searchByKeyword(keyword.trim(), pageable);
  }

  @Override
  public Page<Category> findByStatus(Boolean status, Pageable pageable) {
    return repository.findByStatus(status, pageable);
  }

  @Override
  public Page<Category> findByStatusAndKeyword(Boolean status, String keyword, Pageable pageable) {
    if (keyword == null || keyword.isBlank()) {
      return repository.findByStatus(status, pageable);
    }
    return repository.findByStatusAndKeyword(status, keyword.trim(), pageable);
  }

  // ===== CRUD =====
  @Override
  public Category save(Category category) {
    if (category == null) {
      throw new IllegalArgumentException("Category không được null");
    }
    // validate tên
    if (category.getCategoryName() == null || category.getCategoryName().isBlank()) {
      throw new IllegalArgumentException("Tên danh mục không được trống");
    }
    if (existsByCategoryName(category.getCategoryName())) {
      throw new IllegalArgumentException("Tên danh mục đã tồn tại: " + category.getCategoryName());
    }
    category.setCategoryId(null); // đảm bảo tạo mới
    return repository.save(category);
  }

  @Override
  public Category update(Category category) {
    if (category == null || category.getCategoryId() == null) {
      throw new IllegalArgumentException("Category và ID không được null");
    }

    Category existed = repository.findById(category.getCategoryId())
        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Category với ID: " + category.getCategoryId()));

    // kiểm tra trùng tên (trừ chính nó)
    if (category.getCategoryName() != null &&
        existsByCategoryNameAndIdNot(category.getCategoryName(), category.getCategoryId())) {
      throw new IllegalArgumentException("Tên danh mục đã tồn tại: " + category.getCategoryName());
    }

    // cập nhật các trường cho phép
    if (category.getCategoryName() != null && !category.getCategoryName().isBlank()) {
      existed.setCategoryName(category.getCategoryName().trim());
    }
    existed.setDescription(category.getDescription());
    existed.setIcon(category.getIcon());
    if (category.getStatus() != null) {
      existed.setStatus(category.getStatus());
    }

    return repository.save(existed);
  }

  @Override
  public void deleteById(Long id) {
    if (id == null) {
      throw new IllegalArgumentException("ID không được null");
    }
    if (!repository.existsById(id)) {
      throw new IllegalArgumentException("Không tìm thấy Category với ID: " + id);
    }
    repository.deleteById(id);
  }

  @Override
  public void delete(Category category) {
    if (category == null || category.getCategoryId() == null) {
      throw new IllegalArgumentException("Category và ID không được null");
    }
    deleteById(category.getCategoryId());
  }

  @Override
  public Optional<Category> findById(Long id) {
    if (id == null) return Optional.empty();
    return repository.findById(id);
  }

  @Override
  public boolean existsByCategoryName(String categoryName) {
    return categoryName != null
        && !categoryName.isBlank()
        && repository.existsByCategoryNameIgnoreCase(categoryName.trim());
  }

  @Override
  public boolean existsByCategoryNameAndIdNot(String categoryName, Long id) {
    return categoryName != null
        && !categoryName.isBlank()
        && id != null
        && repository.existsByCategoryNameIgnoreCaseAndCategoryIdNot(categoryName.trim(), id);
  }

  // ===== Thống kê / tiện ích =====
  @Override
  public List<Category> findActiveCategories() {
    return repository.findByStatus(true, Pageable.unpaged()).getContent();
  }

  @Override
  public long count() {
    return repository.count();
  }

  @Override
  public long countByStatus(Boolean status) {
    if (status == null) return repository.count();
    return repository.countByStatus(status);
  }

  @Override
  public Category changeStatus(Long id) {
    Category c = repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Category với ID: " + id));
    c.setStatus(!Boolean.TRUE.equals(c.getStatus()));
    return repository.save(c);
  }

  @Override
  public List<Category> findTop10ByOrderByCreatedAtDesc() {
    return repository.findTop10ByOrderByCreatedAtDesc();
  }
}
