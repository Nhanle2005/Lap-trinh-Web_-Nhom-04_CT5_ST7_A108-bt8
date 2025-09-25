package nhanle.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import nhanle.entity.Category;
import nhanle.entity.Product;
import nhanle.model.ProductModel;
import nhanle.service.CategoryService;
import nhanle.service.ProductService;
import nhanle.storage.FileStorageService;

@Controller
@RequestMapping("/admin/products")
public class ProductAdminController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final FileStorageService fileStorageService;

    public ProductAdminController(ProductService productService, CategoryService categoryService, 
                                 FileStorageService fileStorageService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public String list(ModelMap model, 
                      @RequestParam(value = "q", required = false) String keyword,
                      @RequestParam(value = "categoryId", required = false) Long categoryId,
                      @RequestParam(value = "status", required = false) Boolean status,
                      @RequestParam(value = "page", defaultValue = "0") int page,
                      @RequestParam(value = "size", defaultValue = "10") int size,
                      @RequestParam(value = "sort", defaultValue = "productName") String sort,
                      @RequestParam(value = "direction", defaultValue = "asc") String direction) {

        page = Math.max(page, 0);
        size = Math.max(Math.min(size, 100), 1);
        Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        PageRequest pageable = PageRequest.of(page, size, Sort.by(dir, sort));

        Page<Product> result;
        if (categoryId != null && status != null && keyword != null && !keyword.isBlank()) {
            result = productService.findByCategoryIdAndStatusAndKeyword(categoryId, status, keyword.trim(), pageable);
        } else if (categoryId != null && status != null) {
            result = productService.findByCategoryIdAndStatus(categoryId, status, pageable);
        } else if (categoryId != null && keyword != null && !keyword.isBlank()) {
            result = productService.findByCategoryIdAndKeyword(categoryId, keyword.trim(), pageable);
        } else if (status != null && keyword != null && !keyword.isBlank()) {
            result = productService.findByStatusAndKeyword(status, keyword.trim(), pageable);
        } else if (categoryId != null) {
            result = productService.findByCategoryId(categoryId, pageable);
        } else if (status != null) {
            result = productService.findByStatus(status, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            result = productService.findByKeyword(keyword.trim(), pageable);
        } else {
            result = productService.findAll(pageable);
        }

        // Lấy danh sách categories cho filter dropdown
        List<Category> categories = categoryService.findByStatus(true);

        model.addAttribute("page", result);
        model.addAttribute("q", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("status", status);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        model.addAttribute("categories", categories);
        model.addAttribute("totalProducts", productService.count());
        model.addAttribute("activeProducts", productService.countByStatus(true));
        model.addAttribute("inactiveProducts", productService.countByStatus(false));

        return "admin/products/list";
    }

    @GetMapping("/add")
    public String addForm(ModelMap model) {
        ProductModel productModel = new ProductModel();
        productModel.setEdit(false);
        productModel.setStatus(true); // Mặc định active
        productModel.setQuantity(0);
        
        // Lấy danh sách categories đang hoạt động
        List<Category> categories = categoryService.findByStatus(true);
        
        model.addAttribute("product", productModel);
        model.addAttribute("categories", categories);
        return "admin/products/add";
    }

    @PostMapping("/add")
    public String add(@Valid @ModelAttribute("product") ProductModel productModel, 
                     BindingResult errors,
                     @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                     RedirectAttributes redirectAttributes,
                     ModelMap model) {

        if (errors.hasErrors()) {
            // Reload categories for dropdown
            List<Category> categories = categoryService.findByStatus(true);
            model.addAttribute("categories", categories);
            return "admin/products/add";
        }

        try {
            // Xử lý upload file
            if (imageFile != null && !imageFile.isEmpty()) {
                String savedFileName = fileStorageService.storeImage(imageFile);
                productModel.setImage(savedFileName);
            }

            // Tạo entity và lưu
            Product product = new Product();
            BeanUtils.copyProperties(productModel, product);
            
            // Set category
            if (productModel.getCategoryId() != null) {
                Optional<Category> categoryOpt = categoryService.findById(productModel.getCategoryId());
                if (categoryOpt.isPresent()) {
                    product.setCategory(categoryOpt.get());
                } else {
                    errors.rejectValue("categoryId", "category.not.found", "Danh mục không tồn tại");
                    List<Category> categories = categoryService.findByStatus(true);
                    model.addAttribute("categories", categories);
                    return "admin/products/add";
                }
            }

            productService.save(product);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm thành công!");
            return "redirect:/admin/products";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/admin/products/add";
        }
    }

    @GetMapping("/edit/{productId}")
    public String editForm(@PathVariable Long productId, ModelMap model, RedirectAttributes redirectAttributes) {

        Optional<Product> productOpt = productService.findById(productId);
        if (productOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sản phẩm với ID: " + productId);
            return "redirect:/admin/products";
        }

        Product product = productOpt.get();
        ProductModel productModel = new ProductModel();
        BeanUtils.copyProperties(product, productModel);
        productModel.setCategoryId(product.getCategory().getCategoryId());
        productModel.setEdit(true);

        // Lấy danh sách categories đang hoạt động
        List<Category> categories = categoryService.findByStatus(true);

        model.addAttribute("product", productModel);
        model.addAttribute("categories", categories);
        return "admin/products/edit";
    }

    @PostMapping("/edit")
    public String edit(@Valid @ModelAttribute("product") ProductModel productModel, 
                      BindingResult errors,
                      @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                      RedirectAttributes redirectAttributes,
                      ModelMap model) {

        if (errors.hasErrors()) {
            // Reload categories for dropdown
            List<Category> categories = categoryService.findByStatus(true);
            model.addAttribute("categories", categories);
            return "admin/products/edit";
        }

        try {
            Product productToUpdate = new Product();
            BeanUtils.copyProperties(productModel, productToUpdate);

            // Set category
            if (productModel.getCategoryId() != null) {
                Optional<Category> categoryOpt = categoryService.findById(productModel.getCategoryId());
                if (categoryOpt.isPresent()) {
                    productToUpdate.setCategory(categoryOpt.get());
                } else {
                    errors.rejectValue("categoryId", "category.not.found", "Danh mục không tồn tại");
                    List<Category> categories = categoryService.findByStatus(true);
                    model.addAttribute("categories", categories);
                    return "admin/products/edit";
                }
            }

            // Xử lý upload file mới
            if (imageFile != null && !imageFile.isEmpty()) {
                // Xóa file cũ nếu có
                productService.findById(productModel.getProductId()).ifPresent(oldProduct -> {
                    if (oldProduct.getImage() != null) {
                        fileStorageService.deleteIfExists(oldProduct.getImage());
                    }
                });

                String savedFileName = fileStorageService.storeImage(imageFile);
                productToUpdate.setImage(savedFileName);
            } else {
                // Giữ image cũ nếu không upload file mới
                productService.findById(productModel.getProductId())
                        .ifPresent(oldProduct -> productToUpdate.setImage(oldProduct.getImage()));
            }

            productService.update(productToUpdate);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công!");
            return "redirect:/admin/products";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/admin/products/edit/" + productModel.getProductId();
        }
    }

    @GetMapping("/view/{productId}")
    public String view(@PathVariable Long productId, ModelMap model, RedirectAttributes redirectAttributes) {

        Optional<Product> productOpt = productService.findById(productId);
        if (productOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sản phẩm với ID: " + productId);
            return "redirect:/admin/products";
        }

        model.addAttribute("product", productOpt.get());
        return "admin/products/view";
    }

    @PostMapping("/delete/{productId}")
    public String delete(@PathVariable Long productId, RedirectAttributes redirectAttributes) {
        try {
            Optional<Product> productOpt = productService.findById(productId);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();

                // Xóa file image nếu có
                if (product.getImage() != null) {
                    fileStorageService.deleteIfExists(product.getImage());
                }

                productService.deleteById(productId);
                redirectAttributes.addFlashAttribute("successMessage", "Xóa sản phẩm thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sản phẩm để xóa!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi xóa: " + e.getMessage());
        }

        return "redirect:/admin/products";
    }

    @PostMapping("/toggle-status/{productId}")
    public String toggleStatus(@PathVariable Long productId, RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.changeStatus(productId);
            String statusText = product.getStatus() ? "kích hoạt" : "ngừng bán";
            redirectAttributes.addFlashAttribute("successMessage", "Đã " + statusText + " sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/products";
    }
}