package com.kr.bill.inventorymangementsystem.controller;

import com.kr.bill.inventorymangementsystem.model.Product;
import com.kr.bill.inventorymangementsystem.repository.ProductRepository;
import com.kr.bill.inventorymangementsystem.service.ExcelService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryApiController {

    private final ProductRepository productRepository;
    private final ExcelService excelService;

    public InventoryApiController(ProductRepository productRepository, ExcelService excelService) {
        this.productRepository = productRepository;
        this.excelService = excelService;
    }

    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String query) {
        return productRepository.findByIdOrNameContainingIgnoreCase(query);
    }

    /**
     * Gets all products currently in the inventory.
     * @return A list of all products.
     */
    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Gets a single product by its ID (barcode).
     * @param id The ID of the product to find.
     * @return The product details if found, otherwise a 404 Not Found response.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportProducts() throws IOException {
        String filename = "products.xlsx";
        InputStreamResource file = new InputStreamResource(excelService.exportProductsToExcel());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }

    @PostMapping("/import")
    public ResponseEntity<String> importProducts(@RequestParam("file") MultipartFile file) {
        try {
            excelService.importProductsFromExcel(file);
            return ResponseEntity.ok("Products imported successfully");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Failed to import products: " + e.getMessage());
        }
    }
}
