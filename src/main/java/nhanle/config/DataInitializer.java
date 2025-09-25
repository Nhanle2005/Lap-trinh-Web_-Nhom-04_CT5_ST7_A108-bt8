package nhanle.config;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import nhanle.entity.Category;
import nhanle.entity.Product;
import nhanle.service.CategoryService;
import nhanle.service.ProductService;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Override
    public void run(String... args) throws Exception {
        // Tạo sample categories
        createSampleCategories();
        
        // Tạo sample products
        createSampleProducts();
    }

    private void createSampleCategories() {
        List<Category> categories = Arrays.asList(
            createCategory("Điện tử", "Thiết bị điện tử và công nghệ", "fas fa-laptop"),
            createCategory("Thời trang", "Quần áo và phụ kiện thời trang", "fas fa-tshirt"),
            createCategory("Sách", "Sách và tài liệu học tập", "fas fa-book"),
            createCategory("Thể thao", "Dụng cụ và trang phục thể thao", "fas fa-running"),
            createCategory("Gia dụng", "Đồ gia dụng và nội thất", "fas fa-home"),
            createCategory("Thực phẩm", "Thực phẩm và đồ uống", "fas fa-utensils")
        );

        for (Category category : categories) {
            categoryService.save(category);
        }
    }

    private Category createCategory(String name, String description, String icon) {
        Category category = new Category();
        category.setCategoryName(name);
        category.setDescription(description);
        category.setIcon(icon);
        category.setStatus(true);
        return category;
    }

    private void createSampleProducts() {
        // Lấy các categories đã tạo để tham chiếu
        List<Category> categories = categoryService.findActiveCategories();
        
        if (categories.size() >= 6) {
            Category electronics = categories.get(0);
            Category fashion = categories.get(1);
            Category books = categories.get(2);
            Category sports = categories.get(3);
            Category home = categories.get(4);
            Category food = categories.get(5);

            List<Product> products = Arrays.asList(
                // Electronics
                createProduct("Laptop Dell XPS 13", "Laptop cao cấp với màn hình InfinityEdge", 
                    new BigDecimal("25000000"), 15, electronics, 
                    "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=300"),
                    
                createProduct("iPhone 15 Pro", "Smartphone flagship mới nhất của Apple", 
                    new BigDecimal("28000000"), 10, electronics,
                    "https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=300"),
                    
                createProduct("Samsung Galaxy Watch", "Đồng hồ thông minh Samsung", 
                    new BigDecimal("7500000"), 25, electronics,
                    "https://images.unsplash.com/photo-1544117519-31a4b719223d?w=300"),

                // Fashion
                createProduct("Áo sơ mi nam", "Áo sơ mi công sở cao cấp", 
                    new BigDecimal("450000"), 50, fashion,
                    "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=300"),
                    
                createProduct("Giày thể thao Nike", "Giày chạy bộ Nike Air Max", 
                    new BigDecimal("2800000"), 30, fashion,
                    "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=300"),
                    
                createProduct("Túi xách nữ", "Túi xách da thật cao cấp", 
                    new BigDecimal("1200000"), 20, fashion,
                    "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=300"),

                // Books
                createProduct("Sách Java Programming", "Sách học lập trình Java từ cơ bản đến nâng cao", 
                    new BigDecimal("350000"), 100, books,
                    "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=300"),
                    
                createProduct("Tiểu thuyết Harry Potter", "Bộ truyện Harry Potter 7 tập", 
                    new BigDecimal("580000"), 40, books,
                    "https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=300"),

                // Sports
                createProduct("Bóng đá FIFA", "Bóng đá chính thức FIFA World Cup", 
                    new BigDecimal("850000"), 60, sports,
                    "https://images.unsplash.com/photo-1511886929837-354d827aae26?w=300"),
                    
                createProduct("Áo thể thao Adidas", "Áo thun thể thao Adidas Dri-FIT", 
                    new BigDecimal("650000"), 45, sports,
                    "https://images.unsplash.com/photo-1551698618-1dfe5d97d256?w=300"),

                // Home
                createProduct("Ghế văn phòng", "Ghế văn phòng ergonomic cao cấp", 
                    new BigDecimal("3500000"), 15, home,
                    "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?w=300"),
                    
                createProduct("Đèn LED thông minh", "Đèn LED có thể điều khiển qua app", 
                    new BigDecimal("450000"), 35, home,
                    "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300"),

                // Food
                createProduct("Cà phê hạt Arabica", "Cà phê hạt nguyên chất 100% Arabica", 
                    new BigDecimal("280000"), 80, food,
                    "https://images.unsplash.com/photo-1447933601403-0c6688de566e?w=300"),
                    
                createProduct("Mật ong rừng", "Mật ong rừng tự nhiên 100%", 
                    new BigDecimal("320000"), 25, food,
                    "https://images.unsplash.com/photo-1558642452-9d2a7deb7f62?w=300")
            );

            for (Product product : products) {
                productService.save(product);
            }

            // Tạo thêm một số sản phẩm với status = false để test soft delete
            Product deletedProduct = createProduct("Sản phẩm đã xóa", "Đây là sản phẩm để test soft delete", 
                new BigDecimal("100000"), 0, electronics, null);
            deletedProduct.setStatus(false);
            productService.save(deletedProduct);
        }
    }

    private Product createProduct(String name, String description, BigDecimal price, 
                                int quantity, Category category, String image) {
        Product product = new Product();
        product.setProductName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setCategory(category);
        product.setImage(image);
        product.setStatus(true);
        return product;
    }
}