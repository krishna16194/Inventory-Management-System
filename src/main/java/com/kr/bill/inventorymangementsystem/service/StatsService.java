package com.kr.bill.inventorymangementsystem.service;

import com.kr.bill.inventorymangementsystem.dto.DailySale;
import com.kr.bill.inventorymangementsystem.dto.ProductSale;
import com.kr.bill.inventorymangementsystem.model.Bill;
import com.kr.bill.inventorymangementsystem.model.BillItem;
import com.kr.bill.inventorymangementsystem.repository.BillRepository;
import com.kr.bill.inventorymangementsystem.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private final ProductRepository productRepository;
    private final BillRepository billRepository;

    public StatsService(ProductRepository productRepository, BillRepository billRepository) {
        this.productRepository = productRepository;
        this.billRepository = billRepository;
    }

    public long getTotalProducts() {
        return productRepository.count();
    }

    public long getTotalStockQuantity() {
        return productRepository.findAll().stream().mapToLong(p -> p.getQuantity()).sum();
    }

    public double getTotalInventoryValue() {
        return productRepository.findAll().stream().mapToDouble(p -> p.getPrice() * p.getQuantity()).sum();
    }

    public List<DailySale> getDailySales(LocalDate startDate, LocalDate endDate) {
        List<Bill> bills = billRepository.findAll();
        Map<LocalDate, List<Bill>> billsByDay = bills.stream()
                .filter(bill -> !bill.getTransactionTime().toLocalDate().isBefore(startDate) && !bill.getTransactionTime().toLocalDate().isAfter(endDate))
                .collect(Collectors.groupingBy(bill -> bill.getTransactionTime().toLocalDate()));

        return billsByDay.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<Bill> dailyBills = entry.getValue();
                    long itemsSold = dailyBills.stream()
                            .flatMap(bill -> bill.getBillItems().stream())
                            .mapToLong(item -> item.getQuantity())
                            .sum();
                    double totalRevenue = dailyBills.stream().mapToDouble(Bill::getTotalAmount).sum();
                    return new DailySale(date, itemsSold, totalRevenue);
                })
                .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                .collect(Collectors.toList());
    }

    public List<ProductSale> getTopSellingProducts(LocalDate startDate, LocalDate endDate) {
        List<Bill> bills = billRepository.findAll();
        Map<String, List<BillItem>> itemsByProduct = bills.stream()
                .filter(bill -> !bill.getTransactionTime().toLocalDate().isBefore(startDate) && !bill.getTransactionTime().toLocalDate().isAfter(endDate))
                .flatMap(bill -> bill.getBillItems().stream())
                .collect(Collectors.groupingBy(item -> item.getProduct().getId()));

        return itemsByProduct.entrySet().stream()
                .map(entry -> {
                    String productId = entry.getKey();
                    List<BillItem> items = entry.getValue();
                    String productName = items.get(0).getProduct().getName();
                    long totalQuantitySold = items.stream().mapToLong(BillItem::getQuantity).sum();
                    double totalRevenue = items.stream().mapToDouble(item -> item.getPriceAtSale() * item.getQuantity()).sum();
                    return new ProductSale(productId, productName, totalQuantitySold, totalRevenue);
                })
                .sorted(Comparator.comparing(ProductSale::getTotalQuantitySold).reversed())
                .collect(Collectors.toList());
    }
}
