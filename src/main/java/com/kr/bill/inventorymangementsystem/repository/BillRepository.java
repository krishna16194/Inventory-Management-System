package com.kr.bill.inventorymangementsystem.repository;

import com.kr.bill.inventorymangementsystem.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
}
