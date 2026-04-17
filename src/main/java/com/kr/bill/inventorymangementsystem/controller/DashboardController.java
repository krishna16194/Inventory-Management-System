package com.kr.bill.inventorymangementsystem.controller;

import com.kr.bill.inventorymangementsystem.model.Product;
import com.kr.bill.inventorymangementsystem.repository.ProductRepository;
import com.kr.bill.inventorymangementsystem.service.StatsService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for the main Dashboard page ({@code /dashboard}).
 *
 * <p>The dashboard is the primary working screen for operators. It hosts three
 * tabs rendered by a single Thymeleaf template:
 * <ol>
 *   <li><strong>Billing</strong> – scan or search products and build a bill.</li>
 *   <li><strong>Add Inventory</strong> – add new products or update existing ones
 *       by scanning or typing a barcode.</li>
 *   <li><strong>View Inventory</strong> – browse the full product list with
 *       low-stock highlighting, edit, and delete capabilities.</li>
 * </ol>
 *
 * <p>The model is populated with all data needed by every tab so that the page
 * can be rendered in a single round-trip.</p>
 */
@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final ProductRepository productRepository;
    private final StatsService statsService;

    public DashboardController(ProductRepository productRepository, StatsService statsService) {
        this.productRepository = productRepository;
        this.statsService = statsService;
    }

    /**
     * Renders the main dashboard.
     *
     * <p>Model attributes added:</p>
     * <ul>
     *   <li>{@code product}     – empty {@link Product} bound to the Add-Inventory form</li>
     *   <li>{@code products}    – all active products for the View-Inventory tab</li>
     *   <li>{@code cart}        – current session cart (map of product → quantity)</li>
     *   <li>{@code total}       – monetary total of items currently in the cart</li>
     *   <li>{@code lowStockCount} – number of products at or below the low-stock threshold</li>
     *   <li>{@code lowStockThreshold} – the threshold value, for display in the UI</li>
     *   <li>{@code todayRevenue} – total revenue collected today</li>
     *   <li>{@code todayBillCount} – number of bills completed today</li>
     * </ul>
     *
     * @param model   Spring MVC model
     * @param session HTTP session (holds the shopping cart)
     * @return name of the Thymeleaf template to render ({@code "dashboard"})
     */
    @GetMapping
    public String showDashboard(Model model, HttpSession session) {
        // Add-Inventory tab: blank product for the form binding
        model.addAttribute("product", new Product());

        // View-Inventory tab: full product list
        model.addAttribute("products", productRepository.findAll());

        // Billing tab: current session cart & running total
        @SuppressWarnings("unchecked")
        Map<Product, Integer> cart = (Map<Product, Integer>) session.getAttribute("cart");
        if (cart == null) cart = new HashMap<>();
        model.addAttribute("cart", cart);
        double total = cart.entrySet().stream()
                .mapToDouble(e -> e.getKey().getPrice() * e.getValue())
                .sum();
        model.addAttribute("total", total);

        // Low-stock warnings
        model.addAttribute("lowStockCount", statsService.getLowStockCount());
        model.addAttribute("lowStockThreshold", StatsService.LOW_STOCK_THRESHOLD);

        // Today's quick-stats for the info bar
        model.addAttribute("todayRevenue", statsService.getTodayRevenue());
        model.addAttribute("todayBillCount", statsService.getTodayBillCount());

        return "dashboard";
    }
}