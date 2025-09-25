package nhanle.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import nhanle.entity.Category;
import nhanle.entity.Product;
import nhanle.model.ProductModel;
import nhanle.service.CategoryService;
import nhanle.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductApiController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    // GET all products
    @GetMapping
    public ResponseEntity<?> getAllProducts(
            @RequestParam(defaultValue = "true") Boolean status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        
        try {
            List<Product> products;
            
            if (categoryId != null && keyword != null && !keyword.trim().isEmpty()) {
                products = productService.findByCategoryIdAndStatus(categoryId, status)
                    .stream()
                    .filter(p -> p.getProductName().toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
            } else if (categoryId != null) {
                products = productService.findByCategoryIdAndStatus(categoryId, status);
            } else if (keyword != null && !keyword.trim().isEmpty()) {
                products = productService.findByStatusAndKeyword(status, keyword);
            } else {
                products = productService.findByStatus(status);
            }
            
            List<ProductModel> productModels = products.stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(productModels);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Lỗi khi lấy danh sách sản phẩm: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // GET product by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        try {
            Optional<Product> productOpt = productService.findById(id);
            if (productOpt.isPresent()) {
                ProductModel productModel = convertToModel(productOpt.get());
                return ResponseEntity.ok(productModel);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Không tìm thấy sản phẩm có ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Lỗi khi lấy thông tin sản phẩm: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // CREATE new product
    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductModel productModel, 
                                          BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                Map<String, String> errors = new HashMap<>();
                bindingResult.getFieldErrors().forEach(error -> 
                    errors.put(error.getField(), error.getDefaultMessage()));
                return ResponseEntity.badRequest().body(errors);
            }

            // Check if category exists
            Optional<Category> categoryOpt = categoryService.findById(productModel.getCategoryId());
            if (!categoryOpt.isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Danh mục không tồn tại");
                return ResponseEntity.badRequest().body(error);
            }

            Product product = convertToEntity(productModel);
            product.setCategory(categoryOpt.get());
            Product savedProduct = productService.save(product);
            
            ProductModel responseModel = convertToModel(savedProduct);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseModel);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Lỗi khi tạo sản phẩm: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // UPDATE product
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, 
                                          @Valid @RequestBody ProductModel productModel,
                                          BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                Map<String, String> errors = new HashMap<>();
                bindingResult.getFieldErrors().forEach(error -> 
                    errors.put(error.getField(), error.getDefaultMessage()));
                return ResponseEntity.badRequest().body(errors);
            }

            Optional<Product> existingProductOpt = productService.findById(id);
            if (!existingProductOpt.isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Không tìm thấy sản phẩm có ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // Check if category exists
            Optional<Category> categoryOpt = categoryService.findById(productModel.getCategoryId());
            if (!categoryOpt.isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Danh mục không tồn tại");
                return ResponseEntity.badRequest().body(error);
            }

            Product existingProduct = existingProductOpt.get();
            BeanUtils.copyProperties(productModel, existingProduct, "productId", "createdAt", "updatedAt");
            existingProduct.setCategory(categoryOpt.get());
            
            Product updatedProduct = productService.update(existingProduct);
            ProductModel responseModel = convertToModel(updatedProduct);
            
            return ResponseEntity.ok(responseModel);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Lỗi khi cập nhật sản phẩm: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // SOFT DELETE product
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            if (!productService.existsById(id)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Không tìm thấy sản phẩm có ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            productService.softDelete(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Xóa sản phẩm thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Lỗi khi xóa sản phẩm: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // PERMANENT DELETE product
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<?> permanentDeleteProduct(@PathVariable Long id) {
        try {
            if (!productService.existsById(id)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Không tìm thấy sản phẩm có ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            productService.deleteById(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Xóa vĩnh viễn sản phẩm thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Lỗi khi xóa vĩnh viễn sản phẩm: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // RESTORE product
    @PutMapping("/{id}/restore")
    public ResponseEntity<?> restoreProduct(@PathVariable Long id) {
        try {
            Optional<Product> productOpt = productService.findById(id);
            if (!productOpt.isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Không tìm thấy sản phẩm có ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Product product = productOpt.get();
            product.setStatus(true);
            Product restoredProduct = productService.update(product);
            
            ProductModel responseModel = convertToModel(restoredProduct);
            return ResponseEntity.ok(responseModel);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Lỗi khi khôi phục sản phẩm: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Helper methods
    private ProductModel convertToModel(Product product) {
        ProductModel model = new ProductModel();
        BeanUtils.copyProperties(product, model);
        if (product.getCategory() != null) {
            model.setCategoryId(product.getCategory().getCategoryId());
            model.setCategoryName(product.getCategory().getCategoryName());
        }
        return model;
    }

    private Product convertToEntity(ProductModel model) {
        Product product = new Product();
        BeanUtils.copyProperties(model, product, "categoryId", "categoryName");
        return product;
    }
}