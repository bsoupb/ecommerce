package com.study.ecommerce.infra;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

//@Entity
public class OptimisticLock {
    @Id
    private Long id;

    @Version
    private Long version;

    // Product(id=1, version=3, stock=100)
}
