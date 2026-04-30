package com.kr.bill.inventorymangementsystem.service;

import com.kr.bill.inventorymangementsystem.model.AppUser;
import com.kr.bill.inventorymangementsystem.model.Product;
import com.kr.bill.inventorymangementsystem.repository.ProductRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for exporting the product inventory to Excel (.xlsx) and importing
 * products from an Excel file.
 *
 * <p>Export columns: <em>ID, Name, Price, Quantity</em> (one product per row).</p>
 *
 * <p>Import behaviour:
 * <ul>
 *   <li>If a product with the same ID already exists it is updated (name, price,
 *       quantity are overwritten).</li>
 *   <li>If the ID is new a fresh product record is created.</li>
 * </ul>
 * The header row (row 0) is always skipped during import.</p>
 */
@Service
public class ExcelService {

    private static final String[] COLUMNS = {"ID", "Name", "Price", "Quantity"};

    private final ProductRepository productRepository;

    public ExcelService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Exports all active products to an in-memory Excel workbook.
     *
     * @return {@link ByteArrayInputStream} containing the .xlsx file bytes
     * @throws IOException if workbook serialization fails
     */
    public ByteArrayInputStream exportProductsToExcel() throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Products");

            // Bold header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < COLUMNS.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(COLUMNS[col]);
                cell.setCellStyle(headerStyle);
            }

            List<Product> products = productRepository.findAll();
            int rowIdx = 1;
            for (Product product : products) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(product.getId());
                row.createCell(1).setCellValue(product.getName());
                row.createCell(2).setCellValue(product.getPrice());
                row.createCell(3).setCellValue(product.getQuantity());
            }

            // Auto-size columns for readability
            for (int col = 0; col < COLUMNS.length; col++) {
                sheet.autoSizeColumn(col);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    /**
     * Imports (upserts) products from an uploaded Excel file.
     *
     * <p>Each data row must have four cells in order: ID (string), Name (string),
     * Price (numeric), Quantity (numeric). Rows with an empty ID cell are skipped.
     * If the product ID already exists the existing record is updated; otherwise
     * a new product is created.</p>
     *
     * @param file the uploaded .xlsx file
     * @throws IOException if the file cannot be read or parsed
     */
    public void importProductsFromExcel(MultipartFile file) throws IOException {
        List<Product> toSave = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // skip header

                Cell idCell = row.getCell(0);
                if (idCell == null || idCell.getCellType() == CellType.BLANK) continue;

                String id = idCell.getStringCellValue().trim();
                if (id.isEmpty()) continue;

                // Upsert: load existing product or create new one
                Product product = productRepository.findById(id)
                        .orElseGet(Product::new);
                product.setId(id);
                product.setName(row.getCell(1).getStringCellValue().trim());
                product.setPrice(row.getCell(2).getNumericCellValue());
                product.setQuantity((int) row.getCell(3).getNumericCellValue());
                product.setActive(true);
                toSave.add(product);
            }
        }

        productRepository.saveAll(toSave);
    }

    /**
     * Imports products from Excel and assigns {@code owner} to any newly created product.
     * Existing products (matched by ID) are updated but owner is preserved.
     */
    public void importProductsFromExcel(MultipartFile file, AppUser owner) throws IOException {
        List<Product> toSave = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                Cell idCell = row.getCell(0);
                if (idCell == null || idCell.getCellType() == CellType.BLANK) continue;

                String id = idCell.getStringCellValue().trim();
                if (id.isEmpty()) continue;

                Product product = productRepository.findById(id).orElseGet(Product::new);
                product.setId(id);
                product.setName(row.getCell(1).getStringCellValue().trim());
                product.setPrice(row.getCell(2).getNumericCellValue());
                product.setQuantity((int) row.getCell(3).getNumericCellValue());
                product.setActive(true);
                if (product.getOwner() == null) product.setOwner(owner);
                toSave.add(product);
            }
        }

        productRepository.saveAll(toSave);
    }
}