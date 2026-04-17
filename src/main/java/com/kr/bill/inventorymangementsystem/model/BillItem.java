package com.kr.bill.inventorymangementsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity representing a single line item within a {@link Bill}.
 *
 * <p>Each {@code BillItem} records:</p>
 * <ul>
 *   <li>Which {@link Product} was sold</li>
 *   <li>How many units were sold ({@link #quantity})</li>
 *   <li>The {@link #priceAtSale} at the moment of the transaction – this
 *       preserves historical accuracy even if the product price changes later</li>
 * </ul>
 *
 * <p>Bill items are always created as part of their owning {@link Bill}'s
 * cascade and are never modified after checkout.</p>
 */
@Entity
@Data
@NoArgsConstructor
public class BillItem {

    /** Auto-generated surrogate primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The bill (invoice) this line item belongs to.
     * Uses a foreign key column {@code bill_id}.
     */
    @ManyToOne
    @JoinColumn(name = "bill_id")
    private Bill bill;

    /**
     * The product that was sold.
     * Uses a foreign key column {@code product_id}.
     */
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    /** Number of units sold for this line item. */
    private int quantity;

    /**
     * The price per unit at the time of sale.
     * Stored independently so that later price changes do not affect
     * historical bill totals.
     */
    private double priceAtSale; // Price at the time of sale
}