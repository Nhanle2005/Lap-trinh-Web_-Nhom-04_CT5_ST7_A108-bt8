package nhanle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import nhanle.service.CategoryService;

@Controller
@RequestMapping("/admin/categories")
public class CategoryAjaxController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/ajax")
    public String listCategoriesAjax(Model model) {
        // This will show the AJAX-powered category list page
        return "admin/categories/list-ajax";
    }

    @GetMapping("/ajax/add")
    public String showAddFormAjax(Model model) {
        return "admin/categories/add-ajax";
    }

    @GetMapping("/ajax/edit/{id}")
    public String showEditFormAjax(@PathVariable Long id, Model model) {
        // For AJAX version, we can redirect to the list page
        // The edit will be handled by modal
        return "redirect:/admin/categories/ajax";
    }
}