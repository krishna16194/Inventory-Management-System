package com.kr.bill.inventorymangementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DailySale {
    private LocalDate date;
    private long itemsSold;
    private double totalRevenue;
}
