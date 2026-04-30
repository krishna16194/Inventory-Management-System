package com.kr.bill.inventorymangementsystem.service;

import com.kr.bill.inventorymangementsystem.dto.DailySale;
import com.kr.bill.inventorymangementsystem.dto.ProductSale;
import com.kr.bill.inventorymangementsystem.model.Bill;
import com.kr.bill.inventorymangementsystem.model.BillItem;
import com.kr.bill.inventorymangementsystem.model.Product;
import com.kr.bill.inventorymangementsystem.repository.BillRepository;
import com.kr.bill.inventorymangementsystem.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for computing inventory and sales statistics, scoped per user.
 */
@Service
public class StatsService {

    public static final int LOW_STOCK_THRESHOLD = 5;

    private final ProductRepository productRepository;
    private final BillRepository billRepository;

    public StatsService(ProductRepository productRepository, BillRepository billRepository) {
        this.productRepository = productRepository;
        this.billRepository = billRepository;
    }

    // ── Inventory stats (owner-scoped) ───────────────────────────────────

    public long getTotalProducts(String username) {
        return productRepository.findAllByOwnerUsername(username).size();
    }

    public long getTotalStockQuantity(String username) {
        return productRepository.findAllByOwnerUsername(username).stream()
                .mapToLong(Product::getQuantity).sum();
    }

    public double getTotalInventoryValue(String username) {
        return productRepository.findAllByOwnerUsername(username).stream()
                .mapToDouble(p -> p.getPrice() * p.getQuantity()).sum();
    }

    public List<Product> getLowStockProducts(String username) {
        return productRepository.findLowStockProductsByOwner(LOW_STOCK_THRESHOLD, username);
    }

    public long getLowStockCount(String username) {
        return productRepository.countLowStockProductsByOwner(LOW_STOCK_THRESHOLD, username);
    }

    // ── Today's quick-stats (owner-scoped) ──────────────────────────────

    public double getTodayRevenue(String username) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end   = LocalDate.now().atTime(LocalTime.MAX);
        return billRepository.sumTotalAmountByOwnerAndTransactionTimeBetween(username, start, end);
    }

    public long getTodayBillCount(String username) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end   = LocalDate.now().atTime(LocalTime.MAX);
        return billRepository.countByOwnerUsernameAndTransactionTimeBetween(username, start, end);
    }

    // ── Date-range stats (owner-scoped) ─────────────────────────────────

    public List<DailySale> getDailySales(LocalDate startDate, LocalDate endDate, String username) {
        List<Bill> bills = billRepository.findByOwnerUsernameOrderByTransactionTimeDesc(username);
        Map<LocalDate, List<Bill>> billsByDay = bills.stream()
                .filter(bill -> {
                    LocalDate d = bill.getTransactionTime().toLocalDate();
                    return !d.isBefore(startDate) && !d.isAfter(endDate);
                })
                .collect(Collectors.groupingBy(bill -> bill.getTransactionTime().toLocalDate()));

        return billsByDay.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<Bill> dailyBills = entry.getValue();
                    long itemsSold = dailyBills.stream()
                            .flatMap(bill -> bill.getBillItems().stream())
                            .mapToLong(BillItem::getQuantity).sum();
                    double totalRevenue = dailyBills.stream()
                            .mapToDouble(Bill::getTotalAmount).sum();
                    return new DailySale(date, itemsSold, totalRevenue);
                })
                .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                .collect(Collectors.toList());
    }

    public List<ProductSale> getTopSellingProducts(LocalDate startDate, LocalDate endDate, String username) {
        List<Bill> bills = billRepository.findByOwnerUsernameOrderByTransactionTimeDesc(username);
        Map<String, List<BillItem>> itemsByProduct = bills.stream()
                .filter(bill -> {
                    LocalDate d = bill.getTransactionTime().toLocalDate();
                    return !d.isBefore(startDate) && !d.isAfter(endDate);
                })
                .flatMap(bill -> bill.getBillItems().stream())
                .collect(Collectors.groupingBy(item -> item.getProduct().getId()));

        return itemsByProduct.entrySet().stream()
                .map(entry -> {
                    List<BillItem> items = entry.getValue();
                    String productName = items.get(0).getProduct().getName();
                    long totalQuantitySold = items.stream().mapToLong(BillItem::getQuantity).sum();
                    double totalRevenue = items.stream()
                            .mapToDouble(item -> item.getPriceAtSale() * item.getQuantity()).sum();
                    return new ProductSale(entry.getKey(), productName, totalQuantitySold, totalRevenue);
                })
                .sorted(Comparator.comparing(ProductSale::getTotalQuantitySold).reversed())
                .collect(Collectors.toList());
    }

    // ── Legacy (no-arg) delegates kept for backwards compat ─────────────

    /** @deprecated use owner-scoped variant */
    public long getTotalProducts() { return productRepository.count(); }
    /** @deprecated use owner-scoped variant */
    public long getTotalStockQuantity() {
        return productRepository.findAll().stream().mapToLong(Product::getQuantity).sum();
    }
    /** @deprecated use owner-scoped variant */
    public double getTotalInventoryValue() {
        return productRepository.findAll().stream().mapToDouble(p -> p.getPrice() * p.getQuantity()).sum();
    }
    /** @deprecated use owner-scoped variant */
    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts(LOW_STOCK_THRESHOLD);
    }
    /** @deprecated use owner-scoped variant */
    public long getLowStockCount() {
        return productRepository.countLowStockProducts(LOW_STOCK_THRESHOLD);
    }
    /** @deprecated use owner-scoped variant */
    public double getTodayRevenue() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end   = LocalDate.now().atTime(LocalTime.MAX);
        return billRepository.sumTotalAmountByTransactionTimeBetween(start, end);
    }
    /** @deprecated use owner-scoped variant */
    public long getTodayBillCount() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end   = LocalDate.now().atTime(LocalTime.MAX);
        return billRepository.countByTransactionTimeBetween(start, end);
    }
    /** @deprecated use owner-scoped variant */
    public List<DailySale> getDailySales(LocalDate startDate, LocalDate endDate) {
        return getDailySales(startDate, endDate, null);
    }
    /** @deprecated use owner-scoped variant */
    public List<ProductSale> getTopSellingProducts(LocalDate startDate, LocalDate endDate) {
        return getTopSellingProducts(startDate, endDate, null);
    }
}