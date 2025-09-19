package nhanle.controller;

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
import nhanle.model.CategoryModel;
import nhanle.service.CategoryService;
import nhanle.storage.FileStorageService;

@Controller
@RequestMapping("/admin/categories")
public class CategoryAdminController {

	private final CategoryService categoryService;
	private final FileStorageService fileStorageService;

	public CategoryAdminController(CategoryService categoryService, FileStorageService fileStorageService) {
		this.categoryService = categoryService;
		this.fileStorageService = fileStorageService;
	}

	@GetMapping
	public String list(ModelMap model, @RequestParam(value = "q", required = false) String keyword,
			@RequestParam(value = "status", required = false) Boolean status,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size,
			@RequestParam(value = "sort", defaultValue = "categoryName") String sort,
			@RequestParam(value = "direction", defaultValue = "asc") String direction) {

		page = Math.max(page, 0);
		size = Math.max(Math.min(size, 100), 1);
		Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
		PageRequest pageable = PageRequest.of(page, size, Sort.by(dir, sort));

		Page<Category> result;
		if (status != null && keyword != null && !keyword.isBlank())
			result = categoryService.findByStatusAndKeyword(status, keyword.trim(), pageable);
		else if (status != null)
			result = categoryService.findByStatus(status, pageable);
		else if (keyword != null && !keyword.isBlank())
			result = categoryService.findByKeyword(keyword.trim(), pageable);
		else
			result = categoryService.findAll(pageable);

		model.addAttribute("page", result); // <-- quan trọng: trang list dùng 'page'
		model.addAttribute("q", keyword); // form search đang bind 'q'
		model.addAttribute("status", status);
		model.addAttribute("sort", sort);
		model.addAttribute("direction", direction);
		model.addAttribute("totalCategories", categoryService.count());
		model.addAttribute("activeCategories", categoryService.countByStatus(true));
		model.addAttribute("inactiveCategories", categoryService.countByStatus(false));

		return "admin/categories/list";
	}

	@GetMapping("/add")
	public String addForm(ModelMap model) {
		CategoryModel categoryModel = new CategoryModel();
		categoryModel.setEdit(false);
		categoryModel.setStatus(true); // Mặc định active
		model.addAttribute("category", categoryModel);
		return "admin/categories/add";
	}

	@PostMapping("/add")
	public String add(@Valid @ModelAttribute("category") CategoryModel categoryModel, BindingResult errors,
			@RequestParam(value = "iconFile", required = false) MultipartFile iconFile,
			RedirectAttributes redirectAttributes) {

		if (errors.hasErrors()) {
			return "admin/categories/add";
		}

		try {
			// Kiểm tra tên danh mục đã tồn tại
			if (categoryService.existsByCategoryName(categoryModel.getCategoryName())) {
				errors.rejectValue("categoryName", "category.name.exists", "Tên danh mục đã tồn tại");
				return "admin/categories/add";
			}

			// Xử lý upload file
			if (iconFile != null && !iconFile.isEmpty()) {
				String savedFileName = fileStorageService.storeImage(iconFile);
				categoryModel.setIcon(savedFileName);
			}

			// Tạo entity và lưu
			Category category = new Category();
			BeanUtils.copyProperties(categoryModel, category);
			categoryService.save(category);

			redirectAttributes.addFlashAttribute("successMessage", "Thêm danh mục thành công!");
			return "redirect:/admin/categories";

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
			return "redirect:/admin/categories/add";
		}
	}

	@GetMapping("/edit/{categoryId}")
	public String editForm(@PathVariable Long categoryId, ModelMap model, RedirectAttributes redirectAttributes) {

		Optional<Category> categoryOpt = categoryService.findById(categoryId);
		if (categoryOpt.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy danh mục với ID: " + categoryId);
			return "redirect:/admin/categories";
		}

		CategoryModel categoryModel = new CategoryModel();
		BeanUtils.copyProperties(categoryOpt.get(), categoryModel);
		categoryModel.setEdit(true);

		model.addAttribute("category", categoryModel);
		return "admin/categories/edit";
	}

	@PostMapping("/edit")
	public String edit(@Valid @ModelAttribute("category") CategoryModel categoryModel, BindingResult errors,
			@RequestParam(value = "iconFile", required = false) MultipartFile iconFile,
			RedirectAttributes redirectAttributes) {

		if (errors.hasErrors()) {
			return "admin/categories/edit";
		}

		try {
			// Kiểm tra tên danh mục trùng lặp (trừ chính nó)
			if (categoryService.existsByCategoryNameAndIdNot(categoryModel.getCategoryName(),
					categoryModel.getCategoryId())) {
				errors.rejectValue("categoryName", "category.name.exists", "Tên danh mục đã tồn tại");
				return "admin/categories/edit";
			}

			Category categoryToUpdate = new Category();
			BeanUtils.copyProperties(categoryModel, categoryToUpdate);

			// Xử lý upload file mới
			if (iconFile != null && !iconFile.isEmpty()) {
				// Xóa file cũ nếu có
				categoryService.findById(categoryModel.getCategoryId()).ifPresent(oldCategory -> {
					if (oldCategory.getIcon() != null) {
						fileStorageService.deleteIfExists(oldCategory.getIcon());
					}
				});

				String savedFileName = fileStorageService.storeImage(iconFile);
				categoryToUpdate.setIcon(savedFileName);
			} else {
				// Giữ icon cũ nếu không upload file mới
				categoryService.findById(categoryModel.getCategoryId())
						.ifPresent(oldCategory -> categoryToUpdate.setIcon(oldCategory.getIcon()));
			}

			categoryService.update(categoryToUpdate);
			redirectAttributes.addFlashAttribute("successMessage", "Cập nhật danh mục thành công!");
			return "redirect:/admin/categories";

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
			return "redirect:/admin/categories/edit/" + categoryModel.getCategoryId();
		}
	}

	@PostMapping("/delete/{categoryId}")
	public String delete(@PathVariable Long categoryId, RedirectAttributes redirectAttributes) {
		try {
			Optional<Category> categoryOpt = categoryService.findById(categoryId);
			if (categoryOpt.isPresent()) {
				Category category = categoryOpt.get();

				// Xóa file icon nếu có
				if (category.getIcon() != null) {
					fileStorageService.deleteIfExists(category.getIcon());
				}

				categoryService.deleteById(categoryId);
				redirectAttributes.addFlashAttribute("successMessage", "Xóa danh mục thành công!");
			} else {
				redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy danh mục để xóa!");
			}
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi xóa: " + e.getMessage());
		}

		return "redirect:/admin/categories";
	}

	@PostMapping("/toggle-status/{categoryId}")
	public String toggleStatus(@PathVariable Long categoryId, RedirectAttributes redirectAttributes) {
		try {
			Category category = categoryService.changeStatus(categoryId);
			String statusText = category.getStatus() ? "kích hoạt" : "vô hiệu hóa";
			redirectAttributes.addFlashAttribute("successMessage", "Đã " + statusText + " danh mục thành công!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
		}

		return "redirect:/admin/categories";
	}

	@GetMapping("/view/{categoryId}")
	public String view(@PathVariable Long categoryId, ModelMap model, RedirectAttributes redirectAttributes) {

		Optional<Category> categoryOpt = categoryService.findById(categoryId);
		if (categoryOpt.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy danh mục với ID: " + categoryId);
			return "redirect:/admin/categories";
		}

		model.addAttribute("category", categoryOpt.get());
		return "admin/categories/view";
	}
}
