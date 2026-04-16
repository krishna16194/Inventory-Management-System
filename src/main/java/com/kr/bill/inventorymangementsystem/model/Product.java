package com.kr.bill.inventorymangementsystem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id") // Use only the 'id' field for equals and hashCode
@Where(clause = "active = true")
public class Product {

    @Id
    private String id; // For barcode or unique identifier
    private String name;
    private double price;
    private int quantity;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean active = true;
}
