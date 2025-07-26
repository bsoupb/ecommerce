package com.study.ecommerce.domain.category.service.query;

import com.study.ecommerce.domain.category.dto.resp.CategoryResponse;
import com.study.ecommerce.domain.category.entity.Category;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryQueryService {

    List<Category> findAll();

    Optional<Category> findById(Long id);

    List<Category> findByParentId(Long parentId);

    Long countProductsByCategory(@Param("categoryId") Long id);

}
