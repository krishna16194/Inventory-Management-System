package com.kr.bill.inventorymangementsystem.controller;

import com.kr.bill.inventorymangementsystem.model.Product;
import com.kr.bill.inventorymangementsystem.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final ProductRepository productRepository;

    public DashboardController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public String showDashboard(Model model, HttpSession session) {
        // For "Add Inventory" tab
        model.addAttribute("product", new Product());

        // For "View Inventory" tab
        model.addAttribute("products", productRepository.findAll());

        // For "Billing" tab
        Map<Product, Integer> cart = (Map<Product, Integer>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
        }
        model.addAttribute("cart", cart);
        double total = cart.entrySet().stream()
                .mapToDouble(entry -> entry.getKey().getPrice() * entry.getValue())
                .sum();
        model.addAttribute("total", total);

        return "dashboard";
    }
}
