package com.kr.bill.inventorymangementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data-Transfer Object representing aggregated sales figures for a single
 * product across a selected date range.
 *
 * <p>Instances are produced by
 * {@link com.kr.bill.inventorymangementsystem.service.StatsService#getTopSellingProducts}
 * and consumed by the Statistics Dashboard template to render the top-selling
 * products table. The list is sorted by {@link #totalQuantitySold} descending
 * so the best-selling product appears first.</p>
 */
@Data
@AllArgsConstructor
public class ProductSale {

    /** The barcode / unique ID of the product. */
    private String productId;

    /** Human-readable product name. */
    private String productName;

    /** Total units sold across all bills in the selected date range. */
    private long totalQuantitySold;

    /** Total revenue attributable to this product in the selected date range. */
    private double totalRevenue;
}