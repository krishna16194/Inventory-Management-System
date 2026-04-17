package com.kr.bill.inventorymangementsystem.repository;

import com.kr.bill.inventorymangementsystem.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for {@link Product} entities.
 *
 * <p>Provides standard CRUD operations via {@link JpaRepository} plus
 * custom queries for inventory search and stock-level monitoring.</p>
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    /**
     * Searches for active products whose ID exactly matches or whose name
     * contains the given query string (case-insensitive).
     *
     * @param query the search term (barcode ID or partial product name)
     * @return list of matching active products
     */
    @Query("SELECT p FROM Product p WHERE p.id = :query OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> findByIdOrNameContainingIgnoreCase(@Param("query") String query);

    /**
     * Returns all active products whose stock quantity is at or below the
     * specified threshold. Used to generate low-stock warnings on the dashboard.
     *
     * @param threshold the maximum quantity that qualifies as "low stock"
     * @return list of active products with quantity &le; threshold, ordered by quantity ascending
     */
    @Query("SELECT p FROM Product p WHERE p.quantity <= :threshold ORDER BY p.quantity ASC")
    List<Product> findLowStockProducts(@Param("threshold") int threshold);

    /**
     * Counts the number of active products whose stock quantity is at or below
     * the specified threshold. Used for the low-stock badge in the navigation.
     *
     * @param threshold the maximum quantity that qualifies as "low stock"
     * @return count of low-stock active products
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.quantity <= :threshold")
    long countLowStockProducts(@Param("threshold") int threshold);
}