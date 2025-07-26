package com.study.ecommerce.domain.category.service.command;

import com.study.ecommerce.domain.category.dto.req.CategoryRequest;
import com.study.ecommerce.domain.category.dto.resp.CategoryResponse;
import com.study.ecommerce.domain.category.entity.Category;

public interface CategoryCommandService {

    Category addCategory(String name, Integer depth, Long parentId);

    void removeCategory(Long id);
}
