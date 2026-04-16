package com.kr.bill.inventorymangementsystem.controller;

import com.kr.bill.inventorymangementsystem.service.StatsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/stats-dashboard")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping
    public String showStatsDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        model.addAttribute("totalProducts", statsService.getTotalProducts());
        model.addAttribute("totalStockQuantity", statsService.getTotalStockQuantity());
        model.addAttribute("totalInventoryValue", statsService.getTotalInventoryValue());
        model.addAttribute("dailySales", statsService.getDailySales(startDate, endDate));
        model.addAttribute("topSellingProducts", statsService.getTopSellingProducts(startDate, endDate));
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "stats-dashboard";
    }
}
