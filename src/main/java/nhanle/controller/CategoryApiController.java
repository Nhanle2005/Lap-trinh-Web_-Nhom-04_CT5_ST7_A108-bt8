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
import nhanle.model.CategoryModel;
import nhanle.service.CategoryService;
import nhanle.service.ProductService;

@RestController
@RequestMapping("/api/categories")
public class CategoryApiController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    // GET all categories
    @GetMapping
    public ResponseEntity<?> getAllCategories(
            @RequestParam(defaultValue = "true") Boolean status,
            @RequestParam(required = false) String keyword) {
        
        try {
            List<Category> categories;
            
            if (status && keyword == null) {
                categories = categoryService.findActiveCategories();
            } else {
                // For complex queries, we'll need to get all and filter
                categories = categoryService.findActiveCategories().stream()
                    .filter(c -> status.equals(c.getStatus()))
                    .filter(c -> keyword == null || keyword.trim().isEmpty() || 
                               c.getCategoryName().toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
            }
            
            List<CategoryModel> categoryModels = categories.stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(categoryModels);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Lỗi khi lấy danh sách danh mục: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // GET category by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id) {
        try {
            Optional<Category> categoryOpt = categoryService.findById(id);
            if (categoryOpt.isPresent()) {
                CategoryModel categoryModel = convertToModel(categoryOpt.get());
                // Add product count
                Long productCount = productService.countByCategoryId(id);
                categoryModel.setProductCount(productCount);
                return ResponseEntity.ok(categoryModel);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Không tìm thấy danh mục có ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Lỗi khi lấy thông tin danh mục: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // CREATE new category
    @PostMapping
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryModel categoryModel, 
                                           BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                Map<String, String> errors = new HashMap<>();
                bindingResult.getFieldErrors().forEach(error -> 
                    errors.put(error.getField(), error.getDefaultMessage()));
                return ResponseEntity.badRequest().body(errors);
            }

            // Check if category name already exists
            if (categoryService.existsByCategoryName(categoryModel.getCategoryName())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Tên danh mục đã tồn tại");
                return ResponseEntity.badRequest().body(error);
            }

            Category category = convertToEntity(categoryModel);
            Category savedCategory = categoryService.save(category);
            
            CategoryModel responseModel = convertToModel(savedCategory);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseModel);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Lỗi khi tạo danh mục: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // UPDATE category
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, 
                                           @Valid @RequestBody CategoryModel categoryModel,
                                           BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                Map<String, String> errors = new HashMap<>();
                bindingResult.getFieldErrors().forEach(error -> 
                    errors.put(error.getField(), error.getDefaultMessage()));
                return ResponseEntity.badRequest().body(errors);
            }

            Optional<Category> existingCategoryOpt = categoryService.findById(id);
            if (!existingCategoryOpt.isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Không tìm thấy danh mục có ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // Check if category name already exists (excluding current category)
            Category existingCategory = existingCategoryOpt.get();
            if (!existingCategory.getCategoryName().equals(categoryModel.getCategoryName()) &&
                categoryService.existsByCategoryName(categoryModel.getCategoryName())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Tên danh mục đã tồn tại");
                return ResponseEntity.badRequest().body(error);
            }

            BeanUtils.copyProperties(categoryModel, existingCategory, "categoryId", "createdAt", "updatedAt");
            Category updatedCategory = categoryService.update(existingCategory);
            
            CategoryModel responseModel = convertToModel(updatedCategory);
            return ResponseEntity.ok(responseModel);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Lỗi khi cập nhật danh mục: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // SOFT DELETE category (change status)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            Optional<Category> categoryOpt = categoryService.findById(id);
            if (!categoryOpt.isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Không tìm thấy danh mục có ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // Check if category has products
            Long productCount = productService.countByCategoryId(id);
            if (productCount > 0) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Không thể xóa danh mục vì còn có " + productCount + " sản phẩm thuộc danh mục này");
                return ResponseEntity.badRequest().body(error);
            }

            // Use changeStatus method to soft delete
            categoryService.changeStatus(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Xóa danh mục thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Lỗi khi xóa danh mục: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // PERMANENT DELETE category
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<?> permanentDeleteCategory(@PathVariable Long id) {
        try {
            Optional<Category> categoryOpt = categoryService.findById(id);
            if (!categoryOpt.isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Không tìm thấy danh mục có ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // Check if category has products
            Long productCount = productService.countByCategoryId(id);
            if (productCount > 0) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Không thể xóa vĩnh viễn danh mục vì còn có " + productCount + " sản phẩm thuộc danh mục này");
                return ResponseEntity.badRequest().body(error);
            }

            categoryService.deleteById(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Xóa vĩnh viễn danh mục thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Lỗi khi xóa vĩnh viễn danh mục: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // RESTORE category
    @PutMapping("/{id}/restore")
    public ResponseEntity<?> restoreCategory(@PathVariable Long id) {
        try {
            Optional<Category> categoryOpt = categoryService.findById(id);
            if (!categoryOpt.isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Không tìm thấy danh mục có ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Category category = categoryOpt.get();
            category.setStatus(true);
            Category restoredCategory = categoryService.update(category);
            
            CategoryModel responseModel = convertToModel(restoredCategory);
            return ResponseEntity.ok(responseModel);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Lỗi khi khôi phục danh mục: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Helper methods
    private CategoryModel convertToModel(Category category) {
        CategoryModel model = new CategoryModel();
        BeanUtils.copyProperties(category, model);
        return model;
    }

    private Category convertToEntity(CategoryModel model) {
        Category category = new Category();
        BeanUtils.copyProperties(model, category, "categoryId", "createdAt", "updatedAt");
        return category;
    }
}