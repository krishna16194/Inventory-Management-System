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
 * Service for computing inventory and sales statistics.
 *
 * <p>All heavy aggregation is performed in-memory via Java Streams.
 * For large datasets consider migrating the aggregation queries to JPQL/SQL.</p>
 */
@Service
public class StatsService {

    /** Default threshold below which a product is considered "low stock". */
    public static final int LOW_STOCK_THRESHOLD = 5;

    private final ProductRepository productRepository;
    private final BillRepository billRepository;

    public StatsService(ProductRepository productRepository, BillRepository billRepository) {
        this.productRepository = productRepository;
        this.billRepository = billRepository;
    }

    // -----------------------------------------------------------------------
    // Inventory stats
    // -----------------------------------------------------------------------

    /**
     * Returns the total number of distinct active product SKUs in the system.
     *
     * @return count of active products
     */
    public long getTotalProducts() {
        return productRepository.count();
    }

    /**
     * Returns the sum of {@code quantity} across all active products.
     *
     * @return total units currently in stock
     */
    public long getTotalStockQuantity() {
        return productRepository.findAll().stream()
                .mapToLong(Product::getQuantity).sum();
    }

    /**
     * Returns the total monetary value of the current inventory
     * (price × quantity for every active product).
     *
     * @return total inventory value
     */
    public double getTotalInventoryValue() {
        return productRepository.findAll().stream()
                .mapToDouble(p -> p.getPrice() * p.getQuantity()).sum();
    }

    /**
     * Returns all active products whose stock quantity is at or below
     * {@link #LOW_STOCK_THRESHOLD}, ordered by quantity ascending.
     *
     * @return list of low-stock products
     */
    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts(LOW_STOCK_THRESHOLD);
    }

    /**
     * Returns the count of active products at or below the low-stock threshold.
     * Used to render the warning badge on the dashboard navigation.
     *
     * @return number of low-stock products
     */
    public long getLowStockCount() {
        return productRepository.countLowStockProducts(LOW_STOCK_THRESHOLD);
    }

    // -----------------------------------------------------------------------
    // Today's quick-stats
    // -----------------------------------------------------------------------

    /**
     * Calculates the total revenue generated today (midnight → now).
     *
     * @return today's revenue
     */
    public double getTodayRevenue() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay   = LocalDate.now().atTime(LocalTime.MAX);
        return billRepository.sumTotalAmountByTransactionTimeBetween(startOfDay, endOfDay);
    }

    /**
     * Returns the number of bills (transactions) completed today.
     *
     * @return today's bill count
     */
    public long getTodayBillCount() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay   = LocalDate.now().atTime(LocalTime.MAX);
        return billRepository.countByTransactionTimeBetween(startOfDay, endOfDay);
    }

    // -----------------------------------------------------------------------
    // Date-range stats
    // -----------------------------------------------------------------------

    /**
     * Groups bills by calendar date within the given range and returns a
     * {@link DailySale} summary for each day that had at least one sale.
     * Results are sorted newest-first.
     *
     * @param startDate first date to include (inclusive)
     * @param endDate   last date to include (inclusive)
     * @return daily sales summaries, newest first
     */
    public List<DailySale> getDailySales(LocalDate startDate, LocalDate endDate) {
        List<Bill> bills = billRepository.findAll();
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
                            .mapToLong(BillItem::getQuantity)
                            .sum();
                    double totalRevenue = dailyBills.stream()
                            .mapToDouble(Bill::getTotalAmount).sum();
                    return new DailySale(date, itemsSold, totalRevenue);
                })
                .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                .collect(Collectors.toList());
    }

    /**
     * Aggregates all bill items within the given date range by product and
     * returns a ranked list of top-selling products (highest quantity sold first).
     *
     * @param startDate first date to include (inclusive)
     * @param endDate   last date to include (inclusive)
     * @return product sales summaries, best-seller first
     */
    public List<ProductSale> getTopSellingProducts(LocalDate startDate, LocalDate endDate) {
        List<Bill> bills = billRepository.findAll();
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
}