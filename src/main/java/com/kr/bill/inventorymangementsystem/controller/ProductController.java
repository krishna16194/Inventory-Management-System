package com.kr.bill.inventorymangementsystem.controller;

import com.kr.bill.inventorymangementsystem.model.AppUser;
import com.kr.bill.inventorymangementsystem.model.Product;
import com.kr.bill.inventorymangementsystem.repository.ProductRepository;
import com.kr.bill.inventorymangementsystem.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for product management operations ({@code /products/**}).
 *
 * <p>Handles the full lifecycle of a {@link Product}: listing, creating,
 * updating, and soft-deleting. All mutating operations redirect back to the
 * {@code #add-inventory} tab on the main dashboard so the operator can
 * continue scanning without extra navigation.</p>
 *
 * <p>Soft-delete sets the product's {@code active} flag to {@code false},
 * preserving historical references in bill items while hiding the product
 * from all active-product queries (enforced by the Hibernate {@code @Where}
 * filter on {@link Product}).</p>
 */
@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository productRepository;
    private final UserService userService;

    public ProductController(ProductRepository productRepository, UserService userService) {
        this.productRepository = productRepository;
        this.userService = userService;
    }

    /**
     * Renders the standalone products page (legacy route kept for compatibility).
     *
     * @param model Spring MVC model
     * @return Thymeleaf template name {@code "products"}
     */
    @GetMapping
    public String listProducts(Model model,
                               @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("products", productRepository.findAllByOwnerUsername(userDetails.getUsername()));
        model.addAttribute("product", new Product());
        return "products";
    }

    /**
     * Persists a new product and redirects back to the Add-Inventory tab.
     *
     * @param product            product data bound from the form
     * @param redirectAttributes used to pass a success flash message
     * @return redirect to {@code /dashboard#add-inventory}
     */
    @PostMapping
    public String addProduct(Product product,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        AppUser owner = userService.findByUsername(userDetails.getUsername());
        product.setOwner(owner);
        productRepository.save(product);
        redirectAttributes.addFlashAttribute("success", "Product added successfully!");
        return "redirect:/dashboard#add-inventory";
    }

    /**
     * Updates an existing product (name, price, quantity) and redirects back
     * to the Add-Inventory tab.
     *
     * @param product            updated product data bound from the form
     * @param redirectAttributes used to pass a success flash message
     * @return redirect to {@code /dashboard#add-inventory}
     */
    @PostMapping("/update")
    public String updateProduct(Product product,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        // Preserve the owner from the existing record
        productRepository.findById(product.getId()).ifPresent(existing -> {
            product.setOwner(existing.getOwner());
        });
        productRepository.save(product);
        redirectAttributes.addFlashAttribute("success", "Product updated successfully!");
        return "redirect:/dashboard#add-inventory";
    }

    /**
     * Soft-deletes a product by setting its {@code active} flag to {@code false}.
     * The product record is retained in the database to preserve bill-item history.
     *
     * @param id                 the ID of the product to delete
     * @param redirectAttributes used to pass a success flash message
     * @return redirect to {@code /dashboard#view-inventory}
     */
    @PostMapping("/delete")
    public String deleteProduct(@RequestParam String id,
                                RedirectAttributes redirectAttributes) {
        productRepository.findById(id).ifPresent(product -> {
            product.setActive(false);
            productRepository.save(product);
        });
        redirectAttributes.addFlashAttribute("success", "Product deleted successfully!");
        return "redirect:/dashboard#view-inventory";
    }
}