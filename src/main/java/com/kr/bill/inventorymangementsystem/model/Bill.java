package com.kr.bill.inventorymangementsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA entity representing a completed sales transaction (bill / invoice).
 *
 * <p>A bill is created during the checkout process and is immutable once
 * persisted. It captures:</p>
 * <ul>
 *   <li>The customer details ({@link #customerName}, {@link #customerNumber})</li>
 *   <li>The GPS or descriptive {@link #location} captured in the browser</li>
 *   <li>The exact {@link #transactionTime} (set automatically via {@link #onCreate()})</li>
 *   <li>The {@link #totalAmount} of the transaction</li>
 *   <li>The individual line items ({@link #billItems})</li>
 * </ul>
 *
 * <p>Bill items are orphan-safe: they are cascade-persisted together with the
 * bill and physically deleted if the bill itself is deleted (though bill deletion
 * is not exposed in the current UI).</p>
 */
@Entity
@Data
public class Bill {

    /** Auto-generated surrogate primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Date and time the bill was created. Set automatically on first persist. */
    private LocalDateTime transactionTime;

    /** Grand total (sum of price-at-sale × quantity for all bill items). */
    private double totalAmount;

    /** Customer's full name as entered at checkout. */
    private String customerName;

    /** Customer's contact number as entered at checkout. */
    private String customerNumber;

    /** GPS co-ordinates or descriptive location captured by the browser. */
    private String location;

    /**
     * The individual product lines belonging to this bill.
     * Mapped by {@link BillItem#getBill()} and cascade-persisted.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL)
    private List<BillItem> billItems;

    /** The user who created this bill. */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private AppUser owner;

    /**
     * Lifecycle callback: automatically sets {@link #transactionTime} to the
     * current date-time when the bill is first persisted.
     */
    @PrePersist
    protected void onCreate() {
        transactionTime = LocalDateTime.now();
    }
}