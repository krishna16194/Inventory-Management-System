package com.kr.bill.inventorymangementsystem.controller;

import com.kr.bill.inventorymangementsystem.model.Product;
import com.kr.bill.inventorymangementsystem.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public String listProducts(Model model) {
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("product", new Product()); // For the form
        return "products";
    }

    @PostMapping
    public String addProduct(Product product, RedirectAttributes redirectAttributes) {
        productRepository.save(product);
        redirectAttributes.addFlashAttribute("success", "Product added successfully!");
        return "redirect:/dashboard#view-inventory";
    }

    @PostMapping("/update")
    public String updateProduct(Product product, RedirectAttributes redirectAttributes) {
        productRepository.save(product);
        redirectAttributes.addFlashAttribute("success", "Product updated successfully!");
        return "redirect:/dashboard#view-inventory";
    }

    @PostMapping("/delete")
    public String deleteProduct(@RequestParam String id, RedirectAttributes redirectAttributes) {
        productRepository.findById(id).ifPresent(product -> {
            product.setActive(false);
            productRepository.save(product);
            redirectAttributes.addFlashAttribute("success", "Product deleted successfully!");
        });
        return "redirect:/dashboard#view-inventory";
    }
}
