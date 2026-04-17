package com.kr.bill.inventorymangementsystem.controller;

import com.kr.bill.inventorymangementsystem.model.Product;
import com.kr.bill.inventorymangementsystem.repository.ProductRepository;
import com.kr.bill.inventorymangementsystem.service.ExcelService;
import com.kr.bill.inventorymangementsystem.service.StatsService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * REST API controller for inventory operations ({@code /api/inventory/**}).
 *
 * <p>Provides JSON endpoints consumed by the dashboard's client-side JavaScript
 * (product search autocomplete, barcode lookup) as well as Excel import/export
 * endpoints used by the View Inventory tab.</p>
 *
 * <p>All endpoints are publicly accessible (CSRF is disabled in
 * {@link com.kr.bill.inventorymangementsystem.config.SecurityConfig}).</p>
 */
@RestController
@RequestMapping("/api/inventory")
public class InventoryApiController {

    private final ProductRepository productRepository;
    private final ExcelService excelService;
    private final StatsService statsService;

    public InventoryApiController(ProductRepository productRepository,
                                   ExcelService excelService,
                                   StatsService statsService) {
        this.productRepository = productRepository;
        this.excelService = excelService;
        this.statsService = statsService;
    }

    /**
     * Searches for active products whose ID exactly matches or whose name
     * contains the given query string (case-insensitive).
     * Used by the product-search autocomplete on the billing tab.
     *
     * @param query search term (2+ characters recommended)
     * @return list of matching active products as JSON
     */
    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String query) {
        return productRepository.findByIdOrNameContainingIgnoreCase(query);
    }

    /**
     * Returns all active products in the inventory.
     * Used for populating full product lists in the UI.
     *
     * @return list of all active products as JSON
     */
    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Looks up a single active product by its exact ID (barcode).
     * Used by the Add-Inventory tab to auto-populate form fields when a
     * known barcode is scanned.
     *
     * @param id the exact product ID / barcode to look up
     * @return 200 OK with product JSON, or 404 Not Found if no active product exists
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Returns all active products whose stock quantity is at or below the
     * low-stock threshold ({@link StatsService#LOW_STOCK_THRESHOLD}).
     * Used by the dashboard to highlight at-risk inventory items.
     *
     * @return list of low-stock products ordered by quantity ascending
     */
    @GetMapping("/low-stock")
    public List<Product> getLowStockProducts() {
        return statsService.getLowStockProducts();
    }

    /**
     * Exports all active products to a downloadable Excel (.xlsx) file.
     * Column order: ID, Name, Price, Quantity.
     *
     * @return 200 OK with the .xlsx file as an attachment
     * @throws IOException if workbook generation fails
     */
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportProducts() throws IOException {
        String filename = "products.xlsx";
        InputStreamResource file = new InputStreamResource(excelService.exportProductsToExcel());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }

    /**
     * Imports (upserts) products from an uploaded Excel (.xlsx) file.
     * Existing products (matched by ID) are updated; new IDs create new products.
     *
     * @param file the multipart Excel file upload
     * @return 200 OK with a success message, or 400 Bad Request on parse failure
     */
    @PostMapping("/import")
    public ResponseEntity<String> importProducts(@RequestParam("file") MultipartFile file) {
        try {
            excelService.importProductsFromExcel(file);
            return ResponseEntity.ok("Products imported successfully");
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body("Failed to import products: " + e.getMessage());
        }
    }
}