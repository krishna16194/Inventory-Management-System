package com.kr.bill.inventorymangementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

/**
 * Data-Transfer Object representing aggregated sales figures for a single
 * calendar day.
 *
 * <p>Instances are produced by {@link com.kr.bill.inventorymangementsystem.service.StatsService#getDailySales}
 * and consumed by the Statistics Dashboard template to render the daily-sales
 * table and chart.</p>
 */
@Data
@AllArgsConstructor
public class DailySale {

    /** The calendar date these figures relate to. */
    private LocalDate date;

    /** Total number of individual product units sold on this date. */
    private long itemsSold;

    /** Total revenue (sum of price-at-sale × quantity) collected on this date. */
    private double totalRevenue;
}