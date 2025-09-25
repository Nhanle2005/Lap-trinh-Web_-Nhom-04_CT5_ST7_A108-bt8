package nhanle.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import nhanle.entity.Category;
import nhanle.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByStatus(Boolean status);
    
    List<Product> findByCategoryAndStatus(Category category, Boolean status);
    
    List<Product> findByProductNameContainingIgnoreCase(String productName);
    
    @Query("SELECT p FROM Product p WHERE p.status = :status AND p.productName LIKE %:keyword%")
    List<Product> findByStatusAndKeyword(@Param("status") Boolean status, @Param("keyword") String keyword);
    
    @Query("SELECT p FROM Product p WHERE p.category.categoryId = :categoryId AND p.status = :status")
    List<Product> findByCategoryIdAndStatus(@Param("categoryId") Long categoryId, @Param("status") Boolean status);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.categoryId = :categoryId")
    Long countByCategoryId(@Param("categoryId") Long categoryId);
    
    // Pageable methods
    Page<Product> findByStatus(Boolean status, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.productName LIKE %:keyword% OR p.description LIKE %:keyword%")
    Page<Product> findByProductNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
        @Param("keyword") String productName, @Param("keyword") String description, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.status = :status AND (p.productName LIKE %:keyword% OR p.description LIKE %:keyword%)")
    Page<Product> findByStatusAndKeyword(@Param("status") Boolean status, @Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.category.categoryId = :categoryId AND p.status = :status")
    Page<Product> findByCategoryIdAndStatus(@Param("categoryId") Long categoryId, @Param("status") Boolean status, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.category.categoryId = :categoryId")
    Page<Product> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.category.categoryId = :categoryId AND (p.productName LIKE %:keyword% OR p.description LIKE %:keyword%)")
    Page<Product> findByCategoryIdAndKeyword(@Param("categoryId") Long categoryId, @Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.category.categoryId = :categoryId AND p.status = :status AND (p.productName LIKE %:keyword% OR p.description LIKE %:keyword%)")
    Page<Product> findByCategoryIdAndStatusAndKeyword(@Param("categoryId") Long categoryId, @Param("status") Boolean status, @Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = :status")
    Long countByStatus(@Param("status") Boolean status);
}