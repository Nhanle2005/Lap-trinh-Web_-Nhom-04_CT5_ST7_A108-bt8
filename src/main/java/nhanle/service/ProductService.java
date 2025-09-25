package nhanle.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import nhanle.entity.Category;
import nhanle.entity.Product;

public interface ProductService {
    
    List<Product> findAll();
    
    Page<Product> findAll(Pageable pageable);
    
    List<Product> findByStatus(Boolean status);
    
    Page<Product> findByStatus(Boolean status, Pageable pageable);
    
    Optional<Product> findById(Long id);
    
    Product save(Product product);
    
    Product update(Product product);
    
    void deleteById(Long id);
    
    void softDelete(Long id);
    
    Product changeStatus(Long id);
    
    List<Product> findByCategory(Category category);
    
    List<Product> findByCategoryAndStatus(Category category, Boolean status);
    
    List<Product> findByKeyword(String keyword);
    
    Page<Product> findByKeyword(String keyword, Pageable pageable);
    
    List<Product> findByStatusAndKeyword(Boolean status, String keyword);
    
    Page<Product> findByStatusAndKeyword(Boolean status, String keyword, Pageable pageable);
    
    List<Product> findByCategoryIdAndStatus(Long categoryId, Boolean status);
    
    Page<Product> findByCategoryIdAndStatus(Long categoryId, Boolean status, Pageable pageable);
    
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
    
    Page<Product> findByCategoryIdAndKeyword(Long categoryId, String keyword, Pageable pageable);
    
    Page<Product> findByCategoryIdAndStatusAndKeyword(Long categoryId, Boolean status, String keyword, Pageable pageable);
    
    Long countByCategoryId(Long categoryId);
    
    Long count();
    
    Long countByStatus(Boolean status);
    
    boolean existsById(Long id);
}