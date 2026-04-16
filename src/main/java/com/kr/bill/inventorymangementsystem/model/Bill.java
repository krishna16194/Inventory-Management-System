package com.kr.bill.inventorymangementsystem.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime transactionTime;
    private double totalAmount;
    private String customerName;
    private String customerNumber;
    private String location;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL)
    private List<BillItem> billItems;

    @PrePersist
    protected void onCreate() {
        transactionTime = LocalDateTime.now();
    }
}
