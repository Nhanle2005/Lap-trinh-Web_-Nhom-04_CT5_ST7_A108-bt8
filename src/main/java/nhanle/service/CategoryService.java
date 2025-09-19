package nhanle.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import nhanle.entity.Category;

public interface CategoryService {

  // ===== Truy vấn phân trang / tìm kiếm =====
  Page<Category> findAll(Pageable pageable);

  Page<Category> findByKeyword(String keyword, Pageable pageable);

  Page<Category> findByStatus(Boolean status, Pageable pageable);

  Page<Category> findByStatusAndKeyword(Boolean status, String keyword, Pageable pageable);

  // ===== CRUD =====
  Category save(Category category);

  Category update(Category category);

  void deleteById(Long id);

  void delete(Category category);

  Optional<Category> findById(Long id);

  boolean existsByCategoryName(String categoryName);

  boolean existsByCategoryNameAndIdNot(String categoryName, Long id);

  List<Category> findActiveCategories();

  long count();

  long countByStatus(Boolean status);

  Category changeStatus(Long id);

  List<Category> findTop10ByOrderByCreatedAtDesc();
}
