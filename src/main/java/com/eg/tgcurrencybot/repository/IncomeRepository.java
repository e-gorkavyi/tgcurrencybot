package com.eg.tgcurrencybot.repository;

import com.eg.tgcurrencybot.entity.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {
}