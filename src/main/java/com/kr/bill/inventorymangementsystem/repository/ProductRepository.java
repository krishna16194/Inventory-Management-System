package com.kr.bill.inventorymangementsystem.repository;

import com.kr.bill.inventorymangementsystem.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    @Query("SELECT p FROM Product p WHERE p.id = :query OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> findByIdOrNameContainingIgnoreCase(@Param("query") String query);
}
