package com.kr.bill.inventorymangementsystem.controller;

import com.kr.bill.inventorymangementsystem.repository.BillRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Controller for the Bill History page ({@code /bill-history}).
 *
 * <p>Displays a filterable, newest-first list of all completed transactions.
 * Operators can narrow the list by:</p>
 * <ul>
 *   <li>Date range ({@code startDate} / {@code endDate} query parameters)</li>
 *   <li>Customer name substring ({@code customerName} query parameter)</li>
 * </ul>
 *
 * <p>When no filters are supplied the page defaults to showing bills from the
 * last 30 days.</p>
 */
@Controller
@RequestMapping("/bill-history")
public class BillHistoryController {

    private final BillRepository billRepository;

    public BillHistoryController(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    /**
     * Renders the bill-history page with optional filtering.
     *
     * <p>Filter priority:</p>
     * <ol>
     *   <li>If {@code customerName} is non-blank, results are filtered by customer name
     *       only (date range is ignored for this search mode).</li>
     *   <li>Otherwise results are filtered by the date range
     *       [{@code startDate} 00:00:00, {@code endDate} 23:59:59].</li>
     * </ol>
     *
     * <p>Model attributes added:</p>
     * <ul>
     *   <li>{@code bills}        – filtered list of bills</li>
     *   <li>{@code startDate}    – effective start date used for the form</li>
     *   <li>{@code endDate}      – effective end date used for the form</li>
     *   <li>{@code customerName} – customer name filter value (may be empty)</li>
     * </ul>
     *
     * @param startDate    optional start date (ISO format: {@code yyyy-MM-dd})
     * @param endDate      optional end date (ISO format: {@code yyyy-MM-dd})
     * @param customerName optional partial customer name to search for
     * @param model        Spring MVC model
     * @return Thymeleaf template name {@code "bill-history"}
     */
    @GetMapping
    public String showBillHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "") String customerName,
            Model model) {

        // Default date range: last 30 days
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null)   endDate   = LocalDate.now();

        if (!customerName.isBlank()) {
            // Customer-name search mode
            model.addAttribute("bills",
                    billRepository.findByCustomerNameContainingIgnoreCaseOrderByTransactionTimeDesc(customerName));
        } else {
            // Date-range mode
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end   = endDate.atTime(LocalTime.MAX);
            model.addAttribute("bills",
                    billRepository.findByTransactionTimeBetweenOrderByTransactionTimeDesc(start, end));
        }

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("customerName", customerName);
        return "bill-history";
    }
}