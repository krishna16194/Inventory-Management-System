package com.kr.bill.inventorymangementsystem.controller;

import com.kr.bill.inventorymangementsystem.model.Bill;
import com.kr.bill.inventorymangementsystem.model.BillItem;
import com.kr.bill.inventorymangementsystem.model.Product;
import com.kr.bill.inventorymangementsystem.repository.BillRepository;
import com.kr.bill.inventorymangementsystem.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/billing")
public class BillingController {

    private final ProductRepository productRepository;
    private final BillRepository billRepository;

    public BillingController(ProductRepository productRepository, BillRepository billRepository) {
        this.productRepository = productRepository;
        this.billRepository = billRepository;
    }

    @GetMapping
    public String billingPage(HttpSession session, Model model) {
        Map<Product, Integer> cart = getCart(session);
        model.addAttribute("cart", cart);
        model.addAttribute("total", calculateTotal(cart));
        return "billing";
    }

    @PostMapping("/add")
    public String addProductToBill(@RequestParam String productId, HttpSession session) {
        productRepository.findById(productId).ifPresent(product -> {
            if (product.getQuantity() > 0) {
                Map<Product, Integer> cart = getCart(session);
                cart.put(product, cart.getOrDefault(product, 0) + 1);
                product.setQuantity(product.getQuantity() - 1);
                productRepository.save(product);
                session.setAttribute("cart", cart);
            }
        });
        return "redirect:/dashboard";
    }

    @PostMapping("/update")
    public String updateProductQuantityInBill(@RequestParam String productId, @RequestParam int quantity, HttpSession session) {
        productRepository.findById(productId).ifPresent(product -> {
            Map<Product, Integer> cart = getCart(session);
            int currentQuantityInCart = cart.getOrDefault(product, 0);
            int quantityChange = quantity - currentQuantityInCart;

            if (product.getQuantity() >= quantityChange) {
                product.setQuantity(product.getQuantity() - quantityChange);
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

    @GetMapping("/clear")
    public String clearBill(HttpSession session) {
        Map<Product, Integer> cart = getCart(session);
        cart.forEach((product, quantity) -> {
            product.setQuantity(product.getQuantity() + quantity);
            productRepository.save(product);
        });
        session.removeAttribute("cart");
        return "redirect:/dashboard";
    }

    @PostMapping("/checkout")
    public String checkout(HttpSession session, @RequestParam String customerName, @RequestParam String customerNumber, @RequestParam String location) {
        Map<Product, Integer> cart = getCart(session);
        if (cart.isEmpty()) {
            return "redirect:/dashboard";
        }

        Bill bill = new Bill();
        bill.setTotalAmount(calculateTotal(cart));
        bill.setCustomerName(customerName);
        bill.setCustomerNumber(customerNumber);
        bill.setLocation(location);
        
        List<BillItem> billItems = new ArrayList<>();
        for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
            BillItem billItem = new BillItem();
            billItem.setBill(bill);
            billItem.setProduct(entry.getKey());
            billItem.setQuantity(entry.getValue());
            billItem.setPriceAtSale(entry.getKey().getPrice());
            billItems.add(billItem);
        }
        bill.setBillItems(billItems);
        
        billRepository.save(bill);
        session.removeAttribute("cart");

        return "redirect:/billing/receipt/" + bill.getId();
    }

    @GetMapping("/receipt/{billId}")
    public String viewReceipt(@PathVariable Long billId, Model model) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill not found"));
        model.addAttribute("bill", bill);
        return "receipt";
    }

    private Map<Product, Integer> getCart(HttpSession session) {
        Map<Product, Integer> cart = (Map<Product, Integer>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
        }
        return cart;
    }

    private double calculateTotal(Map<Product, Integer> cart) {
        return cart.entrySet().stream()
                .mapToDouble(entry -> entry.getKey().getPrice() * entry.getValue())
                .sum();
    }
}
