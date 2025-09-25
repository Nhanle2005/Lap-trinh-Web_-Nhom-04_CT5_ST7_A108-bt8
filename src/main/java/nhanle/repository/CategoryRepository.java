package nhanle.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import nhanle.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  // Tìm theo tên
  Page<Category> findByCategoryNameContainingIgnoreCase(String name, Pageable pageable);

  // Tìm theo mô tả
  Page<Category> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);

  // Tìm theo từ khoá (tên hoặc mô tả)
  @Query("""
         SELECT c FROM Category c
         WHERE LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :kw, '%'))
            OR LOWER(c.description)  LIKE LOWER(CONCAT('%', :kw, '%'))
         """)
  Page<Category> searchByKeyword(@Param("kw") String keyword, Pageable pageable);

  // Theo trạng thái
  Page<Category> findByStatus(Boolean status, Pageable pageable);

  // Trạng thái + từ khoá
  @Query("""
         SELECT c FROM Category c
         WHERE c.status = :status AND (
           LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :kw, '%'))
           OR LOWER(c.description) LIKE LOWER(CONCAT('%', :kw, '%'))
         )
         """)
  Page<Category> findByStatusAndKeyword(@Param("status") Boolean status,
                                        @Param("kw") String keyword,
                                        Pageable pageable);

  boolean existsByCategoryNameIgnoreCase(String categoryName);

  @Query("""
         SELECT COUNT(c) > 0 FROM Category c
         WHERE LOWER(c.categoryName) = LOWER(:name) AND c.categoryId <> :id
         """)
  boolean existsByCategoryNameIgnoreCaseAndCategoryIdNot(@Param("name") String name,
                                                         @Param("id") Long id);

  long countByStatus(Boolean status);

  List<Category> findTop10ByOrderByCreatedAtDesc();
  
  List<Category> findByStatus(Boolean status);
}
