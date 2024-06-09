package com.example.tuan2.controller;

import com.example.tuan2.model.Product;
import com.example.tuan2.service.CategoryService;
import com.example.tuan2.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class ProductController {
    @Autowired
    private ProductService productService;
    private static String UPLOADED_FOLDER = "src/main/resources/static/images/";
    @Autowired
    private CategoryService categoryService; // Đảm bảo bạn đã injectCategoryService
// Display a list of all products
    @GetMapping("/products")
    public String showProductList(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "/products/product-list";
    }
    // For adding a new product
    @GetMapping("/products/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories()); // Load categories
        return "/products/add-product";
    }
    // Process the form for adding a new product
    @PostMapping("/products/add")
    public String addProduct(@Valid Product product, BindingResult result, @RequestParam("image") MultipartFile file) {
        if (result.hasErrors()) {
            return "products/add-product"; // Ensure this view exists and is correct
        }
        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                Path path = Paths.get("src/main/resources/static/images/" + file.getOriginalFilename());
                Files.write(path, bytes);
                product.setImages(file.getOriginalFilename());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        productService.addProduct(product);
        return "redirect:/products";
    }
    // For editing a product
    @GetMapping("/products/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories()); //     Load categories
        return "/products/update-product";
    }
    // Process the form for updating a product
    @PostMapping("/products/update/{id}")
    public String updateProduct(@PathVariable Long id, @Valid Product product, BindingResult result, @RequestParam("image") MultipartFile file) {
        if (result.hasErrors()) {
            product.setId(id);
            return "products/update-product"; // Đảm bảo rằng view đúng
        }

        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                Path path = Paths.get("src/main/resources/static/images/" + file.getOriginalFilename());
                Files.write(path, bytes);
                product.setImages(file.getOriginalFilename());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Preserve the existing image if no new image is uploaded
            Product existingProduct = productService.getProductById(id).orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
            product.setImages(existingProduct.getImages());
        }

        productService.updateProduct(product);
        return "redirect:/products";
    }
    // Handle request to delete a product
    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProductById(id);
        return "redirect:/products";
    }
}