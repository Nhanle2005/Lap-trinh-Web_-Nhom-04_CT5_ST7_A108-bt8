package nhanle.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nhanle.entity.Category;
import nhanle.entity.Product;
import nhanle.repository.ProductRepository;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> findByStatus(Boolean status) {
        return productRepository.findByStatus(status);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product update(Product product) {
        return productRepository.save(product);
    }

    @Override
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public void softDelete(Long id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setStatus(false);
            productRepository.save(product);
        }
    }

    @Override
    public List<Product> findByCategory(Category category) {
        return productRepository.findByCategoryAndStatus(category, true);
    }

    @Override
    public List<Product> findByCategoryAndStatus(Category category, Boolean status) {
        return productRepository.findByCategoryAndStatus(category, status);
    }

    @Override
    public List<Product> findByKeyword(String keyword) {
        return productRepository.findByProductNameContainingIgnoreCase(keyword);
    }

    @Override
    public List<Product> findByStatusAndKeyword(Boolean status, String keyword) {
        return productRepository.findByStatusAndKeyword(status, keyword);
    }

    @Override
    public List<Product> findByCategoryIdAndStatus(Long categoryId, Boolean status) {
        return productRepository.findByCategoryIdAndStatus(categoryId, status);
    }

    @Override
    public Long countByCategoryId(Long categoryId) {
        return productRepository.countByCategoryId(categoryId);
    }

    @Override
    public boolean existsById(Long id) {
        return productRepository.existsById(id);
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Page<Product> findByStatus(Boolean status, Pageable pageable) {
        return productRepository.findByStatus(status, pageable);
    }

    @Override
    public Page<Product> findByKeyword(String keyword, Pageable pageable) {
        return productRepository.findByProductNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword, pageable);
    }

    @Override
    public Page<Product> findByStatusAndKeyword(Boolean status, String keyword, Pageable pageable) {
        return productRepository.findByStatusAndKeyword(status, keyword, pageable);
    }

    @Override
    public Page<Product> findByCategoryIdAndStatus(Long categoryId, Boolean status, Pageable pageable) {
        return productRepository.findByCategoryIdAndStatus(categoryId, status, pageable);
    }

    @Override
    public Page<Product> findByCategoryId(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable);
    }

    @Override
    public Page<Product> findByCategoryIdAndKeyword(Long categoryId, String keyword, Pageable pageable) {
        return productRepository.findByCategoryIdAndKeyword(categoryId, keyword, pageable);
    }

    @Override
    public Page<Product> findByCategoryIdAndStatusAndKeyword(Long categoryId, Boolean status, String keyword, Pageable pageable) {
        return productRepository.findByCategoryIdAndStatusAndKeyword(categoryId, status, keyword, pageable);
    }

    @Override
    public Product changeStatus(Long id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setStatus(!product.getStatus());
            return productRepository.save(product);
        }
        throw new RuntimeException("Product not found with id: " + id);
    }

    @Override
    public Long count() {
        return productRepository.count();
    }

    @Override
    public Long countByStatus(Boolean status) {
        return productRepository.countByStatus(status);
    }
}