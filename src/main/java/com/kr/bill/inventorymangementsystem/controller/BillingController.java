package com.kr.bill.inventorymangementsystem.controller;

import com.kr.bill.inventorymangementsystem.model.Bill;
import com.kr.bill.inventorymangementsystem.model.BillItem;
import com.kr.bill.inventorymangementsystem.model.Product;
import com.kr.bill.inventorymangementsystem.repository.BillRepository;
import com.kr.bill.inventorymangementsystem.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

/**
 * Controller for all billing operations ({@code /billing/**}).
 *
 * <p>Supports two modes of adding products to the cart:</p>
 * <ol>
 *   <li><strong>Full-page POST</strong> ({@code /billing/add}) – used by the
 *       manual search-and-click flow; redirects back to the dashboard.</li>
 *   <li><strong>AJAX POST</strong> ({@code /billing/add-scan}) – used by the
 *       continuous barcode-scanner flow; returns JSON so the billing table can
 *       be updated in-place without a page reload.</li>
 * </ol>
 *
 * <p>The shopping cart is stored in the HTTP session under the key {@code "cart"}
 * as a {@code Map<Product, Integer>} (product → quantity). Inventory stock is
 * decremented immediately when an item is added and restored when the bill is
 * cleared or when individual quantities are reduced via the update endpoint.</p>
 */
@Controller
@RequestMapping("/billing")
public class BillingController {

    private final ProductRepository productRepository;
    private final BillRepository billRepository;

    public BillingController(ProductRepository productRepository, BillRepository billRepository) {
        this.productRepository = productRepository;
        this.billRepository = billRepository;
    }

    // -----------------------------------------------------------------------
    // Page rendering
    // -----------------------------------------------------------------------

    /**
     * Renders the standalone billing page (legacy route kept for compatibility).
     *
     * @param session HTTP session containing the cart
     * @param model   Spring MVC model
     * @return Thymeleaf template name {@code "billing"}
     */
    @GetMapping
    public String billingPage(HttpSession session, Model model) {
        Map<Product, Integer> cart = getCart(session);
        model.addAttribute("cart", cart);
        model.addAttribute("total", calculateTotal(cart));
        return "billing";
    }

    // -----------------------------------------------------------------------
    // Cart management
    // -----------------------------------------------------------------------

    /**
     * Adds one unit of the specified product to the session cart (full-page flow).
     *
     * <p>If the product does not exist or has zero stock the request is silently
     * ignored and the user is redirected back to the dashboard.</p>
     *
     * @param productId the barcode / product ID to add
     * @param session   HTTP session
     * @return redirect to {@code /dashboard}
     */
    @PostMapping("/add")
    public String addProductToBill(@RequestParam String productId, HttpSession session) {
        addToCart(productId.trim(), session);
        return "redirect:/dashboard";
    }

    /**
     * Adds one unit of the specified product to the session cart (AJAX / scanner flow).
     *
     * <p>Returns a JSON object with the following fields:</p>
     * <ul>
     *   <li>{@code success} – {@code true} if the product was added successfully</li>
     *   <li>{@code message} – human-readable feedback (e.g. "Added: Coca Cola")</li>
     *   <li>{@code items}   – complete list of cart items (name, id, price, quantity, subtotal)</li>
     *   <li>{@code total}   – updated cart total</li>
     *   <li>{@code cartCount} – total number of units in the cart (for the tab badge)</li>
     * </ul>
     *
     * @param productId barcode / product ID scanned
     * @param session   HTTP session
     * @return {@link ResponseEntity} containing the JSON response map
     */
    @PostMapping("/add-scan")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addProductScan(
            @RequestParam String productId, HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        String trimmedId = productId.trim();

        Optional<Product> productOpt = productRepository.findById(trimmedId);
        if (productOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Product not found: " + trimmedId);
            return ResponseEntity.ok(response);
        }

        Product product = productOpt.get();
        if (product.getQuantity() <= 0) {
            response.put("success", false);
            response.put("message", "Out of stock: " + product.getName());
            return ResponseEntity.ok(response);
        }

        Map<Product, Integer> cart = getCart(session);
        cart.put(product, cart.getOrDefault(product, 0) + 1);
        product.setQuantity(product.getQuantity() - 1);
        productRepository.save(product);
        session.setAttribute("cart", cart);

        response.put("success", true);
        response.put("message", "Added: " + product.getName());
        response.putAll(buildCartPayload(cart));
        return ResponseEntity.ok(response);
    }

    /**
     * Updates the quantity of a single cart item (full-page flow).
     *
     * <p>The inventory is adjusted by the difference between the requested
     * quantity and the current cart quantity. Setting quantity to {@code 0}
     * removes the item from the cart and fully restores its stock.</p>
     *
     * @param productId barcode / product ID of the item to update
     * @param quantity  new desired quantity (0 to remove)
     * @param session   HTTP session
     * @return redirect to {@code /dashboard}
     */
    @PostMapping("/update")
    public String updateProductQuantityInBill(
            @RequestParam String productId,
            @RequestParam int quantity,
            HttpSession session) {

        productRepository.findById(productId.trim()).ifPresent(product -> {
            Map<Product, Integer> cart = getCart(session);
            int currentQty = cart.getOrDefault(product, 0);
            int delta = quantity - currentQty;

            if (product.getQuantity() >= delta) {
                product.setQuantity(product.getQuantity() - delta);
                productRepository.save(product);

                if (quantity > 0) {
                    cart.put(product, quantity);
                } else {
                    cart.remove(product);
                }
                session.setAttribute("cart", cart);
            }
        });
        return "redirect:/dashboard";
    }

    /**
     * Clears the entire cart, restoring the stock of every removed item.
     *
     * @param session HTTP session
     * @return redirect to {@code /dashboard}
     */
    @GetMapping("/clear")
    public String clearBill(HttpSession session) {
        Map<Product, Integer> cart = getCart(session);
        cart.forEach((product, qty) -> {
            product.setQuantity(product.getQuantity() + qty);
            productRepository.save(product);
        });
        session.removeAttribute("cart");
        return "redirect:/dashboard";
    }

    // -----------------------------------------------------------------------
    // Checkout & Receipt
    // -----------------------------------------------------------------------

    /**
     * Finalises the current cart as a persisted {@link Bill}, clears the session
     * cart, and redirects the user to the printed receipt page.
     *
     * <p>If the cart is empty the user is redirected back to the dashboard
     * without creating a bill.</p>
     *
     * @param session        HTTP session (cart is consumed and removed)
     * @param customerName   customer's full name
     * @param customerNumber customer's contact number
     * @param location       GPS co-ordinates or location string captured on the client
     * @return redirect to {@code /billing/receipt/{billId}}
     */
    @PostMapping("/checkout")
    public String checkout(
            HttpSession session,
            @RequestParam String customerName,
            @RequestParam String customerNumber,
            @RequestParam String location) {

        Map<Product, Integer> cart = getCart(session);
        if (cart.isEmpty()) return "redirect:/dashboard";

        Bill bill = new Bill();
        bill.setTotalAmount(calculateTotal(cart));
        bill.setCustomerName(customerName.trim());
        bill.setCustomerNumber(customerNumber.trim());
        bill.setLocation(location);

        List<BillItem> billItems = new ArrayList<>();
        for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
            BillItem item = new BillItem();
            item.setBill(bill);
            item.setProduct(entry.getKey());
            item.setQuantity(entry.getValue());
            item.setPriceAtSale(entry.getKey().getPrice());
            billItems.add(item);
        }
        bill.setBillItems(billItems);
        billRepository.save(bill);
        session.removeAttribute("cart");

        return "redirect:/billing/receipt/" + bill.getId();
    }

    /**
     * Displays the receipt for a completed bill.
     *
     * @param billId the ID of the bill to display
     * @param model  Spring MVC model
     * @return Thymeleaf template name {@code "receipt"}
     * @throws ResponseStatusException HTTP 404 if the bill ID does not exist
     */
    @GetMapping("/receipt/{billId}")
    public String viewReceipt(@PathVariable Long billId, Model model) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill not found"));
        model.addAttribute("bill", bill);
        return "receipt";
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Retrieves the cart from the session, creating a new empty map if none exists.
     *
     * @param session HTTP session
     * @return mutable cart map (product → quantity)
     */
    @SuppressWarnings("unchecked")
    private Map<Product, Integer> getCart(HttpSession session) {
        Map<Product, Integer> cart = (Map<Product, Integer>) session.getAttribute("cart");
        if (cart == null) cart = new HashMap<>();
        return cart;
    }

    /**
     * Adds one unit of the specified product to the cart, decrementing inventory.
     * Does nothing if the product is not found or is out of stock.
     *
     * @param productId trimmed barcode / product ID
     * @param session   HTTP session
     */
    private void addToCart(String productId, HttpSession session) {
        productRepository.findById(productId).ifPresent(product -> {
            if (product.getQuantity() > 0) {
                Map<Product, Integer> cart = getCart(session);
                cart.put(product, cart.getOrDefault(product, 0) + 1);
                product.setQuantity(product.getQuantity() - 1);
                productRepository.save(product);
                session.setAttribute("cart", cart);
            }
        });
    }

    /**
     * Calculates the monetary total for the given cart.
     *
     * @param cart map of product → quantity
     * @return sum of (price × quantity) for all items
     */
    private double calculateTotal(Map<Product, Integer> cart) {
        return cart.entrySet().stream()
                .mapToDouble(e -> e.getKey().getPrice() * e.getValue())
                .sum();
    }

    /**
     * Builds the JSON payload returned by the AJAX scan endpoint, containing
     * the full cart item list, grand total, and total unit count.
     *
     * @param cart current session cart
     * @return map with keys {@code items}, {@code total}, {@code cartCount}
     */
    private Map<String, Object> buildCartPayload(Map<Product, Integer> cart) {
        List<Map<String, Object>> items = new ArrayList<>();
        double total = 0;
        int cartCount = 0;
        for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
            Product p = entry.getKey();
            int qty = entry.getValue();
            Map<String, Object> item = new HashMap<>();
            item.put("name", p.getName());
            item.put("id", p.getId());
            item.put("price", p.getPrice());
            item.put("quantity", qty);
            item.put("subtotal", p.getPrice() * qty);
            items.add(item);
            total += p.getPrice() * qty;
            cartCount += qty;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("items", items);
        payload.put("total", total);
        payload.put("cartCount", cartCount);
        return payload;
    }
}