package com.eg.tgcurrencybot.repository;

import com.eg.tgcurrencybot.entity.Spend;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpendRepository extends JpaRepository<Spend, Long> {
}
