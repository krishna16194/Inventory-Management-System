package com.kr.bill.inventorymangementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductSale {
    private String productId;
    private String productName;
    private long totalQuantitySold;
    private double totalRevenue;
}
