package com.kr.bill.inventorymangementsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * JPA entity representing a product (SKU) in the inventory.
 *
 * <p>Products are identified by a string {@link #id} which may be a barcode,
 * QR-code value, or any other unique identifier assigned by the operator.
 * Soft-deletion is implemented via the {@link #active} flag; the Hibernate
 * {@code @Where} filter ensures that inactive products are automatically
 * excluded from all standard repository queries.</p>
 *
 * <p>Equality and hashing are based solely on {@link #id} so that the same
 * product can be used as a {@link java.util.Map} key in the shopping cart
 * without issues caused by mutable quantity or price fields.</p>
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Where(clause = "active = true")
public class Product {

    /** Unique product identifier (barcode, QR-code, or manual ID). */
    @Id
    private String id;

    /** Human-readable product name shown in the UI and on receipts. */
    private String name;

    /** Selling price per unit. */
    private double price;

    /** Current stock quantity. Decremented on sale, restored on cart clear. */
    private int quantity;

    /**
     * Soft-delete flag. {@code false} means the product has been deleted but
     * its historical data (bill items) is preserved in the database.
     * Defaults to {@code true} (active).
     */
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean active = true;

    /** The user who owns / manages this product. */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private AppUser owner;
}