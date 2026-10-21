package com.eray.ortakalisveris;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository
        extends JpaRepository<Product, Long> {

    List<Product> findByHousehold_IdOrderByIdAsc(Long householdId);

    Optional<Product> findByIdAndHousehold_Id(
        Long id,
        Long householdId
    );
}