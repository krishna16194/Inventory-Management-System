package com.kr.bill.inventorymangementsystem.controller;

import com.kr.bill.inventorymangementsystem.repository.BillRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/bill-history")
public class BillHistoryController {

    private final BillRepository billRepository;

    public BillHistoryController(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    @GetMapping
    public String showBillHistory(Model model) {
        model.addAttribute("bills", billRepository.findAll());
        return "bill-history";
    }
}
