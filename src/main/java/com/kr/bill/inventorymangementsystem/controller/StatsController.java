package com.kr.bill.inventorymangementsystem.controller;

import com.kr.bill.inventorymangementsystem.service.StatsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/**
 * Controller for the Statistics Dashboard page ({@code /stats-dashboard}).
 *
 * <p>Provides aggregated business intelligence including:</p>
 * <ul>
 *   <li>Inventory overview: total products, total stock units, total inventory value</li>
 *   <li>Today's quick-stats: revenue and bill count for the current calendar day</li>
 *   <li>Date-range sales report: daily sales breakdown and top-selling products</li>
 *   <li>Low-stock alerts: list of products at or below the configured threshold</li>
 * </ul>
 *
 * <p>The date range defaults to the past 30 days when no query parameters are
 * supplied by the user.</p>
 */
@Controller
@RequestMapping("/stats-dashboard")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    /**
     * Renders the statistics dashboard.
     *
     * <p>Model attributes added:</p>
     * <ul>
     *   <li>{@code totalProducts}       – number of active product SKUs</li>
     *   <li>{@code totalStockQuantity}  – sum of all stock quantities</li>
     *   <li>{@code totalInventoryValue} – monetary value of current stock</li>
     *   <li>{@code todayRevenue}        – revenue collected today</li>
     *   <li>{@code todayBillCount}      – number of transactions completed today</li>
     *   <li>{@code lowStockProducts}    – products at or below the low-stock threshold</li>
     *   <li>{@code lowStockThreshold}   – threshold value used for low-stock detection</li>
     *   <li>{@code dailySales}          – per-day sales summaries for the selected range</li>
     *   <li>{@code topSellingProducts}  – ranked product sales for the selected range</li>
     *   <li>{@code startDate}           – effective start date used in the filter form</li>
     *   <li>{@code endDate}             – effective end date used in the filter form</li>
     * </ul>
     *
     * @param startDate optional start date for the sales report (ISO format)
     * @param endDate   optional end date for the sales report (ISO format)
     * @param model     Spring MVC model
     * @return Thymeleaf template name {@code "stats-dashboard"}
     */
    @GetMapping
    public String showStatsDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        if (startDate == null) startDate = LocalDate.now().minusMonths(1);
        if (endDate == null)   endDate   = LocalDate.now();

        // Inventory overview
        model.addAttribute("totalProducts",       statsService.getTotalProducts());
        model.addAttribute("totalStockQuantity",  statsService.getTotalStockQuantity());
        model.addAttribute("totalInventoryValue", statsService.getTotalInventoryValue());

        // Today's quick-stats
        model.addAttribute("todayRevenue",    statsService.getTodayRevenue());
        model.addAttribute("todayBillCount",  statsService.getTodayBillCount());

        // Low-stock alerts
        model.addAttribute("lowStockProducts",  statsService.getLowStockProducts());
        model.addAttribute("lowStockThreshold", StatsService.LOW_STOCK_THRESHOLD);

        // Date-range sales report
        model.addAttribute("dailySales",         statsService.getDailySales(startDate, endDate));
        model.addAttribute("topSellingProducts", statsService.getTopSellingProducts(startDate, endDate));
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate",   endDate);

        return "stats-dashboard";
    }
}