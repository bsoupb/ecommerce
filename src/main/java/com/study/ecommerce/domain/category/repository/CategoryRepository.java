package com.study.ecommerce.domain.category.repository;

import com.study.ecommerce.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
